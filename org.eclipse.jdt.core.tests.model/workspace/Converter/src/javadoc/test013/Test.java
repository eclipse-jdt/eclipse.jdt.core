package javadoc.test013;
import java.util.*;
public class Test {
	/**
	 * Javadoc comment
	 */
	public static void main(String[] args) {
		/* method main */
		System.out.println("Hello" + " world"); // comment
	}
	
	/**                    */
	public void foo() {
		System.out.println("Hello" + /* inside comment */ " world");
	}
}
