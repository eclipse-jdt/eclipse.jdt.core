package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.*;
import java.util.Vector;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.ConfigurableOption;

/**
 * Parent is an IClassFile.
 *
 * @see IType
 */

public class BinaryType extends BinaryMember implements IType {
	private static final IField[] NO_FIELDS = new IField[0];
	private static final IMethod[] NO_METHODS = new IMethod[0];
	private static final IType[] NO_TYPES = new IType[0];
	private static final IInitializer[] NO_INITIALIZERS = new IInitializer[0];
	private static final String[] NO_STRINGS = new String[0];
	protected BinaryType(IJavaElement parent, String name) {
		super(TYPE, parent, name);
		Assert.isTrue(name.indexOf('.') == -1);
	}

	/**
	 * @see IOpenable
	 */
	public void close() throws JavaModelException {
		Object info = fgJavaModelManager.peekAtInfo(this);
		if (info != null) {
			ClassFileInfo cfi = getClassFileInfo();
			if (cfi.hasReadBinaryChildren()) {
				try {
					IJavaElement[] children = getChildren();
					for (int i = 0, size = children.length; i < size; ++i) {
						JavaElement child = (JavaElement) children[i];
						child.close();
					}
				} catch (JavaModelException e) {
				}
			}
			closing(info);
			fgJavaModelManager.removeInfo(this);
		}
	}

	/**
	 * Remove my cached children from the Java Model
	 */
	protected void closing(Object info) throws JavaModelException {
		ClassFileInfo cfi = getClassFileInfo();
		cfi.removeBinaryChildren();
	}

