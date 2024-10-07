/*******************************************************************************
 * Copyright (c) 2007, 2023 BEA Systems, Inc and others.
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
 *    IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.visitors;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractAnnotationValueVisitor6;
import javax.lang.model.util.ElementScanner6;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * Processor that tests a variety of Visitors
 */
@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class VisitorProc extends BaseProcessor
{
	/**
	 * This visitor is invoked on the top-level types in resources/targets/model.
	 * We expect to see each of the visitX() methods get hit as a result.
	 * @deprecated
	 */
	private static class ElementVisitorTester extends ElementScanner6<Void, Void> {

		public enum Visited { TYPE, EXECUTABLE, VARIABLE, TYPEPARAM, PACKAGE, UNKNOWN }

		private final EnumSet<Visited> _visited = EnumSet.noneOf(Visited.class);

		public boolean checkVisits() {
			boolean asExpected = true;
			asExpected &= _visited.contains(Visited.TYPE);
			asExpected &= _visited.contains(Visited.EXECUTABLE);
			asExpected &= _visited.contains(Visited.VARIABLE);
			// TODO: Following two cases not yet implemented:
			//asExpected &= _visited.contains(Visited.TYPEPARAM);
			//asExpected &= _visited.contains(Visited.PACKAGE);
			return asExpected;
		}

        /**
         * Check that we can visit types.
         * @return true if all tests passed
         */
        @Override
        public Void visitType(TypeElement e, Void p) {
        	_visited.add(Visited.TYPE);
        	// Scan the type's subtypes, fields, and methods
            return super.visitType(e, p);
        }

        /**
         * Check that we can visit methods.
         */
        @Override
        public Void visitExecutable(ExecutableElement e, Void p) {
        	_visited.add(Visited.EXECUTABLE);
        	// Scan the method's parameters
            return super.visitExecutable(e, p);
        }

        /**
         * Check that we can visit variables.
         */
        @Override
        public Void visitVariable(VariableElement e, Void p) {
        	_visited.add(Visited.VARIABLE);
            // Variables do not enclose any elements, so no need to call super.
        	return null;
        }

        /**
         * Check that we can visit type parameters.
         */
        @Override
        public Void visitTypeParameter(TypeParameterElement e, Void p) {
        	_visited.add(Visited.TYPEPARAM);
            // Type parameters do not enclose any elements, so no need to call super.
        	return null;
        }

        /**
         * Check that we can visit packages.
         */
        @Override
        public Void visitPackage(PackageElement e, Void p) {
        	_visited.add(Visited.PACKAGE);
            // We don't want to scan the package's types here, so don't call super.
        	return null;
        }

        /**
         * This should not actually be encountered.
         */
        @Override
        public Void visitUnknown(Element e, Void p) {
        	_visited.add(Visited.UNKNOWN);
        	return null;
        }

	}

	/**
	 * The specific values checked by this visitor correspond to values in targets.model.pc.TypedAnnos.java
	 * @deprecated
	 */
	private static class AnnotationVisitorTester extends AbstractAnnotationValueVisitor6<Void, Void> {

		public enum Visited { ANNOTATION, ARRAY, BOOLEAN, BYTE, CHAR, DOUBLE, ENUMCONSTANT, FLOAT, INT, LONG, SHORT, STRING, TYPE }

		private final EnumSet<Visited> _visited = EnumSet.noneOf(Visited.class);

		public boolean checkVisits() {
			boolean asExpected = true;
			asExpected &= _visited.contains(Visited.ANNOTATION);
			asExpected &= _visited.contains(Visited.ARRAY);
			asExpected &= _visited.contains(Visited.BOOLEAN);
			asExpected &= _visited.contains(Visited.BYTE);
			asExpected &= _visited.contains(Visited.CHAR);
			asExpected &= _visited.contains(Visited.DOUBLE);
			asExpected &= _visited.contains(Visited.ENUMCONSTANT);
			asExpected &= _visited.contains(Visited.FLOAT);
			asExpected &= _visited.contains(Visited.INT);
			asExpected &= _visited.contains(Visited.LONG);
			asExpected &= _visited.contains(Visited.SHORT);
			asExpected &= _visited.contains(Visited.STRING);
			asExpected &= _visited.contains(Visited.TYPE);
			return asExpected;
		}

		@Override
		public Void visitAnnotation(AnnotationMirror a, Void p)
		{
			if (a != null && a.getElementValues() != null) {
				_visited.add(Visited.ANNOTATION);
			}
			// we could scan the values of the nested annotation here, but that doesn't help our test case
			return null;
		}

		@Override
		public Void visitArray(List<? extends AnnotationValue> vals, Void p)
		{
			if ( null != vals && vals.size() == 2 ) {
				if ( vals.iterator().next().getValue() instanceof TypeMirror) {
					_visited.add(Visited.ARRAY);
				}
			}
			// we could scan the array values here, but that doesn't help our test case
			return null;
		}

		@Override
		public Void visitBoolean(boolean b, Void p)
		{
			if (b) {
				_visited.add(Visited.BOOLEAN);
			}
			return null;
		}

		@Override
		public Void visitByte(byte b, Void p)
		{
			if (b == 3) {
				_visited.add(Visited.BYTE);
			}
			return null;
		}

		@Override
		public Void visitChar(char c, Void p)
		{
			if (c == 'c') {
				_visited.add(Visited.CHAR);
			}
			return null;
		}

		@Override
		public Void visitDouble(double d, Void p)
		{
			if (d == 6.3) {
				_visited.add(Visited.DOUBLE);
			}
			return null;
		}

		@Override
		public Void visitEnumConstant(VariableElement c, Void p)
		{
			if (c.getKind() == ElementKind.ENUM_CONSTANT) {
				if ("A".equals(c.getSimpleName().toString())) {
					_visited.add(Visited.ENUMCONSTANT);
				}
			}
			return null;
		}

		@Override
		public Void visitFloat(float f, Void p)
		{
			if (f == 26.7F) {
				_visited.add(Visited.FLOAT);
			}
			return null;
		}

		@Override
		public Void visitInt(int i, Void p)
		{
			if (i == 19) {
				_visited.add(Visited.INT);
			}
			return null;
		}

		@Override
		public Void visitLong(long i, Void p)
		{
			if (i == 300L) {
				_visited.add(Visited.LONG);
			}
			return null;
		}

		@Override
		public Void visitShort(short s, Void p)
		{
			if (s == 289) {
				_visited.add(Visited.SHORT);
			}
			return null;
		}

		@Override
		public Void visitString(String s, Void p)
		{
			if ("foo".equals(s)) {
				_visited.add(Visited.STRING);
			}
			return null;
		}

		@Override
		public Void visitType(TypeMirror t, Void p)
		{
			if ("java.lang.Exception".equals(t.toString())) {
				_visited.add(Visited.TYPE);
			}
			return null;
		}

	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) {
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}
		ElementVisitorTester elementVisitor = new ElementVisitorTester();
		elementVisitor.scan(roundEnv.getRootElements(), null);
		if (!elementVisitor.checkVisits()) {
			reportError("Element visitor was not visited as expected");
			return false;
		}

		AnnotationVisitorTester annoValVisitor = new AnnotationVisitorTester();
		TypeElement typedAnnosDecl = _elementUtils.getTypeElement("org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos");
		if (null == typedAnnosDecl) {
			reportError("Couldn't find targets.model.pc.AnnotatedWithManyTypes");
			return false;
		}
		for (TypeElement anno : annotations) {
			if (typedAnnosDecl.equals(anno.getEnclosingElement())) {
				for (Element elem : roundEnv.getElementsAnnotatedWith(anno)) {
					for (AnnotationMirror annoMirror : elem.getAnnotationMirrors()) {
						if (anno.equals(annoMirror.getAnnotationType().asElement())) {
							Map<? extends ExecutableElement, ? extends AnnotationValue> values = annoMirror.getElementValues();
							for (AnnotationValue val : values.values()) {
								val.accept(annoValVisitor, null);
							}
						}
					}
				}
			}
		}
		if (!annoValVisitor.checkVisits()) {
			reportError("Annotation value visitor was not visited as expected");
			return false;
		}

		reportSuccess();
		return false;
	}

}
