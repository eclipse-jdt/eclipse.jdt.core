/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

public class SourceTypeBinding extends ReferenceBinding {
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces;
	public FieldBinding[] fields;
	public MethodBinding[] methods;
	public ReferenceBinding[] memberTypes;

	public ClassScope scope;

	// Synthetics are separated into 4 categories: methods, super methods, fields, class literals and changed declaring type bindings
	public final static int METHOD_EMUL = 0;
	public final static int FIELD_EMUL = 1;
	public final static int CLASS_LITERAL_EMUL = 2;
	public final static int RECEIVER_TYPE_EMUL = 3;
	
	Hashtable[] synthetics;
	
protected SourceTypeBinding() {
}
public SourceTypeBinding(char[][] compoundName, PackageBinding fPackage, ClassScope scope) {
	this.compoundName = compoundName;
	this.fPackage = fPackage;
	this.fileName = scope.referenceCompilationUnit().getFileName();
	this.modifiers = scope.referenceContext.modifiers;
	this.sourceName = scope.referenceContext.name;
	this.scope = scope;

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
		if (fPackage.environment.options.targetJDK >= CompilerOptions.JDK1_2) return; // no longer added for post 1.2 targets

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
		synthetics = new Hashtable[4];
	}
	if (synthetics[FIELD_EMUL] == null) {
		synthetics[FIELD_EMUL] = new Hashtable(5);
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
		if ((existingField = this.getField(synthField.name)) != null) {
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
		synthetics = new Hashtable[4];
	}
	if (synthetics[FIELD_EMUL] == null) {
		synthetics[FIELD_EMUL] = new Hashtable(5);
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
	if ((existingField = this.getField(synthField.name)) != null) {
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
		synthetics = new Hashtable[4];
	}
	if (synthetics[CLASS_LITERAL_EMUL] == null) {
		synthetics[CLASS_LITERAL_EMUL] = new Hashtable(5);
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
	if ((existingField = this.getField(synthField.name)) != null) {
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
		synthetics = new Hashtable[4];
	}
	if (synthetics[FIELD_EMUL] == null) {
		synthetics[FIELD_EMUL] = new Hashtable(5);
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
		if ((existingField = this.getField(synthField.name)) != null) {
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
		synthetics = new Hashtable[4];
	}
	if (synthetics[METHOD_EMUL] == null) {
		synthetics[METHOD_EMUL] = new Hashtable(5);
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
		synthetics = new Hashtable[4];
	}
	if (synthetics[METHOD_EMUL] == null) {
		synthetics[METHOD_EMUL] = new Hashtable(5);
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

public FieldBinding[] availableFields() {
	return fields();
}
public MethodBinding[] availableMethods() {
	return methods();
}
void faultInTypesForFieldsAndMethods() {
	fields();
	methods();

	for (int i = 0, length = memberTypes.length; i < length; i++)
		((SourceTypeBinding) memberTypes[i]).faultInTypesForFieldsAndMethods();
}
// NOTE: the type of each field of a source type is resolved when needed

public FieldBinding[] fields() {
	
	try {
		int failed = 0;
		for (int f = 0, max = fields.length; f < max; f++) {
			if (resolveTypeFor(fields[f]) == null) {
				fields[f] = null;
				failed++;
			}
		}
		if (failed > 0) {
			int newSize = fields.length - failed;
			if (newSize == 0)
				return fields = NoFields;
	
			FieldBinding[] newFields = new FieldBinding[newSize];
			for (int i = 0, n = 0, max = fields.length; i < max; i++)
				if (fields[i] != null)
					newFields[n++] = fields[i];
			fields = newFields;
		}
	} catch(AbortCompilation e){
		// ensure null fields are removed
		FieldBinding[] newFields = null;
		int count = 0;
		for (int i = 0, max = fields.length; i < max; i++){
			FieldBinding field = fields[i];
			if (field == null && newFields == null){
				System.arraycopy(fields, 0, newFields = new FieldBinding[max], 0, i);
			} else if (newFields != null && field != null) {
				newFields[count++] = field;
			}
		}
		if (newFields != null){
			System.arraycopy(newFields, 0, fields = new FieldBinding[count], 0, count);
		}			
		throw e;
	}
	return fields;
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

public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	int selectorLength = selector.length;
	boolean foundNothing = true;

	if ((modifiers & AccUnresolved) == 0) { // have resolved all arg types & return type of the methods
		nextMethod : for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];
			if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector)) {
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
				return superInterfaces[0].getExactMethod(selector, argumentTypes);
		} else if (superclass != null) {
			return superclass.getExactMethod(selector, argumentTypes);
		}
	}
	return null;
}
// NOTE: the type of a field of a source type is resolved when needed

public FieldBinding getField(char[] fieldName) {
	int fieldLength = fieldName.length;
	for (int f = fields.length; --f >= 0;) {
		FieldBinding field = fields[f];
		if (field.name.length == fieldLength && CharOperation.prefixEquals(field.name, fieldName)) {
			if (resolveTypeFor(field) != null)
				return field;

			int newSize = fields.length - 1;
			if (newSize == 0) {
				fields = NoFields;
			} else {
				FieldBinding[] newFields = new FieldBinding[newSize];
				System.arraycopy(fields, 0, newFields, 0, f);
				System.arraycopy(fields, f + 1, newFields, f, newSize - f);
				fields = newFields;
			}
			return null;
		}
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed

public MethodBinding[] getMethods(char[] selector) {
	// handle forward references to potential default abstract methods
	addDefaultAbstractMethods();

	try{
		int count = 0;
		int lastIndex = -1;
		int selectorLength = selector.length;
		if ((modifiers & AccUnresolved) == 0) { // have resolved all arg types & return type of the methods
			for (int m = 0, length = methods.length; m < length; m++) {
				MethodBinding method = methods[m];
				if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector)) {
					count++;
					lastIndex = m;
				}
			}
		} else {
			boolean foundProblem = false;
			int failed = 0;
			for (int m = 0, length = methods.length; m < length; m++) {
				MethodBinding method = methods[m];
				if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector)) {
					if (resolveTypesFor(method) == null) {
						foundProblem = true;
						methods[m] = null; // unable to resolve parameters
						failed++;
					} else if (method.returnType == null) {
						foundProblem = true;
					} else {
						count++;
						lastIndex = m;
					}
				}
			}
	
			if (foundProblem || count > 1) {
				for (int m = methods.length; --m >= 0;) {
					MethodBinding method = methods[m];
					if (method != null && method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector)) {
						AbstractMethodDeclaration methodDecl = null;
						for (int i = 0; i < m; i++) {
							MethodBinding method2 = methods[i];
							if (method2 != null && CharOperation.equals(method.selector, method2.selector)) {
								if (method.areParametersEqual(method2)) {
									if (methodDecl == null) {
										methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost
										scope.problemReporter().duplicateMethodInType(this, methodDecl);
										methodDecl.binding = null;
										methods[m] = null;
										failed++;
									}
									scope.problemReporter().duplicateMethodInType(this, method2.sourceMethod());
									method2.sourceMethod().binding = null;
									methods[i] = null;
									failed++;
								}
							}
						}
						if (method.returnType == null && methodDecl == null) { // forget method with invalid return type... was kept to detect possible collisions
							method.sourceMethod().binding = null;
							methods[m] = null;
							failed++;
						}
					}
				}
	
				if (failed > 0) {
					int newSize = methods.length - failed;
					if (newSize == 0)
						return methods = NoMethods;
	
					MethodBinding[] newMethods = new MethodBinding[newSize];
					for (int i = 0, n = 0, max = methods.length; i < max; i++)
						if (methods[i] != null)
							newMethods[n++] = methods[i];
					methods = newMethods;
					return getMethods(selector); // try again now that the problem methods have been removed
				}
			}
		}
		if (count == 1)
			return new MethodBinding[] {methods[lastIndex]};
		if (count > 1) {
			MethodBinding[] result = new MethodBinding[count];
			count = 0;
			for (int m = 0; m <= lastIndex; m++) {
				MethodBinding method = methods[m];
				if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector))
					result[count++] = method;
			}
			return result;
		}
	} catch(AbortCompilation e){
		// ensure null methods are removed
		MethodBinding[] newMethods = null;
		int count = 0;
		for (int i = 0, max = methods.length; i < max; i++){
			MethodBinding method = methods[i];
			if (method == null && newMethods == null){
				System.arraycopy(methods, 0, newMethods = new MethodBinding[max], 0, i);
			} else if (newMethods != null && method != null) {
				newMethods[count++] = method;
			}
		}
		if (newMethods != null){
			System.arraycopy(newMethods, 0, methods = new MethodBinding[count], 0, count);
		}			
		modifiers ^= AccUnresolved;
		throw e;
	}		
	return NoMethods;
}
/* Answer the synthetic field for <actualOuterLocalVariable>
*	or null if one does not exist.
*/

