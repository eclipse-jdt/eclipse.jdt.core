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

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see ICompilationUnit
 */

public class CompilationUnit extends Openable implements ICompilationUnit, org.eclipse.jdt.internal.compiler.env.ICompilationUnit, SuffixConstants {
	
	public WorkingCopyOwner owner;

/**
 * Constructs a handle to a compilation unit with the given name in the
 * specified package for the specified owner
 *
 * @exception IllegalArgumentException if the name of the compilation unit
 * does not end with ".java"
 */
protected CompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
	super(parent, name);
	this.owner = owner;
}
/**
 * Accepts the given visitor onto the parsed tree of this compilation unit, after
 * having runned the name resolution.
 * The visitor's corresponding <code>visit</code> method is called with the
 * corresponding parse tree. If the visitor returns <code>true</code>, this method
 * visits this parse node's members.
 *
 * @param visitor the visitor
 * @exception JavaModelException if this method fails. Reasons include:
 * <ul>
 * <li> This element does not exist.</li>
 * <li> The visitor failed with this exception.</li>
 * </ul>
 */
public void accept(ASTVisitor visitor) throws JavaModelException {
	CompilationUnitVisitor.visit(this, visitor);
}
/*
 * @see ICompilationUnit#becomeWorkingCopy(IProblemRequestor, IProgressMonitor)
 */
public void becomeWorkingCopy(IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager.getPerWorkingCopyInfo(this, false/*don't create*/, true /*record usage*/, null/*no problem requestor needed*/);
	if (perWorkingCopyInfo == null) {
		// close cu and its children
		close();

		BecomeWorkingCopyOperation operation = new BecomeWorkingCopyOperation(this, problemRequestor);
		operation.runOperation(monitor);
	}
}
protected boolean buildStructure(OpenableElementInfo info, final IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {

	// check if this compilation unit can be opened
	if (!isWorkingCopy()) { // no check is done on root kind or exclusion pattern for working copies
		if (((IPackageFragment)getParent()).getKind() == IPackageFragmentRoot.K_BINARY
				|| !isValidCompilationUnit()
				|| !underlyingResource.isAccessible()) {
			throw newNotPresentException();
		}
	}
	
	// prevents reopening of non-primary working copies (they are closed when they are discarded and should not be reopened)
	if (!isPrimary() && getPerWorkingCopyInfo() == null) {
		throw newNotPresentException();
	}

	CompilationUnitElementInfo unitInfo = (CompilationUnitElementInfo) info;

	// get buffer contents
	IBuffer buffer = getBufferManager().getBuffer(CompilationUnit.this);
	if (buffer == null) {
		buffer = openBuffer(pm, unitInfo); // open buffer independently from the info, since we are building the info
	}
	final char[] contents = buffer == null ? null : buffer.getCharacters();

	// generate structure and compute syntax problems if needed
	CompilationUnitStructureRequestor requestor = new CompilationUnitStructureRequestor(this, unitInfo, newElements);
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = getPerWorkingCopyInfo();
	IJavaProject project = getJavaProject();
	boolean computeProblems = JavaProject.hasJavaNature(project.getProject()) && perWorkingCopyInfo != null && perWorkingCopyInfo.isActive();
	IProblemFactory problemFactory = new DefaultProblemFactory();
	Map options = project.getOptions(true);
	SourceElementParser parser = new SourceElementParser(
		requestor, 
		problemFactory, 
		new CompilerOptions(options),
		true/*report local declarations*/);
	requestor.parser = parser;
	CompilationUnitDeclaration unit = parser.parseCompilationUnit(new org.eclipse.jdt.internal.compiler.env.ICompilationUnit() {
			public char[] getContents() {
				return contents;
			}
			public char[] getMainTypeName() {
				return CompilationUnit.this.getMainTypeName();
			}
			public char[][] getPackageName() {
				return CompilationUnit.this.getPackageName();
			}
			public char[] getFileName() {
				return CompilationUnit.this.getFileName();
			}
		}, true /*full parse to find local elements*/);
	
	// update timestamp (might be IResource.NULL_STAMP if original does not exist)
	if (underlyingResource == null) {
		underlyingResource = getResource();
	}
	unitInfo.timestamp = ((IFile)underlyingResource).getModificationStamp();
	
	// compute other problems if needed
	CompilationUnitDeclaration compilationUnitDeclaration = null;
	try {
		if (computeProblems){
			perWorkingCopyInfo.beginReporting();
			compilationUnitDeclaration = CompilationUnitProblemFinder.process(unit, this, parser, this.owner, perWorkingCopyInfo, problemFactory, false/*don't cleanup cu*/, pm);
			perWorkingCopyInfo.endReporting();
		}
		
		if (info instanceof ASTHolderCUInfo) {
			org.eclipse.jdt.core.dom.CompilationUnit cu = AST.convertCompilationUnit(unit, contents, options, pm);
			((ASTHolderCUInfo) info).ast = cu;
		}
	} finally {
	    if (compilationUnitDeclaration != null) {
	        compilationUnitDeclaration.cleanUp();
	    }
	}
	
	return unitInfo.isStructureKnown();
}
/*
 * @see Openable#canBeRemovedFromCache
 */
public boolean canBeRemovedFromCache() {
	if (getPerWorkingCopyInfo() != null) return false; // working copies should remain in the cache until they are destroyed
	return super.canBeRemovedFromCache();
}
/*
 * @see Openable#canBufferBeRemovedFromCache
 */
public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
	if (getPerWorkingCopyInfo() != null) return false; // working copy buffers should remain in the cache until working copy is destroyed
	return super.canBufferBeRemovedFromCache(buffer);
}/*
 * @see IOpenable#close
 */
