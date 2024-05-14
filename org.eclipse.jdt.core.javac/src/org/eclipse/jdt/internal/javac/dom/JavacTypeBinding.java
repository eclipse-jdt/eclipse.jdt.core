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

import java.util.Objects;
import java.util.stream.StreamSupport;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Types.FunctionDescriptorLookupError;

public class JavacTypeBinding implements ITypeBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	final JavacBindingResolver resolver;
	public final TypeSymbol typeSymbol;
	private final Types types;
	private final Type type;

	public JavacTypeBinding(final Type type, final JavacBindingResolver resolver) {
		this(type, type.tsym, resolver);
	}

	private JavacTypeBinding(final Type type, final TypeSymbol typeSymbol, JavacBindingResolver resolver) {
		this.type = type;
		this.typeSymbol = typeSymbol;
		this.resolver = resolver;
		this.types = Types.instance(this.resolver.context);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacTypeBinding other
				&& Objects.equals(this.resolver, other.resolver)
				&& Objects.equals(this.type, other.type)
				&& Objects.equals(this.typeSymbol, other.typeSymbol);
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.resolver, this.type, this.typeSymbol);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return typeSymbol.getAnnotationMirrors().stream()
				.map(am -> this.resolver.canonicalize(new JavacAnnotationBinding(am, resolver, this)))
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
		if (this.resolver.javaProject == null) {
			return null;
		}
		if (this.typeSymbol instanceof final ClassSymbol classSymbol) {
			try {
				return this.resolver.javaProject.findType(classSymbol.className(), new NullProgressMonitor());
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return null;
	}

	@Override
	public String getKey() {
		return getKey(this.type);
	}
	public String getKey(Type t) {
		StringBuilder builder = new StringBuilder();
		getKey(builder, t, false);
		return builder.toString();
	}

	static void getKey(StringBuilder builder, Type typeToBuild, boolean isLeaf) {
		if (typeToBuild instanceof Type.JCNoType) {
			return;
		}
		if (typeToBuild instanceof ArrayType arrayType) {
			builder.append('[');
			getKey(builder, arrayType.elemtype, isLeaf);
			return;
		}
		if (typeToBuild instanceof Type.WildcardType wildcardType) {
			if (wildcardType.isUnbound()) {
				builder.append('*');
			} else if (wildcardType.isExtendsBound()) {
				builder.append('+');
				getKey(builder, wildcardType.getExtendsBound(), isLeaf);
			} else if (wildcardType.isSuperBound()) {
				builder.append('-');
				getKey(builder, wildcardType.getSuperBound(), isLeaf);
			}
			return;
		}
		if (typeToBuild.isReference()) {
			if (!isLeaf) {
				if (typeToBuild.tsym instanceof Symbol.TypeVariableSymbol) {
					builder.append('T');
				} else {
					builder.append('L');
				}
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
		if (typeToBuild.isPrimitiveOrVoid()) {
			/**
			 * @see org.eclipse.jdt.core.Signature
			 */
			switch (typeToBuild.getKind()) {
			case TypeKind.BYTE: builder.append('B'); return;
			case TypeKind.CHAR: builder.append('C'); return;
			case TypeKind.DOUBLE: builder.append('D'); return;
			case TypeKind.FLOAT: builder.append('F'); return;
			case TypeKind.INT: builder.append('I'); return;
			case TypeKind.LONG: builder.append('J'); return;
			case TypeKind.SHORT: builder.append('S'); return;
			case TypeKind.BOOLEAN: builder.append('Z'); return;
			case TypeKind.VOID: builder.append('V'); return;
			default: // fall through to unsupported operation exception
			}
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
		Type type = this.type;
		for (int i = 0; i < dimension; i++) {
			type = this.types.makeArrayType(type);
		}
		return this.resolver.canonicalize(new JavacTypeBinding(type, this.resolver));
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
			return (ITypeBinding)this.resolver.getBinding(wildcardType.type.tsym, wildcardType.type);
		}
		throw new IllegalStateException("Binding is a wildcard, but type cast failed");
	}

	@Override
	public int getRank() {
		if (isWildcardType() || isIntersectionType()) {
			return types.rank(this.type);
		}
		return -1;
	}

	@Override
	public ITypeBinding getComponentType() {
		if (this.type instanceof ArrayType arrayType) {
			return this.resolver.canonicalize(new JavacTypeBinding(arrayType.elemtype, this.resolver));
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
			.map(sym -> this.resolver.canonicalize(new JavacVariableBinding(sym, this.resolver)))
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
			.map(sym -> this.resolver.canonicalize(new JavacMethodBinding(sym.type.asMethodType(), sym, this.resolver)))
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
			.map(sym -> this.resolver.canonicalize(new JavacTypeBinding(sym.type, this.resolver)))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		Symbol parentSymbol = this.typeSymbol.owner;
		do {
			if (parentSymbol instanceof final ClassSymbol clazz) {
				return this.resolver.canonicalize(new JavacTypeBinding(clazz.type, this.resolver));
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
				return this.resolver.canonicalize(new JavacMethodBinding(method.type.asMethodType(), method, this.resolver));
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
		return this.resolver.getBinding(this.typeSymbol.owner, this.typeSymbol.owner.type);
	}

	@Override
	public int getDimensions() {
		return this.types.dimensions(this.type);
	}

	@Override
	public ITypeBinding getElementType() {
		Type t = this.types.elemtype(this.type);
		if (t == null) {
			return null;
		}
		return this.resolver.canonicalize(new JavacTypeBinding(t, this.resolver));
	}

	@Override
	public ITypeBinding getErasure() {
		return this.resolver.canonicalize(new JavacTypeBinding(this.types.erasure(this.type), this.resolver));
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		try {
			Symbol symbol = types.findDescriptorSymbol(this.typeSymbol);
			if (symbol instanceof MethodSymbol methodSymbol) {
				return this.resolver.canonicalize(new JavacMethodBinding(methodSymbol.type.asMethodType(), methodSymbol, resolver));
			}
		} catch (FunctionDescriptorLookupError ignore) {
		}
		return null;
	}

	@Override
	public ITypeBinding[] getInterfaces() {
		if (this.typeSymbol instanceof TypeVariableSymbol && this.type instanceof TypeVar tv) {
			Type t = tv.getUpperBound();
			if (t.tsym instanceof ClassSymbol) {
				JavacTypeBinding jtb = this.resolver.canonicalize(new JavacTypeBinding(t, this.resolver));
				if( jtb.isInterface()) {
					return new ITypeBinding[] {jtb};
				}
			}
		}

		if( this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.getInterfaces() != null ) {
			return 	classSymbol.getInterfaces().map(t -> new JavacTypeBinding(t, this.resolver)).toArray(ITypeBinding[]::new);
		}
		return new ITypeBinding[0];
	}

	@Override
	public int getModifiers() {
		int modifiers = JavacMethodBinding.toInt(this.typeSymbol.getModifiers());
		// JDT doesn't mark interfaces as abstract
		if (this.isInterface()) {
			modifiers &= ~Modifier.ABSTRACT;
		}
		return modifiers;
	}

	@Override
	public String getName() {
		return this.typeSymbol.getSimpleName().toString();
	}

	@Override
	public IPackageBinding getPackage() {
		return this.typeSymbol.packge() != null ?
				this.resolver.canonicalize(new JavacPackageBinding(this.typeSymbol.packge(), this.resolver)) :
			null;
	}

	@Override
	public String getQualifiedName() {
		if (this.typeSymbol.owner instanceof MethodSymbol) {
			return "";
		}
		return this.typeSymbol.getQualifiedName().toString();
	}

	@Override
	public ITypeBinding getSuperclass() {
		if (this.typeSymbol instanceof TypeVariableSymbol && this.type instanceof TypeVar tv) {
			Type t = tv.getUpperBound();
			JavacTypeBinding possible = this.resolver.canonicalize(new JavacTypeBinding(t, this.resolver));
			if( !possible.isInterface()) {
				return possible;
			}
			if( t instanceof ClassType ct ) {
				// we need to return java.lang.object
				ClassType working = ct;
				while( working != null ) {
					Type wt = working.supertype_field;
					String sig = getKey(wt);
					if( new String(ConstantPool.JavaLangObjectSignature).equals(sig)) {
						return this.resolver.canonicalize(new JavacTypeBinding(wt, this.resolver));
					}
					working = wt instanceof ClassType ? (ClassType)wt : null;
				}
			}
		}
		if (this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.getSuperclass() != null && classSymbol.getSuperclass().tsym != null) {
			return this.resolver.canonicalize(new JavacTypeBinding(classSymbol.getSuperclass(), this.resolver));
		}

		return null;
	}

	@Override
	public IAnnotationBinding[] getTypeAnnotations() {
		return this.typeSymbol.getAnnotationMirrors().stream() //
				.map(annotation -> this.resolver.canonicalize(new JavacAnnotationBinding(annotation, this.resolver, this))) //
				.toArray(IAnnotationBinding[]::new);
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		if (this.type.getTypeArguments().isEmpty()) {
			return NO_TYPE_ARGUMENTS;
		}
		return this.type.getTypeArguments()
				.stream()
				.map(typeArg -> this.resolver.canonicalize(new JavacTypeBinding(typeArg, this.resolver)))
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding[] getTypeBounds() {
		Type upperBound = this.type.getUpperBound();
		if (upperBound == null) {
			return new ITypeBinding[0];
		}
		return new ITypeBinding[] { this.resolver.canonicalize(new JavacTypeBinding(upperBound, this.resolver)) };
	}

	@Override
	public ITypeBinding getTypeDeclaration() {
		return this;
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return this.typeSymbol.getTypeParameters().stream()
			.map(symbol -> this.resolver.canonicalize(new JavacTypeBinding(symbol.type, this.resolver)))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getWildcard() {
		//TODO low confidence on this implem.
		if (this.type instanceof WildcardType wildcardType) {
			Type extendsBound = wildcardType.getExtendsBound();
			if (extendsBound != null) {
				return this.resolver.canonicalize(new JavacTypeBinding(extendsBound, resolver));
			}
			Type superBound = wildcardType.getSuperBound();
			if (superBound != null) {
				return this.resolver.canonicalize(new JavacTypeBinding(superBound, resolver));
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
		return this.type instanceof ArrayType;
	}

	@Override
	public boolean isAssignmentCompatible(final ITypeBinding variableType) {
		if (variableType instanceof JavacTypeBinding other) {
			return this.types.isAssignable(other.type, this.type);
		}
		throw new UnsupportedOperationException("Cannot mix with non Javac binding"); //$NON-NLS-1$
	}

	@Override
	public boolean isCapture() {
		return this.type instanceof Type.CapturedType;
	}

	@Override
	public boolean isCastCompatible(final ITypeBinding type) {
		if (type instanceof JavacTypeBinding other) {
			return this.types.isCastable(this.type, other.type);
		}
		throw new UnsupportedOperationException("Cannot mix with non Javac binding"); //$NON-NLS-1$
	}

	@Override
	public boolean isClass() {
		return this.typeSymbol instanceof final ClassSymbol classSymbol
				&& !(classSymbol.isEnum() || classSymbol.isRecord() || classSymbol.isInterface());
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
		return this.type.getTypeArguments().isEmpty() && !this.typeSymbol.getTypeParameters().isEmpty();
	}

	@Override
	public boolean isInterface() {
		return this.typeSymbol.isInterface();
	}

	@Override
	public boolean isIntersectionType() {
		return this.type.isIntersection();
	}

	@Override
	public boolean isLocal() {
		//TODO Not supremely confident in this one
		return this.typeSymbol.isDirectlyOrIndirectlyLocal();
	}

	@Override
	public boolean isMember() {
		return this.typeSymbol.owner instanceof ClassSymbol;
	}

	@Override
	public boolean isNested() {
		return getDeclaringClass() != null;
	}

	@Override
	public boolean isNullType() {
		return this.type instanceof NullType;
	}

	@Override
	public boolean isParameterizedType() {
		return !this.type.getTypeArguments().isEmpty();
	}

	@Override
	public boolean isPrimitive() {
		return this.type.isPrimitiveOrVoid();
	}

	@Override
	public boolean isRawType() {
		return this.type.isRaw();
	}

	@Override
	public boolean isSubTypeCompatible(final ITypeBinding type) {
		if (this == type) {
			return true;
		}
		if (type instanceof JavacTypeBinding other) {
			return this.types.isSubtype(this.type, other.type);
		}
		return false;
	}

	@Override
	public boolean isTopLevel() {
		return getDeclaringClass() == null;
	}

	@Override
	public boolean isTypeVariable() {
		return this.type instanceof TypeVar;
	}

	@Override
	public boolean isUpperbound() {
		return this.type.isExtendsBound();
	}

	@Override
	public boolean isWildcardType() {
		return this.type instanceof WildcardType;
	}

}
