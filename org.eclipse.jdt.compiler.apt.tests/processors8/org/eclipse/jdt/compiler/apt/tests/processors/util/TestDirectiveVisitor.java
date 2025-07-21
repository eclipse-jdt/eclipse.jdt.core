package org.eclipse.jdt.compiler.apt.tests.processors.util;

import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.ModuleElement.UsesDirective;

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