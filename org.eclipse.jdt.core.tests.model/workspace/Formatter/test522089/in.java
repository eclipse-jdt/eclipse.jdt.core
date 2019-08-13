class C {
	
	private C() {
	}
	protected C(int a) {
		// with comment
	}
	protected C(byte a) {
		// with comment
		
	}
	C(String s) {
		/* with comment */
	}
	public C(int a, String s) {
		this(a);
	}
	
	void empty1() {
		
	}
	void empty2() {
		// with comment
	}
	void empty3() {
		/* with comment */
		
	}

	private void foo() {;}

	private int bar(String s) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				System.out.println("running " + s);
			}
		};
		Runnable r2 = () -> {
			System.out.println("running " + s);
			
		};
		new Thread(r).start();
		return 0;
	}
}

enum Enum {
	A('a'), B('b'), C('c'),
	D('d'), E('e'),
	;
	Enum(char c) {
		setup(c);
	}
	private void setup(char c) {
		System.out.println("setting up c");
		System.out.println("setting up c");
		System.out.println("setting up c");
		
	}
}

interface Interface {
	String method1();
	default String method2() {
		return "something";
	}
}