package test0409;

import java.lang.*;
import java.lang.Object;

class Super extends java.lang.Object {
	int k;
	
	int foo2() {
		return 0;
	}
}
/**
 *
 */
public class A extends Super implements Cloneable {

	public static final int i = 0;

	{
		int j;
	}
	
	int[] foo(float f) throws Exception {
		assert f > 0 : "it works";
		{
		}
		test: for (int i = 0; i < 10; i++) {
			if (f > 0) {
				break test;
			} else {
				continue test;
			}
		}
		Object o = new Object();
		do {
			o = null;
		} while (o != null);
		;
		if (o == null) {
			throw new Exception();
		}
		int j = 9;
		switch(j) {
			case 4 :
				break;
			default : 
				return null;
		}
		synchronized(o) {
		}
		try {
			System.out.println();
		} catch(Exception e) {
			System.out.println();
		} finally {
			j = (int) 'c';
		}
		while (j > 0) {
			--j;
		}
		int[] tab = { 1, 2, 3, 4};
		j = tab[3];
		boolean b = (true && false);
		
		j = o instanceof Object ? 4 : 5 + 9;
		
		j = k;
		
		j = foo2();
		
		o = this.bar();
		
		o = A.class;
		
		return new int[] {};
	}

	Object bar() {
		class C {
		}
		return new C() {};
	}
		
	A() {
		this(0);
	}
	
	A(int i) {
		super();
	}
	
	class B {
	}
}	