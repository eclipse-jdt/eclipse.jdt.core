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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
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
     * @return the old value, or null if the option was not previously set.
     */
    public static String addProcessorOption(IJavaProject jproj, String key, String val) {
    	if (key == null || key.length() < 1) {
    		return null;
    	}
    	String old;
    	IEclipsePreferences node;
    	synchronized (AptConfig.class) {
	    	Map<String, String> options = getRawProcessorOptions(jproj);
	    	old = options.get(key);
	    	options.put(key, val);
	    	String serializedOptions = serializeProcessorOptions(options);
			IScopeContext context = (null != jproj) ? 
					new ProjectScope(jproj.getProject()) : new InstanceScope();
			node = context.getNode(AptPlugin.PLUGIN_ID);
			node.put(AptPreferenceConstants.APT_PROCESSOROPTIONS, serializedOptions);
    	}
    	// Do the flush outside of the synchronized block to avoid deadlock:
    	// flush causes a file write, which will block if the workspace is locked.
    	flushPreference(AptPreferenceConstants.APT_PROCESSOROPTIONS, node);
    	return old;
    }
	
	/**
     * Remove an option from the list of processor options.
     * This method is not synchronized.  If two threads simultaneously try 
     * to modify the processor options, one of the requests may be ignored.
     * @param jproj a project, or null to remove the option workspace-wide.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     * @return the old value, or null if the option was not previously set.
     */
    public static String removeProcessorOption(IJavaProject jproj, String key) {
    	String old;
    	IEclipsePreferences node;
    	synchronized (AptConfig.class) {
	    	Map<String, String> options = getRawProcessorOptions(jproj);
	    	old = options.get(key);
	    	options.remove(key);
	    	String serializedOptions = serializeProcessorOptions(options);
			IScopeContext context = (null != jproj) ? 
					new ProjectScope(jproj.getProject()) : new InstanceScope();
			node = context.getNode(AptPlugin.PLUGIN_ID);
			node.put(AptPreferenceConstants.APT_PROCESSOROPTIONS, serializedOptions);
    	}
    	// Do the flush outside of the synchronized block to avoid deadlock:
    	// flush causes a file write, which will block if the workspace is locked.
    	flushPreference(AptPreferenceConstants.APT_PROCESSOROPTIONS, node);
    	return old;
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
    	
    	IPath workspaceRootPath = jproj.getProject().getWorkspace().getRoot().getLocation();
    	
    	// Add sourcepath and classpath variables
    	try {
    		IClasspathEntry[] classpathEntries = jproj.getResolvedClasspath(true);
    		StringBuilder classpathSB = new StringBuilder();
    		StringBuilder sourcepathSB = new StringBuilder();
    		boolean firstCP = true;
    		boolean firstSP = true;
    		for (IClasspathEntry entry : classpathEntries) {
    			int kind = entry.getEntryKind();
    			if (kind == IClasspathEntry.CPE_LIBRARY) {
	    			if (firstCP) {
	    				firstCP = false;
	    			}
	    			else {
	    				classpathSB.append(File.pathSeparatorChar);
	    			}
	    			classpathSB.append(entry.getPath().makeAbsolute().toOSString());
    			}
    			else if (kind == IClasspathEntry.CPE_SOURCE) {
    				if (firstSP) {
    					firstSP = false;
    				}
    				else {
    					sourcepathSB.append(File.separatorChar);
    				}
    				// Sourcepath is a bit odd -- it's workspace-relative
    				IPath sourcepath = entry.getPath();
    				sourcepathSB.append(workspaceRootPath.append(sourcepath).toOSString());
    			}
    		}
    		// if you add options here, also add them in isAutomaticProcessorOption(),
    		// and document them in docs/reference/automatic_processor_options.html.
    		
    		// Classpath and sourcepath
    		options.put("-classpath",classpathSB.toString()); //$NON-NLS-1$
    		options.put("-sourcepath", sourcepathSB.toString()); //$NON-NLS-1$
    		
    		// Get absolute path for generated source dir
    		IFolder genSrcDir = jproj.getProject().getFolder(getGenSrcDir(jproj));
    		options.put("-s", genSrcDir.getRawLocation().toOSString()); //$NON-NLS-1$
    		
    		// Absolute path for bin dir as well
    		IPath binPath = jproj.getOutputLocation();
    		IPath binDir = workspaceRootPath.append(binPath);
    		options.put("-d", binDir.toOSString()); //$NON-NLS-1$
    		
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
     * The implementation of this at present relies on persisting all the options
     * in one string that is the equivalent of the apt command line, e.g.,
     * "-Afoo=bar -Aquux=baz", and then parsing the string into a map in this
     * routine. 
     * 
     * @param jproj a project, or null to query the workspace-wide setting.
     * @return a mutable, possibly empty, map of (key, value) pairs.  
     * The value part of a pair may be null (equivalent to "-Akey").
     * The value part can contain spaces, if it is quoted: -Afoo="bar baz".
     */
	public static Map<String, String> getRawProcessorOptions(IJavaProject jproj) {
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
     * Used to parse an apt-style command line string into a map of key/value
     * pairs.
     * Parsing ignores errors and simply tries to gobble up as many well-formed
     * pairs as it can find.
     */
    public static class ProcessorOptionsParser {
    	final String _s;
    	int _start; // everything before this is already parsed.
    	boolean _hasVal; // does the last key found have a value token?
    	
    	public ProcessorOptionsParser(String s) {
    		_s = s;
    		_start = 0;
    		_hasVal = false;
    	}
    	
     	public Map<String, String> parse() {
        	Map<String, String> options = new LinkedHashMap<String, String>();
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
	 * @return
	 */
	public static boolean isEnabled(IJavaProject jproject) {
		// TODO: Walter have a fix for this problem.
		if( jproject == null )
			return true;
		return getBoolean(jproject, AptPreferenceConstants.APT_ENABLED);
	}
	
	/**
	 * Turn annotation processing on or off for this project.
	 * @param jproject an IJavaProject, or null to set workspace preferences.
	 * @param enabled
	 */
	public static void setEnabled(IJavaProject jproject, boolean enabled) {
		setBoolean(jproject, AptPreferenceConstants.APT_ENABLED, enabled);
	}
	
	private static boolean getBoolean(IJavaProject jproject, String optionName) {
		IPreferencesService service = Platform.getPreferencesService();
		// Don't need to do this, because it's the default-default already:
		//service.setDefaultLookupOrder(AptPlugin.PLUGIN_ID, null, lookupOrder);

		ProjectScope projScope = new ProjectScope(jproject.getProject());
		IScopeContext[] contexts = new IScopeContext[] { projScope };
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
	 * @param project
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
	 * The -A settings are all contained within one single preference setting, 
	 * APT_PROCESSOROPTIONS.  Use @see #getProcessorOptions(IJavaProject) to 
	 * get the -A settings; use @see #getOptions(IJavaProject) to get all the 
	 * preference settings as a map; and use this helper method to get a single 
	 * preference setting.
	 * 
	 * @param jproject the project, or null for workspace.
	 * @param optionName a preference constant from @see AptPreferenceConstants.
	 * @return
	 */
    public static String getString(IJavaProject jproject, String optionName) {
		IPreferencesService service = Platform.getPreferencesService();
		// Don't need to do this, because it's the default-default already:
		//service.setDefaultLookupOrder(AptPlugin.PLUGIN_ID, null, lookupOrder);
		ProjectScope projScope = new ProjectScope(jproject.getProject());
		IScopeContext[] contexts = new IScopeContext[] { projScope };
		return service.getString(
				AptPlugin.PLUGIN_ID, 
				optionName, 
				AptPreferenceConstants.DEFAULT_OPTIONS_MAP.get(optionName), 
				contexts);
	}
    
    public static String getGenSrcDir(IJavaProject jproject) {
    	String genSrcDir = getString(jproject, AptPreferenceConstants.APT_GENSRCDIR);
    	if (genSrcDir == null) {
    		throw new IllegalStateException("Generated Source Directory was null."); //$NON-NLS-1$
    	}
    	return genSrcDir;
    }
    
    public static void setGenSrcDir(IJavaProject jproject, String dirString) {
    	if (dirString == null) {
    		throw new IllegalArgumentException("Cannot set the Generated Source Directory to null"); //$NON-NLS-1$
    	}
    	if( AptPlugin.DEBUG ){
    		AptPlugin.trace("setting gen src dir to " + dirString + " for " + jproject.getElementName() );  //$NON-NLS-1$//$NON-NLS-2$
    	}
    	setString(jproject, AptPreferenceConstants.APT_GENSRCDIR, dirString);
    }
	
    /**
     * Save processor (-A) options as a string.  Option key/val pairs will be
     * serialized as -Akey=val, and key/null pairs as -Akey.  Options are
     * space-delimited.  The result resembles the apt command line.
     * @param options a map containing zero or more key/value or key/null pairs.
     */
    public static String serializeProcessorOptions(Map<String, String> options) {
    	StringBuilder sb = new StringBuilder();
    	boolean firstEntry = true;
    	for (Map.Entry<String, String> entry : options.entrySet()) {
    		if (firstEntry) {
    			firstEntry = false;
        		sb.append("-A"); //$NON-NLS-1$
    		}
    		else {
    			sb.append(" -A"); //$NON-NLS-1$
    		}
    		sb.append(entry.getKey());
    		if (entry.getValue() != null) {
    			sb.append("="); //$NON-NLS-1$
    			sb.append(entry.getValue());
    		}
    	}
    	return sb.toString();
    }
	
	private static void setBoolean(IJavaProject jproject, String optionName, boolean value) {
		IScopeContext context = (null != jproject) ? 
				new ProjectScope(jproject.getProject()) : new InstanceScope();
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.putBoolean(optionName, value);
		flushPreference(optionName, node);
	}
	
	private static void setString(IJavaProject jproject, String optionName, String value) {
		IScopeContext context = (null != jproject) ? 
				new ProjectScope(jproject.getProject()) : new InstanceScope();
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.put(optionName, value);
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
