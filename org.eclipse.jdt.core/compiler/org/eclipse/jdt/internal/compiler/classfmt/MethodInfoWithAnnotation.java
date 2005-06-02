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
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;

class MethodInfoWithAnnotation extends MethodInfo
{
	/** method annotation as well as parameter annotations 
	 * index 0 always contains the method annotation info.
	 * index 1 and onwards contains parameter annotation info. 
	 * If the array is of size 0, there are no annotations.
	 * If the array is of size 1, there are only method annotations.
	 * if the array has a size greater than 1, then there are at least 
	 * parameter annotations.
	 */
	private AnnotationInfo[][] allAnnotations;
	
	MethodInfoWithAnnotation(MethodInfo methodInfo, AnnotationInfo[][] annotations)
	{
		super(methodInfo.reference, methodInfo.constantPoolOffsets, methodInfo.structOffset);
		allAnnotations = annotations;
		accessFlags = methodInfo.accessFlags;
		attributeBytes = methodInfo.attributeBytes;
		constantPoolOffsets = methodInfo.constantPoolOffsets;
		descriptor = methodInfo.descriptor;
		exceptionNames = methodInfo.exceptionNames;
		name = methodInfo.name;
		signature = methodInfo.signature;
		signatureUtf8Offset = methodInfo.signatureUtf8Offset;
		tagBits = methodInfo.tagBits;
	}
	
	void initialize() {
		for( int i=0, max = allAnnotations.length; i<max; i++ ){
			if( this.allAnnotations[i] != null ){
				for( int aIndex=0, aMax = allAnnotations[i].length; aIndex<aMax; aIndex++ ){
					final AnnotationInfo anno = allAnnotations[i][aIndex];
					anno.initialize();					
				}				
			}
		}			
		super.initialize();		
	}
	
	protected void reset() {
		for( int i=0, max = allAnnotations.length; i<max; i++ ){
			if( this.allAnnotations[i] != null ){
				final int aMax = allAnnotations[i].length;
				for( int aIndex=0; aIndex<aMax; aIndex++ ){
					final AnnotationInfo anno = allAnnotations[i][aIndex];
					anno.reset();					
				}				
			}
		}
		super.reset();
	}
	
	/**
	 * @return the annotations or null if there is none.
	 */
	public IBinaryAnnotation[] getAnnotations()
	{		
		return allAnnotations[0];
	}

	public IBinaryAnnotation[] getParameterAnnotations(int index)
	{
		if( allAnnotations.length < 2  ) return null;
		return allAnnotations[index + 1];
	}
	
	void toString(StringBuffer buffer)
	{	
		buffer.append(this.getClass().getName());	
		
		final int totalNumAnno = allAnnotations.length;
		if(totalNumAnno > 0){
			buffer.append('\n');
			if(allAnnotations[0] != null ){
				for( int i=0, len = allAnnotations[0].length; i<len; i++ ){		
					
					buffer.append(allAnnotations[0][i]);
					buffer.append('\n');
				}	
			}
		}
		
		if(totalNumAnno > 1){		
			buffer.append('\n');
			for( int i=1; i<totalNumAnno; i++ ){
				buffer.append("param" + (i-1)); //$NON-NLS-1$
				buffer.append('\n');
				if( allAnnotations[i] != null ){
					for( int j=0, numParamAnno=allAnnotations[i].length; j<numParamAnno; j++){
						buffer.append(allAnnotations[i][j]);
						buffer.append('\n');
					}
				}
			}
		}
		
		toStringContent(buffer);
	}
}
