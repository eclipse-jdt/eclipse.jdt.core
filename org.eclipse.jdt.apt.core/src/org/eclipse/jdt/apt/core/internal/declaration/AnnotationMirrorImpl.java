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
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.util.SourcePosition;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * Annotation instance from source.
 */
public class AnnotationMirrorImpl implements AnnotationMirror, EclipseMirrorImpl
{
    /**The ast node that correspond to the annotation.*/
    private final Annotation _annoAstNode;
    private final ProcessorEnvImpl _env;
    /** the declaration that is annotated with this annotation. */
    private final DeclarationImpl _annotated;
    
    public AnnotationMirrorImpl(Annotation annotationAstNode, DeclarationImpl decl, ProcessorEnvImpl env)
    {
        _annoAstNode = annotationAstNode;
        _env = env;
        _annotated = decl;
        assert _annoAstNode != null : "annotation ast node missing.";
        assert _annotated   != null : "missing the declaration that is annotated with this annotation.";
    }

    public AnnotationType getAnnotationType()
    {
        final ITypeBinding binding = _annoAstNode.resolveTypeBinding();
        if( binding.isAnnotation() )
            return (AnnotationType)Factory.createReferenceType(binding, _env);
        else
            return Factory.createErrorAnnotationType(binding);
    }

    public Map<AnnotationTypeElementDeclaration, AnnotationValue> getElementValues()
    {
        if( _annoAstNode.isSingleMemberAnnotation() ) {
            final Expression value = ((SingleMemberAnnotation)_annoAstNode).getValue();
            final ITypeBinding typeBinding = _annoAstNode.resolveTypeBinding();
            if( typeBinding.isAnnotation() ){
                final IMethodBinding[] methods  = typeBinding.getDeclaredMethods();
                // There should only be one since this is a single member annotation.
                // Ignore it otherwise.
                if( methods != null && methods.length != 1 ){
                    IMethodBinding elementMethod = methods[0];
                    final DeclarationImpl mirrorDecl = Factory.createDeclaration(elementMethod, _env);
                    final AnnotationValue annoValue = Factory.createAnnotationValue(value, _annotated, _env);
                    if( mirrorDecl.kind() == EclipseMirrorImpl.MirrorKind.ANNOTATION_ELEMENT  &&
                        annoValue != null )
                        return Collections.singletonMap( (AnnotationTypeElementDeclaration)mirrorDecl, annoValue);
                }
            }
        }
        else if( _annoAstNode.isNormalAnnotation() ){
            final NormalAnnotation normalAnnotation = (NormalAnnotation)_annoAstNode;
            final List<MemberValuePair> pairs = normalAnnotation.values();
            final ITypeBinding typeBinding = _annoAstNode.resolveTypeBinding();
            if( typeBinding.isAnnotation() ){
                final IMethodBinding[] methodBindings = typeBinding.getDeclaredMethods();
                if( methodBindings != null && methodBindings.length > 0){
                    // Annotation value has to appear in source order.
                    final Map<AnnotationTypeElementDeclaration, AnnotationValue> result =
                        new LinkedHashMap<AnnotationTypeElementDeclaration, AnnotationValue>(methodBindings.length * 4 / 3 + 1 );
                    // locate all the member value pair.
                    for( MemberValuePair pair : pairs ){
                        final SimpleName simpleName = pair.getName();
                        if( simpleName == null ) continue;
                        final String name = simpleName.toString();
                        IMethodBinding elementMethod = null;
                        // look for the corresponding method binding.
                        for( IMethodBinding m : methodBindings ) {
                            if( name.equals( m.getName() ) ){
                                elementMethod = m;
                                break;
                            }
                        }
                        if( elementMethod != null ){
                            final DeclarationImpl mirrorDecl = Factory.createDeclaration(elementMethod, _env);
                            final AnnotationValue annoValue = Factory.createAnnotationValue(pair.getValue(), _annotated, _env);
                            if( mirrorDecl.kind() == EclipseMirrorImpl.MirrorKind.ANNOTATION_ELEMENT  &&
                                annoValue != null )
                                result.put( (AnnotationTypeElementDeclaration)mirrorDecl, annoValue);
                        }
                    }
                    return result;
                }
            }
        }
        // marker annotation or if anything goes wrond above.
        return Collections.emptyMap();
    }

    public SourcePosition getPosition()
    {
        final CompilationUnit unit = _annotated.getCompilationUnit();
        return new SourcePositionImpl(_annoAstNode.getStartPosition(),
                                      _annoAstNode.getLength(),
                                      unit.lineNumber(_annoAstNode.getStartPosition()),
                                      _annotated);
    }

