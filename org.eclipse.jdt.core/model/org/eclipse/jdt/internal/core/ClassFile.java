package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.ConfigurableOption;
import org.eclipse.jdt.internal.compiler.classfmt.*;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.lang.reflect.Modifier;

/**
 * @see IClassFile
 */

public class ClassFile extends Openable implements IClassFile {
	protected BinaryType fBinaryType = null;
/*
 * Creates a handle to a class file.
 *
 * @exception IllegalArgumentExcpetion if the name does not end with ".class"
 */
protected ClassFile(IPackageFragment parent, String name) {
	super(CLASS_FILE, parent, name);
	if (!Util.isClassFileName(name)) {
		throw new IllegalArgumentException("class file name must end with .class");
	}
}
/**
 * @see ICodeAssist
 */
public void codeComplete(int offset, ICodeCompletionRequestor requestor) throws JavaModelException {
	String source = getSource();
	if (source != null) {
		BasicCompilationUnit cu = new BasicCompilationUnit(getSource().toCharArray(), getElementName() + ".java");
		codeComplete(cu, cu, offset, requestor);
	}
}
/**
 * @see ICodeResolve
 */
public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer != null) {
		char[] contents = null;
		contents = buffer.getCharacters();
		String name = getElementName();
		name = name.substring(0, name.length() - 6); // remove ".class"
		name = name + ".java";
		BasicCompilationUnit cu = new BasicCompilationUnit(contents, name);
		return super.codeSelect(cu, offset, length);
	} else {
		//has no associated souce
		return new IJavaElement[] {};
	}
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new ClassFileInfo(this);
}
/**
 * Finds the deepest <code>IJavaElement</code> in the hierarchy of
 * <code>elt</elt>'s children (including <code>elt</code> itself)
 * which has a source range that encloses <code>position</code>
 * according to <code>mapper</code>.
 */
protected IJavaElement findElement(IJavaElement elt, int position, SourceMapper mapper) {
	SourceRange range = mapper.getSourceRange(elt);
	if (range == null || position < range.getOffset() || range.getOffset() + range.getLength() - 1 < position) {
		return null;
	}
	if (elt instanceof IParent) {
		try {
			IJavaElement[] children = ((IParent) elt).getChildren();
			for (int i = 0; i < children.length; i++) {
				IJavaElement match = findElement(children[i], position, mapper);
				if (match != null) {
					return match;
				}
			}
		} catch (JavaModelException npe) {
		}
	}
	return elt;
}
/**
 * Creates the children elements for this class file adding the resulting
 * new handles and info objects to the newElements table. Returns true
 * if successful, or false if an error is encountered parsing the class file.
 * 
 * @see Openable
 * @see Signature
 */
protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Hashtable newElements, IResource underlyingResource) throws JavaModelException {
	IBinaryType typeInfo = getBinaryTypeInfo((IFile) underlyingResource);
	if (typeInfo == null) {
		// The structure of a class file is unknown if a class file format errors occurred
		//during the creation of the diet class file representative of this ClassFile.
		info.setChildren(new IJavaElement[] {});
		return false;
	}

	// Make the type
	IType type = new BinaryType(this, new String(simpleName(typeInfo.getName())));
	info.addChild(type);
	newElements.put(type, typeInfo);
	return true;
}
/**
 * Returns the <code>ClassFileReader</code>specific for this IClassFile, based
 * on its underlying resource, or <code>null</code> if unable to create
 * the diet class file.
 * There are two cases to consider:<ul>
 * <li>a class file corresponding to an IFile resource</li>
 * <li>a class file corresponding to a zip entry in a JAR</li>
 * </ul>
 *
 * @exception JavaModelException when the IFile resource or JAR is not available
 * or when this class file is not present in the JAR
 */
private IBinaryType getBinaryTypeInfo(IFile file) throws JavaModelException {
	JavaElement le = (JavaElement) getParent();
	if (le instanceof JarPackageFragment) {
		try {
			JarPackageFragmentRoot root = (JarPackageFragmentRoot) le.getParent();
			IBinaryType info = null;
			ZipFile zip = null;
			try {
				zip = root.getJar();
				String entryName = getParent().getElementName();
				entryName = entryName.replace('.', '/');
				if (entryName.equals("")) {
					entryName += getElementName();
				} else {
					entryName += '/' + getElementName();
				}
				info = ClassFileReader.read(zip, entryName);
			} finally {
				if (zip != null) {
					try {
						zip.close();
					} catch (IOException e) {
						// ignore 
					}
				}
			}
			if (info == null) {
				throw newNotPresentException();
			}
			return info;
		} catch (ClassFormatException cfe) {
			//the structure remains unknown
			return null;
		} catch (IOException ioe) {
			throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	} else {
		byte[] contents = null;
		contents = BufferManager.getResourceContentsAsBytes(file);
		try {
			return new ClassFileReader(contents, getElementName().toCharArray());
		} catch (ClassFormatException cfe) {
			//the structure remains unknown
			return null;
		}
	}
}
/**
 * Note: a buffer with no unsaved changes can be closed by the Java Model
 * since it has a finite number of buffers allowed open at one time. If this
 * is the first time a request is being made for the buffer, an attempt is
 * made to create and fill this element's buffer. If the buffer has been
 * closed since it was first opened, the buffer is re-created.
 * 
 * @see IOpenable
 */
public IBuffer getBuffer() throws JavaModelException {
	IBuffer buffer = getBufferManager().getBuffer(this);
	if (buffer == null) {
		// try to (re)open a buffer
		return openBuffer(null);
	}
	return buffer;
}
/**
 * @see IMember
 */
public IClassFile getClassFile() {
	return this;
}
/**
 * A class file has a corresponding resource unless it is contained
 * in a jar.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root= (IPackageFragmentRoot)getParent().getParent();
	if (root.isArchive()) {
		return null;
	} else {
		return getUnderlyingResource();
	}
}
/**
 * @see IClassFile
 */
public IJavaElement getElementAt(int position) throws JavaModelException {
	IJavaElement parent = getParent();
	while (parent.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
		parent = parent.getParent();
	}
	PackageFragmentRoot root = (PackageFragmentRoot) parent;
	SourceMapper mapper = root.getSourceMapper();
	if (mapper == null) {
		return null;
	} else {
		IType type = getType();
		return findElement(type, position, mapper);
	}
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_CLASSFILE;
}
/**
 * @see ISourceReference
 */
public String getSource() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer == null) {
		return null;
	}
	return buffer.getContents();
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	return new SourceRange(0, getBuffer().getContents().toString().length());
}
/**
 * @see IClassFile
 */
