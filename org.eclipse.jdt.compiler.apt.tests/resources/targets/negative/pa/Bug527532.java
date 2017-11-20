package targets.negative.pa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_USE)
@interface A {
	
}

// There is no dedicated annotation processor for this class, other process where crashing because of the syntax error (void as field type)
public class Bug527532 {
	@A void f;
}
