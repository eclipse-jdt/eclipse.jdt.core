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
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;

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
	
	
public SourceTypeBinding(char[][] compoundName, PackageBinding fPackage, ClassScope scope) {
	this.compoundName = compoundName;
	this.fPackage = fPackage;
	this.fileName = scope.referenceCompilationUnit().getFileName();
	this.modifiers = scope.referenceContext.modifiers;
	this.sourceName = scope.referenceContext.name;
	this.scope = scope;

	// expect the fields & methods to be initialized correctly later
	this.fields = NoFields;
	this.methods = NoMethods;

	computeId();
}
private void addDefaultAbstractMethod(MethodBinding abstractMethod) {
	MethodBinding defaultAbstract = new MethodBinding(
		abstractMethod.modifiers | AccDefaultAbstract,
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
	if ((tagBits & KnowsDefaultAbstractMethods) != 0) return;

	tagBits |= KnowsDefaultAbstractMethods;

	if (isClass() && isAbstract()) {
		if (fPackage.environment.options.targetJDK >= ClassFileConstants.JDK1_2) return; // no longer added for post 1.2 targets

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
					if (itsInterfaces != NoSuperInterfaces) {
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

public FieldBinding addSyntheticField(LocalVariableBinding actualOuterLocalVariable) {
	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[FIELD_EMUL] == null) {
		synthetics[FIELD_EMUL] = new HashMap(5);
	}
	
	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get(actualOuterLocalVariable);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(SyntheticArgumentBinding.OuterLocalPrefix, actualOuterLocalVariable.name), 
			actualOuterLocalVariable.type, 
			AccPrivate | AccFinal | AccSynthetic, 
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
						SyntheticArgumentBinding.OuterLocalPrefix,
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

public FieldBinding addSyntheticField(ReferenceBinding enclosingType) {

	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[FIELD_EMUL] == null) {
		synthetics[FIELD_EMUL] = new HashMap(5);
	}

	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get(enclosingType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(
				SyntheticArgumentBinding.EnclosingInstancePrefix,
				String.valueOf(enclosingType.depth()).toCharArray()),
			enclosingType,
			AccDefault | AccFinal | AccSynthetic,
			this,
			Constant.NotAConstant,
			synthetics[FIELD_EMUL].size());
		synthetics[FIELD_EMUL].put(enclosingType, synthField);
	}
	// ensure there is not already such a field defined by the user
	FieldBinding existingField;
	if ((existingField = this.getField(synthField.name, true /*resolve*/)) != null) {
		TypeDeclaration typeDecl = scope.referenceContext;
		for (int i = 0, max = typeDecl.fields.length; i < max; i++) {
			FieldDeclaration fieldDecl = typeDecl.fields[i];
			if (fieldDecl.binding == existingField) {
				scope.problemReporter().duplicateFieldInType(this, fieldDecl);
				break;
			}
		}
	}		
	return synthField;
}
/* Add a new synthetic field for a class literal access.
*	Answer the new field or the existing field if one already existed.
*/

public FieldBinding addSyntheticField(TypeBinding targetType, BlockScope blockScope) {

	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[CLASS_LITERAL_EMUL] == null) {
		synthetics[CLASS_LITERAL_EMUL] = new HashMap(5);
	}

	// use a different table than FIELDS, given there might be a collision between emulation of X.this$0 and X.class.
	FieldBinding synthField = (FieldBinding) synthetics[CLASS_LITERAL_EMUL].get(targetType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			("class$" + synthetics[CLASS_LITERAL_EMUL].size()).toCharArray(), //$NON-NLS-1$
			blockScope.getJavaLangClass(),
			AccDefault | AccStatic | AccSynthetic,
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
public FieldBinding addSyntheticField(AssertStatement assertStatement, BlockScope blockScope) {

	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[FIELD_EMUL] == null) {
		synthetics[FIELD_EMUL] = new HashMap(5);
	}

	FieldBinding synthField = (FieldBinding) synthetics[FIELD_EMUL].get("assertionEmulation"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			"$assertionsDisabled".toCharArray(), //$NON-NLS-1$
			BooleanBinding,
			AccDefault | AccStatic | AccSynthetic | AccFinal,
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
						"$assertionsDisabled".toCharArray(), //$NON-NLS-1$
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

public SyntheticAccessMethodBinding addSyntheticMethod(FieldBinding targetField, boolean isReadAccess) {

	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[METHOD_EMUL] == null) {
		synthetics[METHOD_EMUL] = new HashMap(5);
	}

	SyntheticAccessMethodBinding accessMethod = null;
	SyntheticAccessMethodBinding[] accessors = (SyntheticAccessMethodBinding[]) synthetics[METHOD_EMUL].get(targetField);
	if (accessors == null) {
		accessMethod = new SyntheticAccessMethodBinding(targetField, isReadAccess, this);
		synthetics[METHOD_EMUL].put(targetField, accessors = new SyntheticAccessMethodBinding[2]);
		accessors[isReadAccess ? 0 : 1] = accessMethod;		
	} else {
		if ((accessMethod = accessors[isReadAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticAccessMethodBinding(targetField, isReadAccess, this);
			accessors[isReadAccess ? 0 : 1] = accessMethod;
		}
	}
	return accessMethod;
}
/* Add a new synthetic access method for access to <targetMethod>.
 * Must distinguish access method used for super access from others (need to use invokespecial bytecode)
	Answer the new method or the existing method if one already existed.
*/

public SyntheticAccessMethodBinding addSyntheticMethod(MethodBinding targetMethod, boolean isSuperAccess) {

	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[METHOD_EMUL] == null) {
		synthetics[METHOD_EMUL] = new HashMap(5);
	}

	SyntheticAccessMethodBinding accessMethod = null;
	SyntheticAccessMethodBinding[] accessors = (SyntheticAccessMethodBinding[]) synthetics[METHOD_EMUL].get(targetMethod);
	if (accessors == null) {
		accessMethod = new SyntheticAccessMethodBinding(targetMethod, isSuperAccess, this);
		synthetics[METHOD_EMUL].put(targetMethod, accessors = new SyntheticAccessMethodBinding[2]);
		accessors[isSuperAccess ? 0 : 1] = accessMethod;		
	} else {
		if ((accessMethod = accessors[isSuperAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticAccessMethodBinding(targetMethod, isSuperAccess, this);
			accessors[isSuperAccess ? 0 : 1] = accessMethod;
		}
	}
	return accessMethod;
}
/* 
 * Record the fact that bridge methods need to be generated to override certain inherited methods
 */
public SyntheticAccessMethodBinding addSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge, MethodBinding localTargetMethod) {
    
	if (synthetics == null) {
		synthetics = new HashMap[4];
	}
	if (synthetics[METHOD_EMUL] == null) {
		synthetics[METHOD_EMUL] = new HashMap(5);
	} else {
		// TODO (philippe) MethodBindings do not implement equals() so how do we prevent adding 2 'equal' inherited methods?
		// check to see if there is another equivalent inheritedMethod already added
		Iterator synthMethods = synthetics[METHOD_EMUL].keySet().iterator();
		while (synthMethods.hasNext()) {
			Object method = synthMethods.next();
			if (method instanceof MethodBinding)
				if (inheritedMethodToBridge.areParameterErasuresEqual((MethodBinding) method))
					if (inheritedMethodToBridge.returnType.erasure() == ((MethodBinding) method).returnType.erasure())
						return null;
		}
	}

	SyntheticAccessMethodBinding accessMethod = null;
	SyntheticAccessMethodBinding[] accessors = (SyntheticAccessMethodBinding[]) synthetics[METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) {
		accessMethod = new SyntheticAccessMethodBinding(inheritedMethodToBridge, localTargetMethod);
		synthetics[METHOD_EMUL].put(inheritedMethodToBridge, accessors = new SyntheticAccessMethodBinding[2]);
		accessors[1] = accessMethod;		
	} else {
		if ((accessMethod = accessors[1]) == null) {
			accessMethod = new SyntheticAccessMethodBinding(inheritedMethodToBridge, localTargetMethod);
			accessors[1] = accessMethod;
		}
	}
	return accessMethod;
}
void faultInTypesForFieldsAndMethods() {
	fields();
	methods();

	for (int i = 0, length = memberTypes.length; i < length; i++)
		((SourceTypeBinding) memberTypes[i]).faultInTypesForFieldsAndMethods();
}

// NOTE: the type of each field of a source type is resolved when needed
public FieldBinding[] fields() {
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
				return fields = NoFields;
	
			FieldBinding[] newFields = new FieldBinding[newSize];
			for (int i = 0, j = 0, length = fields.length; i < length; i++)
				if (fields[i] != null)
					newFields[j++] = fields[i];
			fields = newFields;
		}
	}
	return fields;
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#genericTypeSignature()
 */
public char[] genericTypeSignature() {
    if (this.genericReferenceTypeSignature == null) {
        if (this.typeVariables == NoTypeVariables) {
	        this.genericReferenceTypeSignature = this.signature();
        } else {
		    char[] typeSig = this.signature();
		    StringBuffer sig = new StringBuffer(10);
		    for (int i = 0; i < typeSig.length-1; i++) { // copy all but trailing semicolon
		    	sig.append(typeSig[i]);
		    }
		    sig.append('<');
		    for (int i = 0, length = this.typeVariables.length; i < length; i++) {
		        sig.append(this.typeVariables[i].genericTypeSignature());
		    }
		    sig.append(">;"); //$NON-NLS-1$
			int sigLength = sig.length();
			this.genericReferenceTypeSignature = new char[sigLength];
			sig.getChars(0, sigLength, this.genericReferenceTypeSignature, 0);		    
	    }
    }
    return this.genericReferenceTypeSignature;
}
/**
 * <param1 ... paramN>superclass superinterface1 ... superinterfaceN
 * <T:LY<TT;>;U:Ljava/lang/Object;V::Ljava/lang/Runnable;:Ljava/lang/Cloneable;:Ljava/util/Map;>Ljava/lang/Exception;Ljava/lang/Runnable;
 */
public char[] genericSignature() {
    StringBuffer sig = null;
	if (this.typeVariables != NoTypeVariables) {
	    sig = new StringBuffer(10);
	    sig.append('<');
	    for (int i = 0, length = this.typeVariables.length; i < length; i++) {
	        sig.append(this.typeVariables[i].genericSignature());
	    }
	    sig.append('>');
	} else {
	    // could still need a signature if any of supertypes is parameterized
	    noSignature: if (this.superclass == null || !this.superclass.isParameterizedType()) {
		    for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
		        if (this.superInterfaces[i].isParameterizedType()) break noSignature;
		    }        
	        return null;
	    }
	    sig = new StringBuffer(10);
	}
	if (this.superclass != null) {
		sig.append(this.superclass.genericTypeSignature());
	} else {
		// interface scenario only (as Object cannot be generic) - 65953
		sig.append(scope.getJavaLangObject().genericTypeSignature());
	}
    for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
        sig.append(this.superInterfaces[i].genericTypeSignature());
    }
	return sig.toString().toCharArray();
}
public MethodBinding[] getDefaultAbstractMethods() {
	int count = 0;
	for (int i = methods.length; --i >= 0;)
		if (methods[i].isDefaultAbstract())
			count++;
	if (count == 0) return NoMethods;

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

	if ((modifiers & AccUnresolved) == 0) { // have resolved all arg types & return type of the methods
		nextMethod : for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];
			if (method.selector == ConstructorDeclaration.ConstantPoolName && method.parameters.length == argCount) {
				TypeBinding[] toMatch = method.parameters;
				for (int p = 0; p < argCount; p++)
					if (toMatch[p] != argumentTypes[p])
						continue nextMethod;
				return method;
			}
		}
	} else {
		MethodBinding[] constructors = getMethods(ConstructorDeclaration.ConstantPoolName); // takes care of duplicates & default abstract methods
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
	if (refScope != null)
		refScope.recordTypeReference(this);

	int argCount = argumentTypes.length;
	int selectorLength = selector.length;
	boolean foundNothing = true;

	if ((modifiers & AccUnresolved) == 0) { // have resolved all arg types & return type of the methods
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
		foundNothing = matchingMethods == NoMethods;
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
			 if (superInterfaces.length == 1)
				return superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
		} else if (superclass != null) {
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
						fields = NoFields;
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
	boolean methodsAreResolved = (modifiers & AccUnresolved) == 0; // have resolved all arg types & return type of the methods
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
	if (matchingMethods == null) return NoMethods;

	MethodBinding[] result = new MethodBinding[matchingMethods.size()];
	matchingMethods.toArray(result);
	if (!methodsAreResolved) {
		for (int i = 0, length = result.length - 1; i < length; i++) {
			MethodBinding method = result[i];
			for (int j = length; j > i; j--) {
				if (method.areParameterErasuresEqual(result[j])) {
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
/* 
 * Answer the bridge method associated for an  inherited methods or null if one does not exist
 */
public SyntheticAccessMethodBinding getSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge) {
    
	if (synthetics == null) return null;
	if (synthetics[METHOD_EMUL] == null) return null;
	SyntheticAccessMethodBinding[] accessors = (SyntheticAccessMethodBinding[]) synthetics[METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) return null;
	return accessors[1];
}
/**
 * Returns true if a type is identical to another one,
 * or for generic types, true if compared to its raw type.
 */
public boolean isEquivalentTo(TypeBinding otherType) {
    if (this == otherType) return true;
    if (otherType == null) return false;
    if (otherType.isWildcard()) // wildcard
		return ((WildcardBinding) otherType).boundCheck(this);
    if (this.typeVariables == NoTypeVariables) return false;
    if (otherType.isParameterizedType()) {
        if ((otherType.tagBits & HasWildcard) == 0 && (!this.isMemberType() || !otherType.isMemberType())) 
        	return false; // should have been identical
        ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
        if (this != otherParamType.type) 
            return false;
        ReferenceBinding enclosing = enclosingType();
        if (enclosing != null && !enclosing.isEquivalentTo(otherParamType.enclosingType()))
            return false;
        int length = this.typeVariables == null ? 0 : this.typeVariables.length;
        TypeBinding[] otherArguments = otherParamType.arguments;
        int otherLength = otherArguments == null ? 0 : otherArguments.length;
        if (otherLength != length) 
            return false;
        // argument must be identical, only equivalence is allowed if wildcard other type
        for (int i = 0; i < length; i++) {
        	TypeBinding argument = this.typeVariables[i];
        	TypeBinding otherArgument = otherArguments[i];
			if (!(argument == otherArgument
					|| (otherArgument.isWildcard()) && argument.isEquivalentTo(otherArgument))) {
				return false;
			}
        }
        return true;
    } else if (otherType.isRawType())
        return otherType.erasure() == this;
	return false;
}

public boolean isGenericType() {
    return this.typeVariables != NoTypeVariables;
}

public ReferenceBinding[] memberTypes() {
	return this.memberTypes;
}
public FieldBinding getUpdatedFieldBinding(FieldBinding targetField, ReferenceBinding newDeclaringClass) {

	if (this.synthetics == null) {
		this.synthetics = new HashMap[4];
	}
	if (this.synthetics[RECEIVER_TYPE_EMUL] == null) {
		this.synthetics[RECEIVER_TYPE_EMUL] = new HashMap(5);
	}

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

	if (this.synthetics == null) {
		this.synthetics = new HashMap[4];
	}
	if (this.synthetics[RECEIVER_TYPE_EMUL] == null) {
		this.synthetics[RECEIVER_TYPE_EMUL] = new HashMap(5);
	}


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
	if ((modifiers & AccUnresolved) == 0)
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
		for (int i = 0, length = methods.length; i < length; i++) {
			MethodBinding method = methods[i];
			if (method != null) {
				AbstractMethodDeclaration methodDecl = null;
				for (int j = length - 1; j > i; j--) {
					MethodBinding method2 = methods[j];
					if (method2 != null && CharOperation.equals(method.selector, method2.selector)) {
						if (method.areParameterErasuresEqual(method2)) {
							if (methodDecl == null) {
								methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost
								scope.problemReporter().duplicateMethodInType(this, methodDecl);
								methodDecl.binding = null;
								methods[i] = null;
								failed++;
							}
							AbstractMethodDeclaration method2Decl = method2.sourceMethod();
							scope.problemReporter().duplicateMethodInType(this, method2Decl);
							method2Decl.binding = null;
							methods[j] = null;
							failed++;
						}
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
				methods = NoMethods;
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

		modifiers ^= AccUnresolved;
	}		
	return methods;
}
private FieldBinding resolveTypeFor(FieldBinding field) {
	if ((field.modifiers & AccUnresolved) == 0)
		return field;

	FieldDeclaration[] fieldDecls = scope.referenceContext.fields;
	for (int f = 0, length = fieldDecls.length; f < length; f++) {
		if (fieldDecls[f].binding != field)
			continue;

			MethodScope initializationScope = field.isStatic() 
				? scope.referenceContext.staticInitializerScope 
				: scope.referenceContext.initializerScope;
			FieldBinding previousField = initializationScope.initializedField;
			try {
				initializationScope.initializedField = field;
				TypeBinding fieldType = fieldDecls[f].type.resolveType(initializationScope);
				field.type = fieldType;
				field.modifiers ^= AccUnresolved;
				if (fieldType == null) {
					fieldDecls[f].binding = null;
					return null;
				}
				if (fieldType == VoidBinding) {
					scope.problemReporter().variableTypeCannotBeVoid(fieldDecls[f]);
					fieldDecls[f].binding = null;
					return null;
				}
				if (fieldType.isArrayType() && ((ArrayBinding) fieldType).leafComponentType == VoidBinding) {
					scope.problemReporter().variableTypeCannotBeVoidArray(fieldDecls[f]);
					fieldDecls[f].binding = null;
					return null;
				}
				if (fieldType instanceof ReferenceBinding && (((ReferenceBinding)fieldType).modifiers & AccGenericSignature) != 0) {
					field.modifiers |= AccGenericSignature;
				}				
			} finally {
			    initializationScope.initializedField = previousField;
			}
		return field;
	}
	return null; // should never reach this point
}
private MethodBinding resolveTypesFor(MethodBinding method) {
    
	if ((method.modifiers & AccUnresolved) == 0)
		return method;

	AbstractMethodDeclaration methodDecl = method.sourceMethod();
	if (methodDecl == null) return null; // method could not be resolved in previous iteration
	
	TypeParameter[] typeParameters = methodDecl.typeParameters();
	if (typeParameters != null) methodDecl.scope.connectTypeVariables(typeParameters);
	TypeReference[] exceptionTypes = methodDecl.thrownExceptions;
	if (exceptionTypes != null) {
		int size = exceptionTypes.length;
		method.thrownExceptions = new ReferenceBinding[size];
		ReferenceBinding throwable = scope.getJavaLangThrowable();
		int count = 0;
		ReferenceBinding resolvedExceptionType;
		for (int i = 0; i < size; i++) {
			resolvedExceptionType = (ReferenceBinding) exceptionTypes[i].resolveType(methodDecl.scope);
			if (resolvedExceptionType == null) {
				continue;
			}
			if (resolvedExceptionType.isGenericType() || resolvedExceptionType.isParameterizedType()) {
				methodDecl.scope.problemReporter().invalidParameterizedExceptionType(resolvedExceptionType, exceptionTypes[i]);
				continue;
			}
			if (throwable != resolvedExceptionType && !throwable.isSuperclassOf(resolvedExceptionType)) {
				methodDecl.scope.problemReporter().cannotThrowType(this, methodDecl, exceptionTypes[i], resolvedExceptionType);
				continue;
			}
		    if ((resolvedExceptionType.modifiers & AccGenericSignature) != 0) {
				method.modifiers |= AccGenericSignature;
			}
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
			TypeBinding parameterType = arg.type.resolveType(methodDecl.scope);
			if (parameterType == null) {
				foundArgProblem = true;
			} else if (parameterType == VoidBinding) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoid(this, methodDecl, arg);
				foundArgProblem = true;
			} else if (parameterType.isArrayType() && ((ArrayBinding) parameterType).leafComponentType == VoidBinding) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoidArray(this, methodDecl, arg);
				foundArgProblem = true;
			} else {
			    if (parameterType instanceof ReferenceBinding && (((ReferenceBinding)parameterType).modifiers & AccGenericSignature) != 0) {
					method.modifiers |= AccGenericSignature;
				}
				method.parameters[i] = parameterType;
			}
		}
	}

	boolean foundReturnTypeProblem = false;
	if (!method.isConstructor()) {
		TypeReference returnType = methodDecl instanceof MethodDeclaration
			? ((MethodDeclaration) methodDecl).returnType
			: ((AnnotationTypeMemberDeclaration) methodDecl).returnType;
		if (returnType == null) {
			methodDecl.scope.problemReporter().missingReturnType(methodDecl);
			method.returnType = null;
			foundReturnTypeProblem = true;
		} else {
		    TypeBinding methodType = returnType.resolveType(methodDecl.scope);
			if (methodType == null) {
				foundReturnTypeProblem = true;
			} else if (methodType.isArrayType() && ((ArrayBinding) methodType).leafComponentType == VoidBinding) {
				methodDecl.scope.problemReporter().returnTypeCannotBeVoidArray(this, (MethodDeclaration) methodDecl);
				foundReturnTypeProblem = true;
			} else {
				method.returnType = methodType;
				if (methodType instanceof ReferenceBinding && (((ReferenceBinding)methodType).modifiers & AccGenericSignature) != 0) {
					method.modifiers |= AccGenericSignature;
				}
			}
		}
	}
	if (foundArgProblem) {
		methodDecl.binding = null;
		return null;
	}
	if (foundReturnTypeProblem)
		return method; // but its still unresolved with a null return type & is still connected to its method declaration

	method.modifiers ^= AccUnresolved;
	return method;
}
public final int sourceEnd() {
	return scope.referenceContext.sourceEnd;
}
public final int sourceStart() {
	return scope.referenceContext.sourceStart;
}
public ReferenceBinding superclass() {
	return superclass;
}
public ReferenceBinding[] superInterfaces() {
	return superInterfaces;
}
// TODO (philippe) could be a performance issue since some senders are building the list just to count them
public SyntheticAccessMethodBinding[] syntheticAccessMethods() {
	
	if (synthetics == null || synthetics[METHOD_EMUL] == null || synthetics[METHOD_EMUL].size() == 0) return null;

	// difficult to compute size up front because of the embedded arrays so assume there is only 1
	int index = 0;
	SyntheticAccessMethodBinding[] bindings = new SyntheticAccessMethodBinding[1];
	Iterator fieldsOrMethods = synthetics[METHOD_EMUL].keySet().iterator();
	while (fieldsOrMethods.hasNext()) {

		Object fieldOrMethod = fieldsOrMethods.next();

		if (fieldOrMethod instanceof MethodBinding) {

			SyntheticAccessMethodBinding[] methodAccessors = (SyntheticAccessMethodBinding[]) synthetics[METHOD_EMUL].get(fieldOrMethod);
			int numberOfAccessors = 0;
			if (methodAccessors[0] != null) numberOfAccessors++;
			if (methodAccessors[1] != null) numberOfAccessors++;
			if (index + numberOfAccessors > bindings.length)
				System.arraycopy(bindings, 0, (bindings = new SyntheticAccessMethodBinding[index + numberOfAccessors]), 0, index);
			if (methodAccessors[0] != null) 
				bindings[index++] = methodAccessors[0]; // super access 
			if (methodAccessors[1] != null) 
				bindings[index++] = methodAccessors[1]; // normal access or bridge

		} else {

			SyntheticAccessMethodBinding[] fieldAccessors = (SyntheticAccessMethodBinding[]) synthetics[METHOD_EMUL].get(fieldOrMethod);
			int numberOfAccessors = 0;
			if (fieldAccessors[0] != null) numberOfAccessors++;
			if (fieldAccessors[1] != null) numberOfAccessors++;
			if (index + numberOfAccessors > bindings.length)
				System.arraycopy(bindings, 0, (bindings = new SyntheticAccessMethodBinding[index + numberOfAccessors]), 0, index);
			if (fieldAccessors[0] != null) 
				bindings[index++] = fieldAccessors[0]; // read access
			if (fieldAccessors[1] != null) 
				bindings[index++] = fieldAccessors[1]; // write access
		}
	}

	// sort them in according to their own indexes
	int length;
	SyntheticAccessMethodBinding[] sortedBindings = new SyntheticAccessMethodBinding[length = bindings.length];
	for (int i = 0; i < length; i++){
		SyntheticAccessMethodBinding binding = bindings[i];
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
    if (id == NoId) 
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

	buffer.append(isInterface() ? "interface " : "class "); //$NON-NLS-1$ //$NON-NLS-2$
	buffer.append((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED TYPE"); //$NON-NLS-1$

	if (this.typeVariables != null && this.typeVariables != NoTypeVariables) {
		buffer.append("\n\t<"); //$NON-NLS-1$
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			if (i  > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append((this.typeVariables[i] != null) ? this.typeVariables[i].toString() : "NULL TYPE VARIABLE"); //$NON-NLS-1$
		}
		buffer.append(">"); //$NON-NLS-1$
	} else {
		buffer.append("<NULL TYPE VARIABLES>"); //$NON-NLS-1$
	}
	buffer.append("\n\textends "); //$NON-NLS-1$
	buffer.append((superclass != null) ? superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$

	if (superInterfaces != null) {
		if (superInterfaces != NoSuperInterfaces) {
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
		if (fields != NoFields) {
			buffer.append("\n/*   fields   */"); //$NON-NLS-1$
			for (int i = 0, length = fields.length; i < length; i++)
			    buffer.append('\n').append((fields[i] != null) ? fields[i].toString() : "NULL FIELD"); //$NON-NLS-1$ 
		}
	} else {
		buffer.append("NULL FIELDS"); //$NON-NLS-1$
	}

	if (methods != null) {
		if (methods != NoMethods) {
			buffer.append("\n/*   methods   */"); //$NON-NLS-1$
			for (int i = 0, length = methods.length; i < length; i++)
				buffer.append('\n').append((methods[i] != null) ? methods[i].toString() : "NULL METHOD"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		buffer.append("NULL METHODS"); //$NON-NLS-1$
	}

	if (memberTypes != null) {
		if (memberTypes != NoMemberTypes) {
			buffer.append("\n/*   members   */"); //$NON-NLS-1$
			for (int i = 0, length = memberTypes.length; i < length; i++)
				buffer.append('\n').append((memberTypes[i] != null) ? memberTypes[i].toString() : "NULL TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
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
			if (CharOperation.prefixEquals(SyntheticArgumentBinding.EnclosingInstancePrefix, field.name)
				&& ((ReferenceBinding) field.type).findSuperTypeErasingTo(targetEnclosingType) != null)
					return field;
		}
	}
	return null;
}
}
