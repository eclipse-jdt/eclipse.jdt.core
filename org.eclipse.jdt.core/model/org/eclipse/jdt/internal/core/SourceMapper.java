package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.core.util.ReferenceInfoAdapter;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * A SourceMapper maps source code in a ZIP file to binary types in
 * a JAR. The SourceMapper uses the fuzzy parser to identify source
 * fragments in a .java file, and attempts to match the source code
 * with children in a binary type. A SourceMapper is associated
 * with a JarPackageFragment by an AttachSourceOperation.
 *
 * @see AttachSourceOperation
 * @see JarPackageFragment
 */
public class SourceMapper extends ReferenceInfoAdapter implements ISourceElementRequestor {

	/**
	 * The binary type source is being mapped for
	 */
	protected BinaryType fType;

	/**
	 * The location of the zip file containing source.
	 */
	protected IPath fZipPath;
	/**
	 * Specifies the location of the package fragment root within
	 * the zip (empty specifies the default root). <code>null</code> is
	 * not a valid root path.
	 */
	protected String fRootPath;

	/**
	 * The Java Model this source mapper is working for.
	 */
	protected JavaModel fJavaModel;

	/**
	 * Used for efficiency
	 */
	protected static String[] fgEmptyStringArray = new String[0];

	/**
	 * Table that maps a binary element to its <code>SourceRange</code>s.
	 * Keys are the element handles, entries are <code>SourceRange[]</code> which
	 * is a two element array; the first being source range, the second
	 * being name range.
	 */
	protected Hashtable fSourceRanges;

	/**
	 * The unknown source range {-1, 0}
	 */
	protected static SourceRange fgUnknownRange= new SourceRange(-1, 0);

	/**
	 * The position within the source of the start of the
	 * current member element, or -1 if we are outside a member.
	 */
	protected int fMemberDeclarationStart = -1;
	/**
	 * The <code>SourceRange</code> of the name of the current member element.
	 */
	protected SourceRange fMemberNameRange;
	/**
	 * The name of the current member element.
	 */
	protected String fMemberName;
	/**
	 * The parameter types for the current member method element.
	 */
	protected char[][] fMethodParameterTypes;

	/**
	 * The element searched for
	 */
	protected IJavaElement searchedElement;

	/**
	 * imports references
	 */
	private char[][] imports;
	private int importsCounter;
	
