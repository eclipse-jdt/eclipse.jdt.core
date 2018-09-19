/*******************************************************************************
 * Copyright (c) 2005, 2013 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.readannotation;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.ComparisonFailure;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.ProcessorUtil;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

public class ReadAnnotationProcessor extends BaseProcessor
{
	private static final String[] NO_ANNOTATIONS = new String[0];

	public ReadAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		super(env);
	}

	public void process()
	{
		ProcessorTestStatus.setProcessorRan();
		try{
			TypeDeclaration typeDecl = _env.getTypeDeclaration("question.AnnotationTest");
			junit.framework.TestCase.assertNotNull("failed to locate type 'question.AnnotationTest'", typeDecl);
			if( typeDecl != null){
				junit.framework.TestCase.assertEquals("Type name mismatch", "question.AnnotationTest", typeDecl.getQualifiedName());

				final String[] expectedPkgAnnos = new String[]{ "@Deprecated()" };
				assertAnnotation(expectedPkgAnnos, typeDecl.getPackage().getAnnotationMirrors() );
				assertAnnotation(expectedPkgAnnos, _env.getPackage("question").getAnnotationMirrors() );

				final String[] expectedNoTypeAnnos = new String[]{ "@SimpleAnnotation(value = foo)" };
				assertAnnotation(expectedNoTypeAnnos, _env.getPackage("notypes").getAnnotationMirrors());

				final String[] expectedTypeAnnos = new String[]{ "@Deprecated()",
														  	     "@RTVisibleAnno(anno = @SimpleAnnotation(value = test), clazzes = {})",
															     "@RTInvisibleAnno(value = question)" };
				assertAnnotation(expectedTypeAnnos, typeDecl.getAnnotationMirrors());

				final Collection<FieldDeclaration> fieldDecls = typeDecl.getFields();

				int counter = 0;
				junit.framework.TestCase.assertEquals(5, fieldDecls.size());
				for(FieldDeclaration fieldDecl : fieldDecls ){
					final String name = "field" + counter;

					junit.framework.TestCase.assertEquals("field name mismatch", name, fieldDecl.getSimpleName());
					final String[] expected;
					switch(counter){
					case 0:
						expected = new String[] { "@RTVisibleAnno(name = Foundation, boolValue = false, byteValue = 16, charValue = c, doubleValue = 99.0, floatValue = 9.0, intValue = 999, longValue = 3333, shortValue = 3, colors = {RED, BLUE}, anno = @SimpleAnnotation(value = core), simpleAnnos = {@SimpleAnnotation(value = org), @SimpleAnnotation(value = eclipse), @SimpleAnnotation(value = jdt)}, clazzes = {java.lang.Object, java.lang.String}, clazz = java.lang.Object)",
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

					assertAnnotation(expected, fieldDecl.getAnnotationMirrors());
					counter ++;
				}

				final Collection<? extends MethodDeclaration> methodDecls = typeDecl.getMethods();
				counter = 0;
				junit.framework.TestCase.assertEquals(8, methodDecls.size());
				for(MethodDeclaration methodDecl : methodDecls ){
					final String name = "method" + counter;

					junit.framework.TestCase.assertEquals("method name mismatch", name, methodDecl.getSimpleName());
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
						expected = NO_ANNOTATIONS;
						break;
					case 7:
						expected = new String[] { "@RTVisibleAnno(name = I'm \"special\": 	\\\n, charValue = ', clazzes = {}, anno = @SimpleAnnotation(value = ))" };
						break;
					default:
						expected = NO_ANNOTATIONS;
					}

					assertAnnotation(expected, methodDecl.getAnnotationMirrors());

					if( counter == 5 ){
						Collection<ParameterDeclaration> paramDecls = methodDecl.getParameters();
						int pCounter = 0;
						for( ParameterDeclaration paramDecl : paramDecls ){
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
							assertAnnotation(expectedParamAnnotations, paramDecl.getAnnotationMirrors());
							pCounter ++;
						}

					}
					counter ++;
				}
			}
		}
		catch( ComparisonFailure failure ){
			if (!ProcessorTestStatus.hasErrors()) {
				ProcessorTestStatus.failWithoutException(failure.toString());
			}
			throw failure;
		}
		catch( junit.framework.AssertionFailedError error ){
			if (!ProcessorTestStatus.hasErrors()) {
				ProcessorTestStatus.failWithoutException(error.toString());
			}
			throw error;
		}
	}

	private void assertAnnotation(final String[] expected, Collection<AnnotationMirror> annotations)
	{
		final int expectedLen = expected.length;
		junit.framework.TestCase.assertEquals("annotation number mismatch", expected.length, annotations.size()); //$NON-NLS-1$

		final HashSet<String> expectedSet = new HashSet<String>(expectedLen * 4 / 3 + 1);
		for( int i=0; i<expectedLen; i++ )
			expectedSet.add(expected[i]);

		int counter = 0;
		for( AnnotationMirror mirror : annotations ){
			String mirrorString = ProcessorUtil.annoMirrorToString(mirror);
			if( counter >= expectedLen )
				junit.framework.TestCase.assertEquals("", mirrorString); //$NON-NLS-1$
			else{
				final boolean contains = expectedSet.contains(mirrorString);
				if( !contains ){
					System.err.println("found unexpected: " + mirrorString);
					System.err.println("expected set: " + expectedSet);
				}
				junit.framework.TestCase.assertTrue("unexpected annotation " + mirrorString, contains); //$NON-NLS-1$
				expectedSet.remove(mirrorString);
			}
			counter ++;
		}
	}
}