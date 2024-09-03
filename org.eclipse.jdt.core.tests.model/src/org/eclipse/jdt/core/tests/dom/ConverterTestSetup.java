/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings("rawtypes")
public abstract class ConverterTestSetup extends AbstractASTTests {

	static int getJLSFirst() {
		return AST.getAllSupportedVersions().getFirst();
	}

	protected AST ast;
	public static List TEST_SUITES = null;
	public static boolean PROJECT_SETUP = false;

	protected ConverterTestSetup(String name) {
		super(name);
	}
	// From Java 24 onwards, we will keep the jclMin*jar and convertJclMin*jar one and same
	// The /JCL/build.xml has been updated to produce only jclMin*.jar
	private String jclMinName(String compliance) {
		long jdkLevel = CompilerOptions.versionToJdkLevel(compliance);
		return (jdkLevel >= ClassFileConstants.JDK24) ? "jclMin" : "converterJclMin";
	}
	protected IPath getConverterJCLPath() {
		return getConverterJCLPath(CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	}

	protected IPath getConverterJCLPath(String compliance) {
		String jarName = jclMinName(compliance);
		return new Path(getExternalPath() + jarName + compliance + ".jar"); //$NON-NLS-1$
	}

	protected IPath getConverterJCLSourcePath() {
		return getConverterJCLSourcePath(CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	}

	protected IPath getConverterJCLSourcePath(String compliance) {
		String jarName = jclMinName(compliance);
		return new Path(getExternalPath() + jarName + compliance + "src.zip"); //$NON-NLS-1$
	}

	protected IPath getConverterJCLRootSourcePath() {
		return new Path(""); //$NON-NLS-1$
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	@Override
	public void tearDownSuite() throws Exception {
		this.ast = null;
		if (TEST_SUITES == null) {
			this.deleteProject("Converter"); //$NON-NLS-1$
			this.deleteProject("Converter15"); //$NON-NLS-1$
			this.deleteProject("Converter16"); //$NON-NLS-1$
			this.deleteProject("Converter17"); //$NON-NLS-1$
			this.deleteProject("Converter18"); //$NON-NLS-1$
			this.deleteProject("Converter9"); //$NON-NLS-1$
			this.deleteProject("Converter10"); //$NON-NLS-1$
			this.deleteProject("Converter11"); //$NON-NLS-1$
			this.deleteProject("Converter13"); //$NON-NLS-1$
			this.deleteProject("Converter14"); //$NON-NLS-1$
			this.deleteProject("Converter_15"); //$NON-NLS-1$
			this.deleteProject("Converter_15_1"); //$NON-NLS-1$
			this.deleteProject("Converter_16"); //$NON-NLS-1$
			this.deleteProject("Converter_17"); //$NON-NLS-1$
			this.deleteProject("Converter_19"); //$NON-NLS-1$
			this.deleteProject("Converter_21"); //$NON-NLS-1$
			this.deleteProject("Converter_22"); //$NON-NLS-1$
			this.deleteProject("Converter_23"); //$NON-NLS-1$
			this.deleteProject("Converter_24"); //$NON-NLS-1$
			PROJECT_SETUP = false;
		} else {
			TEST_SUITES.remove(getClass());
			if (TEST_SUITES.size() == 0) {
				this.deleteProject("Converter"); //$NON-NLS-1$
				this.deleteProject("Converter15"); //$NON-NLS-1$
				this.deleteProject("Converter16"); //$NON-NLS-1$
				this.deleteProject("Converter17"); //$NON-NLS-1$
				this.deleteProject("Converter18"); //$NON-NLS-1$
				this.deleteProject("Converter9"); //$NON-NLS-1$
				this.deleteProject("Converter10"); //$NON-NLS-1$
				this.deleteProject("Converter11"); //$NON-NLS-1$
				this.deleteProject("Converter13"); //$NON-NLS-1$
				this.deleteProject("Converter14"); //$NON-NLS-1$
				this.deleteProject("Converter_15"); //$NON-NLS-1$
				this.deleteProject("Converter_15_1"); //$NON-NLS-1$
				this.deleteProject("Converter_16"); //$NON-NLS-1$
				this.deleteProject("Converter_17"); //$NON-NLS-1$
				this.deleteProject("Converter_19"); //$NON-NLS-1$
				this.deleteProject("Converter_21"); //$NON-NLS-1$
				this.deleteProject("Converter_22"); //$NON-NLS-1$
				this.deleteProject("Converter_23"); //$NON-NLS-1$
				this.deleteProject("Converter_24"); //$NON-NLS-1$
				PROJECT_SETUP = false;
			}
		}

		super.tearDownSuite();
	}

	protected String getExternalJCLPathString(String compliance) {
		return null;
	}
	@Override
	public void setUpJCLClasspathVariables(String compliance, boolean useFullJCL) throws JavaModelException, IOException {
		if (useFullJCL) {
			 super.setUpJCLClasspathVariables(compliance, useFullJCL);
			 return;
		}
		if ("1.8".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL18_LIB") == null) {
				setupExternalJCL("converterJclMin1.8");
				JavaCore.setClasspathVariables(
					new String[] {"CONVERTER_JCL18_LIB", "CONVERTER_JCL18_SRC", "CONVERTER_JCL18_SRCROOT"},
					new IPath[] {getConverterJCLPath("1.8"), getConverterJCLSourcePath("1.8"), getConverterJCLRootSourcePath()},
					null);
			}
		} else if ("9".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL9_LIB") == null) {
				setupExternalJCL("converterJclMin9");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL9_LIB", "CONVERTER_JCL9_SRC", "CONVERTER_JCL9_SRCROOT"},
						new IPath[] {getConverterJCLPath("9"), getConverterJCLSourcePath("9"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("10".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL10_LIB") == null) {
				setupExternalJCL("converterJclMin10");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL10_LIB", "CONVERTER_JCL10_SRC", "CONVERTER_JCL10_SRCROOT"},
						new IPath[] {getConverterJCLPath("10"), getConverterJCLSourcePath("10"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("11".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL11_LIB") == null) {
				setupExternalJCL("converterJclMin11");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL11_LIB", "CONVERTER_JCL11_SRC", "CONVERTER_JCL11_SRCROOT"},
						new IPath[] {getConverterJCLPath("11"), getConverterJCLSourcePath("11"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("12".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL12_LIB") == null) {
				setupExternalJCL("converterJclMin12");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL12_LIB", "CONVERTER_JCL12_SRC", "CONVERTER_JCL12_SRCROOT"},
						new IPath[] {getConverterJCLPath("12"), getConverterJCLSourcePath("12"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("13".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL13_LIB") == null) {
				setupExternalJCL("converterJclMin13");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL13_LIB", "CONVERTER_JCL13_SRC", "CONVERTER_JCL13_SRCROOT"},
						new IPath[] {getConverterJCLPath("13"), getConverterJCLSourcePath("13"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("14".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL14_LIB") == null) {
				setupExternalJCL("converterJclMin14");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL14_LIB", "CONVERTER_JCL14_SRC", "CONVERTER_JCL14_SRCROOT"},
						new IPath[] {getConverterJCLPath("14"), getConverterJCLSourcePath("14"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("15".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL15_LIB") == null) {
				setupExternalJCL("converterJclMin15");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL15_LIB", "CONVERTER_JCL15_SRC", "CONVERTER_JCL15_SRCROOT"},
						new IPath[] {getConverterJCLPath("15"), getConverterJCLSourcePath("15"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("17".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL_17_LIB") == null) {
				setupExternalJCL("converterJclMin17");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL_17_LIB", "CONVERTER_JCL_17_SRC", "CONVERTER_JCL_17_SRCROOT"},
						new IPath[] {getConverterJCLPath("17"), getConverterJCLSourcePath("17"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("19".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL_19_LIB") == null) {
				setupExternalJCL("converterJclMin19");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL_19_LIB", "CONVERTER_JCL_19_SRC", "CONVERTER_JCL_19_SRCROOT"},
						new IPath[] {getConverterJCLPath("19"), getConverterJCLSourcePath("19"), getConverterJCLRootSourcePath()},
						null);
			}
		} else if ("21".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL_21_LIB") == null) {
				setupExternalJCL("converterJclMin21");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL_21_LIB", "CONVERTER_JCL_21_SRC", "CONVERTER_JCL_21_SRCROOT"},
						new IPath[] {getConverterJCLPath("21"), getConverterJCLSourcePath("21"), getConverterJCLRootSourcePath()},
						null);
			}
		}  else if ("22".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL_22_LIB") == null) {
				setupExternalJCL("converterJclMin22");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL_22_LIB", "CONVERTER_JCL_22_SRC", "CONVERTER_JCL_22_SRCROOT"},
						new IPath[] {getConverterJCLPath("22"), getConverterJCLSourcePath("22"), getConverterJCLRootSourcePath()},
						null);
			}
		}  else if ("24".equals(compliance)) {
			if (JavaCore.getClasspathVariable("CONVERTER_JCL_24_LIB") == null) {
				setupExternalJCL("jclMin24");
				JavaCore.setClasspathVariables(
						new String[] {"CONVERTER_JCL_24_LIB", "CONVERTER_JCL_24_SRC", "CONVERTER_JCL_24_SRCROOT"},
						new IPath[] {getConverterJCLPath("24"), getConverterJCLSourcePath("24"), getConverterJCLRootSourcePath()},
						null);
			}
		}
	}

	/**
	 * Create project and set the jar placeholder.
	 */
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		if (!PROJECT_SETUP) {
			setUpJavaProject("Converter", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
			setUpJavaProject("Converter15", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter16", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter17", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter18", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter9", "9"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter10", "10"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter11", "11"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter13", "13"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter14", "14"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_15", "15"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_15_1", "15"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_16", "16"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_17", "17"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_19", "19"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_21", "21"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_22", "22"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_23", "23"); //$NON-NLS-1$ //$NON-NLS-2$
			setUpJavaProject("Converter_24", "24"); //$NON-NLS-1$ //$NON-NLS-2$
			waitUntilIndexesReady(); // needed to find secondary types
			PROJECT_SETUP = true;
		}
	}

	protected IJavaProject setUpJavaProject(final String projectName, String compliance) throws CoreException, IOException {
		this.currentProject =  setUpJavaProject(projectName, compliance, false);
		if(CompilerOptions.getFirstSupportedJavaVersion().equals(compliance)) {
			this.currentProject.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		}
		return this.currentProject;
	}

	protected void assertExtraDimensionsEqual(String message, List dimensions, String expected) {
		StringBuilder buffer = new StringBuilder();
		Iterator iter = dimensions.iterator();
		while(iter.hasNext()) {
			Dimension dim = (Dimension) iter.next();
			buffer.append(convertAnnotationsList(dim.annotations()));
			if (iter.hasNext()) {
				buffer.append("[] ");
			} else {
				buffer.append("[]");
			}
		}
		assertEquals(message, expected, buffer.toString());
	}

	protected String convertAnnotationsList(List annotations) {
		StringBuilder buffer = new StringBuilder();
		Iterator iter = annotations.iterator();
		while (iter.hasNext()) {
			buffer.append('@');
			buffer.append(((Annotation) iter.next()).getTypeName().getFullyQualifiedName());
			buffer.append(' ');
		}
		return buffer.toString();
	}

	public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings,
			boolean bindingsRecovery) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), unit, resolveBindings, false, bindingsRecovery);
	}

	public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), unit, resolveBindings);
	}

	public ASTNode runConversion(ICompilationUnit unit, int position, boolean resolveBindings) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), unit, position, resolveBindings);
	}

	public ASTNode runConversion(IClassFile classFile, int position, boolean resolveBindings) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), classFile, position, resolveBindings);
	}

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), source, unitName, project);
	}

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project, boolean resolveBindings) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), source, unitName, project, resolveBindings);
	}

