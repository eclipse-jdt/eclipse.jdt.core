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
	public NonGeneric.GenericMember ygm;
	public NonGeneric.GenericMember<Object, Exception, RuntimeException> ygm_obj;
	public NonGeneric.GenericMember<Exception, Exception, RuntimeException> ygm_exc;
	public NonGeneric.GenericMember<?, ?, ?> ygm_qmk;
	public NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException> ygm_thr;
	public NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> ygm_run;
	// Qualified name
	public g1.t.m.def.NonGeneric.GenericMember qygm;
	public g1.t.m.def.NonGeneric.GenericMember<Object, Exception, RuntimeException> qygm_obj;
	public g1.t.m.def.NonGeneric.GenericMember<Exception, Exception, RuntimeException> qygm_exc;
	public g1.t.m.def.NonGeneric.GenericMember<?, ?, ?> qygm_qmk;
	public g1.t.m.def.NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException> qygm_thr;
	public g1.t.m.def.NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException> qygm_run;
}
