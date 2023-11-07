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
 *    wharley@bea.com - initial API and implementation
 *    sxenos@gmail.com - Bug 472066 - Deadlock in AnnotationProcessorFactoryLoader
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.Closeable;
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
import java.util.Map.Entry;
import java.util.Set;

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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
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
 * <strong>Processor Factories</strong>
 * <p>
 * This class is compilable against a Java 1.5 runtime.  However, it includes
 * support for discovering and loading both Java 5 and Java 6 annotation
 * processors.  Java 5 annotation processors include a factory object, the
 * AnnotationProcessorFactory, so for Java 5 we simply cache the factory; the
 * client code uses the factory to produce an actual AnnotationProcessor.
 * Java 6 processors do not have a separate factory, so we cache the Class
 * object of the processor implementation and use it to produce new instances.
 * This is wrapped within an IServiceFactory interface, for flexibility in
 * loading from various sources.  The actual Processor class does not exist
 * in the Java 1.5 runtime, so all access to it must be done via reflection.
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

	private static final String JAR_EXTENSION = "jar"; //$NON-NLS-1$

	private static final Object cacheMutex = new Object();

	// Caches the factory classes associated with each project.
	// See class comments for lifecycle of items in this cache.
	// Guarded by cacheMutex
	private final Map<IJavaProject, Map<AnnotationProcessorFactory, FactoryPath.Attributes>> _project2Java5Factories =
		new HashMap<>();

	// Guarded by cacheMutex
	private final Map<IJavaProject, Map<IServiceFactory, FactoryPath.Attributes>> _project2Java6Factories =
		new HashMap<>();

	// Caches the iterative classloaders so that iterative processors
	// are not reloaded on every batch build, unlike batch processors
	// which are.
	// See class comments for lifecycle of items in this cache.
	// Guarded by cacheMutex
	private final Map<IJavaProject, ClassLoader> _iterativeLoaders =
		new HashMap<>();

	// Guarded by cacheMutex
	private final Map<IJavaProject,ClassLoader> _batchLoaders =
		new HashMap<>();

	// Caches information about which resources affect which projects'
	// factory paths.
	// See class comments for lifecycle of items in this cache.
	// Guarded by cacheMutex
	private final Map<String, Set<IJavaProject>> _container2Project =
		new HashMap<>();


	/**
	 * Listen for changes that would affect the factory caches or
	 * build markers.
	 */
	private class ResourceListener implements IResourceChangeListener {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			Map<IJavaProject, LoadFailureHandler> failureHandlers = new HashMap<>();
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
					processChanges(affected, failureHandlers);
				}
				break;

			}
			for (LoadFailureHandler handler : failureHandlers.values()) {
				handler.reportFailureMarkers();
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
				 _affected = new HashSet<>(5);
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
		@Override
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
						synchronized (cacheMutex) {
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
					}
					else {
						// Lookup key is the canonical path of the resource
						String key = null;
						key = absolutePath.toFile().getCanonicalPath();

						synchronized (cacheMutex) {
							Set<IJavaProject> projects = _container2Project.get(key);
							if (projects != null) {
								addAffected(projects);
							}
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

    /**
     * Called when workspace preferences change.  Resource changes, including
     * changes to project-specific factory paths, are picked up through the
     * ResourceChangedListener mechanism instead.
     */
    public void resetAll() {
    	removeAptBuildProblemMarkers( null );
    	Set<ClassLoader> toClose = new HashSet<>();

    	synchronized (cacheMutex) {
    		toClose.addAll(_iterativeLoaders.values());
    		toClose.addAll(_batchLoaders.values());

        	_project2Java5Factories.clear();
        	_project2Java6Factories.clear();
        	_iterativeLoaders.clear();
        	_container2Project.clear();
        	_batchLoaders.clear();
    	}

    	// Need to close the iterative and batch classloaders
    	for (ClassLoader cl : toClose) {
    		tryToCloseClassLoader(cl);
    	}

    	// Validate all projects
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject proj : root.getProjects()) {
			verifyFactoryPath(JavaCore.create(proj));
		}
    }

    /**
     * Called when doing a clean build -- resets
     * the classloaders for the batch processors
     */
    public void resetBatchProcessors(IJavaProject javaProj) {
    	Iterable<Attributes> attrs = null;

    	Map<AnnotationProcessorFactory, Attributes> factories;
		Map<IServiceFactory, Attributes> java6factories;
		synchronized (cacheMutex) {
			factories = _project2Java5Factories.get(javaProj);
			java6factories = _project2Java6Factories.get(javaProj);
    	}
    	if (factories != null) {
    		attrs = factories.values();
    	}
    	else {
    		if (java6factories != null) {
    			attrs = java6factories.values();
    		}
    		else {
    			// This project's factories have already been cleared.
    			return;
    		}
    	}
    	boolean batchProcsFound = false;
    	for (Attributes attr : attrs) {
    		if (attr.runInBatchMode()) {
    			batchProcsFound = true;
    			break;
    		}
		}

		ClassLoader c;
		synchronized (cacheMutex) {
			if (batchProcsFound) {
				_project2Java5Factories.remove(javaProj);
				_project2Java6Factories.remove(javaProj);
			}

			c = _batchLoaders.remove(javaProj);
    	}
    	tryToCloseClassLoader(c);
    }

    /**
     * @param jproj must not be null
     * @return order preserving map of annotation processor factories to their attributes.
     * The order of the annotation processor factories respects the order of factory
     * containers in <code>jproj</code>.  The map is unmodifiable, and may be empty but
     * will not be null.
     */
    public Map<AnnotationProcessorFactory, FactoryPath.Attributes>
    	getJava5FactoriesAndAttributesForProject(IJavaProject jproj){

    	// We can't create problem markers inside synchronization -- see https://bugs.eclipse.org/bugs/show_bug.cgi?id=184923
    	LoadFailureHandler failureHandler = new LoadFailureHandler(jproj);

    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories;
    	synchronized (cacheMutex) {
	    	factories = _project2Java5Factories.get(jproj);
    	}

    	if( factories == null ) {
	    	// Load the project
			FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
			Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers();
			loadFactories(containers, jproj, failureHandler);

	    	failureHandler.reportFailureMarkers();

	    	synchronized (cacheMutex) {
		    	factories = _project2Java5Factories.get(jproj);
	    	}
    	}

    	if (factories != null) {
    		return Collections.unmodifiableMap(factories);
    	} else {
    		return Collections.emptyMap();
    	}
    }

    /**
     * @param jproj must not be null
     * @return order preserving map of annotation processor factories to their attributes.
     * The order of the annotation processor factories respects the order of factory
     * containers in <code>jproj</code>.  The map is unmodifiable, and may be empty but
     * will not be null.
     */
    public Map<IServiceFactory, FactoryPath.Attributes>
    	getJava6FactoriesAndAttributesForProject(IJavaProject jproj) {

    	// We can't create problem markers inside synchronization -- see https://bugs.eclipse.org/bugs/show_bug.cgi?id=184923
    	LoadFailureHandler failureHandler = new LoadFailureHandler(jproj);

    	Map<IServiceFactory, FactoryPath.Attributes> factories;
    	synchronized (cacheMutex) {
	    	 factories = _project2Java6Factories.get(jproj);
    	}

	    if (factories == null) {
	    	// Load the project
			FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
			Map<FactoryContainer, FactoryPath.Attributes> containers = fp.getEnabledContainers();
			loadFactories(containers, jproj, failureHandler);

	    	synchronized (cacheMutex) {
		    	 factories = _project2Java6Factories.get(jproj);
	    	}
    	}

    	failureHandler.reportFailureMarkers();
    	if (factories != null) {
    		return Collections.unmodifiableMap(factories);
    	} else {
    		return Collections.emptyMap();
    	}
    }

    /**
     * Convenience method: get the key set of the map returned by
     * @see #getJava5FactoriesAndAttributesForProject(IJavaProject) as a List.
     */
    public List<AnnotationProcessorFactory> getJava5FactoriesForProject( IJavaProject jproj ) {
    	Map<AnnotationProcessorFactory, FactoryPath.Attributes> factoriesAndAttrs =
    		getJava5FactoriesAndAttributesForProject(jproj);
    	final List<AnnotationProcessorFactory> factories =
    		new ArrayList<>(factoriesAndAttrs.keySet());
    	return Collections.unmodifiableList(factories);
    }

    /**
     * Add the resource/project pair 'key' -> 'jproj' to the
     * _container2Project map.
     * @param key the canonicalized pathname of the resource
     * @param jproj must not be null
     */
	private void addToResourcesMap(String key, IJavaProject jproj) {
		synchronized (cacheMutex) {
			Set<IJavaProject> s = _container2Project.get(key);
			if (s == null) {
				s = new HashSet<>();
				_container2Project.put(key, s);
			}
			s.add(jproj);
		}
	}

	/**
	 * Wrapper around ClassLoader.loadClass().newInstance() to handle reporting of errors.
	 */
	private Object loadInstance( String factoryName, ClassLoader cl, IJavaProject jproj, LoadFailureHandler failureHandler )
	{
		Object f = null;
		try
		{
			Class<?> c = cl.loadClass( factoryName );
			f = c.getDeclaredConstructor().newInstance();
		}
		catch( Exception e )
		{
			AptPlugin.trace("Failed to load factory " + factoryName, e); //$NON-NLS-1$
			failureHandler.addFailedFactory(factoryName);
		}
		catch ( NoClassDefFoundError ncdfe )
		{
			AptPlugin.trace("Failed to load " + factoryName, ncdfe); //$NON-NLS-1$
			failureHandler.addFailedFactory(factoryName);
		}
		return f;
	}

	/**
	 * Load all Java 5 and Java 6 processors on the factory path.  This also resets the
	 * APT-related build problem markers.  Results are saved in the factory caches.
	 * @param containers an ordered map.
	 */
	private void loadFactories(
			Map<FactoryContainer, FactoryPath.Attributes> containers,
			IJavaProject project,
			LoadFailureHandler failureHandler)
	{
		Map<AnnotationProcessorFactory, FactoryPath.Attributes> java5Factories =
			new LinkedHashMap<>();
		Map<IServiceFactory, FactoryPath.Attributes> java6Factories =
			new LinkedHashMap<>();

		removeAptBuildProblemMarkers(project);
		Set<FactoryContainer> badContainers = verifyFactoryPath(project);
		if (badContainers != null) {
			for (FactoryContainer badFC : badContainers) {
				failureHandler.addFailedFactory(badFC.getId());
				containers.remove(badFC);
			}
		}

		// Need to use the cached classloader if we have one
		ClassLoader iterativeClassLoader;
		synchronized (cacheMutex) {
			iterativeClassLoader = _iterativeLoaders.get(project);
			if (iterativeClassLoader == null) {
				iterativeClassLoader = _createIterativeClassLoader(containers);
				_iterativeLoaders.put(project, iterativeClassLoader);
			}
		}

		ClassLoader batchClassLoader = _createBatchClassLoader(containers, project);

		for ( Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet() )
		{
			try {
				final FactoryContainer fc = entry.getKey();
				final FactoryPath.Attributes attr = entry.getValue();
				assert !attr.runInBatchMode() || (batchClassLoader != null);
				ClassLoader cl = attr.runInBatchMode() ? batchClassLoader : iterativeClassLoader;

				// First the Java 5 factories in this container...
				List<AnnotationProcessorFactory> java5FactoriesInContainer;
				java5FactoriesInContainer = loadJava5FactoryClasses(fc, cl, project, failureHandler);
				for ( AnnotationProcessorFactory apf : java5FactoriesInContainer ) {
					java5Factories.put( apf, entry.getValue() );
				}

				if (AptPlugin.canRunJava6Processors()) {
					// Now the Java 6 factories.  Use the same classloader for the sake of sanity.
					List<IServiceFactory> java6FactoriesInContainer;
					java6FactoriesInContainer = loadJava6FactoryClasses(fc, cl, project, failureHandler);
					for ( IServiceFactory isf : java6FactoriesInContainer ) {
						java6Factories.put( isf, entry.getValue() );
					}
				}
			}
			catch (FileNotFoundException fnfe) {
				// it would be bizarre to get this, given that we already checked for file existence up above.
				AptPlugin.log(fnfe, Messages.AnnotationProcessorFactoryLoader_jarNotFound + fnfe.getLocalizedMessage());
			}
			catch (IOException ioe) {
				AptPlugin.log(ioe, Messages.AnnotationProcessorFactoryLoader_ioError + ioe.getLocalizedMessage());
			}
		}

		synchronized (cacheMutex) {
			_project2Java5Factories.put(project, java5Factories);
			_project2Java6Factories.put(project, java6Factories);
		}
	}

	private List<AnnotationProcessorFactory> loadJava5FactoryClasses(
			FactoryContainer fc, ClassLoader classLoader, IJavaProject jproj, LoadFailureHandler failureHandler )
			throws IOException
	{
		Map<String, String> factoryNames = fc.getFactoryNames();
		List<AnnotationProcessorFactory> factories = new ArrayList<>();
		for ( Entry<String, String> entry : factoryNames.entrySet() )
		{
			if (AptPlugin.JAVA5_FACTORY_NAME.equals(entry.getValue())) {
				String factoryName = entry.getKey();
				AnnotationProcessorFactory factory;
				if ( fc.getType() == FactoryType.PLUGIN )
					factory = FactoryPluginManager.getJava5FactoryFromPlugin( factoryName );
				else
					factory = (AnnotationProcessorFactory)loadInstance( factoryName, classLoader, jproj, failureHandler );

				if ( factory != null )
					factories.add( factory );
			}
		}
		return factories;
	}

	private List<IServiceFactory> loadJava6FactoryClasses(
			FactoryContainer fc, ClassLoader classLoader, IJavaProject jproj, LoadFailureHandler failureHandler )
			throws IOException
	{
		Map<String, String> factoryNames = fc.getFactoryNames();
		List<IServiceFactory> factories = new ArrayList<>();
		for ( Entry<String, String> entry : factoryNames.entrySet() )
		{
			if (AptPlugin.JAVA6_FACTORY_NAME.equals(entry.getValue())) {
				String factoryName = entry.getKey();
				IServiceFactory factory = null;
				if ( fc.getType() == FactoryType.PLUGIN ) {
					factory = FactoryPluginManager.getJava6FactoryFromPlugin( factoryName );
				}
				else {
					Class<?> clazz;
					try {
						clazz = classLoader.loadClass(factoryName);
						factory = new ClassServiceFactory(clazz);
					} catch (ClassNotFoundException | ClassFormatError e) {
						AptPlugin.trace("Unable to load annotation processor " + factoryName, e); //$NON-NLS-1$
						failureHandler.addFailedFactory(factoryName);
					}
				}

				if ( factory != null )
					factories.add( factory );
			}
		}
		return factories;
	}

	/**
	 * Re-validate projects whose factory paths may have been affected
	 * by a resource change (e.g., adding a previously absent jar file).
	 * This will cause build problem markers to be removed and regenerated,
	 * and factory class caches to be cleared.
	 */
	private void processChanges(Set<IJavaProject> affected, Map<IJavaProject,LoadFailureHandler> handlers) {
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
					LoadFailureHandler handler = handlers.get(jproj);
					if (handler == null) {
						handler = new LoadFailureHandler(jproj);
						handlers.put(jproj, handler);
					}
					for (FactoryContainer container : badContainers) {
						handler.addMissingLibrary(container.getId());
					}
				}
			}
		}

		// TODO: flag the affected projects for rebuild.
	}

	/**
	 * When a project is deleted, remove its factory path information from the loader.
	 */
    private void uncacheProject(IJavaProject jproj) {
    	ClassLoader c;
    	ClassLoader cl;

    	synchronized (cacheMutex) {
			_project2Java5Factories.remove(jproj);
			_project2Java6Factories.remove(jproj);
			c = _iterativeLoaders.remove(jproj);
			cl = _batchLoaders.remove(jproj);
    	}

		tryToCloseClassLoader(c);
		tryToCloseClassLoader(cl);

		removeProjectFromResourceMap(jproj);
	}

	/**
	 * Remove APT build problem markers, e.g., "missing factory jar".
	 * @param jproj if null, remove markers from all projects that have
	 * factory paths associated with them.
	 */
	private void removeAptBuildProblemMarkers( IJavaProject jproj ) {
		// note that _project2Java6Factories.keySet() should be same as that for Java5.
		Set<IJavaProject> jprojects;
		synchronized (cacheMutex) {
			jprojects = (jproj == null) ? new HashSet<>(_project2Java5Factories.keySet())
					: Collections.singleton(jproj);
		}
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
		synchronized (cacheMutex) {
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
					badContainers = new HashSet<>();
				}
				badContainers.add(fc);
			}
		}
		return badContainers;
    }

	/**
	 * @param containers an ordered map.
	 */
	private static ClassLoader _createIterativeClassLoader( Map<FactoryContainer, FactoryPath.Attributes> containers )
	{
		ArrayList<File> fileList = new ArrayList<>( containers.size() );
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
			cl = createClassLoader( fileList, getParentClassLoader());
		}
		else {
			cl = getParentClassLoader();
		}
		return cl;
	}

	/**
	 * Returns the batch class loader or null if none
	 */
	private ClassLoader _createBatchClassLoader(Map<FactoryContainer, FactoryPath.Attributes> containers,
			IJavaProject p) {
		ArrayList<File> fileList = new ArrayList<>( containers.size() );
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : containers.entrySet()) {
			FactoryPath.Attributes attr = entry.getValue();
			FactoryContainer fc = entry.getKey();
			if (attr.runInBatchMode() && fc instanceof JarFactoryContainer) {

				JarFactoryContainer jfc = (JarFactoryContainer)fc;
				File f = jfc.getJarFile();
				fileList.add( f );

			}
		}

		ClassLoader result = null;
		// Try to use the iterative CL as parent, so we can resolve classes within it
		synchronized (cacheMutex) {
			ClassLoader parentCL = _iterativeLoaders.get(p);
			if (parentCL == null) {
				parentCL = getParentClassLoader();
			}

			if ( fileList.size() > 0 ) {
				result = createClassLoader( fileList, parentCL);
				_batchLoaders.put(p, result);
			}
		}
		return result;
	}

	private static ClassLoader getParentClassLoader() {
		final ClassLoader loaderForComSunMirrorClasses = AnnotationProcessorFactoryLoader.class.getClassLoader();
		final ClassLoader loaderForEverythingElse = ClassLoader.getSystemClassLoader();
		return new ClassLoader() {
			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				if (name.startsWith("com.sun.mirror.")) { //$NON-NLS-1$
					if (name.startsWith("com.sun.mirror.apt") //$NON-NLS-1$
							|| name.startsWith("com.sun.mirror.declaration") //$NON-NLS-1$
							|| name.startsWith("com.sun.mirror.type") //$NON-NLS-1$
							|| name.startsWith("com.sun.mirror.util")) { //$NON-NLS-1$
						return loaderForComSunMirrorClasses.loadClass(name);
					}
				}
				return loaderForEverythingElse.loadClass(name);
			}
		};
	}

	private static ClassLoader createClassLoader(List<File> files, ClassLoader parentCL) {
		//return new JarClassLoader(files, parentCL);
		List<URL> urls = new ArrayList<>(files.size());
		for (int i=0;i<files.size();i++) {
			try {
				urls.add(files.get(i).toURI().toURL());
			}
			catch (MalformedURLException mue) {
				// ignore
			}
		}
		URL[] urlArray = urls.toArray(new URL[urls.size()]);
		return new URLClassLoader(urlArray, parentCL);
	}

	private static void tryToCloseClassLoader(ClassLoader classLoader) {
		if (classLoader instanceof Closeable) {
			try {
				((Closeable) classLoader).close();
			} catch (IOException e) {
				AptPlugin.log(e, "Unable to close class loader."); //$NON-NLS-1$
			}
		}
	}
}
