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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Annotation
 */
public abstract class Annotation extends Expression {
	
	final static MemberValuePair[] NoValuePairs = new MemberValuePair[0];
	public int declarationSourceEnd;
	public Binding recipient;
	
	public TypeReference type;
	
	public static long getRetentionPolicy(char[] policyName) {
		if (policyName == null || policyName.length == 0)
			return 0;
		switch(policyName[0]) {
			case 'C' :
				if (CharOperation.equals(policyName, TypeConstants.UPPER_CLASS)) 
					return TagBits.AnnotationClassRetention;
				break;
			case 'S' :
				if (CharOperation.equals(policyName, TypeConstants.UPPER_SOURCE)) 
					return TagBits.AnnotationSourceRetention;
				break;
			case 'R' :
				if (CharOperation.equals(policyName, TypeConstants.UPPER_RUNTIME)) 
					return TagBits.AnnotationRuntimeRetention;
				break;
		}
		return 0; // unknown
	}
	
	public static long getTargetElementType(char[] elementName) {
		if (elementName == null || elementName.length == 0)
			return 0;
		switch(elementName[0]) {
			case 'A' :
				if (CharOperation.equals(elementName, TypeConstants.UPPER_ANNOTATION_TYPE)) 
					return TagBits.AnnotationForAnnotationType;
				break;
			case 'C' :
				if (CharOperation.equals(elementName, TypeConstants.UPPER_CONSTRUCTOR)) 
					return TagBits.AnnotationForConstructor;
				break;
			case 'F' :
				if (CharOperation.equals(elementName, TypeConstants.UPPER_FIELD)) 
					return TagBits.AnnotationForField;
				break;
			case 'L' :
				if (CharOperation.equals(elementName, TypeConstants.UPPER_LOCAL_VARIABLE)) 
					return TagBits.AnnotationForLocalVariable;
				break;
			case 'M' :
				if (CharOperation.equals(elementName, TypeConstants.UPPER_METHOD)) 
					return TagBits.AnnotationForMethod;
				break;
			case 'P' :
				if (CharOperation.equals(elementName, TypeConstants.UPPER_PARAMETER)) 
					return TagBits.AnnotationForParameter;
				else if (CharOperation.equals(elementName, TypeConstants.UPPER_PACKAGE)) 
					return TagBits.AnnotationForPackage;
				break;
			case 'T' :
				if (CharOperation.equals(elementName, TypeConstants.TYPE)) 
					return TagBits.AnnotationForType;
				break;
		}
		return 0; // unknown
	}		
	
	/**
	 * Returns the suppressed warning level (bit set when irritant is suppressed)
	 */
	public static long getSuppressedWarningLevel(String suppressedWarning) {
		if (suppressedWarning.length() > 0) {
			switch (suppressedWarning.charAt(0)) {
				
				case 'a' :
					if ("all".equals(suppressedWarning)) { //$NON-NLS-1$
						return 0xFFFFFFFFFFFFFFFFl; // suppress all warnings
					}
					break;
					
				case 'd' :
					if ("deprecation".equals(suppressedWarning)) { // //$NON-NLS-1$
						return CompilerOptions.UsingDeprecatedAPI;
					}
					break;
					
				case 'f' :
//					if ("fallthrough".equals(suppressedWarning)) { // //$NON-NLS-1$
//						return CompilerOptions.SwitchCaseFallthrough;
//					} else 
					if ("finally".equals(suppressedWarning)) { // //$NON-NLS-1$
						return  CompilerOptions.FinallyBlockNotCompleting;
					}
					break;
				case 's' :
					if ("serial".equals(suppressedWarning)) { // //$NON-NLS-1$
						return CompilerOptions.MissingSerialVersion;
					}
					break;
				case 'u' :
					if ("unchecked".equals(suppressedWarning)) { // //$NON-NLS-1$
						return  CompilerOptions.UncheckedTypeOperation;
					}
					break;
			}
		}
		return 0;
	}
	
