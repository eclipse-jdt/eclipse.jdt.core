package test0039;

public class Test <T> {
	public class Y {
		public class Z <U>{
			
		}
	}
	void foo() {
		Test<Object>.Y.Z<Object, Stri, Object> x;
	}
}