public void close() throws JavaModelException {
	if (getPerWorkingCopyInfo() != null) return; // a working copy must remain opened until it is discarded
	super.close();
}
/*
 * @see Openable#closing
 */
protected void closing(Object info) {
	if (getPerWorkingCopyInfo() == null) {
		super.closing(info);
	} // else the buffer of a working copy must remain open for the lifetime of the working copy
}
/**
 * @see ICodeAssist#codeComplete(int, ICompletionRequestor)
 */
public void codeComplete(int offset, ICompletionRequestor requestor) throws JavaModelException {
	codeComplete(offset, requestor, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see ICodeAssist#codeComplete(int, ICompletionRequestor, WorkingCopyOwner)
 */
public void codeComplete(int offset, ICompletionRequestor requestor, WorkingCopyOwner workingCopyOwner) throws JavaModelException {
	codeComplete(this, isWorkingCopy() ? (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) getOriginalElement() : this, offset, requestor, workingCopyOwner);
}
/**
 * @see ICodeAssist#codeComplete(int, ICodeCompletionRequestor)
 * @deprecated - use codeComplete(int, ICompletionRequestor)
 */
public void codeComplete(int offset, final ICodeCompletionRequestor requestor) throws JavaModelException {
	
	if (requestor == null){
		codeComplete(offset, (ICompletionRequestor)null);
		return;
	}
	codeComplete(
		offset,
		new ICompletionRequestor(){
			public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
				// ignore
			}
			public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
				requestor.acceptClass(packageName, className, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptError(IProblem error) {
				if (true) return; // was disabled in 1.0

				try {
					IMarker marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IJavaModelMarker.TRANSIENT_PROBLEM);
					marker.setAttribute(IJavaModelMarker.ID, error.getID());
					marker.setAttribute(IMarker.CHAR_START, error.getSourceStart());
					marker.setAttribute(IMarker.CHAR_END, error.getSourceEnd() + 1);
					marker.setAttribute(IMarker.LINE_NUMBER, error.getSourceLineNumber());
					marker.setAttribute(IMarker.MESSAGE, error.getMessage());
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					requestor.acceptError(marker);
				} catch(CoreException e){
					// ignore
				}
			}
			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] fieldName, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
				requestor.acceptField(declaringTypePackageName, declaringTypeName, fieldName, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptInterface(char[] packageName,char[] interfaceName,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance) {
				requestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptKeyword(char[] keywordName,int completionStart,int completionEnd, int relevance){
				requestor.acceptKeyword(keywordName, completionStart, completionEnd);
			}
			public void acceptLabel(char[] labelName,int completionStart,int completionEnd, int relevance){
				requestor.acceptLabel(labelName, completionStart, completionEnd);
			}
			public void acceptLocalVariable(char[] localVarName,char[] typePackageName,char[] typeName,int modifiers,int completionStart,int completionEnd, int relevance){
				// ignore
			}
			public void acceptMethod(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
				// skip parameter names
				requestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
			}
			public void acceptMethodDeclaration(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
				// ignore
			}
			public void acceptModifier(char[] modifierName,int completionStart,int completionEnd, int relevance){
				requestor.acceptModifier(modifierName, completionStart, completionEnd);
			}
			public void acceptPackage(char[] packageName,char[] completionName,int completionStart,int completionEnd, int relevance){
				requestor.acceptPackage(packageName, completionName, completionStart, completionEnd);
			}
			public void acceptType(char[] packageName,char[] typeName,char[] completionName,int completionStart,int completionEnd, int relevance){
				requestor.acceptType(packageName, typeName, completionName, completionStart, completionEnd);
			}
			public void acceptVariableName(char[] typePackageName,char[] typeName,char[] varName,char[] completionName,int completionStart,int completionEnd, int relevance){
				// ignore
			}
		});
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
 */
public void codeComplete(int offset, CompletionRequestor requestor) throws JavaModelException {
	// TODO (jerome) - Missing implementation
	throw new RuntimeException("Not implemented yet");  //$NON-NLS-1$
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
 */
public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner wcowner) throws JavaModelException {
	// TODO (jerome) - Missing implementation
	throw new RuntimeException("Not implemented yet");  //$NON-NLS-1$
}

/**
 * @see ICodeAssist#codeSelect(int, int)
 */
public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
	return codeSelect(offset, length, DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see ICodeAssist#codeSelect(int, int, WorkingCopyOwner)
 */
public IJavaElement[] codeSelect(int offset, int length, WorkingCopyOwner workingCopyOwner) throws JavaModelException {
	return super.codeSelect(this, offset, length, workingCopyOwner);
}
/**
 * @see IWorkingCopy#commit(boolean, IProgressMonitor)
 * @deprecated
 */
public void commit(boolean force, IProgressMonitor monitor) throws JavaModelException {
	commitWorkingCopy(force, monitor);
}
/**
 * @see ICompilationUnit#commitWorkingCopy(boolean, IProgressMonitor)
 */
public void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws JavaModelException {
	CommitWorkingCopyOperation op= new CommitWorkingCopyOperation(this, force);
	op.runOperation(monitor);
}
/**
 * @see ISourceManipulation#copy(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer")); //$NON-NLS-1$
	}
	IJavaElement[] elements = new IJavaElement[] {this};
	IJavaElement[] containers = new IJavaElement[] {container};
	String[] renamings = null;
	if (rename != null) {
		renamings = new String[] {rename};
	}
	getJavaModel().copy(elements, containers, null, renamings, force, monitor);
}
/**
 * Returns a new element info for this element.
 */
protected Object createElementInfo() {
	return new CompilationUnitElementInfo();
}
/**
 * @see ICompilationUnit#createImport(String, IJavaElement, IProgressMonitor)
 */
public IImportDeclaration createImport(String importName, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
	return createImport(importName, sibling, Flags.AccDefault, monitor);
}

/**
 * @see ICompilationUnit#createImport(String, IJavaElement, int, IProgressMonitor)
 * @since 3.0
 */
public IImportDeclaration createImport(String importName, IJavaElement sibling, int flags, IProgressMonitor monitor) throws JavaModelException {
	// TODO (jerome) - consult flags to create static imports
	CreateImportOperation op = new CreateImportOperation(importName, this);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	op.runOperation(monitor);
	return getImport(importName);
}

/**
 * @see ICompilationUnit#createPackageDeclaration(String, IProgressMonitor)
 */
public IPackageDeclaration createPackageDeclaration(String pkg, IProgressMonitor monitor) throws JavaModelException {
	
	CreatePackageDeclarationOperation op= new CreatePackageDeclarationOperation(pkg, this);
	op.runOperation(monitor);
	return getPackageDeclaration(pkg);
}
/**
 * @see ICompilationUnit#createType(String, IJavaElement, boolean, IProgressMonitor)
 */
public IType createType(String content, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (!exists()) {
		//autogenerate this compilation unit
		IPackageFragment pkg = (IPackageFragment) getParent();
		String source = ""; //$NON-NLS-1$
		if (pkg.getElementName().length() > 0) {
			//not the default package...add the package declaration
			source = "package " + pkg.getElementName() + ";"  + org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR + org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
		}
		CreateCompilationUnitOperation op = new CreateCompilationUnitOperation(pkg, this.name, source, force);
		op.runOperation(monitor);
	}
	CreateTypeOperation op = new CreateTypeOperation(this, content, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	op.runOperation(monitor);
	return (IType) op.getResultElements()[0];
}
/**
 * @see ISourceManipulation#delete(boolean, IProgressMonitor)
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements= new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
/**
 * @see IWorkingCopy#destroy()
 * @deprecated
 */
public void destroy() {
	try {
		discardWorkingCopy();
	} catch (JavaModelException e) {
		e.printStackTrace();
	}
}
/*
 * @see ICompilationUnit#discardWorkingCopy
 */
public void discardWorkingCopy() throws JavaModelException {
	// discard working copy and its children
	DiscardWorkingCopyOperation op = new DiscardWorkingCopyOperation(this);
	op.runOperation(null);
}
/**
 * Returns true if this handle represents the same Java element
 * as the given handle.
 *
 * @see Object#equals(java.lang.Object)
 */
public boolean equals(Object obj) {
	if (!(obj instanceof CompilationUnit)) return false;
	CompilationUnit other = (CompilationUnit)obj;
	return this.owner.equals(other.owner) && super.equals(obj);
}
/**
 * @see JavaElement#equalsDOMNode(IDOMNode)
 */
protected boolean equalsDOMNode(IDOMNode node) {
	String elementName = getElementName();
	if (node.getNodeType() == IDOMNode.COMPILATION_UNIT && elementName != null ) {
		String nodeName = node.getName();
		if (nodeName == null) return false;		
		if (elementName.equals(nodeName)) {
			return true;
		} else {
			try {
				// iterate through all the types inside the receiver and see if one of them can fit
				IType[] types = getTypes();
				String typeNodeName = nodeName.substring(0, nodeName.indexOf(SUFFIX_STRING_java));
				for (int i = 0, max = types.length; i < max; i++) {
					if (types[i].getElementName().equals(typeNodeName)) {
						return true;
					}
				}
			} catch (JavaModelException e) {
				return false;
			}
		}
	}
	return false;
}
public boolean exists() {
	// working copy always exists in the model until it is gotten rid of
	if (getPerWorkingCopyInfo() != null) return true;	
	
	// if not a working copy, it exists only if it is a primary compilation unit
	return isPrimary() && super.exists() && isValidCompilationUnit();
}
/**
 * @see IWorkingCopy#findElements(IJavaElement)
 */
public IJavaElement[] findElements(IJavaElement element) {
	ArrayList children = new ArrayList();
	while (element != null && element.getElementType() != IJavaElement.COMPILATION_UNIT) {
		children.add(element);
		element = element.getParent();
	}
	if (element == null) return null;
	IJavaElement currentElement = this;
	for (int i = children.size()-1; i >= 0; i--) {
		JavaElement child = (JavaElement)children.get(i);
		switch (child.getElementType()) {
			case IJavaElement.PACKAGE_DECLARATION:
				currentElement = ((ICompilationUnit)currentElement).getPackageDeclaration(child.getElementName());
				break;
			case IJavaElement.IMPORT_CONTAINER:
				currentElement = ((ICompilationUnit)currentElement).getImportContainer();
				break;
			case IJavaElement.IMPORT_DECLARATION:
				currentElement = ((IImportContainer)currentElement).getImport(child.getElementName());
				break;
			case IJavaElement.TYPE:
				switch (currentElement.getElementType()) {
					case IJavaElement.COMPILATION_UNIT:
						currentElement = ((ICompilationUnit)currentElement).getType(child.getElementName());
						break;
					case IJavaElement.TYPE:
						currentElement = ((IType)currentElement).getType(child.getElementName());
						break;
					case IJavaElement.FIELD:
					case IJavaElement.INITIALIZER:
					case IJavaElement.METHOD:
						currentElement =  ((IMember)currentElement).getType(child.getElementName(), child.occurrenceCount);
						break;
				}
				break;
			case IJavaElement.INITIALIZER:
				currentElement = ((IType)currentElement).getInitializer(child.occurrenceCount);
				break;
			case IJavaElement.FIELD:
				currentElement = ((IType)currentElement).getField(child.getElementName());
				break;
			case IJavaElement.METHOD:
				currentElement = ((IType)currentElement).getMethod(child.getElementName(), ((IMethod)child).getParameterTypes());
				break;
		}
		
	}
	if (currentElement != null && currentElement.exists()) {
		return new IJavaElement[] {currentElement};
	} else {
		return null;
	}
}
/**
 * @see IWorkingCopy#findPrimaryType()
 */
public IType findPrimaryType() {
	String typeName = Signature.getQualifier(this.getElementName());
	IType primaryType= this.getType(typeName);
	if (primaryType.exists()) {
		return primaryType;
	}
	return null;
}

/**
 * @see IWorkingCopy#findSharedWorkingCopy(IBufferFactory)
 * @deprecated
 */
public IJavaElement findSharedWorkingCopy(IBufferFactory factory) {

	// if factory is null, default factory must be used
	if (factory == null) factory = this.getBufferManager().getDefaultBufferFactory();
	
	return findWorkingCopy(BufferFactoryWrapper.create(factory));
}

/**
 * @see ICompilationUnit#findWorkingCopy(WorkingCopyOwner)
 */
public ICompilationUnit findWorkingCopy(WorkingCopyOwner workingCopyOwner) {
	CompilationUnit cu = new CompilationUnit((PackageFragment)this.parent, getElementName(), workingCopyOwner);
	if (workingCopyOwner == DefaultWorkingCopyOwner.PRIMARY) {
		return cu;
	} else {
		// must be a working copy
		JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = cu.getPerWorkingCopyInfo();
		if (perWorkingCopyInfo != null) {
			return perWorkingCopyInfo.getWorkingCopy();
		} else {
			return null;
		}
	}
}
/**
 * @see ICompilationUnit#getAllTypes()
 */
public IType[] getAllTypes() throws JavaModelException {
	IJavaElement[] types = getTypes();
	int i;
	ArrayList allTypes = new ArrayList(types.length);
	ArrayList typesToTraverse = new ArrayList(types.length);
	for (i = 0; i < types.length; i++) {
		typesToTraverse.add(types[i]);
	}
	while (!typesToTraverse.isEmpty()) {
		IType type = (IType) typesToTraverse.get(0);
		typesToTraverse.remove(type);
		allTypes.add(type);
		types = type.getTypes();
		for (i = 0; i < types.length; i++) {
			typesToTraverse.add(types[i]);
		}
	} 
	IType[] arrayOfAllTypes = new IType[allTypes.size()];
	allTypes.toArray(arrayOfAllTypes);
	return arrayOfAllTypes;
}
/**
 * @see IMember#getCompilationUnit()
 */
public ICompilationUnit getCompilationUnit() {
	return this;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getContents()
 */
public char[] getContents() {
	try {
		IBuffer buffer = this.getBuffer();
		return buffer == null ? null : buffer.getCharacters();
	} catch (JavaModelException e) {
		return CharOperation.NO_CHAR;
	}
}
/**
 * A compilation unit has a corresponding resource unless it is contained
 * in a jar.
 *
 * @see IJavaElement#getCorrespondingResource()
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
 * @see ICompilationUnit#getElementAt(int)
 */
public IJavaElement getElementAt(int position) throws JavaModelException {

	IJavaElement e= getSourceElementAt(position);
	if (e == this) {
		return null;
	} else {
		return e;
	}
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return COMPILATION_UNIT;
}
public char[] getFileName(){
	return getElementName().toCharArray();
}
/*
 * @see JavaElement
 */
public IJavaElement getHandleFromMemento(String token, StringTokenizer memento, WorkingCopyOwner workingCopyOwner) {
	switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
		case JEM_IMPORTDECLARATION:
			JavaElement container = (JavaElement)getImportContainer();
			return container.getHandleFromMemento(token, memento, workingCopyOwner);
		case JEM_PACKAGEDECLARATION:
			String pkgName = memento.nextToken();
			JavaElement pkgDecl = (JavaElement)getPackageDeclaration(pkgName);
			return pkgDecl.getHandleFromMemento(memento, workingCopyOwner);
		case JEM_TYPE:
			String typeName = memento.nextToken();
			JavaElement type = (JavaElement)getType(typeName);
			return type.getHandleFromMemento(memento, workingCopyOwner);
	}
	return null;
}
/**
 * @see JavaElement#getHandleMementoDelimiter()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_COMPILATIONUNIT;
}
/**
 * @see ICompilationUnit#getImport(String)
 */
public IImportDeclaration getImport(String importName) {
	return new ImportDeclaration((ImportContainer)getImportContainer(), importName);
}
/**
 * @see ICompilationUnit#getImportContainer()
 */
public IImportContainer getImportContainer() {
	return new ImportContainer(this);
}


/**
 * @see ICompilationUnit#getImports()
 */
public IImportDeclaration[] getImports() throws JavaModelException {
	IImportContainer container= getImportContainer();
	if (container.exists()) {
		IJavaElement[] elements= container.getChildren();
		IImportDeclaration[] imprts= new IImportDeclaration[elements.length];
		System.arraycopy(elements, 0, imprts, 0, elements.length);
		return imprts;
	} else if (!exists()) {
			throw newNotPresentException();
	} else {
		return new IImportDeclaration[0];
	}

}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getMainTypeName()
 */
public char[] getMainTypeName(){
	String elementName = getElementName();
	//remove the .java
	elementName = elementName.substring(0, elementName.length() - 5);
	return elementName.toCharArray();
}
/**
 * @see IWorkingCopy#getOriginal(IJavaElement)
 * @deprecated
 */
public IJavaElement getOriginal(IJavaElement workingCopyElement) {
	// backward compatibility
	if (!isWorkingCopy()) return null;
	CompilationUnit cu = (CompilationUnit)workingCopyElement.getAncestor(COMPILATION_UNIT);
	if (cu == null || !this.owner.equals(cu.owner)) {
		return null;
	}
	
	return workingCopyElement.getPrimaryElement();
}
/**
 * @see IWorkingCopy#getOriginalElement()
 * @deprecated
 */
public IJavaElement getOriginalElement() {
	// backward compatibility
	if (!isWorkingCopy()) return null;
	
	return getPrimaryElement();
}
/*
 * @see ICompilationUnit#getOwner()
 */
public WorkingCopyOwner getOwner() {
	return isPrimary() || !isWorkingCopy() ? null : this.owner;
}
/**
 * @see ICompilationUnit#getPackageDeclaration(String)
 */
public IPackageDeclaration getPackageDeclaration(String pkg) {
	return new PackageDeclaration(this, pkg);
}
/**
 * @see ICompilationUnit#getPackageDeclarations()
 */
public IPackageDeclaration[] getPackageDeclarations() throws JavaModelException {
	ArrayList list = getChildrenOfType(PACKAGE_DECLARATION);
	IPackageDeclaration[] array= new IPackageDeclaration[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getPackageName()
 */
public char[][] getPackageName() {
	return null;
}
/**
 * @see IJavaElement#getPath()
 */
public IPath getPath() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getPath();
	} else {
		return this.getParent().getPath().append(this.getElementName());
	}
}
/*
 * Returns the per working copy info for the receiver, or null if none exist.
 * Note: the use count of the per working copy info is NOT incremented.
 */
public JavaModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
	return JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(this, false/*don't create*/, false/*don't record usage*/, null/*no problem requestor needed*/);
}
/*
 * @see ICompilationUnit#getPrimary()
 */
public ICompilationUnit getPrimary() {
	return (ICompilationUnit)getPrimaryElement(true);
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner && isPrimary()) return this;
	return new CompilationUnit((PackageFragment)getParent(), getElementName(), DefaultWorkingCopyOwner.PRIMARY);
}
/**
 * @see IJavaElement#getResource()
 */
public IResource getResource() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getResource();
	} else {
		return ((IContainer)this.getParent().getResource()).getFile(new Path(this.getElementName()));
	}
}
/**
 * @see ISourceReference#getSource()
 */
public String getSource() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer == null) return ""; //$NON-NLS-1$
	return buffer.getContents();
}
/**
 * @see ISourceReference#getSourceRange()
 */
