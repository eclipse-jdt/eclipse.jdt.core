/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract superclass of all Abstract Syntax Tree (AST) node types.
 * <p>
 * An AST node represents a Java source code construct, such
 * as a name, type, expression, statement, or declaration.
 * </p>
 * <p>
 * Each AST node belongs to a unique AST instance, called the owning AST.
 * The children of an AST node always have the same owner as their parent node.
 * If a node from one AST is to be added to a different AST, the subtree must
 * be cloned first to ensures that the added nodes have the correct owning AST.
 * </p>
 * <p>
 * When an AST node is part of an AST, it has a unique parent node.
 * Clients can navigate upwards, from child to parent, as well as downwards,
 * from parent to child. Newly created nodes are unparented. When an 
 * unparented node is set as a child of a node (using a 
 * <code>set<it>CHILD</it></code> method), its parent link is set automatically
 * and the parent link of the former child is set to <code>null</code>.
 * For nodes with properties that include a list of children (for example,
 * <code>Block</code> whose <code>statements</code> property is a list
 * of statements), adding or removing an element to/for the list property
 * automatically updates the parent links.
 * </p>
 * <p>
 * ASTs must not contain cycles. All operations that could create a cycle
 * detect this possibility and fail.
 * </p>
 * <p>
 * ASTs do not contain "holes" (missing subtrees). If a node is required to
 * have a certain property, a syntactically plausible initial value is
 * always supplied. 
 * </p>
 * <p>
 * The hierarchy of AST node types has some convenient groupings marked
 * by abstract superclasses:
 * <ul>
 * <li>expressions - <code>Expression</code></li>
 * <li>names - <code>Name</code> (a sub-kind of expression)</li>
 * <li>statements - <code>Statement</code></li>
 * <li>types - <code>Type</code></li>
 * <li>type body declarations - <code>BodyDeclaration</code></li>
 * </ul>
 * </p>
 * <p>
 * Abstract syntax trees may be hand constructed by clients, using the
 * <code>new<it>TYPE</it></code> factory methods (see <code>AST</code>) to
 * create new nodes, and the various <code>set<it>CHILD</it></code> methods
 * to connect them together.
 * </p>
 * <p>
 * The static method <code>AST.parseCompilationUnit</code> parses a string
 * containing a Java compilation unit and returns the abstract syntax tree
 * for it. The resulting nodes carry a source range relating the node back to
 * the original source characters. The source range covers the construct
 * as a whole.
 * </p>
 * <p>
 * Each AST node carries bit flags which may convey additional information about
 * the node. For instance, the parser uses a flag to indicate a syntax error.
 * Newly created nodes have no flags set.
 * </p>
 * <p>
 * Each AST node is capable of carraying an open-ended collection of
 * client-defined properties. Newly created nodes have none. 
 * <code>getProperty</code> and <code>setProperty</code> are used to access
 * these properties.
 * </p>
 * <p>
 * AST nodes are <b>not</b> thread-safe; this is true even for trees that
 * are read-only. If synchronization is required, consider using the common AST
 * object that owns the node; that is, use 
 * <code>synchronize (node.getAST()) {...}</code>.
 * </p>
 * <p>
 * ASTs also support the visitor pattern; see the class <code>ASTVisitor</code>
 * for details.
 * </p>
 * 
 * @see AST#parseCompilationUnit
 * @see ASTVisitor
 * @since 2.0
 */
public abstract class ASTNode {
	
	/**
	 * Owning AST.
	 */
	private final AST owner;
	
	/**
	 * Parent AST node, or <code>null</code> if this node is a root.
	 * Initially <code>null</code>.
	 */
	private ASTNode parent = null;
	
	/**
	 * An unmodifiable empty map (used to implement <code>properties()</code>).
	 * 
	 * @see #properties
	 */
	private static Map UNMODIFIABLE_EMPTY_MAP
		= Collections.unmodifiableMap(new HashMap(1));
	
	/**
	 * Primary field used in representing node properties efficiently.
	 * If <code>null</code>, this node has no properties.
	 * If a <code>String</code>, this is the name of this node's sole property,
	 * and <code>property2</code> contains its value.
	 * If a <code>HashMap</code>, this is the table of property name-value
	 * mappings; <code>property2</code>, if non-null is its unmodifiable
	 * equivalent.
	 * Initially <code>null</code>.
	 * 
	 * @see #property2
	 */
	private Object property1 = null;
	
