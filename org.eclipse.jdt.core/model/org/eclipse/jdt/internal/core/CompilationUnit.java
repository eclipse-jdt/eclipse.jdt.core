package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.codeassist.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.core.lookup.*;

import java.util.*;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * @see ICompilationUnit
 */

public class CompilationUnit extends Openable implements ICompilationUnit, org.eclipse.jdt.internal.compiler.env.ICompilationUnit {

/**
 * Constructs a handle to a compilation unit with the given name in the
 * specified package.
 *
 * @exception IllegalArgumentException if the name of the compilation unit
 * does not end with ".java"
 */
protected CompilationUnit(IPackageFragment parent, String name) {
	super(COMPILATION_UNIT, parent, name);
	if (!Util.isJavaFileName(name)) {
		throw new IllegalArgumentException("compilation unit name must end with .java");
	}
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
public void accept(IAbstractSyntaxTreeVisitor visitor) throws JavaModelException {
	CompilationUnitVisitor.visit(this, visitor);
}
/**
 * @see ICodeAssist
 */
public void codeComplete(int offset, ICodeCompletionRequestor requestor) throws JavaModelException {
	codeComplete(this, isWorkingCopy() ? (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) getOriginalElement() : this, offset, requestor);
}
/**
 * @see ICodeResolve
 */
public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
	return super.codeSelect(this, offset, length);
}
/**
 * @see IWorkingCopy
 */
public void commit(boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
}
/**
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException("container cannot be null");
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
protected OpenableElementInfo createElementInfo() {
	return new CompilationUnitElementInfo();
}
/**
 * @see ICompilationUnit
 */
public IImportDeclaration createImport(String name, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
	CreateImportOperation op = new CreateImportOperation(name, this);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
	return getImport(name);
}
/**
 * @see ICompilationUnit
 */
public IPackageDeclaration createPackageDeclaration(String name, IProgressMonitor monitor) throws JavaModelException {
	
	CreatePackageDeclarationOperation op= new CreatePackageDeclarationOperation(name, this);
	runOperation(op, monitor);
	return getPackageDeclaration(name);
}
/**
 * @see ICompilationUnit
 */
public IType createType(String content, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (!exists()) {
		//autogenerate this compilation unit
		IPackageFragment pkg = (IPackageFragment) getParent();
		String source = "";
		if (pkg.getElementName().length() > 0) {
			//not the default package...add the package declaration
			source = "package " + pkg.getElementName() + ";" + JavaModelManager.LINE_SEPARATOR;
		}
		CreateCompilationUnitOperation op = new CreateCompilationUnitOperation(pkg, fName, source, force);
		runOperation(op, monitor);
	}
	CreateTypeOperation op = new CreateTypeOperation(this, content, force);
	if (sibling != null) {
		op.createBefore(sibling);
	}
	runOperation(op, monitor);
	return (IType) op.getResultElements()[0];
}
/**
 * @see ISourceManipulation
 */
public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	IJavaElement[] elements= new IJavaElement[] {this};
	getJavaModel().delete(elements, force, monitor);
}
/**
 * This is not a working copy, do nothing.
 *
 * @see IWorkingCopy
 */
public void destroy() {
}
/**
 * Returns true if this handle represents the same Java element
 * as the given handle.
 *
 * <p>Compilation units must also check working copy state;
 *
 * @see Object#equals
 */
public boolean equals(Object o) {
	return super.equals(o) && !((ICompilationUnit)o).isWorkingCopy();
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	String name = getElementName();
	if (node.getNodeType() == IDOMNode.COMPILATION_UNIT && name != null ) {
		String nodeName = node.getName();
		if (nodeName == null) return false;		
		if (name.equals(nodeName)) {
			return true;
		} else {
			// iterate through all the types inside the receiver and see if one of them can fit
			IType[] types = getTypes();
			String typeNodeName = nodeName.substring(0, nodeName.indexOf(".java"));
			for (int i = 0, max = types.length; i < max; i++) {
				if (types[i].getElementName().equals(typeNodeName)) {
					return true;
				}
			}
		}
	}
	return false;
}
/**
 * @see Openable
 */
protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Hashtable newElements, IResource underlyingResource) throws JavaModelException {

	if (getParent() instanceof JarPackageFragment) {
		// ignore .java files in jar
		throw newNotPresentException();
	} else {
		// put the info now, because getting the contents requires it
		fgJavaModelManager.putInfo(this, info);
		CompilationUnitElementInfo unitInfo = (CompilationUnitElementInfo) info;

		// generate structure
		CompilationUnitStructureRequestor requestor = new CompilationUnitStructureRequestor(this, unitInfo, newElements);
		IProblemFactory factory = new DefaultProblemFactory();
		SourceElementParser parser = new SourceElementParser(requestor, factory);
		parser.parseCompilationUnit(this, !isWorkingCopy());
		if (isWorkingCopy()) {
			// remember problems
			Vector problems = requestor.fProblems;
			if (problems != null) {
				problems.copyInto(((WorkingCopyElementInfo)unitInfo).problems = new IProblem[problems.size()]);
			}
			
			CompilationUnit original = (CompilationUnit) getOriginalElement();
			unitInfo.fTimestamp = ((IFile) original.getUnderlyingResource()).getModificationStamp();
			if(unitInfo.fTimestamp == IResource.NULL_STAMP){
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE));
			}
		}
		return unitInfo.isStructureKnown();
	}
}
/**
 * @see ICompilationUnit
 */
public IType[] getAllTypes() throws JavaModelException {
	IJavaElement[] types = getTypes();
	int i;
	Vector allTypes = new Vector(types.length);
	Vector typesToTraverse = new Vector(types.length);
	for (i = 0; i < types.length; i++) {
		typesToTraverse.addElement(types[i]);
	}
	while (!typesToTraverse.isEmpty()) {
		IType type = (IType) typesToTraverse.elementAt(0);
		typesToTraverse.removeElement(type);
		allTypes.addElement(type);
		types = type.getTypes();
		for (i = 0; i < types.length; i++) {
			typesToTraverse.addElement(types[i]);
		}
	}
	allTypes.trimToSize();
	IType[] arrayOfAllTypes = new IType[allTypes.size()];
	allTypes.copyInto(arrayOfAllTypes);
	return arrayOfAllTypes;
}
/**
 * @see IMember
 */
public ICompilationUnit getCompilationUnit() {
	return this;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.api.ICompilationUnit
 */
public char[] getContents() {
	try {
		return getBuffer().getCharacters();
	} catch (NullPointerException e) { // buffer could be null
		return new char[0];
	} catch (JavaModelException e) {
		return new char[0];
	}
}
/**
 * A compilation unit has a corresponding resource unless it is contained
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
 * @see ICompilationUnit
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
 * @see org.eclipse.jdt.internal.compiler.env.api.ICompilationUnit
 */
public char[] getFileName(){
	return getElementName().toCharArray();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_COMPILATIONUNIT;
}
/**
 * @see ICompilationUnit#getImport
 */
public IImportDeclaration getImport(String name) {
	return new ImportDeclaration(getImportContainer(), name);
}
/**
 * @see ICompilationUnit
 */
public IImportContainer getImportContainer() {
	return new ImportContainer(this);
}
/**
 * @see ICompilationUnit
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
 * @see org.eclipse.jdt.internal.compiler.env.api.ICompilationUnit
 */
public char[] getMainTypeName(){
	String name= getElementName();
	//remove the .java
	name= name.substring(0, name.length() - 5);
	return name.toCharArray();
}
/**
 * Returns <code>null</code>, this is not a working copy.
 *
 * @see IWorkingCopy
 */
public IJavaElement getOriginal(IJavaElement workingCopyElement) {
	return null;
}
/**
 * Returns <code>null</code>, this is not a working copy.
 *
 * @see IWorkingCopy
 */
public IJavaElement getOriginalElement() {
	return null;
}
/**
 * @see ICompilationUnit#getPackageDeclaration(String)
 */
public IPackageDeclaration getPackageDeclaration(String name) {
	return new PackageDeclaration(this, name);
}
/**
 * @see ICompilationUnit
 */
public IPackageDeclaration[] getPackageDeclarations() throws JavaModelException {
	Vector v= getChildrenOfType(PACKAGE_DECLARATION);
	IPackageDeclaration[] array= new IPackageDeclaration[v.size()];
	v.copyInto(array);
	return array;
}
/**
 * Returns the reference information for this compilation unit
 */
public ReferenceInfo getReferenceInfo() throws JavaModelException {
	return ((CompilationUnitElementInfo)getElementInfo()).getReferenceInfo();
}
/**
 * @see ISourceReference
 */
public String getSource() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer == null) return "";
	return buffer.getContents();
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	return ((CompilationUnitElementInfo) getElementInfo()).getSourceRange();
}
/**
 * @see ICompilationUnit
 */
