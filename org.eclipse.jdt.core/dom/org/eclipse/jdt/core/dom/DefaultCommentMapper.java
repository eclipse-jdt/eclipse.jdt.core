/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Internal class for associating comments with AST nodes.
 * 
 * @since 3.0
 */
class DefaultCommentMapper {
	private Comment[] comments;
	private Map leadingComments;
	private Map trailingComments;
	
	private int commentIndex;

	/**
	 * @param table the given table of comments
	 */
	DefaultCommentMapper(Comment[] table) {
		this.comments = table;
	}

	/**
	 * Get comment of the list which includes a given position
	 * 
	 * @param position The position belonging to the looked up comment
	 * @return comment which includes the given position or null if none was found
	 */
	Comment getComment(int position) {

		if (this.comments == null) {
			return null;
		}
		int size = this.comments.length;
		if (size == 0) {
			return null;
		}
		int index = getCommentIndex(0, position, 0);
		if (index<0) {
			return null;
		}
		return this.comments[index];
	}

	/*
	 * Get the index of comment which contains given position.
	 * If there's no matching comment, then return depends on exact parameter:
	 *		= 0: return -1
	 *		< 0: return index of the comment before the given position
	 *		> 0: return index of the comment after the given position
	 */
	private int getCommentIndex(int start, int position, int exact) {
		if (position == 0) {
			if (this.comments.length > 0 && this.comments[0].getStartPosition() == 0) {
				return 0;
			}
			return -1;
		}
		int bottom = start, top = this.comments.length - 1;
		int i = 0, index = -1;
		Comment comment = null;
		while (bottom <= top) {
			i = (bottom + top) /2;
			comment = this.comments[i];
			int commentStart = comment.getStartPosition();
			if (position < commentStart) {
				top = i-1;
			} else if (position >=(commentStart+comment.getLength())) {
				bottom = i+1;
			} else {
				index = i;
				break;
			}
		}
		if (index<0 && exact!=0) {
			comment = this.comments[i];
			if (position < comment.getStartPosition()) {
				return exact<0 ? i-1 : i;
			} else {
				return exact<0 ? i : i+1;
			}
		}
		return index;
	}

	/**
	 * Return all leading comments of a given node.
	 * @param node
	 * @return an array of Comment or null if there's no leading comment
	 */
	Comment[] getLeadingComments(ASTNode node) {
		if (this.leadingComments != null) {
			int[] range = (int[]) this.leadingComments.get(node);
			if (range != null) {
				int length = range[1]-range[0]+1;
				Comment[] leadComments = new Comment[length];
				System.arraycopy(this.comments, range[0], leadComments, 0, length);
				return  leadComments;
			}
		}
		return null;
	}

	/**
	 * Return all trailing comments of a given node.
	 * @param node
	 * @return an array of Comment or null if there's no trailing comment
	 */
	Comment[] getTrailingComments(ASTNode node) {
		if (this.trailingComments != null) {
			int[] range = (int[]) this.trailingComments.get(node);
			if (range != null) {
				int length = range[1]-range[0]+1;
				Comment[] trailComments = new Comment[length];
				System.arraycopy(this.comments, range[0], trailComments, 0, length);
				return  trailComments;
			}
		}
		return null;
	}

	/**
	 * Returns the extended start position of the given node. Unlike
	 * {@link ASTNode#getStartPosition()} and {@link ASTNode#getLength()()},
	 * the extended source range may include comments and whitespace
	 * immediately before or after the normal source range for the node.
	 * 
	 * @param node the node
	 * @return the 0-based character index, or <code>-1</code>
	 *    if no source position information is recorded for this node
	 * @see #getExtendedLength(ASTNode)
	 * @since 3.0
	 */
	public int getExtendedStartPosition(ASTNode node) {
		if (this.leadingComments != null) {
			int[] range = (int[]) this.leadingComments.get(node);
			if (range != null) {
				return  this.comments[range[0]].getStartPosition() ;
			}
		}
		return node.getStartPosition();
	}

