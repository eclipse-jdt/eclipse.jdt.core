/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.internal.core.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IConstantValueAttribute;
import org.eclipse.jdt.core.util.IExceptionAttribute;
import org.eclipse.jdt.core.util.IExceptionTableEntry;
import org.eclipse.jdt.core.util.IFieldInfo;
import org.eclipse.jdt.core.util.IInnerClassesAttribute;
import org.eclipse.jdt.core.util.IInnerClassesAttributeEntry;
import org.eclipse.jdt.core.util.ILineNumberAttribute;
import org.eclipse.jdt.core.util.ILocalVariableAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTableEntry;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.core.util.IModifierConstants;
import org.eclipse.jdt.core.util.ISourceAttribute;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Disassembler of .class files. It generates an output in the Writer that looks close to
 * the javap output.
 */
public class Disassembler implements IClassFileDisassembler {

	private static final char[] ANY_EXCEPTION = Util.bind("classfileformat.anyexceptionhandler").toCharArray();	 //$NON-NLS-1$
	private static final String EMPTY_OUTPUT = ""; //$NON-NLS-1$
	
	/**
	 * Disassemble the class file reader.
	 */
	public String disassemble(IClassFileReader classFileReader, String lineSeparator) {
		if (classFileReader == null) return EMPTY_OUTPUT;
		
		StringBuffer buffer = new StringBuffer();

		buffer.append(Util.bind("classfileformat.magicnumber")); //$NON-NLS-1$
		buffer.append(Integer.toHexString(classFileReader.getMagic()).toUpperCase());
		writeNewLine(buffer, lineSeparator, 0);
		buffer.append(Util.bind("classfileformat.minorversion")); //$NON-NLS-1$
		buffer.append(classFileReader.getMinorVersion());
		writeNewLine(buffer, lineSeparator, 0);
		buffer.append(Util.bind("classfileformat.majorversion")); //$NON-NLS-1$
		buffer.append(classFileReader.getMajorVersion());
		writeNewLine(buffer, lineSeparator, 0);
		writeNewLine(buffer, lineSeparator, 0);
		ISourceAttribute sourceAttribute = classFileReader.getSourceFileAttribute();
		if (sourceAttribute != null) {
			buffer.append(Util.bind("classfileformat.sourcename")); //$NON-NLS-1$
			buffer.append(sourceAttribute.getSourceFileName());
			writeNewLine(buffer, lineSeparator, 0);
		}
		char[] className = classFileReader.getClassName();
		if (className == null) {
			// incomplete initialization. We cannot go further.
			return buffer.toString();
		}
		decodeModifiersForType(buffer, classFileReader.getAccessFlags());
		if (classFileReader.isClass()) {
			buffer.append(Util.bind("classfileformat.class")); //$NON-NLS-1$
		} else {
			buffer.append(Util.bind("classfileformat.interface")); //$NON-NLS-1$
		}
		CharOperation.replace(className, '/', '.');
		buffer.append(className);
		
		char[] superclassName = classFileReader.getSuperclassName();
		if (superclassName != null) {
			buffer.append(Util.bind("classfileformat.extends")); //$NON-NLS-1$
			CharOperation.replace(superclassName, '/', '.');
			buffer.append(superclassName);
		}
		char[][] superclassInterfaces = classFileReader.getInterfaceNames();
		int length = superclassInterfaces.length;
		if (length != 0) {
			buffer.append(Util.bind("classfileformat.implements")); //$NON-NLS-1$
			for (int i = 0; i < length - 1; i++) {
				char[] superinterface = superclassInterfaces[i];
				CharOperation.replace(superinterface, '/', '.');
				buffer
					.append(superinterface)
					.append(Util.bind("disassembler.comma")); //$NON-NLS-1$
			}
			char[] superinterface = superclassInterfaces[length - 1];
			CharOperation.replace(superinterface, '/', '.');
			buffer.append(superinterface);
		}
		buffer.append(Util.bind("disassembler.opentypedeclaration")); //$NON-NLS-1$
		checkSuperFlags(buffer, classFileReader.getAccessFlags(), lineSeparator, 1);
		disassembleTypeMembers(classFileReader, buffer, lineSeparator, 1);
		writeNewLine(buffer, lineSeparator, 0);
		writeNewLine(buffer, lineSeparator, 0);
		IInnerClassesAttribute innerClassesAttribute = classFileReader.getInnerClassesAttribute();
		if (innerClassesAttribute != null) {
			disassemble(innerClassesAttribute, buffer, lineSeparator, 1);
		}
		writeNewLine(buffer, lineSeparator, 0);
		IClassFileAttribute[] attributes = classFileReader.getAttributes();
		length = attributes.length;
		if (length != 0) {
			for (int i = 0; i < length; i++) {
				IClassFileAttribute attribute = attributes[i];
				if (attribute != innerClassesAttribute
					&& attribute != sourceAttribute) {
					disassemble(attribute, buffer, lineSeparator, 0);
				}
			}
		}		
		writeNewLine(buffer, lineSeparator, 0);
		buffer.append(Util.bind("disassembler.closetypedeclaration")); //$NON-NLS-1$
		return buffer.toString();
	}

