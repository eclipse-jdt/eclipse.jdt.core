/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTConverterBindingsTest extends ConverterTestSetup {
	private static final boolean DEBUG = false;

	static class BindingsCollector extends ASTVisitor {

		public ArrayList arrayList;

		BindingsCollector() {
			// visit Javadoc.tags() as well
			super(true);
			this.arrayList = new ArrayList();
		}

		private void collectBindings(
			ASTNode node,
			IBinding binding) {

			if (binding != null) {
				this.arrayList.add(binding);
			}
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(AnnotationTypeDeclaration)
		 * @since 3.0
		 */
		public void endVisit(AnnotationTypeDeclaration node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(AnnotationTypeMemberDeclaration)
		 * @since 3.0
		 */
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			IMethodBinding binding = node.resolveBinding();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(AnonymousClassDeclaration)
		 */
		public void endVisit(AnonymousClassDeclaration node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayAccess)
		 */
		public void endVisit(ArrayAccess node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayCreation)
		 */
		public void endVisit(ArrayCreation node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayInitializer)
		 */
		public void endVisit(ArrayInitializer node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayType)
		 */
		public void endVisit(ArrayType node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(Assignment)
		 */
		public void endVisit(Assignment node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(BooleanLiteral)
		 */
		public void endVisit(BooleanLiteral node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(CastExpression)
		 */
		public void endVisit(CastExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(CharacterLiteral)
		 */
		public void endVisit(CharacterLiteral node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ClassInstanceCreation)
		 */
		public void endVisit(ClassInstanceCreation node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ConditionalExpression)
		 */
		public void endVisit(ConditionalExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ConstructorInvocation)
		 */
		public void endVisit(ConstructorInvocation node) {
			IMethodBinding methodBinding = node.resolveConstructorBinding();
			collectBindings(node, methodBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(EnumConstantDeclaration)
		 * @since 3.0
		 */
		public void endVisit(EnumConstantDeclaration node) {
			IVariableBinding binding = node.resolveVariable();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(EnumDeclaration)
		 * @since 3.0
		 */
		public void endVisit(EnumDeclaration node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(FieldAccess)
		 */
		public void endVisit(FieldAccess node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ImportDeclaration)
		 */
		public void endVisit(ImportDeclaration node) {
			IBinding binding = node.resolveBinding();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(InfixExpression)
		 */
		public void endVisit(InfixExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(InstanceofExpression)
		 */
		public void endVisit(InstanceofExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see ASTVisitor#endVisit(MemberRef)
		 * @since 3.0
		 */
		public void endVisit(MemberRef node) {
			IBinding binding = node.resolveBinding();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(MethodDeclaration)
		 */
		public void endVisit(MethodDeclaration node) {
			IMethodBinding methodBinding = node.resolveBinding();
			collectBindings(node, methodBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ModuleDeclaration)
		 */
		public void endVisit(ModuleDeclaration node) {
			IModuleBinding moduleBinding = node.resolveBinding();
			collectBindings(node, moduleBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(MethodInvocation)
		 */
		public void endVisit(MethodInvocation node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see ASTVisitor#endVisit(MethodRef )
		 * @since 3.0
		 */
		public void endVisit(MethodRef node) {
			IBinding binding = node.resolveBinding();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(NullLiteral)
		 */
		public void endVisit(NullLiteral node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(NumberLiteral)
		 */
		public void endVisit(NumberLiteral node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PackageDeclaration)
		 */
		public void endVisit(PackageDeclaration node) {
			IPackageBinding packageBinding = node.resolveBinding();
			collectBindings(node, packageBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ParameterizedType)
		 * @since 3.0
		 */
		public void endVisit(ParameterizedType node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ParenthesizedExpression)
		 */
		public void endVisit(ParenthesizedExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PostfixExpression)
		 */
		public void endVisit(PostfixExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PrefixExpression)
		 */
		public void endVisit(PrefixExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PrimitiveType)
		 */
		public void endVisit(PrimitiveType node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(QualifiedName)
		 */
		public void endVisit(QualifiedName node) {
			IBinding binding = node.resolveBinding();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SimpleName)
		 */
		public void endVisit(SimpleName node) {
			IBinding binding = node.resolveBinding();
			collectBindings(node, binding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SimpleType)
		 */
		public void endVisit(SimpleType node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SingleVariableDeclaration)
		 */
		public void endVisit(SingleVariableDeclaration node) {
			IVariableBinding variableBinding = node.resolveBinding();
			collectBindings(node, variableBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(StringLiteral)
		 */
		public void endVisit(StringLiteral node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SuperConstructorInvocation)
		 */
		public void endVisit(SuperConstructorInvocation node) {
			IMethodBinding methodBinding = node.resolveConstructorBinding();
			collectBindings(node, methodBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SuperFieldAccess)
		 */
		public void endVisit(SuperFieldAccess node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SuperMethodInvocation)
		 */
		public void endVisit(SuperMethodInvocation node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ThisExpression)
		 */
		public void endVisit(ThisExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(TypeDeclaration)
		 */
		public void endVisit(TypeDeclaration node) {
			ITypeBinding typeBinding = node.resolveBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(TypeLiteral)
		 */
		public void endVisit(TypeLiteral node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(VariableDeclarationExpression)
		 */
		public void endVisit(VariableDeclarationExpression node) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			collectBindings(node, typeBinding);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(VariableDeclarationFragment)
		 */
		public void endVisit(VariableDeclarationFragment node) {
			IVariableBinding variableBinding = node.resolveBinding();
			collectBindings(node, variableBinding);
		}

		public List getBindings() {
			return this.arrayList;
		}

	}


	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getJLS3(), false);
	}

	public ASTConverterBindingsTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverterBindingsTest.class);
	}

	public void test0001() throws JavaModelException {
		checkBindingEqualityForProject("Converter");
		checkBindingEqualityForProject("Converter15");
	}

	private void checkBindingEqualityForProject(String projectName) throws JavaModelException {
		IJavaProject javaProject = getJavaProject(projectName);
		IPackageFragment[] packageFragments = javaProject.getPackageFragments();
		ArrayList compilationUnitscollector = new ArrayList();
		for (int j = 0, max2 = packageFragments.length; j < max2; j++) {
			ICompilationUnit[] units = packageFragments[j].getCompilationUnits();
			if (units != null) {
				for (int k = 0, max3 = units.length; k < max3; k++) {
					compilationUnitscollector.add(units[k]);
				}
			}
		}
		final int length = compilationUnitscollector.size();
		ICompilationUnit[] units = new ICompilationUnit[length];
		compilationUnitscollector.toArray(units);
		for (int j = 0; j < length; j++) {
			ICompilationUnit currentUnit = units[j];
			ASTNode result = runConversion(getJLS3(), currentUnit, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType());
			CompilationUnit unit = (CompilationUnit) result;
			result = runConversion(getJLS3(), currentUnit, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType());
			if (DEBUG) {
				if (unit.types().size() > 0 ) {
					AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) unit.types().get(0);
					StringBuffer buffer = new StringBuffer();
					PackageDeclaration packageDeclaration = unit.getPackage();
					if (packageDeclaration != null) {
						buffer.append(unit.getPackage().getName()).append(".").append(typeDeclaration.getName());
					} else {
						buffer.append(typeDeclaration.getName());
					}
					System.out.println(String.valueOf(buffer));
				} else {
					System.out.println(currentUnit.getElementName());
				}
			}
			CompilationUnit unit2 = (CompilationUnit) result;
			BindingsCollector collector = new BindingsCollector();
			unit.accept(collector);
			List bindings1 = collector.getBindings();
			BindingsCollector collector2 = new BindingsCollector();
			unit2.accept(collector2);
			List bindings2 = collector2.getBindings();
			assertEquals("Wrong size", bindings1.size(), bindings2.size());
			for (int i = 0, max = bindings1.size(); i < max; i++) {
				final Object object = bindings1.get(i);
				assertTrue("not a binding", object instanceof IBinding);
				final IBinding binding = (IBinding) object;
				final Object object2 = bindings2.get(i);
				assertTrue("not a binding", object2 instanceof IBinding);
				final IBinding binding2 = (IBinding) object2;
				final boolean equalTo = binding.isEqualTo(binding2);
				assertTrue("not equals", equalTo);
			}
		}
	}
}
