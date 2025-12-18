package org.eclipse.jdt.compiler.apt.tests.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public @interface Value {
	@Documented
	@Target(ElementType.TYPE)
	@interface Immutable {

		boolean singleton() default false;

		boolean intern() default false;

		boolean copy() default true;

		boolean prehash() default false;

		boolean lazyhash() default false;

		boolean builder() default true;
	}
	@Target(ElementType.TYPE)
	@interface Style {
		String[] get() default "get*";
		String init() default "*";
	}
	@Target(ElementType.TYPE)
	@interface Builder {}
}
