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
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.JarFactoryContainer;
import org.eclipse.jdt.apt.core.internal.PluginFactoryContainer;
import org.eclipse.jdt.apt.core.internal.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.FactoryPathUtil;
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
		return PreferencesTests.class.getName() + "Project";
	}

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testFactoryPathEncodingAndDecoding() throws Exception {
		//encode
		Map<FactoryContainer, Boolean> factories = new HashMap<FactoryContainer, Boolean>();
		FactoryContainer jarFactory = new JarFactoryContainer(new File("C:/test.jar"));
		FactoryContainer pluginFactory = new PluginFactoryContainer("com.bea.ap.plugin");
		factories.put(jarFactory, true);
		factories.put(pluginFactory, false);
		String xml = FactoryPathUtil.encodeFactoryPath(factories);
		assertEquals(serializedFactories, xml);
		
		// decode
		factories = FactoryPathUtil.decodeFactoryPath(xml);
		assertEquals(2, factories.size());
		for (Map.Entry<FactoryContainer, Boolean> entry : factories.entrySet()) {
			FactoryContainer container = entry.getKey();
			if (container.getType() == FactoryType.JAR) {
				assertEquals(Boolean.TRUE, entry.getValue());
			}
			else {
				// Plugin
				assertEquals(Boolean.FALSE, entry.getValue());
				assertEquals("com.bea.ap.plugin", container.getId());
			}
		}
	}
	
	private static final String serializedFactories = 
		"<factorypath>\n" + 
		"    <factorypathentry kind=\"PLUGIN\" id=\"com.bea.ap.plugin\" enabled=\"false\"/>\n" + 
		"    <factorypathentry kind=\"JAR\" id=\"C:\\test.jar\" enabled=\"true\"/>\n" + 
		"</factorypath>\n";
	
}
