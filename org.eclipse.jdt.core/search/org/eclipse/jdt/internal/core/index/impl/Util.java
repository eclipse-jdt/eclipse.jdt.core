package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.Vector;

public class Util {

	private Util() {
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
		int len1 = str1.length;
		int len2 = str2.length;
		int n = Math.min(len1, len2);
		int i = 0;
		while (n-- != 0) {
			char c1 = str1[i];
			char c2 = str2[i++];
			if (c1 != c2) {
				return c1 - c2;
			}
		}
		return len1 - len2;
	}

	public static byte[] getFileByteContent(File file) throws java.io.IOException {
		int fileLength;
		byte classFileBytes[] = new byte[fileLength = (int) file.length()];
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			int bytesRead = 0;
			int lastReadSize = 0;
			while ((lastReadSize != -1) && (bytesRead != fileLength)) {
				lastReadSize = stream.read(classFileBytes, bytesRead, fileLength - bytesRead);
				bytesRead += lastReadSize;
			}
		} finally {
			if (stream != null)
				stream.close();
		}
		return classFileBytes;
	}

	public static char[] getFileCharContent(File file) throws java.io.IOException {
		byte[] bytes = null;
		BufferedReader reader = null;
		char[] contents = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			int length = (int) file.length();
			contents = new char[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				len += readSize;
				readSize = reader.read(contents, len, length - len);
			}
			if (len != length)
				System.arraycopy(contents, 0, (contents = new char[len]), 0, len);
		} finally {
			if (reader != null)
				reader.close();
		}
		return contents;
	}

	/**
	 * Adds all files with the given suffix in the given directory 
	 * and all its subdirectories to the given Vector.
	 */
	public static void getFiles(File fileOrDir, String[] suffix, Vector v) {
		if (fileOrDir.isDirectory()) {
			String[] list = fileOrDir.list();
			if (list != null) {
				sort(list);
				for (int i = 0; i < list.length; ++i) {
					File file = new File(fileOrDir, list[i]);
					getFiles(file, suffix, v);
				}
			}
		} else {
			for (int i = 0; i < suffix.length; i++) {
				if (fileOrDir.getName().toLowerCase().endsWith(suffix[i])) {
					v.addElement(fileOrDir);
					break;
				}
			}
		}
	}

	/**
	 * Adds all files with the given suffix in the given directory 
	 * and all its subdirectories to the given Vector.
	 */
	public static void getFiles(File fileOrDir, String suffix, Vector v) {
		if (fileOrDir.isDirectory()) {
			String[] list = fileOrDir.list();
			if (list != null) {
				sort(list);
				for (int i = 0; i < list.length; ++i) {
					File file = new File(fileOrDir, list[i]);
					getFiles(file, suffix, v);
				}
			}
		} else {
			if (fileOrDir.getName().toLowerCase().endsWith(suffix)) {
				v.addElement(fileOrDir);
			}
		}
	}

	/**
	 * Returns the length of the common prefix between s1 and s2.
	 */
	public static int prefixLength(char[] s1, char[] s2) {
		int len = 0;
		int max = Math.min(s1.length, s2.length);
		for (int i = 0; i < max && s1[i] == s2[i]; ++i)
			++len;
		return len;
	}

	/**
	 * Returns the length of the common prefix between s1 and s2.
	 */
	public static int prefixLength(String s1, String s2) {
		int len = 0;
		int max = Math.min(s1.length(), s2.length());
		for (int i = 0; i < max && s1.charAt(i) == s2.charAt(i); ++i)
			++len;
		return len;
	}

	private static void quickSort(char[][] list, int left, int right) {
		int original_left = left;
		int original_right = right;
		char[] mid = list[(left + right) / 2];
		do {
			while (compare(list[left], mid) < 0) {
				left++;
			}
			while (compare(mid, list[right]) < 0) {
				right--;
			}
			if (left <= right) {
				char[] tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right) {
			quickSort(list, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, left, original_right);
		}
	}

	private static void quickSort(int[] list, int left, int right) {
		int original_left = left;
		int original_right = right;
		int mid = list[(left + right) / 2];
		do {
			while (list[left] < mid) {
				left++;
			}
			while (mid < list[right]) {
				right--;
			}
			if (left <= right) {
				int tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);
		if (original_left < right) {
			quickSort(list, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, left, original_right);
		}
	}

	private static void quickSort(String[] list, int left, int right) {

		int original_left = left;
		int original_right = right;

		String mid = list[(left + right) / 2];
		do {
			while (list[left].compareTo(mid) < 0) {
				left++;
			}
			while (mid.compareTo(list[right]) < 0) {
				right--;
			}
			if (left <= right) {
				String tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);

		if (original_left < right) {
			quickSort(list, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, left, original_right);
		}
	}

	private static void quickSort(IndexedFile[] list, int left, int right) {

		int original_left = left;
		int original_right = right;

		IndexedFile mid = list[(left + right) / 2];
		do {
			while (list[left].getPath().compareTo(mid.getPath()) < 0) {
				left++;
			}
			while (mid.getPath().compareTo(list[right].getPath()) < 0) {
				right--;
			}
			if (left <= right) {
				IndexedFile tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		}
		while (left <= right);

		if (original_left < right) {
			quickSort(list, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, left, original_right);
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
		int utflen = in.readUnsignedShort();
		char str[] = new char[utflen];
		int count = 0;
		int strlen = 0;
		while (count < utflen) {
			int c = in.readUnsignedByte();
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
					str[strlen++] = (char) c;
					break;
				case 12 :
				case 13 :
					// 110x xxxx   10xx xxxx
					count += 2;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2 = in.readUnsignedByte();
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException();
					str[strlen++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14 :
					// 1110 xxxx  10xx xxxx  10xx xxxx
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2 = in.readUnsignedByte();
					char3 = in.readUnsignedByte();
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new UTFDataFormatException();
					str[strlen++] =
						(char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default :
					// 10xx xxxx,  1111 xxxx
					throw new UTFDataFormatException();
			}
		}
		if (strlen < utflen) {
			System.arraycopy(str, 0, str = new char[strlen], 0, strlen);
		}
		return str;
	}

	public static void sort(char[][] list) {
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	public static void sort(int[] list) {
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	public static void sort(String[] list) {
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	public static void sort(IndexedFile[] list) {
		if (list.length > 1)
			quickSort(list, 0, list.length - 1);
	}

	public static int startsWith(char[] str, char[] prefix) {
		int len1 = str.length;
		int len2 = prefix.length;
		int n = Math.min(len1, len2);
		int i = 0;
		while (n-- != 0) {
			char c1 = str[i];
			char c2 = prefix[i++];
			if (c1 != c2) {
				return c1 - c2;
			}
		}
		if (len2 == i)
			return 0;

		return 1;
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
	 * @exception  IOException  if an I/O error occurs.
	 * @since      JDK1.0
	 */
	public static void writeUTF(OutputStream out, char[] str) throws IOException {
		int strlen = str.length;
		int utflen = 0;
		for (int i = 0; i < strlen; i++) {
			int c = str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else
				if (c > 0x07FF) {
					utflen += 3;
				} else {
					utflen += 2;
				}
		}
		if (utflen > 65535)
			throw new UTFDataFormatException();
		out.write((utflen >>> 8) & 0xFF);
		out.write((utflen >>> 0) & 0xFF);
		for (int i = 0; i < strlen; i++) {
			int c = str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				out.write(c);
			} else
				if (c > 0x07FF) {
					out.write(0xE0 | ((c >> 12) & 0x0F));
					out.write(0x80 | ((c >> 6) & 0x3F));
					out.write(0x80 | ((c >> 0) & 0x3F));
				} else {
					out.write(0xC0 | ((c >> 6) & 0x1F));
					out.write(0x80 | ((c >> 0) & 0x3F));
				}
		}
	}

}
