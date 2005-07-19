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
package org.eclipse.jdt.apt.tests.annotations.valueconversion;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;

public class ValueConversionProcessor implements AnnotationProcessor {
	
	public static String ERROR = ""; //$NON-NLS-1$
	private final AnnotationProcessorEnvironment _env;
	public ValueConversionProcessor(AnnotationProcessorEnvironment env)
	{
		_env = env;
	}
	
	@SuppressWarnings("nls")
	public void process() 
	{
		try{
			final TypeDeclaration test = _env.getTypeDeclaration("sample.Test");
			if( test == null )
				TestCase.assertNotNull("failed to locate type 'sample.Test'", test);
			
			final Collection<AnnotationMirror> annotations = test.getAnnotationMirrors();
			final int numAnnotations = annotations == null ? 0 : annotations.size();
			TestCase.assertEquals("annotation number mismatch", 1, numAnnotations);
			
			final AnnotationMirror annotation = annotations.iterator().next();
			final AnnotationType annotationType = annotation.getAnnotationType();
			
			TestCase.assertEquals(
					"annotation type mismatch", 
					"sample.Test.Annotation", 
					annotationType.getDeclaration().getQualifiedName());
			
			final Map<AnnotationTypeElementDeclaration, AnnotationValue> elementValues =
				annotation.getElementValues();
			
			for( Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : 
				 elementValues.entrySet() ){
				
				AnnotationTypeElementDeclaration elementDecl = entry.getKey();
				final String name = elementDecl.getSimpleName();
				final AnnotationValue value = entry.getValue();
				
				if( name.length() == 1 )
				{
					final Class expectedType;
					switch(name.charAt(0))
					{
					case 'b':
						expectedType = Byte.class;
						break;					
					case 'c':
						expectedType = Character.class;
						break;
					case 's':
						expectedType = Short.class;
						break;
					case 'i':
						expectedType = Integer.class;
						break;
					case 'l':
						expectedType = Long.class;
						break;
					case 'f':
						expectedType = Float.class;
						break;
					case 'd':
						expectedType = Double.class;
						break;
					default:
						TestCase.assertNotNull("unexpected member " + name, null);
						throw new IllegalStateException(); // won't get here.
					}
					assertValueTypeMatch(name, value.getValue(), expectedType);
				}	
				else{
					@SuppressWarnings("unused")
					final Class expectedElementType;
					if( "chars".equals(name) )
						expectedElementType = Character.class;
					else if( "bytes".equals(name) )
						expectedElementType = Byte.class;
					else if( "shorts".equals(name) )
						expectedElementType = Short.class;
					else if( "ints".equals(name) )
						expectedElementType = Integer.class;
					else if( "longs".equals(name) )
						expectedElementType = Long.class;
					else if( "floats".equals(name) )
						expectedElementType = Float.class;
					else if( "doubles".equals(name) )
						expectedElementType = Double.class;
					else{
						TestCase.assertNotNull("unexpected member " + name, null);
						throw new IllegalStateException(); // won't get here.
					}
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
	
	private void assertValueTypeMatch(final String name, final Object value, final Class expectedType)
	{
		TestCase.assertEquals(
				"value type mismatch for member " + name,  //$NON-NLS-1$
				expectedType, 
				value.getClass());
	}
			
	
	@SuppressWarnings("unused") //$NON-NLS-1$
	private void assertArrayValueTypeMatch(final String name, final Object value, final Class expectedElementType)
	{	
		TestCase.assertEquals(
				"annotation value type mismatch",  //$NON-NLS-1$
				List.class.getName(), 
				value.getClass().getName());
		
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		final List<AnnotationValue> values = (List<AnnotationValue>)value;
		for( AnnotationValue av : values )
			assertValueTypeMatch(name, av.getValue(), expectedElementType );
	}
}
