package testBug496596;

interface Serial {
}

interface Cmp {
	int cmp();
}

public class Test {

	public static Cmp method() {
		return (Cmp & Serial) () -> 1;
	}
}