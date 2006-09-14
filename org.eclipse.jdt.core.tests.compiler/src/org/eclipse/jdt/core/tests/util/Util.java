/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.compiler.regression.Requestor;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
public class Util {
	private static final boolean DEBUG = false;
	public static String OUTPUT_DIRECTORY = "comptest";

public static void appendProblem(StringBuffer problems, IProblem problem, char[] source, int problemCount) {
	problems.append(problemCount + (problem.isError() ? ". ERROR" : ". WARNING"));
	problems.append(" in " + new String(problem.getOriginatingFileName()));
	if (source != null) {
		problems.append(((DefaultProblem)problem).errorReportSource(source));
	}
	problems.append("\n");
	problems.append(problem.getMessage());
	problems.append("\n");
}

public static CompilationUnit[] compilationUnits(String[] testFiles) {
	int length = testFiles.length / 2;
	CompilationUnit[] result = new CompilationUnit[length];
	int index = 0;
	for (int i = 0; i < length; i++) {
		result[i] = new CompilationUnit(testFiles[index + 1].toCharArray(), testFiles[index], null);
		index += 2;
	}
	return result;
}
public static void compile(String[] pathsAndContents, Map options, String outputPath) {
		IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());
		Requestor requestor = 
			new Requestor(
				problemFactory, 
				outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator, 
				false,
				null/*no custom requestor*/,
				false, /* show category */
				false /* show warning token*/);
		