public FieldBinding getSyntheticField(LocalVariableBinding actualOuterLocalVariable) {
	
	if (synthetics == null || synthetics[FIELD_EMUL] == null) return null;
	return (FieldBinding) synthetics[FIELD_EMUL].get(actualOuterLocalVariable);
}
public ReferenceBinding[] memberTypes() {
	return memberTypes;
}
public FieldBinding getUpdatedFieldBinding(FieldBinding targetField, ReferenceBinding newDeclaringClass) {

	if (synthetics == null) {
		synthetics = new Hashtable[4];
	}
	if (synthetics[RECEIVER_TYPE_EMUL] == null) {
		synthetics[RECEIVER_TYPE_EMUL] = new Hashtable(5);
	}

	Hashtable fieldMap = (Hashtable) synthetics[RECEIVER_TYPE_EMUL].get(targetField);
	if (fieldMap == null) {
		fieldMap = new Hashtable(5);
		synthetics[RECEIVER_TYPE_EMUL].put(targetField, fieldMap);
	}
	FieldBinding updatedField = (FieldBinding) fieldMap.get(newDeclaringClass);
	if (updatedField == null){
		updatedField = new FieldBinding(targetField, newDeclaringClass);
		fieldMap.put(newDeclaringClass, updatedField);
	}
	return updatedField;
}

