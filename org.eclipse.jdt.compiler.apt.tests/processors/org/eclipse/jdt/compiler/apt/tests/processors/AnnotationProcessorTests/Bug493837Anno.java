package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Bug493837Anno {
	Class<?>[] value();
}
