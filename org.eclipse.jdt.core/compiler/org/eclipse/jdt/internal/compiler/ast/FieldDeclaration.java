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
import org.eclipse.jdt.internal.compiler.problem.*;

public class FieldDeclaration extends AbstractVariableDeclaration {
	public FieldBinding binding;
	boolean hasBeenResolved = false;

	//allows to retrieve both the "type" part of the declaration (part1)
	//and also the part that decribe the name and the init and optionally
	//some other dimension ! .... 
	//public int[] a, b[] = X, c ;
	//for b that would give for 
	// - part1 : public int[]
	// - part2 : b[] = X,
	
	public int endPart1Position; 
	public int endPart2Position;
public FieldDeclaration(){}
public FieldDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	
	this.initialization = initialization;
	this.name = name;

	//due to some declaration like 
	// int x, y = 3, z , x ;
	//the sourceStart and the sourceEnd is ONLY on  the name

	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}
public FlowInfo analyseCode(MethodScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (initialization != null) {
		flowInfo = initialization.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		flowInfo.markAsDefinitelyAssigned(binding);
	} else {
		flowInfo.markAsDefinitelyNotAssigned(binding); // clear the bit in case it was already set (from enclosing info)
	}
	return flowInfo;
}
/**
 * Code generation for a field declaration
 *	i.e. normal assignment to a field 
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	// do not generate initialization code if final and static (constant is then
	// recorded inside the field itself).
	int pc = codeStream.position;
	boolean isStatic;
	if (initialization != null 
		&& !((isStatic = binding.isStatic()) && binding.constant != NotAConstant)){
		// non-static field, need receiver
		if (!isStatic) codeStream.aload_0(); 
		// generate initialization value
		initialization.generateCode(currentScope, codeStream, true);
		// store into field
		if (isStatic) {
			codeStream.putstatic(binding);
		} else {
			codeStream.putfield(binding);
		}
	}
	codeStream.recordPositionsFrom(pc, this);
}
public TypeBinding getTypeBinding(Scope scope) {
	return type.getTypeBinding(scope);
}
public boolean isField() {
	return true;
}
public boolean isStatic() {
	if (binding != null) return binding.isStatic();	
	return (modifiers & AccStatic) != 0;
}
public String name(){

	return String.valueOf(name) ;}
public void resolve(MethodScope initializationScope) {
	// the two <constant = Constant.NotAConstant> could be regrouped into
	// a single line but it is clearer to have two lines while the reason of their
	// existence is not at all the same. See comment for the second one.

	//--------------------------------------------------------
	if (!hasBeenResolved && binding != null && binding.isValidBinding()) {
		hasBeenResolved = true;
		if (isTypeUseDeprecated(binding.type, initializationScope))
			initializationScope.problemReporter().deprecatedType(binding.type, type);

		this.type.binding = this.binding.type; // update binding for type reference
		
		// the resolution of the initialization hasn't been done
		if (initialization == null) {
			binding.constant = Constant.NotAConstant;
		} else {
			// break dead-lock cycles by forcing constant to NotAConstant
			int previous = initializationScope.fieldDeclarationIndex;
			try {
				initializationScope.fieldDeclarationIndex = binding.id;
				binding.constant = Constant.NotAConstant;
				TypeBinding tb = binding.type;
				TypeBinding initTb;
				if (initialization instanceof ArrayInitializer) {
					if ((initTb = initialization.resolveTypeExpecting(initializationScope, tb)) != null) {
						((ArrayInitializer) initialization).binding = (ArrayBinding) initTb;
						initialization.implicitWidening(tb, initTb);
					}
				} else if ((initTb = initialization.resolveType(initializationScope)) != null) {
					if (initialization.isConstantValueOfTypeAssignableToType(initTb, tb) || (tb.isBaseType() && BaseTypeBinding.isWidening(tb.id, initTb.id)))
						initialization.implicitWidening(tb, initTb);
					else if (initializationScope.areTypesCompatible(initTb, tb))
						initialization.implicitWidening(tb, initTb);
					else
						initializationScope.problemReporter().typeMismatchError(initTb, tb, this);
					if (binding.isFinal())  // cast from constant actual type to variable type
						binding.constant = initialization.constant.castTo((binding.type.id << 4) + initialization.constant.typeID());
				} else {
					binding.constant = NotAConstant;
				}
			} finally {
				initializationScope.fieldDeclarationIndex = previous;
				if (binding.constant == null) binding.constant = Constant.NotAConstant;
			}
		}
		// cannot define static non-constant field inside nested class
		if (binding.isStatic() && binding.constant == NotAConstant)
			if (binding.declaringClass.isNestedType() && binding.declaringClass.isClass() && !binding.declaringClass.isStatic())
				initializationScope.problemReporter().unexpectedStaticModifierForField((SourceTypeBinding)binding.declaringClass, this);		
	} 
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, MethodScope scope) {
	visitor.visit(this, scope);
	type.traverse(visitor, scope);
	if (initialization != null) initialization.traverse(visitor, scope);
}
}
