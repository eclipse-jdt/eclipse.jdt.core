package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public interface EvaluationConstants {
	public static final char[] CODE_SNIPPET_CLASS_NAME_PREFIX = "CodeSnippet_"/*nonNLS*/.toCharArray();
	public static final char[] GLOBAL_VARS_CLASS_NAME_PREFIX = "GlobalVariables_"/*nonNLS*/.toCharArray();
	public static final char[] PACKAGE_NAME = "org.eclipse.jdt.internal.eval.target"/*nonNLS*/.toCharArray();
	public static final char[] CODE_SNIPPET_NAME = "org/eclipse/jdt/internal/eval/target/CodeSnippet"/*nonNLS*/.toCharArray();
	public static final char[] ROOT_CLASS_NAME = "CodeSnippet"/*nonNLS*/.toCharArray();
	public static final String ROOT_FULL_CLASS_NAME = new String(PACKAGE_NAME) + "."/*nonNLS*/ + new String(ROOT_CLASS_NAME);
	public static final char[] SETRESULT_SELECTOR = "setResult"/*nonNLS*/.toCharArray();
	public static final char[] SETRESULT_ARGUMENTS = "Ljava.lang.Object;Ljava.lang.Class;"/*nonNLS*/.toCharArray();
	public static final char[][] ROOT_COMPOUND_NAME = CharOperation.arrayConcat(CharOperation.splitOn('.', PACKAGE_NAME), ROOT_CLASS_NAME);
	public static final String RUN_METHOD = "run"/*nonNLS*/;
	public static final String RESULT_VALUE_FIELD = "resultValue"/*nonNLS*/;
	public static final String RESULT_TYPE_FIELD = "resultType"/*nonNLS*/;
	public final static char[] LOCAL_VAR_PREFIX = "val$"/*nonNLS*/.toCharArray();
	public final static char[] DELEGATE_THIS = "val$this"/*nonNLS*/.toCharArray();
	
}
