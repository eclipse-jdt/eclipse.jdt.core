package s3;
/* Regression test for bug 23329 search: incorrect range for type references in brackets */
public class A {
	Object foo() {
		return ( B  )this;
	}
}
class B {
}