/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.ISourceModifier;
import org.eclipse.text.edits.ReplaceEdit;


public class SourceModifier implements ISourceModifier {
	
	private final String destinationIndent;
	private final int sourceIndentLevel;
	private final int tabWidth;
		
	public SourceModifier(int sourceIndentLevel, String destinationIndent, int tabWidth) {
		this.destinationIndent= destinationIndent;
		this.sourceIndentLevel= sourceIndentLevel;
		this.tabWidth= tabWidth;
	}
		
	public ISourceModifier copy() {
		// We are state less
		return this;
	}
	
	public ReplaceEdit[] getModifications(String source) {
		List result= new ArrayList();
		int destIndentLevel= Indents.computeIndent(this.destinationIndent, this.tabWidth);
		if (destIndentLevel == this.sourceIndentLevel) {
			return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
		}
		return Indents.getChangeIndentEdits(source, this.sourceIndentLevel, this.tabWidth, this.destinationIndent);
	}
}
