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
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;

public class AnnotationInfo extends ClassFileStruct implements IBinaryAnnotation 
{		
	/** The name of the annotation type */
	private char[] typename;
	/** number of bytes read */
	private int readOffset = 0;
	/** non-null to indicate taht this annontation is initialized 
	 *  @see #getMemberValuePairs()
	 */
	private ElementValuePairInfo[] pairs;
	private int[] constantPoolOffsets;
	private long annoTagBits = 0;
	/**
	 * @param classFileBytes
	 * @param offset the offset into <code>classFileBytes</code> for the 
	 * 		  "type_index" of the annotation attribute.
	 * @param populate <code>true</code> to indicate to build out the annotation structure.
	 */
	AnnotationInfo(byte[] classFileBytes, 
	 			   int offset, 
				   int[] contantPoolOffsets,
				   boolean runtimeVisible, 
				   boolean populate) 
	{
		super(classFileBytes, offset);
		this.constantPoolOffsets = contantPoolOffsets;		
		this.readOffset = 0;	
		if( populate ){
			decodeAnnotation();
		}
		else	
			this.readOffset = scanAnnotation(0, runtimeVisible, true);
	}
	
	public char[] getTypeName() {
		return this.typename;
	}
	
	public IBinaryElementValuePair[] getMemberValuePairs() {
		if( this.pairs == null ){
			initialize();
		}
		return this.pairs;
	}
	
	public long getStandardAnnotationTagBits(){ return this.annoTagBits; }
	
	void initialize()
	{
		if(this.pairs != null ) return;
		this.readOffset = 0;
		decodeAnnotation();
	}
	
	/**
	 * @return the length of this annotation.
	 */
	int getLength(){ return this.readOffset; }
	
	/**
	 * Read through this annotation in order to figure out the necessary tag bits and the length 
	 * of this annotation. The data structure will not be flushed out.
	 * 
	 * The tag bits are derived from the following (supported) standard annotation. 
	 * java.lang.annotation.Documented,
	 * java.lang.annotation.Retention,
	 * java.lang.annotation.Target, and
	 * java.lang.Deprecated
	 * 
	 * @param expectRuntimeVisibleAnno <code>true</cod> to indicate that this is a runtime-visible annotation
	 * @param toplevel <code>false</code> to indicate that an nested annotation is read. <code>true</code>
	 * 		  otherwis.e
	 * @return the next offset to read.
	 */
	private int scanAnnotation(int offset, boolean expectRuntimeVisibleAnno, boolean toplevel)
	{
		int curOffset = offset;
		int utf8Offset = this.constantPoolOffsets[u2At(offset)] - structOffset;
		char[] typeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if(toplevel)
			this.typename = typeName;
		int numberOfPairs = u2At(offset + 2);
		// u2 type_index + u2 number_member_value_pair
		curOffset += 4;
		if(expectRuntimeVisibleAnno && toplevel){	
			switch(typeName.length) {
				case 21 :
					if (CharOperation.equals(typeName, ConstantPool.JAVA_LANG_ANNOTATION_INHERITED)) {
						this.annoTagBits |= TagBits.AnnotationInherited;
						return curOffset;		
					}
					break;
				case 22 :
					if (CharOperation.equals(typeName, ConstantPool.JAVA_LANG_DEPRECATED)) {
						this.annoTagBits |= TagBits.AnnotationDeprecated;
						return curOffset;		
					}
					break;
				case 29 :
					if (CharOperation.equals(typeName, ConstantPool.JAVA_LANG_ANNOTATION_TARGET)) {		
						curOffset += 2;
						return readTargetValue(curOffset);
					}
					break;
				case 33 :
					if (CharOperation.equals(typeName, ConstantPool.JAVA_LANG_ANNOTATION_DOCUMENTED)) {
						this.annoTagBits |= TagBits.AnnotationDocumented;
						return curOffset;		
					}
					break;
				case 32 :
					if (CharOperation.equals(typeName, ConstantPool.JAVA_LANG_ANNOTATION_RETENTION)) {
						curOffset += 2;
						return readRetentionPolicy(curOffset);
					}
					break;
			}
		}
		for (int i = 0; i < numberOfPairs; i++) {
			// u2 member_name_index
			curOffset += 2;
			curOffset = scanElementValue(curOffset);
		}
		return curOffset;
	}
	
	/** 
	 * @param offset the offset to start reading.
	 * @return the next offset to read.
	 */
	private int scanElementValue(int offset) {
		int curOffset = offset;
		int tag = u1At(curOffset);
		curOffset++;
		switch(tag) {
			case 'B' :
			case 'C' :
			case 'D' :
			case 'F' :
			case 'I' :
			case 'J' :
			case 'S' :
			case 'Z' :
			case 's' :
				curOffset += 2;
				break;
			case 'e' :
				curOffset += 4;
				break;
			case 'c' :
				curOffset += 2;
				break;
			case '@' :
				// none of the supported standard annotation are in the nested level.				
				curOffset = scanAnnotation(curOffset, false, false);
				break;
			case '[' :
				int numberOfValues = u2At(curOffset);
				curOffset += 2;
				for (int i = 0; i < numberOfValues; i++) {					
					curOffset = scanElementValue(curOffset);
				}
				break;
			default:
				throw new IllegalStateException();
		}
		return curOffset;
	}
	
