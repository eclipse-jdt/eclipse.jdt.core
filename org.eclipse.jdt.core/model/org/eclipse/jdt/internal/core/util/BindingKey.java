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
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
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
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/*
 * A wrapper class arround a binding key (see Binding#computeUniqueKey()).
 * This decodes a binding keys, and optionally creates the corresponding compiler binding,
 * or computes its signature.
 */
public class BindingKey {
	
	public class Scanner {
		
		public static final int START = -1;
		public static final int PACKAGE = 0;
		public static final int TYPE = 1;
		public static final int FIELD = 2;
		public static final int METHOD = 3;
		public static final int ARRAY = 4;
		public static final int LOCAL_VAR = 5;
		public static final int END = 6;
		
		int index = 0, start;
		public char[] source;
		public int token = START;
	
		Scanner(char[] source) {
			this.source = source;
		}
		
		char[] getTokenSource() {
			int length = this.index-this.start;
			char[] result = new char[length];
			System.arraycopy(this.source, this.start, result, 0, length);
			return result;
		}
		
		boolean isAtFieldOrMethodStart() {
			return 
				this.index+1 < this.source.length
				&& this.source[this.index+1] == '.';
		}
		
		boolean isAtLocalVariableStart() {
			return 
				this.index < this.source.length
				&& this.source[this.index] == '#';
		}
		
		boolean isAtMemberTypeStart() {
			return 
				this.index < this.source.length
				&& (this.source[this.index] == '$'
					|| (this.source[this.index] == '.' && this.source[this.index-1] == '>'));
		}
		
		boolean isAtParametersStart() {
			char currentChar;
			return 
				this.index > 0
				&& this.index < this.source.length
				&& ((currentChar = this.source[this.index]) == '<'
					|| currentChar == '%');
		}
		
		boolean isAtTypeParameterStart() {
			return 
				this.index+1 < this.source.length
				&& this.source[this.index+1] == 'T';
		}
		
		boolean isAtTypeVariableStart() {
			return 
				this.index+3 < this.source.length
				&& this.source[this.index+3] == ':';
		}
	
		boolean isAtTypeStart() {
			return this.index+1 < this.source.length && "LIZVCDBFJS[".indexOf(this.source[this.index+1]) != -1; //$NON-NLS-1$
		}
		
		boolean isAtWildCardStart() {
			return this.index+1 < this.source.length && "*+-".indexOf(this.source[this.index+1]) != -1; //$NON-NLS-1$
		}
		
