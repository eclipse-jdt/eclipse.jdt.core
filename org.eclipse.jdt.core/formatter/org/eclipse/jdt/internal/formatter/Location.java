/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.formatter;

/**
 * A location maintains positional information both in original source and in the output source.
 * It remembers source offsets, line/column and indentation level.
 * @since 2.1
 */
public class Location {

	public int inputOffset;
	public int outputLine;
	public int outputColumn;
	public int outputOffset;
	public int outputIndentationLevel;
	public boolean needSpace;

	// chunk management
	public int lastNumberOfNewLines;	
	
	// position mapping management
	int positionsIndex;
	
	public Location(Scribe scribe, int sourceRestart){
		update(scribe, sourceRestart);
	}
	
	public void update(Scribe scribe, int sourceRestart){
		this.outputColumn = scribe.column;
		this.outputLine = scribe.line;
		this.outputOffset = scribe.size();
		this.inputOffset = sourceRestart;
		this.outputIndentationLevel = scribe.indentationLevel;
		this.lastNumberOfNewLines = scribe.lastNumberOfNewLines;
		this.needSpace = scribe.needSpace;
		this.positionsIndex = scribe.positionsIndex;
	}
}
