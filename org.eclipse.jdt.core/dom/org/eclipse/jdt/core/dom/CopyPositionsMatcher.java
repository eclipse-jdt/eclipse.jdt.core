/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.dom;

class CopyPositionsMatcher extends ASTMatcher {

	private void copyPositions(ASTNode source, ASTNode destination) {
		destination.setSourceRange(source.getStartPosition(), source.getLength());
	}
	
	/*
	 * @see ASTMatcher#match(ArrayType, Object)
	 */
	public boolean match(ArrayType node, Object other) {
		copyPositions(node, (ASTNode) other);
		return super.match(node, other);
	}

	/*
	 * @see ASTMatcher#match(QualifiedName, Object)
	 */
	public boolean match(QualifiedName node, Object other) {
		copyPositions(node, (ASTNode) other);
		return super.match(node, other);
	}

	/*
	 * @see ASTMatcher#match(SimpleName, Object)
	 */
	public boolean match(SimpleName node, Object other) {
		copyPositions(node, (ASTNode) other);
		return super.match(node, other);
	}

	/*
	 * @see ASTMatcher#match(SimpleType, Object)
	 */
	public boolean match(SimpleType node, Object other) {
		copyPositions(node, (ASTNode) other);
		return super.match(node, other);
	}

	/*
	 * @see ASTMatcher#match(PrimitiveType, Object)
	 */
	public boolean match(PrimitiveType node, Object other) {
		copyPositions(node, (ASTNode) other);
		return super.match(node, other);
	}

}
