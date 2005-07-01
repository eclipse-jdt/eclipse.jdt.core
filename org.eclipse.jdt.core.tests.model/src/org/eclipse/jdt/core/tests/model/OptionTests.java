/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.osgi.service.prefs.BackingStoreException;

public class OptionTests extends ModifyingResourceTests {
	
	int eventCount = 0;
	
	class TestPropertyListener implements IEclipsePreferences.IPreferenceChangeListener {
		public void preferenceChange(PreferenceChangeEvent event) {
			eventCount++;
		}
	}
	
	public OptionTests(String name) {
		super(name);
	}
	static {
//		TESTS_NUMBERS = new int[] { 4 };
//		TESTS_RANGE = new int[] { 4, -1 };
	}
	public static Test suite() {
		return buildTestSuite(OptionTests.class);	
	}
	
	protected void tearDown() throws Exception {
		// Put back default options
		JavaCore.setOptions(JavaCore.getDefaultOptions());

		super.tearDown();
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
					"");
			IJavaProject projectB = 
				this.createJavaProject(
					"B", 
					new String[] {}, // source folders
					new String[] {}, // lib folders
					new String[] {}, // projects
					"");
					
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
					"");
			IJavaProject projectB = 
				this.createJavaProject(
					"B", 
					new String[] {}, // source folders
					new String[] {}, // lib folders
					new String[] {}, // projects
					"");
					
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
					"");
			IJavaProject projectB = 
				this.createJavaProject(
					"B", 
					new String[] {}, // source folders
					new String[] {}, // lib folders
					new String[] {}, // projects
					"");
					
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
					"");
			IJavaProject projectB = 
				this.createJavaProject(
					"B", 
					new String[] {}, // source folders
					new String[] {}, // lib folders
					new String[] {}, // projects
					"");
					
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
					"");
			IJavaProject projectB = 
				this.createJavaProject(
					"B", 
					new String[] {}, // source folders
					new String[] {}, // lib folders
					new String[] {}, // projects
					"");
					
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
					"");
			IJavaProject projectB = 
				this.createJavaProject(
					"B", 
					new String[] {}, // source folders
					new String[] {}, // lib folders
					new String[] {}, // projects
					"");
					
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
					"");
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
			assertTrue("projA:preferences property listener has been lost", eventCount == 2);
		
			// change custom options to have one less
			options.clear();
			options.put(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE, JavaCore.ENABLED);
			projectA.setOptions(options);
			assertEquals("projA:unexpected custom value for deprecation option", JavaCore.ENABLED, projectA.getOptions(false).get(JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE));
			assertEquals("projA:unexpected custom value for compliance option", null, projectA.getOptions(false).get(JavaCore.COMPILER_COMPLIANCE));
			assertEquals("projA:unexpected inherited value1 for hidden-catch option", null, projectA.getOptions(false).get(JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK));
	//		assertTrue("projA:preferences should not be reset", preferences == projectA.getPreferences());
			assertTrue("projA:preferences should not be reset", eclipsePreferences == projectA.getEclipsePreferences());
			assertTrue("projA:preferences property listener has been lost", eventCount == 3);
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
					"");
	
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
			JavaProject projectA = (JavaProject) this.createJavaProject("A", new String[] {}, "");
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
			assertEquals("projA:preferences property listener has been lost", 2, eventCount);
		
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

	/**
	 * Test fix for bug 68993: [Preferences] IAE when opening project preferences
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68993">68993</a>
	 */
	public void testBug68993() throws CoreException, BackingStoreException {
		try {
			JavaProject projectA = (JavaProject) this.createJavaProject(
				"A", 
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {}, // projects
				"");

			// Store project eclipse prefs
			IEclipsePreferences eclipsePreferences = projectA.getEclipsePreferences();

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
			assertFalse("projA: Preferences should have been reset", eclipsePreferences == projectA.getEclipsePreferences());
			assertEquals("projA: We should not have any custom options!", 0, projectA.getEclipsePreferences().keys().length);
		} finally {
			this.deleteProject("A");
		}
	}

	/**
	 * Test fix for bug 72214: [Preferences] IAE when opening project preferences
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=72214">72214</a>
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
}
