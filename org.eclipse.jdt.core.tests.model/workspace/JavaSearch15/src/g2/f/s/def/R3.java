package g2.f.s.def;

import g1.t.s.def.Generic;

public class R3 {
	{
		Generic.MemberGeneric member = new Generic().new MemberGeneric();
		member.v.toString();
	}
	{
		Generic<Object>.MemberGeneric<Object> member = new Generic<Object>().new MemberGeneric<Object>();
		member.v.toString();
	}
	{
		Generic<Exception>.MemberGeneric<Exception> member = new Generic<Exception>().new MemberGeneric<Exception>();
		member.v.toString();
	}
	{
		Generic<?>.MemberGeneric<?> member = new Generic<?>().new MemberGeneric<?>();
		member.v.toString();
	}
	{
		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<Exception>().new MemberGeneric<Exception>();
		member.v.toString();
	}
	{
		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<Exception>().new MemberGeneric<? extends Throwable>();
		member.v.toString();
	}
	{
		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<? extends Throwable>().new MemberGeneric<Exception>();
		member.v.toString();
	}
	{
		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> member = new Generic<? extends Throwable>().new MemberGeneric<? extends Throwable>();
		member.v.toString();
	}
	{
		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<Exception>().new MemberGeneric<Exception>();
		member.v.toString();
	}
	{
		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<Exception>().new MemberGeneric<? super RuntimeException>();
		member.v.toString();
	}
	{
		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<? super RuntimeException>().new MemberGeneric<Exception>();
		member.v.toString();
	}
	{
		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> member = new Generic<? super RuntimeException>().new MemberGeneric<? super RuntimeException>();
		member.v.toString();
	}
}
