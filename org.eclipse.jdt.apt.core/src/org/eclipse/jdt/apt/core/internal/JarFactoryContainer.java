/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jdt.apt.core.FactoryContainer;
import org.eclipse.jdt.apt.core.AptPlugin;

/**
 * Represents a jar file that contains annotation processor factories.
 * The factories are listed in the jar's META-INF/services folder, in
 * a file named com.sun.mirror.apt.AnnotationProcessorFactory.
 */
public abstract class JarFactoryContainer extends FactoryContainer
{
	public URL getJarFileURL() throws MalformedURLException { 
		return getJarFile().toURL(); 
	}
	
	protected abstract File getJarFile();
		
	@Override
	protected List<String> loadFactoryNames() { 
		return getServiceClassnamesFromJar( getJarFile() );
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
    protected static List<String> getServiceClassnamesFromJar(File jar)
    {
        List<String> classNames = new ArrayList<String>();
        JarFile jarFile;
        try {
            jarFile = new JarFile(jar);

            for (String providerName : AUTOLOAD_SERVICES) {
            	// Get the service provider def file out of the jar.
                JarEntry provider = jarFile.getJarEntry(providerName);
                if (provider == null) {
                    continue;
                }
                // Extract classnames from the service provider def file.
                InputStream is = jarFile.getInputStream(provider);
                readServiceProvider(is, classNames);
            }
            jarFile.close();
        }
        catch (IOException e) {	
        	AptPlugin.log(e, "Unable to read annotation processor service names from jar file " + jar); //$NON-NLS-1$
            return classNames;
        }
        return classNames;
    }
    
    /**
     * Read service classnames from a service provider definition.
     * @param is an input stream corresponding to a Sun-style service provider
     * definition file, e.g., one of the files named in AUTOLOAD_SERVICES.
     * @param classNames a list to which the classes named in is will be added.
     */
    protected static void readServiceProvider(InputStream is, List<String> classNames) {
    	try {
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
	                classNames.add(tokens[0]);
	            }
	        }
	        rd.close();
    	}
    	catch (IOException e) {
    		AptPlugin.log(e, "Error reading annotation processor jar file"); //$NON-NLS-1$
    	}
    }
	
    /** List of jar file entries that specify autoloadable service providers */
    private static final String[] AUTOLOAD_SERVICES = {
        "META-INF/services/com.sun.mirror.apt.AnnotationProcessorFactory" //$NON-NLS-1$
    };
	
}

