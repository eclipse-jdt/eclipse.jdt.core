package org.eclipse.jdt.internal.compiler.env;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

// clinit methods (synthetics too?) can be returned from IBinaryType>>getMethods()
// BUT do not have to be... the compiler will ignore them when building the binding.
// The synthetic argument of a member type's constructor (ie. the first arg of a non-static
// member type) is also ignored by the compiler, BUT in this case it must be included
// in the constructor's signature.

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
