/*******************************************************************************
 * Copyright (c) 2025 Kamil Krzywanski
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kamil Krzywanski - fix for including superclasses in intersection type in case of type variable
 *******************************************************************************/
package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.util.*;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;

@SupportedAnnotationTypes("targets.issue4446.Annotation4446")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Issue4446Processor extends AbstractProcessor {
    private static boolean status = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            // We're not interested in the postprocessing round.
            return false;
        }
        TypeElement anno = processingEnv.getElementUtils().getTypeElement("targets.issue4446.Annotation4446");
        if (anno == null) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(anno)) {
            scanElement(element);
        }
        status = true;
        return false;
    }
    public static boolean status() {
        return status;
    }


    private void scanElement(Element element) {
        switch (element.getKind()) {
            case CLASS:
            case INTERFACE:
            case ENUM:
                TypeElement typeElement = (TypeElement) element;
                for (TypeParameterElement tpe : typeElement.getTypeParameters()) {
                    handleTypeParameterBounds(tpe);
                    scanType(tpe.asType());
                }
                break;
            case METHOD:
                ExecutableElement method = (ExecutableElement) element;
                for (TypeParameterElement tpe : method.getTypeParameters()) {
                    handleTypeParameterBounds(tpe);
                }
                scanType(method.getReturnType());
                for (VariableElement param : method.getParameters()) {
                    scanType(param.asType());
                }
                break;
            default:
                break;
        }
        for (Element enclosed : element.getEnclosedElements()) {
            scanElement(enclosed);
        }
    }

    private void handleTypeParameterBounds(TypeParameterElement tpe) {
        List<? extends TypeMirror> bounds = tpe.getBounds();
        if (bounds != null) {
            for (TypeMirror b : bounds) {
                scanType(b);
            }
        }
        scanType(tpe.asType());
    }

    private void scanType(TypeMirror typeMirror) {
        if (typeMirror == null) return;
        if (Objects.requireNonNull(typeMirror.getKind()) == TypeKind.TYPEVAR) {
            TypeVariable tv = (TypeVariable) typeMirror;
            scanType(tv.getUpperBound());
            scanType(tv.getLowerBound());
        }
    }
}
