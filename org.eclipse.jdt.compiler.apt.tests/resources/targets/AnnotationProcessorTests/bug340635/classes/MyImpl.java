package classes;

import classes.MyInterface;
import annotation.GetType;

@GetType
public class MyImpl implements MyInterface<String, String> {
	@Override
	public Class<String> foo(String param) {
		return String.class;
	}
}
