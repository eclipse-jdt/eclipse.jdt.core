package org.eclipse.jdt.compiler.apt.tests.annotations;

import java.lang.annotation.Inherited;

@Inherited
public @interface IFooContainer {
	IFoo [] value();
}