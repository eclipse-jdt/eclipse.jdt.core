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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
	private IBuffer cuBuffer;
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
		if (this.modelUnit.getJavaProject() instanceof JavaProject p && requestor != null) {
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
		try {
			this.cuBuffer = this.modelUnit.getBuffer();
		} catch (JavaModelException e) {
			ILog.get().error("unable to access buffer for completion", e); //$NON-NLS-1$
		}
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

		if (node instanceof AbstractTypeDeclaration typeDecl) {
			visibleBindings.addAll(typeDecl.bodyDeclarations().stream()
					.flatMap(bodyDecl -> {
						if (bodyDecl instanceof FieldDeclaration fieldDecl) {
							return ((List<VariableDeclarationFragment>)fieldDecl.fragments()).stream().map(fragment -> fragment.resolveBinding());
						}
						if (bodyDecl instanceof MethodDeclaration methodDecl) {
							return Stream.of(methodDecl.resolveBinding());
						}
						return Stream.of();
					}).toList());
			visibleBindings.add(typeDecl.resolveBinding());
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
			// Use the raw text to walk back the offset to the first non-whitespace spot
			int adjustedOffset = this.offset;
			if (this.cuBuffer != null) {
				if (adjustedOffset >= this.cuBuffer.getLength()) {
					adjustedOffset = this.cuBuffer.getLength() - 1;
				}
				if (adjustedOffset + 1 >= this.cuBuffer.getLength()
						|| Character.isWhitespace(this.cuBuffer.getChar(adjustedOffset + 1))) {
					while (adjustedOffset > 0 && Character.isWhitespace(this.cuBuffer.getChar(adjustedOffset)) ) {
						adjustedOffset--;
					}
				}
			}

			this.toComplete = NodeFinder.perform(this.unit, adjustedOffset, 0);
			this.expectedTypes = new ExpectedTypes(this.assistOptions, this.toComplete);
			ASTNode context = this.toComplete;
			String completeAfter = ""; //$NON-NLS-1$
			if (this.toComplete instanceof SimpleName simpleName) {
				int charCount = this.offset - simpleName.getStartPosition();
				if (!FAKE_IDENTIFIER.equals(simpleName.getIdentifier())) {
					completeAfter = simpleName.getIdentifier().substring(0, simpleName.getIdentifier().length() <= charCount ? simpleName.getIdentifier().length() : charCount);
				}
				if (this.cuBuffer != null) {
					if (this.cuBuffer.getChar(this.offset - 1) == '.') {
						completeAfter = ""; //$NON-NLS-1$
					}
				}
				if (simpleName.getParent() instanceof FieldAccess || simpleName.getParent() instanceof MethodInvocation
						|| simpleName.getParent() instanceof VariableDeclaration || simpleName.getParent() instanceof QualifiedName
						|| simpleName.getParent() instanceof SuperFieldAccess || simpleName.getParent() instanceof SingleMemberAnnotation
						|| simpleName.getParent() instanceof ExpressionMethodReference) {
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
			} else if (this.toComplete instanceof FieldAccess fieldAccess) {
				completeAfter = fieldAccess.getName().toString();
			} else if (this.toComplete instanceof MethodInvocation methodInvocation) {
				completeAfter = methodInvocation.getName().toString();
				if (this.cuBuffer != null) {
					if (this.cuBuffer.getChar(this.offset - 1) == '.') {
						completeAfter = ""; //$NON-NLS-1$
					}
				}
			} else if (this.toComplete instanceof NormalAnnotation || this.toComplete instanceof ExpressionMethodReference) {
				// handle potentially unrecovered/unparented identifier characters
				if (this.cuBuffer != null) {
					int cursor = this.offset;
					while (cursor > 0 && Character.isJavaIdentifierPart(this.cuBuffer.getChar(cursor - 1)) ) {
						cursor--;
					}
					completeAfter = this.cuBuffer.getText(cursor, this.offset - cursor);
				}
			}
			this.prefix = completeAfter;
			this.qualifiedPrefix = this.prefix;
			if (this.toComplete instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete != null && this.toComplete.getParent() instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete instanceof SimpleType simpleType && simpleType.getName() instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			}
			Bindings defaultCompletionBindings = new Bindings();
			Bindings specificCompletionBindings = new Bindings();
			var completionContext = new DOMCompletionContext(this.offset, completeAfter.toCharArray(),
					computeEnclosingElement(), defaultCompletionBindings::stream, expectedTypes);
			this.requestor.acceptContext(completionContext);

			// some flags to controls different applicable completion search strategies
			boolean suggestDefaultCompletions = true;

			checkCancelled();

			if (context instanceof StringLiteral || context instanceof TextBlock || context instanceof Comment || context instanceof Javadoc) {
				return;
			}
			if (context instanceof FieldAccess fieldAccess) {
				statementLikeKeywords();

				ITypeBinding fieldAccessType = fieldAccess.getExpression().resolveTypeBinding();
				if (fieldAccessType != null) {
					processMembers(fieldAccess, fieldAccess.getExpression().resolveTypeBinding(), specificCompletionBindings, false);
				}
				if (specificCompletionBindings.stream().findAny().isPresent()) {
					publishFromScope(specificCompletionBindings);
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
					if (type != null) {
						processMembers(expression, type, specificCompletionBindings, false);
						specificCompletionBindings.stream()
							.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
							.filter(IMethodBinding.class::isInstance)
							.map(binding -> toProposal(binding))
							.forEach(this.requestor::accept);
					}
					suggestDefaultCompletions = false;
				} else if (invocation.getName().getStartPosition() + invocation.getName().getLength() + 3 /* the three chars: `().` */ <= this.offset && this.prefix.isEmpty()) {
					// handle `myMethod().|`
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if (methodBinding != null) {
						ITypeBinding returnType = methodBinding.getReturnType();
						processMembers(invocation, returnType, specificCompletionBindings, false);
						specificCompletionBindings.stream()
							.map(binding -> toProposal(binding))
							.forEach(this.requestor::accept);
					}
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof VariableDeclaration declaration) {
				var binding = declaration.resolveBinding();
				if (binding != null) {
					this.variableDeclHandler.findVariableNames(binding, completeAfter, specificCompletionBindings).stream()
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
				if (context.getParent() instanceof AnnotationTypeMemberDeclaration) {
					suggestDefaultCompletions = false;
				}
				if (context.getLocationInParent().getId().equals(QualifiedName.QUALIFIER_PROPERTY.getId()) && context.getParent() instanceof QualifiedName) {
					// eg.
					// void myMethod() {
					//   String myVariable = "hello, mom";
					//   myVariable.|
					//   Object myObj = null;
					// }
					// It thinks that our variable is a package or some other type. We know that it's a variable.
					// Search the scope for the right binding
					IBinding incorrectBinding = ((SimpleName) context).resolveBinding();
					Bindings localBindings = new Bindings();
					scrapeAccessibleBindings(localBindings);
					Optional<IVariableBinding> realBinding = localBindings.stream() //
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
			if (context instanceof AbstractTypeDeclaration typeDecl) {
				if (this.cuBuffer != null) {
					int nameEndOffset = typeDecl.getName().getStartPosition() + typeDecl.getName().getLength();
					int bodyStart = nameEndOffset;
					while (bodyStart < this.cuBuffer.getLength() && this.cuBuffer.getChar(bodyStart) != '{') {
						bodyStart++;
					}
					int prefixCursor = this.offset;
					while (prefixCursor > 0 && !Character.isWhitespace(this.cuBuffer.getChar(prefixCursor - 1))) {
						prefixCursor--;
					}
					this.prefix = this.cuBuffer.getText(prefixCursor, this.offset - prefixCursor);
					if (nameEndOffset < this.offset && this.offset <= bodyStart) {
						String extendsOrImplementsContent = this.cuBuffer.getText(nameEndOffset, this.offset - nameEndOffset);
						if (extendsOrImplementsContent.indexOf("implements") < 0 && extendsOrImplementsContent.indexOf("extends") < 0) { //$NON-NLS-1$ //$NON-NLS-2$
							// public class Foo | {
							//
							// }
							boolean isInterface = typeDecl instanceof TypeDeclaration realTypeDecl && realTypeDecl.isInterface();
							if (CharOperation.prefixEquals(this.prefix.toCharArray(), Keywords.EXTENDS)) {
								this.requestor.accept(createKeywordProposal(Keywords.EXTENDS, this.offset, this.offset));
							}
							if (!isInterface && CharOperation.prefixEquals(this.prefix.toCharArray(), Keywords.IMPLEMENTS)) {
								this.requestor.accept(createKeywordProposal(Keywords.IMPLEMENTS, this.offset, this.offset));
							}
						} else if (extendsOrImplementsContent.indexOf("implements") < 0 //$NON-NLS-1$
									&& (Character.isWhitespace(this.cuBuffer.getChar(this.offset - 1)) || this.cuBuffer.getChar(this.offset - 1) == ',')) {
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
						findOverridableMethods(typeDeclBinding, this.modelUnit.getJavaProject(), null);
					}
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof QualifiedName qualifiedName) {
				IBinding qualifiedNameBinding = qualifiedName.getQualifier().resolveBinding();
				if (qualifiedNameBinding instanceof ITypeBinding qualifierTypeBinding && !qualifierTypeBinding.isRecovered()) {
					processMembers(qualifiedName, qualifierTypeBinding, specificCompletionBindings, true);
					publishFromScope(specificCompletionBindings);
					int startPos = this.offset;
					int endPos = this.offset;
					if ((qualifiedName.getName().getFlags() & ASTNode.MALFORMED) != 0) {
						startPos = qualifiedName.getName().getStartPosition();
						endPos = startPos + qualifiedName.getName().getLength();
					}

					if (!isFailedMatch(this.toComplete.toString().toCharArray(), Keywords.THIS)) {
						this.requestor.accept(createKeywordProposal(Keywords.THIS, startPos, endPos));
					}
					if (!isFailedMatch(this.toComplete.toString().toCharArray(), Keywords.SUPER)) {
						this.requestor.accept(createKeywordProposal(Keywords.SUPER, startPos, endPos));
					}
					if (!isFailedMatch(this.toComplete.toString().toCharArray(), Keywords.CLASS)) {
						this.requestor.accept(createClassKeywordProposal(qualifierTypeBinding, startPos, endPos));
					}

					suggestDefaultCompletions = false;
				} else if (qualifiedNameBinding instanceof IPackageBinding qualifierPackageBinding) {
					if (!qualifierPackageBinding.isRecovered()) {
						// start of a known package
						suggestPackages();
						// suggests types in the package
						suggestTypesInPackage(qualifierPackageBinding.getName());
						suggestDefaultCompletions = false;
					} else {
						// likely the start of an incomplete field/method access
						Bindings tempScope = new Bindings();
						scrapeAccessibleBindings(tempScope);
						Optional<ITypeBinding> potentialBinding = tempScope.stream() //
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
									throw new IllegalStateException("method, type var, etc. are likely not interpreted as a package"); //$NON-NLS-1$
								})
								.map(ITypeBinding.class::cast)
								.findFirst();
						if (potentialBinding.isPresent()) {
							processMembers(qualifiedName, potentialBinding.get(), specificCompletionBindings, false);
							publishFromScope(specificCompletionBindings);
							suggestDefaultCompletions = false;
						} else {
							// maybe it is actually a package?
							suggestPackages();
							// suggests types in the package
							suggestTypesInPackage(qualifierPackageBinding.getName());
							suggestDefaultCompletions = false;
						}
					}
				} else if (qualifiedNameBinding instanceof IVariableBinding variableBinding) {
					ITypeBinding typeBinding = variableBinding.getType();
					processMembers(qualifiedName, typeBinding, specificCompletionBindings, false);
					publishFromScope(specificCompletionBindings);
					suggestDefaultCompletions = false;
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
				} else if (methodDeclaration.getBody() != null && this.offset <= methodDeclaration.getBody().getStartPosition()) {
					completeThrowsClause(methodDeclaration, specificCompletionBindings);
					suggestDefaultCompletions = false;
				}
			}
			if (context instanceof ClassInstanceCreation cic) {
				ITypeBinding expectedType = null;
				if (cic.getParent() instanceof VariableDeclarationFragment vdf) {
					// relevant types
					if (vdf.getParent() instanceof VariableDeclarationStatement vds) {
						expectedType = vds.getType().resolveBinding();
					} else if (vdf.getParent() instanceof FieldDeclaration fieldDeclaration) {
						expectedType = fieldDeclaration.getType().resolveBinding();
					}
				} else if (cic.getParent() instanceof Assignment assignment) {
					expectedType = assignment.getLeftHandSide().resolveTypeBinding();
				}
				if (expectedType != null) {

					completeConstructor(expectedType, context, this.modelUnit.getJavaProject());
				} else {
					ASTNode current = this.toComplete;
					while (current != null) {
						specificCompletionBindings.addAll(visibleTypeBindings(current));
						current = current.getParent();
					}
					publishFromScope(specificCompletionBindings);
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
					specificCompletionBindings.methods.stream() //
							.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray()))
							.map(this::toProposal) //
							.forEach(this.requestor::accept);
				}
				suggestDefaultCompletions = false;
			}

			// check for accessible bindings to potentially turn into completions.
			// currently, this is always run, even when not using the default completion,
			// because method argument guessing uses it.
			scrapeAccessibleBindings(defaultCompletionBindings);

			if (suggestDefaultCompletions) {
				statementLikeKeywords();
				publishFromScope(defaultCompletionBindings);
				if (!completeAfter.isBlank()) {
					final int typeMatchRule = this.toComplete.getParent() instanceof Annotation
							? IJavaSearchConstants.ANNOTATION_TYPE
							: IJavaSearchConstants.TYPE;
					ExtendsOrImplementsInfo extendsOrImplementsInfo = isInExtendsOrImplements(this.toComplete);
					findTypes(completeAfter, typeMatchRule, null)
							.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
									type.getElementName().toCharArray()))
							.filter(type -> {
								return filterBasedOnExtendsOrImplementsInfo(type, extendsOrImplementsInfo);
							})
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
					.filter(name -> CharOperation.prefixEquals((this.qualifiedPrefix + ".").toCharArray(), name.toCharArray()) && name.length() > this.qualifiedPrefix.length()) //$NON-NLS-1$
					.filter(name -> this.pattern.matchesName(this.prefix.toCharArray(), name.toCharArray()))
					.map(pack -> toPackageProposal(pack, this.toComplete)).forEach(this.requestor::accept);
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}

	private void suggestTypesInPackage(String packageName) {
		if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
			List<IType> foundTypes = findTypes(this.prefix, packageName).toList();
			ExtendsOrImplementsInfo extendsOrImplementsInfo = isInExtendsOrImplements(this.toComplete);
			for (IType foundType : foundTypes) {
				if (this.pattern.matchesName(this.prefix.toCharArray(), foundType.getElementName().toCharArray())) {
					if (filterBasedOnExtendsOrImplementsInfo(foundType, extendsOrImplementsInfo)) {
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
	private static ExtendsOrImplementsInfo isInExtendsOrImplements(ASTNode completion) {
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
		completeAnnotationParams(normalAnnotation, definedKeys, scope);
	}

	private void completeAnnotationParams(Annotation annotation, Set<String> definedKeys, Bindings scope) {
		Arrays.stream(annotation.resolveTypeBinding().getDeclaredMethods()) //
			.filter(declaredMethod -> {
				return (declaredMethod.getModifiers() & Flags.AccStatic) == 0
						&& !definedKeys.contains(declaredMethod.getName().toString());
			}) //
			.forEach(scope::add);
		scope.methods.stream() //
				.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
				.map(binding -> toAnnotationAttributeRefProposal(binding))
				.forEach(this.requestor::accept);
	}

	private void publishFromScope(Bindings scope) {
		scope.stream() //
			.filter(binding -> this.pattern.matchesName(this.prefix.toCharArray(), binding.getName().toCharArray())) //
			.map(binding -> toProposal(binding))
			.forEach(this.requestor::accept);
	}

	private void completeConstructor(ITypeBinding typeBinding, ASTNode referencedFrom, IJavaProject javaProject) {
		// compute type hierarchy
		boolean isArray = typeBinding.isArray();
		IType typeHandle = ((IType)typeBinding.getJavaElement());
		if (typeHandle != null) {
			try {
				AbstractTypeDeclaration enclosingType = (AbstractTypeDeclaration) findParent(referencedFrom, new int[] { ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION, ASTNode.ANNOTATION_TYPE_DECLARATION });
				ITypeBinding enclosingTypeBinding = enclosingType.resolveBinding();
				IType enclosingTypeElement = (IType) enclosingTypeBinding.getJavaElement();
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
				IBinding[] descendantBindings = parser.createBindings(subtypes.toArray(IType[]::new), new NullProgressMonitor());
				for (IBinding descendantBinding : descendantBindings) {
					if (descendantBinding instanceof ITypeBinding descendantBindingType && CharOperation.prefixEquals(this.prefix.toCharArray(), descendantBindingType.getName().toCharArray())) {
						if (isArray) {
							this.requestor.accept(toProposal(descendantBinding));
						} else {
							List<CompletionProposal> proposals = toConstructorProposals(descendantBindingType, referencedFrom);
							for (CompletionProposal proposal : proposals) {
								this.requestor.accept(proposal);
							}
						}
					}
				}
			} catch (JavaModelException e) {
				ILog.get().error("Unable to compute type hierarchy while performing completion", e); //$NON-NLS-1$
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

	private void processMembers(ASTNode referencedFrom, ITypeBinding typeBinding, Bindings scope, boolean isStaticContext) {
		AbstractTypeDeclaration parentType = (AbstractTypeDeclaration)findParent(referencedFrom, new int[] {ASTNode.ANNOTATION_TYPE_DECLARATION, ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION});
		if (parentType == null) {
			return;
		}
		ITypeBinding referencedFromBinding = parentType.resolveBinding();
		boolean includePrivate = referencedFromBinding.getKey().equals(typeBinding.getKey());
		MethodDeclaration methodDeclaration = (MethodDeclaration)findParent(referencedFrom, new int[] {ASTNode.METHOD_DECLARATION});
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
				new HashSet<>(), new HashSet<>());
	}

	private void processMembers(ITypeBinding typeBinding, Bindings scope,
			boolean includePrivate,
			boolean includeProtected,
			String originalPackageKey,
			boolean isStaticContext,
			boolean canUseAbstract,
			Set<String> impossibleMethods,
			Set<String> impossibleFields) {
		if (typeBinding == null) {
			return;
		}
		Predicate<IBinding> accessFilter = binding -> {
			boolean field = binding instanceof IVariableBinding;
			if (field) {
				if (impossibleFields.contains(binding.getName())) {
					return false;
				}
			} else {
				if (impossibleMethods.contains(binding.getName())) {
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
					|| (isStaticContext && (binding.getModifiers() & Flags.AccStatic) == 0)
					// check abstract
					|| (!canUseAbstract && (binding.getModifiers() & Flags.AccAbstract) != 0)
					) {
				if (field) {
					impossibleFields.add(binding.getName());
				} else {
					impossibleMethods.add(binding.getName());
				}
				return false;
			}
			return true;
		};
		Arrays.stream(typeBinding.getDeclaredFields()) //
			.filter(accessFilter) //
			.forEach(scope::add);
		Arrays.stream(typeBinding.getDeclaredMethods()) //
			.filter(accessFilter) //
			.forEach(scope::add);
		if (typeBinding.getInterfaces() != null) {
			for (ITypeBinding superinterfaceBinding : typeBinding.getInterfaces()) {
				processMembers(superinterfaceBinding, scope, false, includeProtected, originalPackageKey, isStaticContext, true, impossibleMethods, impossibleFields);
			}
		}
		ITypeBinding superclassBinding = typeBinding.getSuperclass();
		if (superclassBinding != null) {
			processMembers(superclassBinding, scope, false, includeProtected, originalPackageKey, isStaticContext, true, impossibleMethods, impossibleFields);
		}
	}

	private static boolean findInSupers(ITypeBinding root, ITypeBinding toFind) {
		String keyToFind = toFind.getErasure().getKey();
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
		if (binding instanceof ITypeBinding) {
			kind = CompletionProposal.TYPE_REF;
		} else if (binding instanceof IMethodBinding m) {
			if (findParent(this.toComplete, new int[] { ASTNode.EXPRESSION_METHOD_REFERENCE }) != null) {
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

		InternalCompletionProposal res = new InternalCompletionProposal(kind, this.offset);
		res.setName(binding.getName().toCharArray());
		if (kind == CompletionProposal.METHOD_REF) {
			completion += "()"; //$NON-NLS-1$
		}
		res.setCompletion(completion.toCharArray());
		res.setFlags(binding.getModifiers());

		if (kind == CompletionProposal.METHOD_REF || kind == CompletionProposal.METHOD_NAME_REFERENCE) {
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
			if (declaringClass != null) {
				char[] declSignature = Signature
						.createTypeSignature(
								variableBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
						.toCharArray();
				res.setDeclarationSignature(declSignature);
			} else {
				res.setDeclarationSignature(new char[0]);
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

		if (this.toComplete instanceof SimpleName && !this.toComplete.getLocationInParent().getId().equals(QualifiedName.QUALIFIER_PROPERTY.getId()) && !this.prefix.isEmpty()) {
			res.setReplaceRange(this.toComplete.getStartPosition(), this.offset);
		} else {
			setRange(res);
		}
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
		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.TYPE_REF, this.offset);
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
		while (cursor instanceof IType) {
			if (!completion.isEmpty()) {
				completion.insert(0, '.');
			}
			completion.insert(0, cursor.getElementName());
			cursor = cursor.getParent();
		}
		AbstractTypeDeclaration parentType = (AbstractTypeDeclaration)findParent(this.toComplete,
				new int[] {ASTNode.TYPE_DECLARATION,
						ASTNode.ANNOTATION_TYPE_DECLARATION,
						ASTNode.RECORD_DECLARATION,
						ASTNode.ENUM_DECLARATION});
		IPackageBinding currentPackageBinding = parentType == null ? null : parentType.resolveBinding().getPackage();
		if (packageFrag != null && (currentPackageBinding == null
				|| (!packageFrag.getElementName().equals(currentPackageBinding.getName())
				 && !packageFrag.getElementName().equals("java.lang")))) { //$NON-NLS-1$
			completion.insert(0, '.');
			completion.insert(0, packageFrag.getElementName());
		}
		res.setCompletion(completion.toString().toCharArray());

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
			if (type.isInterface()) {
				relevance += RelevanceConstants.R_INTERFACE;
			}
		} catch (JavaModelException e) {
			// do nothing
		}
		res.setRelevance(relevance);
		// set defaults for now to avoid error downstream
		res.setRequiredProposals(new CompletionProposal[] { toImportProposal(simpleName, signature) });
		return res;
	}

	private List<CompletionProposal> toConstructorProposals(ITypeBinding typeBinding, ASTNode referencedFrom) {

		List<CompletionProposal> proposals = new ArrayList<>();

		AbstractTypeDeclaration parentType = (AbstractTypeDeclaration)findParent(referencedFrom, new int[] {ASTNode.ANNOTATION_TYPE_DECLARATION, ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION});
		if (parentType == null) {
			return proposals;
		}

		ITypeBinding referencedFromBinding = parentType.resolveBinding();
		boolean includePrivate = referencedFromBinding.getKey().equals(typeBinding.getKey());
		MethodDeclaration methodDeclaration = (MethodDeclaration)findParent(referencedFrom, new int[] {ASTNode.METHOD_DECLARATION});
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

		if (!this.requestor.isIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION) && typeBinding.isInterface()) {
			// create an anonymous declaration: `new MyInterface() { }`;
			proposals.add(toAnonymousConstructorProposal(typeBinding));
			// TODO: JDT allows completing the constructors declared on an abstract class,
			// without adding a body to these instance creations.
			// This doesn't make sense, since the abstract methods need to be implemented.
			// We should consider making those completion items here instead
		} else {
			for (IMethodBinding typesMethod: typeBinding.getDeclaredMethods()) {
				if (typesMethod.isConstructor()
						// public
						&& ((typesMethod.getModifiers() & Flags.AccPublic) != 0
						// protected
						|| (includeProtected && (typesMethod.getModifiers() & Flags.AccProtected) != 0)
						// private
						|| (includePrivate && (typesMethod.getModifiers() & Flags.AccPrivate) != 0)
						// package private
						||((typesMethod.getModifiers() & (Flags.AccPrivate | Flags.AccProtected | Flags.AccPublic)) == 0 && typeBinding.getPackage().getKey().equals(referencedFromBinding.getPackage().getKey())))) {
					proposals.add(toConstructorProposal(typesMethod));
				}
			}
		}

		return proposals;
	}

	private CompletionProposal toConstructorProposal(IMethodBinding methodBinding) {
		InternalCompletionProposal res = createProposal(CompletionProposal.CONSTRUCTOR_INVOCATION);
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		char[] simpleName = methodBinding.getName().toCharArray();
		char[] signature = methodBinding.getKey().replace('/', '.').toCharArray();
		res.setCompletion(new char[] {'(', ')'});
		res.setName(simpleName);
		res.setSignature(signature);
		res.setOriginalSignature(signature);

		res.setDeclarationSignature(Signature.createTypeSignature(declaringClass.getQualifiedName(), true).toCharArray());
		res.setDeclarationTypeName(simpleName);
		res.setDeclarationPackageName(declaringClass.getPackage().getName().toCharArray());
		res.setParameterPackageNames(CharOperation.NO_CHAR_CHAR);
		res.setParameterTypeNames(CharOperation.NO_CHAR_CHAR);

		if (methodBinding.getParameterNames().length == 0) {
			res.setParameterNames(CharOperation.NO_CHAR_CHAR);
		} else {
			char[][] paramNamesCharChar = Stream.of(methodBinding.getParameterNames()) //
					.map(String::toCharArray)
					.toArray(char[][]::new);
			res.setParameterNames(paramNamesCharChar);
		}

		res.setIsContructor(true);
		if (declaringClass.isGenericType()) {
			res.setDeclarationTypeVariables(Stream.of(declaringClass.getTypeParameters()).map(a -> a.getName().toCharArray()).toArray(char[][]::new));
		}
		res.setCompatibleProposal(true);

		res.setReplaceRange(this.offset, this.offset);
		res.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		res.setFlags(methodBinding.getModifiers());

		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_EXACT_EXPECTED_TYPE
				+ RelevanceConstants.R_UNQUALIFIED
				+ RelevanceConstants.R_NON_RESTRICTED
				+ RelevanceConstants.R_CONSTRUCTOR;
		if (declaringClass.isAnnotation()) {
			relevance += RelevanceConstants.R_ANNOTATION;
		} else if (declaringClass.isInterface()) {
			relevance += RelevanceConstants.R_INTERFACE;
		} else if (declaringClass.isClass()) {
			relevance += RelevanceConstants.R_CLASS;
		}
		res.setRelevance(relevance);

		CompletionProposal typeProposal = toProposal(methodBinding.getDeclaringClass());
		if (this.toComplete instanceof SimpleName) {
			typeProposal.setReplaceRange(this.toComplete.getStartPosition(), this.offset);
			typeProposal.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		} else {
			typeProposal.setReplaceRange(this.offset, this.offset);
			typeProposal.setTokenRange(this.offset, this.offset);
		}
		StringBuilder typeCompletion = new StringBuilder(Signature.createTypeSignature(methodBinding.getDeclaringClass().getQualifiedName(), true));
		typeCompletion.append('.');
		typeCompletion.append(methodBinding.getName());
		typeProposal.setCompletion(typeCompletion.toString().toCharArray());
		typeProposal.setRequiredProposals(null);

		res.setRequiredProposals(new CompletionProposal[] { typeProposal });
		return res;
	}

	private CompletionProposal toAnonymousConstructorProposal(ITypeBinding typeBinding) {
		InternalCompletionProposal res = createProposal(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION);
		res.setDeclarationSignature(Signature.createTypeSignature(typeBinding.getQualifiedName(), true).toCharArray());
		res.setDeclarationKey(typeBinding.getKey().toCharArray());
		res.setSignature(
				CompletionEngine.createMethodSignature(
						CharOperation.NO_CHAR_CHAR,
						CharOperation.NO_CHAR_CHAR,
						CharOperation.NO_CHAR,
						CharOperation.NO_CHAR));
		res.setDeclarationPackageName(typeBinding.getPackage().getName().toCharArray());
		res.setDeclarationTypeName(typeBinding.getName().toCharArray());
		res.setName(typeBinding.getName().toCharArray());

		int relevance = RelevanceConstants.R_DEFAULT;
		relevance += RelevanceConstants.R_RESOLVED;
		relevance += RelevanceConstants.R_INTERESTING;
		relevance += computeRelevanceForCaseMatching(this.prefix.toCharArray(), typeBinding.getName().toCharArray(), this.assistOptions);
		relevance += RelevanceConstants.R_EXACT_EXPECTED_TYPE;
		relevance += RelevanceConstants.R_UNQUALIFIED;
		relevance += RelevanceConstants.R_NON_RESTRICTED;
		if (typeBinding.getPackage().getName().startsWith("java.")) { //$NON-NLS-1$
			relevance += RelevanceConstants.R_JAVA_LIBRARY;
		}

		if(typeBinding.isClass()) {
			relevance += RelevanceConstants.R_CLASS;
//			relevance += computeRelevanceForException(typeName); // TODO:
		} else if(typeBinding.isEnum()) {
			relevance += RelevanceConstants.R_ENUM;
		} else if(typeBinding.isInterface()) {
			relevance += RelevanceConstants.R_INTERFACE;
		}

		InternalCompletionProposal typeProposal = createProposal(CompletionProposal.TYPE_REF);
		typeProposal.setDeclarationSignature(typeBinding.getPackage().getName().toCharArray());
		typeProposal.setSignature(Signature.createTypeSignature(typeBinding.getQualifiedName(), true).toCharArray());
		typeProposal.setPackageName(typeBinding.getPackage().getName().toCharArray());
		typeProposal.setTypeName(typeBinding.getName().toCharArray());
		typeProposal.setCompletion(typeBinding.getName().toCharArray());
		typeProposal.setFlags(typeBinding.getModifiers());
		typeProposal.setReplaceRange(this.offset, this.offset);
		typeProposal.setTokenRange(this.offset, this.offset);
		typeProposal.setRelevance(relevance);
		res.setRequiredProposals( new CompletionProposal[]{typeProposal});

		res.setCompletion(new char[] {'(', ')'});
		res.setFlags(Flags.AccPublic);
		res.setReplaceRange(this.offset, this.offset);
		res.setTokenRange(this.toComplete.getStartPosition(), this.offset);
		res.setRelevance(relevance);
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

	private CompletionProposal toAnnotationAttributeRefProposal(IMethodBinding method) {
		CompletionProposal proposal = createProposal(CompletionProposal.ANNOTATION_ATTRIBUTE_REF);
		proposal.setDeclarationSignature(DOMCompletionEngineBuilder.getSignature(method.getDeclaringClass()));
		proposal.setSignature(DOMCompletionEngineBuilder.getSignature(method.getReturnType()));
		proposal.setName(method.getName().toCharArray());
		// add "=" to completion since it will always be needed
		char[] completion= method.getName().toCharArray();
		if (JavaCore.INSERT.equals(this.modelUnit.getJavaProject().getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, true))) {
			completion= CharOperation.concat(completion, new char[] {' '});
		}
		completion= CharOperation.concat(completion, new char[] {'='});
		if (JavaCore.INSERT.equals(this.modelUnit.getJavaProject().getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, true))) {
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
				+ computeRelevanceForCaseMatching(this.prefix.toCharArray(), method.getName().toCharArray(), this.assistOptions)
				+ RelevanceConstants.R_UNQUALIFIED
				+ RelevanceConstants.R_NON_RESTRICTED;
		proposal.setRelevance(relevance);
		return proposal;
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
					} else if (proposalType.getPackage() != null && proposalType.getPackage().isUnnamed()) {
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
		InternalCompletionProposal proposal = new DOMInternalCompletionProposal(kind, this.offset);
		proposal.nameLookup = this.nameEnvironment.nameLookup;
		proposal.completionEngine = this.nestedEngine;
		return proposal;
	}

	private static class DOMInternalCompletionProposal extends InternalCompletionProposal {

		public DOMInternalCompletionProposal(int kind, int completionLocation) {
			super(kind, completionLocation);
		}

		@Override
		public boolean canUseDiamond(CompletionContext coreContext) {
			// ECJ-based implementation uses a downcast,
			// so re-implement this method with our own downcast
			if (!coreContext.isExtended()) return false;
			if (coreContext instanceof DOMCompletionContext domCompletionContext) {
				char[] name1 = this.declarationPackageName;
				char[] name2 = this.declarationTypeName;
				char[] declarationType = CharOperation.concat(name1, name2, '.');  // fully qualified name
				// even if the type arguments used in the method have been substituted,
				// extract the original type arguments only, since thats what we want to compare with the class
				// type variables (Substitution might have happened when the constructor is coming from another
				// CU and not the current one).
				char[] sign = (this.originalSignature != null)? this.originalSignature : getSignature();
				if (!(sign == null || sign.length < 2)) {
					sign = Signature.removeCapture(sign);
				}
				char[][] types= Signature.getParameterTypes(sign);
				String[] paramTypeNames= new String[types.length];
				for (int i= 0; i < types.length; i++) {
					paramTypeNames[i]= new String(Signature.toCharArray(types[i]));
				}
				if (this.getDeclarationTypeVariables() != null) {
					return domCompletionContext.canUseDiamond(paramTypeNames, this.getDeclarationTypeVariables());
				}
				return domCompletionContext.canUseDiamond(paramTypeNames, declarationType);
			}
			return false;
		}

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
		while (cursor < this.cuBuffer.getLength()
				&& Character.isJavaIdentifierPart(this.cuBuffer.getChar(cursor))) {
			cursor++;
		}
		completionProposal.setReplaceRange(startPos, cursor);
		completionProposal.setTokenRange(startPos, cursor);
	}

}
