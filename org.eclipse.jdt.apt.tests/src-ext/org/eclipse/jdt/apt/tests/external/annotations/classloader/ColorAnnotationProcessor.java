/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.external.annotations.classloader;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;

public class ColorAnnotationProcessor implements AnnotationProcessor {

	public static final String PROP_KEY = "ColorAnnotationProcessor";
	public static final String SUCCESS_VALUE = "success";

	private final AnnotationProcessorEnvironment _env;

	public ColorAnnotationProcessor(AnnotationProcessorEnvironment env) {
		_env = env;
	}

	public void process() {
		System.setProperty(PROP_KEY, "Beginning processing");
		try {
			TypeDeclaration typeDecl = _env.getTypeDeclaration("colortestpackage.ColorTest");

			ColorAnnotation colorAnno = typeDecl.getAnnotation(ColorAnnotation.class);

			Color color = colorAnno.color();

			if (color != Color.RED)
				throw new IllegalStateException("Expecting red, but got: " + color);

			ColorWrapper colorWrapper = typeDecl.getAnnotation(ColorWrapper.class);
			ColorAnnotation[] colorAnnoArray = colorWrapper.colors();

			if (colorAnnoArray[0].color() != Color.GREEN)
				throw new IllegalStateException("Expecting green, but got: " + color);

			if (colorAnnoArray[1].color() != Color.BLUE)
				throw new IllegalStateException("Expecting blue, but got: " + color);
		}
		catch (Throwable t) {
			t.printStackTrace();
			System.setProperty(PROP_KEY, "Failed");
		}
		System.setProperty(PROP_KEY, SUCCESS_VALUE);

	}

	public static boolean wasSuccessful() {
		return SUCCESS_VALUE.equals(System.getProperty(PROP_KEY));
	}

}
