package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.*;

import java.util.*;

/**
 * This operation deletes a collection of elements (and
 * all of their children).
 * If an element does not exist, it is ignored.
 *
 * <p>NOTE: This operation only deletes elements contained within leaf resources -
 * i.e. elements within compilation units. To delete a compilation unit or
 * a package, etc (i.e. an actual resource), a DeleteResourcesOperation
 * should be used.
 */
public class DeleteElementsOperation extends MultiOperation {
	/**
	 * The elements this operation processes grouped by compilation unit
	 * @see processElements(). Keys are compilation units,
	 * values are <code>IRegion</code>s of elements to be processed in each
	 * compilation unit.
	 */ 
	protected Hashtable fChildrenToRemove;
	/**
	 * The <code>DOMFactory</code> used to manipulate the source code of
	 * <code>ICompilationUnit</code>s.
	 */
	protected DOMFactory fFactory;
/**
 * When executed, this operation will delete the given elements. The elements
 * to delete cannot be <code>null</code> or empty, and must be contained within a
 * compilation unit.
 */
public DeleteElementsOperation(IJavaElement[] elementsToDelete, boolean force) {
	super(elementsToDelete, force);
	fFactory = new DOMFactory();
}
/**
 * Saves the new contents of the compilation unit that has had
 * a member element deleted.
 */
protected void commitChanges(IDOMCompilationUnit cuDOM, ICompilationUnit cu) throws JavaModelException {
	// save the resources that need to be saved
	char[] newContents = cuDOM.getCharacters();
	if (newContents == null) {
		newContents = new char[0];
	}
	((Buffer)cu.getBuffer()).setContents(newContents, true);
	cu.save(getSubProgressMonitor(1), fForce);
}
/**
 * @see MultiOperation
 */
protected String getMainTaskName() {
	return "Deleting elements...";
}
/**
 * Groups the elements to be processed by their compilation unit.
 * If parent/child combinations are present, children are
 * discarded (only the parents are processed). Removes any
 * duplicates specified in elements to be processed.
 */
protected void groupElements() throws JavaModelException {
	fChildrenToRemove = new Hashtable(1);
	int uniqueCUs = 0;
	for (int i = 0, length = fElementsToProcess.length; i < length; i++) {
		IJavaElement e = fElementsToProcess[i];
		ICompilationUnit cu = getCompilationUnitFor(e);
		if (cu == null) {
			throw new JavaModelException(new JavaModelStatus(JavaModelStatus.READ_ONLY, e));
		} else {
			IRegion region = (IRegion) fChildrenToRemove.get(cu);
			if (region == null) {
				region = new Region();
				fChildrenToRemove.put(cu, region);
				uniqueCUs += 1;
			}
			region.add(e);
		}
	}
	fElementsToProcess = new IJavaElement[uniqueCUs];
	Enumeration enum = fChildrenToRemove.keys();
	int i = 0;
	while (enum.hasMoreElements()) {
		fElementsToProcess[i++] = (IJavaElement) enum.nextElement();
	}
}
/**
 * Deletes this element from its compilation unit.
 * @see MultiOperation
 */
protected void processElement(IJavaElement element) throws JavaModelException {
	ICompilationUnit cu = (ICompilationUnit) element;

	// keep track of the import statements - if all are removed, delete
	// the import container (i.e. report it in the delta)
	int numberOfImports = cu.getImports().length;

	IDOMCompilationUnit cuDOM = fFactory.createCompilationUnit(cu.getBuffer().getCharacters(), cu.getElementName());
	JavaElementDelta delta = new JavaElementDelta(cu);
	IJavaElement[] cuElements = ((IRegion) fChildrenToRemove.get(cu)).getElements();
	for (int i = 0; i < cuElements.length; i++) {
		IJavaElement e = cuElements[i];
		if (e.exists()) {
			IDOMNode node = ((JavaElement) e).findNode(cuDOM);
			// TBD
			Assert.isTrue(node != null, "Failed to locate " + e.getElementName() + " in " + cuDOM.getName());
			node.remove();
			delta.removed(e);
			if (e.getElementType() == IJavaElement.IMPORT_DECLARATION) {
				numberOfImports--;
				if (numberOfImports == 0) {
					delta.removed(cu.getImportContainer());
				}
			}
		}
	}
	if (delta.getAffectedChildren().length > 0) {
		commitChanges(cuDOM, cu);
		addDelta(delta);
	}
}
/**
 * @see MultiOperation
 * This method first group the elements by <code>ICompilationUnit</code>,
 * and then processes the <code>ICompilationUnit</code>.
 */
protected void processElements() throws JavaModelException {
	groupElements();
	super.processElements();
}
/**
 * @see MultiOperation
 */
protected void verify(IJavaElement element) throws JavaModelException {
	IJavaElement[] children = ((IRegion) fChildrenToRemove.get(element)).getElements();
	for (int i = 0; i < children.length; i++) {
		IJavaElement child = children[i];
		if (child.getCorrespondingResource() != null)
			error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, child);
		if (child.isReadOnly())
			error(IJavaModelStatusConstants.READ_ONLY, child);
	}
}
}
