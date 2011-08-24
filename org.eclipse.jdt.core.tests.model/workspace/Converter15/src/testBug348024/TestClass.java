package testBug348024;

public class TestClass {

	private String testField;

	public static class TestBuilder {

		private String t;

		public TestClass build() {
			return new TestClass(this);
		}

	}

	private TestClass(TestBuilder builder) {
		this.testField = builder.t;
	}

}