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

import com.sun.mirror.util.SourcePosition;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;

public class AnnotationValueImpl implements IEclipseAnnotationValue
{
    /** The expression that represents the value*/
    private final Expression _expr;   
	/** the declaration that is annotated with the annotation that contains this value. */
	private final DeclarationImpl _annotated;
	private final ProcessorEnvImpl _env;
	/** The value of the expression */
	private final Object _value;

    public AnnotationValueImpl( final Expression expr, 
								final DeclarationImpl annotated,
								final ProcessorEnvImpl env)
    {
	
        _expr = expr;
        _env = env;
		_annotated = annotated;
        assert _expr != null : "expr == null";
        assert _env != null : "missing environment";
		assert _annotated != null : "missing declaration";  
        _value = Factory.createAnnotationValueObject(expr, annotated, env);      
    }
	
    public SourcePosition getPosition()
    {		
        final CompilationUnit unit = _annotated.getCompilationUnit();
        return new SourcePositionImpl(_expr.getStartPosition(),
									  _expr.getLength(),
                                      unit.lineNumber(_expr.getStartPosition()),
                                      _annotated); 
    }

    public Object getValue(){ return _value; }

    public MirrorKind kind(){ return MirrorKind.ANNOTATION_VALUE; }
	
	public ProcessorEnvImpl getEnvironment(){
		return _env;
	}
}
