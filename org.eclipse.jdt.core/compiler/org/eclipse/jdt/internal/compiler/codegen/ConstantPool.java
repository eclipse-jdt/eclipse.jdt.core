/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
/**
 * This type is used to store all the constant pool entries.
 */
public class ConstantPool implements ClassFileConstants, TypeIds {
	public static final int DOUBLE_INITIAL_SIZE = 5;
	public static final int FLOAT_INITIAL_SIZE = 3;
	public static final int INT_INITIAL_SIZE = 248;
	public static final int LONG_INITIAL_SIZE = 5;
	public static final int UTF8_INITIAL_SIZE = 778;
	public static final int STRING_INITIAL_SIZE = 761;
	public static final int METHODS_AND_FIELDS_INITIAL_SIZE = 450;
	public static final int CLASS_INITIAL_SIZE = 86;
	public static final int NAMEANDTYPE_INITIAL_SIZE = 272;
	public static final int CONSTANTPOOL_INITIAL_SIZE = 2000;
	public static final int CONSTANTPOOL_GROW_SIZE = 6000;
	protected DoubleCache doubleCache;
	protected FloatCache floatCache;
	protected IntegerCache intCache;
	protected LongCache longCache;
	public CharArrayCache UTF8Cache;
	protected CharArrayCache stringCache;
	protected HashtableOfObject methodsAndFieldsCache;
	protected CharArrayCache classCache;
	protected HashtableOfObject nameAndTypeCacheForFieldsAndMethods;
	public byte[] poolContent;
	public int currentIndex = 1;
	public int currentOffset;

