package org.eclipse.jdt.internal.compiler.env;

public interface IGenericMethod {
	/**
	 * Answer an int whose bits are set according the access constants
	 * defined by the VM spec.
	 */

	// We have added AccDeprecated & AccSynthetic.

	int getModifiers();
	/**
	 * Answer the name of the method.
	 *
	 * For a constructor, answer <init> & <clinit> for a clinit method.
	 */

	char[] getSelector();
	boolean isConstructor();
}
