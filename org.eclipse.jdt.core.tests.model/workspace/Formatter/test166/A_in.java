public class X {
	public static void main(String[] args) {
		new X().bar();
	}
	public void bar() {
		class C extends X {
			public void foo() {
			System.out.println("SUCCESS");

			}
		}
		class D extends C {
					D(){X.this.super();}
		}
		new D().foo();
	}
}