public IType getType(String name) {
	return new SourceType(this, name);
}
/**
 * @see ICompilationUnit
 */
public IType[] getTypes() throws JavaModelException {
	Vector v= getChildrenOfType(TYPE);
	IType[] array= new IType[v.size()];
	v.copyInto(array);
	return array;
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy() throws JavaModelException {
	WorkingCopy workingCopy= new WorkingCopy((IPackageFragment)getParent(), getElementName());
	// open the working copy now to ensure contents are that of the current state of this element
	workingCopy.open(null);
	return workingCopy;
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
 * Returns false, this is not a working copy.
 *
 * @see IWorkingCopy
 */
public boolean isBasedOn(IResource resource) {
	return false;
}
/**
 * @see IOpenable
 */
public boolean isConsistent() throws JavaModelException {
	return fgJavaModelManager.getElementsOutOfSynchWithBuffers().get(this) == null;
}
/**
 * @see Openable
 */
protected boolean isSourceElement() {
	return true;
}
/**
 * @see IWorkingCopy
 */
public boolean isWorkingCopy() {
	return false;
}
/**
 * @see IOpenable
 */
public void makeConsistent(IProgressMonitor pm) throws JavaModelException {
	if (!isConsistent()) {
		// create a new info and make it the current info
		OpenableElementInfo info = createElementInfo();
		buildStructure(info, pm);
	}
}
/**
 * @see ISourceManipulation
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (container == null) {
		throw new IllegalArgumentException("container cannot be null");
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
 * Changes the source end index of this element and all children (following
 * <code>child</code>). 
 */
public void offsetSourceEndAndChildren(int amount, IJavaElement child) {
	try {
		CompilationUnitElementInfo cuInfo = (CompilationUnitElementInfo) getElementInfo();
		cuInfo.setSourceLength(cuInfo.getSourceLength() + amount);
		IJavaElement[] children = getChildren();
		boolean afterChild = false;
		for (int i = 0; i < children.length; i++) {
			IJavaElement aChild = children[i];
			if (child == null || aChild.equals(child)) {
				afterChild = true;
			} else
				if (afterChild) {
					((JavaElement) aChild).offsetSourceRange(amount);
				}
		}
	} catch (JavaModelException npe) {
		return;
	}
}
/**
 * Changes the source indexes of this element and all children elements.
 */
public void offsetSourceRange(int amount) {
	try {
		CompilationUnitElementInfo cuInfo = (CompilationUnitElementInfo) getElementInfo();
		cuInfo.setSourceLength(cuInfo.getSourceLength() + amount);
		IJavaElement[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			IJavaElement aChild = children[i];
			((JavaElement) aChild).offsetSourceRange(amount);
		}
	} catch (JavaModelException npe) {
		return;
	}
}
/**
 * @see Openable
 */
protected IBuffer openBuffer(IProgressMonitor pm) throws JavaModelException {
	IBuffer buf = getBufferManager().openBuffer((IFile) getUnderlyingResource(), pm, this, isReadOnly());
	buf.addBufferChangedListener(this);
	return buf;
}
/**
 * @see IWorkingCopy
 */
public IMarker[] reconcile() throws JavaModelException {
	// Reconciling is not supported on non working copies
	return null;
}
/**
 * @see ISourceManipulation
 */
public void rename(String name, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (name == null) {
		throw new IllegalArgumentException("name cannot be null");
	}
	IJavaElement[] elements= new IJavaElement[] {this};
	IJavaElement[] dests= new IJavaElement[] {this.getParent()};
	String[] renamings= new String[] {name};
	getJavaModel().rename(elements, dests, renamings, force, monitor);
}
/**
 * Does nothing - this is not a working copy.
 *
 * @see IWorkingCopy
 */
public void restore () throws JavaModelException {
}
/**
 * Updates the source end index for this element.
 */
public void triggerSourceEndOffset(int amount, int nameStart, int nameEnd) {
	try {
		CompilationUnitElementInfo cuInfo = (CompilationUnitElementInfo) getRawInfo();
		cuInfo.setSourceLength(cuInfo.getSourceLength() + amount);
	} catch (JavaModelException npe) {
		return;
	}
}
/**
 * Updates the source indexes for this element.
 */
public void triggerSourceRangeOffset(int amount, int nameStart, int nameEnd) {
	triggerSourceEndOffset(amount, nameStart, nameEnd);
}
}
