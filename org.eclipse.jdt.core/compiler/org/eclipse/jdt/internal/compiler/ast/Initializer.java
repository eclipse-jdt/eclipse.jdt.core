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
import org.eclipse.jdt.internal.compiler.parser.*;

public class Initializer extends FieldDeclaration {
	//public boolean isStatic = false ;
	public Block block;
	public int lastFieldID;
	public int bodyStart;
public Initializer(Block block, int modifiers) {
	this.block = block;
	this.modifiers = modifiers;

	declarationSourceStart = sourceStart = block.sourceStart;
}
public FlowInfo analyseCode(MethodScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return block.analyseCode(currentScope, flowContext, flowInfo);
}
/**
 * Code generation for a non-static initializer
 *	i.e. normal block code gen
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;
	block.generateCode(currentScope, codeStream);
	codeStream.recordPositionsFrom(pc, this);
}
public boolean isField() {
	return false;
}
public boolean isStatic() {
	return (modifiers & AccStatic) != 0;
}
public void parseStatements(Parser parser, TypeDeclaration type, CompilationUnitDeclaration unit) {
	//fill up the method body with statement

	parser.parse(this, type, unit);
}
public void resolve(MethodScope scope) {
	int previous = scope.fieldDeclarationIndex;
	try {
		scope.fieldDeclarationIndex = lastFieldID;
		if (isStatic()) {
			ReferenceBinding declaringType = scope.enclosingSourceType();
			if (declaringType.isNestedType() && !declaringType.isStatic())
				scope.problemReporter().innerTypesCannotDeclareStaticInitializers(declaringType, this);
		}
		block.resolve(scope);
	} finally {
		scope.fieldDeclarationIndex = previous;
	}
}
public String toString(int tab){
	/*slow code */

	if (modifiers != 0){
		StringBuffer buffer = new StringBuffer();
		buffer.append(tabString(tab));
		buffer.append(modifiersString(modifiers));
		buffer.append("{\n"/*nonNLS*/);
		buffer.append(block.toStringStatements(tab));
		buffer.append(tabString(tab));
		buffer.append("}"/*nonNLS*/);
		return buffer.toString();
	} else {
		return block.toString(tab);
	}
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, MethodScope scope) {
	visitor.visit(this, scope);
	block.traverse(visitor, scope);
}
}