	/*
	 * Returns the extended end position of the given node.
	 */
	public int getExtendedEnd(ASTNode node) {
		int end = node.getStartPosition() + node.getLength();
		if (this.trailingComments != null) {
			int[] range = (int[]) this.trailingComments.get(node);
			if (range != null) {
				Comment lastComment = this.comments[range[1]];
				end = lastComment.getStartPosition() + lastComment.getLength();
			}
		}
		return end;
	}

	/**
	 * Returns the extended source length of the given node. Unlike
	 * {@link ASTNode#getStartPosition()} and {@link ASTNode#getLength()()},
	 * the extended source range may include comments and whitespace
	 * immediately before or after the normal source range for the node.
	 * 
	 * @param node the node
	 * @return a (possibly 0) length, or <code>0</code>
	 *    if no source position information is recorded for this node
	 * @see #getExtendedStartPosition(ASTNode)
	 * @see #getExtendedEnd(ASTNode)
	 * @since 3.0
	 */
	public int getExtendedLength(ASTNode node) {
		return getExtendedEnd(node) - getExtendedStartPosition(node);
	}

	/*
	 * Initialize leading and trailing comments tables in whole nodes hierarchy of a compilation
	 * unit.
	 * Scanner is necessary to scan between nodes and comments and verify if there's
	 * nothing else than white spaces.
	 */
	void initialize(CompilationUnit unit, Scanner scanner) {
		
		// Init comments
		this.comments = unit.optionalCommentTable;
		if (this.comments == null) {
			return;
		}
		int size = this.comments.length;
		if (size == 0) {
			return;
		}

		// Init tables
		this.leadingComments = new HashMap();
		this.trailingComments = new HashMap();
		
		// Init scanner and start ranges computing
		scanner.linePtr = scanner.lineEnds.length-1;
		scanner.tokenizeWhiteSpace = true;
		doExtraRangesForChildren(unit, scanner);
	}

	/*
	 * Compute extended ranges for children of a given node.
	 * Note that previous end for first child is naturally the node starting position
	 * end next start for last child is the first token after the end of the node which
	 * is neither a comment nor white spaces.
	 * 
	 * Compute first leading and trailing comment tables as this let us to optimize
	 * comment look up in the table. As all comments on a same level are ordered
	 * by position, we store the index to start the search from it instead of restarting
	 * each time from the beginning (see in storeLeadingComments and storeTrailingComments
	 * methods).
	 */
	private void doExtraRangesForChildren(ASTNode node, Scanner scanner) {
		// Compute node children
		List children= getChildren(node);
		int size = children.size() ;
		
		// Compute last next start and previous end. Next start is the starting position
		// of first token following node end which is neither a comment nor white spaces.
		int lastPos = getExtendedEnd(node);
		int previousEnd = node.getStartPosition();
		
		// Compute leading and trailing comments for all children nodes at this level
		this.commentIndex = 0;
		try {
			for (int i= 0; i < size; i++) {
				ASTNode current = (ASTNode) children.get(i);
				boolean lastChild = i==(size-1);
				int nextStart = lastChild ? lastPos : ((ASTNode) children.get(i+1)).getStartPosition();
				storeLeadingComments(current, previousEnd,scanner);
				previousEnd = storeTrailingComments(current, nextStart, scanner, lastChild);
			}
		}
		catch (Exception ex) {
			// Give up extended ranges at this level if unexpected exception happens...
		}
		
		// Compute extended ranges at sub-levels
		for (int i= 0; i < size; i++) {
			doExtraRangesForChildren((ASTNode) children.get(i), scanner);
		}
	}

