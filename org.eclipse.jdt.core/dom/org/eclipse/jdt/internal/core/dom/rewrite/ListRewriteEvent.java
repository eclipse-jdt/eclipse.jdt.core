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

import org.eclipse.jdt.core.dom.ASTNode;

/**
 *
 */
public class ListRewriteEvent extends RewriteEvent {
	
	public final static int NEW= 1;
	public final static int OLD= 2;
	public final static int BOTH= NEW | OLD;
	
	/** original list of 'ASTNode' */
	private List fOriginalNodes;

	/** list of type 'RewriteEvent' */
	private List fListEntries;
	
	/**
	 * Creates a ListRewriteEvent from the original ASTNodes. The resulting event
	 * represents the unmodified list.
	 * @param originalNodes The original nodes (type ASTNode) 
	 */
	public ListRewriteEvent(List originalNodes) {
		fOriginalNodes= new ArrayList(originalNodes);
	}

	/**
	 * Creates a ListRewriteEvent from existing rewrite events.
	 * @param children The rewrite events for this list.
	 */
	public ListRewriteEvent(RewriteEvent[] children) {
		fListEntries= new ArrayList(children.length * 2);
		fOriginalNodes= new ArrayList(children.length * 2);
		for (int i= 0; i < children.length; i++) {
			RewriteEvent curr= children[i];
			fListEntries.add(curr);
			if (curr.getOriginalValue() != null) {
				fOriginalNodes.add(curr.getOriginalValue());
			}
		}
	}
	
	private List getEntries() {
		if (fListEntries == null) {
			// create if not yet existing
			int nNodes= fOriginalNodes.size();
			fListEntries= new ArrayList(nNodes * 2);
			for (int i= 0; i < nNodes; i++) {
				ASTNode node= (ASTNode) fOriginalNodes.get(i);
				// all nodes unchanged
				fListEntries.add(new NodeRewriteEvent(node, node));
			}
		}
		return fListEntries;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.ASTRewriteChange#getChangeKind()
	 */
	public int getChangeKind() {
		if (fListEntries != null) {
			for (int i= 0; i < fListEntries.size(); i++) {
				RewriteEvent curr= (RewriteEvent) fListEntries.get(i);
				if (curr.getChangeKind() != UNCHANGED) {
					return CHILDREN_CHANGED;
				}
			}
		}
		return UNCHANGED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.ASTRewriteChange#isListChange()
	 */
	public boolean isListRewrite() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.RewriteEvent#getChildren()
	 */
	public RewriteEvent[] getChildren() {
		List listEntries= getEntries();
		return (RewriteEvent[]) listEntries.toArray(new RewriteEvent[listEntries.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.RewriteEvent#getOriginalNode()
	 */
	public Object getOriginalValue() {
		return fOriginalNodes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.dom.RewriteEvent#getNewValue()
	 */
	public Object getNewValue() {
		List listEntries= getEntries();
		ArrayList res= new ArrayList(listEntries.size());
		for (int i= 0; i < listEntries.size(); i++) {
			RewriteEvent curr= (RewriteEvent) listEntries.get(i);
			Object newVal= curr.getNewValue();
			if (newVal != null) {
				res.add(newVal);
			}
		}
		return res;
	}	
	
	// API to modify the list nodes
	
	public RewriteEvent removeEntry(ASTNode originalEntry) {
		return replaceEntry(originalEntry, null);
	}
	
	public RewriteEvent replaceEntry(ASTNode originalEntry, ASTNode newEntry) {
		if (originalEntry == null) {
			throw new IllegalArgumentException();
		}
		
		List listEntries= getEntries();
		int nEntries= listEntries.size();
		for (int i= 0; i < nEntries; i++) {
			NodeRewriteEvent curr= (NodeRewriteEvent) listEntries.get(i);
			if (curr.getOriginalValue() == originalEntry) {
				curr.setNewValue(newEntry);
				return curr;
			}
		}
		return null;
	}
	
	public void revertChange(NodeRewriteEvent event) {
		Object originalValue = event.getOriginalValue();
		if(originalValue == null) {
			List listEntries= getEntries();
			listEntries.remove(event);
		} else {
			event.setNewValue(originalValue);
		}
	}
	
	public int getIndex(ASTNode node, int kind) {
		List listEntries= getEntries();
		for (int i= listEntries.size() - 1; i >= 0; i--) {
			RewriteEvent curr= (RewriteEvent) listEntries.get(i);
			if (((kind & OLD) != 0) && (curr.getOriginalValue() == node)) {
				return i;
			}
			if (((kind & NEW) != 0) && (curr.getNewValue() == node)) {
				return i;
			}
		}
		return -1;
	}
		
	public RewriteEvent insert(ASTNode insertedNode, int insertIndex) {
		NodeRewriteEvent change= new NodeRewriteEvent(null, insertedNode);
		if (insertIndex != -1) {
			getEntries().add(insertIndex, change);
		} else {
			getEntries().add(change);
		}
		return change;
	}
	
	public void setNewValue(ASTNode newValue, int insertIndex) {
		NodeRewriteEvent curr= (NodeRewriteEvent) getEntries().get(insertIndex);
		curr.setNewValue(newValue);
	}
	
	public int getChangeKind(int index) {
		return ((NodeRewriteEvent) getEntries().get(index)).getChangeKind();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf= new StringBuffer();
		buf.append(" [list change\n\t"); //$NON-NLS-1$
		
		RewriteEvent[] events= getChildren();
		for (int i= 0; i < events.length; i++) {
			if (i != 0) {
				buf.append("\n\t"); //$NON-NLS-1$
			}
			buf.append(events[i]);
		}
		buf.append("\n]"); //$NON-NLS-1$
		return buf.toString();
	}


	
}
