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
 *     Tal Lev-Ami - added package cache for zip files
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.IPackageLookup;
import org.eclipse.jdt.internal.compiler.env.ITypeLookup;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment.AutoModule;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

@SuppressWarnings("rawtypes")
public class ClasspathJar extends ClasspathLocation implements IModulePathEntry {

static class PackageCacheEntry {
	long lastModified;
	long fileSize;
	SimpleSet packageSet;

	PackageCacheEntry(long lastModified, long fileSize, SimpleSet packageSet) {
		this.lastModified = lastModified;
		this.fileSize = fileSize;
		this.packageSet = packageSet;
	}
}

protected static SimpleLookupTable PackageCache = new SimpleLookupTable();
protected static SimpleLookupTable ModuleCache = new SimpleLookupTable();
INameEnvironment env = null;

protected static void addToPackageSet(SimpleSet packageSet, String fileName, boolean endsWithSep) {
	int last = endsWithSep ? fileName.length() : fileName.lastIndexOf('/');
	while (last > 0) {
		// extract the package name
		String packageName = fileName.substring(0, last);
		if (packageSet.addIfNotIncluded(packageName) == null)
			return; // already existed
		last = packageName.lastIndexOf('/');
	}
}

/**
 * Calculate and cache the package list available in the zipFile.
 * @return A SimpleSet with the all the package names in the zipFile.
 */
protected SimpleSet findPackageSet() {
	String zipFileName = this.zipFilename;
	PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
	long timestamp = this.lastModified();
	long fileSize = new File(zipFileName).length();
	if (cacheEntry != null && cacheEntry.lastModified == timestamp && cacheEntry.fileSize == fileSize) {
		this.module = (IModule) ModuleCache.get(zipFileName);
		return cacheEntry.packageSet;
	}
	final SimpleSet packageSet = new SimpleSet(41);
	packageSet.add(""); //$NON-NLS-1$
	String modInfo = readJarContent(packageSet);
	PackageCache.put(zipFileName, new PackageCacheEntry(timestamp, fileSize, packageSet));
	if (modInfo != null) {
		try {
			this.acceptModule(ClassFileReader.read(this.zipFile, modInfo));
		} catch (ClassFormatException | IOException e) {
			// TODO BETA_JAVA9 Auto-generated catch block
			e.printStackTrace();
		}
	}
	return packageSet;
}
protected String readJarContent(final SimpleSet packageSet) {
	String modInfo = null;
	for (Enumeration e = this.zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();
		if (modInfo == null) {
			int folderEnd = fileName.lastIndexOf('/');
			folderEnd += 1;
			String className = fileName.substring(folderEnd, fileName.length());
			if (className.equalsIgnoreCase(MODULE_INFO_CLASS)) {
				modInfo = fileName;
			}
		}
		addToPackageSet(packageSet, fileName, false);
	}
	return modInfo;
}
void acceptModule(ClassFileReader classfile) {
	if (classfile != null) {
		IModule mod = classfile.getModuleDeclaration();
		ModuleCache.put(this.zipFilename, mod);
		acceptModule(mod);
	}
}


String zipFilename; // keep for equals
IFile resource;
ZipFile zipFile;
ZipFile annotationZipFile;
long lastModified;
boolean closeZipFileAtEnd;
private SimpleSet knownPackageNames;
AccessRuleSet accessRuleSet;
String externalAnnotationPath;

ClasspathJar(IFile resource, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env, boolean isAutomodule) {
	this.resource = resource;
	try {
		java.net.URI location = resource.getLocationURI();
		if (location == null) {
			this.zipFilename = ""; //$NON-NLS-1$
		} else {
			File localFile = Util.toLocalFile(location, null);
			this.zipFilename = localFile.getPath();
		}
	} catch (CoreException e) {
		// ignore
	}
	this.zipFile = null;
	this.knownPackageNames = null;
	this.accessRuleSet = accessRuleSet;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	if (isAutomodule)
		setAutomaticModule();
}

ClasspathJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env, boolean isAutomodule) {
	this.zipFilename = zipFilename;
	this.lastModified = lastModified;
	this.zipFile = null;
	this.knownPackageNames = null;
	this.accessRuleSet = accessRuleSet;
	this.env = env;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	if (isAutomodule)
		setAutomaticModule();
}

public ClasspathJar(ZipFile zipFile, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env, boolean isAutomodule) {
	this(zipFile.getName(), accessRuleSet, externalAnnotationPath, env, isAutomodule);
	this.zipFile = zipFile;
	this.closeZipFileAtEnd = true;
}

