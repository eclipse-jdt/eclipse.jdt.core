/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Internal class.
 * @since 3.1
 */
class BindingKey {
	 char[][] compoundName;
	 int dimension;
	 BindingKeyScanner scanner;
	 
	 BindingKey(char[] key) {
	 	this.scanner = new BindingKeyScanner(key);
	 	reset();
	 }
	 
	 BindingKey(String key) {
	 	this(key.toCharArray());
	 }
	 
	 /*
	  * If not already cached, computes and cache the compound name (pkg name + top level name) of this key.
	  * Returns the package name if key is a pkg key.
	  * Returns an empty array if malformed.
	  * This key's scanner should be positioned on the package or type token.
	  */
	 char[][] compoundName() {
	 	if (this.compoundName == null) {
	 		switch(this.scanner.nextToken()) {
	 			case BindingKeyScanner.PACKAGE:
	 			case BindingKeyScanner.TYPE:
		 			this.compoundName = CharOperation.splitOn('/', this.scanner.getTokenSource());
		 			break;
		 		case BindingKeyScanner.ARRAY:
		 			this.dimension = this.scanner.getTokenSource().length;
		 			if (this.scanner.nextToken() == BindingKeyScanner.TYPE)
			 			this.compoundName = CharOperation.splitOn('/', this.scanner.getTokenSource());
		 			else
		 				// malformed key
				 		this.compoundName = CharOperation.NO_CHAR_CHAR;
		 			break;
		 		default:
			 		// malformed key
			 		this.compoundName = CharOperation.NO_CHAR_CHAR;
		 			break;
	 		}
	 	}
	 	return this.compoundName;
	 }
	 
	 /*
	  * If the given dimension is greater than 0 returns an array binding for the given type binding.
	  * Otherwise return the given type binding.
	  * Returns null if the given type binding is null.
	  */
	 Binding getArrayBinding(int dim, TypeBinding binding, CompilationUnitResolver resolver) {
	 	if (binding == null) return null;
	 	if (dim == 0) return binding;
		return resolver.lookupEnvironment.createArrayType(binding, dim);
	}
	
	TypeBinding getBaseTypeBinding(char[] signature) {
		switch (signature[0]) {
			case 'I' :
				return BaseTypes.IntBinding;
			case 'Z' :
				return BaseTypes.BooleanBinding;
			case 'V' :
				return BaseTypes.VoidBinding;
			case 'C' :
				return BaseTypes.CharBinding;
			case 'D' :
				return BaseTypes.DoubleBinding;
			case 'B' :
				return BaseTypes.ByteBinding;
			case 'F' :
				return BaseTypes.FloatBinding;
			case 'J' :
				return BaseTypes.LongBinding;
			case 'S' :
				return BaseTypes.ShortBinding;
			default :
				return null;
		}
	}
	 
	/*
	 * Returns a binary binding corresonding to this key's compound name.
	 * Returns null if not found.
	 * This key's scanner should be positioned on the token after the top level type.
	 */
	Binding getBinaryBinding(CompilationUnitResolver resolver) {
		TypeBinding binding = resolver.lookupEnvironment.getType(this.compoundName);
		return getArrayBinding(this.dimension, binding, resolver);
	}
	 
	 /*
	  * Finds the compilation unit declaration corresponding to the key in the given lookup environment.
	  * Returns null if no compilation unit declaration could be found.
	  * This key's scanner should be positioned on the package token.
	  */
	 CompilationUnitDeclaration getCompilationUnitDeclaration(LookupEnvironment lookupEnvironment) {
		char[][] name = compoundName();
		if (name.length == 0) return null;
		ReferenceBinding binding = lookupEnvironment.getType(name);
		if (!(binding instanceof SourceTypeBinding)) return null;
		return ((SourceTypeBinding) binding).scope.compilationUnitScope().referenceContext;
	 }
	 