    public String toString()
    {
        return _annoAstNode.resolveTypeBinding().getName();
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

        if( _annoAstNode.isMarkerAnnotation() ) return null;
        else if( _annoAstNode.isSingleMemberAnnotation() ) {
            final Expression value = ((SingleMemberAnnotation)_annoAstNode).getValue();
            if(value == null) return null;
            final ITypeBinding typeBinding = _annoAstNode.resolveTypeBinding();
            if( typeBinding.isAnnotation() ){
                final IMethodBinding[] methods  = typeBinding.getDeclaredMethods();
                // There should only be one since this is a single member annotation.
                if( methods != null && methods.length != 1 ){
                    IMethodBinding elementMethod = methods[0];
                    // make sure the name matches.
                    if( elementMethod.getName().equals(membername) )
                        return getExpressionTypeBindings(value);
                }
            }
        }
        else if( _annoAstNode.isNormalAnnotation() ){
            final NormalAnnotation normalAnnotation = (NormalAnnotation)_annoAstNode;
            final List<MemberValuePair> pairs = normalAnnotation.values();
            {
               for( MemberValuePair pair : pairs ){
                    final SimpleName simpleName = pair.getName();
                    if( simpleName == null ) continue;
                    final String name = simpleName.toString();
                    if( simpleName.toString().equals(membername) )
                        return getExpressionTypeBindings(pair.getValue());
                }
            }
        }
        // didn't find it in the ast, check the default values.
        final IMethodBinding binding = getMethodBinding(membername);
        final ITypeBinding declaringClass = binding.getDeclaringClass();
        if(binding == null || declaringClass == null ) return null;

        if( declaringClass.isFromSource() ){
            final AnnotationTypeMemberDeclaration methodDecl =
                (AnnotationTypeMemberDeclaration)_env.getASTNodeForBinding(binding);
            if( methodDecl != null )
                return getExpressionTypeBindings(methodDecl.getDefault());
            else
                return null;
        }
        else{
            // todo: (theodora) handle reading default value from binary.
            return null;
        }
    }

    private ITypeBinding[] getExpressionTypeBindings(Expression expr)
    {
        if(expr == null) return null;
        switch(expr.getNodeType())
        {
        case ASTNode.ARRAY_INITIALIZER:
            final ArrayInitializer arrayInit = (ArrayInitializer)expr;
            final List<Expression> exprs = arrayInit.expressions();
            if( exprs == null || exprs.size() == 0 )
                return new ITypeBinding[0];
            final ITypeBinding[] bindings = new ITypeBinding[exprs.size()];
            for( int i=0, size = exprs.size(); i<size; i++ ){
                final Expression initExpr = exprs.get(i);
                bindings[i] = getExpressionTypeBinding(initExpr);
            }
            return bindings;
        default:
            return new ITypeBinding[]{ getExpressionTypeBinding(expr) };
        }
    }

    private ITypeBinding getExpressionTypeBinding(Expression expr)
    {
        if( expr.getNodeType() == ASTNode.TYPE_LITERAL )
            return  ((TypeLiteral)expr).getType().resolveBinding();
        else
            return expr.resolveTypeBinding();
    }

    /**
     * @param memberName the name of the member
     * @return the value of the given member
     */
    private Expression getValue(final String memberName)
    {
        if( _annoAstNode.isMarkerAnnotation() ) return null;
        else if( _annoAstNode.isSingleMemberAnnotation() ) {
            final Expression value = ((SingleMemberAnnotation)_annoAstNode).getValue();
            final ITypeBinding typeBinding = _annoAstNode.resolveTypeBinding();
            if( typeBinding.isAnnotation() ){
                final IMethodBinding[] methods  = typeBinding.getDeclaredMethods();				
                // There should only be one since this is a single member annotation.
                if( methods != null && methods.length != 1 ){
                    IMethodBinding elementMethod = methods[0];
                    // make sure the name matches.
                    if( elementMethod.getName().equals(memberName) )
                        return value;
                }
            }
        }
        else if( _annoAstNode.isNormalAnnotation() ){
            final NormalAnnotation normalAnnotation = (NormalAnnotation)_annoAstNode;
            final List<MemberValuePair> pairs = normalAnnotation.values();
            {
               for( MemberValuePair pair : pairs ){
                    final SimpleName simpleName = pair.getName();
                    if( simpleName == null ) continue;
                    final String name = simpleName.toString();
                    if( simpleName.toString().equals(memberName) )
                        return pair.getValue();
                }
            }
        }
        // didn't find it in the ast, check the default values.
        final IMethodBinding binding = getMethodBinding(memberName);
        final ITypeBinding declaringClass = binding.getDeclaringClass();
        if(binding == null || declaringClass == null ) return null;

        if( declaringClass.isFromSource() ){
            final AnnotationTypeMemberDeclaration methodDecl =
                (AnnotationTypeMemberDeclaration)_env.getASTNodeForBinding(binding);
            if( methodDecl != null )
                return methodDecl.getDefault();
            else
                return null;
        }
        else{
            // todo: (theodora) handle reading default value from binary.
            return null;
        }
    }

