package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.Util;

import java.util.*;

class DeltaImpl implements IDelta {
	protected IDelta fParent;
	protected IDelta[] fChildren;
	
	/* Non state specific object */
	protected Object fObject;
	
	protected String fName;
	protected DeltaKey fKey;

	protected StateImpl fNewState;
	protected StateImpl fOldState;

	protected int fStatus;
	protected IImageContext fContext;

	/* names for checklist items */
	public static final String fgImage = "Image"; //$NON-NLS-1$
	
	public static final String fgPackageCategory = "Packages"; //$NON-NLS-1$
	public static final String fgTypeCategory = "Types"; //$NON-NLS-1$
	public static final String fgBinaryCategory = "Binaries"; //$NON-NLS-1$
	public static final String fgMethodCategory = "Methods"; //$NON-NLS-1$

	public static final String fgPackage = "Package"; //$NON-NLS-1$
	public static final String fgType = "Type"; //$NON-NLS-1$
	public static final String fgBinary = "Binary"; //$NON-NLS-1$
	public static final String fgMethod = "Method"; //$NON-NLS-1$

	/* convenience structure for delta calculation process */
	class DeltaInfo {
		PackageInfo[] addedPkgs;
		PackageInfo[] removedPkgs;
		PackageInfo[] changedPkgs;
	};

	class PackageInfo {
		IPackage pkg;
		IType[] addedTypes;
		IType[] removedTypes;
		IType[] changedTypes;
		IType[] changedBinaries;
	};

	/* shared empty array */
	protected static final IDelta[] fgEmptyArray = new IDelta[0];
	
	protected IncrementalImageBuilder fIncrementalBuilder;

	static final int PS_CHANGED = 1; // has the principal structure changed?
	static final int BIN_CHANGED = 2;  // has the binary changed?
	
