/*
 * package g1.t.s.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have only one single (s) type parameter
 */
package g1.t.s.ref;
import g1.t.s.def.Generic;

/*
 * This type is used to test reference to member type defined in generic type.
 */
public class R4 {
	// Simple name
	public Generic.Member gm;
	public Generic<Object>.Member gm_obj;
	public Generic<Exception>.Member gm_exc;
	public Generic<?>.Member gm_qmk;
	public Generic<? extends Throwable>.Member gm_thr;
	public Generic<? super RuntimeException>.Member gm_run;
	// Qualified name
	public g1.t.s.def.Generic.Member qgm;
	public g1.t.s.def.Generic<Object>.Member qgm_obj;
	public g1.t.s.def.Generic<Exception>.Member qgm_exc;
	public g1.t.s.def.Generic<?>.Member qgm_qmk;
	public g1.t.s.def.Generic<? extends Throwable>.Member qgm_thr;
	public g1.t.s.def.Generic<? super RuntimeException>.Member qgm_run;
}
