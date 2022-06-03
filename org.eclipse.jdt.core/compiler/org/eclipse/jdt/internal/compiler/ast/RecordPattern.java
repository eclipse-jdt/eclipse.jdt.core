/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class RecordPattern extends TypePattern {

	public Pattern[] patterns;
	public TypePattern typePattern;
	public TypeReference type;

	public RecordPattern(LocalDeclaration local) {
		super(local);
		this.typePattern = new TypePattern(local);
		this.type = local.type;
		this.sourceStart = local.sourceStart;
		this.sourceEnd = local.sourceEnd;
	}
	public RecordPattern(TypeReference type, int sourceStart, int sourceEnd) {
		super();
		this.type = type;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	@Override
	public TypeReference getType() {
		return this.type;
	}
 	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		if (this.resolvedType == null) {
			this.resolveType(scope);
		}
		this.addPatternVariablesWhenTrue(variables);
		if (this.typePattern != null && this.typePattern.local.binding != null) {
			if (this.patternVarsWhenTrue == null) {
				this.patternVarsWhenTrue = new LocalVariableBinding[1];
				this.patternVarsWhenTrue[0] = this.typePattern.local.binding;
			} else {
				this.addPatternVariablesWhenTrue(new LocalVariableBinding[] {this.typePattern.local.binding});
			}
		}
		for (Pattern p : this.patterns) {
			p.collectPatternVariablesToScope(this.patternVarsWhenTrue, scope);
			this.addPatternVariablesWhenTrue(p.patternVarsWhenTrue);
		}
	}
	@Override
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (!castType.isReifiable())
			return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
		else
			return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
	}

	@Override
	public LocalDeclaration getPatternVariable() {
		return this.typePattern != null ? this.typePattern.getPatternVariable() : null;
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		if (this.typePattern != null) {
			this.typePattern.analyseCode(currentScope, flowContext, flowInfo);
		}
		for (Pattern p : this.patterns) {
			 p.analyseCode(currentScope, flowContext, flowInfo);
		}
		return flowInfo;
	}
	@Override
	public boolean isTotalForType(TypeBinding t) {
		return false;
	}
	@Override
	public void resolveWithExpression(BlockScope scope, Expression exp) {
		this.expression = exp;
	}
	@Override
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding u) {
		for (Pattern p : this.patterns) {
			p.resolveAtType(scope, u);
		}
		if (this.typePattern != null) {
			this.resolvedType = this.typePattern.resolveAtType(scope, u);
		}
		return this.resolvedType;
	}
	@Override
	public TypeBinding resolveType(BlockScope scope, boolean isPatternVariable) {
		if (this.resolvedType != null)
			return this.resolvedType;

		if (this.typePattern != null) {
			this.resolvedType = this.typePattern.resolveType(scope);
		} else {
			this.resolvedType = this.type.resolveType(scope);
		}

		// check whether the give type reference is a record
		// check whether the pattern signature matches that of the record declaration
		if (!this.resolvedType.isRecord()) {
			scope.problemReporter().unexpectedTypeinRecordPattern(this.resolvedType, this.type);
			return this.resolvedType;
		}
		RecordComponentBinding[] components = this.resolvedType.components();
		if (components.length != this.patterns.length) {
			scope.problemReporter().recordPatternSignatureMismatch(this.resolvedType, this);
		} else {
			for (int i = 0; i < components.length; i++) {
				Pattern p = this.patterns[i];
				TypeBinding resolveType = p.resolveType(scope, true);
				RecordComponentBinding componentBinding = components[i];
				if (p.getType().isTypeNameVar(scope)) {
					p.resolvedType = componentBinding.type;
				} else {
					if (componentBinding.type.isCompatibleWith(resolveType)) {
						MethodBinding[] methods = this.resolvedType.getMethods(componentBinding.name);
						if (methods != null && methods.length > 0) {
							p.accessorMethod = methods[0]; // TODO: Not enough?
						}
					} else {
						scope.problemReporter().recordPatternSignatureMismatch(this.resolvedType, this.patterns[i].getType());
					}
				}
			}
		}

		return this.resolvedType;
	}
	@Override
	public boolean dominates(Pattern p) {
		return isTotalForType(p.resolvedType);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		for (Pattern p : this.patterns) {
			if (p.accessorMethod != null) {
				p.isTotalTypeNode = true;
				codeStream.dup();
				generateArguments(p.accessorMethod, null, currentScope, codeStream);
				codeStream.invoke(Opcodes.OPC_invokevirtual, p.accessorMethod, this.resolvedType, null);
				p.generateCode(currentScope, codeStream);
			}
		}
		if (this.typePattern == null || this.typePattern.getPatternVariable() == null) {
			codeStream.pop();
		} else {
			this.typePattern.isTotalTypeNode = true;
			this.typePattern.generateCode(currentScope, codeStream);
		}
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		for (Pattern p : this.patterns) {
			visitor.visit(p, scope);
		}
		if (visitor.visit(this, scope)) {
			if (this.typePattern != null)
				this.typePattern.traverse(visitor, scope);
			else if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			for (Pattern p : this.patterns) {
				p.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append(this.type).append('(');
		if (this.patterns != null) {
			for (int i = 0; i < this.patterns.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.patterns[i].print(0, output);
			}
		}
		output.append(')');
		if (this.typePattern != null)
			output.append(' ').append(this.typePattern.local.name);
		return output;
	}
}
