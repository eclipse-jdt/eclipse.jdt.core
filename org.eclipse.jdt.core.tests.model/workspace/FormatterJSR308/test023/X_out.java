public class X {
	void foo(
			Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m) {
	}

	void goo(
			Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m) {
	}
}
