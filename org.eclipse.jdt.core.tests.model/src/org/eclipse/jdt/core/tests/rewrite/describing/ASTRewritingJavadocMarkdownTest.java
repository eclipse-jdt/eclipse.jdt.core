/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingJavadocMarkdownTest extends ASTRewritingTest {

	public ASTRewritingJavadocMarkdownTest(String name) {
		super(name);
	}
	public ASTRewritingJavadocMarkdownTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingJavadocMarkdownTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpProjectAbove25();
	}

	public void testParamName_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name Hello World.
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);
			List fragments= tagElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			SimpleName name= (SimpleName) fragments.get(0);
			rewrite.replace(name, ast.newSimpleName("newName"), null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param newName Hello World.
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	public void testEmptyParamName_since_25() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=560055
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);

			TagElement newTagElement = ast.newTagElement();
			newTagElement.setTagName(TagElement.TAG_PARAM);

			SimpleName newName= ast.newSimpleName("newName");
			newTagElement.fragments().add(newName);

			rewrite.replace(tagElement, newTagElement, null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param newName
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	public void testEmptyThrows_since_25() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=560055
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @throws
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);

			TagElement newTagElement = ast.newTagElement();
			newTagElement.setTagName(TagElement.TAG_THROWS);

			SimpleName newName= ast.newSimpleName("Exception");
			newTagElement.fragments().add(newName);

			rewrite.replace(tagElement, newTagElement, null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @throws Exception
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	public void testSeeTag1_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @see String A String
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);
			List fragments= tagElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			SimpleName name= (SimpleName) fragments.get(0);
			rewrite.replace(name, ast.newSimpleName("Vector"), null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @see Vector A String
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testSeeTag2_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @see #toString A String
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);
			List fragments= tagElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			MemberRef ref= (MemberRef) fragments.get(0);
			rewrite.replace(ref.getName(), ast.newSimpleName("hashCode"), null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @see #hashCode A String
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testSeeTag3_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @see #toString A String
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);
			List fragments= tagElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			MemberRef ref= (MemberRef) fragments.get(0);
			rewrite.set(ref, MemberRef.QUALIFIER_PROPERTY, ast.newSimpleName("E"), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @see E#toString A String
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testSeeTagParamInsert1_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @see #toString() A String
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement tagElement= (TagElement) tags.get(0);
			List fragments= tagElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			MethodRef ref= (MethodRef) fragments.get(0);
			MethodRefParameter param= ast.newMethodRefParameter();
			param.setName(ast.newSimpleName("arg"));
			param.setType(ast.newPrimitiveType(PrimitiveType.INT));
			rewrite.getListRewrite(ref, MethodRef.PARAMETERS_PROPERTY).insertLast(param, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @see #toString(int arg) A String
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testSeeTagParamInsert2_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// {@link #toString(int x) A String}
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement topElement= (TagElement) tags.get(0);
			List fragments= topElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			TagElement tagElement= (TagElement) fragments.get(0);
			fragments= tagElement.fragments();
			assertTrue("Has fragments", !fragments.isEmpty());

			MethodRef ref= (MethodRef) fragments.get(0);
			MethodRefParameter param= ast.newMethodRefParameter();
			param.setName(ast.newSimpleName("arg"));
			param.setType(ast.newPrimitiveType(PrimitiveType.INT));
			rewrite.getListRewrite(ref, MethodRef.PARAMETERS_PROPERTY).insertLast(param, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// {@link #toString(int x, int arg) A String}
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testTagInsert1_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement newTag= ast.newTagElement();
			newTag.setTagName("@throws");
			newTag.fragments().add(ast.newSimpleName("Exception"));
			TextElement text= ast.newTextElement();
			text.setText("Thrown for no reason.");
			newTag.fragments().add(text);

			rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY).insertLast(newTag, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param name
			    /// @throws Exception Thrown for no reason.
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testTagInsert2_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement newTag= ast.newTagElement();
			newTag.setTagName("@see");
			MemberRef ref= ast.newMemberRef();
			ref.setQualifier(ast.newSimpleName("Vector"));
			ref.setName(ast.newSimpleName("size"));
			newTag.fragments().add(ref);

			rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY).insertFirst(newTag, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @see Vector#size
			    /// @param name
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testTagInsert3_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

				///
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 0);

			TagElement newTag= ast.newTagElement();
			newTag.setTagName(null);

			TextElement text= ast.newTextElement();
			text.setText("Comment");
			newTag.fragments().add(text);

			TagElement nested= ast.newTagElement();
			nested.setTagName("@link");

			newTag.fragments().add(nested);

			MethodRef ref= ast.newMethodRef();
			ref.setQualifier(ast.newSimpleName("Vector"));
			ref.setName(ast.newSimpleName("size"));
			nested.fragments().add(ref);

			TextElement textNested= ast.newTextElement();
			textNested.setText("Link");
			nested.fragments().add(textNested);

			rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY).insertFirst(newTag, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

				///
				/// Comment {@link Vector#size() Link}
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	public void testTagInsert4_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			TagElement newTag= ast.newTagElement();
			newTag.setTagName("@throws");
			List fragments= newTag.fragments();
			fragments.add(ast.newSimpleName("Exception"));
			TextElement element1 = ast.newTextElement();
			element1.setText("Description line 1\n/// Description line 2");
			fragments.add(element1);

			rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY).insertLast(newTag, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param name
			    /// @throws Exception Description line 1
			    /// Description line 2
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testTagRemove1_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 1);

			rewrite.remove((ASTNode) tags.get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    ///
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testTagRemove2_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name1 The first name.
			    /// @param name2 The second name.
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 2);

			rewrite.remove((ASTNode) tags.get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param name2 The second name.
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testTagRemove3_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name1 The first name.
			    /// @param name2 The second name.
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 2);

			rewrite.remove((ASTNode) tags.get(1), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param name1 The first name.
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	public void testTagRemoveInsert_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @param name1 The first name.
			    /// @param name2 The second name.
			    public void gee(String name) {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= methodDecl.getJavadoc();
			List tags= javadoc.tags();
			assertTrue("Has one tag", tags.size() == 2);

			ListRewrite listRewrite= rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY);
			listRewrite.remove((ASTNode) tags.get(1), null);

			AST ast= astRoot.getAST();
			TagElement element= ast.newTagElement();
			element.setTagName("@since");

			TextElement textElement= ast.newTextElement();
			textElement.setText("1.1");
			element.fragments().add(textElement);

			listRewrite.insertLast(element, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @param name1 The first name.
			    /// @since 1.1
			    public void gee(String name) {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	// TODO: need preference to state that markdown is preferred over old style Javadoc
	public void testAddJavadoc2_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    public int count;
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			FieldDeclaration fieldDecl= type.getFields()[0];

			Javadoc javadoc= ast.newJavadoc();
			TagElement element= ast.newTagElement();
			element.setTagName("@since");

			TextElement textElement= ast.newTextElement();
			textElement.setText("1.1");
			element.fragments().add(textElement);
			javadoc.tags().add(element);

			rewrite.set(fieldDecl, FieldDeclaration.JAVADOC_PROPERTY, javadoc, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /**
			     * @since 1.1
			     */
			    public int count;
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveJavadoc_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @since 1.1
			    static {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			Initializer initializer= (Initializer) type.bodyDeclarations().get(0);
			rewrite.remove(initializer.getJavadoc(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    static {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveJavadoc2_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			/// @since 1.1
			public class E {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");

			rewrite.remove(type.getJavadoc(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {
			}
			""";
		assertEqualString(preview, str1);

	}
	public void testMoveTags_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// @author Mr X
			    ///         and friends
			    /// @since 1.1
			    ///         maybe less
			    static {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			Initializer initializer= (Initializer) type.bodyDeclarations().get(0);
			Javadoc javadoc = initializer.getJavadoc();
			List tags= javadoc.tags();
			ASTNode node1 = (ASTNode) tags.get(0);
			ASTNode placeholder1 = rewrite.createMoveTarget(node1);
			ASTNode node2 = (ASTNode) tags.get(1);
			ASTNode placeholder2 = rewrite.createMoveTarget(node2);

			rewrite.replace(node1, placeholder2, null);
			rewrite.replace(node2, placeholder1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @since 1.1
			    ///         maybe less
			    /// @author Mr X
			    ///         and friends
			    static {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testChangeTagElement_since_25() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class E {

			    /// Mr X
			    /// @author Mr X
			    /// @author Mr X
			    static {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			Initializer initializer= (Initializer) type.bodyDeclarations().get(0);
			Javadoc javadoc = initializer.getJavadoc();
			List tags= javadoc.tags();
			TagElement elem1= (TagElement) tags.get(0);
			rewrite.set(elem1, TagElement.TAG_NAME_PROPERTY, "@param", null);

			TagElement elem2= (TagElement) tags.get(1);
			rewrite.set(elem2, TagElement.TAG_NAME_PROPERTY, "@param", null);

			TagElement elem3= (TagElement) tags.get(2);
			rewrite.set(elem3, TagElement.TAG_NAME_PROPERTY, null, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class E {

			    /// @paramMr X
			    /// @param Mr X
			    ///  Mr X
			    static {
			    }
			}
			""";
		assertEqualString(preview, str1);

	}


}
