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
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.IPackageLookup;
import org.eclipse.jdt.internal.compiler.env.ITypeLookup;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment.AutoModule;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;


public class ClasspathDirectory extends ClasspathLocation {

IContainer binaryFolder; // includes .class files for a single directory
boolean isOutputFolder;
SimpleLookupTable directoryCache;
String[] missingPackageHolder = new String[1];
AccessRuleSet accessRuleSet;
ZipFile annotationZipFile;
String externalAnnotationPath;
INameEnvironment env;

ClasspathDirectory(IContainer binaryFolder, boolean isOutputFolder, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env, boolean isAutomodule) {
	this.binaryFolder = binaryFolder;
	this.isOutputFolder = isOutputFolder || binaryFolder.getProjectRelativePath().isEmpty(); // if binaryFolder == project, then treat it as an outputFolder
	this.directoryCache = new SimpleLookupTable(5);
	this.accessRuleSet = accessRuleSet;
	this.env = env;
	if (externalAnnotationPath != null)
		this.externalAnnotationPath = externalAnnotationPath.toOSString();
	if (isAutomodule) {
		setAutomaticModule();
	}
}

void setAutomaticModule() {
	this.isAutoModule = true;
	acceptModule(new AutoModule(this.binaryFolder.getName().toCharArray()));
}

public void cleanup() {
	if (this.annotationZipFile != null) {
		try {
			this.annotationZipFile.close();
		} catch(IOException e) { // ignore it
		}
		this.annotationZipFile = null;
	}
	this.directoryCache = null;
}

ClasspathDirectory initializeModule() {
	IResource[] members = null;
	try {
		members = this.binaryFolder.members();
		if (members != null) {
			for (int i = 0, l = members.length; i < l; i++) {
				IResource m = members[i];
				String name = m.getName();
				// Note: Look only inside the default package.
				if (m.getType() == IResource.FILE && org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name)) {
					if (name.equalsIgnoreCase(IModuleEnvironment.MODULE_INFO_CLASS)) {
						try {
							this.acceptModule( Util.newClassFileReader(m));
							this.isAutoModule = false;
						} catch (ClassFormatException | IOException e) {
							// TODO BETA_JAVA9 Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	} catch (CoreException e1) {
		e1.printStackTrace();
	}
	return this;
}
String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) this.directoryCache.get(qualifiedPackageName);
	if (dirList == this.missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	try {
		IResource container = this.binaryFolder.findMember(qualifiedPackageName); // this is a case-sensitive check
		if (container instanceof IContainer) {
			IResource[] members = ((IContainer) container).members();
			dirList = new String[members.length];
			int index = 0;
			for (int i = 0, l = members.length; i < l; i++) {
				IResource m = members[i];
				String name = m.getName();
				if (m.getType() == IResource.FILE && org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name)) {
					// add exclusion pattern check here if we want to hide .class files
					dirList[index++] = name;
				}
			}
			if (index < dirList.length)
				System.arraycopy(dirList, 0, dirList = new String[index], 0, index);
			this.directoryCache.put(qualifiedPackageName, dirList);
			return dirList;
		}
	} catch(CoreException ignored) {
		// ignore
	}
	this.directoryCache.put(qualifiedPackageName, this.missingPackageHolder);
	return null;
}
void acceptModule(ClassFileReader classfile) {
	if (classfile != null) {
		acceptModule(classfile.getModuleDeclaration());
	}
}
boolean doesFileExist(String fileName, String qualifiedPackageName, String qualifiedFullName) {
	String[] dirList = directoryList(qualifiedPackageName);
	if (dirList == null) return false; // most common case

	for (int i = dirList.length; --i >= 0;)
		if (fileName.equals(dirList[i]))
			return true;
	return false;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathDirectory)) return false;

	ClasspathDirectory dir = (ClasspathDirectory) o;
	if (this.accessRuleSet != dir.accessRuleSet)
		if (this.accessRuleSet == null || !this.accessRuleSet.equals(dir.accessRuleSet))
			return false;
	if (this.module != dir.module)
		if (this.module == null || !this.module.equals(dir.module))
			return false;
	return this.binaryFolder.equals(dir.binaryFolder);
}
public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
	if (!doesFileExist(binaryFileName, qualifiedPackageName, qualifiedBinaryFileName)) return null; // most common case

	IBinaryType reader = null;
	try {
		reader = Util.newClassFileReader(this.binaryFolder.getFile(new Path(qualifiedBinaryFileName)));
	} catch (CoreException e) {
		return null;
	} catch (ClassFormatException e) {
		return null;
	} catch (IOException e) {
		return null;
	}
	if (reader != null) {
		char[] modName = this.module == null ? null : this.module.name();
		if (reader instanceof ClassFileReader) {
			((ClassFileReader) reader).moduleName = modName;
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
		}
		if (this.accessRuleSet == null)
			return this.module == null ? new NameEnvironmentAnswer(reader, null) : new NameEnvironmentAnswer(reader, null, modName);
		return new NameEnvironmentAnswer(reader, this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()), modName);
	}
	return null;
}

public IPath getProjectRelativePath() {
	return this.binaryFolder.getProjectRelativePath();
}

public int hashCode() {
	return this.binaryFolder == null ? super.hashCode() : this.binaryFolder.hashCode();
}

protected boolean isExcluded(IResource resource) {
	return false;
}

public boolean isOutputFolder() {
	return this.isOutputFolder;
}

public boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}

public void reset() {
	this.directoryCache = new SimpleLookupTable(5);
}

public String toString() {
	String start = "Binary classpath directory " + this.binaryFolder.getFullPath().toString(); //$NON-NLS-1$
	if (this.accessRuleSet == null)
		return start;
	return start + " with " + this.accessRuleSet; //$NON-NLS-1$
}

public String debugPathString() {
	return this.binaryFolder.getFullPath().toString();
}

@Override
public ITypeLookup typeLookup() {
	//
	return this::findClass;
}

@Override
public IPackageLookup packageLookup() {
	return this::isPackage;
}

@Override
public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	// 
	return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
}

}
