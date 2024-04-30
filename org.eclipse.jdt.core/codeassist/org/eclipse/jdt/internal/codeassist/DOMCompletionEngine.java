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
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * A completion engine using a DOM as input (as opposed to {@link CompletionEngine} which
 * relies on lower-level parsing with ECJ)
 */
public class DOMCompletionEngine implements Runnable {

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
	private ASTNode toComplete;

	private static class Bindings {
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
	}

	private static Collection<? extends IBinding> visibleBindings(ASTNode node, int offset) {
		if (node instanceof Block block) {
			return ((List<Statement>)block.statements()).stream()
				.filter(statement -> statement.getStartPosition() < offset)
				.filter(VariableDeclarationStatement.class::isInstance)
				.map(VariableDeclarationStatement.class::cast)
				.flatMap(decl -> ((List<VariableDeclarationFragment>)decl.fragments()).stream())
				.map(VariableDeclarationFragment::resolveBinding)
				.toList();
		}
		return List.of();
	}

	@Override
	public void run() {
		this.requestor.beginReporting();
		this.requestor.acceptContext(new CompletionContext());
		this.toComplete = NodeFinder.perform(this.unit, this.offset, 0);
		this.expectedTypes = new ExpectedTypes(this.assistOptions, this.toComplete);
		ASTNode context = this.toComplete;
		String completeAfter = ""; //$NON-NLS-1$
		if (this.toComplete instanceof SimpleName simpleName) {
			int charCount = this.offset - simpleName.getStartPosition();
			completeAfter = simpleName.getIdentifier().substring(0, charCount);
			if (simpleName.getParent() instanceof FieldAccess || simpleName.getParent() instanceof MethodInvocation) {
				context = this.toComplete.getParent();
			}
		}
		this.prefix = completeAfter;
		Bindings scope = new Bindings();
		if (context instanceof FieldAccess fieldAccess) {
			processMembers(fieldAccess.getExpression().resolveTypeBinding(), scope);
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
			findTypes(completeAfter, packageName)
				.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(), type.getElementName().toCharArray()))
				.map(this::toProposal)
				.forEach(this.requestor::accept);
			List<String> packageNames = new ArrayList<>();
			try {
				this.nameEnvironment.findPackages(this.modelUnit.getSource().substring(fieldAccess.getStartPosition(), this.offset).toCharArray(), new ISearchRequestor() {

					@Override
					public void acceptType(char[] packageName, char[] typeName, char[][] enclosingTypeNames, int modifiers,
							AccessRestriction accessRestriction) { }

					@Override
					public void acceptPackage(char[] packageName) {
						packageNames.add(new String(packageName));
					}

					@Override
					public void acceptModule(char[] moduleName) { }

					@Override
					public void acceptConstructor(int modifiers, char[] simpleTypeName, int parameterCount, char[] signature,
							char[][] parameterTypes, char[][] parameterNames, int typeModifiers, char[] packageName, int extraFlags,
							String path, AccessRestriction access) { }
				});
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			packageNames.removeIf(name -> !this.pattern.matchesName(this.prefix.toCharArray(), name.toCharArray()));
			if (!packageNames.isEmpty()) {
				packageNames.stream().distinct().map(pack -> toPackageProposal(pack, fieldAccess)).forEach(this.requestor::accept);
				return;
			}
		}
		if (context instanceof MethodInvocation invocation) {
			if (this.offset <= invocation.getName().getStartPosition() + invocation.getName().getLength()) {
				// complete name
				ITypeBinding type = invocation.getExpression().resolveTypeBinding();
				processMembers(type, scope);
				scope.stream()
				.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
				.filter(IMethodBinding.class::isInstance)
				.map(binding -> toProposal(binding))
				.forEach(this.requestor::accept);
			}
			// else complete parameters, get back to default
		}

