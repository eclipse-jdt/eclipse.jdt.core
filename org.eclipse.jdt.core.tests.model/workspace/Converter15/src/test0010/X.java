package test0010;

@interface Foo {
}

public class X {

	public static void main(String[] args) {
		for (@Foo final String s : args) {System.out.println(s);}
	}
}