	/**
	 * Auxillary field used in representing node properties efficiently.
	 * 
	 * @see #property1
	 */
	private Object property2 = null;
	
	/**
	 * A character index into the original source string, 
	 * or <code>-1</code> if no source position information is available
	 * for this node; <code>-1</code> by default.
	 */
	private int startPosition = -1;

	/**
	 * A character length, or <code>0</code> if no source position
	 * information is recorded for this node; <code>0</code> by default.
	 */
	private int length = 0;

	/**
	 * Flag constant (bit mask, value 1) indicating that there is something
	 * not quite right with this AST node.
	 * <p>
	 * The standard parser (<code>AST.parseCompilationUnit</code>) sets this
	 * flag on a node to indicate a syntax error detected in the vicinity.
	 * </p>
	 */
	public static final int MALFORMED = 1;

	/**
	 * Flags; none set by default.
	 * 
	 * @see #MALFORMED
	 */
	private int flags = 0;
		
	/**
	 * A specialized implementation of a list of ASTNodes. The
	 * implementation is based on an ArrayList.
	 */ 
	class NodeList extends AbstractList {
		
		/**
		 * The underlying list in which the nodes of this list are
		 * stored (element type: <code>ASTNode</code>).
		 * <p>
		 * Be stingy on storage - assume that list will be empty.
		 * </p>
		 */
		private ArrayList store = new ArrayList(0);
		
		/**
		 * Indicated whether cycles are a risk. A cycle is possible
		 * if the type of nodes that get added to this list could
		 * have a node of the owner's type as a descendent.
		 */
		private boolean cycleCheck;
		
		/**
		 * The declared type of all elements of this list.
		 */
		private Class nodeType;
		
		/**
		 * A cursor for iterating over the elements of the list.
		 * Does not lose its position if the list is changed during
		 * the iteration.
		 */
		class Cursor implements Iterator {
			/**
			 * The position of the cursor between elements. If the value
			 * is N, then the cursor sits between the element at positions
			 * N-1 and N. Initially just before the first element of the
			 * list.
			 */
			private int position = 0;
			
			/* (non-Javadoc)
			 * Method declared on <code>Iterator</code>.
			 */
			public boolean hasNext() {
				return position < store.size();
			}
			
			/* (non-Javadoc)
			 * Method declared on <code>Iterator</code>.
			 */
			public Object next() {
				Object result = store.get(position);
				position++;
				return result;
		    }
			
			/* (non-Javadoc)
			 * Method declared on <code>Iterator</code>.
			 */
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			/**
			 * Adjusts this cursor to accomodate an add/remove at the given
			 * index.
			 * 
			 * @param index the position at which the element was added
			 *    or removed
			 * @param delta +1 for add, and -1 for remove
			 */
			void update(int index, int delta) {
				if (position > index) {
					// the cursor has passed the added or removed element
					position += delta;
				}
			}
		}

		/**
		 * A list of currently active cursors (element type:
		 * <code>Cursor</code>), or <code>null</code> if there are no
		 * active cursors.
		 * <p>
		 * It is important for storage considerations to maintain the
		 * null-means-empty invariant; otherwise, every NodeList instance
		 * will waste a lot of space. A cursor is needed only for the duration
		 * of a visit to the child nodes. Under normal circumstances, only a 
		 * single cursor is needed; multiple cursors are only required if there
		 * are multiple visits going on at the same time.
		 * </p>
		 */
		private List cursors = null;

		/**
		 * Creates a new empty list of nodes owned by this node.
		 * This node will be the common parent of all nodes added to 
		 * this list.
		 * 
		 * @param cycleCheck <code>true</code> if cycles should be
		 *    checked, and <code>false</code> if cycles are not a risk
		 * @param nodeType the type of all elements of this list
		 */
		NodeList(boolean cycleCheck, Class nodeType) {
			super();
			this.cycleCheck = cycleCheck; 
			this.nodeType = nodeType;
		}
	
