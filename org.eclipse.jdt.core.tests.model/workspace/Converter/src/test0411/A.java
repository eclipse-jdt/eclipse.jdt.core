package test0411;

public class A {
	
	int bar(int a, int b) {
		return a*b + 2 + foo();
	}
	
	int foo() {
		return 0;
	}
}