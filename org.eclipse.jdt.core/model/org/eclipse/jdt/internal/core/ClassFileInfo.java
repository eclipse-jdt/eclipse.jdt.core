/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

/**
 * Element info for <code>ClassFile</code> handles.
 */
 
/* package */ class ClassFileInfo extends OpenableElementInfo implements SuffixConstants {
	/** 
	 * The children of the <code>BinaryType</code> corresponding to our
	 * <code>ClassFile</code>. These are kept here because we don't have
	 * access to the <code>BinaryType</code> info (<code>ClassFileReader</code>).
	 * <p>
	 * The children are lazily initialized, on the first call to
	 * <code>getBinaryChildren()</code>, which in turn is called by
	 * <code>BinaryType.getChildren()</code>. 
	 */
	protected JavaElement[] binaryChildren = null;
	/**
	 * Back-pointer to the IClassFile to allow lazy initialization.
	 */
	protected ClassFile classFile = null;
/**
 * Creates a new <code>ClassFileInfo</code> for <code>classFile</code>.
 */
ClassFileInfo(ClassFile classFile) {
	this.classFile = classFile;
}
/**
 * Creates the handles and infos for the fields of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateFieldInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList childrenHandles) {
	// Make the fields
	IBinaryField[] fields = typeInfo.getFields();
	if (fields == null) {
		return;
	}
	for (int i = 0, fieldCount = fields.length; i < fieldCount; i++) {
		IBinaryField fieldInfo = fields[i];
		IField field = new BinaryField((JavaElement)type, new String(fieldInfo.getName()));
		newElements.put(field, fieldInfo);
		childrenHandles.add(field);
	}
}
/**
 * Creates the handles for the inner types of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateInnerClassHandles(IType type, IBinaryType typeInfo, ArrayList childrenHandles) {
	// Add inner types
	// If the current type is an inner type, innerClasses returns
	// an extra entry for the current type.  This entry must be removed.
	// Can also return an entry for the enclosing type of an inner type.
	IBinaryNestedType[] innerTypes = typeInfo.getMemberTypes();
	if (innerTypes != null) {
		for (int i = 0, typeCount = innerTypes.length; i < typeCount; i++) {
			IBinaryNestedType binaryType = innerTypes[i];
			IClassFile parentClassFile= ((IPackageFragment)this.classFile.getParent()).getClassFile(new String(ClassFile.unqualifiedName(binaryType.getName())) + SUFFIX_STRING_class);
			IType innerType = new BinaryType((JavaElement)parentClassFile, new String(ClassFile.simpleName(binaryType.getName())));
			childrenHandles.add(innerType);
		}
	}
}
/**
 * Creates the handles and infos for the methods of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateMethodInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList childrenHandles) {
	IBinaryMethod[] methods = typeInfo.getMethods();
	if (methods == null) {
		return;
	}
	for (int i = 0, methodCount = methods.length; i < methodCount; i++) {
		IBinaryMethod methodInfo = methods[i];
		// TODO (jerome) filter out synthetic members
		//                        indexer should not index them as well
		// if ((methodInfo.getModifiers() & IConstants.AccSynthetic) != 0) continue; // skip synthetic
		char[] signature = methodInfo.getGenericSignature();
		if (signature == null) signature = methodInfo.getMethodDescriptor();
		String[] pNames = null;
		try {
			pNames = Signature.getParameterTypes(new String(signature));
		} catch (IllegalArgumentException e) {
			// protect against malformed .class file (e.g. com/sun/crypto/provider/SunJCE_b.class has a 'a' generic signature)
			signature = methodInfo.getMethodDescriptor();
			pNames = Signature.getParameterTypes(new String(signature));
		}
		char[][] paramNames= new char[pNames.length][];
		for (int j= 0; j < pNames.length; j++) {
			paramNames[j]= pNames[j].toCharArray();
		}
		char[][] parameterTypes = ClassFile.translatedNames(paramNames);
		String selector = new String(methodInfo.getSelector());
		if (methodInfo.isConstructor()) {
			selector = type.getElementName();
		}
		for (int j= 0; j < pNames.length; j++) {
			pNames[j]= new String(parameterTypes[j]);
		}
		BinaryMethod method = new BinaryMethod((JavaElement)type, selector, pNames);
		childrenHandles.add(method);
		
		// ensure that 2 binary methods with the same signature but with different return types have different occurence counts.
		// (case of bridge methods in 1.5)
		while (newElements.containsKey(method))
			method.occurrenceCount++;
		
		newElements.put(method, methodInfo);
	}
}
/**
 * Returns the list of children (<code>BinaryMember</code>s) of the
 * <code>BinaryType</code> of our <code>ClassFile</code>.
 */
IJavaElement[] getBinaryChildren(HashMap newElements) {
	if (this.binaryChildren == null) {
		readBinaryChildren(newElements, null/*type info not known here*/);
	}
	return this.binaryChildren;
}
/**
 * Returns true iff the <code>readBinaryChildren</code> has already
 * been called.
 */
boolean hasReadBinaryChildren() {
	return this.binaryChildren != null;
}
/**
 * Creates the handles for <code>BinaryMember</code>s defined in this
 * <code>ClassFile</code> and adds them to the
 * <code>JavaModelManager</code>'s cache.
 */
protected void readBinaryChildren(HashMap newElements, IBinaryType typeInfo) {
	ArrayList childrenHandles = new ArrayList();
	BinaryType type = null;
	try {
		type = (BinaryType) this.classFile.getType();
		if (typeInfo == null) {
			typeInfo = (IBinaryType) newElements.get(type);
			if (typeInfo == null) {
				// create a classfile reader 
			    typeInfo = this.classFile.getBinaryTypeInfo((IFile)this.classFile.getResource());
			}
		}
	} catch (JavaModelException npe) {
		return;
	}
	if (typeInfo != null) { //may not be a valid class file
		generateFieldInfos(type, typeInfo, newElements, childrenHandles);
		generateMethodInfos(type, typeInfo, newElements, childrenHandles);
		generateInnerClassHandles(type, typeInfo, childrenHandles); // Note inner class are separate openables that are not opened here: no need to pass in newElements
	}
	
	this.binaryChildren = new JavaElement[childrenHandles.size()];
	childrenHandles.toArray(this.binaryChildren);
}
/**
 * Removes the binary children handles and remove their infos from
 * the <code>JavaModelManager</code>'s cache.
 */
void removeBinaryChildren() throws JavaModelException {
	if (this.binaryChildren != null) {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		for (int i = 0; i <this.binaryChildren.length; i++) {
			JavaElement child = this.binaryChildren[i];
			if (child instanceof BinaryType) {
				manager.removeInfoAndChildren((JavaElement)child.getParent());
			} else {
				manager.removeInfoAndChildren(child);
			}
		}
		this.binaryChildren = JavaElement.NO_ELEMENTS;
	}
}
}
