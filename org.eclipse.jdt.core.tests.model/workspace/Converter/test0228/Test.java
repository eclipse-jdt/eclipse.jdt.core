package test0228;
import java.util.*;
public class Test {
	public static int foo() {
		return  4;
	}
	
	public int bar() {
		return test0228.Test.foo();
	}
}