package test0118;
import java.util.*;
public class Test {
	public int foo(Exception e) {
		throw e /* comment in the middle of a throw */  \u003B/** */
	}
}