	private int readRetentionPolicy(int offset) {
		int curOffset = offset;
		int tag = u1At(curOffset);
		curOffset++;
		switch(tag) {			
			case 'e' :
				int utf8Offset = this.constantPoolOffsets[u2At(curOffset)] - structOffset;		
				char[] typeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				curOffset += 2;
				utf8Offset = this.constantPoolOffsets[u2At(curOffset)]- structOffset;	
				char[] constName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				curOffset += 2;
				if (typeName.length == 38 && CharOperation.equals(typeName, ConstantPool.JAVA_LANG_ANNOTATION_RETENTIONPOLICY)) 
					this.annoTagBits |= Annotation.getRetentionPolicy(constName);				
				break;
			case 'B' :
			case 'C' :
			case 'D' :
			case 'F' :
			case 'I' :
			case 'J' :
			case 'S' :
			case 'Z' :
			case 's' :
				curOffset += 2;
				break;			
			case 'c' :
				curOffset += 2;
				break;
			case '@' :
				// none of the supported standard annotation are in the nested level.
				curOffset = scanAnnotation(curOffset, false, false);
				break;
			case '[' :
				int numberOfValues = u2At(curOffset);
				curOffset += 2;
				for (int i = 0; i < numberOfValues; i++) {					
					curOffset = scanElementValue(curOffset);
				}
				break;
			default:
				throw new IllegalStateException();
		}	
		return curOffset;
	}
	
	private int readTargetValue(int offset)
	{
		int curOffset = offset;
		int tag = u1At(curOffset);
		curOffset++;
		switch(tag) {			
			case 'e' :
				int utf8Offset = this.constantPoolOffsets[u2At(curOffset)] - structOffset;	
				char[] typeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				curOffset += 2;
				utf8Offset = this.constantPoolOffsets[u2At(curOffset)] - structOffset;	
				char[] constName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
				curOffset += 2;				
				if (typeName.length == 34 && CharOperation.equals(typeName, ConstantPool.JAVA_LANG_ANNOTATION_ELEMENTTYPE)) {
					this.annoTagBits |= Annotation.getTargetElementType(constName);
				}
				break;				
				
			case 'B' :
			case 'C' :
			case 'D' :
			case 'F' :
			case 'I' :
			case 'J' :
			case 'S' :
			case 'Z' :
			case 's' :
				curOffset += 2;
				break;			
			case 'c' :
				curOffset += 2;
				break;
			case '@' :
				// none of the supported standard annotation are in the nested level.
				curOffset = scanAnnotation(curOffset, false, false);
				break;
			case '[' :
				int numberOfValues = u2At(curOffset);
				curOffset += 2;
				if (numberOfValues == 0) {
					this.annoTagBits |= TagBits.AnnotationTarget;
				} 
				else {
					for (int i = 0; i < numberOfValues; i++) {
						curOffset = readTargetValue(curOffset);
					}
				}
				break;
			default:
				throw new IllegalStateException();
		}		
		return curOffset;
	}
	
	/**
	 * Flush out the annotation data structure.
	 */
	private void decodeAnnotation()
	{
		int utf8Offset = this.constantPoolOffsets[u2At(0)] - structOffset;
		this.typename = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		int numberOfPairs = u2At(2);
		// u2 type_index + u2 num_member_value_pair
		this.readOffset += 4;
		if( numberOfPairs == 0 )
			this.pairs = ElementValuePairInfo.NoMember;
		else
			this.pairs = new ElementValuePairInfo[numberOfPairs];	
		
		for (int i = 0; i < numberOfPairs; i++) {			
			this.pairs[i] = decodePair();		
		}
	}

	private ElementValuePairInfo decodePair()
	{
		// u2    member_name_index;
		int utf8Offset = this.constantPoolOffsets[u2At(this.readOffset)] - structOffset;
		char[] membername = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		this.readOffset += 2;
		final ElementValueInfo elementValue = new ElementValueInfo(this.reference, this.constantPoolOffsets, 
														   this.readOffset + this.structOffset);
		final Object value = elementValue.decodeValue();
		this.readOffset += elementValue.getLength();
		return new ElementValuePairInfo(membername, value);		
	}
	
	protected void reset() {
		this.constantPoolOffsets = null;
		super.reset();
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append('@');
		buffer.append(this.typename);
		if(this.pairs != null){			
			buffer.append('(');
			buffer.append("\n\t"); //$NON-NLS-1$
			for( int i=0, len = this.pairs.length; i<len; i++ ){
				if( i > 0 )
					buffer.append(",\n\t"); //$NON-NLS-1$
				buffer.append(this.pairs[i]);
			}
			buffer.append(')');
		}
		
		return buffer.toString();
	}
}
