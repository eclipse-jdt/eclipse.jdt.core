package test0081;

class Y<T> {
	<T> Class foo(T t) {
		return t.getClass();
	}
}
public class X { 
	 
	public static void main(String[] args) { 
		Class c = new Y().foo(null);
	} 
}
