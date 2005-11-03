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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Stores annotation processor factories, and handles mapping from projects
 * to them.  This is a singleton object, created by the first call to getLoader().
 */
public class AnnotationProcessorFactoryLoader {
	
	/** Loader instance -- holds all workspace and project data */
	private static AnnotationProcessorFactoryLoader LOADER;
	
	private static boolean VERBOSE_LOAD = false;
	
	// Members -- workspace and project data	
	
	private final Map<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>> _project2Factories = 
		new HashMap<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>>();
    
	// Caches the iterative classloaders so that iterative processors
	// are not reloaded on every batch build, unlike batch processors 
	// which are.
	private final Map<IJavaProject, ClassLoader> _project2IterativeClassloaders = 
		new HashMap<IJavaProject, ClassLoader>();
	
	private ClassLoader _batchClassLoader;
    
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
     * Called when underlying preferences change. 
     */
    public synchronized void resetAll() {
    	removeAptBuildProblemMarkers( null );
    	_project2Factories.clear();
    	// Need to close the iterative classloaders
    	for (ClassLoader cl : _project2IterativeClassloaders.values()) {
    		if (cl instanceof JarClassLoader) {
    			((JarClassLoader)cl).close();
    		}
    	}
    	_project2IterativeClassloaders.clear();
    }
    
    public synchronized void closeBatchClassLoader() {
    	if (_batchClassLoader == null)
    		return;
    	if (_batchClassLoader instanceof JarClassLoader) {
    		((JarClassLoader)_batchClassLoader).close();
    	}
    	_batchClassLoader = null;
    }
    
    /**
     * Called when doing a clean build -- resets
     * the classloaders for the batch processors
     */
    public synchronized void resetBatchProcessors(IJavaProject javaProj) {
    	// Only need to do a reset if we have batch processors
    	Map<AnnotationProcessorFactory, Attributes> factories = _project2Factories.get(javaProj);
    	if (factories == null) {
    		// Already empty
    		return;
    	}
    	boolean batchProcsFound = false;
    	for (Attributes attr : factories.values()) {
    		if (attr.runInBatchMode()) {
    			batchProcsFound = true;
    			break;
    		}
    	}
    	if (batchProcsFound) {
    		_project2Factories.remove(javaProj);
    	}
    }
    
    /**
     * @param jproj
     * @return order preserving map of annotation processor factories to their attributes.
     * The order the annotation processor factories respect the order of factory containers in 
     * <code>jproj</code>
     */
    public synchronized Map<AnnotationProcessorFactory, FactoryPath.Attributes> 
    	getFactoriesAndAttributesForProject(IJavaProject jproj){
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = _project2Factories.get(jproj);
    	if( factories != null )
    		return Collections.unmodifiableMap(factories);
    	
    	// Load the project
		FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
		Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers(jproj);
		factories = loadFactories(containers, jproj);
		_project2Factories.put(jproj, factories);
		return Collections.unmodifiableMap(factories);
    	
    }
    
    /**
     * @param javaProj
     * @return <code>true</code> iff there are Annotation Processor Factories associated with 
     * the given project
     */
    public synchronized boolean hasFactoriesForProject(IJavaProject javaProj){
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = _project2Factories.get(javaProj);
    	if( factories != null && !factories.isEmpty() )
    		return true;
    	
    	// Load the project
		FactoryPath fp = FactoryPathUtil.getFactoryPath(javaProj);
		Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers(javaProj);
		factories = loadFactories(containers, javaProj);
		_project2Factories.put(javaProj, factories);
		return factories != null && !factories.isEmpty();
    }
    
    public synchronized List<AnnotationProcessorFactory> getFactoriesForProject( IJavaProject jproj ) {
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs = 
    		getFactoriesAndAttributesForProject(jproj);
    	final List<AnnotationProcessorFactory> factories = 
    		new ArrayList<AnnotationProcessorFactory>(factoriesAndAttrs.keySet());
    	return Collections.unmodifiableList(factories);
    }
    
