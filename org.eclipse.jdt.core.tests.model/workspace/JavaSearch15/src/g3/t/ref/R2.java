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
public class R2 {
	// simple name
	public GS<GM<?, ?, ?>.Member>.Member sgsm_wld;
	public GS<GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member>.Member sgsm_www;
	public GS<GM<Object, Exception, RuntimeException>.Member>.Member sgsm_obj;
	public GM<GS<?>.Member, GS<?>.Member, GS<?>.Member>.Member sgms_wld;
	public GM<GS<?>.Member, GS<GS<?>.Member>.Member, GS<GS<GS<?>.Member>.Member>.Member>.Member sgms_www;
	public GM<GS<Object>.Member, GS<? extends Throwable>.Member, GS<? super RuntimeException>.Member>.Member sgms_obj;
	// qualified name
	public g3.t.def.GS<g3.t.def.GM<?, ?, ?>.Member>.Member qgsm_wld;
	public g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member>.Member qgsm_www;
	public g3.t.def.GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member>.Member qgsm_obj;
	public g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member>.Member qgms_wld;
	public g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<g3.t.def.GS<?>.Member>.Member, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member>.Member qgms_www;
	public g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member qgms_obj;
	
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
