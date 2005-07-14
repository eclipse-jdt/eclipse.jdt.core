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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
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
 * 
 * TODO: synchronization of maps
 * TODO: NLS
 * TODO: rest of settings
 * TODO: optimize performance on projects that do not have project-specific settings.
 */
public class AptConfig {
	
	/*
	 * Hide constructor; this is a static object
	 */
	private AptConfig() {}
	
	/**
	 * Holds the options maps for each project.  Use a WeakHashMap so that
	 * we don't hold on to projects after they've been removed.
	 * 
	 * The key is IProject rather than IJavaProject because we need to
	 * listen for project nodes being removed from the Eclipse preferences 
	 * tree.  By the time a node is removed, it might not have a valid
	 * IJavaProject associated with it any more.
	 */
	private static Map<IProject, Map<String, String>> _optionsMaps = 
		new WeakHashMap<IProject, Map<String, String>>();
	
	
	/**
	 * Add factory containers to the list for a project.  If the container
	 * is already in the project's list, it will remain but will take on
	 * the new value of the 'enabled' attribute.
	 * The resulting list will be saved to the appropriate settings file.
	 * If there is an error accessing the file an exception will be thrown.
	 * @param jproj a project, or null for the workspace list.
	 * @param adds a map of factory containers to add to the list.  The value
	 * indicates whether the container's factories are to be enabled.
	 */
	public static synchronized void addContainers(
			IJavaProject jproj, Map<FactoryContainer, Boolean> adds) 
			throws IOException, CoreException {
		Map<FactoryContainer, Boolean> existing = FactoryPathUtil.getAllContainers(jproj);
		existing.putAll(adds);
		setContainers(jproj, existing);
	}
	
	/**
	 * Returns all containers for the provided project, including disabled ones.
	 * @param jproj The java project in question, or null for the workspace
	 * @return an ordered map, where the key is the container and the value 
	 * indicates whether the container is enabled.
	 */
	public static synchronized Map<FactoryContainer, Boolean> getAllContainers(IJavaProject jproj) {
		return FactoryPathUtil.getAllContainers(jproj);
	}
	
	/**
	 * Get the factory containers for this project. If no project-level configuration
	 * is set, the workspace config will be returned. Any disabled containers
	 * will not be returned.
	 * 
	 * @param jproj The java project in question. 
	 * @return an ordered list of all enabled factory containers.
	 */
	public static synchronized List<FactoryContainer> getEnabledContainers(IJavaProject jproj) {
		// this map is ordered.
		Map<FactoryContainer, Boolean> containers = FactoryPathUtil.getAllContainers(jproj);
		List<FactoryContainer> result = new ArrayList<FactoryContainer>(containers.size());
		for (Map.Entry<FactoryContainer, Boolean> entry : containers.entrySet()) {
			if (entry.getValue()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}
	    
	/**
	 * Remove a processor factory container from the list for a project.  
	 * The resulting list will be saved to the appropriate settings file.
	 * If there is an error accessing the file an exception will be thrown.
	 * @param jproj a project, or null for the workspace list.
	 * @param container a factory container.
	 */
	public static synchronized void removeContainer(
			IJavaProject jproj, FactoryContainer container) 
			throws IOException, CoreException {
		Map<FactoryContainer, Boolean> existing = FactoryPathUtil.getAllContainers(jproj);
		existing.remove(container);
		setContainers(jproj, existing);
	}
	
	/**
     * Add the equivalent of -Akey=val to the list of processor options.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     * @param jproj a project, or null to set the option workspace-wide.
     * @param val can be null (equivalent to -Akey).  This does not mean
     * remove the key; for that functionality, @see #removeProcessorOption(IJavaProject, String).
     * @return the old value, or null if the option was not previously set.
     */
    public static synchronized String addProcessorOption(IJavaProject jproj, String key, String val) {
    	if (key == null || key.length() < 1) {
    		return null;
    	}
    	Map<String, String> options = getProcessorOptions(jproj);
    	String old = options.get(key);
    	options.put(key, val);
    	String serializedOptions = serializeProcessorOptions(options);
    	setString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS, serializedOptions);
    	return old;
    }
	
	/**
     * Remove an option from the list of processor options.
     * @param jproj a project, or null to remove the option workspace-wide.
     * @param key must be a nonempty string.  It should only include the key;
     * that is, it should not start with "-A".
     * @return the old value, or null if the option was not previously set.
     */
    public static synchronized String removeProcessorOption(IJavaProject jproj, String key) {
    	Map<String, String> options = getProcessorOptions(jproj);
    	String old = options.get(key);
    	options.remove(key);
    	String serializedOptions = serializeProcessorOptions(options);
    	setString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS, serializedOptions);
    	return old;
    }
    
