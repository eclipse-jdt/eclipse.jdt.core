package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.core.resources.*;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;

import org.eclipse.jdt.core.*;

/**
 *  This class implements basic namelookup functionality as
 *  described in <code>INameLookup</code>.  It performs its
 *  searches by querying the Java Model directly.
 *
 * @see INameLookup
 */

public class NameLookup implements INameLookup {
	/**
	 * The <code>IPackageFragmentRoot</code>'s associated
	 * with the classpath of this NameLookup facility's
	 * project.
	 */
	protected IPackageFragmentRoot[] fPackageFragmentRoots= null;
	/**
	 * Table that maps package names to lists of package fragments for
	 * all package fragments in the package fragment roots known
	 * by this name lookup facility. To allow > 1 package fragment
	 * with the same name, values are arrays of package fragments
	 * ordered as they appear on the classpath.
	 */
	protected Hashtable fPackageFragments;

	/**
	 * Singleton <code>SingleTypeRequestor</code>.
	 * @see findType(String, IPackageFragment, boolean, int)
	 */
	protected SingleTypeRequestor fgSingleTypeRequestor= new SingleTypeRequestor();
	/**
	 * Singleton <code>JavaElementRequestor</code>.
	 * @see findType(String, boolean, int)
	 */
	protected JavaElementRequestor fgJavaElementRequestor= new JavaElementRequestor();
	/**
	 * The <code>IWorkspace</code> that this NameLookup
	 * is configure within.
	 */
	protected IWorkspace workspace;