	/**
	 * @param containers an ordered map.
	 * @return order preserving map of annotation processor factories to their attributes. 
	 * The order of the factories respect the order of the containers.
	 */
	private Map<AnnotationProcessorFactory, FactoryPath.Attributes> loadFactories( Map<FactoryContainer, FactoryPath.Attributes> containers, IJavaProject project )
	{
		Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs = 
			new LinkedHashMap<AnnotationProcessorFactory, FactoryPath.Attributes>(containers.size() * 4 / 3 + 1);
		
		// Clear existing problem markers; we'll add them back if there are still problems.
		removeAptBuildProblemMarkers( project );
		removeMissingFactoryJars( project, containers );
		
		// Need to use the cached classloader if we have one
		ClassLoader iterativeClassLoader = _project2IterativeClassloaders.get(project);
		if (iterativeClassLoader == null) {
			iterativeClassLoader = _createIterativeClassLoader(containers);
			_project2IterativeClassloaders.put(project, iterativeClassLoader);
		}
		
		_createBatchClassLoader(containers, iterativeClassLoader);
		
		for ( Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet() )
		{
			try {
				final FactoryContainer fc = entry.getKey();
				final FactoryPath.Attributes attr = entry.getValue();
				List<AnnotationProcessorFactory> factories;
				if (attr.runInBatchMode()) {
					factories = loadFactoryClasses(fc, _batchClassLoader, project);
				}
				else {
					factories = loadFactoryClasses(fc, iterativeClassLoader, project);
				}
				for ( AnnotationProcessorFactory apf : factories )
					factoriesAndAttrs.put( apf, entry.getValue() );
			}
			catch (FileNotFoundException fnfe) {
				// it would be bizarre to get this, given that we already checked for file existence up above.
				AptPlugin.log(fnfe, Messages.AnnotationProcessorFactoryLoader_jarNotFound + fnfe.getLocalizedMessage());
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, Messages.AnnotationProcessorFactoryLoader_ioError + ioe.getLocalizedMessage());
			}
		}
		return factoriesAndAttrs;
	}

	private List<AnnotationProcessorFactory> loadFactoryClasses( 
			FactoryContainer fc, ClassLoader classLoader, IJavaProject jproj )
			throws IOException
	{
		List<String> factoryNames = fc.getFactoryNames();
		List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>( factoryNames.size() ); 
		for ( String factoryName : factoryNames )
		{
			AnnotationProcessorFactory factory;
			if ( fc.getType() == FactoryType.PLUGIN )
				factory = FactoryPathUtil.getFactoryFromPlugin( factoryName );
			else
				factory = loadFactoryFromClassLoader( factoryName, classLoader, jproj );
			
			if ( factory != null )
				factories.add( factory );
		}
		return factories;
	}
	
	private AnnotationProcessorFactory loadFactoryFromClassLoader( String factoryName, ClassLoader cl, IJavaProject jproj )
	{
		AnnotationProcessorFactory f = null;
		try
		{
			Class c = cl.loadClass( factoryName );
			f = (AnnotationProcessorFactory)c.newInstance();
		}
		catch( Exception e )
		{
			reportFailureToLoadFactory(factoryName, jproj);
		}
		catch ( NoClassDefFoundError ncdfe )
		{
			// **DO NOT REMOVE THIS CATCH BLOCK***
			// This error indicates a problem with the factory path specified 
			// by the project, and it needs to be caught and reported!
			reportFailureToLoadFactory(factoryName, jproj);
		}
		return f;
	}
	
	/**
	 * Remove APT build problem markers, e.g., "missing factory jar".
	 * @param jproj if null, remove markers from all projects that have
	 * factory paths associated with them.
	 */
	private void removeAptBuildProblemMarkers( IJavaProject jproj ) {
		Set<IJavaProject> jprojects = (jproj == null) ? _project2Factories.keySet() : Collections.singleton(jproj);
		try {
			for (IJavaProject jp : jprojects) {
				if (jp.exists()) {
					IProject p = jp.getProject();
					IMarker[] markers = p.findMarkers(AptPlugin.APT_BUILD_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
					if( markers != null ){
						for( IMarker marker : markers )
							marker.delete();
					}
				}
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete APT build problem marker"); //$NON-NLS-1$
		}
	}

	/**
	 * Remove from the containers list any jar factory containers that cannot
	 * be loaded, and report them as Markers.  
	 * @param jproj must not be null
	 * @param containers will be modified by removing any invalid containers.
	 */
	private void removeMissingFactoryJars(IJavaProject jproj, Map<FactoryContainer, Attributes> containers) {
		Iterator<Entry<FactoryContainer, Attributes>> i = containers.entrySet().iterator(); 
		while (i.hasNext()) {
			FactoryContainer fc = i.next().getKey();
			if (fc instanceof JarFactoryContainer) {
				File file = ((JarFactoryContainer)fc).getJarFile();
				if (!file.exists()) {
					// Remove the jar from the list
					i.remove();
					// Add a marker
					reportMissingFactoryJar( file.toString(), jproj );
				}
			}
		}
	}

	/** 
	 * Enter a marker for a jar file that is specified on the factory path
	 * but cannot be found on disk.  
	 * These markers are removed during a clean and whenever the factory path 
	 * is reset.
	 */
	private void reportMissingFactoryJar(String jarName, IJavaProject jproj) {
		IProject project = jproj.getProject();
		try {
			String message = Messages.bind(
					Messages.AnnotationProcessorFactoryLoader_factorypath_missingLibrary, 
					new String[] {jarName, project.getName()});
			IMarker marker = project.createMarker(AptPlugin.APT_BUILD_PROBLEM_MARKER);
			marker.setAttributes(
					new String[] {
						IMarker.MESSAGE, 
						IMarker.SEVERITY,
						IMarker.LOCATION
					},
					new Object[] {
						message,
						IMarker.SEVERITY_ERROR,
						Messages.AnnotationProcessorFactoryLoader_factorypath
					}
				);
		} catch (CoreException e) {
			AptPlugin.log(e, "Unable to create build problem marker"); //$NON-NLS-1$
		}
	}
	
	/** 
	 * Enter a marker for a factory class that could not be loaded.
	 * Note that if a jar is missing, we won't be able to load its factory
	 * names, and thus we won't even try loading its factory classes; but
	 * we can still fail to load a factory class if, for instance, the
	 * jar is corrupted or the factory constructor throws an exception.  
	 * These markers are removed during a clean and whenever the factory path
	 * is reset.
	 */
	private void reportFailureToLoadFactory(String factoryName, IJavaProject jproj) {
		IProject project = jproj.getProject();
		try {
			String message = Messages.bind(
					Messages.AnnotationProcessorFactoryLoader_unableToLoadFactoryClass, 
					new String[] {factoryName, project.getName()});
			IMarker marker = project.createMarker(AptPlugin.APT_BUILD_PROBLEM_MARKER);
			marker.setAttributes(
					new String[] {
						IMarker.MESSAGE, 
						IMarker.SEVERITY,
						IMarker.LOCATION
					},
					new Object[] {
						message,
						IStatus.ERROR,
						Messages.AnnotationProcessorFactoryLoader_factorypath
					}
				);
		} catch (CoreException e) {
			AptPlugin.log(e, "Unable to create build problem marker"); //$NON-NLS-1$
		}
	}
	
	/**
	 * @param containers an ordered map.
	 */
	private ClassLoader _createIterativeClassLoader( Map<FactoryContainer, FactoryPath.Attributes> containers )
	{
		ArrayList<File> fileList = new ArrayList<File>( containers.size() );
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet()) {
			FactoryPath.Attributes attr = entry.getValue();
			FactoryContainer fc = entry.getKey();
			if (!attr.runInBatchMode() && fc instanceof JarFactoryContainer) {
				JarFactoryContainer jfc = (JarFactoryContainer)fc;
				fileList.add( jfc.getJarFile() );
			}
		}
		
		ClassLoader cl;
		if ( fileList.size() > 0 ) {
			cl = new JarClassLoader( fileList, AnnotationProcessorFactoryLoader.class.getClassLoader() );
		}
		else {
			cl = AnnotationProcessorFactoryLoader.class.getClassLoader();
		}
		return cl;
	}
	
	private void _createBatchClassLoader( 
			Map<FactoryContainer, FactoryPath.Attributes> containers, 
			ClassLoader iterativeClassLoader) {
		
		assert _batchClassLoader == null : "Previous batch classloader was non-null -- it was not closed"; //$NON-NLS-1$
		
		ArrayList<File> fileList = new ArrayList<File>( containers.size() );
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet()) {
			FactoryPath.Attributes attr = entry.getValue();
			FactoryContainer fc = entry.getKey();
			if (attr.runInBatchMode() && fc instanceof JarFactoryContainer) {
				
				JarFactoryContainer jfc = (JarFactoryContainer)fc;
				File f = jfc.getJarFile();
				fileList.add( f );
				
			}
		}
		
		if ( fileList.size() > 0 ) {
			_batchClassLoader = new JarClassLoader( fileList, iterativeClassLoader );
		}
		else {
			// No batch classloader
			_batchClassLoader = null;
		}		
	}
}
