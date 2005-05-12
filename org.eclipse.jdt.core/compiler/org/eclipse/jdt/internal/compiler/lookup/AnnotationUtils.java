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
package org.eclipse.jdt.internal.compiler.lookup;

class AnnotationUtils {
	/**
	 * Build out the data structure representing the standard annotations.
	 * @param annotationTagBits 
	 * @param result to be filled by this method starting from index 0
	 * @param env
	 * @return the number of standard annotations found 
	 */
	static int buildStandardAnnotations(final long annotationTagBits, 
										final IAnnotationInstance[] result,
										final LookupEnvironment env)
	{
		int index = 0;
		if( (annotationTagBits & TagBits.AnnotationTargetMASK) != 0 )
			result[index++] = buildTargetAnnotation(annotationTagBits, env);
		if( (annotationTagBits & TagBits.AnnotationRetentionMASK) != 0 ) 
			result[index++] = buildRetentionAnnotation(annotationTagBits, env);
		if( (annotationTagBits & TagBits.AnnotationDeprecated) != 0 )
			result[index++] = buildMarkerAnnotation(TypeConstants.CharArray_JAVA_LANG_DEPRECATED_SIG, env);
		if( (annotationTagBits & TagBits.AnnotationDocumented) != 0 )
			result[index++] = buildMarkerAnnotation(TypeConstants.CharArray_JAVA_LANG_ANNOTATION_DOCUMENTED_SIG, env);
		if( (annotationTagBits & TagBits.AnnotationInherited) != 0 )
			result[index++] = buildMarkerAnnotation(TypeConstants.CharArray_JAVA_LANG_ANNOTATION_INHERITED_SIG, env);
		if( (annotationTagBits & TagBits.AnnotationOverride) != 0 )
			result[index++] = buildMarkerAnnotation(TypeConstants.CharArray_JAVA_LANG_OVERRIDE_SIG, env);
		if( (annotationTagBits & TagBits.AnnotationSuppressWarnings) != 0 )
			result[index++] = buildMarkerAnnotation(TypeConstants.CharArray_JAVA_LANG_SUPRESSWARNING_SIG, env);
		return index;
	}

	static int getNumberOfStandardAnnotations(final long annotationTagBits)
	{	
		int count = 0;
		if( (annotationTagBits & TagBits.AnnotationTargetMASK) != 0 ) count ++;		
		if( (annotationTagBits & TagBits.AnnotationRetentionMASK) != 0 ) count ++;
		if( (annotationTagBits & TagBits.AnnotationDeprecated) != 0 ) count ++;
		if( (annotationTagBits & TagBits.AnnotationDocumented) != 0 ) count ++;
		if( (annotationTagBits & TagBits.AnnotationInherited) != 0 ) count ++;
		if( (annotationTagBits & TagBits.AnnotationOverride) != 0 ) count ++;
		if( (annotationTagBits & TagBits.AnnotationSuppressWarnings) != 0 ) count ++;
		
		return count;	
	}

	private static IAnnotationInstance buildMarkerAnnotation(final char[] sig, final LookupEnvironment env)
	{
		final ReferenceBinding type = (ReferenceBinding)env.getTypeFromSignature(sig, 0, -1, false, null);
		return new BinaryAnnotation(type, env);
	}

	private static IAnnotationInstance buildTargetAnnotation(final long bits, final LookupEnvironment env)
	{	
		final ReferenceBinding target = (ReferenceBinding)
			env.getTypeFromSignature(TypeConstants.CharArray_JAVA_LANG_ANNOTATION_TARGET_SIG, 
									 0, -1, false, null);
		final BinaryAnnotation anno = new BinaryAnnotation(target, env);
		if( (bits & TagBits.AnnotationTarget) != 0 )
			return anno;
		
		ReferenceBinding elementType = (ReferenceBinding)
			env.getTypeFromSignature(TypeConstants.CharArray_JAVA_LANG_ANNOTATION_ELEMENTTYPE_SIG, 
					  							  0, -1, false, null);
		int arraysize = 0;
		if( (bits & TagBits.AnnotationForAnnotationType) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForConstructor) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForField) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForLocalVariable) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForMethod) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForPackage) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForParameter) != 0 ) arraysize ++;
		if( (bits & TagBits.AnnotationForType) != 0 ) arraysize ++;
		
		final Object[] value = new Object[arraysize];
		if( arraysize > 0 ){
			elementType = BinaryTypeBinding.resolveType(elementType, env, false);
			int index = 0;
			if( (bits & TagBits.AnnotationForAnnotationType) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.UPPER_ANNOTATION_TYPE, true);
			if( (bits & TagBits.AnnotationForConstructor) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.UPPER_CONSTRUCTOR, true);
			if( (bits & TagBits.AnnotationForField) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.UPPER_FIELD, true);
			if( (bits & TagBits.AnnotationForLocalVariable) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.UPPER_LOCAL_VARIABLE, true);
			if( (bits & TagBits.AnnotationForMethod) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.UPPER_METHOD, true);
			if( (bits & TagBits.AnnotationForPackage) != 0 )
				value[index++] = elementType.getField(TypeConstants.UPPER_PACKAGE, true);
			if( (bits & TagBits.AnnotationForParameter) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.UPPER_PARAMETER, true);
			if( (bits & TagBits.AnnotationForType) != 0 ) 
				value[index++] = elementType.getField(TypeConstants.TYPE, true);
		}
		
		anno.pairs = new IElementValuePair[]{ new BinaryElementValuePair(anno, TypeConstants.VALUE, value) };
		return anno;
	}

	private static IAnnotationInstance buildRetentionAnnotation(final long bits, final LookupEnvironment env)
	{
		final ReferenceBinding retention = (ReferenceBinding)
		env.getTypeFromSignature(TypeConstants.CharArray_JAVA_LANG_ANNOTATION_RETENTION_SIG, 
											  0, -1, false, null);
		ReferenceBinding retentionPolicy = (ReferenceBinding)
			env.getTypeFromSignature(TypeConstants.CharArray_JAVA_LANG_ANNOTATION_RETENTIONPOLICY_SIG, 
					  							  0, -1, false, null);

		final BinaryAnnotation anno = new BinaryAnnotation(retention, env);
		
		Object value = null; 	
		{
			retentionPolicy = BinaryTypeBinding.resolveType(retentionPolicy, env, false);		
			if( (bits & TagBits.AnnotationRuntimeRetention) != 0 ) 
				value = retentionPolicy.getField(TypeConstants.UPPER_RUNTIME, true);		
			else if( (bits & TagBits.AnnotationClassRetention) != 0 ) 
				value = retentionPolicy.getField(TypeConstants.UPPER_CLASS, true);
			else if( (bits & TagBits.AnnotationSourceRetention) != 0 ) 
				value = retentionPolicy.getField(TypeConstants.UPPER_SOURCE, true);
			
		}
		anno.pairs = new IElementValuePair[]{ new BinaryElementValuePair(anno, TypeConstants.VALUE, value) };
		return anno;

	}

}
