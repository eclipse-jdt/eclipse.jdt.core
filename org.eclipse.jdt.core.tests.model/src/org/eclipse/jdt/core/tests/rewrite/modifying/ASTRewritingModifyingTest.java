/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.modifying;

import java.util.Hashtable;
import java.util.Map;

import junit.framework.ComparisonFailure;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.core.tests.rewrite.describing.StringAsserts;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public abstract class ASTRewritingModifyingTest extends AbstractJavaModelTests {

	/** @deprecated using deprecated code */
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;

	protected IJavaProject javaProject;
	protected IPackageFragmentRoot sourceFolder;

	private Hashtable oldOptions;

	public ASTRewritingModifyingTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite =  new TestSuite(ASTRewritingModifyingTest.class.getName());
		suite.addTest(ASTRewritingModifyingOtherTest.suite());
		suite.addTest(ASTRewritingModifyingInsertTest.suite());
		suite.addTest(ASTRewritingModifyingReplaceTest.suite());
		suite.addTest(ASTRewritingModifyingRemoveTest.suite());
		suite.addTest(ASTRewritingModifyingMoveTest.suite());
		suite.addTest(ASTRewritingModifyingCopyTest.suite());
		return suite;
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();

		this.javaProject = createJavaProject("P", new String[] {"src"}, "bin");
		this.sourceFolder = getPackageFragmentRoot("P", "src");

		Hashtable options = JavaCore.getOptions();
		this.oldOptions = (Hashtable)options.clone();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		JavaCore.setOptions(options);

		waitUntilIndexesReady();
	}
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		JavaCore.setOptions(this.oldOptions);
		super.tearDownSuite();
	}
	public CompilationUnit createCU(
		ICompilationUnit unit,
		boolean resolveBindings,
		int astLevel) {

		try {
			ASTParser c = ASTParser.newParser(astLevel);
			c.setSource(unit);
			c.setResolveBindings(resolveBindings);
			ASTNode result = c.createAST(null);
			return (CompilationUnit) result;
		} catch (IllegalStateException e) {
			// convert ASTParser's complaints into old form
			throw new IllegalArgumentException();
		}
	}

	public CompilationUnit createCU(
			ICompilationUnit unit,
			boolean resolveBindings) {
		return createCU(unit, resolveBindings, AST_INTERNAL_JLS2);
	}

	public CompilationUnit createCU(char[] source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		ASTParser c = ASTParser.newParser(AST_INTERNAL_JLS2);
		c.setSource(source);
		ASTNode result = c.createAST(null);
		return (CompilationUnit) result;
	}

	public String evaluateRewrite(ICompilationUnit cu, CompilationUnit astRoot)  throws CoreException, MalformedTreeException, BadLocationException {
		return evaluateRewrite(cu.getSource(), astRoot, cu.getJavaProject().getOptions(true));
	}

	public String evaluateRewrite(String source, CompilationUnit astRoot)  throws MalformedTreeException, BadLocationException {
		return evaluateRewrite(source, astRoot, getJavaProject("Rewrite").getOptions(true));
	}
	public String evaluateRewrite(String source, CompilationUnit astRoot, Map options)  throws MalformedTreeException, BadLocationException {
		IDocument doc = new Document(source);

		TextEdit changes = astRoot.rewrite(doc, options);
		changes.apply(doc);
		return doc.get();
	}

	public static void assertEqualString(String actual, String expected) {
		try {
			StringAsserts.assertEqualString(actual, expected);
		} catch (ComparisonFailure e) {
			System.out.println(displayActual(actual, 2));
			throw e;
		}
	}

	public static String displayActual(String inputString, int indent) {
		int length = inputString.length();
		StringBuffer buffer = new StringBuffer(length);
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
		for (int i = 0; i < indent; i++) buffer.append("\t");
		buffer.append("buf.append(\"");
		while (tokenizer.hasMoreTokens()){

			String token = tokenizer.nextToken();
			if (token.equals("\r")) {
				buffer.append("\\r");
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("\n")) {
						buffer.append("\\n");
						if (tokenizer.hasMoreTokens()) {
							buffer.append("\");\n");
							for (int i = 0; i < indent; i++) buffer.append("\t");
							buffer.append("buf.append(\"");
						}
						continue;
					}
					buffer.append("\");\n");
					for (int i = 0; i < indent; i++) buffer.append("\t");
					buffer.append("buf.append(\"");
				} else {
					continue;
				}
			} else if (token.equals("\n")) {
				buffer.append("\\n");
				if (tokenizer.hasMoreTokens()) {
					buffer.append("\");\n");
					for (int i = 0; i < indent; i++) buffer.append("\t");
					buffer.append("buf.append(\"");
				}
				continue;
			}

			StringBuffer tokenBuffer = new StringBuffer();
			for (int i = 0; i < token.length(); i++){
				char c = token.charAt(i);
				switch (c) {
					case '\r' :
						tokenBuffer.append("\\r");
						break;
					case '\n' :
						tokenBuffer.append("\\n");
						break;
					case '\b' :
						tokenBuffer.append("\\b");
						break;
					case '\t' :
						tokenBuffer.append("\t");
						break;
					case '\f' :
						tokenBuffer.append("\\f");
						break;
					case '\"' :
						tokenBuffer.append("\\\"");
						break;
					case '\'' :
						tokenBuffer.append("\\'");
						break;
					case '\\' :
						tokenBuffer.append("\\\\");
						break;
					default :
						tokenBuffer.append(c);
				}
			}
			buffer.append(tokenBuffer.toString());
		}
		buffer.append("\");");
		return buffer.toString();
	}
}
