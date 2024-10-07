/*******************************************************************************
 * Copyright (c) 2017, 2024 IBM Corporation.
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
import java.io.Writer;
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
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;
import org.eclipse.jdt.compiler.apt.tests.processors.util.TestDirectiveVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * A processor that explores the java 9 specific elements and validates the lambda and
 * type annotated elements. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java9ElementProcessor to the command line.
 * @since 3.14
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java9ElementProcessor extends BaseProcessor {
	boolean reportSuccessAlready = true;
	RoundEnvironment roundEnv = null;
	Messager _messager = null;
	boolean isJre23;
	boolean isJre20;
	boolean isJre19;
	boolean isJre18;
	boolean isJre17;
	boolean isJre12;
	boolean isJre11;
	boolean isJre10;
	int roundNo = 0;
	boolean isJavac;
	boolean binary = false;
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_typeUtils = processingEnv.getTypeUtils();
		_messager = processingEnv.getMessager();
		if (!(processingEnv.getClass().getSimpleName().equals("BatchProcessingEnvImpl"))) {
			this.isJavac = true;
		}
		String property = System.getProperty("java.specification.version");
		if (property.equals(CompilerOptions.VERSION_10)) {
			this.isJre10 = true;
		} else if (property.equals(CompilerOptions.VERSION_11)) {
			this.isJre11 = true;
		} else {
			char c = '.';
			if (property.indexOf(c) == -1) {
				int current = Integer.parseInt(property) + ClassFileConstants.MAJOR_VERSION_0;
				if (current >= ClassFileConstants.MAJOR_VERSION_12) {
					if (current >= ClassFileConstants.MAJOR_VERSION_17) {
						this.isJre17 = true;
						if (current >= ClassFileConstants.MAJOR_VERSION_18) {
							this.isJre18 = true;
							if (current >= ClassFileConstants.MAJOR_VERSION_19) {
								this.isJre19 = true;
								if (current >= ClassFileConstants.MAJOR_VERSION_20) {
									this.isJre20 = true;
									if (current >= ClassFileConstants.MAJOR_VERSION_23) {
	                                    this.isJre23 = true;
	                                }
								}
							}
						}
					} else {
						this.isJre12 = true;
					}
				}
			}
		}
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
			if (options.containsKey("binary")) {
				this.binary = true;
			}
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
		testModuleAnnotation1();
		testModuleElement1();
		testModuleElement2();
		testModuleElement3();
		testModuleElement4();
		testModuleElement5();
		testModuleElement6();
		testModuleElement7();
		testModuleJavaBase1();
		testModuleJavaBase2();
		testModuleJavaBase3();
		testModuleJavaBase4();
		testModuleJavaBase5();
		testModuleTypeMirror1();
		testModuleTypeMirror2();
		testModuleJavaSql1();
		testSourceModule1();
		testSourceModule2();
		testRootElements1();
		testRootElements2();
		testUnnamedModule1();
		testUnnamedModule2();
		testUnnamedModule3();
		testUnnamedModule4();
		testUnnamedModule5();
		testBug522472();
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

	public void testRootElements1() {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		int typeCount = 0;
		int moduleCount = 0;
		List<String> types = new ArrayList<>();
		List<String> modules = new ArrayList<>();
		for (Element element : rootElements) {
			Element root = getRoot(element);
			if (element instanceof ModuleElement) {
				ModuleElement mod = (ModuleElement) element;
				moduleCount++;
				modules.add(mod.getQualifiedName().toString());
				assertNull("module should not have an enclosing element", root);
			} else {
				if (element instanceof TypeElement) {
					typeCount++;
					types.add(((TypeElement) element).getQualifiedName().toString());
				}
				assertTrue("Should be a module element", (root instanceof ModuleElement));
				assertFalse("should be a named module", ((ModuleElement) root).isUnnamed());
			}
		}
		Collections.sort(types, String::compareTo);
		Collections.sort(modules, String::compareTo);
		assertEquals("incorrect no of modules in root elements", 2, moduleCount);
		assertEquals("incorrect modules among root elements", "[mod.a, mod.b]", modules.toString());
		assertEquals("incorrect no of types in root elements", 5, typeCount);
		assertEquals("incorrect types among root elements",
				"[abc.A, abc.internal.A, abc.internal.TypeInAModule, abc.internal.pqr.A, pqr.ext.B]",
				types.toString());
	}
	// Test the types part of root elements get the modules right
	public void testRootElements2() {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		TypeElement type = null;
		ModuleElement modFromRoot = null;
		for (Element element : rootElements) {
			if (element instanceof TypeElement && ((TypeElement) element).getSimpleName().toString().equals("TypeInAModule")) {
				type = (TypeElement) element;
			}
			if (element instanceof ModuleElement && ((ModuleElement) element).getQualifiedName().toString().equals("mod.a")) {
				modFromRoot = (ModuleElement) element;
			}
		}
		assertNotNull("type should not be null", type);
		assertNotNull("module from root elements should not be null", modFromRoot);
		ModuleElement module = _elementUtils.getModuleOf(type);
		assertNotNull("type's module should not be null", module);
		assertEquals("modules should be equals", module, modFromRoot);
	}
	/*
	 * Test module element can be retrieved and
	 * annotations on module declarations can be retrieved
	 */
	public void testModuleAnnotation1() {
		CharSequence name = "mod.a";
		ModuleElement mod = _elementUtils.getModuleElement(name);
		assertNotNull("Module element should not be null", mod);
		List<? extends AnnotationMirror> annotationMirrors = mod.getAnnotationMirrors();
		assertNotNull("Should not be null", annotationMirrors);
		verifyAnnotations(mod, new String[]{"@java.lang.Deprecated()"});
		List<? extends Element> enclosedElements = mod.getEnclosedElements();
		PackageElement pack = null;
		for (Element element : enclosedElements) {
			if (element instanceof PackageElement) {
				pack = (PackageElement) element;
				break;
			}
		}
		assertNotNull("Package not found", pack);
		Element elem = pack.getEnclosingElement();
		assertNotNull("Parent not found", elem);
		assertTrue("Parent should be a module", (elem instanceof ModuleElement));
		assertEquals("Incorrect module element", "mod.a", ((ModuleElement) elem).getQualifiedName().toString());
	}
	/*
	 * Test module element can be retrieved and attributed are
	 * verified against declaration
	 */
	public void testModuleElement1() {
		CharSequence name = "mod.a";
		ModuleElement mod = _elementUtils.getModuleElement(name);
		assertEquals("incorrect qualified name", "mod.a", mod.getQualifiedName().toString());
		assertEquals("incorrect simple name", "a", mod.getSimpleName().toString());
		List<? extends Element> enclosedElements = mod.getEnclosedElements();
		PackageElement pack = null;
		for (Element element : enclosedElements) {
			if (element instanceof PackageElement && ((PackageElement) element).getQualifiedName().toString().equals("abc.internal")) {
				pack = (PackageElement) element;
				break;
			}
		}
		assertNotNull("Package not found", pack);
		Element elem = pack.getEnclosingElement();
		assertNotNull("Parent not found", elem);
		assertTrue("Parent should be a module", (elem instanceof ModuleElement));
		assertEquals("Incorrect module element", "mod.a", ((ModuleElement) elem).getQualifiedName().toString());
	}
	/*
	 * Test type elements can be retrieved from Elements API with and without
	 * the context of the module the type is in.
	 */
	public void testModuleElement2() {
		CharSequence name = "mod.a";
		ModuleElement mod = _elementUtils.getModuleElement(name);
		TypeElement typeElement = _elementUtils.getTypeElement(mod, "abc.internal.TypeInAModule");
		assertNotNull("Type should not be null", typeElement);
		typeElement = _elementUtils.getTypeElement("abc.internal.TypeInAModule");
		assertNotNull("Type should not be null", typeElement);
		ModuleElement m = _elementUtils.getModuleOf(typeElement);
		assertEquals("modules should be same", mod, m);
		ModuleElement mElement = _elementUtils.getModuleOf(typeElement);
		assertNotNull("module should not be null", mElement);
		assertEquals("Incorrect module element", "mod.a", mElement.getQualifiedName().toString());
	}
	/*
	 * Test binary modules from JRT system can be loaded and its attributes
	 * as expected
	 */
	public void testModuleElement3() {
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		ModuleElement compiler = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
			if (moduleElement.getQualifiedName().toString().equals("java.compiler")) {
				compiler = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		assertNotNull("java.compiler module null", compiler);
		assertNull("Enclosing element should be null", base.getEnclosingElement());
		assertEquals("Incorrect element kind", ElementKind.MODULE, base.getKind());
		assertFalse("Should be named", base.isUnnamed());
		assertFalse("Should not be open", base.isOpen());

	}
	/*
	 * Test packages can be retrieved with the Elements API with and without
	 * the context of its module.
	 */
	public void testModuleElement4() {
		CharSequence name = "mod.a";
		ModuleElement mod = _elementUtils.getModuleElement(name);
		PackageElement pElement = _elementUtils.getPackageElement("abc.internal");
		assertNotNull("Package should not be null", pElement);
		pElement = _elementUtils.getPackageElement(mod, "abc.internal");
		assertNotNull("Package should not be null", pElement);
		ModuleElement mElement = _elementUtils.getModuleOf(pElement);
		assertNotNull("module should not be null", mElement);
		assertEquals("Incorrect module element", "mod.a", mElement.getQualifiedName().toString());
		assertEquals("Modules should be same", mod, mElement);
	}
	/*
	 * Test packages can be retrieved with Elements API and they contain
	 * the right module element.
	 */
	public void testModuleElement5() {
		CharSequence name = "mod.a";
		ModuleElement mod = _elementUtils.getModuleElement(name);
		Set<? extends PackageElement> allPackageElements = _elementUtils.getAllPackageElements("abc.internal.pqr");
		assertEquals("Incorrect no of packages", 1, allPackageElements.size());
		PackageElement pElement = null;
		for (PackageElement packageElement : allPackageElements) {
			pElement = packageElement;
		}
		assertNotNull("Package should not be null", pElement);
		ModuleElement mElement = _elementUtils.getModuleOf(pElement);
		assertNotNull("module should not be null", mElement);
		assertEquals("Incorrect module element", "mod.a", mElement.getQualifiedName().toString());
		assertEquals("Modules should be same", mod, mElement);

		allPackageElements = _elementUtils.getAllPackageElements("abc");
		assertEquals("Incorrect no of packages", 2, allPackageElements.size());
		List<ModuleElement> mods = new ArrayList<>();
		for (PackageElement packageElement : allPackageElements) {
			mElement = _elementUtils.getModuleOf(packageElement);
			mods.add(mElement);
		}
		assertEquals("incorrect no of modules", 2, mods.size());
		mods.remove(mod);
		mod = _elementUtils.getModuleElement("mod.b");
		assertNotNull("mod b should not be null", mod);
		mods.remove(mod);
		assertEquals("incorrect no of modules", 0, mods.size());
	}
	/*
	 * Test type elements can be loaded and contain the correct module
	 * elements
	 */
	public void testModuleElement6() {
		CharSequence name = "mod.a";
		ModuleElement mod = _elementUtils.getModuleElement(name);
		Set<? extends TypeElement> typeElements = _elementUtils.getAllTypeElements("abc.internal.A");
		assertNotNull("Type should not be null", typeElements);
		assertEquals("Incorrect no of types", 1, typeElements.size());
		TypeElement tElement = null;
		for (TypeElement typeElement : typeElements) {
			tElement = typeElement;
		}
		assertNotNull("Package should not be null", tElement);
		ModuleElement mElement = _elementUtils.getModuleOf(tElement);
		assertNotNull("module should not be null", mElement);
		assertEquals("Incorrect module element", "mod.a", mElement.getQualifiedName().toString());
		assertEquals("Modules should be same", mod, mElement);
	}
	/*
	 * Test that a module not part of the root modules can NOT be retrieved.
	 */
	public void testModuleElement7() {
		// test that a random module from system unrelated to the module we are compiling is not loaded by the compiler
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement mod = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.desktop")) {
				mod = moduleElement;
			}
		}
		assertNull("module java.desktop should not be found", mod);
	}
	/*
	 * Test java.base module can be loaded and verify its exports attributes
	 */
	public void testModuleJavaBase1() {
		try {
			SourceVersion.valueOf("RELEASE_10");
		} catch(IllegalArgumentException iae) {
		}
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		List<? extends Directive> directives = base.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.EXPORTS);
		assertTrue("missing exports", filterDirective.size() > 100);
		ExportsDirective pack = null;
		for (Directive directive : filterDirective) {
			ModuleElement.ExportsDirective exports = (ExportsDirective) directive;
			if (exports.getPackage().getQualifiedName().toString().equals("sun.reflect.annotation")) {
				pack = exports;
				break;
			}
		}
		assertNotNull("Package export not found", pack);
		List<? extends ModuleElement> targetModules = pack.getTargetModules();
		assertEquals("incorrect no of targets", 1, targetModules.size());
		ModuleElement mod = targetModules.get(0);
		assertEquals("incorrect module element", "jdk.compiler", mod.getQualifiedName().toString());
	}
	/*
	 * Test java.base module can be loaded and verify its requires attributes
	 */
	public void testModuleJavaBase2() {
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		List<? extends Directive> directives = base.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.REQUIRES);
		assertEquals("Incorrect no of requires", 0, filterDirective.size());
	}
	/*
	 * Test java.base module can be loaded and verify its 'opens' attributes
	 */
	public void testModuleJavaBase3() {
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		List<? extends Directive> directives = base.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.OPENS);
		assertEquals("incorrect no of opens", 0 , filterDirective.size());
	}
	/*
	 * Test java.base module can be loaded and verify its 'uses' attributes
	 */
	public void testModuleJavaBase4() {
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		List<? extends Directive> directives = base.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.USES);
		int modCount =  (this.isJre11 || this.isJre12) ? 33 : (this.isJre18 ? (this.isJre20 ? (this.isJre23 ? 35 : 36) : 35) : 34);
		assertEquals("incorrect no of uses", modCount, filterDirective.size());
	}
	/*
	 * Test java.base module can be loaded and verify its 'provides' attributes
	 */
	public void testModuleJavaBase5() {
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		List<? extends Directive> directives = base.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.PROVIDES);
		assertEquals("incorrect no of provides", ((isJre17 && !isJre23) ? (this.isJavac ? 4 : 2) : 1), filterDirective.size());
		ProvidesDirective provides = (ProvidesDirective) filterDirective.get(0);
		assertEquals("incorrect service name", "java.nio.file.spi.FileSystemProvider", provides.getService().getQualifiedName().toString());
		List<? extends TypeElement> implementations = provides.getImplementations();
		assertEquals("incorrect no of implementations", 1 , implementations.size());
		TypeElement typeElement = implementations.get(0);
		assertEquals("incorrect implementation name", "jdk.internal.jrtfs.JrtFileSystemProvider", typeElement.getQualifiedName().toString());
	}
	public void testModuleTypeMirror1() {
		ModuleElement base = _elementUtils.getModuleElement("java.base");
		assertNotNull("java.base module null", base);
		TypeMirror asType = base.asType();
		assertNotNull("module type should not be null", asType);
		assertEquals("incorrect type kind", TypeKind.MODULE, asType.getKind());
		assertEquals("must be a NoType", (asType instanceof NoType));
	}
	public void testModuleTypeMirror2() {
		ModuleElement base = _elementUtils.getModuleElement("mod.a");
		assertNotNull("mod.a module null", base);
		TypeMirror asType = base.asType();
		assertNotNull("module type should not be null", asType);
		verifyAnnotations(asType, new String[]{});
	}
	/*
	 * Test java.sql module can be loaded and verify its requires attributes
	 */
	public void testModuleJavaSql1() {
		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.sql")) {
				base = moduleElement;
				break;
			}
		}
		assertNotNull("java.sql module null", base);
		List<? extends Directive> directives = base.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.REQUIRES);
		assertEquals("Incorrect no of requires", (this.isJre11 || this.isJre12 || this.isJre17) ? 4 : 3, filterDirective.size());
		RequiresDirective req = null;
		for (Directive directive : filterDirective) {
			if (((RequiresDirective) directive).getDependency().getQualifiedName().toString().equals("java.logging")) {
				req = (RequiresDirective) directive;
				break;
			}
		}
		assertNotNull("dependency on java.logging not found", req);
		assertTrue("dependency should be transitive", req.isTransitive());
	}
	/*
	 * Test a source module can be retrieved and verify its requires attributes
	 */
	public void testSourceModule1() {
		ModuleElement mod = _elementUtils.getModuleElement("mod.a");
		assertNotNull("mod.a module null", mod);
		List<? extends Directive> directives = mod.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.REQUIRES);
		assertEquals("Incorrect no of requires", 3, filterDirective.size());
		RequiresDirective reqCompiler = null;
		RequiresDirective reqSql = null;
		for (Directive directive : filterDirective) {
			if (((RequiresDirective) directive).getDependency().getQualifiedName().toString().equals("java.compiler")) {
				reqCompiler = (RequiresDirective) directive;
			}
			if (((RequiresDirective) directive).getDependency().getQualifiedName().toString().equals("java.sql")) {
				reqSql = (RequiresDirective) directive;
			}
		}
		assertNotNull("dependency on java.sql not found", reqSql);
		assertNotNull("dependency on java.sql not found", reqCompiler);
		assertTrue("dependency should be transitive", reqSql.isTransitive());
		assertTrue("dependency should be transitive", reqCompiler.isTransitive());
	}
	/*
	 * Test a source module can be retrieved and verify its requires attributes
	 */
	public void testSourceModule2() {
		ModuleElement mod = _elementUtils.getModuleElement("mod.b");
		assertNotNull("mod.b module null", mod);
		List<? extends Directive> directives = mod.getDirectives();
		List<Directive> filterDirective = filterDirective(directives, DirectiveKind.REQUIRES);
		assertEquals("Incorrect no of requires", 2, filterDirective.size());
		RequiresDirective reqCompiler = null;
		RequiresDirective reqSql = null;
		RequiresDirective reqA = null;
		for (Directive directive : filterDirective) {
			if (((RequiresDirective) directive).getDependency().getQualifiedName().toString().equals("java.compiler")) {
				reqCompiler = (RequiresDirective) directive;
			}
			if (((RequiresDirective) directive).getDependency().getQualifiedName().toString().equals("java.sql")) {
				reqSql = (RequiresDirective) directive;
			}
			if (((RequiresDirective) directive).getDependency().getQualifiedName().toString().equals("mod.a")) {
				reqA = (RequiresDirective) directive;
			}
		}
		assertNull("dependency on java.sql should not be visible", reqSql);
		assertNull("dependency on java.compiler should not be visible", reqCompiler);
		assertNotNull("dependency on mod.a not found", reqA);
		assertFalse("dependency should not be transitive", reqA.isTransitive());
	}
	public void testUnnamedModule1() {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		ModuleElement mod = null;
		for (Element element : rootElements) {
			if (element instanceof TypeElement) {
				mod = _elementUtils.getModuleOf(element);
				break;
			}
		}
		assertNotNull("module should not be null", mod);
		assertTrue("module should be unnamed", mod.isUnnamed());
		List<? extends Element> enclosedElements = mod.getEnclosedElements();
		List<? extends Directive> directives = mod.getDirectives();
		assertEquals("incorrect no of directives", 0, directives.size());
		List<Element> filterElements = filterElements(enclosedElements, ElementKind.PACKAGE);
		assertEquals("incorrect no of packages", 4, filterElements.size());
		// FIXME: Note Javac fails here as well
//		PackageElement packageOf = _elementUtils.getPackageOf(mod);
//		assertNotNull("package should not be null", packageOf);
	}
	public void testUnnamedModule2() {
		Set<? extends PackageElement> allPackageElements = _elementUtils.getAllPackageElements("targets.model9.p");
		assertEquals("incorrect no of packages", 1, allPackageElements.size());
		TypeElement typeElement =_elementUtils.getTypeElement("targets.model9.p.A");
		assertNotNull("Type should not be null", typeElement);
		ModuleElement m = _elementUtils.getModuleOf(typeElement);
		assertEquals("module should be unnamed", "", m.getQualifiedName().toString());
	}
	public void testUnnamedModule3() {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		ModuleElement moduleElement = _elementUtils.getModuleElement("");
		assertNotNull("module should not be null", moduleElement);
		ModuleElement mod = null;
		for (Element element : rootElements) {
			if (element instanceof TypeElement) {
				mod = (ModuleElement) element.getEnclosingElement().getEnclosingElement();
				break;
			}
		}
		assertEquals("modules should be equal", mod, moduleElement);
		assertNotNull("module should not be null", mod);
		List<Element> filterElements = filterElements(mod.getEnclosedElements(), ElementKind.PACKAGE);
		assertEquals("incorrect no of packages", 1, filterElements.size());
	}
	public void testUnnamedModule4() {
		ModuleElement moduleElement = _elementUtils.getModuleElement("");
		assertNotNull("module should not be null", moduleElement);
		List<Element> filterElements = filterElements(moduleElement.getEnclosedElements(), ElementKind.PACKAGE);
		PackageElement pack = (PackageElement) filterElements.get(0);
		assertEquals("incorect package", "targets.model9a.internal", pack.getQualifiedName().toString());
		List<? extends Element> enclosedElements = pack.getEnclosedElements();
		assertEquals("incorrect no of types", 2, enclosedElements.size());
	}
	public void testUnnamedModule5() {
		ModuleElement moduleElement = _elementUtils.getModuleElement("");
		assertNotNull("module should not be null", moduleElement);
		List<Element> filterElements = filterElements(moduleElement.getEnclosedElements(), ElementKind.PACKAGE);
		PackageElement pack = (PackageElement) filterElements.get(0);
		assertEquals("incorect package", "targets.model9x", pack.getQualifiedName().toString());
		List<? extends Element> enclosedElements = pack.getEnclosedElements();
		assertEquals("incorrect no of types", 1, enclosedElements.size());
	}
	public void testBug521723() {
		//		private int foo1(int i) { return i; }
		//		default int foo2(int i) {return foo(i); }
		//		public default void foo3() {}
		//		static void foo4() {}
		//		private static void foo5() {}
		//		public static void foo6() {}
		Modifier[] f1 = new Modifier[] {Modifier.PRIVATE};
		Modifier[] f2 = new Modifier[] {Modifier.PUBLIC, Modifier.DEFAULT};
		Modifier[] f3 = f2;
		Modifier[] f4 = new Modifier[] {Modifier.STATIC, Modifier.PUBLIC};
		Modifier[] f5 = new Modifier[] {Modifier.PRIVATE, Modifier.STATIC};
		Modifier[] f6 = f4;
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		TypeElement t = null;
		for (Element element : rootElements) {
			if (element instanceof TypeElement) {
				if (((TypeElement) element).getQualifiedName().toString().equals("targets.bug521723.I")) {
					t = (TypeElement) element;
				}
			}
		}
		assertNotNull("type should not be null", t);
		List<? extends Element> enclosedElements = t.getEnclosedElements();
		for (Element element : enclosedElements) {
			if (element instanceof ExecutableElement) {
				String string = element.getSimpleName().toString();
				if (string.equals("foo1")) {
					validateModifiers((ExecutableElement) element, f1);
				} else if (string.equals("foo2")) {
					validateModifiers((ExecutableElement) element, f2);
				} else if (string.equals("foo3")) {
					validateModifiers((ExecutableElement) element, f3);
				} else if (string.equals("foo4")) {
					validateModifiers((ExecutableElement) element, f4);
				} else if (string.equals("foo5")) {
					validateModifiers((ExecutableElement) element, f5);
				} else if (string.equals("foo6")) {
					validateModifiers((ExecutableElement) element, f6);
				}
			}
		}

	}
	public void testDirectiveVisitor() {
		ModuleElement mod = _elementUtils.getModuleElement("mod.b");
		assertNotNull("mod.b module null", mod);
		try {
			TestDirectiveVisitor<Object, Object> t = new TestDirectiveVisitor<>();
			List<? extends Directive> directives = mod.getDirectives();
			for (Directive directive : directives) {
				Object result = t.visit(directive);
				assertSame("Objects should be same", result, directive);
			}

		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	public void testTypesImpl() {
		ModuleElement mod = _elementUtils.getModuleElement("mod.a");
		Set<? extends TypeElement> typeElements = _elementUtils.getAllTypeElements("abc.internal.A");
		assertNotNull("mod.b module null", mod);
		assertNotNull("Type should not be null", typeElements);
		assertEquals("Incorrect no of types", 1, typeElements.size());
		TypeElement tElement = null;
		for (TypeElement typeElement : typeElements) {
			tElement = typeElement;
		}
		assertNotNull("Package should not be null", tElement);
		TypeMirror t = tElement.asType();
		boolean exception = false;
		try {
			_typeUtils.erasure(mod.asType());
		} catch(IllegalArgumentException iae) {
			exception = true;
		}
		assertTrue("Exception not thrown", exception);
		exception = false;
		try {
			_typeUtils.capture(mod.asType());
		} catch(IllegalArgumentException iae) {
			exception = true;
		}
		assertTrue("Exception not thrown", exception);
		exception = false;
		try {
			_typeUtils.directSupertypes(mod.asType());
		} catch(IllegalArgumentException iae) {
			exception = true;
		}
		assertTrue("Exception not thrown", exception);
		exception = false;
		try {
			_typeUtils.isSubtype(mod.asType(), t);
		} catch(IllegalArgumentException iae) {
			exception = true;
		}
		assertTrue("Exception not thrown", exception);
		exception = false;
		try {
			_typeUtils.isAssignable(mod.asType(), t);
		} catch(IllegalArgumentException iae) {
			exception = true;
		}
		assertTrue("Exception not thrown", exception);
		exception = false;
		try {
			_typeUtils.contains(mod.asType(), t);
		} catch(IllegalArgumentException iae) {
			exception = true;
		}
		assertTrue("Exception not thrown", exception);
	}
	public void testBug498022a() {
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		TypeElement type = null;
		for (Element element : rootElements) {
			if (element.getSimpleName().toString().equals("Main")) {
				type = (TypeElement) element;
				break;
			}
		}
		VariableElement field = null;
		if (type != null) {
			List<? extends Element> members = _elementUtils.getAllMembers(type);
			for (Element member : members) {
				if ("someField".equals(member.getSimpleName().toString())) {
					field = (VariableElement) member;
				}
			}
		}
		assertNotNull("field should not be null", field);
		TypeMirror asType = field.asType();
		DeclaredType declaredType = (DeclaredType) asType;
		Element asElement = declaredType.asElement();
		verifyAnnotations(asElement, new String[] {"@org.eclipse.jdt.compiler.apt.tests.annotations.Type(value=c)"});
	}
	public void testBug498022b() {
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		TypeElement type = null;
		TypeElement anotherType = null;
		for (Element element : rootElements) {
			if (element.getSimpleName().toString().equals("OtherAnnotatedClass")) {
				type = (TypeElement) element;
			} else if (element.getSimpleName().toString().equals("SomeAnnotatedClass")) {
				anotherType = (TypeElement) element;
			}
		}
		verifyAnnotations(type, new String[] {"@targets.model9.q.FooBarAnnotation()"});
		verifyAnnotations(anotherType, new String[] {"@targets.model9.q.FooBarAnnotation(otherClasses=[targets.model9.q.OtherAnnotatedClass.class,])"});
		List<? extends AnnotationMirror> annots = anotherType.getAnnotationMirrors();
		AnnotationMirror annotationMirror = annots.get(0);
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationMirror.getElementValues();
		AnnotationValue value = null;
		Set<? extends ExecutableElement> keys = values.keySet();
		for (ExecutableElement executableElement : keys) {
			if (executableElement.getSimpleName().toString().equals("otherClasses"))
				value = values.get(executableElement);
		}
		assertNotNull("value should not be null", value);
		@SuppressWarnings("rawtypes")
		List list = (List) value.getValue();
		assertEquals("Incorrect no of values", 1, list.size());
		AnnotationValue annotVal = (AnnotationValue) list.get(0);
		DeclaredType declaredType = (DeclaredType) annotVal.getValue();
		TypeElement typeEl = (TypeElement) declaredType.asElement();
		verifyAnnotations(typeEl, new String[] {"@targets.model9.q.FooBarAnnotation()"});
	}
	public boolean testBug535819() {
		if (++roundNo == 1) {
			this.reportSuccessAlready = false;
			try {
				TypeElement annotatedType = _elementUtils.getTypeElement("targets.bug535819.Entity1");
				Filer filer = processingEnv.getFiler();
				JavaFileObject jfo = filer.createSourceFile("targets.bug535819.query.QEntity1", annotatedType);
				Writer writer = jfo.openWriter();
				writer.write("package targets.bug535819.query;\n" +
						"  \n" +
						"import targets.bug535819.Entity1;\n" +
						"public class QEntity1 {\n" +
						"  private static final QEntity1 _alias = new QEntity1(true);\n" +
						"  public QEntity1() {\n" +
						"    super(Entity1.class);\n" +
						"  }\n" +
						"  private QEntity1(boolean dummy) {\n" +
						"    super(dummy);\n" +
						"  }\n" +
						"  public static class Alias {\n" +
						"  }\n" +
						"}");
				writer.close();

				jfo = filer.createSourceFile("targets.bug535819.assoc.QAssocEntity1", annotatedType);
				writer = jfo.openWriter();
				writer.write("package targets.bug535819.query.assoc;\n" +
						"  \n" +
						"import targets.bug535819.Entity1;\n" +
						"import targets.bug535819.query.QEntity1;\n" +
						"public class QAssocEntity1<R>  {\n" +
						"  public QAssocEntity1(String name, R root) {\n" +
						"    super(name, root);\n" +
						"  }\n" +
						"}\n" +
						"");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//System.setProperty(this.getClass().getName(), "Processor did not fully do the job");
		} else if (roundNo == 2){
			this.reportSuccessAlready = true;
		}
		return false;
	}
	public void testBug572673() {
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		Set<ModuleElement> modulesIn = ElementFilter.modulesIn(rootElements);
		assertEquals("incorrect modules" , 1, modulesIn.size());
		boolean found = false;
		for (ModuleElement moduleElement : modulesIn) {
			if (moduleElement.getQualifiedName().toString().equals("mod.one")) {
				found = true;
				List<? extends Directive> directives = moduleElement.getDirectives();
				List<Directive> requires = filterDirective(directives, DirectiveKind.REQUIRES);
				assertEquals("incorrect requires" , 2, requires.size());
				for (Directive r : requires) {
					RequiresDirective req = (RequiresDirective) r;
					ModuleElement depModule = req.getDependency();
					if (_elementUtils.isAutomaticModule(depModule)) {
						assertEquals("incorrect auto-module", "lib.x", depModule.getQualifiedName().toString());
					} else {
						assertEquals("incorrect non auto-module", "java.base", depModule.getQualifiedName().toString());
					}
				}
			}
		}
		assertTrue("module not found", found);
	}
	public void testBug522472() {
		Set<? extends Element> rootElements = this.roundEnv.getRootElements();
		ModuleElement module = null;
		for (Element element : rootElements) {
			if (element instanceof ModuleElement) {
				ModuleElement mod = (ModuleElement) element;
				if (mod.getQualifiedName().toString().equals("mod.a")) {
					module = mod;
					break;
				}
			} else if (element instanceof TypeElement) {
				Element root = getRoot(element);
				if (root instanceof ModuleElement) {
					ModuleElement mod = (ModuleElement) root;
					if (mod.getQualifiedName().toString().equals("mod.a")) {
						module = mod;
						break;
					}
				}
			}
		}
		assertNotNull("module should not be null", module);
		List<? extends Element> elements = module.getEnclosedElements();
		assertEquals("incorrect no of elements", 2, elements.size());
		List<Element> packages = filterElements(elements, ElementKind.PACKAGE);
//		ECJ fails the following tests. 
		for (Element element : packages) {
			Element enclosingElement = element.getEnclosingElement();
			assertNotNull("module should not be null", enclosingElement);
			assertEquals("module should be same", enclosingElement, module);
		}
		assertEquals("incorrect packages count", 2, packages.size());
		List<? extends Directive> directives = module.getDirectives();
		assertEquals("incorrect no of directives", 3, directives.size());
		List<Directive> exports = filterDirective(directives, DirectiveKind.EXPORTS);
		assertEquals("incorrect exports count", 2, exports.size());
	}
	public void testGetFileObjectOf() {
		TypeElement typeElement =_elementUtils.getTypeElement("abc.internal.A");
		assertNotNull("Type should not be null", typeElement);
		JavaFileObject fo = _elementUtils.getFileObjectOf(typeElement);
		assertNotNull("file object should not be null", fo);
		assertEquals("Incorrect kind", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		String expectedUri = "mod.a/abc/internal/A." + (this.binary ? "class" : "java");
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);
		if (!this.isJavac) {
			// Javac returns a null from getNestingKind()
			assertEquals("Incorrect nesting kind", NestingKind.TOP_LEVEL, fo.getNestingKind());
		}
		TypeElement innerType = null;
		List<? extends Element> inner = typeElement.getEnclosedElements();
		for (Element element : inner) {
			if (element.getKind() == ElementKind.CLASS && element.getSimpleName().toString().equals("InnerTypeInAModule")) {
				innerType = (TypeElement) element;
			}
		}
		fo = _elementUtils.getFileObjectOf(innerType);
		assertNotNull("file object should not be null", fo);
		assertEquals("Incorrect kind", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = this.binary ? "A$InnerTypeInAModule.class" : "mod.a/abc/internal/A.java";
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		ModuleElement m = _elementUtils.getModuleOf(typeElement);
		assertNotNull("Module should not be null", m);
		fo = _elementUtils.getFileObjectOf(m);
		assertNotNull("file object should not be null", fo);
		assertEquals("Incorrect kind", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = "mod.a/module-info." + (this.binary ? "class" : "java");
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		PackageElement p = _elementUtils.getPackageOf(typeElement);
		assertNotNull("Module should not be null", p);
		fo = _elementUtils.getFileObjectOf(p);
		// TODO: Javac fails this as of now. So, exclude it
		if (!this.isJavac) {
			assertNotNull("file object should not be null", fo);
			assertEquals("Incorrect kind", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
			expectedUri = "mod.a/abc/internal/package-info." + (this.binary ? "class" : "java");
			assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);
		}

		Set<? extends ModuleElement> allModuleElements = _elementUtils.getAllModuleElements();
		ModuleElement base = null;
		for (ModuleElement moduleElement : allModuleElements) {
			if (moduleElement.getQualifiedName().toString().equals("java.base")) {
				base = moduleElement;
			}
		}
		assertNotNull("java.base module null", base);
		fo = _elementUtils.getFileObjectOf(base);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", JavaFileObject.Kind.CLASS, fo.getKind());
		expectedUri = "java.base/module-info.class";
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);
		VariableElement field = null;
		ExecutableElement method = null;
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		for (Element element : enclosedElements) {
			if (element.getKind() == ElementKind.FIELD && element.getSimpleName().toString().equals("aField")) {
				field = (VariableElement) element;
			} else if (element.getKind() == ElementKind.METHOD && element.getSimpleName().toString().equals("aMethod")) {
				method = (ExecutableElement) element;
			}
		}
		assertNotNull("Field should not be null", field);
		fo = _elementUtils.getFileObjectOf(field);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = "mod.a/abc/internal/A." + (this.binary ? "class" : "java");
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		assertNotNull("Method should not be null", method);
		fo = _elementUtils.getFileObjectOf(method);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = "mod.a/abc/internal/A." + (this.binary ? "class" : "java");
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);
	}
	public void testGetFileObjectOfRecords() {
		TypeElement typeElement =_elementUtils.getTypeElement("xyz.NestedRecord");
		assertNotNull("Type should not be null", typeElement);
		JavaFileObject fo = _elementUtils.getFileObjectOf(typeElement);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		String expectedUri = "xyz/NestedRecord." + (this.binary ? "class" : "java");
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);
		List<? extends Element> inner = typeElement.getEnclosedElements();
		TypeElement recordEl = null;
		TypeElement enumEl = null;
		for (Element element : inner) {
			if (element.getKind() == ElementKind.RECORD && element.getSimpleName().toString().equals("Point")) {
				recordEl = (TypeElement) element;
			} else if (element.getKind() == ElementKind.ENUM && element.getSimpleName().toString().equals("Color")) {
				enumEl = (TypeElement) element;
			}
		}
		assertNotNull("Type should not be null", recordEl);
		fo = _elementUtils.getFileObjectOf(recordEl);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = this.binary ? "xyz/NestedRecord$Point.class" : "xyz/NestedRecord.java";
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		assertNotNull("Type should not be null", enumEl);
		fo = _elementUtils.getFileObjectOf(enumEl);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = this.binary ? "xyz/NestedRecord$Color.class" : "xyz/NestedRecord.java";
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		typeElement =_elementUtils.getTypeElement("xyz.JavaFileWithManyClasses");
		assertNotNull("Type should not be null", typeElement);
		fo = _elementUtils.getFileObjectOf(typeElement);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = "xyz/JavaFileWithManyClasses." + (this.binary ? "class" : "java");
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		typeElement =_elementUtils.getTypeElement("xyz.AnotherClass");
		assertNotNull("Type should not be null", typeElement);
		fo = _elementUtils.getFileObjectOf(typeElement);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = this.binary ? "xyz/AnotherClass.class" : "xyz/JavaFileWithManyClasses.java";
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);

		typeElement =_elementUtils.getTypeElement("xyz.AnotherInterface");
		assertNotNull("Type should not be null", typeElement);
		fo = _elementUtils.getFileObjectOf(typeElement);
		assertNotNull("file object should not be null", fo);
		assertEquals("should be of kind source", this.binary ? JavaFileObject.Kind.CLASS : JavaFileObject.Kind.SOURCE, fo.getKind());
		expectedUri = this.binary ? "xyz/AnotherInterface.class" : "xyz/JavaFileWithManyClasses.java";
		assertEndsWith("Incorrect path", fo.toUri().toString(), expectedUri);
		if (!this.binary) {
			Element anotherCls = null;
			Element anotherInt = null;		
			for (Element e : this.roundEnv.getRootElements()) {
				if ("AnotherClass".equals(e.getSimpleName().toString())) {
					anotherCls = e;
				} else if ("AnotherInterface".equals(e.getSimpleName().toString())) {
					anotherInt = e;
				}
			}
			JavaFileObject fileObject = _elementUtils.getFileObjectOf(anotherCls);
			if (!fileObject.getName().contains("JavaFileWithManyClasses")) {
				reportError("Incorrect FileObject name. Expected to contain JavaFileWithManyClasses"
						+ " but was " + fileObject.getName());
			}
			fileObject = _elementUtils.getFileObjectOf(anotherInt);
			if (!fileObject.getName().contains("JavaFileWithManyClasses")) {
				reportError("Incorrect FileObject name. Expected to contain JavaFileWithManyClasses"
						+ " but was " + fileObject.getName());
			}
		}
	}
	public void testElementsInType() {
		TypeElement topType = _elementUtils.getTypeElement("xyz.TypeWithManyElements");
		JavaFileObject fileObject = _elementUtils.getFileObjectOf(topType);
		String topTypeName = "TypeWithManyElements";
		if (!fileObject.getName().contains(topTypeName)) {
			reportError("Incorrect FileObject name. Expected to contain " + topTypeName + 
					" but was " + fileObject.getName());
		}
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(topType.getEnclosedElements());
		List<ExecutableElement> methods = ElementFilter.methodsIn(topType.getEnclosedElements());
		List<VariableElement> fields = ElementFilter.fieldsIn(topType.getEnclosedElements());
		// Fields
		for (VariableElement field : fields) {
			fileObject = _elementUtils.getFileObjectOf(field);
			if (!fileObject.getName().contains(topTypeName)) {
				reportError("Incorrect FileObject name. Expected to contain " + topTypeName + 
						" but was " + fileObject.getName());
			}
		}
		// Constructors
		for (ExecutableElement constructor : constructors) {
			fileObject = _elementUtils.getFileObjectOf(constructor);
			if (!fileObject.getName().contains(topTypeName)) {
				reportError("Incorrect FileObject name. Expected to contain " + topTypeName + 
					" but was " + fileObject.getName());
			}
		}
		// Methods and Parameters
		for (ExecutableElement method : methods) {
			fileObject = _elementUtils.getFileObjectOf(method);
			if (!fileObject.getName().contains(topTypeName)) {
				reportError("Incorrect FileObject name. Expected to contain " + topTypeName + 
						" but was " + fileObject.getName());
			}
			List<? extends VariableElement> methodParams = method.getParameters();
			for (VariableElement param : methodParams) {
				JavaFileObject fileObjectForParam = _elementUtils.getFileObjectOf(param);
				if (!fileObjectForParam.getName().contains(topTypeName)) {
					reportError("Incorrect FileObject name. Expected to contain " + topTypeName + 
							" but was " + fileObject.getName());
				}
			}
		}

	}
	public void testDeeplyNestedTypes() {
		final String topmost = "xyz.MultiNestedType";
		TypeElement topTypeElement = _elementUtils.getTypeElement(topmost);
		TypeElement[] types = { topTypeElement, 
				_elementUtils.getTypeElement("xyz.NestedRecord"),
				_elementUtils.getTypeElement("xyz.NestedEnum"), 
				_elementUtils.getTypeElement("xyz.NestedTypes") 
		};
		_elementUtils.getFileObjectOf(topTypeElement);
		for (TypeElement element : types) {
			validateInnerElements(element);
		}
	}
	private void validateInnerElements(TypeElement e) {
		List<? extends Element> members = _elementUtils.getAllMembers(e);
		List<TypeElement> types = ElementFilter.typesIn(members);
		for (TypeElement t : types) {
			String topTypeName = _elementUtils.getOutermostTypeElement(t).getSimpleName().toString();
			if (!_elementUtils.getFileObjectOf(t).getName().contains(topTypeName)) {
				reportError("Incorrect FileObject name. Expected to contain " + topTypeName + 
						" but was " + _elementUtils.getFileObjectOf(t));
			}
			validateInnerElements(t);
		}
	}
	private void validateModifiers(ExecutableElement method, Modifier[] expected) {
		Set<Modifier> modifiers = method.getModifiers();
		List<Modifier> list = new ArrayList<>(modifiers);
		for (Modifier modifier : expected) {
			list.remove(modifier);
		}
		assertTrue("modifiers still present: " + list.toString(), list.isEmpty());
	}
	protected <E extends Element> List<Element> filterElements(Iterable<? extends E> list, ElementKind kind) {
		List<Element> elements = new ArrayList<>();
		for (Element e : list) {
			if (e.getKind() == kind)
				elements.add(e);
		}
		return elements;
	}
	protected <D extends Directive> List<Directive> filterDirective(Iterable<? extends Directive> list, DirectiveKind kind) {
		List<Directive> directives = new ArrayList<>();
		for (Directive d : list) {
			if (d.getKind() == kind)
				directives.add(d);
		}
		return directives;
	}

	@Override
	public void reportError(String msg) {
		throw new AssertionFailedError(msg);
	}
	protected String getExceptionStackTrace(Throwable t) {
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
	public void assertEndsWith(String msg, String whole, String tail) {
		if (whole.endsWith(tail)) {
			return;
		}
		reportError(msg + ", String \"" + whole + "\" does not end with \"" + tail + "\"");
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
	private void verifyAnnotations(AnnotatedConstruct construct, String[] annots) {
		List<? extends AnnotationMirror> annotations = construct.getAnnotationMirrors();
		assertEquals("Incorrect no of annotations", annots.length, annotations.size());
		for(int i = 0, length = annots.length; i < length; i++) {
			AnnotationMirror mirror = annotations.get(i);
			assertEquals("Invalid annotation value", annots[i], getAnnotationString(mirror));
		}
	}

	private String getAnnotationString(AnnotationMirror annot) {
		DeclaredType annotType = annot.getAnnotationType();
		TypeElement type = (TypeElement) annotType.asElement();
		StringBuilder buf = new StringBuilder("@" + type.getQualifiedName());
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annot.getElementValues();
		Set<? extends ExecutableElement> keys = values.keySet();
		buf.append('(');
		for (ExecutableElement executableElement : keys) { // @Marker3()
			buf.append(executableElement.getSimpleName());
			buf.append('=');
			AnnotationValue value = values.get(executableElement);
			if (value.getValue() instanceof List) {
				buf.append('[');
				@SuppressWarnings("rawtypes")
				List list = (List) value.getValue();
				for (Object obj : list) {
					buf.append(obj.toString());
					buf.append(',');
				}
				buf.append(']');
			} else {
				buf.append(value.getValue());
			}
		}
		buf.append(')');
		return buf.toString();
	}
	private static class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
}
