
public class MethodReturnsVoid {
    public void testVoidMethod() {

    }

    public String testMethodReturnsVoid() {
        // compiler.err.prob.found.req -> MethodReturnsVoid(67108969)
        return testVoidMethod();
    }
}