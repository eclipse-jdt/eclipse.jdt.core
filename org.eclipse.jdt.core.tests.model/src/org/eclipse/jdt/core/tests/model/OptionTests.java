/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for Bug 346010 - [model] strange initialization dependency in OptionTests
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class OptionTests extends ModifyingResourceTests {

	int eventCount = 0;

class TestPropertyListener implements IEclipsePreferences.IPreferenceChangeListener {
	public void preferenceChange(PreferenceChangeEvent event) {
		OptionTests.this.eventCount++;
	}
}

public OptionTests(String name) {
	super(name);
}
static {
//	TESTS_NAMES = new String[] {    "testBug346010" };
//		TESTS_NUMBERS = new int[] { 125360 };
//		TESTS_RANGE = new int[] { 4, -1 };
}
public static Test suite() {
	return buildModelTestSuite(OptionTests.class);
}

@Override
protected void tearDown() throws Exception {
	// Put back default options
	JavaCore.setOptions(JavaCore.getDefaultOptions());

	super.tearDown();
}

@Override
public void tearDownSuite() throws Exception {
	// We have to reset classpath variables because we've modified defaults
	// in getExternalJCLPathString() below
	Util.cleanupClassPathVariablesAndContainers();
	super.tearDownSuite();
}

/**
 * Test persistence of project custom options
 */
public void test01() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, true));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", JavaCore.ERROR, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, true));

		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", JavaCore.DISABLED, projectB.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, true));
		assertEquals("projB:unexpected custom value for compliance option", "8.0", projectB.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		assertEquals("projB:unexpected inherited value for hidden-catch option", JavaCore.ERROR, projectB.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, true));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null);
		assertEquals("projA:unexpected reverted value for deprecation option", JavaCore.DISABLED, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, true));
		assertEquals("projA:unexpected reverted value for compliance option", "8.0", projectA.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", JavaCore.ERROR, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, true));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom encoding
 */
public void test02() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		String globalEncoding = JavaCore.getOption(JavaCore.CORE_ENCODING);

		Hashtable options = new Hashtable();
		options.put(JavaCore.CORE_ENCODING, "custom");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom encoding", "custom", projectA.getOption(JavaCore.CORE_ENCODING, true));

		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom encoding", globalEncoding, projectB.getOption(JavaCore.CORE_ENCODING, true));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null);
		assertEquals("projA:unexpected reverted encoding", globalEncoding, projectA.getOption(JavaCore.CORE_ENCODING, true));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom project option (if not considering JavaCore options)
 */
public void test03() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, false));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOption(JavaCore.COMPILER_COMPLIANCE, false));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, false));

		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", null, projectB.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, false));
		assertEquals("projB:unexpected custom value for compliance option", null, projectB.getOption(JavaCore.COMPILER_COMPLIANCE, false));
		assertEquals("projB:unexpected inherited value for hidden-catch option", null, projectB.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, false));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null);
		assertEquals("projA:unexpected reverted value for deprecation option", null, projectA.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, false));
		assertEquals("projA:unexpected reverted value for compliance option", null, projectA.getOption(JavaCore.COMPILER_COMPLIANCE, false));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", null, projectA.getOption(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, false));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}
/**
 * Test persistence of project custom options - using getOptions()
 */
public void test04() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", JavaCore.ERROR, projectA.getOptions(true).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", JavaCore.DISABLED, projectB.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projB:unexpected custom value for compliance option", "8.0", projectB.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projB:unexpected inherited value for hidden-catch option", JavaCore.ERROR, projectB.getOptions(true).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null);
		assertEquals("projA:unexpected reverted value for deprecation option", JavaCore.DISABLED, projectA.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected reverted value for compliance option", "8.0", projectA.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", JavaCore.ERROR, projectA.getOptions(true).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom encoding - using getOptions()
 */
public void test05() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		String globalEncoding = JavaCore.getOption(JavaCore.CORE_ENCODING);

		Hashtable options = new Hashtable();
		options.put(JavaCore.CORE_ENCODING, "custom");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom encoding", "custom", projectA.getOptions(true).get(JavaCore.CORE_ENCODING));

		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom encoding", globalEncoding, projectB.getOptions(true).get(JavaCore.CORE_ENCODING));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null);
		assertEquals("projA:unexpected reverted encoding", globalEncoding, projectA.getOptions(true).get(JavaCore.CORE_ENCODING));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}

