/*
 * package g3.t.ref is second package to define types which contain
 * references (ref) to generic (g1) types (t)
 */
package g3.t.ref;

import g3.t.def.GM;
import g3.t.def.GS;

/*
 * This type is used to test references to generic type with nested parameterized types
 */
public class R3 {
	// simple name
	public GS<GM<?, ?, ?>.Generic<?, ?, ?>>.Generic<?> sgsm_wld;
	public GS<GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic<?> sgsm_www;
	public GS<GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic<Exception> sgsm_obj;
	public GM<GS<?>.Generic<?>, GS<?>.Generic<?>, GS<?>.Generic<?>>.Generic<?,?,?> sgms_wld;
	public GM<GS<?>.Generic<?>, GS<GS<?>.Generic<?>>.Generic<?>, GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic<?,?,?> sgms_www;
	public GM<GS<Object>.Generic<?>, GS<? extends Throwable>.Generic<?>, GS<? super RuntimeException>.Generic<?>>.Generic<?,?,?> sgms_obj;
	// qualified name
	public g3.t.def.GS<g3.t.def.GM<?, ?, ?>.Generic<?, ?, ?>>.Generic<?> qgsm_wld;
	public g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic<?> qgsm_www;
	public g3.t.def.GS<g3.t.def.GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic<Exception> qgsm_obj;
	public g3.t.def.GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>>.Generic<?,?,?> qgms_wld;
	public g3.t.def.GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic<?,?,?> qgms_www;
	public g3.t.def.GM<g3.t.def.GS<Object>.Generic<?>, g3.t.def.GS<? extends Throwable>.Generic<?>, g3.t.def.GS<? super RuntimeException>.Generic<?>>.Generic<?,?,?> qgms_obj;
	
	{
		sgsm_wld.toString();
		sgsm_www.toString();
		sgsm_obj.toString();
		sgms_wld.toString();
		sgms_www.toString();
		sgms_obj.toString();
		qgsm_wld.toString();
		qgsm_www.toString();
		qgsm_obj.toString();
		qgms_wld.toString();
		qgms_www.toString();
		qgms_obj.toString();
	}
}
