package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.lookup.ReferenceInfo;

import java.util.*;

/**
 * A requestor for the fuzzy parser, used to compute the children of an ICompilationUnit.
 */
public class CompilationUnitStructureRequestor implements ISourceElementRequestor {

	/**
	 * The handle to the compilation unit being parsed
	 */
	protected ICompilationUnit fUnit;

	/**
	 * The info object for the compilation unit being parsed
	 */
	protected CompilationUnitElementInfo fUnitInfo;

	/**
	 * The import container info - null until created
	 */
	protected JavaElementInfo fImportContainerInfo= null;

	/**
	 * Hashtable of children elements of the compilation unit.
	 * Children are added to the table as they are found by
	 * the parser. Keys are handles, values are corresponding
	 * info objects.
	 */
	protected Hashtable fNewElements;

	/**
	 * Stack of parent scope info objects - i.e. the info on the
	 * top of the stack is the parent of the next element found.
	 * For example, when we locate a method, the parent info object
	 * will be the type the method is contained in.
	 */
	protected Stack fInfoStack;

	/**
	 * Stack of parent handles, corresponding to the info stack. We
	 * keep both, since info objects do not have back pointers to
	 * handles.
	 */
	protected Stack fHandleStack;

	/**
	 * The name of the source file being parsed.
	 */
	protected char[] fSourceFileName= null;

	/**
	 * The dot-separated name of the package the compilation unit
	 * is contained in - based on the package statement in the
	 * compilation unit, and initialized by #acceptPackage.
	 * Initialized to <code>null</code> for the default package.
	 */
	protected char[] fPackageName= null;

	/**
	 * Array of bytes describing reference info found during the
	 * parse. Entires are constants defined by ReferenceInfo,
	 * and the corresponding names are in fReferenceNames.
	 *
	 * @see ReferenceInfo
	 */
	protected byte[] fReferenceKinds= fgEmptyByte;

	/**
	 * Array of referenced names found during the
	 * parse. Entires are char arrays, and the corresponding
	 * reference kinds are in fReferenceKinds.
	 */
	protected char[][] fReferenceNames= fgEmptyCharChar;

	/**
	 * The number of references reported thus far. Used to
	 * expand the arrays of reference kinds and names.
	 */
	protected int fRefCount= 0;

	/**
	 * The initial size of the reference kind and name
	 * arrays. If the arrays fill, they are doubled in
	 * size
	 */
	protected static int fgReferenceAllocation= 50;

	/**
	 * Collection of problems reported during the parse.
	 * If any errors appear (i.e. not warnings), the structure
	 * of the compilation unit is considered unknown.
	 */	
	protected Vector fProblems;

	/**
	 * Empty collections used for efficient initialization
	 */
	protected static String[] fgEmptyStringArray = new String[0];
	protected static byte[] fgEmptyByte= new byte[]{};
	protected static char[][] fgEmptyCharChar= new char[][]{};
	protected static char[] fgEmptyChar= new char[]{};


	protected HashtableOfObject fieldRefCache;
	protected HashtableOfObject messageRefCache;
	protected HashtableOfObject typeRefCache;
	protected HashtableOfObject unknownRefCache;
protected CompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Hashtable newElements) throws JavaModelException {
	fUnit = unit;
	fUnitInfo = unitInfo;
	fNewElements = newElements;
	fSourceFileName= unit.getElementName().toCharArray();
}
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {

	// type name could be qualified
	if (CharOperation.indexOf('.', typeName) < 0){
		acceptTypeReference(typeName, sourcePosition);
	} else {
		acceptTypeReference(CharOperation.splitOn('.', typeName), -1, -1); // source positions are not used
		// use simple name afterwards
		typeName = CharOperation.lastSegment(typeName, '.');
	}
	if (argCount < 10) {
		// common case (i.e. small arg count)
		int len = typeName.length;
		char[] name = new char[len+4];
		name[0] = '<';
		System.arraycopy(typeName, 0, name, 1, len);
		// fix for 1FWAKJJ
		name[len+1] = '>';
		name[len+2] = '/';
		name[len+3] = (char) ('0' + argCount);
		addReference(ReferenceInfo.REFTYPE_call, name);
	}
	else {
		String name = "<"/*nonNLS*/ + new String(typeName) + ">/"/*nonNLS*/ + argCount;  
		addReference(ReferenceInfo.REFTYPE_call, name.toCharArray());
	}
}
public void acceptFieldReference(char[] fieldName, int sourcePosition) {
	addReference(ReferenceInfo.REFTYPE_var, fieldName);
}
/**
 * @see ISourceElementRequestor
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand) {
	JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
	JavaElement parentHandle= (JavaElement)fHandleStack.peek();
	if (!(parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT)) {
		Assert.isTrue(false); // Should not happen
	}

	ICompilationUnit parentCU= (ICompilationUnit)parentHandle;
	//create the import container and its info
	IImportContainer importContainer= parentCU.getImportContainer();
	if (fImportContainerInfo == null) {
		fImportContainerInfo= new JavaElementInfo();
		fImportContainerInfo.setIsStructureKnown(true);
		parentInfo.addChild(importContainer);
		fNewElements.put(importContainer, fImportContainerInfo);
	}
	
	// tack on the '.*' if it is onDemand
	String importName;
	if (onDemand) {
		importName= new String(name) + ".*"/*nonNLS*/;
	} else {
		importName= new String(name);
	}
	
	ImportDeclaration handle = new ImportDeclaration(importContainer, importName);
	resolveDuplicates(handle);
	
	SourceRefElementInfo info = new SourceRefElementInfo();
	info.setSourceRangeStart(declarationStart);
	info.setSourceRangeEnd(declarationEnd);

	fImportContainerInfo.addChild(handle);
	fNewElements.put(handle, info);
}
/**
 * @see ISourceElementRequestor
 */
