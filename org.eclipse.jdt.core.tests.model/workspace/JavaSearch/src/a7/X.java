package a7;
/* Test case for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types */
public class X {
	public void foo() throws MyException {
	}
}
class MyException extends Exception {
}
