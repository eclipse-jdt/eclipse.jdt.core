package p1.p2;

import java.util.Enumeration;

/**
 * Class X javadoc 
 */
public class X {
	/**
	 * Javadoc for initializer
	 */
	static {
	}
	
	 /**
	  * Javadoc for field f 
	  */
	public int f;
	
	/**
	 * Javadoc for method foo
	 */
	public void foo(int i, long l, String s) {
	}
	
	/**
	 * Javadoc for member type A
	 */
	public class A {
	}

	/**
	 * Javadoc for constructor X(int)
	 */
	X(int i) {
	}
	
	/**
	 * Javadoc for f3
	 */
	/*
	 * Not a javadoc comment
	 */
	/**
	 * Real javadoc for f3
	 */
	public String f3;
	
	public int f2;
	
	public void foo2() {
	}
	
	public class B {
	}

	X() {
	}
	
	{
	}
	
	public void foo(Enumeration enumeration) {
		if (enumeration.hasMoreElements()) {
			System.out.println("Has more elements");
		}
	}
}
