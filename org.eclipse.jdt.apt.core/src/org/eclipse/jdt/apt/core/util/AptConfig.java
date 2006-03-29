/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com, wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
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
 */
public class AptConfig {
	
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
				new ProjectScope(jproj.getProject()) : new InstanceScope();
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
				new ProjectScope(jproj.getProject()) : new InstanceScope();
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
     * Get the options that are presented to annotation processors by the
     * AnnotationProcessorEnvironment.  The -A and = are stripped out, so 
     * (key, value) is the equivalent of -Akey=value.
     * 
     * This method returns some options which are set programmatically but 
     * are not directly editable, are not displayed in the configuration GUI, 
     * and are not persisted to the preference store.  This is meant to
     * emulate the behavior of Sun's apt command-line tool, which passes
     * most of its command line options to the processor environment.  The
     * programmatically set options are:
     *  -classpath [set to Java build path]
     *  -sourcepath [set to Java source path]
     *  -s [set to generated src dir]
     *  -d [set to binary output dir]
     *  -target [set to compiler target version]
     *  -source [set to compiler source version]
     * 
     * @param jproj a project, or null to query the workspace-wide setting.
     * @return a mutable, possibly empty, map of (key, value) pairs.  
     * The value part of a pair may be null (equivalent to "-Akey").
     * The value part can contain spaces, if it is quoted: -Afoo="bar baz".
     */
    public static Map<String, String> getProcessorOptions(IJavaProject jproj) {
    	Map<String,String> options;
    	options = getRawProcessorOptions(jproj);
    	if (jproj == null) {
    		// there are no programmatically set options at the workspace level
    		return options;
    	}
    	
    	IWorkspaceRoot root = jproj.getProject().getWorkspace().getRoot();
    	
    	// Add sourcepath and classpath variables
    	try {
    		IClasspathEntry[] classpathEntries = jproj.getResolvedClasspath(true);
    		Set<String> classpath = new LinkedHashSet<String>();
    		Set<String> sourcepath = new LinkedHashSet<String>();
    		
    		// For projects on the classpath, loops can exist; need to make sure we 
    		// don't loop forever
    		Set<IJavaProject> projectsProcessed = new HashSet<IJavaProject>();
    		projectsProcessed.add(jproj);
    		for (IClasspathEntry entry : classpathEntries) {
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
    				if (otherJavaProject != null) {
    					addProjectClasspath(root, otherJavaProject, projectsProcessed, classpath);
    				}
    			}
    		}
    		// if you add options here, also add them in isAutomaticProcessorOption(),
    		// and document them in docs/reference/automatic_processor_options.html.
    		
    		// Classpath and sourcepath
    		options.put("-classpath",convertPathCollectionToString(classpath)); //$NON-NLS-1$    		
    		options.put("-sourcepath", convertPathCollectionToString(sourcepath)); //$NON-NLS-1$
    		
    		// Get absolute path for generated source dir
    		IFolder genSrcDir = jproj.getProject().getFolder(getGenSrcDir(jproj));
    		String genSrcDirString = genSrcDir.getRawLocation().toOSString();
    		options.put("-s", genSrcDirString); //$NON-NLS-1$
    		
    		// Absolute path for bin dir as well
    		IPath binPath = jproj.getOutputLocation();
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
    
    // We need this as a separate method, as we'll put dependent projects' output
    // on the classpath
    private static void addProjectClasspath(
    		IWorkspaceRoot root,
    		IJavaProject otherJavaProject,
    		Set<IJavaProject> projectsProcessed,
    		Set<String> classpath) {
    	
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
    			binDirString = root.findMember(binPath).getLocation().toOSString();
    		}
    		else {
    			binDirString = binPath.toOSString();
    		}
    		classpath.add(binDirString);
    		
    		// Now the rest of the classpath
    		IClasspathEntry[] classpathEntries = otherJavaProject.getResolvedClasspath(true);
    		for (IClasspathEntry entry : classpathEntries) {
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
					IProject otherProject = (IProject)root.getContainerForLocation(otherProjectPath);
					IJavaProject yetAnotherJavaProject = JavaCore.create(otherProject);
					addProjectClasspath(root, yetAnotherJavaProject, projectsProcessed, classpath);
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
				new ProjectScope(jproj.getProject()) : new InstanceScope();

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
		Map<String, String> options = new HashMap<String, String>();
		
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
					new ProjectScope(jproj.getProject()), new InstanceScope() };
		}
		else {
			contexts = new IScopeContext[] { new InstanceScope() };
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
    		options = new HashMap<String, String>();
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
        	Map<String, String> options = new HashMap<String, String>();
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
    		
    		do {
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
    		} while (false);
    		
    		// We found a legitimate -A with some text after it.
    		// Where does the key end?
    		if (equalsAt > 0) {
    			if (spaceAt < 0 || equalsAt < spaceAt) {
    				// there is an equals, so there is a value.
    				key = new String(_s.substring(_start, equalsAt));
    				_start = equalsAt + 1;
    				_hasVal = (_start < _s.length());
    			}
    			else {
    				// the next thing is a space, so this is a valueless key
    				key = new String(_s.substring(_start, spaceAt));
    				_start = spaceAt + 1;
    			}
    		}
    		else {
	    		if (spaceAt < 0) {
					// no equals sign and no spaces: a valueless key, up to the end of the string. 
					key = new String(_s.substring(_start));
					_start = _s.length();
	    		}
	    		else {
    				// the next thing is a space, so this is a valueless key
    				key = new String(_s.substring(_start, spaceAt));
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
 
    		return new String(_s.substring(start, end));
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
    		new InstanceScope().getNode(AptPlugin.PLUGIN_ID).flush();
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
	 * @param jproject an IJavaProject, or null to request workspace preferences.
	 * @return true if annotation processing is turned on.
	 */
	public static boolean isEnabled(IJavaProject jproject) {
		return getBoolean(jproject, AptPreferenceConstants.APT_ENABLED);
	}
	
	/**
	 * Turn annotation processing on or off for this project.
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
		setBoolean(jproject, AptPreferenceConstants.APT_ENABLED, enabled);
	}
	
	private static boolean getBoolean(IJavaProject jproj, String optionName) {
		IPreferencesService service = Platform.getPreferencesService();
		IScopeContext[] contexts;
		if (jproj != null) {
			contexts = new IScopeContext[] { 
					new ProjectScope(jproj.getProject()), new InstanceScope(), new DefaultScope() };
		}
		else {
			contexts = new IScopeContext[] { new InstanceScope(), new DefaultScope() };
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
	 * Has an explicit factory path been set for the specified project, or
	 * is it just defaulting to the workspace settings? 
	 * @return true if there is a project-specific factory path.
	 */
	public static boolean hasProjectSpecificFactoryPath(IJavaProject jproj) {
		if (null == jproj) {
			// say no, even if workspace-level factory path does exist. 
			return false;
		}
		return FactoryPathUtil.doesFactoryPathFileExist(jproj);
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
					new ProjectScope(jproj.getProject()), new InstanceScope(), new DefaultScope() };
		}
		else {
			contexts = new IScopeContext[] { new InstanceScope(), new DefaultScope() };
		}
		return service.getString(
				AptPlugin.PLUGIN_ID, 
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
    
    public static boolean validateGenSrcDir(IJavaProject jproject, String dirName) {
    	return GeneratedSourceFolderManager.validate(jproject, dirName);
    }
	
	private static void setBoolean(IJavaProject jproject, String optionName, boolean value) {
		IScopeContext context = (null != jproject) ? 
				new ProjectScope(jproject.getProject()) : new InstanceScope();
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
				new ProjectScope(jproject.getProject()) : new InstanceScope();
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
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
