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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the java 13 specific elements and validates the lambda and
 * type annotated elements. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java11ElementProcessor to the command line.
 * @since 3.14
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java22ElementProcessor extends BaseProcessor {
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
		testGetEnumConstantBody01();
		testGetEnumConstantBody02();
	}
	public void testGetEnumConstantBody01() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		TypeElement elem = find(rootElements, "EnumColor");
		assertNotNull("TypeElement for enum should not be null", elem);
		List<? extends Element> members = _elementUtils.getAllMembers(elem);
		VariableElement blue = null, red = null;
		for (Element member : members) {
			if ("BLUE".equals(member.getSimpleName().toString())) {
				blue = (VariableElement) member;
			} else if ("RED".equals(member.getSimpleName().toString())) {
				red = (VariableElement) member;
			}
		}
		assertNotNull("enum constant should not be null", blue);
		assertNotNull("enum constant should not be null", red);
		TypeElement enumConstantBody = _elementUtils.getEnumConstantBody(red);
		assertNotNull("constant body should not be null", enumConstantBody);
		Element enclosingElement = enumConstantBody.getEnclosingElement();
		assertSame("Incorrect element kind", ElementKind.ENUM_CONSTANT, enclosingElement.getKind());
		assertEquals("incorrect enum constant", "RED", enclosingElement.getSimpleName().toString());
		List<? extends Element> enclosedElements = enumConstantBody.getEnclosedElements();
		ExecutableElement method = null;
		for (Element element : enclosedElements) {
			if (element.getKind() == ElementKind.METHOD) {
				if (element.getSimpleName().toString().equals("hasOptionalBody")) {
					method = (ExecutableElement) element;
				}
			}
		}
		assertNotNull("method should not be null", method);
		enumConstantBody = _elementUtils.getEnumConstantBody(blue);
		assertNotNull("constant body should not be null", enumConstantBody);
		enclosingElement = enumConstantBody.getEnclosingElement();
		assertSame("Incorrect element kind", ElementKind.ENUM_CONSTANT, enclosingElement.getKind());
		assertEquals("incorrect enum constant", "BLUE", enclosingElement.getSimpleName().toString());
		enclosedElements = enumConstantBody.getEnclosedElements();
		method = null;
		for (Element element : enclosedElements) {
			if (element.getKind() == ElementKind.METHOD) {
				if (element.getSimpleName().toString().equals("foo")) {
					method = (ExecutableElement) element;
				}
			}
		}
		assertNotNull("method should not be null", method);
	}
	public void testGetEnumConstantBody02() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		TypeElement elem = find(rootElements, "EnumShape");
		assertNotNull("TypeElement for enum should not be null", elem);
		List<? extends Element> members = _elementUtils.getAllMembers(elem);
		VariableElement squ = null, cir = null;
		for (Element member : members) {
			if ("SQU".equals(member.getSimpleName().toString())) {
				squ = (VariableElement) member;
			} else if ("CIR".equals(member.getSimpleName().toString())) {
				cir = (VariableElement) member;
			}
		}
		assertNotNull("enum constant should not be null", squ);
		assertNotNull("enum constant should not be null", cir);
		TypeElement enumConstantBody = _elementUtils.getEnumConstantBody(squ);
		assertNull("constant body should be null", enumConstantBody);
		enumConstantBody = _elementUtils.getEnumConstantBody(cir);
		assertNull("constant body should be null", enumConstantBody);
	}
	public void testGetEnumConstantBody03() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		TypeElement elem = find(rootElements, "NonEnum");
		assertNotNull("TypeElement for enum should not be null", elem);
		List<? extends Element> members = _elementUtils.getAllMembers(elem);
		VariableElement xyz = null;
		for (Element member : members) {
			if ("XYZ".equals(member.getSimpleName().toString())) {
				xyz = (VariableElement) member;
			}
		}
		assertNotNull("enum constant should not be null", xyz);
		boolean failed = true;
		try {
			_elementUtils.getEnumConstantBody(xyz);
		} catch (IllegalArgumentException iae) {
			failed = false;
		}
		if (failed)
			reportError("Didn't throw an IllegalArgumentException as expected");
	}
	private TypeElement find(Set<? extends Element> elements, String name) {
		for (Element element : elements) {
			if (name.equals(element.getSimpleName().toString())) {
				return (TypeElement) element;
			}
		}
		return null;
	}
	@Override
	public void reportError(String msg) {
		throw new AssertionFailedError(msg + " [mode = " + this.mode + "]");
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
	protected String getElementsAsString(List<? extends Element> list) {
		StringBuilder builder = new StringBuilder("[");
		for (Element element : list) {
			if (element instanceof PackageElement) {
				builder.append(((PackageElement) element).getQualifiedName());
			} else if (element instanceof ModuleElement) {
				builder.append(((ModuleElement) element).getQualifiedName());
			} else if (element instanceof TypeElement) {
				builder.append(((TypeElement) element).getQualifiedName());
			}  else {
				builder.append(element.getSimpleName());
			}
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
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
	public void assertSame(String msg, Object expected, Object actual) {
		if (expected != actual) {
			reportError(msg + ", should be " + expected.toString() + " but " + actual.toString());
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
	private static class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
}