	/**
	 * Enclosing type information
	 */
	 IType[] types;
	 int[] typeDeclarationStarts;
	 SourceRange[] typeNameRanges;
	 int typeDepth;
/**
 * Creates a <code>SourceMapper</code> that locates source in the zip file
 * at the given location in the specified package fragment root.
 */
public SourceMapper(IPath zipPath, String rootPath, JavaModel model) {
	fZipPath= zipPath;
	fRootPath= rootPath.replace('\\', '/');
	if (fRootPath.endsWith("/"/*nonNLS*/)) {
		fRootPath = fRootPath.substring(0, fRootPath.lastIndexOf('/'));
	}
	fJavaModel= model;
	fSourceRanges= new Hashtable();
}
/**
 * @see ISourceElementRequestor
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand) {
	if (this.imports == null) {
		this.imports = new char[5][];
		this.importsCounter = 0;
	}
	if (this.imports.length == this.importsCounter) {
		System.arraycopy(this.imports, 0, (this.imports = new char[this.importsCounter * 2][]), 0, this.importsCounter);
	}
	this.imports[this.importsCounter++] = name;
}
/**
 * @see ISourceElementRequestor
 */
public void acceptInitializer(int modifiers, int declarationSourceStart, int declarationSourceEnd) {
	//do nothing
}
/**
 * @see ISourceElementRequestor
 */
public void acceptLineSeparatorPositions(int[] positions) {
	//do nothing
}
/**
 * @see ISourceElementRequestor
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {
	//do nothing
}
/**
 * @see ISourceElementRequestor
 */
public void acceptProblem(IProblem problem) {
	//do nothing
}
/**
 * Closes this <code>SourceMapper</code>'s zip file. Once this is done, this
 * <code>SourceMapper</code> cannot be used again.
 */
public void close() throws JavaModelException {
	fSourceRanges= null;
}
/**
 * Converts these type names to signatures.
 * @see Signature.
 */
public String[] convertTypeNamesToSigs(char[][] typeNames) {
	if (typeNames == null)
		return fgEmptyStringArray;
	int n = typeNames.length;
	if (n == 0)
		return fgEmptyStringArray;
	String[] typeSigs = new String[n];
	for (int i = 0; i < n; ++i) {
		typeSigs[i] = Signature.createTypeSignature(typeNames[i], false);
	}
	return typeSigs;
}
/**
 * @see ISourceElementRequestor
 */
public void enterClass(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[] superclass, char[][] superinterfaces) {

	this.typeDepth++;
	if (this.typeDepth == this.types.length){ // need to grow
		System.arraycopy(this.types, 0, this.types = new IType[this.typeDepth*2], 0, this.typeDepth);
		System.arraycopy(this.typeNameRanges, 0, this.typeNameRanges = new SourceRange[this.typeDepth*2], 0, this.typeDepth);
		System.arraycopy(this.typeDeclarationStarts, 0, this.typeDeclarationStarts = new int[this.typeDepth*2], 0, this.typeDepth);
	}
	this.types[typeDepth] = this.getType(new String(name));
	this.typeNameRanges[typeDepth] = new SourceRange(nameSourceStart, nameSourceEnd - nameSourceStart + 1);
	this.typeDeclarationStarts[typeDepth] = declarationStart;
}
/**
 * @see ISourceElementRequestor
 */
public void enterCompilationUnit() {
	// do nothing
}
/**
 * @see ISourceElementRequestor
 */
public void enterConstructor(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	enterMethod(declarationStart, modifiers, null, name, nameSourceStart, nameSourceEnd, parameterTypes, parameterNames, exceptionTypes);
}
/**
 * @see ISourceElementRequestor
 */
public void enterField(int declarationStart, int modifiers, char[] type, char[] name, int nameSourceStart, int nameSourceEnd) {
	if (typeDepth >= 0 && fMemberDeclarationStart == -1) { // don't allow nested member (can only happen with anonymous inner classes)
		fMemberDeclarationStart= declarationStart;
		fMemberNameRange= new SourceRange(nameSourceStart, nameSourceEnd - nameSourceStart + 1);
		fMemberName= new String(name);
	} 
}
/**
 * @see ISourceElementRequestor
 */
public void enterInterface(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] superinterfaces) {
	enterClass(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, null, superinterfaces);
}
/**
 * @see ISourceElementRequestor
 */
public void enterMethod(int declarationStart, int modifiers, char[] returnType, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	if (typeDepth >= 0 && fMemberDeclarationStart == -1) { // don't allow nested member (can only happen with anonymous inner classes)
		fMemberName= new String(name);
		fMemberNameRange= new SourceRange(nameSourceStart, nameSourceEnd - nameSourceStart + 1);
		fMemberDeclarationStart= declarationStart;
		fMethodParameterTypes= parameterTypes;
	} 
}
/**
 * @see ISourceElementRequestor
 */
public void exitClass(int declarationEnd) {
	if (typeDepth >= 0) {
		IType currentType = this.types[typeDepth];
		setSourceRange(
			currentType, 
			new SourceRange(
				this.typeDeclarationStarts[typeDepth] , 
				declarationEnd - this.typeDeclarationStarts[typeDepth]  + 1), 
			this.typeNameRanges[typeDepth]);
		this.typeDepth--;
	}
}
/**
 * @see ISourceElementRequestor
 */
public void exitCompilationUnit(int declarationEnd) {
	//do nothing
}
/**
 * @see ISourceElementRequestor
 */
