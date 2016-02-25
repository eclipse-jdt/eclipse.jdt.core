package test1;

import org.eclipse.jdt.annotation.*;
import libs.*;

class A {}
class B {}
class C {}

@NonNullByDefault
public class Reconcile3 {
	void test1(MyFunction<A,@Nullable B> f1) {
		// nothing
	}
	
	void test2(MyFunction<A,@NonNull B> f2a, MyFunction<@Nullable String,@NonNull A> f2b) {
		f2a.<@Nullable String>compose(f2b);
	}
}