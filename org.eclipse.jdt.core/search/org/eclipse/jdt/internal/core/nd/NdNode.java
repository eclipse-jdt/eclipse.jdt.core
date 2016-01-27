/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * This is a basic node in the PDOM database.
 * PDOM nodes form a multi-root tree.
 * This class managed the parent pointer.
 * @since 3.12
 */
public abstract class NdNode implements IDestructable {
	public static final FieldShort NODE_TYPE;

	public static final StructDef<NdNode> type;

	static {
		type = StructDef.create(NdNode.class);
		NODE_TYPE = type.addShort();
		type.done();
	}

	public final long address;
	private Nd pdom;

	public static long addressOf(NdNode nullable) {
		if (nullable == null) {
			return 0;
		}
		return nullable.address;
	}

	/**
	 * Load a node from the specified address in the given database.  Return null if a node cannot
	 * be loaded.
	 *
	 * @param pdom The PDOM from which to load the node.
	 * @param address The address of the node in the given PDOM.
	 * @return The PDOMNode at the specified location or null if a node cannot be loaded.
	 * @When there is a problem reading the given pdom's Database
	 */
	public static NdNode load(Nd pdom, long address) {
		if (address == 0) {
			return null;
		}

		return pdom.getNode(address, NODE_TYPE.get(pdom, address));
	}

	@SuppressWarnings("unchecked")
	public static <T extends NdNode> T load(Nd pdom, long address, Class<T> clazz) {
		if (address == 0) {
			return null;
		}

		NdNode result = pdom.getNode(address, NODE_TYPE.get(pdom, address));

		if (!clazz.isAssignableFrom(result.getClass())) {
			throw new IndexException("Found wrong data type at address " + address + ". Expected a subclass of " +  //$NON-NLS-1$//$NON-NLS-2$
				clazz.getClass() + " but found " + result.getClass()); //$NON-NLS-1$
		}

		return (T)result;
	}

	/**
	 * Invokes the destructor on this node and frees up its memory
	 */
	public final void delete() {
		getPDOM().delete(this.address);
	}

	protected NdNode(Nd pdom, long address) {
		this.pdom = pdom;
		this.address = address;
	}

	protected NdNode(Nd pdom) {
		Database db = pdom.getDB();
		this.pdom = pdom;

		short nodeType = pdom.getNodeType(getClass());
		ITypeFactory<? extends NdNode> factory1 = pdom.getTypeFactory(nodeType);

		this.address = db.malloc(factory1.getRecordSize());

		NODE_TYPE.put(pdom, this.address, nodeType);
	}

	protected Database getDB() {
		return this.pdom.getDB();
	}

	public Nd getPDOM() {
		return this.pdom;
	}

	/**
	 * Return a value to uniquely identify the node within the factory that is responsible for loading
	 * instances of this node from the PDOM.
	 * <b>
	 */
	public short getNodeType() {
		return this.pdom.getNodeType(getClass());
	}

	public final long getAddress() {
		return this.address;
	}

	public final long getBindingID() {
		return this.address;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof NdNode) {
			NdNode other = (NdNode) obj;
			return getPDOM() == other.getPDOM() && this.address == other.address;
		}

		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return (int) (this.address >> Database.BLOCK_SIZE_DELTA_BITS);
	}

	public void accept(IPDOMVisitor visitor) {
		// No children here.
	}

	/**
	 * Return an value to globally identify the given node within the given linkage.  This value
	 * can be used for comparison with other PDOMNodes.
	 */
	public static int getNodeId(int linkageID, int nodeType) {
		return (linkageID << 16) | (nodeType & 0xffff);
	}

	/**
	 * Convenience method for fetching a byte from the database.
	 * @param offset Location of the byte.
	 * @return a byte from the database.
	 */
	protected byte getByte(long offset) {
		return getDB().getByte(offset);
	}

	/**
	 * Returns the bit at the specified offset in a bit vector.
	 * @param bitVector Bits.
	 * @param offset The position of the desired bit.
	 * @return the bit at the specified offset.
	 */
	protected static boolean getBit(int bitVector, int offset) {
		int mask = 1 << offset;
		return (bitVector & mask) != 0;
	}

	/**
	 * Dispose this PDOMNode. Subclasses should extend this method to perform any high-level node-specific cleanup.
	 * This will be invoked prior to disposing the fields. Implementations must invoke their parent's destruct method
	 * and should not destruct the fields.
	 * <p>
	 * If an external object wants to destroy a node, they should invoke {@link NdNode#delete} rather than this
	 * method.
	 */
	public void destruct() {
	}

}