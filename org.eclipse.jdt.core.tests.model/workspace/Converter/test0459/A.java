package test0459;

public class A {
	public void foo() {
		for (int i= 10; i < 10; i++)/*[*/
			for (int z= 10; z < 10; z++)
				{    }
		/*]*/foo();	
	}
}