package org.eclipse.jdt.internal.compiler.classfmt;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.NullConstant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.*;

import java.io.*;
import java.util.Arrays;

public class ClassFileReader extends ClassFileStruct implements AttributeNamesConstants, IBinaryType {
	private int constantPoolCount;
	private int[] constantPoolOffsets;
	private int accessFlags;
	private char[] className;
	private char[] superclassName;
	private int interfacesCount;
	private char[][] interfaceNames;
	private int fieldsCount;
	private FieldInfo[] fields;
	private int methodsCount;
	private MethodInfo[] methods;
	private InnerClassInfo[] innerInfos;
	private char[] sourceFileName;
	// initialized in case the .class file is a nested type
	private InnerClassInfo innerInfo;
	private char[] classFileName;
	private int classNameIndex;
	private int innerInfoIndex;
/**
 * @param classFileBytes actual bytes of a .class file
 * @param fileName actual name of the file that contains the bytes, can be null
 */
public ClassFileReader(byte classFileBytes[], char[] fileName) throws ClassFormatException {
	// This method looks ugly but is actually quite simple, the constantPool is constructed
	// in 3 passes.  All non-primitive constant pool members that usually refer to other members
	// by index are tweaked to have their value in inst vars, this minor cost at read-time makes
	// all subsequent uses of the constant pool element faster.
	super(classFileBytes, 0);
	classFileName = fileName;
	int readOffset = 10;
	try {
		constantPoolCount = this.u2At(8);
		// Pass #1 - Fill in all primitive constants
		constantPoolOffsets = new int[constantPoolCount];
		for (int i = 1; i < constantPoolCount; i++) {
			int tag = this.u1At(readOffset);
			switch (tag) {
				case Utf8Tag :
					constantPoolOffsets[i] = readOffset;
					readOffset += u2At(readOffset + 1);
					readOffset += ConstantUtf8FixedSize;
					break;
				case IntegerTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantIntegerFixedSize;
					break;
				case FloatTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantFloatFixedSize;
					break;
				case LongTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantLongFixedSize;
					i++;
					break;
				case DoubleTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantDoubleFixedSize;
					i++;
					break;
				case ClassTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantClassFixedSize;
					break;
				case StringTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantStringFixedSize;
					break;
				case FieldRefTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantFieldRefFixedSize;
					break;
				case MethodRefTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantMethodRefFixedSize;
					break;
				case InterfaceMethodRefTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantInterfaceMethodRefFixedSize;
					break;
				case NameAndTypeTag :
					constantPoolOffsets[i] = readOffset;
					readOffset += ConstantNameAndTypeFixedSize;
			}
		}
		// Read and validate access flags
		accessFlags = u2At(readOffset);
		readOffset += 2;

		// Read the classname, use exception handlers to catch bad format
		classNameIndex = u2At(readOffset);
		className = getConstantClassNameAt(classNameIndex);
		readOffset += 2;

		// Read the superclass name, can be null for java.lang.Object
		int superclassNameIndex = u2At(readOffset);
		readOffset += 2;
		// if superclassNameIndex is equals to 0 there is no need to set a value for the 
		// field superclassName. null is fine.
		if (superclassNameIndex != 0) {
			superclassName = getConstantClassNameAt(superclassNameIndex);
		}

		// Read the interfaces, use exception handlers to catch bad format
		interfacesCount = u2At(readOffset);
		readOffset += 2;
		if (interfacesCount != 0) {
			interfaceNames = new char[interfacesCount][];
			for (int i = 0; i < interfacesCount; i++) {
				interfaceNames[i] = getConstantClassNameAt(u2At(readOffset));
				readOffset += 2;
			}
		}
		// Read the fields, use exception handlers to catch bad format
		fieldsCount = u2At(readOffset);
		readOffset += 2;
		if (fieldsCount != 0) {
			FieldInfo field;
			fields = new FieldInfo[fieldsCount];
			for (int i = 0; i < fieldsCount; i++) {
				field = new FieldInfo(reference, constantPoolOffsets, readOffset);
				fields[i] = field;
				readOffset += field.sizeInBytes();
			}
		}
		// Read the methods
		methodsCount = u2At(readOffset);
		readOffset += 2;
		if (methodsCount != 0) {
			methods = new MethodInfo[methodsCount];
			MethodInfo method;
			for (int i = 0; i < methodsCount; i++) {
				method = new MethodInfo(reference, constantPoolOffsets, readOffset);
				methods[i] = method;
				readOffset += method.sizeInBytes();
			}
		}

		// Read the attributes
		int attributesCount = u2At(readOffset);
		readOffset += 2;

		for (int i = 0; i < attributesCount; i++) {
			int utf8Offset = constantPoolOffsets[u2At(readOffset)];
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (CharOperation.equals(attributeName, DeprecatedName)) {
				accessFlags |= AccDeprecated;
			} else {
				if (CharOperation.equals(attributeName, InnerClassName)) {
					int innerOffset = readOffset + 6;
					int number_of_classes = u2At(innerOffset);
					if (number_of_classes != 0) {
						innerInfos = new InnerClassInfo[number_of_classes];
						for (int j = 0; j < number_of_classes; j++) {
							innerInfos[j] = 
								new InnerClassInfo(reference, constantPoolOffsets, innerOffset + 2); 
							if (classNameIndex == innerInfos[j].innerClassNameIndex) {
								innerInfo = innerInfos[j];
								innerInfoIndex = j;
							}
							innerOffset += 8;
						}
					}
				} else {
					if (CharOperation.equals(attributeName, SourceName)) {
						utf8Offset = constantPoolOffsets[u2At(readOffset + 6)];
						sourceFileName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
					} else {
						if (CharOperation.equals(attributeName, SyntheticName)) {
							accessFlags |= AccSynthetic;
						}
					}
				}
			}
			readOffset += (6 + u4At(readOffset + 2));
		}
	} catch (Exception e) {
		throw new ClassFormatException(
			ClassFormatException.ErrTruncatedInput, 
			readOffset); 
	}
}
/**
 * 	Answer the receiver's access flags.  The value of the access_flags
 *	item is a mask of modifiers used with class and interface declarations.
 *  @return int 
 */
public int accessFlags() {
	return accessFlags;
}
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 *
 * Answer the char array that corresponds to the class name of the constant class.
 * constantPoolIndex is the index in the constant pool that is a constant class entry.
 *
 * @param int constantPoolIndex
 * @return char[]
 */
private char[] getConstantClassNameAt(int constantPoolIndex) {
	int utf8Offset = constantPoolOffsets[u2At(constantPoolOffsets[constantPoolIndex] + 1)];
	return utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
}
/**
 * Answer the int array that corresponds to all the offsets of each entry in the constant pool
 *
 * @return int[]
 */
public int[] getConstantPoolOffsets() {
	return constantPoolOffsets;
}
/*
 * Answer the resolved compoundName of the enclosing type
 * or null if the receiver is a top level type.
 */
public char[] getEnclosingTypeName() {
	if (innerInfo != null) {
		return innerInfo.getEnclosingTypeName();
	}
	return null;
}
/**
 * Answer the receiver's fields or null if the array is empty.
 * @return org.eclipse.jdt.internal.compiler.api.IBinaryField[]
 */
public IBinaryField[] getFields() {
	return fields;
}
/**
 * Answer the file name which defines the type.
 * The format is unspecified.
 */
public char[] getFileName() {
	return classFileName;
}
/**
 * Answer the source name if the receiver is a inner type. Return null if it is an anonymous class or if the receiver is a top-level class.
 * e.g.
 * public class A {
 *	public class B {
 *	}
 *	public void foo() {
 *		class C {}
 *	}
 *	public Runnable bar() {
 *		return new Runnable() {
 *			public void run() {}
 *		};
 *	}
 * }
 * It returns {'B'} for the member A$B
 * It returns null for A
 * It returns {'C'} for the local class A$1$C
 * It returns null for the anonymous A$1
 * @return char[]
 */
public char[] getInnerSourceName() {
	if (innerInfo != null)
		return innerInfo.getSourceName();
	return null;
}
/**
 * Answer the resolved names of the receiver's interfaces in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[][]
 */
public char[][] getInterfaceNames() {
	return interfaceNames;
}
/**
 * Answer the receiver's nested types or null if the array is empty.
 *
 * This nested type info is extracted from the inner class attributes.
 * Ask the name environment to find a member type using its compound name
 * @return org.eclipse.jdt.internal.compiler.api.IBinaryNestedType[]
 */
public IBinaryNestedType[] getMemberTypes() {
	// we might have some member types of the current type
	if (innerInfos == null) return null;

	int length = innerInfos.length;
	int startingIndex = innerInfo != null ? innerInfoIndex + 1 : 0;
	if (length != startingIndex) {
		IBinaryNestedType[] memberTypes = 
			new IBinaryNestedType[length - innerInfoIndex]; 
		int memberTypeIndex = 0;
		for (int i = startingIndex; i < length; i++) {
			InnerClassInfo currentInnerInfo = innerInfos[i];
			int outerClassNameIdx = currentInnerInfo.outerClassNameIndex;
			if (outerClassNameIdx != 0 && outerClassNameIdx == classNameIndex) {
				memberTypes[memberTypeIndex++] = currentInnerInfo;
			}
		}
		if (memberTypeIndex == 0) return null;
		if (memberTypeIndex != memberTypes.length) {
			// we need to resize the memberTypes array. Some local or anonymous classes
			// are present in the current class.
			System.arraycopy(
				memberTypes, 
				0, 
				(memberTypes = new IBinaryNestedType[memberTypeIndex]), 
				0, 
				memberTypeIndex); 
		}
		return memberTypes;
	}
	return null;
}
/**
 * Answer the receiver's methods or null if the array is empty.
 * @return org.eclipse.jdt.internal.compiler.api.env.IBinaryMethod[]
 */
public IBinaryMethod[] getMethods() {
	return methods;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Set the AccDeprecated and AccSynthetic bits if necessary
 * @return int
 */
public int getModifiers() {
	if (innerInfo != null) {
		return innerInfo.getModifiers();
	}
	return accessFlags;
}
/**
 * Answer the resolved name of the type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[]
 */
public char[] getName() {
	return className;
}
/**
 * Answer the resolved name of the receiver's superclass in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if it does not have one.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[]
 */
public char[] getSuperclassName() {
	return superclassName;
}
/**
 * Answer true if the receiver is an anonymous type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isAnonymous() {
	return innerInfo != null && innerInfo.getEnclosingTypeName() == null && innerInfo.getSourceName() == null;
}
/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 * @return boolean
 */
public boolean isBinaryType() {
	return true;
}
/**
 * Answer true if the receiver is a class. False otherwise.
 * @return boolean
 */
public boolean isClass() {
	return (getModifiers() & AccInterface) == 0;
}
/**
 * Answer true if the receiver is an interface. False otherwise.
 * @return boolean
 */
public boolean isInterface() {
	return (getModifiers() & AccInterface) != 0;
}
/**
 * Answer true if the receiver is a local type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isLocal() {
	return innerInfo != null && innerInfo.getEnclosingTypeName() == null && innerInfo.getSourceName() != null;
}
/**
 * Answer true if the receiver is a member type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isMember() {
	return innerInfo != null && innerInfo.getEnclosingTypeName() != null;
}
/**
 * Answer true if the receiver is a nested type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isNestedType() {
	return innerInfo != null;
}
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 *
 * @param file The file you want to read
 * @return org.eclipse.jdt.internal.compiler.classfmt.DietClassFile
 */
public static ClassFileReader read(java.io.File file) throws ClassFormatException, java.io.IOException {
	int fileLength;
	byte classFileBytes[] = new byte[fileLength = (int) file.length()];
	java.io.FileInputStream stream = new java.io.FileInputStream(file);
	int bytesRead = 0;
	int lastReadSize = 0;
	while ((lastReadSize != -1) && (bytesRead != fileLength)) {
		lastReadSize = stream.read(classFileBytes, bytesRead, fileLength - bytesRead);
		bytesRead += lastReadSize;
	}
	ClassFileReader classFile = new ClassFileReader(classFileBytes, file.getAbsolutePath().toCharArray());
	stream.close();
	return classFile;
}
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 *
 * @param String fileName
 */
public static ClassFileReader read(String fileName) throws ClassFormatException, java.io.IOException {
	int fileLength;
	File file = new File(fileName);
	byte classFileBytes[] = new byte[fileLength = (int) file.length()];
	java.io.FileInputStream stream = new java.io.FileInputStream(file);
	int bytesRead = 0;
	int lastReadSize = 0;
	while ((lastReadSize != -1) && (bytesRead != fileLength)) {
		lastReadSize = stream.read(classFileBytes, bytesRead, fileLength - bytesRead);
		bytesRead += lastReadSize;
	}
	ClassFileReader classFile = new ClassFileReader(classFileBytes, fileName.toCharArray());
	stream.close();
	return classFile;
}
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 *
 * @param java.util.zip.ZipFile zip
 * @param java.lang.String filename
 * @return org.eclipse.jdt.internal.compiler.classfmt.DietClassFile
 */
public static ClassFileReader read(
	java.util.zip.ZipFile zip, 
	String filename)
	throws ClassFormatException, java.io.IOException {
	java.util.zip.ZipEntry ze = zip.getEntry(filename);
	if (ze == null)
		return null;
	java.io.InputStream zipInputStream = zip.getInputStream(ze);
	byte classFileBytes[] = new byte[(int) ze.getSize()];
	int length = classFileBytes.length;
	int len = 0;
	int readSize = 0;
	while ((readSize != -1) && (len != length)) {
		readSize = zipInputStream.read(classFileBytes, len, length - len);
		len += readSize;
	}
	zipInputStream.close();
	return new ClassFileReader(classFileBytes, filename.toCharArray());
}
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 *
 * Answer the source file name attribute. Return null if there is no source file attribute for the receiver.
 * 
 * @return char[]
 */
public char[] sourceFileName() {
	return sourceFileName;
}
/**
 * (c)1998 Object Technology International.
 * (c)1998 International Business Machines Corporation.
 * 
 * 
 */
public String toString() {
	java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
	java.io.PrintWriter print = new java.io.PrintWriter(out);
	
	print.println(this.getClass().getName() + "{"); //$NON-NLS-1$
	print.println(" className: " + new String(getName())); //$NON-NLS-1$
	print.println(" superclassName: " + (getSuperclassName() == null ? "null" : new String(getSuperclassName()))); //$NON-NLS-2$ //$NON-NLS-1$
	print.println(" access_flags: " + ClassFileStruct.printTypeModifiers(accessFlags()) + "(" + accessFlags() + ")"); //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$

	print.flush();
	return out.toString();
}
/**
 * Check if the receiver has structural changes compare to the byte array in argument.
 * Structural changes are:
 * - modifiers changes for the class, the fields or the methods
 * - signature changes for fields or methods.
 * - changes in the number of fields or methods
 * - changes for field constants
 * - changes for thrown exceptions
 * - change for the super class or any super interfaces.
 * - changes for member types name or modifiers
 * If any of these changes occurs, the method returns true. false otherwise.
 */
public boolean hasStructuralChanges(byte[] newBytes) {
	try {
		ClassFileReader newClassFile =
			new ClassFileReader(newBytes, this.classFileName);
		// type level comparison
		// modifiers
		if (this.getModifiers() != newClassFile.getModifiers()) {
			return true;
		}
		// superclass
		if (!CharOperation.equals(this.getSuperclassName(), newClassFile.getSuperclassName())) {
			return true;
		}
		// interfaces
		char[][] newInterfacesNames = newClassFile.getInterfaceNames();
		int newInterfacesLength = newInterfacesNames == null ? 0 : newInterfacesNames.length;
		if (newInterfacesLength != this.interfacesCount) {
			return true;
		}
		if (this.interfacesCount != 0) {
			for (int i = 0, max = this.interfacesCount; i < max; i++) {
				if (!CharOperation.equals(this.interfaceNames[i], newInterfacesNames[i])) {
					return true;
				}
			}
		}
		// fields
		FieldInfo[] otherFieldInfos = (FieldInfo[]) newClassFile.getFields();
		int otherFieldInfosLength = otherFieldInfos == null ? 0 : otherFieldInfos.length;
		if (this.fieldsCount != otherFieldInfosLength) {
			return true;
		}
		if (otherFieldInfosLength != 0) {
//			Arrays.sort(this.fields);
//			Arrays.sort(otherFieldInfos);
			for (int i = 0; i < otherFieldInfosLength; i++) {
				FieldInfo currentFieldInfo = this.fields[i];
				FieldInfo otherFieldInfo = otherFieldInfos[i];
				if (currentFieldInfo.getModifiers() != otherFieldInfo.getModifiers()) {
					return true;
				}
				if (!CharOperation.equals(currentFieldInfo.getName(), otherFieldInfo.getName())) {
					return true;
				}
				if (currentFieldInfo.hasConstant()) {
					if (!otherFieldInfo.hasConstant()) {
						return true;
					}
					Constant currentConstant = currentFieldInfo.getConstant();
					Constant otherConstant = otherFieldInfo.getConstant();
					if (!currentConstant.getClass().equals(otherConstant.getClass())) {
						return true;
					} 
					switch (currentConstant.typeID()) {
							case TypeIds.T_int : 
								if (otherConstant.typeID() != TypeIds.T_int) {
									return true;
								}
								if (otherConstant.intValue() != currentConstant.intValue()) {
									return true;
								}
								break;
							case TypeIds.T_byte :
								if (otherConstant.typeID() != TypeIds.T_byte) {
									return true;
								}
								if (otherConstant.byteValue() != currentConstant.byteValue()) {
									return true;
								}
								break;
							case TypeIds.T_short : 
								if (otherConstant.typeID() != TypeIds.T_short) {
									return true;
								}
								if (otherConstant.shortValue() != currentConstant.shortValue()) {
									return true;
								}
								break;
							case TypeIds.T_char : 
								if (otherConstant.typeID() != TypeIds.T_char) {
									return true;
								}
								if (otherConstant.charValue() != currentConstant.charValue()) {
									return true;
								}
								break;
							case TypeIds.T_float :
								if (otherConstant.typeID() != TypeIds.T_float) {
									return true;
								}
								if (otherConstant.floatValue() != currentConstant.floatValue()) {
									return true;
								}
								break;
							case TypeIds.T_double :
								if (otherConstant.typeID() != TypeIds.T_double) {
									return true;
								}
								if (otherConstant.doubleValue() != currentConstant.doubleValue()) {
									return true;
								}
								break;
							case TypeIds.T_boolean : 
								if (otherConstant.typeID() != TypeIds.T_boolean) {
									return true;
								}
								if (otherConstant.booleanValue() != currentConstant.booleanValue()) {
									return true;
								}
								break;
							case TypeIds.T_String : 
								if (otherConstant.typeID() != TypeIds.T_String) {
									return true;
								}
								if (otherConstant.stringValue() != currentConstant.stringValue()) {
									return true;
								}
								break;
							case TypeIds.T_null :
								if (otherConstant.typeID() != TypeIds.T_null) {
									return true;
								}
								if (otherConstant != NullConstant.Default) {
									return true;
								}
					}
				} else if (otherFieldInfo.hasConstant()) {
					return true;
				}
			}
		}
		// methods
		MethodInfo[] otherMethodInfos = (MethodInfo[]) newClassFile.getMethods();
		int otherMethodInfosLength = otherMethodInfos == null ? 0 : otherMethodInfos.length;
		if (this.methodsCount != otherMethodInfosLength) {
			return true;
		}
		if (otherMethodInfosLength != 0) {
//			Arrays.sort(this.methods);
//			Arrays.sort(otherMethodInfos);
			for (int i = 0; i < otherMethodInfosLength; i++) {
				MethodInfo otherMethodInfo = otherMethodInfos[i];
				MethodInfo currentMethodInfo = this.methods[i];
				if (otherMethodInfo.getModifiers() != currentMethodInfo.getModifiers()) {
					return true;
				}				
				if (!CharOperation.equals(otherMethodInfo.getSelector(), currentMethodInfo.getSelector())) {
					return true;
				}
				if (!CharOperation.equals(otherMethodInfo.getMethodDescriptor(), currentMethodInfo.getMethodDescriptor())) {
					return true;
				}
				char[][] otherThrownExceptions = otherMethodInfo.getExceptionTypeNames();
				int otherThrownExceptionsLength = otherThrownExceptions == null ? 0 : otherThrownExceptions.length;
				char[][] currentThrownExceptions = currentMethodInfo.getExceptionTypeNames();
				int currentThrownExceptionsLength = currentThrownExceptions == null ? 0 : currentThrownExceptions.length;
				if (currentThrownExceptionsLength != otherThrownExceptionsLength) {
					return true;
				}
				if (currentThrownExceptionsLength != 0) {
					for (int k = 0; k < currentThrownExceptionsLength; k++) {
						if (!CharOperation.equals(currentThrownExceptions[k], otherThrownExceptions[k])) {
							return true;
						}
					}
				}
			}
		}
		// Member types
		InnerClassInfo[] currentMemberTypes = (InnerClassInfo[]) this.getMemberTypes();
		InnerClassInfo[] otherMemberTypes = (InnerClassInfo[]) newClassFile.getMemberTypes();
		int currentMemberTypeLength = currentMemberTypes == null ? 0 : currentMemberTypes.length;
		int otherMemberTypeLength = otherMemberTypes == null ? 0 : otherMemberTypes.length;
		if (currentMemberTypeLength != otherMemberTypeLength) {
			return true;
		}
		if (currentMemberTypeLength != 0) {
			for (int i = 0; i < currentMemberTypeLength; i++) {
				if (!CharOperation.equals(currentMemberTypes[i].getName(), otherMemberTypes[i].getName())
					|| currentMemberTypes[i].getModifiers() != otherMemberTypes[i].getModifiers()) {
						return true;
				}
			}			
		}
		return false;
	} catch (ClassFormatException e) {
		return true;
	}
}
}
