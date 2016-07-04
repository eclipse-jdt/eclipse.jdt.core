/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kenneth Olson - initial API and implementation
 *     Dennis Hendriks - initial API and implementation
 *     IBM Corporation - Contribution for bug 188796
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClasspathJsr199 extends ClasspathLocation {
	private static final Set<JavaFileObject.Kind> fileTypes = new HashSet<>();

	static {
		fileTypes.add(JavaFileObject.Kind.CLASS);
	}

	private JavaFileManager fileManager;
	private JavaFileManager.Location location;

	public ClasspathJsr199(JavaFileManager file, JavaFileManager.Location location) {
		super(null, null);
		this.fileManager = file;
		this.location = location;
	}

	@Override
	public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		// Assume no linked jars
		return null;
	}

	@Override
	public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName, IModule mod) {
		return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false, mod);
	}

	@Override
	public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String aQualifiedBinaryFileName,
			boolean asBinaryOnly, IModule mod) {

		String qualifiedBinaryFileName = File.separatorChar == '/'
				? aQualifiedBinaryFileName
				: aQualifiedBinaryFileName.replace(File.separatorChar, '/');

		try {
			int lastDot = qualifiedBinaryFileName.lastIndexOf('.');
			String className = lastDot < 0 ? qualifiedBinaryFileName : qualifiedBinaryFileName.substring(0, lastDot);
			JavaFileObject jfo = null;
			try {
				jfo = this.fileManager.getJavaFileForInput(this.location, className, JavaFileObject.Kind.CLASS);
			} catch (IOException e) {
				// treat as if class file is missing
			}

			if (jfo == null)
				return null; // most common case
	
			try (InputStream inputStream = jfo.openInputStream()) {
				ClassFileReader reader = ClassFileReader.read(inputStream, qualifiedBinaryFileName);
				if (reader != null) {
					reader.moduleName = this.module == null ? null : this.module.name();
					return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName));
				}
			}
		} catch (ClassFormatException e) {
			// treat as if class file is missing
		} catch (IOException e) {
			// treat as if class file is missing
		}
		return null;
	}

	@Override
	public char[][][] findTypeNames(String aQualifiedPackageName, IModule mod) {
		String qualifiedPackageName = File.separatorChar == '/' ? aQualifiedPackageName : aQualifiedPackageName.replace(
				File.separatorChar, '/');

		Iterable<JavaFileObject> files = null;
		try {
			files = this.fileManager.list(this.location, qualifiedPackageName, fileTypes, false);
		} catch (IOException e) {
			// treat as if empty
		}
		if (files == null) {
			return null;
		}
		ArrayList answers = new ArrayList();
		char[][] packageName = CharOperation.splitOn(File.separatorChar, qualifiedPackageName.toCharArray());

		for (JavaFileObject file : files) {
			String fileName = file.toUri().getPath();

			int last = fileName.lastIndexOf('/');
			if (last > 0) {
				int indexOfDot = fileName.lastIndexOf('.');
				if (indexOfDot != -1) {
					String typeName = fileName.substring(last + 1, indexOfDot);
					answers.add(CharOperation.arrayConcat(packageName, typeName.toCharArray()));
				}
			}
		}
		int size = answers.size();
		if (size != 0) {
			char[][][] result = new char[size][][];
			answers.toArray(result);
			return result;
		}
		return null;
	}

	@Override
	public void initialize() throws IOException {
		// nothing to do
	}

	@Override
	public void acceptModule(IModule mod) {
		this.module = mod;
	}

	@Override
	public boolean isPackage(String aQualifiedPackageName) {
		String qualifiedPackageName = File.separatorChar == '/' ? aQualifiedPackageName : aQualifiedPackageName.replace(
				File.separatorChar, '/');

		boolean result = false;
		try {
			Iterable<JavaFileObject> files = this.fileManager.list(this.location, qualifiedPackageName, fileTypes, false);
			Iterator f = files.iterator();
			// if there is any content, assume a package
			if (f.hasNext()) {
				result = true;
			} else {
				// I hate to do this since it is expensive and will throw off garbage
				// but can't think of an alternative now
				files = this.fileManager.list(this.location, qualifiedPackageName, fileTypes, true);
				f = files.iterator();
				// if there is any content, assume a package
				if (f.hasNext()) {
					result = true;
				}
			}
		} catch (IOException e) {
			// treat as if missing
		}
		return result;
	}

	@Override
	public void reset() {
		try {
			this.fileManager.flush();
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public String toString() {
		return "Classpath for Jsr 199 JavaFileManager: " + this.location; //$NON-NLS-1$
	}

	@Override
	public char[] normalizedPath() {
		if (this.normalizedPath == null) {
			this.normalizedPath = this.path.toCharArray();
		}
		return this.normalizedPath;
	}

	@Override
	public String getPath() {
		if (this.path == null) {
			this.path = this.location.getName();
		}
		return this.path;
	}

	@Override
	public int getMode() {
		return BINARY;
	}
	
	@Override
	public boolean hasAnnotationFileFor(String qualifiedTypeName) {
		return false;
	}

	@Override
	public IModule getModule(char[] moduleName) {
		// TODO Auto-generated method stub
		return null;
	}
}
