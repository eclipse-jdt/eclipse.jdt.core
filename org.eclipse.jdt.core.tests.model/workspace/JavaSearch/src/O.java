/* Regression test for 1GL11J6: ITPJCORE:WIN2000 - search: missing field references (nested types) */
public class O {
	int y;
	class I {
		void y() {
			y = 0;
		}
	}
}