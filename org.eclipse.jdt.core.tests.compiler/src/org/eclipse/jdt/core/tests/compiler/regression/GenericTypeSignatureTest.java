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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTypeTableAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTypeTableEntry;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.core.util.ISignatureAttribute;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeSignatureTest extends AbstractRegressionTest {
	static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	static boolean runJavac = CompilerOptions.ENABLED.equals(RUN_SUN_JAVAC);
	static {
		// Use this static to specify a subset of tests using testsNames, testNumbers or testsRange arrays
//		testsRange = new int[] { 66, -1 };
//		testsNumbers = new int[] { 65 };
	}
	public static Test suite() {
		if (testsNames != null || testsNumbers!=null || testsRange!=null) {
			return new RegressionTestSetup(suite(testClass(), testClass().getName()), highestComplianceLevels());
		} else {
			return setupSuite(testClass());
		}
	}

	public static Class testClass() {
		return GenericTypeSignatureTest.class;
	}
	IPath dirPath;
	
	public GenericTypeSignatureTest(String name) {
		super(name);
	}

	/*######################################
	 * Specific method to let tests Sun javac compilation available...
	 #######################################*/
	/*
	 * Cleans up the given directory by removing all the files it contains as well
	 * but leaving the directory.
	 * @throws TargetException if the target path could not be cleaned up
	 */
	private void cleanupDirectory(File directory) throws TargetException {
		if (!directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete()) {
					throw new TargetException("Could not delete file " + file.getPath());
				}
			}
		}
		if (!directory.delete()) {
			throw new TargetException("Could not delete directory " + directory.getPath());
		}
	}

	/*
	 * Toggle compiler in mode -1.5
	 */
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
		return options;
	}

	/*#########################################
	 * Override basic runConform and run Negative methods to compile test files
	 * with Sun compiler (if specified) and compare its results with ours.
	 ##########################################*/
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[], java.lang.String)
	 */
	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			boolean shouldFlushOutputDirectory, String[] vmArguments,
			Map customOptions) {
		try {
			super.runConformTest(testFiles, expectedSuccessOutputString,
					classLib, shouldFlushOutputDirectory, vmArguments,
					customOptions);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (runJavac)
				runJavac(testFiles, null);
		}
	}

	/*
	 * Run Sun compilation using javac.
	 * Use JRE directory to retrieve javac bin directory and current classpath for
	 * compilation.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 */
	protected void runJavac(String[] testFiles, final String expectedProblemLog) {
		try {
			// Write files in dir
			final IPath dirFilePath = writeFiles(testFiles);
			
			// Create thread to run process
			Thread waitThread = new Thread() {
				public void run() {
					String testName = shortTestName();
					Process process = null;
					try {
						// Compute classpath
						String[] classpath = getDefaultClassPaths();
						StringBuffer cp = new StringBuffer();
						int length = classpath.length;
						for (int i = 0; i < length; i++) {
							cp.append(classpath[i]);
							if (i<(length-1)) cp.append(";");
						}
						// Compute command line
						IPath jdkDir = (new Path(Util.getJREDirectory())).removeLastSegments(1);
						IPath javacPath = jdkDir.append("bin").append("javac.exe");
						StringBuffer cmdLine = new StringBuffer(javacPath.toString());
						cmdLine.append(" -classpath ");
						cmdLine.append(cp);
						cmdLine.append(" -source 1.5 -deprecation -Xlint "); // enable recommended warnings
						if (GenericTypeSignatureTest.this.dirPath.equals(dirFilePath)) {
							cmdLine.append("*.java");
						} else {
							IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(GenericTypeSignatureTest.this.dirPath.segmentCount());
							String subDirName = subDirPath.toString().substring(subDirPath.getDevice().length());
							cmdLine.append(subDirName);
						}
//						System.out.println(testName+": "+cmdLine.toString());
						// Launch process
						process = Runtime.getRuntime().exec(cmdLine.toString(), null, GenericTypeSignatureTest.this.dirPath.toFile());
						process.waitFor();
						// Compare compilation results
						int exitValue = process.exitValue();
						if (expectedProblemLog == null && exitValue != 0) {
							System.out.println(testName+": javac has found error(s) although we're expecting conform result!");
						}
						else if (expectedProblemLog != null && exitValue == 0) {
							System.out.println(testName+": javac has found no error although we're expecting negative result:");
							System.out.println(expectedProblemLog);
						}
					} catch (IOException ioe) {
						System.out.println(testName+": Not possible to launch Sun javac compilation!");
					} catch (InterruptedException e1) {
						if (process != null) process.destroy();
						System.out.println(testName+": Sun javac compilation was aborted!");
					}
				}
			};
			
			// Run thread and wait 5 seconds for end of compilation
			waitThread.start();
			try {
				waitThread.join(2000);

			} catch (InterruptedException e1) {
				// do nothing
			}
			if (waitThread.isAlive()) {
				waitThread.interrupt();
			}

			// Clean up written file(s)
			IPath testDir =  new Path(Util.getOutputDirectory()).append(shortTestName());
			cleanupDirectory(testDir.toFile());
		} catch (Exception e) {
			// fails silently...
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * Override to compile test files with Sun compiler if specified and compare its results with ours.
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions,
			boolean generateOutput) {
		try {
			super.runNegativeTest(testFiles, expectedProblemLog, classLib,
					shouldFlushOutputDirectory, customOptions, generateOutput);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (runJavac)
				runJavac(testFiles, expectedProblemLog);
		}
	}

	/*
	 * Get short test name (without compliance info)
	 */
	String shortTestName() {
		String fname = getName();
		int idx = fname.indexOf(" - ");
		if (idx < 0) {
			return fname;
		} else {
			return fname.substring(idx+3);
		}
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"    	X<X<String>> xs = new X<X<String>>(new X<String>(\"SUCCESS\"));\n" + 
				"        System.out.print(xs.t.t);\n" + 
				"    }\n" + 
				"}",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"}"
			},
			"SUCCESS");

		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Lp/A<TT;>;", new String(signatureAttribute.getSignature()));
		IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
		int length = methodInfos.length;
		assertEquals("Wrong size", 2, length);
		IMethodInfo mainMethod = null;
		for (int i = 0; i < length; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if ("main".equals(new String(methodInfo.getName()))) {
				mainMethod = methodInfo;
				break;
			}
		}
		assertNotNull(mainMethod);
		ICodeAttribute codeAttribute = mainMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		ILocalVariableTypeTableAttribute localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		ILocalVariableTypeTableEntry[] entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		ILocalVariableTypeTableEntry xsEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("xs".equals(new String(entry.getName()))) {
				xsEntry = entry;
				break;
			}
		}
		assertNotNull(xsEntry);
		assertEquals("Wrong signature", "LX<LX<Ljava/lang/String;>;>;", new String(xsEntry.getSignature()));

		IMethodInfo constructorMethod = null;
		for (int i = 0; i < length; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if ("<init>".equals(new String(methodInfo.getName()))) {
				constructorMethod = methodInfo;
				break;
			}
		}
		assertNotNull(constructorMethod);
		codeAttribute = constructorMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		ILocalVariableTypeTableEntry thisEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("this".equals(new String(entry.getName()))) {
				thisEntry = entry;
				break;
			}
		}
		assertNotNull(thisEntry);
		assertEquals("Wrong signature", "LX<TT;>;", new String(thisEntry.getSignature()));
		ILocalVariableTypeTableEntry tEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("t".equals(new String(entry.getName()))) {
				tEntry = entry;
				break;
			}
		}
		assertNotNull(tEntry);
		assertEquals("Wrong signature", "TT;", new String(tEntry.getSignature()));
	}
	
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class X extends p.A<String> {\n" + 
				"    X() {\n" + 
				"        super(null);\n" + 
				"    }\n" + 
				"}",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected A(P p) {\n" + 
				"    }\n" + 
				"}"
			});
		
		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "Lp/A<Ljava/lang/String;>;", new String(signatureAttribute.getSignature()));

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "p/A.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "<P:Ljava/lang/Object;>Ljava/lang/Object;", new String(signatureAttribute.getSignature()));

		IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
		int length = methodInfos.length;
		assertEquals("Wrong size", 1, length);
		IMethodInfo constructorMethod = methodInfos[0];
		ICodeAttribute codeAttribute = constructorMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		ILocalVariableTypeTableAttribute localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		ILocalVariableTypeTableEntry[] entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		ILocalVariableTypeTableEntry thisEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("this".equals(new String(entry.getName()))) {
				thisEntry = entry;
				break;
			}
		}
		assertNotNull(thisEntry);
		assertEquals("Wrong signature", "Lp/A<TP;>;", new String(thisEntry.getSignature()));
		ILocalVariableTypeTableEntry tEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("p".equals(new String(entry.getName()))) {
				tEntry = entry;
				break;
			}
		}
		assertNotNull(tEntry);
		assertEquals("Wrong signature", "TP;", new String(tEntry.getSignature()));
	}	
	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	private IPath writeFiles(String[] testFiles) {
		// Compute and create specific dir
		IPath outDir = new Path(Util.getOutputDirectory());
		this.dirPath =  outDir.append(shortTestName());
		IPath dirFilePath = this.dirPath;
		File dir = dirFilePath.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// For each given test files
		for (int i=0, length=testFiles.length; i<length; i++) {
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			IPath filePath = dirFilePath.append(fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				dirFilePath = filePath.removeLastSegments(1);
				dir = dirFilePath.toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			Util.writeToFile(contents, filePath.toString());
		}
		return dirFilePath;
	}
}