public MethodBinding getUpdatedMethodBinding(MethodBinding targetMethod, ReferenceBinding newDeclaringClass) {

	if (synthetics == null) {
		synthetics = new Hashtable[4];
	}
	if (synthetics[RECEIVER_TYPE_EMUL] == null) {
		synthetics[RECEIVER_TYPE_EMUL] = new Hashtable(5);
	}


	Hashtable methodMap = (Hashtable) synthetics[RECEIVER_TYPE_EMUL].get(targetMethod);
	if (methodMap == null) {
		methodMap = new Hashtable(5);
		synthetics[RECEIVER_TYPE_EMUL].put(targetMethod, methodMap);
	}
	MethodBinding updatedMethod = (MethodBinding) methodMap.get(newDeclaringClass);
	if (updatedMethod == null){
		updatedMethod = new MethodBinding(targetMethod, newDeclaringClass);
		methodMap.put(newDeclaringClass, updatedMethod);
	}
	return updatedMethod;
}

// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding[] methods() {
	try {
		if ((modifiers & AccUnresolved) == 0)
			return methods;
	
		int failed = 0;
		for (int m = 0, max = methods.length; m < max; m++) {
			if (resolveTypesFor(methods[m]) == null) {
				methods[m] = null; // unable to resolve parameters
				failed++;
			}
		}
	
		for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];
			if (method != null) {
				AbstractMethodDeclaration methodDecl = null;
				for (int i = 0; i < m; i++) {
					MethodBinding method2 = methods[i];
					if (method2 != null && CharOperation.equals(method.selector, method2.selector)) {
						if (method.areParametersEqual(method2)) {
							if (methodDecl == null) {
								methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost
								scope.problemReporter().duplicateMethodInType(this, methodDecl);
								methodDecl.binding = null;
								methods[m] = null;
								failed++;
							}
							scope.problemReporter().duplicateMethodInType(this, method2.sourceMethod());
							method2.sourceMethod().binding = null;
							methods[i] = null;
							failed++;
						}
					}
				}
				if (method.returnType == null && methodDecl == null) { // forget method with invalid return type... was kept to detect possible collisions
					method.sourceMethod().binding = null;
					methods[m] = null;
					failed++;
				}
			}
		}
	
		if (failed > 0) {
			int newSize = methods.length - failed;
			if (newSize == 0) {
				methods = NoMethods;
			} else {
				MethodBinding[] newMethods = new MethodBinding[newSize];
				for (int m = 0, n = 0, max = methods.length; m < max; m++)
					if (methods[m] != null)
						newMethods[n++] = methods[m];
				methods = newMethods;
			}
		}
	
		// handle forward references to potential default abstract methods
		addDefaultAbstractMethods();
	} catch(AbortCompilation e){
		// ensure null methods are removed
		MethodBinding[] newMethods = null;
		int count = 0;
		for (int i = 0, max = methods.length; i < max; i++){
			MethodBinding method = methods[i];
			if (method == null && newMethods == null){
				System.arraycopy(methods, 0, newMethods = new MethodBinding[max], 0, i);
			} else if (newMethods != null && method != null) {
				newMethods[count++] = method;
			}
		}
		if (newMethods != null){
			System.arraycopy(newMethods, 0, methods = new MethodBinding[count], 0, count);
		}			
		modifiers ^= AccUnresolved;
		throw e;
	}		
	modifiers ^= AccUnresolved;
	return methods;
}
private FieldBinding resolveTypeFor(FieldBinding field) {
	if (field.type != null)
		return field;

	FieldDeclaration[] fieldDecls = scope.referenceContext.fields;
	for (int f = 0, length = fieldDecls.length; f < length; f++) {
		if (fieldDecls[f].binding != field)
			continue;

		field.type = fieldDecls[f].getTypeBinding(scope);
		if (!field.type.isValidBinding()) {
			scope.problemReporter().fieldTypeProblem(this, fieldDecls[f], field.type);
			//scope.problemReporter().invalidType(fieldDecls[f].type, field.type);
			fieldDecls[f].binding = null;
			return null;
		}
		if (field.type == VoidBinding) {
			scope.problemReporter().variableTypeCannotBeVoid(fieldDecls[f]);
			fieldDecls[f].binding = null;
			return null;
		}
		if (field.type.isArrayType() && ((ArrayBinding) field.type).leafComponentType == VoidBinding) {
			scope.problemReporter().variableTypeCannotBeVoidArray(fieldDecls[f]);
			fieldDecls[f].binding = null;
			return null;
		}
		return field;
	}
	return null; // should never reach this point
}
private MethodBinding resolveTypesFor(MethodBinding method) {
	if ((method.modifiers & AccUnresolved) == 0)
		return method;

	AbstractMethodDeclaration methodDecl = method.sourceMethod();
	TypeReference[] exceptionTypes = methodDecl.thrownExceptions;
	if (exceptionTypes != null) {
		int size = exceptionTypes.length;
		method.thrownExceptions = new ReferenceBinding[size];
		ReferenceBinding throwable = scope.getJavaLangThrowable();
		int count = 0;
		ReferenceBinding resolvedExceptionType;
		for (int i = 0; i < size; i++) {
			resolvedExceptionType = (ReferenceBinding) exceptionTypes[i].getTypeBinding(scope);
			if (!resolvedExceptionType.isValidBinding()) {
				methodDecl.scope.problemReporter().exceptionTypeProblem(this, methodDecl, exceptionTypes[i], resolvedExceptionType);
				//methodDecl.scope.problemReporter().invalidType(exceptionTypes[i], resolvedExceptionType);
				continue;
			}
			if (throwable != resolvedExceptionType && !throwable.isSuperclassOf(resolvedExceptionType)) {
				methodDecl.scope.problemReporter().cannotThrowType(this, methodDecl, exceptionTypes[i], resolvedExceptionType);
				continue;
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
			method.parameters[i] = arg.type.getTypeBinding(scope);
			if (!method.parameters[i].isValidBinding()) {
				methodDecl.scope.problemReporter().argumentTypeProblem(this, methodDecl, arg, method.parameters[i]);
				//methodDecl.scope.problemReporter().invalidType(arg, method.parameters[i]);
				foundArgProblem = true;
			} else if (method.parameters[i] == VoidBinding) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoid(this, methodDecl, arg);
				foundArgProblem = true;
			} else if (method.parameters[i].isArrayType() && ((ArrayBinding) method.parameters[i]).leafComponentType == VoidBinding) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoidArray(this, methodDecl, arg);
				foundArgProblem = true;
			}
		}
	}

	boolean foundReturnTypeProblem = false;
	if (!method.isConstructor()) {
		TypeReference returnType = ((MethodDeclaration) methodDecl).returnType;
		if (returnType == null) {
			methodDecl.scope.problemReporter().missingReturnType(methodDecl);
			method.returnType = null;
			foundReturnTypeProblem = true;
		} else {
			method.returnType = returnType.getTypeBinding(scope);
			if (!method.returnType.isValidBinding()) {
				methodDecl.scope.problemReporter().returnTypeProblem(this, (MethodDeclaration) methodDecl, method.returnType);
				//methodDecl.scope.problemReporter().invalidType(returnType, method.returnType);
				method.returnType = null;
				foundReturnTypeProblem = true;
			} else if (method.returnType.isArrayType() && ((ArrayBinding) method.returnType).leafComponentType == VoidBinding) {
				methodDecl.scope.problemReporter().returnTypeCannotBeVoidArray(this, (MethodDeclaration) methodDecl);
				method.returnType = null;
				foundReturnTypeProblem = true;
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
public SyntheticAccessMethodBinding[] syntheticAccessMethods() {
	
	if (synthetics == null || synthetics[METHOD_EMUL] == null || synthetics[METHOD_EMUL].size() == 0) return null;

	// difficult to compute size up front because of the embedded arrays so assume there is only 1
	int index = 0;
	SyntheticAccessMethodBinding[] bindings = new SyntheticAccessMethodBinding[1];
	Enumeration fieldsOrMethods = synthetics[METHOD_EMUL].keys();
	while (fieldsOrMethods.hasMoreElements()) {

		Object fieldOrMethod = fieldsOrMethods.nextElement();

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
				bindings[index++] = methodAccessors[1]; // normal access

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
		Enumeration elements = synthetics[FIELD_EMUL].elements();
		for (int i = 0; i < fieldSize; i++) {
			SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.nextElement();
			bindings[synthBinding.index] = synthBinding;
		}
	}
	// add class literal synthetics
	if (synthetics[CLASS_LITERAL_EMUL] != null){
		Enumeration elements = synthetics[CLASS_LITERAL_EMUL].elements();
		for (int i = 0; i < literalSize; i++) {
			SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.nextElement();
			bindings[fieldSize+synthBinding.index] = synthBinding;
		}
	}
	return bindings;
}
public String toString() {
	String s = "(id="+(id == NoId ? "NoId" : (""+id) ) +")\n"; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-4$ //$NON-NLS-1$

	if (isDeprecated()) s += "deprecated "; //$NON-NLS-1$
	if (isPublic()) s += "public "; //$NON-NLS-1$
	if (isProtected()) s += "protected "; //$NON-NLS-1$
	if (isPrivate()) s += "private "; //$NON-NLS-1$
	if (isAbstract() && isClass()) s += "abstract "; //$NON-NLS-1$
	if (isStatic() && isNestedType()) s += "static "; //$NON-NLS-1$
	if (isFinal()) s += "final "; //$NON-NLS-1$

	s += isInterface() ? "interface " : "class "; //$NON-NLS-1$ //$NON-NLS-2$
	s += (compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED TYPE"; //$NON-NLS-1$

	s += "\n\textends "; //$NON-NLS-1$
	s += (superclass != null) ? superclass.debugName() : "NULL TYPE"; //$NON-NLS-1$

	if (superInterfaces != null) {
		if (superInterfaces != NoSuperInterfaces) {
			s += "\n\timplements : "; //$NON-NLS-1$
			for (int i = 0, length = superInterfaces.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (superInterfaces[i] != null) ? superInterfaces[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL SUPERINTERFACES"; //$NON-NLS-1$
	}

	if (enclosingType() != null) {
		s += "\n\tenclosing type : "; //$NON-NLS-1$
		s += enclosingType().debugName();
	}

	if (fields != null) {
		if (fields != NoFields) {
			s += "\n/*   fields   */"; //$NON-NLS-1$
			for (int i = 0, length = fields.length; i < length; i++)
				s += (fields[i] != null) ? "\n" + fields[i].toString() : "\nNULL FIELD"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL FIELDS"; //$NON-NLS-1$
	}

	if (methods != null) {
		if (methods != NoMethods) {
			s += "\n/*   methods   */"; //$NON-NLS-1$
			for (int i = 0, length = methods.length; i < length; i++)
				s += (methods[i] != null) ? "\n" + methods[i].toString() : "\nNULL METHOD"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL METHODS"; //$NON-NLS-1$
	}

	if (memberTypes != null) {
		if (memberTypes != NoMemberTypes) {
			s += "\n/*   members   */"; //$NON-NLS-1$
			for (int i = 0, length = memberTypes.length; i < length; i++)
				s += (memberTypes[i] != null) ? "\n" + memberTypes[i].toString() : "\nNULL TYPE"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL MEMBER TYPES"; //$NON-NLS-1$
	}

	s += "\n\n\n"; //$NON-NLS-1$
	return s;
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
		Enumeration enum = synthetics[FIELD_EMUL].elements();
		while (enum.hasMoreElements()) {
			field = (FieldBinding) enum.nextElement();
			if (CharOperation.prefixEquals(SyntheticArgumentBinding.EnclosingInstancePrefix, field.name)
				&& targetEnclosingType.isSuperclassOf((ReferenceBinding) field.type))
					return field;
		}
	}
	return null;
}
}
