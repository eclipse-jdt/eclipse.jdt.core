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

public class MethodInfo extends ClassFileStruct implements IBinaryMethod, Comparable {
	static private final char[][] noException = CharOperation.NO_CHAR_CHAR;
	protected int accessFlags;
	protected int attributeBytes;
	protected int[] constantPoolOffsets;
	protected char[] descriptor;
	protected char[][] exceptionNames;
	protected char[] name;
	protected char[] signature;
	protected int signatureUtf8Offset;
	protected long tagBits;
	

public static MethodInfo createMethod(byte classFileBytes[], int offsets[], int offset ){
	final MethodInfo methodInfo = new MethodInfo(classFileBytes, offsets, offset);
	
	AnnotationInfo[][] allAnnotations = _createMethod(methodInfo); 
	if( allAnnotations == null )
		return methodInfo;
	else
		return new MethodInfoWithAnnotation(methodInfo, allAnnotations);
}

/**
 * populate the given method info
 * @param methodInfo
 * @return all the method and parameter annotations if any. Return null otherwise.
 */
protected static AnnotationInfo[][] _createMethod(MethodInfo methodInfo)
{
	int attributesCount = methodInfo.u2At(6);
	int readOffset = 8;
	AnnotationInfo[][] allAnnotations = null;
	
	for (int i = 0; i < attributesCount; i++) {
		// check the name of each attribute
		int utf8Offset = methodInfo.constantPoolOffsets[methodInfo.u2At(readOffset)] - methodInfo.structOffset;
		char[] attributeName = methodInfo.utf8At(utf8Offset + 3, methodInfo.u2At(utf8Offset + 1));
		if (attributeName.length > 0) {
			
			switch(attributeName[0]) {				
				case 'S' :
					if (CharOperation.equals(AttributeNamesConstants.SignatureName, attributeName)) {
						methodInfo.signatureUtf8Offset = 
							methodInfo.constantPoolOffsets[methodInfo.u2At(readOffset + 6)] - methodInfo.structOffset;
					}
					break;
				case 'R' :
					AnnotationInfo[] methodAnnos = null;
					AnnotationInfo[][] paramAnnos = null;
					if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleAnnotationsName)) {
						methodAnnos = decodeMethodAnnotations(readOffset, true, methodInfo);						
					}
					else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleAnnotationsName)) {
						methodAnnos = decodeMethodAnnotations(readOffset, false, methodInfo);
					}
					else if( CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleParameterAnnotationsName)){
						paramAnnos = decodeParamAnnotations(readOffset, true, methodInfo);						
					}
					else if( CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleParameterAnnotationsName)){
						paramAnnos = decodeParamAnnotations(readOffset, false, methodInfo);
					}
					if( methodAnnos != null ){
						if( allAnnotations == null )
							allAnnotations = new AnnotationInfo[][]{methodAnnos};
						else{
							int curlen = allAnnotations[0].length;
							int numberOfAnnotations = methodAnnos.length;
							int newTotal = curlen + numberOfAnnotations;
							final AnnotationInfo[] newAnnos = new AnnotationInfo[newTotal];
							System.arraycopy(allAnnotations[0], 0, newAnnos, 0, curlen);
							System.arraycopy(methodAnnos, 0, newAnnos, curlen, numberOfAnnotations);
							allAnnotations[0] = newAnnos;
						}
					}
					if( paramAnnos != null ){
						final int numberOfParameters = paramAnnos.length;
						if(allAnnotations == null){
							allAnnotations = new AnnotationInfo[numberOfParameters + 1][];
							for(int j=0, len = numberOfParameters + 1; j < len; j++){
								allAnnotations[j] = null;
							}
						}
						else{
							if( allAnnotations.length == 1 ){
								// make room for the parameter annotations
								final AnnotationInfo[][] newArray = new AnnotationInfo[numberOfParameters + 1][];
								newArray[0] = allAnnotations[0];
								allAnnotations = newArray;
								for(int j=1; j <= numberOfParameters; j++){
									allAnnotations[j] = null;
								}
							}
							// else
							// have already initialize the field to the proper size.
						}						
					
						for( int paramIndex=0; paramIndex<numberOfParameters; paramIndex++ ){
							final int numberOfAnnotations = 
								paramAnnos[paramIndex] == null ? 0 : paramAnnos[paramIndex].length;
							if( numberOfAnnotations > 0 )
							{
								final int paramAnnoIndex = paramIndex + 1;
								if( allAnnotations[paramAnnoIndex] == null )
									allAnnotations[paramAnnoIndex] = paramAnnos[paramIndex];
								else{										
									final int curlen = allAnnotations[paramAnnoIndex].length;
									final int newTotal = curlen + numberOfAnnotations;
									final AnnotationInfo[] newAnnos = new AnnotationInfo[newTotal];
									System.arraycopy(allAnnotations[paramAnnoIndex], 0, newAnnos, 0, curlen);
									System.arraycopy(paramAnnos[paramIndex], 0, newAnnos, curlen, numberOfAnnotations);
									allAnnotations[paramAnnoIndex] = newAnnos;
								}
							}							
						}
					}
			}
		}
		readOffset += (6 + methodInfo.u4At(readOffset + 2));
	}
	methodInfo.attributeBytes = readOffset;
	
	return allAnnotations;
}
	
