/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.hierarchy;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.util.Util;

public abstract class HierarchyBuilder implements IHierarchyRequestor {
	/**
	 * The hierarchy being built.
	 */
	protected TypeHierarchy hierarchy;
	/**
	 * @see NameLookup
	 */
	protected NameLookup nameLookup;
	/**
	 * The resolver used to resolve type hierarchies
	 * @see HierarchyResolver
	 */
	protected HierarchyResolver hierarchyResolver;
	/**
	 * A temporary cache of infos to handles to speed info
	 * to handle translation - it only contains the entries
	 * for the types in the region (in other words, it contains
	 * no supertypes outside the region).
	 */
	protected Map infoToHandle;
	/*
	 * The dot-separated fully qualified name of the focus type, or null of none.
	 */
	protected String focusQualifiedName;
	
	public HierarchyBuilder(TypeHierarchy hierarchy) throws JavaModelException {
		
		this.hierarchy = hierarchy;
		JavaProject project = (JavaProject) hierarchy.javaProject();
		
		IType focusType = hierarchy.getType();
		org.eclipse.jdt.core.ICompilationUnit unitToLookInside = focusType == null ? null : focusType.getCompilationUnit();
		org.eclipse.jdt.core.ICompilationUnit[] workingCopies = this.hierarchy.workingCopies;
		org.eclipse.jdt.core.ICompilationUnit[] unitsToLookInside;
		if (unitToLookInside != null) {
			int wcLength = workingCopies == null ? 0 : workingCopies.length;
			if (wcLength == 0) {
				unitsToLookInside = new org.eclipse.jdt.core.ICompilationUnit[] {unitToLookInside};
			} else {
				unitsToLookInside = new org.eclipse.jdt.core.ICompilationUnit[wcLength+1];
				unitsToLookInside[0] = unitToLookInside;
				System.arraycopy(workingCopies, 0, unitsToLookInside, 1, wcLength);
			}
		} else {
			unitsToLookInside = workingCopies;
		}
		SearchableEnvironment searchableEnvironment = (SearchableEnvironment) project.newSearchableNameEnvironment(unitsToLookInside);
		this.nameLookup = searchableEnvironment.nameLookup;
		this.hierarchyResolver =
			new HierarchyResolver(
				searchableEnvironment,
				project.getOptions(true),
				this,
				new DefaultProblemFactory());
		this.infoToHandle = new HashMap(5);
		this.focusQualifiedName = focusType == null ? null : focusType.getFullyQualifiedName();
	}
	
