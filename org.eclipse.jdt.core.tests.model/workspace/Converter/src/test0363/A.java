package test0363;
/* Regression test for bug 22939 */
public class A {
	void f(){
		String xxxx= "xx";
		String y= ( xxxx );
	}
}