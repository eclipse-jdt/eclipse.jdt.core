package org.eclipse.jdt.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.core.search.AbstractSearchScope;
import org.eclipse.jdt.internal.core.search.indexing.*;

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import javax.xml.parsers.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * The <code>JavaModelManager</code> manages instances of <code>IJavaModel</code>.
 * <code>IElementChangedListener</code>s register with the <code>JavaModelManager</code>,
 * and receive <code>ElementChangedEvent</code>s for all <code>IJavaModel</code>s.
 * <p>
 * The single instance of <code>JavaModelManager</code> is available from
 * the static method <code>JavaModelManager.getJavaModelManager()</code>.
 */
public class JavaModelManager implements IResourceChangeListener, ISaveParticipant { 	

	/**
	 * Variable pool
	 */
	public static Map Variables = new HashMap(5);
/**
 * Returns whether the given full path (for a package) conflicts with the output location
 * of the given project.
 */
public static boolean conflictsWithOutputLocation(IPath folderPath, JavaProject project) {
	try {
		IPath outputLocation = project.getOutputLocation();
		if (outputLocation == null) {
			// in doubt, there is a conflict
			return true;
		}
		if (outputLocation.isPrefixOf(folderPath)) {
			// only allow nesting in outputlocation if there is a corresponding source folder
			return project.getClasspathEntryFor(outputLocation) == null;
		}
		return false;
	} catch (JavaModelException e) {
		// in doubt, there is a conflict
		return true;
	}
}
	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element.
	 * <p>
	 * The resource must be one of:<ul>
	 *	<li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *  <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
	 *			or <code>IPackageFragment</code></li>
	 *  <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IResource resource, IJavaProject project) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT :
				return JavaCore.create((IProject) resource);
			case IResource.FILE :
				return create((IFile) resource, project);
			case IResource.FOLDER :
				return create((IFolder) resource, project);
			case IResource.ROOT :
				return JavaCore.create((IWorkspaceRoot) resource);
			default :
				return null;
		}
	}
/**
 * Returns the Java element corresponding to the given file, its project being the given
 * project.
 * Returns <code>null</code> if unable to associate the given file
 * with a Java element.
 *
 * <p>The file must be one of:<ul>
 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
 *	</ul>
 * <p>
 * Creating a Java element has the side effect of creating and opening all of the
 * element's parents if they are not yet open.
 */
public static IJavaElement create(IFile file, IJavaProject project) {
	if (file == null) {
		return null;
	}
	if (project == null) {
		project = JavaCore.create(file.getProject());
	}

	String extension = file.getFileExtension();
	if (extension != null) {
		if (Util.isValidCompilationUnitName(file.getName())) {
			return createCompilationUnitFrom(file, project);
		} else if (Util.isValidClassFileName(file.getName())) {
			return createClassFileFrom(file, project);
		} else if (extension.equalsIgnoreCase("jar"  //$NON-NLS-1$
			) || extension.equalsIgnoreCase("zip"  //$NON-NLS-1$
			)) {
			return createJarPackageFragmentRootFrom(file, project);
		}
	}
	return null;
}
/**
 * Returns the package fragment or package fragment root corresponding to the given folder,
 * its parent or great parent being the given project. 
 * or <code>null</code> if unable to associate the given folder with a Java element.
 * <p>
 * Note that a package fragment root is returned rather than a default package.
 * <p>
 * Creating a Java element has the side effect of creating and opening all of the
 * element's parents if they are not yet open.
 */
public static IJavaElement create(IFolder folder, IJavaProject project) {
	if (folder == null) {
		return null;
	}
	if (project == null) {
		project = JavaCore.create(folder.getProject());
	}
	IJavaElement element = determineIfOnClasspath(folder, project);
	if (conflictsWithOutputLocation(folder.getFullPath(), (JavaProject)project)
	 	|| (folder.getName().indexOf('.') >= 0 
	 		&& !(element instanceof IPackageFragmentRoot))) {
		return null; // only package fragment roots are allowed with dot names
	} else {
		return element;
	}
}
/**
 * Creates and returns a class file element for the given <code>.class</code> file,
 * its project being the given project. Returns <code>null</code> if unable
 * to recognize the class file.
 */
public static IClassFile createClassFileFrom(IFile file, IJavaProject project ) {
	if (file == null) {
		return null;
	}
	if (project == null) {
		project = JavaCore.create(file.getProject());
	}
	IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
	if (pkg == null) {
		// fix for 1FVS7WE
		// not on classpath - make the root its folder, and a default package
		IPackageFragmentRoot root = project.getPackageFragmentRoot(file.getParent());
		pkg = root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	}
	return pkg.getClassFile(file.getName());
}
/**
 * Creates and returns a compilation unit element for the given <code>.java</code> 
 * file, its project being the given project. Returns <code>null</code> if unable
 * to recognize the compilation unit.
 */
