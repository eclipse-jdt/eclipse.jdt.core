package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.core.*;

import java.util.*;


import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * The <code>IncrementalReconciler</code> notifies the 
 * JavaModel of changes made the compilation unit that 
 * this reconciler watches.
 *
 * <p>The <code>IncrementalReconciler</code> attempts to
 * improve the performance and functionality of the
 * <code>Reconciler</code> by only reconciling elements 
 * which are effected by a text edit.
 *
 * <p>The implementation is not thread safe.
 *
 * <p>The API expects all text edits to have already occured
 * in the buffer of the <code>ICompilationUnit</code>
 * which this reconciler is using.
 *
 * @see Reconciler 
 */
public class IncrementalReconciler implements IJavaReconciler {
	/**
	 * The current compilation unit
	 */
	protected ICompilationUnit fCompilationUnit = null;	
/**
 * Create a reconciler on a compilation unit
 *
 * @exception IllegalArgumentException if the compilation unit is not
 * a working copy.
 */
public IncrementalReconciler(ICompilationUnit cu) {
	fCompilationUnit = cu;
	if (!cu.isWorkingCopy()) {
		throw new IllegalArgumentException("must create reconciler on a working copy");
	}
}
/**
 * Does a full reconcile.
 */
protected void doFullReconcile() {
	try {
		fCompilationUnit.reconcile();
	} catch (JavaModelException e) {
	}
}
/**
 * Answers the <code>IJavaElement</code> closest too and after the given
 * position in the given parent, or <code>null</code> if there is no element
 * after the given position.
 */
protected IJavaElement getElementAfter(int position, IJavaElement parent) {
	IJavaElement[] children = null;
	try {
		children = ((IParent) parent).getChildren();
	} catch (JavaModelException npe) {
		return null;
	}
	if (children == null)
		return null;
	int length = children.length;
	if (length > 0) {
		for(int i = 0; i < length ; i++) {
			IJavaElement child = children[i];
			if (getSourceOffset(child) + getSourceLength(child) - 1 > position)
				return child;
		}
	}
	return null;
}
/**
 * Answers the <code>IJavaElement</code> following the 
 * given element, or <code>null</code> if there is no element
 * following the given element.
 */
protected IJavaElement getElementAfter(IJavaElement element) {
	IJavaElement[] children = null;
	try {
		children = ((IParent)element.getParent()).getChildren();
	} catch (JavaModelException npe) {
		return null;
	}
	if (children == null)
		return null;
	int length = children.length;
	if (length > 0) {
		for(int i = 0; i < length ; i++) {
			IJavaElement child = children[i];
			if (child.equals(element) && i < length - 1)
				return children[i + 1];
		}
	}
	return null;
}
/**
 * Answers the <code>IJavaElement</code> closest too and before the given
 * position in the given parent, or <code>null</code> if there is no element
 * before the given position.
 */
protected IJavaElement getElementBefore(int position, IJavaElement parent) {
	IJavaElement[] children = null;
	try {
		children = ((IParent) parent).getChildren();
	} catch (JavaModelException npe) {
		return null;
	}
	if (children == null)
		return null;
	int length = children.length;
	if (length > 0) {
		for(int i = length -1; i >= 0; i--) {
			IJavaElement child = children[i];
			if (getSourceOffset(child) + getSourceLength(child) - 1 < position)
				return child;
		}
	}
	return null;
}
/**
 * 
 */
protected IJavaElement[] getElementsBetween(IJavaElement first, IJavaElement last) {
	if (first.equals(last)) {
		return new IJavaElement[] {first};
	}

	IJavaElement[] children = null;
	IParent parent= (IParent)first.getParent();	
	Vector elements= new Vector();
	
	try {
		children = ((IParent) parent).getChildren();
	} catch (JavaModelException npe) {
		return null;
	}
	if (children == null)
		return null;

	boolean inside= false;
	for(int i = 0; i < children.length; i++) {
		IJavaElement child = children[i];
		if (inside) {
			elements.addElement(child);
			if (child.equals(last)) {
				inside= false;
				break;
			}
		} else {
			if (child.equals(first)){
				inside= true;
				elements.addElement(child);
			}
		}
	}

	IJavaElement[] array = new IJavaElement[elements.size()];
	elements.copyInto(array);
	return array;
}
/**
 * Answers the closest enclosing type of the element
 */
protected IType getEnclosingType(IJavaElement element) {
	IJavaElement parent = element.getParent();
	while(parent != null && !(parent.getElementType() == IJavaElement.TYPE))
		parent = parent.getParent();
	return (IType)parent;
}
/**
 * Returns the source length of the element or
 * 0 if the element is not present or not a source element.
 */
protected int getSourceLength(IJavaElement element) {
	if (element instanceof SourceRefElement) {
		try {
			return ((SourceRefElement) element).getSourceRange().getLength();
		} catch (JavaModelException npe) {}
	}
	return 0;
}
/**
 * Returns the source offset of the element or
 * -1 if the element is not present or not a source element.
 */
protected int getSourceOffset(IJavaElement element) {
	if (element instanceof SourceRefElement) {
		try {
			return ((SourceRefElement) element).getSourceRange().getOffset();
		} catch (JavaModelException npe) {}
	}
	return -1;
}
protected boolean isWhitespace(int position, int length) {
	int end = position + length;
	try {
		IBuffer buffer = fCompilationUnit.getBuffer();
		for (int i = position; i < end; i++) {
			if (!Character.isWhitespace(buffer.getChar(i)))
				return false;
		}
		return true;
	} catch (JavaModelException npe) {
		return false;
	}
}
protected void reconcile(SourceType type, IncrementalDeterministicRequestor requestor, int start, int end) {
	SourceElementParser parser = new SourceElementParser(requestor, new DefaultProblemFactory());
	try {
		if (type != null) {
			try {
				parser.parseTypeMemberDeclarations((ISourceType) type.getElementInfo(), (CompilationUnit) fCompilationUnit, start, end, false);
			} catch (JavaModelException npe) {
				doFullReconcile();
				return;
			}
		} else {
			parser.parseCompilationUnit((CompilationUnit) fCompilationUnit, start, end, false);
		}
	} catch (FailedReconciliationException fre) {
		doFullReconcile();
		return;
	}
	System.out.println("Did incremental");
}
/**
 * @see IReconciler
 */
public void textDeleted(int position, int length) {
	doFullReconcile();
	if (true) {
		return;
	}	
	try {
		if (isWhitespace(position, length)) {
			System.out.println("Whitespace");
			return;
		}
		
		int endPosition = position + length;
		IJavaElement first = fCompilationUnit.getElementAt(position);
		IJavaElement last = fCompilationUnit.getElementAt(endPosition);

		if (first == last) {
			if (first == null) {
				first = fCompilationUnit;
				last = fCompilationUnit;
			}
		} else {
			doFullReconcile();
			return;
		}

		if (first instanceof IParent) {
			first = getElementBefore(position, first);
			last = getElementAfter(endPosition, last);
		}

		if (first != last) {
			doFullReconcile();
			return;
		}
		
		int offset = getSourceOffset(first);
		int end = offset + getSourceLength(first) - length;

		if (end <= offset) {
			doFullReconcile();
			return;
		}
		
		SourceType type = (SourceType) getEnclosingType(first);
			
		IncrementalDeterministicRequestor requestor = 
			new IncrementalDeterministicRequestor(new IJavaElement[] {first});
		reconcile(type, requestor, offset, end);
	} catch (JavaModelException e) {
		return;
	}
}
/**
 * @see IReconciler
 */
public void textInserted(int position, int length) {
	doFullReconcile();
	if (true) {
		return;
	}
	try {
		if (isWhitespace(position, length)) {
			System.out.println("Whitespace");
			return;
		}
			
		IJavaElement element= fCompilationUnit.getElementAt(position);
		IJavaElement first= null, last= null;

		if (element == null)
			element = fCompilationUnit;
		
		if (element instanceof IParent) {
			first= getElementBefore(position, element);
			last= getElementAfter(position, element);
		} else {
			if (position == getSourceOffset(element)) {
				first = getElementBefore(position, element.getParent());
				last = element;
			} else {
				first = element;
				last = getElementAfter(element);				
			}
		}

		if (first == null || last == null) {
			doFullReconcile();
			return;
		}

		int offset= getSourceOffset(first);
		int end= offset + getSourceLength(last) + length - 1;

		if (end <= offset) {
			doFullReconcile();
			return;
		}
			
		SourceType type = (SourceType)getEnclosingType(first);			
		// parse the new text, hoping for the same elements
		IncrementalDeterministicRequestor requestor = 
			new IncrementalDeterministicRequestor(new IJavaElement[] {first, last});
		
		reconcile(type, requestor, offset, end);
		
	} catch (JavaModelException e) {
		return;
	}
	
}
}
