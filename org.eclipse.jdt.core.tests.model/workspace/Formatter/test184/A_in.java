public class X {
	X(String s) {
	}
	protected void foo() {
		Main
				.bind("compile.instantTime", //$NON-NLS-1$
						new String[]{String.valueOf(this.lineCount), String.valueOf(this.time), String.valueOf(((int) (this.lineCount * 10000.0 / this.time)) / 10.0)});
	}
}