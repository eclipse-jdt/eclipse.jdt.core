package xyz;

record NestedRecord (Color c, Point p) {
	enum Color {
		RED, BLUE, YELLOW;
	}
	record Point(int x, int y) {
	}
}