	public CompilerOptions getCustomCompilerOptions(Scope scope) {
		CompilerOptions options = scope.compilerOptions();
		long warningLevel = options.warningThreshold;
		long originalWarningLevel = warningLevel;
		MemberValuePair[] pairs = this.memberValuePairs();
		pairLoop: for (int i = 0, length = pairs.length; i < length; i++) {
			MemberValuePair pair = pairs[i];
			if (CharOperation.equals(pair.name, TypeConstants.VALUE)) {
				Expression value = pair.value;
				if (value instanceof ArrayInitializer) {
					ArrayInitializer initializer = (ArrayInitializer) value;
					Expression[] inits = initializer.expressions;
					for (int j = 0, initsLength = inits.length; j < initsLength; j++) {
						Constant cst = inits[j].constant;
						if (cst != Constant.NotAConstant && cst.typeID() == T_JavaLangString) {
							warningLevel &= ~getSuppressedWarningLevel(cst.stringValue());
							if (warningLevel == 0) break pairLoop;
						}
					}
				} else {
					Constant cst = value.constant;
					if (cst != Constant.NotAConstant && cst.typeID() == T_JavaLangString) {
						warningLevel &= ~getSuppressedWarningLevel(cst.stringValue());
						if (warningLevel == 0) break pairLoop;
					}
				}
				break pairLoop;
			}
		}
		if (originalWarningLevel != warningLevel) {
			options = new CompilerOptions(options);
			options.warningThreshold = warningLevel;
		}
		return options;
	}
	
	/**
	 * Compute the bit pattern for recognized standard annotations the compiler may need to act upon
	 */
	private long detectStandardAnnotation(Scope scope, ReferenceBinding annotationType, MemberValuePair valueAttribute) {
		long tagBits = 0;
		switch (annotationType.id) {
			// retention annotation
			case TypeIds.T_JavaLangAnnotationRetention :
				if (valueAttribute != null) {
					Expression expr = valueAttribute.value;
					if ((expr.bits & Binding.VARIABLE) == Binding.FIELD) {
						FieldBinding field = ((Reference)expr).fieldBinding();
						if (field != null && field.declaringClass.id == T_JavaLangAnnotationRetentionPolicy) {
							tagBits |= getRetentionPolicy(field.name);
						}
					}
				}
				break;
			// target annotation
			case TypeIds.T_JavaLangAnnotationTarget :		
				tagBits |= TagBits.AnnotationTarget; // target specified (could be empty)
				if (valueAttribute != null) {
					Expression expr = valueAttribute.value;
					if (expr instanceof ArrayInitializer) {
						ArrayInitializer initializer = (ArrayInitializer) expr;
						final Expression[] expressions = initializer.expressions;
						if (expressions != null) {
							for (int i = 0, length = expressions.length; i < length; i++) {
								Expression initExpr = expressions[i];
								if ((initExpr.bits & Binding.VARIABLE) == Binding.FIELD) {
									FieldBinding field = ((Reference) initExpr).fieldBinding();
									if (field != null && field.declaringClass.id == T_JavaLangAnnotationElementType) {
										long element = getTargetElementType(field.name);
										if ((tagBits & element) != 0) {
											scope.problemReporter().duplicateTargetInTargetAnnotation(annotationType, (NameReference)initExpr);
										} else {
											tagBits |= element;
										}
									}							
								}
							}
						}
					} else if ((expr.bits & Binding.VARIABLE) == Binding.FIELD) {
						FieldBinding field = ((Reference) expr).fieldBinding();
						if (field != null && field.declaringClass.id == T_JavaLangAnnotationElementType) {
							tagBits |= getTargetElementType(field.name);
						}
					}
				}
				break;
			// marker annotations
			case TypeIds.T_JavaLangDeprecated :
				tagBits |= TagBits.AnnotationDeprecated;
				break;
			case TypeIds.T_JavaLangAnnotationDocumented :
				tagBits |= TagBits.AnnotationDocumented;
				break;
			case TypeIds.T_JavaLangAnnotationInherited :
				tagBits |= TagBits.AnnotationInherited;
				break;
			case TypeIds.T_JavaLangOverride :
				tagBits |= TagBits.AnnotationOverride;
				break;
			case TypeIds.T_JavaLangSuppressWarnings :
				tagBits |= TagBits.AnnotationSuppressWarnings;
				break;
		}
		return tagBits;
	}
	
