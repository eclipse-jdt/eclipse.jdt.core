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

import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Handle for a source type. Info object is a SourceTypeElementInfo.
 *
 * Note: Parent is either an IClassFile, an ICompilationUnit or an IType.
 *
 * @see IType
 */

public class SourceType extends Member implements IType {
	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
protected SourceType(JavaElement parent, String name) {
	super(parent, name);
	Assert.isTrue(name.indexOf('.') == -1, Util.bind("sourcetype.invalidName", name)); //$NON-NLS-1$
}
/**
 * @see IType
 */
public void codeComplete(char[] snippet,int insertion,int position,char[][] localVariableTypeNames,char[][] localVariableNames,int[] localVariableModifiers,boolean isStatic,ICompletionRequestor requestor) throws JavaModelException {
	codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IType
 */
public void codeComplete(char[] snippet,int insertion,int position,char[][] localVariableTypeNames,char[][] localVariableNames,int[] localVariableModifiers,boolean isStatic,ICompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
	if (requestor == null) {
		throw new IllegalArgumentException("Completion requestor cannot be null"); //$NON-NLS-1$
	}
	
	JavaProject project = (JavaProject) getJavaProject();
	SearchableEnvironment environment = (SearchableEnvironment) project.newSearchableNameEnvironment(owner);
	CompletionRequestorWrapper requestorWrapper = new CompletionRequestorWrapper(requestor, environment.nameLookup);
	CompletionEngine engine = new CompletionEngine(environment, requestorWrapper, project.getOptions(true), project);
	requestorWrapper.completionEngine = engine;
	
	String source = getCompilationUnit().getSource();
	if (source != null && insertion > -1 && insertion < source.length()) {
		
		char[] prefix = CharOperation.concat(source.substring(0, insertion).toCharArray(), new char[]{'{'});
		char[] suffix = CharOperation.concat(new char[]{'}'}, source.substring(insertion).toCharArray());
		char[] fakeSource = CharOperation.concat(prefix, snippet, suffix);
		
		BasicCompilationUnit cu = 
			new BasicCompilationUnit(
				fakeSource, 
				null,
				getElementName(),
				getParent());

		engine.complete(cu, prefix.length + position, prefix.length);
	} else {
		engine.complete(this, snippet, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic);
	}
}
/**
 * @see IType
 */
public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateFieldOperation op = new CreateFieldOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	op.runOperation(monitor);
	return (IField) op.getResultElements()[0];
}
/**
 * @see IType
 */
public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
	CreateInitializerOperation op = new CreateInitializerOperation(this, contents);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	op.runOperation(monitor);
	return (IInitializer) op.getResultElements()[0];
}
/**
 * @see IType
 */
public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateMethodOperation op = new CreateMethodOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	op.runOperation(monitor);
	return (IMethod) op.getResultElements()[0];
}
/**
 * @see IType
 */
public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateTypeOperation op = new CreateTypeOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	op.runOperation(monitor);
	return (IType) op.getResultElements()[0];
}
public boolean equals(Object o) {
	if (!(o instanceof SourceType)) return false;
	return super.equals(o);
}
/**
 * @see JavaElement#equalsDOMNode
 * @deprecated JDOM is obsolete
 */
// TODO - JDOM - remove once model ported off of JDOM
protected boolean equalsDOMNode(IDOMNode node) {
	return (node.getNodeType() == IDOMNode.TYPE) && super.equalsDOMNode(node);
}
/*
 * @see IType
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
 * @see IMember
 */
public IType getDeclaringType() {
	IJavaElement parentElement = getParent();
	while (parentElement != null) {
		if (parentElement.getElementType() == IJavaElement.TYPE) {
			return (IType) parentElement;
		} else
			if (parentElement instanceof IMember) {
				parentElement = parentElement.getParent();
			} else {
				return null;
			}
	}
	return null;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return TYPE;
}
/**
 * @see IType#getField
 */
public IField getField(String fieldName) {
	return new SourceField(this, fieldName);
}
/**
 * @see IType
 */
