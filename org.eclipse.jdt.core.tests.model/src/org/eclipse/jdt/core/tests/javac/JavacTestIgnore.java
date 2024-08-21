package org.eclipse.jdt.core.tests.javac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JavacTestIgnore {
	public static String VALID_ALTERNATIVE = "VALID_ALTERNATIVE";
	public static String IRRELEVANT = "IRRELEVANT";
	public static String JDT_BEHAVIOR_STRANGE = "STRANGE";

	public String cause();
}
