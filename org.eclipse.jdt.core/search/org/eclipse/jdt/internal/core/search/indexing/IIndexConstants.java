package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.search.processing.*;

public interface IIndexConstants extends IJobConstants {

	/* index encoding */
	char[] REF= "ref/".toCharArray(); //$NON-NLS-1$
	char[] FIELD_REF= "fieldRef/".toCharArray(); //$NON-NLS-1$
	char[] METHOD_REF= "methodRef/".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_REF= "constructorRef/".toCharArray(); //$NON-NLS-1$
	char[] TYPE_REF= "typeRef/".toCharArray(); //$NON-NLS-1$
	char[] SUPER_REF = "superRef/".toCharArray(); //$NON-NLS-1$
	char[] TYPE_DECL = "typeDecl/".toCharArray(); //$NON-NLS-1$
	int 	TYPE_DECL_LENGTH = 9;
	char[] CLASS_DECL= "typeDecl/C/".toCharArray(); //$NON-NLS-1$
	char[] INTERFACE_DECL= "typeDecl/I/".toCharArray(); //$NON-NLS-1$
	char[] METHOD_DECL= "methodDecl/".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_DECL= "constructorDecl/".toCharArray(); //$NON-NLS-1$
	char[] FIELD_DECL= "fieldDecl/".toCharArray(); //$NON-NLS-1$
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	char[][] COUNTS= 
		new char[][] { new char[] {'0'}, new char[] {'1'}, new char[] {'2'}, new char[] {'3'}, new char[] {'4'}, new char[] {'5'}, new char[] {'6'}, new char[] {'7'}, new char[] {'8'}, new char[] {'9'}
	};
	char CLASS_SUFFIX = 'C';
	char INTERFACE_SUFFIX = 'I';
	char TYPE_SUFFIX = 0;
	char SEPARATOR= '/';

	char[] ONE_STAR = new char[] {'*'};
	char[] NO_CHAR = new char[0];
	char[][] NO_CHAR_CHAR = new char[0][];
}
