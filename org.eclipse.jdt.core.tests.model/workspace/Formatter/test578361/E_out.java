class Example {
	void example(int empty, int tiny, int small,
			int notSoSmall) {
		String s = switch (empty) {};
		s = switch (tiny) { default -> ""; };
		s = switch (small) {
		case 1 -> "one";
		case 2 -> "two";
		default -> "";
		};
		s = switch (notSoSmall) {
		case 0 -> "zero";
		case 1 -> { doSomething(); yield "one"; }
		case 2 -> {
			doFirstThing();
			doSecondThing();
			yield "two";
		}
		default -> {
			doFirstThing();
			doSecondThing();
			doFirstThing();
			doSecondThing();
			throw new Error();
		}
		};
		switch (empty) {}
		switch (tiny) { case 1 -> {} }
		switch (tiny) { case 2 -> { doSomething(); } }
		switch (small) {
		case 1 -> {}
		case 2 -> { doSomething(); }
		}
		switch (notSoSmall) {
		case tiny -> { doSomething(); }
		case small ->
			{ doFirstThing(); doSecondThing(); }
		}
		switch (notSoSmall) {
		case notSoSmall -> {
			doFirstThing();
			doSecondThing();
			doFirstThing();
			doSecondThing();
			doFirstThing();
			doSecondThing();
		}
		}
		switch (tiny) { case 123 -> {} }
	}
}