public void acceptInitializer(
	int modifiers, 
	int declarationSourceStart, 
	int declarationSourceEnd) {
		JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IInitializer handle = null;
		
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = ((IType) parentHandle).getInitializer(1);
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		InitializerElementInfo info = new InitializerElementInfo();
		info.setSourceRangeStart(declarationSourceStart);
		info.setSourceRangeEnd(declarationSourceEnd);
		info.setFlags(modifiers);

		parentInfo.addChild(handle);
		fNewElements.put(handle, info);
}
/*
 * Table of line separator position. This table is passed once at the end
 * of the parse action, so as to allow computation of normalized ranges.
 *
 * A line separator might corresponds to several characters in the source,
 * 
 */
public void acceptLineSeparatorPositions(int[] positions) {}
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {
	if (argCount < 10) {
		// common case (i.e. small arg count)
		int len = methodName.length;
		char[] name = new char[len+2];
		System.arraycopy(methodName, 0, name, 0, len);
		name[len] = '/';
		name[len+1] = (char) ('0' + argCount);
		addReference(ReferenceInfo.REFTYPE_call, name);
	}
	else {
		String name = new String(methodName) + "/"/*nonNLS*/ + argCount;  
		addReference(ReferenceInfo.REFTYPE_call, name.toCharArray());
	}
}
/**
 * @see ISourceElementRequestor
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {

		JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IPackageDeclaration handle = null;
		fPackageName= name;
		
		if (parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT) {
			handle = new PackageDeclaration((ICompilationUnit) parentHandle, new String(name));
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceRefElementInfo info = new SourceRefElementInfo();
		info.setSourceRangeStart(declarationStart);
		info.setSourceRangeEnd(declarationEnd);

		parentInfo.addChild(handle);
		fNewElements.put(handle, info);

}
public void acceptProblem(IProblem problem) {
	if (fProblems == null) {
		fProblems= new Vector();
	}
	fProblems.addElement(problem);
}
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {
	int last = typeName.length - 1;
	for (int i = 0; i < last; ++i) {
		addReference(ReferenceInfo.REFTYPE_unknown, typeName[i]);
	}
	addReference(ReferenceInfo.REFTYPE_type, typeName[last]);
}
public void acceptTypeReference(char[] typeName, int sourcePosition) {
	addReference(ReferenceInfo.REFTYPE_type, typeName);
}
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {
	for (int i = 0; i < name.length; ++i) {
		addReference(ReferenceInfo.REFTYPE_unknown, name[i]);
	}
}
public void acceptUnknownReference(char[] name, int sourcePosition) {
	addReference(ReferenceInfo.REFTYPE_unknown, name);
}
/**
 * Adds the given reference to the reference info for this CU.
 */
