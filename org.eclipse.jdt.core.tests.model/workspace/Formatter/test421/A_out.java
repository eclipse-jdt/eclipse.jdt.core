public class A {
	void foo(String filename) {
		Runtime.getRuntime().loadLibrary( filename );
		System.loadLibrary( filename );
	}
}