package test0302;
import java.util.*;
public class Test {
	public void foo() {
		/*]*/do
			foo();
		while(1 < 10);/*[*/
	}
}
