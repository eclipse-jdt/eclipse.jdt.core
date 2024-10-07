/*******************************************************************************
 * Copyright (c) 2005, 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    het@google.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.annotationvalue;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import junit.framework.ComparisonFailure;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

public class AnnotationValueProcessor extends BaseProcessor {
	public AnnotationValueProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process() {
		ProcessorTestStatus.setProcessorRan();
		try{
			TypeDeclaration typeDecl = _env.getTypeDeclaration("question.AnnotationTest");
			junit.framework.TestCase.assertNotNull("failed to locate type 'question.AnnotationTest'", typeDecl);
			if( typeDecl != null){
				FieldDeclaration firstFieldDecl = null;
				for (FieldDeclaration fieldDeclaration : typeDecl.getFields()) {
					firstFieldDecl = fieldDeclaration;
					break;
				}

				AnnotationMirror rtVisibleAnnotationMirror = null;
				for (AnnotationMirror annotationMirror : firstFieldDecl.getAnnotationMirrors()) {
					if (annotationMirror.getAnnotationType().getDeclaration().getSimpleName().equals("RTVisibleAnno")) {
						rtVisibleAnnotationMirror = annotationMirror;
						break;
					}
				}

				final Map<String, String> namesToValues = new HashMap<>();
				namesToValues.put("name", "\"Foundation\"");
				namesToValues.put("boolValue", "false");
				namesToValues.put("byteValue", "16");
				namesToValues.put("charValue", "'c'");
				namesToValues.put("doubleValue", "99.0");
				namesToValues.put("floatValue", "9.0");
				namesToValues.put("intValue", "999");
				namesToValues.put("longValue", "3333");
				namesToValues.put("shortValue", "3");
				namesToValues.put("colors", "{question.Color.RED, question.Color.BLUE}");
				namesToValues.put("anno", "@question.SimpleAnnotation(value = \"core\")");
				namesToValues.put("simpleAnnos", "{@question.SimpleAnnotation(value = \"org\"), @question.SimpleAnnotation(value = \"eclipse\"), @question.SimpleAnnotation(value = \"jdt\")}");
				namesToValues.put("clazzes", "{java.lang.Object.class, java.lang.String.class}");
				namesToValues.put("clazz", "java.lang.Object.class");
				assertAnnotation(namesToValues, rtVisibleAnnotationMirror);
			}
		}
		catch(ComparisonFailure failure) {
			if (!ProcessorTestStatus.hasErrors()) {
				ProcessorTestStatus.failWithoutException(failure.toString());
			}
			throw failure;
		}
		catch(junit.framework.AssertionFailedError error) {
			if (!ProcessorTestStatus.hasErrors()) {
				ProcessorTestStatus.failWithoutException(error.toString());
			}
			throw error;
		}
	}

	private void assertAnnotation(final Map<String, String> namesToValues, AnnotationMirror annotation)	{
		Map<AnnotationTypeElementDeclaration, AnnotationValue> values = annotation.getElementValues();
		for (Entry<AnnotationTypeElementDeclaration, AnnotationValue> e : values.entrySet()) {
			String key = e.getKey().getSimpleName();
			if (namesToValues.containsKey(key)) {
				junit.framework.TestCase.assertEquals(namesToValues.get(key), e.getValue().toString());
				namesToValues.remove(key);
			} else {
				junit.framework.TestCase.fail("Unexpected annotation element: " + key);
			}
		}
		junit.framework.TestCase.assertEquals(0, namesToValues.size());
	}
}