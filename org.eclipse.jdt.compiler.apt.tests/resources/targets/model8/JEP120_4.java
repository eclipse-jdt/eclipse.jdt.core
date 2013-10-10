package org.eclipse.jdt.compiler.apt.tests;
import org.eclipse.jdt.compiler.apt.tests.annotations.Foo;
import org.eclipse.jdt.compiler.apt.tests.annotations.FooNonContainer;

@FooNonContainer({@Foo, @Foo})
public class JEP120_4 {
	// ...
}