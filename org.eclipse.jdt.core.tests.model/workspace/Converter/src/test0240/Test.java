package test0240;
import java.util.*;
public class Test {
	public int foo() {
		class X {
			int foo() {
				return Test.this.f;
			}
		}
		return new X().foo();
	}
	
	public int f = 0;
}
