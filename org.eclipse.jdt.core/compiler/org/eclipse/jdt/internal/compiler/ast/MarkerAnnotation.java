/*
 * Created on 2004-03-11
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.jdt.internal.compiler.ast;

public class MarkerAnnotation extends Annotation {
	
	public MarkerAnnotation(char[][] tokens, long[] sourcePositions, int sourceStart) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceStart = sourceStart;
		this.sourceEnd = (int) sourcePositions[sourcePositions.length - 1];
	}
	
	public MarkerAnnotation(char[] token, long sourcePosition, int sourceStart) {
		this.tokens = new char[][] { token };
		this.sourcePositions = new long[] { sourcePosition };
		this.sourceStart = sourceStart;
		this.sourceEnd = (int) sourcePosition;
	}
}
