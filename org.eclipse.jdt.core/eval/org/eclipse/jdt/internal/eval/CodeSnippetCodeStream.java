package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CodeSnippetCodeStream extends CodeStream {
	/**
	 * CodeSnippetCodeStream constructor comment.
	 * @param classFile org.eclipse.jdt.internal.compiler.ClassFile
	 */
	public CodeSnippetCodeStream(
		org.eclipse.jdt.internal.compiler.ClassFile classFile) {
		super(classFile);
	}

	protected void checkcast(int baseId) {
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_checkcast;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_checkcast);
		}
		switch (baseId) {
			case T_byte :
				writeUnsignedShort(constantPool.literalIndexForJavaLangByte());
				break;
			case T_short :
				writeUnsignedShort(constantPool.literalIndexForJavaLangShort());
				break;
			case T_char :
				writeUnsignedShort(constantPool.literalIndexForJavaLangCharacter());
				break;
			case T_int :
				writeUnsignedShort(constantPool.literalIndexForJavaLangInteger());
				break;
			case T_long :
				writeUnsignedShort(constantPool.literalIndexForJavaLangLong());
				break;
			case T_float :
				writeUnsignedShort(constantPool.literalIndexForJavaLangFloat());
				break;
			case T_double :
				writeUnsignedShort(constantPool.literalIndexForJavaLangDouble());
				break;
			case T_boolean :
				writeUnsignedShort(constantPool.literalIndexForJavaLangBoolean());
		}
	}

	public void generateEmulatedAccessForMethod(
		Scope scope,
		MethodBinding methodBinding) {
		CodeSnippetCodeStream localCodeStream = (CodeSnippetCodeStream) this;
		localCodeStream.generateEmulationForMethod(scope, methodBinding);
		localCodeStream.invokeJavaLangReflectMethodInvoke();
	}

	public void generateEmulatedReadAccessForField(FieldBinding fieldBinding) {
		CodeSnippetCodeStream localCodeStream = (CodeSnippetCodeStream) this;
		localCodeStream.generateEmulationForField(fieldBinding);
		// swap  the field with the receiver
		this.swap();
		localCodeStream.invokeJavaLangReflectFieldGetter(fieldBinding.type.id);
		if (fieldBinding.type.isArrayType()) {
			this.checkcast(fieldBinding.type);
		}
	}

	public void generateEmulatedWriteAccessForField(FieldBinding fieldBinding) {
		CodeSnippetCodeStream localCodeStream = (CodeSnippetCodeStream) this;
		localCodeStream.invokeJavaLangReflectFieldSetter(fieldBinding.type.id);
	}

	public void generateEmulationForConstructor(
		Scope scope,
		MethodBinding methodBinding) {
		// leave a java.lang.reflect.Field object on the stack
		CodeSnippetCodeStream localCodeStream = (CodeSnippetCodeStream) this;
		this.ldc(
			String.valueOf(methodBinding.declaringClass.constantPoolName()).replace(
				'/',
				'.'));
		this.invokeClassForName();
		int paramLength = methodBinding.parameters.length;
		this.generateInlinedValue(paramLength);
		this.newArray(
			scope,
			new ArrayBinding(scope.getType(TypeBinding.JAVA_LANG_CLASS), 1));
		if (paramLength > 0) {
			this.dup();
			for (int i = 0; i < paramLength; i++) {
				this.generateInlinedValue(i);
				TypeBinding parameter = methodBinding.parameters[i];
				if (parameter.isBaseType()) {
					this.getTYPE(parameter.id);
				} else
					if (parameter.isArrayType()) {
						ArrayBinding array = (ArrayBinding) parameter;
						if (array.leafComponentType.isBaseType()) {
							this.getTYPE(array.leafComponentType.id);
						} else {
							this.ldc(
								String.valueOf(array.leafComponentType.constantPoolName()).replace('/', '.'));
							this.invokeClassForName();
						}
						int dimensions = array.dimensions;
						this.generateInlinedValue(dimensions);
						this.newarray(T_int);
						this.invokeArrayNewInstance();
						this.invokeObjectGetClass();
					} else {
						// parameter is a reference binding
						this.ldc(
							String.valueOf(methodBinding.declaringClass.constantPoolName()).replace(
								'/',
								'.'));
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
		CodeSnippetCodeStream localCodeStream = (CodeSnippetCodeStream) this;
		this.ldc(
			String.valueOf(fieldBinding.declaringClass.constantPoolName()).replace(
				'/',
				'.'));
		this.invokeClassForName();
		this.ldc(String.valueOf(fieldBinding.name));
		localCodeStream.invokeClassGetDeclaredField();
		this.dup();
		this.iconst_1();
		localCodeStream.invokeAccessibleObjectSetAccessible();
	}

	public void generateEmulationForMethod(
		Scope scope,
		MethodBinding methodBinding) {
		// leave a java.lang.reflect.Field object on the stack
		CodeSnippetCodeStream localCodeStream = (CodeSnippetCodeStream) this;
		this.ldc(
			String.valueOf(methodBinding.declaringClass.constantPoolName()).replace(
				'/',
				'.'));
		this.invokeClassForName();
		this.ldc(String.valueOf(methodBinding.selector));
		int paramLength = methodBinding.parameters.length;
		this.generateInlinedValue(paramLength);
		this.newArray(
			scope,
			new ArrayBinding(scope.getType(TypeBinding.JAVA_LANG_CLASS), 1));
		if (paramLength > 0) {
			this.dup();
			for (int i = 0; i < paramLength; i++) {
				this.generateInlinedValue(i);
				TypeBinding parameter = methodBinding.parameters[i];
				if (parameter.isBaseType()) {
					this.getTYPE(parameter.id);
				} else
					if (parameter.isArrayType()) {
						ArrayBinding array = (ArrayBinding) parameter;
						if (array.leafComponentType.isBaseType()) {
							this.getTYPE(array.leafComponentType.id);
						} else {
							this.ldc(
								String.valueOf(array.leafComponentType.constantPoolName()).replace('/', '.'));
							this.invokeClassForName();
						}
						int dimensions = array.dimensions;
						this.generateInlinedValue(dimensions);
						this.newarray(T_int);
						this.invokeArrayNewInstance();
						this.invokeObjectGetClass();
					} else {
						// parameter is a reference binding
						this.ldc(
							String.valueOf(methodBinding.declaringClass.constantPoolName()).replace(
								'/',
								'.'));
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

	public void getBaseTypeValue(int baseTypeID) {
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		switch (baseTypeID) {
			case T_byte :
				// invokevirtual: byteValue()
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangByteByteValue());
				break;
			case T_short :
				// invokevirtual: shortValue()
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangShortShortValue());
				break;
			case T_char :
				// invokevirtual: charValue()
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangCharacterCharValue());
				break;
			case T_int :
				// invokevirtual: intValue()
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangIntegerIntValue());
				break;
			case T_long :
				// invokevirtual: longValue()
				stackDepth++;
				if (stackDepth > stackMax)
					stackMax = stackDepth;
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangLongLongValue());
				break;
			case T_float :
				// invokevirtual: floatValue()
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangFloatFloatValue());
				break;
			case T_double :
				// invokevirtual: doubleValue()
				stackDepth++;
				if (stackDepth > stackMax)
					stackMax = stackDepth;
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangDoubleDoubleValue());
				break;
			case T_boolean :
				// invokevirtual: booleanValue()
				writeUnsignedShort(
					((CodeSnippetConstantPool) constantPool)
						.literalIndexForJavaLangBooleanBooleanValue());
		}
	}

	protected void invokeAccessibleObjectSetAccessible() {
		// invokevirtual: java.lang.reflect.AccessibleObject.setAccessible(Z)V;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangReflectAccessibleObjectSetAccessible());
		stackDepth -= 2;
	}

	protected void invokeArrayNewInstance() {
		// invokestatic: java.lang.reflect.Array.newInstance(Ljava.lang.Class;int[])Ljava.lang.reflect.Array;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokestatic;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokestatic);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangReflectArrayNewInstance());
		stackDepth--;
	}

	protected void invokeClassGetDeclaredConstructor() {
		// invokevirtual: java.lang.Class getDeclaredConstructor([Ljava.lang.Class)Ljava.lang.reflect.Constructor;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangClassGetDeclaredConstructor());
		stackDepth--;
	}

	protected void invokeClassGetDeclaredField() {
		// invokevirtual: java.lang.Class.getDeclaredField(Ljava.lang.String)Ljava.lang.reflect.Field;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangClassGetDeclaredField());
		stackDepth--;
	}

	protected void invokeClassGetDeclaredMethod() {
		// invokevirtual: java.lang.Class getDeclaredMethod(Ljava.lang.String, [Ljava.lang.Class)Ljava.lang.reflect.Method;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangClassGetDeclaredMethod());
		stackDepth -= 2;
	}

	protected void invokeJavaLangReflectConstructorNewInstance() {
		// invokevirtual: java.lang.reflect.Constructor.newInstance([Ljava.lang.Object;)Ljava.lang.Object;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangReflectConstructorNewInstance());
		stackDepth--;
	}

	protected void invokeJavaLangReflectFieldGetter(int typeID) {
		countLabels = 0;
		int usedTypeID;
		if (typeID == T_null)
			usedTypeID = T_Object;
		else
			usedTypeID = typeID;
		// invokevirtual
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			(
				(CodeSnippetConstantPool) constantPool).literalIndexJavaLangReflectFieldGetter(
				typeID));
		if ((usedTypeID != T_long) && (usedTypeID != T_double)) {
			stackDepth--;
		}
	}

	protected void invokeJavaLangReflectFieldSetter(int typeID) {
		countLabels = 0;
		int usedTypeID;
		if (typeID == T_null)
			usedTypeID = T_Object;
		else
			usedTypeID = typeID;
		// invokevirtual
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			(
				(CodeSnippetConstantPool) constantPool).literalIndexJavaLangReflectFieldSetter(
				typeID));
		if ((usedTypeID != T_long) && (usedTypeID != T_double)) {
			stackDepth -= 3;
		} else {
			stackDepth -= 4;
		}
	}

	protected void invokeJavaLangReflectMethodInvoke() {
		// invokevirtual: java.lang.reflect.Method.invoke(Ljava.lang.Object;[Ljava.lang.Object;)Ljava.lang.Object;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangReflectMethodInvoke());
		stackDepth -= 2;
	}

	protected void invokeObjectGetClass() {
		// invokevirtual: java.lang.Object.getClass()Ljava.lang.Class;
		countLabels = 0;
		try {
			position++;
			bCodeStream[classFileOffset++] = OPC_invokevirtual;
		} catch (IndexOutOfBoundsException e) {
			resizeByteArray(OPC_invokevirtual);
		}
		writeUnsignedShort(
			((CodeSnippetConstantPool) constantPool)
				.literalIndexForJavaLangObjectGetClass());
	}

}
