package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.impl.NullConstant;

/**
 * A return statement inside a code snippet. During the code gen,
 * it uses a macro to set the result of the code snippet instead
 * of returning it.
 */
public class CodeSnippetReturnStatement extends ReturnStatement implements InvocationSite, EvaluationConstants {
	MethodBinding setResultMethod;
public CodeSnippetReturnStatement(Expression expr, int s, int e, EvaluationContext evaluationContext) {
	super(expr, s, e);
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	FlowInfo info = super.analyseCode(currentScope, flowContext, flowInfo);
	// we need to remove this optimization in order to prevent the inlining of the return bytecode
	// 1GH0AU7: ITPJCORE:ALL - Eval - VerifyError in scrapbook page
	this.bits &= ~ValueForReturnMASK;
	this.expression.bits &= ~ValueForReturnMASK;
	return info;
}

/**
 * Dump the suitable return bytecode for a return statement
 *
 */
public void generateReturnBytecode(BlockScope currentScope, CodeStream codeStream) {
	
	// output the return bytecode
	codeStream.return_();
}
public void generateStoreSaveValueIfNecessary(BlockScope currentScope, CodeStream codeStream){

	// push receiver
	codeStream.aload_0();

	// push the 2 parameters of "setResult(Object, Class)"
	if (this.expression == null || this.expressionType == VoidBinding) { // expressionType == VoidBinding if code snippet is the expression "System.out.println()"
		// push null
		codeStream.aconst_null();

		// void.class
		codeStream.generateClassLiteralAccessForType(VoidBinding, null);
	} else {
		// swap with expression
		int valueTypeID = this.expressionType.id;
		if (valueTypeID == T_long || valueTypeID == T_double) {
			codeStream.dup_x2();
			codeStream.pop();
		} else {
			codeStream.swap();
		}

		// generate wrapper if needed
		if (this.expressionType.isBaseType()) { 
			codeStream.generateObjectWrapperForType(this.expressionType);
		}

		// generate the expression type
		codeStream.generateClassLiteralAccessForType(this.expressionType, null);
	}

	// generate the invoke virtual to "setResult(Object,Class)"
	codeStream.invokevirtual(this.setResultMethod);
}
public boolean isSuperAccess() {
	return false;
}
public boolean isTypeAccess() {
	return false;
}
public boolean needValue(){
	return true;
}
public void prepareSaveValueLocation(BlockScope currentScope){
		
	// do nothing: no storage is necessary for snippets
}
public void resolve(BlockScope scope) {
	if (this.expression != null) {
		if ((this.expressionType = this.expression.resolveType(scope)) != null) {
			TypeBinding javaLangClass = scope.getJavaLangClass();
			if (!javaLangClass.isValidBinding()) {
				scope.problemReporter().codeSnippetMissingClass("java.lang.Class"/*nonNLS*/, this.sourceStart, this.sourceEnd);
				return;
			}
			TypeBinding javaLangObject = scope.getJavaLangObject();
			if (!javaLangObject.isValidBinding()) {
				scope.problemReporter().codeSnippetMissingClass("java.lang.Object"/*nonNLS*/, this.sourceStart, this.sourceEnd);
				return;
			}
			TypeBinding[] argumentTypes = new TypeBinding[] {javaLangObject, javaLangClass};
			this.setResultMethod = scope.getImplicitMethod(SETRESULT_SELECTOR, argumentTypes, this);
			if (!this.setResultMethod.isValidBinding()) {
				scope.problemReporter().codeSnippetMissingMethod(ROOT_FULL_CLASS_NAME, new String(SETRESULT_SELECTOR), new String(SETRESULT_ARGUMENTS), this.sourceStart, this.sourceEnd);
				return;
			}
			// in constant case, the implicit conversion cannot be left uninitialized
			if (this.expression.constant != NotAConstant) {
				// fake 'no implicit conversion' (the return type is always void)
				this.expression.implicitConversion = this.expression.constant.typeID() << 4;
			}
		}
	}
}
public void setDepth(int depth) {
}
public void setFieldIndex(int depth) {
}
}
