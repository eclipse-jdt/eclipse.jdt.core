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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.codeassist.impl.Keywords;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ModuleSourcePathManager;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * A completion engine using a DOM as input (as opposed to {@link CompletionEngine} which
 * relies on lower-level parsing with ECJ)
 */
public class DOMCompletionEngine implements Runnable {

	private static final String FAKE_IDENTIFIER = new String(RecoveryScanner.FAKE_IDENTIFIER);

	private final int offset;
	private final CompilationUnit unit;
	private final CompletionRequestor requestor;
	private final ICompilationUnit modelUnit;
	private final SearchableEnvironment nameEnvironment;
	private final AssistOptions assistOptions;
	private final SearchPattern pattern;

	private final CompletionEngine nestedEngine; // to reuse some utilities
	private ExpectedTypes expectedTypes;
	private String prefix;
	private String qualifiedPrefix;
	private ASTNode toComplete;
	private final DOMCompletionEngineVariableDeclHandler variableDeclHandler;
	private final DOMCompletionEngineRecoveredNodeScanner recoveredNodeScanner;
	private final IProgressMonitor monitor;

	static class Bindings {
		private HashSet<IMethodBinding> methods = new HashSet<>();
		private HashSet<IBinding> others = new HashSet<>();

		public void add(IMethodBinding binding) {
			if (binding.isConstructor()) {
				return;
			}
			if (this.methods.stream().anyMatch(method -> method.overrides(binding))) {
				return;
			}
			this.methods.removeIf(method -> binding.overrides(method));
			this.methods.add(binding);
		}
		public void add(IBinding binding) {
			if (binding instanceof IMethodBinding methodBinding) {
				this.add(methodBinding);
			} else {
				this.others.add(binding);
			}
		}
		public void addAll(Collection<? extends IBinding> bindings) {
			bindings.forEach(this::add);
		}
		public Stream<IBinding> stream() {
			return Stream.of(this.methods, this.others).flatMap(Collection::stream);
		}
	}

	public DOMCompletionEngine(int offset, CompilationUnit domUnit, ICompilationUnit modelUnit, WorkingCopyOwner workingCopyOwner, CompletionRequestor requestor, IProgressMonitor monitor) {
		this.offset = offset;
		this.unit = domUnit;
		this.modelUnit = modelUnit;
		this.requestor = requestor;
		SearchableEnvironment env = null;
		if (this.modelUnit.getJavaProject() instanceof JavaProject p) {
			try {
				env = p.newSearchableNameEnvironment(workingCopyOwner, requestor.isTestCodeExcluded());
			} catch (JavaModelException e) {
				ILog.get().error(e.getMessage(), e);
			}
		}
		this.nameEnvironment = env;
		this.assistOptions = new AssistOptions(this.modelUnit.getOptions(true));
		this.pattern = new SearchPattern(SearchPattern.R_PREFIX_MATCH |
			(this.assistOptions.camelCaseMatch ? SearchPattern.R_CAMELCASE_MATCH : 0) |
			(this.assistOptions.substringMatch ? SearchPattern.R_SUBSTRING_MATCH : 0) |
			(this.assistOptions.subwordMatch ? SearchPattern.R_SUBWORD_MATCH :0)) {
			@Override
			public SearchPattern getBlankPattern() { return null; }
		};
		// TODO also honor assistOptions.checkVisibility!
		// TODO also honor requestor.ignore*
		// TODO sorting/relevance: closest/prefix match should go first
		// ...
		this.nestedEngine = new CompletionEngine(this.nameEnvironment, this.requestor, this.modelUnit.getOptions(true), this.modelUnit.getJavaProject(), workingCopyOwner, monitor);
		this.variableDeclHandler = new DOMCompletionEngineVariableDeclHandler();
		this.recoveredNodeScanner = new DOMCompletionEngineRecoveredNodeScanner(modelUnit, offset);
		this.monitor = monitor;
	}