		INameEnvironment nameEnvironment = new FileSystem(getJavaClassLibs(), new String[] {}, null);
		IErrorHandlingPolicy errorHandlingPolicy = 
			new IErrorHandlingPolicy() {
				public boolean proceedOnErrors() {
					return true;
				}
				public boolean stopOnFirstError() {
					return false;
				}
			};
		CompilerOptions compilerOptions = new CompilerOptions(options);
		compilerOptions.performMethodsFullRecovery = false;
		compilerOptions.performStatementsRecovery = false;
		Compiler batchCompiler = 
			new Compiler(
				nameEnvironment, 
				errorHandlingPolicy, 
				compilerOptions,
				requestor, 
				problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		batchCompiler.compile(compilationUnits(pathsAndContents)); // compile all files together
		System.err.print(requestor.problemLog); // problem log empty if no problems
}
public static String[] concatWithClassLibs(String[] classpaths, boolean inFront) {
	String[] classLibs = getJavaClassLibs();
	if (classpaths == null) return classLibs;
	final int classLibsLength = classLibs.length;
	final int classpathsLength = classpaths.length;
	String[] defaultClassPaths = new String[classLibsLength + classpathsLength];
	if (inFront) {
		System.arraycopy(classLibs, 0, defaultClassPaths, classpathsLength, classLibsLength);
		System.arraycopy(classpaths, 0, defaultClassPaths, 0, classpathsLength);
	} else {
		System.arraycopy(classLibs, 0, defaultClassPaths, 0, classLibsLength);
		System.arraycopy(classpaths, 0, defaultClassPaths, classLibsLength, classpathsLength);
	}
	for (int i = 0; i < classpathsLength; i++) {
		File file = new File(classpaths[i]);
		if (!file.exists()) {
			file.mkdirs();
		} 
	}
	return defaultClassPaths;
}
public static String[] concatWithClassLibs(String classpath, boolean inFront) {
	String[] classLibs = getJavaClassLibs();
	final int length = classLibs.length;
	File dir = new File(classpath);
	if (!dir.exists())
		dir.mkdirs();
	String[] defaultClassPaths = new String[length + 1];
	if (inFront) {
		System.arraycopy(classLibs, 0, defaultClassPaths, 1, length);
		defaultClassPaths[0] = classpath;
	} else {
		System.arraycopy(classLibs, 0, defaultClassPaths, 0, length);
		defaultClassPaths[length] = classpath;
	} 
	return defaultClassPaths;
}
public static String convertToIndependantLineDelimiter(String source) {
	if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = source.length(); i < length; i++) {
		char car = source.charAt(i);
		if (car == '\r') {
			buffer.append('\n');
			if (i < length-1 && source.charAt(i+1) == '\n') {
				i++; // skip \n after \r
			}
		} else {
			buffer.append(car);
		}
	}
	return buffer.toString();
}
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
public static void createFile(String path, String contents) throws IOException {
	FileOutputStream output = new FileOutputStream(path);
	try {
		output.write(contents.getBytes());
	} finally {
		output.close();
	}
}
public static void createJar(String[] pathsAndContents, Map options, String jarPath) throws IOException {
	String classesPath = getOutputDirectory() + File.separator + "classes";
	File classesDir = new File(classesPath);
	flushDirectoryContent(classesDir);
	compile(pathsAndContents, options, classesPath);
	zip(classesDir, jarPath);
}
public static void createJar(String[] pathsAndContents, String jarPath, String compliance) throws IOException {
	Map options = new HashMap();
	options.put(CompilerOptions.OPTION_Compliance, compliance);
	options.put(CompilerOptions.OPTION_Source, compliance);
	options.put(CompilerOptions.OPTION_TargetPlatform, compliance);
	// Ignore options with new defaults (since bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=76530)
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
	createJar(pathsAndContents, options, jarPath);
}
public static void createSourceZip(String[] pathsAndContents, String zipPath) throws IOException {
	String sourcesPath = getOutputDirectory() + File.separator + "sources";
	File sourcesDir = new File(sourcesPath);
	flushDirectoryContent(sourcesDir);
	for (int i = 0, length = pathsAndContents.length; i < length; i+=2) {
		String sourcePath = sourcesPath + File.separator + pathsAndContents[i];
		File sourceFile = new File(sourcePath);
		sourceFile.getParentFile().mkdirs();
		createFile(sourcePath, pathsAndContents[i+1]);
	}
	zip(sourcesDir, zipPath);
}
/**
 * Generate a display string from the given String.
 * @param inputString the given input string
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.displayString("abc\ndef\tghi")]
*/
public static String displayString(String inputString){
	return displayString(inputString, 0);
}
/**
 * Generate a display string from the given String.
 * It converts:
 * <ul>
 * <li>\t to \t</li>
 * <li>\r to \\r</li>
 * <li>\n to \n</li>
 * <li>\b to \\b</li>
 * <li>\f to \\f</li>
 * <li>\" to \\\"</li>
 * <li>\' to \\'</li>
 * <li>\\ to \\\\</li>
 * <li>All other characters are unchanged.</li>
 * </ul>
 * This method doesn't convert \r\n to \n. 
 * <p>
 * Example of use:
 * <o>
 * <li>
 * <pre>
 * input string = "abc\ndef\tghi",
 * indent = 3
 * result = "\"\t\t\tabc\\n" +
 * 			"\t\t\tdef\tghi\""
 * </pre>
 * </li>
 * <li>
 * <pre>
 * input string = "abc\ndef\tghi\n",
 * indent = 3
 * result = "\"\t\t\tabc\\n" +
 * 			"\t\t\tdef\tghi\\n\""
 * </pre>
 * </li>
 * <li>
 * <pre>
 * input string = "abc\r\ndef\tghi\r\n",
 * indent = 3
 * result = "\"\t\t\tabc\\r\\n" +
 * 			"\t\t\tdef\tghi\\r\\n\""
 * </pre>
 * </li>
 * </ol>
 * </p>
 * 
 * @param inputString the given input string
 * @param indent number of tabs are added at the begining of each line.
 *
 * @return the displayed string
*/
public static String displayString(String inputString, int indent) {
	return displayString(inputString, indent, false);
}
public static String displayString(String inputString, int indent, boolean shift) {
	if (inputString == null)
		return "null";
	int length = inputString.length();
	StringBuffer buffer = new StringBuffer(length);
	java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
	for (int i = 0; i < indent; i++) buffer.append("\t");
	if (shift) indent++;
	buffer.append("\"");
	while (tokenizer.hasMoreTokens()){

		String token = tokenizer.nextToken();
		if (token.equals("\r")) {
			buffer.append("\\r");
			if (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				if (token.equals("\n")) {
					buffer.append("\\n");
					if (tokenizer.hasMoreTokens()) {
						buffer.append("\" + \n");
						for (int i = 0; i < indent; i++) buffer.append("\t");
						buffer.append("\"");
					}
					continue;
				}
				buffer.append("\" + \n");
				for (int i = 0; i < indent; i++) buffer.append("\t");
				buffer.append("\"");
			} else {
				continue;
			}
		} else if (token.equals("\n")) {
			buffer.append("\\n");
			if (tokenizer.hasMoreTokens()) {
				buffer.append("\" + \n");
				for (int i = 0; i < indent; i++) buffer.append("\t");
				buffer.append("\"");
			}
			continue;
		}	

		StringBuffer tokenBuffer = new StringBuffer();
		for (int i = 0; i < token.length(); i++){ 
			char c = token.charAt(i);
			switch (c) {
				case '\r' :
					tokenBuffer.append("\\r");
					break;
				case '\n' :
					tokenBuffer.append("\\n");
					break;
				case '\b' :
					tokenBuffer.append("\\b");
					break;
				case '\t' :
					tokenBuffer.append("\t");
					break;
				case '\f' :
					tokenBuffer.append("\\f");
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
	buffer.append("\"");
	return buffer.toString();
}
/**
 * Reads the content of the given source file.
 * Returns null if enable to read given source file.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContent("c:/temp/X.java")]
*/
public static String fileContent(String sourceFilePath) {
	File sourceFile = new File(sourceFilePath);
	if (!sourceFile.exists()) {
		if (DEBUG) System.out.println("File " + sourceFilePath + " does not exists.");
		return null;
	}
	if (!sourceFile.isFile()) {
		if (DEBUG) System.out.println(sourceFilePath + " is not a file.");
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
	return sourceContentBuffer.toString();
}

/**
 * Reads the content of the given source file and converts it to a display string.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("c:/temp/X.java", 0)]
*/
public static String fileContentToDisplayString(String sourceFilePath, int indent, boolean independantLineDelimiter) {
	String sourceString = fileContent(sourceFilePath);
	if (independantLineDelimiter) {
		sourceString = convertToIndependantLineDelimiter(sourceString);
	}
	return displayString(sourceString, indent);
}
/**
 * Reads the content of the given source file, converts it to a display string.
 * If the destination file path is not null, writes the result to this file.
 * Otherwise writes it to the console.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("c:/temp/X.java", 0, null)]
*/
public static void fileContentToDisplayString(String sourceFilePath, int indent, String destinationFilePath, boolean independantLineDelimiter) {
	String displayString = fileContentToDisplayString(sourceFilePath, indent, independantLineDelimiter);
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
			if (!current.delete()) 
				System.err.println("Could not delete " + current.getName());
		}
	}
}
/**
 * Returns the next available port number on the local host.
 */
public static int getFreePort() {
	ServerSocket socket = null;
	try {
		socket = new ServerSocket(0);
		return socket.getLocalPort();
	} catch (IOException e) {
		// ignore
	} finally {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	return -1;
}
/**
 * Search the user hard-drive for a Java class library.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJavaClassLib()]
*/
public static String[] getJavaClassLibs() {
	String jreDir = getJREDirectory();
	final String osName = System.getProperty("os.name");
	if (jreDir == null) {
		return new String[] {};
	}
	if (osName.startsWith("Mac")) {
		return new String[] { toNativePath(jreDir + "/../Classes/classes.jar") };
	}
	final String vmName = System.getProperty("java.vm.name");
	if ("J9".equals(vmName)) {
		return new String[] { toNativePath(jreDir + "/lib/jclMax/classes.zip") };
	}
	File file = new File(jreDir + "/lib/rt.jar");
	if (file.exists()) {
		return new String[] {
			toNativePath(jreDir + "/lib/rt.jar")
		};
	}
	file = new File(jreDir + "/lib/vm.jar");
	if (file.exists()) {
		// The IBM J2SE 5.0 has put the java.lang classes in vm.jar.
		return new String[] { 
			toNativePath(jreDir + "/lib/vm.jar"),
			toNativePath(jreDir + "/lib/core.jar"),
			toNativePath(jreDir + "/lib/security.jar"),
			toNativePath(jreDir + "/lib/graphics.jar") };				
	}
	return new String[] { 
		toNativePath(jreDir + "/lib/core.jar"),
		toNativePath(jreDir + "/lib/security.jar"),
		toNativePath(jreDir + "/lib/graphics.jar")
	};
}
public static String getJavaClassLibsAsString() {
	String[] classLibs = getJavaClassLibs();
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, max = classLibs.length; i < max; i++) {
		buffer
			.append(classLibs[i])
			.append(File.pathSeparatorChar);
		
	}
	return buffer.toString();
}
/**
 * Returns the JRE directory this tests are running on.
 * Returns null if none could be found.
 * 
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJREDirectory()]
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
	String container = System.getProperty("jdt.test.output_directory");
	if (container == null){
		container = System.getProperty("user.home");
		if (container == null){
			return null;
		}
	}
	if (Character.isLowerCase(container.charAt(0)) && 
			container.charAt(1) == ':') {
		return toNativePath(Character.toUpperCase(container.charAt(0))
				+ container.substring(1)) + File.separator + OUTPUT_DIRECTORY;
	}
	return toNativePath(container) + File.separator + OUTPUT_DIRECTORY;
}
public static String indentString(String inputString, int indent) {
	if (inputString == null)
		return "";
	int length = inputString.length();
	StringBuffer buffer = new StringBuffer(length);
	java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
	StringBuffer indentStr = new StringBuffer(indent);
	for (int i = 0; i < indent; i++) indentStr.append("\t");
	buffer.append(indentStr);
	while (tokenizer.hasMoreTokens()){
		String token = tokenizer.nextToken();
		buffer.append(token);
		if (token.equals("\r") || token.equals("\n")) {
			buffer.append(indentStr);
		}
	}
	return buffer.toString();
}
public static boolean isMacOS() {
	return System.getProperty("os.name").indexOf("Mac") != -1;
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
/**
 * Unzip the contents of the given zip in the given directory (create it if it doesn't exist)
 */
public static void unzip(String zipPath, String destDirPath) throws IOException {

	InputStream zipIn = new FileInputStream(zipPath);
	byte[] buf = new byte[8192];
	File destDir = new File(destDirPath);
	ZipInputStream zis = new ZipInputStream(zipIn);
	FileOutputStream fos = null;
	try {
		ZipEntry zEntry;
		while ((zEntry = zis.getNextEntry()) != null) {
			// if it is empty directory, create it
			if (zEntry.isDirectory()) {
				new File(destDir, zEntry.getName()).mkdirs();
				continue;
			}
			// if it is a file, extract it
			String filePath = zEntry.getName();
			int lastSeparator = filePath.lastIndexOf("/"); //$NON-NLS-1$
			String fileDir = ""; //$NON-NLS-1$
			if (lastSeparator >= 0) {
				fileDir = filePath.substring(0, lastSeparator);
			}
			//create directory for a file
			new File(destDir, fileDir).mkdirs();
			//write file
			File outFile = new File(destDir, filePath);
			fos = new FileOutputStream(outFile);
			int n = 0;
			while ((n = zis.read(buf)) >= 0) {
				fos.write(buf, 0, n);
			}
			fos.close();
		}
	} catch (IOException ioe) {
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException ioe2) {
			}
		}
	} finally {
		try {
			zipIn.close();
			if (zis != null)
				zis.close();
		} catch (IOException ioe) {
		}
	}
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
public static void zip(File rootDir, String zipPath) throws IOException {
	ZipOutputStream zip = null;
	try {
		zip  = new ZipOutputStream(new FileOutputStream(zipPath));
		zip(rootDir, zip, rootDir.getPath().length()+1); // 1 for last slash
	} finally {
		if (zip != null) {
			zip.close();
		}
	}
}
private static void zip(File dir, ZipOutputStream zip, int rootPathLength) throws IOException {
	String[] list = dir.list();
	if (list != null) {
		for (int i = 0, length = list.length; i < length; i++) {
			String name = list[i];
			File file = new File(dir, name);
			if (file.isDirectory()) {
				zip(file, zip, rootPathLength);
			} else {
				String path = file.getPath();
				path = path.substring(rootPathLength);
				ZipEntry entry = new ZipEntry(path.replace('\\', '/'));
				zip.putNextEntry(entry);
				zip.write(org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(file));
				zip.closeEntry();
			}
		}
	}
}
}