	public ClassFile classFile;

/**
 * ConstantPool constructor comment.
 */
public ConstantPool(ClassFile classFile) {
	this.UTF8Cache = new CharArrayCache(UTF8_INITIAL_SIZE);
	this.stringCache = new CharArrayCache(STRING_INITIAL_SIZE);
	this.methodsAndFieldsCache = new HashtableOfObject(METHODS_AND_FIELDS_INITIAL_SIZE);
	this.classCache = new CharArrayCache(CLASS_INITIAL_SIZE);
	this.nameAndTypeCacheForFieldsAndMethods = new HashtableOfObject(NAMEANDTYPE_INITIAL_SIZE);
	this.poolContent = classFile.header;
	this.currentOffset = classFile.headerOffset;
	// currentOffset is initialized to 0 by default
	this.currentIndex = 1;
	this.classFile = classFile;
}
/**
 * Return the content of the receiver
 */
public byte[] dumpBytes() {
	System.arraycopy(poolContent, 0, (poolContent = new byte[currentOffset]), 0, currentOffset);
	return poolContent;
}
private int getFromCache(char[] declaringClass, char[] name, char[] signature) {
	HashtableOfObject value = (HashtableOfObject) this.methodsAndFieldsCache.get(declaringClass);
	if (value == null) {
		return -1;
	}
	CharArrayCache value2 = (CharArrayCache) value.get(name);
	if (value2 == null) {
		return -1;
	}
	return value2.get(signature);
}
private int getFromNameAndTypeCache(char[] name, char[] signature) {
	CharArrayCache value = (CharArrayCache) this.nameAndTypeCacheForFieldsAndMethods.get(name);
	if (value == null) {
		return -1;
	}
	return value.get(signature);
}
public int literalIndex(byte[] utf8encoding, char[] stringCharArray) {
	int index;
	if ((index = UTF8Cache.get(stringCharArray)) < 0) {
		// The entry doesn't exit yet
		index = UTF8Cache.put(stringCharArray, currentIndex);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		currentIndex++;
		// Write the tag first
		writeU1(Utf8Tag);
		// Then the size of the stringName array
		//writeU2(utf8Constant.length);
		int savedCurrentOffset = currentOffset;
		int utf8encodingLength = utf8encoding.length;
		if (currentOffset + 2 + utf8encodingLength >= poolContent.length) {
			// we need to resize the poolContent array because we won't have
			// enough space to write the length
			resizePoolContents(2 + utf8encodingLength);
		}
		currentOffset += 2;
		// add in once the whole byte array
		System.arraycopy(utf8encoding, 0, poolContent, currentOffset, utf8encodingLength);
		currentOffset += utf8encodingLength;
		// Now we know the length that we have to write in the constant pool
		// we use savedCurrentOffset to do that
		poolContent[savedCurrentOffset] = (byte) (utf8encodingLength >> 8);
		poolContent[savedCurrentOffset + 1] = (byte) utf8encodingLength;
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param utf8Constant char[]
 * @return <CODE>int</CODE>
 */
public int literalIndex(char[] utf8Constant) {
	int index;
	if ((index = UTF8Cache.get(utf8Constant)) < 0) {
		// The entry doesn't exit yet
		// Write the tag first
		writeU1(Utf8Tag);
		// Then the size of the stringName array
		int savedCurrentOffset = currentOffset;
		if (currentOffset + 2 >= poolContent.length) {
			// we need to resize the poolContent array because we won't have
			// enough space to write the length
			resizePoolContents(2);
		}
		currentOffset += 2;
		int length = 0;
		for (int i = 0; i < utf8Constant.length; i++) {
			char current = utf8Constant[i];
			if ((current >= 0x0001) && (current <= 0x007F)) {
				// we only need one byte: ASCII table
				writeU1(current);
				length++;
			} else
				if (current > 0x07FF) {
					// we need 3 bytes
					length += 3;
					writeU1(0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
					writeU1(0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
					writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				} else {
					// we can be 0 or between 0x0080 and 0x07FF
					// In that case we only need 2 bytes
					length += 2;
					writeU1(0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
					writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				}
		}
		if (length >= 65535) {
			currentOffset = savedCurrentOffset - 1;
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceForConstant(this.classFile.referenceBinding.scope.referenceType());
		}
		index = UTF8Cache.put(utf8Constant, currentIndex);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		currentIndex++;     
		// Now we know the length that we have to write in the constant pool
		// we use savedCurrentOffset to do that
		poolContent[savedCurrentOffset] = (byte) (length >> 8);
		poolContent[savedCurrentOffset + 1] = (byte) length;
	}
	return index;
}
public int literalIndex(char[] stringCharArray, byte[] utf8encoding) {
	int index;
	int stringIndex;
	if ((index = stringCache.get(stringCharArray)) < 0) {
		// The entry doesn't exit yet
		stringIndex = literalIndex(utf8encoding, stringCharArray);
		index = stringCache.put(stringCharArray, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the tag first
		writeU1(StringTag);
		// Then the string index
		writeU2(stringIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the double
 * value. If the double is not already present into the pool, it is added. The 
 * double cache is updated and it returns the right index.
 *
 * @param key <CODE>double</CODE>
 * @return <CODE>int</CODE>
 */
public int literalIndex(double key) {
	//Retrieve the index from the cache
	// The double constant takes two indexes into the constant pool, but we only store
	// the first index into the long table
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (doubleCache == null) {
			doubleCache = new DoubleCache(DOUBLE_INITIAL_SIZE);
	}
	if ((index = doubleCache.get(key)) < 0) {
		index = doubleCache.put(key, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		currentIndex++; // a double needs an extra place into the constant pool
		// Write the double into the constant pool
		// First add the tag
		writeU1(DoubleTag);
		// Then add the 8 bytes representing the double
		long temp = java.lang.Double.doubleToLongBits(key);
		int length = poolContent.length;
		if (currentOffset + 8 >= length) {
			resizePoolContents(8);
		}
		for (int i = 0; i < 8; i++) {
			poolContent[currentOffset++] = (byte) (temp >>> (56 - (i << 3)));
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the float
 * value. If the float is not already present into the pool, it is added. The 
 * int cache is updated and it returns the right index.
 *
 * @param key <CODE>float</CODE>
 * @return <CODE>int</CODE>
 */
public int literalIndex(float key) {
	//Retrieve the index from the cache
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (floatCache == null) {
		floatCache = new FloatCache(FLOAT_INITIAL_SIZE);
	}
	if ((index = floatCache.get(key)) < 0) {
		index = floatCache.put(key, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the float constant entry into the constant pool
		// First add the tag
		writeU1(FloatTag);
		// Then add the 4 bytes representing the float
		int temp = java.lang.Float.floatToIntBits(key);
		if (currentOffset + 4 >= poolContent.length) {
			resizePoolContents(4);
		}
		for (int i = 0; i < 4; i++) {
			poolContent[currentOffset++] = (byte) (temp >>> (24 - i * 8));
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the int
 * value. If the int is not already present into the pool, it is added. The 
 * int cache is updated and it returns the right index.
 *
 * @param key <CODE>int</CODE>
 * @return <CODE>int</CODE>
 */
public int literalIndex(int key) {
	//Retrieve the index from the cache
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (intCache == null) {
		intCache = new IntegerCache(INT_INITIAL_SIZE);
	}
	if ((index = intCache.get(key)) < 0) {
		index = intCache.put(key, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the integer constant entry into the constant pool
		// First add the tag
		writeU1(IntegerTag);
		// Then add the 4 bytes representing the int
		if (currentOffset + 4 >= poolContent.length) {
			resizePoolContents(4);
		}
		for (int i = 0; i < 4; i++) {
			poolContent[currentOffset++] = (byte) (key >>> (24 - i * 8));
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the long
 * value. If the long is not already present into the pool, it is added. The 
 * long cache is updated and it returns the right index.
 *
 * @param key <CODE>long</CODE>
 * @return <CODE>int</CODE>
 */
public int literalIndex(long key) {
	// Retrieve the index from the cache
	// The long constant takes two indexes into the constant pool, but we only store
	// the first index into the long table
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (longCache == null) {
		longCache = new LongCache(LONG_INITIAL_SIZE);
	}
	if ((index = longCache.get(key)) < 0) {
		index = longCache.put(key, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		currentIndex++; // long value need an extra place into thwe constant pool
		// Write the long into the constant pool
		// First add the tag
		writeU1(LongTag);
		// Then add the 8 bytes representing the long
		if (currentOffset + 8 >= poolContent.length) {
			resizePoolContents(8);
		}
		for (int i = 0; i < 8; i++) {
			poolContent[currentOffset++] = (byte) (key >>> (56 - (i << 3)));
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param stringConstant java.lang.String
 * @return <CODE>int</CODE>
 */
public int literalIndex(String stringConstant) {
	int index;
	char[] stringCharArray = stringConstant.toCharArray();
	if ((index = stringCache.get(stringCharArray)) < 0) {
		// The entry doesn't exit yet
		int stringIndex = literalIndex(stringCharArray);
		index = stringCache.put(stringCharArray, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the tag first
		writeU1(StringTag);
		// Then the string index
		writeU2(stringIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @param aFieldBinding FieldBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(FieldBinding aFieldBinding) {
	int index;
	final char[] name = aFieldBinding.name;
	final char[] signature = aFieldBinding.type.signature();
	final char[] declaringClassConstantPoolName = aFieldBinding.declaringClass.constantPoolName();
	if ((index = getFromCache(declaringClassConstantPoolName, name, signature)) < 0) {
		// The entry doesn't exit yet
		int classIndex = literalIndexForType(declaringClassConstantPoolName);
		int nameAndTypeIndex = literalIndexForFields(literalIndex(name), literalIndex(signature), name, signature);
		index = putInCache(declaringClassConstantPoolName, name, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 * Note: uses the method binding #constantPoolDeclaringClass which could be an array type
 * for the array clone method (see UpdatedMethodDeclaration).
 * @param aMethodBinding MethodBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(MethodBinding aMethodBinding) {
	int index;
	final TypeBinding constantPoolDeclaringClass = aMethodBinding.constantPoolDeclaringClass();
	final char[] declaringClassConstantPoolName = constantPoolDeclaringClass.constantPoolName();
	final char[] selector = aMethodBinding.selector;
	final char[] signature = aMethodBinding.signature();
	if ((index = getFromCache(declaringClassConstantPoolName, selector, signature)) < 0) {
		int classIndex = literalIndexForType(constantPoolDeclaringClass.constantPoolName());
		int nameAndTypeIndex = literalIndexForMethods(literalIndex(selector), literalIndex(signature), selector, signature);
		index = putInCache(declaringClassConstantPoolName, selector, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the interface method ref constant into the constant pool
		// First add the tag
		writeU1(constantPoolDeclaringClass.isInterface() ? InterfaceMethodRefTag : MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor 
 * corresponding to a type constant pool name.
 */
public int literalIndexForType(final char[] constantPoolName) {
	int index;
	if ((index = classCache.get(constantPoolName)) < 0) {
		// The entry doesn't exit yet
		int nameIndex = literalIndex(constantPoolName);
		index = classCache.put(constantPoolName, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
public int literalIndexForMethod(char[] declaringClass, char[] selector, char[] signature, boolean isInterface) {
	int index = getFromCache(declaringClass, selector, signature);
	if (index == -1) {
		int classIndex;
		if ((classIndex = classCache.get(declaringClass)) < 0) {
			// The entry doesn't exit yet
			int nameIndex = literalIndex(declaringClass);
			classIndex = classCache.put(declaringClass, this.currentIndex++);
			if (index > 0xFFFF){
				this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
			}
			writeU1(ClassTag);
			// Then add the 8 bytes representing the long
			writeU2(nameIndex);
		}
		int nameAndTypeIndex = literalIndexForMethod(selector, signature);
		index = putInCache(declaringClass, selector, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the interface method ref constant into the constant pool
		// First add the tag
		writeU1(isInterface ? InterfaceMethodRefTag : MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);		
	}
	return index;
}
private int literalIndexForField(char[] name, char[] signature) {
	int index = getFromNameAndTypeCache(name, signature);
	if (index == -1) {
		// The entry doesn't exit yet
		int nameIndex = literalIndex(name);
		int typeIndex = literalIndex(signature);
		index = putInNameAndTypeCache(name, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		writeU1(NameAndTypeTag);
		writeU2(nameIndex);
		writeU2(typeIndex);
	}
	return index;
}
private int literalIndexForMethod(char[] selector, char[] signature) {
	int index = getFromNameAndTypeCache(selector, signature);
	if (index == -1) {
		// The entry doesn't exit yet
		int nameIndex = literalIndex(selector);
		int typeIndex = literalIndex(signature);
		index = putInNameAndTypeCache(selector, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		writeU1(NameAndTypeTag);
		writeU2(nameIndex);
		writeU2(typeIndex);
	}
	return index;
}
public int literalIndexForField(char[] declaringClass, char[] name, char[] signature) {
	int index = getFromCache(declaringClass, name, signature);
	if (index == -1) {
		int classIndex;
		if ((classIndex = classCache.get(declaringClass)) < 0) {
			// The entry doesn't exit yet
			int nameIndex = literalIndex(declaringClass);
			classIndex = classCache.put(declaringClass, this.currentIndex++);
			if (index > 0xFFFF){
				this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
			}
			writeU1(ClassTag);
			// Then add the 8 bytes representing the long
			writeU2(nameIndex);
		}
		int nameAndTypeIndex = literalIndexForField(name, signature);
		index = putInCache(declaringClass, name, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the interface method ref constant into the constant pool
		// First add the tag
		writeU1(FieldRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);		
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding 
 * nameAndType constant with nameIndex, typeIndex.
 *
 * @param nameIndex the given name index
 * @param typeIndex the given type index
 * @param name the given field name
 * @param signature the given field signature
 * @return the index into the constantPool corresponding 
 * nameAndType constant with nameIndex, typeInde
 */
private int literalIndexForFields(int nameIndex, int typeIndex, char[] name, char[] signature) {
	int index;
	if ((index = getFromNameAndTypeCache(name, signature)) == -1) {
		// The entry doesn't exit yet
		index = putInNameAndTypeCache(name, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		writeU1(NameAndTypeTag);
		writeU2(nameIndex);
		writeU2(typeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param stringCharArray char[]
 * @return <CODE>int</CODE>
 */
public int literalIndexForLdc(char[] stringCharArray) {
	int index;
	if ((index = stringCache.get(stringCharArray)) < 0) {
		int stringIndex;
		// The entry doesn't exit yet
		if ((stringIndex = UTF8Cache.get(stringCharArray)) < 0) {
			// The entry doesn't exit yet
			// Write the tag first
			writeU1(Utf8Tag);
			// Then the size of the stringName array
			int savedCurrentOffset = currentOffset;
			if (currentOffset + 2 >= poolContent.length) {
				// we need to resize the poolContent array because we won't have
				// enough space to write the length
				resizePoolContents(2);
			}
			currentOffset += 2;
			int length = 0;
			for (int i = 0; i < stringCharArray.length; i++) {
				char current = stringCharArray[i];
				if ((current >= 0x0001) && (current <= 0x007F)) {
					// we only need one byte: ASCII table
					writeU1(current);
					length++;
				} else
					if (current > 0x07FF) {
						// we need 3 bytes
						length += 3;
						writeU1(0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
						writeU1(0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
						writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					} else {
						// we can be 0 or between 0x0080 and 0x07FF
						// In that case we only need 2 bytes
						length += 2;
						writeU1(0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
						writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					}
			}
			if (length >= 65535) {
				currentOffset = savedCurrentOffset - 1;
				return -1;
			}
			stringIndex = UTF8Cache.put(stringCharArray, currentIndex++);
			// Now we know the length that we have to write in the constant pool
			// we use savedCurrentOffset to do that
			if (length > 65535) {
				return 0;
			}
			poolContent[savedCurrentOffset] = (byte) (length >> 8);
			poolContent[savedCurrentOffset + 1] = (byte) length;
		}
		index = stringCache.put(stringCharArray, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		// Write the tag first
		writeU1(StringTag);
		// Then the string index
		writeU2(stringIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding 
 * nameAndType constant with nameIndex, typeIndex.
 *
 * @param nameIndex the given name index
 * @param typeIndex the given type index
 * @param selector the given method selector
 * @param signature the given method signature
 * @return <CODE>int</CODE>
 */
public int literalIndexForMethods(int nameIndex, int typeIndex, char[] selector, char[] signature) {
	int index;
	if ((index = getFromNameAndTypeCache(selector, signature)) == -1) {
		// The entry doesn't exit yet
		index = putInNameAndTypeCache(selector, signature, currentIndex++);
		if (index > 0xFFFF){
			this.classFile.referenceBinding.scope.problemReporter().noMoreAvailableSpaceInConstantPool(this.classFile.referenceBinding.scope.referenceType());
		}
		writeU1(NameAndTypeTag);
		writeU2(nameIndex);
		writeU2(typeIndex);
	}
	return index;
}
private int putInNameAndTypeCache(final char[] key1, final char[] key2, int index) {
	CharArrayCache value = (CharArrayCache) this.nameAndTypeCacheForFieldsAndMethods.get(key1);
	if (value == null) {
		CharArrayCache charArrayCache = new CharArrayCache();
		charArrayCache.put(key2, index);
		this.nameAndTypeCacheForFieldsAndMethods.put(key1, charArrayCache);
	} else {
		value.put(key2, index);
	}
	return index;
}
/**
 * @param key1
 * @param key2
 * @param key3
 * @param index
 * @return the given index
 */
private int putInCache(final char[] key1, final char[] key2, final char[] key3, int index) {
	HashtableOfObject value = (HashtableOfObject) this.methodsAndFieldsCache.get(key1);
	if (value == null) {
		value = new HashtableOfObject();
		this.methodsAndFieldsCache.put(key1, value);
		CharArrayCache charArrayCache = new CharArrayCache();
		charArrayCache.put(key3, index);
		value.put(key2, charArrayCache);
	} else {
		CharArrayCache charArrayCache = (CharArrayCache) value.get(key2);
		if (charArrayCache == null) {
			charArrayCache = new CharArrayCache();
			charArrayCache.put(key3, index);
			value.put(key2, charArrayCache);
		} else {
			charArrayCache.put(key3, index);			
		}
	}
	return index;
}
/**
 * This method is used to clean the receiver in case of a clinit header is generated, but the 
 * clinit has no code.
 * This implementation assumes that the clinit is the first method to be generated.
 * @see org.eclipse.jdt.internal.compiler.ast.TypeDeclaration#addClinit()
 */
public void resetForClinit(int constantPoolIndex, int constantPoolOffset) {
	currentIndex = constantPoolIndex;
	currentOffset = constantPoolOffset;
	if (UTF8Cache.get(AttributeNamesConstants.CodeName) >= constantPoolIndex) {
		UTF8Cache.remove(AttributeNamesConstants.CodeName);
	}
	if (UTF8Cache.get(QualifiedNamesConstants.ClinitSignature) >= constantPoolIndex) {
		UTF8Cache.remove(QualifiedNamesConstants.ClinitSignature);
	}
	if (UTF8Cache.get(QualifiedNamesConstants.Clinit) >= constantPoolIndex) {
		UTF8Cache.remove(QualifiedNamesConstants.Clinit);
	}
}

/**
 * Resize the pool contents
 */
private final void resizePoolContents(int minimalSize) {
	int length = poolContent.length;
	int toAdd = length;
	if (toAdd < minimalSize)
		toAdd = minimalSize;
	System.arraycopy(poolContent, 0, poolContent = new byte[length + toAdd], 0, length);
}
/**
 * Write a unsigned byte into the byte array
 * 
 * @param value <CODE>int</CODE> The value to write into the byte array
 */
protected final void writeU1(int value) {
	if (currentOffset + 1 >= poolContent.length) {
		resizePoolContents(1);
	}
	poolContent[currentOffset++] = (byte) value;
}
/**
 * Write a unsigned byte into the byte array
 * 
 * @param value <CODE>int</CODE> The value to write into the byte array
 */
protected final void writeU2(int value) {
	if (currentOffset + 2 >= poolContent.length) {
		resizePoolContents(2);
	}
	//first byte
	poolContent[currentOffset++] = (byte) (value >> 8);
	poolContent[currentOffset++] = (byte) value;
}
}
