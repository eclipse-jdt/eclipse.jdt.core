package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.jdt.internal.codeassist.impl.CompletionOptions;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.builder.impl.*;
import org.eclipse.jdt.internal.core.builder.impl.ProblemFactory;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.impl.FormatterOptions;

/**
 * The plug-in runtime class for the Java model plug-in containing the core
 * (UI-free) support for Java projects.
 * <p>
 * Like all plug-in runtime classes (subclasses of <code>Plugin</code>), this
 * class is automatically instantiated by the platform when the plug-in gets
 * activated. Clients must not attempt to instantiate plug-in runtime classes
 * directly.
 * </p>
 * <p>
 * The single instance of this class can be accessed from any plug-in declaring
 * the Java model plug-in as a prerequisite via 
 * <code>JavaCore.getJavaCore()</code>. The Java model plug-in will be activated
 * automatically if not already active.
 * </p>
 */
public final class JavaCore extends Plugin implements IExecutableExtension {

	private static Plugin JAVA_CORE_PLUGIN = null;
	/**
	 * The plug-in identifier of the Java core support
	 * (value <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.jdt.core" ; //$NON-NLS-1$
	// getPlugin().getDescriptor().getUniqueIdentifier();

	/**
	 * The identifier for the Java builder
	 * (value <code>"org.eclipse.jdt.core.javabuilder"</code>).
	 */
	public static final String BUILDER_ID = PLUGIN_ID + ".javabuilder" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java model
	 * (value <code>"org.eclipse.jdt.core.javamodel"</code>).
	 */
	public static final String MODEL_ID = PLUGIN_ID + ".javamodel" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java nature
	 * (value <code>"org.eclipse.jdt.core.javanature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * Java-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".javanature" ; //$NON-NLS-1$

	/**
	 * Name of the handle id attribute in a Java marker
	 */
	private static final String ATT_HANDLE_ID =
		"org.eclipse.jdt.internal.core.JavaModelManager.handleId" ; //$NON-NLS-1$

	private static Hashtable Variables = new Hashtable(5);

	/**
	 * Configurable option names. For further information, refer to the file named Java.ini
	 */

	// File containing default settings for configurable options
	private static final String JAVA_CORE_INIT = "JavaCore.ini"; //$NON-NLS-1$
	
	/**
	 * Compiler options
	 */
	public static final String OPTION_LocalVariableAttribute = CompilerOptions.OPTION_LocalVariableAttribute;
	public static final String OPTION_LineNumberAttribute = CompilerOptions.OPTION_LineNumberAttribute;
	public static final String OPTION_SourceFileAttribute = CompilerOptions.OPTION_SourceFileAttribute;
	public static final String OPTION_PreserveUnusedLocal = CompilerOptions.OPTION_PreserveUnusedLocal;
	public static final String OPTION_ReportUnreachableCode = CompilerOptions.OPTION_ReportUnreachableCode;
	public static final String OPTION_ReportInvalidImport = CompilerOptions.OPTION_ReportInvalidImport;
	public static final String OPTION_ReportMethodWithConstructorName = CompilerOptions.OPTION_ReportMethodWithConstructorName;
	public static final String OPTION_ReportOverridingPackageDefaultMethod = CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod;
	public static final String OPTION_ReportDeprecation = CompilerOptions.OPTION_ReportDeprecation;
	public static final String OPTION_ReportHiddenCatchBlock = CompilerOptions.OPTION_ReportHiddenCatchBlock;
	public static final String OPTION_ReportUnusedLocal = CompilerOptions.OPTION_ReportUnusedLocal;
	public static final String OPTION_ReportUnusedParameter = CompilerOptions.OPTION_ReportUnusedParameter;
	public static final String OPTION_TargetPlatform = CompilerOptions.OPTION_TargetPlatform;
	public static final String OPTION_ReportSyntheticAccessEmulation = CompilerOptions.OPTION_ReportSyntheticAccessEmulation;
	public static final String OPTION_ReportNonExternalizedStringLiteral = CompilerOptions.OPTION_ReportNonExternalizedStringLiteral;
	public static final String OPTION_Source = CompilerOptions.OPTION_Source;
	public static final String OPTION_ReportAssertIdentifier = CompilerOptions.OPTION_ReportAssertIdentifier;

	/**
	 * Code Formatter options
	 */
	public static final String OPTION_InsertNewlineBeforeOpeningBrace = FormatterOptions.OPTION_InsertNewlineBeforeOpeningBrace;
	public static final String OPTION_InsertNewlineInControlStatement = FormatterOptions.OPTION_InsertNewlineInControlStatement;
	public static final String OPTION_InsertNewLineBetweenElseAndIf = FormatterOptions.OPTION_InsertNewLineBetweenElseAndIf;
	public static final String OPTION_InsertNewLineInEmptyBlock = FormatterOptions.OPTION_InsertNewLineInEmptyBlock;
	public static final String OPTION_ClearAllBlankLines = FormatterOptions.OPTION_ClearAllBlankLines;
	public static final String OPTION_SplitLineExceedingLength = FormatterOptions.OPTION_SplitLineExceedingLength;
	public static final String OPTION_CompactAssignment = FormatterOptions.OPTION_CompactAssignment;
	public static final String OPTION_TabulationChar = FormatterOptions.OPTION_TabulationChar;
	public static final String OPTION_TabulationSize = FormatterOptions.OPTION_TabulationSize;
	
	/**
	 * Completion Engine options
	 */
	public static final String OPTION_VisibilitySensitivity = CompletionOptions.OPTION_PerformVisibilityCheck;
	public static final String OPTION_EntireWordReplacement = CompletionOptions.OPTION_EntireWordReplacement;
	
	/**
	 * JavaCore options
	 */
	public static final String OPTION_ComputeBuildOrder = "org.eclipse.jdt.core.JavaCore.computeJavaBuildOrder"; //$NON-NLS-1$
	
	/**
	 * Code Assist options
	 */
	
	/**
	 * Creates the Java core plug-in.
	 */
	public JavaCore(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		JAVA_CORE_PLUGIN = this;
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener
	 */
	public static void addElementChangedListener(IElementChangedListener listener) {
		JavaModelManager.getJavaModelManager().addElementChangedListener(listener);
	}

	/**
	 * Configures the given marker attribute map for the given Java element.
	 * Used for markers which denote a Java element rather than a resource.
	 *
	 * @param attributes the mutable marker attribute map (key type: <code>String</code>,
	 *   value type: <code>String</code>)
	 * @param element the Java element for which the marker needs to be configured
	 */
	public static void addJavaElementMarkerAttributes(
		Map attributes,
		IJavaElement element) {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (attributes != null && element != null)
			attributes.put(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	/**
	 * Configures the given marker for the given Java element.
	 * Used for markers which denote a Java element rather than a resource.
	 *
	 * @param marker the marker to be configured
	 * @param element the Java element for which the marker needs to be configured
	 * @exception CoreException if the <code>IMarker.setAttribute</code> on the marker fails
	 */
	public void configureJavaElementMarker(IMarker marker, IJavaElement element)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (marker != null && element != null)
			marker.setAttribute(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	/**
	 * Returns the Java model element corresponding to the given handle identifier
	 * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
	 * <code>null</code> if unable to create the associated element.
	 */
	public static IJavaElement create(String handleIdentifier) {
		if (handleIdentifier == null) {
			return null;
		}
		try {
			return JavaModelManager.getJavaModelManager().getHandleFromMemento(
				handleIdentifier);
		} catch (JavaModelException e) {
			return null;
		}
	}
	/**
	 * Returns the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file
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
	public static IJavaElement create(IFile file) {
		if (file == null) {
			return null;
		}
		String extension = file.getProjectRelativePath().getFileExtension();
		if (extension != null) {
			extension = extension.toLowerCase();
			if (extension.equals("java"  //$NON-NLS-1$
				)) {
				return createCompilationUnitFrom(file);
			} else if (extension.equals("class"  //$NON-NLS-1$
				)) {
				return createClassFileFrom(file);
			} else if (extension.equals("jar"  //$NON-NLS-1$
				) || extension.equals("zip"  //$NON-NLS-1$
				)) {
				return createJarPackageFragmentRootFrom(file);
			}
		}
		return null;
	}
	/**
	 * Returns the package fragment or package fragment root corresponding to the given folder, or
	 * <code>null</code> if unable to associate the given folder with a Java element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFolder folder) {
		if (folder == null) {
			return null;
		}
		if (folder.getName().indexOf('.') < 0) {
			JavaProject project = (JavaProject) create(folder.getProject());
			if (project == null)
				return null;
			IJavaElement element = determineIfOnClasspath(folder, project);
			try {
				IPath outputLocation = project.getOutputLocation();
				if (outputLocation == null)
					return null;
				if (outputLocation.isPrefixOf(folder.getFullPath())) {
					if (project.getClasspathEntryFor(outputLocation) != null) {
						// if the output location is the same as an input location, return the element
						return element;
					} else {
						// otherwise, do not create elements for folders in the output location
						return null;
					}
				} else {
					return element;
				}
			} catch (JavaModelException e) {
				return null;
			}
		}
		return null;
	}
	/**
	 * Returns the Java project corresponding to the given project, or
	 * <code>null</code> if unable to associate the given project
	 * with a Java project.
	 * <p>
	 * Creating a Java Project has the side effect of creating and opening all of the
	 * project's parents if they are not yet open.
	 */
	public static IJavaProject create(IProject project) {
		if (project == null) {
			return null;
		}
		JavaModel javaModel = JavaModelManager.getJavaModel(project.getWorkspace());
		return javaModel.getJavaProject(project);
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
	public static IJavaElement create(IResource resource) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT :
				return create((IProject) resource);
			case IResource.FILE :
				return create((IFile) resource);
			case IResource.FOLDER :
				return create((IFolder) resource);
			case IResource.ROOT :
				return create((IWorkspaceRoot) resource);
			default :
				return null;
		}
	}
	/**
	 * Returns the Java model.
	 */
	public static IJavaModel create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return JavaModelManager.getJavaModel(root.getWorkspace());
	}
	/**
	 * Creates and returns a class file element for
	 * the given <code>.class</code> file. Returns <code>null</code> if unable
	 * to recognize the class file.
	 */
	public static IClassFile createClassFileFrom(IFile file) {
		IJavaProject project = (IJavaProject) create(file.getProject());
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
	 * Creates and returns a compilation unit element for
	 * the given <code>.java</code> file. Returns <code>null</code> if unable
	 * to recognize the compilation unit.
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file) {
		IProject fileProject = file.getProject();
		IJavaProject project = (IJavaProject) create(fileProject);
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
	 * Creates and returns a handle for the given JAR file.
	 * The Java model associated with the JAR's project may be
	 * created as a side effect. 
	 * Returns <code>null</code> if unable to create a JAR package fragment root.
	 * (for example, if the JAR file represents a non-Java resource)
	 */
	public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file) {
		IJavaProject project = (IJavaProject) create(file.getProject());

		// Create a jar package fragment root only if on the classpath
		IPath resourcePath = file.getFullPath();
		try {
			IClasspathEntry[] entries = project.getResolvedClasspath(true);
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
	private static IJavaElement determineIfOnClasspath(
		IResource resource,
		IJavaProject project) {
		IPath resourcePath = resource.getFullPath();
		try {
			IClasspathEntry[] entries = project.getResolvedClasspath(true);
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				IPath rootPath = entry.getPath();
				if (rootPath.equals(resourcePath)) {
					return project.getPackageFragmentRoot(resource);
				} else if (rootPath.isPrefixOf(resourcePath)) {
					IPackageFragmentRoot root =
						((JavaProject) project).getPackageFragmentRoot(rootPath);
					IPath pkgPath = resourcePath.removeFirstSegments(rootPath.segmentCount());
					if (resource.getType() == IResource.FILE) {
						// if the resource is a file, then remove the last segment which
						// is the file name in the package
						pkgPath = pkgPath.removeLastSegments(1);
					}
					StringBuffer pkgName = new StringBuffer(IPackageFragment.DEFAULT_PACKAGE_NAME);
					for (int j = 0, max = pkgPath.segmentCount(); j < max; j++) {
						String segment = pkgPath.segment(j);
						if (segment.indexOf('.') >= 0) {
							return null;
						}
						pkgName.append(segment);
						if (j < pkgPath.segmentCount() - 1) {
							pkgName.append("." ); //$NON-NLS-1$
						}
					}
					return root.getPackageFragment(pkgName.toString());
				}
			}
		} catch (JavaModelException npe) {
			return null;
		}
		return null;
	}
	/**
	 * Returns the path held in the given classpath variable.
	 * Returns <node>null</code> if unable to bind.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @return the path, or <code>null</code> if none 
	 * @see #setClasspathVariable
	 */
	public static IPath getClasspathVariable(String variableName) {
		return (IPath) Variables.get(variableName);
	}
	/**
	 * Returns the names of all known classpath variables.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @return the list of classpath variable names
	 * @see #setClasspathVariable
	 */
	public static String[] getClasspathVariableNames() {
		int length = Variables.size();
		String[] result = new String[length];
		Enumeration vars = Variables.keys();
		int index = 0;
		while (vars.hasMoreElements()) {
			result[index++] = (String) vars.nextElement();
		}
		return result;
	}

	private static IPath getInstallLocation() {
		return new Path(getPlugin().getDescriptor().getInstallURL().getFile());
	}

	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * Equivalent to <code>(JavaCore) getPlugin()</code>.
	 */
	public static JavaCore getJavaCore() {
		return (JavaCore) getPlugin();
	}
	/**
	 * Returns the <code>IJavaProject</code> associated with the
	 * given <code>IProject</code>, or <code>null</code> if the
	 * project does not have a Java nature.
	 */
	private IJavaProject getJavaProject(IProject project) {
		try {
			if (project.hasNature(NATURE_ID)) {
				JavaModel model = JavaModelManager.getJavaModel(project.getWorkspace());
				if (model != null) {
					return model.getJavaProject(project);
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 */
	public static Plugin getPlugin() {
		return JAVA_CORE_PLUGIN;
	}

	/**
	 * This is a helper method which returns the resolved classpath entry denoted 
	 * by a given entry (if it is a variable entry). It is obtained by resolving the variable 
	 * reference in the first segment. Returns <node>null</code> if unable to resolve using 
	 * the following algorithm:
	 * <ul>
	 * <li> if variable segment cannot be resolved, returns <code>null</code></li>
	 * <li> finds a project, JAR or binary folder in the workspace at the resolved path location</li>
	 * <li> if none finds an external JAR file or folder outside the workspace at the resolved path location </li>
	 * <li> if none returns <code>null</code></li>
	 * </ul>
	 * <p>
	 * Variable source attachment path and root path are also resolved and recorded in the resulting classpath entry.
	 * <p>
	 * @return the resolved library or project classpath entry, or <code>null</code>
	 *   if the given variable entry could not be resolved to a valid classpath entry
	 */
	public static IClasspathEntry getResolvedClasspathEntry(IClasspathEntry entry) {

		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
			return entry;

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath resolvedPath = JavaCore.getResolvedVariablePath(entry.getPath());
		if (resolvedPath == null)
			return null;

		Object target = JavaModel.getTarget(workspaceRoot, resolvedPath, false);
		if (target == null)
			return null;

		// inside the workspace
		if (target instanceof IResource) {
			IResource resolvedResource = (IResource) target;
			if (resolvedResource != null) {
				switch (resolvedResource.getType()) {
					case IResource.PROJECT :
						return JavaCore.newProjectEntry(resolvedPath); // internal project
					case IResource.FILE :
						String extension = resolvedResource.getFileExtension();
						if ("jar"  //$NON-NLS-1$
							.equalsIgnoreCase(extension) || "zip"  //$NON-NLS-1$
							.equalsIgnoreCase(extension)) { // internal binary archive
							return JavaCore.newLibraryEntry(
								resolvedPath,
								getResolvedVariablePath(entry.getSourceAttachmentPath()),
								getResolvedVariablePath(entry.getSourceAttachmentRootPath()));
						}
						break;
					case IResource.FOLDER : // internal binary folder
						return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()));
				}
			}
		}
		// outside the workspace
		if (target instanceof File) {
			File externalFile = (File) target;
			if (externalFile.isFile()) {
				String fileName = externalFile.getName().toLowerCase();
				if (fileName.endsWith(".jar"  //$NON-NLS-1$
					) || fileName.endsWith(".zip"  //$NON-NLS-1$
					)) { // external binary archive
					return JavaCore.newLibraryEntry(
						resolvedPath,
						getResolvedVariablePath(entry.getSourceAttachmentPath()),
						getResolvedVariablePath(entry.getSourceAttachmentRootPath()));
				}
			} else { // external binary folder
				return JavaCore.newLibraryEntry(
					resolvedPath,
					getResolvedVariablePath(entry.getSourceAttachmentPath()),
					getResolvedVariablePath(entry.getSourceAttachmentRootPath()));
			}
		}
		return null;
	}

	/**
	 * Resolve a variable path (helper method)
	 */
	public static IPath getResolvedVariablePath(IPath variablePath) {

		if (variablePath == null)
			return null;
		int count = variablePath.segmentCount();
		if (count == 0)
			return null;

		// lookup variable	
		String variableName = variablePath.segment(0);
		IPath resolvedPath = JavaCore.getClasspathVariable(variableName);
		if (resolvedPath == null || resolvedPath.isEmpty())
			return null;

		// append path suffix
		if (count > 1) {
			resolvedPath = resolvedPath.append(variablePath.removeFirstSegments(1));
		}
		return resolvedPath;
	}

	/**
	 * Returns whether the given marker references the given Java element.
	 * Used for markers which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param marker the marker
	 * @return <code>true</code> if the marker references the element
	 * @exception CoreException if the <code>IMarker.getAttribute</code> on the marker fails 	 
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarker marker)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		return (
			element != null
				&& marker != null
				&& element.getHandleIdentifier().equals(marker.getAttribute(ATT_HANDLE_ID)));
	}

	/**
	 * Returns whether the given marker delta references the given Java element.
	 * Used for markers deltas which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param markerDelta the marker delta
	 * @return <code>true</code> if the marker delta references the element
	 * @exception CoreException if the  <code>IMarkerDelta.getAttribute</code> on the marker delta fails 	 
	 */
	public static boolean isReferencedBy(
		IJavaElement element,
		IMarkerDelta markerDelta)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		return element != null
			&& markerDelta != null
			&& element.getHandleIdentifier().equals(markerDelta.getAttribute(ATT_HANDLE_ID));
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_LIBRARY</code> for the JAR or folder
	 * identified by the given absolute path. This specifies that all package fragments within the root 
	 * will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 *
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive, 
	 *    or <code>null</code> if none
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_BINARY,
			IClasspathEntry.CPE_LIBRARY,
			JavaProject.canonicalizedPath(path),
			sourceAttachmentPath,
			sourceAttachmentRootPath);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 */
	public static IClasspathEntry newProjectEntry(IPath path) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_PROJECT,
			path,
			null,
			null);
	}

	/**
	 * Returns a new empty region.
	 */
	public static IRegion newRegion() {
		return new Region();
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code> for the project's source folder 
	 * identified by the given absolute path. This specifies that all package fragments within the root will 
	 * have children of type <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the workspace root, e.g. <code>"/Project/src"</code>.
	 * <p>
	 * A source entry is used to setup the internal source layout of a project, and cannot be used out of the
	 * context of the containing project (a source entry "Proj1/src" cannot be used on the classpath of Proj2).
	 */
	public static IClasspathEntry newSourceEntry(IPath path) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_SOURCE,
			path,
			null,
			null);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li><"JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resoved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resoved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * <p>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
		Assert.isTrue(
			variablePath != null && variablePath.segmentCount() >= 1,
			Util.bind("classpath.illegalVariablePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_VARIABLE,
			variablePath,
			variableSourceAttachmentPath,
			sourceAttachmentRootPath);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @see #setClasspathVariable
	 *
	 * @deprecated - use version with extra IProgressMonitor
	 */
	public static void removeClasspathVariable(String variableName) {
		removeClasspathVariable(variableName, null);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param monitor the progress monitor to report progress
	 * @see #setClasspathVariable
	 */
	public static void removeClasspathVariable(
		String variableName,
		IProgressMonitor monitor) {

		try {
			updateVariableValue(variableName, null, monitor);
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Removes the given element changed listener.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	public static void removeElementChangedListener(IElementChangedListener listener) {
		JavaModelManager.getJavaModelManager().removeElementChangedListener(listener);
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must have at least one segment.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @see #getClasspathVariable
	 *
	 * @deprecated - use API with IProgressMonitor
	 */
	public static void setClasspathVariable(String variableName, IPath path)
		throws JavaModelException {

		setClasspathVariable(variableName, path, null);
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must have at least one segment.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @param monitor a monitor to report progress
	 * @see #getClasspathVariable
	 */
	public static void setClasspathVariable(
		String variableName,
		IPath path,
		IProgressMonitor monitor)
		throws JavaModelException {

		Assert.isTrue(path != null, Util.bind("classpath.nullVariablePath" )); //$NON-NLS-1$
		updateVariableValue(variableName, path, monitor);
	}

	/* (non-Javadoc)
	 * Method declared on IExecutableExtension.
	 * Record any necessary initialization data from the plugin.
	 */
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data)
		throws CoreException {
	}

	/**
	 * Shutdown the JavaCore plugin
	 * <p>
	 * De-registers the JavaModelManager as a resource changed listener and save participant.
	 * <p>
	 * @see Plugin#shutdown
	 */
	public void shutdown() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(JavaModelManager.getJavaModelManager());
		workspace.removeSaveParticipant(this);

		((JavaModelManager) JavaModelManager.getJavaModelManager()).shutdown();
	}

	/**
	 * Initiate the background indexing process.
	 * This should be deferred after the plugin activation.
	 */
	private void startIndexing() {

		JavaModelManager manager =
			(JavaModelManager) JavaModelManager.getJavaModelManager();
		IndexManager indexManager = manager.getIndexManager();
		if (indexManager != null)
			indexManager.reset();
	}

	/**
	 * Startup of the JavaCore plugin
	 * <p>
	 * Registers the JavaModelManager as a resource changed listener and save participant.
	 * Starts the background indexing, and restore saved classpath variable values.
	 * <p>
	 * @see Plugin#startup
	 */
	public void startup() {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IndexManager indexManager = manager.getIndexManager();
			if (indexManager != null) {
				// need to initialize workbench now since a query may be done before indexing starts
				indexManager.workspace = workspace;
			}
			workspace.addResourceChangeListener(
				manager,
				IResourceChangeEvent.PRE_AUTO_BUILD
					| IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);

			startIndexing();

			workspace.addSaveParticipant(this, manager);
			manager.loadVariables();
			manager.loadOptions();
		} catch (CoreException e) {
		} catch (RuntimeException e) {
			manager.shutdown();
			throw e;
		}
	}

	/**
	 * Internal updating of a variable value (null path meaning removal).
	 */
	private static void updateVariableValue(
		String variableName,
		IPath path,
		IProgressMonitor monitor)
		throws JavaModelException {

		// gather classpath information for updating
		Hashtable affectedProjects = new Hashtable(5);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			IJavaModel model = manager.getJavaModel();
			if (model != null) {
				IJavaProject[] projects = model.getJavaProjects();
				nextProject : for (int i = 0, max = projects.length; i < max; i++) {
					IClasspathEntry[] entries = projects[i].getRawClasspath();
					for (int j = 0, cplength = entries.length; j < cplength; j++) {
						IClasspathEntry oldEntry = entries[j];
						if (oldEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
							IPath sourcePath, sourceRootPath;
							if (oldEntry.getPath().segment(0).equals(variableName)
								|| ((sourcePath = oldEntry.getSourceAttachmentPath()) != null
									&& sourcePath.segment(0).equals(variableName))
								|| ((sourceRootPath = oldEntry.getSourceAttachmentRootPath()) != null
									&& sourceRootPath.segment(0).equals(variableName))) {
								affectedProjects.put(projects[i], projects[i].getResolvedClasspath(true));
								continue nextProject;
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
		if (path == null) {
			Variables.remove(variableName);
		} else {
			// new variable value is assigned
			Variables.put(variableName, path);
		}
		if (!affectedProjects.isEmpty()) {
			boolean wasFiring = manager.isFiring();
			try {
				if (wasFiring)
					manager.stopDeltas();
				// propagate classpath change
				Enumeration projectsToUpdate = affectedProjects.keys();
				while (projectsToUpdate.hasMoreElements()) {
					JavaProject project = (JavaProject) projectsToUpdate.nextElement();
					project
						.setRawClasspath(
							project.getRawClasspath(),
							monitor,
							project.getWorkspace().isAutoBuilding(),
					// force build if in auto build mode
					 (IClasspathEntry[]) affectedProjects.get(project));
				}
			} finally {
				manager.mergeDeltas();
				if (wasFiring) {
					manager.startDeltas();
					manager.fire();
				}
			}
		}
	}

	/**
	* Set the value of the current setting for an option.
	*
	* @return IJavaModelStatusConstants.INVALID_OPTION_VALUE if option value
	* are not correct and IJavaModelStatusConstants.INVALID_OPTION if option
	* doesn't exist.
	*/
	public static IJavaModelStatus setOptionValue(String id, String value) {
		IJavaModelStatus status = validateOptionValue(id, value);
		if (status.getCode() == IJavaModelStatus.OK) {
			ConfigurableOption option = (ConfigurableOption) getOptions().get(id);
			if (option != null)
				option.setValue(value);
		}
		return status;
	}

	/**
	* Answer the value of the current setting for an option.
	*
	* @return String
	*/
	public static String getOptionValue(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getValue();

		return null;
	}

	/**
	* Set the value of the default setting for an option.
	*
	* @return IJavaModelStatusConstants.INVALID_OPTION_VALUE if option value
	* are not correct and IJavaModelStatusConstants.INVALID_OPTION if option
	* doesn't exist.
	*/
	private static IJavaModelStatus setOptionDefaultValue(
		String id,
		String value) {
		IJavaModelStatus status = validateOptionValue(id, value);
		if (status.getCode() == IJavaModelStatus.OK) {
			ConfigurableOption option = (ConfigurableOption) getOptions().get(id);
			if (option != null)
				option.setDefaultValue(value);
		}
		return status;
	}

	/**
	* Answer the value of the default setting for an option.
	*
	* @return String
	*/
	public static String getOptionDefaultValue(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getDefaultValue();

		return null;
	}

	/**
	* Return an String that represents the localized description of an option.
	*
	* @return java.lang.String
	*/
	public static String getOptionDescription(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getDescription();

		return null;
	}
	/**
	* Return a String that represents the localized name of an option.
	* @return java.lang.String
	*/
	public static String getOptionName(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getName();

		return null;
	}

	/**
	* Return a String that identifies the component owner of an option
	* (typically the qualified type name of the class which it corresponds to).
	*
	* e.g. "org.eclipse.jdt.internal.compiler.api.Compiler"
	*
	* @return java.lang.String
	*/
	public static String getOptionComponentName(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getComponentName();

		return null;
	}

	/**
	 * Return a String that represents the localized category of an option.
	 * @return java.lang.String
	 */
	public static String getOptionCategory(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getCategory();

		return null;
	}

	/**
	* Return an array of String that represents the localized possible values of an option.
	*
	* @return java.lang.String[]
	*/
	public static String[] getOptionPossibleValues(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getPossibleValues();

		return null;
	}

	/**
	 * Return the type of option. Type is a String with possible value :
	 * <code>discrete</code>,<code>string</code>,<code>int</code> and
	 * <code>float</code>.
	 */
	public static String getOptionType(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getType();

		return null;
	}

	/**
	 * Return the maximum value of option if option's type is <code>int</code>
	 *  or <code>float</code>.Otherwise return null.
	 */
	public static Number getOptionMax(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null) {
			return option.getMax();
		}

		return null;
	}

	/**
	 * Return the minimum value of option if option's type is <code>int</code>
	 *  or <code>float</code>.Otherwise return null.
	 */
	public static Number getOptionMin(String id) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null)
			return option.getMin();

		return null;
	}

	/**
	 * Answers a set of option'IDs which are in option set of JavaCore
	 */
	public static String[] getOptionIDs() {
		return JavaModelManager.getOptionIDs();
	}

	/**
	 * Answers a set of option'IDs which are in option set of JavaCore
	 * and associated with a component.
	 */
	public static String[] getOptionIDs(String componentName) {
		String[] ids = getOptionIDs();

		String[] result = new String[ids.length];
		int resultCount = 0;
		for (int i = 0; i < ids.length; i++) {
			if (ids[i].startsWith(componentName))
				result[resultCount++] = ids[i];
		}

		System.arraycopy(result, 0, result = new String[resultCount], 0, resultCount);

		return result;
	}

	/**
	 * Answers if a value is valide for an option
	 * 
	 * @return IJavaModelStatusConstants.INVALID_OPTION_VALUE if option value
	 * are not correct and IJavaModelStatusConstants.INVALID_OPTION if option
	 * doesn't exist.
	 */
	public static IJavaModelStatus validateOptionValue(String id, String value) {
		ConfigurableOption option = (ConfigurableOption) getOptions().get(id);

		if (option != null) {
			String[] values = option.getPossibleValues();
			if (values == ConfigurableOption.NoDiscreteValue) {
				try {
					if (option.getType().equals(ConfigurableOption.INT)) {
						int max = option.getMax().intValue();
						int min = option.getMin().intValue();
						int val = Integer.parseInt(value);
						if (val > max || val < min)
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_OPTION_VALUE);
					} else if (option.getType().equals(ConfigurableOption.FLOAT)) {
						float max = option.getMax().floatValue();
						float min = option.getMin().floatValue();
						float val = Float.parseFloat(value);
						if (val > max || val < min)
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_OPTION_VALUE);
					}
				} catch (NumberFormatException e) {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_OPTION_VALUE);
				}
				return JavaModelStatus.VERIFIED_OK;
			} else {
				for (int i = 0; i < values.length; i++) {
					if (values[i].equals(value))
						return JavaModelStatus.VERIFIED_OK;
				}
				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_OPTION_VALUE);
			}
		}
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_OPTION);
	}

	/**
	 * Reset JavaCore option values to defaults.
	 */
	public static void resetOptions() {
		Locale locale = Locale.getDefault();

		if (JavaModelManager.fOptions == null) {
			JavaModelManager.initializeOptions();
			// Set options to JavaCore default value
			setJavaCoreDefaultOptionsValue(locale);

		} else {
			ConfigurableOption[] options =
				(ConfigurableOption[]) JavaModelManager.fOptions.values().toArray(
					new ConfigurableOption[0]);
			for (int i = 0; i < options.length; i++)
				options[i].setToDefault();
		}
	}

	private static void setJavaCoreDefaultOptionsValue(Locale locale) {
		BufferedReader reader = null;
		try {
			reader =
				new BufferedReader(
					new InputStreamReader(JavaCore.class.getResourceAsStream(JAVA_CORE_INIT)));
			String line = reader.readLine();
			while (line != null) {
				int equalIndex = line.indexOf("=" ); //$NON-NLS-1$
				if (!line.startsWith("#" ) && equalIndex != -1) { //$NON-NLS-1$
					String id = line.substring(0, equalIndex).trim();

					ConfigurableOption option = new ConfigurableOption(id, locale);
					if (option.getPossibleValues() != ConfigurableOption.NoDiscreteValue) {
						try {
							int index = Integer.parseInt(line.substring(equalIndex + 1).trim());
							option.setDefaultValueIndex(index);
						} catch (NumberFormatException e) {
							// value is default default value
						}
					} else {
						String value = line.substring(equalIndex + 1).trim();
						option.setDefaultValue(value);
					}
					JavaModelManager.addOption(option);
				}
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch(IOException e){
				}
			}
		}
	}

	private static Hashtable getOptions() {
		if (JavaModelManager.fOptions == null)
			resetOptions();

		return JavaModelManager.fOptions;
	}

	/**
	 * Returns all the options of Java Core to be shown by the UI
	 *
	 * @param locale java.util.Locale
	 * @return org.eclipse.jdt.internal.compiler.ConfigurableOption[]
	 */
	private static ConfigurableOption[] getDefaultOptions(Locale locale) {
		String[] ids = ConfigurableOption.getIDs(JavaCore.class.getName(), locale);

		ConfigurableOption[] result = new ConfigurableOption[ids.length];
		for (int i = 0; i < ids.length; i++) {
			result[i] = new ConfigurableOption(ids[i], locale);
		}
		return result;
	}
}