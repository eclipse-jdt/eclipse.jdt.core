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
	super(classFile);
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
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangByte());
			break;
		case T_short :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangShort());
			break;
		case T_char :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangCharacter());
			break;
		case T_int :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangInteger());
			break;
		case T_long :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangLong());
			break;
		case T_float :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangFloat());
			break;
		case T_double :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangDouble());
			break;
		case T_boolean :
			writeUnsignedShort(this.constantPool.literalIndexForJavaLangBoolean());
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
	this.newArray(scope, new ArrayBinding(scope.getType(TypeConstants.JAVA_LANG_CLASS), 1));
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
	this.newArray(scope, new ArrayBinding(scope.getType(TypeConstants.JAVA_LANG_CLASS), 1));
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
	char[][] wrapperTypeCompoundName = null;
	switch (valueType.id) {
		case T_int : // new: java.lang.Integer
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Integer".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_boolean : // new: java.lang.Boolean
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Boolean".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_byte : // new: java.lang.Byte
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Byte".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_char : // new: java.lang.Character
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Character".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_float : // new: java.lang.Float
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Float".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_double : // new: java.lang.Double
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Double".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_short : // new: java.lang.Short
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Short".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
		case T_long : // new: java.lang.Long
			wrapperTypeCompoundName = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Long".toCharArray()}; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
			break;
	}
	TypeBinding wrapperType = this.methodDeclaration.scope.getType(wrapperTypeCompoundName);
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
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	switch (baseTypeID) {
		case T_byte :
			// invokevirtual: byteValue()
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangByteByteValue());
			break;
		case T_short :
			// invokevirtual: shortValue()
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangShortShortValue());
			break;
		case T_char :
			// invokevirtual: charValue()
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangCharacterCharValue());
			break;
		case T_int :
			// invokevirtual: intValue()
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangIntegerIntValue());
			break;
		case T_long :
			// invokevirtual: longValue()
			this.stackDepth++;
			if (this.stackDepth > this.stackMax)
				this.stackMax = this.stackDepth;
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangLongLongValue());
			break;
		case T_float :
			// invokevirtual: floatValue()
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangFloatFloatValue());
			break;
		case T_double :
			// invokevirtual: doubleValue()
			this.stackDepth++;
			if (this.stackDepth > this.stackMax)
				this.stackMax = this.stackDepth;
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangDoubleDoubleValue());
			break;
		case T_boolean :
			// invokevirtual: booleanValue()
			writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangBooleanBooleanValue());
	}
}
protected void invokeAccessibleObjectSetAccessible() {
	// invokevirtual: java.lang.reflect.AccessibleObject.setAccessible(Z)V;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangReflectAccessibleObjectSetAccessible());
	this.stackDepth-=2;
}
protected void invokeArrayNewInstance() {
	// invokestatic: java.lang.reflect.Array.newInstance(Ljava.lang.Class;int[])Ljava.lang.reflect.Array;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokestatic;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangReflectArrayNewInstance());
	this.stackDepth--;
}
protected void invokeClassGetDeclaredConstructor() {
	// invokevirtual: java.lang.Class getDeclaredConstructor([Ljava.lang.Class)Ljava.lang.reflect.Constructor;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangClassGetDeclaredConstructor());
	this.stackDepth--;
}
protected void invokeClassGetDeclaredField() {
	// invokevirtual: java.lang.Class.getDeclaredField(Ljava.lang.String)Ljava.lang.reflect.Field;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangClassGetDeclaredField());
	this.stackDepth--;
}
protected void invokeClassGetDeclaredMethod() {
	// invokevirtual: java.lang.Class getDeclaredMethod(Ljava.lang.String, [Ljava.lang.Class)Ljava.lang.reflect.Method;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangClassGetDeclaredMethod());
	this.stackDepth-=2;
}
protected void invokeJavaLangReflectConstructorNewInstance() {
	// invokevirtual: java.lang.reflect.Constructor.newInstance([Ljava.lang.Object;)Ljava.lang.Object;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangReflectConstructorNewInstance());
	this.stackDepth--;
}
protected void invokeJavaLangReflectFieldGetter(int typeID) {
	this.countLabels = 0;
	int usedTypeID;
	if (typeID == T_null)
		usedTypeID = T_Object;
	else
		usedTypeID = typeID;
	// invokevirtual
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexJavaLangReflectFieldGetter(typeID));
	if ((usedTypeID != T_long) && (usedTypeID != T_double)) {
		this.stackDepth--;
	}
}
protected void invokeJavaLangReflectFieldSetter(int typeID) {
	this.countLabels = 0;
	int usedTypeID;
	if (typeID == T_null)
		usedTypeID = T_Object;
	else
		usedTypeID = typeID;
	// invokevirtual
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexJavaLangReflectFieldSetter(typeID));
	if ((usedTypeID != T_long) && (usedTypeID != T_double)) {
		this.stackDepth-=3;
	} else {
		this.stackDepth-=4;
	}
}
protected void invokeJavaLangReflectMethodInvoke() {
	// invokevirtual: java.lang.reflect.Method.invoke(Ljava.lang.Object;[Ljava.lang.Object;)Ljava.lang.Object;
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(((CodeSnippetConstantPool) this.constantPool).literalIndexForJavaLangReflectMethodInvoke());
	this.stackDepth-=2;
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
