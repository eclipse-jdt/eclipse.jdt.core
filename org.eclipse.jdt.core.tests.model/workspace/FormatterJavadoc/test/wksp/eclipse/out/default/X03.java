package test.wksp.eclipse;

public class X03 {

	void foo() {
		if (((OS.DLGC_WANTARROWS /* | OS.DLGC_WANTALLKEYS */)) != 0) {
		}
	}
}

class OS {
	static final int DLGC_WANTARROWS = 0;
}
