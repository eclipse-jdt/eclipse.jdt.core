
public class Unresolved {
    private void testUndefinedField() {
        // compiler.err.cant.resolve -> UndefinedField(33554502)
        String test = this.str;
        
    }

    private void testUnresolvedVariable1() {
        // compiler.err.cant.resolve.location -> UnresolvedVariable(33554515)
        String test = str;

        Object o = new Object() {
            // compiler.err.cant.resolve -> UnresolvedVariable(33554515)
            int i = f;
        };
    }

    private void testUndefinedName() {
        // compiler.err.cant.resolve.location -> UndefinedName(570425394)
        String test = K.Strin();
    }

}

@interface Anno {
    String name() default "anon";
    String address() default "here";
}

// compiler.err.cant.resolve -> UnresolvedVariable(33554515)
@Anno(name == "fred", address = "there")
class X { }