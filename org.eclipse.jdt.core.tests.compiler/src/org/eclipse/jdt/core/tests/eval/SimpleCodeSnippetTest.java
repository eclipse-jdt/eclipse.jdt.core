package org.eclipse.jdt.core.tests.eval;

import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.InstallException;


public class SimpleCodeSnippetTest
    extends SimpleTest {
    public char[] getCodeSnippetSource() {

        return buildCharArray(new String[] { "1 + 1" });
    }

    public static void main(String[] args)
                     throws TargetException, InstallException {

        SimpleCodeSnippetTest test = new SimpleCodeSnippetTest();
        test.runCodeSnippet();
    }

    void runCodeSnippet()
                 throws TargetException, InstallException {
        this.startEvaluationContext();

        char[] snippet = getCodeSnippetSource();
        INameEnvironment env = getEnv();
        this.context.evaluate(snippet, env, null, this.requestor, 
                              getProblemFactory());
        this.stopEvaluationContext();
    }
}