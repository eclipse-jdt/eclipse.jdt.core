package test0028;

public class Test {
	public class Inner {
		Inner2<Inner2<Object>> stack= new Inner2<Inner2<Object>>();
	}
	class Inner2<T>{
	}
}


