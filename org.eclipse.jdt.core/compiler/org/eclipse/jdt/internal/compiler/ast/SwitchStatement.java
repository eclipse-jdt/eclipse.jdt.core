package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class SwitchStatement extends Statement {
	public Expression testExpression;
	public Statement[] statements;
	public BlockScope scope;
	public int explicitDeclarations;
	public Label breakLabel;
	public Case[] cases;
	public DefaultCase defaultCase;
	public int caseCount = 0;
	
	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;
/**
 * SwitchStatement constructor comment.
 */
public SwitchStatement() {
	super();
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	flowInfo = testExpression.analyseCode(currentScope, flowContext, flowInfo);
	SwitchFlowContext switchContext = new SwitchFlowContext(flowContext, this, (breakLabel = new Label()));

	// analyse the block by considering specially the case/default statements (need to bind them 
	// to the entry point)
	FlowInfo caseInits = FlowInfo.DeadEnd; // in case of statements before the first case
	preSwitchInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
	int caseIndex = 0;
	if (statements != null) {
		for (int i = 0, max = statements.length; i < max; i++) {
			Statement statement = statements[i];
			if ((caseIndex < caseCount) && (statement == cases[caseIndex])) { // statements[i] is a case or a default case
				caseIndex++;
				caseInits = caseInits.mergedWith(flowInfo.copy().unconditionalInits());
			} else {
				if (statement == defaultCase) {
					caseInits = caseInits.mergedWith(flowInfo.copy().unconditionalInits());
				}
			}
			if (!caseInits.complainIfUnreachable(statement, scope)) {
				caseInits = statement.analyseCode(scope, switchContext, caseInits);
			}
		}
	}

	// if no default case, then record it may jump over the block directly to the end
	if (defaultCase == null) {
		// only retain the potential initializations
		flowInfo.addPotentialInitializationsFrom(caseInits.mergedWith(switchContext.initsOnBreak));
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
		return flowInfo;
	}

	// merge all branches inits
	FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak);
	mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}
