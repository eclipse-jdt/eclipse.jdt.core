package org.eclipse.jdt.core.tests.javac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(JavacFailReasons.class)
public @interface JavacFailReason {
	public static String VALID_ALTERNATIVE_IMPL = "VALID_ALTERNATIVE_IMPL";
	public static String TESTS_SPECIFIC_RESULT_FOR_UNDEFINED_BEHAVIOR = "TESTS_SPECIFIC_RESULT_FOR_UNDEFINED_BEHAVIOR";
	public static String JDT_RECOVERS_FROM_BAD_INPUTS = "JDT_RECOVERS_FROM_BAD_INPUTS";
	public static String JDT_VIOLATES_SPEC = "JDT_VIOLATES_SPEC";
	public static String JDT_BEHAVIOR_STRANGE = "JDT_BEHAVIOR_STRANGE";

	// For some reason, javac cannot handle this case correctly
	public static String JAVAC_DEFICIENCY= "JAVAC_DEFICIENCY";
	public static String JAVAC_TREE_NOT_IDENTICAL_MISC= "JAVAC_TREE_NOT_IDENTICAL_MISC";
	public static String JAVAC_TREE_NOT_IDENTICAL_STMTS_RECOVERED= "JAVAC_TREE_NOT_IDENTICAL_STMTS_RECOVERED";
	public static String JAVAC_NOT_SETTING_MALFORMED= "JAVAC_NOT_SETTING_MALFORMED";
	public static String JAVAC_PROBLEM_MAPPING= "JAVAC_PROBLEM_MAPPING";
	public static String JAVAC_COMMENT_MAPPING= "JAVAC_COMMENT_MAPPING";
	public static String JAVAC_TREE_NOT_IDENTICAL_SRC_RANGE= "JAVAC_TREE_NOT_IDENTICAL_SRC_RANGE";

	// Too much information when using a focal position. Tests don't like it
	public static String JAVAC_FOCAL_POSITION= "JAVAC_FOCAL_POSITION";
	public static String BINDING_KEY= "BINDING_KEY";
	public String cause();
}
