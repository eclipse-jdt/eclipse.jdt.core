package g2.f.s.def;

import g1.t.s.def.NonGeneric;

public class R4 {
	{
		NonGeneric.GenericMember member = new NonGeneric().new GenericMember();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<Object> member = new NonGeneric().new GenericMember<Object>();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<Exception> member = new NonGeneric().new GenericMember<Exception>();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<?> member = new NonGeneric().new GenericMember<?>();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<? extends Throwable> member = new NonGeneric().new GenericMember<Exception>();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<? extends Throwable> member = new NonGeneric().new GenericMember<? extends Throwable>();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<? super RuntimeException> member = new NonGeneric().new GenericMember<Exception>();
		member.t.toString();
	}
	{
		NonGeneric.GenericMember<? super RuntimeException> member = new NonGeneric().new GenericMember<? super RuntimeException>();
		member.t.toString();
	}
}