public IField[] getFields() throws JavaModelException {
	ArrayList list = getChildrenOfType(FIELD);
	IField[] array= new IField[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType#getFullyQualifiedName()
 */
public String getFullyQualifiedName() {
	return this.getFullyQualifiedName('$');
}
/**
 * @see IType#getFullyQualifiedName(char)
 */
public String getFullyQualifiedName(char enclosingTypeSeparator) {
	String packageName = getPackageFragment().getElementName();
	if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
		return getTypeQualifiedName(enclosingTypeSeparator);
	}
	return packageName + '.' + getTypeQualifiedName(enclosingTypeSeparator);
}
/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
	switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
		case JEM_FIELD:
			String fieldName = memento.nextToken();
			JavaElement field = (JavaElement)getField(fieldName);
			return field.getHandleFromMemento(memento, workingCopyOwner);
		case JEM_INITIALIZER:
			String count = memento.nextToken();
			JavaElement initializer = (JavaElement)getInitializer(Integer.parseInt(count));
			return initializer.getHandleFromMemento(memento, workingCopyOwner);
		case JEM_METHOD:
			String selector = memento.nextToken();
			ArrayList params = new ArrayList();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
					case JEM_TYPE:
						break nextParam;
					case JEM_METHOD:
						String param = memento.nextToken();
						StringBuffer buffer = new StringBuffer();
						while (Signature.C_ARRAY == param.charAt(0)) {
							buffer.append(Signature.C_ARRAY);
							param = memento.nextToken();
						}
						params.add(buffer.toString() + param);
						break;
					default:
						break nextParam;
				}
			}
			String[] parameters = new String[params.size()];
			params.toArray(parameters);
			JavaElement method = (JavaElement)getMethod(selector, parameters);
			if (token != null) {
				switch (token.charAt(0)) {
					case JEM_TYPE:
					case JEM_LOCALVARIABLE:
						return method.getHandleFromMemento(token, memento, workingCopyOwner);
					default:
						return method;
				}
			} else {
				return method;
			}
		case JEM_TYPE:
			String typeName;
			if (memento.hasMoreTokens()) {
				typeName = memento.nextToken();
				char firstChar = typeName.charAt(0);
				if (firstChar == JEM_FIELD || firstChar == JEM_INITIALIZER || firstChar == JEM_METHOD || firstChar == JEM_TYPE || firstChar == JEM_COUNT) {
					token = typeName;
					typeName = ""; //$NON-NLS-1$
				} else {
					token = null;
				}
			} else {
				typeName = ""; //$NON-NLS-1$
				token = null;
			}
			JavaElement type = (JavaElement)getType(typeName);
			if (token == null) {
				return type.getHandleFromMemento(memento, workingCopyOwner);
			} else {
				return type.getHandleFromMemento(token, memento, workingCopyOwner);
			}
	}
	return null;
}
/**
 * @see IType
 */
public IInitializer getInitializer(int count) {
	return new Initializer(this, count);
}
/**
 * @see IType
 */
public IInitializer[] getInitializers() throws JavaModelException {
	ArrayList list = getChildrenOfType(INITIALIZER);
	IInitializer[] array= new IInitializer[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType#getMethod
 */
public IMethod getMethod(String selector, String[] parameterTypeSignatures) {
	return new SourceMethod(this, selector, parameterTypeSignatures);
}
/**
 * @see IType
 */
public IMethod[] getMethods() throws JavaModelException {
	ArrayList list = getChildrenOfType(METHOD);
	IMethod[] array= new IMethod[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType
 */
public IPackageFragment getPackageFragment() {
	IJavaElement parentElement = this.parent;
	while (parentElement != null) {
		if (parentElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			return (IPackageFragment)parentElement;
		}
		else {
			parentElement = parentElement.getParent();
		}
	}
	Assert.isTrue(false);  // should not happen
	return null;
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner) {
		CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
		if (cu.isPrimary()) return this;
	}
	IJavaElement primaryParent = this.parent.getPrimaryElement(false);
	switch (primaryParent.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			return ((ICompilationUnit)primaryParent).getType(this.name);
		case IJavaElement.TYPE:
			return ((IType)primaryParent).getType(this.name);
		case IJavaElement.FIELD:
		case IJavaElement.INITIALIZER:
		case IJavaElement.METHOD:
			return ((IMember)primaryParent).getType(this.name, this.occurrenceCount);
	}
	return this;
}
/**
 * @see IType
 */
public String getSuperclassName() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	char[] superclassName= info.getSuperclassName();
	if (superclassName == null) {
		return null;
	}
	return new String(superclassName);
}

/**
 * @see IType#getSuperclassTypeSignature()
 * @since 3.0
 */
public String getSuperclassTypeSignature() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	char[] superclassName= info.getSuperclassName();
	if (superclassName == null) {
		return null;
	}
	return new String(Signature.createTypeSignature(superclassName, false));
}

/**
 * @see IType
 */
public String[] getSuperInterfaceNames() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	char[][] names= info.getInterfaceNames();
	if (names == null) {
		return fgEmptyList;
	}
	String[] strings= new String[names.length];
	for (int i= 0; i < names.length; i++) {
		strings[i]= new String(names[i]);
	}
	return strings;
}

/**
 * @see IType#getSuperInterfaceTypeSignatures()
 * @since 3.0
 */
public String[] getSuperInterfaceTypeSignatures() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	char[][] names= info.getInterfaceNames();
	if (names == null) {
		return fgEmptyList;
	}
	String[] strings= new String[names.length];
	for (int i= 0; i < names.length; i++) {
		strings[i]= new String(Signature.createTypeSignature(names[i], false));
	}
	return strings;
}

