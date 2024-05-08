
import java.util.List;
import java.util.ArrayList;

public class TypeMismatch {
    private void testTypeMismatch() {
        // compiler.err.illegal.initializer.for.type -> TypeMismatch(16777233)
        String a = { "a", "b" };
    }

    private void testTypeMismatch1() {
        // compiler.err.prob.found.req -> TypeMismatch(16777233)
        String a = new String[] { "a", "b" };
    }

    private String testReturnTypeMismatch() {
        // compiler.err.prob.found.req -> ReturnTypeMismatch(16777235)
        return new String[] { "a", "b" };
    }


    private void testIncompatibleTypesInForeach() {
        List<Integer> intList = new ArrayList<>();
        // compiler.err.prob.found.req -> IncompatibleTypesInForeach(16777796)
        for (String s : intList) {
            s.hashCode();
        }
    }
}
