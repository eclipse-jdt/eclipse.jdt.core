/*
 * package g1.t.s.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have only one single (s) type parameter
 */
package g1.t.s.ref;
import g1.t.s.def.Generic;

/*
 * This type is used to test reference to generic member type defined in generic type.
 */
public class R3 {
	// Simple name
	public Generic.MemberGeneric gen;
	public Generic<Object>.MemberGeneric<Object> gen_obj;
	public Generic<Exception>.MemberGeneric<Exception> gen_exc;
	public Generic<?>.MemberGeneric<?> gen_wld;
	public Generic<? extends Throwable>.MemberGeneric<? super RuntimeException> gen_thr;
	public Generic<? super RuntimeException>.MemberGeneric<? extends Throwable> gen_run;
	// Qualified name
	public g1.t.s.def.Generic.MemberGeneric qgen;
	public g1.t.s.def.Generic<Object>.MemberGeneric<Object> qgen_obj;
	public g1.t.s.def.Generic<Exception>.MemberGeneric<Exception> qgen_exc;
	public g1.t.s.def.Generic<?>.MemberGeneric<?> qgen_wld;
	public g1.t.s.def.Generic<? extends Throwable>.MemberGeneric<? super RuntimeException> qgen_thr;
	public g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric<? extends Throwable> qgen_run;
}
