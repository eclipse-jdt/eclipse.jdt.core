/*
 * package g1.t.m.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have multiple (m) type parameters
 */
package g1.t.m.ref;
import g1.t.m.def.Generic;

/*
 * This type is used to test references to generic type
 */
public class R1 {
	// Simple name
	public Generic g;
	public Generic<Object, Exception, RuntimeException> g_obj;
	public Generic<Exception, Exception, RuntimeException> g_exc;
	public Generic<?, ?, ?> g_qmk;
	public Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException> g_thr;
	public Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> g_run;
	// qualified name
	public g1.t.m.def.Generic qg;
	public g1.t.m.def.Generic<Object, Exception, RuntimeException> qg_obj;
	public g1.t.m.def.Generic<Exception, Exception, RuntimeException> qg_exc;
	public g1.t.m.def.Generic<?, ?, ?> qg_qmk;
	public g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException> qg_thr;
	public g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> qg_run;
}