public void exitConstructor(int declarationEnd) {
	exitMethod(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitField(int declarationEnd) {
	if (typeDepth >= 0 && fMemberDeclarationStart != -1) {
		IType currentType = this.types[typeDepth];
		setSourceRange(currentType.getField(fMemberName), new SourceRange(fMemberDeclarationStart, declarationEnd - fMemberDeclarationStart + 1), fMemberNameRange);
		fMemberDeclarationStart = -1;
	}
}
/**
 * @see ISourceElementRequestor
 */
public void exitInterface(int declarationEnd) {
	exitClass(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitMethod(int declarationEnd) {
	if (typeDepth >= 0 && fMemberDeclarationStart != -1) {
		IType currentType = this.types[typeDepth];
		SourceRange sourceRange= new SourceRange(fMemberDeclarationStart, declarationEnd - fMemberDeclarationStart + 1);
		setSourceRange(currentType.getMethod(fMemberName, convertTypeNamesToSigs(fMethodParameterTypes)), sourceRange, fMemberNameRange);
		fMemberDeclarationStart = -1;
	}
}
/**
 * Locates and returns source code for the given (binary) type, in this
 * SourceMapper's ZIP file, or returns <code>null</code> if source
 * code cannot be found.
 */
public char[] findSource(IType type) {
	if (!type.isBinary()) {
		return null;
	}
	BinaryType parent= (BinaryType)type.getDeclaringType();
	BinaryType declType= (BinaryType)type;
	while (parent != null) {
		declType= parent;
		parent= (BinaryType)declType.getDeclaringType();
	}
	IBinaryType info= null;
	try {
	 info= (IBinaryType)declType.getRawInfo();
	} catch (JavaModelException e) {
		return null;
	}
	return this.findSource(type, info);
}
/**
 * Locates and returns source code for the given (binary) type, in this
 * SourceMapper's ZIP file, or returns <code>null</code> if source
 * code cannot be found.
 */
public char[] findSource(IType type, IBinaryType info) {
	char[] sourceFileName = info.sourceFileName();
	if (sourceFileName == null) return null; // no source file attribute
	String name = new String(sourceFileName);

	IPackageFragment pkgFrag = type.getPackageFragment();
	if (!pkgFrag.isDefaultPackage()) {
		String pkg= type.getPackageFragment().getElementName().replace('.', '/');
		name= pkg + '/' + name;
	}
	// try to get the entry
	ZipEntry entry= null;
	ZipFile zip = null;
	char[] source= null;
	try {
		String fullName;
		//add the root path if specified
		if (!fRootPath.equals(IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH)) {
			fullName= fRootPath + '/' + name;
		} else {
			fullName= name;
		}
		zip = getZip();
		entry= zip.getEntry(fullName);
		if (entry != null) {
			// now read the source code
			byte[] bytes= readEntry(zip, entry);
			if (bytes != null) {
				try {
					source= BufferManager.bytesToChar(bytes);
				} catch (JavaModelException e) {
					source= null;
				}
			}
		}
	} catch (CoreException e) {
		return null;
	} finally {
		if (zip != null) {
			try {
				zip.close();
			} catch(IOException e) {}
		}
	}
	return source;
}
/**
 * Returns the SourceRange for the name of the given element, or
 * {-1, -1} if no source range is known for the name of the element.
 */
public SourceRange getNameRange(IJavaElement element) {
	if (element.getElementType() == IJavaElement.METHOD && ((IMember)element).isBinary()) {
		element= getUnqualifiedMethodHandle((IMethod)element);
	}
	SourceRange[] ranges= (SourceRange[])fSourceRanges.get(element);
	if (ranges == null) {
		return fgUnknownRange;
	} else {
		return ranges[1];
	}
}
/**
 * Returns the <code>SourceRange</code> for the given element, or
 * {-1, -1} if no source range is known for the element.
 */
public SourceRange getSourceRange(IJavaElement element) {
	if (element.getElementType() == IJavaElement.METHOD && ((IMember)element).isBinary()) {
		element= getUnqualifiedMethodHandle((IMethod)element);
	}
	SourceRange[] ranges= (SourceRange[])fSourceRanges.get(element);
	if (ranges == null) {
		return fgUnknownRange;
	} else {
		return ranges[0];
	}
}
/**
 * Returns the type with the given <code>typeName</code>.  Returns inner classes
 * as well.
 */
protected IType getType(String typeName) {
	if (fType.getElementName().equals(typeName))
		return fType;
	else
		return fType.getType(typeName);
}
/**
 * Creates a handle that has parameter types that are not
 * fully qualified so that the correct source is found.
 */
protected IJavaElement getUnqualifiedMethodHandle(IMethod method) {

	String[] qualifiedParameterTypes = method.getParameterTypes();
	String[] unqualifiedParameterTypes = new String[qualifiedParameterTypes.length];
	for (int i = 0; i < qualifiedParameterTypes.length; i++) {
		StringBuffer unqualifiedName= new StringBuffer();
		String qualifiedName= qualifiedParameterTypes[i];
		int count = 0;
		while (qualifiedName.charAt(count) == Signature.C_ARRAY) {
			unqualifiedName.append(Signature.C_ARRAY);
			++count;
		}
		if (qualifiedName.charAt(count) == Signature.C_RESOLVED) {
			unqualifiedName.append(Signature.C_UNRESOLVED);
			unqualifiedName.append(Signature.getSimpleName(qualifiedName));
		} else {
			unqualifiedName.append(qualifiedName.substring(count, qualifiedName.length()));
		}
		unqualifiedParameterTypes[i]= unqualifiedName.toString();
	}
	return ((IType) method.getParent()).getMethod(method.getElementName(), unqualifiedParameterTypes);
}
/**
 * Returns the <code>ZipFile</code> that source is located in.
 */
public ZipFile getZip() throws CoreException {
	return fJavaModel.fgJavaModelManager.getZipFile(fZipPath);
}
/**
 * Maps the given source code to the given binary type and its children.
 */
public void mapSource(IType type, char[] contents) {
	this.mapSource(type, contents, null);
}
/**
 * Maps the given source code to the given binary type and its children.
 * If a non-null java element is passed, finds the name range for the 
 * given java element without storing it.
 */
public ISourceRange mapSource(IType type, char[] contents, IJavaElement searchedElement) {
	fType= (BinaryType)type;

	this.imports = null;
	this.searchedElement = searchedElement;
	this.types = new IType[1];
	this.typeDeclarationStarts = new int[1];
	this.typeNameRanges = new SourceRange[1];
	this.typeDepth = -1;

	Hashtable oldSourceRanges = (Hashtable)fSourceRanges.clone();
	try {
		IProblemFactory factory= new DefaultProblemFactory();
		SourceElementParser parser = new SourceElementParser(this, factory);
		parser.parseCompilationUnit(new BasicCompilationUnit(contents, type.getElementName() + ".java"/*nonNLS*/), false);
		if (searchedElement != null) {
			ISourceRange range = this.getNameRange(searchedElement);
			return range;
		} else {
			return null;
		}
	} finally {
		if (searchedElement != null) {
			fSourceRanges = oldSourceRanges;
		}
		fType= null;
		this.searchedElement = null;
		this.types = null;
		this.typeDeclarationStarts = null;
		this.typeNameRanges = null;
		this.typeDepth = -1;
	}
}
/**
 * Returns the contents of the specified zip entry
 */
protected byte[] readEntry(ZipFile zip, ZipEntry entry) {
	InputStream stream = null;
	try {
		stream = zip.getInputStream(entry);
		int remaining = (int) entry.getSize();
		byte[] bytes = new byte[remaining];
		int offset = 0;
		while (remaining > 0) {
			int read = stream.read(bytes, offset, remaining);
			if (read == -1)
				break;
			remaining -= read;
			offset += read;
		}
		return bytes;
	} catch (IOException e) {
		return null;
	} catch (ArrayIndexOutOfBoundsException e) {
		return null;
	} finally {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException ioe) {
			}
		}
	}
}
/** 
 * Sets the mapping for this element to its source ranges for its source range
 * and name range.
 *
 * @see fSourceRanges
 */
protected void setSourceRange(IJavaElement element, SourceRange sourceRange, SourceRange nameRange) {
	fSourceRanges.put(element, new SourceRange[] {sourceRange, nameRange});
}

/**
 * Return a char[][] array containing the imports of the attached source for an IType
 */
public char[][] getImports() {
	if (this.imports != null && this.imports.length != this.importsCounter) {
		System.arraycopy(this.imports, 0, (this.imports = new char[this.importsCounter][]), 0, this.importsCounter);
	}
	return this.imports;
}
}
