/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.Util;
import org.w3c.dom.Element;

/**
 * @see IClasspathEntry
 */
public class ClasspathEntry implements IClasspathEntry {

	/**
	 * Describes the kind of classpath entry - one of 
	 * CPE_PROJECT, CPE_LIBRARY, CPE_SOURCE, CPE_VARIABLE or CPE_CONTAINER
	 */
	public int entryKind;

	/**
	 * Describes the kind of package fragment roots found on
	 * this classpath entry - either K_BINARY or K_SOURCE or
	 * K_OUTPUT.
	 */
	public int contentKind;

	/**
	 * The meaning of the path of a classpath entry depends on its entry kind:<ul>
	 *	<li>Source code in the current project (<code>CPE_SOURCE</code>) -  
	 *      The path associated with this entry is the absolute path to the root folder. </li>
	 *	<li>A binary library in the current project (<code>CPE_LIBRARY</code>) - the path
	 *		associated with this entry is the absolute path to the JAR (or root folder), and 
	 *		in case it refers to an external JAR, then there is no associated resource in 
	 *		the workbench.
	 *	<li>A required project (<code>CPE_PROJECT</code>) - the path of the entry denotes the
	 *		path to the corresponding project resource.</li>
	 *  <li>A variable entry (<code>CPE_VARIABLE</code>) - the first segment of the path 
	 *      is the name of a classpath variable. If this classpath variable
	 *		is bound to the path <it>P</it>, the path of the corresponding classpath entry
	 *		is computed by appending to <it>P</it> the segments of the returned
	 *		path without the variable.</li>
	 *  <li> A container entry (<code>CPE_CONTAINER</code>) - the first segment of the path is denoting
	 *     the unique container identifier (for which a <code>ClasspathContainerInitializer</code> could be
	 * 	registered), and the remaining segments are used as additional hints for resolving the container entry to
	 * 	an actual <code>IClasspathContainer</code>.</li>
	 */
	public IPath path;

