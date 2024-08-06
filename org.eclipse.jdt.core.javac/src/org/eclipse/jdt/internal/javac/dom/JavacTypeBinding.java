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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
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
import org.eclipse.jdt.internal.core.util.Util;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Kinds.KindSelector;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.RootPackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.IntersectionClassType;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.JCVoidType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.PackageType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Types.FunctionDescriptorLookupError;
import com.sun.tools.javac.util.Name;

public abstract class JavacTypeBinding implements ITypeBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	final JavacBindingResolver resolver;
	public final TypeSymbol typeSymbol;
	private final Types types;
	private final Type type;
	private boolean recovered = false;

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
		if (recovered) {
			return true;
		}
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
	public IJavaElement getJavaElement() {
		if (isTypeVariable() && this.typeSymbol != null) {
			if (this.typeSymbol.owner instanceof ClassSymbol ownerSymbol
					&& ownerSymbol.type != null
					&& this.resolver.bindings.getTypeBinding(ownerSymbol.type).getJavaElement() instanceof IType ownerType
					&& ownerType.getTypeParameter(this.getName()) != null) {
				return ownerType.getTypeParameter(this.getName());
			} else if (this.typeSymbol.owner instanceof MethodSymbol ownerSymbol
					&& ownerSymbol.type != null
					&& this.resolver.bindings.getMethodBinding(ownerSymbol.type.asMethodType(), ownerSymbol).getJavaElement() instanceof IMethod ownerMethod
					&& ownerMethod.getTypeParameter(this.getName()) != null) {
				return ownerMethod.getTypeParameter(this.getName());
			}
		}
		if (this.resolver.javaProject == null) {
			return null;
		}
		if (this.isArray()) {
			return (IType) this.getElementType().getJavaElement();
		}
		if (this.typeSymbol instanceof final ClassSymbol classSymbol) {
			if (isAnonymous()) {
				if (getDeclaringMethod() != null && getDeclaringMethod().getJavaElement() instanceof IMethod method) {
					// TODO find proper occurenceCount (eg checking the source range)
					return method.getType("", 1);
				} else if (getDeclaringClass() != null && getDeclaringClass().getJavaElement() instanceof IType type) {
					return type.getType("", 1);
				}
			}
			
			JavaFileObject jfo = classSymbol == null ? null : classSymbol.sourcefile;
			ICompilationUnit tmp = jfo == null ? null : getCompilationUnit(jfo.getName().toCharArray(), this.resolver.getWorkingCopyOwner());
			if( tmp != null ) {
				String[] cleaned = cleanedUpName(classSymbol).split("\\$");
				if( cleaned.length > 0 ) {
					cleaned[0] = cleaned[0].substring(cleaned[0].lastIndexOf('.') + 1);
				}
				IType ret = null;
				boolean done = false;
				for( int i = 0; i < cleaned.length && !done; i++ ) {
					ret = (ret == null ? tmp.getType(cleaned[i]) : ret.getType(cleaned[i]));
					if( ret == null )
						done = true;
				}
				if( ret != null ) 
					return ret;
			} 
			try {
				IType ret = this.resolver.javaProject.findType(cleanedUpName(classSymbol), this.resolver.getWorkingCopyOwner(), new NullProgressMonitor());
				return ret;
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return null;
	}

	private static ICompilationUnit getCompilationUnit(char[] fileName, WorkingCopyOwner workingCopyOwner) {
		char[] slashSeparatedFileName = CharOperation.replaceOnCopy(fileName, File.separatorChar, '/');
		int pkgEnd = CharOperation.lastIndexOf('/', slashSeparatedFileName); // pkgEnd is exclusive
		if (pkgEnd == -1)
			return null;
		IPackageFragment pkg = Util.getPackageFragment(slashSeparatedFileName, pkgEnd, -1/*no jar separator for .java files*/);
		if (pkg != null) {
			int start;
			ICompilationUnit cu = pkg.getCompilationUnit(new String(slashSeparatedFileName, start =  pkgEnd+1, slashSeparatedFileName.length - start));
			if (workingCopyOwner != null) {
				ICompilationUnit workingCopy = cu.findWorkingCopy(workingCopyOwner);
				if (workingCopy != null)
					return workingCopy;
			}
			return cu;
		}
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = wsRoot.getFile(new Path(String.valueOf(fileName)));
		if (file.exists()) {
			// this approach works if file exists but is not on the project's build path:
			return JavaCore.createCompilationUnitFrom(file);
		}
		return null;
	}

	private static String cleanedUpName(ClassSymbol classSymbol) {
		if (classSymbol.getEnclosingElement() instanceof ClassSymbol enclosing) {
			String fullClassName = classSymbol.className();
			String lastSegment = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
			return cleanedUpName(enclosing) + "$" + lastSegment;
		}
		return classSymbol.className();
	}

	@Override
	public String getKey() {
		return getKey(this.type, this.typeSymbol.flatName());
	}
	public String getKey(Type t) {
		return getKey(t, this.typeSymbol.flatName());
	}
	public String getKey(Type t, Name n) {
		StringBuilder builder = new StringBuilder();
		getKey(builder, t, n, false);
		return builder.toString();
	}

	static void getKey(StringBuilder builder, Type typeToBuild, boolean isLeaf) {
		getKey(builder, typeToBuild, typeToBuild.asElement().flatName(), isLeaf);
	}

	static void getKey(StringBuilder builder, Type typeToBuild, Name n, boolean isLeaf) {
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
			builder.append(n.toString().replace('.', '/'));
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
		if (this.type instanceof WildcardType wildcardType && !wildcardType.isUnbound()) {
			Type bound = wildcardType.getExtendsBound();
			if (bound == null) {
				bound = wildcardType.getSuperBound();
			}
			if (bound != null) {
				return this.resolver.bindings.getTypeBinding(bound);
			}
			ITypeBinding[] boundsArray = this.getTypeBounds();
			if (boundsArray.length == 1) {
				return boundsArray[0];
			}
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
			.map(sym -> {
				Type.MethodType methodType = this.types.memberType(this.type, sym).asMethodType();
				return this.resolver.bindings.getMethodBinding(methodType, sym);
			})
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
				if (method.type instanceof Type.MethodType methodType) {
					return this.resolver.bindings.getMethodBinding(methodType, method);
				}
				if( method.type instanceof Type.ForAll faType && faType.qtype instanceof MethodType mtt) {
					IMethodBinding found = this.resolver.bindings.getMethodBinding(mtt, method);
					return found;
				}
				return null;
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
		return this.resolver.bindings.getTypeBinding(this.types.erasureRecursive(this.type));
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
		StringBuilder builder = new StringBuilder(this.typeSymbol.getSimpleName().toString());
		if (this.getTypeArguments().length > 0) {
			builder.append("<");
			for (var typeArgument : this.getTypeArguments()) {
				builder.append(typeArgument.getName());
			}
			builder.append(">");
		}
		return builder.toString();
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
		return getQualifiedNameImpl(this.type, this.typeSymbol, this.typeSymbol.owner);
	}
	private String getQualifiedNameImpl(Type type, TypeSymbol typeSymbol, Symbol owner) {
		if (owner instanceof MethodSymbol) {
			return "";
		}

		if (owner instanceof MethodSymbol) {
			return "";
		}
		if (type instanceof NullType) {
			return "null";
		}
		if (type instanceof ArrayType at) {
			if( type.tsym.isAnonymous()) {
				return "";
			}
			return this.resolver.bindings.getTypeBinding(at.getComponentType()).getQualifiedName() + "[]";
		}
		if (type instanceof WildcardType wt) {
			if (wt.type == null || this.resolver.resolveWellKnownType("java.lang.Object").equals(this.resolver.bindings.getTypeBinding(wt.type))) {
				return "?";
			}
			StringBuilder builder = new StringBuilder("? ");
			if (wt.isExtendsBound()) {
				builder.append("extends ");
			} else if (wt.isSuperBound()) {
				builder.append("super ");
			}
			builder.append(this.resolver.bindings.getTypeBinding(wt.type).getQualifiedName());
			return builder.toString();
		}

		if( this.isAnonymous()) {
			return "";
		}
		StringBuilder res = new StringBuilder();
		if( owner instanceof RootPackageSymbol ) {
			return type == null || type.tsym == null || type.tsym.name == null ? "" : type.tsym.name.toString();
		} else if( owner instanceof TypeSymbol tss) {
			Type parentType = (type instanceof ClassType ct && ct.getEnclosingType() != Type.noType ? ct.getEnclosingType() : tss.type);
			String parentName = getQualifiedNameImpl(parentType, tss, tss.owner);
			res.append(parentName);
			if( !"".equals(parentName)) {
				res.append(".");
			}
			res.append(typeSymbol.name.toString());
		} else {
			res.append(typeSymbol.toString());
		}
		ITypeBinding[] typeArguments = getUncheckedTypeArguments(type, typeSymbol);
		if (typeArguments.length > 0) {
			res.append("<");
			int i;
			for (i = 0; i < typeArguments.length - 1; i++) {
				res.append(typeArguments[i].getQualifiedName());
				res.append(",");
			}
			res.append(typeArguments[i].getQualifiedName());
			res.append(">");
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
			if( this.isInterface() && superType.toString().equals("java.lang.Object")) {
				return null;
			}
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
		return getTypeArguments(this.type, this.typeSymbol);
	}
	
	private ITypeBinding[] getTypeArguments(Type t, TypeSymbol ts) {
		if (t.getTypeArguments().isEmpty() || t == ts.type || isTargettingPreGenerics()) {
			return NO_TYPE_ARGUMENTS;
		}
		return getUncheckedTypeArguments(t, ts);
	}
	private ITypeBinding[] getUncheckedTypeArguments(Type t, TypeSymbol ts) {
		return t.getTypeArguments()
				.stream()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new);
	}

	private boolean isTargettingPreGenerics() {
		if (this.resolver.javaProject == null) {
			return false;
		}
		String target = this.resolver.javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true);
		return JavaCore.VERSION_1_1.equals(target)
				|| JavaCore.VERSION_CLDC_1_1.equals(target)
				|| JavaCore.VERSION_1_2.equals(target)
				|| JavaCore.VERSION_1_3.equals(target)
				|| JavaCore.VERSION_1_4.equals(target);
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
		} else if (this.type instanceof TypeVar typeVar) {
			Type bounds = typeVar.getUpperBound();
			if (bounds instanceof IntersectionClassType intersectionType) {
				return intersectionType.getBounds().stream() //
						.filter(Type.class::isInstance) //
						.map(Type.class::cast) //
						.map(this.resolver.bindings::getTypeBinding) //
						.toArray(ITypeBinding[]::new);
			}
			return new ITypeBinding[] { this.resolver.bindings.getTypeBinding(bounds) };
		} else if (this.type instanceof WildcardType wildcardType) {
			return new ITypeBinding[] { wildcardType.isUnbound() || wildcardType.isSuperBound() ?
					this.resolver.resolveWellKnownType(Object.class.getName()) :
					this.resolver.bindings.getTypeBinding(wildcardType.bound) };
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
		return !isRawType() && this.type instanceof ClassType classType
			? classType.getTypeArguments()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new)
			: new ITypeBinding[0];
	}

	@Override
	public ITypeBinding getWildcard() {
		if (this.type instanceof Type.CapturedType capturedType) {
			return this.resolver.bindings.getTypeBinding(capturedType.wildcard);
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
			return this.types.isAssignable(this.type, other.type);
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
				(getDeclaringClass() != null && getDeclaringClass().isFromSource()) ||
				this.isCapture();
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
		if (this.isTypeVariable()) {
			return false;
		}
		return getDeclaringClass() != null;
	}

	@Override
	public boolean isNullType() {
		return this.type instanceof NullType || (this.type instanceof ErrorType et && et.getOriginalType() instanceof NullType);
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

	@Override
	public IModuleBinding getModule() {
		Symbol o = this.type.tsym.owner;
		if( o instanceof PackageSymbol ps) {
			return this.resolver.bindings.getModuleBinding(ps.modle);
		}
		return null;
	}

	public void setRecovered(boolean recovered) {
		this.recovered = recovered;
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
