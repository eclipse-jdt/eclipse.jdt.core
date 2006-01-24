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
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class SourceTypeBinding extends ReferenceBinding {
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces;
	public FieldBinding[] fields;
	public MethodBinding[] methods;
	public ReferenceBinding[] memberTypes;
    public TypeVariableBinding[] typeVariables;

	public ClassScope scope;

	// Synthetics are separated into 5 categories: methods, super methods, fields, class literals, changed declaring type bindings and bridge methods
	public final static int METHOD_EMUL = 0;
	public final static int FIELD_EMUL = 1;
	public final static int CLASS_LITERAL_EMUL = 2;
	public final static int RECEIVER_TYPE_EMUL = 3;
	HashMap[] synthetics;
	char[] genericReferenceTypeSignature;

	private SimpleLookupTable storedAnnotations = null; // keys are this ReferenceBinding & its fields and methods, value is an AnnotationHolder

public SourceTypeBinding(char[][] compoundName, PackageBinding fPackage, ClassScope scope) {
	this.compoundName = compoundName;
	this.fPackage = fPackage;
	this.fileName = scope.referenceCompilationUnit().getFileName();
	this.modifiers = scope.referenceContext.modifiers;
	this.sourceName = scope.referenceContext.name;
	this.scope = scope;

	// expect the fields & methods to be initialized correctly later
	this.fields = Binding.NO_FIELDS;
	this.methods = Binding.NO_METHODS;

	computeId();
}
private void addDefaultAbstractMethod(MethodBinding abstractMethod) {
	MethodBinding defaultAbstract = new MethodBinding(
		abstractMethod.modifiers | ExtraCompilerModifiers.AccDefaultAbstract,
		abstractMethod.selector,
		abstractMethod.returnType,
		abstractMethod.parameters,
		abstractMethod.thrownExceptions,
		this);

	MethodBinding[] temp = new MethodBinding[methods.length + 1];
	System.arraycopy(methods, 0, temp, 0, methods.length);
	temp[methods.length] = defaultAbstract;
	methods = temp;
}
public void addDefaultAbstractMethods() {
	if ((tagBits & TagBits.KnowsDefaultAbstractMethods) != 0) return;

	tagBits |= TagBits.KnowsDefaultAbstractMethods;
	if (isClass() && isAbstract()) {
		if (this.scope.compilerOptions().targetJDK >= ClassFileConstants.JDK1_2)
			return; // no longer added for post 1.2 targets

		ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
		int lastPosition = 0;
		interfacesToVisit[lastPosition] = superInterfaces();
		for (int i = 0; i <= lastPosition; i++) {
			ReferenceBinding[] interfaces = interfacesToVisit[i];
			for (int j = 0, length = interfaces.length; j < length; j++) {
				ReferenceBinding superType = interfaces[j];
				if (superType.isValidBinding()) {
					MethodBinding[] superMethods = superType.methods();
					for (int m = superMethods.length; --m >= 0;) {
						MethodBinding method = superMethods[m];
						if (!implementsMethod(method))
							addDefaultAbstractMethod(method);
					}

					ReferenceBinding[] itsInterfaces = superType.superInterfaces();
					if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
						if (++lastPosition == interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
						interfacesToVisit[lastPosition] = itsInterfaces;
					}
				}
			}
		}
	}
}
/* Add a new synthetic field for <actualOuterLocalVariable>.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForInnerclass(LocalVariableBinding actualOuterLocalVariable) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[FIELD_EMUL] == null)
		synthetics[FIELD_EMUL] = new HashMap(5);
	
	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get(actualOuterLocalVariable);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, actualOuterLocalVariable.name), 
			actualOuterLocalVariable.type, 
			ClassFileConstants.AccPrivate | ClassFileConstants.AccFinal | ClassFileConstants.AccSynthetic, 
			this, 
			Constant.NotAConstant,
			synthetics[FIELD_EMUL].size());
		synthetics[FIELD_EMUL].put(actualOuterLocalVariable, synthField);
	}

	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 1;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX,
						actualOuterLocalVariable.name,
						("$" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic field for <enclosingType>.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForInnerclass(ReferenceBinding enclosingType) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[FIELD_EMUL] == null)
		synthetics[FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get(enclosingType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(
				TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX,
				String.valueOf(enclosingType.depth()).toCharArray()),
			enclosingType,
			ClassFileConstants.AccDefault | ClassFileConstants.AccFinal | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			synthetics[FIELD_EMUL].size());
		synthetics[FIELD_EMUL].put(enclosingType, synthField);
	}
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					if (this.scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5) {
						synthField.name = CharOperation.concat(
							synthField.name,
							"$".toCharArray()); //$NON-NLS-1$
						needRecheck = true;
					} else {
						scope.problemReporter().duplicateFieldInType(this, fieldDecl);
					}
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic field for a class literal access.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForClassLiteral(TypeBinding targetType, BlockScope blockScope) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[CLASS_LITERAL_EMUL] == null)
		synthetics[CLASS_LITERAL_EMUL] = new HashMap(5);

	// use a different table than FIELDS, given there might be a collision between emulation of X.this$0 and X.class.
	FieldBinding synthField = (FieldBinding) synthetics[CLASS_LITERAL_EMUL].get(targetType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(
				TypeConstants.SYNTHETIC_CLASS,
				String.valueOf(synthetics[CLASS_LITERAL_EMUL].size()).toCharArray()),
			blockScope.getJavaLangClass(),
			ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			synthetics[CLASS_LITERAL_EMUL].size());
		synthetics[CLASS_LITERAL_EMUL].put(targetType, synthField);
	}
	// ensure there is not already such a field defined by the user
	FieldBinding existingField;
	if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
		TypeDeclaration typeDecl = blockScope.referenceType();
		for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
			FieldDeclaration fieldDecl = typeDecl.fields[i];
			if (fieldDecl.binding == existingField) {
				blockScope.problemReporter().duplicateFieldInType(this, fieldDecl);
				break;
			}
		}
	}		
	return synthField;
}
/* Add a new synthetic field for the emulation of the assert statement.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForAssert(BlockScope blockScope) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[FIELD_EMUL] == null)
		synthetics[FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get("assertionEmulation"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			TypeConstants.SYNTHETIC_ASSERT_DISABLED,
			TypeBinding.BOOLEAN,
			ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic | ClassFileConstants.AccFinal,
			this,
			Constant.NotAConstant,
			synthetics[FIELD_EMUL].size());
		synthetics[FIELD_EMUL].put("assertionEmulation", synthField); //$NON-NLS-1$
	}
	// ensure there is not already such a field defined by the user
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_ASSERT_DISABLED,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic field for recording all enum constant values
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForEnumValues() {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[FIELD_EMUL] == null)
		synthetics[FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get("enumConstantValues"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			TypeConstants.SYNTHETIC_ENUM_VALUES,
			scope.createArrayType(this,1),
			ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic | ClassFileConstants.AccFinal,
			this,
			Constant.NotAConstant,
			synthetics[FIELD_EMUL].size());
		synthetics[FIELD_EMUL].put("enumConstantValues", synthField); //$NON-NLS-1$
	}
	// ensure there is not already such a field defined by the user
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_ENUM_VALUES,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic access method for read/write access to <targetField>.
	Answer the new method or the existing method if one already existed.
*/
public SyntheticMethodBinding addSyntheticMethod(FieldBinding targetField, boolean isReadAccess) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[METHOD_EMUL] == null)
		synthetics[METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(targetField);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(targetField, isReadAccess, this);
		synthetics[METHOD_EMUL].put(targetField, accessors = new SyntheticMethodBinding[2]);
		accessors[isReadAccess ? 0 : 1] = accessMethod;		
	} else {
		if ((accessMethod = accessors[isReadAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticMethodBinding(targetField, isReadAccess, this);
			accessors[isReadAccess ? 0 : 1] = accessMethod;
		}
	}
	return accessMethod;
}
/* Add a new synthetic method the enum type. Selector can either be 'values' or 'valueOf'.
 * char[] constants from TypeConstants must be used: TypeConstants.VALUES/VALUEOF
*/
public SyntheticMethodBinding addSyntheticEnumMethod(char[] selector) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[METHOD_EMUL] == null)
		synthetics[METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(selector);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(this, selector);
		synthetics[METHOD_EMUL].put(selector, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;		
	} else {
		if ((accessMethod = accessors[0]) == null) {
			accessMethod = new SyntheticMethodBinding(this, selector);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}
/*
 * Add a synthetic field to handle the cache of the switch translation table for the corresponding enum type 
 */
public SyntheticFieldBinding addSyntheticFieldForSwitchEnum(char[] fieldName, String key) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[FIELD_EMUL] == null)
		synthetics[FIELD_EMUL] = new HashMap(5);

	SyntheticFieldBinding synthField = (SyntheticFieldBinding) synthetics[FIELD_EMUL].get(key);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			fieldName,
			scope.createArrayType(TypeBinding.INT,1),
			ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			synthetics[FIELD_EMUL].size());
		synthetics[FIELD_EMUL].put(key, synthField);
	}
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						fieldName,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic method the enum type. Selector can either be 'values' or 'valueOf'.
 * char[] constants from TypeConstants must be used: TypeConstants.VALUES/VALUEOF
