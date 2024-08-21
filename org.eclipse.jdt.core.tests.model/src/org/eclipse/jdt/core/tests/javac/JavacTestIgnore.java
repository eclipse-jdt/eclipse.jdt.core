package org.eclipse.jdt.core.tests.javac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JavacTestIgnore {
	public static String VALID_ALTERNATIVE_IMPL = "VALID_ALTERNATIVE_IMPL";
	public static String TESTS_SPECIFIC_RESULT_FOR_UNDEFINED_BEHAVIOR = "TESTS_SPECIFIC_RESULT_FOR_UNDEFINED_BEHAVIOR";
	public static String JDT_RECOVERS_FROM_BAD_INPUTS = "JDT_RECOVERS_FROM_BAD_INPUTS";
	public static String JDT_VIOLATES_SPEC = "JDT_VIOLATES_SPEC";
	public static String JDT_BEHAVIOR_STRANGE = "JDT_BEHAVIOR_STRANGE";
	public String cause();
}
