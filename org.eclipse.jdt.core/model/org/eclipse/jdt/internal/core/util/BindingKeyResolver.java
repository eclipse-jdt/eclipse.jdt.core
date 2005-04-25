/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

public class BindingKeyResolver extends BindingKeyParser {
	Compiler compiler;
	Binding compilerBinding;
	
	char[][] compoundName;
	int dimension;
	LookupEnvironment environment;
	ReferenceBinding genericType;
	MethodBinding methodBinding;
	
	CompilationUnitDeclaration parsedUnit;
	BlockScope scope;
	TypeBinding typeBinding;
	TypeDeclaration typeDeclaration;
	ArrayList types = new ArrayList();
	int rank = 0;
	
	TypeBinding wildcardGenericType;
	int wildcardRank;
	
	private BindingKeyResolver(BindingKeyParser parser, Compiler compiler, LookupEnvironment environment, int wildcardRank, TypeBinding wildcardGenericType) {
		super(parser);
		this.compiler = compiler;
		this.environment = environment;
		this.wildcardRank = wildcardRank;
		this.wildcardGenericType = wildcardGenericType;
	}
	
	public BindingKeyResolver(String key) {
		this(key, null, null);
	}

	public BindingKeyResolver(String key, Compiler compiler, LookupEnvironment environment) {
		super(key);
		this.compiler = compiler;
		this.environment = environment;
	}
	
	/*
	 * If not already cached, computes and cache the compound name (pkg name + top level name) of this key.
	 * Returns the package name if key is a pkg key.
	 * Returns an empty array if malformed.
	 * This key's scanner should be positioned on the package or type token.
	 */
	public char[][] compoundName() {
		return this.compoundName;
	}
	 
	public void consumeArrayDimension(char[] brakets) {
		this.dimension = brakets.length;
	}
	
