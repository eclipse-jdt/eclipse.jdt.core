public class CompletionInsideStaticMethod {
	public static void main () {
		Object r = new Object() {
			public void run() {
				doT
			}
			private void doTheThing() { }
		};
	}
}