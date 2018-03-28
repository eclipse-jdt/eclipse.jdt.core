@java.lang.Deprecated()
open module mod.a {
	exports abc.internal;

	requires transitive java.compiler;
	requires transitive java.sql;
}