public static ICompilationUnit createCompilationUnitFrom(IFile file, IJavaProject project) {
	if (file == null) {
		return null;
	}
	if (project == null) {
		project = JavaCore.create(file.getProject());
	}
	
	IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
	if (pkg == null) {
		// fix for 1FVS7WE
		// not on classpath - make the root its folder, and a default package
		IPackageFragmentRoot root = project.getPackageFragmentRoot(file.getParent());
		pkg = root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	}
	return pkg.getCompilationUnit(file.getName());
}
	/**
	 * Creates and returns a handle for the given JAR file, its project being the given project.
	 * The Java model associated with the JAR's project may be
	 * created as a side effect. 
	 * Returns <code>null</code> if unable to create a JAR package fragment root.
	 * (for example, if the JAR file represents a non-Java resource)
	 */
	public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file, IJavaProject project) {
		if (file == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(file.getProject());
		}
	
		// Create a jar package fragment root only if on the classpath
		IPath resourcePath = file.getFullPath();
		try {
			IClasspathEntry[] entries = ((JavaProject)project).getResolvedClasspath(true);
			for (int i = 0, length = entries.length; i < length; i++) {
				IClasspathEntry entry = entries[i];
				IPath rootPath = entry.getPath();
				if (rootPath.equals(resourcePath)) {
					return project.getPackageFragmentRoot(file);
				}
			}
		} catch (JavaModelException e) {
		}
		return null;
	}
	/**
	 * Returns the package fragment root represented by the resource, or
	 * the package fragment the given resource is located in, or <code>null</code>
	 * if the given resource is not on the classpath of the given project.
	 */
	public static IJavaElement determineIfOnClasspath(
		IResource resource,
		IJavaProject project) {
			
		IPath resourcePath = resource.getFullPath();
		try {
			IClasspathEntry[] entries = 
				Util.isJavaFileName(resourcePath.lastSegment())
					? project.getRawClasspath() // JAVA file can only live inside SRC folder (on the raw path)
					: ((JavaProject)project).getResolvedClasspath(true);
				
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) continue;
				IPath rootPath = entry.getPath();
				if (rootPath.equals(resourcePath)) {
					return project.getPackageFragmentRoot(resource);
				} else if (rootPath.isPrefixOf(resourcePath)) {
					IPackageFragmentRoot root = ((JavaProject) project).getPackageFragmentRoot(rootPath);
					if (root == null) return null;
					IPath pkgPath = resourcePath.removeFirstSegments(rootPath.segmentCount());
					if (resource.getType() == IResource.FILE) {
						// if the resource is a file, then remove the last segment which
						// is the file name in the package
						pkgPath = pkgPath.removeLastSegments(1);
					}
					String pkgName = Util.packageName(pkgPath);
					if (pkgName == null || JavaConventions.validatePackageName(pkgName).getSeverity() == IStatus.ERROR) {
						return null;
					}
					return root.getPackageFragment(pkgName);
				}
			}
		} catch (JavaModelException npe) {
			return null;
		}
		return null;
	}
	
	/**
	 * The singleton manager
	 */
	protected static JavaModelManager fgManager= null;


	
	/**
	 * Infos cache.
	 */
	protected JavaModelCache cache = new JavaModelCache();

	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Map elementsOutOfSynchWithBuffers = new HashMap(11);
	
	/**
	 * Turns delta firing on/off. By default it is on.
	 */
	protected boolean fFire= true;

	/**
	 * Queue of deltas created explicily by the Java Model that
	 * have yet to be fired.
	 */
	protected ArrayList fJavaModelDeltas= new ArrayList();
	/**
	 * Collection of listeners for Java element deltas
	 */
	private IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
	private int[] elementChangedListenerMasks = new int[5];
	private int elementChangedListenerCount = 0;
	public int currentChangeEventType = ElementChangedEvent.PRE_AUTO_BUILD;
	public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with ElementChangedEvent event masks

	/**
	 * Collection of projects that are in the process of being deleted.
	 * Project reside in this cache from the time the plugin receives
	 * the #deleting message until they resource delta is received
	 * claiming the project has been deleted. The java model will not allow
	 * a project that is being deleted to be opened - since this can leave
	 * files open, causing the delete to fail.
	 *
	 * fix for 1FW67PA
	 */
	protected ArrayList fProjectsBeingDeleted= new ArrayList();

	/**
	 * Used to convert <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
	 */
	protected DeltaProcessor fDeltaProcessor= new DeltaProcessor();

	/**
	 * Local Java workspace properties file name (generated inside JavaCore plugin state location)
	 */
	private static final String WKS_PROP_FILENAME= "workspace.properties"; //$NON-NLS-1$

	/**
	 * Name of the handle id attribute in a Java marker
	 */
	private static final String ATT_HANDLE_ID= "org.eclipse.jdt.internal.core.JavaModelManager.handleId"; //$NON-NLS-1$

	/**
	 * Table from IProject to PerProjectInfo.
	 */
	protected Map perProjectInfo = new HashMap(5);
	
	/**
	 * A map from ICompilationUnit to IWorkingCopy
	 * of the shared working copies.
	 */
	protected Map sharedWorkingCopies = new HashMap();
	
	/**
	 * A weak set of the known scopes.
	 */
	protected WeakHashMap scopes = new WeakHashMap();

	static class PerProjectInfo {
		IProject project;
		Object savedState;
		boolean triedRead;
		PerProjectInfo(IProject project) {
			this.triedRead = false;
			this.savedState = null;
			this.project = project;
		}
	};
	public static boolean VERBOSE = false;
	public static boolean VARIABLE_VERBOSE = false;
	public static boolean ZIP_ACCESS_VERBOSE = false;
	
	/**
	 * A cache of opened zip files
	 * (map from IPath to java.io.ZipFile
	 */
	public HashMap zipFiles;
	
	/**
	 * The number of clients of the zip file cache.
	 */
	private int zipFilesClientCount = 0;

	/**
	 * Line separator to use throughout the JavaModel for any source edit operation
	 */
