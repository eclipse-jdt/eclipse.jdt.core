package test0005;

@interface Name {
	String first() default "Joe";
	String last() default "Smith";
	int age();
}

@interface Author {
	Name name();
}

@interface Retention {
}

@Retention public @Author(@Name(first="Joe", last="Hacker", age=32))
class X {}