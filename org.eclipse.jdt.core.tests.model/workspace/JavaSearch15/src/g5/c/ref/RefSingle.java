package g5.c.ref;

import g5.c.def.Single;

public class RefSingle {
	// Test constructors calls to a generic parameterized with Object
	void testObject() {
		Single<Object> gs = new Single<Object>(new Object());
		new <Throwable>Single<Object>(new Object(), new Throwable()); 
		new Single<Object>(gs);
		new <Exception>Single<Object>(new Exception(), gs);
	}
	// Test constructors calls to a generic parameterized with Exception
	void testException() {
		Single<Exception> gs = new Single<Exception>(new Exception());
		new <Exception>Single<Exception>(new Exception(), new Exception());
		new Single<Exception>(gs);
		new <Exception>Single<Exception>(new Exception(), gs);
	}
	// Test constructors calls to a generic parameterized with RuntimeException
	void testRuntimeException() {
		Single<RuntimeException> gs = new Single<RuntimeException>(new RuntimeException());
		new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException());
		new Single<RuntimeException>(gs);
		new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs);
	}
}
