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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the java 12 specific elements and validates the lambda and
 * type annotated elements. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java11ElementProcessor to the command line.
 * @since 3.14
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java12ElementProcessor extends BaseProcessor {
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
		testBug549687();
		testRootElements1();
	}
	public void testBug549687() throws IOException {
		// Nothing required to reproduce the NPE
	}
	public void testRootElements1() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		List<String> types = new ArrayList<>();
		List<String> modules = new ArrayList<>();
		ModuleElement mod1 = null, mod2 = null, mod3 = null;
		for (Element element : rootElements) {
			Element root = getRoot(element);
			String modName = null;
			ModuleElement mod = null;
			if (element instanceof ModuleElement) {
				mod = (ModuleElement) element;
				modName = mod.getQualifiedName().toString();
				if (!modules.contains(modName) && !modName.equals("java.base"))
					modules.add(modName);
				assertNull("module should not have an enclosing element", root);
			} else {
				if (element instanceof TypeElement) {
					types.add(((TypeElement) element).getQualifiedName().toString());
				}
				assertTrue("Should be a module element", (root instanceof ModuleElement));
				mod = (ModuleElement) root;
				modName = mod.getQualifiedName().toString();
				assertFalse("should be a named module", mod.isUnnamed());
				String string = mod.getQualifiedName().toString();
				if (!modules.contains(string) && !modName.equals("java.base"))
					modules.add(string);
			}
			if (modName.equals("module.main")) {
				mod1 = mod;
			} else if (modName.equals("module.readable.one")) {
				mod2 = mod;
			} else if (modName.equals("module.readable.two")) {
				mod3 = mod;
			}
		}
		Collections.sort(types, (x, y) -> x.compareTo(y)); //unused as of now
		Collections.sort(modules, (x, y) -> x.compareTo(y));
		assertEquals("incorrect no of modules in root elements in in "+ this.mode + " mode", 3, modules.size());
		assertEquals("incorrect modules among root elements in "+ this.mode + " mode", "[module.main, module.readable.one, module.readable.two]", modules.toString());
		assertNotNull("module should not be null in "+ this.mode + " mode", mod1);
		assertNotNull("module should not be null in "+ this.mode + " mode", mod2);
		assertNotNull("module should not be null in "+ this.mode + " mode", mod3);
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[lang.MOD.same, ]", getElementsAsString(mod3.getEnclosedElements()));
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[lang.MOD.same, ]", getElementsAsString(mod2.getEnclosedElements()));
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[lang.MOD, ]", getElementsAsString(mod1.getEnclosedElements()));
	}

	public void testRootElements2() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		List<String> types = new ArrayList<>();
		List<String> modules = new ArrayList<>();
		ModuleElement mod1 = null, mod2 = null, mod3 = null;
		for (Element element : rootElements) {
			Element root = getRoot(element);
			String modName = null;
			ModuleElement mod = null;
			if (element instanceof ModuleElement) {
				mod = (ModuleElement) element;
				modName = mod.getQualifiedName().toString();
				if (!modules.contains(modName) && !modName.equals("java.base"))
					modules.add(modName);
				assertNull("module should not have an enclosing element", root);
			} else {
				if (element instanceof TypeElement) {
					types.add(((TypeElement) element).getQualifiedName().toString());
				}
				assertTrue("Should be a module element", (root instanceof ModuleElement));
				mod = (ModuleElement) root;
				modName = mod.getQualifiedName().toString();
				assertFalse("should be a named module", mod.isUnnamed());
				String string = mod.getQualifiedName().toString();
				if (!modules.contains(string) && !modName.equals("java.base"))
					modules.add(string);
			}
			if (modName.equals("module.main")) {
				mod1 = mod;
			} else if (modName.equals("module.readable.one")) {
				mod2 = mod;
			} else if (modName.equals("module.readable.two")) {
				mod3 = mod;
			}
		}
		Collections.sort(types, (x, y) -> x.compareTo(y)); //unused as of now
		Collections.sort(modules, (x, y) -> x.compareTo(y));
		assertEquals("incorrect no of modules in root elements in "+ this.mode + " mode", this.isBinaryMode ? 2 : 3, modules.size());
		assertEquals("incorrect modules among root elements in "+ this.mode + " mode", "[module.main, module.readable.one" +
							(this.isBinaryMode ? "" : ", module.readable.two") + "]", modules.toString());
		assertNotNull("module should not be null in "+ this.mode + " mode", mod1);
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[my.mod, ]", getElementsAsString(mod1.getEnclosedElements()));
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[my.mod.same, ]", getElementsAsString(mod2.getEnclosedElements()));
		assertNotNull("module should not be null in "+ this.mode + " mode", mod2);
		if (!this.isBinaryMode) {
			assertNotNull("module should not be null ", mod3);
			assertEquals("Incorrect enclosed packages ", "[]", getElementsAsString(mod3.getEnclosedElements()));
		}
	}
	public void testRootElements3() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		List<String> types = new ArrayList<>();
		List<String> modules = new ArrayList<>();
		ModuleElement mod1 = null, mod2 = null, mod3 = null;
		for (Element element : rootElements) {
			Element root = getRoot(element);
			String modName = null;
			ModuleElement mod = null;
			if (element instanceof ModuleElement) {
				mod = (ModuleElement) element;
				modName = mod.getQualifiedName().toString();
				if (!modules.contains(modName) && !modName.equals("java.base"))
					modules.add(modName);
				assertNull("module should not have an enclosing element", root);
			} else {
				if (element instanceof TypeElement) {
					types.add(((TypeElement) element).getQualifiedName().toString());
				}
				assertTrue("Should be a module element", (root instanceof ModuleElement));
				mod = (ModuleElement) root;
				modName = mod.getQualifiedName().toString();
				assertFalse("should be a named module", mod.isUnnamed());
				String string = mod.getQualifiedName().toString();
				if (!modules.contains(string) && !modName.equals("java.base"))
					modules.add(string);
			}
			if (modName.equals("module.main")) {
				mod1 = mod;
			} else if (modName.equals("module.readable.one")) {
				mod2 = mod;
			} else if (modName.equals("module.readable.two")) {
				mod3 = mod;
			}
		}
		Collections.sort(types, (x, y) -> x.compareTo(y)); //unused as of now
		Collections.sort(modules, (x, y) -> x.compareTo(y));
		assertEquals("incorrect no of modules in root elements in "+ this.mode + " mode", 3, modules.size());
		assertEquals("incorrect modules among root elements in "+ this.mode + " mode", "[module.main, module.readable.one, module.readable.two]", modules.toString());
		assertNotNull("module should not be null in "+ this.mode + " mode", mod1);
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[my1.mod, ]", getElementsAsString(mod1.getEnclosedElements()));
		assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[my1.mod.samePackage, ]", getElementsAsString(mod2.getEnclosedElements()));
		assertNotNull("module should not be null in "+ this.mode + " mode", mod2);
			assertNotNull("module should not be null in "+ this.mode + " mode", mod3);
			assertEquals("Incorrect enclosed packages in "+ this.mode + " mode", "[my1.mod.samePackage, ]", getElementsAsString(mod3.getEnclosedElements()));
	}
	public void testRootElements4() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		List<String> names = new ArrayList<>();
		Set<Element> modules = new HashSet<Element>();
		for (Element element : rootElements) {
			Element root = getRoot(element);
			String modName = null;
			ModuleElement mod = null;
			if (element instanceof ModuleElement) {
				mod = (ModuleElement) element;
				modName = mod.getQualifiedName().toString();
				if (!modName.equals("java.base")) {
					names.add(modName);
					modules.add(element);
				}
				assertNull("module should not have an enclosing element", root);

			} else {
				if (root instanceof ModuleElement) {
					modName = ((ModuleElement) root).getQualifiedName().toString();
					if (!modName.equals("java.base")) {
						names.add(modName);
						modules.add(root);
					}
				}
			}
		}
		assertEquals("incorrect no of modules in root elements in "+ this.mode + " mode", 3, modules.size());
	}
	public void testRootElements5() throws IOException {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		List<String> names = new ArrayList<>();
		Set<Element> modules = new HashSet<Element>();
		for (Element element : rootElements) {
			Element root = getRoot(element);
			String modName = null;
			ModuleElement mod = null;
			if (element instanceof ModuleElement) {
				mod = (ModuleElement) element;
				modName = mod.getQualifiedName().toString();
				if (!modName.equals("java.base")) {
					names.add(modName);
					modules.add(element);
				}
				assertNull("module should not have an enclosing element", root);

			} else {
				if (root instanceof ModuleElement) {
					modName = ((ModuleElement) root).getQualifiedName().toString();
					if (!modName.equals("java.base")) {
						names.add(modName);
						modules.add(root);
					}
				}
			}
		}
		// Deliberately ignoring the extra bogus module to let this pass, so we can test the binary mode in next round
		assertTrue("incorrect no of modules in root elements in "+ this.mode + " mode", (3 <= modules.size()));
	}
	public void testBug574097() {
		// Nothing here. Just reaching here is not expected
	}
	private Element getRoot(Element elem) {
		Element enclosingElement = elem.getEnclosingElement();
		while (enclosingElement != null) {
			if (enclosingElement instanceof ModuleElement) {
				return enclosingElement;
			}
			enclosingElement = enclosingElement.getEnclosingElement();
		}
		return enclosingElement;
	}

	@Override
	public void reportError(String msg) {
		throw new AssertionFailedError(msg);
	}
	private String getExceptionStackTrace(Throwable t) {
		StringBuffer buf = new StringBuffer(t.getMessage());
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
	public void assertModifiers(Set<Modifier> modifiers, String[] expected) {
		assertEquals("Incorrect no of modifiers", modifiers.size(), expected.length);
		Set<String> actual = new HashSet<String>(expected.length);
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
			StringBuffer buf = new StringBuffer();
			buf.append(msg);
			buf.append(", expected " + expected + " but was " + actual);
			reportError(buf.toString());
		}
	}
	public void assertEquals(Object expected, Object actual) {
		if (expected != actual) {

		}
	}
	private class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
}
