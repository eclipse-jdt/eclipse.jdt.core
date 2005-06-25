package org.eclipse.jdt.apt.core.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

/**
 * Utility class for dealing with packages, using
 * Eclipse's underlying SearchEngine
 */
public class PackageUtil {
	
	private PackageUtil() {}
	
	public static IPackageFragment[] getPackageFragments(
			final String packageName, 
			final ProcessorEnvImpl env) {
		
		IJavaProject project = env.getJavaProject();
		List<IPackageFragment> packages = new ArrayList<IPackageFragment>();
		try {
			IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				IPackageFragment fragment = root.getPackageFragment(packageName);
				if (fragment != null)
					packages.add(fragment);
			}
		}
		catch (JavaModelException e) {
			return new IPackageFragment[0];
		}
		
		return packages.toArray(new IPackageFragment[packages.size()]);
	}
	
	public static Collection<ClassDeclaration> getClasses(
			final PackageDeclaration pkg,
			final ProcessorEnvImpl env) {
		
		List<IType> types = getTypesInPackage(pkg.getQualifiedName(), env);
		List<ClassDeclaration> classes = new ArrayList<ClassDeclaration>();
		for (IType type : types) {
			try {
				// isClass() will return true if TypeDeclaration is an InterfaceDeclaration
				if (type.isClass() ) {
					TypeDeclaration td = env.getTypeDeclaration( type );
					if ( td instanceof ClassDeclaration ) {				
						classes.add((ClassDeclaration)td);
					}
				}
			}
			catch (JavaModelException ex) {} // No longer exists, don't return it
		}
		
		return classes;
	}
	
	public static Collection<EnumDeclaration> getEnums(
			final PackageDeclaration pkg,
			final ProcessorEnvImpl env) {
		
		List<IType> types = getTypesInPackage(pkg.getQualifiedName(), env);
		List<EnumDeclaration> enums = new ArrayList<EnumDeclaration>();
		for (IType type : types) {
			try {
				if (type.isEnum()) {
					enums.add((EnumDeclaration)env.getTypeDeclaration(type));
				}
			}
			catch (JavaModelException ex) {} // No longer exists, don't return it
		}
		
		return enums;
		
	}
	
	public static Collection<InterfaceDeclaration> getInterfaces(
			final PackageDeclaration pkg,
			final ProcessorEnvImpl env) {
		
		List<IType> types = getTypesInPackage(pkg.getQualifiedName(), env);
		List<InterfaceDeclaration> interfaces = new ArrayList<InterfaceDeclaration>();
		for (IType type : types) {
			try {
				if (type.isInterface()) {
					interfaces.add((InterfaceDeclaration)env.getTypeDeclaration(type));
				}
			}
			catch (JavaModelException ex) {} // No longer exists, don't return it
		}
		
		return interfaces;
	}
	
	private static List<IType> getTypesInPackage(
			final String packageName, 
			final ProcessorEnvImpl env) {
		
		if (packageName == null)
			throw new IllegalArgumentException("packageName cannot be null");
		if (env == null)
			throw new IllegalArgumentException("env cannot be null");
		
		final IJavaProject project = env.getJavaProject();
		
		final List<IType> types = new ArrayList<IType>();
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		SearchPattern pattern = SearchPattern.createPattern(
				packageName + ".*", 
				IJavaSearchConstants.TYPE, 
				IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_PATTERN_MATCH);
		assert pattern != null : "Failed to create a SearchPattern for the following package name: " + packageName;
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				if ( match.getElement() instanceof IType ) {
					IType type = (IType)match.getElement();
					types.add(type);
				}
			}
		};
		
		SearchEngine engine = new SearchEngine();
		try {
			engine.search(
				pattern, 
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope,
				requestor,
				null);
		}
		catch (CoreException ce) {
			// We'll return the empty array later
		}
		return types;
	}

}
