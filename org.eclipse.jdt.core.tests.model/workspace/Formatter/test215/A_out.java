public class TestFormatCode {
	class InnerClass {
		public void innerMethod() {
		}
	}
	public void outerMethod() throws Exception {
		Object anonymousClass = new Object() {
			public void anonymousMethod() {
			}
		};
		synchronized (this) {
		}
	}
}