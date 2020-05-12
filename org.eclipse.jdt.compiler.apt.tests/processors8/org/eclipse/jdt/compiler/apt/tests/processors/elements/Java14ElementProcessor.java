/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementScanner14;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

@SupportedAnnotationTypes("*")
public class Java14ElementProcessor extends BaseProcessor {
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
		testPreviewFlagTrue();
		testRecords1();
		testRecords2();
		testRecords3();
		testRecords4();
		testRecords5();
		testRecords6();
	}

	public void testPreviewFlagTrue() throws IOException {
		if (this.processingEnv instanceof BaseProcessingEnvImpl) {
			boolean preview = ((BaseProcessingEnvImpl) this.processingEnv).isPreviewEnabled();
			assertTrue("Preview flag not seen as enabled", preview);
		}
	}
	/*
	 * Basic test for record element and kind
	 */
	public void testRecords1() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = null;
		for (Element element : elements) {
			if ("Point".equals(element.getSimpleName().toString())) {
				record = (TypeElement) element;
			}
		}
		assertNotNull("TypeElement for record should not be null", record);
		assertEquals("Name for record should not be null", "records.Point", record.getQualifiedName().toString());
		assertEquals("Incorrect element kind", ElementKind.RECORD, record.getKind());
		List<? extends RecordComponentElement> recordComponents = record.getRecordComponents();
		// Test that in the first round, we don't get an NPE
		assertNotNull("recordComponents Should not be null", recordComponents);
		assertEquals("recordComponents Should not be null", 5, recordComponents.size());
	}
	/*
	 * Test for presence of record component in a record element
	 */
	public void testRecords2() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = null;
		for (Element element : elements) {
			if ("Point".equals(element.getSimpleName().toString())) {
				record = (TypeElement) element;
			}
		}
		assertNotNull("TypeElement for record should not be null", record);
		List<? extends Element> enclosedElements = record.getEnclosedElements();
		assertNotNull("enclosedElements for record should not be null", enclosedElements);
		List<RecordComponentElement> recordComponentsIn = ElementFilter.recordComponentsIn(enclosedElements);
		int size = recordComponentsIn.size();
		assertEquals("incorrect no of record components", 5, size);
		Element element = recordComponentsIn.get(0);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		RecordComponentElement recordComponent = (RecordComponentElement) element;
		assertEquals("Incorrect name for record component", "comp_", recordComponent.getSimpleName().toString());
		Element enclosingElement = recordComponent.getEnclosingElement();
		assertEquals("Elements should be same", record, enclosingElement);
	}
	/*
	 * Test that the implicit modifiers are set for a record and its methods
	 */
	public void testRecords3() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = null;
		for (Element element : elements) {
			if ("Point".equals(element.getSimpleName().toString())) {
				record = (TypeElement) element;
			}
		}
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
		TypeElement record = null;
		for (Element element : elements) {
			if ("Record2".equals(element.getSimpleName().toString())) {
				record = (TypeElement) element;
			}
		}
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
		assertFalse("should not be final", modifiers.contains(Modifier.FINAL));
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
		TypeElement record = null;
		for (Element element : elements) {
			if ("Point".equals(element.getSimpleName().toString())) {
				record = (TypeElement) element;
			}
		}
		assertNotNull("TypeElement for record should not be null", record);
		verifyAnnotations(record, new String[]{"@Deprecated()"});

		List<? extends Element> enclosedElements = record.getEnclosedElements();
		assertNotNull("enclosedElements for record should not be null", enclosedElements);
		List<RecordComponentElement> recordComponentsIn = ElementFilter.recordComponentsIn(enclosedElements);
		int size = recordComponentsIn.size();
		assertEquals("incorrect no of record components", 5, size);
		Element element = recordComponentsIn.get(0);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		RecordComponentElement recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{"@MyAnnot()"});

		element = recordComponentsIn.get(1);
		assertEquals("Incorrect kind of element", ElementKind.RECORD_COMPONENT, element.getKind());
		recordComponent = (RecordComponentElement) element;
		verifyAnnotations(recordComponent, new String[]{"@MyAnnot2()"});
	}
	public void testRecords4a() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		TypeElement record = null;
		for (Element element : elements) {
			if ("Point".equals(element.getSimpleName().toString())) {
				record = (TypeElement) element;
			}
		}
		assertNotNull("TypeElement for record should not be null", record);
		verifyAnnotations(record, new String[]{"@Deprecated()"});

		List<? extends Element> enclosedElements = record.getEnclosedElements();
		assertNotNull("enclosedElements for record should not be null", enclosedElements);
		List<RecordComponentElement> recordComponentsIn = ElementFilter.recordComponentsIn(enclosedElements);
		int size = recordComponentsIn.size();
		assertEquals("incorrect no of record components", 5, size);

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
		StringBuffer buf = new StringBuffer("@" + type.getSimpleName());
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annot.getElementValues();
		Set<? extends ExecutableElement> keys = values.keySet();
		buf.append('(');
		for (ExecutableElement executableElement : keys) { // @Marker3()
			buf.append(executableElement.getSimpleName());
			buf.append('=');
			AnnotationValue value = values.get(executableElement);
			buf.append(value.getValue());
		}
		buf.append(')');
		return buf.toString();
	}
	private class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}

	class ScannerImpl<R, P> extends ElementScanner14<R, P> {
		public Object el;
		boolean isType;
		boolean isMethod;
		public boolean visited;
		public boolean scanned;
		R result;
		public Object param;

		public List<TypeParameterElement> params = new ArrayList<>();

		public ScannerImpl() {
			super();
		}

		public ScannerImpl(R r) {
			super(r);
		}

		@Override
		public R visitType(TypeElement e, P p) {
			el = e;
			param = p;
			isType = true;
			return super.visitType(e, p);
		}

		@Override
		public R visitExecutable(ExecutableElement e, P p) {
			isMethod = true;
			el = e;
			param = p;
			return super.visitExecutable(e, p);
		}

		public R visitRecordComponent(RecordComponentElement e, P p) {
			el = e;
			param = p;
			visited = true;
			return super.visitRecordComponent(e, p);
		}

		public R scan(Element e, P p) {
			scanned = true;
			if (e instanceof TypeParameterElement) {
				params.add((TypeParameterElement) e);
			}
			return DEFAULT_VALUE;
		}
	}
}
