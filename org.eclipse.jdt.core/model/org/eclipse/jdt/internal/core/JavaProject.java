package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.eval.EvaluationContext;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Handle for a Java Project.
 *
 * <p>A Java Project internally maintains a devpath that corresponds
 * to the project's classpath. The classpath may include source folders
 * from the current project; jars in the current project, other projects,
 * and the local file system; and binary folders (output location) of other
 * projects. The Java Model presents source elements corresponding to output
 * .class files in other projects, and thus uses the devpath rather than
 * the classpath (which is really a compilation path). The devpath mimics
 * the classpath, except has source folder entries in place of output
 * locations in external projects.
 *
 * <p>Each JavaProject has an INameLookup facility that locates elements
 * on by name, based on the devpath.
 *
 * @see IJavaProject
 */
public class JavaProject extends Openable implements IJavaProject, IProjectNature {
	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES= new String[0];

	/**
	 * The platform project this <code>IJavaProject</code> is based on
	 */
	protected IProject fProject;

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and <code>IProject.addNature()</code>.
	 *
	 * @see #setProject
	 */
	public JavaProject() {
		super(JAVA_PROJECT, null, null);
	}
	public JavaProject(IProject project, IJavaElement parent) {
		super(JAVA_PROJECT, parent, project.getName());
		fProject= project;
	}
	/**
	 * Adds a builder to the build spec for the given project.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException {
		
		IProjectDescription description= getProject().getDescription();
		ICommand javaCommand = getJavaCommand(description);
		
		if (javaCommand == null) {

			// Add a Java command to the build spec
			ICommand command= description.newCommand();
			command.setBuilderName(builderID);
			setJavaCommand(description, command);
		}
	}
/**
 * Returns a canonicalized path from the given external path.
 * Note that the return path contains the same number of segments
 * and it contains a device only if the given path contained one.
 * @see java.io.File for the definition of a canonicalized path
 */
public static IPath canonicalizedPath(IPath externalPath) {
	if (externalPath == null) return null;

	// if not external path, return original path
	if (ResourcesPlugin.getWorkspace().getRoot().findMember(externalPath) != null) {
		return externalPath;
	}
	
	IPath canonicalPath = null;
	try {
		canonicalPath  = new Path(new File(externalPath.toOSString()).getCanonicalPath());
	} catch (IOException e) {
		// default to original path
		return externalPath;
	}
	// keep only segments that were in original path and device if it was there
	IPath result = canonicalPath.removeFirstSegments(canonicalPath.segmentCount() - externalPath.segmentCount());
	if (externalPath.getDevice() == null) {
		return result.setDevice(null);
	} else {
		return result;
	}
}
	/**
	 * Compute the file name to use for a given shared property
	 */
	public String computeSharedPropertyFileName(QualifiedName qName){
		return /*'.' + qName.getQualifier() + */ '.' + qName.getLocalName();
	}
	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException {
		// register Java builder
		addToBuildSpec(JavaCore.BUILDER_ID);

		// notify Java delta (Java project added) 
		JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
		JavaModel model = (JavaModel) getJavaModel();
		JavaElementDelta projectDelta = new JavaElementDelta(model);
		projectDelta.added(this);
		JavaElementInfo jmi= model.getElementInfo();
		jmi.addChild(this);
		manager.registerResourceDelta(projectDelta);
		manager.fire();
	}
	/**
	 * Create's a classpath entry of the specified kind.
	 *
	 * Returns null if unable to create a valid entry.
	 */
	protected IClasspathEntry createClasspathEntry(IPath path, int kind, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath) {
		switch(kind){

			case IClasspathEntry.CPE_PROJECT :
				if (!path.isAbsolute()) return null;
				return JavaCore.newProjectEntry(path);

			case IClasspathEntry.CPE_LIBRARY :
				if (!path.isAbsolute()) return null;
				return JavaCore.newLibraryEntry(path, sourceAttachmentPath, sourceAttachmentRootPath);

			case IClasspathEntry.CPE_SOURCE :
				if (!path.isAbsolute()) return null;
				// must be an entry in this project or specify another project
				// change zrh
				String projSegment= path.segment(0);
				if (projSegment != null && projSegment.equals(getElementName())) {
					// this project
					return JavaCore.newSourceEntry(path);
				} else {
					// another project
					return JavaCore.newProjectEntry(path);
				}
 
			case IClasspathEntry.CPE_VARIABLE :
				return JavaCore.newVariableEntry(path, sourceAttachmentPath, sourceAttachmentRootPath);
 
			case ClasspathEntry.K_OUTPUT :
				if (!path.isAbsolute()) return null;
				return new ClasspathEntry(ClasspathEntry.K_OUTPUT, IClasspathEntry.CPE_LIBRARY, path, null, null);
				
			default:
				return null;
		}
	}
	/**
	 * Returns a new element info for this element.
	 */
	protected OpenableElementInfo createElementInfo() {
		return new JavaProjectElementInfo();
	}
	/**
	 * Removes the Java nature from the project.
	 */
	public void deconfigure() throws CoreException {

		// deregister Java builder
		removeFromBuildSpec(JavaCore.BUILDER_ID);
	}
	/**
	 * Returns a default class path.
	 * This is the root of the project
	 */
	protected IClasspathEntry[] defaultClasspath() throws JavaModelException {
		return new IClasspathEntry[] {JavaCore.newSourceEntry(getProject().getFullPath())};
	}
	/**
	 * Returns a default output location.
	 * This is the project bin folder
	 */
	protected IPath defaultOutputLocation() throws JavaModelException {
		return getProject().getFullPath().append("bin");
	}
	/**
	 * Returns true if this handle represents the same Java project
	 * as the given handle. Two handles represent the same
	 * project if they are identical or if they represent a project with 
	 * the same underlying resource and occurrence counts.
	 *
	 * @see JavaElement#equals
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof JavaProject))
			return false;
		JavaProject other= (JavaProject) o;
		return getProject().equals(other.getProject()) && fOccurrenceCount == other.fOccurrenceCount;
	}
	/**
	 * @see IJavaProject
	 */
	public IJavaElement findElement(IPath path) throws JavaModelException {
		if (path == null || path.isAbsolute()) {
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, path));
		}
		try {
			String extension= path.getFileExtension();
			if (extension == null) {
				String packageName= path.toString().replace(IPath.SEPARATOR, '.');
				IPackageFragment[] pkgFragments = getNameLookup().findPackageFragments(packageName, false);
				if (pkgFragments == null) {
					return null;
				} else {
					// try to return one that is a child of this project
					for (int i = 0, length = pkgFragments.length; i < length; i++) {
						IPackageFragment pkgFragment = pkgFragments[i];
						if (this.equals(pkgFragment.getParent().getParent())) {
							return pkgFragment;
						}
					}
					// default to the first one
					return pkgFragments[0];
				}
			} else if (extension.equalsIgnoreCase("java") || extension.equalsIgnoreCase("class")) {
				IPath packagePath= path.removeLastSegments(1);
				String packageName= packagePath.toString().replace(IPath.SEPARATOR, '.');
				String typeName= path.lastSegment();
				typeName= typeName.substring(0, typeName.length() - extension.length() - 1);
				String qualifiedName= null;
				if (packageName.length() > 0) {
					qualifiedName= packageName + "." + typeName;
				} else {
					qualifiedName= typeName;
				}
				IType type= getNameLookup().findType(qualifiedName, false, INameLookup.ACCEPT_CLASSES | INameLookup.ACCEPT_INTERFACES);
				if (type != null) {
					return type.getParent();
				} else {
					return null;
				}
			} else {
				// unsupported extension
				return null;
			}
		} catch (JavaModelException e) {
			if (e.getStatus().getCode() == IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				return null;
			} else {
				throw e;
			}
		}
	}
	/**
	 * @see INameLookup
	 */
	public IPackageFragment findPackageFragment(IPath path) throws JavaModelException {
		return getNameLookup().findPackageFragment(this.canonicalizedPath(path));
	}
	/**
	 * @see INameLookup
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path) throws JavaModelException {
		return getNameLookup().findPackageFragmentRoot(this.canonicalizedPath(path));
	}
	/**
	 * @see Openable
	 */
	protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Hashtable newElements, IResource underlyingResource) throws JavaModelException {
		boolean validInfo= false;
		try {
			if (((IProject) getUnderlyingResource()).isOpen()) {
				// put the info now, because setting the classpath requires it
				fgJavaModelManager.putInfo(this, info);

				// read classpath property (contains actual classpath and output location settings)
				boolean needToSaveClasspath = false;
				IPath outputLocation= null;			
				IClasspathEntry[] classpath = null;					

				// read from file
				String sharedClasspath = loadClasspath();
				if (sharedClasspath != null){
					try {
						classpath = readPaths(sharedClasspath);
					} catch (IOException e){
					} catch (RuntimeException e){
					}
					// extract out the output location
					if (classpath != null && classpath.length > 0) {
						IClasspathEntry entry= classpath[classpath.length - 1];
						if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
							outputLocation = entry.getPath();
							IClasspathEntry[] copy= new IClasspathEntry[classpath.length - 1];
							System.arraycopy(classpath, 0, copy, 0, copy.length);
							classpath= copy;
						}
					}
				}
				// restore output location				
				if (outputLocation == null) {
					outputLocation= defaultOutputLocation();
					needToSaveClasspath = true;
				}
				setOutputLocation0(outputLocation);

				// restore classpath
				if (classpath == null) {
					classpath= defaultClasspath();
					needToSaveClasspath = true;
				}
				setRawClasspath0(classpath);

				// need to commit classpath ?				
				//if (needToSaveClasspath) saveClasspath();

				// only valid if reaches here				
				validInfo= true; 
			}
		} catch (JavaModelException e) {
		} finally {
			if (!validInfo)
				fgJavaModelManager.removeInfo(this);
		}
		return validInfo;
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots() throws JavaModelException {
		IJavaElement[] tempChildren= getElementInfo().getChildren();
		IPackageFragmentRoot[] children= new IPackageFragmentRoot[tempChildren.length];
		for (int i= 0; i < tempChildren.length; i++) {
			children[i]= (IPackageFragmentRoot) tempChildren[i];
		}
		return children;
	}
	/**
	 * Returns all package fragments from all of the package fragment roots
	 * on the classpath.
	 */
	public IPackageFragment[] getAllPackageFragments() throws JavaModelException {
		IPackageFragmentRoot[] roots= getAllPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}
	/**
	 * Returns all the <code>IPackageFragmentRoot</code>s the builder needs to
	 * know about in order to build this project. This includes:
	 * <ul>
	 *   <li>the source roots for the current project
	 *   <li>the binary roots (output locations) for the required projects
	 *   <li>the binary roots for any jar/lib used by this project
	 * </li>
	 */
	public IPackageFragmentRoot[] getBuilderRoots(IResourceDelta delta) throws JavaModelException {
		Vector builderRoots= new Vector();
		IClasspathEntry[] classpath;
		classpath= getResolvedClasspath(true);
		IResource res;
		IJavaProject project;
		for (int i= 0; i < classpath.length; i++) {
			IClasspathEntry entry= classpath[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_LIBRARY :
					IPackageFragmentRoot[] roots= this.getPackageFragmentRoots(entry);
					if (roots.length > 0)
						builderRoots.addElement(roots[0]);
					break;
				case IClasspathEntry.CPE_PROJECT :
					// other project contributions are restrained to their binary output
					res= retrieveResource(entry.getPath(), delta);
					if (res != null) {
						project= (IJavaProject) JavaCore.create(res);
						if (project.isOpen()){
							res= retrieveResource(project.getOutputLocation(), delta);
							if (res != null) {
								PackageFragmentRoot root= (PackageFragmentRoot) project.getPackageFragmentRoot(res);
								root.setOccurrenceCount(root.getOccurrenceCount() + 1);
								((PackageFragmentRootInfo) root.getElementInfo()).setRootKind(IPackageFragmentRoot.K_BINARY);
								root.refreshChildren();
								builderRoots.addElement(root);
							}
						}
					}
					break;
				case IClasspathEntry.CPE_SOURCE :
					if (getCorrespondingResource().getFullPath().isPrefixOf(entry.getPath())) {
						res= retrieveResource(entry.getPath(), delta);
						if (res != null)
							builderRoots.addElement(getPackageFragmentRoot(res));
					} else {
						IProject proj= (IProject) getWorkspace().getRoot().findMember(entry.getPath());
						project= (IJavaProject) JavaCore.create(proj);
						if (proj.isOpen()){
							res= retrieveResource(project.getOutputLocation(), delta);
							PackageFragmentRoot root= (PackageFragmentRoot) project.getPackageFragmentRoot(res);
							root.setOccurrenceCount(root.getOccurrenceCount() + 1);
							((PackageFragmentRootInfo) root.getElementInfo()).setRootKind(IPackageFragmentRoot.K_BINARY);
							root.refreshChildren();
							builderRoots.addElement(root);
						}
					}
					break;
			}
		}
		IPackageFragmentRoot[] result= new IPackageFragmentRoot[builderRoots.size()];
		builderRoots.copyInto(result);
		return result;
	}
	/**
	 * @see IParent 
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		return getPackageFragmentRoots();
	}
	/**
	 * @see IJavaProject
	 * @deprecated
	 */
	public IClasspathEntry[] getClasspath() throws JavaModelException {
		return getRawClasspath();
	}
	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected String getClasspathAsXMLString(IClasspathEntry[] classpath, IPath outputLocation) throws JavaModelException {
		Document doc= new DocumentImpl();
		Element cpElement= doc.createElement("classpath");
		doc.appendChild(cpElement);

		for (int i= 0; i < classpath.length; ++i) {
			Element cpeElement= getEntryAsXMLElement(doc, classpath[i], getProject().getFullPath());
			cpElement.appendChild(cpeElement);
		}

		if (outputLocation != null) {
			outputLocation= outputLocation.removeFirstSegments(1);
			outputLocation= outputLocation.makeRelative();
			Element oElement= doc.createElement("classpathentry");
			oElement.setAttribute("kind", kindToString(ClasspathEntry.K_OUTPUT));
			oElement.setAttribute("path", outputLocation.toOSString());
			cpElement.appendChild(oElement);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		try {
			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			Serializer serializer = SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(writer, format);
			serializer.asDOMSerializer().serialize(doc);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		return writer.toString();	
	}
	/**
	 * Returns the classpath entry that refers to the given path
	 * or <code>null</code> if there is no reference to the path.
	 */
	public IClasspathEntry getClasspathEntryFor(IPath path) throws JavaModelException {
		IClasspathEntry[] entries= getResolvedClasspath(true);
		for (int i= 0; i < entries.length; i++) {
			if (entries[i].getPath().equals(path)) {
				return entries[i];
			}
		}
		return null;
	}
	/**
	 * Returns the qualified name for the classpath server property
	 * of this project
	 */
	public QualifiedName getClasspathPropertyName() {
		return new QualifiedName(JavaCore.PLUGIN_ID, "classpath");
	}
	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected static Element getEntryAsXMLElement(Document document, IClasspathEntry entry, IPath prefixPath) throws JavaModelException {
		
		Element element= document.createElement("classpathentry");
		element.setAttribute("kind", kindToString(entry.getEntryKind()));
		IPath path= entry.getPath();
		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE){
			// translate to project relative from absolute (unless a device path)
			if (path.isAbsolute()) {
				if (prefixPath != null && prefixPath.isPrefixOf(path)) {
					if (path.segment(0).equals(prefixPath.segment(0))) {
						path= path.removeFirstSegments(1);
						path= path.makeRelative();
					} else {
						path= path.makeAbsolute();
					}
				}
			}
		}
		element.setAttribute("path", path.toString());
		if (entry.getSourceAttachmentPath() != null){
			element.setAttribute("sourcepath", entry.getSourceAttachmentPath().toString());
		}
		if (entry.getSourceAttachmentRootPath() != null){
			element.setAttribute("rootpath", entry.getSourceAttachmentRootPath().toString());
		}
		return element;
	}
	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected char getHandleMementoDelimiter() {
		return JEM_JAVAPROJECT;
	}
	/**
	 * Find the specific Java command amongst the build spec of a given description
	 */
	private ICommand getJavaCommand(IProjectDescription description) throws CoreException {
		ICommand[] commands= description.getBuildSpec();
		for (int i= 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
				return commands[i];
			}
		}
		return null;
	}
	/**
	 * @see IJavaElement
	 */
	public IJavaProject getJavaProject() {
		return this;
	}
	/**
	 * Convenience method that returns the specific type of info for a Java project.
	 */
	protected JavaProjectElementInfo getJavaProjectElementInfo() throws JavaModelException {
		return (JavaProjectElementInfo) getElementInfo();
	}
	/**
	 * @see IJavaProject
	 */
	public INameLookup getNameLookup() throws JavaModelException {
		JavaProjectElementInfo info= getJavaProjectElementInfo();
		if (info.getNameLookup() == null) {
			info.setNameLookup(new NameLookup(this));
		}
		return info.getNameLookup();
	}
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {
		return ((JavaProjectElementInfo) getElementInfo()).getNonJavaResources(this);
	}
	/**
	 * @see IJavaProject
	 */
	public IPath getOutputLocation() throws JavaModelException {
		IPath path = getJavaProjectElementInfo().getOutputLocation();
		if (path == null) {
			return this.defaultOutputLocation();
		} else {
			return path;
		}
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(String jarPath) {
		jarPath = this.canonicalizedPath(new Path(jarPath)).toString();
		return new JarPackageFragmentRoot(jarPath, this);
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {
		String name= resource.getName().toUpperCase();
		if (name.endsWith(".JAR") || name.endsWith(".ZIP")) {
			return new JarPackageFragmentRoot(resource, this);
		} else {
			return new PackageFragmentRoot(resource, this);
		}
	}
	/**
	 * Returns a handle to the package fragment root identified by the given path.
	 * This method is handle-only and the element may or may not exist. Returns
	 * <code>null</code> if unable to generate a handle from the path (for example,
	 * an absolute path that has less than 2 segments. The path may be relative or
	 * absolute.
	 *
	 * @private
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IPath path) {

		IResource resource = null;
		if (!path.isAbsolute() || (resource = getProject().getWorkspace().getRoot().findMember(path)) != null) {
			if (resource != null){
				return getPackageFragmentRoot(resource);				
			}
			if (path.segmentCount() > 0) {
				String ext= path.getFileExtension();
				if (ext == null) {
					return getPackageFragmentRoot(getProject().getFolder(path));
				} else {
					// resource jar
					return getPackageFragmentRoot(getProject().getFile(path));
				}
			} else {
				// default root
				return getPackageFragmentRoot(getProject());
			}
		} else {
			return getPackageFragmentRoot(path.toString()); // external jar
		}
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots() throws JavaModelException {
		IPackageFragmentRoot[] children= getAllPackageFragmentRoots();
		Vector directChildren= new Vector(children.length);
		for (int i= 0; i < children.length; i++) {
			IPackageFragmentRoot root= children[i];
			IJavaProject proj= root.getJavaProject();
			if (proj != null && proj.equals(this)) {
				directChildren.addElement(root);
			}
		}
		children= new IPackageFragmentRoot[directChildren.size()];
		directChildren.copyInto(children);
		return children;
	}
	/**
	 * Returns the package fragment root prefixed by the given path, or
	 * an empty collection if there are no such elements in the model.
	 */
	protected IPackageFragmentRoot[] getPackageFragmentRoots(IPath path) throws JavaModelException {
		IPackageFragmentRoot[] roots= getAllPackageFragmentRoots();
		Vector matches= new Vector();
		for (int i= 0; i < roots.length; ++i) {
			if (path.isPrefixOf(roots[i].getPath())) {
				matches.addElement(roots[i]);
			}
		}
		IPackageFragmentRoot[] copy= new IPackageFragmentRoot[matches.size()];
		matches.copyInto(copy);
		return copy;
	}
	/**
	 * Returns the package fragment roots identified by the given entry.
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {

		entry = JavaCore.getResolvedClasspathEntry(entry);
		if (entry == null){
			return new IPackageFragmentRoot[] {}; // variable not found			
		}
		IPath path= entry.getPath();
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();

		if (entry.getContentKind() == IPackageFragmentRoot.K_BINARY) {
			String ext= path.getFileExtension();
			IPackageFragmentRoot root= null;
			if (ext != null && (ext.equalsIgnoreCase("zip") || ext.equalsIgnoreCase("jar"))) {
				// jar
				// removeFirstSegment removes the part relative to the project which is retrieve 
				// through workspace.getDefaultContentLocation
				if (path.isAbsolute() && getWorkspace().getRoot().findMember(path) == null) {
					// file system jar
					root= new JarPackageFragmentRoot(path.toOSString(), this);
				} else {
					// resource jar
					root= new JarPackageFragmentRoot(workspaceRoot.getFile(path), this);
				}
				return new IPackageFragmentRoot[] {root};
			}
		}
		IPath projectPath= getProject().getFullPath();
		if (projectPath.isPrefixOf(path)) {
			// local to this project
			IResource resource= null;
			// change zrh
			if (path.segmentCount() > 1) {
				resource= workspaceRoot.getFolder(path);
			} else {
				resource= workspaceRoot.findMember(path);
			}
			IPackageFragmentRoot root= new PackageFragmentRoot(resource, this);
			return new IPackageFragmentRoot[] {root};
		} else {
			// another project
			// change zrh
			if (path.segmentCount() != 1)
				return new IPackageFragmentRoot[] {}; // invalid path for a project
			String project= path.segment(0);
			IJavaProject javaProject= getJavaModel().getJavaProject(project);
			Vector sourceRoots= new Vector();
			IPackageFragmentRoot[] roots= null;
			try {
				roots= javaProject.getPackageFragmentRoots();
			} catch (JavaModelException e) {
				return new IPackageFragmentRoot[] {};
			}
			for (int i= 0; i < roots.length; i++) {
				try {
					if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
						sourceRoots.addElement(roots[i]);
					}
				} catch (JavaModelException e) {
					// do nothing if the root does not exist
				}
			}
			IPackageFragmentRoot[] copy= new IPackageFragmentRoot[sourceRoots.size()];
			sourceRoots.copyInto(copy);
			return copy;
		}
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragment[] getPackageFragments() throws JavaModelException {
		IPackageFragmentRoot[] roots= getPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}
	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots.
	 */
	protected IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) {
		Vector frags= new Vector();
		for (int i= 0; i < roots.length; i++) {
			IPackageFragmentRoot root= roots[i];
			try {
				IJavaElement[] rootFragments= root.getChildren();
				for (int j= 0; j < rootFragments.length; j++) {
					frags.addElement(rootFragments[j]);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments= new IPackageFragment[frags.size()];
		frags.copyInto(fragments);
		return fragments;
	}
	/**
	 * @see IJavaProject
	 */
	public IProject getProject() {
		return fProject;
	}
	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getRawClasspath() throws JavaModelException {
		JavaProjectElementInfo info= getJavaProjectElementInfo();
		IClasspathEntry[] classpath= info.getRawClasspath();
		if (classpath != null) {
			return classpath;
		}
		return defaultClasspath();
	}
/**
 * @see IJavaProject#getRequiredProjectNames
 */
public String[] getRequiredProjectNames() throws JavaModelException {
	return this.projectPrerequisites(getResolvedClasspath(true));
}
	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedVariable) throws JavaModelException {

		IClasspathEntry[] classpath = getRawClasspath();
		IClasspathEntry[] resolvedPath = classpath; // clone only if necessary
		int length = classpath.length;
		int index = 0;
		
		for (int i = 0; i < length; i++){
			
			IClasspathEntry entry = classpath[i];

			/* resolve variables if any, unresolved ones are ignored */
			if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE){

				// clone original path
				if (resolvedPath == classpath){
					System.arraycopy(classpath, 0, resolvedPath = new IClasspathEntry[length], 0, i);
				}
				// resolve current variable (handling variable->variable->variable->entry
				IPath variablePath = entry.getPath(); // for error reporting
				entry = JavaCore.getResolvedClasspathEntry(entry);
				if (entry == null && !ignoreUnresolvedVariable){
					throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND, variablePath.toString()));
				}
			}
			if (entry != null){
				resolvedPath[index++] = entry;
			}
		}

		// resize resolved classpath in case some variable entries could not be resolved
		if (index != length){
			System.arraycopy(resolvedPath, 0, resolvedPath = new IClasspathEntry[index], 0, index);
		}
		return resolvedPath;
	}
	/**
	 * @see IJavaProject
	 */
	public ISearchableNameEnvironment getSearchableNameEnvironment() throws JavaModelException {
		JavaProjectElementInfo info= getJavaProjectElementInfo();
		if (info.getSearchableEnvironment() == null) {
			info.setSearchableEnvironment(new SearchableEnvironment(this));
		}
		return info.getSearchableEnvironment();
	}
	/**
	 * Retrieve a shared property on a project. If the property is not defined, answers null.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 *
	 * @see JavaProject.setSharedProperty(...)
	 */
	public String getSharedProperty(QualifiedName key) throws CoreException {

		String property = null;
		try {
			String propertyFileName= computeSharedPropertyFileName(key);					
			IFile rscFile = getProject().getFile(propertyFileName);
			if (rscFile.exists()){
				InputStream input = rscFile.getContents(true);
				property = new String(Util.readContentsAsBytes(input));
			}
		} catch (IOException e) {
		}
		return property;
	}
	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {
		return null;
	}
	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		return getProject();
	}
	/**
	 * @see IJavaProject
	 */
	public boolean hasBuildState() {
		return JavaModelManager.getJavaModelManager().getLastBuiltState(this.getProject(), null) != null;
	}
	/**
	 * @see IJavaProject
	 */
	public boolean hasClasspathCycle(IClasspathEntry[] entries) {
		StringHashtableOfInt depthTable= new StringHashtableOfInt();
		try {
			String projectName= this.getElementName();
			depthTable.put(projectName, -2); // mark this project as being visited
			String[] prerequisites= this.projectPrerequisites(entries);
			for (int i= 0, length= prerequisites.length; i < length; i++) {
				((JavaModel) this.getJavaModel()).computeDepth(prerequisites[i], depthTable, projectName, false);
			}
		} catch (JavaModelException e) {
			return e.getStatus().getCode() == IJavaModelStatusConstants.NAME_COLLISION;
		}
		return false;
	}
	public int hashCode() {
		return fProject.hashCode();
	}
	/**
	 * Answers true if the project potentially contains any source. A project which has no source is immutable.
	 */
	public boolean hasSource() {
		// look if any source folder on the classpath
		// no need for resolved path given source folder cannot be abstracted
		IClasspathEntry[] entries;
		try {
			entries = this.getRawClasspath();
		} catch(JavaModelException e){
			return true; // unsure
		}
		for (int i = 0, max = entries.length; i < max; i++){
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE){
				return true;
			}
		}
		return false;
	}
	/**
	 * Compare current classpath with given one to see if any different.
	 * Note that the argument classpath contains its binary output.
	 */
	public boolean isClasspathEqualsTo(IClasspathEntry[] otherClasspathWithOutput) throws JavaModelException {

		if (otherClasspathWithOutput != null && otherClasspathWithOutput.length > 0) {
		
			IClasspathEntry[] classpath = getRawClasspath();
			int length = otherClasspathWithOutput.length;
			if (length == classpath.length+1){ // output is amongst file entries (last one)

				// compare classpath entries
				for (int i = 0; i < length-1; i++){
					if (!otherClasspathWithOutput[i].equals(classpath[i])) return false;
				}
				// compare binary outputs
				if (otherClasspathWithOutput[length-1].getContentKind() == ClasspathEntry.K_OUTPUT
					&& otherClasspathWithOutput[length-1].getPath().equals(getOutputLocation())) return true;
			}
		}
		return false;
	}
	/**
	 * Returns the kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int kindFromString(String kindStr) {
		if (kindStr.equalsIgnoreCase("prj"))
			return IClasspathEntry.CPE_PROJECT;
		if (kindStr.equalsIgnoreCase("var"))
			return IClasspathEntry.CPE_VARIABLE;
		if (kindStr.equalsIgnoreCase("src"))
			return IClasspathEntry.CPE_SOURCE;
		if (kindStr.equalsIgnoreCase("lib"))
			return IClasspathEntry.CPE_LIBRARY;
		if (kindStr.equalsIgnoreCase("output"))
			return ClasspathEntry.K_OUTPUT;
		return -1;
	}
	/**
	 * Returns a <code>String</code> for the kind of a class path entry.
	 */
	static String kindToString(int kind) {
		switch (kind) {
			case IClasspathEntry.CPE_PROJECT :
				return "src"; // backward compatibility
			case IClasspathEntry.CPE_SOURCE :
				return "src";
			case IClasspathEntry.CPE_LIBRARY :
				return "lib";
			case IClasspathEntry.CPE_VARIABLE :
				return "var";
			case ClasspathEntry.K_OUTPUT :
				return "output";
			default :
				return "unknown";
		}
	}
	/**
	 * load the classpath from a shareable format (VCM-wise)
	 */
	public String loadClasspath() throws JavaModelException {

		try {
			return getSharedProperty(getClasspathPropertyName());
		} catch(CoreException e){
			throw new JavaModelException(e);
		}
	}
	/**
	 * @see IJavaProject#newEvaluationContext
	 */
	public IEvaluationContext newEvaluationContext() {
		return new EvaluationContextWrapper(new EvaluationContext(), this);
	}
	/**
	 * @see IJavaProject
	 * @deprecated
	 */
	public IClasspathEntry newLibraryEntry(IPath path) {
		return JavaCore.newLibraryEntry(path, null, null);
	}
	/**
	 * @see IJavaProject
 	 * @deprecated
	 */
	public IClasspathEntry newProjectEntry(IPath path) {
		return JavaCore.newProjectEntry(path);
	}
	/**
	 * @see IJavaProject
	 * @deprecated	 
	 */
	public IClasspathEntry newSourceEntry(IPath path) {
		return JavaCore.newSourceEntry(path);
	}
	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(IRegion region, IProgressMonitor monitor) throws JavaModelException {
		if (region == null) {
			throw new IllegalArgumentException("region cannot be null");
		}
		CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(null, region, this, true);
		runOperation(op, monitor);
		return op.getResult();
	}
	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(IType type, IRegion region, IProgressMonitor monitor) throws JavaModelException {
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (region == null) {
			throw new IllegalArgumentException("region cannot be null");
		}
		CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(type, region, this, true);
		runOperation(op, monitor);
		return op.getResult();
	}
	/**
	 * Ensures that this project is not currently being deleted before
	 * opening.
	 *
	 * fix for 1FW67PA
	 */
	public void open(IProgressMonitor pm) throws JavaModelException {
		JavaModelManager manager= (JavaModelManager) JavaModelManager.getJavaModelManager();
		if (manager.isBeingDeleted(fProject)) {
			throw newNotPresentException();
		} else {
			super.open(pm);
		}
	}
	/**
	 * Ensures that this project is not currently being deleted before
	 * opening.
	 *
	 * fix for 1FW67PA
	 */
	protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {
		JavaModelManager manager= (JavaModelManager) JavaModelManager.getJavaModelManager();
		if (manager.isBeingDeleted(fProject) || !this.fProject.isOpen()) {
			throw newNotPresentException();
		} else {
			super.openWhenClosed(pm);
		}
	}
	private String[] projectPrerequisites(IClasspathEntry[] entries) throws JavaModelException {
		Vector prerequisites= new Vector();
		for (int i= 0, length= entries.length; i < length; i++) {
			IClasspathEntry entry= entries[i];
			entry = JavaCore.getResolvedClasspathEntry(entry);
			if (entry == null) continue; // ignore unbound variable
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				prerequisites.add(entry.getPath().lastSegment());
			}
		}
		int size= prerequisites.size();
		if (size == 0) {
			return NO_PREREQUISITES;
		} else {
			String[] result= new String[size];
			prerequisites.copyInto(result);
			return result;
		}
	}
	/**
	 * Returns a collection of <code>IClasspathEntry</code>s from the given
	 * classpath string in XML format.
	 *
	 * @exception IOException if the stream cannot be read 
	 */
	protected IClasspathEntry[] readPaths(String xmlClasspath) throws IOException {
		IPath projectPath= getProject().getFullPath();
		StringReader reader = new StringReader(xmlClasspath);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch(SAXException e) {
			throw new IOException("bad format");
		} catch(ParserConfigurationException e){
			reader.close();
			throw new IOException("bad format");
		} finally {
			reader.close();
		}
		if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) {
			throw new IOException("bad format");
		}
		NodeList list= cpElement.getChildNodes();
		Vector paths= new Vector();
		int length= list.getLength();
		for (int i= 0; i < length; ++i) {
			Node node= list.item(i);
			short type= node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element cpeElement= (Element) node;
				if (cpeElement.getNodeName().equalsIgnoreCase("classpathentry")) {
					String cpeElementKind = cpeElement.getAttribute("kind");
					String pathStr = cpeElement.getAttribute("path");
					// ensure path is absolute
					IPath path= new Path(pathStr);
					int kind= kindFromString(cpeElementKind);
					if (kind != IClasspathEntry.CPE_VARIABLE && !path.isAbsolute()) {
						path= projectPath.append(path);
					}
					// source attachment info (optional)
					String sourceAttachmentPathStr = cpeElement.getAttribute("sourcepath");
					IPath sourceAttachmentPath = sourceAttachmentPathStr.equals("") ? null : new Path(sourceAttachmentPathStr);
					String sourceAttachmentRootPathStr = cpeElement.getAttribute("rootpath");
					IPath sourceAttachmentRootPath = sourceAttachmentRootPathStr.equals("") ? null : new Path(sourceAttachmentRootPathStr);
					
					IClasspathEntry entry= createClasspathEntry(path, kind, sourceAttachmentPath, sourceAttachmentRootPath);
					if (entry == null) return null;
					paths.addElement(entry);
				}
			}
		}
		if (paths.size() > 0) {
			IClasspathEntry[] ips= new IClasspathEntry[paths.size()];
			paths.copyInto(ips);
			return ips;
		} else {
			return null;
		}
	}
	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException {
		IProjectDescription description= getProject().getDescription();
		ICommand[] commands= description.getBuildSpec();
		for (int i= 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands= new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				getProject().setDescription(description, null);
				return;
			}
		}
	}