*/
public SyntheticMethodBinding addSyntheticMethodForSwitchEnum(TypeBinding enumBinding) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[METHOD_EMUL] == null)
		synthetics[METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	char[] selector = CharOperation.concat(TypeConstants.SYNTHETIC_SWITCH_ENUM_TABLE, enumBinding.constantPoolName());
	CharOperation.replace(selector, '/', '$');
	final String key = new String(selector);
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(key);
	// first add the corresponding synthetic field
	if (accessors == null) {
		// then create the synthetic method
		final SyntheticFieldBinding fieldBinding = this.addSyntheticFieldForSwitchEnum(selector, key);
		accessMethod = new SyntheticMethodBinding(fieldBinding, this, enumBinding, selector);
		synthetics[METHOD_EMUL].put(key, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			final SyntheticFieldBinding fieldBinding = this.addSyntheticFieldForSwitchEnum(selector, key);
			accessMethod = new SyntheticMethodBinding(fieldBinding, this, enumBinding, selector);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}
/* Add a new synthetic access method for access to <targetMethod>.
 * Must distinguish access method used for super access from others (need to use invokespecial bytecode)
	Answer the new method or the existing method if one already existed.
*/
public SyntheticMethodBinding addSyntheticMethod(MethodBinding targetMethod, boolean isSuperAccess) {
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[METHOD_EMUL] == null)
		synthetics[METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(targetMethod);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(targetMethod, isSuperAccess, this);
		synthetics[METHOD_EMUL].put(targetMethod, accessors = new SyntheticMethodBinding[2]);
		accessors[isSuperAccess ? 0 : 1] = accessMethod;		
	} else {
		if ((accessMethod = accessors[isSuperAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticMethodBinding(targetMethod, isSuperAccess, this);
			accessors[isSuperAccess ? 0 : 1] = accessMethod;
		}
	}
	return accessMethod;
}
/* 
 * Record the fact that bridge methods need to be generated to override certain inherited methods
 */
public SyntheticMethodBinding addSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge, MethodBinding targetMethod) {
	if (isInterface()) return null; // only classes & enums get bridge methods
	// targetMethod may be inherited
	if (inheritedMethodToBridge.returnType.erasure() == targetMethod.returnType.erasure()
		&& inheritedMethodToBridge.areParameterErasuresEqual(targetMethod)) {
			return null; // do not need bridge method
	}
	if (synthetics == null)
		synthetics = new HashMap[4];
	if (synthetics[METHOD_EMUL] == null) {
		synthetics[METHOD_EMUL] = new HashMap(5);
	} else {
		// check to see if there is another equivalent inheritedMethod already added
		Iterator synthMethods = synthetics[METHOD_EMUL].keySet().iterator();
		while (synthMethods.hasNext()) {
			Object synthetic = synthMethods.next();
			if (synthetic instanceof MethodBinding) {
				MethodBinding method = (MethodBinding) synthetic;
				if (CharOperation.equals(inheritedMethodToBridge.selector, method.selector)
					&& inheritedMethodToBridge.returnType.erasure() == method.returnType.erasure()
					&& inheritedMethodToBridge.areParameterErasuresEqual(method)) {
						return null;
				}
			}
		}
	}

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, targetMethod, this);
		synthetics[METHOD_EMUL].put(inheritedMethodToBridge, accessors = new SyntheticMethodBinding[2]);
		accessors[1] = accessMethod;		
	} else {
		if ((accessMethod = accessors[1]) == null) {
			accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, targetMethod, this);
			accessors[1] = accessMethod;
		}
	}
	return accessMethod;
}
public int kind() {
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) return Binding.GENERIC_TYPE;
	return Binding.TYPE;
}
public char[] computeUniqueKey(boolean isLeaf) {
	char[] uniqueKey = super.computeUniqueKey(isLeaf);
	if (uniqueKey.length == 2) return uniqueKey; // problem type's unique key is "L;"
	if (Util.isClassFileName(this.fileName)) return uniqueKey; // no need to insert compilation unit name for a .class file
	
	// insert compilation unit name if the type name is not the main type name
	int end = CharOperation.lastIndexOf('.', this.fileName);
	if (end != -1) {
		int start = CharOperation.lastIndexOf('/', this.fileName) + 1;
		char[] mainTypeName = CharOperation.subarray(this.fileName, start, end);
		start = CharOperation.lastIndexOf('/', uniqueKey) + 1;
		if (start == 0)
			start = 1; // start after L
		end = CharOperation.indexOf('$', uniqueKey, start);
		if (end == -1)
			end = CharOperation.indexOf('<', uniqueKey, start);
		if (end == -1)
			end = CharOperation.indexOf(';', uniqueKey, start);
		char[] topLevelType = CharOperation.subarray(uniqueKey, start, end);
		if (!CharOperation.equals(topLevelType, mainTypeName)) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(uniqueKey, 0, start);
			buffer.append(mainTypeName);
			buffer.append('~');
			buffer.append(topLevelType);
			buffer.append(uniqueKey, end, uniqueKey.length - end);
			int length = buffer.length();
			uniqueKey = new char[length];
			buffer.getChars(0, length, uniqueKey, 0);
			return uniqueKey;
		}
	}
	return uniqueKey;
}
void faultInTypesForFieldsAndMethods() {
	// check @Deprecated annotation
	if ((this.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0) {
		this.modifiers |= ClassFileConstants.AccDeprecated;
		// REVIEW already done in getAnnotationsTagBits
	}
	ReferenceBinding enclosingType = this.enclosingType();
	if (enclosingType != null && enclosingType.isViewedAsDeprecated() && !this.isDeprecated())
		modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	fields();
	methods();

	for (int i = 0, length = memberTypes.length; i < length; i++)
		((SourceTypeBinding) memberTypes[i]).faultInTypesForFieldsAndMethods();
}
// NOTE: the type of each field of a source type is resolved when needed
public FieldBinding[] fields() {
	if ((tagBits & TagBits.AreFieldsComplete) != 0)
		return fields;	

	int failed = 0;
	try {
		for (int i = 0, length = fields.length; i < length; i++) {
			if (resolveTypeFor(fields[i]) == null) {
				fields[i] = null;
				failed++;
			}
		}
	} finally {
		if (failed > 0) {
			// ensure fields are consistent reqardless of the error
			int newSize = fields.length - failed;
			if (newSize == 0)
				return fields = Binding.NO_FIELDS;

			FieldBinding[] newFields = new FieldBinding[newSize];
			for (int i = 0, j = 0, length = fields.length; i < length; i++)
				if (fields[i] != null)
					newFields[j++] = fields[i];
			fields = newFields;
		}
	}
	tagBits |= TagBits.AreFieldsComplete;
	return fields;
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#genericTypeSignature()
 */
public char[] genericTypeSignature() {
    if (this.genericReferenceTypeSignature == null)
    	this.genericReferenceTypeSignature = computeGenericTypeSignature(this.typeVariables);
    return this.genericReferenceTypeSignature;
}
/**
 * <param1 ... paramN>superclass superinterface1 ... superinterfaceN
 * <T:LY<TT;>;U:Ljava/lang/Object;V::Ljava/lang/Runnable;:Ljava/lang/Cloneable;:Ljava/util/Map;>Ljava/lang/Exception;Ljava/lang/Runnable;
 */
public char[] genericSignature() {
    StringBuffer sig = null;
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
	    sig = new StringBuffer(10);
	    sig.append('<');
	    for (int i = 0, length = this.typeVariables.length; i < length; i++)
	        sig.append(this.typeVariables[i].genericSignature());
	    sig.append('>');
	} else {
	    // could still need a signature if any of supertypes is parameterized
	    noSignature: if (this.superclass == null || !this.superclass.isParameterizedType()) {
		    for (int i = 0, length = this.superInterfaces.length; i < length; i++)
		        if (this.superInterfaces[i].isParameterizedType())
					break noSignature;
	        return null;
	    }
	    sig = new StringBuffer(10);
	}
	if (this.superclass != null)
		sig.append(this.superclass.genericTypeSignature());
	else // interface scenario only (as Object cannot be generic) - 65953
		sig.append(scope.getJavaLangObject().genericTypeSignature());
    for (int i = 0, length = this.superInterfaces.length; i < length; i++)
        sig.append(this.superInterfaces[i].genericTypeSignature());
	return sig.toString().toCharArray();
}

/**
 * Compute the tagbits for standard annotations. For source types, these could require
 * lazily resolving corresponding annotation nodes, in case of forward references.
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
public long getAnnotationTagBits() {
	if ((this.tagBits & TagBits.AnnotationResolved) == 0) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0)
			this.modifiers |= ClassFileConstants.AccDeprecated;
	}
	return this.tagBits;
}
public MethodBinding[] getDefaultAbstractMethods() {
	int count = 0;
	for (int i = methods.length; --i >= 0;)
		if (methods[i].isDefaultAbstract())
			count++;
	if (count == 0) return Binding.NO_METHODS;

	MethodBinding[] result = new MethodBinding[count];
	count = 0;
	for (int i = methods.length; --i >= 0;)
		if (methods[i].isDefaultAbstract())
			result[count++] = methods[i];
	return result;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;

	if ((tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		nextMethod : for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];
			if (method.selector == TypeConstants.INIT && method.parameters.length == argCount) {
				TypeBinding[] toMatch = method.parameters;
				for (int p = 0; p < argCount; p++)
					if (toMatch[p] != argumentTypes[p])
						continue nextMethod;
				return method;
			}
		}
	} else {
		MethodBinding[] constructors = getMethods(TypeConstants.INIT); // takes care of duplicates & default abstract methods
		nextConstructor : for (int c = constructors.length; --c >= 0;) {
			MethodBinding constructor = constructors[c];
			TypeBinding[] toMatch = constructor.parameters;
			if (toMatch.length == argCount) {
				for (int p = 0; p < argCount; p++)
					if (toMatch[p] != argumentTypes[p])
						continue nextConstructor;
				return constructor;
			}
		}
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
// searches up the hierarchy as long as no potential (but not exact) match was found.
public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	// sender from refScope calls recordTypeReference(this)
	int argCount = argumentTypes.length;
	int selectorLength = selector.length;
	boolean foundNothing = true;

	if ((tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		nextMethod : for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];
			if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
				foundNothing = false; // inner type lookups must know that a method with this name exists
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextMethod;
					return method;
				}
			}
		}
	} else {
		MethodBinding[] matchingMethods = getMethods(selector); // takes care of duplicates & default abstract methods
		foundNothing = matchingMethods == Binding.NO_METHODS;
		nextMethod : for (int m = matchingMethods.length; --m >= 0;) {
			MethodBinding method = matchingMethods[m];
			TypeBinding[] toMatch = method.parameters;
			if (toMatch.length == argCount) {
				for (int p = 0; p < argCount; p++)
					if (toMatch[p] != argumentTypes[p])
						continue nextMethod;
				return method;
			}
		}
	}

	if (foundNothing) {
		if (isInterface()) {
			 if (superInterfaces.length == 1) {
				if (refScope != null)
					refScope.recordTypeReference(superInterfaces[0]);
				return superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
			 }
		} else if (superclass != null) {
			if (refScope != null)
				refScope.recordTypeReference(superclass);
			return superclass.getExactMethod(selector, argumentTypes, refScope);
		}
	}
	return null;
}
// NOTE: the type of a field of a source type is resolved when needed
public FieldBinding getField(char[] fieldName, boolean needResolve) {
	// always resolve anyway on source types
	int fieldLength = fieldName.length;
	for (int i = 0, length = fields.length; i < length; i++) {
		FieldBinding field = fields[i];
		if (field.name.length == fieldLength && CharOperation.equals(field.name, fieldName)) {
			FieldBinding result = null;
			try {
				result = resolveTypeFor(field);
				return result;
			} finally {
				if (result == null) {
					// ensure fields are consistent reqardless of the error
					int newSize = fields.length - 1;
					if (newSize == 0) {
						fields = Binding.NO_FIELDS;
					} else {
						FieldBinding[] newFields = new FieldBinding[newSize];
						System.arraycopy(fields, 0, newFields, 0, i);
						System.arraycopy(fields, i + 1, newFields, i, newSize - i);
						fields = newFields;
					}
				}
			}
		}
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding[] getMethods(char[] selector) {
	int selectorLength = selector.length;
	boolean methodsAreResolved = (tagBits & TagBits.AreMethodsComplete) != 0; // have resolved all arg types & return type of the methods
	java.util.ArrayList matchingMethods = null;
	for (int i = 0, length = methods.length; i < length; i++) {
		MethodBinding method = methods[i];
		if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
			if (!methodsAreResolved && resolveTypesFor(method) == null || method.returnType == null) {
				methods();
				return getMethods(selector); // try again since the problem methods have been removed
			}
			if (matchingMethods == null)
				matchingMethods = new java.util.ArrayList(2);
			matchingMethods.add(method);
		}
	}
	if (matchingMethods == null)
		return Binding.NO_METHODS;

	MethodBinding[] result = new MethodBinding[matchingMethods.size()];
	matchingMethods.toArray(result);
	if (!methodsAreResolved) {
		boolean isSource15 = this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		for (int i = 0, length = result.length - 1; i < length; i++) {
			MethodBinding method = result[i];
			for (int j = length; j > i; j--) {
				boolean paramsMatch = isSource15
					? method.areParameterErasuresEqual(result[j])
					: method.areParametersEqual(result[j]);
				if (paramsMatch) {
					methods();
					return getMethods(selector); // try again since the duplicate methods have been removed
				}
			}
		}
	}
	return result;
}
/* Answer the synthetic field for <actualOuterLocalVariable>
*	or null if one does not exist.
*/
public FieldBinding getSyntheticField(LocalVariableBinding actualOuterLocalVariable) {
	if (synthetics == null || synthetics[FIELD_EMUL] == null) return null;
	return (FieldBinding) synthetics[FIELD_EMUL].get(actualOuterLocalVariable);
}
/* Answer the synthetic field for <targetEnclosingType>
*	or null if one does not exist.
*/
public FieldBinding getSyntheticField(ReferenceBinding targetEnclosingType, boolean onlyExactMatch) {

	if (synthetics == null || synthetics[FIELD_EMUL] == null) return null;
	FieldBinding field = (FieldBinding) synthetics[FIELD_EMUL].get(targetEnclosingType);
	if (field != null) return field;

	// type compatibility : to handle cases such as
	// class T { class M{}}
	// class S extends T { class N extends M {}} --> need to use S as a default enclosing instance for the super constructor call in N().
	if (!onlyExactMatch){
		Iterator accessFields = synthetics[FIELD_EMUL].values().iterator();
		while (accessFields.hasNext()) {
			field = (FieldBinding) accessFields.next();
			if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX, field.name)
				&& ((ReferenceBinding) field.type).findSuperTypeWithSameErasure(targetEnclosingType) != null)
					return field;
		}
	}
	return null;
}
/* 
 * Answer the bridge method associated for an  inherited methods or null if one does not exist
 */
