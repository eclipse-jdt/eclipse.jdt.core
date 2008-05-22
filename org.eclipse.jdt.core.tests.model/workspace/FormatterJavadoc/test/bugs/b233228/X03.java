package test.bugs.b233228;
public class X03 {
	void foo() {
		if (true) {
			/* Destroy the new icon src and mask and hdc's*/
			destroy();
		}
	}

	void destroy() {
	}

}
