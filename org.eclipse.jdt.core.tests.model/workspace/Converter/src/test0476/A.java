package test0476;

public class A {
	public boolean b() {
		return true;
	}
	
	public void foo() {
		for (;b();)
			/*]*/for(;b();)
				foo();
		/*]*/foo();		
	}	
}