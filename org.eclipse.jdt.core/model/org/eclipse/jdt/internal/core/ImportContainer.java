package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

/**
 * @see IImportContainer
 */
public class ImportContainer extends SourceRefElement implements IImportContainer {
protected ImportContainer(ICompilationUnit parent) {
	super(IMPORT_CONTAINER, parent, "");
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	return ((JavaElement)getParent()).getHandleMemento();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	throw new Error("should not be called");
}
/**
 * @see IImportContainer
 */
public IImportDeclaration getImport(String name) {
	return new ImportDeclaration(this, name);
}
/**
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	IJavaElement[] imports= getChildren();
	ISourceRange firstRange= ((ISourceReference)imports[0]).getSourceRange();
	ISourceRange lastRange= ((ISourceReference)imports[imports.length - 1]).getSourceRange();
	SourceRange range= new SourceRange(firstRange.getOffset(), lastRange.getOffset() + lastRange.getLength() - firstRange.getOffset());
	return range;
}
/**
 * Import containers only exist if they have children.
 * @see IParent
 */
public boolean hasChildren() throws JavaModelException {
	return true;
}
/**
 */
public String readableName() {

	return null;
}
/**
 * @private Debugging purposes
 */
protected void toString(int tab, StringBuffer buffer) {
	Object info = fgJavaModelManager.getInfo(this);
	if (info == null || !(info instanceof JavaElementInfo)) return;
	IJavaElement[] children = ((JavaElementInfo)info).getChildren();
	for (int i = 0; i < children.length; i++) {
		if (i > 0) buffer.append("\n");
		((JavaElement)children[i]).toString(tab, buffer);
	}
}
}
