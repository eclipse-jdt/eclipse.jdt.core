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

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

public class ReadAnnotationTypeProcessorFactory extends BaseFactory {

    public ReadAnnotationTypeProcessorFactory() {
        super(SimpleAnnotation.class.getName());
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations, AnnotationProcessorEnvironment env) {
        return new ReadAnnotationTypeProcessor(declarations, env);
    }

}