/**
 * Test custom project option (if not considering JavaCore options) - using getOptions()
 */
public void test06() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "8.0");
		options.put(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK, JavaCore.ERROR);
		JavaCore.setOptions(options);

		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

		// check project B custom options	(should be none, indicating it sees global ones only)
		assertEquals("projB:unexpected custom value for deprecation option", null, projectB.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projB:unexpected custom value for compliance option", null, projectB.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projB:unexpected inherited value for hidden-catch option", null, projectB.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

		// flush custom options - project A should revert to global ones
		projectA.setOptions(null);
		assertEquals("projA:unexpected reverted value for deprecation option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected reverted value for compliance option", null, projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value2 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));

	} finally {
		this.deleteProject("A");
		this.deleteProject("B");
	}
}
/**
 * Custom options must replace existing ones completely without loosing property listeners
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26255
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=49691
 */
public void test07() throws CoreException {
	try {
		this.eventCount = 0;
		JavaProject projectA = (JavaProject)
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);
//		Preferences preferences = projectA.getPreferences();
//		preferences.addPropertyChangeListener(new TestPropertyListener());
		IEclipsePreferences eclipsePreferences = projectA.getEclipsePreferences();
		TestPropertyListener listener = new TestPropertyListener();
		eclipsePreferences.addPreferenceChangeListener(listener);

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
//		assertTrue("projA:preferences should not be reset", preferences == projectA.getPreferences());
		assertTrue("projA:preferences should not be reset", eclipsePreferences == projectA.getEclipsePreferences());
		assertTrue("projA:preferences property listener has been lost", this.eventCount == 2);

		// change custom options to have one less
		options.clear();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		projectA.setOptions(options);
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", null, projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
		assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
//		assertTrue("projA:preferences should not be reset", preferences == projectA.getPreferences());
		assertTrue("projA:preferences should not be reset", eclipsePreferences == projectA.getEclipsePreferences());
		assertTrue("projA:preferences property listener has been lost", this.eventCount == 3);
	} finally {
		this.deleteProject("A");
	}
}
/**
 * Empty custom option must not be ignored
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26251
 */
public void test08() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"",
				"" /* no compliance to force defaults */);

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_TASK_TAGS, "TODO:");
		JavaCore.setOptions(options);


		// check project A custom options
		assertEquals("1#projA:unexpected custom value for task tags option", null, projectA.getOption(JavaCore.COMPILER_TASK_TAGS, false));
		assertEquals("1#projA:unexpected custom value for inherited task tags option", "TODO:", projectA.getOption(JavaCore.COMPILER_TASK_TAGS, true));
		assertEquals("1#workspace:unexpected custom value for task tags option", "TODO:", JavaCore.getOption(JavaCore.COMPILER_TASK_TAGS));

		// change custom options to have one less
		options.clear();
		options.put(JavaCore.COMPILER_TASK_TAGS, "");
		projectA.setOptions(options);
		assertEquals("2#projA:unexpected custom value for task tags option", "", projectA.getOption(JavaCore.COMPILER_TASK_TAGS, false));
		assertEquals("2#projA:unexpected custom value for inherited task tags option", "", projectA.getOption(JavaCore.COMPILER_TASK_TAGS, true));
		assertEquals("2#workspace:unexpected custom value for task tags option", "TODO:", JavaCore.getOption(JavaCore.COMPILER_TASK_TAGS));

		// change custom options to have one less
		options.clear();
		options.put(JavaCore.COMPILER_TASK_TAGS, "@TODO");
		JavaCore.setOptions(options);
		assertEquals("3#projA:unexpected custom value for task tags option", "", projectA.getOption(JavaCore.COMPILER_TASK_TAGS, false));
		assertEquals("3#projA:unexpected custom value for inherited task tags option", "", projectA.getOption(JavaCore.COMPILER_TASK_TAGS, true));
		assertEquals("3#workspace:unexpected custom value for task tags option", "@TODO", JavaCore.getOption(JavaCore.COMPILER_TASK_TAGS));

	} finally {
		this.deleteProject("A");
	}
}
/**
 * Custom options must replace existing ones completely without loosing property listeners
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=59258
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=60896
 */
