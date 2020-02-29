package java.util;

public interface Collection<T> {
	public Iterator<T> iterator();
	public int size();
	public T get(int index);
	public boolean addAll(Collection<T> c);
	public T[] toArray(T[] o);
}
