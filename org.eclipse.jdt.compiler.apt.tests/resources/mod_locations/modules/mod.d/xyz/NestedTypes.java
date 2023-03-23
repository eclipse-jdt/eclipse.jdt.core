package xyz;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class NestedTypes{
	record NestedRecord() {
		record NestedRecord1() {
			record NestedRecord2() {}
		}
	}
	enum NestedEnum1 {
		CONST1;
		enum NestedEnum2 {}
	}
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Anno1 {
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Anno2 {
			public @interface Anno3 {}
		}
	}
}