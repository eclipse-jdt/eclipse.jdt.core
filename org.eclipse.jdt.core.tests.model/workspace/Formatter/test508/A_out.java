@Retention
package test0006;

@interface Name {
	String first() default "Joe";

	String last() default "Smith";

	int age();
}

@interface Author {
	Name value();
}

@interface Retention {
}

@Retention
@Author(@Name(first = "Joe", last = "Hacker", age = 32))
class A {
}
