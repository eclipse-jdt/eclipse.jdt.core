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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;

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
	Assert.isTrue(name.indexOf('.') == -1);
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
		throw new IllegalArgumentException(Util.bind("codeAssist.nullRequestor")); //$NON-NLS-1$
	}
	
	JavaProject project = (JavaProject) getJavaProject();
	SearchableEnvironment environment = (SearchableEnvironment) project.getSearchableNameEnvironment();
	NameLookup nameLookup = project.getNameLookup();
	CompletionEngine engine = new CompletionEngine(environment, new CompletionRequestorWrapper(requestor,nameLookup), project.getOptions(true), project);
	
	String source = getCompilationUnit().getSource();
	if (source != null && insertion > -1 && insertion < source.length()) {
		try {
			// set the units to look inside
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			ICompilationUnit[] workingCopies = manager.getWorkingCopies(owner, true/*add primary WCs*/);
			nameLookup.setUnitsToLookInside(workingCopies);
	
			// code complete
			String encoding = project.getOption(JavaCore.CORE_ENCODING, true);
			
			char[] prefix = CharOperation.concat(source.substring(0, insertion).toCharArray(), new char[]{'{'});
			char[] suffix = CharOperation.concat(new char[]{'}'}, source.substring(insertion).toCharArray());
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
 * @see IType
 */
public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	CreateFieldOperation op = new CreateFieldOperation(this, contents, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
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
	runOperation(op, monitor);
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
	runOperation(op, monitor);
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
	runOperation(op, monitor);
	return (IType) op.getResultElements()[0];
}
public boolean equals(Object o) {
	if (!(o instanceof SourceType)) return false;
	return super.equals(o);
}
/**
 * @see JavaElement#equalsDOMNode
 */
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
	IJavaElement parent = getParent();
	while (parent != null) {
		if (parent.getElementType() == IJavaElement.TYPE) {
			return (IType) parent;
		} else
			if (parent instanceof IMember) {
				parent = parent.getParent();
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
public IField getField(String name) {
	return new SourceField(this, name);
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
 * @see IType#getFullyQualifiedName
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

/**
 * @see IType
 */
public IInitializer getInitializer(int occurrenceCount) {
	return new Initializer(this, occurrenceCount);
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
public IMethod getMethod(String name, String[] parameterTypeSignatures) {
	return new SourceMethod(this, name, parameterTypeSignatures);
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
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner) {
		CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
		if (cu.owner == DefaultWorkingCopyOwner.PRIMARY) return this;
	}
	IJavaElement parent = fParent.getPrimaryElement(false);
	if (parent instanceof IType) {
		return ((IType)parent).getType(fName);
	} else {
		return ((ICompilationUnit)parent).getType(fName);
	}
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
 * @see IType
 */
public IType getType(String name) {
	return new SourceType(this, name);
}
/**
 * @see IType#getTypeQualifiedName
 */
public String getTypeQualifiedName() {
	return this.getTypeQualifiedName('$');
}
/**
 * @see IType#getTypeQualifiedName(char)
 */
public String getTypeQualifiedName(char enclosingTypeSeparator) {
	if (fParent.getElementType() == IJavaElement.COMPILATION_UNIT) {
		return fName;
	} else {
		return ((IType) fParent).getTypeQualifiedName(enclosingTypeSeparator) + enclosingTypeSeparator + fName;
	}
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
 * @see IParent 
 */
public boolean hasChildren() throws JavaModelException {
	return getChildren().length > 0;
}
/**
 * @see IType#isAnonymous()
 */
public boolean isAnonymous() throws JavaModelException {
	return false; // cannot create source handle onto anonymous types
}
/**
 * @see IType
 */
public boolean isClass() throws JavaModelException {
	return !isInterface();
}
/**
 * @see IType
 */
public boolean isInterface() throws JavaModelException {
	SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
	return info.isInterface();
}
/**
 * @see IType#isLocal()
 */
public boolean isLocal() throws JavaModelException {
	return false; // cannot create source handle onto local types
}
/**
 * @see IType#isMember()
 */
public boolean isMember() throws JavaModelException {
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
 * @see ITypeHierarchy#store(OutputStream, IProgressMonitor)
 * @since 3.0
 */
public ITypeHierarchy loadTypeHierachy(InputStream input, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
	return TypeHierarchy.load(this, input, owner);
}
/**
 * @see IType
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
	runOperation(op, monitor);
	return op.getResult();
}
/**
 * @see IType
 */
public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
	CreateTypeHierarchyOperation op= new CreateTypeHierarchyOperation(this, null, SearchEngine.createWorkspaceScope(), true);
	runOperation(op, monitor);
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
	return resolveType(typeName, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IType#resolveType(String, WorkingCopyOwner)
 */
public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException {
	JavaProject project = (JavaProject)getJavaProject();
	NameLookup lookup = null;
	try {
		// set the units to look inside
		lookup = ((JavaProject)project).getNameLookup();
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		ICompilationUnit[] workingCopies = manager.getWorkingCopies(owner, true/*add primary WCs*/);
		lookup.setUnitsToLookInside(workingCopies);
			
		// resolve
		ISourceType info = (ISourceType) this.getElementInfo();
		ISearchableNameEnvironment environment = project.getSearchableNameEnvironment();
	
		class TypeResolveRequestor implements ISelectionRequestor {
			String[][] answers = null;
			void acceptType(String[] answer){
				if (answers == null) {
					answers = new String[][]{ answer };
				} else {
					// grow
					int length = answers.length;
					System.arraycopy(answers, 0, answers = new String[length+1][], 0, length);
					answers[length] = answer;
				}
			}
			public void acceptClass(char[] packageName, char[] className, boolean needQualification) {
				acceptType(new String[]  { new String(packageName), new String(className) });
			}
			
			public void acceptInterface(char[] packageName, char[] interfaceName, boolean needQualification) {
				acceptType(new String[]  { new String(packageName), new String(interfaceName) });
			}
	
			public void acceptError(IProblem error) {}
			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name) {}
			public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, boolean isConstructor) {}
			public void acceptPackage(char[] packageName){}
	
		}
		TypeResolveRequestor requestor = new TypeResolveRequestor();
		SelectionEngine engine = 
			new SelectionEngine(environment, requestor, this.getJavaProject().getOptions(true));
			
	 	IType[] topLevelTypes = this.getCompilationUnit().getTypes();
	 	int length = topLevelTypes.length;
	 	ISourceType[] topLevelInfos = new ISourceType[length];
	 	for (int i = 0; i < length; i++) {
			topLevelInfos[i] = (ISourceType)((SourceType)topLevelTypes[i]).getElementInfo();
		}
			
		engine.selectType(info, typeName.toCharArray(), topLevelInfos, false);
		return requestor.answers;
	} finally {
		if (lookup != null) {
			lookup.setUnitsToLookInside(null);
		}
	}
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
