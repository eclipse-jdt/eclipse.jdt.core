
import java.util.List;

public class Undefined {
    Undefined(Integer x) {}

    private void testUndefinedConstructor1() {
        // compiler.err.cant.apply.symbols -> UndefinedConstructor(134217858)
        String l = new String("s", "t");
    }

    void testUndefinedConstructor2() {
        // compiler.err.cant.resolve.args -> UndefinedConstructor(134217858)
        new Undefined(""){};
    }

    private void testUndefinedType() {
        // compiler.err.cant.resolve.location -> UndefinedType(16777218)
        UndefinedType a = new UndefinedType();
    }

    private void testUndefinedMethod1() {
        // compiler.err.cant.resolve.location.args -> UndefinedMethod(67108964)
        test();
    }

    private void testUndefinedMethod2() {
        // compiler.err.cant.resolve.args.params -> UndefinedMethod(67108964)
        Object o = new Object() {
            { this.<Integer,Double>m2(1, ""); }
        };
    }

    private void testUndefinedMethod3() {
        // compiler.err.cant.resolve.args -> UndefinedMethod(67108964)
        new Runnable() {
            { unknown(); }
            public void run() { }
        };
    }

    private void testUndefinedMethod4() {
        // compiler.err.cant.resolve.location.args.params -> UndefinedMethod(67108964)
        Object o = List.<String>unknown();
    }
}