public ISourceRange getSourceRange() throws JavaModelException {
	return ((CompilationUnitElementInfo) getElementInfo()).getSourceRange();
}
/**
 * @see ICompilationUnit#getType(String)
 */
public IType getType(String typeName) {
	return new SourceType(this, typeName);
}
/**
 * @see ICompilationUnit#getTypes()
 */
public IType[] getTypes() throws JavaModelException {
	ArrayList list = getChildrenOfType(TYPE);
	IType[] array= new IType[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IJavaElement
 */
public IResource getUnderlyingResource() throws JavaModelException {
	if (isWorkingCopy() && !isPrimary()) return null;
	return super.getUnderlyingResource();
}
/**
 * @see IWorkingCopy#getSharedWorkingCopy(IProgressMonitor, IBufferFactory, IProblemRequestor)
 * @deprecated
 */
public IJavaElement getSharedWorkingCopy(IProgressMonitor pm, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	
	// if factory is null, default factory must be used
	if (factory == null) factory = this.getBufferManager().getDefaultBufferFactory();
	
	return getWorkingCopy(BufferFactoryWrapper.create(factory), problemRequestor, pm);
}
/**
 * @see IWorkingCopy#getWorkingCopy()
 * @deprecated
 */
public IJavaElement getWorkingCopy() throws JavaModelException {
	return getWorkingCopy(null);
}
/**
 * @see ICompilationUnit#getWorkingCopy(IProgressMonitor)
 */
public ICompilationUnit getWorkingCopy(IProgressMonitor monitor) throws JavaModelException {
	return getWorkingCopy(new WorkingCopyOwner() {/*non shared working copy*/}, null/*no problem requestor*/, monitor);
}
/**
 * @see IWorkingCopy#getWorkingCopy(IProgressMonitor, IBufferFactory, IProblemRequestor)
 * @deprecated
 */
public IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	return getWorkingCopy(BufferFactoryWrapper.create(factory), problemRequestor, monitor);
}
/**
 * @see IWorkingCopy#getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)
 */
public ICompilationUnit getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
	if (!isPrimary()) return this;
	
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	
	CompilationUnit workingCopy = new CompilationUnit((PackageFragment)getParent(), getElementName(), workingCopyOwner);
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = 
		manager.getPerWorkingCopyInfo(workingCopy, false/*don't create*/, true/*record usage*/, null/*not used since don't create*/);
	if (perWorkingCopyInfo != null) {
		return perWorkingCopyInfo.getWorkingCopy(); // return existing handle instead of the one created above
	}
	BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(workingCopy, problemRequestor);
	op.runOperation(monitor);
	return workingCopy;
}
/**
 * @see Openable#hasBuffer()
 */
