package org.eclipse.jdt.internal.compiler.ast;

public class ExportReference extends ASTNode {
	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public ImportReference[] targets;

	public ExportReference(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length - 1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
	}
	
	public boolean isTargeted() {
		return this.targets != null && this.targets.length > 0;
	}
	
	public ImportReference[] getTargetedModules() {
		return this.targets;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output;
	}

}