public IType getType() throws JavaModelException {
	if (fBinaryType == null) {
		// Remove the ".class" from the name of the ClassFile - always works
		// since constructor fails if name does not end with ".class"
		String name = fName.substring(0, fName.lastIndexOf('.'));
		name = name.substring(name.lastIndexOf('.') + 1);
		int index = name.lastIndexOf('$');
		if (index > -1) {
			name = name.substring(index + 1);
		}
		fBinaryType = new BinaryType(this, name);
	}
	return fBinaryType;
}
/**
 * 
 */
public WorkingCopy getWorkingCopy() {
	String name = getElementName();
	name = name.substring(0, name.length() - 6); // remove ".class"
	name = name + ".java";
	return new WorkingCopy((IPackageFragment) getParent(), name);
}
/**
 * @see Openable
 */
protected boolean hasBuffer() {
	return true;
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
/**
 * @see IClassFile
 */
public boolean isClass() throws JavaModelException {
	return getType().isClass();
}
/**
 * @see IClassFile
 */
public boolean isInterface() throws JavaModelException {
	return getType().isInterface();
}
/**
 * Returns true - class files are always read only.
 */
public boolean isReadOnly() {
	return true;
}
/**
 * Opens and returns buffer on the source code associated with this class file.
 * Maps the source code to the children elements of this class file.
 * If no source code is associated with this class file, 
 * <code>null</code> is returned.
 * 
 * @see Openable
 */
protected IBuffer openBuffer(IProgressMonitor pm) throws JavaModelException {
	SourceMapper mapper = getSourceMapper();
	if (mapper != null) {
		char[] contents = mapper.findSource(getType());
		if (contents != null) {
			IBufferManager bufManager = getBufferManager();
			IBuffer buf = bufManager.openBuffer(contents, pm, this, isReadOnly());
			// do the source mapping
			mapper.mapSource(getType(), contents);
			return buf;
		}
	} else {
		// Attempts to find the corresponding java file
		String qualifiedName = getType().getFullyQualifiedName();
		INameLookup lookup = ((JavaProject) getJavaProject()).getNameLookup();
		ICompilationUnit cu = lookup.findCompilationUnit(qualifiedName);
		if (cu != null) {
			return cu.getBuffer();
		}
	}
	return null;
}
/**
 * Returns the Java Model format of the simple class name for the
 * given className which is provided in diet class file format,
 * or <code>null</code> if the given className is <code>null</code>.
 * (This removes package name and enclosing type names).
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model simple name format is "Object".
 */

/* package */ static char[] simpleName(char[] className) {
	if (className == null)
		return null;
	className = unqualifiedName(className);
	int count = 0;
	for (int i = className.length - 1; i > -1; i--) {
		if (className[i] == '$') {
			char[] name = new char[count];
			System.arraycopy(className, i + 1, name, 0, count);
			return name;
		}
		count++;
	}
	return className;
}
/**
 * Returns the Java Model representation of the given name
 * which is provided in diet class file format, or <code>null</code>
 * if the given name is <code>null</code>.
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model format is "java.lang.Object".
 */

public static char[] translatedName(char[] name) {
	if (name == null)
		return null;
	int nameLength = name.length;
	char[] newName= new char[nameLength];
	for (int i= 0; i < nameLength; i++) {
		if (name[i] == '/') {
			newName[i]= '.';
		} else {
			newName[i]= name[i];
		}
	}
	return newName;
}
/**
 * Returns the Java Model representation of the given names
 * which are provided in diet class file format, or <code>null</code>
 * if the given names are <code>null</code>.
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model format is "java.lang.Object".
 */

/* package */ static char[][] translatedNames(char[][] names) {
	if (names == null)
		return null;
	int length = names.length;
	char[][] newNames = new char[length][];
	for(int i = 0; i < length; i++) {
		newNames[i] = translatedName(names[i]);
	}
	return newNames;
}
/**
 * Returns the Java Model format of the unqualified class name for the
 * given className which is provided in diet class file format,
 * or <code>null</code> if the given className is <code>null</code>.
 * (This removes the package name, but not enclosing type names).
 *
 * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
 * and corresponding Java Model simple name format is "Object".
 */

/* package */ static char[] unqualifiedName(char[] className) {
	if (className == null)
		return null;
	int count = 0;
	for (int i = className.length - 1; i > -1; i--) {
		if (className[i] == '/') {
			char[] name = new char[count];
			System.arraycopy(className, i + 1, name, 0, count);
			return name;
		}
		count++;
	}
	return className;
}
}
