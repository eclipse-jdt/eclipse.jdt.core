/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JrtPackageFragmentRoot;
import org.eclipse.jdt.internal.core.SourceModule;

import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModularClassFile;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

@SuppressWarnings({"rawtypes"})
public class ASTConverter9Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
	private static final String jcl9lib = "CONVERTER_JCL9_LIB";
	

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST9());
	}
	/**
	 * @deprecated
	 */
	static int getAST9() {
		return AST.JLS9;
	}

	public ASTConverter9Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug515875_002"};
	}
	public static Test suite() {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion.length() > 3) {
			javaVersion = javaVersion.substring(0, 3);
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
		}
		return buildModelTestSuite(ASTConverter9Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testBug497719_0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter9" , "src", "testBug497719_001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a compilation unit", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		TryStatement tryStatement = (TryStatement) methodDeclaration.getBody().statements().get(1);
		List list = tryStatement.resources();
		VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) list.get(0);
		checkSourceRange(variableDeclarationExpression, "final Y y = new Y()", source);
		SimpleName simpleName = (SimpleName) list.get(1);
		checkSourceRange(simpleName, "y1", source);
		variableDeclarationExpression = (VariableDeclarationExpression) list.get(2);
		checkSourceRange(variableDeclarationExpression, "final Y y2 = new Y()", source);
		
	}
	
	public void testBug497719_0002() throws JavaModelException {
		String contents =
				"import java.io.IOException;\n" +
				"\n" +
				"class Z {\n" +
				"	 final Y yz = new Y();\n" +
				"}\n" +
				"public class X extends Z {\n" +
				"	final  Y y2 = new Y();\n" +
				"	\n" +
				"	 Y bar() {\n" +
				"		 return new Y();\n" +
				"	 }\n" +
				"	public void foo() {\n" +
				"		Y y3 = new Y();\n" +
				"		int a[];\n" +
				"		try (y3; y3;super.yz;super.yz;this.y2;Y y4 = new Y())  {  \n" +
				"			System.out.println(\"In Try\");\n" +
				"		} catch (IOException e) {			  \n" +
				"		} \n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo();\n" +
				"	}\n" +
				"}\n" +
				"class Y implements AutoCloseable {\n" +
				"	@Override\n" +
				"	public void close() throws IOException {\n" +
				"		System.out.println(\"Closed\");\n" +
				"	}  \n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter9/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 1, 2);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			TryStatement tryStatement = (TryStatement)methodDeclaration.getBody().statements().get(2);
			List<Expression> resources = tryStatement.resources();
			Expression expr = resources.get(0);
			SimpleName simpleName = (SimpleName) expr;
			checkSourceRange(simpleName, "y3", contents);
			expr = resources.get(1);
			simpleName = (SimpleName) expr;
			checkSourceRange(expr, "y3", contents);
			expr = resources.get(2);
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) expr;
			checkSourceRange(superFieldAccess, "super.yz", contents);
			expr = resources.get(3);
			superFieldAccess = (SuperFieldAccess) expr;
			checkSourceRange(superFieldAccess, "super.yz", contents);
			expr = resources.get(4);
			FieldAccess fieldAccess = (FieldAccess) expr;
			checkSourceRange(fieldAccess, "this.y2", contents);
			expr = resources.get(5);
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) expr;
			checkSourceRange(variableDeclarationExpression, "Y y4 = new Y()", contents);
	}
	public void testBug496123_0001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		String content =  "module first {"
				+ "  requires second;\n"
				+ "  exports pack11 to third, fourth;\n"
				+ "  uses NewType;\n"
				+ "  provides pack22.I22 with pack11.packinternal.Z11;\n"
				+ "}";
		this.workingCopies[0] = getWorkingCopy(
				"/Converter9/src/module-info.java", content);

		CompilationUnit unit = (CompilationUnit) runConversion(this.ast.apiLevel(), this.workingCopies[0], false/*no bindings*/);
		ModuleDeclaration moduleDecl = unit.getModule();

		assertFalse(moduleDecl.isOpen());
		checkSourceRange(moduleDecl, content, content);
		List<ModuleDirective> stmts = moduleDecl.moduleStatements();
		assertTrue(stmts.size() > 0);

		RequiresDirective req = (RequiresDirective) stmts.get(0);
		checkSourceRange(req, "requires second;", content);

		ExportsDirective exp = (ExportsDirective) stmts.get(1);
		checkSourceRange(exp, "exports pack11 to third, fourth;", content);
		checkSourceRange(exp.getName(), "pack11", content);
		List<Name> modules = exp.modules();
		assertTrue(modules.size() == 2);
		checkSourceRange(modules.get(0), "third", content);
		checkSourceRange(modules.get(1), "fourth", content);

		UsesDirective u = (UsesDirective) stmts.get(2);
		checkSourceRange(u, "uses NewType;", content);
		Name name = u.getName();
		checkSourceRange(name, "NewType", content);

		ProvidesDirective p = (ProvidesDirective) stmts.get(3);
		checkSourceRange(p, "provides pack22.I22 with pack11.packinternal.Z11;", content);
		name = p.getName();
		checkSourceRange(name, "pack22.I22", content);
		List<Name> impls = p.implementations();
		assertTrue(impls.size() > 0);
		name = impls.get(0);
		checkSourceRange(name, "pack11.packinternal.Z11", content);
	}

	public void testBug512023_0001() throws Exception {
		try {
			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String content = 
				"module first {\n" +
				"    requires second.third;\n" +
				"    exports pack1.X11 to org.eclipse.jdt;\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	content);
			createFolder("/ConverterTests9/src/pack1");
			createFile("/ConverterTests9/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 {}\n");
			this.workingCopy = getWorkingCopy("/ConverterTests9/src/module-info.java", false);
			ASTNode node = buildAST(content, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			ModuleDeclaration moduleDecl = unit.getModule();
			assertFalse(moduleDecl.isOpen());
			checkSourceRange(moduleDecl, content, content);
			List<ModuleDirective> stmts = moduleDecl.moduleStatements();
			assertTrue(stmts.size() > 0);

			QualifiedName qName;
			RequiresDirective req = (RequiresDirective) stmts.get(0);
			qName = (QualifiedName) req.getName();
			checkSourceRange(qName, "second.third", content);
			checkSourceRange(qName.getName(), "third", content);
			checkSourceRange(qName.getQualifier(), "second", content);

			ExportsDirective exp = (ExportsDirective) stmts.get(1);
			checkSourceRange(exp, "exports pack1.X11 to org.eclipse.jdt;", content);
			qName = (QualifiedName) exp.getName();
			checkSourceRange(qName, "pack1.X11", content);
			checkSourceRange(qName.getName(), "X11", content);
			checkSourceRange(qName.getQualifier(), "pack1", content);

			List<Name> modules = exp.modules();
			qName = (QualifiedName) modules.get(0);
			checkSourceRange(qName, "org.eclipse.jdt", content);
			checkSourceRange(qName.getName(), "jdt", content);
			checkSourceRange(qName.getQualifier(), "org.eclipse", content);
		} finally {
			deleteProject("ConverterTests9");
		}
	}

	public void testBug514417() throws CoreException {
		if (!isJRE9) return;
		try {
			createJava9Project("Bug514417", new String[]{"src"});
			createFolder("/Bug514417/src/pack1");
			String content =  "package pack1;\n" +
					"import java.lang.String;\n" +
					"public class X { \n" +
					"	java.lang.String str = null;\n" +
					"}\n";
			createFile("/Bug514417/src/pack1/X.java", content);
			ICompilationUnit sourceUnit = getCompilationUnit("Bug514417" , "src", "pack1", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit = runConversion(this.ast.apiLevel(), sourceUnit, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit.getNodeType());
			List imps = ((CompilationUnit) unit).imports();
			assertEquals("import missing", 1, imps.size());
			ImportDeclaration impo = (ImportDeclaration) imps.get(0);
			IBinding bind = impo.resolveBinding();
			assertNotNull("binding null", bind);
			IJavaElement element = bind.getJavaElement();
			assertNotNull(element);
			assertEquals("Incorrect element type", IJavaElement.TYPE, element.getElementType());
			IType type = (IType) element;
			assertEquals("Incorrect name", "java.lang.String", type.getFullyQualifiedName());
			element = element.getParent();
			assertNotNull(element);
			assertEquals("Incorrect element type", IJavaElement.CLASS_FILE, element.getElementType());
			element = element.getParent();
			assertNotNull(element);
			assertEquals("Incorrect element type", IJavaElement.PACKAGE_FRAGMENT, element.getElementType());
			element = element.getParent();
			assertNotNull(element);
			assertEquals("Incorrect element type", IJavaElement.PACKAGE_FRAGMENT_ROOT, element.getElementType());
			assertTrue("incorrect root type", (element instanceof JrtPackageFragmentRoot));
			JrtPackageFragmentRoot root = (JrtPackageFragmentRoot) element;
			assertEquals("incorrect module name", "java.base", root.getElementName());
		} finally {
			deleteProject("Bug514417");
		}
	}
	public void testBug516785_0001_since_9() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		String content =  "open module first {"
				+ "  requires one;\n"
				+ "  requires static two;\n"
				+ "  requires transitive three;\n"
				+ "  requires static transitive four;\n"
				+ "  requires transitive static five;\n"
				+ "}";
		this.workingCopies[0] = getWorkingCopy(
				"/Converter9/src/module-info.java", content);
		
		CompilationUnit unit = (CompilationUnit) runConversion(this.ast.apiLevel(), this.workingCopies[0], false/*no bindings*/);
		ModuleDeclaration moduleDecl = unit.getModule();
		
		assertTrue(moduleDecl.isOpen());
		checkSourceRange(moduleDecl, content, content);
		List<ModuleDirective> stmts = moduleDecl.moduleStatements();
		assertTrue(stmts.size() > 0);
		
		int count = 0;
		RequiresDirective req = (RequiresDirective) stmts.get(count++);
		checkSourceRange(req, "requires one;", content);

		req = (RequiresDirective) stmts.get(count++);
		checkSourceRange(req, "requires static two;", content);
		checkSourceRange((ASTNode) req.modifiers().get(0), "static", content);

		req = (RequiresDirective) stmts.get(count++);
		checkSourceRange(req, "requires transitive three;", content);
		checkSourceRange((ASTNode) req.modifiers().get(0), "transitive", content);

		req = (RequiresDirective) stmts.get(count++);
		checkSourceRange(req, "requires static transitive four;", content);
		checkSourceRange((ASTNode) req.modifiers().get(0), "static", content);
		checkSourceRange((ASTNode) req.modifiers().get(1), "transitive", content);

		req = (RequiresDirective) stmts.get(count++);
		checkSourceRange(req, "requires transitive static five;", content);
		checkSourceRange((ASTNode) req.modifiers().get(0), "transitive", content);
		checkSourceRange((ASTNode) req.modifiers().get(1), "static", content);
	}

	public void testBug515875_001() throws Exception {
		try {
			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String content = 
				"module first {\n" +
				"    requires transitive static second.third;\n" +
				"    exports pack1.X11 to org.eclipse.jdt;\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	content);
			createFolder("/ConverterTests9/src/pack1");
			createFile("/ConverterTests9/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 {}\n");
			this.workingCopy = getWorkingCopy("/ConverterTests9/src/module-info.java", true);
			ASTNode node = buildAST(content, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			ModuleDeclaration moduleDecl = unit.getModule();
			checkSourceRange(moduleDecl, content, content);

			IModuleBinding moduleBinding = moduleDecl.resolveBinding();
			assertTrue("Module Binding null", moduleBinding != null);
			String name = moduleBinding.getName();
			assertTrue("Module Name null", name != null);
			assertTrue("Wrong Module Name", name.equals("first"));

			IJavaElement element = moduleBinding.getJavaElement();
			assertNotNull("Module Java Element Null", element);
			assertTrue(element instanceof SourceModule);
			SourceModule sModule = (SourceModule) element;
			assertTrue("Source module name incorrect", sModule.getElementName().equals("first"));

			String key = moduleBinding.getKey();
			assertTrue("Unique Key incorrecct", key.equals("\"first"));

		} finally {
			deleteProject("ConverterTests9");
		}
	}

	public void testBug515875_002() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent = 
				"module first {\n" +
				"    requires second;\n" +
				"	 uses pack22.I22;\n" + 
				"    provides pack22.I22 with pack1.X11;\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
			createFolder("/ConverterTests9/src/pack1");
			createFile("/ConverterTests9/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 implements pack22.I22{}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile = 
					"module second {\n" +
					"    exports pack22 to first;\n" +
					"}";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));

			// workaround: I need a way to navigate from a source module to a binary module containing "uses" and "provides":
			IJavaProject project3 = createJavaProject("third", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project3.open(null);
			addClasspathEntry(project3, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String thirdFile = 
					"module third {\n" +
					"    requires first;\n" +
					"}";
			createFile("/third/src/module-info.java",	thirdFile);
			addClasspathEntry(project3, JavaCore.newProjectEntry(project1.getPath()));
			//

			project1.close(); // sync
			project2.close();
			project3.close();
			project2.open(null);
			project1.open(null);
			project3.open(null);

			ICompilationUnit sourceUnit1 = getCompilationUnit("ConverterTests9" , "src", "", "module-info.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit1 = runConversion(this.ast.apiLevel(), sourceUnit1, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit1.getNodeType());
			ModuleDeclaration moduleDecl1 = ((CompilationUnit) unit1).getModule();
			checkSourceRange(moduleDecl1, fileContent, fileContent);

			IModuleBinding moduleBinding = moduleDecl1.resolveBinding();
			Name modName1 = moduleDecl1.getName();
			IBinding binding = modName1.resolveBinding();
			assertTrue("binding not a module binding", binding instanceof IModuleBinding);
			moduleBinding = (IModuleBinding) binding;

			assertModuleFirstDetails(moduleBinding);
			
			// indirectly fetch the binary version of "first" via "third":
			ICompilationUnit sourceUnit3 = getCompilationUnit("third" , "src", "", "module-info.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit3 = runConversion(this.ast.apiLevel(), sourceUnit3, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit3.getNodeType());
			ModuleDeclaration moduleDecl3 = ((CompilationUnit) unit3).getModule();
			IModuleBinding firstModAsBinary = moduleDecl3.resolveBinding().getRequiredModules()[1]; // skip java.base

			assertModuleFirstDetails(firstModAsBinary);
		
		} finally {
			deleteProject("ConverterTests9");
			deleteProject("second");
			deleteProject("third");
		}
	}

	private void assertModuleFirstDetails(IModuleBinding moduleBinding) {
		assertTrue("Module Binding null", moduleBinding != null);
		String name = moduleBinding.getName();
		assertTrue("Module Name null", name != null);
		assertTrue("Wrong Module Name", name.equals("first"));
		
		assertTrue("Module Binding null", moduleBinding != null);
		name = moduleBinding.getName();
		assertTrue("Module Name null", name != null);
		assertTrue("Wrong Module Name", name.equals("first"));

		IModuleBinding[] reqs = moduleBinding.getRequiredModules();
		assertTrue("Null requires", reqs != null);
		assertTrue("incorrect number of requires modules", reqs.length == 2);
		assertTrue("incorrect name for requires modules", reqs[0].getName().equals("java.base"));
		assertTrue("incorrect name for requires modules", reqs[1].getName().equals("second"));

		IPackageBinding[] secPacks = reqs[1].getExportedPackages();
		assertTrue("Packages Exported in second module null", secPacks != null);
		assertTrue("Incorrect number of exported packages in second module", secPacks.length == 1);
		IPackageBinding pack22 = secPacks[0];
		assertTrue("Incorrect Package", pack22.getName().equals("pack22"));

		ITypeBinding[] uses = moduleBinding.getUses();
		assertTrue("uses null", uses != null);
		assertTrue("Incorrect number of uses", uses.length == 1);
		assertTrue("Incorrect uses", uses[0].getQualifiedName().equals("pack22.I22"));

		ITypeBinding[] services = moduleBinding.getServices();
		assertTrue("services null", services != null);
		assertTrue("Incorrect number of services", services.length == 1);
		for (ITypeBinding s : services) {
			assertTrue("Incorrect service", s.getQualifiedName().equals("pack22.I22"));
			ITypeBinding[] implementations = moduleBinding.getImplementations(s);
			assertTrue("implementations null", implementations != null);
			assertTrue("Incorrect number of implementations", implementations.length == 1);
			assertTrue("Incorrect implementation", implementations[0].getQualifiedName().equals("pack1.X11"));
		}
	}

	public void testBug515875_003() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent = 
				"module first {\n" +
				"    requires second;\n" +
				"    provides pack22.I22 with pack1.X11;\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
			createFolder("/ConverterTests9/src/pack1");
			createFile("/ConverterTests9/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 implements pack22.I22{}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile = 
					"module second {\n" +
					"    exports pack22 to first;\n" +
					"}";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit sourceUnit1 = getCompilationUnit("ConverterTests9" , "src", "", "module-info.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit1 = runConversion(this.ast.apiLevel(), sourceUnit1, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit1.getNodeType());
			ModuleDeclaration moduleDecl1 = ((CompilationUnit) unit1).getModule();
			checkSourceRange(moduleDecl1, fileContent, fileContent);

			RequiresDirective req = (RequiresDirective) moduleDecl1.moduleStatements().get(0);
			Name reqModule = req.getName();
			IModuleBinding moduleBinding = (IModuleBinding) reqModule.resolveBinding();
			assertTrue("Module Binding null", moduleBinding != null);
			String name = moduleBinding.getName();
			assertTrue("Module Name null", name != null);
			assertTrue("Wrong Module Name", name.equals("second"));
		}
		finally {
			deleteProject("ConverterTests9");
			deleteProject("second");
		}
	}

	public void testBug515875_004() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent = 
				"open module first {\n" +
				"    requires second;\n" +
				"    provides pack22.I22 with pack1.X11;\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
			createFolder("/ConverterTests9/src/pack1");
			createFile("/ConverterTests9/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 implements pack22.I22{}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile = 
					"open module second {\n" +
					"    exports pack22 to first;\n" +
					"}";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit sourceUnit1 = getCompilationUnit("ConverterTests9" , "src", "", "module-info.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit1 = runConversion(this.ast.apiLevel(), sourceUnit1, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit1.getNodeType());
			ModuleDeclaration moduleDecl1 = ((CompilationUnit) unit1).getModule();
			checkSourceRange(moduleDecl1, fileContent, fileContent);
			
			Name name = moduleDecl1.getName();
			IModuleBinding moduleBinding = (IModuleBinding) name.resolveBinding();
			assertTrue("Module Binding null", moduleBinding != null);
			assertTrue("Module not open", moduleBinding.isOpen());

			RequiresDirective req = (RequiresDirective) moduleDecl1.moduleStatements().get(0);
			name = req.getName();
			moduleBinding = (IModuleBinding) name.resolveBinding();
			assertTrue("Module Binding null", moduleBinding != null);
			String moduleName = moduleBinding.getName();
			assertTrue("Module Name null", moduleName != null);
			assertTrue("Wrong Module Name", moduleName.equals("second"));
			assertTrue("Module not open", moduleBinding.isOpen());
		}
		finally {
			deleteProject("ConverterTests9");
			deleteProject("second");
		}
	}
	public void testBug515875_005() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String fileContent =
				"module first {\n" +
				"    requires second;\n" +
				"    exports pack1 to test;\n" +
				"    opens pack1 to test;\n" +
				"    provides pack22.I22 with pack1.X11, pack1.X12;\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
			createFolder("/ConverterTests9/src/pack1");
			createFile("/ConverterTests9/src/pack1/X11.java",
					"package pack1;\n" +
					"public class X11 implements pack22.I22{}\n");
			createFile("/ConverterTests9/src/pack1/X12.java",
					"package pack1;\n" +
					"public class X12 implements pack22.I22{}\n");

			IJavaProject project2 = createJavaProject("second", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project2.open(null);
			addClasspathEntry(project2, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String secondFile =
					"module second {\n" +
					"    exports pack22 to first;\n" +
					"}";
			createFile("/second/src/module-info.java",	secondFile);
			createFolder("/second/src/pack22");
			createFile("/second/src/pack22/I22.java",
					"package pack22;\n" +
					"public interface I22 {}\n");

			addClasspathEntry(project1, JavaCore.newProjectEntry(project2.getPath()));
			project1.close(); // sync
			project2.close();
			project2.open(null);
			project1.open(null);

			ICompilationUnit sourceUnit1 = getCompilationUnit("ConverterTests9" , "src", "", "module-info.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit1 = runConversion(this.ast.apiLevel(), sourceUnit1, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit1.getNodeType());
			ModuleDeclaration moduleDecl1 = ((CompilationUnit) unit1).getModule();
			checkSourceRange(moduleDecl1, fileContent, fileContent);

			IModuleBinding moduleBinding = moduleDecl1.resolveBinding();
			assertTrue("Module Binding null", moduleBinding != null);
			String name = moduleBinding.getName();
			assertTrue("Module Name null", name != null);
			assertTrue("Wrong Module Name", name.equals("first"));

			IPackageBinding[] exports = moduleBinding.getExportedPackages();
			assertTrue("Incorrect number of exports", exports.length == 1);
			IPackageBinding e = exports[0];
			assertTrue("Incorrect Export", e.getKey().equals("pack1"));
			String[] targets = moduleBinding.getExportedTo(e);
			assertTrue("Incorrect number of targets", targets.length == 1);
			assertTrue("Incorrect Target", targets[0].equals("test"));

			IPackageBinding[] opens = moduleBinding.getOpenedPackages();
			assertTrue("Incorrect number of opens", opens.length == 1);
			e = opens[0];
			assertTrue("Incorrect Opens", e.getKey().equals("pack1"));
			targets = moduleBinding.getOpenedTo(e);
			assertTrue("Incorrect number of targets", targets.length == 1);
			assertTrue("Incorrect Target", targets[0].equals("test"));

			ITypeBinding[] services = moduleBinding.getServices();
			assertTrue("services null", services != null);
			assertTrue("Incorrect number of services", services.length == 1);
			for (ITypeBinding s : services) {
				assertTrue("Incorrect service", s.getQualifiedName().equals("pack22.I22"));
				ITypeBinding[] implementations = moduleBinding.getImplementations(s);
				assertTrue("implementations null", implementations != null);
				assertTrue("Incorrect number of implementations", implementations.length == 2);
				assertTrue("Incorrect implementation", implementations[0].getQualifiedName().equals("pack1.X11"));
				assertTrue("Incorrect implementation", implementations[1].getQualifiedName().equals("pack1.X12"));
			}
		}
		finally {
			deleteProject("ConverterTests9");
			deleteProject("second");
		}
	}

	public void testBug518843_001() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		String content =  "module first {"
				+ "  exports pack11.module to third, fourth;\n"
				+ "}";
		this.workingCopies[0] = getWorkingCopy("/Converter9/src/module-info.java", content);

		CompilationUnit unit = (CompilationUnit) runConversion(this.ast.apiLevel(), this.workingCopies[0], false/*no bindings*/);
		ModuleDeclaration moduleDecl = unit.getModule();
		checkSourceRange(moduleDecl, content, content);
	}
	public void testBug519310_001() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		String content =  "package p;\n"
				+ "  public interface I1 {\n"
				+ "  private void foo() {}\n"
				+ "}";
		this.workingCopies[0] = getWorkingCopy("/Converter9/src/p/I1.java", content);

		CompilationUnit unit = (CompilationUnit) runConversion(this.ast.apiLevel(), this.workingCopies[0], false/*no bindings*/);
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) unit.types().get(0);
		MethodDeclaration method = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(0);
		assertTrue("Method Malformed", (method.getFlags() & ASTNode.MALFORMED) == 0);
	}

	public void testResolveSourceModule1() throws Exception {
		IJavaProject project1 = null;
		try {
			project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			String fileContent = 
				"open module first.module {\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
	
			IModuleDescription firstModule = project1.findModule("first.module", null);
			IJavaElement[] elements = new IJavaElement[] {
					firstModule,
				};
			ASTParser parser = ASTParser.newParser(this.ast.apiLevel());
			parser.setProject(project1);
			IBinding[] bindings = parser.createBindings(elements, null);
			assertBindingsEqual(
				"\"first.module",
				bindings);
			String key = bindings[0].getKey();
			IJavaElement element = project1.findElement(key, this.wcOwner);
			assertEquals("should be the same module", firstModule, element);
		} finally {
			if (project1 != null)
				deleteProject(project1);
		}
	}

	public void testResolveBinaryModule1() throws Exception {
		IJavaProject project1 = null;
		IJavaProject project2 = null;
		try {
			project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			String fileContent = 
				"module first.module { }";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
			
			project1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
	
			project2 = createJavaProject("second", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			addClasspathEntry(project2, JavaCore.newLibraryEntry(new Path("/ConverterTests9/bin"), null, null, null, 
					new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") }, false));		
			project2.open(null);
	
			IModuleDescription firstModule = null;
			for (IPackageFragmentRoot root : project2.getPackageFragmentRoots()) {
				IModuleDescription module = root.getModuleDescription();
				if (module != null && module.getElementName().equals("first.module")) {
					assertTrue("should be in modular class file", module.getParent() instanceof IModularClassFile);
					firstModule = module;
					break;
				}
			}
			assertNotNull("finding first.module", firstModule);
			assertEquals("same as through find", firstModule, project2.findModule("first.module", this.wcOwner));
			IJavaElement[] elements = new IJavaElement[] {
					firstModule,
				};
			ASTParser parser = ASTParser.newParser(this.ast.apiLevel());
			parser.setProject(project2);
			IBinding[] bindings = parser.createBindings(elements, null);
			assertBindingsEqual(
				"\"first.module",
				bindings);
			String key = bindings[0].getKey();
			IJavaElement element = project2.findElement(key, this.wcOwner);
			assertEquals("should be the same module", firstModule, element);
		} finally {
			if (project1 != null)
				deleteProject(project1);
			if (project2 != null)
				deleteProject(project2);
		}
	}
	public void testBug519884_001() throws Exception {
		try {

			IJavaProject project1 = createJavaProject("ConverterTests9", new String[] {"src"}, new String[] {jcl9lib}, "bin", "9");
			project1.open(null);
			String fileContent =
				"module first {\n" +
				"}";
			createFile("/ConverterTests9/src/module-info.java",	fileContent);
			createFolder("/ConverterTests9/src/pack");
			createFile("/ConverterTests9/src/pack/X.java",
					"package pack;\n" +
					"import java.MyObject;\n" +
					"public class X{}");

			project1.close(); // sync
			project1.open(null);

			ICompilationUnit sourceUnit1 = getCompilationUnit("ConverterTests9" , "src", "pack", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode unit1 = runConversion(this.ast.apiLevel(), sourceUnit1, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, unit1.getNodeType());
			CompilationUnit cu = (CompilationUnit) unit1;
			ImportDeclaration importDeclaration = (ImportDeclaration) cu.imports().get(0);
			QualifiedName qName = (QualifiedName) importDeclaration.getName();
			Name name = qName.getQualifier();
			IBinding binding = name.resolveBinding();
			if (binding != null) {
				assertTrue("Not PackageBinding", binding instanceof IPackageBinding);
				IPackageBinding packageBinding = (IPackageBinding) binding;
				IJavaElement element = packageBinding.getJavaElement();
				assertTrue("element null", element != null);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			assertFalse("Failed", true);
		} finally {
			deleteProject("ConverterTests9");
		}
	}

// Add new tests here
}
