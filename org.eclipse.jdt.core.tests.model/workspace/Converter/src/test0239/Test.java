package test0239;

import java.util.*;
class T {
	int bar() {
		return 0;
	}
}

public class Test extends T {
	public int foo() {
		class X {
			int foo() {
				return Test.super.bar();
			}
		}
		return new X().foo();
	}
	
	public int bar() {
		return 0;
	}
}
