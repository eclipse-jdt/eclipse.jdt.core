package g5.m.ref;

import g5.m.def.*;

public class RefMultiple {
	// Test methods calls
	void test() {
		Multiple<Object, Exception, RuntimeException> gm = new Multiple<Object, Exception, RuntimeException>();
		// Test reference to a standard method
		gm.standard(new Object(), new Exception(), new RuntimeException());
		// Test reference to a generic method
		gm.<Object, Exception, RuntimeException>generic(new Object(), new Exception(), new RuntimeException());
		// Test reference to a method returning a parameterized type
		gm = gm.returnParamType();
		// Test reference to a method with parameterized type arguments
		gm.paramTypesArgs(new Single<Object>(), new Single<Exception>(), new Single<RuntimeException>(), gm);
		// Test reference to a generic method returning a param type with param type parameters (=full)
		gm = gm.<Object, Exception, RuntimeException>complete(new Object(), new Exception(), new RuntimeException(), gm);
	}
	// Test methods calls to a generic parameterized with ?
	void testUnbound() {
		Multiple<?,?,?> gm = new Multiple();
		gm.paramTypesArgs(new Single<Object>(), new Single<Object>(), new Single<Object>(), gm);
		gm = gm.returnParamType();
	}
	// Test methods calls to a generic parameterized with ? extends Throwable
	void testExtends() {
		Multiple<Object, ? extends Throwable, ? extends Exception> gm = new Multiple<Object, Exception, RuntimeException>();
		gm.<Object, RuntimeException, RuntimeException>generic(new Object(), new RuntimeException(), new RuntimeException());
		gm.paramTypesArgs(new Single<Object>(), new Single<Throwable>(), new Single<Exception>(), gm);
		gm = gm.returnParamType();
		gm = gm.<Object, RuntimeException, RuntimeException>complete(new Object(), new RuntimeException(), new RuntimeException(), gm);
	}
	// Test methods calls to a generic parameterized with ? super RuntimeException
	void testSuper() {
		Multiple<Object, ? super RuntimeException, ? super IllegalMonitorStateException> gm = new Multiple<Object, Exception, RuntimeException>();
		gm.<Object, RuntimeException, IllegalMonitorStateException>generic(new Object(), new RuntimeException(), new IllegalMonitorStateException());
		gm.paramTypesArgs(new Single<Object>(), new Single<RuntimeException>(), new Single<RuntimeException>(), gm);
		gm = gm.returnParamType();
		gm = gm.<Object, RuntimeException, IllegalMonitorStateException>complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm);
	}
}