/**
 * @see IType#getTypeParameterSignatures()
 * @since 3.0
 */
public String[] getTypeParameterSignatures() throws JavaModelException {
	// TODO (jerome) - missing implementation
	return new String[0];
}

/**
 * @see IType
 */
public IType getType(String typeName) {
	return new SourceType(this, typeName);
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
	switch (this.parent.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			return this.name;
		case IJavaElement.TYPE:
			String simpleName = this.name.length() == 0 ? Integer.toString(this.occurrenceCount) : this.name;
			return ((IType) this.parent).getTypeQualifiedName(enclosingTypeSeparator) + enclosingTypeSeparator + simpleName;
		case IJavaElement.FIELD:
		case IJavaElement.INITIALIZER:
		case IJavaElement.METHOD:
			simpleName = this.name.length() == 0 ? Integer.toString(this.occurrenceCount) : this.name;
			return 
				((IMember) this.parent).getDeclaringType().getTypeQualifiedName(enclosingTypeSeparator) 
				+ enclosingTypeSeparator + simpleName;
	}
	return null;
}

/**
 * @see IType
 */
public IType[] getTypes() throws JavaModelException {
	ArrayList list= getChildrenOfType(TYPE);
	IType[] array= new IType[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IType#isAnonymous()
 */
public boolean isAnonymous() {
	return this.name.length() == 0;
}

/**
 * @see IType
 */
public boolean isClass() throws JavaModelException {
	// TODO (jerome) - isClass should only return true for classes other than enum classes
	return !isInterface();
}

/**
 * @see IType#isEnum()
 * @since 3.0
 */
public boolean isEnum() throws JavaModelException {
	// TODO (jerome) - missing implementation - should only return true for enum classes
	return false;
}

/**
 * @see IType
 */
public boolean isInterface() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	// TODO (jerome) - isInterface should not return true for annotation types
	return info.isInterface();
}

/**
 * @see IType#isAnnotation()
 * @since 3.0
 */
public boolean isAnnotation() throws JavaModelException {
	// TODO (jerome) - missing implementation - should only return true for annotation types
	return false;
}

/**
 * @see IType#isLocal()
 */
public boolean isLocal() {
	return this.parent instanceof IMethod || this.parent instanceof IInitializer;
}
/**
 * @see IType#isMember()
 */
public boolean isMember() {
	return getDeclaringType() != null;
}
/**
 * @see IType
 */
public ITypeHierarchy loadTypeHierachy(InputStream input, IProgressMonitor monitor) throws JavaModelException {
	return loadTypeHierachy(input, DefaultWorkingCopyOwner.PRIMARY, monitor);
}
/**
 * NOTE: This method is not part of the API has it is not clear clients would easily use it: they would need to
 * first make sure all working copies for the given owner exist before calling it. This is especially har at startup 
 * time.
 * In case clients want this API, here is how it should be specified:
 * <p>
 * Loads a previously saved ITypeHierarchy from an input stream. A type hierarchy can
 * be stored using ITypeHierachy#store(OutputStream). A compilation unit of a
 * loaded type has the given owner if such a working copy exists, otherwise the type's 
 * compilation unit is a primary compilation unit.
 * 
 * Only hierarchies originally created by the following methods can be loaded:
 * <ul>
 * <li>IType#newSupertypeHierarchy(IProgressMonitor)</li>
 * <li>IType#newSupertypeHierarchy(WorkingCopyOwner, IProgressMonitor)</li>
 * <li>IType#newTypeHierarchy(IJavaProject, IProgressMonitor)</li>
 * <li>IType#newTypeHierarchy(IJavaProject, WorkingCopyOwner, IProgressMonitor)</li>
 * <li>IType#newTypeHierarchy(IProgressMonitor)</li>
 * <li>IType#newTypeHierarchy(WorkingCopyOwner, IProgressMonitor)</li>
 * </u>
 * 
 * @param input stream where hierarchy will be read
 * @param monitor the given progress monitor
 * @return the stored hierarchy
 * @exception JavaModelException if the hierarchy could not be restored, reasons include:
 *      - type is not the focus of the hierarchy or 
 *		- unable to read the input stream (wrong format, IOException during reading, ...)
 * @see ITypeHierarchy#store(java.io.OutputStream, IProgressMonitor)
 * @since 3.0
 */
public ITypeHierarchy loadTypeHierachy(InputStream input, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	// TODO monitor should be passed to TypeHierarchy.load(...)
	return TypeHierarchy.load(this, input, owner);
}
/**
 * @see IType
 */
public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	return this.newSupertypeHierarchy(DefaultWorkingCopyOwner.PRIMARY, monitor);
}
/*
 * @see IType#newSupertypeHierarchy(ICompilationUnit[], IProgressMonitor)
 */
