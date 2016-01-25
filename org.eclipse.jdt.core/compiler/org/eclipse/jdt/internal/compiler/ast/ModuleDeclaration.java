/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ModuleDeclaration extends TypeDeclaration {

	public ExportReference[] exports;
	public ModuleReference[] requires;
	public TypeReference[] uses;
	public TypeReference[] interfaces;
	public TypeReference[] implementations;
	public int exportsCount;
	public int requiresCount;
	public int usesCount;
	public int servicesCount;

	public char[][] tokens;
	public char[] moduleName;
	public long[] sourcePositions;

//	public int declarationSourceStart;
//	public int declarationSourceEnd;
//	public int bodyStart;
//	public int bodyEnd; // doesn't include the trailing comment if any.
//	public CompilationResult compilationResult;
	
	public ModuleDeclaration(CompilationResult compilationResult, char[][] tokens, long[] positions) {
		super(compilationResult);
		this.compilationResult = compilationResult;
		this.exportsCount = 0;
		this.requiresCount = 0;
		this.tokens = tokens;
		this.moduleName = CharOperation.concatWith(tokens, '.');
		this.sourcePositions = positions;
		this.sourceEnd = (int) (positions[positions.length-1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (positions[0] >>> 32);
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public StringBuffer printStatement(int indent, StringBuffer output) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resolve() {
		//
		if (this.binding == null) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
		this.binding.compoundName = CharOperation.arrayConcat(this.tokens, this.name);
		for(int i = 0; i < this.usesCount; i++) {
			TypeReference ref = this.uses[i];
			TypeBinding type = ref.resolveType(this.binding.scope);
			if (type == null || !type.isValidBinding()) {
				this.ignoreFurtherInvestigation = true;
			}
		}
		for(int i = 0; i < this.servicesCount; i++) {
			TypeBinding inf = this.interfaces[i].resolveType(this.binding.scope);
			TypeBinding imp = this.implementations[i].resolveType(this.binding.scope);
			if (inf == null || !inf.isValidBinding() || imp == null || !imp.isValidBinding()) {
				this.ignoreFurtherInvestigation = true;
			}
		}
	}

	public StringBuffer printHeader(int indent, StringBuffer output) {
		output.append("module "); //$NON-NLS-1$
		output.append(CharOperation.charToString(this.moduleName));
		return output;
	}
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(" {"); //$NON-NLS-1$
		if (this.requires != null) {
			for(int i = 0; i < this.requiresCount; i++) {
				output.append('\n');
				printIndent(indent + 1, output);
				output.append("requires "); //$NON-NLS-1$
				this.requires[i].print(0, output);
				output.append(";"); //$NON-NLS-1$
			}
		}
		if (this.exports != null) {
			for(int i = 0; i < this.exportsCount; i++) {
				output.append('\n');
				this.exports[i].print(indent + 1, output);
			}
		}
		if (this.uses != null) {
			for(int i = 0; i < this.usesCount; i++) {
				output.append('\n');
				printIndent(indent + 1, output);
				output.append("uses "); //$NON-NLS-1$
				this.uses[i].print(0, output);
				output.append(";"); //$NON-NLS-1$
			}
		}
		if (this.servicesCount != 0) {
			for(int i = 0; i < this.servicesCount; i++) {
				output.append('\n');
				printIndent(indent + 1, output);
				output.append("provides "); //$NON-NLS-1$
				this.interfaces[i].print(0, output);
				output.append('\n');
				printIndent(indent + 2, output);
				output.append("with "); //$NON-NLS-1$
				this.implementations[i].print(0, output);
				output.append(";"); //$NON-NLS-1$
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}
