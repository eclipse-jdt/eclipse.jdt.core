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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;


/**
 * Stores all rewrite events, descriptions of events and knows which nodes
 * are copy or move sources or tracked.
 */
public final class RewriteEventStore {
	

	public final class PropertyLocation {
		private ASTNode fParent;
		private StructuralPropertyDescriptor fProperty;
		
		public PropertyLocation(ASTNode parent, StructuralPropertyDescriptor property) {
			fParent= parent;
			fProperty= property;
		}

		public ASTNode getParent() {
			return fParent;
		}

		public StructuralPropertyDescriptor getProperty() {
			return fProperty;
		}
	}
	
	/**
	 * Interface that allows to override the way how children are accessed from
	 * a parent. Use this interface when the rewriter is set up on an already
	 * modified AST's (as it is the case in the old ASTRewrite infrastructure)
	 */
	public static interface INodePropertyMapper {
		/**
		 * Returns the node attribute for a given property name. 
		 * @param parent The parent node
		 * @param childProperty The child property to access 
		 * @return The child node at the given property location.
		 */
		Object getOriginalValue(ASTNode parent, StructuralPropertyDescriptor childProperty);
	}
	
	/*
	 * Store element to associate event and node position/
	 */
	private static class EventHolder {
		public ASTNode parent;
		public StructuralPropertyDescriptor childProperty;
		public RewriteEvent event;
		
		public EventHolder(ASTNode parent, StructuralPropertyDescriptor childProperty, RewriteEvent change) {
			this.parent= parent;
			this.childProperty= childProperty;
			this.event= change;
		}
		
		public String toString() {
			StringBuffer buf= new StringBuffer();
			buf.append(parent).append(" - "); //$NON-NLS-1$
			buf.append(childProperty.getId()).append(": "); //$NON-NLS-1$
			buf.append(event).append('\n');
			return buf.toString();
		}
	}
	
	public static class CopySourceInfo {
		public ASTNode parent;
		public StructuralPropertyDescriptor childProperty;
		public ASTNode node;
		public boolean isMove;

		public String toString() {
			StringBuffer buf= new StringBuffer();
			if (isMove) {
				buf.append("move source: "); //$NON-NLS-1$
			} else {
				buf.append("copy source: "); //$NON-NLS-1$
			}
			buf.append(node);
			return buf.toString();
		}
	}
	
	public static class CopyRangeSourceInfo {
		public ASTNode parent;
		public StructuralPropertyDescriptor childProperty;
		public ASTNode node;
		public boolean isMove;

