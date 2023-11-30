/*******************************************************************************
 * Copyright (c) 2007, 2011 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fix for 342598
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.negative;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.eclipse.jdt.compiler.apt.tests.processors.base.XMLComparer;
import org.eclipse.jdt.compiler.apt.tests.processors.base.XMLConverter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * An annotation processor that investigates the model produced by code containing
 * semantic errors such as missing types. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc to the
 * command line.
 *
 * Optionally, enable just a single test, by adding an integer value denoting the
 * test to the option key.  For example, to enable testNegative2, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc=2
 * to the command line.  If 0 or no value is specified, all tests will be run.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({"org.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc", NegativeModelProc.IGNORE_JAVAC_BUGS})
public class NegativeModelProc extends AbstractProcessor
{
	/**
	 * Reference model for types in Negative1 test
	 */
	private static final String NEGATIVE_1_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative1\" sname=\"Negative1\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <annotations>\n" +
		"   <annotation sname=\"A3\"/>\n" +
		"  </annotations>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  <variable-element kind=\"FIELD\" sname=\"s1\" type=\"java.lang.String\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"Anno1\">\n" +
		"     <annotation-values>\n" +
		"      <annotation-value member=\"value\" type=\"java.lang.String\" value=\"spud\"/>\n" +
		"     </annotation-values>\n" +
		"    </annotation>\n" +
		"   </annotations>\n" +
		"  </variable-element>\n" +
		"  <variable-element kind=\"FIELD\" sname=\"m1\" type=\"Missing1\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"A4\"/>\n" +
		"   </annotations>\n" +
		"  </variable-element>\n" +
		"  <variable-element kind=\"FIELD\" sname=\"i1\" type=\"int\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"A5\"/>\n" +
		"   </annotations>\n" +
		"  </variable-element>\n" +
		"  <variable-element kind=\"FIELD\" sname=\"m2\" type=\"Missing2.Missing3.Missing4\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"A8\"/>\n" +
		"   </annotations>\n" +
		"  </variable-element>\n" +
		" </type-element>\n" +
		"</model>\n" +
		"";

	/**
	 * Reference model for types in Negative4 test
	 */
	private static final String NEGATIVE_4_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative4\" sname=\"Negative4\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"zorkRaw\"/>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"zorkOfString\"/>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"ifooOfString\" optional=\"true\"/>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"ibarRaw\"/>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"ibarOfT1T2\" optional=\"true\"/>\n" +
		" </type-element>\n" +
		"</model>\n";

	/**
	 * Reference model for types in Negative5 test
	 */
	private static final String NEGATIVE_5_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative5\" sname=\"Negative5\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative5.C1\" sname=\"C1\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"ERROR\" to-string=\"M1\"/>\n" +
		"   </superclass>\n" +
		"   <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  </type-element>\n" +
		"  <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative5.C2\" sname=\"C2\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"ERROR\" to-string=\"java.lang.Object\"/>\n" +
		"   </superclass>\n" +
		"   <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  </type-element>\n" +
		"  <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative5.I1\" sname=\"I1\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"   </superclass>\n" +
		"  </type-element>\n" +
		"  <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative5.I2\" sname=\"I2\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"   </superclass>\n" +
		"  </type-element>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.INegative5\" sname=\"INegative5\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <type-element kind=\"CLASS\" qname=\"targets.negative.pa.INegative5.C101\" sname=\"C101\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"ERROR\" to-string=\"M101\"/>\n" +
		"   </superclass>\n" +
		"   <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  </type-element>\n" +
		"  <type-element kind=\"CLASS\" qname=\"targets.negative.pa.INegative5.C102\" sname=\"C102\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"ERROR\" to-string=\"java.lang.Object\"/>\n" +
		"   </superclass>\n" +
		"   <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		"  </type-element>\n" +
		"  <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.INegative5.I101\" sname=\"I101\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"   </superclass>\n" +
		"  </type-element>\n" +
		"  <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.INegative5.I102\" sname=\"I102\">\n" +
		"   <superclass>\n" +
		"    <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"   </superclass>\n" +
		"  </type-element>\n" +
		" </type-element>\n" +
		"</model>";

	/**
	 * Reference model for class Negative6.
	 */
	private static final String NEGATIVE_6_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative6\" sname=\"Negative6\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"method1\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"M11\"/>\n" +
		"   </annotations>\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg0\" type=\"M16\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"M13\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"  </executable-element>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"method2\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"M21\"/>\n" +
		"   </annotations>\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg0\" type=\"int\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"M22\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"  </executable-element>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"method3\">\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg0\" type=\"java.lang.String\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"M31\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg1\" type=\"java.lang.String\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"M32\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"  </executable-element>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"M41\"/>\n" +
		"   </annotations>\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg0\" type=\"M43\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"M42\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"  </executable-element>\n" +
		" </type-element>\n" +
		"</model>";

	/**
	 * Reference model for class Negative7.
	 */
	private static final String NEGATIVE_7_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative7\" sname=\"Negative7\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"method1\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"A1\"/>\n" +
		"   </annotations>\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg0\" type=\"int\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"A1\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"  </executable-element>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative7A\" sname=\"Negative7A\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Cloneable\"/>\n" +
		"  </interfaces>\n" +
		"  <executable-element kind=\"METHOD\" sname=\"method1\">\n" +
		"   <annotations>\n" +
		"    <annotation sname=\"A1\"/>\n" +
		"   </annotations>\n" +
		"   <variable-element kind=\"PARAMETER\" sname=\"arg0\" type=\"int\">\n" +
		"    <annotations>\n" +
		"     <annotation sname=\"A1\"/>\n" +
		"    </annotations>\n" +
		"   </variable-element>\n" +
		"  </executable-element>\n" +
		" </type-element>\n" +
		"</model>\n";
	/**
	 * Reference model for class Negative8.
	 */
	private static final String NEGATIVE_8_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative8a\" sname=\"Negative8a\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"ERROR\" to-string=\"RemoteNegative8a\"/>\n" +
		"  </interfaces>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8b\" sname=\"Negative8b\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"ERROR\" to-string=\"RemoteNegative8a\"/>\n" +
		"  </interfaces>\n" +
		" </type-element>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative8c\" sname=\"Negative8c\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"ERROR\" to-string=\"RemoteNegative8b&lt;T&gt;\"/>\n" +
		"  </interfaces>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8d\" sname=\"Negative8d\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"ERROR\" to-string=\"RemoteNegative8b&lt;T&gt;\"/>\n" +
		"  </interfaces>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8e\" sname=\"Negative8e\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"ERROR\" to-string=\"targets.negative.pa.Negative8f&lt;T&gt;\"/>\n" +
		"  </interfaces>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8f\" sname=\"Negative8f\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		"</model>\n";

	/*
	 * Reference model for class Negative8.
	 */
	private static final String NEGATIVE_8_MODEL_VERSION6 =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative8a\" sname=\"Negative8a\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8b\" sname=\"Negative8b\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		" <type-element kind=\"CLASS\" qname=\"targets.negative.pa.Negative8c\" sname=\"Negative8c\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
		"  </superclass>\n" +
		"  <executable-element kind=\"CONSTRUCTOR\" sname=\"&lt;init&gt;\"/>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8d\" sname=\"Negative8d\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8e\" sname=\"Negative8e\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative8f\" sname=\"Negative8f\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		"</model>\n";

	/**
	 * Reference model for types in Negative1 test
	 */
	private static final String NEGATIVE_9_MODEL =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative9a\" sname=\"Negative9a\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative9b\" sname=\"Negative9b\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		"  <interfaces>\n" +
		"   <type-mirror kind=\"ERROR\" to-string=\"targets.negative.pa.Negative9a&lt;T&gt;\"/>\n" +
		"  </interfaces>\n" +
		" </type-element>\n" +
		"</model>\n";

	/**
	 * Reference model for types in Negative1 test
	 */
	private static final String NEGATIVE_9_MODEL_VERSION6 =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
		"<model>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative9a\" sname=\"Negative9a\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		" <type-element kind=\"INTERFACE\" qname=\"targets.negative.pa.Negative9b\" sname=\"Negative9b\">\n" +
		"  <superclass>\n" +
		"   <type-mirror kind=\"NONE\" to-string=\"none\"/>\n" +
		"  </superclass>\n" +
		" </type-element>\n" +
		"</model>\n";

	/**
	 * Declare this option (-AignoreJavacBugs) to ignore failures of cases that are
	 * known to fail under javac, i.e., known bugs in javac.
	 */
	public static final String IGNORE_JAVAC_BUGS = "ignoreJavacBugs";

	private static final String CLASSNAME = NegativeModelProc.class.getName();

	private static final String[] testMethodNames = {
		"checkNegative1",
		"checkNegative2",
		"checkNegative3",
		"checkNegative4",
		"checkNegative5",
		"checkNegative6",
		"checkNegative7",
		"checkNegative8",
		"checkNegative9",
	};

	private static final Method[] testMethods = new Method[testMethodNames.length];

	/**
	 * Report an error to the test case code.
	 * This is not the same as reporting via Messager!  Use this if some API fails.
	 * @param value will be displayed in the test output, in the event of failure.
	 * Can be anything except "succeeded".
	 */
	public static void reportError(String value) {
		// Uncomment for processor debugging - don't report error
		// value = "succeeded";
		System.setProperty(CLASSNAME, value);
	}

	/**
	 * Report success to the test case code
	 */
	public static void reportSuccess() {
		System.setProperty(CLASSNAME, "succeeded");
	}

	private Elements _elementUtils;

	// 0 means run all tests; otherwise run just the (1-based) single test indicated
	private int _oneTest;

	// Report failures on tests that are already known to be unsupported
	private final boolean _reportFailingCases = true;

	// If processor options don't include this processor's classname, don't run the proc at all.
	private boolean _processorEnabled;

	private boolean _ignoreJavacBugs = false;


	public NegativeModelProc() {
		for (int i = 0; i < testMethodNames.length; ++i) {
			try {
				testMethods[i] = NegativeModelProc.class.getMethod(testMethodNames[i]);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
		// parse options
		_oneTest = -1;
		Map<String, String> options = processingEnv.getOptions();
		_processorEnabled = options.containsKey(CLASSNAME);
		String oneTestOption = options.get(CLASSNAME);
		if (oneTestOption == null || oneTestOption.length() == 0) {
			_oneTest = 0;
		}
		else {
			try {
				_oneTest = Integer.parseInt(oneTestOption);
			} catch (Exception e) {
				// report it in process(), where we have better error reporting capability
			}
		}
		_ignoreJavacBugs = options.containsKey(IGNORE_JAVAC_BUGS);
	}

	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!_processorEnabled) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}
		if (roundEnv.processingOver()) {
			// We're not interested in the postprocessing round.
			return false;
		}
		if (_oneTest < 0 || _oneTest > testMethodNames.length) {
			reportError("Invalid test method specified: " + processingEnv.getOptions().get(CLASSNAME));
			return false;
		}

		// Reflectively invoke the specified tests.
		try {
			if (_oneTest == 0) {
				for (Method testMethod : testMethods) {
					Object success = testMethod.invoke(this);
					if (!(success instanceof Boolean) || !(Boolean)success) {
						return false;
					}
				}
			}
			else {
				Object success = testMethods[_oneTest - 1].invoke(this);
				if (!(success instanceof Boolean) || !(Boolean)success) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			reportError("Exception thrown while invoking test method: " + e);
			return false;
		}

		reportSuccess();
		return false;
	}

	/**
	 * Check the model of resources/targets.negative.pa.Negative6
	 * @return true if all tests passed
	 */
	public boolean checkNegative1() throws Exception {

		// Self-test of XML language model framework.
		// TODO: this should be moved into the ModelTests test suite.
		if (!XMLComparer.test()) {
			reportError("XML language model comparison framework failed self-test");
			return false;
		}

		// Test is failing on Linux - https://bugs.eclipse.org/bugs/show_bug.cgi?id=224424
		if (System.getProperty("os.name").indexOf("Windows") == -1) return true;

		// Get the root of the Negative1 model
		TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative1");
		if (null == element || element.getKind() != ElementKind.CLASS) {
			reportError("Element Negative1 was not found or was not a class");
			return false;
		}

		return checkModel(Collections.singletonList(element), NEGATIVE_1_MODEL, "Negative1");
	}

	/**
	 * Check the annotations in the model of resources/targets.negative.pa.Negative2
	 * @return true if all tests passed
	 */
	public boolean checkNegative2() {
		TypeElement elementN2 = _elementUtils.getTypeElement("targets.negative.pa.Negative2");
		if (null == elementN2 || elementN2.getKind() != ElementKind.CLASS) {
			reportError("Element Negative2 was not found or was not a class");
			return false;
		}
		List<? extends Element> enclosedElements = elementN2.getEnclosedElements();
		for (Element element : enclosedElements) {
			String name = element.getSimpleName().toString();
			if ("m1".equals(name)) {
				AnnotationMirror am2 = findAnnotation(element, "Anno2");
				if (_reportFailingCases && null == am2) {
					reportError("Couldn't find annotation Anno2 on method Negative2.m1");
					return false;
				}
			}
			else if ("m2".equals(name)) {
				AnnotationMirror am1 = findAnnotation(element, "Anno1");
				if (_reportFailingCases && null == am1) {
					reportError("Couldn't find annotation Anno1 on method Negative2.m2");
					return false;
				}
				AnnotationMirror am3 = findAnnotation(element, "FakeAnno3");
				if (_reportFailingCases && null == am3) {
					reportError("Couldn't find annotation FakeAnno3 on method Negative2.m2");
					return false;
				}
			}
			else if ("m3".equals(name)) {
				AnnotationMirror am2 = findAnnotation(element, "Anno2");
				if (_reportFailingCases && null == am2) {
					reportError("Couldn't find annotation Anno2 on method Negative2.m3");
					return false;
				}
				AnnotationMirror am3 = findAnnotation(element, "FakeAnno3");
				if (_reportFailingCases && null == am3) {
					reportError("Couldn't find annotation FakeAnno3 on method Negative2.m3");
					return false;
				}
			}
			else if ("m4".equals(name)) {
				AnnotationMirror am4 = findAnnotation(element, "Anno4");
				if (_reportFailingCases && null == am4) {
					reportError("Couldn't find annotation Anno4 on method Negative2.m4");
					return false;
				}
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = am4.getElementValues();
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
					if ("value".equals(entry.getKey().getSimpleName().toString())) {
						String value = entry.getValue().getValue().toString();
						if (!"123".equals(value) && !"<error>".equals(value)) {
							reportError("Unexpected value for Anno4 on Negative1.s1: " + value);
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Check the model of resources/targets.negative.pa.Negative3
	 * @return true if all tests passed
	 */
	public boolean checkNegative3() {
		TypeElement elementN3 = _elementUtils.getTypeElement("targets.negative.pa.Negative3");
		if (null == elementN3 || elementN3.getKind() != ElementKind.CLASS) {
			reportError("Element Negative3 was not found or was not a class");
			return false;
		}
		List<? extends Element> enclosedElements = elementN3.getEnclosedElements();
		for (Element element : enclosedElements) {
			String name = element.getSimpleName().toString();
			if ("foo".equals(name)) {
				ElementKind kind = element.getKind();
				if (_reportFailingCases && ElementKind.METHOD != kind) {
					reportError("Element 'foo' was expected to be a METHOD but was a " + kind);
					return false;
				}
				List<? extends VariableElement> params = ((ExecutableElement)element).getParameters();
				if (_reportFailingCases && (params == null || params.size() != 1)) {
					reportError("Expected method Negative3.foo() to have one param, but found " +
							(params == null ? 0 : params.size()));
					return false;
				}
				VariableElement param1 = params.iterator().next();
				TypeMirror param1Type = param1.asType();
				TypeKind tkind = param1Type.getKind();
				if (_reportFailingCases && TypeKind.ERROR != tkind && TypeKind.DECLARED != tkind) {
					reportError("Expected the TypeKind of Negative3.foo() param to be ERROR or DECLARED, but found " + tkind);
					return false;
				}
				// The behavior of TypeMirror.toString() is suggested, not required, by its javadoc.
				// So, this is a test of whether we behave like javac, rather than whether we meet the spec.
				String pname = param1Type.toString();
				if (_reportFailingCases && !"M2.M3.M4".equals(pname)) {
					reportError("Expected toString() of the type of Negative3.foo() param to be M2.M3.M4, but found " + pname);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check the model of resources/targets.negative.pa.Negative4
	 * @return true if all tests passed
	 */
	public boolean checkNegative4() throws Exception {
		TypeElement elementN4 = _elementUtils.getTypeElement("targets.negative.pa.Negative4");
		if (null == elementN4 || elementN4.getKind() != ElementKind.CLASS) {
			reportError("Element Negative4 was not found or was not a class");
			return false;
		}

		return checkModel(Collections.singletonList(elementN4), NEGATIVE_4_MODEL, "Negative4");
	}


	public boolean checkNegative5() throws Exception {
		List<TypeElement> rootElements = new ArrayList<>();
		TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative5");
		if (null == element) {
			reportError("Element Negative5 was not found");
			return false;
		}
		rootElements.add(element);
		element = _elementUtils.getTypeElement("targets.negative.pa.INegative5");
		if (null == element) {
			reportError("Element INegative5 was not found");
			return false;
		}
		rootElements.add(element);

		return checkModel(rootElements, NEGATIVE_5_MODEL, "Negative5");
	}

	/**
	 * Check the model of resources/targets.negative.pa.Negative6
	 * @return true if all tests passed
	 */
	public boolean checkNegative6() throws Exception {

		// Get the root of the Negative6 model
		TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative6");
		if (null == element || element.getKind() != ElementKind.CLASS) {
			reportError("Element Negative6 was not found or was not a class");
			return false;
		}

		return checkModel(Collections.singletonList(element), NEGATIVE_6_MODEL, "Negative6");
	}

	/**
	 * Check the model of resources/targets.negative.pa.Negative7
	 * @return true if all tests passed
	 */
	public boolean checkNegative7() throws Exception {

		// Get the roots of the Negative7 model
		List<TypeElement> rootElements = new ArrayList<>();
		TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative7");
		if (null == element) {
			reportError("Element Negative7 was not found");
			return false;
		}
		rootElements.add(element);
		element = _elementUtils.getTypeElement("targets.negative.pa.Negative7A");
		if (null == element) {
			reportError("Element Negative7A was not found");
			return false;
		}
		rootElements.add(element);

		return checkModel(rootElements, NEGATIVE_7_MODEL, "Negative7");
	}

	/**
	 * Check the model of resources/targets.negative.pa.Negative8
	 * @return true if all tests passed
	 */
	public boolean checkNegative8() throws Exception {
		// check that all expected elements are here
		List<TypeElement> rootElements = new ArrayList<>();
		String[] suffixes = new String[] {"a", "b", "c", "d", "e", "f"};
		for (int i = 0, l = suffixes.length; i < l; i++) {
			TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative8" + suffixes[i]);
			if (null == element) {
				reportError("Element Negative8" + suffixes[i] + " was not found");
				return false;
			}
			rootElements.add(element);
		}
		if (this.processingEnv.getSourceVersion() == SourceVersion.RELEASE_6) {
			if (!checkModel(rootElements, NEGATIVE_8_MODEL_VERSION6, "Negative8")) {
				return false;
			}
		} else if (!checkModel(rootElements, NEGATIVE_8_MODEL, "Negative8")) {
			return false;
		}
		return true;
	}
	/**
	 * Check the model of resources/targets.negative.pa.Negative9
	 * @return true if all tests passed
	 */
	public boolean checkNegative9() throws Exception {
		// check that all expected elements are here
		List<TypeElement> rootElements = new ArrayList<>();
		String[] suffixes = new String[] {"a", "b"};
		for (int i = 0, l = suffixes.length; i < l; i++) {
			TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative9" + suffixes[i]);
			if (null == element) {
				reportError("Element Negative9" + suffixes[i] + " was not found");
				return false;
			}
			rootElements.add(element);
		}
		if (this.processingEnv.getSourceVersion() == SourceVersion.RELEASE_6) {
			if (!checkModel(rootElements, NEGATIVE_9_MODEL_VERSION6, "Negative9")) {
				return false;
			}
			return true;
		} else {
			if (!checkModel(rootElements, NEGATIVE_9_MODEL, "Negative9")) {
				return false;
			}
			// check that specific elements are not here
			suffixes = new String[] { "b" };
			boolean result = true;
			String errorMessage = "";
			for (int i = 0, l = suffixes.length; i < l; i++) {
				TypeElement element = _elementUtils.getTypeElement("targets.negative.pa.Negative9" + suffixes[i]);
				List<? extends TypeMirror> interfaces = element.getInterfaces();
				if (interfaces.isEmpty()) {
					errorMessage += "Element Negative9" + suffixes[i] + " has missing interfaces\n";
					result = false;
				}
			}
			if (!result) {
				reportError(errorMessage);
			}
			return result;
		}
	}
	/**
	 * Compare a set of elements to a reference model, and output error information if there is a
	 * mismatch.
	 *
	 * @param expected
	 *            a string representation of the XML reference model, as it would be serialized by
	 *            XMLConverter
	 * @param name
	 *            the name of the test, which is used for human-readable output
	 * @return true if the actual and expected models were equivalent
	 */
	private boolean checkModel(List<TypeElement> rootElements, String expected, String name) throws Exception {
		Document actualModel = XMLConverter.convertModel(rootElements);

    	InputSource source = new InputSource(new StringReader(expected));
        Document expectedModel = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE().parse(source);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder summary = new StringBuilder();
        summary.append("Test ").append(name).append(" failed; see console for details.  ");
        boolean success = XMLComparer.compare(actualModel, expectedModel, out, summary, _ignoreJavacBugs);
        if (!success) {
        	System.out.println("Test " + name + " failed.  Detailed output follows:");
        	System.out.print(out.toString());
        	System.out.println("Cut and paste:");
        	System.out.println(XMLConverter.xmlToCutAndPasteString(actualModel, 0, false));
        	System.out.println("=============== end output ===============");
        	reportError(summary.toString());
        }
        return success;
	}

	/**
	 * Find a particular annotation on a specified element.
	 * @param el the annotated element
	 * @param name the simple name of the annotation
	 * @return a mirror for the annotation, or null if the annotation was not found.
	 */
	private AnnotationMirror findAnnotation(Element el, String name) {
		for (AnnotationMirror am : el.getAnnotationMirrors()) {
			DeclaredType annoType = am.getAnnotationType();
			if (null != annoType) {
				Element annoTypeElement = annoType.asElement();
				if (null != annoTypeElement) {
					if (name.equals(annoTypeElement.getSimpleName().toString())) {
						return am;
					}
				}
			}
		}
		return null;
	}
}
