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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.sun.mirror.apt.AnnotationProcessorFactory;

public class AnnotationProcessorFactoryLoader {
	
	private List<AnnotationProcessorFactory> _workspaceFactories = new ArrayList<AnnotationProcessorFactory>();
	
	private HashMap<IProject, List<AnnotationProcessorFactory>> _project2factories = new HashMap<IProject, List<AnnotationProcessorFactory>>();
	
	private HashMap<String, AnnotationProcessorFactory> _pluginFactoryMap = new HashMap<String, AnnotationProcessorFactory>();
	
	private static AnnotationProcessorFactoryLoader _factoryLoader;
	
	private static boolean _verboseLoad = false;

    public static synchronized AnnotationProcessorFactoryLoader getLoader() {
    	if ( _factoryLoader == null )
    		_factoryLoader = new AnnotationProcessorFactoryLoader();
    	return _factoryLoader;
    }
    
    private AnnotationProcessorFactoryLoader() {
    	loadPluginFactoryMap();
    	List<FactoryContainer> containers = getPluginFactoryContainers();    	
    	setWorkspaceAnnotationProcessorFactories( containers );    	
    }
    
    public List<AnnotationProcessorFactory> getFactoriesForProject( IProject p ) {
    	List<AnnotationProcessorFactory> factories = _project2factories.get(p);
    	if ( factories == null )
    		factories = Collections.unmodifiableList( _workspaceFactories );
    	return factories;
    }
    
	public synchronized void setWorkspaceAnnotationProcessorFactories( List<FactoryContainer> containers )
	{
		// always reset the list.  create a new list in case anyone has a handle on the old one
		_workspaceFactories = new ArrayList<AnnotationProcessorFactory>( containers.size() );
		loadFactories( _workspaceFactories, containers );
	}
	
	public synchronized void setProjectAnnotationProcessorFactories( IProject p, List<FactoryContainer> containers )
	{
		// always reset the list.  create a new list in case anyone has a handle on the old one
		List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>( containers.size() );
		_project2factories.put( p, factories );
		loadFactories( factories, containers );
	}
    
	private void loadFactories( List<AnnotationProcessorFactory> factories, List<FactoryContainer> containers )
	{
		ClassLoader classLoader = createClassLoader( containers );
		for ( FactoryContainer fc : containers )
		{
			List<AnnotationProcessorFactory> f = loadFactoryClasses( fc, classLoader );
			for ( AnnotationProcessorFactory apf : f )
				factories.add( apf  );
		}
	}
	
	private List<AnnotationProcessorFactory> loadFactoryClasses( FactoryContainer fc, ClassLoader classLoader )
	{
		List<String> factoryNames = fc.getFactoryNames();
		List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>( factoryNames.size() ); 
		for ( String factoryName : factoryNames )
		{
			AnnotationProcessorFactory factory;
			if ( fc.isPlugin() )
				factory = loadFactoryFromPlugin( factoryName );
			else
				factory = loadFactoryFromClassLoader( factoryName, classLoader );
			
			if ( factory != null )
				factories.add( factory );
		}
		return factories;
	}
	
	private AnnotationProcessorFactory loadFactoryFromPlugin( String factoryName )
	{
		AnnotationProcessorFactory apf = _pluginFactoryMap.get( factoryName );
		if ( apf == null ) 
		{
			// TODO:  log error somewhere
			System.err.println("could not find AnnotationProcessorFactory " + 
					factoryName + " from available factories defined by plugins" );
		}
		return apf;
	}

	private AnnotationProcessorFactory loadFactoryFromClassLoader( String factoryName, ClassLoader cl )
	{
		AnnotationProcessorFactory f = null;
		try
		{
			Class c = cl.loadClass( factoryName );
			f = (AnnotationProcessorFactory)c.newInstance();
		}
		catch(Exception e )
		{
			// TODO:  log this error
			e.printStackTrace();
		}
		catch( NoClassDefFoundError ncdfe )
		{
			// TODO:  log this error
			ncdfe.printStackTrace();
		}
		return f;
	}
	
	private ClassLoader createClassLoader( Collection<FactoryContainer> containers )
	{
		ArrayList<URL> urlList = new ArrayList<URL>( containers.size() );
		for ( FactoryContainer fc : containers ) 
		{
			if ( ! fc.isPlugin() )
			{
				JarFactoryContainer jfc = (JarFactoryContainer) fc;
				try
				{
					URL u = jfc.getJarFileURL();
					urlList.add( u );
				}
				catch ( MalformedURLException mue )
				{
					// TODO:  log this exception
					mue.printStackTrace();
				}
			}
		}
		
		ClassLoader cl = null;
		if ( urlList.size() > 0 )
		{
			URL[] urls = (URL[])urlList.toArray( new URL[ urlList.size() ]);
			cl = new URLClassLoader( urls, this.getClass().getClassLoader() );
		}
		return cl;
	}
	
	/**
	 * Discover and instantiate annotation processor factories by searching for plugins
	 * which contribute to org.eclipse.jdt.apt.core.annotationProcessorFactory.
	 * This method is used when running within the Eclipse framework.  When running
	 * standalone at the command line, use {@link #LoadFactoriesFromJars}.
	 * This method can be called repeatedly, but each time it will erase the previous
	 * contents of the set of known AnnotationProcessorFactoriesDefined by plugin and 
	 * do a full rediscovery.
	 */
	private void loadPluginFactoryMap() {
		_pluginFactoryMap.clear();

		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - namecls of plugin that exposes this extension
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
						_pluginFactoryMap.put( execExt.getClass().getName(), (AnnotationProcessorFactory)execExt );
					}
				} catch(CoreException e) {
						e.printStackTrace();
				}
			}
		}
	}
	
	private List<FactoryContainer> getPluginFactoryContainers()
	{
		List<FactoryContainer> factories = new ArrayList<FactoryContainer>();
	
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.jdt.apt.core",  //$NON-NLS-1$ - name of plugin that exposes this extension
				"annotationProcessorFactory"); //$NON-NLS-1$ - extension id

		IExtension[] extensions =  extension.getExtensions();
		for(int i = 0; i < extensions.length; i++) 
		{
			PluginFactoryContainer container = null;
			IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
			for(int j = 0; j < configElements.length; j++)
			{
				String elementName = configElements[j].getName();
				if ( "factory".equals( elementName ) ) //$NON-NLS-1$ - name of configElement 
				{ 
					if ( container == null )
					{
						container = new PluginFactoryContainer();
						factories.add( container );
					}
					container.addFactoryName( configElements[j].getAttribute("class") );
				}
			}
		}
		return factories;
	}
}