	 /*
	  * Returns the compiler binding corresponding to this key.
	  * This key's scanner should be positioned on the top level type token.
	  * Returns null otherwise.
	  */
	 Binding getCompilerBinding(CompilationUnitDeclaration parsedUnit, CompilationUnitResolver resolver) {
	 	switch (this.scanner.token) {
	 		case BindingKeyScanner.PACKAGE:
	 			return new PackageBinding(this.compoundName, null, resolver.lookupEnvironment);
	 		case BindingKeyScanner.TYPE:
	 			if (this.compoundName.length == 1 && this.compoundName[0].length == 1) {
	 				// case of base type
		 			TypeBinding baseTypeBinding = getBaseTypeBinding(this.compoundName[0]);
		 			if (baseTypeBinding != null) 
	 					return getArrayBinding(this.dimension, baseTypeBinding, resolver);
	 			}
	 			if (parsedUnit == null) 
	 				return getBinaryBinding(resolver);
	 			char[] typeName = this.compoundName[this.compoundName.length-1];
	 			int dim = this.dimension;
	 			TypeBinding binding = getTypeBinding(parsedUnit, parsedUnit.types, typeName, resolver);
	 			if (binding == null) return null;
	 			if (this.scanner.isAtFieldOrMethodStart()) {
	 				switch (this.scanner.nextToken()) {
		 				case BindingKeyScanner.FIELD:
		 					return getFieldBinding(((SourceTypeBinding) binding).fields);
		 				case BindingKeyScanner.METHOD:
		 					MethodBinding methodBinding = getMethodBinding(((SourceTypeBinding) binding).methods, resolver);
		 					if (this.scanner.isAtLocalVariableStart()) {
		 						MethodScope methodScope = methodBinding.sourceMethod().scope;
		 						return getLocalVariableBinding(methodScope);
		 					} else
		 						return methodBinding;
	 				}
	 				return null; // malformed key
	 			} else {
	 				TypeBinding typeBinding = null;
	 				if (this.scanner.isAtParametersStart()) {
						if (this.scanner.isAtTypeParameterStart())	 					
		 					// generic type binding
		 					typeBinding = getGenericTypeBinding((SourceTypeBinding) binding, resolver);
		 				else if (this.scanner.isAtTypeStart())
	 						// parameterized type binding
		 					typeBinding = getParameterizedTypeBinding((ReferenceBinding) binding, null/*no enclosing type*/, resolver); 
	 				} else if (binding.typeVariables().length > 0)
	 					// raw type binding
	 					typeBinding = resolver.lookupEnvironment.createRawType((ReferenceBinding) binding, null/*no enclosing type*/);
	 				else
 						// non-generic type binding
 						typeBinding = binding;
	 				return getArrayBinding(dim, typeBinding, resolver);
	 			}
	 	}
	 	return null;
	 }
	 
	 /*
	  * Returns the compiler binding corresponding to this key.
	  * Returns null is malformed.
	  * This key's scanner should be positioned on the package token.
	  */
	 Binding getCompilerBinding(CompilationUnitResolver resolver) {
		CompilationUnitDeclaration parsedUnit = getCompilationUnitDeclaration(resolver.lookupEnvironment);
		if (parsedUnit != null) {
			char[] fileName = parsedUnit.compilationResult.getFileName();
			// don't resolve a second time the same unit (this would create the same bindingd twice)
			if (!resolver.requestedKeys.containsKey(fileName) && !resolver.requestedSources.containsKey(fileName))
				resolver.process(parsedUnit, resolver.totalUnits+1);
		}
		return getCompilerBinding(parsedUnit, resolver);
	 }

	/*
	 * Finds the field binding that corresponds to this key in the given field bindings.
	 * Returns null if not found.
	 * This key's scanner should be positioned on the field name.
	 */
	FieldBinding getFieldBinding(FieldBinding[] fields) {
	 	if (fields == null) return null;
	 	char[] fieldName = this.scanner.getTokenSource();
	 	for (int i = 0, length = fields.length; i < length; i++) {
			FieldBinding field = fields[i];
			if (CharOperation.equals(fieldName, field.name)) 
				return field;
		}
	 	return null;
	 }
	 
	 /*
	  * Ensures that the given generic type binding corresponds to this key.
	  * This key's scanner should be positionned on the first type parameter name token.
	  */
	 SourceTypeBinding getGenericTypeBinding(SourceTypeBinding typeBinding, CompilationUnitResolver resolver) {
	 	TypeVariableBinding[] typeVariableBindings = typeBinding.typeVariables();
	 	for (int i = 0, length = typeVariableBindings.length; i < length; i++) {
			TypeVariableBinding typeVariableBinding = typeVariableBindings[i];
			if (this.scanner.nextToken() != BindingKeyScanner.TYPE)
				return null;
		 	char[] typeVariableName = this.scanner.getTokenSource();
			if (!CharOperation.equals(typeVariableName, typeVariableBinding.sourceName()))
				return null;
		}
	 	return typeBinding;
	 }
	 
	 /*
	  * Returns the string that this binding key wraps.
	  */
	 String getKey() {
	 	return new String(this.scanner.source);
	 }
	 
