package test0556;

import java.util.Vector;

public class A {
	AA aa;
	void foo() {    
		(aa.bar()).size();
	}
}
class AA {
	Vector bar() { return new Vector(1); };
}