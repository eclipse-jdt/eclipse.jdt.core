/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
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


package org.eclipse.jdt.apt.tests.annotations.mirrortest;

/**
 * Holds information for the mirror tests.
 */
public class CodeExample {

	public static final String CODE_PACKAGE = "mirrortestpackage";

	public static final String CODE_CLASS_NAME = "MirrorTestClass";

	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"package mirrortestpackage;\n" +
        "\n" +
        "import java.io.Serializable;\n" +
        "import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorTestAnnotation;\n" +
        "\n" +
        "public class MirrorTestClass implements Serializable {\n" +
        "\n" +
        "    public static final String STATIC_FIELD = \"Static Field\";\n" +
        "\n" +
        "    private static final long serialVersionUID = 42L;\n" +
        "\n" +
        "    public String field;\n" +
        "\n" +
        "    public MirrorTestClass() {\n" +
        "        field = \"Field\";\n" +
        "    }\n" +
        "\n" +
        "    @MirrorTestAnnotation\n" +
        "    public static Object staticMethod() {\n" +
        "        return null;\n" +
        "    }\n" +
        "\n" +
        "    public String stringMethod() {\n" +
        "        return null;\n" +
        "    }\n" +
        "\n" +
		"\n" +
        "    public String toString() {\n" +
        "        return null;\n" +
        "    }\n" +
        "\n" +
        "    public static class InnerClass extends MirrorTestClass {\n" +
		"\n" +
        "        private static final long serialVersionUID = 148L;\n" +
        "\n" +
        "        public static Object staticMethod() {\n" +
        "            return null;\n" +
        "        }\n" +
        "\n" +
        "    }\n" +
        "}";
}
