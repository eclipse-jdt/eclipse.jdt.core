package test0063;

import java.util.List;

public class X<T> {
	
	Object foo() {
		return new X<String>();
	}
	
	public void bar(List<? extends X<?>> c) {
	}
}