/*
 * package g1.t.m.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have multiple (m) type parameters
 */
package g1.t.m.ref;
import g1.t.m.def.NonGeneric;

/*
 * This type is used to test reference to generic member type defined in a non-generic type.
 */
public class R2 {
	// Simple name
	public NonGeneric.GenericMember gen;
	public NonGeneric.GenericMember<Object, Exception, RuntimeException> gen_obj;
	public NonGeneric.GenericMember<Exception, Exception, RuntimeException> gen_exc;
	public NonGeneric.GenericMember<?, ?, ?> gen_wld;
	public NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException> gen_thr;
	public NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> gen_run;
	// Qualified name
	public g1.t.m.def.NonGeneric.GenericMember qgen;
	public g1.t.m.def.NonGeneric.GenericMember<Object, Exception, RuntimeException> qgen_obj;
	public g1.t.m.def.NonGeneric.GenericMember<Exception, Exception, RuntimeException> qgen_exc;
	public g1.t.m.def.NonGeneric.GenericMember<?, ?, ?> qgen_wld;
	public g1.t.m.def.NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException> qgen_thr;
	public g1.t.m.def.NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> qgen_run;
}
