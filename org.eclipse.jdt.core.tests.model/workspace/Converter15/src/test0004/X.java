package test0004;

@interface Name {
	String first();
	String last();
}

@interface Author {
	Name name();
}

public @Author(@Name(first="Joe", last="Hacker"))
class X {}