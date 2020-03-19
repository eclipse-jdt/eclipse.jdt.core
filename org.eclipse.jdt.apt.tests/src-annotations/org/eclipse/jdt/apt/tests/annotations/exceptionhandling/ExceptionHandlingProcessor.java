/*******************************************************************************
 * Copyright (c) 2006, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.exceptionhandling;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.exceptionhandling.ExceptionHandlingAnnotation.EHAEnum;

public class ExceptionHandlingProcessor extends BaseProcessor {

	private AnnotationTypeDeclaration _annotationType;

    public ExceptionHandlingProcessor(Set<AnnotationTypeDeclaration> declarationTypes, AnnotationProcessorEnvironment env) {
        super(env);
        assert declarationTypes.size() == 1;
        _annotationType = declarationTypes.iterator().next();
    }

    @SuppressWarnings("unused")
    public void process() {
		ProcessorTestStatus.setProcessorRan();
        Collection<Declaration> declarations = _env.getDeclarationsAnnotatedWith(_annotationType);
        assert declarations.size() == 1;
        for (Declaration dec : declarations) {
        	ExceptionHandlingAnnotation annotation = dec.getAnnotation(ExceptionHandlingAnnotation.class);
        	boolean booleanValue = annotation.booleanValue();
        	String strValue = annotation.strValue();
        	String[] arrValue = annotation.arrValue();
        	EHAEnum[] enumsValue = annotation.enumsValue();
        }
	}
}
