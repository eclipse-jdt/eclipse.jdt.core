/*******************************************************************************
 * Copyright (c) 2024, 2025 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

public class ModuleImportASTConverterTest extends ConverterTestSetup {

	private static String CONV_PREFIX = "Converter_25";

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST25(), false);
		this.currentProject = getJavaProject(CONV_PREFIX);
		this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_25);
		this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_25);
		this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_25);
	}

	public ModuleImportASTConverterTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ModuleImportASTConverterTest.class);
	}

	static int getAST25() {
		return AST.JLS25;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}


	public void test001() throws CoreException {
		String contents = """
				package p;
				import module java.base;
				import static java.lang.System.out;
				class X {
					void m() {
						out.println(Map.class.toString());
					}
				}
				""";
		this.workingCopy = getWorkingCopy("/" + CONV_PREFIX + "/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<ImportDeclaration> imports = compilationUnit.imports();
		assertEquals("Incorrect no of imports", 2, imports.size());

		{
			ImportDeclaration imp = imports.get(0);
			assertEquals("Incorrect modifier bits", Modifier.MODULE, imp.getModifiers());
			assertEquals("Incorrect no of modifiers", 1, imp.modifiers().size());
			Modifier mod = (Modifier) imp.modifiers().get(0);
			assertEquals("Incorrect modifier", "module", mod.getKeyword().toString());
			assertEquals("Incorrect modifier", Modifier.ModifierKeyword.MODULE_KEYWORD, mod.getKeyword());
			assertEquals("Incorrect position", 18, mod.getStartPosition());
			assertEquals("Incorrect content", "module", contents.substring(mod.getStartPosition(), mod.getStartPosition()+6));
			assertEquals("Incorrect name", "java.base", imp.getName().toString());
		}
		{
			ImportDeclaration imp = imports.get(1);
			assertEquals("Incorrect modifier bits", Modifier.STATIC, imp.getModifiers());
			assertEquals("Incorrect no of modifiers", 1, imp.modifiers().size());
			Modifier mod = (Modifier) imp.modifiers().get(0);
			assertEquals("Incorrect modifier", "static", mod.getKeyword().toString());
			assertEquals("Incorrect modifier", Modifier.ModifierKeyword.STATIC_KEYWORD, mod.getKeyword());
			assertEquals("Incorrect position", 43, mod.getStartPosition());
			assertEquals("Incorrect content", "static", contents.substring(mod.getStartPosition(), mod.getStartPosition()+6));
			assertEquals("Incorrect name", "java.lang.System.out", imp.getName().toString());
		}
	}

	public void test002() throws CoreException {
		String contents = """
					/** */
					void main() {
					    System.out.println("Eclipse");
					}
				""";
		this.workingCopy = getWorkingCopy("/" + CONV_PREFIX + "/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		ImplicitTypeDeclaration implicitTypeDeclaration = (ImplicitTypeDeclaration) compilationUnit.types().get(0);
		assertEquals("Not an ImplicitTypeDeclaration Type", implicitTypeDeclaration.getNodeType(), ASTNode.UNNAMED_CLASS);
		assertEquals("Not an ImplicitTypeDeclaration Name Type", implicitTypeDeclaration.getName().getNodeType(), ASTNode.SIMPLE_NAME);
		assertEquals("Identifier is not empty String", implicitTypeDeclaration.getName().getIdentifier(), "");
		MethodDeclaration bodyDeclaration = (MethodDeclaration) implicitTypeDeclaration.bodyDeclarations().get(0);
		assertEquals("Not a Method Declaration", bodyDeclaration.getNodeType(), ASTNode.METHOD_DECLARATION);
		assertEquals("Method Declaration start is not one", bodyDeclaration.getStartPosition(), 1);
		Javadoc javaDoc = bodyDeclaration.getJavadoc();
		assertEquals("Not a JavaDoc", javaDoc.getNodeType(), ASTNode.JAVADOC);
		assertEquals("JavaDoc startPosition is not One", javaDoc.getStartPosition(), 1);
		Block block =  bodyDeclaration.getBody();
		assertEquals("Not a Block", block.getNodeType(), ASTNode.BLOCK);
		assertEquals("Block startPosition is not correct", block.getStartPosition(), 21);
	}

	public void test003_a() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		sealed class A permits B, C {}
		    		final class B extends A {}
		    		non-sealed class C extends A {}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isSealed(a.getModifiers()));

	    assertEquals("permitted types are not present in AST", a.permittedTypes().size(), 2);

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isSealed(aBinding.getModifiers()));
	}

	public void test003_b() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		sealed class A permits B, C {}
		    		final class B extends A {}
		    		non-sealed class C extends A {}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(2);

	    assertTrue(Modifier.isNonSealed(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isNonSealed(aBinding.getModifiers()));
	}

	//public sealed
	public void test003_c() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		public sealed class A permits B, C {}
		    		final class B extends A {}
		    		non-sealed class C extends A {}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isSealed(a.getModifiers()));
	    assertTrue(Modifier.isPublic(a.getModifiers()));

	    assertEquals("permitted types are not present in AST", a.permittedTypes().size(), 2);

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isSealed(aBinding.getModifiers()));
	    assertTrue(Modifier.isPublic(aBinding.getModifiers()));
	}

	//abstract final
	public void test003_d() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			abstract final class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isAbstract(a.getModifiers()));
	    assertTrue(Modifier.isFinal(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isAbstract(aBinding.getModifiers()));
	    assertTrue(Modifier.isFinal(aBinding.getModifiers()));
	}

	//abstract non-sealed
	public void test003_e() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			abstract non-sealed class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isAbstract(a.getModifiers()));
	    assertTrue(Modifier.isNonSealed(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isAbstract(aBinding.getModifiers()));
	    assertTrue(Modifier.isNonSealed(aBinding.getModifiers()));
	}

	//public final
	public void test003_f() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			public final class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isPublic(a.getModifiers()));
	    assertTrue(Modifier.isFinal(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isPublic(aBinding.getModifiers()));
	    assertTrue(Modifier.isFinal(aBinding.getModifiers()));
	}

	//public non-sealed
	public void test003_g() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			public non-sealed class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isPublic(a.getModifiers()));
	    assertTrue(Modifier.isNonSealed(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isPublic(aBinding.getModifiers()));
	    assertTrue(Modifier.isNonSealed(aBinding.getModifiers()));
	}

	//protected non-sealed
	public void test003_h() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			protected non-sealed class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isProtected(a.getModifiers()));
	    assertTrue(Modifier.isNonSealed(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isProtected(aBinding.getModifiers()));
	    assertTrue(Modifier.isNonSealed(aBinding.getModifiers()));
	}

	//private non-sealed
	public void test003_i() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			private non-sealed class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isPrivate(a.getModifiers()));
	    assertTrue(Modifier.isNonSealed(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isPrivate(aBinding.getModifiers()));
	    assertTrue(Modifier.isNonSealed(aBinding.getModifiers()));
	}

	//protected abstract
	public void test003_j() throws CoreException {
	    ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source = """
	    			protected abstract class A {}
	    		""";
	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isProtected(a.getModifiers()));
	    assertTrue(Modifier.isAbstract(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();

	    assertTrue(Modifier.isProtected(aBinding.getModifiers()));
	    assertTrue(Modifier.isAbstract(aBinding.getModifiers()));
	}

	//public sealed interface
	public void test003_k() throws CoreException {
		ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		public sealed interface A permits X {}
	    			public final  class X implements A {}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isSealed(a.getModifiers()));
	    assertTrue(Modifier.isPublic(a.getModifiers()));
	    assertEquals("permitted types are not present in AST", a.permittedTypes().size(), 1);

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isSealed(aBinding.getModifiers()));
	    assertTrue(Modifier.isPublic(aBinding.getModifiers()));
	    assertFalse(Modifier.isNonSealed(aBinding.getModifiers()));
	}

	//public non-sealed interface
	public void test003_l() throws CoreException {
		ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		public non-sealed interface A permits X {}
	    			public final  class X implements A {}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isNonSealed(a.getModifiers()));
	    assertTrue(Modifier.isPublic(a.getModifiers()));
	    assertEquals("permitted types are not present in AST", a.permittedTypes().size(), 1);

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isNonSealed(aBinding.getModifiers()));
	    assertTrue(Modifier.isPublic(aBinding.getModifiers()));
	    assertFalse(Modifier.isSealed(aBinding.getModifiers()));
	}

	//public strictfp interface
	public void test003_m() throws CoreException {
		ASTParser astParser = ASTParser.newParser(getAST25());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "23");
	    options.put(JavaCore.COMPILER_SOURCE, "23");

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		public strictfp interface A permits X {}
	    			public final  class X implements A {}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    TypeDeclaration a = (TypeDeclaration) compilationUnit.types().get(0);

	    assertTrue(Modifier.isStrictfp(a.getModifiers()));
	    assertTrue(Modifier.isPublic(a.getModifiers()));
	    assertEquals("permitted types are not present in AST", a.permittedTypes().size(), 1);

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isStrictfp(aBinding.getModifiers()));
	    assertTrue(Modifier.isPublic(aBinding.getModifiers()));
	}

	public void testBug549248_01() throws CoreException {
	    String contents = """
	        public enum X {
	            JAPAN(new java.lang.String[] {"1","2"}){
	    		    @Override
	    		    public String getGreeting() {
	    				return "Hello from Japan!";
	    			}
	            },

	            public enum LoginType {
	                public com.naver.linewebtoon.common.localization.X.LoginType EMAIL = "null";
	            }
	        }
	        """;

	    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
	    parser.setSource(contents.toCharArray());
	    parser.setEnvironment(null, null, null, true);
	    parser.setResolveBindings(false);

	    Hashtable<String, String> options = JavaCore.getDefaultOptions();
	    options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_23);
	    options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_23);
	    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_23);

	    parser.setCompilerOptions(options);

	    ASTNode node = parser.createAST(null);

	    assertNotNull("ASTNode creation failed. Node is null!", node);
	}
}