		/**
		 * @see AbstractCollection
		 */
		public int size() {
			return store.size();
		}
	
		/**
		 * @see AbstractList
		 */
		public Object get(int index) {
			return store.get(index);
		}
	
		/**
		 * @see List
		 */
		public Object set(int index, Object element) {
			// delink old child from parent, and link new child to parent
			ASTNode newChild = (ASTNode) element;
			ASTNode oldChild = (ASTNode) store.get(index);
			if (oldChild == newChild) {
				return oldChild;
			}
			ASTNode.checkNewChild(ASTNode.this, newChild, cycleCheck, nodeType);
			Object result = store.set(index, newChild);
			// n.b. setParent will call modifying()
			oldChild.setParent(null);
			newChild.setParent(ASTNode.this);
			return result;
		}
		
		/**
		 * @see List
		 */
		public void add(int index, Object element) {
			// link new child to parent
			ASTNode newChild = (ASTNode) element;
			ASTNode.checkNewChild(ASTNode.this, newChild, cycleCheck, nodeType);
			store.add(index, element);
			updateCursors(index, +1);
			// n.b. setParent will call modifying()
			newChild.setParent(ASTNode.this);
		}
		
		/**
		 * @see List
		 */
		public Object remove(int index) {
			// delink old child from parent
			ASTNode oldChild = (ASTNode) store.get(index);
			// n.b. setParent will call modifying()
			oldChild.setParent(null);
			Object result = store.remove(index);
			updateCursors(index, -1);
			return result;

		}
		
		/**
		 * Allocate a cursor to use for a visit. The client must call
		 * <code>releaseCursor</code> when done.
		 * 
		 * @return a new cursor positioned before the first element 
		 *    of the list
		 */
		Cursor newCursor() {
			if (cursors == null) {
				// convert null to empty list
				cursors = new ArrayList(1);
			}
			Cursor result = new Cursor();
			cursors.add(result);
			return result;
		}
		
		/**
		 * Releases the given cursor at the end of a visit.
		 * 
		 * @param cursor the cursor
		 */
		void releaseCursor(Cursor cursor) {
			cursors.remove(cursor);
			if (cursors.isEmpty()) {
				// important: convert empty list back to null
				// otherwise the node will hang on to needless junk
				cursors = null;
			}
		}

		/**
		 * Adjusts all cursors to accomodate an add/remove at the given
		 * index.
		 * 
		 * @param index the position at which the element was added
		 *    or removed
		 * @param delta +1 for add, and -1 for remove
		 */
		private void updateCursors(int index, int delta) {
			if (cursors == null) {
				// there are no cursors to worry about
				return;
			}
			for (Iterator it = cursors.iterator(); it.hasNext(); ) {
				Cursor c = (Cursor) it.next();
				c.update(index, delta);
			}
		}
		
		/**
		 * Returns an estimate of the memory footprint of this node list 
		 * instance in bytes.
	     * <ul>
	     * <li>1 object header for the NodeList instance</li>
	     * <li>5 4-byte fields of the NodeList instance</li>
	     * <li>0 for cursors since null unless walk in progress</li>
	     * <li>1 object header for the ArrayList instance</li>
	     * <li>2 4-byte fields of the ArrayList instance</li>
	     * <li>1 object header for an Object[] instance</li>
	     * <li>4 bytes in array for each element</li>
	     * </ul>
	 	 * 
		 * @return the size of this node list in bytes
		 */
		int memSize() {
			int result = HEADERS + 5 * 4;
			result += HEADERS + 2 * 4;
			result += HEADERS + 4 * size();
			return result;
		}

		/**
		 * Returns an estimate of the memory footprint in bytes of this node
		 * list and all its subtrees.
		 * 
		 * @return the size of this list of subtrees in bytes
		 */
		int listSize() {
			int result = memSize();
			for (Iterator it = iterator(); it.hasNext(); ) {
				ASTNode child = (ASTNode) it.next();
				result += child.treeSize();
			}
			return result;
		}
	}

