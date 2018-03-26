package targets.model9;

import java.lang.annotation.Documented;

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;

class Y_s {
    void m() {
    }
}

public class Y extends Y_s {
	 @Deprecated void m() {
    }
}

