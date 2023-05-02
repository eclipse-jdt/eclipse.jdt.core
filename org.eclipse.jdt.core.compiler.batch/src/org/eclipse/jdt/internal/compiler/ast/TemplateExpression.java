package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class TemplateExpression extends Expression {
	Expression processor;
	public StringTemplate template;
	MessageSend invocation;
	public TemplateExpression(Expression processor, StringTemplate template) {
		this.processor = processor;
		this.template = template;
		this.sourceStart = processor.sourceStart;
		this.sourceEnd = template.sourceEnd;
	}
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		this.processor.printExpression(0, output);
		output.append("."); //$NON-NLS-1$
		this.template.printExpression(0, output);
		return output;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.constant != Constant.NotAConstant) {
			this.constant = Constant.NotAConstant;
		}
		//this.literal.resolve(scope);
		//this.resolvedType = this.literal.resolvedType;
		//this.processor.resolve(scope);
//		if (this.processor.resolvedType.isValidBinding()) {
//			if (!CharOperation.equals(TypeConstants.CharArray_JAVA_LANG_PROCESSOR, this.processor.resolvedType.readableName())) {
//				// Report an error
//				return this.resolvedType;
//			}
			this.invocation = new MessageSend();
			this.invocation.receiver = this.processor;
			this.invocation.selector = "process".toCharArray(); // TODO make a constant //$NON-NLS-1$
			this.invocation.arguments = new Expression[] {this.template};
			this.invocation.resolve(scope);
			this.resolvedType = this.invocation.resolvedType;
//		}
		// Validate processor is of expected type (java.lang.StringTemplate.Processor)
		// Create
		return this.resolvedType;
	}
	@Override
	public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
		this.invocation.computeConversion(scope, runtimeTimeType, compileTimeType);
	}
	private void generateNewTemplateBootstrap(CodeStream codeStream) {
		int index = codeStream.classFile.recordBootstrapMethod(this);
		codeStream.invokeDynamic(index,
				2, //
				1, // int
				"typeSwitch".toCharArray(), //$NON-NLS-1$
				"(Ljava/lang/Object;I)I".toCharArray(), //$NON-NLS-1$
				TypeIds.T_int,
				TypeBinding.INT);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.template.generateCode(currentScope, codeStream, true); // Just the generation of the string literal

		this.invocation.generateCode(currentScope, codeStream, true);
	}
}
