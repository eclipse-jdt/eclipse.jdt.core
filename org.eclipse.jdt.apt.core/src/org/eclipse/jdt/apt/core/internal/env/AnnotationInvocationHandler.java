/*******************************************************************************
 * Copyright (c) 2005, 2016 BEA Systems, Inc.
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

package org.eclipse.jdt.apt.core.internal.env;

import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.MirroredTypesException;
import com.sun.mirror.type.TypeMirror;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class AnnotationInvocationHandler implements InvocationHandler
{
	private static final String JAVA_LANG_CLASS = "java.lang.Class"; //$NON-NLS-1$
    private final AnnotationMirrorImpl _instance;
    private final Class<?> _clazz;

    public AnnotationInvocationHandler(final AnnotationMirrorImpl annotation,
    								   final Class<?> clazz)
    {
        _instance = annotation;
        _clazz = clazz;
    }

    @Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        final String methodName = method.getName();
        if( args == null || args.length == 0 )
        {
            if( methodName.equals("hashCode") ) //$NON-NLS-1$
                return Integer.valueOf( _instance.hashCode() );
            if( methodName.equals("toString") ) //$NON-NLS-1$
                return _instance.toString();
            if( methodName.equals("annotationType")) //$NON-NLS-1$
            	return _clazz;
        }
        else if( args.length == 1 && methodName.equals("equals") ) //$NON-NLS-1$
        {
            return Boolean.valueOf(_instance.equals(args[0]));
        }
        if( args != null && args.length != 0 )
            throw new NoSuchMethodException("method " + method.getName() + formatArgs(args) + " does not exists"); //$NON-NLS-1$ //$NON-NLS-2$
        final String c_methodName = method.getName();
        final IMethodBinding methodBinding = _instance.getMethodBinding(c_methodName);
        if( methodBinding == null )
            throw new NoSuchMethodException("method " + method.getName() + "() does not exists"); //$NON-NLS-1$ //$NON-NLS-2$

        final ITypeBinding retType = methodBinding.getReturnType();
        if( retType == null ) return null;

        final String qName = retType.getTypeDeclaration().getQualifiedName();
        // type of annotation member is java.lang.Class
        if( retType.isClass() && JAVA_LANG_CLASS.equals(qName) ){
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
            final String leafQName = leafType.getTypeDeclaration().getQualifiedName();
            // type of annotation member is java.lang.Class[]
            if( leafType.isClass() && JAVA_LANG_CLASS.equals(leafQName) ){
                final ITypeBinding[] classTypes = _instance.getMemberValueTypeBinding(c_methodName);
                final Collection<TypeMirror> mirrorTypes;
                if( classTypes == null || classTypes.length == 0 )
                    mirrorTypes = Collections.emptyList();
                else{
                    mirrorTypes = new ArrayList<>(classTypes.length);
                    for( ITypeBinding type : classTypes ){
                        TypeMirror mirror = Factory.createTypeMirror(type, _instance.getEnvironment() );
                        if( mirror == null )
                            mirrorTypes.add(Factory.createErrorClassType(type));
                        else
                            mirrorTypes.add(mirror);
                    }
                }

                throw new MirroredTypesException(mirrorTypes);
            }
        }
        final Object sourceValue = _instance.getValue(c_methodName);
        return getReflectionValueWithTypeConversion(sourceValue, method.getReturnType());
    }

    private Object getReflectionValueWithTypeConversion(
    		final Object domValue,
    		final Class<?> expectedType )
    {

    	final Object actualValue = _getReflectionValue(domValue, expectedType);
    	return performNecessaryTypeConversion(expectedType, actualValue);
    }

	private Object _getReflectionValue(final Object domValue, final Class<?> expectedType)
	{
		if( expectedType == null || domValue == null )
			return null;

	    if( domValue instanceof IVariableBinding )
		{
			final IVariableBinding varBinding = (IVariableBinding)domValue;
	        final ITypeBinding declaringClass = varBinding.getDeclaringClass();
	        if( declaringClass != null ){
	        	try {
	        		final Field returnedField = expectedType.getField( varBinding.getName() );
	        		return returnedField == null ? null : returnedField.get(null);
	        	}
	        	catch (NoSuchFieldException nsfe) {
	        		return null;
	        	}
	        	catch (IllegalAccessException iae) {
	        		return null;
	        	}
	        }
	        return null;
		}
	    else if (domValue instanceof Object[])
		{
			final Object[] elements = (Object[])domValue;
			if(!expectedType.isArray())
				return null; // bad user source
	        final Class<?> componentType = expectedType.getComponentType();
	        final int length = elements.length;
	        final Object array = Array.newInstance(componentType, length);

	        for( int i=0; i<length; i++ ){
	            final Object returnObj =
	            	getReflectionValueWithTypeConversion( elements[i], componentType );
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
	                    throw new IllegalStateException("unrecognized primitive type: "  + componentType ); //$NON-NLS-1$
	                }
	            }
	            else{
	                Array.set( array, i, returnObj );
	            }
	        }
	        return array;
		}
		// caller should have caught this case.
	    else if( domValue instanceof ITypeBinding )
			throw new IllegalStateException("sourceValue is a type binding."); //$NON-NLS-1$

	    else if( domValue instanceof IAnnotationBinding )
		{
	    	// We cannot convert an annotation into anything else
	    	if (!expectedType.isAnnotation()) {
	    		return null;
	    	}

			final AnnotationMirrorImpl annoMirror =
				(AnnotationMirrorImpl)Factory.createAnnotationMirror(
					(IAnnotationBinding)domValue,
					_instance.getAnnotatedDeclaration(),
					_instance.getEnvironment());
	        final AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annoMirror, expectedType);
	        return Proxy.newProxyInstance(expectedType.getClassLoader(),
	                                      new Class[]{ expectedType }, handler );
		}
	    // primitive wrapper or String.
	    else
	    	return domValue;
	}

	private Object performNecessaryTypeConversion(Class<?> expectedType, Object actualValue){
		if( actualValue == null )
			return Factory.getMatchingDummyValue(expectedType);
		else if( expectedType.isPrimitive() )
			return Factory.performNecessaryPrimitiveTypeConversion( expectedType, actualValue, true);
		else if( expectedType.isAssignableFrom(actualValue.getClass()))
			return actualValue;
		else if( expectedType.isArray() ){
			// the above assignableFrom test failed which leave up with
			// the array-ificiation problem.
			// arrays are always type corrected.
			actualValue = performNecessaryTypeConversion(expectedType.getComponentType(), actualValue);
			return arrayify(expectedType, actualValue);
		}
		// type conversion cannot be performed and expected type is not a primitive
		// Returning null so that we don't get a ClassCastException.
		else return null;
	}

	private Object arrayify(final Class<?> expectedType, Object actualValue){
		assert expectedType.isArray() : "expected type must be an array"; //$NON-NLS-1$
		assert ( !(actualValue instanceof Object[]) ) :
			"actual value cannot be of type Object[]"; //$NON-NLS-1$
		final Class<?> componentType = expectedType.getComponentType();
		final Object array = Array.newInstance(componentType, 1);

		if( componentType.isPrimitive() ){
            if( componentType == boolean.class ){
                final Boolean bool = (Boolean)actualValue;
                Array.setBoolean( array, 0, bool.booleanValue());
            }
            else if( componentType == byte.class ){
                final Byte b = (Byte)actualValue;
                Array.setByte( array, 0, b.byteValue() );
            }
            else if( componentType == char.class ){
                final Character c = (Character)actualValue;
                Array.setChar( array, 0, c.charValue() );
            }
            else if( componentType == double.class ){
                final Double d = (Double)actualValue;
                Array.setDouble( array, 0, d.doubleValue() );
            }
            else if( componentType == float.class ){
                final Float f = (Float)actualValue;
                Array.setFloat( array, 0, f.floatValue() );
            }
            else if( componentType == int.class ){
                final Integer integer = (Integer)actualValue;
                Array.setInt( array, 0, integer.intValue() );
            }
            else if( componentType == long.class ){
                final Long l = (Long)actualValue;
                Array.setLong( array, 0, l.longValue() );
            }
            else if( componentType == short.class ){
                final Short s = (Short)actualValue;
                Array.setShort( array, 0, s.shortValue() );
            }
            else {
                throw new IllegalStateException("unrecognized primitive type: "  + componentType ); //$NON-NLS-1$
            }
        }
        else{
            Array.set( array, 0, actualValue );
        }
		return array;
    }

    private String formatArgs(final Object[] args)
    {
        // estimate that each class name (plus the separators) is 10 characters long plus 2 for "()".
        final StringBuilder builder = new StringBuilder(args.length * 8 + 2 );
        builder.append('(');
        for( int i=0; i<args.length; i++ )
        {
            if( i > 0 ) builder.append(", "); //$NON-NLS-1$
            builder.append(args[i].getClass().getName());
        }

        builder.append(')');

        return builder.toString();
    }
}
