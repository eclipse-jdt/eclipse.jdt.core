/*
 * package g3.t.ref is second package to define types which contain
 * references (ref) to generic (g1) types (t)
 */
package g4.v.ref;

import g1.t.s.def.Generic;

/*
 * This type is used to test references to generic type with nested parameterized types
 */
public class R4 {
	void simple_name() {
		Generic<Object>.Member gen_obj = new Generic<Object>().new Member();
		Generic<Exception>.Member gen_exc = new Generic<Exception>().new Member();
		Generic<? extends Throwable>.Member gen_thr = new Generic<? extends Throwable>().new Member();
		Generic<? super RuntimeException>.Member gen_run = new Generic<? super RuntimeException>().new Member();

		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
	void qualified_name(
		g1.t.s.def.Generic<Object>.Member gen_obj,
		g1.t.s.def.Generic<Exception>.Member gen_exc,
		g1.t.s.def.Generic<? extends Throwable>.Member gen_thr,
		g1.t.s.def.Generic<? super RuntimeException>.Member gen_run)
	{
		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
}
