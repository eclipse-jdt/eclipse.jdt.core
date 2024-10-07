/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingMethodDeclTest extends ASTRewritingTest {

	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_FIELD_MODIFIERS_PROPERTY = FieldDeclaration.MODIFIERS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_INITIALIZER_MODIFIERS_PROPERTY = Initializer.MODIFIERS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_METHOD_MODIFIERS_PROPERTY = MethodDeclaration.MODIFIERS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final ChildPropertyDescriptor INTERNAL_METHOD_RETURN_TYPE_PROPERTY = MethodDeclaration.RETURN_TYPE_PROPERTY;
	/** @deprecated using deprecated code */
	private static final SimplePropertyDescriptor INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY = MethodDeclaration.EXTRA_DIMENSIONS_PROPERTY;
	/** @deprecated using deprecated code */
	private static final ChildListPropertyDescriptor INTERNAL_METHOD_THROWN_EXCEPTIONS_PROPERTY = MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY;

	public ASTRewritingMethodDeclTest(String name) {
		super(name);
	}

	public ASTRewritingMethodDeclTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingMethodDeclTest.class);
	}

	/** @deprecated using deprecated code */
	private Type getReturnType(MethodDeclaration methodDecl) {
		return this.apiLevel < AST.JLS3 ? methodDecl.getReturnType() : methodDecl.getReturnType2();
	}

	/** @deprecated using deprecated code */
	private ChildPropertyDescriptor getMethodReturnTypeProperty(AST ast) {
		return ast.apiLevel() < AST.JLS3 ? INTERNAL_METHOD_RETURN_TYPE_PROPERTY : MethodDeclaration.RETURN_TYPE2_PROPERTY;
	}

	/** @deprecated using deprecated code */
	private ASTNode createNewExceptionType(AST ast, String name) {
		return ast.apiLevel() < AST.JLS8 ? ast.newSimpleName(name) : (ASTNode) ast.newSimpleType(ast.newSimpleName(name));
	}

	/** @deprecated using deprecated code */
	private List getThrownExceptions(MethodDeclaration methodDecl) {
		return this.apiLevel < AST.JLS8 ? methodDecl.thrownExceptions() : methodDecl.thrownExceptionTypes();
	}

	/** @deprecated using deprecated code */
	private ChildListPropertyDescriptor getMethodThrownExceptionsProperty(AST ast) {
		return ast.apiLevel() < AST.JLS8 ? INTERNAL_METHOD_THROWN_EXCEPTIONS_PROPERTY : MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY;
	}

	/** @deprecated using deprecated code */
	private void setModifiers(ASTRewrite rewrite, MethodDeclaration methodDecl, int newModifiers) {
		if (this.apiLevel < AST.JLS3) {
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);
		} else {
			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			for (Iterator iter= listRewrite.getOriginalList().iterator(); iter.hasNext(); ) {
				ASTNode modifier= (ASTNode) iter.next();
				listRewrite.remove(modifier, null);
			}
			List newModifierNodes = methodDecl.getAST().newModifiers(newModifiers);
			for (Iterator iter= newModifierNodes.iterator(); iter.hasNext(); ) {
				Modifier modifier= (Modifier) iter.next();
				listRewrite.insertLast(modifier, null);
			}
		}
	}

	/** @deprecated using deprecated code */
	private void setExtraDimensions(ASTRewrite rewrite, MethodDeclaration methodDecl, int extraDimensions) {
		if (this.apiLevel < AST.JLS8) {
			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, Integer.valueOf(extraDimensions), null);
		} else {
			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
			for (Iterator iter= listRewrite.getOriginalList().iterator(); iter.hasNext(); ) {
				ASTNode extraDimension= (ASTNode) iter.next();
				listRewrite.remove(extraDimension, null);
			}
			for (int i= 0; i < extraDimensions; i++) {
				listRewrite.insertFirst(methodDecl.getAST().newDimension(), null);
			}
		}
	}

	public void testMethodDeclChanges() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // convert constructor to method: insert return type
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, getMethodReturnTypeProperty(ast), newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}
		{ // change return type
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");
			assertTrue("Has no return type: gee", getReturnType(methodDecl) != null);

			Type returnType= getReturnType(methodDecl);
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			rewrite.replace(returnType, newReturnType, null);
		}
		{ // remove return type
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");
			assertTrue("Has no return type: hee", getReturnType(methodDecl) != null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}
		{ // rename method name
			MethodDeclaration methodDecl= findMethodDeclaration(type, "iee");

			SimpleName name= methodDecl.getName();
			SimpleName newName= ast.newSimpleName("xii");

			rewrite.replace(name, newName, null);
		}
		{ // rename first param & last throw statement
			MethodDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.replace((ASTNode) parameters.get(0), newParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			ASTNode newThrownException= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException, null);
		}
		{ // rename first and second param & rename first and last exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.replace((ASTNode) parameters.get(0), newParam1, null);
			rewrite.replace((ASTNode) parameters.get(1), newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			ASTNode newThrownException1= createNewExceptionType(ast, "ArrayStoreException");
			ASTNode newThrownException2= createNewExceptionType(ast, "InterruptedException");
			rewrite.replace((ASTNode) thrownExceptions.get(0), newThrownException1, null);
			rewrite.replace((ASTNode) thrownExceptions.get(2), newThrownException2, null);
		}
		{ // rename all params & rename second exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			SingleVariableDeclaration newParam3= createNewParam(ast, "m3");
			rewrite.replace((ASTNode) parameters.get(0), newParam1, null);
			rewrite.replace((ASTNode) parameters.get(1), newParam2, null);
			rewrite.replace((ASTNode) parameters.get(2), newParam3, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			ASTNode newThrownException= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E(int p1, int p2, int p3) {}\n");
		buf.append("    public float gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void xii(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(float m, int p2, int p3) throws IllegalArgumentException, ArrayStoreException {}\n");
		buf.append("    public abstract void kee(float m1, float m2, int p3) throws ArrayStoreException, IllegalAccessException, InterruptedException;\n");
		buf.append("    public abstract void lee(float m1, float m2, float m3) throws IllegalArgumentException, ArrayStoreException, SecurityException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}

	public void testMethodTypeParameterAdds_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    E(int p1) {}\n");
		buf.append("    E(int p1, int p2) {}\n");
		buf.append("    public E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    void gee(int p1) {}\n");
		buf.append("    void hee(int p1, int p2) {}\n");
		buf.append("    public void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {

			// add type parameter
			MethodDeclaration methodDecl= methods[i];
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.TYPE_PARAMETERS_PROPERTY);
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName("X"));
			listRewrite.insertFirst(typeParameter, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> E(int p1) {}\n");
		buf.append("    <X> E(int p1, int p2) {}\n");
		buf.append("    public <X> E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> void gee(int p1) {}\n");
		buf.append("    <X> void hee(int p1, int p2) {}\n");
		buf.append("    public <X> void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testMethodTypeParameterRemoves_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> E(int p1) {}\n");
		buf.append("    <X> E(int p1, int p2) {}\n");
		buf.append("    public <X> E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    <X> void gee(int p1) {}\n");
		buf.append("    <X> void hee(int p1, int p2) {}\n");
		buf.append("    public <X> void hee(int p1, byte p2) {}\n");
		buf.append("    public<X>void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration[] methods= type.getMethods();
		for (int i= 0; i < methods.length; i++) {

			// add type parameter
			MethodDeclaration methodDecl= methods[i];
			rewrite.remove((ASTNode) methodDecl.typeParameters().get(0), null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    E(int p1) {}\n");
		buf.append("    E(int p1, int p2) {}\n");
		buf.append("    public E(int p1, byte p2) {}\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    void gee(int p1) {}\n");
		buf.append("    void hee(int p1, int p2) {}\n");
		buf.append("    public void hee(int p1, byte p2) {}\n");
		buf.append("    public void hee(int p1, byte p2) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}



	public void testMethodReturnTypeChanges_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E() {}\n");
		buf.append("    E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ E(int i, int j) {}\n");
		buf.append("    public void gee1() {}\n");
		buf.append("    void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.PUBLIC | Modifier.FINAL), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, INTERNAL_METHOD_RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.FINAL), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, INTERNAL_METHOD_RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);

			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.FINAL), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, INTERNAL_METHOD_RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}

		{ // add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.PUBLIC | Modifier.FINAL), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.FINAL), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);

			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.FINAL), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public final float E() {}\n");
		buf.append("    final float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final /* comment */ float E(int i, int j) {}\n");
		buf.append("    public final gee1() {}\n");
		buf.append("    final gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testMethodReturnTypeChanges2_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public synchronized E() {}\n");
		buf.append("    public E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ E(int i, int j) {}\n");
		buf.append("    public synchronized void gee1() {}\n");
		buf.append("    public void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.PUBLIC), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, INTERNAL_METHOD_RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);
			setModifiers(rewrite, methodDecl, 0);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, INTERNAL_METHOD_RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);

			setModifiers(rewrite, methodDecl, 0);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, INTERNAL_METHOD_RETURN_TYPE_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}

		{ // remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);
			rewrite.set(methodDecl, INTERNAL_METHOD_MODIFIERS_PROPERTY, Integer.valueOf(Modifier.PUBLIC), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);
			setModifiers(rewrite, methodDecl, 0);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);

			setModifiers(rewrite, methodDecl, 0);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E() {}\n");
		buf.append("    float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ float E(int i, int j) {}\n");
		buf.append("    public gee1() {}\n");
		buf.append("    gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}



	public void testMethodReturnTypeChanges_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E() {}\n");
		buf.append("    E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ E(int i, int j) {}\n");
		buf.append("    public void gee1() {}\n");
		buf.append("    void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}

		{ // remove return type, add second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove return type, add (first) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, add second modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);

			ListRewrite modifiers= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			modifiers.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public final float E() {}\n");
		buf.append("    final float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final /* comment */ float E(int i, int j) {}\n");
		buf.append("    public final gee1() {}\n");
		buf.append("    final gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testMethodReturnTypeChanges2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public synchronized E() {}\n");
		buf.append("    public E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ E(int i, int j) {}\n");
		buf.append("    public synchronized void gee1() {}\n");
		buf.append("    public void gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public /* comment */ void gee3() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List list= type.bodyDeclarations();

		{ // insert return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);

		}
		{ // insert return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(1);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);

			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // insert return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(2);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);

			// from constructor to method
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newReturnType, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.FALSE, null);
		}

		{ // remove return type, remove second modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(3);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);

		}
		{ // remove return type, remove (only) modifier
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(4);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		{ // remove return type, remove modifier with comments
			MethodDeclaration methodDecl= (MethodDeclaration) list.get(5);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);

			// from method to constructor
			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, null, null);
			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public float E() {}\n");
		buf.append("    float E(int i) {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    /* comment */ float E(int i, int j) {}\n");
		buf.append("    public gee1() {}\n");
		buf.append("    gee2() {}\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    gee3() {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}




	public void testListRemoves() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete first param
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
		}
		{ // delete second param & remove exception & remove public
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			// change flags
			setModifiers(rewrite, methodDecl, 0);

			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(1), null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
		}
		{ // delete last param
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(2), null);
		}
		{ // delete first and second param & remove first exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "iee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
		}
		{ // delete first and last param & remove second
			MethodDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
		}
		{ // delete second and last param & remove first exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
		}
		{ // delete all params & remove first and last exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);
			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);
			rewrite.remove((ASTNode) thrownExceptions.get(0), null);
			rewrite.remove((ASTNode) thrownExceptions.get(2), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p2, int p3) {}\n");
		buf.append("    void gee(int p1, int p3) {}\n");
		buf.append("    public void hee(int p1, int p2) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p3) throws IllegalAccessException {}\n");
		buf.append("    public void jee(int p2) throws IllegalArgumentException {}\n");
		buf.append("    public abstract void kee(int p1) throws IllegalArgumentException, SecurityException;\n");
		buf.append("    public abstract void lee() throws IllegalAccessException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListRemoves2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void setMyProp(String property1) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);

		{ // delete param, insert new
			MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(0);
			List parameters= methodDecl.parameters();
			rewrite.remove((ASTNode) parameters.get(0), null);

			SingleVariableDeclaration decl= ast.newSingleVariableDeclaration();
			decl.setType(ast.newPrimitiveType(PrimitiveType.INT));
			decl.setName(ast.newSimpleName("property11"));

			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(decl, null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void setMyProp(int property11) {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331111
	public void _testListRemoves3() throws Exception {
		Map options = this.project1.getOptions(true);
		Map newOptions = this.project1.getOptions(true);
		try {
			newOptions.put(
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION,
					JavaCore.INSERT);
			newOptions.put(
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION,
					JavaCore.INSERT);
			newOptions.put(
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION,
					JavaCore.DO_NOT_INSERT);
			this.project1.setOptions(newOptions);
			IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
			StringBuilder buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo( String s ) {}\n");
			buf.append("}\n");
			ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);

			{ // delete param, insert new
				MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(0);
				List parameters= methodDecl.parameters();
				rewrite.remove((ASTNode) parameters.get(0), null);
			}
			String preview= evaluateRewrite(cu, rewrite);

			buf= new StringBuilder();
			buf.append("package test1;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo() {}\n");
			buf.append("}\n");

			assertEqualString(preview, buf.toString());
		} finally {
			this.project1.setOptions(options);
		}
	}

	public void testListInserts() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert before first param & insert an exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);

			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertFirst(newThrownException, null);

		}
		{ // insert before second param & insert before first exception & add synchronized
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.SYNCHRONIZED;
			setModifiers(rewrite, methodDecl, newModifiers);

			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ASTNode secondParam= (ASTNode) parameters.get(1);
			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertBefore(newParam, secondParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertBefore(newThrownException, firstException, null);
		}
		{ // insert after last param & insert after first exception & add synchronized, static
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.SYNCHRONIZED | Modifier.STATIC;
			setModifiers(rewrite, methodDecl, newModifiers);

			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertAfter(newThrownException, firstException, null);

		}
		{ // insert 2 params before first & insert between two exception
			MethodDeclaration methodDecl= findMethodDeclaration(type, "iee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ASTNode firstParam= (ASTNode) parameters.get(0);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");

			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);
			listRewrite.insertBefore(newParam1, firstParam, null);
			listRewrite.insertBefore(newParam2, firstParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);

			ASTNode firstException= (ASTNode) thrownExceptions.get(0);
			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertAfter(newThrownException, firstException, null);
		}
		{ // insert 2 params after first & replace the second exception and insert new after
			MethodDeclaration methodDecl= findMethodDeclaration(type, "jee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

			ASTNode firstParam= (ASTNode) parameters.get(0);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertAfter(newParam2, firstParam, null);
			listRewrite.insertAfter(newParam1, firstParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 2 thrown exceptions", thrownExceptions.size() == 2);

			ASTNode newThrownException1= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException1, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.replace((ASTNode) thrownExceptions.get(1), newThrownException2, null);
		}
		{ // insert 2 params after last & remove the last exception and insert new after
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);
			ASTNode lastParam= (ASTNode) parameters.get(2);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");

			listRewrite.insertAfter(newParam2, lastParam, null);
			listRewrite.insertAfter(newParam1, lastParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);

			ASTNode lastException= (ASTNode) thrownExceptions.get(2);
			rewrite.remove(lastException, null);

			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertBefore(newThrownException, lastException, null);
		}
		{ // insert at first and last position & remove 2nd, add after 2nd, remove 3rd
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertFirst(newParam1, null);
			listRewrite.insertLast(newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);

			ASTNode secondException= (ASTNode) thrownExceptions.get(1);
			ASTNode lastException= (ASTNode) thrownExceptions.get(2);
			rewrite.remove(secondException, null);
			rewrite.remove(lastException, null);

			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertAfter(newThrownException, secondException, null);

		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m, int p1, int p2, int p3) throws InterruptedException {}\n");
		buf.append("    public synchronized void gee(int p1, float m, int p2, int p3) throws InterruptedException, IllegalArgumentException {}\n");
		buf.append("    public static synchronized void hee(int p1, int p2, int p3, float m) throws IllegalArgumentException, InterruptedException {}\n");
		buf.append("    public void iee(float m1, float m2, int p1, int p2, int p3) throws IllegalArgumentException, InterruptedException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, float m1, float m2, int p2, int p3) throws IllegalArgumentException, ArrayStoreException, InterruptedException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3, float m1, float m2) throws IllegalArgumentException, IllegalAccessException, InterruptedException;\n");
		buf.append("    public abstract void lee(float m1, int p1, int p2, int p3, float m2) throws IllegalArgumentException, InterruptedException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListInsert() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert at first and last position & remove 2nd, add after 2nd, remove 3rd
			MethodDeclaration methodDecl= findMethodDeclaration(type, "lee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			listRewrite.insertFirst(newParam1, null);
			listRewrite.insertLast(newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 3 thrown exceptions", thrownExceptions.size() == 3);

			rewrite.remove((ASTNode) thrownExceptions.get(1), null);
			rewrite.remove((ASTNode) thrownExceptions.get(2), null);

			ASTNode newThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException, null);
		}



		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public abstract void lee(float m1, int p1, int p2, int p3, float m2) throws IllegalArgumentException, InterruptedException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListCombinations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete all and insert after & insert 2 exceptions
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);


			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);

			ASTNode newThrownException1= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException1, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);

		}
		{ // delete first 2, replace last and insert after & replace first exception and insert before
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.replace((ASTNode) parameters.get(2), newParam1, null);

			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam2, null);


			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			ASTNode modifiedThrownException= createNewExceptionType(ast, "InterruptedException");
			rewrite.replace((ASTNode) thrownExceptions.get(0), modifiedThrownException, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);

		}
		{ // delete first 2, replace last and insert at first & remove first and insert before
			MethodDeclaration methodDecl= findMethodDeclaration(type, "hee");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.replace((ASTNode) parameters.get(2), newParam1, null);

			SingleVariableDeclaration newParam2= createNewParam(ast, "m2");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam2, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 1 thrown exceptions", thrownExceptions.size() == 1);

			rewrite.remove((ASTNode) thrownExceptions.get(0), null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("    public void gee(float m1, float m2) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("    public void hee(float m2, float m1) throws ArrayStoreException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListCombination() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete all and insert after & insert 2 exceptions
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			List parameters= methodDecl.parameters();
			assertTrue("must be 3 parameters", parameters.size() == 3);

			rewrite.remove((ASTNode) parameters.get(0), null);
			rewrite.remove((ASTNode) parameters.get(1), null);
			rewrite.remove((ASTNode) parameters.get(2), null);

			SingleVariableDeclaration newParam= createNewParam(ast, "m");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam, null);

			List thrownExceptions= getThrownExceptions(methodDecl);
			assertTrue("must be 0 thrown exceptions", thrownExceptions.size() == 0);

			ASTNode newThrownException1= createNewExceptionType(ast, "InterruptedException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException1, null);

			ASTNode newThrownException2= createNewExceptionType(ast, "ArrayStoreException");
			rewrite.getListRewrite(methodDecl, getMethodThrownExceptionsProperty(ast)).insertLast(newThrownException2, null);


		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(float m) throws InterruptedException, ArrayStoreException {}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testListCombination2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void bar() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void foo2() {\n");
		buf.append("       // user comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration[] methods= type.getMethods();
		Arrays.sort(methods, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((MethodDeclaration) o1).getName().getIdentifier().compareTo(((MethodDeclaration) o2).getName().getIdentifier());
			}
		});

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		for (int i= 0; i < methods.length; i++) {
			ASTNode copy= rewrite.createMoveTarget(methods[i]);
			listRewrite.insertLast(copy, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    void bar() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void foo2() {\n");
		buf.append("       // user comment\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}


	public void testMethodBody() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {}\n");
		buf.append("    public void gee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public abstract void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // replace block
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");

			Block body= methodDecl.getBody();
			assertTrue("No body: E", body != null);

			Block newBlock= ast.newBlock();

			rewrite.replace(body, newBlock, null);
		}
		{ // delete block & set abstract
			MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");

			// change flags
			int newModifiers= Modifier.PUBLIC | Modifier.ABSTRACT;
			setModifiers(rewrite, methodDecl, newModifiers);

			Block body= methodDecl.getBody();
			assertTrue("No body: gee", body != null);

			rewrite.remove(body, null);
		}
		{ // insert block & set to private
			MethodDeclaration methodDecl= findMethodDeclaration(type, "kee");

			// change flags
			int newModifiers= Modifier.PRIVATE;
			setModifiers(rewrite, methodDecl, newModifiers);


			Block body= methodDecl.getBody();
			assertTrue("Has body", body == null);

			Block newBlock= ast.newBlock();
			rewrite.set(methodDecl, MethodDeclaration.BODY_PROPERTY, newBlock, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public E(int p1, int p2, int p3) {\n");
		buf.append("    }\n");
		buf.append("    public abstract void gee(int p1, int p2, int p3) throws IllegalArgumentException;\n");
		buf.append("    public void hee(int p1, int p2, int p3) throws IllegalArgumentException {}\n");
		buf.append("    public void iee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    public void jee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException {}\n");
		buf.append("    private void kee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException {\n");
		buf.append("    }\n");
		buf.append("    public abstract void lee(int p1, int p2, int p3) throws IllegalArgumentException, IllegalAccessException, SecurityException;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodDeclarationExtraDimensions_only_2_3_4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    public Object foo2() throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo3()[][] { return null; }\n");
		buf.append("    public Object foo4()[][] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5()[][] { return null; }\n");
		buf.append("    public Object foo6(int i)[][] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i)[][] { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, 1, null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, INTERNAL_METHOD_THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // add extra dim, remove throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, 1, null);

			rewrite.remove((ASTNode) getThrownExceptions(methodDecl).get(0), null);
		}
		{ // remove extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, 1, null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, INTERNAL_METHOD_THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // add extra dim, remove throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, 1, null);

			rewrite.remove((ASTNode) getThrownExceptions(methodDecl).get(0), null);
		}
		{ // add params, add extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo5");

			SingleVariableDeclaration newParam1= createNewParam(ast, "m1");
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(newParam1, null);


			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, 4, null);

			Name newThrownException2= ast.newSimpleName("ArrayStoreException");
			rewrite.getListRewrite(methodDecl, INTERNAL_METHOD_THROWN_EXCEPTIONS_PROPERTY).insertLast(newThrownException2, null);

		}
		{ // remove params, add extra dim, remove throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo6");

			rewrite.remove((ASTNode) methodDecl.parameters().get(0), null);

			rewrite.set(methodDecl, INTERNAL_METHOD_EXTRA_DIMENSIONS_PROPERTY, 4, null);

			rewrite.remove((ASTNode) getThrownExceptions(methodDecl).get(0), null);
		}
		{ // remove block
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo7");
			rewrite.remove(methodDecl.getBody(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo2()[] { return null; }\n");
		buf.append("    public Object foo3()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo4()[] { return null; }\n");
		buf.append("    public Object foo5(float m1)[][][][] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo6()[][][][] { return null; }\n");
		buf.append("    public Object foo7(int i)[][];\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testModifiersAST3_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("    Object foo5() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public Object foo6() { return null; }\n");
		buf.append("    public Object foo7() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    public static Object foo8() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    Object foo9() { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert first and last
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD), null);
		}
		{ // insert 2x first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD), null);
		}
		{ // remove and insert first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // remove and insert last
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // insert first and insert Javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo5");
			Javadoc javadoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(methodDecl, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}
		{ // remove modifier and remove javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo6");
			rewrite.remove(methodDecl.getJavadoc(), null);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove modifier and insert javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo7");

			Javadoc javadoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(methodDecl, MethodDeclaration.JAVADOC_PROPERTY, javadoc, null);

			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove all
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo8");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(1), null);
		}
		{ // insert (first) with javadoc
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo9");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    final public synchronized Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    static final public Object foo2() { return null; }\n");
		buf.append("    final Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final Object foo4() { return null; }\n");
		buf.append("    /**\n");
		buf.append("     * Hello\n");
		buf.append("     */\n");
		buf.append("    final Object foo5() { return null; }\n");
		buf.append("    Object foo6() { return null; }\n");
		buf.append("    /**\n");
		buf.append("     * Hello\n");
		buf.append("     */\n");
		buf.append("    Object foo7() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    Object foo8() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    final Object foo9() { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testModifiersAST3WithAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    @ToBeRemoved\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @ToBeRemoved\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert annotation first before normal
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // insert annotation first before annotation
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // remove annotation before normal
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}
		{ // remove annotation before annotation
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    @Override\n");
		buf.append("    public Object foo1() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Override\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    /** javadoc comment */\n");
		buf.append("    @Deprecated\n");
		buf.append("    public Object foo4() { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testModifiersAST3WithAnnotations2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    Object foo1() { return null; }\n");
		buf.append("    Object foo2() { return null; }\n");
		buf.append("    @Deprecated()Object foo3() { return null; }\n");
		buf.append("    @Deprecated()Object foo4() { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // insert annotation first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Override"));
			listRewrite.insertFirst(annot, null);
		}
		{ // insert modifier first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			Modifier modifier= ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			listRewrite.insertFirst(modifier, null);
		}
		{ // insert modifier last
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			Modifier modifier= ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			listRewrite.insertLast(modifier, null);
		}
		{ // insert modifier first
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.MODIFIERS2_PROPERTY);
			Modifier modifier= ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			listRewrite.insertFirst(modifier, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    @Override\n");
		buf.append("    Object foo1() { return null; }\n");
		buf.append("    public Object foo2() { return null; }\n");
		buf.append("    @Deprecated()\n");
		buf.append("    public Object foo3() { return null; }\n");
		buf.append("    public @Deprecated()Object foo4() { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}



	public void testFieldDeclaration_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    int i1= 1;\n");
		buf.append("    int i2= 1, k2= 2, n2= 3;\n");
		buf.append("    static final int i3= 1, k3= 2, n3= 3;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");

		FieldDeclaration[] fieldDeclarations= type.getFields();
		assertTrue("Number of fieldDeclarations not 3", fieldDeclarations.length == 3);
		{	// add modifier, change type, add fragment
			FieldDeclaration decl= fieldDeclarations[0];

			// add modifier
			int newModifiers= Modifier.FINAL;
			rewrite.set(decl, INTERNAL_FIELD_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			PrimitiveType newType= ast.newPrimitiveType(PrimitiveType.BOOLEAN);
			rewrite.replace(decl.getType(), newType, null);

			VariableDeclarationFragment frag=	ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("k1"));
			frag.setInitializer(null);

			rewrite.getListRewrite(decl, FieldDeclaration.FRAGMENTS_PROPERTY).insertLast(frag, null);

		}
		{	// add modifiers, remove first two fragments, replace last
			FieldDeclaration decl= fieldDeclarations[1];

			// add modifier
			int newModifiers= Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT;
			rewrite.set(decl, INTERNAL_FIELD_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

			List fragments= decl.fragments();
			assertTrue("Number of fragments not 3", fragments.size() == 3);

			rewrite.remove((ASTNode) fragments.get(0), null);
			rewrite.remove((ASTNode) fragments.get(1), null);

			VariableDeclarationFragment frag=	ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("k2"));
			frag.setInitializer(null);

			rewrite.replace((ASTNode) fragments.get(2), frag, null);
		}
		{	// remove modifiers
			FieldDeclaration decl= fieldDeclarations[2];

			// change modifier
			int newModifiers= 0;
			rewrite.set(decl, INTERNAL_FIELD_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    final boolean i1= 1, k1;\n");
		buf.append("    static final transient int k2;\n");
		buf.append("    int i3= 1, k3= 2, n3= 3;\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testInitializer_only_2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    {\n");
		buf.append("        foo();\n");
		buf.append("    }\n");
		buf.append("    static {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");

		List declarations= type.bodyDeclarations();
		assertTrue("Number of fieldDeclarations not 2", declarations.size() == 2);
		{	// change modifier, replace body
			Initializer initializer= (Initializer) declarations.get(0);

			// add modifier
			int newModifiers= Modifier.STATIC;
			rewrite.set(initializer, INTERNAL_INITIALIZER_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);


			Block block= ast.newBlock();
			block.statements().add(ast.newReturnStatement());

			rewrite.replace(initializer.getBody(), block, null);
		}
		{	// change modifier
			Initializer initializer= (Initializer) declarations.get(1);

			int newModifiers= 0;
			rewrite.set(initializer, INTERNAL_INITIALIZER_MODIFIERS_PROPERTY, Integer.valueOf(newModifiers), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    static {\n");
		buf.append("        return;\n");
		buf.append("    }\n");
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}


	public void testMethodDeclarationParamShuffel() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(int i, boolean b) { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add extra dim, add throws
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			List params= methodDecl.parameters();

			SingleVariableDeclaration first= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration second= (SingleVariableDeclaration) params.get(1);
			rewrite.replace(first.getName(), ast.newSimpleName("x"), null);
			rewrite.replace(second.getName(), ast.newSimpleName("y"), null);

			ASTNode copy1= rewrite.createCopyTarget(first);
			ASTNode copy2= rewrite.createCopyTarget(second);

			rewrite.replace(first, copy2, null);
			rewrite.replace(second, copy1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(boolean y, int x) { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testMethodDeclarationParamShuffel1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(int i, boolean b) { return null; }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			List params= methodDecl.parameters();

			SingleVariableDeclaration first= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration second= (SingleVariableDeclaration) params.get(1);

			ASTNode copy2= rewrite.createCopyTarget(second);

			rewrite.replace(first, copy2, null);
			rewrite.remove(second, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1(boolean b) { return null; }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodDeclaration_bug24916() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private int DD()[]{\n");
		buf.append("    };\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "DD");

			rewrite.set(methodDecl, MethodDeclaration.CONSTRUCTOR_PROPERTY, Boolean.TRUE, null);
			setExtraDimensions(rewrite, methodDecl, 0);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private DD(){\n");
		buf.append("    };\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ASTNode node= rewrite.createCopyTarget(methodDecl);

			ASTNode firstDecl= (ASTNode) type.bodyDeclarations().get(0);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAfter(node, firstDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testBUG_38447() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("\n"); // missing closing bracket
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodComments4() throws Exception {


		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			ASTNode copy= rewrite.createCopyTarget(methodDecl);

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(copy, null);

			MethodDeclaration newMethodDecl= createNewMethod(astRoot.getAST(), "xoo", false);
			rewrite.replace(methodDecl, newMethodDecl, null);

			//MethodDeclaration methodDecl2= findMethodDeclaration(type, "foo1");
			//rewrite.markAsReplaced(methodDecl2, copy);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    // one line comment\n");
		buf.append("\n");
		buf.append("    private void xoo(String str) {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo(){\n");
		buf.append("    } // another\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	/** @deprecated using deprecated code */
	public void testInsertFieldAfter_only_2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    private int fCount1;\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
			frag.setName(ast.newSimpleName("fColor"));
			FieldDeclaration newField= ast.newFieldDeclaration(frag);
			newField.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
			newField.setModifiers(Modifier.PRIVATE);

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertAt(newField, 1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");

		buf.append("public class DD {\n");
		buf.append("    private int fCount1;\n");
		buf.append("    private char fColor;\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testVarArgs_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object... args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, Object[] args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			SingleVariableDeclaration param= (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			SingleVariableDeclaration param= (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.set(param, SingleVariableDeclaration.TYPE_PROPERTY, ast.newPrimitiveType(PrimitiveType.INT), null);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.TRUE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, int... args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ELLIPSIS, JavaCore.INSERT);

		preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class DD {\n");
		buf.append("    private void foo1(String format, Object args){\n");
		buf.append("    }\n");
		buf.append("    private void foo2(String format, int ... args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testAnnotationTypeMember_since_4() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public @interface DD {\n");
		buf.append("    public String value1();\n");
		buf.append("    String value2() default 1;\n");
		buf.append("    String value3() default 2;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AnnotationTypeDeclaration type= (AnnotationTypeDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			AnnotationTypeMemberDeclaration methodDecl= (AnnotationTypeMemberDeclaration) type.bodyDeclarations().get(0);
			rewrite.remove((ASTNode) methodDecl.modifiers().get(0), null);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.TYPE_PROPERTY, ast.newPrimitiveType(PrimitiveType.BOOLEAN), null);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.NAME_PROPERTY, ast.newSimpleName("test"), null);

			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY, ast.newNumberLiteral("1"), null);
		}
		{
			AnnotationTypeMemberDeclaration methodDecl= (AnnotationTypeMemberDeclaration) type.bodyDeclarations().get(1);
			rewrite.getListRewrite(methodDecl, AnnotationTypeMemberDeclaration.MODIFIERS2_PROPERTY).insertFirst(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD), null);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY, ast.newNumberLiteral("2"), null);
		}
		{
			AnnotationTypeMemberDeclaration methodDecl= (AnnotationTypeMemberDeclaration) type.bodyDeclarations().get(2);
			rewrite.set(methodDecl, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY, null, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public @interface DD {\n");
		buf.append("    boolean test() default 1;\n");
		buf.append("    public String value2() default 2;\n");
		buf.append("    String value3();\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumConstantDeclaration1_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    E1(1), E2, E3(), E4(1, 2)\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration type= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(0);
			rewrite.set(enumConst, EnumConstantDeclaration.NAME_PROPERTY, ast.newSimpleName("X"), null);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.remove((ASTNode) enumConst.arguments().get(0), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(1);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(ast.newNumberLiteral("1"), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(2);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(ast.newNumberLiteral("2"), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(3);
			ListRewrite listRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			listRewrite.remove((ASTNode) enumConst.arguments().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    X, E2(1), E3(2), E4(2)\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumConstantDeclaration2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    E1Add(1),\n");
		buf.append("    E2Add,\n");
		buf.append("    E3Add(1),\n");
		buf.append("    E4Add(1),\n");
		buf.append("    E5Add(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E1Remove(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E2Remove {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E3Remove(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E4Remove(1) {\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration type= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(0);
			assertNull(enumConst.getAnonymousClassDeclaration());

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);

			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(1);
			assertNull(enumConst.getAnonymousClassDeclaration());

			ListRewrite argsRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			argsRewrite.insertFirst(ast.newNumberLiteral("1"), null);

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);

			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);

		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(2);
			assertNull(enumConst.getAnonymousClassDeclaration());

			rewrite.remove((ASTNode) enumConst.arguments().get(0), null);

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);

			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(3);
			assertNull(enumConst.getAnonymousClassDeclaration());

			AnonymousClassDeclaration classDecl= ast.newAnonymousClassDeclaration();
			rewrite.set(enumConst, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY, classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(4);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			ListRewrite bodyRewrite= rewrite.getListRewrite(classDecl, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
			bodyRewrite.insertFirst(createNewMethod(ast, "test", false), null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(5);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			rewrite.remove(classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(6);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			ListRewrite argsRewrite= rewrite.getListRewrite(enumConst, EnumConstantDeclaration.ARGUMENTS_PROPERTY);
			argsRewrite.insertFirst(ast.newNumberLiteral("1"), null);

			rewrite.remove(classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(7);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			rewrite.remove((ASTNode) enumConst.arguments().get(0), null);
			rewrite.remove(classDecl, null);
		}
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(8);

			AnonymousClassDeclaration classDecl= enumConst.getAnonymousClassDeclaration();
			assertNotNull(classDecl);

			rewrite.remove((ASTNode) classDecl.bodyDeclarations().get(0), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    E1Add(1) {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E2Add(1) {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E3Add {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E4Add(1) {\n");
		buf.append("    },\n");
		buf.append("    E5Add(1) {\n");
		buf.append("        private void test(String str) {\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("        public void foo() {\n");
		buf.append("        }\n");
		buf.append("    },\n");
		buf.append("    E1Remove(1),\n");
		buf.append("    E2Remove(1),\n");
		buf.append("    E3Remove,\n");
		buf.append("    E4Remove(1) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testEnumConstantDeclaration_bug114119_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    RED, BROWN(), GREEN(){};\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		EnumDeclaration type= (EnumDeclaration) findAbstractTypeDeclaration(astRoot, "DD");
		{
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) type.enumConstants().get(2);
			assertNotNull(enumConst.getAnonymousClassDeclaration());

			rewrite.remove(enumConst.getAnonymousClassDeclaration(), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public enum DD {\n");
		buf.append("    RED, BROWN(), GREEN();\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testVarArgsAnnotations_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("@interface Marker {\n");
		buf.append("}\n");
		buf.append("public class DD {\n");
		buf.append("    public void foo1(String format, Object @Marker... args){\n");
		buf.append("    }\n");
		buf.append("    public void foo2(Object... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo3(Object @Marker ... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo4(Object @Marker... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo5(Object... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo6(Object args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo7(Object @Marker... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo8(Object @Marker... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo9(@B @C int @A... a) {\n");
		buf.append("    }\n");
		buf.append("    public void foo10(Object args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo11(Object @Marker... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo12(Object... args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("DD.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu, false);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "DD");

		{
			// Remove annotation from first method args - boundary condition -
			// - only one annotation should be present.
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");
			SingleVariableDeclaration param = (SingleVariableDeclaration) methodDecl.parameters().get(1);
			rewrite.remove((ASTNode)param.varargsAnnotations().get(0), null);

			// Add one annotation to the second method - boundary condition
			// - no annotation should be present
			methodDecl= findMethodDeclaration(type, "foo2");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));
			ListRewrite listRewrite= rewrite.getListRewrite(param, SingleVariableDeclaration.VARARGS_ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);

			// Remove the varargs property - annotation(s) should disappear
			methodDecl= findMethodDeclaration(type, "foo3");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);

			// Remove the varargs property - annotation(s) should disappear
			// - differs from the above due to the absence of a blank before ...
			methodDecl= findMethodDeclaration(type, "foo4");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);

			// Remove the varargs property - Existing functionality unchanged without annotations
			methodDecl= findMethodDeclaration(type, "foo5");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);

			// Add the varargs property  and annotation
			methodDecl= findMethodDeclaration(type, "foo6");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.TRUE, null);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));
			listRewrite= rewrite.getListRewrite(param, SingleVariableDeclaration.VARARGS_ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);

			// Replace annotation
			methodDecl= findMethodDeclaration(type, "foo7");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));
			rewrite.replace((ASTNode)param.varargsAnnotations().get(0), markerAnnotation, null);

			// Reset and Set Varargs - output should not change.
			methodDecl= findMethodDeclaration(type, "foo8");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.TRUE, null);

			// Add multiple (two) annotations, remove an existing annotation
			methodDecl= findMethodDeclaration(type, "foo9");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			NormalAnnotation normalAnnotation = ast.newNormalAnnotation();
			normalAnnotation.setTypeName(ast.newSimpleName("X"));
			listRewrite= rewrite.getListRewrite(param, SingleVariableDeclaration.VARARGS_ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(normalAnnotation, null);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Y"));
			listRewrite.insertFirst(markerAnnotation, null);
			rewrite.remove((ASTNode)param.varargsAnnotations().get(0), null);

			// Add the varargs property
			methodDecl= findMethodDeclaration(type, "foo10");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.TRUE, null);

			// Remove the annotations and varargs property as well.
			methodDecl= findMethodDeclaration(type, "foo11");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.remove((ASTNode)param.varargsAnnotations().get(0), null);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);

			// Add an annotation but remove the varargs property - should not add the annotation.
			methodDecl= findMethodDeclaration(type, "foo12");
			param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			rewrite.set(param, SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));
			listRewrite= rewrite.getListRewrite(param, SingleVariableDeclaration.VARARGS_ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("@interface Marker {\n");
		buf.append("}\n");
		buf.append("public class DD {\n");
		buf.append("    public void foo1(String format, Object... args){\n");
		buf.append("    }\n");
		buf.append("    public void foo2(Object @X... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo3(Object args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo4(Object args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo5(Object args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo6(Object @X... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo7(Object @X... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo8(Object @Marker... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo9(@B @C int @Y @X()... a) {\n");
		buf.append("    }\n");
		buf.append("    public void foo10(Object... args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo11(Object args) {\n");
		buf.append("    }\n");
		buf.append("    public void foo12(Object args) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testMethodDeclChangesBug77538() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("// comment\n");
		buf.append("public class A {\n");
		buf.append("	public int foo() {\n");
		buf.append("		return 0;\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		// Get method declaration and its body
		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "A");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block body = methodDecl.getBody();

	   // start record of the modifications
	   astRoot.recordModifications();

	   // Modify method body
		Block newBody = ast.newBlock();
		methodDecl.setBody(newBody);
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("lock"));
		fragment.setInitializer(ast.newQualifiedName(ast.newSimpleName("OS"), ast.newSimpleName("lock")));
		VariableDeclarationExpression variableDeclarationExpression = ast.newVariableDeclarationExpression(fragment);
		variableDeclarationExpression.setType(ast.newSimpleType(ast.newSimpleName("Lock")));
		ExpressionStatement expressionStatement = ast.newExpressionStatement(variableDeclarationExpression);
		newBody.statements().add(expressionStatement);
		TryStatement tryStatement = ast.newTryStatement();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName("lock"));
		methodInvocation.setExpression(ast.newSimpleName("lock"));
		ExpressionStatement expressionStatement2 = ast.newExpressionStatement(methodInvocation);
		body.statements().add(0, expressionStatement2);
		tryStatement.setBody(body);
		Block finallyBlock = ast.newBlock();
		tryStatement.setFinally(finallyBlock);
		methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName("unLock"));
		methodInvocation.setExpression(ast.newSimpleName("lock"));
		expressionStatement2 = ast.newExpressionStatement(methodInvocation);
		finallyBlock.statements().add(expressionStatement2);
		newBody.statements().add(tryStatement);

		// Verify that body extended length does not become negative!
		assertFalse("Invalid extended length for "+body, astRoot.getExtendedLength(body)<0);
	}

	public void testAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("@An\n");
		buf.append("@An()\n");
		buf.append("@An(val=1, val=2)\n");
		buf.append("@An(val=1, val=2)\n");
		buf.append("@An(1)\n");
		buf.append("class E {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List modifiers= type.modifiers();
		assertEquals(5, modifiers.size());
		{
			MarkerAnnotation an= (MarkerAnnotation) modifiers.get(0);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, MarkerAnnotation.TYPE_NAME_PROPERTY, newName, null);
		}
		{
			NormalAnnotation an= (NormalAnnotation) modifiers.get(1);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, NormalAnnotation.TYPE_NAME_PROPERTY, newName, null);

			ListRewrite listRewrite= rewrite.getListRewrite(an, NormalAnnotation.VALUES_PROPERTY);

			MemberValuePair newPair= ast.newMemberValuePair();
			newPair.setName(ast.newSimpleName("foo"));
			newPair.setValue(ast.newNumberLiteral("0"));

			listRewrite.insertFirst(newPair, null);
		}
		{
			NormalAnnotation an= (NormalAnnotation) modifiers.get(2);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, NormalAnnotation.TYPE_NAME_PROPERTY, newName, null);

			List values= an.values();

			ListRewrite listRewrite= rewrite.getListRewrite(an, NormalAnnotation.VALUES_PROPERTY);
			listRewrite.remove((ASTNode) values.get(0), null);

			MemberValuePair p= (MemberValuePair) values.get(1);
			SimpleName newMember= ast.newSimpleName("Y");
			SimpleName newValue= ast.newSimpleName("Z");
			rewrite.set(p, MemberValuePair.NAME_PROPERTY, newMember, null);
			rewrite.set(p, MemberValuePair.VALUE_PROPERTY, newValue, null);

			MemberValuePair newPair= ast.newMemberValuePair();
			newPair.setName(ast.newSimpleName("foo"));
			newPair.setValue(ast.newNumberLiteral("0"));

			listRewrite.insertLast(newPair, null);
		}
		{
			NormalAnnotation an= (NormalAnnotation) modifiers.get(3);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, NormalAnnotation.TYPE_NAME_PROPERTY, newName, null);

			List values= an.values();

			ListRewrite listRewrite= rewrite.getListRewrite(an, NormalAnnotation.VALUES_PROPERTY);
			listRewrite.remove((ASTNode) values.get(0), null);
			listRewrite.remove((ASTNode) values.get(1), null);
		}
		{
			SingleMemberAnnotation an= (SingleMemberAnnotation) modifiers.get(4);
			SimpleName newName= ast.newSimpleName("X");
			rewrite.set(an, SingleMemberAnnotation.TYPE_NAME_PROPERTY, newName, null);
			rewrite.set(an, SingleMemberAnnotation.VALUE_PROPERTY, ast.newBooleanLiteral(true), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("@X\n");
		buf.append("@X(foo = 0)\n");
		buf.append("@X(Y=Z, foo = 0)\n");
		buf.append("@X()\n");
		buf.append("@X(true)\n");
		buf.append("class E {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testParameterAnnotations_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    public void foo(@A int a, @B1 @B2 int b, int c, @D int d) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		MethodDeclaration methodDecl= (MethodDeclaration) findTypeDeclaration(astRoot, "E").bodyDeclarations().get(0);
		List params= methodDecl.parameters();
		assertEquals(4, params.size());
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(0);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);
		}
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(1);

			rewrite.remove((ASTNode) decl.modifiers().get(0), null);
		}
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(2);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));

			ListRewrite listRewrite= rewrite.getListRewrite(decl, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(markerAnnotation, null);
		}
		{
			SingleVariableDeclaration decl= (SingleVariableDeclaration) params.get(3);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));

			rewrite.replace((ASTNode) decl.modifiers().get(0), markerAnnotation, null);
		}
		{
			SingleVariableDeclaration decl= ast.newSingleVariableDeclaration();

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("X"));
			decl.modifiers().add(markerAnnotation);

			Type type= ast.newPrimitiveType(PrimitiveType.INT);
			decl.setType(type);

			decl.setName(ast.newSimpleName("e"));

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);
			listRewrite.insertLast(decl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    public void foo(@X @A int a, @B2 int b, @X int c, @X int d, @X int e) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, JavaCore.INSERT);
		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ANNOTATIONS_ON_PARAMETER,
				DefaultCodeFormatterConstants.createAlignmentValue(true, DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE));

		preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class E {\n");
		buf.append("    public void foo(@X\n    @A int a, @B2 int b, @X\n    int c, @X int d, @X\n    int e) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	public void testExtraDimwithAnnotations_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo2()[] { return null; }\n");
		buf.append("    public Object foo3() @Annot1 [] @Annot2 [] { return null; }\n");
		buf.append("    public Object foo4()@Annot1 [] @Annot2 [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5() @Annot1 []   @Annot2 [] { return null; }\n");
		buf.append("    public Object foo6(int i)  @Annot1 [] @Annot2 [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i) @Annot1 []  @Annot2 [] { return null; }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo1");

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
			Dimension dim= ast.newDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 1, null);

			dim= ast.newDimension();
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 2, null);

			ASTNode exception = (ASTNode) methodDecl.thrownExceptionTypes().get(0);
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY).remove(exception, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo2");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY);

			Dimension dim= ast.newDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			listRewrite.insertAt(dim, 1, null);

			Type exception= ast.newSimpleType(ast.newSimpleName("ArrayStoreException"));
			rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY).insertFirst(exception, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo3");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY);

			Dimension dim= ast.newDimension();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			dim.annotations().add(markerAnnotation);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			dim.annotations().add(markerAnnotation);
			listRewrite.insertAt(dim, 1, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo4");
			Dimension dim= (Dimension) methodDecl.extraDimensions().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			listRewrite.insertAt(markerAnnotation, 0, null);

			dim= (Dimension) methodDecl.extraDimensions().get(1);
			listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot1"));
			listRewrite.insertAt(markerAnnotation, 1, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo5");
			Dimension dim= (Dimension) methodDecl.extraDimensions().get(0);
			Annotation annot= (Annotation) dim.annotations().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.remove(annot, null);

			dim= (Dimension) methodDecl.extraDimensions().get(1);
			listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.insertAt(annot, 1, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo6");
			Dimension dim= (Dimension) methodDecl.extraDimensions().get(0);
			Annotation annot= (Annotation) dim.annotations().get(0);
			ListRewrite listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.remove(annot, null);

			dim= (Dimension) methodDecl.extraDimensions().get(1);
			annot= (Annotation) dim.annotations().get(0);
			listRewrite= rewrite.getListRewrite(dim, Dimension.ANNOTATIONS_PROPERTY);
			listRewrite.remove(annot, null);
		}
		{
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo7");
			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
			Dimension dim= (Dimension) methodDecl.extraDimensions().get(0);
			listRewrite.remove(dim, null);
			dim= (Dimension) methodDecl.extraDimensions().get(1);
			listRewrite.remove(dim, null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public Object foo1()[]@Annot1 []@Annot2 [] { return null; }\n");
		buf.append("    public Object foo2()[]@Annot1 [] throws ArrayStoreException { return null; }\n");
		buf.append("    public Object foo3() @Annot1 [] @Annot1 @Annot2 []@Annot2 [] { return null; }\n");
		buf.append("    public Object foo4()@Annot2 @Annot1 [] @Annot2 @Annot1 [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo5()  []   @Annot2 @Annot1 [] { return null; }\n");
		buf.append("    public Object foo6(int i)   []  [] throws IllegalArgumentException { return null; }\n");
		buf.append("    public Object foo7(int i) { return null; }\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		buf.append("@java.lang.annotation.Target(value= {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot2 {}\n");
		assertEqualString(preview, buf.toString());
	}
	public void testReceiver1_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("    public void foo(@A @B @C X this, int i, int j) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot = createAST(cu);
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		AST ast = astRoot.getAST();
		MethodDeclaration method = (MethodDeclaration) findTypeDeclaration(astRoot, "X").bodyDeclarations().get(0);
		Type receiverType = method.getReceiverType();
		assertEquals("Invalid receiver type", ASTNode.SIMPLE_TYPE, receiverType.getNodeType());
		MarkerAnnotation annot = ast.newMarkerAnnotation();
		annot.setTypeName(ast.newSimpleName("Marker"));
		ListRewrite listRewrite = rewrite.getListRewrite(receiverType, SimpleType.ANNOTATIONS_PROPERTY);
		listRewrite.insertFirst(annot, null);

		String preview = evaluateRewrite(cu, rewrite);
		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("    public void foo(@Marker @A @B @C X this, int i, int j) {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	public void testReceiver2_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("	class Y {\n");
		buf.append("    	public Y(@A X this, int i, @B boolean b, @A int j) {\n");
		buf.append("    	}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot = createAST(cu);
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		AST ast = astRoot.getAST();
		TypeDeclaration type = (TypeDeclaration) findTypeDeclaration(astRoot, "X").bodyDeclarations().get(0);
		MethodDeclaration method = (MethodDeclaration) type.bodyDeclarations().get(0);
		List params = method.parameters();

		SingleVariableDeclaration first = (SingleVariableDeclaration) params.get(0);
		SingleVariableDeclaration second = (SingleVariableDeclaration) params.get(1);
		SingleVariableDeclaration third = (SingleVariableDeclaration) params.get(2);
		rewrite.replace(first.getName(), ast.newSimpleName("i"), null);
		rewrite.replace(second.getName(), ast.newSimpleName("b"), null);

		ASTNode copy1 = rewrite.createCopyTarget(first);
		ASTNode copy2 = rewrite.createCopyTarget(second);

		rewrite.replace(first, copy2, null);
		rewrite.replace(second, copy1, null);
		rewrite.remove(third, null);

		String preview = evaluateRewrite(cu, rewrite);
		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("	class Y {\n");
		buf.append("    	public Y(@A X this, @B boolean b, int i) {\n");
		buf.append("    	}\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	public void testReceiver3_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("    public void foo(X this) {}\n");
		buf.append("    public void foo() {}\n");
		buf.append("    public void foo(X this,/*comment*/ int i) {}\n");
		buf.append("    public void foo(int i, int j) {}\n");
		buf.append("    public void foo(X this) {}\n");
		buf.append("    public void foo(X this, float f1, float f2) {}\n");
		buf.append("    public void foo(X this, int i) {}\n");
		buf.append("    public void foo(X this, float f) {}\n");
		buf.append("    public void foo1(X this, float f) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot = createAST(cu);
		AST ast = astRoot.getAST();
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type = findTypeDeclaration(astRoot, "X");
		MethodDeclaration method1 = (MethodDeclaration) type.bodyDeclarations().get(0);
		MethodDeclaration method2 = (MethodDeclaration) type.bodyDeclarations().get(1);
		MethodDeclaration method3 = (MethodDeclaration) type.bodyDeclarations().get(2);
		MethodDeclaration method4 = (MethodDeclaration) type.bodyDeclarations().get(3);
		MethodDeclaration method5 = (MethodDeclaration) type.bodyDeclarations().get(4);
		MethodDeclaration method6 = (MethodDeclaration) type.bodyDeclarations().get(5);
		MethodDeclaration method7 = (MethodDeclaration) type.bodyDeclarations().get(6);
		MethodDeclaration method8 = (MethodDeclaration) type.bodyDeclarations().get(7);
		MethodDeclaration method9 = (MethodDeclaration) type.bodyDeclarations().get(8);

		SimpleType receiver1 = (SimpleType) method1.getReceiverType();
		SimpleType receiver3 = (SimpleType) method3.getReceiverType();
		SimpleType receiver5 = (SimpleType) method5.getReceiverType();
		SimpleType receiver6 = (SimpleType) method6.getReceiverType();
		SimpleType receiver8 = (SimpleType) method8.getReceiverType();
		SimpleType receiver9 = (SimpleType) method9.getReceiverType();

		SimpleType receiverCopy = (SimpleType) rewrite.createCopyTarget(receiver1);
		rewrite.set(method2, MethodDeclaration.RECEIVER_TYPE_PROPERTY, receiverCopy, null);
		rewrite.remove(receiver1, null);

		receiverCopy = (SimpleType) rewrite.createCopyTarget(receiver3);
		rewrite.set(method4, MethodDeclaration.RECEIVER_TYPE_PROPERTY, receiverCopy, null);
		rewrite.remove(receiver3, null);

		ListRewrite listRewrite = rewrite.getListRewrite(method3, MethodDeclaration.MODIFIERS2_PROPERTY);
		listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);

		receiverCopy = ast.newSimpleType(ast.newSimpleName("XY"));
		rewrite.replace(receiver5, receiverCopy, null);

		receiverCopy = ast.newSimpleType(ast.newSimpleName("XY"));
		rewrite.replace(receiver6, receiverCopy, null);
		SingleVariableDeclaration paramCopy = (SingleVariableDeclaration) rewrite.createCopyTarget((SingleVariableDeclaration) method6.parameters().get(0));
		rewrite.remove((SingleVariableDeclaration) method6.parameters().get(0), null);

		listRewrite = rewrite.getListRewrite(method7, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.insertLast(paramCopy, null);

		rewrite.remove(receiver8, null);
		rewrite.remove((SingleVariableDeclaration) method8.parameters().get(0), null);

		rewrite.remove(receiver9, null);
		rewrite.remove((SingleVariableDeclaration) method9.parameters().get(0), null);

		String preview = evaluateRewrite(cu, rewrite);
		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("    public void foo() {}\n");
		buf.append("    public void foo(X this) {}\n");
		buf.append("    public final void foo(/*comment*/ int i) {}\n");
		buf.append("    public void foo(X this, int i, int j) {}\n");
		buf.append("    public void foo(XY this) {}\n");
		buf.append("    public void foo(XY this, float f2) {}\n");
		buf.append("    public void foo(X this, int i, float f1) {}\n");
		buf.append("    public void foo() {}\n");
		buf.append("    public void foo1() {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	public void _testReceiver4_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class XYZ {\n");
		buf.append("	class Y {\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i, @C int j) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B float e) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B float e, @C float f) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i, @C float f) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B float f, @C int i) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B boolean b1) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B boolean b2, @C int i) {}\n");
		buf.append("    	public Y(@B boolean b, @C float f) {}\n");
		buf.append("    	public Y(@B boolean b, @C boolean c) {}\n");
		buf.append("    	public Y(@B boolean b, String str) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i, @C int j, @D int k) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this) {}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("XYZ.java", buf.toString(), false, null);
		CompilationUnit astRoot = createAST(cu);
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		AST ast = astRoot.getAST();
		TypeDeclaration type = (TypeDeclaration) findTypeDeclaration(astRoot, "XYZ").bodyDeclarations().get(0);
		MethodDeclaration method1 = (MethodDeclaration) type.bodyDeclarations().get(0); // Remove the receiver type and qualifying name
		MethodDeclaration method2 = (MethodDeclaration) type.bodyDeclarations().get(1); // Remove the receiver but not the qualifier
		MethodDeclaration method3 = (MethodDeclaration) type.bodyDeclarations().get(2); // Remove the qualifier only
		MethodDeclaration method4 = (MethodDeclaration) type.bodyDeclarations().get(3); // Remove the qualifier and receiver annotation
		MethodDeclaration method5 = (MethodDeclaration) type.bodyDeclarations().get(4); // Remove the receiver type and all parameters
		MethodDeclaration method6 = (MethodDeclaration) type.bodyDeclarations().get(5); // Remove the receiver type and add a param
		MethodDeclaration method7 = (MethodDeclaration) type.bodyDeclarations().get(6); // Remove the qualifier and remove a param
		MethodDeclaration method8 = (MethodDeclaration) type.bodyDeclarations().get(7); // Remove the qualifier and replace a param
		MethodDeclaration method9 = (MethodDeclaration) type.bodyDeclarations().get(8); // Add a receiver type and qualifier with annotation
		MethodDeclaration method10 = (MethodDeclaration) type.bodyDeclarations().get(9); // Add a receiver type and qualifier with annotations and add one parameter
		MethodDeclaration method11 = (MethodDeclaration) type.bodyDeclarations().get(10); // Add a receiver type with qualifier & annotations and remove all parameters
		MethodDeclaration method12 = (MethodDeclaration) type.bodyDeclarations().get(11); // Keep the receiver type and qualifier, but alter parameters
		MethodDeclaration method13 = (MethodDeclaration) type.bodyDeclarations().get(12); // Keep the receiver type and qualifier, but alter parameters

		rewrite.set(method1, MethodDeclaration.RECEIVER_TYPE_PROPERTY, null, null);
		rewrite.set(method1, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, null, null);
		rewrite.set(method2, MethodDeclaration.RECEIVER_TYPE_PROPERTY, null, null);
		rewrite.set(method3, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, null, null);

		rewrite.set(method4, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, null, null);
		ListRewrite listRewrite = rewrite.getListRewrite(method4.getReceiverType(), SimpleType.ANNOTATIONS_PROPERTY);
		listRewrite.remove((ASTNode) ((AnnotatableType) method4.getReceiverType()).annotations().get(0), null);

		rewrite.set(method5, MethodDeclaration.RECEIVER_TYPE_PROPERTY, null, null);
		List params = method5.parameters();
		SingleVariableDeclaration first = (SingleVariableDeclaration) params.get(0);
		listRewrite = rewrite.getListRewrite(method5, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.remove(first, null);
		first = (SingleVariableDeclaration) params.get(1);
		listRewrite.remove(first, null);

		rewrite.set(method6, MethodDeclaration.RECEIVER_TYPE_PROPERTY, null, null);
		listRewrite = rewrite.getListRewrite(method6, MethodDeclaration.PARAMETERS_PROPERTY);
		SingleVariableDeclaration paramCopy = ast.newSingleVariableDeclaration();
		SimpleType typeCopy = ast.newSimpleType(ast.newSimpleName("Object"));
		MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
		markerAnnotation.setTypeName(ast.newSimpleName("A"));
		typeCopy.annotations().add(markerAnnotation);
		paramCopy.setType(typeCopy);
		paramCopy.setName(ast.newSimpleName("obj"));
		listRewrite.insertFirst(paramCopy, null);

		rewrite.set(method7, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, null, null);
		params = method7.parameters();
		first = (SingleVariableDeclaration) params.get(0);
		listRewrite = rewrite.getListRewrite(method7, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.remove(first, null);
		params = method8.parameters();
		rewrite.set(method8, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, null, null);
		listRewrite = rewrite.getListRewrite(method8, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.remove((SingleVariableDeclaration) params.get(0), null);
		listRewrite.remove((SingleVariableDeclaration) params.get(1), null);
		listRewrite.insertLast(paramCopy, null);

		SimpleType receiverType = ast.newSimpleType(ast.newSimpleName("XYZ"));
		SimpleName qual = ast.newSimpleName("XYZ");
		markerAnnotation= ast.newMarkerAnnotation();
		markerAnnotation.setTypeName(ast.newSimpleName("A"));
		receiverType.annotations().add(markerAnnotation);

		rewrite.set(method9, MethodDeclaration.RECEIVER_TYPE_PROPERTY, receiverType, null);
		rewrite.set(method9, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, qual, null);

		rewrite.set(method10, MethodDeclaration.RECEIVER_TYPE_PROPERTY, receiverType, null);
		rewrite.set(method10, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, qual, null);
		listRewrite = rewrite.getListRewrite(method10, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.insertFirst(paramCopy, null);

		rewrite.set(method11, MethodDeclaration.RECEIVER_TYPE_PROPERTY, receiverType, null);
		rewrite.set(method11, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, qual, null);
		listRewrite = rewrite.getListRewrite(method11, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.remove((ASTNode) method11.parameters().get(0), null);
		listRewrite.remove((ASTNode) method11.parameters().get(1), null);

		listRewrite = rewrite.getListRewrite(method12, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.remove((ASTNode) method12.parameters().get(1), null);

		listRewrite = rewrite.getListRewrite(method13, MethodDeclaration.PARAMETERS_PROPERTY);
		listRewrite.insertFirst(paramCopy, null);

		String preview = evaluateRewrite(cu, rewrite);
		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class XYZ {\n");
		buf.append("	class Y {\n");
		buf.append("    	public Y(@B int i) {}\n");
		buf.append("    	public Y(@B int i, @C int j) {}\n");
		buf.append("    	public Y(@A XYZ this, @B float e) {}\n");
		buf.append("    	public Y(XYZ this, @B float e, @C float f) {}\n");
		buf.append("    	public Y() {}\n");
		buf.append("    	public Y(@A Object obj, @B float f, @C int i) {}\n");
		buf.append("    	public Y(@A XYZ this) {}\n");
		buf.append("    	public Y(@A XYZ this, @A Object obj) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B boolean b, @C float f) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @A Object obj, @B boolean b, @C boolean c) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i, @D int k) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @A Object obj) {}\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	public void testReceiver5_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class XYZ {\n");
		buf.append("	class Y {\n");
		buf.append("    	public Y(@A Y Y.this, @B int i) {}\n");
		buf.append("    	public Y(@A XYZ this, @B int i, @C int j) {}\n");
		buf.append("    	public Y(@A XYZ Y.this, @B float e) {}\n");
		buf.append("    	public Y(@A XYZ Y.this, @B float e, @C float f) {}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("XYZ.java", buf.toString(), false, null);
		CompilationUnit astRoot = createAST(cu);
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		AST ast = astRoot.getAST();
		TypeDeclaration type = (TypeDeclaration) findTypeDeclaration(astRoot, "XYZ").bodyDeclarations().get(0);
		MethodDeclaration method1 = (MethodDeclaration) type.bodyDeclarations().get(0); // Change receiver type's child/children
		MethodDeclaration method2 = (MethodDeclaration) type.bodyDeclarations().get(1); // Insert receiver qualifier
		MethodDeclaration method3 = (MethodDeclaration) type.bodyDeclarations().get(2); // Replace receiver qualifier
		MethodDeclaration method4 = (MethodDeclaration) type.bodyDeclarations().get(3); // Change receiver qualifier's children

		Name newName = ast.newSimpleName("XYZ");
		rewrite.replace(((SimpleType) method1.getReceiverType()).getName(), newName, null);
		rewrite.replace(method1.getReceiverQualifier(), newName, null);
		rewrite.set(method2, MethodDeclaration.RECEIVER_QUALIFIER_PROPERTY, newName, null);
		rewrite.replace(method3.getReceiverQualifier(), newName, null);
		SimpleName qualifier = method4.getReceiverQualifier();
		rewrite.set(qualifier, SimpleName.IDENTIFIER_PROPERTY, "XYZ", null);

		String preview = evaluateRewrite(cu, rewrite);
		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class XYZ {\n");
		buf.append("	class Y {\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B int i, @C int j) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B float e) {}\n");
		buf.append("    	public Y(@A XYZ XYZ.this, @B float e, @C float f) {}\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403985
	public void testBug403985_since_8() throws Exception {
		String contents =
			"public interface X {\n" +
			"	static void foo(){}\n" +
			"	public default void foo(int i){}\n" +
			"	public default int foo2(int i) { return 0;}\n" +
			"	public void foo3(int i);\n" +
			"	public default int foo4(int i) { return 0;}\n" +
			"}\n";
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", contents, false, null);
		CompilationUnit astRoot= createAST(cu);
		AST ast = astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Incorrect no of methods", 5, methods.length);
		MethodDeclaration method = methods[0];
		{	// Change default method to static and vice versa
			ListRewrite listRewrite = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
			ASTNode newMod = ast.newModifier(ModifierKeyword.DEFAULT_KEYWORD);
			listRewrite.replace((ASTNode) method.modifiers().get(0), newMod, null);

			method = methods[1];
			listRewrite = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
			newMod = ast.newModifier(ModifierKeyword.STATIC_KEYWORD);
			listRewrite.replace((ASTNode) method.modifiers().get(1), newMod, null);
		}
		{	// Remove default and the body
			method = methods[2];
			ListRewrite listRewrite = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.remove((ASTNode) method.modifiers().get(1), null);
			rewrite.set(method, MethodDeclaration.BODY_PROPERTY, null, null);
		}
		{	// Add a default and body
			method = methods[3];
			ListRewrite listRewrite = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
			ASTNode newMod = ast.newModifier(ModifierKeyword.DEFAULT_KEYWORD);
			listRewrite.insertAt(newMod, 1, null);
			Block newBlock = ast.newBlock();
			rewrite.set(method, MethodDeclaration.BODY_PROPERTY, newBlock, null);
		}
		{	// Alter parameters for a default method
			method = methods[4];
			ListRewrite listRewrite = rewrite.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);
			listRewrite.remove((ASTNode) method.parameters().get(0), null);
			listRewrite = rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.remove((ASTNode) method.modifiers().get(0), null);
		}
		String preview = evaluateRewrite(cu, rewrite);
		contents =
				"public interface X {\n" +
				"	default void foo(){}\n" +
				"	public static void foo(int i){}\n" +
				"	public int foo2(int i);\n" +
				"	public default void foo3(int i) {\n    }\n" +
				"	default int foo4() { return 0;}\n" +
				"}\n";
		assertEqualString(preview, contents);
	}

	public void testReceiverParam_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot = createAST(cu);
		AST ast = astRoot.getAST();
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type = findTypeDeclaration(astRoot, "E");

		MethodDeclaration newMethodDeclaration = ast.newMethodDeclaration();
		SimpleName methodName = ast.newSimpleName("bar");
		SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("E"));
		MarkerAnnotation annotationC = ast.newMarkerAnnotation();
		annotationC.setTypeName(ast.newSimpleName("C"));
		simpleType.annotations().add(annotationC);
		newMethodDeclaration.setName(methodName);
		newMethodDeclaration.setReceiverType(simpleType);

		MethodDeclaration[] methods = type.getMethods();
		MethodDeclaration methodDeclaration = methods[0];
		methodDeclaration.setReceiverType(ast.newSimpleType(ast.newSimpleName("E")));

		ListRewrite listRewrite = rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertLast(newMethodDeclaration, null);

		String preview = evaluateRewrite(cu, rewrite);

		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    void bar(@C E this);\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}

	public void testReceiverParam_InnerClass_since_8() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(
				"test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    class Inner{\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack1.createCompilationUnit("E.java",
				buf.toString(), false, null);

		CompilationUnit astRoot = createAST(cu);
		AST ast = astRoot.getAST();
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type = findTypeDeclaration(astRoot, "E");
		TypeDeclaration inner = (TypeDeclaration) type.bodyDeclarations().get(1);

		MethodDeclaration newMethodDeclaration = ast.newMethodDeclaration();
		SimpleName methodName = ast.newSimpleName("Inner");
		SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("E"));
		MarkerAnnotation annotationC = ast.newMarkerAnnotation();
		annotationC.setTypeName(ast.newSimpleName("C"));
		simpleType.annotations().add(annotationC);
		newMethodDeclaration.setName(methodName);
		newMethodDeclaration.setConstructor(true);
		newMethodDeclaration.setReceiverType(simpleType);
		newMethodDeclaration.setReceiverQualifier(ast.newSimpleName("E"));
		newMethodDeclaration.setBody(ast.newBlock());

		ListRewrite listRewrite = rewrite.getListRewrite(inner, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertLast(newMethodDeclaration, null);

		String preview = evaluateRewrite(cu, rewrite);

		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    class Inner{\n");
		buf.append("\n");
		buf.append("        Inner(@C E E.this) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	public void testBug427622a_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test(){}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add an annotated simpletype as throws annotation
			MethodDeclaration methodDecl = findMethodDeclaration(type, "test");
			SimpleType newThrownException = (SimpleType) createNewExceptionType(ast, "MyException");
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker"));
			ListRewrite listRewrite = rewrite.getListRewrite(newThrownException, SimpleType.ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(annot, null);
			listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
			listRewrite.insertFirst(newThrownException, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test() throws @Marker MyException{}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");

		assertEqualString(preview, buf.toString());

		// still no new line if new line after annotation on parameter is enabled:
		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, JavaCore.INSERT);

		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, buf.toString());

		// do insert new line if new line after type annotation is enabled:
		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, JavaCore.DO_NOT_INSERT);
		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_TYPE_ANNOTATION, JavaCore.INSERT);

		preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test() throws @Marker\n");
		buf.append("    MyException{}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");

		assertEqualString(preview, buf.toString());
	}

	public void testBug427622b_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test() throws MyException{}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add an annotation to the simpletype
			MethodDeclaration methodDecl = findMethodDeclaration(type, "test");
			SimpleType newThrownException = (SimpleType) methodDecl.thrownExceptionTypes().get(0);
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker"));
			ListRewrite listRewrite = rewrite.getListRewrite(newThrownException, SimpleType.ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(annot, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test() throws @Marker MyException{}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");

		assertEqualString(preview, buf.toString());
	}
	public void testBug427622c_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test(){}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // add an annotated simpletype as throws annotation
			MethodDeclaration methodDecl = findMethodDeclaration(type, "test");
			ListRewrite listRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
			SimpleType newThrownException = (SimpleType) createNewExceptionType(ast, "MyException");
			listRewrite.insertFirst(newThrownException, null);
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker"));
			listRewrite = rewrite.getListRewrite(newThrownException, SimpleType.ANNOTATIONS_PROPERTY);
			listRewrite.insertFirst(annot, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.*;\n");
		buf.append("public abstract class E {\n");
		buf.append("    public void test() throws @Marker MyException{}\n");
		buf.append("    class MyException extends Throwable {\n");
		buf.append("     private static final long serialVersionUID=-3045365361549263819L;");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("@Target (Element.TYPE_USE);\n");
		buf.append("@interface Marker {}\n");

		assertEqualString(preview, buf.toString());
	}
}
