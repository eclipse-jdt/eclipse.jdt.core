/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.*;
import java.util.*; 

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	 * Whether the underlying file system is case sensitive.
	 */
	protected static final boolean IS_CASE_SENSITIVE = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = new String[0];

	/**
	 * The platform project this <code>IJavaProject</code> is based on
	 */
	protected IProject fProject;
	
	/**
	 * Name of file containing project classpath
	 */
	public static final String CLASSPATH_FILENAME = ".classpath";  //$NON-NLS-1$

	/**
	 * Name of file containing custom project preferences
	 */
	public static final String PREF_FILENAME = ".jprefs";  //$NON-NLS-1$
	
	/**
	 * Value of the project's raw classpath if the .classpath file contains invalid entries.
	 */
	public static final IClasspathEntry[] INVALID_CLASSPATH = new IClasspathEntry[0];

	private static final String CUSTOM_DEFAULT_OPTION_VALUE = "#\r\n\r#custom-non-empty-default-value#\r\n\r#"; //$NON-NLS-1$
	
	/**
	 * Returns a canonicalized path from the given external path.
	 * Note that the return path contains the same number of segments
	 * and it contains a device only if the given path contained one.
	 * @see java.io.File for the definition of a canonicalized path
	 */
	public static IPath canonicalizedPath(IPath externalPath) {
		
		if (externalPath == null)
			return null;

//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonicalizing " + externalPath.toString()); //$NON-NLS-1$
//		}

		if (IS_CASE_SENSITIVE) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (file system is case sensitive)"); //$NON-NLS-1$
//			}
			return externalPath;
		}

		// if not external path, return original path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null) return externalPath; // protection during shutdown (30487)
		if (workspace.getRoot().findMember(externalPath) != null) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (member of workspace)"); //$NON-NLS-1$
//			}
			return externalPath;
		}

		IPath canonicalPath = null;
		try {
			canonicalPath =
				new Path(new File(externalPath.toOSString()).getCanonicalPath());
		} catch (IOException e) {
			// default to original path
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (IOException)"); //$NON-NLS-1$
//			}
			return externalPath;
		}
		
		IPath result;
		int canonicalLength = canonicalPath.segmentCount();
		if (canonicalLength == 0) {
			// the java.io.File canonicalization failed
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (canonical path is empty)"); //$NON-NLS-1$
//			}
			return externalPath;
		} else if (externalPath.isAbsolute()) {
			result = canonicalPath;
		} else {
			// if path is relative, remove the first segments that were added by the java.io.File canonicalization
			// e.g. 'lib/classes.zip' was converted to 'd:/myfolder/lib/classes.zip'
			int externalLength = externalPath.segmentCount();
			if (canonicalLength >= externalLength) {
				result = canonicalPath.removeFirstSegments(canonicalLength - externalLength);
			} else {
//				if (JavaModelManager.VERBOSE) {
//					System.out.println("JAVA MODEL - Canonical path is original path (canonical path is " + canonicalPath.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
//				}
				return externalPath;
			}
		}
		
		// keep device only if it was specified (this is because File.getCanonicalPath() converts '/lib/classed.zip' to 'd:/lib/classes/zip')
		if (externalPath.getDevice() == null) {
			result = result.setDevice(null);
		} 
