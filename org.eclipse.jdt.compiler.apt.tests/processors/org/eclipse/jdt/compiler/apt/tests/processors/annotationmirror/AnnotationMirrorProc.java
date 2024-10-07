/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     het@google.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.annotationmirror;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that tests that the methods for {@link AnnotationMirror} work as
 * expected.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationMirrorProc extends BaseProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			// We're not interested in the postprocessing round.
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}

		if (!examineToString()) {
			return false;
		}

		if (!examineEscaping()) {
			return false;
		}

		reportSuccess();
		return false;
	}

	/**
	 * Tests that the {@link AnnotationValue#toString()} returns a
	 * {@link String} that is "in a form suitable for representing this value in
	 * the source code of an annotation."
	 * @return true if all tests pass
	 */
	private boolean examineToString() {
		TypeElement annotatedElement = _elementUtils.getTypeElement("targets.model.pc.AnnotatedWithManyTypes.Annotated");
		if (null == annotatedElement || annotatedElement.getKind() != ElementKind.CLASS) {
			reportError("examineToString: couldn't get AnnotatedWithManyTypes.Annotated element");
			return false;
		}
		final String badValue = "examineToString: unexpected value for ";
		List<? extends AnnotationMirror> annoMirrors = annotatedElement.getAnnotationMirrors();
		AnnotationMirror annoByte = annoMirrors.get(0);
		if (null == annoByte || !annoByte.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoByte(value = 3)")) {
			reportError(badValue + "AnnoByte");
			return false;
		}
		AnnotationMirror annoBoolean = annoMirrors.get(1);
		if (null == annoBoolean || !annoBoolean.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoBoolean(value = true)")) {
			reportError(badValue + "AnnoBoolean");
			return false;
		}
		AnnotationMirror annoChar = annoMirrors.get(2);
		if (null == annoChar || !annoChar.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoChar(value = 'c')")) {
			reportError(badValue + "AnnoChar");
			return false;
		}
		AnnotationMirror annoDouble = annoMirrors.get(3);
		if (null == annoDouble || !annoDouble.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoDouble(value = 6.3)")) {
			reportError(badValue + "AnnoDouble");
			return false;
		}
		AnnotationMirror annoFloat = annoMirrors.get(4);
		if (null == annoFloat || !annoFloat.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoFloat(value = 26.7)")) {
			reportError(badValue + "AnnoFloat");
			return false;
		}
		AnnotationMirror annoInt = annoMirrors.get(5);
		if (null == annoInt || !annoInt.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoInt(value = 19)")) {
			reportError(badValue + "AnnoInt");
			return false;
		}
		AnnotationMirror annoLong = annoMirrors.get(6);
		if (null == annoLong || !annoLong.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoLong(value = 300)")) {
			reportError(badValue + "AnnoLong");
			return false;
		}
		AnnotationMirror annoShort = annoMirrors.get(7);
		if (null == annoShort || !annoShort.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoShort(value = 289)")) {
			reportError(badValue + "AnnoShort");
			return false;
		}
		AnnotationMirror annoString = annoMirrors.get(8);
		if (null == annoString || !annoString.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoString(value = \"foo\")")) {
			reportError(badValue + "AnnoString");
			return false;
		}
		AnnotationMirror annoEnumConst = annoMirrors.get(9);
		if (null == annoEnumConst || !annoEnumConst.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoEnumConst(value = org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.Enum.A)")) {
			reportError(badValue + "AnnoEnumConst");
			return false;
		}
		AnnotationMirror annoType = annoMirrors.get(10);
		if (null == annoType || !annoType.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoType(value = java.lang.Exception.class)")) {
			reportError(badValue + "AnnoType");
			return false;
		}
		AnnotationMirror annoAnnoChar = annoMirrors.get(11);
		if (null == annoAnnoChar || !annoAnnoChar.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoAnnoChar(value = @org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoChar(value = 'x'))")) {
			reportError(badValue + "AnnoAnnoChar");
			return false;
		}
		AnnotationMirror annoArrayInt = annoMirrors.get(12);
		if (null == annoArrayInt || !annoArrayInt.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoArrayInt(value = {5, 8, 11})")) {
			reportError(badValue + "AnnoArrayInt");
			return false;
		}
		AnnotationMirror annoArrayString = annoMirrors.get(13);
		if (null == annoArrayString || !annoArrayString.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoArrayString(value = {\"bar\", \"quux\"})")) {
			reportError(badValue + "AnnoArrayString");
			return false;
		}
		AnnotationMirror annoArrayEnumConst = annoMirrors.get(14);
		if (null == annoArrayEnumConst || !annoArrayEnumConst.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoArrayEnumConst(value = {org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.Enum.B, org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.Enum.C})")) {
			reportError(badValue + "AnnoArrayEnumConst");
			return false;
		}
		AnnotationMirror annoArrayType = annoMirrors.get(15);
		if (null == annoArrayType || !annoArrayType.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoArrayType(value = {java.lang.String.class, targets.model.pc.AnnotatedWithManyTypes.Annotated.class})")) {
			reportError(badValue + "AnnoArrayType");
			return false;
		}
		AnnotationMirror annoArrayAnnoChar = annoMirrors.get(16);
		if (null == annoArrayAnnoChar || !annoArrayAnnoChar.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoArrayAnnoChar(value = {@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoChar(value = 'y'), @org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoChar(value = 'z')})")) {
			reportError(badValue + "AnnoArrayAnnoChar");
			return false;
		}
		return true;
	}

	private boolean examineEscaping() {
		TypeElement annotatedElement = _elementUtils.getTypeElement("targets.model.pc.K");
		if (null == annotatedElement || annotatedElement.getKind() != ElementKind.CLASS) {
			reportError("examineEscaping: couldn't get K element");
			return false;
		}
		final String badValue = "examineEscaping: unexpected value for ";
		List<? extends AnnotationMirror> annoMirrors = annotatedElement.getAnnotationMirrors();
		AnnotationMirror annoString = annoMirrors.get(0);
		if (null == annoString || !annoString.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoString(value = \"I'm \\\"special\\\": \\t\\\\\\n\")")) {
			reportError(badValue + "AnnoString");
			return false;
		}
		AnnotationMirror annoChar = annoMirrors.get(1);
		if (null == annoChar || !annoChar.toString().equals("@org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoChar(value = '\\'')")) {
			reportError(badValue + "AnnoChar");
			return false;
		}
		return true;
	}
}
