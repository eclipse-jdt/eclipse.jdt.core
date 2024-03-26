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

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;

public class JavacMethodBinding implements IMethodBinding {

	public final MethodSymbol methodSymbol;
	final JavacBindingResolver resolver;

	public JavacMethodBinding(MethodSymbol sym, JavacBindingResolver resolver) {
		this.methodSymbol = sym;
		this.resolver = resolver;
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isRecovered'");
	}

	@Override
	public boolean isSynthetic() {
		return (this.methodSymbol.flags() & Flags.SYNTHETIC) != 0;
	}

	@Override
	public IJavaElement getJavaElement() {
		IJavaElement parent = this.resolver.getBinding(this.methodSymbol.owner).getJavaElement();
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getKey'");
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isCompactConstructor'");
	}

	@Override
	public boolean isCanonicalConstructor() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isCanonicalConstructor'");
	}

	@Override
	public boolean isDefaultConstructor() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isDefaultConstructor'");
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
				return new JavacTypeBinding(clazz, this.resolver);
			}
			parentSymbol = parentSymbol.owner;
		} while (parentSymbol != null);
		return null;
	}

	@Override
	public IBinding getDeclaringMember() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getDeclaringMember'");
	}

	@Override
	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getDefaultValue'");
	}

	@Override
	public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getParameterAnnotations'");
	}

	@Override
	public ITypeBinding[] getParameterTypes() {
		return this.methodSymbol.params().stream()
			.map(param -> param.type)
			.map(type -> new JavacTypeBinding(type, this.resolver))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaredReceiverType() {
		return new JavacTypeBinding(this.methodSymbol.getReceiverType(), this.resolver);
	}

	@Override
	public ITypeBinding getReturnType() {
		return new JavacTypeBinding(this.methodSymbol.getReturnType(), this.resolver);
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTypeParameters'");
	}

	@Override
	public boolean isAnnotationMember() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isAnnotationMember'");
	}

	@Override
	public boolean isGenericMethod() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isGenericMethod'");
	}

	@Override
	public boolean isParameterizedMethod() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isParameterizedMethod'");
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTypeArguments'");
	}

	@Override
	public IMethodBinding getMethodDeclaration() {
		return this;
	}

	@Override
	public boolean isRawMethod() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isRawMethod'");
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isVarargs'");
	}

	@Override
	public boolean overrides(IMethodBinding method) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'overrides'");
	}

	@Override
	public IVariableBinding[] getSyntheticOuterLocals() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getSyntheticOuterLocals'");
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
