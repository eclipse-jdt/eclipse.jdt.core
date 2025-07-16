/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;

/**
 * Represents a jar file that contains annotation processor factories.
 * The factories are listed in the jar's META-INF/services folder, in
 * a file named com.sun.mirror.apt.AnnotationProcessorFactory.
 */
public abstract class JarFactoryContainer extends FactoryContainer
{

	/**
	 * @return a java.io.File.  The file is not guaranteed to exist.
	 */
	public abstract File getJarFile();

	@Override
	public boolean exists() {
		try {
			final File jarFile = getJarFile();
			if(jarFile == null)
				return false;
			return getJarFile().exists();
		} catch (SecurityException e) {
			return false;
		}
	}

	@Override
	protected Map<String, String> loadFactoryNames() throws IOException {
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
    protected static Map<String, String> getServiceClassnamesFromJar(File jar) throws IOException
    {
        Map<String, String> classNames = new LinkedHashMap<>();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jar);

            for (String serviceName : AUTOLOAD_SERVICES) {
            	String providerName = "META-INF/services/" + serviceName; //$NON-NLS-1$
            	// Get the service provider def file out of the jar.
                JarEntry provider = jarFile.getJarEntry(providerName);
                if (provider == null) {
                    continue;
                }
                // Extract classnames from the service provider def file.
                InputStream is = jarFile.getInputStream(provider);
                readServiceProvider(is, serviceName, classNames);
            }
        } catch (IOException ioe) {
        	AptPlugin.log(ioe, Messages.AnnotationProcessorFactoryLoader_ioError + jar.getAbsolutePath());
        } finally {
        	try {if (jarFile != null) jarFile.close();} catch (IOException ioe) {}
        }
        return classNames;
    }

    /**
     * Read service classnames from a service provider definition.
     * @param is an input stream corresponding to a Sun-style service provider
     * definition file, e.g., one of the files named in AUTOLOAD_SERVICES.
     * @param classNames a list to which the classes named in is will be added.
     */
    protected static void readServiceProvider(InputStream is, String serviceName, Map<String, String> classNames) throws IOException {
    	BufferedReader rd = null;
    	try {
	        rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	        for (String line = rd.readLine(); line != null; line = rd.readLine()) {
	            // hack off any comments
	            int iComment = line.indexOf('#');
	            if (iComment >= 0) {
	                line = line.substring(0, iComment);
	            }
	            // add the first non-whitespace token to the list
	            final String[] tokens = line.split("\\s", 2); //$NON-NLS-1$
	            if (tokens[0].length() > 0) {
	                classNames.put(tokens[0], serviceName);
	            }
	        }
	        rd.close();
    	}
    	finally {
    		if (rd != null) try {rd.close();} catch (IOException ioe) {}
    	}
    }

    /** List of jar file entries within META-INF/services that specify autoloadable service providers */
    private static final String[] AUTOLOAD_SERVICES = {
        AptPlugin.JAVA5_FACTORY_NAME,
        AptPlugin.JAVA6_FACTORY_NAME
    };

}

