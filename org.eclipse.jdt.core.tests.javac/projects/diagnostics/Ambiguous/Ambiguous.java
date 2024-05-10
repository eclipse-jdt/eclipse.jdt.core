package Ambiguous;

import Ambiguous.pkg1.*;
import Ambiguous.pkg2.*;

public class Ambiguous {
    private void testAmbiguous1() {
        // compiler.err.ref.ambiguous -> AmbiguousType(16777220)
        A a;
        // compiler.err.ref.ambiguous -> AmbiguousMethod(67108966)
        method(1, 2);
    }

    void method(int i, double d) {
    }

    void method(double d, int m) {
    }
}
