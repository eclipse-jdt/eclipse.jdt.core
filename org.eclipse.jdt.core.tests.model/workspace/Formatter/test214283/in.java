abstract class C {
	interface I {
		void method1();
		
		String method2();
		void method3(String s);
		default void method4() {
			System.out.println("method4");
			System.out.println("method5");
		}
	}
	public String field;
	public abstract String methodA();
	public abstract int methodB(Object o);
	public void methodC() {
		System.out.println("methodC");
	}
	public String methodD(String s) {
		return s + "s";
	}
	protected abstract Object methodE(String a);
	protected abstract void methodF(String ... a);
}