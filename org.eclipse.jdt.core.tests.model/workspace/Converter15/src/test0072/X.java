package test0072;

public class X {
	
	static {
		int var1 = 0;
		System.out.println(var1);
	}

	{
		int var2 = 0;
		System.out.println(var2);
	}

	public int var3;
	
	public void foo() {
		int var4;
	}
	
	public Object bar() {
		return new Object() {
			public void foo2() {
				int var5;
			}
		};
	}
	
	public int bar2() {
		class C {
			int var6;
			
			public void foo3() {
				int var7;
			}
		};
		return new C().var6;
	}
	
	public X() {
		int var8;
	}
	
	public void bar3(int var9) {
		int[] var10 = new int[] {};
		for (int var11 : var10) {
		}
	}

	public X(Object var12) {
	}
}