package org.eclipse.jdt.compiler.apt.tests;
import org.eclipse.jdt.compiler.apt.tests.annotations.Foo;
import org.eclipse.jdt.compiler.apt.tests.annotations.FooContainer;

@FooContainer({@Foo, @Foo})
public class JEP120_1 {
	// ...
}