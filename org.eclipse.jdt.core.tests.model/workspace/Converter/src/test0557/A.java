package test0557;

import java.util.Vector;
public class A {
	AA aa;
	void foo() {
		(aa.bar()).get(0);
		// comment
		if (true) {
			System.out.println("Hello: " + toString()); //$NON-NLS-1$
		}
	}
}
class AA {
	Vector bar() {
		return new Vector(1);
	}
}