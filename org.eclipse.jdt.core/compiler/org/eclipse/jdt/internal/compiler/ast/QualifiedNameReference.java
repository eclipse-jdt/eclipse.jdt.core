/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedNameReference extends NameReference {
	
	public char[][] tokens;
	public FieldBinding[] otherBindings, otherCodegenBindings;
	int[] otherDepths;
	public int indexOfFirstFieldBinding;//points (into tokens) for the first token that corresponds to first FieldBinding
	SyntheticAccessMethodBinding syntheticWriteAccessor;
	SyntheticAccessMethodBinding[] syntheticReadAccessors;
	protected FieldBinding lastFieldBinding;
	public QualifiedNameReference(
		char[][] sources,
		int sourceStart,
		int sourceEnd) {
		super();
		tokens = sources;
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
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // reading a field
				lastFieldBinding = (FieldBinding) binding;
				if (needValue) {
					manageSyntheticReadAccessIfNecessary(currentScope, lastFieldBinding, this.actualReceiverType, 0);
				}				// check if final blank field
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
			case LOCAL :
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
		}
		
		if (needValue) {
			manageEnclosingInstanceAccessIfNecessary(currentScope);
			// only for first binding
		}
		// all intermediate field accesses are read accesses
		if (otherBindings != null) {
			for (int i = 0; i < otherBindingsCount-1; i++) {
				lastFieldBinding = otherBindings[i];
				needValue = !otherBindings[i+1].isStatic();
				if (needValue) {
					manageSyntheticReadAccessIfNecessary(
						currentScope, 
						lastFieldBinding, 
						i == 0 
							? ((VariableBinding)binding).type
							: otherBindings[i-1].type,
						i + 1);
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
			manageSyntheticReadAccessIfNecessary(
				currentScope,
				lastFieldBinding,
				lastReceiverType,
				lastFieldBinding == binding
					? 0 
					: otherBindingsCount);
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
					flowContext.recordSettingFinal(lastFieldBinding, this);
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
		manageSyntheticWriteAccessIfNecessary(currentScope, lastFieldBinding, lastReceiverType);

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
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // reading a field
				if (needValue) {
					manageSyntheticReadAccessIfNecessary(currentScope, (FieldBinding) binding, this.actualReceiverType, 0);
				}
				// check if reading a final blank field
				FieldBinding fieldBinding;
					if ((fieldBinding = (FieldBinding) binding).isBlankFinal()
						&& (indexOfFirstFieldBinding == 1)
					// was an implicit reference to the first field binding
						&& currentScope.allowBlankFinalFieldAssignment(fieldBinding)
						&& (!flowInfo.isDefinitelyAssigned(fieldBinding))) {
					currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
				}
				break;
			case LOCAL : // reading a local variable
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
		}
		if (needValue) {
			manageEnclosingInstanceAccessIfNecessary(currentScope);
			// only for first binding
		}
		if (otherBindings != null) {
			for (int i = 0; i < otherBindingsCount; i++) {
				needValue = i < otherBindingsCount-1 ? !otherBindings[i+1].isStatic() : valueRequired;
				if (needValue) {
					manageSyntheticReadAccessIfNecessary(
						currentScope, 
						otherBindings[i], 
						i == 0 
							? ((VariableBinding)binding).type
							: otherBindings[i-1].type,
						i + 1);
				}
			}
		}
		return flowInfo;
	}
	/**
	 * Check and/or redirect the field access to the delegate receiver if any
	 */
	public TypeBinding checkFieldAccess(BlockScope scope) {
		// check for forward references
		FieldBinding fieldBinding = (FieldBinding) binding;
		MethodScope methodScope = scope.methodScope();
		if (methodScope.enclosingSourceType() == fieldBinding.declaringClass
			&& methodScope.fieldDeclarationIndex != MethodScope.NotInFieldDecl
			&& fieldBinding.id >= methodScope.fieldDeclarationIndex) {
			if ((!fieldBinding.isStatic() || methodScope.isStatic)
				&& this.indexOfFirstFieldBinding == 1)
				scope.problemReporter().forwardReference(this, 0, scope.enclosingSourceType());
		}
		bits &= ~RestrictiveFlagMASK; // clear bits
		bits |= FIELD;
		return getOtherFieldBindings(scope);
	}
	public void generateAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Assignment assignment,
		boolean valueRequired) {
			
		generateReadSequence(currentScope, codeStream);
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
			generateReadSequence(currentScope, codeStream); 
			if (valueRequired) {
				if (lastFieldBinding.declaringClass == null) { // array length
					codeStream.arraylength();
					codeStream.generateImplicitConversion(implicitConversion);
				} else {
					if (lastFieldBinding.constant != NotAConstant) {
						if (!lastFieldBinding.isStatic()){
							codeStream.invokeObjectGetClass();
							codeStream.pop();
						}
						// inline the last field constant
						codeStream.generateConstant(lastFieldBinding.constant, implicitConversion);
					} else {
						SyntheticAccessMethodBinding accessor =
							syntheticReadAccessors == null
								? null
								: syntheticReadAccessors[syntheticReadAccessors.length - 1];
						if (accessor == null) {
							if (lastFieldBinding.isStatic()) {
								codeStream.getstatic(lastFieldBinding);
							} else {
								codeStream.getfield(lastFieldBinding);
							}
						} else {
							codeStream.invokestatic(accessor);
						}
						codeStream.generateImplicitConversion(implicitConversion);
					}
				}
			} else {
				if (lastFieldBinding != null && !lastFieldBinding.isStatic()){
					codeStream.invokeObjectGetClass(); // perform null check
					codeStream.pop();
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
			
		generateReadSequence(currentScope, codeStream);
		SyntheticAccessMethodBinding accessor =
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
		if ((operationTypeID = implicitConversion >> 4) == T_String) {
			codeStream.generateStringAppend(currentScope, null, expression);
		} else {
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
		generateReadSequence(currentScope, codeStream);
		SyntheticAccessMethodBinding accessor =
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
		codeStream.generateConstant(
			postIncrement.expression.constant,
			implicitConversion);
		codeStream.sendOperator(postIncrement.operator, lastFieldBinding.type.id);
		codeStream.generateImplicitConversion(
			postIncrement.assignmentImplicitConversion);
		fieldStore(codeStream, lastFieldBinding, syntheticWriteAccessor, false);
	}
	/*
	 * Generate code for all bindings (local and fields) excluding the last one, which may then be generated code
	 * for a read or write access.
	 */
	public void generateReadSequence(
		BlockScope currentScope,
		CodeStream codeStream) {
			
		// determine the rank until which we now we do not need any actual value for the field access
		int otherBindingsCount = this.otherCodegenBindings == null ? 0 : otherCodegenBindings.length;

		boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
		switch (bits & RestrictiveFlagMASK) {
			case FIELD :
				lastFieldBinding = (FieldBinding) this.codegenBinding;
				// if first field is actually constant, we can inline it
				if (lastFieldBinding.constant != NotAConstant) {
					break;
				}
				if (needValue && !lastFieldBinding.isStatic()) {
					if ((bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						generateReceiver(codeStream);
					}
				}
				break;
			case LOCAL : // reading the first local variable
				if (!needValue) break; // no value needed
				lastFieldBinding = null;
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				// regular local variable read
				if (localBinding.constant != NotAConstant) {
					codeStream.generateConstant(localBinding.constant, 0);
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
				if (lastFieldBinding != null) {
					needValue = !nextField.isStatic();
					if (needValue) {
						MethodBinding accessor =
							syntheticReadAccessors == null ? null : syntheticReadAccessors[i]; 
						if (accessor == null) {
							if (lastFieldBinding.constant != NotAConstant) {
								if (this.lastFieldBinding != this.codegenBinding && !this.lastFieldBinding.isStatic()) {
									codeStream.invokeObjectGetClass(); // perform null check
									codeStream.pop();
								}
								codeStream.generateConstant(lastFieldBinding.constant, 0);
							} else if (lastFieldBinding.isStatic()) {
								codeStream.getstatic(lastFieldBinding);
							} else {
								codeStream.getfield(lastFieldBinding);
							}
						} else {
							codeStream.invokestatic(accessor);
						}
					} else {
						if (this.codegenBinding != this.lastFieldBinding && !this.lastFieldBinding.isStatic()){
							codeStream.invokeObjectGetClass(); // perform null check
							codeStream.pop();
						}						
					}
				}
				this.lastFieldBinding = nextField;
			}
		}
	}
	public void generateReceiver(CodeStream codeStream) {
		codeStream.aload_0();
	}
	public TypeBinding getOtherFieldBindings(BlockScope scope) {
		// At this point restrictiveFlag may ONLY have two potential value : FIELD LOCAL (i.e cast <<(VariableBinding) binding>> is valid)
		if ((bits & FIELD) != 0) {
			if (!((FieldBinding) binding).isStatic()) {
				//must check for the static status....
				if (indexOfFirstFieldBinding == 1) {
					//the field is the first token of the qualified reference....
					if (scope.methodScope().isStatic) {
						scope.problemReporter().staticFieldAccessToNonStaticVariable(
							this,
							(FieldBinding) binding);
						return null;
					}
				} else { //accessing to a field using a type as "receiver" is allowed only with static field	
					scope.problemReporter().staticFieldAccessToNonStaticVariable(
						this,
						(FieldBinding) binding);
					return null;
				}
			}
			if (isFieldUseDeprecated((FieldBinding) binding, scope))
				scope.problemReporter().deprecatedField((FieldBinding) binding, this);
		}
		TypeBinding type = ((VariableBinding) binding).type;
		int index = indexOfFirstFieldBinding;
		int length = tokens.length;
		if (index == length) { //	restrictiveFlag == FIELD
			this.constant = FieldReference.getConstantFor((FieldBinding) binding, this, false, scope);
			return type;
		}
		// allocation of the fieldBindings array	and its respective constants
		int otherBindingsLength = length - index;
		otherCodegenBindings = otherBindings = new FieldBinding[otherBindingsLength];
		otherDepths = new int[otherBindingsLength];
		
		// fill the first constant (the one of the binding)
		this.constant =
			((bits & FIELD) != 0)
				? FieldReference.getConstantFor((FieldBinding) binding, this, false, scope)
				: ((VariableBinding) binding).constant;
		// save first depth, since will be updated by visibility checks of other bindings
		int firstDepth = (bits & DepthMASK) >> DepthSHIFT;
		// iteration on each field	
		while (index < length) {
			char[] token = tokens[index];
			if (type == null)
				return null; // could not resolve type prior to this point

			bits &= ~DepthMASK; // flush previous depth if any			
			FieldBinding field = scope.getField(type, token, this);
			int place = index - indexOfFirstFieldBinding;
			otherBindings[place] = field;
			otherDepths[place] = (bits & DepthMASK) >> DepthSHIFT;
			if (field.isValidBinding()) {
				if (isFieldUseDeprecated(field, scope))
					scope.problemReporter().deprecatedField(field, this);
				Constant someConstant = FieldReference.getConstantFor(field, this, false, scope);
				// constant propagation can only be performed as long as the previous one is a constant too.
				if (this.constant != NotAConstant) {
					this.constant = someConstant;					
				}

				type = field.type;
				index++;
				
				if (field.isStatic()) {
					// static field accessed through receiver? legal but unoptimal (optional warning)
					scope.problemReporter().unnecessaryReceiverForStaticField(this, field);
				}
				
			} else {
				constant = NotAConstant; //don't fill other constants slots...
				scope.problemReporter().invalidField(this, field, index, type);
				setDepth(firstDepth);
				return null;
			}
		}
		setDepth(firstDepth);
		return (otherBindings[otherBindingsLength - 1]).type;
	}
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {
		//If inlinable field, forget the access emulation, the code gen will directly target it
		if (((bits & DepthMASK) == 0) || (constant != NotAConstant)) {
			return;
		}
		if ((bits & RestrictiveFlagMASK) == LOCAL) {
			currentScope.emulateOuterAccess((LocalVariableBinding) binding);
		}
	}
	public void manageSyntheticReadAccessIfNecessary(
		BlockScope currentScope,
		FieldBinding fieldBinding,
		TypeBinding lastReceiverType,
		int index) {
		// index == 0 denotes the first fieldBinding, index > 0 denotes one of the 'otherBindings'
		if (fieldBinding.constant != NotAConstant)
			return;
		if (fieldBinding.isPrivate()) { // private access
			if (fieldBinding.declaringClass != currentScope.enclosingSourceType()) {
				if (syntheticReadAccessors == null) {
					if (otherBindings == null)
						syntheticReadAccessors = new SyntheticAccessMethodBinding[1];
					else
						syntheticReadAccessors =
							new SyntheticAccessMethodBinding[otherBindings.length + 1];
				}
				syntheticReadAccessors[index] = ((SourceTypeBinding) fieldBinding.declaringClass).addSyntheticMethod(fieldBinding, true);
				currentScope.problemReporter().needToEmulateFieldReadAccess(fieldBinding, this);
				return;
			}
		} else if (fieldBinding.isProtected()){
			int depth = index == 0 ? (bits & DepthMASK) >> DepthSHIFT : otherDepths[index-1];
			// implicit protected access (only for first one)
			if (depth > 0 && (fieldBinding.declaringClass.getPackage()
								!= currentScope.enclosingSourceType().getPackage())) {
				if (syntheticReadAccessors == null) {
					if (otherBindings == null)
						syntheticReadAccessors = new SyntheticAccessMethodBinding[1];
					else
						syntheticReadAccessors =
							new SyntheticAccessMethodBinding[otherBindings.length + 1];
				}
				syntheticReadAccessors[index] =
					((SourceTypeBinding) currentScope.enclosingSourceType().enclosingTypeAt(depth))
											.addSyntheticMethod(fieldBinding, true);
				currentScope.problemReporter().needToEmulateFieldReadAccess(fieldBinding, this);
				return;
			}
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		if (fieldBinding.declaringClass != lastReceiverType
			&& !lastReceiverType.isArrayType()			
			&& fieldBinding.declaringClass != null
			&& fieldBinding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
					&& (index > 0 || indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_Object)
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope))){
			if (index == 0){
				this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			} else {
				if (this.otherCodegenBindings == this.otherBindings){
					int l = this.otherBindings.length;
					System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[l], 0, l);
				}
				this.otherCodegenBindings[index-1] = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			}
		}
	}
	/*
	 * No need to emulate access to protected fields since not implicitly accessed
	 */
	public void manageSyntheticWriteAccessIfNecessary(
		BlockScope currentScope,
		FieldBinding fieldBinding,
		TypeBinding lastReceiverType) {
		if (fieldBinding.isPrivate()) {
			if (fieldBinding.declaringClass != currentScope.enclosingSourceType()) {
				syntheticWriteAccessor = ((SourceTypeBinding) fieldBinding.declaringClass)
											.addSyntheticMethod(fieldBinding, false);
				currentScope.problemReporter().needToEmulateFieldWriteAccess(fieldBinding, this);
				return;
			}
		} else if (fieldBinding.isProtected()){
			int depth = fieldBinding == binding ? (bits & DepthMASK) >> DepthSHIFT : otherDepths[otherDepths.length-1];
			if (depth > 0 && (fieldBinding.declaringClass.getPackage()
								!= currentScope.enclosingSourceType().getPackage())) {
				syntheticWriteAccessor = ((SourceTypeBinding) currentScope.enclosingSourceType().enclosingTypeAt(depth))
											.addSyntheticMethod(fieldBinding, false);
				currentScope.problemReporter().needToEmulateFieldWriteAccess(fieldBinding, this);
				return;
			}
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		if (fieldBinding.declaringClass != lastReceiverType
			&& !lastReceiverType.isArrayType()			
			&& fieldBinding.declaringClass != null
			&& fieldBinding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
					&& (fieldBinding != binding || indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_Object)
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope))){
			if (fieldBinding == binding){
				this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			} else {
				if (this.otherCodegenBindings == this.otherBindings){
					int l = this.otherBindings.length;
					System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[l], 0, l);
				}
				this.otherCodegenBindings[this.otherCodegenBindings.length-1] = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			}
		}
		
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
		this.actualReceiverType = this.receiverType = scope.enclosingSourceType();
		constant = Constant.NotAConstant;
		if ((this.codegenBinding = this.binding = scope.getBinding(tokens, bits & RestrictiveFlagMASK, this))
			.isValidBinding()) {
			switch (bits & RestrictiveFlagMASK) {
				case VARIABLE : //============only variable===========
				case TYPE | VARIABLE :
					if (binding instanceof LocalVariableBinding) {
						if (!((LocalVariableBinding) binding).isFinal() && ((bits & DepthMASK) != 0))
							scope.problemReporter().cannotReferToNonFinalOuterLocal(
								(LocalVariableBinding) binding,
								this);
						bits &= ~RestrictiveFlagMASK; // clear bits
						bits |= LOCAL;
						return this.resolvedType = getOtherFieldBindings(scope);
					}
					if (binding instanceof FieldBinding) {
						// check for forward references
						FieldBinding fieldBinding = (FieldBinding) binding;
						MethodScope methodScope = scope.methodScope();
						if (methodScope.enclosingSourceType() == fieldBinding.declaringClass
							&& methodScope.fieldDeclarationIndex != MethodScope.NotInFieldDecl
							&& fieldBinding.id >= methodScope.fieldDeclarationIndex) {
							if ((!fieldBinding.isStatic() || methodScope.isStatic)
								&& this.indexOfFirstFieldBinding == 1)
								scope.problemReporter().forwardReference(this, 0, scope.enclosingSourceType());
						}
						bits &= ~RestrictiveFlagMASK; // clear bits
						bits |= FIELD;
						
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
					bits |= TYPE;
				case TYPE : //=============only type ==============
					if (isTypeUseDeprecated((TypeBinding) binding, scope))
						scope.problemReporter().deprecatedType((TypeBinding) binding, this);
					return this.resolvedType = (TypeBinding) binding;
			}
		}
		//========error cases===============
		return this.resolvedType = this.reportError(scope);
	}
	public void setFieldIndex(int index) {
		this.indexOfFirstFieldBinding = index;
	}
	public String toStringExpression() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			buffer.append(tokens[i]);
			if (i < (tokens.length - 1)) {
				buffer.append("."); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public String unboundReferenceErrorName() {
		return new String(tokens[0]);
	}
}