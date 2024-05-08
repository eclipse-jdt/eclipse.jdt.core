package notVisibleField;

public class B {
    public void testNotVisibleField() {
        A a = new A();
        // compiler.err.report.access -> NotVisibleField(33554503)
        a.a = 1;
    }
}