package test0010;

@interface Foo {
}

public class X {

	public void bar(String[][] args) {
		for (@Foo final String s[] : args) {System.out.println(s);}
	}
}