//	public static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	/**
	 * Constructs a new JavaModelManager
	 */
	private JavaModelManager() {
	}
	/**
	 * addElementChangedListener method comment.
	 * Need to clone defensively the listener information, in case some listener is reacting to some notification iteration by adding/changing/removing
	 * any of the other (i.e. it deregisters itself).
	 */
	public void addElementChangedListener(IElementChangedListener listener, int eventMask) {
		for (int i = 0; i < this.elementChangedListenerCount; i++){
			if (this.elementChangedListeners[i].equals(listener)){
				
				// only clone the masks, since we could be in the middle of notifications and one listener decide to change
				// any event mask of another listeners (yet not notified).
				int cloneLength = this.elementChangedListenerMasks.length;
				System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[cloneLength], 0, cloneLength);
				this.elementChangedListenerMasks[i] = eventMask; // could be different
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount){
			System.arraycopy(this.elementChangedListeners, 0, this.elementChangedListeners = new IElementChangedListener[length*2], 0, length);
			System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[length*2], 0, length);
		}
		this.elementChangedListeners[this.elementChangedListenerCount] = listener;
		this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
		this.elementChangedListenerCount++;
	}

	/**
	 * Starts caching ZipFiles.
	 * Ignores if there are already clients.
	 */
	public synchronized void cacheZipFiles() {
		if (this.zipFilesClientCount == 0) {
			this.zipFiles = new HashMap();
		}
		this.zipFilesClientCount++;
	}
/*
 * Checks that the delta contains an added project. In this case,
 * removes it from the list of projects being deleted.
 */
public void checkProjectBeingAdded(IResourceDelta delta) {
	IResource resource = delta.getResource();
	switch (resource.getType()) {
		case IResource.ROOT :
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				this.checkProjectBeingAdded(children[i]);
			}
			break;
		case IResource.PROJECT :
			int deltaKind = delta.getKind();
			if (deltaKind == IResourceDelta.ADDED /* case of a project removed then added */
				|| deltaKind == IResourceDelta.CHANGED /* case of a project removed then added then changed */) {
				fProjectsBeingDeleted.remove(delta.getResource());
			}
	}
}


	/**
	 * Note that the project is about to be deleted.
	 *
	 * fix for 1FW67PA
	 */
	public void deleting(IProject project) {
		
		getIndexManager().deleting(project);
		
		if (!fProjectsBeingDeleted.contains(project)) {
			fProjectsBeingDeleted.add(project);
		}
	}
/**
 * @see ISaveParticipant
 */
