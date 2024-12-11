/*******************************************************************************
 * Copyright (c) 2015, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.tool.ModuleLocationHandler.LocationWrapper;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClasspathJsr199 extends ClasspathLocation {
	private static final Set<JavaFileObject.Kind> fileTypes = new HashSet<>();

	static {
		fileTypes.add(JavaFileObject.Kind.CLASS);
	}

	private final JavaFileManager fileManager;
	private final JavaFileManager.Location location;
	private Classpath jrt;

	public ClasspathJsr199(JavaFileManager file, JavaFileManager.Location location) {
		super(null, null);
		this.fileManager = file;
		this.location = location;
	}
	public ClasspathJsr199(Classpath jrt, JavaFileManager file, JavaFileManager.Location location) {
		super(null, null);
		this.fileManager = file;
		this.jrt = jrt;
		this.location = location;
	}
	/*
	 * Maintain two separate constructors to avoid this being constructed with any other kind of classpath
	 * (other than ClasspathJrt and ClasspathJep249
	 */
	public ClasspathJsr199(ClasspathJep247 older, JavaFileManager file, JavaFileManager.Location location) {
		super(null, null);
		this.fileManager = file;
		this.jrt = older;
		this.location = location;
	}

	@Override
	public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		// Assume no linked jars
		return null;
	}

	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName,
			String aQualifiedBinaryFileName, boolean asBinaryOnly) {
		if (this.jrt != null) {
			return this.jrt.findClass(typeName, qualifiedPackageName, moduleName, aQualifiedBinaryFileName, asBinaryOnly);
		}
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
				ClassFileReader reader = ClassFileReader.read(inputStream.readAllBytes(), qualifiedBinaryFileName);
				if (reader != null) {
					char[] answerModule = this.module != null ? this.module.name() : null;
					return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), answerModule);
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
	public char[][][] findTypeNames(String aQualifiedPackageName, String moduleName) {
		if (this.jrt != null) {
			return this.jrt.findTypeNames(aQualifiedPackageName, moduleName);
		}
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
		if (this.jrt != null) {
			this.jrt.initialize();
		} else if (this.location.isModuleOrientedLocation()) {
			Iterable<Set<Location>> locationsForModules = this.fileManager.listLocationsForModules(this.location);
			for (Set<Location> locs: locationsForModules) {
				for (Location loc : locs) {
					if (loc instanceof LocationWrapper wrapper) {
						for (Path locPath : wrapper.getPaths()) {
							File file = locPath.toFile();
							IModule mod = ModuleFinder.scanForModule(this, file, null, true, null);
							if (mod != null) {
								return;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void acceptModule(IModule mod) {
		if (this.jrt != null)
			return; // do nothing
		this.module = mod;
	}

	@Override
	public char[][] getModulesDeclaringPackage(String aQualifiedPackageName, String moduleName) {
		if (this.jrt != null) {
			return this.jrt.getModulesDeclaringPackage(aQualifiedPackageName, moduleName);
		}
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
		return singletonModuleNameIf(result);
	}

	@Override
	public char[][] listPackages() {
		Set<String> packageNames = new HashSet<>();
		try {
			for (JavaFileObject fileObject : this.fileManager.list(this.location, "", fileTypes, true)) { //$NON-NLS-1$
				String name = fileObject.getName();
				int lastSlash = name.lastIndexOf('/');
				if (lastSlash != -1) {
					packageNames.add(name.substring(0, lastSlash).replace('/', '.'));
				}
			}
			char[][] result = new char[packageNames.size()][];
			int i = 0;
			for (String s : packageNames) {
				result[i++] = s.toCharArray();
			}
			return result;
		} catch (IOException e) {
			// ??
		}
		return CharOperation.NO_CHAR_CHAR;
	}

	@Override
	public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
		if (this.jrt != null)
			return this.jrt.hasCompilationUnit(qualifiedPackageName, moduleName);
		return false;
	}

	@Override
	public void reset() {
		try {
			super.reset();
			this.fileManager.flush();
		} catch (IOException e) {
			// ignore
		}
		if (this.jrt != null) {
			this.jrt.reset();
		}
	}

	@Override
	public String toString() {
		return "Classpath for Jsr 199 JavaFileManager: " + this.location; //$NON-NLS-1$
	}

	@Override
	public char[] normalizedPath() {
		if (this.normalizedPath == null) {
			this.normalizedPath = this.getPath().toCharArray();
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
	public Collection<String> getModuleNames(Collection<String> limitModules) {
		if (this.jrt != null)
			return this.jrt.getModuleNames(limitModules);
		if (this.location.isModuleOrientedLocation()) {
			Set<String> moduleNames = new HashSet<>();
			try {
				for (Set<Location> locs : this.fileManager.listLocationsForModules(this.location)) {
					for (Location loc : locs) {
						String moduleName = this.fileManager.inferModuleName(loc);
						if (moduleName != null)
							moduleNames.add(moduleName);
					}
				}
				return moduleNames;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean hasModule() {
		if (this.jrt != null) {
			return this.jrt.hasModule();
		}
		return super.hasModule();
	}

	@Override
	public IModule getModule(char[] name) {
		if (this.jrt != null) {
			return this.jrt.getModule(name);
		}
		return super.getModule(name);
	}

	@Override
	public IModule getModule() {
		return this.module;
	}

	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName,
			String moduleName, String qualifiedBinaryFileName) {
		//
		return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false);
	}
}