	/**
	 * Creates a new root image delta
	 */
	DeltaImpl(IncrementalImageBuilder builder, IImageContext context) {
		this(builder.getOldState(), builder.getNewState(), context, builder);
	}
	/**
	 * Creates a new root image delta
	 */
	protected DeltaImpl(StateImpl oldState, StateImpl newState, String name,
		Object o, DeltaKey key, IDelta parent, int status) {

		fNewState = (StateImpl)newState;
		fOldState = (StateImpl)oldState;
		fName = name;
		fObject = o;
		fParent = parent;
		fKey = key;
		fStatus = status;
		fChildren = fgEmptyArray;		
		fContext = parent.getImageContext();
	}
	/**
	 * Creates a new root image delta
	 */
	DeltaImpl(IState oldState, IState newState, IImageContext context) {
		this(oldState, newState, context, null);
	}
	/**
	 * Creates a new root image delta
	 */
	DeltaImpl(IState oldState, IState newState, IImageContext context, IncrementalImageBuilder builder) {
		try {
			fNewState = (StateImpl)newState;
			fOldState = (StateImpl)oldState;
		} catch (ClassCastException cce) {
			Assert.isTrue(false, "Internal Error - Invalid states"); //$NON-NLS-1$
		}
		fStatus = CHANGED;
		fName = fgImage;
		fObject = newState.getDevelopmentContext().getImage();
		fParent = null;
		fKey = DeltaKey.getRoot();
		fContext = context;
		fChildren = fgEmptyArray;
		fIncrementalBuilder = builder;
		computeImageDelta();
	}
	/**
	 * Creates and returns a child with the given name and object
	 */
	protected DeltaImpl add(String name, Object object, int status) {
		return add(name, object, getKey().add(object), status);
	}
	/**
	 * Creates and returns a child with the given name and object
	 */
	protected DeltaImpl add(String name, Object object, DeltaKey key, int status) {
		return new DeltaImpl(
			fOldState, fNewState,	//same states as me
			name, object, key,			//given name, object and key
			this, 					//parent
			status);				//added, removed, or changed
	}
	/**
	 * Returns the child of this delta with the given object
	 */
	protected IDelta at(Object o) {
		if (fChildren != null){
			for (int i = 0; i < fChildren.length; i++) {
				if (o instanceof String) {
					if (o.equals(fChildren[i].getName())) {
						return fChildren[i];
					}
				}
				else {
					if (o.equals(fChildren[i].getObject())) {
						return fChildren[i];
					}
				}
			}
		}
		/* not found */
		return null;
	}
/**
 * Compares the type in the old and new states.
 * Returns a bit mask indicating whether the principal structure or binary have changed.
 */
protected int compareBuilderType(BuilderType type) {

	if (!type.isAffected()) {
		return 0;
	}
	
	TypeStructureEntry oldEntry = type.getOldTypeStructureEntry();
	TypeStructureEntry newEntry = type.getNewTypeStructureEntry();
	if (oldEntry == null || newEntry == null) {
		return PS_CHANGED | BIN_CHANGED;
	}
	if (oldEntry == newEntry) {
		return 0;
	}

	int result = 0;

	IBinaryType oldType = type.getOldBinaryType();
	IBinaryType newType = type.getNewBinaryType();
	
	if (!BinaryStructure.compare(oldType, newType)) {
		result |= PS_CHANGED;
	}
	/*
	 * If the source entry has changed, consider the binary to have changed
	 * even if the binary is actually the same.  Some clients, such as the
	 * target manager, need to know when the source changes. 
	 */
	if (!oldEntry.getSourceEntry().equals(newEntry.getSourceEntry())) {
		result |= BIN_CHANGED;
	} else
		if (oldEntry.getCRC32() != newEntry.getCRC32()) {
			result |= BIN_CHANGED;
		} else {
			boolean oldIsBinary = oldEntry.isBinary();
			boolean newIsBinary = newEntry.isBinary();
			if (oldIsBinary != newIsBinary) {
				result |= BIN_CHANGED;
			}
		}
	return result;
}
	/**
	 * Returns an integer indicating whether anotherDelta is less than, equal to,
	 * or greater than this delta.
	 */
	public int compareTo(IDelta anotherDelta) {
		/* is this used? */
		Assert.isTrue(false, "TBD"); //$NON-NLS-1$
		return 0;
	}
/**
 * Compares the type in the old and new states.
 * Returns a bit mask indicating whether the principal structure or binary have changed.
 */
protected int compareTypes(IType handle) {
	if (fIncrementalBuilder != null) {
		return compareBuilderType(fIncrementalBuilder.getBuilderType(handle));
	}

	TypeStructureEntry oldEntry = fOldState.getTypeStructureEntry(handle, true);
	TypeStructureEntry newEntry = fNewState.getTypeStructureEntry(handle, true);
	if (oldEntry == null || newEntry == null) {
		return PS_CHANGED | BIN_CHANGED;
	}
	if (oldEntry == newEntry) {
		return 0;
	}
	int result = 0;

	IBinaryType oldType = fOldState.getBinaryType(oldEntry);
	IBinaryType newType = fNewState.getBinaryType(newEntry);
	
	if (!BinaryStructure.compare(oldType, newType)) {
		result |= PS_CHANGED;
	}
	/*
	 * If the source entry has changed, consider the binary to have changed
	 * even if the binary is actually the same.  Some clients, such as the
	 * target manager, need to know when the source changes. 
	 */
	if (!oldEntry.getSourceEntry().equals(newEntry.getSourceEntry())) {
		result |= BIN_CHANGED;
	} else
		if (oldEntry.getCRC32() != newEntry.getCRC32()) {
			result |= BIN_CHANGED;
		} else {
			boolean oldIsBinary = oldEntry.isBinary();
			boolean newIsBinary = newEntry.isBinary();
			if (oldIsBinary != newIsBinary) {
				result |= BIN_CHANGED;
			}
		}
	return result;
}
	/**
	 * Computes the added classes for each added package.
	 * Returns a DeltaInfo object for each package describing the affected
	 * classes.
	 */
	protected PackageInfo[] computeAddedClasses(Vector pkgs) {

		PackageInfo[] infos = new PackageInfo[pkgs.size()];
		int i = 0;

		for (Enumeration pkgEnum = pkgs.elements(); pkgEnum.hasMoreElements(); i++) {
			IPackage pkg = (IPackage) pkgEnum.nextElement();
			infos[i] = new PackageInfo();
			infos[i].pkg = pkg;
			IType[] types = ((IPackage) pkg.inState(fNewState)).getAllClasses();
			for (int j = 0; j < types.length; ++j) {
				types[j] = (IType) types[j].nonStateSpecific();
			}
			infos[i].addedTypes = types;
		}//next package

		return infos;
	}//end function
	/**
	 * Computes added, removed, and changed classes for a set of packages.
	 * Stores vectors of IType handles.
	 */
	protected void computeAllClasses(IPackage pkg,
		Vector added, Vector removed, Vector changedTypes, Vector changedBinaries) {
		
		/* collect a set of all classes in old package */
		/* we have to make a copy here to avoid destroying the state */
		Hashtable oldTypeTable = new Hashtable();
		IType[] oldTypes = ((IPackage) pkg.inState(fOldState)).getAllClasses();
		for (int i = 0; i < oldTypes.length; i++) {
			IType handle = (IType)oldTypes[i].nonStateSpecific();
			oldTypeTable.put(handle, handle);
		}

		/* iterate through new classes */
		IType[] newTypes = ((IPackage)pkg.inState(fNewState)).getAllClasses();
		for (int i = 0; i < newTypes.length; i++) {
			IType handle = (IType)newTypes[i].nonStateSpecific();
			if (oldTypeTable.remove(handle) != null) {
				/* class is in both packages */
				int code = compareTypes(handle);
				if ((code & PS_CHANGED) != 0) {
					changedTypes.addElement(handle);
				}
				if ((code & BIN_CHANGED) != 0) {
					changedBinaries.addElement(handle);
				}
			} else {
				/* its only in the new package */
				added.addElement(handle);
			}
		}

		/* remaining classes are removed */
		for (Enumeration e = oldTypeTable.keys(); e.hasMoreElements();) {
			removed.addElement(e.nextElement());
		}
	}
	/**
	 * Computes added, removed, and changed packages.  At this
	 * stage, if a package hasn't been added or removed, it is
	 * considered to be changed.  Later, when the classes are
	 * calculated, the packages that don't have changed classes
	 * will be removed from the list.  Stores vectors of non-state
	 * specific package handles.
	 */
	protected void computeAllPackages(
		Vector added, Vector removed, Vector changed) {

		/* do for each affected package */
		for (Enumeration e = getAffectedPackages();e.hasMoreElements();) {
			IPackage pkg = (IPackage)e.nextElement();
			boolean inOld = pkg.inState(fOldState).isPresent();
			boolean inNew = pkg.inState(fNewState).isPresent();
			if (inOld) {
				if (inNew) {
					changed.addElement(pkg);
				}
				else {
					// Package was removed.
					removed.addElement(pkg);
				}
			}
			else {
				if (inNew) {
					// Package was added.
					added.addElement(pkg);
				}
				else {
					// Package was in image context but was not in either state.
					// Ignore it.
				}
			}
		}
	}
	/**
	 * Computes added, removed, and changed classes for a set of packages.
	 * Returns a DeltaInfo object for each package describing the affected
	 * classes.
	 */
	protected PackageInfo[] computeChangedClasses(Vector pkgs) {

		Vector vInfo = new Vector();
		Vector toRemove = new Vector();

		for (Enumeration pkgEnum = pkgs.elements(); pkgEnum.hasMoreElements();) {
			IPackage pkg = (IPackage)pkgEnum.nextElement();

			Vector vAdded = new Vector();
			Vector vRemoved = new Vector();
			Vector vChanged = new Vector();
			Vector vBinaries = new Vector();
			computeAllClasses(pkg, vAdded, vRemoved, vChanged, vBinaries);

			/* if there are any affected classes */
			if (vAdded.size() + vRemoved.size() + vChanged.size() + vBinaries.size() > 0) {

				/* fill out package information */
				PackageInfo pkgInfo = new PackageInfo();
				pkgInfo.pkg = pkg;

				pkgInfo.addedTypes = new IType[vAdded.size()];
				vAdded.copyInto(pkgInfo.addedTypes);

				pkgInfo.removedTypes = new IType[vRemoved.size()];
				vRemoved.copyInto(pkgInfo.removedTypes);
				
				pkgInfo.changedTypes = new IType[vChanged.size()];
				vChanged.copyInto(pkgInfo.changedTypes);
				
				pkgInfo.changedBinaries = new IType[vBinaries.size()];
				vBinaries.copyInto(pkgInfo.changedBinaries);

				vInfo.addElement(pkgInfo);
			} else {
				/* remove the element -- can't delete while enumerating */
				toRemove.addElement(pkg);
			}
		}//next package

		/* remove packages with no changes */
		for (Enumeration e = toRemove.elements(); e.hasMoreElements();) {
			pkgs.removeElement(e.nextElement());
		}
		
		/* convert info vector to array */
		PackageInfo[] infos = new PackageInfo[vInfo.size()];
		vInfo.copyInto(infos);
		return infos;
	}//end function
	/**
	 * Calculates the added, removed, and changed packages, types and methods
	 * that consitute an image delta.  The results are stored in the supplied
	 * DeltaInfo object
	 */
	protected DeltaInfo computeDeltaInfo() {
		DeltaInfo info = new DeltaInfo();

		/* compute packages */
		Vector vAddedPkgs = new Vector();
		Vector vRemovedPkgs = new Vector();
		Vector vChangedPkgs = new Vector();
		computeAllPackages(vAddedPkgs, vRemovedPkgs, vChangedPkgs);

		/* compute classes */
		info.addedPkgs = computeAddedClasses(vAddedPkgs);
		info.removedPkgs = computeRemovedClasses(vRemovedPkgs);
		info.changedPkgs = computeChangedClasses(vChangedPkgs);

		return info;
	}
	/**
	 * Computes the image delta.
	 */
	protected void computeImageDelta() {

		/* compute the delta info */
		DeltaInfo info = computeDeltaInfo();

		/* create categories, any or all of them may be null */
		IDelta pkgCategory = createPackageCategory(this, info); 
		IDelta typeCategory = createCategory(this, info, fgTypeCategory);
		IDelta binaryCategory = createCategory(this, info, fgBinaryCategory);

		/* Create top level children */
		int childCount = 0;
		if (pkgCategory != null) childCount++;
		if (typeCategory != null) childCount++;
		if (binaryCategory != null) childCount++;

		fChildren = new IDelta[childCount];
		childCount = 0;
		if (pkgCategory != null) 
			fChildren[childCount++] = pkgCategory;
		if (typeCategory != null) 
			fChildren[childCount++] = typeCategory;
		if (binaryCategory != null) 
			fChildren[childCount++] = binaryCategory;
	}
	/**
	 * Computes the removed classes for each removed package.
	 * Returns a DeltaInfo object for each package describing the affected
	 * classes.
	 */
	protected PackageInfo[] computeRemovedClasses(Vector pkgs) {

		PackageInfo[] infos = new PackageInfo[pkgs.size()];
		int i = 0;

		for (Enumeration pkgEnum = pkgs.elements(); pkgEnum.hasMoreElements(); i++) {
			IPackage pkg = (IPackage) pkgEnum.nextElement();
			infos[i] = new PackageInfo();
			infos[i].pkg = pkg;
			IType[] types = ((IPackage) pkg.inState(fOldState)).getAllClasses();
			for (int j = 0; j < types.length; ++j) {
				types[j] = (IType) types[j].nonStateSpecific();
			}
			infos[i].removedTypes = types;
		}//next package

		return infos;
	}//end function
	/**
	 * Converts a vector of handles to an array of delta info objects of the given
	 * type.
	 */
	protected IDelta[] convertHandlesToDeltaArray(Vector vHandles, String type, int status) {

		int i = 0;
		IDelta[] results = new IDelta[vHandles.size()];
		for (Enumeration e = vHandles.elements(); e.hasMoreElements(); i++) {
			results[i] = add(type, e.nextElement(), status);
		}
		return results;
	}
	/**
	 * Copies information from a PackageInfo to the children of a DeltaImpl.
	 * If types is true, copy the type information, otherwise copy the binary information
	 */
	protected void copyInfoToChildren(DeltaImpl delta, PackageInfo info, boolean types) {

		/* are we looking at binaries or types ? */
		IType[] changed = types ? info.changedTypes : info.changedBinaries;
		String name = types ? fgType : fgBinary;
			
		int count = info.addedTypes.length + info.removedTypes.length + changed.length;
		if (count == 0) {
			delta.fStatus = SAME;
			delta.setChildren(new IDelta[0]);
			return;
		}
		IDelta[] children = new IDelta[count];
		int i = 0;
		for (; i < info.addedTypes.length; i++) {
			children[i] = delta.add(name, info.addedTypes[i], ADDED);
		}
		for (int j = 0; j < info.removedTypes.length; j++, i++) {
			children[i] = delta.add(name, info.removedTypes[j], REMOVED);
		}
		for (int j = 0; j < changed.length; j++, i++) {
			children[i] = delta.add(name, changed[j], CHANGED);
		}
		delta.setChildren(children);
	}
	/**
	 * Creates the types category of the image delta.  Returns
	 * the Delta object for the category.  If there are no added, 
	 * removed, or changed types, returns null.
	 */
	protected IDelta createCategory(DeltaImpl parent, DeltaInfo info, String categoryName) {

		int size = info.changedPkgs.length + info.addedPkgs.length + info.removedPkgs.length;
		if (size == 0) return null;
		
		/* collection of packages that are children of the type category */
		IDelta[] children = new IDelta[size];

		/* create Type category node */
		DeltaImpl category = parent.add(categoryName, parent.getObject(), parent.getKey().add(categoryName), CHANGED);
		
		/* create new deltas for changed packages */
		int count = 0;
		for (int i = 0; i < info.changedPkgs.length; i++) {
			PackageInfo pkgInfo = info.changedPkgs[i];
			DeltaImpl child = (DeltaImpl)category.add(fgPackage, pkgInfo.pkg, CHANGED);
			copyInfoToChildren(child, pkgInfo, categoryName == fgTypeCategory);
			children[count++] = child;
		}

		/* create deltas for added and removed packages */
		for (int i = 0; i < info.addedPkgs.length; i++) {
			children[count++] = category.packageForTypeCategory(ADDED, info.addedPkgs[i]);
		}
		for (int i = 0; i < info.removedPkgs.length; i++) {
			children[count++] = category.packageForTypeCategory(REMOVED, info.removedPkgs[i]);
		}

		category.setChildren(children);
		return category;
	}
	/**
	 * Creates the package category of the image delta.  Returns
	 * the Delta object for the category.  If there are no added,
	 * removed, or changed packages, returns null.  A changed package
	 * is a package that has had a package fragment added or removed.
	 */
	protected IDelta createPackageCategory(DeltaImpl parent, DeltaInfo info) {

		/* number of children of this category */
		int size = info.addedPkgs.length + info.changedPkgs.length + info.removedPkgs.length;
		if (size == 0) return null;

		/* create the category */
		DeltaImpl category = 
			parent.add(
				fgPackageCategory, 
				parent.getObject(), 
				parent.getKey().add(fgPackageCategory), 
				CHANGED);

		IDelta[] children = new IDelta[size];
		int count = 0;
		for (int i = 0; i < info.addedPkgs.length; i++) {
			children[count++] = category.add(fgPackage, info.addedPkgs[i].pkg, ADDED);
		}
		for (int i = 0; i < info.removedPkgs.length; i++) {
			children[count++] = category.add(fgPackage, info.removedPkgs[i].pkg, REMOVED);
		}
		for (int i = 0; i < info.changedPkgs.length; i++) {
			IPackage pkg = info.changedPkgs[i].pkg;
			if (!Util.equalArraysOrNull(
				fOldState.getPackageMap().getFragments(pkg),
				fNewState.getPackageMap().getFragments(pkg))) {
				children[count++] = category.add(fgPackage, pkg, CHANGED);
			}
		}

		/* there could be no changed packages */
		if (count == 0) {
			return null;
		}
		
		/* compact if some changed packages did not actually change */
		if (count < children.length) {
			System.arraycopy(children, 0, children = new IDelta[count], 0, count);
		}
		category.setChildren(children);
		return category;
	}
	public void dump() {
		dump(this, 0);
	}
	protected static void dump(IDelta delta, int depth) {
		for (int i = 0; i < depth; ++i)
			System.out.print("  "); //$NON-NLS-1$
		System.out.println(delta);
		IDelta[] children = delta.getAffectedSubdeltas();
		for (int i = 0; i < children.length; ++i) {
			dump(children[i], depth+1);
		}
	}
	/**
	 * Returns the delta reached by navigating the given 
	 * relative path from this object.
	 * It is an error if the delta could never have such a descendent.
	 * <pre>
	 *     - navigation off a checklist with the wrong child name 
	 *       is always bad
	 *     - navigation off a batch delta is common when you don't 
	 *       yet know whether the object is present; this is allowed
	 *     - navigation off a leaf delta is always bad
	 * </pre>
	 *
	 * @param path the path to follow
	 * @exception InvalidKeyException if an invalid key is given.
	 */
	public IDelta follow(IDeltaKey path) throws InvalidKeyException {
		IDelta delta = this;
		for (int i = 0, size = path.size(); i < size; ++i) {
			delta = ((DeltaImpl) delta).at(path.at(i));
			if (delta == null) {
				throw new InvalidKeyException();
			}
		}
		return delta;
	}
	/**
	 * Returns the immediate subdeltas of this delta that are additions
	 * (i.e. their status is Added).
	 */
	public IDelta[] getAddedSubdeltas() {
		if (fChildren == null) return fgEmptyArray;
		Vector vAdded = new Vector();
		for (int i = 0; i < fChildren.length; i++) {
			if (fChildren[i].getStatus() == IDelta.ADDED) {
				vAdded.addElement(fChildren[i]);
			}
		}
		IDelta[] added = new IDelta[vAdded.size()];
		vAdded.copyInto(added);
		return added;
	}
/**
 * Returns an enumeration of packages which are possibly affected.
 */
public Enumeration getAffectedPackages() {
	/* Non-naive case - the image builder knows. */
	if (fIncrementalBuilder != null) {
		return fIncrementalBuilder.getAffectedPackages();
	}

	/* Naive case - take union of packages in old and new state. */
	Hashtable affected = new Hashtable(21);
	for (Enumeration e = fOldState.getPackageMap().getAllPackages(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		affected.put(pkg, pkg);
	}
	for (Enumeration e = fNewState.getPackageMap().getAllPackages(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		affected.put(pkg, pkg);
	}
	return affected.keys();
}
	/**
	 * Returns the immediate subdeltas of this delta that are
	 * not the same (i.e. their status is either Added, Removed, or Changed).
	 */
	public IDelta[] getAffectedSubdeltas() {
		if (fChildren == null) return fgEmptyArray;
		Vector vAffected = new Vector();
		for (int i = 0; i < fChildren.length; i++) {
			if (fChildren[i].getStatus() != IDelta.SAME) {
				vAffected.addElement(fChildren[i]);
			}
		}
		IDelta[] affected = new IDelta[vAffected.size()];
		vAffected.copyInto(affected);
		return affected;
	}
	/**
	 * Returns the immediate subdeltas of this delta that are true
	 * changes, not additions or removals (i.e. their status is Changed).
	 */
	public IDelta[] getChangedSubdeltas() {
		if (fChildren == null) return fgEmptyArray;
		Vector vChanged = new Vector();
		for (int i = 0; i < fChildren.length; i++) {
			if (fChildren[i].getStatus() == IDelta.CHANGED) {
				vChanged.addElement(fChildren[i]);
			}
		}
		IDelta[] changed = new IDelta[vChanged.size()];
		vChanged.copyInto(changed);
		return changed;
	}
	/**
	 * Returns the ImageContext that the delta is restricted to.
	 *
	 * @see IImageBuilder#getImageDelta
	 */
	public IImageContext getImageContext() {
		return fContext;
	}
	/**
	 * Returns the delta key for this delta.
	 * Delta keys often contain non-state-specific handles, but
	 * never state-specific ones.
	 *
	 * @see DeltaKey
	 */
	public IDeltaKey getKey() {
		return fKey;
	}
	/**
	 * Returns the name of the aspect being compared in this delta.
	 */
	public String getName() {
		return fName;
	}
	/**
	 * Returns the object in the new state that is the focus of this delta.
	 * It only make sense to talk about the 'new object' if the object
	 * that is the focus of this delta is a Handle.  If it is not, this
	 * returns null.
	 *
	 * @return the state-specific handle of the object in the new state.
	 */
	public IHandle getNewObject() {
		IHandle handle;
		try {
			handle = (IHandle)fObject;
		} catch (ClassCastException cce) {
			//not a handle
			return null;
		}
		return handle.inState(fNewState);
	}
	/**
	 * Returns the new state to which this delta pertains.
	 */
	public IState getNewState() {
		return fNewState;
	}
	/**
	 * Returns the object that is the focus of this delta.
	 * The result is often a Handle, but in
	 * some cases it is another type of object.
	 * When it is a Handle, it is non-state-specific.
	 */
	public Object getObject() {
		return fObject;
	}
	/**
	 * Returns the object in the old state that is the focus of this delta.
	 * It only make sense to talk about the 'old object' if the object
	 * that is the focus of this delta is a Handle.  If it is not, this
	 * returns null.
	 *
	 * @return the state-specific handle of the object in the old state.
	 */
	public IHandle getOldObject() {
		IHandle handle;
		try {
			handle = (IHandle)fObject;
		} catch (ClassCastException cce) {
			//not a handle
			return null;
		}
		return handle.inState(fOldState);
	}
	/**
	 * Returns the old state to which this delta pertains.
	 */
	public org.eclipse.jdt.internal.core.builder.IState getOldState() {
		return fOldState;
	}
	/**
	 * Returns the parent delta of this delta, or null if it has no parent.
	 */
	public IDelta getParent() {
		return fParent;
	}
	/**
	 * Returns the immediate subdeltas of this delta that are removals
	 * (i.e. their status is Removed).
	 */
	public IDelta[] getRemovedSubdeltas() {
		if (fChildren == null) return fgEmptyArray;
		Vector vRemoved = new Vector();
		for (int i = 0; i < fChildren.length; i++) {
			if (fChildren[i].getStatus() == IDelta.REMOVED) {
				vRemoved.addElement(fChildren[i]);
			}
		}
		IDelta[] removed = new IDelta[vRemoved.size()];
		vRemoved.copyInto(removed);
		return removed;
	}
	/**
	 * Returns the root delta of the tree containing this delta.
	 */
	public IDelta getRoot() {
		/* don't bother caching the root at each branch for now */
		if (fKey == DeltaKey.getRoot()) {
			return this;
		} else {
			return getParent().getRoot();
		}
	}
	/**
	 * Returns the status of this delta.  If this delta
	 * is not applicable, it always returns SAME.
	 * If the status is not currently known, it is computed
	 * (UNKNOWN is never returned).
	 */
	public int getStatus() {
		return fStatus;
	}
	/**
	 * Returns the status of this delta if it is known.
	 * Returns UNKNOWN if it is not known whether the object has changed.
	 *
	 */
	public int getStatusIfKnown() {
		return fStatus;
	}
	/**
	 * Returns an array of Delta objects that are children of this delta.
	 * Returns an array of length 0 if this delta has no children,
	 * or if it is not composite.
	 */
	public IDelta[] getSubdeltas() {
		return fChildren;
	}
	/**
	 * Returns whether this delta is a composite delta that is further 
	 * broken down into subdeltas. 
	 */
	public boolean hasSubdeltas() {
		return fChildren != null && fChildren.length > 0;
	}
	/**
	 * Creates a package delta for the types or binaries category.
	 * The package delta can be for either an added or removed package,
	 * according to the constant given.  The package delta will have
	 * children for each type in the package.
	 */
	protected IDelta packageForTypeCategory(int status, PackageInfo info) {

		/* create the package delta */
		DeltaImpl pkgDelta = (DeltaImpl)this.add(fgPackage, info.pkg, status);
		
		IType[] types = (status == ADDED ? info.addedTypes : info.removedTypes);
		IDelta[] children = new IDelta[types.length];
		for (int i = 0; i < types.length; i++) {
			children[i] = pkgDelta.add(fgType, types[i], status);
		}
		pkgDelta.setChildren(children);
		return pkgDelta;
	}
	/**
	 * Sets the children of this delta
	 */
	protected void setChildren(IDelta[] children) {
		fChildren = children;
	}
	/**
	 * Return a string of the form:
	 * 		status key 
	 * 
	 * status will be one of the following:
	 *		+ if status is ADDED
	 *		- if status is REMOVED
	 *		* if status is CHANGED
	 * 		= if status is SAME
	 *		? if status is UNKNOWN
	 * The string returned is only for debugging purposes,
	 * and the contents of the string may change in the future.
	 * @return java.lang.String
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("DeltaImpl("); //$NON-NLS-1$
		switch (fStatus) {
			case ADDED: sb.append("+"); //$NON-NLS-1$
			break;
			case REMOVED: sb.append("-"); //$NON-NLS-1$
			break;
			case CHANGED: sb.append("*"); //$NON-NLS-1$
			break;
			case SAME: sb.append("="); //$NON-NLS-1$
			break;
			case UNKNOWN: sb.append("?"); //$NON-NLS-1$
			break;
			default: sb.append("(ERROR)"); //$NON-NLS-1$
		}
		if (fKey.isRoot()) {
			sb.append(fOldState);
			sb.append("-->"); //$NON-NLS-1$
			sb.append(fNewState);
		}
		else {
			for (int i = 0; i < fKey.size(); ++i) {
				if (i != 0) {
					sb.append('/');
				}
				sb.append(fKey.at(i));
			}
		}
		sb.append(')');
		return sb.toString();
	}
}
