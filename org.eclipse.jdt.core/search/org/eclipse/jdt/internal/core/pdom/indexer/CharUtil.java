package org.eclipse.jdt.internal.core.pdom.indexer;

/**
 */
public class CharUtil {

	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	/**
	 * Answers a new array which is the concatenation of all the given arrays.
	 *
	 * @param toCatenate
	 * @return
	 * @since 3.12
	 */
	public static char[] concat(char[]... toCatenate) {
		int totalSize = 0;
		for (char[] next: toCatenate) {
			totalSize += next.length;
		}

		char[] result = new char[totalSize];
		int writeIndex = 0;
		for (char[] next: toCatenate) {
			System.arraycopy(next, 0, result, writeIndex, next.length);
			writeIndex += next.length;
		}
		return result;
	}

	public static boolean startsWith(char[] fieldDescriptor, char c) {
		return fieldDescriptor.length > 0 && fieldDescriptor[0] == c;
	}

	public static char[] substring(char[] inputString, int index) {
		if (inputString.length <= index) {
			return EMPTY_CHAR_ARRAY;
		}

		char[] result = new char[inputString.length - index];
		System.arraycopy(inputString, index, result, 0, result.length);
		return result;
	}

}
