package g2.f.m.def;

import g1.t.m.def.Generic;

public class R1 {
	{
		Generic generic = new Generic();
		generic.t1.toString();
	}
	{
		Generic<Object, Exception, RuntimeException> generic = new Generic<Object, Exception, RuntimeException>();
		generic.t1.toString();
	}
	{
		Generic<Exception, Exception, RuntimeException> generic = new Generic<Exception, Exception, RuntimeException>();
		generic.t1.toString();
	}
	{
		Generic<?, ?, ?> generic = new Generic<?, ? ,?>();
		generic.t1.toString();
	}
	{
		Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException> generic = new Generic<Exception, RuntimeException, IllegalMonitorStateException>();
		generic.t1.toString();
	}
	{
		Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException> generic = new Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>();
		generic.t1.toString();
	}
	{
		Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> generic = new Generic<Exception, RuntimeException, RuntimeException>();
		generic.t1.toString();
	}
	{
		Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> generic = new Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>();
		generic.t1.toString();
	}
}
