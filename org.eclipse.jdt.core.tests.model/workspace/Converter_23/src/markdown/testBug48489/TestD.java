package javadoc.testBug48489;
import java.util.*;
public class TestD {
	/**
	 * Javadoc
	 */
	public static void main(String[] args) {
		System.out./* */println("Hello" + " world");
	}
}