public void doneSaving(ISaveContext context){
}
	/**
	 * Make sure the resource content is available locally
	 */
	public void ensureLocal(IResource resource) throws CoreException {

		// need to be tuned once having VCM support
		// resource.ensureLocal(IResource.DEPTH_ZERO, null);

		if (!resource.isLocal(IResource.DEPTH_ZERO) || !resource.exists()) { // project is always local but might not exist
			throw new CoreException(new JavaModelStatus(IJavaModelStatusConstants.NO_LOCAL_CONTENTS, resource.getFullPath()));
		}
	}

	
	/**
	 * Fire Java Model delta, flushing them after the fact after post_change notification.
	 * If the firing mode has been turned off, this has no effect. 
	 */
	public void fire(JavaElementDelta customDelta, int originalEventType) {

		if (fFire) {

			int eventType;
			
			/* DEFAULT event type is used when operation doesn't know actual event type and needed to fire immediately:
			 * e.g. non-resource modifying operation, create/destroy shared working copies
			 *
			 * this is mapped to a POST-change + PRE-build change for all interested listeners
			 */
			if (originalEventType == DEFAULT_CHANGE_EVENT){
				eventType = ElementChangedEvent.POST_CHANGE;
			} else {
				eventType = originalEventType;
			}
			
			JavaElementDelta deltaToNotify;
			if (customDelta == null){
				this.mergeDeltas();
				if (fJavaModelDeltas.size() > 0){ 

					// cannot be more than 1 after merge
					deltaToNotify = (JavaElementDelta)fJavaModelDeltas.get(0);

					// empty the queue only after having fired final volley of deltas and no custom deltas was superposed
					if (eventType == ElementChangedEvent.POST_CHANGE){
						// flush now so as to keep listener reactions to post their own deltas for subsequent iteration
						this.flush();
					}
				} else {
					return;
				}
			} else {
				deltaToNotify = customDelta;
			}
				
			// Refresh internal scopes
			Iterator scopes = this.scopes.keySet().iterator();
			while (scopes.hasNext()) {
				AbstractSearchScope scope = (AbstractSearchScope)scopes.next();
				scope.processDelta(deltaToNotify);
			}
				
			// Notification

			// Important: if any listener reacts to notification by updating the listeners list or mask, these lists will
			// be duplicated, so it is necessary to remember original lists in a variable (since field values may change under us)
			IElementChangedListener[] listeners = this.elementChangedListeners;
			int[] listenerMask = this.elementChangedListenerMasks;
			int listenerCount = this.elementChangedListenerCount;

			// in case using a DEFAULT change event, will notify also all listeners also interested in PRE-build events
			if (originalEventType == DEFAULT_CHANGE_EVENT){
				if (DeltaProcessor.VERBOSE){
					System.out.println("FIRING PRE_AUTO_BUILD Delta ["+Thread.currentThread()+"]:\n" + deltaToNotify);//$NON-NLS-1$//$NON-NLS-2$
				}
				ElementChangedEvent extraEvent = new ElementChangedEvent(deltaToNotify, ElementChangedEvent.PRE_AUTO_BUILD);
				for (int i= 0; i < listenerCount; i++) {
					if ((listenerMask[i] & ElementChangedEvent.PRE_AUTO_BUILD) != 0){
						listeners[i].elementChanged(extraEvent);
					}
				}
			}

			// regular notification
			if (DeltaProcessor.VERBOSE){
				String type = "";
				switch (eventType) {
					case ElementChangedEvent.POST_CHANGE:
						type = "POST_CHANGE"; //$NON-NLS-2$
						break;
					case ElementChangedEvent.PRE_AUTO_BUILD:
						type = "PRE_AUTO_BUILD"; //$NON-NLS-2$
						break;
					case ElementChangedEvent.POST_RECONCILE:
						type = "POST_RECONCILE"; //$NON-NLS-2$
						break;
				}
				System.out.println("FIRING " + type + " Delta ["+Thread.currentThread()+"]:\n" + deltaToNotify);//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
			ElementChangedEvent event = new ElementChangedEvent(deltaToNotify, eventType);
			for (int i= 0; i < listenerCount; i++) {
				if ((listenerMask[i] & eventType) != 0){
					listeners[i].elementChanged(event);
				}
			}
		}
	}
	
	/**
	 * Flushes all deltas without firing them.
	 */
	protected void flush() {
		fJavaModelDeltas= new ArrayList();
	}
	/**
	 * Flushes ZipFiles cache if there are no more clients.
	 */
	public synchronized void flushZipFiles() {
		if (this.zipFilesClientCount == 0) return;
		this.zipFilesClientCount--;
		if (this.zipFilesClientCount > 0) return;
		Iterator iterator = this.zipFiles.values().iterator();
		while (iterator.hasNext()) {
			try {
				((ZipFile)iterator.next()).close();
			} catch (IOException e) {
			}
		}
		this.zipFiles = null;
	}
	/** 
	 * Returns the set of elements which are out of synch with their buffers.
	 */
	protected Map getElementsOutOfSynchWithBuffers() {
		return this.elementsOutOfSynchWithBuffers;
	}
	/**
	 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
	 * memento.
	 * @see getHandleMemento()
	 */
	public IJavaElement getHandleFromMemento(String memento) throws JavaModelException {
		if (memento == null) {
			return null;
		}
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		if (workspace == null) {
			return null;
		}
		JavaModel model= (JavaModel) getJavaModel(workspace);
		if (memento.equals("")){ // workspace memento //$NON-NLS-1$
			return model;
		}
		int modelEnd= memento.indexOf(JavaElement.JEM_JAVAPROJECT);
		if (modelEnd == -1) {
			return null;
		}
		boolean returnProject= false;
		int projectEnd= memento.indexOf(JavaElement.JEM_PACKAGEFRAGMENTROOT, modelEnd);
		if (projectEnd == -1) {
			projectEnd= memento.length();
			returnProject= true;
		}
		String projectName= memento.substring(modelEnd + 1, projectEnd);
		JavaProject proj= (JavaProject) model.getJavaProject(projectName);
		if (returnProject) {
			return proj;
		}
		int rootEnd= memento.indexOf(JavaElement.JEM_PACKAGEFRAGMENT, projectEnd + 1);
		if (rootEnd == -1) {
			return proj.getPackageFragmentRoot(new Path(Path.SEPARATOR + memento.substring(modelEnd + 1)));
		}
		String rootName= null;
		if (rootEnd == projectEnd - 1) {
			//default root
			rootName= IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
		} else {
			rootName= memento.substring(projectEnd + 1, rootEnd);
		}
		IPath rootPath= new Path(rootName);
		IPackageFragmentRoot root= null;
		if (rootPath.isAbsolute()) {
			root= proj.getPackageFragmentRoot(rootPath);
		} else {
			root= proj.getPackageFragmentRoot(proj.getProject().getFullPath().append(rootName));
		}
		if (root == null)
			return null;

		int end= memento.indexOf(JavaElement.JEM_COMPILATIONUNIT, rootEnd);
		if (end == -1) {
			end= memento.indexOf(JavaElement.JEM_CLASSFILE, rootEnd);
			if (end == -1) {
				if (rootEnd + 1 == memento.length()) {
					return root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
				} else {
					return root.getPackageFragment(memento.substring(rootEnd + 1));
				}
			}
			//deal with class file and binary members
			return model.getHandleFromMementoForBinaryMembers(memento, root, rootEnd, end);
		}

		//deal with compilation units and source members
		return model.getHandleFromMementoForSourceMembers(memento, root, rootEnd, end);
	}
	public IndexManager getIndexManager() {
		return fDeltaProcessor.indexManager;
	}
	/**
	 *  Returns the info for the element.
	 */
	public Object getInfo(IJavaElement element) {
		return this.cache.getInfo(element);
	}
	/**
	 * Returns the handle to the active Java Model, or <code>null</code> if there
	 * is no active Java Model.
	 */
	public IJavaModel getJavaModel() {
		return this.cache.getJavaModel();
	}
	/**
	 * Returns the JavaModel for the given workspace, creating
	 * it if it does not yet exist.
	 */
	public static JavaModel getJavaModel(IWorkspace workspace) {
		JavaModelCache modelCache = getJavaModelManager().cache;
		IJavaModel javaModel = modelCache.getJavaModel();
		if (javaModel != null) {
			// if the current java model corresponds to a different workspace,
			// try to close it
			JavaModelInfo modelInfo = (JavaModelInfo) modelCache.getInfo(javaModel);
			if (!modelInfo.workspace.equals(workspace)) {
				try {
					javaModel.close();
					javaModel = null;
				} catch (JavaModelException e) {
					Assert.isTrue(false, Util.bind("element.onlyOneJavaModel")); //$NON-NLS-1$
					return null;
				}
			}
		}
		if (javaModel == null) {
			return new JavaModel(workspace);
		} else {
			return (JavaModel)javaModel;
		}

	}
	/**
	 * Returns the singleton JavaModelManager
	 */
	public static synchronized JavaModelManager getJavaModelManager() {
		if (fgManager == null) {
			fgManager= new JavaModelManager();
		}
		return fgManager;
	}
	/**
	 * Returns the last built state for the given project, or null if there is none.
	 * Deserializes the state if necessary.
	 *
	 * @private for use by image builder and evaluation support only
	 */
	public Object getLastBuiltState(IProject project, IProgressMonitor monitor) {
		PerProjectInfo info = getPerProjectInfo(project);
		if (!info.triedRead) {
			info.triedRead = true;
			try {
				if (monitor != null)
					monitor.subTask(Util.bind("build.readStateProgress", project.getName())); //$NON-NLS-1$
				info.savedState = readState(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return info.savedState;
	}
	/**
	 * Returns the per-project info for the given project.
	 */
	private PerProjectInfo getPerProjectInfo(IProject project) {
		PerProjectInfo info= (PerProjectInfo) perProjectInfo.get(project);
		if (info == null) {
			info= new PerProjectInfo(project);
			perProjectInfo.put(project, info);
		}
		return info;
	}
	/**
	 * Returns the File to use for saving and restoring the last built state for the given project.
	 */
	private File getSerializationFile(IProject project) {
		if (!project.exists()) return null;
		IPluginDescriptor descr= JavaCore.getJavaCore().getDescriptor();
		IPath workingLocation= project.getPluginWorkingLocation(descr);
		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
	}
	public String getVariablesAsXMLString() throws CoreException {

		Document document = new DocumentImpl();
		Element rootElement = document.createElement("variables"); //$NON-NLS-1$
		document.appendChild(rootElement);

		String[] variables = JavaCore.getClasspathVariableNames();
		
		for (int i= 0; i < variables.length; ++i) {
			String var = variables[i];
			IPath varPath = JavaCore.getClasspathVariable(var);
			Element varElement= document.createElement("variable"); //$NON-NLS-1$
			varElement.setAttribute("name", var); //$NON-NLS-1$
			varElement.setAttribute("path", varPath.toString());			 //$NON-NLS-1$
			rootElement.appendChild(varElement);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		try {
			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			Serializer serializer = SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(writer, format);
			serializer.asDOMSerializer().serialize(document);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		return writer.toString();	
			
	}
	
/**
	 * Returns the open ZipFile at the given location. If the ZipFile
	 * does not yet exist, it is created, opened, and added to the cache
	 * of open ZipFiles. The location must be a absolute path.
	 *
	 * @exception CoreException If unable to create/open the ZipFile.
	 */
	public ZipFile getZipFile(IPath path) throws CoreException {
		ZipFile zipFile;
		if (this.zipFiles != null && (zipFile = (ZipFile)this.zipFiles.get(path)) != null) {
			return zipFile;
		}
		String fileSystemPath= null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource file = root.findMember(path);
		if (path.isAbsolute() && file != null) {
			if (file == null || file.getType() != IResource.FILE) {
				fileSystemPath= path.toOSString();
			} else {
				ensureLocal(file);
				fileSystemPath= file.getLocation().toOSString();
			}
		} else if (!path.isAbsolute()) {
			file= root.getFile(path);
			if (file == null || file.getType() != IResource.FILE) {
				throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("file.notFound"), null)); //$NON-NLS-1$
			}
			ensureLocal(file);
			fileSystemPath= file.getLocation().toOSString();
		} else {
			fileSystemPath= path.toOSString();
		}

		try {
			if (ZIP_ACCESS_VERBOSE) {
				System.out.println("[JavaModelManager.getZipFile(IPath)] Creating ZipFile on " + fileSystemPath ); //$NON-NLS-1$
			}
			zipFile = new ZipFile(fileSystemPath);
			if (this.zipFiles != null) {
				this.zipFiles.put(path, zipFile);
			}
			return zipFile;
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("status.IOException"), e)); //$NON-NLS-1$
		}
	}
	/**
	 * Returns true if the given project is being deleted, otherwise false.
	 *
	 * fix for 1FW67PA
	 */
	public boolean isBeingDeleted(IProject project) {
		return fProjectsBeingDeleted.contains(project);
	}
	/**
	 * Returns true if the firing is enabled
	 */
	public boolean isFiring() {
		return this.fFire;
	}
	public void loadVariables() throws CoreException {
		
		String xmlString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(
								new QualifiedName(JavaCore.PLUGIN_ID, "variables")); //$NON-NLS-1$
		try {
			if (xmlString != null) readVariables(xmlString);
		} catch(IOException e){
			return;
		}
	}
	
/**
 * Merged all awaiting deltas.
 */
public void mergeDeltas() {
	if (fJavaModelDeltas.size() <= 1) return;
	
	if (DeltaProcessor.VERBOSE) {
		System.out.println("MERGING " + fJavaModelDeltas.size() + " DELTAS ["+Thread.currentThread()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	Iterator deltas = fJavaModelDeltas.iterator();
	IJavaElement javaModel = this.getJavaModel();
	JavaElementDelta rootDelta = new JavaElementDelta(javaModel);
	boolean insertedTree = false;
	while (deltas.hasNext()) {
		JavaElementDelta delta = (JavaElementDelta)deltas.next();
		if (DeltaProcessor.VERBOSE) {
			System.out.println(delta.toString());
		}
		IJavaElement element = delta.getElement();
		if (javaModel.equals(element)) {
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int j = 0; j < children.length; j++) {
				JavaElementDelta projectDelta = (JavaElementDelta) children[j];
				rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
				insertedTree = true;
			}
		} else {
			rootDelta.insertDeltaTree(element, delta);
			insertedTree = true;
		}
	}
	if (insertedTree){
		fJavaModelDeltas = new ArrayList(1);
		fJavaModelDeltas.add(rootDelta);
	}
	else {
		fJavaModelDeltas = new ArrayList(0);
	}
}	
	/**
	 *  Returns the info for this element without
	 *  disturbing the cache ordering.
	 */
	protected Object peekAtInfo(IJavaElement element) {
		return this.cache.peekAtInfo(element);
	}
	/**
	 * @see ISaveParticipant
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
	}
	
	protected void putInfo(IJavaElement element, Object info) {
		this.cache.putInfo(element, info);
	}

	/**
	 * Reads the build state for the relevant project.
	 */
	protected Object readState(IProject project) throws CoreException {
		File file = getSerializationFile(project);
		if (file != null && file.exists()) {
			try {
				DataInputStream in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				try {
					String pluginID= in.readUTF();
					if (!pluginID.equals(JavaCore.PLUGIN_ID))
						throw new IOException(Util.bind("build.wrongFileFormat")); //$NON-NLS-1$
					String kind= in.readUTF();
					if (!kind.equals("STATE")) //$NON-NLS-1$
						throw new IOException(Util.bind("build.wrongFileFormat")); //$NON-NLS-1$
					if (in.readBoolean())
						return JavaBuilder.readState(in);
					if (JavaBuilder.DEBUG)
						System.out.println("Saved state thinks last build failed for " + project.getName()); //$NON-NLS-1$
				} finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR, "Error reading last build state for project "+ project.getName(), e)); //$NON-NLS-1$
			}
		}
		return null;
	}
	public void readVariables(String xmlString) throws IOException {
		
		StringReader reader = new StringReader(xmlString);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch(SAXException e) {
			return;
		} catch(ParserConfigurationException e){
			return;
		} finally {
			reader.close();
		}
		if (cpElement == null) return;
		if (!cpElement.getNodeName().equalsIgnoreCase("variables")) { //$NON-NLS-1$
			return;
		}
		ArrayList variableNamesList = new ArrayList();
		ArrayList variablePathsList = new ArrayList();
		
		NodeList list= cpElement.getChildNodes();
		int length= list.getLength();
		for (int i= 0; i < length; ++i) {
			Node node= list.item(i);
			short type= node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (element.getNodeName().equalsIgnoreCase("variable")) { //$NON-NLS-1$
					Variables.put( 
						element.getAttribute("name"), //$NON-NLS-1$
						new Path(element.getAttribute("path"))); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * Registers the given delta with this manager. This API is to be
	 * used to registerd deltas that are created explicitly by the Java
	 * Model. Deltas created as translations of <code>IResourceDeltas</code>
	 * are to be registered with <code>#registerResourceDelta</code>.
	 */
	protected void registerJavaModelDelta(IJavaElementDelta delta) {
		fJavaModelDeltas.add(delta);
	}
	/**
	 * Remembers the given scope in a weak set
	 * (so no need to remove it: it will be removed by the garbage collector)
	 */
	public void rememberScope(AbstractSearchScope scope) {
		// NB: The value has to be null so as to not create a strong reference on the scope
		this.scopes.put(scope, null); 
	}

	/**
	 * removeElementChangedListener method comment.
	 */
	public void removeElementChangedListener(IElementChangedListener listener) {
		
		for (int i = 0; i < this.elementChangedListenerCount; i++){
			
			if (this.elementChangedListeners[i].equals(listener)){
				
				// need to clone defensively since we might be in the middle of listener notifications (#fire)
				int length = this.elementChangedListeners.length;
				IElementChangedListener[] newListeners = new IElementChangedListener[length];
				System.arraycopy(this.elementChangedListeners, 0, newListeners, 0, i);
				int[] newMasks = new int[length];
				System.arraycopy(this.elementChangedListenerMasks, 0, newMasks, 0, i);
				
				// copy trailing listeners
				int trailingLength = this.elementChangedListenerCount - i - 1;
				if (trailingLength > 0){
					System.arraycopy(this.elementChangedListeners, i+1, newListeners, i, trailingLength);
					System.arraycopy(this.elementChangedListenerMasks, i+1, newMasks, i, trailingLength);
				}
				
				// update manager listener state (#fire need to iterate over original listeners through a local variable to hold onto
				// the original ones)
				this.elementChangedListeners = newListeners;
				this.elementChangedListenerMasks = newMasks;
				this.elementChangedListenerCount--;
				return;
			}
		}
	}
	
	protected void removeInfo(IJavaElement element) {
		this.cache.removeInfo(element);
	}
	void removePerProjectInfo(JavaProject javaProject) {
		IProject project = javaProject.getProject();
		PerProjectInfo info= (PerProjectInfo) perProjectInfo.get(project);
		if (info != null) {
			perProjectInfo.remove(project);
		}
	}
	/**
	 * Notifies this Java Model Manager that some resource changes have happened
	 * on the platform, and that the Java Model should update any required
	 * internal structures such that its elements remain consistent.
	 * Translates <code>IResourceDeltas</code> into <code>IJavaElementDeltas</code>.
	 *
	 * @see IResourceDelta
	 * @see IResource 
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		if (event.getSource() instanceof IWorkspace) {
			IResource resource = event.getResource();
			IResourceDelta delta = event.getDelta();
			
			switch(event.getType()){
				case IResourceChangeEvent.PRE_DELETE :
					try {
						if(resource.getType() == IResource.PROJECT 
							&& ((IProject) resource).hasNature(JavaCore.NATURE_ID)) {
							this.deleting((IProject)resource);
						}
					} catch(CoreException e){
					}
					return;
					
				case IResourceChangeEvent.PRE_AUTO_BUILD :
					if(delta != null) {
						this.checkProjectBeingAdded(delta);
						DeltaProcessor.performPreBuildCheck(delta, null); // will close project if affected by the property file change
					}
					// only fire already computed deltas (resource ones will be processed in post change only)
					fire(null, ElementChangedEvent.PRE_AUTO_BUILD);
					break;
					
				case IResourceChangeEvent.POST_CHANGE :
					if (delta != null) {
						try {
							IJavaElementDelta[] translatedDeltas = fDeltaProcessor.processResourceDelta(delta, ElementChangedEvent.POST_CHANGE);
							if (translatedDeltas.length > 0) {
								for (int i= 0; i < translatedDeltas.length; i++) {
									registerJavaModelDelta(translatedDeltas[i]);
								}
							}
							fire(null, ElementChangedEvent.POST_CHANGE);
						} finally {
							// fix for 1FWIAEQ: ITPJCORE:ALL - CRITICAL - "projects being deleted" cache not cleaned up when solution deleted
							if (!fProjectsBeingDeleted.isEmpty()) {
								fProjectsBeingDeleted= new ArrayList(1);
							}
						}			
					}		
			}
		}
	}
	
/**
 * @see ISaveParticipant
 */
public void rollback(ISaveContext context){
}
	/**
	 * Runs a Java Model Operation
	 */
	public void runOperation(JavaModelOperation operation, IProgressMonitor monitor) throws JavaModelException {
		boolean hadAwaitingDeltas = !fJavaModelDeltas.isEmpty();		try {
			if (operation.isReadOnly()) {
				operation.run(monitor);
			} else {
				// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
				this.getJavaModel().getWorkspace().run(operation, monitor);
			}
		} catch (CoreException ce) {
			if (ce instanceof JavaModelException) {
				throw (JavaModelException)ce;
			} else {
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e= ce.getStatus().getException();
					if (e instanceof JavaModelException) {
						throw (JavaModelException) e;
					}
				}
				throw new JavaModelException(ce);
			}
		} finally {
			// fire only if there were no awaiting deltas (if there were, they would come from a resource modifying operation)
			// and the operation has not modified any resource
			if (!hadAwaitingDeltas && !operation.hasModifiedResource()) {
				fire(null, JavaModelManager.DEFAULT_CHANGE_EVENT);
			} // else deltas are fired while processing the resource delta
		}
	}
	private void saveBuildState() throws CoreException {
		ArrayList vStats= null; // lazy initialized
		for (Iterator iter =  perProjectInfo.values().iterator(); iter.hasNext();) {
			try {
				PerProjectInfo info = (PerProjectInfo) iter.next();
				if (info.triedRead)
					saveState(info);
			} catch (CoreException e) {
				if (vStats == null)
					vStats= new ArrayList();
				vStats.add(e.getStatus());
			}
		}
		if (vStats != null) {
			IStatus[] stats= new IStatus[vStats.size()];
			vStats.toArray(stats);
			throw new CoreException(new MultiStatus(JavaCore.PLUGIN_ID, IStatus.ERROR, stats, Util.bind("build.cannotSaveStates"), null)); //$NON-NLS-1$
		}
	}
	/**
	 * Saves the built state for the project.
	 */
	private void saveState(PerProjectInfo info) throws CoreException {
		if (JavaBuilder.DEBUG)
			System.out.println(Util.bind("build.saveStateProgress", info.project.getName())); //$NON-NLS-1$
		File file = getSerializationFile(info.project);
		if (file == null) return;
		long t = System.currentTimeMillis();
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			try {
				out.writeUTF(JavaCore.PLUGIN_ID);
				out.writeUTF("STATE"); //$NON-NLS-1$
				if (info.savedState == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean(true);
					JavaBuilder.writeState(info.savedState, out);
				}
			} finally {
				out.close();
			}
		} catch (RuntimeException e) {
			try {file.delete();} catch(SecurityException se) {}
			throw new CoreException(
				new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
					Util.bind("build.cannotSaveState", info.project.getName()), e)); //$NON-NLS-1$
		} catch (IOException e) {
			try {file.delete();} catch(SecurityException se) {}
			throw new CoreException(
				new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
					Util.bind("build.cannotSaveState", info.project.getName()), e)); //$NON-NLS-1$
		}
		if (JavaBuilder.DEBUG) {
			t = System.currentTimeMillis() - t;
			System.out.println(Util.bind("build.saveStateComplete", String.valueOf(t))); //$NON-NLS-1$
		}
	}
	public void saveVariables() throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(
			new QualifiedName(JavaCore.PLUGIN_ID, "variables"),  //$NON-NLS-1$
			getVariablesAsXMLString());
	}
	
