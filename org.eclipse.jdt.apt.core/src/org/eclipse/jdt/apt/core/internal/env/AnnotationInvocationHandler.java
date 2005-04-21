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

package org.eclipse.jdt.apt.core.internal.env; 

import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.MirroredTypesException;
import com.sun.mirror.type.TypeMirror;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class AnnotationInvocationHandler implements InvocationHandler
{
    private final AnnotationMirrorImpl _instance;

    public AnnotationInvocationHandler(final AnnotationMirrorImpl annotation)
    {
        _instance = annotation;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        final String methodName = method.getName();
        if( args == null || args.length == 0 )
        {
            if( methodName.equals("hashCode") )
                return new Integer( _instance.hashCode() );
            if( methodName.equals("toString") )
                return _instance.toString();
        }
        else if( args.length == 1 && methodName.equals("equals") )
        {
            return new Boolean( _instance.equals( args[0] ) );
        }
        if( args != null && args.length != 0 )
            throw new NoSuchMethodException("method " + method.getName() + formatArgs(args) + " does not exists");
        final String c_methodName = method.getName();
        final IMethodBinding methodBinding = _instance.getMethodBinding(c_methodName);
        if( methodBinding == null )
            throw new NoSuchMethodException("method " + method.getName() + "() does not exists");

        final ITypeBinding retType = methodBinding.getReturnType();
        // type of annotation member is java.lang.Class
        if( retType.isClass() && "java.lang.Class".equals(retType.getQualifiedName()) ){
            // need to figure out the class that's being accessed
            final ITypeBinding[] classTypes = _instance.getMemberValueTypeBinding(c_methodName);
            TypeMirror mirrorType = null;
            if( classTypes != null && classTypes.length > 0 ){
                mirrorType = Factory.createTypeMirror(classTypes[0], _instance.getEnvironment() );
            }
            if( mirrorType == null )
                mirrorType = Factory.createErrorClassType(classTypes[0]);
            throw new MirroredTypeException(mirrorType);
        }
        else if( retType.isArray() ){
            final ITypeBinding leafType = retType.getElementType();
            // type of annotation member is java.lang.Class[]
            if( leafType.isClass() && "java.lang.Class".equals(leafType.getQualifiedName()) ){
                final ITypeBinding[] classTypes = _instance.getMemberValueTypeBinding(c_methodName);
                final Collection<TypeMirror> mirrorTypes;
                if( classTypes == null || classTypes.length == 0 )
                    mirrorTypes = Collections.emptyList();
                else{
                    mirrorTypes = new ArrayList<TypeMirror>(classTypes.length);
                    for( ITypeBinding type : classTypes ){
                        TypeMirror mirror = Factory.createTypeMirror(type, _instance.getEnvironment() );
                        if( mirror == null )
                            mirrorTypes.add( Factory.createTypeMirror(type, _instance.getEnvironment() ) );
                        else
                            mirrorTypes.add(mirror);
                    }
                }

                throw new MirroredTypesException(mirrorTypes);
            }
        }

        return _instance.getReflectionValue(c_methodName, method);
    }

    private String formatArgs(final Object[] args)
    {
        // estimate that each class name (plus the separators) is 10 characters long plus 2 for "()".
        final StringBuilder builder = new StringBuilder(args.length * 8 + 2 );
        builder.append('(');
        for( int i=0; i<args.length; i++ )
        {
            if( i > 0 ) builder.append(", ");
            builder.append(args[i].getClass().getName());
        }

        builder.append(')');

        return builder.toString();
    }
} 
