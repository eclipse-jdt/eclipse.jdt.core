package org.eclipse.jdt.internal.core.hierarchy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.*;

import java.util.*;
import java.util.zip.ZipFile;

/**
 * @see ITypeHierarchy
 */
public class TypeHierarchy implements ITypeHierarchy, IElementChangedListener {
	/**
	 * The type the hierarchy was specifically computed for,
	 * possibly null.
	 */
	protected IType fType;

	protected Hashtable fClassToSuperclass;
	protected Hashtable fTypeToSuperInterfaces;
	protected Hashtable fTypeToSubtypes;
	protected TypeVector fRootClasses = new TypeVector();
	protected Vector fInterfaces = new Vector(10);

	protected static final IType[] fgEmpty = new IType[0];

	/**
	 * The progress monitor to report work completed too.
	 */
	protected IProgressMonitor fProgressMonitor = null;

	/**
	 * Change listeners - null if no one is listening.
	 */
	protected Vector fChangeListeners = null;

	/**
	 * A set of the compilation units and class
	 * files that are considered in this hierarchy. Null if
	 * not activated.
	 */
	protected Hashtable files = null;

	/**
	 * A region describing the packages considered by this
	 * hierarchy. Null if not activated.
	 */
	protected Region fPackageRegion = null;

	/**
	 * A region describing the package fragment roots considered by this
	 * hierarchy. Null if not activated.
	 */
	protected Region fRootRegion = null;

	/**
	 * A region describing the projects considered by this
	 * hierarchy. Null if not activated.
	 */
	protected Region fProjectRegion = null;

	/**
	 * A boolean indicating if this hierarchy is actively tracking changes
	 * in the Java Model.
	 */
	protected boolean fIsActivated = false;

	/**
	 * A boolean indicating if the hierarchy exists
	 *
	 * fix for 1FW67PA
	 */
	protected boolean fExists = true;

	/**
	 * Whether this hierarchy should contains subtypes.
	 */
	protected boolean computeSubtypes;

	/**
	 * The scope this hierarchy should restrain itsef in.
	 */
	IJavaSearchScope scope;

	/**
	 * Creates a TypeHierarchy on the given type.
	 */
	public TypeHierarchy(
		IType type,
		IJavaSearchScope scope,
		boolean computeSubtypes)
		throws JavaModelException {
		fType = type;
		this.computeSubtypes = computeSubtypes;
		this.scope = scope;
	}

	/**
	 * Activates this hierarchy for change listeners
	 */
	protected void activate() {

		// determine my file, package, root, & project regions.
		this.files = new Hashtable(5);
		fProjectRegion = new Region();
		fPackageRegion = new Region();
		fRootRegion = new Region();
		IType[] types = getAllTypes();
		for (int i = 0; i < types.length; i++) {
			IType type = types[i];
			Openable o = (Openable) ((JavaElement) type).getOpenableParent();
			if (o != null) {
				this.files.put(o, o);
			}
			IPackageFragment pkg = type.getPackageFragment();
			fPackageRegion.add(pkg);
			fRootRegion.add(pkg.getParent());
			IJavaProject project = type.getJavaProject();
			if (project != null) {
				fProjectRegion.add(project);
			}
			checkCanceled();
		}
		JavaModelManager.getJavaModelManager().addElementChangedListener(this);
		fIsActivated = true;
	}

	/**
	 * Adds all of the elements in the collection to the vector if the
	 * element is not already in the vector.
	 */
	private void addAllCheckingDuplicates(Vector vector, IType[] collection) {
		for (int i = 0; i < collection.length; i++) {
			IType element = collection[i];
			if (!vector.contains(element)) {
				vector.addElement(element);
			}
		}
	}

	/**
	 * Adds the type to the collection of interfaces.
	 */
	protected void addInterface(IType type) {
		fInterfaces.addElement(type);
	}

	/**
	 * Adds the type to the collection of root classes
	 * if the classes is not already present in the collection.
	 */
	protected void addRootClass(IType type) {
		if (fRootClasses.contains(type))
			return;
		fRootClasses.add(type);
	}

