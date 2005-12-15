class A {
	public void foo() {
		int i = 0;
		switch (i) {
			case 0:
				// handling 0
				System.out.println("Case was 0");
				// fall through
			case 1:
				// handling 1.
				// some additional remark (also handles 0).
				System.out.println("Case was 0 or 1");
				break;
			case 2:
			// fall through
			default:
				// some default action
				System.out.println("Case was :" + i);
		}
	}
}
