package test0197;
import java.io.File;
class A {
	public File getFile() {
		return null;
	}
	public void useFile(File file) {
	}
}

public class Test {
	public void foo() {
		A a= new A();
		/*]*/a.getFile()/*[*/.getName();
	}
}
