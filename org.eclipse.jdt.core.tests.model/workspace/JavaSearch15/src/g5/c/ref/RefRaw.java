package g5.c.ref;

import g5.c.def.*;


// Test methods calls to a raw types
public class RefRaw {
	// Single type parameter generic type
	void testSingle() {
		Single gs = new Single(new Object());
		new Single(new Object(), new Throwable()); 
		new Single(gs);
		new Single(new Object(), gs);
	}
	// Multiple type parameters generic type
	void testMultiple() {
		Multiple gm = new Multiple(new Object(), new Object(), new Object());
		new Multiple(gm, new Object(), new Throwable(), new Exception());
		new Multiple(gm);
		new Multiple(new Object(), new Throwable(), new Exception(), gm);
	}
}
