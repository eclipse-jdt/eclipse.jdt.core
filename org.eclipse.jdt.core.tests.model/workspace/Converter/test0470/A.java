package test0470;

public class A {

	void foo(String loginName, String password) {
		assert(password != null) : "null password";
		assert(loginName != null) ;
	}
}