	private void disassemble(IInnerClassesAttribute innerClassesAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		buffer.append(Util.bind("disassembler.innerattributesheader")); //$NON-NLS-1$
		writeNewLine(buffer, lineSeparator, tabNumber);
		IInnerClassesAttributeEntry[] innerClassesAttributeEntries = innerClassesAttribute.getInnerClassAttributesEntries();
		int length = innerClassesAttributeEntries.length;
		int innerClassNameIndex, outerClassNameIndex, innerNameIndex, accessFlags;
		IInnerClassesAttributeEntry innerClassesAttributeEntry;
		for (int i = 0; i < length - 1; i++) {
			innerClassesAttributeEntry = innerClassesAttributeEntries[i];
			innerClassNameIndex = innerClassesAttributeEntry.getInnerClassNameIndex();
			outerClassNameIndex = innerClassesAttributeEntry.getOuterClassNameIndex();
			innerNameIndex = innerClassesAttributeEntry.getInnerNameIndex();
			accessFlags = innerClassesAttributeEntry.getAccessFlags();
			buffer
				.append(Util.bind("disassembler.openinnerclassentry")) //$NON-NLS-1$
				.append(Util.bind("disassembler.inner_class_info_name")) //$NON-NLS-1$
				.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
				.append(innerClassNameIndex);
			if (innerClassNameIndex != 0) {
				buffer
					.append(Util.bind("disassembler.space")) //$NON-NLS-1$
					.append(innerClassesAttributeEntry.getInnerClassName());
			}
			writeNewLine(buffer, lineSeparator, tabNumber);
			dumpTab(tabNumber, buffer);
			buffer
				.append(Util.bind("disassembler.outer_class_info_name")) //$NON-NLS-1$
				.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
				.append(outerClassNameIndex);
			if (outerClassNameIndex != 0) {
				buffer	
					.append(Util.bind("disassembler.space")) //$NON-NLS-1$
					.append(innerClassesAttributeEntry.getOuterClassName());
			}
			writeNewLine(buffer, lineSeparator, tabNumber);
			dumpTab(tabNumber, buffer);
			buffer
				.append(Util.bind("disassembler.inner_name")) //$NON-NLS-1$
				.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
				.append(innerNameIndex);
			if (innerNameIndex != 0) {
				buffer
					.append(Util.bind("disassembler.space")) //$NON-NLS-1$
					.append(innerClassesAttributeEntry.getInnerName());
			}
			writeNewLine(buffer, lineSeparator, tabNumber);
			dumpTab(tabNumber, buffer);
			buffer
				.append(Util.bind("disassembler.inner_accessflags")) //$NON-NLS-1$
				.append(accessFlags)
				.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			decodeModifiersForInnerClasses(buffer, accessFlags);
			buffer
				.append(Util.bind("disassembler.closeinnerclassentry")) //$NON-NLS-1$
				.append(Util.bind("disassembler.comma")); //$NON-NLS-1$
			writeNewLine(buffer, lineSeparator, tabNumber);
		}
		// last entry
		innerClassesAttributeEntry = innerClassesAttributeEntries[length - 1];
		innerClassNameIndex = innerClassesAttributeEntry.getInnerClassNameIndex();
		outerClassNameIndex = innerClassesAttributeEntry.getOuterClassNameIndex();
		innerNameIndex = innerClassesAttributeEntry.getInnerNameIndex();
		accessFlags = innerClassesAttributeEntry.getAccessFlags();
		buffer
			.append(Util.bind("disassembler.openinnerclassentry")) //$NON-NLS-1$
			.append(Util.bind("disassembler.inner_class_info_name")) //$NON-NLS-1$
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(innerClassNameIndex);
		if (innerClassNameIndex != 0) {
			buffer
				.append(Util.bind("disassembler.space")) //$NON-NLS-1$
				.append(innerClassesAttributeEntry.getInnerClassName());
		}
		writeNewLine(buffer, lineSeparator, tabNumber);
		dumpTab(tabNumber, buffer);
		buffer
			.append(Util.bind("disassembler.outer_class_info_name")) //$NON-NLS-1$
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(outerClassNameIndex);
		if (outerClassNameIndex != 0) {
			buffer	
				.append(Util.bind("disassembler.space")) //$NON-NLS-1$
				.append(innerClassesAttributeEntry.getOuterClassName());
		}
		writeNewLine(buffer, lineSeparator, tabNumber);
		dumpTab(tabNumber, buffer);
		buffer
			.append(Util.bind("disassembler.inner_name")) //$NON-NLS-1$
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(innerNameIndex);
		if (innerNameIndex != 0) {
			buffer
				.append(Util.bind("disassembler.space")) //$NON-NLS-1$
				.append(innerClassesAttributeEntry.getInnerName());
		}
		writeNewLine(buffer, lineSeparator, tabNumber);
		dumpTab(tabNumber, buffer);
		buffer
			.append(Util.bind("disassembler.inner_accessflags")) //$NON-NLS-1$
			.append(accessFlags)
			.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		decodeModifiersForInnerClasses(buffer, accessFlags);
		buffer.append(Util.bind("disassembler.closeinnerclassentry")); //$NON-NLS-1$
	}
	
