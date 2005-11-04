/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A <code>NameLookup</code> provides name resolution within a Java project.
 * The name lookup facility uses the project's classpath to prioritize the 
 * order in which package fragments are searched when resolving a name.
 *
 * <p>Name lookup only returns a handle when the named element actually
 * exists in the model; otherwise <code>null</code> is returned.
 *
 * <p>There are two logical sets of methods within this interface.  Methods
 * which start with <code>find*</code> are intended to be convenience methods for quickly
 * finding an element within another element; for instance, for finding a class within a
 * package.  The other set of methods all begin with <code>seek*</code>.  These methods
 * do comprehensive searches of the <code>IJavaProject</code> returning hits
 * in real time through an <code>IJavaElementRequestor</code>.
 *
 */
public class NameLookup implements SuffixConstants {
	// TODO (jerome) suppress the accept flags (qualified name is sufficient to find a type)
	/**
	 * Accept flag for specifying classes.
	 */
	public static final int ACCEPT_CLASSES = ASTNode.Bit2;

	/**
	 * Accept flag for specifying interfaces.
	 */
	public static final int ACCEPT_INTERFACES = ASTNode.Bit3;
	
	/**
	 * Accept flag for specifying enums.
	 */
	public static final int ACCEPT_ENUMS = ASTNode.Bit4;

	/**
	 * Accept flag for specifying annotations.
	 */
	public static final int ACCEPT_ANNOTATIONS = ASTNode.Bit5;
	
	/*
	 * Accept flag for all kinds of types
	 */
	public static final int ACCEPT_ALL = ACCEPT_CLASSES | ACCEPT_INTERFACES | ACCEPT_ENUMS | ACCEPT_ANNOTATIONS;

	public static boolean VERBOSE = false;
	
	private static final IType[] NO_TYPES = {};

	/**
	 * The <code>IPackageFragmentRoot</code>'s associated
	 * with the classpath of this NameLookup facility's
	 * project.
	 */
	protected IPackageFragmentRoot[] packageFragmentRoots;

	/**
	 * Table that maps package names to lists of package fragment roots
	 * that contain such a package known by this name lookup facility. 
	 * To allow > 1 package fragment with the same name, values are 
	 * arrays of package fragment roots ordered as they appear on the 
	 * classpath.
	 * Note if the list is of size 1, then the IPackageFragmentRoot object
	 * replaces the array.
	 */
	protected HashtableOfArrayToObject packageFragments;

	/**
	 * Reverse map from root path to corresponding resolved CP entry
	 * (so as to be able to figure inclusion/exclusion rules)
	 */
	protected Map rootToResolvedEntries;
	
	/**
	 * A map from package handles to a map from type name to an IType or an IType[].
	 * Allows working copies to take precedence over compilation units.
	 */
	protected HashMap typesInWorkingCopies;
	
	public long timeSpentInSeekTypesInSourcePackage = 0;
	public long timeSpentInSeekTypesInBinaryPackage = 0;

