/*******************************************************************************
 * Copyright (c) 2024 Kamil Krzywanski
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kamil Krzywanski - initial creation if Interesection type and Implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;

@SupportedAnnotationTypes("targets.issue565.Annotation565")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Issue565Processor extends AbstractProcessor {
	private static boolean status = false;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			// We're not interested in the postprocessing round.
			return false;
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(annotations.stream().findAny().get())) {
			if (element instanceof TypeElement) {
				keyBuilder.visit(element.asType(), true);
			}
		}
		status = true;
		return false;
	}

	public static boolean status() {
		return status;
	}

	// This is a fragment of querydsl visitor
	private final TypeVisitor<List<String>, Boolean> keyBuilder = new SimpleTypeVisitor8<>() {
		private final List<String> defaultValue = Collections.singletonList("Object");

		private List<String> visitBase(TypeMirror t) {
			List<String> rv = new ArrayList<>();
			String name = t.toString();
			if (name.contains("<")) {
				name = name.substring(0, name.indexOf('<'));
			}
			rv.add(name);
			return rv;
		}

		@Override
		public List<String> visitDeclared(DeclaredType t, Boolean p) {
			List<String> rv = visitBase(t);
			for (TypeMirror arg : t.getTypeArguments()) {
				if (p) {
					rv.addAll(visit(arg, false));
				} else {
					rv.add(arg.toString());
				}
			}
			return rv;
		}

		@Override
		public List<String> visitTypeVariable(TypeVariable t, Boolean p) {
			List<String> rv = visitBase(t);
			if (t.getUpperBound() != null) {
				rv.addAll(visit(t.getUpperBound(), p));
			}
			if (t.getLowerBound() != null) {
				rv.addAll(visit(t.getLowerBound(), p));
			}
			return rv;
		}

		@Override
		public List<String> visitIntersection(IntersectionType t, Boolean p) {
			return t.getBounds().get(0).accept(this, p);
		}

		@Override
		public List<String> visitNull(NullType t, Boolean p) {
			return defaultValue;
		}
	};
}
