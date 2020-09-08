@PackageAnnotation @OtherPackageAnnotation("argument") @YetAnotherPackageAnnotation(key1 = "value1", key2 = "value2")
@TestAnnotation
package org.eclipse.jdt.core.tests.formatter.test545078;

@ClassAnnotation @OtherClassAnnotation("argument") @YetAnotherClassAnnotation(key1 = "value1", key2 = "value2") @TestAnnotation
public class Test545078 {

	@MethodAnnotation("argument")
	@YetAnotherMethodAnnotation("argument")
	@OtherMethodAnnotation("argument") public String f() {

		@VariableAnnotation
		@OtherVariableAnnotation
		@YetAnotherVariableAnnotaion
		@TestAnnotation
		Object o = new Object() {

			@FirstFieldAnnotation(a = "a", b = "b")
			@SecondFieldAnnotation(c = "c", d = "d")
			@FieldAnnotation(e = "e", f = "f")
			public final @TypeAnnotation @OtherTypeAnnotation @YetAnother @TestAnnotation String field = "";

			@Override public String toString() {
				return field;
			}
		};
	}

	public void g(
			@ParameterAnnotation(key1 = "value1", key2 = "value2") @OtherParameterAnnotation() @ExtraParamAnno String arg1) {
		//
	}

	enum Enum1 {
		@A1
		@A2
		@A3
		CONSTANT1, @A4
		@A5
		@A6
		CONSTANT2;
	}
}