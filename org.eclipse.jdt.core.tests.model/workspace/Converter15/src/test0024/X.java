package test0024;

public class X<E> {
	
	<E> X() {
	}
	
	public static void main(String[] args) {
		X<String> x = new <String> X<String>();
		System.out.println(x);
	}
}