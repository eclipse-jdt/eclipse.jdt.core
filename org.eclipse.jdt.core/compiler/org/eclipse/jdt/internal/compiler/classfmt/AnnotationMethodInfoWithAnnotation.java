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

public class AnnotationMethodInfoWithAnnotation extends AnnotationMethodInfo{
	private AnnotationInfo[] annotations;
	
	AnnotationMethodInfoWithAnnotation(AnnotationMethodInfo methodInfo, AnnotationInfo[] anno)
	{
		super(methodInfo.reference, methodInfo.constantPoolOffsets, methodInfo.structOffset);
		annotations = anno;
		defaultValue = methodInfo.defaultValue;
	}
	
	void initialize() {
		for( int i=0, max = annotations.length; i<max; i++ ){
			if( annotations[i] != null ){
				annotations[i].initialize();									
			}
		}
		super.initialize();		
	}
	
	protected void reset() {
		for( int i=0, max = annotations.length; i<max; i++ ){
			if( annotations[i] != null ){
				annotations[i].reset();
			}
		}
		super.reset();
	}
	
	public IBinaryAnnotation[] getAnnotations()
	{		
		return annotations;
	}

	public IBinaryAnnotation[] getParameterAnnotations(int index)
	{
		return null;
	}
	
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append(this.getClass().getName());	
		if( annotations != null ){
			for( int i=0, len = annotations.length; i<len; i++ ){						
				buffer.append(annotations[i]);
				buffer.append('\n');
			}	
		}
		toStringContent(buffer);
		toStringDefault(buffer);
		
		return buffer.toString();
	}

}
