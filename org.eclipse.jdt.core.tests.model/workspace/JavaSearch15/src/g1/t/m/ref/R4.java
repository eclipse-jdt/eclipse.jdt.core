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
	public Generic.Member gm;
	public Generic<Object, Exception, RuntimeException>.Member gm_obj;
	public Generic<Exception, Exception, RuntimeException>.Member gm_exc;
	public Generic<?, ?, ?>.Member gm_qmk;
	public Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member gm_thr;
	public Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member gm_run;
	// Qualified name
	public g1.t.m.def.Generic.Member qgm;
	public g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member qgm_obj;
	public g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member qgm_exc;
	public g1.t.m.def.Generic<?, ?, ?>.Member qgm_qmk;
	public g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member qgm_thr;
	public g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member qgm_run;
}
