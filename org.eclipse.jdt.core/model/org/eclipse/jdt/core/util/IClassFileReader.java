/**********************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *********************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a .class file. This class reifies the internal structure of a .class
 * file following the JVM specifications.
 *  
 * @since 2.0
 */
public interface IClassFileReader {
	/**
	 * This value should be used to read completely each part of a .class file.
	 */
	int ALL 					= 0xFFFF;
	
	/**
	 * This value should be used to read only the constant pool entries of a .class file.
	 */
	int CONSTANT_POOL 			= 0x0001;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the method infos of a .class file.
	 */
	int METHOD_INFOS 			= 0x0003;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the field infos of a .class file.
	 */
	int FIELD_INFOS 			= 0x0005;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the super interface names of a .class file.
	 */
	int SUPER_INTERFACES 		= 0x0009;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the attributes of a .class file.
	 */
	int CLASSFILE_ATTRIBUTES 	= 0x0011;

	/**
	 * Answer back the access flag of the .class file.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAccessFlags();
	
	/**
	 * Answer back the array of field infos of the .class file, 
	 * an empty array if none.
	 * 
	 * @return org.eclipse.jdt.core.util.IIFieldInfo
	 */
	IFieldInfo[] getFieldInfos();

	/**
	 * Answer back the names of interfaces implemented by this .class file,
	 * an empty array if none. The names are returned as described in the 
	 * JVM specifications.
	 * 
	 * @return char[][]
	 */
	char[][] getInterfaceNames();

	/**
	 * Answer back the indexes in the constant pool of interfaces implemented 
	 * by this .class file, an empty array if none.
	 * 
	 * @return int[]
	 */
	int[] getInterfaceIndexes();

	/**
	 * Answer back the inner classes attribute of this .class file, null if none.
	 * 
	 * @return org.eclipse.jdt.core.util.IInnerClassesAttribute
	 */
	IInnerClassesAttribute getInnerClassesAttribute();

	/**
	 * Answer back the array of method infos of this .class file,
	 * an empty array if none.
	 * 
	 * @return org.eclipse.jdt.core.util.IMethodInfo[]
	 */
	IMethodInfo[] getMethodInfos();

	/**
	 * Answer back the qualified name of the .class file.
	 * The name is returned as described in the JVM specifications.
	 *  
	 * @return char[]
	 */
	char[] getClassName();

	/**
	 * Answer back the index of the class name in the constant pool 
	 * of the .class file.
	 *  
	 * @return <CODE>int</CODE>
	 */
	int getClassIndex();
		
	/**
	 * Answer back the qualified name of the superclass of this .class file.
	 * The name is returned as described in the JVM specifications. Answer null if 
	 * getSuperclassIndex() is zero.
	 * 
	 * @return char[]
	 */
	char[] getSuperclassName();

	/**
	 * Answer back the index of the superclass name in the constant pool 
	 * of the .class file. Answer 0  is this .class file represents java.lang.Object.
	 *  
	 * @return <CODE>int</CODE>
	 */
	int getSuperclassIndex();

	/**
	 * Answer true if this .class file represents an class, false otherwise.
	 * 
	 * @return <CODE>boolean</CODE>
	 */
	boolean isClass();

	/**
	 * Answer true if this .class file represents an interface, false otherwise.
	 * 
	 * @return <CODE>boolean</CODE>
	 */
	boolean isInterface();

	/**
	 * Answer the source file attribute, if it exists, null otherwise.
	 * 
	 * @return ISourceAttribute
	 */
	ISourceAttribute getSourceFileAttribute();

	/**
	 * Answer the constant pool of this .class file.
	 * 
	 * @return org.eclipse.jdt.core.util.IConstantPool
	 */
	IConstantPool getConstantPool();
	
	/**
	 * Answer the minor version of this .class file.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getMinorVersion();

	/**
	 * Answer the major version of this .class file.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getMajorVersion();

	/**
	 * Answer back the attribute number of the .class file.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAttributeCount();
	
	/**
	 * Answer back the collection of all attributes of the field info. It 
	 * includes SyntheticAttribute, ConstantValueAttributes, etc. Answers an empty
	 * array if none.
	 * 
	 * @return IClassFileAttribute[]
	 */
	IClassFileAttribute[] getAttributes();
	
	/**
	 * Answer back the magic number.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getMagic();
	
	/**
	 * Answer back the number of field infos.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getFieldsCount();

	/**
	 * Answer back the number of method infos.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getMethodsCount();
}