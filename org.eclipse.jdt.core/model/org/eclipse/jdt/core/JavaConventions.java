/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import java.io.File;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.Util;

/**
 * Provides methods for checking Java-specific conventions such as name syntax.
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public final class JavaConventions {

	private final static char fgDot= '.';
	private final static Scanner SCANNER = new Scanner();

	/**
	 * Not instantiable.
	 */
	private JavaConventions() {}

	/*
	 * Returns the index of the first argument paths which is strictly enclosing the path to check
	 */
	private static int indexOfEnclosingPath(IPath checkedPath, IPath[] paths, int pathCount) {

		for (int i = 0; i < pathCount; i++){
			if (paths[i].equals(checkedPath)) continue;
			if (paths[i].isPrefixOf(checkedPath)) return i;
		}
		return -1;
	}
	
	/*
	 * Returns the index of the first argument paths which is equal to the path to check
	 */
	private static int indexOfMatchingPath(IPath checkedPath, IPath[] paths, int pathCount) {

		for (int i = 0; i < pathCount; i++){
			if (paths[i].equals(checkedPath)) return i;
		}
		return -1;
	}

	/*
	 * Returns the index of the first argument paths which is strictly nested inside the path to check
	 */
	private static int indexOfNestedPath(IPath checkedPath, IPath[] paths, int pathCount) {

		for (int i = 0; i < pathCount; i++){
			if (checkedPath.equals(paths[i])) continue;
			if (checkedPath.isPrefixOf(paths[i])) return i;
		}
		return -1;
	}

	/**
	 * Returns whether the given package fragment root paths are considered
	 * to overlap.
	 * <p>
	 * Two root paths overlap if one is a prefix of the other, or they point to
	 * the same location. However, a JAR is allowed to be nested in a root.
	 *
	 * @param rootPath1 the first root path
	 * @param rootPath2 the second root path
	 * @return true if the given package fragment root paths are considered to overlap, false otherwise
	 * @deprecated Overlapping roots are allowed in 2.1
	 */
	public static boolean isOverlappingRoots(IPath rootPath1, IPath rootPath2) {
		if (rootPath1 == null || rootPath2 == null) {
			return false;
		}
		String extension1 = rootPath1.getFileExtension();
		String extension2 = rootPath2.getFileExtension();
		String jarExtension = "JAR"; //$NON-NLS-1$
		String zipExtension = "ZIP"; //$NON-NLS-1$
		if (extension1 != null && (extension1.equalsIgnoreCase(jarExtension) || extension1.equalsIgnoreCase(zipExtension))) {
			return false;
		} 
		if (extension2 != null && (extension2.equalsIgnoreCase(jarExtension) || extension2.equalsIgnoreCase(zipExtension))) {
			return false;
		}
		return rootPath1.isPrefixOf(rootPath2) || rootPath2.isPrefixOf(rootPath1);
	}

	/**
	 * Returns the current identifier extracted by the scanner (ie. without unicodes)
	 * from the given id.
	 * Returns <code>null</code> if the id was not valid.
	 */
	private static synchronized char[] scannedIdentifier(String id) {
		if (id == null) {
			return null;
		}
		String trimmed = id.trim();
		if (!trimmed.equals(id)) {
			return null;
		}
		try {
			SCANNER.setSource(id.toCharArray());
			int token = SCANNER.getNextToken();
			char[] currentIdentifier;
			try {
				currentIdentifier = SCANNER.getCurrentIdentifierSource();
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
			int nextToken= SCANNER.getNextToken();
			if (token == TerminalTokens.TokenNameIdentifier 
				&& nextToken == TerminalTokens.TokenNameEOF
				&& SCANNER.startPosition == SCANNER.source.length) { // to handle case where we had an ArrayIndexOutOfBoundsException 
																     // while reading the last token
				return currentIdentifier;
			} else {
				return null;
			}
		}
		catch (InvalidInputException e) {
			return null;
		}
	}

	/**
	 * Validate the given compilation unit name.
	 * A compilation unit name must obey the following rules:
	 * <ul>
	 * <li> it must not be null
	 * <li> it must include the <code>".java"</code> suffix
	 * <li> its prefix must be a valid identifier
	 * <li> it must not contain any characters or substrings that are not valid 
	 *		   on the file system on which workspace root is located.
	 * </ul>
	 * </p>
	 * @param name the name of a compilation unit
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a compilation unit name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateCompilationUnitName(String name) {
		if (name == null) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.unit.nullName"), null); //$NON-NLS-1$
		}
		if (!Util.isJavaFileName(name)) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.unit.notJavaName"), null); //$NON-NLS-1$
		}
		String identifier;
		int index;
		index = name.lastIndexOf('.');
		if (index == -1) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.unit.notJavaName"), null); //$NON-NLS-1$
		}
		identifier = name.substring(0, index);
		IStatus status = validateIdentifier(identifier);
		if (!status.isOK()) {
			return status;
		}
		status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
		if (!status.isOK()) {
			return status;
		}
		return JavaModelStatus.VERIFIED_OK;
	}

	/**
	 * Validate the given .class file name.
	 * A .class file name must obey the following rules:
	 * <ul>
	 * <li> it must not be null
	 * <li> it must include the <code>".class"</code> suffix
	 * <li> its prefix must be a valid identifier
	 * <li> it must not contain any characters or substrings that are not valid 
	 *		   on the file system on which workspace root is located.
	 * </ul>
	 * </p>
	 * @param name the name of a .class file
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a .class file name, otherwise a status 
	 *		object indicating what is wrong with the name
	 * @since 2.0
	 */
	public static IStatus validateClassFileName(String name) {
		if (name == null) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.classFile.nullName"), null); //$NON-NLS-1$
		}
		if (!Util.isClassFileName(name)) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.classFile.notClassFileName"), null); //$NON-NLS-1$
		}
		String identifier;
		int index;
		index = name.lastIndexOf('.');
		if (index == -1) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.classFile.notJavaName"), null); //$NON-NLS-1$
		}
		identifier = name.substring(0, index);
		IStatus status = validateIdentifier(identifier);
		if (!status.isOK()) {
			return status;
		}
		status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
		if (!status.isOK()) {
			return status;
		}
		return JavaModelStatus.VERIFIED_OK;
	}

	/**
	 * Validate the given field name.
	 * <p>
	 * Syntax of a field name corresponds to VariableDeclaratorId (JLS2 8.3).
	 * For example, <code>"x"</code>.
	 *
	 * @param name the name of a field
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a field name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateFieldName(String name) {
		return validateIdentifier(name);
	}

	/**
	 * Validate the given Java identifier.
	 * The identifier must have the same spelling as a Java keyword,
	 * boolean literal (<code>"true"</code>, <code>"false"</code>), or null literal (<code>"null"</code>).
	 * See section 3.8 of the <em>Java Language Specification, Second Edition</em> (JLS2).
	 * A valid identifier can act as a simple type name, method name or field name.
	 *
	 * @param id the Java identifier
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given identifier is a valid Java identifier, otherwise a status 
	 *		object indicating what is wrong with the identifier
	 */
	public static IStatus validateIdentifier(String id) {
		if (scannedIdentifier(id) != null) {
			return JavaModelStatus.VERIFIED_OK;
		} else {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.illegalIdentifier", id), null); //$NON-NLS-1$
		}
	}

	/**
	 * Validate the given import declaration name.
	 * <p>
	 * The name of an import corresponds to a fully qualified type name
	 * or an on-demand package name as defined by ImportDeclaration (JLS2 7.5).
	 * For example, <code>"java.util.*"</code> or <code>"java.util.Hashtable"</code>.
	 *
	 * @param name the import declaration
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as an import declaration, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateImportDeclaration(String name) {
		if (name == null || name.length() == 0) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.import.nullImport"), null); //$NON-NLS-1$
		} 
		if (name.charAt(name.length() - 1) == '*') {
			if (name.charAt(name.length() - 2) == '.') {
				return validatePackageName(name.substring(0, name.length() - 2));
			} else {
				return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.import.unqualifiedImport"), null); //$NON-NLS-1$
			}
		}
		return validatePackageName(name);
	}

	/**
	 * Validate the given Java type name, either simple or qualified.
	 * For example, <code>"java.lang.Object"</code>, or <code>"Object"</code>.
	 * <p>
	 *
	 * @param name the name of a type
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a Java type name, 
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged, 
	 *      otherwise a status object indicating what is wrong with 
	 *      the name
	 */
	public static IStatus validateJavaTypeName(String name) {
		if (name == null) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.nullName"), null); //$NON-NLS-1$
		}
		String trimmed = name.trim();
		if (!name.equals(trimmed)) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.nameWithBlanks"), null); //$NON-NLS-1$
		}
		int index = name.lastIndexOf('.');
		char[] scannedID;
		if (index == -1) {
			// simple name
			scannedID = scannedIdentifier(name);
		} else {
			// qualified name
			String pkg = name.substring(0, index).trim();
			IStatus status = validatePackageName(pkg);
			if (!status.isOK()) {
				return status;
			}
			String type = name.substring(index + 1).trim();
			scannedID = scannedIdentifier(type);
		}
	
		if (scannedID != null) {
			IStatus status = ResourcesPlugin.getWorkspace().validateName(new String(scannedID), IResource.FILE);
			if (!status.isOK()) {
				return status;
			}
			if (CharOperation.contains('$', scannedID)) {
				return new Status(IStatus.WARNING, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.dollarName"), null); //$NON-NLS-1$
			}
			if ((scannedID.length > 0 && Character.isLowerCase(scannedID[0]))) {
				return new Status(IStatus.WARNING, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.lowercaseName"), null); //$NON-NLS-1$
			}
			return JavaModelStatus.VERIFIED_OK;
		} else {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.invalidName", name), null); //$NON-NLS-1$
		}
	}

	/**
	 * Validate the given method name.
	 * The special names "&lt;init&gt;" and "&lt;clinit&gt;" are not valid.
	 * <p>
	 * The syntax for a method  name is defined by Identifier
	 * of MethodDeclarator (JLS2 8.4). For example "println".
	 *
	 * @param name the name of a method
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a method name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validateMethodName(String name) {

		return validateIdentifier(name);
	}

	/**
	 * Validate the given package name.
	 * <p>
	 * The syntax of a package name corresponds to PackageName as
	 * defined by PackageDeclaration (JLS2 7.4). For example, <code>"java.lang"</code>.
	 * <p>
	 * Note that the given name must be a non-empty package name (ie. attempting to
	 * validate the default package will return an error status.)
	 * Also it must not contain any characters or substrings that are not valid 
	 * on the file system on which workspace root is located.
	 *
	 * @param name the name of a package
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a package name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static IStatus validatePackageName(String name) {

		if (name == null) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.nullName"), null); //$NON-NLS-1$
		}
		int length;
		if ((length = name.length()) == 0) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.emptyName"), null); //$NON-NLS-1$
		}
		if (name.charAt(0) == fgDot || name.charAt(length-1) == fgDot) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.dotName"), null); //$NON-NLS-1$
		}
		if (CharOperation.isWhitespace(name.charAt(0)) || CharOperation.isWhitespace(name.charAt(name.length() - 1))) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.nameWithBlanks"), null); //$NON-NLS-1$
		}
		int dot = 0;
		while (dot != -1 && dot < length-1) {
			if ((dot = name.indexOf(fgDot, dot+1)) != -1 && dot < length-1 && name.charAt(dot+1) == fgDot) {
				return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.consecutiveDotsName"), null); //$NON-NLS-1$
				}
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		StringTokenizer st = new StringTokenizer(name, new String(new char[] {fgDot}));
		boolean firstToken = true;
		while (st.hasMoreTokens()) {
			String typeName = st.nextToken();
			typeName = typeName.trim(); // grammar allows spaces
			char[] scannedID = scannedIdentifier(typeName);
			if (scannedID == null) {
				return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.illegalIdentifier", typeName), null); //$NON-NLS-1$
			}
			IStatus status = workspace.validateName(new String(scannedID), IResource.FOLDER);
			if (!status.isOK()) {
				return status;
			}
			if (firstToken && scannedID.length > 0 && Character.isUpperCase(scannedID[0])) {
				return new Status(IStatus.WARNING, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.uppercaseName"), null); //$NON-NLS-1$
			}
			firstToken = false;
		}
		return JavaModelStatus.VERIFIED_OK;
	}
	
	/**
	 * Validate a given classpath and output location for a project, using the following rules:
	 * <ul>
	 *   <li> Classpath entries cannot collide with each other, i.e. all entry paths must be unique.
	 *   <li> The project output location path cannot be null, must be absolute and located inside the project.
	 *   <li> Specific output locations (specified on source entries) can be null, if not they must be located inside the project,
	 *   <li> A project entry cannot refer to itself directly.
     *   <li> Classpath entries or output locations cannot coincidate or be nested in each other, except for the following scenarii listed below:
	 *      <ul><li> A source folder can coincidate with an output location, in which case this output can then contain library archives.
	 *          <li> A source/library folder can be nested in any source folder as long as the nested folder is excluded from the enclosing one.
	 * 			<li> An output location can be nested in a source folder, if the source folder coincidates with the project itself.
	 *      </ul>
	 * </ul>
	 * 
	 *  Note that the classpath entries are not validated automatically. Only bound variables or containers are considered 
	 *  in the checking process (this allows to perform a consistency check on a classpath which has references to
	 *  yet non existing projects, folders, ...).
	 *  <p>
	 *  This validation is intended to anticipate classpath issues prior to assigning it to a project. In particular, it will automatically
	 *  be performed during the classpath setting operation (if validation fails, the classpath setting will not complete).
	 *  <p>
	 * @param javaProject the given java project
	 * @param classpath a given classpath
	 * @param outputLocation a given output location
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given classpath and output location are compatible, otherwise a status 
	 *		object indicating what is wrong with the classpath or output location
	 * @since 2.0
	 */
	public static IJavaModelStatus validateClasspath(IJavaProject javaProject, IClasspathEntry[] rawClasspath, IPath projectOutputLocation) {
	
		IProject project = javaProject.getProject();
		IPath projectPath= project.getFullPath();
	
		/* validate output location */
		if (projectOutputLocation == null) {
			return new JavaModelStatus(IJavaModelStatusConstants.NULL_PATH);
		}
		if (projectOutputLocation.isAbsolute()) {
			if (!projectPath.isPrefixOf(projectOutputLocation)) {
				return new JavaModelStatus(IJavaModelStatusConstants.PATH_OUTSIDE_PROJECT, javaProject, projectOutputLocation.toString());
			}
		} else {
			return new JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, projectOutputLocation);
		}
	
		boolean hasSource = false;
		boolean hasLibFolder = false;
	

		// tolerate null path, it will be reset to default
		if (rawClasspath == null) 
			return JavaModelStatus.VERIFIED_OK;
		
		// retrieve resolved classpath
		IClasspathEntry[] classpath; 
		try {
			classpath = ((JavaProject)javaProject).getResolvedClasspath(rawClasspath, true/*ignore pb*/, false/*no marker*/);
		} catch(JavaModelException e){
			return e.getJavaModelStatus();
		}
		int length = classpath.length; 

		int outputCount = 1;
		IPath[] outputLocations	= new IPath[length+1];
		boolean[] allowNestingInOutputLocations = new boolean[length+1];
		outputLocations[0] = projectOutputLocation;
		
		// retrieve and check output locations
		IPath potentialNestedOutput = null;
		int sourceEntryCount = 0;
		for (int i = 0 ; i < length; i++) {
			IClasspathEntry resolvedEntry = classpath[i];
			switch(resolvedEntry.getEntryKind()){
				case IClasspathEntry.CPE_SOURCE :
					sourceEntryCount++;
					IPath customOutput; 
					if ((customOutput = resolvedEntry.getOutputLocation()) != null) {
						// ensure custom output is in project
						if (customOutput.isAbsolute()) {
							if (!javaProject.getPath().isPrefixOf(customOutput)) {
								return new JavaModelStatus(IJavaModelStatusConstants.PATH_OUTSIDE_PROJECT, javaProject, customOutput.toString());
							}
						} else {
							return new JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, customOutput);
						}
						
						// ensure custom output doesn't conflict with other outputs
						int index;
						if ((index = indexOfMatchingPath(customOutput, outputLocations, outputCount)) != -1) {
							continue; // already found
						}
						if ((index = indexOfEnclosingPath(customOutput, outputLocations, outputCount)) != -1) {
							if (index == 0) {
								// custom output is nested in project's output: need to check if all source entries have a custom
								// output before complaining
								if (potentialNestedOutput == null) potentialNestedOutput = customOutput;
							} else {
								return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOutputInOutput", customOutput.makeRelative().toString(), outputLocations[index].makeRelative().toString())); //$NON-NLS-1$
							}
						}
						outputLocations[outputCount++] = resolvedEntry.getOutputLocation();
					}
			}	
		}	
		// allow custom output nesting in project's output if all source entries have a custom output
		if (potentialNestedOutput != null && sourceEntryCount > outputCount-1) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOutputInOutput", potentialNestedOutput.makeRelative().toString(), outputLocations[0].makeRelative().toString())); //$NON-NLS-1$
		}
		
		for (int i = 0 ; i < length; i++) {
			IClasspathEntry resolvedEntry = classpath[i];
			int index;
			switch(resolvedEntry.getEntryKind()){
				
				case IClasspathEntry.CPE_SOURCE :
					hasSource = true;
					if ((index = indexOfMatchingPath(resolvedEntry.getPath(), outputLocations, outputCount)) != -1){
						allowNestingInOutputLocations[index] = true;
					}
					break;

				case IClasspathEntry.CPE_LIBRARY:
					hasLibFolder |= !org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(resolvedEntry.getPath().lastSegment());
					if ((index = indexOfMatchingPath(resolvedEntry.getPath(), outputLocations, outputCount)) != -1){
						allowNestingInOutputLocations[index] = true;
					}
					break;
			}
		}
		if (!hasSource && !hasLibFolder) { // if no source and no lib folder, then allowed
			for (int i = 0; i < outputCount; i++) allowNestingInOutputLocations[i] = true;
		}
		
		HashSet pathes = new HashSet(length);
		
		// check all entries
		for (int i = 0 ; i < length; i++) {
			IClasspathEntry entry = classpath[i];
	
			if (entry == null) continue;
	
			IPath entryPath = entry.getPath();
			int kind = entry.getEntryKind();
	
			// complain if duplicate path
			if (!pathes.add(entryPath)){
				return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION, Util.bind("classpath.duplicateEntryPath", entryPath.makeRelative().toString())); //$NON-NLS-1$
			}
			// no further check if entry coincidates with project or output location
			if (entryPath.equals(projectPath)){
				// complain if self-referring project entry
				if (kind == IClasspathEntry.CPE_PROJECT){
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, Util.bind("classpath.cannotReferToItself", entryPath.makeRelative().toString()));//$NON-NLS-1$
				}
				// tolerate nesting output in src if src==prj
				continue;
			}
	
			// allow nesting source entries in each other as long as the outer entry excludes the inner one
			if (kind == IClasspathEntry.CPE_SOURCE 
					|| (kind == IClasspathEntry.CPE_LIBRARY && !org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(entryPath.lastSegment()))){
				for (int j = 0; j < classpath.length; j++){
					IClasspathEntry otherEntry = classpath[j];
					if (otherEntry == null) continue;
					int otherKind = otherEntry.getEntryKind();
					IPath otherPath = otherEntry.getPath();
					if (entry != otherEntry 
						&& (otherKind == IClasspathEntry.CPE_SOURCE 
								|| (otherKind == IClasspathEntry.CPE_LIBRARY 
										&& !org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(otherPath.lastSegment())))){
						char[][] exclusionPatterns;
						if (otherPath.isPrefixOf(entryPath) 
								&& !otherPath.equals(entryPath)
								&& !Util.isExcluded(entryPath.append("*"), exclusionPatterns = ((ClasspathEntry)otherEntry).fullExclusionPatternChars())) { //$NON-NLS-1$
									
							String exclusionPattern = entryPath.removeFirstSegments(otherPath.segmentCount()).segment(0);
							if (Util.isExcluded(entryPath, exclusionPatterns)) {
								return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.mustEndWithSlash", exclusionPattern, entryPath.makeRelative().toString())); //$NON-NLS-1$
							} else {
								if (otherKind == IClasspathEntry.CPE_SOURCE) {
									exclusionPattern += '/';
									return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestEntryInEntry", new String[] {entryPath.makeRelative().toString(), otherEntry.getPath().makeRelative().toString(), exclusionPattern})); //$NON-NLS-1$
								} else {
									return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestEntryInLibrary", new String[] {entryPath.makeRelative().toString(), otherEntry.getPath().makeRelative().toString()})); //$NON-NLS-1$
								}
							}
						}
					}
				}
			}
			
			// prevent nesting output location inside entry
			int index;
			if ((index = indexOfNestedPath(entryPath, outputLocations, outputCount)) != -1) {
				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOutputInEntry", outputLocations[index].makeRelative().toString(), entryPath.makeRelative().toString())); //$NON-NLS-1$
			}

			// prevent nesting entry inside output location - when distinct from project or a source folder
			if ((index = indexOfEnclosingPath(entryPath, outputLocations, outputCount)) != -1) {
				if (!allowNestingInOutputLocations[index]) {
					// allow nesting in project's output if all source entries have a custom output
					if (index != 0 || sourceEntryCount > outputCount - 1) {
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestEntryInOutput", entryPath.makeRelative().toString(), outputLocations[index].makeRelative().toString())); //$NON-NLS-1$
					}
				}
			}
		}
		return JavaModelStatus.VERIFIED_OK;	
	}
	
	/**
	 * Returns a java model status describing the problem related to this classpath entry if any, 
	 * a status object with code <code>IStatus.OK</code> if the entry is fine.
	 * (i.e. if the given classpath entry denotes a valid element to be referenced onto a classpath).
	 * 
	 * @param javaProject the given java project
	 * @param entry the given classpath entry
	 * @param checkSourceAttachment a flag to determine if source attachement should be checked
	 * @return a java model status describing the problem related to this classpath entry if any, a status object with code <code>IStatus.OK</code> if the entry is fine
	 * @since 2.0
	 */
	public static IJavaModelStatus validateClasspathEntry(IJavaProject javaProject, IClasspathEntry entry, boolean checkSourceAttachment){
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();			
		IPath path = entry.getPath();
		switch(entry.getEntryKind()){
	
			// container entry check
			case IClasspathEntry.CPE_CONTAINER :
				if (path != null && path.segmentCount() >= 1){
					try {
						IClasspathContainer container = JavaCore.getClasspathContainer(path, javaProject);
						// container retrieval is performing validation check on container entry kinds.
						if (container == null){
							ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(path.segment(0));
							String description = null;
							if (initializer != null) {
								description = initializer.getDescription(path);
							}
							if (description == null) description = path.makeRelative().toString();
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundContainerPath", description)); //$NON-NLS-1$
						}
						IClasspathEntry[] containerEntries = container.getClasspathEntries();
						if (containerEntries != null){
							for (int i = 0, length = containerEntries.length; i < length; i++){
								IClasspathEntry containerEntry = containerEntries[i];
								int kind = containerEntry == null ? 0 : containerEntry.getEntryKind();
								if (containerEntry == null
									|| kind == IClasspathEntry.CPE_SOURCE
									|| kind == IClasspathEntry.CPE_VARIABLE
									|| kind == IClasspathEntry.CPE_CONTAINER){
										String description = container.getDescription();
										if (description == null) description = path.makeRelative().toString();
										return new JavaModelStatus(
											IJavaModelStatusConstants.INVALID_CP_CONTAINER_ENTRY,
											Util.bind("classpath.invalidContainer", description)); //$NON-NLS-1$
								}
								IJavaModelStatus containerEntryStatus = validateClasspathEntry(javaProject, containerEntry, checkSourceAttachment);
								if (!containerEntryStatus.isOK()){
									return containerEntryStatus;
								}
							}
						}
					} catch(JavaModelException e){
						return new JavaModelStatus(e);
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalContainerPath", path.makeRelative().toString()));					 //$NON-NLS-1$
				}
				break;
				
			// variable entry check
			case IClasspathEntry.CPE_VARIABLE :
				if (path != null && path.segmentCount() >= 1){
					entry = JavaCore.getResolvedClasspathEntry(entry);
					if (entry == null){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundVariablePath", path.makeRelative().toString())); //$NON-NLS-1$
					}
					return validateClasspathEntry(javaProject, entry, checkSourceAttachment);
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalVariablePath", path.makeRelative().toString()));					 //$NON-NLS-1$
				}

			// library entry check
			case IClasspathEntry.CPE_LIBRARY :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IPath sourceAttachment = entry.getSourceAttachmentPath();
					Object target = JavaModel.getTarget(workspaceRoot, path, true);
					if (target instanceof IResource){
						IResource resolvedResource = (IResource) target;
						switch(resolvedResource.getType()){
							case IResource.FILE :
								String extension = resolvedResource.getFileExtension();
								if ("jar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)){ // internal binary archive //$NON-NLS-2$ //$NON-NLS-1$
									if (checkSourceAttachment 
										&& sourceAttachment != null
										&& !sourceAttachment.isEmpty()
										&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
										return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", sourceAttachment.makeRelative().toString(), path.makeRelative().toString())); //$NON-NLS-1$
									}
								}
								break;
							case IResource.FOLDER :	// internal binary folder
								if (checkSourceAttachment 
									&& sourceAttachment != null 
									&& !sourceAttachment.isEmpty()
									&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
									return  new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", sourceAttachment.makeRelative().toString(), path.makeRelative().toString())); //$NON-NLS-1$
								}
						}
					} else if (target instanceof File){
						if (checkSourceAttachment 
							&& sourceAttachment != null 
							&& !sourceAttachment.isEmpty()
							&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
							return  new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", sourceAttachment.toString(), path.makeRelative().toString())); //$NON-NLS-1$
						}
					} else {
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundLibrary", path.makeRelative().toString())); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalLibraryPath", path.makeRelative().toString())); //$NON-NLS-1$
				}
				break;

			// project entry check
			case IClasspathEntry.CPE_PROJECT :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IProject project = workspaceRoot.getProject(path.segment(0));
					try {
						if (!project.exists() || !project.hasNature(JavaCore.NATURE_ID)){
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundProject", path.makeRelative().segment(0).toString())); //$NON-NLS-1$
						}
						if (!project.isOpen()){
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.closedProject", path.segment(0).toString())); //$NON-NLS-1$
						}
					} catch (CoreException e){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundProject", path.segment(0).toString())); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalProjectPath", path.segment(0).toString())); //$NON-NLS-1$
				}
				break;

			// project source folder
			case IClasspathEntry.CPE_SOURCE :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IPath projectPath= javaProject.getProject().getFullPath();
					if (!projectPath.isPrefixOf(path) || JavaModel.getTarget(workspaceRoot, path, true) == null){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceFolder", path.makeRelative().toString())); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalSourceFolderPath", path.makeRelative().toString())); //$NON-NLS-1$
				}
				break;
		}
		return JavaModelStatus.VERIFIED_OK;		
	}
}
