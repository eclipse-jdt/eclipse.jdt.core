package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * An entry in the state's principal structure table
 */
public class TypeStructureEntry extends StateTables {
	SourceEntry fSourceEntry;
	IType fType;
	int fCRC32;

	static IType[] fgEmptyTypeList = new IType[0];
	/**
	 * Creates a new TypeStructureEntry.
	 */
	TypeStructureEntry(SourceEntry sEntry, IType type) {
		this(sEntry, type, 0);
	}
	/**
	 * Creates a new TypeStructureEntry.
	 */
	TypeStructureEntry(SourceEntry sEntry, IType type, int crc) {
		fSourceEntry = sEntry;
		fType = type;
		fCRC32 = crc;
	}
	/**
	 * Returns true if the given object is equal to this one, and false otherwise
	 * A TypeStructureEntry equals method is needed because they are keys to the
	 * TypeDescriptorCache in the State.
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TypeStructureEntry)) return false;

		TypeStructureEntry e = (TypeStructureEntry)o;
		return fType.equals(e.fType);
	}
int getCRC32() {
	Assert.isTrue(fCRC32 != 0);
	return fCRC32;
}
	/**
	 * Returns the key for this type in the dependency graph.
	 */
	Object getDependencyGraphKey() {
		if (fSourceEntry.isSource()) {
			return new PackageElement(fType.getPackage(), fSourceEntry);
		}
		else {
			return fType;
		}
	}
	public SourceEntry getSourceEntry() {
		return fSourceEntry;
	}
	ISourceFragment getSourceFragment() {
		return new SourceFragmentImpl(
			-1, -1,
			fSourceEntry);
	}
	public IType getType() {
		return fType;
	}
	/**
	 * Returns a consistent hashcode for this entry.
	 * A hashcode method is needed because TypeStructureEntrys are keys to the
	 * TypeDescriptorCache in the State.
	 */
	public int hashcode() {
		return fType.hashCode();
	}
	/**
	 * Returns true if this entry comes from a binary file, otherwise
	 * returns false.
	 */
	boolean isBinary() {
		return fSourceEntry.isBinary();
	}
	void setCRC32(int val) {
		fCRC32 = val;
	}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	StringBuffer buf = new StringBuffer("TypeStructureEntry("/*nonNLS*/);
	if (fType != null) {
		buf.append(fType.getName());
	}
	buf.append(")"/*nonNLS*/);
	return buf.toString();
}
}
