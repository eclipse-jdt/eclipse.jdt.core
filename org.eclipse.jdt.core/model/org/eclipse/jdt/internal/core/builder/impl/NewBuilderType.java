package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * A type that only exists in the new state (an addition)
 */
public class NewBuilderType extends BuilderType {
	/**
	 * The tsEntry in the new state
	 */
	protected TypeStructureEntry fNewTSEntry;
/**
 * NewBuilderType constructor comment.
 */
public NewBuilderType(IncrementalImageBuilder builder, TypeStructureEntry newEntry) {
	/* this is a new type, which is a hierarchy change */
	super(builder, true, true);
	fNewTSEntry = newEntry;
}
/**
 * No indictments for a new type.
 */
public void computeIndictments(IndictmentSet indictments) {
}
/**
 * Returns the new tsEntry
 */
public TypeStructureEntry getNewTypeStructureEntry() {
	return fNewTSEntry;
}
/**
 * Added types do not have an entry in the old state.
 */
public TypeStructureEntry getOldTypeStructureEntry() {
	return null;
}
/**
 * Sets the tsEntry in the new state
 */
public void setNewTypeStructureEntry(TypeStructureEntry newEntry) {
	fNewTSEntry = newEntry;
}
/**
 * For debugging only
 */
public String toString() {
	return "NewBuilderType("/*nonNLS*/ + fNewTSEntry.getType().getName() + ")"/*nonNLS*/;
}
}
