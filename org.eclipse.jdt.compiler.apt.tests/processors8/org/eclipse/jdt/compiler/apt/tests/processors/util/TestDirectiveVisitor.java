package org.eclipse.jdt.compiler.apt.tests.processors.util;

import javax.lang.model.element.ModuleElement.*;

public class TestDirectiveVisitor<R, P> implements DirectiveVisitor<Object, Object> {

	@Override
	public Object visitExports(ExportsDirective arg0, Object arg1) {
		return arg0;
	}

	@Override
	public Object visitOpens(OpensDirective arg0, Object arg1) {
		return arg0;
	}

	@Override
	public Object visitProvides(ProvidesDirective arg0, Object arg1) {
		return arg0;
	}

	@Override
	public Object visitRequires(RequiresDirective arg0, Object arg1) {
		return arg0;
	}

	@Override
	public Object visitUses(UsesDirective arg0, Object arg1) {
		return arg0;
	}

}