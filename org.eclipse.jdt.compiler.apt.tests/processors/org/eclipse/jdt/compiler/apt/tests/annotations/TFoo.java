package org.eclipse.jdt.compiler.apt.tests.annotations;
import java.lang.annotation.*;

@Repeatable(TFooContainer.class)
@Target(ElementType.TYPE_USE)
public @interface TFoo {

}
