package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;

/**
 * A super reference inside a code snippet denotes a reference to the super type of 
 * the remote receiver object (i.e. the one of the context in the stack frame). This is 
 * used to report an error through JavaModelException according to the fact that super
 * reference are not supported in code snippet.
 */
public class CodeSnippetSuperReference extends SuperReference implements EvaluationConstants, InvocationSite {
	EvaluationContext evaluationContext;
	
public CodeSnippetSuperReference(int pos, int sourceEnd, 	EvaluationContext evaluationContext) {
	super(pos, sourceEnd);
	this.evaluationContext = evaluationContext;
}

public TypeBinding resolveType(BlockScope scope) {
		scope.problemReporter().cannotUseSuperInCodeSnippet(this.sourceStart, this.sourceEnd); //$NON-NLS-1$
		return null;
}
public boolean isSuperAccess(){
	return false;
}
public boolean isTypeAccess(){
	return false;
}
public void setDepth(int depth){
}
public void setFieldIndex(int index){
}
}

