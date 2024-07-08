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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.util.Names;

public abstract class JavacMethodBinding implements IMethodBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	public final MethodSymbol methodSymbol;
	final MethodType methodType;
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
		return this.methodSymbol != null ? toInt(this.methodSymbol.getModifiers()) : 0;
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
		// This can be invalid: it looks like it's possible to get some methodSymbol
		// for a method that doesn't exist (eg `Runnable.equals()`). So we may be
		// constructing incorrect bindings.
		// If it is true, then once we only construct correct binding that really
		// reference the method, then we can probably get rid of a lot of complexity
		// here or in `getDeclaringClass()`
		if (this.resolver.bindings.getBinding(this.methodSymbol.owner, this.methodType) instanceof ITypeBinding typeBinding) {
			Queue<ITypeBinding> types = new LinkedList<>();
			types.add(typeBinding);
			while (!types.isEmpty()) {
				ITypeBinding currentBinding = types.poll();
				// prefer DOM object (for type parameters)
				if (currentBinding.getJavaElement() instanceof IType currentType) {
					MethodDeclaration methodDeclaration = (MethodDeclaration)this.resolver.findDeclaringNode(this);
					if (methodDeclaration != null) {
						String[] params = ((List<SingleVariableDeclaration>)methodDeclaration.parameters()).stream() //
								.map(param -> Util.getSignature(param.getType())) //
								.toArray(String[]::new);
						IMethod method = currentType.getMethod(getName(), params);
						if (method.exists()) {
							return method;
						}
					}
					var parametersResolved = this.methodSymbol.params().stream()
							.map(varSymbol -> varSymbol.type)
							.map(t ->
								t instanceof TypeVar typeVar ? Signature.C_TYPE_VARIABLE + typeVar.tsym.name.toString() + ";" : // check whether a better constructor exists for it
									Signature.createTypeSignature(resolveTypeName(t, true), true))
							.toArray(String[]::new);
					IMethod[] methods = currentType.findMethods(currentType.getMethod(getName(), parametersResolved));
					if (methods != null && methods.length > 0) {
						return methods[0];
					}
					var parametersNotResolved = this.methodSymbol.params().stream()
							.map(varSymbol -> varSymbol.type)
							.map(t ->
								t instanceof TypeVar typeVar ? Signature.C_TYPE_VARIABLE + typeVar.tsym.name.toString() + ";" : // check whether a better constructor exists for it
									Signature.createTypeSignature(resolveTypeName(t, false), false))
							.toArray(String[]::new);
					methods = currentType.findMethods(currentType.getMethod(getName(), parametersNotResolved));
					if (methods != null && methods.length > 0) {
						return methods[0];
					}
				}
				// nothing found: move up in hierarchy
				ITypeBinding superClass = currentBinding.getSuperclass();
				if (superClass != null) {
					types.add(superClass);
				}
				types.addAll(Arrays.asList(currentBinding.getInterfaces()));
			}
		}
		return null;
	}

	private String resolveTypeName(com.sun.tools.javac.code.Type type, boolean binary) {
		if (binary) {
			TypeSymbol sym = type.asElement();
			if (sym != null) {
				return sym.getQualifiedName().toString();
			}
			return type.toString(); // this will emit the string representation of the type which might include
									// information which cannot be converted to a type signature.
		}
		return type.asElement().toString();
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
		if (methodSymbol.type != null) { // initializer
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
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof JavacMethodBinding other && //
			Objects.equals(this.methodSymbol, other.methodSymbol) && //
			Objects.equals(this.resolver, other.resolver);
	}

	@Override
	public boolean isConstructor() {
		return this.methodSymbol != null && this.methodSymbol.isConstructor();
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
		if (Objects.equals(Names.instance(this.resolver.context).init, this.methodSymbol.getSimpleName())) {
			return this.getDeclaringClass().getName();
		}
		return this.methodSymbol.getSimpleName().toString();
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		// probably incorrect as it may not return the actual declaring type, see getJavaElement()
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
				.map(annotation -> this.resolver.bindings.getAnnotationBinding(annotation, null)) //
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public ITypeBinding[] getParameterTypes() {
		return this.methodType.getParameterTypes().stream()
			.map(this.resolver.bindings::getTypeBinding)
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaredReceiverType() {
		return this.resolver.bindings.getTypeBinding(this.methodType.getReceiverType());
	}

	@Override
	public ITypeBinding getReturnType() {
		return this.resolver.bindings.getTypeBinding(this.methodType.getReturnType());
	}

	@Override
	public ITypeBinding[] getExceptionTypes() {
		return this.methodType.getThrownTypes().stream() //
				.map(this.resolver.bindings::getTypeBinding) //
				.toArray(ITypeBinding[]::new);
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

	@Override
	public String toString() {
		return modifiersAsString() + getReturnType().getQualifiedName() + ' ' + getName().toString() + '('
				+ Arrays.stream(getParameterTypes()).map(ITypeBinding::getQualifiedName).collect(Collectors.joining(","))
				+ ") ";
	}

	protected String modifiersAsString() {
		String res = "";
		int modifiers = getModifiers();
		if (Modifier.isPublic(modifiers)) {
			res += "public ";
		}
		if (Modifier.isProtected(modifiers)) {
			res += "protected ";
		}
		if (Modifier.isPrivate(modifiers)) {
			res += "private ";
		}
		if (Modifier.isStatic(modifiers)) {
			res += "static ";
		}
		if (Modifier.isAbstract(modifiers)) {
			res += "abstract ";
		}
		return res;
	}
}
