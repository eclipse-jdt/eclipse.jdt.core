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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Label;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.LoopingFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ForeachStatement extends Statement {
    
	public LocalDeclaration elementVariable;
	public int elementVariableImplicitWidening = -1; 
	public Expression collection;
	public Statement action;
	
	// set the kind of foreach
	private int kind;
	// possible kinds of iterating behavior
	private static final int ARRAY = 0;
	private static final int RAW_ITERABLE = 1;
	private static final int GENERIC_ITERABLE = 2;

	private int arrayElementTypeID;

	// loop labels
	private Label breakLabel;
	private Label continueLabel;
	
	public BlockScope scope;

	// secret variables for codegen
	public LocalVariableBinding indexVariable;
	public LocalVariableBinding collectionVariable;	// to store the collection expression value
	public LocalVariableBinding maxVariable;
	// secret variable names
	private static final char[] SecretIndexVariableName = " index".toCharArray(); //$NON-NLS-1$
	private static final char[] SecretCollectionVariableName = " collection".toCharArray(); //$NON-NLS-1$
	private static final char[] SecretMaxVariableName = " max".toCharArray(); //$NON-NLS-1$
	
	int postCollectionInitStateIndex = -1;
	int mergedInitStateIndex = -1;
	
	public ForeachStatement(
		LocalDeclaration elementVariable,
		Expression collection,
		Statement action,
		int start,
		int end) {

		this.elementVariable = elementVariable;
		this.collection = collection;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= IsUsefulEmptyStatementMASK;
		
		this.kind = -1;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
		// initialize break and continue labels
		breakLabel = new Label();
		continueLabel = new Label();

		// process the element variable and collection
		flowInfo = this.elementVariable.analyseCode(scope, flowContext, flowInfo);
		flowInfo = this.collection.analyseCode(scope, flowContext, flowInfo);

		// element variable will be assigned when iterating
		flowInfo.markAsDefinitelyAssigned(this.elementVariable.binding);

		this.postCollectionInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
		
		// process the action
		LoopingFlowContext loopingContext = new LoopingFlowContext(flowContext, this, breakLabel, continueLabel, scope);
		FlowInfo actionInfo = flowInfo.initsWhenTrue().copy();
		if (!(action == null || (action.isEmptyBlock() 
		        	&& currentScope.environment().options.complianceLevel <= ClassFileConstants.JDK1_3))) {

			if (!this.action.complainIfUnreachable(actionInfo, scope, false)) {
				actionInfo = action.analyseCode(scope, loopingContext, actionInfo);
			}

			// code generation can be optimized when no need to continue in the loop
			if (!actionInfo.isReachable() && !loopingContext.initsOnContinue.isReachable()) {
				continueLabel = null;
			} else {
				actionInfo = actionInfo.mergedWith(loopingContext.initsOnContinue.unconditionalInits());
				loopingContext.complainOnFinalAssignmentsInLoop(scope, actionInfo);
			}
		}

		// we need the variable to iterate the collection even if the 
		// element variable is not used
		if (!(this.action == null
				|| this.action.isEmptyBlock()
				|| ((this.action.bits & IsUsefulEmptyStatementMASK) != 0))) {
			switch(this.kind) {
				case ARRAY :
					this.collectionVariable.useFlag = LocalVariableBinding.USED;
					this.indexVariable.useFlag = LocalVariableBinding.USED;
					this.maxVariable.useFlag = LocalVariableBinding.USED;
					break;
				case RAW_ITERABLE :
				case GENERIC_ITERABLE :
					this.collectionVariable.useFlag = LocalVariableBinding.USED;
					this.indexVariable.useFlag = LocalVariableBinding.USED;
					break;
			}
		}
		//end of loop
		FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
				loopingContext.initsOnBreak, 
				false, 
				flowInfo.initsWhenFalse(), 
				false, 
				true /*for(;;){}while(true); unreachable(); */);
		return mergedInfo;
	}

	/**
	 * For statement code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	    
		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		if (this.action == null
				|| this.action.isEmptyBlock()
				|| ((this.action.bits & IsUsefulEmptyStatementMASK) != 0)) {
			codeStream.exitUserScope(scope);
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					mergedInitStateIndex);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		// TODO (olivier) reverse the loop again
		// generate the initializations
		switch(this.kind) {
			case ARRAY :
				collection.generateCode(scope, codeStream, true);
				codeStream.store(this.collectionVariable, false);
				codeStream.iconst_0();
				codeStream.store(this.indexVariable, false);
				codeStream.load(this.collectionVariable);
				codeStream.arraylength();
				codeStream.store(this.maxVariable, false);
				break;
			case RAW_ITERABLE :
			case GENERIC_ITERABLE :
				collection.generateCode(scope, codeStream, true);
				codeStream.store(this.collectionVariable, false);
				// TODO (olivier) use well known method
				MethodBinding methodBinding = scope.getMethod(collection.resolvedType, "iterator".toCharArray(), TypeConstants.NoParameters, (InvocationSite) collection);
				codeStream.invokevirtual(methodBinding);
				codeStream.store(this.indexVariable, false);
				break;
		}
		
		// label management
		Label conditionLabel = new Label(codeStream);
		this.breakLabel.codeStream = codeStream;
		if (this.continueLabel != null) {
			this.continueLabel.codeStream = codeStream;
		}
		
		// generate the condition
		conditionLabel.place();
		if (this.postCollectionInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				this.postCollectionInitStateIndex);
		}
		switch(this.kind) {
			case ARRAY :
				codeStream.load(this.indexVariable);
				codeStream.load(this.maxVariable);
				codeStream.if_icmpge(this.breakLabel);
				break;
			case RAW_ITERABLE :
			case GENERIC_ITERABLE :
				codeStream.load(this.indexVariable);
				codeStream.invokeJavaUtilIteratorHasNext();
				break;
		}

		// generate the loop action
		if (this.elementVariable.binding.resolvedPosition != -1) {
			switch(this.kind) {
				case ARRAY :
					codeStream.load(this.collectionVariable);
					codeStream.load(this.indexVariable);
					codeStream.arrayAt(this.arrayElementTypeID);
					if (this.elementVariableImplicitWidening != -1) {
						codeStream.generateImplicitConversion(this.elementVariableImplicitWidening);
					}
					codeStream.store(this.elementVariable.binding, false);
					break;
				case RAW_ITERABLE :
				case GENERIC_ITERABLE :
					codeStream.load(this.indexVariable);
					codeStream.invokeJavaUtilIteratorNext();
					if (this.elementVariable.binding.type.id != T_JavaLangObject) {
						codeStream.checkcast(this.elementVariable.binding.type);
					}
					codeStream.store(this.elementVariable.binding, false);
					break;
			}
			codeStream.addVisibleLocalVariable(this.elementVariable.binding);
			if (this.postCollectionInitStateIndex != -1) {
				codeStream.addDefinitelyAssignedVariables(
					currentScope,
					this.postCollectionInitStateIndex);
			}
		}
		this.action.generateCode(scope, codeStream);
			
		// continuation point
		if (this.continueLabel != null) {
			this.continueLabel.place();
			// generate the increments for next iteration
			switch(this.kind) {
				case ARRAY :
					codeStream.iinc(this.indexVariable.resolvedPosition, 1);
					break;
				case RAW_ITERABLE :
				case GENERIC_ITERABLE :
					break;
			}
			codeStream.goto_(conditionLabel);
		}
		this.breakLabel.place();
		codeStream.exitUserScope(scope);
		if (this.mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				this.mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("for ("); //$NON-NLS-1$
		this.elementVariable.print(0, output); 
		output.append(" : ");//$NON-NLS-1$
		this.collection.print(0, output).append(") "); //$NON-NLS-1$
		//block
		if (this.action == null) {
			output.append(';');
		} else {
			output.append('\n');
			this.action.printStatement(tab + 1, output); //$NON-NLS-1$
		}
		return output;
	}

	public void resetStateForCodeGeneration() {
		if (this.breakLabel != null) {
			this.breakLabel.resetStateForCodeGeneration();
		}
		if (this.continueLabel != null) {
			this.continueLabel.resetStateForCodeGeneration();
		}
	}

	public void resolve(BlockScope upperScope) {
		// use the scope that will hold the init declarations
		scope = new BlockScope(upperScope);
		this.elementVariable.resolve(scope); // collection expression can see itemVariable
		TypeBinding elementType = this.elementVariable.type.resolvedType;
		TypeBinding collectionType = this.collection.resolveType(scope);
		boolean hasError = elementType == null || collectionType == null;
		
		if (!hasError) {
			if (collectionType.isArrayType()) { // for(E e : E[])
				this.kind = ARRAY;
				TypeBinding collectionElementType = ((ArrayBinding) collectionType).elementsType(scope);
				if (!collectionElementType.isCompatibleWith(elementType)) {
					scope.problemReporter().notCompatibleTypesErrorInForeach(collection, collectionElementType, elementType);
				}
				// in case we need to do a conversion
				this.arrayElementTypeID = collectionElementType.id;
				if (elementType.isBaseType()) {
					this.elementVariableImplicitWidening = (elementType.id << 4) + this.arrayElementTypeID;
				}
			} else if (collectionType.id == T_JavaLangIterable) { // for(Object o : Iterable)
				this.kind = RAW_ITERABLE;
					TypeBinding collectionElementType = scope.getJavaLangObject(); 
					if (!collectionElementType.isCompatibleWith(elementType)) {
						scope.problemReporter().notCompatibleTypesErrorInForeach(collection, collectionElementType, elementType);
					}
					// no conversion needed as only for reference types
			} else if (collectionType.isParameterizedType()) {
			    ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding)collectionType;
			    if (parameterizedType.type.id == T_JavaLangIterable) { // for(E e : Iterable<E>)
					if (parameterizedType.arguments.length == 1) { // per construction can only be one
						this.kind = GENERIC_ITERABLE;
						TypeBinding collectionElementType = parameterizedType.arguments[0]; 
						if (!collectionElementType.isCompatibleWith(elementType)) {
							scope.problemReporter().notCompatibleTypesErrorInForeach(collection, collectionElementType, elementType);
						}
						// no conversion needed as only for reference types
					}
			    }
			}
	
			if (this.kind == -1) {
				scope.problemReporter().invalidTypeForCollection(collection);
			} else {
				switch(this.kind) {
					case ARRAY :
						// indexVariable is used only if collection is an array
						this.indexVariable = new LocalVariableBinding(SecretIndexVariableName, IntBinding, AccDefault, false);
						scope.addLocalVariable(this.indexVariable);
						this.indexVariable.constant = NotAConstant; // not inlinable
			
						this.maxVariable = new LocalVariableBinding(SecretMaxVariableName, IntBinding, AccDefault, false);
						scope.addLocalVariable(this.maxVariable);
						this.maxVariable.constant = NotAConstant; // not inlinable
						break;
					case RAW_ITERABLE :
					case GENERIC_ITERABLE :
						this.indexVariable = new LocalVariableBinding(SecretIndexVariableName, scope.getJavaUtilIterator(), AccDefault, false);
						scope.addLocalVariable(this.indexVariable);
						this.indexVariable.constant = NotAConstant; // not inlinable
						break;
				}
			}
		}
		if (action != null) {
			action.resolve(scope);
		}
		if (!hasError) {
			// add secret variables used for the code generation
			this.collectionVariable = new LocalVariableBinding(SecretCollectionVariableName, collectionType, AccDefault, false);
			scope.addLocalVariable(this.collectionVariable);
			this.collectionVariable.constant = NotAConstant; // not inlinable
		}
	}
	
	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.elementVariable.traverse(visitor, scope);
			this.collection.traverse(visitor, scope);
			if (action != null) {
				action.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}
