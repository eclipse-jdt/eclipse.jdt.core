// ---
public class X {
	void foo(@Marker X this) {
	}

	class Y {
		Y(@Marker X X.this) {
		}

		void foo(X.Y this) {
		}
	}
}

@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
@interface Marker {

}