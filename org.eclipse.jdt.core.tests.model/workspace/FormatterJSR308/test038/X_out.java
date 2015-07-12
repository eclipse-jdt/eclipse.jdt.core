interface I {
	Y foo(int x);
}

public class X {
	class Z extends Y {
		public Z(int x) {
			super(x);
			System.out.println();
		}
	}

	public static void main(String[] args) {
		i = @Marker W<@Marker Integer>::<@Marker String>new;
	}
}

class W<T> extends Y {
	public W(T x) {
		super(0);
		System.out.println(x);
	}
}

class Y {
	public Y(int x) {
		System.out.println(x);
	}
}
