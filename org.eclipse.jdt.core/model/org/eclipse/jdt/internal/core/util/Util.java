/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.IFieldInfo;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * Provides convenient utility methods to other types in this package.
 */
public class Util {

	public interface Comparable {
		/**
		 * Returns 0 if this and c are equal, >0 if this is greater than c,
		 * or <0 if this is less than c.
		 */
		int compareTo(Comparable c);
	}

	public interface Comparer {
		/**
		 * Returns 0 if a and b are equal, >0 if a is greater than b,
		 * or <0 if a is less than b.
		 */
		int compare(Object a, Object b);
	}
	private static final String ARGUMENTS_DELIMITER = "#"; //$NON-NLS-1$

	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName = "org.eclipse.jdt.internal.core.util.messages"; //$NON-NLS-1$

	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private static final String EMPTY_ARGUMENT = "   "; //$NON-NLS-1$
	
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$

	static {
		relocalize();
	}	

	private Util() {
		// cannot be instantiated
	}
	
	/**
	 * Returns a new array adding the second array at the end of first array.
	 * It answers null if the first and second are null.
	 * If the first array is null or if it is empty, then a new array is created with second.
	 * If the second array is null, then the first array is returned.
	 * <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li><pre>
	 *    first = null
	 *    second = "a"
	 *    => result = {"a"}
	 * </pre>
	 * <li><pre>
	 *    first = {"a"}
	 *    second = null
	 *    => result = {"a"}
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    first = {"a"}
	 *    second = {"b"}
	 *    => result = {"a", "b"}
	 * </pre>
	 * </li>
	 * </ol>
	 * 
	 * @param first the first array to concatenate
	 * @param second the array to add at the end of the first array
	 * @return a new array adding the second array at the end of first array, or null if the two arrays are null.
	 */
	public static final String[] arrayConcat(String[] first, String second) {
		if (second == null)
			return first;
		if (first == null)
			return new String[] {second};

		int length = first.length;
		if (first.length == 0) {
			return new String[] {second};
		}
		
		String[] result = new String[length + 1];
		System.arraycopy(first, 0, result, 0, length);
		result[length] = second;
		return result;
	}

