package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.builder.IImage;
import org.eclipse.jdt.internal.core.builder.IPackage;
import org.eclipse.jdt.internal.core.builder.IType;

import org.eclipse.jdt.internal.compiler.util.*;

/**
 * This class represents a type that is being examined by
 * the incremental image builder.  There is a one-one correspondence
 * between builder types and class files.  Each builder type
 * has a structure in both the old and new states.
 */
public abstract class BuilderType {
	/**
	 * The builder associated with this builder type
	 */
	protected IncrementalImageBuilder fBuilder;

	/**
	 * For computing hierarchy changes
	 */
	protected boolean fComputedHierarchy;
	protected boolean fHasHierarchyChange;
/**
 * Creates a new BuilderType.
 */
public BuilderType(IncrementalImageBuilder builder, boolean hasComputedHierarchy, boolean hasHierarchyChange) {
	fBuilder = builder;
	fComputedHierarchy = hasComputedHierarchy;
	fHasHierarchyChange = hasHierarchyChange;
}
/**
 * Computes the indictments for this type and adds them to the
 * given indictment set.
 */
public abstract void computeIndictments(IndictmentSet indictments);
/**
 * Returns true if there is a change to the supertype hierarchy of this type.
 * Needs to check the whole hierarchy because changes from higher up are -not- 
 * automatically propagated through the dependency graph.
 */
protected boolean detectHierarchyChange() {
	/* If we've already done the work, return the result */
	if (fComputedHierarchy) {
		return fHasHierarchyChange;
	}
	fComputedHierarchy = true;
	if (getNewTypeStructureEntry() == null) {
		return fHasHierarchyChange = true;
	}
	IBinaryType oldType = getOldBinaryType();
	if (oldType == null){
		return fHasHierarchyChange = true;
	}
	IBinaryType newType = getNewBinaryType();

	/* check superclasses */
	char[] oldSuper = oldType.getSuperclassName();
	char[] newSuper = newType.getSuperclassName();
	if (oldSuper == null ^ newSuper == null) {
		return fHasHierarchyChange = true;
	}
	if (oldSuper != null && newSuper != null) {
		if (!CharOperation.equals(oldSuper, newSuper)) {
			return fHasHierarchyChange = true;
		}

		/* recurse on superclass */
		BuilderType superBuilderType = fBuilder.getBuilderType(BinaryStructure.getType(getNewState(), getNewTypeStructureEntry(), oldSuper));
		if (superBuilderType.detectHierarchyChange()) {
			return fHasHierarchyChange = true;
		}
	}

	/* check interfaces */
	char[][] oldInterfaces = oldType.getInterfaceNames();
	char[][] newInterfaces = newType.getInterfaceNames();
	if (!CharOperation.equals(oldInterfaces, newInterfaces)) {
		return fHasHierarchyChange = true;
	}

	/* recurse on interfaces */
	if (oldInterfaces != null) {
		for (int i = 0; i < oldInterfaces.length; i++) {
			BuilderType superBuilderType = fBuilder.getBuilderType(BinaryStructure.getType(getNewState(), getNewTypeStructureEntry(), oldInterfaces[i]));
			if (superBuilderType.detectHierarchyChange()) {
				return fHasHierarchyChange = true;
			}
		}
	}
	return fHasHierarchyChange = false;
}
/**
 * Returns the binary type in the new state
 */
public IBinaryType getNewBinaryType() {
	return getNewState().getBinaryType(getNewTypeStructureEntry());
}
/**
 * Returns the new state
 */
protected StateImpl getNewState() {
	return (StateImpl)fBuilder.getNewState();
}
/**
 * Returns the tsEntry in the new state
 */
public abstract TypeStructureEntry getNewTypeStructureEntry();
/**
 * Returns the binary type in the old state
 */
public IBinaryType getOldBinaryType() {
	return getOldState().getBinaryType(getOldTypeStructureEntry());
}
/**
 * Returns the old state
 */
protected StateImpl getOldState() {
	return (StateImpl)fBuilder.getOldState();
}
/**
 * Returns the tsEntry in the old state
 */
public abstract TypeStructureEntry getOldTypeStructureEntry();
/**
 * Returns the non state-specific type handle for the superclass
 * of this type, or null if the superclass is null.
 */
public IType getSuperclass() {
	char[] newSuper = getNewBinaryType().getSuperclassName();
	return BinaryStructure.getType(getNewState(), getNewTypeStructureEntry(), newSuper);
}
/**
 * Returns true if the given type is either a direct or indirect superclass
 * of this type, otherwise returns false.  This method must never be
 * called on a type that has had a hierarchy change.
 */
public boolean hasSuperclass(IType type) {
	IType supr = getSuperclass();
	if (supr == null) {
		return false;
	}
	if (supr.equals(type)) {
		return true;
	}
	return fBuilder.getBuilderType(supr).hasSuperclass(type);
}
/**
 * Returns true if the given type is either a direct or indirect superclass
 * of this type, otherwise returns false.  This method must never be
 * called on a type that has had a hierarchy change.
 */
public boolean hasSuperInterface(IType type) {
	char[][] interfaces = getNewBinaryType().getInterfaceNames();
	if (interfaces == null) {
		return false;
	}
	for (int i = 0; i < interfaces.length; i++) {
		IType supr = BinaryStructure.getType(getNewState(), getNewTypeStructureEntry(), interfaces[i]);
		if (supr.equals(type) || fBuilder.getBuilderType(supr).hasSuperInterface(type)) {
			return true;
		}
	}
	return false;
}
/**
 * Returns true if the given type is either a direct or indirect supertype
 * of this type, otherwise returns false.  This method must never be
 * called on a type that has had a hierarchy change.
 */
public boolean hasSuperType(IType type) {
	return hasSuperclass(type) || hasSuperInterface(type);
}
/**
 * Returns true if the given type was affected by the build
 * (either added, removed, or changed)
 */
public boolean isAffected() {
	return true;
}
/**
 * Sets the tsEntry in the new state
 */
public abstract void setNewTypeStructureEntry(TypeStructureEntry newEntry);
}
