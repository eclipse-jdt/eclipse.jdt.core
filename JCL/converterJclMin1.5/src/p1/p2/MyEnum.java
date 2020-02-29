package p1.p2;

/**
 * Test case for bug 147875
 */
public enum MyEnum {

	@Annot(MyEnum2.B)
	A,
	@Annot(MyEnum2.C)
	B,
	@Annot(MyEnum2.A)
	C,
	@Annot(MyEnum2.D)
	D;
	
}
