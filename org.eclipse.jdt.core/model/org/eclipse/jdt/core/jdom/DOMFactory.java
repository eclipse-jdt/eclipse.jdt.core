package org.eclipse.jdt.core.jdom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.jdom.*;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Standard implementation of <code>IDOMFactory</code>, and the only means
 * of creating JDOMs and document fragments.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DOMFactory implements IDOMFactory {
	/**
	 * Creates a new DOM factory.
	 */
	public DOMFactory() {
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMCompilationUnit createCompilationUnit() {
		return (new DOMBuilder()).createCompilationUnit();
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMCompilationUnit createCompilationUnit(
		char[] sourceCode,
		String name) {
		if (sourceCode == null) {
			return null;
		}
		return (new SimpleDOMBuilder()).createCompilationUnit(
			sourceCode,
			name.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMCompilationUnit createCompilationUnit(
		String sourceCode,
		String name) {
		if (sourceCode == null) {
			return null;
		}
		return (new SimpleDOMBuilder()).createCompilationUnit(
			sourceCode.toCharArray(),
			name.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMField createField() {
		return createField("Object aField;" + JavaModelManager.LINE_SEPARATOR);
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMField createField(String sourceCode) {
		if (sourceCode == null) {
			return null;
		}
		return (new DOMBuilder()).createField(sourceCode.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMImport createImport() {
		return (new DOMBuilder()).createImport();
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMImport createImport(String sourceCode) {
		if (sourceCode == null) {
			return null;
		}
		return (new DOMBuilder()).createImport(sourceCode.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMInitializer createInitializer() {
		return createInitializer("static {}" + JavaModelManager.LINE_SEPARATOR);
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMInitializer createInitializer(String sourceCode) {
		if (sourceCode == null) {
			return null;
		}
		return (new DOMBuilder()).createInitializer(sourceCode.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMMethod createMethod() {
		return createMethod(
			"public void newMethod() {"
				+ JavaModelManager.LINE_SEPARATOR
				+ "}"
				+ JavaModelManager.LINE_SEPARATOR);
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMMethod createMethod(String sourceCode) {
		if (sourceCode == null) {
			return null;
		}
		return (new DOMBuilder()).createMethod(sourceCode.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMPackage createPackage() {
		return (new DOMBuilder()).createPackage();
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMPackage createPackage(String sourceCode) {
		if (sourceCode == null) {
			return null;
		}
		return (new DOMBuilder()).createPackage(sourceCode.toCharArray());
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMType createType() {
		return createType(
			"public class AClass {"
				+ JavaModelManager.LINE_SEPARATOR
				+ "}"
				+ JavaModelManager.LINE_SEPARATOR);
	}

	/* (non-Javadoc)
	 * Method declared on IDOMFactory.
	 */
	public IDOMType createType(String sourceCode) {
		if (sourceCode == null) {
			return null;
		}
		return (new DOMBuilder()).createType(sourceCode.toCharArray());
	}

}
