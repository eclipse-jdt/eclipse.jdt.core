package test0187;
import java.util.*;
public class Test {
	public void foo() {
		int i= 0;
		while (/*]*/i <= 10/*[*/)
			foo();
		foo();	
	}	
}