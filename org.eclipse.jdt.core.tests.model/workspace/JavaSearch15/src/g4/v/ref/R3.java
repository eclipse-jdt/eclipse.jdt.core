/*
 * package g3.t.ref is second package to define types which contain
 * references (ref) to generic (g1) types (t)
 */
package g4.v.ref;

import g1.t.s.def.Generic;

/*
 * This type is used to test references to generic type with nested parameterized types
 */
public class R3 {
	void simple_name(
		Generic<Object>.MemberGeneric<Object> gen_obj,
		Generic<Exception>.MemberGeneric<Exception> gen_exc,
		Generic<? extends Throwable>.MemberGeneric<? extends Throwable> gen_thr,
		Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> gen_run)
	{
		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
	void qualified_name() {
		g1.t.s.def.Generic<Object>.MemberGeneric<Object> gen_obj = new Generic().new MemberGeneric<Object>();
		g1.t.s.def.Generic<Exception>.MemberGeneric<Exception> gen_exc = new Generic().new MemberGeneric<Exception>();
		g1.t.s.def.Generic<? extends Throwable>.MemberGeneric<? extends Throwable> gen_thr = new Generic().new MemberGeneric<Exception>();
		g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException> gen_run = new Generic().new MemberGeneric<Exception>();

		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
}
