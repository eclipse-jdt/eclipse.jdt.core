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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class SingleNameReference extends NameReference implements OperatorIds {
    
	public char[] token;
	public MethodBinding[] syntheticAccessors; // [0]=read accessor [1]=write accessor
	public static final int READ = 0;
	public static final int WRITE = 1;
	public TypeBinding genericCast;
	
	public SingleNameReference(char[] source, long pos) {
		super();
		token = source;
		sourceStart = (int) (pos >>> 32);
		sourceEnd = (int) pos;
	}
	public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
	
		boolean isReachable = flowInfo.isReachable();
		// compound assignment extra work
		if (isCompound) { // check the variable part is initialized if blank final
			switch (bits & RestrictiveFlagMASK) {
				case FIELD : // reading a field
					FieldBinding fieldBinding;
					if ((fieldBinding = (FieldBinding) binding).isBlankFinal() 
							&& currentScope.allowBlankFinalFieldAssignment(fieldBinding)) {
						if (!flowInfo.isDefinitelyAssigned(fieldBinding)) {
							currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
						}
					}
					manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
					break;
				case LOCAL : // reading a local variable
					// check if assigning a final blank field
					LocalVariableBinding localBinding;
					if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
						currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
						// we could improve error msg here telling "cannot use compound assignment on final local variable"
					}
					if (isReachable) {
						localBinding.useFlag = LocalVariableBinding.USED;
					} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
						localBinding.useFlag = LocalVariableBinding.FAKE_USED;
					}
			}
		}
		if (assignment.expression != null) {
			flowInfo = assignment.expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // assigning to a field
				manageSyntheticAccessIfNecessary(currentScope, flowInfo, false /*write-access*/);
	
				// check if assigning a final field
				FieldBinding fieldBinding;
				if ((fieldBinding = (FieldBinding) binding).isFinal()) {
					// inside a context where allowed
					if (!isCompound && fieldBinding.isBlankFinal() && currentScope.allowBlankFinalFieldAssignment(fieldBinding)) {
						if (flowInfo.isPotentiallyAssigned(fieldBinding)) {
							currentScope.problemReporter().duplicateInitializationOfBlankFinalField(fieldBinding, this);
						} else {
							flowContext.recordSettingFinal(fieldBinding, this, flowInfo);						
						}
						flowInfo.markAsDefinitelyAssigned(fieldBinding);
					} else {
						currentScope.problemReporter().cannotAssignToFinalField(fieldBinding, this);
					}
				}
				break;
			case LOCAL : // assigning to a local variable 
				LocalVariableBinding localBinding = (LocalVariableBinding) binding;
				if (!flowInfo.isDefinitelyAssigned(localBinding)){// for local variable debug attributes
					bits |= FirstAssignmentToLocalMASK;
				} else {
					bits &= ~FirstAssignmentToLocalMASK;
				}
				if (localBinding.isFinal()) {
					if ((bits & DepthMASK) == 0) {
						// tolerate assignment to final local in unreachable code (45674)
						if ((isReachable && isCompound) || !localBinding.isBlankFinal()){
							currentScope.problemReporter().cannotAssignToFinalLocal(localBinding, this);
						} else if (flowInfo.isPotentiallyAssigned(localBinding)) {
							currentScope.problemReporter().duplicateInitializationOfFinalLocal(localBinding, this);
						} else {
							flowContext.recordSettingFinal(localBinding, this, flowInfo);								
						}
					} else {
						currentScope.problemReporter().cannotAssignToFinalOuterLocal(localBinding, this);
					}
				}
				flowInfo.markAsDefinitelyAssigned(localBinding);
		}
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		return flowInfo;
	}
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return analyseCode(currentScope, flowContext, flowInfo, true);
	}
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // reading a field
				if (valueRequired) {
					manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
				}
				// check if reading a final blank field
				FieldBinding fieldBinding;
				if ((fieldBinding = (FieldBinding) binding).isBlankFinal() 
						&& currentScope.allowBlankFinalFieldAssignment(fieldBinding)) {
					if (!flowInfo.isDefinitelyAssigned(fieldBinding)) {
						currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
					}
				}
				break;
			case LOCAL : // reading a local variable
				LocalVariableBinding localBinding;
				if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
					currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
				}
				if (flowInfo.isReachable()) {
					localBinding.useFlag = LocalVariableBinding.USED;
				} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
					localBinding.useFlag = LocalVariableBinding.FAKE_USED;
				}
		}
		if (valueRequired) {
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		}
		return flowInfo;
	}
	public TypeBinding checkFieldAccess(BlockScope scope) {
	
		FieldBinding fieldBinding = (FieldBinding) binding;
		
		bits &= ~RestrictiveFlagMASK; // clear bits
		bits |= FIELD;
		if (!((FieldBinding) binding).isStatic()) {
			// must check for the static status....
			if (scope.methodScope().isStatic) {
				scope.problemReporter().staticFieldAccessToNonStaticVariable(this, fieldBinding);
				constant = NotAConstant;
				return fieldBinding.type;
			}
		}
		constant = FieldReference.getConstantFor(fieldBinding, this, true, scope);
	
		if (isFieldUseDeprecated(fieldBinding, scope, (this.bits & IsStrictlyAssignedMASK) !=0))
			scope.problemReporter().deprecatedField(fieldBinding, this);
	
		MethodScope ms = scope.methodScope();
		if ((this.bits & IsStrictlyAssignedMASK) == 0
			&& ms.enclosingSourceType() == fieldBinding.declaringClass
			&& ms.lastVisibleFieldID >= 0
			&& fieldBinding.id >= ms.lastVisibleFieldID) {
			//if the field is static and ms is not .... then it is valid
			if (!fieldBinding.isStatic() || ms.isStatic)
				scope.problemReporter().forwardReference(this, 0, scope.enclosingSourceType());
		}
		//====================================================
	
		return fieldBinding.type;
	
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
	 */
	public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
		if (runtimeTimeType == null || compileTimeType == null)
			return;				
		if ((bits & FIELD) != 0 && this.binding != null && this.binding.isValidBinding()) {
			// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
			FieldBinding originalBinding = ((FieldBinding)this.binding).original();
			if (originalBinding != this.binding) {
			    // extra cast needed if method return type has type variable
			    if ((originalBinding.type.tagBits & TagBits.HasTypeVariable) != 0 && runtimeTimeType.id != T_Object) {
			        this.genericCast = originalBinding.type.genericCast(runtimeTimeType);
			    }
			} 	
		}
		super.computeConversion(scope, runtimeTimeType, compileTimeType);
}	

	public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	
		// optimizing assignment like: i = i + 1 or i = 1 + i
		if (assignment.expression.isCompactableOperation()) {
			BinaryExpression operation = (BinaryExpression) assignment.expression;
			SingleNameReference variableReference;
			if ((operation.left instanceof SingleNameReference) && ((variableReference = (SingleNameReference) operation.left).binding == binding)) {
				// i = i + value, then use the variable on the right hand side, since it has the correct implicit conversion
				variableReference.generateCompoundAssignment(currentScope, codeStream, syntheticAccessors == null ? null : syntheticAccessors[WRITE], operation.right, (operation.bits & OperatorMASK) >> OperatorSHIFT, operation.left.implicitConversion /*should be equivalent to no conversion*/, valueRequired);
				return;
			}
			int operator = (operation.bits & OperatorMASK) >> OperatorSHIFT;
			if ((operation.right instanceof SingleNameReference)
				&& ((operator == PLUS) || (operator == MULTIPLY)) // only commutative operations
				&& ((variableReference = (SingleNameReference) operation.right).binding == binding)
				&& (operation.left.constant != NotAConstant) // exclude non constant expressions, since could have side-effect
				&& ((operation.left.implicitConversion >> 4) != T_String) // exclude string concatenation which would occur backwards
				&& ((operation.right.implicitConversion >> 4) != T_String)) { // exclude string concatenation which would occur backwards
				// i = value + i, then use the variable on the right hand side, since it has the correct implicit conversion
				variableReference.generateCompoundAssignment(currentScope, codeStream, syntheticAccessors == null ? null : syntheticAccessors[WRITE], operation.left, operator, operation.right.implicitConversion /*should be equivalent to no conversion*/, valueRequired);
				return;
			}
		}
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // assigning to a field
				FieldBinding fieldBinding;
				if (!(fieldBinding = (FieldBinding) this.codegenBinding).isStatic()) { // need a receiver?
					if ((bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						this.generateReceiver(codeStream);
					}
				}
				assignment.expression.generateCode(currentScope, codeStream, true);
				fieldStore(codeStream, fieldBinding, syntheticAccessors == null ? null : syntheticAccessors[WRITE], valueRequired);
				if (valueRequired) {
					codeStream.generateImplicitConversion(assignment.implicitConversion);
				}
				// no need for generic cast as value got dupped
				return;
			case LOCAL : // assigning to a local variable
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				if (localBinding.resolvedPosition != -1) {
					assignment.expression.generateCode(currentScope, codeStream, true);
				} else {
					if (assignment.expression.constant != NotAConstant) {
						// assigning an unused local to a constant value = no actual assignment is necessary
						if (valueRequired) {
							codeStream.generateConstant(assignment.expression.constant, assignment.implicitConversion);
						}
					} else {
						assignment.expression.generateCode(currentScope, codeStream, true);
						/* Even though the value may not be required, we force it to be produced, and discard it later
						on if it was actually not necessary, so as to provide the same behavior as JDK1.2beta3.	*/
						if (valueRequired) {
							codeStream.generateImplicitConversion(assignment.implicitConversion); // implicit conversion
						} else {
							if ((localBinding.type == LongBinding) || (localBinding.type == DoubleBinding)) {
								codeStream.pop2();
							} else {
								codeStream.pop();
							}
						}
					}
					return;
				}
				// 26903, need extra cast to store null in array local var	
				if (localBinding.type.isArrayType() 
					&& (assignment.expression.resolvedType == NullBinding	// arrayLoc = null
						|| ((assignment.expression instanceof CastExpression)	// arrayLoc = (type[])null
							&& (((CastExpression)assignment.expression).innermostCastedExpression().resolvedType == NullBinding)))){
					codeStream.checkcast(localBinding.type); 
				}
				
				// normal local assignment (since cannot store in outer local which are final locations)
				codeStream.store(localBinding, valueRequired);
				if ((bits & FirstAssignmentToLocalMASK) != 0) { // for local variable debug attributes
					localBinding.recordInitializationStartPC(codeStream.position);
				}
				// implicit conversion
				if (valueRequired) {
					codeStream.generateImplicitConversion(assignment.implicitConversion);
				}
		}
	}
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (constant != NotAConstant) {
			if (valueRequired) {
				codeStream.generateConstant(constant, implicitConversion);
			}
		} else {
			switch (bits & RestrictiveFlagMASK) {
				case FIELD : // reading a field
					FieldBinding fieldBinding;
					if (valueRequired) {
						if (!(fieldBinding = (FieldBinding) this.codegenBinding).isConstantValue()) { // directly use inlined value for constant fields
							boolean isStatic;
							if (!(isStatic = fieldBinding.isStatic())) {
								if ((bits & DepthMASK) != 0) {
									ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
									Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
									codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
								} else {
									generateReceiver(codeStream);
								}
							}
							// managing private access							
							if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
								if (isStatic) {
									codeStream.getstatic(fieldBinding);
								} else {
									codeStream.getfield(fieldBinding);
								}
							} else {
								codeStream.invokestatic(syntheticAccessors[READ]);
							}
							codeStream.generateImplicitConversion(implicitConversion);
							if (this.genericCast != null) codeStream.checkcast(this.genericCast);
					} else { // directly use the inlined value
							codeStream.generateConstant(fieldBinding.constant(), implicitConversion);
						}
					}
					break;
				case LOCAL : // reading a local
					LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
					if (valueRequired) {
						// outer local?
						if ((bits & DepthMASK) != 0) {
							// outer local can be reached either through a synthetic arg or a synthetic field
							VariableBinding[] path = currentScope.getEmulationPath(localBinding);
							codeStream.generateOuterAccess(path, this, localBinding, currentScope);
						} else {
							// regular local variable read
							codeStream.load(localBinding);
						}
						codeStream.generateImplicitConversion(implicitConversion);
					}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	/*
	 * Regular API for compound assignment, relies on the fact that there is only one reference to the
	 * variable, which carries both synthetic read/write accessors.
	 * The APIs with an extra argument is used whenever there are two references to the same variable which
	 * are optimized in one access: e.g "a = a + 1" optimized into "a++".
	 */
	public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	
		this.generateCompoundAssignment(
			currentScope, 
			codeStream, 
			syntheticAccessors == null ? null : syntheticAccessors[WRITE],
			expression,
			operator, 
			assignmentImplicitConversion, 
			valueRequired);
	}
	/*
	 * The APIs with an extra argument is used whenever there are two references to the same variable which
	 * are optimized in one access: e.g "a = a + 1" optimized into "a++".
	 */
	public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, MethodBinding writeAccessor, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // assigning to a field
				FieldBinding fieldBinding;
				if ((fieldBinding = (FieldBinding) this.codegenBinding).isStatic()) {
					if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
						codeStream.getstatic(fieldBinding);
					} else {
						codeStream.invokestatic(syntheticAccessors[READ]);
					}
				} else {
					if ((bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						codeStream.aload_0();
					}
					codeStream.dup();
					if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
						codeStream.getfield(fieldBinding);
					} else {
						codeStream.invokestatic(syntheticAccessors[READ]);
					}
				}
				break;
			case LOCAL : // assigning to a local variable (cannot assign to outer local)
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				Constant assignConstant;
				int increment;
				// using incr bytecode if possible
				switch (localBinding.type.id) {
					case T_String :
						codeStream.generateStringConcatenationAppend(currentScope, this, expression);
						if (valueRequired) {
							codeStream.dup();
						}
						codeStream.store(localBinding, false);
						return;
					case T_int :
						if (((assignConstant = expression.constant) != NotAConstant) 
							&& (assignConstant.typeID() != T_float) // only for integral types
							&& (assignConstant.typeID() != T_double)
							&& ((increment = assignConstant.intValue()) == (short) increment)) { // 16 bits value
							switch (operator) {
								case PLUS :
									codeStream.iinc(localBinding.resolvedPosition, increment);
									if (valueRequired) {
										codeStream.load(localBinding);
									}
									return;
								case MINUS :
									codeStream.iinc(localBinding.resolvedPosition, -increment);
									if (valueRequired) {
										codeStream.load(localBinding);
									}
									return;
							}
						}
					default :
						codeStream.load(localBinding);
				}
		}
		// perform the actual compound operation
		int operationTypeID;
		if ((operationTypeID = implicitConversion >> 4) == T_String || operationTypeID == T_Object) {
			// we enter here if the single name reference is a field of type java.lang.String or if the type of the 
			// operation is java.lang.Object
			// For example: o = o + ""; // where the compiled type of o is java.lang.Object.
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
			// no need for generic cast on previous #getfield since using Object string buffer methods.			
		} else {
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One){ // prefix operation
				codeStream.generateConstant(expression.constant, implicitConversion);			
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}		
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		// store the result back into the variable
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // assigning to a field
				fieldStore(codeStream, (FieldBinding) this.codegenBinding, writeAccessor, valueRequired);
				// no need for generic cast as value got dupped
				return;
			case LOCAL : // assigning to a local variable
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				if (valueRequired) {
					if ((localBinding.type == LongBinding) || (localBinding.type == DoubleBinding)) {
						codeStream.dup2();
					} else {
						codeStream.dup();
					}
				}
				codeStream.store(localBinding, false);
		}
	}
	
	public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // assigning to a field
				FieldBinding fieldBinding;
				if ((fieldBinding = (FieldBinding) this.codegenBinding).isStatic()) {
					if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
						codeStream.getstatic(fieldBinding);
					} else {
						codeStream.invokestatic(syntheticAccessors[READ]);
					}
				} else {
					if ((bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						codeStream.aload_0();
					}
					codeStream.dup();
					if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
						codeStream.getfield(fieldBinding);
					} else {
						codeStream.invokestatic(syntheticAccessors[READ]);
					}
				}
				if (valueRequired) {
					if (fieldBinding.isStatic()) {
						if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
							codeStream.dup2();
						} else {
							codeStream.dup();
						}
					} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
						if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
							codeStream.dup2_x1();
						} else {
							codeStream.dup_x1();
						}
					}
				}
				codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
				codeStream.sendOperator(postIncrement.operator, fieldBinding.type.id);
				codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
				fieldStore(codeStream, fieldBinding, syntheticAccessors == null ? null : syntheticAccessors[WRITE], false);
				// no need for generic cast 
				return;
			case LOCAL : // assigning to a local variable
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				// using incr bytecode if possible
				if (localBinding.type == IntBinding) {
					if (valueRequired) {
						codeStream.load(localBinding);
					}
					if (postIncrement.operator == PLUS) {
						codeStream.iinc(localBinding.resolvedPosition, 1);
					} else {
						codeStream.iinc(localBinding.resolvedPosition, -1);
					}
				} else {
					codeStream.load(localBinding);
					if (valueRequired){
						if ((localBinding.type == LongBinding) || (localBinding.type == DoubleBinding)) {
							codeStream.dup2();
						} else {
							codeStream.dup();
						}
					}
					codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
					codeStream.sendOperator(postIncrement.operator, localBinding.type.id);
					codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
	
					codeStream.store(localBinding, false);
				}
		}
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
	
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	
		if (!flowInfo.isReachable()) return;
		//If inlinable field, forget the access emulation, the code gen will directly target it
		if (((bits & DepthMASK) == 0) || (constant != NotAConstant)) return;
	
		if ((bits & RestrictiveFlagMASK) == LOCAL) {
			currentScope.emulateOuterAccess((LocalVariableBinding) binding);
		}
	}
	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {
	
		if (!flowInfo.isReachable()) return;
	
		//If inlinable field, forget the access emulation, the code gen will directly target it
		if (constant != NotAConstant)
			return;
	
		if ((bits & FIELD) != 0) {
			FieldBinding fieldBinding = (FieldBinding) binding;
			FieldBinding codegenField = fieldBinding.original();
			this.codegenBinding = codegenField;
			if (((bits & DepthMASK) != 0)
				&& (codegenField.isPrivate() // private access
					|| (codegenField.isProtected() // implicit protected access
							&& codegenField.declaringClass.getPackage() != currentScope.enclosingSourceType().getPackage()))) {
				if (syntheticAccessors == null)
					syntheticAccessors = new MethodBinding[2];
				syntheticAccessors[isReadAccess ? READ : WRITE] = 
				    ((SourceTypeBinding)currentScope.enclosingSourceType().
						enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT)).addSyntheticMethod(codegenField, isReadAccess);
				currentScope.problemReporter().needToEmulateFieldAccess(codegenField, this, isReadAccess);
				return;
			}
			// if the binding declaring class is not visible, need special action
			// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
			// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
			// and not from Object or implicit static field access.	
			if (fieldBinding.declaringClass != this.actualReceiverType
				&& !this.actualReceiverType.isArrayType()	
				&& fieldBinding.declaringClass != null
				&& !fieldBinding.isConstantValue()
				&& ((currentScope.environment().options.targetJDK >= ClassFileConstants.JDK1_2 
						&& !fieldBinding.isStatic()
						&& fieldBinding.declaringClass.id != T_Object) // no change for Object fields (if there was any)
					|| !codegenField.declaringClass.canBeSeenBy(currentScope))){
				this.codegenBinding = 
				    currentScope.enclosingSourceType().getUpdatedFieldBinding(
					       codegenField, 
					        (ReferenceBinding)this.actualReceiverType.erasure());
			}
		}
	}
	public StringBuffer printExpression(int indent, StringBuffer output){
	
		return output.append(token);
	}
	
	public TypeBinding reportError(BlockScope scope) {
		
		//=====error cases=======
		constant = Constant.NotAConstant;
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
		// for code gen, harm the restrictiveFlag 	
	
		this.actualReceiverType = this.receiverType = scope.enclosingSourceType();
		
		if ((this.codegenBinding = this.binding = scope.getBinding(token, bits & RestrictiveFlagMASK, this, true /*resolve*/)).isValidBinding()) {
			switch (bits & RestrictiveFlagMASK) {
				case VARIABLE : // =========only variable============
				case VARIABLE | TYPE : //====both variable and type============
					if (binding instanceof VariableBinding) {
						VariableBinding variable = (VariableBinding) binding;
						if (binding instanceof LocalVariableBinding) {
							bits &= ~RestrictiveFlagMASK;  // clear bits
							bits |= LOCAL;
							if ((this.bits & IsStrictlyAssignedMASK) == 0) {
								constant = variable.constant();
							} else {
								constant = NotAConstant;
							}
							if (!variable.isFinal() && (bits & DepthMASK) != 0) {
								scope.problemReporter().cannotReferToNonFinalOuterLocal((LocalVariableBinding)variable, this);
							}
							return this.resolvedType = variable.type;
						}
						// a field
						FieldBinding field = (FieldBinding) this.binding;
						if (!field.isStatic() && scope.environment().options.getSeverity(CompilerOptions.UnqualifiedFieldAccess) != ProblemSeverities.Ignore) {
							scope.problemReporter().unqualifiedFieldAccess(this, field);
						}
						return this.resolvedType = checkFieldAccess(scope);
					}
	
					// thus it was a type
					bits &= ~RestrictiveFlagMASK;  // clear bits
					bits |= TYPE;
				case TYPE : //========only type==============
					constant = Constant.NotAConstant;
					//deprecated test
					TypeBinding type = (TypeBinding)binding;
					if (isTypeUseDeprecated(type, scope))
						scope.problemReporter().deprecatedType(type, this);
					return this.resolvedType = scope.convertToRawType(type);
			}
		}
	
		// error scenarii
		return this.resolvedType = this.reportError(scope);
	}
		
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	
	public String unboundReferenceErrorName(){
	
		return new String(token);
	}
}