	public NameLookup(IPackageFragmentRoot[] packageFragmentRoots, HashtableOfArrayToObject packageFragments, ICompilationUnit[] workingCopies, Map rootToResolvedEntries) {
		long start = -1;
		if (VERBOSE) {
			System.out.println(Thread.currentThread() + " BUILDING NameLoopkup");  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> pkg roots size: " + (packageFragmentRoots == null ? 0 : packageFragmentRoots.length));  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> pkgs size: " + (packageFragments == null ? 0 : packageFragments.size()));  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> working copy size: " + (workingCopies == null ? 0 : workingCopies.length));  //$NON-NLS-1$
			start = System.currentTimeMillis();
		}
		this.packageFragmentRoots = packageFragmentRoots;
		try {
			this.packageFragments = (HashtableOfArrayToObject) packageFragments.clone();
		} catch (CloneNotSupportedException e1) {
			// ignore (implementation of HashtableOfArrayToObject supports cloning)
		}
		if (workingCopies != null) {
			this.typesInWorkingCopies = new HashMap();
			for (int i = 0, length = workingCopies.length; i < length; i++) {
				ICompilationUnit workingCopy = workingCopies[i];
				PackageFragment pkg = (PackageFragment) workingCopy.getParent();
				HashMap typeMap = (HashMap) this.typesInWorkingCopies.get(pkg);
				if (typeMap == null) {
					typeMap = new HashMap();
					this.typesInWorkingCopies.put(pkg, typeMap);
				}
				try {
					IType[] types = workingCopy.getTypes();
					int typeLength = types.length;
					if (typeLength == 0) {
						String typeName = Util.getNameWithoutJavaLikeExtension(workingCopy.getElementName());
						typeMap.put(typeName, NO_TYPES);
					} else {
						for (int j = 0; j < typeLength; j++) {
							IType type = types[j];
							String typeName = type.getElementName();
							Object existing = typeMap.get(typeName);
							if (existing == null) {
								typeMap.put(typeName, type);
							} else if (existing instanceof IType) {
								typeMap.put(typeName, new IType[] {(IType) existing, type});
							} else {
								IType[] existingTypes = (IType[]) existing;
								int existingTypeLength = existingTypes.length;
								System.arraycopy(existingTypes, 0, existingTypes = new IType[existingTypeLength+1], 0, existingTypeLength);
								existingTypes[existingTypeLength] = type;
								typeMap.put(typeName, existingTypes);
							}
						}
					}
				} catch (JavaModelException e) {
					// working copy doesn't exist -> ignore
				}
				
				// add root of package fragment to cache
				IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();
				String[] pkgName = pkg.names;
				Object existing = this.packageFragments.get(pkgName);
				if (existing == null) {
					this.packageFragments.put(pkgName, root);
				} else {
					if (existing instanceof PackageFragmentRoot) {
						if (!existing.equals(root))
							this.packageFragments.put(pkgName, new IPackageFragmentRoot[] {(PackageFragmentRoot) existing, root});
					} else {
						IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) existing;
						int rootLength = roots.length;
						boolean containsRoot = false;
						for (int j = 0; j < rootLength; j++) {
							if (roots[j].equals(root)) {
								containsRoot = true;
								break;
							}
						}
						if (containsRoot) {
							System.arraycopy(roots, 0, roots = new IPackageFragmentRoot[rootLength+1], 0, rootLength);
							roots[rootLength] = root;
							this.packageFragments.put(pkgName, roots);
						}
					}
				}
			}
		}
		this.rootToResolvedEntries = rootToResolvedEntries;
        if (VERBOSE) {
            System.out.println(Thread.currentThread() + " -> spent: " + (start - System.currentTimeMillis()) + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
        }
	}

	/**
	 * Returns true if:<ul>
	 *  <li>the given type is an existing class and the flag's <code>ACCEPT_CLASSES</code>
	 *      bit is on
	 *  <li>the given type is an existing interface and the <code>ACCEPT_INTERFACES</code>
	 *      bit is on
	 *  <li>neither the <code>ACCEPT_CLASSES</code> or <code>ACCEPT_INTERFACES</code>
	 *      bit is on
	 *  </ul>
	 * Otherwise, false is returned. 
	 */
	protected boolean acceptType(IType type, int acceptFlags, boolean isSourceType) {
		if (acceptFlags == 0 || acceptFlags == ACCEPT_ALL)
			return true; // no flags or all flags, always accepted
		try {
			int kind = isSourceType
					? TypeDeclaration.kind(((SourceTypeElementInfo) ((SourceType) type).getElementInfo()).getModifiers())
					: TypeDeclaration.kind(((IBinaryType) ((BinaryType) type).getElementInfo()).getModifiers());
			switch (kind) {
				case TypeDeclaration.CLASS_DECL :
					return (acceptFlags & ACCEPT_CLASSES) != 0;
				case TypeDeclaration.INTERFACE_DECL :
					return (acceptFlags & ACCEPT_INTERFACES) != 0;
				case TypeDeclaration.ENUM_DECL :
					return (acceptFlags & ACCEPT_ENUMS) != 0;
				default:
					//case IGenericType.ANNOTATION_TYPE :
					return (acceptFlags & ACCEPT_ANNOTATIONS) != 0;
			}
		} catch (JavaModelException npe) {
			return false; // the class is not present, do not accept.
		}
	}

	/**
	 * Finds every type in the project whose simple name matches
	 * the prefix, informing the requestor of each hit. The requestor
	 * is polled for cancellation at regular intervals.
	 *
	 * <p>The <code>partialMatch</code> argument indicates partial matches
	 * should be considered.
	 */
	private void findAllTypes(String prefix, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
		int count= this.packageFragmentRoots.length;
		for (int i= 0; i < count; i++) {
			if (requestor.isCanceled())
				return;
			IPackageFragmentRoot root= this.packageFragmentRoots[i];
			IJavaElement[] packages= null;
			try {
				packages= root.getChildren();
			} catch (JavaModelException npe) {
				continue; // the root is not present, continue;
			}
			if (packages != null) {
				for (int j= 0, packageCount= packages.length; j < packageCount; j++) {
					if (requestor.isCanceled())
						return;
					seekTypes(prefix, (IPackageFragment) packages[j], partialMatch, acceptFlags, requestor);
				}
			}
		}
	}

	/**
	 * Returns the <code>ICompilationUnit</code> which defines the type
	 * named <code>qualifiedTypeName</code>, or <code>null</code> if
	 * none exists. The domain of the search is bounded by the classpath
	 * of the <code>IJavaProject</code> this <code>NameLookup</code> was
	 * obtained from.
	 * <p>
	 * The name must be fully qualified (eg "java.lang.Object", "java.util.Hashtable$Entry")
	 */
	public ICompilationUnit findCompilationUnit(String qualifiedTypeName) {
		String[] pkgName = CharOperation.NO_STRINGS;
		String cuName = qualifiedTypeName;

		int index= qualifiedTypeName.lastIndexOf('.');
		if (index != -1) {
			pkgName= Util.splitOn('.', qualifiedTypeName, 0, index);
			cuName= qualifiedTypeName.substring(index + 1);
		}
		index= cuName.indexOf('$');
		if (index != -1) {
			cuName= cuName.substring(0, index);
		}
		Object value = this.packageFragments.get(pkgName);
		if (value != null) {
			if (value instanceof PackageFragmentRoot) {
				return findCompilationUnit(pkgName, cuName, (PackageFragmentRoot) value);
			} else {
				IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) value;
				for (int i= 0; i < roots.length; i++) {
					PackageFragmentRoot root= (PackageFragmentRoot) roots[i];
					ICompilationUnit cu = findCompilationUnit(pkgName, cuName, root);
					if (cu != null)
						return cu;
				}
			}
		}
		return null;
	}
	
	private ICompilationUnit findCompilationUnit(String[] pkgName, String cuName, PackageFragmentRoot root) {
		if (!root.isArchive()) {
			IPackageFragment pkg = root.getPackageFragment(pkgName);
			try {
				ICompilationUnit[] cus = pkg.getCompilationUnits();
				for (int j = 0, length = cus.length; j < length; j++) {
					ICompilationUnit cu = cus[j];
					if (Util.equalsIgnoreJavaLikeExtension(cu.getElementName(), cuName))
						return cu;
				}
			} catch (JavaModelException e) {
				// pkg does not exist
				// -> try next package
			}
		}
		return null;
}
	
	/**
	 * Returns the package fragment whose path matches the given
	 * (absolute) path, or <code>null</code> if none exist. The domain of
	 * the search is bounded by the classpath of the <code>IJavaProject</code>
	 * this <code>NameLookup</code> was obtained from.
	 * The path can be:
	 * 	- internal to the workbench: "/Project/src"
	 *  - external to the workbench: "c:/jdk/classes.zip/java/lang"
	 */
	public IPackageFragment findPackageFragment(IPath path) {
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Messages.path_mustBeAbsolute); 
		}
