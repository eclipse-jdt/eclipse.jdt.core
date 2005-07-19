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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptUtil;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotationProcessorFactory;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldWildcardAnnotationProcessorFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

import com.sun.mirror.apt.AnnotationProcessorFactory;

public class APITests extends Tests {
	
	public APITests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( APITests.class );
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
		return APITests.class.getName() + "Project"; //$NON-NLS-1$
	}

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testAptUtil() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		
		// Check getting a known annotation
		AnnotationProcessorFactory factory = 
			AptUtil.getFactoryForAnnotation(HelloWorldAnnotation.class.getName(), jproj);
		assertEquals(factory.getClass(), HelloWorldAnnotationProcessorFactory.class);
		
		// Check getting an annotation with a wildcard
		factory = 
			AptUtil.getFactoryForAnnotation(HelloWorldAnnotation.class.getName() + "qwerty", jproj); //$NON-NLS-1$
		assertEquals(factory.getClass(), HelloWorldWildcardAnnotationProcessorFactory.class);
	}
	
}
