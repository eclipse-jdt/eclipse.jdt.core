package org.eclipse.jdt.compiler.apt.tests;
import org.eclipse.jdt.compiler.apt.tests.annotations.Foo;
import org.eclipse.jdt.compiler.apt.tests.annotations.TFoo;

@Foo
public class JEP120_5 {
	@TFoo @TFoo @Foo String field;
}