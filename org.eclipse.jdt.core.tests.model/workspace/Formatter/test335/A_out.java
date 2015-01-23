public class Test {
	protected void doSomeStuff() {
		/*
		 * Comment
		 */
		doSomething(Expression.class, "Sting value for something",
				new Object() {
					public Something buildSomething() {
						// Comment
						Expression s = getBuilder().get("name").equal(
								getArgument("name", String.class, 0));
						return s;
					}
				});
	}
}