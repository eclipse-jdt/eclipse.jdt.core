package test0475;

public class A {
	public boolean b() {
		return true;
	}
	
	public void foo() {
		if (b())
			/*]*/if(b())
				foo();
		/*]*/foo();		
	}	
}