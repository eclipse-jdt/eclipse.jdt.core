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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;

/**
 * Parent is an IClassFile.
 *
 * @see IType
 */

public class BinaryType extends BinaryMember implements IType, SuffixConstants {
	
	private static final IField[] NO_FIELDS = new IField[0];
	private static final IMethod[] NO_METHODS = new IMethod[0];
	private static final IType[] NO_TYPES = new IType[0];
	private static final IInitializer[] NO_INITIALIZERS = new IInitializer[0];
	private static final String[] NO_STRINGS = new String[0];
	
protected BinaryType(JavaElement parent, String name) {
	super(parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
}
/**
 * Remove my cached children from the Java Model
 */
protected void closing(Object info) throws JavaModelException {
	ClassFileInfo cfi = getClassFileInfo();
	cfi.removeBinaryChildren();
}
/**
 * @see IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, ICompletionRequestor)
 */
public void codeComplete(char[] snippet,int insertion,int position,char[][] localVariableTypeNames,char[][] localVariableNames,int[] localVariableModifiers,boolean isStatic,ICompletionRequestor requestor) throws JavaModelException {
	codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, ICompletionRequestor, WorkingCopyOwner)
 */
public void codeComplete(char[] snippet,int insertion,int position,char[][] localVariableTypeNames,char[][] localVariableNames,int[] localVariableModifiers,boolean isStatic,ICompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
	if (requestor == null) {
		throw new IllegalArgumentException(Util.bind("codeAssist.nullRequestor")); //$NON-NLS-1$
	}
	JavaProject project = (JavaProject) getJavaProject();
	SearchableEnvironment environment = (SearchableEnvironment) project.getSearchableNameEnvironment();
	NameLookup nameLookup = project.getNameLookup();
	CompletionRequestorWrapper requestorWrapper = new CompletionRequestorWrapper(requestor,nameLookup);
	CompletionEngine engine = new CompletionEngine(environment, requestorWrapper, project.getOptions(true), project);
	requestorWrapper.completionEngine = engine;

	String source = getClassFile().getSource();
	if (source != null && insertion > -1 && insertion < source.length()) {
		try {
			// set the units to look inside
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			ICompilationUnit[] workingCopies = manager.getWorkingCopies(owner, true/*add primary WCs*/);
			nameLookup.setUnitsToLookInside(workingCopies);
	
			// code complete
			String encoding = project.getOption(JavaCore.CORE_ENCODING, true); 
			
			char[] prefix = CharOperation.concat(source.substring(0, insertion).toCharArray(), new char[]{'{'});
			char[] suffix =  CharOperation.concat(new char[]{'}'}, source.substring(insertion).toCharArray());
			char[] fakeSource = CharOperation.concat(prefix, snippet, suffix);
			
			BasicCompilationUnit cu = 
				new BasicCompilationUnit(
					fakeSource, 
					null,
					getElementName(),
					encoding); 
	
			engine.complete(cu, prefix.length + position, prefix.length);
		} finally {
			if (nameLookup != null) {
				nameLookup.setUnitsToLookInside(null);
			}
		}
	} else {
		engine.complete(this, snippet, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic);
	}
}
/**
 * @see IType#createField(String, IJavaElement, boolean, IProgressMonitor)
 */
public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#createInitializer(String, IJavaElement, IProgressMonitor)
 */
public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#createMethod(String, IJavaElement, boolean, IProgressMonitor)
 */
public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see IType#createType(String, IJavaElement, boolean, IProgressMonitor)
 */
public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
public boolean equals(Object o) {
	if (!(o instanceof BinaryType)) return false;
	return super.equals(o);
}
/**
 * @see IType#findMethods(IMethod)
 */
public IMethod[] findMethods(IMethod method) {
	try {
		return this.findMethods(method, this.getMethods());
	} catch (JavaModelException e) {
		// if type doesn't exist, no matching method can exist
		return null;
	}
}
/**
 * @see IParent#getChildren()
 */
public IJavaElement[] getChildren() throws JavaModelException {
	// ensure present
	// fix for 1FWWVYT
	if (!exists()) {
		throw newNotPresentException();
	}
	// get children
	ClassFileInfo cfi = getClassFileInfo();
	if (cfi.binaryChildren == null) {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		boolean hadTemporaryCache = manager.hasTemporaryCache();
		try {
			Object info = manager.getInfo(this);
			HashMap newElements = manager.getTemporaryCache();
			cfi.readBinaryChildren(newElements, (IBinaryType)info);
			if (!hadTemporaryCache) {
				manager.putInfos(this, newElements);
			}
		} finally {
			if (!hadTemporaryCache) {
				manager.resetTemporaryCache();
			}
		}
	}
	return cfi.binaryChildren;
}
protected ClassFileInfo getClassFileInfo() throws JavaModelException {
	ClassFile cf = (ClassFile) fParent;
	return (ClassFileInfo) cf.getElementInfo();
}
/**
 * @see IMember#getDeclaringType()
 */
public IType getDeclaringType() {
	IClassFile classFile = this.getClassFile();
	if (classFile.isOpen()) {
		try {
			char[] enclosingTypeName = ((IBinaryType) getElementInfo()).getEnclosingTypeName();
			if (enclosingTypeName == null) {
				return null;
			}
		 	enclosingTypeName = ClassFile.unqualifiedName(enclosingTypeName);
		 	
			// workaround problem with class files compiled with javac 1.1.* 
			// that return a non-null enclosing type name for local types defined in anonymous (e.g. A$1$B)
			if (classFile.getElementName().length() > enclosingTypeName.length+1 
					&& Character.isDigit(classFile.getElementName().charAt(enclosingTypeName.length+1))) {
				return null;
			} 
			
			return getPackageFragment().getClassFile(new String(enclosingTypeName) + SUFFIX_STRING_class).getType();
		} catch (JavaModelException npe) {
			return null;
		}
	} else {
		// cannot access .class file without opening it 
		// and getDeclaringType() is supposed to be a handle-only method,
		// so default to assuming $ is an enclosing type separator
		String classFileName = classFile.getElementName();
		int lastDollar = -1;
		for (int i = 0, length = classFileName.length(); i < length; i++) {
			char c = classFileName.charAt(i);
			if (Character.isDigit(c) && lastDollar == i-1) {
				// anonymous or local type
				return null;
			} else if (c == '$') {
				lastDollar = i;
			}
		}
		if (lastDollar == -1) {
			return null;
		} else {
			String enclosingName = classFileName.substring(0, lastDollar);
			String enclosingClassFileName = enclosingName + SUFFIX_STRING_class;
			return 
				new BinaryType(
					(JavaElement)this.getPackageFragment().getClassFile(enclosingClassFileName),
					enclosingName.substring(enclosingName.lastIndexOf('$')+1));
		}
	}
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return TYPE;
}
/**
 * @see IType#getField(String name)
 */
public IField getField(String name) {
	return new BinaryField(this, name);
}
/**
 * @see IType#getFields()
 */
public IField[] getFields() throws JavaModelException {
	ArrayList list = getChildrenOfType(FIELD);
	int size;
	if ((size = list.size()) == 0) {
		return NO_FIELDS;
	} else {
		IField[] array= new IField[size];
		list.toArray(array);
		return array;
	}
}
/**
 * @see IMember#getFlags()
 */
public int getFlags() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	return info.getModifiers();
}
/**
 * @see IType#getFullyQualifiedName()
 */
public String getFullyQualifiedName() {
	return this.getFullyQualifiedName('$');
}
/**
 * @see IType#getFullyQualifiedName(char enclosingTypeSeparator)
 */
public String getFullyQualifiedName(char enclosingTypeSeparator) {
	String packageName = getPackageFragment().getElementName();
	if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
		return getTypeQualifiedName(enclosingTypeSeparator);
	}
	return packageName + '.' + getTypeQualifiedName(enclosingTypeSeparator);
}
/**
 * @see IType#getInitializer(int occurrenceCount)
 */
