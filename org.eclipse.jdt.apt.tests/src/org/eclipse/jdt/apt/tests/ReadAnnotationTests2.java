/*******************************************************************************
 * Copyright (c) 2005, 2021 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.tests.annotations.readannotation.CodeExample;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * This test the dom layer of annotation support. No APT involved.
 * @author tyeung
 */
public class ReadAnnotationTests2 extends BuilderTests {

	private String[] NO_ANNOTATIONS = new String[0];
	private ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];
	private int counter = 0;
	private String projectName = null;
	public ReadAnnotationTests2(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( ReadAnnotationTests2.class );
	}

	public String getProjectName() {
		return projectName;
	}

	public String getUniqueProjectName(){
		projectName = ReadAnnotationTests.class.getName() + "Project" + counter; //$NON-NLS-1$
		counter ++;
		return projectName;
	}


	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}

	public IPath getBinaryPath(){
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "binary" ); //$NON-NLS-1$
		IPath lib = srcFolder.getFullPath();
		return lib;
	}

	public IPath getOutputPath(){
		IProject project = env.getProject( getProjectName() );
		IFolder binFolder = project.getFolder( "bin" ); //$NON-NLS-1$
		IPath bin = binFolder.getFullPath();
		return bin;
	}

	private void addAllSources()
	{
		IPath srcRoot = getSourcePath();
		// SimpleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.SIMPLE_ANNOTATION_CLASS,
				CodeExample.SIMPLE_ANNOTATION_CODE );

		// RTVisibleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.RTVISIBLE_CLASS,
				CodeExample.RTVISIBLE_ANNOTATION_CODE);

		// RTInvisibleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.RTINVISIBLE_CLASS,
				CodeExample.RTINVISIBLE_ANNOTATION_CODE);

		// package-info.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.PACKAGE_INFO_CLASS,
				CodeExample.PACKAGE_INFO_CODE);

		// Color.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.COLOR_CLASS,
				CodeExample.COLOR_CODE);

		// AnnotationTest.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.ANNOTATION_TEST_CLASS,
				CodeExample.ANNOTATION_TEST_CODE);
	}

	private IProject setupTest() throws Exception
	{
		// project will be deleted by super-class's tearDown() method
		IPath projectPath = env.addProject( getUniqueProjectName(), "1.5" ); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );

		IJavaProject jproj = env.getJavaProject(projectPath);
		jproj.setOption("org.eclipse.jdt.core.compiler.problem.deprecation", "ignore");

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$

		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		return env.getProject(getProjectName());
	}

	public void testSourceAnnotation() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();
		addAllSources();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		_testAnnotations();
	}

	public void testBinaryAnnotation() throws Exception
	{
		IProject project = setupTest();
		final File jar =
			TestUtil.getFileInPlugin(AptTestsPlugin.getDefault(),
									 new Path("/resources/question.jar")); //$NON-NLS-1$
		final String path = jar.getAbsolutePath();
		env.addExternalJar(project.getFullPath(), path);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		_testAnnotations();
	}

	private ITypeBinding getTypeBinding(final String key, final IJavaProject javaProj)
	{
		class BindingRequestor extends ASTRequestor
		{
			private ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding)
			{
				if( binding != null && binding.getKind() == IBinding.TYPE )
					_result = (ITypeBinding)binding;
			}
		}

		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setResolveBindings(true);
		parser.setProject(javaProj);
		parser.setIgnoreMethodBodies(true);
		parser.createASTs(NO_UNIT, new String[]{key}, requestor, null);
		return requestor._result;
	}

	public void _testAnnotations()
	{
		final String typeKey = BindingKey.createTypeBindingKey("question.AnnotationTest");
		final ITypeBinding typeBinding = getTypeBinding(typeKey, env.getJavaProject(getProjectName()));

		assertNotNull("failed to locate 'question.AnnotationTest'", typeBinding);
		assertEquals("Type name mismatch", "question.AnnotationTest", typeBinding.getQualifiedName());

		// test package annotation
		final String[] expectedPkgAnnos = new String[]{ "@Deprecated()" };
		assertAnnotation(expectedPkgAnnos, typeBinding.getPackage().getAnnotations() );

		// test annotation on type.
		final String[] expectedTypeAnnos = new String[]{ "@Deprecated()",
		  	     "@RTVisibleAnno(anno = @SimpleAnnotation(value = test), clazzes = {})",
			     "@RTInvisibleAnno(value = question)" };

		assertAnnotation(expectedTypeAnnos, typeBinding.getAnnotations());

		final IVariableBinding[] fieldBindings = typeBinding.getDeclaredFields();
		int counter = 0;
		assertEquals(5, fieldBindings.length);
		for(IVariableBinding fieldDecl : fieldBindings ){
			final String name = "field" + counter;

			assertEquals("field name mismatch", name, fieldDecl.getName());
			final String[] expected;
			switch(counter){
			case 0:
				expected = new String[] { "@RTVisibleAnno(name = Foundation, boolValue = false, byteValue = 16, charValue = c, doubleValue = 99.0, floatValue = 9.0, intValue = 999, longValue = 3333, shortValue = 3, colors = {question.Color RED, question.Color BLUE}, anno = @SimpleAnnotation(value = core), simpleAnnos = {@SimpleAnnotation(value = org), @SimpleAnnotation(value = eclipse), @SimpleAnnotation(value = jdt)}, clazzes = {Object.class, String.class}, clazz = Object.class)",
								          "@RTInvisibleAnno(value = org.eclipse.jdt.core)",
								          "@Deprecated()" };
				break;
			case 1:
				expected = new String[] { "@Deprecated()" };
				break;
			case 2:
				expected = new String[] { "@RTVisibleAnno(anno = @SimpleAnnotation(value = field), clazzes = {})",
										  "@RTInvisibleAnno(value = 2)" };
				break;
			case 3:
				expected = new String[] { "@RTInvisibleAnno(value = 3)" };
				break;
			case 4:
				expected = new String[] { "@SimpleAnnotation(value = 4)" };
				break;
			default:
				expected = NO_ANNOTATIONS;
			}

			assertAnnotation(expected, fieldDecl.getAnnotations());
			counter ++;
		}


		final IMethodBinding[] methodBindings = typeBinding.getDeclaredMethods();
		counter = 0;
		assertEquals(7, methodBindings.length);
		for(IMethodBinding methodDecl : methodBindings ){
			final String name = "method" + counter;

			assertEquals("method name mismatch", name, methodDecl.getName());
			final String[] expected;
			switch(counter)
			{
			case 0:
				expected = new String[] { "@RTVisibleAnno(anno = @SimpleAnnotation(value = method0), clazzes = {})",
					                      "@RTInvisibleAnno(value = 0)",
					                      "@Deprecated()" };
				break;
			case 1:
				expected = new String[] { "@Deprecated()" };
				break;
			case 2:
				expected = new String[] { "@RTVisibleAnno(anno = @SimpleAnnotation(value = method2), clazzes = {})",
										  "@RTInvisibleAnno(value = 2)" };
				break;
			case 3:
				expected = new String[] { "@RTInvisibleAnno(value = 3)" };
				break;
			case 4:
				expected = new String[] { "@SimpleAnnotation(value = method4)" };
				break;
			case 5:
			case 6:
			default:
				expected = NO_ANNOTATIONS;
			}

			assertAnnotation(expected, methodDecl.getAnnotations());

			if( counter == 5 ){
				final int numParameters = methodDecl.getParameterTypes().length;
				for( int pCounter=0; pCounter<numParameters; pCounter++ ){
					final String[] expectedParamAnnotations;
					switch( pCounter )
					{
					case 1:
						expectedParamAnnotations = new String[] { "@Deprecated()" };
						break;
					case 2:
						expectedParamAnnotations = new String[] { "@RTVisibleAnno(anno = @SimpleAnnotation(value = param2), clazzes = {})",
															      "@RTInvisibleAnno(value = 2)" };
						break;
					default:
						expectedParamAnnotations = NO_ANNOTATIONS;
					}
					assertAnnotation(expectedParamAnnotations, methodDecl.getParameterAnnotations(pCounter));
				}

			}
			counter ++;
		}
	}

	private void assertAnnotation(final String[] expected, IAnnotationBinding[] annotations)
	{
		final int expectedLen = expected.length;
		assertEquals("annotation number mismatch", expected.length, annotations.length); //$NON-NLS-1$

		final HashSet<String> expectedSet = new HashSet<String>(expectedLen * 4 / 3 + 1);
		for( int i=0; i<expectedLen; i++ )
			expectedSet.add(expected[i]);

		int counter = 0;
		for( IAnnotationBinding mirror : annotations ){
			if( counter >= expectedLen )
				assertEquals("", mirror.toString()); //$NON-NLS-1$
			else{
				final String mirrorToString = mirror.toString();
				final boolean contains = expectedSet.contains(mirrorToString);
				if( !contains ){
					System.err.println(mirrorToString);
					System.err.println(expectedSet);
				}
				assertTrue("unexpected annotation " + mirrorToString, contains); //$NON-NLS-1$
				expectedSet.remove(mirrorToString);
			}
			counter ++;
		}
	}


}
