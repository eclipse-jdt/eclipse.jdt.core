package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.eval.EvaluationContext;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

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
 * <p>Each JavaProject has a NameLookup facility that locates elements
 * on by name, based on the devpath.
 *
 * @see IJavaProject
 */
public class JavaProject
	extends Openable
	implements IJavaProject, IProjectNature {

/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = new String[0];

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
		fProject = project;
	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = getProject().getDescription();
		ICommand javaCommand = getJavaCommand(description);

		if (javaCommand == null) {

			// Add a Java command to the build spec
			ICommand command = description.newCommand();
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

		if (externalPath == null)
			return null;

		// if not external path, return original path
		if (ResourcesPlugin.getWorkspace().getRoot().findMember(externalPath)
			!= null) {
			return externalPath;
		}

		IPath canonicalPath = null;
		try {
			canonicalPath =
				new Path(new File(externalPath.toOSString()).getCanonicalPath());
		} catch (IOException e) {
			// default to original path
			return externalPath;
		}
		// keep only segments that were in original path and device if it was there
		IPath result =
			canonicalPath.removeFirstSegments(
				canonicalPath.segmentCount() - externalPath.segmentCount());
		if (externalPath.getDevice() == null) {
			return result.setDevice(null);
		} else {
			return result;
		}
	}
	
	protected void closing(Object info) throws JavaModelException {
		
		// forget source attachment recommendations
		IPackageFragmentRoot[] roots = this.getPackageFragmentRoots();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i] instanceof JarPackageFragmentRoot){
				((JarPackageFragmentRoot) roots[i]).setSourceAttachmentProperty(null); 
			}
		}
		super.closing(info);
	}
	
	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(boolean retrieveExportedRoots) {

		ObjectVector accumulatedRoots = new ObjectVector();
		computePackageFragmentRoots(accumulatedRoots, new HashSet(5), true, true, retrieveExportedRoots);
		IPackageFragmentRoot[] rootArray = new IPackageFragmentRoot[accumulatedRoots.size()];
		accumulatedRoots.copyInto(rootArray);
		return rootArray;
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 */
	public void computePackageFragmentRoots(
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		boolean insideOriginalProject,
		boolean checkExistency,
		boolean retrieveExportedRoots) {

		if (insideOriginalProject){
			rootIDs.add(rootID());
		}	
		try {
			IClasspathEntry[] classpath = getResolvedClasspath(true);
	
			for (int i = 0, length = classpath.length; i < length; i++){
				computePackageFragmentRoots(
					classpath[i],
					accumulatedRoots,
					rootIDs,
					insideOriginalProject,
					checkExistency,
					retrieveExportedRoots);
			}
		} catch(JavaModelException e){
		}			
	}

	/**
	 * Returns the package fragment roots identified by the given entry. In case it refers to
	 * a project, it will follow its classpath so as to find exported roots as well.
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry entry,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		boolean insideOriginalProject,
		boolean checkExistency,
		boolean retrieveExportedRoots) {
			
		String rootID = ((ClasspathEntry)entry).rootID();
		if (rootIDs.contains(rootID)) return;

		IPath projectPath = getProject().getFullPath();
		IPath entryPath = entry.getPath();
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();
		
		switch(entry.getEntryKind()){
			
			// source folder
			case IClasspathEntry.CPE_SOURCE :

				if (projectPath.isPrefixOf(entryPath)){
					Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
					if (target == null) return;

					if (target instanceof IFolder || target instanceof IProject){
						accumulatedRoots.add(
							new PackageFragmentRoot((IResource)target, this));
						rootIDs.add(rootID);
					}
				}
				break;

			// internal/external JAR or folder
			case IClasspathEntry.CPE_LIBRARY :
			
				if (!insideOriginalProject && !entry.isExported()) return;

				String extension = entryPath.getFileExtension();

				Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
				if (target == null) return;

				if (target instanceof IResource){
					
					// internal target
					IResource resource = (IResource) target;
					switch (resource.getType()){
						case IResource.FOLDER :
							accumulatedRoots.add(
								new PackageFragmentRoot(resource, this));
							rootIDs.add(rootID);
							break;
						case IResource.FILE :
							if ("jar".equalsIgnoreCase(extension) //$NON-NLS-1$
								|| "zip".equalsIgnoreCase(extension)) { //$NON-NLS-1$
								accumulatedRoots.add(
									new JarPackageFragmentRoot(resource, this));
								}
								rootIDs.add(rootID);
						break;
					}
				} else {
					// external target - only JARs allowed
					if ("jar".equalsIgnoreCase(extension) //$NON-NLS-1$
						|| "zip".equalsIgnoreCase(extension)) { //$NON-NLS-1$
						accumulatedRoots.add(
							new JarPackageFragmentRoot(entryPath.toOSString(), this));
						rootIDs.add(rootID);
					}
				}
				break;

			// recurse into required project
			case IClasspathEntry.CPE_PROJECT :

				if (!retrieveExportedRoots) return;
				if (!insideOriginalProject && !entry.isExported()) return;

				JavaProject requiredProject = (JavaProject)getJavaModel().getJavaProject(entryPath.segment(0));
				IProject requiredProjectRsc = requiredProject.getProject();
				if (requiredProjectRsc.exists() && requiredProjectRsc.isOpen()){ // special builder binary output
					rootIDs.add(rootID);
					requiredProject.computePackageFragmentRoots(accumulatedRoots, rootIDs, false, checkExistency, retrieveExportedRoots);
				}
				break;
			}
	}

	/**
	 * Compute the file name to use for a given shared property
	 */
	public String computeSharedPropertyFileName(QualifiedName qName) {

		return '.' + qName.getLocalName();
	}
	
	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException {

		// register Java builder
		addToBuildSpec(JavaCore.BUILDER_ID);

		// notify Java delta (Java project added) 
		JavaModelManager manager =
			(JavaModelManager) JavaModelManager.getJavaModelManager();
		JavaModel model = (JavaModel) getJavaModel();
		JavaElementDelta projectDelta = new JavaElementDelta(model);
		projectDelta.added(this);
		JavaElementInfo jmi = model.getElementInfo();
		jmi.addChild(this);
		manager.registerResourceDelta(projectDelta);
		manager.fire();
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

		return new IClasspathEntry[] {
			 JavaCore.newSourceEntry(getProject().getFullPath())};
	}

	/**
	 * Returns a default output location.
	 * This is the project bin folder
	 */
	protected IPath defaultOutputLocation() throws JavaModelException {
		return getProject().getFullPath().append("bin"); //$NON-NLS-1$
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

		JavaProject other = (JavaProject) o;
		return getProject().equals(other.getProject())
			&& fOccurrenceCount == other.fOccurrenceCount;
	}

	/**
	 * @see IJavaProject
	 */
	public IJavaElement findElement(IPath path) throws JavaModelException {

		if (path == null || path.isAbsolute()) {
			throw new JavaModelException(
				new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, path));
		}
		try {

			String extension = path.getFileExtension();
			if (extension == null) {
				String packageName = path.toString().replace(IPath.SEPARATOR, '.');

				IPackageFragment[] pkgFragments =
					getNameLookup().findPackageFragments(packageName, false);
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
			} else if (
				extension.equalsIgnoreCase("java") //$NON-NLS-1$
					|| extension.equalsIgnoreCase("class")) {  //$NON-NLS-1$
				IPath packagePath = path.removeLastSegments(1);
				String packageName = packagePath.toString().replace(IPath.SEPARATOR, '.');
				String typeName = path.lastSegment();
				typeName = typeName.substring(0, typeName.length() - extension.length() - 1);
				String qualifiedName = null;
				if (packageName.length() > 0) {
					qualifiedName = packageName + "." + typeName; //$NON-NLS-1$
				} else {
					qualifiedName = typeName;
				}
				IType type =
					getNameLookup().findType(
						qualifiedName,
						false,
						NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
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
			if (e.getStatus().getCode()
				== IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				return null;
			} else {
				throw e;
			}
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment findPackageFragment(IPath path)
		throws JavaModelException {

		return getNameLookup().findPackageFragment(this.canonicalizedPath(path));
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaModelException {

		IPackageFragmentRoot[] allRoots = this.getAllPackageFragmentRoots();
		path = this.canonicalizedPath(path);
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Util.bind("path.mustBeAbsolute")); //$NON-NLS-1$
		}
		for (int i= 0; i < allRoots.length; i++) {
			IPackageFragmentRoot classpathRoot= allRoots[i];
			if (classpathRoot.getPath().equals(path)) {
				return classpathRoot;
			}
		}
		return null;
	}
	
	/**
	 * @see Openable
	 */
	protected boolean generateInfos(
		OpenableElementInfo info,
		IProgressMonitor pm,
		Map newElements,
		IResource underlyingResource)
		throws JavaModelException {

		boolean validInfo = false;
		try {
			if (((IProject) getUnderlyingResource()).isOpen()) {
				// put the info now, because setting the classpath requires it
				fgJavaModelManager.putInfo(this, info);

				// read classpath property (contains actual classpath and output location settings)
				boolean needToSaveClasspath = false;
				IPath outputLocation = null;
				IClasspathEntry[] classpath = null;

				// read from file
				String sharedClasspath = loadClasspath();
				if (sharedClasspath != null) {
					try {
						classpath = readPaths(sharedClasspath);
					} catch (IOException e) {
					} catch (RuntimeException e) {
					}
					// extract out the output location
					if (classpath != null && classpath.length > 0) {
						IClasspathEntry entry = classpath[classpath.length - 1];
						if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
							outputLocation = entry.getPath();
							IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
							System.arraycopy(classpath, 0, copy, 0, copy.length);
							classpath = copy;
						}
					}
				}
				// restore output location				
				if (outputLocation == null) {
					outputLocation = defaultOutputLocation();
					needToSaveClasspath = true;
				}
				((JavaProjectElementInfo)info).setOutputLocation(outputLocation);

				// restore classpath
				if (classpath == null) {
					classpath = defaultClasspath();
					needToSaveClasspath = true;
				}
				setRawClasspath0(classpath);

				// only valid if reaches here				
				validInfo = true;
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
	public IPackageFragmentRoot[] getAllPackageFragmentRoots()
		throws JavaModelException {

		return computePackageFragmentRoots(true);
	}

	/**
	 * Returns all the <code>IPackageFragmentRoot</code>s the builder needs to
	 * know about in order to build this project. This includes:
	 * <ul>
	 *   <li>the source roots for the current project
	 *   <li>the binary roots (output locations) for the required projects
	 *   <li>the binary roots for any jar/lib used by this project
	 * </li>
	 * 
	 */
	public IPackageFragmentRoot[] getBuilderRoots(IResourceDelta delta)
		throws JavaModelException {

		ArrayList builderRoots = new ArrayList();
		IClasspathEntry[] classpath;
		classpath = getExpandedClasspath(true);
		IResource res;
		IJavaProject project;

		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			switch (entry.getEntryKind()) {

				case IClasspathEntry.CPE_LIBRARY :
					IPackageFragmentRoot[] roots = this.getPackageFragmentRoots(entry);
					if (roots.length > 0)
						builderRoots.add(roots[0]);
					break;

				case IClasspathEntry.CPE_PROJECT :
					// other project contributions are restrained to their binary output
					res = retrieveResource(entry.getPath(), delta);
					if (res != null) {
						project = (IJavaProject) JavaCore.create(res);
						if (project.isOpen()) {
							res = retrieveResource(project.getOutputLocation(), delta);
							if (res != null) {
								PackageFragmentRoot root =
									(PackageFragmentRoot) project.getPackageFragmentRoot(res);
								root.setOccurrenceCount(root.getOccurrenceCount() + 1);
								((PackageFragmentRootInfo) root.getElementInfo()).setRootKind(
									IPackageFragmentRoot.K_BINARY);
								root.refreshChildren();
								builderRoots.add(root);
							}
						}
					}
					break;

				case IClasspathEntry.CPE_SOURCE :
					if (getCorrespondingResource().getFullPath().isPrefixOf(entry.getPath())) {
						res = retrieveResource(entry.getPath(), delta);
						if (res != null)
							builderRoots.add(getPackageFragmentRoot(res));
					} else {
						IProject proj = (IProject) getWorkspace().getRoot().findMember(entry.getPath());
						project = (IJavaProject) JavaCore.create(proj);
						if (proj.isOpen()) {
							res = retrieveResource(project.getOutputLocation(), delta);
							PackageFragmentRoot root =
								(PackageFragmentRoot) project.getPackageFragmentRoot(res);
							root.setOccurrenceCount(root.getOccurrenceCount() + 1);
							((PackageFragmentRootInfo) root.getElementInfo()).setRootKind(
								IPackageFragmentRoot.K_BINARY);
							root.refreshChildren();
							builderRoots.add(root);
						}
					}
					break;
			}
		}
		IPackageFragmentRoot[] result = new IPackageFragmentRoot[builderRoots.size()];
		builderRoots.toArray(result);
		return result;
	}

	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected String getClasspathAsXMLString(
		IClasspathEntry[] classpath,
		IPath outputLocation)
		throws JavaModelException {

		Document doc = new DocumentImpl();
		Element cpElement = doc.createElement("classpath"); //$NON-NLS-1$
		doc.appendChild(cpElement);

		for (int i = 0; i < classpath.length; ++i) {
			Element cpeElement =
				getEntryAsXMLElement(doc, classpath[i], getProject().getFullPath());
			cpElement.appendChild(cpeElement);
		}

		if (outputLocation != null) {
			outputLocation = outputLocation.removeFirstSegments(1);
			outputLocation = outputLocation.makeRelative();
			Element oElement = doc.createElement("classpathentry"); //$NON-NLS-1$
			oElement.setAttribute("kind", kindToString(ClasspathEntry.K_OUTPUT));	//$NON-NLS-1$
			oElement.setAttribute("path", outputLocation.toOSString()); //$NON-NLS-1$
			cpElement.appendChild(oElement);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		try {
			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			Serializer serializer =
				SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
					writer,
					format);
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
	public IClasspathEntry getClasspathEntryFor(IPath path)
		throws JavaModelException {

		IClasspathEntry[] entries = getExpandedClasspath(true);
		for (int i = 0; i < entries.length; i++) {
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
		return new QualifiedName(JavaCore.PLUGIN_ID, "classpath"); //$NON-NLS-1$
	}

	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected static Element getEntryAsXMLElement(
		Document document,
		IClasspathEntry entry,
		IPath prefixPath)
		throws JavaModelException {

		Element element = document.createElement("classpathentry"); //$NON-NLS-1$
		element.setAttribute("kind", kindToString(entry.getEntryKind()));	//$NON-NLS-1$
		IPath path = entry.getPath();
		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE) {
			// translate to project relative from absolute (unless a device path)
			if (path.isAbsolute()) {
				if (prefixPath != null && prefixPath.isPrefixOf(path)) {
					if (path.segment(0).equals(prefixPath.segment(0))) {
						path = path.removeFirstSegments(1);
						path = path.makeRelative();
					} else {
						path = path.makeAbsolute();
					}
				}
			}
		}
		element.setAttribute("path", path.toString()); //$NON-NLS-1$
		if (entry.getSourceAttachmentPath() != null) {
			element.setAttribute("sourcepath", entry.getSourceAttachmentPath().toString()); //$NON-NLS-1$
		}
		if (entry.getSourceAttachmentRootPath() != null) {
			element.setAttribute(
				"rootpath", //$NON-NLS-1$
				entry.getSourceAttachmentRootPath().toString());
		}
		if (entry.isExported()) {
			element.setAttribute("exported", "true"); //$NON-NLS-1$ //$NON-NLS-2$
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
	private ICommand getJavaCommand(IProjectDescription description)
		throws CoreException {

		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
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
	protected JavaProjectElementInfo getJavaProjectElementInfo()
		throws JavaModelException {

		return (JavaProjectElementInfo) getElementInfo();
	}

	/**
	 * @see IJavaProject
	 */
	public NameLookup getNameLookup() throws JavaModelException {

		JavaProjectElementInfo info = getJavaProjectElementInfo();
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

		String name = resource.getName();
		if (Util.endsWithIgnoreCase(name, ".jar") //$NON-NLS-1$
			|| Util.endsWithIgnoreCase(name, ".zip")) { //$NON-NLS-1$ 
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
		if (!path.isAbsolute()
			|| (resource = getProject().getWorkspace().getRoot().findMember(path)) != null) {
			if (resource != null) {
				return getPackageFragmentRoot(resource);
			}
			if (path.segmentCount() > 0) {
				String ext = path.getFileExtension();
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
			String ext = path.getFileExtension();
			if ("jar".equalsIgnoreCase(ext)  //$NON-NLS-1$
				|| "zip".equalsIgnoreCase(ext)) { //$NON-NLS-1$
				// external jar
				return getPackageFragmentRoot(path.toOSString());
			} else {
				// unknown path
				return null;
			}
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots()
		throws JavaModelException {

		Object[] children;
		int length;
		IPackageFragmentRoot[] roots;

		System.arraycopy(
			children = getChildren(), 
			0, 
			roots = new IPackageFragmentRoot[length = children.length], 
			0, 
			length);
			
		return roots;
	}

	/**
	 * Returns the package fragment root prefixed by the given path, or
	 * an empty collection if there are no such elements in the model.
	 */
	protected IPackageFragmentRoot[] getPackageFragmentRoots(IPath path)

		throws JavaModelException {
		IPackageFragmentRoot[] roots = getAllPackageFragmentRoots();
		ArrayList matches = new ArrayList();

		for (int i = 0; i < roots.length; ++i) {
			if (path.isPrefixOf(roots[i].getPath())) {
				matches.add(roots[i]);
			}
		}
		IPackageFragmentRoot[] copy = new IPackageFragmentRoot[matches.size()];
		matches.toArray(copy);
		return copy;
	}

	/**
	 * Returns the package fragment roots identified by the given entry.
	 * @deprecated - use findPackageFragmentRoot(IPath)
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {

		entry = JavaCore.getResolvedClasspathEntry(entry);
		if (entry == null) {
			return new IPackageFragmentRoot[] {
			}; // variable not found			
		}
		IPath path = entry.getPath();
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();

		String ext = path.getFileExtension();
		if (ext != null && entry.getContentKind() == IPackageFragmentRoot.K_BINARY) {
			IPackageFragmentRoot root = null;
			if (ext.equalsIgnoreCase("zip") //$NON-NLS-1$
				|| ext.equalsIgnoreCase("jar")) {  //$NON-NLS-1$
				// jar
				// removeFirstSegment removes the part relative to the project which is retrieve 
				// through workspace.getDefaultContentLocation
				if (path.isAbsolute() && getWorkspace().getRoot().findMember(path) == null) {
					// file system jar
					root = new JarPackageFragmentRoot(path.toOSString(), this);
				} else {
					// resource jar
					root = new JarPackageFragmentRoot(workspaceRoot.getFile(path), this);
				}
				return new IPackageFragmentRoot[] { root };
			}
		}
		IPath projectPath = getProject().getFullPath();
		if (projectPath.isPrefixOf(path)) {
			// local to this project
			IResource resource = null;
			if (path.segmentCount() > 1) {
				resource = workspaceRoot.getFolder(path);
			} else {
				resource = workspaceRoot.findMember(path);
			}
			if (resource == null)
				return new IPackageFragmentRoot[] {
			};
			IPackageFragmentRoot root = new PackageFragmentRoot(resource, this);
			return new IPackageFragmentRoot[] { root };
		} else {
			// another project
			if (path.segmentCount() != 1) {
				if (entry.getContentKind() == IPackageFragmentRoot.K_BINARY) {
					// binary folder in another project
					IResource resource = workspaceRoot.getFolder(path);
					if (resource == null) {
						return new IPackageFragmentRoot[] {};
					} else {
						IPackageFragmentRoot root = new PackageFragmentRoot(resource, this);
						return new IPackageFragmentRoot[] { root };
					}
				} else {
					// invalid path for a project
					return new IPackageFragmentRoot[] {};
				}
			} else {
				String project = path.segment(0);
				IJavaProject javaProject = getJavaModel().getJavaProject(project);
				ArrayList sourceRoots = new ArrayList();
				IPackageFragmentRoot[] roots = null;
				try {
					roots = javaProject.getPackageFragmentRoots();
				} catch (JavaModelException e) {
					return new IPackageFragmentRoot[] {};
				}
				for (int i = 0; i < roots.length; i++) {
					try {
						if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
							sourceRoots.add(roots[i]);
						}
					} catch (JavaModelException e) {
						// do nothing if the root does not exist
					}
				}
				IPackageFragmentRoot[] copy = new IPackageFragmentRoot[sourceRoots.size()];
				sourceRoots.toArray(copy);
				return copy;
			}
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment[] getPackageFragments() throws JavaModelException {

		IPackageFragmentRoot[] roots = getPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}

	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots.
	 */
	public IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) {

		ArrayList frags = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			try {
				IJavaElement[] rootFragments = root.getChildren();
				for (int j = 0; j < rootFragments.length; j++) {
					frags.add(rootFragments[j]);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
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

		IClasspathEntry[] classpath = null;
		if (this.isOpen()) {
			JavaProjectElementInfo info = getJavaProjectElementInfo();
			classpath = info.getRawClasspath();
			if (classpath != null) {
				return classpath;
			}
			return defaultClasspath();
		}
		// if not already opened, then read from file (avoid populating the model for CP question)
		String sharedClasspath = loadClasspath();
		if (sharedClasspath != null) {
			try {
				classpath = readPaths(sharedClasspath);
			} catch (IOException e) {
			} catch (RuntimeException e) {
			}
			// extract out the output location
			if (classpath != null && classpath.length > 0) {
				IClasspathEntry entry = classpath[classpath.length - 1];
				if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
					IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
					System.arraycopy(classpath, 0, copy, 0, copy.length);
					classpath = copy;
				}
			}
		}
		if (classpath != null) {
			return classpath;
		}
		return defaultClasspath();
	}

	/**
	 * @see IJavaProject#getRequiredProjectNames
	 */
	public String[] getRequiredProjectNames() throws JavaModelException {

		return this.projectPrerequisites(getExpandedClasspath(true));
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedVariable)
		throws JavaModelException {

		return this.getResolvedClasspath(ignoreUnresolvedVariable, false);
	}

	/**
	 * Internal variant which can create marker on project for invalid entries
	 */
	public IClasspathEntry[] getResolvedClasspath(
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError)
		throws JavaModelException {

		// expanded path is cached on its info
//		IClasspathEntry[] infoPath = getJavaProjectElementInfo().lastResolvedClasspath;
//		if (infoPath != null) return infoPath;

		IClasspathEntry[] classpath = getRawClasspath();
		IClasspathEntry[] resolvedPath = classpath; // clone only if necessary
		int length = classpath.length;
		int index = 0;

		for (int i = 0; i < length; i++) {

			IClasspathEntry entry = classpath[i];

			/* validation if needed */
			if (generateMarkerOnError) {
				IJavaModelStatus status =
					JavaConventions.validateClasspathEntry(this, entry, false);
				if (!status.isOK())
					createClasspathProblemMarker(entry, status.getMessage());
			}

			/* resolve variables if any, unresolved ones are ignored */
			if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {

				// clone original path
				if (resolvedPath == classpath) {
					System.arraycopy(
						classpath,
						0,
						resolvedPath = new IClasspathEntry[length],
						0,
						i);
				}
				// resolve current variable (handling variable->variable->variable->entry
				IPath variablePath = entry.getPath(); // for error reporting
				entry = JavaCore.getResolvedClasspathEntry(entry);
				if (entry == null) {
					if (!ignoreUnresolvedVariable) {
						throw new JavaModelException(
							new JavaModelStatus(
								IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND,
								variablePath.toString()));
					}
				}
			}
			if (entry != null) {
				resolvedPath[index++] = entry;
			}
		}

		// resize resolved classpath in case some variable entries could not be resolved
		if (index != length) {
			System.arraycopy(
				resolvedPath,
				0,
				resolvedPath = new IClasspathEntry[index],
				0,
				index);
		}
//		getJavaProjectElementInfo().lastResolvedClasspath = resolvedPath;
		return resolvedPath;
	}
	
	/**
	 * This is a helper method returning the expanded classpath for the project, as a list of classpath entries, 
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * All project exports have been appended to project entries.
	 */
	public IClasspathEntry[] getExpandedClasspath(boolean ignoreUnresolvedVariable)	throws JavaModelException {
			
			return getExpandedClasspath(ignoreUnresolvedVariable, false);
	}
		
	/**
	 * Internal variant which can create marker on project for invalid entries,
	 * it will also perform classpath expansion in presence of project prerequisites
	 * exporting their entries.
	 */
	public IClasspathEntry[] getExpandedClasspath(
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError)	throws JavaModelException {

		// expanded path is cached on its info
//		IClasspathEntry[] infoPath = getJavaProjectElementInfo().lastExpandedClasspath;
//		if (infoPath != null) return infoPath;
		
		ObjectVector accumulatedEntries = new ObjectVector();		
		computeExpandedClasspath(this, ignoreUnresolvedVariable, generateMarkerOnError, new HashSet(5), accumulatedEntries);
		
		IClasspathEntry[] expandedPath = new IClasspathEntry[accumulatedEntries.size()];
		accumulatedEntries.copyInto(expandedPath);
		
//		getJavaProjectElementInfo().lastExpandedClasspath = expandedPath;
		return expandedPath;
	}

	/**
	 * Internal computation of an expanded classpath. It will eliminate duplicates, and produce copies
	 * of exported classpath entries to avoid possible side-effects ever after.
	 */			
	private void computeExpandedClasspath(
		JavaProject initialProject, 
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError,
		HashSet visitedProjects, 
		ObjectVector accumulatedEntries) throws JavaModelException {
		
		if (visitedProjects.contains(this)) return; // break cycles if any
		visitedProjects.add(this);
		
		IClasspathEntry[] immediateClasspath = getResolvedClasspath(ignoreUnresolvedVariable, generateMarkerOnError);
		for (int i = 0, length = immediateClasspath.length; i < length; i++){
			IClasspathEntry entry = immediateClasspath[i];

			boolean isInitialProject = this.equals(initialProject);
			if (isInitialProject || entry.isExported()){
				
				accumulatedEntries.add(entry);
				
				// recurse in project to get all its indirect exports (only consider exported entries from there on)				
				if (entry.getEntryKind() == ClasspathEntry.CPE_PROJECT) {
					IProject projRsc = (IProject) getWorkspace().getRoot().findMember(entry.getPath());
					if (projRsc != null && projRsc.isOpen()) {				
						JavaProject project = (JavaProject) JavaCore.create(projRsc);
						project.computeExpandedClasspath(
							initialProject, 
							ignoreUnresolvedVariable, 
							generateMarkerOnError,
							visitedProjects, 
							accumulatedEntries);
					}
				}
			}			
		}
	}
	
	/**
	 * @see IJavaProject
	 */
	public ISearchableNameEnvironment getSearchableNameEnvironment()
		throws JavaModelException {

		JavaProjectElementInfo info = getJavaProjectElementInfo();
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
		String propertyFileName = computeSharedPropertyFileName(key);
		IFile rscFile = getProject().getFile(propertyFileName);
		if (rscFile.exists()) {
			property = new String(Util.getResourceContentsAsByteArray(rscFile));
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

		if (JavaModelManager.USING_NEW_BUILDER){
			return JavaModelManager.getJavaModelManager().getLastBuiltState2(this.getProject(), null) != null;
		} else {
			return JavaModelManager.getJavaModelManager().getLastBuiltState(this.getProject(), null) != null;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasClasspathCycle(IClasspathEntry[] entries) {

		StringHashtableOfInt depthTable = new StringHashtableOfInt();
		try {
			String projectName = this.getElementName();
			depthTable.put(projectName, -2); // mark this project as being visited
			String[] prerequisites = this.projectPrerequisites(entries);
			for (int i = 0, length = prerequisites.length; i < length; i++) {
				((JavaModel) this.getJavaModel()).computeDepth(
					prerequisites[i],
					depthTable,
					projectName,
					false);
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
		} catch (JavaModelException e) {
			return true; // unsure
		}
		for (int i = 0, max = entries.length; i < max; i++) {
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Compare current classpath with given one to see if any different.
	 * Note that the argument classpath contains its binary output.
	 */
	public boolean isClasspathEqualsTo(IClasspathEntry[] otherClasspathWithOutput)
		throws JavaModelException {

		if (otherClasspathWithOutput != null && otherClasspathWithOutput.length > 0) {

			IClasspathEntry[] classpath = getRawClasspath();
			int length = otherClasspathWithOutput.length;
			if (length == classpath.length + 1) {
				// output is amongst file entries (last one)

				// compare classpath entries
				for (int i = 0; i < length - 1; i++) {
					if (!otherClasspathWithOutput[i].equals(classpath[i]))
						return false;
				}
				// compare binary outputs
				if (otherClasspathWithOutput[length - 1].getContentKind()
					== ClasspathEntry.K_OUTPUT
					&& otherClasspathWithOutput[length - 1].getPath().equals(getOutputLocation()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns the kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int kindFromString(String kindStr) {

		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
			return IClasspathEntry.CPE_PROJECT;
		if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
			return IClasspathEntry.CPE_VARIABLE;
		if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
			return IClasspathEntry.CPE_SOURCE;
		if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
			return IClasspathEntry.CPE_LIBRARY;
		if (kindStr.equalsIgnoreCase("output")) //$NON-NLS-1$
			return ClasspathEntry.K_OUTPUT;
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the kind of a class path entry.
	 */
	static String kindToString(int kind) {

		switch (kind) {
			case IClasspathEntry.CPE_PROJECT :
				return "src"; // backward compatibility //$NON-NLS-1$
			case IClasspathEntry.CPE_SOURCE :
				return "src"; //$NON-NLS-1$
			case IClasspathEntry.CPE_LIBRARY :
				return "lib"; //$NON-NLS-1$
			case IClasspathEntry.CPE_VARIABLE :
				return "var"; //$NON-NLS-1$
			case ClasspathEntry.K_OUTPUT :
				return "output"; //$NON-NLS-1$
			default :
				return "unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * load the classpath from a shareable format (VCM-wise)
	 */
	public String loadClasspath() throws JavaModelException {

		try {
			return getSharedProperty(getClasspathPropertyName());
		} catch (CoreException e) {
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
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (region == null) {
			throw new IllegalArgumentException(Util.bind("hierarchy.nullRegion"));//$NON-NLS-1$
		}
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(null, region, this, true);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (type == null) {
			throw new IllegalArgumentException(Util.bind("hierarchy.nullFocusType"));//$NON-NLS-1$
		}
		if (region == null) {
			throw new IllegalArgumentException(Util.bind("hierarchy.nullRegion"));//$NON-NLS-1$
		}
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(type, region, this, true);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * Ensures that this project is not currently being deleted before
	 * opening.
	 *
	 * fix for 1FW67PA
	 */
	protected void openWhenClosed(IProgressMonitor pm, IBuffer buffer) throws JavaModelException {

		JavaModelManager manager =
			(JavaModelManager) JavaModelManager.getJavaModelManager();
		if (manager.isBeingDeleted(fProject) || !this.fProject.isOpen()) {
			throw newNotPresentException();
		} else {
			super.openWhenClosed(pm, buffer);
		}
	}

	private String[] projectPrerequisites(IClasspathEntry[] entries)
		throws JavaModelException {
		ArrayList prerequisites = new ArrayList();
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = entries[i];
			entry = JavaCore.getResolvedClasspathEntry(entry);
			if (entry == null)
				continue; // ignore unbound variable
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				prerequisites.add(entry.getPath().lastSegment());
			}
		}
		int size = prerequisites.size();
		if (size == 0) {
			return NO_PREREQUISITES;
		} else {
			String[] result = new String[size];
			prerequisites.toArray(result);
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

		IPath projectPath = getProject().getFullPath();
		StringReader reader = new StringReader(xmlClasspath);
		Element cpElement;

		try {
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			reader.close();
			throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
		} finally {
			reader.close();
		}

		if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
			throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
		}
		NodeList list = cpElement.getChildNodes();
		ArrayList paths = new ArrayList();
		int length = list.getLength();

		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element cpeElement = (Element) node;

				if (cpeElement.getNodeName().equalsIgnoreCase("classpathentry")) { //$NON-NLS-1$
					String cpeElementKind = cpeElement.getAttribute("kind"); //$NON-NLS-1$
					String pathStr = cpeElement.getAttribute("path"); //$NON-NLS-1$
					// ensure path is absolute
					IPath path = new Path(pathStr);
					int kind = kindFromString(cpeElementKind);
					if (kind != IClasspathEntry.CPE_VARIABLE && !path.isAbsolute()) {
						path = projectPath.append(path);
					}
					// source attachment info (optional)
					String sourceAttachmentPathStr = cpeElement.getAttribute("sourcepath");	//$NON-NLS-1$
					IPath sourceAttachmentPath =
						sourceAttachmentPathStr.equals("") ? null : new Path(sourceAttachmentPathStr); //$NON-NLS-1$
					String sourceAttachmentRootPathStr = cpeElement.getAttribute("rootpath"); //$NON-NLS-1$
					IPath sourceAttachmentRootPath =
						sourceAttachmentRootPathStr.equals("") //$NON-NLS-1$
							? null
							: new Path(sourceAttachmentRootPathStr);
					
					// exported flag
					boolean isExported = cpeElement.getAttribute("exported").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$

					// recreate the CP entry
					switch (kind) {
			
						case IClasspathEntry.CPE_PROJECT :
							if (!path.isAbsolute()) return null;
							paths.add(JavaCore.newProjectEntry(path, isExported));
							break;
							
						case IClasspathEntry.CPE_LIBRARY :
							if (!path.isAbsolute()) return null;
							paths.add(JavaCore.newLibraryEntry(
															path,
															sourceAttachmentPath,
															sourceAttachmentRootPath,
															isExported));
							break;
							
						case IClasspathEntry.CPE_SOURCE :
							if (!path.isAbsolute()) return null;
							// must be an entry in this project or specify another project
							String projSegment = path.segment(0);
							if (projSegment != null && projSegment.equals(getElementName())) {
								// this project
								paths.add(JavaCore.newSourceEntry(path));
							} else {
								// another project
								paths.add(JavaCore.newProjectEntry(path, isExported));
							}
							break;
			
						case IClasspathEntry.CPE_VARIABLE :
							paths.add(JavaCore.newVariableEntry(
									path,
									sourceAttachmentPath,
									sourceAttachmentRootPath, 
									isExported));
							break;
							
						case ClasspathEntry.K_OUTPUT :
							if (!path.isAbsolute()) return null;
							paths.add(new ClasspathEntry(
									ClasspathEntry.K_OUTPUT,
									IClasspathEntry.CPE_LIBRARY,
									path,
									null,
									null,
									false));
							break;
					}
				}
			}
		}
		if (paths.size() > 0) {
			IClasspathEntry[] ips = new IClasspathEntry[paths.size()];
			paths.toArray(ips);
			return ips;
		} else {
			return null;
		}
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				getProject().setDescription(description, null);
				return;
			}
		}
	}

	/**
	 * Reset the collection of package fragment roots (local ones) - only if opened.
	 */
	public void updatePackageFragmentRoots(){
		
			if (this.isOpen()) {

				try {
					IPackageFragmentRoot[] oldRoots = getPackageFragmentRoots();
					IPackageFragmentRoot[] newRoots = computePackageFragmentRoots(false);
					checkIdentical: {
						if (oldRoots.length == newRoots.length){
							for (int i = 0, length = oldRoots.length; i < length; i++){
								if (!oldRoots[i].equals(newRoots[i])){
									break checkIdentical;
								}
							}
							return; // no need to update
						}	
					}
					JavaProjectElementInfo info = getJavaProjectElementInfo();
					info.setChildren(
						computePackageFragmentRoots(false));		
					info.setNameLookup(null); // discard name lookup (hold onto roots)
					info.setNonJavaResources(null);
				} catch(JavaModelException e){
					try {
						close(); // could not do better
					} catch(JavaModelException ex){
					}
				}
			}
	}
	
	/**
	 * Returns the <code>IResource</code> that correspond to the specified path.
	 * null if none.
	 */
	private IResource retrieveResource(IPath path, IResourceDelta delta)
		throws JavaModelException {

		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();
		IResource res = workspaceRoot.findMember(path);

		if (delta == null)
			return res;

		if (res == null) {
			// the resource has been removed or renamed
			// look for a possible delta that might help to retrieve the new resource if case of renamed
			IResourceDelta[] deltas = delta.getAffectedChildren();
			for (int i = 0, max = deltas.length; i < max; i++) {
				IResourceDelta currentDelta = deltas[i];
				if (currentDelta.getKind() == IResourceDelta.REMOVED) {
					IPath moveToPath = currentDelta.getMovedToPath();
					if (moveToPath != null) {
						res = workspaceRoot.findMember(moveToPath);
						if (res == null) {
							throw new JavaModelException(
								new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, moveToPath));
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
	 * Answers an ID which is used to distinguish project/entries during package
	 * fragment root computations
	 */
	public String rootID(){
		return "[PRJ]"+this.getProject().getFullPath(); //$NON-NLS-1$
	}
	
	/**
	 * Save the classpath in a shareable format (VCM-wise) if necessary (i.e. semantically different)
	 */
	public void saveClasspath(boolean force) throws JavaModelException {
		
		if (!getProject().exists())
			return;

		if (!isOpen())
			return; // no update for closed projects

		QualifiedName classpathProp = getClasspathPropertyName();

		try {
			// attempt to prove the classpath has not change
			String fileClasspathString = getSharedProperty(classpathProp);
			if (fileClasspathString != null) {
				IClasspathEntry[] fileEntries = readPaths(fileClasspathString);
				if (!force && isClasspathEqualsTo(fileEntries)) {
					// no need to save it, it is the same
					return;
				}
			}
		} catch (IOException e) {
		} catch (RuntimeException e) {
		} catch (CoreException e) {
		}

		// actual file saving
		try {
			setSharedProperty(
				classpathProp,
				getClasspathAsXMLString(getRawClasspath(), getOutputLocation()));
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setJavaCommand(
		IProjectDescription description,
		ICommand newCommand)
		throws CoreException {

		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldJavaCommand = getJavaCommand(description);
		ICommand[] newCommands;

		if (oldJavaCommand == null) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
			newCommands[0] = newCommand;
		} else {
			for (int i = 0, max = oldCommands.length; i < max; i++) {
				if (oldCommands[i] == oldJavaCommand) {
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
	public void setOutputLocation(IPath outputLocation, IProgressMonitor monitor)
		throws JavaModelException {

		if (outputLocation == null) {
			throw new IllegalArgumentException(Util.bind("path.nullpath")); //$NON-NLS-1$
		}
		if (outputLocation.equals(getOutputLocation())) {
			return;
		}
		this.setRawClasspath(SetClasspathOperation.ReuseClasspath, outputLocation, monitor);
	}

	/**
	 * Sets the underlying kernel project of this Java project,
	 * and fills in its parent and name.
	 * Called by IProject.getNature().
	 *
	 * @see IProjectNature#setProject
	 */
	public void setProject(IProject project) {

		fProject = project;
		fParent = JavaModelManager.getJavaModel(project.getWorkspace());
		fName = project.getName();
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(entries, SetClasspathOperation.ReuseOutputLocation, monitor, true, true, getExpandedClasspath(true));
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IPath outputLocation,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(entries, outputLocation, monitor, true, true, getExpandedClasspath(true));
	}

	public void setRawClasspath(
		IClasspathEntry[] newEntries,
		IPath newOutputLocation,
		IProgressMonitor monitor,
		boolean canChangeResource,
		boolean forceSave,
		IClasspathEntry[] oldClasspath)
		throws JavaModelException {

		JavaModelManager manager =
			(JavaModelManager) JavaModelManager.getJavaModelManager();
		try {
			JavaProjectElementInfo info = getJavaProjectElementInfo();
			IClasspathEntry[] newRawPath = newEntries;
			if (newRawPath == null) { //are we already with the default classpath
				newRawPath = defaultClasspath();
			}
			SetClasspathOperation op =
				new SetClasspathOperation(
					this, 
					oldClasspath, 
					newRawPath, 
					newOutputLocation,
					canChangeResource, 
					forceSave);
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
	protected void setRawClasspath0(IClasspathEntry[] rawEntries)
		throws JavaModelException {

		JavaProjectElementInfo info = getJavaProjectElementInfo();

		synchronized (info) {
			if (rawEntries == null) {
				rawEntries = defaultClasspath();
			}

			// clear the existing children
			info.setChildren(new IPackageFragmentRoot[] {});
			info.setRawClasspath(rawEntries);

			// compute the new roots
			updatePackageFragmentRoots();				
			
			// only trigger indexing of immediate libraries
			IndexManager indexManager =
				((JavaModelManager) JavaModelManager.getJavaModelManager()).getIndexManager();
			IPackageFragmentRoot[] immediateRoots = getPackageFragmentRoots();						
			for(int i = 0, length = immediateRoots.length; i < length; i++){
				PackageFragmentRoot root = (PackageFragmentRoot)immediateRoots[i];
				if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
					if (root.isArchive()) {
						indexManager.indexJarFile(root.getPath(), getUnderlyingResource().getName());
					} else {
						indexManager.indexBinaryFolder(
							(IFolder) root.getUnderlyingResource(),
							(IProject) this.getUnderlyingResource());
					}
				}
			}
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
	public void setSharedProperty(QualifiedName key, String value)
		throws CoreException {

		IProject project = getProject();
		String propertyName = computeSharedPropertyFileName(key);
		IFile rscFile = getProject().getFile(propertyName);
		InputStream input = new ByteArrayInputStream(value.getBytes());
		// update the resource content
		if (rscFile.exists()) {
			rscFile.setContents(input, true, false, null);
		} else {
			rscFile.create(input, true, null);
		}
	}

	public void updateClassPath(IProgressMonitor monitor, boolean canChangeResource) throws JavaModelException {

		setRawClasspath(getRawClasspath(), SetClasspathOperation.ReuseOutputLocation, monitor, canChangeResource, false, getExpandedClasspath(true));
	}

	/**
	 * Record a new marker denoting a classpath problem for a given entry
	 */
	private void createClasspathProblemMarker(
		IClasspathEntry entry,
		String message) {
			
		try {
			IMarker marker =
				getProject().createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] { IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LOCATION },
				new Object[] {
					message,
					new Integer(IMarker.SEVERITY_WARNING),
					Util.bind("classpath.buildPath")});//$NON-NLS-1$
		} catch (CoreException e) {
		}
	}

	/**
	 * Remove all markers denoting classpath problems
	 */
	protected void flushClasspathProblemMarkers() {

		try {
			IProject project = getProject();
			if (project.exists()) {
				project.deleteMarkers(
					IJavaModelMarker.BUILDPATH_PROBLEM_MARKER,
					false,
					IResource.DEPTH_ONE);
			}
		} catch (CoreException e) {
		}
	}
	/*
	 * @see IJavaProject#getClasspath()
	 * @deprecated
	 */
	public IClasspathEntry[] getClasspath() throws JavaModelException {

		return this.getRawClasspath();
	}

	/*
	 * @see IJavaProject#newLibraryEntry(IPath)
	 * @deprecated
	 */
	public IClasspathEntry newLibraryEntry(IPath path) {

		return JavaCore.newLibraryEntry(path, null, null, false);
	}

	/*
	 * @see IJavaProject#newProjectEntry(IPath)
	 * @deprecated
	 */
	public IClasspathEntry newProjectEntry(IPath path) {
		
		return JavaCore.newProjectEntry(path, false);
	}

	/*
	 * @see IJavaProject#newSourceEntry(IPath)
	 * @deprecated
	 */
	public IClasspathEntry newSourceEntry(IPath path) {
		
		return JavaCore.newSourceEntry(path);
	}

	/*
	 * @see IJavaProject#setClasspath(IClasspathEntry[], IProgressMonitor)
	 * @deprecated
	 */
	public void setClasspath(IClasspathEntry[] entries, IProgressMonitor monitor)
		throws JavaModelException {
			
		this.setRawClasspath(entries, monitor);
	}
}