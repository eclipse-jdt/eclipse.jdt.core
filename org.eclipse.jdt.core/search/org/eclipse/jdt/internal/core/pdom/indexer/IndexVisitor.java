package org.eclipse.jdt.internal.core.pdom.indexer;

import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.java.JavaIndex;
import org.eclipse.jdt.internal.core.pdom.java.PDOMResourceFile;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTreeNode;

final class IndexVisitor extends HierarchicalASTVisitor {
	private IJavaElement currentElement;
	private PDOMResourceFile file;
	private JavaIndex javaIndex;
	private Stack<PDOMTreeNode> nodeStack;
	private PDOM pdom;
	private String packageName = "";
	private BindingToIndexConverter converter;

	public IndexVisitor(IJavaElement currentElement, PDOMResourceFile file) {
		super();
		this.currentElement = currentElement;
		this.file = file;
		this.nodeStack = new Stack<>();
		this.nodeStack.push(file);
		this.pdom = file.getPDOM();
		this.javaIndex = JavaIndex.getIndex(this.pdom);
		this.converter = new BindingToIndexConverter(file);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		this.packageName = node.getName().getFullyQualifiedName();
		return super.visit(node);
	}

	@Override
	public boolean visit(AbstractTypeDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
//		ITypeBinding typeBinding = node.resolveBinding();
//
//		if (typeBinding != null) {
//			this.converter.addType(typeBinding, null);
//		}
//		typeBinding.get
//		
//		PDOMType newType = new PDOMType(this.pdom, this.nodeStack.lastElement());
//		newType.setFile(this.file);
//
//		PDOMNamedNode parentName = createParentNamedNode();
//		if (parentName != null) {
//			SimpleName name = node.getName();
//			String fqName = name.getFullyQualifiedName();
//			PDOMTypeId typeId = this.javaIndex.createTypeId(createParentNamedNode(), fqName.toCharArray());
//			newType.setTypeId(typeId);
//		}
//
//		ITypeBinding typeBinding = node.resolveBinding();
//		ITypeBinding superclass = typeBinding.getSuperclass();
//
//		
//		Type superclass = node.getSuperclassType();
//
//		if (superclass != null) {
//			if (superclass.isNameQualifiedType()) {
//				NameQualifiedType named = (NameQualifiedType)superclass;
//				SimpleName name = named.getName();
//	
//				String fqName = name.getFullyQualifiedName();
//				String identifier = name.getIdentifier();
//	
//				identifier = identifier;
//			}
//		}

		return true;
	}
	
	@Override
	public void endVisit(AbstractTypeDeclaration node) {
		super.endVisit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		return super.visit(node);
	}
}