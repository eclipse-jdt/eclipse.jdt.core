package org.eclipse.jdt.internal.core.hierarchy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.jdt.internal.compiler.HierarchyResolver;
import org.eclipse.jdt.internal.compiler.IHierarchyRequestor;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IGenericType;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public abstract class HierarchyBuilder implements IHierarchyRequestor {
	/**
	 * The hierarchy being built.
	 */
	protected TypeHierarchy hierarchy;
	
	/**
	 * The name environment used by the HierarchyResolver
	 */
	protected SearchableEnvironment searchableEnvironment;

	/**
	 * @see INameLookup
	 */
	protected INameLookup nameLookup;

	/**
	 * The resolver used to resolve type hierarchies
	 * @see HierarchyResolver
	 */
	protected HierarchyResolver hierarchyResolver;

	/**
	 * A temporary cache of infos to handles to speed info
	 * to handle translation - it only contains the entries
	 * for the types in the region (i.e. no supertypes outside
	 * the region).
	 */
	protected Hashtable infoToHandle;
public HierarchyBuilder(TypeHierarchy hierarchy) throws JavaModelException {
	this.hierarchy = hierarchy;
	JavaProject project = (JavaProject)hierarchy.javaProject();
	this.searchableEnvironment = (SearchableEnvironment)project.getSearchableNameEnvironment();
	this.nameLookup = project.getNameLookup();
	this.hierarchyResolver = 
		new HierarchyResolver(this.searchableEnvironment, this, new DefaultProblemFactory());
	this.infoToHandle = new Hashtable(5);
}
public abstract void build(boolean computeSubtypes) throws JavaModelException, CoreException;
/**
 * Configure this type hierarchy by computing the supertypes only.
 */
protected void buildSupertypes() {
	IType focusType = this.getType();
	if (focusType == null) return;
	
	// get generic type from focus type
	IGenericType type;
	try {
		type = (IGenericType)((JavaElement) focusType).getRawInfo();
	} catch (JavaModelException e) {
		// if the focus type is not present, or if cannot get workbench path
		// we cannot create the hierarchy
		return;
	}

	// resolve
	this.searchableEnvironment.unitToLookInside = (CompilationUnit)focusType.getCompilationUnit();
	this.hierarchyResolver.resolve(type);
	this.searchableEnvironment.unitToLookInside = null;

	// Add focus if not already in (case of a type with no explicit super type)
	if (!this.hierarchy.contains(focusType)) {
		this.hierarchy.addRootClass(focusType);
	}
}
/**
 * @see IHierarchyRequestor
 */
public void connect(IGenericType suppliedType, IGenericType superclass, IGenericType[] superinterfaces) {

	this.worked(1);
	
	// convert all infos to handles
	IType typeHandle= getHandle(suppliedType);

	/*
	 * Temporary workaround for 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class
	 */
	if (typeHandle == null) return;
	
	IType superHandle= null;
	if (superclass != null) {
		superHandle= getHandle(superclass);
	}
	IType[] interfaceHandles= null;
	if (superinterfaces != null && superinterfaces.length > 0) {
		interfaceHandles= new IType[superinterfaces.length];
		for (int i= 0; i < interfaceHandles.length; i++) {
			interfaceHandles[i]= getHandle(superinterfaces[i]);
		}
	}

	// now do the caching
	if (suppliedType.isClass()) {
		if (superHandle == null) {
			this.hierarchy.addRootClass(typeHandle);
		} else {
			this.hierarchy.cacheSuperclass(typeHandle, superHandle);
		}
	} else {
		this.hierarchy.addInterface(typeHandle);
	}

	if (interfaceHandles == null) {
		interfaceHandles= this.hierarchy.fgEmpty;
	}
	this.hierarchy.cacheSuperInterfaces(typeHandle, interfaceHandles);
}
/**
 * Returns a handle for the given generic type or null if not found.
 */
protected IType getHandle(IGenericType genericType) {
	if (genericType == null) return null;
	if (genericType.isBinaryType()) {
		IClassFile classFile = (IClassFile)this.infoToHandle.get(genericType);
		// if it's null, it's from outside the region, so do lookup
		if (classFile == null) {
			IType handle = lookupBinaryHandle((IBinaryType)genericType);
			if (handle == null) return null; // case of an anonymous type (see 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class)
			
			// optimization: remember the handle for next call (case of java.io.Serializable that a lot of classes implement)
			this.infoToHandle.put(genericType, handle.getParent());
			return handle;
		} else {
			try {
				return classFile.getType();
			} catch (JavaModelException e) {
				return null;
			}
		}
	} else if (genericType instanceof SourceTypeElementInfo) {
		return ((SourceTypeElementInfo)genericType).getHandle();
	} else 
		return null;
}
protected IType getType() {
	return this.hierarchy.getType();
}
/**
 * Looks up and returns a handle for the given binary info.
 */
protected IType lookupBinaryHandle(IBinaryType typeInfo) {
	int flag;
	String qualifiedName;
	if (typeInfo.isClass()) {
		flag = this.nameLookup.ACCEPT_CLASSES;
	} else {
		flag = this.nameLookup.ACCEPT_INTERFACES;
	}
	char[] bName = typeInfo.getName();
	qualifiedName = new String(ClassFile.translatedName(bName));
	return this.nameLookup.findType(qualifiedName, false, flag);
}
protected void worked(int work) {
	IProgressMonitor progressMonitor = this.hierarchy.fProgressMonitor;
	if (progressMonitor != null) {
		if (progressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		} else {
			progressMonitor.worked(work);
		}
	}
}
}
