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
	public NonGeneric.GenericMember xgm;
	public NonGeneric.GenericMember<Object> xgm_obj;
	public NonGeneric.GenericMember<Exception> xgm_exc;
	public NonGeneric.GenericMember<?> xgm_qmk;
	public NonGeneric.GenericMember<? extends Throwable> xgm_thr;
	public NonGeneric.GenericMember<? super RuntimeException> xgm_run;
	// Qualified name
	public g1.t.s.def.NonGeneric.GenericMember qxgm;
	public g1.t.s.def.NonGeneric.GenericMember<Object> qxgm_obj;
	public g1.t.s.def.NonGeneric.GenericMember<Exception> qxgm_exc;
	public g1.t.s.def.NonGeneric.GenericMember<?> qxgm_qmk;
	public g1.t.s.def.NonGeneric.GenericMember<? extends Throwable> qxgm_thr;
	public g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException> qxgm_run;
}
