package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

import java.util.Vector;

/**
 * @see IJavaElementDelta
 */
public class JavaElementDelta implements IJavaElementDelta {
	/**
	 * The element that this delta describes the change to.
	 * @see #getElement()
	 */
	protected IJavaElement fChangedElement;
	/**
	 * @see #getKind()
	 */
	private int fKind;
	/**
	 * @see #getFlags()
	 */
	private int fChangeFlags = 0;
	/**
	 * @see #getAffectedChildren()
	 */
	protected IJavaElementDelta[] fAffectedChildren = fgEmptyDelta;

	/**
	 * Collection of resource deltas that correspond to non java resources deltas.
	 */
	protected IResourceDelta[] resourceDeltas = null;

	/**
	 * Counter of resource deltas
	 */
	protected int resourceDeltasCounter;
	/**
	 * @see #getMovedFromHandle()
	 */
	protected IJavaElement fMovedFromHandle = null;
	/**
	 * @see #getMovedToHandle()
	 */
	protected IJavaElement fMovedToHandle = null;
	/**
	 * Empty array of IJavaElementDelta
	 */
	protected static  IJavaElementDelta[] fgEmptyDelta= new IJavaElementDelta[] {};
/**
 * Creates the root delta. To create the nested delta
 * hierarchies use the following convenience methods. The root
 * delta can be created at any level (i.e. project, package root,
 * package fragment...).
 * <ul>
 * <li><code>added(IJavaElement)</code>
 * <li><code>changed(IJavaElement)</code>
 * <li><code>moved(IJavaElement, IJavaElement)</code>
 * <li><code>removed(IJavaElement)</code>
 * <li><code>renamed(IJavaElement, IJavaElement)</code>
 * </ul>
 */
public JavaElementDelta(IJavaElement element) {
	super();
	fChangedElement = element;
	fKind = CHANGED;
	fChangeFlags = F_CHILDREN;
}
/**
 * Adds the child delta to the collection of affected children.  If the
 * child is already in the collection, walk down the hierarchy.
 */
protected void addAffectedChild(JavaElementDelta child) {
	if (fAffectedChildren.length == 0) {
		fAffectedChildren = new IJavaElementDelta[] {child};
		return;
	}
	IJavaElementDelta sameChild = null;
	if (fAffectedChildren != null) {
		for (int i = 0; i < fAffectedChildren.length; i++) {
			if (this.equalsAndSameParent(fAffectedChildren[i].getElement(), child.getElement())) { // handle case of two jars that can be equals but not in the same project
				sameChild = fAffectedChildren[i];
				break;
			}
		}
	}
	if (sameChild == null) { //new affected child

		fAffectedChildren= growAndAddToArray(fAffectedChildren, child);
	} else {
		IJavaElementDelta[] children = child.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			JavaElementDelta childsChild = (JavaElementDelta) children[i];
			((JavaElementDelta) sameChild).addAffectedChild(childsChild);
		}
	}
}
/**
 * Creates the nested deltas resulting from an add operation.
 * Convenience method for creating add deltas.
 * The constructor should be used to create the root delta 
 * and then an add operation should call this method.
 */
public void added(IJavaElement element) {
	JavaElementDelta addedDelta = new JavaElementDelta(element);
	addedDelta.fKind = ADDED;
	addedDelta.fChangeFlags= 0;
	insertDeltaTree(element, addedDelta);
}
/**
 * Adds the child delta to the collection of affected children.  If the
 * child is already in the collection, walk down the hierarchy.
 */
protected void addResourceDelta(IResourceDelta child) {
	if (resourceDeltas == null) {
		resourceDeltas = new IResourceDelta[5];
		resourceDeltas[resourceDeltasCounter++] = child;
		return;
	}
	if (resourceDeltas.length == resourceDeltasCounter) {
		// need a resize
		System.arraycopy(resourceDeltas, 0, (resourceDeltas = new IResourceDelta[resourceDeltasCounter * 2]), 0, resourceDeltasCounter);
	}
	resourceDeltas[resourceDeltasCounter++] = child;
}
/**
 * Creates the nested deltas resulting from a change operation.
 * Convenience method for creating change deltas.
 * The constructor should be used to create the root delta 
 * and then a change operation should call this method.
 */
