/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
    JDT UI team - Initial implementation copied from internal code in plugin org.eclipse.jdt.ui
**********************************************************************/
package org.eclipse.jdt.core.util;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Utility methods for the Java Model.
 */
public class Util {
/**
 * Concatenates two names. Uses a dot for separation.
 * If one of the strings is empty, then the other one is retuned and
 * no dot is append. 
 * <code>null</code> is interpreted as an empty string, so if both strings
 * are <code>null</code>, the an empty string is returned.
 */
public static String concatenateName(String name1, String name2) {
	if (name1 == null || name1.length() == 0) {
		return name2 == null ? "" : name2; //$NON-NLS-1$
	} else {
		if (name2 == null || name2.length() == 0) {
			return name1;
		} else {
			return name1 + "." + name2; //$NON-NLS-1$
		}
	}
}
/**
 * Returns the fully qualified name of the given type using '.' as separators.
 * This is a replace for IType.getFullyQualifiedTypeName
 * which uses '$' as separators. As '$' is also a valid character in an id
 * this is ambiguous. JavaCore PR: 1GCFUNT
 */
public static String getFullyQualifiedName(IType type) {
	StringBuffer buf= new StringBuffer();
	String packName= type.getPackageFragment().getElementName();
	if (packName.length() > 0) {
		buf.append(packName);
		buf.append('.');
	}
	getTypeQualifiedName(type, buf);
	return buf.toString();
}
/**
 * Resolves a type name in the context of the declaring type.
 * @param refTypeSig the type name in signature notation (for example 'QVector')
 *                   this can also be an array type, but dimensions will be ignored.
 * @param declaringType the context for resolving (type where the reference was made in)
 * @return returns the fully qualified type name or build-in-type name. 
 *  			if a unresoved type couldn't be resolved null is returned
 */
public static String getResolvedTypeName(String refTypeSig, IType declaringType) throws JavaModelException {
	int arrayCount= Signature.getArrayCount(refTypeSig);
	char type= refTypeSig.charAt(arrayCount);
	if (type == Signature.C_UNRESOLVED) {
		int semi= refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
		if (semi == -1) {
			throw new IllegalArgumentException();
		}
		String name= refTypeSig.substring(arrayCount + 1, semi);				
		
		String[][] resolvedNames= declaringType.resolveType(name);
		if (resolvedNames != null && resolvedNames.length > 0) {
			return concatenateName(resolvedNames[0][0], resolvedNames[0][1]);
		}
		return null;
	} else {
		return Signature.toString(refTypeSig.substring(arrayCount));
	}
}
/**
 * Returns the fully qualified name of a type's container. (package name or enclosing type name)
 */
public static String getTypeContainerName(IType type) {
	IType outerType= type.getDeclaringType();
	if (outerType != null) {
		return getFullyQualifiedName(outerType);
	} else {
		return type.getPackageFragment().getElementName();
	}
}
private static void getTypeQualifiedName(IType type, StringBuffer buf) {
	IType outerType= type.getDeclaringType();
	if (outerType != null) {
		getTypeQualifiedName(outerType, buf);
		buf.append('.');
	}
	buf.append(type.getElementName());
}
/**
 * Returns the qualified type name of the given type using '.' as separators.
 * This is a replace for IType.getTypeQualifiedName()
 * which uses '$' as separators. As '$' is also a valid character in an id
 * this is ambiguous. JavaCore PR: 1GCFUNT
 */
public static String getTypeQualifiedName(IType type) {
	StringBuffer buf= new StringBuffer();
	getTypeQualifiedName(type, buf);
	return buf.toString();
}
/**
 * Tests if a method is a main method. Does not resolve the parameter types.
 * Method must exist.
 */
public static boolean isMainMethod(IMethod method) throws JavaModelException {
	if ("main".equals(method.getElementName()) && Signature.SIG_VOID.equals(method.getReturnType())) { //$NON-NLS-1$
		int flags= method.getFlags();
		if (Flags.isStatic(flags) && Flags.isPublic(flags)) {
			String[] paramTypes= method.getParameterTypes();
			if (paramTypes.length == 1) {
				String name=  Signature.toString(paramTypes[0]);
				return "String[]".equals(Signature.getSimpleName(name)); //$NON-NLS-1$
			}
		}
	}
	return false;
}
/**
 * Tests if a method equals to the given signature.
 * Parameter types are only compared by the simple name, no resolving for
 * the fully qualified type name is done. Constructors are only compared by
 * parameters, not the name.
 * @param Name of the method
 * @param The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
 * @param Specifies if the method is a constructor
 * @return Returns <code>true</code> if the method has the given name and parameter types and constructor state.
 */
public static boolean isSameMethodSignature(String name, String[] paramTypes, boolean isConstructor, IMethod curr) throws JavaModelException {
	if (isConstructor || name.equals(curr.getElementName())) {
		if (isConstructor == curr.isConstructor()) {
			String[] currParamTypes= curr.getParameterTypes();
			if (paramTypes.length == currParamTypes.length) {
				for (int i= 0; i < paramTypes.length; i++) {
					String t1= Signature.getSimpleName(Signature.toString(paramTypes[i]));
					String t2= Signature.getSimpleName(Signature.toString(currParamTypes[i]));
					if (!t1.equals(t2)) {
						return false;
					}
				}
				return true;
			}
		}
	}
	return false;
}
}
