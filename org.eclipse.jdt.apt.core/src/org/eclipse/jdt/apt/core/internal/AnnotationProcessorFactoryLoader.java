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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Stores annotation processor factories, and handles mapping from projects
 * to them.  This is a singleton object, created by the first call to getLoader().
 * <p>
 * Factories contained in plugins are loaded at APT initialization time.  
 * Factories contained in jar files are loaded for a given project the first time 
 * getFactoriesForProject() is called, and cached thereafter.  Factories are loaded 
 * from one of two custom classloaders depending on whether the factory container 
 * is to be run in batch processing mode or normal (iterative) mode; the batch
 * classloader for a project is parented by the iterative classloader for that
 * project.
 * <p>
 * <strong>Caches</strong>
 * <p>
 * Factory classes and iterative-mode classloaders are cached for each project,
 * the first time that the classes are needed (e.g., during a build or reconcile).  
 * The cache is cleared when the project's factory path changes, when a resource
 * listed on the factory path is changed, or when the project is deleted.  
 * If a project contains batch-mode processors, the cache is also cleared at
 * the beginning of every full build (batch-mode processors do not run at all 
 * during reconcile).
 * <p>
 * If a project's factory path includes containers which cannot be located on
 * disk, problem markers will be added to the project.  This validation process
 * occurs when the cache for a project is first loaded, and whenever the cache
 * is invalidated.  We do not validate the workspace-level factory path as such; 
 * it is only used to construct a project-specific factory path for projects 
 * that do not have their own factory path.
 * <p>
 * In order to efficiently perform re-validation when resources change, we keep 
 * track of which projects' factory paths mention which containers.  This is
 * stored as a map from canonicalized resource path to project.  Entries are
 * created and updated during factory path validation, and removed upon project 
 * deletion.
 * <p>
 * Resource changes are presented as delta trees which may contain more than
 * one change.  When a change arrives, we build up a list of all potentially
 * affected projects, and then perform re-validation after the list is complete.
 * That way we avoid redundant validations if a project is affected by more 
 * than one change.   
 * <p>
 * Note that markers and factory classes have different lifetimes: they are
 * discarded at the same time (when something changes), but markers are recreated
 * immediately (as a result of validation) while factory classes are not reloaded
 * until the next time a build or reconcile occurs.
 * <p>
 * <strong>Synchronization</strong>
 * <p>
 * The loader is often accessed on multiple threads, e.g., a build thread, a
 * reconcile thread, and a change notification thread all at once.  It is
 * important to maintain consistency across the various cache objects. 
 */
public class AnnotationProcessorFactoryLoader {
	
	/** Loader instance -- holds all workspace and project data */
	private static AnnotationProcessorFactoryLoader LOADER;
	
	private static boolean VERBOSE_LOAD = false;
	
	private static final String JAR_EXTENSION = "jar"; //$NON-NLS-1$
	
	// Caches the factory classes associated with each project.
	// See class comments for lifecycle of items in this cache.
	private final Map<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>> _project2Factories = 
		new HashMap<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>>();
    
	// Caches the iterative classloaders so that iterative processors
	// are not reloaded on every batch build, unlike batch processors 
	// which are.
	// See class comments for lifecycle of items in this cache.
	private final Map<IJavaProject, ClassLoader> _project2IterativeClassloaders = 
		new HashMap<IJavaProject, ClassLoader>();
	
	// Caches information about which resources affect which projects'
	// factory paths.
	// See class comments for lifecycle of items in this cache.
	private final Map<String, Set<IJavaProject>> _container2Project =
		new HashMap<String, Set<IJavaProject>>();
	
	private ClassLoader _batchClassLoader;
    
	/**
	 * Listen for changes that would affect the factory caches or
	 * build markers.
	 */
	private class ResourceListener implements IResourceChangeListener {

		public void resourceChanged(IResourceChangeEvent event) {
			synchronized (AnnotationProcessorFactoryLoader.this) {
				switch (event.getType()) {
				
				// Project deletion
				case (IResourceChangeEvent.PRE_DELETE) :
					IResource project = event.getResource();
					if (project != null && project instanceof IProject) {
						IJavaProject jproj = JavaCore.create((IProject)project);
						if (jproj != null) {
							uncacheProject(jproj);
						}
					}
					break;
					
				// Changes to jar files or .factorypath files
				case (IResourceChangeEvent.PRE_BUILD) :
					IResourceDelta rootDelta = event.getDelta();
					FactoryPathDeltaVisitor visitor = new FactoryPathDeltaVisitor();
					try {
						rootDelta.accept(visitor);
					} catch (CoreException e) {
						AptPlugin.log(e, "Unable to determine whether resource change affects annotation processor factory path"); //$NON-NLS-1$
					}
					Set<IJavaProject> affected = visitor.getAffectedProjects();
					if (affected != null) {
						processChanges(affected);
					}
					break;
	
				}
			}
		}
		
	}
	
