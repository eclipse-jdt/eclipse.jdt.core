package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.IType;

/**
 * A type that existed in the old state.  Unless
 * subclassed, this type does not exist in the new state.
 */
public class OldBuilderType extends BuilderType {
	/**
	 * The tsEntry for this type in the old state
	 */
	protected TypeStructureEntry fOldTSEntry;

	/**
	 * Creates a new OldBuilderType
	 */
	public OldBuilderType(
		IncrementalImageBuilder builder,
		TypeStructureEntry oldEntry) {
		/* this is a deleted type, which is a hierarchy change */
		super(builder, true, true);
		fOldTSEntry = oldEntry;
	}

	/**
	 * Adds the indictments for the descriptor's type, methods,
	 * and fields.  Usually used when a type has been added or removed.
	 */
	public void computeIndictments(IndictmentSet set) {
		/* dependents should already have been compiled */
	}

	/**
	 * Old builder types don't necessarily exist in the new state.
	 */
	public TypeStructureEntry getNewTypeStructureEntry() {
		return null;
	}

	/**
	 * Returns the old tsEntry
	 */
	public TypeStructureEntry getOldTypeStructureEntry() {
		return fOldTSEntry;
	}

	/**
	 * Sets the tsEntry in the new state
	 */
	public void setNewTypeStructureEntry(TypeStructureEntry newEntry) {
		Assert.isTrue(false);
	}

	/**
	 * For debugging only
	 */
	public String toString() {
		return "OldBuilderType(" + fOldTSEntry.getType().getName() + ")";
	}

}
