package org.eclipse.jdt.compiler.apt.tests;
import org.eclipse.jdt.compiler.apt.tests.annotations.Goo;
import org.eclipse.jdt.compiler.apt.tests.annotations.GooNonContainer;

@GooNonContainer({@Goo, @Goo})
public class JEP120_2 {
	// ...
}