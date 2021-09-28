package lib565512wrong;

// this class has no references to lib565512.Class565512
// but it has the same member names
public class WrongClass565512 {
	class InnerClass {}
	public static class PublicStaticInnerClass {}
	private class PrivateInnerClass {}
	private static class PrivateStaticInnerClass {}

	public class ExtendedClass extends WrongClass565512 {}
	public class ExtendedInnerClass extends InnerClass {}
	static class ExtendedPublicStaticInnerClass extends PublicStaticInnerClass {}
	private class ExtendedPrivateInnerClass extends PrivateInnerClass {}
	private static class ExtendedPrivateStaticInnerClass extends PrivateStaticInnerClass {}

	void x() {
	}

	private static void privateStatic() {
	}

	public static void customMain(String[] args) {
	}

	public static String stringReturning() {
		return null;
	}

	public static void main(String[] args) {
	}

	public static int primitiveMain(int[] args) {
		return 0;
	}
}
