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
package org.eclipse.jdt.internal.core;

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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.TypeNameMatchRequestorWrapper;
import org.eclipse.jdt.internal.core.util.Util;

class DOMCodeSelector {

	private final CompilationUnit unit;
	private final WorkingCopyOwner owner;

	DOMCodeSelector(CompilationUnit unit, WorkingCopyOwner owner) {
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
		String text = this.unit.getSource().substring(offset, offset + length).trim();
		NodeFinder finder = new NodeFinder(currentAST, offset, length);
		final ASTNode node = finder.getCoveredNode() != null && finder.getCoveredNode().getStartPosition() > offset && finder.getCoveringNode().getStartPosition() + finder.getCoveringNode().getLength() > offset + length ?
			finder.getCoveredNode() :
			finder.getCoveringNode();
		org.eclipse.jdt.core.dom.ImportDeclaration importDecl = findImportDeclaration(node);
		if (node instanceof ExpressionMethodReference emr && emr.getExpression().getStartPosition() + emr.getExpression().getLength() <= offset && offset + length <= emr.getName().getStartPosition()) {
			if (emr.getParent() instanceof MethodInvocation methodInvocation) {
				int index = methodInvocation.arguments().indexOf(emr);
				return new IJavaElement[] {methodInvocation.resolveMethodBinding().getParameterTypes()[index].getDeclaredMethods()[0].getJavaElement()};
			}
		}
		if (importDecl != null && importDecl.isStatic()) {
			IBinding importBinding = importDecl.resolveBinding();
			if (importBinding instanceof IMethodBinding methodBinding) {
				ArrayDeque<IJavaElement> overloadedMethods = Stream.of(methodBinding.getDeclaringClass().getDeclaredMethods()) //
						.filter(otherMethodBinding -> methodBinding.getName().equals(otherMethodBinding.getName())) //
						.map(IMethodBinding::getJavaElement) //
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
			if (binding != null) {
				if (binding instanceof IPackageBinding packageBinding
						&& text.length() > 0
						&& !text.equals(packageBinding.getName())
						&& packageBinding.getName().startsWith(text)) {
					// resolved a too wide node for package name, restrict to selected name only
					IJavaElement fragment = this.unit.getJavaProject().findPackageFragment(text);
					if (fragment != null) {
						return new IJavaElement[] { fragment };
					}
				}
				IJavaElement element = binding.getJavaElement();
				if (element != null && (element instanceof IPackageFragment || element.exists())) {
					return new IJavaElement[] { element };
				}
				if (binding instanceof ITypeBinding typeBinding) {
					if (this.unit.getJavaProject() != null) {
						IType type = this.unit.getJavaProject().findType(typeBinding.getQualifiedName());
						if (type != null) {
							return new IJavaElement[] { type };
						}
					}
					// fallback to calling index, inspired/copied from SelectionEngine
					IJavaElement[] indexMatch = findTypeInIndex(typeBinding.getPackage().getName(), typeBinding.getName());
					if (indexMatch.length > 0) {
						return indexMatch;
					}
				} else if (binding instanceof IVariableBinding variableBinding && variableBinding.getDeclaringMethod() != null && variableBinding.getDeclaringMethod().isCompactConstructor()) {
					// workaround for JavaSearchBugs15Tests.testBug558812_012
					if (variableBinding.getDeclaringMethod().getJavaElement() instanceof IMethod method) {
						Optional<ILocalVariable> parameter = Arrays.stream(method.getParameters()).filter(param -> Objects.equals(param.getElementName(), variableBinding.getName())).findAny();
						if (parameter.isPresent()) {
							return new IJavaElement[] { parameter.get() };
						}
					}
				}
				ASTNode bindingNode = currentAST.findDeclaringNode(binding);
				if (bindingNode != null) {
					IJavaElement parent = this.unit.getElementAt(bindingNode.getStartPosition());
					if (parent != null && bindingNode instanceof SingleVariableDeclaration variableDecl) {
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
			if (currentElement instanceof IParent parentElement) {
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
		if (currentElement instanceof JavaElement impl &&
				impl.getElementInfo() instanceof AnnotatableInfo annotable &&
				annotable.getNameSourceStart() >= 0 &&
				annotable.getNameSourceStart() <= offset &&
				annotable.getNameSourceEnd() >= offset) {
			return new IJavaElement[] { currentElement };
		}
		// failback to lookup search
		ASTNode currentNode = node;
		while (currentNode != null && !(currentNode instanceof Type)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode instanceof Type parentType) {
			if (this.unit.getJavaProject() != null) {
				StringBuilder buffer = new StringBuilder();
				Util.getFullyQualifiedName(parentType, buffer);
				IType type = this.unit.getJavaProject().findType(buffer.toString());
				if (type != null) {
					return new IJavaElement[] { type };
				}
			}
			String packageName = parentType instanceof QualifiedType qType ? qType.getQualifier().toString() :
				parentType instanceof SimpleType sType ?
					sType.getName() instanceof QualifiedName qName ? qName.getQualifier().toString() :
					null :
				null;
			String simpleName = parentType instanceof QualifiedType qType ? qType.getName().toString() :
				parentType instanceof SimpleType sType ?
					sType.getName() instanceof SimpleName sName ? sName.getIdentifier() :
					sType.getName() instanceof QualifiedName qName ? qName.getName().toString() :
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
		if (node instanceof MethodDeclaration decl) {
			return decl.resolveBinding();
		}
		if (node instanceof MethodInvocation invocation) {
			return invocation.resolveMethodBinding();
		}
		if (node instanceof VariableDeclaration decl) {
			return decl.resolveBinding();
		}
		if (node instanceof FieldAccess access) {
			return access.resolveFieldBinding();
		}
		if (node instanceof Type type) {
			return type.resolveBinding();
		}
		if (node instanceof Name aName) {
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
			if (node.getParent() instanceof ExpressionMethodReference exprMethodReference && exprMethodReference.getName() == node) {
				return resolveBinding(exprMethodReference);
			}
			IBinding res = aName.resolveBinding();
			if (res != null) {
				return res;
			}
			return resolveBinding(aName.getParent());
		}
		if (node instanceof org.eclipse.jdt.core.dom.LambdaExpression lambda) {
			return lambda.resolveMethodBinding();
		}
		if (node instanceof ExpressionMethodReference methodRef) {
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
					if (cursor.getParent() instanceof VariableDeclarationFragment declFragment) {
						type = declFragment.resolveBinding().getType();
					}
					else if (cursor.getParent() instanceof MethodInvocation methodInvocation) {
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
		if (node instanceof MethodReference methodRef) {
			return methodRef.resolveMethodBinding();
		}
		if (node instanceof org.eclipse.jdt.core.dom.TypeParameter typeParameter) {
			return typeParameter.resolveBinding();
		}
		if (node instanceof SuperConstructorInvocation superConstructor) {
			return superConstructor.resolveConstructorBinding();
		}
		if (node instanceof ConstructorInvocation constructor) {
			return constructor.resolveConstructorBinding();
		}
		if (node instanceof org.eclipse.jdt.core.dom.Annotation annotation) {
			return annotation.resolveTypeBinding();
		}
		return null;
	}

	private static ClassInstanceCreation findConstructor(ASTNode node) {
		while (node != null && !(node instanceof ClassInstanceCreation)) {
			ASTNode parent = node.getParent();
			if ((parent instanceof SimpleType type && type.getName() == node) ||
				(parent instanceof ClassInstanceCreation constructor && constructor.getType() == node) ||
				(parent instanceof ParameterizedType parameterized && parameterized.getType() == node)) {
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
		if (cursor instanceof AbstractTypeDeclaration typeDecl && typeDecl.getName() == node) {
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
		IJavaSearchScope scope = BasicSearchEngine.createJavaSearchScope(new IJavaProject[] { this.unit.getJavaProject() });
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

}
