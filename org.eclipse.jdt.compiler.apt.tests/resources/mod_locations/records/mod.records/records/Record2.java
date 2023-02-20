package records;
import java.math.BigInteger;

public strictfp record Record2(int x, BigInteger i, Record3 r, Test t) {
	void foo() {}
	public void bar() {}
	private static String s;
	public static double d;
	protected static Character c;
}
record Record3 (int x, int y) {
	Record3 {
		x = y;
	}
}
class Test{}