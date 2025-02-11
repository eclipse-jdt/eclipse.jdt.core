/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.lang.annotation.Target;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypePattern;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.codeassist.impl.Keywords;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.ModuleSourcePathManager;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * A completion engine using a DOM as input (as opposed to {@link CompletionEngine} which
 * relies on lower-level parsing with ECJ)
 */
public class DOMCompletionEngine implements ICompletionEngine {

	private static final String FAKE_IDENTIFIER = new String(RecoveryScanner.FAKE_IDENTIFIER);
	private static final char[] VOID = PrimitiveType.VOID.toString().toCharArray();
	private static final List<char[]> TYPE_KEYWORDS_EXCEPT_VOID = List.of(
			PrimitiveType.BOOLEAN.toString().toCharArray(),
			PrimitiveType.BYTE.toString().toCharArray(),
			PrimitiveType.SHORT.toString().toCharArray(),
			PrimitiveType.INT.toString().toCharArray(),
			PrimitiveType.LONG.toString().toCharArray(),
			PrimitiveType.DOUBLE.toString().toCharArray(),
			PrimitiveType.FLOAT.toString().toCharArray(),
			PrimitiveType.CHAR.toString().toCharArray());

	private final CompletionRequestor requestor;
	private final SearchableEnvironment nameEnvironment;
	private final AssistOptions assistOptions;
	private final SearchPattern pattern;
	private final WorkingCopyOwner workingCopyOwner;
	private final IProgressMonitor monitor;
	private final Map<String, String> settings;
	private final IJavaProject javaProject;

	private final CompletionEngine nestedEngine; // to reuse some utilities

	private CompilationUnit unit;
	private int offset;
	private ITypeRoot modelUnit;

	private ExpectedTypes expectedTypes;
	private String prefix;
	private String qualifiedPrefix;
	private ITypeBinding qualifyingType;
	private ASTNode toComplete;
	private String textContent;
	private ExtendsOrImplementsInfo extendsOrImplementsInfo;

	static class Bindings {
		// those need to be list since the order matters
		// fields must be before methods
		private List<IBinding> others = new ArrayList<>();

		public void add(IBinding binding) {
			if (binding instanceof IMethodBinding methodBinding) {
				if (methodBinding.isConstructor()) {
					return;
				}
				if (this.methods().anyMatch(method -> method.overrides(methodBinding))) {
					return;
				}
				this.others.removeIf(existing -> existing instanceof IMethodBinding existingMethod && methodBinding.overrides(existingMethod));
			}
			if (binding != null) {
				this.others.add(binding);
			}
		}
		public void addAll(Collection<? extends IBinding> bindings) {
			bindings.forEach(this::add);
		}
		public Stream<IBinding> all() {
			return this.others.stream().distinct();
		}
		public Stream<IMethodBinding> methods() {
			return all().filter(IMethodBinding.class::isInstance).map(IMethodBinding.class::cast);
		}
	}

	public DOMCompletionEngine(SearchableEnvironment nameEnvironment, CompletionRequestor requestor, Map<String, String> settings, IJavaProject javaProject, WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor) {
		this.nameEnvironment = nameEnvironment;
		this.requestor = requestor;
		this.settings = settings;
		this.workingCopyOwner = workingCopyOwner;
		this.monitor = monitor;
		this.javaProject = javaProject;

		this.nestedEngine = new CompletionEngine(this.nameEnvironment, this.requestor, settings, javaProject, workingCopyOwner, monitor);

		// TODO also honor assistOptions.checkVisibility!
		// TODO also honor requestor.ignore*
		// TODO sorting/relevance: closest/prefix match should go first
		// ...
		this.assistOptions = new AssistOptions(this.settings);
		this.pattern = new SearchPattern(SearchPattern.R_PREFIX_MATCH |
			(this.assistOptions.camelCaseMatch ? SearchPattern.R_CAMELCASE_MATCH : 0) |
			(this.assistOptions.substringMatch ? SearchPattern.R_SUBSTRING_MATCH : 0) |
			(this.assistOptions.subwordMatch ? SearchPattern.R_SUBWORD_MATCH : 0)) {
			@Override
			public SearchPattern getBlankPattern() { return null; }
		};
	}

	private Collection<? extends IBinding> visibleBindings(ASTNode node) {
		List<IBinding> visibleBindings = new ArrayList<>();

		if (node instanceof MethodDeclaration m) {
			visibleBindings.addAll(((List<VariableDeclaration>) m.parameters()).stream()
					.filter(decl -> !FAKE_IDENTIFIER.equals(decl.getName().toString()))
					.map(VariableDeclaration::resolveBinding).toList());
		}

		if (node instanceof LambdaExpression le) {
			visibleBindings.addAll(((List<VariableDeclaration>) le.parameters()).stream()
					.filter(decl -> !FAKE_IDENTIFIER.equals(decl.getName().toString()))
					.map(VariableDeclaration::resolveBinding).toList());
		}

		if (node instanceof AbstractTypeDeclaration typeDecl) {
			// a different mechanism is used to collect class members, which takes into account accessibility & such
			// so only the type declaration itself is needed here
			visibleBindings.add(typeDecl.resolveBinding());
		}

		if (node instanceof Block block && node.getStartPosition() + node.getLength() > this.offset) {
			for (Statement statement : ((List<Statement>)block.statements())) {
				if (statement.getStartPosition() + statement.getLength() >= this.offset) {
					break;
				}
				if (statement instanceof IfStatement ifStatement && ifStatement.getElseStatement() == null) {
					visibleBindings.addAll(collectTrueFalseBindings(ifStatement.getExpression()).falseBindings());
				} else if (statement instanceof ForStatement forStatement && forStatement.getExpression() != null) {
					visibleBindings.addAll(collectTrueFalseBindings(forStatement.getExpression()).falseBindings());
				} else if (statement instanceof VariableDeclarationStatement variableDeclarationStatement) {
					for (var fragment : (List<VariableDeclarationFragment>)variableDeclarationStatement.fragments()) {
						if (!FAKE_IDENTIFIER.equals(fragment.getName().toString())) {
							visibleBindings.add(fragment.resolveBinding());
						}
					}
				}
			}
		}

		if (node.getParent() instanceof IfStatement ifStatement
				&& (node.getStartPosition() + node.getLength() > this.offset
				|| ifStatement.getThenStatement() == node || ifStatement.getElseStatement() == node)) {
			TrueFalseBindings trueFalseBindings = collectTrueFalseBindings(ifStatement.getExpression());
			if (ifStatement.getThenStatement() == node) {
				visibleBindings.addAll(trueFalseBindings.trueBindings());
			} else {
				visibleBindings.addAll(trueFalseBindings.falseBindings());
			}
		}

		if (node.getParent() instanceof ForStatement forStatement
				&& (node.getStartPosition() + node.getLength() > this.offset
						|| forStatement.getBody() == node)) {
			if (forStatement.getExpression() != null) {
				TrueFalseBindings trueFalseBindings = collectTrueFalseBindings(forStatement.getExpression());
				visibleBindings.addAll(trueFalseBindings.trueBindings());
			}
			if (forStatement.initializers().size() == 1 && forStatement.initializers().get(0) instanceof VariableDeclarationExpression vde) {
				var bindings = ((List<VariableDeclarationFragment>)vde.fragments()).stream()
					.filter(frag -> !FAKE_IDENTIFIER.equals(frag.getName().toString()))
					.map(VariableDeclarationFragment::resolveBinding)
					.toList();
				visibleBindings.addAll(bindings);
			}
		}

		if (node.getParent() instanceof EnhancedForStatement foreachStatement
				&& (node.getStartPosition() + node.getLength() > this.offset
						|| foreachStatement.getBody() == node)) {
			visibleBindings.add(foreachStatement.getParameter().resolveBinding());
		}

		if (node instanceof SwitchStatement switchStatement) {
			int i;
			for (i = 0; i < switchStatement.statements().size(); i++) {
				if (((List<Statement>)switchStatement.statements()).get(i).getStartPosition() >= this.offset) {
					break;
				}
				if (((List<Statement>)switchStatement.statements()).get(i) instanceof SwitchCase switchCase) {
					DOMCompletionUtil.visitChildren(switchCase, ASTNode.TYPE_PATTERN, (TypePattern e) -> {
						visibleBindings.add(e.getPatternVariable().resolveBinding());
					});
				}
			}
		}

		if (node instanceof SwitchExpression switchExpression) {
			int i;
			for (i = 0; i < switchExpression.statements().size(); i++) {
				if (((List<Statement>)switchExpression.statements()).get(i).getStartPosition() >= this.offset) {
					break;
				}
				if (((List<Statement>)switchExpression.statements()).get(i) instanceof SwitchCase switchCase) {
					DOMCompletionUtil.visitChildren(switchCase, ASTNode.TYPE_PATTERN, (TypePattern e) -> {
						visibleBindings.add(e.getPatternVariable().resolveBinding());
					});
				}
			}
		}

		return visibleBindings;
	}

	/**
	 * Represents collections of bindings that might be accessible depending on whether a boolean expression is true or false.
	 *
	 * @param trueBindings the bindings that are accessible when the expression is true
	 * @param falseBindings the bindings that are accessible when the expression is false
	 */
	record TrueFalseBindings(List<IVariableBinding> trueBindings, List<IVariableBinding> falseBindings) {}

	private TrueFalseBindings collectTrueFalseBindings(Expression e) {
		if (e instanceof PrefixExpression prefixExpression && prefixExpression.getOperator() == PrefixExpression.Operator.NOT) {
			TrueFalseBindings notBindings = collectTrueFalseBindings(prefixExpression.getOperand());
			return new TrueFalseBindings(notBindings.falseBindings(), notBindings.trueBindings());
		} else if (e instanceof InfixExpression infixExpression && (infixExpression.getOperator() == InfixExpression.Operator.CONDITIONAL_AND || infixExpression.getOperator() == InfixExpression.Operator.AND )) {
			TrueFalseBindings left = collectTrueFalseBindings(infixExpression.getLeftOperand());
			TrueFalseBindings right = collectTrueFalseBindings(infixExpression.getRightOperand());
			List<IVariableBinding> combined = new ArrayList<>();
			combined.addAll(left.trueBindings());
			combined.addAll(right.trueBindings());
			return new TrueFalseBindings(combined, Collections.emptyList());
		} else if (e instanceof InfixExpression infixExpression && (infixExpression.getOperator() == InfixExpression.Operator.CONDITIONAL_OR || infixExpression.getOperator() == InfixExpression.Operator.OR)) {
			TrueFalseBindings left = collectTrueFalseBindings(infixExpression.getLeftOperand());
			TrueFalseBindings right = collectTrueFalseBindings(infixExpression.getRightOperand());
			List<IVariableBinding> combined = new ArrayList<>();
			combined.addAll(left.falseBindings());
			combined.addAll(right.falseBindings());
			return new TrueFalseBindings(Collections.emptyList(), combined);
		} else {
			List<IVariableBinding> typePatternBindings = new ArrayList<>();
			DOMCompletionUtil.visitChildren(e, ASTNode.TYPE_PATTERN, (TypePattern patt) -> {
				typePatternBindings.add(patt.getPatternVariable().resolveBinding());
			});
			return new TrueFalseBindings(typePatternBindings, Collections.emptyList());
		}
	}

	private Collection<? extends ITypeBinding> visibleTypeBindings(ASTNode node) {
		List<ITypeBinding> visibleBindings = new ArrayList<>();
		if (node instanceof AbstractTypeDeclaration typeDeclaration) {
			visibleBindings.add(typeDeclaration.resolveBinding());
			for (ASTNode bodyDeclaration : (List<ASTNode>)typeDeclaration.bodyDeclarations()) {
				visibleBindings.addAll(visibleTypeBindings(bodyDeclaration));
			}
		}
		if (node instanceof Block block) {
			var bindings = ((List<Statement>) block.statements()).stream()
					.filter(statement -> statement.getStartPosition() < this.offset)
				.filter(TypeDeclaration.class::isInstance)
				.map(TypeDeclaration.class::cast)
					.map(TypeDeclaration::resolveBinding).toList();
			visibleBindings.addAll(bindings);
		}
		return visibleBindings;
	}

	private static CompilationUnit getCompletionAST(ITypeRoot typeRoot, IJavaProject javaProject,
			WorkingCopyOwner workingCopyOwner, int focalPosition) {
		Map<String, String> options = javaProject.getOptions(true);
		// go through AST constructor to convert options to apiLevel
		// but we should probably instead just use the latest Java version
		// supported by the compiler
		ASTParser parser = ASTParser.newParser(new AST(options).apiLevel());
		parser.setWorkingCopyOwner(workingCopyOwner);
		parser.setSource(typeRoot);
		// greedily enable everything assuming the AST will be used extensively for edition
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(options);
		parser.setFocalPosition(focalPosition);
		if (parser.createAST(null) instanceof org.eclipse.jdt.core.dom.CompilationUnit newAST) {
			return newAST;
		}
		return null;
	}

	private static CompilationUnit getCompletionAST(char[] content, char[] unitName, IJavaProject javaProject,
			WorkingCopyOwner workingCopyOwner, int focalPosition) {
		Map<String, String> options = javaProject.getOptions(true);
		// go through AST constructor to convert options to apiLevel
		// but we should probably instead just use the latest Java version
		// supported by the compiler
		ASTParser parser = ASTParser.newParser(new AST(options).apiLevel());
		parser.setWorkingCopyOwner(workingCopyOwner);
		parser.setSource(content);
		parser.setProject(javaProject);
		parser.setUnitName(new String(unitName));
		// greedily enable everything assuming the AST will be used extensively for edition
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(options);
		parser.setFocalPosition(focalPosition);
		if (parser.createAST(null) instanceof org.eclipse.jdt.core.dom.CompilationUnit newAST) {
			return newAST;
		}
		return null;
	}

	@Override
	public void complete(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, int completionPosition, int adjustment, ITypeRoot root) {
		if (this.monitor != null) {
			this.monitor.beginTask(Messages.engine_completing, IProgressMonitor.UNKNOWN);
		}
		this.requestor.beginReporting();

		this.modelUnit = root;
		if (modelUnit != null) {
			try {
				this.textContent = new String(this.modelUnit.getBuffer().getCharacters());
			} catch (JavaModelException e) {
				this.textContent = new String(sourceUnit.getContents());
				ILog.get().error("unable to access buffer for completion", e); //$NON-NLS-1$
			}
			this.offset = completionPosition;
			if (modelUnit instanceof org.eclipse.jdt.internal.core.CompilationUnit modelCU) {
				try {
					this.unit = modelCU.getOrBuildAST(this.workingCopyOwner, completionPosition);
				} catch (JavaModelException e) {
					// do nothing
				}
			}
			if (this.unit == null) {
				this.unit = getCompletionAST(this.modelUnit, root.getJavaProject(), this.workingCopyOwner, completionPosition);
			}
			if (this.unit == null) {
				return;
			}
		} else {
			this.textContent = new String(sourceUnit.getContents());
			this.offset = completionPosition;
			if (this.unit == null) {
				this.unit = getCompletionAST(sourceUnit.getContents(), sourceUnit.getFileName(), this.javaProject, this.workingCopyOwner, completionPosition);
			}
		}


		try {
			Bindings defaultCompletionBindings = new Bindings();
			Bindings specificCompletionBindings = new Bindings();
//			var completionContext = new DOMCompletionContext(this.offset, completeAfter.toCharArray(),
//					computeEnclosingElement(), defaultCompletionBindings::stream, expectedTypes, this.toComplete);
			var completionContext = new DOMCompletionContext(this.unit, this.modelUnit, this.textContent, this.offset, this.assistOptions, defaultCompletionBindings);
			this.nestedEngine.completionToken = completionContext.getToken();
			this.requestor.acceptContext(completionContext);

			this.expectedTypes = completionContext.expectedTypes;
			char[] token = completionContext.getToken();
			String completeAfter = token == null ? new String() : new String(token);
			ASTNode context = completionContext.node;
			this.toComplete = completionContext.node;
			completionContext.setInJavadoc(DOMCompletionUtil.findParent(this.toComplete, new int[] {ASTNode.JAVADOC}) != null);
			ASTNode potentialTagElement = DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.TAG_ELEMENT, ASTNode.MEMBER_REF, ASTNode.METHOD_REF });
			if (potentialTagElement != null) {
				context = potentialTagElement;
			} else if (completionContext.node instanceof SimpleName simpleName) {
				if (simpleName.getParent() instanceof FieldAccess || simpleName.getParent() instanceof MethodInvocation
						|| simpleName.getParent() instanceof VariableDeclaration || simpleName.getParent() instanceof QualifiedName
						|| simpleName.getParent() instanceof SuperFieldAccess || simpleName.getParent() instanceof SingleMemberAnnotation
						|| simpleName.getParent() instanceof ExpressionMethodReference || simpleName.getParent() instanceof TagElement) {
					if (!this.toComplete.getLocationInParent().getId().equals(QualifiedName.QUALIFIER_PROPERTY.getId())) {
						context = this.toComplete.getParent();
					}
				}
				if (simpleName.getParent() instanceof SimpleType simpleType && (simpleType.getParent() instanceof ClassInstanceCreation)) {
					context = simpleName.getParent().getParent();
				}
			} else if (this.toComplete instanceof SimpleType simpleType) {
				if (FAKE_IDENTIFIER.equals(simpleType.getName().toString())) {
					context = this.toComplete.getParent();
				} else if (simpleType.getName() instanceof QualifiedName qualifiedName) {
					context = qualifiedName;
				}
			} else if (this.toComplete instanceof Block block && this.offset == block.getStartPosition()) {
				context = this.toComplete.getParent();
			} else if (this.toComplete instanceof StringLiteral stringLiteral && (this.offset <= stringLiteral.getStartPosition() || stringLiteral.getStartPosition() + stringLiteral.getLength() <= this.offset)) {
				context = stringLiteral.getParent();
			} else if (this.toComplete instanceof VariableDeclaration vd) {
				context = vd.getInitializer();
			} else if (this.toComplete instanceof QualifiedName && this.toComplete.getParent() instanceof TagElement tagElement) {
				context = tagElement;
			}
			this.prefix = token == null ? new String() : new String(token);
			if (this.toComplete instanceof MethodInvocation methodInvocation && this.offset == (methodInvocation.getName().getStartPosition() + methodInvocation.getName().getLength()) + 1) {
				// myMethod(|)
				this.prefix = methodInvocation.getName().toString();
			}
			this.qualifiedPrefix = this.prefix;
			if (this.toComplete instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete != null && this.toComplete.getParent() instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete instanceof SimpleType simpleType && simpleType.getName() instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete instanceof TextElement textElement && context instanceof TagElement) {
				String packageName = textElement.getText().trim();
				if (!packageName.isEmpty()) {
					if (packageName.charAt(packageName.length() - 1) == '.') {
						packageName = packageName.substring(0, packageName.length() - 1);
					}
					this.qualifiedPrefix = packageName;
				}
			} else if (this.toComplete instanceof ThisExpression thisExpression) {
				if (thisExpression.getQualifier() != null) {
					this.qualifiedPrefix = thisExpression.getQualifier().toString();
				}
			}
			this.extendsOrImplementsInfo = isInExtendsOrImplements(context);
			// some flags to controls different applicable completion search strategies
			boolean suggestDefaultCompletions = true;

			checkCancelled();

