package test0196;
import java.util.*;
public class Test {
	class Inner {
	}
	public void foo() {
		Inner inner= null;
		boolean b;
		b= /*]*/inner instanceof Inner/*[*/;
	}
}