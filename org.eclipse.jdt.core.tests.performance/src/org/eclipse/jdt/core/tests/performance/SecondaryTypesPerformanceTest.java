/*******************************************************************************
 * Copyright (c) 2016, 2021 Sven Strohschein and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sven Strohschein - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.test.performance.PerformanceTestCase;

public class SecondaryTypesPerformanceTest extends PerformanceTestCase {

	private static final String testScratchArea = Util.getOutputDirectory() + File.separator + "enumPerformanceTestScratchArea";
	private static final int numberOfClasspathsToCreate = 10; //can get set higher, but not lower than 3 (the first 3 packages are used within the test case)

	public static Test suite() {
		TestSuite suite = new TestSuite(SecondaryTypesPerformanceTest.class.getName());
		suite.addTestSuite(SecondaryTypesPerformanceTest.class);
		return suite;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		File testScratchAreaFile = new File(testScratchArea);
		if(!testScratchAreaFile.exists()) {
			testScratchAreaFile.mkdir();

			File packageFile = new File(testScratchAreaFile, "test" + File.separator + "performance");
			packageFile.mkdirs();
			writeDummySourceClasses(packageFile, "test.performance");

			Set<String> paths = createClasspathStrings();
			for(String path: paths) {
				File pathFile = new File(path);
				pathFile.mkdir();

				writeDummySourceClasses(pathFile, "test.performance." + pathFile.getName());
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		File testScratchAreaFile = new File(testScratchArea);
		if(testScratchAreaFile.exists()) {
			Util.delete(testScratchAreaFile);
		}
	}

	@SuppressWarnings("deprecation")
	public void test01() {
		List<String> classpathList = new ArrayList<>();
		classpathList.addAll(createClasspathStrings());
		classpathList.add(testScratchArea);

		for (int i = 0; i<10; ++i) {
			ASTParser parser = ASTParser.newParser(AST.JLS16);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			parser.setCompilerOptions(createCompilerOptions());
			parser.setUnitName("X");
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(classpathList.toArray(new String[classpathList.size()]), null, null, true);
			parser.setSource((""
					+ "import test.performance.test1.*;\n"
					+ "public enum X\n" +
					"{\n" +
					"    TEST;\n" +
					"\n" +
					"    public void method() {\n" +
					"        new Z().toString();\n" +
					"        new test.performance.test2.Z().toString();\n" +
					"        new test.performance.test3.Z().toString();\n" +
					"\n" +
					"    }\n" +
					"}").toCharArray());

			startMeasuring();
			ASTNode theAST = parser.createAST(null);
			theAST.accept(new ASTVisitor() {});
			stopMeasuring();
		}

		//Commit measure
		commitMeasurements();
		assertPerformance();
	}

	private Map<String, String> createCompilerOptions() {
		Map<String, String> compilerOptions = new HashMap<>(10);
		compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		compilerOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, JavaCore.COMPILER_TASK_PRIORITY_HIGH);
		compilerOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		return compilerOptions;
	}

	private Set<String> createClasspathStrings() {
		Set<String> paths = new LinkedHashSet<>(numberOfClasspathsToCreate);
		for(int i = 1; i <= numberOfClasspathsToCreate; i++) {
			paths.add(testScratchArea + File.separator + "test" + File.separator + "performance" + File.separator + "test" + i);
		}
		return paths;
	}

	private void writeDummySourceClasses(File aPath, String packageName) throws IOException {
		writeFile(new File(aPath, "Z.java"), "public class Z {}");

		"ABC".toLowerCase(Locale.ENGLISH);

		for(int i = 1; i <= 100; i++) {
			String sourceContent = "package " + packageName + ";\n" +
				"\n" +
				"import java.util.Locale;\n" +
				"\n" +
				"public class X" + i + " {\n" +
				"\tpublic X" + i + "() {\n" +
				"\t\t\"ABC\".toLowerCase(Locale.ENGLISH);\n" +
				"new Object() {\n" +
				"            @Override\n" +
				"            public String toString() {\n" +
				"                return \"Test\";\n" +
				"            }\n" +
				"        }.toString();" +
				"\t}\n" +
				"}";

			writeFile(new File(aPath, "X" + i + ".java"), sourceContent);
		}
	}

	private void writeFile(File aFile, String aSource) throws IOException {

		try (FileWriter fileWriter = new FileWriter(aFile)) {
			fileWriter.write(aSource);
		}
	}
}