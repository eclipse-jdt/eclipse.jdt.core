package org.eclipse.jdt.internal.compiler.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public final class CharOperation {
public static final char[][] arrayConcat(char[][] first, char[][] second) {
	if (first == null)
		return second;
	if (second == null)
		return first;

	int length1 = first.length;
	int length2 = second.length;
	char[][] result = new char[length1 + length2][];
/* if we do not trust System.arraycopy on our VM with char[][]'s
	int i;
	for (i = 0; i < length1; i++)
		result[i] = first[i];
	for (int j = 0; j < length2; j++)
		result[i++] = second[j];
*/
	System.arraycopy(first, 0, result, 0, length1);
	System.arraycopy(second, 0, result, length1, length2);
	return result;
}
public static final char[][] arrayConcat(char[][] first, char[] second) {
	if (second == null)
		return first;
	if (first == null)
		return new char[][] {second};

	int length = first.length;
	char[][] result = new char[length + 1][];
/* if we do not trust System.arraycopy on our VM with char[][]'s
	for (int i = 0; i < length; i++)
		result[i] = first[i];
*/
	System.arraycopy(first, 0, result, 0, length);
	result[length] = second;
	return result;
}
public static final char[] concat(char[] first, char[] second) {
	if (first == null)
		return second;
	if (second == null)
		return first;

	int length1 = first.length;
	int length2 = second.length;
	char[] result = new char[length1 + length2];
	System.arraycopy(first, 0, result, 0, length1);
	System.arraycopy(second, 0, result, length1, length2);
	return result;
}
public static final char[] concat(char[] first, char[] second, char[] third) {
	if (first == null)
		return concat(second, third);
	if (second == null)
		return concat(first, third);
	if (third == null)
		return concat(first, second);

	int length1 = first.length;
	int length2 = second.length;
	int length3 = third.length;
	char[] result = new char[length1 + length2 + length3];
	System.arraycopy(first, 0, result, 0, length1);
	System.arraycopy(second, 0, result, length1, length2);
	System.arraycopy(third, 0, result, length1 + length2, length3);
	return result;
}
public static final char[] concat(char[] first, char[] second, char separator) {
	if (first == null)
		return second;
	if (second == null)
		return first;

	int length1 = first.length;
	if (length1 == 0)
		return second;
	int length2 = second.length;
	if (length2 == 0)
		return first;

	char[] result = new char[length1 + length2 + 1];
	System.arraycopy(first, 0, result, 0, length1);
	result[length1] = separator;
	System.arraycopy(second, 0, result, length1 + 1, length2);
	return result;
}
public static final char[] concat(char[] first, char sep1, char[] second, char sep2, char[] third) {
	if (first == null)
		return concat(second, third, sep2);
	if (second == null)
		return concat(first, third, sep1);
	if (third == null)
		return concat(first, second, sep1);

	int length1 = first.length;
	int length2 = second.length;
	int length3 = third.length;
	char[] result = new char[length1 + length2 + length3 + 2];
	System.arraycopy(first, 0, result, 0, length1);
	result[length1] = sep1;
	System.arraycopy(second, 0, result, length1 + 1, length2);
	result[length1+length2+1] = sep2;
	System.arraycopy(third, 0, result, length1 + length2 + 2, length3);
	return result;
}
public static final char[] concat(char prefix, char[] array, char suffix) {
	if (array == null)
		return new char[] {prefix, suffix};

	int length = array.length;
	char[] result = new char[length + 2];
	result[0] = prefix;
	System.arraycopy(array, 0, result, 1, length);
	result[length + 1] = suffix;
	return result;
}
public static final char[] concatWith(char[][] array, char[] name, char separator) {
	int nameLength = name == null ? 0 : name.length;
	if (nameLength == 0)
		return concatWith(array, separator);

	int length = array == null ? 0 : array.length;
	if (length == 0)
		return name;

	int size = nameLength;
	int index = length;
	while (--index >= 0)
		if (array[index].length > 0)
			size += array[index].length + 1;
	char[] result = new char[size];
	index = 0;
	for (int i = 0; i < length; i++) {
		int subLength = array[i].length;
		if (subLength > 0) {
			System.arraycopy(array[i], 0, result, index, subLength);
			index += subLength;
			result[index++] = separator;
		}
	}
	System.arraycopy(name, 0, result, index, nameLength);
	return result;
}
public static final char[] concatWith(char[][] array, char separator) {
	int length = array == null ? 0 : array.length;
	if (length == 0)
		return TypeConstants.NoChar;

	int size = length - 1;
	int index = length;
	while (--index >= 0) {
		if (array[index].length == 0)
			size--;
		else
			size += array[index].length;
	}
	if (size <= 0)
		return TypeConstants.NoChar;
	char[] result = new char[size];
	index = length;
	while (--index >= 0) {
		length = array[index].length;
		if (length > 0) {
			System.arraycopy(array[index], 0, result, (size -= length), length);
			if (--size >= 0)
				result[size] = separator;
		}
	}
	return result;
}
public static final boolean contains(char character, char[][] array) {
	for (int i = array.length; --i >= 0;) {
		char[] subarray = array[i];
		for (int j = subarray.length; --j >= 0;)
			if (subarray[j] == character)
				return true;
	}
	return false;
}
public static final boolean contains(char character, char[] array) {
	for (int i = array.length; --i >= 0;)
		if (array[i] == character)
			return true;
	return false;
}
public static final char[][] deepCopy(char[][] toCopy) {
	int toCopyLength = toCopy.length;
	char[][] result = new char[toCopyLength][];
	for (int i = 0; i < toCopyLength; i++) {
		char[] toElement = toCopy[i];
		int toElementLength = toElement.length;
		char[] resultElement = new char[toElementLength];
		System.arraycopy(toElement, 0, resultElement, 0, toElementLength);
		result[i] = resultElement;
	}
	return result;
}
public static final boolean equals(char[][] first, char[][] second) {
	if (first == second)
		return true;
	if (first == null || second == null)
		return false;
	if (first.length != second.length)
		return false;

	for (int i = first.length; --i >= 0;)
		if (!equals(first[i], second[i]))
			return false;
	return true;
}
public static final boolean equals(char[][] first, char[][] second, boolean isCaseSensitive) {

	if (isCaseSensitive){
		return equals(first, second);
	}
	if (first == second)
		return true;
	if (first == null || second == null)
		return false;
	if (first.length != second.length)
		return false;

	for (int i = first.length; --i >= 0;)
		if (!equals(first[i], second[i], false))
			return false;
	return true;
}
public static final boolean equals(char[] first, char[] second) {
	if (first == second)
		return true;
	if (first == null || second == null)
		return false;
	if (first.length != second.length)
		return false;

	for (int i = first.length; --i >= 0;)
		if (first[i] != second[i])
			return false;
	return true;
}
public static final boolean equals(char[] first, char[] second, boolean isCaseSensitive) {

	if (isCaseSensitive){
		return equals(first, second);
	}
	if (first == second)
		return true;
	if (first == null || second == null)
		return false;
	if (first.length != second.length)
		return false;

	for (int i = first.length; --i >= 0;)
		if (Character.toLowerCase(first[i]) != Character.toLowerCase(second[i]))
			return false;
	return true;
}
public static final boolean fragmentEquals(char[] fragment, char[] name, int startIndex, boolean isCaseSensitive) {

	int max = fragment.length;
	if (name.length < max+startIndex) return false;
	if (isCaseSensitive){
		for (int i = max; --i >= 0;) // assumes the prefix is not larger than the name
			if (fragment[i] != name[i + startIndex])
				return false;
		return true;
	}
	for (int i = max; --i >= 0;) // assumes the prefix is not larger than the name
		if (Character.toLowerCase(fragment[i]) != Character.toLowerCase(name[i + startIndex]))
			return false;
	return true;
}
public static final int hashCode(char[] array) {
	int hash = 0;
	int offset = 0;
	int length = array.length;
	if (length < 16) {
		for (int i = length; i > 0; i--)
			hash = (hash * 37) + array[offset++];
	} else {
		// only sample some characters
		int skip = length / 8;
		for (int i = length; i > 0; i -= skip, offset += skip)
			hash = (hash * 39) + array[offset];
	}
	return hash & 0x7FFFFFFF;
}
public static final int indexOf(char toBeFound, char[] array) {
	for (int i = 0; i < array.length; i++)
		if (toBeFound == array[i])
			return i;
	return -1;
}
public static final int indexOf(char toBeFound, char[] array, int start) {
	for (int i = start; i < array.length; i++)
		if (toBeFound == array[i])
			return i;
	return -1;
}
public static final int lastIndexOf(char toBeFound, char[] array) {
	for (int i = array.length; --i >= 0;)
		if (toBeFound == array[i])
			return i;
	return -1;
}
public static final int lastIndexOf(char toBeFound, char[] array, int startIndex) {
	for (int i = array.length; --i >= startIndex;)
		if (toBeFound == array[i])
			return i;
	return -1;
}
public static final int lastIndexOf(char toBeFound, char[] array, int startIndex, int endIndex) {
	for (int i = endIndex; --i >= startIndex;)
		if (toBeFound == array[i])
			return i;
	return -1;
}
/**
 * Answer the last portion of a name given a separator
 * e.g. lastSegment("java.lang.Object".toCharArray(),'.') --> Object
 */
final static public char[] lastSegment(char[] array, char separator) {
	int pos = lastIndexOf(separator, array);
	if (pos < 0) return array;
	return subarray(array, pos+1, array.length);
}
/**
 * char[] pattern matching, accepting wild-cards '*'.
 *
 * When not case sensitive, the pattern is assumed to already be lowercased, the
 * name will be lowercased character per character as comparing.
 */
public static final boolean match(char[] pattern, char[] name, boolean isCaseSensitive) {

	if (name == null) return false; // null name cannot match
	if (pattern == null) return true; // null pattern is equivalent to '*'
	int iPattern = 0, patternLength = pattern.length;
	int iName = 0, nameLength = name.length;

	/* check first segment */
	char patternChar = 0;
	while ((iPattern < patternLength) && (patternChar = pattern[iPattern]) != '*'){
		if (iName == nameLength) return false;
		if (patternChar != (isCaseSensitive 
								? name[iName] 
								: Character.toLowerCase(name[iName]))){
			return false;
		}
		iName++;
		iPattern++;
	}
	/* check sequence of star+segment */
	int segmentStart;
	if (patternChar == '*'){
		segmentStart = ++iPattern; // skip star
	} else {
		segmentStart = 0; // force iName check
	}
	int prefixStart = iName;
	checkSegment: while (iName < nameLength && iPattern < patternLength){
		/* segment is ending */
		if ((patternChar = pattern[iPattern]) == '*'){
			segmentStart = ++iPattern; // skip start
			prefixStart = iName;
			continue checkSegment;
		}
		/* chech current name character */
		if ((isCaseSensitive 
				? name[iName] 
				: Character.toLowerCase(name[iName]))!= patternChar){
			iPattern = segmentStart; // mismatch - restart current segment
			iName = ++prefixStart;
			continue checkSegment;
		}
		iName++;
		iPattern++;
	}

	return (segmentStart == patternLength)
			|| (iName == nameLength && iPattern == patternLength)	
			|| (iPattern == patternLength - 1 && pattern[iPattern] == '*'); 
}
public static final int occurencesOf(char toBeFound, char[] array) {
	int count = 0;
	for (int i = 0; i < array.length; i++)
		if (toBeFound == array[i]) count++;
	return count;
}
public static final int occurencesOf(char toBeFound, char[] array, int start) {
	int count = 0;
	for (int i = start; i < array.length; i++)
		if (toBeFound == array[i]) count++;
	return count;
}
public static final boolean prefixEquals(char[] prefix, char[] name) {

	int max = prefix.length;
	if (name.length < max) return false;
	for (int i = max; --i >= 0;) // assumes the prefix is not larger than the name
		if (prefix[i] != name[i])
			return false;
	return true;
}
public static final boolean prefixEquals(char[] prefix, char[] name, boolean isCaseSensitive) {

	int max = prefix.length;
	if (name.length < max) return false;
	if (isCaseSensitive){
		for (int i = max; --i >= 0;) // assumes the prefix is not larger than the name
			if (prefix[i] != name[i])
				return false;
		return true;
	}
	
	for (int i = max; --i >= 0;) // assumes the prefix is not larger than the name
		if (Character.toLowerCase(prefix[i]) != Character.toLowerCase(name[i]))
			return false;
	return true;
}
public static final void replace(
	char[] array, 
	char toBeReplaced, 
	char replacementChar) {
	if (toBeReplaced != replacementChar) {
		for (int i = 0, max = array.length; i < max; i++) {
			if (array[i] == toBeReplaced)
				array[i] = replacementChar;
		}
	}
}
public static final char[][] splitOn(char divider, char[] array) {
	int length = array == null ? 0 : array.length;
	if (length == 0)
		return TypeConstants.NoCharChar;

	int wordCount = 1;
	for (int i = 0; i < length; i++)
		if (array[i] == divider)
			wordCount++;
	char[][] split = new char[wordCount][];
	int last = 0, currentWord = 0;
	for (int i = 0; i < length; i++) {
		if (array[i] == divider) {
			split[currentWord] = new char[i - last];
			System.arraycopy(array, last, split[currentWord++], 0, i - last);
			last = i + 1;
		}
	}
	split[currentWord] = new char[length - last];
	System.arraycopy(array, last, split[currentWord], 0, length - last);
	return split;
}
public static final char[][] splitOn(char divider, char[] array, int start, int end) {
	int length = array == null ? 0 : array.length;
	if (length == 0)
		return TypeConstants.NoCharChar;

	int wordCount = 1;
	for (int i = start; i < end; i++)
		if (array[i] == divider)
			wordCount++;
	char[][] split = new char[wordCount][];
	int last = start, currentWord = 0;
	for (int i = start; i < end; i++) {
		if (array[i] == divider) {
			split[currentWord] = new char[i - last];
			System.arraycopy(array, last, split[currentWord++], 0, i - last);
			last = i + 1;
		}
	}
	split[currentWord] = new char[end - last + 1];
	System.arraycopy(array, last, split[currentWord], 0, end - last + 1);
	return split;
}
public static final boolean startsWith(char[] array, char[] toBeFound) {
	int i = toBeFound.length;
	if (i > array.length)
		return false;
	while (--i >= 0)
		if (toBeFound[i] != array[i])
			return false;
	return true;
}
/*
 * copies from array[start] through array[end - 1] (does not copy array[end])
 */
public static final char[][] subarray(char[][] array, int start, int end) {
	if (end == -1) end = array.length;
	if (start > end) return null;
	if (start < 0) return null;
	if (end > array.length) return null;

	char[][] result = new char[end - start][];
/* if we do not trust System.arraycopy on our VM with char[][]'s
	for (int i = 0, s = start; s < end; i++, s++)
		result[i] = array[s];
*/
	System.arraycopy(array, start, result, 0, end - start);
	return result;
}
/*
 * copies from array[start] through array[end - 1] (does not copy array[end])
 */
public static final char[] subarray(char[] array, int start, int end) {
	if (end == -1) end = array.length;
	if (start > end) return null;
	if (start < 0) return null;
	if (end > array.length) return null;

	char[] result = new char[end - start];
	System.arraycopy(array, start, result, 0, end - start);
	return result;
}
/**
 *	Answers the result of a char[] conversion to lowercase.
 *	NOTE: if no conversion was necessary, then answers back the argument one.
 */
final static public char[] toLowerCase(char[] chars) {
	if (chars == null) return null;
	int length = chars.length;
	char[] lowerChars = null;
	for (int i = 0; i < length; i++){
		char c = chars[i];
		char lc = Character.toLowerCase(c);
		if ((c != lc) || (lowerChars != null)){
			if (lowerChars == null){
				System.arraycopy(chars, 0, lowerChars = new char[length], 0, i);
			}
			lowerChars[i] = lc;
		}
	}
	return lowerChars == null ? chars : lowerChars;
}
final static public String toString(char[][] array) {
	char[] result = concatWith(array, '.');
	if (result == null)
		return "";
	return new String(result);
}
}
