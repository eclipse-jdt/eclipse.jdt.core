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
package org.eclipse.jdt.core.internal.dom.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.Position;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

/* package */ class ASTRewriteFormatter {

	public static class NodeMarker extends Position {
		public Object data;
	}
		
	private class ExtendedFlattener extends ASTRewriteFlattener {

		private ArrayList fPositions;

		public ExtendedFlattener(RewriteEventStore store) {
			super(store);
			fPositions= new ArrayList();
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(ASTNode)
		 */
		public void preVisit(ASTNode node) {
			Object trackData= fEventStore.getTrackedNodeData(node);
			if (trackData != null) {
				addMarker(trackData, fResult.length(), 0);
			}
			Object placeholderData= fPlaceholders.getPlaceholderData(node);
			if (placeholderData != null) {
				addMarker(placeholderData, fResult.length(), 0);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#postVisit(ASTNode)
		 */
		public void postVisit(ASTNode node) {
			Object placeholderData= fPlaceholders.getPlaceholderData(node);
			if (placeholderData != null) {
				fixupLength(placeholderData, fResult.length());
			}
			Object trackData= fEventStore.getTrackedNodeData(node);
			if (trackData != null) {
				fixupLength(trackData, fResult.length());
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.corext.dom.ASTRewriteFlattener#visit(org.eclipse.jdt.core.dom.Block)
		 */
		public boolean visit(Block node) {
			if (fPlaceholders.isCollapsed(node)) {
				visitList(node, Block.STATEMENTS_PROPERTY, null);
				return false;
			}
			return super.visit(node);
		}
	
		private NodeMarker addMarker(Object annotation, int startOffset, int length) {
			NodeMarker marker= new NodeMarker();
			marker.offset= startOffset;
			marker.length= length;
			marker.data= annotation;
			fPositions.add(marker);
			return marker;
		}
	
		private void fixupLength(Object data, int endOffset) {
			for (int i= fPositions.size()-1; i >= 0 ; i--) {
				NodeMarker marker= (NodeMarker) fPositions.get(i);
				if (marker.data == data) {
					marker.length= endOffset - marker.offset;
					return;
				}
			}
		}

		public NodeMarker[] getMarkers() {
			return (NodeMarker[]) fPositions.toArray(new NodeMarker[fPositions.size()]);
		}
	}
	
	protected String fLineDelimiter;
	
	protected NodeInfoStore fPlaceholders;
	private RewriteEventStore fEventStore;

	private Map fOptions;
	
	public ASTRewriteFormatter(NodeInfoStore placeholders, RewriteEventStore eventStore, Map options, String lineDelimiter) {
		fPlaceholders= placeholders;
		fEventStore= eventStore;

		if (options == null) {
			options= JavaCore.getOptions();
		}
		fOptions= options;
		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, String.valueOf(9999));

		fLineDelimiter= lineDelimiter;
	}
	
	/**
	 * Returns the string accumulated in the visit formatted using the default formatter.
	 * Updates the existing node's positions.
	 *
	 * @param node The node to flatten.
	 * @param initialIndentationLevel The initial indentation level.
	 * @param resultingMarkers Resulting the updated NodeMarkers.
	 * @return Retuens the serialized and formatted code.
	 */	
	public String getFormattedResult(ASTNode node, int initialIndentationLevel, Collection resultingMarkers) {
		
		ExtendedFlattener flattener= new ExtendedFlattener(fEventStore);
		node.accept(flattener);

		NodeMarker[] markers= flattener.getMarkers();
		for (int i= 0; i < markers.length; i++) {
			resultingMarkers.add(markers[i]); // add to result
		}		
		
		String unformatted= flattener.getResult();
		TextEdit edit= CodeFormatterUtil.format2(node, unformatted, initialIndentationLevel, fLineDelimiter, fOptions);
		if (edit == null) {
			return unformatted;
		}
		
		return CodeFormatterUtil.evaluateFormatterEdit(unformatted, edit, markers);
	}
	
	public static interface Prefix {
		String getPrefix(int indent, String lineDelim);
	}
	
	public static interface BlockContext {
		String[] getPrefixAndSuffix(int indent, String lineDelim, ASTNode node, RewriteEventStore events);
	}	
	
	public static class ConstPrefix implements Prefix {
		private String fPrefix;
		
		public ConstPrefix(String prefix) {
			fPrefix= prefix;
		}
		
		public String getPrefix(int indent, String lineDelim) {
			return fPrefix;
		}
	}
	
	private class FormattingPrefix implements Prefix {
		private int fKind;
		private String fString;
		private int fStart;
		private int fLength;
		
		public FormattingPrefix(String string, String sub, int kind) {
			fStart= string.indexOf(sub);
			fLength= sub.length();
			fString= string;
			fKind= kind;
		}
		
		public String getPrefix(int indent, String lineDelim) {
			Position pos= new Position(fStart, fLength);
			String str= fString;
			TextEdit res= CodeFormatterUtil.format2(fKind, str, indent, lineDelim, fOptions);
			if (res != null) {
				str= CodeFormatterUtil.evaluateFormatterEdit(str, res, new Position[] { pos });
			}
			return str.substring(pos.offset + 1, pos.offset + pos.length - 1);
		}
	}

	private class BlockFormattingPrefix implements BlockContext {
		private String fPrefix;
		private int fStart;
		
		public BlockFormattingPrefix(String prefix, int start) {
			fStart= start;
			fPrefix= prefix;
		}
		
		public String[] getPrefixAndSuffix(int indent, String lineDelim, ASTNode node, RewriteEventStore events) {
			String nodeString= ASTRewriteFlattener.asString(node, events);
			String str= fPrefix + nodeString;
			Position pos= new Position(fStart, fPrefix.length() + 1 - fStart);

			TextEdit res= CodeFormatterUtil.format2(CodeFormatter.K_STATEMENTS, str, indent, lineDelim, fOptions);
			if (res != null) {
				str= CodeFormatterUtil.evaluateFormatterEdit(str, res, new Position[] { pos });
			}
			return new String[] { str.substring(pos.offset + 1, pos.offset + pos.length - 1), ""}; //$NON-NLS-1$
		}
	}
	
	private class BlockFormattingPrefixSuffix implements BlockContext {
		private String fPrefix;
		private String fSuffix;
		private int fStart;
		
		public BlockFormattingPrefixSuffix(String prefix, String suffix, int start) {
			fStart= start;
			fSuffix= suffix;
			fPrefix= prefix;
		}
		
		public String[] getPrefixAndSuffix(int indent, String lineDelim, ASTNode node, RewriteEventStore events) {
			String nodeString= ASTRewriteFlattener.asString(node, events);
			int nodeStart= fPrefix.length();
			int nodeEnd= nodeStart + nodeString.length() - 1;
			
			String str= fPrefix + nodeString + fSuffix;
			
			Position pos1= new Position(fStart, nodeStart + 1 - fStart);
			Position pos2= new Position(nodeEnd, 2);

			TextEdit res= CodeFormatterUtil.format2(CodeFormatter.K_STATEMENTS, str, indent, lineDelim, fOptions);
			if (res != null) {
				str= CodeFormatterUtil.evaluateFormatterEdit(str, res, new Position[] { pos1, pos2 });
			}
			return new String[] {
				str.substring(pos1.offset + 1, pos1.offset + pos1.length - 1),
				str.substring(pos2.offset + 1, pos2.offset + pos2.length - 1)
			};
		}
	}	
	
	public final static Prefix NONE= new ConstPrefix(""); //$NON-NLS-1$
	public final static Prefix SPACE= new ConstPrefix(" "); //$NON-NLS-1$
	public final static Prefix ASSERT_COMMENT= new ConstPrefix(" : "); //$NON-NLS-1$
	
	public final Prefix VAR_INITIALIZER= new FormattingPrefix("A a={};", "a={" , CodeFormatter.K_STATEMENTS); //$NON-NLS-1$ //$NON-NLS-2$
	public final Prefix METHOD_BODY= new FormattingPrefix("void a() {}", ") {" , CodeFormatter.K_CLASS_BODY_DECLARATIONS); //$NON-NLS-1$ //$NON-NLS-2$
	public final Prefix FINALLY_BLOCK= new FormattingPrefix("try {} finally {}", "} finally {", CodeFormatter.K_STATEMENTS); //$NON-NLS-1$ //$NON-NLS-2$
	public final Prefix CATCH_BLOCK= new FormattingPrefix("try {} catch(Exception e) {}", "} c" , CodeFormatter.K_STATEMENTS); //$NON-NLS-1$ //$NON-NLS-2$

	public final BlockContext IF_BLOCK_WITH_ELSE= new BlockFormattingPrefixSuffix("if (true)", "else{}", 8); //$NON-NLS-1$ //$NON-NLS-2$
	public final BlockContext IF_BLOCK_NO_ELSE= new BlockFormattingPrefix("if (true)", 8); //$NON-NLS-1$ //$NON-NLS-2$
	public final BlockContext ELSE_AFTER_STATEMENT= new BlockFormattingPrefix("if (true) foo(); else ", 15); //$NON-NLS-1$ //$NON-NLS-2$
	public final BlockContext ELSE_AFTER_BLOCK= new BlockFormattingPrefix("if (true) {} else ", 11); //$NON-NLS-1$ //$NON-NLS-2$

	public final BlockContext FOR_BLOCK= new BlockFormattingPrefix("for (;;) ", 7); //$NON-NLS-1$ //$NON-NLS-2$
	public final BlockContext WHILE_BLOCK= new BlockFormattingPrefix("while (true)", 11); //$NON-NLS-1$ //$NON-NLS-2$
	public final BlockContext DO_BLOCK= new BlockFormattingPrefixSuffix("do ", "while (true);", 1); //$NON-NLS-1$ //$NON-NLS-2$

}
