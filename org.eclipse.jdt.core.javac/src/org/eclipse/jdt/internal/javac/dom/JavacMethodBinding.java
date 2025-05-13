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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.JavacBindingResolver.BindingKeyException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.internal.core.BinaryMethod;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.Member;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.util.Util;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.util.ListBuffer;

public abstract class JavacMethodBinding implements IMethodBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];
	private static final ITypeBinding[] NO_TYPE_PARAMS = new ITypeBinding[0];

	public final MethodSymbol methodSymbol;
	final MethodType methodType;
	// allows to better identify parameterized method
	final Type parentType;
	final JavacBindingResolver resolver;
	final boolean explicitSynthetic;
	// allows to discriminate generic vs parameterized
	private final boolean isDeclaration;
	private IMethod javaElement;
	private String key;

	/**
	 *
	 * @param methodType
	 * @param methodSymbol
	 * @param parentType can be null, in which case <code>methodSymbol.owner.type</code> will be used instead
	 * @param resolver
	 */
	public JavacMethodBinding(MethodType methodType, MethodSymbol methodSymbol, Type parentType, JavacBindingResolver resolver) {
		this(methodType, methodSymbol, parentType, resolver, false, false);
	}

	public JavacMethodBinding(MethodType methodType, MethodSymbol methodSymbol, Type parentType, JavacBindingResolver resolver, boolean explicitSynthetic, boolean isDeclaration) {
		this.methodType = methodType;
		this.methodSymbol = methodSymbol;
		this.parentType = parentType == null && methodSymbol != null && methodSymbol.owner instanceof ClassSymbol classSymbol && JavacBindingResolver.isTypeOfType(classSymbol.type) ?
				classSymbol.type : parentType;
		this.isDeclaration = isParameterized(methodSymbol) && isDeclaration;
		this.explicitSynthetic = explicitSynthetic;
		this.resolver = resolver;
	}

	private static boolean isParameterized(Symbol symbol) {
		while (symbol != null) {
			if (symbol.type != null &&
				(symbol.type.isParameterized() || symbol.type instanceof ForAll)) {
				return true;
			}
			symbol = symbol.owner;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacMethodBinding other
				&& Objects.equals(this.resolver, other.resolver)
				&& Objects.equals(this.methodSymbol, other.methodSymbol)
				&& equals(this.methodType, other.methodType) // workaround non-uniqueness MethodType and missing equals/hashCode (ASTConverter15JLS8Test.test0214)
				&& Objects.equals(this.explicitSynthetic, other.explicitSynthetic)
				&& Objects.equals(this.parentType, other.parentType)
				&& Objects.equals(this.isDeclaration, other.isDeclaration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.resolver, this.methodSymbol, this.parentType, this.explicitSynthetic, this.isDeclaration) ^ hashCode(this.methodType);
	}

	private static boolean equals(MethodType second, MethodType first) {
		return second == first ||
				(Objects.equals(first.argtypes, second.argtypes) &&
				Objects.equals(first.restype, second.restype) &&
				Objects.equals(first.thrown, second.thrown) &&
				Objects.equals(first.recvtype, second.recvtype) &&
				Objects.equals(first.tsym, second.tsym));
	}
	private static int hashCode(MethodType methodType) {
		return Objects.hash(methodType.tsym, methodType.argtypes, methodType.restype, methodType.thrown, methodType.recvtype);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		if( methodSymbol == null ) {
			return new IAnnotationBinding[0];
		}
		return methodSymbol.getAnnotationMirrors().stream().map(ann -> this.resolver.bindings.getAnnotationBinding(ann, this)).toArray(IAnnotationBinding[]::new);
	}

	@Override
	public int getKind() {
		return METHOD;
	}

	@Override
	public int getModifiers() {
		var outerClass = getDeclaringClass();
		int extraModifiers = outerClass != null &&
				outerClass.isInterface() &&
				this.methodSymbol != null &&
				!this.methodSymbol.isDefault() &&
				!this.methodSymbol.isStatic() ? Modifier.ABSTRACT : 0;
		return this.methodSymbol != null ? toInt(this.methodSymbol.getModifiers()) | extraModifiers : extraModifiers;
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
		return this.methodSymbol != null ? this.methodSymbol.isDeprecated() : false;
	}

	@Override
	public boolean isRecovered() {
		return this.methodSymbol == null ? true : this.methodSymbol.kind == Kinds.Kind.ERR;
	}

	@Override
	public boolean isSynthetic() {
		return this.methodSymbol != null && (this.methodSymbol.flags() & Flags.SYNTHETIC) != 0;
	}

	@Override
	public IMethod getJavaElement() {
		this.javaElement = resolved(getUnresolvedJavaElement());
		return this.javaElement;
	}

	IMethod getUnresolvedJavaElement() {
		if (this.javaElement == null) {
			this.javaElement = computeUnresolvedJavaElement();
		}
		return this.javaElement;
	}

	private IMethod computeUnresolvedJavaElement() {
		Object possibleTypeBinding = null;
		if (this.methodSymbol == null) {
			possibleTypeBinding = this.resolver.bindings.getBinding(this.methodType.restype.tsym, this.methodType.restype);
		} else {
			possibleTypeBinding = this.resolver.bindings.getBinding(this.methodSymbol.owner, this.methodType);
		}
		if (possibleTypeBinding instanceof ITypeBinding typeBinding) {
			if (typeBinding != null && typeBinding.getJavaElement() instanceof IType currentType) {
				// prefer DOM object (for type parameters)
				ASTNode declaringNode = this.resolver.findDeclaringNode(this);
				if (declaringNode instanceof MethodDeclaration methodDeclaration) {
					return getJavaElementForMethodDeclaration(currentType, methodDeclaration);
				} else if (declaringNode instanceof AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
					return getJavaElementForAnnotationTypeMemberDeclaration(currentType, annotationTypeMemberDeclaration);
				}

				String[] parametersResolved = new String[0];
				if( this.methodSymbol != null ) {
					parametersResolved = this.methodSymbol.params().stream()
						.map(varSymbol -> varSymbol.type)
						.map(t ->
							t instanceof TypeVar typeVar ? Signature.C_TYPE_VARIABLE + typeVar.tsym.name.toString() + ";" : // check whether a better constructor exists for it
								Signature.createTypeSignature(resolveTypeName(t, true), true))
						.toArray(String[]::new);
				} else {
					parametersResolved = this.methodType.argtypes.stream()
							.map(t ->
								t instanceof TypeVar typeVar ? Signature.C_TYPE_VARIABLE + typeVar.tsym.name.toString() + ";" : // check whether a better constructor exists for it
									Signature.createTypeSignature(resolveTypeName(t, true), true))
							.toArray(String[]::new);
				}

				IMethod[] methods = currentType.findMethods(currentType.getMethod(getName(), parametersResolved));
				if (methods != null && methods.length > 0) {
					return methods[0];
				}
				String[] parametersNotResolved = new String[0];
				if( this.methodSymbol != null ) {
						parametersNotResolved = this.methodSymbol.params().stream()
						.map(varSymbol -> varSymbol.type)
						.map(t ->
							t instanceof TypeVar typeVar ? Signature.C_TYPE_VARIABLE + typeVar.tsym.name.toString() + ";" : // check whether a better constructor exists for it
								Signature.createTypeSignature(resolveTypeName(t, false), false))
						.toArray(String[]::new);
				}
				methods = currentType.findMethods(currentType.getMethod(getName(), parametersNotResolved));
				if (methods != null && methods.length > 0) {
					return methods[0];
				}
			}
		}
		return null;
	}

	private IMethod getJavaElementForMethodDeclaration(IType currentType, MethodDeclaration methodDeclaration) {
		ArrayList<String> typeParamsList = new ArrayList<>();
		List<TypeParameter> typeParams = null;
		typeParams = methodDeclaration.typeParameters();
		if( typeParams == null ) {
			typeParams = new ArrayList<>();
		}
		for( int i = 0; i < typeParams.size(); i++ ) {
			typeParamsList.add(typeParams.get(i).getName().toString());
		}

		List<SingleVariableDeclaration> p = methodDeclaration.parameters();
		String[] params = p.stream() //
				.map(param -> {
					String sig = Util.getSignature(param.getType());
					if (param.isVarargs()) {
						sig = Signature.createArraySignature(sig, 1);
					}
					return sig;
				}).toArray(String[]::new);
		IMethod result = currentType.getMethod(getName(), params);
		if (currentType.isBinary() || result.exists()) {
			return result;
		}
		IMethod[] methods = null;
		try {
			methods = currentType.getMethods();
		} catch (JavaModelException e) {
			// declaring type doesn't exist
			return null;
		}
		IMethod[] candidates = Member.findMethods(result, methods);
		if (candidates == null || candidates.length == 0)
			return null;
		return candidates[0];
	}

	private IMethod resolved(IMethod from) {
		if (from == null) {
			return null;
		}
		if (from.isResolved()) {
			return from;
		}
		if (from instanceof SourceMethod && !(from instanceof ResolvedSourceMethod)) {
			return new ResolvedSourceMethod((JavaElement)from.getParent(), from.getElementName(), from.getParameterTypes(), computeKeyWithThrowsFromJavadoc(from), from.getOccurrenceCount());
		}
		if (from instanceof BinaryMethod && !(from instanceof ResolvedBinaryMethod)) {
			return new ResolvedBinaryMethod((JavaElement)from.getParent(), from.getElementName(), from.getParameterTypes(), computeKeyWithThrowsFromJavadoc(from), from.getOccurrenceCount());
		}
		return from;
	}

	/**
	 * Computes the key including throws from Javadoc (which are available
	 * from the {@link IMethod} but not from the binding).
	 * @param method the {@link IMethod} matching current binding
	 * @return the key including the exception suffix
	 */
	private String computeKeyWithThrowsFromJavadoc(IMethod method) {
		String[] exceptions = null;
		try {
			exceptions = method.getExceptionTypes();
		} catch (JavaModelException ex) {
		}
		if (exceptions == null || exceptions.length == 0) {
			exceptions = Arrays.stream(getExceptionTypes()).map(ITypeBinding::getKey).toArray(String[]::new);
		}
		String exceptionsAsString = Arrays.stream(exceptions).map(t -> '|' + t.replace('.', '/')).collect(Collectors.joining());
		return getKey() + exceptionsAsString;
	}

	private IMethod getJavaElementForAnnotationTypeMemberDeclaration(IType currentType, AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
		IMethod result = currentType.getMethod(getName(), new String[0]);
		if (currentType.isBinary() || result.exists()) {
			return result;
		}
		IMethod[] methods = null;
		try {
			methods = currentType.getMethods();
		} catch (JavaModelException e) {
			// declaring type doesn't exist
			return null;
		}
		IMethod[] candidates = Member.findMethods(result, methods);
		if (candidates == null || candidates.length == 0)
			return null;
		return candidates[0];
	}

	private String resolveTypeName(com.sun.tools.javac.code.Type type, boolean binary) {
		if (binary) {
			if (type instanceof com.sun.tools.javac.code.Type.ArrayType arrayType) {
				return resolveTypeName(arrayType.elemtype, binary) + "[]";
			}
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
		if (this.key == null) {
			this.key = computeKey();
		}
		return this.key;
	}

	private String computeKey() {
		try {
			StringBuilder builder = new StringBuilder();
			getKey(builder, this.methodSymbol, this.methodType, this.parentType, this.resolver);
			return builder.toString();
		} catch(BindingKeyException bke) {
			return null;
		}
	}

	static void getKey(StringBuilder builder, MethodSymbol methodSymbol, MethodType methodType, Type parentType, JavacBindingResolver resolver) throws BindingKeyException {
		getKey(builder, methodSymbol, methodType, parentType, false, resolver);
	}

	static void getKey(StringBuilder builder, MethodSymbol methodSymbol, MethodType methodType, Type parentType, boolean useSlashes, JavacBindingResolver resolver) throws BindingKeyException {

		if (parentType != null) {
			JavacTypeBinding parentBinding = resolver.bindings.getTypeBinding(parentType);
			String parentKey = parentBinding.getKey(true, true);
			builder.append(parentKey);
		} else {
			Symbol ownerSymbol = methodSymbol.owner;
			while (ownerSymbol != null && !(ownerSymbol instanceof TypeSymbol)) {
				ownerSymbol = ownerSymbol.owner;
			}
			if (ownerSymbol instanceof TypeSymbol ownerTypeSymbol && ownerTypeSymbol.type != Type.noType) {
				JavacTypeBinding.getKey(builder, resolver.getTypes().erasure(ownerTypeSymbol.type), false, false, useSlashes, resolver);
			} else {
				throw new BindingKeyException(new IllegalArgumentException("Method has no owning class"));
			}
		}

		boolean appendMethodName = !methodSymbol.isConstructor() && methodSymbol.getSimpleName().toString().length() != 0;
		boolean methodSymbolNonNullType = methodSymbol.type != null;
		if( appendMethodName || methodSymbolNonNullType) {
			builder.append(".");
		}
		if (appendMethodName) {
			builder.append(methodSymbol.getSimpleName());
		}
		if (methodSymbolNonNullType) { // initializer
			if (methodType != null && !methodType.getTypeArguments().isEmpty()) {
				builder.append('<');
				for (var typeParam : methodType.getTypeArguments()) {
					JavacTypeBinding.getKey(builder, typeParam, false, true, useSlashes, resolver);
				}
				builder.append('>');
			} else if (!methodSymbol.getTypeParameters().isEmpty()) {
				builder.append('<');
				for (var typeParam : methodSymbol.getTypeParameters()) {
					builder.append(JavacTypeVariableBinding.getTypeVariableKey(typeParam, resolver));
				}
				builder.append('>');
			}
			builder.append('(');
			if (methodType != null) {
				for (var param : methodType.getParameterTypes()) {
					JavacTypeBinding.getKey(builder, param, false, false, true, resolver);
				}
			} else {
				for (var param : methodSymbol.getParameters()) {
					JavacTypeBinding.getKey(builder, param.type, false, false, true, resolver);
				}
			}
			builder.append(')');
			if (methodType != null && !(methodType.getReturnType() instanceof JCNoType)) {
				JavacTypeBinding.getKey(builder, methodType.getReturnType(), false, true, true, resolver);
			} else if (!(methodSymbol.getReturnType() instanceof JCNoType)) {
				JavacTypeBinding.getKey(builder, methodSymbol.getReturnType(), false, true, true, resolver);
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
		return binding instanceof IMethodBinding other && //
			Objects.equals(this.getKey(), other.getKey());
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
		if (isConstructor()) {
			ITypeBinding tb = getDeclaringClass();
			if( tb instanceof JavacTypeBinding tb2) {
				return tb2.getName(false);
			}
			return tb.getName();
		}
		return this.methodSymbol.getSimpleName().toString();
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		if (this.parentType != null) {
			return this.resolver.bindings.getTypeBinding(this.parentType, isDeclaration);
		}
		if (this.methodSymbol != null && this.methodSymbol.owner instanceof ClassSymbol clazz) {
			return this.resolver.bindings.getTypeBinding(clazz.type, isDeclaration);
		}
		return null;
	}

	@Override
	public IBinding getDeclaringMember() {
		return null; // overriden in JavacLambdaBinding
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
		ITypeBinding[] res = new ITypeBinding[this.methodType.getParameterTypes().length()];
		for (int i = 0; i < res.length; i++) {
			Type paramType = methodType.getParameterTypes().get(i);
			ITypeBinding paramBinding = this.resolver.bindings.getTypeBinding(paramType);
			if (paramBinding == null) {
				// workaround javac missing recovery symbols for unresolved parameterized types
				if (this.resolver.findDeclaringNode(this) instanceof MethodDeclaration methodDecl) {
					if (methodDecl.parameters().get(i) instanceof SingleVariableDeclaration paramDeclaration) {
						paramBinding = this.resolver.resolveType(paramDeclaration.getType());
					}
				}
			}
			res[i] = paramBinding;
		}
		return res;
	}

	@Override
	public ITypeBinding getDeclaredReceiverType() {
		return this.resolver.bindings.getTypeBinding(this.methodType.getReceiverType());
	}

	@Override
	public ITypeBinding getReturnType() {
		return this.resolver.bindings.getTypeBinding(this.methodType.getReturnType(), false);
	}

	@Override
	public ITypeBinding[] getExceptionTypes() {
		return this.methodType.getThrownTypes().stream() //
				.map(this.resolver.bindings::getTypeBinding) //
				.toArray(ITypeBinding[]::new);
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		if (this.getTypeArguments().length != 0) {
			return NO_TYPE_PARAMS;
		}
		if( this.methodSymbol != null ) {
			return this.methodSymbol.getTypeParameters().stream()
					.map(symbol -> this.resolver.bindings.getTypeBinding(symbol.type))
					.toArray(ITypeBinding[]::new);
		}
		// We have no methodSymbol, most likely an error node
		return new ITypeBinding[0];
	}

	@Override
	public boolean isAnnotationMember() {
		return getDeclaringClass().isAnnotation();
	}

	@Override
	public boolean isGenericMethod() {
		if( methodHasGenerics() ) {
			return true;
		}
		// instead of the methodType, get a less typed Type and check if it is a ForAll
		if( this.methodSymbol != null && this.methodSymbol.type instanceof ForAll) {
			return !methodMatchesParameterized();
		}
		return false;
	}

	private boolean methodHasGenerics() {
		boolean b1 = (isConstructor() && getDeclaringClass().isGenericType())
				|| (this.methodSymbol != null && !this.methodSymbol.getTypeParameters().isEmpty() && isDeclaration);
		return b1;
	}

	@Override
	public boolean isParameterizedMethod() {
		return !isRawMethod() && !methodHasGenerics() && methodMatchesParameterized();
	}

	private boolean methodMatchesParameterized() {
		return ((isConstructor() && getDeclaringClass().isParameterizedType())
				|| (this.methodSymbol != null && !this.methodSymbol.getTypeParameters().isEmpty() && !isDeclaration));
	}

	@Override
	public boolean isRawMethod() {
		if (isConstructor()) {
			return getDeclaringClass().isRawType() && this.methodSymbol.getTypeParameters().isEmpty();
		}
		boolean parentNullOrRaw = (this.parentType != null && this.parentType.isRaw());
		List<TypeVariableSymbol> tvs = this.methodSymbol.getTypeParameters();
		return parentNullOrRaw || tvs.isEmpty();
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		// methodType.getTypeArguments() is always null
		// we must compute the arguments ourselves by computing a mapping from the method with type variables
		// to the specific instance that potentially has the type variables substituted for real types
		Map<Type, Type> typeMap = new HashMap<>();
		Type methodSymbolType = null;
		if( this.methodSymbol != null ) {
			methodSymbolType = this.methodSymbol.type;
		} else {
			methodSymbolType = methodType;
		}

		// scrape the parameters
		List<Type> paramTypes = methodSymbolType.getParameterTypes();
		for (int i = 0; i < paramTypes.size(); i++) {
			ListBuffer<Type> originalTypes = new ListBuffer<>();
			ListBuffer<Type> substitutedTypes = new ListBuffer<>();
			this.resolver.getTypes().adapt(
					paramTypes.get(i),
					methodType.getParameterTypes().get(i), originalTypes, substitutedTypes);
			List<Type> originalTypesList = originalTypes.toList();
			List<Type> substitutedTypesList = substitutedTypes.toList();
			for (int j = 0; j < originalTypesList.size(); j++) {
				typeMap.putIfAbsent(originalTypesList.get(j), substitutedTypesList.get(j));
			}
		}
		{
			// also scrape the return type
			ListBuffer<Type> originalTypes = new ListBuffer<>();
			ListBuffer<Type> substitutedTypes = new ListBuffer<>();
			this.resolver.getTypes().adapt(methodSymbolType.getReturnType(), methodType.getReturnType(), originalTypes, substitutedTypes);
			List<Type> originalTypesList = originalTypes.toList();
			List<Type> substitutedTypesList = substitutedTypes.toList();
			for (int j = 0; j < originalTypesList.size(); j++) {
				typeMap.putIfAbsent(originalTypesList.get(j), substitutedTypesList.get(j));
			}
		}

		boolean allEqual = true;
		for (Map.Entry<Type, Type> entry : typeMap.entrySet()) {
			if (!entry.getKey().equals(entry.getValue())) {
				allEqual = false;
			}
			if (entry.getValue() == null) {
				return NO_TYPE_ARGUMENTS;
			}
		}
		if (allEqual) {
			// methodType also contains all the type variables,
			// which means it's also generic and no type arguments have been applied.
			return NO_TYPE_ARGUMENTS;
		}

		if( this.methodSymbol != null ) {
			return this.methodSymbol.getTypeParameters().stream() //
					.map(tvSym -> typeMap.get(tvSym.type)) //
					.map(this.resolver.bindings::getTypeBinding) //
					.toArray(ITypeBinding[]::new);
		}
		return NO_TYPE_ARGUMENTS;
	}

	@Override
	public IMethodBinding getMethodDeclaration() {
		// This method intentionally converts the type to its generic type,
		// i.e. drops the type arguments
		// i.e. <code>this.<String>getValue(12);</code> will be converted back to <code><T> T getValue(int i) {</code>
		MethodType mt = (this.methodSymbol == null ? this.methodType : this.methodSymbol.type.asMethodType());
		return this.resolver.bindings.getMethodBinding(mt, methodSymbol, null, true);
	}

	@Override
	public boolean isSubsignature(IMethodBinding otherMethod) {
		if (otherMethod instanceof JavacMethodBinding otherJavacMethod) {
			return resolver.getTypes().isSubSignature(this.methodType, otherJavacMethod.methodType);
		}
		return false;
	}

	@Override
	public boolean isVarargs() {
		return this.methodSymbol.isVarArgs();
	}

	@Override
	public boolean overrides(IMethodBinding method) {
		if (this == method) {
			return false;
		}
		if (method instanceof JavacMethodBinding javacMethod) {
			if( this.methodSymbol == null ) {
				return javacMethod.methodSymbol == null;
			}

			return Objects.equals(this.methodSymbol.name, javacMethod.methodSymbol.name)
				&& this.methodSymbol.overrides(((JavacMethodBinding)method).methodSymbol, javacMethod.methodSymbol.enclClass(), this.resolver.getTypes(), true);
		}
		return false;
	}

	@Override
	public IVariableBinding[] getSyntheticOuterLocals() {
		if( this.methodSymbol == null ) {
			return new IVariableBinding[0];
		}

		if (!this.methodSymbol.isLambdaMethod()) {
			return new IVariableBinding[0];
		}
		return this.methodSymbol.capturedLocals.stream() //
				.map(varSymbol -> this.resolver.bindings.getVariableBinding(varSymbol))
				.toArray(IVariableBinding[]::new);
	}

	@Override
	public boolean isSyntheticRecordMethod() {
		return this.explicitSynthetic || (!this.methodSymbol.isStatic()
				&& !isConstructor()
				&& (this.methodSymbol != null && (this.methodSymbol.flags() & (Flags.GENERATED_MEMBER | Flags.SYNTHETIC | Flags.RECORD)) != 0)
				&& getDeclaringClass().isRecord());
	}

	@Override
	public String[] getParameterNames() {
		if (this.methodSymbol == null || this.methodSymbol.getParameters() == null) {
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
