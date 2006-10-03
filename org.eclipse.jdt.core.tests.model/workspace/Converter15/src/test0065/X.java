package test0065;

import java.util.List;

public class X<T, U extends List<?>> {
	
	Object foo() {
		return new X<String, List<?>>();
	}
	
	public void bar(List<? extends X<?, ?>> c) {
	}
}