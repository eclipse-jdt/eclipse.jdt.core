package test.comments.block;

public class X08 {
	void foo(boolean condition) {
		if (true) {
			if (true) {
				if (true) {
					if (condition /*
									 * && useChange(d.fDirection) &&
									 * !d.fIsWhitespace
									 */) {
					}
				}
			}
		}
	}
}
