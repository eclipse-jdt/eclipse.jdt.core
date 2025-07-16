/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
public class ASTRewritingJavadocTest extends ASTRewritingTest {

	public ASTRewritingJavadocTest(String name) {
		super(name);
	}
	public ASTRewritingJavadocTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingJavadocTest.class);
	}

	public void testParamName() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name Hello World.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param newName Hello World.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEmptyParamName() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=560055
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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
			assertTrue("Has fragments", fragments.isEmpty());

			TagElement newTagElement = ast.newTagElement();
			newTagElement.setTagName(TagElement.TAG_PARAM);

			SimpleName newName= ast.newSimpleName("newName");
			newTagElement.fragments().add(newName);

			rewrite.replace(tagElement, newTagElement, null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param newName\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEmptyThrows() throws Exception {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=560055
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @throws\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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
			assertTrue("Has fragments", fragments.isEmpty());

			TagElement newTagElement = ast.newTagElement();
			newTagElement.setTagName(TagElement.TAG_THROWS);

			SimpleName newName= ast.newSimpleName("Exception");
			newTagElement.fragments().add(newName);

			rewrite.replace(tagElement, newTagElement, null);
			}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @throws Exception\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testSeeTag1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see String A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see Vector A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSeeTag2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see #toString A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see #hashCode A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testSeeTag3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see #toString A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see E#toString A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSeeTagParamInsert1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see #toString() A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see #toString(int arg) A String\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSeeTagParamInsert2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * {@link #toString(int x) A String}\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * {@link #toString(int x, int arg) A String}\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTagInsert1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name\n");
		buf.append("     * @throws Exception Thrown for no reason.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTagInsert2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @see Vector#size\n");
		buf.append("     * @param name\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTagInsert3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * Comment {@link Vector#size() Link}\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTagInsert4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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
			element1.setText("Description line 1\n * Description line 2");
			fragments.add(element1);

			rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY).insertLast(newTag, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name\n");
		buf.append("     * @throws Exception Description line 1\n");
		buf.append("     * Description line 2\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTagRemove1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTagRemove2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name1 The first name.\n");
		buf.append("     * @param name2 The second name.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name2 The second name.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTagRemove3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name1 The first name.\n");
		buf.append("     * @param name2 The second name.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name1 The first name.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTagRemove4_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("/**\n");
		buf.append(" * @author xy\n");
		buf.append(" */\n");
		buf.append("package test1;\n");
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		rewrite.remove(astRoot.getPackage().getJavadoc(), null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTagRemoveInsert() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name1 The first name.\n");
		buf.append("     * @param name2 The second name.\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @param name1 The first name.\n");
		buf.append("     * @since 1.1\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testAddJavadoc_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			Javadoc javadoc= ast.newJavadoc();
			TagElement element= ast.newTagElement();
			element.setTagName("@since");

			TextElement textElement= ast.newTextElement();
			textElement.setText("1.1");
			element.fragments().add(textElement);
			javadoc.tags().add(element);

			rewrite.set(methodDecl, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @since 1.1\n");
		buf.append("     */\n");
		buf.append("    public void gee(String name) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testAddJavadoc2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    public int count;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @since 1.1\n");
		buf.append("     */\n");
		buf.append("    public int count;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testRemoveJavadoc() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @since 1.1\n");
		buf.append("     */\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			Initializer initializer= (Initializer) type.bodyDeclarations().get(0);
			rewrite.remove(initializer.getJavadoc(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testRemoveJavadoc2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/**\n");
		buf.append(" * @since 1.1\n");
		buf.append(" */\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");

			rewrite.remove(type.getJavadoc(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	public void testMoveTags() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @author Mr X\n");
		buf.append("     *         and friends\n");
		buf.append("     * @since 1.1\n");
		buf.append("     *         maybe less\n");
		buf.append("     */\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @since 1.1\n");
		buf.append("     *         maybe less\n");
		buf.append("     * @author Mr X\n");
		buf.append("     *         and friends\n");
		buf.append("     */\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testChangeTagElement() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * Mr X\n");
		buf.append("     * @author Mr X\n");
		buf.append("     * @author Mr X\n");
		buf.append("     */\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     * @paramMr X\n");
		buf.append("     * @param Mr X\n");
		buf.append("     *  Mr X\n");
		buf.append("     */\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


}
