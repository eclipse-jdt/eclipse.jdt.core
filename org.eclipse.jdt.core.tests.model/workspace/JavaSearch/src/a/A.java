package a;
/* Test case for 1GL0MN9: ITPJCORE:WIN2000 - search: not consistent results for nested types */
public class A {
	class X {
	}

};
class S extends A {
}
class B {
	A.X ax; /*1*/
	S.X sx; /*2*/
}