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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class QualifiedNameReference extends NameReference {
	
	public char[][] tokens;
	public long[] sourcePositions;	
	public FieldBinding[] otherBindings, otherCodegenBindings;
	int[] otherDepths;
	public int indexOfFirstFieldBinding;//points (into tokens) for the first token that corresponds to first FieldBinding
	SyntheticMethodBinding syntheticWriteAccessor;
	SyntheticMethodBinding[] syntheticReadAccessors;
	public TypeBinding genericCast;
	public TypeBinding[] otherGenericCasts;
	
	public QualifiedNameReference(
		char[][] sources,
		long[] positions,
		int sourceStart,
		int sourceEnd) {
		super();
		this.tokens = sources;
		this.sourcePositions = positions;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	
	public FlowInfo analyseAssignment(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Assignment assignment,
		boolean isCompound) {

		// determine the rank until which we now we do not need any actual value for the field access
		int otherBindingsCount = otherBindings == null ? 0 : otherBindings.length;
		boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
		boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4;
		FieldBinding lastFieldBinding = null;
		switch (bits & RestrictiveFlagMASK) {
			case Binding.FIELD : // reading a field
				lastFieldBinding = (FieldBinding) binding;
				if (needValue || complyTo14) {
					manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, this.actualReceiverType, 0, flowInfo);
				}
				if (this.indexOfFirstFieldBinding == 1) { // was an implicit reference to the first field binding
					ReferenceBinding declaringClass = lastFieldBinding.declaringClass;
					// check if accessing enum static field in initializer					
					if (declaringClass.isEnum()) {
						MethodScope methodScope = currentScope.methodScope();
						SourceTypeBinding sourceType = methodScope.enclosingSourceType();
						if (lastFieldBinding.isStatic()
								&& (sourceType == declaringClass || sourceType.superclass == declaringClass) // enum constant body
								&& lastFieldBinding.constant() == NotAConstant
								&& !methodScope.isStatic
								&& methodScope.isInsideInitializerOrConstructor()) {
							currentScope.problemReporter().enumStaticFieldUsedDuringInitialization(lastFieldBinding, this);
						}
					}				
				}				
				// check if final blank field
				if (lastFieldBinding.isBlankFinal()
				    && this.otherBindings != null // the last field binding is only assigned
	 				&& currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)) {
					if (!flowInfo.isDefinitelyAssigned(lastFieldBinding)) {
						currentScope.problemReporter().uninitializedBlankFinalField(
							lastFieldBinding,
							this);
					}
				}
				break;
			case Binding.LOCAL :
				// first binding is a local variable
				LocalVariableBinding localBinding;
				if (!flowInfo
					.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
					currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
				}
				if (flowInfo.isReachable()) {
					localBinding.useFlag = LocalVariableBinding.USED;
				} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
					localBinding.useFlag = LocalVariableBinding.FAKE_USED;
				}
				this.checkNullStatus(currentScope, flowContext, flowInfo, FlowInfo.NON_NULL);
		}
		
		if (needValue) {
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
			// only for first binding
		}
		// all intermediate field accesses are read accesses
		if (otherBindings != null) {
			for (int i = 0; i < otherBindingsCount-1; i++) {
				lastFieldBinding = otherBindings[i];
				needValue = !otherBindings[i+1].isStatic();
				if (needValue || complyTo14) {
					manageSyntheticAccessIfNecessary(
						currentScope, 
						lastFieldBinding, 
						i == 0 
							? ((VariableBinding)binding).type
							: otherBindings[i-1].type,
						i + 1, 
						flowInfo);
				}
			}
			lastFieldBinding = otherBindings[otherBindingsCount-1];
		}

		if (isCompound) {
			if (binding == lastFieldBinding
				&& lastFieldBinding.isBlankFinal()
				&& currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)
				&& (!flowInfo.isDefinitelyAssigned(lastFieldBinding))) {
				currentScope.problemReporter().uninitializedBlankFinalField(
					lastFieldBinding,
					this);
			}
			TypeBinding lastReceiverType;
			if (lastFieldBinding == binding){
				lastReceiverType = this.actualReceiverType;
			} else if (otherBindingsCount == 1){
				lastReceiverType = ((VariableBinding)this.binding).type;
			} else {
				lastReceiverType = this.otherBindings[otherBindingsCount-2].type;
			}
			manageSyntheticAccessIfNecessary(
				currentScope,
				lastFieldBinding,
				lastReceiverType,
				lastFieldBinding == binding
					? 0 
					: otherBindingsCount, 
				flowInfo);
		}
		
		if (assignment.expression != null) {
			flowInfo =
				assignment
					.expression
					.analyseCode(currentScope, flowContext, flowInfo)
					.unconditionalInits();
		}
		
		// the last field access is a write access
		if (lastFieldBinding.isFinal()) {
			// in a context where it can be assigned?
			if (lastFieldBinding.isBlankFinal()
					&& !isCompound
					&& currentScope.allowBlankFinalFieldAssignment(lastFieldBinding) 
					&& indexOfFirstFieldBinding == 1) {
				if (flowInfo.isPotentiallyAssigned(lastFieldBinding)) {
					currentScope.problemReporter().duplicateInitializationOfBlankFinalField(lastFieldBinding, this);
				} else {
					flowContext.recordSettingFinal(lastFieldBinding, this, flowInfo);
				}
				flowInfo.markAsDefinitelyAssigned(lastFieldBinding);
			} else {
				currentScope.problemReporter().cannotAssignToFinalField(lastFieldBinding, this);
				if (currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)) { // pretend it got assigned
					flowInfo.markAsDefinitelyAssigned(lastFieldBinding);
				}
			}
		}
		// equivalent to valuesRequired[maxOtherBindings]
		TypeBinding lastReceiverType;
		if (lastFieldBinding == binding){
			lastReceiverType = this.actualReceiverType;
		} else if (otherBindingsCount == 1){
			lastReceiverType = ((VariableBinding)this.binding).type;
		} else {
			lastReceiverType = this.otherBindings[otherBindingsCount-2].type;
		}
		manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, lastReceiverType, -1 /*write-access*/, flowInfo);

		return flowInfo;
	}
	
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return analyseCode(currentScope, flowContext, flowInfo, true);
	}
	
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		boolean valueRequired) {
			
		// determine the rank until which we now we do not need any actual value for the field access
		int otherBindingsCount = otherBindings == null ? 0 : otherBindings.length;

		boolean needValue = otherBindingsCount == 0 ? valueRequired : !this.otherBindings[0].isStatic();
		boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4;
		switch (bits & RestrictiveFlagMASK) {
			case Binding.FIELD : // reading a field
				if (needValue || complyTo14) {
					manageSyntheticAccessIfNecessary(currentScope, (FieldBinding) binding, this.actualReceiverType, 0, flowInfo);
				}
				if (this.indexOfFirstFieldBinding == 1) { // was an implicit reference to the first field binding
					FieldBinding fieldBinding = (FieldBinding) binding;
					ReferenceBinding declaringClass = fieldBinding.declaringClass;
					// check if accessing enum static field in initializer					
					if (declaringClass.isEnum()) {
						MethodScope methodScope = currentScope.methodScope();
						SourceTypeBinding sourceType = methodScope.enclosingSourceType();
						if (fieldBinding.isStatic()
								&& (sourceType == declaringClass || sourceType.superclass == declaringClass) // enum constant body
								&& fieldBinding.constant() == NotAConstant
								&& !methodScope.isStatic
								&& methodScope.isInsideInitializerOrConstructor()) {
							currentScope.problemReporter().enumStaticFieldUsedDuringInitialization(fieldBinding, this);
						}
					}				
					// check if reading a final blank field
					if (fieldBinding.isBlankFinal()
							&& currentScope.allowBlankFinalFieldAssignment(fieldBinding)
							&& !flowInfo.isDefinitelyAssigned(fieldBinding)) {
						currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
					}
				}
				break;
			case Binding.LOCAL : // reading a local variable
				LocalVariableBinding localBinding;
				if (!flowInfo
					.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
					currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
				}
				if (flowInfo.isReachable()) {
					localBinding.useFlag = LocalVariableBinding.USED;
				} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
					localBinding.useFlag = LocalVariableBinding.FAKE_USED;
				}
				this.checkNullStatus(currentScope, flowContext, flowInfo, FlowInfo.NON_NULL);
		}
		if (needValue) {
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
			// only for first binding
		}
		if (otherBindings != null) {
			for (int i = 0; i < otherBindingsCount; i++) {
				needValue = i < otherBindingsCount-1 ? !otherBindings[i+1].isStatic() : valueRequired;
				if (needValue || complyTo14) {
					TypeBinding lastReceiverType = getGenericCast(i);
					if (lastReceiverType == null) {
						if (i == 0) {
							 lastReceiverType = ((VariableBinding)binding).type;
						} else {
							lastReceiverType = otherBindings[i-1].type;
						}
					}
					manageSyntheticAccessIfNecessary(
						currentScope, 
						otherBindings[i], 
						lastReceiverType,
						i + 1,
						flowInfo);
				}
			}
		}
		return flowInfo;
	}
	/**
	 * Check and/or redirect the field access to the delegate receiver if any
	 */
	public TypeBinding checkFieldAccess(BlockScope scope) {
		FieldBinding fieldBinding = (FieldBinding) binding;
		MethodScope methodScope = scope.methodScope();
		// check for forward references
		if (this.indexOfFirstFieldBinding == 1
				&& methodScope.enclosingSourceType() == fieldBinding.declaringClass
				&& methodScope.lastVisibleFieldID >= 0
				&& fieldBinding.id >= methodScope.lastVisibleFieldID
				&& (!fieldBinding.isStatic() || methodScope.isStatic)) {
			scope.problemReporter().forwardReference(this, 0, methodScope.enclosingSourceType());
		}
		bits &= ~RestrictiveFlagMASK; // clear bits
		bits |= Binding.FIELD;
		return getOtherFieldBindings(scope);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
	 */
	public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
		if (runtimeTimeType == null || compileTimeType == null)
			return;		
		// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
		FieldBinding field = null;
		int length = this.otherBindings == null ? 0 : this.otherBindings.length;
		if (length == 0) {
			if ((this.bits & Binding.FIELD) != 0 && this.binding != null && this.binding.isValidBinding()) {
				field = (FieldBinding) this.binding;
			}
		} else {
			field  = this.otherBindings[length-1];
		}
		if (field != null) {
			FieldBinding originalBinding = field.original();
			if (originalBinding != field) {
			    // extra cast needed if method return type has type variable
			    if ((originalBinding.type.tagBits & TagBits.HasTypeVariable) != 0 && runtimeTimeType.id != T_JavaLangObject) {
			    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType()) 
			    		? compileTimeType  // unboxing: checkcast before conversion
			    		: runtimeTimeType;
			    	setGenericCast(length,originalBinding.type.genericCast(targetType));
			    }
			} 	
		}
		super.computeConversion(scope, runtimeTimeType, compileTimeType);
	}

	public void generateAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Assignment assignment,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
		codeStream.recordPositionsFrom(pc , this.sourceStart);
		assignment.expression.generateCode(currentScope, codeStream, true);
		fieldStore(codeStream, lastFieldBinding, syntheticWriteAccessor, valueRequired);
		// equivalent to valuesRequired[maxOtherBindings]
		if (valueRequired) {
			codeStream.generateImplicitConversion(assignment.implicitConversion);
		}
	}
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		if (constant != NotAConstant) {
			if (valueRequired) {
				codeStream.generateConstant(constant, implicitConversion);
			}
		} else {
			FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
			if (lastFieldBinding != null) {
				boolean isStatic = lastFieldBinding.isStatic();
				if (lastFieldBinding.isConstantValue()) {
					if (!isStatic){
						codeStream.invokeObjectGetClass();
						codeStream.pop();
					}
					if (valueRequired) { // inline the last field constant
						codeStream.generateConstant(lastFieldBinding.constant(), implicitConversion);
					}
				} else {
					if (valueRequired  || currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4) {
						if (lastFieldBinding.declaringClass == null) { // array length
							codeStream.arraylength();
							if (valueRequired) {
								codeStream.generateImplicitConversion(implicitConversion);
							} else {
								// could occur if !valueRequired but compliance >= 1.4
								codeStream.pop();
							}
						} else {
							SyntheticMethodBinding accessor =
								syntheticReadAccessors == null
									? null
									: syntheticReadAccessors[syntheticReadAccessors.length - 1];
							if (accessor == null) {
								if (isStatic) {
									codeStream.getstatic(lastFieldBinding);
								} else {
									codeStream.getfield(lastFieldBinding);
								}
							} else {
								codeStream.invokestatic(accessor);
							}
							TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
							if (valueRequired) {
								if (requiredGenericCast != null) codeStream.checkcast(requiredGenericCast);
								codeStream.generateImplicitConversion(implicitConversion);
							} else {
								// could occur if !valueRequired but compliance >= 1.4
								switch (lastFieldBinding.type.id) {
									case T_long :
									case T_double :
										codeStream.pop2();
										break;
									default :
										codeStream.pop();
								}							
							}
						}
					} else {
						if (!isStatic){
							codeStream.invokeObjectGetClass(); // perform null check
							codeStream.pop();
						}
					}									
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	public void generateCompoundAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Expression expression,
		int operator,
		int assignmentImplicitConversion,
		boolean valueRequired) {
			
		FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
		SyntheticMethodBinding accessor =
			syntheticReadAccessors == null
				? null
				: syntheticReadAccessors[syntheticReadAccessors.length - 1];
		if (lastFieldBinding.isStatic()) {
			if (accessor == null) {
				codeStream.getstatic(lastFieldBinding);
			} else {
				codeStream.invokestatic(accessor);
			}
		} else {
			codeStream.dup();
			if (accessor == null) {
				codeStream.getfield(lastFieldBinding);
			} else {
				codeStream.invokestatic(accessor);
			}
		}
		// the last field access is a write access
		// perform the actual compound operation
		int operationTypeID;
		switch(operationTypeID = (implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) {
			case T_JavaLangString :
			case T_JavaLangObject :
			case T_undefined :
				codeStream.generateStringConcatenationAppend(currentScope, null, expression);
				break;
			default :
				TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
				if (requiredGenericCast != null) codeStream.checkcast(requiredGenericCast);				
				// promote the array reference to the suitable operation type
				codeStream.generateImplicitConversion(implicitConversion);
				// generate the increment value (will by itself  be promoted to the operation value)
				if (expression == IntLiteral.One) { // prefix operation
					codeStream.generateConstant(expression.constant, implicitConversion);
				} else {
					expression.generateCode(currentScope, codeStream, true);
				}
				// perform the operation
				codeStream.sendOperator(operator, operationTypeID);
				// cast the value back to the array reference type
				codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		// actual assignment
		fieldStore(codeStream, lastFieldBinding, syntheticWriteAccessor, valueRequired);
		// equivalent to valuesRequired[maxOtherBindings]
	}
	public void generatePostIncrement(
		BlockScope currentScope,
		CodeStream codeStream,
		CompoundAssignment postIncrement,
		boolean valueRequired) {
	    
		FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
		SyntheticMethodBinding accessor =
			syntheticReadAccessors == null
				? null
				: syntheticReadAccessors[syntheticReadAccessors.length - 1];
		if (lastFieldBinding.isStatic()) {
			if (accessor == null) {
				codeStream.getstatic(lastFieldBinding);
			} else {
				codeStream.invokestatic(accessor);
			}
		} else {
			codeStream.dup();
			if (accessor == null) {
				codeStream.getfield(lastFieldBinding);
			} else {
				codeStream.invokestatic(accessor);
			}
		}
		// duplicate the old field value
		if (valueRequired) {
			if (lastFieldBinding.isStatic()) {
				if ((lastFieldBinding.type == LongBinding)
					|| (lastFieldBinding.type == DoubleBinding)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
				if ((lastFieldBinding.type == LongBinding)
					|| (lastFieldBinding.type == DoubleBinding)) {
					codeStream.dup2_x1();
				} else {
					codeStream.dup_x1();
				}
			}
		}
		TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
		if (requiredGenericCast != null) codeStream.checkcast(requiredGenericCast);
		
		codeStream.generateImplicitConversion(implicitConversion);		
		codeStream.generateConstant(
			postIncrement.expression.constant,
			implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.implicitConversion & COMPILE_TYPE_MASK);
		codeStream.generateImplicitConversion(
			postIncrement.preAssignImplicitConversion);
		fieldStore(codeStream, lastFieldBinding, syntheticWriteAccessor, false);
	}
	/*
	 * Generate code for all bindings (local and fields) excluding the last one, which may then be generated code
	 * for a read or write access.
	 */
	public FieldBinding generateReadSequence(BlockScope currentScope, CodeStream codeStream) {
			
		// determine the rank until which we now we do not need any actual value for the field access
		int otherBindingsCount = this.otherCodegenBindings == null ? 0 : otherCodegenBindings.length;
		boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
		FieldBinding lastFieldBinding = null;
		TypeBinding lastGenericCast = null;
		boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4;

		switch (bits & RestrictiveFlagMASK) {
			case Binding.FIELD :
				lastFieldBinding = (FieldBinding) this.codegenBinding;
				lastGenericCast = this.genericCast;
				// if first field is actually constant, we can inline it
				if (lastFieldBinding.isConstantValue()) {
					break;
				}
				if ((needValue || complyTo14) && !lastFieldBinding.isStatic()) {
					int pc = codeStream.position;
					if ((bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						generateReceiver(codeStream);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
				}
				break;
			case Binding.LOCAL : // reading the first local variable
				if (!needValue) break; // no value needed
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				// regular local variable read
				if (localBinding.isConstantValue()) {
					codeStream.generateConstant(localBinding.constant(), 0);
					// no implicit conversion
				} else {
					// outer local?
					if ((bits & DepthMASK) != 0) {
						// outer local can be reached either through a synthetic arg or a synthetic field
						VariableBinding[] path = currentScope.getEmulationPath(localBinding);
						codeStream.generateOuterAccess(path, this, localBinding, currentScope);
					} else {
						codeStream.load(localBinding);
					}
				}
		}
						
		// all intermediate field accesses are read accesses
		// only the last field binding is a write access
		if (this.otherCodegenBindings != null) {
			for (int i = 0; i < otherBindingsCount; i++) {
				FieldBinding nextField = this.otherCodegenBindings[i];
				TypeBinding nextGenericCast = this.otherGenericCasts == null ? null : this.otherGenericCasts[i];
				if (lastFieldBinding != null) {
					needValue = !nextField.isStatic();
					if (lastFieldBinding.isConstantValue()) {
						if (lastFieldBinding != this.codegenBinding && !lastFieldBinding.isStatic()) {
							codeStream.invokeObjectGetClass(); // perform null check
							codeStream.pop();
						}
						if (needValue) {
							codeStream.generateConstant(lastFieldBinding.constant(), 0);
						}
					} else {
						if (needValue || complyTo14) {
							MethodBinding accessor = syntheticReadAccessors == null ? null : syntheticReadAccessors[i]; 
							if (accessor == null) {
								if (lastFieldBinding.isStatic()) {
									codeStream.getstatic(lastFieldBinding);
								} else {
									codeStream.getfield(lastFieldBinding);
								}
							} else {
								codeStream.invokestatic(accessor);
							}
							if (needValue) {
								if (lastGenericCast != null) codeStream.checkcast(lastGenericCast);
							} else {
								codeStream.pop();
							}
						} else {
							if (this.codegenBinding != lastFieldBinding && !lastFieldBinding.isStatic()){
								codeStream.invokeObjectGetClass(); // perform null check
								codeStream.pop();
							}						
						}
					}
				}
				lastFieldBinding = nextField;
				lastGenericCast = nextGenericCast;
			}
		}
		return lastFieldBinding;
	}
	public void generateReceiver(CodeStream codeStream) {
		codeStream.aload_0();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return null;
	}
	
	// get the matching codegenBinding
	protected FieldBinding getCodegenBinding(int index) {
	  if (index == 0){
			return (FieldBinding)this.codegenBinding;
		} else {
			return this.otherCodegenBindings[index-1];
		}
	}

	// get the matching generic cast
	protected TypeBinding getGenericCast(int index) {
	   if (index == 0){
			return this.genericCast;
		} else {
		    if (this.otherGenericCasts == null) return null;
			return this.otherGenericCasts[index-1];
		}
	}
	
	public TypeBinding getOtherFieldBindings(BlockScope scope) {
		// At this point restrictiveFlag may ONLY have two potential value : FIELD LOCAL (i.e cast <<(VariableBinding) binding>> is valid)
		int length = tokens.length;
		FieldBinding field;
		if ((bits & Binding.FIELD) != 0) {
			field = (FieldBinding) this.binding;
			if (!field.isStatic()) {
				//must check for the static status....
				if (indexOfFirstFieldBinding > 1  //accessing to a field using a type as "receiver" is allowed only with static field
						 || scope.methodScope().isStatic) { 	// the field is the first token of the qualified reference....
					scope.problemReporter().staticFieldAccessToNonStaticVariable(this, field);
					return null;
				 }
			} else {
				// indirect static reference ?
				if (indexOfFirstFieldBinding > 1 
						&& field.declaringClass != actualReceiverType) {
					scope.problemReporter().indirectAccessToStaticField(this, field);
				}
			}
			// only last field is actually a write access if any
			if (isFieldUseDeprecated(field, scope, (this.bits & IsStrictlyAssignedMASK) !=0 && indexOfFirstFieldBinding == length))
				scope.problemReporter().deprecatedField(field, this);
		} else {
			field = null;
		}
		TypeBinding type = ((VariableBinding) binding).type;
		int index = indexOfFirstFieldBinding;
		if (index == length) { //	restrictiveFlag == FIELD
			this.constant = FieldReference.getConstantFor((FieldBinding) binding, this, false, scope);
			// perform capture conversion if read access
			return (type != null && (this.bits & IsStrictlyAssignedMASK) == 0)
					? type.capture(scope, this.sourceEnd)
					: type;
		}
		// allocation of the fieldBindings array	and its respective constants
		int otherBindingsLength = length - index;
		otherCodegenBindings = otherBindings = new FieldBinding[otherBindingsLength];
		otherDepths = new int[otherBindingsLength];
		
		// fill the first constant (the one of the binding)
		this.constant = field != null
				? FieldReference.getConstantFor((FieldBinding) binding, this, false, scope)
				: ((VariableBinding) binding).constant();
		// save first depth, since will be updated by visibility checks of other bindings
		int firstDepth = (bits & DepthMASK) >> DepthSHIFT;
		// iteration on each field	
		while (index < length) {
			char[] token = tokens[index];
			if (type == null)
				return null; // could not resolve type prior to this point

			bits &= ~DepthMASK; // flush previous depth if any		
			FieldBinding previousField = field;
			field = scope.getField(type.capture(scope, (int)this.sourcePositions[index]), token, this);
			int place = index - indexOfFirstFieldBinding;
			otherBindings[place] = field;
			otherDepths[place] = (bits & DepthMASK) >> DepthSHIFT;
			if (field.isValidBinding()) {
				// set generic cast of for previous field (if any)
				if (previousField != null) {
					TypeBinding fieldReceiverType = type;
					TypeBinding receiverErasure = type.erasure();
					if (receiverErasure instanceof ReferenceBinding) {
						ReferenceBinding match = ((ReferenceBinding)receiverErasure).findSuperTypeWithSameErasure(field.declaringClass);
						if (match == null) {
							fieldReceiverType = field.declaringClass; // handle indirect inheritance thru variable secondary bound
						}
					}				
					FieldBinding originalBinding = previousField.original();
				    if ((originalBinding.type.tagBits & TagBits.HasTypeVariable) != 0 && fieldReceiverType.id != T_JavaLangObject) {
				    	setGenericCast(index-1,originalBinding.type.genericCast(fieldReceiverType)); // type cannot be base-type even in boxing case
				    }
			    }
				// only last field is actually a write access if any
				if (isFieldUseDeprecated(field, scope, (this.bits & IsStrictlyAssignedMASK) !=0 && index+1 == length)) {
					scope.problemReporter().deprecatedField(field, this);
				}
				Constant someConstant = FieldReference.getConstantFor(field, this, false, scope);
				// constant propagation can only be performed as long as the previous one is a constant too.
				if (this.constant != NotAConstant) {
					this.constant = someConstant;					
				}

				if (field.isStatic()) {
					// static field accessed through receiver? legal but unoptimal (optional warning)
					scope.problemReporter().nonStaticAccessToStaticField(this, field);
					// indirect static reference ?
					if (field.declaringClass != type) {
						scope.problemReporter().indirectAccessToStaticField(this, field);
					}
				}
				type = field.type;
				index++;
			} else {
				constant = NotAConstant; //don't fill other constants slots...
				scope.problemReporter().invalidField(this, field, index, type);
				setDepth(firstDepth);
				return null;
			}
		}
		setDepth(firstDepth);
		type = (otherBindings[otherBindingsLength - 1]).type;
		// perform capture conversion if read access
		return (type != null && (this.bits & IsStrictlyAssignedMASK) == 0)
				? type.capture(scope, this.sourceEnd)
				: type;		
	}
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		if (!flowInfo.isReachable()) return;
		//If inlinable field, forget the access emulation, the code gen will directly target it
		if (((bits & DepthMASK) == 0) || (constant != NotAConstant)) {
			return;
		}
		if ((bits & RestrictiveFlagMASK) == Binding.LOCAL) {
			currentScope.emulateOuterAccess((LocalVariableBinding) binding);
		}
	}
	/**
	 * index is <0 to denote write access emulation
	 */
	public void manageSyntheticAccessIfNecessary(
			BlockScope currentScope,
			FieldBinding fieldBinding,
			TypeBinding lastReceiverType,
			int index,
			FlowInfo flowInfo) {
	    
		if (!flowInfo.isReachable()) return;
		// index == 0 denotes the first fieldBinding, index > 0 denotes one of the 'otherBindings', index < 0 denotes a write access (to last binding)
		if (fieldBinding.isConstantValue())
			return;

		// if field from parameterized type got found, use the original field at codegen time
		FieldBinding originalField = fieldBinding.original();
		if (originalField != fieldBinding) {
			setCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index, originalField);
		}
		
		if (fieldBinding.isPrivate()) { // private access
		    FieldBinding someCodegenBinding = getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index);
			if (someCodegenBinding.declaringClass != currentScope.enclosingSourceType()) {
			    setSyntheticAccessor(fieldBinding, index, 
			            ((SourceTypeBinding) someCodegenBinding.declaringClass).addSyntheticMethod(someCodegenBinding, index >= 0 /*read-access?*/));
				currentScope.problemReporter().needToEmulateFieldAccess(someCodegenBinding, this, index >= 0 /*read-access?*/);
				return;
			}
		} else if (fieldBinding.isProtected()){
		    int depth = fieldBinding == binding 
		    		? (bits & DepthMASK) >> DepthSHIFT 
		    		 : otherDepths[index < 0 ? otherDepths.length-1 : index-1];
			
			// implicit protected access 
			if (depth > 0 && (fieldBinding.declaringClass.getPackage() != currentScope.enclosingSourceType().getPackage())) {
			    FieldBinding someCodegenBinding = getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index);
			    setSyntheticAccessor(fieldBinding, index, 
			            ((SourceTypeBinding) currentScope.enclosingSourceType().enclosingTypeAt(depth)).addSyntheticMethod(someCodegenBinding, index >= 0 /*read-access?*/));
				currentScope.problemReporter().needToEmulateFieldAccess(someCodegenBinding, this, index >= 0 /*read-access?*/);
				return;
			}
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
		// and not from Object or implicit static field access.	
		if (fieldBinding.declaringClass != lastReceiverType
				&& !lastReceiverType.isArrayType()
				&& fieldBinding.declaringClass != null // array.length
				&& !fieldBinding.isConstantValue()) {
			CompilerOptions options = currentScope.compilerOptions();
			if ((options.targetJDK >= ClassFileConstants.JDK1_2
					&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || fieldBinding != binding || indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_JavaLangObject) // no change for Object fields
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope)) {
	
		    setCodegenBinding(
		            index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index, 
		            currentScope.enclosingSourceType().getUpdatedFieldBinding(
		                    getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index), 
		                    (ReferenceBinding)lastReceiverType.erasure()));
			}
		}			
	}

	public Constant optimizedBooleanConstant() {

		switch (this.resolvedType.id) {
			case T_boolean :
			case T_JavaLangBoolean :
				if (this.constant != NotAConstant) return this.constant;
				switch (bits & RestrictiveFlagMASK) {
					case Binding.FIELD : // reading a field
						if (this.otherBindings == null)
							return ((FieldBinding)this.binding).constant();
						// fall thru
					case Binding.LOCAL : // reading a local variable
						return this.otherBindings[this.otherBindings.length-1].constant();
				}
		}
		return NotAConstant;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(tokens[i]);
		}
		return output;
	}
		
	/**
	 * Normal field binding did not work, try to bind to a field of the delegate receiver.
	 */
	public TypeBinding reportError(BlockScope scope) {
		if (binding instanceof ProblemFieldBinding) {
			scope.problemReporter().invalidField(this, (FieldBinding) binding);
		} else if (binding instanceof ProblemReferenceBinding) {
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else {
			scope.problemReporter().unresolvableReference(this, binding);
		}
		return null;
	}
	public TypeBinding resolveType(BlockScope scope) {
		// field and/or local are done before type lookups
		// the only available value for the restrictiveFlag BEFORE
		// the TC is Flag_Type Flag_LocalField and Flag_TypeLocalField 
		this.actualReceiverType = scope.enclosingSourceType();
		constant = Constant.NotAConstant;
		if ((this.codegenBinding = this.binding = scope.getBinding(tokens, bits & RestrictiveFlagMASK, this, true /*resolve*/)).isValidBinding()) {
			switch (bits & RestrictiveFlagMASK) {
				case Binding.VARIABLE : //============only variable===========
				case Binding.TYPE | Binding.VARIABLE :
					if (binding instanceof LocalVariableBinding) {
						if (!((LocalVariableBinding) binding).isFinal() && ((bits & DepthMASK) != 0))
							scope.problemReporter().cannotReferToNonFinalOuterLocal(
								(LocalVariableBinding) binding,
								this);
						bits &= ~RestrictiveFlagMASK; // clear bits
						bits |= Binding.LOCAL;
						return this.resolvedType = getOtherFieldBindings(scope);
					}
					if (binding instanceof FieldBinding) {
						FieldBinding fieldBinding = (FieldBinding) binding;
						MethodScope methodScope = scope.methodScope();
						ReferenceBinding declaringClass = fieldBinding.declaringClass;
						// check for forward references
						if (this.indexOfFirstFieldBinding == 1
								&& methodScope.enclosingSourceType() == declaringClass
								&& methodScope.lastVisibleFieldID >= 0
								&& fieldBinding.id >= methodScope.lastVisibleFieldID
								&& (!fieldBinding.isStatic() || methodScope.isStatic)) {
							scope.problemReporter().forwardReference(this, 0, methodScope.enclosingSourceType());
						}
						if (!fieldBinding.isStatic() 
								&& this.indexOfFirstFieldBinding == 1
								&& scope.compilerOptions().getSeverity(CompilerOptions.UnqualifiedFieldAccess) != ProblemSeverities.Ignore) {
							scope.problemReporter().unqualifiedFieldAccess(this, fieldBinding);
						}
						bits &= ~RestrictiveFlagMASK; // clear bits
						bits |= Binding.FIELD;
						
						// check for deprecated receiver type
						// deprecation check for receiver type if not first token
						if (indexOfFirstFieldBinding > 1) {
							if (isTypeUseDeprecated(this.actualReceiverType, scope))
								scope.problemReporter().deprecatedType(this.actualReceiverType, this);
						}
						
						return this.resolvedType = getOtherFieldBindings(scope);
					}
					// thus it was a type
					bits &= ~RestrictiveFlagMASK; // clear bits
					bits |= Binding.TYPE;
				case Binding.TYPE : //=============only type ==============
				    TypeBinding type = (TypeBinding) binding;
					if (isTypeUseDeprecated(type, scope))
						scope.problemReporter().deprecatedType(type, this);
					type = scope.environment().convertToRawType(type);
					return this.resolvedType = type;
			}
		}
		//========error cases===============
		return this.resolvedType = this.reportError(scope);
	}

	// set the matching codegenBinding and generic cast
	protected void setCodegenBinding(int index, FieldBinding someCodegenBinding) {

		if (index == 0){
			this.codegenBinding = someCodegenBinding;
		} else {
		    int length = this.otherBindings.length;
			if (this.otherCodegenBindings == this.otherBindings){
				System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[length], 0, length);
			}
			this.otherCodegenBindings[index-1] = someCodegenBinding;
		}	    
	}

	// set the matching codegenBinding and generic cast
	protected void setGenericCast(int index, TypeBinding someGenericCast) {

		if (index == 0){
			this.genericCast = someGenericCast;
		} else {
		    if (this.otherGenericCasts == null) {
		        this.otherGenericCasts = new TypeBinding[this.otherBindings.length];
		    }
		    this.otherGenericCasts[index-1] = someGenericCast;
		}	    
	}
	
	// set the matching synthetic accessor
	protected void setSyntheticAccessor(FieldBinding fieldBinding, int index, SyntheticMethodBinding syntheticAccessor) {
		if (index < 0) { // write-access ?
			syntheticWriteAccessor = syntheticAccessor;
	    } else {
			if (syntheticReadAccessors == null) {
				syntheticReadAccessors = new SyntheticMethodBinding[otherBindings == null ? 1 : otherBindings.length + 1];
			}
			syntheticReadAccessors[index] = syntheticAccessor;
	    }
	}
	
	public void setFieldIndex(int index) {
		this.indexOfFirstFieldBinding = index;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public String unboundReferenceErrorName() {
		return new String(tokens[0]);
	}
}
