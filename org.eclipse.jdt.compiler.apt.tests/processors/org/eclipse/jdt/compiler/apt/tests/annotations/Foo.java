package org.eclipse.jdt.compiler.apt.tests.annotations;
import java.lang.annotation.Repeatable;

@Repeatable(FooContainer.class)
public @interface Foo {
	// ...
}

