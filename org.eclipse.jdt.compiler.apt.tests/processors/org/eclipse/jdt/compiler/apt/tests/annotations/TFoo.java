package org.eclipse.jdt.compiler.apt.tests.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Repeatable(TFooContainer.class)
@Target(ElementType.TYPE_USE)
public @interface TFoo {

}
