package lib565512;

//this class has references itself
public class Class565512 {
	class InnerClass {
		class InnerClass2 {
		}
	}

	static class StaticNestedClass {
		static class StaticNestedClass2 {
		}
	}

	{  class LocalClass {{ class LocalClass2 {}}}
		new Class565512() {// Anonymous
			{new Class565512() {// Anonymous2
				};
			}
		};
	}
	public static class PublicStaticInnerClass {}
	private class PrivateInnerClass {}
	private static class PrivateStaticInnerClass {}

	public class ExtendedClass extends Class565512 {}
	public class ExtendedInnerClass extends InnerClass {}
	static class ExtendedPublicStaticInnerClass extends PublicStaticInnerClass {}
	private class ExtendedPrivateInnerClass extends PrivateInnerClass {}
	private static class ExtendedPrivateStaticInnerClass extends PrivateStaticInnerClass {}

	void inside(Class565512 c) {
		class InnerClassInside {
			class InnerClass2Inside {
			}
		}


		{  class LocalClassInside {{ class LocalClass2Inside {}}}
			new Class565512() {// Anonymous
				{new Class565512() {// Anonymous2
					};
				}
			};
		}
	}

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
