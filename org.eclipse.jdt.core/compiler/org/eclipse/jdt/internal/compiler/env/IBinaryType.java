package org.eclipse.jdt.internal.compiler.env;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public interface IBinaryType extends IGenericType {

	char[][] NoInterface = new char[0][];
	IBinaryNestedType[] NoNestedType = new IBinaryNestedType[0];
	IBinaryField[] NoField = new IBinaryField[0];
	IBinaryMethod[] NoMethod = new IBinaryMethod[0];
/**
 * Answer the resolved name of the enclosing type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the receiver is a top level type.
 *
 * For example, java.lang.String is java/lang/String.
 */

char[] getEnclosingTypeName();
/**
 * Answer the receiver's fields or null if the array is empty.
 */

IBinaryField[] getFields();
/**
 * Answer the resolved names of the receiver's interfaces in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 */

char[][] getInterfaceNames();
/**
 * Answer the receiver's nested types or null if the array is empty.
 *
 * This nested type info is extracted from the inner class attributes.
 * Ask the name environment to find a member type using its compound name.
 */

// NOTE: The compiler examines the nested type info & ignores the local types
// so the local types do not have to be included.

IBinaryNestedType[] getMemberTypes();
/**
 * Answer the receiver's methods or null if the array is empty.
 */

IBinaryMethod[] getMethods();
/**
 * Answer the resolved name of the type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, java.lang.String is java/lang/String.
 */

char[] getName();
/**
 * Answer the resolved name of the receiver's superclass in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if it does not have one.
 *
 * For example, java.lang.String is java/lang/String.
 */

char[] getSuperclassName();

/**
 * Answer the source file attribute, or null if none.
 *
 * For example, "String.java"
 */

char[] sourceFileName();
}
