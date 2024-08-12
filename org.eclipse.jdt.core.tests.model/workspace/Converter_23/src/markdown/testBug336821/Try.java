package javadoc.testBug336821;
///
/// constructor
/// #Try
/// String)
/// #foo
///
public class Try {
    public Try(int i, String message) {
        System.out.println(message + i);
    }

    public void foo(int i, String message) {
        System.out.println(message + i);
    }
}