/*
 * TODO (jerome) this code should rather use the package fragment map to find the candidate package, then
 * check if the respective enclosing root maps to the one on this given IPath.
 */		
		IResource possibleFragment = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (possibleFragment == null) {
			//external jar
			for (int i = 0; i < this.packageFragmentRoots.length; i++) {
				IPackageFragmentRoot root = this.packageFragmentRoots[i];
				if (!root.isExternal()) {
					continue;
				}
				IPath rootPath = root.getPath();
				int matchingCount = rootPath.matchingFirstSegments(path);
				if (matchingCount != 0) {
					String name = path.toOSString();
					// + 1 is for the File.separatorChar
					name = name.substring(rootPath.toOSString().length() + 1, name.length());
					name = name.replace(File.separatorChar, '.');
					IJavaElement[] list = null;
					try {
						list = root.getChildren();
					} catch (JavaModelException npe) {
						continue; // the package fragment root is not present;
					}
					int elementCount = list.length;
					for (int j = 0; j < elementCount; j++) {
						IPackageFragment packageFragment = (IPackageFragment) list[j];
						if (nameMatches(name, packageFragment, false)) {
							return packageFragment;
						}
					}
				}
			}
		} else {
			IJavaElement fromFactory = JavaCore.create(possibleFragment);
			if (fromFactory == null) {
				return null;
			}
			switch (fromFactory.getElementType()) {
				case IJavaElement.PACKAGE_FRAGMENT:
					return (IPackageFragment) fromFactory;
				case IJavaElement.JAVA_PROJECT:
					// default package in a default root
					JavaProject project = (JavaProject) fromFactory;
					try {
						IClasspathEntry entry = project.getClasspathEntryFor(path);
						if (entry != null) {
							IPackageFragmentRoot root =
								project.getPackageFragmentRoot(project.getResource());
							IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) this.packageFragments.get(CharOperation.NO_STRINGS);
							if (roots == null) {
								return null;
							}
							for (int i = 0; i < roots.length; i++) {
								if (roots[i].equals(root)) {
									return  ((PackageFragmentRoot) root).getPackageFragment(CharOperation.NO_STRINGS);
								}
							}
						}
					} catch (JavaModelException e) {
						return null;
					}
					return null;
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
					return ((PackageFragmentRoot)fromFactory).getPackageFragment(CharOperation.NO_STRINGS);
			}
		}
		return null;
	}

	/**
	 * Returns the package fragments whose name matches the given
	 * (qualified) name, or <code>null</code> if none exist.
	 *
	 * The name can be:
	 *	- empty: ""
	 *	- qualified: "pack.pack1.pack2"
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *	only exact name matches qualify when <code>false</code>
	 */
	public IPackageFragment[] findPackageFragments(String name, boolean partialMatch) {
		if (partialMatch) {
			String[] splittedName = Util.splitOn('.', name, 0, name.length());
			IPackageFragment[] oneFragment = null;
			ArrayList pkgs = null;
			Object[][] keys = this.packageFragments.keyTable;
			for (int i = 0, length = keys.length; i < length; i++) {
				String[] pkgName = (String[]) keys[i];
				if (pkgName != null && Util.startsWithIgnoreCase(pkgName, splittedName)) {
					Object value = this.packageFragments.valueTable[i];
					if (value instanceof PackageFragmentRoot) {
						IPackageFragment pkg = ((PackageFragmentRoot) value).getPackageFragment(pkgName);
						if (oneFragment == null) {
							oneFragment = new IPackageFragment[] {pkg};
						} else {
							if (pkgs == null) {
								pkgs = new ArrayList();
								pkgs.add(oneFragment[0]);
							}
							pkgs.add(pkg);
						}
					} else {
						IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) value;
						for (int j = 0, length2 = roots.length; j < length2; j++) {
							PackageFragmentRoot root = (PackageFragmentRoot) roots[j];
							IPackageFragment pkg = root.getPackageFragment(pkgName);
							if (oneFragment == null) {
								oneFragment = new IPackageFragment[] {pkg};
							} else {
								if (pkgs == null) {
									pkgs = new ArrayList();
									pkgs.add(oneFragment[0]);
								}
								pkgs.add(pkg);
							}
						}
					}
				}
			}
			if (pkgs == null) return oneFragment;
			int resultLength = pkgs.size();
			IPackageFragment[] result = new IPackageFragment[resultLength];
			pkgs.toArray(result);
			return result;
		} else {
			String[] splittedName = Util.splitOn('.', name, 0, name.length());
			Object value = this.packageFragments.get(splittedName);
			if (value instanceof PackageFragmentRoot) {
				return new IPackageFragment[] {((PackageFragmentRoot) value).getPackageFragment(splittedName)};
			} else {
				IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) value;
				if (roots != null) {
					IPackageFragment[] result = new IPackageFragment[roots.length];
					for (int i= 0; i < roots.length; i++) {
						result[i] = ((PackageFragmentRoot) roots[i]).getPackageFragment(splittedName);
					}
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 */
	public IType findType(String typeName, String packageName, boolean partialMatch, int acceptFlags) {
		if (packageName == null || packageName.length() == 0) {
			packageName= IPackageFragment.DEFAULT_PACKAGE_NAME;
		} else if (typeName.length() > 0 && Character.isLowerCase(typeName.charAt(0))) {
			// see if this is a known package and not a type
			if (findPackageFragments(packageName + "." + typeName, false) != null) return null; //$NON-NLS-1$
		}
		JavaElementRequestor elementRequestor = new JavaElementRequestor();
		seekPackageFragments(packageName, false, elementRequestor);
		IPackageFragment[] packages= elementRequestor.getPackageFragments();

		for (int i= 0, length= packages.length; i < length; i++) {
			IType type= findType(typeName, packages[i], partialMatch, acceptFlags);
			if (type != null)
				return type;
		}
		return null;
	}
	
	private IType getMemberType(IType type, String name, int dot) {
		while (dot != -1) {
			int start = dot+1;
			dot = name.indexOf('.', start);
			String typeName = name.substring(start, dot == -1 ? name.length() : dot);
			type = type.getType(typeName);
		}
		return type;
	}

	/**
	 * Returns the first type in the given package whose name
	 * matches the given (unqualified) name, or <code>null</code> if none
	 * exist. Specifying a <code>null</code> package will result in no matches.
	 * The domain of the search is bounded by the Java project from which 
	 * this name lookup was obtained.
	 *
	 * @param name the name of the type to find
	 * @param pkg the package to search
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *	only exact name matches qualify when <code>false</code>
	 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
	 * 	are desired results. If no flags are specified, all types are returned.
	 *
	 * @see #ACCEPT_CLASSES
	 * @see #ACCEPT_INTERFACES
	 * @see #ACCEPT_ENUMS
	 * @see #ACCEPT_ANNOTATIONS
	 */
	public IType findType(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags) {
		if (pkg == null) return null;

		// Return first found (ignore duplicates).
		SingleTypeRequestor typeRequestor = new SingleTypeRequestor();
		seekTypes(name, pkg, partialMatch, acceptFlags, typeRequestor);
		IType type = typeRequestor.getType();
//		if (type == null)
//			type = findSecondaryType(name, pkg, partialMatch, acceptFlags);
		return type;
	}

	// TODO (kent) enable once index support is in
	IType findSecondaryType(String typeName, IPackageFragment pkg, boolean partialMatch, final int acceptFlags) {
		try {
			final ArrayList paths = new ArrayList();
			TypeNameRequestor nameRequestor = new TypeNameRequestor() {
				public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
					if (enclosingTypeNames == null || enclosingTypeNames.length == 0) { // accept only top level types
						int kind = modifiers & (ClassFileConstants.AccInterface|ClassFileConstants.AccEnum|ClassFileConstants.AccAnnotation);
						switch (kind) {
							case ClassFileConstants.AccAnnotation:
							case ClassFileConstants.AccAnnotation|ClassFileConstants.AccInterface:
								if ((acceptFlags & ACCEPT_ANNOTATIONS) != 0) paths.add(path);
								break;
							case ClassFileConstants.AccEnum:
								if ((acceptFlags & ACCEPT_ENUMS) != 0) paths.add(path);
								break;
							case ClassFileConstants.AccInterface:
								if ((acceptFlags & ACCEPT_INTERFACES) != 0) paths.add(path);
								break;
							default:
								if ((acceptFlags & ACCEPT_CLASSES) != 0) paths.add(path);
								break;
						}
					}
				}
			};

			int matchMode = partialMatch ? SearchPattern.R_PREFIX_MATCH : SearchPattern.R_EXACT_MATCH;
			int matchRule = !partialMatch ? matchMode | SearchPattern.R_CASE_SENSITIVE : matchMode;
			new SearchEngine().searchAllTypeNames(
				pkg.getElementName().toCharArray(),
				typeName.toCharArray(),
				matchRule,
				IJavaSearchConstants.TYPE,
				SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg}, false),
				nameRequestor,
				IJavaSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH,
				null);

			if (!paths.isEmpty()) {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				for (int i = 0, l = paths.size(); i < l; i++) {
					String pathname = (String) paths.get(i);
					if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(pathname)) {
						IFile file = workspace.getRoot().getFile(new Path(pathname));
						ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
						return unit.getType(typeName);
					}
				}
			}
		} catch (JavaModelException e) {
			// ignore
		} catch (OperationCanceledException ignore) {
			// ignore
		}
		return null;
	}

	/**
	 * Returns the type specified by the qualified name, or <code>null</code>
	 * if none exist. The domain of
	 * the search is bounded by the Java project from which this name lookup was obtained.
	 *
	 * @param name the name of the type to find
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *	only exact name matches qualify when <code>false</code>
	 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
	 * 	are desired results. If no flags are specified, all types are returned.
	 *
	 * @see #ACCEPT_CLASSES
	 * @see #ACCEPT_INTERFACES
	 * @see #ACCEPT_ENUMS
	 * @see #ACCEPT_ANNOTATIONS
	 */
	public IType findType(String name, boolean partialMatch, int acceptFlags) {
		int index= name.lastIndexOf('.');
		String className= null, packageName= null;
		if (index == -1) {
			packageName= IPackageFragment.DEFAULT_PACKAGE_NAME;
			className= name;
		} else {
			packageName= name.substring(0, index);
			className= name.substring(index + 1);
		}
		return findType(className, packageName, partialMatch, acceptFlags);
	}

	/**
	 * Returns true if the given element's name matches the
	 * specified <code>searchName</code>, otherwise false.
	 *
	 * <p>The <code>partialMatch</code> argument indicates partial matches
	 * should be considered.
	 * NOTE: in partialMatch mode, the case will be ignored, and the searchName must already have
	 *          been lowercased.
	 */
	protected boolean nameMatches(String searchName, IJavaElement element, boolean partialMatch) {
		if (partialMatch) {
			// partial matches are used in completion mode, thus case insensitive mode
			return element.getElementName().toLowerCase().startsWith(searchName);
		} else {
			return element.getElementName().equals(searchName);
		}
	}

	/**
	 * Returns true if the given cu's name matches the
	 * specified <code>searchName</code>, otherwise false.
	 *
	 * <p>The <code>partialMatch</code> argument indicates partial matches
	 * should be considered.
	 * NOTE: in partialMatch mode, the case will be ignored, and the searchName must already have
	 *          been lowercased.
	 */
	protected boolean nameMatches(String searchName, ICompilationUnit cu, boolean partialMatch) {
		if (partialMatch) {
			// partial matches are used in completion mode, thus case insensitive mode
			return cu.getElementName().toLowerCase().startsWith(searchName);
		} else {
			return Util.equalsIgnoreJavaLikeExtension(cu.getElementName(), searchName);
		}
	}

	/**
	 * Notifies the given requestor of all package fragments with the
	 * given name. Checks the requestor at regular intervals to see if the
	 * requestor has canceled. The domain of
	 * the search is bounded by the <code>IJavaProject</code>
	 * this <code>NameLookup</code> was obtained from.
	 *
	 * @param partialMatch partial name matches qualify when <code>true</code>;
	 *	only exact name matches qualify when <code>false</code>
	 */
	public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
