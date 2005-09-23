/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

public class PreferencesTests extends Tests {
	
	public PreferencesTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( PreferencesTests.class );
	}

	public void setUp() throws Exception {
		super.setUp();
		
		// project will be deleted by super-class's tearDown() method
		IPath projectPath = env.addProject( getProjectName(), "1.5" ); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$

		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		TestUtil.createAndAddAnnotationJar( env
			.getJavaProject( projectPath ) );
	}
	
	public static String getProjectName() {
		return PreferencesTests.class.getName() + "Project"; //$NON-NLS-1$
	}

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testFactoryPathEncodingAndDecoding() throws Exception {
		//encode
		Map<FactoryContainer, FactoryPath.Attributes> factories = new LinkedHashMap<FactoryContainer, FactoryPath.Attributes>();
		FactoryContainer jarFactory1 = FactoryPathUtil.newExtJarFactoryContainer(new File("C:/test1.jar")); //$NON-NLS-1$
		FactoryPath.Attributes jarFPA1 = new FactoryPath.Attributes(true, false);
		FactoryContainer jarFactory2 = FactoryPathUtil.newExtJarFactoryContainer(new File("C:/test2.jar")); //$NON-NLS-1$
		FactoryPath.Attributes jarFPA2 = new FactoryPath.Attributes(true, true);
		FactoryContainer pluginFactory = FactoryPathUtil.getPluginFactoryContainer("org.eclipse.jdt.apt.tests"); //$NON-NLS-1$
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
	
	@SuppressWarnings("nls")
	private static final String serializedFactories = 
		"<factorypath>\n" + 
		"    <factorypathentry kind=\"EXTJAR\" id=\"C:\\test1.jar\" enabled=\"true\" runInBatchMode=\"false\"/>\n" + 
		"    <factorypathentry kind=\"EXTJAR\" id=\"C:\\test2.jar\" enabled=\"true\" runInBatchMode=\"true\"/>\n" + 
		"    <factorypathentry kind=\"PLUGIN\" id=\"org.eclipse.jdt.apt.tests\" enabled=\"false\" runInBatchMode=\"false\"/>\n" + 
		"</factorypath>\n";
	
	/**
	 * Test the config API for settings other than factory path
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testSimpleConfigApi() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		
		// aptEnabled
		AptConfig.setEnabled(jproj, false);
		assertFalse(AptConfig.isEnabled(jproj));
		AptConfig.setEnabled(jproj, true);
		assertTrue(AptConfig.isEnabled(jproj));
		
		// processorOptions
		AptConfig.addProcessorOption(jproj, "foo", "bar");
		AptConfig.addProcessorOption(jproj, "space", "\"text with spaces\"");
		AptConfig.addProcessorOption(jproj, "quux", null);
		AptConfig.addProcessorOption(jproj, "quux", null); // adding twice should have no effect
		AptConfig.addProcessorOption(jproj, "", null); // should gracefully do nothing
		AptConfig.addProcessorOption(jproj, null, "spud"); // should gracefully do nothing
		Map<String, String> options = AptConfig.getProcessorOptions(jproj);
		String val = options.get("foo");
		assertEquals(val, "bar");
		val = options.get("quux");
		assertNull(val);
		val = options.get("space");
		assertEquals(val, "\"text with spaces\"");
		AptConfig.removeProcessorOption(jproj, "foo");
		options = AptConfig.getProcessorOptions(jproj);
		assertFalse(options.containsKey("foo"));
		assertTrue(options.containsKey("quux"));
		AptConfig.removeProcessorOption(jproj, "quux");
		AptConfig.removeProcessorOption(jproj, null);
		AptConfig.removeProcessorOption(jproj, "");
		AptConfig.removeProcessorOption(jproj, "anOptionThatDoesn'tExist");
	}
	
	/**
	 * Test the config API for automatically generated options.
	 */
	@SuppressWarnings("nls")
	public void testAutomaticOptions() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		Map<String,String> options = AptConfig.getProcessorOptions(jproj);
		
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
		assertEquals(AptPreferenceConstants.DEFAULT_GENERATED_SOURCE_FOLDER_NAME, genSrcDir);
		
		final String newDir = "gen/src";
		AptConfig.setGenSrcDir(jproj, newDir);
		genSrcDir = AptConfig.getGenSrcDir(jproj);
		
		assertEquals(newDir, genSrcDir);
		
	}
}
