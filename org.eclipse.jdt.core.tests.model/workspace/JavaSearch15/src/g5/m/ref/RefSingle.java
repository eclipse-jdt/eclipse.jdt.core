package g5.m.ref;

import g5.m.def.Single;

public class RefSingle {
	// Test methods calls to a generic parameterized with Object
	void testObject() {
		Single<Object> gs = new Single<Object>();
		// Test reference to a standard method
		gs.standard(new Object());
		// Test reference to a generic method
		gs.<Object>generic(new Object());
		// Test reference to a method returning a parameterized type
		gs = gs.returnParamType();
		// Test reference to a method with parameterized type arguments
		gs.paramTypesArgs(gs);
		// Test reference to a generic method returning a param type with param type parameters
		gs.<Object>complete(new Object(), gs);
	}
	// Test methods calls to a generic parameterized with Exception
	void testException() {
		Single<Exception> gs = new Single<Exception>();
		gs.standard(new Exception());
		gs.<Exception>generic(new Exception());
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<Exception>complete(new Exception(), gs);
	}
	// Test methods calls to a generic parameterized with RuntimeException
	void testRuntimeException() {
		Single<RuntimeException> gs = new Single<RuntimeException>();
		gs.standard(new RuntimeException());
		gs.<RuntimeException>generic(new RuntimeException());
		gs = gs.returnParamType();
		gs.paramTypesArgs(gs);
		gs.<RuntimeException>complete(new RuntimeException(), gs);
	}
	// Test methods calls to a generic parameterized with ?
	void testUnbound() {
		Single<?> gs = new Single();
		gs.paramTypesArgs(gs);
		gs = gs.returnParamType();
		gs.<String>complete(new String(), gs);
	}
	// Test methods calls to a generic parameterized with ? extends Throwable
	void testExtends() {
		Single<? extends Throwable> gs = new Single<Throwable>();
		gs.paramTypesArgs(gs);
		gs.returnParamType();
		gs.<Throwable>complete(new Throwable(), gs);
	}
	// Test methods calls to a generic parameterized with ? super RuntimeException
	void testSuper() {
		Single<? super RuntimeException> gs = new Single<RuntimeException>();
		gs.paramTypesArgs(gs);
		gs = gs.returnParamType();
		gs.<RuntimeException>complete(new RuntimeException(), gs);
	}
}
