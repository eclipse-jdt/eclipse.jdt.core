public void foo(String s) {
	synchronized (this) {
		System.out.println(s.length());
	}
}