public ITypeHierarchy newSupertypeHierarchy(
	ICompilationUnit[] workingCopies,
	IProgressMonitor monitor)
	throws JavaModelException {

	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, workingCopies, SearchEngine.createWorkspaceScope(), false);
	op.runOperation(monitor);
	return op.getResult();
}
/**
 * @param workingCopies the working copies that take precedence over their original compilation units
 * @param monitor the given progress monitor
 * @return a type hierarchy for this type containing this type and all of its supertypes
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource.
 *
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
	return newSupertypeHierarchy(copies, monitor);
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
	op.runOperation(monitor);
	return op.getResult();
}
/**
 * @see IType
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
	op.runOperation(monitor);
	return op.getResult();
}
/**
 * @see IType
 */
public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, null, SearchEngine.createWorkspaceScope(), true);
	op.runOperation(monitor);
	return op.getResult();
}
/*
 * @see IType#newTypeHierarchy(ICompilationUnit[], IProgressMonitor)
 */
public ITypeHierarchy newTypeHierarchy(
	ICompilationUnit[] workingCopies,
	IProgressMonitor monitor)
	throws JavaModelException {
		
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, workingCopies, SearchEngine.createWorkspaceScope(), true);
	op.runOperation(monitor);
	return op.getResult();
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
	return newTypeHierarchy(copies, monitor);
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
	op.runOperation(monitor);
	return op.getResult();	
}
/**
 * @see IType#resolveType(String)
 */
public String[][] resolveType(String typeName) throws JavaModelException {
	return resolveType(typeName, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IType#resolveType(String, WorkingCopyOwner)
 */
public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException {
	ISourceType info = (ISourceType) getElementInfo();
	JavaProject project = (JavaProject) getJavaProject();
	ISearchableNameEnvironment environment = project.newSearchableNameEnvironment(owner);

	class TypeResolveRequestor implements ISelectionRequestor {
		String[][] answers = null;
		void acceptType(String[] answer){
			if (this.answers == null) {
				this.answers = new String[][]{ answer };
			} else {
				// grow
				int length = this.answers.length;
				System.arraycopy(this.answers, 0, this.answers = new String[length+1][], 0, length);
				this.answers[length] = answer;
			}
		}
		public void acceptClass(char[] packageName, char[] className, boolean needQualification) {
			acceptType(new String[]  { new String(packageName), new String(className) });
		}
		
		public void acceptInterface(char[] packageName, char[] interfaceName, boolean needQualification) {
			acceptType(new String[]  { new String(packageName), new String(interfaceName) });
		}

		public void acceptError(IProblem error) {
			// ignore
		}
		public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] fieldName) {
			// ignore
		}
		public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, boolean isConstructor, boolean isDeclaration, int start, int end) {
			// ignore
		}
		public void acceptPackage(char[] packageName){
			// ignore
		}

	}
	TypeResolveRequestor requestor = new TypeResolveRequestor();
	SelectionEngine engine = 
		new SelectionEngine(environment, requestor, project.getOptions(true));
		
 	IType[] topLevelTypes = getCompilationUnit().getTypes();
 	int length = topLevelTypes.length;
 	SourceTypeElementInfo[] topLevelInfos = new SourceTypeElementInfo[length];
 	for (int i = 0; i < length; i++) {
		topLevelInfos[i] = (SourceTypeElementInfo) ((SourceType)topLevelTypes[i]).getElementInfo();
	}
		
	engine.selectType(info, typeName.toCharArray(), topLevelInfos, false);
	return requestor.answers;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(tabString(tab));
	if (info == null) {
		String elementName = getElementName();
		if (elementName.length() == 0) {
			buffer.append("<anonymous #"); //$NON-NLS-1$
			buffer.append(this.occurrenceCount);
			buffer.append(">"); //$NON-NLS-1$
		} else {
			toStringName(buffer);
		}
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		String elementName = getElementName();
		if (elementName.length() == 0) {
			buffer.append("<anonymous #"); //$NON-NLS-1$
			buffer.append(this.occurrenceCount);
			buffer.append(">"); //$NON-NLS-1$
		} else {
			toStringName(buffer);
		}
	} else {
		try {
			if (this.isInterface()) {
				buffer.append("interface "); //$NON-NLS-1$
			} else {
				buffer.append("class "); //$NON-NLS-1$
			}
			String elementName = getElementName();
			if (elementName.length() == 0) {
				buffer.append("<anonymous #"); //$NON-NLS-1$
				buffer.append(this.occurrenceCount);
				buffer.append(">"); //$NON-NLS-1$
			} else {
				toStringName(buffer);
			}
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
