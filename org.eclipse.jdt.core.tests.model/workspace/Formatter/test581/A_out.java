import java.lang.reflect.Constructor;

public class X {
	public <T extends X> Constructor<T> ctor() {
		return null;
	}

	static <T extends X> T f1() throws Exception {
		return new X().<T> ctor().newInstance(new Object[0]);
	}

	static <T extends X> T f2() throws Exception {
		return f1();
	}
}
