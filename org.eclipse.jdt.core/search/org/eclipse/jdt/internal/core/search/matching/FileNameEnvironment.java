package org.eclipse.jdt.internal.core.search.matching;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class FileNameEnvironment extends FileSystem {
	
public FileNameEnvironment(String[] classpathNames, String encoding) {
	super(classpathNames, new String[0], encoding);
}

public ClasspathJar getClasspathJar(File file) throws IOException {
	try {
		ZipFile zipFile = JavaModelManager.getJavaModelManager().getZipFile(new Path(file.getPath()));
		return new ClasspathJar(zipFile, false);
	} catch (CoreException e) {
		return super.getClasspathJar(file, true);
	}
}
}
