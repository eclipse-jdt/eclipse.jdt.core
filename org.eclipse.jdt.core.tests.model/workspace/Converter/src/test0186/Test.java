package test0186;
import java.util.*;
public class Test {
	public void foo() {
		Object o= null;
		if (/*]*/o == o/*[*/)
			foo();
	}	
}