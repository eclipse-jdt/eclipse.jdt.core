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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.QualifiedNamesConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class CodeSnippetCodeStream extends CodeStream {
	static InvocationSite NO_INVOCATION_SITE = 
		new InvocationSite(){	
			public TypeBinding[] genericTypeArguments() { return null; }
			public boolean isSuperAccess(){ return false; }
			public boolean isTypeAccess() { return false; }
			public void setActualReceiverType(ReferenceBinding receiverType) {}
			public void setDepth(int depth) {}
			public void setFieldIndex(int depth){}
			public int sourceStart() { return 0; }
			public int sourceEnd() { return 0; }
		};
/**
 * CodeSnippetCodeStream constructor comment.
 * @param classFile org.eclipse.jdt.internal.compiler.ClassFile
 */
public CodeSnippetCodeStream(org.eclipse.jdt.internal.compiler.ClassFile classFile) {
	super(classFile, JDK1_4);
}
protected void checkcast(int baseId) {
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_checkcast;
	switch (baseId) {
		case T_byte :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangByteConstantPoolName));
			break;
		case T_short :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangShortConstantPoolName));
			break;
		case T_char :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangCharacterConstantPoolName));
			break;
		case T_int :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangIntegerConstantPoolName));
			break;
		case T_long :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangLongConstantPoolName));
			break;
		case T_float :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangFloatConstantPoolName));
			break;
		case T_double :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangDoubleConstantPoolName));
			break;
		case T_boolean :
			writeUnsignedShort(this.constantPool.literalIndexForType(QualifiedNamesConstants.JavaLangBooleanConstantPoolName));
	}
}
public void generateEmulatedAccessForMethod(Scope scope, MethodBinding methodBinding) {
	CodeSnippetCodeStream localCodeStream = this;
	localCodeStream.generateEmulationForMethod(scope, methodBinding);
	localCodeStream.invokeJavaLangReflectMethodInvoke();
}
public void generateEmulatedReadAccessForField(FieldBinding fieldBinding) {
	CodeSnippetCodeStream localCodeStream = this;
	localCodeStream.generateEmulationForField(fieldBinding);
	// swap  the field with the receiver
	this.swap();
	localCodeStream.invokeJavaLangReflectFieldGetter(fieldBinding.type.id);
	if (!fieldBinding.type.isBaseType()) {
		this.checkcast(fieldBinding.type);
	}
}
public void generateEmulatedWriteAccessForField(FieldBinding fieldBinding) {
	CodeSnippetCodeStream localCodeStream = this;
	localCodeStream.invokeJavaLangReflectFieldSetter(fieldBinding.type.id);
}
public void generateEmulationForConstructor(Scope scope, MethodBinding methodBinding) {
	// leave a java.lang.reflect.Field object on the stack
	CodeSnippetCodeStream localCodeStream = this;
	this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
	this.invokeClassForName();
	int paramLength = methodBinding.parameters.length;
	this.generateInlinedValue(paramLength);
	this.newArray(scope.createArrayType(scope.getType(TypeConstants.JAVA_LANG_CLASS, 3), 1));
	if (paramLength > 0) {
		this.dup();
		for (int i = 0; i < paramLength; i++) {
			this.generateInlinedValue(i);	
			TypeBinding parameter = methodBinding.parameters[i];
			if (parameter.isBaseType()) {
				this.getTYPE(parameter.id);
			} else if (parameter.isArrayType()) {
				ArrayBinding array = (ArrayBinding)parameter;
				if (array.leafComponentType.isBaseType()) {
					this.getTYPE(array.leafComponentType.id);
				} else {
					this.ldc(String.valueOf(array.leafComponentType.constantPoolName()).replace('/', '.'));
					this.invokeClassForName();
				}
				int dimensions = array.dimensions;
				this.generateInlinedValue(dimensions);
				this.newarray(T_int);	
				this.invokeArrayNewInstance();
				this.invokeObjectGetClass();
			} else {
				// parameter is a reference binding
				this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
				this.invokeClassForName();
			}
			this.aastore();
			if (i < paramLength - 1) {
				this.dup();
			}
		}
	}
	localCodeStream.invokeClassGetDeclaredConstructor();
	this.dup();
	this.iconst_1();
	localCodeStream.invokeAccessibleObjectSetAccessible();
}
public void generateEmulationForField(FieldBinding fieldBinding) {
	// leave a java.lang.reflect.Field object on the stack
	CodeSnippetCodeStream localCodeStream = this;
	this.ldc(String.valueOf(fieldBinding.declaringClass.constantPoolName()).replace('/', '.'));
	this.invokeClassForName();
	this.ldc(String.valueOf(fieldBinding.name));
	localCodeStream.invokeClassGetDeclaredField();
	this.dup();
	this.iconst_1();
	localCodeStream.invokeAccessibleObjectSetAccessible();
}
public void generateEmulationForMethod(Scope scope, MethodBinding methodBinding) {
	// leave a java.lang.reflect.Field object on the stack
	CodeSnippetCodeStream localCodeStream = this;
	this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
	this.invokeClassForName();
	this.ldc(String.valueOf(methodBinding.selector));
	int paramLength = methodBinding.parameters.length;
	this.generateInlinedValue(paramLength);
	this.newArray(scope.createArrayType(scope.getType(TypeConstants.JAVA_LANG_CLASS, 3), 1));
	if (paramLength > 0) {
		this.dup();
		for (int i = 0; i < paramLength; i++) {
			this.generateInlinedValue(i);	
			TypeBinding parameter = methodBinding.parameters[i];
			if (parameter.isBaseType()) {
				this.getTYPE(parameter.id);
			} else if (parameter.isArrayType()) {
				ArrayBinding array = (ArrayBinding)parameter;
				if (array.leafComponentType.isBaseType()) {
					this.getTYPE(array.leafComponentType.id);
				} else {
					this.ldc(String.valueOf(array.leafComponentType.constantPoolName()).replace('/', '.'));
					this.invokeClassForName();
				}
				int dimensions = array.dimensions;
				this.generateInlinedValue(dimensions);
				this.newarray(T_int);	
				this.invokeArrayNewInstance();
				this.invokeObjectGetClass();
			} else {
				// parameter is a reference binding
				this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
				this.invokeClassForName();
			}
			this.aastore();
			if (i < paramLength - 1) {
				this.dup();
			}
		}
	}
	localCodeStream.invokeClassGetDeclaredMethod();
	this.dup();
	this.iconst_1();
	localCodeStream.invokeAccessibleObjectSetAccessible();
}
public void generateObjectWrapperForType(TypeBinding valueType) {

	/* The top of stack must be encapsulated inside 
	 * a wrapper object if it corresponds to a base type
	 */
	TypeBinding wrapperType = this.methodDeclaration.scope.boxing(valueType);
	new_(wrapperType);
	if (valueType.id == T_long || valueType.id == T_double) {
		dup_x2();
		dup_x2();
		pop();
	} else {
		dup_x1();
		swap();
	}
	MethodBinding methodBinding = this.methodDeclaration.scope.getMethod(
				wrapperType, 
				QualifiedNamesConstants.Init, 
				new TypeBinding[] {valueType}, 
				NO_INVOCATION_SITE);
	invokespecial(methodBinding);
}
public void getBaseTypeValue(int baseTypeID) {
	switch (baseTypeID) {
		case T_byte :
			// invokevirtual: byteValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					1, // return type size
					QualifiedNamesConstants.JavaLangByteConstantPoolName,
					QualifiedNamesConstants.BYTEVALUE_BYTE_METHOD_NAME,
					QualifiedNamesConstants.BYTEVALUE_BYTE_METHOD_SIGNATURE);
			break;
		case T_short :
			// invokevirtual: shortValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					1, // return type size
					QualifiedNamesConstants.JavaLangShortConstantPoolName,
					QualifiedNamesConstants.SHORTVALUE_SHORT_METHOD_NAME,
					QualifiedNamesConstants.SHORTVALUE_SHORT_METHOD_SIGNATURE);
			break;
		case T_char :
			// invokevirtual: charValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					1, // return type size
					QualifiedNamesConstants.JavaLangCharacterConstantPoolName,
					QualifiedNamesConstants.CHARVALUE_CHARACTER_METHOD_NAME,
					QualifiedNamesConstants.CHARVALUE_CHARACTER_METHOD_SIGNATURE);
			break;
		case T_int :
			// invokevirtual: intValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					1, // return type size
					QualifiedNamesConstants.JavaLangIntegerConstantPoolName,
					QualifiedNamesConstants.INTVALUE_INTEGER_METHOD_NAME,
					QualifiedNamesConstants.INTVALUE_INTEGER_METHOD_SIGNATURE);
			break;
		case T_long :
			// invokevirtual: longValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					2, // return type size
					QualifiedNamesConstants.JavaLangLongConstantPoolName,
					QualifiedNamesConstants.LONGVALUE_LONG_METHOD_NAME,
					QualifiedNamesConstants.LONGVALUE_LONG_METHOD_SIGNATURE);
			break;
		case T_float :
			// invokevirtual: floatValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					1, // return type size
					QualifiedNamesConstants.JavaLangFloatConstantPoolName,
					QualifiedNamesConstants.FLOATVALUE_FLOAT_METHOD_NAME,
					QualifiedNamesConstants.FLOATVALUE_FLOAT_METHOD_SIGNATURE);
			break;
		case T_double :
			// invokevirtual: doubleValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					2, // return type size
					QualifiedNamesConstants.JavaLangDoubleConstantPoolName,
					QualifiedNamesConstants.DOUBLEVALUE_DOUBLE_METHOD_NAME,
					QualifiedNamesConstants.DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE);
			break;
		case T_boolean :
			// invokevirtual: booleanValue()
			this.invoke(
					OPC_invokevirtual,
					0, // argCount
					1, // return type size
					QualifiedNamesConstants.JavaLangBooleanConstantPoolName,
					QualifiedNamesConstants.BOOLEANVALUE_BOOLEAN_METHOD_NAME,
					QualifiedNamesConstants.BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE);
	}
}
protected void invokeAccessibleObjectSetAccessible() {
	// invokevirtual: java.lang.reflect.AccessibleObject.setAccessible(Z)V;
	this.invoke(
			OPC_invokevirtual,
			1, // argCount
			0, // return type size
			QualifiedNamesConstants.JAVALANGREFLECTACCESSIBLEOBJECT_CONSTANTPOOLNAME,
			QualifiedNamesConstants.SETACCESSIBLE_NAME,
			QualifiedNamesConstants.SETACCESSIBLE_SIGNATURE);
}
protected void invokeArrayNewInstance() {
	// invokestatic: java.lang.reflect.Array.newInstance(Ljava.lang.Class;int[])Ljava.lang.Object;
	this.invoke(
			OPC_invokestatic,
			2, // argCount
			1, // return type size
			QualifiedNamesConstants.JAVALANGREFLECTARRAY_CONSTANTPOOLNAME,
			QualifiedNamesConstants.NewInstance,
			QualifiedNamesConstants.NewInstanceSignature);
}
protected void invokeClassGetDeclaredConstructor() {
	// invokevirtual: java.lang.Class getDeclaredConstructor([Ljava.lang.Class)Ljava.lang.reflect.Constructor;
	this.invoke(
			OPC_invokevirtual,
			1, // argCount
			1, // return type size
			QualifiedNamesConstants.JavaLangClassConstantPoolName,
			QualifiedNamesConstants.GETDECLAREDCONSTRUCTOR_NAME,
			QualifiedNamesConstants.GETDECLAREDCONSTRUCTOR_SIGNATURE);
}
protected void invokeClassGetDeclaredField() {
	// invokevirtual: java.lang.Class.getDeclaredField(Ljava.lang.String)Ljava.lang.reflect.Field;
	this.invoke(
			OPC_invokevirtual,
			1, // argCount
			1, // return type size
			QualifiedNamesConstants.JavaLangClassConstantPoolName,
			QualifiedNamesConstants.GETDECLAREDFIELD_NAME,
			QualifiedNamesConstants.GETDECLAREDFIELD_SIGNATURE);
}
protected void invokeClassGetDeclaredMethod() {
	// invokevirtual: java.lang.Class getDeclaredMethod(Ljava.lang.String, [Ljava.lang.Class)Ljava.lang.reflect.Method;
	this.invoke(
			OPC_invokevirtual,
			2, // argCount
			1, // return type size
			QualifiedNamesConstants.JavaLangClassConstantPoolName,
			QualifiedNamesConstants.GETDECLAREDMETHOD_NAME,
			QualifiedNamesConstants.GETDECLAREDMETHOD_SIGNATURE);
}
protected void invokeJavaLangReflectConstructorNewInstance() {
	// invokevirtual: java.lang.reflect.Constructor.newInstance([Ljava.lang.Object;)Ljava.lang.Object;
	this.invoke(
			OPC_invokevirtual,
			1, // argCount
			1, // return type size
			QualifiedNamesConstants.JavaLangReflectConstructor,
			QualifiedNamesConstants.NewInstance,
			QualifiedNamesConstants.JavaLangReflectConstructorNewInstanceSignature);
}
protected void invokeJavaLangReflectFieldGetter(int typeID) {
	int returnTypeSize = 1;
	char[] signature = null;
	char[] selector = null;
	switch (typeID) {
		case T_int :
			selector = QualifiedNamesConstants.GET_INT_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_INT_METHOD_SIGNATURE;
			break;
		case T_byte :
			selector = QualifiedNamesConstants.GET_BYTE_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_BYTE_METHOD_SIGNATURE;
			break;
		case T_short :
			selector = QualifiedNamesConstants.GET_SHORT_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_SHORT_METHOD_SIGNATURE;
			break;
		case T_long :
			selector = QualifiedNamesConstants.GET_LONG_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_LONG_METHOD_SIGNATURE;
			returnTypeSize = 2;
			break;
		case T_float :
			selector = QualifiedNamesConstants.GET_FLOAT_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_FLOAT_METHOD_SIGNATURE;
			break;
		case T_double :
			selector = QualifiedNamesConstants.GET_DOUBLE_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_DOUBLE_METHOD_SIGNATURE;
			returnTypeSize = 2;
			break;
		case T_char :
			selector = QualifiedNamesConstants.GET_CHAR_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_CHAR_METHOD_SIGNATURE;
			break;
		case T_boolean :
			selector = QualifiedNamesConstants.GET_BOOLEAN_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_BOOLEAN_METHOD_SIGNATURE;
			break;
		default :
			selector = QualifiedNamesConstants.GET_OBJECT_METHOD_NAME;
			signature = QualifiedNamesConstants.GET_OBJECT_METHOD_SIGNATURE;
			break;
	}
	this.invoke(
			OPC_invokevirtual,
			1, // argCount
			returnTypeSize, // return type size
			QualifiedNamesConstants.JAVALANGREFLECTFIELD_CONSTANTPOOLNAME,
			selector,
			signature);
}
protected void invokeJavaLangReflectFieldSetter(int typeID) {
	int argCount = 2;
	char[] signature = null;
	char[] selector = null;
	switch (typeID) {
		case T_int :
			selector = QualifiedNamesConstants.SET_INT_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_INT_METHOD_SIGNATURE;
			break;
		case T_byte :
			selector = QualifiedNamesConstants.SET_BYTE_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_BYTE_METHOD_SIGNATURE;
			break;
		case T_short :
			selector = QualifiedNamesConstants.SET_SHORT_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_SHORT_METHOD_SIGNATURE;
			break;
		case T_long :
			selector = QualifiedNamesConstants.SET_LONG_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_LONG_METHOD_SIGNATURE;
			argCount = 3;
			break;
		case T_float :
			selector = QualifiedNamesConstants.SET_FLOAT_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_FLOAT_METHOD_SIGNATURE;
			break;
		case T_double :
			selector = QualifiedNamesConstants.SET_DOUBLE_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_DOUBLE_METHOD_SIGNATURE;
			argCount = 3;
			break;
		case T_char :
			selector = QualifiedNamesConstants.SET_CHAR_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_CHAR_METHOD_SIGNATURE;
			break;
		case T_boolean :
			selector = QualifiedNamesConstants.SET_BOOLEAN_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_BOOLEAN_METHOD_SIGNATURE;
			break;
		default :
			selector = QualifiedNamesConstants.SET_OBJECT_METHOD_NAME;
			signature = QualifiedNamesConstants.SET_OBJECT_METHOD_SIGNATURE;
			break;
	}
	this.invoke(
			OPC_invokevirtual,
			argCount, // argCount
			0, // return type size
			QualifiedNamesConstants.JAVALANGREFLECTFIELD_CONSTANTPOOLNAME,
			selector,
			signature);
}
protected void invokeJavaLangReflectMethodInvoke() {
	// invokevirtual: java.lang.reflect.Method.invoke(Ljava.lang.Object;[Ljava.lang.Object;)Ljava.lang.Object;
	this.invoke(
			OPC_invokevirtual,
			2, // argCount
			1, // return type size
			QualifiedNamesConstants.JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME,
			QualifiedNamesConstants.INVOKE_METHOD_METHOD_NAME,
			QualifiedNamesConstants.INVOKE_METHOD_METHOD_SIGNATURE);
}
private final void resizeByteArray() {
	int length = bCodeStream.length;
	int requiredSize = length + length;
	if (classFileOffset > requiredSize) {
		// must be sure to grow by enough
		requiredSize = classFileOffset + length;
	}
	System.arraycopy(bCodeStream, 0, bCodeStream = new byte[requiredSize], 0, length);
}
}
