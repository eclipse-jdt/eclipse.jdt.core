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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.w3c.dom.Document;
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
	 * Patterns allowing to exclude portions of the resource tree denoted by this entry path.	 */
	
	public IPath[] exclusionPatterns;
	private char[][] fullCharExclusionPatterns;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray() }; //$NON-NLS-1$

	private String rootID;
	
	/**
	 * Default exclusion pattern set
	 */
	public final static IPath[] NO_EXCLUSION_PATTERNS = {};
				
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
	 * Specific output location (for this source entry)	 */
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
		IPath[] exclusionPatterns,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		IPath specificOutputLocation,
		boolean isExported) {

		this.contentKind = contentKind;
		this.entryKind = entryKind;
		this.path = path;
		this.exclusionPatterns = exclusionPatterns;
		if (exclusionPatterns.length > 0) {
			this.fullCharExclusionPatterns = UNINIT_PATTERNS;
		}
		this.sourceAttachmentPath = sourceAttachmentPath;
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
		this.specificOutputLocation = specificOutputLocation;
		this.isExported = isExported;
	}
	
	/*
	 * Returns a char based representation of the exclusions patterns full path.	 */
	public char[][] fullExclusionPatternChars() {

		if (this.fullCharExclusionPatterns == UNINIT_PATTERNS) {
			int length = this.exclusionPatterns.length;
			this.fullCharExclusionPatterns = new char[length][];
			IPath prefixPath = path.removeTrailingSeparator();
			for (int i = 0; i < length; i++) {
				this.fullCharExclusionPatterns[i] = 
					prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
			}
		}
		return this.fullCharExclusionPatterns;
	}
	
	/**
	 * Returns the XML encoding of the class path.
	 */
	public Element elementEncode(
		Document document,
		IPath projectPath)
		throws JavaModelException {

		Element element = document.createElement("classpathentry"); //$NON-NLS-1$
		element.setAttribute("kind", kindToString(this.entryKind));	//$NON-NLS-1$
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
		element.setAttribute("path", xmlPath.toString()); //$NON-NLS-1$
		if (this.sourceAttachmentPath != null) {
			element.setAttribute("sourcepath", this.sourceAttachmentPath.toString()); //$NON-NLS-1$
		}
		if (this.sourceAttachmentRootPath != null) {
			element.setAttribute("rootpath", this.sourceAttachmentRootPath.toString()); //$NON-NLS-1$
		}
		if (this.isExported) {
			element.setAttribute("exported", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (this.exclusionPatterns.length > 0) {
			StringBuffer excludeRule = new StringBuffer(10);
			for (int i = 0, max = this.exclusionPatterns.length; i < max; i++){
				if (i > 0) excludeRule.append('|');
				excludeRule.append(this.exclusionPatterns[i]);
			}
			element.setAttribute("excluding", excludeRule.toString());  //$NON-NLS-1$
		}
		
		if (this.specificOutputLocation != null) {
			IPath outputLocation = this.specificOutputLocation.removeFirstSegments(1);
			outputLocation = outputLocation.makeRelative();
			element.setAttribute("output", outputLocation.toString()); //$NON-NLS-1$ 
		}
		return element;
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
		IPath sourceAttachmentRootPath = 
			element.hasAttribute("rootpath") //$NON-NLS-1$
			? new Path(element.getAttribute("rootpath")) //$NON-NLS-1$
			: null;
		
		// exported flag (optional)
		boolean isExported = element.getAttribute("exported").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$

		// exclusion patterns (optional)
		String exclusion = element.getAttribute("excluding"); //$NON-NLS-1$ 
		IPath[] exclusionPatterns = ClasspathEntry.NO_EXCLUSION_PATTERNS;
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
					return JavaCore.newSourceEntry(path, exclusionPatterns, outputLocation);
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
						ClasspathEntry.NO_EXCLUSION_PATTERNS, 
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

			IPath[] otherExcludes = otherEntry.getExclusionPatterns();
			if (this.exclusionPatterns != otherExcludes){
				int excludeLength = this.exclusionPatterns.length;
				if (otherExcludes.length != excludeLength) 
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
		IPath[] patterns = getExclusionPatterns();
		int length;
		if ((length = patterns.length) > 0) {
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
}