/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.FactoryPluginManager;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class PreferencesTests extends APTTestBase {

	public PreferencesTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( PreferencesTests.class );
	}

	public void testFactoryPathEncodingAndDecoding() throws Exception {
		//encode
		Map<FactoryContainer, FactoryPath.Attributes> factories = new LinkedHashMap<FactoryContainer, FactoryPath.Attributes>();
		FactoryContainer jarFactory1 = FactoryPathUtil.newExtJarFactoryContainer(new File(JAR_PATH_1)); //$NON-NLS-1$
		FactoryPath.Attributes jarFPA1 = new FactoryPath.Attributes(true, false);
		FactoryContainer jarFactory2 = FactoryPathUtil.newExtJarFactoryContainer(new File(JAR_PATH_2)); //$NON-NLS-1$
		FactoryPath.Attributes jarFPA2 = new FactoryPath.Attributes(true, true);
		FactoryContainer pluginFactory = FactoryPluginManager.getPluginFactoryContainer("org.eclipse.jdt.apt.tests"); //$NON-NLS-1$
		FactoryPath.Attributes pluginFPA = new FactoryPath.Attributes(false, false);
		factories.put(jarFactory1, jarFPA1);
		factories.put(jarFactory2, jarFPA2);
		factories.put(pluginFactory, pluginFPA);
		String xml = FactoryPathUtil.encodeFactoryPath(factories);
		assertEquals(serializedFactories, xml);

		// decode
		factories = FactoryPathUtil.decodeFactoryPath(xml);
		assertEquals(3, factories.size());

		int index=0;
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : factories.entrySet()) {
			FactoryContainer container = entry.getKey();
			switch (index) {
			case 0:
				// jar1
				assertEquals(FactoryType.EXTJAR, container.getType());
				assertTrue(entry.getValue().isEnabled());
				assertFalse(entry.getValue().runInBatchMode());
				break;
			case 1:
				// jar2
				assertEquals(FactoryType.EXTJAR, container.getType());
				assertTrue(entry.getValue().isEnabled());
				assertTrue(entry.getValue().runInBatchMode());
				break;
			case 2:
				// plugin
				assertEquals(FactoryType.PLUGIN, container.getType());
				assertFalse(entry.getValue().isEnabled());
				assertEquals("org.eclipse.jdt.apt.tests", container.getId()); //$NON-NLS-1$
				break;
			default:
				fail("FactoryPath had an unexpected number of entries: " + (index + 1));
			}

			index++;
		}
	}

	// Need to use temp files to get path to external jars.
	// Platform differences prevent us from hard-coding a string here
	private static final String JAR_PATH_1;
	private static final String JAR_PATH_2;

	static {
		File jar1 = null;
		File jar2 = null;
		try {
			jar1 = File.createTempFile("test1", "jar");
			jar2 = File.createTempFile("test2", "jar");
			JAR_PATH_1 = jar1.getAbsolutePath();
			JAR_PATH_2 = jar2.getAbsolutePath();
		}
		catch (IOException ioe) {
			throw new RuntimeException("Could not create temp jar files", ioe);
		}
		finally {
			if (jar1 != null) jar1.delete();
			if (jar2 != null) jar2.delete();
		}
	}

	private static final String serializedFactories =
		"<factorypath>\n" +
		"    <factorypathentry kind=\"EXTJAR\" id=\"" + JAR_PATH_1 + "\" enabled=\"true\" runInBatchMode=\"false\"/>\n" +
		"    <factorypathentry kind=\"EXTJAR\" id=\"" + JAR_PATH_2 + "\" enabled=\"true\" runInBatchMode=\"true\"/>\n" +
		"    <factorypathentry kind=\"PLUGIN\" id=\"org.eclipse.jdt.apt.tests\" enabled=\"false\" runInBatchMode=\"false\"/>\n" +
		"</factorypath>\n";

	/**
	 * Test the config API for settings other than factory path
	 * @throws Exception
	 */
	public void testSimpleConfigApi() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );

		// aptEnabled
		AptConfig.setEnabled(jproj, false);
		assertFalse(AptConfig.isEnabled(jproj));
		assertFalse(AptConfig.isEnabled(null));
		AptConfig.setEnabled(jproj, true);
		assertTrue(AptConfig.isEnabled(jproj));
		assertFalse(AptConfig.isEnabled(null));

		// processorOptions
		Map<String, String> wkspOpts = new HashMap<String, String>(3);
		wkspOpts.put("b", "bVal");
		wkspOpts.put("another option", "and\\more \"punctuation!\"");
		AptConfig.setProcessorOptions(wkspOpts, null);
		Map<String, String> retrievedWkspOpts = AptConfig.getRawProcessorOptions(null);
		assertTrue("getRawProcessorOptions() should return the values set in setProcessorOptions()",
				wkspOpts.equals(retrievedWkspOpts));

		Map<String, String> projOpts = new HashMap<String, String>(3);
		projOpts.put("a", "aVal");
		projOpts.put("with spaces", "value also has spaces");
		projOpts.put("foo", "bar");
		AptConfig.setProcessorOptions(projOpts, jproj);
		Map<String, String> retrievedProjOpts = AptConfig.getRawProcessorOptions(jproj);
		assertTrue("getRawProcessorOptions() should return the values set in setProcessorOptions()",
				projOpts.equals(retrievedProjOpts));

		wkspOpts.clear();
		wkspOpts.put("noodle", "nubble");
		wkspOpts.put("spoo/mack", "wumpus");
		AptConfig.setProcessorOptions(wkspOpts, null);
		retrievedWkspOpts = AptConfig.getRawProcessorOptions(null);
		assertTrue("getRawProcessorOptions() should return the values set in setProcessorOptions()",
				wkspOpts.equals(retrievedWkspOpts));

		projOpts.clear();
		projOpts.put("smurf", "more smurfs\\=bad");
		projOpts.put("baz/quack", "quux");
		AptConfig.setProcessorOptions(projOpts, jproj);
		retrievedProjOpts = AptConfig.getRawProcessorOptions(jproj);
		assertTrue("getRawProcessorOptions() should return the values set in setProcessorOptions()",
				projOpts.equals(retrievedProjOpts));

		AptConfig.addProcessorOption(jproj, "foo", "bar");
		AptConfig.addProcessorOption(jproj, "space", "\"text with spaces\"");
		AptConfig.addProcessorOption(jproj, "quux", null);
		AptConfig.addProcessorOption(jproj, "quux", null); // adding twice should have no effect
		Map<String, String> options = AptConfig.getProcessorOptions(jproj, false);
		String val = options.get("foo");
		assertEquals(val, "bar");
		val = options.get("quux");
		assertNull(val);
		val = options.get("space");
		assertEquals(val, "\"text with spaces\"");
		AptConfig.removeProcessorOption(jproj, "foo");
		options = AptConfig.getProcessorOptions(jproj, false);
		assertFalse(options.containsKey("foo"));
		assertTrue(options.containsKey("quux"));
		AptConfig.removeProcessorOption(jproj, "quux");
		AptConfig.removeProcessorOption(jproj, "anOptionThatDoesn'tExist");

		AptConfig.addProcessorOption(null, "workspace option", "corresponding value");
		AptConfig.addProcessorOption(null, "foo", "whatever");
		AptConfig.removeProcessorOption(null, "foo");
		options = AptConfig.getProcessorOptions(null, false);
		assertFalse(options.containsKey("foo"));
		assertTrue(options.containsKey("workspace option"));
		AptConfig.removeProcessorOption(null, "workspace option");
	}

	/**
	 * Test the config API for automatically generated options.
	 */
	public void testAutomaticOptions() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		Map<String,String> options = AptConfig.getProcessorOptions(jproj, false);

		String classpath = options.get("-classpath");
		assertNotNull(classpath);
		assertTrue(classpath.length() > 0);

		String sourcepath = options.get("-sourcepath");
		assertNotNull(sourcepath);
		assertTrue(sourcepath.length() > 0);

		String target = options.get("-target");
		assertEquals(target, "1.5");

		String source = options.get("-source");
		assertEquals(source, "1.5");

		String bindir = options.get("-d");
		assertNotNull(bindir);
		assertTrue(bindir.length() > 0);

		String gensrcdirAuto = options.get("-s");
		assertNotNull(gensrcdirAuto);
		assertTrue(gensrcdirAuto.length() > 0);
	}

	public void testGenSrcDir() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		String genSrcDir = AptConfig.getGenSrcDir(jproj);
		String genTestSrcDir = AptConfig.getGenTestSrcDir(jproj);
		assertEquals(AptPreferenceConstants.DEFAULT_GENERATED_SOURCE_FOLDER_NAME, genSrcDir);
		assertEquals(AptPreferenceConstants.DEFAULT_GENERATED_TEST_SOURCE_FOLDER_NAME, genTestSrcDir);

		final String newDir = "gen/src";
		final String newTestDir = "gen/src-tests";
		AptConfig.setGenSrcDir(jproj, newDir);
		AptConfig.setGenTestSrcDir(jproj, newTestDir);
		genSrcDir = AptConfig.getGenSrcDir(jproj);
		genTestSrcDir = AptConfig.getGenTestSrcDir(jproj);

		assertEquals(newDir, genSrcDir);
		assertEquals(newTestDir, genTestSrcDir);

	}

	/**
	 * Test a series of configuration and make sure the GeneratedFileManager and
	 * the classpath reflecting the setup. Configuration setting includes
	 * enabling and disabling apt and configure the generated source folder
	 * with and without apt enabled.
	 *
	 * See comments in method body for detail testing scenarios
	 * @throws Exception
	 */
	public void testConfigGenSrcDir() throws Exception {

		final String projectName = "ConfigTestProj";
		IPath projectPath = env.addProject( projectName, "1.5" );
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		final IJavaProject javaProj = env.getJavaProject(projectName);
		// APT is not enabled
		boolean aptEnabled = AptConfig.isEnabled(javaProj);
		// test 1: make sure apt is disabled by default
		assertEquals(false, aptEnabled);
		final GeneratedSourceFolderManager gsfm = AptPlugin.getAptProject(javaProj).getGeneratedSourceFolderManager(false);
		final GeneratedSourceFolderManager testgsfm = AptPlugin.getAptProject(javaProj).getGeneratedSourceFolderManager(true);
		IFolder srcFolder = gsfm.getFolder();
		IFolder testSrcFolder = testgsfm.getFolder();
		String folderName = srcFolder.getProjectRelativePath().toOSString();
		String testFolderName = testSrcFolder.getProjectRelativePath().toOSString();
		// test 2: apt is disabled, then folder should not exists
		assertEquals(srcFolder.exists(), false);
		assertEquals(testSrcFolder.exists(), false);

		// test 3: folder name has not been configured, then it should be the default.
		// folder name should be the default name.
		assertEquals(folderName, AptPreferenceConstants.DEFAULT_GENERATED_SOURCE_FOLDER_NAME);
		assertEquals(testFolderName, AptPreferenceConstants.DEFAULT_GENERATED_TEST_SOURCE_FOLDER_NAME);

		// set folder name while apt is disabled
		String newName = ".gensrcdir";
		String newTestName = ".gentestsrcdir";

		AptConfig.setGenSrcDir(javaProj, newName);
		AptConfig.setGenTestSrcDir(javaProj, newTestName);

		srcFolder = gsfm.getFolder();
		testSrcFolder = testgsfm.getFolder();

		folderName = srcFolder.getProjectRelativePath().toOSString();
		testFolderName = testSrcFolder.getProjectRelativePath().toOSString();

		// test 4: apt still disabled but folder name changed, make sure the folder is not on disk.
		assertEquals(false, srcFolder.exists());
		assertEquals(false, testSrcFolder.exists());

		// test 5: make sure we got the new name
		assertEquals(newName, folderName);
		assertEquals(newTestName, testFolderName);

		// test 6: make sure the source folder is not on the classpath.
		assertEquals( false, isOnClasspath(javaProj, srcFolder.getFullPath()) );
		assertEquals( false, isOnClasspath(javaProj, testSrcFolder.getFullPath()) );

		// enable apt
		AptConfig.setEnabled(javaProj, true);
		aptEnabled = AptConfig.isEnabled(javaProj);
		// test 7: make sure it's enabled after we called the API to enable it.
		assertEquals(true, aptEnabled);
		srcFolder = gsfm.getFolder();
		testSrcFolder = testgsfm.getFolder();

		folderName = srcFolder.getProjectRelativePath().toOSString();
		testFolderName = testSrcFolder.getProjectRelativePath().toOSString();

		// test 8: apt enabled, the source folder should be on disk
		assertEquals(true, srcFolder.exists());
		// generated test source folder should NOT exist, as the project has no test source folder
		assertEquals(false, testSrcFolder.exists());

		// test 9: make sure the name matches
		assertEquals(newName, folderName);
		assertEquals(newTestName, testFolderName);

		// test 10: apt is enabled, folder must be on classpath.
		assertEquals( true, isOnClasspath(javaProj, srcFolder.getFullPath()) );
		// generated test source folder should NOT be on classpath, as the project has no test source folder
		assertEquals( false, isOnClasspath(javaProj, testSrcFolder.getFullPath()) );

		// test 11: now add a test source folder, generated test source folder should then exist and be on classpath
		env.removePackageFragmentRoot(projectPath, "");
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addPackageFragmentRoot( projectPath, "src" );
		env.addPackageFragmentRoot(javaProj.getPath(), "src-tests", null, null,
				"bin-tests", true);
		fullBuild( javaProj.getProject().getFullPath() );
		assertEquals(true, testSrcFolder.exists());
		assertEquals(true, isOnClasspath(javaProj, testSrcFolder.getFullPath()) );


		// now disable apt.
		AptConfig.setEnabled(javaProj, false);
		aptEnabled = AptConfig.isEnabled(javaProj);
		// test 11: make sure it's disabled.
		assertEquals(false, aptEnabled);

		srcFolder = gsfm.getFolder();
		testSrcFolder = testgsfm.getFolder();

		folderName = srcFolder.getProjectRelativePath().toOSString();
		testFolderName = testSrcFolder.getProjectRelativePath().toOSString();
		// test 12: make sure we deleted the source folder when we disable apt
		assertEquals(false, srcFolder.exists());
		assertEquals("testSrcFolder '"+testSrcFolder+"' does still exist",false, testSrcFolder.exists());
		// test 13: make sure we didn't overwrite the configure folder name
		assertEquals(newName, folderName);
		assertEquals(newTestName, testFolderName);
		// test 14: make sure we cleaned up the classpath.
		assertEquals( false, isOnClasspath(javaProj, srcFolder.getFullPath()) );
		assertEquals( false, isOnClasspath(javaProj, testSrcFolder.getFullPath()) );
	}

	private boolean isOnClasspath(IJavaProject javaProj, IPath path)
			throws JavaModelException
	{
		final IClasspathEntry[] cp = javaProj.getRawClasspath();
		for (int i = 0; i < cp.length; i++)
		{
			if (cp[i].getPath().equals( path ))
			{
				return true;
			}
		}
		return false;
	}
}
