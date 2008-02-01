@OnMember @Retention package pkg1;

@interface Name {
	String first() default "Joe";

	String last() default "Smith";

	int age();
}

@interface Author {
	Name value();
}

@interface Retention {
}

@interface OnMember {
}

@interface OnParameter {
	String value();
}

@interface OnLocalVariable {
}

@OnMember @Retention @Author(@Name(first = "Jdt", last = "Core", age = 32)) public class X {

	@OnMember @Retention private String aString;

	@OnMember @Author(@Name(first = "John", last = "Doe", age = 32)) X() {

	}

	@OnMember public void bar(@OnParameter("unused value") int i,
			@OnParameter("unused value") String s) {
		@OnLocalVariable
		@Retention
		String localString = "string";
	}
}
