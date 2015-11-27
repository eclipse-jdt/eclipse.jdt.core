package org.eclipse.jdt.internal.core.pdom.field;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.db.Database;

/**
 * Represents a 1-to-0..1 relationship in a PDOM database.
 */
public class FieldOneToOne<T extends PDOMNode> implements IField, IDestructableField {
	private int offset;
	public final Class<T> nodeType; 
	FieldOneToOne<?> backPointer;
	private boolean pointsToOwner;

	/**
	 * @param nodeType
	 * @param backPointer
	 */
	private FieldOneToOne(Class<T> nodeType, FieldOneToOne<?> backPointer, boolean pointsToOwner) {
		this.nodeType = nodeType;

		if (backPointer != null) {
			if (backPointer.backPointer != null && backPointer.backPointer != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldOneToOne referring to a backpointer list that is already in use" //$NON-NLS-1$
						+ " by another field"); //$NON-NLS-1$
			}
			backPointer.backPointer = this;
		}
		this.backPointer = backPointer;
		this.pointsToOwner = pointsToOwner;
	}

	public static <T extends PDOMNode, B extends PDOMNode> FieldOneToOne<T> create(StructDef<B> builder,
			Class<T> nodeType, FieldOneToOne<B> forwardPointer) {

		FieldOneToOne<T> result = new FieldOneToOne<T>(nodeType, forwardPointer, false);
		builder.add(result);
		builder.addDestructableField(result);
		return result;
	}

	public static <T extends PDOMNode, B extends PDOMNode> FieldOneToOne<T> createOwner(StructDef<B> builder,
			Class<T> nodeType, FieldOneToOne<B> forwardPointer) {

		FieldOneToOne<T> result = new FieldOneToOne<T>(nodeType, forwardPointer, true);
		builder.add(result);
		builder.addDestructableField(result);
		return result;
	}

	public T get(PDOM pdom, long address) {
		long ptr = pdom.getDB().getRecPtr(address + this.offset);
		return PDOMNode.load(pdom, ptr, this.nodeType);
	}

	public void put(PDOM pdom, long address, T target) {
		cleanup(pdom, address);
		pdom.getDB().putRecPtr(address + this.offset, target == null ? 0 : target.address);
	}

	@Override
	public void destruct(PDOM pdom, long address) {
		cleanup(pdom, address);
	}

	private void cleanup(PDOM pdom, long address) {
		Database db = pdom.getDB();
		long ptr = db.getRecPtr(address + this.offset);
		if (ptr != 0) {
			// If we own our target, delete it
			if (this.backPointer.pointsToOwner) {
				PDOMNode.delete(pdom, ptr);
			} else {
				db.putRecPtr(ptr + this.backPointer.offset, 0);
			}
		}
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return Database.PTR_SIZE;
	}
}
