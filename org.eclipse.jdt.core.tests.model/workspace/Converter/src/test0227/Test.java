package test0227;
import java.util.*;
class A {
	long j;
}
class B {
	A fA;
}
class C {
	B fB;
}
public class Test {
	public C field;
	public int i;

	public int foo() {
		return field.fB.fA.j;
	}	
}