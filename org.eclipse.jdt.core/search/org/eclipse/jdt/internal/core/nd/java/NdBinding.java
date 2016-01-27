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
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Base class for bindings in the PDOM.
 * 
 * @since 3.12
 */
public abstract class NdBinding extends NdNode implements IAdaptable {
	public static final FieldOneToMany<NdAnnotation> ANNOTATIONS;
	public static final FieldInt MODIFIERS;
	public static final FieldOneToMany<NdTypeParameter> TYPE_PARAMETERS;
	public static final FieldManyToOne<NdResourceFile> FILE;
	public static final FieldOneToMany<NdVariable> VARIABLES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdBinding> type;

	static {
		type = StructDef.create(NdBinding.class, NdNode.type);
		ANNOTATIONS = FieldOneToMany.create(type, NdAnnotation.PARENT_BINDING);
		MODIFIERS = type.addInt();
		TYPE_PARAMETERS = FieldOneToMany.create(type, NdTypeParameter.PARENT);
		FILE = FieldManyToOne.createOwner(type, NdResourceFile.ALL_NODES);
		VARIABLES = FieldOneToMany.create(type, NdVariable.PARENT);
		type.done();
	}

	private static final NdAnnotation[] NO_ANNOTATIONS = new NdAnnotation[0];

	public NdBinding(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdBinding(Nd pdom, NdResourceFile resource) {
		super(pdom);

		FILE.put(pdom, this.address, resource);
	}

	/**
	 * Tests whether this binding has one of the flags defined in {@link Flags}
	 */
	public boolean hasModifier(int toTest) {
		return (MODIFIERS.get(getNd(), this.address) & toTest) != 0;
	}

	/**
	 * Sets the modifiers for this binding (defined in {@link Flags})
	 */
	public void setModifiers(int toSet) {
		MODIFIERS.put(getNd(), this.address, toSet);
	}

	public int getModifiers() {
		return MODIFIERS.get(getNd(), this.address);
	}

	public NdAnnotation[] getAnnotations() {
		int numAnnotations = ANNOTATIONS.size(getNd(), this.address);

		if (numAnnotations == 0) {
			return NO_ANNOTATIONS;
		}

		final NdAnnotation[] result = new NdAnnotation[numAnnotations];

		// If we got this far, the pointer to the linked list is non-null
		ANNOTATIONS.accept(getNd(), this.address, new FieldOneToMany.Visitor<NdAnnotation>() {
			@Override
			public void visit(int index, NdAnnotation toVisit) {
				result[index] = toVisit;
			}
		});

		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(NdBinding.class))
			return this;

		// Any PDOMBinding can have a persistent tag. These tags should be deleted when
		// the PDOMBinding is deleted. However, PDOMBinding's don't get deleted, so there is no way
		// to trigger deleting of the tags. If the implementation is changed so that PDOMBindings
		// do get deleted, then it should call:
		// PDOMTagIndex.setTags(getPDOM(), pdomBinding.address, Collections.<ITag>emptyList());
		// to clear out all tags for the binding.
		// if (adapter.isAssignableFrom(ITagReader.class))
		// return new PDOMTaggable(getPDOM(), getRecord());

		return null;
	}

	// /**
	// * Is the binding as the record orphaned, i.e., has no declarations
	// * or references.
	// * Watch out, a binding may also be used in a type (e.g. pointer to class)
	// *
	// * @param pdom
	// * @param address
	// * @return <code>true</code> if the binding is orphaned.
	// * @throws IndexException
	// */
	// public static boolean isOrphaned(PDOM pdom, long address) throws IndexException {
	// Database db = pdom.getDB();
	// return db.getRecPtr(address + FIRST_DECL_OFFSET) == 0
	// && db.getRecPtr(address + FIRST_DEF_OFFSET) == 0
	// && db.getRecPtr(address + FIRST_REF_OFFSET) == 0
	// && db.getRecPtr(address + FIRST_EXTREF_OFFSET) == 0;
	// }
	//
	// public final boolean hasDeclaration() throws IndexException {
	// if (hasDeclaration == -1) {
	// final Database db = getDB();
	// if (db.getRecPtr(address + FIRST_DECL_OFFSET) != 0
	// || db.getRecPtr(address + FIRST_DEF_OFFSET) != 0) {
	// hasDeclaration= 1;
	// return true;
	// }
	// hasDeclaration= 0;
	// return false;
	// }
	// return hasDeclaration != 0;
	// }

