package test;

public class MyTest {

	void f(int a, int b) {
		switch (a) {
		case 1:
			doSomething();
			break;


		case 2:
		case 3:
			doSomeOtherThing();
			break;


		case 4: {
			// yield missing, oh well...
		}


		case 5: {
			doSomething(55);
			break;
		}


		default:
			doNothing();
			break;
		}

		int c = switch (b) {
		case 444:
			yield a + b;


		case 555:
		case 666:
			doSoemthing();
			yield 777;


		default:
			doSomeOtherSomething();
			return;

		};

		int d = switch (c) {
		case 1 -> 6;
		case 2 -> {
			int f = a + b;
			yield f * f;
		}
		default -> 55;
		};

		while (true) {
			switch (a + b + c + d) {
			case 9:
				doSomething();
				continue;


			case 10:
				doNothing();
				throw new RuntimeException();


			case 11:
			case 12:
				doSomething(33);
				//$FALL-THROUGH$
			case 13:
				fallThrougAgain();
			case 14:
				doSomething(switch (a * b * c * d) {
				case 888:
					aaa();
					yield bbb();


				// comment
				case 999:
					aaa();
					yield bbb();
				// comment


				case 101010:
					aaa();
					yield bbb();


				// comment
				case 111111:
					aaa();
					yield bbb();

				// comment


				case 121212:
					aaa();
					yield bbb();

				// comment

				// comment


				case 131313:
					aaa();
					yield bbb();


				// comment
				// comment
				default:
					aaa();
					yield bbb();
				});
			}
		}
	}
}