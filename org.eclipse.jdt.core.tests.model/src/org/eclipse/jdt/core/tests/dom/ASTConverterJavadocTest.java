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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;

public class ASTConverterJavadocTest extends ConverterTestSetup {

	// List of comments read from source of test
	List comments = new ArrayList();
	// List of tags contained in each comment read from test source.
	List allTags = new ArrayList();
	// Current compilation unit
	ICompilationUnit sourceUnit;
	// Test package binding
	protected boolean resolveBinding = true;
	protected boolean packageBinding = true;
	// Debug
	protected String prefix = "";
	protected boolean debug = false;
	protected String problems = JavaCore.IGNORE;
	protected String compilerOption = JavaCore.IGNORE;

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
		suite.addTest(new ASTConverterJavadocTest("testBug51770"));
		return suite;
	}

	class ASTConverterJavadocFlattener extends ASTVisitor {

		/**
		 * The string buffer into which the serialized representation of the AST is
		 * written.
		 */
		private StringBuffer buffer;
		
		private String comment;
		
		/**
		 * Creates a new AST printer.
		 */
		ASTConverterJavadocFlattener(String comment) {
			this.buffer = new StringBuffer();
			this.comment = comment;
		}
		
		/**
		 * Returns the string accumulated in the visit.
		 *
		 * @return the serialized 
		 */
		public String getResult() {
			return this.buffer.toString();
		}
		
		/**
		 * Resets this printer so that it can be used again.
		 */
		public void reset() {
			this.buffer.setLength(0);
		}


		/*
		 * @see ASTVisitor#visit(ArrayType)
		 */
		public boolean visit(ArrayType node) {
			node.getComponentType().accept(this);
			this.buffer.append("[]");//$NON-NLS-1$
			return false;
		}
	
		/*
		 * @see ASTVisitor#visit(BlockComment)
		 * @since 3.0
		 */
		public boolean visit(BlockComment node) {
			this.buffer.append(this.comment);
			return false;
		}
	
		/*
		 * @see ASTVisitor#visit(Javadoc)
		 */
		public boolean visit(Javadoc node) {
			// ignore deprecated node.getComment()
			this.buffer.append("/**");//$NON-NLS-1$
			ASTNode e = null;
			int start = 3;
			for (Iterator it = node.tags().iterator(); it.hasNext(); ) {
				e = (ASTNode) it.next();
				try {
					this.buffer.append(this.comment.substring(start, e.getStartPosition()-node.getStartPosition()));
					start = e.getStartPosition()-node.getStartPosition();
				} catch (IndexOutOfBoundsException ex) {
					// do nothing
				}
				e.accept(this);
				start += e.getLength();
			}
//			if (e != null) {
//				try {
					this.buffer.append(this.comment.substring(start, node.getLength()));
//				} catch (IndexOutOfBoundsException ex) {
//					// do nothing
//				}
//			} else {
//				this.buffer.append(" */");//$NON-NLS-1$
//			}
			return false;
		}
	
		/*
		 * @see ASTVisitor#visit(LineComment)
		 * @since 3.0
		 */
		public boolean visit(LineComment node) {
			this.buffer.append(this.comment);
			return false;
		}
	
		/*
		 * @see ASTVisitor#visit(MemberRef)
		 * @since 3.0
		 */
		public boolean visit(MemberRef node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
			}
			this.buffer.append("#");//$NON-NLS-1$
			node.getName().accept(this);
			return false;
		}
		
		/*
		 * @see ASTVisitor#visit(MethodRef)
		 * @since 3.0
		 */
		public boolean visit(MethodRef node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
			}
			this.buffer.append("#");//$NON-NLS-1$
			node.getName().accept(this);
			this.buffer.append("(");//$NON-NLS-1$
			for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
				MethodRefParameter e = (MethodRefParameter) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(",");//$NON-NLS-1$
				}
			}
			this.buffer.append(")");//$NON-NLS-1$
			return false;
		}
		
		/*
		 * @see ASTVisitor#visit(MethodRefParameter)
		 * @since 3.0
		 */
		public boolean visit(MethodRefParameter node) {
			node.getType().accept(this);
			if (node.getName() != null) {
				this.buffer.append(" ");//$NON-NLS-1$
				node.getName().accept(this);
			}
			return false;
		}

		/*
		 * @see ASTVisitor#visit(TagElement)
		 * @since 3.0
		 */
		public boolean visit(TagElement node) {
			Javadoc javadoc = null;
			int start = 0;
			if (node.isNested()) {
				// nested tags are always enclosed in braces
				this.buffer.append("{");//$NON-NLS-1$
				javadoc = (Javadoc) node.getParent().getParent();
				start++;
			} else {
				javadoc = (Javadoc) node.getParent();
			}
			start += node.getStartPosition()-javadoc.getStartPosition();
			if (node.getTagName() != null) {
				this.buffer.append(node.getTagName());
				start += node.getTagName().length();
			}
			for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
				ASTNode e = (ASTNode) it.next();
				try {
					this.buffer.append(this.comment.substring(start, e.getStartPosition()-javadoc.getStartPosition()));
					start = e.getStartPosition()-javadoc.getStartPosition();
				} catch (IndexOutOfBoundsException ex) {
					// do nothing
				}
				start += e.getLength();
				e.accept(this);
			}
			if (node.isNested()) {
				this.buffer.append("}");//$NON-NLS-1$
			}
			return false;
		}
		
		/*
		 * @see ASTVisitor#visit(TextElement)
		 * @since 3.0
		 */
		public boolean visit(TextElement node) {
			this.buffer.append(node.getText());
			return false;
		}

		/*
		 * @see ASTVisitor#visit(PrimitiveType)
		 */
		public boolean visit(PrimitiveType node) {
			this.buffer.append(node.getPrimitiveTypeCode().toString());
			return false;
		}
	
		/*
		 * @see ASTVisitor#visit(QualifiedName)
		 */
		public boolean visit(QualifiedName node) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
			node.getName().accept(this);
			return false;
		}

		/*
		 * @see ASTVisitor#visit(SimpleName)
		 */
		public boolean visit(SimpleName node) {
			this.buffer.append(node.getIdentifier());
			return false;
		}

		/*
		 * @see ASTVisitor#visit(SimpleName)
		 */
		public boolean visit(SimpleType node) {
			node.getName().accept(this);
			return false;
		}
	}

	/*
	 * Convert Javadoc source to match Javadoc.toString().
	 * Store converted comments and their corresponding tags respectively
	 * in this.comments and this.allTags fields
	 */
	void setSourceComment(char[] source) {
		this.comments = new ArrayList();
		this.allTags = new ArrayList();
		StringBuffer buffer = null;
		int comment = 0;
		boolean end = false, lineStarted = false;
		String tag = null;
		List tags = new ArrayList();
		for (int i=0; i<source.length; i++) {
			if (comment == 0) {
				if (source[i] == '/') {
					switch (source[++i]) {
						case '/':
							comment = 1; // line comment
							buffer = new StringBuffer("//");
							i++;
							break;
						case '*':
							if (source[++i] == '*') {
								if (source[++i] == '/') { // empty block comment
									this.comments.add("/**/");
									this.allTags.add(new ArrayList());
									i++;
								} else {
									comment = 3; // javadoc
									buffer = new StringBuffer("/**");
								}
							} else {
								comment = 2; // block comment
								buffer = new StringBuffer("/*");
							}
							break;
					}
				}
			}
			switch (comment) {
				case 1: // line comment
					buffer.append(source[i]);
					if (source[i] == '\r' || source[i] == '\n') {
						if (source[i] == '\r' && source[i+1] == '\n') {
							buffer.append(source[++i]);
						}
						comment = 0;
						this.comments.add(buffer.toString());
						this.allTags.add(tags);
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
					switch (source[i]) {
						case '@':
							if (!lineStarted || source[i-1] == '{') {
								tag = "";
								lineStarted = true;
							}
							break;
						case '\r':
						case '\n':
							lineStarted = false;
							break;
						case '*':
							break;
						default:
							if (!Character.isWhitespace(source[i])) {
								lineStarted = true;
							}
					}
				case 2: // block comment
					buffer.append(source[i]);
					if (end && source[i] == '/') {
						comment = 0;
						lineStarted = false;
						this.comments.add(buffer.toString());
						this.allTags.add(tags);
						tags = new ArrayList();
					}
					end = source[i] == '*';
					break;
				default:
					// do nothing
					break;
			}
		}
	}
	
	/*
	 * Return all tags number for a given Javadoc
	 */
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
	protected boolean compare(String str, int start, int length, char[] source) {
		if (str.length() != length) return false;
		for (int i=0; i<length; i++) {
			if (str.charAt(i) != source[start+i]) return false;
		}
		return true;
	}

	/*
	 * Verify positions of tags in source
	 */
	private void verifyPositions(Javadoc docComment, char[] source) {
		// Verify javadoc start and end position
		int start = docComment.getStartPosition();
		int end = start+docComment.getLength()-1;
		assertTrue(this.prefix+"Misplaced javadoc start", source[start++] == '/' && source[start++] == '*' && source[start++] == '*');
		// Get first meaningful character
		int tagStart = start;
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
				tagStart++; // purge non-stored characters
			}
			TagElement tagElement = (TagElement) tags.next();
			assertEquals(this.prefix+"Tag element has wrong start position", tagStart, tagElement.getStartPosition());
//			int tagEnd = tagStart+tagElement.getLength()-1;
			verifyPositions(tagElement, source);
			tagStart += tagElement.getLength();
		}
		while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
			tagStart++; // purge non-stored characters
		}
		assertTrue(this.prefix+"Misplaced javadoc end", source[tagStart-1] == '*' && source[tagStart] == '/');
		assertEquals(this.prefix+"Wrong javadoc length", tagStart, end);
	}

	/*
	 * Verify positions of fragments in source
	 */
	private void verifyPositions(TagElement tagElement, char[] source) {
		String text = null;
		// Verify tag name
		String tagName = tagElement.getTagName();
		int tagStart = tagElement.getStartPosition();
		if (tagElement.isNested()) {
			assertEquals(this.prefix+"Wrong start position for "+tagElement, '{', source[tagStart++]);
		}
		if (tagName != null) {
			text= new String(source, tagStart, tagName.length());
			assertEquals(this.prefix+"Misplaced tag name at "+tagStart, tagName, text);
			tagStart += tagName.length();
		}
		// Verify each fragment
		ASTNode previousFragment = null;
		Iterator elements = tagElement.fragments().listIterator();
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				if (previousFragment != null) {
					if (previousFragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
						assertTrue(this.prefix+"Wrong length for text element "+previousFragment, source[tagStart] == '\r' || source[tagStart] == '\n');
						while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
							tagStart++; // purge non-stored characters
						}
					} else {
						int start = tagStart;
						boolean newLine = false;
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
							if (source[tagStart] == '\r' || source[tagStart] == '\n') {
								newLine = true;
							}
						}
						if (newLine) tagStart = start;
					}
				}
				text = new String(source, tagStart, fragment.getLength());
				assertEquals(this.prefix+"Misplaced or wrong text element at "+tagStart, text, ((TextElement) fragment).getText());
			} else {
				while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
					tagStart++; // purge non-stored characters
				}
				if (fragment.getNodeType() == ASTNode.SIMPLE_NAME || fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					verifyNamePositions(tagStart, (Name) fragment, source);
				} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
					TagElement inlineTag = (TagElement) fragment;
					assertEquals(this.prefix+"Tag element has wrong start position", tagStart, inlineTag.getStartPosition());
					verifyPositions(inlineTag, source);
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					// Store start position
					int start = tagStart;
					// Verify qualifier position
					Name qualifier = memberRef.getQualifier();
					if (qualifier != null) {
						text = new String(source, start, qualifier.getLength());
						assertEquals(this.prefix+"Misplaced or wrong member ref qualifier at "+start, text, qualifier.toString());
						verifyNamePositions(start, qualifier, source);
						start += qualifier.getLength();
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
						}
					}
					// Verify member separator position
					assertEquals(this.prefix+"Misplace # separator for member ref"+memberRef, '#', source[start]);
					start++;
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify member name position
					Name name = memberRef.getName();
					text = new String(source, start, name.getLength());
					assertEquals(this.prefix+"Misplaced or wrong member ref name at "+start, text, name.toString());
					verifyNamePositions(start, name, source);
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					// Store start position
					int start = tagStart;
					// Verify qualifier position
					Name qualifier = methodRef.getQualifier();
					if (qualifier != null) {
						text = new String(source, start, qualifier.getLength());
						assertEquals(this.prefix+"Misplaced or wrong member ref qualifier at "+start, text, qualifier.toString());
						verifyNamePositions(start, qualifier, source);
						start += qualifier.getLength();
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
						}
					}
					// Verify member separator position
					assertEquals(this.prefix+"Misplaced # separator for member ref"+methodRef, '#', source[start]);
					start++;
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify member name position
					Name name = methodRef.getName();
					text = new String(source, start, name.getLength());
					assertEquals(this.prefix+"Misplaced or wrong member ref name at "+start, text, name.toString());
					verifyNamePositions(start, name, source);
					start += name.getLength();
					// Verify arguments starting open parenthesis
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					assertEquals(this.prefix+"Misplaced ( for member ref arguments "+methodRef, '(', source[start]);
					start++;
					// Verify parameters
					Iterator parameters = methodRef.parameters().listIterator();
					while (parameters.hasNext()) {
						MethodRefParameter param = (MethodRefParameter) parameters.next();
						// Verify parameter type positions
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							 start++; // purge non-stored characters
						}
						Type type = param.getType();
						text = new String(source, start, type.getLength());
						assertEquals(this.prefix+"Misplaced or wrong method ref parameter type at "+start, text, type.toString());
						if (type.isSimpleType()) {
							verifyNamePositions(start, ((SimpleType)type).getName(), source);
						}
						start += type.getLength();
						// Verify parameter name positions
						while (Character.isWhitespace(source[start])) { // do NOT accept '*' in parameter declaration
							 start++; // purge non-stored characters
						}
						name = param.getName();
						if (name != null) {
							text = new String(source, start, name.getLength());
							assertEquals(this.prefix+"Misplaced or wrong method ref parameter name at "+start, text, name.toString());
							start += name.getLength();
						}
						// Verify end parameter declaration
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++;
						}
						assertTrue(this.prefix+"Misplaced or wrong method ref parameter end at "+start, source[start] == ',' || source[start] == ')');
						start++;
						if (source[start] == ')') {
							break;
						}
					}
				}
			}
			tagStart += fragment.getLength();
			previousFragment = fragment;
		}
		if (tagElement.isNested()) {
			assertEquals(this.prefix+"Wrong end character for "+tagElement, '}', source[tagStart++]);
		}
	}

	/*
	 * Verify each name component positions.
	 */
	private void verifyNamePositions(int nameStart, Name name, char[] source) {
		if (name.isQualifiedName()) {
			QualifiedName qualified = (QualifiedName) name;
			String str = new String(source, qualified.getName().getStartPosition(), qualified.getName().getLength());
			assertEquals(this.prefix+"Misplaced or wrong name component "+name, str, qualified.getName().toString());
			verifyNamePositions(nameStart, ((QualifiedName) name).getQualifier(), source);
		}
		String str = new String(source, nameStart, name.getLength());
		assertEquals(this.prefix+"Misplaced or wrong name component at "+nameStart, str, name.toString());
	}

	/*
	 * Verify that bindings of Javadoc comment structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	private void verifyBindings(Javadoc docComment) {
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
	private void verifyBindings(TagElement tagElement) {
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		IBinding previousBinding = null;
		boolean resolvedBinding = false;
//		ASTNode previousFragment = null;
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				TextElement text = (TextElement) fragment;
				if (resolvedBinding) {
					if (previousBinding == null) {
						assertTrue(this.prefix+"Reference in '"+tagElement+"' should be bound!", text.getText().trim().startsWith("Unknown"));
					} else {
						assertFalse(this.prefix+"Unknown reference in '"+tagElement+"' should NOT be bound!", text.getText().trim().startsWith("Unknown"));
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
				} else if (fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					QualifiedName name = (QualifiedName) fragment;
					previousBinding = name.resolveBinding();
//					verifyNameBindings(name.getQualifier());
					verifyNameBindings(name);
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					previousBinding = memberRef.resolveBinding();
//					previousFragment = fragment;
					if (previousBinding != null) {
						assertNotNull(this.prefix+""+memberRef.getName()+" binding was not found!", memberRef.getName().resolveBinding());
						verifyNameBindings(memberRef.getQualifier());
					}
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					previousBinding = methodRef.resolveBinding();
					if (previousBinding != null) {
						assertNotNull(this.prefix+""+methodRef.getName()+" binding was not found!", methodRef.getName().resolveBinding());
						verifyNameBindings(methodRef.getQualifier());
						Iterator parameters = methodRef.parameters().listIterator();
						while (parameters.hasNext()) {
							MethodRefParameter param = (MethodRefParameter) parameters.next();
							assertNotNull(this.prefix+""+param.getType()+" binding was not found!", param.getType().resolveBinding());
							if (param.getType().isSimpleType()) {
								verifyNameBindings(((SimpleType)param.getType()).getName());
							}
							//	Do not verify parameter name as no binding is expected for them
						}
					}
				}
//				previousFragment = fragment;
			}
		}
		assertTrue(this.prefix+"Reference in '"+tagElement+"' should be bound!", (!resolvedBinding || previousBinding != null));
	}

	/*
	 * Verify each name component binding.
	 */
	private void verifyNameBindings(Name name) {
		if (name != null) {
			IBinding binding = name.resolveBinding();
			if (name.toString().indexOf("Unknown") > 0) {
				assertNull(this.prefix+name+" binding should be null!", binding);
			} else {
				assertNotNull(this.prefix+name+" binding was not found!", binding);
			}
			SimpleName simpleName = null;
			int index = 0;
			while (name.isQualifiedName()) {
				simpleName = ((QualifiedName) name).getName();
				binding = simpleName.resolveBinding();
				if (simpleName.getIdentifier().equalsIgnoreCase("Unknown")) {
					assertNull(this.prefix+simpleName+" binding should be null!", binding);
				} else {
					assertNotNull(this.prefix+simpleName+" binding was not found!", binding);
				}
				if (index > 0 && this.packageBinding) {
					assertEquals(this.prefix+"Wrong binding type", IBinding.PACKAGE, binding.getKind());
				}
				index++;
				name = ((QualifiedName) name).getQualifier();
				binding = name.resolveBinding();
				if (name.toString().indexOf("Unknown") > 0) {
					assertNull(this.prefix+name+" binding should be null!", binding);
				} else {
					assertNotNull(this.prefix+name+" binding was not found!", binding);
				}
				if (this.packageBinding) {
					assertEquals(this.prefix+"Wrong binding type", IBinding.PACKAGE, binding.getKind());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void verifyComments(ICompilationUnit unit) throws JavaModelException {
		// Get test file
		this.sourceUnit = unit;
		this.prefix = unit.getElementName()+": ";

		// Create DOM AST nodes hierarchy
		String sourceStr = this.sourceUnit.getSource();
		IJavaProject project = this.sourceUnit.getJavaProject();
		Map originalOptions = project.getOptions(true);
		Comment[] unitComments = null;
		try {
				project.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, this.compilerOption);
				project.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, this.compilerOption);
				project.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, this.compilerOption);
				project.setOption(JavaCore.COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME, JavaCore.IGNORE);
				CompilationUnit compilUnit = (CompilationUnit) runConversion(this.sourceUnit, this.resolveBinding); // resolve bindings
				if (problems.equals(JavaCore.ERROR)) {
					assertEquals(this.prefix+"Unexpected problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
				} else if (problems.equals(JavaCore.WARNING)) {
					IProblem[] problemsList = compilUnit.getProblems();
					int length = problemsList.length;
					System.out.println(this.prefix+length+" unexpected problems:"); //$NON-NLS-1$
					for (int i = 0; i < problemsList.length; i++) {
						System.out.println("  - "+problemsList[i]);
					}
				}
				unitComments = compilUnit.getCommentTable();
		} finally {
			project.setOptions(originalOptions);
		}
		assertNotNull(this.prefix+"Unexpected problems", unitComments);
		
		// Verify source regardings converted comments
		char[] source = sourceStr.toCharArray();
		verifyComments(sourceStr, source, unitComments);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void verifyComments(String sourceStr, char[] source, Comment[] unitComments) throws JavaModelException {
		// Get comments infos from test file
		setSourceComment(source);
		
		// Basic comments verification
		assertEquals(this.prefix+"Wrong number of comments in source:\n"+sourceStr+"\n", this.comments.size(), unitComments.length);
		
		// Verify comments positions and bindings
		for (int i=0; i<unitComments.length; i++) {
			Comment comment = unitComments[i];
			List tags = (List) allTags.get(i);
			// Verify flattened content
			String stringComment = (String) this.comments.get(i);
			ASTConverterJavadocFlattener printer = new ASTConverterJavadocFlattener(stringComment);
			comment.accept(printer);
			String text = new String(source, comment.getStartPosition(), comment.getLength());
			assertEquals(this.prefix+"Flattened comment does NOT match source!", stringComment, text);
			// Verify javdoc tags positions and bindings
			if (comment.isDocComment()) {
				Javadoc docComment = (Javadoc)comment;
				assertEquals(this.prefix+"Invalid tags number in javadoc:\n"+docComment+"\n", tags.size(), allTags(docComment));
				verifyPositions(docComment, source);
				if (this.resolveBinding) {
					verifyBindings(docComment);
				}
			}
		}
		
		/* Verify each javadoc: not implemented yet
		Iterator types = compilUnit.types().listIterator();
		while (types.hasNext()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) types.next();
			verifyJavadoc(typeDeclaration.getJavadoc());
		}
		*/
	}

	/* 
	 * Verify each javadoc
	 * Not implented yet
	private void verifyJavadoc(Javadoc docComment) {
	}
	*/

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void verifyComments(String test) throws JavaModelException {
		ICompilationUnit[] units = getCompilationUnits("Converter" , "src", "javadoc."+test); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<units.length; i++) {
			verifyComments(units[i]);
		}
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void testJavadoc000() throws JavaModelException {
		verifyComments("test000");
	}

	/**
	 * Check javadoc for invalid syntax
	 */
	public void testJavadoc001() throws JavaModelException {
		verifyComments("test001");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50781
	 */
	public void testJavadoc002() throws JavaModelException {
		verifyComments("test002");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50784
	 */
	public void testJavadoc003() throws JavaModelException {
		verifyComments("test003");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50785
	 */
	public void testJavadoc004() throws JavaModelException {
		verifyComments("test004");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50838
	 */
	public void testJavadoc005() throws JavaModelException {
		verifyComments("test005");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc006() throws JavaModelException {
		verifyComments("test006");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc007() throws JavaModelException {
		verifyComments("test007");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc008() throws JavaModelException {
		verifyComments("test008");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877
	 */
	public void testJavadoc009() throws JavaModelException {
		verifyComments("test009");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50880
	 */
	public void testJavadoc010() throws JavaModelException {
		verifyComments("test010");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47396
	 */
	public void testJavadoc011() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.test011", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, true);
		assertNotNull("No compilation unit", result);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50938
	 */
	public void testJavadoc012() throws JavaModelException {
		verifyComments("test012");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51104
	 */
	public void testJavadoc013() throws JavaModelException {
		verifyComments("test013");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48489
	 */
	public void testBug48489() throws JavaModelException {
		verifyComments("testBug48489");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50898
	 */
	public void testBug50898() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "javadoc.testBug50898", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.packageBinding = false;
		verifyComments(unit);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51226
	 */
	public void testBug51226() throws JavaModelException {
		ICompilationUnit[] units = getCompilationUnits("Converter" , "src", "javadoc.testBug51226"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<units.length; i++) {
			ASTNode result = runConversion(units[i], false);
			final CompilationUnit unit = (CompilationUnit) result;
			assertEquals(this.prefix+"Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
			assertEquals(this.prefix+"Wrong number of comments", 1, unit.getCommentTable().length);
			Comment comment = unit.getCommentTable()[0];
			assertTrue(this.prefix+"Comment should be a Javadoc one", comment.isDocComment());
			Javadoc docComment = (Javadoc) comment;
			assertEquals(this.prefix+"Wrong number of tags", 1, docComment.tags().size());
			TagElement tagElement = (TagElement) docComment.tags().get(0);
			assertNull(this.prefix+"Wrong type of tag ["+tagElement+"]", tagElement.getTagName());
			assertEquals(this.prefix+"Wrong number of fragments in tag ["+tagElement+"]", 1, tagElement.fragments().size());
			ASTNode fragment = (ASTNode) tagElement.fragments().get(0);
			assertEquals(this.prefix+"Invalid type for fragment ["+fragment+"]", ASTNode.TEXT_ELEMENT, fragment.getNodeType());
			TextElement textElement = (TextElement) fragment;
			assertEquals(this.prefix+"Invalid content for text element ", "Test", textElement.getText());
			if (debug) System.out.println(docComment+"\nsuccessfully verified.");
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51241
	 */
	public void testBug51241() throws JavaModelException {
		verifyComments("testBug51241");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51363
	 */
	public void testBug51363() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug51363", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals(this.prefix+"Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		assertEquals(this.prefix+"Wrong number of comments", 2, unit.getCommentTable().length);
		// verify first comment
		Comment comment = unit.getCommentTable()[0];
		assertTrue(this.prefix+"Comment should be a line comment ", comment.isLineComment());
		String sourceStr = this.sourceUnit.getSource();
		int startPos = comment.getStartPosition()+comment.getLength();
		assertEquals("Wrong length for line comment "+comment, "public", sourceStr.substring(startPos, startPos+6));
		if (debug) System.out.println(comment+"\nsuccessfully verified.");
		// verify second comment
		comment = unit.getCommentTable()[1];
		assertTrue(this.prefix+"Comment should be a line comment", comment.isLineComment());
		sourceStr = this.sourceUnit.getSource();
		startPos = comment.getStartPosition()+comment.getLength();
		assertEquals("Wrong length for line comment "+comment, "void", sourceStr.substring(startPos, startPos+4));
		if (debug) System.out.println(comment+"\nsuccessfully verified.");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51476
	 */
	public void testBug51476() throws JavaModelException {
		verifyComments("testBug51476");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51478
	 */
	public void testBug51478() throws JavaModelException {
		verifyComments("testBug51478");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51508
	 */
	public void testBug51508() throws JavaModelException {
		verifyComments("testBug51508");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51650
	 */
	public void testBug51650() throws JavaModelException {
		verifyComments("testBug51650");
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51770
	 */
	public void testBug51770() throws JavaModelException {
		verifyComments("testBug51770");
	}
}
