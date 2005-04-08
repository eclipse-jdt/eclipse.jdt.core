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
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class AnnotationElementDeclarationImpl extends MethodDeclarationImpl implements AnnotationTypeElementDeclaration
{
    public AnnotationElementDeclarationImpl(final IMethodBinding binding,
                                            final ProcessorEnvImpl env)
    {
        super(binding, env);
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitAnnotationTypeElementDeclaration(this);
    }

    public AnnotationTypeDeclaration getDeclaringType()
    {
        return (AnnotationTypeDeclaration)super.getDeclaringType();
    }

	/**
	 * @return the default value of this annotation element if one exists.
	 * 		   Return null if the annotation element is defined in binary (feature not available right now).
	 *         Return null if the annotation element is part of a seconary type that is defined outside
	 *         the file associated with the environment. 
	 */
    public AnnotationValue getDefaultValue()
    {   
        if( isFromSource() ){
            final AnnotationTypeMemberDeclaration astNode = (AnnotationTypeMemberDeclaration)getAstNode();
			if( astNode == null ) return null;
            final Expression defaultExpr = astNode.getDefault();
            if(defaultExpr == null ) return null;
            return Factory.createAnnotationValue(defaultExpr, this, _env);
        }
        else{
            // TODO: handle the binary case.            
            return null;
        }
    }

    public Collection<ParameterDeclaration> getParameters(){ return Collections.emptyList(); }

    public MirrorKind kind(){ return MirrorKind.ANNOTATION_ELEMENT; }
}
