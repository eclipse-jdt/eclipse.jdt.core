package test0238;
import java.util.*;

public class Test {
	public int foo() {
		class X {
			int foo() {
				return Test.this.bar();
			}
		}
		return new X().foo();
	}
	
	public int bar() {
		return 0;
	}
}
