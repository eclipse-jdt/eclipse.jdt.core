package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SwitchPatternTest21 extends AbstractBatchCompilerTest {

	private static String[] JAVAC_OPTIONS = new String[] { "--enable-preview" };

	public static Test suite() {
		return buildMinimalComplianceTestSuite(SwitchPatternTest21.class, F_21);
	}

	public SwitchPatternTest21(String name) {
		super(name);
	}

	@Override
	protected Map<String, String> getCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions(super.getCompilerOptions());
		if (compilerOptions.sourceLevel == ClassFileConstants.JDK21) {
			compilerOptions.enablePreviewFeatures = true;
		}
		return compilerOptions.getMap();
	}

	public void runConformTest(String[] files, String expectedOutput) {
		super.runConformTest(files, expectedOutput, null, JAVAC_OPTIONS);
	}

	public void testListOfPatterns_000() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_001() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_002() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Licorice(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"failure");
	}

	public void testListOfPatterns_003() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when 1 == 1 && 2 == 2: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_004() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when 1 == 1 && 2 == 2 : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_005() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Licorice(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when 1 == 1 && 2 == 2 : System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"failure");
	}

	// next three tests: beat the static analysis so that the `when` clause comparison isn't optimized away

	public void testListOfPatterns_006() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								String myString = "asdf";
								Object o = new Pantaloon(new Melon(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when myString.length() == 4: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_007() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								String myString = "asdf";
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when myString.length() == 4: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success");
	}

	public void testListOfPatterns_008() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								String myString = "asdf";
								Object o = new Pantaloon(new Licorice(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int _), Pantaloon(Melon _, int _) when myString.length() == 4: System.out.println("success"); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"failure");
	}

	public void testTwoCasesWithRecordPatternsShouldNotDominateRegression() {
		this.runConformTest(
				new String[] { "X.java",
						"""
						public class X {

							public static void main(String... args) {
								Object o = new Pantaloon(new Chartreuse(), 12);
								switch (o) {
								case Pantaloon(Chartreuse _, int i) when true: System.out.println("success " + i); break;
								case Pantaloon(Melon _, int i) when true : System.out.println("success " + i); break;
								default: System.out.println("failure"); break;
								}
							}

							static sealed abstract class Flavour permits Chartreuse, Orange, Licorice, Melon { }
							static final class Chartreuse extends Flavour {}
							static final class Orange extends Flavour {}
							static final class Licorice extends Flavour {}
							static final class Melon extends Flavour {}
							static record Pantaloon(Flavour flavour, int i) {}

						}
						""", },
				"success 12");
	}

}
