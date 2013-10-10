package org.eclipse.jdt.compiler.apt.tests.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_USE)
public @interface TFooContainer {
	TFoo [] value();
}
