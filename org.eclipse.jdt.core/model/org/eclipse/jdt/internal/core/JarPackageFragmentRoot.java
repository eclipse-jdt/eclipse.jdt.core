package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * A package fragment root that corresponds to a .jar or .zip.
 *
 * <p>NOTE: The only visible entries from a .jar or .zip package fragment root
 * are .class files.
 * <p>NOTE: A jar package fragment root may or may not have an associated resource.
 *
 * @see IPackageFragmentRoot
 * @see JarPackageFragmentRootInfo
 */
public class JarPackageFragmentRoot extends PackageFragmentRoot {
	/**
	 * The absolute path to the jar file.
	 */
	protected IPath fJarPath= null;

	/**
	 * The delimiter between the zip path and source path in the
	 * attachment server property.
	 */
	protected final static char ATTACHMENT_PROPERTY_DELIMITER= '*';

	/**
	 * The name of the meta-inf directory not to be included as a 
	 * jar package fragment.
	 * @see #computeJarChildren
	 */
	//protected final static String META_INF_NAME = "META-INF/";
	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file that is not contained in a <code>IJavaProject</code> and
	 * does not have an associated <code>IResource</code>.
	 */
	protected JarPackageFragmentRoot(String jarPath, IJavaProject project) {
		super(null, project, jarPath);
		fJarPath= new Path(jarPath);
	}
	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file.
	 */
	protected JarPackageFragmentRoot(IResource resource, IJavaProject project) {
		super(resource, project);
		fJarPath= resource.getFullPath();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public void attachSource(IPath zipPath, IPath rootPath, IProgressMonitor monitor) throws JavaModelException {
		QualifiedName qName= getSourceAttachmentPropertyName();
		try {
			verifyAttachSource(zipPath);
			if (monitor != null) {
				monitor.beginTask("Attaching source...", 2);
			}
			SourceMapper mapper= null;
			SourceMapper oldMapper= getSourceMapper();
			IWorkspace workspace= getJavaModel().getWorkspace();
			boolean rootNeedsToBeClosed= false;

			if (zipPath == null) {
				//source being detached
				rootNeedsToBeClosed= true;
			/* Disable deltas (see 1GDTUSD)
				// fire a delta to notify the UI about the source detachement.
				JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
				JavaModel model = (JavaModel) getJavaModel();
				JavaElementDelta attachedSourceDelta = new JavaElementDelta(model);
				attachedSourceDelta .sourceDetached(this); // this would be a JarPackageFragmentRoot
				manager.registerResourceDelta(attachedSourceDelta );
				manager.fire(); // maybe you want to fire the change later. Let us know about it.
			*/
			} else {
			/*
				// fire a delta to notify the UI about the source attachement.
				JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
				JavaModel model = (JavaModel) getJavaModel();
				JavaElementDelta attachedSourceDelta = new JavaElementDelta(model);
				attachedSourceDelta .sourceAttached(this); // this would be a JarPackageFragmentRoot
				manager.registerResourceDelta(attachedSourceDelta );
				manager.fire(); // maybe you want to fire the change later. Let us know about it.
			 */
				String rootPathString= null;
				if (rootPath == null) {
					rootPath= new Path(IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH);
				}

				//check if different from the current attachment
				IPath storedZipPath= getSourceAttachmentPath();
				IPath storedRootPath= getSourceAttachmentRootPath();
				if (monitor != null) {
					monitor.worked(1);
				}
				if (storedZipPath != null) {
					if (!(storedZipPath.equals(zipPath) && rootPath.equals(storedRootPath))) {
						rootNeedsToBeClosed= true;
					}
				}
				if ((zipPath.isAbsolute() && workspace.getRoot().findMember(zipPath) != null) || !zipPath.isAbsolute()) {
					// internal to the workbench
					// a resource
					IResource zipFile= workspace.getRoot().findMember(zipPath);
					if (zipFile == null) {
						if (monitor != null) {
							monitor.done();
						}
						throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, zipPath));
					}
					if (!(zipFile.getType() == IResource.FILE)) {
						if (monitor != null) {
							monitor.done();
						}
						throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, zipPath));
					}
				}
				mapper= new SourceMapper(zipPath, rootPath.toOSString(), (JavaModel) getJavaModel());
			}
			setSourceMapper(mapper);
			if (zipPath == null) {
				//remove the property
				getWorkspace().getRoot().setPersistentProperty(qName, null);
			} else {
				//set the property to the path of the mapped zip
				getWorkspace().getRoot().setPersistentProperty(qName, zipPath.toString() + ATTACHMENT_PROPERTY_DELIMITER + rootPath.toString());
			}
			if (rootNeedsToBeClosed) {
				if (oldMapper != null) {
					oldMapper.close();
				}
				IBufferManager manager= BufferManager.getDefaultBufferManager();
				Enumeration openBuffers= manager.getOpenBuffers();
				while (openBuffers.hasMoreElements()) {
					IBuffer buffer= (IBuffer) openBuffers.nextElement();
					IOpenable possibleJarMember= buffer.getOwner();
					if (isAncestorOf((IJavaElement) possibleJarMember)) {
						buffer.close();
					}
				}
				if (monitor != null) {
					monitor.worked(1);
				}
			}
		} catch (JavaModelException e) {
			try {
				getWorkspace().getRoot().setPersistentProperty(qName, null); // loose info - will be recomputed
			} catch(CoreException ce){
			}
			throw e;
		} catch (CoreException rae) {
			try {
				getWorkspace().getRoot().setPersistentProperty(qName, null); // loose info - will be recomputed
			} catch(CoreException ce){
			}
			throw new JavaModelException(rae);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
	/**
	 * Close the associated JAR file stored in the info of this element. If
	 * this jar has an associated ZIP source attachment, close it too.
	 *
	 * @see IOpenable
	 */
	protected void closing(Object info) throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			mapper.close();
		}
		super.closing(info);
	}
	/**
	 * Compute the package fragment children of this package fragment root.
	 * These are all of the directory zip entries, and any directories implied
	 * by the path of class files contained in the jar of this package fragment root.
	 * Has the side effect of opening the package fragment children.
	 */
	protected boolean computeChildren(OpenableElementInfo info) throws JavaModelException {
		Vector vChildren= new Vector();
		computeJarChildren((JarPackageFragmentRootInfo) info, vChildren);
		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.copyInto(children);
		info.setChildren(children);
		return true;
	}
