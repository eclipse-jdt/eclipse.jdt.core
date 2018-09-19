package targets.model9.q;

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;

public class Main {
	@Type("f")
	public SomeClassWithAnAnnotation someField;
}

@Type("c")
class SomeClassWithAnAnnotation {
}