	public NameLookup(IJavaProject project) throws JavaModelException {
		configureFromProject(project);
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
	protected boolean acceptType(IType type, int acceptFlags) {
		if (acceptFlags == 0)
			return true; // no flags, always accepted
		try {
			if (type.isClass()) {
				return (acceptFlags & ACCEPT_CLASSES) != 0;
			} else {
				return (acceptFlags & ACCEPT_INTERFACES) != 0;
			}
		} catch (JavaModelException npe) {
			return false; // the class is not present, do not accept.
		}
	}
	/**
	 * Configures this <code>NameLookup</code> based on the
	 * info of the given <code>IJavaProject</code>.
	 *
	 * @throws JavaModelException if the <code>IJavaProject</code> has no classpath.
	 */
	private void configureFromProject(IJavaProject project) throws JavaModelException {
		workspace= project.getJavaModel().getWorkspace();
		fPackageFragmentRoots= ((JavaProject) project).getAllPackageFragmentRoots();
		fPackageFragments= new Hashtable();
		IPackageFragment[] frags= ((JavaProject) project).getAllPackageFragments();
		for (int i= 0; i < frags.length; i++) {
			IPackageFragment fragment= frags[i];
			IPackageFragment[] entry= (IPackageFragment[]) fPackageFragments.get(fragment.getElementName());
			if (entry == null) {
				entry= new IPackageFragment[1];
				entry[0]= fragment;
				fPackageFragments.put(fragment.getElementName(), entry);
			} else {
				IPackageFragment[] copy= new IPackageFragment[entry.length + 1];
				System.arraycopy(entry, 0, copy, 0, entry.length);
				copy[entry.length]= fragment;
				fPackageFragments.put(fragment.getElementName(), copy);
			}
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
		int count= fPackageFragmentRoots.length;
		for (int i= 0; i < count; i++) {
			if (requestor.isCanceled())
				return;
			IPackageFragmentRoot root= fPackageFragmentRoots[i];
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
	 * @see INameLookup
	 */
	public ICompilationUnit findCompilationUnit(String qualifiedTypeName) {
		String pkgName= IPackageFragment.DEFAULT_PACKAGE_NAME;
		String cuName= qualifiedTypeName;

		int index= qualifiedTypeName.lastIndexOf('.');
		if (index != -1) {
			pkgName= qualifiedTypeName.substring(0, index);
			cuName= qualifiedTypeName.substring(index + 1);
		}

		index= cuName.indexOf('$');
		if (index != -1) {
			cuName= cuName.substring(0, index);
		}
		cuName += ".java"/*nonNLS*/;

		IPackageFragment[] frags= (IPackageFragment[]) fPackageFragments.get(pkgName);
		if (frags != null) {
			for (int i= 0; i < frags.length; i++) {
				IPackageFragment frag= frags[i];
				if (!(frag instanceof JarPackageFragment)) {
					ICompilationUnit cu= frag.getCompilationUnit(cuName);
					if (cu != null && cu.exists()) {
						return cu;
					}
				}
			}
		}
		return null;
	}
/**
 * @see INameLookup
 */
public IPackageFragment findPackageFragment(IPath path) {
	if (!path.isAbsolute()) {
		throw new IllegalArgumentException(Util.bind("path.mustBeAbsolute"/*nonNLS*/));
	}
	IResource possibleFragment = workspace.getRoot().findMember(path);
	if (possibleFragment == null) {
		//external jar
		for (int i = 0; i < fPackageFragmentRoots.length; i++) {
			IPackageFragmentRoot root = fPackageFragmentRoots[i];
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
						if (packageFragment.exists())
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
		if (fromFactory instanceof IPackageFragment) {
			return (IPackageFragment) fromFactory;
		} else
			if (fromFactory instanceof IJavaProject) {
				// default package in a default root
				JavaProject project = (JavaProject) fromFactory;
				try {
					IClasspathEntry entry = project.getClasspathEntryFor(path);
					if (entry != null) {
						IPackageFragmentRoot root =
							project.getPackageFragmentRoot(project.getUnderlyingResource());
						IPackageFragment[] pkgs = (IPackageFragment[]) fPackageFragments.get(IPackageFragment.DEFAULT_PACKAGE_NAME);
						if (pkgs == null) {
							return null;
						}
						for (int i = 0; i < pkgs.length; i++) {
							if (pkgs[i].getParent().equals(root)) {
								return pkgs[i];
							}
						}
					}
				} catch (JavaModelException e) {
					return null;
				}
			}
	}
	return null;
}
	/**
	 * @see INameLookup
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path) {
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Util.bind("path.mustBeAbsolute"/*nonNLS*/));
		}
		for (int i= 0; i < fPackageFragmentRoots.length; i++) {
			IPackageFragmentRoot classpathRoot= fPackageFragmentRoots[i];
			if (classpathRoot.getPath().equals(path)) {
				return classpathRoot;
			}
		}
		return null;
	}
	/**
	 * @see INameLookup
	 */
	public IPackageFragment[] findPackageFragments(String name, boolean partialMatch) {
		int count= fPackageFragmentRoots.length;
		if (partialMatch) {
			name= name.toLowerCase();
			for (int i= 0; i < count; i++) {
				IPackageFragmentRoot root= fPackageFragmentRoots[i];
				IJavaElement[] list= null;
				try {
					list= root.getChildren();
				} catch (JavaModelException npe) {
					continue; // the package fragment root is not present;
				}
				int elementCount= list.length;
				IPackageFragment[] result = new IPackageFragment[elementCount];
				int resultLength = 0; 
				for (int j= 0; j < elementCount; j++) {
					IPackageFragment packageFragment= (IPackageFragment) list[j];
					if (nameMatches(name, packageFragment, true)) {
						if (packageFragment.exists())
							result[resultLength++] = packageFragment;
					}
				}
				if (resultLength > 0) {
					System.arraycopy(result, 0, result = new IPackageFragment[resultLength], 0, resultLength);
					return result;
				} else {
					return null;
				}
			}
		} else {
			// Return only fragments that exists
			IPackageFragment[] fragments= (IPackageFragment[]) fPackageFragments.get(name);
			if (fragments != null) {
				IPackageFragment[] result = new IPackageFragment[fragments.length];
				int resultLength = 0; 
				for (int i= 0; i < fragments.length; i++) {
					IPackageFragment packageFragment= fragments[i];
					if (packageFragment.exists())
						result[resultLength++] = packageFragment;
				}
				if (resultLength > 0) {
					System.arraycopy(result, 0, result = new IPackageFragment[resultLength], 0, resultLength);
					return result;
				} else {
					return null;
				}
			}
		}
		return null;
	}
	/**
	 * 
	 */
	public IType findType(String typeName, String packageName, boolean partialMatch, int acceptFlags) {
		if (packageName == null) {
			packageName= IPackageFragment.DEFAULT_PACKAGE_NAME;
		}

		seekPackageFragments(packageName, false, fgJavaElementRequestor);
		IPackageFragment[] packages= fgJavaElementRequestor.getPackageFragments();
		fgJavaElementRequestor.reset();
		for (int i= 0, length= packages.length; i < length; i++) {
			IType type= findType(typeName, packages[i], partialMatch, acceptFlags);
			if (type != null)
				return type;
		}
		return null;
	}
	/**
	 * @see INameLookup
	 */
	public IType findType(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags) {
		if (pkg == null) {
			return null;
		}
		// Return first found (ignore duplicates).
		seekTypes(name, pkg, partialMatch, acceptFlags, fgSingleTypeRequestor);
		IType type= fgSingleTypeRequestor.getType();
		fgSingleTypeRequestor.reset();
		return type;
	}
	/**
	 * @see INameLookup
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
	 * @see INameLookup
	 */
	public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor) {
		int count= fPackageFragmentRoots.length;
		String matchName= partialMatch ? name.toLowerCase() : name;
		for (int i= 0; i < count; i++) {
			if (requestor.isCanceled())
				return;
			IPackageFragmentRoot root= fPackageFragmentRoots[i];
			IJavaElement[] list= null;
			try {
				list= root.getChildren();
			} catch (JavaModelException npe) {
				continue; // this root package fragment is not present
			}
			int elementCount= list.length;
			for (int j= 0; j < elementCount; j++) {
				if (requestor.isCanceled())
					return;
				IPackageFragment packageFragment= (IPackageFragment) list[j];
				if (nameMatches(matchName, packageFragment, partialMatch))
					requestor.acceptPackageFragment(packageFragment);
			}
		}
	}
	/**
	 * Notifies the given requestor of all types (classes and interfaces) in the
	 * given type with the given (possibly qualified) name. Checks
	 * the requestor at regular intervals to see if the requestor
	 * has canceled.
	 *
	 * @param partialMatch partial name matches qualify when <code>true</code>,
	 *  only exact name matches qualify when <code>false</code>
	 */
	protected void seekQualifiedMemberTypes(String qualifiedName, IType type, boolean partialMatch, IJavaElementRequestor requestor) {
		if (type == null)
			return;
		IType[] types= null;
		try {
			types= type.getTypes();
		} catch (JavaModelException npe) {
			return; // the enclosing type is not present
		}
		String matchName= qualifiedName;
		int index= qualifiedName.indexOf('$');
		boolean nested= false;
		if (index != -1) {
			matchName= qualifiedName.substring(0, index);
			nested= true;
		}
		int length= types.length;
		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return;
			IType memberType= types[i];
			if (nameMatches(matchName, memberType, partialMatch))
				if (nested) {
					seekQualifiedMemberTypes(qualifiedName.substring(index + 1, qualifiedName.length()), memberType, partialMatch, requestor);
				} else {
					requestor.acceptMemberType(memberType);
				}
		}
	}
	/**
	 * @see INameLookup
	 */
	public void seekTypes(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {

		String matchName= partialMatch ? name.toLowerCase() : name;
		if (matchName.indexOf('.') >= 0) { //looks for member type A.B
			matchName= matchName.replace('.', '$');
		}
		if (pkg == null) {
			findAllTypes(matchName, partialMatch, acceptFlags, requestor);
			return;
		}
		IPackageFragmentRoot root= (IPackageFragmentRoot) pkg.getParent();
		try {
			int packageFlavor= root.getKind();
			switch (packageFlavor) {
				case IPackageFragmentRoot.K_BINARY :
					seekTypesInBinaryPackage(matchName, pkg, partialMatch, acceptFlags, requestor);
					break;
				case IPackageFragmentRoot.K_SOURCE :
					seekTypesInSourcePackage(matchName, pkg, partialMatch, acceptFlags, requestor);
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
		IClassFile[] classFiles= null;
		try {
			classFiles= pkg.getClassFiles();
		} catch (JavaModelException npe) {
			return; // the package is not present
		}
		int length= classFiles.length;

		String unqualifiedName= name;
		int index= name.lastIndexOf('$');
		if (index != -1) {
			//the type name of the inner type
			unqualifiedName= name.substring(index + 1, name.length());
		}
		String lowerName= name.toLowerCase();
		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return;
			IClassFile classFile= classFiles[i];
			/**
			 * In the following call to nameMatches we must always send true 
			 * for the partialMatch argument since name will never have the 
			 * extension ".class" and the classFile always will.
			 */
			if (nameMatches(lowerName, classFile, true)) {
				IType type= null;
				try {
					type= classFile.getType();
				} catch (JavaModelException npe) {
					continue; // the classFile is not present
				}
				if (!partialMatch || (type.getElementName().length() > 0 && !Character.isDigit(type.getElementName().charAt(0)))) { //not an anonymous type
					if (nameMatches(unqualifiedName, type, partialMatch) && acceptType(type, acceptFlags))
						requestor.acceptType(type);
				}
			}
		}
	}
	/**
	 * Performs type search in a source package.
	 */
	protected void seekTypesInSourcePackage(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
		ICompilationUnit[] compilationUnits= null;
		try {
			compilationUnits= pkg.getCompilationUnits();
		} catch (JavaModelException npe) {
			return; // the package is not present
		}
		int length= compilationUnits.length;
		String matchName= name;
		int index= name.indexOf('$');
		boolean memberType= false;
		if (index != -1) {
			//the compilation unit name of the inner type
			matchName= name.substring(0, index);
			memberType= true;
		}

		/**
		 * In the following, matchName will never have the extension ".java" and 
		 * the compilationUnits always will. So add it if we're looking for 
		 * an exact match.
		 */
		String unitName= partialMatch ? matchName.toLowerCase() : matchName + ".java"/*nonNLS*/;

		for (int i= 0; i < length; i++) {
			if (requestor.isCanceled())
				return;
			ICompilationUnit compilationUnit= compilationUnits[i];

			if (nameMatches(unitName, compilationUnit, partialMatch)) {
				IType[] types= null;
				try {
					types= compilationUnit.getTypes();
				} catch (JavaModelException npe) {
					continue; // the compilation unit is not present
				}
				int typeLength= types.length;
				for (int j= 0; j < typeLength; j++) {
					if (requestor.isCanceled())
						return;
					IType type= types[j];
					if (nameMatches(matchName, type, partialMatch) && acceptType(type, acceptFlags))
						if (!memberType) {
							requestor.acceptType(type);
						} else {
							seekQualifiedMemberTypes(name.substring(index + 1, name.length()), type, partialMatch, requestor);
						}
				}
			}
		}
	}
}
