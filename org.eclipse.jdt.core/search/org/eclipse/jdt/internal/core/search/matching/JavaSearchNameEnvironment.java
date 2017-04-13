/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.IModuleContext;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.ClasspathJar;
import org.eclipse.jdt.internal.core.builder.ClasspathJrt;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.util.Util;

/*
 * A name environment based on the classpath of a Java project.
 */
public class JavaSearchNameEnvironment implements IModuleAwareNameEnvironment, SuffixConstants {

	LinkedHashSet<IModuleEnvironment> locationSet;
	Map<String, IModuleDescription> modules;
	private boolean modulesComputed = false;
	List<IModulePathEntry> modulePathEntries;

	/*
	 * A map from the fully qualified slash-separated name of the main type (String) to the working copy
	 */
	Map<String, org.eclipse.jdt.core.ICompilationUnit> workingCopies;

public JavaSearchNameEnvironment(IJavaProject javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
	this.modulePathEntries = new ArrayList<>();
	this.modules = new HashMap<>();
	this.locationSet = computeClasspathLocations((JavaProject) javaProject);
	this.workingCopies = getWorkingCopyMap(copies);
}

public static Map<String, org.eclipse.jdt.core.ICompilationUnit> getWorkingCopyMap(
		org.eclipse.jdt.core.ICompilationUnit[] copies) {
	int length = copies == null ? 0 : copies.length;
	HashMap<String, org.eclipse.jdt.core.ICompilationUnit> result = new HashMap<>(length);
	try {
		if (copies != null) {
			for (int i = 0; i < length; i++) {
				org.eclipse.jdt.core.ICompilationUnit workingCopy = copies[i];
				IPackageDeclaration[] pkgs = workingCopy.getPackageDeclarations();
				String pkg = pkgs.length > 0 ? pkgs[0].getElementName() : ""; //$NON-NLS-1$
				String cuName = workingCopy.getElementName();
				String mainTypeName = Util.getNameWithoutJavaLikeExtension(cuName);
				String qualifiedMainTypeName = pkg.length() == 0 ? mainTypeName : pkg.replace('.', '/') + '/' + mainTypeName;
				result.put(qualifiedMainTypeName, workingCopy);
				// TODO : JAVA 9 - module-info.java has the same name across modules - Any issues here?
			}
		}
	} catch (JavaModelException e) {
		// working copy doesn't exist: cannot happen
	}
	return result;
}

public void cleanup() {
	this.locationSet.clear();
}

void addProjectClassPath(JavaProject javaProject) {
	LinkedHashSet<IModuleEnvironment> locations = computeClasspathLocations(javaProject);
	if (locations != null) this.locationSet.addAll(locations);
}

private LinkedHashSet<IModuleEnvironment> computeClasspathLocations(JavaProject javaProject) {

	IPackageFragmentRoot[] roots = null;
	try {
		roots = javaProject.getAllPackageFragmentRoots();
	} catch (JavaModelException e) {
		return null;// project doesn't exist
	}
	LinkedHashSet<IModuleEnvironment> locations = new LinkedHashSet<>();
	int length = roots.length;
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		ClasspathLocation cp = mapToClassPathLocation(manager, (PackageFragmentRoot) roots[i]);
		if (cp != null) locations.add(cp);
	}
	return locations;
}