/**
 * Determine all of the package fragments associated with this package fragment root.
 * Cache the zip entries for each package fragment in the info for the package fragment.
 * The package fragment children are all opened.
 * Add all of the package fragments to vChildren.
 *
 * @exception JavaModelException The resource (the jar) associated with this package fragment root does not exist
 */
protected void computeJarChildren(JarPackageFragmentRootInfo info, Vector vChildren) throws JavaModelException {
	ZipFile jar= null;
	try {
		jar= getJar();
		Hashtable packageFragToTypes= new Hashtable();

		// always create the default package
		packageFragToTypes.put(IPackageFragment.DEFAULT_PACKAGE_NAME, new Vector[] { new Vector(), new Vector()
		});

		Vector[] temp;
		for (Enumeration e= jar.entries(); e.hasMoreElements();) {
			ZipEntry member= (ZipEntry) e.nextElement();
			String eName= member.getName();
			if (member.isDirectory()) {
				eName= eName.substring(0, eName.length() - 1);
				eName= eName.replace('/', '.');
				temp= (Vector[]) packageFragToTypes.get(eName);
				if (temp == null) {
					temp= new Vector[] { new Vector(), new Vector()
				 };
					packageFragToTypes.put(eName, temp);
				}
			} else {
				if (Util.isClassFileName(eName)) {
					//only interested in class files
					//store the class file entry name to be cached in the appropriate package fragment
					//zip entries only use '/'
					Vector classTemp;
					int lastSeparator= eName.lastIndexOf('/');
					String key= IPackageFragment.DEFAULT_PACKAGE_NAME;
					String value= eName;
					if (lastSeparator != -1) {
						//not in the default package
						eName= eName.replace('/', '.');
						value= eName.substring(lastSeparator + 1);
						key= eName.substring(0, lastSeparator);
					}
					temp= (Vector[]) packageFragToTypes.get(key);
					if (temp == null) {
						// build all package fragments in the key
						lastSeparator= key.indexOf('.');
						while (lastSeparator > 0) {
							String prefix= key.substring(0, lastSeparator);
							if (packageFragToTypes.get(prefix) == null) {
								packageFragToTypes.put(prefix, new Vector[] { new Vector(), new Vector()
							 });
							}
							lastSeparator= key.indexOf('.', lastSeparator + 1);
						}
						classTemp= new Vector();
						classTemp.addElement(value);
						packageFragToTypes.put(key, new Vector[] {classTemp, new Vector()
					 });
					} else {
						classTemp= temp[0];
						classTemp.addElement(value);
					}
				} else {
					Vector resTemp;
					int lastSeparator= eName.lastIndexOf('/');
					String key= IPackageFragment.DEFAULT_PACKAGE_NAME;
					String value= eName;
					if (lastSeparator != -1) {
						//not in the default package
						eName= eName.replace('/', '.');
						key= eName.substring(0, lastSeparator);
					}
					temp= (Vector[]) packageFragToTypes.get(key);
					if (temp == null) {
						// build all package fragments in the key
						lastSeparator= key.indexOf('.');
						while (lastSeparator > 0) {
							String prefix= key.substring(0, lastSeparator);
							if (packageFragToTypes.get(prefix) == null) {
								packageFragToTypes.put(prefix, new Vector[] { new Vector(), new Vector()
							 });
							}
							lastSeparator= key.indexOf('.', lastSeparator + 1);
						}
						resTemp= new Vector();
						resTemp.addElement(value);
						packageFragToTypes.put(key, new Vector[] { new Vector(), resTemp });
					} else {
						resTemp= temp[1];
						resTemp.addElement(value);
					}
				}
			}
		}
		//loop through all of referenced packages, creating package fragments if necessary
		// and cache the entry names in the infos created for those package fragments
		Enumeration packages= packageFragToTypes.keys();
		while (packages.hasMoreElements()) {
			String packName= (String) packages.nextElement();
			Vector[] entries= (Vector[]) packageFragToTypes.get(packName);
			JarPackageFragment packFrag= (JarPackageFragment) getPackageFragment(packName);
			JarPackageFragmentInfo fragInfo= (JarPackageFragmentInfo) packFrag.createElementInfo();
			fragInfo.setEntryNames(entries[0]);
			int resLength= entries[1].size();
			if (resLength == 0) {
				packFrag.computeNonJavaResources(new String[] {}, fragInfo, jar.getName());
			} else {
				String[] resNames= new String[resLength];
				entries[1].copyInto(resNames);
				packFrag.computeNonJavaResources(resNames, fragInfo, jar.getName());
			}
			packFrag.computeChildren(fragInfo);
			fgJavaModelManager.putInfo(packFrag, fragInfo);
			vChildren.addElement(packFrag);
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	} finally {
		if (jar != null) {
			try {
				jar.close();
			} catch (IOException e) {
				// ignore 
			}
		}
	}
}
	/**
	 * Returns a new element info for this element.
	 */
	protected OpenableElementInfo createElementInfo() {
		return new JarPackageFragmentRootInfo();
	}
	/**
	 * A Jar is always K_BINARY.
	 *
	 * @exception NotPresentException if the project and root do
	 *      not exist.
	 */
	protected int determineKind(IResource underlyingResource) throws JavaModelException {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns true if this handle represents the same jar
	 * as the given handle. Two jars are equal if they share
	 * the same zip file.
	 *
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof JarPackageFragmentRoot) {
			JarPackageFragmentRoot other= (JarPackageFragmentRoot) o;
			return fJarPath.equals(other.fJarPath);
		}
		return false;
	}
