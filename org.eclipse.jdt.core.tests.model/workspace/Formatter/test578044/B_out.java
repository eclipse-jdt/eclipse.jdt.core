class Example {
	boolean foo(Color color) {
		boolean b = switch (color) {
		case RED, GREEN, BLACK, BLUE, CYAN, ORANGE, WHITE,
				PINK -> true;
		case PURPLE, YELLOW, BROWN, GRAY, INDIGO, OLIVE,
				VIOLET -> {
			logger.warn(
					"processing special color: " + color);
			return true;
		}
		default -> false;
		};
		switch (color) {
		case RED, GREEN, BLACK, BLUE, CYAN, ORANGE, WHITE,
				PINK:
			return true;
		default:
			return false;
		}
	}
}