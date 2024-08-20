/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

import com.sun.tools.javac.tree.JCTree.JCModuleDecl;

public class JavacClassFile extends ClassFile {
	private String fullName;
	private IContainer outputDir;
	private byte[] bytes = null;
	private File proxyFile = null;

	public JavacClassFile(String qualifiedName, ClassFile enclosingClass, IContainer outputDir) {
		this.fullName = qualifiedName;
		this.isNestedType = enclosingClass != null;
		this.enclosingClassFile = enclosingClass;
		this.outputDir = outputDir;
	}

	public JavacClassFile(JCModuleDecl moduleDecl, IContainer outputDir) {
		// TODO: moduleDecl probably needs to be used, but how?
		this.fullName = "module-info";
		this.isNestedType = false;
		this.enclosingClassFile = null;
		this.outputDir = outputDir;
	}

	@Override
	public char[][] getCompoundName() {
		String[] names = this.fullName.split("\\.");
		char[][] compoundNames = new char[names.length][];
		for (int i = 0; i < names.length; i++) {
			compoundNames[i] = names[i].toCharArray();
		}

		return compoundNames;
	}

	@Override
	public char[] fileName() {
		String compoundName = this.fullName.replace('.', '/');
		return compoundName.toCharArray();
	}

	@Override
	public byte[] getBytes() {
		if (this.bytes == null) {
			File tempClassFile = this.getProxyTempClassFile();
			if (tempClassFile == null || !tempClassFile.exists()) {
				this.bytes = new byte[0];
			} else {
				try {
					this.bytes = Files.readAllBytes(tempClassFile.toPath());
				} catch (IOException e) {
					this.bytes = new byte[0];
				}
			}
		}

		return this.bytes;
	}

	File getProxyTempClassFile() {
		if (this.proxyFile == null) {
			this.proxyFile = computeMappedTempClassFile(this.outputDir, this.fullName);
		}

		return this.proxyFile;
	}

	void deleteTempClassFile() {
		File tempClassFile = this.getProxyTempClassFile();
		if (tempClassFile != null && tempClassFile.exists()) {
			tempClassFile.delete();
		}
	}

	void deleteExpectedClassFile() {
		IFile targetClassFile = computeExpectedClassFile(this.outputDir, this.fullName);
		if (targetClassFile != null) {
			try {
				targetClassFile.delete(true, null);
			} catch (CoreException e) {
				// ignore
			}
		}
	}

	/**
	 * Returns the mapped temporary class file for the specified class symbol.
	 */
	public static File computeMappedTempClassFile(IContainer expectedOutputDir, String qualifiedClassName) {
		if (expectedOutputDir != null) {
			IPath baseOutputPath = getMappedTempOutput(expectedOutputDir);
			String fileName = qualifiedClassName.replace('.', File.separatorChar);
			IPath filePath = new Path(fileName);
			return baseOutputPath.append(filePath.addFileExtension(SuffixConstants.EXTENSION_class)).toFile();
		}

		return null;
	}

	/**
	 * Returns the expected class file for the specified class symbol.
	 */
	public static IFile computeExpectedClassFile(IContainer expectedOutputDir, String qualifiedClassName) {
		if (expectedOutputDir != null) {
			String fileName = qualifiedClassName.replace('.', File.separatorChar);
			IPath filePath = new Path(fileName);
			return expectedOutputDir.getFile(filePath.addFileExtension(SuffixConstants.EXTENSION_class));
		}

		return null;
	}

	/**
	 * The upstream ImageBuilder expects the Javac Compiler to return the
	 * class file as bytes instead of writing it directly to the target
	 * output directory. To prevent conflicts with the ImageBuilder, we
	 * configure Javac to generate the class file in a temporary location.
	 * This method returns the mapped temporary output location for the
	 * specified output directory.
	 */
	public static IPath getMappedTempOutput(IContainer expectedOutput) {
		IProject project = expectedOutput.getProject();
		if (project == null) {
			return expectedOutput.getRawLocation();
		}

		IPath workingLocation = project.getWorkingLocation(JavaCore.PLUGIN_ID);
		String tempOutputName = expectedOutput.getName() + "_" + Integer.toHexString(expectedOutput.hashCode());
		return workingLocation.append("javac/" + tempOutputName);
	}
}
