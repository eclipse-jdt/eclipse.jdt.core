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
	public Generic gen;
	public Generic<Object> gen_obj;
	public Generic<Exception> gen_exc;
	public Generic<?> gen_wld;
	public Generic<? extends Throwable> gen_thr;
	public Generic<? super RuntimeException> gen_run;
	// qualified name
	public g1.t.s.def.Generic qgen;
	public g1.t.s.def.Generic<Object> qgen_obj;
	public g1.t.s.def.Generic<Exception> qgen_exc;
	public g1.t.s.def.Generic<?> qgen_wld;
	public g1.t.s.def.Generic<? extends Throwable> qgen_thr;
	public g1.t.s.def.Generic<? super RuntimeException> qgen_run;
}
