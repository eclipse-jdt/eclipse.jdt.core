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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.jdom.IDOMNode;

import java.util.*;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
/**
 * @see ICompilationUnit
 */

public class CompilationUnit extends Openable implements ICompilationUnit, org.eclipse.jdt.internal.compiler.env.ICompilationUnit {
	
	public static boolean SHARED_WC_VERBOSE = false;
	public final static Object DEFAULT_FACTORY = "DEFAULT"; //$NON-NLS-1$

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
		throw new IllegalArgumentException(org.eclipse.jdt.internal.core.Util.bind("convention.unit.notJavaName")); //$NON-NLS-1$
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

protected void buildStructure(OpenableElementInfo info, IProgressMonitor pm) throws JavaModelException {

	// remove existing (old) infos
	removeInfo();

	HashMap newElements = new HashMap(11);
	info.setIsStructureKnown(generateInfos(info, pm, newElements, getUnderlyingResource()));
	fgJavaModelManager.getElementsOutOfSynchWithBuffers().remove(this);
	for (Iterator iter = newElements.keySet().iterator(); iter.hasNext();) {
		IJavaElement key = (IJavaElement) iter.next();
		Object value = newElements.get(key);
		fgJavaModelManager.putInfo(key, value);
	}

	// error detection
	IProblemRequestor problemRequestor = this.getProblemRequestor();
	if (problemRequestor != null && problemRequestor.isActive()){
		problemRequestor.beginReporting();
		CompilationUnitProblemFinder.resolve(this, problemRequestor);
		problemRequestor.endReporting();
	}
	

	// add the info for this at the end, to ensure that a getInfo cannot reply null in case the LRU cache needs
	// to be flushed. Might lead to performance issues.
	// see PR 1G2K5S7: ITPJCORE:ALL - NPE when accessing source for a binary type
	fgJavaModelManager.putInfo(this, info);	
}

/**
 * @see ICodeAssist
 */
public void codeComplete(int offset, ICompletionRequestor requestor) throws JavaModelException {
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
		String source = ""; //$NON-NLS-1$
		if (pkg.getElementName().length() > 0) {
			//not the default package...add the package declaration
			source = "package " + pkg.getElementName() + ";"  + org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR + org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
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
			String typeNodeName = nodeName.substring(0, nodeName.indexOf(".java")); //$NON-NLS-1$
			for (int i = 0, max = types.length; i < max; i++) {
				if (types[i].getElementName().equals(typeNodeName)) {
					return true;
				}
			}
		}
	}
	return false;
}
/*
 * @see IWorkingCopy
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
		IJavaElement child = (IJavaElement)children.get(i);
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
				if (currentElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
					currentElement = ((ICompilationUnit)currentElement).getType(child.getElementName());
				} else {
					currentElement = ((IType)currentElement).getType(child.getElementName());
				}
				break;
			case IJavaElement.INITIALIZER:
				currentElement = ((IType)currentElement).getInitializer(((JavaElement)child).getOccurrenceCount());
				break;
			case IJavaElement.FIELD:
				currentElement = ((IType)currentElement).getField(child.getElementName());
				break;
			case IJavaElement.METHOD:
				return ((IType)currentElement).findMethods((IMethod)child);
		}
		
	}
	if (currentElement != null && currentElement.exists()) {
		return new IJavaElement[] {currentElement};
	} else {
		return null;
	}
}
/*
 * @see IWorkingCopy
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
 * @see IWorkingCopy
 */
public IJavaElement findSharedWorkingCopy(IBufferFactory factory) {

	// In order to be shared, working copies have to denote the same compilation unit 
	// AND use the same buffer factory.
	// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
	Map sharedWorkingCopies = JavaModelManager.getJavaModelManager().sharedWorkingCopies;
	
	Map perFactoryWorkingCopies = 
		factory == null 
			?(Map) sharedWorkingCopies.get(CompilationUnit.DEFAULT_FACTORY) 
			: (Map) sharedWorkingCopies.get(factory);
	if (perFactoryWorkingCopies == null) return null;
	return (WorkingCopy)perFactoryWorkingCopies.get(this);
}

/**
 * work-around for UI dependency - TOFIX (should be removed)
 */
public IJavaElement findSharedWorkingCopy() {

	// iterate over all the working copies maps, and answer first matching working copy
	Map sharedWorkingCopies = JavaModelManager.getJavaModelManager().sharedWorkingCopies;
	for (Iterator iterator =  sharedWorkingCopies.values().iterator(); iterator.hasNext();) {
		Map perFactoryWorkingCopies = (Map) iterator.next();
		WorkingCopy copy = (WorkingCopy)perFactoryWorkingCopies.get(this);
		if (copy != null) return copy;
	}
	return null;
}