/**
 * @see ISaveParticipant
 */
public void saving(ISaveContext context) throws CoreException {

	this.saveVariables();
	
	if (context.getKind() == ISaveContext.FULL_SAVE){
		this.saveBuildState();	// build state
	}
}
	/**
	 * Record the order in which to build the java projects (batch build). This order is based
	 * on the projects classpath settings.
	 */
	protected void setBuildOrder(String[] javaBuildOrder) throws JavaModelException {
		// optional behaviour
		// possible value of index 0 is Compute
		if (!"compute".equals(JavaCore.getOptions().get("org.eclipse.jdt.core.computeJavaBuildOrder"))) return; //$NON-NLS-1$ //$NON-NLS-2$
		
		if (javaBuildOrder == null || javaBuildOrder.length <= 1) return;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		String[] wksBuildOrder = description.getBuildOrder();

		String[] newOrder;
		if (wksBuildOrder == null){
			newOrder = javaBuildOrder;
		} else {
			// remove projects which are already mentionned in java builder order
			int javaCount = javaBuildOrder.length;
			HashMap newSet = new HashMap(javaCount); // create a set for fast check
			for (int i = 0; i < javaCount; i++){
				newSet.put(javaBuildOrder[i], javaBuildOrder[i]);
			}
			int removed = 0;
			int oldCount = wksBuildOrder.length;
			for (int i = 0; i < oldCount; i++){
				if (newSet.containsKey(wksBuildOrder[i])){
					wksBuildOrder[i] = null;
					removed++;
				}
			}
			// add Java ones first
			newOrder = new String[oldCount - removed + javaCount];
			System.arraycopy(javaBuildOrder, 0, newOrder, 0, javaCount); // java projects are built first

			// copy previous items in their respective order
			int index = javaCount;
			for (int i = 0; i < oldCount; i++){
				if (wksBuildOrder[i] != null){
					newOrder[index++] = wksBuildOrder[i];
				}
			}
		}
		// commit the new build order out
		description.setBuildOrder(newOrder);
		try {
			workspace.setDescription(description);
		} catch(CoreException e){
			throw new JavaModelException(e);
		}
	}
	/**
	 * Sets the last built state for the given project, or null to reset it.
	 */
	public void setLastBuiltState(IProject project, Object state) {
		PerProjectInfo info = getPerProjectInfo(project);
		info.triedRead = true; // no point trying to re-read once using setter
		info.savedState = state;
	}
	public void shutdown () {
		if (fDeltaProcessor.indexManager != null){ // no more indexing
			fDeltaProcessor.indexManager.shutdown();
		}
		try {
			IJavaModel model = this.getJavaModel();
			if (model != null) {
				model.close();
			}
		} catch (JavaModelException e) {
		}
	}
	/**
	 * Turns the firing mode to on. That is, deltas that are/have been
	 * registered will be fired.
	 */
	public void startDeltas() {
		fFire= true;
	}
	/**
	 * Turns the firing mode to off. That is, deltas that are/have been
	 * registered will not be fired until deltas are started again.
	 */
	public void stopDeltas() {
		fFire= false;
	}
	
}
