/*
 * Created on 2004-03-11
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.jdt.internal.compiler.ast;

public class MarkerAnnotation extends Annotation {
	
	public MarkerAnnotation(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
	}
	
	public MarkerAnnotation(char[] token, long sourcePosition) {
		this.tokens = new char[][] { token };
		this.sourcePositions = new long[] { sourcePosition };
	}
}
