package test0023;

public class Test {
	X var;
	void foo(Y t, A<Y> a, A<Y>.B b, A<Y>.C<Y> c, A<Y>.B.D<Y> d) {
		var.add(t,a,b,c,d);
	}
}