	private void checkSuperFlags(StringBuffer buffer, int accessFlags, String lineSeparator, int tabNumber ) {
		if ((accessFlags & IModifierConstants.ACC_SUPER) == 0) {
			writeNewLine(buffer, lineSeparator, tabNumber);
			buffer
				.append(Util.bind("disassembler.commentstart")) //$NON-NLS-1$
				.append(Util.bind("classfileformat.superflagnotset")) //$NON-NLS-1$
				.append(Util.bind("disassembler.commentend")); //$NON-NLS-1$
		}
	}

	
	private final void dumpTab(int tabNumber, StringBuffer buffer) {
		for (int i = 0; i < tabNumber; i++) {
			buffer.append(Util.bind("disassembler.tab")); //$NON-NLS-1$
		}
	} 
	
	private void disassembleTypeMembers(IClassFileReader classFileReader, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		int fieldsCount = classFileReader.getFieldsCount();
		IFieldInfo[] fields = classFileReader.getFieldInfos();
		for (int i = 0; i < fieldsCount; i++) {
			writeNewLine(buffer, lineSeparator, tabNumber);
			disassemble(fields[i], buffer, lineSeparator, tabNumber);
			writeNewLine(buffer, lineSeparator, tabNumber);
		}
		int methodsCount = classFileReader.getMethodsCount();
		IMethodInfo[] methods = classFileReader.getMethodInfos();
		for (int i = 0; i < methodsCount; i++) {
			writeNewLine(buffer, lineSeparator, tabNumber);
			disassemble(classFileReader, methods[i], buffer, lineSeparator, tabNumber);
			writeNewLine(buffer, lineSeparator, 0);
		}
	}

	private void writeNewLine(StringBuffer buffer, String lineSeparator, int tabNumber) {
		buffer.append(lineSeparator);
		dumpTab(tabNumber, buffer);
	}
 
