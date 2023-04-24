/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation.
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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

@SupportedAnnotationTypes("*")
public class RecordElementProcessor extends BaseElementProcessor {
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
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
		testPreviewFlagTrue();
		testRecords1();
		testRecords2();
		testRecords3();
		testRecords4();
		testRecords5();
		testRecords6();
		testRecords7();
		testRecords8();
		testRecords9();
		testRecords10();
	}

	public void testPreviewFlagTrue() throws IOException {
		if (this.processingEnv instanceof BaseProcessingEnvImpl) {
			boolean preview = ((BaseProcessingEnvImpl) this.processingEnv).isPreviewEnabled();
			assertTrue("Preview flag not seen as enabled", preview);
		}
		SourceVersion sourceVersion = this.processingEnv.getSourceVersion();
		SourceVersion expected = SourceVersion.valueOf("RELEASE_" + Runtime.version().feature());
		assertEquals("Should support the latest compliance", expected, sourceVersion);
	}
	/*
	 * Basic test for record element and kind
	 */
	public void testRecords1() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Point");
		assertNotNull("TypeElement for record should not be null", record);
		assertEquals("Name for record should not be null", "records.Point", record.getQualifiedName().toString());
		assertEquals("Incorrect element kind", ElementKind.RECORD, record.getKind());
		List<? extends RecordComponentElement> recordComponents = record.getRecordComponents();
		// Test that in the first round, we don't get an NPE
		assertNotNull("recordComponents Should not be null", recordComponents);
		assertEquals("recordComponents Should not be null", 6, recordComponents.size());
	}
	/*
	 * Test for presence of record component in a record element
	 */
	public void testRecords2() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Point");
		assertNotNull("TypeElement for record should not be null", record);
		List<? extends Element> enclosedElements = record.getEnclosedElements();
		assertNotNull("enclosedElements for record should not be null", enclosedElements);
		List<RecordComponentElement> recordComponentsIn = ElementFilter.recordComponentsIn(enclosedElements);
		int size = recordComponentsIn.size();
		assertEquals("incorrect no of record components", 6, size);
		Element element = recordComponentsIn.get(0);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		RecordComponentElement recordComponent = (RecordComponentElement) element;
		assertEquals("Incorrect name for record component", "comp_", recordComponent.getSimpleName().toString());
		Element enclosingElement = recordComponent.getEnclosingElement();
		assertEquals("Elements should be same", record, enclosingElement);
		TypeMirror recordTypeElement = record.asType();
		DeclaredType declaredType = (DeclaredType) recordTypeElement;
		TypeMirror member = _typeUtils.asMemberOf(declaredType, element);
		assertNotNull("member of the element should not be null", member);
		assertTrue("member is a primitive type", member instanceof PrimitiveType);
		assertEquals("member is a primitive type", TypeKind.INT, member.getKind());
	}
	/*
	 * Test that the implicit modifiers are set for a record and its methods
	 */
	public void testRecords3() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Point");
		assertNotNull("TypeElement for record should not be null", record);
		Set<Modifier> modifiers = record.getModifiers();
		assertTrue("record should be public", modifiers.contains(Modifier.PUBLIC));
		assertTrue("record should be final", modifiers.contains(Modifier.FINAL));
		List<? extends Element> enclosedElements = record.getEnclosedElements();
		List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
		ExecutableElement equalsMethod = null;
		ExecutableElement toSMethod = null;
		ExecutableElement hashMethod = null;
		for (ExecutableElement executableElement : methods) {
			if (executableElement.getSimpleName().toString().equals("equals")) {
				equalsMethod = executableElement;
			} else if (executableElement.getSimpleName().toString().equals("hashCode")) {
				hashMethod = executableElement;
			} else if (executableElement.getSimpleName().toString().equals("toString")) {
				toSMethod = executableElement;
			}
		}

		modifiers = equalsMethod.getModifiers();
		assertTrue("should be public", modifiers.contains(Modifier.PUBLIC));
		assertFalse("should not be final", modifiers.contains(Modifier.FINAL));
		modifiers = toSMethod.getModifiers();
		assertTrue("should be public", modifiers.contains(Modifier.PUBLIC));
		assertFalse("should not be final", modifiers.contains(Modifier.FINAL));
		modifiers = hashMethod.getModifiers();
		assertTrue("should be public", modifiers.contains(Modifier.PUBLIC));
		assertFalse("should not be final", modifiers.contains(Modifier.FINAL));
	}
	public void testRecords3a() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Record2");
		assertNotNull("TypeElement for record should not be null", record);
		Set<Modifier> modifiers = record.getModifiers();
		assertTrue("record should be public", modifiers.contains(Modifier.PUBLIC));
		assertTrue("record should be final", modifiers.contains(Modifier.FINAL));
		List<? extends Element> enclosedElements = record.getEnclosedElements();
		List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
		ExecutableElement equalsMethod = null;
		ExecutableElement toSMethod = null;
		ExecutableElement hashMethod = null;
		for (ExecutableElement executableElement : methods) {
			if (executableElement.getSimpleName().toString().equals("equals")) {
				equalsMethod = executableElement;
			} else if (executableElement.getSimpleName().toString().equals("hashCode")) {
				hashMethod = executableElement;
			} else if (executableElement.getSimpleName().toString().equals("toString")) {
				toSMethod = executableElement;
			}
		}
		modifiers = equalsMethod.getModifiers();
		assertTrue("should be public", modifiers.contains(Modifier.PUBLIC));
		assertTrue("should be final", modifiers.contains(Modifier.FINAL));
		assertTrue("should be strictfp", modifiers.contains(Modifier.STRICTFP));
		modifiers = toSMethod.getModifiers();
		assertTrue("should be public", modifiers.contains(Modifier.PUBLIC));
		assertTrue("should be strictfp", modifiers.contains(Modifier.STRICTFP));
		assertTrue("should be final", modifiers.contains(Modifier.FINAL));
		modifiers = hashMethod.getModifiers();
		assertTrue("should be public", modifiers.contains(Modifier.PUBLIC));
		assertTrue("should be final", modifiers.contains(Modifier.FINAL));
		assertTrue("should be strictfp", modifiers.contains(Modifier.STRICTFP));

	}
	/*
	 * Test for annotations on record and record components
	 */
	public void testRecords4() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Point");
		assertNotNull("TypeElement for record should not be null", record);
		verifyAnnotations(record, new String[]{"@Deprecated()"});

		List<? extends Element> enclosedElements = record.getEnclosedElements();
		assertNotNull("enclosedElements for record should not be null", enclosedElements);
		List<RecordComponentElement> recordComponentsIn = ElementFilter.recordComponentsIn(enclosedElements);
		int size = recordComponentsIn.size();
		assertEquals("incorrect no of record components", 6, size);
		Element element = recordComponentsIn.get(0);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		RecordComponentElement recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{"@MyAnnot()"});

		element = recordComponentsIn.get(1);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{"@MyAnnot2()"});
	}
	private TypeElement find(Set<? extends Element> elements, String name) {
		for (Element element : elements) {
			if (name.equals(element.getSimpleName().toString())) {
				return (TypeElement) element;
			}
		}
		return null;
	}
	public void testRecords4a() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Point");
		assertNotNull("TypeElement for record should not be null", record);
		verifyAnnotations(record, new String[]{"@Deprecated()"});

		assertEquals("incorrect no of record components", 6, record.getRecordComponents().size());

		List<? extends Element> enclosedElements = record.getEnclosedElements();
		assertNotNull("enclosedElements for record should not be null", enclosedElements);
		List<RecordComponentElement> recordComponentsIn = ElementFilter.recordComponentsIn(enclosedElements);
		int size = recordComponentsIn.size();
		assertEquals("incorrect no of record components", 6, size);

		Element element = recordComponentsIn.get(2);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		RecordComponentElement recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{});

		element = recordComponentsIn.get(3);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{});

		element = recordComponentsIn.get(4);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{});
		List<ExecutableElement> methodsIn = ElementFilter.methodsIn(enclosedElements);
		List<String> actualMethodNames = methodsIn.stream().filter((m) -> m.getSimpleName().toString().startsWith("comp"))
																	.map((m) -> m.getSimpleName().toString())
																	.collect(Collectors.toList());
		assertEquals("incorrect method", 6, actualMethodNames.size());
		for (ExecutableElement method : methodsIn) {
			if (method.getSimpleName().toString().equals("comp_")) {
				verifyAnnotations(method, new String[]{});
				TypeMirror asType = method.asType();
				verifyAnnotations(asType, new String[]{});
			} else if (method.getSimpleName().toString().equals("comp2_")) {
				if (!isBinaryMode) {
					verifyAnnotations(method, new String[]{"@MyAnnot2()"});
					TypeMirror asType = method.asType();
					verifyAnnotations(asType, new String[]{});
				}
			} else if (method.getSimpleName().toString().equals("comp3_")) {
				// Known issue
				if (!isBinaryMode) {
					verifyAnnotations(method, new String[]{"@MyAnnot3()"});
					TypeMirror asType = method.asType();
					verifyAnnotations(asType, new String[]{});
				}
			} else if (method.getSimpleName().toString().equals("comp4_")) {
				verifyAnnotations(method, new String[]{});
				TypeMirror asType = method.asType();
				verifyAnnotations(asType, new String[]{});
			} else if (method.getSimpleName().toString().equals("comp5_")) {
				// Known issue
				if (!isBinaryMode) {
					verifyAnnotations(method, new String[]{});
					TypeMirror asType = method.asType();
					verifyAnnotations(asType, new String[]{});
				}
			} else if (method.getSimpleName().toString().equals("comp6_")) {
				List<? extends AnnotationMirror> annots = method.getAnnotationMirrors();
				assertEquals("incorrect no of annotations", 1, annots.size());
				AnnotationMirror mirror = annots.get(0);
				DeclaredType annotationType = mirror.getAnnotationType();
				Element asElement = annotationType.asElement();
				if (isBinaryMode) {
					List<? extends AnnotationMirror> annotationMirrors = asElement.getAnnotationMirrors();
					assertEquals("incorrect no of annotations", 2, annotationMirrors.size());
					for (AnnotationMirror mirror1 : annotationMirrors) {
						if (mirror1.getAnnotationType().asElement().getSimpleName().toString().contains("Retention")) {
							assertEquals("Invalid annotation value", "@Retention(value=RUNTIME)", getAnnotationString(mirror1));
						} else if (mirror1.getAnnotationType().asElement().getSimpleName().toString().contains("Target")) {
							String annotationString = getAnnotationString(mirror1);
							assertTrue("must contain target METHOD", annotationString.contains("METHOD"));
							assertTrue("must contain target TYPE_USE", annotationString.contains("TYPE_USE"));
							assertEquals("Invalid annotation value", "@Target(value=METHOD,TYPE_USE)", getAnnotationString(mirror1));
						}
					}
				}
			}
		}
		methodsIn = ElementFilter.constructorsIn(enclosedElements);
		assertEquals("incorrect method", 1, methodsIn.size());
		ExecutableElement m = methodsIn.get(0);
		verifyAnnotations(m, new String[]{});
		TypeMirror asType = m.asType();
		verifyAnnotations(asType, new String[]{});
		List<? extends VariableElement> parameters = m.getParameters();
		assertEquals("incorrect parameters", 6, parameters.size());
		for (VariableElement v : parameters) {
			if (v.getSimpleName().toString().equals("comp_")) {
				verifyAnnotations(v, new String[]{"@MyAnnot()"}); // ECJ fails
			} else if (v.getSimpleName().toString().equals("comp2_")) {
				verifyAnnotations(v, new String[]{"@MyAnnot2()"});
			} else if (v.getSimpleName().toString().equals("comp3_")) {
				verifyAnnotations(v, new String[]{}); // ECJ fails
			} else if (v.getSimpleName().toString().equals("comp4_")) {
				verifyAnnotations(v, new String[]{}); // ECJ fails
			} else if (v.getSimpleName().toString().equals("comp5_")) {
				verifyAnnotations(v, new String[]{});
			}
		}
	}
	public void testRecords5() {
		Map<String, TypeKind> expRecComps = new HashMap<>();
		expRecComps.put("x", TypeKind.INT);
		expRecComps.put("i", TypeKind.DECLARED);
		expRecComps.put( "r", TypeKind.DECLARED);
		expRecComps.put("t", TypeKind.DECLARED);

        Map<String, TypeKind> fields = new HashMap<>();

        fields.put("s", TypeKind.DECLARED);
        fields.put("d", TypeKind.DOUBLE);
        fields.put("c", TypeKind.DECLARED);

        Map<String, TypeKind> expFields = new HashMap<>(expRecComps);
        expFields.putAll(fields);

		String[] arr = new String[] {"x", "i", "r", "r", "foo", "bar",
                "equals", "hashCode", "toString"};
		String[] arr2 = new String[] {"x", "i", "r", "t"};

		List<String> expMethodNames = Arrays.asList(arr);
		List<String> expRecComppNames = Arrays.asList(arr2);

        Element recordElement = _elementUtils.getTypeElement("records.Record2");
        List<? extends Element> recordElements = recordElement.getEnclosedElements();
        List<VariableElement> actFields = ElementFilter.fieldsIn(recordElements);
        List<RecordComponentElement> actRecComps = ElementFilter.recordComponentsIn(recordElements);
        List<ExecutableElement> methods = ElementFilter.methodsIn(recordElements);
        //checking the size
        assertEquals("expected enclosed fields size mismatch", expFields.size(), actFields.size());

        //checking for types for the given field Names.
        for (VariableElement actField : actFields) {
            String key = actField.getSimpleName().toString();
            if (expFields.get(key) != actField.asType().getKind()) {
            	assertEquals("expected enclosed fields mismatch", expFields.get(key), actField.asType().getKind());
            }
        }
        //checking recComp  size
        assertEquals("expected enclosed Record Components size mismatch", expRecComps.size(), actRecComps.size());
        //checking for types for the given record component name.
        for (RecordComponentElement actRecComp : actRecComps) {
            String key = actRecComp.getSimpleName().toString();
            assertEquals("expected enclosed Record Components mismatch", expRecComps.get(key), actRecComp.asType().getKind());
        }

        List<String> actualMethodNames = methods.stream().map((m) -> m.getSimpleName().toString()).collect(Collectors.toList());
        List<String> actualRecordCompNames = actRecComps.stream().map((m) -> m.getSimpleName().toString()).collect(Collectors.toList());

        //checking the size
        assertEquals("expected enclosed Record Components size mismatch", expMethodNames.size(), actualMethodNames.size());
        //check the method names.
        if (!actualMethodNames.containsAll(expMethodNames)) {
        	fail(" expected enclosed methods mismatch - expected at least : " + expMethodNames + " " +
                    "actual : " + actualMethodNames);
        }
        if (!actualRecordCompNames.containsAll(expRecComppNames)) {
        	fail(" expected enclosed record components mismatch - expected at least : " + expRecComppNames + " " +
                    "actual : " + actualRecordCompNames);
        }
	}
	// Same as above, but use getRecordComponents() instead of getEnclosedElements()
	public void testRecords5a() {
		Map<String, TypeKind> expRecComps = new HashMap<>();
		expRecComps.put("x", TypeKind.INT);
		expRecComps.put("i", TypeKind.DECLARED);
		expRecComps.put( "r", TypeKind.DECLARED);
		expRecComps.put("t", TypeKind.DECLARED);

		String[] arr2 = new String[] {"x", "i", "r", "t"};
		List<String> expRecComppNames = Arrays.asList(arr2);

        TypeElement recordElement = _elementUtils.getTypeElement("records.Record2");
        List<? extends Element> recordElements = recordElement.getRecordComponents();
        List<RecordComponentElement> actRecComps = ElementFilter.recordComponentsIn(recordElements);
        //checking recComp  size
        assertEquals("expected enclosed Record Components size mismatch", expRecComps.size(), actRecComps.size());
        //checking for types for the given record component name.
        for (RecordComponentElement actRecComp : actRecComps) {
            String key = actRecComp.getSimpleName().toString();
            assertEquals("expected enclosed Record Components mismatch", expRecComps.get(key), actRecComp.asType().getKind());
        }

        List<String> actualRecordCompNames = actRecComps.stream().map((m) -> m.getSimpleName().toString()).collect(Collectors.toList());

        if (!actualRecordCompNames.containsAll(expRecComppNames)) {
        	fail(" expected enclosed record components mismatch - expected at least : " + expRecComppNames + " " +
                    "actual : " + actualRecordCompNames);
        }
	}
	public void testRecords6() {
		TypeElement recordElement = _elementUtils.getTypeElement("records.Record2");
		final List<? extends Element> members = _elementUtils.getAllMembers(recordElement);
		final List<? extends Element> enclosedElements = recordElement.getEnclosedElements();

		final HashSet<? extends Element> enclosedElementsSet = new HashSet<Element>(recordElement.getEnclosedElements());

		List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);
		List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElements);
		List<VariableElement> fields = ElementFilter.fieldsIn(enclosedElements);

		Set<ExecutableElement> constructorsSet = ElementFilter.constructorsIn(enclosedElementsSet);
		Set<ExecutableElement> methodsSet = ElementFilter.methodsIn(enclosedElementsSet);
		Set<VariableElement> fieldsSet = ElementFilter.fieldsIn(enclosedElementsSet);

		assertTrue("Constructors must be within all members", members.containsAll(constructors));
		assertTrue("Constructors must be within enclosed elements", enclosedElements.containsAll(constructors));
		assertEquals("Overloaded versions of ElementFilter.constructorsIn() must return equal results",
				new HashSet<Element>(constructors), constructorsSet);

		assertTrue("Methods must be within all members", members.containsAll(methods));
		assertTrue("Methods must be within enclosed elements", enclosedElements.containsAll(methods));
		assertEquals("Overloaded versions of ElementFilter.methodsIn() must return equal results",
				new HashSet<Element>(methods), methodsSet);

		assertTrue("Fields must be within all members", members.containsAll(fields));
		assertTrue("Fields must be within enclosed elements", enclosedElements.containsAll(fields));
		assertEquals("Overloaded versions of ElementFilter.fieldsIn() must return equal results", new HashSet<Element>(fields), fieldsSet);
	}
	public void testRecords7() {
		TypeElement recordElement = _elementUtils.getTypeElement("records.Record2");
		final List<? extends Element> members = _elementUtils.getAllMembers(recordElement);
		final List<? extends Element> enclosedElements = recordElement.getEnclosedElements();
		List<RecordComponentElement> records = ElementFilter.recordComponentsIn(enclosedElements);
		for (RecordComponentElement record : records) {
			ExecutableElement method = record.getAccessor();
			assertTrue("Accessor method not found", members.contains(method));
			assertTrue("Accessor method not found", enclosedElements.contains(method));
			assertEquals("Accessor method name incorrect", record.getSimpleName().toString(), method.getSimpleName().toString());
		}
	}
	public void testRecords8() {
		boolean result = false;
        Object r = "DEFAULT";
        Object param = new Object();
        ScannerImpl<Object, Object> es = new ScannerImpl<>(r);

		Element e = _elementUtils.getTypeElement("records.Record2");
		List<? extends Element> recordElements = e.getEnclosedElements();
		for (Element recComp : recordElements) {
			if (recComp.getKind() == ElementKind.RECORD_COMPONENT) {
				result = true;
				Object r2 = recComp.accept(es, param);
				assertSame("returned message not same", r, r2);
				assertTrue("not visited", es.visited);
				assertSame("Visited element not the same", recComp, es.el);
				assertSame("Visited param not the same", param, es.param);
			}
		}
        assertTrue("Test returned negative", result);
	}
	public void testRecords9() {
		String[] arr1 = new String[] { "x", "bigInt", "r1", "floatValue", "c", "recordInstance" };
		TypeKind[] arr2 = new TypeKind[] { TypeKind.INT, TypeKind.DECLARED, TypeKind.DECLARED, TypeKind.FLOAT,
				TypeKind.DECLARED, TypeKind.DECLARED };
		List<String> names = Arrays.asList(arr1);
		List<TypeKind> types = Arrays.asList(arr2);

		Element record = _elementUtils.getTypeElement("records.R3");
		List<? extends Element> allElements = record.getEnclosedElements();
		List<RecordComponentElement> components = ElementFilter.recordComponentsIn(allElements);
		List<ExecutableElement> accessors = components.stream().map
				(RecordComponentElement::getAccessor).collect(Collectors.toList());
		assertEquals("Size mismatch", names.size(), accessors.size());
		for (ExecutableElement accessor : accessors) {
			String name = accessor.getSimpleName().toString();
			int indexOf = names.indexOf(name);
			assertSame("Type kind not same for \"" + name + "\".", types.get(indexOf), accessor.getReturnType().getKind());
			assertTrue("should be executable type for \"" + name + "\".", (accessor.asType() instanceof ExecutableType));
			assertNull("should be null", accessor.getDefaultValue());
			List<? extends AnnotationMirror> mirrors = accessor.getAnnotationMirrors();
			if (name.equals("c") || name.equals("bigInt") || name.equals("r1")) {
				assertEquals("annotations count mismatch for \"" + name + "\".", 1, mirrors.size());
				Set<? extends ExecutableElement> accessorAnnotations = mirrors.get(0)
						.getElementValues().keySet();
				assertEquals("annotations type element mismatch for \"" + name + "\".", 1, accessorAnnotations.size());
				int val = (int) accessorAnnotations.toArray(new ExecutableElement[0])[0].getDefaultValue()
						.getValue();
				assertEquals("Incorrect default value for \"" + name + "\".", 1, val);
			}
			if (name.equals("floatValue") || name.equals("x")) {
				assertEquals("annotations count mismatch for \"" + name + "\".", 0, mirrors.size());
			}
			if (name.equals("recordInstance")) {
				assertEquals("annotations count mismatch for \"" + name + "\".", 2, mirrors.size());
			}
			assertTrue("Parameters should be empty for \"" + name + "\".", accessor.getParameters().isEmpty());
			assertTrue("Thrown types should be empty for \"" + name + "\".", accessor.getThrownTypes().isEmpty());
			assertTrue("Type parameters should be empty for \"" + name + "\".", accessor.getTypeParameters().isEmpty());
			assertFalse("Should not be default for \"" + name + "\".", accessor.isDefault());
			assertFalse("Should not be varargs for \"" + name + "\".", accessor.isVarArgs());
		}
	}
	public void testRecords10() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "R4");
		assertNotNull("TypeElement for record should not be null", record);

		List<? extends Element> enclosedElements = record.getEnclosedElements();
		List<ExecutableElement> methodsIn = ElementFilter.constructorsIn(enclosedElements);
		assertEquals("incorrect method", 1, methodsIn.size());
		ExecutableElement m = methodsIn.get(0);
		verifyAnnotations(m, new String[]{});
		TypeMirror asType = m.asType();
		verifyAnnotations(asType, new String[]{});
		List<? extends VariableElement> parameters = m.getParameters();
		assertEquals("incorrect parameters", 1, parameters.size());
		VariableElement var = parameters.get(0);
		assertEquals("component name incorrect", "i", var.getSimpleName().toString());
		verifyAnnotations(var, new String[]{"@Marker4()"});
	}
	public void testRecordsConstructors() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = find(elements, "Record2");
		assertNotNull("TypeElement for record should not be null", record);
		List<? extends Element> enclosedElements = record.getEnclosedElements();
		List<ExecutableElement> methods = ElementFilter.constructorsIn(enclosedElements);
		assertEquals("Incorrect no of constructors", 1, methods.size());
		ExecutableElement constr = methods.get(0);
		if (isBinaryMode) {
			assertFalse("Should not be canonical constructor", _elementUtils.isCanonicalConstructor(constr));
			assertFalse("Should not be compact constructor", _elementUtils.isCompactConstructor(constr));
		} else {
			assertTrue("Should be canonical constructor", _elementUtils.isCanonicalConstructor(constr));
			assertFalse("Should not be compact constructor", _elementUtils.isCompactConstructor(constr));
		}
		record = find(elements, "Record3");
		assertNotNull("TypeElement for record should not be null", record);
		enclosedElements = record.getEnclosedElements();
		methods = ElementFilter.constructorsIn(enclosedElements);
		assertEquals("Incorrect no of constructors", 1, methods.size());
		constr = methods.get(0);
		if (isBinaryMode) {
			assertFalse("Should not be canonical constructor", _elementUtils.isCanonicalConstructor(constr));
			assertFalse("Should not be compact constructor", _elementUtils.isCompactConstructor(constr));
		} else {
			assertTrue("Should not be canonical constructor", _elementUtils.isCanonicalConstructor(constr));
			assertTrue("Should be a compact constructor", _elementUtils.isCompactConstructor(constr));
		}
	}
}