protected boolean hasBuffer() {
	return true;
}
/*
 * @see ICompilationUnit#hasResourceChanged()
 */
public boolean hasResourceChanged() {
	if (!isWorkingCopy()) return false;
	
	// if resource got deleted, then #getModificationStamp() will answer IResource.NULL_STAMP, which is always different from the cached
	// timestamp
	Object info = JavaModelManager.getJavaModelManager().getInfo(this);
	if (info == null) return false;
	return ((CompilationUnitElementInfo)info).timestamp != getResource().getModificationStamp();
}
/**
 * @see IWorkingCopy#isBasedOn(IResource)
 * @deprecated
 */
public boolean isBasedOn(IResource resource) {
	if (!isWorkingCopy()) return false;
	if (!getResource().equals(resource)) return false;
	return !hasResourceChanged();
}
/**
 * @see IOpenable#isConsistent()
 */
public boolean isConsistent() {
	return JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers().get(this) == null;
}
/**
 * 
 * @see IOpenable
 */
public boolean isOpen() {
	Object info = JavaModelManager.getJavaModelManager().getInfo(this);
	return info != null && ((CompilationUnitElementInfo)info).isOpen();
}
public boolean isPrimary() {
	return this.owner == DefaultWorkingCopyOwner.PRIMARY;
}
/**
 * @see Openable#isSourceElement()
 */
