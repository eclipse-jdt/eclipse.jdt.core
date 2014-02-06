public class X {
	int x() {
		try (@Marker
		Integer p = null; final @Marker Integer q = null; @Marker
		final Integer r = null) {
		}
		return 10;
	}

	Zork z;
}

@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_PARAMETER)
@interface Marker {
}