    /**
     * @return the method binding that matches the given name from the annotation type
     *         referenced by this annotation.
     */
    public IMethodBinding getMethodBinding(final String memberName)
    {
        if( memberName == null ) return null;
        final ITypeBinding typeBinding = _annoAstNode.resolveTypeBinding();
        final IMethodBinding[] methods  = typeBinding.getDeclaredMethods();
        for( IMethodBinding method : methods ){
            if( memberName.equals(method.getName()) )
                return method;
        }
        return null;
    }

    public Object getReflectionValue(String memberName, Method method)
        throws Throwable
    {
        if(memberName == null || memberName.length() == 0 ) return null;
        final Class targetType = method.getReturnType();
        final Expression value = getValue(memberName);

        return getReflectionValue(value, targetType);
    }

    private Object getReflectionValue(final Expression expr, final Class targetType)
        throws Throwable
    {
        if( expr == null ) return null;
		final Object constantValue = expr.resolveConstantExpressionValue();
		if( constantValue != null ) return constantValue;
        switch(expr.getNodeType())
        {
        case ASTNode.SIMPLE_NAME:
        case ASTNode.QUALIFIED_NAME:
            final Name name = (Name)expr;
            final IBinding nameBinding = name.resolveBinding();
            if( nameBinding.getKind() == IBinding.VARIABLE ) {
                final IVariableBinding varBinding = (IVariableBinding)nameBinding;
                final ITypeBinding declaringClass = varBinding.getDeclaringClass();
                if( declaringClass != null ){
                    final String className = new String( declaringClass.getBinaryName() );
                    final Class clazz = expr.getClass().getClassLoader().loadClass( className );
                    final Field returnedField = clazz.getField( varBinding.getName() );
                    if( returnedField.getType() != targetType )
                        throw new ClassCastException( targetType.getName() );
                    return returnedField.get(null);
                }
            }
            break;
        case ASTNode.ARRAY_INITIALIZER:
            assert targetType.isArray();
            final Class componentType = targetType.getComponentType();
            final char componentTypeName = componentType.getName().charAt(0);
            final ArrayInitializer arrayInit = (ArrayInitializer)expr;
            final List<Expression> exprs = arrayInit.expressions();
            final int length = exprs == null ? 0 : exprs.size();
            final Object array = Array.newInstance(componentType, length);
            if( length == 0) return array;

            for( int i=0; i<length; i++ ){
                final Expression element = exprs.get(i);
                final Object returnObj = getReflectionValue( element, componentType );
                // fill in the array.
                // If it is an array of some primitive type, we will need to unwrap it.
                if( componentType.isPrimitive() ){
                    if( componentType == boolean.class ){
                        final Boolean bool = (Boolean)returnObj;
                        Array.setBoolean( array, i, bool.booleanValue());
                    }
                    else if( componentType == byte.class ){
                        final Byte b = (Byte)returnObj;
                        Array.setByte( array, i, b.byteValue() );
                    }
                    else if( componentType == char.class ){
                        final Character c = (Character)returnObj;
                        Array.setChar( array, i, c.charValue() );
                    }
                    else if( componentType == double.class ){
                        final Double d = (Double)returnObj;
                        Array.setDouble( array, i, d.doubleValue() );
                    }
                    else if( componentType == float.class ){
                        final Float f = (Float)returnObj;
                        Array.setFloat( array, i, f.floatValue() );
                    }
                    else if( componentType == int.class ){
                        final Integer integer = (Integer)returnObj;
                        Array.setInt( array, i, integer.intValue() );
                    }
                    else if( componentType == long.class ){
                        final Long l = (Long)returnObj;
                        Array.setLong( array, i, l.longValue() );
                    }
                    else if( componentType == short.class ){
                        final Short s = (Short)returnObj;
                        Array.setShort( array, i, s.shortValue() );
                    }
                    else {
                        throw new IllegalStateException("unrecognized primitive type: "  + componentType );
                    }
                }
                else{
                    Array.set( array, i, returnObj );
                }
            }
            return array;
        case ASTNode.NORMAL_ANNOTATION:
        case ASTNode.MARKER_ANNOTATION:
        case ASTNode.SINGLE_MEMBER_ANNOTATION:
            return Factory.createAnnotationMirror((Annotation)expr, _annotated, _env);        
        case ASTNode.TYPE_LITERAL:
            throw new IllegalStateException("illegal expression " + expr);     
        }

        return null;
    }

    public MirrorKind kind(){ return MirrorKind.ANNOTATION_MIRROR; }

    boolean isFromSource(){ return true; }

    Annotation getAstNode(){ return _annoAstNode; }

    CompilationUnit getCompilationUnit() { return _annotated.getCompilationUnit(); }

    public IResource getResource(){
        return _annotated.getResource();
    }
	
	public ProcessorEnvImpl getEnvironment(){ return _env; }

    public boolean equals(Object obj){
        if( obj instanceof AnnotationMirrorImpl ){
            return ((AnnotationMirrorImpl)obj)._annoAstNode == _annoAstNode;
        }
        return false;
    }

    public int hashCode(){
        return _annoAstNode.hashCode();
    }
}
