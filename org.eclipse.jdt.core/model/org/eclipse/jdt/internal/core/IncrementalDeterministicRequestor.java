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
public class IncrementalDeterministicRequestor extends ReferenceInfoAdapter implements ISourceElementRequestor {
	

	/**
	 * The expected elements
	 */
	protected IJavaElement[] fElements;

	/**
	 * The next element expected
	 */
	protected IJavaElement fNextElement;

	/**
	 * The index of the next expected element
	 */
	protected int fNextIndex= 0;


/**
 * Creates a requestor
 */
public IncrementalDeterministicRequestor(IJavaElement[] expectations) {
	fElements= expectations;
	next();
}
/**
 * @see ISourceElementRequestor
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand) {
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
public void acceptInitializer(int modifiers, int declarationSourceStart, int declarationSourceEnd) {
	if (fNextElement.getElementType() == IJavaElement.IMPORT_DECLARATION) {
		next();
		return;
	}
	throw new FailedReconciliationException();
}
/**
 * @see ISourceElementRequestor
 */
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * @see ISourceElementRequestor
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {
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
public void acceptProblem(IProblem problem) {}
/**
 * Advances to the next expected element
 */
protected void advance() {
	if (fNextIndex < fElements.length) {
		fNextElement= fElements[fNextIndex];
		fNextIndex++;
	} else {
		fNextElement= null;
	}
}
/**
 * @see ISourceElementRequestor
 */
public void enterClass(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[] superclass, char[][] superinterfaces) {
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
public void enterConstructor(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	if (fNextElement.getElementType() == IJavaElement.METHOD) {
		if (isIdentical((IMethod)fNextElement, name, parameterTypes)) {
			next();
			return;
		} 
	}
	throw new FailedReconciliationException();
}
/**
 * @see ISourceElementRequestor
 */
public void enterField(int declarationStart, int modifiers, char[] type, char[] name, int nameSourceStart, int nameSourceEnd) {
	if (fNextElement.getElementType() == IJavaElement.FIELD) {
		if (isIdentical((IField)fNextElement, name)) {
			next();
			return;
		} 
	}
	throw new FailedReconciliationException();
}
/**
 * @see ISourceElementRequestor
 */
public void enterInterface(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] superinterfaces) {
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
public void enterMethod(int declarationStart, int modifiers, char[] returnType, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	if (fNextElement.getElementType() == IJavaElement.METHOD) {
		if (isIdentical((IMethod)fNextElement, name, parameterTypes)) {
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
	while(length-- != 0) {
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
public void exitCompilationUnit(int declarationEnd) {}
/**
 * @see ISourceElementRequestor
 */
public void exitConstructor(int declarationEnd) {}
/**
 * @see ISourceElementRequestor
 */
public void exitField(int declarationEnd) {}
/**
 * @see ISourceElementRequestor
 */
public void exitInterface(int declarationEnd) {
}
/**
 * @see ISourceElementRequestor
 */
public void exitMethod(int declarationEnd) {}
protected boolean isIdentical(IField field, char[] name) {
	if (!equals(field.getElementName(), name))
		return false;
	return true;
}
protected boolean isIdentical(IImportDeclaration anImport, char[] name, boolean isOnDemand) {
	String importString = new String(name);
	if (isOnDemand)
		importString += ".*";
	if (!anImport.getElementName().equals(importString))
		return false;
	return true;
}
protected boolean isIdentical(IMethod method, char[] name, char[][] parameterTypes) {
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
	for(int i = 0, length = parameters.length; i < length; i++) {
		if (parameters[i] == null ^ parameterTypes[i] == null)
			return false;
		if (parameters[i] == null)
			continue;
		if (!parameters[i].equals(Signature.createTypeSignature(parameterTypes[i], false)))
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
}
