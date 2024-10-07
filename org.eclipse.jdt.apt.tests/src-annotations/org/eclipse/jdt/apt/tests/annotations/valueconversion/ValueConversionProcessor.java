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
package org.eclipse.jdt.apt.tests.annotations.valueconversion;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

public class ValueConversionProcessor extends BaseProcessor {

	public static final Byte BYTE_49 = 49;
	public static final Byte BYTE_50 = 50;
	public static final Character CHAR_ONE = '1';
	public static final Character CHAR_TWO = '2';
	public static final Double DOUBLE_49 = 49d;
	public static final Double DOUBLE_50 = 50d;
	public static final Float FLOAT_49 = 49f;
	public static final Float FLOAT_50 = 50f;
	public static final Integer INTEGER_49 = 49;
	public static final Integer INTEGER_50 = 50;
	public static final Long LONG_49 = 49l;
	public static final Long LONG_50 = 50l;
	public static final Short SHORT_49 = 49;
	public static final Short SHORT_50 = 50;

	public ValueConversionProcessor(AnnotationProcessorEnvironment env)
	{
		super(env);
	}

	public void process()
	{
		ProcessorTestStatus.setProcessorRan();
		final TypeDeclaration test = _env.getTypeDeclaration("sample.Test");
		if( test == null )
			junit.framework.TestCase.assertNotNull("failed to locate type 'sample.Test'", test);

		testCompilerAPIPath(test);
		testReflectionPath(test);
	}

	private void testCompilerAPIPath(TypeDeclaration test){
		final Collection<AnnotationMirror> annotations = test.getAnnotationMirrors();
		final int numAnnotations = annotations == null ? 0 : annotations.size();
		junit.framework.TestCase.assertEquals("annotation number mismatch", 1, numAnnotations);

		final AnnotationMirror annotation = annotations.iterator().next();
		final AnnotationType annotationType = annotation.getAnnotationType();
		final String annoTypeName = annotationType.getDeclaration().getQualifiedName();
		if( !Annotation.class.getName().equals( annoTypeName ) &&
			!AnnotationWithArray.class.getName().equals( annoTypeName ))
			return;

		final Map<AnnotationTypeElementDeclaration, AnnotationValue> elementValues =
			annotation.getElementValues();

		for( Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry :
			 elementValues.entrySet() ){
			AnnotationTypeElementDeclaration elementDecl = entry.getKey();
			final String name = elementDecl.getSimpleName();
			final AnnotationValue value = entry.getValue();
			compare(name, value.getValue());
		}
	}

	private void testReflectionPath(TypeDeclaration test){
		final RefAnnotation refAnno = test.getAnnotation(RefAnnotation.class);
		if( refAnno != null ){
			assertValueMatch("z", refAnno.z(), true);
			assertValueMatch("b", refAnno.b(), (byte)49);
			assertValueMatch("c", refAnno.c(), '1');
			assertValueMatch("d", refAnno.d(), 49);
			assertValueMatch("f", refAnno.f(), 49);
			assertValueMatch("i", refAnno.i(), 49);
			assertValueMatch("l", refAnno.l(), 49);
			assertValueMatch("s", refAnno.s(), (short)49);
		}
		final RefAnnotationWithArray refAnnoArray = test.getAnnotation(RefAnnotationWithArray.class);
		if( refAnnoArray != null ){
			assertArrayValueMatch("booleans", refAnnoArray.booleans(), new boolean[]{true, true});
			assertArrayValueMatch("bytes", refAnnoArray.bytes(), new byte[]{49, 50} );
			assertArrayValueMatch("chars", refAnnoArray.chars(), new char[]{'1', '2'});
			assertArrayValueMatch("doubles", refAnnoArray.doubles(), new double[]{49d, 50d});
			assertArrayValueMatch("floats", refAnnoArray.floats(), new float[]{49f, 50f});
			assertArrayValueMatch("ints", refAnnoArray.ints(), new int[]{49, 50});
			assertArrayValueMatch("longs", refAnnoArray.longs(), new long[]{49l, 50l});
			assertArrayValueMatch("shorts", refAnnoArray.shorts(), new short[]{49, 50});
			compare("str", refAnnoArray.str());
		}
	}