	/**
	 * Creates a new AST node owned by the given AST. Once established,
	 * the relationship between an AST node and its owning AST does not change
	 * over the lifetime of the node. The new node has no parent node,
	 * and no properties.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses my be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ASTNode(AST ast) {
		if (ast == null) {
			throw new IllegalArgumentException();
		}
		owner = ast;
		modifying();
	}
	
	/**
	 * Returns this node's AST.
	 * <p>
	 * Note that the relationship between an AST node and its owing AST does
	 * not change over the lifetime of a node.
	 * </p>
	 * 
	 * @return the AST that owns this node
	 */ 
	public AST getAST() {
		return owner;
	}
	
	/**
	 * Returns this node's parent node, or <code>null</code> if this is the
	 * root node.
	 * <p>
	 * Note that the relationship between an AST node and its parent node
	 * may change over the lifetime of a node.
	 * </p>
	 * 
	 * @return the parent of this node, or <code>null</code> if none
	 */ 
	public ASTNode getParent() {
		return parent;
	}
		
	/**
	 * Returns the root node at or above this node; returns this node if 
	 * it is a root.
	 * 
	 * @return the root node at or above this node
	 */ 
	public ASTNode getRoot() {
		ASTNode candidate = this;
		while (true) {
			ASTNode p = candidate.getParent();
			if (p == null) {
				// candidate has no parent - that's the guy
				return candidate;
			}
			candidate = p;
		}
	}
	
	/**
	 * Internal callback indicating that a field of this node is about to
	 * be modified.
	 */
	void modifying() {
		getAST().modifying();
	}

	/**
	 * Sets or clears this node's parent node.
	 * <p>
	 * Note that this method is package-private. The pointer from a node
	 * to its parent is set implicitly as a side effect of inserting or
	 * removing the node as a child of another node. This method calls
	 * <code>modifying</code>.
	 * </p>
	 * 
	 * @param parent the new parent of this node, or <code>null</code> if none
	 */ 
	void setParent(ASTNode parent) {
		modifying();
		this.parent = parent;
	}
	
	/**
	 * Replaces an old child of this node with another node.
	 * The old child is delinked from its parent (making it a root node),
	 * and the new child node is linked to its parent. The new child node
	 * must be a root node in the same AST as its new parent, and must not
	 * be an ancestor of this node. This operation fails atomically;
	 * all precondition checks are done before any linking and delinking
	 * is done.
	 * <p>
	 * This method calls <code>modifying</code> for the nodes affected.
	 * </p>
	 * 
	 * @param oldChild the old child of this node, or <code>null</code> if
	 *   there was no old child to replace
	 * @param newChild the new child of this node, or <code>null</code> if
	 *   there is no replacement child
	 * @param cycleCheck <code>true</code> if cycles are possible and need to
	 *   be checked, <code>false</code> if cycles are impossible and do not
	 *   need to be checked
	 * @exception $precondition-violation:different-ast$
	 * @exception $precondition-violation:not-unparented$
	 * @exception $postcondition-violation:ast-cycle$
	 */ 
	void replaceChild(ASTNode oldChild, ASTNode newChild, boolean cycleCheck) {
		if (newChild != null) {
			checkNewChild(this, newChild, cycleCheck, null);
		}
		// delink old child from parent
		if (oldChild != null) {
			oldChild.setParent(null);
		}
		// link new child to parent
		if (newChild != null) {
			newChild.setParent(this);
		}
	}

