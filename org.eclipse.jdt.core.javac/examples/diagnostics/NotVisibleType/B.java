package NotVisibleType;

public class B {
    public void testNotVisibleType() {
        // compiler.err.report.access -> NotVisibleType(16777219)
        A.Inner i = new A.Inner();
    }
}