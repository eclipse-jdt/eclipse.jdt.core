package test0034;

enum Bar {
	CONSTANT
}

@interface Foo {
	Bar value();
}
	 
public @Foo(Bar.CONSTANT) class X {
}