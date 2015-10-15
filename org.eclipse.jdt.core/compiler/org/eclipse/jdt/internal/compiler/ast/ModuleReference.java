package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class ModuleReference extends ASTNode {
	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers = ClassFileConstants.AccDefault;
	public int modifiersSourceStart;

	public ModuleReference(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length - 1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
	}
	
	public boolean isPublic() {
		return (this.modifiers & ClassFileConstants.AccPublic) != 0;
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
