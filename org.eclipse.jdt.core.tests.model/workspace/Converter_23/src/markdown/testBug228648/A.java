package javadoc.testBug228648;
import javadoc.testBug228648.B.Inner;
///
/// #foo(Inner)}
/// #foo2(B)}
///
public class A {
    public void foo(Inner inner) {
    }
    public void foo2(B b) {
    }
}
