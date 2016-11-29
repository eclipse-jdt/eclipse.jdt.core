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
import org.eclipse.jdt.internal.compiler.env.IModuleEnvironment;
import org.eclipse.jdt.internal.compiler.env.PackageExportImpl;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ModuleFinder {

	protected static List<FileSystem.Classpath> findModules(File f, String destinationPath, Parser parser, Map<String, String> options, boolean isModulepath) {
		List<FileSystem.Classpath> collector = new ArrayList<>();
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			if (files == null) 
				return Collections.EMPTY_LIST;
			for (final File file : files) {
				FileSystem.Classpath modulePath = FileSystem.getClasspath(
						file.getAbsolutePath(),
						null,
						!isModulepath,
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
								if (dir == file && (name.equalsIgnoreCase(IModuleEnvironment.MODULE_INFO_CLASS)
										|| name.equalsIgnoreCase(IModuleEnvironment.MODULE_INFO_JAVA))) {
									return true;
								}
								return false;
							}
						});
						if (list.length > 0) {
							String fileName = list[0];
							switch (fileName) {
								case IModuleEnvironment.MODULE_INFO_CLASS:
									module = ModuleFinder.extractModuleFromClass(new File(file, fileName), modulePath);
									break;
								case IModuleEnvironment.MODULE_INFO_JAVA:
									module = ModuleFinder.extractModuleFromSource(new File(file, fileName), parser, modulePath);
									break;
							}
						}
					} else if (isJar(file)) {
						module = extractModuleFromJar(file, modulePath);
					}
					if (isModulepath && module == null) {
						 // The name includes the file's extension, but it shouldn't matter.
						module = new ModuleEnvironment.AutoModule(getFileName(file).toCharArray());
					}
					if (module != null)
						modulePath.acceptModule(module);
				}
			}
		}
		return collector;
	}
	private static String getFileName(File file) {
		String name = file.getName();
		int index = name.lastIndexOf('.');
		if (index == -1)
			return name;
		return name.substring(0, index);
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
		PackageExportImpl export = new PackageExportImpl();
		export.pack = pack.toCharArray();
		export.exportedTo = new char[targets.size()][];
		for(int i = 0; i < export.exportedTo.length; i++) {
			export.exportedTo[i] = targets.get(i).toCharArray();
		}
		BasicModule module = new BasicModule(source.toCharArray(), false);
		module.exports = new IModule.IPackageExport[]{export};
		return module;
	}

	private static boolean isJar(File file) {
		int format = Util.archiveFormat(file.getAbsolutePath());
		return format >= Util.ZIP_FILE;
	}
	private static IModule extractModuleFromJar(File file, Classpath pathEntry) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			ClassFileReader reader = ClassFileReader.read(zipFile, IModuleEnvironment.MODULE_INFO_CLASS);
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
			return new BasicModule(unit.moduleDeclaration, pathEntry);
		}
		return null;
	}
}
