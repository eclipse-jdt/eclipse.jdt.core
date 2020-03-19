/*******************************************************************************
 * Copyright (c) 2010 Walter Harley
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.mirrortest;

/**
 * Holds information testing mirrors of annotations defined in source.
 */
public class SourceMirrorCodeExample {

	public static final String ANNO_CODE_CLASS_NAME = "SourceAnnotation";

	public static final String ANNO_CODE =
		"package mirrortestpackage;\n" +
		"\n" +
		"@interface SourceAnnotation {\n" +
		"    String value() default \"def\";\n" +
		"}\n";

	public static final String CODE_PACKAGE = "mirrortestpackage";

	public static final String CODE_CLASS_NAME = "MirrorTestClass2";

	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"package mirrortestpackage;\n" +
        "\n" +
		"import org.eclipse.jdt.apt.tests.annotations.generic.*;\n" +
		"@GenericAnnotation\n" +
        "@SourceAnnotation(\"nondef\")\n" +
        "public class MirrorTestClass2 {\n" +
        "}";
}
