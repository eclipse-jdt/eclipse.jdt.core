package g5.c.ref;

import g5.c.def.*;

public class RefMultiple {
	// Test constructors calls to a generic parameterized with Object
	void testObject() {
		Multiple<Object, Object, Object> gm = new Multiple<Object, Object, Object>(new Object(), new Object(), new Object());
		new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception());
		new Multiple<Object, Object, Object>(gm);
		new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm);
	}
	// Test constructors calls to a generic parameterized with Exception
	void testException() {
		Multiple<Exception, Exception, Exception> gm = new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception());
		new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception());
		new Multiple<Exception, Exception, Exception>(gm);
		new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm);
	}
	// Test constructors calls to a generic parameterized with RuntimeException
	void testRuntimeException() {
		Multiple<RuntimeException, RuntimeException, RuntimeException> gm = new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException());
		new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException());
		new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm);
		new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm);
	}
}
