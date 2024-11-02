/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements.DocCommentKind;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the Java 23 specific elements and validates the lambda and
 * type annotated elements. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java23ElementProcessor to the command line.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java23ElementProcessor extends BaseProcessor {
	boolean reportSuccessAlready = true;
	RoundEnvironment roundEnv = null;
	Messager _messager = null;
	Filer _filer = null;
	boolean isBinaryMode = false;
	String mode;
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
		_messager = processingEnv.getMessager();
		_filer = processingEnv.getFiler();
	}
	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}

		this.roundEnv = roundEnv;
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		} else {
			try {
				if (options.containsKey("binary")) {
					this.isBinaryMode = true;
					this.mode = "binary";
				} else {
					this.mode = "source";
				}
				if (!invokeTestMethods(options)) {
					testAll();
				}
				if (this.reportSuccessAlready) {
					super.reportSuccess();
				}
			} catch (AssertionFailedError e) {
				super.reportError(getExceptionStackTrace(e));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean invokeTestMethods(Map<String, String> options) throws Throwable {
		Method testMethod = null;
		Set<String> keys = options.keySet();
		boolean testsFound = false;
		for (String option : keys) {
			if (option.startsWith("test")) {
				try {
					testMethod = this.getClass().getDeclaredMethod(option, new Class[0]);
					if (testMethod != null) {
						testsFound = true;
						testMethod.invoke(this,  new Object[0]);
					}
				} catch (InvocationTargetException e) {
					throw e.getCause();
				} catch (Exception e) {
					super.reportError(getExceptionStackTrace(e));
				}
			}
		}
		return testsFound;
	}

	public void testAll() throws AssertionFailedError, IOException {
		testJavadocKind1();
		testJavadocKind2();
	}

	public void testJavadocKind1() throws IOException {
		String typeName = "my.mod.Main1";
		TypeElement typeElement = _elementUtils.getTypeElement(typeName);
		assertNotNull("type element should not be null", typeElement);
		DocCommentKind kind = _elementUtils.getDocCommentKind(typeElement);
		assertSame("Incorrect doc kind", DocCommentKind.TRADITIONAL, kind);
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		ExecutableElement method = null;
		for (Element element : enclosedElements) {
			if ("myMethod".equals(element.getSimpleName().toString())) {
				method = (ExecutableElement) element;
			}
		}
		assertNotNull("method element should not be null", method);
		kind = _elementUtils.getDocCommentKind(method);
		assertSame("Incorrect doc kind", DocCommentKind.END_OF_LINE, kind);
		String docComment = _elementUtils.getDocComment(method);
		assertEquals("Incorrect doc comment", 
				"\n/A markdown type comment on a method - line 1\n"
				+ "//// A markdown type comment on a method - line 2\n"
				+ "    A markdown type comment on a method - line 3\n", docComment);
	}
	public void testJavadocKind2() throws IOException {
		String typeName = "my.mod.Main2";
		TypeElement typeElement = _elementUtils.getTypeElement(typeName);
		assertNotNull("type element should not be null", typeElement);
		DocCommentKind kind = _elementUtils.getDocCommentKind(typeElement);
		assertSame("Incorrect doc kind", DocCommentKind.END_OF_LINE, kind);
		String docComment = _elementUtils.getDocComment(typeElement);
		assertEquals("Incorrect doc comment",
				"\n/A markdown type comment on a class - line 1\n"
				+ "//// A markdown type comment on a class - line 2\n"
				+ "    A markdown type comment on a class - line 3\n", docComment);
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		ExecutableElement method = null;
		for (Element element : enclosedElements) {
			if ("myMethod".equals(element.getSimpleName().toString())) {
				method = (ExecutableElement) element;
			}
		}
		assertNotNull("method element should not be null", method);
		kind = _elementUtils.getDocCommentKind(method);
		assertSame("Incorrect doc kind", DocCommentKind.TRADITIONAL, kind);
	}
	public void testMarkdownContent3() throws IOException {
		String typeName = "my.mod.Main1";
		TypeElement typeElement = _elementUtils.getTypeElement(typeName);
		assertNotNull("type element should not be null", typeElement);
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		ExecutableElement foo1 = null;
		ExecutableElement foo2 = null;
		ExecutableElement foo3 = null;
		ExecutableElement foo4 = null;
		for (Element element : enclosedElements) {
			switch(element.getSimpleName().toString()) {
				case "foo1": 
					foo1 = (ExecutableElement) element;
				case "foo2": 
					foo2 = (ExecutableElement) element;
				case "foo3": 
					foo3 = (ExecutableElement) element;
				case "foo4": 
					foo4 = (ExecutableElement) element;
				default: break;
			}
		}
		String docComment = _elementUtils.getDocComment(foo1);
		assertEquals("Incorrect doc comment", 
				"Doc comment with 3 lines\n"
				+ "\n"
				+ "with an empty line in the middle", docComment);
		docComment = _elementUtils.getDocComment(foo2);
		assertEquals("Incorrect doc comment", 
				"This is the actual doc commment.", docComment);
		docComment = _elementUtils.getDocComment(foo3);
		assertEquals("Incorrect doc comment", 
				  "| Code  | Color |\n"
				+ "|-------|-------|\n"
				+ "| R     | Red   |\n"
				+ "| G     | Green |\n"
				+ "| B     | Blue  |", docComment);
		docComment = _elementUtils.getDocComment(foo4);
		assertEquals("Incorrect doc comment", 
				  "{@inheritDoc}\n"
				+ "Get the inherited function.\n"
				+ "\n"
				+ "@param p parameter", docComment);
	}

	@Override
	public void reportError(String msg) {
		throw new AssertionFailedError(msg);
	}
	private String getExceptionStackTrace(Throwable t) {
		StringBuilder buf = new StringBuilder(t.getMessage());
		StackTraceElement[] traces = t.getStackTrace();
		for (int i = 0; i < traces.length; i++) {
			StackTraceElement trace = traces[i];
			buf.append("\n\tat " + trace);
			if (i == 12)
				break; // Don't dump all stacks
		}
		return buf.toString();
	}
    public void assertEquals(String message, Object expected, Object actual) {
        if (equalsRegardingNull(expected, actual)) {
            return;
        } else {
        	reportError(message + ", expected " + expected.toString() + " but was " + actual.toString());
        }
    }

    public void assertEquals(String message, Object expected, Object alternateExpected, Object actual) {
        if (equalsRegardingNull(expected, actual) || equalsRegardingNull(alternateExpected, actual)) {
            return;
        } else {
        	reportError(message + ", expected " + expected.toString() + " but was " + actual.toString());
        }
    }

    static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }
        return expected.equals(actual);
    }
	public void assertSame(String msg, Object obj1, Object obj2) {
		if (obj1 != obj2) {
			reportError(msg + ", should be " + obj1.toString() + " but " + obj2.toString());
		}
	}
	public void assertNotNull(String msg, Object obj) {
		if (obj == null) {
			reportError(msg);
		}
	}
	private static class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
}
