package org.eclipse.jdt.internal.compiler.codegen;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.problem.*;

public interface QualifiedNamesConstants {
	char[] JavaLangObjectConstantPoolName = "java/lang/Object".toCharArray();
	char[] JavaLangStringConstantPoolName = "java/lang/String".toCharArray();
	char[] JavaLangStringBufferConstantPoolName =
		"java/lang/StringBuffer".toCharArray();
	char[] JavaLangClassConstantPoolName = "java/lang/Class".toCharArray();
	char[] JavaLangThrowableConstantPoolName = "java/lang/Throwable".toCharArray();
	char[] JavaLangClassNotFoundExceptionConstantPoolName =
		"java/lang/ClassNotFoundException".toCharArray();
	char[] JavaLangNoClassDefFoundErrorConstantPoolName =
		"java/lang/NoClassDefFoundError".toCharArray();
	char[] JavaLangIntegerConstantPoolName = "java/lang/Integer".toCharArray();
	char[] JavaLangFloatConstantPoolName = "java/lang/Float".toCharArray();
	char[] JavaLangDoubleConstantPoolName = "java/lang/Double".toCharArray();
	char[] JavaLangLongConstantPoolName = "java/lang/Long".toCharArray();
	char[] JavaLangShortConstantPoolName = "java/lang/Short".toCharArray();
	char[] JavaLangByteConstantPoolName = "java/lang/Byte".toCharArray();
	char[] JavaLangCharacterConstantPoolName = "java/lang/Character".toCharArray();
	char[] JavaLangVoidConstantPoolName = "java/lang/Void".toCharArray();
	char[] JavaLangBooleanConstantPoolName = "java/lang/Boolean".toCharArray();
	char[] JavaLangSystemConstantPoolName = "java/lang/System".toCharArray();
	char[] JavaLangErrorConstantPoolName = "java/lang/Error".toCharArray();
	char[] JavaLangExceptionConstantPoolName = "java/lang/Exception".toCharArray();
	char[] JavaLangReflectConstructor =
		"java/lang/reflect/Constructor".toCharArray();
	char[] Append = new char[] { 'a', 'p', 'p', 'e', 'n', 'd' };
	char[] ToString = new char[] { 't', 'o', 'S', 't', 'r', 'i', 'n', 'g' };
	char[] Init = new char[] { '<', 'i', 'n', 'i', 't', '>' };
	char[] Clinit = new char[] { '<', 'c', 'l', 'i', 'n', 'i', 't', '>' };
	char[] ValueOf = new char[] { 'v', 'a', 'l', 'u', 'e', 'O', 'f' };
	char[] ForName = new char[] { 'f', 'o', 'r', 'N', 'a', 'm', 'e' };
	char[] GetMessage =
		new char[] { 'g', 'e', 't', 'M', 'e', 's', 's', 'a', 'g', 'e' };
	char[] NewInstance = "newInstance".toCharArray();
	char[] GetConstructor = "getConstructor".toCharArray();
	char[] Exit = new char[] { 'e', 'x', 'i', 't' };
	char[] Intern = "intern".toCharArray();
	char[] Out = new char[] { 'o', 'u', 't' };
	char[] TYPE = new char[] { 'T', 'Y', 'P', 'E' };
	char[] This = new char[] { 't', 'h', 'i', 's' };
	char[] JavaLangClassSignature =
		new char[] {
			'L',
			'j',
			'a',
			'v',
			'a',
			'/',
			'l',
			'a',
			'n',
			'g',
			'/',
			'C',
			'l',
			'a',
			's',
			's',
			';' };
	char[] ForNameSignature = "(Ljava/lang/String;)Ljava/lang/Class;".toCharArray();
	char[] GetMessageSignature = "()Ljava/lang/String;".toCharArray();
	char[] GetConstructorSignature =
		"([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;".toCharArray();
	char[] StringConstructorSignature = "(Ljava/lang/String;)V".toCharArray();
	char[] NewInstanceSignature =
		"([Ljava/lang/Object;)Ljava/lang/Object;".toCharArray();
	char[] DefaultConstructorSignature = { '(', ')', 'V' };
	char[] ClinitSignature = DefaultConstructorSignature;
	char[] ToStringSignature = GetMessageSignature;
	char[] InternSignature = GetMessageSignature;
	char[] AppendIntSignature = "(I)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendLongSignature = "(J)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendFloatSignature = "(F)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendDoubleSignature = "(D)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendCharSignature = "(C)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendBooleanSignature = "(Z)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendObjectSignature =
		"(Ljava/lang/Object;)Ljava/lang/StringBuffer;".toCharArray();
	char[] AppendStringSignature =
		"(Ljava/lang/String;)Ljava/lang/StringBuffer;".toCharArray();
	char[] ValueOfObjectSignature =
		"(Ljava/lang/Object;)Ljava/lang/String;".toCharArray();
	char[] ValueOfIntSignature = "(I)Ljava/lang/String;".toCharArray();
	char[] ValueOfLongSignature = "(J)Ljava/lang/String;".toCharArray();
	char[] ValueOfCharSignature = "(C)Ljava/lang/String;".toCharArray();
	char[] ValueOfBooleanSignature = "(Z)Ljava/lang/String;".toCharArray();
	char[] ValueOfDoubleSignature = "(D)Ljava/lang/String;".toCharArray();
	char[] ValueOfFloatSignature = "(F)Ljava/lang/String;".toCharArray();
	char[] JavaIoPrintStreamSignature = "Ljava/io/PrintStream;".toCharArray();
	char[] ExitIntSignature = new char[] { '(', 'I', ')', 'V' };
	char[] ArrayJavaLangObjectConstantPoolName =
		"[Ljava/lang/Object;".toCharArray();
	char[] ArrayJavaLangClassConstantPoolName = "[Ljava/lang/Class;".toCharArray();

}
