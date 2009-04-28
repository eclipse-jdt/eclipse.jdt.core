/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import org.eclipse.jface.text.Document;

import org.eclipse.jdt.internal.core.dom.rewrite.SourceModifier;

/**
 *
 */
public class SourceModifierTest extends ASTRewritingTest {

	private static final Class THIS= SourceModifierTest.class;

	public SourceModifierTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}

	public static Test suite() {
		return allTests();
	}

	public void testRemoveIndents() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        return;\n");
		buf.append("    }\n");
		buf.append("}\n");

		Document buffer= new Document(buf.toString());

		int offset= buf.toString().indexOf("while");
		int length= buf.toString().indexOf("return;") + "return;".length() - offset;

		String content= buffer.get(offset, length);
		SourceModifier modifier= new SourceModifier(2, "    ", 4, 4);
		MultiTextEdit edit= new MultiTextEdit(0, content.length());
		ReplaceEdit[] replaces= modifier.getModifications(content);
		for (int i= 0; i < replaces.length; i++) {
			edit.addChild(replaces[i]);
		}

		Document innerBuffer= new Document(content);
		edit.apply(innerBuffer);

		buffer.replace(offset, length, innerBuffer.get());

		String preview= buffer.get();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("        i++;\n");
		buf.append("    }\n");
		buf.append("    return;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertEqualString(preview, expected);
	}

	public void testAddIndents() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        return;\n");
		buf.append("    }\n");
		buf.append("}\n");

		Document buffer= new Document(buf.toString());

		int offset= buf.toString().indexOf("while");
		int length= buf.toString().indexOf("return;") + "return;".length() - offset;

		String content= buffer.get(offset, length);
		SourceModifier modifier= new SourceModifier(2, "            ", 4, 4);
		MultiTextEdit edit= new MultiTextEdit(0, content.length());
		ReplaceEdit[] replaces= modifier.getModifications(content);
		for (int i= 0; i < replaces.length; i++) {
			edit.addChild(replaces[i]);
		}

		Document innerBuffer= new Document(content);
		edit.apply(innerBuffer);

		buffer.replace(offset, length, innerBuffer.get());

		String preview= buffer.get();

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("                foo();\n");
		buf.append("                i++; // comment\n");
		buf.append("                i++;\n");
		buf.append("            }\n");
		buf.append("            return;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertEqualString(preview, expected);
	}
}
