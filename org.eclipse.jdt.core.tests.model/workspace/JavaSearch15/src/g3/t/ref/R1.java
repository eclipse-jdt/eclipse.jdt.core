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
public class R1 {
	// simple name
	public GS<GM<?, ?, ?>> sgsm_wld;
	public GS<GM<GM<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>> sgsm_www;
	public GS<GM<Object, Exception, RuntimeException>> sgsm_obj;
	public GM<GS<?>, GS<?>, GS<?>> sgms_wld;
	public GM<GS<?>, GS<GS<?>>, GS<GS<GS<?>>>> sgms_www;
	public GM<GS<Object>, GS<? extends Throwable>, GS<? super RuntimeException>> sgms_obj;
	// qualified name
	public g3.t.def.GS<g3.t.def.GM<?, ?, ?>> qgsm_wld;
	public g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>>> qgsm_www;
	public g3.t.def.GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>> qgsm_obj;
	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<?>, g3.t.def.GS<?>> qgms_wld;
	public g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<g3.t.def.GS<?>>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>>>> qgms_www;
	public g3.t.def.GM<g3.t.def.GS<java.lang.Object>, g3.t.def.GS<? extends java.lang.Throwable>, g3.t.def.GS<? super java.lang.RuntimeException>> qgms_obj;
	
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
