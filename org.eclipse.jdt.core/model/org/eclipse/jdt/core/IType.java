package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents either a source type in a compilation unit (either a top-level
 * type or a member type) or a binary type in a class file.
 * <p>
 * If a binary type cannot be parsed, its structure remains unknown.
 * Use <code>IJavaElement.isStructureKnown</code> to determine whether this
 * is the case.
 * </p>
 * <p>
 * The children are of type <code>IMember</code>, which includes <code>IField</code>,
 * <code>IMethod</code>, <code>IInitializer</code> and <code>IType</code>.
 * The children are listed in the order in which they appear in the source or class file.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IType extends IMember, IParent {

	/**
	 * Creates and returns a field in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the element will be inserted
	 * as the last field declaration in this type.
	 *
	 * <p>It is possible that a field with the same name already exists in this type.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the field is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 *
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as a field declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * <li> There was a naming collision with an existing field (NAME_COLLISION)
	 * </ul>
	 */
	IField createField(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Creates and returns a static initializer in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the new initializer is positioned
	 * after the last existing initializer declaration, or as the first member
	 * in the type if there are no
	 * initializers.
	 *
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This element does not exist
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as an initializer declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * </ul>
	 */
	IInitializer createInitializer(
		String contents,
		IJavaElement sibling,
		IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Creates and returns a method or constructor in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the element will be appended
	 * to this type.
	 *
	 * <p>It is possible that a method with the same signature already exists in this type.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the method is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 *
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as a method or constructor
	 *		declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * <li> There was a naming collision with an existing method (NAME_COLLISION)
	 * </ul>
	 */
	IMethod createMethod(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Creates and returns a type in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new type can be positioned before the specified
	 * sibling. If no sibling is specified, the type will be appended
	 * to this type.
	 *
	 * <p>It is possible that a type with the same name already exists in this type.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the type is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 *
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as a type declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * <li> There was a naming collision with an existing field (NAME_COLLISION)
	 * </ul>
	 */
	IType createType(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Returns the simple name of this type, unqualified by package or enclosing type.
	 * This is a handle-only method.
	 */
	String getElementName();
	/**
	 * Returns the field with the specified name
	 * in this type (for example, <code>"bar"</code>).
	 * This is a handle-only method.  The field may or may not exist.
	 */
	IField getField(String name);
	/**
	 * Returns the fields declared by this type.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source, otherwise, the results are
	 * in no particular order.  For binary types, this includes synthetic fields.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	IField[] getFields() throws JavaModelException;
	/**
	 * Returns the fully qualified name of this type, 
	 * including qualification for any containing types and packages.
	 * This is the name of the package, followed by <code>'.'</code>,
	 * followed by the type-qualified name.
	 * This is a handle-only method.
	 *
	 * @see IType#getTypeQualifiedName()
	 */
	String getFullyQualifiedName();
	/**
	 * Returns the Initializer with the specified position relative to
	 * the order they are defined in the source.
	 * Numbering starts at 1 (i.e. the first occurrence is occurrence 1, not occurrence 0).
	 * This is a handle-only method.  The initializer may or may not be present.
	 */
	IInitializer getInitializer(int occurrenceCount);
	/**
	 * Returns the initializers declared by this type.
	 * For binary types this is an empty collection.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	IInitializer[] getInitializers() throws JavaModelException;
	/**
	 * Returns the method with the specified name and parameter types
	 * in this type (for example, <code>"foo", {"I", "QString;"}</code>). To get the
	 * handle for a constructor, the name specified must be the simple
	 * name of the enclosing type.
	 * This is a handle-only method.  The method may or may not be present.
	 */
	IMethod getMethod(String name, String[] parameterTypeSignatures);
	/**
	 * Returns the methods and constructors declared by this type.
	 * For binary types, this may include the special <code>&lt;clinit&gt</code>; method 
	 * and synthetic methods.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source, otherwise, the results are
	 * in no particular order.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	IMethod[] getMethods() throws JavaModelException;
	/**
	 * Returns the package fragment in which this element is defined.
	 * This is a handle-only method.
	 */
	IPackageFragment getPackageFragment();
	/**
	 * Returns the name of this type's superclass, or <code>null</code>
	 * for source types that do not specify a superclass.
	 * For interfaces, the superclass name is always <code>"java.lang.Object"</code>.
	 * For source types, the name as declared is returned, for binary types,
	 * the resolved, qualified name is returned.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	String getSuperclassName() throws JavaModelException;
	/**
	 * Returns the names of interfaces that this type implements or extends,
	 * in the order in which they are listed in the source.
	 * For classes, this gives the interfaces that this class implements.
	 * For interfaces, this gives the interfaces that this interface extends.
	 * An empty collection is returned if this type does not implement or
	 * extend any interfaces. For source types, simples name are returned,
	 * for binary types, qualified names are returned.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	String[] getSuperInterfaceNames() throws JavaModelException;
	/**
	 * Returns the member type declared in this type with the given simple name.
	 * This is a handle-only method. The type may or may not exist.
	 */
	IType getType(String name);
	/**
	 * Returns the type-qualified name of this type, 
	 * including qualification for any enclosing types,
	 * but not including package qualification.
	 * For source types, this consists of the simple names of
	 * any enclosing types, separated by <code>"$"</code>, followed by the simple name of this type.
	 * For binary types, this is the name of the class file without the ".class" suffix.
	 * This is a handle-only method.
	 */
	String getTypeQualifiedName();
	/**
	 * Returns the immediate member types declared by this type.
	 * The results are listed in the order in which they appear in the source or class file.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	IType[] getTypes() throws JavaModelException;
	/**
	 * Returns whether this type represents a class.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	boolean isClass() throws JavaModelException;
	/**
	 * Returns whether this type represents an interface.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	boolean isInterface() throws JavaModelException;
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ITypeHierarchy newTypeHierarchy(IJavaProject project, IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Resolves the given type name within the context of this type (depending on the type hierarchy 
	 * and its imports). Multiple answers might be found in case there are ambiguous matches.
	 *
	 * Each matching type name is decomposed as an array of two strings, the first denoting the package
	 * name (dot-separated) and the second being the type name.
	 * Returns <code>null</code> if unable to find any matching type.
	 *
	 * For example, resolution of <code>"Object"</code> would typically return
	 * <code>{{"java.lang", "Object"}}</code>.
	 *
	 * @exception JavaModelException if code resolve could not be performed. 
	 */
	String[][] resolveType(String typeName) throws JavaModelException;
}
