package test1;

import org.eclipse.jdt.annotation.*;
import java.util.function.*;


class A {}
class B {}
class C {}

@NonNullByDefault
public class Test3 {
	C test(Function<A,@Nullable B> f1, Function<B,C> f2, A a) {
		return f2.compose(f1).apply(a); // actually incompatible, but we tweak compose to pretend it's compatible
	}
}