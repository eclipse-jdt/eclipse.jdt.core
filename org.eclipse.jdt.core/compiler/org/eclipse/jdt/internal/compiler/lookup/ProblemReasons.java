package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public interface ProblemReasons {
	final int NoError = 0;
	final int NotFound = 1;
	final int NotVisible = 2;
	final int Ambiguous = 3;
	final int InternalNameProvided = 4; // used if an internal name is used in source
	final int InheritedNameHidesEnclosingName = 5;
	final int NonStaticReferenceInConstructorInvocation = 6;
	final int NonStaticReferenceInStaticContext = 7;
}
