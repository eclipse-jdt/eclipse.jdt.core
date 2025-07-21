/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
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
public abstract class ErrorType implements DeclaredType, EclipseMirrorType
{
    final String _name;

    ErrorType(final String name){
        _name = name;
    }

    @Override
	public Collection<TypeMirror> getActualTypeArguments(){ return Collections.emptyList(); }

    @Override
	public DeclaredType getContainingType(){ return null; }

    @Override
	public String toString(){ return _name; }

    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
    }

    @Override
	public Collection<InterfaceType> getSuperinterfaces(){ return Collections.emptyList(); }

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_ERROR; }

	@Override
	public BaseProcessorEnv getEnvironment(){ return null; }

    public static final class ErrorClass extends ErrorType implements ClassType
    {
        public ErrorClass(final String name){ super(name); }

        @Override
		public void accept(TypeVisitor visitor)
        {
            visitor.visitClassType(this);
        }

        @Override
		public ClassType getSuperclass()
        {
            return null;
        }

        @Override
		public ClassDeclaration getDeclaration(){ return null; }

    }

    public static class ErrorInterface extends ErrorType implements InterfaceType
    {
        public ErrorInterface(final String name){ super(name); }

        @Override
		public void accept(TypeVisitor visitor)
        {
            visitor.visitInterfaceType(this);
        }

        @Override
		public InterfaceDeclaration getDeclaration(){ return null; }
    }

    public static final class ErrorAnnotation extends ErrorInterface implements AnnotationType
    {
        public ErrorAnnotation(final String name){ super(name); }

        @Override
		public void accept(TypeVisitor visitor)
        {
            visitor.visitAnnotationType(this);
        }

        @Override
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

    	@Override
		public void accept(TypeVisitor visitor)
        {
            visitor.visitArrayType(this);
        }

    	@Override
		public TypeDeclaration getDeclaration() { return null; }

    	@Override
		public TypeMirror getComponentType() {
    		return new ErrorClass(_name);
    	}

    	@Override
		public String toString()
    	{
    		final StringBuilder buffer = new StringBuilder();
    		buffer.append(_name);
    		for( int i=0; i<_dimension; i++ )
    			buffer.append("[]"); //$NON-NLS-1$
    		return buffer.toString();
    	}
    }

	@Override
	public ITypeBinding getTypeBinding() {
		return null;
	}

	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return false;
	}

	@Override
	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return false;
	}
}