	 LocalVariableBinding getLocalVariableBinding(BlockScope scope) {
	 	if (this.scanner.nextToken() != BindingKeyScanner.LOCAL_VAR)
			return null; // malformed key
		char[] varName = this.scanner.getTokenSource();
		if (Character.isDigit(varName[0])) {
			int index = Integer.parseInt(new String(varName));
			if (index >= scope.subscopeCount)
				return null; // malformed key
			if (!this.scanner.isAtLocalVariableStart())
				return null; // malformed key
			return getLocalVariableBinding((BlockScope) scope.subscopes[index]);
		} else {
		 	for (int i = 0; i < scope.localIndex; i++) {
				LocalVariableBinding local = scope.locals[i];
				if (CharOperation.equals(varName, local.name))
					return local;
			}
		}
	 	return null;
	 }
	 
	/*
	 * Finds the method binding that corresponds to this key in the given method bindings.
	 * Returns null if not found.
	 * This key's scanner should be positioned on the selector token.
	 */
	 MethodBinding getMethodBinding(MethodBinding[] methods, CompilationUnitResolver resolver) {
	 	if (methods == null) return null;
	 	char[] selector = this.scanner.getTokenSource();
	 	this.scanner.skipMethodSignature();
	 	char[] signature = this.scanner.getTokenSource();
	 	
	 	nextMethod: for (int i = 0, methodLength = methods.length; i < methodLength; i++) {
			MethodBinding method = methods[i];
			if (CharOperation.equals(selector, method.selector) || (selector.length == 0 && method.isConstructor())) {
				if (CharOperation.equals(signature, method.genericSignature()))
					return method;
				return method;
			}
		}
	 	return null;
	 }
	 
	 /*
	  * Finds parameterized type binding that corresponds to this key.
	  * This key's scanner should be positionned on the first type argument name token.
	  */
	 ParameterizedTypeBinding getParameterizedTypeBinding(ReferenceBinding genericType, ReferenceBinding enclosingType, CompilationUnitResolver resolver) {
	 	TypeVariableBinding[] typeVariableBindings = genericType.typeVariables();
	 	int length = typeVariableBindings.length;
	 	TypeBinding[] arguments = new TypeBinding[length];
	 	for (int i = 0; i < length; i++) {
			reset();
			Binding argument = getCompilerBinding(resolver);
			if (argument == null) 
				return resolver.lookupEnvironment.createRawType(genericType, enclosingType);
			arguments[i] = (TypeBinding) argument;
		}
	 	ParameterizedTypeBinding parameterizedTypeBinding = resolver.lookupEnvironment.createParameterizedType(genericType, arguments, enclosingType);
	 	// skip ";>"
	 	this.scanner.index += 2;
	 	if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == BindingKeyScanner.TYPE) {
	 		char[] typeName = this.scanner.getTokenSource();
	 		ReferenceBinding memberType = genericType.getMemberType(typeName);
	 		return getParameterizedTypeBinding(memberType, parameterizedTypeBinding, resolver);
	 	} else {
		 	return parameterizedTypeBinding;
	 	}
	 }
	 
	/*
	 * Finds the type binding that corresponds to this key in the given type bindings.
	 * Returns null if not found.
	 * This key's scanner should be positioned on the type name token.
	 */
	 TypeBinding getTypeBinding(CompilationUnitDeclaration parsedUnit, TypeDeclaration[] types, char[] typeName, CompilationUnitResolver resolver) {
	 	if (Character.isDigit(typeName[0])) {
	 		// anonymous or local type
	 		int nextToken = BindingKeyScanner.TYPE;
	 		while (this.scanner.isAtMemberTypeStart()) 
	 			nextToken = this.scanner.nextToken();
	 		typeName = nextToken == BindingKeyScanner.END ? this.scanner.source : CharOperation.subarray(this.scanner.source, 0, this.scanner.index+1);
	 		LocalTypeBinding[] localTypeBindings  = parsedUnit.localTypes;
	 		for (int i = 0; i < parsedUnit.localTypeCount; i++)
	 			if (CharOperation.equals(typeName, localTypeBindings[i].signature()))
	 				return localTypeBindings[i];
	 		return null;
	 	} else {
	 		// member type
		 	if (types == null) return null;
			for (int i = 0, length = types.length; i < length; i++) {
				TypeDeclaration declaration = types[i];
				if (CharOperation.equals(typeName, declaration.name)) {
					if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == BindingKeyScanner.TYPE)
						return getTypeBinding(parsedUnit, declaration.memberTypes, this.scanner.getTokenSource(), resolver);
					else
						return declaration.binding;
				}
			}
	 	}
		return null;
	 }
	 
	 /*
	  * Forget about this key's compound name and dimension.
	  */
	 void reset() {
	 	this.compoundName = null;
	 	this.dimension = 0;
	 }
	 
	 public String toString() {
		return getKey();
	}
}
