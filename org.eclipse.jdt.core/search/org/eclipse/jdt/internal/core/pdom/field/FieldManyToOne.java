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
package org.eclipse.jdt.internal.core.pdom.field;

import org.eclipse.jdt.internal.core.pdom.ITypeFactory;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;

/**
 * Declares a PDOM field which is a pointer of a PDOMNode of the specified type. {@link FieldManyToOne} forms a
 * one-to-many relationship with {@link FieldOneToMany}. Whenever a {@link FieldManyToOne} points to an object, the
 * inverse pointer is automatically inserted into the matching back pointer list.
 * 
 * @since 3.12
 */
public class FieldManyToOne<T extends PDOMNode> implements IDestructableField, IField, IRefCountedField {
	public final static FieldPointer TARGET;
	public final static FieldInt BACKPOINTER_INDEX;

	private int offset;
	Class<T> targetType;
	final Class<? extends PDOMNode> localType;
	FieldOneToMany<?> backPointer;
	private final static StructDef<FieldManyToOne> type;
	/**
	 * True iff the other end of this pointer should delete this object when its end of the pointer is cleared.
	 */
	public final boolean pointsToOwner;

	static {
		type = StructDef.createAbstract(FieldManyToOne.class);
		TARGET = type.addPointer();
		BACKPOINTER_INDEX = type.addInt();
		type.done();
	}

	/**
	 * @param nodeType
	 * @param backPointer
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private FieldManyToOne(Class<? extends PDOMNode> localType, FieldOneToMany<?> backPointer, boolean pointsToOwner) {
		this.localType = localType;
		this.pointsToOwner = pointsToOwner;

		if (backPointer != null) {
			if (backPointer.forwardPointer != null && backPointer.forwardPointer != this) {
				throw new IllegalArgumentException(
						"Attempted to construct a FieldNodePointer referring to a backpointer list that is already in use" //$NON-NLS-1$
								+ " by another field"); //$NON-NLS-1$
			}
			backPointer.targetType = (Class) localType;
			this.targetType = (Class) backPointer.localType;
			backPointer.forwardPointer = this;
		}
		this.backPointer = backPointer;
	}

	public static <T extends PDOMNode, B extends PDOMNode> FieldManyToOne<T> create(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {
		FieldManyToOne<T> result = new FieldManyToOne<T>(builder.getStructClass(), forwardPointer, false);
		builder.add(result);
		builder.addDestructableField(result);
		return result;
	}

	/**
	 * Creates a many-to-one pointer which points to this object's owner. If the pointer is non-null when the owner is
	 * deleted, this object will be deleted too.
	 * 
	 * @param builder
	 * @param forwardPointer
	 * @return
	 */
	public static <T extends PDOMNode, B extends PDOMNode> FieldManyToOne<T> createOwner(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {

		FieldManyToOne<T> result = new FieldManyToOne<T>(builder.getStructClass(), forwardPointer, true);
		builder.add(result);
		builder.addDestructableField(result);
		builder.addOwnerField(result);
		return result;
	}

	public T get(PDOM pdom, long address) {
		return PDOMNode.load(pdom, getAddress(pdom, address), this.targetType);
	}

	public long getAddress(PDOM pdom, long address) {
		return pdom.getDB().getRecPtr(address + this.offset);
	}

	/**
	 * Directs this pointer to the given target. Also removes this pointer from the old backpointer list (if any) and
	 * inserts it into the new backpointer list (if any)
	 */
	public void put(PDOM pdom, long address, T value) {
		if (value != null) {
			put(pdom, address, value.address);
		} else {
			put(pdom, address, 0);
		}
	}

	public void put(PDOM pdom, long address, long newTargetAddress) {
		long fieldStart = address + this.offset;
		if (this.backPointer == null) {
			throw new IllegalStateException("FieldNodePointer must be associated with a FieldBackPointer"); //$NON-NLS-1$
		}

		long oldTargetAddress = TARGET.get(pdom, fieldStart);
		if (oldTargetAddress == newTargetAddress) {
			return;
		}

		if (oldTargetAddress != 0) {
			int oldIndex = BACKPOINTER_INDEX.get(pdom, fieldStart);

			this.backPointer.remove(pdom, oldTargetAddress, oldIndex);

			if (oldTargetAddress != 0) {
				short targetTypeId = PDOMNode.NODE_TYPE.get(pdom, oldTargetAddress);

				ITypeFactory<T> typeFactory = pdom.getTypeFactory(targetTypeId);

				if (typeFactory.getDeletionSemantics() == StructDef.DeletionSemantics.REFCOUNTED 
						&& typeFactory.isReadyForDeletion(pdom, oldTargetAddress)) {
					pdom.scheduleDeletion(oldTargetAddress);
				}
			}
		}
		TARGET.put(pdom, fieldStart, newTargetAddress);
		if (newTargetAddress != 0) {
			// Note that newValue is the address of the backpointer list and record (the address of the struct
			// containing the forward pointer) is the value being inserted into the list.
			BACKPOINTER_INDEX.put(pdom, fieldStart, this.backPointer.add(pdom, newTargetAddress, address));
		} else {
			if (this.pointsToOwner) {
				pdom.scheduleDeletion(address);
			}
		}
	}

	/**
	 * Called when the index of this forward pointer has moved in the backpointer list. Adjusts the index.
	 * <p>
	 * Not intended to be called by clients. This is invoked by {@link FieldOneToMany} whenever it reorders elements in
	 * the array.
	 */
	void adjustIndex(PDOM pdom, long address, int index) {
		BACKPOINTER_INDEX.put(pdom, address + this.offset, index);
	}

	@Override
	public void destruct(PDOM pdom, long address) {
		put(pdom, address, 0);
	}

	void clearedByBackPointer(PDOM pdom, long address) {
		long fieldStart = this.offset + address;
		FieldManyToOne.TARGET.put(pdom, fieldStart, 0);
		FieldManyToOne.BACKPOINTER_INDEX.put(pdom, fieldStart, 0);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return type.size();
	}

	@Override
	public boolean hasReferences(PDOM pdom, long address) {
		long fieldStart = this.offset + address;
		long target = TARGET.get(pdom, fieldStart);
		return target != 0;
	}
}
