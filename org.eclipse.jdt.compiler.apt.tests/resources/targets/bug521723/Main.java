package targets.bug521723;

public class Main {
}

interface I {
	private int foo1(int i) { return i; }
	default int foo2(int i) {return foo1(i); }
	public default void foo3() {}
	static void foo4() {}
	private static void foo5() {}
	public static void foo6() {}
}
