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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class GenerateBuildScript {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String HEADER=
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR +
		"<project name=\"export-executable\" default=\"build_executable\">" +LINE_SEPARATOR +
		"    <target name=\"build_executable\">" + LINE_SEPARATOR +
		"        <echo message=\"compiling resources   -> .o\"/>" + LINE_SEPARATOR;
		
	private static final String FOOTER =
		"	    <echo message=\"compiling sources      -> .o\"/>" + LINE_SEPARATOR +
		"        <apply failonerror=\"true\" executable=\"${gcc-path}/bin/gcj.exe\" dest=\"${work}\" parallel=\"false\">" + LINE_SEPARATOR +
		"  			 <arg value=\"--verbose\"/>" + LINE_SEPARATOR +
		"            <arg value=\"--classpath=${work}\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-O2\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-c\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-fassume-compiled\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-march=pentium4\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-mfpmath=sse\"/>" + LINE_SEPARATOR +
		"            <srcfile/>" + LINE_SEPARATOR +
		"            <arg value=\"-o\"/>" + LINE_SEPARATOR +
		"            <targetfile/>" + LINE_SEPARATOR +
		"            <fileset dir=\"${work}\" includes=\"**/*.java\"/>" + LINE_SEPARATOR +
		"            <mapper type=\"glob\" from=\"*.java\" to=\"*.o\"/>" + LINE_SEPARATOR +
		"        </apply>" + LINE_SEPARATOR + LINE_SEPARATOR +
		"        <echo message=\"linking .o -> ${binaryname}\"/>" + LINE_SEPARATOR +
		"        <apply failonerror=\"true\" executable=\"${gcc-path}/bin/gcj.exe\" parallel=\"true\">" + LINE_SEPARATOR +
		"        	<arg value=\"--verbose\"/>" + LINE_SEPARATOR +
		"            <arg line =\"-o ${dest}${binaryname}.exe\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-fassume-compiled\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-march=pentium4\"/>" + LINE_SEPARATOR +
		"            <arg value=\"-mfpmath=sse\"/>" + LINE_SEPARATOR +
		"            <arg line=\"--main=org.eclipse.jdt.internal.compiler.batch.Main\"/>" + LINE_SEPARATOR +
		"            <fileset dir=\"${work}\" includes=\"**/*.o\"/>" + LINE_SEPARATOR +
		"       </apply>" + LINE_SEPARATOR +
		"    </target>" + LINE_SEPARATOR +
		"</project>" + LINE_SEPARATOR;

	private static void collectAllPropertiesFiles(File root, ArrayList collector) {
		File[] files = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				collectAllPropertiesFiles(files[i], collector);
			} else if (files[i].getName().endsWith(".rsc") || files[i].getName().endsWith(".properties")) { //$NON-NLS-1$
				String newElement = files[i].getAbsolutePath();
				newElement = newElement.replace('\\', '/');
				collector.add(newElement);
			}
		}
	}

	private static void dumpAllProperties(Writer writer, File sourceDir, ArrayList collector) throws IOException {
		for (int i = 0, max = collector.size(); i < max; i++) {
			String absolutePath = (String) collector.get(i);
			String fileName = absolutePath.substring(sourceDir.getAbsolutePath().length() + 1); 
			writer.write("  		<exec dir=\"${work}\" executable=\"${gcc-path}/bin/gcj.exe\">" + LINE_SEPARATOR);
			writer.write("  		  <arg line=\"--resource ");
			writer.write(fileName + " " + fileName + " -c -o " + getObjectName(fileName) + "\"/>" + LINE_SEPARATOR);
			writer.write("  		</exec>" + LINE_SEPARATOR);
		}
	}

	private static String getObjectName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.')) + ".o";
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
				collectAllPropertiesFiles(sourceDir, collector);
				dumpAllProperties(writer, sourceDir, collector);
			}
			writer.write(FOOTER);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
