/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
		private ASTNode parent;
		private StructuralPropertyDescriptor property;
		
		public PropertyLocation(ASTNode parent, StructuralPropertyDescriptor property) {
			this.parent= parent;
			this.property= property;
		}

		public ASTNode getParent() {
			return this.parent;
		}

		public StructuralPropertyDescriptor getProperty() {
			return this.property;
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
		public final ASTNode parent;
		public final StructuralPropertyDescriptor childProperty;
		public final RewriteEvent event;
		
		public EventHolder(ASTNode parent, StructuralPropertyDescriptor childProperty, RewriteEvent change) {
			this.parent= parent;
			this.childProperty= childProperty;
			this.event= change;
		}
		
		public String toString() {
			StringBuffer buf= new StringBuffer();
			buf.append(this.parent).append(" - "); //$NON-NLS-1$
			buf.append(this.childProperty.getId()).append(": "); //$NON-NLS-1$
			buf.append(this.event).append('\n');
			return buf.toString();
		}
	}
	
	public static class CopySourceInfo {
		public final ASTNode parent;
		public final StructuralPropertyDescriptor childProperty;
		private final ASTNode first;
		private final ASTNode last;
		public final boolean isMove;
		
		public CopySourceInfo(ASTNode parent, StructuralPropertyDescriptor childProperty, ASTNode first, ASTNode last, boolean isMove) {
			this.parent= parent;
			this.childProperty= childProperty;
			this.first= first;
			this.last= last;
			this.isMove= isMove;
		}
		
		public ASTNode getStartNode() {
			return this.first;
		}
		
		public ASTNode getEndNode() {
			return this.last;
		}

		public String toString() {
			StringBuffer buf= new StringBuffer();
			if (this.first != this.last) {
				buf.append("range ");  //$NON-NLS-1$
			}
			if (this.isMove) {
				buf.append("move source: "); //$NON-NLS-1$
			} else {
				buf.append("copy source: "); //$NON-NLS-1$
			}
			buf.append(this.first);
			if (this.first != this.last) {
				buf.append(" - "); //$NON-NLS-1$
				buf.append(this.last);
			}
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
			return e2.getEndNode().getStartPosition() - e1.getEndNode().getStartPosition();
		}
	
	}
	
	/**
	 * Iterates over all event parent nodes, tracked nodes and all copy/move sources 
	 */
	private class ParentIterator implements Iterator {
		
		private Iterator eventIter;
		private Iterator sourceNodeIter;
		private Iterator rangeNodeIter;
		private Iterator trackedNodeIter;
		
		public ParentIterator() {
			this.eventIter= RewriteEventStore.this.eventLookup.keySet().iterator();
			if (RewriteEventStore.this.nodeCopySources != null) {
				this.sourceNodeIter= RewriteEventStore.this.nodeCopySources.iterator();
			} else {
				this.sourceNodeIter= Collections.EMPTY_LIST.iterator();
			}
			if (RewriteEventStore.this.rangeCopySources != null) {
				this.rangeNodeIter= RewriteEventStore.this.rangeCopySources.iterator();
			} else {
				this.rangeNodeIter= Collections.EMPTY_LIST.iterator();
			}
			if (RewriteEventStore.this.trackedNodes != null) {
				this.trackedNodeIter= RewriteEventStore.this.trackedNodes.keySet().iterator();
			} else {
				this.trackedNodeIter= Collections.EMPTY_LIST.iterator();
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return this.eventIter.hasNext() || this.sourceNodeIter.hasNext() || this.rangeNodeIter.hasNext() || this.trackedNodeIter.hasNext();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (this.eventIter.hasNext()) {
				return this.eventIter.next();
			}
			if (this.sourceNodeIter.hasNext()) {
				return ((CopySourceInfo) this.sourceNodeIter.next()).getStartNode();
			}
			if (this.rangeNodeIter.hasNext()) {
				return ((CopySourceInfo) this.rangeNodeIter.next()).parent;
			}
			return this.trackedNodeIter.next();
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
		
		
	/** all events by parent*/
	final Map eventLookup;
	
	/** cache for last accessed event */
	private EventHolder lastEvent;
	
	/** Maps events to group descriptions */
	private Map editGroups;
		
	/** Stores which nodes are source of a copy or move (list of CopyRangeSourceInfo)*/
	List nodeCopySources;
	
	/** Stores which node ranges that are source of a copy or move (list of CopyRangeSourceInfo)*/
	List rangeCopySources;
	
	/** Stores which nodes are tracked and the corresponding edit group*/
	Map trackedNodes;
	
	/** Stores which inserted nodes bound to the previous node. If not, a node is
	 * always bound to the next node */
	private Set insertBoundToPrevious;
	
	/** optional mapper to allow fix already modified AST trees */
	private INodePropertyMapper nodePropertyMapper;
		
	public RewriteEventStore() {
		this.eventLookup= new HashMap();
		this.lastEvent= null;
		
		this.editGroups= null; // lazy initialization
		
		this.trackedNodes= null;
		this.insertBoundToPrevious= null;
		
		this.nodePropertyMapper= null;
		this.nodeCopySources= null;
	}
	
	/**
	 * Override the default way how to access children from a parent node.
	 * @param nodePropertyMapper The new <code>INodePropertyMapper</code> or
	 * <code>null</code>. to use the default.
	 */
	public void setNodePropertyMapper(INodePropertyMapper nodePropertyMapper) {
		this.nodePropertyMapper= nodePropertyMapper;
	}
	
	public void clear() {
		this.eventLookup.clear();
		this.lastEvent= null;
		this.trackedNodes= null;
		
		this.editGroups= null; // lazy initialization
		this.insertBoundToPrevious= null;
		this.nodeCopySources= null;
	}
	
	public void addEvent(ASTNode parent, StructuralPropertyDescriptor childProperty, RewriteEvent event) {
		validateHasChildProperty(parent, childProperty);
		
		if (event.isListRewrite()) {
			validateIsListProperty(childProperty);
		}
		
		EventHolder holder= new EventHolder(parent, childProperty, event);
		
		List entriesList = (List) this.eventLookup.get(parent);
		if (entriesList != null) {
			for (int i= 0; i < entriesList.size(); i++) {
				EventHolder curr= (EventHolder) entriesList.get(i);
				if (curr.childProperty == childProperty) {
					entriesList.set(i, holder);
					this.lastEvent= null;
					return;
				}
			}
		} else {
			entriesList= new ArrayList(3);
			this.eventLookup.put(parent, entriesList);
		}
		entriesList.add(holder);
	}
	
	public RewriteEvent getEvent(ASTNode parent, StructuralPropertyDescriptor property) {
		validateHasChildProperty(parent, property);
		
		if (this.lastEvent != null && this.lastEvent.parent == parent && this.lastEvent.childProperty == property) {
			return this.lastEvent.event;
		}
		
		List entriesList = (List) this.eventLookup.get(parent);
		if (entriesList != null) {
			for (int i= 0; i < entriesList.size(); i++) {
				EventHolder holder= (EventHolder) entriesList.get(i);
				if (holder.childProperty == property) {
					this.lastEvent= holder;
					return holder.event;
				}
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
		List entriesList = (List) this.eventLookup.get(parent);
		if (entriesList != null) {
			for (int i= 0; i < entriesList.size(); i++) {
				EventHolder holder= (EventHolder) entriesList.get(i);
				if (holder.event.getChangeKind() != RewriteEvent.UNCHANGED) {
					return true;
				}
			}
		}
		return false;
	}
	
	public PropertyLocation getPropertyLocation(Object value, int kind) {
		for (Iterator iter= this.eventLookup.values().iterator(); iter.hasNext();) {
			List events= (List) iter.next();
			for (int i= 0; i < events.size(); i++) {
				EventHolder holder= (EventHolder) events.get(i);
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
		}
		if (value instanceof ASTNode) {
			ASTNode node= (ASTNode) value;
			return new PropertyLocation(node.getParent(), node.getLocationInParent()); 
		}
		return null;
	}
	
	
	/**
	 * Kind is either ORIGINAL, NEW, or BOTH
	 * @param value Object
	 * @param kind int
	 * @return RewriteEvent
	 */
	public RewriteEvent findEvent(Object value, int kind) {
		for (Iterator iter= this.eventLookup.values().iterator(); iter.hasNext();) {
			List events= (List) iter.next();
			for (int i= 0; i < events.size(); i++) {
				RewriteEvent event= ((EventHolder) events.get(i)).event;
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
		RewriteEvent event= findEvent(node, ORIGINAL);
		if (event != null) {
			return event.getChangeKind();
		}
		return RewriteEvent.UNCHANGED;
	}
	
	/*
	 * Gets an original child from the AST.
	 * Temporarily overridden to port the old rewriter to the new infrastructure.
	 */
	private Object accessOriginalValue(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		if (this.nodePropertyMapper != null) {
			return this.nodePropertyMapper.getOriginalValue(parent, childProperty);
		}
		
		return parent.getStructuralProperty(childProperty);
	}	
	
	public TextEditGroup getEventEditGroup(RewriteEvent event) {
		if (this.editGroups == null) {
			return null;
		}
		return (TextEditGroup) this.editGroups.get(event);
	}
	
	public void setEventEditGroup(RewriteEvent event, TextEditGroup editGroup) {
		if (this.editGroups == null) {
			this.editGroups= new IdentityHashMap(5);
		}	
		this.editGroups.put(event, editGroup);
	}
	
	
	public final TextEditGroup getTrackedNodeData(ASTNode node) {
		if (this.trackedNodes != null) {
			return (TextEditGroup) this.trackedNodes.get(node);
		}
		return null;	
	}
	
	public void setTrackedNodeData(ASTNode node, TextEditGroup editGroup) {
		if (this.trackedNodes == null) {
			this.trackedNodes= new IdentityHashMap();
		}
		this.trackedNodes.put(node, editGroup);
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
		CopySourceInfo copySource= new CopySourceInfo(parent, property, node, node, isMove);
		
		if (this.nodeCopySources == null) {
			this.nodeCopySources= new ArrayList();
		}
		this.nodeCopySources.add(copySource);
		return copySource;
	}
	
	public final CopySourceInfo markAsRangeCopySource(ASTNode parent, StructuralPropertyDescriptor property, ASTNode first, ASTNode last, boolean isMove) {
		CopySourceInfo copySource= new CopySourceInfo(parent, property, first, last, isMove);
		assertNoOverlap(copySource);
		
		if (this.rangeCopySources == null) {
			this.rangeCopySources= new ArrayList();
		}
		this.rangeCopySources.add(copySource);
		return copySource;
	}
	
	
	public CopySourceInfo[] getNodeCopySources(ASTNode node) {
		if (this.nodeCopySources == null) {
			return null;
		}
		return internalGetCopySources(this.nodeCopySources, node);
	}
	
	public CopySourceInfo[] getRangeCopySources(ASTNode node) {
		if (this.rangeCopySources == null) {
			return null;
		}
		return internalGetCopySources(this.rangeCopySources, node);
	}
	
	public boolean hasRangeCopySources(ASTNode parent, StructuralPropertyDescriptor property) {
		if (this.rangeCopySources == null) {
			return false;
		}
		for (int i= 0; i < this.rangeCopySources.size(); i++) {
			CopySourceInfo curr= (CopySourceInfo) this.rangeCopySources.get(i);
			if (curr.parent == parent && curr.childProperty == property) {
				return true;
			}
		}
		return false;
	}
	
	public CopySourceInfo[] internalGetCopySources(List copySources, ASTNode node) {
		ArrayList res= new ArrayList(3);
		for (int i= 0; i < copySources.size(); i++) {
			CopySourceInfo curr= (CopySourceInfo) copySources.get(i);
			if (curr.getStartNode() == node) {
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
	
	
	private void assertNoOverlap(CopySourceInfo copySource) {
		// todo
	}
	
	/**
	 * Make sure all moved nodes are marked as removed or replaced.
	 */
	public void markMovedNodesRemoved() {
		if (this.nodeCopySources == null) {
			return;
		}
		for (int i= 0; i < this.nodeCopySources.size(); i++) {
			CopySourceInfo curr= (CopySourceInfo) this.nodeCopySources.get(i);
			if (curr.isMove) {
				doMarkMovedAsRemoved(curr);
			}
		}
		
	}
	
	private void doMarkMovedAsRemoved(CopySourceInfo curr) {
		if (curr.childProperty.isChildListProperty()) {
			ListRewriteEvent event= getListEvent(curr.parent, curr.childProperty, true);
			int index= event.getIndex(curr.getStartNode(), ListRewriteEvent.OLD);
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
		if (this.insertBoundToPrevious != null) {
			return this.insertBoundToPrevious.contains(node);
		}
		return false;
	}

	public void setInsertBoundToPrevious(ASTNode node) {
		if (this.insertBoundToPrevious == null) {
			this.insertBoundToPrevious= new HashSet();
		}
		this.insertBoundToPrevious.add(node);
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
		for (Iterator iter = this.eventLookup.values().iterator(); iter.hasNext();) {
			List events = (List) iter.next();
			for (int i= 0; i < events.size(); i++) {
				buf.append(events.get(i).toString()).append('\n');
			}
		}
		return buf.toString();
	}
	
	public static boolean isNewNode(ASTNode node) {
		return (node.getFlags() & ASTNode.ORIGINAL) == 0;
	}
}