protected boolean generateInfos(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {

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
		SourceElementParser parser = new SourceElementParser(requestor, factory, new CompilerOptions(JavaCore.getOptions()));
		parser.parseCompilationUnit(this, false);
		if (isWorkingCopy()) {
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
	ArrayList list = getChildrenOfType(PACKAGE_DECLARATION);
	IPackageDeclaration[] array= new IPackageDeclaration[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.api.ICompilationUnit
 */
public char[][] getPackageName() {
	return null;
}
/*
 * @see IJavaElement
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
 * @see IJavaElement
 */
public IResource getResource() {
	PackageFragmentRoot root = this.getPackageFragmentRoot();
	if (root.isArchive()) {
		return root.getResource();
	} else {
		return ((IContainer)this.getParent().getResource()).getFile(new Path(this.getElementName()));
	}
}

/*
 * Answer requestor to notify with problems
 */
public IProblemRequestor getProblemRequestor(){
	return null;
}

/**
 * @see ISourceReference
 */
public String getSource() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer == null) return ""; //$NON-NLS-1$
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
	ArrayList list = getChildrenOfType(TYPE);
	IType[] array= new IType[list.size()];
	list.toArray(array);
	return array;
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getSharedWorkingCopy(IProgressMonitor pm, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {

	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	
	// In order to be shared, working copies have to denote the same compilation unit 
	// AND use the same buffer factory.
	// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
	Map sharedWorkingCopies = manager.sharedWorkingCopies;
	
	Map perFactoryWorkingCopies = 
		factory == null 
			?(Map) sharedWorkingCopies.get(CompilationUnit.DEFAULT_FACTORY) 
			: (Map) sharedWorkingCopies.get(factory);
	if (perFactoryWorkingCopies == null){
		perFactoryWorkingCopies = new HashMap();
		if (factory == null){
			sharedWorkingCopies.put(CompilationUnit.DEFAULT_FACTORY, perFactoryWorkingCopies); 
		} else {
			sharedWorkingCopies.put(factory, perFactoryWorkingCopies);
		}
	}
	WorkingCopy workingCopy = (WorkingCopy)perFactoryWorkingCopies.get(this);
	if (workingCopy != null) {
		workingCopy.useCount++;

		if (SHARED_WC_VERBOSE) {
			System.out.println("Incrementing use count of shared working copy " + workingCopy.toStringWithAncestors()); //$NON-NLS-1$
		}

		return workingCopy;
	} else {
		workingCopy = (WorkingCopy)this.getWorkingCopy(pm, factory, problemRequestor);
		perFactoryWorkingCopies.put(this, workingCopy);

		if (SHARED_WC_VERBOSE) {
			System.out.println("Creating shared working copy " + workingCopy.toStringWithAncestors()); //$NON-NLS-1$
		}

		// report added java delta
		JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
		delta.added(workingCopy);
		manager.fire(delta, JavaModelManager.DEFAULT_CHANGE_EVENT);

		return workingCopy;
	}
}
/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy() throws JavaModelException {
	return this.getWorkingCopy(null, null, null);
}

/**
 * @see IWorkingCopy
 */
public IJavaElement getWorkingCopy(IProgressMonitor pm, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	WorkingCopy workingCopy = new WorkingCopy((IPackageFragment)getParent(), getElementName(), factory, problemRequestor);
	// open the working copy now to ensure contents are that of the current state of this element
	workingCopy.open(pm);
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
	// create buffer
	BufferManager bufManager = getBufferManager();
	IBuffer buffer = bufManager.getDefaultBufferFactory().createBuffer(this);
	bufManager.addBuffer(buffer);
	
	// set the buffer source
	if (buffer != null && buffer.getCharacters() == null){
		buffer.setContents(Util.getResourceContentsAsCharArray((IFile)this.getResource()));
	}
			
	// listen to buffer changes
	buffer.addBufferChangedListener(this);
	
	return buffer;
}
/*
 * @see Openable#openParent(IProgressMonitor)
 */
protected void openParent(IProgressMonitor pm) throws JavaModelException {
	try {
		super.openParent(pm);
	} catch(JavaModelException e){
		// allow parent to not exist for fake units defined outside classpath
		// will be ok for both working copies and compilation units
		if (!e.isDoesNotExist()){ 
			throw e;
		}
	}
}
/**
 *  Answers true if the parent exists (null parent is answering true)
 * 
 */
protected boolean parentExists(){
	
	return true; // tolerate units outside classpath
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
		throw new IllegalArgumentException(Util.bind("operation.nullName")); //$NON-NLS-1$
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
/**
 * @see ICodeAssist
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
				}
			}
			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
				requestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd);
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
			public void acceptLocalVariable(char[] name,char[] typePackageName,char[] typeName,int modifiers,int completionStart,int completionEnd, int relevance){
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
			public void acceptVariableName(char[] typePackageName,char[] typeName,char[] name,char[] completionName,int completionStart,int completionEnd, int relevance){
				// ignore
			}
		});
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return
		new CompilationUnit(
			(IPackageFragment)((JavaElement)fParent).rootedAt(project), 
			fName);
}

}
