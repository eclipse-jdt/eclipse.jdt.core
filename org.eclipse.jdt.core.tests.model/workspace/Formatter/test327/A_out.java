public class A {
	void foo() {
		boolean b = true;
		if (b)
			for (int i = 0; i < 10; i++) {
				System.out.println(i);
			}
		else
			System.out.println("nothing to do");
	}
}