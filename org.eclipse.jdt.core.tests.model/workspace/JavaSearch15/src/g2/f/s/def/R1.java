package g2.f.s.def;

import g1.t.s.def.Generic;

public class R1 {
	{
		Generic generic = new Generic();
		generic.t.toString();
	}
	{
		Generic<Object> generic = new Generic<Object>();
		generic.t.toString();
	}
	{
		Generic<Exception> generic = new Generic<Exception>();
		generic.t.toString();
	}
	{
		Generic<?> generic = new Generic<?>();
		generic.t.toString();
	}
	{
		Generic<? extends Throwable> generic = new Generic<Exception>();
		generic.t.toString();
	}
	{
		Generic<? extends Throwable> generic = new Generic<? extends Throwable>();
		generic.t.toString();
	}
	{
		Generic<? super RuntimeException> generic = new Generic<Exception>();
		generic.t.toString();
	}
	{
		Generic<? super RuntimeException> generic = new Generic<? super RuntimeException>();
		generic.t.toString();
	}
}