	// public final void addDeclaration(PDOMName name) throws IndexException {
	// PDOMName first = getFirstDeclaration();
	// if (first != null) {
	// first.setPrevInBinding(name);
	// name.setNextInBinding(first);
	// }
	// setFirstDeclaration(name);
	// }
	//
	// public final void addDefinition(PDOMName name) throws IndexException {
	// PDOMName first = getFirstDefinition();
	// if (first != null) {
	// first.setPrevInBinding(name);
	// name.setNextInBinding(first);
	// }
	// setFirstDefinition(name);
	// }
	//
	// public final void addReference(PDOMName name) throws IndexException {
	// // This needs to filter between the local and external lists because it can be used in
	// // contexts that don't know which type of list they are iterating over. E.g., this is
	// // used when deleting names from a PDOMFile.
	// if (!getLinkage().equals(name.getLinkage())) {
	// new PDOMExternalReferencesList(getPDOM(), address + FIRST_EXTREF_OFFSET).add(name);
	// return;
	// }
	//
	// PDOMName first = getFirstReference();
	// if (first != null) {
	// first.setPrevInBinding(name);
	// name.setNextInBinding(first);
	// }
	// setFirstReference(name);
	// }
	//
	// public PDOMName getFirstDeclaration() throws IndexException {
	// long namerec = getDB().getRecPtr(address + FIRST_DECL_OFFSET);
	// return namerec != 0 ? new PDOMName(getLinkage(), namerec) : null;
	// }
	//
	// public void setFirstDeclaration(PDOMName name) throws IndexException {
	// long namerec = name != null ? name.getRecord() : 0;
	// getDB().putRecPtr(address + FIRST_DECL_OFFSET, namerec);
	// }
	//
	// public PDOMName getFirstDefinition() throws IndexException {
	// long namerec = getDB().getRecPtr(address + FIRST_DEF_OFFSET);
	// return namerec != 0 ? new PDOMName(getLinkage(), namerec) : null;
	// }
	//
	// public void setFirstDefinition(PDOMName name) throws IndexException {
	// long namerec = name != null ? name.getRecord() : 0;
	// getDB().putRecPtr(address + FIRST_DEF_OFFSET, namerec);
	// }
	//
	// public PDOMName getFirstReference() throws IndexException {
	// long namerec = getDB().getRecPtr(address + FIRST_REF_OFFSET);
	// return namerec != 0 ? new PDOMName(getLinkage(), namerec) : null;
	// }
	//
	// /**
	// * Returns an iterator over the names in other linkages that reference this binding. Does
	// * not return null.
	// */
	// public IPDOMIterator<PDOMName> getExternalReferences() throws IndexException {
	// return new PDOMExternalReferencesList(getPDOM(), address + FIRST_EXTREF_OFFSET).getIterator();
	// }
	//
	// /**
	// * In most cases the linkage can be found from the linkage of the name. However, when the
	// * list is being cleared (there is no next), the linkage must be passed in.
	// */
	// public void setFirstReference(PDOMLinkage linkage, PDOMName name) throws IndexException {
	// if (linkage.equals(getLinkage())) {
	// setFirstReference(name);
	// } else {
	// new PDOMExternalReferencesList(getPDOM(), address + FIRST_EXTREF_OFFSET).setFirstReference(linkage, name);
	// }
	// }
	//
	// private void setFirstReference(PDOMName name) throws IndexException {
	// // This needs to filter between the local and external lists because it can be used in
	// // contexts that don't know which type of list they are iterating over. E.g., this is
	// // used when deleting names from a PDOMFile.
	// if (name != null
	// && !getLinkage().equals(name.getLinkage())) {
	// new PDOMExternalReferencesList(getPDOM(), address + FIRST_EXTREF_OFFSET).add(name);
	// return;
	// }
	//
	// // Otherwise put the reference into list of locals.
	// long namerec = name != null ? name.getRecord() : 0;
	// getDB().putRecPtr(address + FIRST_REF_OFFSET, namerec);
	// }
	//
	// @Override
	// public final PDOMFile getLocalToFile() throws IndexException {
	// final long filerec = getLocalToFileRec(getDB(), address);
	// return filerec == 0 ? null : new PDOMFile(getLinkage(), filerec);
	// }
	//
	// public final long getLocalToFileRec() throws IndexException {
	// return getLocalToFileRec(getDB(), address);
	// }

