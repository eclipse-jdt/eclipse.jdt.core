package s3;
/* Regression test for bug 23329 search: incorrect range for type references in brackets */
public class A {
	int field;
	Object foo() {
		return ( B  )this;
	}
	int bar() {
		return ( field  );
	}
}
class B {
}