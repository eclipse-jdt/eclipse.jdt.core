/* Test case for 1GIIBC3: ITPJCORE:WINNT - search for method references - missing matches */
public class CA {
	class CB {
		void f() {
			m();
		}
		class CC {
			void f() {
				m();
			}
		}
	}
	void m() {
		System.out.println("a");
	}
}