public void changed(IJavaElement element, int changeFlag) {
	JavaElementDelta changedDelta = new JavaElementDelta(element);
	changedDelta.fKind = CHANGED;
	changedDelta.fChangeFlags = changeFlag;
	insertDeltaTree(element, changedDelta);
}
/**
 * Creates the nested deltas for a closed element.
 */
public void closed(IJavaElement element) {
	JavaElementDelta delta = new JavaElementDelta(element);
	delta.fKind = CHANGED;
	delta.fChangeFlags= F_CLOSED;
	insertDeltaTree(element, delta);
}
/**
 * Creates the nested delta deltas based on the affected element
 * its delta, and the root of this delta tree. Returns the root
 * of the created delta tree.
 */
protected JavaElementDelta createDeltaTree(IJavaElement element, JavaElementDelta delta) {
	JavaElementDelta childDelta = delta;
	Vector ancestors= getAncestors(element);
	if (ancestors == null) {
		if (this.equalsAndSameParent(delta.getElement(), getElement())) { // handle case of two jars that can be equals but not in the same project
			// the element being changed is the root element
			fKind= delta.fKind;
			fChangeFlags= delta.fChangeFlags;
			fMovedToHandle= delta.fMovedToHandle;
			fMovedFromHandle= delta.fMovedFromHandle;
		} else {
			// the given delta is not the root or a child - illegal
			Assert.isTrue(false);
		}
	} else {
		for (int i = 0, size = ancestors.size(); i < size; i++) {
			IJavaElement ancestor = (IJavaElement) ancestors.elementAt(i);
			JavaElementDelta ancestorDelta = new JavaElementDelta(ancestor);
			ancestorDelta.addAffectedChild(childDelta);
			ancestorDelta.fKind = CHANGED;
			ancestorDelta.fChangeFlags = F_CHILDREN;
			childDelta = ancestorDelta;
		}
	}
	return childDelta;
}
/**
 * Returns whether the two java elements are equals and have the same parent.
 */
protected boolean equalsAndSameParent(IJavaElement e1, IJavaElement e2) {
	IJavaElement parent1;
	return e1.equals(e2) && ((parent1 = e1.getParent()) != null) && parent1.equals(e2.getParent());
}
/**
 * Returns the <code>JavaElementDelta</code> for the given element
 * in the delta tree, or null, if no delta for the given element is found.
 */
