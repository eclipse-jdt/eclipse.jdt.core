package q8;
/* Test case for bug 5821 Refactor > Rename renames local variable instead of member in case of name clash  */
public class EclipseTest {
    public int test = 0;

    public static void main(String[] args) {
        EclipseTest test = new EclipseTest();

        test.test = 1;
    }
}
