package test0193;
import java.util.*;
public class Test {
	class Inner {
	}
	public void foo() {
		Inner inner= /*]*/new Inner();/*[*/
	}
}