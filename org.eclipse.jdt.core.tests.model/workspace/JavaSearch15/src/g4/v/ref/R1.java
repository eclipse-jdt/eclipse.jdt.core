/*
 * package g4.v.ref is a package to define method which define and contain
 * references (ref) to local variables (v) based on parameterized types
 */
package g4.v.ref;

import g1.t.s.def.Generic;

/*
 * This type is used to test declaration and references to local variables
 */
public class R1 {
	void simple_name(
		Generic<Object> gen_obj,
		Generic<Exception> gen_exc,
		Generic<? extends Throwable> gen_thr,
		Generic<? super RuntimeException> gen_run)
	{
		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
	void qualified_name() {
		g1.t.s.def.Generic<Object> gen_obj = new Generic<Object>();
		g1.t.s.def.Generic<Exception> gen_exc = new Generic<Exception>();
		g1.t.s.def.Generic<? extends Throwable> gen_thr = new Generic<? extends Throwable>();
		g1.t.s.def.Generic<? super RuntimeException> gen_run = new Generic<? super RuntimeException>();

		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
}
