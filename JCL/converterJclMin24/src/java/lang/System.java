package java.lang;

import java.io.InputStream;
import java.io.PrintStream;

public final class System {
    private static native void registerNatives();
    static {
        registerNatives();
    }

    private System() {
    }

    public static final InputStream in = null;

    public static final PrintStream out = null;


}
