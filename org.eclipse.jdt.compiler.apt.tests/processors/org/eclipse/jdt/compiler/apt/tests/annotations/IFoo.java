package org.eclipse.jdt.compiler.apt.tests.annotations;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;

@Repeatable(IFooContainer.class)
@Inherited
public @interface IFoo {
	int value() default -1;
}