	/**
	 * Disassemble a field info
	 */
	private void disassemble(IFieldInfo fieldInfo, StringBuffer buffer, String lineSeparator, int tabNumber) {
		decodeModifiersForField(buffer, fieldInfo.getAccessFlags());
		char[] fieldDescriptor = fieldInfo.getDescriptor();
		CharOperation.replace(fieldDescriptor, '/', '.');
		buffer.append(Signature.toCharArray(fieldDescriptor));
		buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		buffer.append(new String(fieldInfo.getName()));
		IConstantValueAttribute constantValueAttribute = fieldInfo.getConstantValueAttribute();
		if (constantValueAttribute != null) {
			buffer.append(Util.bind("disassembler.fieldhasconstant")); //$NON-NLS-1$
			IConstantPoolEntry constantPoolEntry = constantValueAttribute.getConstantValue();
			switch(constantPoolEntry.getKind()) {
				case IConstantPoolConstant.CONSTANT_Long :
					buffer.append(constantPoolEntry.getLongValue());
					break;
				case IConstantPoolConstant.CONSTANT_Float :
					buffer.append(constantPoolEntry.getFloatValue());
					break;
				case IConstantPoolConstant.CONSTANT_Double :
					buffer.append(constantPoolEntry.getDoubleValue());
					break;
				case IConstantPoolConstant.CONSTANT_Integer:
					buffer.append(constantPoolEntry.getIntegerValue());
					break;
				case IConstantPoolConstant.CONSTANT_String:
					buffer.append(constantPoolEntry.getStringValue());
			}
		}
		buffer.append(Util.bind("disassembler.endoffieldheader")); //$NON-NLS-1$
		IClassFileAttribute[] attributes = fieldInfo.getAttributes();
		int length = attributes.length;
		if (length != 0) {
			for (int i = 0; i < length; i++) {
				IClassFileAttribute attribute = attributes[i];
				if (attribute != constantValueAttribute) {
					disassemble(attribute, buffer, lineSeparator, tabNumber);
				}
			}
		}
		writeNewLine(buffer, lineSeparator, tabNumber);
		CharOperation.replace(fieldDescriptor, '.', '/');
		buffer
			.append(Util.bind("disassembler.commentstart")) //$NON-NLS-1$
			.append(Util.bind("classfileformat.fieldddescriptor")) //$NON-NLS-1$
			.append(Util.bind("classfileformat.fielddescriptorindex")) //$NON-NLS-1$
			.append(fieldInfo.getDescriptorIndex())
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(fieldDescriptor)
			.append(Util.bind("disassembler.commentend")); //$NON-NLS-1$
	}

	/**
	 * Disassemble a method info header
	 */
	private void disassemble(IClassFileReader classFileReader, IMethodInfo methodInfo, StringBuffer buffer, String lineSeparator, int tabNumber) {
		int accessFlags = methodInfo.getAccessFlags();
		decodeModifiersForMethod(buffer, accessFlags);
		char[] methodDescriptor = methodInfo.getDescriptor();
		CharOperation.replace(methodDescriptor, '/', '.');
		char[] methodName = null;
		if (methodInfo.isConstructor()) {
			methodName = classFileReader.getClassName();
			buffer.append(Signature.toCharArray(methodDescriptor, methodName, getParameterNames(methodDescriptor) , true, false));
		} else if (methodInfo.isClinit()) {
			methodName = Util.bind("classfileformat.clinitname").toCharArray(); //$NON-NLS-1$
			buffer.append(methodName);
		} else {
			methodName = methodInfo.getName();
			buffer.append(Signature.toCharArray(methodDescriptor, methodName, getParameterNames(methodDescriptor) , false, true));
		}
		IExceptionAttribute exceptionAttribute = methodInfo.getExceptionAttribute();
		if (exceptionAttribute != null) {
			buffer.append(Util.bind("classfileformat.throws")); //$NON-NLS-1$
			char[][] exceptionNames = exceptionAttribute.getExceptionNames();
			int length = exceptionNames.length;
			for (int i = 0; i < length - 1; i++) {
				char[] exceptionName = exceptionNames[i];
				CharOperation.replace(exceptionName, '/', '.');
				buffer
					.append(exceptionName)
					.append(Util.bind("disassembler.comma")); //$NON-NLS-1$
			}
			char[] exceptionName = exceptionNames[length - 1];
			CharOperation.replace(exceptionName, '/', '.');
			buffer.append(exceptionName);
			buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		}
		buffer.append(Util.bind("disassembler.endofmethodheader")); //$NON-NLS-1$
		writeNewLine(buffer, lineSeparator, tabNumber);
		CharOperation.replace(methodDescriptor, '.', '/');
		buffer
			.append(Util.bind("disassembler.commentstart")) //$NON-NLS-1$
			.append(Util.bind("classfileformat.methoddescriptor")) //$NON-NLS-1$
			.append(Util.bind("disassembler.constantpoolindex")) //$NON-NLS-1$
			.append(methodInfo.getDescriptorIndex())
			.append(Util.bind("disassembler.space")) //$NON-NLS-1$
			.append(methodDescriptor)
			.append(Util.bind("disassembler.commentend")); //$NON-NLS-1$
		writeNewLine(buffer, lineSeparator, tabNumber);
		ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		IClassFileAttribute[] attributes = methodInfo.getAttributes();
		int length = attributes.length;
		if (length != 0) {
			for (int i = 0; i < length; i++) {
				IClassFileAttribute attribute = attributes[i];
				if ((attribute != codeAttribute) && (attribute != exceptionAttribute)) {
					disassemble(attribute, buffer, lineSeparator, tabNumber);
					writeNewLine(buffer, lineSeparator, tabNumber);
				}
			}
		}
		if (codeAttribute != null) {
			disassemble(codeAttribute, buffer, lineSeparator, tabNumber);
		}
	}
	


