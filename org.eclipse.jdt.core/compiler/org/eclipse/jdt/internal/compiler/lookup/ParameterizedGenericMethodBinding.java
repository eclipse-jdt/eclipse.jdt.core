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
import java.util.Map;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Binding denoting a generic method after type parameter substitutions got performed.
 * On parameterized type bindings, all methods got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these methods.
 */
public class ParameterizedGenericMethodBinding extends ParameterizedMethodBinding implements Substitution {

    public TypeBinding[] typeArguments; 
    private LookupEnvironment environment;
    public boolean inferredReturnType;
    public boolean wasInferred; // only set to true for instances resulting from method invocation inferrence
    
    /**
     * Create method of parameterized type, substituting original parameters with type arguments.
     */
	public ParameterizedGenericMethodBinding(MethodBinding originalMethod, TypeBinding[] typeArguments, LookupEnvironment environment) {

	    this.environment = environment;
		this.modifiers = originalMethod.modifiers;
		this.selector = originalMethod.selector;
		this.declaringClass = originalMethod.declaringClass;
	    this.typeVariables = NoTypeVariables;
	    this.typeArguments = typeArguments;
	    this.originalMethod = originalMethod;
	    this.parameters = Scope.substitute(this, originalMethod.parameters);
	    this.thrownExceptions = Scope.substitute(this, originalMethod.thrownExceptions);
	    this.returnType = this.substitute(originalMethod.returnType);
	    this.wasInferred = true;// resulting from method invocation inferrence
	}
	/**
	 * Create raw generic method for raw type (double substitution from type vars with raw type arguments, and erasure of method variables)
	 * Only invoked for non-static generic methods of raw type (could remove support for statics from below)
	 */
	public ParameterizedGenericMethodBinding(MethodBinding originalMethod, RawTypeBinding rawType, LookupEnvironment environment) {

		TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
		int length = originalVariables.length;
		TypeBinding[] rawArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			rawArguments[i] = originalVariables[i].erasure();
		}		
	    this.environment = environment;
		this.modifiers = originalMethod.modifiers;
		this.selector = originalMethod.selector;
		this.declaringClass = rawType;
	    this.typeVariables = NoTypeVariables;
	    this.typeArguments = rawArguments;
	    this.originalMethod = originalMethod;
		boolean isStatic = originalMethod.isStatic();
	    this.parameters = Scope.substitute(this, isStatic 
	    									? originalMethod.parameters // no substitution if original was static
	    									: Scope.substitute(rawType, originalMethod.parameters));
	    this.thrownExceptions = Scope.substitute(this, 	isStatic 
	    									? originalMethod.thrownExceptions // no substitution if original was static
	    									: Scope.substitute(rawType, originalMethod.thrownExceptions));
	    this.returnType = this.substitute(isStatic 
	    									? originalMethod.returnType // no substitution if original was static
	    									: rawType.substitute(originalMethod.returnType));
	    this.wasInferred = false; // not resulting from method invocation inferrence
	}
	
	/**
	 * Perform inference of generic method type parameters and/or expected type
	 */	
	public static MethodBinding computeCompatibleMethod(MethodBinding originalMethod, TypeBinding[] arguments, Scope scope, InvocationSite invocationSite) {
		
		ParameterizedGenericMethodBinding methodSubstitute;
		TypeVariableBinding[] typeVariables = originalMethod.typeVariables;
		TypeBinding[] substitutes = invocationSite.genericTypeArguments();
		
		if (substitutes != null) {
			if (substitutes.length != typeVariables.length) {
		        // incompatible due to wrong arity
		        return new ProblemMethodBinding(originalMethod, originalMethod.selector, substitutes, TypeParameterArityMismatch);
			}
			methodSubstitute = new ParameterizedGenericMethodBinding(originalMethod, substitutes, scope.environment());
		} else {
			// perform type inference based on argument types and expected type
			
			// collect substitutes by pattern matching parameters and arguments
			TypeBinding[] parameters = originalMethod.parameters;
			int varLength = typeVariables.length;
			HashMap collectedSubstitutes = new HashMap(varLength);
			for (int i = 0; i < varLength; i++)
				collectedSubstitutes.put(typeVariables[i], new TypeBinding[1]);
			
			// collect argument type mapping, handling varargs
			if (originalMethod.isVarargs()) {
				int paramLength = parameters.length;
				int minArgLength = paramLength - 1;
				int argLength = arguments.length;
				// process mandatory arguments
				for (int i = 0; i < minArgLength; i++)
					parameters[i].collectSubstitutes(arguments[i], collectedSubstitutes);
				// process optional arguments
				if (minArgLength < argLength) {
					TypeBinding varargType = parameters[minArgLength]; // last arg type - as is ?
					if (paramLength != argLength // argument is passed as is ?
							||  (arguments[minArgLength] != NullBinding
									&& (arguments[minArgLength].dimensions() != varargType.dimensions()))) { 
						varargType = ((ArrayBinding)varargType).elementsType(); // eliminate one array dimension
					}
					for (int i = minArgLength; i < argLength; i++)
						varargType.collectSubstitutes(arguments[i], collectedSubstitutes);
				}
			} else {
				int paramLength = parameters.length;
				for (int i = 0; i < paramLength; i++)
					parameters[i].collectSubstitutes(arguments[i], collectedSubstitutes);
			}
			substitutes = new TypeBinding[varLength];
			boolean needReturnTypeInference = false;
			for (int i = 0; i < varLength; i++) {
				TypeBinding[] variableSubstitutes = (TypeBinding[]) collectedSubstitutes.get(typeVariables[i]);
				TypeBinding mostSpecificSubstitute = scope.lowerUpperBound(variableSubstitutes);
				//TypeBinding mostSpecificSubstitute = scope.mostSpecificCommonType(variableSubstitutes);
				if (mostSpecificSubstitute == null)
					return null; // incompatible
				if (mostSpecificSubstitute == VoidBinding) {
					needReturnTypeInference = true;
				    mostSpecificSubstitute = typeVariables[i];
				}
				substitutes[i] = mostSpecificSubstitute;
			}
			// apply inferred variable substitutions
			methodSubstitute = new ParameterizedGenericMethodBinding(originalMethod, substitutes, scope.environment());
	
			if (needReturnTypeInference && invocationSite instanceof MessageSend) {
				MessageSend message = (MessageSend) invocationSite;
				TypeBinding expectedType = message.expectedType;
				if (expectedType != null)
					methodSubstitute.inferFromExpectedType(message.expectedType, scope);
			}
		}
		// check bounds
		for (int i = 0, length = typeVariables.length; i < length; i++) {
		    TypeVariableBinding typeVariable = typeVariables[i];
		    if (!typeVariable.boundCheck(methodSubstitute, substitutes[i]))
		        // incompatible due to bound check
		        return new ProblemMethodBinding(methodSubstitute, originalMethod.selector, new TypeBinding[]{substitutes[i], typeVariables[i] }, ParameterBoundMismatch);
		}

		return methodSubstitute;
	}

	/**
	 * Returns true if some parameters got substituted.
	 * NOTE: generic method invocation delegates to its declaring method (could be a parameterized one)
	 */
	public boolean hasSubstitutedParameters() {
		// generic parameterized method can represent either an invocation or a raw generic method
		if (this.wasInferred) 
			return this.originalMethod.hasSubstitutedParameters();
		return super.hasSubstitutedParameters();
	}
	/**
	 * Returns true if the return type got substituted.
	 * NOTE: generic method invocation delegates to its declaring method (could be a parameterized one)
	 */
	public boolean hasSubstitutedReturnType() {
		if (this.wasInferred) 
			return this.originalMethod.hasSubstitutedReturnType();
		return super.hasSubstitutedReturnType();
	}
	
	public void inferFromExpectedType(TypeBinding expectedType, Scope scope) {
	    if (this.returnType == expectedType) 
	        return;
        if ((this.returnType.tagBits & TagBits.HasTypeVariable) == 0) 
            return;
        Map substitutes = new HashMap(1);
        int length = this.typeArguments.length;
        TypeVariableBinding[] originalVariables = this.original().typeVariables;
        boolean hasUnboundParameters = false;
        for (int i = 0; i < length; i++) {
            if (this.typeArguments[i] == originalVariables[i]) {
                hasUnboundParameters = true;
	        	substitutes.put(originalVariables[i], new TypeBinding[1]);
            } else {
	        	substitutes.put(originalVariables[i], new TypeBinding[] { this.typeArguments[i] });
            }
        }
        if (!hasUnboundParameters)
            return;
        returnType.collectSubstitutes(expectedType, substitutes);
		for (int i = 0; i < length; i++) {
			TypeBinding[] variableSubstitutes = (TypeBinding[]) substitutes.get(originalVariables[i]);
			TypeBinding mostSpecificSubstitute = scope.lowerUpperBound(variableSubstitutes);
			if (mostSpecificSubstitute == null) {
			    return; // TODO (philippe) should report no way to infer type
			}
			if (mostSpecificSubstitute != VoidBinding) 
				this.typeArguments[i] = mostSpecificSubstitute;
		}
		TypeBinding oldReturnType = this.returnType;
		this.returnType = this.substitute(this.returnType);
		this.inferredReturnType = this.returnType != oldReturnType;
	    this.parameters = Scope.substitute(this, this.parameters);
	    this.thrownExceptions = Scope.substitute(this, this.thrownExceptions);
	}
	
    /**
	 * Returns a type, where original type was substituted using the receiver
	 * parameterized method.
	 */
	public TypeBinding substitute(TypeBinding originalType) {
	    
		switch (originalType.bindingType()) {
			
			case Binding.TYPE_PARAMETER:
		        TypeVariableBinding originalVariable = (TypeVariableBinding) originalType;
		        TypeVariableBinding[] variables = this.originalMethod.typeVariables;
		        int length = variables.length;
		        // check this variable can be substituted given parameterized type
		        if (originalVariable.rank < length && variables[originalVariable.rank] == originalVariable) {
					return this.typeArguments[originalVariable.rank];
		        }
		        if (this.declaringClass instanceof Substitution) {
		        	return ((Substitution)this.declaringClass).substitute(originalType);
		        }
		        break;
	   		       
			case Binding.PARAMETERIZED_TYPE:
				ParameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding) originalType;
				ReferenceBinding originalEnclosing = originalType.enclosingType();
				ReferenceBinding substitutedEnclosing = originalEnclosing;
				if (originalEnclosing != null) {
					substitutedEnclosing = (ReferenceBinding) this.substitute(originalEnclosing);
				}
				TypeBinding[] originalArguments = originalParameterizedType.arguments;
				TypeBinding[] substitutedArguments = originalArguments;
				if (originalArguments != null) {
					substitutedArguments = Scope.substitute(this, originalArguments);
				}
				if (substitutedArguments != originalArguments || substitutedEnclosing != originalEnclosing) {
					identicalVariables: { // if substituted with original variables, then answer the generic type itself
						if (substitutedEnclosing != originalEnclosing) break identicalVariables;
						TypeVariableBinding[] originalVariables = originalParameterizedType.type.typeVariables();
						length = originalVariables.length;
						for (int i = 0; i < length; i++) {
							if (substitutedArguments[i] != originalVariables[i]) break identicalVariables;
						}
						return originalParameterizedType.type;
					}
					return this.environment.createParameterizedType(
							originalParameterizedType.type, substitutedArguments, substitutedEnclosing);
				}
				break;   		        
		        
			case Binding.ARRAY_TYPE:
				TypeBinding originalLeafComponentType = originalType.leafComponentType();
				TypeBinding substitute = substitute(originalLeafComponentType); // substitute could itself be array type
				if (substitute != originalLeafComponentType) {
					return this.environment.createArrayType(substitute.leafComponentType(), substitute.dimensions() + originalType.dimensions());
				}
				break;
				
			case Binding.WILDCARD_TYPE:
		        WildcardBinding wildcard = (WildcardBinding) originalType;
		        if (wildcard.kind != Wildcard.UNBOUND) {
			        TypeBinding originalBound = wildcard.bound;
			        TypeBinding substitutedBound = substitute(originalBound);
			        if (substitutedBound != originalBound) {
		        		return this.environment.createWildcard(wildcard.genericType, wildcard.rank, substitutedBound, wildcard.kind);
			        }
		        }
		        break;
	
	
			case Binding.GENERIC_TYPE:
			    // treat as if parameterized with its type variables
				ReferenceBinding originalGenericType = (ReferenceBinding) originalType;
				originalEnclosing = originalType.enclosingType();
				substitutedEnclosing = originalEnclosing;
				if (originalEnclosing != null) {
					substitutedEnclosing = (ReferenceBinding) this.substitute(originalEnclosing);
				}
				TypeVariableBinding[] originalVariables = originalGenericType.typeVariables();
				length = originalVariables.length;
				System.arraycopy(originalVariables, 0, originalArguments = new TypeBinding[length], 0, length);
				substitutedArguments = Scope.substitute(this, originalArguments);
				if (substitutedArguments != originalArguments || substitutedEnclosing != originalEnclosing) {
					return this.environment.createParameterizedType(
							originalGenericType, substitutedArguments, substitutedEnclosing);
				}
				break;
	    }
	    return originalType;
	}
}
