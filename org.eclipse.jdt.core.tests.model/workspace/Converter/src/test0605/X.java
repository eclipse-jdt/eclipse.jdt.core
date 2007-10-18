package test0605;

public class X {

	void foo() {
        int[] a= {1};
        int lenA= a.length;
        int lenB = this.a.length;
        C c = new C();
        int lenC = c.d.length;
    }
	int[] a = {};
	class C {
		int[] d = {};
	}
}