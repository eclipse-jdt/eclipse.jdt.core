package test0252;
import java.util.*;
public class Test {
	Object foo() {
		final int j = 4;
		
		return new Object() {
			int bar() {
				return j;
			}
		};
	}
}

