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
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

public class ReadAnnotationTypeProcessor extends BaseProcessor {

    private final AnnotationTypeDeclaration _annotationType;

    public ReadAnnotationTypeProcessor(Set<AnnotationTypeDeclaration> declarationTypes, AnnotationProcessorEnvironment env) {
        super(env);
        assert declarationTypes.size() == 1;
        _annotationType = declarationTypes.iterator().next();
    }

    public void process() {
        try
        {
            Collection<Declaration> declarations = _env.getDeclarationsAnnotatedWith(_annotationType);
            assert declarations.size() == 1;
            new AnnotationReader().createClassFilesForAnnotatedDeclarations(declarations, _env);
        } catch (Throwable e)
        {
            e.printStackTrace();
            _env.getMessager().printError(e.getMessage());
        }
    }


}
