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

public final class FieldInfoWithAnnotation extends FieldInfo 
{
	private AnnotationInfo[] annotations;	
	
	FieldInfoWithAnnotation(FieldInfo info, AnnotationInfo[] annos)
	{
		super(info.reference, info.constantPoolOffsets, info.structOffset);
		accessFlags = info.accessFlags;
		attributeBytes = info.attributeBytes;
		constant = info.constant;
		constantPoolOffsets = info.constantPoolOffsets;
		descriptor = info.descriptor;
		name = info.name;
		signature = info.signature;
		signatureUtf8Offset = info.signatureUtf8Offset;
		tagBits = info.tagBits;
		wrappedConstantValue = info.wrappedConstantValue;
		annotations = annos; 
	}
	
	public IBinaryAnnotation[] getAnnotations(){
		return this.annotations;
	}
	
	void initialize() {
		
		for( int i=0, max = annotations.length; i<max; i++ ){
			annotations[i].initialize();
		}
		super.initialize();
	}
	
	protected void reset() {
		this.constantPoolOffsets = null;
		if( annotations != null ){
			for( int i=0, max = annotations.length; i<max; i++ ){
				annotations[i].reset();
			}
		}
		super.reset();
	}
	
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer(this.getClass().getName());
		if(this.annotations != null){
			buffer.append('\n');
			for( int i=0; i<this.annotations.length; i++ ){			
				buffer.append(annotations[i]);
				buffer.append('\n');
			}
		}
		toStringContent(buffer);
		return buffer.toString();		
	}
}
