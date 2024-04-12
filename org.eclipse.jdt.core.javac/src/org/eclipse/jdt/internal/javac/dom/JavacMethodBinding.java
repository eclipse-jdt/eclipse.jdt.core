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
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;

public class JavacMethodBinding implements IMethodBinding {

	public final MethodSymbol methodSymbol;
	final JavacBindingResolver resolver;
	private final List<TypeSymbol> typeArguments;

	public JavacMethodBinding(MethodSymbol sym, JavacBindingResolver resolver, List<TypeSymbol> typeArguments) {
		this.methodSymbol = sym;
		this.resolver = resolver;
		this.typeArguments = typeArguments;
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return methodSymbol.getAnnotationMirrors().stream().map(ann -> new JavacAnnotationBinding(ann, this.resolver)).toArray(IAnnotationBinding[]::new);
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
		IJavaElement parent = this.resolver.getBinding(this.methodSymbol.owner, null).getJavaElement();
		if (parent instanceof IType type) {
			return type.getMethod(this.methodSymbol.getSimpleName().toString(),
				this.methodSymbol.params().stream()
					.map(varSymbol -> varSymbol.type)
					.map(t -> t.tsym.name.toString())
					.toArray(String[]::new));
		}
		return null;
	}

	@Override
	public String getKey() {
		StringBuilder builder = new StringBuilder();
		getKey(builder, this.methodSymbol);
		return builder.toString();
	}

	static void getKey(StringBuilder builder, MethodSymbol methodSymbol) {
		Symbol ownerSymbol = methodSymbol.owner;
		while (ownerSymbol != null && !(ownerSymbol instanceof TypeSymbol)) {
			ownerSymbol = ownerSymbol.owner;
		}
		if (ownerSymbol instanceof TypeSymbol ownerTypeSymbol) {
			builder.append(ownerTypeSymbol.name);
		} else {
			throw new IllegalArgumentException("Method has no owning class");
		}
		builder.append('.');
		// TODO: what is a selector? why is it added?
		for (var typeParam : methodSymbol.getTypeParameters()) {
			builder.append(typeParam.getQualifiedName());
		}
		for (var param : methodSymbol.getParameters()) {
			builder.append(param.getQualifiedName());
		}
		for (var thrownException : methodSymbol.getThrownTypes()) {
			builder.append(thrownException.tsym.getQualifiedName());
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
				return new JavacTypeBinding(clazz, this.resolver, null);
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
			return new JavacMethodBinding(methodSymbol, resolver, null);
		} else if (this.methodSymbol.owner instanceof VarSymbol variableSymbol) {
			return new JavacVariableBinding(variableSymbol, resolver);
		}
		throw new IllegalArgumentException("Unexpected owner type: " + this.methodSymbol.owner.getClass().getCanonicalName());
	}

	@Override
	public Object getDefaultValue() {
		Attribute attribute = this.methodSymbol.defaultValue;
		if (attribute instanceof Attribute.Constant constant) {
			return constant.value;
		} else if (attribute instanceof Attribute.Class clazz) {
			return new JavacTypeBinding(clazz.classType.tsym, this.resolver, null);
		} else if (attribute instanceof Attribute.Enum enumm) {
			return new JavacVariableBinding(enumm.value, this.resolver);
		} else if (attribute instanceof Attribute.Array array) {
			return Stream.of(array.values) //
					.map(nestedAttr -> {
						if (attribute instanceof Attribute.Constant constant) {
							return constant.value;
						} else if (attribute instanceof Attribute.Class clazz) {
							return new JavacTypeBinding(clazz.classType.tsym, this.resolver, null);
						} else if (attribute instanceof Attribute.Enum enumerable) {
							return new JavacVariableBinding(enumerable.value, this.resolver);
						}
						throw new IllegalArgumentException("Unexpected attribute type: " + nestedAttr.getClass().getCanonicalName());
					}) //
					.toArray(Object[]::new);
		}
		throw new IllegalArgumentException("Unexpected attribute type: " + attribute.getClass().getCanonicalName());
	}

	@Override
	public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
		VarSymbol parameter = this.methodSymbol.params.get(paramIndex);
		return parameter.getAnnotationMirrors().stream() //
				.map(annotation -> new JavacAnnotationBinding(annotation, this.resolver)) //
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public ITypeBinding[] getParameterTypes() {
		return this.methodSymbol.params().stream()
			.map(param -> param.type)
			.map(type -> new JavacTypeBinding(type, this.resolver,  /* TODO */ null))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaredReceiverType() {
		return new JavacTypeBinding(this.methodSymbol.getReceiverType(), this.resolver, /* TODO */ null);
	}

	@Override
	public ITypeBinding getReturnType() {
		return new JavacTypeBinding(this.methodSymbol.getReturnType(), this.resolver, /* TODO */ null);
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
				.map(symbol -> new JavacTypeBinding(symbol, this.resolver, null))
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public boolean isAnnotationMember() {
		return getDeclaringClass().isAnnotation();
	}

	@Override
	public boolean isGenericMethod() {
		return this.typeArguments == null && !this.methodSymbol.getTypeParameters().isEmpty();
	}

	@Override
	public boolean isParameterizedMethod() {
		return this.typeArguments != null;
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		return this.typeArguments.stream()
				.map(symbol -> new JavacTypeBinding(symbol, this.resolver, null))
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
				.map(capturedLocal -> new JavacVariableBinding(capturedLocal, this.resolver)) //
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