	/**
	 * Lookup the message with the given ID in this catalog 
	 */
	public static String bind(String id) {
		return bind(id, (String[])null);
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] {binding});
	}
	
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] {binding1, binding2});
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string values.
	 */
	public static String bind(String id, String[] bindings) {
		if (id == null)
			return "No message available"; //$NON-NLS-1$
		String message = null;
		try {
			message = bundle.getString(id);
		} catch (MissingResourceException e) {
			// If we got an exception looking for the message, fail gracefully by just returning
			// the id we were looking for.  In most cases this is semi-informative so is not too bad.
			return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-2$ //$NON-NLS-1$
		}
		// for compatibility with MessageFormat which eliminates double quotes in original message
		char[] messageWithNoDoubleQuotes =
			CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
	
		if (bindings == null) return new String(messageWithNoDoubleQuotes);
	
		int length = messageWithNoDoubleQuotes.length;
		int start = 0;
		int end = length;
		StringBuffer output = null;
		while (true) {
			if ((end = CharOperation.indexOf('{', messageWithNoDoubleQuotes, start)) > -1) {
				if (output == null) output = new StringBuffer(length+bindings.length*20);
				output.append(messageWithNoDoubleQuotes, start, end - start);
				if ((start = CharOperation.indexOf('}', messageWithNoDoubleQuotes, end + 1)) > -1) {
					int index = -1;
					String argId = new String(messageWithNoDoubleQuotes, end + 1, start - end - 1);
					try {
						index = Integer.parseInt(argId);
						output.append(bindings[index]);
					} catch (NumberFormatException nfe) { // could be nested message ID {compiler.name}
						boolean done = false;
						if (!id.equals(argId)) {
							String argMessage = null;
							try {
								argMessage = bundle.getString(argId);
								output.append(argMessage);
								done = true;
							} catch (MissingResourceException e) {
								// unable to bind argument, ignore (will leave argument in)
							}
						}
						if (!done) output.append(messageWithNoDoubleQuotes, end + 1, start - end);
					} catch (ArrayIndexOutOfBoundsException e) {
						output.append("{missing " + Integer.toString(index) + "}"); //$NON-NLS-2$ //$NON-NLS-1$
					}
					start++;
				} else {
					output.append(messageWithNoDoubleQuotes, end, length);
					break;
				}
			} else {
				if (output == null) return new String(messageWithNoDoubleQuotes);
				output.append(messageWithNoDoubleQuotes, start, length - start);
				break;
			}
		}
		return output.toString();
	}

	/**
	 * Checks the type signature in String sig, 
	 * starting at start and ending before end (end is not included).
	 * Returns the index of the character immediately after the signature if valid,
	 * or -1 if not valid.
	 */
	private static int checkTypeSignature(String sig, int start, int end, boolean allowVoid) {
		if (start >= end) return -1;
		int i = start;
		char c = sig.charAt(i++);
		int nestingDepth = 0;
		while (c == '[') {
			++nestingDepth;
			if (i >= end) return -1;
			c = sig.charAt(i++);
		}
		switch (c) {
			case 'B':
			case 'C': 
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S': 
			case 'Z':
				break;
			case 'V':
				if (!allowVoid) return -1;
				// array of void is not allowed
				if (nestingDepth != 0) return -1;
				break;
			case 'L':
				int semicolon = sig.indexOf(';', i);
				// Must have at least one character between L and ;
				if (semicolon <= i || semicolon >= end) return -1;
				i = semicolon + 1;
				break;
			default:
				return -1;
		}
		return i;
	}
	
	/**
	 * Combines two hash codes to make a new one.
	 */
	public static int combineHashCodes(int hashCode1, int hashCode2) {
		return hashCode1 * 17 + hashCode2;
	}
	
	/**
	 * Compares two byte arrays.  
	 * Returns <0 if a byte in a is less than the corresponding byte in b, or if a is shorter, or if a is null.
	 * Returns >0 if a byte in a is greater than the corresponding byte in b, or if a is longer, or if b is null.
	 * Returns 0 if they are equal or both null.
	 */
	public static int compare(byte[] a, byte[] b) {
		if (a == b)
			return 0;
		if (a == null)
			return -1;
		if (b == null)
			return 1;
		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; ++i) {
			int diff = a[i] - b[i];
			if (diff != 0)
				return diff;
		}
		if (a.length > len)
			return 1;
		if (b.length > len)
			return -1;
		return 0;
	}
	/**
	 * Compares two strings lexicographically. 
	 * The comparison is based on the Unicode value of each character in
	 * the strings. 
	 *
	 * @return  the value <code>0</code> if the str1 is equal to str2;
	 *          a value less than <code>0</code> if str1
	 *          is lexicographically less than str2; 
	 *          and a value greater than <code>0</code> if str1 is
	 *          lexicographically greater than str2.
	 */
	public static int compare(char[] str1, char[] str2) {
		int len1= str1.length;
		int len2= str2.length;
		int n= Math.min(len1, len2);
		int i= 0;
		while (n-- != 0) {
			char c1= str1[i];
			char c2= str2[i++];
			if (c1 != c2) {
				return c1 - c2;
			}
		}
		return len1 - len2;
	}

	/**
	 * Concatenate two strings with a char in between.
	 * @see #concat(String, String)
	 */
	public static String concat(String s1, char c, String s2) {
		if (s1 == null) s1 = "null"; //$NON-NLS-1$
		if (s2 == null) s2 = "null"; //$NON-NLS-1$
		int l1 = s1.length();
		int l2 = s2.length();
		char[] buf = new char[l1 + 1 + l2];
		s1.getChars(0, l1, buf, 0);
		buf[l1] = c;
		s2.getChars(0, l2, buf, l1 + 1);
		return new String(buf);
	}
	
	/**
	 * Concatenate two strings.
	 * Much faster than using +, which:
	 * 		- creates a StringBuffer,
	 * 		- which is synchronized,
	 * 		- of default size, so the resulting char array is
	 *        often larger than needed.
	 * This implementation creates an extra char array, since the
	 * String constructor copies its argument, but there's no way around this.
	 */
	public static String concat(String s1, String s2) {
		if (s1 == null) s1 = "null"; //$NON-NLS-1$
		if (s2 == null) s2 = "null"; //$NON-NLS-1$
		int l1 = s1.length();
		int l2 = s2.length();
		char[] buf = new char[l1 + l2];
		s1.getChars(0, l1, buf, 0);
		s2.getChars(0, l2, buf, l1);
		return new String(buf);
	}

	/**
	 * Returns the concatenation of the given array parts using the given separator between each part.
	 * <br>
	 * <br>
	 * For example:<br>
	 * <ol>
	 * <li><pre>
	 *    array = {"a", "b"}
	 *    separator = '.'
	 *    => result = "a.b"
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    array = {}
	 *    separator = '.'
	 *    => result = ""
	 * </pre></li>
	 * </ol>
	 * 
	 * @param array the given array
	 * @param separator the given separator
	 * @return the concatenation of the given array parts using the given separator between each part
	 */
	public static final String concatWith(String[] array, char separator) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = array.length; i < length; i++) {
			buffer.append(array[i]);
			if (i < length - 1)
				buffer.append(separator);
		}
		return buffer.toString();
	}
	
	/**
	 * Returns the concatenation of the given array parts using the given separator between each
	 * part and appending the given name at the end.
	 * <br>
	 * <br>
	 * For example:<br>
	 * <ol>
	 * <li><pre>
	 *    name = "c"
	 *    array = { "a", "b" }
	 *    separator = '.'
	 *    => result = "a.b.c"
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    name = null
	 *    array = { "a", "b" }
	 *    separator = '.'
	 *    => result = "a.b"
	 * </pre></li>
	 * <li><pre>
	 *    name = " c"
	 *    array = null
	 *    separator = '.'
	 *    => result = "c"
	 * </pre></li>
	 * </ol>
	 * 
	 * @param array the given array
	 * @param name the given name
	 * @param separator the given separator
	 * @return the concatenation of the given array parts using the given separator between each
	 * part and appending the given name at the end
	 */
	public static final String concatWith(
		String[] array,
		String name,
		char separator) {
		
		if (array == null || array.length == 0) return name;
		if (name == null || name.length() == 0) return concatWith(array, separator);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = array.length; i < length; i++) {
			buffer.append(array[i]);
			buffer.append(separator);
		}
		buffer.append(name);
		return buffer.toString();
		
	}
	
	/**
	 * Concatenate three strings.
	 * @see #concat(String, String)
	 */
	public static String concat(String s1, String s2, String s3) {
		if (s1 == null) s1 = "null"; //$NON-NLS-1$
		if (s2 == null) s2 = "null"; //$NON-NLS-1$
		if (s3 == null) s3 = "null"; //$NON-NLS-1$
		int l1 = s1.length();
		int l2 = s2.length();
		int l3 = s3.length();
		char[] buf = new char[l1 + l2 + l3];
		s1.getChars(0, l1, buf, 0);
		s2.getChars(0, l2, buf, l1);
		s3.getChars(0, l3, buf, l1 + l2);
		return new String(buf);
	}
		
	/**
	 * Converts a type signature from the IBinaryType representation to the DC representation.
	 */
	public static String convertTypeSignature(char[] sig) {
		return new String(sig).replace('/', '.');
	}

	/**
	 * Apply the given edit on the given string and return the updated string.
	 * Return the given string if anything wrong happen while applying the edit.
	 * 
	 * @param original the given string
	 * @param edit the given edit
	 * 
	 * @return the updated string
	 */
	public final static String editedString(String original, TextEdit edit) {
		if (edit == null) {
			return original;
		}
		SimpleDocument document = new SimpleDocument(original);
		try {
			edit.apply(document, TextEdit.NONE);
			return document.get();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return original;
	}

	/**
	 * Returns true iff str.toLowerCase().endsWith(end.toLowerCase())
	 * implementation is not creating extra strings.
	 */
	public final static boolean endsWithIgnoreCase(String str, String end) {
		
		int strLength = str == null ? 0 : str.length();
		int endLength = end == null ? 0 : end.length();
		
		// return false if the string is smaller than the end.
		if(endLength > strLength)
			return false;
			
		// return false if any character of the end are
		// not the same in lower case.
		for(int i = 1 ; i <= endLength; i++){
			if(Character.toLowerCase(end.charAt(endLength - i)) != Character.toLowerCase(str.charAt(strLength - i)))
				return false;
		}
		
		return true;
	}

	/**
	 * Compares two arrays using equals() on the elements.
	 * Neither can be null. Only the first len elements are compared.
	 * Return false if either array is shorter than len.
	 */
	public static boolean equalArrays(Object[] a, Object[] b, int len) {
		if (a == b)	return true;
		if (a.length < len || b.length < len) return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] == null) {
				if (b[i] != null) return false;
			} else {
				if (!a[i].equals(b[i])) return false;
			}
		}
		return true;
	}

	/**
	 * Compares two arrays using equals() on the elements.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * all elements are equal.
	 */
	public static boolean equalArraysOrNull(int[] a, int[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len != b.length)
			return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	/**
	 * Compares two arrays using equals() on the elements.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * all elements compare true with equals.
	 */
	public static boolean equalArraysOrNull(Object[] a, Object[] b) {
		if (a == b)	return true;
		if (a == null || b == null) return false;

		int len = a.length;
		if (len != b.length) return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] == null) {
				if (b[i] != null) return false;
			} else {
				if (!a[i].equals(b[i])) return false;
			}
		}
		return true;
	}
	
	/**
	 * Compares two arrays using equals() on the elements.
	 * The arrays are first sorted.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * iff, after sorting both arrays, all elements compare true with equals.
	 * The original arrays are left untouched.
	 */
	public static boolean equalArraysOrNullSortFirst(Comparable[] a, Comparable[] b) {
		if (a == b)	return true;
		if (a == null || b == null) return false;
		int len = a.length;
		if (len != b.length) return false;
		if (len >= 2) {  // only need to sort if more than two items
			a = sortCopy(a);
			b = sortCopy(b);
		}
		for (int i = 0; i < len; ++i) {
			if (!a[i].equals(b[i])) return false;
		}
		return true;
	}
	
	/**
	 * Compares two String arrays using equals() on the elements.
	 * The arrays are first sorted.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * iff, after sorting both arrays, all elements compare true with equals.
	 * The original arrays are left untouched.
	 */
	public static boolean equalArraysOrNullSortFirst(String[] a, String[] b) {
		if (a == b)	return true;
		if (a == null || b == null) return false;
		int len = a.length;
		if (len != b.length) return false;
		if (len >= 2) {  // only need to sort if more than two items
			a = sortCopy(a);
			b = sortCopy(b);
		}
		for (int i = 0; i < len; ++i) {
			if (!a[i].equals(b[i])) return false;
		}
		return true;
	}
	
	/**
	 * Compares two objects using equals().
	 * Either or both array may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * Otherwise, return the result of comparing with equals().
	 */
	public static boolean equalOrNull(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}
	
	/**
	 * Given a qualified name, extract the last component.
	 * If the input is not qualified, the same string is answered.
	 */
	public static String extractLastName(String qualifiedName) {
		int i = qualifiedName.lastIndexOf('.');
		if (i == -1) return qualifiedName;
		return qualifiedName.substring(i+1);
	}
	
	/**
	 * Extracts the parameter types from a method signature.
	 */
	public static String[] extractParameterTypes(char[] sig) {
		int count = getParameterCount(sig);
		String[] result = new String[count];
		if (count == 0)
			return result;
		int i = CharOperation.indexOf('(', sig) + 1;
		count = 0;
		int len = sig.length;
		int start = i;
		for (;;) {
			if (i == len)
				break;
			char c = sig[i];
			if (c == ')')
				break;
			if (c == '[') {
				++i;
			} else
				if (c == 'L') {
					i = CharOperation.indexOf(';', sig, i + 1) + 1;
					Assert.isTrue(i != 0);
					result[count++] = convertTypeSignature(CharOperation.subarray(sig, start, i));
					start = i;
				} else {
					++i;
					result[count++] = convertTypeSignature(CharOperation.subarray(sig, start, i));
					start = i;
				}
		}
		return result;
	}

	/**
	 * Extracts the return type from a method signature.
	 */
	public static String extractReturnType(String sig) {
		int i = sig.lastIndexOf(')');
		Assert.isTrue(i != -1);
		return sig.substring(i+1);	
	}
	private static IFile findFirstClassFile(IFolder folder) {
		try {
			IResource[] members = folder.members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource member = members[i];
				if (member.getType() == IResource.FOLDER) {
					return findFirstClassFile((IFolder)member);
				} else if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(member.getName())) {
					return (IFile) member;
				}
			}
		} catch (CoreException e) {
			// ignore
		}
		return null;
	}
	
	/**
	 * Finds the first line separator used by the given text.
	 *
	 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>,
	 *			or <code>null</code> if none found
	 */
	public static String findLineSeparator(char[] text) {
		// find the first line separator
		int length = text.length;
		if (length > 0) {
			char nextChar = text[0];
			for (int i = 0; i < length; i++) {
				char currentChar = nextChar;
				nextChar = i < length-1 ? text[i+1] : ' ';
				switch (currentChar) {
					case '\n': return "\n"; //$NON-NLS-1$
					case '\r': return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		// not found
		return null;
	}
	
	public static IClassFileAttribute getAttribute(IClassFileReader classFileReader, char[] attributeName) {
		IClassFileAttribute[] attributes = classFileReader.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), attributeName)) {
				return attributes[i];
			}
		}
		return null;
	}
	
	public static IClassFileAttribute getAttribute(ICodeAttribute codeAttribute, char[] attributeName) {
		IClassFileAttribute[] attributes = codeAttribute.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), attributeName)) {
				return attributes[i];
			}
		}
		return null;
	}	
	
	public static IClassFileAttribute getAttribute(IFieldInfo fieldInfo, char[] attributeName) {
		IClassFileAttribute[] attributes = fieldInfo.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), attributeName)) {
				return attributes[i];
			}
		}
		return null;
	}

	public static IClassFileAttribute getAttribute(IMethodInfo methodInfo, char[] attributeName) {
		IClassFileAttribute[] attributes = methodInfo.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), attributeName)) {
				return attributes[i];
			}
		}
		return null;
	}
	/**
	 * Get the jdk level of this root.
	 * The value can be:
	 * <ul>
	 * <li>major<<16 + minor : see predefined constants on ClassFileConstants </li>
	 * <li><code>0</null> if the root is a source package fragment root or if a Java model exception occured</li>
	 * </ul>
	 * Returns the jdk level
	 */
	public static long getJdkLevel(Object targetLibrary) {
		try {
				ClassFileReader reader = null;
				if (targetLibrary instanceof IFolder) {
					IFile classFile = findFirstClassFile((IFolder) targetLibrary); // only internal classfolders are allowed
					if (classFile != null) {
						byte[] bytes = Util.getResourceContentsAsByteArray(classFile);
						IPath location = classFile.getLocation();
						reader = new ClassFileReader(bytes, location == null ? null : location.toString().toCharArray());
					}
				} else {
					// root is a jar file or a zip file
					ZipFile jar = null;
					try {
						IPath path = null;
						if (targetLibrary instanceof IResource) {
							path = ((IResource)targetLibrary).getLocation();
						} else if (targetLibrary instanceof File){
							File f = (File) targetLibrary;
							if (!f.isDirectory()) {
								path = new Path(((File)targetLibrary).getPath());
							}
						}
						if (path != null) {
							jar = JavaModelManager.getJavaModelManager().getZipFile(path);
							for (Enumeration e= jar.entries(); e.hasMoreElements();) {
								ZipEntry member= (ZipEntry) e.nextElement();
								String entryName= member.getName();
								if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(entryName)) {
									reader = ClassFileReader.read(jar, entryName);
									break;
								}
							}
						}
					} catch (CoreException e) {
						// ignore
					} finally {
						JavaModelManager.getJavaModelManager().closeZipFile(jar);
					}
				}
				if (reader != null) {
					return reader.getVersion();
				}
		} catch(JavaModelException e) {
			// ignore
		} catch(ClassFormatException e) {
			// ignore
		} catch(IOException e) {
			// ignore
		}
		return 0;
	}
	
	/**
	 * Returns the line separator used by the given buffer.
	 * Uses the given text if none found.
	 *
	 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>
	 */
	private static String getLineSeparator(char[] text, char[] buffer) {
		// search in this buffer's contents first
		String lineSeparator = findLineSeparator(buffer);
		if (lineSeparator == null) {
			// search in the given text
			lineSeparator = findLineSeparator(text);
			if (lineSeparator == null) {
				// default to system line separator
				return org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR;
			}
		}
		return lineSeparator;
	}
		
	/**
	 * Returns the number of parameter types in a method signature.
	 */
	public static int getParameterCount(char[] sig) {
		int i = CharOperation.indexOf('(', sig) + 1;
		Assert.isTrue(i != 0);
		int count = 0;
		int len = sig.length;
		for (;;) {
			if (i == len)
				break;
			char c = sig[i];
			if (c == ')')
				break;
			if (c == '[') {
				++i;
			} else
				if (c == 'L') {
					++count;
					i = CharOperation.indexOf(';', sig, i + 1) + 1;
					Assert.isTrue(i != 0);
				} else {
					++count;
					++i;
				}
		}
		return count;
	}
	
	/**
	 * Put all the arguments in one String.
	 */
	public static String getProblemArgumentsForMarker(String[] arguments){
		StringBuffer args = new StringBuffer(10);
		
		args.append(arguments.length);
		args.append(':');
		
			
		for (int j = 0; j < arguments.length; j++) {
			if(j != 0)
				args.append(ARGUMENTS_DELIMITER);
			
			if(arguments[j].length() == 0) {
				args.append(EMPTY_ARGUMENT);
			} else {			
				args.append(arguments[j]);
			}
		}
		
		return args.toString();
	}
	
	/**
	 * Separate all the arguments of a String made by getProblemArgumentsForMarker
	 */
	public static String[] getProblemArgumentsFromMarker(String argumentsString){
		if (argumentsString == null) return null;
		int index = argumentsString.indexOf(':');
		if(index == -1)
			return null;
		
		int length = argumentsString.length();
		int numberOfArg;
		try{
			numberOfArg = Integer.parseInt(argumentsString.substring(0 , index));
		} catch (NumberFormatException e) {
			return null;
		}
		argumentsString = argumentsString.substring(index + 1, length);
		
		String[] args = new String[length];
		int count = 0;
		
		StringTokenizer tokenizer = new StringTokenizer(argumentsString, ARGUMENTS_DELIMITER);
		while(tokenizer.hasMoreTokens()) {
			String argument = tokenizer.nextToken();
			if(argument.equals(EMPTY_ARGUMENT))
				argument = "";  //$NON-NLS-1$
			args[count++] = argument;
		}
		
		if(count != numberOfArg)
			return null;
		
		System.arraycopy(args, 0, args = new String[count], 0, count);
		return args;
	}
	
	/**
	 * Returns the given file's contents as a byte array.
	 */
	public static byte[] getResourceContentsAsByteArray(IFile file) throws JavaModelException {
		InputStream stream= null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
		try {
			return org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsByteArray(stream, -1);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	/**
	 * Returns the given file's contents as a character array.
	 */
	public static char[] getResourceContentsAsCharArray(IFile file) throws JavaModelException {
		// Get encoding from file
		String encoding = null;
		try {
			encoding = file.getCharset();
		}
		catch(CoreException ce) {
			// do not use any encoding
		}
		return getResourceContentsAsCharArray(file, encoding);
	}

	public static char[] getResourceContentsAsCharArray(IFile file, String encoding) throws JavaModelException {
		// Get resource contents
		InputStream stream= null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST);
		}
		try {
			return org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsCharArray(stream, -1, encoding);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	/**
	 * Returns a trimmed version the simples names returned by Signature.
	 */
	public static String[] getTrimmedSimpleNames(String name) {
		String[] result = Signature.getSimpleNames(name);
		for (int i = 0, length = result.length; i < length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}
	
		/*
	 * Returns the index of the most specific argument paths which is strictly enclosing the path to check
	 */
	public static int indexOfEnclosingPath(IPath checkedPath, IPath[] paths, int pathCount) {

	    int bestMatch = -1, bestLength = -1;
		for (int i = 0; i < pathCount; i++){
			if (paths[i].equals(checkedPath)) continue;
			if (paths[i].isPrefixOf(checkedPath)) {
			    int currentLength = paths[i].segmentCount();
			    if (currentLength > bestLength) {
			        bestLength = currentLength;
			        bestMatch = i;
			    }
			}
		}
		return bestMatch;
	}
	
	/*
	 * Returns the index of the first argument paths which is equal to the path to check
	 */
	public static int indexOfMatchingPath(IPath checkedPath, IPath[] paths, int pathCount) {

		for (int i = 0; i < pathCount; i++){
			if (paths[i].equals(checkedPath)) return i;
		}
		return -1;
	}

	/*
	 * Returns the index of the first argument paths which is strictly nested inside the path to check
	 */
	public static int indexOfNestedPath(IPath checkedPath, IPath[] paths, int pathCount) {

		for (int i = 0; i < pathCount; i++){
			if (checkedPath.equals(paths[i])) continue;
			if (checkedPath.isPrefixOf(paths[i])) return i;
		}
		return -1;
	}

	/*
	 * Returns whether the given java element is exluded from its root's classpath.
	 * It doesn't check whether the root itself is on the classpath or not
	 */
	public static final boolean isExcluded(IJavaElement element) {
		int elementType = element.getElementType();
		switch (elementType) {
			case IJavaElement.JAVA_MODEL:
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				return false;

			case IJavaElement.PACKAGE_FRAGMENT:
				PackageFragmentRoot root = (PackageFragmentRoot)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				IResource resource = element.getResource();
				return resource != null && isExcluded(resource, root.fullInclusionPatternChars(), root.fullExclusionPatternChars());
				
			case IJavaElement.COMPILATION_UNIT:
				root = (PackageFragmentRoot)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				resource = element.getResource();
				if (resource != null && isExcluded(resource, root.fullInclusionPatternChars(), root.fullExclusionPatternChars()))
					return true;
				return isExcluded(element.getParent());
				
			default:
				IJavaElement cu = element.getAncestor(IJavaElement.COMPILATION_UNIT);
				return cu != null && isExcluded(cu);
		}
	}
	/*
	 * Returns whether the given resource path matches one of the inclusion/exclusion
	 * patterns.
	 * NOTE: should not be asked directly using pkg root pathes
	 * @see IClasspathEntry#getInclusionPatterns
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IPath resourcePath, char[][] inclusionPatterns, char[][] exclusionPatterns, boolean isFolderPath) {
		if (inclusionPatterns == null && exclusionPatterns == null) return false;
		char[] path = resourcePath.toString().toCharArray();

		inclusionCheck: if (inclusionPatterns != null) {
			for (int i = 0, length = inclusionPatterns.length; i < length; i++) {
				char[] pattern = inclusionPatterns[i];
				char[] folderPattern = pattern;
				if (isFolderPath) {
					int lastSlash = CharOperation.lastIndexOf('/', pattern);
					if (lastSlash != -1 && lastSlash != pattern.length-1){ // trailing slash -> adds '**' for free (see http://ant.apache.org/manual/dirtasks.html)
						int star = CharOperation.indexOf('*', pattern, lastSlash);
						if ((star == -1
								|| star >= pattern.length-1 
								|| pattern[star+1] != '*')) {
							folderPattern = CharOperation.subarray(pattern, 0, lastSlash);
						}
					}
				}
				if (CharOperation.pathMatch(folderPattern, path, true, '/')) {
					break inclusionCheck;
				}
			}
			return true; // never included
		}
		if (isFolderPath) {
			path = CharOperation.concat(path, new char[] {'*'}, '/');
		}
		exclusionCheck: if (exclusionPatterns != null) {
			for (int i = 0, length = exclusionPatterns.length; i < length; i++) {
				if (CharOperation.pathMatch(exclusionPatterns[i], path, true, '/')) {
					return true;
				}
			}
		}
		return false;
	}	
	
	/*
	 * Returns whether the given resource matches one of the exclusion patterns.
	 * NOTE: should not be asked directly using pkg root pathes
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IResource resource, char[][] inclusionPatterns, char[][] exclusionPatterns) {
		IPath path = resource.getFullPath();
		// ensure that folders are only excluded if all of their children are excluded
		return isExcluded(path, inclusionPatterns, exclusionPatterns, resource.getType() == IResource.FOLDER);
	}

	/**
	 * Validate the given .class file name.
	 * A .class file name must obey the following rules:
	 * <ul>
	 * <li> it must not be null
	 * <li> it must include the <code>".class"</code> suffix
	 * <li> its prefix must be a valid identifier
	 * </ul>
	 * </p>
	 * @param name the name of a .class file
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a .class file name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static boolean isValidClassFileName(String name) {
		return JavaConventions.validateClassFileName(name).getSeverity() != IStatus.ERROR;
	}

	/**
	 * Validate the given compilation unit name.
	 * A compilation unit name must obey the following rules:
	 * <ul>
	 * <li> it must not be null
	 * <li> it must include the <code>".java"</code> suffix
	 * <li> its prefix must be a valid identifier
	 * </ul>
	 * </p>
	 * @param name the name of a compilation unit
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a compilation unit name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static boolean isValidCompilationUnitName(String name) {
		return JavaConventions.validateCompilationUnitName(name).getSeverity() != IStatus.ERROR;
	}
	
	/**
	 * Returns true if the given folder name is valid for a package,
	 * false if it is not.
	 */
	public static boolean isValidFolderNameForPackage(String folderName) {
		return JavaConventions.validateIdentifier(folderName).getSeverity() != IStatus.ERROR;
	}	

	/**
	 * Returns true if the given method signature is valid,
	 * false if it is not.
	 */
	public static boolean isValidMethodSignature(String sig) {
		int len = sig.length();
		if (len == 0) return false;
		int i = 0;
		char c = sig.charAt(i++);
		if (c != '(') return false;
		if (i >= len) return false;
		while (sig.charAt(i) != ')') {
			// Void is not allowed as a parameter type.
			i = checkTypeSignature(sig, i, len, false);
			if (i == -1) return false;
			if (i >= len) return false;
		}
		++i;
		i = checkTypeSignature(sig, i, len, true);
		return i == len;
	}
	
	/**
	 * Returns true if the given type signature is valid,
	 * false if it is not.
	 */
	public static boolean isValidTypeSignature(String sig, boolean allowVoid) {
		int len = sig.length();
		return checkTypeSignature(sig, 0, len, allowVoid) == len;
	}

	/*
	 * Add a log entry
	 */
	public static void log(Throwable e, String message) {
		Throwable nestedException;
		if (e instanceof JavaModelException 
				&& (nestedException = ((JavaModelException)e).getException()) != null) {
			e = nestedException;
		}
		IStatus status= new Status(
			IStatus.ERROR, 
			JavaCore.PLUGIN_ID, 
			IStatus.ERROR, 
			message, 
			e); 
		JavaCore.getPlugin().getLog().log(status);
	}	
	
	/**
	 * Normalizes the cariage returns in the given text.
	 * They are all changed  to use the given buffer's line separator.
	 */
	public static char[] normalizeCRs(char[] text, char[] buffer) {
		CharArrayBuffer result = new CharArrayBuffer();
		int lineStart = 0;
		int length = text.length;
		if (length == 0) return text;
		String lineSeparator = getLineSeparator(text, buffer);
		char nextChar = text[0];
		for (int i = 0; i < length; i++) {
			char currentChar = nextChar;
			nextChar = i < length-1 ? text[i+1] : ' ';
			switch (currentChar) {
				case '\n':
					int lineLength = i-lineStart;
					char[] line = new char[lineLength];
					System.arraycopy(text, lineStart, line, 0, lineLength);
					result.append(line);
					result.append(lineSeparator);
					lineStart = i+1;
					break;
				case '\r':
					lineLength = i-lineStart;
					if (lineLength >= 0) {
						line = new char[lineLength];
						System.arraycopy(text, lineStart, line, 0, lineLength);
						result.append(line);
						result.append(lineSeparator);
						if (nextChar == '\n') {
							nextChar = ' ';
							lineStart = i+2;
						} else {
							// when line separator are mixed in the same file
							// \r might not be followed by a \n. If not, we should increment
							// lineStart by one and not by two.
							lineStart = i+1;
						}
					} else {
						// when line separator are mixed in the same file
						// we need to prevent NegativeArraySizeException
						lineStart = i+1;
					}
					break;
			}
		}
		char[] lastLine;
		if (lineStart > 0) {
			int lastLineLength = length-lineStart;
			if (lastLineLength > 0) {
				lastLine = new char[lastLineLength];
				System.arraycopy(text, lineStart, lastLine, 0, lastLineLength);
				result.append(lastLine);
			}
			return result.getContents();
		}
		return text;
	}

	/**
	 * Normalizes the cariage returns in the given text.
	 * They are all changed  to use given buffer's line sepatator.
	 */
	public static String normalizeCRs(String text, String buffer) {
		return new String(normalizeCRs(text.toCharArray(), buffer.toCharArray()));
	}

	/**
	 * Converts the given relative path into a package name.
	 * Returns null if the path is not a valid package name.
	 */
	public static String packageName(IPath pkgPath) {
		StringBuffer pkgName = new StringBuffer(IPackageFragment.DEFAULT_PACKAGE_NAME);
		for (int j = 0, max = pkgPath.segmentCount(); j < max; j++) {
			String segment = pkgPath.segment(j);
			if (!isValidFolderNameForPackage(segment)) {
				return null;
			}
			pkgName.append(segment);
			if (j < pkgPath.segmentCount() - 1) {
				pkgName.append("." ); //$NON-NLS-1$
			}
		}
		return pkgName.toString();
	}

	/**
	 * Returns the length of the common prefix between s1 and s2.
	 */
	public static int prefixLength(char[] s1, char[] s2) {
		int len= 0;
		int max= Math.min(s1.length, s2.length);
		for (int i= 0; i < max && s1[i] == s2[i]; ++i)
			++len;
		return len;
	}
	/**
	 * Returns the length of the common prefix between s1 and s2.
	 */
	public static int prefixLength(String s1, String s2) {
		int len= 0;
		int max= Math.min(s1.length(), s2.length());
		for (int i= 0; i < max && s1.charAt(i) == s2.charAt(i); ++i)
			++len;
		return len;
	}
	private static void quickSort(char[][] list, int left, int right) {
		int original_left= left;
		int original_right= right;
		char[] mid= list[(left + right) / 2];
		do {
			while (compare(list[left], mid) < 0) {
				left++;
			}
			while (compare(mid, list[right]) < 0) {
				right--;
			}
			if (left <= right) {
				char[] tmp= list[left];
				list[left]= list[right];
				list[right]= tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(list, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, left, original_right);
		}
	}

	/**
	 * Sort the comparable objects in the given collection.
	 */
	private static void quickSort(Comparable[] sortedCollection, int left, int right) {
		int original_left = left;
		int original_right = right;
		Comparable mid = sortedCollection[ (left + right) / 2];
		do {
			while (sortedCollection[left].compareTo(mid) < 0) {
				left++;
			}
			while (mid.compareTo(sortedCollection[right]) < 0) {
				right--;
			}
			if (left <= right) {
				Comparable tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(sortedCollection, original_left, right);
		}
		if (left < original_right) {
			quickSort(sortedCollection, left, original_right);
		}
	}
	private static void quickSort(int[] list, int left, int right) {
		int original_left= left;
		int original_right= right;
		int mid= list[(left + right) / 2];
		do {
			while (list[left] < mid) {
				left++;
			}
			while (mid < list[right]) {
				right--;
			}
			if (left <= right) {
				int tmp= list[left];
				list[left]= list[right];
				list[right]= tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(list, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, left, original_right);
		}
	}

	/**
	 * Sort the objects in the given collection using the given comparer.
	 */
	private static void quickSort(Object[] sortedCollection, int left, int right, Comparer comparer) {
		int original_left = left;
		int original_right = right;
		Object mid = sortedCollection[ (left + right) / 2];
		do {
			while (comparer.compare(sortedCollection[left], mid) < 0) {
				left++;
			}
			while (comparer.compare(mid, sortedCollection[right]) < 0) {
				right--;
			}
			if (left <= right) {
				Object tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(sortedCollection, original_left, right, comparer);
		}
		if (left < original_right) {
			quickSort(sortedCollection, left, original_right, comparer);
		}
	}

	/**
	 * Sort the objects in the given collection using the given sort order.
	 */
	private static void quickSort(Object[] sortedCollection, int left, int right, int[] sortOrder) {
		int original_left = left;
		int original_right = right;
		int mid = sortOrder[ (left + right) / 2];
		do {
			while (sortOrder[left] < mid) {
				left++;
			}
			while (mid < sortOrder[right]) {
				right--;
			}
			if (left <= right) {
				Object tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				int tmp2 = sortOrder[left];
				sortOrder[left] = sortOrder[right];
				sortOrder[right] = tmp2;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(sortedCollection, original_left, right, sortOrder);
		}
		if (left < original_right) {
			quickSort(sortedCollection, left, original_right, sortOrder);
		}
	}

	/**
	 * Sort the strings in the given collection.
	 */
	private static void quickSort(String[] sortedCollection, int left, int right) {
		int original_left = left;
		int original_right = right;
		String mid = sortedCollection[ (left + right) / 2];
		do {
			while (sortedCollection[left].compareTo(mid) < 0) {
				left++;
			}
			while (mid.compareTo(sortedCollection[right]) < 0) {
				right--;
			}
			if (left <= right) {
				String tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(sortedCollection, original_left, right);
		}
		if (left < original_right) {
			quickSort(sortedCollection, left, original_right);
		}
	}

	/**
	 * Sort the strings in the given collection in reverse alphabetical order.
	 */
	private static void quickSortReverse(String[] sortedCollection, int left, int right) {
		int original_left = left;
		int original_right = right;
		String mid = sortedCollection[ (left + right) / 2];
		do {
			while (sortedCollection[left].compareTo(mid) > 0) {
				left++;
			}
			while (mid.compareTo(sortedCollection[right]) > 0) {
				right--;
			}
			if (left <= right) {
				String tmp = sortedCollection[left];
				sortedCollection[left] = sortedCollection[right];
				sortedCollection[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSortReverse(sortedCollection, original_left, right);
		}
		if (left < original_right) {
			quickSortReverse(sortedCollection, left, original_right);
		}
	}
	/**
	 * Reads in a string from the specified data input stream. The 
	 * string has been encoded using a modified UTF-8 format. 
	 * <p>
	 * The first two bytes are read as if by 
	 * <code>readUnsignedShort</code>. This value gives the number of 
	 * following bytes that are in the encoded string, not
	 * the length of the resulting string. The following bytes are then 
	 * interpreted as bytes encoding characters in the UTF-8 format 
	 * and are converted into characters. 
	 * <p>
	 * This method blocks until all the bytes are read, the end of the 
	 * stream is detected, or an exception is thrown. 
	 *
	 * @param      in   a data input stream.
	 * @return     a Unicode string.
	 * @exception  EOFException            if the input stream reaches the end
	 *               before all the bytes.
	 * @exception  IOException             if an I/O error occurs.
	 * @exception  UTFDataFormatException  if the bytes do not represent a
	 *               valid UTF-8 encoding of a Unicode string.
	 * @see        java.io.DataInputStream#readUnsignedShort()
	 */
	public final static char[] readUTF(DataInput in) throws IOException {
		int utflen= in.readUnsignedShort();
		char str[]= new char[utflen];
		int count= 0;
		int strlen= 0;
		while (count < utflen) {
			int c= in.readUnsignedByte();
			int char2, char3;
			switch (c >> 4) {
				case 0 :
				case 1 :
				case 2 :
				case 3 :
				case 4 :
				case 5 :
				case 6 :
				case 7 :
					// 0xxxxxxx
					count++;
					str[strlen++]= (char) c;
					break;
				case 12 :
				case 13 :
					// 110x xxxx   10xx xxxx
					count += 2;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2= in.readUnsignedByte();
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException();
					str[strlen++]= (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14 :
					// 1110 xxxx  10xx xxxx  10xx xxxx
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2= in.readUnsignedByte();
					char3= in.readUnsignedByte();
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new UTFDataFormatException();
					str[strlen++]= (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default :
					// 10xx xxxx,  1111 xxxx
					throw new UTFDataFormatException();
			}
		}
		if (strlen < utflen) {
			System.arraycopy(str, 0, str= new char[strlen], 0, strlen);
		}
		return str;
	}

	/**
	 * Creates a NLS catalog for the given locale.
	 */
	public static void relocalize() {
		try {
			bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
		} catch(MissingResourceException e) {
			System.out.println("Missing resource : " + bundleName.replace('.', '/') + ".properties for locale " + Locale.getDefault()); //$NON-NLS-1$//$NON-NLS-2$
			throw e;
		}
	}
	/**
	 * Return a new array which is the split of the given string using the given divider. The given end 
	 * is exclusive and the given start is inclusive.
	 * <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li><pre>
	 *    divider = 'b'
	 *    string = "abbaba"
	 *    start = 2
	 *    end = 5
	 *    result => { "", "a", "" }
	 * </pre>
	 * </li>
	 * </ol>
	 * 
	 * @param divider the given divider
	 * @param string the given string
	 * @param start the given starting index
	 * @param end the given ending index
	 * @return a new array which is the split of the given string using the given divider
	 * @throws ArrayIndexOutOfBoundsException if start is lower than 0 or end is greater than the array length
	 */
	public static final String[] splitOn(
		char divider,
		String string,
		int start,
		int end) {
		int length = string == null ? 0 : string.length();
		if (length == 0 || start > end)
			return CharOperation.NO_STRINGS;

		int wordCount = 1;
		for (int i = start; i < end; i++)
			if (string.charAt(i) == divider)
				wordCount++;
		String[] split = new String[wordCount];
		int last = start, currentWord = 0;
		for (int i = start; i < end; i++) {
			if (string.charAt(i) == divider) {
				split[currentWord++] = string.substring(last, i);
				last = i + 1;
			}
		}
		split[currentWord] = string.substring(last, end);
		return split;
	}
	public static void sort(char[][] list) {
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	/**
	 * Sorts an array of Comparable objects in place.
	 */
	public static void sort(Comparable[] objects) {
		if (objects.length > 1)
			quickSort(objects, 0, objects.length - 1);
	}
	public static void sort(int[] list) {
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	/**
	 * Sorts an array of objects in place.
	 * The given comparer compares pairs of items.
	 */
	public static void sort(Object[] objects, Comparer comparer) {
		if (objects.length > 1)
			quickSort(objects, 0, objects.length - 1, comparer);
	}

	/**
	 * Sorts an array of objects in place, using the sort order given for each item.
	 */
	public static void sort(Object[] objects, int[] sortOrder) {
		if (objects.length > 1)
			quickSort(objects, 0, objects.length - 1, sortOrder);
	}

	/**
	 * Sorts an array of strings in place using quicksort.
	 */
	public static void sort(String[] strings) {
		if (strings.length > 1)
			quickSort(strings, 0, strings.length - 1);
	}

	/**
	 * Sorts an array of Comparable objects, returning a new array
	 * with the sorted items.  The original array is left untouched.
	 */
	public static Comparable[] sortCopy(Comparable[] objects) {
		int len = objects.length;
		Comparable[] copy = new Comparable[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy);
		return copy;
	}

	/**
	 * Sorts an array of Strings, returning a new array
	 * with the sorted items.  The original array is left untouched.
	 */
	public static Object[] sortCopy(Object[] objects, Comparer comparer) {
		int len = objects.length;
		Object[] copy = new Object[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy, comparer);
		return copy;
	}

	/**
	 * Sorts an array of Strings, returning a new array
	 * with the sorted items.  The original array is left untouched.
	 */
	public static String[] sortCopy(String[] objects) {
		int len = objects.length;
		String[] copy = new String[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy);
		return copy;
	}

	/**
	 * Sorts an array of strings in place using quicksort
	 * in reverse alphabetical order.
	 */
	public static void sortReverseOrder(String[] strings) {
		if (strings.length > 1)
			quickSortReverse(strings, 0, strings.length - 1);
	}

	/**
	 * Converts a String[] to char[][].
	 */
	public static char[][] toCharArrays(String[] a) {
		int len = a.length;
		char[][] result = new char[len][];
		for (int i = 0; i < len; ++i) {
			result[i] = a[i].toCharArray();
		}
		return result;
	}

	/**
	 * Converts a String to char[][], where segments are separate by '.'.
	 */
	public static char[][] toCompoundChars(String s) {
		int len = s.length();
		if (len == 0) {
			return CharOperation.NO_CHAR_CHAR;
		}
		int segCount = 1;
		for (int off = s.indexOf('.'); off != -1; off = s.indexOf('.', off + 1)) {
			++segCount;
		}
		char[][] segs = new char[segCount][];
		int start = 0;
		for (int i = 0; i < segCount; ++i) {
			int dot = s.indexOf('.', start);
			int end = (dot == -1 ? s.length() : dot);
			segs[i] = new char[end - start];
			s.getChars(start, end, segs[i], 0);
			start = end + 1;
		}
		return segs;
	}
	
	/**
	 * Converts a char[] to String.
	 */
	public static String toString(char[] c) {
		return new String(c);
	}

	/**
	 * Converts a char[][] to String, where segments are separated by '.'.
	 */
	public static String toString(char[][] c) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, max = c.length; i < max; ++i) {
			if (i != 0) sb.append('.');
			sb.append(c[i]);
		}
		return sb.toString();
	}

	/**
	 * Converts a char[][] and a char[] to String, where segments are separated by '.'.
	 */
	public static String toString(char[][] c, char[] d) {
		if (c == null) return new String(d);
		StringBuffer sb = new StringBuffer();
		for (int i = 0, max = c.length; i < max; ++i) {
			sb.append(c[i]);
			sb.append('.');
		}
		sb.append(d);
		return sb.toString();
	}
	
	/*
	 * Returns the unresolved type parameter signatures of the given method
	 * e.g. {"QString;", "[int", "[[Qjava.util.Vector;"}
	 */
	public static String[] typeParameterSignatures(AbstractMethodDeclaration method) {
		Argument[] args = method.arguments;
		if (args != null) {
			int length = args.length;
			String[] signatures = new String[length];
			for (int i = 0; i < args.length; i++) {
				Argument arg = args[i];
				signatures[i] = typeSignature(arg.type);
			}
			return signatures;
		}
		return new String[0];
	}

	/*
	 * Returns the unresolved type signature of the given type reference, 
	 * e.g. "QString;", "[int", "[[Qjava.util.Vector;"
	 */
	public static String typeSignature(TypeReference type) {
		char[][] compoundName = type.getParameterizedTypeName();
		char[] typeName =CharOperation.concatWith(compoundName, '.');
		String signature = Signature.createTypeSignature(typeName, false/*don't resolve*/);
		return signature;
	}

	/**
	 * Asserts that the given method signature is valid.
	 */
	public static void validateMethodSignature(String sig) {
		Assert.isTrue(isValidMethodSignature(sig));
	}

	/**
	 * Asserts that the given type signature is valid.
	 */
	public static void validateTypeSignature(String sig, boolean allowVoid) {
		Assert.isTrue(isValidTypeSignature(sig, allowVoid));
	}
	public static void verbose(String log) {
		verbose(log, System.out);
	}
	public static synchronized void verbose(String log, PrintStream printStream) {
		int start = 0;
		do {
			int end = log.indexOf('\n', start);
			printStream.print(Thread.currentThread());
			printStream.print(" "); //$NON-NLS-1$
			printStream.print(log.substring(start, end == -1 ? log.length() : end+1));
			start = end+1;
		} while (start != 0);
		printStream.println();
	}
	/**
	 * Writes a string to the given output stream using UTF-8 
	 * encoding in a machine-independent manner. 
	 * <p>
	 * First, two bytes are written to the output stream as if by the 
	 * <code>writeShort</code> method giving the number of bytes to 
	 * follow. This value is the number of bytes actually written out, 
	 * not the length of the string. Following the length, each character 
	 * of the string is output, in sequence, using the UTF-8 encoding 
	 * for the character. 
	 *
	 * @param      str   a string to be written.
	 * @return     the number of bytes written to the stream.
	 * @exception  IOException  if an I/O error occurs.
	 * @since      JDK1.0
	 */
	public static int writeUTF(OutputStream out, char[] str) throws IOException {
		int strlen= str.length;
		int utflen= 0;
		for (int i= 0; i < strlen; i++) {
			int c= str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}
		if (utflen > 65535)
			throw new UTFDataFormatException();
		out.write((utflen >>> 8) & 0xFF);
		out.write((utflen >>> 0) & 0xFF);
		if (strlen == utflen) {
			for (int i= 0; i < strlen; i++)
				out.write(str[i]);
		} else {
			for (int i= 0; i < strlen; i++) {
				int c= str[i];
				if ((c >= 0x0001) && (c <= 0x007F)) {
					out.write(c);
				} else if (c > 0x07FF) {
					out.write(0xE0 | ((c >> 12) & 0x0F));
					out.write(0x80 | ((c >> 6) & 0x3F));
					out.write(0x80 | ((c >> 0) & 0x3F));
				} else {
					out.write(0xC0 | ((c >> 6) & 0x1F));
					out.write(0x80 | ((c >> 0) & 0x3F));
				}
			}
		}
		return utflen + 2; // the number of bytes written to the stream
	}	
}
