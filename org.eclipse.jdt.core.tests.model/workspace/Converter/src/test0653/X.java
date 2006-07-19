package test0653;

import java.util.ArrayList;

public class X extends A {

	public X(String name) {
		super(name);
	}

	public static void main(String[] args) {
		new X("SimpleTest").test();
	}

	public void test() {
		System.out.println(this.name.length());
		ArrayList arrayList = new ArrayList(); // BREAKPOINT
		for (int i = 0; i < 100; i++) {
			arrayList.add(new Integer(i));
		}
	}
}