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

import org.eclipse.jdt.core.compiler.CharOperation;

public class AnnotationMethodInfo extends MethodInfo 
{	
	protected Object defaultValue = null;	
	
	public static AnnotationMethodInfo createAnnotationMethod(byte classFileBytes[], int constantPoolOffsets[], int offset)
	{
		final AnnotationMethodInfo methodInfo = new AnnotationMethodInfo(classFileBytes, constantPoolOffsets, offset);
		final AnnotationInfo[][] allAnnotations = _createMethod(methodInfo);
		methodInfo.readAttributes();
		if( allAnnotations == null )
			return methodInfo;
		else
			return new AnnotationMethodInfoWithAnnotation(methodInfo, allAnnotations[0]);
	}
	
	protected AnnotationMethodInfo(byte classFileBytes[], int constantPoolOffsets[], int offset)
	{	
		super(classFileBytes, constantPoolOffsets, offset);		
	}
	
	private void readAttributes()
	{
		int attributesCount = u2At(6);
		int readOffset = 8;		
		for (int i = 0; i < attributesCount; i++) {
			// check the name of each attribute
			int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (attributeName.length > 0) {
				switch(attributeName[0]) {
					case 'A' :
						if(CharOperation.equals(attributeName, AnnotationDefaultName)){		
							decodeDefaultValue(readOffset);
						}
						break;
				}
			}
			readOffset += (6 + u4At(readOffset + 2));
		}		
	}
	
	private void decodeDefaultValue( int offset )
	{
		// offset + 6 so the offset is at the start of the 'member_value' entry
		// u2 attribute_name_index + u4 attribute_length = + 6
		final ElementValueInfo defaultValueInfo = 
			new ElementValueInfo(this.reference, this.constantPoolOffsets, offset + 6 + this.structOffset );
		this.defaultValue = defaultValueInfo.decodeValue();
	}
	
	public Object getDefaultValue(){ return this.defaultValue; }
	
	protected void toStringDefault(StringBuffer buffer)
	{
		if( this.defaultValue != null )
		{
			buffer.append(" default "); //$NON-NLS-1$
			if( this.defaultValue instanceof Object[] )
			{
				buffer.append('{');
				final Object[] elements = (Object[])this.defaultValue;
				for( int i=0, len = elements.length; i<len; i++ ){
					if( i > 0 )
						buffer.append(", "); //$NON-NLS-1$
					buffer.append(elements[i]);
				}
				buffer.append('}');
			}
			else
				buffer.append(this.defaultValue);
		}
	}
	
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append(this.getClass().getName());
		toStringContent(buffer);
		toStringDefault(buffer);
		
		return buffer.toString();
	}
}
