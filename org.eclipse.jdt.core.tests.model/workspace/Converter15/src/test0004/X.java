package test0004;

@interface Name {
	String first();
	String last();
}

@interface Author {
	Name value();
}

public @Author(@Name(first="Joe", last="Hacker"))
class X {}