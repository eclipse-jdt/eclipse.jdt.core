public class Test {

static {
	Test var = new Test();
}

{
	Test t = null;
}

public static void main(String[] args) {
	p.Y y = new p.Y();
	y.foo(1, "a", y);
	p.Y.bar();
	
	p.Z z = new p.Z();
	z.foo(1, "a", z);
	
	p.A a = new p.A(y);
	a.foo(1, "a", a.x);
}
}
