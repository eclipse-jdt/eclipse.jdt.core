/*
 * package g1.t.s.ref is the package to define types which contain
 * references (ref) to generic (g1) types (t) which have only one single (s) type parameter
 */
package g1.t.s.ref;
import g1.t.s.def.NonGeneric;

/*
 * This type is used to test reference to generic member type defined in a non-generic type.
 */
public class R2 {
	// Simple name
	public NonGeneric.GenericMember gen;
	public NonGeneric.GenericMember<Object> gen_obj;
	public NonGeneric.GenericMember<Exception> gen_exc;
	public NonGeneric.GenericMember<?> gen_wld;
	public NonGeneric.GenericMember<? extends Throwable> gen_thr;
	public NonGeneric.GenericMember<? super RuntimeException> gen_run;
	// Qualified name
	public g1.t.s.def.NonGeneric.GenericMember qgen;
	public g1.t.s.def.NonGeneric.GenericMember<Object> qgen_obj;
	public g1.t.s.def.NonGeneric.GenericMember<Exception> qgen_exc;
	public g1.t.s.def.NonGeneric.GenericMember<?> qgen_wld;
	public g1.t.s.def.NonGeneric.GenericMember<? extends Throwable> qgen_thr;
	public g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException> qgen_run;
}
