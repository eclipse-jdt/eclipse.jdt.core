/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
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
 *    het@google.com - Bug 441790
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.util.SourcePosition;

/**
 * Annotation instance from source.
 */
public class AnnotationMirrorImpl implements AnnotationMirror, EclipseMirrorObject
{
    /**The ast node that correspond to the annotation.*/
    private final IAnnotationBinding _domAnnotation;
    private final BaseProcessorEnv _env;
    /** the declaration that is annotated by this annotation or the annotation element declaration
     *  if this is (part of) a default value*/
    private final EclipseDeclarationImpl _annotated;

    public AnnotationMirrorImpl(IAnnotationBinding annotationAstNode, EclipseDeclarationImpl decl, BaseProcessorEnv env)
    {
		_domAnnotation = annotationAstNode;
        _env = env;
        _annotated = decl;
        assert _domAnnotation != null : "annotation node missing."; //$NON-NLS-1$
        assert _annotated   != null : "missing the declaration that is annotated with this annotation."; //$NON-NLS-1$
    }

    @Override
	public AnnotationType getAnnotationType()
    {
        final ITypeBinding binding = _domAnnotation.getAnnotationType();
        if( binding == null || binding.isRecovered() ){
        	final ASTNode node = _annotated.getCompilationUnit().findDeclaringNode(_domAnnotation);
        	String name = ""; //$NON-NLS-1$
        	if( node != null && node instanceof Annotation ){
        		final Name typeNameNode = ((Annotation)node).getTypeName();
        		if( typeNameNode != null )
        			name = typeNameNode.toString();
        	}
        	return Factory.createErrorAnnotationType(name);
        }
        else
        	return (AnnotationType)Factory.createReferenceType(binding, _env);
    }

    @Override
	public Map<AnnotationTypeElementDeclaration, AnnotationValue> getElementValues()
    {
		final IMemberValuePairBinding[] pairs = _domAnnotation.getDeclaredMemberValuePairs();
		if (pairs.length == 0) {
			return Collections.emptyMap();
		}

		final Map<AnnotationTypeElementDeclaration, AnnotationValue> result =
			new LinkedHashMap<>(pairs.length * 4 / 3 + 1 );
		for( IMemberValuePairBinding pair : pairs ){
			 final String name = pair.getName();
             if( name == null ) continue;
             IMethodBinding elementMethod = pair.getMethodBinding();
             if( elementMethod != null ){
                 final EclipseDeclarationImpl mirrorDecl = Factory.createDeclaration(elementMethod, _env);
                 if( mirrorDecl != null && mirrorDecl.kind() == EclipseMirrorObject.MirrorKind.ANNOTATION_ELEMENT  )
                 {
                	 final AnnotationTypeElementDeclaration elementDecl =
                		 (AnnotationTypeElementDeclaration)mirrorDecl;
                	 final AnnotationValue annoValue =
    					 Factory.createAnnotationMemberValue(pair.getValue(), name, this, _env, elementDecl.getReturnType());
                	 if( annoValue != null )
                		 result.put( elementDecl, annoValue);
                 }
             }
		}
        return result;
    }

    @Override
	public SourcePosition getPosition()
    {
		if( isFromSource() ){
			final CompilationUnit unit = _annotated.getCompilationUnit();
			final org.eclipse.jdt.core.dom.Annotation annotation = getAstNode();
			if( annotation == null ) return null;
			org.eclipse.jdt.core.dom.ASTNode astNode = annotation.getTypeName();
			if( astNode == null )
				astNode = annotation;

			final int offset = astNode.getStartPosition();
			return new SourcePositionImpl(astNode.getStartPosition(),
										  astNode.getLength(),
						                  unit.getLineNumber(offset),
						                  unit.getColumnNumber(offset),
						                  _annotated);
		}
		return null;
    }

    @Override
	public String toString()
    {
    	AnnotationTypeDeclaration decl = getAnnotationType().getDeclaration();
    	StringBuilder sb = new StringBuilder();
    	sb.append('@');
    	sb.append(decl.getQualifiedName());
    	Map<AnnotationTypeElementDeclaration, AnnotationValue> values = getElementValues();
		if (!values.isEmpty()) {
			sb.append('(');
			boolean first = true;
			for (Entry<AnnotationTypeElementDeclaration, AnnotationValue> e : values.entrySet()) {
				if (!first) {
					sb.append(", "); //$NON-NLS-1$
				}
				first = false;
				sb.append(e.getKey().getSimpleName());
				sb.append(" = "); //$NON-NLS-1$
				sb.append(e.getValue().toString());
			}
			sb.append(')');
		}
		return sb.toString();
    }