	public void consumeCapture(final int position) {
		if (this.parsedUnit == null) return;
		class CaptureFinder extends ASTVisitor {
			Binding parameterizedTypeBinding;
			public boolean visit(SingleNameReference singleNameReference, BlockScope blockScope) {
				if (singleNameReference.sourceEnd == position) {
					this.parameterizedTypeBinding = singleNameReference.resolvedType;
					return false;
				} 
				return super.visit(singleNameReference, blockScope);
			}
			public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope blockScope) {
				if (qualifiedNameReference.sourceEnd == position) {
					this.parameterizedTypeBinding = qualifiedNameReference.resolvedType;
					return false;
				} 
				return super.visit(qualifiedNameReference, blockScope);
			}
			public boolean visit(MessageSend messageSend, BlockScope blockScope) {
				if (messageSend.sourceEnd == position) {
					this.parameterizedTypeBinding = messageSend.resolvedType;
					return false;
				} 
				return super.visit(messageSend, blockScope);
			}
			public boolean visit(FieldReference fieldReference, BlockScope blockScope) {
				if (fieldReference.sourceEnd == position) {
					this.parameterizedTypeBinding = fieldReference.resolvedType;
					return false;
				} 
				return super.visit(fieldReference, blockScope);
			}
			public boolean visit(ConditionalExpression conditionalExpression, BlockScope blockScope) {
				if (conditionalExpression.sourceEnd == position) {
					this.parameterizedTypeBinding = conditionalExpression.resolvedType;
					return false;
				} 
				return super.visit(conditionalExpression, blockScope);
			}
			public boolean visit(CastExpression castExpression, BlockScope blockScope) {
				if (castExpression.sourceEnd == position) {
					this.parameterizedTypeBinding = castExpression.resolvedType;
					return false;
				} 
				return super.visit(castExpression, blockScope);
			}
			public boolean visit(Assignment assignment, BlockScope blockScope) {
				if (assignment.sourceEnd == position) {
					this.parameterizedTypeBinding = assignment.resolvedType;
					return false;
				} 
				return super.visit(assignment, blockScope);
			}
			public boolean visit(ArrayReference arrayReference, BlockScope blockScope) {
				if (arrayReference.sourceEnd == position) {
					this.parameterizedTypeBinding = arrayReference.resolvedType;
					return false;
				} 
				return super.visit(arrayReference, blockScope);
			}
		}
		CaptureFinder captureFinder = new CaptureFinder();
		this.parsedUnit.traverse(captureFinder, this.parsedUnit.scope);
		if (!(captureFinder.parameterizedTypeBinding instanceof ParameterizedTypeBinding))
			return;
		TypeBinding[] arguments = ((ParameterizedTypeBinding) captureFinder.parameterizedTypeBinding).arguments;
		if (arguments == null) return;
		Binding wildcardBinding = ((BindingKeyResolver) this.types.get(0)).compilerBinding;
		for (int i = 0, length = arguments.length; i < length; i++) {
			TypeBinding binding = arguments[i];
			if (binding instanceof CaptureBinding) {
				CaptureBinding captureBinding = (CaptureBinding) binding;
				if (captureBinding.wildcard == wildcardBinding && captureBinding.sourceType == this.typeBinding) {
					this.compilerBinding = binding;
					return;
				}
			}
		}
	}
	
	public void consumeField(char[] fieldName) {
		FieldBinding[] fields = ((ReferenceBinding) this.typeBinding).fields();
	 	for (int i = 0, length = fields.length; i < length; i++) {
			FieldBinding field = fields[i];
			if (CharOperation.equals(fieldName, field.name)) {
				this.compilerBinding = field;
				return;
			}
		}
	}

	public void consumeParameterizedMethod() {
		TypeBinding[] arguments = getTypeBindingArguments();
		if (arguments.length != this.methodBinding.typeVariables().length) return;
	 	this.methodBinding = new ParameterizedGenericMethodBinding(this.methodBinding, arguments, this.environment);
		this.compilerBinding = this.methodBinding;
	}
	
	public void consumeLocalType(char[] uniqueKey) {
 		LocalTypeBinding[] localTypeBindings  = this.parsedUnit.localTypes;
 		for (int i = 0; i < this.parsedUnit.localTypeCount; i++)
 			if (CharOperation.equals(uniqueKey, localTypeBindings[i].computeUniqueKey(false/*without access flags*/))) {
 				this.typeBinding = localTypeBindings[i];
				this.compilerBinding = this.typeBinding;
 				return;
 			}
	}

	public void consumeLocalVar(char[] varName) {
		if (this.scope == null) {
			this.scope = this.methodBinding.sourceMethod().scope;
		}
	 	for (int i = 0; i < this.scope.localIndex; i++) {
			LocalVariableBinding local = this.scope.locals[i];
			if (CharOperation.equals(varName, local.name)) {
				this.compilerBinding = local;
				return;
			}
		}
	}

	public void consumeMethod(char[] selector, char[] signature) {
		MethodBinding[] methods = ((ReferenceBinding) this.typeBinding).methods();
	 	for (int i = 0, methodLength = methods.length; i < methodLength; i++) {
			MethodBinding method = methods[i];
			if (CharOperation.equals(selector, method.selector) || (selector.length == 0 && method.isConstructor())) {
				char[] methodSignature = method.original().genericSignature();
				if (methodSignature == null)
					methodSignature = method.signature();
				if (CharOperation.equals(signature, methodSignature)) {
					this.methodBinding = method;
					this.compilerBinding = this.methodBinding;
					return;
				}
			}
		}
	}
	
	public void consumeMemberType(char[] simpleTypeName) {
		this.typeBinding = getTypeBinding(simpleTypeName);
		this.compilerBinding = this.typeBinding;
	}

	public void consumePackage(char[] pkgName) {
		this.compoundName = CharOperation.splitOn('/', pkgName);
		this.compilerBinding = new PackageBinding(this.compoundName, null, this.environment);
	}
	
	public void consumeParameterizedType(char[] simpleTypeName, boolean isRaw) {
		TypeBinding[] arguments = getTypeBindingArguments();
		if (simpleTypeName != null) {
			// parameterized member type
			this.genericType = this.genericType.getMemberType(simpleTypeName);
			if (!isRaw)
				this.typeBinding = this.environment.createParameterizedType(this.genericType, arguments, (ReferenceBinding) this.typeBinding);
			else
				// raw type
				this.typeBinding = this.environment.createRawType(this.genericType, (ReferenceBinding) this.typeBinding);
		} else {
			// parameterized top level type
			this.genericType = (ReferenceBinding) this.typeBinding;
			this.typeBinding = this.environment.createParameterizedType(this.genericType, arguments, null);
		}
		this.compilerBinding = this.typeBinding;
	}
	

	public void consumeParser(BindingKeyParser parser) {
		this.types.add(parser);
		if (((BindingKeyResolver) parser).compilerBinding instanceof WildcardBinding)
			this.rank++;
	}
	
	public void consumeScope(int scopeNumber) {
		if (this.scope == null) {
			this.scope = this.methodBinding.sourceMethod().scope;
		}
		if (scopeNumber >= this.scope.subscopeCount)
			return; // malformed key
		this.scope = (BlockScope) this.scope.subscopes[scopeNumber];
	}
	
	public void consumeRawType() {
		if (this.typeBinding == null) return;
		this.typeBinding = this.environment.createRawType((ReferenceBinding) this.typeBinding, null/*no enclosing type*/);
	}
	public void consumeSecondaryType(char[] simpleTypeName) {
		if (this.parsedUnit == null) return;
		this.typeDeclaration = null; // start from the parsed unit
		this.typeBinding = getTypeBinding(simpleTypeName);
		this.compilerBinding = this.typeBinding;
	}
	
	public void consumeFullyQualifiedName(char[] fullyQualifiedName) {
		this.compoundName = CharOperation.splitOn('/', fullyQualifiedName);
	}
	
	public void consumeTopLevelType() {
		if (this.compoundName.length == 1 && this.compoundName[0].length == 1) {
			// case of base type
 			TypeBinding baseTypeBinding = getBaseTypeBinding(this.compoundName[0]);
 			if (baseTypeBinding != null) {
				this.typeBinding = baseTypeBinding;
				this.compilerBinding = this.typeBinding;
				return;
 			}
		}
		this.parsedUnit = getCompilationUnitDeclaration();
		if (this.parsedUnit != null && this.compiler != null) {
			this.compiler.process(this.parsedUnit, this.compiler.totalUnits+1); // noop if unit has already been resolved
		}
		if (this.parsedUnit == null) {
			this.typeBinding = getBinaryBinding();
			this.compilerBinding = this.typeBinding;
		} else {
			char[] typeName = this.compoundName[this.compoundName.length-1];
			this.typeBinding = getTypeBinding(typeName);
			this.compilerBinding = this.typeBinding;
		}
	}
	
	public void consumeType() {
		this.typeBinding = getArrayBinding(this.dimension, this.typeBinding);
		this.compilerBinding = this.typeBinding;
	}
	
	public void consumeTypeVariable(char[] typeVariableName) {
	 	TypeVariableBinding[] typeVariableBindings = this.methodBinding != null ? this.methodBinding.typeVariables() : this.typeBinding.typeVariables();
	 	for (int i = 0, length = typeVariableBindings.length; i < length; i++) {
			TypeVariableBinding typeVariableBinding = typeVariableBindings[i];
			if (CharOperation.equals(typeVariableName, typeVariableBinding.sourceName())) {
				this.compilerBinding = typeVariableBinding;
				return;
			}
		}
	}
	
	public void consumeWildCard(int kind) {
		switch (kind) {
			case Wildcard.EXTENDS:
			case Wildcard.SUPER:
				BindingKeyResolver boundResolver = (BindingKeyResolver) this.types.get(0);
				this.compilerBinding = this.environment.createWildcard((ReferenceBinding) this.wildcardGenericType, this.wildcardRank, (TypeBinding) boundResolver.compilerBinding, null /*no extra bound*/, kind);
				break;
			case Wildcard.UNBOUND:
				this.compilerBinding = this.environment.createWildcard((ReferenceBinding) this.wildcardGenericType, rank++, null/*no bound*/, null /*no extra bound*/, kind);
				break;
		}
	}
	
	/*
	 * If the given dimension is greater than 0 returns an array binding for the given type binding.
	 * Otherwise return the given type binding.
	 * Returns null if the given type binding is null.
	 */
	private TypeBinding getArrayBinding(int dim, TypeBinding binding) {
		if (binding == null) return null;
		if (dim == 0) return binding;
		return this.environment.createArrayType(binding, dim);
	}
	
	private TypeBinding getBaseTypeBinding(char[] signature) {
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
			case 'N':
				return BaseTypes.NullBinding;
			default :
				return null;
		}
	}
	 
	/*
	 * Returns a binary binding corresonding to this key's compound name.
	 * Returns null if not found.
	 */
	private TypeBinding getBinaryBinding() {
		if (this.compoundName.length == 0) return null;
		return this.environment.getType(this.compoundName);
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
		SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) binding;
		if (sourceTypeBinding.scope == null) 
			return null;
		return sourceTypeBinding.scope.compilationUnitScope().referenceContext;
	}
	 
	/*
	 * Returns the compiler binding corresponding to this key.
	 * Returns null is malformed.
	 * This key's scanner should be positioned on the package token.
	 */
	public Binding getCompilerBinding() {
		parse();
		return this.compilerBinding;
	}
	
	private TypeBinding getTypeBinding(char[] simpleTypeName) {
		if (this.typeBinding instanceof BinaryTypeBinding) {
			return ((BinaryTypeBinding) this.typeBinding).getMemberType(simpleTypeName);
		} else {
			TypeDeclaration[] typeDeclarations = 
				this.typeDeclaration == null ? 
					(this.parsedUnit == null ? null : this.parsedUnit.types) : 
					this.typeDeclaration.memberTypes;
			if (typeDeclarations == null) return null;
			for (int i = 0, length = typeDeclarations.length; i < length; i++) {
				TypeDeclaration declaration = typeDeclarations[i];
				if (CharOperation.equals(simpleTypeName, declaration.name)) {
					this.typeDeclaration = declaration;
					return declaration.binding;
				}
			}
		}
		return null;
	}
	
	private TypeBinding[] getTypeBindingArguments() {
		int size = this.types.size();
		TypeBinding[] arguments = new TypeBinding[size];
		for (int i = 0; i < size; i++) {
			BindingKeyResolver resolver = (BindingKeyResolver) this.types.get(i);
			arguments[i] = (TypeBinding) resolver.compilerBinding;
		}
		this.types = new ArrayList();
		return arguments;
	}
	 
	public void malformedKey() {
		this.compoundName = CharOperation.NO_CHAR_CHAR;
	}
	
	public BindingKeyParser newParser() {
		return new BindingKeyResolver(this, this.compiler, this.environment, this.rank, this.typeBinding);
	}
	 
	public String toString() {
		return getKey();
	}

}
