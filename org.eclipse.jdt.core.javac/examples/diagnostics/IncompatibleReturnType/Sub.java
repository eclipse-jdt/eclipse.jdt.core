package IncompatibleReturnType;

// compiler.err.override.incompatible.ret -> UndefinedAnnotationMember(67109475)
public class Sub extends Super {
    @Override
    void foo() {

    }
}
