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

import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

/* TODO (olivier) constants should be moved to ConstantPool */
public interface QualifiedNamesConstants {
	char[] JavaLangObjectConstantPoolName = "java/lang/Object".toCharArray(); //$NON-NLS-1$
	char[] JavaLangStringConstantPoolName = "java/lang/String".toCharArray(); //$NON-NLS-1$
	char[] JavaLangStringBufferConstantPoolName = "java/lang/StringBuffer".toCharArray(); //$NON-NLS-1$
	char[] JavaLangClassConstantPoolName = "java/lang/Class".toCharArray(); //$NON-NLS-1$
	char[] JavaLangThrowableConstantPoolName = "java/lang/Throwable".toCharArray(); //$NON-NLS-1$
	char[] JavaLangClassNotFoundExceptionConstantPoolName = "java/lang/ClassNotFoundException".toCharArray(); //$NON-NLS-1$
	char[] JavaLangNoClassDefFoundErrorConstantPoolName = "java/lang/NoClassDefFoundError".toCharArray(); //$NON-NLS-1$
	char[] JavaLangIntegerConstantPoolName = "java/lang/Integer".toCharArray(); //$NON-NLS-1$
	char[] JavaLangFloatConstantPoolName = "java/lang/Float".toCharArray(); //$NON-NLS-1$
	char[] JavaLangDoubleConstantPoolName = "java/lang/Double".toCharArray(); //$NON-NLS-1$
	char[] JavaLangLongConstantPoolName = "java/lang/Long".toCharArray(); //$NON-NLS-1$
	char[] JavaLangShortConstantPoolName = "java/lang/Short".toCharArray(); //$NON-NLS-1$
	char[] JavaLangByteConstantPoolName = "java/lang/Byte".toCharArray(); //$NON-NLS-1$
	char[] JavaLangCharacterConstantPoolName = "java/lang/Character".toCharArray(); //$NON-NLS-1$
	char[] JavaLangVoidConstantPoolName = "java/lang/Void".toCharArray(); //$NON-NLS-1$
	char[] JavaLangBooleanConstantPoolName = "java/lang/Boolean".toCharArray(); //$NON-NLS-1$
	char[] JavaLangSystemConstantPoolName = "java/lang/System".toCharArray(); //$NON-NLS-1$
	char[] JavaLangErrorConstantPoolName = "java/lang/Error".toCharArray(); //$NON-NLS-1$
	char[] JavaLangExceptionConstantPoolName = "java/lang/Exception".toCharArray(); //$NON-NLS-1$
	char[] JavaLangReflectConstructor = "java/lang/reflect/Constructor".toCharArray();   //$NON-NLS-1$
	char[] JavaUtilIteratorConstantPoolName = "java/util/Iterator".toCharArray(); //$NON-NLS-1$
	char[] JavaLangStringBuilderConstantPoolName = "java/lang/StringBuilder".toCharArray(); //$NON-NLS-1$
	char[] Append = "append".toCharArray(); //$NON-NLS-1$
	char[] ToString = "toString".toCharArray(); //$NON-NLS-1$
	char[] Init = "<init>".toCharArray(); //$NON-NLS-1$
	char[] Clinit = "<clinit>".toCharArray(); //$NON-NLS-1$
	char[] ValueOf = "valueOf".toCharArray(); //$NON-NLS-1$
	char[] ForName = "forName".toCharArray(); //$NON-NLS-1$
	char[] GetMessage = "getMessage".toCharArray(); //$NON-NLS-1$
	char[] NewInstance = "newInstance".toCharArray(); //$NON-NLS-1$
	char[] GetConstructor = "getConstructor".toCharArray(); //$NON-NLS-1$
	char[] Exit = "exit".toCharArray(); //$NON-NLS-1$
	char[] Intern = "intern".toCharArray(); //$NON-NLS-1$
	char[] Out = "out".toCharArray(); //$NON-NLS-1$
	char[] TYPE = "TYPE".toCharArray(); //$NON-NLS-1$
	char[] This = "this".toCharArray(); //$NON-NLS-1$
	char[] JavaLangClassSignature = "Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	char[] ForNameSignature = "(Ljava/lang/String;)Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	char[] GetMessageSignature = "()Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] GetConstructorSignature = "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray(); //$NON-NLS-1$
	char[] StringConstructorSignature = "(Ljava/lang/String;)V".toCharArray(); //$NON-NLS-1$
	char[] NewInstanceSignature = "(Ljava/lang/Class;[I)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] JavaLangReflectConstructorNewInstanceSignature = "([Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] DefaultConstructorSignature = "()V".toCharArray(); //$NON-NLS-1$
	char[] ClinitSignature = DefaultConstructorSignature;
	char[] ToStringSignature = GetMessageSignature;
	char[] InternSignature = GetMessageSignature;
	char[] StringBufferAppendIntSignature = "(I)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendLongSignature = "(J)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendFloatSignature = "(F)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendDoubleSignature = "(D)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendCharSignature = "(C)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendBooleanSignature = "(Z)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendObjectSignature = "(Ljava/lang/Object;)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBufferAppendStringSignature = "(Ljava/lang/String;)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendIntSignature = "(I)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendLongSignature = "(J)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendFloatSignature = "(F)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendDoubleSignature = "(D)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendCharSignature = "(C)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendBooleanSignature = "(Z)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendObjectSignature = "(Ljava/lang/Object;)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] StringBuilderAppendStringSignature = "(Ljava/lang/String;)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfObjectSignature = "(Ljava/lang/Object;)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfIntSignature = "(I)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfLongSignature = "(J)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfCharSignature = "(C)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfBooleanSignature = "(Z)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfDoubleSignature = "(D)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] ValueOfFloatSignature = "(F)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] JavaIoPrintStreamSignature = "Ljava/io/PrintStream;".toCharArray(); //$NON-NLS-1$
	char[] ExitIntSignature = "(I)V".toCharArray(); //$NON-NLS-1$
	char[] ArrayJavaLangObjectConstantPoolName = "[Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] ArrayJavaLangClassConstantPoolName = "[Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	char[] JavaLangAssertionErrorConstantPoolName = "java/lang/AssertionError".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorIntConstrSignature = "(I)V".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorLongConstrSignature = "(J)V".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorFloatConstrSignature = "(F)V".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorDoubleConstrSignature = "(D)V".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorCharConstrSignature = "(C)V".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorBooleanConstrSignature = "(Z)V".toCharArray(); //$NON-NLS-1$
	char[] AssertionErrorObjectConstrSignature = "(Ljava/lang/Object;)V".toCharArray(); //$NON-NLS-1$
	char[] DesiredAssertionStatus = "desiredAssertionStatus".toCharArray(); //$NON-NLS-1$
	char[] DesiredAssertionStatusSignature = "()Z".toCharArray(); //$NON-NLS-1$
	char[] ShortConstrSignature = "(S)V".toCharArray(); //$NON-NLS-1$
	char[] ByteConstrSignature = "(B)V".toCharArray(); //$NON-NLS-1$
	char[] GetClass = "getClass".toCharArray(); //$NON-NLS-1$
	char[] GetClassSignature = "()Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	char[] GetComponentType = "getComponentType".toCharArray(); //$NON-NLS-1$
	char[] GetComponentTypeSignature = GetClassSignature;
	char[] HasNext = "hasNext".toCharArray();//$NON-NLS-1$
	char[] HasNextSignature = "()Z".toCharArray();//$NON-NLS-1$
	char[] Next = "next".toCharArray();//$NON-NLS-1$
	char[] NextSignature = "()Ljava/lang/Object;".toCharArray();//$NON-NLS-1$
	char[] Equals = "equals".toCharArray(); //$NON-NLS-1$
	char[] EqualsSignature = "(Ljava/lang/Object;)Z".toCharArray(); //$NON-NLS-1$
	char[] Ordinal = "ordinal".toCharArray(); //$NON-NLS-1$
	char[] OrdinalSignature = "()I".toCharArray(); //$NON-NLS-1$
	char[] JavaLangIllegalArgumentExceptionConstantPoolName = "java/lang/IllegalArgumentException".toCharArray(); //$NON-NLS-1$
	char[] ArrayCopy = "arraycopy".toCharArray(); //$NON-NLS-1$
	char[] ArrayCopySignature = "(Ljava/lang/Object;ILjava/lang/Object;II)V".toCharArray(); //$NON-NLS-1$
	char[] JavaLangStringSignature = "Ljava/lang/String;".toCharArray(); //$NON-NLS-1$

	// predefined type constant names
	char[][] JAVA_LANG_REFLECT_FIELD = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "Field".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_ACCESSIBLEOBJECT = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "AccessibleObject".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_METHOD = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "Method".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_ARRAY = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "Array".toCharArray()}; //$NON-NLS-1$