public IClasspathEntry findSourceAttachmentRecommendation() {

	try {

		IPath rootPath = this.getPath();
		IClasspathEntry entry;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		// try on enclosing project first
		JavaProject parentProject = (JavaProject) getJavaProject();
		try {
			entry = parentProject.getClasspathEntryFor(rootPath);
			if (entry != null){
				Object target = JavaModel.getTarget(workspaceRoot, entry.getSourceAttachmentPath(), true);
				if (target instanceof IFile){
					IFile file = (IFile) target;
					if ("jar".equalsIgnoreCase(file.getFileExtension()) || "zip".equalsIgnoreCase(file.getFileExtension())){
						return entry;
					}
				}
				if (target instanceof java.io.File){
					java.io.File file = (java.io.File) target;
					String name = file.getName().toLowerCase();
					if (name.endsWith(".jar") || name.endsWith(".zip")){
						return entry;
					}
				}
			}
		} catch(JavaModelException e){
		}
		
		// iterate over all projects
		IJavaModel model = getJavaModel();
		IJavaProject[] jProjects = model.getJavaProjects();
		for (int i = 0, max = jProjects.length; i < max; i++){
			JavaProject jProject = (JavaProject) jProjects[i];
			if (jProject == parentProject) continue; // already done
			try {
				entry = jProject.getClasspathEntryFor(rootPath);
				if (entry != null){
					Object target = JavaModel.getTarget(workspaceRoot, entry.getSourceAttachmentPath(), true);
					if (target instanceof IFile){
						IFile file = (IFile) target;
						if ("jar".equalsIgnoreCase(file.getFileExtension()) || "zip".equalsIgnoreCase(file.getFileExtension())){
							return entry;
						}
					}
					if (target instanceof java.io.File){
						java.io.File file = (java.io.File) target;
						String name = file.getName().toLowerCase();
						if (name.endsWith(".jar") || name.endsWith(".zip")){
							return entry;
						}
					}
				}
			} catch(JavaModelException e){
			}
		}
	} catch(JavaModelException e){
	}

	return null;
}
	/**
	 * Returns the underlying ZipFile for this Jar package fragment root.
	 *
	 * @exception CoreException if an error occurs accessing the jar
	 */
	public ZipFile getJar() throws CoreException {
		return fgJavaModelManager.getZipFile(getPath());
	}
	/**
	 * @see IJavaElement
	 */
	public IJavaProject getJavaProject() {
		IJavaElement parent= getParent();
		if (parent == null) {
			return null;
		} else {
			return parent.getJavaProject();
		}
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public int getKind() {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {
		// We want to show non java resources of the default package at the root (see PR #1G58NB8)
		return ((JarPackageFragment) this.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME)).storedNonJavaResources();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPackageFragment getPackageFragment(String packageName) {

		return new JarPackageFragment(this, packageName);
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getPath() {
		if (fResource == null) {
			return fJarPath;
		} else {
			return super.getPath();
		}
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getSourceAttachmentPath() throws JavaModelException {
		String serverPathString= getSourceAttachmentProperty();
		if (serverPathString == null) {
			return null;
		}
		int index= serverPathString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER);
		if (index < 0) return null;
		String serverZipPathString= serverPathString.substring(0, index);
		return new Path(serverZipPathString);
	}
	/**
	 * Returns the server property for this package fragment root's
	 * source attachement.
	 */
	protected String getSourceAttachmentProperty() throws JavaModelException {
		String propertyString = null;
		QualifiedName qName= getSourceAttachmentPropertyName();
		try {
			propertyString = getWorkspace().getRoot().getPersistentProperty(qName);
			
			// if no existing source attachment information, then lookup a recommendation from classpath entries
			if (propertyString == null || propertyString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER) < 0){
				IClasspathEntry recommendation = findSourceAttachmentRecommendation();
				if (recommendation != null){
					propertyString = recommendation.getSourceAttachmentPath().toString() 
										+ ATTACHMENT_PROPERTY_DELIMITER 
										+ (recommendation.getSourceAttachmentRootPath() == null ? "" : recommendation.getSourceAttachmentRootPath().toString());
					getWorkspace().getRoot().setPersistentProperty(qName, propertyString);
				}
			}
			return propertyString;
		} catch (CoreException ce) {
			throw new JavaModelException(ce);
		}
	}
	/**
	 * Returns the qualified name for the source attachment property
	 * of this jar.
	 */
	protected QualifiedName getSourceAttachmentPropertyName() throws JavaModelException {
		ZipFile jarFile = null;
		try {
			jarFile = getJar();
			return new QualifiedName(JavaCore.PLUGIN_ID, "sourceattachment: " + jarFile.getName());
		} catch (CoreException e) {
			throw new JavaModelException(e);
		} finally {
			try {
				if (jarFile != null) {
					jarFile.close();
				}
			} catch(IOException e) {
				// ignore 
			}
		}
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getSourceAttachmentRootPath() throws JavaModelException {
		String serverPathString= getSourceAttachmentProperty();
		if (serverPathString == null) {
			return null;
		}
		int index= serverPathString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER);
		String serverRootPathString= IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
		if (index != serverPathString.length() - 1) {
			serverRootPathString= serverPathString.substring(index + 1);
		}
		return new Path(serverRootPathString);
	}
	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {
		try {
			return ((JarPackageFragmentRootInfo) getElementInfo()).getSourceMapper();
		} catch (JavaModelException e) {
			return null;
		}
	}
	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		if (fResource == null) {
			return null;
		} else {
			return super.getUnderlyingResource();
		}
	}
	/**
	 * If I am not open, return true to avoid parsing.
	 *
	 * @see IParent 
	 */
	public boolean hasChildren() throws JavaModelException {
		if (isOpen()) {
			return getChildren().length > 0;
		} else {
			return true;
		}
	}
	public int hashCode() {
		return fJarPath.hashCode();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isArchive() {
		return true;
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isExternal() {
		return fResource == null;
	}
	/**
	 * Jars and jar entries are all read only
	 */
	public boolean isReadOnly() {
		return true;
	}
	/**
	 * @see Openable#openWhenClosed()
	 */
	protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {
		super.openWhenClosed(pm);
		try {
			//restore any stored attached source zip
			IPath zipPath= getSourceAttachmentPath();
			if (zipPath != null) {
				IPath rootPath= getSourceAttachmentRootPath();
				attachSource(zipPath, rootPath, pm);
			}
		} catch(JavaModelException e){ // no attached source
		}
	}
	/**
	 * An archive cannot refresh its children.
	 */
	public void refreshChildren() {
		// do nothing
	}
	/**
	 * Reset the array of non-java resources contained in the receiver to null.
	 */
	public void resetNonJavaResources() throws JavaModelException {
		((JarPackageFragmentRootInfo) getElementInfo()).setNonJavaResources(null);
	}
	/**
	 * @private - for use by <code>AttachSourceOperation</code> only.
	 * Sets the source mapper associated with this jar.
	 */
	public void setSourceMapper(SourceMapper mapper) throws JavaModelException {
		((JarPackageFragmentRootInfo) getElementInfo()).setSourceMapper(mapper);
	}
	/**
	 * Possible failures: <ul>
	 *  <li>RELATIVE_PATH - the path supplied to this operation must be
	 *      an absolute path
	 *  <li>ELEMENT_NOT_PRESENT - the jar supplied to the operation
	 *      does not exist
	 * </ul>
	 */
	protected void verifyAttachSource(IPath zipPath) throws JavaModelException {
		IJavaModelStatus status= null;
		if (!exists0()) {
			throw newNotPresentException();
		} else if (zipPath != null && !zipPath.isAbsolute()) {
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, zipPath));
		}
	}

/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	StringBuffer buff= new StringBuffer(((JavaElement)getParent()).getHandleMemento());
	buff.append(getHandleMementoDelimiter());
	buff.append(this.fJarPath.toString()); // 1GEP51U
	return buff.toString();
}
}