	public abstract MemberValuePair[] memberValuePairs();
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append('@');
		this.type.printExpression(0, output);
		return output;
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		
		this.constant = NotAConstant;
		
		TypeBinding typeBinding = this.type.resolveType(scope);
		if (typeBinding == null)
			return null;
		this.resolvedType = typeBinding;
		// ensure type refers to an annotation type
		if (!typeBinding.isAnnotationType()) {
			scope.problemReporter().typeMismatchError(typeBinding, scope.getJavaLangAnnotationAnnotation(), this.type);
			return null;
		}

		ReferenceBinding annotationType = (ReferenceBinding) this.resolvedType;
		MethodBinding[] methods = annotationType.methods();
		// clone valuePairs to keep track of unused ones
		MemberValuePair[] originalValuePairs = memberValuePairs();
		MemberValuePair valueAttribute = null; // remember the first 'value' pair
		MemberValuePair[] pairs;
		int pairsLength = originalValuePairs.length;
		System.arraycopy(originalValuePairs, 0, pairs = new MemberValuePair[pairsLength], 0, pairsLength);
		
		nextMember: for (int i = 0, requiredLength = methods.length; i < requiredLength; i++) {
			MethodBinding method = methods[i];
			char[] selector = method.selector;
			boolean foundValue = false;
			nextPair: for (int j = 0; j < pairsLength; j++) {
				MemberValuePair pair = pairs[j];
				if (pair == null) continue nextPair;
				char[] name = pair.name;
				if (CharOperation.equals(name, selector)) {
					if (valueAttribute == null && CharOperation.equals(name, TypeConstants.VALUE)) {
						valueAttribute = pair;
					}
					pair.binding = method;
					pair.resolveTypeExpecting(scope, method.returnType);
					pairs[j] = null; // consumed
					foundValue = true;
					
					// check duplicates
					boolean foundDuplicate = false;
					for (int k = j+1; k < pairsLength; k++) {
						MemberValuePair otherPair = pairs[k];
						if (otherPair == null) continue;
						if (CharOperation.equals(otherPair.name, selector)) {
							foundDuplicate = true;
							scope.problemReporter().duplicateAnnotationValue(annotationType, otherPair);
							otherPair.binding = method;
							otherPair.resolveTypeExpecting(scope, method.returnType);
							pairs[k] = null;
						}
					}
					if (foundDuplicate) {
						scope.problemReporter().duplicateAnnotationValue(annotationType, pair);
						continue nextMember;
					}
				}
			}
			if (!foundValue && (method.modifiers & AccAnnotationDefault) == 0) {
				scope.problemReporter().missingValueForAnnotationMember(this, selector);
			}
		}
		// check unused pairs
		for (int i = 0; i < pairsLength; i++) {
			if (pairs[i] != null) {
				scope.problemReporter().undefinedAnnotationValue(annotationType, pairs[i]);
			}
		}
		// recognize standard annotations ?
		long tagBits = detectStandardAnnotation(scope, annotationType, valueAttribute);
		if (this.recipient != null) {
			if (tagBits != 0) {
				// tag bits onto recipient
				switch (this.recipient.kind()) {
					case Binding.PACKAGE :
						((PackageBinding)this.recipient).tagBits |= tagBits;
						break;
					case Binding.TYPE :
					case Binding.GENERIC_TYPE :
					case Binding.TYPE_PARAMETER :
						SourceTypeBinding sourceType = (SourceTypeBinding) this.recipient;
						sourceType.tagBits |= tagBits;
						if ((tagBits & TagBits.AnnotationSuppressWarnings) != 0) {
							ClassScope recipientScope = sourceType.scope;
							// construct custom compiler options with suppressed warnings
							CompilerOptions customOptions = getCustomCompilerOptions(recipientScope);
							if (customOptions != null) {
								TypeDeclaration typeDeclaration = recipientScope.referenceContext;
								recipientScope.options = customOptions;
								// discard already generated warnings which got suppressed
								typeDeclaration.compilationResult().suppressRecordedWarnings(
										typeDeclaration.declarationSourceStart, 
										typeDeclaration.declarationSourceEnd, 
										recipientScope.problemReporter());
							}
						}
						break;
					case Binding.METHOD :
						MethodBinding sourceMethod = (MethodBinding) this.recipient;
						sourceMethod.tagBits |= tagBits;
						if ((tagBits & TagBits.AnnotationSuppressWarnings) != 0) {
							AbstractMethodDeclaration methodDeclaration = ((SourceTypeBinding)sourceMethod.declaringClass).scope.referenceContext.declarationOf(sourceMethod);
							MethodScope recipientScope = methodDeclaration.scope;
							// construct custom compiler options with suppressed warnings
							CompilerOptions customOptions = getCustomCompilerOptions(recipientScope);
							if (customOptions != null) {
								recipientScope.options = customOptions;
								// discard already generated warnings which got suppressed
								methodDeclaration.compilationResult().suppressRecordedWarnings(
										methodDeclaration.declarationSourceStart, 
										methodDeclaration.declarationSourceEnd, 
										recipientScope.problemReporter());
							}
						}						
						break;
					case Binding.FIELD :
						((FieldBinding)this.recipient).tagBits |= tagBits;
						break;
					case Binding.LOCAL :
						((LocalVariableBinding)this.recipient).tagBits |= tagBits;
						break;
				}			
			}
			// check (meta)target compatibility
			checkTargetCompatibility: {
				long metaTagBits = annotationType.getAnnotationTagBits(); // could be forward reference
				if ((metaTagBits & TagBits.AnnotationTargetMASK) == 0) // does not specify any target restriction
					break checkTargetCompatibility;
					
				switch (recipient.kind()) {
					case Binding.PACKAGE :
						if ((metaTagBits & TagBits.AnnotationForPackage) != 0)
							break checkTargetCompatibility;
						break;
					case Binding.TYPE :
					case Binding.GENERIC_TYPE :
						if (((ReferenceBinding)this.recipient).isAnnotationType()) {
							if ((metaTagBits & (TagBits.AnnotationForAnnotationType|TagBits.AnnotationForType)) != 0)
							break checkTargetCompatibility;
						} else if ((metaTagBits & TagBits.AnnotationForType) != 0) 
							break checkTargetCompatibility;
						break;
					case Binding.METHOD :
						if (((MethodBinding)this.recipient).isConstructor()) {
							if ((metaTagBits & TagBits.AnnotationForConstructor) != 0)
								break checkTargetCompatibility;
						} else 	if ((metaTagBits & TagBits.AnnotationForMethod) != 0)
							break checkTargetCompatibility;
						break;
					case Binding.FIELD :
						if ((metaTagBits & TagBits.AnnotationForField) != 0)
							break checkTargetCompatibility;
						break;
					case Binding.LOCAL :
						if (((LocalVariableBinding)this.recipient).isArgument) {
							if ((metaTagBits & TagBits.AnnotationForParameter) != 0)
								break checkTargetCompatibility;
						} else 	if ((annotationType.tagBits & TagBits.AnnotationForLocalVariable) != 0)
							break checkTargetCompatibility;
						break;
				}			
				scope.problemReporter().disallowedTargetForAnnotation(this);
			}
		}
		return this.resolvedType;
	}
	
	public abstract void traverse(ASTVisitor visitor, BlockScope scope);
	public abstract void traverse(ASTVisitor visitor, CompilationUnitScope scope);
}