	/**
	 * Adds the given subtype to the type.
	 */
	protected void addSubtype(IType type, IType subtype) {
		TypeVector subtypes = (TypeVector) fTypeToSubtypes.get(type);
		if (subtypes == null) {
			subtypes = new TypeVector();
			fTypeToSubtypes.put(type, subtypes);
		}
		if (!subtypes.contains(subtype)) {
			subtypes.add(subtype);
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public void addTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener) {
		if (fChangeListeners == null) {
			fChangeListeners = new Vector();
			// fix for 1FW67PA
			if (fExists) {
				activate();
			}
		}
		// add listener only if it is not already present
		if (fChangeListeners.indexOf(listener) == -1) {
			fChangeListeners.addElement(listener);
		}
	}

	/**
	 * Caches the handle of the superclass for the specified type.
	 * As a side effect cache this type as a subtype of the superclass.
	 */
	protected void cacheSuperclass(IType type, IType superclass) {
		if (superclass != null) {
			fClassToSuperclass.put(type, superclass);
			addSubtype(superclass, type);
		}
	}

	/**
	 * Caches all of the superinterfaces that are specified for the
	 * type.
	 */
	protected void cacheSuperInterfaces(IType type, IType[] superinterfaces) {
		fTypeToSuperInterfaces.put(type, superinterfaces);
		for (int i = 0; i < superinterfaces.length; i++) {
			IType superinterface = superinterfaces[i];
			if (superinterface != null) {
				addSubtype(superinterface, type);
			}
		}
	}

	/**
	 * Checks with the progress monitor to see whether the creation of the type hierarchy
	 * should be canceled. Should be regularly called
	 * so that the user can cancel.
	 *
	 * @exception OperationCanceledException if cancelling the operation has been requested
	 * @see IProgressMonitor#isCanceled
	 */
	protected void checkCanceled() {
		if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Compute this type hierarchy.
	 */
	protected void compute() throws JavaModelException, CoreException {
		if (JavaModelManager.ENABLE_INDEXING && fType != null) {
			HierarchyBuilder builder = new IndexBasedHierarchyBuilder(this, this.scope);
			builder.build(this.computeSubtypes);
		} // else a RegionBasedTypeHierarchy should be used
	}

	/**
	 * @see ITypeHierarchy
	 */
	public boolean contains(IType type) {
		// classes
		if (fClassToSuperclass.get(type) != null) {
			return true;
		}

		// root classes
		if (fRootClasses.contains(type))
			return true;

		// interfaces
		for (Enumeration enum = fInterfaces.elements(); enum.hasMoreElements();) {
			if (enum.nextElement().equals(type)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Deactivates this hierarchy for change listeners
	 */
	protected void deactivate() {
		JavaModelManager.getJavaModelManager().removeElementChangedListener(this);
		this.files = null;
		fPackageRegion = null;
		fRootRegion = null;
		fProjectRegion = null;
		fChangeListeners = null;
		fIsActivated = false;
	}

	/**
	 * Empties this hierarchy.
	 *
	 * fix for 1FW67PA
	 */
	protected void destroy() {
		fExists = false;
		fClassToSuperclass = new Hashtable(1);
		this.files = new Hashtable(5);
		fInterfaces = new Vector(0);
		fPackageRegion = new Region();
		fProjectRegion = new Region();
		fRootClasses = new TypeVector();
		fRootRegion = new Region();
		fTypeToSubtypes = new Hashtable(1);
		fTypeToSuperInterfaces = new Hashtable(1);
		JavaModelManager.getJavaModelManager().removeElementChangedListener(this);
	}

	/**
	 * Determines if the change effects this hierarchy, and fires
	 * change notification if required.
	 */
	public void elementChanged(ElementChangedEvent event) {
		// fix for 1FW67PA
		if (fExists) {
			if (exists()) {
				if (isAffected(event.getDelta())) {
					fireChange();
				}
			} else {
				destroy();
				fireChange();
			}
		}

	}

	/**
	 * @see ITypeHierarchy
	 *
	 * fix for 1FW67PA
	 */
	public boolean exists() {
		if (fExists) {
			fExists =
				(fType == null || (fType != null && fType.exists()))
					&& this.javaProject().exists();
			if (!fExists) {
				destroy();
			}
		}
		return fExists;
	}

	/**
	 * Notifies listeners that this hierarchy has changed and needs
	 * refreshing. Note that listeners can be removed as we iterate
	 * through the list.
	 */
	protected void fireChange() {
		if (fChangeListeners == null) {
			return;
		}
		Vector listeners = (Vector) fChangeListeners.clone();
		for (int i = 0; i < listeners.size(); i++) {
			ITypeHierarchyChangedListener listener =
				(ITypeHierarchyChangedListener) listeners.elementAt(i);
			// ensure the listener is still a listener
			if (fChangeListeners != null && fChangeListeners.indexOf(listener) >= 0) {
				listener.typeHierarchyChanged(this);
			}
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllClasses() {
		Enumeration keys = fClassToSuperclass.keys();
		TypeVector classes = fRootClasses.copy();
		while (keys.hasMoreElements()) {
			classes.add((IType) keys.nextElement());
		}
		return classes.elements();
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllInterfaces() {
		IType[] collection = new IType[fInterfaces.size()];
		fInterfaces.copyInto(collection);
		return collection;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllSubtypes(IType type) {
		return getAllSubtypesForType(type);
	}

	/**
	 * @see getAllSubtypes(IType)
	 */
	private IType[] getAllSubtypesForType(IType type) {
		Vector subTypes = new Vector();
		getAllSubtypesForType0(type, subTypes);
		IType[] subClasses = new IType[subTypes.size()];
		subTypes.copyInto(subClasses);
		return subClasses;
	}

	/**
	 */
	private void getAllSubtypesForType0(IType type, Vector subs) {
		IType[] subTypes = getSubtypesForType(type);
		if (subTypes.length != 0) {
			for (int i = 0; i < subTypes.length; i++) {
				IType subType = subTypes[i];
				subs.addElement(subType);
				getAllSubtypesForType0(subType, subs);
			}
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllSuperclasses(IType type) {
		IType superclass = getSuperclass(type);
		TypeVector supers = new TypeVector();
		while (superclass != null) {
			supers.add(superclass);
			superclass = getSuperclass(superclass);
		}
		return supers.elements();
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllSuperInterfaces(IType type) {
		Vector supers = new Vector();
		if (fTypeToSuperInterfaces.get(type) == null) {
			return fgEmpty;
		}
		getAllSuperInterfaces0(type, supers);
		IType[] superinterfaces = new IType[supers.size()];
		supers.copyInto(superinterfaces);
		return superinterfaces;
	}

	private void getAllSuperInterfaces0(IType type, Vector supers) {
		IType[] superinterfaces = (IType[]) fTypeToSuperInterfaces.get(type);
		if (superinterfaces != null && superinterfaces.length != 0) {
			addAllCheckingDuplicates(supers, superinterfaces);
			for (int i = 0; i < superinterfaces.length; i++) {
				getAllSuperInterfaces0(superinterfaces[i], supers);
			}
		}
		IType superclass = (IType) fClassToSuperclass.get(type);
		if (superclass != null) {
			getAllSuperInterfaces0(superclass, supers);
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllSupertypes(IType type) {
		Vector supers = new Vector();
		if (fTypeToSuperInterfaces.get(type) == null) {
			return fgEmpty;
		}
		getAllSupertypes0(type, supers);
		IType[] supertypes = new IType[supers.size()];
		supers.copyInto(supertypes);
		return supertypes;
	}

	private void getAllSupertypes0(IType type, Vector supers) {
		IType[] superinterfaces = (IType[]) fTypeToSuperInterfaces.get(type);
		if (superinterfaces != null && superinterfaces.length != 0) {
			addAllCheckingDuplicates(supers, superinterfaces);
			for (int i = 0; i < superinterfaces.length; i++) {
				getAllSuperInterfaces0(superinterfaces[i], supers);
			}
		}
		IType superclass = (IType) fClassToSuperclass.get(type);
		if (superclass != null) {
			supers.addElement(superclass);
			getAllSupertypes0(superclass, supers);
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getAllTypes() {
		IType[] classes = getAllClasses();
		int classesLength = classes.length;
		IType[] interfaces = getAllInterfaces();
		int interfacesLength = interfaces.length;
		IType[] all = new IType[classesLength + interfacesLength];
		System.arraycopy(classes, 0, all, 0, classesLength);
		System.arraycopy(interfaces, 0, all, classesLength, interfacesLength);
		return all;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getExtendingInterfaces(IType type) {
		try {
			if (type.isClass()) {
				return new IType[] {
				};
			}
		} catch (JavaModelException npe) {
			return new IType[] {
			};
		}
		return getExtendingInterfaces0(type);
	}

	/**
	 * Assumes that the type is an interface
	 * @see getExtendingInterfaces
	 */
	private IType[] getExtendingInterfaces0(IType interfce) {
		Enumeration keys = fTypeToSuperInterfaces.keys();
		Vector xers = new Vector();
		while (keys.hasMoreElements()) {
			IType type = (IType) keys.nextElement();
			try {
				if (type.isClass()) {
					continue;
				}
			} catch (JavaModelException npe) {
				continue;
			}
			IType[] interfaces = (IType[]) fTypeToSuperInterfaces.get(type);
			if (interfaces != null) {
				for (int i = 0; i < interfaces.length; i++) {
					IType iFace = interfaces[i];
					if (iFace.equals(interfce)) {
						xers.addElement(type);
					}
				}
			}
		}
		IType[] extenders = new IType[xers.size()];
		xers.copyInto(extenders);
		return extenders;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getImplementingClasses(IType type) {
		try {
			if (type.isClass()) {
				return fgEmpty;
			}
		} catch (JavaModelException npe) {
			return fgEmpty;
		}
		return getImplementingClasses0(type);
	}

	/**
	 * Assumes that the type is an interface
	 * @see getImplementingClasses
	 */
	private IType[] getImplementingClasses0(IType interfce) {
		Enumeration keys = fTypeToSuperInterfaces.keys();
		Vector iMenters = new Vector();
		while (keys.hasMoreElements()) {
			IType type = (IType) keys.nextElement();
			try {
				if (type.isInterface()) {
					continue;
				}
			} catch (JavaModelException npe) {
				continue;
			}
			IType[] interfaces = (IType[]) fTypeToSuperInterfaces.get(type);
			for (int i = 0; i < interfaces.length; i++) {
				IType iFace = interfaces[i];
				if (iFace.equals(interfce)) {
					iMenters.addElement(type);
				}
			}
		}
		IType[] implementers = new IType[iMenters.size()];
		iMenters.copyInto(implementers);
		return implementers;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getRootClasses() {
		return fRootClasses.elements();
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getRootInterfaces() {
		IType[] allInterfaces = getAllInterfaces();
		IType[] roots = new IType[allInterfaces.length];
		int rootNumber = 0;
		for (int i = 0; i < allInterfaces.length; i++) {
			IType[] superInterfaces = getSuperInterfaces(allInterfaces[i]);
			if (superInterfaces == null || superInterfaces.length == 0) {
				roots[rootNumber++] = allInterfaces[i];
			}
		}
		IType[] result = new IType[rootNumber];
		if (result.length > 0) {
			System.arraycopy(roots, 0, result, 0, rootNumber);
		}
		return result;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getSubclasses(IType type) {
		try {
			if (type.isInterface()) {
				return fgEmpty;
			}
		} catch (JavaModelException npe) {
			return new IType[] {
			};
		}
		TypeVector vector = (TypeVector) fTypeToSubtypes.get(type);
		if (vector == null)
			return fgEmpty;
		else
			return vector.elements();
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getSubtypes(IType type) {
		return getSubtypesForType(type);
	}

	/**
	 * Returns an array of subtypes for the given type - will never return null.
	 */
	private IType[] getSubtypesForType(IType type) {
		TypeVector vector = (TypeVector) fTypeToSubtypes.get(type);
		if (vector == null)
			return fgEmpty;
		else
			return vector.elements();
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType getSuperclass(IType type) {
		try {
			if (type.isInterface()) {
				return null;
			}
			return (IType) fClassToSuperclass.get(type);

		} catch (JavaModelException npe) {
			return null;
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getSuperInterfaces(IType type) {
		IType[] interfaces = (IType[]) fTypeToSuperInterfaces.get(type);
		if (interfaces == null) {
			return fgEmpty;
		}
		return interfaces;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType[] getSupertypes(IType type) {
		IType superclass = getSuperclass(type);
		if (superclass == null) {
			return getSuperInterfaces(type);
		} else {
			TypeVector superTypes = new TypeVector(getSuperInterfaces(type));
			superTypes.add(superclass);
			return superTypes.elements();
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public IType getType() {
		return fType;
	}

	/**
	 * Adds the new elements to a new array that contains all of the elements of the old array.
	 * Returns the new array.
	 */
	protected IType[] growAndAddToArray(IType[] array, IType[] additions) {
		if (array == null || array.length == 0) {
			return additions;
		}
		IType[] old = array;
		array = new IType[old.length + additions.length];
		System.arraycopy(old, 0, array, 0, old.length);
		System.arraycopy(additions, 0, array, old.length, additions.length);
		return array;
	}

	/**
	 * Adds the new element to a new array that contains all of the elements of the old array.
	 * Returns the new array.
	 */
	protected IType[] growAndAddToArray(IType[] array, IType addition) {
		if (array == null || array.length == 0) {
			return new IType[] { addition };
		}
		IType[] old = array;
		array = new IType[old.length + 1];
		System.arraycopy(old, 0, array, 0, old.length);
		array[old.length] = addition;
		return array;
	}

	/**
	 * Returns whether one of the subtypes in this hierarchy has the given simple name
	 * or this type has the given simple name.
	 */
	private boolean hasSubtypeNamed(String simpleName) {
		if (fType.getElementName().equals(simpleName)) {
			return true;
		}
		IType[] types = this.getAllSubtypes(fType);
		for (int i = 0, length = types.length; i < length; i++) {
			if (types[i].getElementName().equals(simpleName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the given delta (a compilation unit delta or a class file delta)
	 * indicates that one of the supertypes has changed or one of the imports has changed.
	 */
	private boolean hasSuperTypeOrImportChange(IJavaElementDelta delta) {
		IJavaElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0, length = children.length; i < length; i++) {
			IJavaElementDelta child = children[i];
			if ((child.getFlags() & IJavaElementDelta.F_SUPER_TYPES) > 0) {
				return true;
			}
			if (child.getElement() instanceof ImportContainer) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether one of the types in this hierarchy has the given simple name.
	 */
	private boolean hasTypeNamed(String simpleName) {
		IType[] types = this.getAllTypes();
		for (int i = 0, length = types.length; i < length; i++) {
			if (types[i].getElementName().equals(simpleName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the given delta (a compilation unit delta or a class file delta)
	 * indicates that one of its types has a visibility change.
	 */
	private boolean hasVisibilityChange(IJavaElementDelta delta) {
		IJavaElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0, length = children.length; i < length; i++) {
			IJavaElementDelta child = children[i];
			if ((child.getFlags() & IJavaElementDelta.F_MODIFIERS) > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the simple name of a supertype of the given type is 
	 * the simple name of one of the types in this hierarchy.
	 */
	private boolean includesSupertypeOf(IType type) {
		IType[] supertypes = getSupertypes(type);
		for (int i = 0, length = supertypes.length; i < length; i++) {
			if (hasTypeNamed(supertypes[i].getElementName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Initializes this hierarchy's internal tables with the given size.
	 */
	protected void initialize(int size) {
		if (size < 10) {
			size = 10;
		}
		int smallSize = (size / 2);
		fClassToSuperclass = new Hashtable(size);
		fTypeToSubtypes = new Hashtable(smallSize);
		fTypeToSuperInterfaces = new Hashtable(smallSize);
	}

	/**
	 * Returns true if this hierarchy is actively tracking changes
	 * in the Java Model.
	 */
	protected boolean isActivated() {
		return fIsActivated;
	}

	/**
	 * Returns true if the given delta could change this type hierarchy
	 */
	private boolean isAffected(IJavaElementDelta delta) {
		IJavaElement element = delta.getElement();
		switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL :
				return isAffectedByJavaModel(delta, element);
			case IJavaElement.JAVA_PROJECT :
				return isAffectedByJavaProject(delta, element);
			case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				return isAffectedByPackageFragmentRoot(delta, element);
			case IJavaElement.PACKAGE_FRAGMENT :
				return isAffectedByPackageFragment(delta, element);
			case IJavaElement.CLASS_FILE :
			case IJavaElement.COMPILATION_UNIT :
				return isAffectedByType(delta, element);
		}
		return false;
	}

	/**
	 * Returns true if any of the children of a project, package
	 * fragment root, or package fragment have changed in a way that
	 * effects this type hierarchy.
	 */
	private boolean isAffectedByChildren(IJavaElementDelta delta) {
		if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) > 0) {
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				if (isAffected(children[i])) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the given java model delta could affect this type hierarchy
	 */
	private boolean isAffectedByJavaModel(
		IJavaElementDelta delta,
		IJavaElement element) {
		switch (delta.getKind()) {
			case IJavaElementDelta.ADDED :
			case IJavaElementDelta.REMOVED :
				return element.equals(this.javaProject().getJavaModel());
			case IJavaElementDelta.CHANGED :
				return isAffectedByChildren(delta);
		}
		return false;
	}

	/**
	 * Returns true if the given java project delta could affect this type hierarchy
	 */
	private boolean isAffectedByJavaProject(
		IJavaElementDelta delta,
		IJavaElement element) {
		switch (delta.getKind()) {
			case IJavaElementDelta.ADDED :
				try {
					// if the added project is on the classpath, then the hierarchy has changed
					IClasspathEntry[] classpath = this.javaProject().getResolvedClasspath(true);
					for (int i = 0; i < classpath.length; i++) {
						if (classpath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT
							&& classpath[i].getPath().equals(element.getUnderlyingResource().getFullPath())) {
							return true;
						}
					}
					return false;
				} catch (JavaModelException e) {
					return false;
				}
			case IJavaElementDelta.REMOVED :
				// removed project - if it contains packages we are interested in
				// then the type hierarchy has changed
				IJavaElement[] pkgs = fPackageRegion.getElements();
				for (int i = 0; i < pkgs.length; i++) {
					IJavaProject project = pkgs[i].getJavaProject();
					if (project != null && project.equals(element)) {
						return true;
					}
				}
				return false;
			case IJavaElementDelta.CHANGED :
				return isAffectedByChildren(delta);
		}
		return false;
	}

	/**
	 * Returns true if the given package fragment delta could affect this type hierarchy
	 */
	private boolean isAffectedByPackageFragment(
		IJavaElementDelta delta,
		IJavaElement element) {
		switch (delta.getKind()) {
			case IJavaElementDelta.ADDED :
				// if the package fragment is in the projects being considered, this could
				// introduce new types, changing the hierarchy
				return fProjectRegion.contains(element);
			case IJavaElementDelta.REMOVED :
				// is a change if the package fragment contains types in this hierarchy
				return packageRegionContainsSamePackageFragment(element);
			case IJavaElementDelta.CHANGED :
				// look at the files in the package fragment
				return isAffectedByChildren(delta);
		}
		return false;
	}

	/**
	 * Returns true if the given package fragment root delta could affect this type hierarchy
	 */
	private boolean isAffectedByPackageFragmentRoot(
		IJavaElementDelta delta,
		IJavaElement element) {
		switch (delta.getKind()) {
			case IJavaElementDelta.ADDED :
				return fProjectRegion.contains(element);
			case IJavaElementDelta.REMOVED :
			case IJavaElementDelta.CHANGED :
				if ((delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0
					|| (delta.getFlags() & IJavaElementDelta.F_CONTENT) > 0) {
					// 1. removed from classpath - if it contains packages we are interested in
					// the the type hierarchy has changed
					// 2. content of a jar changed - if it contains packages we are interested in
					// the the type hierarchy has changed
					IJavaElement[] pkgs = fPackageRegion.getElements();
					for (int i = 0; i < pkgs.length; i++) {
						if (pkgs[i].getParent().equals(element)) {
							return true;
						}
					}
					return false;
				}
		}
		return isAffectedByChildren(delta);
	}

	/**
	 * Returns true if the given type delta (a compilation unit delta or a class file delta)
	 * could affect this type hierarchy.
	 *
	 * The rules are:
	 * - if the delta is an added type X, then the hierarchy is changed 
	 *   . if one of the types in this hierarchy has a supertype whose simple name is the
	 *     simple name of X
	 *   . if the simple name of a supertype of X is the simple name of one of
	 *     the subtypes in this hierarchy (X will be added as one of the subtypes)
	 * - if the delta is a changed type X, then the hierarchy is changed
	 *   . if the visibility of X has changed and if one of the types in this hierarchy has a 
	 *	   supertype whose simple name is the simple name of X
	 *   . if one of the supertypes of X has changed or one of the imports has changed,
	 *     and if the simple name of a supertype of X is the simple name of one of 
	 *     the types in this hierarchy
	 * - if the delta is a removed type X, then the hierarchy is changed
	 *   . if the given element is part of this hierarchy (note we cannot acces the types 
	 *     because the element has been removed)
	 */
	protected boolean isAffectedByType(
		IJavaElementDelta delta,
		IJavaElement element) {
		// ignore changes to working copies
		if (element instanceof CompilationUnit
			&& ((CompilationUnit) element).isWorkingCopy()) {
			return false;
		}

		int kind = delta.getKind();
		if (kind == IJavaElementDelta.REMOVED) {
			return this.files.get(element) != null;
		} else {
			IType[] types = null;
			try {
				types =
					(element instanceof CompilationUnit)
						? ((CompilationUnit) element).getAllTypes()
						: new IType[] {((org.eclipse.jdt.internal.core.ClassFile) element).getType()};
			} catch (JavaModelException e) {
				e.printStackTrace();
				return false;
			}
			if (kind == IJavaElementDelta.ADDED) {
				for (int i = 0, length = types.length; i < length; i++) {
					IType type = types[i];
					if (typeHasSupertype(type) || subtypesIncludeSupertypeOf(type)) {
						return true;
					}
				}
			} else { // kind == IJavaElementDelta.CHANGED :
				boolean hasSupertypeChange = hasSuperTypeOrImportChange(delta);
				boolean hasVisibilityChange = hasVisibilityChange(delta);
				for (int i = 0, length = types.length; i < length; i++) {
					IType type = types[i];
					if ((hasVisibilityChange && typeHasSupertype(type))
						|| (hasSupertypeChange && includesSupertypeOf(type))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns the java project this hierarchy was created in.
	 */
	public IJavaProject javaProject() {
		return fType.getJavaProject();
	}

	/**
	 * Returns <code>true</code> if an equivalent package fragment is included in the package
	 * region. Package fragments are equivalent if they both have the same name.
	 */
	protected boolean packageRegionContainsSamePackageFragment(IJavaElement element) {
		IJavaElement[] pkgs = fPackageRegion.getElements();
		for (int i = 0; i < pkgs.length; i++) {
			if (pkgs[i].getElementName().equals(element.getElementName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prunes this type hierarchy to only contain the branch to the given type,
	 * and its subtree. Pruning is only done for classes.
	 */
	protected void pruneTypeHierarchy(IType type, IProgressMonitor monitor)
		throws JavaModelException {
		if (type.isClass()) {
			IType[] supers = getAllSuperclasses(type);

			if (supers.length == 0) {
				// nothing to prune if this is a root - unless there are other roots
				return;
			}

			IType[] branch = new IType[supers.length + 1];
			System.arraycopy(supers, 0, branch, 1, supers.length);
			branch[0] = type;
			// Branch is a list from the root to our type
			// Walk the branch pruning all other subtrees
			for (int i = branch.length - 1; i > 0; i--) {
				IType[] subtrees = getSubtypes(branch[i]);
				for (int j = 0; j < subtrees.length; j++) {
					if (!subtrees[j].equals(branch[i - 1])) {
						removeType(subtrees[j]);
					}
					this.worked(1);
				}
				fTypeToSubtypes.put(branch[i], new TypeVector(branch[i - 1]));
			}
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public void refresh(IProgressMonitor monitor) throws JavaModelException {
		try {
			boolean reactivate = isActivated();
			Vector listeners = fChangeListeners;
			if (reactivate) {
				deactivate();
			}
			fProgressMonitor = monitor;
			if (monitor != null) {
				monitor.beginTask("Creating type hierarchy...", IProgressMonitor.UNKNOWN);
			}
			compute();
			if (fType != null) {
				//prune the hierarchy tree to only include branch and subtree for the type
				pruneTypeHierarchy(fType, monitor);
			}
			if (reactivate) {
				activate();
				fChangeListeners = listeners;
			}
			if (monitor != null) {
				monitor.done();
			}
			fProgressMonitor = null;
		} catch (JavaModelException e) {
			fProgressMonitor = null;
			throw e;
		} catch (CoreException e) {
			fProgressMonitor = null;
			throw new JavaModelException(e);
		} catch (OperationCanceledException oce) {
			refreshCancelled(oce);
		}
	}

	/**
	 * The refresh of this type hierarchy has been cancelled.
	 * Cleanup the state of this now invalid type hierarchy.
	 */
	protected void refreshCancelled(OperationCanceledException oce)
		throws JavaModelException {
		destroy();
		fProgressMonitor = null;
		throw oce;
	}

	/**
	 * Removes all the subtypes of the given type from the type hierarchy,
	 * and removes its superclass entry.
	 */
	protected void removeType(IType type) throws JavaModelException {
		IType[] subtypes = getSubtypes(type);
		fTypeToSubtypes.remove(type);
		fClassToSuperclass.remove(type);
		if (subtypes != null) {
			for (int i = 0; i < subtypes.length; i++) {
				removeType(subtypes[i]);
			}
		}
	}

	/**
	 * @see ITypeHierarchy
	 */
	public void removeTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener) {
		if (fChangeListeners == null) {
			return;
		}
		fChangeListeners.removeElement(listener);
		if (fChangeListeners.isEmpty()) {
			deactivate();
		}
	}

	/**
	 * Returns whether the simple name of a supertype of the given type is 
	 * the simple name of one of the subtypes in this hierarchy or the
	 * simple name of this type.
	 */
	private boolean subtypesIncludeSupertypeOf(IType type) {
		// look for superclass
		String superclassName = null;
		try {
			superclassName = type.getSuperclassName();
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
		if (superclassName == null) {
			superclassName = "Object";
		}
		int dot = -1;
		String simpleSuper =
			(dot = superclassName.lastIndexOf('.')) > -1
				? superclassName.substring(dot + 1)
				: superclassName;
		if (hasSubtypeNamed(simpleSuper)) {
			return true;
		}

		// look for super interfaces
		String[] interfaceNames = null;
		try {
			interfaceNames = type.getSuperInterfaceNames();
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
		for (int i = 0, length = interfaceNames.length; i < length; i++) {
			dot = -1;
			String interfaceName = interfaceNames[i];
			String simpleInterface =
				(dot = interfaceName.lastIndexOf('.')) > -1
					? interfaceName.substring(dot)
					: interfaceName;
			if (hasSubtypeNamed(simpleInterface)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see ITypeHierarchy
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Focus: ");
		buffer.append(fType == null ? "<NONE>" : fType.getFullyQualifiedName());
		buffer.append("\n");
		if (exists()) {
			if (fType != null) {
				buffer.append("Super types:\n");
				toString(buffer, fType, 1, true);
				buffer.append("Sub types:\n");
				toString(buffer, fType, 1, false);
			} else {
				buffer.append("Sub types of root classes:\n");
				IType[] roots = getRootClasses();
				for (int i = 0; i < roots.length; i++) {
					toString(buffer, roots[i], 1, false);
				}
			}
		} else {
			buffer.append("(Hierarchy became stale)");
		}
		return buffer.toString();
	}

	/**
	 * Append a String to the given buffer representing the hierarchy for the type,
	 * beginning with the specified indentation level.
	 * If ascendant, shows the super types, otherwise show the sub types.
	 */
	private void toString(
		StringBuffer buffer,
		IType type,
		int indent,
		boolean ascendant) {
		for (int i = 0; i < indent; i++) {
			buffer.append("  ");
		}
		buffer.append(type.getFullyQualifiedName());
		buffer.append('\n');

		IType[] types = ascendant ? getSupertypes(type) : getSubtypes(type);
		for (int i = 0; i < types.length; i++) {
			toString(buffer, types[i], indent + 1, ascendant);
		}

	}

	/**
	 * Returns whether one of the types in this hierarchy has a supertype whose simple 
	 * name is the simple name of the given type.
	 */
	private boolean typeHasSupertype(IType type) {
		String simpleName = type.getElementName();
		Enumeration enum = fClassToSuperclass.elements();
		while (enum.hasMoreElements()) {
			IType superType = (IType) enum.nextElement();
			if (superType.getElementName().equals(simpleName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see IProgressMonitor
	 */
	protected void worked(int work) {
		if (fProgressMonitor != null) {
			fProgressMonitor.worked(work);
			checkCanceled();
		}
	}

}
