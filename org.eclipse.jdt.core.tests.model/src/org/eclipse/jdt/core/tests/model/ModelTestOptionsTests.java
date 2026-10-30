/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.text.Document;
import org.eclipse.test.internal.performance.NullPerformanceMeter;
import org.eclipse.text.edits.TextEdit;

/** Tests the workspace option cleanup shared by Java model tests. */
public class ModelTestOptionsTests extends TestCase {

	private static final String CAST_SPACE = DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST;
	private Hashtable<String, String> savedOptions;

	public ModelTestOptionsTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ModelTestOptionsTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertTrue("These tests require an Eclipse runtime", Platform.isRunning());
		this.savedOptions = JavaCore.getOptions();
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (this.savedOptions != null) {
				JavaCore.setOptions(this.savedOptions);
			}
		} finally {
			super.tearDown();
		}
	}

	public void testFormatterOptionLeakIsReported() {
		TestResult result = runModelTest(null, () -> setOption(CAST_SPACE, JavaCore.DO_NOT_INSERT));
		assertFormatterFailure(result, CAST_SPACE);
	}

	public void testFormatterOptionLeakDoesNotAffectFollowingFormatting() throws Exception {
		runModelTest(null, () -> setOption(CAST_SPACE, JavaCore.DO_NOT_INSERT));
		assertEquals("A following test must use the restored formatter options", "(String) value", formatCast());
	}

	public void testClassFormatterDefaultsAreAccepted() throws Exception {
		Hashtable<String, String> defaults = classDefaults();
		JavaCore.setOptions(defaults);
		TestResult result = runModelTest(defaults, () -> {});
		assertEquals("Class-specific formatter defaults must be accepted", 0, result.failureCount());
		assertEquals(JavaCore.SPACE, JavaCore.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
		assertEquals("(String)value", formatCast());
	}

	public void testFormatterLeakRestoresClassDefaults() throws Exception {
		Hashtable<String, String> defaults = classDefaults();
		JavaCore.setOptions(defaults);
		TestResult result = runModelTest(defaults, () -> setOption(CAST_SPACE, JavaCore.INSERT));
		assertEquals("Restore the class baseline, not the Eclipse defaults", "(String)value", formatCast());
		assertFormatterFailure(result, CAST_SPACE);
	}

	public void testInactiveFormatterOptionIsRestored() {
		String key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
		Hashtable<String, String> defaults = JavaCore.getDefaultOptions();
		defaults.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		defaults.put(key, "4");
		JavaCore.setOptions(defaults);
		TestResult result = runModelTest(defaults, () -> setOption(key, "8"));
		// The formatter ignores indentation_size in TAB mode, but a later test may switch to MIXED.
		assertEquals("Dormant formatter values must not leak either", "4", JavaCore.getOption(key));
		assertFormatterFailure(result, key);
	}

	public void testFormatterSelectionIsRestored() {
		TestResult result = runModelTest(null, () -> setOption(JavaCore.JAVA_FORMATTER, "another.formatter"));
		assertEquals(JavaCore.DEFAULT_JAVA_FORMATTER, JavaCore.getOption(JavaCore.JAVA_FORMATTER));
		assertFormatterFailure(result, JavaCore.JAVA_FORMATTER);
	}

	@SuppressWarnings("deprecation")
	public void testOptionTestsRemoveDeprecatedFormatterPreferences() {
		String key = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		String variableKey = JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX + "MODEL_TEST_OPTIONS";
		String savedVariable = preferences.get(variableKey, null);
		try {
			preferences.put(variableKey, "/formatter-options");
			TestResult result = new TestResult();
			new OptionTests(getName()) {
				@Override
				protected void setUp() {
					this.indexDisabledForTest = false;
					this.fPerformanceMeter = new NullPerformanceMeter();
				}

				@Override
				protected void runTest() {
					preferences.put(key, JavaCore.DO_NOT_INSERT);
				}
			}.run(result);
			assertEquals("The option test must have run", 1, result.runCount());
			assertEquals("Unexpected lifecycle error", 0, result.errorCount());
			assertEquals("OptionTests must restore preferences before the shared check", 0, result.failureCount());
			assertNull("OptionTests must remove obsolete preferences", preferences.get(key, null));
			assertEquals("Option cleanup must preserve classpath variable preferences",
					"/formatter-options", preferences.get(variableKey, null));
		} finally {
			preferences.remove(key);
			if (savedVariable == null) {
				preferences.remove(variableKey);
			} else {
				preferences.put(variableKey, savedVariable);
			}
		}
	}

	public void testFormatterCleanupPreservesClasspathVariables() throws Exception {
		String variable = "MODEL_TEST_OPTIONS";
		IPath savedVariable = JavaCore.getClasspathVariable(variable);
		try {
			JavaCore.setClasspathVariable(variable, new Path("/formatter-options"), null);
			TestResult result = runModelTest(null, () -> setOption(CAST_SPACE, JavaCore.DO_NOT_INSERT));
			assertFormatterFailure(result, CAST_SPACE);
			assertEquals("Formatter recovery must preserve persisted classpath variables", "/formatter-options",
					InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID).get(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX + variable, null));
		} finally {
			if (savedVariable == null) {
				JavaCore.removeClasspathVariable(variable);
			} else {
				JavaCore.setClasspathVariable(variable, savedVariable, null);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void testDeprecatedFormatterOptionIsRemoved() {
		String key = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		assertFalse("The obsolete option has no default entry", JavaCore.getDefaultOptions().containsKey(key));
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		try {
			// Like OptionTests.testBug324987_Workspace02, simulate an older workspace.
			TestResult result = runModelTest(null, () -> preferences.put(key, JavaCore.DO_NOT_INSERT));
			assertFormatterFailure(result, key);
			assertNull("Remove obsolete preferences, not just their cached values", preferences.get(key, null));
			// Force a fresh options read through real preference events.
			preferences.put(CAST_SPACE, JavaCore.DO_NOT_INSERT);
			preferences.remove(CAST_SPACE);
			assertFalse("The obsolete option must not reappear after invalidation", JavaCore.getOptions().containsKey(key));
		} finally {
			preferences.remove(key);
		}
	}

	public void testCompilerOptionsAreStillRestored() {
		Hashtable<String, String> defaults = JavaCore.getDefaultOptions();
		defaults.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.WARNING);
		JavaCore.setOptions(defaults);
		TestResult result = runModelTest(defaults, () -> setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR));
		assertEquals("Compiler option leaks must still be reported", 1, result.failureCount());
		assertEquals(JavaCore.WARNING, JavaCore.getOption(JavaCore.COMPILER_PB_UNUSED_LOCAL));
	}

	private Hashtable<String, String> classDefaults() {
		Hashtable<String, String> defaults = JavaCore.getDefaultOptions();
		defaults.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		defaults.put(CAST_SPACE, JavaCore.DO_NOT_INSERT);
		return defaults;
	}

	private void setOption(String key, String value) {
		Hashtable<String, String> options = JavaCore.getOptions();
		options.put(key, value);
		JavaCore.setOptions(options);
	}

	private String formatCast() throws Exception {
		Document document = new Document("(String)value");
		CodeFormatter formatter = ToolFactory.createCodeFormatter(JavaCore.getOptions());
		TextEdit edit = formatter.format(CodeFormatter.K_EXPRESSION, document.get(), 0, document.getLength(), 0, "\n");
		assertNotNull("The cast must be formattable", edit);
		edit.apply(document);
		return document.get();
	}

	private void assertFormatterFailure(TestResult result, String key) {
		assertEquals("The leaking test must fail in teardown", 1, result.failureCount());
		String message = result.failures().nextElement().exceptionMessage();
		assertTrue("The failure must identify the formatter option: " + message, message.contains(key));
	}

	private TestResult runModelTest(Hashtable<String, String> defaults, Runnable body) {
		TestResult result = new TestResult();
		new AbstractJavaModelTests(getName()) {
			@Override
			protected void setUp() {
				// Only initialize what inherited teardown needs; no nested indexer or freeze monitor lifecycle.
				this.indexDisabledForTest = false;
				this.fPerformanceMeter = new NullPerformanceMeter();
			}

			@Override
			protected void runTest() {
				body.run();
			}

			@Override
			protected Hashtable<String, String> getDefaultJavaCoreOptions() {
				return defaults == null ? super.getDefaultJavaCoreOptions() : defaults;
			}
		}.run(result);
		assertEquals("The model test must have run", 1, result.runCount());
		assertEquals("Unexpected lifecycle error", 0, result.errorCount());
		return result;
	}
}
