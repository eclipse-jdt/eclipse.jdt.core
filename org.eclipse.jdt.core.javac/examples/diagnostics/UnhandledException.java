
import java.io.IOException;

public class UnhandledException {
    public void testUnhandledException() {
        throw new IOException("IOExp");
    }

    public static class AutoCloseableClass implements AutoCloseable {
        @Override
        public void close() throws Exception {
            System.out.println("close");
        }
    }

    // compiler.err.unreported.exception.implicit.close -> UnhandledExceptionOnAutoClose(16778098)
    public void testUnhandledExceptionOnAutoClose() {
        try (AutoCloseableClass a = new AutoCloseableClass()) {
            System.out.println("try-with-resource AutoCloseableClass");
        }
    }
}
