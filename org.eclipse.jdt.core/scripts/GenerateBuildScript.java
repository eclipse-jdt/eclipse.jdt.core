/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class GenerateBuildScript {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String HEADER=
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + //$NON-NLS-1$
		"<project name=\"export-executable\" default=\"build_executable\">" +LINE_SEPARATOR + //$NON-NLS-1$
		"    <target name=\"build_executable\">" + LINE_SEPARATOR; //$NON-NLS-1$
		
	private static final String SOURCE_FILES =
		"	    <echo message=\"compiling sources      -> .o\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"        <apply failonerror=\"true\" executable=\"${gcc-path}/bin/gcj.exe\" dest=\"${bin}\" parallel=\"false\">" + LINE_SEPARATOR + //$NON-NLS-1$
		"  			 <arg value=\"--verbose\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"--classpath=${bin}\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-O2\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-c\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-fassume-compiled\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-march=pentium4\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-mfpmath=sse\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <srcfile/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-o\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <targetfile/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <fileset dir=\"${bin}\" includes=\"**/*.java\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <mapper type=\"glob\" from=\"*.java\" to=\"*.o\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"        </apply>" + LINE_SEPARATOR + LINE_SEPARATOR; //$NON-NLS-1$
	private static final String FOOTER =
		"        <echo message=\"linking .o -> ${binaryname}\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"        <apply failonerror=\"true\" executable=\"${gcc-path}/bin/gcj.exe\" parallel=\"true\">" + LINE_SEPARATOR + //$NON-NLS-1$
		"        	<arg value=\"--verbose\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg line =\"-o ${dest}/${binaryname}.exe\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-fassume-compiled\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-march=pentium4\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-mfpmath=sse\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg line=\"--main=org.eclipse.jdt.internal.compiler.batch.Main\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <fileset dir=\"${bin}\" includes=\"**/*.o\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"       </apply>" + LINE_SEPARATOR + //$NON-NLS-1$
		"    </target>" + LINE_SEPARATOR + //$NON-NLS-1$
		"</project>" + LINE_SEPARATOR; //$NON-NLS-1$

	private static void collectAllFiles(File root, ArrayList collector, FileFilter fileFilter) {
		File[] files = root.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				collectAllFiles(files[i], collector, fileFilter);
			} else {
				String newElement = files[i].getAbsolutePath();
				newElement = newElement.replace('\\', '/');
				collector.add(newElement);
			}
		}
	}

	private static void dumpAllProperties(Writer writer, File sourceDir, ArrayList collector) throws IOException {
		writer.write("        <echo message=\"compiling resources   -> .o\"/>" + LINE_SEPARATOR); //$NON-NLS-1$
		for (int i = 0, max = collector.size(); i < max; i++) {
			String absolutePath = (String) collector.get(i);
			String fileName = absolutePath.substring(sourceDir.getAbsolutePath().length() + 1); 
			writer.write("  		<exec dir=\"${bin}\" executable=\"${gcc-path}/bin/gcj.exe\">" + LINE_SEPARATOR); //$NON-NLS-1$
			writer.write("  		  <arg line=\"--resource "); //$NON-NLS-1$
			writer.write(fileName + " " + fileName + " -c -o " + getObjectName(fileName) + "\"/>" + LINE_SEPARATOR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writer.write("  		</exec>" + LINE_SEPARATOR); //$NON-NLS-1$
		}
	}

	private static void dumpAllClassFiles(Writer writer, File sourceDir, ArrayList collector) throws IOException {
		writer.write("        <echo message=\"compiling class files   -> .o\"/>" + LINE_SEPARATOR); //$NON-NLS-1$
//		for (int i = 0, max = collector.size(); i < max; i++) {
//			String absolutePath = (String) collector.get(i);
//			String fileName = absolutePath.substring(sourceDir.getAbsolutePath().length() + 1);
//			writer.write("  		<exec dir=\"${bin}\" executable=\"${gcc-path}/bin/gcj.exe\">" + LINE_SEPARATOR); //$NON-NLS-1$
//			writer.write("  		  <arg line=\""); //$NON-NLS-1$
//			writer.write(fileName + " -c -o " + getObjectName(fileName) + "\"/>" + LINE_SEPARATOR); //$NON-NLS-1$ //$NON-NLS-2$
//			writer.write("  		</exec>" + LINE_SEPARATOR); //$NON-NLS-1$
//		}
		writer.write(
		"        <apply failonerror=\"true\" executable=\"${gcc-path}/bin/gcj.exe\" dest=\"${bin}\" parallel=\"false\">" + LINE_SEPARATOR + //$NON-NLS-1$
		"  			 <arg value=\"--verbose\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"--classpath=${bin}\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-O2\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-c\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-fassume-compiled\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-march=pentium4\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-mfpmath=sse\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <srcfile/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <arg value=\"-o\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <targetfile/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <fileset dir=\"${bin}\" includes=\"**/*.class\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"            <mapper type=\"glob\" from=\"*.class\" to=\"*.o\"/>" + LINE_SEPARATOR + //$NON-NLS-1$
		"        </apply>" + LINE_SEPARATOR + LINE_SEPARATOR); //$NON-NLS-1$
	}

	private static String getObjectName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.')) + ".o"; //$NON-NLS-1$
	}
			
	public static void main(String[] args) {
		if (args.length != 2) {
			return;
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[0])));
			writer.write(HEADER);
			File sourceDir = new File(args[1]);
			if (sourceDir.exists()) { 
				ArrayList collector = new ArrayList();
				collectAllFiles(sourceDir, collector, new FileFilter() {
					public boolean accept(File pathname) {
						String fileName = pathname.getAbsolutePath();
						return pathname.isDirectory() || fileName.endsWith(".rsc") || fileName.endsWith(".properties"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
				dumpAllProperties(writer, sourceDir, collector);
				collector = new ArrayList();
				collectAllFiles(sourceDir, collector, new FileFilter() {
					public boolean accept(File pathname) {
						String fileName = pathname.getAbsolutePath();
						return pathname.isDirectory() || fileName.endsWith(".class"); //$NON-NLS-1$
					}
				});
				dumpAllClassFiles(writer, sourceDir, collector);				
				
//				writer.write(SOURCE_FILES);
			}
			writer.write(FOOTER);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
