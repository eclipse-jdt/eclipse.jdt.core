package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class ModuleDeclaration extends Statement {

	public ExportReference[] exports;
	public ModuleReference[] requires;
	public TypeReference[] uses;
	public TypeReference[] interfaces;
	public TypeReference[] implementations;
	public int exportsCount;
	public int requiresCount;
	public int usesCount;
	public int servicesCount;

	public char[][] moduleName;
	public long[] sourcePositions;

	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int bodyStart;
	public int bodyEnd; // doesn't include the trailing comment if any.
	public CompilationResult compilationResult;
	
	public ModuleDeclaration(CompilationResult compilationResult, char[][] tokens, long[] positions) {
		this.compilationResult = compilationResult;
		this.exportsCount = 0;
		this.requiresCount = 0;
		this.moduleName = tokens;
		this.sourcePositions = positions;
		this.sourceEnd = (int) (positions[positions.length-1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (positions[0] >>> 32);
	}

	public boolean isModule() {
		return true;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StringBuffer printStatement(int indent, StringBuffer output) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resolve(BlockScope scope) {
		// TODO Auto-generated method stub
		
	}
}
