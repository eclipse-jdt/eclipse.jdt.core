package org.eclipse.jdt.core.tests.util;

import java.io.*;
public class Util {
	public static String OUTPUT_DIRECTORY = "comptest";

	public static int MAX_PORT_NUMBER = 9999;
/**
 * Copy the given source (a file or a directory that must exists) to the given destination (a directory that must exists).
 */
public static void copy(String sourcePath, String destPath) {
	sourcePath = toNativePath(sourcePath);
	destPath = toNativePath(destPath);
	File source = new File(sourcePath);
	if (!source.exists()) return;
	File dest = new File(destPath);
	if (!dest.exists()) return;
	if (source.isDirectory()) {
		String[] files = source.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String file = files[i];
				File sourceFile = new File(source, file);
				if (sourceFile.isDirectory()) {
					File destSubDir = new File(dest, file);
					destSubDir.mkdir();
					copy(sourceFile.getPath(), destSubDir.getPath());
				} else {
					copy(sourceFile.getPath(), dest.getPath());
				}
			}
		}
	} else {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(source);
			File destFile = new File(dest, source.getName());
			if (destFile.exists() && !destFile.delete()) {
				throw new IOException(destFile + " is in use");
			}
		 	out = new FileOutputStream(destFile);
			int bufferLength = 1024;
			byte[] buffer = new byte[bufferLength];
			int read = 0;
			while (read != -1) {
				read = in.read(buffer, 0, bufferLength);
				if (read != -1) {
					out.write(buffer, 0, read);
				}
			}
		} catch (IOException e) {
			throw new Error(e.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
/**
 * Generate a display string from the given String.
 * @param indent number of tabs are added at the begining of each line.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.displayString("abc\ndef\tghi")]
*/
public static String displayString(String inputString){
	return displayString(inputString, 0);
}
/**
 * Generate a display string from the given String.
 * @param indent number of tabs are added at the begining of each line.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.displayString("abc\ndef\tghi", 3)]
*/
public static String displayString(String inputString, int indent) {
	int length = inputString.length();
	StringBuffer buffer = new StringBuffer(length);
	java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r");
	int count = 0;
	for (int i = 0; i < indent; i++) buffer.append("\t");
	buffer.append("\"");
	while (tokenizer.hasMoreTokens()){
		if (count++ != 0) {
			buffer.append("\\n\" + \n");
			for (int i = 0; i < indent; i++) buffer.append("\t");
			buffer.append("\"");
		}
		String token = tokenizer.nextToken();
		StringBuffer tokenBuffer = new StringBuffer();
		for (int i = 0; i < token.length(); i++){ 
			char c = token.charAt(i);
			switch (c) {
				case '\b' :
					tokenBuffer.append("\\b");
					break;
				case '\t' :
					tokenBuffer.append("\t");
					break;
				case '\n' :
					tokenBuffer.append("\\n");
					break;
				case '\f' :
					tokenBuffer.append("\\f");
					break;
				case '\r' :
					tokenBuffer.append("\\r");
					break;
				case '\"' :
					tokenBuffer.append("\\\"");
					break;
				case '\'' :
					tokenBuffer.append("\\'");
					break;
				case '\\' :
					tokenBuffer.append("\\\\");
					break;
				default :
					tokenBuffer.append(c);
			}
		}
		buffer.append(tokenBuffer.toString());
	}
	char lastChar = length == 0 ? 0 : inputString.charAt(length-1);
	if ((lastChar == '\n') || (lastChar == '\r')) buffer.append("\\n");
	buffer.append("\"");
	return buffer.toString();
}
/**
 * Reads the content of the given source file and converts it to a display string.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("c:/temp/X.java", 0)]
*/
public static String fileContentToDisplayString(String sourceFilePath, int indent) {
	File sourceFile = new File(sourceFilePath);
	if (!sourceFile.exists()) {
		System.out.println("File " + sourceFilePath + " does not exists.");
		return null;
	}
	if (!sourceFile.isFile()) {
		System.out.println(sourceFilePath + " is not a file.");
		return null;
	}
	StringBuffer sourceContentBuffer = new StringBuffer();
	FileInputStream input = null;
	try {
		input = new FileInputStream(sourceFile);
	} catch (FileNotFoundException e) {
		return null;
	}
	try { 
		int read;
		do {
			read = input.read();
			if (read != -1) {
				sourceContentBuffer.append((char)read);
			}
		} while (read != -1);
		input.close();
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	} finally {
		try {
			input.close();
		} catch (IOException e2) {
		}
	}
	return displayString(sourceContentBuffer.toString(), indent);
}
/**
 * Reads the content of the given source file, converts it to a display string.
 * If the destination file path is not null, writes the result to this file.
 * Otherwise writes it to the console.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("c:/temp/X.java", 0, null)]
*/
public static void fileContentToDisplayString(String sourceFilePath, int indent, String destinationFilePath) {
	String displayString = fileContentToDisplayString(sourceFilePath, indent);
	if (destinationFilePath == null) {
		System.out.println(displayString);
		return;
	}
	writeToFile(displayString, destinationFilePath);
}
/**
 * Flush content of a given directory (leaving it empty),
 * no-op if not a directory.
 */
public static void flushDirectoryContent(File dir) {
	if (dir.isDirectory()) {
		String[] files = dir.list();
		if (files == null) return;
		for (int i = 0, max = files.length; i < max; i++) {
			File current = new File(dir, files[i]);
			if (current.isDirectory()) {
				flushDirectoryContent(current);
			}
			current.delete();
		}
	}
}
/**
 * Search the user hard-drive for a Java class library.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJavaClassLib()]
*/
public static String getJavaClassLib() {
	String jreDir = getJREDirectory();
	if (jreDir == null) 
		return null;
	else
		return toNativePath(jreDir + "/lib/rt.jar");
}
/**
 * Returns the JRE directory this tests are running on.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJDKDirectory()]
*/
public static String getJREDirectory() {
	return System.getProperty("java.home");
}
/**
 * Search the user hard-drive for a possible output directory.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getOutputDirectory()]
*/
public static String getOutputDirectory() {
	String container = System.getProperty("user.home");
	if (container == null){
		return null;
	} else {
		return toNativePath(container) + File.separator + OUTPUT_DIRECTORY;
	}
}
/**
 * Returns whether one of the arguments is "-expert".
 */
public static boolean isExpert(String[] args) {
	for (int i = 0; i < args.length; i++) {
		if (args[i].toLowerCase().equals("-expert")) {
			return true;
		}
	}
	return false;
}
/**
 * Returns the next available port number on the local host.
 */
public static int nextAvailablePortNumber() {
	for (int i = MAX_PORT_NUMBER; i > 1000; i--) {
		int localPort = new SocketHelper().getAvailablePort(i);
		if (localPort != -1) {
			return localPort;
		}
	}
	return -1;
}
/**
 * Makes the given path a path using native path separators as returned by File.getPath()
 * and trimming any extra slash.
 */
public static String toNativePath(String path) {
	String nativePath = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
	return
		nativePath.endsWith("/") || nativePath.endsWith("\\") ?
			nativePath.substring(0, nativePath.length() - 1) :
			nativePath;
}
public static void writeToFile(String contents, String destinationFilePath) {
	File destFile = new File(destinationFilePath);
	FileOutputStream output = null;
	try {
		output = new FileOutputStream(destFile);
		PrintWriter writer = new PrintWriter(output);
		writer.print(contents);
		writer.flush();
	} catch (IOException e) {
		e.printStackTrace();
		return;
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e2) {
			}
		}
	}
}
}
