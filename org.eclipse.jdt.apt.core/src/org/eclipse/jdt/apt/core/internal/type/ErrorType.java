/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.type;

import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.TypeVisitor;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorType;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * This is the error type marker
 */
public abstract class ErrorType implements DeclaredType, ReferenceType, EclipseMirrorType
{
    final String _name;

    ErrorType(final String name){
        _name = name;
    }

    public Collection<TypeMirror> getActualTypeArguments(){ return Collections.emptyList(); }

    public DeclaredType getContainingType(){ return null; }

    public String toString(){ return _name; }

    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
    }

    public Collection<InterfaceType> getSuperinterfaces(){ return Collections.emptyList(); }

    public MirrorKind kind(){ return MirrorKind.TYPE_ERROR; }
	
	public BaseProcessorEnv getEnvironment(){ return null; }

    public static final class ErrorClass extends ErrorType implements ClassType
    {
        public ErrorClass(final String name){ super(name); }

        public void accept(TypeVisitor visitor)
        {
            visitor.visitClassType(this);
        }

        public ClassType getSuperclass()
        {
            return null;
        }

        public ClassDeclaration getDeclaration(){ return null; }		
		
    }

    public static class ErrorInterface extends ErrorType implements InterfaceType
    {
        public ErrorInterface(final String name){ super(name); }

        public void accept(TypeVisitor visitor)
        {
            visitor.visitInterfaceType(this);
        }

        public InterfaceDeclaration getDeclaration(){ return null; }
    }

    public static final class ErrorAnnotation extends ErrorInterface implements AnnotationType
    {
        public ErrorAnnotation(final String name){ super(name); }

        public void accept(TypeVisitor visitor)
        {
            visitor.visitAnnotationType(this);
        }

        public AnnotationTypeDeclaration getDeclaration(){ return null; }
    }
    
    public static final class ErrorArrayType extends ErrorType implements ArrayType 
    {
    	private final int _dimension;
    	public ErrorArrayType(final String name, final int dimension )
    	{
    		super(name);
    		_dimension = dimension;
    	}
    	
    	public void accept(TypeVisitor visitor)
        {
            visitor.visitArrayType(this);
        }
    	
    	public TypeDeclaration getDeclaration() { return null; }
    	
    	public TypeMirror getComponentType() {
    		return new ErrorClass(_name);
    	}
    	
    	public String toString()
    	{
    		final StringBuilder buffer = new StringBuilder();
    		buffer.append(_name);
    		for( int i=0; i<_dimension; i++ )
    			buffer.append("[]"); //$NON-NLS-1$
    		return buffer.toString();
    	}
    }

	public ITypeBinding getTypeBinding() {
		return null;
	}

	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return false;
	}

	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return false;
	}
}
