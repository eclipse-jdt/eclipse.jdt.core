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
		String str = """
			package test1;
			public class E {
			    public void foo() {
			        while (i == 0) {
			            foo();
			            i++; // comment
			            i++;
			        }
			        return;
			    }
			}
			""";
		Document buffer= new Document(str);

		int offset= str.indexOf("while");
		int length= str.indexOf("return;") + "return;".length() - offset;

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

		String expected= """
			package test1;
			public class E {
			    public void foo() {
			        while (i == 0) {
			        foo();
			        i++; // comment
			        i++;
			    }
			    return;
			    }
			}
			""";

		StringAsserts.assertEqualString(preview, expected);
	}

	public void testAddIndents() throws Exception {
		String str = """
			package test1;
			public class E {
			    public void foo() {
			        while (i == 0) {
			            foo();
			            i++; // comment
			            i++;
			        }
			        return;
			    }
			}
			""";
		Document buffer= new Document(str);

		int offset= str.indexOf("while");
		int length= str.indexOf("return;") + "return;".length() - offset;

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

		String expected= """
			package test1;
			public class E {
			    public void foo() {
			        while (i == 0) {
			                foo();
			                i++; // comment
			                i++;
			            }
			            return;
			    }
			}
			""";

		StringAsserts.assertEqualString(preview, expected);
	}
}
