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
package org.eclipse.jdt.internal.core.search.indexing;

public interface IIndexConstants {

	/* index encoding */
	char[] REF= "ref".toCharArray(); //$NON-NLS-1$
	char[] METHOD_REF= "methodRef".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_REF= "constructorRef".toCharArray(); //$NON-NLS-1$
	char[] SUPER_REF = "superRef".toCharArray(); //$NON-NLS-1$
	char[] TYPE_DECL = "typeDecl".toCharArray(); //$NON-NLS-1$
	char[] METHOD_DECL= "methodDecl".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_DECL= "constructorDecl".toCharArray(); //$NON-NLS-1$
	char[] FIELD_DECL= "fieldDecl".toCharArray(); //$NON-NLS-1$
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	char[][] COUNTS= 
		new char[][] { new char[] {'/', '0'}, new char[] {'/', '1'}, new char[] {'/', '2'}, new char[] {'/', '3'}, new char[] {'/', '4'},
			new char[] {'/', '5'}, new char[] {'/', '6'}, new char[] {'/', '7'}, new char[] {'/', '8'}, new char[] {'/', '9'}
	};
	char CLASS_SUFFIX = 'C';
	char INTERFACE_SUFFIX = 'I';
	char ENUM_SUFFIX = 'E';
	char ANNOTATION_TYPE_SUFFIX = 'A';
	char TYPE_SUFFIX = 0;
	char SEPARATOR= '/';

	char[] ONE_STAR = new char[] {'*'};
	char[][] ONE_STAR_CHAR = new char[][] {ONE_STAR};

	// used as special marker for enclosing type name of local and anonymous classes
	char[] ONE_ZERO = new char[] {'0'}; 
	char[][] ONE_ZERO_CHAR = new char[][] {ONE_ZERO};

	int PKG_REF_PATTERN = 1;
	int PKG_DECL_PATTERN = 2;
	int TYPE_REF_PATTERN = 4;
	int TYPE_DECL_PATTERN = 8;
	int SUPER_REF_PATTERN = 16;
	int CONSTRUCTOR_PATTERN = 32;
	int FIELD_PATTERN = 64;
	int METHOD_PATTERN = 128;
	int OR_PATTERN = 256;
	int LOCAL_VAR_PATTERN = 512;
}
