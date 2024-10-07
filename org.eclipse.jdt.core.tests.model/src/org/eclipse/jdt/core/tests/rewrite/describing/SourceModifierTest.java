/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;


import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.internal.core.dom.rewrite.SourceModifier;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class SourceModifierTest extends AbstractJavaModelTests {

	public SourceModifierTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(SourceModifierTest.class);
	}

	public void testRemoveIndents() throws Exception {
		StringBuilder buf= new StringBuilder();
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

		buf= new StringBuilder();
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

		StringAsserts.assertEqualString(preview, expected);
	}

	public void testAddIndents() throws Exception {
		StringBuilder buf= new StringBuilder();
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

		buf= new StringBuilder();
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

		StringAsserts.assertEqualString(preview, expected);
	}
}
