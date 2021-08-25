package abc.internal;
public class A {
	// Without this being part of -classpath, the following
	// should be reported, even if it's part of --processor-module-path
	org.eclipse.jdt.compiler.apt.tests.processors.elements.Java12ElementProcessor prc = null;
}