	/**
	 * Patterns allowing to include/exclude portions of the resource tree denoted by this entry path.
	 */
	public IPath[] inclusionPatterns;
	private char[][] fullCharInclusionPatterns;
	public IPath[] exclusionPatterns;
	private char[][] fullCharExclusionPatterns;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray() }; //$NON-NLS-1$

	private String rootID;
	
	/*
	 * Default inclusion pattern set
	 */
	public final static IPath[] INCLUDE_ALL = {};
				
	/*
	 * Default exclusion pattern set
	 */
	public final static IPath[] EXCLUDE_NONE = {};
				
	/**
	 * Describes the path to the source archive associated with this
	 * classpath entry, or <code>null</code> if this classpath entry has no
	 * source attachment.
	 * <p>
	 * Only library and variable classpath entries may have source attachments.
	 * For library classpath entries, the result path (if present) locates a source
	 * archive. For variable classpath entries, the result path (if present) has
	 * an analogous form and meaning as the variable path, namely the first segment 
	 * is the name of a classpath variable.
	 */
	public IPath sourceAttachmentPath;

	/**
	 * Describes the path within the source archive where package fragments
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns 
	 * a non-<code>null</code> value.
	 */
	public IPath sourceAttachmentRootPath;

	/**
	 * Specific output location (for this source entry)
	 */
	public IPath specificOutputLocation;
	
	/**
	 * A constant indicating an output location.
	 */
	public static final int K_OUTPUT = 10;

	/**
	 * The export flag
	 */
	public boolean isExported;

	/**
	 * Creates a class path entry of the specified kind with the given path.
	 */
	public ClasspathEntry(
		int contentKind,
		int entryKind,
		IPath path,
		IPath[] inclusionPatterns,
		IPath[] exclusionPatterns,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		IPath specificOutputLocation,
		boolean isExported) {

		this.contentKind = contentKind;
		this.entryKind = entryKind;
		this.path = path;
		this.inclusionPatterns = inclusionPatterns;
	    if (inclusionPatterns != INCLUDE_ALL && inclusionPatterns.length > 0) {
			this.fullCharInclusionPatterns = UNINIT_PATTERNS;
	    } else {
			this.fullCharInclusionPatterns = null; // empty inclusion pattern means everything is included
	    }
		this.exclusionPatterns = exclusionPatterns;
	    if (exclusionPatterns.length > 0) {
			this.fullCharExclusionPatterns = UNINIT_PATTERNS;
	    } else {
			this.fullCharExclusionPatterns = null; // empty exclusion pattern means nothing is excluded
	    }
		this.sourceAttachmentPath = sourceAttachmentPath;
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
		this.specificOutputLocation = specificOutputLocation;
		this.isExported = isExported;
	}
	
	/*
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars() {

		if (this.fullCharExclusionPatterns == UNINIT_PATTERNS) {
			int length = this.exclusionPatterns.length;
			this.fullCharExclusionPatterns = new char[length][];
			IPath prefixPath = this.path.removeTrailingSeparator();
			for (int i = 0; i < length; i++) {
				this.fullCharExclusionPatterns[i] = 
					prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
			}
		}
		return this.fullCharExclusionPatterns;
	}
	
	/*
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullInclusionPatternChars() {

		if (this.fullCharInclusionPatterns == UNINIT_PATTERNS) {
			int length = this.inclusionPatterns.length;
			this.fullCharInclusionPatterns = new char[length][];
			IPath prefixPath = this.path.removeTrailingSeparator();
			for (int i = 0; i < length; i++) {
				this.fullCharInclusionPatterns[i] = 
					prefixPath.append(this.inclusionPatterns[i]).toString().toCharArray();
			}
		}
		return this.fullCharInclusionPatterns;
	}

	/**
	 * Returns the XML encoding of the class path.
	 */
	public void elementEncode(XMLWriter writer, IPath projectPath, boolean indent, boolean newLine) {
		HashMap parameters = new HashMap();
		
		parameters.put("kind", ClasspathEntry.kindToString(this.entryKind));//$NON-NLS-1$
		
		IPath xmlPath = this.path;
		if (this.entryKind != IClasspathEntry.CPE_VARIABLE && this.entryKind != IClasspathEntry.CPE_CONTAINER) {
			// translate to project relative from absolute (unless a device path)
			if (xmlPath.isAbsolute()) {
				if (projectPath != null && projectPath.isPrefixOf(xmlPath)) {
					if (xmlPath.segment(0).equals(projectPath.segment(0))) {
						xmlPath = xmlPath.removeFirstSegments(1);
						xmlPath = xmlPath.makeRelative();
					} else {
						xmlPath = xmlPath.makeAbsolute();
					}
				}
			}
		}
		parameters.put("path", String.valueOf(xmlPath));//$NON-NLS-1$
		
		if (this.sourceAttachmentPath != null) {
			xmlPath = this.sourceAttachmentPath;
			// translate to project relative from absolute 
			if (this.entryKind != IClasspathEntry.CPE_VARIABLE && projectPath != null && projectPath.isPrefixOf(xmlPath)) {
				if (xmlPath.segment(0).equals(projectPath.segment(0))) {
					xmlPath = xmlPath.removeFirstSegments(1);
					xmlPath = xmlPath.makeRelative();
				}
			}
			parameters.put("sourcepath", String.valueOf(xmlPath));//$NON-NLS-1$
		}
		if (this.sourceAttachmentRootPath != null) {
			parameters.put("rootpath", String.valueOf(this.sourceAttachmentRootPath));//$NON-NLS-1$
		}
		if (this.isExported) {
			parameters.put("exported", "true");//$NON-NLS-1$//$NON-NLS-2$
		}
		if (this.inclusionPatterns != null && this.inclusionPatterns.length > 0) {
			StringBuffer includeRule = new StringBuffer(10);
			for (int i = 0, max = this.inclusionPatterns.length; i < max; i++){
				if (i > 0) includeRule.append('|');
				includeRule.append(this.inclusionPatterns[i]);
			}
			parameters.put("including", String.valueOf(includeRule));//$NON-NLS-1$
		}
		if (this.exclusionPatterns != null && this.exclusionPatterns.length > 0) {
			StringBuffer excludeRule = new StringBuffer(10);
			for (int i = 0, max = this.exclusionPatterns.length; i < max; i++){
				if (i > 0) excludeRule.append('|');
				excludeRule.append(this.exclusionPatterns[i]);
			}
			parameters.put("excluding", String.valueOf(excludeRule));//$NON-NLS-1$
		}
		
		if (this.specificOutputLocation != null) {
			IPath outputLocation = this.specificOutputLocation.removeFirstSegments(1);
			outputLocation = outputLocation.makeRelative();
			parameters.put("output", String.valueOf(outputLocation));//$NON-NLS-1$
		}

		writer.printTag("classpathentry", parameters, indent, newLine, true);//$NON-NLS-1$
	}
	
	public static IClasspathEntry elementDecode(Element element, IJavaProject project) {
	
		IPath projectPath = project.getProject().getFullPath();
		String kindAttr = element.getAttribute("kind"); //$NON-NLS-1$
		String pathAttr = element.getAttribute("path"); //$NON-NLS-1$

		// ensure path is absolute
		IPath path = new Path(pathAttr); 		
		int kind = kindFromString(kindAttr);
		if (kind != IClasspathEntry.CPE_VARIABLE && kind != IClasspathEntry.CPE_CONTAINER && !path.isAbsolute()) {
			path = projectPath.append(path);
		}
		// source attachment info (optional)
		IPath sourceAttachmentPath = 
			element.hasAttribute("sourcepath")	//$NON-NLS-1$
			? new Path(element.getAttribute("sourcepath")) //$NON-NLS-1$
			: null;
		if (kind != IClasspathEntry.CPE_VARIABLE && sourceAttachmentPath != null && !sourceAttachmentPath.isAbsolute()) {
			sourceAttachmentPath = projectPath.append(sourceAttachmentPath);
		}
		IPath sourceAttachmentRootPath = 
			element.hasAttribute("rootpath") //$NON-NLS-1$
			? new Path(element.getAttribute("rootpath")) //$NON-NLS-1$
			: null;
		
		// exported flag (optional)
		boolean isExported = element.getAttribute("exported").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$

		// inclusion patterns (optional)
		String inclusion = element.getAttribute("including"); //$NON-NLS-1$ 
		IPath[] inclusionPatterns = INCLUDE_ALL;
		if (!inclusion.equals("")) { //$NON-NLS-1$ 
			char[][] patterns = CharOperation.splitOn('|', inclusion.toCharArray());
			int patternCount;
			if ((patternCount  = patterns.length) > 0) {
				inclusionPatterns = new IPath[patternCount];
				for (int j = 0; j < patterns.length; j++){
					inclusionPatterns[j] = new Path(new String(patterns[j]));
				}
			}
		}

		// exclusion patterns (optional)
		String exclusion = element.getAttribute("excluding"); //$NON-NLS-1$ 
		IPath[] exclusionPatterns = EXCLUDE_NONE;
		if (!exclusion.equals("")) { //$NON-NLS-1$ 
			char[][] patterns = CharOperation.splitOn('|', exclusion.toCharArray());
			int patternCount;
			if ((patternCount  = patterns.length) > 0) {
				exclusionPatterns = new IPath[patternCount];
				for (int j = 0; j < patterns.length; j++){
					exclusionPatterns[j] = new Path(new String(patterns[j]));
				}
			}
		}

		// custom output location
		IPath outputLocation = element.hasAttribute("output") ? projectPath.append(element.getAttribute("output")) : null; //$NON-NLS-1$ //$NON-NLS-2$
		
		// recreate the CP entry
		switch (kind) {

			case IClasspathEntry.CPE_PROJECT :
				return JavaCore.newProjectEntry(path, isExported);
				
			case IClasspathEntry.CPE_LIBRARY :
				return JavaCore.newLibraryEntry(
												path,
												sourceAttachmentPath,
												sourceAttachmentRootPath,
												isExported);
				
			case IClasspathEntry.CPE_SOURCE :
				// must be an entry in this project or specify another project
				String projSegment = path.segment(0);
				if (projSegment != null && projSegment.equals(project.getElementName())) { // this project
					return JavaCore.newSourceEntry(path, inclusionPatterns, exclusionPatterns, outputLocation);
				} else { // another project
					return JavaCore.newProjectEntry(path, isExported);
				}

			case IClasspathEntry.CPE_VARIABLE :
				return JavaCore.newVariableEntry(
						path,
						sourceAttachmentPath,
						sourceAttachmentRootPath, 
						isExported);
				
			case IClasspathEntry.CPE_CONTAINER :
				return JavaCore.newContainerEntry(
						path,
						isExported);

			case ClasspathEntry.K_OUTPUT :
				if (!path.isAbsolute()) return null;
				return new ClasspathEntry(
						ClasspathEntry.K_OUTPUT,
						IClasspathEntry.CPE_LIBRARY,
						path,
						ClasspathEntry.INCLUDE_ALL, 
						ClasspathEntry.EXCLUDE_NONE, 
						null, // source attachment
						null, // source attachment root
						null, // custom output location
						false);
			default :
				throw new Assert.AssertionFailedException(Util.bind("classpath.unknownKind", kindAttr)); //$NON-NLS-1$
		}
	}

	/**
	 * Returns true if the given object is a classpath entry
	 * with equivalent attributes.
	 */
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object instanceof IClasspathEntry) {
			IClasspathEntry otherEntry = (IClasspathEntry) object;

			if (this.contentKind != otherEntry.getContentKind())
				return false;

			if (this.entryKind != otherEntry.getEntryKind())
				return false;

			if (this.isExported != otherEntry.isExported())
				return false;

			if (!this.path.equals(otherEntry.getPath()))
				return false;

			IPath otherPath = otherEntry.getSourceAttachmentPath();
			if (this.sourceAttachmentPath == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.sourceAttachmentPath.equals(otherPath))
					return false;
			}

			otherPath = otherEntry.getSourceAttachmentRootPath();
			if (this.sourceAttachmentRootPath == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.sourceAttachmentRootPath.equals(otherPath))
					return false;
			}

			IPath[] otherIncludes = otherEntry.getInclusionPatterns();
			if (this.inclusionPatterns != otherIncludes){
			    if (this.inclusionPatterns == null) return false;
				int includeLength = this.inclusionPatterns.length;
				if (otherIncludes == null || otherIncludes.length != includeLength) 
					return false;
				for (int i = 0; i < includeLength; i++) {
					// compare toStrings instead of IPaths 
					// since IPath.equals is specified to ignore trailing separators
					if (!this.inclusionPatterns[i].toString().equals(otherIncludes[i].toString()))
						return false;
				}
			}

			IPath[] otherExcludes = otherEntry.getExclusionPatterns();
			if (this.exclusionPatterns != otherExcludes){
			    if (this.exclusionPatterns == null) return false;
				int excludeLength = this.exclusionPatterns.length;
				if (otherExcludes == null || otherExcludes.length != excludeLength) 
					return false;
				for (int i = 0; i < excludeLength; i++) {
					// compare toStrings instead of IPaths 
					// since IPath.equals is specified to ignore trailing separators
					if (!this.exclusionPatterns[i].toString().equals(otherExcludes[i].toString()))
						return false;
				}
			}
			
			otherPath = otherEntry.getOutputLocation();
			if (this.specificOutputLocation == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.specificOutputLocation.equals(otherPath))
					return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see IClasspathEntry
	 */
	public int getContentKind() {
		return this.contentKind;
	}

	/**
	 * @see IClasspathEntry
	 */
	public int getEntryKind() {
		return this.entryKind;
	}

	/**
	 * @see IClasspathEntry#getExclusionPatterns()
	 */
	public IPath[] getExclusionPatterns() {
		return this.exclusionPatterns;
	}

	/**
	 * @see IClasspathEntry#getExclusionPatterns()
	 */
	public IPath[] getInclusionPatterns() {
		return this.inclusionPatterns;
	}

	/**
	 * @see IClasspathEntry#getOutputLocation()
	 */
	public IPath getOutputLocation() {
		return this.specificOutputLocation;
	}

	/**
	 * @see IClasspathEntry
	 */
	public IPath getPath() {
		return this.path;
	}

	/**
	 * @see IClasspathEntry
	 */
	public IPath getSourceAttachmentPath() {
		return this.sourceAttachmentPath;
	}

	/**
	 * @see IClasspathEntry
	 */
	public IPath getSourceAttachmentRootPath() {
		return this.sourceAttachmentRootPath;
	}

	/**
	 * Returns the hash code for this classpath entry
	 */
	public int hashCode() {
		return this.path.hashCode();
	}

	/**
	 * @see IClasspathEntry#isExported()
	 */
	public boolean isExported() {
		return this.isExported;
	}

	/**
	 * Returns the kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int kindFromString(String kindStr) {

		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
			return IClasspathEntry.CPE_PROJECT;
		if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
			return IClasspathEntry.CPE_VARIABLE;
		if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
			return IClasspathEntry.CPE_CONTAINER;
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
			case IClasspathEntry.CPE_CONTAINER :
				return "con"; //$NON-NLS-1$
			case ClasspathEntry.K_OUTPUT :
				return "output"; //$NON-NLS-1$
			default :
				return "unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * Returns a printable representation of this classpath entry.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getPath().toString());
		buffer.append('[');
		switch (getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY :
				buffer.append("CPE_LIBRARY"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_PROJECT :
				buffer.append("CPE_PROJECT"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_SOURCE :
				buffer.append("CPE_SOURCE"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_VARIABLE :
				buffer.append("CPE_VARIABLE"); //$NON-NLS-1$
				break;
			case IClasspathEntry.CPE_CONTAINER :
				buffer.append("CPE_CONTAINER"); //$NON-NLS-1$
				break;
		}
		buffer.append("]["); //$NON-NLS-1$
		switch (getContentKind()) {
			case IPackageFragmentRoot.K_BINARY :
				buffer.append("K_BINARY"); //$NON-NLS-1$
				break;
			case IPackageFragmentRoot.K_SOURCE :
				buffer.append("K_SOURCE"); //$NON-NLS-1$
				break;
			case ClasspathEntry.K_OUTPUT :
				buffer.append("K_OUTPUT"); //$NON-NLS-1$
				break;
		}
		buffer.append(']');
		if (getSourceAttachmentPath() != null) {
			buffer.append("[sourcePath:"); //$NON-NLS-1$
			buffer.append(getSourceAttachmentPath());
			buffer.append(']');
		}
		if (getSourceAttachmentRootPath() != null) {
			buffer.append("[rootPath:"); //$NON-NLS-1$
			buffer.append(getSourceAttachmentRootPath());
			buffer.append(']');
		}
		buffer.append("[isExported:"); //$NON-NLS-1$
		buffer.append(this.isExported);
		buffer.append(']');
		IPath[] patterns = getInclusionPatterns();
		int length;
		if ((length = patterns == null ? 0 : patterns.length) > 0) {
			buffer.append("[including:"); //$NON-NLS-1$
			for (int i = 0; i < length; i++) {
				buffer.append(patterns[i]);
				if (i != length-1) {
					buffer.append('|');
				}
			}
			buffer.append(']');
		}
		patterns = getExclusionPatterns();
		if ((length = patterns == null ? 0 : patterns.length) > 0) {
			buffer.append("[excluding:"); //$NON-NLS-1$
			for (int i = 0; i < length; i++) {
				buffer.append(patterns[i]);
				if (i != length-1) {
					buffer.append('|');
				}
			}
			buffer.append(']');
		}
		if (getOutputLocation() != null) {
			buffer.append("[output:"); //$NON-NLS-1$
			buffer.append(getOutputLocation());
			buffer.append(']');
		}
		return buffer.toString();
	}
	
	/**
	 * Answers an ID which is used to distinguish entries during package
	 * fragment root computations
	 */
	public String rootID(){

		if (this.rootID == null) {
			switch(this.entryKind){
				case IClasspathEntry.CPE_LIBRARY :
					this.rootID = "[LIB]"+this.path;  //$NON-NLS-1$
					break;
				case IClasspathEntry.CPE_PROJECT :
					this.rootID = "[PRJ]"+this.path;  //$NON-NLS-1$
					break;
				case IClasspathEntry.CPE_SOURCE :
					this.rootID = "[SRC]"+this.path;  //$NON-NLS-1$
					break;
				case IClasspathEntry.CPE_VARIABLE :
					this.rootID = "[VAR]"+this.path;  //$NON-NLS-1$
					break;
				case IClasspathEntry.CPE_CONTAINER :
					this.rootID = "[CON]"+this.path;  //$NON-NLS-1$
					break;
				default :
					this.rootID = "";  //$NON-NLS-1$
					break;
			}
		}
		return this.rootID;
	}
	
	/**
	 * @see IClasspathEntry
	 * @deprecated
	 */
	public IClasspathEntry getResolvedEntry() {
	
		return JavaCore.getResolvedClasspathEntry(this);
	}
	
	/**
	 * Validate a given classpath and output location for a project, using the following rules:
	 * <ul>
	 *   <li> Classpath entries cannot collide with each other; that is, all entry paths must be unique.
	 *   <li> The project output location path cannot be null, must be absolute and located inside the project.
	 *   <li> Specific output locations (specified on source entries) can be null, if not they must be located inside the project,
	 *   <li> A project entry cannot refer to itself directly (that is, a project cannot prerequisite itself).
     *   <li> Classpath entries or output locations cannot coincidate or be nested in each other, except for the following scenarii listed below:
	 *      <ul><li> A source folder can coincidate with its own output location, in which case this output can then contain library archives. 
	 *                     However, a specific output location cannot coincidate with any library or a distinct source folder than the one referring to it. </li> 
	 *              <li> A source/library folder can be nested in any source folder as long as the nested folder is excluded from the enclosing one. </li>
	 * 			<li> An output location can be nested in a source folder, if the source folder coincidates with the project itself, or if the output
	 * 					location is excluded from the source folder. </li>
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
	 */
	public static IJavaModelStatus validateClasspath(IJavaProject javaProject, IClasspathEntry[] rawClasspath, IPath projectOutputLocation) {
	
		IProject project = javaProject.getProject();
		IPath projectPath= project.getFullPath();
		String projectName = javaProject.getElementName();
	
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
			classpath = ((JavaProject)javaProject).getResolvedClasspath(rawClasspath, null /*output*/, true/*ignore pb*/, false/*no marker*/, null /*no reverse map*/);
		} catch(JavaModelException e){
			return e.getJavaModelStatus();
		}
		int length = classpath.length; 

		int outputCount = 1;
		IPath[] outputLocations	= new IPath[length+1];
		boolean[] allowNestingInOutputLocations = new boolean[length+1];
		outputLocations[0] = projectOutputLocation;
		
		// retrieve and check output locations
		IPath potentialNestedOutput = null; // for error reporting purpose
		int sourceEntryCount = 0;
		boolean disableExclusionPatterns = JavaCore.DISABLED.equals(javaProject.getOption(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, true));
		boolean disableCustomOutputLocations = JavaCore.DISABLED.equals(javaProject.getOption(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, true));
		
		for (int i = 0 ; i < length; i++) {
			IClasspathEntry resolvedEntry = classpath[i];
			switch(resolvedEntry.getEntryKind()){
				case IClasspathEntry.CPE_SOURCE :
					sourceEntryCount++;

					if (disableExclusionPatterns &&
					        ((resolvedEntry.getInclusionPatterns() != null && resolvedEntry.getInclusionPatterns().length > 0) 
					        || (resolvedEntry.getExclusionPatterns() != null && resolvedEntry.getExclusionPatterns().length > 0))) {
						return new JavaModelStatus(IJavaModelStatusConstants.DISABLED_CP_EXCLUSION_PATTERNS, javaProject, resolvedEntry.getPath());
					}
					IPath customOutput; 
					if ((customOutput = resolvedEntry.getOutputLocation()) != null) {

						if (disableCustomOutputLocations) {
							return new JavaModelStatus(IJavaModelStatusConstants.DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS, javaProject, resolvedEntry.getPath());
						}
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
						
						// check exact match
						if ((index = Util.indexOfMatchingPath(customOutput, outputLocations, outputCount)) != -1) {
							continue; // already found
						}
						
						// check nesting
						if ((index = Util.indexOfEnclosingPath(customOutput, outputLocations, outputCount)) != -1) {
							if (index == 0) {
								// custom output is nested in project's output: need to check if all source entries have a custom
								// output before complaining
								if (potentialNestedOutput == null) potentialNestedOutput = customOutput;
							} else {
								return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOutputInOutput", customOutput.makeRelative().toString(), outputLocations[index].makeRelative().toString())); //$NON-NLS-1$
							}
						}
						outputLocations[outputCount++] = customOutput;
					}
			}	
		}	
		// allow custom output nesting in project's output if all source entries have a custom output
		if (sourceEntryCount <= outputCount-1) {
		    allowNestingInOutputLocations[0] = true;
		} else if (potentialNestedOutput != null) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOutputInOutput", potentialNestedOutput.makeRelative().toString(), outputLocations[0].makeRelative().toString())); //$NON-NLS-1$
		}

		for (int i = 0 ; i < length; i++) {
			IClasspathEntry resolvedEntry = classpath[i];
			IPath path = resolvedEntry.getPath();
			int index;
			switch(resolvedEntry.getEntryKind()){
				
				case IClasspathEntry.CPE_SOURCE :
					hasSource = true;
					if ((index = Util.indexOfMatchingPath(path, outputLocations, outputCount)) != -1){
						allowNestingInOutputLocations[index] = true;
					}
					break;

				case IClasspathEntry.CPE_LIBRARY:
					hasLibFolder |= !org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(path.lastSegment());
					if ((index = Util.indexOfMatchingPath(path, outputLocations, outputCount)) != -1){
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
			
			// Build some common strings for status message
			boolean isProjectRelative = entryPath.segment(0).toString().equals(projectName);
			String entryPathMsg = isProjectRelative ? entryPath.removeFirstSegments(1).toString() : entryPath.makeRelative().toString();
	
			// complain if duplicate path
			if (!pathes.add(entryPath)){
				return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION, Util.bind("classpath.duplicateEntryPath", entryPathMsg, projectName)); //$NON-NLS-1$
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
						char[][] inclusionPatterns, exclusionPatterns;
						if (otherPath.isPrefixOf(entryPath) 
								&& !otherPath.equals(entryPath)
								&& !Util.isExcluded(entryPath.append("*"), inclusionPatterns = ((ClasspathEntry)otherEntry).fullInclusionPatternChars(), exclusionPatterns = ((ClasspathEntry)otherEntry).fullExclusionPatternChars(), false)) { //$NON-NLS-1$
							String exclusionPattern = entryPath.removeFirstSegments(otherPath.segmentCount()).segment(0);
							if (Util.isExcluded(entryPath, inclusionPatterns, exclusionPatterns, false)) {
								return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.mustEndWithSlash", exclusionPattern, entryPath.makeRelative().toString())); //$NON-NLS-1$
							} else {
								if (otherKind == IClasspathEntry.CPE_SOURCE) {
									exclusionPattern += '/';
									return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestEntryInEntry", new String[] {entryPath.makeRelative().toString(), otherEntry.getPath().makeRelative().toString(), exclusionPattern})); //$NON-NLS-1$
								} else {
									return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestEntryInLibrary", entryPath.makeRelative().toString(), otherEntry.getPath().makeRelative().toString())); //$NON-NLS-1$
								}
							}
						}
					}
				}
			}
			
			// prevent nesting output location inside entry unless enclosing is a source entry which explicitly exclude the output location
		    char[][] inclusionPatterns = ((ClasspathEntry)entry).fullInclusionPatternChars();
		    char[][] exclusionPatterns = ((ClasspathEntry)entry).fullExclusionPatternChars();
		    for (int j = 0; j < outputCount; j++){
		        IPath currentOutput = outputLocations[j];
    			if (entryPath.equals(currentOutput)) continue;
				if (entryPath.isPrefixOf(currentOutput)) {
				    if (kind != IClasspathEntry.CPE_SOURCE || !Util.isExcluded(currentOutput, inclusionPatterns, exclusionPatterns, true)) {
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOutputInEntry", currentOutput.makeRelative().toString(), entryPath.makeRelative().toString())); //$NON-NLS-1$
				    }
				}
		    }

		    // prevent nesting entry inside output location - when distinct from project or a source folder
		    for (int j = 0; j < outputCount; j++){
		        if (allowNestingInOutputLocations[j]) continue;
		        IPath currentOutput = outputLocations[j];
				if (currentOutput.isPrefixOf(entryPath)) {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestEntryInOutput", entryPath.makeRelative().toString(), currentOutput.makeRelative().toString())); //$NON-NLS-1$
				}
		    }			
		}
		// ensure that no specific output is coincidating with another source folder (only allowed if matching current source folder)
		// 36465 - for 2.0 backward compatibility, only check specific output locations (the default can still coincidate)
		// perform one separate iteration so as to not take precedence over previously checked scenarii (in particular should
		// diagnose nesting source folder issue before this one, for example, [src]"Project/", [src]"Project/source/" and output="Project/" should
		// first complain about missing exclusion pattern
		for (int i = 0 ; i < length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry == null) continue;
			IPath entryPath = entry.getPath();
			int kind = entry.getEntryKind();

			// Build some common strings for status message
			boolean isProjectRelative = entryPath.segment(0).toString().equals(projectName);
			String entryPathMsg = isProjectRelative ? entryPath.removeFirstSegments(1).toString() : entryPath.makeRelative().toString();
	
			if (kind == IClasspathEntry.CPE_SOURCE) {
				IPath output = entry.getOutputLocation();
				if (output == null) continue; // 36465 - for 2.0 backward compatibility, only check specific output locations (the default can still coincidate)
				// if (output == null) output = projectOutputLocation; // if no specific output, still need to check using default output (this line would check default output)
				for (int j = 0; j < length; j++) {
					IClasspathEntry otherEntry = classpath[j];
					if (otherEntry == entry) continue;

					// Build some common strings for status message
					boolean opStartsWithProject = otherEntry.getPath().segment(0).toString().equals(projectName);
					String otherPathMsg = opStartsWithProject ? otherEntry.getPath().removeFirstSegments(1).toString() : otherEntry.getPath().makeRelative().toString();
	
					switch (otherEntry.getEntryKind()) {
						case IClasspathEntry.CPE_SOURCE :
							if (otherEntry.getPath().equals(output)) {
								return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotUseDistinctSourceFolderAsOutput", new String[] {entryPathMsg, otherPathMsg, projectName})); //$NON-NLS-1$
							}
							break;
						case IClasspathEntry.CPE_LIBRARY :
							if (otherEntry.getPath().equals(output)) {
								return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotUseLibraryAsOutput", new String[] {entryPathMsg, otherPathMsg, projectName})); //$NON-NLS-1$
							}
					}
				}
			}			
		}
		return JavaModelStatus.VERIFIED_OK;	
	}
	
	/**
	 * Returns a Java model status describing the problem related to this classpath entry if any, 
	 * a status object with code <code>IStatus.OK</code> if the entry is fine (that is, if the
	 * given classpath entry denotes a valid element to be referenced onto a classpath).
	 * 
	 * @param project the given java project
	 * @param entry the given classpath entry
	 * @param checkSourceAttachment a flag to determine if source attachement should be checked
	 * @param recurseInContainers flag indicating whether validation should be applied to container entries recursively
	 * @return a java model status describing the problem related to this classpath entry if any, a status object with code <code>IStatus.OK</code> if the entry is fine
	 */
	public static IJavaModelStatus validateClasspathEntry(IJavaProject project, IClasspathEntry entry, boolean checkSourceAttachment, boolean recurseInContainers){
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();			
		IPath path = entry.getPath();
	
		// Build some common strings for status message
		String projectName = project.getElementName();
		boolean pathStartsWithProject = path.segment(0).toString().equals(projectName);
		String entryPathMsg = pathStartsWithProject ? path.removeFirstSegments(1).toString() : path.makeRelative().toString();
	
		switch(entry.getEntryKind()){
	
			// container entry check
			case IClasspathEntry.CPE_CONTAINER :
				if (path != null && path.segmentCount() >= 1){
					try {
						IClasspathContainer container = JavaModelManager.getJavaModelManager().getClasspathContainer(path, project);
						// container retrieval is performing validation check on container entry kinds.
						if (container == null){
							return new JavaModelStatus(IJavaModelStatusConstants.CP_CONTAINER_PATH_UNBOUND, project, path);
						} else if (container == JavaModelManager.CONTAINER_INITIALIZATION_IN_PROGRESS) {
							// don't create a marker if initialization is in progress (case of cp initialization batching)
							return JavaModelStatus.VERIFIED_OK;
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
										return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CP_CONTAINER_ENTRY, project, path);
								}
								if (recurseInContainers) {
									IJavaModelStatus containerEntryStatus = validateClasspathEntry(project, containerEntry, checkSourceAttachment, recurseInContainers);
									if (!containerEntryStatus.isOK()){
										return containerEntryStatus;
									}
								} 
							}
						}
					} catch(JavaModelException e){
						return new JavaModelStatus(e);
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalContainerPath", entryPathMsg, projectName));					 //$NON-NLS-1$
				}
				break;
				
			// variable entry check
			case IClasspathEntry.CPE_VARIABLE :
				if (path != null && path.segmentCount() >= 1){
					try {
						entry = JavaCore.getResolvedClasspathEntry(entry);
					} catch (Assert.AssertionFailedException e) {
						// Catch the assertion failure and throw java model exception instead
						// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, e.getMessage());
					}
					if (entry == null){
						return new JavaModelStatus(IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND, project, path);
					}
					return validateClasspathEntry(project, entry, checkSourceAttachment, recurseInContainers);
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalVariablePath", path.makeRelative().toString(), projectName));					 //$NON-NLS-1$
				}
	
			// library entry check
			case IClasspathEntry.CPE_LIBRARY :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IPath sourceAttachment = entry.getSourceAttachmentPath();
					Object target = JavaModel.getTarget(workspaceRoot, path, true);
					if (target != null && project.getOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, true) != JavaCore.IGNORE) {
						long projectTargetJDK = CompilerOptions.versionToJdkLevel(project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
						long libraryJDK = Util.getJdkLevel(target);
						if (libraryJDK != 0 && libraryJDK > projectTargetJDK) {
							return new JavaModelStatus(IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL, project, path, CompilerOptions.versionFromJdkLevel(libraryJDK)); 
						}
					}
					if (target instanceof IResource){
						IResource resolvedResource = (IResource) target;
						switch(resolvedResource.getType()){
							case IResource.FILE :
								if (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(resolvedResource.getFileExtension())) {
									if (checkSourceAttachment 
										&& sourceAttachment != null
										&& !sourceAttachment.isEmpty()
										&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
										return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", new String [] {sourceAttachment.makeRelative().toString(), path.makeRelative().toString(), projectName})); //$NON-NLS-1$
									}
								}
								break;
							case IResource.FOLDER :	// internal binary folder
								if (checkSourceAttachment 
									&& sourceAttachment != null 
									&& !sourceAttachment.isEmpty()
									&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
									return  new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", new String [] {sourceAttachment.makeRelative().toString(), path.makeRelative().toString(), projectName})); //$NON-NLS-1$
								}
						}
					} else if (target instanceof File){
						if (checkSourceAttachment 
							&& sourceAttachment != null 
							&& !sourceAttachment.isEmpty()
							&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
							return  new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", new String [] {sourceAttachment.toString(), path.makeRelative().toString(), projectName})); //$NON-NLS-1$
						}
					} else {
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundLibrary", path.makeRelative().toString(), projectName)); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalLibraryPath", path.makeRelative().toString(), projectName)); //$NON-NLS-1$
				}
				break;
	
			// project entry check
			case IClasspathEntry.CPE_PROJECT :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IProject prereqProjectRsc = workspaceRoot.getProject(path.segment(0));
					IJavaProject prereqProject = JavaCore.create(prereqProjectRsc);
					try {
						if (!prereqProjectRsc.exists() || !prereqProjectRsc.hasNature(JavaCore.NATURE_ID)){
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundProject", path.makeRelative().segment(0).toString(), projectName)); //$NON-NLS-1$
						}
						if (!prereqProjectRsc.isOpen()){
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.closedProject", path.segment(0).toString())); //$NON-NLS-1$
						}
						if (project.getOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, true) != JavaCore.IGNORE) {
							long projectTargetJDK = CompilerOptions.versionToJdkLevel(project.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
							long prereqProjectTargetJDK = CompilerOptions.versionToJdkLevel(prereqProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true));
							if (prereqProjectTargetJDK > projectTargetJDK) {
								return new JavaModelStatus(IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL, project, path, CompilerOptions.versionFromJdkLevel(prereqProjectTargetJDK)); 
							}
						}
					} catch (CoreException e){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundProject", path.segment(0).toString(), projectName)); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalProjectPath", path.segment(0).toString(), projectName)); //$NON-NLS-1$
				}
				break;
	
			// project source folder
			case IClasspathEntry.CPE_SOURCE :
				if (((entry.getInclusionPatterns() != null && entry.getInclusionPatterns().length > 0)
				        	|| (entry.getExclusionPatterns() != null && entry.getExclusionPatterns().length > 0))
						&& JavaCore.DISABLED.equals(project.getOption(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, true))) {
					return new JavaModelStatus(IJavaModelStatusConstants.DISABLED_CP_EXCLUSION_PATTERNS, project, path);
				}
				if (entry.getOutputLocation() != null && JavaCore.DISABLED.equals(project.getOption(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, true))) {
					return new JavaModelStatus(IJavaModelStatusConstants.DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS, project, path);
				}
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IPath projectPath= project.getProject().getFullPath();
					if (!projectPath.isPrefixOf(path) || JavaModel.getTarget(workspaceRoot, path, true) == null){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceFolder", entryPathMsg, projectName)); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalSourceFolderPath", entryPathMsg, projectName)); //$NON-NLS-1$
				}
				break;
		}
		return JavaModelStatus.VERIFIED_OK;		
	}
}
