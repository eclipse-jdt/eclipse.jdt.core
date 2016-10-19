/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.env.IModuleLocation;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ModuleFinder {

	protected static List<FileSystem.Classpath> findModules(File f, String destinationPath, Parser parser, Map<String, String> options, boolean sourceOnly) {
		List<FileSystem.Classpath> collector = new ArrayList<>();
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			if (files == null) 
				return Collections.EMPTY_LIST;
			for (final File file : files) {
				FileSystem.Classpath modulePath = FileSystem.getClasspath(
						file.getAbsolutePath(),
						null,
						sourceOnly,
						null,
						destinationPath == null ? null : (destinationPath + File.separator + file.getName()), 
						options);
				if (modulePath != null) {
					collector.add(modulePath);
					IModule module = null;
					if (file.isDirectory()) {
						String[] list = file.list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								if (dir == file && (name.equalsIgnoreCase(IModuleLocation.MODULE_INFO_CLASS)
										|| name.equalsIgnoreCase(IModuleLocation.MODULE_INFO_JAVA))) {
									return true;
								}
								return false;
							}
						});
						if (list.length > 0) {
							String fileName = list[0];
							switch (fileName) {
								case IModuleLocation.MODULE_INFO_CLASS:
									module = ModuleFinder.extractModuleFromClass(new File(file, fileName), modulePath);
									break;
								case IModuleLocation.MODULE_INFO_JAVA:
									module = ModuleFinder.extractModuleFromSource(new File(file, fileName), parser, modulePath);
									break;
							}
						}
					} else if (isJar(file)) {
						module = extractModuleFromJar(file, modulePath);
					}
					if (module != null)
						modulePath.acceptModule(module);
				}
//				FileSystem.Classpath modulePath = FileSystem.getClasspath(
//						file.getAbsolutePath(),
//						null,
//						sourceOnly,
//						null,
//						destinationPath == null ? null : (destinationPath + File.separator + file.getName()), 
//						options);
//				if (modulePath != null)
//					collector.add(modulePath);
//				if (module != null)
//					modulePath.acceptModule(module);
			}
		}
		return collector;
	}
	/**
	 * Extracts the single reads clause from the given
	 * command line option (--add-reads). The result is a String[] with two
	 * element, first being the source module and second being the target module.
	 * The expected format is: 
	 *  --add-reads <source-module>=<target-module>
	 * @param option
	 * @return a String[] with source and target module of the "reads" clause. 
	 */
	protected static String[] extractAddonRead(String option) {
		StringTokenizer tokenizer = new StringTokenizer(option, "="); //$NON-NLS-1$
		String source = null;
		String target = null;
		if (tokenizer.hasMoreTokens()) {
			source = tokenizer.nextToken();
		} else {
			// Handle error
			return null;
		}
		if (tokenizer.hasMoreTokens()) {
			target = tokenizer.nextToken();
		} else {
			// Handle error
			return null;
		}
 		return new String[]{source, target};
	}
	/**
	 * Parses the --add-exports command line option and returns the package export definitions
	 * in the form of an IModule. Note the IModule returned only holds this specific exports-to
	 * clause and can't by itself be used as a module description.
	 *
	 * The expected format is:
	 *   --add-exports <source-module>/<package>=<target-module>(,<target-module>)*
	 * @param option
	 * @return a dummy module object with package exports
	 */
	protected static IModule extractAddonExport(String option) {
		StringTokenizer tokenizer = new StringTokenizer(option, "/"); //$NON-NLS-1$
		String source = null;
		String pack = null;
		List<String> targets = new ArrayList<>();
		if (tokenizer.hasMoreTokens()) {
			source = tokenizer.nextToken("/"); //$NON-NLS-1$
		} else {
			// Handle error
			return null;
		}
		if (tokenizer.hasMoreTokens()) {
			pack = tokenizer.nextToken("/="); //$NON-NLS-1$
		} else {
			// Handle error
			return null;
		}
		while (tokenizer.hasMoreTokens()) {
			targets.add(tokenizer.nextToken("=,")); //$NON-NLS-1$
		}
		PackageExport export = new PackageExport(pack.toCharArray());
		export.exportedTo = new char[targets.size()][];
		for(int i = 0; i < export.exportedTo.length; i++) {
			export.exportedTo[i] = targets.get(i).toCharArray();
		}
		return new Module(source.toCharArray(), export);
	}

	static class PackageExport implements IPackageExport {
		char[] name;
		char[][] exportedTo;
		PackageExport(char[] name) {
			this.name = name;
		}
		@Override
		public char[] name() {
			return this.name;
		}
		@Override
		public char[][] exportedTo() {
			return this.exportedTo;
		}
	}
	
	static class Module implements IModule {
		char[] name;
		IPackageExport[] export;
		Module(char[] name, IPackageExport export) {
			this.name = name;
			this.export = new IPackageExport[]{export};
		}
		@Override
		public char[] name() {
			return this.name;
		}
		@Override
		public IModuleReference[] requires() {
			return null;
		}
		@Override
		public IPackageExport[] exports() {
			return this.export;
		}
		@Override
		public char[][] uses() {
			return null;
		}
		@Override
		public IService[] provides() {
			return null;
		}
	}
	
	private static boolean isJar(File file) {
		int format = Util.archiveFormat(file.getAbsolutePath());
		return format >= Util.ZIP_FILE;
	}
	private static IModule extractModuleFromJar(File file, Classpath pathEntry) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			ClassFileReader reader = ClassFileReader.read(zipFile, IModuleLocation.MODULE_INFO_CLASS);
			IModule module = getModule(reader);
			if (module != null) {
				return reader.getModuleDeclaration();
			}
			return null;
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					// Nothing much to do here
				}
			}
		}
		return null;
	}
	private static IModule extractModuleFromClass(File classfilePath, Classpath pathEntry) {
		ClassFileReader reader;
		try {
			reader = ClassFileReader.read(classfilePath);
			IModule module =  getModule(reader);
			if (module != null) {
				return reader.getModuleDeclaration();
			}
			return null;
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static IModule getModule(ClassFileReader classfile) {
		if (classfile != null) {
			return classfile.getModuleDeclaration();
		}
		return null;
	}
	private static IModule extractModuleFromSource(File file, Parser parser, Classpath pathEntry) {
		ICompilationUnit cu = new CompilationUnit(null, file.getAbsolutePath(), null);
		CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
		CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
		if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
			return new SourceModule(unit.moduleDeclaration, pathEntry);
		}
		return null;
	}
}