public IInitializer getInitializer(int count) {
	return new Initializer(this, count);
}
/**
 * @see IType#getInitializers()
 */
public IInitializer[] getInitializers() {
	return NO_INITIALIZERS;
}
/**
 * @see IType#getMethod(String name, String[] parameterTypeSignatures)
 */
public IMethod getMethod(String name, String[] parameterTypeSignatures) {
	return new BinaryMethod(this, name, parameterTypeSignatures);
}
/**
 * @see IType#getMethods()
 */
public IMethod[] getMethods() throws JavaModelException {
	ArrayList list = getChildrenOfType(METHOD);
	int size;
	if ((size = list.size()) == 0) {
		return NO_METHODS;
	} else {
		IMethod[] array= new IMethod[size];
		list.toArray(array);
		return array;
	}
}
/**
 * @see IType#getPackageFragment()
 */
public IPackageFragment getPackageFragment() {
	IJavaElement parent = fParent;
	while (parent != null) {
		if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			return (IPackageFragment) parent;
		}
		else {
			parent = parent.getParent();
		}
	}
	Assert.isTrue(false);  // should not happen
	return null;
}
/**
 * @see IType#getSuperclassName()
 */
public String getSuperclassName() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	char[] superclassName = info.getSuperclassName();
	if (superclassName == null) {
		return null;
	}
	return new String(ClassFile.translatedName(superclassName));
}
/**
 * @see IType#getSuperInterfaceNames()
 */
