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
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.*;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

/**
 * Disassembler of .class files. It generates an output in the Writer that looks close to
 * the javap output.
 */
public class Disassembler extends ClassFileBytesDisassembler {

	private static final char[] EMPTY_CLASS_NAME = new char[0];
	private static final char[] ANY_EXCEPTION = Messages.classfileformat_anyexceptionhandler.toCharArray();	 
	private static final String EMPTY_OUTPUT = ""; //$NON-NLS-1$
	private static final String VERSION_UNKNOWN = Messages.classfileformat_versionUnknown;

	private boolean appendModifier(StringBuffer buffer, int accessFlags, int modifierConstant, String modifier, boolean firstModifier) {
		if ((accessFlags & modifierConstant) != 0) {		
			if (!firstModifier) {
				buffer.append(Messages.disassembler_space); 
			}
			if (firstModifier) {
				firstModifier = false;
			}
			buffer.append(modifier);
		}
		return firstModifier;
	}
	
	private void decodeModifiersForField(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PUBLIC, "public", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PROTECTED, "protected", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PRIVATE, "private", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_STATIC, "static", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_FINAL, "final", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_TRANSIENT, "transient", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_VOLATILE, "volatile", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_ENUM, "enum", firstModifier); //$NON-NLS-1$
		if (!firstModifier) {
			buffer.append(Messages.disassembler_space); 
		}
	}	

	private void decodeModifiersForFieldForWorkingCopy(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PUBLIC, "public", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PROTECTED, "protected", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PRIVATE, "private", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_STATIC, "static", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_FINAL, "final", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_TRANSIENT, "transient", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_VOLATILE, "volatile", firstModifier); //$NON-NLS-1$
		if (!firstModifier) {
			buffer.append(Messages.disassembler_space); 
		}
	}	
	
	private final void decodeModifiersForInnerClasses(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PUBLIC, "public", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PROTECTED, "protected", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PRIVATE, "private", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_ABSTRACT, "abstract", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_STATIC, "static", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_FINAL, "final", firstModifier); //$NON-NLS-1$
		if (!firstModifier) {
			buffer.append(Messages.disassembler_space); 
		}
	}

	private final void decodeModifiersForMethod(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PUBLIC, "public", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PROTECTED, "protected", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PRIVATE, "private", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_ABSTRACT, "abstract", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_STATIC, "static", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_FINAL, "final", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_SYNCHRONIZED, "synchronized", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_NATIVE, "native", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_STRICT, "strictfp", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_BRIDGE, "bridge", firstModifier); //$NON-NLS-1$
		if (!firstModifier) {
			buffer.append(Messages.disassembler_space); 
		}
	}

	private final void decodeModifiersForType(StringBuffer buffer, int accessFlags) {
		boolean firstModifier = true;
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_PUBLIC, "public", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_ABSTRACT, "abstract", firstModifier); //$NON-NLS-1$
		firstModifier = appendModifier(buffer, accessFlags, IModifierConstants.ACC_FINAL, "final", firstModifier); //$NON-NLS-1$
		if (!firstModifier) {
			buffer.append(Messages.disassembler_space); 
		}
	}

	private String decodeStringValue(char[] chars) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, max = chars.length; i < max; i++) {
			char c = chars[i];
			switch(c) {
				case '\b' :
					buffer.append("\\b"); //$NON-NLS-1$
					break;
				case '\t' :
					buffer.append("\\t"); //$NON-NLS-1$
					break;
				case '\n' :
					buffer.append("\\n"); //$NON-NLS-1$
					break;
				case '\f' :
					buffer.append("\\f"); //$NON-NLS-1$
					break;
				case '\r' :
					buffer.append("\\r"); //$NON-NLS-1$
					break;
				case '\"':
					buffer.append("\\\""); //$NON-NLS-1$
					break;
				case '\'':
					buffer.append("\\\'"); //$NON-NLS-1$
					break;
				case '\\':
					buffer.append("\\\\"); //$NON-NLS-1$
					break;
				case '\0' :
					buffer.append("\\0"); //$NON-NLS-1$
					break;
				case '\1' :
					buffer.append("\\1"); //$NON-NLS-1$
					break;
				case '\2' :
					buffer.append("\\2"); //$NON-NLS-1$
					break;
				case '\3' :
					buffer.append("\\3"); //$NON-NLS-1$
					break;
				case '\4' :
					buffer.append("\\4"); //$NON-NLS-1$
					break;
				case '\5' :
					buffer.append("\\5"); //$NON-NLS-1$
					break;
				case '\6' :
					buffer.append("\\6"); //$NON-NLS-1$
					break;
				case '\7' :
					buffer.append("\\7"); //$NON-NLS-1$
					break;			
				default:
					buffer.append(c);
			}
		}
		return buffer.toString();
	}

	private String decodeStringValue(String s) {
		return decodeStringValue(s.toCharArray());
	}

	/**
	 * @see org.eclipse.jdt.core.util.ClassFileBytesDisassembler#disassemble(byte[], java.lang.String)
	 */
	public String disassemble(byte[] classFileBytes, String lineSeparator) throws ClassFormatException {
		return disassemble(new ClassFileReader(classFileBytes, IClassFileReader.ALL), lineSeparator, ClassFileBytesDisassembler.DEFAULT);
	}

	/**
	 * @see org.eclipse.jdt.core.util.ClassFileBytesDisassembler#disassemble(byte[], java.lang.String, int)
	 */
	public String disassemble(byte[] classFileBytes, String lineSeparator, int mode) throws ClassFormatException {
		return disassemble(new ClassFileReader(classFileBytes, IClassFileReader.ALL), lineSeparator, mode);
	}

	private void disassemble(IAnnotation annotation, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		final int typeIndex = annotation.getTypeIndex();
		final char[] typeName = CharOperation.replaceOnCopy(annotation.getTypeName(), '/', '.');
		buffer.append(
			Messages.bind(Messages.disassembler_annotationentrystart, new String[] {
				Integer.toString(typeIndex),
				new String(Signature.toCharArray(typeName))
			}));
		final IAnnotationComponent[] components = annotation.getComponents();
		for (int i = 0, max = components.length; i < max; i++) {
			disassemble(components[i], buffer, lineSeparator, tabNumber + 1);
		}
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_annotationentryend); 
	}

	private void disassemble(IAnnotationComponent annotationComponent, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(
			Messages.bind(Messages.disassembler_annotationcomponent,
				new String[] {
					Integer.toString(annotationComponent.getComponentNameIndex()),
					new String(annotationComponent.getComponentName())
				}));
		disassemble(annotationComponent.getComponentValue(), buffer, lineSeparator, tabNumber + 1);
	}

	private void disassemble(IAnnotationComponentValue annotationComponentValue, StringBuffer buffer, String lineSeparator, int tabNumber) {
		switch(annotationComponentValue.getTag()) {
			case IAnnotationComponentValue.BYTE_TAG:
			case IAnnotationComponentValue.CHAR_TAG:
			case IAnnotationComponentValue.DOUBLE_TAG:
			case IAnnotationComponentValue.FLOAT_TAG:
			case IAnnotationComponentValue.INTEGER_TAG:
			case IAnnotationComponentValue.LONG_TAG:
			case IAnnotationComponentValue.SHORT_TAG:
			case IAnnotationComponentValue.BOOLEAN_TAG:
			case IAnnotationComponentValue.STRING_TAG:
				IConstantPoolEntry constantPoolEntry = annotationComponentValue.getConstantValue();
				String value = null;
				switch(constantPoolEntry.getKind()) {
					case IConstantPoolConstant.CONSTANT_Long :
						value = constantPoolEntry.getLongValue() + "L"; //$NON-NLS-1$
						break;
					case IConstantPoolConstant.CONSTANT_Float :
						value = constantPoolEntry.getFloatValue() + "f"; //$NON-NLS-1$
						break;
					case IConstantPoolConstant.CONSTANT_Double :
						value = Double.toString(constantPoolEntry.getDoubleValue());
						break;
					case IConstantPoolConstant.CONSTANT_Integer:
						switch(annotationComponentValue.getTag()) {
							case IAnnotationComponentValue.CHAR_TAG :
								value = "'" + (char) constantPoolEntry.getIntegerValue() + "'"; //$NON-NLS-1$//$NON-NLS-2$
								break;
							case IAnnotationComponentValue.BOOLEAN_TAG :
								value = constantPoolEntry.getIntegerValue() == 1 ? "true" : "false";//$NON-NLS-1$//$NON-NLS-2$
								break;
							case IAnnotationComponentValue.BYTE_TAG :
								value = "(byte) " + constantPoolEntry.getIntegerValue(); //$NON-NLS-1$
								break;
							case IAnnotationComponentValue.SHORT_TAG :
								value =  "(short) " + constantPoolEntry.getIntegerValue(); //$NON-NLS-1$
								break;
							case IAnnotationComponentValue.INTEGER_TAG :
								value =  "(int) " + constantPoolEntry.getIntegerValue(); //$NON-NLS-1$
						}
						break;
					case IConstantPoolConstant.CONSTANT_Utf8:
						value = "\"" + decodeStringValue(constantPoolEntry.getUtf8Value()) + "\"";//$NON-NLS-1$//$NON-NLS-2$
				}
				buffer.append(Messages.bind(Messages.disassembler_annotationdefaultvalue, value)); 
				break;
			case IAnnotationComponentValue.ENUM_TAG:
				final int enumConstantTypeNameIndex = annotationComponentValue.getEnumConstantTypeNameIndex();
				final char[] typeName = CharOperation.replaceOnCopy(annotationComponentValue.getEnumConstantTypeName(), '/', '.');
				final int enumConstantNameIndex = annotationComponentValue.getEnumConstantNameIndex();
				final char[] constantName = annotationComponentValue.getEnumConstantName();
				buffer.append(Messages.bind(Messages.disassembler_annotationenumvalue,
					new String[] {
						Integer.toString(enumConstantTypeNameIndex),
						Integer.toString(enumConstantNameIndex),
						new String(Signature.toCharArray(typeName)),
						new String(constantName)
					}));
				break;
			case IAnnotationComponentValue.CLASS_TAG:
				final int classIndex = annotationComponentValue.getClassInfoIndex();
				constantPoolEntry = annotationComponentValue.getClassInfo();
				final char[] className = CharOperation.replaceOnCopy(constantPoolEntry.getUtf8Value(), '/', '.');
				buffer.append(Messages.bind(Messages.disassembler_annotationclassvalue,
					new String[] {
						Integer.toString(classIndex),
						new String(Signature.toCharArray(className))
					}));
				break;
			case IAnnotationComponentValue.ANNOTATION_TAG:
				buffer.append(Messages.disassembler_annotationannotationvalue); 
				IAnnotation annotation = annotationComponentValue.getAnnotationValue();
				disassemble(annotation, buffer, lineSeparator, tabNumber + 1);
				break;
			case IAnnotationComponentValue.ARRAY_TAG:
				buffer.append(Messages.disassembler_annotationarrayvaluestart); 
				final IAnnotationComponentValue[] annotationComponentValues = annotationComponentValue.getAnnotationComponentValues();
				for (int i = 0, max = annotationComponentValues.length; i < max; i++) {
					writeNewLine(buffer, lineSeparator, tabNumber + 1);
					disassemble(annotationComponentValues[i], buffer, lineSeparator, tabNumber + 1);
				}
				writeNewLine(buffer, lineSeparator, tabNumber + 1);
				buffer.append(Messages.disassembler_annotationarrayvalueend); 
		}
	}
	
	private void disassemble(IAnnotationDefaultAttribute annotationDefaultAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_annotationdefaultheader); 
		IAnnotationComponentValue componentValue = annotationDefaultAttribute.getMemberValue();
		writeNewLine(buffer, lineSeparator, tabNumber + 2);
		disassemble(componentValue, buffer, lineSeparator, tabNumber + 1);
	}

	private void disassemble(IClassFileAttribute classFileAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.bind(Messages.disassembler_genericattributeheader,
			new String[] {
				new String(classFileAttribute.getAttributeName()),
				Long.toString(classFileAttribute.getAttributeLength())
			}));
	}

	private void disassembleEnumConstructor(IClassFileReader classFileReader, char[] className, IMethodInfo methodInfo, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		final ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		char[] methodDescriptor = methodInfo.getDescriptor();
		final IClassFileAttribute runtimeVisibleAnnotationsAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.RUNTIME_VISIBLE_ANNOTATIONS);
		final IClassFileAttribute runtimeInvisibleAnnotationsAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.RUNTIME_INVISIBLE_ANNOTATIONS);
		// disassemble compact version of annotations
		if (runtimeInvisibleAnnotationsAttribute != null) {
			disassembleAsModifier((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
			writeNewLine(buffer, lineSeparator, tabNumber);
		}
		if (runtimeVisibleAnnotationsAttribute != null) {
			disassembleAsModifier((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
			writeNewLine(buffer, lineSeparator, tabNumber);
		}
		final int accessFlags = methodInfo.getAccessFlags();
		decodeModifiersForMethod(buffer, accessFlags & IModifierConstants.ACC_PRIVATE);
		CharOperation.replace(methodDescriptor, '/', '.');
		final boolean isVarArgs = (accessFlags & IModifierConstants.ACC_VARARGS) != 0;
		final char[] signature = Signature.toCharArray(methodDescriptor, returnClassName(className, '.', COMPACT), getParameterNames(methodDescriptor, codeAttribute, accessFlags) , !checkMode(mode, COMPACT), false, isVarArgs);
		int index = CharOperation.indexOf(',', signature);
		index = CharOperation.indexOf(',', signature, index + 1);
		buffer.append(signature, 0, CharOperation.indexOf('(', signature) + 1);
		buffer.append(signature, index + 2, signature.length - index - 2);
		IExceptionAttribute exceptionAttribute = methodInfo.getExceptionAttribute();
		if (exceptionAttribute != null) {
			buffer.append(" throws "); //$NON-NLS-1$
			char[][] exceptionNames = exceptionAttribute.getExceptionNames();
			int length = exceptionNames.length;
			for (int i = 0; i < length - 1; i++) {
				char[] exceptionName = exceptionNames[i];
				CharOperation.replace(exceptionName, '/', '.');
				buffer
					.append(returnClassName(exceptionName, '.', mode))
					.append(Messages.disassembler_comma)
					.append(Messages.disassembler_space); 
			}
			char[] exceptionName = exceptionNames[length - 1];
			CharOperation.replace(exceptionName, '/', '.');
			buffer.append(returnClassName(exceptionName, '.', mode));
		}
		if (((accessFlags & IModifierConstants.ACC_NATIVE) == 0)
				&& ((accessFlags & IModifierConstants.ACC_ABSTRACT) == 0)) {
			buffer.append(" {"); //$NON-NLS-1$
			final char[] returnType = Signature.getReturnType(methodDescriptor);
			if (returnType.length == 1) {
				switch(returnType[0]) {
					case 'V' :
						writeNewLine(buffer, lineSeparator, tabNumber);							
						break;
					case 'I' :
					case 'B' :
					case 'J' :
					case 'D' :
					case 'F' :
					case 'S' :
					case 'C' :
						writeNewLine(buffer, lineSeparator, tabNumber + 1);
						buffer.append("return 0;"); //$NON-NLS-1$
						writeNewLine(buffer, lineSeparator, tabNumber);							
						break;
					default :
						// boolean
						writeNewLine(buffer, lineSeparator, tabNumber + 1);
						buffer.append("return false;"); //$NON-NLS-1$
						writeNewLine(buffer, lineSeparator, tabNumber);							
				}
			} else {
				// object
				writeNewLine(buffer, lineSeparator, tabNumber + 1);
				buffer.append("return null;"); //$NON-NLS-1$
				writeNewLine(buffer, lineSeparator, tabNumber);							
			}
			buffer.append('}');
		} else {
			buffer.append(';');
		}
	}
	
	/**
	 * Disassemble a method info header
	 */
	private void disassemble(IClassFileReader classFileReader, char[] className, IMethodInfo methodInfo, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		final ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		final char[] methodDescriptor = methodInfo.getDescriptor();
		final ISignatureAttribute signatureAttribute = (ISignatureAttribute) Util.getAttribute(methodInfo, IAttributeNamesConstants.SIGNATURE);
		final IClassFileAttribute runtimeVisibleAnnotationsAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.RUNTIME_VISIBLE_ANNOTATIONS);
		final IClassFileAttribute runtimeInvisibleAnnotationsAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.RUNTIME_INVISIBLE_ANNOTATIONS);
		final IClassFileAttribute runtimeVisibleParameterAnnotationsAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS);
		final IClassFileAttribute runtimeInvisibleParameterAnnotationsAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS);
		final IClassFileAttribute annotationDefaultAttribute = Util.getAttribute(methodInfo, IAttributeNamesConstants.ANNOTATION_DEFAULT);
		if (checkMode(mode, SYSTEM | DETAILED)) {
			buffer.append(Messages.bind(Messages.classfileformat_methoddescriptor,
				new String[] {
					Integer.toString(methodInfo.getDescriptorIndex()),
					new String(methodDescriptor)
				}));
			if (methodInfo.isDeprecated()) {
				buffer.append(Messages.disassembler_deprecated);
			}			
			writeNewLine(buffer, lineSeparator, tabNumber);
			if (signatureAttribute != null) {
				buffer.append(Messages.bind(Messages.disassembler_signatureattributeheader, new String(signatureAttribute.getSignature())));
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
			if (codeAttribute != null) {
				buffer.append(Messages.bind(Messages.classfileformat_stacksAndLocals,
					new String[] {
						Integer.toString(codeAttribute.getMaxStack()),
						Integer.toString(codeAttribute.getMaxLocals())
					}));
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
		}
		if (checkMode(mode, DETAILED)) {
			// disassemble compact version of annotations
			if (runtimeInvisibleAnnotationsAttribute != null) {
				disassembleAsModifier((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
			if (runtimeVisibleAnnotationsAttribute != null) {
				disassembleAsModifier((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
		}
		final int accessFlags = methodInfo.getAccessFlags();
		decodeModifiersForMethod(buffer, accessFlags);
		if (methodInfo.isSynthetic() && !checkMode(mode, WORKING_COPY)) {
			buffer.append("synthetic"); //$NON-NLS-1$
			buffer.append(Messages.disassembler_space); 
		}
		CharOperation.replace(methodDescriptor, '/', '.');
		final boolean isVarArgs = (accessFlags & IModifierConstants.ACC_VARARGS) != 0;
		if (methodInfo.isConstructor()) {
			if (checkMode(mode, WORKING_COPY) && signatureAttribute != null) {
				final char[] signature = signatureAttribute.getSignature();
				CharOperation.replace(signature, '/', '.');
				disassembleGenericSignature(mode, buffer, signature);
				buffer.append(' ');
				buffer.append(Signature.toCharArray(signature, returnClassName(className, '.', COMPACT), getParameterNames(methodDescriptor, codeAttribute, accessFlags) , !checkMode(mode, COMPACT), false, isVarArgs));
			} else {
				buffer.append(Signature.toCharArray(methodDescriptor, returnClassName(className, '.', COMPACT), getParameterNames(methodDescriptor, codeAttribute, accessFlags) , !checkMode(mode, COMPACT), false, isVarArgs));
			}
		} else if (methodInfo.isClinit()) {
			buffer.append(Messages.bind(Messages.classfileformat_clinitname));
		} else {
			if (checkMode(mode, WORKING_COPY) && signatureAttribute != null) {
				final char[] signature = signatureAttribute.getSignature();
				CharOperation.replace(signature, '/', '.');
				disassembleGenericSignature(mode, buffer, signature);
				buffer.append(' ');
				buffer.append(Signature.toCharArray(signature, methodInfo.getName(), getParameterNames(methodDescriptor, codeAttribute, accessFlags) , !checkMode(mode, COMPACT), true, isVarArgs));
			} else {
				buffer.append(Signature.toCharArray(methodDescriptor, methodInfo.getName(), getParameterNames(methodDescriptor, codeAttribute, accessFlags) , !checkMode(mode, COMPACT), true, isVarArgs));
			}
		}
		IExceptionAttribute exceptionAttribute = methodInfo.getExceptionAttribute();
		if (exceptionAttribute != null) {
			buffer.append(" throws "); //$NON-NLS-1$
			char[][] exceptionNames = exceptionAttribute.getExceptionNames();
			int length = exceptionNames.length;
			for (int i = 0; i < length - 1; i++) {
				char[] exceptionName = exceptionNames[i];
				CharOperation.replace(exceptionName, '/', '.');
				buffer
					.append(returnClassName(exceptionName, '.', mode))
					.append(Messages.disassembler_comma)
					.append(Messages.disassembler_space); 
			}
			char[] exceptionName = exceptionNames[length - 1];
			CharOperation.replace(exceptionName, '/', '.');
			buffer.append(returnClassName(exceptionName, '.', mode));
		}
		if (checkMode(mode, DETAILED)) {
			if (annotationDefaultAttribute != null) {
				buffer.append(" default "); //$NON-NLS-1$
				disassembleAsModifier((IAnnotationDefaultAttribute) annotationDefaultAttribute, buffer, lineSeparator, tabNumber, mode);
			}
		}
		if (checkMode(mode, WORKING_COPY)) {
			// put the annotation default attribute if needed
			if (annotationDefaultAttribute != null) {
				buffer.append(" default "); //$NON-NLS-1$
				disassembleAsModifier((IAnnotationDefaultAttribute) annotationDefaultAttribute, buffer, lineSeparator, tabNumber, mode);
			}
			if (((accessFlags & IModifierConstants.ACC_NATIVE) == 0)
					&& ((accessFlags & IModifierConstants.ACC_ABSTRACT) == 0)) {
				buffer.append(" {"); //$NON-NLS-1$
				final char[] returnType = Signature.getReturnType(methodDescriptor);
				if (returnType.length == 1) {
					switch(returnType[0]) {
						case 'V' :
							writeNewLine(buffer, lineSeparator, tabNumber);							
							break;
						case 'I' :
						case 'B' :
						case 'J' :
						case 'D' :
						case 'F' :
						case 'S' :
						case 'C' :
							writeNewLine(buffer, lineSeparator, tabNumber + 1);
							buffer.append("return 0;"); //$NON-NLS-1$
							writeNewLine(buffer, lineSeparator, tabNumber);							
							break;
						default :
							// boolean
							writeNewLine(buffer, lineSeparator, tabNumber + 1);
							buffer.append("return false;"); //$NON-NLS-1$
							writeNewLine(buffer, lineSeparator, tabNumber);							
					}
				} else {
					// object
					writeNewLine(buffer, lineSeparator, tabNumber + 1);
					buffer.append("return null;"); //$NON-NLS-1$
					writeNewLine(buffer, lineSeparator, tabNumber);							
				}
				buffer.append('}');
			} else {
				buffer.append(';');
			}	
		} else {
			buffer.append(Messages.disassembler_endofmethodheader);
		}
		
		if (checkMode(mode, SYSTEM | DETAILED)) {
			if (codeAttribute != null) {
				disassemble(codeAttribute, buffer, lineSeparator, tabNumber, mode);
			}
		}
		if (checkMode(mode, SYSTEM)) {
			IClassFileAttribute[] attributes = methodInfo.getAttributes();
			int length = attributes.length;
			if (length != 0) {
				for (int i = 0; i < length; i++) {
					IClassFileAttribute attribute = attributes[i];
					if (attribute != codeAttribute
							&& attribute != exceptionAttribute
							&& attribute != signatureAttribute
							&& attribute != annotationDefaultAttribute
							&& attribute != runtimeInvisibleAnnotationsAttribute
							&& attribute != runtimeVisibleAnnotationsAttribute
							&& attribute != runtimeInvisibleParameterAnnotationsAttribute
							&& attribute != runtimeVisibleParameterAnnotationsAttribute
							&& !CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.DEPRECATED)
							&& !CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.SYNTHETIC)) {
						disassemble(attribute, buffer, lineSeparator, tabNumber);
						writeNewLine(buffer, lineSeparator, tabNumber);
					}
				}
			}
			if (annotationDefaultAttribute != null) {
				disassemble((IAnnotationDefaultAttribute) annotationDefaultAttribute, buffer, lineSeparator, tabNumber);
			}
			if (runtimeVisibleAnnotationsAttribute != null) {
				disassemble((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber);
			}
			if (runtimeInvisibleAnnotationsAttribute != null) {
				disassemble((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber);
			}
			if (runtimeVisibleParameterAnnotationsAttribute != null) {
				disassemble((IRuntimeVisibleParameterAnnotationsAttribute) runtimeVisibleParameterAnnotationsAttribute, buffer, lineSeparator, tabNumber);
			}
			if (runtimeInvisibleParameterAnnotationsAttribute != null) {
				disassemble((IRuntimeInvisibleParameterAnnotationsAttribute) runtimeInvisibleParameterAnnotationsAttribute, buffer, lineSeparator, tabNumber);
			}
		}
	}

	/**
	 * @see #disassemble(org.eclipse.jdt.core.util.IClassFileReader, java.lang.String, int)
	 */
	public String disassemble(IClassFileReader classFileReader, String lineSeparator) {
		return disassemble(classFileReader, lineSeparator, ClassFileBytesDisassembler.DEFAULT);
	}

	/**
	 * Answers back the disassembled string of the IClassFileReader according to the
	 * mode.
	 * This is an output quite similar to the javap tool.
	 * 
	 * @param classFileReader The classFileReader to be disassembled
	 * @param lineSeparator the line separator to use.
	 * @param mode the mode used to disassemble the IClassFileReader
	 * 
	 * @return the disassembled string of the IClassFileReader according to the mode
	 */
	public String disassemble(IClassFileReader classFileReader, String lineSeparator, int mode) {
		if (classFileReader == null) return EMPTY_OUTPUT;
		char[] className = classFileReader.getClassName();
		if (className == null) {
			// incomplete initialization. We cannot go further.
			return EMPTY_OUTPUT;
		}
		CharOperation.replace(className, '/', '.');
		final int classNameLength = className.length;
		final int accessFlags = classFileReader.getAccessFlags();
		final boolean isEnum = (accessFlags & IModifierConstants.ACC_ENUM) != 0;

		StringBuffer buffer = new StringBuffer();
		ISourceAttribute sourceAttribute = classFileReader.getSourceFileAttribute();
		IClassFileAttribute classFileAttribute = Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		if (checkMode(mode, SYSTEM | DETAILED)) {
			int minorVersion = classFileReader.getMinorVersion();
			int majorVersion = classFileReader.getMajorVersion();
			buffer.append(Messages.disassembler_begincommentline); 
			if (sourceAttribute != null) {
				buffer.append(Messages.disassembler_sourceattributeheader); 
				buffer.append(sourceAttribute.getSourceFileName());
			}
			String versionNumber = VERSION_UNKNOWN;
			if (minorVersion == 3 && majorVersion == 45) {
				versionNumber = JavaCore.VERSION_1_1;
			} else if (minorVersion == 0 && majorVersion == 46) {
				versionNumber = JavaCore.VERSION_1_2;
			} else if (minorVersion == 0 && majorVersion == 47) {
				versionNumber = JavaCore.VERSION_1_3;
			} else if (minorVersion == 0 && majorVersion == 48) {
				versionNumber = JavaCore.VERSION_1_4;
			} else if (minorVersion == 0 && majorVersion == 49) {
				versionNumber = JavaCore.VERSION_1_5;
			} else if (minorVersion == 0 && majorVersion == 50) {
				versionNumber = JavaCore.VERSION_1_6;
			}
			buffer.append(
				Messages.bind(Messages.classfileformat_versiondetails,
				new String[] {
					versionNumber,
					Integer.toString(majorVersion),
					Integer.toString(minorVersion),
					((accessFlags & IModifierConstants.ACC_SUPER) != 0
							? Messages.classfileformat_superflagisset
							: Messages.classfileformat_superflagisnotset)
					+ (isDeprecated(classFileReader) ? ", deprecated" : EMPTY_OUTPUT)//$NON-NLS-1$
				}));
			writeNewLine(buffer, lineSeparator, 0);
			if (signatureAttribute != null) {
				buffer.append(Messages.bind(Messages.disassembler_signatureattributeheader, new String(signatureAttribute.getSignature()))); 
				writeNewLine(buffer, lineSeparator, 0);
			}
		}
		final int lastDotIndexInClassName = CharOperation.lastIndexOf('.', className);
		
		if (checkMode(mode, WORKING_COPY) && lastDotIndexInClassName != -1) { 
			// we print a package declaration
			buffer.append("package ");//$NON-NLS-1$
			buffer.append(className, 0, lastDotIndexInClassName);
			buffer.append(';');
			writeNewLine(buffer, lineSeparator, 0);
		}

		IInnerClassesAttribute innerClassesAttribute = classFileReader.getInnerClassesAttribute();
		IClassFileAttribute runtimeVisibleAnnotationsAttribute = Util.getAttribute(classFileReader, IAttributeNamesConstants.RUNTIME_VISIBLE_ANNOTATIONS);
		IClassFileAttribute runtimeInvisibleAnnotationsAttribute = Util.getAttribute(classFileReader, IAttributeNamesConstants.RUNTIME_INVISIBLE_ANNOTATIONS);
		
		if (checkMode(mode, DETAILED)) {
			// disassemble compact version of annotations
			if (runtimeInvisibleAnnotationsAttribute != null) {
				disassembleAsModifier((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, 1, mode);
				writeNewLine(buffer, lineSeparator, 0);
			}
			if (runtimeVisibleAnnotationsAttribute != null) {
				disassembleAsModifier((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, 1, mode);
				writeNewLine(buffer, lineSeparator, 0);
			}
		}
		boolean decoded = false;
		if (isEnum && checkMode(mode, WORKING_COPY)) {
			decodeModifiersForType(buffer, accessFlags & IModifierConstants.ACC_PUBLIC);
		} else {
			if (innerClassesAttribute != null) {
				// search the right entry
				IInnerClassesAttributeEntry[] entries = innerClassesAttribute.getInnerClassAttributesEntries();
				for (int i = 0, max = entries.length; i < max ; i++) {
					IInnerClassesAttributeEntry entry = entries[i];
					char[] innerClassName = entry.getInnerClassName();
					if (innerClassName != null) {
						if (CharOperation.equals(classFileReader.getClassName(), innerClassName)) {
							decodeModifiersForInnerClasses(buffer, entry.getAccessFlags());
							decoded = true;
						}
					}
				}
			}
			if (!decoded) {
				decodeModifiersForType(buffer, accessFlags);
				if (isSynthetic(classFileReader)) {
					buffer.append("synthetic"); //$NON-NLS-1$
					buffer.append(Messages.disassembler_space); 
				}
			}
		}
		
		final boolean isAnnotation = (accessFlags & IModifierConstants.ACC_ANNOTATION) != 0;
		if (isEnum) {
			buffer.append("enum "); //$NON-NLS-1$
		} else if (classFileReader.isClass()) {
			buffer.append("class "); //$NON-NLS-1$
		} else {
			if (isAnnotation) {
				buffer.append("@"); //$NON-NLS-1$
			}
			buffer.append("interface "); //$NON-NLS-1$
		}
		
		if (checkMode(mode, WORKING_COPY)) {
			// we print the simple class name
			final int start = lastDotIndexInClassName + 1;
			buffer.append(className, start, classNameLength - start);
			className = CharOperation.subarray(className, start, classNameLength);
			if (signatureAttribute != null) {
				disassembleGenericSignature(mode, buffer, signatureAttribute.getSignature());
			}
		} else {
			buffer.append(className);
		}
		
		char[] superclassName = classFileReader.getSuperclassName();
		if (superclassName != null) {
			CharOperation.replace(superclassName, '/', '.');
			if (!isJavaLangObject(superclassName) && !isEnum) {
				buffer.append(" extends "); //$NON-NLS-1$
				buffer.append(returnClassName(superclassName, '.', mode));
			}
		}
		if (!isAnnotation || !checkMode(mode, WORKING_COPY)) {
			char[][] superclassInterfaces = classFileReader.getInterfaceNames();
			int length = superclassInterfaces.length;
			if (length != 0) {
				buffer.append(" implements "); //$NON-NLS-1$
				for (int i = 0; i < length - 1; i++) {
					char[] superinterface = superclassInterfaces[i];
					CharOperation.replace(superinterface, '/', '.');
					buffer
						.append(returnClassName(superinterface, '.', mode))
						.append(Messages.disassembler_comma)
						.append(Messages.disassembler_space); 
				}
				char[] superinterface = superclassInterfaces[length - 1];
				CharOperation.replace(superinterface, '/', '.');
				buffer.append(returnClassName(superinterface, '.', mode));
			}
		}
		buffer.append(Messages.bind(Messages.disassembler_opentypedeclaration)); 
		if (checkMode(mode, SYSTEM)) {
			disassemble(classFileReader.getConstantPool(), buffer, lineSeparator, 1);
		}
		disassembleTypeMembers(classFileReader, className, buffer, lineSeparator, 1, mode, isEnum);
		if (checkMode(mode, SYSTEM | DETAILED)) {
			IClassFileAttribute[] attributes = classFileReader.getAttributes();
			int length = attributes.length;
			IEnclosingMethodAttribute enclosingMethodAttribute = getEnclosingMethodAttribute(classFileReader);
			int remainingAttributesLength = length;
			if (innerClassesAttribute != null) {
				remainingAttributesLength--;
			}
			if (enclosingMethodAttribute != null) {
				remainingAttributesLength--;
			}
			if (sourceAttribute != null) {
				remainingAttributesLength--;
			}
			if (signatureAttribute != null) {
				remainingAttributesLength--;
			}
			if (innerClassesAttribute != null || enclosingMethodAttribute != null || remainingAttributesLength != 0) {
				writeNewLine(buffer, lineSeparator, 0);
			}
			if (innerClassesAttribute != null) {
				disassemble(innerClassesAttribute, buffer, lineSeparator, 1);
			}
			if (enclosingMethodAttribute != null) {
				disassemble(enclosingMethodAttribute, buffer, lineSeparator, 0);
			}
			if (checkMode(mode, SYSTEM)) {
				if (runtimeVisibleAnnotationsAttribute != null) {
					disassemble((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, 0);
				}
				if (runtimeInvisibleAnnotationsAttribute != null) {
					disassemble((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, 0);
				}
				if (length != 0) {
					for (int i = 0; i < length; i++) {
						IClassFileAttribute attribute = attributes[i];
						if (attribute != innerClassesAttribute
							&& attribute != sourceAttribute
							&& attribute != signatureAttribute
							&& attribute != enclosingMethodAttribute
							&& attribute != runtimeInvisibleAnnotationsAttribute
							&& attribute != runtimeVisibleAnnotationsAttribute
							&& !CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.DEPRECATED)
							&& !CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.SYNTHETIC)) {
							disassemble(attribute, buffer, lineSeparator, 0);
						}
					}
				}
			}
		}
		writeNewLine(buffer, lineSeparator, 0);
		buffer.append(Messages.disassembler_closetypedeclaration); 
		return buffer.toString();
	}

	private void disassembleGenericSignature(int mode, StringBuffer buffer, final char[] signature) {
		CharOperation.replace(signature, '/', '.');
		final char[][] typeParameters = Signature.getTypeParameters(signature);
		final int typeParametersLength = typeParameters.length;
		if (typeParametersLength != 0) {
			buffer.append('<');
			for (int i = 0; i < typeParametersLength; i++) {
				// extract the name
				buffer.append(typeParameters[i], 0, CharOperation.indexOf(':', typeParameters[i]));
				final char[][] bounds = Signature.getTypeParameterBounds(typeParameters[i]);
				final int boundsLength = bounds.length;
				if (boundsLength != 0) {
					if (boundsLength == 1) {
						final char[] bound = bounds[0];
						// check if this is java.lang.Object
						if (!isJavaLangObject(Signature.toCharArray(bound))) {
							buffer.append(" extends "); //$NON-NLS-1$
							buffer.append(returnClassName(Signature.toCharArray(bound), '.', mode));
						}
					} else {
						buffer.append(" extends "); //$NON-NLS-1$
						for (int j= 0; j < boundsLength - 1; j++) {
							buffer.append(returnClassName(Signature.toCharArray(bounds[j]), '.', mode));
							buffer.append(" & "); //$NON-NLS-1$
							}
						buffer.append(returnClassName(Signature.toCharArray(bounds[boundsLength - 1]), '.', mode));
					}
				}	
				if (i < typeParametersLength - 1) {
					buffer.append(',');
				}
			}
			buffer.append('>');
		}
	}

	private boolean isJavaLangObject(final char[] className) {
		return CharOperation.equals(TypeConstants.JAVA_LANG_OBJECT, CharOperation.splitOn('.', className));
	}
	
	private void disassemble(ICodeAttribute codeAttribute, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		writeNewLine(buffer, lineSeparator, tabNumber - 1);
		DefaultBytecodeVisitor visitor = new DefaultBytecodeVisitor(codeAttribute, buffer, lineSeparator, tabNumber, mode);
		try {
			codeAttribute.traverse(visitor);
		} catch(ClassFormatException e) {
			dumpTab(tabNumber + 2, buffer);
			buffer.append(Messages.classformat_classformatexception);
			writeNewLine(buffer, lineSeparator, tabNumber + 1);
		}
		final int exceptionTableLength = codeAttribute.getExceptionTableLength();
		boolean isFirstAttribute = true;
		if (exceptionTableLength != 0) {
			final int tabNumberForExceptionAttribute = tabNumber + 2;
			isFirstAttribute = false;
			dumpTab(tabNumberForExceptionAttribute, buffer);
			final IExceptionTableEntry[] exceptionTableEntries = codeAttribute.getExceptionTable();
			buffer.append(Messages.disassembler_exceptiontableheader); 
			writeNewLine(buffer, lineSeparator, tabNumberForExceptionAttribute + 1);
			for (int i = 0; i < exceptionTableLength - 1; i++) {
				IExceptionTableEntry exceptionTableEntry = exceptionTableEntries[i];
				char[] catchType;
				if (exceptionTableEntry.getCatchTypeIndex() != 0) {
					catchType = exceptionTableEntry.getCatchType();
					CharOperation.replace(catchType, '/', '.');
				} else {
					catchType = ANY_EXCEPTION;
				}
				buffer.append(Messages.bind(Messages.classfileformat_exceptiontableentry,
					new String[] {
						Integer.toString(exceptionTableEntry.getStartPC()),
						Integer.toString(exceptionTableEntry.getEndPC()),
						Integer.toString(exceptionTableEntry.getHandlerPC()),
						new String(catchType)
					}));
				writeNewLine(buffer, lineSeparator, tabNumberForExceptionAttribute + 1);
			}
			IExceptionTableEntry exceptionTableEntry = exceptionTableEntries[exceptionTableLength - 1];
			char[] catchType;
			if (exceptionTableEntry.getCatchTypeIndex() != 0) {
				catchType = exceptionTableEntry.getCatchType();
				CharOperation.replace(catchType, '/', '.');
			} else {
				catchType = ANY_EXCEPTION;
			}
			buffer.append(Messages.bind(Messages.classfileformat_exceptiontableentry,
				new String[] {
					Integer.toString(exceptionTableEntry.getStartPC()),
					Integer.toString(exceptionTableEntry.getEndPC()),
					Integer.toString(exceptionTableEntry.getHandlerPC()),
					new String(catchType)
				}));
		}
		final ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
		final int lineAttributeLength = lineNumberAttribute == null ? 0 : lineNumberAttribute.getLineNumberTableLength();
		if (lineAttributeLength != 0) {
			int tabNumberForLineAttribute = tabNumber + 2;
			if (!isFirstAttribute) {
				writeNewLine(buffer, lineSeparator, tabNumberForLineAttribute);
			} else {
				dumpTab(tabNumberForLineAttribute, buffer);
				isFirstAttribute = false;
			}
			buffer.append(Messages.disassembler_linenumberattributeheader); 
			writeNewLine(buffer, lineSeparator, tabNumberForLineAttribute + 1);
			int[][] lineattributesEntries = lineNumberAttribute.getLineNumberTable();
			for (int i = 0; i < lineAttributeLength - 1; i++) {
				buffer.append(Messages.bind(Messages.classfileformat_linenumbertableentry,
					new String[] {
						Integer.toString(lineattributesEntries[i][0]),
						Integer.toString(lineattributesEntries[i][1])
					}));
				writeNewLine(buffer, lineSeparator, tabNumberForLineAttribute + 1);
			}
			buffer.append(Messages.bind(Messages.classfileformat_linenumbertableentry,
				new String[] {
					Integer.toString(lineattributesEntries[lineAttributeLength - 1][0]),
					Integer.toString(lineattributesEntries[lineAttributeLength - 1][1])
				}));
		} 
		final ILocalVariableAttribute localVariableAttribute = codeAttribute.getLocalVariableAttribute();
		final int localVariableAttributeLength = localVariableAttribute == null ? 0 : localVariableAttribute.getLocalVariableTableLength();
		if (localVariableAttributeLength != 0) {
			int tabNumberForLocalVariableAttribute = tabNumber + 2;
			if (!isFirstAttribute) {
				writeNewLine(buffer, lineSeparator, tabNumberForLocalVariableAttribute);
			} else {
				isFirstAttribute = false;
				dumpTab(tabNumberForLocalVariableAttribute, buffer);
			}
			buffer.append(Messages.disassembler_localvariabletableattributeheader); 
			writeNewLine(buffer, lineSeparator, tabNumberForLocalVariableAttribute + 1);
			ILocalVariableTableEntry[] localVariableTableEntries = localVariableAttribute.getLocalVariableTable();
			for (int i = 0; i < localVariableAttributeLength - 1; i++) {
				ILocalVariableTableEntry localVariableTableEntry = localVariableTableEntries[i];
				int index= localVariableTableEntry.getIndex();
				int startPC = localVariableTableEntry.getStartPC();
				int length  = localVariableTableEntry.getLength();
				final char[] typeName = Signature.toCharArray(localVariableTableEntry.getDescriptor());
				CharOperation.replace(typeName, '/', '.');
				buffer.append(Messages.bind(Messages.classfileformat_localvariabletableentry,
					new String[] {
						Integer.toString(startPC),
						Integer.toString(startPC + length),
						new String(localVariableTableEntry.getName()),
						Integer.toString(index),
						new String(returnClassName(typeName, '.', mode))
					}));
				writeNewLine(buffer, lineSeparator, tabNumberForLocalVariableAttribute + 1);
			}
			ILocalVariableTableEntry localVariableTableEntry = localVariableTableEntries[localVariableAttributeLength - 1];
			int index= localVariableTableEntry.getIndex();
			int startPC = localVariableTableEntry.getStartPC();
			int length  = localVariableTableEntry.getLength();
			final char[] typeName = Signature.toCharArray(localVariableTableEntry.getDescriptor());
			CharOperation.replace(typeName, '/', '.');
			buffer.append(Messages.bind(Messages.classfileformat_localvariabletableentry,
				new String[] {
					Integer.toString(startPC),
					Integer.toString(startPC + length),
					new String(localVariableTableEntry.getName()),
					Integer.toString(index),
					new String(returnClassName(typeName, '.', mode))
				}));
		}
		final ILocalVariableTypeTableAttribute localVariableTypeAttribute= (ILocalVariableTypeTableAttribute) getAttribute(IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE, codeAttribute);
		final int localVariableTypeTableLength = localVariableTypeAttribute == null ? 0 : localVariableTypeAttribute.getLocalVariableTypeTableLength();
		if (localVariableTypeTableLength != 0) {
			int tabNumberForLocalVariableAttribute = tabNumber + 2;
			if (!isFirstAttribute) {
				writeNewLine(buffer, lineSeparator, tabNumberForLocalVariableAttribute);
			} else {
				isFirstAttribute = false;
				dumpTab(tabNumberForLocalVariableAttribute, buffer);
			}
			buffer.append(Messages.disassembler_localvariabletypetableattributeheader); 
			writeNewLine(buffer, lineSeparator, tabNumberForLocalVariableAttribute + 1);
			ILocalVariableTypeTableEntry[] localVariableTypeTableEntries = localVariableTypeAttribute.getLocalVariableTypeTable();
			for (int i = 0; i < localVariableTypeTableLength - 1; i++) {
				ILocalVariableTypeTableEntry localVariableTypeTableEntry = localVariableTypeTableEntries[i];
				int index= localVariableTypeTableEntry.getIndex();
				int startPC = localVariableTypeTableEntry.getStartPC();
				int length  = localVariableTypeTableEntry.getLength();
				final char[] typeName = Signature.toCharArray(localVariableTypeTableEntry.getSignature());
				CharOperation.replace(typeName, '/', '.');
				buffer.append(Messages.bind(Messages.classfileformat_localvariabletableentry,
					new String[] {
						Integer.toString(startPC),
						Integer.toString(startPC + length),
						new String(localVariableTypeTableEntry.getName()),
						Integer.toString(index),
						new String(returnClassName(typeName, '.', mode))
					}));
				writeNewLine(buffer, lineSeparator, tabNumberForLocalVariableAttribute + 1);
			}
			ILocalVariableTypeTableEntry localVariableTypeTableEntry = localVariableTypeTableEntries[localVariableTypeTableLength - 1];
			int index= localVariableTypeTableEntry.getIndex();
			int startPC = localVariableTypeTableEntry.getStartPC();
			int length  = localVariableTypeTableEntry.getLength();
			final char[] typeName = Signature.toCharArray(localVariableTypeTableEntry.getSignature());
			CharOperation.replace(typeName, '/', '.');
			buffer.append(Messages.bind(Messages.classfileformat_localvariabletableentry,
				new String[] {
					Integer.toString(startPC),
					Integer.toString(startPC + length),
					new String(localVariableTypeTableEntry.getName()),
					Integer.toString(index),
					new String(returnClassName(typeName, '.', mode))
				}));
		}
		final int length = codeAttribute.getAttributesCount();
		if (length != 0) {
			IClassFileAttribute[] attributes = codeAttribute.getAttributes();
			for (int i = 0; i < length; i++) {
				IClassFileAttribute attribute = attributes[i];
				if (CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.STACK_MAP_TABLE)) {
					IStackMapTableAttribute stackMapTableAttribute = (IStackMapTableAttribute) attribute;
					if (!isFirstAttribute) {
						writeNewLine(buffer, lineSeparator, tabNumber + 2);
					} else {
						isFirstAttribute = false;
						dumpTab(tabNumber + 1, buffer);
					}
					int numberOfEntries = stackMapTableAttribute.getNumberOfEntries();
					buffer.append(Messages.bind(Messages.disassembler_stackmaptableattributeheader, Integer.toString(numberOfEntries)));
					if (numberOfEntries != 0) {
						writeNewLine(buffer, lineSeparator, tabNumber + 3);
						final IStackMapFrame[] stackMapFrames = stackMapTableAttribute.getStackMapFrame();
						for (int j = 0; j < numberOfEntries; j++) {
							disassemble(stackMapFrames[j], buffer, lineSeparator, tabNumber + 2, mode);
						}
					}
				} else if (attribute != lineNumberAttribute
						&& attribute != localVariableAttribute
						&& attribute != localVariableTypeAttribute) {
					if (!isFirstAttribute) {
						writeNewLine(buffer, lineSeparator, tabNumber + 2);
					} else {
						isFirstAttribute = false;
						dumpTab(tabNumber + 1, buffer);
					}
					buffer.append(Messages.bind(Messages.disassembler_genericattributeheader,
						new String[] {
							new String(attribute.getAttributeName()),
							Long.toString(attribute.getAttributeLength())
						}));
				}
			}
		}		
	}

	private void disassemble(IConstantPool constantPool, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		int length = constantPool.getConstantPoolCount();
		buffer.append(Messages.disassembler_constantpoolheader); 
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		for (int i = 1; i < length; i++) {
			IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(i);
			switch (constantPool.getEntryKind(i)) {
				case IConstantPoolConstant.CONSTANT_Class :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_class,
							new String[] {
								Integer.toString(i),
								Integer.toString(constantPoolEntry.getClassInfoNameIndex()),
								new String(constantPoolEntry.getClassInfoName())}));
					break;
				case IConstantPoolConstant.CONSTANT_Double :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_double,
							new String[] {
								Integer.toString(i),
								Double.toString(constantPoolEntry.getDoubleValue())}));
					break;
				case IConstantPoolConstant.CONSTANT_Fieldref :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_fieldref,
							new String[] {
								Integer.toString(i),
								Integer.toString(constantPoolEntry.getClassIndex()),
								Integer.toString(constantPoolEntry.getNameAndTypeIndex()),
								new String(constantPoolEntry.getClassName()),
								new String(constantPoolEntry.getFieldName()),
								new String(constantPoolEntry.getFieldDescriptor())
							}));
					break;
				case IConstantPoolConstant.CONSTANT_Float :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_float,
						new String[] {
							Integer.toString(i),
							Float.toString(constantPoolEntry.getFloatValue())}));
					break;
				case IConstantPoolConstant.CONSTANT_Integer :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_integer,
							new String[] {
								Integer.toString(i),
								Integer.toString(constantPoolEntry.getIntegerValue())}));
					break;
				case IConstantPoolConstant.CONSTANT_InterfaceMethodref :
					buffer.append(
							Messages.bind(Messages.disassembler_constantpool_interfacemethodref,
								new String[] {
									Integer.toString(i),
									Integer.toString(constantPoolEntry.getClassIndex()),
									Integer.toString(constantPoolEntry.getNameAndTypeIndex()),
									new String(constantPoolEntry.getClassName()),
									new String(constantPoolEntry.getMethodName()),
									new String(constantPoolEntry.getMethodDescriptor())}));
					break;
				case IConstantPoolConstant.CONSTANT_Long :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_long,
							new String[] {
								Integer.toString(i),
								Long.toString(constantPoolEntry.getLongValue())}));
					break;
				case IConstantPoolConstant.CONSTANT_Methodref :
					buffer.append(
							Messages.bind(Messages.disassembler_constantpool_methodref,
								new String[] {
									Integer.toString(i),
									Integer.toString(constantPoolEntry.getClassIndex()),
									Integer.toString(constantPoolEntry.getNameAndTypeIndex()),
									new String(constantPoolEntry.getClassName()),
									new String(constantPoolEntry.getMethodName()),
									new String(constantPoolEntry.getMethodDescriptor())}));
					break;
				case IConstantPoolConstant.CONSTANT_NameAndType :
					int nameIndex = constantPoolEntry.getNameAndTypeInfoNameIndex();
					int typeIndex = constantPoolEntry.getNameAndTypeInfoDescriptorIndex();
					IConstantPoolEntry entry = constantPool.decodeEntry(nameIndex);
					char[] nameValue = entry.getUtf8Value();
					entry = constantPool.decodeEntry(typeIndex);
					char[] typeValue = entry.getUtf8Value();
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_name_and_type,
							new String[] {
								Integer.toString(i),
								Integer.toString(nameIndex),
								Integer.toString(typeIndex),
								String.valueOf(nameValue),
								String.valueOf(typeValue)}));
					break;
				case IConstantPoolConstant.CONSTANT_String :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_string,
							new String[] {
								Integer.toString(i),
								Integer.toString(constantPoolEntry.getStringIndex()),
								constantPoolEntry.getStringValue()}));
					break;
				case IConstantPoolConstant.CONSTANT_Utf8 :
					buffer.append(
						Messages.bind(Messages.disassembler_constantpool_utf8,
							new String[] {
								Integer.toString(i),
								new String(constantPoolEntry.getUtf8Value())}));
					break;
			}
			if (i < length - 1) {
				writeNewLine(buffer, lineSeparator, tabNumber + 1);
			}
		}
	}

	private void disassemble(IEnclosingMethodAttribute enclosingMethodAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_enclosingmethodheader); 
		buffer
			.append(Messages.disassembler_constantpoolindex) 
			.append(enclosingMethodAttribute.getEnclosingClassIndex())
			.append(" ")//$NON-NLS-1$
			.append(Messages.disassembler_constantpoolindex) 
			.append(enclosingMethodAttribute.getMethodNameAndTypeIndex())
			.append(" ")//$NON-NLS-1$
			.append(enclosingMethodAttribute.getEnclosingClass());
		if (enclosingMethodAttribute.getMethodNameAndTypeIndex() != 0) {
			buffer
				.append(".")//$NON-NLS-1$
				.append(enclosingMethodAttribute.getMethodName())
				.append(enclosingMethodAttribute.getMethodDescriptor());
		}
	}
	
	private void disassembleEnumConstants(IFieldInfo fieldInfo, StringBuffer buffer, String lineSeparator, int tabNumber, char[][] argumentTypes, int mode) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		final IClassFileAttribute runtimeVisibleAnnotationsAttribute = Util.getAttribute(fieldInfo, IAttributeNamesConstants.RUNTIME_VISIBLE_ANNOTATIONS);
		final IClassFileAttribute runtimeInvisibleAnnotationsAttribute = Util.getAttribute(fieldInfo, IAttributeNamesConstants.RUNTIME_INVISIBLE_ANNOTATIONS);
		// disassemble compact version of annotations
		if (runtimeInvisibleAnnotationsAttribute != null) {
			disassembleAsModifier((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
			writeNewLine(buffer, lineSeparator, tabNumber);
		}
		if (runtimeVisibleAnnotationsAttribute != null) {
			disassembleAsModifier((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
			writeNewLine(buffer, lineSeparator, tabNumber);
		}
		buffer.append(new String(fieldInfo.getName()));
		buffer.append('(');
		final int length = argumentTypes.length;
		if (length != 0) {
			// insert default value for corresponding argument types
			for (int i = 0; i < length; i++) {
				final char[] type = argumentTypes[i];
				switch(type.length) {
					case 1 :
						switch(type[0]) {
							case 'B' :
							case 'I' :
							case 'J' :
							case 'D' :
							case 'F' :
							case 'S' :
								buffer.append('0');
								break;
							case 'Z' :
								buffer.append("false"); //$NON-NLS-1$
								break;
							case 'C' :
								buffer.append("\' \'"); //$NON-NLS-1$
								break;
						}
						break;
					default :
						buffer.append("null"); //$NON-NLS-1$
				}
				if (i < length - 1) {
					buffer.append(',');
				}
			}
		}
		buffer.append(')').append(',');
	}
	
	/**
	 * Disassemble a field info
	 */
	private void disassemble(IFieldInfo fieldInfo, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		final char[] fieldDescriptor = fieldInfo.getDescriptor();
		final ISignatureAttribute signatureAttribute = (ISignatureAttribute) Util.getAttribute(fieldInfo, IAttributeNamesConstants.SIGNATURE);
		if (checkMode(mode, SYSTEM | DETAILED)) {
			buffer.append(Messages.bind(Messages.classfileformat_fieldddescriptor,
				new String[] {
					Integer.toString(fieldInfo.getDescriptorIndex()),
					new String(fieldDescriptor)
				}));
			if (fieldInfo.isDeprecated()) {
				buffer.append(Messages.disassembler_deprecated);
			}
			writeNewLine(buffer, lineSeparator, tabNumber);
			if (signatureAttribute != null) {
				buffer.append(Messages.bind(Messages.disassembler_signatureattributeheader, new String(signatureAttribute.getSignature()))); 
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
		}
		final IClassFileAttribute runtimeVisibleAnnotationsAttribute = Util.getAttribute(fieldInfo, IAttributeNamesConstants.RUNTIME_VISIBLE_ANNOTATIONS);
		final IClassFileAttribute runtimeInvisibleAnnotationsAttribute = Util.getAttribute(fieldInfo, IAttributeNamesConstants.RUNTIME_INVISIBLE_ANNOTATIONS);
		if (checkMode(mode, DETAILED)) {
			// disassemble compact version of annotations
			if (runtimeInvisibleAnnotationsAttribute != null) {
				disassembleAsModifier((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
			if (runtimeVisibleAnnotationsAttribute != null) {
				disassembleAsModifier((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber + 1, mode);
				writeNewLine(buffer, lineSeparator, tabNumber);
			}
		}
		if (checkMode(mode, WORKING_COPY)) {
			decodeModifiersForFieldForWorkingCopy(buffer, fieldInfo.getAccessFlags());
			if (signatureAttribute != null) {
				buffer.append(returnClassName(getSignatureForField(signatureAttribute.getSignature()), '.', mode));
			} else {
				buffer.append(returnClassName(getSignatureForField(fieldDescriptor), '.', mode));
			}
		} else {
			decodeModifiersForField(buffer, fieldInfo.getAccessFlags());
			if (fieldInfo.isSynthetic()) {
				buffer.append("synthetic"); //$NON-NLS-1$
				buffer.append(Messages.disassembler_space);
			}
			buffer.append(returnClassName(getSignatureForField(fieldDescriptor), '.', mode));
		}
		buffer.append(' ');
		buffer.append(new String(fieldInfo.getName()));
		IConstantValueAttribute constantValueAttribute = fieldInfo.getConstantValueAttribute();
		if (constantValueAttribute != null) {
			buffer.append(Messages.disassembler_fieldhasconstant); 
			IConstantPoolEntry constantPoolEntry = constantValueAttribute.getConstantValue();
			switch(constantPoolEntry.getKind()) {
				case IConstantPoolConstant.CONSTANT_Long :
					buffer.append(constantPoolEntry.getLongValue() + "L"); //$NON-NLS-1$
					break;
				case IConstantPoolConstant.CONSTANT_Float :
					buffer.append(constantPoolEntry.getFloatValue() + "f"); //$NON-NLS-1$
					break;
				case IConstantPoolConstant.CONSTANT_Double :
					final double doubleValue = constantPoolEntry.getDoubleValue();
					if (checkMode(mode, ClassFileBytesDisassembler.WORKING_COPY)) {
						if (doubleValue == Double.POSITIVE_INFINITY) {
							buffer.append("1.0 / 0.0"); //$NON-NLS-1$
						} else if (doubleValue == Double.NEGATIVE_INFINITY) {
							buffer.append("-1.0 / 0.0"); //$NON-NLS-1$
						} else {
							buffer.append(constantPoolEntry.getDoubleValue());
						}
					} else {
						buffer.append(constantPoolEntry.getDoubleValue());
					}
					break;
				case IConstantPoolConstant.CONSTANT_Integer:
					switch(fieldDescriptor[0]) {
						case 'C' :
							buffer.append("'" + (char) constantPoolEntry.getIntegerValue() + "'"); //$NON-NLS-1$//$NON-NLS-2$
							break;
						case 'Z' :
							buffer.append(constantPoolEntry.getIntegerValue() == 1 ? "true" : "false");//$NON-NLS-1$//$NON-NLS-2$
							break;
						case 'B' :
							buffer.append(constantPoolEntry.getIntegerValue());
							break;
						case 'S' :
							buffer.append(constantPoolEntry.getIntegerValue());
							break;
						case 'I' :
							buffer.append(constantPoolEntry.getIntegerValue());
					}
					break;
				case IConstantPoolConstant.CONSTANT_String:
					buffer.append("\"" + decodeStringValue(constantPoolEntry.getStringValue()) + "\"" );//$NON-NLS-1$//$NON-NLS-2$
			}
		}
		buffer.append(Messages.disassembler_endoffieldheader); 
		if (checkMode(mode, SYSTEM)) {
			IClassFileAttribute[] attributes = fieldInfo.getAttributes();
			int length = attributes.length;
			if (length != 0) {
				for (int i = 0; i < length; i++) {
					IClassFileAttribute attribute = attributes[i];
					if (attribute != constantValueAttribute
						&& attribute != signatureAttribute
						&& attribute != runtimeInvisibleAnnotationsAttribute
						&& attribute != runtimeVisibleAnnotationsAttribute
						&& !CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.DEPRECATED)
						&& !CharOperation.equals(attribute.getAttributeName(), IAttributeNamesConstants.SYNTHETIC)) {
						disassemble(attribute, buffer, lineSeparator, tabNumber);
					}
				}
			}
			if (runtimeVisibleAnnotationsAttribute != null) {
				disassemble((IRuntimeVisibleAnnotationsAttribute) runtimeVisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber);
			}
			if (runtimeInvisibleAnnotationsAttribute != null) {
				disassemble((IRuntimeInvisibleAnnotationsAttribute) runtimeInvisibleAnnotationsAttribute, buffer, lineSeparator, tabNumber);
			}
		}
	}
	
	private void disassemble(IInnerClassesAttribute innerClassesAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber);
		buffer.append(Messages.disassembler_innerattributesheader); 
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
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
				.append(Messages.disassembler_openinnerclassentry) 
				.append(Messages.disassembler_inner_class_info_name) 
				.append(Messages.disassembler_constantpoolindex) 
				.append(innerClassNameIndex);
			if (innerClassNameIndex != 0) {
				buffer
					.append(Messages.disassembler_space) 
					.append(innerClassesAttributeEntry.getInnerClassName());
			}
			buffer
				.append(Messages.disassembler_comma) 
				.append(Messages.disassembler_space) 
				.append(Messages.disassembler_outer_class_info_name) 
				.append(Messages.disassembler_constantpoolindex) 
				.append(outerClassNameIndex);
			if (outerClassNameIndex != 0) {
				buffer	
					.append(Messages.disassembler_space) 
					.append(innerClassesAttributeEntry.getOuterClassName());
			}
			writeNewLine(buffer, lineSeparator, tabNumber);
			dumpTab(tabNumber, buffer);
			buffer.append(Messages.disassembler_space); 
			buffer
				.append(Messages.disassembler_inner_name) 
				.append(Messages.disassembler_constantpoolindex) 
				.append(innerNameIndex);
			if (innerNameIndex != 0) {
				buffer
					.append(Messages.disassembler_space) 
					.append(innerClassesAttributeEntry.getInnerName());
			}
			buffer
				.append(Messages.disassembler_comma) 
				.append(Messages.disassembler_space) 
				.append(Messages.disassembler_inner_accessflags) 
				.append(accessFlags)
				.append(Messages.disassembler_space); 
			decodeModifiersForInnerClasses(buffer, accessFlags);
			buffer
				.append(Messages.disassembler_closeinnerclassentry) 
				.append(Messages.disassembler_comma); 
			writeNewLine(buffer, lineSeparator, tabNumber + 1);
		}
		// last entry
		innerClassesAttributeEntry = innerClassesAttributeEntries[length - 1];
		innerClassNameIndex = innerClassesAttributeEntry.getInnerClassNameIndex();
		outerClassNameIndex = innerClassesAttributeEntry.getOuterClassNameIndex();
		innerNameIndex = innerClassesAttributeEntry.getInnerNameIndex();
		accessFlags = innerClassesAttributeEntry.getAccessFlags();
		buffer
			.append(Messages.disassembler_openinnerclassentry) 
			.append(Messages.disassembler_inner_class_info_name) 
			.append(Messages.disassembler_constantpoolindex) 
			.append(innerClassNameIndex);
		if (innerClassNameIndex != 0) {
			buffer
				.append(Messages.disassembler_space) 
				.append(innerClassesAttributeEntry.getInnerClassName());
		}
		buffer
			.append(Messages.disassembler_comma) 
			.append(Messages.disassembler_space) 
			.append(Messages.disassembler_outer_class_info_name) 
			.append(Messages.disassembler_constantpoolindex) 
			.append(outerClassNameIndex);
		if (outerClassNameIndex != 0) {
			buffer	
				.append(Messages.disassembler_space) 
				.append(innerClassesAttributeEntry.getOuterClassName());
		}
		writeNewLine(buffer, lineSeparator, tabNumber);
		dumpTab(tabNumber, buffer);
		buffer.append(Messages.disassembler_space); 
		buffer
			.append(Messages.disassembler_inner_name) 
			.append(Messages.disassembler_constantpoolindex) 
			.append(innerNameIndex);
		if (innerNameIndex != 0) {
			buffer
				.append(Messages.disassembler_space) 
				.append(innerClassesAttributeEntry.getInnerName());
		}
		buffer
			.append(Messages.disassembler_comma) 
			.append(Messages.disassembler_space) 
			.append(Messages.disassembler_inner_accessflags) 
			.append(accessFlags)
			.append(Messages.disassembler_space); 
		decodeModifiersForInnerClasses(buffer, accessFlags);
		buffer.append(Messages.disassembler_closeinnerclassentry); 
	}

	private void disassemble(int index, IParameterAnnotation parameterAnnotation, StringBuffer buffer, String lineSeparator, int tabNumber) {
		IAnnotation[] annotations = parameterAnnotation.getAnnotations();
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(
			Messages.bind(Messages.disassembler_parameterannotationentrystart, new String[] {Integer.toString(index), Integer.toString(annotations.length)}));
		for (int i = 0, max = annotations.length; i < max; i++) {
			disassemble(annotations[i], buffer, lineSeparator, tabNumber + 1);
		}
	}

	private void disassemble(IRuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_runtimeinvisibleannotationsattributeheader); 
		IAnnotation[] annotations = runtimeInvisibleAnnotationsAttribute.getAnnotations();
		for (int i = 0, max = annotations.length; i < max; i++) {
			disassemble(annotations[i], buffer, lineSeparator, tabNumber + 1);
		}
	}

	private void disassemble(IRuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_runtimeinvisibleparameterannotationsattributeheader); 
		IParameterAnnotation[] parameterAnnotations = runtimeInvisibleParameterAnnotationsAttribute.getParameterAnnotations();
		for (int i = 0, max = parameterAnnotations.length; i < max; i++) {
			disassemble(i, parameterAnnotations[i], buffer, lineSeparator, tabNumber + 1);
		}
	}
	
	private void disassemble(IRuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_runtimevisibleannotationsattributeheader); 
		IAnnotation[] annotations = runtimeVisibleAnnotationsAttribute.getAnnotations();
		for (int i = 0, max = annotations.length; i < max; i++) {
			disassemble(annotations[i], buffer, lineSeparator, tabNumber + 1);
		}
	}

	private void disassemble(IRuntimeVisibleParameterAnnotationsAttribute runtimeVisibleParameterAnnotationsAttribute, StringBuffer buffer, String lineSeparator, int tabNumber) {
		writeNewLine(buffer, lineSeparator, tabNumber + 1);
		buffer.append(Messages.disassembler_runtimevisibleparameterannotationsattributeheader); 
		IParameterAnnotation[] parameterAnnotations = runtimeVisibleParameterAnnotationsAttribute.getParameterAnnotations();
		for (int i = 0, max = parameterAnnotations.length; i < max; i++) {
			disassemble(i, parameterAnnotations[i], buffer, lineSeparator, tabNumber + 1);
		}
	}

	private String disassemble(IVerificationTypeInfo[] infos, String lineSeparator, int tabNumber, int mode) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, max = infos.length; i < max; i++) {
			if(i != 0) buffer.append(',');
			switch(infos[i].getTag()) {
				case IVerificationTypeInfo.ITEM_DOUBLE :
					buffer.append("double"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_FLOAT :
					buffer.append("float"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_INTEGER :
					buffer.append("int"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_LONG :
					buffer.append("long"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_NULL :
					buffer.append("null"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_OBJECT :
					final char[] classTypeName = infos[i].getClassTypeName();
					CharOperation.replace(classTypeName, '/', '.');
					buffer.append(returnClassName(classTypeName, '.', mode));
					break;
				case IVerificationTypeInfo.ITEM_TOP :
					buffer.append("top"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_UNINITIALIZED :
					buffer.append("uninitialized"); //$NON-NLS-1$
					break;
				case IVerificationTypeInfo.ITEM_UNINITIALIZED_THIS :
					buffer.append("uninitialized_this"); //$NON-NLS-1$
			}
		}
		return String.valueOf(buffer);
	}
	private void disassemble(IStackMapFrame frame, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		// disassemble each frame
		int type = frame.getFrameType();
		switch(type) {
			case 247 : // SAME_LOCALS_1_STACK_ITEM_EXTENDED
				buffer.append(
					Messages.bind(
						Messages.disassembler_frame_same_locals_1_stack_item_extended,
						Integer.toString(frame.getOffsetDelta()),
						disassemble(frame.getStackItems(), lineSeparator, tabNumber + 1, mode)));
				writeNewLine(buffer, lineSeparator, tabNumber);
				break;
			case 248 :
			case 249 :
			case 250:
				// CHOP
				buffer.append(
						Messages.bind(
							Messages.disassembler_frame_chop,
							Integer.toString(frame.getOffsetDelta()),
							Integer.toString(251 - type)));
				writeNewLine(buffer, lineSeparator, tabNumber);
				break;
			case 251 :
				// SAME_FRAME_EXTENDED
				buffer.append(
						Messages.bind(
							Messages.disassembler_frame_same_frame_extended,
							Integer.toString(frame.getOffsetDelta())));
				writeNewLine(buffer, lineSeparator, tabNumber);
				break;
			case 252 :
			case 253 :
			case 254 :
				// APPEND
				buffer.append(
						Messages.bind(
							Messages.disassembler_frame_append,
							Integer.toString(frame.getOffsetDelta()),
							disassemble(frame.getLocals(), lineSeparator, tabNumber + 1, mode)));
				writeNewLine(buffer, lineSeparator, tabNumber);
				break;
			case 255 :
				// FULL_FRAME
				buffer.append(
						Messages.bind(
							Messages.disassembler_frame_full_frame,
							new String[] {
								Integer.toString(frame.getOffsetDelta()),
								Integer.toString(frame.getNumberOfLocals()),
								disassemble(frame.getLocals(), lineSeparator, tabNumber + 1, mode),
								Integer.toString(frame.getNumberOfStackItems()),
								disassemble(frame.getStackItems(), lineSeparator, tabNumber + 1, mode),
								dumpNewLineWithTabs(lineSeparator, tabNumber + 2)
							}));
				writeNewLine(buffer, lineSeparator, tabNumber);
				break;
			default:
				if (type <= 63) {
					// SAME_FRAME
					buffer.append(
							Messages.bind(
								Messages.disassembler_frame_same_frame,
								Integer.toString(type)));
					writeNewLine(buffer, lineSeparator, tabNumber);
				} else if (type <= 127) {
					// SAME_LOCALS_1_STACK_ITEM
					buffer.append(
							Messages.bind(
								Messages.disassembler_frame_same_locals_1_stack_item,
								Integer.toString(type - 64),
								disassemble(frame.getStackItems(), lineSeparator, tabNumber + 1, mode)));
					writeNewLine(buffer, lineSeparator, tabNumber);
				}
		}
	}
	
	private void disassembleAsModifier(IAnnotation annotation, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		final char[] typeName = CharOperation.replaceOnCopy(annotation.getTypeName(), '/', '.');
		buffer.append('@').append(returnClassName(Signature.toCharArray(typeName), '.', mode));
		final IAnnotationComponent[] components = annotation.getComponents();
		final int length = components.length;
		if (length != 0) {
			buffer.append('(');
			for (int i = 0; i < length; i++) {
				if (i > 0) {
					buffer.append(',');
					writeNewLine(buffer, lineSeparator, tabNumber);
				}
				disassembleAsModifier(components[i], buffer, lineSeparator, tabNumber + 1, mode);
			}
			buffer.append(')');
		}
	}

	private void disassembleAsModifier(IAnnotationComponent annotationComponent, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		buffer.append(annotationComponent.getComponentName()).append('=');
		disassembleAsModifier(annotationComponent.getComponentValue(), buffer, lineSeparator, tabNumber + 1, mode);
	}

	private void disassembleAsModifier(IAnnotationComponentValue annotationComponentValue, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		switch(annotationComponentValue.getTag()) {
			case IAnnotationComponentValue.BYTE_TAG:
			case IAnnotationComponentValue.CHAR_TAG:
			case IAnnotationComponentValue.DOUBLE_TAG:
			case IAnnotationComponentValue.FLOAT_TAG:
			case IAnnotationComponentValue.INTEGER_TAG:
			case IAnnotationComponentValue.LONG_TAG:
			case IAnnotationComponentValue.SHORT_TAG:
			case IAnnotationComponentValue.BOOLEAN_TAG:
			case IAnnotationComponentValue.STRING_TAG:
				IConstantPoolEntry constantPoolEntry = annotationComponentValue.getConstantValue();
				String value = null;
				switch(constantPoolEntry.getKind()) {
					case IConstantPoolConstant.CONSTANT_Long :
						value = constantPoolEntry.getLongValue() + "L"; //$NON-NLS-1$
						break;
					case IConstantPoolConstant.CONSTANT_Float :
						value = constantPoolEntry.getFloatValue() + "f"; //$NON-NLS-1$
						break;
					case IConstantPoolConstant.CONSTANT_Double :
						value = Double.toString(constantPoolEntry.getDoubleValue());
						break;
					case IConstantPoolConstant.CONSTANT_Integer:
						switch(annotationComponentValue.getTag()) {
							case IAnnotationComponentValue.CHAR_TAG :
								value = "'" + (char) constantPoolEntry.getIntegerValue() + "'"; //$NON-NLS-1$//$NON-NLS-2$
								break;
							case IAnnotationComponentValue.BOOLEAN_TAG :
								value = constantPoolEntry.getIntegerValue() == 1 ? "true" : "false";//$NON-NLS-1$//$NON-NLS-2$
								break;
							case IAnnotationComponentValue.BYTE_TAG :
								value = "(byte) " + constantPoolEntry.getIntegerValue(); //$NON-NLS-1$
								break;
							case IAnnotationComponentValue.SHORT_TAG :
								value =  "(short) " + constantPoolEntry.getIntegerValue(); //$NON-NLS-1$
								break;
							case IAnnotationComponentValue.INTEGER_TAG :
								value =  "(int) " + constantPoolEntry.getIntegerValue(); //$NON-NLS-1$
						}
						break;
					case IConstantPoolConstant.CONSTANT_Utf8:
						value = "\"" + decodeStringValue(constantPoolEntry.getUtf8Value()) + "\"";//$NON-NLS-1$//$NON-NLS-2$
				}
				buffer.append(value);
				break;
			case IAnnotationComponentValue.ENUM_TAG:
				final char[] typeName = CharOperation.replaceOnCopy(annotationComponentValue.getEnumConstantTypeName(), '/', '.');
				final char[] constantName = annotationComponentValue.getEnumConstantName();
				buffer.append(Signature.toCharArray(typeName)).append('.').append(constantName);
				break;
			case IAnnotationComponentValue.CLASS_TAG:
				constantPoolEntry = annotationComponentValue.getClassInfo();
				final char[] className = CharOperation.replaceOnCopy(constantPoolEntry.getUtf8Value(), '/', '.');
				buffer.append(Signature.toCharArray(className));
				break;
			case IAnnotationComponentValue.ANNOTATION_TAG:
				IAnnotation annotation = annotationComponentValue.getAnnotationValue();
				disassembleAsModifier(annotation, buffer, lineSeparator, tabNumber + 1, mode);
				break;
			case IAnnotationComponentValue.ARRAY_TAG:
				final IAnnotationComponentValue[] annotationComponentValues = annotationComponentValue.getAnnotationComponentValues();
				buffer.append('{');
				for (int i = 0, max = annotationComponentValues.length; i < max; i++) {
					if (i > 0) {
						buffer.append(',');
					}
					disassembleAsModifier(annotationComponentValues[i], buffer, lineSeparator, tabNumber + 1, mode);
				}
				buffer.append('}');
		}
	}

	private void disassembleAsModifier(IAnnotationDefaultAttribute annotationDefaultAttribute, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		IAnnotationComponentValue componentValue = annotationDefaultAttribute.getMemberValue();
		disassembleAsModifier(componentValue, buffer, lineSeparator, tabNumber + 1, mode);
	}
	
	private void disassembleAsModifier(IRuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		IAnnotation[] annotations = runtimeInvisibleAnnotationsAttribute.getAnnotations();
		for (int i = 0, max = annotations.length; i < max; i++) {
			disassembleAsModifier(annotations[i], buffer, lineSeparator, tabNumber + 1, mode);
		}
	}

	private void disassembleAsModifier(IRuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute, StringBuffer buffer, String lineSeparator, int tabNumber, int mode) {
		IAnnotation[] annotations = runtimeVisibleAnnotationsAttribute.getAnnotations();
		for (int i = 0, max = annotations.length; i < max; i++) {
			disassembleAsModifier(annotations[i], buffer, lineSeparator, tabNumber + 1, mode);
		}
	}

	private void disassembleTypeMembers(IClassFileReader classFileReader, char[] className, StringBuffer buffer, String lineSeparator, int tabNumber, int mode, boolean isEnum) {
		IFieldInfo[] fields = classFileReader.getFieldInfos();
		if (isEnum && checkMode(mode, WORKING_COPY)) {
			int index = 0;
			final int fieldsLength = fields.length;
			IMethodInfo[] methods = classFileReader.getMethodInfos();
			char[][] constructorArguments = getConstructorArgumentsForEnum(methods);
			enumConstantLoop: for (; index < fieldsLength; index++) {
				final IFieldInfo fieldInfo = fields[index];
				final int accessFlags = fieldInfo.getAccessFlags();
				if ((accessFlags & IModifierConstants.ACC_ENUM) != 0) {
					writeNewLine(buffer, lineSeparator, tabNumber);
					disassembleEnumConstants(fields[index], buffer, lineSeparator, tabNumber, constructorArguments, mode);
				} else {
					break enumConstantLoop;
				}
			}
			buffer.append(';');
			boolean foundSyntheticField = false;
			fieldLoop: for (; index < fieldsLength; index++) {
				if (!foundSyntheticField && CharOperation.equals(TypeConstants.SYNTHETIC_ENUM_VALUES, fields[index].getName())) {
					foundSyntheticField = true;
					continue fieldLoop;
				}
				writeNewLine(buffer, lineSeparator, tabNumber);
				disassemble(fields[index], buffer, lineSeparator, tabNumber, mode);
			}
			methodLoop: for (int i = 0, max = methods.length; i < max; i++) {
				final IMethodInfo methodInfo = methods[i];
				if (CharOperation.equals(methodInfo.getName(), TypeConstants.VALUES)) {
					final char[] descriptor = methodInfo.getDescriptor();
					CharOperation.replace(descriptor, '/', '.');
					if (Signature.getParameterCount(descriptor) == 0) {
						if (CharOperation.equals(returnClassName(Signature.getReturnType(descriptor), '.', mode),
								CharOperation.concat(new char[] {'[', 'L'}, className, new char[] {';'}))) {
							continue methodLoop;
						}
					}
				} else if (CharOperation.equals(methodInfo.getName(), TypeConstants.VALUEOF)) {
					final char[] descriptor = methodInfo.getDescriptor();
					CharOperation.replace(descriptor, '/', '.');
					final char[][] parameterTypes = Signature.getParameterTypes(descriptor);
					if (parameterTypes.length == 1
							&& CharOperation.equals(parameterTypes[0], "Ljava.lang.String;".toCharArray())) { //$NON-NLS-1$
						if (CharOperation.equals(returnClassName(Signature.getReturnType(descriptor), '.', mode),
								CharOperation.concat('L', className, ';'))) {
							continue methodLoop;
						}
					}
				} else if (methodInfo.isClinit() || methodInfo.isSynthetic()) {
					continue methodLoop;
				} else if (methodInfo.isConstructor()) {
					writeNewLine(buffer, lineSeparator, tabNumber);
					disassembleEnumConstructor(classFileReader, className, methodInfo, buffer, lineSeparator, tabNumber, mode);
				} else {
					writeNewLine(buffer, lineSeparator, tabNumber);
					disassemble(classFileReader, className, methodInfo, buffer, lineSeparator, tabNumber, mode);
				}
			}
		} else {
			for (int i = 0, max = fields.length; i < max; i++) {
				writeNewLine(buffer, lineSeparator, tabNumber);
				disassemble(fields[i], buffer, lineSeparator, tabNumber, mode);
			}
			IMethodInfo[] methods = classFileReader.getMethodInfos();
			for (int i = 0, max = methods.length; i < max; i++) {
				writeNewLine(buffer, lineSeparator, tabNumber);
				disassemble(classFileReader, className, methods[i], buffer, lineSeparator, tabNumber, mode);
			}
		}
	}
	
	private char[][] getConstructorArgumentsForEnum(final IMethodInfo[] methods) {
		loop: for (int i = 0, max = methods.length; i < max; i++) {
			IMethodInfo methodInfo = methods[i];
			if (methodInfo.isConstructor()) {
				char[][] parameterTypes = Signature.getParameterTypes(methodInfo.getDescriptor());
				final int length = parameterTypes.length;
				if (length >= 2) {
					return CharOperation.subarray(parameterTypes, 2, length);
				}
			} else {
				continue loop;
			}
		}
		return null;
	}

	private final void dumpTab(int tabNumber, StringBuffer buffer) {
		for (int i = 0; i < tabNumber; i++) {
			buffer.append(Messages.disassembler_indentation); 
		}
	}
	
	private final String dumpNewLineWithTabs(String lineSeparator, int tabNumber) {
		StringBuffer buffer = new StringBuffer();
		writeNewLine(buffer, lineSeparator, tabNumber);
		return String.valueOf(buffer);
	} 
	
	/**
	 * @see org.eclipse.jdt.core.util.ClassFileBytesDisassembler#getDescription()
	 */
	public String getDescription() {
		return Messages.disassembler_description; 
	}

	private IEnclosingMethodAttribute getEnclosingMethodAttribute(IClassFileReader classFileReader) {
		IClassFileAttribute[] attributes = classFileReader.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), IAttributeNamesConstants.ENCLOSING_METHOD)) {
				return (IEnclosingMethodAttribute) attributes[i];
			}
		}
		return null;
	}
	private IClassFileAttribute getAttribute(final char[] attributeName, final ICodeAttribute codeAttribute) {
		IClassFileAttribute[] attributes = codeAttribute.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), attributeName)) {
				return attributes[i];
			}
		}
		return null;
	}
	
	private char[][] getParameterNames(char[] methodDescriptor, ICodeAttribute codeAttribute, int accessFlags) {
		int paramCount = Signature.getParameterCount(methodDescriptor);
		char[][] parameterNames = new char[paramCount][];
		// check if the code attribute has debug info for this method
		if (codeAttribute != null) {
			ILocalVariableAttribute localVariableAttribute = codeAttribute.getLocalVariableAttribute();
			if (localVariableAttribute != null) {
				ILocalVariableTableEntry[] entries = localVariableAttribute.getLocalVariableTable();
				final int startingIndex = (accessFlags & IModifierConstants.ACC_STATIC) != 0 ? 0 : 1;
				for (int i = 0; i < paramCount; i++) {
					ILocalVariableTableEntry searchedEntry = getEntryFor(getLocalIndex(startingIndex, i, methodDescriptor), entries);
					if (searchedEntry != null) {
						parameterNames[i] = searchedEntry.getName();
					} else {
						parameterNames[i] = CharOperation.concat(Messages.disassembler_parametername.toCharArray(), Integer.toString(i).toCharArray()); 
					}
				}
			} else {
				for (int i = 0; i < paramCount; i++) {
					parameterNames[i] = CharOperation.concat(Messages.disassembler_parametername.toCharArray(), Integer.toString(i).toCharArray()); 
				}
			}
		} else {
			for (int i = 0; i < paramCount; i++) {
				parameterNames[i] = CharOperation.concat(Messages.disassembler_parametername.toCharArray(), Integer.toString(i).toCharArray()); 
			}
		}
		return parameterNames;
	}
	
	private int getLocalIndex(final int startingSlot, final int index, final char[] methodDescriptor) {
		int slot = startingSlot;
		final char[][] types = Signature.getParameterTypes(methodDescriptor);
		for (int i = 0; i < index; i++) {
			final char[] type = types[i];
			switch(type.length) {
				case 1 :
					switch(type[0]) {
						case 'D' :
						case 'J' :
							slot += 2;
							break;
						default :
							slot++;
					}
					break;
				default :
					slot++;
			}
		}
		return slot;
	}

	private ILocalVariableTableEntry getEntryFor(final int index, final ILocalVariableTableEntry[] entries) {
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTableEntry entry = entries[i];
			if (index == entry.getIndex()) {
				return entry;
			}
		}
		return null;
	}

	private char[] getSignatureForField(char[] fieldDescriptor) {
		char[] newFieldDescriptor = CharOperation.replaceOnCopy(fieldDescriptor, '/', '.');
		newFieldDescriptor = CharOperation.replaceOnCopy(newFieldDescriptor, '$', '%');
		char[] fieldDescriptorSignature = Signature.toCharArray(newFieldDescriptor);
		CharOperation.replace(fieldDescriptorSignature, '%', '$');
		return fieldDescriptorSignature;
	}
	
	private boolean isDeprecated(IClassFileReader classFileReader) {
		IClassFileAttribute[] attributes = classFileReader.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), IAttributeNamesConstants.DEPRECATED)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isSynthetic(IClassFileReader classFileReader) {
		int flags = classFileReader.getAccessFlags();
		if ((flags & IModifierConstants.ACC_SYNTHETIC) != 0) {
			return true;
		}
		IClassFileAttribute[] attributes = classFileReader.getAttributes();
		for (int i = 0, max = attributes.length; i < max; i++) {
			if (CharOperation.equals(attributes[i].getAttributeName(), IAttributeNamesConstants.SYNTHETIC)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkMode(int mode, int flag) {
		return (mode & flag) != 0;
	}
	
	private boolean isCompact(int mode) {
		return (mode & ClassFileBytesDisassembler.COMPACT) != 0;
	}

	private char[] returnClassName(char[] classInfoName, char separator, int mode) {
		if (classInfoName.length == 0) {
			return EMPTY_CLASS_NAME;
		} else if (isCompact(mode)) {
			int lastIndexOfSlash = CharOperation.lastIndexOf(separator, classInfoName);
			if (lastIndexOfSlash != -1) {
				return CharOperation.subarray(classInfoName, lastIndexOfSlash + 1, classInfoName.length);
			}
		}
		return classInfoName;
	}
	
	private void writeNewLine(StringBuffer buffer, String lineSeparator, int tabNumber) {
		buffer.append(lineSeparator);
		dumpTab(tabNumber, buffer);
	}
}
