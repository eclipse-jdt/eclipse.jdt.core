package g5.m.ref;

import g5.m.def.GS;

public class R1 {
	void testObject() {
		// Test methods calls with a generic parameterized with Object
		GS<Object> gs = new GS<Object>();
		// Test reference to a standard method
		gs.standard(new Object());
		// Test reference to a generic method
		gs.<Object>generic(new Object());
		// Test reference to a method returning a parameterized type
		gs = gs.returnParamType();
		// Test reference to a method with parameterized type arguments
		gs.paramTypesArgs(gs);
		// Test reference to a generic method returning a param type with param type parameters (=full)
		gs = gs.<Exception>full(new Exception(), gs);
	}
	void testException() {
		GS<Exception> gs = new GS<Exception>();
		gs.standard(new Exception());
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<Exception>generic(new Exception());
	}
	void testRuntimeException() {
		GS<RuntimeException> gs = new GS<RuntimeException>();
		gs.standard(new RuntimeException());
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<RuntimeException>generic(new RuntimeException());
		gs = gs.<RuntimeException>full(new RuntimeException(), gs);
	}
	void testUnbound() {
		GS<?> gs = new GS<?>();
		gs.standard(null);
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<Exception>generic(new Exception());
	}
	void testExtends() {
		GS<? extends Throwable> gs = new GS<? extends Throwable>();
		gs.standard(null);
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<Exception>generic(new Exception());
	}
	void testSuper() {
		GS<? super RuntimeException> gs = new GS<? super RuntimeException>();
		gs.standard(new Exception());
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<Exception>generic(new Exception());
	}
}