		int nextToken() {
			int previousTokenEnd = this.index;
			this.start = this.index;
			int length = this.source.length;
			while (this.index <= length) {
				char currentChar = this.index == length ? Character.MIN_VALUE : this.source[this.index];
				switch (currentChar) {
					case 'B':
					case 'C':
					case 'D':
					case 'F':
					case 'I':
					case 'J':
					case 'S':
					case 'V':
					case 'Z':
						// base type
						if (this.index == previousTokenEnd) {
							this.index++;
							this.token = TYPE;
							return this.token;
						}
						break;
					case 'L':
					case 'T':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
						}
						break;
					case ';':
					case '$':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							previousTokenEnd = this.start;
						} else {
							this.token = TYPE;
							return this.token;
						}
						break;
					case '.':
					case '%':
					case ':':
						this.start = this.index+1;
						previousTokenEnd = this.start;
						break;
					case '[':
						while (this.index < length && this.source[this.index] == '[')
							this.index++;
						this.token = ARRAY;
						return this.token;
					case '<':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							previousTokenEnd = this.start;
						} else if (this.start > 0) {
							switch (this.source[this.start-1]) {
								case '.':
									if (this.source[this.start-2] == '>')
										// case of member type where enclosing type is parameterized
										this.token = TYPE;
									else
										this.token = METHOD;
									return this.token;
								default:
									this.token = TYPE;
									return this.token;
							}
						} 
						break;
					case '(':
						this.token = METHOD;
						return this.token;
					case ')':
						this.start = ++this.index;
						this.token = END;
						return this.token;
					case '#':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							previousTokenEnd = this.start;
						} else {
							this.token = LOCAL_VAR;
							return this.token;
						}
						break;
					case Character.MIN_VALUE:
						switch (this.token) {
							case START:
								this.token = PACKAGE;
								break;
							case METHOD:
							case LOCAL_VAR:
								this.token = LOCAL_VAR;
								break;
							case TYPE:
								if (this.index > this.start && this.source[this.start-1] == '.')
									this.token = FIELD;
								else
									this.token = END;
								break;
							default:
								this.token = END;
								break;
						}
						return this.token;
					case '*':
					case '+':
					case '-':
						this.index++;
						this.token = TYPE;
						return this.token;
				}
				this.index++;
			}
			this.token = END;
			return this.token;
		}
		
		void skipMethodSignature() {
			char currentChar;
			while (this.index < this.source.length && (currentChar = this.source[this.index]) != '#' && currentChar != '%')
				this.index++;
		}
		
		void skipParametersEnd() {
			while (this.index < this.source.length && this.source[this.index] != '>')
				this.index++;
			this.index++;
		}
		
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			switch (this.token) {
				case START:
					buffer.append("START: "); //$NON-NLS-1$
					break;
				case PACKAGE:
					buffer.append("PACKAGE: "); //$NON-NLS-1$
					break;
				case TYPE:
					buffer.append("TYPE: "); //$NON-NLS-1$
					break;
				case FIELD:
					buffer.append("FIELD: "); //$NON-NLS-1$
					break;
				case METHOD:
					buffer.append("METHOD: "); //$NON-NLS-1$
					break;
				case ARRAY:
					buffer.append("ARRAY: "); //$NON-NLS-1$
					break;
				case LOCAL_VAR:
					buffer.append("LOCAL VAR: "); //$NON-NLS-1$
					break;
				case END:
					buffer.append("END: "); //$NON-NLS-1$
					break;
			}
			if (this.index < 0) {
				buffer.append("**"); //$NON-NLS-1$
				buffer.append(this.source);
			} else if (this.index <= this.source.length) {
				buffer.append(CharOperation.subarray(this.source, 0, this.start));
				buffer.append('*');
				if (this.start <= this.index) {
					buffer.append(CharOperation.subarray(this.source, this.start, this.index));
					buffer.append('*');
					buffer.append(CharOperation.subarray(this.source, this.index, this.source.length));
				} else {
					buffer.append('*');
					buffer.append(CharOperation.subarray(this.source, this.start, this.source.length));
				}
			} else {
				buffer.append(this.source);
				buffer.append("**"); //$NON-NLS-1$
			}
			return buffer.toString();
		}
	
	}
	
	char[][] compoundName;
	int dimension;
	public Scanner scanner;
	Compiler compiler;
	LookupEnvironment environment;
	
	public BindingKey(String key) {
		this(key, null, null);
	}

	public BindingKey(String key, Compiler compiler, LookupEnvironment environment) {
		this.scanner = new Scanner(key.toCharArray());
		this.compiler = compiler;
		this.environment = environment;
		reset();
	}
	
	/*
	 * If not already cached, computes and cache the compound name (pkg name + top level name) of this key.
	 * Returns the package name if key is a pkg key.
	 * Returns an empty array if malformed.
	 * This key's scanner should be positioned on the package or type token.
	 */
	public char[][] compoundName() {
		if (this.compoundName == null) {
			switch(this.scanner.nextToken()) {
				case Scanner.PACKAGE:
				case Scanner.TYPE:
	 				this.compoundName = CharOperation.splitOn('/', this.scanner.getTokenSource());
	 				break;
		 		case Scanner.ARRAY:
		 			this.dimension = this.scanner.getTokenSource().length;
	 				if (this.scanner.nextToken() == Scanner.TYPE)
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
	TypeBinding getArrayBinding(int dim, TypeBinding binding) {
		if (binding == null) return null;
		if (dim == 0) return binding;
		return this.environment.createArrayType(binding, dim);
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
	TypeBinding getBinaryBinding() {
		TypeBinding binding = this.environment.getType(this.compoundName);
		return getArrayBinding(this.dimension, binding);
	}
	 
	/*
	 * Finds the compilation unit declaration corresponding to the key in the given lookup environment.
	 * Returns null if no compilation unit declaration could be found.
	 * This key's scanner should be positioned on the package token.
	 */
	public CompilationUnitDeclaration getCompilationUnitDeclaration() {
		char[][] name = compoundName();
		if (name.length == 0) return null;
		if (this.environment == null) return null;
		ReferenceBinding binding = this.environment.getType(name);
		if (!(binding instanceof SourceTypeBinding)) return null;
		return ((SourceTypeBinding) binding).scope.compilationUnitScope().referenceContext;
	}
	 
	public Binding getCompilerBinding(CompilationUnitDeclaration parsedUnit) {
		if (this.environment == null) return null;
		switch (this.scanner.token) {
			case Scanner.PACKAGE:
	 			return new PackageBinding(this.compoundName, null, this.environment);
	 		case Scanner.TYPE:
	 			if (this.compoundName.length == 1 && this.compoundName[0].length == 1) {
	 				// case of base type
		 			TypeBinding baseTypeBinding = getBaseTypeBinding(this.compoundName[0]);
		 			if (baseTypeBinding != null) 
	 					return getArrayBinding(this.dimension, baseTypeBinding);
	 			}
	 			char[] typeName = this.compoundName[this.compoundName.length-1];
	 			int dim = this.dimension;
	 			TypeBinding binding = parsedUnit == null ? getBinaryBinding() : getTypeBinding(parsedUnit, parsedUnit.types, typeName);
	 			if (binding == null) return null;
				TypeBinding typeBinding = null;
				if (this.scanner.isAtParametersStart()) {
					if (this.scanner.isAtTypeParameterStart())	 					
	 					// generic type binding
	 					typeBinding = getGenericTypeBinding((SourceTypeBinding) binding);
	 				else if (this.scanner.isAtTypeStart() || this.scanner.isAtWildCardStart())
						// parameterized type binding
	 					typeBinding = getParameterizedTypeBinding((ReferenceBinding) binding, null/*no enclosing type*/); 
				} else if (binding.typeVariables().length > 0)
					// raw type binding
					typeBinding = this.environment.createRawType((ReferenceBinding) binding, null/*no enclosing type*/);
				else
					// non-generic type binding
					typeBinding = binding;
	 			if (this.scanner.isAtFieldOrMethodStart()) {
	 				switch (this.scanner.nextToken()) {
		 				case Scanner.FIELD:
		 					return getFieldBinding(((ReferenceBinding) typeBinding).fields());
		 				case Scanner.METHOD:
		 					MethodBinding methodBinding = getMethodBinding(((ReferenceBinding) typeBinding).methods());
		 					if (this.scanner.isAtParametersStart())
		 						// parameterized generic method binding
		 						methodBinding = getParameterizedGenericMethodBinding(methodBinding);
		 					if (this.scanner.isAtLocalVariableStart()) {
		 						MethodScope methodScope = methodBinding.sourceMethod().scope;
		 						return getLocalVariableBinding(methodScope);
		 					} else
		 						return methodBinding;
	 				}
	 				return null; // malformed key
	 			} else if (this.scanner.isAtTypeVariableStart()) {
	 				return getTypeVariableBinding((SourceTypeBinding) binding);
	 			} else {
	 				return getArrayBinding(dim, typeBinding);
	 			}
	 	}
	 	return null;
	 }
	 
	/*
	 * Returns the compiler binding corresponding to this key.
	 * Returns null is malformed.
	 * This key's scanner should be positioned on the package token.
	 */
	public Binding getCompilerBinding() {
		CompilationUnitDeclaration parsedUnit = getCompilationUnitDeclaration();
		if (parsedUnit != null && this.compiler != null) {
			this.compiler.process(parsedUnit, this.compiler.totalUnits+1); // noop if unit has already been resolved
		}
		return getCompilerBinding(parsedUnit);
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
	 SourceTypeBinding getGenericTypeBinding(SourceTypeBinding typeBinding) {
	 	TypeVariableBinding[] typeVariableBindings = typeBinding.typeVariables();
	 	for (int i = 0, length = typeVariableBindings.length; i < length; i++) {
			TypeVariableBinding typeVariableBinding = typeVariableBindings[i];
			if (this.scanner.nextToken() != Scanner.TYPE)
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
	public String getKey() {
		return new String(this.scanner.source);
	}
	 
	 LocalVariableBinding getLocalVariableBinding(BlockScope scope) {
	 	if (this.scanner.nextToken() != Scanner.LOCAL_VAR)
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
	 MethodBinding getMethodBinding(MethodBinding[] methods) {
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
	  * Finds parameterized generic method binding that corresponds to this key.
	  * This key's scanner should be positionned on the first type argument name token.
	  */
	 ParameterizedGenericMethodBinding getParameterizedGenericMethodBinding(MethodBinding methodBinding) {
	 	int length = methodBinding.typeVariables().length;
	 	TypeBinding[] arguments = new TypeBinding[length];
	 	for (int i = 0; i < length; i++) {
			reset();
			Binding argument = getCompilerBinding();
			if (argument == null) 
				return null;
			arguments[i] = (TypeBinding) argument;
		}
	 	return new ParameterizedGenericMethodBinding(methodBinding, arguments, this.environment);
	 }
	 
	 /*
	  * Finds parameterized type binding that corresponds to this key.
	  * This key's scanner should be positionned on the first type argument name token.
	  */
	 ParameterizedTypeBinding getParameterizedTypeBinding(ReferenceBinding genericType, ReferenceBinding enclosingType) {
	 	TypeVariableBinding[] typeVariableBindings = genericType.typeVariables();
	 	int length = typeVariableBindings.length;
	 	TypeBinding[] arguments = new TypeBinding[length];
	 	for (int i = 0; i < length; i++) {
	 		TypeBinding argument;
	 		if (this.scanner.isAtWildCardStart()) {
	 			argument = getWildCardBinding(genericType, i);
	 		} else {
				reset();
				argument = (TypeBinding) getCompilerBinding();
	 		}
			if (argument == null) 
				return this.environment.createRawType(genericType, enclosingType);
			arguments[i] =argument;
	 		
		}
	 	ParameterizedTypeBinding parameterizedTypeBinding = this.environment.createParameterizedType(genericType, arguments, enclosingType);
	 	// skip ";>"
	 	this.scanner.skipParametersEnd();
	 	if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == Scanner.TYPE) {
	 		char[] typeName = this.scanner.getTokenSource();
	 		ReferenceBinding memberType = genericType.getMemberType(typeName);
	 		return getParameterizedTypeBinding(memberType, parameterizedTypeBinding);
	 	} else {
		 	return parameterizedTypeBinding;
		}
	}
	
	/*
	 * Returns the signature of this binding key
	 */
	public String getSignature() {
		return null; // TODO (jerome) implement
	}
	
	/*
	 * Finds the type binding that corresponds to this key in the given type bindings.
	 * Returns null if not found.
	 * This key's scanner should be positioned on the type name token.
	 */
	 TypeBinding getTypeBinding(CompilationUnitDeclaration parsedUnit, TypeDeclaration[] types, char[] typeName) {
	 	if (Character.isDigit(typeName[0])) {
	 		// anonymous or local type
	 		int nextToken = Scanner.TYPE;
	 		while (this.scanner.isAtMemberTypeStart()) 
	 			nextToken = this.scanner.nextToken();
	 		typeName = nextToken == Scanner.END ? this.scanner.source : CharOperation.subarray(this.scanner.source, 0, this.scanner.index+1);
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
					if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == Scanner.TYPE)
						return getTypeBinding(parsedUnit, declaration.memberTypes, this.scanner.getTokenSource());
					else
						return declaration.binding;
				}
			}
	 	}
		return null;
	 }
	 
	 TypeVariableBinding getTypeVariableBinding(SourceTypeBinding typeBinding) {
	 	// skip ";>"
	 	this.scanner.skipParametersEnd();
		if (this.scanner.nextToken() != Scanner.TYPE)
			return null;
	 	char[] typeVariableName = this.scanner.getTokenSource();
	 	TypeVariableBinding[] typeVariableBindings = typeBinding.typeVariables();
	 	for (int i = 0, length = typeVariableBindings.length; i < length; i++) {
			TypeVariableBinding typeVariableBinding = typeVariableBindings[i];
			if (CharOperation.equals(typeVariableName, typeVariableBinding.sourceName()))
				return typeVariableBinding;
		}
	 	return null;
	 }
	 
	 TypeBinding getWildCardBinding(ReferenceBinding genericType, int rank) {
	 	if (this.scanner.nextToken() != Scanner.TYPE) return null;
	 	char[] source = this.scanner.getTokenSource();
	 	if (source.length == 0) return null; //malformed key
	 	int kind = -1;
	 	TypeBinding bound = null;
	 	switch (source[0]) {
		 	case '*':
		 		kind = Wildcard.UNBOUND;
		 		break;
		 	case '+':
		 		reset();
		 		kind = Wildcard.EXTENDS;
		 		bound = (TypeBinding) getCompilerBinding();
		 		break;
		 	case '-':
		 		reset();
		 		kind = Wildcard.SUPER;
		 		bound = (TypeBinding) getCompilerBinding();
		 		break;
	 	}
	 	if (kind == -1) return null; // malformed key
	 	if (kind != Wildcard.UNBOUND && bound == null) return null; // malformed key
 		return this.environment.createWildcard(genericType, rank, bound, kind);
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
