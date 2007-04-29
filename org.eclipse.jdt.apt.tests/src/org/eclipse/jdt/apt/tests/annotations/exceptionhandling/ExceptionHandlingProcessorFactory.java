/*******************************************************************************
 * Copyright (c) 2005, 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.exceptionhandling;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseFactory;


public class ExceptionHandlingProcessorFactory extends BaseFactory
{

	public ExceptionHandlingProcessorFactory() {
        super(ExceptionHandlingAnnotation.class.getName());
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations, AnnotationProcessorEnvironment env) {
        return new ExceptionHandlingProcessor(declarations, env);
    }
} 