			if (context instanceof StringLiteral || context instanceof TextBlock || context instanceof Comment || context instanceof Javadoc || context instanceof NumberLiteral) {
				return;
			}
			if (context instanceof FieldAccess fieldAccess) {
				Expression fieldAccessExpr = fieldAccess.getExpression();
				ITypeBinding fieldAccessType = fieldAccessExpr.resolveTypeBinding();
				if (fieldAccessType != null) {
					if (!fieldAccessType.isRecovered()) {
						processMembers(fieldAccess, fieldAccessExpr.resolveTypeBinding(), specificCompletionBindings, false);
						publishFromScope(specificCompletionBindings);
					} else if (fieldAccessExpr instanceof MethodInvocation method &&
								this.unit.findDeclaringNode(method.resolveMethodBinding()) instanceof MethodDeclaration decl) {
						completeMissingType(decl.getReturnType2());
					}
				} else if (DOMCompletionUtil.findParent(fieldAccessExpr, new int[]{ ASTNode.METHOD_INVOCATION }) == null) {
					String packageName = ""; //$NON-NLS-1$
					if (fieldAccess.getExpression() instanceof FieldAccess parentFieldAccess
							&& parentFieldAccess.getName().resolveBinding() instanceof IPackageBinding packageBinding) {
						packageName = packageBinding.getName();
					} else if (fieldAccess.getExpression() instanceof SimpleName name
							&& name.resolveBinding() instanceof IPackageBinding packageBinding) {
						packageName = packageBinding.getName();
					}
					suggestPackages(fieldAccess);
					suggestTypesInPackage(packageName);
				}
				suggestDefaultCompletions = false;
			}
			if (context instanceof MethodInvocation invocation) {
				if (this.offset <= invocation.getName().getStartPosition() + invocation.getName().getLength()) {
					Expression expression = invocation.getExpression();
					if (expression == null) {
						return;
					}
					// complete name
					ITypeBinding type = expression.resolveTypeBinding();
					if (type != null) {
						processMembers(expression, type, specificCompletionBindings, false);
						specificCompletionBindings.all()
							.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
							.filter(IMethodBinding.class::isInstance)
							.map(binding -> toProposal(binding))
							.forEach(this.requestor::accept);
					}
					suggestDefaultCompletions = false;
				} else if (invocation.getStartPosition() + invocation.getLength() <= this.offset && this.prefix.isEmpty()) {
					// handle `myMethod().|`
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if (methodBinding != null) {
						ITypeBinding returnType = methodBinding.getReturnType();
						processMembers(invocation, returnType, specificCompletionBindings, false);
						specificCompletionBindings.all()
							.map(binding -> toProposal(binding))
							.forEach(this.requestor::accept);
					}
					suggestDefaultCompletions = false;
				} else {
					// inside parens, but not on any specific argument
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if (methodBinding == null && this.toComplete == invocation) {
						// myMethod(|), where myMethod does not exist
						suggestDefaultCompletions = false;
					} else if (this.toComplete == invocation) {
						for (ITypeBinding param : this.expectedTypes.getExpectedTypes()) {
							IMethodBinding potentialLambda = param.getFunctionalInterfaceMethod();
							if (potentialLambda != null) {
								this.requestor.accept(createLambdaExpressionProposal(potentialLambda));
							}
						}
						CompletionProposal proposal = toProposal(methodBinding);
						proposal.setCompletion(new char[0]);
						proposal.setReplaceRange(this.offset, this.offset);
						proposal.setTokenRange(this.offset, this.offset);
						this.requestor.accept(proposal);
						suggestDefaultCompletions = false;
					}
				}
			}
			if (context instanceof VariableDeclaration declaration) {
				if (declaration.getName() == this.toComplete) {
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof ModuleDeclaration mod) {
				findModules(this.prefix.toCharArray(), this.javaProject, this.assistOptions, Set.of(mod.getName().toString()));
			}
			if (context instanceof SimpleName) {
				if (context.getParent() instanceof SimpleType simpleType
						&& simpleType.getParent() instanceof FieldDeclaration fieldDeclaration
						&& fieldDeclaration.getParent() instanceof AbstractTypeDeclaration typeDecl) {
					// eg.
					// public class Foo {
					//     ba|
					// }
					ITypeBinding typeDeclBinding = typeDecl.resolveBinding();
					findOverridableMethods(typeDeclBinding, this.javaProject, context);
					suggestTypeKeywords(true);
					if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
						findTypes(this.prefix, IJavaSearchConstants.TYPE, null)
							// don't care about annotations
							.filter(type -> {
								try {
									return !type.isAnnotation();
								} catch (JavaModelException e) {
									return true;
								}
							})
							.filter(type -> {
								return defaultCompletionBindings.all().map(typeBinding -> typeBinding.getJavaElement()).noneMatch(elt -> type.equals(elt));
							})
							.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
									type.getElementName().toCharArray()))
							.filter(type -> {
								return filterBasedOnExtendsOrImplementsInfo(type, this.extendsOrImplementsInfo);
							})
							.map(this::toProposal).forEach(this.requestor::accept);
					}
					if (!this.requestor.isIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION)) {
						int cursorStart = this.offset - this.prefix.length() - 1;
						while (cursorStart > 0 && Character.isWhitespace(this.textContent.charAt(cursorStart))) {
							cursorStart--;
						}
						int cursorEnd = cursorStart;
						while (cursorEnd > 0 && Character.isJavaIdentifierPart(this.textContent.charAt(cursorEnd - 1))) {
							cursorEnd--;
						}
						boolean suggest = true;
						if (cursorStart != cursorEnd) {
							String potentialModifier = this.textContent.substring(cursorEnd, cursorStart + 1);
							if (DOMCompletionUtil.isJavaFieldOrMethodModifier(potentialModifier)) {
								suggest = false;
							}
						}
						if (suggest) {
							this.requestor.accept(toNewMethodProposal(typeDeclBinding, this.prefix));
						}
					}
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof MarkerAnnotation) {
					completeMarkerAnnotation(completeAfter);
					suggestDefaultCompletions = false;
				}
				if (context.getLocationInParent() == MemberValuePair.NAME_PROPERTY && context.getParent() instanceof MemberValuePair memberValuePair) {
					Set<String> names = new HashSet<>();
					if (memberValuePair.getParent() instanceof NormalAnnotation normalAnnotation) {
						for (Object o : normalAnnotation.values()) {
							if (o instanceof MemberValuePair other && other != memberValuePair) {
								names.add(other.getName().getIdentifier());
							}
						}
					}
					Arrays.stream(((Annotation)memberValuePair.getParent()).resolveTypeBinding().getDeclaredMethods()) //
						.filter(this::isVisible) //
						.filter(method -> !names.contains(method.getName()))
						.map(this::toAnnotationAttributeRefProposal) //
						.forEach(this.requestor::accept);
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof MemberValuePair) {
					// TODO: most of the time a constant value is expected,
					// however if an enum is expected, we can build out the completion for that
					suggestDefaultCompletions = false;
				}
				if ((context.getLocationInParent() == SwitchCase.EXPRESSIONS2_PROPERTY || context.getLocationInParent() == SwitchCase.EXPRESSION_PROPERTY) && context.getParent() instanceof SwitchCase switchCase) {
					// find the enum if there is one
					ITypeBinding firstEnumType = null;
					for (ITypeBinding expectedType : completionContext.expectedTypes.getExpectedTypes()) {
						if (expectedType.isEnum()) {
							firstEnumType = expectedType;
							break;
						}
					}
					if (firstEnumType != null) {
						Stream.of(firstEnumType.getDeclaredFields())
							.filter(IVariableBinding::isEnumConstant)
							.filter(constant -> this.pattern.matchesName(this.prefix.toCharArray(), constant.getName().toCharArray()))
							.map(this::toProposal)
							.forEach(this.requestor::accept);
					} else {
						// if there is no expected enum type, use the default completion.
						// perhaps there is a suitable int or String constant
						Bindings caseBindings = new Bindings();
						scrapeAccessibleBindings(caseBindings);
						caseBindings.all()
							.filter(IVariableBinding.class::isInstance)
							.map(IVariableBinding.class::cast)
							.filter(varBinding -> {
								return ((varBinding.getModifiers() & Flags.AccFinal) != 0);
							})
							.filter(varBinding -> this.pattern.matchesName(this.prefix.toCharArray(), varBinding.getName().toCharArray()))
							.filter(varBinding -> {
								for (ITypeBinding expectedType : completionContext.expectedTypes.getExpectedTypes()) {
									if (varBinding.getType().getKey().equals(expectedType.getKey())) {
										return true;
									}
								}
								return false;
							})
							.map(this::toProposal)
							.forEach(proposal -> {
								// Seems like the `R_FINAL` constant is only added when completing switch statements:
								// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195346
								proposal.setRelevance(proposal.getRelevance() + RelevanceConstants.R_FINAL);
								this.requestor.accept(proposal);
							});
					}
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof MethodDeclaration) {
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof SimpleType simpleType && simpleType.getParent() instanceof MethodDeclaration
						&& simpleType.getLocationInParent().getId().equals(MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY.getId())) {
					findTypes(completeAfter, null)
							.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
									type.getElementName().toCharArray()))
							// ideally we should filter out all classes that don't descend from Throwable
							// however JDT doesn't do this yet from what I can tell
							.filter(type -> {
								try {
									return !type.isAnnotation() && !type.isInterface();
								} catch (JavaModelException e) {
									return true;
								}
							})
							.map(this::toProposal).forEach(this.requestor::accept);
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof AnnotationTypeMemberDeclaration) {
					suggestDefaultCompletions = false;
				}
				if (context.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY && context.getParent() instanceof QualifiedName) {
					IBinding incorrectBinding = ((SimpleName) context).resolveBinding();
					if (incorrectBinding != null) {
						// eg.
						// void myMethod() {
						//   String myVariable = "hello, mom";
						//   myVariable.|
						//   Object myObj = null;
						// }
						// It thinks that our variable is a package or some other type. We know that it's a variable.
						// Search the scope for the right binding
						Bindings localBindings = new Bindings();
						scrapeAccessibleBindings(localBindings);
						Optional<IVariableBinding> realBinding = localBindings.all() //
								.filter(IVariableBinding.class::isInstance)
								.map(IVariableBinding.class::cast)
								.filter(varBind -> varBind.getName().equals(incorrectBinding.getName()))
								.findFirst();
						if (realBinding.isPresent()) {
							processMembers(context, realBinding.get().getType(), specificCompletionBindings, false);
							this.prefix = ""; //$NON-NLS-1$
							publishFromScope(specificCompletionBindings);
							suggestDefaultCompletions = false;
						}
					}
				}
				if (context.getParent() instanceof ImportDeclaration importDeclaration
						&& context.getAST().apiLevel() >= AST.JLS23
						&& this.javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true).equals(JavaCore.ENABLED)) {
					for (Modifier modifier : (List<Modifier>)importDeclaration.modifiers()) {
						if (modifier.getKeyword() == ModifierKeyword.MODULE_KEYWORD) {
							findModules(this.qualifiedPrefix.toCharArray(), this.javaProject, this.assistOptions, Collections.emptySet());
							suggestDefaultCompletions = false;
							break;
						}
					}
				}
			}
			if (context instanceof AbstractTypeDeclaration typeDecl) {
				if (this.textContent != null) {
					if (this.extendsOrImplementsInfo != null) {
						// keyword present, but no simple or qualified name
						// class MyClass implements | {
						Bindings typeCompletionBindings = new Bindings();
						topLevelTypes(typeCompletionBindings);
						typeCompletionBindings.all()
							.filter(ITypeBinding.class::isInstance)
							.map(typeBinding -> (IType)typeBinding.getJavaElement())
							.filter(type -> filterBasedOnExtendsOrImplementsInfo(type, this.extendsOrImplementsInfo))
							.map(this::toProposal)
							.forEach(this.requestor::accept);
					} else {
						int nameEndOffset = typeDecl.getName().getStartPosition() + typeDecl.getName().getLength();
						int bodyStart = nameEndOffset;
						while (bodyStart < this.textContent.length() && this.textContent.charAt(bodyStart) != '{') {
							bodyStart++;
						}
						int prefixCursor = this.offset;
						while (prefixCursor > 0 && !Character.isWhitespace(this.textContent.charAt(prefixCursor - 1))) {
							prefixCursor--;
						}
						this.prefix = this.textContent.substring(prefixCursor, this.offset);
						if (nameEndOffset < this.offset && this.offset <= bodyStart) {
							String extendsOrImplementsContent = this.textContent.substring(nameEndOffset, this.offset);
							int implementsOffset = extendsOrImplementsContent.indexOf("implements");
							int extendsOffset = extendsOrImplementsContent.indexOf("extends");
							if (implementsOffset < 0 && extendsOffset < 0) {
								// public class Foo | {
								//
								// }
								boolean isInterface = typeDecl instanceof TypeDeclaration realTypeDecl && realTypeDecl.isInterface();
								boolean isEnumOrRecord = typeDecl instanceof RecordDeclaration || typeDecl instanceof EnumDeclaration;
								if (!isEnumOrRecord && CharOperation.prefixEquals(this.prefix.toCharArray(), Keywords.EXTENDS)) {
									this.requestor.accept(createKeywordProposal(Keywords.EXTENDS, this.offset, this.offset));
								}
								if (!isInterface && CharOperation.prefixEquals(this.prefix.toCharArray(), Keywords.IMPLEMENTS)) {
									this.requestor.accept(createKeywordProposal(Keywords.IMPLEMENTS, this.offset, this.offset));
								}
							} else if (implementsOffset < 0
									&& (Character.isWhitespace(this.textContent.charAt(this.offset - 1)) || this.textContent.charAt(this.offset - 1) == ',')) {
								// public class Foo extends Bar, Baz, | {
								//
								// }
								this.requestor.accept(createKeywordProposal(Keywords.IMPLEMENTS, this.offset, this.offset));
							}
						} else if (bodyStart < this.offset) {
							// public class Foo {
							//     |
							// }
							ITypeBinding typeDeclBinding = typeDecl.resolveBinding();
							findOverridableMethods(typeDeclBinding, this.javaProject, null);
						}
					}
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof QualifiedName qualifiedName) {
				ImportDeclaration importDecl = (ImportDeclaration)DOMCompletionUtil.findParent(context, new int[] { ASTNode.IMPORT_DECLARATION });
				if (isParameterInNonParameterizedType(context)) {
					// do not complete
					suggestDefaultCompletions = false;
				} else if (importDecl != null) {
					if(importDecl.getAST().apiLevel() >= AST.JLS23
						&& this.javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true).equals(JavaCore.ENABLED)
						&& importDecl.modifiers().stream().anyMatch(node -> node instanceof Modifier modifier && modifier.getKeyword() == ModifierKeyword.MODULE_KEYWORD)) {
						findModules((this.qualifiedPrefix + "." + this.prefix).toCharArray(), this.javaProject, this.assistOptions, Collections.emptySet()); //$NON-NLS-1$
						suggestDefaultCompletions = false;
					} else {
						suggestPackages(context);
						suggestTypesInPackage(qualifiedName.toString());
						suggestTypesInPackage(qualifiedName.getQualifier().toString());
						if (importDecl.isStatic() &&
							qualifiedName.getQualifier().resolveBinding() instanceof ITypeBinding type) {
							Stream.of(type.getDeclaredFields(), type.getDeclaredMethods(), type.getDeclaredTypes())
								.flatMap(Arrays::stream) //
								.filter(binding -> Modifier.isStatic(binding.getModifiers())) //
								.map(this::toProposal) //
								.forEach(this.requestor::accept);
						}
						suggestDefaultCompletions = false;
					}
				} else {
					IBinding qualifiedNameBinding = qualifiedName.getQualifier().resolveBinding();
					if (qualifiedNameBinding instanceof ITypeBinding qualifierTypeBinding && !qualifierTypeBinding.isRecovered()) {

						boolean isTypeInVariableDeclaration = isTypeInVariableDeclaration(context);

						processMembers(qualifiedName, qualifierTypeBinding, specificCompletionBindings, true);
						if (this.extendsOrImplementsInfo == null && !isTypeInVariableDeclaration) {
							this.qualifyingType = qualifierTypeBinding;
							publishFromScope(specificCompletionBindings);
						} else {
							specificCompletionBindings.all() //
								.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
								.filter(ITypeBinding.class::isInstance)
								.filter(type -> filterBasedOnExtendsOrImplementsInfo((IType)type.getJavaElement(), this.extendsOrImplementsInfo))
								.map(binding -> toProposal(binding))
								.forEach(this.requestor::accept);
						}
						int startPos = this.offset;
						int endPos = this.offset;
						if ((qualifiedName.getName().getFlags() & ASTNode.MALFORMED) != 0) {
							startPos = qualifiedName.getName().getStartPosition();
							endPos = startPos + qualifiedName.getName().getLength();
						}
						if (!(this.toComplete instanceof Type)) {
							AbstractTypeDeclaration parentTypeDeclaration = DOMCompletionUtil.findParentTypeDeclaration(context);
							if (parentTypeDeclaration != null) {
								ITypeBinding currentTypeBinding = parentTypeDeclaration.resolveBinding();
								if (currentTypeBinding.isSubTypeCompatible(qualifierTypeBinding)) {
									if (!isFailedMatch(this.prefix.toCharArray(), Keywords.THIS)) {
										this.requestor.accept(createKeywordProposal(Keywords.THIS, startPos, endPos));
									}
									if (!isFailedMatch(this.prefix.toCharArray(), Keywords.SUPER)) {
										this.requestor.accept(createKeywordProposal(Keywords.SUPER, startPos, endPos));
									}
								}
								if (!isFailedMatch(this.prefix.toCharArray(), Keywords.CLASS)) {
									this.requestor.accept(createClassKeywordProposal(qualifierTypeBinding, startPos, endPos));
								}
							}
						}

						suggestDefaultCompletions = false;
					} else if (qualifiedNameBinding instanceof IPackageBinding qualifierPackageBinding) {
						if (!qualifierPackageBinding.isRecovered()) {
							// start of a known package
							suggestPackages(null);
							// suggests types in the package
							suggestTypesInPackage(qualifierPackageBinding.getName());
							suggestDefaultCompletions = false;
						} else {
							// likely the start of an incomplete field/method access
							Bindings tempScope = new Bindings();
							scrapeAccessibleBindings(tempScope);
							Optional<ITypeBinding> potentialBinding = tempScope.all() //
									.filter(binding -> {
										IJavaElement elt = binding.getJavaElement();
										if (elt == null) {
											return false;
										}
										return elt.getElementName().equals(qualifiedName.getQualifier().toString());
									}) //
									.map(binding -> {
										if (binding instanceof IVariableBinding variableBinding) {
											return variableBinding.getType();
										} else if (binding instanceof ITypeBinding typeBinding) {
											return typeBinding;
										}
										throw new IllegalStateException(
												"method, type var, etc. are likely not interpreted as a package"); //$NON-NLS-1$
									}) //
									.map(ITypeBinding.class::cast) //
									.findFirst();
							if (potentialBinding.isPresent()) {
								processMembers(qualifiedName, potentialBinding.get(), specificCompletionBindings,
										false);
								if (this.extendsOrImplementsInfo == null) {
									publishFromScope(specificCompletionBindings);
								} else {
									specificCompletionBindings.all() //
										.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
										.filter(ITypeBinding.class::isInstance)
										.filter(type -> filterBasedOnExtendsOrImplementsInfo((IType)type.getJavaElement(), this.extendsOrImplementsInfo))
										.map(binding -> toProposal(binding))
										.forEach(this.requestor::accept);
								}
								suggestDefaultCompletions = false;
							} else {
								// maybe it is actually a package?
								if (shouldSuggestPackages(context)) {
									suggestPackages(context);
								}
								// suggests types in the package
								suggestTypesInPackage(qualifierPackageBinding.getName());
								suggestDefaultCompletions = false;
							}
						}
					} else if (qualifiedNameBinding instanceof IVariableBinding variableBinding) {
						ITypeBinding typeBinding = variableBinding.getType();
						if ((typeBinding == null || typeBinding.isRecovered()) && unit.findDeclaringNode(variableBinding) instanceof VariableDeclaration decl) {
							Type type = null;
							if (decl instanceof SingleVariableDeclaration single) {
								type = single.getType();
							} else if (decl instanceof VariableDeclarationFragment fragment) {
								if (fragment.getParent() instanceof FieldDeclaration field) {
									type = field.getType();
								} else if (fragment.getParent() instanceof VariableDeclarationExpression expr) {
									type = expr.getType();
								} else if (fragment.getParent() instanceof VariableDeclarationStatement stmt) {
									type = stmt.getType();
								}
							}
							if (type != null) {
								typeBinding = type.resolveBinding();
							}
							if (typeBinding == null || typeBinding.isRecovered()) {
								completeMissingType(type);
							}
						}
						processMembers(qualifiedName, typeBinding, specificCompletionBindings, false);
						publishFromScope(specificCompletionBindings);
						suggestDefaultCompletions = false;
					} else {
						// UnimportedType.|
						List<IType> foundTypes = findTypes(qualifiedName.getQualifier().toString(), null).toList();
						// HACK: We requested exact matches from the search engine but some results aren't exact
						foundTypes = foundTypes.stream().filter(type -> type.getElementName().equals(qualifiedName.getQualifier().toString())).toList();
						if (!foundTypes.isEmpty()) {
							IType firstType = foundTypes.get(0);
							ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
							parser.setProject(this.javaProject);
							IBinding[] descendantBindings = parser.createBindings(new IType[] { firstType }, new NullProgressMonitor());
							if (descendantBindings.length == 1) {
								ITypeBinding qualifierTypeBinding = (ITypeBinding)descendantBindings[0];
								processMembers(qualifiedName, qualifierTypeBinding, specificCompletionBindings, true);
								publishFromScope(specificCompletionBindings);
								int startPos = this.offset;
								int endPos = this.offset;
								if ((qualifiedName.getName().getFlags() & ASTNode.MALFORMED) != 0) {
									startPos = qualifiedName.getName().getStartPosition();
									endPos = startPos + qualifiedName.getName().getLength();
								}
								if (!(this.toComplete instanceof Type) && !isFailedMatch(this.prefix.toCharArray(), Keywords.CLASS)) {
									this.requestor.accept(createClassKeywordProposal(qualifierTypeBinding, startPos, endPos));
								}
							}
						}
						suggestDefaultCompletions = false;
					}
				}
			}
			if (context instanceof SuperFieldAccess superFieldAccess) {
				ITypeBinding superTypeBinding = superFieldAccess.resolveTypeBinding();
				processMembers(superFieldAccess, superTypeBinding, specificCompletionBindings, false);
				publishFromScope(specificCompletionBindings);
				suggestDefaultCompletions = false;
			}
			if (context instanceof MarkerAnnotation) {
				completeMarkerAnnotation(completeAfter);
				suggestDefaultCompletions = false;
			}
			if (context instanceof SingleMemberAnnotation singleMemberAnnotation) {
				if (singleMemberAnnotation.getTypeName().getStartPosition() + singleMemberAnnotation.getTypeName().getLength() > this.offset) {
					completeMarkerAnnotation(completeAfter);
				} else if (!this.requestor.isIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF)) {
					completeAnnotationParams(singleMemberAnnotation, Collections.emptySet(), specificCompletionBindings);
				}
				suggestDefaultCompletions = false;
			}
			if (context instanceof NormalAnnotation normalAnnotation) {
				if (normalAnnotation.getTypeName().getStartPosition() + normalAnnotation.getTypeName().getLength() > this.offset) {
					completeMarkerAnnotation(completeAfter);
				} else if (!this.requestor.isIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF)) {
					completeNormalAnnotationParams(normalAnnotation, specificCompletionBindings);
				}
				suggestDefaultCompletions = false;
			}
			if (context instanceof MethodDeclaration methodDeclaration) {
				if (this.offset < methodDeclaration.getName().getStartPosition()) {
					completeMethodModifiers(methodDeclaration);
					// return type: suggest types from current CU
					if (methodDeclaration.getReturnType2() == null) {
						ASTNode current = this.toComplete;
						while (current != null) {
							specificCompletionBindings.addAll(visibleTypeBindings(current));
							current = current.getParent();
						}
						publishFromScope(specificCompletionBindings);
					}
					suggestDefaultCompletions = false;
				} else if (methodDeclaration.getBody() == null || (methodDeclaration.getBody() != null && this.offset <= methodDeclaration.getBody().getStartPosition())) {
					completeThrowsClause(methodDeclaration, specificCompletionBindings);
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof ClassInstanceCreation) {
				if (this.expectedTypes.getExpectedTypes() != null && !this.expectedTypes.getExpectedTypes().isEmpty() && !this.expectedTypes.getExpectedTypes().get(0).isRecovered()) {
					completeConstructor(this.expectedTypes.getExpectedTypes().get(0), context, this.javaProject);
				} else {
					if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF) && !this.requestor.isIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION)) {
						String packageName = "";//$NON-NLS-1$
						PackageDeclaration packageDecl = this.unit.getPackage();
						if (packageDecl != null) {
							packageName = packageDecl.getName().toString();
						}
						this.findTypes(this.prefix, IJavaSearchConstants.TYPE, packageName)
								.filter(type -> {
									try {
										return !type.isAnnotation();
									} catch (JavaModelException e) {
										return true;
									}
								}) //
								.flatMap(type -> {
									if (this.prefix.isEmpty()) {
										return Stream.of(toProposal(type));
									} else {
										return toConstructorProposals(type, this.toComplete, false).stream();
									}
								}) //
								.forEach(this.requestor::accept);
					}
				}
				suggestDefaultCompletions = false;
			}
			if (context instanceof Javadoc) {
				suggestDefaultCompletions = false;
			}
			if (context instanceof ExpressionMethodReference emr) {
				ITypeBinding typeBinding = emr.getExpression().resolveTypeBinding();
				if (typeBinding != null && !this.requestor.isIgnored(CompletionProposal.METHOD_NAME_REFERENCE)) {
					processMembers(emr, typeBinding, specificCompletionBindings, false);
					specificCompletionBindings.methods() //
							.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
							.map(this::toProposal) //
							.forEach(this.requestor::accept);
				}
				suggestDefaultCompletions = false;
			}
			if (context instanceof TagElement tagElement) {
				boolean isTagName = true;
				ASTNode cursor = this.toComplete;
				while (cursor != null && cursor != tagElement) {
					int index = tagElement.fragments().indexOf(cursor);
					if (index >= 0) {
						isTagName = false;
						break;
					}
					cursor = cursor.getParent();
				}

				if (isTagName || (tagElement.getTagName() != null && tagElement.getTagName().length() == 1) || (tagElement.getTagName() != null && this.offset == (tagElement.getStartPosition() + tagElement.getTagName().length()))) {
					completeJavadocBlockTags(tagElement);
					completeJavadocInlineTags(tagElement);
					suggestDefaultCompletions = false;
				} else {
					if (tagElement.getTagName() != null) {
						Javadoc javadoc = (Javadoc)DOMCompletionUtil.findParent(tagElement, new int[] { ASTNode.JAVADOC });
						switch (tagElement.getTagName()) {
							case TagElement.TAG_PARAM: {

								int start = tagElement.getStartPosition() + TagElement.TAG_PARAM.length() + 1;
								int endPos = start;
								if (this.textContent != null) {
									while (endPos < this.textContent.length() && !Character.isWhitespace(this.textContent.charAt(endPos))) {
										endPos++;
									}
								}
								String paramPrefix = this.textContent.substring(start, endPos);

								if (javadoc.getParent() instanceof MethodDeclaration methodDecl) {
									Set<String> alreadyDocumentedParameters = findAlreadyDocumentedParameters(javadoc);
									IMethodBinding methodBinding = methodDecl.resolveBinding();
									Stream.of(methodBinding.getParameterNames()) //
										.filter(name -> !alreadyDocumentedParameters.contains(name)) //
										.filter(name -> this.pattern.matchesName(paramPrefix.toCharArray(), name.toCharArray())) //
										.map(paramName -> toAtParamProposal(paramName, tagElement)) //
										.forEach(this.requestor::accept);
									Stream.of(methodBinding.getTypeParameters()) //
										.map(typeParam -> "<" + typeParam.getName() + ">") //$NON-NLS-1$ //$NON-NLS-2$
										.filter(name -> !alreadyDocumentedParameters.contains(name)) //
										.filter(name -> this.pattern.matchesName(paramPrefix.toCharArray(), name.toCharArray())) //
										.map(paramName -> toAtParamProposal(paramName, tagElement)) //
										.forEach(this.requestor::accept);
								} else {
									if (javadoc.getParent() instanceof AbstractTypeDeclaration typeDecl) {
										Set<String> alreadyDocumentedParameters = findAlreadyDocumentedParameters(javadoc);
										ITypeBinding typeBinding = typeDecl.resolveBinding().getTypeDeclaration();
										Stream.of(typeBinding.getTypeParameters())
											.map(typeParam -> "<" + typeParam.getName() + ">") //$NON-NLS-1$ //$NON-NLS-2$
											.filter(name -> !alreadyDocumentedParameters.contains(name)) //
											.filter(name -> this.pattern.matchesName(paramPrefix.toCharArray(), name.toCharArray())) //
											.map(name -> toAtParamProposal(name, tagElement))
											.forEach(this.requestor::accept);
									}
								}
								suggestDefaultCompletions = false;
								break;
							}
							case TagElement.TAG_LINK:
							case TagElement.TAG_SEE: {

								if (this.qualifiedPrefix.indexOf('#') >= 0) {
									// eg. MyClass#|
									// eg. #|
									// Eclipse expects these to be TextElement instead of MemberRef
									String classToComplete = this.qualifiedPrefix.substring(0, this.qualifiedPrefix.indexOf('#'));
									if (classToComplete.isEmpty()) {
										// use parent type
										ITypeBinding typeBinding = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding();
										Bindings javadocScope = new Bindings();
										processMembers(this.toComplete, typeBinding, javadocScope, false);
										publishFromScope(javadocScope);
										suggestAccessibleConstructorsForType(typeBinding);
									} else {
										String packageName = classToComplete.lastIndexOf('.') < 0 ? null : classToComplete.substring(0, classToComplete.lastIndexOf('.'));
										if (packageName != null) {
											classToComplete = classToComplete.substring(classToComplete.lastIndexOf('.') + 1);
										} else {
											CompilationUnit cu = (CompilationUnit)DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.COMPILATION_UNIT });
											if (cu.getPackage() != null) {
												packageName = cu.getPackage().getName().toString();
											} else {
												packageName = ""; //$NON-NLS-1$
											}
										}
										List<IType> potentialTypes = findTypes(classToComplete, IJavaSearchConstants.TYPE, packageName).toList();
										List<IType> sourceTypes = potentialTypes.stream().filter(type -> type instanceof SourceType).toList();
										if (!potentialTypes.isEmpty()) {
											IType typeToComplete;
											if (potentialTypes.size() > 1 && !sourceTypes.isEmpty()) {
												typeToComplete = sourceTypes.get(0);
											} else {
												typeToComplete = potentialTypes.get(0);
											}
											ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
											parser.setProject(this.javaProject);
											IBinding[] createdBindings = parser.createBindings(new IType[] { typeToComplete }, new NullProgressMonitor());
											if (createdBindings.length > 0 && createdBindings[0] instanceof ITypeBinding typeBinding) {
												Bindings javadocScope = new Bindings();
												processMembers(this.toComplete, typeBinding, javadocScope, false);
												publishFromScope(javadocScope);
												suggestAccessibleConstructorsForType(typeBinding);
											}
										}
									}
									suggestDefaultCompletions = false;
								} else {
									int endPos = this.offset, startPos = endPos;
									if (this.textContent != null) {
										while (startPos > 0 && !Character.isWhitespace(this.textContent.charAt(startPos - 1))) {
											startPos--;
										}
									}

									String paramPrefix = this.textContent.substring(startPos, endPos);
									if (paramPrefix.indexOf('/') >= 0) {
										// TODO: only complete types that are in the specified module
										suggestDefaultCompletions = false;
									} else {
										// local types are suggested first
										String currentPackage = ""; //$NON-NLS-1$
										CompilationUnit cuNode = (CompilationUnit) DOMCompletionUtil.findParent(tagElement, new int[] { ASTNode.COMPILATION_UNIT });
										if (cuNode.getPackage() != null) {
											currentPackage = cuNode.getPackage().getName().toString();
										}
										final String finalizedCurrentPackage = currentPackage;

										Bindings localTypeBindings = new Bindings();
										scrapeAccessibleBindings(localTypeBindings);
										localTypeBindings.all() //
											.filter(binding -> binding instanceof ITypeBinding) //
											.filter(type -> (this.qualifiedPrefix.equals(this.prefix) || this.qualifiedPrefix.equals(finalizedCurrentPackage)) && this.pattern.matchesName(this.prefix.toCharArray(), type.getName().toCharArray()))
											.map(this::toProposal).forEach(this.requestor::accept);

										findTypes(completeAfter, IJavaSearchConstants.TYPE, completeAfter.equals(this.qualifiedPrefix) ? null : this.qualifiedPrefix)
											.filter(type -> {
												return localTypeBindings.all().map(typeBinding -> typeBinding.getJavaElement()).noneMatch(elt -> type.equals(elt));
											})
											.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(), type.getElementName().toCharArray()))
											.map(this::toProposal).forEach(this.requestor::accept);

										suggestDefaultCompletions = false;
									}
								}
								break;
							}
						}
					} else {
						// the tag name is null, so this is probably a broken conversion
						suggestDefaultCompletions = false;
					}
				}
			}
			if (context instanceof ImportDeclaration) {
				if (context.getAST().apiLevel() >= AST.JLS23
						&& this.javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true).equals(JavaCore.ENABLED)) {
					findModules(this.qualifiedPrefix.toCharArray(), this.javaProject, this.assistOptions, Collections.emptySet());
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof CompilationUnit unit &&
				CharOperation.prefixEquals(completionContext.getToken(), Keywords.PACKAGE) &&
				unit.getPackage() == null &&
				this.offset <= ((Collection<ASTNode>)unit.imports()).stream().mapToInt(ASTNode::getStartPosition).filter(n -> n >= 0).min().orElse(Integer.MAX_VALUE) &&
				this.offset <= ((Collection<ASTNode>)unit.types()).stream().mapToInt(ASTNode::getStartPosition).filter(n -> n >= 0).min().orElse(Integer.MAX_VALUE)) {
				this.requestor.accept(createKeywordProposal(Keywords.PACKAGE, completionContext.getTokenStart(), completionContext.getTokenEnd()));
			}
			if (context instanceof MethodRef methodRef) {
				JavadocMethodReferenceParseState state = JavadocMethodReferenceParseState.BEFORE_IDENTIFIER;
				int minNumParams = 0;
				int cursor = methodRef.getName().getStartPosition() + methodRef.getName().toString().length() + 1;
				while (cursor < this.offset) {
					if (this.textContent.charAt(cursor) == ',') {
						minNumParams++;
						state = JavadocMethodReferenceParseState.BEFORE_IDENTIFIER;
					} else {
						switch (state) {
							case BEFORE_IDENTIFIER: {
								char cursorCharacter = this.textContent.charAt(cursor);
								if (!Character.isWhitespace(cursorCharacter) && cursorCharacter != ')') {
									if (minNumParams == 0) {
										minNumParams++;
									}
									state = JavadocMethodReferenceParseState.IN_IDENTIFIER;
								}
								break;
							}
							case IN_IDENTIFIER: {
								if (Character.isWhitespace(this.textContent.charAt(cursor))) {
									state = JavadocMethodReferenceParseState.AFTER_IDENTIFIER;
								}
								break;
							}
							case AFTER_IDENTIFIER: {
								// do nothing
								break;
							}
						}
					}
					cursor++;
				}
				if (state == JavadocMethodReferenceParseState.BEFORE_IDENTIFIER) {
					String expectedMethodName = methodRef.getName().toString();
					final int finalizedMinNumParams = minNumParams;
					List<IMethodBinding> potentialMethodCompletions = null;
					// FIXME: this should use resolve binding for MethodRef, but the current implementation is unusably broken
					if (potentialMethodCompletions == null) {
						Name qualifier = methodRef.getQualifier();
						if (qualifier == null) {
							ITypeBinding parentTypeBinding = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding();
							potentialMethodCompletions = Stream.of(parentTypeBinding.getDeclaredMethods()) //
									.filter(methodCandidate -> {
										if (!expectedMethodName.equals(methodCandidate.getName())) {
											return false;
										}
										return methodCandidate.getParameterTypes().length >= finalizedMinNumParams;
									}) //
									.toList();
						} else if (qualifier.resolveBinding() instanceof ITypeBinding javadocResolvedTypeBinding) {
							potentialMethodCompletions = Stream.of(javadocResolvedTypeBinding.getDeclaredMethods()) //
								.filter(methodCandidate -> {
									if (!expectedMethodName.equals(methodCandidate.getName())) {
										return false;
									}
									return methodCandidate.getParameterTypes().length >= finalizedMinNumParams;
								}) //
								.toList();
						} else {
							// Use the search engine to get the type binding
							String classToComplete = qualifier.toString();
							String packageName = classToComplete.lastIndexOf('.') < 0 ? null : classToComplete.substring(0, classToComplete.lastIndexOf('.'));
							if (packageName != null) {
								classToComplete = classToComplete.substring(classToComplete.lastIndexOf('.') + 1);
							} else {
								CompilationUnit cu = (CompilationUnit)DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.COMPILATION_UNIT });
								if (cu.getPackage() != null) {
									packageName = cu.getPackage().getName().toString();
								} else {
									packageName = ""; //$NON-NLS-1$
								}
							}
							List<IType> potentialTypes = findTypes(classToComplete, IJavaSearchConstants.TYPE, packageName).toList();
							List<IType> sourceTypes = potentialTypes.stream().filter(type -> type instanceof SourceType).toList();
							if (!potentialTypes.isEmpty()) {
								IType typeToComplete;
								if (potentialTypes.size() > 1 && !sourceTypes.isEmpty()) {
									typeToComplete = sourceTypes.get(0);
								} else {
									typeToComplete = potentialTypes.get(0);
								}
								ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
								parser.setProject(this.javaProject);
								IBinding[] createdBindings = parser.createBindings(new IType[] { typeToComplete }, new NullProgressMonitor());
								if (createdBindings.length > 0 && createdBindings[0] instanceof ITypeBinding typeBinding) {
									potentialMethodCompletions = Stream.of(typeBinding.getDeclaredMethods()) //
											.filter(methodCandidate -> {
												if (!expectedMethodName.equals(methodCandidate.getName())) {
													return false;
												}
												return methodCandidate.getParameterTypes().length >= finalizedMinNumParams;
											}) //
											.toList();
								}
							}
						}
					}
					if (potentialMethodCompletions != null) {
						for (IMethodBinding potentialMethodCompletion : potentialMethodCompletions) {
							CompletionProposal proposal = toProposal(potentialMethodCompletion);
							proposal.setReplaceRange(methodRef.getName().getStartPosition(), methodRef.getStartPosition() + methodRef.getLength());
							proposal.setTokenRange(methodRef.getName().getStartPosition(), methodRef.getStartPosition() + methodRef.getLength());
							proposal.setRelevance(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED
									+ RelevanceConstants.R_INTERESTING + RelevanceConstants.R_CASE
									+ RelevanceConstants.R_EXACT_NAME + RelevanceConstants.R_UNQUALIFIED
									+ RelevanceConstants.R_NON_RESTRICTED);
							this.requestor.accept(proposal);
						}
					}
					suggestDefaultCompletions = false;
				} else if (state == JavadocMethodReferenceParseState.AFTER_IDENTIFIER) {
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof MemberRef memberRef) {
				IBinding bindingToComplete;
				if (memberRef.getQualifier() != null) {
					bindingToComplete = memberRef.getQualifier().resolveBinding();
				} else {
					bindingToComplete = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding();
				}
				if (bindingToComplete instanceof ITypeBinding typeBinding) {
					Bindings javadocScope = new Bindings();
					processMembers(this.toComplete, typeBinding, javadocScope, false);
					publishFromScope(javadocScope);
					suggestAccessibleConstructorsForType(typeBinding);
				}
				suggestDefaultCompletions = false;
			}
			if (context instanceof ThisExpression thisExpression) {
				if (thisExpression.getQualifier() != null) {
					IBinding binding = thisExpression.getQualifier().resolveBinding();
					if (binding instanceof ITypeBinding typeBinding) {
						this.qualifyingType = typeBinding;
						Bindings typesMembers = new Bindings();
						processMembers(this.toComplete, typeBinding, typesMembers, true);
						publishFromScope(typesMembers);
						this.requestor.accept(createClassKeywordProposal(typeBinding, -1,-1));
					}
					for (char[] keyword : List.of(Keywords.SUPER, Keywords.THIS)) {
						if (!isFailedMatch(this.prefix.toCharArray(), keyword)) {
							CompletionProposal res = createKeywordProposal(keyword, -1, -1);
							res.setRelevance(res.getRelevance() + RelevanceConstants.R_NON_INHERITED);
							this.requestor.accept(res);
						}
					}
					suggestDefaultCompletions = false;
				}
			}
			if (context != null && context.getLocationInParent() == QualifiedType.NAME_PROPERTY && context.getParent() instanceof QualifiedType qType) {
				Type qualifier = qType.getQualifier();
				if (qualifier != null) {
					ITypeBinding qualifierBinding = qualifier.resolveBinding();
					if (qualifierBinding != null) {
						for (ITypeBinding nestedType : qualifierBinding.getDeclaredTypes()) {
							this.requestor.accept(toProposal(nestedType));
						}
					}
				}
			}
			if (isParameterInNonParameterizedType(context)) {
				suggestDefaultCompletions = false;
			}

			// check for accessible bindings to potentially turn into completions.
			// currently, this is always run, even when not using the default completion,
			// because method argument guessing uses it.
			scrapeAccessibleBindings(defaultCompletionBindings);
			if (shouldSuggestPackages(toComplete)) {
				suggestPackages(toComplete);
			}
			if (context instanceof SimpleName simple && !(simple.getParent() instanceof Name)) {
				for (ImportDeclaration importDecl : (List<ImportDeclaration>)this.unit.imports()) {
					if (importDecl.isStatic()) {
						if (!importDecl.isOnDemand()) {
							defaultCompletionBindings.add(importDecl.resolveBinding());
						} else if (importDecl.resolveBinding() instanceof ITypeBinding staticallyImportedAll) {
							// only add direct declarations, not inherited ones
							Stream.of(staticallyImportedAll.getDeclaredFields(), staticallyImportedAll.getDeclaredMethods()) //
								.flatMap(Arrays::stream) //
								.filter(binding -> Modifier.isStatic(binding.getModifiers()))
								.filter(this::isVisible)
								.forEach(defaultCompletionBindings::add);
						}
					}
				}
			}

			if (suggestDefaultCompletions) {
				statementLikeKeywords();
				if (!this.prefix.isEmpty() && this.extendsOrImplementsInfo == null) {
					suggestTypeKeywords(DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.BLOCK }) == null);
				}
				publishFromScope(defaultCompletionBindings);
				suggestSuperConstructors();
				if (!completeAfter.isBlank()) {
					String currentPackage = this.unit.getPackage() == null ? "" : this.unit.getPackage().getName().toString();
					AbstractTypeDeclaration typeDecl = DOMCompletionUtil.findParentTypeDeclaration(context);
					ITypeBinding currentTypeBinding = typeDecl == null ? null : typeDecl.resolveBinding();
					final int typeMatchRule = this.toComplete.getParent() instanceof Annotation
							? IJavaSearchConstants.ANNOTATION_TYPE
							: IJavaSearchConstants.TYPE;
					if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
						final Set<String> alreadySuggestedFqn = ConcurrentHashMap.newKeySet();
						findTypes(completeAfter, typeMatchRule, null)
							.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
									type.getElementName().toCharArray()))
							.filter(type -> filterTypeBasedOnAccess(type, currentPackage, currentTypeBinding))
							.filter(type -> {
								for (var scrapedBinding : defaultCompletionBindings.all().toList()) {
									if (scrapedBinding instanceof ITypeBinding scrapedTypeBinding) {
										if (type.equals(scrapedTypeBinding.getJavaElement()) || type.getKey().equals(scrapedTypeBinding.getKey())) {
											return false;
										}
									}
								}
								return true;
							})
							.filter(type -> {
								return filterBasedOnExtendsOrImplementsInfo(type, this.extendsOrImplementsInfo);
							})
							.filter(type -> {
								if (alreadySuggestedFqn.contains(type.getFullyQualifiedName())) {
									return false;
								}
								alreadySuggestedFqn.add(type.getFullyQualifiedName());
								return true;
							})
							.map(this::toProposal).forEach(this.requestor::accept);
					}
				}
				checkCancelled();
				if (shouldSuggestPackages(toComplete)) {
					suggestPackages(toComplete);
				}
			}

			checkCancelled();
		} catch (JavaModelException e) {
			ILog.get().error(e.getMessage(), e);
		} finally {
			this.requestor.endReporting();
			if (this.monitor != null) {
				this.monitor.done();
			}
		}
	}

	private boolean isTypeInVariableDeclaration(ASTNode context) {
		ASTNode cursor = context.getParent();
		ASTNode childCursor = context;
		while (cursor != null
				&& !(cursor instanceof FieldDeclaration)
				&& !(cursor instanceof VariableDeclarationStatement)
				&& !(cursor instanceof SingleVariableDeclaration)) {
			childCursor = cursor;
			cursor = cursor.getParent();
		}
		if (cursor instanceof FieldDeclaration fieldDecl) {
			return fieldDecl.getType() == childCursor;
		} else if (cursor instanceof VariableDeclarationStatement varDecl) {
			return varDecl.getType() == childCursor;
		} else if (cursor instanceof SingleVariableDeclaration varDecl) {
			return varDecl.getType() == childCursor;
		}
		return false;
	}

	private void completeMissingType(Type type) throws JavaModelException {
		Type simpleType = type instanceof ParameterizedType parameterized ?
			parameterized.getType() : type;
		var scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { this.javaProject });
		SearchEngine searchEngine = new SearchEngine(this.workingCopyOwner);
		List<IType> types = new ArrayList<>();
		searchEngine.searchAllTypeNames(null, SearchPattern.R_PREFIX_MATCH, simpleType.toString().toCharArray(), SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.TYPE, scope, new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(TypeNameMatch match) {
				types.add(match.getType());
			}
		}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
		StringBuilder builder = new StringBuilder();
		if (type instanceof ParameterizedType parameterized) {
			builder.append(Signature.C_GENERIC_START);
			for (Type typeParam : (List<Type>)parameterized.typeArguments()) {
				builder.append(SignatureUtils.createSignature(typeParam, searchEngine, scope, monitor));
			}
			builder.append(Signature.C_GENERIC_END);
		}
		for (IType matchedType : types) {
			processMembers(matchedType, searchEngine, scope)
			.map(member -> {
				StringBuilder declaringSignature = new StringBuilder();
				declaringSignature.append(member.getDeclarationSignature());
				declaringSignature.deleteCharAt(declaringSignature.length() - 1); // `;`
				declaringSignature.append(builder);
				declaringSignature.append(';');
				member.setDeclarationSignature(declaringSignature.toString().toCharArray());
				CompletionProposal typeProposal = toProposal(matchedType);
				typeProposal.setReplaceRange(simpleType.getStartPosition(), simpleType.getStartPosition() + simpleType.getLength());
				typeProposal.setRelevance(member.getRelevance());
				type.accept(new ASTVisitor() {
					@Override
					public boolean visit(SimpleType simpleType) {
						ITypeBinding binding = simpleType.resolveBinding();
						if (binding == null || binding.isRecovered()) {
							List<IType> matchingITypes = new ArrayList<>();
							try {
								searchEngine.searchAllTypeNames(null, SearchPattern.R_PREFIX_MATCH, simpleType.toString().toCharArray(), SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.TYPE, scope, new TypeNameMatchRequestor() {
									@Override
									public void acceptTypeNameMatch(TypeNameMatch match) {
										matchingITypes.add(match.getType());
									}
								}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
							} catch (JavaModelException ex) {
								ILog.get().error(ex.getMessage(), ex);
							}
							if (matchingITypes.size() == 1) {
								CompletionProposal typeProposal = toProposal(matchingITypes.get(0));
								typeProposal.setReplaceRange(simpleType.getStartPosition(), simpleType.getStartPosition() + simpleType.getLength());
								typeProposal.setRelevance(member.getRelevance());
							}
						}
						return true;
					}
				});
				member.setRequiredProposals(new CompletionProposal[] { typeProposal });
				return member;
			}).forEach(DOMCompletionEngine.this.requestor::accept);
		}
	}

	private void suggestSuperConstructors() {
		if (this.requestor.isIgnored(CompletionProposal.METHOD_REF) || this.requestor.isIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION)) {
			return;
		}
		ASTNode methodOrTypeDeclaration = DOMCompletionUtil.findParent(toComplete,
				new int[] { ASTNode.METHOD_DECLARATION, ASTNode.TYPE_DECLARATION, ASTNode.ANNOTATION_TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION });
		if (this.pattern.matchesName(this.prefix.toCharArray(),
				Keywords.SUPER) && methodOrTypeDeclaration instanceof MethodDeclaration methodDecl && methodDecl.isConstructor()) {
			AbstractTypeDeclaration parentType = DOMCompletionUtil.findParentTypeDeclaration(toComplete);
			ITypeBinding parentTypeBinding = parentType.resolveBinding();
			ITypeBinding superclassBinding = parentTypeBinding.getSuperclass();
			if (superclassBinding != null) {
				for (IMethodBinding superclassMethod : superclassBinding.getDeclaredMethods()) {
					if (superclassMethod.isConstructor() && isVisible(superclassMethod)) {
						this.requestor.accept(toSuperConstructorProposal(superclassMethod));
					}
				}
			}
		}
	}

	/**
	 * Returns true if the given type can be accessed from the given context
	 *
	 * @param type the type that you're trying to access
	 * @param currentPackage the package that you're in
	 * @param currentTypeBinding the binding of the type that you're currently in, can be null
	 * @return true if the given type can be accessed from the given context
	 */
	private boolean filterTypeBasedOnAccess(IType type, String currentPackage, ITypeBinding currentTypeBinding) {
		String typePackage = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT).getElementName();
		// can only access classes in the default package from the default package
		if (!currentPackage.isEmpty() && typePackage.isEmpty()) {
			return false;
		}
		try {
			int flags = type.getFlags();
			if ((flags & (Flags.AccPublic | Flags.AccPrivate | Flags.AccProtected)) == 0) {
				return currentPackage.equals(typePackage);
			}
			if ((flags & Flags.AccPublic) != 0) {
				return true;
			}
			if ((flags & Flags.AccProtected) != 0) {
				// if `protected` is used correctly means `type` is an inner class
				if (currentTypeBinding == null || type.getDeclaringType() == null) {
					return false;
				}
				return findInSupers(currentTypeBinding, type.getDeclaringType().getKey());
			}
			// private inner class
			return false;
		} catch (JavaModelException e) {
			return true;
		}
	}

	private boolean isParameterInNonParameterizedType(ASTNode context) {
		if (DOMCompletionUtil.findParent(context, new int[] { ASTNode.PARAMETERIZED_TYPE }) != null) {
			ASTNode cursor1 = context;
			ASTNode cursor2 = context.getParent();
			while (!(cursor2 instanceof ParameterizedType paramType)) {
				cursor1 = cursor2;
				cursor2 = cursor2.getParent();
			}
			ITypeBinding paramTypeBinding = paramType.resolveBinding().getTypeDeclaration();
			if (paramTypeBinding.getTypeParameters().length <= paramType.typeArguments().indexOf(cursor1)) {
				return true;
			}
		}
		return false;
	}

	private void suggestAccessibleConstructorsForType(ITypeBinding typeBinding) {
		ITypeBinding parentTypeBinding = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding();
		Stream.of(typeBinding.getDeclaredMethods()) //
			.filter(IMethodBinding::isConstructor) //
			.filter(method -> {
				// note that this isn't the usual logic.
				// this is case sensitive, whereas filtering is usually case-insensitive.
				// See CompletionEngine:2702
				// FIXME: file this as a bug upstream and see what folks say about it
				return CharOperation.prefixEquals(this.prefix.toCharArray(), method.getName().toCharArray())
						|| (this.assistOptions.camelCaseMatch && CharOperation.camelCaseMatch(this.prefix.toCharArray(), method.getName().toCharArray()));
			}) //
			.filter(method -> {
				boolean includeProtected = findInSupers(parentTypeBinding, typeBinding);
				if ((method.getModifiers() & Flags.AccPrivate) != 0
						&& (parentTypeBinding == null || !method.getDeclaringClass().getKey().equals(parentTypeBinding.getKey()))) {
					return false;
				}
				if ((method.getModifiers() & (Flags.AccPrivate | Flags.AccPublic | Flags.AccProtected)) == 0) {
					if (parentTypeBinding == null) {
						return false;
					}
					return method.getDeclaringClass().getPackage().getName().equals(parentTypeBinding.getPackage().getName());
				}
				if ((method.getModifiers() & Flags.AccProtected) != 0) {
					return includeProtected;
				}
				return true;
			})
			.sorted(Comparator.comparing(this::getSignature).reversed())
			.map(this::toProposal) //
			.forEach(this.requestor::accept);
	}

	private enum JavadocMethodReferenceParseState {
		BEFORE_IDENTIFIER,
		IN_IDENTIFIER,
		AFTER_IDENTIFIER,
		;
	}

	private Set<String> findAlreadyDocumentedParameters(Javadoc javadoc) {
		Set<String> alreadyDocumentedParameters = new HashSet<>();
		for (TagElement tagElement : (List<TagElement>)javadoc.tags()) {
			if (TagElement.TAG_PARAM.equals(tagElement.getTagName())) {
				List<?> fragments = tagElement.fragments();
				if (!fragments.isEmpty()) {
					if (fragments.get(0) instanceof TextElement textElement) {
						if (fragments.size() >= 3 && fragments.get(1) instanceof Name) {
							// ["<", "MyTypeVarName", ">"]
							StringBuilder builder = new StringBuilder();
							builder.append(fragments.get(0));
							builder.append(fragments.get(1));
							builder.append(fragments.get(2));
							alreadyDocumentedParameters.add(builder.toString());
						} else if (!textElement.getText().isEmpty()) {
							alreadyDocumentedParameters.add(textElement.getText());
						}
					} else if (fragments.get(0) instanceof String str && !str.isBlank()) {
						alreadyDocumentedParameters.add(str);
					} else if (fragments.get(0) instanceof Name name && !name.toString().isBlank()) {
						alreadyDocumentedParameters.add(name.toString());
					}
				}
			}
		}
		return alreadyDocumentedParameters;
	}

	private CompletionProposal toAtParamProposal(String paramName, TagElement tagElement) {
		InternalCompletionProposal res = createProposal(CompletionProposal.JAVADOC_PARAM_REF);
		boolean isTypeParam = paramName.startsWith("<"); //$NON-NLS-1$
		res.setCompletion(paramName.toCharArray());
		if (isTypeParam) {
			res.setName(paramName.substring(1, paramName.length() - 1).toCharArray());
		} else {
			res.setName(paramName.toCharArray());
		}
		int relevance = RelevanceConstants.R_DEFAULT
				// FIXME: "interesting" is added twice upstream for normal parameters,
				// (but 0 times for type parameters).
				// This doesn't make sense to me.
				+ (isTypeParam ? 0 : 2 * RelevanceConstants.R_INTERESTING)
				+ RelevanceConstants.R_NON_RESTRICTED;
		res.setRelevance(relevance);

		int tagStart = tagElement.getStartPosition();
		int realStart = tagStart + TagElement.TAG_PARAM.length() + 1;

		int endPos = realStart;
		if (this.textContent != null) {
			while (endPos < this.textContent.length() && !Character.isWhitespace(this.textContent.charAt(endPos))) {
				endPos++;
			}
		}

		res.setReplaceRange(realStart, endPos);
		res.setTokenRange(realStart, endPos);
		return res;
	}

	private boolean shouldSuggestPackages(ASTNode context) {
		if (context instanceof CompilationUnit) {
			return false;
		}
		if (context instanceof PackageDeclaration decl && decl.getName() != null && !Arrays.equals(decl.getName().toString().toCharArray(), RecoveryScanner.FAKE_IDENTIFIER)) {
			// on `package` token
			return false;
		}
		boolean inExtendsOrImplements = false;
		while (context != null) {
			if (context instanceof SimpleName) {
				if (context.getLocationInParent() == QualifiedName.NAME_PROPERTY
					|| (context.getLocationInParent() instanceof ChildPropertyDescriptor child && child.getChildType() == Expression.class)
					|| (context.getLocationInParent() instanceof ChildListPropertyDescriptor childList && childList.getElementType() == Expression.class)) {
					return true;
				}
			}
			if (context instanceof QualifiedName) {
				return true;
			}
			if (context instanceof Type || context instanceof Name) {
				context = context.getParent();
			} else if ((context.getLocationInParent() instanceof ChildPropertyDescriptor child && child.getChildType() == Type.class)
					|| (context.getLocationInParent() instanceof ChildListPropertyDescriptor childList && childList.getElementType() == Type.class)) {
				return true;
			} else {
				return false;
			}
		}
		return !inExtendsOrImplements;
	}

	private void suggestTypeKeywords(boolean includeVoid) {
		for (char[] keyword : TYPE_KEYWORDS_EXCEPT_VOID) {
			if (!this.isFailedMatch(this.prefix.toCharArray(), keyword)) {
				this.requestor.accept(createKeywordProposal(keyword, -1, -1));
			}
		}
		if (includeVoid && !this.isFailedMatch(this.prefix.toCharArray(), VOID)) {
			this.requestor.accept(createKeywordProposal(VOID, -1, -1));
		}
	}

	private void scrapeAccessibleBindings(Bindings scope) {
		ASTNode current = this.toComplete;
		while (current != null) {
			Collection<? extends IBinding> gottenVisibleBindings = visibleBindings(current);
			scope.addAll(gottenVisibleBindings);
			if (current instanceof AbstractTypeDeclaration typeDecl) {
				processMembers(this.toComplete, typeDecl.resolveBinding(), scope, false);
			}
			current = current.getParent();
		}

		// handle favourite members
		if (this.requestor.getFavoriteReferences() == null) {
			return;
		}

		Set<String> scopedMethods = scope.methods().map(IBinding::getName).collect(Collectors.toSet());
		Set<String> scopedVariables = scope.all().filter(IVariableBinding.class::isInstance).map(IBinding::getName).collect(Collectors.toSet());
		Set<String> scopedTypes = scope.all().filter(ITypeBinding.class::isInstance).map(IBinding::getName).collect(Collectors.toSet());

		Set<IJavaElement> keysToResolve = new HashSet<>();
		IJavaProject project = this.javaProject;
		for (String favouriteReference: this.requestor.getFavoriteReferences()) {
			if (favouriteReference.endsWith(".*")) { //$NON-NLS-1$
				favouriteReference = favouriteReference.substring(0, favouriteReference.length() - 2);
				String packageName = favouriteReference.indexOf('.') < 0 ? "" : favouriteReference.substring(0, favouriteReference.lastIndexOf('.')); //$NON-NLS-1$
				String typeName = favouriteReference.indexOf('.') < 0 ? favouriteReference : favouriteReference.substring(favouriteReference.lastIndexOf('.') + 1);
				findTypes(typeName, SearchPattern.R_EXACT_MATCH, packageName).filter(type -> type.getElementName().equals(typeName)).forEach(keysToResolve::add);
			} else if (favouriteReference.lastIndexOf('.') >= 0) {
				String memberName = favouriteReference.substring(favouriteReference.lastIndexOf('.') + 1);
				String typeFqn = favouriteReference.substring(0, favouriteReference.lastIndexOf('.'));
				String packageName = typeFqn.indexOf('.') < 0 ? "" : typeFqn.substring(0, typeFqn.lastIndexOf('.')); //$NON-NLS-1$
				String typeName = typeFqn.indexOf('.') < 0 ? typeFqn : typeFqn.substring(typeFqn.lastIndexOf('.') + 1);
				findTypes(typeName, SearchPattern.R_EXACT_MATCH, packageName).filter(type -> type.getElementName().equals(typeName)).findFirst().ifPresent(type -> {
					try {
						for (IMethod method : type.getMethods()) {
							if (method.exists() && (method.getFlags() & Flags.AccStatic) != 0 && memberName.equals(method.getElementName())) {
								keysToResolve.add(method);
							}
						}
						IField field = type.getField(memberName);
						if (field.exists() && (field.getFlags() & Flags.AccStatic) != 0) {
							keysToResolve.add(type.getField(memberName));
						}
					} catch (JavaModelException e) {
						// do nothing
					}
				});
			}
		}
		Bindings favoriteBindings = new Bindings();
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setProject(project);
		if (!keysToResolve.isEmpty()) {
			IBinding[] bindings = parser.createBindings(keysToResolve.toArray(IJavaElement[]::new), this.monitor);
			for (IBinding binding : bindings) {
				if (binding instanceof ITypeBinding typeBinding) {
					processMembers(this.toComplete, typeBinding, favoriteBindings, true);
				} else if (binding instanceof IMethodBinding methodBinding) {
					favoriteBindings.add(methodBinding);
				} else {
					favoriteBindings.add(binding);
				}
			}
		}
		favoriteBindings.all()
			.filter(binding -> {
				if (binding instanceof IMethodBinding) {
					return !scopedMethods.contains(binding.getName());
				} else if (binding instanceof IVariableBinding) {
					return !scopedVariables.contains(binding.getName());
				}
				return !scopedTypes.contains(binding.getName());
			})
			.forEach(scope::add);
	}

	private void topLevelTypes(Bindings scope) {
		Queue<AbstractTypeDeclaration> toCollect = new ArrayDeque<AbstractTypeDeclaration>();
		toCollect.addAll(this.unit.types());
		while (!toCollect.isEmpty()) {
			AbstractTypeDeclaration current = toCollect.poll();
			scope.add(current.resolveBinding());
			for (ASTNode bodyDecl : (List<ASTNode>)current.bodyDeclarations()) {
				if (bodyDecl instanceof AbstractTypeDeclaration childTypeDecl) {
					toCollect.add(childTypeDecl);
				}
			}
		}
	}

	private void completeMethodModifiers(MethodDeclaration methodDeclaration) {
		List<char[]> keywords = new ArrayList<>();

		if ((methodDeclaration.getModifiers() & Flags.AccAbstract) == 0) {
			keywords.add(Keywords.ABSTRACT);
		}
		if ((methodDeclaration.getModifiers() & (Flags.AccPublic | Flags.AccPrivate | Flags.AccProtected)) == 0) {
			keywords.add(Keywords.PUBLIC);
			keywords.add(Keywords.PRIVATE);
			keywords.add(Keywords.PROTECTED);
		}
		if ((methodDeclaration.getModifiers() & Flags.AccDefaultMethod) == 0) {
			keywords.add(Keywords.DEFAULT);
		}
		if ((methodDeclaration.getModifiers() & Flags.AccFinal) == 0) {
			keywords.add(Keywords.FINAL);
		}
		if ((methodDeclaration.getModifiers() & Flags.AccNative) == 0) {
			keywords.add(Keywords.NATIVE);
		}
		if ((methodDeclaration.getModifiers() & Flags.AccStrictfp) == 0) {
			keywords.add(Keywords.STRICTFP);
		}
		if ((methodDeclaration.getModifiers() & Flags.AccSynchronized) == 0) {
			keywords.add(Keywords.SYNCHRONIZED);
		}

		for (char[] keyword : keywords) {
			if (!isFailedMatch(this.prefix.toCharArray(), keyword)) {
				this.requestor.accept(createKeywordProposal(keyword, this.offset, this.offset));
			}
		}
	}

	private void checkCancelled() {
		if (this.monitor != null && this.monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	private void statementLikeKeywords() {
		List<char[]> keywords = new ArrayList<>();
		keywords.add(Keywords.ASSERT);
		keywords.add(Keywords.RETURN);
		keywords.add(Keywords.SUPER);
		if (DOMCompletionUtil.findParent(this.toComplete,
				new int[] { ASTNode.WHILE_STATEMENT, ASTNode.DO_STATEMENT, ASTNode.FOR_STATEMENT }) != null) {
			keywords.add(Keywords.BREAK);
			keywords.add(Keywords.CONTINUE);
		}
		ExpressionStatement exprStatement = (ExpressionStatement) DOMCompletionUtil.findParent(this.toComplete, new int[] {ASTNode.EXPRESSION_STATEMENT});
		if (exprStatement != null) {

			ASTNode statementParent = exprStatement.getParent();
			if (statementParent instanceof Block block) {
				int exprIndex = block.statements().indexOf(exprStatement);
				if (exprIndex > 0) {
					ASTNode prevStatement = (ASTNode)block.statements().get(exprIndex - 1);
					if (prevStatement.getNodeType() == ASTNode.IF_STATEMENT) {
						keywords.add(Keywords.ELSE);
					}
				}
			}
		}
		for (char[] keyword : keywords) {
			if (!isFailedMatch(this.toComplete.toString().toCharArray(), keyword)) {
				this.requestor.accept(createKeywordProposal(keyword, this.toComplete.getStartPosition(), this.offset));
			}
		}
	}

	private void completeJavadocBlockTags(TagElement tagNode) {
		if (this.requestor.isIgnored(CompletionProposal.JAVADOC_BLOCK_TAG)) {
			return;
		}
		for (char[] blockTag : DOMCompletionEngineJavadocUtil.getJavadocBlockTags(this.javaProject, tagNode)) {
			if (!isFailedMatch(this.prefix.toCharArray(), blockTag)) {
				this.requestor.accept(toJavadocBlockTagProposal(blockTag));
			}
		}
	}

	private void completeJavadocInlineTags(TagElement tagNode) {
		if (this.requestor.isIgnored(CompletionProposal.JAVADOC_INLINE_TAG)) {
			return;
		}
		for (char[] blockTag : DOMCompletionEngineJavadocUtil.getJavadocInlineTags(this.javaProject, tagNode)) {
			if (!isFailedMatch(this.prefix.toCharArray(), blockTag)) {
				this.requestor.accept(toJavadocInlineTagProposal(blockTag));
			}
		}
	}

	private void completeThrowsClause(MethodDeclaration methodDeclaration, Bindings scope) {
		if (methodDeclaration.thrownExceptionTypes().size() == 0) {
			int startScanIndex = Math.max(
					methodDeclaration.getName().getStartPosition() + methodDeclaration.getName().getLength(),
					methodDeclaration.getReturnType2().getStartPosition() + methodDeclaration.getReturnType2().getLength());
			if (!methodDeclaration.parameters().isEmpty()) {
				SingleVariableDeclaration lastParam = (SingleVariableDeclaration)methodDeclaration.parameters().get(methodDeclaration.parameters().size() - 1);
				startScanIndex = lastParam.getName().getStartPosition() + lastParam.getName().getLength();
			}
			if (this.textContent != null) {
				String text = this.textContent.substring(startScanIndex, this.offset);
				// JDT checks for "throw" instead of "throws", probably assuming that it's a common misspelling
				int firstThrow = text.indexOf("throw"); //$NON-NLS-1$
				if (firstThrow == -1 || firstThrow >= this.offset - this.prefix.length()) {
					if (!isFailedMatch(this.prefix.toCharArray(), Keywords.THROWS)) {
						this.requestor.accept(createKeywordProposal(Keywords.THROWS, -1, -1));
					}
				}
			} else {
				if (!isFailedMatch(this.prefix.toCharArray(), Keywords.THROWS)) {
					this.requestor.accept(createKeywordProposal(Keywords.THROWS, -1, -1));
				}
			}
		}
	}

	private void suggestPackages(ASTNode context) {
		checkCancelled();
		while (context != null && context.getParent() instanceof Name) {
			context = context.getParent();
		}
		String prefix = context instanceof Name name ? name.toString() : this.prefix;
		if (prefix != null && !prefix.isBlank()) {
			this.nameEnvironment.findPackages(prefix.toCharArray(), this.nestedEngine);
		}
	}

	private void suggestTypesInPackage(String packageName) {
		if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
			List<IType> foundTypes = findTypes(this.prefix, packageName).toList();
			String currentPackage = this.unit.getPackage() == null ? "" : this.unit.getPackage().getName().toString();
			AbstractTypeDeclaration typeDecl = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete);
			ITypeBinding currentTypeBinding = typeDecl == null ? null : typeDecl.resolveBinding();
			for (IType foundType : foundTypes) {
				if (this.pattern.matchesName(this.prefix.toCharArray(), foundType.getElementName().toCharArray())) {
					if (filterBasedOnExtendsOrImplementsInfo(foundType, this.extendsOrImplementsInfo) && filterTypeBasedOnAccess(foundType, currentPackage, currentTypeBinding)) {
						this.requestor.accept(this.toProposal(foundType));
					}
				}
			}
		}
	}

	private boolean filterBasedOnExtendsOrImplementsInfo(IType toFilter, ExtendsOrImplementsInfo info) {
		if (info == null) {
			return true;
		}
		try {
			if (!(info.typeDecl instanceof TypeDeclaration typeDeclaration)
					|| (toFilter.getFlags() & Flags.AccFinal) != 0
					|| typeDeclaration.resolveBinding().getKey().equals(toFilter.getKey())) {
				return false;
			}
			{
				IType cursor = toFilter;
				String invocationKey = info.typeDecl.resolveBinding().getKey();
				while (cursor != null) {
					if (cursor.getKey().equals(invocationKey)) {
						return false;
					}
					cursor = cursor.getDeclaringType();
				}
			}
			if (toFilter.isEnum() || toFilter.isRecord()) {
				// cannot extend or implement
				return false;
			}
			if (toFilter.isSealed()) {
				String currentTypeName = info.typeDecl.getName().toString();
				boolean permitted = false;
				for (var elt : toFilter.getPermittedSubtypeNames()) {
					if (elt.equals(currentTypeName)) {
						permitted = true;
					}
				}
				if (!permitted) {
					return false;
				}
			}
			if (typeDeclaration.isInterface()
					// in an interface extends clause, we should rule out non-interfaces
					&& toFilter.isInterface()
					// prevent double extending
					&& !extendsOrImplementsGivenType(typeDeclaration, toFilter)) {
				return true;
			} else if (!typeDeclaration.isInterface()
					// in an extends clause, only accept non-interfaces
					// in an implements clause, only accept interfaces
					&& (info.isImplements == toFilter.isInterface())
					// prevent double extending
					&& !extendsOrImplementsGivenType(typeDeclaration, toFilter)) {
				return true;
			}
			return false;
		} catch (JavaModelException e) {
			// we can't really tell if it's appropriate
			return true;
		}
	}

	/**
	 * Returns info if the given node is in an extends or implements clause, or null if not in either clause
	 *
	 * @see ExtendsOrImplementsInfo
	 * @param completion the node to check
	 * @return info if the given node is in an extends or implements clause, or null if not in either clause
	 */
	private ExtendsOrImplementsInfo isInExtendsOrImplements(ASTNode completion) {
		if (completion instanceof AbstractTypeDeclaration typeDecl) {
			// string manipulation
			int nameEndOffset = typeDecl.getName().getStartPosition() + typeDecl.getName().getLength();
			int bodyStart = nameEndOffset;
			while (bodyStart < this.textContent.length() && this.textContent.charAt(bodyStart) != '{') {
				bodyStart++;
			}
			int prefixCursor = this.offset;
			while (prefixCursor > 0 && !Character.isWhitespace(this.textContent.charAt(prefixCursor - 1))) {
				prefixCursor--;
			}
			if (nameEndOffset < this.offset && this.offset <= bodyStart) {
				String extendsOrImplementsContent = this.textContent.substring(nameEndOffset, this.offset);
				int implementsOffset = extendsOrImplementsContent.indexOf("implements");
				int extendsOffset = extendsOrImplementsContent.indexOf("extends");
				if (implementsOffset >= 0 || extendsOffset >= 0) {
					boolean isInterface = implementsOffset >= 0 && this.offset > implementsOffset;
					return new ExtendsOrImplementsInfo(typeDecl, isInterface);
				}
			}
			return null;
		}
		ASTNode cursor = completion;
		while (cursor != null
				&& cursor.getNodeType() != ASTNode.TYPE_DECLARATION
				&& cursor.getNodeType() != ASTNode.ENUM_DECLARATION
				&& cursor.getNodeType() != ASTNode.RECORD_DECLARATION
				&& cursor.getNodeType() != ASTNode.ANNOTATION_TYPE_DECLARATION) {
			StructuralPropertyDescriptor locationInParent = cursor.getLocationInParent();
			if (locationInParent == null) {
				return null;
			}
			if (locationInParent.isChildListProperty()) {
				String locationId = locationInParent.getId();
				if (TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY.getId().equals(locationId)
							|| EnumDeclaration.SUPER_INTERFACE_TYPES_PROPERTY.getId().equals(locationId)
							|| RecordDeclaration.SUPER_INTERFACE_TYPES_PROPERTY.getId().equals(locationId)) {
					return new ExtendsOrImplementsInfo((AbstractTypeDeclaration)cursor.getParent(), true);
				}
			} else if (locationInParent.isChildProperty()) {
				String locationId = locationInParent.getId();
				if (TypeDeclaration.SUPERCLASS_TYPE_PROPERTY.getId().equals(locationId)) {
					return new ExtendsOrImplementsInfo((AbstractTypeDeclaration)cursor.getParent(), false);
				}
			}
			cursor = cursor.getParent();
		}
		return null;
	}

	/**
	 * @param typeDecl the type declaration that holds the completion node
	 * @param isImplements true if the node to complete is in an implements clause, or false if the node
	 */
	private static record ExtendsOrImplementsInfo(AbstractTypeDeclaration typeDecl, boolean isImplements) {
	}

	/**
	 * Returns true if the given declaration already extends or implements the given reference
	 *
	 * @param typeDecl the declaration to check the extends and implements of
	 * @param typeRef the reference to check for in the extends and implements
	 * @return true if the given declaration already extends or implements the given reference
	 */
	private static boolean extendsOrImplementsGivenType(TypeDeclaration typeDecl, IType typeRef) {
		String refKey = typeRef.getKey();
		if (typeDecl.getSuperclassType() != null
				&& typeDecl.getSuperclassType().resolveBinding() != null
				&& refKey.equals(typeDecl.getSuperclassType().resolveBinding().getKey())) {
			return true;
		}
		for (var superInterface : typeDecl.superInterfaceTypes()) {
			ITypeBinding superInterfaceBinding = ((Type)superInterface).resolveBinding();
			if (superInterfaceBinding != null && refKey.equals(superInterfaceBinding.getKey())) {
				return true;
			}
		}
		return false;
	}

	private void completeMarkerAnnotation(String completeAfter) {
		findTypes(completeAfter, IJavaSearchConstants.ANNOTATION_TYPE, null)
			.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
					type.getElementName().toCharArray()))
			.map(this::toProposal).forEach(this.requestor::accept);
	}

	private void completeNormalAnnotationParams(NormalAnnotation normalAnnotation, Bindings scope) {
		Set<String> definedKeys = ((List<MemberValuePair>)normalAnnotation.values()).stream() //
				.map(mvp -> mvp.getName().toString()) //
				.collect(Collectors.toSet());
		completeAnnotationParams(normalAnnotation, definedKeys, scope);
	}

	private void completeAnnotationParams(Annotation annotation, Set<String> definedKeys, Bindings scope) {
		Arrays.stream(annotation.resolveTypeBinding().getDeclaredMethods()) //
			.filter(declaredMethod -> {
				return (declaredMethod.getModifiers() & Flags.AccStatic) == 0
						&& !definedKeys.contains(declaredMethod.getName().toString());
			}) //
			.forEach(scope::add);
		scope.methods() //
			.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
			.map(binding -> toAnnotationAttributeRefProposal(binding))
			.forEach(this.requestor::accept);
	}

	private void publishFromScope(Bindings scope) {
		scope.all() //
			.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
			.filter(binding -> {
				if (binding instanceof ITypeBinding typeBinding) {
					return filterBasedOnExtendsOrImplementsInfo((IType)typeBinding.getJavaElement(), this.extendsOrImplementsInfo);
				}
				return true;
			})
			.map(binding -> toProposal(binding))
			.forEach(this.requestor::accept);
	}

	private void completeConstructor(ITypeBinding typeBinding, ASTNode referencedFrom, IJavaProject javaProject) {
		// compute type hierarchy
		boolean isArray = typeBinding.isArray();
		AbstractTypeDeclaration enclosingType = (AbstractTypeDeclaration) DOMCompletionUtil.findParent(referencedFrom, new int[] { ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION, ASTNode.ANNOTATION_TYPE_DECLARATION });
		ITypeBinding enclosingTypeBinding = enclosingType.resolveBinding();
		IType enclosingTypeElement = (IType) enclosingTypeBinding.getJavaElement();
		if (typeBinding.getJavaElement() instanceof IType typeHandle) {
			try {
				ITypeHierarchy newTypeHierarchy = typeHandle.newTypeHierarchy(javaProject, null);
				ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
				parser.setProject(javaProject);
				List<IType> subtypes = new ArrayList<>();
				subtypes.add(typeHandle);
				for (IType subtype : newTypeHierarchy.getSubtypes(typeHandle)) {
					subtypes.add(subtype);
				}
				// Always include the current type as a possible constructor suggestion,
				// regardless if it's appropriate
				// FIXME: I feel like this is a misfeature of JDT
				// I want to fix it upstream as well as here
				if (enclosingTypeElement != null) {
					boolean alreadyThere = false;
					for (IType subtype : subtypes) {
						if (subtype.getKey().equals(enclosingTypeElement.getKey())) {
							alreadyThere = true;
						}
					}
					if (!alreadyThere) {
						subtypes.add(enclosingTypeElement);
					}
				}

				if (isArray) {
					for (IType subtype : subtypes) {
						if (!this.isFailedMatch(this.prefix.toCharArray(), subtype.getElementName().toCharArray())) {
							InternalCompletionProposal typeProposal = (InternalCompletionProposal) toProposal(subtype);
							typeProposal.setArrayDimensions(typeBinding.getDimensions());
							typeProposal.setRelevance(typeProposal.getRelevance() + RelevanceConstants.R_EXACT_EXPECTED_TYPE);
							this.requestor.accept(typeProposal);
						}
					}
				} else {
					for (IType subtype : subtypes) {
						if (!this.isFailedMatch(this.prefix.toCharArray(), subtype.getElementName().toCharArray())) {
							List<CompletionProposal> proposals = toConstructorProposals(subtype, referencedFrom, true);
							for (CompletionProposal proposal : proposals) {
								this.requestor.accept(proposal);
							}
						}
					}
				}

			} catch (JavaModelException e) {
				ILog.get().error("Unable to compute type hierarchy while performing completion", e); //$NON-NLS-1$
			}
		} else if (enclosingTypeElement != null) {
			if (isArray) {
				suggestTypeKeywords(false);
			}
			// for some reason the enclosing type is almost always suggested
			if (!this.isFailedMatch(this.prefix.toCharArray(), enclosingTypeElement.getElementName().toCharArray())) {
				List<CompletionProposal> proposals = toConstructorProposals(enclosingTypeElement, referencedFrom, true);
				for (CompletionProposal proposal : proposals) {
					this.requestor.accept(proposal);
				}
			}
		}
	}

	private void findOverridableMethods(ITypeBinding typeBinding, IJavaProject javaProject, ASTNode toReplace) {
		String originalPackageKey = typeBinding.getPackage().getKey();
		Set<String> alreadySuggestedMethodKeys = new HashSet<>();
		if (typeBinding.getSuperclass() != null) {
			findOverridableMethods0(typeBinding.getSuperclass(), alreadySuggestedMethodKeys, javaProject, originalPackageKey, toReplace);
		}
		for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
			findOverridableMethods0(superInterface, alreadySuggestedMethodKeys, javaProject, originalPackageKey, toReplace);
		}
	}

	private void findOverridableMethods0(ITypeBinding typeBinding, Set<String> alreadySuggestedKeys, IJavaProject javaProject, String originalPackageKey, ASTNode toReplace) {
		next : for (IMethodBinding method : typeBinding.getDeclaredMethods()) {
			if (alreadySuggestedKeys.contains(method.getKey())) {
				continue next;
			}
			if (method.isSynthetic() || method.isConstructor()
					|| (this.assistOptions.checkDeprecation && method.isDeprecated())
					|| Modifier.isStatic(method.getModifiers())
					|| Modifier.isPrivate(method.getModifiers())
					|| ((method.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)) == 0) && !typeBinding.getPackage().getKey().equals(originalPackageKey)) {
				continue next;
			}
			alreadySuggestedKeys.add(method.getKey());
			if (Modifier.isFinal(method.getModifiers())) {
				continue next;
			}
			if (isFailedMatch(this.prefix.toCharArray(), method.getName().toCharArray())) {
				continue next;
			}
			DOMInternalCompletionProposal proposal = createProposal(CompletionProposal.METHOD_DECLARATION);
			proposal.setReplaceRange(this.offset, this.offset);
			if (toReplace != null) {
				proposal.setReplaceRange(toReplace.getStartPosition(), toReplace.getStartPosition() + toReplace.getLength());
			}
			proposal.setName(method.getName().toCharArray());
			proposal.setFlags(method.getModifiers());
			proposal.setTypeName(method.getReturnType().getName().toCharArray());
			proposal.setDeclarationPackageName(typeBinding.getPackage().getName().toCharArray());
			proposal.setDeclarationTypeName(typeBinding.getQualifiedName().toCharArray());
			proposal.setDeclarationSignature(SignatureUtils.getSignatureChar(method.getDeclaringClass()));
			proposal.setKey(method.getKey().toCharArray());
			proposal.setSignature(SignatureUtils.getSignatureChar(method));
			proposal.setParameterNames(Stream.of(method.getParameterNames()).map(name -> name.toCharArray()).toArray(char[][]::new));

			int relevance = RelevanceConstants.R_DEFAULT
					+ RelevanceConstants.R_RESOLVED
					+ RelevanceConstants.R_INTERESTING
					+ RelevanceConstants.R_METHOD_OVERIDE
					+ (Modifier.isAbstract(method.getModifiers()) ? RelevanceConstants.R_ABSTRACT_METHOD : 0)
					+ RelevanceConstants.R_NON_RESTRICTED;
			proposal.setRelevance(relevance);

			StringBuilder completion = new StringBuilder();
			DOMCompletionEngineBuilder.createMethod(method, completion);
			proposal.setCompletion(completion.toString().toCharArray());
			this.requestor.accept(proposal);
		}
		if (typeBinding.getSuperclass() != null) {
			findOverridableMethods0(typeBinding.getSuperclass(), alreadySuggestedKeys, javaProject, originalPackageKey, toReplace);
		}
		for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
			findOverridableMethods0(superInterface, alreadySuggestedKeys, javaProject, originalPackageKey, toReplace);
		}
	}

	private Stream<IType> findTypes(String namePrefix, String packageName) {
		return findTypes(namePrefix, IJavaSearchConstants.TYPE, packageName);
	}

	private Stream<IType> findTypes(String namePrefix, int typeMatchRule, String packageName) {
		if (namePrefix == null) {
			namePrefix = ""; //$NON-NLS-1$
		}
		List<IType> types = new ArrayList<>();
		var searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[] { this.javaProject });
		TypeNameMatchRequestor typeRequestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch match) {
				types.add(match.getType());
			}
		};
		try {
			new SearchEngine(this.modelUnit instanceof ICompilationUnit modelCU ? modelCU.getOwner() : this.workingCopyOwner).searchAllTypeNames(
					packageName == null ? null : packageName.toCharArray(), SearchPattern.R_EXACT_MATCH,
					namePrefix.toCharArray(),
					SearchPattern.R_PREFIX_MATCH
							| (this.assistOptions.substringMatch ? SearchPattern.R_SUBSTRING_MATCH : 0)
							| (this.assistOptions.subwordMatch ? SearchPattern.R_SUBWORD_MATCH : 0)
							| (this.assistOptions.camelCaseMatch ? SearchPattern.R_CAMELCASE_MATCH : 0),
					typeMatchRule, searchScope, typeRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
			// TODO also resolve potential sub-packages
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return types.stream();
	}

	private void processMembers(ASTNode referencedFrom, ITypeBinding typeBinding, Bindings scope, boolean isStaticContext) {
		if (typeBinding == null) {
			return;
		}
		AbstractTypeDeclaration parentType = (AbstractTypeDeclaration)DOMCompletionUtil.findParent(referencedFrom, new int[] {ASTNode.ANNOTATION_TYPE_DECLARATION, ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION});
		if (parentType == null) {
			return;
		}
		ITypeBinding referencedFromBinding = parentType.resolveBinding();
		boolean includePrivate = referencedFromBinding.getKey().equals(typeBinding.getKey());
		MethodDeclaration methodDeclaration = (MethodDeclaration)DOMCompletionUtil.findParent(referencedFrom, new int[] {ASTNode.METHOD_DECLARATION});
		// you can reference protected fields/methods from a static method,
		// as long as those protected fields/methods are declared in the current class.
		// otherwise, the (inherited) fields/methods can only be accessed in non-static methods.
		boolean includeProtected;
		if (referencedFromBinding.getKey().equals(typeBinding.getKey())) {
			includeProtected = true;
		} else if (methodDeclaration != null
				&& (methodDeclaration.getModifiers() & Flags.AccStatic) != 0) {
			includeProtected = false;
		} else {
			includeProtected = findInSupers(referencedFromBinding, typeBinding);
		}
		processMembers(typeBinding, scope, includePrivate, includeProtected, referencedFromBinding.getPackage().getKey(), isStaticContext, typeBinding.isInterface(),
				new HashSet<>(), new HashSet<>(), new HashSet<>());
	}

	private void processMembers(ITypeBinding typeBinding, Bindings scope,
			boolean includePrivate,
			boolean includeProtected,
			String originalPackageKey,
			boolean isStaticContext,
			boolean canUseAbstract,
			Set<String> impossibleMethods,
			Set<String> impossibleFields,
			Set<String> impossibleClasses) {
		if (typeBinding == null) {
			return;
		}

		Predicate<IBinding> accessFilter = binding -> {
			if (binding == null) {
				return false;
			}
			if (binding instanceof IVariableBinding variableBinding) {
				if (impossibleFields.contains(variableBinding.getName())) {
					return false;
				}
			} else if (binding instanceof IMethodBinding methodBinding) {
				if (impossibleMethods.contains(methodBinding.getName())) {
					return false;
				}
				if (methodBinding.isConstructor()) {
					return false;
				}
			} else {
				if (impossibleClasses.contains(binding.getName())) {
					return false;
				}
			}
			if (
					// check private
					(!includePrivate && (binding.getModifiers() & Flags.AccPrivate) != 0)
					// check protected
					|| (!includeProtected && (binding.getModifiers() & Flags.AccProtected) != 0)
					// check package private
					|| ((binding.getModifiers() & (Flags.AccPublic | Flags.AccProtected | Flags.AccPrivate)) == 0 && !originalPackageKey.equals(typeBinding.getPackage().getKey()))
					// check static
					|| (isStaticContext && ((binding.getModifiers() & Flags.AccStatic) == 0 && !(binding instanceof ITypeBinding)))
					// check abstract
					|| (!canUseAbstract && (binding.getModifiers() & Flags.AccAbstract) != 0)
					) {
				if (binding instanceof IVariableBinding) {
					impossibleFields.add(binding.getName());
				} else if (binding instanceof IMethodBinding) {
					impossibleMethods.add(binding.getName());
				} else {
					impossibleClasses.add(binding.getName());
				}
				return false;
			}
			return true;
		};

		ASTNode foundDecl = DOMCompletionUtil.findParent(this.toComplete, new int[] {ASTNode.FIELD_DECLARATION, ASTNode.METHOD_DECLARATION, ASTNode.LAMBDA_EXPRESSION, ASTNode.BLOCK});
		// includePrivate means we are in the declaring class
		if (includePrivate && foundDecl instanceof FieldDeclaration fieldDeclaration) {
			// we need to take into account the order of field declarations and their fragments,
			// because any declared after this node are not viable.
			VariableDeclarationFragment fragment = (VariableDeclarationFragment)DOMCompletionUtil.findParent(this.toComplete, new int[] {ASTNode.VARIABLE_DECLARATION_FRAGMENT});
			AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration)((CompilationUnit)this.toComplete.getRoot()).findDeclaringNode(typeBinding);
			int indexOfField = typeDecl.bodyDeclarations().indexOf(fieldDeclaration);
			if (indexOfField < 0) {
				// oops we messed up, probably this fieldDecl is in a nested class
				// proceed as normal
				Arrays.stream(typeBinding.getDeclaredFields()) //
					.filter(accessFilter) //
					.forEach(scope::add);
			} else {
				for (int i = 0; i < indexOfField + 1; i++) {
					if (typeDecl.bodyDeclarations().get(i) instanceof FieldDeclaration fieldDecl) {
						List<VariableDeclarationFragment> frags = fieldDecl.fragments();
						int fragIterEndpoint = frags.indexOf(fragment);
						if (fragIterEndpoint == -1) {
							fragIterEndpoint = frags.size();
						}
						for (int j = 0; j < fragIterEndpoint; j++) {
							IVariableBinding varBinding = frags.get(j).resolveBinding();
							if (accessFilter.test(varBinding)) {
								scope.add(varBinding);
							}
						}
					}
				}
			}
		} else {
			Arrays.stream(typeBinding.getDeclaredFields()) //
				.filter(accessFilter) //
				.forEach(scope::add);
		}
		Arrays.stream(typeBinding.getDeclaredMethods()) //
			.filter(accessFilter) //
			.sorted(Comparator.comparing(this::getSignature).reversed()) // as expected by tests
			.forEach(scope::add);
		Arrays.stream(typeBinding.getDeclaredTypes()) //
			.filter(accessFilter) //
			.forEach(scope::add);
		if (typeBinding.getInterfaces() != null) {
			for (ITypeBinding superinterfaceBinding : typeBinding.getInterfaces()) {
				processMembers(superinterfaceBinding, scope, false, includeProtected, originalPackageKey, isStaticContext, true, impossibleMethods, impossibleFields, impossibleClasses);
			}
		}
		ITypeBinding superclassBinding = typeBinding.getSuperclass();
		if (superclassBinding != null) {
			processMembers(superclassBinding, scope, false, includeProtected, originalPackageKey, isStaticContext, true, impossibleMethods, impossibleFields, impossibleClasses);
		}
	}

	private Stream<CompletionProposal> processMembers(IType type, SearchEngine searchEngine, IJavaSearchScope scope) {
		IJavaElement[] children;
		try {
			children = type.getChildren();
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			children = new IJavaElement[0];
		}
		Stream<CompletionProposal> current = Arrays.stream(children)
			.filter(element -> element.getElementType() == IJavaElement.FIELD || element.getElementType() == IJavaElement.METHOD)
			.filter(this::isVisible)
			.map(this::toProposal);
		List<String> superTypes = new ArrayList<>();
		try {
			superTypes.add(type.getSuperclassName());
			superTypes.addAll(Arrays.asList(type.getSuperInterfaceNames()));
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return Stream.concat(current, superTypes.stream()
				.filter(Objects::nonNull)
				.map(typeName -> {
					int index = typeName.lastIndexOf('.');
					char[] packageName = index >= 0 ? typeName.substring(0, index).toCharArray() : null;
					char[] simpleName = index >= 0 ? typeName.substring(index + 1, typeName.length()).toCharArray() : typeName.toCharArray();
					List<IType> types = new ArrayList<>();
					try {
						searchEngine.searchAllTypeNames(packageName, SearchPattern.R_EXACT_MATCH, simpleName, SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.TYPE, scope, new TypeNameMatchRequestor() {
							@Override
							public void acceptTypeNameMatch(TypeNameMatch match) {
								types.add(match.getType());
							}
						}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
					} catch (JavaModelException ex) {
						ILog.get().error(ex.getMessage(), ex);
					}
					return types;
				}).flatMap(List::stream)
				.map(t -> processMembers(t, searchEngine, scope))
				.flatMap(Function.identity()));
	}

	private String getSignature(IMethodBinding method) {
		return method.getName() + '(' +
				Arrays.stream(method.getParameterTypes()).map(ITypeBinding::getName).collect(Collectors.joining(","))
				+ ')';
	}

	private static boolean findInSupers(ITypeBinding root, ITypeBinding toFind) {
		ITypeBinding superFind = toFind.getErasure();
		if( superFind != null ) {
			String keyToFind = superFind.getKey();
			return findInSupers(root, keyToFind);
		}
		return false;
	}

	private static boolean findInSupers(ITypeBinding root, String keyOfTypeToFind) {
		String keyToFind = keyOfTypeToFind;
		Queue<ITypeBinding> toCheck = new LinkedList<>();
		Set<String> alreadyChecked = new HashSet<>();
		toCheck.add(root.getErasure());
		while (!toCheck.isEmpty()) {
			ITypeBinding current = toCheck.poll();
			String currentKey = current.getErasure().getKey();
			if (alreadyChecked.contains(currentKey)) {
				continue;
			}
			alreadyChecked.add(currentKey);
			if (currentKey.equals(keyToFind)) {
				return true;
			}
			for (ITypeBinding superInterface : current.getInterfaces()) {
				toCheck.add(superInterface);
			}
			if (current.getSuperclass() != null) {
				toCheck.add(current.getSuperclass());
			}
		}
		return false;
	}

	private CompletionProposal toProposal(IBinding binding) {
		return toProposal(binding, binding.getName());
	}

	private CompletionProposal toProposal(IBinding binding, String completion) {
		if (binding instanceof ITypeBinding && binding.getJavaElement() instanceof IType type) {
			return toProposal(type);
		}

		int kind = -1;
		boolean inJavadoc = DOMCompletionUtil.findParent(this.toComplete, new int [] { ASTNode.JAVADOC }) != null;
		if (binding instanceof ITypeBinding) {
			kind = CompletionProposal.TYPE_REF;
		} else if (binding instanceof IMethodBinding m) {
			if (DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.EXPRESSION_METHOD_REFERENCE }) != null) {
				kind = CompletionProposal.METHOD_NAME_REFERENCE;
			} else if (m.getDeclaringClass() != null && m.getDeclaringClass().isAnnotation()) {
				kind = CompletionProposal.ANNOTATION_ATTRIBUTE_REF;
			} else {
				kind = CompletionProposal.METHOD_REF;
			}
		} else if (binding instanceof IVariableBinding variableBinding) {
			if (variableBinding.isField()) {
				kind = CompletionProposal.FIELD_REF;
			} else {
				kind = CompletionProposal.LOCAL_VARIABLE_REF;
			}
		}

		DOMInternalCompletionProposal res = createProposal(kind);
		res.setName(binding.getName().toCharArray());
		if (kind == CompletionProposal.METHOD_REF) {
			completion += "()"; //$NON-NLS-1$
		}
		res.setCompletion(completion.toCharArray());
		res.setFlags(binding.getModifiers());

		boolean inheritedValue = false;
		if (kind == CompletionProposal.METHOD_REF || kind == CompletionProposal.METHOD_NAME_REFERENCE) {
			var methodBinding = (IMethodBinding) binding;

			if (inJavadoc) {
				// the completion text is completely different from method invocations,
				// since we add the type names instead of guessing the parameters
				StringBuilder javadocCompletion = new StringBuilder();
				javadocCompletion.append(binding.getName().toCharArray());
				javadocCompletion.append('(');
				boolean isVarargs = methodBinding.isVarargs();
				for (int p=0, ln=methodBinding.getParameterTypes().length; p < ln; p++) {
					if (p>0) javadocCompletion.append(", "); //$NON-NLS-1$
					ITypeBinding argTypeBinding = methodBinding.getParameterTypes()[p];
					if (isVarargs && p == ln - 1)  {
						javadocCompletion.append(argTypeBinding.getElementType().getName());
						javadocCompletion.append("..."); //$NON-NLS-1$
					} else {
						javadocCompletion.append(argTypeBinding.getName());
					}
				}
				javadocCompletion.append(')');
				res.setCompletion(javadocCompletion.toString().toCharArray());
			}

			if (methodBinding.isConstructor()) {
				res.setIsContructor(true);
			}

			var paramNames = DOMCompletionEngineMethodDeclHandler.findVariableNames(methodBinding);
			if (paramNames.isEmpty()) {
				res.setParameterNames(null);
			} else {
				res.setParameterNames(paramNames.stream().map(String::toCharArray).toArray(i -> new char[i][]));
			}
			res.setParameterTypeNames(Stream.of(methodBinding.getParameterNames()).map(String::toCharArray).toArray(char[][]::new));
			res.setSignature(SignatureUtils.getSignatureChar(methodBinding));
			if (!methodBinding.getDeclaringClass().getQualifiedName().isEmpty()) {
				res.setDeclarationSignature(Signature
						.createTypeSignature(methodBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
						.toCharArray());
			}

			if (Modifier.isStatic(methodBinding.getModifiers())
				&& this.toComplete.getLocationInParent() != QualifiedName.NAME_PROPERTY && this.toComplete.getLocationInParent() != FieldAccess.NAME_PROPERTY
				&& !isStaticallyImported(binding)) {
				ITypeBinding topLevelClass = methodBinding.getDeclaringClass();
				while (topLevelClass.getDeclaringClass() != null) {
					topLevelClass = topLevelClass.getDeclaringClass();
				}
				final ITypeBinding finalizedTopLevelClass = topLevelClass;
				ITypeBinding methodTypeBinding = methodBinding.getDeclaringClass();
				AbstractTypeDeclaration parentTypeDecl = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete);
				if (parentTypeDecl != null) {
					ITypeBinding completionContextTypeBinding = parentTypeDecl.resolveBinding();
					while (completionContextTypeBinding != null) {
						if (completionContextTypeBinding.getErasure().getKey().equals(methodTypeBinding.getErasure().getKey())) {
							inheritedValue = true;
							break;
						}
						completionContextTypeBinding = completionContextTypeBinding.getSuperclass();
					}
				}

				boolean isMethodInCurrentCU = ((List<AbstractTypeDeclaration>)this.unit.types()).stream().anyMatch(typeDecl -> typeDecl.getName().toString().equals(finalizedTopLevelClass.getName()));
				if (!inheritedValue && !isMethodInCurrentCU) {
					if (this.qualifiedPrefix.equals(this.prefix) && !this.javaProject.getOption(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, true).equals(JavaCore.DISABLED)) {
						res.setRequiredProposals(new CompletionProposal[] { toStaticImportProposal(methodBinding) });
					} else {
						ITypeBinding directParentClass = methodBinding.getDeclaringClass();
						res.setRequiredProposals(new CompletionProposal[] { toStaticImportProposal(directParentClass) });
						StringBuilder builder = new StringBuilder(new String(res.getCompletion()));
						builder.insert(0, '.');
						builder.insert(0, directParentClass.getName());
						res.setCompletion(builder.toString().toCharArray());
					}
				}
			}
		} else if (kind == CompletionProposal.LOCAL_VARIABLE_REF) {
			var variableBinding = (IVariableBinding) binding;
			res.setSignature(
					Signature.createTypeSignature(variableBinding.getType().getQualifiedName().toCharArray(), true)
							.toCharArray());
		} else if (kind == CompletionProposal.FIELD_REF) {
			var variableBinding = (IVariableBinding) binding;
			ITypeBinding declaringClass = variableBinding.getDeclaringClass();
			res.setSignature(
					Signature.createTypeSignature(variableBinding.getType().getQualifiedName().toCharArray(), true)
							.toCharArray());
			if (declaringClass != null && !declaringClass.getQualifiedName().isEmpty()) {
				char[] declSignature = Signature
						.createTypeSignature(
								declaringClass.getQualifiedName().toCharArray(), true)
						.toCharArray();
				res.setDeclarationSignature(declSignature);
			} else {
				res.setDeclarationSignature(new char[0]);
			}

			if ((variableBinding.getModifiers() & Flags.AccStatic) != 0) {
				ITypeBinding topLevelClass = variableBinding.getDeclaringClass();
				while (topLevelClass.getDeclaringClass() != null) {
					topLevelClass = topLevelClass.getDeclaringClass();
				}
				final ITypeBinding finalizedTopLevelClass = topLevelClass;
				ITypeBinding variableTypeBinding = variableBinding.getDeclaringClass();
				AbstractTypeDeclaration parentTypeDecl = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete);
				if (parentTypeDecl != null) {
					ITypeBinding completionContextTypeBinding = parentTypeDecl.resolveBinding();
					while (completionContextTypeBinding != null) {
						if (completionContextTypeBinding.getErasure().getKey().equals(variableTypeBinding.getErasure().getKey())) {
							inheritedValue = true;
							break;
						}
						completionContextTypeBinding = completionContextTypeBinding.getSuperclass();
					}
				}
				boolean isVariableInCurrentCU = ((List<AbstractTypeDeclaration>)this.unit.types()).stream().anyMatch(typeDecl -> typeDecl.getName().toString().equals(finalizedTopLevelClass.getName()));
				if (!inheritedValue && !isVariableInCurrentCU) {
					if (this.qualifiedPrefix.equals(this.prefix) && !this.javaProject.getOption(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, true).equals(JavaCore.DISABLED)) {
						if (!isStaticallyImported(variableBinding) && !(variableBinding.isEnumConstant() && Set.of(SwitchCase.EXPRESSION_PROPERTY, SwitchCase.EXPRESSIONS2_PROPERTY).contains(this.toComplete.getLocationInParent()))) {
							res.setRequiredProposals(new CompletionProposal[] { toStaticImportProposal(variableBinding) });
						}
					} else {
						ITypeBinding directParentClass = variableBinding.getDeclaringClass();
						res.setRequiredProposals(new CompletionProposal[] { toStaticImportProposal(directParentClass) });
					}
				}
			}
		} else if (kind == CompletionProposal.TYPE_REF) {
			var typeBinding = (ITypeBinding) binding;
			res.setSignature(
					Signature.createTypeSignature(typeBinding.getQualifiedName().toCharArray(), true).toCharArray());
		} else if (kind == CompletionProposal.ANNOTATION_ATTRIBUTE_REF) {
			var methodBinding = (IMethodBinding) binding;
			StringBuilder annotationCompletion = new StringBuilder(completion);
			boolean surroundWithSpaces = JavaCore.INSERT.equals(this.unit.getJavaElement().getJavaProject().getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, true));
			if (surroundWithSpaces) {
				annotationCompletion.append(' ');
			}
			annotationCompletion.append('=');
			if (surroundWithSpaces) {
				annotationCompletion.append(' ');
			}
			res.setCompletion(annotationCompletion.toString().toCharArray());
			res.setSignature(Signature.createTypeSignature(qualifiedTypeName(methodBinding.getReturnType()), true)
					.toCharArray());
			res.setReceiverSignature(Signature
					.createTypeSignature(methodBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
					.toCharArray());
			res.setDeclarationSignature(Signature
					.createTypeSignature(methodBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
					.toCharArray());
		} else {
			res.setSignature(new char[] {});
			res.setReceiverSignature(new char[] {});
			res.setDeclarationSignature(new char[] {});
		}

		if (this.toComplete instanceof SimpleName && !this.toComplete.getLocationInParent().getId().equals(QualifiedName.QUALIFIER_PROPERTY.getId()) && !this.prefix.isEmpty() && !inJavadoc) {
			res.setReplaceRange(this.toComplete.getStartPosition(), this.offset);
			res.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		} else {
			setRange(res);
		}
		var element = binding.getJavaElement();
		if (element != null) {
			res.setDeclarationTypeName(((IType)element.getAncestor(IJavaElement.TYPE)).getFullyQualifiedName().toCharArray());
			res.setDeclarationPackageName(element.getAncestor(IJavaElement.PACKAGE_FRAGMENT).getElementName().toCharArray());
		}

		boolean isInQualifiedName = this.toComplete.getLocationInParent() == QualifiedName.NAME_PROPERTY || this.toComplete.getLocationInParent() == FieldAccess.NAME_PROPERTY;
		res.setRelevance(RelevanceConstants.R_DEFAULT +
				RelevanceConstants.R_RESOLVED +
				RelevanceConstants.R_INTERESTING +
				(res.isConstructor() ? 0 : RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), binding.getName().toCharArray(), this.assistOptions)) +
				RelevanceUtils.computeRelevanceForExpectingType(binding instanceof ITypeBinding typeBinding ? typeBinding :
					binding instanceof IMethodBinding methodBinding ? methodBinding.getReturnType() :
					binding instanceof IVariableBinding variableBinding ? variableBinding.getType() :
					this.toComplete.getAST().resolveWellKnownType(Object.class.getName()), this.expectedTypes) +
				(isInQualifiedName || res.getRequiredProposals() != null || inJavadoc ? 0 : RelevanceUtils.computeRelevanceForQualification(false, this.prefix, this.qualifiedPrefix)) +
				RelevanceConstants.R_NON_RESTRICTED +
				RelevanceUtils.computeRelevanceForInheritance(this.qualifyingType, binding) +
				((insideQualifiedReference() && !staticOnly() && !Modifier.isStatic(binding.getModifiers())) || (inJavadoc && !res.isConstructor()) ? RelevanceConstants.R_NON_STATIC : 0) +
				(binding instanceof IVariableBinding field && field.isEnumConstant() ? RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT : 0)
				);
		if (res.getRequiredProposals() != null) {
			for (CompletionProposal req : res.getRequiredProposals()) {
				req.setRelevance(res.getRelevance());
			}
		}
		return res;
	}

	private boolean isStaticallyImported(IBinding binding) {
		return ((List<ImportDeclaration>)unit.imports()).stream() //
			.filter(ImportDeclaration::isStatic) //
			.map(ImportDeclaration::resolveBinding) //
			.anyMatch(importBinding -> binding.isEqualTo(importBinding) || getDeclaringClass(binding).isEqualTo(importBinding));
	}

	private boolean insideQualifiedReference() {
		return this.toComplete instanceof QualifiedName ||
			(this.toComplete instanceof SimpleName simple && (simple.getParent() instanceof QualifiedName || simple.getParent() instanceof FieldAccess));
	}

	private boolean staticOnly() {
		if (this.toComplete.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
			return DOMCodeSelector.resolveBinding(((QualifiedName)this.toComplete.getParent()).getQualifier()) instanceof ITypeBinding;
		}
		return false;
	}

	private String qualifiedTypeName(ITypeBinding typeBinding) {
		if (typeBinding.isTypeVariable()) {
			return typeBinding.getName();
		} else {
			return typeBinding.getQualifiedName();
		}
	}

	private CompletionProposal toProposal(IType type) {
		DOMInternalCompletionProposal res = createProposal(CompletionProposal.TYPE_REF);
		char[] simpleName = type.getElementName().toCharArray();
		char[] signature = Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray();

		res.setSignature(signature);

		// set owner package
		IJavaElement cursor = type;
		while (cursor != null && !(cursor instanceof IPackageFragment)) {
			cursor = cursor.getParent();
		}
		IPackageFragment packageFrag = (IPackageFragment) cursor;
		if (packageFrag != null) {
			res.setDeclarationSignature(packageFrag.getElementName().toCharArray());
		}

		// set completion, considering nested types
		cursor = type;
		StringBuilder completion = new StringBuilder();
		AbstractTypeDeclaration parentTypeDeclaration = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete);
		if (parentTypeDeclaration != null
				&& parentTypeDeclaration.resolveBinding() != null
				&& parentTypeDeclaration.resolveBinding().getJavaElement() != null
				&& type.getFullyQualifiedName().equals(((IType)parentTypeDeclaration.resolveBinding().getJavaElement()).getFullyQualifiedName())) {
			completion.insert(0, cursor.getElementName());
		} else {
			ASTNode currentName = this.toComplete instanceof Name ? this.toComplete : null;
			while (cursor instanceof IType currentType && (currentName == null || !Objects.equals(currentName.toString(), currentType.getFullyQualifiedName()))) {
				if (!completion.isEmpty()) {
					completion.insert(0, '.');
				}
				completion.insert(0, cursor.getElementName());
				cursor = cursor.getParent();
				if (currentName != null && currentName.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
					currentName = ((QualifiedName)currentName.getParent()).getQualifier();
				} else {
					currentName = null;
				}
			}
		}
		AbstractTypeDeclaration parentType = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete);
		Javadoc javadoc = (Javadoc) DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.JAVADOC });
		if (parentType != null || javadoc != null) {
			IPackageBinding currentPackageBinding = parentType == null ? null : parentType.resolveBinding().getPackage();
			if (packageFrag != null && (currentPackageBinding == null
					|| (!packageFrag.getElementName().equals(currentPackageBinding.getName())
							&& !packageFrag.getElementName().equals("java.lang")))) { //$NON-NLS-1$
				completion.insert(0, '.');
				completion.insert(0, packageFrag.getElementName());
			}
		} else {
			// in imports list
			int lastOffset = this.toComplete.getStartPosition() + this.toComplete.getLength();
			if (lastOffset >= this.textContent.length() || this.textContent.charAt(lastOffset) != ';') {
				completion.append(';');
			}
		}
		res.setCompletion(completion.toString().toCharArray());

		if (this.toComplete instanceof FieldAccess || this.prefix.isEmpty()) {
			res.setReplaceRange(this.offset, this.offset);
		} else if (this.toComplete instanceof MarkerAnnotation) {
			res.setReplaceRange(this.toComplete.getStartPosition() + 1, this.toComplete.getStartPosition() + this.toComplete.getLength());
		} else if (this.toComplete instanceof SimpleName currentName && FAKE_IDENTIFIER.equals(currentName.toString())) {
			res.setReplaceRange(this.offset, this.offset);
		} else if (this.toComplete instanceof SimpleName) {
			res.setReplaceRange(this.toComplete.getStartPosition(), this.toComplete.getStartPosition() + this.toComplete.getLength());
		} else if (this.toComplete instanceof ThisExpression thisExpression
				&& thisExpression.getQualifier() != null
				&& this.offset > (thisExpression.getQualifier().getStartPosition() + thisExpression.getQualifier().getLength())) {
			setRange(res);
		} else {
			res.setReplaceRange(this.toComplete.getStartPosition(), this.offset);
		}
		try {
			res.setFlags(type.getFlags());
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		if (this.toComplete instanceof SimpleName) {
			res.setTokenRange(this.toComplete.getStartPosition(), this.toComplete.getStartPosition() + this.toComplete.getLength());
		} else if (this.toComplete instanceof MarkerAnnotation) {
			res.setTokenRange(this.offset, this.offset);
		}
		boolean nodeInImports = DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.IMPORT_DECLARATION }) != null;

		IType topLevelClass = type;
		while (topLevelClass.getDeclaringType() instanceof IType parentIType) {
			topLevelClass = parentIType;
		}
		final IType finalizedTopLevelClass = topLevelClass;

		boolean fromCurrentCU = ((List<AbstractTypeDeclaration>)this.unit.types()).stream().anyMatch(typeDecl -> typeDecl.resolveBinding().getQualifiedName().equals(finalizedTopLevelClass.getFullyQualifiedName()));
		boolean inSamePackage = false;
		boolean typeIsImported = ((List<ImportDeclaration>)this.unit.imports()).stream().anyMatch(importDecl -> {
			return importDecl.getName().toString().equals(type.getFullyQualifiedName());
		});
		PackageDeclaration packageDeclaration = this.unit.getPackage();
		if (packageDeclaration != null) {
			inSamePackage = packageDeclaration.getName().toString().equals(type.getPackageFragment().getElementName());
		} else {
			inSamePackage = type.getPackageFragment().getElementName().isEmpty();
		}
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceUtils.computeRelevanceForInheritance(this.qualifyingType, type)
				+ RelevanceUtils.computeRelevanceForQualification(!type.getFullyQualifiedName().startsWith("java.") && !nodeInImports && !fromCurrentCU && !inSamePackage && !typeIsImported, this.prefix, this.qualifiedPrefix)
				+ (type.getFullyQualifiedName().startsWith("java.") ? RelevanceConstants.R_JAVA_LIBRARY : 0)
				+ (expectedTypes.getExpectedTypes().stream().map(ITypeBinding::getQualifiedName).anyMatch(type.getFullyQualifiedName()::equals) ? RelevanceConstants.R_EXACT_EXPECTED_TYPE :
					expectedTypes.getExpectedTypes().stream().map(ITypeBinding::getQualifiedName).anyMatch(Object.class.getName()::equals) ? RelevanceConstants.R_EXPECTED_TYPE :
					0)
				+ RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), simpleName, this.assistOptions);
		try {
			if (type.isAnnotation()) {
				ASTNode current = this.toComplete;
				while (current instanceof Name) {
					current = current.getParent();
				}
				if (current instanceof Annotation annotation) {
					relevance += RelevanceConstants.R_ANNOTATION;
					IAnnotation targetAnnotation = type.getAnnotation(Target.class.getName());
					if (targetAnnotation == null || !targetAnnotation.exists()) {
						// On Javadoc for @Target: "If a Target meta-annotation is not present on an annotation type declaration,
						// the declared type may be used on any program element."
						relevance += RelevanceConstants.R_TARGET;
					} else {
						var memberValuePairs = targetAnnotation.getMemberValuePairs();
						if (memberValuePairs != null) {
							if (Stream.of(memberValuePairs)
								.filter(memberValue -> "value".equals(memberValue.getMemberName()))
								.map(IMemberValuePair::getValue)
								.anyMatch(target -> matchHostType(annotation.getParent(), target))) {
								relevance += RelevanceConstants.R_TARGET;
							}
						}
					}
				}
			}
		} catch (JavaModelException ex) {
			ILog.get().warn(ex.getMessage(), ex);
		}
		if (isInExtendsOrImplements(this.toComplete) != null) {
			try {
				if (type.isAnnotation()) {
					relevance += RelevanceConstants.R_ANNOTATION;
				}
				if (type.isInterface()) {
					relevance += RelevanceConstants.R_INTERFACE;
				}
				if (type.isClass()) {
					relevance += RelevanceConstants.R_CLASS;
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		res.setRelevance(relevance);
		if (parentType != null) {
			String packageName = ""; //$NON-NLS-1$
			PackageDeclaration packageDecl = this.unit.getPackage();
			if (packageDecl != null) {
				packageName = packageDecl.getName().toString();
			}
			if (!packageName.equals(type.getPackageFragment().getElementName()) && !new String(res.getCompletion()).equals(type.getFullyQualifiedName('.'))) {
				// propose importing the type
				res.setRequiredProposals(new CompletionProposal[] { toImportProposal(simpleName, signature, type.getPackageFragment().getElementName().toCharArray()) });
			}
		}
		return res;
	}

	private static boolean matchHostType(ASTNode host, Object targetAnnotationElementValue) {
		return false;
	}

	private CompletionProposal toSuperConstructorProposal(IMethodBinding superConstructor) {
		CompletionProposal res = toProposal(superConstructor);
		res.setName(Keywords.SUPER);
		res.setCompletion(CharOperation.concat(Keywords.SUPER, new char[] {'(', ')'}));
		res.setTokenRange(res.getReplaceStart(), res.getReplaceEnd());

		res.setRelevance(RelevanceConstants.R_DEFAULT +
				RelevanceConstants.R_RESOLVED +
				RelevanceConstants.R_INTERESTING +
				RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), Keywords.SUPER, this.assistOptions) +
				RelevanceConstants.R_NON_RESTRICTED
				);

		return res;
	}

	private CompletionProposal toProposal(IJavaElement element) {
		if (element instanceof IType type) {
			return toProposal(type);
		}
		DOMInternalCompletionProposal res = null;
		IType parentType = (IType)element.getAncestor(IJavaElement.TYPE);
		if (element instanceof IField field) {
			res = createProposal(CompletionProposal.FIELD_REF);
			res.setName(field.getElementName().toCharArray());
			try {
				res.setSignature(field.getTypeSignature().toCharArray());
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			res.setCompletion(field.getElementName().toCharArray());
			setRange(res);
			res.setRelevance(RelevanceConstants.R_DEFAULT + 
					RelevanceConstants.R_RESOLVED +
					RelevanceConstants.R_INTERESTING +
					RelevanceConstants.R_CASE +
					RelevanceConstants.R_NON_STATIC +
					RelevanceConstants.R_NON_RESTRICTED +
					RelevanceConstants.R_NO_PROBLEMS);
		}
		if (element instanceof IMethod method) {
			res = createProposal(CompletionProposal.METHOD_REF);
			try {
				res.setSignature(method.getSignature().toCharArray());
				res.setParameterNames(Arrays.stream(method.getParameterNames()).map(String::toCharArray).toArray(char[][]::new));
				res.setParameterTypeNames(Arrays.stream(method.getParameterTypes()).map(String::toCharArray).toArray(char[][]::new));
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			res.setName(method.getElementName().toCharArray());
			res.setCompletion((method.getElementName() + "()").toCharArray());
			setRange(res);
			res.setRelevance(RelevanceConstants.R_DEFAULT +
				RelevanceConstants.R_RESOLVED +
				RelevanceConstants.R_INTERESTING +
				RelevanceConstants.R_CASE +
				RelevanceConstants.R_NON_STATIC +
				RelevanceConstants.R_NON_RESTRICTED +
				RelevanceConstants.R_NO_PROBLEMS);
		}
		if (res != null) {
			if (element instanceof IMember member) {
				try {
					res.setFlags(member.getFlags());
				} catch (JavaModelException ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
			res.setDeclarationSignature(SignatureUtils.createSignature(parentType).toCharArray());
			res.setDeclarationTypeName(parentType.getFullyQualifiedName().toCharArray());
			res.setDeclarationPackageName(element.getAncestor(IJavaElement.PACKAGE_FRAGMENT).getElementName().toCharArray());
		}
		return res;
	}

	private CompletionProposal toNewMethodProposal(ITypeBinding parentType, String newMethodName) {
		DOMInternalCompletionProposal res =  createProposal(CompletionProposal.POTENTIAL_METHOD_DECLARATION);
		res.setDeclarationSignature(SignatureUtils.getSignatureChar(parentType));
		res.setSignature(Signature.createMethodSignature(CharOperation.NO_CHAR_CHAR, Signature.createCharArrayTypeSignature(VOID, true)));
		res.setDeclarationPackageName(parentType.getPackage().getName().toCharArray());
		res.setDeclarationTypeName(parentType.getQualifiedName().toCharArray());
		res.setTypeName(VOID);
		res.setName(newMethodName.toCharArray());
		res.setCompletion(newMethodName.toCharArray());
		res.setFlags(Flags.AccPublic);
		setRange(res);
		int relevance = RelevanceConstants.R_DEFAULT;
		relevance += RelevanceConstants.R_RESOLVED;
		relevance += RelevanceConstants.R_INTERESTING;
		relevance += RelevanceConstants.R_NON_RESTRICTED;
		res.setRelevance(relevance);
		return res;
	}

	private List<CompletionProposal> toConstructorProposals(IType type, ASTNode referencedFrom, boolean exactType) {

		List<CompletionProposal> proposals = new ArrayList<>();

		AbstractTypeDeclaration parentType = (AbstractTypeDeclaration)DOMCompletionUtil.findParent(referencedFrom, new int[] {ASTNode.ANNOTATION_TYPE_DECLARATION, ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION});
		if (parentType == null) {
			return proposals;
		}

		ITypeBinding referencedFromBinding = parentType.resolveBinding();
		boolean includePrivate = referencedFromBinding.getKey().equals(type.getKey());
		MethodDeclaration methodDeclaration = (MethodDeclaration)DOMCompletionUtil.findParent(referencedFrom, new int[] {ASTNode.METHOD_DECLARATION});
		// you can reference protected fields/methods from a static method,
		// as long as those protected fields/methods are declared in the current class.
		// otherwise, the (inherited) fields/methods can only be accessed in non-static methods.
		boolean includeProtected;
		if (referencedFromBinding.getKey().equals(type.getKey())) {
			includeProtected = true;
		} else if (methodDeclaration != null
				&& (methodDeclaration.getModifiers() & Flags.AccStatic) != 0) {
			includeProtected = false;
		} else {
			includeProtected = findInSupers(referencedFromBinding, type.getKey());
		}

		IPackageFragment packageFragment = (IPackageFragment)type.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		String packageKey = packageFragment == null ? "" : packageFragment.getElementName().replace('.', '/'); //$NON-NLS-1$

		boolean isInterface = false;
		try {
			isInterface = type.isInterface();
		} catch (JavaModelException e) {
			// do nothing
		}


		if (!this.requestor.isIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION) && isInterface) {
			// create an anonymous declaration: `new MyInterface() { }`;
			proposals.add(toAnonymousConstructorProposal(type));
			// TODO: JDT allows completing the constructors declared on an abstract class,
			// without adding a body to these instance creations.
			// This doesn't make sense, since the abstract methods need to be implemented.
			// We should consider making those completion items here instead
		} else {
			try {
				List<IMethod> constructors = Stream.of(type.getMethods()).filter(method -> {
						try {
							return method.isConstructor();
						} catch (JavaModelException e) {
							return false;
						}
					}).toList();
				if (!constructors.isEmpty()) {
					for (IMethod constructor: constructors) {
						if (
								// public
								(constructor.getFlags() & Flags.AccPublic) != 0
								// protected
								|| (includeProtected && (constructor.getFlags() & Flags.AccProtected) != 0)
								// private
								|| (includePrivate && (constructor.getFlags() & Flags.AccPrivate) != 0)
								// package private
								||((constructor.getFlags() & (Flags.AccPrivate | Flags.AccProtected | Flags.AccPublic)) == 0 && packageKey.equals(referencedFromBinding.getPackage().getKey()))) {
							proposals.add(toConstructorProposal(constructor, exactType));
						}
					}
				} else {
					proposals.add(toDefaultConstructorProposal(type, exactType));
				}
			} catch (JavaModelException e) {
				ILog.get().error("Model exception while trying to collect constructors for completion", e); //$NON-NLS-1$
			}
		}

		return proposals;
	}

	private CompletionProposal toConstructorProposal(IMethod method, boolean isExactType) throws JavaModelException {
		DOMInternalCompletionProposal res = createProposal(CompletionProposal.CONSTRUCTOR_INVOCATION);
		IType declaringClass = method.getDeclaringType();
		char[] simpleName = method.getElementName().toCharArray();
		res.setCompletion(new char[] {'(', ')'});
		res.setName(simpleName);

		char[] signature = method.getKey().substring(method.getKey().indexOf('.') + 1).toCharArray();
		res.setSignature(signature);
		res.setOriginalSignature(signature);

		IPackageFragment packageFragment = (IPackageFragment)declaringClass.getAncestor(IJavaElement.PACKAGE_FRAGMENT);

		res.setDeclarationSignature(Signature.createTypeSignature(declaringClass.getFullyQualifiedName(), true).toCharArray());
		res.setDeclarationTypeName(simpleName);
		res.setDeclarationPackageName(packageFragment.getElementName().toCharArray());
		res.setParameterPackageNames(CharOperation.NO_CHAR_CHAR);
		res.setParameterTypeNames(CharOperation.NO_CHAR_CHAR);

		if (method.getParameterNames().length == 0) {
			res.setParameterNames(CharOperation.NO_CHAR_CHAR);
		} else {
			char[][] paramNamesCharChar = Stream.of(method.getParameterNames()) //
					.map(String::toCharArray)
					.toArray(char[][]::new);
			res.setParameterNames(paramNamesCharChar);
		}

		res.setIsContructor(true);
		if (declaringClass.getTypeParameters() != null && declaringClass.getTypeParameters().length > 0) {
			res.setDeclarationTypeVariables(Stream.of(declaringClass.getTypeParameters()).map(a -> a.getElementName().toCharArray()).toArray(char[][]::new));
		}
		res.setCompatibleProposal(true);

		res.setReplaceRange(this.offset, this.offset);
		res.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		res.setFlags(method.getFlags());

		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ (isExactType ? RelevanceConstants.R_EXACT_EXPECTED_TYPE : 0)
				+ RelevanceConstants.R_UNQUALIFIED
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceConstants.R_CONSTRUCTOR
				+ RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), simpleName, this.assistOptions);
		res.setRelevance(relevance);

		CompletionProposal typeProposal = toProposal(declaringClass);
		if (this.toComplete instanceof SimpleName) {
			typeProposal.setReplaceRange(this.toComplete.getStartPosition(), this.offset);
			typeProposal.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		} else {
			typeProposal.setReplaceRange(this.offset, this.offset);
			typeProposal.setTokenRange(this.offset, this.offset);
		}
		typeProposal.setRequiredProposals(null);
		typeProposal.setRelevance(relevance);

		res.setRequiredProposals(new CompletionProposal[] { typeProposal });
		return res;
	}

	private CompletionProposal toDefaultConstructorProposal(IType type, boolean isExactType) throws JavaModelException {
		DOMInternalCompletionProposal res = createProposal(CompletionProposal.CONSTRUCTOR_INVOCATION);
		char[] simpleName = type.getElementName().toCharArray();
		res.setCompletion(new char[] {'(', ')'});
		res.setName(simpleName);

		char[] signature = Signature.createMethodSignature(CharOperation.NO_CHAR_CHAR, new char[]{ 'V' });
		res.setSignature(signature);
		res.setOriginalSignature(signature);

		IPackageFragment packageFragment = (IPackageFragment)type.getAncestor(IJavaElement.PACKAGE_FRAGMENT);

		res.setDeclarationSignature(Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray());
		res.setDeclarationTypeName(simpleName);
		res.setDeclarationPackageName(packageFragment.getElementName().toCharArray());
		res.setParameterPackageNames(CharOperation.NO_CHAR_CHAR);
		res.setParameterTypeNames(CharOperation.NO_CHAR_CHAR);

		res.setParameterNames(CharOperation.NO_CHAR_CHAR);

		res.setIsContructor(true);
		if (type.getTypeParameters() != null && type.getTypeParameters().length > 0) {
			res.setDeclarationTypeVariables(Stream.of(type.getTypeParameters()).map(a -> a.getElementName().toCharArray()).toArray(char[][]::new));
		}
		res.setCompatibleProposal(true);

		res.setReplaceRange(this.offset, this.offset);
		res.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		res.setFlags(type.getFlags() & (Flags.AccPublic | Flags.AccPrivate | Flags.AccProtected));

		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ (isExactType ? RelevanceConstants.R_EXACT_EXPECTED_TYPE : 0)
				+ RelevanceConstants.R_UNQUALIFIED
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceConstants.R_CONSTRUCTOR;
		res.setRelevance(relevance);

		CompletionProposal typeProposal = toProposal(type);
		if (this.toComplete instanceof SimpleName) {
			typeProposal.setReplaceRange(this.toComplete.getStartPosition(), this.offset);
			typeProposal.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		} else {
			typeProposal.setReplaceRange(this.offset, this.offset);
			typeProposal.setTokenRange(this.offset, this.offset);
		}
		typeProposal.setRequiredProposals(null);
		typeProposal.setRelevance(relevance);

		res.setRequiredProposals(new CompletionProposal[] { typeProposal });
		return res;
	}

	private CompletionProposal toAnonymousConstructorProposal(IType type) {
		DOMInternalCompletionProposal res = createProposal(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION);
		res.setDeclarationSignature(Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray());
		res.setDeclarationKey(type.getKey().toCharArray());
		res.setSignature(
				CompletionEngine.createMethodSignature(
						CharOperation.NO_CHAR_CHAR,
						CharOperation.NO_CHAR_CHAR,
						CharOperation.NO_CHAR,
						CharOperation.NO_CHAR));

		IPackageFragment packageFragment = (IPackageFragment)type.getAncestor(IJavaElement.PACKAGE_FRAGMENT);

		res.setDeclarationPackageName(packageFragment.getElementName().toCharArray());
		res.setDeclarationTypeName(type.getElementName().toCharArray());
		res.setName(type.getElementName().toCharArray());

		int relevance = RelevanceConstants.R_DEFAULT;
		relevance += RelevanceConstants.R_RESOLVED;
		relevance += RelevanceConstants.R_INTERESTING;
		relevance += RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), type.getElementName().toCharArray(), this.assistOptions);
		relevance += RelevanceConstants.R_EXACT_EXPECTED_TYPE;
		relevance += RelevanceConstants.R_UNQUALIFIED;
		relevance += RelevanceConstants.R_NON_RESTRICTED;
		if (packageFragment.getElementName().startsWith("java.")) { //$NON-NLS-1$
			relevance += RelevanceConstants.R_JAVA_LIBRARY;
		}

		try {
			if(type.isClass()) {
				relevance += RelevanceConstants.R_CLASS;
//				relevance += computeRelevanceForException(typeName); // TODO:
			} else if(type.isEnum()) {
				relevance += RelevanceConstants.R_ENUM;
			} else if(type.isInterface()) {
				relevance += RelevanceConstants.R_INTERFACE;
			}
		} catch (JavaModelException e) {
			// do nothing
		}

		DOMInternalCompletionProposal typeProposal = createProposal(CompletionProposal.TYPE_REF);
		typeProposal.setDeclarationSignature(packageFragment.getElementName().toCharArray());
		typeProposal.setSignature(Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray());
		typeProposal.setPackageName(packageFragment.getElementName().toCharArray());
		typeProposal.setTypeName(type.getElementName().toCharArray());
		typeProposal.setCompletion(type.getElementName().toCharArray());
		try {
			typeProposal.setFlags(type.getFlags());
		} catch (JavaModelException e) {
			// do nothing
		}
		setRange(typeProposal);
		typeProposal.setRelevance(relevance);
		res.setRequiredProposals( new CompletionProposal[]{typeProposal});

		res.setCompletion(new char[] {'(', ')'});
		res.setFlags(Flags.AccPublic);
		res.setReplaceRange(this.offset, this.offset);
		res.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		res.setRelevance(relevance);
		return res;
	}

	private CompletionProposal toImportProposal(char[] simpleName, char[] signature, char[] packageName) {
		DOMInternalCompletionProposal res = createProposal(CompletionProposal.TYPE_IMPORT);
		res.setName(simpleName);
		res.setSignature(signature);
		res.setPackageName(packageName);
		return res;
	}

	private CompletionProposal toStaticImportProposal(IBinding binding) {
		DOMInternalCompletionProposal res = null;
		if (binding instanceof IMethodBinding methodBinding) {
			res = createProposal(CompletionProposal.METHOD_IMPORT);
			res.setName(methodBinding.getName().toCharArray());
			res.setSignature(SignatureUtils.getSignatureChar(methodBinding));

			res.setDeclarationSignature(SignatureUtils.getSignatureChar(methodBinding.getDeclaringClass()));
			res.setSignature(SignatureUtils.getSignatureChar(methodBinding));
			if(methodBinding != methodBinding.getMethodDeclaration()) {
				res.setOriginalSignature(SignatureUtils.getSignatureChar(methodBinding.getMethodDeclaration()));
			}
			res.setDeclarationPackageName(methodBinding.getDeclaringClass().getPackage().getName().toCharArray());
			res.setDeclarationTypeName(methodBinding.getDeclaringClass().getQualifiedName().toCharArray());
			res.setParameterPackageNames(Stream.of(methodBinding.getParameterTypes())//
					.map(typeBinding -> {
						if (typeBinding.getPackage() != null) {
							return typeBinding.getPackage().getName().toCharArray();
						}
						return CharOperation.NO_CHAR;
					}) //
					.toArray(char[][]::new));
			res.setParameterTypeNames(Stream.of(methodBinding.getParameterTypes())//
					.map(typeBinding -> {
						return typeBinding.getName().toCharArray();
					}) //
					.toArray(char[][]::new));
			if (methodBinding.getReturnType().getPackage() != null) {
				res.setPackageName(methodBinding.getReturnType().getPackage().getName().toCharArray());
			}
			res.setTypeName(methodBinding.getReturnType().getQualifiedName().toCharArray());
			res.setName(methodBinding.getName().toCharArray());
			res.setCompletion(("import static " + methodBinding.getDeclaringClass().getQualifiedName() + "." + methodBinding.getName() + ";\n").toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			res.setFlags(methodBinding.getModifiers());
			res.setAdditionalFlags(CompletionFlags.StaticImport);
			res.setParameterNames(Stream.of(methodBinding.getParameterNames()) //
					.map(String::toCharArray) //
					.toArray(char[][]::new));
		} else if (binding instanceof IVariableBinding variableBinding) {
			res = createProposal(CompletionProposal.FIELD_IMPORT);

			res.setDeclarationSignature(SignatureUtils.getSignatureChar(variableBinding.getDeclaringClass()));
			res.setSignature(Signature.createTypeSignature(variableBinding.getType().getQualifiedName().toCharArray(), true)
					.toCharArray());
			res.setDeclarationPackageName(variableBinding.getDeclaringClass().getPackage().getName().toCharArray());
			res.setDeclarationTypeName(variableBinding.getDeclaringClass().getQualifiedName().toCharArray());
			if (variableBinding.getType().getPackage() != null) {
				res.setPackageName(variableBinding.getType().getPackage().getName().toCharArray());
			}
			res.setTypeName(variableBinding.getType().getQualifiedName().toCharArray());
			res.setName(variableBinding.getName().toCharArray());
			res.setCompletion(("import static " + variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName() + ";\n").toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			res.setFlags(variableBinding.getModifiers());
			res.setAdditionalFlags(CompletionFlags.StaticImport);
		} else if (binding instanceof ITypeBinding typeBinding) {
			// NOTE: slightly different fields are filled out when a type import + qualification is being used in place of a static import
			// That's why we do something different here

			res = createProposal(CompletionProposal.TYPE_IMPORT);
			res.setDeclarationSignature(typeBinding.getPackage().getName().toCharArray());
			res.setSignature(SignatureUtils.getSignatureChar(typeBinding));
			res.setPackageName(typeBinding.getPackage().getName().toCharArray());
			res.setTypeName(typeBinding.getQualifiedName().toCharArray());
			res.setAdditionalFlags(CompletionFlags.Default);

			StringBuilder importCompletionBuilder = new StringBuilder("import "); //$NON-NLS-1$
			importCompletionBuilder.append(typeBinding.getQualifiedName().replace('$', '.'));
			importCompletionBuilder.append(';');
			importCompletionBuilder.append('\n');
			res.setCompletion(importCompletionBuilder.toString().toCharArray());
		}
		if (res != null) {
			CompilationUnit cu = ((CompilationUnit)this.toComplete.getRoot());
			List<ASTNode> imports = cu.imports();
			int place;
			if (!imports.isEmpty()) {
				int lastIndex = imports.size() - 1;
				place = imports.get(lastIndex).getStartPosition() + imports.get(lastIndex).getLength();
			} else if (cu.getPackage() != null) {
				place = cu.getPackage().getStartPosition() + cu.getPackage().getLength();
			} else {
				place = 0;
			}
			if (this.textContent != null && place != 0) {
				if (this.textContent.charAt(place) == '\n') {
					place++;
				} else if (this.textContent.charAt(place) == '\r' && this.textContent.charAt(place + 1) == '\n') {
					place += 2;
				}
			}
			res.setReplaceRange(place, place);
			res.setTokenRange(place, place);
			// relevance is set in invokee, since it's expected to be the same as that of the parent completion proposal
			return res;
		}
		throw new IllegalArgumentException("unexpected binding type: " + binding.getClass()); //$NON-NLS-1$
	}

	private CompletionProposal toAnnotationAttributeRefProposal(IMethodBinding method) {
		CompletionProposal proposal = createProposal(CompletionProposal.ANNOTATION_ATTRIBUTE_REF);
		proposal.setDeclarationSignature(SignatureUtils.getSignatureChar(method.getDeclaringClass()));
		proposal.setSignature(SignatureUtils.getSignatureChar(method.getReturnType()));
		proposal.setName(method.getName().toCharArray());
		// add "=" to completion since it will always be needed
		char[] completion= method.getName().toCharArray();
		if (JavaCore.INSERT.equals(this.javaProject.getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, true))) {
			completion= CharOperation.concat(completion, new char[] {' '});
		}
		completion= CharOperation.concat(completion, new char[] {'='});
		if (JavaCore.INSERT.equals(this.javaProject.getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, true))) {
			completion= CharOperation.concat(completion, new char[] {' '});
		}
		proposal.setCompletion(completion);
		proposal.setFlags(method.getModifiers());
		setRange(proposal);
		if (this.toComplete instanceof SimpleName simpleName) {
			proposal.setReplaceRange(simpleName.getStartPosition(), simpleName.getStartPosition() + simpleName.getLength());
			proposal.setTokenRange(simpleName.getStartPosition(), simpleName.getStartPosition() + simpleName.getLength());
		}
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), method.getName().toCharArray(), this.assistOptions)
				+ RelevanceConstants.R_UNQUALIFIED
				+ RelevanceConstants.R_NON_RESTRICTED;
		proposal.setRelevance(relevance);
		return proposal;
	}

	private CompletionProposal toJavadocBlockTagProposal(char[] blockTag) {
		InternalCompletionProposal res = createProposal(CompletionProposal.JAVADOC_BLOCK_TAG);
		res.setName(blockTag);
		StringBuilder completion = new StringBuilder();
		completion.append('@');
		completion.append(blockTag);
		res.setCompletion(completion.toString().toCharArray());
		setRange(res);
		ASTNode replaceNode = this.toComplete;
		if (replaceNode instanceof TextElement) {
			replaceNode = replaceNode.getParent();
		}
		res.setReplaceRange(replaceNode.getStartPosition(), replaceNode.getStartPosition() + replaceNode.getLength());
		res.setRelevance(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_INTERESTING + RelevanceConstants.R_NON_RESTRICTED);
		return res;
	}

	private CompletionProposal toJavadocInlineTagProposal(char[] inlineTag) {
		InternalCompletionProposal res = createProposal(CompletionProposal.JAVADOC_INLINE_TAG);
		res.setName(inlineTag);
		StringBuilder completion = new StringBuilder();
		completion.append('{');
		completion.append('@');
		completion.append(inlineTag);
		completion.append('}');
		res.setCompletion(completion.toString().toCharArray());
		setRange(res);
		ASTNode replaceNode = this.toComplete;
		if (replaceNode instanceof TextElement) {
			replaceNode = replaceNode.getParent();
		}
		res.setReplaceRange(replaceNode.getStartPosition(), replaceNode.getStartPosition() + replaceNode.getLength());
		res.setRelevance(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_INTERESTING + RelevanceConstants.R_NON_RESTRICTED);
		return res;
	}

	private Map<String, IModuleDescription> getAllJarModuleNames(IJavaProject project) {
		Map<String, IModuleDescription> modules = new HashMap<>();
		try {
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
				if (root instanceof JarPackageFragmentRoot) {
					IModuleDescription desc = root.getModuleDescription();
					desc = desc == null ? ((JarPackageFragmentRoot) root).getAutomaticModuleDescription() : desc;
					String name = desc != null ? desc.getElementName() : null;
					if (name != null && name.length() > 0)
						modules.putIfAbsent(name, desc);
				}
			}
		} catch (JavaModelException e) {
			// do nothing
		}
		return modules;
	}

	private void findModules(char[] prefix, IJavaProject project, AssistOptions options, Set<String> skip) {
		if(this.requestor.isIgnored(CompletionProposal.MODULE_REF)) {
			return;
		}
		HashMap<String, IModuleDescription> probableModules = new HashMap<>();
		ModuleSourcePathManager mManager = JavaModelManager.getModulePathManager();
		JavaElementRequestor javaElementRequestor = new JavaElementRequestor();
		try {
			mManager.seekModule(prefix, true, javaElementRequestor);
			IModuleDescription[] modules = javaElementRequestor.getModules();
			for (IModuleDescription module : modules) {
				String name = module.getElementName();
				if (name == null || name.equals("")) //$NON-NLS-1$
					continue;
				probableModules.putIfAbsent(name, module);
			}
		} catch (JavaModelException e) {
			// ignore the error
		}
		probableModules.putAll(getAllJarModuleNames(project));
		Set<String> requiredModules = collectRequiredModules(probableModules);
		List<String> removeList = new ArrayList<>();
		if (prefix != CharOperation.ALL_PREFIX && prefix != null && prefix.length > 0) {
			for (String key : probableModules.keySet()) {
				if (!this.pattern.matchesName(prefix, key.toCharArray())) {
					removeList.add(key);
				}
			}
		}
		for (String key : removeList) {
			probableModules.remove(key);
		}
		removeList.clear();
		for (String key : skip) {
			probableModules.remove(key);
		}
		probableModules.entrySet().forEach(m -> this.requestor.accept(toModuleCompletion(m.getKey(), prefix, requiredModules)));
	}

	/**
	 * Returns the list of modules required by the current module, including transitive ones.
	 *
	 * The current module and java.base included in the set.
	 *
	 * @param reachableModules the map of reachable modules
	 * @return the list of modules required by the current module, including transitive ones
	 */
	private Set<String> collectRequiredModules(Map<String, IModuleDescription> reachableModules) {
		Set<String> requiredModules = new HashSet<>();
		requiredModules.add("java.base"); //$NON-NLS-1$
		try {
			IModuleDescription ownDescription = this.javaProject.getModuleDescription();
			if (ownDescription != null && !ownDescription.getElementName().isEmpty()) {
				Deque<String> moduleQueue = new ArrayDeque<>();
				requiredModules.add(ownDescription.getElementName());
				for (String moduleName : ownDescription.getRequiredModuleNames()) {
					moduleQueue.add(moduleName);
				}
				while (!moduleQueue.isEmpty()) {
					String top = moduleQueue.pollFirst();
					requiredModules.add(top);
					if (reachableModules.containsKey(top)) {
						for (String moduleName : reachableModules.get(top).getRequiredModuleNames()) {
							if (!requiredModules.contains(moduleName)) {
								moduleQueue.add(moduleName);
							}
						}
					}
				}
			} else {
				// working with the default module, so everything is required I think?
				return reachableModules.keySet();
			}
		} catch (JavaModelException e) {
			// do nothing
		}
		return requiredModules;
	}

	private CompletionProposal toModuleCompletion(String moduleName, char[] prefix, Set<String> requiredModules) {

		char[] completion = moduleName.toCharArray();
		int relevance = RelevanceConstants.R_DEFAULT;
		relevance += RelevanceConstants.R_RESOLVED;
		relevance += RelevanceConstants.R_INTERESTING;
		relevance += RelevanceUtils.computeRelevanceForCaseMatching(prefix, completion, this.assistOptions);
		relevance += RelevanceUtils.computeRelevanceForQualification(true, this.prefix, this.qualifiedPrefix);
		if (requiredModules.contains(moduleName)) {
			relevance += RelevanceConstants.R_NON_RESTRICTED;
		}
		DOMInternalCompletionProposal proposal = createProposal(CompletionProposal.MODULE_REF);
		proposal.setModuleName(completion);
		proposal.setDeclarationSignature(completion);
		proposal.setCompletion(completion);

		// replacement range using import decl range:
		ImportDeclaration importDecl = (ImportDeclaration) DOMCompletionUtil.findParent(this.toComplete, new int[] {ASTNode.IMPORT_DECLARATION});
		proposal.setReplaceRange(importDecl.getName().getStartPosition(), importDecl.getName().getStartPosition() + importDecl.getName().getLength());

		proposal.setRelevance(relevance);

		return proposal;
	}

	/**
	 * Returns an internal completion proposal of the given kind.
	 *
	 * Inspired by {@link CompletionEngine#createProposal}
	 *
	 * @param kind the kind of completion proposal (see the constants in {@link CompletionProposal})
	 * @return an internal completion proposal of the given kind
	 */
	protected DOMInternalCompletionProposal createProposal(int kind) {
		DOMInternalCompletionProposal proposal = new DOMInternalCompletionProposal(kind, this.offset);
		proposal.setNameLookup(this.nameEnvironment.nameLookup);
		proposal.setCompletionEngine(this.nestedEngine);
		return proposal;
	}

	/**
	 * Returns true if the orphaned content DOESN'T match the given name (the completion suggestion),
	 * according to the matching rules the user has configured.
	 *
	 * Inspired by {@link CompletionEngine#isFailedMatch}.
	 * However, this version also checks that the length of the orphaned content is not longer than then suggestion.
	 *
	 * @param orphanedContent the orphaned content to be completed
	 * @param name the completion suggestion
	 * @return true if the orphaned content DOESN'T match the given name
	 */
	protected boolean isFailedMatch(char[] orphanedContent, char[] name) {
		if (name.length < orphanedContent.length) {
			return true;
		}
		return !(
				(this.assistOptions.substringMatch && CharOperation.substringMatch(orphanedContent, name))
				|| (this.assistOptions.camelCaseMatch && CharOperation.camelCaseMatch(orphanedContent, name))
				|| (CharOperation.prefixEquals(orphanedContent, name, false))
				|| (this.assistOptions.subwordMatch && CharOperation.subWordMatch(orphanedContent, name))
		);
	}

	private CompletionProposal createKeywordProposal(char[] keyword, int startPos, int endPos) {
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), keyword, this.assistOptions);
		CompletionProposal keywordProposal = createProposal(CompletionProposal.KEYWORD);
		keywordProposal.setCompletion(keyword);
		keywordProposal.setName(keyword);
		if (startPos == -1 && endPos == -1) {
			setRange(keywordProposal);
		} else {
			keywordProposal.setReplaceRange(startPos, endPos);
			keywordProposal.setTokenRange(startPos, endPos);
		}
		keywordProposal.setRelevance(relevance);
		return keywordProposal;
	}

	private CompletionProposal createClassKeywordProposal(ITypeBinding typeBinding, int startPos, int endPos) {
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceConstants.R_NON_INHERITED
				+ RelevanceUtils.computeRelevanceForCaseMatching(this.prefix.toCharArray(), Keywords.CLASS, assistOptions)
//				+ RelevanceUtils.computeRelevanceForExpectingType(typeBinding, this.expectedTypes)
				;

		DOMInternalCompletionProposal keywordProposal = createProposal(CompletionProposal.FIELD_REF);
		keywordProposal.setCompletion(Keywords.CLASS);

		if (startPos == -1 && endPos == -1) {
			setRange(keywordProposal);
		} else {
			keywordProposal.setReplaceRange(startPos, endPos);
			keywordProposal.setTokenRange(startPos, endPos);
		}

		keywordProposal.setRelevance(relevance);
		keywordProposal.setPackageName(CharOperation.concatWith(TypeConstants.JAVA_LANG, '.'));
		keywordProposal.setTypeName("Class".toCharArray()); //$NON-NLS-1$
		keywordProposal.setName(Keywords.CLASS);

		// create the signature
		StringBuilder builder = new StringBuilder();
		builder.append("Ljava.lang.Class<"); //$NON-NLS-1$
		String typeBindingKey = typeBinding.getKey().replace('/', '.');
		builder.append(typeBindingKey);
		builder.append(">;"); //$NON-NLS-1$
		keywordProposal.setSignature(builder.toString().toCharArray());

		return keywordProposal;
	}

	private static final char[] LAMBDA = new char[] {'-', '>'};
	private CompletionProposal createLambdaExpressionProposal(IMethodBinding method) {
		DOMInternalCompletionProposal res = createProposal(CompletionProposal.LAMBDA_EXPRESSION);

		int relevance = RelevanceConstants.R_DEFAULT;
		relevance += RelevanceConstants.R_EXACT_EXPECTED_TYPE;
		relevance += RelevanceConstants.R_ABSTRACT_METHOD;
		relevance += RelevanceConstants.R_RESOLVED;
		relevance += RelevanceConstants.R_INTERESTING;
		relevance += RelevanceConstants.R_NON_RESTRICTED;

		int length = method.getParameterTypes().length;
		char[][] parameterTypeNames = new char[length][];

		for (int j = 0; j < length; j++) {
			ITypeBinding p = method.getParameterTypes()[j];
			parameterTypeNames[j] = p.getQualifiedName().toCharArray();
		}
		char[][] parameterNames = Stream.of(method.getParameterNames())//
				.map(s -> s.toCharArray()) //
				.toArray(char[][]::new);

		res.setDeclarationSignature(SignatureUtils.getSignatureChar(method.getDeclaringClass()));
		res.setSignature(SignatureUtils.getSignatureChar(method));

		IMethodBinding original = method.getMethodDeclaration();
		if (original != method) {
			res.setOriginalSignature(SignatureUtils.getSignatureChar(original));
		}

		setRange(res);

		res.setRelevance(relevance);
		res.setCompletion(LAMBDA);
		res.setParameterTypeNames(parameterTypeNames);
		res.setFlags(method.getModifiers());
		res.setDeclarationPackageName(method.getDeclaringClass().getPackage().getName().toCharArray());
		res.setDeclarationTypeName(method.getDeclaringClass().getQualifiedName().toCharArray());
		res.setName(method.getName().toCharArray());
		res.setTypeName(method.getReturnType().getQualifiedName().toCharArray());
		if (parameterNames != null) {
			res.setParameterNames(parameterNames);
		}

		return res;
	}

	/**
	 * Sets the replace and token ranges of the completion based on the contents of the buffer.
	 *
	 * Useful as a last case resort if there is no SimpleName node to base the range on.
	 *
	 * @param completionProposal the proposal whose range to set
	 */
	private void setRange(CompletionProposal completionProposal) {
		int startPos = this.offset - this.prefix.length();
		int cursor = this.offset;
		while (cursor < this.textContent.length()
				&& Character.isJavaIdentifierPart(this.textContent.charAt(cursor))) {
			cursor++;
		}
		completionProposal.setReplaceRange(startPos, cursor);
		completionProposal.setTokenRange(startPos, cursor);
	}

	private boolean isVisible(IBinding binding) {
		if (binding == null) {
			return false;
		}
		if (Modifier.isPublic(binding.getModifiers())) {
			return true;
		}
		if (Modifier.isPrivate(binding.getModifiers())) {
			return binding.isEqualTo(DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding());
		}
		ITypeBinding declaringClass = getDeclaringClass(binding);
		if (declaringClass == null) {
			return false;
		}
		if (Modifier.isProtected(binding.getModifiers())) {
			return findInSupers(DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding(), declaringClass);
		}
		return declaringClass.getPackage().isEqualTo(DOMCompletionUtil.findParentTypeDeclaration(this.toComplete).resolveBinding().getPackage());
	}

	private boolean isVisible(IJavaElement element) {
		if (element == null) {
			return false;
		}
		int flags;
		try {
			flags = element instanceof IType type ? type.getFlags() :
				element instanceof IMethod method ? method.getFlags() :
				element instanceof IField field ? field.getFlags() :
				0;
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			flags = 0;
		}
		if (element instanceof IType type) {
			if (Modifier.isPublic(flags)) {
				return true;
			}
			if (Modifier.isPrivate(flags)) {
				return type.equals(this.toComplete);
			}
			if (Modifier.isProtected(flags)) {
				// TODO
			}
			return Objects.equals(type.getPackageFragment().getElementName(), this.unit.getPackage().getName().toString());
		} else {
			IType type = element instanceof IMethod method ? method.getDeclaringType() :
				element instanceof IField field ? field.getDeclaringType() :
				null;
			if (!isVisible(type)) {
				return false;
			}
			if (Modifier.isPublic(flags)) {
				return true;
			}
			if (Modifier.isPrivate(flags)) {
				return type.equals(this.toComplete);
			}
			if (Modifier.isProtected(flags)) {
				// TODO
			}
			return Objects.equals(type.getPackageFragment().getElementName(), this.unit.getPackage().getName().toString());
		}
	}

	private ITypeBinding getDeclaringClass(IBinding binding) {
		return binding instanceof ITypeBinding typeBinding ? typeBinding :
			binding instanceof IMethodBinding methodBinding ? methodBinding.getDeclaringClass() :
			binding instanceof IVariableBinding variableBinding && variableBinding.isField() ? variableBinding.getDeclaringClass() :
			null;
	}
}
