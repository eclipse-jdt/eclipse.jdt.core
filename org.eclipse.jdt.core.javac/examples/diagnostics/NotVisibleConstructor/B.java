package NotVisibleConstructor;

public class B {
    public void testNotVisibleConstructor() {
        // compiler.err.report.access -> NotVisibleConstructor(134217859)
        A a = new A("a");
    }
}
