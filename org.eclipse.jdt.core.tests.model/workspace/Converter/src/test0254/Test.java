package test0254;
import java.util.*;
public class Test {

	class C {
	}

	Object foo() {
		return new Test().new C();
	}
}
