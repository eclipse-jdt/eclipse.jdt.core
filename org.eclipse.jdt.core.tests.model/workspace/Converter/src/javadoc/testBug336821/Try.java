package javadoc.testBug336821;
/**
 * First ref should resolve to constructor
 * @see #Try
 * @see #Try(int, String)
 * @see #foo
 */
public class Try {
    public Try(int i, String message) {
        System.out.println(message + i);
    }

    public void foo(int i, String message) {
        System.out.println(message + i);
    }
}