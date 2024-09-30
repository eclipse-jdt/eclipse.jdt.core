/*******************************************************************************
 * Copyright (c) 2005, 2013 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class AnnotationDeclarationImpl extends InterfaceDeclarationImpl implements AnnotationTypeDeclaration, AnnotationType
{
    public AnnotationDeclarationImpl(final ITypeBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isAnnotation() : "binding does not represent a annotation "; //$NON-NLS-1$
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitAnnotationTypeDeclaration(this);
    }

    @Override
	@SuppressWarnings("unchecked") // _getMethods() return type is too broadly specified
	public Collection<AnnotationTypeElementDeclaration> getMethods()
    {
        return (Collection<AnnotationTypeElementDeclaration>)_getMethods();
    }

    // start of implementation of AnnotationType API
    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitAnnotationType(this);
    }

    @Override
	public AnnotationTypeDeclaration getDeclaration()
    {
        return (AnnotationTypeDeclaration)super.getDeclaration();
    }
    // end of implementation of AnnotationType API

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_ANNOTATION; }
}
