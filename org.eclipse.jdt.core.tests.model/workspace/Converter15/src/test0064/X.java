package test0064;

import java.util.List;

public class X<T, U> {
	
	Object foo() {
		return new X<String, Integer>();
	}
	
	public void bar(List<? extends X<?,?>> c) {
	}
}