package p7;
public class A {
	{
		new X() {
		};
		class Y1 extends X {
		}
		class Y2 extends Y1 {
		}
	}
	
	static {
		new X() {
		};
		class Y3 {
		}
	}
	
	X field1 = new X() {
	};
	
	X[] field2 = new X[] {
		new X() {
		},
		new X() {
		}
	};
	
	void foo() {
		new X() {
		};
		class Y1 extends X {
		}
		class Y2 extends Y1 {
		}
	}
}