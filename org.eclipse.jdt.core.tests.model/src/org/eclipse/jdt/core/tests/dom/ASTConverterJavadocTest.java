/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

public class ASTConverterJavadocTest extends ConverterTestSetup {

	/**
	 * @param name
	 */
	public ASTConverterJavadocTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterTest.class.getName());		

		if (false) {
			Class c = ASTConverterJavadocTest.class;
			Method[] methods = c.getMethods();
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterJavadocTest(methods[i].getName()));
				}
			}
			return suite;
		}
		suite.addTest(new ASTConverterJavadocTest("testJavadoc01"));			
		return suite;
	}

	/*
	 * Convert Javadoc source to match Javadoc.toString()
	 */
	List[] convertSource(char[] source) {
		List comments = new ArrayList();
		List numbers = new ArrayList();
		StringBuffer buffer = new StringBuffer(source.length);
		boolean store = false;
		boolean javadoc = false;
		boolean star = false;
		boolean firstTag = true;
		int number = 0;
		for (int i=0; i<source.length; i++) {
			if (javadoc) {
				if (star) {
					if (source[i] == '/') {
						javadoc = false;
						buffer.append("\n */");
						comments.add(buffer.toString());
						numbers.add(new Integer(number));
						buffer = new StringBuffer(source.length);
						number = 0;
						break;
					} else {
						buffer.append('*');
					}
				}
			} else {
				if (source[i] == '/' && source[i+1] == '*' && source[i+2] == '*') {
					javadoc = true;
				}
			}
			if (javadoc) {
				if (source[i] == '\r' || source[i] == '\n') {
					while (source[i] == '*' || Character.isWhitespace(source[i])) {
						star = source[i++] == '*';
					}
					if (star && source[i] == '/') {
						javadoc = false;
						buffer.append("\n */");
						comments.add(buffer.toString());
						numbers.add(new Integer(number));
						buffer = new StringBuffer(source.length);
						number = 0;
						continue;
					}
					if (firstTag) {
						firstTag = false;
						buffer.append(" \n * ");
						number++;
					} else if (source[i] == '@') {
						buffer.append("\n * ");
						number++;
					}
				} else {
					star = source[i] == '*';
				}
				if (!star) buffer.append(source[i]);
			}
		}
		List[] lists = new List[2];
		lists[0] = numbers;
		lists[1] = comments;
		return lists;
	}
	
	/*
	 * Compare a string a subset of a char array.
	 */
	boolean compare(String str, char[] source, int start, int length) {
		if (str.length() != length) return false;
		for (int i=0; i<length; i++) {
			if (str.charAt(i) != source[start+i]) return false;
		}
		return true;
	}

	/*
	 * Verify that objects of Javadoc comment structure match their source positions
	 */
	protected void assertMatchPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int start = docComment.getStartPosition();
		assertTrue("Misplaced javadoc start", source[start] == '/' && source[start+1] == '*' && source[start+2] == '*');
		int end = start+docComment.getLength()-1;
		assertTrue("Wrong javadoc length", end<source.length);
		assertTrue("Misplaced javadoc end", source[end-1] == '*' && source[end] == '/');
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			assertMatchPositions((TagElement) tags.next(), source);
		}
	}

	/*
	 * Verify that objects of Javadoc tag structure match their source positions
	 */
	protected void assertMatchPositions(TagElement tagElement, char[] source) {
		// Verify tag name
		String tagName = tagElement.getTagName();
		if (tagName != null) {
			assertEquals("Misplaced tag name at "+tagElement.getStartPosition(), tagName, new String(source, tagElement.getStartPosition(), tagName.length()));
		}
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			String text = null;
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				text = new String(source, fragment.getStartPosition(), fragment.getLength());
				assertEquals("Misplaced or wrong text element at "+fragment.getStartPosition(), text, ((TextElement) fragment).getText());
			} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
				assertMatchPositions((TagElement) fragment, source);
			} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
				MemberRef memberRef = (MemberRef) fragment;
				ASTNode node = memberRef.getQualifier();
				if (node != null) {
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong member ref container at "+node.getStartPosition(), text, node.toString());
				}
				if (memberRef.getName() != null) {
					node = memberRef.getName();
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong member ref name at "+node.getStartPosition(), text, node.toString());
				}
			} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
				MethodRef methodRef = (MethodRef) fragment;
				ASTNode node = methodRef.getQualifier();
				if (node != null) {
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong method ref container at "+node.getStartPosition(), text, node.toString());
				}
				node = methodRef.getName();
				text = new String(source, node.getStartPosition(), node.getLength());
				assertEquals("Misplaced or wrong method ref name at "+node.getStartPosition(), text, node.toString());
				Iterator parameters = methodRef.parameters().listIterator();
				while (parameters.hasNext()) {
					MethodRefParameter param = (MethodRefParameter) parameters.next();
					node = param.getName();
					if (node != null) {
						text = new String(source, node.getStartPosition(), node.getLength());
						assertEquals("Misplaced or wrong method ref parameter name at "+node.getStartPosition(), text, node.toString());
					}
					node = param.getType();
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong method ref parameter type at "+node.getStartPosition(), text, node.toString());
				}
			}
		}
	}


	/* New implementation which uses an internal index to verify in source: NOT FINALIZED YET
	protected void assertMatchPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int index = docComment.getStartPosition();
		assertTrue("Misplaced javadoc start", source[index++] == '/' && source[index++] == '*' && source[index++] == '*');
		int end = docComment.getEndPosition();
		assertTrue("Wrong javadoc length", end<source.length);
		assertTrue("Misplaced javadoc end", source[end-1] == '*' && source[end] == '/');
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			assertMatchPositions((TagElement) tags.next(), source, index);
		}
	}

	protected void assertMatchPositions(TagElement tagElement, char[] source, int index) {
		// Verify tag name
		String tagName = tagElement.getTagName();
		if (tagName != null) {
			if (tagElement.isNested()) {
				index++; // '{'
			} else {
				index+=4; // '\n * '
			}
//			assertEquals("Misplaced tag name at "+tagElement.getStartPosition(), tagName, new String(source, tagElement.getStartPosition(), tagName.length()));
			assertTrue("Misplaced tag name at "+index, compare(tagName, source, index, tagName.length()));
			index += tagName.length();
		}
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			String text = null;
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
//				text = new String(source, fragment.getStartPosition(), fragment.getLength());
//				assertEquals("Misplaced or wrong text element at "+fragment.getStartPosition(), text, ((TextElement) fragment).getText());
				assertTrue("Misplaced or wrong text element at "+index, compare(((TextElement) fragment).getText(), source, index, fragment.getLength()));
				index += fragment.getLength();
			} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
				assertMatchPositions((TagElement) fragment, source, index);
			} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
				MemberRef memberRef = (MemberRef) fragment;
				ASTNode node = memberRef.getContainer();
				if (node != null) {
//					text = new String(source, node.getStartPosition(), node.getLength());
//					assertEquals("Misplaced or wrong member ref container at "+node.getStartPosition(), text, node.toString());
					assertTrue("Misplaced or wrong member ref container at "+index, compare(node.toString(), );
				}
				if (memberRef.getName() != null) {
					node = memberRef.getName();
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong member ref name at "+node.getStartPosition(), text, node.toString());
				}
			} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
				MethodRef methodRef = (MethodRef) fragment;
				ASTNode node = methodRef.getContainer();
				if (node != null) {
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong method ref container at "+node.getStartPosition(), text, node.toString());
				}
				node = methodRef.getName();
				text = new String(source, node.getStartPosition(), node.getLength());
				assertEquals("Misplaced or wrong method ref name at "+node.getStartPosition(), text, node.toString());
				Iterator parameters = methodRef.parameters().listIterator();
				while (parameters.hasNext()) {
					MethodRefParameter param = (MethodRefParameter) parameters.next();
					node = param.getName();
					if (node != null) {
						text = new String(source, node.getStartPosition(), node.getLength());
						assertEquals("Misplaced or wrong method ref parameter name at "+node.getStartPosition(), text, node.toString());
					}
					node = param.getType();
					text = new String(source, node.getStartPosition(), node.getLength());
					assertEquals("Misplaced or wrong method ref parameter type at "+node.getStartPosition(), text, node.toString());
				}
			}
		}
	}
	*/

	/*
	 * Verify that bindings of Javadoc comment structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	protected void assertMatchBindings(Javadoc docComment) {
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			assertMatchBindings((TagElement) tags.next());
		}
	}

	/*
	 * Verify that bindings of Javadoc tag structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	protected void assertMatchBindings(TagElement tagElement) {
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		IBinding previousBinding = null;
		boolean resolvedBinding = false;
		ASTNode previousFragment = null;
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				TextElement text = (TextElement) fragment;
				if (resolvedBinding) {
					if (previousBinding == null) {
						assertTrue("Reference in '"+previousFragment+"' should be bound!", text.getText().trim().startsWith("Unknown"));
					} else {
						assertFalse("Unknown reference in'"+previousFragment+"' should NOT be bound!", text.getText().trim().startsWith("Unknown"));
					}
				}
				previousBinding = null;
				resolvedBinding = false;
			} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
				assertMatchBindings((TagElement) fragment);
				previousBinding = null;
				resolvedBinding = false;
			} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
				previousBinding = ((MemberRef)fragment).resolveBinding();
				resolvedBinding = true;
			} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
				previousBinding = ((MethodRef) fragment).resolveBinding();
				resolvedBinding = true;
			}
			previousFragment = fragment;
		}
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void testJavadoc00() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "testJ00", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String sourceStr = sourceUnit.getSource();
		char[] source = sourceStr.toCharArray();
		ASTNode result = runConversion(sourceUnit, true); // resolve bindings
		ASTNode node = getASTNode((CompilationUnit) result, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		Javadoc javadoc = method.getJavadoc();
		List[] commentsInfos = convertSource(source);
		assertEquals("Invalid javadoc fragments number", ((Integer)commentsInfos[0].get(0)).intValue(), javadoc.tags().size());
		String modifiedSource = (String)commentsInfos[1].get(0);
		assertEquals("Flattened javadoc does NOT match source!", modifiedSource, javadoc.toString());
		assertMatchPositions(javadoc, source);
		assertMatchBindings(javadoc);
	}

	/**
	 * Check javadoc for invalid syntax
	 */
	public void testJavadoc01() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "testJ01", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String sourceStr = sourceUnit.getSource();
		char[] source = sourceStr.toCharArray();
		CompilationUnit unit = (CompilationUnit) runConversion(sourceUnit, true); // resolve bindings
		assertTrue("Wrong number of comments", unit.getCommentTable().length == 2);
	}
}
