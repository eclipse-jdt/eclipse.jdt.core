@java.lang.Deprecated()
module mod.a {
	exports abc.internal;

	requires transitive java.compiler;
	requires transitive java.sql;
}