public String[] getSuperInterfaceNames() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	char[][] names= info.getInterfaceNames();
	int length;
	if (names == null || (length = names.length) == 0) {
		return NO_STRINGS;
	}
	names= ClassFile.translatedNames(names);
	String[] strings= new String[length];
	for (int i= 0; i < length; i++) {
		strings[i]= new String(names[i]);
	}
	return strings;
}
/**
 * @see IType#getType(String)
 */
public IType getType(String name) {
	IClassFile classFile= getPackageFragment().getClassFile(getTypeQualifiedName() + "$" + name + SUFFIX_STRING_class); //$NON-NLS-1$
	return new BinaryType((JavaElement)classFile, name);
}
/**
 * @see IType#getTypeQualifiedName()
 */
public String getTypeQualifiedName() {
	return this.getTypeQualifiedName('$');
}
/**
 * @see IType#getTypeQualifiedName(char)
 */
public String getTypeQualifiedName(char enclosingTypeSeparator) {
	IType declaringType = this.getDeclaringType();
	if (declaringType == null) {
		String classFileName = this.getClassFile().getElementName();
		if (classFileName.indexOf('$') == -1) {
			// top level class file: name of type is same as name of class file
			return fName;
		} else {
			// anonymous or local class file
			return classFileName.substring(0, classFileName.lastIndexOf('.')); // remove .class
		}
	} else {
		return 
			declaringType.getTypeQualifiedName(enclosingTypeSeparator)
			+ enclosingTypeSeparator
			+ fName;
	}
}
/**
 * @see IType#getTypes()
 */
public IType[] getTypes() throws JavaModelException {
	ArrayList list = getChildrenOfType(TYPE);
	int size;
	if ((size = list.size()) == 0) {
		return NO_TYPES;
	} else {
		IType[] array= new IType[size];
		list.toArray(array);
		return array;
	}
}
/**
 * @see IParent#hasChildren()
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IType#isAnonymous()
 */
public boolean isAnonymous() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	return info.isAnonymous();
}
/**
 * @see IType#isClass()
 */
public boolean isClass() throws JavaModelException {
	return !isInterface();
}
/**
 * @see IType#isInterface()
 */
public boolean isInterface() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	return info.isInterface();
}

/**
 * @see IType#isLocal()
 */
public boolean isLocal() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	return info.isLocal();
}
/**
 * @see IType#isMember()
 */
public boolean isMember() throws JavaModelException {
	IBinaryType info = (IBinaryType) getElementInfo();
	return info.isMember();
}
/**
 * @see IType
 */
public ITypeHierarchy loadTypeHierachy(InputStream input, IProgressMonitor monitor) throws JavaModelException {
	return loadTypeHierachy(input, DefaultWorkingCopyOwner.PRIMARY, monitor);
}
/**
 * @see IType
 */
public ITypeHierarchy loadTypeHierachy(InputStream input, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	return TypeHierarchy.load(this, input, owner);
}
/**
 * @see IType#newSupertypeHierarchy(IProgressMonitor monitor)
 */
