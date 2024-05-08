package NotVisibleMethod;

public class B {
    public void testNotVisibleMethod() {
        A a = new A();
        // compiler.err.report.access -> NotVisibleMethod(67108965)
        a.a();
    }
}