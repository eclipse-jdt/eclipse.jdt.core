package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public interface BindingIds {
	final int FIELD = 1;
	final int LOCAL = 2;
	final int VARIABLE = FIELD | LOCAL;
	final int TYPE = 4;
	final int METHOD = 8;
	final int PACKAGE = 16;
	final int IMPORT = 32;
}
