package test0197;
import java.util.*;
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
		A a= null;
		/*]*/a.getFile()/*[*/.getName();
	}
}
