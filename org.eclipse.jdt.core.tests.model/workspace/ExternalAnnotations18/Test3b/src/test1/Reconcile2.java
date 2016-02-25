package test1;

import org.eclipse.jdt.annotation.*;
import libs.*;


class A {}
class B {}
class C {}

@NonNullByDefault
public class Reconcile2 {
	C test(MyFunction<A,@Nullable B> f1, MyFunction<B,C> f2, A a) {
		return f2.compose(f1).apply(a); // actually incompatible, but we tweak compose to pretend it's compatible
	}
	
	void test2(Arrays lib) {
		@Nullable String[]@NonNull[] arr = lib.getArray();
		if (arr == null)
			throw new NullPointerException(); // not dead code
		@Nullable String @NonNull[] arr2 = Arrays.array;
		Arrays.array[1] = null;
	}
}