	/**
	 * Checks whether the given new child node is a node 
	 * in a different AST from its parent-to-be, whether it is
	 * already has a parent, and whether adding it to its
	 * parent-to-be would create a cycle. The parent-to-be
	 * is the enclosing instance.
	 * 
	 * @param node the parent-to-be node
	 * @param newChild the new child of the parent, or <code>null</code> 
	 *   if there is no replacement child
	 * @param cycleCheck <code>true</code> if cycles are possible and need 
	 *   to be checked, <code>false</code> if cycles are impossible and do 
	 *   not need to be checked
	 * @param nodeType a type constraint on child nodes, or <code>null</code>
	 *   if no special check is required
	 * @exception $precondition-violation:null-child$
	 * @exception $precondition-violation:different-ast$
	 * @exception $precondition-violation:incorrect-child-type$
	 * @exception $precondition-violation:not-unparented$
	 * @exception $postcondition-violation:ast-cycle$
	 */ 
	static void checkNewChild(ASTNode node, ASTNode newChild,
			boolean cycleCheck, Class nodeType) {
		AST ast = node.getAST();
		if (newChild.getAST() != ast) {
			// new child is from a different AST
			throw new IllegalArgumentException();
		}
		Class childClass = newChild.getClass();
		
//		// FIXME - test is erratic
//		if (nodeType != null && childClass.isAssignableFrom(nodeType)) {
//			// new child is not of the right type
//			throw new IllegalArgumentException();
//		}
		if (newChild.getParent() != null) {
			// new child currently has a different parent
			throw new IllegalArgumentException();
		}
		if (cycleCheck && newChild == node.getRoot()) {
			// inserting new child would create a cycle
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns the named property of this node, or <code>null</code> if none.
	 * 
	 * @param propertyName the property name
	 * @return the property value, or <code>null</code> if none
	 * @see #setProperty
	 */
	public Object getProperty(String propertyName) {
		if (propertyName == null) {
			throw new IllegalArgumentException();
		}
		if (property1 == null) {
			// node has no properties at all
			return null;
		}
		if (property1 instanceof String) {
			// node has only a single property
			if (propertyName.equals(property1)) {
				return property2;
			} else {
				return null;
			}
		}
		// otherwise node has table of properties
		Map m = (Map) property1;
		return m.get(propertyName);
	}
	
	/**
	 * Sets the named property of this node to the given value,
	 * or to <code>null</code> to clear it.
	 * <p>
	 * Clients should employ property names that are sufficiently unique
	 * to avoid inadvertent conflicts with other clients that might also be
	 * setting properties on the same node.
	 * </p>
	 * <p>
	 * Note that modifying a property is not considered a modification to the 
	 * AST itself. This is to allow clients to decorate existing nodes with 
	 * their own properties without jeopardizing certain things (like the 
	 * validity of bindings) which rely on the underlying tree remaining static.
	 * </p>
	 * 
	 * @param propertyName the property name
	 * @param data the new property value, or <code>null</code> if none
	 * @see #getProperty
	 */
	public void setProperty(String propertyName, Object data) {
		if (propertyName == null) {
			throw new IllegalArgumentException();
		}
		// N.B. DO NOT CALL modifying();

		if (property1 == null) {
			// node has no properties at all
			if (data == null) {
				// we already know this
				return;
			}
			// node gets its fist property
			property1 = propertyName;
			property2 = data;
			return;
		}

		if (property1 instanceof String) {
			// node has only a single property
			if (propertyName.equals(property1)) {
				// we're in luck
				property2 = data;
				if (data == null) {
					// just deleted last property
					property1 = null;
					property2 = null;
				}
				return;
			}
			if (data == null) {
				// we already know this
				return;
			}
			// node already has one property - getting its second
			// convert to more flexible representation
			HashMap m = new HashMap(2);
			m.put(property1, property2);
			m.put(propertyName, data);
			property1 = m;
			property2 = null;
			return;
		}
			
		// node has two or more properties
		HashMap m = (HashMap) property1;
		if (data == null) {
			m.remove(propertyName);
			// check for just one property left
			if (m.size() == 1) {
				// convert to more efficient representation
				Map.Entry[] entries = (Map.Entry[]) m.entrySet().toArray(new Map.Entry[1]);
				property1 = entries[0].getKey();
				property2 = entries[0].getValue();
			}
			return;
		} else {
			m.put(propertyName, data);
			// still has two or more properties
			return;
		}
	}

	/**
	 * Returns an unmodifiable table of the properties of this node with 
	 * non-<code>null</code> values.
	 * 
	 * @return the table of property values keyed by property name
	 *   (key type: <code>String</code>; value type: <code>Object</code>)
	 */
	public Map properties() {
		if (property1 == null) {
			// node has no properties at all
			return UNMODIFIABLE_EMPTY_MAP;
		} 
		if (property1 instanceof String) {
			// node has a single property
			return Collections.singletonMap(property1, property2);
		}
		
		// node has two or more properties
		if (property2 == null) {
			property2 = Collections.unmodifiableMap((Map) property1);
		}
		// property2 is unmodifiable wrapper for map in property1
		return (Map) property2;
	}
	
	/**
	 * Returns the flags associated with this node.
	 * <p>
	 * No flags are associated with newly-created nodes.
	 * </p>
	 * <p>
	 * The flags are the bitwise-or of individual flags.
	 * The following flags are currently defined:
	 * <ul>
	 * <li><code>MALFORMED</code> - indicates node is syntactically 
	 *   malformed</li>
	 * </ul>
	 * Other bit positions are reserved for future use.
	 * </p>
	 * 
	 * @return the bitwise-or of individual flags
	 * @see #setFlags
	 * @see #MALFORMED
	 */
	public int getFlags() {
		return flags;
	}
	
	/**
	 * Sets the flags associated with this node to the given value.
	 * <p>
	 * The flags are the bitwise-or of individual flags.
	 * The following flags are currently defined:
	 * <ul>
	 * <li><code>MALFORMED</code> - indicates node is syntactically 
	 *   malformed</li>
	 * </ul>
	 * Other bit positions are reserved for future use.
	 * </p>
	 * 
	 * @param flags the bitwise-or of individual flags
	 * @see #getFlags
	 * @see #MALFORMED
	 */
	public void setFlags(int flags) {
		modifying();
		this.flags = flags;
	}

	/**
	 * The <code>ASTNode</code> implementation of this <code>Object</code>
	 * method uses object identity (==). Use <code>subtreeEquals</code> to
	 * compare two subtrees for equality.
	 * 
	 * @see #subtreeEquals
	 */
	public final boolean equals(Object obj) {
		return this == obj; // equivalent to Object.equals
	}

	/**
	 * Returns whether the subtree rooted at the given node is the same
	 * as the subtree rooted at the given node. Returns <code>false</code>
	 * if the given node is <code>null</code>.
	 * <p>
	 * [Explain subtree isomorphism. The subtrees may or may not be from
	 *  the same ASTs.
	 *  Comments (and source positions, etc.) are ignored.
	 * ]
	 * </p>
	 * 
	 * @param other the root of the AST subtree, or <code>null</code>
	 * @return <code>true</code> if the subtrees are the same, or 
	 * <code>false</code> if the subtrees are different or the other node is
	 * <code>null</code>
	 * @deprecated Use subtreeMatch(new ASTMatcher(), other)
	 */
	public boolean subtreeEquals(ASTNode other) {
		return subtreeMatch(new ASTMatcher(), other);
	}
	
	/**
	 * Returns whether the subtree rooted at the given node matches the
	 * given other object as decided by the given matcher.
	 * 
	 * @param matcher the matcher
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 * <code>false</code> if they do not match
	 */
	public abstract boolean subtreeMatch(ASTMatcher matcher, Object other);
	
	/**
	 * Returns a deep copy of the subtree of AST nodes rooted at the
	 * given node. The resulting nodes are owned by the given AST,
	 * which may be different from the ASTs of the given node. 
	 * Even if the given node has a parent, the result node will be unparented.
	 * <p>
	 * Note that client properties are not carried over to the new nodes.
	 * </p>
	 * 
	 * @param target the AST that is to own the nodes in the result
	 * @param node the node to copy, or <code>null</code> if none
	 * @return the copied node, or <code>null</code> if <code>node</code>
	 *    is <code>null</code>
	 */
	public static ASTNode copySubtree(AST target, ASTNode node) {
		if (node == null) {
			return null;
		}
		ASTNode newNode = node.clone(target);
		return newNode;
	}

	/**
	 * Returns a deep copy of the subtrees of AST nodes rooted at the
	 * given list of nodes. The resulting nodes are owned by the given AST,
	 * which may be different from the ASTs of the nodes in the list. 
	 * Even if the nodes in the list have parents, the nodes in the result
	 * will be unparented.
	 * <p>
	 * Note that client properties are not carried over to the new nodes.
	 * </p>
	 * 
	 * @param target the AST that is to own the nodes in the result
	 * @param nodes the list of nodes to copy
	 *    (element type: <code>ASTNode</code>)
	 * @return the list of copied subtrees
	 *    (element type: <code>ASTNode</code>)
	 */
	public static List copySubtrees(AST target, List nodes) {
		List result = new ArrayList(nodes.size());
		for (Iterator it = nodes.iterator(); it.hasNext(); ) {
			ASTNode oldNode = (ASTNode) it.next();
			ASTNode newNode = oldNode.clone(target);
			result.add(newNode);
		}
		return result;
	}

	/**
	 * Returns a deep copy of the subtree of AST nodes rooted at this node.
	 * The resulting nodes are owned by the given AST, which may be different
	 * from the AST of this node. Even if this node has a parent, the 
	 * result node will be unparented.
	 * <p>
	 * N.B. This method is package-private, so that the implementations
	 * of this method in each of the concrete AST node types do not
	 * clutter up the API doc.
	 * </p>
	 * 
	 * @param target the AST that is to own the nodes in the result
	 * @return the root node of the copies subtree
	 */
	abstract ASTNode clone(AST target);

	/**
	 * Accepts the given visitor on a visit of the current node.
	 * This method much be implemented in all concrete AST node types.
	 * 
	 * @param visitor the visitor object
	 * @exception $precondition-violation:illegal-argument$
	 */
	public final void accept(ASTVisitor visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException();
		}
		// begin with the generic pre-visit
		visitor.preVisit(this);
		// dynamic dispatch to internal method for type-specific visit/endVisit
		accept0(visitor);
		// end with the generic post-visit
		visitor.postVisit(this);
	}

	/**
	 * Accepts the given visitor on a type-specific visit of the current node.
	 * This method must be implemented in all concrete AST node types.
	 * <p>
	 * General template for implementation on each concrete ASTNode class:
	 * <pre>
	 * <code>
	 * boolean visitChildren = visitor.visit(this);
	 * if (visitChildren) {
	 *    // visit children in normal left to right reading order
	 *    acceptChild(visitor, getProperty1());
	 *    acceptChildren(visitor, rawListProperty);
	 *    acceptChild(visitor, getProperty2());
	 * }
	 * visitor.endVisit(this);
	 * </code>
	 * </pre>
	 * Note that <code>accept</code>, <code>acceptChild</code>,
	 * and <code>acceptChildren</code> take care of invoking
	 * <code>visitor.preVisit</code> and <code>visitor.postVisit</code>.
	 * </p>
	 * 
	 * @param visitor the visitor object
	 */
	abstract void accept0(ASTVisitor visitor);

	/**
	 * Accepts the given visitor on a visit of the current node.
	 * <p>
	 * This method should be used by the concrete implementations of
	 * <code>accept0</code> to traverse optional properties. Equivalent
	 * to <code>child.accept0(visitor)</code> if <code>child</code>
	 * is not <code>null</code>.
	 * </p>
	 * 
	 * @param visitor the visitor object
	 * @param child the child AST node to dispatch too, or <code>null</code>
	 *    if none
	 */
	final void acceptChild(ASTVisitor visitor, ASTNode child) {
		if (child == null) {
			return;
		}
		// begin with the generic pre-visit
		visitor.preVisit(this);
		// dynamic dispatch to internal method for type-specific visit/endVisit
		child.accept0(visitor);
		// end with the generic post-visit
		visitor.postVisit(this);
	}

	/**
	 * Accepts the given visitor on a visit of the given live list of
	 * child nodes. 
	 * <p>
	 * This method must be used by the concrete implementations of
	 * <code>accept0</code> to traverse list-values properties; it
	 * encapsulates the proper handling of on-the-fly changes to the list.
	 * </p>
	 * 
	 * @param visitor the visitor object
	 * @param child the child AST node to dispatch too, or <code>null</code>
	 *    if none
	 */
	final void acceptChildren(ASTVisitor visitor, ASTNode.NodeList children) {
		// use a cursor to keep track of where we are up to
		// (the list may be changing under foot)
		NodeList.Cursor cursor = children.newCursor();
		try {
			while (cursor.hasNext()) {
				ASTNode child = (ASTNode) cursor.next();
				// dynamic dispatch to internal method
				// begin with the generic pre-visit
				visitor.preVisit(this);
				// dynamic dispatch to internal method for type-specific visit/endVisit
				child.accept0(visitor);
				// end with the generic post-visit
				visitor.postVisit(this);
			}
		} finally {
			children.releaseCursor(cursor);
		}
	}

	/**
	 * Returns the character index into the original source file indicating
	 * where the source fragment corresponding to this node begins.
	 * 
	 * @return the 0-based character index, or <code>-1</code>
	 *    if no source position information is recorded for this node
	 * @see #getLength
	 */
	public int getStartPosition() {
		return startPosition;
	}

	/**
	 * Returns the length in character of the original source file indicating
	 * where the source fragment corresponding to this node ends.
	 * 
	 * @return a (possibly 0) length, or <code>0</code>
	 *    if no source position information is recorded for this node
	 * @see #getStartPositions
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Sets the source range of the original source file where the source
	 * fragment corresponding to this node was found.
	 * 
	 * @param startPosition a 0-based character index, 
	 *    or <code>-1</code> if no source position information is 
	 *    available for this node
	 * @param length a (possibly 0) length, 
	 *    or <code>0</code> if no source position information is recorded 
	 *    for this node
	 * @see #getStartPosition
	 * @see #getLength
	 */
	public void setSourceRange(int startPosition, int length) {
		if (startPosition >= 0 && length < 0) {
			throw new IllegalArgumentException();
		}
		if (startPosition < 0 && length != 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.startPosition = startPosition;
		this.length = length;
	}
	
	/**
	 * Returns a string representation of this node suitable for debugging
	 * purposes only.
	 * 
	 * @return a debug string 
	 */
	public final String toString() {
		// allocate a buffer that is large enough to hold an average compilation unit
		StringBuffer buffer = new StringBuffer(6000);
		int p = buffer.length();
		try {
			appendDebugString(buffer);
		} catch (RuntimeException e) {
			// since debugger sometimes call toString methods, problems can easily happen when
			// toString is called on an instance that is being initialized
			buffer.setLength(p);
			buffer.append("!");
			buffer.append(standardToString());
		}
		// convert to a string, but lose the extra space in the string buffer by copying
		return new String(buffer.toString());
	}
	
	/**
	 * Returns the string representation of this node produced by the standard
	 * <code>Object.toString</code> method.
	 * 
	 * @return a debug string 
	 */
	final String standardToString() {
		return super.toString();
	}
	
	/**
	 * Appends a debug representation of this node to the given string buffer.
	 * <p>
	 * The <code>ASTNode</code> implementation of this method prints out the entire 
	 * subtree. Subclasses may override to provide a more succinct representation.
	 * </p>
	 * 
	 * @param buffer the string buffer to append to
	 */
	void appendDebugString(StringBuffer buffer) {
		// print the subtree by default
		appendPrintString(buffer);
	}
		
	/**
	 * Appends a standard Java source code representation of this subtree to the given
	 * string buffer.
	 * 
	 * @param buffer the string buffer to append to
	 */
	final void appendPrintString(StringBuffer buffer) {
		NaiveASTFlattener printer = new NaiveASTFlattener();
		this.accept(printer);
		buffer.append(printer.getResult());
	}
	
	/**
	 * Estimate of size of an object header in bytes.
	 */
	static final int HEADERS = 12;
	
	/**
	 * Approximate base size of an AST node instance in bytes, 
	 * including object header and instance fields.
	 */
	static final int BASE_NODE_SIZE = HEADERS + 6 * 4;
	
	/**
	 * Returns an estimate of the memory footprint in bytes of the entire 
	 * subtree rooted at this node.
	 * 
	 * @return the size of this subtree in bytes
	 */
	public final int subtreeBytes() {
		return treeSize();
	}
		
	/**
	 * Returns an estimate of the memory footprint in bytes of the entire 
	 * subtree rooted at this node.
	 * <p>
	 * N.B. This method is package-private, so that the implementations
	 * of this method in each of the concrete AST node types do not
	 * clutter up the API doc.
	 * </p>
	 * 
	 * @return the size of this subtree in bytes
	 */
	abstract int treeSize();

	/**
	 * Returns an estimate of the memory footprint of this node in bytes.
	 * The estimate does not include the space occupied by child nodes.
	 * 
	 * @return the size of this node in bytes
	 */
	abstract int memSize();
}
