/*******************************************************************************
 * Copyright (c) 2007, 2015 BEA Systems, Inc. and others
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
 *    IBM Corporation - fix for 342936
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.messager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

/**
 * A processor that uses the Messager interface to report errors against various
 * elements in the targets.model resource hierarchy.  To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.messager.MessagerProc to the command line.
 * <p>
 * The idea of this processor is that it calls the Messager interface with various messages
 * on various elements.  If the interface itself fails, an error is reported via the
 * reportError() method, which sets a system property that the calling test case will
 * inspect.  Then, following processor execution, the calling test case will inspect all
 * the messages that were passed to Messager, to make sure that they all made it into the
 * compiler error output in the expected way.
 *
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions("org.eclipse.jdt.compiler.apt.tests.processors.messager.MessagerProc")
public class MessagerProc extends AbstractProcessor {

	private static final String CLASSNAME = MessagerProc.class.getName();

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
	//private Types _typeUtils;
	private Messager _messager;

	// Initialized in collectElements()
	private TypeElement _elementD;

	// Initialized in collectElements()
//	private ExecutableElement _methodElement;

	// Initialized in collectElements()
	private TypeElement _element2;

	// Initialized in collectElements()
	private AnnotationMirror _annotationMirror;

	private AnnotationMirror _nestedAnnotation;

	// Initialized in collectElements()
	private AnnotationValue _annotationValue;

	// Initialized in collectElements()
	private TypeElement _elementE;

	// Initialized in collectElements()
	private ExecutableElement _methodElement;

	// Initialized in collectElements()
	private VariableElement _variableElement;

	// Initialized in collectElements()
	private TypeElement _elementF;

	// Initialized in collectElements()
	private VariableElement _parameterElement;

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
		//_typeUtils = processingEnv.getTypeUtils();
		_messager = processingEnv.getMessager();
	}

	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			// We're not interested in the postprocessing round.
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(CLASSNAME)) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}

		if (null == _messager) {
			reportError("Env.getMessager() returned null");
			return false;
		}

		if (!collectElements()) {
			return false;
		}

		if (!printErrorsOnElements()) {
			return false;
		}

		MessagerProc.reportSuccess();
		return false;
	}

	/**
	 * Collect some elements that will be reused in various tests
	 * @return true if all tests passed
	 */
	private boolean collectElements() {
		_elementD = _elementUtils.getTypeElement("targets.errors.pb.D");
		if (null == _elementD || _elementD.getKind() != ElementKind.CLASS) {
			reportError("Element D was not found or was not a class");
			return false;
		}
//		printVariableElements(_elementD);

		for (ExecutableElement method : ElementFilter.methodsIn(_elementD.getEnclosedElements())) {
			if ("methodDvoid".equals(method.getSimpleName().toString())) {
				List<? extends VariableElement> params = method.getParameters();
				if (params.size() < 1) {
					reportError("D.methodDvoid() had no parameters");
					return false;
				}
				_parameterElement = params.get(0);
			}
		}

		_elementE = _elementUtils.getTypeElement("targets.errors.pb.E");
		if (null == _elementE || _elementE.getKind() != ElementKind.CLASS) {
			reportError("Element E was not found or was not a class");
			return false;
		}
//		printVariableElements(_elementE);

		_elementF = _elementUtils.getTypeElement("targets.errors.pb.F");
		if (null == _elementF || _elementF.getKind() != ElementKind.CLASS) {
			reportError("Element F was not found or was not a class");
			return false;
		}
//		printVariableElements(_elementF);

		List<? extends Element> enclosedElements = _elementE.getEnclosedElements();
		for (Element element : enclosedElements) {
			switch(element.getKind()) {
				case METHOD :
					ExecutableElement executableElement = (ExecutableElement) element;
					StringBuilder builder = new StringBuilder(executableElement.getSimpleName());
					String name = String.valueOf(builder);
					if ("foo".equals(name) && _methodElement == null) {
						_methodElement = executableElement;
					}
					break;
				case FIELD :
					VariableElement variableElement = (VariableElement) element;
					builder = new StringBuilder(variableElement.getSimpleName());
					name = String.valueOf(builder);
					if ("j".equals(name) && _variableElement == null) {
						_variableElement = variableElement;
					}
					break;
				default:
					break;
			}
		}

		if (_methodElement == null) {
			reportError("Element for method foo could not be found");
			return false;
		}

		if (_variableElement == null) {
			reportError("Element for field j could not be found");
			return false;
		}

		List<? extends AnnotationMirror> annotationMirrors = _elementD.getAnnotationMirrors();
		for (AnnotationMirror mirror : annotationMirrors) {
			if (_annotationMirror == null) {
				_annotationMirror = mirror;
			} else if (_nestedAnnotation == null) {
				Collection<? extends AnnotationValue> values = mirror.getElementValues().values();
				for (AnnotationValue annotationValue : values) {
					_nestedAnnotation = (AnnotationMirror) annotationValue.getValue();
				}
			} else {
				break;
			}
		}
		if (_annotationMirror == null) {
			reportError("Annotation mirror was not found");
			return false;
		}
		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = _annotationMirror.getElementValues();
		Collection<? extends AnnotationValue> values = elementValues.values();
		for (AnnotationValue value : values) {
			if (_annotationValue == null) {
				_annotationValue = value;
				break;
			}
		}
		if (_annotationValue == null) {
			reportError("Annotation value was not found");
			return false;
		}

		_element2 = _elementUtils.getTypeElement("java.lang.String");
		if (_element2 == null) {
			reportError("Element for java.lang.String could not be found");
			return false;
		}
//		printVariableElements(_element2);

		return true;
	}

	static void printVariableElements(final TypeElement typeElement) {
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		for (Element element : enclosedElements) {
			switch(element.getKind()) {
				case ENUM :
					System.out.println("enum type : " + element.getSimpleName());
					break;
				case CLASS :
					System.out.println("class : " + element.getSimpleName());
					break;
				case INSTANCE_INIT :
					System.out.println("initializer : " + element.getSimpleName());
					break;
				case STATIC_INIT :
					System.out.println("static initializer : " + element.getSimpleName());
					break;
				case FIELD :
					System.out.println("field : " + element.getSimpleName());
					break;
				case CONSTRUCTOR :
					System.out.println("constructor : " + element.getSimpleName());
					ExecutableElement executableElement = (ExecutableElement) element;
					List<? extends VariableElement> parameters = executableElement.getParameters();
					for (VariableElement variableElement : parameters) {
						System.out.print("name = " + variableElement.getSimpleName());
						TypeMirror typeMirror = variableElement.asType();
						System.out.print(" type = " + typeMirror);
						System.out.println(" type kind = " + typeMirror.getKind());
					}
					break;
				case METHOD :
					System.out.println("method : " + element.getSimpleName());
					executableElement = (ExecutableElement) element;
					parameters = executableElement.getParameters();
					for (VariableElement variableElement : parameters) {
						System.out.print("name = " + variableElement.getSimpleName());
						TypeMirror typeMirror = variableElement.asType();
						System.out.print(" type = " + typeMirror);
						System.out.println(" type kind = " + typeMirror.getKind());
					}
					break;
				default:
					System.out.println(element.getKind() + ": " + element.getSimpleName());
					break;
			}
		}
	}

	private boolean printErrorsOnElements() {
		_messager.printMessage(Kind.NOTE, "Informational message not associated with an element");
		_messager.printMessage(Kind.ERROR, "Error on element D", _elementD);
		_messager.printMessage(Kind.ERROR, "Error on element D", _elementD, _annotationMirror);
		_messager.printMessage(Kind.ERROR, "Error on element D", _elementD, _nestedAnnotation);
		_messager.printMessage(Kind.ERROR, "Error on element D", _elementD, _annotationMirror, _annotationValue);
		_messager.printMessage(Kind.ERROR, "Error on element java.lang.String", _element2);
		_messager.printMessage(Kind.WARNING, "Warning on method foo", _methodElement);
		_messager.printMessage(Kind.NOTE, "Note for field j", _variableElement);
		_messager.printMessage(Kind.WARNING, "Warning on parameter of D.methodDvoid", _parameterElement);
		return true;
	}
}
