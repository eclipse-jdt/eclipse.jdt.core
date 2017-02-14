/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
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

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJMod extends ClasspathJar {

	public static char[] CLASSES = "classes".toCharArray(); //$NON-NLS-1$
	public static char[] CLASSES_FOLDER = "classes/".toCharArray(); //$NON-NLS-1$
	private static int MODULE_DESCRIPTOR_NAME_LENGTH = MODULE_INFO_CLASS.length();

	ClasspathJMod(String zipFilename, long lastModified, AccessRuleSet accessRuleSet, IPath externalAnnotationPath, INameEnvironment env) {
		super(zipFilename, lastModified, accessRuleSet, externalAnnotationPath, env, true);
	}


	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		// TOOD: BETA_JAVA9 - Should really check for packages with the module context
		if (!isPackage(qualifiedPackageName)) return null; // most common case

		try {
			qualifiedBinaryFileName = new String(CharOperation.append(CLASSES_FOLDER, qualifiedBinaryFileName.toCharArray()));
			IBinaryType reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
			if (reader != null) {
				if (reader instanceof ClassFileReader) {
					ClassFileReader classReader = (ClassFileReader) reader;
					if (classReader.moduleName == null) {
						classReader.moduleName = this.module == null ? null : this.module.name();
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
				}
				if (this.accessRuleSet == null)
					return new NameEnvironmentAnswer(reader, null);
				return new NameEnvironmentAnswer(reader, this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()));
			}
		} catch (IOException e) { // treat as if class file is missing
		} catch (ClassFormatException e) { // treat as if class file is missing
		}
		return null;
	}
	protected String readJarContent(final SimpleSet packageSet) {
		String modInfo = null;
		for (Enumeration<? extends ZipEntry> e = this.zipFile.entries(); e.hasMoreElements(); ) {
			ZipEntry entry = e.nextElement();
			char[] entryName = entry.getName().toCharArray();
			int index = CharOperation.indexOf('/', entryName);
			if (index != -1) {
				char[] folder = CharOperation.subarray(entryName, 0, index);
				if (CharOperation.equals(CLASSES, folder)) {
					char[] fileName = CharOperation.subarray(entryName, index + 1, entryName.length);
					if (modInfo == null && fileName.length == MODULE_DESCRIPTOR_NAME_LENGTH) {
						if (CharOperation.equals(fileName, MODULE_INFO_CLASS.toCharArray())) {
							InputStream stream = null;
							InputStream inputStream;
							try {
								inputStream = this.zipFile.getInputStream(entry);
								if (inputStream == null) throw new IOException("Invalid zip entry name : " + entry.getName()); //$NON-NLS-1$
								stream = new BufferedInputStream(inputStream);
								byte[] content = Util.getInputStreamAsByteArray(stream, (int) entry.getSize());
								DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("c:\\temp\\module-info.class"))); //$NON-NLS-1$
								dos.write(content);
								dos.close();
//								FileWriter writer = new FileWriter(new File("c:\\temp\\module-info.class")); //$NON-NLS-1$
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							modInfo = new String(entryName);
						}
					}
					addToPackageSet(packageSet, new String(fileName), false);
				}
			}
		}
		return modInfo;
	}
}
