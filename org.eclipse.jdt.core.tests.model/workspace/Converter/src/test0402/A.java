package test0402;

class A{
	class Inner{
		Inner(){
		}
	}
}

class I2 extends A.Inner{
	I2(){
		new A().super();
	}
}