	// public static long getLocalToFileRec(Database db, long address) throws IndexException {
	// return db.getRecPtr(address + LOCAL_TO_FILE);
	// }
	//
	// public final void setLocalToFileRec(long rec) throws IndexException {
	// getDB().putRecPtr(address + LOCAL_TO_FILE, rec);
	// }

	// public String getName() {
	// try {
	// return super.getDBName().getString();
	// } catch (IndexException e) {
	// Package.log(e);
	// }
	// return ""; //$NON-NLS-1$
	// }
	//
	//
	// public char[] getNameCharArray() {
	//
	// try {
	// return super.getNameCharArray();
	// } catch (IndexException e) {
	// Package.log(e);
	// }
	// return CharArrayUtils.EMPTY;
	// }
	//
	// public IIndexScope getParent() {
	// try {
	// IBinding parent = getParentBinding();
	// if (parent instanceof IIndexScope) {
	// return (IIndexScope) parent;
	// }
	// } catch (IndexException e) {
	// CCorePlugin.log(e);
	// }
	// return getLinkage().getGlobalScope();
	// }
	//
	// @Override
	// public IIndexScope getScope() {
	// // The parent node in the binding hierarchy is the scope.
	// try {
	// IBinding parent= getParentBinding();
	// if (parent instanceof IIndexScope) {
	// return (IIndexScope) parent;
	// }
	// } catch (IndexException e) {
	// CCorePlugin.log(e);
	// }
	// return getLinkage().getGlobalScope();
	// }
	//
	// @Override
	// public IIndexFragment getFragment() {
	// return getPDOM();
	// }
	// /** For debug purposes only. */
	// @Override
	// public final String toString() {
	// String name = toStringBase();
	// try {
	// PDOMFile localToFile = getLocalToFile();
	// if (localToFile != null)
	// return name + " (local to " + localToFile.getLocation().getURI().getPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	// } catch (IndexException e) {
	// }
	// return name;
	// }
	//
	// protected String toStringBase() {
	// if (this instanceof IType) {
	// return ASTTypeUtil.getType((IType) this);
	// } else if (this instanceof IFunction) {
	// IFunctionType t= null;
	// t = ((IFunction) this).getType();
	// if (t != null) {
	// return getName() + ASTTypeUtil.getParameterTypeString(t);
	// } else {
	// return getName() + "()"; //$NON-NLS-1$
	// }
	// }
	// return getName();
	// }
	//
	// /**
	// * For debug purposes only.
	// * @param linkage
	// * @param value
	// * @return String representation of <code>value</code>.
	// */
	// protected static String getConstantNameForValue(PDOMLinkage linkage, int value) {
	// Class<? extends PDOMLinkage> c= linkage.getClass();
	// Field[] fields= c.getFields();
	// for (Field field : fields) {
	// try {
	// field.setAccessible(true);
	// if ((field.getModifiers() & Modifier.STATIC) != 0) {
	// if (int.class.equals(field.getType())) {
	// int fvalue= field.getInt(null);
	// if (fvalue == value)
	// return field.getName();
	// }
	// }
	// } catch (IllegalAccessException e) {
	// continue;
	// } catch (IllegalArgumentException e) {
	// continue;
	// }
	// }
	// return Integer.toString(value);
	// }
	//
	// public PDOMName getScopeName() {
	// try {
	// PDOMName name = getFirstDefinition();
	// if (name == null)
	// name = getFirstDeclaration();
	// return name;
	// } catch (IndexException e) {
	// CCorePlugin.log(e);
	// return null;
	// }
	// }
	//
	// @Override
	// public String[] getQualifiedName() {
	// return new String[] { getName() };
	// }
	//
	// @Override
	// final public boolean isFileLocal() throws IndexException {
	// return getDB().getRecPtr(address + LOCAL_TO_FILE) != 0;
	// }
	//
	// @Override
	// public boolean hasDefinition() throws IndexException {
	// return getDB().getRecPtr(address + FIRST_DEF_OFFSET) != 0;
	// }
	//
	// /**
	// * Compares two binding fully qualified names. If b0 has
	// * less segments than b1 then -1 is returned, if b0 has
	// * more segments than b1 then 1 is returned. If the segment
	// * lengths are equal then comparison is lexicographical on each
	// * component name, beginning with the most nested name and working
	// * outward.
	// * If one of the bindings in the hierarchy is file-local it is treated as a different
	// * binding.
	// * The first non-zero comparison is returned as the result.
	// * @param b0
	// * @param b1
	// * @return <ul><li> -1 if b0 &lt; b1
	// * <li> 0 if b0 == b1
	// * <li> 1 if b0 &gt; b1
	// * </ul>
	// * @throws IndexException
	// */
	// private static int comparePDOMBindingQNs(PDOMBinding b0, PDOMBinding b1) {
	// try {
	// int cmp = 0;
	// do {
	// IString s0 = b0.getDBName(), s1 = b1.getDBName();
	// cmp = s0.compare(s1, true);
	// if (cmp == 0) {
	// long l1= b0.getLocalToFileRec();
	// long l2= b1.getLocalToFileRec();
	// if (l1 != l2) {
	// return l1 < l2 ? -1 : 1;
	// }
	// b0 = (PDOMBinding) b0.getParentBinding();
	// b1 = (PDOMBinding) b1.getParentBinding();
	// if (b0 == null || b1 == null) {
	// cmp = b0 == b1 ? 0 : (b0 == null ? -1 : 1);
	// }
	// }
	// } while (cmp == 0 && b1 != null && b0 != null);
	// return cmp;
	// } catch (IndexException ce) {
	// Package.log(ce);
	// return -1;
	// }
	// }
	//
	// /**
	// * Compares two PDOMBinding objects in accordance with
	// * {@link IIndexFragmentBindingComparator#compare(IIndexFragmentBinding, IIndexFragmentBinding)}
	// * @param other
	// * @return comparison result, -1, 0, or 1.
	// */
	// public int pdomCompareTo(PDOMBinding other) {
	// int cmp = comparePDOMBindingQNs(this, other);
	// if (cmp == 0) {
	// int t1 = getNodeType();
	// int t2 = other.getNodeType();
	// return t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
	// }
	// return cmp;
	// }
	//
	// /**
	// * Returns whether pdomCompareTo returns zero
	// */
	// public final boolean pdomEquals(PDOMBinding other) {
	// return pdomCompareTo(other)==0;
	// }

	public final int getBindingConstant() {
		return getNodeType();
	}

	public void setFile(NdResourceFile file) {
		FILE.put(getNd(), this.address, file);
	}

	public NdResourceFile getFile() {
		return FILE.get(getNd(), this.address);
	}

	// @Override
	// final public void delete() throws IndexException {
	// assert false;
	// }
	//
	// /**
	// * Bindings may set additional flags for their occurrences
	// * Return a combination of flags defined in {@link PDOMName}.
	// * @since 5.0
	// */
	// public int getAdditionalNameFlags(int standardFlags, IASTName name) {
	// return 0;
	// }
	//
	// public final IBinding getBinding(IASTName name, boolean resolve) {
	// return getBinding(name, resolve, null);
	// }
	//
	// public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
	// return null;
	// }
	//
	// public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
	// return getBindings(new ScopeLookupData(name, resolve, prefix));
	// }
	//
	// @Deprecated
	// public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix, IIndexFileSet fileSet) {
	// return IBinding.EMPTY_BINDING_ARRAY;
	// }
	//
	// public IBinding[] getBindings(ScopeLookupData lookup) {
	// return IBinding.EMPTY_BINDING_ARRAY;
	// }
}
