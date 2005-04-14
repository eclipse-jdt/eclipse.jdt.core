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

package org.eclipse.jdt.apt.core.internal.type; 

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.util.TypeVisitor;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class PrimitiveTypeImpl implements PrimitiveType, EclipseMirrorImpl
{
    static final AST STATIC_AST = AST.newAST(AST.JLS3);
    public static final PrimitiveType PRIMITIVE_BOOLEAN;
    public static final PrimitiveType PRIMITIVE_BYTE;
    public static final PrimitiveType PRIMITIVE_CHAR;
    public static final PrimitiveType PRIMITIVE_DOUBLE;
    public static final PrimitiveType PRIMITIVE_FLOAT;
    public static final PrimitiveType PRIMITIVE_INT;
    public static final PrimitiveType PRIMITIVE_LONG;
    public static final PrimitiveType PRIMITIVE_SHORT;
    
    static{
         final org.eclipse.jdt.core.dom.PrimitiveType astBoolean =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.BOOLEAN);
         PRIMITIVE_BOOLEAN = new PrimitiveTypeImpl(astBoolean.resolveBinding());

         final org.eclipse.jdt.core.dom.PrimitiveType astByte =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.BYTE);
         PRIMITIVE_BYTE = new PrimitiveTypeImpl(astByte.resolveBinding());
         
         final org.eclipse.jdt.core.dom.PrimitiveType astChar =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.CHAR);
         PRIMITIVE_CHAR = new PrimitiveTypeImpl(astChar.resolveBinding());

         final org.eclipse.jdt.core.dom.PrimitiveType astDouble =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.DOUBLE);
         PRIMITIVE_DOUBLE = new PrimitiveTypeImpl(astDouble.resolveBinding());

         final org.eclipse.jdt.core.dom.PrimitiveType astFloat =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.FLOAT);
         PRIMITIVE_FLOAT = new PrimitiveTypeImpl(astFloat.resolveBinding());

         final org.eclipse.jdt.core.dom.PrimitiveType astInt =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.INT);
         PRIMITIVE_INT = new PrimitiveTypeImpl(astInt.resolveBinding());

         final org.eclipse.jdt.core.dom.PrimitiveType astLong =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.LONG);
         PRIMITIVE_LONG = new PrimitiveTypeImpl(astLong.resolveBinding());

         final org.eclipse.jdt.core.dom.PrimitiveType astShort =
            STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.SHORT);
         PRIMITIVE_SHORT = new PrimitiveTypeImpl(astShort.resolveBinding());
    };
    
    private final ITypeBinding _binding;    
    
    private PrimitiveTypeImpl(ITypeBinding binding)
    {
        _binding = binding;        
    }
    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
        visitor.visitPrimitiveType(this);
    }

    public PrimitiveType.Kind getKind()
    {
		final String name = getTypeBinding().getName();
		if( "int".equals(name) )
			return PrimitiveType.Kind.INT; 
		else if( "byte".equals(name) )
			return PrimitiveType.Kind.BYTE;
		else if( "short".equals(name) )
			return PrimitiveType.Kind.SHORT;
		else if( "char".equals(name) )
			return PrimitiveType.Kind.CHAR;
		else if( "long".equals(name) )
			return PrimitiveType.Kind.LONG;
		else if( "float".equals(name) )
			return PrimitiveType.Kind.FLOAT;
		else if( "double".equals(name) )
			return PrimitiveType.Kind.DOUBLE;
		else if( "boolean".equals(name))
			return PrimitiveType.Kind.BOOLEAN;
		else
			throw new IllegalStateException("unrecognized primitive type " + _binding);
    }
    
    public String toString(){ return _binding.getName(); }

    public ITypeBinding getTypeBinding(){ return _binding; }

    public MirrorKind kind(){ return MirrorKind.TYPE_PRIMITIVE; }
	
	public ProcessorEnvImpl getEnvironment(){ return null; }
}
