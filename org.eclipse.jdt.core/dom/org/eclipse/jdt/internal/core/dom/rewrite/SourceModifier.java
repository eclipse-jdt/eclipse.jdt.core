/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.ISourceModifier;
import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;


public class SourceModifier implements ISourceModifier {
	
	private String fDestinationIndent;
	private int fSourceIndentLevel;
	private int fTabWidth;
		
	public SourceModifier(int sourceIndentLevel, String destinationIndent, int tabWidth) {
		super();
		fDestinationIndent= destinationIndent;
		fSourceIndentLevel= sourceIndentLevel;
		fTabWidth= tabWidth;
	}
	
	public static SourceModifier createCopyModifier(int sourceIndentLevel, String destIndentString, int tabWidth) {
		return new SourceModifier(sourceIndentLevel, destIndentString, tabWidth);
	}
		
	public static SourceModifier createMoveModifier(int sourceIndentLevel, String destIndentString, int tabWidth) {
		return new SourceModifier(sourceIndentLevel, destIndentString, tabWidth);
	}
	
	public ISourceModifier copy() {
		// We are state less
		return this;
	}
	
	public ReplaceEdit[] getModifications(String source) {
		List result= new ArrayList();
		int destIndentLevel= Strings.computeIndent(fDestinationIndent, fTabWidth);
		if (destIndentLevel == fSourceIndentLevel) {
			return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
		}
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(source);
			int nLines= tracker.getNumberOfLines();
			if (nLines == 1)
				return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
			for (int i= 1; i < nLines; i++) {
				IRegion region= tracker.getLineInformation(i);
				int offset= region.getOffset();
				String line= source.substring(offset, offset + region.getLength());
				int length= Strings.computeIndentLength(line, fSourceIndentLevel, fTabWidth);
				if (length >= 0) {
					result.add(new ReplaceEdit(offset, length, fDestinationIndent));
				} else {
					length= Strings.computeIndent(line, fTabWidth);
					result.add(new ReplaceEdit(offset, length, "")); //$NON-NLS-1$
				}
			}
		} catch (BadLocationException cannotHappen) {
			// can not happen
		}
		return (ReplaceEdit[])result.toArray(new ReplaceEdit[result.size()]);
	}
}
