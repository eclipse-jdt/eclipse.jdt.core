/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration; 

import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.TypeVisitor;
import java.util.Collection;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class AnnotationDeclarationImpl extends InterfaceDeclarationImpl implements AnnotationTypeDeclaration, AnnotationType
{    
    public AnnotationDeclarationImpl(final ITypeBinding binding, final ProcessorEnvImpl env)
    {
        super(binding, env);
        assert binding.isAnnotation() : "binding does not represent a annotation ";
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitAnnotationTypeDeclaration(this);
    }

    public Collection<AnnotationTypeElementDeclaration> getMethods()
    {
        return (Collection<AnnotationTypeElementDeclaration>)_getMethods();
    }

    // start of implementation of AnnotationType API
    public void accept(TypeVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitAnnotationType(this);
    }

    public AnnotationTypeDeclaration getDeclaration()
    {
        return (AnnotationTypeDeclaration)super.getDeclaration();
    }
    // end of implementation of AnnotationType API

    public MirrorKind kind(){ return MirrorKind.TYPE_ANNOTATION; }
}