	public abstract void build(boolean computeSubtypes)
		throws JavaModelException, CoreException;
	/**
	 * Configure this type hierarchy by computing the supertypes only.
	 */
	protected void buildSupertypes() {
		IType focusType = this.getType();
		if (focusType == null)
			return;
		// get generic type from focus type
		IGenericType type;
		try {
			type = (IGenericType) ((JavaElement) focusType).getElementInfo();
		} catch (JavaModelException e) {
			// if the focus type is not present, or if cannot get workbench path
			// we cannot create the hierarchy
			return;
		}
		//NB: no need to set focus type on hierarchy resolver since no other type is injected
		//    in the hierarchy resolver, thus there is no need to check that a type is 
		//    a sub or super type of the focus type.
		this.hierarchyResolver.resolve(type);

		// Add focus if not already in (case of a type with no explicit super type)
		if (!this.hierarchy.contains(focusType)) {
			this.hierarchy.addRootClass(focusType);
		}
	}
	/**
	 * @see IHierarchyRequestor
	 */
	public void connect(
		IGenericType suppliedType,
		IGenericType superclass,
		IGenericType[] superinterfaces) {

		// convert all infos to handles
		IType typeHandle = getHandle(suppliedType);
		/*
		 * Temporary workaround for 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class
		 */
		if (typeHandle == null)
			return;
		IType superHandle = null;
		if (superclass != null) {
			if (superclass instanceof HierarchyResolver.MissingType) {
				this.hierarchy.missingTypes.add(((HierarchyResolver.MissingType)superclass).simpleName);
			} else {
				superHandle = getHandle(superclass);
			}
		}
		IType[] interfaceHandles = null;
		if (superinterfaces != null && superinterfaces.length > 0) {
			int length = superinterfaces.length;
			IType[] resolvedInterfaceHandles = new IType[length];
			int index = 0;
			for (int i = 0; i < length; i++) {
				IGenericType superInterface = superinterfaces[i];
				if (superInterface != null) {
					if (superInterface instanceof HierarchyResolver.MissingType) {
						this.hierarchy.missingTypes.add(((HierarchyResolver.MissingType)superInterface).simpleName);
					} else {
						resolvedInterfaceHandles[index] = getHandle(superInterface);
						if (resolvedInterfaceHandles[index] != null) {
							index++;
						}
					}
				}
			}
			// resize
			System.arraycopy(
				resolvedInterfaceHandles,
				0,
				interfaceHandles = new IType[index],
				0,
				index);
		}
		if (TypeHierarchy.DEBUG) {
			System.out.println(
				"Connecting: " + ((JavaElement) typeHandle).toStringWithAncestors()); //$NON-NLS-1$
			System.out.println(
				"  to superclass: " //$NON-NLS-1$
					+ (superHandle == null
						? "<None>" //$NON-NLS-1$
						: ((JavaElement) superHandle).toStringWithAncestors()));
			System.out.print("  and superinterfaces:"); //$NON-NLS-1$
			if (interfaceHandles == null || interfaceHandles.length == 0) {
				System.out.println(" <None>"); //$NON-NLS-1$
			} else {
				System.out.println();
				for (int i = 0, length = interfaceHandles.length; i < length; i++) {
					System.out.println(
						"    " + ((JavaElement) interfaceHandles[i]).toStringWithAncestors()); //$NON-NLS-1$
				}
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
			interfaceHandles = TypeHierarchy.NO_TYPE;
		}
		this.hierarchy.cacheSuperInterfaces(typeHandle, interfaceHandles);
		 
		// record flags
		this.hierarchy.cacheFlags(typeHandle, suppliedType.getModifiers());
	}
	/**
	 * Returns a handle for the given generic type or null if not found.
	 */
	protected IType getHandle(IGenericType genericType) {
		if (genericType == null)
			return null;
		if (genericType instanceof HierarchyType) {
			IType handle = (IType)this.infoToHandle.get(genericType);
			if (handle == null) {
				handle = ((HierarchyType)genericType).typeHandle;
				this.infoToHandle.put(genericType, handle);
			}
			return handle;
		} else if (genericType.isBinaryType()) {
			IClassFile classFile = (IClassFile) this.infoToHandle.get(genericType);
			// if it's null, it's from outside the region, so do lookup
			if (classFile == null) {
				IType handle = lookupBinaryHandle((IBinaryType) genericType);
				if (handle == null)
					return null;
				// case of an anonymous type (see 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class)
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
			return ((SourceTypeElementInfo) genericType).getHandle();
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
			flag = NameLookup.ACCEPT_CLASSES;
		} else {
			flag = NameLookup.ACCEPT_INTERFACES;
		}
		char[] bName = typeInfo.getName();
		qualifiedName = new String(ClassFile.translatedName(bName));
		if (qualifiedName.equals(this.focusQualifiedName)) return getType();
		return this.nameLookup.findType(qualifiedName, false, flag);
	}
	protected void worked(IProgressMonitor monitor, int work) {
		if (monitor != null) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			} else {
				monitor.worked(work);
			}
		}
	}
/**
 * Create an ICompilationUnit info from the given compilation unit on disk.
 */
protected ICompilationUnit createCompilationUnitFromPath(Openable handle, String osPath) {
	return 
		new BasicCompilationUnit(
			null,
			null,
			osPath,
			handle);
}
	/**
 * Creates the type info from the given class file on disk and
 * adds it to the given list of infos.
 */
protected IBinaryType createInfoFromClassFile(Openable handle, String osPath) {
	IBinaryType info = null;
	try {
		info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(osPath);
	} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
		if (TypeHierarchy.DEBUG) {
			e.printStackTrace();
		}
		return null;
	} catch (java.io.IOException e) {
		if (TypeHierarchy.DEBUG) {
			e.printStackTrace();
		}
		return null;
	}						
	this.infoToHandle.put(info, handle);
	return info;
}
	/**
 * Create a type info from the given class file in a jar and adds it to the given list of infos.
 */
protected IBinaryType createInfoFromClassFileInJar(Openable classFile) {
	PackageFragment pkg = (PackageFragment) classFile.getParent();
	String classFilePath = Util.concatWith(pkg.names, classFile.getElementName(), '/');
	IBinaryType info = null;
	java.util.zip.ZipFile zipFile = null;
	try {
		zipFile = ((JarPackageFragmentRoot)pkg.getParent()).getJar();
		info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(
			zipFile,
			classFilePath);
	} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
		if (TypeHierarchy.DEBUG) {
			e.printStackTrace();
		}
		return null;
	} catch (java.io.IOException e) {
		if (TypeHierarchy.DEBUG) {
			e.printStackTrace();
		}
		return null;
	} catch (CoreException e) {
		if (TypeHierarchy.DEBUG) {
			e.printStackTrace();
		}
		return null;
	} finally {
		JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
	}
	this.infoToHandle.put(info, classFile);
	return info;
}

}
