package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.jdom.DOMNode;

/**
 * <p>This abstract class implements behavior common to <code>CreateElementInCUOperations</code>.
 * To create a compilation unit, or an element contained in a compilation unit, the
 * source code for the entire compilation unit is updated and saved.
 *
 * <p>The element being created can be positioned relative to an existing
 * element in the compilation unit via the methods <code>#createAfter</code>
 * and <code>#createBefore</code>. By default, the new element is positioned
 * as the last child of its parent element.
 *
 */
public abstract class CreateElementInCUOperation extends JavaModelOperation {
	/**
	 * The compilation unit DOM used for this operation
	 */
	protected IDOMCompilationUnit fCUDOM;
	/**
	 * A constant meaning to position the new element
	 * as the last child of its parent element.
	 */
	protected static final int INSERT_LAST = 1;
	/**
	 * A constant meaning to position the new element
	 * after the element defined by <code>fAnchorElement</code>.
	 */
	protected static final int INSERT_AFTER = 2;

	/**
	 * A constant meaning to position the new element
	 * before the element defined by <code>fAnchorElement</code>.
	 */
	protected static final int INSERT_BEFORE = 3;
	/**
	 * One of the position constants, describing where
	 * to position the newly created element.
	 */
	protected int fInsertionPolicy = INSERT_LAST;
	/**
	 * The element that the newly created element is
	 * positioned relative to, as described by
	 * <code>fInsertPosition</code>, or <code>null</code>
	 * if the newly created element will be positioned
	 * last.
	 */
	protected IJavaElement fAnchorElement = null;
	/**
	 * A flag indicating whether creation of a new element occurred.
	 * A request for creating a duplicate element would request in this
	 * flag being set to <code>false</code>. Ensures that no deltas are generated
	 * when creation does not occur.
	 */
	protected boolean fCreationOccurred = true;
	/**
	 * The element that is being created.
	 */
	protected DOMNode fCreatedElement;
	/**
	 * The position of the element that is being created.
	 */
	protected int fInsertionPosition = -1;
	/**
	 * The number of characters the new element replaces,
	 * or 0 if the new element is inserted,
	 * or -1 if the new element is append to the end of the CU.
	 */
	protected int fReplacementLength = -1;
/**
 * Constructs an operation that creates a Java Language Element with
 * the specified parent, contained within a compilation unit.
 */
public CreateElementInCUOperation(IJavaElement parentElement) {
	super(null, new IJavaElement[]{parentElement});
	initializeDefaultPosition();
}
/**
 * Only allow cancelling if this operation is not nested.
 */
protected void checkCanceled() {
	if (!fNested) {
		super.checkCanceled();
	}
}
/**
 * Instructs this operation to position the new element after
 * the given sibling, or to add the new element as the last child
 * of its parent if <code>null</code>.
 */
public void createAfter(IJavaElement sibling) {
	setRelativePosition(sibling, INSERT_AFTER);
}
/**
 * Instructs this operation to position the new element before
 * the given sibling, or to add the new element as the last child
 * of its parent if <code>null</code>.
 */
public void createBefore(IJavaElement sibling) {
	setRelativePosition(sibling, INSERT_BEFORE);
}
/**
 * Execute the operation - generate new source for the compilation unit
 * and save the results.
 *
 * @exception JavaModelException if the operation is unable to complete
 */
protected void executeOperation() throws JavaModelException {
	beginTask(getMainTaskName(), getMainAmountOfWork());
	JavaElementDelta delta = newJavaElementDelta();
	ICompilationUnit unit = getCompilationUnit();
	generateNewCompilationUnitDOM(unit);
	if (fCreationOccurred) {
		//a change has really occurred
		Buffer buffer = (Buffer)unit.getBuffer();
		switch (fReplacementLength) {
			case -1 : 
				// element is append at the end
				buffer.append(fCreatedElement.getCharacters(), true);
				break;
			case 0 :
				// element is inserted
				buffer.replace(fInsertionPosition, 0, fCreatedElement.getCharacters(), true);
				break;
			default :
				// element is replacing the previous one
				buffer.replace(fInsertionPosition, fReplacementLength, fCreatedElement.getCharacters(), true);
		}
		unit.save(null, false);
		worked(1);
		fResultElements = generateResultHandles();
		for (int i = 0; i < fResultElements.length; i++) {
			delta.added(fResultElements[i]);
		}
		addDelta(delta);
	}
	done();
}
/**
 * Returns a JDOM document fragment for the element being created.
 */
protected abstract IDOMNode generateElementDOM() throws JavaModelException;
/**
 * Returns the DOM with the new source to use for the given compilation unit.
 */
protected void generateNewCompilationUnitDOM(ICompilationUnit cu) throws JavaModelException {
	char[] prevSource = cu.getBuffer().getCharacters();

	// create a JDOM for the compilation unit
	fCUDOM = (new DOMFactory()).createCompilationUnit(prevSource, cu.getElementName());
	IDOMNode child = generateElementDOM();
	if (child != null) {
		insertDOMNode(fCUDOM, child);
	}
	worked(1);
}
/**
 * Creates and returns the handle for the element this operation created.
 */
protected abstract IJavaElement generateResultHandle();
/**
 * Creates and returns the handles for the elements this operation created.
 */
protected IJavaElement[] generateResultHandles() throws JavaModelException {
	return new IJavaElement[]{generateResultHandle()};
}
/**
 * Returns the compilation unit in which the new element is being created.
 */
protected ICompilationUnit getCompilationUnit() {
	return getCompilationUnitFor(getParentElement());
}
/**
 * Returns the amount of work for the main task of this operation for
 * progress reporting.
 * @see executeOperation()
 */
protected int getMainAmountOfWork(){
	return 2;
}
/**
 * Returns the name of the main task of this operation for
 * progress reporting.
 * @see executeOperation()
 */
protected abstract String getMainTaskName();
/**
 * Returns the elements created by this operation.
 */
public IJavaElement[] getResultElements() {
	return fResultElements;
}
/**
 * Sets the default position in which to create the new type
 * member. By default, the new element is positioned as the
 * last child of the parent element in which it is created.
 * Operations that require a different default position must
 * override this method.
 */
protected void initializeDefaultPosition() {

}
/**
 * Inserts the given child into the given JDOM, 
 * based on the position settings of this operation.
 *
 * @see createAfter(IJavaElement)
 * @see createBefore(IJavaElement);
 */
protected void insertDOMNode(IDOMNode parent, IDOMNode child) {
	if (fInsertionPolicy != INSERT_LAST) {
		IDOMNode sibling = ((JavaElement)fAnchorElement).findNode(fCUDOM);
		if (sibling != null && fInsertionPolicy == INSERT_AFTER) {
			sibling = sibling.getNextNode();
		}
		if (sibling != null) {
			sibling.insertSibling(child);
			fCreatedElement = (DOMNode)child;
			fInsertionPosition = ((DOMNode)sibling).getStartPosition();
			fReplacementLength = 0;
			return;
		}
	}
	//add as the last element of the parent
	parent.addChild(child);
	fCreatedElement = (DOMNode)child;
	DOMNode lastChild = (DOMNode)fCreatedElement.getPreviousNode();
	fInsertionPosition = lastChild == null ? ((DOMNode)parent).getInsertionPosition() : lastChild.getEndPosition() + 1;
	fReplacementLength = parent.getParent() == null ? -1 : 0;
}
/**
 * Sets the name of the <code>DOMNode</code> that will be used to
 * create this new element.
 * Used by the <code>CopyElementsOperation</code> for renaming.
 * Only used for <code>CreateTypeMemberOperation</code>
 */
protected void setAlteredName(String newName) {
}
/**
 * Instructs this operation to position the new element relative
 * to the given sibling, or to add the new element as the last child
 * of its parent if <code>null</code>. The <code>position</code>
 * must be one of the position constants.
 */
protected void setRelativePosition(IJavaElement sibling, int policy) throws IllegalArgumentException {
	if (sibling == null) {
		fAnchorElement = null;
		fInsertionPolicy = INSERT_LAST;
	} else {
		fAnchorElement = sibling;
		fInsertionPolicy = policy;
	}
}
/**
 * Possible failures: <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is
 * 		<code>null</code>.
 *  <li>INVALID_NAME - no name, a name was null or not a valid
 * 		import declaration name.
 *  <li>INVALID_SIBLING - the sibling provided for positioning is not valid.
 * </ul>
 * @see IJavaModelStatus
 * @see JavaNamingConventions
 */
public IJavaModelStatus verify() {
	if (getParentElement() == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	if (fAnchorElement != null) {
		IJavaElement domPresentParent = fAnchorElement.getParent();
		if (domPresentParent.getElementType() == IJavaElement.IMPORT_CONTAINER) {
			domPresentParent = domPresentParent.getParent();
		}
		if (!domPresentParent.equals(getParentElement())) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_SIBLING, fAnchorElement);
		}
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
