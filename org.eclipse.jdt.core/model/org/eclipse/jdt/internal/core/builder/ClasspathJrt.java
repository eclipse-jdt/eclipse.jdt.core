/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.IMultiModuleEntry;
import org.eclipse.jdt.internal.compiler.env.IMultiModulePackageLookup;
import org.eclipse.jdt.internal.compiler.env.IMultiModuleTypeLookup;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.env.IPackageLookup;
import org.eclipse.jdt.internal.compiler.env.ITypeLookup;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class ClasspathJrt extends ClasspathLocation implements IMultiModuleEntry {

//private HashMap<String, SimpleSet> packagesInModule = null;
private static HashMap<String, HashMap<String, SimpleSet>> PackageCache = new HashMap<>();
private static HashMap<String, Set<IModule>> ModulesCache = new HashMap<>();
INameEnvironment env = null;
private String externalAnnotationPath;
private ZipFile annotationZipFile;
String zipFilename; // keep for equals

protected Function<char[], ITypeLookup> typeLookupForModule = modName -> 
(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly) -> {
return typeLookup().findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly, modName);
};

protected Function<char[], IPackageLookup> pkgLookupForModule = modName -> 
qualifiedPackageName -> {
return packageLookup().isPackage(qualifiedPackageName, modName);
};

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
					jrt.acceptModule(JRTUtil.getClassfileContent(imageFile, IModuleEnvironment.MODULE_INFO_CLASS, name));
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
						jrt.acceptModule(JRTUtil.getClassfileContent(imageFile, IModuleEnvironment.MODULE_INFO_CLASS, mod.toString()));
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
//		for (IModuleDeclaration iModule : cache) {
//			jimage.env.acceptModule(iModule, jimage);
//		}
	}
}
void acceptModule(byte[] content) {
	if (content == null) 
		return;
	ClassFileReader reader = null;
	try {
		reader = new ClassFileReader(content, IModuleEnvironment.MODULE_INFO_CLASS.toCharArray(), null);
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

private NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, Optional<Collection<char[]>> moduleNames) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		IBinaryType reader = ClassFileReader.readFromModules(new File(this.zipFilename), qualifiedBinaryFileName, moduleNames);
		if (reader != null) {
			if (this.externalAnnotationPath != null) {
				String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
				try {
					if (this.annotationZipFile == null) {
						this.annotationZipFile = ExternalAnnotationDecorator.getAnnotationZipFile(this.externalAnnotationPath, null);
					}
					reader = ExternalAnnotationDecorator.create(reader, this.externalAnnotationPath, fileNameWithoutExtension, this.annotationZipFile);
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
			}
			return new NameEnvironmentAnswer(reader, null, reader.getModule());
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
@Override
public boolean isPackage(String qualifiedPackageName) {
	//
	return isPackage(qualifiedPackageName, Optional.empty());
}
public boolean isPackage(String qualifiedPackageName, Optional<char[]> moduleName) {
//	try {
//		synchronized (this) {
//			if (this.packagesInModule == null) {
//				this.packagesInModule = findPackagesInModules(this);
//			}
//		}
//	} catch(Exception e) {
//		// TODO BETA_JAVA9
//	}
//	if (moduleName.isPresent()) {
//		SimpleSet set = this.packagesInModule.get(moduleName.get());
//		return set != null && set.includes(qualifiedPackageName);
//	}
//	Set<String> keySet = this.packagesInModule.keySet();
//	for (String string : keySet) {
//		SimpleSet set = this.packagesInModule.get(string);
//		if (set.includes(qualifiedPackageName)) return true;
//	}
//
//	return false;
	return JRTUtil.isPackage(new File(this.zipFilename), qualifiedPackageName, moduleName);
}

public String toString() {
	String start = "Classpath jrt file " + this.zipFilename; //$NON-NLS-1$
	return start;
}

public String debugPathString() {
	return this.zipFilename;
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, Optional<Collection<char[]>> moduleNames) {
	String fileName = new String(typeName);
	return findClass(fileName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly, Optional.empty());
}
@Override
public IModule getModule(char[] moduleName) {
	Set<IModule> modules = ModulesCache.get(this.zipFilename);
	if (modules != null) {
		for (IModule mod : modules) {
			if (CharOperation.equals(mod.name(), moduleName))
					return mod;
		}
	}
	return null;
}
@Override
public IMultiModuleTypeLookup typeLookup() {
	return this::findClass;
}
@Override
public IMultiModulePackageLookup packageLookup() {
	return this::isPackage;
}

@Override
public IModuleEnvironment getLookupEnvironment() {
	//
	return this;
}
@Override
public IModuleEnvironment getLookupEnvironmentFor(IModule mod) {
	// 
	return new IModuleEnvironment() {
		
		@Override
		public ITypeLookup typeLookup() {
			//
			return servesModule(mod.name()) ? ClasspathJrt.this.typeLookupForModule.apply(mod.name()) : ITypeLookup.Dummy;
		}
		
		@Override
		public IPackageLookup packageLookup() {
			//
			return servesModule(mod.name()) ? ClasspathJrt.this.pkgLookupForModule.apply(mod.name()) : IPackageLookup.Dummy;
		}
	};
}
@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	// 
	return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false, Optional.empty());
}
@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName,
		boolean asBinaryOnly) {
	//
	return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly, Optional.empty());
}

}