public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	return this.newSupertypeHierarchy(DefaultWorkingCopyOwner.PRIMARY, monitor);
}
/**
 * @see IType#newSupertypeHierarchy(IWorkingCopy[], IProgressMonitor)
 * @deprecated
 */
public ITypeHierarchy newSupertypeHierarchy(
	IWorkingCopy[] workingCopies,
	IProgressMonitor monitor)
	throws JavaModelException {
	
	ICompilationUnit[] copies;
	if (workingCopies == null) {
		copies = null;
	} else {
		int length = workingCopies.length;
		System.arraycopy(workingCopies, 0, copies = new ICompilationUnit[length], 0, length);
	}
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, copies, SearchEngine.createWorkspaceScope(), false);
	runOperation(op, monitor);
	return op.getResult();
}
/**
 * @see IType#newSupertypeHierarchy(WorkingCopyOwner, IProgressMonitor)
 */
public ITypeHierarchy newSupertypeHierarchy(
	WorkingCopyOwner owner,
	IProgressMonitor monitor)
	throws JavaModelException {

	ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, workingCopies, SearchEngine.createWorkspaceScope(), false);
	runOperation(op, monitor);
	return op.getResult();
}
/**
 * @see IType#newTypeHierarchy(IJavaProject, IProgressMonitor)
 */
public ITypeHierarchy newTypeHierarchy(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
	return newTypeHierarchy(project, DefaultWorkingCopyOwner.PRIMARY, monitor);
}
/**
 * @see IType#newTypeHierarchy(IJavaProject, WorkingCopyOwner, IProgressMonitor)
 */
public ITypeHierarchy newTypeHierarchy(IJavaProject project, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	if (project == null) {
		throw new IllegalArgumentException(Util.bind("hierarchy.nullProject")); //$NON-NLS-1$
	}
	ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
	ICompilationUnit[] projectWCs = null;
	if (workingCopies != null) {
		int length = workingCopies.length;
		projectWCs = new ICompilationUnit[length];
		int index = 0;
		for (int i = 0; i < length; i++) {
			ICompilationUnit wc = workingCopies[i];
			if (project.equals(wc.getJavaProject())) {
				projectWCs[index++] = wc;
			}
		}
		if (index != length) {
			System.arraycopy(projectWCs, 0, projectWCs = new ICompilationUnit[index], 0, index);
		}
	}
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(
		this, 
		projectWCs,
		project, 
		true);
	runOperation(op, monitor);
	return op.getResult();
}
/**
 * @see IType#newTypeHierarchy(IProgressMonitor monitor)
 * @deprecated
 */
public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	return newTypeHierarchy((IWorkingCopy[])null, monitor);
}
/**
 * @see IType#newTypeHierarchy(IWorkingCopy[], IProgressMonitor)
 * @deprecated
 */
public ITypeHierarchy newTypeHierarchy(
	IWorkingCopy[] workingCopies,
	IProgressMonitor monitor)
	throws JavaModelException {

	ICompilationUnit[] copies;
	if (workingCopies == null) {
		copies = null;
	} else {
		int length = workingCopies.length;
		System.arraycopy(workingCopies, 0, copies = new ICompilationUnit[length], 0, length);
	}
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, copies, SearchEngine.createWorkspaceScope(), true);
	runOperation(op, monitor);
	return op.getResult();
}
/**
 * @see IType#newTypeHierarchy(WorkingCopyOwner, IProgressMonitor)
 */
public ITypeHierarchy newTypeHierarchy(
	WorkingCopyOwner owner,
	IProgressMonitor monitor)
	throws JavaModelException {
		
	ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, workingCopies, SearchEngine.createWorkspaceScope(), true);
	runOperation(op, monitor);
	return op.getResult();	
}
/**
 * @see IType#resolveType(String)
 */
public String[][] resolveType(String typeName) throws JavaModelException {
	// not implemented for binary types
	return null;
}
/**
 * @see IType#resolveType(String, WorkingCopyOwner)
 */
public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException {
	// not implemented for binary types
	return null;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		buffer.append(this.getElementName());
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append(getElementName());
	} else {
		try {
			if (this.isInterface()) {
				buffer.append("interface "); //$NON-NLS-1$
			} else {
				buffer.append("class "); //$NON-NLS-1$
			}
			buffer.append(this.getElementName());
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
