package java.lang.invoke;
import java.lang.invoke.MethodType;

public class MethodHandles {
	public static final class Lookup {
		public MethodHandle findVirtual(Class<?> refc, String name,
				MethodType type) throws NoSuchMethodException,
				IllegalAccessException {
			return null;
		}

		public MethodHandle findStatic(Class<?> refc, String name,
				MethodType type) throws NoSuchMethodException,
				IllegalAccessException {
			return null;
		}
	}

	public static Lookup lookup() {
		return null;
	}
}
