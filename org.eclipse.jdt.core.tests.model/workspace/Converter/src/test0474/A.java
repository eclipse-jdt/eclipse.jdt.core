package test0474;

public class A {
	public boolean b() {
		return true;
	}
	
	public void foo() {
		while(b())
			/*]*/while(b())
				foo();
		/*]*/foo();		
	}	
}