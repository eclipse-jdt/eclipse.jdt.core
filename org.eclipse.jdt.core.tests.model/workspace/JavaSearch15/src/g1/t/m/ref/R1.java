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
	public Generic gen;
	public Generic<Object, Exception, RuntimeException> gen_obj;
	public Generic<Exception, Exception, RuntimeException> gen_exc;
	public Generic<?, ?, ?> gen_wld;
	public Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException> gen_thr;
	public Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> gen_run;
	// qualified name
	public g1.t.m.def.Generic qgen;
	public g1.t.m.def.Generic<Object, Exception, RuntimeException> qgen_obj;
	public g1.t.m.def.Generic<Exception, Exception, RuntimeException> qgen_exc;
	public g1.t.m.def.Generic<?, ?, ?> qgen_wld;
	public g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException> qgen_thr;
	public g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> qgen_run;
}
