package targets.model.pa;

public class A implements IA {
	public String methodIAString(int int1) 
	{ 
		_fieldAint = int1;
		return String.valueOf(_fieldAint);
	}
	
	public void methodThrows1() throws ExceptionA {
		if (_fieldAint < 0) {
			throw new ExceptionA();
		}
	}
	
	public void methodThrows2() throws ExceptionA, UnsupportedOperationException {
		if (_fieldAint > 0) {
			throw new ExceptionA();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	private int _fieldAint;
}
