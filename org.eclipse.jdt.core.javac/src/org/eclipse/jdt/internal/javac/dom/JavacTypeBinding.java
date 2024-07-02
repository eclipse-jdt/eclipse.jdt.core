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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.core.SourceType;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Kinds.KindSelector;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.JCVoidType;
import com.sun.tools.javac.code.Type.PackageType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Types.FunctionDescriptorLookupError;

public abstract class JavacTypeBinding implements ITypeBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	final JavacBindingResolver resolver;
	public final TypeSymbol typeSymbol;
	private final Types types;
	private final Type type;

	public JavacTypeBinding(final Type type, final TypeSymbol typeSymbol, JavacBindingResolver resolver) {
		if (type instanceof PackageType) {
			throw new IllegalArgumentException("Use JavacPackageBinding");
		}
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
		return this.typeSymbol.getAnnotationMirrors().stream()
				.map(am -> this.resolver.bindings.getAnnotationBinding(am, this))
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
		if (isArray()) {
			return getComponentType().isRecovered();
		}
		return this.typeSymbol.kind == Kinds.Kind.ERR ||
			(Object.class.getName().equals(this.typeSymbol.getQualifiedName().toString())
			&& getJavaElement() == null);
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
		if (typeToBuild.hasTag(TypeTag.UNKNOWN)) {
			builder.append('*');
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
			builder.append(typeToBuild.asElement().flatName().toString().replace('.', '/'));
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
		if (typeToBuild.isNullOrReference()) {
			// should be null, since we've handled references
			return;
		}
		throw new UnsupportedOperationException("Unimplemented method 'getKey'");
	}

	@Override
	public boolean isEqualTo(final IBinding binding) {
		return binding instanceof final JavacTypeBinding other && 
			Objects.equals(this.resolver, other.resolver) && 
			Objects.equals(this.typeSymbol, other.typeSymbol);
	}

	@Override
	public ITypeBinding createArrayType(final int dimension) {
		if (this.type instanceof JCVoidType) {
			return null;
		}
		Type type = this.type;
		for (int i = 0; i < dimension; i++) {
			type = this.types.makeArrayType(type);
		}
		return this.resolver.bindings.getTypeBinding(type);
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
			return (ITypeBinding)this.resolver.bindings.getBinding(wildcardType.type.tsym, wildcardType.type);
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
			return this.resolver.bindings.getTypeBinding(arrayType.elemtype);
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
			.map(this.resolver.bindings::getVariableBinding)
			.toArray(IVariableBinding[]::new);
	}

	@Override
	public IMethodBinding[] getDeclaredMethods() {
		if (this.typeSymbol.members() == null) {
			return new IMethodBinding[0];
		}
		ArrayList<Symbol> l = new ArrayList<>();
		this.typeSymbol.members().getSymbols().forEach(l::add);
		// This is very very questionable, but trying to find
		// the order of these members in the file has been challenging
		Collections.reverse(l);

		return StreamSupport.stream(l.spliterator(), false)
			.filter(MethodSymbol.class::isInstance)
			.map(MethodSymbol.class::cast)
			.map(sym -> this.resolver.bindings.getMethodBinding(sym.type.asMethodType(), sym))
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
		var members = this.typeSymbol.members();
		if (members == null) {
			return new ITypeBinding[0];
		}
		return StreamSupport.stream(members.getSymbols().spliterator(), false)
			.filter(TypeSymbol.class::isInstance)
			.map(TypeSymbol.class::cast)
			.map(sym -> this.resolver.bindings.getTypeBinding(sym.type))
			.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		Symbol parentSymbol = this.typeSymbol.owner;
		do {
			if (parentSymbol instanceof final ClassSymbol clazz) {
				return this.resolver.bindings.getTypeBinding(clazz.type);
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
				return this.resolver.bindings.getMethodBinding(method.type.asMethodType(), method);
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
		return this.resolver.bindings.getBinding(this.typeSymbol.owner, this.typeSymbol.owner.type);
	}

	@Override
	public int getDimensions() {
		return this.types.dimensions(this.type);
	}

	@Override
	public ITypeBinding getElementType() {
		Type t = this.types.elemtype(this.type);
		while (t instanceof Type.ArrayType) {
			t = this.types.elemtype(t);
		}
		if (t == null) {
			return null;
		}
		return this.resolver.bindings.getTypeBinding(t);
	}

	@Override
	public ITypeBinding getErasure() {
		return this.resolver.bindings.getTypeBinding(this.types.erasure(this.type));
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		try {
			Symbol symbol = types.findDescriptorSymbol(this.typeSymbol);
			if (symbol instanceof MethodSymbol methodSymbol) {
				return this.resolver.bindings.getMethodBinding(methodSymbol.type.asMethodType(), methodSymbol);
			}
		} catch (FunctionDescriptorLookupError ignore) {
		}
		return null;
	}

	@Override
	public ITypeBinding[] getInterfaces() {
		return this.types.interfaces(this.type).stream()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new);
//		if (this.typeSymbol instanceof TypeVariableSymbol && this.type instanceof TypeVar tv) {
//			Type t = tv.getUpperBound();
//			if (t.tsym instanceof ClassSymbol) {
//				JavacTypeBinding jtb = this.resolver.bindings.getTypeBinding(t);
//				if( jtb.isInterface()) {
//					return new ITypeBinding[] {jtb};
//				}
//			}
//		}
//
//		if( this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.getInterfaces() != null ) {
//			return 	classSymbol.getInterfaces().map(this.resolver.bindings::getTypeBinding).toArray(ITypeBinding[]::new);
//		}
//		return new ITypeBinding[0];
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
		if (this.isArray()) {
			StringBuilder builder = new StringBuilder(this.getElementType().getName());
			for (int i = 0; i < this.getDimensions(); i++) {
				builder.append("[]");
			}
			return builder.toString();
		}
		return this.typeSymbol.getSimpleName().toString();
	}

	@Override
	public IPackageBinding getPackage() {
		if (isPrimitive() || isArray() || isWildcardType() || isNullType() || isTypeVariable()) {
			return null;
		}
		return this.typeSymbol.packge() != null ?
				this.resolver.bindings.getPackageBinding(this.typeSymbol.packge()) :
			null;
	}

	@Override
	public String getQualifiedName() {
		if (this.typeSymbol.owner instanceof MethodSymbol) {
			return "";
		}
		if (this.type instanceof NullType) {
			return "null";
		}
		if (this.type instanceof ArrayType at) {
			return this.resolver.bindings.getTypeBinding(at.getComponentType()).getQualifiedName() + "[]";
		}

		StringBuilder res = new StringBuilder();
		if (!isParameterizedType()) {
			res.append(this.typeSymbol.getQualifiedName().toString());
		} else {
			res.append(this.type.toString()); // may include type parameters
		}
		// remove annotations here
		int annotationIndex = -1;
		while ((annotationIndex = res.lastIndexOf("@")) >= 0) {
			int nextSpace = res.indexOf(" ", annotationIndex);
			if (nextSpace >= 0) {
				res.delete(annotationIndex, nextSpace + 1);
			}
		}
		return res.toString();
	}

	@Override
	public ITypeBinding getSuperclass() {
		Type superType = this.types.supertype(this.type);
		if (superType != null && !(superType instanceof JCNoType)) {
			return this.resolver.bindings.getTypeBinding(superType);
		}
		String jlObject = this.typeSymbol.getQualifiedName().toString();
		if (Object.class.getName().equals(jlObject)) {
			return null;
		}
		if (this.typeSymbol instanceof TypeVariableSymbol && this.type instanceof TypeVar tv) {
			Type t = tv.getUpperBound();
			JavacTypeBinding possible = this.resolver.bindings.getTypeBinding(t);
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
						return this.resolver.bindings.getTypeBinding(wt);
					}
					working = wt instanceof ClassType ? (ClassType)wt : null;
				}
			}
		}
		if (this.typeSymbol instanceof final ClassSymbol classSymbol && classSymbol.getSuperclass() != null && classSymbol.getSuperclass().tsym != null) {
			return this.resolver.bindings.getTypeBinding(classSymbol.getSuperclass());
		}
		return null;
	}

	@Override
	public IAnnotationBinding[] getTypeAnnotations() {
		if (this.typeSymbol.hasTypeAnnotations()) {
			return new IAnnotationBinding[0];
		}
		// TODO implement this correctly (used to be returning
		// same as getAnnotations() which is incorrect
		return new IAnnotationBinding[0];
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		if (this.type.getTypeArguments().isEmpty()) {
			return NO_TYPE_ARGUMENTS;
		}
		return this.type.getTypeArguments()
				.stream()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding[] getTypeBounds() {
		if (this.type instanceof ClassType classType) {
			Type z1 = classType.supertype_field;
			List<Type> z2 = classType.interfaces_field;
			ArrayList<JavacTypeBinding> l = new ArrayList<>();
			if( z1 != null ) {
				l.add(this.resolver.bindings.getTypeBinding(z1));
			}
			if( z2 != null ) {
				for( int i = 0; i < z2.size(); i++ ) {
					l.add(this.resolver.bindings.getTypeBinding(z2.get(i)));
				}
			}
			return l.toArray(JavacTypeBinding[]::new);
		}
		return new ITypeBinding[0];
	}

	@Override
	public ITypeBinding getTypeDeclaration() {
		return this.typeSymbol.type == this.type
			? this
			: this.resolver.bindings.getTypeBinding(this.typeSymbol.type);
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return isRawType()
			? new ITypeBinding[0]
			: this.type.getParameterTypes()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding getWildcard() {
		//TODO low confidence on this implem.
		if (this.type instanceof WildcardType wildcardType) {
			Type extendsBound = wildcardType.getExtendsBound();
			if (extendsBound != null) {
				return this.resolver.bindings.getTypeBinding(extendsBound);
			}
			Type superBound = wildcardType.getSuperBound();
			if (superBound != null) {
				return this.resolver.bindings.getTypeBinding(superBound);
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
		// records count as classes, so they are not excluded here
		return this.typeSymbol instanceof final ClassSymbol classSymbol
				&& !(classSymbol.isEnum() || classSymbol.isInterface());
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
		return this.resolver.findDeclaringNode(this) != null ||
				getJavaElement() instanceof SourceType ||
				(getDeclaringClass() != null && getDeclaringClass().isFromSource());
	}

	@Override
	public boolean isGenericType() {
		return this.type.isParameterized() && this.type.getTypeArguments().stream().anyMatch(TypeVar.class::isInstance);
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
		//TODO Still not confident in this one,
		//but now it doesn't check recursively
		return this.typeSymbol.owner.kind.matches(KindSelector.VAL_MTH);
	}

	@Override
	public boolean isMember() {
		if (isClass() || isInterface() || isEnum()) {
			return this.typeSymbol.owner instanceof ClassSymbol;
		}
		return false;
	}

	@Override
	public boolean isNested() {
		return getDeclaringClass() != null;
	}

	@Override
	public boolean isNullType() {
		return this.type instanceof NullType || (this.type instanceof ErrorType et && et.getOriginalType() instanceof NullType);
	}

	@Override
	public boolean isParameterizedType() {
		return !this.type.getTypeArguments().isEmpty() && this.type.getTypeArguments().stream().noneMatch(TypeVar.class::isInstance);
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

	@Override
	public IModuleBinding getModule() {
		Symbol o = this.type.tsym.owner;
		if( o instanceof PackageSymbol ps) {
			return this.resolver.bindings.getModuleBinding(ps.modle);
		}
		return null;
	}

	@Override
	public String toString() {
		return Arrays.stream(getAnnotations())
					.map(Object::toString) 
					.map(ann -> ann + " ") 
					.collect(Collectors.joining())
				+ getQualifiedName();
	}

}
