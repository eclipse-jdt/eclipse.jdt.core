package javax.lang.model;

public enum SourceVersion {

	RELEASE_0,

	RELEASE_1,

	RELEASE_2,

	RELEASE_3,

	RELEASE_4,

	RELEASE_5,

	RELEASE_6,

	RELEASE_7,

	RELEASE_8, 
	RELEASE_9,

	RELEASE_10,

	RELEASE_11,

	RELEASE_12,

	RELEASE_13, 
	RELEASE_14;

	public static SourceVersion latest() {
		return RELEASE_14;
	}

	private static final SourceVersion latestSupported = getLatestSupported();

	private static SourceVersion getLatestSupported() {
		return RELEASE_14;
	}

	public static SourceVersion latestSupported() {
		return latestSupported;
	}

	public static boolean isIdentifier(CharSequence name) {
		return true;
	}

	public static boolean isName(CharSequence name) {
		return isName(name, latest());
	}

	public static boolean isName(CharSequence name, SourceVersion version) {
		return true;
	}

	public static boolean isKeyword(CharSequence s) {
		return isKeyword(s, latest());
	}

	public static boolean isKeyword(CharSequence s, SourceVersion version) {
		return true;
	}
}
