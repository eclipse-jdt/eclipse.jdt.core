package p3;
/* Test case for bug 23829 IType::resolveType incorrectly returns null  */
public class B{}
class A extends B{
}

class Test{
	void f(){
		A a= new A();
		f(a);
	}
	void f(B b){
	}
}