	// predefined methods constant names
	char[] GETDECLAREDFIELD_NAME = "getDeclaredField".toCharArray(); //$NON-NLS-1$
	char[] GETDECLAREDFIELD_SIGNATURE = "(Ljava/lang/String;)Ljava/lang/reflect/Field;".toCharArray(); //$NON-NLS-1$
	char[] SETACCESSIBLE_NAME = "setAccessible".toCharArray(); //$NON-NLS-1$
	char[] SETACCESSIBLE_SIGNATURE = "(Z)V".toCharArray(); //$NON-NLS-1$
	char[] JAVALANGREFLECTFIELD_CONSTANTPOOLNAME = "java/lang/reflect/Field".toCharArray(); //$NON-NLS-1$
	char[] JAVALANGREFLECTACCESSIBLEOBJECT_CONSTANTPOOLNAME = "java/lang/reflect/AccessibleObject".toCharArray(); //$NON-NLS-1$
	char[] JAVALANGREFLECTARRAY_CONSTANTPOOLNAME = "java/lang/reflect/Array".toCharArray(); //$NON-NLS-1$
	char[] JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME = "java/lang/reflect/Method".toCharArray(); //$NON-NLS-1$
	char[] GET_INT_METHOD_NAME = "getInt".toCharArray(); //$NON-NLS-1$
	char[] GET_LONG_METHOD_NAME = "getLong".toCharArray(); //$NON-NLS-1$
	char[] GET_DOUBLE_METHOD_NAME = "getDouble".toCharArray(); //$NON-NLS-1$
	char[] GET_FLOAT_METHOD_NAME = "getFloat".toCharArray(); //$NON-NLS-1$
	char[] GET_BYTE_METHOD_NAME = "getByte".toCharArray(); //$NON-NLS-1$
	char[] GET_CHAR_METHOD_NAME = "getChar".toCharArray(); //$NON-NLS-1$
	char[] GET_BOOLEAN_METHOD_NAME = "getBoolean".toCharArray(); //$NON-NLS-1$
	char[] GET_OBJECT_METHOD_NAME = "get".toCharArray(); //$NON-NLS-1$
	char[] GET_SHORT_METHOD_NAME = "getShort".toCharArray(); //$NON-NLS-1$
	char[] ARRAY_NEWINSTANCE_NAME = "newInstance".toCharArray(); //$NON-NLS-1$
	char[] GET_INT_METHOD_SIGNATURE = "(Ljava/lang/Object;)I".toCharArray(); //$NON-NLS-1$
	char[] GET_LONG_METHOD_SIGNATURE = "(Ljava/lang/Object;)J".toCharArray(); //$NON-NLS-1$
	char[] GET_DOUBLE_METHOD_SIGNATURE = "(Ljava/lang/Object;)D".toCharArray(); //$NON-NLS-1$
	char[] GET_FLOAT_METHOD_SIGNATURE = "(Ljava/lang/Object;)F".toCharArray(); //$NON-NLS-1$
	char[] GET_BYTE_METHOD_SIGNATURE = "(Ljava/lang/Object;)B".toCharArray(); //$NON-NLS-1$
	char[] GET_CHAR_METHOD_SIGNATURE = "(Ljava/lang/Object;)C".toCharArray(); //$NON-NLS-1$
	char[] GET_BOOLEAN_METHOD_SIGNATURE = "(Ljava/lang/Object;)Z".toCharArray(); //$NON-NLS-1$
	char[] GET_OBJECT_METHOD_SIGNATURE = "(Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] GET_SHORT_METHOD_SIGNATURE = "(Ljava/lang/Object;)S".toCharArray(); //$NON-NLS-1$
	char[] SET_INT_METHOD_NAME = "setInt".toCharArray(); //$NON-NLS-1$
	char[] SET_LONG_METHOD_NAME = "setLong".toCharArray(); //$NON-NLS-1$
	char[] SET_DOUBLE_METHOD_NAME = "setDouble".toCharArray(); //$NON-NLS-1$
	char[] SET_FLOAT_METHOD_NAME = "setFloat".toCharArray(); //$NON-NLS-1$
	char[] SET_BYTE_METHOD_NAME = "setByte".toCharArray(); //$NON-NLS-1$
	char[] SET_CHAR_METHOD_NAME = "setChar".toCharArray(); //$NON-NLS-1$
	char[] SET_BOOLEAN_METHOD_NAME = "setBoolean".toCharArray(); //$NON-NLS-1$
	char[] SET_OBJECT_METHOD_NAME = "set".toCharArray(); //$NON-NLS-1$
	char[] SET_SHORT_METHOD_NAME = "setShort".toCharArray(); //$NON-NLS-1$
	char[] SET_INT_METHOD_SIGNATURE = "(Ljava/lang/Object;I)V".toCharArray(); //$NON-NLS-1$
	char[] SET_LONG_METHOD_SIGNATURE = "(Ljava/lang/Object;J)V".toCharArray(); //$NON-NLS-1$
	char[] SET_DOUBLE_METHOD_SIGNATURE = "(Ljava/lang/Object;D)V".toCharArray(); //$NON-NLS-1$
	char[] SET_FLOAT_METHOD_SIGNATURE = "(Ljava/lang/Object;F)V".toCharArray(); //$NON-NLS-1$
	char[] SET_BYTE_METHOD_SIGNATURE = "(Ljava/lang/Object;B)V".toCharArray(); //$NON-NLS-1$
	char[] SET_CHAR_METHOD_SIGNATURE = "(Ljava/lang/Object;C)V".toCharArray(); //$NON-NLS-1$
	char[] SET_BOOLEAN_METHOD_SIGNATURE = "(Ljava/lang/Object;Z)V".toCharArray(); //$NON-NLS-1$
	char[] SET_OBJECT_METHOD_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)V".toCharArray(); //$NON-NLS-1$
	char[] SET_SHORT_METHOD_SIGNATURE = "(Ljava/lang/Object;S)V".toCharArray(); //$NON-NLS-1$
	char[] GETDECLAREDMETHOD_NAME = "getDeclaredMethod".toCharArray(); //$NON-NLS-1$
	char[] GETDECLAREDMETHOD_SIGNATURE = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;".toCharArray(); //$NON-NLS-1$
	char[] ARRAY_NEWINSTANCE_SIGNATURE = "(Ljava/lang/Class;[I)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] INVOKE_METHOD_METHOD_NAME = "invoke".toCharArray(); //$NON-NLS-1$
	char[] INVOKE_METHOD_METHOD_SIGNATURE = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] BYTEVALUE_BYTE_METHOD_NAME = "byteValue".toCharArray(); //$NON-NLS-1$
	char[] BYTEVALUE_BYTE_METHOD_SIGNATURE = "()B".toCharArray(); //$NON-NLS-1$
	char[] SHORTVALUE_SHORT_METHOD_NAME = "shortValue".toCharArray(); //$NON-NLS-1$
	char[] DOUBLEVALUE_DOUBLE_METHOD_NAME = "doubleValue".toCharArray(); //$NON-NLS-1$
	char[] FLOATVALUE_FLOAT_METHOD_NAME = "floatValue".toCharArray(); //$NON-NLS-1$
	char[] INTVALUE_INTEGER_METHOD_NAME = "intValue".toCharArray(); //$NON-NLS-1$
	char[] CHARVALUE_CHARACTER_METHOD_NAME = "charValue".toCharArray(); //$NON-NLS-1$
	char[] BOOLEANVALUE_BOOLEAN_METHOD_NAME = "booleanValue".toCharArray(); //$NON-NLS-1$
	char[] LONGVALUE_LONG_METHOD_NAME = "longValue".toCharArray(); //$NON-NLS-1$
	char[] SHORTVALUE_SHORT_METHOD_SIGNATURE = "()S".toCharArray(); //$NON-NLS-1$
	char[] DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE = "()D".toCharArray(); //$NON-NLS-1$
	char[] FLOATVALUE_FLOAT_METHOD_SIGNATURE = "()F".toCharArray(); //$NON-NLS-1$
	char[] INTVALUE_INTEGER_METHOD_SIGNATURE = "()I".toCharArray(); //$NON-NLS-1$
	char[] CHARVALUE_CHARACTER_METHOD_SIGNATURE = "()C".toCharArray(); //$NON-NLS-1$
	char[] BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE = "()Z".toCharArray(); //$NON-NLS-1$
	char[] LONGVALUE_LONG_METHOD_SIGNATURE = "()J".toCharArray(); //$NON-NLS-1$
	char[] GETDECLAREDCONSTRUCTOR_NAME = "getDeclaredConstructor".toCharArray(); //$NON-NLS-1$
	char[] GETDECLAREDCONSTRUCTOR_SIGNATURE = "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray(); //$NON-NLS-1$
	char[] Name = "name".toCharArray(); //$NON-NLS-1$
	char[] NameSignature = "()Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
}