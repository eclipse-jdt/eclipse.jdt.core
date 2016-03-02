package libs;
public interface MyMap<K,V> {
	V get(Object key);
	V put(K key, V val);
	V remove(Object key);
}
