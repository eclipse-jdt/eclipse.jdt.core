package java.util;

public interface Map<K,V> {
	public interface Entry<K,V> {}
	
	V get(Object key);
	V put(K key, V value);
	V remove(Object key);
}