		public String toString() {
			StringBuffer buf= new StringBuffer();
			if (isMove) {
				buf.append("move source: "); //$NON-NLS-1$
			} else {
				buf.append("copy source: "); //$NON-NLS-1$
			}
			buf.append(node);
			return buf.toString();
		}
		
		
	}
	
	
	public static class CopySourceInfoSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			CopySourceInfo e1= (CopySourceInfo) o1;
			CopySourceInfo e2= (CopySourceInfo) o2;
			if (e1.isMove) {
				return -1;
			} else if (e2.isMove) {
				return 1;
			}
			return 0;
		}
	
	}
	
	/**
	 * Iterates over all event parent nodes, tracked nodes and all copy/move sources 
	 */
	private class ParentIterator implements Iterator {
		
		private Iterator fEventIter;
		private Iterator fSourceNodeIter;
		private Iterator fTrackedNodeIter;
		
		public ParentIterator() {
			fEventIter= fEvents.iterator();
			if (fCopySources != null) {
				fSourceNodeIter= fCopySources.iterator();
			} else {
				fSourceNodeIter= Collections.EMPTY_LIST.iterator();
			}
			if (fTrackedNodes != null) {
				fTrackedNodeIter= fTrackedNodes.keySet().iterator();
			} else {
				fTrackedNodeIter= Collections.EMPTY_LIST.iterator();
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return fEventIter.hasNext() || fSourceNodeIter.hasNext() || fTrackedNodeIter.hasNext();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (fEventIter.hasNext()) {
				return ((EventHolder) fEventIter.next()).parent;
			}
			if (fSourceNodeIter.hasNext()) {
				return ((CopySourceInfo) fSourceNodeIter.next()).parent;
			}
			return fTrackedNodeIter.next();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public final static int NEW= 1;
	public final static int ORIGINAL= 2;
	public final static int BOTH= NEW | ORIGINAL;
		
	
	/** all events */
	final List fEvents;
	
	/** cache for last accessed event */
	private EventHolder fLastEvent;
	
	/** Maps events to group descriptions */
	private Map fEditGroups;
		
	/** Stores which nodes are source of a copy or move */
	List fCopySources;
	
	/** Stores which nodes are tracked and the corresponding edit group*/
	Map fTrackedNodes;
	
	/** Stores which inserted nodes bound to the previous node. If not, a node is
	 * always bound to the next node */
	private Set fInsertBoundToPrevious;
	
	/** optional mapper to allow fix already modified AST trees */
	private INodePropertyMapper fNodePropertyMapper;
		
	public RewriteEventStore() {
		fEvents= new ArrayList();
		fLastEvent= null;
		
		fEditGroups= null; // lazy initialization
		
		fTrackedNodes= null;
		fInsertBoundToPrevious= null;
		
		fNodePropertyMapper= null;
		fCopySources= null;
	}
	
	/**
	 * Override the default way how to access children from a parent node.
	 * @param nodePropertyMapper The new <code>INodePropertyMapper</code> or
	 * <code>null</code>. to use the default.
	 */
	public void setNodePropertyMapper(INodePropertyMapper nodePropertyMapper) {
		fNodePropertyMapper= nodePropertyMapper;
	}
	
	public void clear() {
		fEvents.clear();
		fLastEvent= null;
		fTrackedNodes= null;
		
		fEditGroups= null; // lazy initialization
		fInsertBoundToPrevious= null;
		fCopySources= null;
	}
	
	public void addEvent(ASTNode parent, StructuralPropertyDescriptor childProperty, RewriteEvent event) {
		validateHasChildProperty(parent, childProperty);
		
		if (event.isListRewrite()) {
			validateIsListProperty(childProperty);
		}
		
		EventHolder holder= new EventHolder(parent, childProperty, event);
		
		// check if already in list
		for (int i= 0; i < fEvents.size(); i++) {
			EventHolder curr= (EventHolder) fEvents.get(i);
			if (curr.parent == parent && curr.childProperty == childProperty) {
				fEvents.set(i, holder);
				fLastEvent= null;
				return;
			}
		}
		fEvents.add(holder);
	}
	
	public RewriteEvent getEvent(ASTNode parent, StructuralPropertyDescriptor property) {
		validateHasChildProperty(parent, property);
		
		if (fLastEvent != null && fLastEvent.parent == parent && fLastEvent.childProperty == property) {
			return fLastEvent.event;
		}
		
		for (int i= 0; i < fEvents.size(); i++) {
			EventHolder holder= (EventHolder) fEvents.get(i);
			if (holder.parent == parent && holder.childProperty == property) {
				fLastEvent= holder;
				return holder.event;
			}
		}
		return null;
	}
	
	public NodeRewriteEvent getNodeEvent(ASTNode parent, StructuralPropertyDescriptor childProperty, boolean forceCreation) {
		validateIsNodeProperty(childProperty);
		NodeRewriteEvent event= (NodeRewriteEvent) getEvent(parent, childProperty);
		if (event == null && forceCreation) {
			Object originalValue= accessOriginalValue(parent, childProperty);
			event= new NodeRewriteEvent(originalValue, originalValue);
			addEvent(parent, childProperty, event);
		}
		return event;		
	}
	
	public ListRewriteEvent getListEvent(ASTNode parent, StructuralPropertyDescriptor childProperty, boolean forceCreation) {
		validateIsListProperty(childProperty);
		ListRewriteEvent event= (ListRewriteEvent) getEvent(parent, childProperty);
		if (event == null && forceCreation) {
			List originalValue= (List) accessOriginalValue(parent, childProperty);
			event= new ListRewriteEvent(originalValue);
			addEvent(parent, childProperty, event);
		}
		return event;
	}
	
	public Iterator getChangeRootIterator() {
		return new ParentIterator();
	}
	
	
	public boolean hasChangedProperties(ASTNode parent) {
		for (int i= 0; i < fEvents.size(); i++) {
			EventHolder holder= (EventHolder) fEvents.get(i);
			if (holder.parent == parent) {
				if (holder.event.getChangeKind() != RewriteEvent.UNCHANGED) {
					return true;
				}
			}
		}
		return false;
	}
	
	public PropertyLocation getPropertyLocation(Object value, int kind) {
		for (int i= 0; i < fEvents.size(); i++) {
			EventHolder holder= (EventHolder) fEvents.get(i);
			RewriteEvent event= holder.event;
			if (isNodeInEvent(event, value, kind)) {
				return new PropertyLocation(holder.parent, holder.childProperty);
			}
			if (event.isListRewrite()) {
				RewriteEvent[] children= event.getChildren();
				for (int k= 0; k < children.length; k++) {
					if (isNodeInEvent(children[k], value, kind)) {
						return new PropertyLocation(holder.parent, holder.childProperty);
					}
				}
			}
		}
		if (value instanceof ASTNode) {
			ASTNode node= (ASTNode) value;
			return new PropertyLocation(node.getParent(), node.getLocationInParent()); 
		}
		return null;
	}
	
	
	/**
	 * Kind is either ORIGINAL, NEW, or BOTH
	 * @param value
	 * @param kind
	 * @return
	 */
	public RewriteEvent findEvent(Object value, int kind) {
		for (int i= 0; i < fEvents.size(); i++) {
			RewriteEvent event= ((EventHolder) fEvents.get(i)).event;
			if (isNodeInEvent(event, value, kind)) {
				return event;
			}
			if (event.isListRewrite()) {
				RewriteEvent[] children= event.getChildren();
				for (int k= 0; k < children.length; k++) {
					if (isNodeInEvent(children[k], value, kind)) {
						return children[k];
					}
				}
			}
		}
		return null;
	}
	
	private boolean isNodeInEvent(RewriteEvent event, Object value, int kind) {
		if (((kind & NEW) != 0) && event.getNewValue() == value) {
			return true;
		}
		if (((kind & ORIGINAL) != 0) && event.getOriginalValue() == value) {
			return true;
		}
		return false;
	}
	
	
	public RewriteEvent findEventByOriginal(Object original) {
		return findEvent(original, ORIGINAL);
	}
	
	public RewriteEvent findEventByNew(Object original) {
		return findEvent(original, NEW);
	}
	
	
	public Object getOriginalValue(ASTNode parent, StructuralPropertyDescriptor property) {
		RewriteEvent event= getEvent(parent, property);
		if (event != null) {
			return event.getOriginalValue();
		}
		return accessOriginalValue(parent, property);
	}
	
	public Object getNewValue(ASTNode parent, StructuralPropertyDescriptor property) {
		RewriteEvent event= getEvent(parent, property);
		if (event != null) {
			return event.getNewValue();
		}
		return accessOriginalValue(parent, property);
	}
	
	public int getChangeKind(ASTNode node) {
		RewriteEvent event= findEventByOriginal(node);
		if (event != null) {
			return event.getChangeKind();
		}
		return RewriteEvent.UNCHANGED;
	}
	
	/*
	 * Gets an original child from the AST. The behav
	 * Temporarily overridden to port. All rewriters should prevent AST modification without their control.
	 */
	private Object accessOriginalValue(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		if (fNodePropertyMapper != null) {
			return fNodePropertyMapper.getOriginalValue(parent, childProperty);
		}
		
		return parent.getStructuralProperty(childProperty);
	}	
	
	public TextEditGroup getEventEditGroup(RewriteEvent event) {
		if (fEditGroups == null) {
			return null;
		}
		return (TextEditGroup) fEditGroups.get(event);
	}
	
	public void setEventEditGroup(RewriteEvent event, TextEditGroup editGroup) {
		if (editGroup != null) {
			if (fEditGroups == null) {
				fEditGroups= new IdentityHashMap(5);
			}	
			fEditGroups.put(event, editGroup);
		}
	}
	
	
	public final TextEditGroup getTrackedNodeData(ASTNode node) {
		if (fTrackedNodes != null) {
			return (TextEditGroup) fTrackedNodes.get(node);
		}
		return null;	
	}
	
	public void setTrackedNodeData(ASTNode node, TextEditGroup editGroup) {
		if (fTrackedNodes == null) {
			fTrackedNodes= new IdentityHashMap();
		}
		fTrackedNodes.put(node, editGroup);
	}
	
	/**
	 * Marks a node as tracked. The edits added to the group editGroup can be used to get the
	 * position of the node after the rewrite operation.
	 * @param node The node to track
	 * @param editGroup Collects the range markers describing the node position.
	 */
	public final void markAsTracked(ASTNode node, TextEditGroup editGroup) {
		if (getTrackedNodeData(node) != null) {
			throw new IllegalArgumentException("Node is already marked as tracked"); //$NON-NLS-1$
		}
		setTrackedNodeData(node, editGroup);
	}	
	
	public final CopySourceInfo markAsCopySource(ASTNode parent, StructuralPropertyDescriptor property, ASTNode node, boolean isMove) {
		
		CopySourceInfo copySource= new CopySourceInfo();
		copySource.parent= parent;
		copySource.childProperty= property;
		copySource.node= node;
		copySource.isMove= isMove;
		assertNoNesting(copySource);
		
		if (fCopySources == null) {
			fCopySources= new ArrayList();
		}
		fCopySources.add(copySource);
		return copySource;
	}
	
	
	public CopySourceInfo[] getCopySources(ASTNode node) {
		if (fCopySources == null) {
			return null;
		}
		ArrayList res= new ArrayList(3);
		for (int i= 0; i < fCopySources.size(); i++) {
			CopySourceInfo curr= (CopySourceInfo) fCopySources.get(i);
			if (curr.node == node) {
				res.add(curr);
			}
		}
		if (res.isEmpty()) {
			return null;
		}
		CopySourceInfo[] arr= (CopySourceInfo[]) res.toArray(new CopySourceInfo[res.size()]);
		if (arr.length > 1) {
			Arrays.sort(arr, new CopySourceInfoSorter());
		}
		return arr;
	}
	
	private void assertNoNesting(CopySourceInfo copySource) {
	    // ignore
	}
	
	/**
	 * Make sure all moved nodes are marked as removed or replaced.
	 */
	public void markMovedNodesRemoved() {
		if (fCopySources == null) {
			return;
		}
		for (int i= 0; i < fCopySources.size(); i++) {
			CopySourceInfo curr= (CopySourceInfo) fCopySources.get(i);
			if (curr.isMove) {
				doMarkMovedAsRemoved(curr);
			}
		}
		
	}
	
	private void doMarkMovedAsRemoved(CopySourceInfo curr) {
		if (curr.childProperty.isChildListProperty()) {
			ListRewriteEvent event= getListEvent(curr.parent, curr.childProperty, true);
			int index= event.getIndex(curr.node, ListRewriteEvent.OLD);
			if (index != -1 && event.getChangeKind(index) == RewriteEvent.UNCHANGED) {
				event.setNewValue(null, index);
			}
		} else {
			NodeRewriteEvent event= getNodeEvent(curr.parent, curr.childProperty, true);
			if (event.getChangeKind() == RewriteEvent.UNCHANGED) {
				event.setNewValue(null);
			}
		}
	}

	public boolean isInsertBoundToPrevious(ASTNode node) {	
		if (fInsertBoundToPrevious != null) {
			return fInsertBoundToPrevious.contains(node);
		}
		return false;
	}

	public void setInsertBoundToPrevious(ASTNode node) {
		if (fInsertBoundToPrevious == null) {
			fInsertBoundToPrevious= new HashSet();
		}
		fInsertBoundToPrevious.add(node);
	}
	
	private void validateIsListProperty(StructuralPropertyDescriptor property) {
		if (!property.isChildListProperty()) {
			String message= property.getId() + " is not a list property"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}
	}
	
	private void validateHasChildProperty(ASTNode parent, StructuralPropertyDescriptor property) {
		if (!parent.structuralPropertiesForType().contains(property)) {
			String message= Signature.getSimpleName(parent.getClass().getName()) + " has no property " + property.getId(); //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}
	}
	
	private void validateIsNodeProperty(StructuralPropertyDescriptor property) {
		if (property.isChildListProperty()) {
			String message= property.getId() + " is not a node property"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}
	}	
	
	public String toString() {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < fEvents.size(); i++) {
			buf.append(fEvents.get(i).toString()).append('\n');
		}
		return buf.toString();
	}
	
	public static boolean isNewNode(ASTNode node) {
		return node.getStartPosition() == -1; // should be changed with new API
	}




}
