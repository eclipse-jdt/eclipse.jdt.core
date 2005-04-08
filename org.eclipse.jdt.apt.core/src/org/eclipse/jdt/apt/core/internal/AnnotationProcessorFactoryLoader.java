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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.sun.mirror.apt.AnnotationProcessorFactory;

public class AnnotationProcessorFactoryLoader {
	
	private List<AnnotationProcessorFactory> _factories = new ArrayList<AnnotationProcessorFactory>();
	
	private static boolean _verboseLoad = false;
	
    /** List of jar file entries that specify autoloadable service providers */
    private static final String[] AUTOLOAD_SERVICES = {
        "META-INF/services/com.sun.mirror.apt.AnnotationProcessorFactory"
    };

	/**
	 * Discover and instantiate annotation processor factories by searching for plugins
	 * which contribute to org.eclipse.jdt.apt.core.annotationProcessorFactory.
	 * This method is used when running within the Eclipse framework.  When running
	 * standalone at the command line, use {@link #LoadFactoriesFromJars}.
	 * This method can be called repeatedly, but each time it will erase the previous
	 * contents of the list and do a full rediscovery.
	 */
	public void loadFactoriesFromPlugins() {
		_factories.clear();
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - name of plugin that exposes this extension
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id
		IExtension[] extensions =  extension.getExtensions();
		// for all extensions of this point...
		for(int i = 0; i < extensions.length; i++){
			IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
			// for all config elements named "factory"
			for(int j = 0; j < configElements.length; j++){
				String elementName = configElements[j].getName();
				if (!("factory".equals(elementName))) { //$NON-NLS-1$ - name of configElement
					continue;
				}
				try {
					Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$ - attribute name
					if (execExt instanceof AnnotationProcessorFactory){
						_factories.add((AnnotationProcessorFactory)execExt);
					}
				} catch(CoreException e) {
						e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Discover and instantiate annotation processor factories by searching for jars
	 * on the classpath or factorypath that specify an AnnotationProcessorFactory
	 * interface in their META-INF/services directory.  This method is used when
	 * running standalone at the command line ("apt mode").  When running within the
	 * Eclipse framework, use {@link #loadFactoriesFromPlugins()}
	 * This method can be called repeatedly, but each time it will erase the previous
	 * contents of the list and do a full rediscovery.
	 */
	public void loadFactoriesFromJars() {
		_factories.clear();
		// TODO: get these values somehow
		final String factoryClassName = null;
		final File[] factoryPaths = new File[0];
		_loadFromJars(factoryClassName, factoryPaths);
	}

	/**
	 * @return Returns an immutable copy of the list of annotation processor factories.
	 */
	public List<AnnotationProcessorFactory> getFactories() {
		return Collections.unmodifiableList(_factories);
	}
	
    /**
     * Discover and load all annotation processor factories.
     * @param factoryClassName if specified, only this factory will be loaded.
     * @param factoryPaths if specified, this will be used instead of classpath.
     */
    private void _loadFromJars (final String factoryClassName, final File[] factoryPaths)
    {
        final long start = System.nanoTime();
		File[] jarPath;

		// Create an appropriate loader.  If factoryPaths is set, use it; otherwise use classpath.
        ClassLoader factoryLoader = null;
		if (factoryPaths.length > 0) {
			factoryLoader = _getExtensionClassLoader(factoryPaths);
			jarPath = factoryPaths;
		}
		else {
			factoryLoader = getClass().getClassLoader();
			jarPath = new File[0]; //TODO: how can I list all jars on compile cmdline classPath?
		}

		// If factoryClassName is specified, load only that; otherwise search all jars.
        if( factoryClassName != null ){
			_loadFactory(factoryClassName, factoryLoader);
			return;
        }
        else {
            final Set<String> classNames = new HashSet<String>();
			for (File jar : jarPath) {
                classNames.addAll(_getServiceClassnamesFromJar(jar));
			}
            for (String className : classNames) {
                final long loadStart = System.nanoTime();
                _loadFactory(className, factoryLoader);
                if (_verboseLoad) {
                    System.err.printf("\tLoading APT factory %s took %.2f seconds.", 
							className, (System.nanoTime() - loadStart) / 1000000000.0);
                    System.err.println();
                    System.err.println();
                }
            }
        }

        if (_verboseLoad) {
            System.err.println();
            System.err.printf("Loading all APT factories took %.2f seconds.", (System.nanoTime() - start) / 1000000000.0);
            System.err.println();
            System.err.println();
        }
    }
	
    /**
     * Get a class loader for loading the language implementations.
     * This is only called in the command-line compile case; in
     * the plugin case, Eclipse does the loading.
     *
     * @param jars the list of jars in the autoload directory
     * @return a classloader that can be used to load services from these jars
     */
    private ClassLoader _getExtensionClassLoader(final File[] jars)
    {
		//TODO: check that this is actually creating the right classLoader, in the apt/Eclipse world.
        final ClassLoader myLoader = getClass().getClassLoader();
 		if (_verboseLoad)
			System.err.println("I will create my own URL class loader to load these classes; my class loader type is \"" + 
					myLoader.getClass().getName() + "\".");
		final List<URL> temp = new ArrayList<URL>(jars.length);
		for (File jar : jars) {
			try {
				final URL url = jar.toURL();
				if (_verboseLoad) System.err.println("Conversion to URL succeeded: " + url);
				temp.add(url);
			}
			catch (MalformedURLException e) {
				if (_verboseLoad) System.err.println("This URL was malformed; skipping.");
			}
		}
		final URL[] urls = temp.toArray(new URL[temp.size()]);
		final ClassLoader jarLoader = new URLClassLoader(urls, myLoader);
        return jarLoader;
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
        JarFile jarFile;
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
                rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                for (String line = rd.readLine(); line != null; line = rd.readLine()) {
                    // hack off any comments
                    int iComment = line.indexOf('#');
                    if (iComment >= 0) {
                        line = line.substring(0, iComment);
                    }
                    // add the first non-whitespace token to the list
                    final String[] tokens = line.split("\\s", 2);
                    if (tokens[0].length() > 0) {
                        if (_verboseLoad) {
                            System.err.println("Found provider classname: " + tokens[0]);
                        }
                        classNames.add(tokens[0]);
                    }
                }
                rd.close();
            }
            jarFile.close();
        }
        catch (IOException e) {
            if (_verboseLoad) {
                System.err.println("\tUnable to extract provider names from \"" + jar + "\"; skipping because of: " + e);
            }
            return classNames;
        }
        return classNames;
    }

    private void _loadFactory(final String className, final ClassLoader classLoader)
    {
        try {
            if (_verboseLoad) {
                System.err.println("\tAttempting to load APT factory class \"" + className + "\"...");
            }
            Class c = classLoader.loadClass(className);
            Constructor ctor = c.getDeclaredConstructor( new Class[0] );
            AnnotationProcessorFactory factory = ( AnnotationProcessorFactory ) ctor.newInstance( new Object[0] );
            if (factory != null) {
                if(!_factories.contains( factory) )
                    _factories.add(factory);
            }
            if (_verboseLoad) {
                System.err.println("\t... succeeded.");
            }
        }
        catch (Throwable t) {
            if (_verboseLoad) {
                System.err.println("\t... failed: " + t);
                if (t.getCause() != null) t.getCause().printStackTrace(System.err);
            }
            // Uncomment this to debug exception throws that are real.
            //throw new IllegalStateException(t);
        }
    }
}