/**
 * @param classFileBytes byte[]
 * @param offsets int[]
 * @param offset int
 */
protected MethodInfo (byte classFileBytes[], int offsets[], int offset) {
	super(classFileBytes, offset);
	constantPoolOffsets = offsets;
	accessFlags = -1;	
	this.signatureUtf8Offset = -1;
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
private static AnnotationInfo[][] decodeParamAnnotations(int offset, boolean runtimeVisible, MethodInfo methodInfo)
{		
	// u1 num_parameters;
	int numberOfParameters = methodInfo.u1At(offset + 6);
	AnnotationInfo[][] allParamAnnotations = null;	
	if( numberOfParameters > 0 ){
		// u2 attribute_name_index + u4 attribute_length + u1 num_parameters
		int readOffset = offset + 7;
		for( int i=0; i<numberOfParameters; i++ ){
			int numberOfAnnotations = methodInfo.u2At(readOffset);
			readOffset += 2;	    
			if( numberOfAnnotations > 0 ){	
				
				if(allParamAnnotations == null ){
					allParamAnnotations = new AnnotationInfo[numberOfParameters][];
					for(int j=0; j < numberOfParameters; j++)
						allParamAnnotations[j] = null;
				}
				
				final AnnotationInfo[] annos = 
					decodeAnnotations(readOffset, runtimeVisible, numberOfAnnotations, methodInfo);
				allParamAnnotations[i] = annos;
				for( int aIndex = 0; aIndex < annos.length; aIndex ++ )
					readOffset += annos[aIndex].getLength();						
			}
		}
	}
	return allParamAnnotations;
}

/**
 * @param offset begining of the 'RuntimeVisibleAnnotation' or 'RuntimeInvisibleAnnotation'
 * attribute.
 * @param runtimeVisible <code>true</code> to indicate decoding 'RuntimeVisibleAnnotation'
 */
private static AnnotationInfo[] decodeMethodAnnotations(int offset, boolean runtimeVisible, MethodInfo methodInfo){
	int numberOfAnnotations = methodInfo.u2At(offset + 6);
	AnnotationInfo[] annos = decodeAnnotations(offset + 8, runtimeVisible, numberOfAnnotations, methodInfo);
	
	if( numberOfAnnotations > 0 ){
		if( runtimeVisible ){
			int numStandardAnnotations = 0;			
			for( int i=0; i<numberOfAnnotations; i++ ){
				final long standardAnnoTagBits = annos[i].getStandardAnnotationTagBits();
				methodInfo.tagBits |= standardAnnoTagBits;
				if(standardAnnoTagBits != 0){
					annos[i] = null;
					numStandardAnnotations ++;
				}
			}
			
			if( numStandardAnnotations != 0 ){
				if( numStandardAnnotations == numberOfAnnotations )
					return null;
				
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
		return annos;
	}
	
	return null;
}

/**
 * @param offset the offset is located at the beginning of the  
 * annotation attribute.
 */
private static AnnotationInfo[] decodeAnnotations(int offset, 
												  boolean runtimeVisible,
												  int numberOfAnnotations,
												  MethodInfo methodInfo) {
	int readOffset = offset;
	AnnotationInfo[] result = null;
	if( numberOfAnnotations > 0 ){	
		result = new AnnotationInfo[numberOfAnnotations];	
	}	
	
	for (int i = 0; i < numberOfAnnotations; i++) {
		result[i] = new AnnotationInfo(methodInfo.reference, 
									   readOffset + methodInfo.structOffset, 
									   methodInfo.constantPoolOffsets, 
									   runtimeVisible, 
									   false);		
		readOffset = result[i].getLength() + readOffset;		
	}
	return result;
}

/**
 * @return the annotations or null if there is none.
 */
public IBinaryAnnotation[] getAnnotations(){ return null; }	

public IBinaryAnnotation[] getParameterAnnotations(int index){ return null; }

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
		if (CharOperation.equals(attributeName, AttributeNamesConstants.ExceptionsName)) {
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
					if (CharOperation.equals(attributeName, AttributeNamesConstants.DeprecatedName))
						this.accessFlags |= AccDeprecated;
					break;
				case 'S' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.SyntheticName))
						this.accessFlags |= AccSynthetic;
					break;
				case 'A' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.AnnotationDefaultName))
						this.accessFlags |= AccAnnotationDefault;
					break;
			}
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
}
protected void reset() {
	this.constantPoolOffsets = null;	
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

protected void toStringContent(StringBuffer buffer)
{
	int modifiers = getModifiers();
	char[] desc = getGenericSignature();
	if (desc == null)
		desc = getMethodDescriptor();
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

void toString(StringBuffer buffer)
{	
	buffer.append(this.getClass().getName());	
	toStringContent(buffer);
}

public String toString() {
	final StringBuffer buffer = new StringBuffer();
	toString(buffer);
	return buffer.toString();
}


}