	private void disassemble(IClassFileAttribute classFileAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		buffer.append(Util.bind("disassembler.genericattributeheader")); //$NON-NLS-1$
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer
			.append(Util.bind("disassembler.genericattributename")) //$NON-NLS-1$
			.append(classFileAttribute.getAttributeName())
			.append(Util.bind("disassembler.genericattributelength")) //$NON-NLS-1$
			.append(classFileAttribute.getAttributeLength());
	}
	
	private void disassemble(ICodeAttribute codeAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		buffer.append(Util.bind("disassembler.codeattributeheader")); //$NON-NLS-1$
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer
			.append(Util.bind("disassembler.commentstart")) //$NON-NLS-1$
			.append(Util.bind("classfileformat.maxStack")) //$NON-NLS-1$
			.append(codeAttribute.getMaxStack())
			.append(Util.bind("disassembler.comma")) //$NON-NLS-1$
			.append(Util.bind("classfileformat.maxLocals")) //$NON-NLS-1$
			.append(codeAttribute.getMaxLocals())
			.append(Util.bind("disassembler.commentend")); //$NON-NLS-1$
		writeNewLine(buffer, lineSeparator, tabNumber - 1);
		DefaultBytecodeVisitor visitor = new DefaultBytecodeVisitor(buffer, lineSeparator, tabNumber);
		try {
			codeAttribute.traverse(visitor);
		} catch(ClassFormatException e) {
		}
		int exceptionTableLength = codeAttribute.getExceptionTableLength();
		if (exceptionTableLength != 0) {
			writeNewLine(buffer, lineSeparator, tabNumber);
			IExceptionTableEntry[] exceptionTableEntries = codeAttribute.getExceptionTable();
			buffer.append(Util.bind("disassembler.exceptiontableheader")); //$NON-NLS-1$
			writeNewLine(buffer, lineSeparator, tabNumber + 1);
			for (int i = 0; i < exceptionTableLength; i++) {
				IExceptionTableEntry exceptionTableEntry = exceptionTableEntries[i];
				buffer
					.append(Util.bind("classfileformat.exceptiontablefrom")) //$NON-NLS-1$
					.append(exceptionTableEntry.getStartPC())
					.append(Util.bind("classfileformat.exceptiontableto")) //$NON-NLS-1$
					.append(exceptionTableEntry.getEndPC())
					.append(Util.bind("classfileformat.exceptiontablegoto")) //$NON-NLS-1$
					.append(exceptionTableEntry.getHandlerPC())
					.append(Util.bind("classfileformat.exceptiontablewhen")); //$NON-NLS-1$
				if (exceptionTableEntry.getCatchTypeIndex() == 0) {
					buffer.append(ANY_EXCEPTION);
				} else {
					char[] catchType = exceptionTableEntry.getCatchType();
					CharOperation.replace(catchType, '/', '.');
					buffer.append(catchType);
				}
				writeNewLine(buffer, lineSeparator, tabNumber + 1);
			}
		}
		ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
		int lineAttributeLength = lineNumberAttribute == null ? 0 : lineNumberAttribute.getLineNumberTableLength();
		if (lineAttributeLength != 0) {
			writeNewLine(buffer, lineSeparator, tabNumber);
			buffer.append(Util.bind("disassembler.linenumberattributeheader")); //$NON-NLS-1$
			writeNewLine(buffer, lineSeparator, tabNumber + 1);
			int[][] lineattributesEntries = lineNumberAttribute.getLineNumberTable();
			for (int i = 0; i < lineAttributeLength; i++) {
				buffer
					.append(Util.bind("classfileformat.linenumbertablefrom")) //$NON-NLS-1$
					.append(lineattributesEntries[i][0])
					.append(Util.bind("classfileformat.linenumbertableto")) //$NON-NLS-1$
					.append(lineattributesEntries[i][1])
					.append(Util.bind("classfileformat.linenumbertableclose")); //$NON-NLS-1$
				writeNewLine(buffer, lineSeparator, tabNumber + 1);
			}
		} 
		ILocalVariableAttribute localVariableAttribute = codeAttribute.getLocalVariableAttribute();
		int localVariableAttributeLength = localVariableAttribute == null ? 0 : localVariableAttribute.getLocalVariableTableLength();
		if (localVariableAttributeLength != 0) {
			writeNewLine(buffer, lineSeparator, tabNumber);
			buffer.append(Util.bind("disassembler.localvariabletableattributeheader")); //$NON-NLS-1$
			writeNewLine(buffer, lineSeparator, tabNumber + 1);
			ILocalVariableTableEntry[] localVariableTableEntries = localVariableAttribute.getLocalVariableTable();
			for (int i = 0; i < localVariableAttributeLength; i++) {
				ILocalVariableTableEntry localVariableTableEntry = localVariableTableEntries[i];
				int startPC = localVariableTableEntry.getStartPC();
				int length  = localVariableTableEntry.getLength();
				buffer
					.append(Util.bind("classfileformat.localvariabletablefrom")) //$NON-NLS-1$
					.append(startPC)
					.append(Util.bind("classfileformat.localvariabletableto")) //$NON-NLS-1$
					.append(startPC + length)
					.append(Util.bind("classfileformat.localvariabletablelocalname")) //$NON-NLS-1$
					.append(localVariableTableEntry.getName())
					.append(Util.bind("classfileformat.localvariabletablelocalindex")) //$NON-NLS-1$
					.append(localVariableTableEntry.getIndex())
					.append(Util.bind("classfileformat.localvariabletablelocaltype")) //$NON-NLS-1$
					.append(Signature.toCharArray(localVariableTableEntry.getDescriptor()));
				writeNewLine(buffer, lineSeparator, tabNumber + 1);
			}
		} 
	}

