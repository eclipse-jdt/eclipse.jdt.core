package my.mod;

import my.annot.*;

public class Main3 implements @MyAnnotation Runnable {

	public void run() {
	}

	public static @MyAnnotation("1") int method1() {
		return 0;
	}

	public static @MyAnnotation("2") Integer method2() {
		return null;
	}

	public static @MyAnnotation("3") String method3() {
		return null;
	}

	public static String @MyAnnotation("4") [] method4() {
		return null;
	}

	public static java.util.@MyAnnotation("5") Set<@MyAnnotation("6") String> method5() {
		return null;
	}

	public static <@MyAnnotation("7") T extends @MyAnnotation("8") String> @MyAnnotation("9") T method6() {
		return null;
	}

	public static java.util.@MyAnnotation("10") Set<@MyAnnotation("11") ? extends @MyAnnotation("12") Number> method7() {
		return null;
	}

	public static <@MyAnnotation("13") S extends Number & Runnable> @MyAnnotation("14") S method8() {
		return null;
	}
}