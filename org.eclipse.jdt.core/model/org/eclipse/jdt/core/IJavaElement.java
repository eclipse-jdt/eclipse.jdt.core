package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Common protocol for all elements provided by the Java model.
 * Java model elements are exposed to clients as handles to the actual underlying element.
 * The Java model may hand out any number of handles for each element. Handles
 * that refer to the same element are guaranteed to be equal, but not necessarily identical.
 * <p>
 * Methods annotated as "handle-only" do not require underlying elements to exist. 
 * Methods that require underlying elements to exist throw
 * a <code>JavaModelException</code> when an underlying element is missing.
 * <code>JavaModelException.isDoesNotExist</code> can be used to recognize
 * this common special case.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IJavaElement extends IAdaptable {

	/**
	 * Constant representing a Java model (workspace level object).
	 * A Java element with this type can be safely cast to <code>IJavaModel</code>.
	 */
	public static final int JAVA_MODEL = 1;

	/**
	 * Constant representing a Java project.
	 * A Java element with this type can be safely cast to <code>IJavaProject</code>.
	 */
	public static final int JAVA_PROJECT = 2;

	/**
	 * Constant representing a package fragment root.
	 * A Java element with this type can be safely cast to <code>IPackageFragmentRoot</code>.
	 */
	public static final int PACKAGE_FRAGMENT_ROOT = 3;

	/**
	 * Constant representing a package fragment.
	 * A Java element with this type can be safely cast to <code>IPackageFragment</code>.
	 */
	public static final int PACKAGE_FRAGMENT = 4;

	/**
	 * Constant representing a Java compilation unit.
	 * A Java element with this type can be safely cast to <code>ICompilationUnit</code>.
	 */
	public static final int COMPILATION_UNIT = 5;

	/**
	 * Constant representing a class file.
	 * A Java element with this type can be safely cast to <code>IClassFile</code>.
	 */
	public static final int CLASS_FILE = 6;

	/**
	 * Constant representing a type (a class or interface).
	 * A Java element with this type can be safely cast to <code>IType</code>.
	 */
	public static final int TYPE = 7;

	/**
	 * Constant representing a field.
	 * A Java element with this type can be safely cast to <code>IField</code>.
	 */
	public static final int FIELD = 8;

	/**
	 * Constant representing a method or constructor.
	 * A Java element with this type can be safely cast to <code>IMethod</code>.
	 */
	public static final int METHOD = 9;

	/**
	 * Constant representing a stand-alone instance or class initializer.
	 * A Java element with this type can be safely cast to <code>IInitializer</code>.
	 */
	public static final int INITIALIZER = 10;

	/**
	 * Constant representing a package declaration within a compilation unit.
	 * A Java element with this type can be safely cast to <code>IPackageDeclaration</code>.
	 */
	public static final int PACKAGE_DECLARATION = 11;

	/**
	 * Constant representing all import declarations within a compilation unit.
	 * A Java element with this type can be safely cast to <code>IImportContainer</code>.
	 */
	public static final int IMPORT_CONTAINER = 12;

	/**
	 * Constant representing an import declaration within a compilation unit.
	 * A Java element with this type can be safely cast to <code>IImportDeclaration</code>.
	 */
	public static final int IMPORT_DECLARATION = 13;

/**
 * Returns whether this Java element exists in the model.
 *
 * @return <code>true</code> if this element exists in the Java model
 */
boolean exists();
/**
 * Returns the resource that corresponds directly to this element,
 * or <code>null</code> if there is no resource that corresponds to
 * this element.
 * <p>
 * For example, the corresponding resource for an <code>ICompilationUnit</code>
 * is its underlying <code>IFile</code>. The corresponding resource for
 * an <code>IPackageFragment</code> that is not contained in an archive 
 * is its underlying <code>IFolder</code>. An <code>IPackageFragment</code>
 * contained in an archive has no corresponding resource. Similarly, there
 * are no corresponding resources for <code>IMethods</code>,
 * <code>IFields</code>, etc.
 * <p>
 *
 * @return the corresponding resource, or <code>null</code> if none
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
IResource getCorrespondingResource() throws JavaModelException;
/**
 * Returns the name of this element. This is a handle-only method.
 *
 * @return the element name
 */
String getElementName();
/**
 * Returns this element's kind encoded as an integer.
 * This is a handle-only method.
 *
 * @return the kind of element; one of the constants declared in
 *   <code>IJavaElement</code>
 * @see IJavaElement
 */
public int getElementType();
/**
 * Returns a string representation of this element handle. The format of
 * the string is not specified; however, the identifier is stable across
 * workspace sessions, and can be used to recreate this handle via the 
 * <code>JavaCore.create(String)</code> method.
 *
 * @return the string handle identifier
 * @see JavaCore#create(java.lang.String)
 */
String getHandleIdentifier();
/**
 * Returns the Java model.
 * This is a handle-only method.
 *
 * @return the Java model
 */
IJavaModel getJavaModel();
/**
 * Returns the Java project this element is contained in,
 * or <code>null</code> if this element is not contained in any Java project
 * (for instance, the <code>IJavaModel</code> is not contained in any Java 
 * project).
 * This is a handle-only method.
 *
 * @return the containing Java project, or <code>null</code> if this element is
 *   not contained in a Java project
 */
IJavaProject getJavaProject();
/**
 * Returns the element directly containing this element,
 * or <code>null</code> if this element has no parent.
 * This is a handle-only method.
 *
 * @return the parent element, or <code>null</code> if this element has no parent
 */
IJavaElement getParent();
/**
 * Returns the smallest underlying resource that contains
 * this element, or <code>null</code> if this element is not contained
 * in a resource.
 *
 * @return the underlying resource, or <code>null</code> if none
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its underlying resource
 */
IResource getUnderlyingResource() throws JavaModelException;
/**
 * Returns whether this Java element is read-only.
 * This is a handle-only method.
 *
 * @return <code>true</code> if this element is read-only
 */
boolean isReadOnly();
/**
 * Returns whether the structure of this element is known. For example, for a
 * compilation unit that could not be parsed, <code>false</code> is returned.
 * If the structure of an element is unknown, navigations will return reasonable
 * defaults. For example, <code>getChildren</code> will return an empty collection.
 * <p>
 * Note: This does not imply anything about consistency with the
 * underlying resource/buffer contents.
 * </p>
 *
 * @return <code>true</code> if the structure of this element is known
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
boolean isStructureKnown() throws JavaModelException;
}
