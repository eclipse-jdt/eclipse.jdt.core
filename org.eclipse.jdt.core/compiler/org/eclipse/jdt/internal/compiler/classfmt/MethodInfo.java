/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

public class MethodInfo extends ClassFileStruct implements IBinaryMethod, AttributeNamesConstants, Comparable {
	static private final char[][] noException = CharOperation.NO_CHAR_CHAR;
	private int accessFlags;
	private int attributeBytes;
	protected int[] constantPoolOffsets;
	private char[] descriptor;
	private char[][] exceptionNames;
	private char[] name;
	private char[] signature;
	private int signatureUtf8Offset;
	private long tagBits;
	/** method annotation as well as parameter annotations 
	 * index 0 always contains the method annotation info.
	 * index 1 and onwards contains parameter annotation info. 
	 * If the array is of size 0, there are no annotations.
	 * If the array is of size 1, there are only method annotations.
	 * if the array has a size greater than 1, then there are at least 
	 * parameter annotations.
	 */
	private AnnotationInfo[][] allAnnotations;	
	
/**
 * @param classFileBytes byte[]
 * @param offsets int[]
 * @param offset int
 */
public MethodInfo (byte classFileBytes[], int offsets[], int offset) {
	super(classFileBytes, offset);
	constantPoolOffsets = offsets;
	accessFlags = -1;
	int attributesCount = u2At(6);
	int readOffset = 8;
	this.signatureUtf8Offset = -1;
	for (int i = 0; i < attributesCount; i++) {
		// check the name of each attribute
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (attributeName.length > 0) {
			switch(attributeName[0]) {				
				case 'S' :
					if (CharOperation.equals(AttributeNamesConstants.SignatureName, attributeName)) {
						this.signatureUtf8Offset = constantPoolOffsets[u2At(readOffset + 6)] - structOffset;
					}
					break;
				case 'R' :
					if (CharOperation.equals(attributeName, RuntimeVisibleAnnotationsName)) {
						decodeMethodAnnotations(readOffset, true);
					}
					else if (CharOperation.equals(attributeName, RuntimeInvisibleAnnotationsName)) {
						decodeMethodAnnotations(readOffset, false);
					}
					else if( CharOperation.equals(attributeName, RuntimeVisibleParameterAnnotationsName)){
						decodeParamAnnotations(readOffset, true);						
					}
					else if( CharOperation.equals(attributeName, RuntimeInvisibleParameterAnnotationsName)){
						decodeParamAnnotations(readOffset, false);
					}
			}
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
	attributeBytes = readOffset;
}
public int compareTo(Object o) {
	if (!(o instanceof MethodInfo)) {
		throw new ClassCastException();
	}

	MethodInfo otherMethod = (MethodInfo) o;
	int result = new String(this.getSelector()).compareTo(new String(otherMethod.getSelector()));
	if (result != 0) return result;
	return new String(this.getMethodDescriptor()).compareTo(new String(otherMethod.getMethodDescriptor()));
}

/**
 * @param offset the offset is located at the beginning of the 
 * parameter annotation attribute.
 */
private void decodeParamAnnotations(int offset, boolean runtimeVisible)
{		
	// u1 num_parameters;
	int numberOfParameters = u1At(offset + 6);
	if( numberOfParameters > 0 ){
		// u2 attribute_name_index + u4 attribute_length + u1 num_parameters
		int readOffset = offset + 7;
		for( int i=0; i<numberOfParameters; i++ ){
			int numberOfAnnotations = u2At(readOffset);
			readOffset += 2;	    
			if( numberOfAnnotations > 0 ){	
				if(this.allAnnotations == null){
					this.allAnnotations = new AnnotationInfo[numberOfParameters + 1][];
					for(int j=0, len = numberOfParameters + 1; j < len; j++){
						this.allAnnotations[j] = null;
					}
				}
				else{
					if( this.allAnnotations.length == 1 ){
						// make room for the parameter annotations
						final AnnotationInfo[][] newArray = new AnnotationInfo[numberOfParameters + 1][];
						newArray[0] = this.allAnnotations[0];
						this.allAnnotations = newArray;
						for(int j=1; j <= numberOfParameters; j++){
							this.allAnnotations[j] = null;
						}
					}
					// else
					// have already initialize the field to the proper size.
				}
				final AnnotationInfo[] annos = 
					decodeAnnotations(readOffset, runtimeVisible, numberOfAnnotations);
				for( int aIndex = 0; aIndex < annos.length; aIndex ++ )
					readOffset += annos[aIndex].getLength();
				final int paramAnnoIndex = i + 1;
				if( this.allAnnotations[paramAnnoIndex] == null )
					this.allAnnotations[paramAnnoIndex] = annos;
				else{
					final int curlen = this.allAnnotations[paramAnnoIndex].length;
					final int newTotal = curlen + numberOfAnnotations;
					final AnnotationInfo[] newAnnos = new AnnotationInfo[newTotal];
					System.arraycopy(this.allAnnotations[paramAnnoIndex], 0, newAnnos, 0, curlen);
					System.arraycopy(annos, 0, newAnnos, curlen, numberOfAnnotations);
					this.allAnnotations[paramAnnoIndex] = newAnnos;
				}
			}
		}
	}
}

/**
 * @param offset begining of the 'RuntimeVisibleAnnotation' or 'RuntimeInvisibleAnnotation'
 * attribute.
 * @param runtimeVisible <code>true</code> to indicate decoding 'RuntimeVisibleAnnotation'
 */
private void decodeMethodAnnotations(int offset, boolean runtimeVisible){
	int numberOfAnnotations = u2At(offset + 6);
	AnnotationInfo[] annos = decodeAnnotations(offset + 8, runtimeVisible, numberOfAnnotations);
	
	if( numberOfAnnotations > 0 ){
		if( runtimeVisible ){
			int numStandardAnnotations = 0;			
			for( int i=0; i<numberOfAnnotations; i++ ){
				final long standardAnnoTagBits = annos[i].getStandardAnnotationTagBits();
				this.tagBits |= standardAnnoTagBits;
				if(standardAnnoTagBits != 0){
					annos[i] = null;
					numStandardAnnotations ++;
				}
			}
			
			if( numStandardAnnotations != 0 ){
				if( numStandardAnnotations == numberOfAnnotations )
					return;
				
				// need to resize			
				AnnotationInfo[] temp = new AnnotationInfo[numberOfAnnotations - numStandardAnnotations ];
				int tmpIndex = 0;
				for( int i=0; i<numberOfAnnotations; i++ ){
					if( annos[i] != null )
						temp[tmpIndex ++] = annos[i];
				}
				annos = temp;
				numberOfAnnotations = numberOfAnnotations - numStandardAnnotations;				
			}
		}
		
		if( this.allAnnotations == null )
			this.allAnnotations = new AnnotationInfo[][]{annos};
		else{
			int curlen = this.allAnnotations[0].length;
			int newTotal = curlen + numberOfAnnotations;
			final AnnotationInfo[] newAnnos = new AnnotationInfo[newTotal];
			System.arraycopy(this.allAnnotations[0], 0, newAnnos, 0, curlen);
			System.arraycopy(annos, 0, newAnnos, curlen, numberOfAnnotations);
			this.allAnnotations[0] = newAnnos;
		}
	}
}

/**
 * @param offset the offset is located at the beginning of the  
 * annotation attribute.
 */
private AnnotationInfo[] decodeAnnotations(int offset, 
										   boolean runtimeVisible,
										   int numberOfAnnotations) {
	int readOffset = offset;
	AnnotationInfo[] result = null;
	if( numberOfAnnotations > 0 ){	
		result = new AnnotationInfo[numberOfAnnotations];	
	}	
	
	for (int i = 0; i < numberOfAnnotations; i++) {
		result[i] = new AnnotationInfo(reference, 
									   readOffset + structOffset, 
									   this.constantPoolOffsets, 
									   runtimeVisible, 
									   false);		
		readOffset = result[i].getLength() + readOffset;		
	}
	return result;
}

/**
 * @return the annotations or null if there is none.
 */
public IBinaryAnnotation[] getAnnotations(){
	if( this.allAnnotations == null || this.allAnnotations.length == 0 ) return null;
	return this.allAnnotations[0];
}

public IBinaryAnnotation[] getParameterAnnotations(int index)
{
	if(this.allAnnotations == null || this.allAnnotations.length < 2  ) return null;
	return this.allAnnotations[index + 1];
}

/**
 * @see org.eclipse.jdt.internal.compiler.env.IGenericMethod#getArgumentNames()
 */
public char[][] getArgumentNames() {
	return null;
}
/**
 * Answer the resolved names of the exception types in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[][]
 */
public char[][] getExceptionTypeNames() {
	if (exceptionNames == null) {
		readExceptionAttributes();
	}
	return exceptionNames;
}
public char[] getGenericSignature() {
	if (this.signatureUtf8Offset != -1) {
		if (this.signature == null) {
			// decode the signature
			this.signature = utf8At(this.signatureUtf8Offset + 3, u2At(this.signatureUtf8Offset + 1));
		}
		return this.signature;
	}
	return null;
}
/**
 * Answer the receiver's method descriptor which describes the parameter &
 * return types as specified in section 4.3.3 of the Java 2 VM spec.
 *
 * For example:
 *   - int foo(String) is (Ljava/lang/String;)I
 *   - void foo(Object[]) is (I)[Ljava/lang/Object;
 * @return char[]
 */
public char[] getMethodDescriptor() {
	if (descriptor == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(4)] - structOffset;
		descriptor = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return descriptor;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Set the AccDeprecated and AccSynthetic bits if necessary
 * @return int
 */
public int getModifiers() {
	if (this.accessFlags == -1) {
		// compute the accessflag. Don't forget the deprecated attribute
		this.accessFlags = u2At(0);
		readModifierRelatedAttributes();
	}
	return this.accessFlags;
}
/**
 * Answer the name of the method.
 *
 * For a constructor, answer <init> & <clinit> for a clinit method.
 * @return char[]
 */
public char[] getSelector() {
	if (name == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(2)] - structOffset;
		name = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return name;
}
public long getTagBits() {
	return this.tagBits;
}
/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
void initialize() {
	getModifiers();
	getSelector();
	getMethodDescriptor();
	getExceptionTypeNames();
	getGenericSignature();
	if( this.allAnnotations != null ){
		for( int i=0, max = this.allAnnotations.length; i<max; i++ ){
			if( this.allAnnotations[i] != null ){
				for( int aIndex=0, aMax = this.allAnnotations[i].length; aIndex<aMax; aIndex++ ){
					final AnnotationInfo anno = this.allAnnotations[i][aIndex];
					anno.initialize();					
				}				
			}
		}
	}	
	reset();
}
/**
 * Answer true if the method is a class initializer, false otherwise.
 * @return boolean
 */
public boolean isClinit() {
	char[] selector = getSelector();
	return selector[0] == '<' && selector.length == 8; // Can only match <clinit>
}
/**
 * Answer true if the method is a constructor, false otherwise.
 * @return boolean
 */
public boolean isConstructor() {
	char[] selector = getSelector();
	return selector[0] == '<' && selector.length == 6; // Can only match <init>
}
/**
 * Return true if the field is a synthetic method, false otherwise.
 * @return boolean
 */
public boolean isSynthetic() {
	return (getModifiers() & AccSynthetic) != 0;
}
private void readExceptionAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation.equals(attributeName, ExceptionsName)) {
			// read the number of exception entries
			int entriesNumber = u2At(readOffset + 6);
			// place the readOffset at the beginning of the exceptions table
			readOffset += 8;
			if (entriesNumber == 0) {
				exceptionNames = noException;
			} else {
				exceptionNames = new char[entriesNumber][];
				for (int j = 0; j < entriesNumber; j++) {
					utf8Offset = 
						constantPoolOffsets[u2At(
							constantPoolOffsets[u2At(readOffset)] - structOffset + 1)]
							- structOffset; 
					exceptionNames[j] = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
					readOffset += 2;
				}
			}
		} else {
			readOffset += (6 + u4At(readOffset + 2));
		}
	}
	if (exceptionNames == null) {
		exceptionNames = noException;
	}
}
private void readModifierRelatedAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		// test added for obfuscated .class file. See 79772
		if (attributeName.length != 0) {
			switch(attributeName[0]) {
				case 'D' :
					if (CharOperation.equals(attributeName, DeprecatedName))
						this.accessFlags |= AccDeprecated;
					break;
				case 'S' :
					if (CharOperation.equals(attributeName, SyntheticName))
						this.accessFlags |= AccSynthetic;
					break;
				case 'A' :
					if (CharOperation.equals(attributeName, AnnotationDefaultName))
						this.accessFlags |= AccAnnotationDefault;
					break;
			}
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
}
protected void reset() {
	this.constantPoolOffsets = null;
	if( this.allAnnotations != null ){
		for( int i=0, max = this.allAnnotations.length; i<max; i++ ){
			if( this.allAnnotations[i] != null ){
				final int aMax = this.allAnnotations[i].length;
				for( int aIndex=0; aIndex<aMax; aIndex++ ){
					final AnnotationInfo anno = this.allAnnotations[i][aIndex];
					anno.reset();					
				}				
			}
		}
	}
	super.reset();
}
/**
 * Answer the size of the receiver in bytes.
 * 
 * @return int
 */
public int sizeInBytes() {
	return attributeBytes;
}

public Object getDefaultValue(){ return null; }

void toString(StringBuffer buffer)
{
	int modifiers = getModifiers();
	char[] desc = getGenericSignature();
	if (desc == null)
		desc = getMethodDescriptor();
	
	buffer.append(this.getClass().getName());	
	
	final int totalNumAnno = this.allAnnotations == null ? 0 : this.allAnnotations.length;
	if(totalNumAnno > 0){
		buffer.append('\n');
		if(this.allAnnotations[0] != null ){
			for( int i=0, len = this.allAnnotations[0].length; i<len; i++ ){		
				
				buffer.append(this.allAnnotations[0][i]);
				buffer.append('\n');
			}	
		}
	}
	
	if(totalNumAnno > 1){		
		buffer.append('\n');
		for( int i=1; i<totalNumAnno; i++ ){
			buffer.append("param" + (i-1)); //$NON-NLS-1$
			buffer.append('\n');
			if( this.allAnnotations[i] != null ){
				for( int j=0, numParamAnno=this.allAnnotations[i].length; j<numParamAnno; j++){
					buffer.append(this.allAnnotations[i][j]);
					buffer.append('\n');
				}
			}
		}
	}
	
	buffer
		.append("{") //$NON-NLS-1$
		.append(
			((modifiers & AccDeprecated) != 0 ? "deprecated " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0001) == 1 ? "public " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0002) == 0x0002 ? "private " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0004) == 0x0004 ? "protected " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0008) == 0x000008 ? "static " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0010) == 0x0010 ? "final " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0040) == 0x0040 ? "volatile " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0080) == 0x0080 ? "varargs " : "")) //$NON-NLS-1$ //$NON-NLS-2$
		.append(getSelector())
		.append(desc)
		.append("}"); //$NON-NLS-1$ 
}
public String toString() {
	final StringBuffer buffer = new StringBuffer();
	toString(buffer);
	return buffer.toString();
}
}