public SyntheticMethodBinding getSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge) {
	if (synthetics == null) return null;
	if (synthetics[METHOD_EMUL] == null) return null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) return null;
	return accessors[1];
}

/**
 * Compute the tagbits for @Deprecated annotations; avoiding resolving
 * entire annotation if not necessary.
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#initializeDeprecatedAnnotationTagBits()
 */
public void initializeDeprecatedAnnotationTagBits() {
	if ((this.tagBits & (TagBits.AnnotationResolved|TagBits.AnnotationDeprecated)) == 0) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveDeprecatedAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0)
			this.modifiers |= ClassFileConstants.AccDeprecated;
	}
}

/**
 * Returns true if a type is identical to another one,
 * or for generic types, true if compared to its raw type.
 */
public boolean isEquivalentTo(TypeBinding otherType) {

	if (this == otherType) return true;
	if (otherType == null) return false;
	switch(otherType.kind()) {

		case Binding.WILDCARD_TYPE :
			return ((WildcardBinding) otherType).boundCheck(this);

		case Binding.PARAMETERIZED_TYPE :
			if ((otherType.tagBits & TagBits.HasDirectWildcard) == 0 && (!this.isMemberType() || !otherType.isMemberType())) 
				return false; // should have been identical
			ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
			if (this != otherParamType.type) 
				return false;
			if (!isStatic()) { // static member types do not compare their enclosing
            	ReferenceBinding enclosing = enclosingType();
            	if (enclosing != null) {
            		ReferenceBinding otherEnclosing = otherParamType.enclosingType();
            		if (otherEnclosing == null) return false;
            		if ((otherEnclosing.tagBits & TagBits.HasDirectWildcard) == 0) {
						if (enclosing != otherEnclosing) return false;
            		} else {
            			if (!enclosing.isEquivalentTo(otherParamType.enclosingType())) return false;
            		}
            	}				
			}
			int length = this.typeVariables == null ? 0 : this.typeVariables.length;
			TypeBinding[] otherArguments = otherParamType.arguments;
			int otherLength = otherArguments == null ? 0 : otherArguments.length;
			if (otherLength != length) 
				return false;
			for (int i = 0; i < length; i++)
				if (!this.typeVariables[i].isTypeArgumentContainedBy(otherArguments[i]))
					return false;
			return true;

		case Binding.RAW_TYPE :
	        return otherType.erasure() == this;
	}
	return false;
}
public boolean isGenericType() {
    return this.typeVariables != Binding.NO_TYPE_VARIABLES;
}
public ReferenceBinding[] memberTypes() {
	return this.memberTypes;
}
public FieldBinding getUpdatedFieldBinding(FieldBinding targetField, ReferenceBinding newDeclaringClass) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[4];
	if (this.synthetics[RECEIVER_TYPE_EMUL] == null)
		this.synthetics[RECEIVER_TYPE_EMUL] = new HashMap(5);

	Hashtable fieldMap = (Hashtable) this.synthetics[RECEIVER_TYPE_EMUL].get(targetField);
	if (fieldMap == null) {
		fieldMap = new Hashtable(5);
		this.synthetics[RECEIVER_TYPE_EMUL].put(targetField, fieldMap);
	}
	FieldBinding updatedField = (FieldBinding) fieldMap.get(newDeclaringClass);
	if (updatedField == null){
		updatedField = new FieldBinding(targetField, newDeclaringClass);
		fieldMap.put(newDeclaringClass, updatedField);
	}
	return updatedField;
}
public MethodBinding getUpdatedMethodBinding(MethodBinding targetMethod, ReferenceBinding newDeclaringClass) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[4];
	if (this.synthetics[RECEIVER_TYPE_EMUL] == null)
		this.synthetics[RECEIVER_TYPE_EMUL] = new HashMap(5);

	Hashtable methodMap = (Hashtable) synthetics[RECEIVER_TYPE_EMUL].get(targetMethod);
	if (methodMap == null) {
		methodMap = new Hashtable(5);
		this.synthetics[RECEIVER_TYPE_EMUL].put(targetMethod, methodMap);
	}
	MethodBinding updatedMethod = (MethodBinding) methodMap.get(newDeclaringClass);
	if (updatedMethod == null){
		updatedMethod = new MethodBinding(targetMethod, newDeclaringClass);
		methodMap.put(newDeclaringClass, updatedMethod);
	}
	return updatedMethod;
}
public boolean hasMemberTypes() {
    return this.memberTypes.length > 0;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding[] methods() {
	if ((tagBits & TagBits.AreMethodsComplete) != 0)
		return methods;

	int failed = 0;
	try {
		for (int i = 0, length = methods.length; i < length; i++) {
			if (resolveTypesFor(methods[i]) == null) {
				methods[i] = null; // unable to resolve parameters
				failed++;
			}
		}

		// find & report collision cases
		boolean complyTo15 = this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		for (int i = 0, length = methods.length; i < length; i++) {
			MethodBinding method = methods[i];
			if (method != null) {
				char[] selector = method.selector;
				AbstractMethodDeclaration methodDecl = null;
				nextMethod : for (int j = length - 1; j > i; j--) {
					MethodBinding method2 = methods[j];
					if (method2 == null || !CharOperation.equals(selector, method2.selector))
						continue nextMethod;
					if (complyTo15 && method.returnType != null && method2.returnType != null) {
						// 8.4.2, for collision to be detected between m1 and m2:
						// signature(m1) == signature(m2) i.e. same arity, same type parameter count, can be substituted
						// signature(m1) == erasure(signature(m2)) or erasure(signature(m1)) == signature(m2)
						TypeBinding[] params1 = method.parameters;
						TypeBinding[] params2 = method2.parameters;
						int pLength = params1.length;
						if (pLength != params2.length)
							continue nextMethod;

						TypeVariableBinding[] vars = method.typeVariables;
						TypeVariableBinding[] vars2 = method2.typeVariables;
						boolean equalTypeVars = vars == vars2;
						MethodBinding subMethod = method2;
						if (!equalTypeVars) {
							MethodBinding temp = method.computeSubstitutedMethod(method2, this.scope.environment());
							if (temp != null) {
								equalTypeVars = true;
								subMethod = temp;
							}
						}
						boolean equalParams = method.areParametersEqual(subMethod);
						if (equalParams && equalTypeVars) {
							// duplicates regardless of return types
						} else if (method.returnType.erasure() == subMethod.returnType.erasure() && (equalParams || method.areParameterErasuresEqual(method2))) {
							// name clash for sure if not duplicates, report as duplicates
						} else if (!equalTypeVars && vars != Binding.NO_TYPE_VARIABLES && vars2 != Binding.NO_TYPE_VARIABLES) {
							// type variables are different so we can distinguish between methods
							continue nextMethod;
						} else if (pLength > 0) {
							// check to see if the erasure of either method is equal to the other
							int index = pLength;
							for (; --index >= 0;) {
								if (params1[index] != params2[index].erasure())
									break;
								if (params1[index] == params2[index]) {
									TypeBinding type = params1[index].leafComponentType();
									if (type instanceof SourceTypeBinding && type.typeVariables() != Binding.NO_TYPE_VARIABLES) {
										index = pLength; // handle comparing identical source types like X<T>... its erasure is itself BUT we need to answer false
										break;
									}
								}
							}
							if (index >= 0 && index < pLength) {
								for (index = pLength; --index >= 0;)
									if (params1[index].erasure() != params2[index])
										break;
							}
							if (index >= 0)
								continue nextMethod;
						}
					} else if (!method.areParametersEqual(method2)) { // prior to 1.5, parameter identity meant a collision case
						continue nextMethod;
					}

					// report duplicate
					boolean isEnumSpecialMethod = isEnum()
						&& (selector == TypeConstants.VALUEOF || selector == TypeConstants.VALUES);
					if (methodDecl == null) {
						methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost & may still be null if method is special
						if (methodDecl != null && methodDecl.binding != null) { // ensure its a valid user defined method
							if (isEnumSpecialMethod)
								scope.problemReporter().duplicateEnumSpecialMethod(this, methodDecl);
							else
								scope.problemReporter().duplicateMethodInType(this, methodDecl);
							methodDecl.binding = null;
							methods[i] = null;
							failed++;
						}
					}
					AbstractMethodDeclaration method2Decl = method2.sourceMethod();
					if (method2Decl != null && method2Decl.binding != null) { // ensure its a valid user defined method
						if (isEnumSpecialMethod)
							scope.problemReporter().duplicateEnumSpecialMethod(this, method2Decl);
						else
							scope.problemReporter().duplicateMethodInType(this, method2Decl);
						method2Decl.binding = null;
						methods[j] = null;
						failed++;
					}
				}
				if (method.returnType == null && methodDecl == null) { // forget method with invalid return type... was kept to detect possible collisions
					method.sourceMethod().binding = null;
					methods[i] = null;
					failed++;
				}
			}
		}
	} finally {
		if (failed > 0) {
			int newSize = methods.length - failed;
			if (newSize == 0) {
				methods = Binding.NO_METHODS;
			} else {
				MethodBinding[] newMethods = new MethodBinding[newSize];
				for (int i = 0, j = 0, length = methods.length; i < length; i++)
					if (methods[i] != null)
						newMethods[j++] = methods[i];
				methods = newMethods;
			}
		}

		// handle forward references to potential default abstract methods
		addDefaultAbstractMethods();
		tagBits |= TagBits.AreMethodsComplete;
	}		
	return methods;
}
private FieldBinding resolveTypeFor(FieldBinding field) {
	if ((field.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return field;

	if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
		if ((field.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0)
			field.modifiers |= ClassFileConstants.AccDeprecated;
		// REVIEW in the case of fields, getAnnotationTagBits does not mark, whereas it
		//        does it for types
	}
	if (isViewedAsDeprecated() && !field.isDeprecated())
		field.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;	
	if (hasRestrictedAccess())
		field.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;
	FieldDeclaration[] fieldDecls = scope.referenceContext.fields;
	for (int f = 0, length = fieldDecls.length; f < length; f++) {
		if (fieldDecls[f].binding != field)
			continue;

			MethodScope initializationScope = field.isStatic() 
				? this.scope.referenceContext.staticInitializerScope 
				: this.scope.referenceContext.initializerScope;
			FieldBinding previousField = initializationScope.initializedField;
			try {
				initializationScope.initializedField = field;
				FieldDeclaration fieldDecl = fieldDecls[f];
				TypeBinding fieldType = 
					fieldDecl.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT
						? initializationScope.environment().convertToRawType(this) // enum constant is implicitly of declaring enum type
						: fieldDecl.type.resolveType(initializationScope, true /* check bounds*/);
				field.type = fieldType;
				field.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
				if (fieldType == null) {
					fieldDecls[f].binding = null;
					return null;
				}
				if (fieldType == TypeBinding.VOID) {
					scope.problemReporter().variableTypeCannotBeVoid(fieldDecls[f]);
					fieldDecls[f].binding = null;
					return null;
				}
				if (fieldType.isArrayType() && ((ArrayBinding) fieldType).leafComponentType == TypeBinding.VOID) {
					scope.problemReporter().variableTypeCannotBeVoidArray(fieldDecls[f]);
					fieldDecls[f].binding = null;
					return null;
				}
				TypeBinding leafType = fieldType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding)leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0) {
					field.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				}				
			} finally {
			    initializationScope.initializedField = previousField;
			}
		return field;
	}
	return null; // should never reach this point
}
private MethodBinding resolveTypesFor(MethodBinding method) {
	if ((method.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return method;

	if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
		if ((method.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0)
			method.modifiers |= ClassFileConstants.AccDeprecated;
	}
	if (isViewedAsDeprecated() && !method.isDeprecated())
		method.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	if (hasRestrictedAccess())
		method.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;

	AbstractMethodDeclaration methodDecl = method.sourceMethod();
	if (methodDecl == null) return null; // method could not be resolved in previous iteration

	TypeParameter[] typeParameters = methodDecl.typeParameters();
	if (typeParameters != null) {
		methodDecl.scope.connectTypeVariables(typeParameters);
		// Perform deferred bound checks for type variables (only done after type variable hierarchy is connected)
		for (int i = 0, paramLength = typeParameters.length; i < paramLength; i++)
			typeParameters[i].checkBounds(methodDecl.scope);
	}
	TypeReference[] exceptionTypes = methodDecl.thrownExceptions;
	if (exceptionTypes != null) {
		int size = exceptionTypes.length;
		method.thrownExceptions = new ReferenceBinding[size];
		ReferenceBinding throwable = this.scope.getJavaLangThrowable();
		int count = 0;
		ReferenceBinding resolvedExceptionType;
		for (int i = 0; i < size; i++) {
			resolvedExceptionType = (ReferenceBinding) exceptionTypes[i].resolveType(methodDecl.scope, true /* check bounds*/);
			if (resolvedExceptionType == null)
				continue;
			if (resolvedExceptionType.isBoundParameterizedType()) {
				methodDecl.scope.problemReporter().invalidParameterizedExceptionType(resolvedExceptionType, exceptionTypes[i]);
				continue;
			}
			if (throwable != resolvedExceptionType && !throwable.isSuperclassOf(resolvedExceptionType)) {
				methodDecl.scope.problemReporter().cannotThrowType(this, methodDecl, exceptionTypes[i], resolvedExceptionType);
				continue;
			}
		    if ((resolvedExceptionType.modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
				method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
			method.thrownExceptions[count++] = resolvedExceptionType;
		}
		if (count < size)
			System.arraycopy(method.thrownExceptions, 0, method.thrownExceptions = new ReferenceBinding[count], 0, count);
	}

	boolean foundArgProblem = false;
	Argument[] arguments = methodDecl.arguments;
	if (arguments != null) {
		int size = arguments.length;
		method.parameters = new TypeBinding[size];
		for (int i = 0; i < size; i++) {
			Argument arg = arguments[i];
			TypeBinding parameterType = arg.type.resolveType(methodDecl.scope, true /* check bounds*/);
			if (parameterType == null) {
				foundArgProblem = true;
			} else if (parameterType == TypeBinding.VOID) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoid(this, methodDecl, arg);
				foundArgProblem = true;
			} else if (parameterType.isArrayType() && ((ArrayBinding) parameterType).leafComponentType == TypeBinding.VOID) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoidArray(this, methodDecl, arg);
				foundArgProblem = true;
			} else {
				TypeBinding leafType = parameterType.leafComponentType();
			    if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				method.parameters[i] = parameterType;
			}
		}
	}

	boolean foundReturnTypeProblem = false;
	if (!method.isConstructor()) {
		TypeReference returnType = methodDecl instanceof MethodDeclaration
			? ((MethodDeclaration) methodDecl).returnType
			: null;
		if (returnType == null) {
			methodDecl.scope.problemReporter().missingReturnType(methodDecl);
			method.returnType = null;
			foundReturnTypeProblem = true;
		} else {
		    TypeBinding methodType = returnType.resolveType(methodDecl.scope, true /* check bounds*/);
			if (methodType == null) {
				foundReturnTypeProblem = true;
			} else if (methodType.isArrayType() && ((ArrayBinding) methodType).leafComponentType == TypeBinding.VOID) {
				methodDecl.scope.problemReporter().returnTypeCannotBeVoidArray(this, (MethodDeclaration) methodDecl);
				foundReturnTypeProblem = true;
			} else {
				method.returnType = methodType;
				TypeBinding leafType = methodType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
			}
		}
	}
	if (foundArgProblem) {
		methodDecl.binding = null;
		method.parameters = Binding.NO_PARAMETERS; // see 107004
		// nullify type parameter bindings as well as they have a backpointer to the method binding
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=81134)
		if (typeParameters != null)
			for (int i = 0, length = typeParameters.length; i < length; i++)
				typeParameters[i].binding = null;
		return null;
	}
	if (foundReturnTypeProblem)
		return method; // but its still unresolved with a null return type & is still connected to its method declaration

	method.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
	return method;
}
AnnotationHolder retrieveAnnotationHolder(Binding binding, boolean forceInitialization) {
	if (forceInitialization)
		binding.getAnnotationTagBits(); // ensure annotations are up to date
	return super.retrieveAnnotationHolder(binding, false);
}
public final int sourceEnd() {
	return scope.referenceContext.sourceEnd;
}
public final int sourceStart() {
	return scope.referenceContext.sourceStart;
}
SimpleLookupTable storedAnnotations(boolean forceInitialize) {
	if (forceInitialize && this.storedAnnotations == null) {
		this.scope.referenceCompilationUnit().compilationResult.hasAnnotations = true;
		if (!this.scope.environment().globalOptions.storeAnnotations)
			return null; // not supported during this compile
		this.storedAnnotations = new SimpleLookupTable(3);
	}
	return this.storedAnnotations;
}
public ReferenceBinding superclass() {
	return superclass;
}
public ReferenceBinding[] superInterfaces() {
	return superInterfaces;
}
// TODO (philippe) could be a performance issue since some senders are building the list just to count them
public SyntheticMethodBinding[] syntheticMethods() {
	
	if (synthetics == null || synthetics[METHOD_EMUL] == null || synthetics[METHOD_EMUL].size() == 0) return null;

	// difficult to compute size up front because of the embedded arrays so assume there is only 1
	int index = 0;
	SyntheticMethodBinding[] bindings = new SyntheticMethodBinding[1];
	Iterator fieldsOrMethods = synthetics[METHOD_EMUL].keySet().iterator();
	while (fieldsOrMethods.hasNext()) {

		Object fieldOrMethod = fieldsOrMethods.next();

		if (fieldOrMethod instanceof MethodBinding) {

			SyntheticMethodBinding[] methodAccessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(fieldOrMethod);
			int numberOfAccessors = 0;
			if (methodAccessors[0] != null) numberOfAccessors++;
			if (methodAccessors[1] != null) numberOfAccessors++;
			if (index + numberOfAccessors > bindings.length)
				System.arraycopy(bindings, 0, (bindings = new SyntheticMethodBinding[index + numberOfAccessors]), 0, index);
			if (methodAccessors[0] != null) 
				bindings[index++] = methodAccessors[0]; // super access 
			if (methodAccessors[1] != null) 
				bindings[index++] = methodAccessors[1]; // normal access or bridge

		} else {

			SyntheticMethodBinding[] fieldAccessors = (SyntheticMethodBinding[]) synthetics[METHOD_EMUL].get(fieldOrMethod);
			int numberOfAccessors = 0;
			if (fieldAccessors[0] != null) numberOfAccessors++;
			if (fieldAccessors[1] != null) numberOfAccessors++;
			if (index + numberOfAccessors > bindings.length)
				System.arraycopy(bindings, 0, (bindings = new SyntheticMethodBinding[index + numberOfAccessors]), 0, index);
			if (fieldAccessors[0] != null) 
				bindings[index++] = fieldAccessors[0]; // read access
			if (fieldAccessors[1] != null) 
				bindings[index++] = fieldAccessors[1]; // write access
		}
	}

	// sort them in according to their own indexes
	int length;
	SyntheticMethodBinding[] sortedBindings = new SyntheticMethodBinding[length = bindings.length];
	for (int i = 0; i < length; i++){
		SyntheticMethodBinding binding = bindings[i];
		sortedBindings[binding.index] = binding;
	}
	return sortedBindings;
}
/**
 * Answer the collection of synthetic fields to append into the classfile
 */
public FieldBinding[] syntheticFields() {
	
	if (synthetics == null) return null;

	int fieldSize = synthetics[FIELD_EMUL] == null ? 0 : synthetics[FIELD_EMUL].size();
	int literalSize = synthetics[CLASS_LITERAL_EMUL] == null ? 0 :synthetics[CLASS_LITERAL_EMUL].size();
	int totalSize = fieldSize + literalSize;
	if (totalSize == 0) return null;
	FieldBinding[] bindings = new FieldBinding[totalSize];

	// add innerclass synthetics
	if (synthetics[FIELD_EMUL] != null){
		Iterator elements = synthetics[FIELD_EMUL].values().iterator();
		for (int i = 0; i < fieldSize; i++) {
			SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.next();
			bindings[synthBinding.index] = synthBinding;
		}
	}
	// add class literal synthetics
	if (synthetics[CLASS_LITERAL_EMUL] != null){
		Iterator elements = synthetics[CLASS_LITERAL_EMUL].values().iterator();
		for (int i = 0; i < literalSize; i++) {
			SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.next();
			bindings[fieldSize+synthBinding.index] = synthBinding;
		}
	}
	return bindings;
}
public String toString() {
    StringBuffer buffer = new StringBuffer(30);
    buffer.append("(id="); //$NON-NLS-1$
    if (id == TypeIds.NoId) 
        buffer.append("NoId"); //$NON-NLS-1$
    else 
        buffer.append(id);
    buffer.append(")\n"); //$NON-NLS-1$
	if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
	if (isPublic()) buffer.append("public "); //$NON-NLS-1$
	if (isProtected()) buffer.append("protected "); //$NON-NLS-1$
	if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
	if (isAbstract() && isClass()) buffer.append("abstract "); //$NON-NLS-1$
	if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$
	if (isFinal()) buffer.append("final "); //$NON-NLS-1$

	if (isEnum()) buffer.append("enum "); //$NON-NLS-1$
	else if (isAnnotationType()) buffer.append("@interface "); //$NON-NLS-1$
	else if (isClass()) buffer.append("class "); //$NON-NLS-1$
	else buffer.append("interface "); //$NON-NLS-1$
	buffer.append((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED TYPE"); //$NON-NLS-1$

	if (this.typeVariables == null) {
		buffer.append("<NULL TYPE VARIABLES>"); //$NON-NLS-1$
	} else if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
		buffer.append("\n\t<"); //$NON-NLS-1$
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			if (i  > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append((this.typeVariables[i] != null) ? this.typeVariables[i].toString() : "NULL TYPE VARIABLE"); //$NON-NLS-1$
		}
		buffer.append(">"); //$NON-NLS-1$
	}
	buffer.append("\n\textends "); //$NON-NLS-1$
	buffer.append((superclass != null) ? superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$

	if (superInterfaces != null) {
		if (superInterfaces != Binding.NO_SUPERINTERFACES) {
			buffer.append("\n\timplements : "); //$NON-NLS-1$
			for (int i = 0, length = superInterfaces.length; i < length; i++) {
				if (i  > 0)
					buffer.append(", "); //$NON-NLS-1$
				buffer.append((superInterfaces[i] != null) ? superInterfaces[i].debugName() : "NULL TYPE"); //$NON-NLS-1$
			}
		}
	} else {
		buffer.append("NULL SUPERINTERFACES"); //$NON-NLS-1$
	}

	if (enclosingType() != null) {
		buffer.append("\n\tenclosing type : "); //$NON-NLS-1$
		buffer.append(enclosingType().debugName());
	}

	if (fields != null) {
		if (fields != Binding.NO_FIELDS) {
			buffer.append("\n/*   fields   */"); //$NON-NLS-1$
			for (int i = 0, length = fields.length; i < length; i++)
			    buffer.append('\n').append((fields[i] != null) ? fields[i].toString() : "NULL FIELD"); //$NON-NLS-1$ 
		}
	} else {
		buffer.append("NULL FIELDS"); //$NON-NLS-1$
	}

	if (methods != null) {
		if (methods != Binding.NO_METHODS) {
			buffer.append("\n/*   methods   */"); //$NON-NLS-1$
			for (int i = 0, length = methods.length; i < length; i++)
				buffer.append('\n').append((methods[i] != null) ? methods[i].toString() : "NULL METHOD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL METHODS"); //$NON-NLS-1$
	}

	if (memberTypes != null) {
		if (memberTypes != Binding.NO_MEMBER_TYPES) {
			buffer.append("\n/*   members   */"); //$NON-NLS-1$
			for (int i = 0, length = memberTypes.length; i < length; i++)
				buffer.append('\n').append((memberTypes[i] != null) ? memberTypes[i].toString() : "NULL TYPE"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL MEMBER TYPES"); //$NON-NLS-1$
	}

	buffer.append("\n\n"); //$NON-NLS-1$
	return buffer.toString();
}
public TypeVariableBinding[] typeVariables() {
	return this.typeVariables;
}
void verifyMethods(MethodVerifier verifier) {
	verifier.verify(this);

	for (int i = memberTypes.length; --i >= 0;)
		 ((SourceTypeBinding) memberTypes[i]).verifyMethods(verifier);
}
}
