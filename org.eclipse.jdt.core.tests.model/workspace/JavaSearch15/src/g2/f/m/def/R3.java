package g2.f.m.def;

import g1.t.m.def.Generic;

public class R3 {
	{
		Generic.MemberGeneric member = new Generic().new MemberGeneric();
		member.u2.toString();
	}
	{
		Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException> member = new Generic<Object, Exception, RuntimeException>().new MemberGeneric<Object, Exception, RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException> member = new Generic<Exception, Exception, RuntimeException>().new MemberGeneric<Exception, Exception, RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<?, ?, ?>.MemberGeneric<?, ?, ?> member = new Generic<?, ?, ?>().new MemberGeneric<?, ?, ?>();
		member.u2.toString();
	}
	{
		Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> member = new Generic<Exception, Exception, RuntimeException>().new MemberGeneric<Exception, Exception, RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> member = new Generic<Exception, Exception, RuntimeException>().new MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> member = new Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>().new MemberGeneric<Exception, Exception, RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> member = new Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>().new MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> member = new Generic<Exception, Exception, RuntimeException>().new MemberGeneric<Exception, Exception, RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> member = new Generic<Exception, Exception, RuntimeException>().new MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>();
		member.u2.toString();
	}
	{
		Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> member = new Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>().new MemberGeneric<Exception, Exception, RuntimeException>();
		member.u2.toString();
	}
	{
		Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> member = new Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>().new MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>();
		member.u2.toString();
	}
}
