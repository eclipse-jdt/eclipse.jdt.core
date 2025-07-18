/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DOMToModelPopulator;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.TypeNameMatchRequestorWrapper;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A util to select relevant IJavaElement from a DOM (as opposed to {@link SelectionEngine}
 * which processes it using lower-level ECJ parser)
 */
public class DOMCodeSelector {

	private final CompilationUnit unit;
	private final WorkingCopyOwner owner;

	public DOMCodeSelector(CompilationUnit unit, WorkingCopyOwner owner) {
		this.unit = unit;
		this.owner = owner;
	}

	public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
		if (offset < 0) {
			throw new JavaModelException(new IndexOutOfBoundsException(offset), IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		}
		if (offset + length > this.unit.getSource().length()) {
			throw new JavaModelException(new IndexOutOfBoundsException(offset + length), IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		}
		org.eclipse.jdt.core.dom.CompilationUnit currentAST = this.unit.getOrBuildAST(this.owner);
		if (currentAST == null) {
			return new IJavaElement[0];
		}
		String rawText = this.unit.getSource().substring(offset, offset + length);
		int initialOffset = offset, initialLength = length;
		boolean insideComment = ((List<Comment>)currentAST.getCommentList()).stream()
			.anyMatch(comment -> comment.getStartPosition() <= initialOffset && comment.getStartPosition() + comment.getLength() >= initialOffset + initialLength);
		if (!insideComment) { // trim whitespaces and surrounding comments
			boolean changed = false;
			do {
				changed = false;
				if (length > 0 && Character.isWhitespace(this.unit.getSource().charAt(offset))) {
					offset++;
					length--;
					changed = true;
				}
				if (length > 0 && Character.isWhitespace(this.unit.getSource().charAt(offset + length - 1))) {
					length--;
					changed = true;
				}
				List<Comment> comments = currentAST.getCommentList();
				// leading comment
				int offset1 = offset, length1 = length;
				OptionalInt leadingCommentEnd = comments.stream().filter(comment -> {
					int commentEndOffset = comment.getStartPosition() + comment.getLength() -1;
					return comment.getStartPosition() <= offset1 && commentEndOffset > offset1 && commentEndOffset < offset1 + length1 - 1;
				}).mapToInt(comment -> comment.getStartPosition() + comment.getLength() - 1)
				.findAny();
				if (length > 0 && leadingCommentEnd.isPresent()) {
					changed = true;
					int newStart = leadingCommentEnd.getAsInt();
					int removedLeading = newStart + 1 - offset;
					offset = newStart + 1;
					length -= removedLeading;
				}
				// Trailing comment
				int offset2 = offset, length2 = length;
				OptionalInt trailingCommentStart = comments.stream().filter(comment -> {
					return comment.getStartPosition() >= offset2
						&& comment.getStartPosition() < offset2 + length2
						&& comment.getStartPosition() + comment.getLength() > offset2 + length2;
				}).mapToInt(Comment::getStartPosition)
				.findAny();
				if (length > 0 && trailingCommentStart.isPresent()) {
					changed = true;
					int newEnd = trailingCommentStart.getAsInt();
					int removedTrailing = offset + length - 1 - newEnd;
					length -= removedTrailing;
				}
			} while (changed);
		}
		String trimmedText = rawText.trim();
		NodeFinder finder = new NodeFinder(currentAST, offset, length);
		final ASTNode node = finder.getCoveredNode() != null && finder.getCoveredNode().getStartPosition() > offset && finder.getCoveringNode().getStartPosition() + finder.getCoveringNode().getLength() > offset + length ?
			finder.getCoveredNode() :
			finder.getCoveringNode();
		if (node instanceof TagElement && TagElement.TAG_INHERITDOC.equals(((TagElement) node).getTagName())) {
            TagElement tagElement = (TagElement) node;
            ASTNode javadocNode = node;
			while (javadocNode != null && !(javadocNode instanceof Javadoc)) {
				javadocNode = javadocNode.getParent();
			}
			if (javadocNode instanceof Javadoc) {
                Javadoc javadoc = (Javadoc) javadocNode;
                ASTNode parent = javadoc.getParent();
				IBinding binding = resolveBinding(parent);
				if (binding instanceof IMethodBinding) {
                    IMethodBinding methodBinding = (IMethodBinding) binding;
                    var typeBinding = methodBinding.getDeclaringClass();
					if (typeBinding != null) {
						List<ITypeBinding> types = new ArrayList<>(Arrays.asList(typeBinding.getInterfaces()));
						if (typeBinding.getSuperclass() != null) {
							types.add(typeBinding.getSuperclass());
						}
						while (!types.isEmpty()) {
							ITypeBinding type = types.remove(0);
							for (IMethodBinding m : Arrays.stream(type.getDeclaredMethods()).filter(methodBinding::overrides).toList()) {
								if (m.getJavaElement() instanceof IMethod && ((IMethod) m.getJavaElement()).getJavadocRange() != null) {
                                    IMethod methodElement = (IMethod) m.getJavaElement();
                                    return new IJavaElement[] { methodElement };
								} else {
									types.addAll(Arrays.asList(type.getInterfaces()));
									if (type.getSuperclass() != null) {
										types.add(type.getSuperclass());
									}
								}
							}
						}
					}
					IJavaElement element = methodBinding.getJavaElement();
					if (element != null) {
						return new IJavaElement[] { element };
					}
				}
			}
		}
		org.eclipse.jdt.core.dom.ImportDeclaration importDecl = findImportDeclaration(node);
		if (node instanceof ExpressionMethodReference &&
            ((ExpressionMethodReference) node).getExpression().getStartPosition() + ((ExpressionMethodReference) node).getExpression().getLength() <= offset && offset + length <= ((ExpressionMethodReference) node).getName().getStartPosition()) {
            ExpressionMethodReference emr = (ExpressionMethodReference) node;
            if (!(rawText.isEmpty() || rawText.equals(":") || rawText.equals("::"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return new IJavaElement[0];
			}
			if (emr.getParent() instanceof MethodInvocation) {
                MethodInvocation methodInvocation = (MethodInvocation) emr.getParent();
                int index = methodInvocation.arguments().indexOf(emr);
				return new IJavaElement[] {methodInvocation.resolveMethodBinding().getParameterTypes()[index].getDeclaredMethods()[0].getJavaElement()};
			}
			if (emr.getParent() instanceof VariableDeclaration) {
                VariableDeclaration variableDeclaration = (VariableDeclaration) emr.getParent();
                ITypeBinding requestedType = variableDeclaration.resolveBinding().getType();
				if (requestedType.getDeclaredMethods().length == 1
                    && requestedType.getDeclaredMethods()[0].getJavaElement() instanceof IMethod) {
                    IMethod overridenMethod = (IMethod) requestedType.getDeclaredMethods()[0].getJavaElement();
                    return new IJavaElement[] { overridenMethod };
				}
			}
		}
		if (node instanceof LambdaExpression) {
            LambdaExpression lambda = (LambdaExpression) node;
            if (!(rawText.isEmpty() || rawText.equals("-") || rawText.equals(">") || rawText.equals("->"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return new IJavaElement[0]; // as requested by some tests
			}
			if (lambda.resolveMethodBinding() != null
				&& lambda.resolveMethodBinding().getMethodDeclaration() != null
				&& lambda.resolveMethodBinding().getMethodDeclaration().getJavaElement() != null) {
				return new IJavaElement[] { lambda.resolveMethodBinding().getMethodDeclaration().getJavaElement() };
			}
		}
		if (importDecl != null && importDecl.isStatic()) {
			IBinding importBinding = importDecl.resolveBinding();
			if (importBinding instanceof IMethodBinding) {
                IMethodBinding methodBinding = (IMethodBinding) importBinding;
                ArrayDeque<IJavaElement> overloadedMethods = Stream.of(methodBinding.getDeclaringClass().getDeclaredMethods()) //
						.filter(otherMethodBinding -> methodBinding.getName().equals(otherMethodBinding.getName())) //
						.map(IMethodBinding::getJavaElement) //
						.filter(IJavaElement::exists)
						.collect(Collectors.toCollection(ArrayDeque::new));
				IJavaElement[] reorderedOverloadedMethods = new IJavaElement[overloadedMethods.size()];
				Iterator<IJavaElement> reverseIterator = overloadedMethods.descendingIterator();
				for (int i = 0; i < reorderedOverloadedMethods.length; i++) {
					reorderedOverloadedMethods[i] = reverseIterator.next();
				}
				return reorderedOverloadedMethods;
			}
			return new IJavaElement[] { importBinding.getJavaElement() };
		} else if (findTypeDeclaration(node) == null) {
			IBinding binding = resolveBinding(node);
			if (binding != null && !binding.isRecovered()) {
				if (node instanceof SuperMethodInvocation && // on `super`
                    binding instanceof IMethodBinding &&
                    ((IMethodBinding) binding).getDeclaringClass() instanceof ITypeBinding &&
                    ((ITypeBinding) ((IMethodBinding) binding).getDeclaringClass()).getJavaElement() instanceof IType) {
                    IType type = (IType) ((ITypeBinding) ((IMethodBinding) binding).getDeclaringClass()).getJavaElement();
                    ITypeBinding typeBinding = (ITypeBinding) ((IMethodBinding) binding).getDeclaringClass();
                    IMethodBinding methodBinding = (IMethodBinding) binding;
                    return new IJavaElement[] { type };
				}
				if (binding instanceof IPackageBinding
                    && trimmedText.length() > 0
                    && !trimmedText.equals(((IPackageBinding) binding).getName())
                    && ((IPackageBinding) binding).getName().startsWith(trimmedText)) {
                    IPackageBinding packageBinding = (IPackageBinding) binding;
                    // resolved a too wide node for package name, restrict to selected name only
					IJavaElement fragment = this.unit.getJavaProject().findPackageFragment(trimmedText);
					if (fragment != null) {
						return new IJavaElement[] { fragment };
					}
				}
				// workaround https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2177
				if (binding instanceof IVariableBinding &&
                    ((IVariableBinding) binding).getDeclaringMethod() instanceof IMethodBinding &&
                    ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).isCompactConstructor() &&
                    Arrays.stream(((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getParameterNames()).anyMatch(((IVariableBinding) binding).getName()::equals) &&
                    ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getDeclaringClass() instanceof ITypeBinding &&
                    ((ITypeBinding) ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getDeclaringClass()).isRecord() &&
                    ((ITypeBinding) ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getDeclaringClass()).getJavaElement() instanceof IType &&
                    ((IType) ((ITypeBinding) ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getDeclaringClass()).getJavaElement()).getField(((IVariableBinding) binding).getName()) instanceof SourceField) {
                    IType recordType = (IType) ((ITypeBinding) ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getDeclaringClass()).getJavaElement();
                    SourceField field = (SourceField) recordType.getField(((IVariableBinding) binding).getName());
                    ITypeBinding recordBinding = (ITypeBinding) ((IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod()).getDeclaringClass();
                    IMethodBinding declaringMethod = (IMethodBinding) ((IVariableBinding) binding).getDeclaringMethod();
                    IVariableBinding variableBinding = (IVariableBinding) binding;
                    // the parent must be the field and not the method
					return new IJavaElement[] { new LocalVariable(field,
						variableBinding.getName(),
						0, // must be 0 for subsequent call to LocalVariableLocator.matchLocalVariable() to work
						field.getSourceRange().getOffset() + field.getSourceRange().getLength() - 1,
						field.getNameRange().getOffset(),
						field.getNameRange().getOffset() + field.getNameRange().getLength() - 1,
						field.getTypeSignature(),
						null,
						field.getFlags(),
						true) };
				}
				if (binding instanceof ITypeBinding &&
                    ((ITypeBinding) binding).isIntersectionType()) {
                    ITypeBinding typeBinding = (ITypeBinding) binding;
                    return Arrays.stream(typeBinding.getTypeBounds())
							.map(ITypeBinding::getJavaElement)
							.filter(Objects::nonNull)
							.toArray(IJavaElement[]::new);
				}
				IJavaElement element = binding.getJavaElement();
				if (element != null && (element instanceof IPackageFragment || element.exists())) {
					return new IJavaElement[] { element };
				}
				if (binding instanceof ITypeBinding) {
                    ITypeBinding typeBinding = (ITypeBinding) binding;
                    if (this.unit.getJavaProject() != null) {
						IType type = this.unit.getJavaProject().findType(typeBinding.getQualifiedName());
						if (type != null) {
							return new IJavaElement[] { type };
						}
					}
					// fallback to calling index, inspired/copied from SelectionEngine
					IJavaElement[] indexMatch = findTypeInIndex(typeBinding.getPackage() != null ? typeBinding.getPackage().getName() : null, typeBinding.getName());
					if (indexMatch.length > 0) {
						return indexMatch;
					}
				}
				if (binding instanceof IVariableBinding && ((IVariableBinding) binding).getDeclaringMethod() != null && ((IVariableBinding) binding).getDeclaringMethod().isCompactConstructor()) {
                    IVariableBinding variableBinding = (IVariableBinding) binding;
                    // workaround for JavaSearchBugs15Tests.testBug558812_012
					if (variableBinding.getDeclaringMethod().getJavaElement() instanceof IMethod) {
                        IMethod method = (IMethod) variableBinding.getDeclaringMethod().getJavaElement();
                        Optional<ILocalVariable> parameter = Arrays.stream(method.getParameters()).filter(param -> Objects.equals(param.getElementName(), variableBinding.getName())).findAny();
						if (parameter.isPresent()) {
							return new IJavaElement[] { parameter.get() };
						}
					}
				}
				if (binding instanceof IMethodBinding &&
                    ((IMethodBinding) binding).isSyntheticRecordMethod() &&
                    ((IMethodBinding) binding).getDeclaringClass().getJavaElement() instanceof IType &&
                    ((IType) ((IMethodBinding) binding).getDeclaringClass().getJavaElement()).getField(((IMethodBinding) binding).getName()) instanceof IField) {
                    IField field = (IField) ((IType) ((IMethodBinding) binding).getDeclaringClass().getJavaElement()).getField(((IMethodBinding) binding).getName());
                    IType recordType = (IType) ((IMethodBinding) binding).getDeclaringClass().getJavaElement();
                    IMethodBinding methodBinding = (IMethodBinding) binding;
                    return new IJavaElement[] { field };
				}
				ASTNode bindingNode = currentAST.findDeclaringNode(binding);
				if (bindingNode != null) {
					IJavaElement parent = this.unit.getElementAt(bindingNode.getStartPosition());
					if (parent != null && bindingNode instanceof SingleVariableDeclaration) {
                        SingleVariableDeclaration variableDecl = (SingleVariableDeclaration) bindingNode;
                        return new IJavaElement[] { DOMToModelPopulator.toLocalVariable(variableDecl, (JavaElement)parent) };
					}
				}
			}
		}
		// fallback: crawl the children of this unit
		IJavaElement currentElement = this.unit;
		boolean newChildFound;
		int finalOffset = offset;
		int finalLength = length;
		do {
			newChildFound = false;
			if (currentElement instanceof IParent) {
                IParent parentElement = (IParent) currentElement;
                Optional<IJavaElement> candidate = Stream.of(parentElement.getChildren())
					.filter(ISourceReference.class::isInstance)
					.map(ISourceReference.class::cast)
					.filter(sourceRef -> {
						try {
							ISourceRange elementRange = sourceRef.getSourceRange();
							return elementRange != null
								&& elementRange.getOffset() >= 0
								&& elementRange.getOffset() <= finalOffset
								&& elementRange.getOffset() + elementRange.getLength() >= finalOffset + finalLength;
						} catch (JavaModelException e) {
							return false;
						}
					}).map(IJavaElement.class::cast)
					.findAny();
				if (candidate.isPresent()) {
					newChildFound = true;
					currentElement = candidate.get();
				}
			}
		} while (newChildFound);
		if (currentElement instanceof JavaElement &&
            ((JavaElement) currentElement).getElementInfo() instanceof AnnotatableInfo &&
            ((AnnotatableInfo) ((JavaElement) currentElement).getElementInfo()).getNameSourceStart() >= 0 &&
            ((AnnotatableInfo) ((JavaElement) currentElement).getElementInfo()).getNameSourceStart() <= offset &&
            ((AnnotatableInfo) ((JavaElement) currentElement).getElementInfo()).getNameSourceEnd() + 1 /* end exclusive vs offset inclusive */ >= offset) {
            AnnotatableInfo annotable = (AnnotatableInfo) ((JavaElement) currentElement).getElementInfo();
            JavaElement impl = (JavaElement) currentElement;
            return new IJavaElement[] { currentElement };
		}
		if (insideComment) {
			String toSearch = trimmedText.isBlank() ? findWord(offset) : trimmedText;
			String resolved = ((List<org.eclipse.jdt.core.dom.ImportDeclaration>)currentAST.imports()).stream()
				.map(org.eclipse.jdt.core.dom.ImportDeclaration::getName)
				.map(Name::toString)
				.filter(importedPackage -> importedPackage.endsWith(toSearch))
				.findAny()
				.orElse(toSearch);
			if (this.unit.getJavaProject().findType(resolved) instanceof IType) {
                IType type = (IType) this.unit.getJavaProject().findType(resolved);
                return new IJavaElement[] { type };
			}
		}
		// failback to lookup search
		ASTNode currentNode = node;
		while (currentNode != null && !(currentNode instanceof Type)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode instanceof Type) {
            Type parentType = (Type) currentNode;
            if (this.unit.getJavaProject() != null) {
				StringBuilder buffer = new StringBuilder();
				Util.getFullyQualifiedName(parentType, buffer);
				IType type = this.unit.getJavaProject().findType(buffer.toString());
				if (type != null) {
					return new IJavaElement[] { type };
				}
			}
			String packageName = parentType instanceof QualifiedType ? ((QualifiedType) parentType).getQualifier().toString() :
                    parentType instanceof SimpleType ?
                            ((SimpleType) parentType).getName() instanceof QualifiedName ? ((QualifiedName) ((SimpleType) parentType).getName()).getQualifier().toString() :
					null :
				null;
			String simpleName = parentType instanceof QualifiedType ? ((QualifiedType) parentType).getName().toString() :
                    parentType instanceof SimpleType ?
                            ((SimpleType) parentType).getName() instanceof SimpleName ? ((SimpleName) ((SimpleType) parentType).getName()).getIdentifier() :
                                    ((SimpleType) parentType).getName() instanceof QualifiedName ? ((QualifiedName) ((SimpleType) parentType).getName()).getName().toString() :
					null :
				null;
			IJavaElement[] indexResult = findTypeInIndex(packageName, simpleName);
			if (indexResult.length > 0) {
				return indexResult;
			}
		}
		// no good idea left
		return new IJavaElement[0];
	}

	static IBinding resolveBinding(ASTNode node) {
		if (node instanceof MethodDeclaration) {
            MethodDeclaration decl = (MethodDeclaration) node;
            return decl.resolveBinding();
		}
		if (node instanceof MethodInvocation) {
            MethodInvocation invocation = (MethodInvocation) node;
            return invocation.resolveMethodBinding();
		}
		if (node instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) node;
            return decl.resolveBinding();
		}
		if (node instanceof FieldAccess) {
            FieldAccess access = (FieldAccess) node;
            return access.resolveFieldBinding();
		}
		if (node instanceof Type) {
            Type type = (Type) node;
            return type.resolveBinding();
		}
		if (node instanceof Name) {
            Name aName = (Name) node;
            ClassInstanceCreation newInstance = findConstructor(aName);
			if (newInstance != null) {
				var constructorBinding = newInstance.resolveConstructorBinding();
				if (constructorBinding != null) {
					var constructorElement = constructorBinding.getJavaElement();
					if (constructorElement != null) {
						boolean hasSource = true;
						try {
							hasSource = ((ISourceReference)constructorElement.getParent()).getSource() != null;
						} catch (Exception e) {
							hasSource = false;
						}
						if ((constructorBinding.getParameterTypes().length > 0 /*non-default*/ ||
								constructorElement instanceof SourceMethod || !hasSource)) {
							return constructorBinding;
						}
					} else if (newInstance.resolveTypeBinding().isAnonymous()) {
						// it's not in the anonymous class body, check for constructor decl in parent types

						ITypeBinding superclassBinding = newInstance.getType().resolveBinding();

						while (superclassBinding != null) {
							Optional<IMethodBinding> potentialConstructor = Stream.of(superclassBinding.getDeclaredMethods()) //
									.filter(methodBinding -> methodBinding.isConstructor() && matchSignatures(constructorBinding, methodBinding))
									.findFirst();
							if (potentialConstructor.isPresent()) {
								IMethodBinding theConstructor = potentialConstructor.get();
								if (theConstructor.isDefaultConstructor()) {
									return theConstructor.getDeclaringClass();
								}
								return theConstructor;
							}
							superclassBinding = superclassBinding.getSuperclass();
						}
						return null;
					}
				}
			}
			if (node.getParent() instanceof ExpressionMethodReference && ((ExpressionMethodReference) node.getParent()).getName() == node) {
                ExpressionMethodReference exprMethodReference = (ExpressionMethodReference) node.getParent();
                return resolveBinding(exprMethodReference);
			}
			if (node.getParent() instanceof TypeMethodReference && ((TypeMethodReference) node.getParent()).getName() == node) {
                TypeMethodReference typeMethodReference = (TypeMethodReference) node.getParent();
                return resolveBinding(typeMethodReference);
			}
			IBinding res = aName.resolveBinding();
			if (res != null) {
				return res;
			}
			return resolveBinding(aName.getParent());
		}
		if (node instanceof org.eclipse.jdt.core.dom.LambdaExpression) {
            org.eclipse.jdt.core.dom.LambdaExpression lambda = (org.eclipse.jdt.core.dom.LambdaExpression) node;
            return lambda.resolveMethodBinding();
		}
		if (node instanceof ExpressionMethodReference) {
            ExpressionMethodReference methodRef = (ExpressionMethodReference) node;
            IMethodBinding methodBinding = methodRef.resolveMethodBinding();
			try {
				if (methodBinding == null) {
					return null;
				}
				IMethod methodModel = ((IMethod)methodBinding.getJavaElement());
				boolean allowExtraParam = true;
				if ((methodModel.getFlags() & Flags.AccStatic) != 0) {
					allowExtraParam = false;
					if (methodRef.getExpression() instanceof ClassInstanceCreation) {
						return null;
					}
				}

				// find the type that the method is bound to
				ITypeBinding type = null;
				ASTNode cursor = methodRef;
				while (type == null && cursor != null) {
					if (cursor.getParent() instanceof VariableDeclarationFragment) {
                        VariableDeclarationFragment declFragment = (VariableDeclarationFragment) cursor.getParent();
                        type = declFragment.resolveBinding().getType();
					}
					else if (cursor.getParent() instanceof MethodInvocation) {
                        MethodInvocation methodInvocation = (MethodInvocation) cursor.getParent();
                        IMethodBinding methodInvocationBinding = methodInvocation.resolveMethodBinding();
						int index = methodInvocation.arguments().indexOf(cursor);
						type = methodInvocationBinding.getParameterTypes()[index];
					} else {
						cursor = cursor.getParent();
					}
				}

				IMethodBinding boundMethod = type.getDeclaredMethods()[0];

				if (boundMethod.getParameterTypes().length != methodBinding.getParameterTypes().length && (!allowExtraParam || boundMethod.getParameterTypes().length != methodBinding.getParameterTypes().length + 1)) {
					return null;
				}
			} catch (JavaModelException e) {
				return null;
			}
			return methodBinding;
		}
		if (node instanceof MethodReference) {
            MethodReference methodRef = (MethodReference) node;
            return methodRef.resolveMethodBinding();
		}
		if (node instanceof org.eclipse.jdt.core.dom.TypeParameter) {
            org.eclipse.jdt.core.dom.TypeParameter typeParameter = (org.eclipse.jdt.core.dom.TypeParameter) node;
            return typeParameter.resolveBinding();
		}
		if (node instanceof SuperConstructorInvocation) {
            SuperConstructorInvocation superConstructor = (SuperConstructorInvocation) node;
            return superConstructor.resolveConstructorBinding();
		}
		if (node instanceof ConstructorInvocation) {
            ConstructorInvocation constructor = (ConstructorInvocation) node;
            return constructor.resolveConstructorBinding();
		}
		if (node instanceof org.eclipse.jdt.core.dom.Annotation) {
            org.eclipse.jdt.core.dom.Annotation annotation = (org.eclipse.jdt.core.dom.Annotation) node;
            return annotation.resolveTypeBinding();
		}
		if (node instanceof SuperMethodInvocation) {
            SuperMethodInvocation superMethod = (SuperMethodInvocation) node;
            return superMethod.resolveMethodBinding();
		}
		return null;
	}

	private static ClassInstanceCreation findConstructor(ASTNode node) {
		while (node != null && !(node instanceof ClassInstanceCreation)) {
			ASTNode parent = node.getParent();
			if ((parent instanceof SimpleType && ((SimpleType) parent).getName() == node) ||
                (parent instanceof ClassInstanceCreation && ((ClassInstanceCreation) parent).getType() == node) ||
                (parent instanceof ParameterizedType && ((ParameterizedType) parent).getType() == node)) {
				node = parent;
			} else {
				node = null;
			}
		}
		return (ClassInstanceCreation)node;
	}

	private static AbstractTypeDeclaration findTypeDeclaration(ASTNode node) {
		ASTNode cursor = node;
		while (cursor != null && (cursor instanceof Type || cursor instanceof Name)) {
			cursor = cursor.getParent();
		}
		if (cursor instanceof AbstractTypeDeclaration && ((AbstractTypeDeclaration) cursor).getName() == node) {
            AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) cursor;
            return typeDecl;
		}
		return null;
	}

	private static org.eclipse.jdt.core.dom.ImportDeclaration findImportDeclaration(ASTNode node) {
		while (node != null && !(node instanceof org.eclipse.jdt.core.dom.ImportDeclaration)) {
			node = node.getParent();
		}
		return (org.eclipse.jdt.core.dom.ImportDeclaration)node;
	}

	private static boolean matchSignatures(IMethodBinding invocation, IMethodBinding declaration) {
		if (declaration.getTypeParameters().length == 0) {
			return invocation.isSubsignature(declaration);
		}
		if (invocation.getParameterTypes().length != declaration.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < invocation.getParameterTypes().length; i++) {
			if (declaration.getParameterTypes()[i].isTypeVariable()) {
				if (declaration.getParameterTypes()[i].getTypeBounds().length > 0) {
					ITypeBinding[] bounds = declaration.getParameterTypes()[i].getTypeBounds();
					for (int j = 0; j < bounds.length; j++) {
						if (!invocation.getParameterTypes()[i].isSubTypeCompatible(bounds[j])) {
							return false;
						}
					}
				}
			} else if (!invocation.getParameterTypes()[i].isSubTypeCompatible(declaration.getParameterTypes()[i])) {
				return false;
			}

		}
		return true;
	}

	private IJavaElement[] findTypeInIndex(String packageName, String simpleName) throws JavaModelException {
		List<IType> indexMatch = new ArrayList<>();
		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch match) {
				indexMatch.add(match.getType());
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] { this.unit.getJavaProject() });
		new SearchEngine(this.owner).searchAllTypeNames(
			packageName != null ? packageName.toCharArray() : null,
			SearchPattern.R_EXACT_MATCH,
			simpleName.toCharArray(),
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			new NullProgressMonitor());
		if (!indexMatch.isEmpty()) {
			return indexMatch.toArray(IJavaElement[]::new);
		}
		scope = BasicSearchEngine.createWorkspaceScope();
		new BasicSearchEngine(this.owner).searchAllTypeNames(
			packageName != null ? packageName.toCharArray() : null,
			SearchPattern.R_EXACT_MATCH,
			simpleName.toCharArray(),
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			scope,
			new TypeNameMatchRequestorWrapper(requestor, scope),
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			new NullProgressMonitor());
		if (!indexMatch.isEmpty()) {
			return indexMatch.toArray(IJavaElement[]::new);
		}
		return new IJavaElement[0];
	}

	private String findWord(int offset) throws JavaModelException {
		int start = offset;
		String source = this.unit.getSource();
		while (start >= 0 && Character.isJavaIdentifierPart(source.charAt(start))) start--;
		int end = offset + 1;
		while (end < source.length() && Character.isJavaIdentifierPart(source.charAt(end))) end++;
		return source.substring(start, end);
	}
}
