package b79267;
public class Test {
	private static final X<String, String> BEFORE	= new X<String, String>(4);

	static {
		BEFORE.put("key1","value1");
		BEFORE.put("key2","value2");
	}
	
	private static final X<Y, Object>	objectToPrimitiveMap	= new X<Y, Object>(8);

	static {
		objectToPrimitiveMap.put(new Y<Object>(new Object()), new Object());
	}
}

class X<T, U> {
	X(int x) {}
	void put(T t, U u) {}
}

class Y<T> {
	Y(T t) {}
}
