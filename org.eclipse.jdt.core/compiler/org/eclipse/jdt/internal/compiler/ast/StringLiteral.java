package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class StringLiteral extends Literal {
	char[] source;

	public StringLiteral(char[] token, int s, int e) {
		this(s, e);
		source = token;
	}

	public StringLiteral(int s, int e) {
		super(s, e);
	}

	public void computeConstant() {

		constant = Constant.fromValue(String.valueOf(source));
	}

	public ExtendedStringLiteral extendWith(CharLiteral lit) {
		//add the lit source to mine, just as if it was mine

		return new ExtendedStringLiteral(this, lit);
	}

	public ExtendedStringLiteral extendWith(StringLiteral lit) {
		//add the lit source to mine, just as if it was mine

		return new ExtendedStringLiteral(this, lit);
	}

	/**
	 * Code generation for string literal
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			codeStream.ldc(constant.stringValue());
		codeStream.recordPositionsFrom(pc, this);
	}

	public TypeBinding literalType(BlockScope scope) {
		return scope.getJavaLangString();
	}

	/**
	 * source method comment.
	 */
	public char[] source() {
		return source;
	}

	public String toStringExpression() {

		// handle some special char.....
		StringBuffer result = new StringBuffer("\"");
		for (int i = 0; i < source.length; i++) {
			switch (source[i]) {
				case '\b' :
					result.append("\\b");
					break;
				case '\t' :
					result.append("\\t");
					break;
				case '\n' :
					result.append("\\n");
					break;
				case '\f' :
					result.append("\\f");
					break;
				case '\r' :
					result.append("\\r");
					break;
				case '\"' :
					result.append("\\\"");
					break;
				case '\'' :
					result.append("\\'");
					break;
				case '\\' : //take care not to display the escape as a potential real char
					result.append("\\\\");
					break;
				default :
					result.append(source[i]);
			}
		}
		result.append("\"");
		return result.toString();
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

}
