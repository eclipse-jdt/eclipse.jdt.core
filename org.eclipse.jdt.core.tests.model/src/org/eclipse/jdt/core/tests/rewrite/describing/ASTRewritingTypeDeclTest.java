/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingTypeDeclTest extends ASTRewritingTest {

	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_TYPE_MODIFIERS_PROPERTY = TypeDeclaration.MODIFIERS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final ChildPropertyDescriptor INTERNAL_TYPE_SUPERCLASS_PROPERTY = TypeDeclaration.SUPERCLASS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final ChildListPropertyDescriptor INTERNAL_TYPE_SUPER_INTERFACES_PROPERTY = TypeDeclaration.SUPER_INTERFACES_PROPERTY;
	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_FRAGMENT_EXTRA_DIMENSIONS_PROPERTY = VariableDeclarationFragment.EXTRA_DIMENSIONS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_VARIABLE_MODIFIERS_PROPERTY = SingleVariableDeclaration.MODIFIERS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_VARIABLE_EXTRA_DIMENSIONS_PROPERTY = SingleVariableDeclaration.EXTRA_DIMENSIONS_PROPERTY;


	public ASTRewritingTypeDeclTest(String name) {
		super(name);
	}
	public ASTRewritingTypeDeclTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingTypeDeclTest.class);
	}
	/**
	 * @deprecated references deprecated old AST level
	 */
	protected static int getAST8() {
		return AST.JLS8;
	}
	/** @deprecated using deprecated code */
	public void testTypeDeclChanges_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // rename type, rename supertype, rename first interface, replace inner class with field
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			SimpleName name= type.getName();
			SimpleName newName= ast.newSimpleName("X");

			rewrite.replace(name, newName, null);

			Name superClass= type.getSuperclass();
			assertTrue("Has super type", superClass != null);

			SimpleName newSuperclass= ast.newSimpleName("Object");
			rewrite.replace(superClass, newSuperclass, null);

			List superInterfaces= type.superInterfaces();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());

			SimpleName newSuperinterface= ast.newSimpleName("Cloneable");
			rewrite.replace((ASTNode) superInterfaces.get(0), newSuperinterface, null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			FieldDeclaration newFieldDecl= createNewField(ast, "fCount");

			rewrite.replace((ASTNode) members.get(0), newFieldDecl, null);
		}
		{ // replace method in F, change to interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");

			// change flags
			int newModifiers= 0;
			rewrite.set(type, INTERNAL_TYPE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", members.size() == 1);

			MethodDeclaration methodDecl= createNewMethod(ast, "newFoo", true);

			rewrite.replace((ASTNode) members.get(0), methodDecl, null);
		}

		{ // change to class, add supertype
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");

			// change flags
			int newModifiers= 0;
			rewrite.set(type, INTERNAL_TYPE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			// change to class
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.FALSE, null);


			SimpleName newSuperclass= ast.newSimpleName("Object");
			rewrite.set(type, INTERNAL_TYPE_SUPERCLASS_PROPERTY, newSuperclass, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X extends Object implements Cloneable, Serializable {\n");
		buf.append("    private double fCount;\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface F extends Runnable {\n");
		buf.append("    private abstract void newFoo(String str);\n");
		buf.append("}\n");
		buf.append("class G extends Object {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308754
	public void testTypeDeclarationChange() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C {}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		{
			// change to interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "C");

			astRoot.recordModifications();
			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public interface C {}");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308754
	public void testTypeDeclarationChange2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("@A(X.class) public class C {}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		{
			// change to interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "C");

			astRoot.recordModifications();
			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("@A(X.class) public interface C {}");
		assertEqualString(preview, buf.toString());
	}
	public void testTypeDeclChanges2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("final class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // rename type, rename supertype, rename first interface, replace inner class with field
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			SimpleName name= type.getName();
			SimpleName newName= ast.newSimpleName("X");

			rewrite.replace(name, newName, null);

			Type superClass= type.getSuperclassType();
			assertTrue("Has super type", superClass != null);

			Type newSuperclass= ast.newSimpleType(ast.newSimpleName("Object"));
			rewrite.replace(superClass, newSuperclass, null);

			List superInterfaces= type.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());

			Type newSuperinterface= ast.newSimpleType(ast.newSimpleName("Cloneable"));
			rewrite.replace((ASTNode) superInterfaces.get(0), newSuperinterface, null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			FieldDeclaration newFieldDecl= createNewField(ast, "fCount");

			rewrite.replace((ASTNode) members.get(0), newFieldDecl, null);
		}
		{ // replace method in F, change to interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");

			rewrite.remove((ASTNode) type.modifiers().get(0), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", members.size() == 1);

			MethodDeclaration methodDecl= createNewMethod(ast, "newFoo", true);

			rewrite.replace((ASTNode) members.get(0), methodDecl, null);
		}

		{ // add modifier, change to class, add supertype
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");

			rewrite.getListRewrite(type, TypeDeclaration.MODIFIERS2_PROPERTY).insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// change to class
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.FALSE, null);


			Type newSuperclass= ast.newSimpleType(ast.newSimpleName("Object"));
			rewrite.set(type, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X extends Object implements Cloneable, Serializable {\n");
		buf.append("    private double fCount;\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface F extends Runnable {\n");
		buf.append("    private abstract void newFoo(String str);\n");
		buf.append("}\n");
		buf.append("final class G extends Object {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	/** @deprecated using deprecated code */
	public void testTypeDeclRemoves_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		{ // change to interface, remove supertype, remove first interface, remove field
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");

			// change flags
			int newModifiers= 0;
			rewrite.set(type, INTERNAL_TYPE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);

			Name superClass= type.getSuperclass();
			assertTrue("Has super type", superClass != null);

			rewrite.remove(superClass, null);

			List superInterfaces= type.superInterfaces();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());

			rewrite.remove((ASTNode) superInterfaces.get(0), null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			rewrite.remove((ASTNode) members.get(1), null);

			MethodDeclaration meth= findMethodDeclaration(type, "hee");
			rewrite.remove(meth, null);
		}
		{ // remove superinterface & method, change to interface & final
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");

			// change flags
			int newModifiers= Modifier.FINAL;
			rewrite.set(type, INTERNAL_TYPE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			// change to interface
			rewrite.set(type, TypeDeclaration.INTERFACE_PROPERTY, Boolean.TRUE, null);

			List superInterfaces= type.superInterfaces();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			rewrite.remove((ASTNode) superInterfaces.get(0), null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", members.size() == 1);

			rewrite.remove((ASTNode) members.get(0), null);
		}
		{ // remove class G
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");
			rewrite.remove(type, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("interface E extends Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("final interface F {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclInserts_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Errors in AST", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AST ast= astRoot.getAST();
		{ // add interface & set to final
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.FINAL;
			rewrite.set(type, INTERNAL_TYPE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			SimpleName newSuperinterface= ast.newSimpleName("Cloneable");

			rewrite.getListRewrite(type, INTERNAL_TYPE_SUPER_INTERFACES_PROPERTY).insertFirst(newSuperinterface, null);

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			assertTrue("Cannot find inner class", members.get(0) instanceof TypeDeclaration);
			TypeDeclaration innerType= (TypeDeclaration) members.get(0);

/*		bug 22161
			SimpleName newSuperclass= ast.newSimpleName("Exception");
			innerType.setSuperclass(newSuperclass);
			rewrite.markAsInserted(newSuperclass);
*/

			FieldDeclaration newField= createNewField(ast, "fCount");

			rewrite.getListRewrite(innerType, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newField, null);

			MethodDeclaration newMethodDecl= createNewMethod(ast, "newMethod", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAt(newMethodDecl, 4, null);
		}
		{ // add exception, add method
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");

			SimpleName newSuperclass= ast.newSimpleName("Exception");
			rewrite.set(type, INTERNAL_TYPE_SUPERCLASS_PROPERTY, newSuperclass, null);

			MethodDeclaration newMethodDecl= createNewMethod(ast, "newMethod", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethodDecl, null);
		}
		{ // insert interface
			TypeDeclaration type= findTypeDeclaration(astRoot, "G");

			SimpleName newInterface= ast.newSimpleName("Runnable");
			rewrite.getListRewrite(type, INTERNAL_TYPE_SUPER_INTERFACES_PROPERTY).insertLast(newInterface, null);

			MethodDeclaration newMethodDecl= createNewMethod(ast, "newMethod", true);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethodDecl,  null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public final class E extends Exception implements Cloneable, Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        private double fCount;\n");
		buf.append("\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("    }\n");
		buf.append("    private void newMethod(String str) {\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class F extends Exception implements Runnable {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void newMethod(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G extends Runnable {\n");
		buf.append("\n");
		buf.append("    private abstract void newMethod(String str);\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclInsertFields1_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		buf.append("class F {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Errors in AST", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AST ast= astRoot.getAST();
		{
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");

			VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("x"));

			FieldDeclaration decl= ast.newFieldDeclaration(frag);
			decl.setType(ast.newPrimitiveType(PrimitiveType.INT));

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(decl, null);

		}
		{
			TypeDeclaration type= findTypeDeclaration(astRoot, "F");

			VariableDeclarationFragment frag1= ast.newVariableDeclarationFragment();
			frag1.setName(ast.newSimpleName("x"));

			FieldDeclaration decl1= ast.newFieldDeclaration(frag1);
			decl1.setType(ast.newPrimitiveType(PrimitiveType.INT));

			VariableDeclarationFragment frag2= ast.newVariableDeclarationFragment();
			frag2.setName(ast.newSimpleName("y"));

			FieldDeclaration decl2= ast.newFieldDeclaration(frag2);
			decl2.setType(ast.newPrimitiveType(PrimitiveType.INT));

			ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertFirst(decl1, null);
			listRewrite.insertAfter(decl2, decl1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    int x;\n");
		buf.append("}\n");
		buf.append("class F {\n");
		buf.append("\n");
		buf.append("    int x;\n");
		buf.append("    int y;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testTypeParameters_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class E extends A {}\n");
		buf.append("class F {}\n");
		buf.append("class G <T> extends A {}\n");
		buf.append("class H <T> {}\n");
		buf.append("class I<T> extends A {}\n");
		buf.append("class J<T>extends A {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		List types= astRoot.types();

		for (int i= 0; i < 2; i++) {
			// add type parameter
			TypeDeclaration typeDecl= (TypeDeclaration) types.get(i);
			ListRewrite listRewrite= rewrite.getListRewrite(typeDecl, TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName("X"));
			listRewrite.insertFirst(typeParameter, null);
		}
		for (int i= 2; i < 6; i++) {
			// remove type parameter
			TypeDeclaration typeDecl= (TypeDeclaration) types.get(i);
			rewrite.remove((ASTNode) typeDecl.typeParameters().get(0), null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class E<X> extends A {}\n");
		buf.append("class F<X> {}\n");
		buf.append("class G extends A {}\n");
		buf.append("class H {}\n");
		buf.append("class I extends A {}\n");
		buf.append("class J extends A {}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testBug22161() throws Exception {
	//	System.out.println(getClass().getName()+"::" + getName() +" disabled (bug 22161)");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class T extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		assertTrue("Errors in AST", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "T");
		assertTrue("Outer type not found", type != null);

		List members= type.bodyDeclarations();
		assertTrue("Cannot find inner class", members.size() == 1 &&  members.get(0) instanceof TypeDeclaration);

		TypeDeclaration innerType= (TypeDeclaration) members.get(0);

		SimpleName name= innerType.getName();
		assertTrue("Name positions not correct", name.getStartPosition() != -1 && name.getLength() > 0);

	}

	public void testAnonymousClassDeclaration_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E2 {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new Runnable() {\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("            int i= 8;\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("            int i= 8;\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E2.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E2");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 3);
		{	// insert body decl in AnonymousClassDeclaration
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();
			AnonymousClassDeclaration anonym= creation.getAnonymousClassDeclaration();
			assertTrue("no anonym class decl", anonym != null);

			List decls= anonym.bodyDeclarations();
			assertTrue("Number of bodyDeclarations not 0", decls.size() == 0);

			MethodDeclaration newMethod= createNewMethod(ast, "newMethod", false);
			rewrite.getListRewrite(anonym, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newMethod, null);
		}
		{	// remove body decl in AnonymousClassDeclaration
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();
			AnonymousClassDeclaration anonym= creation.getAnonymousClassDeclaration();
			assertTrue("no anonym class decl", anonym != null);

			List decls= anonym.bodyDeclarations();
			assertTrue("Number of bodyDeclarations not 1", decls.size() == 1);

			rewrite.remove((ASTNode) decls.get(0), null);
		}
		{	// replace body decl in AnonymousClassDeclaration
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();
			AnonymousClassDeclaration anonym= creation.getAnonymousClassDeclaration();
			assertTrue("no anonym class decl", anonym != null);

			List decls= anonym.bodyDeclarations();
			assertTrue("Number of bodyDeclarations not 1", decls.size() == 1);

			MethodDeclaration newMethod= createNewMethod(ast, "newMethod", false);

			rewrite.replace((ASTNode) decls.get(0), newMethod, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E2 {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new Runnable() {\n");
		buf.append("\n");
		buf.append("            private void newMethod(String str) {\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("        };\n");
		buf.append("        new Runnable() {\n");
		buf.append("            private void newMethod(String str) {\n");
		buf.append("            }\n");
		buf.append("        };\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testImportDeclaration_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.net.*;\n");
		buf.append("import java.text.*;\n");
		buf.append("import static java.lang.Math.*;\n");
		buf.append("import java.lang.Math.*;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		List imports= astRoot.imports();
		assertTrue("Number of imports not 6", imports.size() == 6);

		{ // rename import
			ImportDeclaration imp= (ImportDeclaration) imports.get(0);

			Name name= ast.newName(new String[] { "org", "eclipse", "X" });
			rewrite.replace(imp.getName(), name, null);
		}
		{ // change to import on demand
			ImportDeclaration imp= (ImportDeclaration) imports.get(1);

			Name name= ast.newName(new String[] { "java", "util" });
			rewrite.replace(imp.getName(), name, null);

			rewrite.set(imp, ImportDeclaration.ON_DEMAND_PROPERTY, Boolean.TRUE, null);
		}
		{ // change to single import
			ImportDeclaration imp= (ImportDeclaration) imports.get(2);

			rewrite.set(imp, ImportDeclaration.ON_DEMAND_PROPERTY, Boolean.FALSE, null);
		}
		{ // rename import
			ImportDeclaration imp= (ImportDeclaration) imports.get(3);

			Name name= ast.newName(new String[] { "org", "eclipse" });
			rewrite.replace(imp.getName(), name, null);
		}
		{ // remove static
			ImportDeclaration imp= (ImportDeclaration) imports.get(4);

			rewrite.set(imp, ImportDeclaration.STATIC_PROPERTY, Boolean.FALSE, null);
		}
		{ // add static
			ImportDeclaration imp= (ImportDeclaration) imports.get(5);

			rewrite.set(imp, ImportDeclaration.STATIC_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import org.eclipse.X;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.net;\n");
		buf.append("import org.eclipse.*;\n");
		buf.append("import java.lang.Math.*;\n");
		buf.append("import static java.lang.Math.*;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testPackageDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		{ // rename package
			PackageDeclaration packageDeclaration= astRoot.getPackage();

			Name name= ast.newName(new String[] { "org", "eclipse" });

			rewrite.replace(packageDeclaration.getName(), name, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package org.eclipse;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testCompilationUnit() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		{
			PackageDeclaration packageDeclaration= astRoot.getPackage();
			rewrite.remove(packageDeclaration, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testCompilationUnit2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("public class Z {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Z.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		{
			PackageDeclaration packageDeclaration= ast.newPackageDeclaration();
			Name name= ast.newName(new String[] { "org", "eclipse" });
			packageDeclaration.setName(name);

			rewrite.set(astRoot, CompilationUnit.PACKAGE_PROPERTY, packageDeclaration, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package org.eclipse;\n");
		buf.append("public class Z {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262517
	public void testSingleMemberAnnotation1_since_3() throws Exception {
		String previousValue = null;
		try {
			previousValue = this.project1.getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_ANNOTATION, false);

			this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_ANNOTATION, JavaCore.INSERT);

			IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();

			{
				TypeDeclaration type= findTypeDeclaration(astRoot, "E");

				SingleMemberAnnotation newAnnot= ast.newSingleMemberAnnotation();

				newAnnot.setTypeName(ast.newName("SuppressWarnings"));

				StringLiteral newStringLiteral= ast.newStringLiteral();
				newStringLiteral.setLiteralValue("deprecation");
				newAnnot.setValue(newStringLiteral);

				ListRewrite modifiers= rewrite.getListRewrite(type, TypeDeclaration.MODIFIERS2_PROPERTY);
				modifiers.insertFirst(newAnnot, null);
			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("@SuppressWarnings (\"deprecation\")\n");
			buf.append("public class E {\n");
			buf.append("}\n");
			assertEqualString(preview, buf.toString());
		} finally {
			if (previousValue != null) {
				this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_ANNOTATION, previousValue);
			}
		}
	}

	public void testSingleVariableDeclaration_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, final int[] k, int[] x[]) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		List arguments= methodDecl.parameters();
		{ // add modifier, change type, change name, add extra dimension
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(0);

			int newModifiers= Modifier.FINAL;
			rewrite.set(decl, INTERNAL_VARIABLE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			rewrite.set(decl, INTERNAL_VARIABLE_EXTRA_DIMENSIONS_PROPERTY, 1, null);

			ArrayType newVarType= ast.newArrayType(ast.newPrimitiveType(PrimitiveType.FLOAT), 2);
			rewrite.replace(decl.getType(), newVarType, null);

			Name newName= ast.newSimpleName("count");
			rewrite.replace(decl.getName(), newName, null);
		}
		{ // remove modifier, change type
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(1);

			int newModifiers= 0;
			rewrite.set(decl, INTERNAL_VARIABLE_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			Type newVarType= ast.newPrimitiveType(PrimitiveType.FLOAT);
			rewrite.replace(decl.getType(), newVarType, null);
		}
		{ // remove extra dim
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(2);

			rewrite.set(decl, INTERNAL_VARIABLE_EXTRA_DIMENSIONS_PROPERTY, 0, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(final float[][] count[], float k, int[] x) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testVariableDeclarationFragment_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i, j, k= 0, x[][], y[]= {0, 1};\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);

		VariableDeclarationStatement variableDeclStatement= (VariableDeclarationStatement) statements.get(0);
		List fragments= variableDeclStatement.fragments();
		assertTrue("Number of fragments not 5", fragments.size() == 5);

		{ // rename var, add dimension
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(0);

			ASTNode name= ast.newSimpleName("a");
			rewrite.replace(fragment.getName(), name, null);

			rewrite.set(fragment, INTERNAL_FRAGMENT_EXTRA_DIMENSIONS_PROPERTY, 2, null);
		}

		{ // add initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(1);

			assertTrue("Has initializer", fragment.getInitializer() == null);

			Expression initializer= ast.newNumberLiteral("1");
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, initializer, null);
		}

		{ // remove initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(2);

			assertTrue("Has no initializer", fragment.getInitializer() != null);
			rewrite.remove(fragment.getInitializer(), null);
		}
		{ // add dimension, add initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(3);

			rewrite.set(fragment, INTERNAL_FRAGMENT_EXTRA_DIMENSIONS_PROPERTY, 4, null);

			assertTrue("Has initializer", fragment.getInitializer() == null);

			Expression initializer= ast.newNullLiteral();
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, initializer, null);
		}
		{ // remove dimension
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(4);

			rewrite.set(fragment, INTERNAL_FRAGMENT_EXTRA_DIMENSIONS_PROPERTY, 0, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int a[][], j = 1, k, x[][][][] = null, y= {0, 1};\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclSpacingMethods1_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			MethodDeclaration newMethodDecl= createNewMethod(ast, "foo", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(newMethodDecl, null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclSpacingMethods2_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			MethodDeclaration newMethodDecl= createNewMethod(ast, "foo", false);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newMethodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTypeDeclSpacingFields_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private int x;\n");
		buf.append("    private int y;\n");
		buf.append("\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // insert method at first position
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			FieldDeclaration newField= createNewField(ast, "fCount");
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(newField, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    private double fCount;\n");
		buf.append("    private int x;\n");
		buf.append("    private int y;\n");
		buf.append("\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    public void hee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testEnumDeclaration_since_3() throws Exception {
		// test the creation of an enum declaration

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List members= type.bodyDeclarations();
		assertTrue("Has declarations", members.isEmpty());

        ListRewrite declarations= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		{  // insert an enum inner class
	        EnumDeclaration enumD= ast.newEnumDeclaration();

	        // where fEnumName is a String
	        SimpleName enumName= ast.newSimpleName("MyEnum");
	        enumD.setName(enumName);
	        List enumStatements= enumD.enumConstants();

	        String[] names= { "a", "b", "c" };

	        // where fFieldsToExtract is an array of SimpleNames
	        for (int i= 0; i < names.length; i++) {
	            String curr= names[i];
	            EnumConstantDeclaration constDecl= ast.newEnumConstantDeclaration();
	            constDecl.setName(ast.newSimpleName(curr));
	            enumStatements.add(constDecl);
	        }

	        declarations.insertFirst(enumD, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("\n");
		buf.append("    enum MyEnum {\n");
		buf.append("        a, b, c\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testEnumDeclaration1_since_3() throws Exception {
		// test the creation of an enum declaration

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");

		List members= declaration.bodyDeclarations();
		assertTrue("Has declarations", members.isEmpty());

        ListRewrite declarations= rewrite.getListRewrite(declaration, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
        EnumConstantDeclaration constDecl= ast.newEnumConstantDeclaration();
        constDecl.setName(ast.newSimpleName("A"));

        declarations.insertFirst(constDecl, null);


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testEnumDeclaration2_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B, C\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");

		{
			// remove first, insert after 2nd
			rewrite.remove((ASTNode) declaration.enumConstants().get(0), null);

			EnumConstantDeclaration newEnumConstant = ast.newEnumConstantDeclaration();
			newEnumConstant.setName(ast.newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(declaration, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
			listRewrite.insertAfter(newEnumConstant, (ASTNode) declaration.enumConstants().get(1), null);

			// add body declaration

			ListRewrite bodyListRewrite= rewrite.getListRewrite(declaration, EnumDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyListRewrite.insertFirst(createNewMethod(ast, "foo", false), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    B, X, C;\n");
		buf.append("\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumDeclaration3_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B, C;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");

		{
			// remove first, insert after 2nd
			rewrite.remove((ASTNode) declaration.enumConstants().get(0), null);

			EnumConstantDeclaration newEnumConstant = ast.newEnumConstantDeclaration();
			newEnumConstant.setName(ast.newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(declaration, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
			listRewrite.insertAfter(newEnumConstant, (ASTNode) declaration.enumConstants().get(1), null);

			// add body declaration

			ListRewrite bodyListRewrite= rewrite.getListRewrite(declaration, EnumDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyListRewrite.insertFirst(createNewMethod(ast, "foo", false), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    B, X, C;\n");
		buf.append("\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumDeclaration4_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B, C;\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");
		{
			rewrite.remove((ASTNode) declaration.enumConstants().get(2), null);
			rewrite.remove((ASTNode) declaration.bodyDeclarations().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumDeclaration5_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B, C;\n");
		buf.append("\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");
		{

			EnumConstantDeclaration newEnumConstant = astRoot.getAST().newEnumConstantDeclaration();
			newEnumConstant.setName(astRoot.getAST().newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(declaration, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
			listRewrite.insertAfter(newEnumConstant, (ASTNode) declaration.enumConstants().get(2), null);

			rewrite.remove((ASTNode) declaration.bodyDeclarations().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A, B, C, X;\n");
		buf.append("\n");
		buf.append("    private void foo2(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumDeclaration6_since_3() throws Exception {
		// test the creation of an enum declaration

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    A\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");

		List members= declaration.bodyDeclarations();
		assertTrue("Has declarations", members.isEmpty());

		rewrite.remove((ASTNode) declaration.enumConstants().get(0), null);


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumDeclaration7_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration declaration= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "E");

		ListRewrite bodyListRewrite= rewrite.getListRewrite(declaration, EnumDeclaration.BODY_DECLARATIONS_PROPERTY);

		AST ast= astRoot.getAST();
		bodyListRewrite.insertFirst(createNewMethod(ast, "foo", false), null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum E {\n");
		buf.append("    ;\n");
		buf.append("\n");
		buf.append("    private void foo(String str) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testAnnotationTypeDeclaration1_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/**\n");
		buf.append(" * test\n");
		buf.append(" */\n");
		buf.append("public @interface E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{
			AnnotationTypeDeclaration type= (AnnotationTypeDeclaration) findAbstractTypeDeclaration(astRoot, "E");

			ListRewrite listRewrite= rewrite.getListRewrite(type, AnnotationTypeDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			SimpleName name= type.getName();
			SimpleName newName= ast.newSimpleName("X");

			rewrite.replace(name, newName, null);

			AnnotationTypeMemberDeclaration declaration= ast.newAnnotationTypeMemberDeclaration();
			declaration.setName(ast.newSimpleName("value"));
			declaration.setType(ast.newSimpleType(ast.newSimpleName("String")));

			ListRewrite bodyList= rewrite.getListRewrite(type, AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyList.insertFirst(declaration, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/**\n");
		buf.append(" * test\n");
		buf.append(" */\n");
		buf.append("final public @interface X {\n");
		buf.append("\n");
		buf.append("    String value();\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testWildcardType_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    X<?, ?, ? extends A, ? super B, ? extends A, ? super B> x;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		FieldDeclaration field= type.getFields()[0];
		ParameterizedType fieldType= (ParameterizedType) field.getType();
		List args= fieldType.typeArguments();
		{
			WildcardType wildcardType= (WildcardType) args.get(0);
			rewrite.set(wildcardType, WildcardType.UPPER_BOUND_PROPERTY, Boolean.TRUE, null);
			rewrite.set(wildcardType, WildcardType.BOUND_PROPERTY, ast.newSimpleType(ast.newSimpleName("A")), null);
		}
		{
			WildcardType wildcardType= (WildcardType) args.get(1);
			rewrite.set(wildcardType, WildcardType.UPPER_BOUND_PROPERTY, Boolean.FALSE, null);
			rewrite.set(wildcardType, WildcardType.BOUND_PROPERTY, ast.newSimpleType(ast.newSimpleName("B")), null);
		}
		{
			WildcardType wildcardType= (WildcardType) args.get(2);
			rewrite.set(wildcardType, WildcardType.UPPER_BOUND_PROPERTY, Boolean.FALSE, null);
			rewrite.set(wildcardType, WildcardType.BOUND_PROPERTY, ast.newSimpleType(ast.newSimpleName("B")), null);
		}
		{
			WildcardType wildcardType= (WildcardType) args.get(3);
			rewrite.set(wildcardType, WildcardType.UPPER_BOUND_PROPERTY, Boolean.TRUE, null);
			rewrite.set(wildcardType, WildcardType.BOUND_PROPERTY, ast.newSimpleType(ast.newSimpleName("A")), null);
		}
		{
			WildcardType wildcardType= (WildcardType) args.get(4);
			rewrite.set(wildcardType, WildcardType.BOUND_PROPERTY, null, null);
		}
		{
			WildcardType wildcardType= (WildcardType) args.get(5);
			rewrite.set(wildcardType, WildcardType.BOUND_PROPERTY, null, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    X<? extends A, ? super B, ? super B, ? extends A, ?, ?> x;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=396576
	public void testVariableDeclarationFragmentWithAnnot_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i, j, k = 0, x, y[][][], z @Annot1 [], zz @Annot2 @Annot2[] = {0, 1};\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED)== 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size()== 1);

		VariableDeclarationStatement variableDeclStatement= (VariableDeclarationStatement) statements.get(0);
		List fragments= variableDeclStatement.fragments();
		assertTrue("Number of fragments not 7", fragments.size()== 7);

		{ // rename var, add dimension with annotations
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(0);
			ASTNode name= ast.newSimpleName("a");
			rewrite.replace(fragment.getName(), name, null);

			ListRewrite listRewrite= rewrite.getListRewrite(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS2_PROPERTY);
			Dimension dim= ast.newDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 0, null);
		}
		{ // add initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(1);
			assertTrue("Has initializer", fragment.getInitializer()== null);
			Expression initializer= ast.newNumberLiteral("1");
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, initializer, null);
		}
		{ // remove initializer and add extra dimensions with annotations
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(2);
			assertTrue("Has no initializer", fragment.getInitializer() != null);
			rewrite.remove(fragment.getInitializer(), null);

			ListRewrite listRewrite= rewrite.getListRewrite(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS2_PROPERTY);
			Dimension dim= ast.newDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 0, null);
		}
		{ // add dimension, add initializer
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(3);
			assertTrue("Has initializer", fragment.getInitializer()== null);
			Expression initializer= ast.newNullLiteral();
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, initializer, null);

			ListRewrite listRewrite= rewrite.getListRewrite(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS2_PROPERTY);

			Dimension dim= ast.newDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 0, null);
		}
		{ // remove one dimension and add annotations for the rest of the dimensions
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(4);

			Dimension dim= (Dimension) fragment.extraDimensions().get(1);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			listRewrite.insertAt(markerAnnotation, 0, null);

			dim= (Dimension) fragment.extraDimensions().get(2);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.insertAt(markerAnnotation, 0, null);

			listRewrite= rewrite.getListRewrite(fragment, VariableDeclarationFragment.EXTRA_DIMENSIONS2_PROPERTY);
			listRewrite.remove((Dimension) fragment.extraDimensions().get(0), null);
		}
		{ // remove a fragment
			ListRewrite listRewrite= rewrite.getListRewrite(variableDeclStatement, VariableDeclarationStatement.FRAGMENTS_PROPERTY);
			VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(5);
			listRewrite.remove(fragment, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int a @Annot1 @Annot2 [], j = 1, k @Annot1 @Annot2 [], x @Annot1 @Annot2 [] = null, y @Annot1 [] @Annot2 [], zz @Annot2 @Annot2[] = {0, 1};\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		assertEqualString(preview, buf.toString());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=396576
	public void testSingleVariableDeclarationWithAnnotations_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int i, final int[] j @Annot1 @Annot2 [], int[] k @Annot1 @Annot3 [] @Annot2 @Annot3 [], int l []) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED)== 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		List arguments= methodDecl.parameters();

		{ // add modifier, move extra dimensions from one variable to another
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(0);
			SingleVariableDeclaration decl2= (SingleVariableDeclaration) arguments.get(1);
			Dimension dim= (Dimension) decl2.extraDimensions().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			listRewrite= rewrite.getListRewrite(decl2, SingleVariableDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
			listRewrite.remove(dim, null);
			listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
			listRewrite.insertAt(dim, 0, null);
		}
		{ // move annotations from one dim to another
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(2);
			Dimension dim1= (Dimension) decl.extraDimensions().get(0);
			Dimension dim2= (Dimension) decl.extraDimensions().get(1);
			Annotation annot1= (Annotation) dim1.annotations().get(0);
			Annotation annot2= (Annotation) dim2.annotations().get(0);

			ListRewrite listRewrite= rewrite.getListRewrite(dim1, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.replace(annot1, annot2, null);

			listRewrite= rewrite.getListRewrite(dim2, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.replace(annot2, annot1, null);
		}
		{ // remove extra dim
			SingleVariableDeclaration decl= (SingleVariableDeclaration) arguments.get(3);
			ListRewrite listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
			listRewrite.remove((Dimension) decl.extraDimensions().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(final int i @Annot1 @Annot2 [], final int[] j, int[] k @Annot2 @Annot3 [] @Annot1 @Annot3 [], int l) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		assertEqualString(preview, buf.toString());
	}

	// Bug 419057 - ITypeBinding#getModifiers() misses implicit "static" for class member interface
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419057
	public void testBug419057a() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    interface IC {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(getAST8(), cu, true, false);
		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (((TypeDeclaration) types.get(0)).getTypes())[0];
		ITypeBinding iTypeBinding = typeDeclaration.resolveBinding();
		assertTrue((iTypeBinding.getModifiers() & Modifier.STATIC) != 0);
	}
	public void testBug419057b() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public interface C {\n");
		buf.append("    interface IC {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(getAST8(), cu, true, false);
		List types = astRoot.types();
		TypeDeclaration outerTypeDeclaration = (TypeDeclaration) types.get(0);
		TypeDeclaration memberTypeDeclaration = (outerTypeDeclaration.getTypes())[0];
		ITypeBinding outerTypeBinding = outerTypeDeclaration.resolveBinding();
		assertTrue((outerTypeBinding.getModifiers() & Modifier.STATIC) == 0);
		ITypeBinding memberTypeBinding = memberTypeDeclaration.resolveBinding();
		assertTrue((memberTypeBinding.getModifiers() & Modifier.STATIC) != 0);
	}

	public void test401848_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int i;\n");
		buf.append("    public F f;\n");
		buf.append("}\n");
		buf.append("class F {}\n");
		buf.append("@Target (Element.FIELD);\n");
		buf.append("@interface Marker {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{  // Add an annotation to fields
			TypeDeclaration type= findTypeDeclaration(astRoot, "E");
			FieldDeclaration[] fields = type.getFields();
			{
				FieldDeclaration field = fields[0];
				MarkerAnnotation annot = ast.newMarkerAnnotation();
				annot.setTypeName(ast.newSimpleName("Marker"));
				ListRewrite listRewrite = rewrite.getListRewrite(field, FieldDeclaration.MODIFIERS2_PROPERTY);
				listRewrite.insertFirst(annot, null);
			}
			{
				FieldDeclaration field = fields[1];
				MarkerAnnotation annot = ast.newMarkerAnnotation();
				annot.setTypeName(ast.newQualifiedName(ast.newName("test1"), ast.newSimpleName("Marker")));
				ListRewrite listRewrite = rewrite.getListRewrite(field, FieldDeclaration.MODIFIERS2_PROPERTY);
				listRewrite.insertFirst(annot, null);
			}
		}

		String preview= evaluateRewrite(cu, rewrite);
		String expected = "package test1;\n" +
			"public class E {\n" +
			"    @Marker\n"+
			"    public int i;\n" +
			"    @test1.Marker\n" +
			"    public F f;\n" +
			"}\n" +
			"class F {}\n" +
			"@Target (Element.FIELD);\n" +
			"@interface Marker {}\n";
		assertEqualString(preview, expected);

	}
	public void testBug526097a() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.getPackageFragment(IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH);
		StringBuilder buf= new StringBuilder();
		buf.append("public class T");
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(getAST8(), cu, true, false);
		List<TypeDeclaration> types = astRoot.types();
		TypeDeclaration typeDeclaration = types.get(0);
		SimpleName simpleName = typeDeclaration.getName();
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		rewrite.replace(simpleName, ast.newSimpleName("Test"), null);
		String preview= evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();
		buf.append("public class Test");
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("deprecation")
	private boolean checkAPILevel(int level) {
		if (this.apiLevel != level) {
			System.err.println("Test "+getName()+" requires a JRE " + level);
			return true;
		}
		return false;
	}

	public void testSealedModifier_001() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();

			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			buf.append("non-sealed class  C2 extends C{}\n");

			ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // add 1 permits

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					SimpleType newPermits= ast.newSimpleType(ast.newSimpleName("C2"));
					listRewrite2.insertLast(newPermits, null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1, C2{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			buf.append("non-sealed class  C2 extends C{}\n");


			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}
	private void setProjectCompliance() {
		this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project1.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
		this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
		this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);
	}


	public void testSealedModifier_002() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public sealed class C permits C1, C2 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("final class  C1 extends C{\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("non-sealed class  C2 extends C{}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // remove permits

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					List permittedTypes = typeC.permittedTypes();
					listRewrite2.remove((SimpleType)permittedTypes.get(1), null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1 {\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("final class  C1 extends C{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("non-sealed class  C2 extends C{}\n");

			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}

	public void testSealedModifier_003() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class  C1 extends C{}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // add sealed and permits
					ListRewrite listRewrite= rewrite.getListRewrite(typeC, TypeDeclaration.MODIFIERS2_PROPERTY);
					listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.SEALED_KEYWORD), null);

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					SimpleType newPermits= ast.newSimpleType(ast.newSimpleName("C1"));
					listRewrite2.insertLast(newPermits, null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1 {\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("class  C1 extends C{}\n");

			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}

	public void testSealedModifier_004() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public sealed class C permits C1{}\n");
		buf.append("final class  C1 extends C{}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // remove last permit

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					List permittedTypes = typeC.permittedTypes();
					listRewrite2.remove((SimpleType)permittedTypes.get(0), null);

				// remove sealed
					ListRewrite listRewrite= rewrite.getListRewrite(typeC, TypeDeclaration.MODIFIERS2_PROPERTY);
					List modifiers = typeC.modifiers();
					listRewrite.remove((Modifier)modifiers.get(1), null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public class C{}\n");
			buf.append("final class  C1 extends C{}\n");

			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}

	public void testSealedModifier_005() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();

			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			buf.append("non-sealed class  C2 extends C{}\n");

			ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // replace permits

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					List permittedTypes = typeC.permittedTypes();
					listRewrite2.remove((SimpleType)permittedTypes.get(0), null);
					SimpleType newPermits= ast.newSimpleType(ast.newSimpleName("C2"));
					listRewrite2.insertLast(newPermits, null);

			}

			TypeDeclaration typeC1= findTypeDeclaration(astRoot, "C1");
			{
				// remove non-sealed
				ListRewrite listRewrite= rewrite.getListRewrite(typeC1, TypeDeclaration.MODIFIERS2_PROPERTY);
				List modifiers = typeC1.modifiers();
				listRewrite.remove((Modifier)modifiers.get(0), null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C2{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("class  C1 extends C{}\n");
			buf.append("non-sealed class  C2 extends C{}\n");


			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}

	public void testSealedModifier_006() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();

			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C3{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("non-sealed class  C3 extends C{}\n");
			buf.append("class  C1{\n");
			buf.append("final class  C2 extends C{}\n");
			buf.append("}\n");

			ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // add  permits for inner class

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					SimpleType newPermits= ast.newSimpleType(ast.newQualifiedName(ast.newName("C1"), ast.newSimpleName("C2")));
					listRewrite2.insertLast(newPermits, null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C3, C1.C2{\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("non-sealed class  C3 extends C{}\n");
			buf.append("class  C1{\n");
			buf.append("final class  C2 extends C{}\n");
			buf.append("}\n");


			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}

	public void testSealedModifier_007() throws Exception {

		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();
			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1 //comment\n");
			buf.append("{\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			buf.append("non-sealed class  C2 extends C{}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");

			{ // add 1 more permits

				ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
				SimpleType newPermits= ast.newSimpleType(ast.newSimpleName("C2"));
				listRewrite2.insertLast(newPermits, null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C permits C1 //comment\n");
			buf.append(", C2\n");
			buf.append("{\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			buf.append("non-sealed class  C2 extends C{}\n");
			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testSealedModifier_008() throws Exception {

		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();
			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public class C//comment\n");
			buf.append("{\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");

			{ // add 1 permits

				ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
				SimpleType newPermits= ast.newSimpleType(ast.newSimpleName("C1"));
				listRewrite2.insertLast(newPermits, null);

			}
			{ // add sealed
				ListRewrite listRewrite= rewrite.getListRewrite(typeC, TypeDeclaration.MODIFIERS2_PROPERTY);
				listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.SEALED_KEYWORD), null);
			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C//comment\n");
			buf.append(" permits C1\n");
			buf.append("{\n");
			buf.append("}\n");
			buf.append("non-sealed class  C1 extends C{}\n");
			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testSealedModifier_009() throws Exception {
		if (checkAPILevel(17)) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C implements B {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class  C1 extends C{}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String old = this.project1.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			setProjectCompliance();
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

			AST ast= astRoot.getAST();

			assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
			TypeDeclaration typeC= findTypeDeclaration(astRoot, "C");
			{ // add sealed and permits
					ListRewrite listRewrite= rewrite.getListRewrite(typeC, TypeDeclaration.MODIFIERS2_PROPERTY);
					listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.SEALED_KEYWORD), null);

					ListRewrite listRewrite2= rewrite.getListRewrite(typeC, TypeDeclaration.PERMITS_TYPES_PROPERTY);
					SimpleType newPermits= ast.newSimpleType(ast.newSimpleName("C1"));
					listRewrite2.insertLast(newPermits, null);

			}

			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public sealed class C implements B permits C1 {\n");
			buf.append("\n");
			buf.append("}\n");
			buf.append("class  C1 extends C{}\n");

			assertEqualString(preview, buf.toString());
		}finally {
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}

	}

}
