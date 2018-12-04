class Example{
	public void example() {
		for (int i = 0; i < 10; i++) {
		}
		int a = 10;
		while (a-- > 0) { System.out.println(a); }
		do { a += 2;
		System.out.println(a); } while(a < 50);
	}
}


class Example {
	public String example(int a) {
		if (a < 0) { 
			throw new IllegalArgumentException(); }
		if (a == 0) { return null; }
		if (false) {}
		if (a % 3 == 0) {
			System.out.println("fizz"); }
		if (a % 5 == 0) { System.out.println("buzz"); return ""; }
		return Integer.toString(a);
	}
}


class Example {
	Runnable emptyLambda = () -> {};
	Runnable emptyLambda2 = () -> {
	};
	Runnable tinyLambda = () -> { doSomething(); };
	Runnable smallLambda = () -> { doFirstThing(); doSecondThing(); };
}


class Example {
	static {
	}
	
	void foo() {
		if (true) {} else {}
		synchronized(this) {}
		try {} finally {}
		
		labeled:{}
	}
}


public class Example {
	private int something;
	public int getSomething() { return something; }
	public void setSomehing(int something) { this.something = something; }
	public void doNoting() {}
	public void doOneThing() { System.out.println();
	}
	public void doMoreThings() { something = 4; doOneThing(); doOneThing(); }
}


public class EmptyClass{}
public class TinyClass{ 
	int a; }
public class SmallClass{ int a; String b; }


public class AnonymousClasses {
	EmptyClass emptyAnonymous = new EmptyClass() {
	};
	TinyClass tinyAnonymous = new TinyClass() { String b; };
	Object o = new SmallClass() { int a; int getA() { return a; } };
}


public enum EmptyEnum {}
public enum TinyEnum{ A;
}
public enum SmallEnum{ VALUE(0); SmallEnum(int val) {}; }


public enum EnumConstants {
	EMPTY {
	},
	TINY { int getVal() { return 2; }},
	SMALL { int val = 3; int getVal() { return 3; }};
	int getVal() { return 1; }
}


public @interface EmptyInterface {}
public @interface TinyInterface { 
	void run(); }
public @interface SmallInteface { int toA(); String toB(); }
