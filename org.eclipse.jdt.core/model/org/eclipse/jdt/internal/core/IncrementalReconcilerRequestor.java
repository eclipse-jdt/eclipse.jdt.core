package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.ReferenceInfoAdapter;

import java.util.*;

/**
 * Handles the results of a source element parse, and with
 * the help of the <code>Reconciler</code>, throws element
 * changed deltas.
 */
public class IncrementalReconcilerRequestor
	extends ReferenceInfoAdapter
	implements ISourceElementRequestor {
	/**
	 * The compilation unit the reconciler is working on
	 */
	protected ICompilationUnit fCompilationUnit;

	/**
	 * The last expected element
	 */
	protected IJavaElement fLastElement;

	/**
	 * As the compilation unit is traversed, this stack
	 * maintains lists of children and positions in the list
	 */
	protected Stack fStack;

	/**
	 * The next element expected from the parser.
	 */
	protected IJavaElement fNextElement;

	/**
	 * A helper class which stores children and array position information
	 */
	class TraversalInfo {
		protected IJavaElement[] fArray;
		protected int fLength;
		protected int fPosition;
		protected boolean fIsType;
		protected String fParentName;
		protected int fInitializerCount;

		public TraversalInfo(IJavaElement[] array, String parentName, boolean isType) {
			fArray = array;
			fParentName = parentName;
			fIsType = isType;
			fLength = (fArray != null) ? fArray.length : 0;
			fPosition = 0;
			fInitializerCount = 0;
		}
		public IJavaElement next() {
			if (fPosition < fLength)
				return fArray[fPosition++];
			return null;
		}
		public String getParentName() {
			return fParentName;
		}
		public int getNextInitializer() {
			return ++fInitializerCount;
		}
		public int getInitializerCount() {
			return fInitializerCount;
		}
		public boolean isType() {
			return fIsType;
		}
	}

	/**
	 * Creates a requestor
	 */
	public IncrementalReconcilerRequestor(
		ICompilationUnit cu,
		IJavaElement first,
		IJavaElement last) {
		fCompilationUnit = cu;
		fStack = new Stack();
		initializeStack();
		advanceTo(first);
		fLastElement = last;
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptImport(
		int declarationStart,
		int declarationEnd,
		char[] name,
		boolean onDemand) {
		if (fNextElement.getElementType() == IJavaElement.IMPORT_DECLARATION) {
			if (isIdentical((IImportDeclaration) fNextElement, name, onDemand)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptInitializer(
		int modifiers,
		int declarationSourceStart,
		int declarationSourceEnd) {
		if (fNextElement.getElementType() == IJavaElement.IMPORT_DECLARATION) {
			next();
			return;
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptLineSeparatorPositions(int[] positions) {
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptPackage(
		int declarationStart,
		int declarationEnd,
		char[] name) {
		if (fNextElement.getElementType() == IJavaElement.PACKAGE_DECLARATION) {
			if (isIdentical((IPackageDeclaration) fNextElement, name)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptProblem(IProblem problem) {
	}

	/**
	 * Advances to the next expected element
	 */
	protected void advance() {
		if (!fStack.empty()) {
			TraversalInfo info = (TraversalInfo) fStack.peek();
			fNextElement = info.next();
			if (fNextElement == null) {
				fStack.pop();
				next();
				return;
			} else {
				if (fNextElement instanceof IParent)
					push(fNextElement);
			}
		} else {
			fNextElement = null;
		}
	}

	/**
	 * Advances to the given element.
	 */
	protected void advanceTo(IJavaElement element) {
		while (fNextElement != element)
			advance();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterClass(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[] superclass,
		char[][] superinterfaces) {
		if (fNextElement.getElementType() == IJavaElement.TYPE) {
			if (isIdentical((IType) fNextElement, name)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterCompilationUnit() {
		if (fNextElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			next();
			return;
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterConstructor(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes) {
		if (fNextElement.getElementType() == IJavaElement.METHOD) {
			if (isIdentical((IMethod) fNextElement, name, parameterTypes)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterField(
		int declarationStart,
		int modifiers,
		char[] type,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd) {
		if (fNextElement.getElementType() == IJavaElement.FIELD) {
			if (isIdentical((IField) fNextElement, name)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterInterface(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] superinterfaces) {
		if (fNextElement.getElementType() == IJavaElement.TYPE) {
			if (isIdentical((IType) fNextElement, name)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterMethod(
		int declarationStart,
		int modifiers,
		char[] returnType,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes) {
		if (fNextElement.getElementType() == IJavaElement.METHOD) {
			if (isIdentical((IMethod) fNextElement, name, parameterTypes)) {
				next();
				return;
			}
		}
		throw new FailedReconciliationException();
	}

	/**
	 * Answers true if the two arguments are equal.  If either are null answers false, if
	 * both are null answers true;
	 */
	protected static final boolean equals(String string, char[] chars) {
		if (string == null ^ chars == null)
			return false;
		if (string == null)
			return true;
		int length = chars.length;
		if (string.length() != length)
			return false;
		while (length-- != 0) {
			if (string.charAt(length) != chars[length])
				return false;
		}
		return true;
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitClass(int declarationEnd) {
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitCompilationUnit(int declarationEnd) {
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitConstructor(int declarationEnd) {
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitField(int declarationEnd) {
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitInterface(int declarationEnd) {
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitMethod(int declarationEnd) {
	}

	/**
	 * Returns the first type encountered on the top of the stack
	 */
	protected IType getCurrentType() {
		IType type = null;
		for (int i = 0, length = fStack.size(); i < length; i++) {
			TraversalInfo info = (TraversalInfo) fStack.elementAt(i);
			if (info.isType()) {
				if (type == null) {
					type = fCompilationUnit.getType(info.getParentName());
				} else {
					type = type.getType(info.getParentName());
				}
			}
		}
		return type;
	}

	/**
	 * Answers the next initializer number for the current type
	 */
	protected int getNextInitializerNumber() {
		// reverse traversal intentional
		for (int i = fStack.size() - 1; i >= 0; i--) {
		}
		return -1;
	}

	/**
	 * Initializes the stack to contain <code>fCompilationUnit</code>
	 */
	protected void initializeStack() {
		IJavaElement[] elements = { fCompilationUnit };
		fStack.push(new TraversalInfo(elements, null, false));
		next();
	}

	protected boolean isIdentical(IField field, char[] name) {
		if (!equals(field.getElementName(), name))
			return false;
		return true;
	}

	protected boolean isIdentical(
		IImportDeclaration anImport,
		char[] name,
		boolean isOnDemand) {
		String importString = new String(name);
		if (isOnDemand)
			importString += ".*";
		if (!anImport.getElementName().equals(importString))
			return false;
		return true;
	}

	protected boolean isIdentical(
		IMethod method,
		char[] name,
		char[][] parameterTypes) {
		if (!equals(method.getElementName(), name))
			return false;
		String[] parameters = method.getParameterTypes();
		if (parameters == null || parameters.length == 0) {
			if (parameterTypes == null || parameterTypes.length == 0) {
				return true;
			} else {
				return false;
			}
		}

		if (parameters.length != parameterTypes.length)
			return false;
		for (int i = 0, length = parameters.length; i < length; i++) {
			if (parameters[i] == null ^ parameterTypes[i] == null)
				return false;
			if (parameters[i] == null)
				continue;
			if (!parameters[i]
				.equals(Signature.createTypeSignature(parameterTypes[i], false)))
				return false;
		}
		return true;
	}

	protected boolean isIdentical(IPackageDeclaration aPackage, char[] name) {
		if (!equals(aPackage.getElementName(), name))
			return false;
		return true;
	}

	protected boolean isIdentical(IType type, char[] name) {
		if (!equals(type.getElementName(), name))
			return false;
		return true;
	}

	/**
	 * Advances the next element expected.
	 */
	protected void next() {
		advance();
		if (fNextElement.getElementType() == IJavaElement.IMPORT_CONTAINER)
			next();
	}

	/**
	 * Pushes the children of the container on the stack
	 */
	protected void push(IJavaElement element) {
		IParent parent = (IParent) element;
		boolean hasChildren = false;
		try {
			hasChildren = parent.hasChildren();
		} catch (JavaModelException npe) {
		}
		if (hasChildren) {
			IJavaElement[] children = null;
			try {
				children = parent.getChildren();
			} catch (JavaModelException npe) {
			}
			if (children != null) {
				TraversalInfo info =
					new TraversalInfo(
						children,
						element.getElementName(),
						element.getElementType() == IJavaElement.TYPE);
				fStack.push(info);
			}
		}
	}

	/**
	 * Updates the source ranges of the elements which weren't effected by
	 * the parse, but follow the parsed elements.
	 */
	void updateSourceRanges() {
	}

}