public void test09() throws CoreException {
	try {
		this.eventCount = 0;
		JavaProject projectA = (JavaProject) this.createJavaProject("A", new String[] {}, new String[] {}, "",
				"" /* no compliance to force defaults */);
//		Preferences preferences = projectA.getPreferences();
//		preferences.addPropertyChangeListener(new TestPropertyListener());
		IEclipsePreferences eclipsePreferences = projectA.getEclipsePreferences();
		eclipsePreferences.addPreferenceChangeListener(new TestPropertyListener());

		Hashtable options = new Hashtable();
		options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_COMPLIANCE, "10.0");
		projectA.setOptions(options);

		// check project A custom options
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", "10.0", projectA.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertTrue("projA:preferences should not be reset", eclipsePreferences == projectA.getEclipsePreferences());
		assertEquals("projA:preferences property listener has been lost", 2, this.eventCount);

		// delete/create project A and verify that options are well reset
		this.deleteProject("A");
		projectA = (JavaProject) this.createJavaProject("A", new String[] {}, "");
		assertEquals("projA:unexpected custom value for deprecation option", JavaCore.getOption(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE), projectA.getOptions(true).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
		assertEquals("projA:unexpected custom value for compliance option", JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE), projectA.getOptions(true).get(JavaCore.COMPILER_COMPLIANCE));
		assertTrue("projA:preferences should not be reset", eclipsePreferences != projectA.getEclipsePreferences());
	} finally {
		this.deleteProject("A");
	}
}

/*
 * Ensures that a classpath variable is still in the preferences after shutdown/restart
 * (regression test for bug 98720 [preferences] classpath variables are not exported if the session is closed and restored)
 */
public void test10() throws CoreException {
	JavaCore.setClasspathVariable("TEST", new Path("testing"), null);
	simulateExitRestart();
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IEclipsePreferences preferences = manager.getInstancePreferences();
	assertEquals(
		"Should find variable TEST in preferences",
		"testing",
		preferences.get(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX+"TEST", "null"));
}

/*
 * Ensures that a classpath variable is removed from the preferences if set to null
 * (regression test for bug 98720 [preferences] classpath variables are not exported if the session is closed and restored)
 */
public void test11() throws CoreException {
	JavaCore.setClasspathVariable("TEST", new Path("testing"), null);
	JavaCore.removeClasspathVariable("TEST", null);
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IEclipsePreferences preferences = manager.getInstancePreferences();
	assertEquals(
		"Should not find variable TEST in preferences",
		"null",
		preferences.get(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX+"TEST", "null"));
}

/*
 * Ensures that classpath problems are removed when a missing classpath variable is added through the preferences
 * (regression test for bug 109691 Importing preferences does not update classpath variables)
 */
