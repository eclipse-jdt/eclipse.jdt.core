/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * The intent of this tests series is to check the consistency of parts of our
 * APIs documentation with real values and results.
 */
public class APIDocumentationTests extends AbstractASTTests {
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
 * Helper class able to analyze JavaCore options javadocs.
 */
class JavaCoreJavadocAnalyzer {
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
					if (line.startsWith(" * <dt>Option id:")) {
						this.optionID = line.substring(33, line.length() - 13);
					} else if (line.startsWith(" * <dt>Default:")) {
						this.defaultValue = line.substring(31, line.length() - 13);
						return;
					}
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
// TODO maxime: reactivate in early 3.4 M6 and refine for remote execution
public void test001() throws CoreException, IllegalArgumentException, IllegalAccessException, IOException {
	// fetch JavaCore class
	Class javaCoreClass = JavaCore.class;
	// fetch JavaCore source file
	File javaCoreSourceFile = new File(FileLocator.toFileURL(JavaCore.getJavaCore().getBundle().getEntry("/model/org/eclipse/jdt/core/JavaCore.java")).getPath());
	if (javaCoreSourceFile.exists()) {
		// load field values in a map
		Hashtable realOptionIDs = new Hashtable();
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
		// build cross-index
		Hashtable realOptionNames = new Hashtable();
		Iterator optionIDs = realOptionIDs.entrySet().iterator();
		while (optionIDs.hasNext()) {
			Map.Entry optionID = (Map.Entry) optionIDs.next();
			realOptionNames.put(optionID.getValue(), optionID.getKey());
		}

		// fetch default option values
		Hashtable realDefaultValues = JavaCore.getDefaultOptions();
		// load documented values in a map
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(javaCoreSourceFile, null));
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
		while (check.hasNext()) {
			Map.Entry entry = (Map.Entry) check.next();
			key = (String) entry.getKey();
			value = (String) entry.getValue();
			if (deprecatedFields.get(key) == null) {
				if (!value.equals(javadocOptionIDs.get(key))) {
					expected = value;
					actual = (String) javadocOptionIDs.get(key);
					System.out.println("option ID mismatch for " + key + ", real: " + expected +
						", javadoc: " + actual);
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
				actual = (String) realOptionIDs.get(key);
				System.out.println("option ID mismatch, javadoc " + expected +
					", real " + actual);
			}
		}
		// checking default values
		check = realDefaultValues.entrySet().iterator();
		while (check.hasNext()) {
			Map.Entry entry = (Map.Entry) check.next();
			key = (String) entry.getKey();
			value = (String) entry.getValue();
			String name = (String) realOptionNames.get(key);
			if (name != null && deprecatedFields.get(name) == null) {
				if (!value.equals(javadocDefaultValues.get(key)) &&
						!"org.eclipse.jdt.core.encoding".equals(key)) {
					expected = value;
					actual = (String) javadocDefaultValues.get(key);
					System.out.println("default value mismatch for " + key + ", real: " + expected +
						", javadoc: " + actual);
				}
			}
		}
		check = javadocDefaultValues.entrySet().iterator();
		while (check.hasNext()) {
			Map.Entry entry = (Map.Entry) check.next();
			key = (String) entry.getKey();
			value = (String) entry.getValue();
			if (!value.equals(realDefaultValues.get(key)) &&
					!"org.eclipse.jdt.core.compiler.problem.booleanMethodThrowingException".equals(key)) { // will remove once bug 216571 is fixed
				expected = value;
				actual = (String) realDefaultValues.get(key);
				System.out.println("default value mismatch for " + key + ", javadoc " + expected +
					", real " + actual);
			}
		}
		assertEquals("One or many discrepancies, including: ", expected, actual);
	} else {
		System.err.println("JavaCore.java not found, skipping APIDocumentationTests#test001");
	}
}
}
