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
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
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
	public static final char[] Append = "append".toCharArray(); //$NON-NLS-1$
	public static final char[] ARRAY_NEWINSTANCE_NAME = "newInstance".toCharArray(); //$NON-NLS-1$
	public static final char[] ARRAY_NEWINSTANCE_SIGNATURE = "(Ljava/lang/Class;[I)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	public static final char[] ArrayCopy = "arraycopy".toCharArray(); //$NON-NLS-1$
	public static final char[] ArrayCopySignature = "(Ljava/lang/Object;ILjava/lang/Object;II)V".toCharArray(); //$NON-NLS-1$
	public static final char[] ArrayJavaLangClassConstantPoolName = "[Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	public static final char[] ArrayJavaLangObjectConstantPoolName = "[Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	public static final char[] booleanBooleanSignature = "(Z)Ljava/lang/Boolean;".toCharArray(); //$NON-NLS-1$
	public static final char[] BooleanConstrSignature = "(Z)V".toCharArray(); //$NON-NLS-1$
	public static final char[] BOOLEANVALUE_BOOLEAN_METHOD_NAME = "booleanValue".toCharArray(); //$NON-NLS-1$
	public static final char[] BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE = "()Z".toCharArray(); //$NON-NLS-1$
	public static final char[] byteByteSignature = "(B)Ljava/lang/Byte;".toCharArray(); //$NON-NLS-1$
	public static final char[] ByteConstrSignature = "(B)V".toCharArray(); //$NON-NLS-1$
	public static final char[] BYTEVALUE_BYTE_METHOD_NAME = "byteValue".toCharArray(); //$NON-NLS-1$
	public static final char[] BYTEVALUE_BYTE_METHOD_SIGNATURE = "()B".toCharArray(); //$NON-NLS-1$
	public static final char[] charCharacterSignature = "(C)Ljava/lang/Character;".toCharArray(); //$NON-NLS-1$
	public static final char[] CharConstrSignature = "(C)V".toCharArray(); //$NON-NLS-1$
	public static final char[] CHARVALUE_CHARACTER_METHOD_NAME = "charValue".toCharArray(); //$NON-NLS-1$
	public static final char[] CHARVALUE_CHARACTER_METHOD_SIGNATURE = "()C".toCharArray(); //$NON-NLS-1$
	public static final char[] Clinit = "<clinit>".toCharArray(); //$NON-NLS-1$
	public static final char[] DefaultConstructorSignature = "()V".toCharArray(); //$NON-NLS-1$
	public static final char[] ClinitSignature = DefaultConstructorSignature;
	public static final char[] DesiredAssertionStatus = "desiredAssertionStatus".toCharArray(); //$NON-NLS-1$
	public static final char[] DesiredAssertionStatusSignature = "()Z".toCharArray(); //$NON-NLS-1$
	public static final char[] DoubleConstrSignature = "(D)V".toCharArray(); //$NON-NLS-1$
	public static final char[] doubleDoubleSignature = "(D)Ljava/lang/Double;".toCharArray(); //$NON-NLS-1$
	public static final char[] DOUBLEVALUE_DOUBLE_METHOD_NAME = "doubleValue".toCharArray(); //$NON-NLS-1$
	public static final char[] DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE = "()D".toCharArray(); //$NON-NLS-1$
	public static final char[] Equals = "equals".toCharArray(); //$NON-NLS-1$
	public static final char[] EqualsSignature = "(Ljava/lang/Object;)Z".toCharArray(); //$NON-NLS-1$
	public static final char[] Exit = "exit".toCharArray(); //$NON-NLS-1$
	public static final char[] ExitIntSignature = "(I)V".toCharArray(); //$NON-NLS-1$
	public static final char[] FloatConstrSignature = "(F)V".toCharArray(); //$NON-NLS-1$
	public static final char[] floatFloatSignature = "(F)Ljava/lang/Float;".toCharArray(); //$NON-NLS-1$
	public static final char[] FLOATVALUE_FLOAT_METHOD_NAME = "floatValue".toCharArray(); //$NON-NLS-1$
	public static final char[] FLOATVALUE_FLOAT_METHOD_SIGNATURE = "()F".toCharArray(); //$NON-NLS-1$
	public static final char[] ForName = "forName".toCharArray(); //$NON-NLS-1$
	public static final char[] ForNameSignature = "(Ljava/lang/String;)Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_BOOLEAN_METHOD_NAME = "getBoolean".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_BOOLEAN_METHOD_SIGNATURE = "(Ljava/lang/Object;)Z".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_BYTE_METHOD_NAME = "getByte".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_BYTE_METHOD_SIGNATURE = "(Ljava/lang/Object;)B".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_CHAR_METHOD_NAME = "getChar".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_CHAR_METHOD_SIGNATURE = "(Ljava/lang/Object;)C".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_DOUBLE_METHOD_NAME = "getDouble".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_DOUBLE_METHOD_SIGNATURE = "(Ljava/lang/Object;)D".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_FLOAT_METHOD_NAME = "getFloat".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_FLOAT_METHOD_SIGNATURE = "(Ljava/lang/Object;)F".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_INT_METHOD_NAME = "getInt".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_INT_METHOD_SIGNATURE = "(Ljava/lang/Object;)I".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_LONG_METHOD_NAME = "getLong".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_LONG_METHOD_SIGNATURE = "(Ljava/lang/Object;)J".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_OBJECT_METHOD_NAME = "get".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_OBJECT_METHOD_SIGNATURE = "(Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_SHORT_METHOD_NAME = "getShort".toCharArray(); //$NON-NLS-1$
	public static final char[] GET_SHORT_METHOD_SIGNATURE = "(Ljava/lang/Object;)S".toCharArray(); //$NON-NLS-1$
	public static final char[] GetClass = "getClass".toCharArray(); //$NON-NLS-1$
	public static final char[] GetClassSignature = "()Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	public static final char[] GetComponentType = "getComponentType".toCharArray(); //$NON-NLS-1$
	public static final char[] GetComponentTypeSignature = GetClassSignature;
	public static final char[] GetConstructor = "getConstructor".toCharArray(); //$NON-NLS-1$
	public static final char[] GetConstructorSignature = "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray(); //$NON-NLS-1$
	public static final char[] GETDECLAREDCONSTRUCTOR_NAME = "getDeclaredConstructor".toCharArray(); //$NON-NLS-1$
	public static final char[] GETDECLAREDCONSTRUCTOR_SIGNATURE = "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray(); //$NON-NLS-1$
	// predefined methods constant names
	public static final char[] GETDECLAREDFIELD_NAME = "getDeclaredField".toCharArray(); //$NON-NLS-1$
	public static final char[] GETDECLAREDFIELD_SIGNATURE = "(Ljava/lang/String;)Ljava/lang/reflect/Field;".toCharArray(); //$NON-NLS-1$
	public static final char[] GETDECLAREDMETHOD_NAME = "getDeclaredMethod".toCharArray(); //$NON-NLS-1$
	public static final char[] GETDECLAREDMETHOD_SIGNATURE = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;".toCharArray(); //$NON-NLS-1$
	public static final char[] GetMessage = "getMessage".toCharArray(); //$NON-NLS-1$
	public static final char[] GetMessageSignature = "()Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] HasNext = "hasNext".toCharArray();//$NON-NLS-1$
	public static final char[] HasNextSignature = "()Z".toCharArray();//$NON-NLS-1$
	public static final char[] Init = "<init>".toCharArray(); //$NON-NLS-1$
	public static final char[] IntConstrSignature = "(I)V".toCharArray(); //$NON-NLS-1$
	public static final char[] Intern = "intern".toCharArray(); //$NON-NLS-1$
	public static final char[] InternSignature = GetMessageSignature;
	public static final char[] IntIntegerSignature = "(I)Ljava/lang/Integer;".toCharArray(); //$NON-NLS-1$
	public static final char[] INTVALUE_INTEGER_METHOD_NAME = "intValue".toCharArray(); //$NON-NLS-1$
	public static final char[] INTVALUE_INTEGER_METHOD_SIGNATURE = "()I".toCharArray(); //$NON-NLS-1$
	public static final char[] INVOKE_METHOD_METHOD_NAME = "invoke".toCharArray(); //$NON-NLS-1$
	public static final char[] INVOKE_METHOD_METHOD_SIGNATURE = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	public static final char[][] JAVA_LANG_REFLECT_ACCESSIBLEOBJECT = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "AccessibleObject".toCharArray()}; //$NON-NLS-1$
	public static final char[][] JAVA_LANG_REFLECT_ARRAY = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "Array".toCharArray()}; //$NON-NLS-1$
	// predefined type constant names
	public static final char[][] JAVA_LANG_REFLECT_FIELD = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "Field".toCharArray()}; //$NON-NLS-1$
	public static final char[][] JAVA_LANG_REFLECT_METHOD = new char[][] {TypeConstants.JAVA, TypeConstants.LANG, TypeConstants.REFLECT, "Method".toCharArray()}; //$NON-NLS-1$
	public static final char[] JavaIoPrintStreamSignature = "Ljava/io/PrintStream;".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangAssertionErrorConstantPoolName = "java/lang/AssertionError".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangBooleanConstantPoolName = "java/lang/Boolean".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangByteConstantPoolName = "java/lang/Byte".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangCharacterConstantPoolName = "java/lang/Character".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangClassConstantPoolName = "java/lang/Class".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangClassNotFoundExceptionConstantPoolName = "java/lang/ClassNotFoundException".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangClassSignature = "Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangDoubleConstantPoolName = "java/lang/Double".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangErrorConstantPoolName = "java/lang/Error".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangExceptionConstantPoolName = "java/lang/Exception".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangFloatConstantPoolName = "java/lang/Float".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangIllegalArgumentExceptionConstantPoolName = "java/lang/IllegalArgumentException".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangIntegerConstantPoolName = "java/lang/Integer".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangLongConstantPoolName = "java/lang/Long".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangNoClassDefFoundErrorConstantPoolName = "java/lang/NoClassDefFoundError".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangNoSuchFieldErrorConstantPoolName = "java/lang/NoSuchFieldError".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangObjectConstantPoolName = "java/lang/Object".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVALANGREFLECTACCESSIBLEOBJECT_CONSTANTPOOLNAME = "java/lang/reflect/AccessibleObject".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVALANGREFLECTARRAY_CONSTANTPOOLNAME = "java/lang/reflect/Array".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangReflectConstructor = "java/lang/reflect/Constructor".toCharArray();   //$NON-NLS-1$
	public static final char[] JavaLangReflectConstructorNewInstanceSignature = "([Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVALANGREFLECTFIELD_CONSTANTPOOLNAME = "java/lang/reflect/Field".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME = "java/lang/reflect/Method".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangShortConstantPoolName = "java/lang/Short".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangStringBufferConstantPoolName = "java/lang/StringBuffer".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangStringBuilderConstantPoolName = "java/lang/StringBuilder".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangStringConstantPoolName = "java/lang/String".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangStringSignature = "Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangSystemConstantPoolName = "java/lang/System".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangThrowableConstantPoolName = "java/lang/Throwable".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaLangVoidConstantPoolName = "java/lang/Void".toCharArray(); //$NON-NLS-1$
	public static final char[] JavaUtilIteratorConstantPoolName = "java/util/Iterator".toCharArray(); //$NON-NLS-1$
	public static final char[] LongConstrSignature = "(J)V".toCharArray(); //$NON-NLS-1$
	public static final char[] longLongSignature = "(J)Ljava/lang/Long;".toCharArray(); //$NON-NLS-1$
	public static final char[] LONGVALUE_LONG_METHOD_NAME = "longValue".toCharArray(); //$NON-NLS-1$
	public static final char[] LONGVALUE_LONG_METHOD_SIGNATURE = "()J".toCharArray(); //$NON-NLS-1$
	public static final char[] Name = "name".toCharArray(); //$NON-NLS-1$
	public static final char[] NameSignature = "()Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] NewInstance = "newInstance".toCharArray(); //$NON-NLS-1$
	public static final char[] NewInstanceSignature = "(Ljava/lang/Class;[I)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	public static final char[] Next = "next".toCharArray();//$NON-NLS-1$
	public static final char[] NextSignature = "()Ljava/lang/Object;".toCharArray();//$NON-NLS-1$
	public static final char[] ObjectConstrSignature = "(Ljava/lang/Object;)V".toCharArray(); //$NON-NLS-1$
	public static final char[] Ordinal = "ordinal".toCharArray(); //$NON-NLS-1$
	public static final char[] OrdinalSignature = "()I".toCharArray(); //$NON-NLS-1$
	public static final char[] Out = "out".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_BOOLEAN_METHOD_NAME = "setBoolean".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_BOOLEAN_METHOD_SIGNATURE = "(Ljava/lang/Object;Z)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_BYTE_METHOD_NAME = "setByte".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_BYTE_METHOD_SIGNATURE = "(Ljava/lang/Object;B)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_CHAR_METHOD_NAME = "setChar".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_CHAR_METHOD_SIGNATURE = "(Ljava/lang/Object;C)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_DOUBLE_METHOD_NAME = "setDouble".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_DOUBLE_METHOD_SIGNATURE = "(Ljava/lang/Object;D)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_FLOAT_METHOD_NAME = "setFloat".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_FLOAT_METHOD_SIGNATURE = "(Ljava/lang/Object;F)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_INT_METHOD_NAME = "setInt".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_INT_METHOD_SIGNATURE = "(Ljava/lang/Object;I)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_LONG_METHOD_NAME = "setLong".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_LONG_METHOD_SIGNATURE = "(Ljava/lang/Object;J)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_OBJECT_METHOD_NAME = "set".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_OBJECT_METHOD_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_SHORT_METHOD_NAME = "setShort".toCharArray(); //$NON-NLS-1$
	public static final char[] SET_SHORT_METHOD_SIGNATURE = "(Ljava/lang/Object;S)V".toCharArray(); //$NON-NLS-1$
	public static final char[] SETACCESSIBLE_NAME = "setAccessible".toCharArray(); //$NON-NLS-1$
	public static final char[] SETACCESSIBLE_SIGNATURE = "(Z)V".toCharArray(); //$NON-NLS-1$
	public static final char[] ShortConstrSignature = "(S)V".toCharArray(); //$NON-NLS-1$
	public static final char[] shortShortSignature = "(S)Ljava/lang/Short;".toCharArray(); //$NON-NLS-1$
	public static final char[] SHORTVALUE_SHORT_METHOD_NAME = "shortValue".toCharArray(); //$NON-NLS-1$
	public static final char[] SHORTVALUE_SHORT_METHOD_SIGNATURE = "()S".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendBooleanSignature = "(Z)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendCharSignature = "(C)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendDoubleSignature = "(D)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendFloatSignature = "(F)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendIntSignature = "(I)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendLongSignature = "(J)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendObjectSignature = "(Ljava/lang/Object;)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBufferAppendStringSignature = "(Ljava/lang/String;)Ljava/lang/StringBuffer;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendBooleanSignature = "(Z)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendCharSignature = "(C)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendDoubleSignature = "(D)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendFloatSignature = "(F)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendIntSignature = "(I)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendLongSignature = "(J)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendObjectSignature = "(Ljava/lang/Object;)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringBuilderAppendStringSignature = "(Ljava/lang/String;)Ljava/lang/StringBuilder;".toCharArray(); //$NON-NLS-1$
	public static final char[] StringConstructorSignature = "(Ljava/lang/String;)V".toCharArray(); //$NON-NLS-1$
	public static final char[] This = "this".toCharArray(); //$NON-NLS-1$
	public static final char[] ToString = "toString".toCharArray(); //$NON-NLS-1$
	public static final char[] ToStringSignature = GetMessageSignature;
	public static final char[] TYPE = "TYPE".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOf = "valueOf".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfBooleanSignature = "(Z)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfCharSignature = "(C)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfDoubleSignature = "(D)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfFloatSignature = "(F)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfIntSignature = "(I)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfLongSignature = "(J)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] ValueOfObjectSignature = "(Ljava/lang/Object;)Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_ANNOTATION_DOCUMENTED = "Ljava/lang/annotation/Documented;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_ANNOTATION_ELEMENTTYPE = "Ljava/lang/annotation/ElementType;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_ANNOTATION_RETENTION = "Ljava/lang/annotation/Retention;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_ANNOTATION_RETENTIONPOLICY = "Ljava/lang/annotation/RetentionPolicy;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_ANNOTATION_TARGET = "Ljava/lang/annotation/Target;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_DEPRECATED = "Ljava/lang/Deprecated;".toCharArray(); //$NON-NLS-1$
	public static final char[] JAVA_LANG_ANNOTATION_INHERITED = "Ljava/lang/Inherited;".toCharArray(); //$NON-NLS-1$
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
		writeU1(constantPoolDeclaringClass.isInterface() || constantPoolDeclaringClass.isAnnotationType() ? InterfaceMethodRefTag : MethodRefTag);
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
public int literalIndexForMethod(char[] selector, char[] signature) {
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
	if (UTF8Cache.get(ConstantPool.ClinitSignature) >= constantPoolIndex) {
		UTF8Cache.remove(ConstantPool.ClinitSignature);
	}
	if (UTF8Cache.get(ConstantPool.Clinit) >= constantPoolIndex) {
		UTF8Cache.remove(ConstantPool.Clinit);
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
