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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * This operation deletes a collection of elements (and
 * all of their children).
 * If an element does not exist, it is ignored.
 *
 * <p>NOTE: This operation only deletes elements contained within leaf resources -
 * that is, elements within compilation units. To delete a compilation unit or
 * a package, etc (which have an actual resource), a DeleteResourcesOperation
 * should be used.
 */
public class DeleteElementsOperation extends MultiOperation {
	/**
	 * The elements this operation processes grouped by compilation unit
	 * @see processElements(). Keys are compilation units,
	 * values are <code>IRegion</code>s of elements to be processed in each
	 * compilation unit.
	 */ 
	protected Map childrenToRemove;
	/**
	 * The <code>DOMFactory</code> used to manipulate the source code of
	 * <code>ICompilationUnit</code>s.
	 * @deprecated JDOM is obsolete 
	 */
    // TODO - JDOM - remove once model ported off of JDOM
	protected DOMFactory factory;
	/**
	 * When executed, this operation will delete the given elements. The elements
	 * to delete cannot be <code>null</code> or empty, and must be contained within a
	 * compilation unit.
	 */
	public DeleteElementsOperation(IJavaElement[] elementsToDelete, boolean force) {
		super(elementsToDelete, force);
		initDOMFactory();
	}
	
	/**
	 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
	 */
    // TODO - JDOM - remove once model ported off of JDOM
	private void initDOMFactory() {
		factory = new DOMFactory();
	}

	/**
	 * @see MultiOperation
	 */
	protected String getMainTaskName() {
		return Util.bind("operation.deleteElementProgress"); //$NON-NLS-1$
	}
	/**
	 * Groups the elements to be processed by their compilation unit.
	 * If parent/child combinations are present, children are
	 * discarded (only the parents are processed). Removes any
	 * duplicates specified in elements to be processed.
	 */
	protected void groupElements() throws JavaModelException {
		childrenToRemove = new HashMap(1);
		int uniqueCUs = 0;
		for (int i = 0, length = elementsToProcess.length; i < length; i++) {
			IJavaElement e = elementsToProcess[i];
			ICompilationUnit cu = getCompilationUnitFor(e);
			if (cu == null) {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, e));
			} else {
				IRegion region = (IRegion) childrenToRemove.get(cu);
				if (region == null) {
					region = new Region();
					childrenToRemove.put(cu, region);
					uniqueCUs += 1;
				}
				region.add(e);
			}
		}
		elementsToProcess = new IJavaElement[uniqueCUs];
		Iterator iter = childrenToRemove.keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			elementsToProcess[i++] = (IJavaElement) iter.next();
		}
	}
	/**
	 * Deletes this element from its compilation unit.
	 * @see MultiOperation
	 */
	protected void processElement(IJavaElement element) throws JavaModelException {
		ICompilationUnit cu = (ICompilationUnit) element;
	
		// keep track of the import statements - if all are removed, delete
		// the import container (and report it in the delta)
		int numberOfImports = cu.getImports().length;
	
		IBuffer buffer = cu.getBuffer();
		if (buffer == null) return;
		JavaElementDelta delta = new JavaElementDelta(cu);
		IJavaElement[] cuElements = ((IRegion) childrenToRemove.get(cu)).getElements();
		for (int i = 0, length = cuElements.length; i < length; i++) {
			IJavaElement e = cuElements[i];
			if (e.exists()) {
				char[] contents = buffer.getCharacters();
				if (contents == null) continue;
				String cuName = cu.getElementName();
				replaceElementInBuffer(buffer, e, cuName);
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
			cu.save(getSubProgressMonitor(1), force);
			if (!cu.isWorkingCopy()) { // if unit is working copy, then save will have already fired the delta
				addDelta(delta);
				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
			}
		}
	}
	/**
	 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
	 */
    // TODO - JDOM - remove once model ported off of JDOM
	private void replaceElementInBuffer(IBuffer buffer, IJavaElement elementToRemove, String cuName) {
		IDOMCompilationUnit cuDOM = factory.createCompilationUnit(buffer.getCharacters(), cuName);
		org.eclipse.jdt.internal.core.jdom.DOMNode node = (org.eclipse.jdt.internal.core.jdom.DOMNode)((JavaElement) elementToRemove).findNode(cuDOM);
		if (node == null) Assert.isTrue(false, "Failed to locate " + elementToRemove.getElementName() + " in " + cuDOM.getName()); //$NON-NLS-1$//$NON-NLS-2$

		int startPosition = node.getStartPosition();
		buffer.replace(startPosition, node.getEndPosition() - startPosition + 1, CharOperation.NO_CHAR);
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
		IJavaElement[] children = ((IRegion) childrenToRemove.get(element)).getElements();
		for (int i = 0; i < children.length; i++) {
			IJavaElement child = children[i];
			if (child.getCorrespondingResource() != null)
				error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, child);

			Member localContext;
			if (child instanceof Member && (localContext = ((Member)child).getOuterMostLocalContext()) != null && localContext != child) {
				// JDOM doesn't support source manipulation in local/anonymous types
				error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, child);
			}

			if (child.isReadOnly())
				error(IJavaModelStatusConstants.READ_ONLY, child);
		}
	}
}
