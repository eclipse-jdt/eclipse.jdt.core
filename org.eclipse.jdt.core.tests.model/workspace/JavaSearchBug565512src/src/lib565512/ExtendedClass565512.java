package lib565512;

//this class has references to lib565512.Class565512
public class ExtendedClass565512 extends Class565512{
	class InnerClass {}
	public static class PublicStaticInnerClass {}
	private class PrivateInnerClass {}
	private static class PrivateStaticInnerClass {}

	public class ExtendedClass extends Class565512 {}
	public class ExtendedInnerClass extends Class565512.InnerClass {}
	static class ExtendedPublicStaticInnerClass extends Class565512.PublicStaticInnerClass {}

	void x() {
		Class565512 c = new Class565512();
		c.x();
		c.main(null);
		c.customMain(null);
		c.primitiveMain(null);
		c.stringReturning();
	}

	private static void privateStatic() {
		Class565512.main(null);
		Class565512.customMain(null);
		Class565512.primitiveMain(null);
		Class565512.stringReturning();
	}

	public static void customMain(String[] args) {
		Class565512.main(null);
		Class565512.customMain(null);
		Class565512.primitiveMain(null);
		Class565512.stringReturning();
	}

	public static String stringReturning() {
		Class565512.main(null);
		Class565512.customMain(null);
		Class565512.primitiveMain(null);
		Class565512.stringReturning();
		return null;
	}

	public static void main(String[] args) {
		Class565512.main(null);
		Class565512.customMain(null);
		Class565512.primitiveMain(null);
		Class565512.stringReturning();
	}

	public static int primitiveMain(int[] args) {
		Class565512.main(null);
		Class565512.customMain(null);
		Class565512.primitiveMain(null);
		Class565512.stringReturning();
		return 0;
	}
}