private void computeModules() {
	if (!this.modulesComputed) {
		this.modulesComputed = true;
		JavaElementRequestor requestor = new JavaElementRequestor();
		try {
			JavaModelManager.getModulePathManager().seekModule(CharOperation.ALL_PREFIX, true, requestor);
			IModuleDescription[] mods = requestor.getModules();
			for (IModuleDescription mod : mods) {
				this.modules.putIfAbsent(mod.getElementName(), mod);
			}
		} catch (JavaModelException e) {
			// do nothing
		}
	}
}
private ClasspathLocation mapToClassPathLocation(JavaModelManager manager, PackageFragmentRoot root) {
	ClasspathLocation cp = null;
	IPath path = root.getPath();
	IModuleDescription imd = root.getModuleDescription();
	IModule mod = NameLookup.getModuleDescriptionInfo(imd);
	this.modules.put(new String(mod.name()), imd);
	IModulePathEntry ime = null;
	try {
		if (root.isArchive()) {
			ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
			cp = JavaModelManager.isJrt(path) ? 
					new ClasspathJrt(path.toOSString(), 
							ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry, ((IJavaProject)root.getParent()).getProject(), true), this) :
						new ClasspathJar(manager.getZipFile(path), rawClasspathEntry.getAccessRuleSet(),
								ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry,
										((IJavaProject) root.getParent()).getProject(), true),
								this, rawClasspathEntry.isAutomaticModule());		
			ime = (IModulePathEntry) cp;
		} else {
			Object target = JavaModel.getTarget(path, true);
			if (target != null) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					cp = new ClasspathSourceDirectory((IContainer)target, root.fullExclusionPatternChars(), root.fullInclusionPatternChars());
					ime = (IModulePathEntry) cp;
				} else {
					ClasspathEntry rawClasspathEntry = (ClasspathEntry) root.getRawClasspathEntry();
					cp = ClasspathLocation.forBinaryFolder((IContainer) target, false, rawClasspathEntry.getAccessRuleSet(), 
							ClasspathEntry.getExternalAnnotationPath(rawClasspathEntry, ((IJavaProject)root.getParent()).getProject(), true), this, rawClasspathEntry.isAutomaticModule());
					if (cp instanceof IModulePathEntry && ((IModulePathEntry) cp).getModule() != null) {
						ime = (IModulePathEntry) cp;
					}
				}
			}
		}
		cp.setModule(mod);
		if (ime != null)
			this.modulePathEntries.add(ime);
	} catch (CoreException e1) {
		// problem opening zip file or getting root kind
		// consider root corrupt and ignore
	}
	return cp;
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, IModuleContext moduleContext) {
	if (moduleContext == IModuleContext.UNNAMED_MODULE_CONTEXT)
		return findClass(qualifiedTypeName, typeName);

	IModuleEnvironment[] envs = moduleContext.getEnvironment().toArray(IModuleEnvironment[] :: new);
	LinkedHashSet<IModuleEnvironment> cLocs = new LinkedHashSet<>();
	for  (IModuleEnvironment e : envs) {
		cLocs.add(e);
	}
	return findClass(qualifiedTypeName, typeName, cLocs);
}
private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, LinkedHashSet<IModuleEnvironment> locSets) {
	String
		binaryFileName = null, qBinaryFileName = null,
		sourceFileName = null, qSourceFileName = null,
		qPackageName = null;
	NameEnvironmentAnswer suggestedAnswer = null;
	Iterator <IModuleEnvironment> iter = locSets.iterator();
	while (iter.hasNext()) {
		IModuleEnvironment location = iter.next();
		NameEnvironmentAnswer answer = null;
		if (location instanceof ClasspathSourceDirectory) {
			ClasspathSourceDirectory csd = (ClasspathSourceDirectory) location;
			if (sourceFileName == null) {
				qSourceFileName = qualifiedTypeName; // doesn't include the file extension
				sourceFileName = qSourceFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qSourceFileName.length() - typeName.length;
					qPackageName =  qSourceFileName.substring(0, typeNameStart - 1);
					sourceFileName = qSourceFileName.substring(typeNameStart);
				}
			}
			ICompilationUnit workingCopy = (ICompilationUnit) this.workingCopies.get(qualifiedTypeName);
			if (workingCopy != null) {
				answer = new NameEnvironmentAnswer(workingCopy, null /*no access restriction*/);
			} else {
					answer = csd.findClass(
							sourceFileName, // doesn't include the file extension
							qPackageName,
							qSourceFileName);  // doesn't include the file extension
			}
		} else if (location instanceof ClasspathLocation){
			if (binaryFileName == null) {
				qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
				binaryFileName = qBinaryFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
					qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
					binaryFileName = qBinaryFileName.substring(typeNameStart);
				}
			}
			answer =
					((ClasspathLocation) location).findClass(
							binaryFileName,
							qPackageName,
							qBinaryFileName);
		} else {
			// TODO: handle anything other than ClassPathLocation, if any.
		}
		if (answer != null) {
			if (!answer.ignoreIfBetter()) {
				if (answer.isBetter(suggestedAnswer))
					return answer;
			} else if (answer.isBetter(suggestedAnswer))
				// remember suggestion and keep looking
				suggestedAnswer = answer;
		}
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	return null;
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName) {
	return findClass(qualifiedTypeName, typeName, this.locationSet);
}

@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModuleContext context) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName, context);
	return null;
}

@Override
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	return findType(typeName, 
			packageName,
			IModuleContext.UNNAMED_MODULE_CONTEXT);
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	return findType(compoundName, IModuleContext.UNNAMED_MODULE_CONTEXT);
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return isPackage(new String(CharOperation.concatWith(compoundName, packageName, '/')));
}

public boolean isPackage(String qualifiedPackageName) {
	Iterator<IModuleEnvironment> iter = this.locationSet.iterator();
	while (iter.hasNext()) {
		if (iter.next().isPackage(qualifiedPackageName)) return true;
	}
	return false;
}

@Override
public NameEnvironmentAnswer findType(char[][] compoundName, IModuleContext moduleContext) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			moduleContext);
	return null;
}

@Override
public boolean isPackage(char[][] parentPackageName, char[] packageName, IModuleContext moduleContext) {
	if (moduleContext == IModuleContext.UNNAMED_MODULE_CONTEXT)
		return isPackage(parentPackageName, packageName);
	String qualifiedPackageName = new String(CharOperation.concatWith(parentPackageName, packageName, '/'));
	Stream<IModuleEnvironment> env = moduleContext.getEnvironment();
	List<IModuleEnvironment> envs = env.collect(Collectors.toList());
	for (IModuleEnvironment e : envs) {
		if (e.isPackage(qualifiedPackageName)) 
			return true;
	}
	return false;
}

@Override
public IModule getModule(char[] moduleName) {
	computeModules();
	IModuleDescription moduleDesc = this.modules.get(new String(moduleName));
	IModule module = null;
	try {
		if (moduleDesc != null)
			module =  ((ModuleDescriptionInfo)((JavaElement) moduleDesc).getElementInfo());
	} catch (JavaModelException e) {
		// do nothing
	}
	return module;
}

@Override
public IModuleEnvironment getModuleEnvironmentFor(char[] moduleName) {
	IModule module = null;
	for (IModulePathEntry mpe : this.modulePathEntries) {
		if ((module = mpe.getModule(moduleName)) != null)
			return mpe.getLookupEnvironmentFor(module);
	}
	return null;
}

@Override
public IModule[] getAllAutomaticModules() {
	if (this.modulePathEntries == null || this.modulePathEntries.size() == 0)
		return IModule.NO_MODULES;
	Set<IModule> set = this.modulePathEntries.stream().map(e -> e.getModule()).filter(m -> m.isAutomatic())
			.collect(Collectors.toSet());
	return set.toArray(new IModule[set.size()]);
}
}
