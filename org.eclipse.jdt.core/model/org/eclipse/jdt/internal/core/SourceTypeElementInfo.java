package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.*;

/** 
 * Element info for an IType element that originated from source. 
 */
public class SourceTypeElementInfo
	extends MemberElementInfo
	implements ISourceType {
	/**
	 * The name of the superclass for this type. This name
	 * is fully qualified for binary types and is NOT
	 * fully qualified for source types.
	 */
	protected char[] fSuperclassName;

	/**
	 * The names of the interfaces this type implements or
	 * extends. These names are fully qualified in the case
	 * of a binary type, and are NOT fully qualified in the
	 * case of a source type
	 */
	protected char[][] fSuperInterfaceNames;

	/**
	 * The enclosing type name for this type.
	 *
	 * @see getEnclosingTypeName
	 */
	protected char[] fEnclosingTypeName = null;

	/**
	 * The name of the source file this type is declared in.
	 */
	protected char[] fSourceFileName = null;

	/**
	 * The name of the package this type is contained in.
	 */
	protected char[] fPackageName = null;

	/**
	 * The qualified name of this type.
	 */
	protected char[] fQualifiedName = null;

	/**
	 * The fields declared in this type
	 */
	protected ISourceField[] fFields = null;

	/**
	 * The methods declared in this type
	 */
	protected ISourceMethod[] fMethods = null;

	/**
	 * The types declared in this type
	 */
	protected ISourceType[] fMemberTypes = null;

	/**
	 * The imports in this type's compilation unit
	 */
	protected char[][] fImports = null;

	/**
	 * Backpointer to my type handle - useful for translation
	 * from info to handle.
	 */
	protected IType fHandle = null;

	/**
	 * Empty list of methods
	 */
	protected static ISourceMethod[] fgEmptyMethods = new ISourceMethod[] {
	};

	/**
	 * Empty list of types
	 */
	protected static ISourceType[] fgEmptyTypes = new ISourceType[] {
	};

	/**
	 * Empty list of fields
	 */
	protected static ISourceField[] fgEmptyFields = new ISourceField[] {
	};

	/**
	 * Empty list of imports
	 */
	protected static char[][] fgEmptyImports = new char[][] {
	};

	/**
	 * Adds the given field to this type's collection of fields
	 */
	protected void addField(ISourceField field) {
		if (fFields == null) {
			fFields = fgEmptyFields;
		}
		ISourceField[] copy = new ISourceField[fFields.length + 1];
		System.arraycopy(fFields, 0, copy, 0, fFields.length);
		copy[fFields.length] = field;
		fFields = copy;
	}

	/**
	 * Adds the given field to this type's collection of fields
	 */
	protected void addImport(char[] i) {
		if (fImports == null) {
			fImports = fgEmptyImports;
		}
		char[][] copy = new char[fImports.length + 1][];
		System.arraycopy(fImports, 0, copy, 0, fImports.length);
		copy[fImports.length] = i;
		fImports = copy;
	}

	/**
	 * Adds the given type to this type's collection of member types
	 */
	protected void addMemberType(ISourceType type) {
		if (fMemberTypes == null) {
			fMemberTypes = fgEmptyTypes;
		}
		ISourceType[] copy = new ISourceType[fMemberTypes.length + 1];
		System.arraycopy(fMemberTypes, 0, copy, 0, fMemberTypes.length);
		copy[fMemberTypes.length] = type;
		fMemberTypes = copy;
	}

	/**
	 * Adds the given method to this type's collection of methods
	 */
	protected void addMethod(ISourceMethod method) {
		if (fMethods == null) {
			fMethods = fgEmptyMethods;
		}
		ISourceMethod[] copy = new ISourceMethod[fMethods.length + 1];
		System.arraycopy(fMethods, 0, copy, 0, fMethods.length);
		copy[fMethods.length] = method;
		fMethods = copy;
	}

	/**
	 * Returns the ISourceType that is the enclosing type for this
	 * type, or <code>null</code> if this type is a top level type.
	 */
	public ISourceType getEnclosingType() {
		IJavaElement parent = fHandle.getParent();
		if (parent != null && parent.getElementType() == IJavaElement.TYPE) {
			try {
				return (ISourceType) ((JavaElement) parent).getElementInfo();
			} catch (JavaModelException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @see ISourceType
	 */
	public char[] getEnclosingTypeName() {
		return fEnclosingTypeName;
	}

	/**
	 * @see ISourceType
	 */
	public ISourceField[] getFields() {
		return fFields;
	}

	/**
	 * @see ISourceType
	 */
	public char[] getFileName() {
		return fSourceFileName;
	}

	/**
	 * Returns the handle for this type info
	 */
	public IType getHandle() {
		return fHandle;
	}

	/**
	 * @see ISourceType
	 */
	public char[][] getImports() {
		return fImports;
	}

	/**
	 * @see ISourceType
	 */
	public char[][] getInterfaceNames() {
		return fSuperInterfaceNames;
	}

	/**
	 * @see ISourceType
	 */
	public ISourceType[] getMemberTypes() {
		return fMemberTypes;
	}

	/**
	 * @see ISourceType
	 */
	public ISourceMethod[] getMethods() {
		return fMethods;
	}

	/**
	 * @see ISourceType
	 */
	public char[] getPackageName() {
		return fPackageName;
	}

	/**
	 * @see ISourceType
	 */
	public char[] getQualifiedName() {
		return fQualifiedName;
	}

	/**
	 * @see ISourceType
	 */
	public char[] getSuperclassName() {
		return fSuperclassName;
	}

	/**
	 * @see ISourceType
	 */
	public boolean isBinaryType() {
		return false;
	}

	/**
	 * @see ISourceType
	 */
	public boolean isClass() {
		return (this.flags & IConstants.AccInterface) == 0;
	}

	/**
	 * @see ISourceType
	 */
	public boolean isInterface() {
		return (this.flags & IConstants.AccInterface) != 0;
	}

	/**
	 * Sets the (unqualified) name of the type that encloses this type.
	 */
	protected void setEnclosingTypeName(char[] enclosingTypeName) {
		fEnclosingTypeName = enclosingTypeName;
	}

	/**
	 * Sets the handle for this type info
	 */
	protected void setHandle(IType handle) {
		fHandle = handle;
	}

	/**
	 * Sets the name of the package this type is declared in.
	 */
	protected void setPackageName(char[] name) {
		fPackageName = name;
	}

	/**
	 * Sets this type's qualified name.
	 */
	protected void setQualifiedName(char[] name) {
		fQualifiedName = name;
	}

	/**
	 * Sets the name of the source file this type is declared in.
	 */
	protected void setSourceFileName(char[] name) {
		fSourceFileName = name;
	}

	/**
	 * Sets the (unqualified) name of this type's superclass
	 */
	protected void setSuperclassName(char[] superclassName) {
		fSuperclassName = superclassName;
	}

	/**
	 * Sets the (unqualified) names of the interfaces this type implements or extends
	 */
	protected void setSuperInterfaceNames(char[][] superInterfaceNames) {
		fSuperInterfaceNames = superInterfaceNames;
	}

}
