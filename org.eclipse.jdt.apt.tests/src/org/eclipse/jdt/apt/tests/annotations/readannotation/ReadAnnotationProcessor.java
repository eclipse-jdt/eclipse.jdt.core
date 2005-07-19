/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.readannotation;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

public class ReadAnnotationProcessor implements AnnotationProcessor
{
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String[] NO_ANNOTATIONS = new String[0];
	AnnotationProcessorEnvironment	_env;
	public static String ERROR = EMPTY_STRING; 
	
	public ReadAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		_env = env;
	}

	@SuppressWarnings("nls")
	public void process()
	{	
		try{			
			TypeDeclaration typeDecl = _env.getTypeDeclaration("question.AnnotationTest");		
			TestCase.assertNotNull("failed to locate type 'question.AnnotationTest'", typeDecl);
			if( typeDecl != null){			
				TestCase.assertEquals("Type name mismatch", "question.AnnotationTest", typeDecl.getQualifiedName());			
				
				final String[] expectedPkgAnnos = new String[]{ "@Deprecated()" };
				assertAnnotation(expectedPkgAnnos, typeDecl.getPackage().getAnnotationMirrors() );				
				
				final String[] expectedTypeAnnos = new String[]{ "@Deprecated()",
														  	     "@RTVisibleAnno(anno = @SimpleAnnotation(value = test), clazzes = {})",
															     "@RTInvisibleAnno(value = question)" };
				assertAnnotation(expectedTypeAnnos, typeDecl.getAnnotationMirrors());	
				
				final Collection<FieldDeclaration> fieldDecls = typeDecl.getFields();
				
				int counter = 0;
				TestCase.assertEquals(5, fieldDecls.size());
				for(FieldDeclaration fieldDecl : fieldDecls ){
					final String name = "field" + counter;				
					
					TestCase.assertEquals("field name mismatch", name, fieldDecl.getSimpleName());
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
					
					assertAnnotation(expected, fieldDecl.getAnnotationMirrors());
					counter ++;
				}			
				
				final Collection<? extends MethodDeclaration> methodDecls = typeDecl.getMethods();
				counter = 0;
				TestCase.assertEquals(7, methodDecls.size());
				for(MethodDeclaration methodDecl : methodDecls ){
					final String name = "method" + counter;				
					
					TestCase.assertEquals("method name mismatch", name, methodDecl.getSimpleName());
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
			ERROR = failure.getMessage();
			throw failure;
		}
		catch( junit.framework.AssertionFailedError error ){
			ERROR = error.getMessage();
			throw error;
		}
	}
	
	private void assertAnnotation(final String[] expected, Collection<AnnotationMirror> annotations)
	{
		final int expectedLen = expected.length;		
		TestCase.assertEquals("annotation number mismatch", expected.length, annotations.size()); //$NON-NLS-1$
		
		final HashSet<String> expectedSet = new HashSet<String>(expectedLen * 4 / 3 + 1);
		for( int i=0; i<expectedLen; i++ )
			expectedSet.add(expected[i]);
			
		int counter = 0;
		for( AnnotationMirror mirror : annotations ){
			if( counter >= expectedLen )
				TestCase.assertEquals(EMPTY_STRING, mirror.toString());
			else{
				final String mirrorToString = mirror.toString();
				final boolean contains = expectedSet.contains(mirrorToString);
				if( !contains ){					
					System.err.println(mirrorToString);
					System.err.println(expectedSet);
				}
				TestCase.assertTrue("unexpected annotation " + mirrorToString, contains); //$NON-NLS-1$
				expectedSet.remove(mirrorToString);
			}
			counter ++;
		}
	}

	public AnnotationProcessorEnvironment getEnvironment()
	{
		return _env;
	}
}