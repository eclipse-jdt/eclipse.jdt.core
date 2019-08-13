import java.io.IOException;
import java.util.Arrays;
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
		} else if (false) {
			doSomethingElse();
		}


		if (1 == 1) {
		}


		// comment
		if (2 == 2) {
		}
		// comment


		if (3 == 3) {
		}


		// comment
		if (4 == 4) {
		}
		// double comment


		// double comment
		if (5 == 5) {
		}


		// double comment

		// double comment


		if (6 == 6) {
		}


		if (a)
			instruction();


		if (11 == 11) {
		}


		/* comment */
		if (22 == 22) {
		}
		/* comment */


		if (33 == 33) {
		}


		/* comment */
		if (44 == 44) {
		}
		/* double comment */


		/* double comment */
		if (55 == 55) {
		}


		/* double comment */

		/* double comment */


		if (66 == 66) {
		}


		for (String s : Arrays.asList(""))
			doSomething(s);


		for (int i = 0; i < 10; i++) {
			aaa();


			switch (i) {
			case 1:
				System.out.println("one");
				break;
			default:
				System.out.println("no one");
				break;
			}


			aaa();
		}


		aaa();


		for (String s : Collections.emptyList()) {
			aaa();


			try {
				doSomething(s);
			} catch (IOException e) {
				handleError(e);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cleanup();
			}


			aaa();
		}


		aaa();


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