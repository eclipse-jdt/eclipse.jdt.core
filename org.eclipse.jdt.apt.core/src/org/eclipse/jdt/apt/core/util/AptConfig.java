/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com, wharley@bea.com - initial API and implementation
 *    het@google.com - Bug 423254 - There is no way to tell if a project's factory path is different from the workspace default
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.generatedfile.ClasspathUtil;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Accesses configuration data for APT.
 * Note that some of the code in org.eclipse.jdt.ui reads and writes settings
 * data directly, rather than calling into the methods of this class.
 *
 * This class is static.  Instances should not be constructed.
 *
 * Helpful information about the Eclipse preferences mechanism can be found at:
 * http://dev.eclipse.org/viewcvs/index.cgi/~checkout~/platform-core-home/documents/user_settings/faq.html
 * @since 3.5
 */
public class AptConfig {

	/** regex to identify substituted token in path variables */
	private static final String PATHVAR_TOKEN = "^%[^%/\\\\ ]+%.*"; //$NON-NLS-1$
	/** path variable meaning "workspace root" */
	private static final String PATHVAR_ROOT = "%ROOT%"; //$NON-NLS-1$
	/** path variable meaning "project root" */
	private static final String PATHVAR_PROJECTROOT = "%PROJECT.DIR%"; //$NON-NLS-1$

	/*
	 * Hide constructor; this is a static object
	 */
	private AptConfig() {}