/**
 * Switch code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	int[] sortedIndexes = new int[caseCount];
	int[] localKeysCopy;
	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;

	// prepare the labels and constants
	breakLabel.codeStream = codeStream;
	CaseLabel[] caseLabels = new CaseLabel[caseCount];
	int[] constants = new int[caseCount];
	boolean needSwitch = caseCount != 0;
	for (int i = 0; i < caseCount; i++) {
		constants[i] = cases[i].constantExpression.constant.intValue();
		cases[i].targetLabel = (caseLabels[i] = new CaseLabel(codeStream));
	}

	// we sort the keys to be able to generate the code for tableswitch or lookupswitch
	for (int i = 0; i < caseCount; i++) {
		sortedIndexes[i] = i;
	}
	System.arraycopy(
		constants, 
		0, 
		(localKeysCopy = new int[caseCount]), 
		0, 
		caseCount); 
	CodeStream.sort(localKeysCopy, 0, caseCount - 1, sortedIndexes);
	CaseLabel defaultLabel = new CaseLabel(codeStream);
	if (defaultCase != null) {
		defaultCase.targetLabel = defaultLabel;
	}
	// generate expression testes
	testExpression.generateCode(currentScope, codeStream, needSwitch);

	// generate the appropriate switch table
	if (needSwitch) {
		int max = localKeysCopy[caseCount - 1];
		int min = localKeysCopy[0];
		if ((long) (caseCount * 2.5) > ((long) max - (long) min)) {
			codeStream.tableswitch(
				defaultLabel, 
				min, 
				max, 
				constants, 
				sortedIndexes, 
				caseLabels); 
		} else {
			codeStream.lookupswitch(defaultLabel, constants, sortedIndexes, caseLabels);
		}
		codeStream.updateLastRecordedEndPC(codeStream.position);
	}
	// generate the switch block statements
	int caseIndex = 0;
	if (statements != null) {
		for (int i = 0, maxCases = statements.length; i < maxCases; i++) {
			Statement statement = statements[i];
			if ((caseIndex < caseCount)
				&& (statement == cases[caseIndex])) { // statements[i] is a case
				if (preSwitchInitStateIndex != -1) {
					codeStream.removeNotDefinitelyAssignedVariables(
						currentScope, 
						preSwitchInitStateIndex); 
					caseIndex++;
				}
			} else {
				if (statement == defaultCase) { // statements[i] is a case or a default case
					if (preSwitchInitStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(
							currentScope, 
							preSwitchInitStateIndex); 
					}
				}
			}
			statement.generateCode(scope, codeStream);
		}
	}
	// place the trailing labels (for break and default case)
	breakLabel.place();
	if (defaultCase == null) {
		defaultLabel.place();
	}
	// May loose some local variable initializations : affecting the local variable attributes
	if (mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(
			currentScope, 
			mergedInitStateIndex); 
		codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
	}
	if (scope != currentScope) {
		codeStream.exitUserScope(scope);
	}
	codeStream.recordPositionsFrom(pc, this);
}
public void resolve(BlockScope upperScope) {
	TypeBinding testType = testExpression.resolveType(upperScope);
	if (testType == null)
		return;
	testExpression.implicitWidening(testType, testType);
	if (!(testExpression.isConstantValueOfTypeAssignableToType(testType, IntBinding))) {
		if (!upperScope.areTypesCompatible(testType, IntBinding)) {
			upperScope.problemReporter().incorrectSwitchType(testExpression, testType);
			return;
		}
	}
	if (statements != null) {
		scope = explicitDeclarations == 0 ? upperScope : new BlockScope(upperScope);
		int length;
		// collection of cases is too big but we will only iterate until caseCount
		cases = new Case[length = statements.length];
		int[] casesValues = new int[length];
		int counter = 0;
		for (int i = 0; i < length; i++) {
			Constant cst;
			if ((cst = statements[i].resolveCase(scope, testType, this)) != null) {
				//----check for duplicate case statement------------
				if (cst != NotAConstant) {
					// a case with a welled typed constant, so intValue() is valid
					int key = cst.intValue();
					for (int j = 0; j < counter; j++) {
						if (casesValues[j] == key) {
							scope.problemReporter().duplicateCase((Case) statements[i], cst);
						}
					}
					casesValues[counter++] = key;
				}
			}
		}
	}
}
public String toString(int tab){
	/* slow code */
	
	String inFront , s = tabString(tab) ;
	inFront = s ;
	s = s + "switch (" + testExpression.toStringExpression() + ") ";
	if (statements == null)
	{ 	s = s + "{}" ; 
		return s;}
	else
		s = s + "{";

	s = s + (explicitDeclarations != 0
				? "// ---scope needed for "+String.valueOf(explicitDeclarations) +" locals------------ \n"
				: "// ---NO scope needed------ \n") ;
		
	int i = 0;
	String tabulation = "  ";
	try	{while(true){
		//use instanceof in order not to polluate classes with behavior only needed for printing purpose.
		if ( statements[i]  instanceof Expression)
			s = s + "\n" + inFront + tabulation;
		if ( statements[i]  instanceof Break)
			s = s + statements[i].toString(0) ;
		else	
			s = s + "\n" + statements[i].toString(tab+2) ;
		//=============	
		if ( (statements[i] instanceof Case) || (statements[i] instanceof DefaultCase))
		{	i++;
			while(! ((statements[i] instanceof Case) || (statements[i] instanceof DefaultCase)))
			{	if ( (statements[i] instanceof Expression) || (statements[i] instanceof Break))
					s = s +  statements[i].toString(0) +" ; ";
				else
					s = s + "\n" + statements[i].toString(tab+6) + " ; ";
				i++;}}
		else
		{	s = s + " ;" ;
			i++;}}}
	catch(IndexOutOfBoundsException e){};
	s = s + "}";
	return s;}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		testExpression.traverse(visitor, scope);
		if (statements != null) {
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++)
				statements[i].traverse(visitor, scope);
		}
	}
	visitor.endVisit(this, blockScope);
}
}