	/**
     * Get the options that are the equivalent of the -A command line options
     * for apt.  The -A and = are stripped out, so (key, value) is the
     * equivalent of -Akey=value.  
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
    public static Map<String, String> getProcessorOptions(IJavaProject jproj) {
    	Map<String,String> options;
    	String allOptions = getString(jproj, AptPreferenceConstants.APT_PROCESSOROPTIONS);
    	if (null == allOptions) {
    		options = new HashMap<String, String>();
    	}
    	else {
    		OptionsParser op = new OptionsParser(allOptions);
    		options = op.parse();
    	}
    	
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
	    			classpathSB.append(entry.getPath().toFile().getAbsolutePath());
    			}
    			else if (kind == IClasspathEntry.CPE_SOURCE) {
    				if (firstSP) {
    					firstSP = false;
    				}
    				else {
    					sourcepathSB.append(File.separatorChar);
    				}
    				sourcepathSB.append(entry.getPath().toFile().getAbsolutePath());
    			}
    		}
    		options.put("classpath",classpathSB.toString()); //$NON-NLS-1$
    		options.put("sourcepath", sourcepathSB.toString()); //$NON-NLS-1$
    	}
    	catch (JavaModelException jme) {
    		AptPlugin.log(jme, "Could not get the classpath for project: " + jproj); //$NON-NLS-1$
    	}
    	
    	return options;
    }
    
    /**
     * Used to parse an apt-style command line string into a map of key/value
     * pairs.
     * Parsing ignores errors and simply tries to gobble up as many well-formed
     * pairs as it can find.
     */
    private static class OptionsParser {
    	final String _s;
    	int _start; // everything before this is already parsed.
    	boolean _hasVal; // does the last key found have a value token?
    	
    	OptionsParser(String s) {
    		_s = s;
    		_start = 0;
    		_hasVal = false;
    	}
    	
