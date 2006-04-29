public class Test {

	public static <T> void service() {
	}

	public static void main(String[] args) {
		Test.<String>service(); //XXX <<<<
		Test t = new Test();
		t.<String>service2(); //XXX <<<<
		new Test().<String>service2(); //XXX <<<<
	}

	public <T> void service3() {
	}

	public <T> void service2() {
		this.<T>service3(); //XXX <<<<
	}
}