	private char[][] getParameterNames(char[] methodDescriptor) {
		int paramCount = Signature.getParameterCount(methodDescriptor);
		char[][] parameterNames = new char[paramCount][];
		for (int i = 0; i < paramCount; i++) {
			parameterNames[i] = Util.bind("disassembler.parametername").toCharArray(); //$NON-NLS-1$
		}
		return parameterNames;
	}
	
	private final void decodeModifiersForType(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		if ((accessFlags & IModifierConstants.ACC_ABSTRACT) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_abstract")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_FINAL) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_final")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PUBLIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_public")); //$NON-NLS-1$
		}
		if (!firstModifier) {
			buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		}
	}

	private final void decodeModifiersForInnerClasses(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		if ((accessFlags & IModifierConstants.ACC_PUBLIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_public")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PRIVATE) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_private")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PROTECTED) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_protected")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_STATIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_static")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_FINAL) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_final")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_ABSTRACT) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_abstract")); //$NON-NLS-1$
		}
		if (!firstModifier) {
			buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		}
	}

	private final void decodeModifiersForMethod(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		if ((accessFlags & IModifierConstants.ACC_ABSTRACT) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_abstract")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_FINAL) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_final")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_NATIVE) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_native")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PRIVATE) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_private")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PROTECTED) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_protected")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PUBLIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_public")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_STATIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_static")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_STRICT) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_strict")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_SYNCHRONIZED) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_synchronized")); //$NON-NLS-1$
		}
		if (!firstModifier) {
			buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		}
	}

	private void decodeModifiersForField(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		if ((accessFlags & IModifierConstants.ACC_FINAL) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_final")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PRIVATE) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_private")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PROTECTED) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_protected")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_PUBLIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_public")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_STATIC) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_static")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_TRANSIENT) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_transient")); //$NON-NLS-1$
		}
		if ((accessFlags & IModifierConstants.ACC_VOLATILE) != 0) {
			if (!firstModifier) {
				buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(Util.bind("classfileformat.acc_volatile")); //$NON-NLS-1$
		}
		if (!firstModifier) {
			buffer.append(Util.bind("disassembler.space")); //$NON-NLS-1$
		}
	}	 
}
