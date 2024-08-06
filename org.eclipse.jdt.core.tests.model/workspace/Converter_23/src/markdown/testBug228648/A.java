package javadoc.testBug228648;
import javadoc.testBug228648.B.Inner;
/**
 * {@link #foo(Inner)}
 * {@link #foo2(B)}
 */
public class A {
        public void foo(Inner inner) {
        }
        public void foo2(B b) {
        }
}
