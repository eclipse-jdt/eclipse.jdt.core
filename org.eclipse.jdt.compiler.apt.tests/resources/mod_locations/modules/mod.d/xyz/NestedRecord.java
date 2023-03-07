package xyz;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
record NestedRecord (Color c, Point p) {
	enum Color {
		RED, BLUE, YELLOW;
	}
	record Point(int x, int y) {}
	public class NestedClass1 {
		class NestedClass2 {
		}
	}
	enum NestedEnum1 {
		X, Y;

		enum NestedEnum2 {
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