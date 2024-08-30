/*******************************************************************************
 * Copyright (c) 2023, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac.dom;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.JavacBindingResolver.BindingKeyException;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.core.DOMToModelPopulator;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LambdaMethod;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.util.Util;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;

public abstract class JavacVariableBinding implements IVariableBinding {

	public final VarSymbol variableSymbol;
	private final JavacBindingResolver resolver;

	public JavacVariableBinding(VarSymbol sym, JavacBindingResolver resolver) {
		this.variableSymbol = sym;
		this.resolver = resolver;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacVariableBinding other
				&& Objects.equals(this.resolver, other.resolver)
				&& Objects.equals(this.variableSymbol, other.variableSymbol);
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.resolver, this.variableSymbol);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return this.variableSymbol.getAnnotationMirrors().stream()
				.map(am -> this.resolver.bindings.getAnnotationBinding(am, this))
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public int getKind() {
		return VARIABLE;
	}

	@Override
	public int getModifiers() {
		return JavacMethodBinding.toInt(this.variableSymbol.getModifiers());
	}

	@Override
	public boolean isDeprecated() {
		return this.variableSymbol.isDeprecated();
	}

	@Override
	public boolean isRecovered() {
		return this.variableSymbol.kind == Kinds.Kind.ERR || this.variableSymbol.type == null || this.variableSymbol.type instanceof Type.ErrorType;
	}

	@Override
	public boolean isSynthetic() {
		return (this.variableSymbol.flags() & Flags.SYNTHETIC) != 0;
	}

	@Override
	public IJavaElement getJavaElement() {
		if (this.resolver.javaProject == null) {
			return null;
		}
		IMethodBinding methodBinding = getDeclaringMethod();
		if (methodBinding != null && methodBinding.getJavaElement() instanceof IMethod method) {
			if (isParameter()) {
				if (method instanceof LambdaMethod parentLambda && this.resolver.findDeclaringNode(this) instanceof VariableDeclarationFragment decl) {
					return new LocalVariable(parentLambda, getName(),
							decl.getStartPosition(), decl.getStartPosition() + decl.getLength() - 1,
							decl.getName().getStartPosition(), decl.getName().getStartPosition() + decl.getName().getLength() - 1,
							Signature.createTypeSignature(getType().getQualifiedName(), true),
							null,
							getModifiers(),
							true);
				}
				try {
					return Arrays.stream(method.getParameters())
							.filter(param -> Objects.equals(param.getElementName(), getName()))
							.findAny()
							.orElse(null);
				} catch (JavaModelException e) {
					ILog.get().error(e.getMessage(), e);
				}
			} else {
				ASTNode node = this.resolver.findNode(this.variableSymbol);
				if (node instanceof VariableDeclarationFragment fragment) {
					return toLocalVariable(fragment, (JavaElement) method);
				} else if (node instanceof SingleVariableDeclaration variableDecl) {
					return DOMToModelPopulator.toLocalVariable(variableDecl, (JavaElement) method);
				} else if (node instanceof VariableDeclarationStatement statement && statement.fragments().size() == 1) {
					return toLocalVariable((VariableDeclarationFragment)statement.fragments().get(0), (JavaElement)method);
				} else if (node instanceof VariableDeclarationExpression expression && expression.fragments().size() == 1) {
					return toLocalVariable((VariableDeclarationFragment)expression.fragments().get(0), (JavaElement)method);
				}
			}
		}
		if (this.variableSymbol.owner instanceof TypeSymbol parentType // field
			&& parentType.type != null
			&& this.resolver.bindings.getTypeBinding(parentType.type).getJavaElement() instanceof IType type) {
			return type.getField(this.variableSymbol.name.toString());
		}
		return null;
	}

	@Override
	public String getKey() {
		try {
			return getKeyImpl();
		} catch(BindingKeyException bke) {
			return null;
		}
	}
	private String getKeyImpl() throws BindingKeyException {
		StringBuilder builder = new StringBuilder();
		if (this.variableSymbol.owner instanceof ClassSymbol classSymbol) {
			JavacTypeBinding.getKey(builder, classSymbol.type, false, this.resolver);
			builder.append('.');
			builder.append(this.variableSymbol.name);
			builder.append(')');
			if (this.variableSymbol.type != null) {
				JavacTypeBinding.getKey(builder, this.variableSymbol.type, false, this.resolver);
			} else {
				builder.append('V');
			}
			return builder.toString();
		} else if (this.variableSymbol.owner instanceof MethodSymbol methodSymbol) {
			JavacMethodBinding.getKey(builder, methodSymbol, methodSymbol.type instanceof Type.MethodType methodType ? methodType : null, null, this.resolver);
			builder.append('#');
			builder.append(this.variableSymbol.name);
			// FIXME: is it possible for the javac AST to contain multiple definitions of the same variable?
			// If so, we will need to distinguish them (@see org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding)
			return builder.toString();
		}
		throw new UnsupportedOperationException("unhandled `Symbol` subclass " + this.variableSymbol.owner.getClass().toString());
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof JavacVariableBinding other && //
			Objects.equals(this.variableSymbol, other.variableSymbol) && //
			Objects.equals(this.resolver, other.resolver);
	}

	@Override
	public boolean isField() {
		return this.variableSymbol.owner instanceof ClassSymbol;
	}

	@Override
	public boolean isEnumConstant() {
		return this.variableSymbol.isEnum();
	}

	@Override
	public boolean isParameter() {
		return this.variableSymbol.owner instanceof MethodSymbol && (this.variableSymbol.flags() & Flags.PARAMETER) != 0;
	}

	@Override
	public String getName() {
		return this.variableSymbol.getSimpleName().toString();
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		Symbol parentSymbol = this.variableSymbol.owner;
		do {
			if (parentSymbol instanceof MethodSymbol) {
				return null;
			} else if (parentSymbol instanceof ClassSymbol clazz) {
				if( clazz.name.toString().equals("Array") && clazz.owner != null && clazz.owner.kind == Kinds.Kind.NIL) {
					return null;
				}
				if (clazz.type != null) {
					return this.resolver.bindings.getTypeBinding(clazz.type);
				}
			}
			parentSymbol = parentSymbol.owner;
		} while (parentSymbol != null);
		return null;
	}

	@Override
	public ITypeBinding getType() {
		var res = this.resolver.bindings.getTypeBinding(this.variableSymbol.type);
		if (res != null) {
			return res;
		}
		// workaround: Javac doesn't typeSymbol for the variable
		// that does match the recovered one on the type definition
		// In case the typeBinding is wrong, just lookup the declaration
		// in AST to resolve the type definition directly
		ASTNode node = this.resolver.findDeclaringNode(this);
		if (node instanceof SingleVariableDeclaration decl) {
			return decl.getType().resolveBinding();
		} else if (node instanceof VariableDeclarationFragment fragment) {
			if (fragment.getParent() instanceof VariableDeclarationExpression expr) {
				return expr.getType().resolveBinding();
			} else if (fragment.getParent() instanceof FieldDeclaration fieldDecl) {
				return fieldDecl.getType().resolveBinding();
			}
		}
		return null;
	}

	@Override
	public int getVariableId() {
		if (this.resolver.symbolToDeclaration.get(this.variableSymbol) instanceof VariableDeclaration decl) {
			return decl.getStartPosition();
		}
		// FIXME: since we are not running code generation,
		// the variable has not been assigned an offset,
		// so it's always -1.
		return variableSymbol.adr;
	}

	@Override
	public Object getConstantValue() {
		return variableSymbol.getConstantValue();
	}

	@Override
	public IMethodBinding getDeclaringMethod() {
		Symbol parentSymbol = this.variableSymbol.owner;
		do {
			if (parentSymbol instanceof MethodSymbol method) {
				if (!(method.type instanceof Type.MethodType methodType)) {
					return null;
				}
				JavacMethodBinding res = this.resolver.bindings.getMethodBinding(methodType, method, null, false);
				if (this.resolver.findDeclaringNode(this).getParent() instanceof LambdaExpression lambda) {
					return lambda.resolveMethodBinding();
				}
				return res;
			}
			parentSymbol = parentSymbol.owner;
		} while (parentSymbol != null);
		return null;
	}

	@Override
	public IVariableBinding getVariableDeclaration() {
		return this;
	}

	@Override
	public boolean isEffectivelyFinal() {
		return (this.variableSymbol.flags() & Flags.EFFECTIVELY_FINAL) != 0;
	}

	private static LocalVariable toLocalVariable(VariableDeclarationFragment fragment, JavaElement parent) {
		if (fragment.getParent() instanceof VariableDeclarationStatement variableDeclaration) {
			return new LocalVariable(parent,
				fragment.getName().getIdentifier(),
				variableDeclaration.getStartPosition(),
				variableDeclaration.getStartPosition() + variableDeclaration.getLength() - 1,
				fragment.getName().getStartPosition(),
				fragment.getName().getStartPosition() + fragment.getName().getLength() - 1,
				Util.getSignature(variableDeclaration.getType()),
				null, // I don't think we need this, also it's the ECJ's annotation node
				toModelFlags(variableDeclaration.getModifiers(), false),
				false);
		} else if (fragment.getParent() instanceof VariableDeclarationExpression variableDeclaration) {
			return new LocalVariable(parent,
					fragment.getName().getIdentifier(),
					variableDeclaration.getStartPosition(),
					variableDeclaration.getStartPosition() + variableDeclaration.getLength() - 1,
					fragment.getName().getStartPosition(),
					fragment.getName().getStartPosition() + fragment.getName().getLength() - 1,
					Util.getSignature(variableDeclaration.getType()),
					null, // I don't think we need this, also it's the ECJ's annotation node
					toModelFlags(variableDeclaration.getModifiers(), false),
					false);
		}
		return null;
	}

	private static int toModelFlags(int domModifiers, boolean isDeprecated) {
		int res = 0;
		if (Modifier.isAbstract(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccAbstract;
		if (Modifier.isDefault(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccDefaultMethod;
		if (Modifier.isFinal(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccFinal;
		if (Modifier.isNative(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccNative;
		if (Modifier.isNonSealed(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccNonSealed;
		if (Modifier.isPrivate(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccPrivate;
		if (Modifier.isProtected(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccProtected;
		if (Modifier.isPublic(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccPublic;
		if (Modifier.isSealed(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccSealed;
		if (Modifier.isStatic(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccStatic;
		if (Modifier.isStrictfp(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccStrictfp;
		if (Modifier.isSynchronized(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccSynchronized;
		if (Modifier.isTransient(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccTransient;
		if (Modifier.isVolatile(domModifiers)) res |= org.eclipse.jdt.core.Flags.AccVolatile;
		if (isDeprecated) res |= org.eclipse.jdt.core.Flags.AccDeprecated;
		return res;
	}

	@Override
	public String toString() {
		return getType().getQualifiedName() + " " + getName();
	}

	@Override
	public boolean isRecordComponent() {
		return this.variableSymbol.owner instanceof ClassSymbol ownerType
				&& ownerType.isRecord();
	}
}