protected boolean isSourceElement() {
	return true;
}
protected boolean isValidCompilationUnit() {
	IPackageFragmentRoot root = getPackageFragmentRoot();
	try {
		if (root.getKind() != IPackageFragmentRoot.K_SOURCE) return false;
	} catch (JavaModelException e) {
		return false;
	}
	IResource resource = getResource();
	if (resource != null) {
		char[][] exclusionPatterns = ((PackageFragmentRoot)root).fullExclusionPatternChars();
		if (Util.isExcluded(resource, exclusionPatterns)) return false;
	}
	if (!Util.isValidCompilationUnitName(getElementName())) return false;
	return true;
}
/*
 * @see ICompilationUnit#isWorkingCopy()
 */
public boolean isWorkingCopy() {
	// For backward compatibility, non primary working copies are always returning true; in removal
	// delta, clients can still check that element was a working copy before being discarded.
	return !isPrimary() || getPerWorkingCopyInfo() != null;
}
/**
 * @see IOpenable#makeConsistent(IProgressMonitor)
 */
public void makeConsistent(IProgressMonitor monitor) throws JavaModelException {
	makeConsistent(false/*don't create AST*/, monitor);
}
public org.eclipse.jdt.core.dom.CompilationUnit makeConsistent(boolean createAST, IProgressMonitor monitor) throws JavaModelException {
	if (isConsistent()) return null;
		
	// close
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	// working copy: remove info from cache, but keep buffer open
	manager.removeInfoAndChildren(this);
	
	// create a new info and make it the current info
	if (createAST) {
		ASTHolderCUInfo info = new ASTHolderCUInfo();
		openWhenClosed(info, monitor);
		org.eclipse.jdt.core.dom.CompilationUnit result = info.ast;
		info.ast = null;
		return result;
	} else {
		openWhenClosed(createElementInfo(), monitor);
		return null;
	}
}
/**
 * @see ISourceManipulation#move(IJavaElement, IJavaElement, String, boolean, IProgressMonitor)
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullContainer")); //$NON-NLS-1$
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] containers= new IJavaElement[] {container};
	
	String[] renamings= null;
	if (rename != null) {
		renamings= new String[] {rename};
	}
	getJavaModel().move(elements, containers, null, renamings, force, monitor);
}

/**
 * @see Openable#openBuffer(IProgressMonitor)
 */
protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws JavaModelException {

	// create buffer
	boolean isWorkingCopy = isWorkingCopy();
	IBuffer buffer = 
		isWorkingCopy 
			? this.owner.createBuffer(this) 
			: BufferManager.getDefaultBufferManager().createBuffer(this);
	if (buffer == null) return null;
	
	// set the buffer source
	if (buffer.getCharacters() == null) {
		if (isWorkingCopy) {
			ICompilationUnit original;
			if (!isPrimary() 
					&& (original = new CompilationUnit((PackageFragment)getParent(), getElementName(), DefaultWorkingCopyOwner.PRIMARY)).isOpen()) {
				buffer.setContents(original.getSource());
			} else {
				IFile file = (IFile)getResource();
				if (file == null || !file.exists()) {
					// initialize buffer with empty contents
					buffer.setContents(CharOperation.NO_CHAR);
				} else {
					buffer.setContents(Util.getResourceContentsAsCharArray(file));
				}
			}
		} else {
			IFile file = (IFile)this.getResource();
			if (file == null || !file.exists()) throw newNotPresentException();
			buffer.setContents(Util.getResourceContentsAsCharArray(file));
		}
	}

	// add buffer to buffer cache
	BufferManager bufManager = getBufferManager();
	bufManager.addBuffer(buffer);
			
	// listen to buffer changes
	buffer.addBufferChangedListener(this);
	
	return buffer;
}
/*
 * @see Openable#openParent
 */