//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonical path is " + result.toString()); //$NON-NLS-1$
//		}
		return result;
	}

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
	 * Internal computation of an expanded classpath. It will eliminate duplicates, and produce copies
	 * of exported classpath entries to avoid possible side-effects ever after.
	 */			
	private void computeExpandedClasspath(
		JavaProject initialProject, 
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError,
		HashSet visitedProjects, 
		ObjectVector accumulatedEntries) throws JavaModelException {
		
		if (visitedProjects.contains(this)){
			return; // break cycles if any
		}
		visitedProjects.add(this);

		if (generateMarkerOnError && !this.equals(initialProject)){
			generateMarkerOnError = false;
		}
		IClasspathEntry[] immediateClasspath = 
			getResolvedClasspath(ignoreUnresolvedVariable, generateMarkerOnError);
			
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0, length = immediateClasspath.length; i < length; i++){
			IClasspathEntry entry = immediateClasspath[i];

			boolean isInitialProject = this.equals(initialProject);
			if (isInitialProject || entry.isExported()){
				
				accumulatedEntries.add(entry);
				
				// recurse in project to get all its indirect exports (only consider exported entries from there on)				
				if (entry.getEntryKind() == ClasspathEntry.CPE_PROJECT) {
					IResource member = workspaceRoot.findMember(entry.getPath()); 
					if (member != null && member.getType() == IResource.PROJECT){ // double check if bound to project (23977)
						IProject projRsc = (IProject) member;
						if (JavaProject.hasJavaNature(projRsc)) {				
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
	}
	
	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(IClasspathEntry[] resolvedClasspath, boolean retrieveExportedRoots) throws JavaModelException {

		ObjectVector accumulatedRoots = new ObjectVector();
		computePackageFragmentRoots(
			resolvedClasspath, 
			accumulatedRoots, 
			new HashSet(5), // rootIDs
			true, // inside original project
			true, // check existency
			retrieveExportedRoots);
		IPackageFragmentRoot[] rootArray = new IPackageFragmentRoot[accumulatedRoots.size()];
		accumulatedRoots.copyInto(rootArray);
		return rootArray;
	}

	/**
	 * Computes the package fragment roots identified by the given entry.
	 * Only works with resolved entry
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(IClasspathEntry resolvedEntry) {
		try {
			return 
				computePackageFragmentRoots(
					new IClasspathEntry[]{ resolvedEntry }, 
					false // don't retrieve exported roots
				);
		} catch (JavaModelException e) {
			return new IPackageFragmentRoot[] {};
		}
	}
	
	/**
	 * Returns the package fragment roots identified by the given entry. In case it refers to
	 * a project, it will follow its classpath so as to find exported roots as well.
	 * Only works with resolved entry
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry resolvedEntry,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		boolean insideOriginalProject,
		boolean checkExistency,
		boolean retrieveExportedRoots) throws JavaModelException {
			
		String rootID = ((ClasspathEntry)resolvedEntry).rootID();
		if (rootIDs.contains(rootID)) return;

		IPath projectPath = getProject().getFullPath();
		IPath entryPath = resolvedEntry.getPath();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		switch(resolvedEntry.getEntryKind()){
			
			// source folder
			case IClasspathEntry.CPE_SOURCE :

				if (projectPath.isPrefixOf(entryPath)){
					if (checkExistency) {
						Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
						if (target == null) return;
	
						if (target instanceof IFolder || target instanceof IProject){
							accumulatedRoots.add(
								getPackageFragmentRoot((IResource)target));
							rootIDs.add(rootID);
						}
					} else {
						IPackageFragmentRoot root = getFolderPackageFragmentRoot(entryPath);
						if (root != null) {
							accumulatedRoots.add(root);
							rootIDs.add(rootID);
						}
					}
				}
				break;

			// internal/external JAR or folder
			case IClasspathEntry.CPE_LIBRARY :
			
				if (!insideOriginalProject && !resolvedEntry.isExported()) return;
				
				if (checkExistency) {
					Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
					if (target == null) return;
	
					if (target instanceof IResource){
						// internal target
						IResource resource = (IResource) target;
						IPackageFragmentRoot root = getPackageFragmentRoot(resource);
						if (root != null) {
							accumulatedRoots.add(root);
							rootIDs.add(rootID);
						}
					} else {
						// external target - only JARs allowed
						if (((java.io.File)target).isFile() && (Util.isArchiveFileName(entryPath.lastSegment()))) {
							accumulatedRoots.add(
								new JarPackageFragmentRoot(entryPath, this));
							rootIDs.add(rootID);
						}
					}
				} else {
					IPackageFragmentRoot root = getPackageFragmentRoot(entryPath);
					if (root != null) {
						accumulatedRoots.add(root);
						rootIDs.add(rootID);
					}
				}
				break;

			// recurse into required project
			case IClasspathEntry.CPE_PROJECT :

				if (!retrieveExportedRoots) return;
				if (!insideOriginalProject && !resolvedEntry.isExported()) return;

				IResource member = workspaceRoot.findMember(entryPath);
				if (member != null && member.getType() == IResource.PROJECT){// double check if bound to project (23977)
					IProject requiredProjectRsc = (IProject) member;
					if (JavaProject.hasJavaNature(requiredProjectRsc)){ // special builder binary output
						rootIDs.add(rootID);
						JavaProject requiredProject = (JavaProject)JavaCore.create(requiredProjectRsc);
						requiredProject.computePackageFragmentRoots(
							requiredProject.getResolvedClasspath(true), 
							accumulatedRoots, 
							rootIDs, 
							false, 
							checkExistency, 
							retrieveExportedRoots);
					}
				break;
			}
		}
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry[] resolvedClasspath,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		boolean insideOriginalProject,
		boolean checkExistency,
		boolean retrieveExportedRoots) throws JavaModelException {

		if (insideOriginalProject){
			rootIDs.add(rootID());
		}	
		for (int i = 0, length = resolvedClasspath.length; i < length; i++){
			computePackageFragmentRoots(
				resolvedClasspath[i],
				accumulatedRoots,
				rootIDs,
				insideOriginalProject,
				checkExistency,
				retrieveExportedRoots);
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
	}
	/*
	 * Returns whether the given resource is accessible through the children or the non-Java resources of this project.
	 * Returns true if the resource is not in the project.
	 * Assumes that the resource is a folder or a file.
	 */
	public boolean contains(IResource resource) {
			
		IClasspathEntry[] classpath;
		IPath output;
		try {
			classpath = getResolvedClasspath(true);
			output = getOutputLocation();
		} catch (JavaModelException e) {
			return false;
		}
		
		IPath fullPath = resource.getFullPath();
		IPath innerMostOutput = output.isPrefixOf(fullPath) ? output : null;
		IClasspathEntry innerMostEntry = null;
		for (int j = 0, cpLength = classpath.length; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
		
			IPath entryPath = entry.getPath();
			if ((innerMostEntry == null || innerMostEntry.getPath().isPrefixOf(entryPath))
					&& entryPath.isPrefixOf(fullPath)) {
				innerMostEntry = entry;
			}
			IPath entryOutput = classpath[j].getOutputLocation();
			if (entryOutput != null && entryOutput.isPrefixOf(fullPath)) {
				innerMostOutput = entryOutput;
			}
		}
		if (innerMostEntry != null) {
			// special case prj==src and nested output location
			if (innerMostOutput != null && innerMostOutput.segmentCount() > 1 // output isn't project
					&& innerMostEntry.getPath().segmentCount() == 1) { // 1 segment must be project name
				return false;
			}
			if  (resource instanceof IFolder) {
				 // folders are always included in src/lib entries
				 return true;
			}
			switch (innerMostEntry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE:
					// .class files are not visible in source folders 
					return !Util.isClassFileName(fullPath.lastSegment());
				case IClasspathEntry.CPE_LIBRARY:
					// .java files are not visible in library folders
					return !Util.isJavaFileName(fullPath.lastSegment());
			}
		}
		if (innerMostOutput != null) {
			return false;
		}
		return true;
	}

	/**
	 * Record a new marker denoting a classpath problem
	 */
	IMarker createClasspathProblemMarker(IJavaModelStatus status) {
			
		IMarker marker = null;
		int severity;
		String[] arguments = new String[0];
		boolean isCycleProblem = false, isClasspathFileFormatProblem = false;
		switch (status.getCode()) {

			case  IJavaModelStatusConstants.CLASSPATH_CYCLE :
				isCycleProblem = true;
				if (JavaCore.ERROR.equals(getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true))) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;

			case  IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT :
				isClasspathFileFormatProblem = true;
				severity = IMarker.SEVERITY_ERROR;
				break;

			default:
				IPath path = status.getPath();
				if (path != null) arguments = new String[] { path.toString() };
				if (JavaCore.ERROR.equals(getOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, true))) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;
		}
		
		try {
			marker = getProject().createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] { 
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IMarker.LOCATION, 
					IJavaModelMarker.CYCLE_DETECTED,
					IJavaModelMarker.CLASSPATH_FILE_FORMAT,
					IJavaModelMarker.ID,
					IJavaModelMarker.ARGUMENTS ,
				},
				new Object[] {
					status.getMessage(),
					new Integer(severity), 
					Util.bind("classpath.buildPath"),//$NON-NLS-1$
					isCycleProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					isClasspathFileFormatProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					new Integer(status.getCode()),
					Util.getProblemArgumentsForMarker(arguments) ,
				}
			);
		} catch (CoreException e) {
		}
		return marker;
	}
	
	/**
	 * Returns a new element info for this element.
	 */
	protected OpenableElementInfo createElementInfo() {

		return new JavaProjectElementInfo();
	}

	/**
	 * Reads and decode an XML classpath string
	 */
	protected IClasspathEntry[] decodeClasspath(String xmlClasspath, boolean createMarker, boolean logProblems) {

		ArrayList paths = new ArrayList();
		IClasspathEntry defaultOutput = null;
		try {
			if (xmlClasspath == null) return null;
			StringReader reader = new StringReader(xmlClasspath);
			Element cpElement;
	
			try {
				DocumentBuilder parser =
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
				cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
			} catch (SAXException e) {
				throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
			} finally {
				reader.close();
			}
	
			if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
				throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
			}
			NodeList list = cpElement.getElementsByTagName("classpathentry"); //$NON-NLS-1$
			int length = list.getLength();
	
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					IClasspathEntry entry = ClasspathEntry.elementDecode((Element)node, this);
					if (entry != null){
						if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) { 
							defaultOutput = entry; // separate output
						} else {
							paths.add(entry);
				}
			}
				}
			}
		} catch (IOException e) {
			// bad format
			if (createMarker && this.getProject().isAccessible()) {
					this.createClasspathProblemMarker(new JavaModelStatus(
							IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
							Util.bind("classpath.xmlFormatError", this.getElementName(), e.getMessage()))); //$NON-NLS-1$
			}
			if (logProblems) {
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will mark classpath as invalid"); //$NON-NLS-1$
			}
			return INVALID_CLASSPATH;
		} catch (Assert.AssertionFailedException e) { 
			// failed creating CP entries from file
			if (createMarker && this.getProject().isAccessible()) {
				this.createClasspathProblemMarker(new JavaModelStatus(
						IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
						Util.bind("classpath.illegalEntryInClasspathFile", this.getElementName(), e.getMessage()))); //$NON-NLS-1$
			}
			if (logProblems) {
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will mark classpath as invalid"); //$NON-NLS-1$
			}
			return INVALID_CLASSPATH;
		}
		int pathSize = paths.size();
		if (pathSize > 0 || defaultOutput != null) {
			IClasspathEntry[] entries = new IClasspathEntry[pathSize + (defaultOutput == null ? 0 : 1)];
			paths.toArray(entries);
			if (defaultOutput != null) entries[pathSize] = defaultOutput; // ensure output is last item
			return entries;
		} else {
			return null;
		}
	}

	/**
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
	 * Returns the XML String encoding of the class path.
	 */
	protected String encodeClasspath(IClasspathEntry[] classpath, IPath outputLocation, boolean useLineSeparator) throws JavaModelException {

		Document document = new DocumentImpl();
		Element cpElement = document.createElement("classpath"); //$NON-NLS-1$
		document.appendChild(cpElement);

		for (int i = 0; i < classpath.length; ++i) {
			cpElement.appendChild(((ClasspathEntry)classpath[i]).elementEncode(document, getProject().getFullPath()));
		}

		if (outputLocation != null) {
			outputLocation = outputLocation.removeFirstSegments(1);
			outputLocation = outputLocation.makeRelative();
			Element oElement = document.createElement("classpathentry"); //$NON-NLS-1$
			oElement.setAttribute("kind", ClasspathEntry.kindToString(ClasspathEntry.K_OUTPUT));	//$NON-NLS-1$
			oElement.setAttribute("path", outputLocation.toString()); //$NON-NLS-1$
			cpElement.appendChild(oElement);
		}

		// produce a String output
		try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			OutputFormat format = new OutputFormat();
			if (useLineSeparator) {
				format.setIndenting(true);
				format.setLineSeparator(System.getProperty("line.separator"));  //$NON-NLS-1$
			} else {
				format.setPreserveSpace(true);
			}			
			Serializer serializer =
				SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
					new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
					format);
			serializer.asDOMSerializer().serialize(document);
			return s.toString("UTF8"); //$NON-NLS-1$
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
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

	public boolean exists() {
		if (!hasJavaNature(fProject)) return false;
		return super.exists();
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

		return findPackageFragment0(JavaProject.canonicalizedPath(path));
	}

	/**
	 * non path canonicalizing version
	 */
	public IPackageFragment findPackageFragment0(IPath path) 
		throws JavaModelException {

		return getNameLookup().findPackageFragment(path);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaModelException {

		return findPackageFragmentRoot0(JavaProject.canonicalizedPath(path));
	}

	/**
	 * no path canonicalization 
	 */
	public IPackageFragmentRoot findPackageFragmentRoot0(IPath path)
		throws JavaModelException {

		IPackageFragmentRoot[] allRoots = this.getAllPackageFragmentRoots();
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
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry) {
		try {
			IClasspathEntry[] classpath = this.getRawClasspath();
			for (int i = 0, length = classpath.length; i < length; i++) {
				if (classpath[i].equals(entry)) { // entry may need to be resolved
					return 
						computePackageFragmentRoots(
							getResolvedClasspath(new IClasspathEntry[] {entry}, null, true, false, null/*no reverse map*/), 
							false); // don't retrieve exported roots
				}
			}
		} catch (JavaModelException e) {
		}
		return new IPackageFragmentRoot[] {};
	}
	
	/**
	 * @see IJavaProject#findType(String)
	 */
	public IType findType(String fullyQualifiedName) throws JavaModelException {
		IType type = 
			this.getNameLookup().findType(
				fullyQualifiedName, 
				false,
				NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
		if (type == null) {
			// try to find enclosing type
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) return null;
			type = this.findType(fullyQualifiedName.substring(0, lastDot));
			if (type != null) {
				type = type.getType(fullyQualifiedName.substring(lastDot+1));
				if (!type.exists()) {
					return null;
				}
			}
		}
		return type;
	}
	
	/**
	 * @see IJavaProject#findType(String, String)
	 */
	public IType findType(String packageName, String typeQualifiedName) throws JavaModelException {
		return 
			this.getNameLookup().findType(
				typeQualifiedName, 
				packageName,
				false,
				NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
	}	
	
	/**
	 * Remove all markers denoting classpath problems
	 */
	protected void flushClasspathProblemMarkers(boolean flushCycleMarkers, boolean flushClasspathFormatMarkers) {
		try {
			IProject project = getProject();
			if (project.exists()) {
				IMarker[] markers = project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					if (flushCycleMarkers && flushClasspathFormatMarkers) {
						marker.delete();
					} else {
						String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
						String classpathFileFormatAttr =  (String)marker.getAttribute(IJavaModelMarker.CLASSPATH_FILE_FORMAT);
						if ((flushCycleMarkers == (cycleAttr != null && cycleAttr.equals("true"))) //$NON-NLS-1$
							&& (flushClasspathFormatMarkers == (classpathFileFormatAttr != null && classpathFileFormatAttr.equals("true")))){ //$NON-NLS-1$
							marker.delete();
						}
					}
				}
			}
		} catch (CoreException e) {
		}
	}

	/**
	 * @see Openable
	 */
	protected boolean generateInfos(
		OpenableElementInfo info,
		IProgressMonitor pm,
		Map newElements,
		IResource underlyingResource) throws JavaModelException {

		boolean validInfo = false;
		try {
			if (getProject().isOpen()) {
				// put the info now, because computing the roots requires it
				JavaModelManager.getJavaModelManager().putInfo(this, info);

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot wRoot = workspace.getRoot();
				// request marker refresh on project opening (so as to diagnose unbound variables on startup)
				IClasspathEntry[] resolvedClasspath = getResolvedClasspath(true/*ignore unresolved variable*/, !workspace.isTreeLocked()  /*refresh CP markers*/);

				// compute the pkg fragment roots (resolved CP should already be cached from marker refresh)
				updatePackageFragmentRoots();				
	
				// remember the timestamps of external libraries the first time they are looked up
				for (int i = 0, length = resolvedClasspath.length; i < length; i++) {
					IClasspathEntry entry = resolvedClasspath[i];
					if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						IPath path = entry.getPath();
						Object target = JavaModel.getTarget(wRoot, path, true);
						if (target instanceof java.io.File) {
							Map externalTimeStamps = JavaModelManager.getJavaModelManager().deltaProcessor.externalTimeStamps;
							if (externalTimeStamps.get(path) == null) {
								long timestamp = DeltaProcessor.getTimeStamp((java.io.File)target);
								externalTimeStamps.put(path, new Long(timestamp));							
							}
						}
					}
				}			

				// only valid if reaches here				
				validInfo = true;
			}
		} finally {
			if (!validInfo)
				JavaModelManager.getJavaModelManager().removeInfo(this);
		}
		return validInfo;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots()
		throws JavaModelException {

		return computePackageFragmentRoots(getResolvedClasspath(true), true);
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

	/*
	 * Returns the cycle marker associated with this project or null if none.
	 */
	public IMarker getCycleMarker(){
		try {
			IProject project = getProject();
			if (project.exists()) {
				IMarker[] markers = project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
					if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
						return marker;
					}
				}
			}
		} catch (CoreException e) {
		}
		return null;
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
		boolean generateMarkerOnError) throws JavaModelException {
	
		ObjectVector accumulatedEntries = new ObjectVector();		
		computeExpandedClasspath(this, ignoreUnresolvedVariable, generateMarkerOnError, new HashSet(5), accumulatedEntries);
		
		IClasspathEntry[] expandedPath = new IClasspathEntry[accumulatedEntries.size()];
		accumulatedEntries.copyInto(expandedPath);

		return expandedPath;
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
		// lock on the project info to avoid race condition
		synchronized(info){
			NameLookup nameLookup;
			if ((nameLookup = info.getNameLookup()) == null){
				info.setNameLookup(nameLookup = new NameLookup(this));
			}
			return nameLookup;
		}
	}

	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {

		return ((JavaProjectElementInfo) getElementInfo()).getNonJavaResources(this);
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#getOption(String, boolean)
	 */	
	public String getOption(String optionName, boolean inheritJavaCoreOptions) {
		
		if (JavaModelManager.OptionNames.contains(optionName)){
			
			Preferences preferences = getPreferences();
			if (preferences == null || preferences.isDefault(optionName)) {
				return inheritJavaCoreOptions ? JavaCore.getOption(optionName) : null;
			}
			return preferences.getString(optionName).trim();
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.core.IJavaProject#getOptions(boolean)
	 */
	public Map getOptions(boolean inheritJavaCoreOptions) {
		
		// initialize to the defaults from JavaCore options pool
		Map options = inheritJavaCoreOptions ? JavaCore.getOptions() : new Hashtable(5);

		Preferences preferences = getPreferences();
		if (preferences == null) return options; // cannot do better (non-Java project)
		HashSet optionNames = JavaModelManager.OptionNames;
		
		// get preferences set to their default
		if (inheritJavaCoreOptions){
			String[] defaultPropertyNames = preferences.defaultPropertyNames();
			for (int i = 0; i < defaultPropertyNames.length; i++){
				String propertyName = defaultPropertyNames[i];
				if (optionNames.contains(propertyName)){
					options.put(propertyName, preferences.getDefaultString(propertyName).trim());
				}
			}		
		}
		// get custom preferences not set to their default
		String[] propertyNames = preferences.propertyNames();
		for (int i = 0; i < propertyNames.length; i++){
			String propertyName = propertyNames[i];
			if (optionNames.contains(propertyName)){
				options.put(propertyName, preferences.getString(propertyName).trim());
			}
		}		
		return options;
	}
	
	/**
	 * @see IJavaProject
	 */
	public IPath getOutputLocation() throws JavaModelException {

		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(fProject);
		IPath outputLocation = perProjectInfo.outputLocation;
		if (outputLocation != null) return outputLocation;

		// force to read classpath - will position output location as well
		this.getRawClasspath();
		outputLocation = perProjectInfo.outputLocation;
		if (outputLocation == null) {
			return defaultOutputLocation();
		}
		return outputLocation;
	}

	/**
	 * @return A handle to the package fragment root identified by the given path.
	 * This method is handle-only and the element may or may not exist. Returns
	 * <code>null</code> if unable to generate a handle from the path (for example,
	 * an absolute path that has less than 1 segment. The path may be relative or
	 * absolute.
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IPath path) {
		if (!path.isAbsolute()) {
			path = getPath().append(path);
		}
		int segmentCount = path.segmentCount();
		switch (segmentCount) {
			case 0:
				return null;
			case 1:
				// default root
				return getPackageFragmentRoot(getProject());
			default:
				// a path ending with .jar/.zip is still ambiguous and could still resolve to a source/lib folder 
				// thus will try to guess based on existing resource
				if (Util.isArchiveFileName(path.lastSegment())) {
					IResource resource = getProject().getWorkspace().getRoot().findMember(path); 
					if (resource != null && resource.getType() == IResource.FOLDER){
						return getPackageFragmentRoot(resource);
					}
					return getPackageFragmentRoot0(path);
				} else {
					return getPackageFragmentRoot(getProject().getWorkspace().getRoot().getFolder(path));
				}
		}
	}

	/**
	 * The path is known to match a source/library folder entry.
	 */
	public IPackageFragmentRoot getFolderPackageFragmentRoot(IPath path) {
		if (path.segmentCount() == 1) { // default project root
			return getPackageFragmentRoot(getProject());
		}
		return getPackageFragmentRoot(getProject().getWorkspace().getRoot().getFolder(path));
	}
	
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {

		switch (resource.getType()) {
			case IResource.FILE:
				if (Util.isArchiveFileName(resource.getName())) {
					return new JarPackageFragmentRoot(resource, this);
				} else {
					return null;
				}
			case IResource.FOLDER:
				return new PackageFragmentRoot(resource, this, resource.getName());
			case IResource.PROJECT:
				return new PackageFragmentRoot(resource, this, ""); //$NON-NLS-1$
			default:
				return null;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(String jarPath) {

		return getPackageFragmentRoot0(JavaProject.canonicalizedPath(new Path(jarPath)));
	}
	
	/**
	 * no path canonicalization
	 */
	public IPackageFragmentRoot getPackageFragmentRoot0(IPath jarPath) {

		return new JarPackageFragmentRoot(jarPath, this);
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
	 * @see IJavaProject
	 * @deprecated
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
		return findPackageFragmentRoots(entry);
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
	
	/*
	 * @see IJavaElement
	 */
	public IPath getPath() {
		return this.getProject().getFullPath();
	}
	
	/**
	 * @see IJavaProject
	 */
	public IProject getProject() {

		return fProject;
	}

	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 */	
	public Preferences getPreferences(){
		IProject project = getProject();
		if (!JavaProject.hasJavaNature(project)) return null;
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(project, true);
		Preferences preferences =  perProjectInfo.preferences;
		if (preferences != null) return preferences;
		preferences = loadPreferences();
		if (preferences == null) preferences = new Preferences();
		perProjectInfo.preferences = preferences;
		return preferences;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getRawClasspath() throws JavaModelException {

		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(fProject);
		IClasspathEntry[] classpath = perProjectInfo.classpath;
		if (classpath != null) return classpath;
		classpath = this.readClasspathFile(false/*don't create markers*/, true/*log problems*/);
		
		// extract out the output location
		IPath outputLocation = null;
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getPath();
				IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, copy, 0, copy.length);
				classpath = copy;
			}
		}
		if (classpath == null) {
			return defaultClasspath();
		}
		/* Disable validate: classpath can contain CP variables and container that need to be resolved 
		if (classpath != INVALID_CLASSPATH
				&& !JavaConventions.validateClasspath(this, classpath, outputLocation).isOK()) {
			classpath = INVALID_CLASSPATH;
		}
		*/
		perProjectInfo.classpath = classpath;
		perProjectInfo.outputLocation = outputLocation;
		return classpath;
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
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry)
		throws JavaModelException {

		return 
			this.getResolvedClasspath(
				ignoreUnresolvedEntry, 
				false); // generateMarkerOnError
	}

	/**
	 * Internal variant which can create marker on project for invalid entries
	 * and caches the resolved classpath on perProjectInfo
	 */
	public IClasspathEntry[] getResolvedClasspath(
		boolean ignoreUnresolvedEntry,
		boolean generateMarkerOnError)
		throws JavaModelException {

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		JavaModelManager.PerProjectInfo perProjectInfo = manager.getPerProjectInfoCheckExistence(fProject);
		
		// reuse cache if not needing to refresh markers or checking bound variables
		if (ignoreUnresolvedEntry && !generateMarkerOnError && perProjectInfo != null){
			// resolved path is cached on its info
			IClasspathEntry[] infoPath = perProjectInfo.lastResolvedClasspath;
			if (infoPath != null) return infoPath;
		}
		Map reverseMap = perProjectInfo == null ? null : new HashMap(5);
		IClasspathEntry[] resolvedPath = getResolvedClasspath(
			getRawClasspath(), 
			generateMarkerOnError ? getOutputLocation() : null, 
			ignoreUnresolvedEntry, 
			generateMarkerOnError,
			reverseMap);

		if (perProjectInfo != null){
			if (perProjectInfo.classpath == null // .classpath file could not be read
				&& generateMarkerOnError 
				&& JavaProject.hasJavaNature(fProject)) {
					this.createClasspathProblemMarker(new JavaModelStatus(
						IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
						Util.bind("classpath.cannotReadClasspathFile", this.getElementName()))); //$NON-NLS-1$
				}

			perProjectInfo.lastResolvedClasspath = resolvedPath;
			perProjectInfo.resolvedPathToRawEntries = reverseMap;
		}
		return resolvedPath;
	}
	
	/**
	 * Internal variant which can process any arbitrary classpath
	 */
	public IClasspathEntry[] getResolvedClasspath(
		IClasspathEntry[] classpathEntries,
		IPath projectOutputLocation, // only set if needing full classpath validation (and markers)
		boolean ignoreUnresolvedEntry, // if unresolved entries are met, should it trigger initializations
		boolean generateMarkerOnError,
		Map reverseMap) // can be null if not interested in reverse mapping
		throws JavaModelException {

		IJavaModelStatus status;
		if (generateMarkerOnError){
			flushClasspathProblemMarkers(false, false);
		}

		int length = classpathEntries.length;
		ArrayList resolvedEntries = new ArrayList();
		
		for (int i = 0; i < length; i++) {

			IClasspathEntry rawEntry = classpathEntries[i];
			IPath resolvedPath;
			status = null;
			
			/* validation if needed */
			if (generateMarkerOnError || !ignoreUnresolvedEntry) {
				status = JavaConventions.validateClasspathEntry(this, rawEntry, false);
				if (generateMarkerOnError && !status.isOK()) createClasspathProblemMarker(status);
			}

			switch (rawEntry.getEntryKind()){
				
				case IClasspathEntry.CPE_VARIABLE :
				
					IClasspathEntry resolvedEntry = JavaCore.getResolvedClasspathEntry(rawEntry);
					if (resolvedEntry == null) {
						if (!ignoreUnresolvedEntry) throw new JavaModelException(status);
					} else {
						if (reverseMap != null && reverseMap.get(resolvedPath = resolvedEntry.getPath()) == null) reverseMap.put(resolvedPath , rawEntry);
						resolvedEntries.add(resolvedEntry);
					}
					break; 

				case IClasspathEntry.CPE_CONTAINER :
				
					IClasspathContainer container = JavaCore.getClasspathContainer(rawEntry.getPath(), this);
					if (container == null){
						if (!ignoreUnresolvedEntry) throw new JavaModelException(status);
						break;
					}

					IClasspathEntry[] containerEntries = container.getClasspathEntries();
					if (containerEntries == null) break;

					// container was bound
					for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
						IClasspathEntry cEntry = containerEntries[j];
						
						if (generateMarkerOnError) {
							IJavaModelStatus containerStatus = JavaConventions.validateClasspathEntry(this, cEntry, false);
							if (!containerStatus.isOK()) createClasspathProblemMarker(containerStatus);
						}
						// if container is exported, then its nested entries must in turn be exported  (21749)
						if (rawEntry.isExported()){
							cEntry = new ClasspathEntry(cEntry.getContentKind(),
								cEntry.getEntryKind(), cEntry.getPath(),
								cEntry.getExclusionPatterns(), cEntry.getSourceAttachmentPath(),
								cEntry.getSourceAttachmentRootPath(), cEntry.getOutputLocation(), 
								true); // duplicate container entry for tagging it as exported
						}
						if (reverseMap != null && reverseMap.get(resolvedPath = cEntry.getPath()) == null) reverseMap.put(resolvedPath, rawEntry);
						resolvedEntries.add(cEntry);
					}
					break;
										
				default :

					if (reverseMap != null && reverseMap.get(resolvedPath = rawEntry.getPath()) == null) reverseMap.put(resolvedPath, rawEntry);
					resolvedEntries.add(rawEntry);
				
			}					
		}

		IClasspathEntry[] resolvedPath = new IClasspathEntry[resolvedEntries.size()];
		resolvedEntries.toArray(resolvedPath);

		if (generateMarkerOnError && projectOutputLocation != null) {
			status = JavaConventions.validateClasspath(this, resolvedPath, projectOutputLocation);
			if (!status.isOK()) createClasspathProblemMarker(status);
		}
		return resolvedPath;
	}

	/*
	 * @see IJavaElement
	 */
	public IResource getResource() {
		return this.getProject();
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
	 * @see JavaProject#setSharedProperty(String, String)
	 */
	public String getSharedProperty(String key) throws CoreException {

		String property = null;
		IFile rscFile = getProject().getFile(key);
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
		if (!exists()) throw newNotPresentException();
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
	public boolean hasClasspathCycle(IClasspathEntry[] preferredClasspath) {
		HashSet visited = new HashSet();
		visited.add(getElementName());
		return hasClasspathCycle(preferredClasspath, visited, ResourcesPlugin.getWorkspace().getRoot());
	}
	
	private boolean hasClasspathCycle(IClasspathEntry[] preferredClasspath, HashSet visited, IWorkspaceRoot workspaceRoot) {
		try {
			IClasspathEntry[] classpath = preferredClasspath == null ? getResolvedClasspath(true) : preferredClasspath;
	
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry;
				if ((entry = classpath[i]).getEntryKind() == IClasspathEntry.CPE_PROJECT){
					IPath entryPath = entry.getPath();
					IResource member = workspaceRoot.findMember(entryPath);
					if (member != null && member.getType() == IResource.PROJECT){
						String projectName = entryPath.lastSegment();
						if (!visited.add(projectName)) return true;
						JavaProject project = (JavaProject)JavaCore.create((IProject)member);
						if (project.hasClasspathCycle(null, visited, workspaceRoot)) return true;
						visited.remove(projectName);
					}
				}
			}
		} catch(JavaModelException e){
		}
		return false;
	}
	
	public boolean hasCycleMarker(){
		return this.getCycleMarker() != null;
	}

	public int hashCode() {
		return fProject.hashCode();
	}

	/**
	 * Returns true if the given project is accessible and it has
	 * a java nature, otherwise false.
	 */
	public static boolean hasJavaNature(IProject project) { 
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			// project does not exist or is not open
		}
		return false;
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
	public boolean isClasspathEqualsTo(IClasspathEntry[] newClasspath, IPath newOutputLocation, IClasspathEntry[] otherClasspathWithOutput)
		throws JavaModelException {

		if (otherClasspathWithOutput != null && otherClasspathWithOutput.length > 0) {

			int length = otherClasspathWithOutput.length;
			if (length == newClasspath.length + 1) {
				// output is amongst file entries (last one)

				// compare classpath entries
				for (int i = 0; i < length - 1; i++) {
					if (!otherClasspathWithOutput[i].equals(newClasspath[i]))
						return false;
				}
				// compare binary outputs
				IClasspathEntry output = otherClasspathWithOutput[length - 1];
				if (output.getContentKind() == ClasspathEntry.K_OUTPUT
						&& output.getPath().equals(newOutputLocation))
					return true;
			}
		}
		return false;
	}
	

	
	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IJavaElement element) {
		IPath path = element.getPath();
		switch (element.getElementType()) {
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				if (!((IPackageFragmentRoot)element).isArchive()) {
					// ensure that folders are only excluded if all of their children are excluded
					path = path.append("*"); //$NON-NLS-1$
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				if (!((IPackageFragmentRoot)element.getParent()).isArchive()) {
					// ensure that folders are only excluded if all of their children are excluded
					path = path.append("*"); //$NON-NLS-1$
				}
				break;
		}
		return this.isOnClasspath(path);
	}
	private boolean isOnClasspath(IPath path) {
		IClasspathEntry[] classpath;
		try {
			classpath = this.getResolvedClasspath(true/*ignore unresolved variable*/);
		} catch(JavaModelException e){
			return false; // not a Java project
		}
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getPath().isPrefixOf(path) 
					&& !Util.isExcluded(path, ((ClasspathEntry)entry).fullExclusionPatternChars())) {
				return true;
			}
		}
		return false;
	}
	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IResource resource) {
		IPath path = resource.getFullPath();
		
		// ensure that folders are only excluded if all of their children are excluded
		if (resource.getType() == IResource.FOLDER) {
			path = path.append("*"); //$NON-NLS-1$
		}
		
		return this.isOnClasspath(path);
	}


	/*
	 * load preferences from a shareable format (VCM-wise)
	 */
	 public Preferences loadPreferences() {
	 	
	 	Preferences preferences = new Preferences();
	 	
//		File prefFile = getProject().getLocation().append(PREF_FILENAME).toFile();
		IPath projectMetaLocation = getProject().getPluginWorkingLocation(JavaCore.getPlugin().getDescriptor());
		if (projectMetaLocation != null) {
			File prefFile = projectMetaLocation.append(PREF_FILENAME).toFile();
			if (prefFile.exists()) { // load preferences from file
				InputStream in = null;
				try {
					in = new BufferedInputStream(new FileInputStream(prefFile));
					preferences.load(in);
					return preferences;
				} catch (IOException e) { // problems loading preference store - quietly ignore
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) { // ignore problems with close
						}
					}
				}
			}
		}
		return null;
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
	 * Open project if resource isn't closed
	 */
	protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {

		if (!this.fProject.isOpen()) {
			throw newNotPresentException();
		} else {
			super.openWhenClosed(pm);
		}
	}

	public String[] projectPrerequisites(IClasspathEntry[] entries)
		throws JavaModelException {
			
		ArrayList prerequisites = new ArrayList();
		// need resolution
		entries = getResolvedClasspath(entries, null, true, false, null/*no reverse map*/);
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = entries[i];
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
	 * Reads the .classpath file from disk and returns the list of entries it contains (including output location entry)
	 * Returns null if .classfile is not present.
	 * Returns INVALID_CLASSPATH if it has a format problem.
	 */
	protected IClasspathEntry[] readClasspathFile(boolean createMarker, boolean logProblems) {

		try {
			String xmlClasspath = getSharedProperty(CLASSPATH_FILENAME);
			if (xmlClasspath == null) return null;
			return decodeClasspath(xmlClasspath, createMarker, logProblems);
		} catch(CoreException e) {
			// file does not exist (or not accessible)
			if (createMarker && this.getProject().isAccessible()) {
					this.createClasspathProblemMarker(new JavaModelStatus(
						IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
						Util.bind("classpath.cannotReadClasspathFile", this.getElementName()))); //$NON-NLS-1$
			}
			if (logProblems) {
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will revert to default classpath"); //$NON-NLS-1$
			}
		}
		return null;
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
	 * @see JavaElement#rootedAt(IJavaProject)
	 */
	public IJavaElement rootedAt(IJavaProject project) {
		return project;
	
	}
	
	/**
	 * Answers an ID which is used to distinguish project/entries during package
	 * fragment root computations
	 */
	public String rootID(){
		return "[PRJ]"+this.getProject().getFullPath(); //$NON-NLS-1$
	}
	
	/**
	 * Saves the classpath in a shareable format (VCM-wise) only when necessary, that is, if  it is semantically different
	 * from the existing one in file. Will never write an identical one.
	 * 
	 * @return Return whether the .classpath file was modified.
	 */
	public boolean saveClasspath(IClasspathEntry[] newClasspath, IPath newOutputLocation) throws JavaModelException {

		if (!getProject().exists()) return false;

		IClasspathEntry[] fileEntries = readClasspathFile(false /*don't create markers*/, false/*don't log problems*/);
		if (fileEntries != null && isClasspathEqualsTo(newClasspath, newOutputLocation, fileEntries)) {
			// no need to save it, it is the same
			return false;
		}

		// actual file saving
		try {
			setSharedProperty(CLASSPATH_FILENAME, encodeClasspath(newClasspath, newOutputLocation, true));
			return true;
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Save project custom preferences to shareable file (.jprefs)
	 */
	private void savePreferences(Preferences preferences) {
		
		IProject project = getProject();
		if (!JavaProject.hasJavaNature(project)) return; // ignore
		
		if (preferences == null || (!preferences.needsSaving() && preferences.propertyNames().length != 0)) {
			// nothing to save
			return;
		}
	
		// preferences need to be saved
		// the preferences file is located in the plug-in's state area
		// at a well-known name (.jprefs)
//		File prefFile = getProject().getLocation().append(PREF_FILENAME).toFile();
		File prefFile = project.getPluginWorkingLocation(JavaCore.getPlugin().getDescriptor()).append(PREF_FILENAME).toFile();
		if (preferences.propertyNames().length == 0) {
			// there are no preference settings
			// rather than write an empty file, just delete any existing file
			if (prefFile.exists()) {
				prefFile.delete(); // don't worry if delete unsuccessful
			}
			return;
		}
		
		// write file, overwriting an existing one
		OutputStream out = null;
		try {
			// do it as carefully as we know how so that we don't lose/mangle
			// the setting in times of stress
			out = new BufferedOutputStream(new FileOutputStream(prefFile));
			preferences.store(out, null);
		} catch (IOException e) { // problems saving preference store - quietly ignore
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) { // ignore problems with close
				}
			}
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
	 * @see org.eclipse.jdt.core.IJavaProject#setOptions(Map)
	 */
	public void setOptions(Map newOptions) {

		Preferences preferences;
		setPreferences(preferences = new Preferences()); // always reset (26255)
		if (newOptions != null){
			Iterator keys = newOptions.keySet().iterator();
			while (keys.hasNext()){
				String key = (String)keys.next();
				if (!JavaModelManager.OptionNames.contains(key)) continue; // unrecognized option
				// no filtering for encoding (custom encoding for project is allowed)
				String value = (String)newOptions.get(key);
				preferences.setDefault(key, CUSTOM_DEFAULT_OPTION_VALUE); // empty string isn't the default (26251)
				preferences.setValue(key, value);
			}
		}
		
		// persist options
		savePreferences(preferences);	
	}

	/**
	 * @see IJavaProject
	 */
	public void setOutputLocation(IPath path, IProgressMonitor monitor)
		throws JavaModelException {

		if (path == null) {
			throw new IllegalArgumentException(Util.bind("path.nullpath")); //$NON-NLS-1$
		}
		if (path.equals(getOutputLocation())) {
			return;
		}
		this.setRawClasspath(SetClasspathOperation.ReuseClasspath, path, monitor);
	}

	/*
	 * Set cached preferences, no preference file is saved, only info is updated
	 */
	public void setPreferences(Preferences preferences) {
		IProject project = getProject();
		if (!JavaProject.hasJavaNature(project)) return; // ignore
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(project, true);
		perProjectInfo.preferences = preferences;
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
		fParent = JavaModelManager.getJavaModelManager().getJavaModel();
		fName = project.getName();
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IPath outputLocation,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries, 
			outputLocation, 
			monitor, 
			true, // canChangeResource (as per API contract)
			getResolvedClasspath(true), // ignoreUnresolvedVariable
			true, // needValidation
			true); // need to save
	}

	public void setRawClasspath(
		IClasspathEntry[] newEntries,
		IPath newOutputLocation,
		IProgressMonitor monitor,
		boolean canChangeResource,
		IClasspathEntry[] oldResolvedPath,
		boolean needValidation,
		boolean needSave)
		throws JavaModelException {

		JavaModelManager manager =
			(JavaModelManager) JavaModelManager.getJavaModelManager();
		try {
			IClasspathEntry[] newRawPath = newEntries;
			if (newRawPath == null) { //are we already with the default classpath
				newRawPath = defaultClasspath();
			}
			SetClasspathOperation op =
				new SetClasspathOperation(
					this, 
					oldResolvedPath, 
					newRawPath, 
					newOutputLocation,
					canChangeResource, 
					needValidation,
					needSave);
			runOperation(op, monitor);
			
		} catch (JavaModelException e) {
			manager.flush();
			throw e;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries, 
			SetClasspathOperation.ReuseOutputLocation, 
			monitor, 
			true, // canChangeResource (as per API contract)
			getResolvedClasspath(true), // ignoreUnresolvedVariable
			true, // needValidation
			true); // need to save
	}

	/**
	 * NOTE: <code>null</code> specifies default classpath, and an empty
	 * array specifies an empty classpath.
	 *
	 * @exception NotPresentException if this project does not exist.
	 */
	protected void setRawClasspath0(IClasspathEntry[] rawEntries)
		throws JavaModelException {

		JavaModelManager.PerProjectInfo info = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(fProject);
	
		synchronized (info) {
			if (rawEntries != null) {
				info.classpath = rawEntries;
			}
			
			// clear cache of resolved classpath
			info.lastResolvedClasspath = null;
			info.resolvedPathToRawEntries = null;
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
	 * @see JavaProject#getSharedProperty(String key)
	 */
	public void setSharedProperty(String key, String value) throws CoreException {

		IFile rscFile = getProject().getFile(key);
		InputStream inputStream = new ByteArrayInputStream(value.getBytes());
		// update the resource content
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only .classpath file (23984)
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[]{rscFile}, null);
			}
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
	}

	/**
	 * Update cycle markers for all java projects
	 */
	public static void updateAllCycleMarkers() throws JavaModelException {
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaProject[] projects = manager.getJavaModel().getJavaProjects();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		HashSet cycleParticipants = new HashSet();
		HashSet alreadyVisited = new HashSet();
		int length = projects.length;
		
		/* alternate implementation for cycle participants computation
		computeCycleParticipants(projects, cycleParticipants);
		*/

		// compute cycle participants
		ArrayList visited = new ArrayList();
		for (int i = 0; i < length; i++){
			JavaProject project = (JavaProject)projects[i];
			if (!cycleParticipants.contains(project) && !alreadyVisited.contains(project)){
				visited.clear();
				project.updateCycleParticipants(null, visited, cycleParticipants, workspaceRoot, alreadyVisited);
			}
		}
		
		for (int i = 0; i < length; i++){
			JavaProject project = (JavaProject)projects[i];
			
			if (cycleParticipants.contains(project)){
				IMarker cycleMarker = project.getCycleMarker();
				String circularCPOption = project.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true);
				int circularCPSeverity = JavaCore.ERROR.equals(circularCPOption) ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
				if (cycleMarker != null) {
					// update existing cycle marker if needed
					try {
						int existingSeverity = ((Integer)cycleMarker.getAttribute(IMarker.SEVERITY)).intValue();
						if (existingSeverity != circularCPSeverity) {
							cycleMarker.setAttribute(IMarker.SEVERITY, circularCPSeverity);
						}
					} catch (CoreException e) {
						throw new JavaModelException(e);
					}
				} else {
					// create new marker
					project.createClasspathProblemMarker(
						new JavaModelStatus(IJavaModelStatusConstants.CLASSPATH_CYCLE, project)); 
				}
			} else {
				project.flushClasspathProblemMarkers(true, false);
			}			
		}
	}

	/**
	 * If a cycle is detected, then cycleParticipants contains all the project involved in this cycle (directly),
	 * no cycle if the set is empty (and started empty)
	 */
	public void updateCycleParticipants(
			IClasspathEntry[] preferredClasspath, 
			ArrayList visited, 
			HashSet cycleParticipants, 
			IWorkspaceRoot workspaceRoot,
			HashSet alreadyTraversed){
				
		visited.add(this);
		try {
			IClasspathEntry[] classpath = preferredClasspath == null ? getResolvedClasspath(true) : preferredClasspath;
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];
				
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT){
					IPath entryPath = entry.getPath();
					IResource member = workspaceRoot.findMember(entryPath);
					if (member != null && member.getType() == IResource.PROJECT){
						JavaProject project = (JavaProject)JavaCore.create((IProject)member);
						int index = visited.indexOf(project);
						if (index == -1 && cycleParticipants.contains(project))
							index = visited.indexOf(this); // another loop in the cycle exists
						if (index >= 0) { // only consider direct participants inside the cycle
							for (int size = visited.size(); index < size; index++)
								cycleParticipants.add(visited.get(index)); 
						} else {
							if (!alreadyTraversed.contains(project)) {
								project.updateCycleParticipants(null, visited, cycleParticipants, workspaceRoot, alreadyTraversed);
							}
						}
					}
				}
			}
		} catch(JavaModelException e){
		}
		visited.remove(this);
		alreadyTraversed.add(this);
	}
		
	/**
	 * Reset the collection of package fragment roots (local ones) - only if opened.
	 * Need to check *all* package fragment roots in order to reset NameLookup
	 */
	public void updatePackageFragmentRoots(){
		
			if (this.isOpen()) {
				try {
					JavaProjectElementInfo info = getJavaProjectElementInfo();

					IClasspathEntry[] classpath = getResolvedClasspath(true);
					NameLookup lookup = info.getNameLookup();
					if (lookup != null){
						IPackageFragmentRoot[] oldRoots = lookup.fPackageFragmentRoots;
						IPackageFragmentRoot[] newRoots = computePackageFragmentRoots(classpath, true);
						checkIdentical: { // compare all pkg fragment root lists
							if (oldRoots.length == newRoots.length){
								for (int i = 0, length = oldRoots.length; i < length; i++){
									if (!oldRoots[i].equals(newRoots[i])){
										break checkIdentical;
									}
								}
								return; // no need to update
							}	
						}
						info.setNameLookup(null); // discard name lookup (hold onto roots)
					}				
					info.setNonJavaResources(null);
					info.setChildren(
						computePackageFragmentRoots(classpath, false));		

				} catch(JavaModelException e){
					try {
						close(); // could not do better
					} catch(JavaModelException ex){
					}
				}
			}
	}


}
