/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class ClasspathJrt extends ClasspathLocation {

private HashMap<String, SimpleSet> packagesInModule = null;
private static HashMap<String, HashMap<String, SimpleSet>> PackageCache = new HashMap<>();
private static HashMap<String, Set<IModule>> ModulesCache = new HashMap<>();
INameEnvironment env = null;
private String externalAnnotationPath;
private ZipFile annotationZipFile;
String zipFilename; // keep for equals
public ClasspathJrt(String zipFilename, IPath externalAnnotationPath, INameEnvironment env) {
	this.zipFilename = zipFilename;
	this.env = env;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	loadModules(this);
}
/**
 * Calculate and cache the package list available in the zipFile.
 * @param jrt The ClasspathJar to use
 * @return A SimpleSet with the all the package names in the zipFile.
 */
static HashMap<String, SimpleSet> findPackagesInModules(final ClasspathJrt jrt) {
	String zipFileName = jrt.zipFilename;
	HashMap<String, SimpleSet> cache = PackageCache.get(zipFileName);
	if (cache != null) {
		return cache;
	}
	final HashMap<String, SimpleSet> packagesInModule = new HashMap<>();
	PackageCache.put(zipFileName, packagesInModule);
	try {
		final File imageFile = new File(zipFileName);
		org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(imageFile, 
				new org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor<Path>() {
			SimpleSet packageSet = null;
			@Override
			public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs) throws IOException {
				ClasspathJar.addToPackageSet(this.packageSet, dir.toString(), true);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, Path mod, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitModule(Path mod) throws IOException {
				String name = mod.toString();
				try {
					jrt.acceptModule(JRTUtil.getClassfileContent(imageFile, MODULE_INFO_CLASS, name));
				} catch (ClassFormatException e) {
					e.printStackTrace();
				}
				this.packageSet = new SimpleSet(41);
				this.packageSet.add(""); //$NON-NLS-1$
				packagesInModule.put(name, this.packageSet);
				return FileVisitResult.CONTINUE;
			}
		}, JRTUtil.NOTIFY_PACKAGES | JRTUtil.NOTIFY_MODULES);
	} catch (IOException e) {
		// TODO: BETA_JAVA9 Should report better
	}
	return packagesInModule;
}

public static void loadModules(final ClasspathJrt jrt) {
	String zipFileName = jrt.zipFilename;
	Set<IModule> cache = ModulesCache.get(zipFileName);

	if (cache == null) {
		try {
			final File imageFile = new File(zipFileName);
			org.eclipse.jdt.internal.compiler.util.JRTUtil.walkModuleImage(imageFile,
					new org.eclipse.jdt.internal.compiler.util.JRTUtil.JrtFileVisitor<Path>() {
				SimpleSet packageSet = null;

				@Override
				public FileVisitResult visitPackage(Path dir, Path mod, BasicFileAttributes attrs)
						throws IOException {
					ClasspathJar.addToPackageSet(this.packageSet, dir.toString(), true);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, Path mod, BasicFileAttributes attrs)
						throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitModule(Path mod) throws IOException {
					try {
						jrt.acceptModule(JRTUtil.getClassfileContent(imageFile, MODULE_INFO_CLASS, mod.toString()));
					} catch (ClassFormatException e) {
						e.printStackTrace();
					}
					return FileVisitResult.SKIP_SUBTREE;
				}
			}, JRTUtil.NOTIFY_MODULES);
		} catch (IOException e) {
			// TODO: BETA_JAVA9 Should report better
		}
	} else {
//		for (IModule iModule : cache) {
//			jimage.env.acceptModule(iModule, jimage);
//		}
	}
}
void acceptModule(byte[] content) {
	if (content == null) 
		return;
	ClassFileReader reader = null;
	try {
		reader = new ClassFileReader(content, MODULE_INFO_CLASS.toCharArray(), null);
	} catch (ClassFormatException e) {
		e.printStackTrace();
	}
	if (reader != null) {
		IModule moduleDecl = reader.getModuleDeclaration();
		if (moduleDecl != null) {
			Set<IModule> cache = ModulesCache.get(this.zipFilename);
			if (cache == null) {
				ModulesCache.put(this.zipFilename, cache = new HashSet<IModule>());
			}
			cache.add(moduleDecl);
			//this.env.acceptModule(moduleDecl, this);
		}
	}
}
public void cleanup() {
	if (this.annotationZipFile != null) {
		try {
			this.annotationZipFile.close();
		} catch(IOException e) { // ignore it
		}
		this.annotationZipFile = null;
	}
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJrt)) return false;
	ClasspathJrt jar = (ClasspathJrt) o;
	return this.zipFilename.endsWith(jar.zipFilename);
}

public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, IModule mod) {
	return findClass(binaryFileName, qualifiedPackageName, qualifiedBinaryFileName, mod);
}
public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName, IModule mod) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.readFromJrt(new File(this.zipFilename), qualifiedBinaryFileName, mod);
		if (reader != null) {
			if (this.externalAnnotationPath != null) {
				String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
				try {
					this.annotationZipFile = reader.setExternalAnnotationProvider(this.externalAnnotationPath, fileNameWithoutExtension, this.annotationZipFile, null);
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
			}
			return new NameEnvironmentAnswer(reader, null);
		}
	} catch (IOException e) { // treat as if class file is missing
	} catch (ClassFormatException e) { // treat as if class file is missing
	}
	return null;
}

public IPath getProjectRelativePath() {
	return null;
}

public int hashCode() {
	return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
}

public boolean isPackage(String qualifiedPackageName) {
	try {
		synchronized (this) {
			if (this.packagesInModule == null) {
				this.packagesInModule = findPackagesInModules(this);
			}
		}
	} catch(Exception e) {
		// TODO BETA_JAVA9
	}
	Set<String> keySet = this.packagesInModule.keySet();
	for (String string : keySet) {
		SimpleSet set = this.packagesInModule.get(string);
		if (set.includes(qualifiedPackageName)) return true;
	}

	return false;
}

public String toString() {
	String start = "Classpath jrt file " + this.zipFilename; //$NON-NLS-1$
	return start;
}

public String debugPathString() {
	return this.zipFilename;
}

@Override
public boolean servesModule(IModule mod) {
	if (mod == null) 
		return false;
	synchronized (this) {
		if (this.packagesInModule == null) {
			this.packagesInModule = findPackagesInModules(this);
		}
	}
	if (mod == ModuleEnvironment.UNNAMED_MODULE)
		return true;
	if (this.packagesInModule.containsKey(new String(mod.name()))) {
		return true;
	}
	return false;
}
@Override
public IModule getModule(char[] moduleName) {
	// 
	Set<IModule> modules = ModulesCache.get(this.zipFilename);
	if (modules != null) {
		for (IModule mod : modules) {
			if (CharOperation.equals(mod.name(), moduleName))
					return mod;
		}
	}
	return null;
}
}
