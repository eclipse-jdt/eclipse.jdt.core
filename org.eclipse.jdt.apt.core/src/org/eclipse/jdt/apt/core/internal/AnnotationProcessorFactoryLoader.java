/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.FactoryContainer;
import org.eclipse.jdt.apt.core.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.util.FactoryPath;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Stores annotation processor factories, and handles mapping from projects
 * to them.
 */
public class AnnotationProcessorFactoryLoader {
	
	/** List of jar file entries that specify autoloadable service providers */
    private static final String[] AUTOLOAD_SERVICES = {
        "META-INF/services/com.sun.mirror.apt.AnnotationProcessorFactory" //$NON-NLS-1$
    };
	
	/** Loader instance -- holds all workspace and project data */
	private static AnnotationProcessorFactoryLoader LOADER;
	
	private static boolean VERBOSE_LOAD = false;
	
	// Members -- workspace and project data	
	
	private final Map<IJavaProject, List<AnnotationProcessorFactory>> _project2Factories = 
		new HashMap<IJavaProject, List<AnnotationProcessorFactory>>();

	private final Set<IJavaProject> _projectsLoaded = new HashSet<IJavaProject>();
    
    
	/** 
	 * Singleton
	 */
    public static synchronized AnnotationProcessorFactoryLoader getLoader() {
    	if ( LOADER == null )
    		LOADER = new AnnotationProcessorFactoryLoader();
    	return LOADER;
    }
    
    private AnnotationProcessorFactoryLoader() {
    	FactoryPathUtil.loadPluginFactories();
    }
    
    /**
     * Called when underlying preferences change
     */
    public synchronized void reset() {
    	_project2Factories.clear();
    }
    
    public synchronized List<AnnotationProcessorFactory> getFactoriesForProject( IJavaProject jproj ) {
    	
    	List<AnnotationProcessorFactory> factories = null;
    	
		if (_projectsLoaded.contains(jproj)) {
    		factories = _project2Factories.get(jproj);
    		if (factories != null) {
    			return factories;
    		}
		}
		// Load the project
		List<FactoryContainer> containers = FactoryPath.getEnabledContainers(jproj);
		factories = loadFactories(containers, jproj);
		_projectsLoaded.add(jproj);
		_project2Factories.put(jproj, factories);
		return factories;
    	
    }
    

    
	private List<AnnotationProcessorFactory> loadFactories( List<FactoryContainer> containers, IJavaProject project )
	{
		List<AnnotationProcessorFactory> factories = new ArrayList(containers.size());
		ClassLoader classLoader = _createClassLoader( containers );

		for ( FactoryContainer fc : containers )
		{
			List<AnnotationProcessorFactory> f = loadFactoryClasses( fc, classLoader );
			for ( AnnotationProcessorFactory apf : f )
				factories.add( apf  );
		}
		return factories;
	}
	
	private List<AnnotationProcessorFactory> loadFactoryClasses( FactoryContainer fc, ClassLoader classLoader )
	{
		List<String> factoryNames = fc.getFactoryNames();
		List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>( factoryNames.size() ); 
		for ( String factoryName : factoryNames )
		{
			AnnotationProcessorFactory factory;
			if ( fc.getType() == FactoryType.PLUGIN )
				factory = FactoryPathUtil.getFactoryFromPlugin( factoryName );
			else
				factory = loadFactoryFromClassLoader( factoryName, classLoader );
			
			if ( factory != null )
				factories.add( factory );
		}
		return factories;
	}
	
	private AnnotationProcessorFactory loadFactoryFromClassLoader( String factoryName, ClassLoader cl )
	{
		AnnotationProcessorFactory f = null;
		try
		{
			Class c = cl.loadClass( factoryName );
			f = (AnnotationProcessorFactory)c.newInstance();
		}
		catch( Exception e )
		{
			AptPlugin.log(e, "Could not load annotation processor factory " + factoryName); //$NON-NLS-1$
		}
		catch ( NoClassDefFoundError ncdfe )
		{
			// **DO NOT REMOVE THIS CATCH BLOCK***
			// This error indicates a problem with the factory path specified 
			// by the project, and it needs to be caught and reported!
			AptPlugin.log(ncdfe, "Could not load annotation processor factory " + factoryName); //$NON-NLS-1$
		}
		return f;
	}
	
	private ClassLoader _createClassLoader( Collection<? extends FactoryContainer> containers )
	{
		ArrayList<URL> urlList = new ArrayList<URL>( containers.size() );
		for ( FactoryContainer fc : containers ) 
		{
			if ( fc instanceof JarFactoryContainer  )
			{
				JarFactoryContainer jfc = (JarFactoryContainer) fc;
				try
				{
					URL u = jfc.getJarFileURL();
					urlList.add( u );
				}
				catch ( MalformedURLException mue )
				{
					AptPlugin.log(mue, "Could not create ClassLoader for " + jfc); //$NON-NLS-1$
				}
			}
		}
		
		ClassLoader cl = null;
		if ( urlList.size() > 0 )
		{
			URL[] urls = urlList.toArray(new URL[urlList.size()]);
			cl = new URLClassLoader( urls, AnnotationProcessorFactoryLoader.class.getClassLoader() );
		}
		return cl;
	}
	
    /**
     * Given a jar file, get the names of any AnnotationProcessorFactory
     * implementations it offers.  The information is based on the Sun
     * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">
     * Jar Service Provider spec</a>: the jar file contains a META-INF/services
     * directory; that directory contains text files named according to the desired
     * interfaces; and each file contains the names of the classes implementing
     * the specified service.  The files may also contain whitespace (which is to
     * be ignored).  The '#' character indicates the beginning of a line comment,
     * also to be ignored.  Implied but not stated in the spec is that this routine
     * also ignores anything after the first nonwhitespace token on a line.
     * @param jar the jar file.
     * @return a list, possibly empty, of fully qualified classnames to be instantiated.
     */
    private List<String> _getServiceClassnamesFromJar(File jar)
    {
        List<String> classNames = new ArrayList<String>();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jar);

            for (String providerName : AUTOLOAD_SERVICES) {
                JarEntry provider = jarFile.getJarEntry(providerName);
                if (provider == null) {
                    continue;
                }
                // Extract classnames from this text file.
                InputStream is = jarFile.getInputStream(provider);
                BufferedReader rd;
                rd = new BufferedReader(new InputStreamReader(is, "UTF-8")); //$NON-NLS-1$
                for (String line = rd.readLine(); line != null; line = rd.readLine()) {
                    // hack off any comments
                    int iComment = line.indexOf('#');
                    if (iComment >= 0) {
                        line = line.substring(0, iComment);
                    }
                    // add the first non-whitespace token to the list
                    final String[] tokens = line.split("\\s", 2); //$NON-NLS-1$
                    if (tokens[0].length() > 0) {
                        if (VERBOSE_LOAD) {
                            System.err.println("Found provider classname: " + tokens[0]); //$NON-NLS-1$
                        }
                        classNames.add(tokens[0]);
                    }
                }
                rd.close();
            }
        }
        catch (IOException e) {
            if (VERBOSE_LOAD) {
                System.err.println("\tUnable to extract provider names from \"" + jar + "\"; skipping because of: " + e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return classNames;
        }
        finally {
        	if (jarFile != null) {try {jarFile.close();} catch (IOException ioe) {}}
        }
        return classNames;
    }
}
