/*
 * package g4.v.ref is a package to define method which define and contain
 * references (ref) to local variables (v) based on parameterized types
 */
package g4.v.ref;

import g1.t.s.def.NonGeneric;


/*
 * This type is used to test declaration and references to local variables
 */
public class R2 {
	void simple_name() {
		NonGeneric.GenericMember<Object> gen_obj = new NonGeneric().new GenericMember<Object>();
		NonGeneric.GenericMember<Exception> gen_exc = new NonGeneric().new GenericMember<Exception>();
		NonGeneric.GenericMember<? extends Throwable> gen_thr = new NonGeneric().new GenericMember<Exception>();
		NonGeneric.GenericMember<? super RuntimeException> gen_run = new NonGeneric().new GenericMember<Exception>();

		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
	void qualified_name(
		g1.t.s.def.NonGeneric.GenericMember<Object> gen_obj,
		g1.t.s.def.NonGeneric.GenericMember<Exception> gen_exc,
		g1.t.s.def.NonGeneric.GenericMember<? extends Throwable> gen_thr,
		g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException> gen_run)
	{
		gen_obj.toString();
		gen_exc.toString();
		gen_thr.toString();
		gen_run.toString();
	}
}
