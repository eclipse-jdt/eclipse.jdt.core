package org.eclipse.jdt.compiler.apt.tests.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
public @interface Generated {
  String from() default "";
  String generator() default "";
}