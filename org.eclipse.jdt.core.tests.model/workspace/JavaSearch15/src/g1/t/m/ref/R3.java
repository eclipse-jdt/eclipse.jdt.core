/*
 * package g1.t.m.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have multiple (m) type parameters
 */
package g1.t.m.ref;
import g1.t.m.def.Generic;

/*
 * This type is used to test reference to generic member type defined in generic type.
 */
public class R3 {
	// Simple name
	public Generic.MemberGeneric gen;
	public Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException> gen_obj;
	public Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException> gen_exc;
	public Generic<?, ?, ?>.MemberGeneric<?, ?, ?> gen_wld;
	public Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> gen_thr;
	public Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> gen_run;
	// Qualified name
	public g1.t.m.def.Generic.MemberGeneric qgen;
	public g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException> qgen_obj;
	public g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException> qgen_exc;
	public g1.t.m.def.Generic<?, ?, ?>.MemberGeneric<?, ?, ?> qgen_wld;
	public g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> qgen_thr;
	public g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> qgen_run;
}
