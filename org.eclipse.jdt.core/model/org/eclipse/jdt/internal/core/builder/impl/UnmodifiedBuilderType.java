package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;

/**
 * A type that exists in the old state, and is not being 
 * compiled during the current build.  Its structure will 
 * be the same in both the old and new states.
 */
public class UnmodifiedBuilderType extends NewBuilderType {
/**
 * UnmodifiedBuilderType constructor comment.
 */
public UnmodifiedBuilderType(IncrementalImageBuilder builder, TypeStructureEntry entry) {
	super(builder, entry);

	/* we don't yet know if there is a hierarchy change */
	fComputedHierarchy = false;
}
/**
 * Should not be trying to compute indictments for an unmodified type.
 */
public void computeIndictments(IndictmentSet indictments) {
	Assert.isTrue(false);
}
/**
 * Returns true if there is a change to the supertype hierarchy of this type.
 * Needs to check the whole hierarchy because changes from higher up are -not- 
 * automatically propagated through the dependency graph.
 */
protected boolean detectHierarchyChange() {
	return false;
}
/**
 * Returns the tsEntry in the old state (same in both states)
 */
public TypeStructureEntry getOldTypeStructureEntry() {
	return fNewTSEntry;
}
/**
 * Returns true if the given type was affected by the build
 * (either added, removed, or changed)
 */
public boolean isAffected() {
	return false;
}
/**
 * For debugging only
 */
public String toString() {
	return "UnmodifiedBuilderType("/*nonNLS*/ + fNewTSEntry.getType().getName() + ")"/*nonNLS*/;
}
}