	/**
	 * @see IType
	 */
	public IField createField(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	/**
	 * @see IType
	 */
	public IInitializer createInitializer(
		String contents,
		IJavaElement sibling,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	/**
	 * @see IType
	 */
	public IMethod createMethod(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	/**
	 * @see IType
	 */
	public IType createType(
		String contents,
		IJavaElement sibling,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	/**
	 * @see IParent 
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		// ensure present
		// fix for 1FWWVYT
		if (!exists()) {
			throw newNotPresentException();
		}
		// get children
		ClassFileInfo cfi = getClassFileInfo();
		return cfi.getBinaryChildren();
	}

	protected ClassFileInfo getClassFileInfo() throws JavaModelException {
		ClassFile cf = (ClassFile) fParent;
		return (ClassFileInfo) cf.getElementInfo();
	}

	/**
	 * @see IMember
	 */
	public IType getDeclaringType() {
		try {
			char[] enclosingTypeName = ((IBinaryType) getRawInfo()).getEnclosingTypeName();
			if (enclosingTypeName == null) {
				return null;
			}
			enclosingTypeName = ClassFile.unqualifiedName(enclosingTypeName);
			return getPackageFragment()
				.getClassFile(new String(enclosingTypeName) + ".class")
				.getType();
		} catch (JavaModelException npe) {
			return null;
		}
	}

	/**
	 * @see IType#getField
	 */
	public IField getField(String name) {
		return new BinaryField(this, name);
	}

	/**
	 * @see IType
	 */
	public IField[] getFields() throws JavaModelException {
		Vector v = getChildrenOfType(FIELD);
		int size;
		if ((size = v.size()) == 0) {
			return NO_FIELDS;
		} else {
			IField[] array = new IField[size];
			v.copyInto(array);
			return array;
		}
	}

	/**
	 * @see IMember
	 */
	public int getFlags() throws JavaModelException {
		IBinaryType info = (IBinaryType) getRawInfo();
		return info.getModifiers();
	}

	/**
	 * @see IType
	 */
	public String getFullyQualifiedName() {
		String packageName = getPackageFragment().getElementName();
		if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
			return getTypeQualifiedName();
		}
		return packageName + '.' + getTypeQualifiedName();
	}

	/**
	 * @see IType
	 */
	public IInitializer getInitializer(int occurrenceCount) {
		return new Initializer(this, occurrenceCount);
	}

	/**
	 * @see IType
	 */
	public IInitializer[] getInitializers() {
		return NO_INITIALIZERS;
	}

	/**
	 * @see IType#getMethod
	 */
	public IMethod getMethod(String name, String[] parameterTypeSignatures) {
		return new BinaryMethod(this, name, parameterTypeSignatures);
	}

	/**
	 * @see IType
	 */
	public IMethod[] getMethods() throws JavaModelException {
		Vector v = getChildrenOfType(METHOD);
		int size;
		if ((size = v.size()) == 0) {
			return NO_METHODS;
		} else {
			IMethod[] array = new IMethod[size];
			v.copyInto(array);
			return array;
		}
	}

	/**
	 * @see IType
	 */
	public IPackageFragment getPackageFragment() {
		IJavaElement parent = fParent;
		while (parent != null) {
			if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				return (IPackageFragment) parent;
			} else {
				parent = parent.getParent();
			}
		}
		Assert.isTrue(false); // should not happen
		return null;
	}

	/**
	 * @see IType#getSuperclassName
	 */
	public String getSuperclassName() throws JavaModelException {
		IBinaryType info = (IBinaryType) getRawInfo();
		char[] superclassName = info.getSuperclassName();
		if (superclassName == null) {
			return null;
		}
		return new String(ClassFile.translatedName(superclassName));
	}

	/**
	 * @see IType#getSuperInterfaceNames
	 */
	public String[] getSuperInterfaceNames() throws JavaModelException {
		IBinaryType info = (IBinaryType) getRawInfo();
		char[][] names = info.getInterfaceNames();
		int length;
		if (names == null || (length = names.length) == 0) {
			return NO_STRINGS;
		}
		names = ClassFile.translatedNames(names);
		String[] strings = new String[length];
		for (int i = 0; i < length; i++) {
			strings[i] = new String(names[i]);
		}
		return strings;
	}

	/**
	 * @see IType#getType
	 */
	public IType getType(String name) {
		IClassFile classFile =
			getPackageFragment().getClassFile(
				getTypeQualifiedName() + "$" + name + ".class");
		return new BinaryType(classFile, name);
	}

	/**
	 * @see IType#getTypeQualifiedName
	 */
	public String getTypeQualifiedName() {
		if (fParent.getElementType() == IJavaElement.CLASS_FILE) {
			String name = fParent.getElementName();
			return name.substring(0, name.lastIndexOf('.'));
		}
		if (fParent.getElementType() == IJavaElement.TYPE) {
			return ((IType) fParent).getTypeQualifiedName() + '$' + fName;
		}
		Assert.isTrue(false); // should not be reachable
		return null;
	}

	/**
	 * @see IType
	 */
	public IType[] getTypes() throws JavaModelException {
		Vector v = getChildrenOfType(TYPE);
		int size;
		if ((size = v.size()) == 0) {
			return NO_TYPES;
		} else {
			IType[] array = new IType[size];
			v.copyInto(array);
			return array;
		}
	}

	/**
	 * @see IParent 
	 */
	public boolean hasChildren() throws JavaModelException {
		return getChildren().length > 0;
	}

	/**
	 * @see IType
	 */
	public boolean isClass() throws JavaModelException {
		return !isInterface();
	}

	/**
	 * @see IType#isInterface
	 */
	public boolean isInterface() throws JavaModelException {
		IBinaryType info = (IBinaryType) getRawInfo();
		return info.isInterface();
	}

	/**
	 * @see IType
	 */
	public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor)
		throws JavaModelException {
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(
				this,
				SearchEngine.createWorkspaceScope(),
				false);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * @see IType
	 */
	public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor)
		throws JavaModelException {
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(
				this,
				SearchEngine.createWorkspaceScope(),
				true);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * @see IType
	 */
	public ITypeHierarchy newTypeHierarchy(
		IJavaProject project,
		IProgressMonitor monitor)
		throws JavaModelException {
		if (project == null) {
			throw new IllegalArgumentException("project argument cannot be null");
		}
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(
				this,
				SearchEngine.createJavaSearchScope(new IResource[] { project.getProject()}),
				true);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * Removes all cached info from the Java Model, including all children,
	 * but does not close this element.
	 */
	protected void removeInfo() {
		Object info = fgJavaModelManager.peekAtInfo(this);
		if (info != null) {
			try {
				IJavaElement[] children = getChildren();
				for (int i = 0, size = children.length; i < size; ++i) {
					JavaElement child = (JavaElement) children[i];
					child.removeInfo();
				}
			} catch (JavaModelException e) {
			}
			fgJavaModelManager.removeInfo(this);
			try {
				ClassFileInfo cfi = getClassFileInfo();
				cfi.removeBinaryChildren();
			} catch (JavaModelException npe) {
			}
		}
	}

	public String[][] resolveType(String typeName) throws JavaModelException {
		// not implemented for binary types
		return null;
	}

}
