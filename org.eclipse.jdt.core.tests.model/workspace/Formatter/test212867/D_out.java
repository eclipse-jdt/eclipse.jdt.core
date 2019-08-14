import java.io.IOException;
import java.util.Collections;

class Example {
	static {
		staticInitialize();
	}
	{
		initialize();
	}

	void example() {
		if (true) {
			doSomething();
		} else {
			doSomethingElse();
		}
		if (true) {
			doSomething();
		} else {
			doSomethingElse();
		}
		if (1 == 1) {
		}
		if (2 == 2) {
		}
		if (3 == 3) {
			// comment
		}
		if (4 == 4) {
			// comment
		}
		if (5 == 5) {
			// comment
		}
		if (6 == 6) {
			// comment
		}
		if (7 == 7) {
			// comment
			statement();
			// comment
		}
		if (8 == 8) {
			// comment

			statement();

			// comment
		}
		if (33 == 33) {
			/* comment */
		}
		if (44 == 44) {
			/* comment */
		}
		if (55 == 55) {
			/* comment */
		}
		if (66 == 66) {
			/* comment */
		}
		if (77 == 77) {
			/* comment */
			statement();
			/* comment */
		}
		if (88 == 88) {
			/* comment */

			statement();

			/* comment */
		}
		for (int i = 0; i < 10; i++) {
			switch (i) {
			case 1:
				System.out.println("one");
				break;
			default:
				System.out.println("no one");
				break;
			}
		}
		for (String s : Collections.emptyList()) {
			try {
				doSomething(s);
			} catch (IOException e) {
				handleError(e);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cleanup();
			}
		}
		while (condition()) {
			step1();
			step2();
		}
		do {
			int a = step3();
			try (Closable c = open()) {
				a++;
				synchronized (this) {
					step4(a, a);
				}
				a--;
			} catch (Exception e) {
				a += 10;
				Object lambda = () -> {
					int b = a + 2 * a;
					String c = "" + b;
				};
			}
		} while (!condition2());

		{ // TODO
			todo();
		}
	}
}