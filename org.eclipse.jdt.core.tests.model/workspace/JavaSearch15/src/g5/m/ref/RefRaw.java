package g5.m.ref;

import g5.m.def.*;


// Test methods calls to a raw types
public class RefRaw {
	// Single type parameter generic type
	void testSingle() {
		Single gs = new Single();
		gs.standard(new Exception());
		gs.generic(new Exception());
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.complete(new Exception(), gs);
	}
	// Multiple type parameters generic type
	void testMultiple() {
		Multiple gm = new Multiple();
		gm.standard(new Object(), new Exception(), new RuntimeException());
		gm.generic(new Object(), new Exception(), new RuntimeException());
		gm = gm.returnParamType();
		gm.paramTypesArgs(new Single<Object>(), new Single<Exception>(), new Single<RuntimeException>(), gm);
		gm = gm.complete(new Object(), new Exception(), new RuntimeException(), gm);
	}
}