	/**
	 * Search and store node leading comments. Comments are searched in position range
	 * from previous extended position to node start position. If one or several comment are found,
	 * returns first comment start position, otherwise returns node start position.
	 * 
	 * First look after first comment before node start position using global comment index
	 * to reduce range of search. Obviously returns if no comment is found before the node...
	 * @see #doExtraRangesForChildren(ASTNode, Scanner)
	 *
	 * When first comment was found before node, goes up in comment list until one of
	 * following condition becomes true:
	 * 	1) comment end is before previous end
	 * 	2) comment start and previous end is on the same line but not on same line of node start
	 * 	3) there's other than white characters between current node and comment
	 * 	4) there's more than 1 line between current node and comment
	 * 
	 * If at least one potential comment has been found, then no token should be on
	 * on the same line before, so remove all comments which do not verify this assumption.
	 * 
	 * If finally there is a subset of comments, then store start and end indexes 
	 * in leading comments table.
	 */
	int storeLeadingComments(ASTNode node, int previousEnd, Scanner scanner) {
		// Init extended position
		int nodeStart = node.getStartPosition();
		int extended = nodeStart;
		
		// Get line of node start position
		int previousEndLine = scanner.getLineNumber(previousEnd);
		int nodeStartLine = scanner.getLineNumber(nodeStart);
		
		// Find first comment index
		int idx = getCommentIndex(this.commentIndex, nodeStart, -1);
		if (idx == -1) {
			return nodeStart;
		}
		
		// Look after potential comments
		int startIdx = -1;
		int endIdx = idx;
		int previousStart = nodeStart;
		while (idx >= 0 && previousStart  >= previousEnd) {
			// Verify for each comment that there's only white spaces between end and start of {following comment|node}
			Comment comment = this.comments[idx];
			int commentStart = comment.getStartPosition();
			int end = commentStart+comment.getLength()-1;
			int commentLine = scanner.getLineNumber(commentStart);
			if (end <= previousEnd || (commentLine == previousEndLine && commentLine != nodeStartLine)) {
				// stop search on condition 1) and 2)
				break;
			} else if ((end+1) < previousStart) { // may be equals => then no scan is necessary
				scanner.resetTo(end+1, previousStart);
				try {
					int token = scanner.getNextToken();
					if (token != TerminalTokens.TokenNameWHITESPACE || scanner.currentPosition != previousStart) {
						// stop search on condition 3)
						// if first comment fails, then there's no extended position in fact
						if (idx == endIdx) {
							return nodeStart;
						}
						break;
					}
				} catch (InvalidInputException e) {
					// Should not happen, but return no extended position...
					return nodeStart;
				}
				// verify that there's no more than one line between node/comments
				char[] gap = scanner.getCurrentIdentifierSource();
				int nbrLine = 0;
				int pos = -1;
				while ((pos=CharOperation.indexOf('\n', gap,pos+1)) >= 0) {
					nbrLine++;
				}
				if (nbrLine > 1) {
					// stop search on condition 4)
					break;
				}
			}
			// Store previous infos
			previousStart = commentStart;
			startIdx = idx--;
		}
		if (startIdx != -1) {
			// Verify that there's no token on the same line before first leading comment
			int commentStart = this.comments[startIdx].getStartPosition();
			if (previousEnd < commentStart && previousEndLine != nodeStartLine) {
				int lastTokenEnd = previousEnd;
				scanner.resetTo(previousEnd, commentStart);
				try {
					while (scanner.currentPosition != commentStart) {
						if (scanner.getNextToken() != TerminalTokens.TokenNameWHITESPACE) {
							lastTokenEnd =  scanner.getCurrentTokenEndPosition();
						}
					}
				} catch (InvalidInputException e) {
					// do nothing
				}
				int lastTokenLine = scanner.getLineNumber(lastTokenEnd);
				int length = this.comments.length;
				while (startIdx<length && lastTokenLine == scanner.getLineNumber(this.comments[startIdx].getStartPosition()) && nodeStartLine != lastTokenLine) {
					startIdx++;
				}
			}
			// Store leading comments indexes
			if (startIdx <= endIdx) {
				this.leadingComments.put(node, new int[] { startIdx, endIdx });
				extended = this.comments[endIdx].getStartPosition();
				this.commentIndex = endIdx;
			}
		}
		return extended;
	}

