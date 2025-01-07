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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
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
			this.others.add(binding);
		}
		public void addAll(Collection<? extends IBinding> bindings) {
			bindings.forEach(this::add);
		}
		public Stream<IBinding> all() {
			return this.others.stream();
		}
		public Stream<IMethodBinding> methods() {
			return all().filter(IMethodBinding.class::isInstance).map(IMethodBinding.class::cast);
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

		if (node instanceof Block block) {
			var bindings = ((List<Statement>) block.statements()).stream()
					.filter(statement -> statement.getStartPosition() < this.offset)
					.filter(VariableDeclarationStatement.class::isInstance)
					.map(VariableDeclarationStatement.class::cast)
					.flatMap(decl -> ((List<VariableDeclarationFragment>)decl.fragments()).stream())
					.filter(frag -> !FAKE_IDENTIFIER.equals(frag.getName().toString()))
					.map(VariableDeclarationFragment::resolveBinding)
					.toList();
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

	@Override
	public void run() {
		if (this.monitor != null) {
			this.monitor.beginTask(Messages.engine_completing, IProgressMonitor.UNKNOWN);
		}
		this.requestor.beginReporting();
		try {
			Bindings defaultCompletionBindings = new Bindings();
			Bindings specificCompletionBindings = new Bindings();
//			var completionContext = new DOMCompletionContext(this.offset, completeAfter.toCharArray(),
//					computeEnclosingElement(), defaultCompletionBindings::stream, expectedTypes, this.toComplete);
			var completionContext = new DOMCompletionContext(this.unit, this.modelUnit, this.cuBuffer, this.offset, this.assistOptions, defaultCompletionBindings);
			this.nestedEngine.completionToken = completionContext.getToken();
			this.requestor.acceptContext(completionContext);

			this.expectedTypes = completionContext.expectedTypes;
			char[] token = completionContext.getToken();
			String completeAfter = token == null ? new String() : new String(token);
			ASTNode context = completionContext.node;
			this.toComplete = completionContext.node;
			if (completionContext.node instanceof SimpleName simpleName) {
				int charCount = this.offset - simpleName.getStartPosition();
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
			} else if (this.toComplete instanceof TextElement textElement) {
				if (offset >= textElement.getStartPosition() + textElement.getLength()) {
					ASTNode parent = textElement.getParent();
					if (parent instanceof TagElement tagElement && TagElement.TAG_PARAM.equals(tagElement.getTagName())) {
						context = tagElement;
					} else {
						while (parent != null && !(parent instanceof Javadoc)) {
							parent = parent.getParent();
						}
						if (parent instanceof Javadoc javadoc) {
							context = javadoc.getParent();
						}
					}
				} else {
					context = textElement.getParent();
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
			}
			this.prefix = token == null ? new String() : new String(token);
			this.qualifiedPrefix = this.prefix;
			if (this.toComplete instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete != null && this.toComplete.getParent() instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			} else if (this.toComplete instanceof SimpleType simpleType && simpleType.getName() instanceof QualifiedName qualifiedName) {
				this.qualifiedPrefix = qualifiedName.getQualifier().toString();
			}
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
					processMembers(fieldAccess, fieldAccessExpr.resolveTypeBinding(), specificCompletionBindings, false);
					publishFromScope(specificCompletionBindings);
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
					// args
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if (methodBinding == null && this.toComplete == invocation) {
						// myMethod(|), where myMethod does not exist
						suggestDefaultCompletions = false;
					} else {
						for (ITypeBinding param : this.expectedTypes.getExpectedTypes()) {
							IMethodBinding potentialLambda = param.getFunctionalInterfaceMethod();
							if (potentialLambda != null) {
								this.requestor.accept(createLambdaExpressionProposal(potentialLambda));
							}
						}
					}
				}
			}
			if (context instanceof VariableDeclaration declaration) {
				if (declaration.getName() == this.toComplete) {
					suggestDefaultCompletions = false;
				}
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
					ExtendsOrImplementsInfo extendsOrImplementsInfo = isInExtendsOrImplements(this.toComplete);
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
								return filterBasedOnExtendsOrImplementsInfo(type, extendsOrImplementsInfo);
							})
							.map(this::toProposal).forEach(this.requestor::accept);
					}
					if (!this.requestor.isIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION)) {
						int cursorStart = this.offset - this.prefix.length() - 1;
						while (cursorStart > 0 && Character.isWhitespace(this.cuBuffer.getChar(cursorStart))) {
							cursorStart--;
						}
						int cursorEnd = cursorStart;
						while (cursorEnd > 0 && Character.isJavaIdentifierPart(this.cuBuffer.getChar(cursorEnd - 1))) {
							cursorEnd--;
						}
						boolean suggest = true;
						if (cursorStart != cursorEnd) {
							String potentialModifier = this.cuBuffer.getText(cursorEnd, cursorStart - cursorEnd + 1);
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
				if (context.getParent() instanceof MemberValuePair) {
					// TODO: most of the time a constant value is expected,
					// however if an enum is expected, we can build out the completion for that
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
						&& this.modelUnit.getJavaProject().getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true).equals(JavaCore.ENABLED)) {
					for (Modifier modifier : (List<Modifier>)importDeclaration.modifiers()) {
						if (modifier.getKeyword() == ModifierKeyword.MODULE_KEYWORD) {
							findModules(this.qualifiedPrefix.toCharArray(), this.modelUnit.getJavaProject(), this.assistOptions, Collections.emptySet());
							suggestDefaultCompletions = false;
							break;
						}
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
				ImportDeclaration importDecl = (ImportDeclaration)DOMCompletionUtil.findParent(context, new int[] { ASTNode.IMPORT_DECLARATION });
				if (importDecl != null
						&& importDecl.getAST().apiLevel() >= AST.JLS23
						&& this.modelUnit.getJavaProject().getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true).equals(JavaCore.ENABLED)
						&& importDecl.modifiers().stream().anyMatch(node -> node instanceof Modifier modifier && modifier.getKeyword() == ModifierKeyword.MODULE_KEYWORD)) {
					findModules((this.qualifiedPrefix + "." + this.prefix).toCharArray(), this.modelUnit.getJavaProject(), this.assistOptions, Collections.emptySet()); //$NON-NLS-1$
					suggestDefaultCompletions = false;
				} else {
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
						if (!(this.toComplete instanceof Type)) {
							ITypeBinding currentTypeBinding = DOMCompletionUtil.findParentTypeDeclaration(context).resolveBinding();
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
								publishFromScope(specificCompletionBindings);
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
							parser.setProject(this.modelUnit.getJavaProject());
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
					completeConstructor(this.expectedTypes.getExpectedTypes().get(0), context, this.modelUnit.getJavaProject());
				} else {
					if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF) && !this.requestor.isIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION)) {
						String packageName = "";//$NON-NLS-1$
						try {
							IPackageDeclaration[] packageDecls = this.modelUnit.getPackageDeclarations();
							if (packageDecls != null && packageDecls.length > 0) {
								packageName = packageDecls[0].getElementName();
							}
						} catch (JavaModelException e) {
							// do nothing
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
				completionContext.setInJavadoc(true);
				if (tagElement.fragments().indexOf(this.toComplete) < 0) {
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
								if (this.cuBuffer != null) {
									while (endPos < this.cuBuffer.getLength() && !Character.isWhitespace(this.cuBuffer.getChar(endPos))) {
										endPos++;
									}
								}
								String paramPrefix = this.cuBuffer.getText(start, endPos - start);

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
						}
					} else {
						// the tag name is null, so this is probably a broken conversion
						suggestDefaultCompletions = false;
					}
				}
			}
			if (context instanceof ImportDeclaration) {
				if (context.getAST().apiLevel() >= AST.JLS23
						&& this.modelUnit.getJavaProject().getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true).equals(JavaCore.ENABLED)) {
					findModules(this.qualifiedPrefix.toCharArray(), this.modelUnit.getJavaProject(), this.assistOptions, Collections.emptySet());
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

			// check for accessible bindings to potentially turn into completions.
			// currently, this is always run, even when not using the default completion,
			// because method argument guessing uses it.
			scrapeAccessibleBindings(defaultCompletionBindings);
			if (shouldSuggestPackages(toComplete)) {
				suggestPackages(toComplete);
			}

			if (suggestDefaultCompletions) {
				ExtendsOrImplementsInfo extendsOrImplementsInfo = isInExtendsOrImplements(this.toComplete);
				statementLikeKeywords();
				if (!this.prefix.isEmpty() && extendsOrImplementsInfo == null) {
					suggestTypeKeywords(DOMCompletionUtil.findParent(this.toComplete, new int[] { ASTNode.BLOCK }) == null);
				}
				publishFromScope(defaultCompletionBindings);
				if (!completeAfter.isBlank()) {
					final int typeMatchRule = this.toComplete.getParent() instanceof Annotation
							? IJavaSearchConstants.ANNOTATION_TYPE
							: IJavaSearchConstants.TYPE;
					if (!this.requestor.isIgnored(CompletionProposal.TYPE_REF)) {
						findTypes(completeAfter, typeMatchRule, null)
							.filter(type -> {
								return defaultCompletionBindings.all().map(typeBinding -> typeBinding.getJavaElement()).noneMatch(elt -> type.equals(elt));
							})
							.filter(type -> this.pattern.matchesName(this.prefix.toCharArray(),
									type.getElementName().toCharArray()))
							.filter(type -> {
								return filterBasedOnExtendsOrImplementsInfo(type, extendsOrImplementsInfo);
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
		} finally {
			this.requestor.endReporting();
			if (this.monitor != null) {
				this.monitor.done();
			}
		}
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
		if (this.cuBuffer != null) {
			while (endPos < this.cuBuffer.getLength() && !Character.isWhitespace(this.cuBuffer.getChar(endPos))) {
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
		IJavaProject project = this.modelUnit.getJavaProject();
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
		for (char[] blockTag : DOMCompletionEngineJavadocUtil.getJavadocBlockTags(this.modelUnit.getJavaProject(), tagNode)) {
			if (!isFailedMatch(this.prefix.toCharArray(), blockTag)) {
				this.requestor.accept(toJavadocBlockTagProposal(blockTag));
			}
		}
	}

	private void completeJavadocInlineTags(TagElement tagNode) {
		if (this.requestor.isIgnored(CompletionProposal.JAVADOC_INLINE_TAG)) {
			return;
		}
		for (char[] blockTag : DOMCompletionEngineJavadocUtil.getJavadocInlineTags(this.modelUnit.getJavaProject(), tagNode)) {
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
			if (this.cuBuffer != null) {
				String text = this.cuBuffer.getText(startScanIndex, this.offset - startScanIndex);
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
			.map(binding -> toProposal(binding))
			.forEach(this.requestor::accept);
	}

	private void completeConstructor(ITypeBinding typeBinding, ASTNode referencedFrom, IJavaProject javaProject) {
		// compute type hierarchy
		boolean isArray = typeBinding.isArray();
		IType typeHandle = ((IType)typeBinding.getJavaElement());
		AbstractTypeDeclaration enclosingType = (AbstractTypeDeclaration) DOMCompletionUtil.findParent(referencedFrom, new int[] { ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION, ASTNode.ANNOTATION_TYPE_DECLARATION });
		ITypeBinding enclosingTypeBinding = enclosingType.resolveBinding();
		IType enclosingTypeElement = (IType) enclosingTypeBinding.getJavaElement();
		if (typeHandle != null) {
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
			if (binding == null) {
				return false;
			}
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

		InternalCompletionProposal res = new InternalCompletionProposal(kind, this.offset);
		res.setName(binding.getName().toCharArray());
		if (kind == CompletionProposal.METHOD_REF) {
			completion += "()"; //$NON-NLS-1$
		}
		res.setCompletion(completion.toCharArray());
		res.setFlags(binding.getModifiers());

		boolean inheritedValue = false;
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
			if (!methodBinding.getDeclaringClass().getQualifiedName().isEmpty()) {
				res.setDeclarationSignature(Signature
						.createTypeSignature(methodBinding.getDeclaringClass().getQualifiedName().toCharArray(), true)
						.toCharArray());
			}

			if ((methodBinding.getModifiers() & Flags.AccStatic) != 0) {
				ITypeBinding topLevelClass = methodBinding.getDeclaringClass();
				while (topLevelClass.getDeclaringClass() != null) {
					topLevelClass = topLevelClass.getDeclaringClass();
				}
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
				if (!inheritedValue && !this.modelUnit.getType(topLevelClass.getName()).exists()) {
					if (this.qualifiedPrefix.equals(this.prefix) && !this.modelUnit.getJavaProject().getOption(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, true).equals(JavaCore.DISABLED)) {
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
				if (!inheritedValue && !this.modelUnit.getType(topLevelClass.getName()).exists()) {
					if (this.qualifiedPrefix.equals(this.prefix) && !this.modelUnit.getJavaProject().getOption(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, true).equals(JavaCore.DISABLED)) {
						res.setRequiredProposals(new CompletionProposal[] { toStaticImportProposal(variableBinding) });
					} else {
						ITypeBinding directParentClass = variableBinding.getDeclaringClass();
						res.setRequiredProposals(new CompletionProposal[] { toStaticImportProposal(directParentClass) });
						if (this.toComplete.getLocationInParent() != QualifiedName.NAME_PROPERTY &&
							this.toComplete.getLocationInParent() != FieldAccess.NAME_PROPERTY &&
							this.toComplete.getLocationInParent() != NameQualifiedType.NAME_PROPERTY) {
							StringBuilder builder = new StringBuilder(new String(res.getCompletion()));
							builder.insert(0, '.');
							builder.insert(0, directParentClass.getName());
							res.setCompletion(builder.toString().toCharArray());
						}
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
				(res.getRequiredProposals() != null ? 0 : computeRelevanceForQualification(false)) +
				CompletionEngine.computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE) + //no access restriction for class field
				(!staticOnly() || inheritedValue ? 0 : RelevanceConstants.R_NON_INHERITED) // TODO: when is this active?
				);
		if (res.getRequiredProposals() != null) {
			for (CompletionProposal req : res.getRequiredProposals()) {
				req.setRelevance(res.getRelevance());
			}
		}
		return res;
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
		AbstractTypeDeclaration parentTypeDeclaration = DOMCompletionUtil.findParentTypeDeclaration(this.toComplete);
		if (parentTypeDeclaration != null && type.getFullyQualifiedName().equals(((IType)parentTypeDeclaration.resolveBinding().getJavaElement()).getFullyQualifiedName())) {
			completion.insert(0, cursor.getElementName());
		} else {
			while (cursor instanceof IType) {
				if (!completion.isEmpty()) {
					completion.insert(0, '.');
				}
				completion.insert(0, cursor.getElementName());
				cursor = cursor.getParent();
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
			if (lastOffset >= this.cuBuffer.getLength() || this.cuBuffer.getChar(lastOffset) != ';') {
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
		boolean fromCurrentCU = this.modelUnit.equals(type.getCompilationUnit());
		boolean typeIsImported = false;
		boolean inSamePackage = false;
		try {
			typeIsImported = Stream.of(this.modelUnit.getImports()).anyMatch(id -> {
				return id.getElementName().equals(type.getFullyQualifiedName());
			});
			IPackageDeclaration[] packageDecls = this.modelUnit.getPackageDeclarations();
			if (packageDecls != null && packageDecls.length > 0) {
				inSamePackage = this.modelUnit.getPackageDeclarations()[0].getElementName().equals(type.getPackageFragment().getElementName());
			} else {
				inSamePackage = type.getPackageFragment().getElementName().isEmpty();
			}
		} catch (JavaModelException e) {
			// there are sensible default set if accessing the model fails
		}
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;
		int relevance = RelevanceConstants.R_DEFAULT
				+ RelevanceConstants.R_RESOLVED
				+ RelevanceConstants.R_INTERESTING
				+ RelevanceConstants.R_NON_RESTRICTED
				+ computeRelevanceForQualification(!nodeInImports && !fromCurrentCU && !inSamePackage && !typeIsImported);
		relevance += computeRelevanceForCaseMatching(this.prefix.toCharArray(), simpleName, this.assistOptions);
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
			try {
				if (this.modelUnit.getPackageDeclarations() != null && this.modelUnit.getPackageDeclarations().length > 0) {
					packageName = this.modelUnit.getPackageDeclarations()[0].getElementName();
				}
			} catch (JavaModelException e) {
				// do nothing
			}
			if (!packageName.equals(type.getPackageFragment().getElementName())) {
				// propose importing the type
				res.setRequiredProposals(new CompletionProposal[] { toImportProposal(simpleName, signature, type.getPackageFragment().getElementName().toCharArray()) });
			}
		}
		return res;
	}

	private CompletionProposal toNewMethodProposal(ITypeBinding parentType, String newMethodName) {
		InternalCompletionProposal res =  createProposal(CompletionProposal.POTENTIAL_METHOD_DECLARATION);
		res.setDeclarationSignature(DOMCompletionEngineBuilder.getSignature(parentType));
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
		InternalCompletionProposal res = createProposal(CompletionProposal.CONSTRUCTOR_INVOCATION);
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
				+ computeRelevanceForCaseMatching(this.prefix.toCharArray(), simpleName, this.assistOptions);
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
		InternalCompletionProposal res = createProposal(CompletionProposal.CONSTRUCTOR_INVOCATION);
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
		InternalCompletionProposal res = createProposal(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION);
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
		relevance += computeRelevanceForCaseMatching(this.prefix.toCharArray(), type.getElementName().toCharArray(), this.assistOptions);
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

		InternalCompletionProposal typeProposal = createProposal(CompletionProposal.TYPE_REF);
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
		InternalCompletionProposal res = new InternalCompletionProposal(CompletionProposal.TYPE_IMPORT, this.offset);
		res.setName(simpleName);
		res.setSignature(signature);
		res.setPackageName(packageName);
		res.completionEngine = this.nestedEngine;
		res.nameLookup = this.nameEnvironment.nameLookup;
		return res;
	}

	private CompletionProposal toStaticImportProposal(IBinding binding) {
		InternalCompletionProposal res = null;
		if (binding instanceof IMethodBinding methodBinding) {
			res = createProposal(CompletionProposal.METHOD_IMPORT);
			res.setName(methodBinding.getName().toCharArray());
			res.setSignature(DOMCompletionEngineBuilder.getSignature(methodBinding));

			res.setDeclarationSignature(DOMCompletionEngineBuilder.getSignature(methodBinding.getDeclaringClass()));
			res.setSignature(DOMCompletionEngineBuilder.getSignature(methodBinding));
			if(methodBinding != methodBinding.getMethodDeclaration()) {
				res.setOriginalSignature(DOMCompletionEngineBuilder.getSignature(methodBinding.getMethodDeclaration()));
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

			res.setDeclarationSignature(DOMCompletionEngineBuilder.getSignature(variableBinding.getDeclaringClass()));
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
			res.setSignature(DOMCompletionEngineBuilder.getSignature(typeBinding));
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
			if (this.cuBuffer != null && place != 0) {
				if (this.cuBuffer.getChar(place) == '\n') {
					place++;
				} else if (this.cuBuffer.getChar(place) == '\r' && this.cuBuffer.getChar(place + 1) == '\n') {
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
				if (CompletionEngine.isFailedMatch(prefix, key.toCharArray(), options)) {
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
			IModuleDescription ownDescription = this.modelUnit.getJavaProject().getModuleDescription();
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
		int relevance = CompletionEngine.computeBaseRelevance();
		relevance += CompletionEngine.computeRelevanceForResolution();
		relevance += this.nestedEngine.computeRelevanceForInterestingProposal();
		relevance += this.nestedEngine.computeRelevanceForCaseMatching(prefix, completion);
		relevance += this.nestedEngine.computeRelevanceForQualification(true);
		if (requiredModules.contains(moduleName)) {
			relevance += CompletionEngine.computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
		}
		InternalCompletionProposal proposal = createProposal(CompletionProposal.MODULE_REF);
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
		if (startPos == -1 && endPos == -1) {
			setRange(keywordProposal);
		} else {
			keywordProposal.setReplaceRange(startPos, endPos);
		}
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

	private static final char[] LAMBDA = new char[] {'-', '>'};
	private CompletionProposal createLambdaExpressionProposal(IMethodBinding method) {
		InternalCompletionProposal res = createProposal(CompletionProposal.LAMBDA_EXPRESSION);

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

		res.setDeclarationSignature(DOMCompletionEngineBuilder.getSignature(method.getDeclaringClass()));
		res.setSignature(DOMCompletionEngineBuilder.getSignature(method));

		IMethodBinding original = method.getMethodDeclaration();
		if (original != method) {
			res.setOriginalSignature(DOMCompletionEngineBuilder.getSignature(original));
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

	private int computeRelevanceForQualification(boolean prefixRequired) {
		boolean insideQualifiedReference = !this.prefix.equals(this.qualifiedPrefix);
		if (!prefixRequired && !insideQualifiedReference) {
			return RelevanceConstants.R_UNQUALIFIED;
		}
		if (prefixRequired && insideQualifiedReference) {
			return RelevanceConstants.R_QUALIFIED;
		}
		return 0;
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
