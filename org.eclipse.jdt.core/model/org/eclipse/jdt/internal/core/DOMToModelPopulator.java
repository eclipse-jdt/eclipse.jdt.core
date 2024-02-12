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
package org.eclipse.jdt.internal.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTagElement;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExportsDirective;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.OpensDirective;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ProvidesDirective;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.UsesDirective;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.ModuleReferenceInfo;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.PackageExportInfo;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.ServiceInfo;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Process an AST to populate a tree of IJavaElement->JavaElementInfo.
 * DOM-first approach to what legacy implements through ECJ parser and CompilationUnitStructureRequestor
 */
class DOMToModelPopulator extends ASTVisitor {

	private final Map<IJavaElement, IElementInfo> toPopulate;
	private final Stack<JavaElement> elements = new Stack<>();
	private final Stack<JavaElementInfo> infos = new Stack<>();
	private final Set<String> currentTypeParameters = new HashSet<>();
	private final CompilationUnitElementInfo unitInfo;
	private ImportContainer importContainer;
	private ImportContainerInfo importContainerInfo;
	private final CompilationUnit root;
	private Boolean alternativeDeprecated = null;

	public DOMToModelPopulator(Map<IJavaElement, IElementInfo> newElements, CompilationUnit root, CompilationUnitElementInfo unitInfo) {
		this.toPopulate = newElements;
		this.elements.push(root);
		this.infos.push(unitInfo);
		this.root = root;
		this.unitInfo = unitInfo;
	}

	private static void addAsChild(JavaElementInfo parentInfo, IJavaElement childElement) {
		if (parentInfo instanceof AnnotatableInfo annotable && childElement instanceof IAnnotation annotation) {
			IAnnotation[] newAnnotations = Arrays.copyOf(annotable.annotations, annotable.annotations.length + 1);
			newAnnotations[newAnnotations.length - 1] = annotation;
			annotable.annotations = newAnnotations;
			return;
		}
		if (childElement instanceof TypeParameter typeParam) {
			if (parentInfo instanceof SourceTypeElementInfo type) {
				type.typeParameters = Arrays.copyOf(type.typeParameters, type.typeParameters.length + 1);
				type.typeParameters[type.typeParameters.length - 1] = typeParam;
				return;
			}
			if (parentInfo instanceof SourceMethodElementInfo method) {
				method.typeParameters = Arrays.copyOf(method.typeParameters, method.typeParameters.length + 1);
				method.typeParameters[method.typeParameters.length - 1] = typeParam;
				return;
			}
		}
		if (parentInfo instanceof ImportContainerInfo importContainer && childElement instanceof org.eclipse.jdt.internal.core.ImportDeclaration importDecl) {
			IJavaElement[] newImports = Arrays.copyOf(importContainer.getChildren(), importContainer.getChildren().length + 1);
			newImports[newImports.length - 1] = importDecl;
			importContainer.children = newImports;
			return;
		}
		// if nothing more specialized, add as child
		if (parentInfo instanceof SourceTypeElementInfo type) {
			type.children = Arrays.copyOf(type.children, type.children.length + 1);
			type.children[type.children.length - 1] = childElement;
			return;
		}
		if (parentInfo instanceof OpenableElementInfo openable) {
			openable.addChild(childElement);
			return;
		}
	}

