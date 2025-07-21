/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
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
package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("targets.bug510118.Annotation510118")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug510118Processor extends AbstractProcessor {

	private static final String INTERFACE_NAME = "targets.bug510118.I";
	private static final String CLASS_NAME = "targets.bug510118.A";
	private static final String TYPE_ARGUMENT_NAME =  "targets.bug510118.TypeParam";
	private static boolean status = false;

	private static final class DeclaredTypeVisitor extends CastingTypeVisitor<DeclaredType> {
		private static final DeclaredTypeVisitor INSTANCE = new DeclaredTypeVisitor();

		DeclaredTypeVisitor() {
			super("declared type");
		}

		@Override
		public DeclaredType visitDeclared(DeclaredType type, Void ignore) {
			return type;
		}
	}

	private abstract static class CastingTypeVisitor<T> extends SimpleTypeVisitor6<T, Void> {
		private final String label;

		@SuppressWarnings("deprecation")
		CastingTypeVisitor(String label) {
			this.label = label;
		}

		@Override
		protected T defaultAction(TypeMirror e, Void v) {
			throw new IllegalArgumentException(e + " does not represent a " + label);
		}
	}

	public Bug510118Processor() {
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!roundEnv.processingOver()) {
			TypeElement testClass = null;
			Messager messager = processingEnv.getMessager();
			try {
				Elements elements = processingEnv.getElementUtils();
				testClass = elements.getTypeElement(CLASS_NAME);

				TypeElement typeAdapterElement = elements.getTypeElement(INTERFACE_NAME);
				TypeParameterElement param = typeAdapterElement.getTypeParameters().get(0);
				DeclaredType declaredType = testClass.asType().accept(DeclaredTypeVisitor.INSTANCE, null);
				TypeMirror tm = processingEnv.getTypeUtils().asMemberOf(declaredType, param);
				if (tm != null) {
					testClass = elements.getTypeElement(TYPE_ARGUMENT_NAME);
					if (processingEnv.getTypeUtils().isSameType(tm, testClass.asType())) {
						status = true;
					}
				}
			} catch (Exception e) {
				messager.printMessage(Diagnostic.Kind.ERROR, ("Exception: " + e), testClass);
			}
		}
		return false;
	}
	public static boolean status() {
		return status;
	}

}
