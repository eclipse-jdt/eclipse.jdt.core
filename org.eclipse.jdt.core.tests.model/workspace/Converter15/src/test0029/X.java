package test0029;

import java.util.Iterator;

interface Predicate<T> {
	boolean is(T t);
}

interface List<T> {
	List<T> select(Predicate<T> p);
}

class X<T> implements List<T>, Iterable<T> {
	public List<T> select(Predicate<T> p) {
		X<T> result = new X<T>();
		for (T t : this) {
			if (p.is(t))
				result.add(t);
		}
		return result;
	}
	
	public Iterator<T> iterator() {
		return null;
	}
	void add(T t) {
	}
}