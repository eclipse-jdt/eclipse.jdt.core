package test.comments.line;

public class X08 {

	void foo() {
		try {
			if (true) {
				//BUILD FAILED: C:\Darins\Debugger\20021213\eclipse\runtime-workspace\Mine\build.xml:4: Following error occured while executing this line
				//C:\Darins\Debugger\20021213\eclipse\runtime-workspace\Mine\subbuild.xml:4: srcdir attribute must be set!
			}
		}
		finally {
		}
	}
}