     	public Map<String, String> parse() {
        	Map<String, String> options = new LinkedHashMap<String, String>();
        	String key;
        	while (null != (key = parseKey())) {
        		String val;
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
		// some change listeners here.  (Cf. per-project code in getOptions()).
	}
	
	/**
	 * Is annotation processing turned on for this project?
	 * @param jproject an IJavaProject, or null to request workspace preferences.
	 * @return
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
		setBoolean(jproject, AptPreferenceConstants.APT_ENABLED, enabled);
	}
	
	private static synchronized boolean getBoolean(IJavaProject jproject, String optionName) {
		Map options = getOptions(jproject);
		return "true".equals(options.get(optionName)); //$NON-NLS-1$
	}
	
    /**
	 * Return the apt settings for this project, or the workspace settings
	 * if they are not overridden by project settings.
	 * TODO: efficiently handle the case of projects that don't have per-project settings
	 * (e.g., only cache one workspace-wide map, not a separate copy for each project).
	 * @param jproject
	 * @return
	 */
	private static Map getOptions(IJavaProject jproject) {
		IProject project = jproject.getProject();
		assert(null != project);
		Map options = _optionsMaps.get(project);
		if (null != options) {
			return options;
		}
		// We didn't already have an options map for this project, so create one.
		IPreferencesService service = Platform.getPreferencesService();
		// Don't need to do this, because it's the default-default already:
		//service.setDefaultLookupOrder(AptPlugin.PLUGIN_ID, null, lookupOrder);
		options = new HashMap(AptPreferenceConstants.NSETTINGS);
		if (jproject != null) {
			_optionsMaps.put(project, options);
			// Load project values into the map
			ProjectScope projScope = new ProjectScope(project);
			IScopeContext[] contexts = new IScopeContext[] { projScope };
			for (String optionName : AptPreferenceConstants.OPTION_NAMES) {
				String val = service.getString(AptPlugin.PLUGIN_ID, optionName, null, contexts);
				if (val != null) {
					options.put(optionName, val);
				}
			}
			// Add change listener for this project, so we can update the map later on
			IEclipsePreferences projPrefs = projScope.getNode(AptPlugin.PLUGIN_ID);
			ChangeListener listener = new ChangeListener(project);
			projPrefs.addPreferenceChangeListener(listener);
			((IEclipsePreferences)projPrefs.parent()).addNodeChangeListener(listener);
		}
		
		return options;
	}
	
	private static class ChangeListener implements IPreferenceChangeListener, INodeChangeListener {
		private final IProject _proj;
		public ChangeListener(IProject proj) {
			_proj = proj;
		}
		public void preferenceChange(PreferenceChangeEvent event) {
			// update the changed value in the options map.
			Map<String, String> options = _optionsMaps.get(_proj);
			options.put((String)event.getKey(), (String)event.getNewValue());
			
			// handle change to generated source directory
			if ( AptPreferenceConstants.APT_GENSRCDIR.equals( event.getKey() ) ) {

				if ( event.getNewValue() != null && ! event.getNewValue().equals( event.getOldValue())) {
					GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( _proj );
					gfm.setGeneratedSourceFolderName( (String)event.getNewValue() );
				}
			}
			
		}
		public void added(NodeChangeEvent event) {
			// do nothing
		}
		public void removed(NodeChangeEvent event) {
			// clear out the cached options for this project.
			_optionsMaps.remove(_proj);
		}
	}
	
    public static synchronized String getString(IJavaProject jproject, String optionName) {
		Map options = getOptions(jproject);
		return (String)options.get(optionName);
	}
	
    /**
     * Save processor (-A) options as a string.  Option key/val pairs will be
     * serialized as -Akey=val, and key/null pairs as -Akey.  Options are
     * space-delimited.  The result resembles the apt command line.
     * @param options a map containing zero or more key/value or key/null pairs.
     */
    private static String serializeProcessorOptions(Map<String, String> options) {
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
	
	private static synchronized void setBoolean(IJavaProject jproject, String optionName, boolean value) {
		// TODO: should we try to determine whether a project has no per-project settings,
		// and if so, set the workspace settings?  Or, do we want the caller to tell us
		// explicitly by setting jproject == null in that case?
		
		IScopeContext context;
		if (null != jproject) {
			context = new ProjectScope(jproject.getProject());
		}
		else {
			context = new InstanceScope();
		}
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.putBoolean(optionName, value);
	}
	
	private static synchronized void setString(IJavaProject jproject, String optionName, String value) {
		IScopeContext context;
		if (null != jproject) {
			context = new ProjectScope(jproject.getProject());
		}
		else {
			context = new InstanceScope();
		}
		IEclipsePreferences node = context.getNode(AptPlugin.PLUGIN_ID);
		node.put(optionName, value);
	}

    /**
	 * Set or reset the factory containers for a given project or the workspace.
	 * @param jproj the java project, or null for the workspace
	 * @param containers an ordered map whose key is a factory container and
	 * whose value indicates whether the container's factories are enabled;
	 * or null, to restore defaults.
	 */
	public static synchronized void setContainers(IJavaProject jproj, Map<FactoryContainer, Boolean> containers) 
	throws IOException, CoreException 
	{
		FactoryPathUtil.saveFactoryPathFile(jproj, containers);
		// The factory path isn't saved to the Eclipse preference store,
		// so we can't rely on the ChangeListener mechanism.
		AnnotationProcessorFactoryLoader.getLoader().reset();
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
	 * Get a factory path corresponding to the default values: if jproj is
	 * non-null, return the current workspace factory path; if jproj is null,
	 * return the default list of plugin factories.
	 */
	public static Map<FactoryContainer, Boolean> getDefaultFactoryPath(IJavaProject jproj) {
		return FactoryPathUtil.getDefaultFactoryPath(jproj);
	}
	
}
