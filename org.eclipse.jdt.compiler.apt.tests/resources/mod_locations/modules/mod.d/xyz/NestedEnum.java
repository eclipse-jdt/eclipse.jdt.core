package xyz;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public enum NestedEnum {
	X, Y;
	class NestedEnum1 {
		class NestedEnum2 {}
	}
	interface NestedEnumI {
		interface NestedEnumI1 {
		}
	}
	record NestedRecord() {
		record NestedRecord1() {
			record NestedRecord2() {
			}
		}
	}
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Anno1 {
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Anno2 {
			public @interface Anno3 {
			}
		}
	}
}
