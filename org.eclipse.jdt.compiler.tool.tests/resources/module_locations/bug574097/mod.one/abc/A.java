package abc;
public class A {
	// Without this being part of -classpath, the following
	// should be reported, even if it's part of --processor-module-path
	test.processor.TestProcessor prc = null;
}
