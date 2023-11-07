/*******************************************************************************
 * Copyright (c) 2000, 20157IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.performance;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FullSourceWorkspaceASTTests extends FullSourceWorkspaceTests {
	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

    // Tests counter
    private static int TESTS_COUNT = 0;
	private final static int ITERATIONS_COUNT = 10;
	int nodesCount = 0;

    // Log files
    private static PrintStream[] LOG_STREAMS = new PrintStream[DIM_NAMES.length];

	public FullSourceWorkspaceASTTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testDomAstCreationJLS2";
	}

	public static Test suite() {
        Test suite = buildSuite(testClass());
        TESTS_COUNT = suite.countTestCases();
        createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
        return suite;
    }

    private static Class testClass() {
        return FullSourceWorkspaceASTTests.class;
    }

	@Override
    protected void setUp() throws Exception {
		waitUntilIndexesReady();
		super.setUp();
	}

	@Override
    protected void tearDown() throws Exception {

		// End of execution => one test less
        TESTS_COUNT--;

        // Log perf result
        if (LOG_DIR != null) {
            logPerfResult(LOG_STREAMS, TESTS_COUNT);
        }

		// Print statistics
        if (TESTS_COUNT == 0) {
			System.out.println("-------------------------------------");
			System.out.println("DOM/AST creation performance test statistics:");
			NumberFormat intFormat = NumberFormat.getIntegerInstance();
			System.out.println("  - "+intFormat.format(this.nodesCount)+" nodes have been parsed.");
			System.out.println("-------------------------------------\n");
        }

		// Call super at the end as it close print streams
        super.tearDown();
	}

	/**
	 * Comment Mapper visitor
	 */
	class CommentMapperASTVisitor extends ASTVisitor {
		CompilationUnit compilationUnit;
		int nodes = 0;
		int extendedStartPositions = 0;
		int extendedEndPositions = 0;

		public CommentMapperASTVisitor(CompilationUnit unit) {
			this.compilationUnit = unit;
		}
		protected boolean visitNode(ASTNode node) {
			// get node positions and extended positions
			int nodeStart = node.getStartPosition();
			int nodeEnd = node.getLength() - 1 - nodeStart;
			int extendedStart = this.compilationUnit.getExtendedStartPosition(node);
			int extendedEnd = this.compilationUnit.getExtendedLength(node) - 1 - extendedStart;
			// update counters
			if (extendedStart < nodeStart) this.extendedStartPositions++;
			if (extendedEnd > nodeEnd) this.extendedEndPositions++;
			this.nodes++;
			return true;
		}
		protected void endVisitNode(ASTNode node) {
			// do nothing
		}
		public boolean visit(AnonymousClassDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(ArrayAccess node) {
			return visitNode(node);
		}
		public boolean visit(ArrayCreation node) {
			return visitNode(node);
		}
		public boolean visit(ArrayInitializer node) {
			return visitNode(node);
		}
		public boolean visit(ArrayType node) {
			visitNode(node);
			return false;
		}
		public boolean visit(AssertStatement node) {
			return visitNode(node);
		}
		public boolean visit(Assignment node) {
			return visitNode(node);
		}
		public boolean visit(Block node) {
			return visitNode(node);
		}
		public boolean visit(BooleanLiteral node) {
			return visitNode(node);
		}
		public boolean visit(BreakStatement node) {
			return visitNode(node);
		}
		public boolean visit(CastExpression node) {
			return visitNode(node);
		}
		public boolean visit(CatchClause node) {
			return visitNode(node);
		}
		public boolean visit(CharacterLiteral node) {
			return visitNode(node);
		}
		public boolean visit(ClassInstanceCreation node) {
			return visitNode(node);
		}
		public boolean visit(CompilationUnit node) {
			return visitNode(node);
		}
		public boolean visit(ConditionalExpression node) {
			return visitNode(node);
		}
		public boolean visit(ConstructorInvocation node) {
			return visitNode(node);
		}
		public boolean visit(ContinueStatement node) {
			return visitNode(node);
		}
		public boolean visit(DoStatement node) {
			return visitNode(node);
		}
		public boolean visit(EmptyStatement node) {
			return visitNode(node);
		}
		public boolean visit(ExpressionStatement node) {
			return visitNode(node);
		}
		public boolean visit(FieldAccess node) {
			return visitNode(node);
		}
		public boolean visit(FieldDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(ForStatement node) {
			return visitNode(node);
		}
		public boolean visit(IfStatement node) {
			return visitNode(node);
		}
		public boolean visit(ImportDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(InfixExpression node) {
			return visitNode(node);
		}
		public boolean visit(InstanceofExpression node) {
			return visitNode(node);
		}
		public boolean visit(Initializer node) {
			return visitNode(node);
		}
		public boolean visit(Javadoc node) {
			// do not visit Javadoc tags by default. Use constructor with
			// boolean to enable.
			if (super.visit(node)) { return visitNode(node); }
			return false;
		}
		public boolean visit(LabeledStatement node) {
			return visitNode(node);
		}
		public boolean visit(MethodDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(ModuleDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(MethodInvocation node) {
			return visitNode(node);
		}
		public boolean visit(NullLiteral node) {
			return visitNode(node);
		}
		public boolean visit(NumberLiteral node) {
			return visitNode(node);
		}
		public boolean visit(PackageDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(ParenthesizedExpression node) {
			return visitNode(node);
		}
		public boolean visit(PostfixExpression node) {
			return visitNode(node);
		}
		public boolean visit(PrefixExpression node) {
			return visitNode(node);
		}
		public boolean visit(PrimitiveType node) {
			return visitNode(node);
		}
		public boolean visit(QualifiedName node) {
			return visitNode(node);
		}
		public boolean visit(ReturnStatement node) {
			return visitNode(node);
		}
		public boolean visit(SimpleName node) {
			return visitNode(node);
		}
		public boolean visit(SimpleType node) {
			return visitNode(node);
		}
		public boolean visit(StringLiteral node) {
			return visitNode(node);
		}
		public boolean visit(SuperConstructorInvocation node) {
			return visitNode(node);
		}
		public boolean visit(SuperFieldAccess node) {
			return visitNode(node);
		}
		public boolean visit(SuperMethodInvocation node) {
			return visitNode(node);
		}
		public boolean visit(SwitchCase node) {
			return visitNode(node);
		}
		public boolean visit(SwitchStatement node) {
			return visitNode(node);
		}
		public boolean visit(SynchronizedStatement node) {
			return visitNode(node);
		}
		public boolean visit(ThisExpression node) {
			return visitNode(node);
		}
		public boolean visit(ThrowStatement node) {
			return visitNode(node);
		}
		public boolean visit(TryStatement node) {
			return visitNode(node);
		}
		public boolean visit(TypeDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(TypeDeclarationStatement node) {
			return visitNode(node);
		}
		public boolean visit(TypeLiteral node) {
			return visitNode(node);
		}
		public boolean visit(SingleVariableDeclaration node) {
			return visitNode(node);
		}
		public boolean visit(VariableDeclarationExpression node) {
			return visitNode(node);
		}
		public boolean visit(VariableDeclarationStatement node) {
			return visitNode(node);
		}
		public boolean visit(VariableDeclarationFragment node) {
			return visitNode(node);
		}
		public boolean visit(WhileStatement node) {
			return visitNode(node);
		}
		/* since 3.0 */
		public boolean visit(BlockComment node) {
			return visitNode(node);
		}
		public boolean visit(LineComment node) {
			return visitNode(node);
		}
		public boolean visit(MemberRef node) {
			return visitNode(node);
		}
		public boolean visit(MethodRef node) {
			return visitNode(node);
		}
		public boolean visit(MethodRefParameter node) {
			return visitNode(node);
		}
		public boolean visit(TagElement node) {
			return visitNode(node);
		}
		public boolean visit(TextElement node) {
			return visitNode(node);
		}
		public void endVisit(AnonymousClassDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(ArrayAccess node) {
			endVisitNode(node);
		}
		public void endVisit(ArrayCreation node) {
			endVisitNode(node);
		}
		public void endVisit(ArrayInitializer node) {
			endVisitNode(node);
		}
		public void endVisit(ArrayType node) {
			endVisitNode(node);
		}
		public void endVisit(AssertStatement node) {
			endVisitNode(node);
		}
		public void endVisit(Assignment node) {
			endVisitNode(node);
		}
		public void endVisit(Block node) {
			endVisitNode(node);
		}
		public void endVisit(BooleanLiteral node) {
			endVisitNode(node);
		}
		public void endVisit(BreakStatement node) {
			endVisitNode(node);
		}
		public void endVisit(CastExpression node) {
			endVisitNode(node);
		}
		public void endVisit(CatchClause node) {
			endVisitNode(node);
		}
		public void endVisit(CharacterLiteral node) {
			endVisitNode(node);
		}
		public void endVisit(ClassInstanceCreation node) {
			endVisitNode(node);
		}
		public void endVisit(CompilationUnit node) {
			endVisitNode(node);
		}
		public void endVisit(ConditionalExpression node) {
			endVisitNode(node);
		}
		public void endVisit(ConstructorInvocation node) {
			endVisitNode(node);
		}
		public void endVisit(ContinueStatement node) {
			endVisitNode(node);
		}
		public void endVisit(DoStatement node) {
			endVisitNode(node);
		}
		public void endVisit(EmptyStatement node) {
			endVisitNode(node);
		}
		public void endVisit(ExpressionStatement node) {
			endVisitNode(node);
		}
		public void endVisit(FieldAccess node) {
			endVisitNode(node);
		}
		public void endVisit(FieldDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(ForStatement node) {
			endVisitNode(node);
		}
		public void endVisit(IfStatement node) {
			endVisitNode(node);
		}
		public void endVisit(ImportDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(InfixExpression node) {
			endVisitNode(node);
		}
		public void endVisit(InstanceofExpression node) {
			endVisitNode(node);
		}
		public void endVisit(Initializer node) {
			endVisitNode(node);
		}
		public void endVisit(Javadoc node) {
			endVisitNode(node);
		}
		public void endVisit(LabeledStatement node) {
			endVisitNode(node);
		}
		public void endVisit(MethodDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(ModuleDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(MethodInvocation node) {
			endVisitNode(node);
		}
		public void endVisit(NullLiteral node) {
			endVisitNode(node);
		}
		public void endVisit(NumberLiteral node) {
			endVisitNode(node);
		}
		public void endVisit(PackageDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(ParenthesizedExpression node) {
			endVisitNode(node);
		}
		public void endVisit(PostfixExpression node) {
			endVisitNode(node);
		}
		public void endVisit(PrefixExpression node) {
			endVisitNode(node);
		}
		public void endVisit(PrimitiveType node) {
			endVisitNode(node);
		}
		public void endVisit(QualifiedName node) {
			endVisitNode(node);
		}
		public void endVisit(ReturnStatement node) {
			endVisitNode(node);
		}
		public void endVisit(SimpleName node) {
			endVisitNode(node);
		}
		public void endVisit(SimpleType node) {
			endVisitNode(node);
		}
		public void endVisit(StringLiteral node) {
			endVisitNode(node);
		}
		public void endVisit(SuperConstructorInvocation node) {
			endVisitNode(node);
		}
		public void endVisit(SuperFieldAccess node) {
			endVisitNode(node);
		}
		public void endVisit(SuperMethodInvocation node) {
			endVisitNode(node);
		}
		public void endVisit(SwitchCase node) {
			endVisitNode(node);
		}
		public void endVisit(SwitchStatement node) {
			endVisitNode(node);
		}
		public void endVisit(SynchronizedStatement node) {
			endVisitNode(node);
		}
		public void endVisit(ThisExpression node) {
			endVisitNode(node);
		}
		public void endVisit(ThrowStatement node) {
			endVisitNode(node);
		}
		public void endVisit(TryStatement node) {
			endVisitNode(node);
		}
		public void endVisit(TypeDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(TypeDeclarationStatement node) {
			endVisitNode(node);
		}
		public void endVisit(TypeLiteral node) {
			endVisitNode(node);
		}
		public void endVisit(SingleVariableDeclaration node) {
			endVisitNode(node);
		}
		public void endVisit(VariableDeclarationExpression node) {
			endVisitNode(node);
		}
		public void endVisit(VariableDeclarationStatement node) {
			endVisitNode(node);
		}
		public void endVisit(VariableDeclarationFragment node) {
			endVisitNode(node);
		}
		public void endVisit(WhileStatement node) {
			endVisitNode(node);
		}
		/* since 3.0 */
		public void endVisit(BlockComment node) {
			endVisitNode(node);
		}
		public void endVisit(LineComment node) {
			endVisitNode(node);
		}
		public void endVisit(MemberRef node) {
			endVisitNode(node);
		}
		public void endVisit(MethodRef node) {
			endVisitNode(node);
		}
		public void endVisit(MethodRefParameter node) {
			endVisitNode(node);
		}
		public void endVisit(TagElement node) {
			endVisitNode(node);
		}
		public void endVisit(TextElement node) {
			endVisitNode(node);
		}
	}

	/**
	 * Create AST nodes tree for a given compilation unit at a JLS given level
	 *
	 * @deprecated
	 */
	private void createAST(ICompilationUnit unit, int astLevel) throws JavaModelException {

		// Warm up
		for (int i = 0; i < 2; i++) {
			ASTParser parser = ASTParser.newParser(astLevel);
			parser.setSource(unit);
			parser.setResolveBindings(false);
			parser.createAST(null);
		}

		// Measures
		int measures = MEASURES_COUNT * 2;
		int iterations = ITERATIONS_COUNT >> 1;
		for (int i = 0; i < measures; i++) {
			ASTNode result = null;
			runGc();
			startMeasuring();
			for (int j=0; j<iterations; j++) {
				ASTParser parser = ASTParser.newParser(astLevel);
				parser.setSource(unit);
				parser.setResolveBindings(false);
				result = parser.createAST(null);
			}
			stopMeasuring();
			assertEquals("Wrong type for node"+result, result.getNodeType(), ASTNode.COMPILATION_UNIT);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			CommentMapperASTVisitor visitor = new CommentMapperASTVisitor(compilationUnit);
			compilationUnit.accept(visitor);
			this.nodesCount += visitor.nodes * iterations;
		}

		// Commit
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * @deprecated To reduce deprecated warnings
	 */
	public void testPerfDomAstCreationJLS2() throws JavaModelException {
		tagAsSummary("DOM AST tree for one file using JLS2", false); // do NOT put in fingerprint

		ICompilationUnit unit = getCompilationUnit("org.eclipse.jdt.core", "org.eclipse.jdt.internal.compiler.parser", "Parser.java");
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL, JavaCore.IGNORE);
		JavaCore.setOptions(options);
		createAST(unit, AST.JLS2);
	}

	/**
	 * Performance DOM/AST creation on the entire workspace using JLS3.
	 */
	public void testPerfDomAstCreationJLS3() throws JavaModelException {
		tagAsSummary("DOM AST tree for one file using JLS3", false); // do NOT put in fingerprint

		ICompilationUnit unit = getCompilationUnit("org.eclipse.jdt.core", "org.eclipse.jdt.internal.compiler.parser", "Parser.java");
		createAST(unit, JLS3_INTERNAL);
	}

	/*
	 * Create AST nodes tree for all compilation units of all projects
	 */
	private int runAllProjectsAstCreation(int astLevel) throws JavaModelException {
		int unitsCount = 0;
		startMeasuring();
		if (DEBUG) System.out.println("Creating AST hierarchy for all units of projects:");
		for (int i = 0; i < ALL_PROJECTS.length; i++) {
			// Get project compilation units
			if (DEBUG) System.out.print("\t- "+ALL_PROJECTS[i].getElementName());
			List units = getProjectCompilationUnits(ALL_PROJECTS[i]);
			int size = units.size();
			if (size == 0) {
				if (DEBUG) System.out.println(": empty!");
				continue;
			}
			unitsCount += size;
			List unitsArrays = splitListInSmallArrays(units, 20);
			int n = unitsArrays.size();
			if (DEBUG)
				if (n==1)
					System.out.print(": "+size+" units to proceed ("+n+" step): ");
				else
					System.out.print(": "+size+" units to proceed ("+n+" steps): ");
			while (unitsArrays.size() > 0) {
				ICompilationUnit[] unitsArray = (ICompilationUnit[]) unitsArrays.remove(0);
				if (DEBUG) System.out.print('.');
				int length = unitsArray.length;
				CompilationUnit[] compilationUnits = new CompilationUnit[length];
				// Create AST tree
				for (int ptr=0; ptr<length; ptr++) {
					ICompilationUnit unit = unitsArray[ptr];
					unitsArray[ptr] = null; // release memory handle
					ASTParser parser = ASTParser.newParser(astLevel);
					parser.setSource(unit);
					parser.setResolveBindings(false);
					ASTNode result = parser.createAST(null);
					assertEquals("Wrong type for node"+result, result.getNodeType(), ASTNode.COMPILATION_UNIT);
					compilationUnits[ptr] = (CompilationUnit) result;
				}
			}
			if (DEBUG) System.out.println(" done!");
		}
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
		return unitsCount;
	}

	/**
	 * @deprecated To reduce deprecated warnings
	 */
	public void testWkspDomAstCreationJLS2() throws JavaModelException {
		tagAsSummary("DOM AST tree for workspace files (JLS2)", false); // do NOT put in fingerprint
		runAllProjectsAstCreation(AST.JLS2);
	}

	/*
	 * Create AST nodes for all compilation unit of a given project
	 */
	private void runAstCreation(IJavaProject javaProject) throws JavaModelException {
		if (DEBUG) System.out.println("Creating AST for project" + javaProject.getElementName());
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(javaProject);

		Map options= javaProject.getOptions(true);
		// turn all errors and warnings into ignore. The customizable set of compiler
		// options only contains additional Eclipse options. The standard JDK compiler
		// options can't be changed anyway.
		for (Iterator iter= options.keySet().iterator(); iter.hasNext();) {
			String key= (String)iter.next();
			String value= (String)options.get(key);
			if ("error".equals(value) || "warning".equals(value)) {  //$NON-NLS-1$//$NON-NLS-2$
				// System.out.println("Ignoring - " + key);
				options.put(key, "ignore"); //$NON-NLS-1$
			} else if ("enabled".equals(value)) {
				// System.out.println("	- disabling " + key);
				options.put(key, "disabled");
			}
		}
		options.put(JavaCore.COMPILER_TASK_TAGS, "");
		parser.setCompilerOptions(options);

		List units = getProjectCompilationUnits(javaProject);
		ICompilationUnit[] compilationUnits = new ICompilationUnit[units.size()];
		units.toArray(compilationUnits);

		if (PRINT) {
			System.out.println("	- options: "+options);
			System.out.println("	- "+compilationUnits.length+" units will be parsed in "+javaProject.getElementName()+" project");
		}

		// warm up
		parser.createASTs(compilationUnits, new String[0], new ASTRequestor() {
				public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
					IProblem[] problems = ast.getProblems();
					int length = problems.length;
					if (length > 0) {
						StringBuffer buffer = new StringBuffer();
						for (int i=0; i<length; i++) {
							buffer.append(problems[i].getMessage());
							buffer.append('\n');
						}
						assertEquals("Unexpected problems: "+buffer.toString(), 0, length);
					}
				}
			},
			null);

		// Measures
		int measures = MEASURES_COUNT * 2;
		for (int i = 0; i < measures; i++) {
			runGc();
			startMeasuring();
			parser.createASTs(compilationUnits, new String[0], new ASTRequestor() {/* do nothing*/}, null);
			stopMeasuring();
		}
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Create AST nodes tree for all compilation units in JUnit project.
	 */
	public void testDomAstCreationProjectJLS3() throws JavaModelException {
		tagAsSummary("DOM AST tree for project files (JLS3)", true); // put in fingerprint
		runAstCreation(getProject("org.eclipse.search"));
	}
}