public void test12() throws Exception {
	IEclipsePreferences preferences = JavaModelManager.getJavaModelManager().getInstancePreferences();
	try {
		IJavaProject project = createJavaProject("P", new String[0], new String[] {"TEST"}, "");
		waitForAutoBuild();
		setupExternalJCL("jclMin" + CompilerOptions.getFirstSupportedJavaVersion());
		preferences.put(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX+"TEST", getExternalJCLPathString(CompilerOptions.getFirstSupportedJavaVersion()));
		assertBuildPathMarkers("Unexpected markers", "", project);
	} finally {
		deleteProject("P");
		preferences.remove(JavaModelManager.CP_VARIABLE_PREFERENCES_PREFIX+"TEST");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217443
public void test13() {
	Hashtable options = JavaCore.getDefaultOptions();
	String immutableValue = (String) options.get(JavaCore.CORE_ENCODING);
	assertEquals(ResourcesPlugin.getEncoding(), immutableValue);
	options.put(JavaCore.CORE_ENCODING, immutableValue + "_extra_tail");
	JavaCore.setOptions(options);
	assertEquals(immutableValue, JavaCore.getOptions().get(JavaCore.CORE_ENCODING));
}
/**
 * Bug 68993: [Preferences] IAE when opening project preferences
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=68993"
 */
public void testBug68993() throws CoreException, BackingStoreException {
	try {
		JavaProject projectA = (JavaProject) this.createJavaProject(
			"A",
			new String[] {}, // source folders
			new String[] {}, // lib folders
			new String[] {}, // projects
			"");

		// set all project options as custom ones: this is what happens when user select
		// "Use project settings" in project 'Java Compiler' preferences page...
		Hashtable options = new Hashtable(projectA.getOptions(true));
		projectA.setOptions(options);

		// reset all project custom options: this is what happens when user select
		// "Use workspace settings" in project 'Java Compiler' preferences page...
		options = new Hashtable();
		options.put("internal.default.compliance", JavaCore.DEFAULT);
		projectA.setOptions(options);

		// verify that project preferences have been reset
		assertEquals("projA: We should not have any custom options!", 0, projectA.getEclipsePreferences().keys().length);
	} finally {
		this.deleteProject("A");
	}
}

/**
 * Bug 72214: [Preferences] IAE when opening project preferences
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=72214"
 */
public void testBug72214() throws CoreException, BackingStoreException {
	// Remove JavaCore instance prefs
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IEclipsePreferences preferences = manager.getInstancePreferences();
	int size = JavaCore.getOptions().size();
	preferences.removeNode();

	// verify that JavaCore preferences have been reset
	assertFalse("JavaCore preferences should have been reset", preferences == manager.getInstancePreferences());
	assertEquals("JavaCore preferences should have been resotred!", size, JavaCore.getOptions().size());
}

/**
 * Bug 100393: Defaults for compiler errors/warnings settings
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=100393"
 */
public void testBug100393() throws CoreException, BackingStoreException {
	// Get default compiler options
	Map options = new CompilerOptions().getMap();

	// verify that CompilerOptions default preferences for modified options
	assertEquals("Invalid default for "+CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING, options.get(CompilerOptions.OPTION_ReportUnusedLocal));
	assertEquals("Invalid default for "+CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING, options.get(CompilerOptions.OPTION_ReportUnusedPrivateMember));
	assertEquals("Invalid default for "+CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE, options.get(CompilerOptions.OPTION_ReportFieldHiding));
	assertEquals("Invalid default for "+CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE, options.get(CompilerOptions.OPTION_ReportLocalVariableHiding));
}
public void testBug100393b() throws CoreException, BackingStoreException {
	// Get JavaCore default preferences
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IEclipsePreferences preferences = manager.getDefaultPreferences();

	// verify that JavaCore default preferences for modified options
	assertEquals("Invalid default for "+JavaCore.COMPILER_PB_UNUSED_LOCAL, "warning", preferences.get(JavaCore.COMPILER_PB_UNUSED_LOCAL, "null"));
	assertEquals("Invalid default for "+JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, "warning", preferences.get(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, "null"));
	assertEquals("Invalid default for "+JavaCore.COMPILER_PB_FIELD_HIDING, "ignore", preferences.get(JavaCore.COMPILER_PB_FIELD_HIDING, "null"));
	assertEquals("Invalid default for "+JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING, "ignore", preferences.get(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING, "null"));
}

/**
 * bug 125360: IJavaProject#setOption() doesn't work if same option as default
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=125360"
 */
public void testBug125360() throws CoreException, BackingStoreException {
	try {
		JavaProject project = (JavaProject) createJavaProject(
			"P",
			new String[] {}, // source folders
			new String[] {}, // lib folders
			new String[] {}, // projects
			"");
		project.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		String option = project.getOption(JavaCore.COMPILER_SOURCE, true);
		assertEquals(JavaCore.VERSION_1_3, option);
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 131707: Cannot add classpath variables when starting with -pluginCustomization option
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=131707"
 */
public void testBug131707() throws CoreException {
	IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
	try {
		defaultPreferences.put("org.eclipse.jdt.core.classpathVariable.MY_DEFAULT_LIB", "c:\\temp\\lib.jar");
		simulateExitRestart();
		String[] variableNames = JavaCore.getClasspathVariableNames();
		for (int i = 0, length = variableNames.length; i < length; i++) {
			if ("MY_DEFAULT_LIB".equals(variableNames[i])) {
				assertEquals(
					"Unexpected value for MY_DEFAULT_LIB",
					new Path("c:\\temp\\lib.jar"),
					JavaCore.getClasspathVariable("MY_DEFAULT_LIB"));
				return;
			}
		}
		assertFalse("Variable MY_DEFAULT_LIB not found", true);
	} finally {
		defaultPreferences.remove("org.eclipse.jdt.core.classpathVariable.MY_DEFAULT_LIB");
	}
}

/**
 * bug 152562: [prefs] IJavaProject.setOption(..., null) does not work
 * test Verify that setting an option to null removes it from project preferences
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=152562"
 */
public void testBug152562() throws CoreException {
	String wkspCompilerSource = JavaCore.getOption(JavaCore.COMPILER_SOURCE);
	String compilerSource = wkspCompilerSource.equals(JavaCore.VERSION_1_5) ? JavaCore.VERSION_1_6 : JavaCore.VERSION_1_5;
	try {
		JavaProject project = (JavaProject) createJavaProject("P");
		project.setOption(JavaCore.COMPILER_SOURCE, compilerSource);
		String option = project.getOption(JavaCore.COMPILER_SOURCE, true);
		if (!option.equals(compilerSource)) {
			System.err.println("Unexpected option value: "+option+" instead of: "+compilerSource);
		}
		project.setOption(JavaCore.COMPILER_SOURCE, null);
		option = project.getOption(JavaCore.COMPILER_SOURCE, true);
		assertEquals(wkspCompilerSource, option);
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 152578: [prefs] IJavaProject.setOption(Object,Object) wrongly removes key when value is equals to JavaCore one
 * test Verify that setting an option to workspace value does not remove it from project preferences
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=152578"
 */
public void testBug152578() throws CoreException {
	Hashtable wkspOptions = JavaCore.getOptions();
	String wkspCompilerSource = (String) wkspOptions.get(JavaCore.COMPILER_SOURCE);
	String compilerSource = wkspCompilerSource.equals(JavaCore.VERSION_1_5) ? JavaCore.VERSION_1_6 : JavaCore.VERSION_1_5;
	try {
		JavaProject project = (JavaProject) createJavaProject("P");
		project.setOption(JavaCore.COMPILER_SOURCE, wkspCompilerSource);
		String option = project.getOption(JavaCore.COMPILER_SOURCE, true);
		if (!option.equals(wkspCompilerSource)) {
			System.err.println("Unexpected option value: "+option+" instead of: "+wkspCompilerSource);
		}
		Hashtable newOptions = JavaCore.getOptions();
		newOptions.put(JavaCore.COMPILER_SOURCE, compilerSource);
		JavaCore.setOptions(newOptions);
		option = project.getOption(JavaCore.COMPILER_SOURCE, false);
		assertNotNull("Project should still have the option set!", option);
	} finally {
		deleteProject("P");
		JavaCore.setOptions(wkspOptions);
	}
}

/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a deprecated option is well preserved when a client use it
 * 		through the IJavaProject.setOption(String, String) API
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Project01() throws CoreException {
	try {
		// Set the obsolete option using the IJavaProject API
		JavaProject project = (JavaProject) createJavaProject("P");
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		project.setOption(obsoleteOption, JavaCore.DO_NOT_INSERT);
		// Verify that obsolete preference is not stored
		assertNull(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				project.getEclipsePreferences().get(obsoleteOption, null));
		// Verify that project obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.DO_NOT_INSERT,
				project.getOption(obsoleteOption, true));
	} finally {
		deleteProject("P");
	}
}
/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a new option beats the deprecated option when a client sets both
 * 		through the IJavaProject#setOptions(Map) API
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Project02() throws CoreException {
	try {
		// Set the obsolete option using the IJavaProject API
		JavaProject project = (JavaProject) createJavaProject("P");
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		Map testOptions = project.getOptions(true);
		testOptions.put(obsoleteOption, JavaCore.DO_NOT_INSERT);
		project.setOptions(testOptions);
		// Verify that obsolete preference is not stored
		assertNull(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				project.getEclipsePreferences().get(obsoleteOption, null));
		// Verify that project obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.INSERT,
				project.getOption(obsoleteOption, true));
	} finally {
		deleteProject("P");
	}
}
/**
 * bug 346010 - [model] strange initialization dependency in OptionTests
 * test Verify that unfortunate order of map entries doesn't spoil intended semantics.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=346010"
 * @deprecated As using deprecated constants
 */
public void testBug346010() throws CoreException {
	class ForcedOrderMap extends Hashtable {
		private static final long serialVersionUID = 8012963985718522218L;
		Map original;
		Map.Entry additionalEntry;
		/* Force (additionalKey,additionalValue) to be served after all entries of original. */
		public ForcedOrderMap(Map original, String additionalKey, String additionalValue) {
			this.original = original;
			// convert additionalKey->additionalValue to a Map.Entry without inserting into original:
			Hashtable tmp = new Hashtable();
			tmp.put(additionalKey, additionalValue);
			this.additionalEntry = (Map.Entry) tmp.entrySet().iterator().next();
		}
		@Override
		public Set entrySet() {
			return new HashSet() {
				private static final long serialVersionUID = 1L;
				@Override
				public Iterator iterator() {
					List orderedEntries;
					orderedEntries = new ArrayList(ForcedOrderMap.this.original.entrySet());
					orderedEntries.add(ForcedOrderMap.this.additionalEntry);
					return orderedEntries.iterator();
				}
			};
		}
		@Override
		public synchronized boolean containsKey(Object key) {
			return this.original.containsKey(key) || key.equals(this.additionalEntry.getKey());
		}
	}
	try {
		// Set the obsolete option using the IJavaProject API
		JavaProject project = (JavaProject) createJavaProject("P");
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		Map testOptions = project.getOptions(true);
		Map orderedOptions = new ForcedOrderMap(testOptions, obsoleteOption, JavaCore.DO_NOT_INSERT);
		project.setOptions(orderedOptions);
		// Verify that obsolete preference is not stored
		assertNull(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				project.getEclipsePreferences().get(obsoleteOption, null));
		// Verify that project obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.INSERT,
				project.getOption(obsoleteOption, true));
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a deprecated option is well preserved when read through
 * 		the IEclipsePreferences (i.e. simulate reading project preferences of a project
 * 		coming from an older workspace)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Project03() throws CoreException {
	try {
		// Set the obsolete preference simulating a project coming from an older version workspace
		JavaProject project = (JavaProject) createJavaProject("P");
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		project.getEclipsePreferences().put(obsoleteOption, JavaCore.DO_NOT_INSERT);
		// Verify that obsolete preference is stored
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.DO_NOT_INSERT,
				project.getEclipsePreferences().get(obsoleteOption, null));
		// Verify that project obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.DO_NOT_INSERT,
				project.getOption(obsoleteOption, true));
	} finally {
		deleteProject("P");
	}
}
/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a new option beats the deprecated option when a client sets both
 * 		through the JavaCore.setOptions(Hashtable) API
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Workspace01() throws CoreException {
	try {
		// Set the obsolete option using the JavaCore API
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		Hashtable testOptions = JavaCore.getOptions();
		testOptions.put(obsoleteOption, JavaCore.DO_NOT_INSERT);
		JavaCore.setOptions(testOptions);
		// Verify that obsolete preference is not stored
		assertNull(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaModelManager.getJavaModelManager().getInstancePreferences().get(obsoleteOption, null));
		// Verify that workspace obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.INSERT,
				JavaCore.getOption(obsoleteOption));
	} finally {
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}
}
/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a deprecated option is well preserved when read through
 * 		the IEclipsePreferences (i.e. simulate reading an older workspace)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Workspace02() throws CoreException {
	try {
		// Set the obsolete preference simulating an older version workspace
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		IEclipsePreferences instancePreferences = JavaModelManager.getJavaModelManager().getInstancePreferences();
		instancePreferences.put(obsoleteOption, JavaCore.DO_NOT_INSERT);
		// Verify that obsolete preference is stored
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.DO_NOT_INSERT,
				instancePreferences.get(obsoleteOption, null));
		// Verify that project obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.DO_NOT_INSERT,
				JavaCore.getOption(obsoleteOption));
	} finally {
		deleteProject("P");
	}
}
/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a deprecated option is well preserved when a client use it
 * 		through the JavaCore.setOptions(Hashtable) API
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Workspace03() throws CoreException {
	try {
		// Set the obsolete option using the JavaCore API
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		Hashtable testOptions = JavaCore.getOptions();
		testOptions.put(obsoleteOption, JavaCore.INSERT);
		JavaCore.setOptions(testOptions);
		// Verify that obsolete preference is not stored
		assertNull(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaModelManager.getJavaModelManager().getInstancePreferences().get(obsoleteOption, null));
		// Verify that workspace obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.INSERT,
				JavaCore.getOption(obsoleteOption));
	} finally {
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}
}
/**
 * bug 324987: [formatter] API compatibility problem with Annotation Newline options
 * test Verify that a deprecated option is well preserved when read through
 * 		the IEclipsePreferences (i.e. simulate reading an older workspace)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324987"
 * @deprecated As using deprecated constants
 */
public void testBug324987_Workspace04() throws CoreException {
	try {
		// Set the obsolete preference simulating an older version workspace
		final String obsoleteOption = DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER;
		IEclipsePreferences instancePreferences = JavaModelManager.getJavaModelManager().getInstancePreferences();
		instancePreferences.put(obsoleteOption, JavaCore.INSERT);
		// Verify that obsolete preference is stored
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.INSERT,
				instancePreferences.get(obsoleteOption, null));
		// Verify that project obsolete option is well retrieved
		assertEquals(
				"Unexpected value for formatter deprecated option 'org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member'",
				JavaCore.INSERT,
				JavaCore.getOption(obsoleteOption));
	} finally {
		deleteProject("P");
	}
}
public void testBug550081() {
	String latestVersion = JavaCore.latestSupportedJavaVersion();
	try {
		Collections.reverse(JavaCore.getAllVersions());
	} catch (UnsupportedOperationException e) {
		// ignore
	}
	assertEquals("latest should be unchanged", latestVersion, JavaCore.latestSupportedJavaVersion());
}
}
