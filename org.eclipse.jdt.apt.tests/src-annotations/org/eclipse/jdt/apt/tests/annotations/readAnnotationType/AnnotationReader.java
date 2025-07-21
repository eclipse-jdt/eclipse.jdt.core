/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.readAnnotationType;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.Declaration;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class AnnotationReader {
    public void createClassFilesForAnnotatedDeclarations(
            Collection<Declaration> declarations,
            AnnotationProcessorEnvironment env) throws IOException {
        assert declarations.size() == 1;
        for (Declaration dec : declarations) {

            PrintWriter writer = null;
            try {
                SimpleAnnotation annotation = dec
                        .getAnnotation(SimpleAnnotation.class);
                String value = annotation.value().name();
                //Class annotationType = annotation.annotationType();
                String className = value + "Gen";
                writer = env.getFiler().createSourceFile("test." + className);
                writer.println("package test;");
                writer.println("public class " + className);
                writer.println("{}");
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
}
