package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.builder.*;

import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.Util;


/**
 * Utility class for interpreting the contents of IBinaryType objects.
 */
public abstract class BinaryStructure {
/**
 * Should not instantiate.
 */
private BinaryStructure() {
}
protected static boolean compare(IBinaryField[] listA, IBinaryField[] listB) {
	if (listA == listB)
		return true;
	if (listA == null || listB == null)
		return false;
	if (listA.length != listB.length)
		return false;
	Util.Comparer comparer = new Util.Comparer() {
		public int compare(Object a, Object b) {
			IBinaryField fa = (IBinaryField) a;
			IBinaryField fb = (IBinaryField) b;
			int code = Util.compare(fa.getName(), fb.getName());
			if (code != 0)
				return code;
			return Util.compare(fa.getTypeName(), fb.getTypeName());
		}
	};
	Object[] sortedA = Util.sortCopy(listA, comparer);
	Object[] sortedB = Util.sortCopy(listB, comparer);
	for (int i = 0; i < listA.length; ++i) {
		if (!compare((IBinaryField) sortedA[i], (IBinaryField) sortedB[i]))
			return false;
	}
	return true;
}
protected static boolean compare(IBinaryMethod[] listA, IBinaryMethod[] listB) {
	if (listA == listB)
		return true;
	if (listA == null || listB == null)
		return false;
	if (listA.length != listB.length)
		return false;
	Util.Comparer comparer = new Util.Comparer() {
		public int compare(Object a, Object b) {
			IBinaryMethod ma = (IBinaryMethod) a;
			IBinaryMethod mb = (IBinaryMethod) b;
			int code = Util.compare(ma.getSelector(), mb.getSelector());
			if (code != 0)
				return code;
			return Util.compare(ma.getMethodDescriptor(), mb.getMethodDescriptor());
		}
	};
	Object[] sortedA = Util.sortCopy(listA, comparer);
	Object[] sortedB = Util.sortCopy(listB, comparer);
	for (int i = 0; i < listA.length; ++i) {
		if (!compare((IBinaryMethod) sortedA[i], (IBinaryMethod) sortedB[i]))
			return false;
	}
	return true;
}
protected static boolean compare(IBinaryNestedType[] listA, IBinaryNestedType[] listB) {
	if (listA == listB)
		return true;
	if (listA == null || listB == null)
		return false;
	if (listA.length != listB.length)
		return false;
	for (int i = 0; i < listA.length; ++i) {
		if (!compare((IBinaryNestedType) listA[i], (IBinaryNestedType) listB[i]))
			return false;
	}
	return true;
}
public static boolean compare(IBinaryField a, IBinaryField b) {
	if (a == b)
		return true;
	if (!CharOperation.equals(a.getName(), b.getName()))
		return false;
	if (!CharOperation.equals(a.getTypeName(), b.getTypeName()))
		return false;
	if (a.getModifiers() != b.getModifiers())
		return false;
	if (!Util.equalOrNull(a.getConstant(), b.getConstant()))
		return false;
	return true;
}
public static boolean compare(IBinaryMethod a, IBinaryMethod b) {
	if (a == b)
		return true;
	if (!CharOperation.equals(a.getSelector(), b.getSelector()))
		return false;
	if (!CharOperation.equals(a.getMethodDescriptor(), b.getMethodDescriptor()))
		return false;
	if (a.getModifiers() != b.getModifiers())
		return false;
	if (!CharOperation.equals(a.getExceptionTypeNames(), b.getExceptionTypeNames()))
		return false;
	return true;
}
protected static boolean compare(IBinaryNestedType a, IBinaryNestedType b) {
	if (a == b)
		return true;
	if (!CharOperation.equals(a.getName(), b.getName()))
		return false;
	if (a.getModifiers() != b.getModifiers())
		return false;
	if (!CharOperation.equals(a.getEnclosingTypeName(), b.getEnclosingTypeName()))
		return false;
	return true;
}
public static boolean compare(IBinaryType a, IBinaryType b) {
	if (a == b)
		return true;
	if (!CharOperation.equals(a.getName(), b.getName()))
		return false;
	if (a.getModifiers() != b.getModifiers())
		return false;
	if (!CharOperation.equals(a.getSuperclassName(), b.getSuperclassName()))
		return false;
	if (!CharOperation.equals(a.getInterfaceNames(), b.getInterfaceNames()))
		return false;
	if (!compare(a.getFields(), b.getFields()))
		return false;
	if (!compare(a.getMethods(), b.getMethods()))
		return false;
	if (!compare(a.getMemberTypes(), b.getMemberTypes()))
		return false;
	return true;
}
/**
 * Converts from the compiler's name representation to ours.
 * i.e., converts from char[] some/pkg/Type$Subtype to
 * String some.pkg.Type$Subtype
 */
public static String convertTypeName(char[] vmName) {
	char[] builderName = new char[vmName.length];
	for (int i = vmName.length; --i >=0;) {
		if (vmName[i] == '/') {
			builderName[i] = '.';
		} else {
			builderName[i] = vmName[i];
		}
	}
	return new String(builderName);
}
/**
 * Returns the constructor of the type, corresponding to the given constructor handle signature,
 * or null if there is none which matches.
 */
public static IBinaryMethod getConstructor(IBinaryType type, String handleSig) {
	IBinaryMethod[] methods = type.getMethods();
	if (methods != null) {
		char[] sig = handleSig.replace('.', '/').toCharArray();
		for (int i = 0; i < methods.length; i++) {
			IBinaryMethod m = (IBinaryMethod) methods[i];
			if (m.isConstructor() && CharOperation.startsWith(m.getMethodDescriptor(), sig))
				return m;
		}
	}
	return null;
}
/**
 * Returns the non-state-specific constructor handle for an IBinaryMethod, given the containing 
 * non-state-specific type handle.
 */
public static IConstructor getConstructorHandle(IBinaryMethod method, ClassOrInterfaceHandleImpl type) {
	char[] sig = method.getMethodDescriptor();
	sig = CharOperation.subarray(sig, 0, sig.length - 1); // Trim off V trailer
	String convertedSig = new String(sig).replace('/', '.');
	return new ConstructorImpl(type, convertedSig);
}
/**
 * Returns the declared name (name as it appears in the source) for the given type.
 */
public static char[] getDeclaredName(IBinaryNestedType type) {
	char[] name = type.getName();
	int lastDot = CharOperation.lastIndexOf('/', name);
	if (lastDot != -1) {
		name = CharOperation.subarray(name, lastDot + 1, name.length);
	}
	int lastDollar = CharOperation.lastIndexOf('$', name);
	if (lastDollar != -1) {
		name = CharOperation.subarray(name, lastDollar + 1, name.length);
	}
	return name;
}
/**
 * Returns the declared name (name as it appears in the source) for the given type.
 */
public static char[] getDeclaredName(IBinaryType type) {
	char[] name = type.getName();
	int lastDot = CharOperation.lastIndexOf('/', name);
	if (lastDot != -1) {
		name = CharOperation.subarray(name, lastDot + 1, name.length);
	}
	int lastDollar = CharOperation.lastIndexOf('$', name);
	if (lastDollar != -1) {
		name = CharOperation.subarray(name, lastDollar + 1, name.length);
	}
	return name;
}
/**
 * Returns the name of the enclosing type, or null.
 */
public static char[] getEnclosingTypeName(IBinaryType type) {
	IBinaryNestedType t = getInnerClassEntry(type);
	return t == null ? null : t.getEnclosingTypeName();
}
/**
 * Returns the field of the type, with the given name,
 * or null if there is none which matches.
 */
public static IBinaryField getField(IBinaryType type, String name) {
	IBinaryField[] fields = type.getFields();
	if (fields != null) {
		char[] nameChars = name.toCharArray();
		for (int i = 0; i < fields.length; i++) {
			IBinaryField f = (IBinaryField) fields[i];
			if (CharOperation.equals(f.getName(), nameChars))
				return f;
		}
	}
	return null;
}
/**
 * Returns the non-state-specific field handle for an IBinaryField, given the containing 
 * non-state-specific type handle.
 */
public static IField getFieldHandle(IBinaryField field, ClassOrInterfaceHandleImpl type) {
	return type.getFieldHandle(new String(field.getName()));
}
/**
 * Returns the fully qualified name of the given binary type
 */
public static String getFullyQualifiedName(IBinaryType type) {
	return convertTypeName(type.getName());
}
/**
 * Returns the inner class entry representing this type, or null if not applicable.
 */
protected static IBinaryNestedType getInnerClassEntry(IBinaryType type) {
	IBinaryNestedType[] inners = type.getMemberTypes();
	if (inners != null) {
		char[] name = type.getName();
		for (int i = 0, max = inners.length; i < max; ++i) {
			IBinaryNestedType t = (IBinaryNestedType) inners[i];
			if (CharOperation.equals(t.getName(), name)) {
				return t;
			}
		}
	}
	return null;
}
/**
 * Returns the names of inner types in the inner class entries which have this type
 * as their outer type.  May include both local (pre JDK1.2) and member types.
 */
public static char[][] getInnerTypes(IBinaryType type) {
	IBinaryNestedType[] nested = type.getMemberTypes();
	if (nested == null) {
		return new char[0][];
	} else {
		char[][] result = new char[nested.length][];
		int count = 0;
		for (int i = 0; i < nested.length; ++i) {
			IBinaryNestedType t = nested[i];
			if (CharOperation.equals(t.getEnclosingTypeName(), type.getName())) {
				result[count++] = t.getName();
			}
		}
		if (count < result.length) {
			System.arraycopy(result, 0, result = new char[count][], 0, count);
		}
		return result;
	}
}
/**
 * Returns the method of the type, corresponding to the given method handle signature,
 * or null if there is none which matches.
 */
public static IBinaryMethod getMethod(IBinaryType type, String handleSig) {
	IBinaryMethod[] methods = type.getMethods();
	if (methods != null) {
		char[] sig = handleSig.replace('.', '/').toCharArray();
		int paren = CharOperation.indexOf('(', sig);
		if (paren == -1)
			return null;
		char[] sel = CharOperation.subarray(sig, 0, paren);
		char[] desc = CharOperation.subarray(sig, paren, sig.length);
		for (int i = 0; i < methods.length; i++) {
			IBinaryMethod m = (IBinaryMethod) methods[i];
			if (!m.isConstructor() && CharOperation.equals(m.getSelector(), sel) && CharOperation.startsWith(m.getMethodDescriptor(), desc))
				return m;
		}
	}
	return null;
}
/**
 * Returns the non-state-specific method handle for an IBinaryMethod, given the containing 
 * non-state-specific type handle.
 */
public static IMethod getMethodHandle(IBinaryMethod method, ClassOrInterfaceHandleImpl type) {

	/**
	 * Note that DC method signatures have the following grammar rule:
	 *  - MethodSignature: selector ( ParameterDescriptor* )
	 * Whereas the VM signature format in IBinaryMethod is:
	 *  - MethodDescriptor: ( ParameterDescriptor* ) ReturnDescriptor
	 */

	char[] sig = method.getMethodDescriptor();
	int i = CharOperation.lastIndexOf(')', sig);
	if (i != -1)
		sig = CharOperation.subarray(sig, 0, i + 1); // Trim return type

	sig = CharOperation.concat(method.getSelector(), sig);
	String convertedSig = new String(sig).replace('/', '.');
	return new MethodImpl(type, convertedSig);
}
/**
 * Returns a type handle for the given type name.  The name
 * must be in IBinaryType format.
 * The TypeStructureEntry is passed in case the default package needs to be determined
 * relative to the referring type.
 */
public static IType getType(StateImpl state, TypeStructureEntry referringType, char[] name) {
	/* null is a valid type name */
	if (name == null) {
		return null;
	}
	return state.typeNameToHandle(referringType, new String(name).replace('/', '.'));
}
/**
 * Returns whether the type is an anonymous type (local, without a name).
 */
public static boolean isAnonymous(IBinaryType type) {
	/* TBD: Don't have access to declared name, but assume it's anonymous if it's local. */
	return isLocal(type);
}
/**
 * Returns whether the type is a local type (not a top-level or member type).
 */
public static boolean isLocal(IBinaryType type) {
	/* As of JDK 1.2, the inner class entry for local types have null for the enclosing type. */
	IBinaryNestedType t = getInnerClassEntry(type);
	return t != null && t.getEnclosingTypeName() == null;
}
/**
 * Returns whether the type is a package member (not a nested type).
 */
public static boolean isPackageMember(IBinaryType type) {
	return getInnerClassEntry(type) == null;
}
}
