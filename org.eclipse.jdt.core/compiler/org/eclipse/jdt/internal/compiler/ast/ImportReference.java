package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ImportReference extends AstNode {
	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public boolean onDemand = true; //most of the time

	public int declarationSourceStart;
	public int declarationSourceEnd;

public ImportReference(char[][] sources , long[] poss , boolean d) {
	tokens = sources ;
	sourcePositions = poss ;
	onDemand = d;
	sourceEnd = (int)(sourcePositions[sourcePositions.length-1] & 0x00000000FFFFFFFF);
	sourceStart = (int)(sourcePositions[0]>>>32) ;
}
/**
 * @return char[][]
 */
public char[][] getImportName() {
	return tokens;
}
public String toString(int tab ){

	return toString(tab,true);}
public String toString(int tab, boolean withOnDemand) {
	/* when withOnDemand is false, only the name is printed */
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		if (i < (tokens.length - 1)) {
			buffer.append("."/*nonNLS*/);
		}
	}
	if (withOnDemand && onDemand) {
		buffer.append(".*"/*nonNLS*/);
	}
	return buffer.toString();
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, CompilationUnitScope scope) {
	visitor.visit(this, scope);
}
}
