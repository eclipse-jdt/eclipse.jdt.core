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

import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;

/**
 * This is a transitional object for decoding a element value of an annotation 
 * and default value of a annotation method. 
 */
public class ElementValueInfo extends ClassFileStruct 
{
	private int readOffset = 0;		
	private int[] constantPoolOffsets;
	private Object value = null;
	
	ElementValueInfo(final byte[] classFileBytes, 
						final int[] constantPoolOffsets, 
						final int absoluteOffset)
	{
		super(classFileBytes, absoluteOffset);			
		this.constantPoolOffsets = constantPoolOffsets;
	}
	
	/**
	 * @return the length of this annotation.
	 */
	int getLength(){ return this.readOffset; }
	
	Object decodeValue()
	{
		// u1 tag;
		int tag = u1At(this.readOffset);		
		this.readOffset++;
		int constValueOffset = -1;
		switch(tag) {
			case 'Z' : // boolean constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new BooleanConstant(i4At(constValueOffset+1) == 1);
				this.readOffset += 2;				
				break;
			case 'I' : // integer constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new IntConstant(i4At(constValueOffset+1));
				this.readOffset += 2;
				break;
			case 'C' : // char constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new CharConstant((char)i4At(constValueOffset+1));
				this.readOffset += 2;
				break;
			case 'B' : // byte constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new ByteConstant((byte) i4At(constValueOffset+1));
				this.readOffset += 2;
				break;
			case 'S' : // short constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new ShortConstant((short) i4At(constValueOffset+1));
				this.readOffset += 2;
				break;			
			case 'D' : // double constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new DoubleConstant(doubleAt(constValueOffset+1));
				this.readOffset += 2;
				break;
			case 'F' :	// float constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new FloatConstant(floatAt(constValueOffset+1));
				this.readOffset += 2;
				break;
			case 'J' :  // long constant
				constValueOffset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new LongConstant(i8At(constValueOffset+1));
				this.readOffset += 2;
				break;
			case 's' : // String	
			{
				int utf8Offset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				this.value = new StringConstant(
							 	 String.valueOf(utf8At(utf8Offset + 3, u2At(utf8Offset + 1))));
				this.readOffset += 2;
				break;	
			}
			case 'e' :
			{
				int utf8Offset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				char[] typeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				this.readOffset += 2;
				utf8Offset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				char[] constName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				this.readOffset += 2;				
				this.value = new EnumReference(typeName, constName);
				break;
			}
			case 'c' :
			{
				int utf8Offset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
				char[] className = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				this.value = new ClassReference(className);
				this.readOffset += 2;
				break;
			}
			case '@' :
				this.value = new AnnotationInfo(reference, this.readOffset + structOffset, this.constantPoolOffsets, false, true);
				this.readOffset += ((AnnotationInfo)value).getLength();
				break;
			case '[' :	
				int numberOfValues = u2At(this.readOffset);				
				this.readOffset += 2;
				if( numberOfValues == 0 )
					this.value = ElementValuePairInfo.ZeroLengthArray;
				else{	
					Object[] arrayElements = new Object[numberOfValues];
					this.value = arrayElements;	
					for (int i = 0; i < numberOfValues; i++) {
						final ElementValueInfo elementValue = new ElementValueInfo(this.reference, this.constantPoolOffsets, 
								   this.readOffset + this.structOffset);
						arrayElements[i] = elementValue.decodeValue();
						this.readOffset += elementValue.getLength();
					}			
				}		
				break;
			default:
				throw new IllegalStateException("Unrecognized tag " + (char)tag); //$NON-NLS-1$	
			
		}
		return this.value;
	}
}
