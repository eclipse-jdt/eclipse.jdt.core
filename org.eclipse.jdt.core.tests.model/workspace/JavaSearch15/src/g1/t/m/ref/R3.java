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
	public Generic.MemberGeneric gmg;
	public Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException> gmg_obj;
	public Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException> gmg_exc;
	public Generic<?, ?, ?>.MemberGeneric<?, ?, ?> gmg_qmk;
	public Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> gmg_thr;
	public Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> gmg_run;
	// Qualified name
	public g1.t.m.def.Generic.MemberGeneric qgmg;
	public g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException> qgmg_obj;
	public g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException> qgmg_exc;
	public g1.t.m.def.Generic<?, ?, ?>.MemberGeneric<?, ?, ?> qgmg_qmk;
	public g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> qgmg_thr;
	public g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException> qgmg_run;
}
