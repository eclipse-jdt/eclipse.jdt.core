package g2.f.s.def;

import g1.t.s.def.Generic;

public class R2 {
	{
		Generic.Member member = new Generic().new Member();
		member.m.toString();
	}
	{
		Generic<Object>.Member member = new Generic<Object>().new Member();
		member.m.toString();
	}
	{
		Generic<Exception>.Member member = new Generic<Exception>().new Member();
		member.m.toString();
	}
	{
		Generic<?>.Member member = new Generic<?>().new Member();
		member.m.toString();
	}
	{
		Generic<? extends Throwable>.Member member = new Generic<Exception>().new Member();
		member.m.toString();
	}
	{
		Generic<? extends Throwable>.Member member = new Generic<? extends Throwable>().new Member();
		member.m.toString();
	}
	{
		Generic<? super RuntimeException>.Member member = new Generic<Exception>().new Member();
		member.m.toString();
	}
	{
		Generic<? super RuntimeException>.Member member = new Generic<? super RuntimeException>().new Member();
		member.m.toString();
	}
}
