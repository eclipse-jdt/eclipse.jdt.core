package f1;
public class X {
	int field;
	void foo1() {
		int var1 = 1;
		var1++;
	}
	void foo2() {
		X var2 = new X();
		var2.field++;
	}
}