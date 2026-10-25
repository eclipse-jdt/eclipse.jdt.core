package targets.GH5566;

import java.util.ArrayList;
import java.util.Map;

public class GH5566Box<X> {
	<V extends ArrayList<X>> void m() {
	}
	<V extends Number & Comparable<V>> void comparable() {
	}
	<V extends Map<X, X> & Runnable> void map() {
	}
	<A, B extends A> void pick() {
	}
	class Inner<Y> {
	}
}
