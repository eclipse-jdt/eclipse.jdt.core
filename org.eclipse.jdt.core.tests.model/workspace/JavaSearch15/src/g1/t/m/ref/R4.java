/*
 * package g1.t.m.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have multiple (m) type parameters
 */
package g1.t.m.ref;
import g1.t.m.def.Generic;

/*
 * This type is used to test reference to member type defined in generic type.
 */
public class R4 {
	// Simple name
	public Generic.Member gen;
	public Generic<Object, Exception, RuntimeException>.Member gen_obj;
	public Generic<Exception, Exception, RuntimeException>.Member gen_exc;
	public Generic<?, ?, ?>.Member gen_wld;
	public Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member gen_thr;
	public Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member gen_run;
	// Qualified name
	public g1.t.m.def.Generic.Member qgen;
	public g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member qgen_obj;
	public g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member qgen_exc;
	public g1.t.m.def.Generic<?, ?, ?>.Member qgen_wld;
	public g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member qgen_thr;
	public g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member qgen_run;
}