protected void openParent(Object childInfo, HashMap newElements, IProgressMonitor pm) throws JavaModelException {
	try {
		super.openParent(childInfo, newElements, pm);
	} catch(JavaModelException e){
		// allow parent to not exist for working copies defined outside classpath
		if (!isWorkingCopy() && !e.isDoesNotExist()){ 
			throw e;
		}
	}
}
/**
 * @see ICompilationUnit#reconcile()
 * @deprecated
 */
public IMarker[] reconcile() throws JavaModelException {
	reconcile(false/*don't force problem detection*/, null/*use primary owner*/, null/*no progress monitor*/);
	return null;
}
/**
 * @see ICompilationUnit#reconcile(boolean, IProgressMonitor)
 */
public void reconcile(boolean forceProblemDetection, IProgressMonitor monitor) throws JavaModelException {
	reconcile(forceProblemDetection, null/*use primary owner*/, monitor);
}
/**
 * @see ICompilationUnit#reconcile(boolean, boolean, WorkingCopyOwner, IProgressMonitor)
 */
public org.eclipse.jdt.core.dom.CompilationUnit reconcile(
	boolean createAST,
	boolean forceProblemDetection,
	WorkingCopyOwner workingCopyOwner,
	IProgressMonitor monitor)
	throws JavaModelException {
	
	if (!isWorkingCopy()) return null; // Reconciling is not supported on non working copies
	if (workingCopyOwner == null) workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
	
	ReconcileWorkingCopyOperation op = new ReconcileWorkingCopyOperation(this, createAST, forceProblemDetection, workingCopyOwner);
	op.runOperation(monitor);
	return op.ast;
}
/**
 * @see ICompilationUnit#reconcile(boolean, WorkingCopyOwner, IProgressMonitor)
 */
