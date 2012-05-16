/*******************************************************************************
 * Copyright (c) 2005, 2010 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration;

import java.util.List;

import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.util.SourcePosition;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class AnnotationValueImpl implements EclipseMirrorObject, AnnotationValue
{   
	/**
	 * Either the annotation that directly contains this annotation value
	 * or an annotation method, which indicates that this is its default value.
	 */
	private EclipseMirrorObject _parent;
	private final BaseProcessorEnv _env;
	/** the annotation value */
	private final Object _value;
	/** 
	 *  The name of the element if this is a value from an annotation member value.
	 *  <code>null</code> otherwise 
	 */
	private final String _name;
	/**
	 * If this is part of an array, then the index into the array.
	 * <code>-1</code> when this doesn't apply
	 */
	private final int _index;

	/**
	 * 
	 * @param value the default value of an annotation element declaration
	 * @param element the annotation element declaration.
	 * @param index zero-based index into the array if the this value is an array element.
	 *        <code>-1</code> otherwise.
	 * @param env
	 */
    public AnnotationValueImpl( final Object value,
								final int index,
								final AnnotationElementDeclarationImpl element,								
								final BaseProcessorEnv env)
    {
	
        _value = value;
        _env = env;
		_parent = element;     
		_name = null;
		_index = index;
        assert _env != null : "missing environment"; //$NON-NLS-1$
		assert _parent != null : "missing element"; //$NON-NLS-1$
    }
	
	/**
	 * 
	 * @param value the annotation value
	 * @param name the name of the element member
	 * @param index zero-based index into the array if the this value is an array element.
	 *        <code>-1</code> otherwise.
	 * @param annotation the annotation containing this value
	 * @param env
	 */
	public AnnotationValueImpl( final Object value,
								final String name,
								final int index,
								final AnnotationMirrorImpl annotation,
								final BaseProcessorEnv env)
	{
		assert value != null : "value is null"; //$NON-NLS-1$
		_value = value;
        _env = env;		
		_parent = annotation;        
		_name = name;
		_index = index;
        assert _env != null : "missing environment"; //$NON-NLS-1$
		assert _parent != null : "missing element"; //$NON-NLS-1$
	}
	
	@SuppressWarnings("rawtypes") // DOM AST API returns raw collections
    public SourcePosition getPosition()
    {		
		final MirrorKind kind = _parent.kind();
		ASTNode astNode = null;
		switch(kind)
		{
		case ANNOTATION_MIRROR:
			final AnnotationMirrorImpl anno = (AnnotationMirrorImpl)_parent;
			astNode = anno.getASTNodeForElement(_name);
			break;
		case ANNOTATION_ELEMENT:
			final AnnotationElementDeclarationImpl element = (AnnotationElementDeclarationImpl)_parent;
			astNode = element.getAstNodeForDefault();
			break;
		default:
			throw new IllegalStateException(); // should never reach this point.
		}
		// did not come from source.
		if( astNode == null )
			return null;
		if( _index >= 0 && astNode.getNodeType() == ASTNode.ARRAY_INITIALIZER ){
			final ArrayInitializer arrayInit = (ArrayInitializer)astNode;
			final List exprs = arrayInit.expressions();
			if (exprs != null && _index < exprs.size() )
				astNode = (ASTNode)exprs.get(_index);			
		}		
		if( astNode == null ) return null;
		
        final CompilationUnit unit = getCompilationUnit();
		if( unit == null ) return null;		
		final int offset = astNode.getStartPosition();		
        return new SourcePositionImpl(astNode.getStartPosition(),
									  astNode.getLength(),
                                      unit.getLineNumber(offset),
                                      unit.getColumnNumber(offset),
                                      this);
    }
	
	CompilationUnit getCompilationUnit()
	{	
		final MirrorKind kind = _parent.kind();
		switch(kind)
		{
		case ANNOTATION_MIRROR:
			return ((AnnotationMirrorImpl)_parent).getCompilationUnit();
		case ANNOTATION_ELEMENT:
			if( ((EclipseDeclarationImpl)_parent).isBindingBased() )
				return ((AnnotationElementDeclarationImpl)_parent).getCompilationUnit();
			else
				return ((ASTBasedAnnotationElementDeclarationImpl)_parent).getCompilationUnit();
		default:
			throw new IllegalStateException(); // should never reach this point.
		}
	}
	
	public boolean isFromSource()
	{
		final MirrorKind kind = _parent.kind();
		switch(kind)
		{
		case ANNOTATION_MIRROR:
			return ((AnnotationMirrorImpl)_parent).isFromSource();
		case ANNOTATION_ELEMENT:
			if( ((EclipseDeclarationImpl)_parent).isBindingBased() )
				return ((AnnotationElementDeclarationImpl)_parent).isFromSource();
			else
				return ((ASTBasedAnnotationElementDeclarationImpl)_parent).isFromSource();
		default:
			throw new IllegalStateException(); // should never reach this point.
		}
	}
	
	public IFile getResource()
	{
		final MirrorKind kind = _parent.kind();
		switch(kind)
		{
		case ANNOTATION_MIRROR:
			return ((AnnotationMirrorImpl)_parent).getResource();
		case ANNOTATION_ELEMENT:
			if( ((EclipseDeclarationImpl)_parent).isBindingBased() )
				return ((AnnotationElementDeclarationImpl)_parent).getResource();
			else
				return ((ASTBasedAnnotationElementDeclarationImpl)_parent).getResource();
		default:
			throw new IllegalStateException(); // should never reach this point.
		}
	}

    public Object getValue(){ return _value; }

    public MirrorKind kind(){ return MirrorKind.ANNOTATION_VALUE; }
	
	public BaseProcessorEnv getEnvironment(){
		return _env;
	}
	
	public String toString(){ return _value == null ? "" : _value.toString();  } //$NON-NLS-1$
}
