// ---

class I {
	int id(int x, int y) {
		return 0;
	}

	static void foo(int x, int y) {

	}
}

interface J {
	default int id(int x, int y) {
		return 0;
	}

	static void foo(int x, int y) {

	}
}