protected void addReference(byte kind, char[] name) {

	HashtableOfObject refCache = null;
	switch(kind){
		case ReferenceInfo.REFTYPE_unknown:
			if ((refCache = this.unknownRefCache) == null){
				refCache = this.unknownRefCache = new HashtableOfObject(5);
			}
			break;		
		case ReferenceInfo.REFTYPE_call: 
			if ((refCache = this.messageRefCache) == null){
				refCache = this.messageRefCache = new HashtableOfObject(5);
			}
			break;
		case ReferenceInfo.REFTYPE_type:
			if ((refCache = this.typeRefCache) == null){
				refCache = this.typeRefCache = new HashtableOfObject(5);
			}
			break;		
		case ReferenceInfo.REFTYPE_var:
			if ((refCache = this.fieldRefCache) == null){
				refCache = this.fieldRefCache = new HashtableOfObject(5);
			}
			break;		
		case ReferenceInfo.REFTYPE_import:
		case ReferenceInfo.REFTYPE_derive:
		case ReferenceInfo.REFTYPE_class:
		case ReferenceInfo.REFTYPE_constant:	
	}
	if (refCache == null) addReference0(kind, name); // backward compatible
	if (refCache.containsKey(name)) return;
	refCache.put(name, name);
	addReference0(kind, name);
}
/**
 * Adds the given reference to the reference info for this CU.
 */
private void addReference0(byte kind, char[] name) {

	int count = fRefCount++;
	if (count >= fReferenceKinds.length) {
		int size = fgReferenceAllocation;
		if (fReferenceKinds.length > 0) {
			size = fReferenceKinds.length * 2;
		}
		byte[] kinds = new byte[size];
		System.arraycopy(fReferenceKinds, 0, kinds, 0, fReferenceKinds.length);
		fReferenceKinds = kinds;
		char[][] names = new char[size][];
		System.arraycopy(fReferenceNames, 0, names, 0, fReferenceNames.length);
		fReferenceNames = names;
	}
	fReferenceKinds[count] = kind;
	fReferenceNames[count] = name;
}
/**
 * Convert these type names to signatures.
 * @see Signature.
 */
/* default */ static String[] convertTypeNamesToSigs(char[][] typeNames) {
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
public void enterClass(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces) {

	enterType(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, superclass, superinterfaces);

}
/**
 * @see ISourceElementRequestor
 */
public void enterCompilationUnit() {
	fInfoStack = new Stack();
	fHandleStack= new Stack();
	fInfoStack.push(fUnitInfo);
	fHandleStack.push(fUnit);
}
/**
 * @see ISourceElementRequestor
 */
public void enterConstructor(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes) {

		enterMethod(declarationStart, modifiers, null, name, nameSourceStart,
			nameSourceEnd,	parameterTypes, parameterNames, exceptionTypes, true);
}
/**
 * @see ISourceElementRequestor
 */
public void enterField(
	int declarationStart,
	int modifiers,
	char[] type,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd) {

		SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IField handle = null;
		
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new SourceField((IType) parentHandle, new String(name));
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceFieldElementInfo info = new SourceFieldElementInfo();
		info.setName(name);
		info.setNameSourceStart(nameSourceStart);
		info.setNameSourceEnd(nameSourceEnd);
		info.setSourceRangeStart(declarationStart);
		info.setFlags(modifiers);
		info.setTypeName(type);

		parentInfo.addChild(handle);
		parentInfo.addField(info);
		fNewElements.put(handle, info);

		fInfoStack.push(info);
		fHandleStack.push(handle);
}
/**
 * @see ISourceElementRequestor
 */
public void enterInterface(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] superinterfaces) {

	enterType(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, null, superinterfaces);

}
/**
 * @see ISourceElementRequestor
 */
public void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes) {

		enterMethod(declarationStart, modifiers, returnType, name, nameSourceStart,
			nameSourceEnd, parameterTypes, parameterNames, exceptionTypes, false);
}
/**
 * @see ISourceElementRequestor
 */
protected void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes,
	boolean isConstructor) {

		SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) fInfoStack.peek();
		JavaElement parentHandle= (JavaElement)fHandleStack.peek();
		IMethod handle = null;

		// translate nulls to empty arrays
		if (parameterTypes == null) {
			parameterTypes= fgEmptyCharChar;
		}
		if (parameterNames == null) {
			parameterNames= fgEmptyCharChar;
		}
		if (exceptionTypes == null) {
			exceptionTypes= fgEmptyCharChar;
		}
		
		String[] parameterTypeSigs = convertTypeNamesToSigs(parameterTypes);
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new SourceMethod((IType) parentHandle, new String(name), parameterTypeSigs);
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceMethodElementInfo info = new SourceMethodElementInfo();
		info.setSourceRangeStart(declarationStart);
		int flags = modifiers;
		info.setName(name);
		info.setNameSourceStart(nameSourceStart);
		info.setNameSourceEnd(nameSourceEnd);
		info.setConstructor(isConstructor);
		info.setFlags(flags);
		info.setArgumentNames(parameterNames);
		info.setArgumentTypeNames(parameterTypes);
		info.setReturnType(returnType == null ? new char[]{'v', 'o','i', 'd'} : returnType);
		info.setExceptionTypeNames(exceptionTypes);

		parentInfo.addChild(handle);
		parentInfo.addMethod(info);
		fNewElements.put(handle, info);
		fInfoStack.push(info);
		fHandleStack.push(handle);
}
/**
 * Common processing for classes and interfaces.
 */
