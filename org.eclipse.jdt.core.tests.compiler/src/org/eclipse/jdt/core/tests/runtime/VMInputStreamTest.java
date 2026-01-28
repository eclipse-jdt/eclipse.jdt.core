package org.eclipse.jdt.core.tests.runtime;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class VMInputStreamTest {

    @Test(timeout = 2000)
    public void testReadDoesNotRecurseInfinitelyOnIOException() throws Exception {

        InputStream failingInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("forced failure");
            }
        };

        Process runningProcess = new Process() {
            int calls = 0;

            @Override
            public int exitValue() {
                if (calls++ == 0) {
                    throw new IllegalThreadStateException(); // running once
                }
                return 0; // then finished
            }

            @Override public void destroy() {}
            @Override public InputStream getInputStream() { return null; }
            @Override public InputStream getErrorStream() { return null; }
            @Override public java.io.OutputStream getOutputStream() { return null; }
            @Override public int waitFor() { return 0; }
        };

        VMInputStream stream =
                new VMInputStream(runningProcess, failingInputStream);

        try {
            stream.read();
            fail("Expected IOException");
        } catch (IOException expected) {
            // success: no recursion, no stack overflow
        }
 // should not recurse forever
    }
}
