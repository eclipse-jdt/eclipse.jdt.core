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

public class ClassLiteralAccess extends Expression {
	public TypeReference type;
	public TypeBinding targetType;
	FieldBinding syntheticField;

public ClassLiteralAccess(int pos, TypeReference t) {
	type = t;
	sourceEnd = (sourceStart = pos)+4 ; // "class" length - 1
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	
	// if reachable, request the addition of a synthetic field for caching the class descriptor
	SourceTypeBinding sourceType = currentScope.outerMostMethodScope().enclosingSourceType();
	if (!(sourceType.isInterface() // no field generated in interface case (would'nt verify) see 1FHHEZL
			|| sourceType.isBaseType())){ 
		syntheticField = sourceType.addSyntheticField(targetType, currentScope);
	}
	return flowInfo;
}
/**
 * MessageSendDotClass code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;

	// in interface case, no caching occurs, since cannot make a cache field for interface
	if (valueRequired)
		codeStream.generateClassLiteralAccessForType(type.binding, syntheticField);
	codeStream.recordPositionsFrom(pc, this);
}
public TypeBinding resolveType(BlockScope scope) {
	constant = NotAConstant;
	if ((targetType = type.resolveType(scope)) == null)
		return null;

	if (targetType.isArrayType() && ((ArrayBinding) targetType).leafComponentType == VoidBinding) {
		scope.problemReporter().cannotAllocateVoidArray(this);
		return null;
	}

	return scope.getJavaLangClass();
}
public String toStringExpression(){
	/*slow code*/
	
	String s = ""/*nonNLS*/;
	s = s + type.toString(0) + ".class"/*nonNLS*/ ;
	return s;}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		type.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
