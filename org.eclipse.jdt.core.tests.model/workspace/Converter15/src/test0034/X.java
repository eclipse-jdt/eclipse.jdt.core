package test0034;

enum Bar {
	CONSTANT
}

@interface Foo {
	Bar val();
}
	 
public @Foo(Bar.CONSTANT) class X {
}