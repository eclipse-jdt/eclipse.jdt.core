package test0200;
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
		int f= new A[/*]*/1 + 2/*[*/].length;
	}
}