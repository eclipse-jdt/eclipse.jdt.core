package org.eclipse.jdt.internal.compiler.env;

public interface IBinaryMethod extends IGenericMethod {
/**
 * Answer the resolved names of the exception types in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 */

char[][] getExceptionTypeNames();
/**
 * Answer the receiver's method descriptor which describes the parameter &
 * return types as specified in section 4.3.3 of the Java 2 VM spec.
 *
 * For example:
 *   - int foo(String) is (Ljava/lang/String;)I
 *   - Object[] foo(int) is (I)[Ljava/lang/Object;
 */

char[] getMethodDescriptor();
/**
 * Answer whether the receiver represents a class initializer method.
 */

boolean isClinit();
}
