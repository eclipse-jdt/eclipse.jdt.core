package targets.model.pa;

public class A implements IA {
	public String methodIAString(int int1) 
	{ 
		_fieldAint = int1;
		return String.valueOf(_fieldAint);
	}
	private int _fieldAint;
}
