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
	public Generic.MemberGeneric gmg;
	public Generic<Object>.MemberGeneric<Object> gmg_obj;
	public Generic<Exception>.MemberGeneric<Exception> gmg_exc;
	public Generic<?>.MemberGeneric<?> gmg_qmk;
	public Generic<? extends Throwable>.MemberGeneric<? super RuntimeException> gmg_thr;
	public Generic<? super RuntimeException>.MemberGeneric<? extends Throwable> gmg_run;
	// Qualified name
	public g1.t.s.def.Generic.MemberGeneric qgmg;
	public g1.t.s.def.Generic<Object>.MemberGeneric<Object> qgmg_obj;
	public g1.t.s.def.Generic<Exception>.MemberGeneric<Exception> qgmg_exc;
	public g1.t.s.def.Generic<?>.MemberGeneric<?> qgmg_qmk;
	public g1.t.s.def.Generic<? extends Throwable>.MemberGeneric<? super RuntimeException> qgmg_thr;
	public g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric<? extends Throwable> qgmg_run;
}
