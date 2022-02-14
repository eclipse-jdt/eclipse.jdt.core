
public class C {

	void foo() {
		switch (
			a + b + c /* + d */
					+ e + f
		) {
		case 1, 2, 99999, 5, 6:
			method1();
			break;
		case 999, 998, 997:
		case 996, 995, //
				994:
			method2();
		default :
			method3();
		}

		switch (
			"a" + "b" + "c" //
					+ something
		) {
		case "abc1" -> System.out.println("1");
		case "abc2", /* ??? */ "abc3", "abc4" -> System.out.println("234");
		case "def2", /* ??? */
				"def3", "def4" ->
			System.out.println("def");
		case "abc5", "abc6" -> {
		}
		case "abc7" -> {
			System.out.println("7");
			return;
		}
		default-> System.out.println("?");
		}

		int value = switch (
			ABC
		) {
		case A:
			yield 1;
		case B:
			System.out.println("!!!");
			return;
		case BB:
			;
		case C, D, E, F, G:
		case H, I, J: {
			System.out.println("@@@");
			yield 3454366;
		}
		default :
			yield 6;
		};

		Object value2 = switch (
			a.b.c.d(e.f.g.h())
		) {
		case a -> {
			System.out.println("aaaaaaa");
			yield "";
		}
		case b + c, d.e, f("aaaaaaaaaaaa"//
				+ //
				"bbbbbbbbb"//
		), (33), aaa = bbb + ccc, new int[] { 1, 2, aaa }, AAA::BBB -> (Runnable) () -> f();
		// $$$$
		case new Object() {
			String toString() {
				return "";
			}
		} -> ABCD;
		case null -> {
			return null;
		}
		case something -> //
			null;
		default-> throw new RuntimeException("unsupported");
		};
	}
}