	/**
     * @return the type(s) of the member value named <code>membername</code>.
     * If the value is a class literal, then return the type binding corresponding to the type requested.
     * Otherwise, return the type of the expression.
     * If the value is an array initialization, then the type of each of the initialization expresion will
     * be returned. Return null if no match is found.
     */
    public ITypeBinding[] getMemberValueTypeBinding(String membername)
    {
        if( membername == null ) return null;
		final IMemberValuePairBinding[] declaredPairs = _domAnnotation.getDeclaredMemberValuePairs();
		for( IMemberValuePairBinding pair : declaredPairs ){
			if( membername.equals(pair.getName()) ){
				final Object value = pair.getValue();
				return getValueTypeBinding(value, pair.getMethodBinding().getReturnType());
			}
		}

        // didn't find it in the ast, check the default values.
        final IMethodBinding binding = getMethodBinding(membername);
		if(binding == null ) return null;
		final Object defaultValue = binding.getDefaultValue();
		if( defaultValue != null )
			return getValueTypeBinding(defaultValue, binding.getReturnType() );
		else
			return null;
    }

	private ITypeBinding[] getValueTypeBinding(Object value, final ITypeBinding resolvedType)
	{
		if( value == null ) return null;
		if( resolvedType.isPrimitive() ||  resolvedType.isAnnotation() || value instanceof String )
			return new ITypeBinding[]{ resolvedType };
		else if( resolvedType.isArray() ){
			final Object[] elements = (Object[])value;
			final ITypeBinding[] result = new ITypeBinding[elements.length];
			final ITypeBinding leafType = resolvedType.getElementType();
			for(int i=0, len = elements.length; i<len; i++ ){
				final ITypeBinding[] t = getValueTypeBinding(elements[i], leafType);
				result[i] = t == null ? null : t[0];
			}
			return result;
		}
		else if( value instanceof IVariableBinding )
			return new ITypeBinding[]{ ( (IVariableBinding)value ).getDeclaringClass() };
		else if( value instanceof ITypeBinding )
			return new ITypeBinding[]{ (ITypeBinding)value };
		else
			throw new IllegalStateException("value = " + value + " resolvedType = " + resolvedType ); //$NON-NLS-1$ //$NON-NLS-2$

	}

    /**
     * @param memberName the name of the member
     * @return the value of the given member
     */
    public Object getValue(final String memberName)
    {
		if( memberName == null ) return null;
		final IMemberValuePairBinding[] declaredPairs = _domAnnotation.getDeclaredMemberValuePairs();
		for( IMemberValuePairBinding pair : declaredPairs ){
			if( memberName.equals(pair.getName()) ){
				return pair.getValue();
			}
		}

        // didn't find it in the ast, check the default values.
        final IMethodBinding binding = getMethodBinding(memberName);
		if(binding == null ) return null;
		return binding.getDefaultValue();
    }

    /**
     * @return the method binding that matches the given name from the annotation type
     *         referenced by this annotation.
     */
    public IMethodBinding getMethodBinding(final String memberName)
    {
        if( memberName == null ) return null;
        final ITypeBinding typeBinding = _domAnnotation.getAnnotationType();
		if( typeBinding == null ) return null;
        final IMethodBinding[] methods  = typeBinding.getDeclaredMethods();
        for( IMethodBinding method : methods ){
            if( memberName.equals(method.getName()) )
                return method;
        }
        return null;
    }

    public IAnnotationBinding getResolvedAnnotaion(){return _domAnnotation; }



    @Override
	public MirrorKind kind(){ return MirrorKind.ANNOTATION_MIRROR; }

    boolean isFromSource()
	{
		return _annotated.isFromSource();
	}

    org.eclipse.jdt.core.dom.Annotation getAstNode()
	{
		if( isFromSource() ){
			final CompilationUnit unit = _annotated.getCompilationUnit();
			final ASTNode node = unit.findDeclaringNode(_domAnnotation);
			if( node instanceof org.eclipse.jdt.core.dom.Annotation )
				return (org.eclipse.jdt.core.dom.Annotation)node;
		}
		return null;
    }

	ASTNode getASTNodeForElement(String name)
	{
		if( name == null ) return null;
		final org.eclipse.jdt.core.dom.Annotation anno = getAstNode();
		if( anno != null ){
			if( anno.isSingleMemberAnnotation() ){
				if( "value".equals(name) ) //$NON-NLS-1$
					return ((SingleMemberAnnotation)anno).getValue();
			}
			else if( anno.isNormalAnnotation() ){
				final List<MemberValuePair> pairs = ((NormalAnnotation)anno).values();
				for( MemberValuePair pair : pairs )
				{
					final String pairName = pair.getName() == null ? null : pair.getName().toString();
					if( name.equals(pairName) )
						return pair.getValue();
				}
			}
		}
		// marker annotation or no match.
		return null;
	}

    CompilationUnit getCompilationUnit() { return _annotated.getCompilationUnit(); }

	@Override
	public BaseProcessorEnv getEnvironment(){ return _env; }

	public IFile getResource()
	{ 	return _annotated.getResource(); }

	public EclipseDeclarationImpl getAnnotatedDeclaration(){ return _annotated; }

    @Override
	public boolean equals(Object obj){
        if( obj instanceof AnnotationMirrorImpl ){
            return ((AnnotationMirrorImpl)obj)._domAnnotation == _domAnnotation;
        }
        return false;
    }

    @Override
	public int hashCode(){
        return _domAnnotation.hashCode();
    }
}