	/**
	 * Search and store node trailing comments. Comments are searched in position range
	 * from node end position to specified next start. If one or several comment are found,
	 * returns last comment end position, otherwise returns node end position.
	 * 
	 * First look after first comment after node end position using global comment index
	 * to reduce range of search. Obviously returns if no comment is found after the node...
	 * @see #doExtraRangesForChildren(ASTNode, Scanner)
	 *
	 * When first comment was found after node, goes down in comment list until one of
	 * following condition becomes true:
	 * 	1) comment start is after next start
	 * 	2) there's other than white characters between current node and comment
	 * 	3) there's more than 1 line between current node and comment
	 * 
	 * If at least one potential comment has been found, then all of them has to be separated
	 * from following node. So, remove all comments which do not verify this assumption.
	 * Note that this verification is not applicable on last node.
	 * 
	 * If finally there is a subset of comments, then store start and end indexes 
	 * in trailing comments table.
	 */
	int storeTrailingComments(ASTNode node, int nextStart, Scanner scanner, boolean lastChild) {
		// Init extended position
		int nodeEnd = node.getStartPosition()+node.getLength()-1;
		int extended = nodeEnd;
		
		// Get line number
		int nodeEndLine = scanner.getLineNumber(nodeEnd);
		
		// Find comments range index
		int idx = getCommentIndex(this.commentIndex, nodeEnd, 1);
		if (idx == -1) {
			return nodeEnd;
		}

		// Look after potential comments
		int startIdx = idx;
		int endIdx = -1;
		int length = this.comments.length;
		int commentStart = extended+1;
		int previousEnd = nodeEnd+1;
		int sameLineIdx = -1;
		while (idx<length && commentStart < nextStart) {
			// get comment and leave if next starting position has been reached
			Comment comment = this.comments[idx];
			commentStart = comment.getStartPosition();
			// verify that there's nothing else than white spaces between node/comments
			if (commentStart >= nextStart) {
				// stop search on condition 1)
				break;
			} else if (previousEnd < commentStart) {
				scanner.resetTo(previousEnd, commentStart);
				try {
					int token = scanner.getNextToken();
					if (token != TerminalTokens.TokenNameWHITESPACE || scanner.currentPosition != commentStart) {
						// stop search on condition 2)
						// if first index fails, then there's no extended position in fact...
						if (idx == startIdx) {
							return nodeEnd;
						}
						// otherwise we get the last index of trailing comment => break
						break;
					}
				} catch (InvalidInputException e) {
					// Should not happen, but return no extended position...
					return nodeEnd;
				}
				// verify that there's no more than one line between node/comments
				char[] gap = scanner.getCurrentIdentifierSource();
				int nbrLine = 0;
				int pos = -1;
				while ((pos=CharOperation.indexOf('\n', gap,pos+1)) >= 0) {
					nbrLine++;
				}
				if (nbrLine > 1) {
					// stop search on condition 3)
					break;
				}
			}
			// Store index if we're on the same line than node end
			int commentLine = scanner.getLineNumber(commentStart);
			if (commentLine == nodeEndLine) {
				sameLineIdx = idx;
			}
			// Store previous infos
			previousEnd = commentStart+comment.getLength();
			endIdx = idx++;
		}
		if (endIdx != -1) {
			// Verify that following node start is separated
			if (!lastChild) {
				int nextLine = scanner.getLineNumber(nextStart);
				int previousLine = scanner.getLineNumber(previousEnd);
				if((nextLine - previousLine) <= 1) {
					if (sameLineIdx == -1) return nodeEnd;
					endIdx = sameLineIdx;
				}
			}
			// Store trailing comments indexes
			this.trailingComments.put(node, new int[] { startIdx, endIdx });
			extended = this.comments[endIdx].getStartPosition()+this.comments[endIdx].getLength()-1;
			this.commentIndex = endIdx;
		}
		return extended;
	}
	
	/**
	 * Returns a list of the direct chidrens of a node. The siblings are ordered by start offset.
	 * @param node
	 * @return
	 */    
	private List getChildren(ASTNode node) {
		ChildrenCollector visitor= new ChildrenCollector();
		node.accept(visitor);
		return visitor.result;		
	}

	private class ChildrenCollector extends DefaultASTVisitor {
		public List result;

		public ChildrenCollector() {
			super();
			this.result= null;
		}
		protected boolean visitNode(ASTNode node) {
			if (this.result == null) { // first visitNode: on the node's parent: do nothing, return true
				this.result= new ArrayList();
				return true;
			}
			this.result.add(node);
			return false;
		}
	}
}
