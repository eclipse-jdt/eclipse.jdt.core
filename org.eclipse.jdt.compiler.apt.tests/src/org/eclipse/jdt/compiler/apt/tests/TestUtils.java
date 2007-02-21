/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

/**
 * @since 3.3
 */
public class TestUtils {

	/**
	 * Convert an array of strings into a path.  
	 * E.g., turn { "a", "b", "c.d" } into a File representing "a/b/c.d".
	 */
	public static File concatPath(String... names) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.length; ++i) {
			if (i > 0) {
				sb.append(File.separator);
			}
			sb.append(names[i]);
		}
		return new File(sb.toString());
	}

	/**
	 * Create a temporary file that is a copy of a file in the plug-in.  Also create any necessary 
	 * parent directories.  Note that for a file to be found in the plug-in in a JUnit test, it must
	 * be in a source folder; also, it must not have a .java extension, or else it will be compiled
	 * and only the .class file will be available at runtime.  For this reason, this method allows
	 * the target name to be different than the source name.
	 * @param srcName the pathname of the source resource to be copied, relative to the plug-in root.
	 * Path elements in this string must be delimited by '/' regardless of operating system, as
	 * described in {@link ClassLoader#getResource(String)}.  For example, "targets/ExampleClass.java.txt". 
	 * @param targetFolder the folder on disk where the file will be written, including any package directories.
	 * Path elements in this string should be operating-system appropriate, e.g. {@link File#separator}.
	 * @param targetName the name of the file to be created, e.g. "ExampleClass.java".
	 * @return a File corresponding to the created file.
	 */
	public static File copyResource(String srcName, File targetFolder, String targetName) {
		InputStream source = TestUtils.class.getClassLoader().getResourceAsStream(srcName);
		Assert.assertNotNull("Couldn't read from plugin: " + srcName, source);
		targetFolder.mkdirs();
		Assert.assertTrue("couldn't mkdirs " + targetFolder.toString(), targetFolder.exists());
		File targetFile = new File(targetFolder, targetName);
		
		FileOutputStream target = null;
		try {
			target = new FileOutputStream(targetFile);
			byte[] buf = new byte[256];
			int read = 0;
			while ((read = source.read(buf)) > 0) {
				target.write(buf, 0, read);
			}		
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Couldn't copy target file " + srcName + "from plug-in to " + targetFile.toString());
		} finally {
			try {
				source.close();
				if (target != null) {
					target.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
		return targetFile;
	}

	public static String convertToIndependantLineDelimiter(String source) {
		if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = source.length(); i < length; i++) {
			char car = source.charAt(i);
			if (car == '\r') {
				buffer.append('\n');
				if (i < length-1 && source.charAt(i+1) == '\n') {
					i++; // skip \n after \r
				}
			} else {
				buffer.append(car);
			}
		}
		return buffer.toString();
	}
}
