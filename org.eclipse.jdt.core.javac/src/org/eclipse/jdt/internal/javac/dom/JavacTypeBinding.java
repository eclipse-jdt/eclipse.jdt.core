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
import java.util.stream.StreamSupport;

import javax.lang.model.type.NullType;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types.FunctionDescriptorLookupError;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Kinds;

public class JavacTypeBinding implements ITypeBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	final JavacBindingResolver resolver;
	public final TypeSymbol typeSymbol;
	private final Types types;
	private final List<TypeSymbol> typeArguments;

	/**
	 *
	 * @param classSymbol
	 * @param resolver
	 * @param typeArguments the type arguments (NOT the type parameters) or null if this is not a parameterized type
	 */
	public JavacTypeBinding(final TypeSymbol classSymbol, final JavacBindingResolver resolver, final List<TypeSymbol> typeArguments) {
		this.typeSymbol = classSymbol;
		this.resolver = resolver;
		this.types = Types.instance(this.resolver.context);
		this.typeArguments = typeArguments;
	}

	public JavacTypeBinding(final Type type, final JavacBindingResolver resolver, final List<TypeSymbol> typeArguments) {
		this(type.tsym, resolver, typeArguments);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return typeSymbol.getAnnotationMirrors().stream()
				.map(am -> new JavacAnnotationBinding(am, resolver, this))
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public int getKind() {
		return TYPE;
	}

	@Override
	public boolean isDeprecated() {
		return this.typeSymbol.isDeprecated();
	}

	@Override
	public boolean isRecovered() {
		return this.typeSymbol.kind == Kinds.Kind.ERR;
	}

	@Override
	public boolean isSynthetic() {
		return (this.typeSymbol.flags() & Flags.SYNTHETIC) != 0;
	}

	@Override
	public IType getJavaElement() {
		if (this.typeSymbol instanceof final ClassSymbol classSymbol) {
			try {
				return this.resolver.javaProject.findType(classSymbol.className());
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return null;
	}

	@Override
	public String getKey() {
		StringBuilder builder = new StringBuilder();
		getKey(builder, this.typeSymbol.type, false);
		return builder.toString();
	}

	static void getKey(StringBuilder builder, Type typeToBuild, boolean isLeaf) {
		if (typeToBuild instanceof ArrayType arrayType) {
			builder.append('[');
			getKey(builder, arrayType.elemtype, isLeaf);
			return;
		}
		if (typeToBuild.isReference()) {
			if (!isLeaf) {
				builder.append('L');
			}
			builder.append(typeToBuild.asElement().getQualifiedName().toString().replace('.', '/'));
			if (typeToBuild.isParameterized()) {
				builder.append('<');
				for (var typeArgument : typeToBuild.getTypeArguments()) {
					getKey(builder, typeArgument, false);
				}
				builder.append('>');
			}
			if (!isLeaf) {
				builder.append(';');
			}
			return;
		}
		throw new UnsupportedOperationException("Unimplemented method 'getKey'");
	}

	@Override
	public boolean isEqualTo(final IBinding binding) {
		return binding instanceof final JavacTypeBinding other && //
			Objects.equals(this.resolver, other.resolver) && //
			Objects.equals(this.typeSymbol, other.typeSymbol);
	}

	@Override
	public ITypeBinding createArrayType(final int dimension) {
		Type type = this.typeSymbol.type;
		for (int i = 0; i < dimension; i++) {
			type = this.types.makeArrayType(type);
		}
		return new JavacTypeBinding(type, this.resolver, this.typeArguments);
	}

	@Override
	public String getBinaryName() {
		return this.typeSymbol.flatName().toString();
	}

	@Override
	public ITypeBinding getBound() {
		if (!this.isWildcardType()) {
			return null;
		}
		ITypeBinding[] boundsArray = this.getTypeBounds();
		if (boundsArray.length == 1) {
			return boundsArray[0];
		}
		return null;
	}

	@Override
	public ITypeBinding getGenericTypeOfWildcardType() {
		if (!this.isWildcardType()) {
			return null;
		}
		if (this.typeSymbol.type instanceof WildcardType wildcardType) {
			// TODO: probably wrong, we might need to pass in the parent node from the AST
			return (ITypeBinding)this.resolver.getBinding(wildcardType.type.tsym, null);
		}
		throw new IllegalStateException("Binding is a wildcard, but type cast failed");
	}

	@Override
	public int getRank() {
		if (isWildcardType() || isIntersectionType()) {
			return types.rank(this.typeSymbol.type);
		}
		return -1;
	}

	@Override
	public ITypeBinding getComponentType() {
		if (this.typeSymbol.type instanceof ArrayType arrayType) {
			return new JavacTypeBinding(arrayType.elemtype.tsym, this.resolver, null);
		}
		return null;
	}

	@Override
	public IVariableBinding[] getDeclaredFields() {
		if (this.typeSymbol.members() == null) {
			return new IVariableBinding[0];
		}
		return StreamSupport.stream(this.typeSymbol.members().getSymbols().spliterator(), false)
			.filter(VarSymbol.class::isInstance)
			.map(VarSymbol.class::cast)
			.map(sym -> new JavacVariableBinding(sym, this.resolver))
			.toArray(IVariableBinding[]::new);
	}

	@Override
	public IMethodBinding[] getDeclaredMethods() {
		if (this.typeSymbol.members() == null) {
			return new IMethodBinding[0];
		}
		return StreamSupport.stream(this.typeSymbol.members().getSymbols().spliterator(), false)
			.filter(MethodSymbol.class::isInstance)
			.map(MethodSymbol.class::cast)
			.map(sym -> new JavacMethodBinding(sym, this.resolver, null))
			.toArray(IMethodBinding[]::new);
	}

	@Override
	public int getDeclaredModifiers() {
		return this.resolver.findNode(this.typeSymbol) instanceof TypeDeclaration typeDecl ?
			typeDecl.getModifiers() :
			0;
	}

	@Override
	public ITypeBinding[] getDeclaredTypes() {
		return StreamSupport.stream(this.typeSymbol.members().getSymbols().spliterator(), false)
			.filter(TypeSymbol.class::isInstance)
			.map(TypeSymbol.class::cast)
			.map(sym -> new JavacTypeBinding(sym, this.resolver, null))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		Symbol parentSymbol = this.typeSymbol.owner;
		do {
			if (parentSymbol instanceof final ClassSymbol clazz) {
				return new JavacTypeBinding(clazz, this.resolver, null);
			}
			parentSymbol = parentSymbol.owner;
		} while (parentSymbol != null);
		return null;
	}

	@Override
	public IMethodBinding getDeclaringMethod() {
		Symbol parentSymbol = this.typeSymbol.owner;
		do {
			if (parentSymbol instanceof final MethodSymbol method) {
				return new JavacMethodBinding(method, this.resolver, null);
			}
			parentSymbol = parentSymbol.owner;
		} while (parentSymbol != null);
		return null;
	}

	@Override
	public IBinding getDeclaringMember() {
		if (!this.isLocal()) {
			return null;
		}
		return this.resolver.getBinding(this.typeSymbol.owner, null);
	}

	@Override
	public int getDimensions() {
		return this.types.dimensions(this.typeSymbol.type);
	}

	@Override
	public ITypeBinding getElementType() {
		return new JavacTypeBinding(this.types.elemtype(this.typeSymbol.type), this.resolver, null);
	}

	@Override
	public ITypeBinding getErasure() {
		return new JavacTypeBinding(this.types.erasure(this.typeSymbol.type), this.resolver, null);
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		try {
			Symbol symbol = types.findDescriptorSymbol(this.typeSymbol);
			if (symbol instanceof MethodSymbol methodSymbol) {
				return new JavacMethodBinding(methodSymbol, resolver, null);
			}
		} catch (FunctionDescriptorLookupError ignore) {
		}
		return null;
	}

	@Override
	public ITypeBinding[] getInterfaces() {
		return this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.getInterfaces() != null ?
			classSymbol.getInterfaces().map(t -> new JavacTypeBinding(t, this.resolver, null)).toArray(ITypeBinding[]::new) :
			null;
	}

	@Override
	public int getModifiers() {
		return JavacMethodBinding.toInt(this.typeSymbol.getModifiers());
	}

	@Override
	public String getName() {
		return this.typeSymbol.getSimpleName().toString();
	}

	@Override
	public IPackageBinding getPackage() {
		return this.typeSymbol.packge() != null ?
			new JavacPackageBinding(this.typeSymbol.packge(), this.resolver) :
			null;
	}

	@Override
	public String getQualifiedName() {
		return this.typeSymbol.getQualifiedName().toString();
	}

	@Override
	public ITypeBinding getSuperclass() {
		if (this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.getSuperclass() != null && classSymbol.getSuperclass().tsym != null) {
			return new JavacTypeBinding(classSymbol.getSuperclass().tsym, this.resolver, null);
		}
		return null;
	}

	@Override
	public IAnnotationBinding[] getTypeAnnotations() {
		return this.typeSymbol.getAnnotationMirrors().stream() //
				.map(annotation -> new JavacAnnotationBinding(annotation, this.resolver, this)) //
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		if (this.typeArguments == null) {
			return NO_TYPE_ARGUMENTS;
		}
		return this.typeArguments.stream()
			.map(typeArgument -> this.resolver.getBinding(typeArgument, null))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding[] getTypeBounds() {
		Type upperBound = this.typeSymbol.type.getUpperBound();
		if (upperBound == null) {
			return new ITypeBinding[0];
		}
		return new ITypeBinding[] { new JavacTypeBinding(upperBound.tsym, this.resolver, null) };
	}

	@Override
	public ITypeBinding getTypeDeclaration() {
		return this;
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return this.typeSymbol.getTypeParameters().stream()
			.map(symbol -> new JavacTypeBinding(symbol, this.resolver, null))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getWildcard() {
		//TODO low confidence on this implem.
		if (typeSymbol.type instanceof WildcardType wildcardType) {
			Type extendsBound = wildcardType.getExtendsBound();
			if (extendsBound != null) {
				return new JavacTypeBinding(extendsBound, resolver, null);
			}
			Type superBound = wildcardType.getSuperBound();
			if (superBound != null) {
				return new JavacTypeBinding(superBound, resolver, null);
			}
		}
		return null;
	}

	@Override
	public boolean isAnnotation() {
		return this.typeSymbol.isAnnotationType();
	}

	@Override
	public boolean isAnonymous() {
		return this.typeSymbol.isAnonymous();
	}

	@Override
	public boolean isArray() {
		return this.typeSymbol.type instanceof ArrayType;
	}

	@Override
	public boolean isAssignmentCompatible(final ITypeBinding variableType) {
		if (variableType instanceof JavacTypeBinding other) {
			return this.types.isAssignable(other.typeSymbol.type, this.typeSymbol.type);
		}
		throw new UnsupportedOperationException("Cannot mix with non Javac binding"); //$NON-NLS-1$
	}

	@Override
	public boolean isCapture() {
		return this.typeSymbol.type instanceof Type.CapturedType;
	}

	@Override
	public boolean isCastCompatible(final ITypeBinding type) {
		if (type instanceof JavacTypeBinding other) {
			return this.types.isCastable(this.typeSymbol.type, other.typeSymbol.type);
		}
		throw new UnsupportedOperationException("Cannot mix with non Javac binding"); //$NON-NLS-1$
	}

	@Override
	public boolean isClass() {
		return this.typeSymbol instanceof final ClassSymbol classSymbol
				&& !(classSymbol.isEnum() || classSymbol.isRecord());
	}

	@Override
	public boolean isEnum() {
		return this.typeSymbol.isEnum();
	}

	@Override
	public boolean isRecord() {
		return this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.isRecord();
	}

	@Override
	public boolean isFromSource() {
		return this.resolver.findDeclaringNode(this) != null;
	}

	@Override
	public boolean isGenericType() {
		return this.typeArguments == null && !this.typeSymbol.getTypeParameters().isEmpty();
	}

	@Override
	public boolean isInterface() {
		return this.typeSymbol.isInterface();
	}

	@Override
	public boolean isIntersectionType() {
		return this.typeSymbol.type.isIntersection();
	}

	@Override
	public boolean isLocal() {
		//TODO Not supremely confident in this one
		return this.typeSymbol.isDirectlyOrIndirectlyLocal();
	}

	@Override
	public boolean isMember() {
		return this.typeSymbol.owner instanceof TypeSymbol;
	}

	@Override
	public boolean isNested() {
		return getDeclaringClass() != null;
	}

	@Override
	public boolean isNullType() {
		return this.typeSymbol.type instanceof NullType;
	}

	@Override
	public boolean isParameterizedType() {
		return this.typeArguments != null;
	}

	@Override
	public boolean isPrimitive() {
		return this.typeSymbol.type.isPrimitive();
	}

	@Override
	public boolean isRawType() {
		return this.typeSymbol.type.isRaw();
	}

	@Override
	public boolean isSubTypeCompatible(final ITypeBinding type) {
		if (this == type) {
			return true;
		}
		if (type instanceof JavacTypeBinding other) {
			return this.types.isSubtype(this.typeSymbol.type, other.typeSymbol.type);
		}
		return false;
	}

	@Override
	public boolean isTopLevel() {
		return getDeclaringClass() == null;
	}

	@Override
	public boolean isTypeVariable() {
		return this.typeSymbol.type instanceof TypeVar;
	}

	@Override
	public boolean isUpperbound() {
		return this.typeSymbol.type.isExtendsBound();
	}

	@Override
	public boolean isWildcardType() {
		return this.typeSymbol.type instanceof WildcardType;
	}

}