public void reconcile(
	boolean forceProblemDetection,
	WorkingCopyOwner workingCopyOwner,
	IProgressMonitor monitor)
	throws JavaModelException {

	reconcile(false/*don't create AST*/, forceProblemDetection, workingCopyOwner, monitor);
}
/**
 * @see ISourceManipulation#rename(String, boolean, IProgressMonitor)
 */
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (newName == null) {
		throw new IllegalArgumentException(Util.bind("operation.nullName")); //$NON-NLS-1$
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {this.getParent()};
	String[] renamings= new String[] {newName};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
/*
 * @see ICompilationUnit
 */
public void restore() throws JavaModelException {

	if (!isWorkingCopy()) return;

	CompilationUnit original = (CompilationUnit) getOriginalElement();
	IBuffer buffer = this.getBuffer();
	if (buffer == null) return;
	buffer.setContents(original.getContents());
	updateTimeStamp(original);
	makeConsistent(null);
}
/**
 * @see IOpenable
 */
public void save(IProgressMonitor pm, boolean force) throws JavaModelException {
	if (isWorkingCopy()) {
		// no need to save the buffer for a working copy (this is a noop)
		reconcile();   // not simply makeConsistent, also computes fine-grain deltas
								// in case the working copy is being reconciled already (if not it would miss
								// one iteration of deltas).
	} else {		
		super.save(pm, force);
	}
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	if (!isPrimary()) {
		buffer.append(this.tabString(tab));
		buffer.append("[Working copy] "); //$NON-NLS-1$
		buffer.append(getElementName());
	} else {
		if (isWorkingCopy()) {
			buffer.append(this.tabString(tab));
			buffer.append("[Working copy] "); //$NON-NLS-1$
			buffer.append(getElementName());
			if (info == null) {
				buffer.append(" (not open)"); //$NON-NLS-1$
			}
		} else {
			super.toStringInfo(tab, buffer, info);
		}
	}
}
/*
 * Assume that this is a working copy
 */
protected void updateTimeStamp(CompilationUnit original) throws JavaModelException {
	long timeStamp =
		((IFile) original.getResource()).getModificationStamp();
	if (timeStamp == IResource.NULL_STAMP) {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE));
	}
	((CompilationUnitElementInfo) getElementInfo()).timestamp = timeStamp;
}

}