/**
 * Reset the non-java resources collection for package fragment roots of the
 * receiver
 */
protected void resetNonJavaResourcesForPackageFragmentRoots() throws JavaModelException {
	IPackageFragmentRoot[] roots = getAllPackageFragmentRoots();
	if (roots == null) return;
	for (int i = 0, max = roots.length; i < max; i++) {
		IPackageFragmentRoot root = roots[i];
		try {
			IResource res = root.getUnderlyingResource();
			if (res != null) {
				((PackageFragmentRoot)root).resetNonJavaResources();
			}
		} catch(JavaModelException e) {
			// ignore if the resource cannot be retrieved anymore.
		}
	}
}
	/**
	 * Returns the <code>IResource</code> that correspond to the specified path.
	 * null if none.
	 */
	private IResource retrieveResource(IPath path, IResourceDelta delta) throws JavaModelException {
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();
		IResource res= workspaceRoot.findMember(path);

		if (delta == null)
			return res;

		if (res == null) {
			// the resource has been removed or renamed
			// look for a possible delta that might help to retrieve the new resource if case of renamed
			IResourceDelta[] deltas= delta.getAffectedChildren();
			for (int i= 0, max= deltas.length; i < max; i++) {
				IResourceDelta currentDelta= deltas[i];
				if (currentDelta.getKind() == IResourceDelta.REMOVED) {
					IPath moveToPath= currentDelta.getMovedToPath();
					if (moveToPath != null) {
						res= workspaceRoot.findMember(moveToPath);
						if (res == null) {
							throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, moveToPath));
						}
						break;
					} else {
						break;
					}
				}
			}
		}
		return res;
	}
	/**
	 * Save the classpath in a shareable format (VCM-wise) if necessary (i.e. semantically different)
	 */
	public void saveClasspath() throws JavaModelException {
		this.saveClasspath(false);
	}
	/**
	 * Save the classpath in a shareable format (VCM-wise) if necessary (i.e. semantically different)
	 */
	public void saveClasspath(boolean force) throws JavaModelException {
		if (!getProject().exists()) return;
		if (!isOpen()) return; // no update for closed projects ???
		
		QualifiedName classpathProp = getClasspathPropertyName();

		try {
			// attempt to prove the classpath has not change
			String fileClasspathString = getSharedProperty(classpathProp);
			if (fileClasspathString != null){
				IClasspathEntry[] fileEntries = readPaths(fileClasspathString);
				if (!force && isClasspathEqualsTo(fileEntries)) {
					// no need to save it, it is already the same
					return; 
				}
			}
		} catch (IOException e){
		} catch (RuntimeException e){
		} catch(CoreException e){
		}
	
		// actual file saving
		try {
			setSharedProperty(
				classpathProp, 
				getClasspathAsXMLString(getRawClasspath(), getOutputLocation()));
		} catch(CoreException e){
			throw new JavaModelException(e);
		}
	}
	/**
	 * @see IJavaProject
	 * @deprecated
	 */
	public void setClasspath(IClasspathEntry[] entries, IProgressMonitor monitor) throws JavaModelException {
		setRawClasspath(entries, monitor, true);
	}
	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setJavaCommand(IProjectDescription description, ICommand newCommand) throws CoreException {

		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldJavaCommand = getJavaCommand(description);
		
		ICommand[] newCommands;
		
		if (oldJavaCommand == null) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
			newCommands[0]= newCommand;
		} else {
			for (int i = 0, max = oldCommands.length; i < max; i++){
				if (oldCommands[i] == oldJavaCommand){
					oldCommands[i] = newCommand;
					break;
				}
			}
			newCommands = oldCommands;
		}
		
		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		getProject().setDescription(description, null);
	}
	/**
	 * @see IJavaProject
	 */
	public void setOutputLocation(IPath outputLocation, IProgressMonitor monitor) throws JavaModelException {
		if (outputLocation == null) {
			throw new IllegalArgumentException("path cannot be null");
		}
		if (outputLocation.equals(getOutputLocation())) {
			return;
		}
		SetOutputLocationOperation op= new SetOutputLocationOperation(this, outputLocation);
		runOperation(op, monitor);
	}
	/**
	 * @private - for use by <code>SetOutputLocationOperation</code> only.<br>
	 * Set the path to the location where the builder writes .class files
	 *
	 * @exception JavaModelException if an error occurs setting the output location
	 *      or if this project is not present
	 */
	protected void setOutputLocation0(IPath outputLocation) throws JavaModelException {
		//getting the element info (if it is generated) has the side effect of setting the
		//output location to that specified in the classpath file (or the default output location
		//if none is specified in the classpath file).
		JavaProjectElementInfo info= getJavaProjectElementInfo();
		info.setOutputLocation(outputLocation);
	}
	/**
	 * Sets the underlying kernel project of this Java project,
	 * and fills in its parent and name.
	 * Called by IProject.getNature().
	 *
	 * @see IProjectNature#setProject
	 */
	public void setProject(IProject project) {
		fProject= project;
		fParent= JavaModelManager.getJavaModel(project.getWorkspace());
		fName= project.getName();
	}
	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(IClasspathEntry[] entries, IProgressMonitor monitor) throws JavaModelException {
		setRawClasspath(entries, monitor, true, getResolvedClasspath(true)); 
	}
	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(IClasspathEntry[] entries, IProgressMonitor monitor, boolean saveClasspath) throws JavaModelException {
		setRawClasspath(entries, monitor, saveClasspath, getResolvedClasspath(true)); 
	}
	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(IClasspathEntry[] newEntries, IProgressMonitor monitor, boolean saveClasspath, IClasspathEntry[] oldResolvedPath) throws JavaModelException {
		JavaModelManager manager= (JavaModelManager) JavaModelManager.getJavaModelManager();
		try {
			IJavaModelStatus status= verifyClasspath(newEntries);
			if (!status.isOK()) {
				throw new JavaModelException(status);
			}
			JavaProjectElementInfo info = getJavaProjectElementInfo();
			IClasspathEntry[] newRawPath = newEntries;
			if (newRawPath == null) { //are we already with the default classpath
				newRawPath = defaultClasspath();
			}
			SetClasspathOperation op= new SetClasspathOperation(this, oldResolvedPath, newRawPath, saveClasspath);
			runOperation(op, monitor);
		} catch (JavaModelException e) {
			manager.flush();
			throw e;
		}
	}
	/**
	 * NOTE: <code>null</code> specifies default classpath, and an empty
	 * array specifies an empty classpath.
	 *
	 * @exception NotPresentException if this project does not exist.
	 */
	protected void setRawClasspath0(IClasspathEntry[] entries) throws JavaModelException {
		JavaProjectElementInfo info= getJavaProjectElementInfo();

		synchronized(info){
			if (entries == null) {
				entries= defaultClasspath();
			}

			// clear the existing children
			info.setChildren(new IPackageFragmentRoot[] {});
			info.setRawClasspath(entries);

			IndexManager indexManager = ((JavaModelManager)JavaModelManager.getJavaModelManager()).getIndexManager();
			
			// determine the new children
			for (int i= 0; i < entries.length; i++) {
				IClasspathEntry entry= entries[i];
				IPackageFragmentRoot[] roots= getPackageFragmentRoots(entry);
				for (int j= 0; j < roots.length; j++) {
					PackageFragmentRoot root= (PackageFragmentRoot)roots[j];
					if (root.exists0()){
						if (root.isArchive()) {
							IResource rsc= root.getUnderlyingResource();
							if (rsc == null) {
								if (indexManager != null) indexManager.indexJarFile(root, getUnderlyingResource().getName());
							} else {
								if (indexManager != null) indexManager.indexJarFile((IFile) rsc, getUnderlyingResource().getName());
							}
						}
						info.addChild(roots[j]);
					}
				}
			}
			// flush namelookup (holds onto caches)
			info.setNameLookup(null);
			// See PR 1G8BFWS: ITPJUI:WINNT - internal jar appearing twice in packages view
			resetNonJavaResourcesForPackageFragmentRoots();
			((JavaProjectElementInfo) getElementInfo()).setNonJavaResources(null);
		}
	}
	/**
	 * Record a shared persistent property onto a project.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	* 
	 * shared properties end up in resource files, and thus cannot be modified during
	 * delta notifications (a CoreException would then be thrown).
	 * 
	 * @see JavaProject.getSharedProperty(...)
	 */
	public void setSharedProperty(QualifiedName key, String value) throws CoreException  {
		
		IProject project = getProject();
		String propertyName= computeSharedPropertyFileName(key);
		IFile rscFile = getProject().getFile(propertyName);
		InputStream input = new ByteArrayInputStream(value.getBytes());
		// update the resource content
		if (rscFile.exists()){
			rscFile.setContents(input, true, false, null);
		} else {
			rscFile.create(input, true, null);
		}
	}
	public void updateClassPath() throws JavaModelException {
		setRawClasspath(getRawClasspath(), null, false);
	}
	/**
	 * Possible failures: <ul>
	 *  <li>NAME_COLLISION - two entries specify the same path.
	 *  <li>INVALID_PATH - a CPE_PROJECT entry has been specified referring to this project
	 * </ul>
	 */
	protected IJavaModelStatus verifyClasspath(IClasspathEntry[] classpath) {
		if (classpath != null) {
			int entryCount= classpath.length;
			for (int i= 0; i < entryCount; i++) {
				IClasspathEntry entry= classpath[i];
				inner : for (int j= 0; j < entryCount; j++) {
					if (i == j) {
						continue inner;
					}
					if (JavaConventions.isOverlappingRoots(entry.getPath(), classpath[j].getPath())) {
						return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION);
					}
				}
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT && entry.getPath().equals(getProject().getFullPath())) {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH);
				}
			}
		}
		return JavaModelStatus.VERIFIED_OK;
	}
}
