package org.eclipse.jdt.internal.compiler.lookup;

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
