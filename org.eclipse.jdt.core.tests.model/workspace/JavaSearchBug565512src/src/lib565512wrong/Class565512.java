package lib565512wrong;

// this class has no references to lib565512.Class565512
// but it has the same unqualified name
public class Class565512 {
	class InnerClass {}
	public static class PublicStaticInnerClass {}
	private class PrivateInnerClass {}
	private static class PrivateStaticInnerClass {}

	public class ExtendedClass extends Class565512 {}
	public class ExtendedInnerClass extends InnerClass {}
	static class ExtendedPublicStaticInnerClass extends PublicStaticInnerClass {}
	private class ExtendedPrivateInnerClass extends PrivateInnerClass {}
	private static class ExtendedPrivateStaticInnerClass extends PrivateStaticInnerClass {}

	void x() {
		x();
		main(null);
		customMain(null);
		privateStatic();
		primitiveMain(null);
		stringReturning();
	}

	private static void privateStatic() {
		main(null);
		customMain(null);
		privateStatic();
		primitiveMain(null);
		stringReturning();
	}

	public static void customMain(String[] args) {
		main(null);
		customMain(null);
		privateStatic();
		primitiveMain(null);
		stringReturning();
	}

	public static String stringReturning() {
		main(null);
		customMain(null);
		privateStatic();
		primitiveMain(null);
		stringReturning();
		return null;
	}

	public static void main(String[] args) {
		main(null);
		customMain(null);
		privateStatic();
		primitiveMain(null);
		stringReturning();
	}

	public static int primitiveMain(int[] args) {
		main(null);
		customMain(null);
		privateStatic();
		primitiveMain(null);
		stringReturning();
		return 0;
	}
}