	/**
     * Add the equivalent of -Akey=val to the list of processor options.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     * @param jproj a project, or null to set the option workspace-wide.
     * @param val can be null (equivalent to -Akey).  This does not mean
     * remove the key; for that functionality, @see #removeProcessorOption(IJavaProject, String).
     */
    public static void addProcessorOption(IJavaProject jproj, String key, String val) {
    	if (key == null || key.length() < 1) {
    		throw new IllegalArgumentException();
    	}
		IScopeContext context = (null != jproj) ?
				new ProjectScope(jproj.getProject()) : InstanceScope.INSTANCE;
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID + "/" +  //$NON-NLS-1$
				AptPreferenceConstants.APT_PROCESSOROPTIONS);
		String nonNullVal = val == null ? AptPreferenceConstants.APT_NULLVALUE : val;
		node.put(key, nonNullVal);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			AptPlugin.log(e, "Unable to save annotation processor option" + key); //$NON-NLS-1$
		}
    }

	/**
     * Remove an option from the list of processor options.
     * @param jproj a project, or null to remove the option workspace-wide.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     */
    public static void removeProcessorOption(IJavaProject jproj, String key) {
    	if (key == null || key.length() < 1) {
    		throw new IllegalArgumentException();
    	}
    	IScopeContext context = (null != jproj) ?
				new ProjectScope(jproj.getProject()) : InstanceScope.INSTANCE;
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID + "/" +  //$NON-NLS-1$
				AptPreferenceConstants.APT_PROCESSOROPTIONS);
		node.remove(key);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			AptPlugin.log(e, "Unable to save annotation processor option" + key); //$NON-NLS-1$
		}
    }

	/**
	 * @deprecated Use {@link #getProcessorOptions(IJavaProject, boolean)} or
	 *             {@link #getRawProcessorOptions(IJavaProject)}
	 */
    @Deprecated
    public static Map<String, String> getProcessorOptions(IJavaProject jproj) {
    		return getProcessorOptions(jproj, false);
    }

	/**
     * Get the options that are presented to annotation processors by the
     * AnnotationProcessorEnvironment.  Options are key/value pairs which
     * are set in the project properties.
     *
     * Option values can begin with a percent-delimited token representing
     * a classpath variable or one of several predefined values.  The token
     * must either be followed by a path delimiter, or be the entire value.
     * Such tokens will be replaced with their resolved value.  The predefined
     * values are <code>%ROOT%</code>, which is replaced by the absolute pathname
     * of the workspace root directory, and <code>%PROJECT.DIR%</code>, which
     * will be replaced by the absolute pathname of the project root directory.
     * For example, a value of <code>%ECLIPSE_HOME%/configuration/config.ini</code>
     * might be resolved to <code>d:/eclipse/configuration/config.ini</code>.
     *
     * This method returns some options which are set programmatically but
     * are not directly editable, are not displayed in the configuration GUI,
     * and are not persisted to the preference store.  This is meant to
     * emulate the behavior of Sun's apt command-line tool, which passes
     * most of its command line options to the processor environment.  The
     * programmatically set options are:
     *  <code>-classpath</code> [set to Java build path]
     *  <code>-sourcepath</code> [set to Java source path]
     *  <code>-s</code> [set to generated src dir]
     *  <code>-d</code> [set to binary output dir]
     *  <code>-target</code> [set to compiler target version]
     *  <code>-source</code> [set to compiler source version]
     *
     * There are some slight differences between the options returned by this
     * method and the options returned from this implementation of @see
     * AnnotationProcessorEnvironment#getOptions().  First, that method returns
     * additional options which are only meaningful during a build, such as
     * <code>phase</code>.  Second, that method also adds alternate encodings
     * of each option, to be compatible with a bug in Sun's apt implementation:
     * specifically, for each option key="k", value="v", an additional option
     * is created with key="-Ak=v", value=null.  This includes the user-created
     * options, but does not include the programmatically defined options listed
     * above.
     *
     * @param jproj a project, or null to query the workspace-wide setting.
     * @param isTestCode if true, the programmatically set options are computed for test code compilation
     * @return a mutable, possibly empty, map of (key, value) pairs.
     * The value part of a pair may be null (equivalent to "-Akey" on the Sun apt
     * command line).
     * The value part may contain spaces.
	 * @since 3.6
     */
    public static Map<String, String> getProcessorOptions(IJavaProject jproj, boolean isTestCode) {
    	Map<String,String> rawOptions = getRawProcessorOptions(jproj);
    	// map is large enough to also include the programmatically generated options
    	Map<String, String> options = new HashMap<>(rawOptions.size() + 6);

    	// Resolve path metavariables like %ROOT%
    	for (Map.Entry<String, String> entry : rawOptions.entrySet()) {
    		String resolvedValue = resolveVarPath(jproj, entry.getValue());
    		String value = (resolvedValue == null) ? entry.getValue() : resolvedValue;
    		options.put(entry.getKey(), value);
    	}

    	if (jproj == null) {
    		// there are no programmatically set options at the workspace level
    		return options;
    	}

    	IWorkspaceRoot root = jproj.getProject().getWorkspace().getRoot();

    	// Add sourcepath and classpath variables
    	try {
    		IClasspathEntry[] classpathEntries = jproj.getResolvedClasspath(true);
    		Set<String> classpath = new LinkedHashSet<>();
    		Set<String> sourcepath = new LinkedHashSet<>();

    		// For projects on the classpath, loops can exist; need to make sure we
    		// don't loop forever
    		Set<IJavaProject> projectsProcessed = new HashSet<>();
    		projectsProcessed.add(jproj);
    		for (IClasspathEntry entry : classpathEntries) {
			if (!isTestCode && entry.isTest()) {
				continue;
			}
    			int kind = entry.getEntryKind();
    			if (kind == IClasspathEntry.CPE_LIBRARY) {
	    			IPath cpPath = entry.getPath();

	    			IResource res = root.findMember(cpPath);

	    			// If res is null, the path is absolute (it's an external jar)
	    			if (res == null) {
	    				classpath.add(cpPath.toOSString());
	    			}
	    			else {
	    				// It's relative
	    				classpath.add(res.getLocation().toOSString());
	    			}
    			}
    			else if (kind == IClasspathEntry.CPE_SOURCE) {
    				IResource res = root.findMember(entry.getPath());
    				if (res == null) {
    					continue;
    				}
    				IPath srcPath = res.getLocation();
    				if (srcPath == null) {
    					continue;
    				}

    				sourcepath.add(srcPath.toOSString());
    			}
    			else if (kind == IClasspathEntry.CPE_PROJECT) {
    				// Add the dependent project's build path and classpath to ours
    				IPath otherProjectPath = entry.getPath();
    				IProject otherProject = root.getProject(otherProjectPath.segment(0));

    				// Note: JavaCore.create() is safe, even if the project is null --
    				// in that case, we get null back
    				IJavaProject otherJavaProject = JavaCore.create(otherProject);

    				// If it doesn't exist, ignore it
    				if (otherJavaProject != null && otherJavaProject.getProject().isOpen()) {
    					addProjectClasspath(root, otherJavaProject, projectsProcessed, classpath, isTestCode);
    				}
    			}
    		}
    		// if you add options here, also add them in isAutomaticProcessorOption(),
    		// and document them in docs/reference/automatic_processor_options.html.

    		// Classpath and sourcepath
    		options.put("-classpath",convertPathCollectionToString(classpath)); //$NON-NLS-1$
    		options.put("-sourcepath", convertPathCollectionToString(sourcepath)); //$NON-NLS-1$

    		// Get absolute path for generated source dir
    		IFolder genSrcDir = jproj.getProject().getFolder(isTestCode ? getGenTestSrcDir(jproj) : getGenSrcDir(jproj));
    		String genSrcDirString = genSrcDir.getRawLocation().toOSString();
    		options.put("-s", genSrcDirString); //$NON-NLS-1$

    		// Absolute path for bin dir as well
    		IPath binPath = isTestCode ? ClasspathUtil.findTestOutputLocation(jproj.getRawClasspath()) : jproj.getOutputLocation();
    		IResource binPathResource = root.findMember(binPath);
    		String binDirString;
    		if (binPathResource != null) {
    			binDirString = root.findMember(binPath).getLocation().toOSString();
    		}
    		else {
    			binDirString = binPath.toOSString();
    		}
    		options.put("-d", binDirString); //$NON-NLS-1$

    		String target = jproj.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true);
    		options.put("-target", target); //$NON-NLS-1$

    		String source = jproj.getOption(JavaCore.COMPILER_SOURCE, true);
    		options.put("-source", source); //$NON-NLS-1$
    	}
    	catch (JavaModelException jme) {
    		AptPlugin.log(jme, "Could not get the classpath for project: " + jproj); //$NON-NLS-1$
    	}

    	return options;
    }

	/**
	 * If the value starts with a path variable such as %ROOT%, replace it with
	 * the absolute path.
	 * @param value the value of a -Akey=value command option
	 */
	private static String resolveVarPath(IJavaProject jproj, String value) {
		if (value == null) {
			return null;
		}
		// is there a token to substitute?
		if (!Pattern.matches(PATHVAR_TOKEN, value)) {
			return value;
		}
		IPath path = new Path(value);
		String firstToken = path.segment(0);
		// If it matches %ROOT%/project, it is a project-relative path.
		if (PATHVAR_ROOT.equals(firstToken)) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource proj = root.findMember(path.segment(1));
			if (proj == null) {
				return value;
			}
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(2);
			IPath absoluteProjPath = proj.getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}

		// If it matches %PROJECT.DIR%/project, the path is relative to the current project.
		if (jproj != null && PATHVAR_PROJECTROOT.equals(firstToken)) {
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(1);
			IPath absoluteProjPath = jproj.getProject().getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}

		// otherwise it's a classpath-var-based path.
		String cpvName = firstToken.substring(1, firstToken.length() - 1);
		IPath cpvPath = JavaCore.getClasspathVariable(cpvName);
		if (cpvPath != null) {
			IPath resolved = cpvPath.append(path.removeFirstSegments(1));
			return resolved.toOSString();
		}
		else {
			return value;
		}
	}

	// We need this as a separate method, as we'll put dependent projects' output
    // on the classpath
    private static void addProjectClasspath(
    		IWorkspaceRoot root,
    		IJavaProject otherJavaProject,
    		Set<IJavaProject> projectsProcessed,
    		Set<String> classpath,
    		boolean isTestCode) {

    	// Check for cycles. If we've already seen this project,
    	// no need to go any further.
    	if (projectsProcessed.contains(otherJavaProject)) {
			return;
		}
    	projectsProcessed.add(otherJavaProject);

    	try {
    		// Add the output directory first as a binary entry for other projects
    		IPath binPath = otherJavaProject.getOutputLocation();
    		IResource binPathResource = root.findMember(binPath);
    		String binDirString;
    		if (binPathResource != null) {
    			IPath binPathLocation = binPathResource.getLocation();
    			if (binPathLocation == null) {
    				AptPlugin.logWarning(null, "Failed to resolve output location for the following project: " + otherJavaProject); //$NON-NLS-1$
    				return;
    			}
    			binDirString = binPathLocation.toOSString();
    		}
    		else {
    			binDirString = binPath.toOSString();
    		}
    		classpath.add(binDirString);

    		// Now the rest of the classpath
    		IClasspathEntry[] classpathEntries = otherJavaProject.getResolvedClasspath(true);
    		for (IClasspathEntry entry : classpathEntries) {
    			if (!isTestCode && entry.isTest()) {
    				continue;
    			}
    			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
    				IPath cpPath = entry.getPath();

	    			IResource res = root.findMember(cpPath);

	    			// If res is null, the path is absolute (it's an external jar)
	    			if (res == null) {
	    				classpath.add(cpPath.toOSString());
	    			}
	    			else {
	    				// It's relative
	    				classpath.add(res.getLocation().toOSString());
	    			}
    			}
    			else if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
    				IPath otherProjectPath = entry.getPath();
    				IProject otherProject = root.getProject(otherProjectPath.segment(0));
					IJavaProject yetAnotherJavaProject = JavaCore.create(otherProject);
					if (yetAnotherJavaProject != null) {
						addProjectClasspath(root, yetAnotherJavaProject, projectsProcessed, classpath, isTestCode);
					}
    			}
    			// Ignore source types
    		}
    	}
    	catch (JavaModelException jme) {
    		AptPlugin.log(jme, "Failed to get the classpath for the following project: " + otherJavaProject); //$NON-NLS-1$
    	}
	}

    private static String convertPathCollectionToString(Collection<String> paths) {
    	if (paths.size() == 0) {
    		return ""; //$NON-NLS-1$
    	}
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	for (String path : paths) {
    		if (first) {
    			first = false;
    		}
    		else {
    			sb.append(File.pathSeparatorChar);
    		}
    		sb.append(path);
    	}
    	return sb.toString();
    }

	/**
     * Set all the processor options in one call.  This will delete any
     * options that are not passed in, so callers who do not wish to
     * destroy pre-existing options should use addProcessorOption() instead.
     * @param options a map of keys to values.  The keys should not include
     * any automatic options (@see #isAutomaticProcessorOption(String)),
     * and the "-A" should not be included.  That is, to perform the
     * equivalent of the apt command line "-Afoo=bar", use the key "foo"
     * and the value "bar".  Keys cannot contain spaces; values can
     * contain anything at all.  Keys cannot be null, but values can be.
     */
    public static void setProcessorOptions(Map<String, String> options, IJavaProject jproj) {
		IScopeContext context = (null != jproj) ?
				new ProjectScope(jproj.getProject()) : InstanceScope.INSTANCE;

    	// TODO: this call is needed only for backwards compatibility with
	    // settings files previous to 2005.11.13.  At some point it should be
	    // removed.
    	removeOldStyleSettings(context);

		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID + "/" +  //$NON-NLS-1$
				AptPreferenceConstants.APT_PROCESSOROPTIONS);
		try {
			node.clear();
			for (Entry<String, String> option : options.entrySet()) {
				String nonNullVal = option.getValue() == null ?
						AptPreferenceConstants.APT_NULLVALUE : option.getValue();
				node.put(option.getKey(), nonNullVal);
			}
			node.flush();
		} catch (BackingStoreException e) {
			AptPlugin.log(e, "Unable to save annotation processor options"); //$NON-NLS-1$
		}
    }

    /**
     * Is the named option automatically generated in getProcessorOptions(),
     * or did it come from somewhere else, such as a -A processor option?
     * @param key the name of an AnnotationProcessorEnvironment option
     * @return true if the option is automatically set.
     */
	public static boolean isAutomaticProcessorOption(String key) {
		if ("-classpath".equals(key)) //$NON-NLS-1$
			return true;
		if ("-sourcepath".equals(key)) //$NON-NLS-1$
			return true;
		if ("-s".equals(key)) //$NON-NLS-1$
			return true;
		if ("-d".equals(key)) //$NON-NLS-1$
			return true;
		if ("-target".equals(key)) //$NON-NLS-1$
			return true;
		if ("-source".equals(key)) //$NON-NLS-1$
			return true;
		return false;
	}

	/**
     * Get the options that are presented to annotation processors by the
     * AnnotationProcessorEnvironment.  The -A and = are stripped out, so
     * (key, value) is the equivalent of -Akey=value.
     *
     * This method differs from getProcessorOptions in that the options returned
     * by this method do NOT include any programmatically set options.  This
     * method returns only the options that are persisted to the preference
     * store and that are displayed in the configuration GUI.
     *
     * @param jproj a project, or null to query the workspace-wide setting.
     * If jproj is not null, but the project has no per-project settings,
     * this method will fall back to the workspace-wide settings.
     * @return a mutable, possibly empty, map of (key, value) pairs.
     * The value part of a pair may be null (equivalent to "-Akey").
     * The value part can contain spaces, if it is quoted: -Afoo="bar baz".
     */
	public static Map<String, String> getRawProcessorOptions(IJavaProject jproj) {
        Map<String, String> options = new HashMap<>();

	    // TODO: this code is needed only for backwards compatibility with
	    // settings files previous to 2005.11.13.  At some point it should be
	    // removed.
		// If an old-style setting exists, add it into the mix for backward
		// compatibility.
		options.putAll(getOldStyleRawProcessorOptions(jproj));

		// Fall back from project to workspace scope on an all-or-nothing basis,
		// not value by value.  (Never fall back to default scope; there are no
		// default processor options.)  We can't use IPreferencesService for this
		// as we would normally do, because we don't know the names of the keys.
		IScopeContext[] contexts;
		if (jproj != null) {
			contexts = new IScopeContext[] {
					new ProjectScope(jproj.getProject()), InstanceScope.INSTANCE };
		}
		else {
			contexts = new IScopeContext[] { InstanceScope.INSTANCE };
		}
		for (IScopeContext context : contexts) {
			IEclipsePreferences prefs = context.getNode(AptPlugin.PLUGIN_ID);
			try {
				if (prefs.childrenNames().length > 0) {
					IEclipsePreferences procOptionsNode = context.getNode(
							AptPlugin.PLUGIN_ID + "/" + AptPreferenceConstants.APT_PROCESSOROPTIONS); //$NON-NLS-1$
					if (procOptionsNode != null) {
						for (String key : procOptionsNode.keys()) {
							String nonNullVal = procOptionsNode.get(key, null);
							String val = AptPreferenceConstants.APT_NULLVALUE.equals(nonNullVal) ?
									null : nonNullVal;
							options.put(key, val);
						}
						break;
					}
				}
			} catch (BackingStoreException e) {
				AptPlugin.log(e, "Unable to load annotation processor options"); //$NON-NLS-1$
			}
		}
		return options;
	}

	/**
     * TODO: this code is needed only for backwards compatibility with
     * settings files previous to 2005.11.13.  At some point it should be
     * removed.
     * Get the processor options as an APT-style string ("-Afoo=bar -Abaz=quux")
	 */
	private static Map<String, String> getOldStyleRawProcessorOptions(IJavaProject jproj) {
		Map<String, String> options;
		String allOptions = getString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS);
		if (null == allOptions) {
    		options = new HashMap<>();
    	}
    	else {
    		ProcessorOptionsParser op = new ProcessorOptionsParser(allOptions);
    		options = op.parse();
    	}
		return options;
	}
    /**
     * TODO: this code is needed only for backwards compatibility with
     * settings files previous to 2005.11.13.  At some point it should be
     * removed.
     *
     * Used to parse an apt-style command line string into a map of key/value
     * pairs.
     * Parsing ignores errors and simply tries to gobble up as many well-formed
     * pairs as it can find.
     */
    private static class ProcessorOptionsParser {
    	final String _s;
    	int _start; // everything before this is already parsed.
    	boolean _hasVal; // does the last key found have a value token?

    	public ProcessorOptionsParser(String s) {
    		_s = s;
    		_start = 0;
    		_hasVal = false;
    	}

     	public Map<String, String> parse() {
        	Map<String, String> options = new HashMap<>();
        	String key;
        	while (null != (key = parseKey())) {
       			options.put(key, parseVal());
        	}
         	return options;
    	}

    	/**
    	 * Skip until a well-formed key (-Akey[=val]) is found, and
    	 * return the key.  Set _start to the beginning of the value,
    	 * or to the first character after the end of the key and
    	 * delimiter, for a valueless key.  Set _hasVal according to
    	 * whether a value was found.
    	 * @return a key, or null if no well-formed keys can be found.
    	 */
    	private String parseKey() {
    		String key;
    		int spaceAt = -1;
    		int equalsAt = -1;

    		_hasVal = false;

    		while (true) {
	        	_start = _s.indexOf("-A", _start); //$NON-NLS-1$
	        	if (_start < 0) {
	        		return null;
	        	}

	    		// we found a -A.  The key is everything up to the next '=' or ' ' or EOL.
	    		_start += 2;
	    		if (_start >= _s.length()) {
	    			// it was just a -A, nothing following.
	    			return null;
	    		}

	    		spaceAt = _s.indexOf(' ', _start);
	    		equalsAt = _s.indexOf('=', _start);
	    		if (spaceAt == _start || equalsAt == _start) {
	    			// false alarm.  Keep trying.
	    			++_start;
	    			continue;
	    		}
	    		break;
    		}

    		// We found a legitimate -A with some text after it.
    		// Where does the key end?
    		if (equalsAt > 0) {
    			if (spaceAt < 0 || equalsAt < spaceAt) {
    				// there is an equals, so there is a value.
    				key = _s.substring(_start, equalsAt);
    				_start = equalsAt + 1;
    				_hasVal = (_start < _s.length());
    			}
    			else {
    				// the next thing is a space, so this is a valueless key
    				key = _s.substring(_start, spaceAt);
    				_start = spaceAt + 1;
    			}
    		}
    		else {
	    		if (spaceAt < 0) {
					// no equals sign and no spaces: a valueless key, up to the end of the string.
					key = _s.substring(_start);
					_start = _s.length();
	    		}
	    		else {
    				// the next thing is a space, so this is a valueless key
    				key = _s.substring(_start, spaceAt);
    				_start = spaceAt + 1;
	    		}
    		}
        	return key;
    	}

    	/**
    	 * A value token is delimited by a space; but spaces inside quoted
    	 * regions are ignored.  A value may include multiple quoted regions.
    	 * An unmatched quote is treated as if there was a matching quote at
    	 * the end of the string.  Quotes are returned as part of the value.
    	 * @return the value, up to the next nonquoted space or end of string.
    	 */
    	private String parseVal() {
    		if (!_hasVal || _start < 0 || _start >= _s.length()) {
    			return null;
    		}
    		boolean inQuotedRegion = false;
    		int start = _start;
    		int end = _start;
    		while (end < _s.length()) {
    			char c = _s.charAt(end);
    			if (c == '"') {
    				inQuotedRegion = !inQuotedRegion;
    			}
    			else if (!inQuotedRegion && c == ' ') {
    				// end of token.
    				_start = end + 1;
    				break;
    			}
    			++end;
    		}

    		return _s.substring(start, end);
    	}
    }

    /**
     * TODO: this code is needed only for backwards compatibility with
     * settings files previous to 2005.11.13.  At some point it should be
     * removed.
     * Delete the key that saves annotation processor options as a single
     * command-line-type string ("-Afoo=bar -Abaz=quux").
     */
    private static void removeOldStyleSettings(IScopeContext context) {
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.remove(AptPreferenceConstants.APT_PROCESSOROPTIONS);
    }

    /**
     * Flush unsaved preferences and perform any other config-related shutdown.
     * This is called once, from AptPlugin.shutdown().
     */
    public static void dispose() {
    	try {
    		InstanceScope.INSTANCE.getNode(AptPlugin.PLUGIN_ID).flush();
    	}
    	catch (BackingStoreException e) {
    		// log failure and continue
    		AptPlugin.log(e, "Couldn't flush preferences to disk"); //$NON-NLS-1$
    	}
    }

	/**
	 * Initialize preferences lookups, and register change listeners.
	 * This is called once, from AptPlugin.startup().
	 */
	public static void initialize() {
		// If we cached workspace-level preferences, we would want to install
		// some change listeners here.
	}

	/**
	 * Is annotation processing turned on for this project?
	 * <p>
	 * Prior to Eclipse 3.3, this read the org.eclipse.jdt.apt.aptEnabled
	 * setting.  In Eclipse 3.3, it reads the org.eclipse.jdt.core.compiler.processingEnabled
	 * setting; the result is logically or-ed with value of the older setting in order to
	 * preserve backward compatibility.
	 * @param jproject an IJavaProject, or null to request workspace preferences.
	 * @return true if annotation processing is turned on.
	 */
	public static boolean isEnabled(IJavaProject jproject) {
		if ("enabled".equals(getString(jproject, AptPreferenceConstants.APT_PROCESSANNOTATIONS))) { //$NON-NLS-1$
			return true;
		}
		// backward compatibility: also return true if old setting is enabled
		return getBoolean(jproject, AptPreferenceConstants.APT_ENABLED);
	}


	/**
	 * Turn annotation processing on or off for this project.
	 * <p>
	 * Prior to Eclipse 3.3, this affected the org.eclipse.jdt.apt.aptEnabled
	 * setting.  In Eclipse 3.3, it affects the org.eclipse.jdt.core.compiler.processingEnabled
	 * setting; the older setting is still set (and read) in order to preserve backward
	 * compatibility.
	 * @param jproject an IJavaProject, or null to set workspace preferences.
	 * @param enabled
	 */
	public static void setEnabled(IJavaProject jproject, boolean enabled) {
		if (jproject == null && enabled == true) {
			IllegalArgumentException e = new IllegalArgumentException();
			IStatus status = AptPlugin.createWarningStatus(e,
				"Illegal attempt to enable annotation processing workspace-wide"); //$NON-NLS-1$
			AptPlugin.log(status);
			throw e;
		}
		setString(jproject, AptPreferenceConstants.APT_PROCESSANNOTATIONS,
				enabled ? AptPreferenceConstants.ENABLED : AptPreferenceConstants.DISABLED);
		// backward compatibility: also save old setting
		setBoolean(jproject, AptPreferenceConstants.APT_ENABLED, enabled);
		if (enabled) {
			AptProject aptProject = AptPlugin.getAptProject(jproject);
			aptProject.getGeneratedSourceFolderManager(true).ensureFolderExists();
			aptProject.getGeneratedSourceFolderManager(false).ensureFolderExists();
		}
	}

	/**
	 * Is annotation processing turned on during reconcile, or only during build?
	 * Note that if isEnabled() is false, processing will not occur at all; the
	 * two settings are independent.
	 * @param jproject an IJavaProject to query, or null to get the default value.
	 * @return true if processing is enabled during both reconcile and build
	 */
	public static boolean shouldProcessDuringReconcile(IJavaProject jproject) {
		return getBoolean(jproject, AptPreferenceConstants.APT_RECONCILEENABLED);
	}

	/**
	 * Turn processing during reconcile on or off.  Processing during build is
	 * unaffected.  Note that if isEnabled() is false, processing will not occur
	 * at all; the two settings are independent.
	 * @param jproject the IJavaProject to modify.  This setting is only valid
	 * on individual projects.
	 */
	public static void setProcessDuringReconcile(IJavaProject jproject, boolean enabled) {
		setBoolean(jproject, AptPreferenceConstants.APT_RECONCILEENABLED, enabled);
	}

	private static boolean getBoolean(IJavaProject jproj, String optionName) {
		IPreferencesService service = Platform.getPreferencesService();
		IScopeContext[] contexts;
		if (jproj != null) {
			contexts = new IScopeContext[] {
					new ProjectScope(jproj.getProject()), InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		else {
			contexts = new IScopeContext[] { InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		return service.getBoolean(
				AptPlugin.PLUGIN_ID,
				optionName,
				Boolean.parseBoolean(AptPreferenceConstants.DEFAULT_OPTIONS_MAP.get(optionName)),
				contexts);
	}

	/**
	 * Get a factory path corresponding to the default values: if jproj is
	 * non-null, return the current workspace factory path (workspace prefs
	 * are the default for a project); if jproj is null, return the default
	 * list of plugin factories (which is the "factory default").
	 */
	public static IFactoryPath getDefaultFactoryPath(IJavaProject jproj) {
		return FactoryPathUtil.getDefaultFactoryPath(jproj);
	}

	/**
	 * Get the factory path for a given project or for the workspace.
	 * @param jproj the project, or null to get the factory path for the workspace.
	 * @return a FactoryPath representing the current state of the specified project.
	 * Note that changes made to the project after this call will not affect the
	 * returned object - that is, it behaves like a value, not like a live link to
	 * the project state.
	 */
	public static IFactoryPath getFactoryPath(IJavaProject jproj) {
		return FactoryPathUtil.getFactoryPath(jproj);
	}

	/**
	 * Set the factory path for a given project or for the workspace.
	 * Does not perform any validation on the path.
	 * @param jproj the project, or null to set the factory path for the workspace.
	 * @param path a factory path, or null to reset the factory path to the default.
	 */
	public static void setFactoryPath(IJavaProject jproj, IFactoryPath path)
			throws CoreException
	{
		FactoryPath fp = (FactoryPath)path;
		FactoryPathUtil.setFactoryPath(jproj, fp);
		// Project-specific factory path files are resources, so changes
		// get picked up by the resource listener.  Workspace changes aren't.
		if (jproj == null) {
			AnnotationProcessorFactoryLoader.getLoader().resetAll();
		}
	}

	/**
	 * Does this project have a factory path that is different from the
	 * workspace default?
	 *
	 * @return true if there is a project-specific factory path.
	 */
	public static boolean hasProjectSpecificFactoryPath(IJavaProject jproj) {
		if (null == jproj) {
			// say no, even if workspace-level factory path does exist.
			return false;
		}
		return FactoryPathUtil.doesFactoryPathFileExist(jproj)
				&& !getFactoryPath(jproj).equals(getDefaultFactoryPath(jproj));
	}

	/**
	 * Helper method to get a single preference setting, e.g., APT_GENSRCDIR.
	 * This is a different level of abstraction than the processor -A settings!
	 * The -A settings are all contained under one single preference node,
	 * APT_PROCESSOROPTIONS.  Use @see #getProcessorOptions(IJavaProject) to
	 * get the -A settings; use @see #getOptions(IJavaProject) to get all the
	 * preference settings as a map; and use this helper method to get a single
	 * preference setting.
	 *
	 * @param jproj the project, or null for workspace.
	 * @param optionName a preference constant from @see AptPreferenceConstants.
	 * @return the string value of the setting.
	 */
    public static String getString(IJavaProject jproj, String optionName) {
		IPreferencesService service = Platform.getPreferencesService();
		IScopeContext[] contexts;
		if (jproj != null) {
			contexts = new IScopeContext[] {
					new ProjectScope(jproj.getProject()), InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		else {
			contexts = new IScopeContext[] { InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		String pluginId = null;
		if (AptPreferenceConstants.APT_PROCESSANNOTATIONS.equals(optionName)) {
			pluginId = JavaCore.PLUGIN_ID;
		}
		else {
			pluginId = AptPlugin.PLUGIN_ID;
		}
		return service.getString(
				pluginId,
				optionName,
				AptPreferenceConstants.DEFAULT_OPTIONS_MAP.get(optionName),
				contexts);
	}

    public static String getGenSrcDir(IJavaProject jproject) {
    	return getString(jproject, AptPreferenceConstants.APT_GENSRCDIR);
    }

    public static void setGenSrcDir(IJavaProject jproject, String dirString) {
    	if (!GeneratedSourceFolderManager.validate(jproject, dirString)) {
    		throw new IllegalArgumentException("Illegal name for generated source folder: " + dirString); //$NON-NLS-1$
    	}
    	setString(jproject, AptPreferenceConstants.APT_GENSRCDIR, dirString);
    }

    /**
	 * @since 3.6
	 */
    public static String getGenTestSrcDir(IJavaProject jproject) {
    	return getString(jproject, AptPreferenceConstants.APT_GENTESTSRCDIR);
    }

    /**
	 * @since 3.6
	 */
    public static void setGenTestSrcDir(IJavaProject jproject, String dirString) {
    	if (!GeneratedSourceFolderManager.validate(jproject, dirString)) {
    		throw new IllegalArgumentException("Illegal name for generated test source folder: " + dirString); //$NON-NLS-1$
    	}
    	setString(jproject, AptPreferenceConstants.APT_GENTESTSRCDIR, dirString);
    }

    public static boolean validateGenSrcDir(IJavaProject jproject, String dirName) {
    	return GeneratedSourceFolderManager.validate(jproject, dirName);
    }

	private static void setBoolean(IJavaProject jproject, String optionName, boolean value) {
		IScopeContext context = (null != jproject) ?
				new ProjectScope(jproject.getProject()) : InstanceScope.INSTANCE;
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		// get old val as a String, so it can be null if setting doesn't exist yet
		String oldValue = node.get(optionName, null);
		node.putBoolean(optionName, value);
		if (jproject != null && oldValue == null || (value != Boolean.parseBoolean(oldValue))) {
			AptProject aproj = AptPlugin.getAptProject(jproject);
			aproj.preferenceChanged(optionName);
		}
		flushPreference(optionName, node);
	}

	private static void setString(IJavaProject jproject, String optionName, String value) {
		IScopeContext context = (null != jproject) ?
				new ProjectScope(jproject.getProject()) : InstanceScope.INSTANCE;
		IEclipsePreferences node;
		if (AptPreferenceConstants.APT_PROCESSANNOTATIONS.equals(optionName)) {
			node = context.getNode(JavaCore.PLUGIN_ID);
		}
		else {
			node = context.getNode(AptPlugin.PLUGIN_ID);
		}
		String oldValue = node.get(optionName, null);
		node.put(optionName, value);
		if (jproject != null && !value.equals(oldValue)) {
			AptProject aproj = AptPlugin.getAptProject(jproject);
			aproj.preferenceChanged(optionName);
		}
		flushPreference(optionName, node);
	}

	private static void flushPreference(String optionName, IEclipsePreferences node) {
		try {
			node.flush();
		}
		catch (BackingStoreException e){
			AptPlugin.log(e, "Failed to save preference: " + optionName); //$NON-NLS-1$
		}
	}

}
