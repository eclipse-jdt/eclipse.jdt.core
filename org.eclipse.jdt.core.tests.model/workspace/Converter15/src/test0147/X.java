package test0147;

import java.lang.Comparable;
import java.util.Collection;
import java.util.Iterator;

public class X {
	public static <T extends Object & Comparable<? super T>> T min(
			Collection<? extends T> coll) {
		Iterator<? extends T> i = coll.iterator();
		T candidate = i.next();

		while (i.hasNext()) {
			T next = i.next();
			if (next.compareTo(candidate) < 0)
				candidate = next;
		}
		return candidate;
	}
}