	@Override
	public boolean visit(org.eclipse.jdt.core.dom.CompilationUnit node) {
		this.unitInfo.setSourceLength(node.getLength());
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		org.eclipse.jdt.internal.core.PackageDeclaration newElement = new org.eclipse.jdt.internal.core.PackageDeclaration(this.root, node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		AnnotatableInfo newInfo = new AnnotatableInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(PackageDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (this.importContainer == null) {
			this.importContainer = this.root.getImportContainer();
			this.importContainerInfo = new ImportContainerInfo();
			JavaElementInfo parentInfo = this.infos.peek();
			addAsChild(parentInfo, this.importContainer);
			this.toPopulate.put(this.importContainer, this.importContainerInfo);
		}
		org.eclipse.jdt.internal.core.ImportDeclaration newElement = new org.eclipse.jdt.internal.core.ImportDeclaration(this.importContainer, node.getName().toString(), node.isOnDemand());
		this.elements.push(newElement);
		addAsChild(this.importContainerInfo, newElement);
		ImportDeclarationElementInfo newInfo = new ImportDeclarationElementInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(ImportDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)node.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::add);
		}
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		boolean isDeprecated = isNodeDeprecated(node);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.setFlags(node.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(TypeDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
		if (decl.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)decl.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::remove);
		}
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		boolean isDeprecated = isNodeDeprecated(node);
		newInfo.setFlags(node.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}

	@Override
	public void endVisit(AnnotationTypeDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		boolean isDeprecated = isNodeDeprecated(node);
		newInfo.setFlags(node.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(EnumDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		SourceField newElement = new SourceField(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceFieldElementInfo info = new SourceFieldElementInfo();
		info.setSourceRangeStart(node.getStartPosition());
		info.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		boolean isDeprecated = isNodeDeprecated(node);
		info.setFlags(node.getModifiers() | ClassFileConstants.AccEnum | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		info.setNameSourceStart(node.getName().getStartPosition());
		info.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		// TODO populate info
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(EnumConstantDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(RecordDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		boolean isDeprecated = isNodeDeprecated(node);
		newInfo.setFlags(node.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(RecordDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}


	@Override
	public boolean visit(MethodDeclaration method) {
		if (method.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)method.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::add);
		}
		SourceMethod newElement = new SourceMethod(this.elements.peek(),
			method.getName().getIdentifier(),
			((List<SingleVariableDeclaration>)method.parameters()).stream()
				.map(this::createSignature)
				.toArray(String[]::new));
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceMethodInfo info = new SourceMethodInfo();
		info.setArgumentNames(((List<SingleVariableDeclaration>)method.parameters()).stream().map(param -> param.getName().toString().toCharArray()).toArray(char[][]::new));
		info.arguments = ((List<SingleVariableDeclaration>)method.parameters()).stream()
			.map(this::toLocalVariable)
			.toArray(LocalVariable[]::new);
		if (method.getAST().apiLevel() > 2) {
			if (method.getReturnType2() != null) {
				info.setReturnType(method.getReturnType2().toString().toCharArray());
			} else {
				info.setReturnType("void".toCharArray()); //$NON-NLS-1$
			}
		}
		if (this.infos.peek() instanceof SourceTypeElementInfo parentInfo) {
			parentInfo.addCategories(newElement, getCategories(method));
		}
		info.setSourceRangeStart(method.getStartPosition());
		info.setSourceRangeEnd(method.getStartPosition() + method.getLength() - 1);
		boolean isDeprecated = isNodeDeprecated(method);
		info.setFlags(method.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		info.setNameSourceStart(method.getName().getStartPosition());
		info.setNameSourceEnd(method.getName().getStartPosition() + method.getName().getLength() - 1);
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(MethodDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
		if (decl.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)decl.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::remove);
		}
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration method) {
		SourceMethod newElement = new SourceMethod(this.elements.peek(),
			method.getName().getIdentifier(),
			new String[0]);
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceAnnotationMethodInfo info = new SourceAnnotationMethodInfo();
		info.setReturnType(method.getType().toString().toCharArray());
		info.setSourceRangeStart(method.getStartPosition());
		info.setSourceRangeEnd(method.getStartPosition() + method.getLength() - 1);
		((SourceTypeElementInfo)this.infos.peek()).addCategories(newElement, getCategories(method));
		boolean isDeprecated = isNodeDeprecated(method);
		info.setFlags(method.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
		info.setNameSourceStart(method.getName().getStartPosition());
		info.setNameSourceEnd(method.getName().getStartPosition() + method.getName().getLength() - 1);
		Expression defaultExpr = method.getDefault();
		if (defaultExpr != null) {
			Entry<Object, Integer> value = memberValue(defaultExpr);
			org.eclipse.jdt.internal.core.MemberValuePair mvp = new org.eclipse.jdt.internal.core.MemberValuePair(newElement.getElementName(), value.getKey(), value.getValue());
			info.defaultValue = mvp;
			info.defaultValueStart = defaultExpr.getStartPosition();
			info.defaultValueEnd = defaultExpr.getStartPosition() + defaultExpr.getLength();
		}
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(AnnotationTypeMemberDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(org.eclipse.jdt.core.dom.TypeParameter node) {
		TypeParameter newElement = new TypeParameter(this.elements.peek(), node.getName().getFullyQualifiedName());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		TypeParameterElementInfo info = new TypeParameterElementInfo();
		info.setSourceRangeStart(node.getStartPosition());
		info.setSourceRangeEnd(node.getStartPosition() + node.getLength());
		info.nameStart = node.getName().getStartPosition();
		info.nameEnd = node.getName().getStartPosition() + node.getName().getLength() - 1;
		info.bounds = ((List<Type>)node.typeBounds()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new);
		info.boundsSignatures = ((List<Type>)node.typeBounds()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new);
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(org.eclipse.jdt.core.dom.TypeParameter typeParam) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		Annotation newElement = new Annotation(this.elements.peek(), node.getTypeName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		AnnotationInfo newInfo = new AnnotationInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.nameStart = node.getTypeName().getStartPosition();
		newInfo.nameEnd = node.getTypeName().getStartPosition() + node.getTypeName().getLength() - 1;
		newInfo.members = ((List<org.eclipse.jdt.core.dom.MemberValuePair>)node.values())
			.stream()
			.map(domMemberValuePair -> {
				Entry<Object, Integer> value = memberValue(domMemberValuePair.getValue());
				return new org.eclipse.jdt.internal.core.MemberValuePair(domMemberValuePair.getName().toString(), value.getKey(), value.getValue());
			})
			.toArray(IMemberValuePair[]::new);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(NormalAnnotation decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		Annotation newElement = new Annotation(this.elements.peek(), node.getTypeName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		AnnotationInfo newInfo = new AnnotationInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.members = new IMemberValuePair[0];
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(MarkerAnnotation decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		Annotation newElement = new Annotation(this.elements.peek(), node.getTypeName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		AnnotationInfo newInfo = new AnnotationInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		Entry<Object, Integer> value = memberValue(node.getValue());
		newInfo.members = new IMemberValuePair[] { new org.eclipse.jdt.internal.core.MemberValuePair("value", value.getKey(), value.getValue()) }; //$NON-NLS-1$
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(SingleMemberAnnotation decl) {
		this.elements.pop();
		this.infos.pop();
	}

	public Entry<Object, Integer> memberValue(Expression dom) {
		if (dom == null ||
			dom instanceof NullLiteral nullLiteral ||
			(dom instanceof SimpleName name && (
				"MISSING".equals(name.getIdentifier()) || //$NON-NLS-1$ // better compare with internal SimpleName.MISSING
				Arrays.equals(RecoveryScanner.FAKE_IDENTIFIER, name.getIdentifier().toCharArray())))) {
			return new SimpleEntry<>(null, IMemberValuePair.K_UNKNOWN);
		}
		if (dom instanceof StringLiteral stringValue) {
			return new SimpleEntry<>(stringValue.getLiteralValue(), IMemberValuePair.K_STRING);
		}
		if (dom instanceof BooleanLiteral booleanValue) {
			return new SimpleEntry<>(booleanValue.booleanValue(), IMemberValuePair.K_BOOLEAN);
		}
		if (dom instanceof CharacterLiteral charValue) {
			return new SimpleEntry<>(charValue.charValue(), IMemberValuePair.K_CHAR);
		}
		if (dom instanceof TypeLiteral typeLiteral) {
			return new SimpleEntry<>(typeLiteral.getType(), IMemberValuePair.K_CLASS);
		}
		if (dom instanceof SimpleName simpleName) {
			return new SimpleEntry<>(simpleName.toString(), IMemberValuePair.K_SIMPLE_NAME);
		}
		if (dom instanceof QualifiedName qualifiedName) {
			return new SimpleEntry<>(qualifiedName.toString(), IMemberValuePair.K_QUALIFIED_NAME);
		}
		if (dom instanceof org.eclipse.jdt.core.dom.Annotation annotation) {
			return new SimpleEntry<>(toModelAnnotation(annotation), IMemberValuePair.K_ANNOTATION);
		}
		if (dom instanceof ArrayInitializer arrayInitializer) {
			var values = ((List<Expression>)arrayInitializer.expressions()).stream().map(this::memberValue).toList();
			var types = values.stream().map(Entry::getValue).distinct().toList();
			return new SimpleEntry<>(values.stream().map(Entry::getKey).toArray(), types.size() == 1 ? types.get(0) : IMemberValuePair.K_UNKNOWN);
		}
		if (dom instanceof NumberLiteral number) {
			String token = number.getToken();
			int type = toAnnotationValuePairType(token);
			Object value = token;
			if ((type == IMemberValuePair.K_LONG && token.endsWith("L")) || //$NON-NLS-1$
				(type == IMemberValuePair.K_FLOAT && token.endsWith("f"))) { //$NON-NLS-1$
				value = token.substring(0, token.length() - 1);
			}
			if (value instanceof String valueString) {
				// I tried using `yield`, but this caused ECJ to throw an AIOOB, preventing compilation
				switch (type) {
					case IMemberValuePair.K_INT: {
						try {
							value =  Integer.parseInt(valueString);
						} catch (NumberFormatException e) {
							type = IMemberValuePair.K_LONG;
							value = Long.parseLong(valueString);
						}
						break;
					}
					case IMemberValuePair.K_LONG: value = Long.parseLong(valueString); break;
					case IMemberValuePair.K_SHORT: value = Short.parseShort(valueString); break;
					case IMemberValuePair.K_BYTE: value = Byte.parseByte(valueString); break;
					case IMemberValuePair.K_FLOAT: value = Float.parseFloat(valueString); break;
					case IMemberValuePair.K_DOUBLE: value = Double.parseDouble(valueString); break;
					default: throw new IllegalArgumentException("Type not (yet?) supported"); //$NON-NLS-1$
				}
			}
			return new SimpleEntry<>(value, type);
		}
		if (dom instanceof PrefixExpression prefixExpression) {
			Expression operand = prefixExpression.getOperand();
			if (!(operand instanceof NumberLiteral) && !(operand instanceof BooleanLiteral)) {
				return new SimpleEntry<>(null, IMemberValuePair.K_UNKNOWN);
			}
			Entry<Object, Integer> entry = memberValue(prefixExpression.getOperand());
			return new SimpleEntry<>(prefixExpression.getOperator().toString() + entry.getKey(), entry.getValue());
		}
		return new SimpleEntry<>(null, IMemberValuePair.K_UNKNOWN);
	}

	private int toAnnotationValuePairType(String token) {
		// inspired by NumberLiteral.setToken
		Scanner scanner = new Scanner();
		scanner.setSource(token.toCharArray());
		try {
			int tokenType = scanner.getNextToken();
			return switch(tokenType) {
				case TerminalTokens.TokenNameDoubleLiteral -> IMemberValuePair.K_DOUBLE;
				case TerminalTokens.TokenNameIntegerLiteral -> IMemberValuePair.K_INT;
				case TerminalTokens.TokenNameFloatingPointLiteral -> IMemberValuePair.K_FLOAT;
				case TerminalTokens.TokenNameLongLiteral -> IMemberValuePair.K_LONG;
				case TerminalTokens.TokenNameMINUS ->
					switch (scanner.getNextToken()) {
						case TerminalTokens.TokenNameDoubleLiteral -> IMemberValuePair.K_DOUBLE;
						case TerminalTokens.TokenNameIntegerLiteral -> IMemberValuePair.K_INT;
						case TerminalTokens.TokenNameFloatingPointLiteral -> IMemberValuePair.K_FLOAT;
						case TerminalTokens.TokenNameLongLiteral -> IMemberValuePair.K_LONG;
						default -> throw new IllegalArgumentException("Invalid number literal : >" + token + "<"); //$NON-NLS-1$//$NON-NLS-2$
					};
				default -> throw new IllegalArgumentException("Invalid number literal : >" + token + "<"); //$NON-NLS-1$//$NON-NLS-2$
			};
		} catch (InvalidInputException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return IMemberValuePair.K_UNKNOWN;
		}
	}

	private Annotation toModelAnnotation(org.eclipse.jdt.core.dom.Annotation domAnnotation) {
		IMemberValuePair[] members;
		if (domAnnotation instanceof NormalAnnotation normalAnnotation) {
			members = ((List<MemberValuePair>)normalAnnotation.values()).stream().map(domMemberValuePair -> {
				Entry<Object, Integer> value = memberValue(domMemberValuePair.getValue());
				return new org.eclipse.jdt.internal.core.MemberValuePair(domMemberValuePair.getName().toString(), value.getKey(), value.getValue());
			}).toArray(IMemberValuePair[]::new);
		} else if (domAnnotation instanceof SingleMemberAnnotation single) {
			Entry<Object, Integer> value = memberValue(single.getValue());
			members = new IMemberValuePair[] { new org.eclipse.jdt.internal.core.MemberValuePair("value", value.getKey(), value.getValue())}; //$NON-NLS-1$
		} else {
			members = new IMemberValuePair[0];
		}
		return new Annotation(null, domAnnotation.getTypeName().toString()) {
			@Override
			public IMemberValuePair[] getMemberValuePairs() {
				return members;
			}
		};
	}

	private LocalVariable toLocalVariable(SingleVariableDeclaration parameter) {
		return new LocalVariable(this.elements.peek(),
				parameter.getName().getIdentifier(),
				parameter.getStartPosition(),
				parameter.getStartPosition() + parameter.getLength(),
				parameter.getName().getStartPosition(),
				parameter.getName().getStartPosition() + parameter.getName().getLength(),
				parameter.getType().toString(),
				null, // TODO
				parameter.getFlags(),
				parameter.getParent() instanceof MethodDeclaration);
	}

	@Override
	public boolean visit(FieldDeclaration field) {
		JavaElementInfo parentInfo = this.infos.peek();
		JavaElement parentElement = this.elements.peek();
		boolean isDeprecated = isNodeDeprecated(field);
		char[][] categories = getCategories(field);
		for (VariableDeclarationFragment fragment : (Collection<VariableDeclarationFragment>) field.fragments()) {
			SourceField newElement = new SourceField(parentElement, fragment.getName().toString());
			this.elements.push(newElement);
			addAsChild(parentInfo, newElement);
			SourceFieldElementInfo info = new SourceFieldElementInfo();
			info.setTypeName(field.getType().toString().toCharArray());
			info.setSourceRangeStart(field.getStartPosition());
			info.setSourceRangeEnd(field.getStartPosition() + field.getLength() - 1);
			if (parentInfo instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
			}
			info.setFlags(field.getModifiers() | (isDeprecated ? ClassFileConstants.AccDeprecated : 0));
			info.setNameSourceStart(fragment.getName().getStartPosition());
			info.setNameSourceEnd(fragment.getName().getStartPosition() + fragment.getName().getLength() - 1);
			// TODO populate info
			this.infos.push(info);
			this.toPopulate.put(newElement, info);
		}
		return true;
	}
	@Override
	public void endVisit(FieldDeclaration decl) {
		int numFragments = decl.fragments().size();
		for (int i = 0; i < numFragments; i++) {
			this.elements.pop();
			this.infos.pop();
		}
	}

	private String createSignature(SingleVariableDeclaration decl) {
		String initialSignature = Util.getSignature(decl.getType());
		int extraDimensions = decl.getExtraDimensions();
		if (decl.isVarargs()) {
			extraDimensions++;
		}
		return Signature.createArraySignature(initialSignature, extraDimensions);
	}

	@Override
	public boolean visit(Initializer node) {
		org.eclipse.jdt.internal.core.Initializer newElement = new org.eclipse.jdt.internal.core.Initializer(this.elements.peek(), 1);
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		InitializerElementInfo newInfo = new InitializerElementInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.setFlags(node.getModifiers());
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(Initializer decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(ModuleDeclaration node) {
		SourceModule newElement = new SourceModule(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		this.unitInfo.setModule(newElement);
		try {
			this.root.getJavaProject().setModuleDescription(newElement);
		} catch (JavaModelException e) {
			ILog.get().error(e.getMessage(), e);
		}
		ModuleDescriptionInfo newInfo = new ModuleDescriptionInfo();
		newInfo.setHandle(newElement);
		newInfo.name = node.getName().toString().toCharArray();
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.setFlags(node.getFlags());
		List<?> moduleStatements = node.moduleStatements();
		newInfo.requires = moduleStatements.stream()
			.filter(RequiresDirective.class::isInstance)
			.map(RequiresDirective.class::cast)
			.map(this::toModuleReferenceInfo)
			.toArray(ModuleReferenceInfo[]::new);
		newInfo.exports = moduleStatements.stream()
			.filter(ExportsDirective.class::isInstance)
			.map(ExportsDirective.class::cast)
			.map(req -> toPackageExportInfo(req, req.getName(), req.getFlags()))
			.toArray(PackageExportInfo[]::new);
		newInfo.opens = moduleStatements.stream()
			.filter(OpensDirective.class::isInstance)
			.map(OpensDirective.class::cast)
			.map(req -> toPackageExportInfo(req, req.getName(), req.getFlags()))
			.toArray(PackageExportInfo[]::new);
		newInfo.usedServices = moduleStatements.stream()
			.filter(UsesDirective.class::isInstance)
			.map(UsesDirective.class::cast)
			.map(UsesDirective::getName)
			.map(Name::toString)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		newInfo.services = moduleStatements.stream()
			.filter(ProvidesDirective.class::isInstance)
			.map(ProvidesDirective.class::cast)
			.map(this::toServiceInfo)
			.toArray(ServiceInfo[]::new);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(ModuleDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	private ModuleReferenceInfo toModuleReferenceInfo(RequiresDirective node) {
		ModuleReferenceInfo res = new ModuleReferenceInfo();
		res.modifiers = node.getModifiers();
		res.name = node.getName().toString().toCharArray();
		res.setSourceRangeStart(node.getStartPosition());
		res.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		return res;
	}
	private PackageExportInfo toPackageExportInfo(ASTNode node, Name name, int flags) {
		PackageExportInfo res = new PackageExportInfo();
		res.flags = flags;
		res.pack = name.toString().toCharArray();
		res.setSourceRangeStart(node.getStartPosition());
		res.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		return res;
	}
	private ServiceInfo toServiceInfo(ProvidesDirective node) {
		ServiceInfo res = new ServiceInfo();
		res.flags = node.getFlags();
		res.serviceName = node.getName().toString().toCharArray();
		res.implNames = ((List<Name>)node.implementations()).stream().map(Name::toString).map(String::toCharArray).toArray(char[][]::new);
		res.setSourceRangeStart(node.getStartPosition());
		res.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		return res;
	}
	private boolean isNodeDeprecated(BodyDeclaration node) {
		if (node.getJavadoc() != null) {
			boolean javadocDeprecated = node.getJavadoc().tags().stream() //
					.anyMatch(tag -> {
						return TagElement.TAG_DEPRECATED.equals(((AbstractTagElement)tag).getTagName());
					});
			if (javadocDeprecated) {
				return true;
			}
		}
		if (node.getAST().apiLevel() <= 2) {
			return false;
		}
		return ((List<ASTNode>)node.modifiers()).stream() //
				.anyMatch(modifier -> {
					if (!isAnnotation(modifier)) {
						return false;
					}
					String potentiallyUnqualifiedAnnotationType = ((org.eclipse.jdt.core.dom.Annotation)modifier).getTypeName().toString();
					if ("java.lang.Deprecated".equals(potentiallyUnqualifiedAnnotationType)) { //$NON-NLS-1$
						return true;
					}
					return "Deprecated".equals(potentiallyUnqualifiedAnnotationType) && !hasAlternativeDeprecated(); //$NON-NLS-1$
				});
	}
	private static boolean isAnnotation(ASTNode node) {
		int nodeType = node.getNodeType();
		return nodeType == ASTNode.MARKER_ANNOTATION || nodeType == ASTNode.SINGLE_MEMBER_ANNOTATION
				|| nodeType == ASTNode.NORMAL_ANNOTATION;
	}
	private boolean hasAlternativeDeprecated() {
		if (this.alternativeDeprecated != null) {
			return this.alternativeDeprecated;
		}
		if (this.importContainer != null) {
			try {
				IJavaElement[] importElements = this.importContainer.getChildren();
				for (IJavaElement child : importElements) {
					IImportDeclaration importDeclaration = (IImportDeclaration) child;
					// It's possible that the user has imported
					// an annotation called "Deprecated" using a wildcard import
					// that replaces "java.lang.Deprecated"
					// However, it's very costly and complex to check if they've done this,
					// so I haven't bothered.
					if (!importDeclaration.isOnDemand()
							&& importDeclaration.getElementName().endsWith("Deprecated")) { //$NON-NLS-1$
						this.alternativeDeprecated = true;
						return this.alternativeDeprecated;
					}
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		this.alternativeDeprecated = false;
		return this.alternativeDeprecated;
	}
	private char[][] getCategories(BodyDeclaration decl) {
		if (decl.getJavadoc() != null) {
			return ((List<AbstractTagElement>)decl.getJavadoc().tags()).stream() //
					.filter(tag -> "@category".equals(tag.getTagName()) && ((List<ASTNode>)tag.fragments()).size() > 0) //$NON-NLS-1$
					.map(tag -> ((List<ASTNode>)tag.fragments()).get(0)) //
					.map(fragment -> {
						String fragmentString = fragment.toString();
						/**
						 * I think this is a bug in JDT, but I am replicating the behaviour.
						 *
						 * @see CompilationUnitTests.testGetCategories13()
						 */
						int firstAsterix = fragmentString.indexOf('*');
						return fragmentString.substring(0, firstAsterix != -1 ? firstAsterix : fragmentString.length());
					}) //
					.flatMap(fragment -> (Stream<String>)Stream.of(fragment.split("\\s+"))) // //$NON-NLS-1$
					.filter(category -> category.length() > 0) //
					.map(category -> (category).toCharArray()) //
					.toArray(char[][]::new);
		}
		return new char[0][0];
	}
}
