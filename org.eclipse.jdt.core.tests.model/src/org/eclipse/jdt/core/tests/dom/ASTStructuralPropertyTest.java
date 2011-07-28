/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;

public class ASTStructuralPropertyTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase {

	/** @deprecated using deprecated code */
	public static Test suite() {
		// TODO (frederic) use buildList + setAstLevel(init) instead...
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTStructuralPropertyTest.class.getName());

		Class c = ASTStructuralPropertyTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTStructuralPropertyTest(methods[i].getName(), AST.JLS2));
				suite.addTest(new ASTStructuralPropertyTest(methods[i].getName(), AST.JLS3));
			}
		}
		return suite;
	}

	AST ast;
	ASTParser parser;
	int API_LEVEL;

	public ASTStructuralPropertyTest(String name, int apiLevel) {
		super(name);
		this.API_LEVEL = apiLevel;
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.ast = AST.newAST(this.API_LEVEL);
		this.parser = ASTParser.newParser(this.API_LEVEL);
	}

	protected void tearDown() throws Exception {
		this.ast = null;
		super.tearDown();
	}

	/** @deprecated using deprecated code */
	public String getName() {
		String name = super.getName();
		switch (this.API_LEVEL) {
			case AST.JLS2:
				name = "JLS2 - " + name;
				break;
			case AST.JLS3:
				name = "JLS3 - " + name;
				break;
		}
		return name;
	}

	public void testLocationInParent() {
		final ASTNode root = SampleASTs.oneOfEach(this.ast);
		ASTVisitor v = new ASTVisitor(true) {
			public void postVisit(ASTNode node) {
				StructuralPropertyDescriptor me = node.getLocationInParent();
				assertTrue(me != null || (node == root));
                ASTNode p = node.getParent();
                if (p != null) {
                    List parentProperties = p.structuralPropertiesForType();
                    boolean foundMe = false;
                    for (Iterator it = parentProperties.iterator(); it
                            .hasNext();) {
                        StructuralPropertyDescriptor prop =
                            (StructuralPropertyDescriptor) it.next();
                        if (me == prop || prop.getId().equals(me.getId())) {
                            foundMe = true;
                            break;
                        }
                    }
                    assertTrue(foundMe);
                }
			}
		};
		root.accept(v);
	}

	/**
	 * @deprecated since using deprecated constant
	 */
	public void testStructuralProperties() {
		final ASTNode root = SampleASTs.oneOfEach(this.ast);

		final Set simpleProperties = new HashSet(400);
		final Set childProperties = new HashSet(400);
		final Set childListProperties = new HashSet(400);
		final Set visitedProperties = new HashSet(400);
		final Set nodeClasses = new HashSet(100);

		ASTVisitor v = new ASTVisitor(true) {
			public void postVisit(ASTNode node) {
				StructuralPropertyDescriptor me = node.getLocationInParent();
				if (me != null) {
					visitedProperties.add(me);
				}
				visitedProperties.add(me);
				nodeClasses.add(node.getClass());
				List ps = node.structuralPropertiesForType();
				for (Iterator it = ps.iterator(); it.hasNext(); ) {
					StructuralPropertyDescriptor p = (StructuralPropertyDescriptor) it.next();
					Object o = node.getStructuralProperty(p);
					if (p.isSimpleProperty()) {
						simpleProperties.add(p);
						// slam simple properties
						node.setStructuralProperty(p, o);
					} else if (p.isChildProperty()) {
						childProperties.add(p);
						// replace child with a copy
						ASTNode copy = ASTNode.copySubtree(ASTStructuralPropertyTest.this.ast, (ASTNode) o);
						node.setStructuralProperty(p, copy);
					} else if (p.isChildListProperty()) {
						childListProperties.add(p);
						// replace child list with copies
						List list = (List) o;
						List copy = ASTNode.copySubtrees(ASTStructuralPropertyTest.this.ast, list);
						list.clear();
						list.addAll(copy);
					}
				}
			}
		};
		root.accept(v);
		switch(this.API_LEVEL) {
			case AST.JLS2 :
				assertEquals("Wrong number of visited node classes", 67, nodeClasses.size());
				assertEquals("Wrong number of visited properties", 82, visitedProperties.size());
				assertEquals("Wrong number of simple properties", 26, simpleProperties.size());
				assertEquals("Wrong number of child properties", 90, childProperties.size());
				assertEquals("Wrong number of child list properties", 26, childListProperties.size());
				break;
			case AST.JLS3 :
				assertEquals("Wrong number of visited node classes", 80, nodeClasses.size());
				assertEquals("Wrong number of visited properties", 104, visitedProperties.size());
				assertEquals("Wrong number of simple properties", 23, simpleProperties.size());
				assertEquals("Wrong number of child properties", 115, childProperties.size());
				assertEquals("Wrong number of child list properties", 52, childListProperties.size());
		}
		// visit should rebuild tree
		ASTNode newRoot = SampleASTs.oneOfEach(this.ast);
		assertTrue(root.subtreeMatch(new ASTMatcher(), newRoot));
	}

	public void testProtect() {
		final ASTNode root = SampleASTs.oneOfEach(this.ast);

		// check that all properties are again modifiable
		class Slammer extends ASTVisitor {
			boolean shouldBeProtected;
			Slammer(boolean shouldBeProtected){
				super(true); // visit doc
				this.shouldBeProtected = shouldBeProtected;
			}
			public void postVisit(ASTNode node) {
				try {
					node.setSourceRange(1, 1);
					assertTrue(!this.shouldBeProtected);
				} catch (RuntimeException e) {
					assertTrue(this.shouldBeProtected);
				}
				List ps = node.structuralPropertiesForType();
				for (Iterator it = ps.iterator(); it.hasNext(); ) {
					StructuralPropertyDescriptor p = (StructuralPropertyDescriptor) it.next();
					Object o = node.getStructuralProperty(p);
					if (p.isSimpleProperty()) {
						// slam simple properties
						try {
							node.setStructuralProperty(p, o);
							assertTrue(!this.shouldBeProtected);
						} catch (RuntimeException e) {
							assertTrue(this.shouldBeProtected);
						}
					} else if (p.isChildProperty()) {
						// replace child with a copy
						ASTNode copy = ASTNode.copySubtree(ASTStructuralPropertyTest.this.ast, (ASTNode) o);
						try {
							node.setStructuralProperty(p, copy);
							assertTrue(!this.shouldBeProtected);
						} catch (RuntimeException e) {
							assertTrue(this.shouldBeProtected);
						}
					} else if (p.isChildListProperty()) {
						// replace child list with copies
						List list = (List) o;
						List copy = ASTNode.copySubtrees(ASTStructuralPropertyTest.this.ast, list);
						if (!list.isEmpty()) {
							try {
								list.clear();
								assertTrue(!this.shouldBeProtected);
							} catch (RuntimeException e) {
								assertTrue(this.shouldBeProtected);
							}
							try {
								list.addAll(copy);
								assertTrue(!this.shouldBeProtected);
							} catch (RuntimeException e) {
								assertTrue(this.shouldBeProtected);
							}
						}
					}
				}
			}
		}

		class Protector extends ASTVisitor {
			boolean shouldBeProtected;
			Protector(boolean shouldBeProtected){
				super(true); // visit doc
				this.shouldBeProtected = shouldBeProtected;
			}
			public void preVisit(ASTNode node) {
				int f = node.getFlags();
				if (this.shouldBeProtected) {
					f |= ASTNode.PROTECT;
				} else {
					f &= ~ASTNode.PROTECT;
				}
				node.setFlags(f);
			}
		}


		// mark all nodes as protected
		root.accept(new Protector(true));
		root.accept(new Slammer(true));

		// mark all nodes as unprotected
		root.accept(new Protector(false));
		root.accept(new Slammer(false));
	}

	public void testDelete() {
		final ASTNode root = SampleASTs.oneOfEach(this.ast);

		// check that nodes can be deleted unless mandatory
		root.accept(new ASTVisitor(true) {
			public void postVisit(ASTNode node) {
				List ps = node.structuralPropertiesForType();
				for (Iterator it = ps.iterator(); it.hasNext(); ) {
					StructuralPropertyDescriptor p = (StructuralPropertyDescriptor) it.next();
					if (p.isChildProperty()) {
						ChildPropertyDescriptor c = (ChildPropertyDescriptor) p;
						ASTNode child = (ASTNode) node.getStructuralProperty(c);
						if (!c.isMandatory() && child != null) {
							try {
								child.delete();
								assertTrue(node.getStructuralProperty(c) == null);
						    } catch (RuntimeException e) {
							    assertTrue(false);
						    }
						}
					} else if (p.isChildListProperty()) {
						// replace child list with copies
						List list = (List) node.getStructuralProperty(p);
						// iterate over a copy and try removing all members
						List copy = new ArrayList();
						copy.addAll(list);
						for (Iterator it2 = copy.iterator(); it2.hasNext(); ) {
							ASTNode n = (ASTNode) it2.next();
							try {
								n.delete();
								assertTrue(!list.contains(n));
						    } catch (RuntimeException e) {
							    assertTrue(false);
						    }
						}
					}
				}
			}
		});
	}

	/** @deprecated using deprecated code */
	public void testCreateInstance() {
		for (int nodeType = 0; nodeType < 100; nodeType++) {
			Class nodeClass = null;
			try {
				nodeClass = ASTNode.nodeClassForType(nodeType);
			} catch (RuntimeException e) {
				// oops - guess that's not valid
			}
			if (nodeClass != null) {
				try {
					ASTNode node = this.ast.createInstance(nodeClass);
					if (this.ast.apiLevel() == AST.JLS2) {
						assertTrue((nodeType >= 1) && (nodeType <= 69));
					} else if (this.ast.apiLevel() == AST.JLS3) {
						assertTrue((nodeType >= 1) && (nodeType <= 83));
					} else if (this.ast.apiLevel() == AST.JLS4) {
						assertTrue((nodeType >= 1) && (nodeType <= 84));
					}
					assertTrue(node.getNodeType() == nodeType);
					//ASTNode node2 = ast.createInstance(nodeType);
					//assertTrue(node2.getNodeType() == nodeType);
				} catch (RuntimeException e) {
					if (this.ast.apiLevel() == AST.JLS2) {
						assertTrue((nodeType < 1) || (nodeType > 69));
					} else if (this.ast.apiLevel() == AST.JLS3) {
						assertTrue((nodeType < 1) || (nodeType > 83));
					} else if (this.ast.apiLevel() == AST.JLS4) {
						assertTrue((nodeType < 1) || (nodeType > 84));
					}
				}
			}
		}
	}

	public void testNodeClassForType() {
		Set classes = new HashSet(100);
		// make sure node types are contiguous starting at 0
		int hi = 0;
		for (int nodeType = 1; nodeType < 100; nodeType++) {
			try {
				Class nodeClass = ASTNode.nodeClassForType(nodeType);
				assertTrue(ASTNode.class.isAssignableFrom(nodeClass));
				classes.add(nodeClass);
				if (nodeType > 1) {
					assertTrue(hi == nodeType - 1);
				}
				hi = nodeType;
			} catch (RuntimeException e) {
				// oops - guess that's not valid
			}
		}
		assertEquals("Wrong last known type", 84, hi); // last known one
		assertEquals("Wrong number of distinct types",  hi, classes.size()); // all classes are distinct
	}
}
