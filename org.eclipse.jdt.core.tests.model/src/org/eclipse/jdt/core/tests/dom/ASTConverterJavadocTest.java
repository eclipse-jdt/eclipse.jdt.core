/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

public class ASTConverterJavadocTest extends ConverterTestSetup {
//		List comments = new ArrayList();
//		List allTags = new ArrayList();

		/**
	 * @param name
	 */
	public ASTConverterJavadocTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterJavadocTest.class.getName());		

		if (true) {
			Class c = ASTConverterJavadocTest.class;
			Method[] methods = c.getMethods();
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterJavadocTest(methods[i].getName()));
				}
			}
			return suite;
		}
		suite.addTest(new ASTConverterJavadocTest("testJavadoc05"));			
		return suite;
	}

//	/*
//	 * Convert Javadoc source to match Javadoc.toString()
//	 */
//	List[] convertSource(char[] source) {
//		List comments = new ArrayList();
//		List numbers = new ArrayList();
//		StringBuffer buffer = new StringBuffer(source.length);
//		boolean javadoc = false;
//		boolean star = false;
//		boolean firstTag = true;
//		int number = 0;
//		for (int i=0; i<source.length; i++) {
//			if (javadoc) {
//				if (star) {
//					if (source[i] == '/') {
//						javadoc = false;
//						buffer.append("\n */");
//						comments.add(buffer.toString());
//						numbers.add(new Integer(number));
//						buffer = new StringBuffer(source.length);
//						number = 0;
//						break;
//					} else {
//						buffer.append('*');
//					}
//				}
//			} else {
//				if (source[i] == '/' && source[i+1] == '*' && source[i+2] == '*') {
//					javadoc = true;
//				}
//			}
//			if (javadoc) {
//				if (source[i] == '\r' || source[i] == '\n') {
//					while (source[i] == '*' || Character.isWhitespace(source[i])) {
//						star = source[i++] == '*';
//					}
//					if (star && source[i] == '/') {
//						javadoc = false;
//						buffer.append("\n */");
//						comments.add(buffer.toString());
//						numbers.add(new Integer(number));
//						buffer = new StringBuffer(source.length);
//						number = 0;
//						continue;
//					}
//					if (firstTag) {
//						firstTag = false;
//						number++;
//					} else if (source[i] == '@') {
//						number++;
//					}
//					buffer.append("\n * ");
//				} else {
//					star = source[i] == '*';
//				}
//				if (!star) buffer.append(source[i]);
//			}
//		}
//		List[] lists = new List[2];
//		lists[0] = numbers;
//		lists[1] = comments;
//		return lists;
//	}

	/*
	 * Convert Javadoc source to match Javadoc.toString()
	 */
	Hashtable setSourceComment(char[] source) {
		Hashtable comments = new Hashtable();
//		this.comments = new ArrayList();
//		this.allTags = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		int comment = 0;
		boolean end = false;
		String tag = null;
		List tags = new ArrayList();
		for (int i=0; i<source.length; i++) {
			if (comment == 0) {
				if (source[i] == '/') {
					switch (source[i+1]) {
						case '/':
							comment = 1; // line comment
							break;
						case '*':
							if (source[i+2] == '*') {
								comment = 3; // javadoc
							} else {
								comment = 2; // block comment
							}
							break;
					}
				}
			}
			switch (comment) {
				case 1: // line comment
					if (source[i] == '\r' || source[i] == '\n') {
						comment = 0;
						comments.put(buffer.toString(), tags);
						buffer = new StringBuffer();
					} else {
						buffer.append(source[i]);
					}
					break;
				case 3: // javadoc comment
					if (tag != null) {
						if (source[i] >= 'a' && source[i] <= 'z') {
							tag += source[i];
						} else {
							tags.add(tag);
							tag = null;
						}
					}
					if (source[i] == '@') {
						tag = "";
					}
				case 2: // block comment
					if (comment == 2 || source[i] != '\r') {
						buffer.append(source[i]);
					}
					if (end && source[i] == '/') {
						comment = 0;
						comments.put(buffer.toString(), tags);
						buffer = new StringBuffer();
						tags = new ArrayList();
					}
					end = source[i] == '*';
					break;
				default:
					// do nothing
					break;
			}
		}
		return comments;
	}
	int allTags(Javadoc docComment) {
		int all = 0;
		// Count main tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			TagElement tagElement = (TagElement) tags.next();
			if (tagElement.getTagName() != null) {
				all++;
			}
			Iterator fragments = tagElement.fragments().listIterator();
			while (fragments.hasNext()) {
				ASTNode node = (ASTNode) fragments.next();
				if (node.getNodeType() == ASTNode.TAG_ELEMENT) {
					all++;
				}
			}
		}
		return all;
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
	protected void verifyFragmentsPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int start = docComment.getStartPosition();
		assertTrue("Misplaced javadoc start", source[start] == '/' && source[start+1] == '*' && source[start+2] == '*');
		int end = start+docComment.getLength()-1;
		assertTrue("Wrong javadoc length", end<source.length);
		assertTrue("Misplaced javadoc end", source[end-1] == '*' && source[end] == '/');
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			verifyFragmentsPositions((TagElement) tags.next(), source);
		}
	}

	/*
	 * Verify that objects of Javadoc tag structure match their source positions
	 */
	protected void verifyFragmentsPositions(TagElement tagElement, char[] source) {
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
				verifyFragmentsPositions((TagElement) fragment, source);
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
	protected void verifyFragmentsPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int index = docComment.getStartPosition();
		assertTrue("Misplaced javadoc start", source[index++] == '/' && source[index++] == '*' && source[index++] == '*');
		int end = docComment.getEndPosition();
		assertTrue("Wrong javadoc length", end<source.length);
		assertTrue("Misplaced javadoc end", source[end-1] == '*' && source[end] == '/');
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			verifyFragmentsPositions((TagElement) tags.next(), source, index);
		}
	}

	protected void verifyFragmentsPositions(TagElement tagElement, char[] source, int index) {
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
				verifyFragmentsPositions((TagElement) fragment, source, index);
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
	protected void verifyBindings(Javadoc docComment) {
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			verifyBindings((TagElement) tags.next());
		}
	}

	/*
	 * Verify that bindings of Javadoc tag structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	protected void verifyBindings(TagElement tagElement) {
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
				verifyBindings((TagElement) fragment);
				previousBinding = null;
				resolvedBinding = false;
			} else {
				resolvedBinding = true;
				if (fragment.getNodeType() == ASTNode.SIMPLE_NAME) {
					previousBinding = ((Name)fragment).resolveBinding();
				} else if (fragment.getNodeType() == ASTNode.SIMPLE_NAME || fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					QualifiedName name = (QualifiedName) fragment;
					previousBinding = name.resolveBinding();
					verifyName(name.getQualifier());
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					previousBinding = memberRef.resolveBinding();
					previousFragment = fragment;
					if (previousBinding != null) {
						assertNotNull(memberRef.getName()+" binding was not found!", memberRef.getName().resolveBinding());
						verifyName(memberRef.getQualifier());
					}
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					previousBinding = methodRef.resolveBinding();
					if (previousBinding != null) {
						assertNotNull(methodRef.getName()+" binding was not found!", methodRef.getName().resolveBinding());
						verifyName(methodRef.getQualifier());
						verifyParameters(methodRef.parameters());
					}
				}
				previousFragment = fragment;
			}
		}
		assertTrue("Reference in '"+previousFragment+"' should be bound!", (!resolvedBinding || previousBinding != null));
	}
	
	void verifyName(Name name) {
		if (name != null) {
			assertNotNull(name+" binding was not found!", name.resolveBinding());
			SimpleName simpleName = null;
			while (name.isQualifiedName()) {
				simpleName = ((QualifiedName) name).getName();
				assertNotNull(simpleName+" binding was not found!", simpleName.resolveBinding());
				name = ((QualifiedName) name).getQualifier();
				assertNotNull(name+" binding was not found!", name.resolveBinding());
			}
		}
	}
	
	void verifyParameters(List parametersList) {
		Iterator parameters = parametersList.listIterator();
		while (parameters.hasNext()) {
			MethodRefParameter param = (MethodRefParameter) parameters.next();
//				if (param.getName() != null) {
//					assertNotNull(param.getName()+" binding was not found!", param.getName().resolveBinding());
//				}
				assertNotNull(param.getType()+" binding was not found!", param.getType().resolveBinding());
				if (param.getType().isSimpleType()) {
					verifyName(((SimpleType)param.getType()).getName());
				}
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void verifyComments(String testNbre) throws JavaModelException {
		// Get test file
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.test"+testNbre, "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull("Test file Converter/src/javadoc/test"+testNbre+"/Test.java was not found!", sourceUnit);

		// Create DOM AST nodes hierarchy
		String sourceStr = sourceUnit.getSource();
		CompilationUnit compilUnit = (CompilationUnit) runConversion(sourceUnit, true); // resolve bindings
		Comment[] unitComments = compilUnit.getCommentTable();

		// Get comments infos from test file
		char[] source = sourceStr.toCharArray();
		Hashtable commentsInfo = setSourceComment(source);
		
		// Basic comments verification
		assertEquals("Wrong number of comments", commentsInfo.size(), unitComments.length);
		
		// Verify comments positions
		Enumeration commentStrings = commentsInfo.keys();
		Enumeration commentTags = commentsInfo.elements();
		for (int i=0; i<unitComments.length; i++) {
			String comment = (String) commentStrings.nextElement();
			List tags = (List) commentTags.nextElement();
//			int start = unitComments[i].getStartPosition();
//			assertEquals("Comment at position "+start+" does NOT match source!", comment, sourceStr.substring(start, start+unitComments[i].getLength()));
			if (unitComments[i].isDocComment()) {
				Javadoc docComment = (Javadoc)unitComments[i];
				assertEquals("Invalid tags number! ", tags.size(), allTags(docComment));
				assertEquals("Flattened javadoc does NOT match source!", comment, docComment.toString());
				verifyFragmentsPositions(docComment, source);
				verifyBindings(docComment);
			}
		}
		
		/* Verify each javadoc
		Iterator types = compilUnit.types().listIterator();
		while (types.hasNext()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) types.next();
			verifyJavadoc(typeDeclaration.getJavadoc());
		}
		*/
	}

	void verifyJavadoc(Javadoc docComment) {
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void testJavadoc00() throws JavaModelException {
		verifyComments("000");
	}

	/**
	 * Check javadoc for invalid syntax
	 */
	public void testJavadoc01() throws JavaModelException {
		verifyComments("001");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50781
	 */
	public void testJavadoc02() throws JavaModelException {
		verifyComments("002");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50784
	 */
	public void testJavadoc03() throws JavaModelException {
		verifyComments("003");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50785
	 */
	public void testJavadoc04() throws JavaModelException {
		verifyComments("004");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50838
	 */
	public void testJavadoc05() throws JavaModelException {
		verifyComments("005");
	}
}
