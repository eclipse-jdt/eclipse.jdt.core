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
	char[] Append = new char[] {'a', 'p', 'p', 'e', 'n', 'd'};
	char[] ToString = new char[] {'t', 'o', 'S', 't', 'r', 'i', 'n', 'g'};
	char[] Init = new char[] {'<', 'i', 'n', 'i', 't', '>'};
	char[] Clinit = new char[] {'<', 'c', 'l', 'i', 'n', 'i', 't', '>'};
	char[] ValueOf = new char[] {'v', 'a', 'l', 'u', 'e', 'O', 'f'};
	char[] ForName = new char[] {'f', 'o', 'r', 'N', 'a', 'm', 'e'};
	char[] GetMessage = new char[] {'g', 'e', 't', 'M', 'e', 's', 's', 'a', 'g', 'e'};
	char[] NewInstance = "newInstance".toCharArray(); //$NON-NLS-1$
	char[] GetConstructor = "getConstructor".toCharArray(); //$NON-NLS-1$
	char[] Exit = new char[] {'e', 'x', 'i', 't'};
	char[] Intern = "intern".toCharArray(); //$NON-NLS-1$
	char[] Out = new char[] {'o', 'u', 't'};
	char[] TYPE = new char[] {'T', 'Y', 'P', 'E'};
	char[] This = new char[] {'t', 'h', 'i', 's'};
	char[] JavaLangClassSignature = new char[] {'L', 'j', 'a', 'v', 'a', '/', 'l', 'a', 'n', 'g', '/', 'C', 'l', 'a', 's', 's', ';'};
	char[] ForNameSignature = "(Ljava/lang/String;)Ljava/lang/Class;".toCharArray(); //$NON-NLS-1$
	char[] GetMessageSignature = "()Ljava/lang/String;".toCharArray(); //$NON-NLS-1$
	char[] GetConstructorSignature = "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray(); //$NON-NLS-1$
	char[] StringConstructorSignature = "(Ljava/lang/String;)V".toCharArray(); //$NON-NLS-1$
	char[] NewInstanceSignature = "([Ljava/lang/Object;)Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	char[] DefaultConstructorSignature = {'(', ')', 'V'};
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
	char[] ExitIntSignature = new char[] {'(', 'I', ')', 'V'};
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
}