protected void enterType(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces) {

	char[] enclosingTypeName= null;
	char[] qualifiedName= null;
	
	JavaElementInfo parentInfo = (JavaElementInfo) fInfoStack.peek();
	JavaElement parentHandle= (JavaElement)fHandleStack.peek();
	IType handle = null;
	String nameString= new String(name);
	
	if (parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT) {
		handle = ((ICompilationUnit) parentHandle).getType(nameString);
		if (fPackageName == null) {
			qualifiedName= nameString.toCharArray();
		} else {
			qualifiedName= (new String(fPackageName) + "."/*nonNLS*/ + nameString).toCharArray();
		}
	}
	else if (parentHandle.getElementType() == IJavaElement.TYPE) {
		handle = ((IType) parentHandle).getType(nameString);
		enclosingTypeName= ((SourceTypeElementInfo)parentInfo).getName();
		qualifiedName= (new String(((SourceTypeElementInfo)parentInfo).getQualifiedName()) + "."/*nonNLS*/ + nameString).toCharArray();
	}
	else {
		Assert.isTrue(false); // Should not happen
	}
	resolveDuplicates(handle);
	
	SourceTypeElementInfo info = new SourceTypeElementInfo();
	info.setHandle(handle);
	info.setSourceRangeStart(declarationStart);
	info.setFlags(modifiers);
	info.setName(name);
	info.setNameSourceStart(nameSourceStart);
	info.setNameSourceEnd(nameSourceEnd);
	info.setSuperclassName(superclass);
	info.setSuperInterfaceNames(superinterfaces);
	info.setEnclosingTypeName(enclosingTypeName);
	info.setSourceFileName(fSourceFileName);
	info.setPackageName(fPackageName);
	info.setQualifiedName(qualifiedName);
	Enumeration e = fNewElements.keys();
	while(e.hasMoreElements()) {
		Object object = e.nextElement();
		if (object instanceof IImportDeclaration)
			info.addImport(((IImportDeclaration)object).getElementName().toCharArray());
	}
	

	parentInfo.addChild(handle);
	if (parentInfo instanceof SourceTypeElementInfo) {
		((SourceTypeElementInfo)parentInfo).addMemberType(info);
	}
	fNewElements.put(handle, info);

	fInfoStack.push(info);
	fHandleStack.push(handle);

}
/**
 * @see ISourceElementRequestor
 */
public void exitClass(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitCompilationUnit(int declarationEnd) {
	fUnitInfo.setSourceLength(declarationEnd + 1);

	// make the reference arrays the correct size
	if (fRefCount != fReferenceKinds.length) {
		byte[] kinds= new byte[fRefCount];
		System.arraycopy(fReferenceKinds, 0, kinds, 0, fRefCount);
		fReferenceKinds= kinds;
		char[][] names= new char[fRefCount][];
		System.arraycopy(fReferenceNames, 0, names, 0, fRefCount);
		fReferenceNames= names;
	}
	fUnitInfo.setReferenceInfo(new ReferenceInfo(fReferenceNames, fReferenceKinds));

	// determine if there were any parsing errors
	fUnitInfo.setIsStructureKnown(true);
	if (fProblems != null) {
		for (int i= 0; i < fProblems.size(); i++) {
			IProblem problem= (IProblem)fProblems.elementAt(i);
			if (!problem.isWarning()) {
				fUnitInfo.setIsStructureKnown(false);
				break;
			}
		}
	}
}
/**
 * @see ISourceElementRequestor
 */
public void exitConstructor(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitField(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitInterface(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * common processing for classes and interfaces
 */
protected void exitMember(int declarationEnd) {
	SourceRefElementInfo info = (SourceRefElementInfo) fInfoStack.pop();
	info.setSourceRangeEnd(declarationEnd);
	fHandleStack.pop();
}
/**
 * @see ISourceElementRequestor
 */
public void exitMethod(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * Resolves duplicate handles by incrementing the occurrence count
 * of the handle being created until there is no conflict.
 */
protected void resolveDuplicates(IJavaElement handle) {
	while (fNewElements.containsKey(handle)) {
		JavaElement h = (JavaElement) handle;
		h.setOccurrenceCount(h.getOccurrenceCount() + 1);
	}
}
}
