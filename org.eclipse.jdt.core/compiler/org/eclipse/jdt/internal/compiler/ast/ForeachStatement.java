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
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ForeachStatement extends Statement {
    
	public LocalDeclaration itemVariable;
	public int itemVariableImplicitWidening = -1; 
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
		LocalDeclaration localDeclaration,
		Expression collection,
		Statement action,
		int start,
		int end) {

		this.itemVariable = localDeclaration;
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

		// mark the synthetic variables as being used
	    // TODO (philippe) should only allocate/tag required variables
		this.collectionVariable.useFlag = LocalVariableBinding.USED;
		this.indexVariable.useFlag = LocalVariableBinding.USED;
		this.maxVariable.useFlag = LocalVariableBinding.USED;

		// initialize break and continue labels
		breakLabel = new Label();
		continueLabel = new Label();

		// process the item variable and collection
		flowInfo = this.itemVariable.analyseCode(scope, flowContext, flowInfo);
		flowInfo = this.collection.analyseCode(scope, flowContext, flowInfo);

		this.postCollectionInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
		
		// item variable will be assigned when iterating
		flowInfo.markAsDefinitelyAssigned(this.itemVariable.binding);
		// TODO (philippe) item variable is always used ? if action is empty, do we still use it ?
		this.itemVariable.binding.useFlag = LocalVariableBinding.USED;

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
		// TODO should optimize cases where nothing is to be done (action is empty statement)
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
				// TODO to be completed
				break;
			case GENERIC_ITERABLE :
				// TODO to be completed
				break;
		}
		
		// label management
		Label actionLabel = new Label(codeStream);
		Label conditionLabel = new Label(codeStream);
		breakLabel.codeStream = codeStream;
		if (this.continueLabel != null) {
			this.continueLabel.codeStream = codeStream;
		}
		// jump over the actionBlock
		if (!(action == null || action.isEmptyBlock())) {
			int jumpPC = codeStream.position;
			codeStream.goto_(conditionLabel);
			codeStream.recordPositionsFrom(jumpPC, this.itemVariable.sourceStart);
		}
		// generate the loop action
		actionLabel.place();
		if (action != null) {
			// initialize the indexVariable value
			switch(this.kind) {
				case ARRAY :
					codeStream.load(this.collectionVariable);
					codeStream.load(this.indexVariable);
					codeStream.arrayAt(this.arrayElementTypeID);
					if (this.itemVariableImplicitWidening != -1) {
						codeStream.generateImplicitConversion(this.itemVariableImplicitWidening);
					}
					codeStream.store(this.itemVariable.binding, false);
					break;
				case RAW_ITERABLE :
					// TODO to be completed
					break;
				case GENERIC_ITERABLE :
					// TODO to be completed
					break;
			}
			action.generateCode(scope, codeStream);
		}
		// continuation point
		if (this.continueLabel != null) {
			this.continueLabel.place();
			// generate the increments for next iteration
			switch(this.kind) {
				case ARRAY :
					codeStream.iinc(this.indexVariable.resolvedPosition, 1);
					break;
				case RAW_ITERABLE :
					// TODO to be completed
					break;
				case GENERIC_ITERABLE :
					// TODO to be completed
					break;
			}			
		}
		// generate the condition
		conditionLabel.place();
		switch(this.kind) {
			case ARRAY :
				codeStream.load(this.indexVariable);
				codeStream.load(this.maxVariable);
				codeStream.if_icmplt(actionLabel);
				break;
			case RAW_ITERABLE :
				// TODO to be completed
				break;
			case GENERIC_ITERABLE :
				// TODO to be completed
				break;
		}
		breakLabel.place();
	
		codeStream.exitUserScope(scope);
		if (this.postCollectionInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				postCollectionInitStateIndex);
		}		
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("for ("); //$NON-NLS-1$
		this.itemVariable.print(0, output); 
		output.append(" : ");//$NON-NLS-1$
		this.collection.print(0, output).append(") "); //$NON-NLS-1$
		//block
		if (action == null) {
			output.append(';');
		} else {
			output.append('\n');
			action.printStatement(tab + 1, output); //$NON-NLS-1$
		}
		return output;
	}

	public void resetStateForCodeGeneration() {
		// TODO to be completed
	}

	public void resolve(BlockScope upperScope) {
		// use the scope that will hold the init declarations
		scope = new BlockScope(upperScope);
		this.itemVariable.resolve(scope); // collection expression can see itemVariable
		TypeBinding elementType = this.itemVariable.type.resolvedType;
		TypeBinding collectionType = this.collection.resolveType(scope);

		// TODO (olivier) need to handle java.lang.Iterable and java.lang.Iterable<E>
		if (collectionType.isArrayType()) {
			this.kind = ARRAY;
			TypeBinding collectionElementType = ((ArrayBinding) collectionType).elementsType(scope);
			if (!collectionElementType.isCompatibleWith(elementType)) {
				scope.problemReporter().notCompatibleTypesErrorForIteratorFor(collection, collectionElementType, elementType);
			}
			// in case we need to do a conversion
			this.arrayElementTypeID = collectionElementType.id;
			if (elementType.isBaseType()) {
				this.itemVariableImplicitWidening = (elementType.id << 4) + this.arrayElementTypeID;
			}
		}
/*		if (collectionType == scope.getJavaLangIterable()) {
			this.kind = RAW_ITERABLE;
		}
		if (collectionType.isGenericIterable()) {
			this.kind = GENERIC_ITERABLE;
		}
*/
		if (this.kind == -1) {
			scope.problemReporter().invalidTypeForCollection(collection);
		} else {
			// indexVariable is an int only if collection is an array
			this.indexVariable = new LocalVariableBinding(SecretIndexVariableName, IntBinding, AccDefault, false);
			scope.addLocalVariable(this.indexVariable);
			this.indexVariable.constant = NotAConstant; // not inlinable

			this.maxVariable = new LocalVariableBinding(SecretMaxVariableName, IntBinding, AccDefault, false);
			scope.addLocalVariable(this.maxVariable);
			this.maxVariable.constant = NotAConstant; // not inlinable
		}
		if (action != null) {
			action.resolve(scope);
		}

		// add secret variables used for the code generation
		this.collectionVariable = new LocalVariableBinding(SecretCollectionVariableName, collectionType, AccDefault, false);
		scope.addLocalVariable(this.collectionVariable);
		this.collectionVariable.constant = NotAConstant; // not inlinable
	}
	
	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.itemVariable.traverse(visitor, scope);
			this.collection.traverse(visitor, scope);
			if (action != null) {
				action.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}
