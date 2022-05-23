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

import java.util.function.Consumer;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class RecordPattern extends Pattern {

	public Pattern[] patterns;
	public TypePattern typePattern;
	public TypeReference type;

	public RecordPattern(LocalDeclaration local) {
		this.typePattern = new TypePattern(local);
		this.type = local.type;
		this.sourceStart = local.sourceStart;
		this.sourceEnd = local.sourceEnd;
	}
	public RecordPattern(TypeReference type, int sourceStart, int sourceEnd) {
		this.type = type;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	@Override
	public TypeReference getType() {
		return this.type;
	}
	public void runFunctionOnPatterns(Consumer<Pattern> func) {
		if (this.patterns != ASTNode.NO_TYPE_PATTERNS) {
			for (Pattern p : this.patterns) {
				func.accept(p);
			}
		}

	}
 	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		if (this.resolvedType == null) {
			this.resolveType(scope);
		}
		if (this.typePattern != null && this.typePattern.local.binding != null) {
			if (this.patternVarsWhenTrue == null) {
				this.patternVarsWhenTrue = new LocalVariableBinding[1];
				this.patternVarsWhenTrue[0] = this.typePattern.local.binding;
			} else {
				this.addPatternVariablesWhenTrue(new LocalVariableBinding[] {this.typePattern.local.binding});
			}
		}
		runFunctionOnPatterns((p) -> {
			p.collectPatternVariablesToScope(this.patternVarsWhenTrue, scope);
			this.addPatternVariablesWhenTrue(p.patternVarsWhenTrue);
		});
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
			runFunctionOnPatterns((p) -> {
				// p.analyseCode(currentScope, flowContext, flowInfo);
			});
		}
		return flowInfo;
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
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null)
			return this.resolvedType;
		runFunctionOnPatterns((p) -> {
			p.resolveType(scope);
		});
		if (this.typePattern != null) {
			this.resolvedType = this.typePattern.resolveType(scope);
		} else {
			this.resolvedType = this.type.resolveType(scope);
		}
		return this.resolvedType;
	}
	@Override
	public boolean dominates(Pattern p) {
		return isTotalForType(p.resolvedType);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if (this.typePattern != null) {
			this.typePattern.generateCode(currentScope, codeStream);
		} else {
			this.type.generateCode(currentScope, codeStream);
		}
		runFunctionOnPatterns((p) -> {
			//p.generateCode(currentScope, codeStream);
		});
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
