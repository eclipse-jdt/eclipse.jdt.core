package test0188;
import java.util.*;
public class Test {
	public void foo() {
		int i= 0;
		foo();
		do {
			foo();
		} while (/*]*/i <= 10/*[*/);
	}
}