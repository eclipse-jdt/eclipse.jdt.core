package org.eclipse.jdt.internal.compiler.env;

public interface IGenericField {
	/**
	 * Answer an int whose bits are set according the access constants
	 * defined by the VM spec.
	 */

	// We have added AccDeprecated & AccSynthetic.

	int getModifiers();
	/**
	 * Answer the name of the field.
	 */

	char[] getName();
}