	public ASTNode runConversion(int astLevel, ICompilationUnit unit, boolean resolveBindings) {
		return runConversion(astLevel, unit, resolveBindings, false);
	}

	public ASTNode runConversion(int astLevel, ICompilationUnit unit, boolean resolveBindings, boolean statementsRecovery) {
		return runConversion(astLevel, unit, resolveBindings, statementsRecovery, false);
	}

	public ASTNode runConversion(
			ICompilationUnit unit,
			boolean resolveBindings,
			boolean statementsRecovery,
			boolean bindingsRecovery) {
		ASTParser parser = ASTParser.newParser(getJLSFirst());
		parser.setSource(unit);
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		parser.setBindingsRecovery(bindingsRecovery);
		return parser.createAST(null);
	}

	public ASTNode runConversion(
			int astLevel,
			ICompilationUnit unit,
			boolean resolveBindings,
			boolean statementsRecovery,
			boolean bindingsRecovery) {
		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(unit);
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		parser.setBindingsRecovery(bindingsRecovery);
		return parser.createAST(null);
	}

	class NullBindingVerifier extends ASTVisitor {

		public void endVisit(ArrayAccess node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(ArrayCreation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(ArrayInitializer node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(Assignment node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(BooleanLiteral node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(CastExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(CharacterLiteral node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(ClassInstanceCreation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(ConditionalExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(FieldAccess node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(InfixExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(InstanceofExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(MarkerAnnotation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(MethodInvocation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(NormalAnnotation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(NullLiteral node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(NumberLiteral node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(ParenthesizedExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(PostfixExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(PrefixExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(SingleMemberAnnotation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(StringLiteral node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(SuperFieldAccess node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(SuperMethodInvocation node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(ThisExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(TypeLiteral node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(VariableDeclarationExpression node) {
			assertNotNull(node+" should have a binding", node.resolveTypeBinding());
			super.endVisit(node);
		}

		public void endVisit(AnnotationTypeDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(AnnotationTypeMemberDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(AnonymousClassDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(ArrayType node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(EnumDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(ImportDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(MemberRef node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(MethodDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(ModuleDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}
		public void endVisit(MethodRef node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(PackageDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(ParameterizedType node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(PrimitiveType node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(QualifiedName node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(QualifiedType node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(SimpleName node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(SimpleType node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(SingleVariableDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(TypeDeclaration node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(TypeDeclarationStatement node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(TypeParameter node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(VariableDeclarationFragment node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

		public void endVisit(WildcardType node) {
			assertNotNull(node+" should have a binding", node.resolveBinding());
			super.endVisit(node);
		}

	}
	public ASTNode runJLS3Conversion(ICompilationUnit unit, boolean resolveBindings) {
		return runJLS3Conversion(unit, resolveBindings, false);
	}

	public ASTNode runJLS3Conversion(ICompilationUnit unit, boolean resolveBindings, boolean bindingRecovery) {

		// Create parser
		ASTParser parser = ASTParser.newParser(getJLSFirst());
		parser.setSource(unit);
		parser.setResolveBindings(resolveBindings);
		parser.setBindingsRecovery(bindingRecovery);

		// Parse compilation unit
		ASTNode result = parser.createAST(null);

		// Verify we get a compilation unit node and that binding are correct
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		if (resolveBindings && compilationUnit.getProblems().length == 0) {
			compilationUnit.accept(new NullBindingVerifier());
		}
		return result;
	}

	public ASTNode runJLS4Conversion(ICompilationUnit unit, boolean resolveBindings) {
		return runJLS4Conversion(unit, resolveBindings, false);
	}

	public ASTNode runJLS4Conversion(ICompilationUnit unit, boolean resolveBindings, boolean bindingRecovery) {

		// Create parser
		ASTParser parser = ASTParser.newParser(getJLSFirst());
		parser.setSource(unit);
		parser.setResolveBindings(resolveBindings);
		parser.setBindingsRecovery(bindingRecovery);

		// Parse compilation unit
		ASTNode result = parser.createAST(null);

		// Verify we get a compilation unit node and that binding are correct
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		if (resolveBindings && compilationUnit.getProblems().length == 0) {
			compilationUnit.accept(new NullBindingVerifier());
		}
		return result;
	}

	public ASTNode runJLS8Conversion(ICompilationUnit unit, boolean resolveBindings) {
		return runJLS8Conversion(unit, resolveBindings, false);
	}

	public ASTNode runJLS8Conversion(ICompilationUnit unit, boolean resolveBindings, boolean bindingRecovery) {
		// Create parser
        ASTParser parser = ASTParser.newParser(getJLSFirst());
		parser.setSource(unit);
		parser.setResolveBindings(resolveBindings);
		parser.setBindingsRecovery(bindingRecovery);

		// Parse compilation unit
		ASTNode result = parser.createAST(null);

		// Verify we get a compilation unit node and that binding are correct
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		if (resolveBindings && compilationUnit.getProblems().length == 0) {
			compilationUnit.accept(new NullBindingVerifier());
		}
		return result;
	}
	public ASTNode runConversion(int astLevel, ICompilationUnit unit, int position, boolean resolveBindings) {

		// Create parser
		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(unit);
		parser.setFocalPosition(position);
		parser.setResolveBindings(resolveBindings);

		// Parse compilation unit
		ASTNode result = parser.createAST(null);

		// Verify we get a compilation unit node and that binding are correct
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		if (resolveBindings && compilationUnit.getProblems().length == 0) {
			compilationUnit.accept(new NullBindingVerifier());
		}
		return result;
	}

	public ASTNode runConversion(int astLevel, IClassFile classFile, int position, boolean resolveBindings) {

		// Create parser
		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(classFile);
		parser.setFocalPosition(position);
		parser.setResolveBindings(resolveBindings);

		// Parse compilation unit
		ASTNode result = parser.createAST(null);

		// Verify we get a compilation unit node and that binding are correct
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		if (resolveBindings && compilationUnit.getProblems().length == 0) {
			compilationUnit.accept(new NullBindingVerifier());
		}
		return result;
	}

	public ASTNode runConversion(int astLevel, char[] source, String unitName, IJavaProject project) {
		return runConversion(astLevel, source, unitName, project, false);
	}

	public ASTNode runConversion(int astLevel, char[] source, String unitName, IJavaProject project, boolean resolveBindings) {
		return runConversion(astLevel, source, unitName, project, null, resolveBindings);
	}

	public ASTNode runConversion(int astLevel, char[] source, String unitName, IJavaProject project, Map<String, String> options, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		if (options != null) {
			parser.setCompilerOptions(options);
		}
		parser.setResolveBindings(resolveBindings);
		return parser.createAST(null);
	}

	public ASTNode runConversion(int astLevel, char[] source, String unitName, IJavaProject project, Map<String, String> options) {
		return runConversion(astLevel, source, unitName, project, options, false);
	}

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map<String, String> options, boolean resolveBindings) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), source, unitName, project, options, resolveBindings);
	}
	public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map<String, String> options) {
		return runConversion(AST.getAllSupportedVersions().getFirst(), source, unitName, project, options);
	}

	protected ASTNode getASTNodeToCompare(org.eclipse.jdt.core.dom.CompilationUnit unit) {
		ExpressionStatement statement = (ExpressionStatement) getASTNode(unit, 0, 0, 0);
		return (ASTNode) ((MethodInvocation) statement.getExpression()).arguments().get(0);
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex, int statementIndex) {
		BodyDeclaration bodyDeclaration = (BodyDeclaration) getASTNode(unit, typeIndex, bodyIndex);
		if (bodyDeclaration instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
			Block block = methodDeclaration.getBody();
			return (ASTNode) block.statements().get(statementIndex);
		} else if (bodyDeclaration instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclaration;
			return (ASTNode) typeDeclaration.bodyDeclarations().get(statementIndex);
		} else if (bodyDeclaration instanceof Initializer) {
			Initializer initializer = (Initializer) bodyDeclaration;
			Block block = initializer.getBody();
			return (ASTNode) block.statements().get(statementIndex);
		} else if (bodyDeclaration instanceof FieldDeclaration) {
			return bodyDeclaration;
		}
		return null;
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex) {
		return (ASTNode) ((AbstractTypeDeclaration)unit.types().get(typeIndex)).bodyDeclarations().get(bodyIndex);
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex) {
		return (ASTNode) unit.types().get(typeIndex);
	}

	protected void checkSourceRange(int start, int length, String expectedContents, String source) {
		assertTrue("length == 0", length != 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("start == -1", start != -1); //$NON-NLS-1$
		String actualContentsString = source.substring(start, start + length);
		assertSourceEquals("Unexpected source", Util.convertToIndependantLineDelimiter(expectedContents), Util.convertToIndependantLineDelimiter(actualContentsString));
	}

	protected void checkSourceRange(ASTNode node, String expectedContents, String source) {
		assertNotNull("The node is null", node); //$NON-NLS-1$
		assertTrue("The node(" + node.getClass() + ").getLength() == 0", node.getLength() != 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("The node.getStartPosition() == -1", node.getStartPosition() != -1); //$NON-NLS-1$
		int length = node.getLength();
		int start = node.getStartPosition();
		String actualContentsString = source.substring(start, start + length);
		assertSourceEquals("Unexpected source", Util.convertToIndependantLineDelimiter(expectedContents), Util.convertToIndependantLineDelimiter(actualContentsString));
	}

	protected void checkSourceRange(ASTNode node, String expectedContents, char[] source) {
		checkSourceRange(node, expectedContents, source, false);
	}
	protected void checkSourceRange(ASTNode node, String expectedContents, char[] source, boolean expectMalformed) {
		assertNotNull("The node is null", node); //$NON-NLS-1$
		assertTrue("The node(" + node.getClass() + ").getLength() == 0", node.getLength() != 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("The node.getStartPosition() == -1", node.getStartPosition() != -1); //$NON-NLS-1$
		int length = node.getLength();
		int start = node.getStartPosition();
		char[] actualContents = new char[length];
		System.arraycopy(source, start, actualContents, 0, length);
		String actualContentsString = new String(actualContents);
		assertSourceEquals("Unexpected source", Util.convertToIndependantLineDelimiter(expectedContents), Util.convertToIndependantLineDelimiter(actualContentsString));
		if (expectMalformed) {
			assertTrue("Is not malformed", isMalformed(node));
		} else {
			assertFalse("Is malformed", isMalformed(node));
		}
	}

	protected boolean isMalformed(ASTNode node) {
		return (node.getFlags() & ASTNode.MALFORMED) != 0;
	}

	protected boolean isRecovered(ASTNode node) {
		return (node.getFlags() & ASTNode.RECOVERED) != 0;
	}

	protected boolean isOriginal(ASTNode node) {
		return (node.getFlags() & ASTNode.ORIGINAL) != 0;
	}

	protected void assertProblemsSize(CompilationUnit compilationUnit, int expectedSize) {
		assertProblemsSize(compilationUnit, expectedSize, "");
	}
	protected void assertProblemsSize(CompilationUnit compilationUnit, int expectedSize, String expectedOutput) {
		final IProblem[] problems = compilationUnit.getProblems();
		final int length = problems.length;
		if (length != expectedSize) {
			checkProblemMessages(expectedOutput, problems, length);
			assertEquals("Wrong size", expectedSize, length);
		}
		checkProblemMessages(expectedOutput, problems, length);
	}

	public void checkProblemMessages(String expectedOutput, final IProblem[] problems, final int length) {
		if (length != 0) {
			if (expectedOutput != null) {
				StringBuilder buffer = new StringBuilder();
				for (int i = 0; i < length; i++) {
					buffer.append(problems[i].getMessage());
					if (i < length - 1) {
						buffer.append('\n');
					}
				}
				String actualOutput = String.valueOf(buffer);
				expectedOutput = Util.convertToIndependantLineDelimiter(expectedOutput);
				actualOutput = Util.convertToIndependantLineDelimiter(actualOutput);
				if (!expectedOutput.equals(actualOutput)) {
					boolean match = checkAlternateProblemMessages(expectedOutput, actualOutput, problems, length);
					if( !match ) {
						System.out.println(Util.displayString(actualOutput));
						assertEquals("different output", expectedOutput, actualOutput);
					}
				}
			}
		}
	}
	private boolean checkAlternateProblemMessages(String expectedOutput, String actualOutput, final IProblem[] problems, final int length) {
		List<String> expectedSplit = Arrays.asList(expectedOutput.split("\n"));
		for( int i = 0; i < problems.length; i++ ) {
			String oneActualMessage = problems[i].getMessage();
			String oneExpectedMessage = i < expectedSplit.size() ? expectedSplit.get(i) : null;
			if( !oneActualMessage.equals(oneExpectedMessage)) {
				boolean matchesAlt = matchesAlternateMessage(oneActualMessage, oneExpectedMessage, problems[i].getID(), problems[i].getArguments());
				if(!matchesAlt) {
					return false;
				}
			}
		}
		return true;
	}
	private boolean matchesAlternateMessage(String original, String expected, int problemId, Object[] arguments) {
		String fqqnToSimpleNameRegex = "[^-\\s<,]*\\.";

		switch(problemId) {
		case IProblem.NotVisibleType:
			List<String> possible = new ArrayList<>();
			String msg = "The type %s is not visible";
			int lastDot = ((String)arguments[0]).lastIndexOf(".") + 1;
			String alt = String.format(msg, ((String)arguments[0]).substring(lastDot));
			String alt2 = String.format(msg, ((String)arguments[0]));
			possible.add(alt);
			possible.add(alt2);

			if( arguments.length == 3 && ((String)arguments[0]).startsWith((String)arguments[2])) {
				int lastDot2 = ((String)arguments[2]).lastIndexOf(".") + 1;
				String type = ((String)arguments[0]).substring(lastDot2);
				String alt3 = String.format(msg, type);
				possible.add(alt3);
			}
			return possible.contains(expected);
		case IProblem.UsingDeprecatedField:
			if( arguments.length == 2 ) {
				String simpleName = ((String)arguments[1]).replaceAll(fqqnToSimpleNameRegex, "");
				if(("The type " + simpleName + " is deprecated").equals(expected))
					return true;
				String simpleName2 = ((String)arguments[0]).replaceAll(fqqnToSimpleNameRegex, "");
				if(("The type " + simpleName2 + " is deprecated").equals(expected))
					return true;
				if((arguments[0] + " in " + arguments[1] + " has been deprecated and marked for removal").equals(expected))
					return true;
			}
			return false;
		case IProblem.PackageDoesNotExistOrIsEmpty:
			return (arguments[0] + " cannot be resolved to a type").equals(expected);
		case IProblem.UndefinedType:
		case IProblem.UndefinedName:
			return (arguments[0] + " cannot be resolved to a type").equals(expected);
		case IProblem.RawTypeReference:
			String[] segments = ((String)arguments[0]).split("\\.");
			String simple = segments[segments.length-1];
			String alt3 = simple + " is a raw type. References to generic type " + simple + "<T> should be parameterized";
			return alt3.equals(expected);
		case IProblem.TypeMismatch:
			if( expected == null )
				return false;
			String expected2 = expected.replaceAll("capture#[0-9]*-", "capture ");
			String arg0 = ((String)arguments[0]).replaceAll(fqqnToSimpleNameRegex, "").replaceAll("capture#[0-9]* ", "capture ");
			String arg1 = ((String)arguments[1]).replaceAll(fqqnToSimpleNameRegex, "").replaceAll("capture#[0-9]* ", "capture ");
			String altString = "Type safety: Unchecked cast from " + arg0 + " to " + arg1;
			if( altString.equals(expected2) )
				return true;

			altString = "Type mismatch: cannot convert from " + arg0 + " to " + arg1;
			if( altString.equals(expected2) )
				return true;
			return false;
		case IProblem.VarargsConflict:
			return "Extended dimensions are illegal for a variable argument".equals(expected);
		case IProblem.UnsafeRawMethodInvocation:
			String clazzName = ((String)arguments[1]).substring(((String)arguments[1]).lastIndexOf(".") + 1);
			String pattern = "Type safety: The method .* belongs to the raw type " + clazzName + ". References to generic type Y.* should be parameterized";
			boolean m = Pattern.matches(pattern, expected);
			return m;
		case IProblem.JavadocMissingParamTag:
			return original.replace("no @param for ", "Javadoc: Missing tag for parameter ").equals(expected);
		case IProblem.UncheckedAccessOfValueOfFreeTypeVariable:
			String p = "Type safety: The expression of type (.*) needs unchecked conversion to conform to (.*)";
			Pattern r = Pattern.compile(p);
			Matcher m1 = r.matcher(expected);
			if (m1.find( )) {
				String g0 = m1.group(1);
				String g1 = m1.group(2);
				String originalToSimple = original.replaceAll(fqqnToSimpleNameRegex, "");
				String found = "unchecked conversion\n  required:.*" + g1 + "\n  found:.*" + g0;
				if( originalToSimple.replaceAll(found, "").equals("")) {
					return true;
				}
			}
			return false;
		case IProblem.DuplicateMethod: // TODO these should really be fixed elsewhere
			if( expected.startsWith("Duplicate local variable ")) {
				return original.startsWith(expected.substring(16) + " is already defined");
			}
			if( expected.startsWith("Duplicate parameter ")) {
				return original.startsWith("variable " + expected.substring(20) + " is already defined");
			}
			if( expected.startsWith("Duplicate nested type ")) {
				return original.startsWith("class " + expected.substring(22) + " is already defined");
			}
			return false;
		default:
			return false;
		}
	}


}
