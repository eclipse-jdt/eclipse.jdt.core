package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This operation renames elements.
 *
 * <p>Notes:<ul>
 * <li>Resource rename is not supported - this operation only renames
 *	   elements contained in compilation units.
 * <li>When a main type is renamed, its compilation unit and constructors are renamed.
 * <li>Constructors cannot be renamed.
 * </ul>
 */
public class RenameElementsOperation extends MoveElementsOperation {
/**
 * When executed, this operation will rename the specified elements with the given names in the
 * corresponding destinations.
 */
public RenameElementsOperation(IJavaElement[] elements, IJavaElement[] destinations, String[] newNames, boolean force) {
	//a rename is a move to the same parent with a new name specified
	//these elements are from different parents
	super(elements, destinations, force);
	setRenamings(newNames);
}
/**
 * @see MultiOperation
 */
protected String getMainTaskName() {
	return "Renaming elements...";
}
/**
 * @see CopyElementsOperation#isRename()
 */
protected boolean isRename() {
	return true;
}
/**
 * @see MultiOperation
 */
protected IJavaModelStatus verify() {
	IJavaModelStatus status = super.verify();
	if (! status.isOK())
		return status;
	if (fRenamingsList == null || fRenamingsList.length == 0)
		return new JavaModelStatus(IJavaModelStatusConstants.NULL_NAME);
	return JavaModelStatus.VERIFIED_OK;
}
/**
 * @see MultiOperation
 */
protected void verify(IJavaElement element) throws JavaModelException {
	int elementType = element.getElementType();
	
	if (element == null || !element.exists())
		error(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);
		
	if (element.isReadOnly())
		error(IJavaModelStatusConstants.READ_ONLY, element);
		
	if (!(element instanceof ISourceReference))
		error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
		
	if (elementType < IJavaElement.TYPE || elementType == IJavaElement.INITIALIZER)
		error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
		
	verifyRenaming(element);
}
}
