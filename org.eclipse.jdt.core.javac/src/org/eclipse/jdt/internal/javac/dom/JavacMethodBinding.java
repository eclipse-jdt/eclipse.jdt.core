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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.core.util.Util;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.MethodType;

public abstract class JavacMethodBinding implements IMethodBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	public final MethodSymbol methodSymbol;
	private final MethodType methodType;
	final JavacBindingResolver resolver;

	public JavacMethodBinding(MethodType methodType, MethodSymbol methodSymbol, JavacBindingResolver resolver) {
		this.methodType = methodType;
		this.methodSymbol = methodSymbol;
		this.resolver = resolver;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacMethodBinding other
				&& Objects.equals(this.resolver, other.resolver)
				&& Objects.equals(this.methodSymbol, other.methodSymbol)
				&& Objects.equals(this.methodType, other.methodType);
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.resolver, this.methodSymbol, this.methodType);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return methodSymbol.getAnnotationMirrors().stream().map(ann -> this.resolver.bindings.getAnnotationBinding(ann, this)).toArray(IAnnotationBinding[]::new);
	}

	@Override
	public int getKind() {
		return METHOD;
	}

	@Override
	public int getModifiers() {
		return toInt(this.methodSymbol.getModifiers());
	}

	static int toInt(Set<javax.lang.model.element.Modifier> javac) {
		if (javac == null) {
			return 0;
		}
		int[] res = new int[] { 0 };
		javac.forEach(mod -> res[0] |= toInt(mod));
		return res[0];
	}

	private static int toInt(javax.lang.model.element.Modifier javac) {
		return switch (javac) {
			case PUBLIC -> Modifier.PUBLIC;
			case PROTECTED -> Modifier.PROTECTED;
			case PRIVATE -> Modifier.PRIVATE;
			case ABSTRACT -> Modifier.ABSTRACT;
			case DEFAULT -> Modifier.DEFAULT;
			case STATIC -> Modifier.STATIC;
			case SEALED -> Modifier.SEALED;
			case NON_SEALED -> Modifier.NON_SEALED;
			case FINAL -> Modifier.FINAL;
			case TRANSIENT -> Modifier.TRANSIENT;
			case VOLATILE -> Modifier.VOLATILE;
			case SYNCHRONIZED -> Modifier.SYNCHRONIZED;
			case NATIVE -> Modifier.NATIVE;
			case STRICTFP -> Modifier.STRICTFP;
		};
	}

	@Override
	public boolean isDeprecated() {
		return this.methodSymbol.isDeprecated();
	}

	@Override
	public boolean isRecovered() {
		return this.methodSymbol.kind == Kinds.Kind.ERR;
	}

	@Override
	public boolean isSynthetic() {
		return (this.methodSymbol.flags() & Flags.SYNTHETIC) != 0;
	}

	@Override
	public IJavaElement getJavaElement() {
		IJavaElement parent = this.resolver.bindings.getBinding(this.methodSymbol.owner, this.methodType).getJavaElement();
		if (parent instanceof IType type) {
			// prefer DOM object (for type parameters)
			MethodDeclaration methodDeclaration = (MethodDeclaration)this.resolver.findDeclaringNode(this);
			if (methodDeclaration != null) {
				String[] params = ((List<SingleVariableDeclaration>)methodDeclaration.parameters()).stream() //
						.map(param -> Util.getSignature(param.getType())) //
						.toArray(String[]::new);
				return type.getMethod(this.methodSymbol.getSimpleName().toString(), params);
			}
			// fail back to symbol args (type params erased)
			return type.getMethod(this.methodSymbol.getSimpleName().toString(),
					this.methodSymbol.params().stream()
							.map(varSymbol -> varSymbol.type)
							.map(t -> t.tsym.name.toString())
							.map(t -> Signature.createTypeSignature(t, false))
							.toArray(String[]::new));
		}
		return null;
	}

	@Override
	public String getKey() {
		StringBuilder builder = new StringBuilder();
		getKey(builder, this.methodSymbol, this.resolver);
		return builder.toString();
	}

	static void getKey(StringBuilder builder, MethodSymbol methodSymbol, JavacBindingResolver resolver) {
		Symbol ownerSymbol = methodSymbol.owner;
		while (ownerSymbol != null && !(ownerSymbol instanceof TypeSymbol)) {
			ownerSymbol = ownerSymbol.owner;
		}
		if (ownerSymbol instanceof TypeSymbol ownerTypeSymbol) {
			JavacTypeBinding.getKey(builder, resolver.getTypes().erasure(ownerTypeSymbol.type), false);
		} else {
			throw new IllegalArgumentException("Method has no owning class");
		}
		builder.append('.');
		if (!methodSymbol.isConstructor()) {
			builder.append(methodSymbol.getSimpleName());
		}
		if (!methodSymbol.getTypeParameters().isEmpty()) {
			builder.append('<');
			for (var typeParam : methodSymbol.getTypeParameters()) {
				JavacTypeVariableBinding typeVarBinding = resolver.bindings.getTypeVariableBinding(typeParam);
				builder.append(typeVarBinding.getKey());
			}
			builder.append('>');
		}
		builder.append('(');
		for (var param : methodSymbol.getParameters()) {
			JavacTypeBinding.getKey(builder, param.type, false);
		}
		builder.append(')');
		if (!(methodSymbol.getReturnType() instanceof JCNoType)) {
			JavacTypeBinding.getKey(builder, methodSymbol.getReturnType(), false);
		}
		if (
				methodSymbol.getThrownTypes().stream().anyMatch(a -> !a.getParameterTypes().isEmpty())
			) {
			builder.append('^');
			for (var thrownException : methodSymbol.getThrownTypes()) {
				builder.append(thrownException.tsym.getQualifiedName());
			}
		}
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof JavacMethodBinding other && //
			Objects.equals(this.methodSymbol, other.methodSymbol) && //
			Objects.equals(this.resolver, other.resolver);
	}

	@Override
	public boolean isConstructor() {
		return this.methodSymbol.isConstructor();
	}

	@Override
	public boolean isCompactConstructor() {
		return (this.methodSymbol.flags() & Flags.COMPACT_RECORD_CONSTRUCTOR) != 0;
	}

	@Override
	public boolean isCanonicalConstructor() {
		// see com.sun.tools.javac.code.Flags.RECORD
		return (this.methodSymbol.flags() & Flags.RECORD) != 0;
	}

	@Override
	public boolean isDefaultConstructor() {
		return (this.methodSymbol.flags() & Flags.GENERATEDCONSTR) != 0;
	}

	@Override
	public String getName() {
		return this.methodSymbol.getSimpleName().toString();
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		Symbol parentSymbol = this.methodSymbol.owner;
		do {
			if (parentSymbol instanceof ClassSymbol clazz) {
				return this.resolver.bindings.getTypeBinding(clazz.type);
			}
			parentSymbol = parentSymbol.owner;
		} while (parentSymbol != null);
		return null;
	}

	@Override
	public IBinding getDeclaringMember() {
		if (!this.methodSymbol.isLambdaMethod()) {
			return null;
		}
		if (this.methodSymbol.owner instanceof MethodSymbol methodSymbol) {
			return this.resolver.bindings.getMethodBinding(methodSymbol.type.asMethodType(), methodSymbol);
		} else if (this.methodSymbol.owner instanceof VarSymbol variableSymbol) {
			return this.resolver.bindings.getVariableBinding(variableSymbol);
		}
		throw new IllegalArgumentException("Unexpected owner type: " + this.methodSymbol.owner.getClass().getCanonicalName());
	}

	@Override
	public Object getDefaultValue() {
		return this.resolver.getValueFromAttribute(this.methodSymbol.defaultValue);
	}

	@Override
	public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
		VarSymbol parameter = this.methodSymbol.params.get(paramIndex);
		return parameter.getAnnotationMirrors().stream() //
				.map(annotation -> this.resolver.bindings.getAnnotationBinding(annotation, this)) //
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public ITypeBinding[] getParameterTypes() {
		return this.methodSymbol.params().stream()
			.map(param -> param.type)
			.map(this.resolver.bindings::getTypeBinding)
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaredReceiverType() {
		return this.resolver.bindings.getTypeBinding(this.methodSymbol.getReceiverType());
	}

	@Override
	public ITypeBinding getReturnType() {
		return this.resolver.bindings.getTypeBinding(this.methodSymbol.getReturnType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ITypeBinding[] getExceptionTypes() {
		ASTNode node = this.resolver.findNode(this.methodSymbol);
		if (node instanceof MethodDeclaration method) {
			return ((List<Type>)method.thrownExceptionTypes()).stream()
				.map(Type::resolveBinding)
				.toArray(ITypeBinding[]::new);
		}
		return new ITypeBinding[0];
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return this.methodSymbol.getTypeParameters().stream()
				.map(symbol -> this.resolver.bindings.getTypeBinding(symbol.type))
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public boolean isAnnotationMember() {
		return getDeclaringClass().isAnnotation();
	}

	@Override
	public boolean isGenericMethod() {
		return this.methodType.getTypeArguments().isEmpty() && !this.methodSymbol.getTypeParameters().isEmpty();
	}

	@Override
	public boolean isParameterizedMethod() {
		return !this.methodType.getTypeArguments().isEmpty();
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		if (this.methodType.getTypeArguments().isEmpty()) {
			return NO_TYPE_ARGUMENTS;
		}
		return this.methodType.getTypeArguments().stream()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public IMethodBinding getMethodDeclaration() {
		return this;
	}

	@Override
	public boolean isRawMethod() {
		return this.methodSymbol.type.isRaw();
	}

	@Override
	public boolean isSubsignature(IMethodBinding otherMethod) {
		if (otherMethod instanceof JavacMethodBinding otherJavacMethod) {
			return resolver.getTypes().isSubSignature(this.methodSymbol.asType(), otherJavacMethod.methodSymbol.asType());
		}
		return false;
	}

	@Override
	public boolean isVarargs() {
		return this.methodSymbol.isVarArgs();
	}

	@Override
	public boolean overrides(IMethodBinding method) {
		if (method instanceof JavacMethodBinding javacMethod) {
			return Objects.equals(this.methodSymbol.name, javacMethod.methodSymbol.name)
				&&this.methodSymbol.overrides(((JavacMethodBinding)method).methodSymbol, javacMethod.methodSymbol.enclClass(), this.resolver.getTypes(), true);
		}
		return false;
	}

	@Override
	public IVariableBinding[] getSyntheticOuterLocals() {
		if (!this.methodSymbol.isLambdaMethod()) {
			return new IVariableBinding[0];
		}
		return this.methodSymbol.capturedLocals.stream() //
				.map(this.resolver.bindings::getVariableBinding) //
				.toArray(IVariableBinding[]::new);
	}

	@Override
	public boolean isSyntheticRecordMethod() {
		return !this.methodSymbol.isStatic()
				&& (this.methodSymbol.flags() & Flags.SYNTHETIC) != 0
				&& (this.methodSymbol.type.tsym.flags() & Flags.RECORD) != 0;
	}

	@Override
	public String[] getParameterNames() {
		if (this.methodSymbol.getParameters() == null) {
			return new String[0];
		}
		return this.methodSymbol.getParameters().stream() //
			.map(VarSymbol::getSimpleName) //
			.map(Object::toString) //
			.toArray(String[]::new);
	}

}