/*		if (VERBOSE) {
			System.out.println(Thread.currentThread() + " SEEKING PACKAGE FRAGMENTS");  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> name: " + name);  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> partial match:" + partialMatch);  //$NON-NLS-1$
		}
*/		if (partialMatch) {
			String[] splittedName = Util.splitOn('.', name, 0, name.length());
			Object[][] keys = this.packageFragments.keyTable;
			for (int i = 0, length = keys.length; i < length; i++) {
				if (requestor.isCanceled())
					return;
				String[] pkgName = (String[]) keys[i];
				if (pkgName != null && Util.startsWithIgnoreCase(pkgName, splittedName)) {
					Object value = this.packageFragments.valueTable[i];
					if (value instanceof PackageFragmentRoot) {
						PackageFragmentRoot root = (PackageFragmentRoot) value;
						requestor.acceptPackageFragment(root.getPackageFragment(pkgName));				
					} else {
						IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) value;
						for (int j = 0, length2 = roots.length; j < length2; j++) {
							if (requestor.isCanceled())
								return;
							PackageFragmentRoot root = (PackageFragmentRoot) roots[j];
							requestor.acceptPackageFragment(root.getPackageFragment(pkgName));					
						}
					}
				}
			}
		} else {
			String[] splittedName = Util.splitOn('.', name, 0, name.length());
			Object value = this.packageFragments.get(splittedName);
			if (value instanceof PackageFragmentRoot) {
				requestor.acceptPackageFragment(((PackageFragmentRoot) value).getPackageFragment(splittedName));
			} else {
				IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) value;
				if (roots != null) {
					for (int i = 0, length = roots.length; i < length; i++) {
						if (requestor.isCanceled())
							return;
						PackageFragmentRoot root = (PackageFragmentRoot) roots[i];
						requestor.acceptPackageFragment(root.getPackageFragment(splittedName));
					}
				}
			}
		}
	}

	/**
	 * Notifies the given requestor of all types (classes and interfaces) in the
	 * given package fragment with the given (unqualified) name.
	 * Checks the requestor at regular intervals to see if the requestor
	 * has canceled. If the given package fragment is <code>null</code>, all types in the
	 * project whose simple name matches the given name are found.
	 *
	 * @param name The name to search
	 * @param pkg The corresponding package fragment
	 * @param partialMatch partial name matches qualify when <code>true</code>;
	 *	only exact name matches qualify when <code>false</code>
	 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
	 * 	are desired results. If no flags are specified, all types are returned.
	 * @param requestor The requestor that collects the result
	 *
	 * @see #ACCEPT_CLASSES
	 * @see #ACCEPT_INTERFACES
	 * @see #ACCEPT_ENUMS
	 * @see #ACCEPT_ANNOTATIONS
	 */
	public void seekTypes(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
/*		if (VERBOSE) {
			System.out.println(Thread.currentThread() + " SEEKING TYPES");  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> name: " + name);  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> pkg: " + ((JavaElement) pkg).toStringWithAncestors());  //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " -> partial match:" + partialMatch);  //$NON-NLS-1$
		}
*/
		String matchName= partialMatch ? name.toLowerCase() : name;
		if (pkg == null) {
			findAllTypes(matchName, partialMatch, acceptFlags, requestor);
			return;
		}
		IPackageFragmentRoot root= (IPackageFragmentRoot) pkg.getParent();
		try {

			// look in working copies first
			int firstDot = -1;
			String topLevelTypeName = null;
			int packageFlavor= root.getKind();
			if (this.typesInWorkingCopies != null || packageFlavor == IPackageFragmentRoot.K_SOURCE) {
				matchName= matchName.replace('$', '.');
				firstDot = matchName.indexOf('.');
				if (!partialMatch)
					topLevelTypeName = firstDot == -1 ? matchName : matchName.substring(0, firstDot);
			}
			if (this.typesInWorkingCopies != null) {
				if (seekTypesInWorkingCopies(matchName, pkg, firstDot, partialMatch, topLevelTypeName, acceptFlags, requestor))
					return;
			}
			
			// look in model
			switch (packageFlavor) {
				case IPackageFragmentRoot.K_BINARY :
					matchName= matchName.replace('.', '$');
					seekTypesInBinaryPackage(matchName, pkg, partialMatch, acceptFlags, requestor);
					break;
				case IPackageFragmentRoot.K_SOURCE :
					seekTypesInSourcePackage(matchName, pkg, firstDot, partialMatch, topLevelTypeName, acceptFlags, requestor);
					break;
				default :
					return;
			}
		} catch (JavaModelException e) {
			return;
		}
	}

	/**
	 * Performs type search in a binary package.
	 */
	protected void seekTypesInBinaryPackage(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
		long start = -1;
		if (VERBOSE)
			start = System.currentTimeMillis();
		try {
			IClassFile[] classFiles= null;
			try {
				classFiles= pkg.getClassFiles();
			} catch (JavaModelException npe) {
				return; // the package is not present
			}
			int length= classFiles.length;
			if (!partialMatch) {
				// exact match
				for (int i= 0; i < length; i++) {
					if (requestor.isCanceled()) return;
					ClassFile classFile= (ClassFile) classFiles[i];
					if (name.equals(classFile.name)) { // ClassFile#name contains the name of the .class file without the .class extension
						IType type = classFile.getType();
						if (acceptType(type, acceptFlags, false/*not a source type*/)) {
							requestor.acceptType(type);
							break;  // since an exact match was requested, no other matching type can exist
						}
					}
				}
			} else {
				String unqualifiedName = name;
				int index = name.lastIndexOf('$');
				if (index != -1) {
					//the type name of the inner type
					unqualifiedName = Util.localTypeName(name, index, name.length());
					// unqualifiedName is empty if the name ends with a '$' sign.
					// See http://dev.eclipse.org/bugs/show_bug.cgi?id=14642
				}
				String matchName = name.toLowerCase();
				for (int i = 0; i < length; i++) {
					if (requestor.isCanceled())
						return;
					IClassFile classFile= classFiles[i];
					String elementName = classFile.getElementName();
					elementName = elementName.toLowerCase();
		
					/**
					 * Must use startWith because matchName will never have the 
					 * extension ".class" and the elementName always will.
					 */
					if (elementName.startsWith(matchName)) {
						IType type= null;
						try {
							type = classFile.getType();
						} catch (JavaModelException npe) {
							continue; // the classFile is not present
						}
						if ((type.getElementName().length() > 0 && !Character.isDigit(type.getElementName().charAt(0)))) { //not an anonymous type
							if (nameMatches(unqualifiedName, type, true/*partial match*/) && acceptType(type, acceptFlags, false/*not a source type*/))
								requestor.acceptType(type);
						}
					}
				}
			}
		} finally {
			if (VERBOSE)
				this.timeSpentInSeekTypesInBinaryPackage += System.currentTimeMillis()-start;
		}
	}

	/**
	 * Performs type search in a source package.
	 */
	protected void seekTypesInSourcePackage(
			String name, 
			IPackageFragment pkg, 
			int firstDot, 
			boolean partialMatch, 
			String topLevelTypeName, 
			int acceptFlags,
			IJavaElementRequestor requestor) {
		
		long start = -1;
		if (VERBOSE)
			start = System.currentTimeMillis();
		try {
			if (!partialMatch) {
				try {
					ICompilationUnit[] compilationUnits = pkg.getCompilationUnits();
					for (int i = 0, length = compilationUnits.length; i < length; i++) {
						if (requestor.isCanceled())
							return;
						ICompilationUnit cu = compilationUnits[i];
						String cuName = cu.getElementName();
						int lastDot = cuName.lastIndexOf('.');
						if (!topLevelTypeName.equals(cuName.substring(0, lastDot))) 
							continue;
						IType type = cu.getType(topLevelTypeName);
						type = getMemberType(type, name, firstDot);
						if (acceptType(type, acceptFlags, true/*a source type*/)) { // accept type checks for existence
							requestor.acceptType(type);
							break;  // since an exact match was requested, no other matching type can exist
						}
					}
				} catch (JavaModelException e) {
					// package doesn't exist -> ignore
				}
			} else {
				try {
					String cuPrefix = firstDot == -1 ? name : name.substring(0, firstDot);
					ICompilationUnit[] compilationUnits = pkg.getCompilationUnits();
					for (int i = 0, length = compilationUnits.length; i < length; i++) {
						if (requestor.isCanceled())
							return;
						ICompilationUnit cu = compilationUnits[i];
						if (!cu.getElementName().toLowerCase().startsWith(cuPrefix))
							continue;
						try {
							IType[] types = cu.getTypes();
							for (int j = 0, typeLength = types.length; j < typeLength; j++)
								seekTypesInTopLevelType(name, firstDot, types[j], requestor, acceptFlags);
						} catch (JavaModelException e) {
							// cu doesn't exist -> ignore
						}
					}
				} catch (JavaModelException e) {
					// package doesn't exist -> ignore
				}
			}
		} finally {
			if (VERBOSE)
				this.timeSpentInSeekTypesInSourcePackage += System.currentTimeMillis()-start;
		}
	}
	
	/**
	 * Notifies the given requestor of all types (classes and interfaces) in the
	 * given type with the given (possibly qualified) name. Checks
	 * the requestor at regular intervals to see if the requestor
	 * has canceled.
	 */
	protected boolean seekTypesInType(String prefix, int firstDot, IType type, IJavaElementRequestor requestor, int acceptFlags) {
		IType[] types= null;
		try {
			types= type.getTypes();
		} catch (JavaModelException npe) {
			return false; // the enclosing type is not present
		}
		int length= types.length;
		if (length == 0) return false; 
		
		String memberPrefix = prefix;
		boolean isMemberTypePrefix = false;
		if (firstDot != -1) {
			memberPrefix= prefix.substring(0, firstDot);
			isMemberTypePrefix = true;
		}
		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return false;
			IType memberType= types[i];
			if (memberType.getElementName().toLowerCase().startsWith(memberPrefix))
				if (isMemberTypePrefix) {
					String subPrefix = prefix.substring(firstDot + 1, prefix.length());
					return seekTypesInType(subPrefix, subPrefix.indexOf('.'), memberType, requestor, acceptFlags);
				} else {
					if (acceptType(memberType, acceptFlags, true/*a source type*/)) {
						requestor.acceptMemberType(memberType);
						return true;
					}
				}
		}
		return false;
	}
	
	protected boolean seekTypesInTopLevelType(String prefix, int firstDot, IType topLevelType, IJavaElementRequestor requestor, int acceptFlags) {
		if (!topLevelType.getElementName().toLowerCase().startsWith(prefix))
			return false;
		if (firstDot == -1) {
			if (acceptType(topLevelType, acceptFlags, true/*a source type*/)) {
				requestor.acceptType(topLevelType);
				return true;
			}
		} else {
			return seekTypesInType(prefix, firstDot, topLevelType, requestor, acceptFlags);
		}
		return false;
	}
	
	/*
	 * Seeks the type with the given name in the map of types with precedence (coming from working copies)
	 * Return whether a type has been found.
	 */
	protected boolean seekTypesInWorkingCopies(
			String name, 
			IPackageFragment pkg, 
			int firstDot, 
			boolean partialMatch, 
			String topLevelTypeName, 
			int acceptFlags,
			IJavaElementRequestor requestor) {

		if (!partialMatch) {
			HashMap typeMap = (HashMap) (this.typesInWorkingCopies == null ? null : this.typesInWorkingCopies.get(pkg));
			if (typeMap != null) {
				Object object = typeMap.get(topLevelTypeName);
				if (object instanceof IType) {
					IType type = getMemberType((IType) object, name, firstDot);
					if (acceptType(type, acceptFlags, true/*a source type*/)) {
						requestor.acceptType(type);
						return true; // don't continue with compilation unit
					}
				} else if (object instanceof IType[]) {
					if (object == NO_TYPES) return true; // all types where deleted -> type is hidden
					IType[] topLevelTypes = (IType[]) object;
					for (int i = 0, length = topLevelTypes.length; i < length; i++) {
						if (requestor.isCanceled())
							return false;
						IType type = getMemberType(topLevelTypes[i], name, firstDot);
						if (acceptType(type, acceptFlags, true/*a source type*/)) {
							requestor.acceptType(type);
							return true; // return the first one
						}
					}
				}
			}
		} else {
			HashMap typeMap = (HashMap) (this.typesInWorkingCopies == null ? null : this.typesInWorkingCopies.get(pkg));
			if (typeMap != null) {
				Iterator iterator = typeMap.values().iterator();
				while (iterator.hasNext()) {
					if (requestor.isCanceled())
						return false;
					Object object = iterator.next();
					if (object instanceof IType) {
						seekTypesInTopLevelType(name, firstDot, (IType) object, requestor, acceptFlags);
					} else if (object instanceof IType[]) {
						IType[] topLevelTypes = (IType[]) object;
						for (int i = 0, length = topLevelTypes.length; i < length; i++)
							seekTypesInTopLevelType(name, firstDot, topLevelTypes[i], requestor, acceptFlags);
					}
				}
			}
		}
		return false;
	}
		
	
}
