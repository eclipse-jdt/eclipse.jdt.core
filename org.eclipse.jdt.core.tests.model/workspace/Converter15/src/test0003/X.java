package test0003;

@interface Name {
	String first();
	String last();
}

@interface Author {
	Name name();
}

@Author(@Name(first="Joe", last="Hacker"))

public class X {}