/*
 * package g3.t.ref is second package to define types which contain
 * references (ref) to generic (g1) types (t)
 */
package g3.t.ref;

import g3.t.def.NGM;
import g3.t.def.NGS;

/*
 * This type is used to test references to generic type with nested parameterized types
 */
public class R4 {
	// simple name
	public NGS.Generic<NGM.Generic<?, ?, ?>> sgsm_wld;
	public NGS.Generic<NGM.Generic<NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>>> sgsm_www;
	public NGS.Generic<NGM.Generic<Object, Exception, RuntimeException>> sgsm_obj;
	public NGM.Generic<NGS.Generic<?>, NGS.Generic<?>, NGS.Generic<?>> sgms_wld;
	public NGM.Generic<NGS.Generic<?>, NGS.Generic<NGS.Generic<?>>, NGS.Generic<NGS.Generic<NGS.Generic<?>>>> sgms_www;
	public NGM.Generic<NGS.Generic<Object>, NGS.Generic<? extends Throwable>, NGS.Generic<? super RuntimeException>> sgms_obj;
	// qualified name
	public g3.t.def.NGS.Generic<g3.t.def.NGM.Generic<?, ?, ?>> qgsm_wld;
	public g3.t.def.NGS.Generic<g3.t.def.NGM.Generic<g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>>> qgsm_www;
	public g3.t.def.NGS.Generic<g3.t.def.NGM.Generic<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>> qgsm_obj;
	public g3.t.def.NGM.Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>> qgms_wld;
	public g3.t.def.NGM.Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>>> qgms_www;
	public g3.t.def.NGM.Generic<g3.t.def.NGS.Generic<java.lang.Object>, g3.t.def.NGS.Generic<? extends java.lang.Throwable>, g3.t.def.NGS.Generic<? super java.lang.RuntimeException>> qgms_obj;
	
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
