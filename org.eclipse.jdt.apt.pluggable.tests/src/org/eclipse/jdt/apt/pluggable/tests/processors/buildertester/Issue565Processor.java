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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("targets.issue565.Annotation565")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Issue565Processor extends AbstractProcessor {
	private static boolean status = false;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		String classpath = System.getProperty("java.class.path");
		String[] classPathValues = classpath.split(File.pathSeparator);
		for (String classPath: classPathValues) {
			System.out.println(classPath);
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


	private final TypeVisitor<List<String>, Boolean> keyBuilder = new SimpleTypeVisitor6<List<String>, Boolean>() {

		private final List<String> defaultValue = Collections.singletonList("Object");


		private static final Class<?> intersectionTypeClass;

		private static final Method getBoundsMethod;

		static {
			Class<?> availableClass;
			Method availableMethod;
			try {
				availableClass = Class.forName("javax.lang.model.type.IntersectionType");
				availableMethod = availableClass.getMethod("getBounds");
			} catch (Exception e) {
				// Not using Java 8
				availableClass = null;
				availableMethod = null;
			}
			intersectionTypeClass = availableClass;
			getBoundsMethod = availableMethod;
		}


		private List<String> visitBase(TypeMirror t) {
			List<String> rv = new ArrayList<String>();
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
		public List<String> visitUnknown(TypeMirror t, Boolean p) {
			if (intersectionTypeClass != null && intersectionTypeClass.isInstance(t)) {
				try {
					List<TypeMirror> bounds = (List<TypeMirror>) getBoundsMethod.invoke(t);
					return bounds.get(0).accept(this, p);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			} else {
				return super.visitUnknown(t, p);
			}
		}

		@Override
		public List<String> visitNull(NullType t, Boolean p) {
			return defaultValue;
		}
	};
}
