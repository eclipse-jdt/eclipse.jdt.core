package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.IProblem;

/**
 * A callback interface for receiving code snippet evaluation results.
 */
public interface IRequestor {
/**
 * @see ICodeSnippetRequestor
 */
boolean acceptClassFiles(ClassFile[] classFiles, char[] codeSnippetClassName);
/**
 * @see ICodeSnippetRequestor
 */
void acceptProblem(IProblem problem, char[] fragmentSource, int fragmentKind);
}
