package b77093;
public class X {
	class Z {
	}
	Z[][] z_arrays;
	X() {
		this(new Z[10][]);
	}
	X(Z[][] arrays) {
		z_arrays = arrays;
	}
	private void foo(Z[] args) {
	}
	void bar() {
		for (int i=0; i<z_arrays.length; i++)
			foo(z_arrays[i]);
	}
}