	/**
	 * Walk the delta tree to see if there have been changes to
	 * a factory path or the containers it references.  If so,
	 * re-validate the affected projects' factory paths.
	 */
	private class FactoryPathDeltaVisitor implements IResourceDeltaVisitor {
		
		// List of projects affected by this change.
		// Lazy construction because we assume most changes won't affect any projects.
		private Set<IJavaProject> _affected = null;
		
		private void addAffected(Set<IJavaProject> projects) {
			if (_affected == null) {
				 _affected = new HashSet<IJavaProject>(5);
			}
			_affected.addAll(projects);
		}
		
		/**
		 * Get the list of IJavaProject affected by the delta we visited.
		 * Not valid until done visiting.
		 * @return null if there were no affected projects, or a non-empty
		 * set of IJavaProject otherwise. 
		 */
		public Set<IJavaProject> getAffectedProjects() {
			return _affected;
		}
		
		/**
		 * @return true to visit children
		 */
		public boolean visit(IResourceDelta delta) {
			switch (delta.getKind()) {
			default:
				return true;
			case IResourceDelta.ADDED :
			case IResourceDelta.REMOVED :
			case IResourceDelta.CHANGED :
				break;
			}
			// If the resource is a factory path file, then the project it
			// belongs to is affected.
			IResource res = delta.getResource();
			if (res == null) {
				return true;
			}
			IProject proj = res.getProject();
			if (FactoryPathUtil.isFactoryPathFile(res)) {
				addAffected(Collections.singleton(JavaCore.create(proj)));
				return true;
			}
			// If the resource is a jar file named in at least one factory
			// path, then the projects owning those factorypaths are affected.
			if (res.getType() != IResource.FILE) {
				return true;
			}
			IPath relativePath = res.getFullPath();
			String ext = relativePath.getFileExtension();
			try {
				if (JAR_EXTENSION.equals(ext)) {
					IPath absolutePath = res.getLocation();
					if (absolutePath == null) {
						// Jar file within a deleted project.  In this case getLocation() 
						// returns null, so we can't get a canonical path.  Bounce every
						// factory path that contains anything resembling this jar.
						for (Entry<String, Set<IJavaProject>> entry : _container2Project.entrySet()) {
							IPath jarPath = new Path(entry.getKey());
							if (relativePath.lastSegment().equals(jarPath.lastSegment())) {
								addAffected(entry.getValue());
							}
						}
					}
					else {
						// Lookup key is the canonical path of the resource
						String key = null;
						key = absolutePath.toFile().getCanonicalPath();
						Set<IJavaProject> projects = _container2Project.get(key);
						if (projects != null) {
							addAffected(projects);
						}
					}
				}
			} catch (Exception e) {
				AptPlugin.log(e, 
					"Couldn't determine whether any factory paths were affected by change to resource " + res.getName()); //$NON-NLS-1$
			}
			return true;
		}
		
	}
	
	/** 
	 * Singleton
	 */
    public static synchronized AnnotationProcessorFactoryLoader getLoader() {
    	if ( LOADER == null ) {
    		LOADER = new AnnotationProcessorFactoryLoader();
    		LOADER.registerListener();
    	}
    	return LOADER;
    }
    
