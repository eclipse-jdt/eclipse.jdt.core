package p6;
/* Test case for bug 43274 Type hierarchy broken */
class A{
	class Inner{
	}
}
class B extends A.Inner{
	B(){
		new A().super();
	}
}