	private void compare(final String name, final Object actualValue){
		if( name.length() == 1 )
		{
			final Class<?> expectedType;
			final Object expectedValue;
			switch(name.charAt(0))
			{
			case 'z':
				expectedType = Boolean.class;
				expectedValue = Boolean.TRUE;
				break;
			case 'b':
				expectedType = Byte.class;
				expectedValue = BYTE_49;
				break;
			case 'c':
				expectedType = Character.class;
				expectedValue = CHAR_ONE;
				break;
			case 's':
				expectedType = Short.class;
				expectedValue = SHORT_49;
				break;
			case 'i':
				expectedType = Integer.class;
				expectedValue = INTEGER_49;
				break;
			case 'l':
				expectedType = Long.class;
				expectedValue = LONG_49;
				break;
			case 'f':
				expectedType = Float.class;
				expectedValue = FLOAT_49;
				break;
			case 'd':
				expectedType = Double.class;
				expectedValue = DOUBLE_49;
				break;
			default:
				junit.framework.TestCase.assertNotNull("unexpected member " + name, null);
				throw new IllegalStateException(); // won't get here.
			}
			assertValueTypeMatch(name, actualValue, expectedType, expectedValue);
		}
		else{
			final Class<?> expectedElementType;
			final Object[] expectedElementValues;
			if( "booleans".equals(name) ){
				expectedElementType = Boolean.class;
				expectedElementValues = new Object[]{Boolean.TRUE, Boolean.TRUE};
			}
			else if( "chars".equals(name) ){
				expectedElementType = Character.class;
				expectedElementValues = new Object[]{CHAR_ONE, CHAR_TWO};
			}
			else if( "bytes".equals(name) ){
				expectedElementType = Byte.class;
				expectedElementValues = new Object[]{BYTE_49, BYTE_50};
			}
			else if( "shorts".equals(name) ){
				expectedElementType = Short.class;
				expectedElementValues = new Object[]{SHORT_49, SHORT_50};
			}
			else if( "ints".equals(name) ){
				expectedElementType = Integer.class;
				expectedElementValues = new Object[]{INTEGER_49, INTEGER_50};
			}
			else if( "longs".equals(name) ){
				expectedElementType = Long.class;
				expectedElementValues = new Object[]{LONG_49, LONG_50};
			}
			else if( "floats".equals(name) ){
				expectedElementType = Float.class;
				expectedElementValues = new Object[]{FLOAT_49, FLOAT_50};

			}
			else if( "doubles".equals(name) ){
				expectedElementType = Double.class;
				expectedElementValues = new Object[]{DOUBLE_49, DOUBLE_50};
			}
			else if( "str".equals(name) ){
				assertValueTypeMatch(name, actualValue, String.class, "string");
				return;
			}
			else{
				junit.framework.TestCase.assertNotNull("unexpected member " + name, null);
				throw new IllegalStateException(); // won't get here.
			}
			@SuppressWarnings("unchecked")
			List<AnnotationValue> actualList = (List<AnnotationValue>)actualValue;
			assertArrayValueTypeMatch(name, actualList, expectedElementType, expectedElementValues);
		}
	}

	private void assertValueTypeMatch(
			final String name,
			final Object actualValue,
			final Class<?> expectedType,
			final Object expectedValue)
	{
		if( actualValue != null && expectedType != actualValue.getClass() ){
			final Messager msgr = _env.getMessager();
			msgr.printError("type mismatch for member " + name +
					" expected " + expectedType.getName() + " but got " + actualValue.getClass().getName());
		}
		else if( !expectedValue.equals(actualValue) ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expectedValue + " but got " + actualValue);
		}
	}

	private void assertArrayValueTypeMatch(
			final String name,
			final List<AnnotationValue> actualValues,
			final Class<?> expectedElementType,
			final Object[] expectedValues)
	{
		int i=0;
		for( AnnotationValue av : actualValues ){
			assertValueTypeMatch(name, av.getValue(), expectedElementType, expectedValues[i] );
			i++;
		}
	}

	private void assertValueMatch(
			final String name,
			final boolean actual,
			final boolean expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final byte actual,
			final byte expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final char actual,
			final char expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final double actual,
			final double expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final float actual,
			final float expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final int actual,
			final int expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final long actual,
			final long expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}

	private void assertValueMatch(
			final String name,
			final short actual,
			final short expected){

		if( actual != expected ){
			final Messager msgr = _env.getMessager();
			msgr.printError("value mismatch for member " + name +
					" expected " + expected + " but got " + actual);
		}
	}


	private void assertArrayValueMatch(
			final String name,
			final boolean[] actual,
			final boolean[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( boolean a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final byte[] actual,
			final byte[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( byte a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final char[] actual,
			final char[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( char a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final double[] actual,
			final double[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( double a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final float[] actual,
			final float[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( float a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final int[] actual,
			final int[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( int a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final long[] actual,
			final long[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( long a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}

	private void assertArrayValueMatch(
			final String name,
			final short[] actual,
			final short[] expected){

		int i=0;
		final Messager msgr = _env.getMessager();
		for( short a : actual ){
			if( a != expected[i] ){
				msgr.printError("value mismatch for member " + name +
						" expected " + expected[i] + " but got " + a);
			}
			i++;
		}
	}
}
