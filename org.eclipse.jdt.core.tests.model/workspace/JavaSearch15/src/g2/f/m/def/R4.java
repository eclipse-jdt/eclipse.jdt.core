package g2.f.m.def;

import g1.t.m.def.NonGeneric;

public class R4 {
	{
		NonGeneric.GenericMember member = new NonGeneric().new GenericMember();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<Object, Exception, RuntimeException> member = new NonGeneric().new GenericMember<Object, Exception, RuntimeException>();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<Exception, Exception, RuntimeException> member = new NonGeneric().new GenericMember<Exception, Exception, RuntimeException>();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<?, ?, ?> member = new NonGeneric().new GenericMember<?, ?, ?>();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException> member = new NonGeneric().new GenericMember<Exception, Exception, RuntimeException>();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException> member = new NonGeneric().new GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> member = new NonGeneric().new GenericMember<Exception, Exception, RuntimeException>();
		member.t3.toString();
	}
	{
		NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> member = new NonGeneric().new GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>();
		member.t3.toString();
	}
}
