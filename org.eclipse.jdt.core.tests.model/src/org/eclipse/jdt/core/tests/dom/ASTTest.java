/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial test suite for AST API 
 ******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;


import org.eclipse.jdt.core.dom.*;

// testing

public class ASTTest extends TestCase { 


	
	public static Test suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTTest.class.getName());
		
		Class c = ASTTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) {
				suite.addTest(new ASTTest(methods[i].getName()));
			}
		}
		return suite;
	}	
	
	AST ast;
	
	public ASTTest(String name) {
		super(name);
	}
	
	protected void setUp() {
		ast = new AST();
	}
	
	protected void tearDown() {
		ast = null;
	}
	
	
	/**
	 * Snippets that show how to...
	 */
	public void testExampleSnippets() {
		{
			AST ast = new AST();
			CompilationUnit cu = ast.newCompilationUnit();

			// package com.example;
			PackageDeclaration pd = ast.newPackageDeclaration();
			pd.setName(ast.newName(new String[]{"com", "example"}));
			cu.setPackage(pd);
			assertTrue(pd.getRoot() == cu);

			// import java.io;*;
			ImportDeclaration im1 = ast.newImportDeclaration();
			im1.setName(ast.newName(new String[]{"java", "io"}));
			im1.setOnDemand(true);
			cu.imports().add(im1);
			assertTrue(im1.getRoot() == cu);
			
			// import java.util.List;
			ImportDeclaration im2 = ast.newImportDeclaration();
			im2.setName(ast.newName(new String[]{"java", "util", "List"}));
			im2.setOnDemand(false);
			cu.imports().add(im2);
			assertTrue(im2.getRoot() == cu);
			
			// public class MyClass {}
			TypeDeclaration td = ast.newTypeDeclaration();
			td.setModifiers(Modifier.PUBLIC);
			td.setInterface(false); 
			td.setName(ast.newSimpleName("MyClass"));
			cu.types().add(td);
			assertTrue(td.getRoot() == cu);
			
			// private static boolean DEBUG = true;
			VariableDeclarationFragment f1 = ast.newVariableDeclarationFragment();
			f1.setName(ast.newSimpleName("DEBUG"));
			f1.setInitializer(ast.newBooleanLiteral(true));
			FieldDeclaration fd = ast.newFieldDeclaration(f1);
			fd.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
			fd.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
			td.bodyDeclarations().add(fd);
			assertTrue(fd.getRoot() == cu);
			
			// public static void main();
			MethodDeclaration md = ast.newMethodDeclaration();
			md.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
			md.setConstructor(false);
			md.setName(ast.newSimpleName("main"));
			md.setReturnType(ast.newPrimitiveType(PrimitiveType.VOID));
			td.bodyDeclarations().add(md);
			assertTrue(md.getRoot() == cu);
			
			// String[] args
			SingleVariableDeclaration a1 = ast.newSingleVariableDeclaration();
			a1.setType(ast.newArrayType(
				ast.newSimpleType(ast.newSimpleName("String"))));
			a1.setName(ast.newSimpleName("args"));
			md.parameters().add(a1);
			assertTrue(a1.getRoot() == cu);
			
			// {}
			Block b = ast.newBlock();
			md.setBody(b);
			assertTrue(b.getRoot() == cu);

			// System.out.println("hello world");		
			MethodInvocation e = ast.newMethodInvocation();
			e.setExpression(ast.newName(new String[] {"System", "out"}));
			e.setName(ast.newSimpleName("println"));
			StringLiteral h = ast.newStringLiteral();
			h.setLiteralValue("hello world");
			e.arguments().add(h);
			
			b.statements().add(ast.newExpressionStatement(e));
			assertTrue(e.getRoot() == cu);
			assertTrue(h.getRoot() == cu);
			
			// new String[len]
			ArrayCreation ac1 = ast.newArrayCreation();
			ac1.setType(
				ast.newArrayType(
					ast.newSimpleType(ast.newSimpleName("String"))));
			ac1.dimensions().add(ast.newSimpleName("len"));
			b.statements().add(ast.newExpressionStatement(ac1));
			assertTrue(ac1.getRoot() == cu);

			// new double[7][24][]
			ArrayCreation ac2 = ast.newArrayCreation();
			ac2.setType(
				ast.newArrayType(
					ast.newPrimitiveType(PrimitiveType.DOUBLE), 3));
			ac2.dimensions().add(ast.newNumberLiteral("7"));
			ac2.dimensions().add(ast.newNumberLiteral("24"));
			b.statements().add(ast.newExpressionStatement(ac2));
			assertTrue(ac2.getRoot() == cu);

			// new int[] {1, 2}
			ArrayCreation ac3 = ast.newArrayCreation();
			ac3.setType(
				ast.newArrayType(
					ast.newPrimitiveType(PrimitiveType.INT)));
			ArrayInitializer ai = ast.newArrayInitializer();
			ac3.setInitializer(ai);
			ai.expressions().add(ast.newNumberLiteral("1"));
			ai.expressions().add(ast.newNumberLiteral("2"));
			b.statements().add(ast.newExpressionStatement(ac3));
			assertTrue(ac3.getRoot() == cu);
			assertTrue(ai.getRoot() == cu);
			
			// new String(10)
			ClassInstanceCreation cr1 = ast.newClassInstanceCreation();
			cr1.setName(ast.newSimpleName("String"));
			cr1.arguments().add(ast.newNumberLiteral("10"));		
			b.statements().add(ast.newExpressionStatement(cr1));
			assertTrue(cr1.getRoot() == cu);

			// new Listener() {public void handleEvent() {} }
			ClassInstanceCreation cr2 = ast.newClassInstanceCreation();
			AnonymousClassDeclaration ad1 = ast.newAnonymousClassDeclaration();
			cr2.setAnonymousClassDeclaration(ad1);
			cr2.setName(ast.newSimpleName("Listener"));
			MethodDeclaration md0 = ast.newMethodDeclaration();
			md0.setModifiers(Modifier.PUBLIC);
			md0.setName(ast.newSimpleName("handleEvent"));
			md0.setBody(ast.newBlock());
			ad1.bodyDeclarations().add(md0);
			b.statements().add(ast.newExpressionStatement(cr2));
			assertTrue(cr2.getRoot() == cu);
			assertTrue(md0.getRoot() == cu);
			assertTrue(ad1.getRoot() == cu);

		}
	}
	
	abstract class Property {
		
		/**
		 * The property name.
		 */
		private String propertyName;
		
		/**
		 * Indicates whether this property is compulsory, in that every node
		 * must have a value at all times.
		 */
		private boolean compulsory;
		
		/**
		 * The type of node allowed as a child.
		 */
		private Class nodeType;

		/**
		 * Creates a new property with the given name.
		 */
		Property(String propertyName, boolean compulsory, Class nodeType) {
			this.propertyName = propertyName;
			this.compulsory = compulsory;
			this.nodeType = nodeType;
		}
		
		/**
		 * Returns a sample node of a type suitable for storing
		 * in this property.
		 * 
		 * @param ast the target AST
		 * @param parented <code>true</code> if the sample should be
		 *    parented, and <code>false</code> if unparented
		 * @return a sample node
		 */
		public abstract ASTNode sample(AST ast, boolean parented);

		/**
		 * Returns a sample node of a type suitable for storing
		 * in this property. The sample embeds the node itself.
		 * <p>
		 * For instance, for an Expression-valued property of a given
		 * Statement, this method returns an Expression that embeds
		 * this Statement node (as a descendent).
		 * </p>
		 * <p>
		 * Returns <code>null</code> if such an embedding is impossible.
		 * For instance, for an Name-valued property of a given
		 * Statement, this method returns <code>null</code> because
		 * an Expression cannot be embedded in a Name.
		 * </p>
		 * <p>
		 * This implementation returns <code>null</code>. Subclasses
		 * should reimplement to specify an embedding.
		 * </p>
		 * 
		 * @param ast the target AST
		 * @return a sample node that embeds the given node,
		 *    and <code>null</code> if such an embedding is impossible
		 */
		public ASTNode wrap() {
			return null;
		}
		
		/**
		 * Undoes the effects of a previous <code>wrap</code>.
		 * <p>
		 * This implementation does nothing. Subclasses
		 * should reimplement if they reimplement <code>wrap</code>.
		 * </p>
		 * 
		 * @param ast the target AST
		 * @return a sample node that embeds the given node,
		 *    and <code>null</code> if such an embedding is impossible
		 */
		public void unwrap() {
		}
		
		/**
		 * Returns whether this property is compulsory, in that every node
		 * must have a value at all times.
		 * 
		 * @return <code>true</code> if the property is compulsory,
		 *    and <code>false</code> if the property may be null
		 */
		public final boolean isCompulsory() {
			return compulsory;
		}
		
		/**
		 * Returns the value of this property.
		 * <p>
		 * This implementation throws an unchecked exception. Subclasses 
		 * should reimplement.
		 * </p>
		 * 
		 * @return the property value, or <code>null</code> if no value
		 */
		public ASTNode get() {
			throw new RuntimeException("get not implemented");
		}
		
		/**
		 * Sets or clears the value of this property.
		 * <p>
		 * This implementation throws an unchecked exception. Subclasses 
		 * should reimplement.
		 * </p>
		 * 
		 * @param value the property value, or <code>null</code> if no value
		 */
		public void set(ASTNode value) {
			throw new RuntimeException("get not implemented");
		}
	}

	/**
	 * Exercises the given property of the given node.
	 * 
	 * @param node the node to test
	 * @param prop the property descriptor
	 */
	void genericPropertyTest(ASTNode node, Property prop) {
		
		ASTNode x1 = prop.sample(node.getAST(), false);
		prop.set(x1);
		assertTrue(prop.get() == x1);
		assertTrue(x1.getParent() == node);
		
		// check handling of null
		if (prop.isCompulsory()) {
			try {
				prop.set(null);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		} else {
			long previousCount = node.getAST().modificationCount();
			prop.set(null);
			assertTrue(prop.get() == null);
			assertTrue(node.getAST().modificationCount() > previousCount);
		}			

		// check that a child from a different AST is detected
		try {
			prop.set(prop.sample(new AST(), false));
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a child with a parent is detected
		try {
			ASTNode b1 = prop.sample(node.getAST(), true);
			prop.set(b1); // bogus: already has parent
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a cycle is detected
		assertTrue(node.getParent() == null);
		ASTNode s1 = null;
		try {
			s1 = prop.wrap();
			if (s1 != null) {
				prop.set(s1);  // bogus: creates a cycle
				assertTrue(false);
			}
		} catch (RuntimeException e) {
			// pass
		} finally {
			if (s1 != null) {
				prop.unwrap();
				assertTrue(node.getParent() == null);
			}
		}
	}

	/**
	 * Exercises the given property of the given node.
	 * 
	 * @param node the node to test
	 * @param children the node to test
	 * @param prop the property descriptor
	 */
	void genericPropertyListTest(ASTNode node, List children, Property prop) {
		
		// wipe the slate clean
		children.clear();
		assertTrue(children.size() == 0);
		
		// add a child
		ASTNode x1 = prop.sample(node.getAST(), false);
		long previousCount = node.getAST().modificationCount();
		children.add(x1);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 1);
		assertTrue(children.get(0) == x1);
		assertTrue(x1.getParent() == node);
		
		// add a second child
		ASTNode x2 = prop.sample(node.getAST(), false);
		previousCount = node.getAST().modificationCount();
		children.add(x2);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 2);
		assertTrue(children.get(0) == x1);
		assertTrue(children.get(1) == x2);
		assertTrue(x1.getParent() == node);
		assertTrue(x2.getParent() == node);

		// remove the first child
		previousCount = node.getAST().modificationCount();
		children.remove(0);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 1);
		assertTrue(children.get(0) == x2);
		assertTrue(x1.getParent() == null);
		assertTrue(x2.getParent() == node);

		// remove the remaining child
		previousCount = node.getAST().modificationCount();
		children.remove(x2);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 0);
		assertTrue(x1.getParent() == null);
		assertTrue(x2.getParent() == null);

		// check that null is never allowed
		try {
			children.add(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a child from a different AST is detected
		try {
			children.add(prop.sample(new AST(), false));
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a child with a parent is detected
		try {
			ASTNode b1 = prop.sample(node.getAST(), true);
			children.add(b1); // bogus: already has parent
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a cycle is detected
		assertTrue(node.getParent() == null);
		ASTNode s1 = null;
		try {
			s1 = prop.wrap();
			if (s1 != null) {
				children.add(s1);  // bogus: creates a cycle
				assertTrue(false);
			}
		} catch (RuntimeException e) {
			// pass
		} finally {
			if (s1 != null) {
				prop.unwrap();
				assertTrue(node.getParent() == null);
			}
		}
		
	}

	public void testAST() {
		
		// modification count is always non-negative
		assertTrue(ast.modificationCount() >= 0);
		
		// modification count increases for node creations
		long previousCount = ast.modificationCount();
		SimpleName x = ast.newSimpleName("first");
		assertTrue(ast.modificationCount() > previousCount);

		// modification count does not increase for reading node attributes
		previousCount = ast.modificationCount();
		x.getIdentifier();
		x.getParent();
		x.getRoot();
		x.getAST();
		x.getFlags();
		x.getStartPosition();
		x.getLength();
		x.equals(x);
		assertTrue(ast.modificationCount() == previousCount);

		// modification count does not increase for reading or writing properties
		previousCount = ast.modificationCount();
		x.getProperty("any");
		x.setProperty("any", "value"); // N.B.
		x.properties();
		assertTrue(ast.modificationCount() == previousCount);

		// modification count increases for changing node attributes
		previousCount = ast.modificationCount();
		x.setIdentifier("second");
		assertTrue(ast.modificationCount() > previousCount);
		
		previousCount = ast.modificationCount();
		x.setFlags(0);
		assertTrue(ast.modificationCount() > previousCount);
		
		previousCount = ast.modificationCount();
		x.setSourceRange(-1,0);
		assertTrue(ast.modificationCount() > previousCount);
	}	
	
	public void testWellKnownBindings() {

		// well known bindings
		String[] wkbs = {
			"byte", "char", "short", "int", "long",
			"boolean", "float", "double", "void",
			"java.lang.Object",
			"java.lang.String",
			"java.lang.StringBuffer",
			"java.lang.Throwable",
			"java.lang.Exception",
			"java.lang.RuntimeException",
			"java.lang.Error",
		};
		
		// no-so-well-known bindings
		String[] nwkbs = {
			"verylong",
			"java.lang.Math",
			"com.example.MyCode",
		};
	
		// none of the well known bindings resolve in a plain AST		
		for (int i = 0; i<wkbs.length; i++) {
			assertTrue(ast.resolveWellKnownType(wkbs[i]) == null);
		}
	
		// none of the no so well known bindings resolve either		
		for (int i = 0; i<nwkbs.length; i++) {
			assertTrue(ast.resolveWellKnownType(nwkbs[i]) == null);
		}
	}
	
	public void testSimpleName() {
		long previousCount = ast.modificationCount();
		SimpleName x = ast.newSimpleName("foo");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Name);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("foo".equals(x.getIdentifier()));
		assertTrue(x.getNodeType() == ASTNode.SIMPLE_NAME);
		assertTrue(x.isDeclaration() == false);

		previousCount = ast.modificationCount();
		x.setIdentifier("bar");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("bar".equals(x.getIdentifier()));

		// check that property cannot be set to null
		try {
			x.setIdentifier(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		
		// check that property cannot be set to keyword or reserved work
		String[] reserved  = 
				new String[] {
						"true", "false", "null", // literals
						"abstract", "default", "if", "private", "this",
						"boolean", "do", "implements", "protected", "throw",
						"break", "double", "import", "public", "throws",
						"byte", "else", "instanceof", "return", "transient",
						"case", "extends", "int", "short", "try",
						"catch", "final", "interface", "static", "void",
						"char", "finally", "long", "strictfp", "volatile",
						"class", "float", "native", "super", "while",
						"const", "for", "new", "switch",
						"continue", "goto", "package", "synchronized"};
		for (int i=0; i<reserved.length; i++) {
			try {
				x.setIdentifier(reserved[i]);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}
		
		// check that "assert" is not considered a keyword
		x.setIdentifier("assert");
		
		// check that isDeclaration works
		QualifiedName y = ast.newQualifiedName(ast.newSimpleName("a"), x);
		assertTrue(x.isDeclaration() == false);
		y.setName(ast.newSimpleName("b"));
		assertTrue(x.isDeclaration() == false);

		TypeDeclaration td = ast.newTypeDeclaration();
		td.setName(x);
		assertTrue(x.isDeclaration() == true);
		td.setName(ast.newSimpleName("b"));
		assertTrue(x.isDeclaration() == false);
		
		MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(x);
		assertTrue(x.isDeclaration() == true);
		md.setName(ast.newSimpleName("b"));
		assertTrue(x.isDeclaration() == false);
		
		SingleVariableDeclaration vd = ast.newSingleVariableDeclaration();
		vd.setName(x);
		assertTrue(x.isDeclaration() == true);
		vd.setName(ast.newSimpleName("b"));
		assertTrue(x.isDeclaration() == false);
		
		VariableDeclarationFragment fd = ast.newVariableDeclarationFragment();
		fd.setName(x);
		assertTrue(x.isDeclaration() == true);
		fd.setName(ast.newSimpleName("b"));
		assertTrue(x.isDeclaration() == false);
		
	}		

	public void testQualifiedName() {
		long previousCount = ast.modificationCount();
		final QualifiedName x = ast.newQualifiedName(
			ast.newSimpleName("q"),
			ast.newSimpleName("i"));
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Name);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getQualifier().getParent() == x);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == false);
		assertTrue(x.getNodeType() == ASTNode.QUALIFIED_NAME);

		genericPropertyTest(x, new Property("Qualifier", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"),
					targetAst.newSimpleName("b"));
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				QualifiedName s1 = ast.newQualifiedName(x, ast.newSimpleName("z"));
				return s1;
			}
			public void unwrap() {
				QualifiedName s1 = (QualifiedName) x.getParent();
				s1.setQualifier(ast.newSimpleName("z"));
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
	}		

	public void testNullLiteral() {
		long previousCount = ast.modificationCount();
		NullLiteral x = ast.newNullLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.NULL_LITERAL);
	}		

	public void testBooleanLiteral() {
		long previousCount = ast.modificationCount();
		BooleanLiteral x = ast.newBooleanLiteral(true);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.booleanValue() == true);
		assertTrue(x.getNodeType() == ASTNode.BOOLEAN_LITERAL);
		
		previousCount = ast.modificationCount();
		x.setBooleanValue(false);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.booleanValue() == false);
		
		previousCount = ast.modificationCount();
		x.setBooleanValue(true);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.booleanValue() == true);
	}		
	
	public void testStringLiteral() {
		long previousCount = ast.modificationCount();
		// check 0-arg factory first
		StringLiteral x = ast.newStringLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("\"\"".equals(x.getEscapedValue()));
		assertTrue("".equals(x.getLiteralValue()));
		assertTrue(x.getNodeType() == ASTNode.STRING_LITERAL);
		
		previousCount = ast.modificationCount();
		x.setEscapedValue("\"bye\"");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\"bye\"".equals(x.getEscapedValue()));
		assertTrue("bye".equals(x.getLiteralValue()));

		previousCount = ast.modificationCount();
		x.setLiteralValue("hi");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\"hi\"".equals(x.getEscapedValue()));
		assertTrue("hi".equals(x.getLiteralValue()));

		// check that property cannot be set to null
		try {
			x.setEscapedValue(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that property cannot be set to null
		try {
			x.setLiteralValue(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
	}		

	public void testStringLiteralUnicode() {
		AST ast = new AST();
		StringLiteral literal = ast.newStringLiteral();
		literal.setEscapedValue("\"hello\\u0026\\u0050worl\\u0064\"");
		assertTrue(literal.getLiteralValue().equals("hello&Pworld"));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setEscapedValue("\"hello\\nworld\"");
		assertTrue(literal.getLiteralValue().equals("hello\nworld"));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setLiteralValue("hello\nworld");
		assertTrue(literal.getLiteralValue().equals("hello\nworld"));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setLiteralValue("\n");
		assertTrue(literal.getEscapedValue().equals("\"\\n\""));
		assertTrue(literal.getLiteralValue().equals("\n"));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setEscapedValue("\"hello\\\"world\"");
		assertTrue(literal.getLiteralValue().equals("hello\"world"));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setLiteralValue("hello\\u0026world");
		assertTrue(literal.getLiteralValue().equals("hello\\u0026world"));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setLiteralValue("hello\\u0026world");
		assertTrue(literal.getEscapedValue().equals("\"hello\\\\u0026world\""));
		
		ast = new AST();
		literal = ast.newStringLiteral();
		literal.setLiteralValue("\\u0001");
		assertTrue(literal.getEscapedValue().equals("\"\\\\u0001\""));
	}		
	
	public void testCharacterLiteral() {
		long previousCount = ast.modificationCount();
		CharacterLiteral x = ast.newCharacterLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getEscapedValue().startsWith("\'"));
		assertTrue(x.getEscapedValue().endsWith("\'"));
		assertTrue(x.getNodeType() == ASTNode.CHARACTER_LITERAL);

		previousCount = ast.modificationCount();
		x.setEscapedValue("\'z\'");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\'z\'".equals(x.getEscapedValue()));
		assertTrue(x.charValue() == 'z');

		// test other factory method
		previousCount = ast.modificationCount();
		CharacterLiteral y = ast.newCharacterLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(y instanceof Expression);
		assertTrue(y.getAST() == ast);
		assertTrue(y.getParent() == null);
		String v = y.getEscapedValue();
		assertTrue(v.length() >= 3 && v.charAt(0) == '\'' & v.charAt(v.length()-1 ) == '\'');

		// check that property cannot be set to null
		try {
			x.setEscapedValue(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		
		// test escaped characters
		// b, t, n, f, r, ", ', \, 0, 1, 2, 3, 4, 5, 6, or 7
		try {
			x.setEscapedValue("\'\\b\'");
			x.setEscapedValue("\'\\t\'");
			x.setEscapedValue("\'\\n\'");
			x.setEscapedValue("\'\\f\'");
			x.setEscapedValue("\'\\\"\'");
			x.setEscapedValue("\'\\'\'");
			x.setEscapedValue("\'\\\\\'");
			x.setEscapedValue("\'\\0\'");
			x.setEscapedValue("\'\\1\'");
			x.setEscapedValue("\'\\2\'");
			x.setEscapedValue("\'\\3\'");
			x.setEscapedValue("\'\\4\'");
			x.setEscapedValue("\'\\5\'");
			x.setEscapedValue("\'\\6\'");
			x.setEscapedValue("\'\\7\'");
			x.setEscapedValue("\'\\u0041\'");
			assertTrue(x.charValue() == 'A');
		} catch(IllegalArgumentException e) {
			assertTrue(false);
		}
		
		x.setCharValue('\u0041');
		assertTrue(x.getEscapedValue().equals("\'A\'"));
		x.setCharValue('\t');
		assertTrue(x.getEscapedValue().equals("\'\\t\'"));
		x.setEscapedValue("\'\\\\\'");
		assertTrue(x.getEscapedValue().equals("\'\\\\\'"));
		assertTrue(x.charValue() == '\\');
		x.setEscapedValue("\'\\\'\'");
		assertTrue(x.getEscapedValue().equals("\'\\\'\'"));
		assertTrue(x.charValue() == '\'');		
		x.setCharValue('\'');
		assertTrue(x.getEscapedValue().equals("\'\\\'\'"));
		assertTrue(x.charValue() == '\'');		
		x.setCharValue('\\');
		assertTrue(x.getEscapedValue().equals("\'\\\\\'"));
		assertTrue(x.charValue() == '\\');		
	}		

	public void testNumberLiteral() {
		long previousCount = ast.modificationCount();
		NumberLiteral x = ast.newNumberLiteral("1234");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("1234".equals(x.getToken()));
		assertTrue(x.getNodeType() == ASTNode.NUMBER_LITERAL);

		// test other factory method
		previousCount = ast.modificationCount();
		NumberLiteral y = ast.newNumberLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(y.getAST() == ast);
		assertTrue(y.getParent() == null);
		assertTrue("0".equals(y.getToken()));

		final String[] samples =
			{ "0", "1", "1234567890",
			  "0L", "1L", "1234567890L",
			  "0l", "1l", "1234567890l",
			  "077", "0177", "012345670",
			  "077L", "0177L", "012345670L",
			  "077l", "0177l", "012345670l",
			  "0x00", "0x1", "0x0123456789ABCDEF",
			  "0x00L", "0x1L", "0x0123456789ABCDEFL",
			  "0x00l", "0x1l", "0x0123456789ABCDEFl",
			  "1e1f", "2.f", ".3f", "0f", "3.14f", "6.022137e+23f",
			  "1e1", "2.", ".3", "0.0", "3.14", "1e-9d", "1e137",
			};
		for (int i = 0; i < samples.length; i++) {			
			previousCount = ast.modificationCount();
			x.setToken(samples[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(samples[i].equals(x.getToken()));
		}

		// check that property cannot be set to null
		try {
			x.setToken(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

	}		

	public void testSimpleType() {
		long previousCount = ast.modificationCount();
		final SimpleType x = ast.newSimpleType(ast.newSimpleName("String"));
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Type);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(x.getNodeType() == ASTNode.SIMPLE_TYPE);

		genericPropertyTest(x, new Property("Name", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});
	}		
	
	public void testPrimitiveType() {
		long previousCount = ast.modificationCount();
		PrimitiveType x = ast.newPrimitiveType(PrimitiveType.INT);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Type);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getPrimitiveTypeCode().equals(PrimitiveType.INT));
		assertTrue(!x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(x.isPrimitiveType());
		assertTrue(x.getNodeType() == ASTNode.PRIMITIVE_TYPE);
		
		// check the names of the primitive type codes
		assertTrue(PrimitiveType.BYTE.toString().equals("byte"));
		assertTrue(PrimitiveType.INT.toString().equals("int"));
		assertTrue(PrimitiveType.BOOLEAN.toString().equals("boolean"));
		assertTrue(PrimitiveType.CHAR.toString().equals("char"));
		assertTrue(PrimitiveType.SHORT.toString().equals("short"));
		assertTrue(PrimitiveType.LONG.toString().equals("long"));
		assertTrue(PrimitiveType.FLOAT.toString().equals("float"));
		assertTrue(PrimitiveType.DOUBLE.toString().equals("double"));
		assertTrue(PrimitiveType.VOID.toString().equals("void"));

		
		PrimitiveType.Code[] known = {
			PrimitiveType.BOOLEAN,
			PrimitiveType.BYTE,
			PrimitiveType.CHAR,
			PrimitiveType.INT,
			PrimitiveType.SHORT,
			PrimitiveType.LONG,
			PrimitiveType.FLOAT,
			PrimitiveType.DOUBLE,
			PrimitiveType.VOID,
		};
		
		// check all primitive type codes are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all primitive type codes work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setPrimitiveTypeCode(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getPrimitiveTypeCode().equals(known[i]));
		}
		// ensure null does not work as a primitive type code
		try {
			x.setPrimitiveTypeCode(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toCode lookup of primitive type code by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(PrimitiveType.toCode(name).equals(known[i]));
		}
		assertTrue(PrimitiveType.toCode("not-a-type") == null);
	}		
	
	public void testArrayType() {
		SimpleName x1 = ast.newSimpleName("String");
		SimpleType x2 = ast.newSimpleType(x1);
		long previousCount = ast.modificationCount();
		final ArrayType x = ast.newArrayType(x2);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Type);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getComponentType().getParent() == x);
		assertTrue(!x.isSimpleType());
		assertTrue(x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(x.getNodeType() == ASTNode.ARRAY_TYPE);

		assertTrue(x.getDimensions() == 1);
		assertTrue(x.getElementType() == x2);

		genericPropertyTest(x, new Property("ComponentType", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("a"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ArrayType result = ast.newArrayType(x);
				return result;
			}
			public void unwrap() {
				ArrayType a = (ArrayType) x.getParent();
				a.setComponentType(ast.newPrimitiveType(PrimitiveType.INT));
			}
			public ASTNode get() {
				return x.getComponentType();
			}
			public void set(ASTNode value) {
				x.setComponentType((Type) value);
			}
		});
		
		x.setComponentType(
			ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT), 4));
			
		assertTrue(x.getDimensions() == 5);
		assertTrue(x.getElementType().isPrimitiveType());
	}		

	public void testPackageDeclaration() {
		long previousCount = ast.modificationCount();
		final PackageDeclaration x = ast.newPackageDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.PACKAGE_DECLARATION);

		genericPropertyTest(x, new Property("Name", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});
	}		
	
	public void testImportDeclaration() {
		long previousCount = ast.modificationCount();
		final ImportDeclaration x = ast.newImportDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.isOnDemand() == false);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.IMPORT_DECLARATION);

		genericPropertyTest(x, new Property("Name", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});

		previousCount = ast.modificationCount();
		x.setOnDemand(false);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isOnDemand() == false);
		previousCount = ast.modificationCount();
		x.setOnDemand(true);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isOnDemand() == true);
	}
	
	public void testCompilationUnit() {
		long previousCount = ast.modificationCount();
		final CompilationUnit x = ast.newCompilationUnit();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getPackage() == null);
		assertTrue(x.imports().size() == 0);
		assertTrue(x.types().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.COMPILATION_UNIT);

		tClientProperties(x);
		
		genericPropertyTest(x, new Property("Package", false, PackageDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				PackageDeclaration result = targetAst.newPackageDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.setPackage(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getPackage();
			}
			public void set(ASTNode value) {
				x.setPackage((PackageDeclaration) value);
			}
		});

		genericPropertyListTest(x, x.imports(), new Property("Imports", true, ImportDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				ImportDeclaration result = targetAst.newImportDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.imports().add(result);
				}
				return result;
			}
		});
		
		genericPropertyListTest(x, x.types(), new Property("Types", true, TypeDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.types().add(result);
				}
				return result;
			}
		});
		
		// check that TypeDeclarations in body are classified correctly
		TypeDeclaration t1 = ast.newTypeDeclaration();
		x.types().add(t1);
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == false);
		assertTrue(t1.isPackageMemberTypeDeclaration() == true);

	}		
	
	public void testCompilationUnitLineNumberTable() {
//		TO RUN THIS TEST YOU MUST TEMPORARILY MAKE PUBLIC
//		THE METHOD CompilationUnit.setLineEndTable
		
//		final CompilationUnit x = ast.newCompilationUnit();
//		
//		// table starts off empty
//		for (int i= -10; i < 10; i++) {
//			assertTrue(x.lineNumber(i) == 1);
//		}
//		
//		// supply a simple line table to test
//		String s = "AA\nBBB\nCC\nDDDD\nEEE";
//		assertTrue(s.length() == 18);  // cross check
//		int le[] = new int[5];
//		le[0] = s.indexOf('\n');
//		le[1] = s.indexOf('\n', le[0] + 1);
//		le[2] = s.indexOf('\n', le[1] + 1);
//		le[3] = s.indexOf('\n', le[2] + 1);
//		le[4] = s.length() - 1;
//		long previousCount = ast.modificationCount();
//		x.setLineEndTable(le);
//		assertTrue(ast.modificationCount() > previousCount);
//
//		assertTrue(x.lineNumber(0) == 1);
//		assertTrue(x.lineNumber(1) == 1);
//		assertTrue(x.lineNumber(2) == 1);
//		assertTrue(x.lineNumber(3) == 2);
//		assertTrue(x.lineNumber(4) == 2);
//		assertTrue(x.lineNumber(5) == 2);
//		assertTrue(x.lineNumber(6) == 2);
//		assertTrue(x.lineNumber(7) == 3);
//		assertTrue(x.lineNumber(8) == 3);
//		assertTrue(x.lineNumber(9) == 3);
//		assertTrue(x.lineNumber(10) == 4);
//		assertTrue(x.lineNumber(11) == 4);
//		assertTrue(x.lineNumber(12) == 4);
//		assertTrue(x.lineNumber(13) == 4);
//		assertTrue(x.lineNumber(14) == 4);
//		assertTrue(x.lineNumber(15) == 5);
//		assertTrue(x.lineNumber(16) == 5);
//		assertTrue(x.lineNumber(17) == 5);
//
//		assertTrue(x.lineNumber(18) == 1);
//		assertTrue(x.lineNumber(100) == 1);
//		assertTrue(x.lineNumber(1000000) == 1);
//		assertTrue(x.lineNumber(-1) == 1);
//		assertTrue(x.lineNumber(-100) == 1);
//		assertTrue(x.lineNumber(-1000000) == 1);
//				
//		// slam table back to none
//		x.setLineEndTable(new int[0]);
//		for (int i= -10; i < 10; i++) {
//			assertTrue(x.lineNumber(i) == 1);
//		}
	}		
	
	public void testTypeDeclaration() {
		long previousCount = ast.modificationCount();
		final TypeDeclaration x = ast.newTypeDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof BodyDeclaration);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.isInterface() == false);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getSuperclass() == null);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.superInterfaces().size() == 0);
		assertTrue(x.bodyDeclarations().size()== 0);
		assertTrue(x.getNodeType() == ASTNode.TYPE_DECLARATION);
	
		previousCount = ast.modificationCount();
		x.setInterface(true);	
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isInterface() == true);
		
		int legal = Modifier.PUBLIC | Modifier.PROTECTED
			| Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC
			| Modifier.FINAL | Modifier.STRICTFP;
		previousCount = ast.modificationCount();
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);

		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);

		tJavadocComment(x);
				
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Superclass", false, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getSuperclass();
			}
			public void set(ASTNode value) {
				x.setSuperclass((Name) value);
			}
		});
		
		genericPropertyListTest(x, x.superInterfaces(),
		  new Property("SuperInterfaces", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
		});
		
		genericPropertyListTest(x, x.bodyDeclarations(),
		  new Property("BodyDeclarations", true, BodyDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.types().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				TypeDeclaration s1 = x.getAST().newTypeDeclaration();
				s1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s1 = (TypeDeclaration) x.getParent();
				s1.bodyDeclarations().remove(x);
			}
		});
		
		// check special bodyDeclaration methods
		x.bodyDeclarations().clear();
		FieldDeclaration f1 = ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
		FieldDeclaration f2 = ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
		MethodDeclaration m1 = ast.newMethodDeclaration();
		MethodDeclaration m2 = ast.newMethodDeclaration();
		TypeDeclaration t1 = ast.newTypeDeclaration();
		TypeDeclaration t2 = ast.newTypeDeclaration();

		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(f1);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(f2);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(t1);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(m1);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(m2);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(t2);
		x.bodyDeclarations().add(ast.newInitializer());

		List fs = Arrays.asList(x.getFields());
		assertTrue(fs.size() == 2);
		assertTrue(fs.contains(f1));
		assertTrue(fs.contains(f2));
		
		List ms = Arrays.asList(x.getMethods());
		assertTrue(ms.size() == 2);
		assertTrue(ms.contains(m1));
		assertTrue(ms.contains(m2));

		List ts = Arrays.asList(x.getTypes());
		assertTrue(ts.size() == 2);
		assertTrue(ts.contains(t1));
		assertTrue(ts.contains(t2));
		
		// check that TypeDeclarations in body are classified correctly
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == true);
		assertTrue(t1.isPackageMemberTypeDeclaration() == false);
	
	}	
	
	public void testSingleVariableDeclaration() {
		long previousCount = ast.modificationCount();
		final SingleVariableDeclaration x = ast.newSingleVariableDeclaration();
		assertTrue(x instanceof VariableDeclaration);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getInitializer() == null);
		assertTrue(x.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION);

		int legal = Modifier.PUBLIC | Modifier.PROTECTED
			| Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL
			| Modifier.TRANSIENT | Modifier.VOLATILE;
		previousCount = ast.modificationCount();
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);

		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Type", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
		
		genericPropertyTest(x, new Property("Initializer", false, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return an Expression that embeds x
				CatchClause s1 = ast.newCatchClause();
				s1.setException(x);
				return s1;
			}
			public void unwrap() {
				CatchClause s1 = (CatchClause) x.getParent();
				s1.setException(ast.newSingleVariableDeclaration());
			}
			public ASTNode get() {
				return x.getInitializer();
			}
			public void set(ASTNode value) {
				x.setInitializer((Expression) value);
			}
		});
	}
	
	public void testVariableDeclarationFragment() {
		long previousCount = ast.modificationCount();
		final VariableDeclarationFragment x = ast.newVariableDeclarationFragment();
		assertTrue(x instanceof VariableDeclaration);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getExtraDimensions() == 0);
		assertTrue(x.getInitializer() == null);
		assertTrue(x.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT);

		previousCount = ast.modificationCount();
		x.setExtraDimensions(1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 1);

		previousCount = ast.modificationCount();
		x.setExtraDimensions(0);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 0);
		
		// check that property cannot be set negative
		try {
			x.setExtraDimensions(-1);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Initializer", false, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return an Expression that embeds x
				VariableDeclarationExpression s1 =
					ast.newVariableDeclarationExpression(x);
				return s1;
			}
			public void unwrap() {
				VariableDeclarationExpression s1 = 
					(VariableDeclarationExpression) x.getParent();
				s1.fragments().remove(x);
			}
			public ASTNode get() {
				return x.getInitializer();
			}
			public void set(ASTNode value) {
				x.setInitializer((Expression) value);
			}
		});
	}
	
	public void testMethodDeclaration() {
		long previousCount = ast.modificationCount();
		final MethodDeclaration x = ast.newMethodDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof BodyDeclaration);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.isConstructor() == false);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getReturnType().getParent() == x);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.parameters().size() == 0);
		assertTrue(x.thrownExceptions().size() == 0);
		assertTrue(x.getBody() == null);
		assertTrue(x.getNodeType() == ASTNode.METHOD_DECLARATION);
	
		previousCount = ast.modificationCount();
		x.setConstructor(true);	
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isConstructor() == true);
		assertTrue(x.getName().isDeclaration() == false);

		previousCount = ast.modificationCount();
		x.setConstructor(false);	
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isConstructor() == false);
		
		previousCount = ast.modificationCount();
		int legal = Modifier.PUBLIC | Modifier.PROTECTED
			| Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC 
			| Modifier.FINAL | Modifier.SYNCHRONIZED| Modifier.NATIVE
			| Modifier.STRICTFP;
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);

		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);

		tJavadocComment(x);
						
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("ReturnType", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getReturnType();
			}
			public void set(ASTNode value) {
				x.setReturnType((Type) value);
			}
		});
		
		genericPropertyListTest(x, x.parameters(),
		  new Property("Parameters", true, SingleVariableDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SingleVariableDeclaration result = targetAst.newSingleVariableDeclaration();
				if (parented) {
					targetAst.newCatchClause().setException(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a SingleVariableDeclaration that embeds x
				SingleVariableDeclaration s1 = ast.newSingleVariableDeclaration();
				ClassInstanceCreation s2 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s2.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s2);
				a1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration a1 = (AnonymousClassDeclaration) x.getParent();
				a1.bodyDeclarations().remove(x);
			}
		});
		
		genericPropertyListTest(x, x.thrownExceptions(),
		  new Property("ThrownExceptions", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
		});
		
		genericPropertyTest(x, new Property("Body", false, Block.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				TypeDeclaration s2 = ast.newTypeDeclaration();
				s1.statements().add(ast.newTypeDeclarationStatement(s2));
				s2.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s2 = (TypeDeclaration) x.getParent();
				s2.bodyDeclarations().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}	
	
	public void testInitializer() {
		long previousCount = ast.modificationCount();
		final Initializer x = ast.newInitializer();
		assertTrue(x instanceof BodyDeclaration);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody().statements().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.INITIALIZER);
		
		tJavadocComment(x);
				
		int legal = Modifier.STATIC;
		previousCount = ast.modificationCount();
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);
		
		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);
		
		genericPropertyTest(x, new Property("Body", true, Block.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Block that embeds x
				Block s1 = ast.newBlock();
				TypeDeclaration s2 = ast.newTypeDeclaration();
				s1.statements().add(ast.newTypeDeclarationStatement(s2));
				s2.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s2 = (TypeDeclaration) x.getParent();
				s2.bodyDeclarations().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}	
	
	public void testJavadoc() {
		long previousCount = ast.modificationCount();
		Javadoc x = ast.newJavadoc();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getComment().startsWith("/**"));
		assertTrue(x.getComment().endsWith("*/"));
		assertTrue(x.getNodeType() == ASTNode.JAVADOC);

		final String[] samples =
			{ 
			  "/** Hello there */",
			  "/**\n * Line 1\n * Line 2\n */",
			  "/***/",
			};
		for (int i = 0; i < samples.length; i++) {			
			previousCount = ast.modificationCount();
			x.setComment(samples[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(samples[i].equals(x.getComment()));
		}

		final String[] badSamples =
			{ 
			  null,
			  "",
			  "/* */",
			  "/**/",
			  "/**",
			  "*/",
			};

		// check that property cannot be set to clearly illegal things
		for (int i = 0; i < badSamples.length; i++) {			
			try {
				x.setComment(badSamples[i]);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}
	}		

	public void testBlock() {
		long previousCount = ast.modificationCount();
		final Block x = ast.newBlock();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.statements().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.BLOCK);
		
		tLeadingComment(x);

		genericPropertyListTest(x, x.statements(),
		  new Property("Statements", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
		});
	}	
	
	public void testMethodInvocation() {
		long previousCount = ast.modificationCount();
		final MethodInvocation x = ast.newMethodInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getExpression() == null);
		assertTrue(x.arguments().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.METHOD_INVOCATION);
	
		genericPropertyTest(x, new Property("Expression", false, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
		});
	}	
	
	public void testExpressionStatement() {
		long previousCount = ast.modificationCount();
		SimpleName x1 = ast.newSimpleName("foo");
		final ExpressionStatement x = ast.newExpressionStatement(x1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.EXPRESSION_STATEMENT);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

	}	
	
	public void testVariableDeclarationStatement() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		long previousCount = ast.modificationCount();
		final VariableDeclarationStatement x = 
			ast.newVariableDeclarationStatement(x1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.getType() != null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.fragments().size() == 1);
		assertTrue(x.fragments().get(0) == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
	
		tLeadingComment(x);

		int legal = Modifier.FINAL;
		previousCount = ast.modificationCount();
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);

		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);
		
		// check that property cannot be set to illegal value
		try {
			x.setModifiers(Modifier.PUBLIC);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		genericPropertyTest(x, new Property("Type", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyListTest(x, x.fragments(),
		  new Property("VariableSpecifiers", true, VariableDeclarationFragment.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				VariableDeclarationFragment result = targetAst.newVariableDeclarationFragment();
				if (parented) {
					targetAst.newVariableDeclarationExpression(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return VariableDeclarationFragment that embeds x
				VariableDeclarationFragment s1 = ast.newVariableDeclarationFragment();
				ClassInstanceCreation s0 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s0.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s0);
				Initializer s2 = ast.newInitializer();
				a1.bodyDeclarations().add(s2);
				s2.getBody().statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});
	

	}	
	
	public void testTypeDeclarationStatement() {
		TypeDeclaration x1 = ast.newTypeDeclaration();
		long previousCount = ast.modificationCount();
		final TypeDeclarationStatement x =
			ast.newTypeDeclarationStatement(x1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getTypeDeclaration() == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.TYPE_DECLARATION_STATEMENT);

		// check that TypeDeclaration inside is classified correctly
		assertTrue(x1.isLocalTypeDeclaration() == true);
		assertTrue(x1.isMemberTypeDeclaration() == false);
		assertTrue(x1.isPackageMemberTypeDeclaration() == false);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("TypeDeclaration", true, TypeDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					targetAst.newTypeDeclarationStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return TypeDeclaration that embeds x
				TypeDeclaration s1 = ast.newTypeDeclaration();
				MethodDeclaration s2 = ast.newMethodDeclaration();
				s1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getTypeDeclaration();
			}
			public void set(ASTNode value) {
				x.setTypeDeclaration((TypeDeclaration) value);
			}
		});

	}	
	
	public void testVariableDeclarationExpression() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		long previousCount = ast.modificationCount();
		final VariableDeclarationExpression x = 
			ast.newVariableDeclarationExpression(x1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.getType() != null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.fragments().size() == 1);
		assertTrue(x.fragments().get(0) == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION);
	
		int legal = Modifier.FINAL;
		previousCount = ast.modificationCount();
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);

		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);
		
		// check that property cannot be set to illegal value
		try {
			x.setModifiers(Modifier.PUBLIC);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		genericPropertyTest(x, new Property("Type", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyListTest(x, x.fragments(),
		  new Property("VariableSpecifiers", true, VariableDeclarationFragment.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				VariableDeclarationFragment result = targetAst.newVariableDeclarationFragment();
				if (parented) {
					targetAst.newVariableDeclarationExpression(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return VariableDeclarationFragment that embeds x
				VariableDeclarationFragment s1 = ast.newVariableDeclarationFragment();
				ClassInstanceCreation s0 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s0.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s0);
				ForStatement s2 = ast.newForStatement();
				s2.initializers().add(x);
				Initializer s3 = ast.newInitializer();
				a1.bodyDeclarations().add(s3);
				s3.getBody().statements().add(s2);
				return s1;
			}
			public void unwrap() {
				ForStatement s2 = (ForStatement) x.getParent();
				s2.initializers().remove(x);
			}
		});
	}	
	
	public void testFieldDeclaration() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		long previousCount = ast.modificationCount();
		final FieldDeclaration x = ast.newFieldDeclaration(x1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof BodyDeclaration);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.getModifiers() == Modifier.NONE);
		assertTrue(x.getType() != null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.fragments().size() == 1);
		assertTrue(x.fragments().get(0) == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.FIELD_DECLARATION);
	
		int legal = Modifier.PUBLIC | Modifier.PROTECTED
			| Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL
			| Modifier.TRANSIENT | Modifier.VOLATILE;
		previousCount = ast.modificationCount();
		x.setModifiers(legal);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == legal);

		previousCount = ast.modificationCount();
		x.setModifiers(Modifier.NONE);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getModifiers() == Modifier.NONE);

		// check that property cannot be set to illegal value
		try {
			x.setModifiers(Modifier.SYNCHRONIZED);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		tJavadocComment(x);
						
		genericPropertyTest(x, new Property("Type", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyListTest(x, x.fragments(),
		  new Property("VariableSpecifiers", true, VariableDeclarationFragment.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				VariableDeclarationFragment result = targetAst.newVariableDeclarationFragment();
				if (parented) {
					targetAst.newVariableDeclarationStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return VariableDeclarationFragment that embeds x
				VariableDeclarationFragment s1 = ast.newVariableDeclarationFragment();
				ClassInstanceCreation s2 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s2.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s2);
				a1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration a1 = (AnonymousClassDeclaration) x.getParent();
				a1.bodyDeclarations().remove(x);
			}
		});
	
	}
	
	public void testAssignment() {
		long previousCount = ast.modificationCount();
		final Assignment x = ast.newAssignment();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getOperator() == Assignment.Operator.ASSIGN);
		assertTrue(x.getLeftHandSide().getParent() == x);
		assertTrue(x.getRightHandSide().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.ASSIGNMENT);
	
		previousCount = ast.modificationCount();
		x.setOperator(Assignment.Operator.PLUS_ASSIGN);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getOperator() == Assignment.Operator.PLUS_ASSIGN);
		assertTrue(Assignment.Operator.PLUS_ASSIGN 
			!= Assignment.Operator.ASSIGN);

		// check the names of the primitive type codes
		assertTrue(Assignment.Operator.ASSIGN.toString().equals("="));
		assertTrue(Assignment.Operator.PLUS_ASSIGN.toString().equals("+="));
		assertTrue(Assignment.Operator.MINUS_ASSIGN.toString().equals("-="));
		assertTrue(Assignment.Operator.TIMES_ASSIGN.toString().equals("*="));
		assertTrue(Assignment.Operator.DIVIDE_ASSIGN.toString().equals("/="));
		assertTrue(Assignment.Operator.REMAINDER_ASSIGN.toString().equals("%="));
		assertTrue(Assignment.Operator.LEFT_SHIFT_ASSIGN.toString().equals("<<="));
		assertTrue(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN.toString().equals(">>="));
		assertTrue(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN.toString().equals(">>>="));
		assertTrue(Assignment.Operator.BIT_AND_ASSIGN.toString().equals("&="));
		assertTrue(Assignment.Operator.BIT_OR_ASSIGN.toString().equals("|="));
		assertTrue(Assignment.Operator.BIT_XOR_ASSIGN.toString().equals("^="));
		
		Assignment.Operator[] known = {
			Assignment.Operator.ASSIGN,
			Assignment.Operator.PLUS_ASSIGN,
			Assignment.Operator.MINUS_ASSIGN,
			Assignment.Operator.TIMES_ASSIGN,
			Assignment.Operator.DIVIDE_ASSIGN,
			Assignment.Operator.REMAINDER_ASSIGN,
			Assignment.Operator.LEFT_SHIFT_ASSIGN,
			Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN,
			Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN,
			Assignment.Operator.BIT_AND_ASSIGN,
			Assignment.Operator.BIT_OR_ASSIGN,
			Assignment.Operator.BIT_XOR_ASSIGN,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as a primitive type code
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operators by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(Assignment.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(Assignment.Operator.toOperator("not-an-op") == null);

		genericPropertyTest(x, new Property("LeftHandSide", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
			public ASTNode get() {
				return x.getLeftHandSide();
			}
			public void set(ASTNode value) {
				x.setLeftHandSide((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("RightHandSide", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
			public ASTNode get() {
				return x.getRightHandSide();
			}
			public void set(ASTNode value) {
				x.setRightHandSide((Expression) value);
			}
		});
	}	

	public void testBreakStatement() {
		long previousCount = ast.modificationCount();
		final BreakStatement x = ast.newBreakStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLabel() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.BREAK_STATEMENT);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Label", false, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getLabel();
			}
			public void set(ASTNode value) {
				x.setLabel((SimpleName) value);
			}
		});
	}	
	
	public void testContinueStatement() {
		long previousCount = ast.modificationCount();
		final ContinueStatement x = ast.newContinueStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLabel() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.CONTINUE_STATEMENT);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Label", false, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getLabel();
			}
			public void set(ASTNode value) {
				x.setLabel((SimpleName) value);
			}
		});
	}	
	
	public void testIfStatement() {
		long previousCount = ast.modificationCount();
		final IfStatement x = ast.newIfStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getThenStatement().getParent() == x);
		assertTrue(x.getThenStatement() instanceof Block);
		assertTrue(((Block) x.getThenStatement()).statements().isEmpty());
		assertTrue(x.getElseStatement() == null);
		assertTrue(x.getNodeType() == ASTNode.IF_STATEMENT);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("ThenStatement", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getThenStatement();
			}
			public void set(ASTNode value) {
				x.setThenStatement((Statement) value);
			}
		});
		
		genericPropertyTest(x, new Property("ElseStatement", false, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getElseStatement();
			}
			public void set(ASTNode value) {
				x.setElseStatement((Statement) value);
			}
		});
	}	
	
	public void testWhileStatement() {
		long previousCount = ast.modificationCount();
		final WhileStatement x = ast.newWhileStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.WHILE_STATEMENT);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}	
	
	public void testDoStatement() {
		long previousCount = ast.modificationCount();
		final DoStatement x = ast.newDoStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.DO_STATEMENT);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}	
	
	public void testTryStatement() {
		long previousCount = ast.modificationCount();
		final TryStatement x = ast.newTryStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getFinally() == null);
		assertTrue(x.catchClauses().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.TRY_STATEMENT);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Body", true, Block.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});

		genericPropertyListTest(x, x.catchClauses(),
		  new Property("CatchClauses", true, CatchClause.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				CatchClause result = targetAst.newCatchClause();
				if (parented) {
					TryStatement s1 = targetAst.newTryStatement();
					s1.catchClauses().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return CatchClause that embeds x
				CatchClause s1 = ast.newCatchClause();
				Block s2 = ast.newBlock();
				s1.setBody(s2);
				s2.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
		});

		genericPropertyTest(x, new Property("Finally", false, Block.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getFinally();
			}
			public void set(ASTNode value) {
				x.setFinally((Block) value);
			}
		});
	}	

	public void testCatchClause() {
		long previousCount = ast.modificationCount();
		final CatchClause x = ast.newCatchClause();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody().statements().isEmpty());
		assertTrue(x.getException().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.CATCH_CLAUSE);

		genericPropertyTest(x, new Property("Exception", true, SingleVariableDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SingleVariableDeclaration result = targetAst.newSingleVariableDeclaration();
				if (parented) {
					targetAst.newCatchClause().setException(result);
					
				}
				return result;
			}
			public ASTNode wrap() {
				// return SingleVariableDeclaration that embeds x
				SingleVariableDeclaration s1 = ast.newSingleVariableDeclaration();
				ClassInstanceCreation s2 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s2.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s2);
				MethodDeclaration s3 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s3);
				Block s4 = ast.newBlock();
				s3.setBody(s4);
				TryStatement s5 = ast.newTryStatement();
				s4.statements().add(s5);
				s5.catchClauses().add(x);
				return s1;
			}
			public void unwrap() {
				TryStatement s5 = (TryStatement) x.getParent();
				s5.catchClauses().remove(x);
			}
			public ASTNode get() {
				return x.getException();
			}
			public void set(ASTNode value) {
				x.setException((SingleVariableDeclaration) value);
			}
		});

		genericPropertyTest(x, new Property("Body", true, Block.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				TryStatement s2 = ast.newTryStatement();
				s1.statements().add(s2);
				s2.catchClauses().add(x);
				return s1;
			}
			public void unwrap() {
				TryStatement s2 = (TryStatement) x.getParent();
				s2.catchClauses().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}

	public void testEmptyStatement() {
		long previousCount = ast.modificationCount();
		final EmptyStatement x = ast.newEmptyStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.EMPTY_STATEMENT);
		
		tLeadingComment(x);
	}
	
	/**
	 * Exercise the leadingComment property.
	 * 
	 * @param x the statement to test
	 */
	void tLeadingComment(Statement x) {
		
		// check that null is allowed
		long previousCount = ast.modificationCount();
		x.setLeadingComment(null);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == null);
		
		// check that regular comment is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("/* X */");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "/* X */");
		
		// check that regular comment with line breaks is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("/* X\n *Y\n */");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "/* X\n *Y\n */");
		
		// check that end-of-line comment is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("// X");
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "// X");
		
		// check that end-of-line comment with embedded end of line 
		// not allowed
		try {
			x.setLeadingComment("// X\n extra");
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		
	}
		
	/**
	 * Exercise the javadoc property.
	 * 
	 * @param x the body declaration to test
	 */
	void tJavadocComment(final BodyDeclaration x) {
		genericPropertyTest(x, new Property("Javadoc", false, Javadoc.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Javadoc result = targetAst.newJavadoc();
				if (parented) {
					targetAst.newInitializer().setJavadoc(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getJavadoc();
			}
			public void set(ASTNode value) {
				x.setJavadoc((Javadoc) value);
			}
		});
	}

	/**
	 * Exercise the client properties of a node.
	 * 
	 * @param x the node to test
	 */
	void tClientProperties(ASTNode x) {
		
		long previousCount = ast.modificationCount();
		
		// a node initially has no properties
		assertTrue(x.properties().size() == 0);
		assertTrue(x.getProperty("1") == null);

		// clearing an unset property does not add it to list of known ones
		x.setProperty("1", null);
		assertTrue(x.getProperty("1") == null);
		assertTrue(x.properties().size() == 0);

		// setting an unset property does add it to the list of known ones
		x.setProperty("1", "a1");
		assertTrue(x.getProperty("1") == "a1");
		assertTrue(x.properties().size() == 1);
		Map.Entry[] m = (Map.Entry[]) x.properties().entrySet().toArray(new Map.Entry[1]);
		assertTrue(m[0].getKey() == "1");
		assertTrue(m[0].getValue() == "a1");

		// setting an already set property just changes its value
		x.setProperty("1", "a2");
		assertTrue(x.getProperty("1") == "a2");
		assertTrue(x.properties().size() == 1);
		m = (Map.Entry[]) x.properties().entrySet().toArray(new Map.Entry[1]);
		assertTrue(m[0].getKey() == "1");
		assertTrue(m[0].getValue() == "a2");

		// clearing a set property removes it from list of known ones
		x.setProperty("1", null);
		assertTrue(x.getProperty("1") == null);
		assertTrue(x.properties().size() == 0);
		
		
		// ========= test 2 and 3 properties
		x.setProperty("1", "a1");
		x.setProperty("2", "b1");
		x.setProperty("3", "c1");
		assertTrue(x.getProperty("1") == "a1");
		assertTrue(x.getProperty("2") == "b1");
		assertTrue(x.getProperty("3") == "c1");
		assertTrue(x.properties().size() == 3);
		assertTrue(x.properties().get("1") == "a1");
		assertTrue(x.properties().get("2") == "b1");
		assertTrue(x.properties().get("3") == "c1");
		x.setProperty("1", "a2");
		x.setProperty("2", "b2");
		x.setProperty("3", "c2");
		assertTrue(x.getProperty("1") == "a2");
		assertTrue(x.getProperty("2") == "b2");
		assertTrue(x.getProperty("3") == "c2");
		assertTrue(x.properties().size() == 3);
		assertTrue(x.properties().get("1") == "a2");
		assertTrue(x.properties().get("2") == "b2");
		assertTrue(x.properties().get("3") == "c2");
		x.setProperty("2", null);
		assertTrue(x.getProperty("1") == "a2");
		assertTrue(x.getProperty("2") == null);
		assertTrue(x.getProperty("3") == "c2");
		assertTrue(x.properties().size() == 2);
		assertTrue(x.properties().get("1") == "a2");
		assertTrue(x.properties().get("2") == null);
		assertTrue(x.properties().get("3") == "c2");
		x.setProperty("1", null);
		assertTrue(x.getProperty("1") == null);
		assertTrue(x.getProperty("2") == null);
		assertTrue(x.getProperty("3") == "c2");
		assertTrue(x.properties().size() == 1);
		assertTrue(x.properties().get("1") == null);
		assertTrue(x.properties().get("2") == null);
		assertTrue(x.properties().get("3") == "c2");
		
		// none of this is considered to have affected the AST
		assertTrue(ast.modificationCount() == previousCount);
	}
	
	public void testReturnStatement() {
		long previousCount = ast.modificationCount();
		final ReturnStatement x = ast.newReturnStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == null);
		assertTrue(x.getNodeType() == ASTNode.RETURN_STATEMENT);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", false, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	public void testThrowStatement() {
		long previousCount = ast.modificationCount();
		final ThrowStatement x = ast.newThrowStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.THROW_STATEMENT);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	public void testAssertStatement() {
		long previousCount = ast.modificationCount();
		final AssertStatement x = ast.newAssertStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getMessage() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.ASSERT_STATEMENT);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Message", false, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getMessage();
			}
			public void set(ASTNode value) {
				x.setMessage((Expression) value);
			}
		});
	}

	public void testSwitchStatement() {
		long previousCount = ast.modificationCount();
		final SwitchStatement x = ast.newSwitchStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.SWITCH_STATEMENT);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyListTest(x, x.statements(),
		  new Property("Statements", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
		});
	}

	public void testSwitchCase() {
		long previousCount = ast.modificationCount();
		final SwitchCase x = ast.newSwitchCase();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(!x.isDefault());	
		assertTrue(x.getNodeType() == ASTNode.SWITCH_CASE);

		genericPropertyTest(x, new Property("Expression", false, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				SwitchStatement s4 = ast.newSwitchStatement();
				s3.statements().add(s4);
				s4.statements().add(x);
				return s1;
			}
			public void unwrap() {
				SwitchStatement s4 = (SwitchStatement) x.getParent();
				s4.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}
	
	public void testSynchronizedStatement() {
		long previousCount = ast.modificationCount();
		final SynchronizedStatement x = ast.newSynchronizedStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.SYNCHRONIZED_STATEMENT);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Block.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}
	
	public void testLabeledStatement() {
		long previousCount = ast.modificationCount();
		final LabeledStatement x = ast.newLabeledStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLabel().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.LABELED_STATEMENT);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Label", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getLabel();
			}
			public void set(ASTNode value) {
				x.setLabel((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}
	

	/**
	 * Returns a subtree of sample of AST nodes. The sample includes
	 * one of each kind, but otherwise does not make sense.
	 */
	ASTNode oneOfEach(AST target) {
		CompilationUnit cu = target.newCompilationUnit();
		
		PackageDeclaration pd = target.newPackageDeclaration();
		cu.setPackage(pd);
		
		ImportDeclaration im = target.newImportDeclaration();
		cu.imports().add(im);
		
		TypeDeclaration td = target.newTypeDeclaration();
		td.setJavadoc(target.newJavadoc());
		cu.types().add(td);
		
		FieldDeclaration fd = 
			target.newFieldDeclaration(target.newVariableDeclarationFragment());
		td.bodyDeclarations().add(fd);	
		
		Initializer in = target.newInitializer();
		td.bodyDeclarations().add(in);	
		
		MethodDeclaration md = target.newMethodDeclaration();
		md.parameters().add(target.newSingleVariableDeclaration());
		td.bodyDeclarations().add(md);
		
		SimpleName sn1 = target.newSimpleName("one");
		SimpleName sn2 =target.newSimpleName("two");
		QualifiedName qn = target.newQualifiedName(sn1, sn2);

		SimpleType st = target.newSimpleType(qn);
		PrimitiveType pt = target.newPrimitiveType(PrimitiveType.INT);
		ArrayType at = target.newArrayType(st);

		md.setReturnType(at);
		fd.setType(pt);
		
		Block b = target.newBlock();
		md.setBody(b);
		
		// all statements (in alphabetic order of statement type)
		b.statements().add(target.newAssertStatement());
		b.statements().add(target.newBlock());
		b.statements().add(target.newBreakStatement());
		b.statements().add(target.newContinueStatement());
		b.statements().add(target.newConstructorInvocation());
		b.statements().add(target.newDoStatement());
		b.statements().add(target.newEmptyStatement());
		b.statements().add(target.newExpressionStatement(target.newNullLiteral()));
		b.statements().add(target.newForStatement());
		b.statements().add(target.newIfStatement());
		b.statements().add(target.newLabeledStatement());
		b.statements().add(target.newReturnStatement());
		b.statements().add(target.newSuperConstructorInvocation());
		SwitchStatement ss = target.newSwitchStatement();
			ss.statements().add(target.newSwitchCase());
			b.statements().add(ss);
		b.statements().add(target.newSwitchStatement());
		b.statements().add(target.newSwitchCase());
		b.statements().add(target.newSynchronizedStatement());
		b.statements().add(target.newThrowStatement());
		TryStatement tr = target.newTryStatement();
			tr.catchClauses().add(target.newCatchClause());
			b.statements().add(tr);
		b.statements().add(target.newTypeDeclarationStatement(target.newTypeDeclaration()));
		b.statements().add(target.newVariableDeclarationStatement(target.newVariableDeclarationFragment()));
		b.statements().add(target.newWhileStatement());

		// all expressions (in alphabetic order of expressions type)
		MethodInvocation inv = target.newMethodInvocation();
		b.statements().add(target.newExpressionStatement(inv));
		List z = inv.arguments();
		z.add(target.newArrayAccess());
		z.add(target.newArrayCreation());
		z.add(target.newArrayInitializer());
		z.add(target.newAssignment());
		z.add(target.newBooleanLiteral(true));
		z.add(target.newCastExpression());
		z.add(target.newCharacterLiteral());
		ClassInstanceCreation cic = target.newClassInstanceCreation();
		cic.setAnonymousClassDeclaration(target.newAnonymousClassDeclaration());
		z.add(cic);
		z.add(target.newConditionalExpression());
		z.add(target.newFieldAccess());
		z.add(target.newInfixExpression());
		z.add(target.newInstanceofExpression());
		z.add(target.newMethodInvocation());
		z.add(target.newName(new String[]{"a", "b"}));
		z.add(target.newNullLiteral());
		z.add(target.newNumberLiteral("1024"));
		z.add(target.newParenthesizedExpression());
		z.add(target.newPostfixExpression());
		z.add(target.newPrefixExpression());
		z.add(target.newStringLiteral());
		z.add(target.newSuperFieldAccess());
		z.add(target.newSuperMethodInvocation());
		z.add(target.newThisExpression());
		z.add(target.newTypeLiteral());
		z.add(target.newVariableDeclarationExpression(
				target.newVariableDeclarationFragment()));
		
		return cu;
	}	
	
	public void testClone() {
		ASTNode x = oneOfEach(ast);
		assertTrue(x.subtreeMatch(new ASTMatcher(), x));
		
		// same AST clone
		ASTNode y = ASTNode.copySubtree(ast, x);
		assertTrue(x.subtreeMatch(new ASTMatcher(), y));
		assertTrue(y.subtreeMatch(new ASTMatcher(), x));
		
		// different AST clone
		ASTNode z = ASTNode.copySubtree(new AST(), x);
		assertTrue(x.subtreeMatch(new ASTMatcher(), z));
		assertTrue(z.subtreeMatch(new ASTMatcher(), x));
	}

	public void testNullResolve() {
		ASTNode x = oneOfEach(ast);
		
		ASTVisitor v = new ASTVisitor() {
			// NAMES

			public boolean visit(SimpleName node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(QualifiedName node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}

			// TYPES
	
			public boolean visit(SimpleType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(ArrayType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(PrimitiveType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
	
			// EXPRESSIONS
	
			public boolean visit(Assignment node) {
				assertTrue(node.resolveTypeBinding() == null);
				return true;
			}
	
			public boolean visit(ClassInstanceCreation node) {
				assertTrue(node.resolveConstructorBinding() == null);
				return true;
			}
	
			public boolean visit(ConstructorInvocation node) {
				assertTrue(node.resolveConstructorBinding() == null);
				return true;
			}
	
			public boolean visit(SuperConstructorInvocation node) {
				assertTrue(node.resolveConstructorBinding() == null);
				return true;
			}
	
			// MAJOR DECLARATIONS
	
			public boolean visit(PackageDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(ImportDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(MethodDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(TypeDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(TypeDeclarationStatement node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(SingleVariableDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(VariableDeclarationFragment node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
		};
		
		x.accept(v);
	}

	public void testForStatement() {
		long previousCount = ast.modificationCount();
		final ForStatement x = ast.newForStatement();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.initializers().isEmpty());
		assertTrue(x.getExpression() == null);
		assertTrue(x.updaters().isEmpty());
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.FOR_STATEMENT);

		tLeadingComment(x);
		
		genericPropertyListTest(x, x.initializers(),
		  new Property("Initializers", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});

		genericPropertyTest(x, new Property("Expression", false, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyListTest(x, x.updaters(),
		  new Property("Updaters", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});

		genericPropertyTest(x, new Property("Body", true, Statement.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}

	public void testConstructorInvocation() {
		long previousCount = ast.modificationCount();
		final ConstructorInvocation x = ast.newConstructorInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION);
		
		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});
	}

	public void testSuperConstructorInvocation() {
		long previousCount = ast.modificationCount();
		final SuperConstructorInvocation x = ast.newSuperConstructorInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Statement);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == null);
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION);
		
		genericPropertyTest(x, new Property("Expression", false, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});
	}

	public void testThisExpression() {
		long previousCount = ast.modificationCount();
		final ThisExpression x = ast.newThisExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.getNodeType() == ASTNode.THIS_EXPRESSION);
		
		genericPropertyTest(x, new Property("Qualifier", false, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"),
					targetAst.newSimpleName("b"));
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});
	}

	public void testFieldAccess() {
		long previousCount = ast.modificationCount();
		final FieldAccess x = ast.newFieldAccess();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.FIELD_ACCESS);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}


	public void testSuperFieldAccess() {
		long previousCount = ast.modificationCount();
		final SuperFieldAccess x = ast.newSuperFieldAccess();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.getNodeType() == ASTNode.SUPER_FIELD_ACCESS);
		
		genericPropertyTest(x, new Property("Qualifier", false, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"),
					targetAst.newSimpleName("b"));
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}

	public void testSuperMethodInvocation() {
		long previousCount = ast.modificationCount();
		final SuperMethodInvocation x = ast.newSuperMethodInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION);

		genericPropertyTest(x, new Property("Qualifier", false, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"),
					targetAst.newSimpleName("b"));
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});

		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
		});
	}

	public void testTypeLiteral() {
		long previousCount = ast.modificationCount();
		final TypeLiteral x = ast.newTypeLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.TYPE_LITERAL);

		genericPropertyTest(x, new Property("Type", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("a"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
	}

	public void testCastExpression() {
		long previousCount = ast.modificationCount();
		final CastExpression x = ast.newCastExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.CAST_EXPRESSION);

		genericPropertyTest(x, new Property("Type", true, Type.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("a"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	public void testPrefixExpression() {
		long previousCount = ast.modificationCount();
		final PrefixExpression x = ast.newPrefixExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getOperand().getParent() == x);
		assertTrue(x.getOperator() != null);
		assertTrue(x.getNodeType() == ASTNode.PREFIX_EXPRESSION);
		
		// Operator property - mandatory typesafe enumeration
		// check the names of the operators
		assertTrue(PrefixExpression.Operator.INCREMENT.toString().equals("++"));
		assertTrue(PrefixExpression.Operator.DECREMENT.toString().equals("--"));
		assertTrue(PrefixExpression.Operator.PLUS.toString().equals("+"));
		assertTrue(PrefixExpression.Operator.MINUS.toString().equals("-"));
		assertTrue(PrefixExpression.Operator.COMPLEMENT.toString().equals("~"));
		assertTrue(PrefixExpression.Operator.NOT.toString().equals("!"));
		
		PrefixExpression.Operator[] known = {
			PrefixExpression.Operator.INCREMENT,
			PrefixExpression.Operator.DECREMENT,
			PrefixExpression.Operator.PLUS,
			PrefixExpression.Operator.MINUS,
			PrefixExpression.Operator.COMPLEMENT,
			PrefixExpression.Operator.NOT,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operator by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(PrefixExpression.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(PrefixExpression.Operator.toOperator("huh") == null);

		genericPropertyTest(x, new Property("Operand", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getOperand();
			}
			public void set(ASTNode value) {
				x.setOperand((Expression) value);
			}
		});
	}

	public void testPostfixExpression() {
		long previousCount = ast.modificationCount();
		final PostfixExpression x = ast.newPostfixExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getOperand().getParent() == x);
		assertTrue(x.getOperator() != null);
		assertTrue(x.getNodeType() == ASTNode.POSTFIX_EXPRESSION);
		
		// Operator property - mandatory typesafe enumeration
		// check the names of the operators
		assertTrue(PostfixExpression.Operator.INCREMENT.toString().equals("++"));
		assertTrue(PostfixExpression.Operator.DECREMENT.toString().equals("--"));
		
		PostfixExpression.Operator[] known = {
			PostfixExpression.Operator.INCREMENT,
			PostfixExpression.Operator.DECREMENT,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operator by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(PostfixExpression.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(PostfixExpression.Operator.toOperator("huh") == null);

		genericPropertyTest(x, new Property("Operand", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getOperand();
			}
			public void set(ASTNode value) {
				x.setOperand((Expression) value);
			}
		});
	}

	public void testInfixExpression() {
		long previousCount = ast.modificationCount();
		final InfixExpression x = ast.newInfixExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeftOperand().getParent() == x);
		assertTrue(x.getOperator() != null);
		assertTrue(x.getRightOperand().getParent() == x);
		assertTrue(x.extendedOperands().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.INFIX_EXPRESSION);
		
		// Operator property - mandatory typesafe enumeration
		// check the names of the operators
		assertTrue(InfixExpression.Operator.TIMES.toString().equals("*"));
		assertTrue(InfixExpression.Operator.DIVIDE.toString().equals("/"));
		assertTrue(InfixExpression.Operator.REMAINDER.toString().equals("%"));
		assertTrue(InfixExpression.Operator.PLUS.toString().equals("+"));
		assertTrue(InfixExpression.Operator.MINUS.toString().equals("-"));
		assertTrue(InfixExpression.Operator.LEFT_SHIFT.toString().equals("<<"));
		assertTrue(InfixExpression.Operator.RIGHT_SHIFT_SIGNED.toString().equals(">>"));
		assertTrue(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED.toString().equals(">>>"));
		assertTrue(InfixExpression.Operator.LESS.toString().equals("<"));
		assertTrue(InfixExpression.Operator.GREATER.toString().equals(">"));
		assertTrue(InfixExpression.Operator.LESS_EQUALS.toString().equals("<="));
		assertTrue(InfixExpression.Operator.GREATER_EQUALS.toString().equals(">="));
		assertTrue(InfixExpression.Operator.EQUALS.toString().equals("=="));
		assertTrue(InfixExpression.Operator.NOT_EQUALS.toString().equals("!="));
		assertTrue(InfixExpression.Operator.XOR.toString().equals("^"));
		assertTrue(InfixExpression.Operator.OR.toString().equals("|"));
		assertTrue(InfixExpression.Operator.AND.toString().equals("&"));
		assertTrue(InfixExpression.Operator.CONDITIONAL_OR.toString().equals("||"));
		assertTrue(InfixExpression.Operator.CONDITIONAL_AND.toString().equals("&&"));
		
		InfixExpression.Operator[] known = {
				InfixExpression.Operator.TIMES,
				InfixExpression.Operator.DIVIDE,
				InfixExpression.Operator.REMAINDER,
				InfixExpression.Operator.PLUS,
				InfixExpression.Operator.MINUS,
				InfixExpression.Operator.LEFT_SHIFT,
				InfixExpression.Operator.RIGHT_SHIFT_SIGNED,
				InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED,
				InfixExpression.Operator.LESS,
				InfixExpression.Operator.GREATER,
				InfixExpression.Operator.LESS_EQUALS,
				InfixExpression.Operator.GREATER_EQUALS,
				InfixExpression.Operator.EQUALS,
				InfixExpression.Operator.NOT_EQUALS,
				InfixExpression.Operator.XOR,
				InfixExpression.Operator.OR,
				InfixExpression.Operator.AND,
				InfixExpression.Operator.CONDITIONAL_OR,
				InfixExpression.Operator.CONDITIONAL_AND,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operator by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(InfixExpression.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(InfixExpression.Operator.toOperator("huh") == null);

		genericPropertyTest(x, new Property("LeftOperand", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getLeftOperand();
			}
			public void set(ASTNode value) {
				x.setLeftOperand((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("RightOperand", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getRightOperand();
			}
			public void set(ASTNode value) {
				x.setRightOperand((Expression) value);
			}
		});

		genericPropertyListTest(x, x.extendedOperands(),
		  new Property("ExtendedOperands", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
		});
	}

	public void testInstanceofExpression() {
		long previousCount = ast.modificationCount();
		final InstanceofExpression x = ast.newInstanceofExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeftOperand().getParent() == x);
		assertTrue(x.getRightOperand().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION);
						
		genericPropertyTest(x, new Property("LeftOperand", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getLeftOperand();
			}
			public void set(ASTNode value) {
				x.setLeftOperand((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("RightOperand", true, Type.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Type result = ast.newSimpleType(ast.newSimpleName("Object"));
				if (parented) {
					ast.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getRightOperand();
			}
			public void set(ASTNode value) {
				x.setRightOperand((Type) value);
			}
		});
	}

	public void testConditionalExpression() {
		long previousCount = ast.modificationCount();
		final ConditionalExpression x = ast.newConditionalExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getThenExpression().getParent() == x);
		assertTrue(x.getElseExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION);
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("ThenExpression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getThenExpression();
			}
			public void set(ASTNode value) {
				x.setThenExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("ElseExpression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getElseExpression();
			}
			public void set(ASTNode value) {
				x.setElseExpression((Expression) value);
			}
		});
	}

	public void testArrayAccess() {
		long previousCount = ast.modificationCount();
		final ArrayAccess x = ast.newArrayAccess();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getArray().getParent() == x);
		assertTrue(x.getIndex().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.ARRAY_ACCESS);
		
		genericPropertyTest(x, new Property("Array", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getArray();
			}
			public void set(ASTNode value) {
				x.setArray((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Index", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getIndex();
			}
			public void set(ASTNode value) {
				x.setIndex((Expression) value);
			}
		});
	}

	public void testArrayInitializer() {
		long previousCount = ast.modificationCount();
		final ArrayInitializer x = ast.newArrayInitializer();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.expressions().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.ARRAY_INITIALIZER);
		
		genericPropertyListTest(x, x.expressions(),
		  new Property("Expressions", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
		});
	}

	public void testClassInstanceCreation() {
		long previousCount = ast.modificationCount();
		final ClassInstanceCreation x = ast.newClassInstanceCreation();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getAnonymousClassDeclaration() == null);
		assertTrue(x.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION);
		
		genericPropertyTest(x, new Property("Expression", false, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, Name.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});

		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
		});
		
		genericPropertyTest(x, new Property("AnonymousClassDeclaration", false, AnonymousClassDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				AnonymousClassDeclaration result = targetAst.newAnonymousClassDeclaration();
				if (parented) {
					targetAst.newClassInstanceCreation().setAnonymousClassDeclaration(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return AnonymousClassDeclaration that embeds x
				AnonymousClassDeclaration s0 = x.getAST().newAnonymousClassDeclaration();
				VariableDeclarationFragment s1 = x.getAST().newVariableDeclarationFragment();
				FieldDeclaration s2 = x.getAST().newFieldDeclaration(s1);
				s0.bodyDeclarations().add(s2);
				s1.setInitializer(x);
				return s0;
			}
			public void unwrap() {
				VariableDeclarationFragment s1 = (VariableDeclarationFragment) x.getParent();
				s1.setInitializer(null);
			}
			public ASTNode get() {
				return x.getAnonymousClassDeclaration();
			}
			public void set(ASTNode value) {
				x.setAnonymousClassDeclaration((AnonymousClassDeclaration) value);
			}
		});

	}

	public void testAnonymousClassDeclaration() {
		long previousCount = ast.modificationCount();
		final AnonymousClassDeclaration x = ast.newAnonymousClassDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof ASTNode);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.bodyDeclarations().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION);
		
		genericPropertyListTest(x, x.bodyDeclarations(),
		  new Property("BodyDeclarations", true, BodyDeclaration.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit compilationUnit = targetAst.newCompilationUnit();
					compilationUnit.types().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return BodyDeclaration that embeds x
				VariableDeclarationFragment s0 = x.getAST().newVariableDeclarationFragment();
				FieldDeclaration s1 = x.getAST().newFieldDeclaration(s0);
				ClassInstanceCreation s2= x.getAST().newClassInstanceCreation(); 
				s0.setInitializer(s2);
				s2.setAnonymousClassDeclaration(x);
				return s1;
			}
			public void unwrap() {
				ClassInstanceCreation s2 = (ClassInstanceCreation) x.getParent();
				s2.setAnonymousClassDeclaration(null);
			}
		});
		
		// check that TypeDeclarations in body are classified correctly
		TypeDeclaration t1 = ast.newTypeDeclaration();
		x.bodyDeclarations().add(t1);
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == true);
		assertTrue(t1.isPackageMemberTypeDeclaration() == false);
	}

	public void testArrayCreation() {
		long previousCount = ast.modificationCount();
		final ArrayCreation x = ast.newArrayCreation();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.dimensions().isEmpty());
		assertTrue(x.getInitializer() == null);
		assertTrue(x.getNodeType() == ASTNode.ARRAY_CREATION);
		
		genericPropertyTest(x, new Property("Type", true, ArrayType.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				ArrayType result = targetAst.newArrayType(
					targetAst.newSimpleType(targetAst.newSimpleName("a")));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((ArrayType) value);
			}
		});
		
		genericPropertyListTest(x, x.dimensions(),
		  new Property("Dimensions", true, Expression.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo");
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x"));
			}
		});

		genericPropertyTest(x, new Property("Initializer", false, ArrayInitializer.class) {
			public ASTNode sample(AST targetAst, boolean parented) {
				ArrayInitializer result = targetAst.newArrayInitializer();
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return ArrayInitializer that embeds x
				ArrayInitializer s1 = ast.newArrayInitializer();
				s1.expressions().add(x);
				return s1;
			}
			public void unwrap() {
				ArrayInitializer s1 = (ArrayInitializer) x.getParent();
				s1.expressions().remove(x);
			}
			public ASTNode get() {
				return x.getInitializer();
			}
			public void set(ASTNode value) {
				x.setInitializer((ArrayInitializer) value);
			}
		});
	}

	public void testParenthesizedExpression() {
		long previousCount = ast.modificationCount();
		final ParenthesizedExpression x = ast.newParenthesizedExpression();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x instanceof Expression);
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) {
			public ASTNode sample(AST ast, boolean parented) {
				Expression result = ast.newSimpleName("foo");
				if (parented) {
					ast.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie"));
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	public void testModifiers() {
		
		// check all modifiers match their JVM spec values
		assertTrue(Modifier.ABSTRACT == 0x0400);
		assertTrue(Modifier.FINAL == 0x0010);
		assertTrue(Modifier.NATIVE == 0x0100);
		assertTrue(Modifier.NONE == 0x0000);
		assertTrue(Modifier.PRIVATE == 0x0002);
		assertTrue(Modifier.PROTECTED == 0x0004);
		assertTrue(Modifier.PUBLIC == 0x0001);
		assertTrue(Modifier.STATIC == 0x0008);
		assertTrue(Modifier.STRICTFP == 0x0800);
		assertTrue(Modifier.SYNCHRONIZED == 0x0020);
		assertTrue(Modifier.TRANSIENT == 0x0080);
		assertTrue(Modifier.VOLATILE == 0x0040);
		
		// check that all
		int[] mods =
			{
				Modifier.ABSTRACT,
				Modifier.FINAL,
				Modifier.NATIVE,
				Modifier.PRIVATE,
				Modifier.PROTECTED,
				Modifier.PUBLIC,
				Modifier.STATIC,
				Modifier.STRICTFP,
				Modifier.SYNCHRONIZED,
				Modifier.TRANSIENT,
				Modifier.VOLATILE,
				};
				
		for (int i=0; i< mods.length; i++) {
			int m = mods[i];
			assertTrue(Modifier.isAbstract(m) == (m == Modifier.ABSTRACT));
			assertTrue(Modifier.isFinal(m) == (m == Modifier.FINAL));
			assertTrue(Modifier.isNative(m) == (m == Modifier.NATIVE));
			assertTrue(Modifier.isPrivate(m) == (m == Modifier.PRIVATE));
			assertTrue(Modifier.isProtected(m) == (m == Modifier.PROTECTED));
			assertTrue(Modifier.isPublic(m) == (m == Modifier.PUBLIC));
			assertTrue(Modifier.isStatic(m) == (m == Modifier.STATIC));
			assertTrue(Modifier.isStrictfp(m) == (m == Modifier.STRICTFP));
			assertTrue(Modifier.isSynchronized(m) == (m == Modifier.SYNCHRONIZED));
			assertTrue(Modifier.isTransient(m) == (m == Modifier.TRANSIENT));
			assertTrue(Modifier.isVolatile(m) == (m == Modifier.VOLATILE));
		}
	}
		
	public void testSubtreeBytes() {
		
		ASTNode x = oneOfEach(ast);
		System.out.println("oneOfEach().subtreeBytes(): " + x.subtreeBytes());
		assertTrue(x.subtreeBytes() > 0);
	}
	
	public void testNodeTypeConstants() {
		// it would be a breaking API change to change the numeric values of
		// public static final ints
		assertTrue(ASTNode.ANONYMOUS_CLASS_DECLARATION == 1);
		assertTrue(ASTNode.ARRAY_ACCESS == 2);
		assertTrue(ASTNode.ARRAY_CREATION == 3);
		assertTrue(ASTNode.ARRAY_INITIALIZER == 4);
		assertTrue(ASTNode.ARRAY_TYPE == 5);
		assertTrue(ASTNode.ASSERT_STATEMENT == 6);
		assertTrue(ASTNode.ASSIGNMENT == 7);
		assertTrue(ASTNode.BLOCK == 8);
		assertTrue(ASTNode.BOOLEAN_LITERAL == 9);
		assertTrue(ASTNode.BREAK_STATEMENT == 10);
		assertTrue(ASTNode.CAST_EXPRESSION == 11);
		assertTrue(ASTNode.CATCH_CLAUSE == 12);
		assertTrue(ASTNode.CHARACTER_LITERAL == 13);
		assertTrue(ASTNode.CLASS_INSTANCE_CREATION == 14);
		assertTrue(ASTNode.COMPILATION_UNIT == 15);
		assertTrue(ASTNode.CONDITIONAL_EXPRESSION == 16);
		assertTrue(ASTNode.CONSTRUCTOR_INVOCATION == 17);
		assertTrue(ASTNode.CONTINUE_STATEMENT == 18);
		assertTrue(ASTNode.DO_STATEMENT == 19);
		assertTrue(ASTNode.EMPTY_STATEMENT == 20);
		assertTrue(ASTNode.EXPRESSION_STATEMENT == 21);
		assertTrue(ASTNode.FIELD_ACCESS == 22);
		assertTrue(ASTNode.FIELD_DECLARATION == 23);
		assertTrue(ASTNode.FOR_STATEMENT == 24);
		assertTrue(ASTNode.IF_STATEMENT == 25);
		assertTrue(ASTNode.IMPORT_DECLARATION == 26);
		assertTrue(ASTNode.INFIX_EXPRESSION == 27);
		assertTrue(ASTNode.INITIALIZER == 28);
		assertTrue(ASTNode.JAVADOC == 29);
		assertTrue(ASTNode.LABELED_STATEMENT == 30);
		assertTrue(ASTNode.METHOD_DECLARATION == 31);
		assertTrue(ASTNode.METHOD_INVOCATION == 32);
		assertTrue(ASTNode.NULL_LITERAL == 33);
		assertTrue(ASTNode.NUMBER_LITERAL == 34);
		assertTrue(ASTNode.PACKAGE_DECLARATION == 35);
		assertTrue(ASTNode.PARENTHESIZED_EXPRESSION == 36);
		assertTrue(ASTNode.POSTFIX_EXPRESSION == 37);
		assertTrue(ASTNode.PREFIX_EXPRESSION == 38);
		assertTrue(ASTNode.PRIMITIVE_TYPE == 39);
		assertTrue(ASTNode.QUALIFIED_NAME == 40);
		assertTrue(ASTNode.RETURN_STATEMENT == 41);
		assertTrue(ASTNode.SIMPLE_NAME == 42);
		assertTrue(ASTNode.SIMPLE_TYPE == 43);
		assertTrue(ASTNode.SINGLE_VARIABLE_DECLARATION == 44);
		assertTrue(ASTNode.STRING_LITERAL == 45);
		assertTrue(ASTNode.SUPER_CONSTRUCTOR_INVOCATION == 46);
		assertTrue(ASTNode.SUPER_FIELD_ACCESS == 47);
		assertTrue(ASTNode.SUPER_METHOD_INVOCATION == 48);
		assertTrue(ASTNode.SWITCH_CASE == 49);
		assertTrue(ASTNode.SWITCH_STATEMENT == 50);
		assertTrue(ASTNode.SYNCHRONIZED_STATEMENT == 51);
		assertTrue(ASTNode.THIS_EXPRESSION == 52);
		assertTrue(ASTNode.THROW_STATEMENT == 53);
		assertTrue(ASTNode.TRY_STATEMENT == 54);
		assertTrue(ASTNode.TYPE_DECLARATION == 55);
		assertTrue(ASTNode.TYPE_DECLARATION_STATEMENT == 56);
		assertTrue(ASTNode.TYPE_LITERAL == 57);
		assertTrue(ASTNode.VARIABLE_DECLARATION_EXPRESSION == 58);
		assertTrue(ASTNode.VARIABLE_DECLARATION_FRAGMENT == 59);
		assertTrue(ASTNode.VARIABLE_DECLARATION_STATEMENT == 60);
		assertTrue(ASTNode.WHILE_STATEMENT == 61);
		assertTrue(ASTNode.INSTANCEOF_EXPRESSION == 62);
		
		// ensure that all constants are distinct, positive, and small
		// (this may seem paranoid, but this test did uncover a stupid bug!)
		int[] all= {
              ASTNode.ANONYMOUS_CLASS_DECLARATION,
              ASTNode.ARRAY_ACCESS,
              ASTNode.ARRAY_CREATION,
              ASTNode.ARRAY_INITIALIZER,
              ASTNode.ARRAY_TYPE,
              ASTNode.ASSERT_STATEMENT,
              ASTNode.ASSIGNMENT,
              ASTNode.BLOCK,
              ASTNode.BOOLEAN_LITERAL,
              ASTNode.BREAK_STATEMENT,
              ASTNode.CAST_EXPRESSION,
              ASTNode.CATCH_CLAUSE,
              ASTNode.CHARACTER_LITERAL,
              ASTNode.CLASS_INSTANCE_CREATION,
              ASTNode.COMPILATION_UNIT,
              ASTNode.CONDITIONAL_EXPRESSION,
              ASTNode.CONSTRUCTOR_INVOCATION,
              ASTNode.CONTINUE_STATEMENT,
              ASTNode.DO_STATEMENT,
              ASTNode.EMPTY_STATEMENT,
              ASTNode.EXPRESSION_STATEMENT,
              ASTNode.FIELD_ACCESS,
              ASTNode.FIELD_DECLARATION,
              ASTNode.FOR_STATEMENT,
              ASTNode.IF_STATEMENT,
              ASTNode.IMPORT_DECLARATION,
              ASTNode.INFIX_EXPRESSION,
              ASTNode.INSTANCEOF_EXPRESSION,
              ASTNode.INITIALIZER,
              ASTNode.JAVADOC,
              ASTNode.LABELED_STATEMENT,
              ASTNode.METHOD_DECLARATION,
              ASTNode.METHOD_INVOCATION,
              ASTNode.NULL_LITERAL,
              ASTNode.NUMBER_LITERAL,
              ASTNode.PACKAGE_DECLARATION,
              ASTNode.PARENTHESIZED_EXPRESSION,
              ASTNode.POSTFIX_EXPRESSION,
              ASTNode.PREFIX_EXPRESSION,
              ASTNode.PRIMITIVE_TYPE,
              ASTNode.QUALIFIED_NAME,
              ASTNode.RETURN_STATEMENT,
              ASTNode.SIMPLE_NAME,
              ASTNode.SIMPLE_TYPE,
              ASTNode.SINGLE_VARIABLE_DECLARATION,
              ASTNode.STRING_LITERAL,
              ASTNode.SUPER_CONSTRUCTOR_INVOCATION,
              ASTNode.SUPER_FIELD_ACCESS,
              ASTNode.SUPER_METHOD_INVOCATION,
              ASTNode.SWITCH_CASE,
              ASTNode.SWITCH_STATEMENT,
              ASTNode.SYNCHRONIZED_STATEMENT,
              ASTNode.THIS_EXPRESSION,
              ASTNode.THROW_STATEMENT,
              ASTNode.TRY_STATEMENT,
              ASTNode.TYPE_DECLARATION,
              ASTNode.TYPE_DECLARATION_STATEMENT,
              ASTNode.TYPE_LITERAL,
              ASTNode.VARIABLE_DECLARATION_EXPRESSION,
              ASTNode.VARIABLE_DECLARATION_FRAGMENT,
              ASTNode.VARIABLE_DECLARATION_STATEMENT,
              ASTNode.WHILE_STATEMENT,

		};
		int MIN = 1;
		int MAX = 100;
		Set s = new HashSet();
		for (int i=0; i<all.length; i++) {
			assertTrue(MIN <= all[i] && all[i] <= MAX);
			s.add(new Integer(all[i]));
		}
		assertTrue(s.size() == all.length);
		// ensure that Integers really do compare properly with equals
		assertTrue(new Integer(1).equals(new Integer(1)));
			
	}}