	private void registerListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			new ResourceListener(),
			IResourceChangeEvent.PRE_DELETE
			| IResourceChangeEvent.PRE_BUILD);
	}

	private AnnotationProcessorFactoryLoader() {
    	FactoryPathUtil.loadPluginFactories();
    }
    
    /**
     * Called when workspace preferences change.  Resource changes, including
     * changes to project-specific factory paths, are picked up through the
     * ResourceChangedListener mechanism instead.
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
    	_container2Project.clear();
    	
    	// Validate all projects
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject proj : root.getProjects()) {
			verifyFactoryPath(JavaCore.create(proj));
		}
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
     * @param jproj must not be null
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
		Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers();
		factories = loadFactories(containers, jproj);
		_project2Factories.put(jproj, factories);
		return Collections.unmodifiableMap(factories);
    	
    }
    
    /**
     * Convenience method: get the key set of the map returned by
     * @see #getFactoriesAndAttributesForProject(IJavaProject), as a List.
     */
    public synchronized List<AnnotationProcessorFactory> getFactoriesForProject( IJavaProject jproj ) {
    	
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs = 
    		getFactoriesAndAttributesForProject(jproj);
    	final List<AnnotationProcessorFactory> factories = 
    		new ArrayList<AnnotationProcessorFactory>(factoriesAndAttrs.keySet());
    	return Collections.unmodifiableList(factories);
    }
    
    /**
     * Add the resource/project pair 'key' -> 'jproj' to the 
     * _container2Project map.
     * @param key the canonicalized pathname of the resource
     * @param jproj must not be null
     */
	private void addToResourcesMap(String key, IJavaProject jproj) {
		Set<IJavaProject> s = _container2Project.get(key);
		if (s == null) {
			s = new HashSet<IJavaProject>();
			_container2Project.put(key, s);
		}
		s.add(jproj);
	}

	/**
	 * @param containers an ordered map.
	 * @return order preserving map of annotation processor factories to their attributes. 
	 * The order of the factories respect the order of the containers.
	 */
	private Map<AnnotationProcessorFactory, FactoryPath.Attributes> loadFactories( 
			Map<FactoryContainer, FactoryPath.Attributes> containers, IJavaProject project )
	{
		Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs = 
			new LinkedHashMap<AnnotationProcessorFactory, FactoryPath.Attributes>(containers.size() * 4 / 3 + 1);
		
		removeAptBuildProblemMarkers(project);
		Set<FactoryContainer> badContainers = verifyFactoryPath(project);
		if (badContainers != null) {
			reportMissingFactoryContainers(badContainers, project);
			for (FactoryContainer badFC : badContainers) {
				containers.remove(badFC);
			}
		}
		
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
			reportFailureToLoadFactory(factoryName, jproj);
		}
		return f;
	}
	
	/**
	 * Re-validate projects whose factory paths may have been affected
	 * by a resource change (e.g., adding a previously absent jar file).
	 * This will cause build problem markers to be removed and regenerated,
	 * and factory class caches to be cleared.
	 */
	private void processChanges(Set<IJavaProject> affected) {
		for (IJavaProject jproj : affected) {
			removeAptBuildProblemMarkers(jproj);
			uncacheProject(jproj);
		}
		// We will do another clear and re-verify when loadFactories() 
		// is called.  But we have to do it then, because things might
		// have changed in the interim; and if we don't do it here, then
		// we'll have an empty _resources2Project cache, so we'll ignore
		// all resource changes until the next build.  Is that a problem?   
		for (IJavaProject jproj : affected) {
			if (jproj.exists()) {
				Set<FactoryContainer> badContainers = verifyFactoryPath(jproj);
				if (badContainers != null) {
					reportMissingFactoryContainers(badContainers, jproj);
				}
			}
		}
	
		// TODO: flag the affected projects for rebuild.
	}

	/**
	 * When a project is deleted, remove its factory path information from the loader.
	 * @param jproj
	 */
    private void uncacheProject(IJavaProject jproj) {
		_project2Factories.remove(jproj);
		_project2IterativeClassloaders.remove(jproj);
		removeProjectFromResourceMap(jproj);
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
					IMarker[] markers = p.findMarkers(AptPlugin.APT_LOADER_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
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
	 * Remove references to the project from _container2Project.  This is done
	 * when a project is deleted, or before re-verifying the project's
	 * factory path.
	 */
	private void removeProjectFromResourceMap(IJavaProject jproj) {
		Iterator<Entry<String, Set<IJavaProject>>> i = _container2Project.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, Set<IJavaProject>> e = i.next();
			Set<IJavaProject> s = e.getValue();
			s.remove(jproj);
			// Remove any resulting orphaned resources.
			if (s.isEmpty()) {
				i.remove();
			}
		}
	}

	/** 
	 * Enter problem markers for factory containers that could not be found on 
	 * disk.  This routine does not check whether markers already exist.
	 * See class comments for information about the lifecycle of these markers.
	 * @param jarName the name of the jar file.  This string is used only in
	 * the text of the message, so it doesn't matter whether it's a relative
	 * path, absolute path, or complete garbage.
	 * @param jproj must not be null.  
	 */
	private void reportMissingFactoryContainers(Set<FactoryContainer> badContainers, IJavaProject jproj) {
		IProject project = jproj.getProject();
		for (FactoryContainer fc : badContainers) {
			try {
				String message = Messages.bind(
						Messages.AnnotationProcessorFactoryLoader_factorypath_missingLibrary, 
						new String[] {fc.getId(), project.getName()});
				IMarker marker = project.createMarker(AptPlugin.APT_LOADER_PROBLEM_MARKER);
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
				AptPlugin.log(e, "Unable to create APT build problem marker on project " + project.getName()); //$NON-NLS-1$
			}
		}
	}
	
	/** 
	 * Enter a marker for a factory class that could not be loaded.
	 * Note that if a jar is missing, we won't be able to load its factory
	 * names, and thus we won't even try loading its factory classes; but
	 * we can still fail to load a factory class if, for instance, the
	 * jar is corrupted or the factory constructor throws an exception.  
	 * See class comments for information about the lifecycle of these markers.
	 * @param factoryName the fully qualified class name of the factory
	 * @param jproj must not be null
	 */
	private void reportFailureToLoadFactory(String factoryName, IJavaProject jproj) {
		IProject project = jproj.getProject();
		try {
			String message = Messages.bind(
					Messages.AnnotationProcessorFactoryLoader_unableToLoadFactoryClass, 
					new String[] {factoryName, project.getName()});
			IMarker marker = project.createMarker(AptPlugin.APT_LOADER_PROBLEM_MARKER);
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
     * Check the factory path for a project and ensure that all the
     * containers it lists are available.  Adds jar factory container
     * resources to the _container2Project cache, whether or not the
     * resource can actually be found.
     * 
     * @param jproj the project, or null to check all projects that
     * are in the cache.
     * @return a Set of all invalid containers, or null if all containers
     * on the path were valid.
     */
    private Set<FactoryContainer> verifyFactoryPath(IJavaProject jproj) {
    	Set<FactoryContainer> badContainers = null;
		FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
		Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers();
		for (FactoryContainer fc : containers.keySet()) {
			if (fc instanceof JarFactoryContainer) {
				try {
					final File jarFile = ((JarFactoryContainer)fc).getJarFile();
					// if null, will add to bad container set below.
					if( jarFile != null ){
						String key = jarFile.getCanonicalPath();
						addToResourcesMap(key, jproj);
					}
				} catch (IOException e) {
					// If there's something this malformed on the factory path,
					// don't bother putting it on the resources map; we'll never
					// get notified about a change to it anyway.  It should get
					// reported either as a bad container (below) or as a failure
					// to load (later on).
				}
			}
			if ( !fc.exists() ) {
				if (badContainers == null) {
					badContainers = new HashSet<FactoryContainer>();
				}
				badContainers.add(fc);
			}
		}
		return badContainers;
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
			//cl = new JarClassLoader( fileList, AnnotationProcessorFactoryLoader.class.getClassLoader() );
			// Temporary revert to URLClassLoader, as the JarClassLoader doesn't properly define packages
			List<URL> urls = new ArrayList<URL>(fileList.size());
			for (File f : fileList) {
				try {
					urls.add(f.toURL());
				}
				catch (MalformedURLException mue) {
					mue.printStackTrace();
				}
			}
			URL[] urlArray = urls.toArray(new URL[urls.size()]);
			cl = new URLClassLoader( urlArray, AnnotationProcessorFactoryLoader.class.getClassLoader() );
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
			//_batchClassLoader = new JarClassLoader( fileList, iterativeClassLoader );
//			 Temporary revert to URLClassLoader, as the JarClassLoader doesn't properly define packages
			List<URL> urls = new ArrayList<URL>(fileList.size());
			for (File f : fileList) {
				try {
					urls.add(f.toURL());
				}
				catch (MalformedURLException mue) {
					mue.printStackTrace();
				}
			}
			URL[] urlArray = urls.toArray(new URL[urls.size()]);
			_batchClassLoader = new URLClassLoader( urlArray, AnnotationProcessorFactoryLoader.class.getClassLoader() );
		}
		else {
			// No batch classloader
			_batchClassLoader = null;
		}		
	}
}
