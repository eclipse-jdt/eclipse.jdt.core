package IncompatibleExpInThrow;

// compiler.err.override.meth.doesnt.throw -> IncompatibleExceptionInThrowsClause(67109266)
public class Sub extends Super {
    @Override
    void foo() throws Exception {

    }
}
