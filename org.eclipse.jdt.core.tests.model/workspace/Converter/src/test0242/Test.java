package test0242;
import java.util.*;
class T {
	public int f = 0;
}

public class Test extends T {
	public int foo() {
		class X {
			int foo() {
				return Test.super.f;
			}
		}
		return new X().foo();
	}
}
