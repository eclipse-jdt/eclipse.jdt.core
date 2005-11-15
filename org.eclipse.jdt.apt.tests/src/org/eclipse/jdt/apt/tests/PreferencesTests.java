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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.IJavaProject;

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
		AptConfig.setEnabled(null, true);
		assertFalse(AptConfig.isEnabled(jproj));
		assertTrue(AptConfig.isEnabled(null));
		AptConfig.setEnabled(null, true);
		AptConfig.setEnabled(jproj, false);
		assertFalse(AptConfig.isEnabled(jproj));
		assertTrue(AptConfig.isEnabled(null));
		
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
		AptConfig.removeProcessorOption(jproj, "anOptionThatDoesn'tExist");
		
		AptConfig.addProcessorOption(null, "workspace option", "corresponding value");
		AptConfig.addProcessorOption(null, "foo", "whatever");
		AptConfig.removeProcessorOption(null, "foo");
		options = AptConfig.getProcessorOptions(null);
		assertFalse(options.containsKey("foo"));
		assertTrue(options.containsKey("workspace option"));
		AptConfig.removeProcessorOption(null, "workspace option");
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
