/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.builder.*;

public class JavaSearchNameEnvironment implements INameEnvironment {
	
	ClasspathLocation[] locations;
	
public JavaSearchNameEnvironment(IJavaProject javaProject) {
	try {
		computeClasspathLocations(javaProject.getProject().getWorkspace().getRoot(), (JavaProject) javaProject);
	} catch(CoreException e) {
		this.locations = new ClasspathLocation[0];
	}
}

public void cleanup() {
	for (int i = 0, length = this.locations.length; i < length; i++) {
		this.locations[i].cleanup();
	}
}

private void computeClasspathLocations(
	IWorkspaceRoot root,
	JavaProject javaProject) throws CoreException {

	String encoding = null;

	IClasspathEntry[] classpath = javaProject.getExpandedClasspath(true/*ignore unresolved variables*/);
	int length = classpath.length;
	ArrayList locations = new ArrayList(length);
	nextEntry : for (int i = 0; i < length; i++) {
		IClasspathEntry entry = classpath[i];
		IPath path = entry.getPath();
		Object target = JavaModel.getTarget(root, path, true);
		if (target == null) continue nextEntry;

		switch(entry.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE :
				if (!(target instanceof IContainer)) continue nextEntry;
				if (encoding == null) {
					encoding = javaProject.getOption(JavaCore.CORE_ENCODING, true);
				}
				locations.add(new ClasspathSourceDirectory((IContainer)target, encoding));
				continue nextEntry;

			case IClasspathEntry.CPE_LIBRARY :
				if (target instanceof IResource) {
					IResource resource = (IResource) target;
					ClasspathLocation location = null;
					if (resource instanceof IFile) {
						String fileName = path.lastSegment();
						if (!Util.isArchiveFileName(fileName)) continue nextEntry;
						location = getClasspathJar((IFile)resource);
					} else if (resource instanceof IContainer) {
						location = ClasspathLocation.forBinaryFolder((IContainer) target, false); // is library folder not output folder
					}
					locations.add(location);
				} else if (target instanceof File) {
					String fileName = path.lastSegment();
					if (!Util.isArchiveFileName(fileName)) continue nextEntry;
					locations.add(getClasspathJar(path.toOSString()));
				}
				continue nextEntry;
		}
	}

	this.locations = new ClasspathLocation[locations.size()];
	locations.toArray(this.locations);
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName) {
	String 
		binaryFileName = null, qBinaryFileName = null, 
		sourceFileName = null, qSourceFileName = null, 
		qPackageName = null;
	for (int i = 0, length = this.locations.length; i < length; i++) {
		ClasspathLocation location = this.locations[i];
		NameEnvironmentAnswer answer;
		if (location instanceof ClasspathSourceDirectory) {
			if (sourceFileName == null) {
				qSourceFileName = qualifiedTypeName + ".java"; //$NON-NLS-1$
				sourceFileName = qSourceFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qSourceFileName.length() - typeName.length - 5; // size of ".java"
					qPackageName =  qSourceFileName.substring(0, typeNameStart - 1);
					sourceFileName = qSourceFileName.substring(typeNameStart);
				}
			}
			answer = location.findClass(
				sourceFileName,
				qPackageName,
				qSourceFileName);
		} else {
			if (binaryFileName == null) {
				qBinaryFileName = qualifiedTypeName + ".class"; //$NON-NLS-1$
				binaryFileName = qBinaryFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
					qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
					binaryFileName = qBinaryFileName.substring(typeNameStart);
				}
			}
			answer = 
				location.findClass(
					binaryFileName, 
					qPackageName, 
					qBinaryFileName);
		}
		if (answer != null) return answer;
	}
	return null;
}

public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName);
	return null;
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1]);
	return null;
}

private ClasspathLocation getClasspathJar(IFile file) {
	try {
		ZipFile zipFile = JavaModelManager.getJavaModelManager().getZipFile(file.getFullPath());
		return new ClasspathJar(zipFile);
	} catch (CoreException e) {
		return null;
	}
}

private ClasspathLocation getClasspathJar(String zipPathString) {
	IPath zipPath = new Path(zipPathString);
	try {
		ZipFile zipFile = JavaModelManager.getJavaModelManager().getZipFile(zipPath);
		return new ClasspathJar(zipFile);
	} catch (CoreException e) {
		return null;
	}
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return isPackage(new String(CharOperation.concatWith(compoundName, packageName, '/')));
}

public boolean isPackage(String qualifiedPackageName) {
	for (int i = 0, length = this.locations.length; i < length; i++)
		if (this.locations[i].isPackage(qualifiedPackageName))
			return true;
	return false;
}

}