		ASTNode current = this.toComplete;
		ASTNode parent = current;
		while (parent != null) {
			if (parent instanceof AbstractTypeDeclaration typeDecl) {
				processMembers(typeDecl.resolveBinding(), scope);
			}
			parent = parent.getParent();
		}
		while (current != null) {
			scope.addAll(visibleBindings(current, this.offset));
			current = current.getParent();
		}
		scope.stream()
			.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
			.map(binding -> toProposal(binding))
			.forEach(this.requestor::accept);
		if (!completeAfter.isBlank()) {
			findTypes(completeAfter, null)
				.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(), type.getElementName().toCharArray()))
				.map(this::toProposal)
				.forEach(this.requestor::accept);
		}
		try {
			Arrays.stream(this.modelUnit.getJavaProject().getPackageFragments())
				.map(IPackageFragment::getElementName)
				.distinct()
				.filter(name -> this.pattern.matchesName(this.prefix.toCharArray(), name.toCharArray()))
				.map(pack -> toPackageProposal(pack, toComplete))
				.forEach(this.requestor::accept);
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		this.requestor.endReporting();
	}

	private Stream<IType> findTypes(String namePrefix, String packageName) {
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
			new SearchEngine(this.modelUnit.getOwner()).searchAllTypeNames(packageName == null ? null : packageName.toCharArray(), SearchPattern.R_EXACT_MATCH,
					namePrefix.toCharArray(), SearchPattern.R_PREFIX_MATCH | (this.assistOptions.substringMatch ? SearchPattern.R_SUBSTRING_MATCH : 0) | (this.assistOptions.subwordMatch ? SearchPattern.R_SUBWORD_MATCH : 0),
					IJavaSearchConstants.TYPE,
					searchScope,
					typeRequestor,
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
					null);
			// TODO also resolve potential sub-packages
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return types.stream();
	}

	private void processMembers(ITypeBinding typeBinding, Bindings scope) {
		if (typeBinding == null) {
			return;
		}
		Arrays.stream(typeBinding.getDeclaredFields()).forEach(scope::add);
		Arrays.stream(typeBinding.getDeclaredMethods()).forEach(scope::add);
		if (typeBinding.getInterfaces() != null) {
			Arrays.stream(typeBinding.getInterfaces()).forEach(member -> processMembers(member, scope));
		}
		processMembers(typeBinding.getSuperclass(), scope);
	}

	private CompletionProposal toProposal(IBinding binding) {
		if (binding instanceof ITypeBinding && binding.getJavaElement() instanceof IType type) {
			return toProposal(type);
		}
		InternalCompletionProposal res = new InternalCompletionProposal(
			binding instanceof ITypeBinding ? CompletionProposal.TYPE_REF :
			binding instanceof IMethodBinding ? CompletionProposal.METHOD_REF :
			binding instanceof IVariableBinding variableBinding ? CompletionProposal.LOCAL_VARIABLE_REF :
			-1, this.offset);
		res.setName(binding.getName().toCharArray());
		String completion = binding.getName();
		if (binding instanceof IMethodBinding) {
			completion += "()"; //$NON-NLS-1$
		}
		res.setCompletion(completion.toCharArray());
		res.setSignature(
			binding instanceof IMethodBinding methodBinding ?
				Signature.createMethodSignature(
					Arrays.stream(methodBinding.getParameterTypes())
						.map(ITypeBinding::getName)
						.map(String::toCharArray)
						.map(type -> Signature.createTypeSignature(type, true).toCharArray())
						.toArray(char[][]::new),
					Signature.createTypeSignature(methodBinding.getReturnType().getQualifiedName().toCharArray(), true).toCharArray()) :
			binding instanceof IVariableBinding variableBinding ?
				Signature.createTypeSignature(variableBinding.getType().getQualifiedName().toCharArray(), true).toCharArray() :
			binding instanceof ITypeBinding typeBinding ?
				Signature.createTypeSignature(typeBinding.getQualifiedName().toCharArray(), true).toCharArray() :
			new char[] {});
		res.setReplaceRange(this.toComplete instanceof SimpleName ? this.toComplete.getStartPosition() : this.offset, DOMCompletionEngine.this.offset);
		res.setReceiverSignature(
			binding instanceof IMethodBinding method ?
				Signature.createTypeSignature(method.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray() :
			binding instanceof IVariableBinding variable && variable.isField() ?
				Signature.createTypeSignature(variable.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray() :
			new char[]{});
		res.setDeclarationSignature(
			binding instanceof IMethodBinding method ?
				Signature.createTypeSignature(method.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray() :
			binding instanceof IVariableBinding variable && variable.isField() ?
				Signature.createTypeSignature(variable.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray() :
			new char[]{});

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
				CompletionEngine.computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE) + //no access restriction for class field
				CompletionEngine.R_NON_INHERITED);
		return res;
	}

	private CompletionProposal toProposal(IType type) {
		// TODO add import if necessary
		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.TYPE_REF, this.offset);
		res.setName(type.getElementName().toCharArray());
		res.setCompletion(type.getElementName().toCharArray());
		res.setSignature(Signature.createTypeSignature(type.getFullyQualifiedName(), true).toCharArray());
		res.setReplaceRange(!(this.toComplete instanceof FieldAccess) ? this.toComplete.getStartPosition() : this.offset, this.offset);
		try {
			res.setFlags(type.getFlags());
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		if (this.toComplete instanceof SimpleName) {
			res.setTokenRange(this.toComplete.getStartPosition(), this.toComplete.getStartPosition() + this.toComplete.getLength());
		}
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;
		return res;
	}

	private CompletionProposal toPackageProposal(String packageName, ASTNode completing) {
		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.PACKAGE_REF, this.offset);
		res.setName(packageName.toCharArray());
		res.setCompletion(packageName.toCharArray());
		res.setReplaceRange(completing.getStartPosition(), this.offset);
		res.setDeclarationSignature(packageName.toCharArray());
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;
		return res;
	}

	private int computeRelevanceForExpectingType(ITypeBinding proposalType){
		if (proposalType != null) {
			int relevance = 0;
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=271296
			// If there is at least one expected type, then void proposal types attract a degraded relevance.
			if (PrimitiveType.VOID.toString().equals(proposalType.getName())) {
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

}
