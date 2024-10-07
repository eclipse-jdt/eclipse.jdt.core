/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the java 11 specific elements and validates the lambda and
 * type annotated elements. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java11ElementProcessor to the command line.
 * @since 3.14
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java11ElementProcessor extends BaseProcessor {
	boolean reportSuccessAlready = true;
	RoundEnvironment roundEnv = null;
	Messager _messager = null;
	Filer _filer = null;
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

	public void testAll() throws AssertionFailedError {
	}

	public void testFiler1() throws IOException {
		String typeName = "abc.internal.TypeInAModule";
		TypeElement typeElement = _elementUtils.getTypeElement(typeName);
		assertNotNull("type element should not be null", typeElement);
		Object obj = null;
		try {
			obj = _filer.createSourceFile("mod.a/" + typeName);
			obj = typeName;
		} catch (IOException e) {
		}
		assertNull("Source should not be created", obj);
	}
	public void testFiler2() throws IOException {
		String typeName = "abc.internal.TypeInAModule";
		TypeElement typeElement = _elementUtils.getTypeElement(typeName);
		assertNotNull("type element should not be null", typeElement);
		Object obj = null;
		try {
			obj = _filer.createSourceFile(typeName);
			obj = typeName;
		} catch (IOException e) {
		}
		assertNull("Source should not be created", obj);
	}
	public void testFiler3() throws IOException {
		String typeName = "mod.a/abc.internal.AnotherTypeInAModule";
		JavaFileObject obj = null;
		try {
			obj = _filer.createSourceFile(typeName);
		} catch (IOException e) {
		}
		assertNotNull("Source should have been created", obj);
		String name = obj.getName();
		if (!name.contains("mod.a")) {
			reportError("source should be created inside the module");
		}

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
	public void assertModifiers(Set<Modifier> modifiers, String[] expected) {
		assertEquals("Incorrect no of modifiers", modifiers.size(), expected.length);
		Set<String> actual = new HashSet<>(expected.length);
		for (Modifier modifier : modifiers) {
			actual.add(modifier.toString());
		}
		for(int i = 0, length = expected.length; i < length; i++) {
			boolean result = actual.remove(expected[i]);
			if (!result) reportError("Modifier not present :" + expected[i]);
		}
		if (!actual.isEmpty()) {
			reportError("Unexpected modifiers present:" + actual.toString());
		}
	}
	public void assertTrue(String msg, boolean value) {
		if (!value) reportError(msg);
	}
	public void assertFalse(String msg, boolean value) {
		if (value) reportError(msg);
	}
	public void assertSame(String msg, Object obj1, Object obj2) {
		if (obj1 != obj2) {
			reportError(msg + ", should be " + obj1.toString() + " but " + obj2.toString());
		}
	}
	public void assertNotSame(String msg, Object obj1, Object obj2) {
		if (obj1 == obj2) {
			reportError(msg + ", " + obj1.toString() + " should not be same as " + obj2.toString());
		}
	}
	public void assertNotNull(String msg, Object obj) {
		if (obj == null) {
			reportError(msg);
		}
	}
	public void assertNull(String msg, Object obj) {
		if (obj != null) {
			reportError(msg);
		}
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

	public void assertEquals(String msg, int expected, int actual) {
		if (expected != actual) {
			StringBuilder buf = new StringBuilder();
			buf.append(msg);
			buf.append(", expected " + expected + " but was " + actual);
			reportError(buf.toString());
		}
	}
	public void assertEquals(Object expected, Object actual) {
		if (expected != actual) {

		}
	}
	private static class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
}
