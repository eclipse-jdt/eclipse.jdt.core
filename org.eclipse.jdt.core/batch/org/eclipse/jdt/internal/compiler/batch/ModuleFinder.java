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
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleLocation;
import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;
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
				IModule module = null;
				if (file.isDirectory()) {
					String[] list = file.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							if (dir == file 
									&& (name.equalsIgnoreCase(IModuleLocation.MODULE_INFO_CLASS) ||
											name.equalsIgnoreCase(IModuleLocation.MODULE_INFO_JAVA))) {
								return true;
							}
							return false;
						}
					});
					if (list.length > 0) {
						String fileName = list[0];
						switch (fileName) {
							case IModuleLocation.MODULE_INFO_CLASS:
								module = ModuleFinder.extractModuleFromClass(new File(file, fileName));
								break;
							case IModuleLocation.MODULE_INFO_JAVA:
								module = ModuleFinder.extractModuleFromSource(new File(file, fileName), parser);
								break;
						}
					}
				} else if (isJar(file)){
					module = extractModuleFromJar(file);
				}
				FileSystem.Classpath modulePath = FileSystem.getClasspath(
						file.getAbsolutePath(),
						null,
						sourceOnly,
						null,
						destinationPath == null ? null : (destinationPath + File.separator + file.getName()), 
						options);
				if (modulePath != null)
					collector.add(modulePath);
				if (module != null)
					modulePath.acceptModule(module);
			}
		}
		return collector;
	}
	private static boolean isJar(File file) {
		int format = Util.archiveFormat(file.getAbsolutePath());
		return format >= Util.ZIP_FILE;
	}
	private static IModule extractModuleFromJar(File file) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			ClassFileReader reader = ClassFileReader.read(zipFile, IModuleLocation.MODULE_INFO_CLASS);
			return getModule(reader);
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
	private static IModule extractModuleFromClass(File classfilePath) {
		ClassFileReader reader;
		try {
			reader = ClassFileReader.read(classfilePath);
			return getModule(reader);
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
	private static IModule extractModuleFromSource(File file, Parser parser) {
		ICompilationUnit cu = new CompilationUnit(null, file.getAbsolutePath(), null);
		CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
		CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
		if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
			return ModuleEnvironment.createModule(unit.moduleDeclaration);
		}
		return null;
	}
}
