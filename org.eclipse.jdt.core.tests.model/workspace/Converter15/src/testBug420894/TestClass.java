package testBug420894;
public class TestClass {
	
	SomeUndeclaredType<?>[] undeclaredField;	// works fine
	
	public static void main(String[] args) {
		SomeUndeclaratedType<?>[] undeclaredLocalDeclaration;	//ClassCastException
	}

}