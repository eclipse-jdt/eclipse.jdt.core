/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.internal.core.SourceRange;

/**
 * Utility class to map comments to ASTNodes. Workaround until we get the core support.
 * ASTnodes get extended source ranges that include all comments mapped to the node.
 * The extended ranges are stored in the property fields by <code>annotateExtraRanges</code> which
 * has to be called first. 
 * To access the extended ranges use <code>getExtendedOffset</code>, <code>getExtendedEnd</code> and
 * <code>getExtendedLength</code>.
 */
public class CommentMapper {
	
	public static final String NODE_RANGE_PROPERTY= "noderange"; //$NON-NLS-1$
	
	
	public static int getExtendedOffset(ASTNode node) {
		ISourceRange range= (ISourceRange) node.getProperty(NODE_RANGE_PROPERTY);
		if (range != null) {
			return range.getOffset();
		}
		return node.getStartPosition();
	}
	
	public static int getExtendedEnd(ASTNode node) {
		ISourceRange range= (ISourceRange) node.getProperty(NODE_RANGE_PROPERTY);
		if (range != null) {
			return range.getOffset() + range.getLength();
		}
		return node.getStartPosition() + node.getLength();
	}
	
	public static int getExtendedLength(ASTNode node) {
		ISourceRange range= (ISourceRange) node.getProperty(NODE_RANGE_PROPERTY);
		if (range != null) {
			return range.getLength();
		}
		return node.getLength();
	}
	
	/**
	 * Annotates all node that have extended ranges with a ISourceRange in
	 * the NODE_RANGE_PROPERTY property 
	 * @param node A node from the tree. The root compilation unit mist be reachable
	 * through this node.
	 * @param scanner The scanner initialized to the source corresponding to the node.
	 */
	public static void annotateExtraRanges(ASTNode node, TokenScanner scanner) {
		ASTNode astRoot= node.getRoot();
		
		// annotate the full cu (preliminary solution until the code goes to jdt.core.
		if (astRoot.getProperty(NODE_RANGE_PROPERTY) != null) {
			return;
		}
		
		// mark the cu with a rnage so we know it's already annotated
		astRoot.setProperty(NODE_RANGE_PROPERTY, new SourceRange(node.getStartPosition(), node.getLength()));
		doExtraRangesForChildren(astRoot, scanner);
	}
	
	private static void doExtraRangesForChildren(ASTNode node, TokenScanner scanner) {
		List children= CommentMapper.getChildren(node);
		
		int lastChild= children.size() - 1;
		int lastPos= node.getStartPosition() + node.getLength();
		
		int endOfLast= node.getStartPosition();
		for (int i= 0; i <= lastChild; i++) {
			ASTNode curr= (ASTNode) children.get(i);
			if (curr.getStartPosition() != -1) {
				int beginOfNext= getNextExistingOffset(children, i, lastPos);
				endOfLast= annotateNode(curr, endOfLast, beginOfNext, scanner);
				doExtraRangesForChildren(curr, scanner);
			}
		}
	}
	
	/* workaround, deals with finding positions in modified ASTs. */
	private static int getNextExistingOffset(List children, int idx, int def) {
		for (int i= idx + 1; i < children.size(); i++) {
			ASTNode curr= (ASTNode) children.get(i);
			if (curr.getStartPosition() != -1) {
				return curr.getStartPosition();
			}
		}
		return def;
	}
	
			
	private static int annotateNode(ASTNode node, int prevEnd, int nextStart, TokenScanner scanner) {
		int tokenStart= node.getStartPosition();
		int tokenLength= node.getLength();
		try {
			int start= scanner.getTokenCommentStart(prevEnd, tokenStart);
			int length= scanner.getTokenCommentEnd(tokenStart + tokenLength, nextStart) - start;
			if (start != tokenStart || length != tokenLength) {
				node.setProperty(NODE_RANGE_PROPERTY, new SourceRange(start, length));
			}
			return start + length;
		} catch (CoreException e) {
			//JavaPlugin.log(e);
			// log, no extra range annotated
		}
		return tokenStart + tokenLength;
	}
	
	/**
	 * Returns a list of the direct chidrens of a node. The siblings are ordered by start offset.
	 * @param node
	 * @return
	 */    
	private static List getChildren(ASTNode node) {
		ChildrenCollector visitor= new ChildrenCollector();
		node.accept(visitor);
		return visitor.result;		
	}
	
	private static class ChildrenCollector extends GenericVisitor {
		public List result;

		public ChildrenCollector() {
			result= null;
		}
		protected boolean visitNode(ASTNode node) {
			if (result == null) { // first visitNode: on the node's parent: do nothing, return true
				result= new ArrayList();
				return true;
			}
			result.add(node);
			return false;
		}
	}
}
