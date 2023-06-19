/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;

import junit.framework.Test;

/**
 * The intent of this tests series is to check the consistency of parts of our
 * APIs documentation with real values and results.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class APIDocumentationTests extends AbstractASTTests {

	private static final String PATH_JAVA_CORE_JAVA = "org/eclipse/jdt/core/JavaCore.java";
	private static final String ORG_ECLIPSE_JDT_CORE_SOURCE = "org.eclipse.jdt.core.source";
	private static final String ORG_ECLIPSE_JDT_CORE = "org.eclipse.jdt.core";
	private static final String REFERENCE_FILE_SCHEMA = "reference:file:";

	public APIDocumentationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(APIDocumentationTests.class);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testBug86380";
//		TESTS_NAMES = new String[] { "test056" };
//		TESTS_NUMBERS = new int[] { 78, 79, 80 };
//		TESTS_RANGE = new int[] { 83304, -1 };
		}

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

/**
 * Helper class able to analyze JavaCore options javadocs.
 */
static class JavaCoreJavadocAnalyzer {
	static final String OPTION_BEGIN = "<dt>Option id:</dt><dd><code>\"";
	static final String DEFAULT_BEGIN = "<dt>Default:</dt><dd><code>\"";
	static final String END = "\"</code></dd>";
	private String javadoc;
	void reset(String newJavadoc) {
		// do not pass null - unchecked
		this.javadoc = newJavadoc;
		this.analyzed = false;
		this.optionID = null;
		this.defaultValue = null;
	}
	private boolean analyzed;
	private String optionID, defaultValue;
	private void analyze() {
		if (!this.analyzed) {
			this.analyzed = true;
			BufferedReader javadocReader = new BufferedReader(new StringReader(this.javadoc));
			String line;
			try {
				while ((line = javadocReader.readLine()) != null) {
					int start = line.indexOf(OPTION_BEGIN);
					if (start > -1) {
						int end = line.indexOf(END, start);
						this.optionID = line.substring(start+OPTION_BEGIN.length(), end);
					}
					start = line.indexOf(DEFAULT_BEGIN);
					if (start > -1) {
						int end = line.indexOf(END, start);
						this.defaultValue = line.substring(start+DEFAULT_BEGIN.length(), end);
					}
					if (this.optionID != null && this.defaultValue != null)
						return;
				}
			} catch (IOException e) {
				// silent
			}
		}
	}
	String getOptionID() {
		analyze();
		return this.optionID;
	}
	String getDefaultValue() {
		analyze();
		return this.defaultValue;
	}
	boolean isDeprecated() {
		return this.javadoc.indexOf("* @deprecated") != -1;
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202490
// checks that option ids and option default values match between the code and
// the javadoc
public void testJavaCoreAPI() throws CoreException, IllegalArgumentException, IllegalAccessException, IOException {

	// Makes sure the
	JavaRuntime.getVMInstallTypes();

	// fetch JavaCore class
	Class javaCoreClass = JavaCore.class;

	char[] sourceChars = getJavaCoreClassSource();
	assertNotNull("JavaCore.java not found, skipping APIDocumentationTests#test001", sourceChars);

	// load field values in a map
	LinkedHashMap<String, String> realOptionIDs = new LinkedHashMap();
	Field[] fields = javaCoreClass.getDeclaredFields();
	for (int i = 0, l = fields.length; i < l; i++) {
		Field field = fields[i];
		int modifiers = field.getModifiers();
		if (Modifier.isPublic(modifiers) &&
				Modifier.isStatic(modifiers) &&
				field.getType() == String.class) {
			String constantValue = (String) field.get(null);
			if (constantValue.startsWith(JavaCore.PLUGIN_ID)) {
				realOptionIDs.put(field.getName(), constantValue);
			}
		}
	}
	// exempt a few values
	realOptionIDs.remove("PLUGIN_ID");
	realOptionIDs.remove("BUILDER_ID");
	realOptionIDs.remove("JAVA_SOURCE_CONTENT_TYPE");
	realOptionIDs.remove("MODEL_ID");
	realOptionIDs.remove("NATURE_ID");
	realOptionIDs.remove("DEFAULT_JAVA_FORMATTER");

	// build cross-index
	LinkedHashMap<String, String> realOptionNames = new LinkedHashMap();
	Iterator<Map.Entry<String, String>> optionIDs = realOptionIDs.entrySet().iterator();
	while (optionIDs.hasNext()) {
		Map.Entry<String, String> optionID = optionIDs.next();
		realOptionNames.put(optionID.getValue(), optionID.getKey());
	}
	// tests have specific defaults for these:
	realOptionNames.remove(JavaCore.COMPILER_SOURCE);
	realOptionNames.remove(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
	realOptionNames.remove(JavaCore.COMPILER_COMPLIANCE);

	// This option is set in LaunchingPreferenceInitializer and always
	// differs from empty default value
	realOptionNames.remove(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);

	// fetch default option values
	Hashtable<String, String> realDefaultValues = JavaCore.getDefaultOptions();
	// load documented values in a map
	ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
	parser.setSource(sourceChars);
	ASTNode rootNode = parser.createAST(null);
	final JavaCoreJavadocAnalyzer analyzer = new JavaCoreJavadocAnalyzer();
	final Hashtable javadocOptionIDs = new Hashtable();
	final Hashtable javadocDefaultValues = new Hashtable();
	final Hashtable deprecatedFields = new Hashtable();
	rootNode.accept(new ASTVisitor() {
		public boolean visit(FieldDeclaration node) {
			String key = ((VariableDeclarationFragment) node.fragments().get(0)).getName().getIdentifier();
			Javadoc javadoc = node.getJavadoc();
			if (javadoc != null) {
				analyzer.reset(javadoc.toString());
				String id, value;
				if ((id = analyzer.getOptionID()) != null) {
					javadocOptionIDs.put(key, id);
				}
				if ((value = analyzer.getDefaultValue()) != null) {
					javadocDefaultValues.put(id, value);
				}
				if (analyzer.isDeprecated()) {
					deprecatedFields.put(key, key /* not null */);
				}
			}
			return super.visit(node);
		}
	});
	// checking ids
	Iterator check = realOptionIDs.entrySet().iterator();
	String key, value;
	String expected = "", actual = "";
	List<String> errors = new ArrayList<>();
	while (check.hasNext()) {
		Map.Entry entry = (Map.Entry) check.next();
		key = (String) entry.getKey();
		value = (String) entry.getValue();
		if (deprecatedFields.get(key) == null) {
			if (!value.equals(javadocOptionIDs.get(key))) {
				expected = value;
				actual = (String) javadocOptionIDs.get(key);
				String errMessage = "option ID mismatch for " + key + ", real: " + expected + ", javadoc: " + actual;
				System.out.println(errMessage);
				errors.add(errMessage);
			}
		}
	}
	check = javadocOptionIDs.entrySet().iterator();
	while (check.hasNext()) {
		Map.Entry entry = (Map.Entry) check.next();
		key = (String) entry.getKey();
		value = (String) entry.getValue();
		if (!value.equals(realOptionIDs.get(key))) {
			expected = value;
			actual = realOptionIDs.get(key);
			String errMessage = "option ID mismatch, javadoc " + expected + ", real " + actual;
			System.out.println(errMessage);
			errors.add(errMessage);
		}
	}
	// checking default values
	check = realDefaultValues.entrySet().iterator();
	while (check.hasNext()) {
		Map.Entry entry = (Map.Entry) check.next();
		key = (String) entry.getKey();
		value = (String) entry.getValue();
		String name = realOptionNames.get(key);
		if (name != null && deprecatedFields.get(name) == null) {
			if (!value.equals(javadocDefaultValues.get(key)) &&
					!"org.eclipse.jdt.core.encoding".equals(key)) {
				expected = value;
				actual = (String) javadocDefaultValues.get(key);
				String errMessage = "default value mismatch for " + key + ", real: " + expected + ", javadoc: " + actual;
				System.out.println(errMessage);
				errors.add(errMessage);
			}
		}
	}
	check = javadocDefaultValues.entrySet().iterator();
	while (check.hasNext()) {
		Map.Entry entry = (Map.Entry) check.next();
		key = (String) entry.getKey();
		value = (String) entry.getValue();
		if (realOptionNames.containsKey(key) && !value.equals(realDefaultValues.get(key)) &&
				!"org.eclipse.jdt.core.compiler.problem.booleanMethodThrowingException".equals(key)) { // will remove once bug 216571 is fixed
			expected = value;
			actual = realDefaultValues.get(key);
			String errMessage = "default value mismatch for " + key + ", javadoc " + expected +	", real " + actual;
			System.out.println(errMessage);
			errors.add(errMessage);
		}
	}
	assertEquals("JavaCore options discrepancies found", List.of().toString(), errors.toString());
}

@Override
protected void tearDown() throws Exception {
	JavaCore.setOptions(JavaCore.getDefaultOptions());
	super.tearDown();
}

private char[] getJavaCoreClassSource() throws IOException, ZipException {
	// fetch JavaCore source file
	// 1. attempt: workspace relative location in project org.eclipse.jdt.core:
	Bundle bundle = org.eclipse.jdt.core.tests.Activator.getInstance().getBundle();
	URL url = bundle.getEntry("/");
	IPath path = new Path(FileLocator.toFileURL(url).getPath());
	path = path.removeLastSegments(1).append(ORG_ECLIPSE_JDT_CORE);
	String stringPath = path.toString() + "/model/" + PATH_JAVA_CORE_JAVA;
	File javaCoreSourceFile = new File(stringPath);
	char[] sourceChars = null;
	if (javaCoreSourceFile.exists()) {
		sourceChars = Util.getFileCharContent(javaCoreSourceFile, null);
	} else {
		// 2. attempt: locate org.eclipse.jdt.core.source jar next to org.eclipse.jdt.core jar:
		String location = Platform.getBundle(ORG_ECLIPSE_JDT_CORE).getLocation();
		if (location.startsWith(REFERENCE_FILE_SCHEMA)) {
			location = location.substring(REFERENCE_FILE_SCHEMA.length());
		}
		location = location.replace(ORG_ECLIPSE_JDT_CORE, ORG_ECLIPSE_JDT_CORE_SOURCE);
		if (location.endsWith(".jar")) {
			File jarFile = new File(location);
			try (ZipFile zipFile = new ZipFile(jarFile)) {
				ZipEntry entry = zipFile.getEntry(PATH_JAVA_CORE_JAVA);
				try (InputStream inputStream = zipFile.getInputStream(entry)) {
					sourceChars = Util.getInputStreamAsCharArray(inputStream, null);
				}
			}
		}
	}
	return sourceChars;
}
// see https://bugs.eclipse.org/482991
// check that tests are run with -Djdt.default.test.compliance=1.8 in effect
public void testTestDefaultCompliance() {
	Hashtable<String,String> defaultValues = JavaCore.getDefaultOptions();
	assertEquals("Tests should run at default compliance 1.8", "1.8", defaultValues.get(JavaCore.COMPILER_COMPLIANCE));
	assertEquals("Tests should run at default source level 1.8", "1.8", defaultValues.get(JavaCore.COMPILER_SOURCE));
	assertEquals("Tests should run at default target level 1.8", "1.8", defaultValues.get(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM));
}
}
