public class Example {
	@SomeAnnotation(key1 = "value1", key2 = "value2")
	void method1() {
		for (int counter = 0; counter < 100; counter++) {
			if (counter % 2 == 0 && counter % 7 == 0 && counter % 13 == 0) {
				try (
						AutoCloseable resource = null
				) {
					// read resource
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Deprecated()
	void method2(
			String argument) {
		switch (argument) {
		case "1":
			this.method3(this, this, this, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
					"ccccccccccccccccccc");
			break;
		}
	}

	void method3(Example argument1, Example argument2, Example argument3, String argument4, String argument5,
			String argument6) {
		method1();
		while (argument1.toString().contains(argument4)) {
			argument1.method2(argument5);
		}
	}

	java.util.function.BiConsumer<Example, String> lambda = (Example example, String text) -> {
		do {
			example.method1();
		} while (example.toString()//
				.contains(""));
	};
	Runnable r = () -> {
	};
}

enum SomeEnum {
	VALUE1(), VALUE2("example")
}
