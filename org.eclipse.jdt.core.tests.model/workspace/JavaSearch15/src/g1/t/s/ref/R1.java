/*
 * package g1.t.s.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have only one single (s) type parameter
 */
package g1.t.s.ref;
import g1.t.s.def.Generic;

/*
 * This type is used to test references to generic type
 */
public class R1 {
	// Simple name
	public Generic g;
	public Generic<Object> g_obj;
	public Generic<Exception> g_exc;
	public Generic<?> g_qmk;
	public Generic<? extends Throwable> g_thr;
	public Generic<? super RuntimeException> g_run;
	// qualified name
	public g1.t.s.def.Generic qg;
	public g1.t.s.def.Generic<Object> qg_obj;
	public g1.t.s.def.Generic<Exception> qg_exc;
	public g1.t.s.def.Generic<?> qg_qmk;
	public g1.t.s.def.Generic<? extends Throwable> qg_thr;
	public g1.t.s.def.Generic<? super RuntimeException> qg_run;
}
