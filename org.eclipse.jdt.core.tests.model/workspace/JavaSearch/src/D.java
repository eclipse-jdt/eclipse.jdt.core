/* Regression test for 1GL12XE: ITPJCORE:WIN2000 - search: missing field references in inner class */
public class D {
	int h;
	void g() {
		new Object() {
			public void run() {
				int y = 0;
				h = y;
			}
		};
	}
}