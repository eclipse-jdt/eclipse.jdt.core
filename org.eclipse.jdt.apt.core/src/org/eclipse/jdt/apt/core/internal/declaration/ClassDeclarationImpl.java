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

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import com.sun.mirror.util.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ClassDeclarationImpl extends TypeDeclarationImpl implements ClassDeclaration, ClassType
{
    public ClassDeclarationImpl(final ITypeBinding binding, final ProcessorEnvImpl env)
    {
        super(binding, env);
        // Enum types return false for isClass().
        assert !binding.isInterface();
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitClassDeclaration(this);
    }

    public Collection<ConstructorDeclaration> getConstructors()
    {
        final IMethodBinding[] methods = getDeclarationBinding().getDeclaredMethods();
        final List<ConstructorDeclaration> results = new ArrayList<ConstructorDeclaration>(methods.length);
        for( IMethodBinding method : methods ){
            if( method.isSynthetic() ) continue;
            if( method.isConstructor() ){
                Declaration mirrorDecl = Factory.createDeclaration(method, _env);
                if( mirrorDecl != null)
                    results.add((ConstructorDeclaration)mirrorDecl);
            }
        }
        return results;

    }

    public Collection<MethodDeclaration> getMethods()
    {
        return (Collection<MethodDeclaration>)_getMethods();
    }

    // Start of implementation of ClassType API
    public void accept(TypeVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitClassType(this);
    }

    public ClassType getSuperclass()
    {
        final ITypeBinding superClass = getDeclarationBinding().getSuperclass();
        if( superClass.isClass() )
            return (ClassType)Factory.createReferenceType(superClass, _env);
        else // catch error case where user extends some interface instead of a class.
            return Factory.createErrorClassType(superClass);
    }

    public ClassDeclaration getDeclaration()
    {
        return (ClassDeclaration)super.getDeclaration();        
    }
    // End of implementation of ClassType API

    public MirrorKind kind(){ return MirrorKind.TYPE_CLASS; }
}
