/*
 * package g4.v.ref is a package to define method which define and contain
 * references (ref) to local variables (v) based on parameterized types
 */
package g4.v.ref;

import g3.t.def.GM;
import g3.t.def.GS;

/*
 * This type is used to test declaration and references to local variables
 */
public class R5 {
	void simple_name() {
		GS<GM<?, ?, ?>> gen_wld = new GS<GM<?, ?, ?>>();
		GS<GM<GM<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>> gen_www = new GS<GM<GM<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>>();
		GS<GM<Object, Exception, RuntimeException>> gen_obj = new GS<GM<Object, Exception, RuntimeException>>();
		gen_wld.toString();
		gen_www.toString();
		gen_obj.toString();
	}
	void simple_name(
		GM<GS<?>, GS<?>, GS<?>> gen_wld, // simple
		GM<GS<?>, GS<GS<?>>, GS<GS<GS<?>>>> gen_www, // simple
		GM<GS<Object>, GS<? extends Throwable>, GS<? super RuntimeException>> gen_obj) // simple
	{
		gen_wld.toString();
		gen_www.toString();
		gen_obj.toString();
	}
	void qualified_name (
		g3.t.def.GS<g3.t.def.GM<?, ?, ?>> gen_wld, // qualified
		g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>>> gen_www, // qualified
		g3.t.def.GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>> gen_obj) // qualified
	{
		gen_wld.toString();
		gen_www.toString();
		gen_obj.toString();
	}
	void qualified_name () {
		g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<?>, g3.t.def.GS<?>> gen_wld = new GM<GS<?>, GS<?>, GS<?>>();
		g3.t.def.GM<g3.t.def.GS<?>, g3.t.def.GS<g3.t.def.GS<?>>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>>>> gen_www = new GM<GS<?>, GS<GS<?>>, GS<GS<GS<?>>>>();
		g3.t.def.GM<g3.t.def.GS<java.lang.Object>, g3.t.def.GS<? extends java.lang.Throwable>, g3.t.def.GS<? super java.lang.RuntimeException>> gen_obj = new GM<GS<Object>, GS<? extends Throwable>, GS<? super RuntimeException>>();
		gen_wld.toString();
		gen_www.toString();
		gen_obj.toString();
	}
}