protected JavaElementDelta find(IJavaElement e) {
	if (this.equalsAndSameParent(fChangedElement, e)) { // handle case of two jars that can be equals but not in the same project
		return this;
	} else {
		for (int i = 0; i < fAffectedChildren.length; i++) {
			JavaElementDelta delta = ((JavaElementDelta)fAffectedChildren[i]).find(e);
			if (delta != null) {
				return delta;
			}
		}
	}
	return null;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getAddedChildren() {
	return getChildrenOfType(ADDED);
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getAffectedChildren() {
	return fAffectedChildren;
}
/**
 * Returns a collection of all the parents of this element up to (but
 * not including) the root of this tree in bottom-up order. If the given
 * element is not a descendant of the root of this tree, <code>null</code>
 * is returned.
 */
private Vector getAncestors(IJavaElement element) {
	IJavaElement parent = element.getParent();
	if (parent == null) {
		return null;
	}
	Vector parents = new Vector();
	while (!parent.equals(fChangedElement)) {
		parents.addElement(parent);
		parent = parent.getParent();
		if (parent == null) {
			return null;
		}
	}
	parents.trimToSize();
	return parents;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getChangedChildren() {
	return getChildrenOfType(CHANGED);
}
/**
 * @see IJavaElementDelta
 */
protected IJavaElementDelta[] getChildrenOfType(int type) {
	int length = fAffectedChildren.length;
	if (length == 0) {
		return new IJavaElementDelta[] {};
	}
	Vector children= new Vector(length);
	for (int i = 0; i < length; i++) {
		if (fAffectedChildren[i].getKind() == type) {
			children.addElement(fAffectedChildren[i]);
		}
	}

	IJavaElementDelta[] childrenOfType = new IJavaElementDelta[children.size()];
	children.copyInto(childrenOfType);
	
	return childrenOfType;
}
/**
 * Returns the delta for a given element.  Only looks below this
 * delta.
 */
protected JavaElementDelta getDeltaFor(IJavaElement element) {
	if (this.equalsAndSameParent(getElement(), element)) // handle case of two jars that can be equals but not in the same project
		return this;
	if (fAffectedChildren.length == 0)
		return null;
	int childrenCount = fAffectedChildren.length;
	for (int i = 0; i < childrenCount; i++) {
		JavaElementDelta delta = (JavaElementDelta)fAffectedChildren[i];
		if (this.equalsAndSameParent(delta.getElement(), element)) { // handle case of two jars that can be equals but not in the same project
			return delta;
		} else {
			delta = ((JavaElementDelta)delta).getDeltaFor(element);
			if (delta != null)
				return delta;
		}
	}
	return null;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElement getElement() {
	return fChangedElement;
}
/**
 * @see IJavaElementDelta
 */
public int getFlags() {
	return fChangeFlags;
}
/**
 * @see IJavaElementDelta
 */
public int getKind() {
	return fKind;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElement getMovedFromElement() {
	return fMovedFromHandle;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElement getMovedToElement() {
	return fMovedToHandle;
}
/**
 * @see IJavaElementDelta
 */
public IJavaElementDelta[] getRemovedChildren() {
	return getChildrenOfType(REMOVED);
}
/**
 * Return the collection of resource deltas. Return null if none.
 */
public IResourceDelta[] getResourceDeltas() {
	if (resourceDeltas == null) return null;
	if (resourceDeltas.length != resourceDeltasCounter) {
		System.arraycopy(resourceDeltas, 0, resourceDeltas = new IResourceDelta[resourceDeltasCounter], 0, resourceDeltasCounter);
	}
	return resourceDeltas;
}
/**
 * Adds the new element to a new array that contains all of the elements of the old array.
 * Returns the new array.
 */
protected IJavaElementDelta[] growAndAddToArray(IJavaElementDelta[] array, IJavaElementDelta addition) {
	IJavaElementDelta[] old = array;
	array = new IJavaElementDelta[old.length + 1];
	System.arraycopy(old, 0, array, 0, old.length);
	array[old.length] = addition;
	return array;
}
/**
 * Creates the delta tree for the given element and delta, and then
 * inserts the tree as an affected child of this node.
 */
protected void insertDeltaTree(IJavaElement element, JavaElementDelta delta) {
	JavaElementDelta childDelta= createDeltaTree(element, delta);
	if (!this.equalsAndSameParent(element, getElement())) { // handle case of two jars that can be equals but not in the same project
		addAffectedChild(childDelta);
	}
}
/**
 * Creates the nested deltas resulting from an move operation.
 * Convenience method for creating the "move from" delta.
 * The constructor should be used to create the root delta 
 * and then the move operation should call this method.
 */
public void movedFrom(IJavaElement movedFromElement, IJavaElement movedToElement) {
	JavaElementDelta removedDelta = new JavaElementDelta(movedFromElement);
	removedDelta.fKind = REMOVED;
	removedDelta.fChangeFlags = F_MOVED_TO;
	removedDelta.fMovedToHandle = movedToElement;
	insertDeltaTree(movedFromElement, removedDelta);
}
/**
 * Creates the nested deltas resulting from an move operation.
 * Convenience method for creating the "move to" delta.
 * The constructor should be used to create the root delta 
 * and then the move operation should call this method.
 */
public void movedTo(IJavaElement movedToElement, IJavaElement movedFromElement) {
	JavaElementDelta addedDelta = new JavaElementDelta(movedToElement);
	addedDelta.fKind = ADDED;
	addedDelta.fChangeFlags = F_MOVED_FROM;
	addedDelta.fMovedFromHandle = movedFromElement;
	insertDeltaTree(movedToElement, addedDelta);
}
/**
 * Creates the nested deltas for an opened element.
 */
public void opened(IJavaElement element) {
	JavaElementDelta delta = new JavaElementDelta(element);
	delta.fKind = CHANGED;
	delta.fChangeFlags= F_OPENED;
	insertDeltaTree(element, delta);
}
/**
 * Removes the child delta from the collection of affected children.
 */
protected void removeAffectedChild(JavaElementDelta child) {
	int index = -1;
	if (fAffectedChildren != null) {
		for (int i = 0; i < fAffectedChildren.length; i++) {
			if (this.equalsAndSameParent(fAffectedChildren[i].getElement(), child.getElement())) { // handle case of two jars that can be equals but not in the same project
				index = i;
				break;
			}
		}
	}
	if (index >= 0) {
		fAffectedChildren= removeAndShrinkArray(fAffectedChildren, index);
	}
}
/**
 * Removes the element from the array.
 * Returns the a new array which has shrunk.
 */
protected IJavaElementDelta[] removeAndShrinkArray(IJavaElementDelta[] old, int index) {
	IJavaElementDelta[] array = new IJavaElementDelta[old.length - 1];
	if (index > 0)
		System.arraycopy(old, 0, array, 0, index);
	int rest = old.length - index - 1;
	if (rest > 0)
		System.arraycopy(old, index + 1, array, index, rest);
	return array;
}
/**
 * Creates the nested deltas resulting from an delete operation.
 * Convenience method for creating removed deltas.
 * The constructor should be used to create the root delta 
 * and then the delete operation should call this method.
 */
public void removed(IJavaElement element) {
	JavaElementDelta removedDelta= new JavaElementDelta(element);
	insertDeltaTree(element, removedDelta);
	JavaElementDelta actualDelta = getDeltaFor(element);
	if (actualDelta != null) {
		actualDelta.fKind= REMOVED;
		actualDelta.fChangeFlags= 0;
		actualDelta.fAffectedChildren = fgEmptyDelta;
	}
}
/**
 * Sets the change flags of this delta.
 */
protected void setFlags(int flags) {
	fChangeFlags = flags;
}
/**
 * Creates the nested deltas resulting from a change operation.
 * Convenience method for creating change deltas.
 * The constructor should be used to create the root delta 
 * and then a change operation should call this method.
 */
public void sourceAttached(IJavaElement element) {
	JavaElementDelta attachedDelta = new JavaElementDelta(element);
	attachedDelta.fKind = CHANGED;
	attachedDelta.fChangeFlags = F_SOURCEATTACHED;
	insertDeltaTree(element, attachedDelta);
}
/**
 * Creates the nested deltas resulting from a change operation.
 * Convenience method for creating change deltas.
 * The constructor should be used to create the root delta 
 * and then a change operation should call this method.
 */
public void sourceDetached(IJavaElement element) {
	JavaElementDelta detachedDelta = new JavaElementDelta(element);
	detachedDelta.fKind = CHANGED;
	detachedDelta.fChangeFlags = F_SOURCEDETACHED;
	insertDeltaTree(element, detachedDelta);
}
/** 
 * Returns a string representation of this delta's
 * structure suitable for debug purposes.
 *
 * @see toString
 */
public String toDebugString(int depth) {
	StringBuffer buffer = new StringBuffer();
	for (int i= 0; i < depth; i++) {
		buffer.append('\t');
	}
	buffer.append(((JavaElement)getElement()).toDebugString());
	buffer.append("[");
	switch (getKind()) {
		case IJavaElementDelta.ADDED :
			buffer.append('+');
			break;
		case IJavaElementDelta.REMOVED :
			buffer.append('-');
			break;
		case IJavaElementDelta.CHANGED :
			buffer.append('*');
			break;
		default :
			buffer.append('?');
			break;
	}
	buffer.append("]: {");
	int changeFlags = getFlags();
	boolean prev = false;
	if ((changeFlags & IJavaElementDelta.F_CHILDREN) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("CHILDREN");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_CONTENT) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("CONTENT");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_MOVED_FROM) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("MOVED_FROM(" + ((JavaElement)getMovedFromElement()).toDebugString() + ")");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_MOVED_TO) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("MOVED_TO(" + ((JavaElement)getMovedToElement()).toDebugString() + ")");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("ADDED TO CLASSPATH");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("REMOVED FROM CLASSPATH");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_CLASSPATH_REORDER) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("REORDERED IN CLASSPATH");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_MODIFIERS) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("MODIFIERS CHANGED");
		prev = true;
	}
	if ((changeFlags & IJavaElementDelta.F_SUPER_TYPES) != 0) {
		if (prev)
			buffer.append(" | ");
		buffer.append("SUPER TYPES CHANGED");
		prev = true;
	}
	buffer.append("}");
	IJavaElementDelta[] children = getAffectedChildren();
	if (children != null) {
		for (int i = 0; i < children.length; ++i) {
			buffer.append("\n");
			buffer.append(((JavaElementDelta) children[i]).toDebugString(depth + 1));
		}
	}
	return buffer.toString();
}
/** 
 * Returns a string representation of this delta's
 * structure suitable for debug purposes.
 */
public String toString() {
	return toDebugString(0);
}
}
