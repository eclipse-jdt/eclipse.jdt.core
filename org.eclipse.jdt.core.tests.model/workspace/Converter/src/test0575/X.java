package test0575;

public class X {
	int field;

	class Inner {
		int field;
		public void foo() {
			field= 10;
			X.this.field= 11;
		}
	}
	
	public void foo() {
		field= 10;
	}	
}