package org.eclipse.jdt.internal.compiler.env;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;

public interface IBinaryField extends IGenericField {
	/**
	 * 
	 * @return org.eclipse.jdt.internal.compiler.Constant
	 */
	Constant getConstant();
	/**
	 * Answer the resolved name of the receiver's type in the
	 * class file format as specified in section 4.3.2 of the Java 2 VM spec.
	 *
	 * For example:
	 *   - java.lang.String is Ljava/lang/String;
	 *   - an int is I
	 *   - a 2 dimensional array of strings is [[Ljava/lang/String;
	 *   - an array of floats is [F
	 */

	char[] getTypeName();
}