	private Collection<? extends IBinding> visibleBindings(ASTNode node) {
		List<IBinding> visibleBindings = new ArrayList<>();

		if (node instanceof MethodDeclaration m) {
			visibleBindings.addAll(((List<VariableDeclaration>) m.parameters()).stream()
					.map(VariableDeclaration::resolveBinding).toList());
		}

		if (node instanceof LambdaExpression le) {
			visibleBindings.addAll(((List<VariableDeclaration>) le.parameters()).stream()
					.map(VariableDeclaration::resolveBinding).toList());
		}

		if (node instanceof Block block) {
			var bindings = ((List<Statement>) block.statements()).stream()
					.filter(statement -> statement.getStartPosition() < this.offset)
				.filter(VariableDeclarationStatement.class::isInstance)
				.map(VariableDeclarationStatement.class::cast)
				.flatMap(decl -> ((List<VariableDeclarationFragment>)decl.fragments()).stream())
					.map(VariableDeclarationFragment::resolveBinding).toList();
			visibleBindings.addAll(bindings);
		}
		return visibleBindings;
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

	private IJavaElement computeEnclosingElement() {
		try {
			if (this.modelUnit == null)
				return null;
			IJavaElement enclosingElement = this.modelUnit.getElementAt(this.offset);
			return enclosingElement == null ? this.modelUnit : enclosingElement;
		} catch (JavaModelException e) {
			ILog.get().error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public void run() {
		if (this.monitor != null) {
			this.monitor.beginTask(Messages.engine_completing, IProgressMonitor.UNKNOWN);
		}
		this.requestor.beginReporting();
		try {
			this.toComplete = NodeFinder.perform(this.unit, this.offset, 0);
			this.expectedTypes = new ExpectedTypes(this.assistOptions, this.toComplete);
			ASTNode context = this.toComplete;
			String completeAfter = ""; //$NON-NLS-1$
			if (this.toComplete instanceof SimpleName simpleName) {
				int charCount = this.offset - simpleName.getStartPosition();
				if (!FAKE_IDENTIFIER.equals(simpleName.getIdentifier())) {
					completeAfter = simpleName.getIdentifier().substring(0, charCount);
				}
				if (simpleName.getParent() instanceof FieldAccess || simpleName.getParent() instanceof MethodInvocation
						|| simpleName.getParent() instanceof VariableDeclaration || simpleName.getParent() instanceof QualifiedName) {
					context = this.toComplete.getParent();
				}
			} else if (this.toComplete instanceof SimpleType simpleType) {
				if (FAKE_IDENTIFIER.equals(simpleType.getName().toString())) {
					context = this.toComplete.getParent();
				}
			} else if (this.toComplete instanceof Block block && this.offset == block.getStartPosition()) {
				context = this.toComplete.getParent();
			}
			this.prefix = completeAfter;
			this.qualifiedPrefix = this.prefix;
			if (this.toComplete instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete != null && this.toComplete.getParent () instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			}
			Bindings scope = new Bindings();
			var completionContext = new DOMCompletionContext(this.offset, completeAfter.toCharArray(),
					computeEnclosingElement(), scope::stream);
			this.requestor.acceptContext(completionContext);

			// some flags to controls different applicable completion search strategies
			boolean suggestDefaultCompletions = true;

			checkCancelled();

			if (context instanceof FieldAccess fieldAccess) {
				statementLikeKeywords();
				processMembers(fieldAccess.getExpression().resolveTypeBinding(), scope, true, isNodeInStaticContext(fieldAccess));
				if (scope.stream().findAny().isPresent()) {
					scope.stream()
						.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
						.map(binding -> toProposal(binding))
						.forEach(this.requestor::accept);
					this.requestor.endReporting();
					return;
				}
				String packageName = ""; //$NON-NLS-1$
				if (fieldAccess.getExpression() instanceof FieldAccess parentFieldAccess
					&& parentFieldAccess.getName().resolveBinding() instanceof IPackageBinding packageBinding) {
					packageName = packageBinding.getName();
				} else if (fieldAccess.getExpression() instanceof SimpleName name
						&& name.resolveBinding() instanceof IPackageBinding packageBinding) {
					packageName = packageBinding.getName();
				}
				suggestPackages();
				suggestTypesInPackage(packageName);
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
					processMembers(type, scope, true, isNodeInStaticContext(invocation));
					scope.stream()
					.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
					.filter(IMethodBinding.class::isInstance)
					.map(binding -> toProposal(binding))
					.forEach(this.requestor::accept);
				}
				// else complete parameters, get back to default
			}
			if (context instanceof VariableDeclaration declaration) {
				var binding = declaration.resolveBinding();
				if (binding != null) {
					this.variableDeclHandler.findVariableNames(binding, completeAfter, scope).stream()
							.map(name -> toProposal(binding, name)).forEach(this.requestor::accept);
				}
				// seems we are completing a variable name, no need for further completion search.
				suggestDefaultCompletions = false;
			}
			if (context instanceof ModuleDeclaration mod) {
				findModules(this.prefix.toCharArray(), this.modelUnit.getJavaProject(), this.assistOptions, Set.of(mod.getName().toString()));
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
					findOverridableMethods(typeDeclBinding, this.modelUnit.getJavaProject(), context);
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof MarkerAnnotation) {
					completeMarkerAnnotation(completeAfter);
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof MemberValuePair) {
					// TODO: most of the time a constant value is expected,
					// however if an enum is expected, we can build out the completion for that
					suggestDefaultCompletions = false;
				}
				if (context.getParent() instanceof MethodDeclaration) {
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof AbstractTypeDeclaration typeDecl) {
				// eg.
				// public class Foo {
				//     |
				// }
				ITypeBinding typeDeclBinding = typeDecl.resolveBinding();
				findOverridableMethods(typeDeclBinding, this.modelUnit.getJavaProject(), null);
				suggestDefaultCompletions = false;
			}
			if (context instanceof QualifiedName qualifiedName) {
				IBinding qualifiedNameBinding = qualifiedName.getQualifier().resolveBinding();
				if (qualifiedNameBinding instanceof ITypeBinding qualifierTypeBinding && !qualifierTypeBinding.isRecovered()) {
					processMembers(qualifierTypeBinding, scope, false, isNodeInStaticContext(qualifiedName));
					publishFromScope(scope);
					int startPos = this.offset;
					int endPos = this.offset;
					if ((qualifiedName.getName().getFlags() & ASTNode.MALFORMED) != 0) {
						startPos = qualifiedName.getName().getStartPosition();
						endPos = startPos + qualifiedName.getName().getLength();
					}
					this.requestor.accept(createKeywordProposal(Keywords.THIS, startPos, endPos));
					this.requestor.accept(createKeywordProposal(Keywords.SUPER, startPos, endPos));
					this.requestor.accept(createClassKeywordProposal(qualifierTypeBinding, startPos, endPos));

					suggestDefaultCompletions = false;
				} else if (qualifiedNameBinding instanceof IPackageBinding qualifierPackageBinding && !qualifierPackageBinding.isRecovered()) {
					// start of a known package
					suggestPackages();
					// suggests types in the package
					suggestTypesInPackage(qualifierPackageBinding.getName());
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof SuperFieldAccess superFieldAccess) {
				ITypeBinding superTypeBinding = superFieldAccess.resolveTypeBinding();
				processMembers(superTypeBinding, scope, false, isNodeInStaticContext(superFieldAccess));
				publishFromScope(scope);
				suggestDefaultCompletions = false;
			}
			if (context instanceof MarkerAnnotation) {
				completeMarkerAnnotation(completeAfter);
				suggestDefaultCompletions = false;
			}
			if (context instanceof NormalAnnotation normalAnnotation) {
				completeNormalAnnotationParams(normalAnnotation, scope);
				suggestDefaultCompletions = false;
			}
			if (context instanceof MethodDeclaration methodDeclaration) {
				if (this.offset < methodDeclaration.getName().getStartPosition()) {
					completeMethodModifiers(methodDeclaration);
					// return type: suggest types from current CU
					if (methodDeclaration.getReturnType2() == null) {
						ASTNode current = this.toComplete;
						while (current != null) {
							scope.addAll(visibleTypeBindings(current));
							current = current.getParent();
						}
						publishFromScope(scope);
					}
					suggestDefaultCompletions = false;
				} else if (methodDeclaration.getBody() != null && this.offset <= methodDeclaration.getBody().getStartPosition()) {
					completeThrowsClause(methodDeclaration, scope);
					suggestDefaultCompletions = false;
				}
			}

			if (suggestDefaultCompletions) {
				ASTNode current = this.toComplete;
				while (current != null) {
					scope.addAll(visibleBindings(current));
					// break if following conditions match, otherwise we get all visible symbols which is unwanted in this
					// completion context.
					if (current instanceof NormalAnnotation normalAnnotation) {
						completeNormalAnnotationParams(normalAnnotation, scope);
						break;
					}
					if (current instanceof AbstractTypeDeclaration typeDecl) {
						processMembers(typeDecl.resolveBinding(), scope, true, isNodeInStaticContext(this.toComplete));
					}
					current = current.getParent();
				}
				statementLikeKeywords();
				publishFromScope(scope);
				if (!completeAfter.isBlank()) {
					final int typeMatchRule = this.toComplete.getParent() instanceof Annotation
							? IJavaSearchConstants.ANNOTATION_TYPE
							: IJavaSearchConstants.TYPE;
					findTypes(completeAfter, typeMatchRule, null)
							.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
									type.getElementName().toCharArray()))
							.map(this::toProposal).forEach(this.requestor::accept);
				}
				checkCancelled();
				suggestPackages();
			}

			checkCancelled();
		} finally {
			this.requestor.endReporting();
			if (this.monitor != null) {
				this.monitor.done();
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
		if (findParent(this.toComplete,
				new int[] { ASTNode.WHILE_STATEMENT, ASTNode.DO_STATEMENT, ASTNode.FOR_STATEMENT }) != null) {
			keywords.add(Keywords.BREAK);
			keywords.add(Keywords.CONTINUE);
		}
		ExpressionStatement exprStatement = (ExpressionStatement) findParent(this.toComplete, new int[] {ASTNode.EXPRESSION_STATEMENT});
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

	private void completeThrowsClause(MethodDeclaration methodDeclaration, Bindings scope) {
		if (methodDeclaration.thrownExceptionTypes().size() == 0) {
			if (!isFailedMatch(this.prefix.toCharArray(), Keywords.THROWS)) {
				this.requestor.accept(createKeywordProposal(Keywords.THROWS, this.offset, this.offset));
			}
		}
		// TODO: JDT doesn't filter out non-throwable types, should we?
		ASTNode current = this.toComplete;
		while (current != null) {
			scope.addAll(visibleTypeBindings(current));
			current = current.getParent();
		}
		publishFromScope(scope);
	}

	private void suggestPackages() {
		try {
			if(this.requestor.isIgnored(CompletionProposal.PACKAGE_REF))
				return;
			if (this.prefix.isEmpty() && this.qualifiedPrefix.isEmpty()) {
				// JDT doesn't suggest package names in this case
				return;
			}

			Arrays.stream(this.modelUnit.getJavaProject().getPackageFragments())
					.map(IPackageFragment::getElementName).distinct()
					// the default package doesn't make sense as a completion item
					.filter(name -> !name.isBlank())
					// the qualifier must match exactly. only the last segment is (potentially) fuzzy matched.
					// However, do not match the already completed package name!
					.filter(name -> CharOperation.prefixEquals(this.qualifiedPrefix.toCharArray(), name.toCharArray()) && name.length() > this.qualifiedPrefix.length())
					.filter(name -> this.pattern.matchesName(this.prefix.toCharArray(), name.toCharArray()))
					.map(pack -> toPackageProposal(pack, this.toComplete)).forEach(this.requestor::accept);
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}

	private void suggestTypesInPackage(String packageName) {
		if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
			List<IType> foundTypes = findTypes(this.prefix, packageName).toList();
			for (IType foundType : foundTypes) {
				if (this.pattern.matchesName(this.prefix.toCharArray(), foundType.getElementName().toCharArray())) {
					this.requestor.accept(this.toProposal(foundType));
				}
			}
		}
	}

	private static ASTNode findParent(ASTNode nodeToSearch, int[] kindsToFind) {
		ASTNode cursor = nodeToSearch;
		while (cursor != null) {
			for (int kindToFind : kindsToFind) {
				if (cursor.getNodeType() == kindToFind) {
					return cursor;
				}
			}
			cursor = cursor.getParent();
		}
		return null;
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
		Arrays.stream(normalAnnotation.resolveTypeBinding().getDeclaredMethods()) //
			.filter(declaredMethod -> {
				return (declaredMethod.getModifiers() & Flags.AccStatic) == 0
						&& !definedKeys.contains(declaredMethod.getName().toString());
			}) //
			.forEach(scope::add);
		publishFromScope(scope);
	}

	private void publishFromScope(Bindings scope) {
		scope.stream() //
			.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
			.map(binding -> toProposal(binding)).forEach(this.requestor::accept);
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
					|| (method.getModifiers() & Modifier.STATIC) != 0
					|| (method.getModifiers() & Modifier.PRIVATE) != 0
					|| ((method.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)) == 0) && !typeBinding.getPackage().getKey().equals(originalPackageKey)) {
				continue next;
			}
			alreadySuggestedKeys.add(method.getKey());
			if ((method.getModifiers() & Modifier.FINAL) != 0) {
				continue next;
			}
			if (isFailedMatch(this.prefix.toCharArray(), method.getName().toCharArray())) {
				continue next;
			}
			InternalCompletionProposal proposal = createProposal(CompletionProposal.METHOD_DECLARATION);
			proposal.setReplaceRange(this.offset, this.offset);
			if (toReplace != null) {
				proposal.setReplaceRange(toReplace.getStartPosition(), toReplace.getStartPosition() + toReplace.getLength());
			}
			proposal.setName(method.getName().toCharArray());
			proposal.setFlags(method.getModifiers());
			proposal.setTypeName(method.getReturnType().getName().toCharArray());
			proposal.setDeclarationPackageName(typeBinding.getPackage().getName().toCharArray());
			proposal.setDeclarationTypeName(typeBinding.getQualifiedName().toCharArray());
			proposal.setDeclarationSignature(DOMCompletionEngineBuilder.getSignature(method.getDeclaringClass()));
			proposal.setKey(method.getKey().toCharArray());
			proposal.setSignature(DOMCompletionEngineBuilder.getSignature(method));
			proposal.setParameterNames(Stream.of(method.getParameterNames()).map(name -> name.toCharArray()).toArray(char[][]::new));

			int relevance = RelevanceConstants.R_DEFAULT
					+ RelevanceConstants.R_RESOLVED
					+ RelevanceConstants.R_INTERESTING
					+ RelevanceConstants.R_METHOD_OVERIDE
					+ ((method.getModifiers() & Modifier.ABSTRACT) != 0 ? RelevanceConstants.R_ABSTRACT_METHOD : 0)
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
		var searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[] { this.modelUnit.getJavaProject() });
		TypeNameMatchRequestor typeRequestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch match) {
				types.add(match.getType());
			}
		};
		try {
			new SearchEngine(this.modelUnit.getOwner()).searchAllTypeNames(
					packageName == null ? null : packageName.toCharArray(), SearchPattern.R_EXACT_MATCH,
					namePrefix.toCharArray(),
					SearchPattern.R_PREFIX_MATCH
							| (this.assistOptions.substringMatch ? SearchPattern.R_SUBSTRING_MATCH : 0)
							| (this.assistOptions.subwordMatch ? SearchPattern.R_SUBWORD_MATCH : 0),
					typeMatchRule, searchScope, typeRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
			// TODO also resolve potential sub-packages
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return types.stream();
	}

	private void processMembers(ITypeBinding typeBinding, Bindings scope, boolean includePrivate, boolean isStaticContext) {
		if (typeBinding == null) {
			return;
		}
		Arrays.stream(typeBinding.getDeclaredFields()) //
			.filter(field -> (includePrivate || (field.getModifiers() & Flags.AccPrivate) == 0)
					&& (!isStaticContext || (field.getModifiers() & Flags.AccStatic) != 0)) //
			.forEach(scope::add);
		Arrays.stream(typeBinding.getDeclaredMethods()) //
			.filter(method -> includePrivate || (method.getModifiers() & Flags.AccPrivate) == 0
					&& (!isStaticContext || (method.getModifiers() & Flags.AccStatic) != 0)) //
			.forEach(scope::add);
		if (typeBinding.getInterfaces() != null) {
			Arrays.stream(typeBinding.getInterfaces()).forEach(member -> processMembers(member, scope, false, isStaticContext));
		}
		processMembers(typeBinding.getSuperclass(), scope, false, isStaticContext);
	}
	private CompletionProposal toProposal(IBinding binding) {
		return toProposal(binding, binding.getName());
	}

	private CompletionProposal toProposal(IBinding binding, String completion) {
		if (binding instanceof ITypeBinding && binding.getJavaElement() instanceof IType type) {
			return toProposal(type);
		}

		int kind = -1;
		if (binding instanceof ITypeBinding) {
			kind = CompletionProposal.TYPE_REF;
		} else if (binding instanceof IMethodBinding m) {
			if (m.getDeclaringClass() != null && m.getDeclaringClass().isAnnotation()) {
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

		InternalCompletionProposal res = new InternalCompletionProposal(kind, this.offset);
		res.setName(binding.getName().toCharArray());
		if (kind == CompletionProposal.METHOD_REF) {
			completion += "()"; //$NON-NLS-1$
		}
		res.setCompletion(completion.toCharArray());
		res.setFlags(binding.getModifiers());

		if (kind == CompletionProposal.METHOD_REF) {
			var methodBinding = (IMethodBinding) binding;
			var paramNames = DOMCompletionEngineMethodDeclHandler.findVariableNames(methodBinding);
			if (paramNames.isEmpty()) {
				res.setParameterNames(null);
			} else {
				res.setParameterNames(paramNames.stream().map(String::toCharArray).toArray(i -> new char[i][]));
			}
			res.setParameterTypeNames(Stream.of(methodBinding.getParameterNames()).map(String::toCharArray).toArray(char[][]::new));
			res.setSignature(DOMCompletionEngineBuilder.getSignature(methodBinding));
			res.setDeclarationSignature(Signature
					.createTypeSignature(methodBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
					.toCharArray());
		} else if (kind == CompletionProposal.LOCAL_VARIABLE_REF || kind == CompletionProposal.FIELD_REF) {
			var variableBinding = (IVariableBinding) binding;
			res.setSignature(
					Signature.createTypeSignature(variableBinding.getType().getQualifiedName().toCharArray(), true)
							.toCharArray());
			res.setReceiverSignature(
					variableBinding.isField()
							? Signature
									.createTypeSignature(
											variableBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
									.toCharArray()
							: new char[] {});
			res.setDeclarationSignature(
					variableBinding.isField()
							? Signature
									.createTypeSignature(
											variableBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
									.toCharArray()
							: new char[] {});
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
		res.setReplaceRange(this.toComplete instanceof SimpleName ? this.toComplete.getStartPosition() : this.offset,
				DOMCompletionEngine.this.offset);
		var element = binding.getJavaElement();
		if (element != null) {
			res.setDeclarationTypeName(((IType)element.getAncestor(IJavaElement.TYPE)).getFullyQualifiedName().toCharArray());
			res.setDeclarationPackageName(element.getAncestor(IJavaElement.PACKAGE_FRAGMENT).getElementName().toCharArray());
		}
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;

		res.setRelevance(CompletionEngine.computeBaseRelevance() +
				CompletionEngine.computeRelevanceForResolution() +
				this.nestedEngine.computeRelevanceForInterestingProposal() +
				CompletionEngine.computeRelevanceForCaseMatching(this.prefix.toCharArray(), binding.getName().toCharArray(), this.assistOptions) +
				computeRelevanceForExpectingType(binding instanceof ITypeBinding typeBinding ? typeBinding :
					binding instanceof IMethodBinding methodBinding ? methodBinding.getReturnType() :
					binding instanceof IVariableBinding variableBinding ? variableBinding.getType() :
					this.toComplete.getAST().resolveWellKnownType(Object.class.getName())) +
				RelevanceConstants.R_UNQUALIFIED + // TODO: add logic
				CompletionEngine.computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE) //no access restriction for class field
				//RelevanceConstants.R_NON_INHERITED // TODO: when is this active?
				);
		return res;
	}

	private String qualifiedTypeName(ITypeBinding typeBinding) {
		if (typeBinding.isTypeVariable()) {
			return typeBinding.getName();
		} else {
			return typeBinding.getQualifiedName();
		}
	}

	private CompletionProposal toProposal(IType type) {
		// TODO add import if necessary
		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.TYPE_REF, this.offset);
		char[] simpleName = type.getElementName().toCharArray();
		char[] signature = Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray();

		res.setName(simpleName);
		res.setCompletion(type.getElementName().toCharArray());
		res.setSignature(signature);
		if (this.toComplete instanceof FieldAccess || this.prefix.isEmpty()) {
			res.setReplaceRange(this.offset, this.offset);
		} else if (this.toComplete instanceof MarkerAnnotation) {
			res.setReplaceRange(this.toComplete.getStartPosition() + 1, this.toComplete.getStartPosition() + this.toComplete.getLength());
		} else if (this.toComplete instanceof SimpleName currentName && FAKE_IDENTIFIER.equals(currentName.toString())) {
			res.setReplaceRange(this.offset, this.offset);
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
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED;
		relevance += computeRelevanceForCaseMatching(this.prefix.toCharArray(), simpleName, this.assistOptions);
		try {
			if (type.isAnnotation()) {
				relevance += RelevanceConstants.R_ANNOTATION;
			}
		} catch (JavaModelException e) {
			// do nothing
		}
		res.setRelevance(relevance);
		// set defaults for now to avoid error downstream
		res.setRequiredProposals(new CompletionProposal[] { toImportProposal(simpleName, signature) });
		return res;
	}

	private CompletionProposal toImportProposal(char[] simpleName, char[] signature) {
		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.TYPE_IMPORT, this.offset);
		res.setName(simpleName);
		res.setSignature(signature);
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;
		return res;
	}

	private CompletionProposal toPackageProposal(String packageName, ASTNode completing) {

		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.PACKAGE_REF, this.offset);
		res.setName(packageName.toCharArray());
		res.setCompletion(packageName.toCharArray());
		res.setDeclarationSignature(packageName.toCharArray());
		res.setSignature(packageName.toCharArray());
		QualifiedName qualifiedName = (QualifiedName)findParent(completing, new int[] {ASTNode.QUALIFIED_NAME});
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ computeRelevanceForCaseMatching(this.prefix.toCharArray(), packageName.toCharArray(), this.assistOptions)
				+ (qualifiedName != null ? RelevanceConstants.R_QUALIFIED : RelevanceConstants.R_QUALIFIED)
				+ RelevanceConstants.R_NON_RESTRICTED;
		res.setRelevance(relevance);
		configureProposal(res, completing);
		if (qualifiedName != null) {
			res.setReplaceRange(qualifiedName.getStartPosition(), this.offset);
		}
		return res;
	}

	private void configureProposal(InternalCompletionProposal proposal, ASTNode completing) {
		proposal.setReplaceRange(completing.getStartPosition(), this.offset);
		proposal.completionEngine = this.nestedEngine;
		proposal.nameLookup = this.nameEnvironment.nameLookup;
	}

	private int computeRelevanceForExpectingType(ITypeBinding proposalType){
		if (proposalType != null) {
			int relevance = 0;
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=271296
			// If there is at least one expected type, then void proposal types attract a degraded relevance.
			if (!this.expectedTypes.getExpectedTypes().isEmpty() && PrimitiveType.VOID.toString().equals(proposalType.getName())) {
				return RelevanceConstants.R_VOID;
			}
			for (ITypeBinding expectedType : this.expectedTypes.getExpectedTypes()) {
				if(this.expectedTypes.allowsSubtypes()
						&& proposalType.getErasure().isSubTypeCompatible(expectedType.getErasure())) {

					if(Objects.equals(expectedType.getQualifiedName(), proposalType.getQualifiedName())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
					} else if (proposalType.getPackage().isUnnamed()) {
						return RelevanceConstants.R_PACKAGE_EXPECTED_TYPE;
					}
					relevance = RelevanceConstants.R_EXPECTED_TYPE;

				}
				if(this.expectedTypes.allowsSupertypes() && expectedType.isSubTypeCompatible(proposalType)) {

					if(Objects.equals(expectedType.getQualifiedName(), proposalType.getQualifiedName())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
					}
					relevance = RelevanceConstants.R_EXPECTED_TYPE;
				}
				// Bug 84720 - [1.5][assist] proposal ranking by return value should consider auto(un)boxing
				// Just ensuring that the unitScope is not null, even though it's an unlikely case.
//				if (this.unitScope != null && this.unitScope.isBoxingCompatibleWith(proposalType, this.expectedTypes[i])) {
//					relevance = CompletionEngine.R_EXPECTED_TYPE;
//				}
			}
			return relevance;
		}
		return 0;
	}

	private HashSet<String> getAllJarModuleNames(IJavaProject project) {
		HashSet<String> modules = new HashSet<>();
		try {
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
				if (root instanceof JarPackageFragmentRoot) {
					IModuleDescription desc = root.getModuleDescription();
					desc = desc == null ? ((JarPackageFragmentRoot) root).getAutomaticModuleDescription() : desc;
					String name = desc != null ? desc.getElementName() : null;
					if (name != null && name.length() > 0)
						modules.add(name);
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

		HashSet<String> probableModules = new HashSet<>();
		ModuleSourcePathManager mManager = JavaModelManager.getModulePathManager();
		JavaElementRequestor javaElementRequestor = new JavaElementRequestor();
		try {
			mManager.seekModule(prefix, true, javaElementRequestor);
			IModuleDescription[] modules = javaElementRequestor.getModules();
			for (IModuleDescription module : modules) {
				String name = module.getElementName();
				if (name == null || name.equals("")) //$NON-NLS-1$
					continue;
				probableModules.add(name);
			}
		} catch (JavaModelException e) {
			// ignore the error
		}
		probableModules.addAll(getAllJarModuleNames(project));
		if (prefix != CharOperation.ALL_PREFIX && prefix != null && prefix.length > 0) {
			probableModules.removeIf(e -> CompletionEngine.isFailedMatch(prefix, e.toCharArray(), options));
		}
		probableModules.removeIf(skip::contains);
		probableModules.forEach(m -> this.requestor.accept(toModuleCompletion(m, prefix)));
	}

	private CompletionProposal toModuleCompletion(String moduleName, char[] prefix) {
		char[] completion = moduleName.toCharArray();
		int relevance = CompletionEngine.computeBaseRelevance();
		relevance += CompletionEngine.computeRelevanceForResolution();
		relevance += this.nestedEngine.computeRelevanceForInterestingProposal();
		relevance += this.nestedEngine.computeRelevanceForCaseMatching(prefix, completion);
		relevance += this.nestedEngine.computeRelevanceForQualification(true);
		relevance += this.nestedEngine.computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
		InternalCompletionProposal proposal = new InternalCompletionProposal(CompletionProposal.MODULE_REF,
				this.offset);
		proposal.setModuleName(completion);
		proposal.setDeclarationSignature(completion);
		proposal.setCompletion(completion);
		proposal.setReplaceRange(
				this.toComplete instanceof SimpleName ? this.toComplete.getStartPosition() : this.offset,
				DOMCompletionEngine.this.offset);
		proposal.setRelevance(relevance);
		proposal.completionEngine = this.nestedEngine;
		proposal.nameLookup = this.nameEnvironment.nameLookup;

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
	protected InternalCompletionProposal createProposal(int kind) {
		InternalCompletionProposal proposal = (InternalCompletionProposal) CompletionProposal.create(kind, this.offset);
		proposal.nameLookup = this.nameEnvironment.nameLookup;
		proposal.completionEngine = this.nestedEngine;
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

	static int computeRelevanceForCaseMatching(char[] token, char[] proposalName, AssistOptions options) {
		if (CharOperation.equals(token, proposalName, true)) {
			return RelevanceConstants.R_EXACT_NAME + RelevanceConstants.R_CASE;
		} else if (CharOperation.equals(token, proposalName, false)) {
			return RelevanceConstants.R_EXACT_NAME;
		} else if (CharOperation.prefixEquals(token, proposalName, false)) {
			if (CharOperation.prefixEquals(token, proposalName, true))
				return RelevanceConstants.R_CASE;
		} else if (options.camelCaseMatch && CharOperation.camelCaseMatch(token, proposalName)) {
			return RelevanceConstants.R_CAMEL_CASE;
		} else if (options.substringMatch && CharOperation.substringMatch(token, proposalName)) {
			return RelevanceConstants.R_SUBSTRING;
		} else if (options.subwordMatch && CharOperation.subWordMatch(token, proposalName)) {
			return RelevanceConstants.R_SUBWORD;
		}
		return 0;
	}

	private CompletionProposal createKeywordProposal(char[] keyword, int startPos, int endPos) {
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED
				+ CompletionEngine.computeRelevanceForCaseMatching(this.prefix.toCharArray(), keyword, this.assistOptions);
		CompletionProposal keywordProposal = createProposal(CompletionProposal.KEYWORD);
		keywordProposal.setCompletion(keyword);
		keywordProposal.setName(keyword);
		keywordProposal.setReplaceRange(startPos, endPos);
		keywordProposal.setRelevance(relevance);
		return keywordProposal;
	}

	private CompletionProposal createClassKeywordProposal(ITypeBinding typeBinding, int startPos, int endPos) {
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceConstants.R_EXPECTED_TYPE;
		if (!isFailedMatch(this.prefix.toCharArray(), Keywords.CLASS)) {
			relevance += RelevanceConstants.R_SUBSTRING;
		}
		InternalCompletionProposal keywordProposal = createProposal(CompletionProposal.FIELD_REF);
		keywordProposal.setCompletion(Keywords.CLASS);
		keywordProposal.setReplaceRange(startPos, endPos);
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

	private static boolean isNodeInStaticContext(ASTNode node) {
		boolean isStatic = false;
		ASTNode cursor = node;
		while (cursor != null && !(cursor instanceof MethodDeclaration)) {
			cursor = cursor.getParent();
		}
		if (cursor instanceof MethodDeclaration methodDecl) {
			isStatic = (methodDecl.resolveBinding().getModifiers() & Flags.AccStatic) != 0;
		}
		return isStatic;
	}

}
