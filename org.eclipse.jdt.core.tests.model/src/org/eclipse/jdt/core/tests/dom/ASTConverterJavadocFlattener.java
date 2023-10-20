/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

@SuppressWarnings("rawtypes")
public class ASTConverterJavadocFlattener extends ASTVisitor {

	/**
	 * The string buffer into which the serialized representation of the AST is
	 * written.
	 */
	private final StringBuilder buffer;
	private final int indent = 0;
	private final String comment;

/**
 * Creates a new AST printer.
 */
ASTConverterJavadocFlattener(String comment) {
	this.buffer = new StringBuilder();
	this.comment = comment;
}

/**
 * @deprecated
 */
private Type componentType(ArrayType array) {
	return array.getComponentType();
}

/**
 * Returns the string accumulated in the visit.
 *
 * @return the serialized
 */
public String getResult() {
	return this.buffer.toString();
}

/**
 * Resets this printer so that it can be used again.
 */
public void reset() {
	this.buffer.setLength(0);
}

/**
 * @see ASTVisitor#visit(ArrayType)
 * @deprecated
 */
public boolean visit(ArrayType node) {
	if (node.getAST().apiLevel() < AST.JLS8) {
		componentType(node).accept(this);
		this.buffer.append("[]");//$NON-NLS-1$
	} else {
		node.getElementType().accept(this);
		int noOfDimensions = node.getDimensions();
		List dimensions = node.dimensions();
		for (int i = 0;  i < noOfDimensions; ++i) {
			((Dimension) dimensions.get(i)).accept(this);
		}
	}
	return false;
}

/*
 * @see ASTVisitor#visit(BlockComment)
 * @since 3.0
 */
public boolean visit(BlockComment node) {
	this.buffer.append(this.comment);
	return false;
}

/*
 * @see ASTVisitor#visit(Javadoc)
 */
public boolean visit(Javadoc javadoc) {
	printIndent();
	this.buffer.append("/**");//$NON-NLS-1$
	for (Iterator it = javadoc.tags().iterator(); it.hasNext(); ) {
		ASTNode e = (ASTNode) it.next();
		e.accept(this);
	}
	this.buffer.append("\n */\n");//$NON-NLS-1$
	return false;
}

private void printIndent() {
	for (int i=0; i<this.indent; i++) {
		this.buffer.append('\t');
	}
}

/*
private void printNewLine() {
	buffer.append('\n');
	printIndent();
	buffer.append(" * ");
}
*/

/*
 * @see ASTVisitor#visit(LineComment)
 * @since 3.0
 */
public boolean visit(LineComment node) {
	this.buffer.append(this.comment);
	return false;
}

/*
 * @see ASTVisitor#visit(MemberRef)
 * @since 3.0
 */
public boolean visit(MemberRef node) {
	if (node.getQualifier() != null) {
		node.getQualifier().accept(this);
	}
	this.buffer.append("#");//$NON-NLS-1$
	node.getName().accept(this);
	return true;
}

/*
 * @see ASTVisitor#visit(MethodRef)
 * @since 3.0
 */
public boolean visit(MethodRef node) {
	if (node.getQualifier() != null) {
		node.getQualifier().accept(this);
	}
	this.buffer.append("#");//$NON-NLS-1$
	node.getName().accept(this);
	this.buffer.append("(");//$NON-NLS-1$
	for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
		MethodRefParameter e = (MethodRefParameter) it.next();
		e.accept(this);
		if (it.hasNext()) {
			this.buffer.append(",");//$NON-NLS-1$
		}
	}
	this.buffer.append(")");//$NON-NLS-1$
	return true;
}

/*
 * @see ASTVisitor#visit(MethodRefParameter)
 * @since 3.0
 */
public boolean visit(MethodRefParameter node) {
	node.getType().accept(this);
	if (node.getName() != null) {
		this.buffer.append(" ");//$NON-NLS-1$
		node.getName().accept(this);
	}
	return true;
}

/*
 * @see ASTVisitor#visit(TagElement)
 * @since 3.0
 */
public boolean visit(TagElement node) {
	if (node.isNested()) {
		// nested tags are always enclosed in braces
		this.buffer.append("{");//$NON-NLS-1$
	} else {
		// top-level tags always begin on a new line
		this.buffer.append("\n * ");//$NON-NLS-1$
	}
	boolean previousRequiresWhiteSpace = false;
	if (node.getTagName() != null) {
		this.buffer.append(node.getTagName());
		previousRequiresWhiteSpace = true;
	}
	boolean previousRequiresNewLine = false;
	for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
		ASTNode e = (ASTNode) it.next();
		// assume text elements include necessary leading and trailing whitespace
		// but Name, MemberRef, MethodRef, and nested TagElement do not include white space
		boolean currentIncludesWhiteSpace = (e instanceof TextElement);
		if (previousRequiresNewLine && currentIncludesWhiteSpace) {
			this.buffer.append("\n * ");//$NON-NLS-1$
		}
		previousRequiresNewLine = currentIncludesWhiteSpace;
		// add space if required to separate
		if (previousRequiresWhiteSpace && !currentIncludesWhiteSpace) {
			this.buffer.append(" "); //$NON-NLS-1$
		}
		e.accept(this);
		previousRequiresWhiteSpace = !currentIncludesWhiteSpace && !(e instanceof TagElement);
	}
	if (node.isNested()) {
		this.buffer.append("}");//$NON-NLS-1$
	}
	return false;
}

/*
 * @see ASTVisitor#visit(TextElement)
 * @since 3.0
 */
public boolean visit(TextElement node) {
	this.buffer.append(node.getText());
	return false;
}

/*
 * @see ASTVisitor#visit(PrimitiveType)
 */
public boolean visit(PrimitiveType node) {
	this.buffer.append(node.getPrimitiveTypeCode().toString());
	return false;
}

/*
 * @see ASTVisitor#visit(QualifiedName)
 */
public boolean visit(QualifiedName node) {
	node.getQualifier().accept(this);
	this.buffer.append(".");//$NON-NLS-1$
	node.getName().accept(this);
	return false;
}

/*
 * @see ASTVisitor#visit(SimpleName)
 */
public boolean visit(SimpleName node) {
	this.buffer.append(node.getIdentifier());
	return false;
}

/*
 * @see ASTVisitor#visit(SimpleName)
 */
public boolean visit(SimpleType node) {
	node.getName().accept(this);
	return false;
}
}
