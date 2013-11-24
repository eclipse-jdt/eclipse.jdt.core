interface I {
	void foo(int x);
}

public class X {
	public static void main(String[] args) {
		I i = @Marker Y.@Marker Z @Marker [][] @Marker []::foo;
		I i2 = @Value("Joe") Y.@Value2(@Value("Joe")) @Marker Z @Marker [][] @Marker []::foo;
		I i3 = @Value("Duo") Y.@Value2(@Value("Joe")) @Marker Z::foo;
		i.foo(10);
		Zork z;
	}
}

class Y {
	static class Z {
		public static void foo(int x) {
			System.out.println(x);
		}
	}
}