public ClasspathJar(String fileName, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env, boolean isAutomodule) {
	this(fileName, 0, accessRuleSet, externalAnnotationPath, env, isAutomodule);
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toString();
	if (isAutomodule)
		setAutomaticModule();
}

void setAutomaticModule() {
	this.isAutoModule = true;
	acceptModule(new AutoModule(getFileName(this.zipFilename).toCharArray()));
}
private static String getFileName(String name) {
	int index = name.lastIndexOf('.');
	if (index != -1)
		name = name.substring(0, index);
	index = name.lastIndexOf(File.separatorChar);
	if (index == -1)
		return name;
	return name.substring(index + 1);
}
public void cleanup() {
	if (this.closeZipFileAtEnd) {
		if (this.zipFile != null) {
			try {
				this.zipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.zipFile = null;
		}
		if (this.annotationZipFile != null) {
			try {
				this.annotationZipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.annotationZipFile = null;
		}
	}
	this.knownPackageNames = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;
	ClasspathJar jar = (ClasspathJar) o;
	if (this.accessRuleSet != jar.accessRuleSet)
		if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet))
			return false;
	return this.zipFilename.equals(jar.zipFilename) 
			&& lastModified() == jar.lastModified()
			&& this.isAutoModule == jar.isAutoModule;
}

public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
	// TOOD: BETA_JAVA9 - Should really check for packages with the module context
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		IBinaryType reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
		if (reader != null) {
			char[] modName = this.module == null ? null : this.module.name();
			if (reader instanceof ClassFileReader) {
				ClassFileReader classReader = (ClassFileReader) reader;
				if (classReader.moduleName == null) {
					classReader.moduleName = modName;
				}
			}
			String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
			if (this.externalAnnotationPath != null) {
				try {
					if (this.annotationZipFile == null) {
						this.annotationZipFile = ExternalAnnotationDecorator
								.getAnnotationZipFile(this.externalAnnotationPath, null);
					}

					reader = ExternalAnnotationDecorator.create(reader, this.externalAnnotationPath,
							fileNameWithoutExtension, this.annotationZipFile);
				} catch (IOException e) {
					// don't let error on annotations fail class reading
				}
				if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.NOT_EEA_CONFIGURED) {
					// ensure a reader that answers NO_EEA_FILE
					reader = new ExternalAnnotationDecorator(reader, null);
				}
			}
			if (this.accessRuleSet == null)
				return new NameEnvironmentAnswer(reader, null);
			return new NameEnvironmentAnswer(reader, 
					this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()), 
					modName);
		}
	} catch (IOException e) { // treat as if class file is missing
	} catch (ClassFormatException e) { // treat as if class file is missing
	}
	return null;
}

public IPath getProjectRelativePath() {
	if (this.resource == null) return null;
	return	this.resource.getProjectRelativePath();
}

public int hashCode() {
	return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
}

public boolean isPackage(String qualifiedPackageName) {
	if (this.knownPackageNames != null)
		return this.knownPackageNames.includes(qualifiedPackageName);

	try {
		if (this.zipFile == null) {
			if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [ClasspathJar.isPackage(String)] Creating ZipFile on " + this.zipFilename); //$NON-NLS-1$	//$NON-NLS-2$
			}
			this.zipFile = new ZipFile(this.zipFilename);
			this.closeZipFileAtEnd = true;
			this.knownPackageNames = findPackageSet();
		} else {
			this.knownPackageNames = findPackageSet();
		}
	} catch(Exception e) {
		this.knownPackageNames = new SimpleSet(); // assume for this build the zipFile is empty
	}
	return this.knownPackageNames.includes(qualifiedPackageName);
}

public long lastModified() {
	if (this.lastModified == 0)
		this.lastModified = new File(this.zipFilename).lastModified();
	return this.lastModified;
}

public String toString() {
	String start = "Classpath jar file " + this.zipFilename; //$NON-NLS-1$
	if (this.accessRuleSet == null)
		return start;
	return start + " with " + this.accessRuleSet; //$NON-NLS-1$
}

public String debugPathString() {
	long time = lastModified();
	if (time == 0)
		return this.zipFilename;
	return this.zipFilename + '(' + (new Date(time)) + " : " + time + ')'; //$NON-NLS-1$
}

@Override
public IModule getModule() {
	//
	return this.module;
}

@Override
public IModuleEnvironment getLookupEnvironment() {
	//
	return this;
}

@Override
public ITypeLookup typeLookup() {
	// 
	return this::findClass;
}

@Override
public IPackageLookup packageLookup() {
	//
	return this::isPackage;
}

@Override
public IModuleEnvironment getLookupEnvironmentFor(IModule mod) {
	//
	return this.module == mod ? this : null;
}

@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	// 
	return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
}
}
