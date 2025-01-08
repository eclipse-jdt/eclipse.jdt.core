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
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.JavacBindingResolver.BindingKeyException;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.core.SourceType;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Kinds.Kind;
import com.sun.tools.javac.code.Kinds.KindSelector;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.RootPackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.IntersectionClassType;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.JCVoidType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types.FunctionDescriptorLookupError;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public abstract class JavacTypeBinding implements ITypeBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];

	final JavacBindingResolver resolver;
	public final TypeSymbol typeSymbol;
	private final Types types;
	private final Names names;
	public final Type type;
	private final boolean isGeneric; // only relevent for parameterized types
	private boolean recovered = false;

	public JavacTypeBinding(Type type, final TypeSymbol typeSymbol, boolean isDeclaration, JavacBindingResolver resolver) {
		if (!JavacBindingResolver.isTypeOfType(type)) {
			if (typeSymbol != null) {
				type = typeSymbol.type;
			}
		}
		this.isGeneric = type.isParameterized() && isDeclaration;
		this.typeSymbol = typeSymbol.kind == Kind.ERR && type != null? type.tsym : typeSymbol;
		this.type = this.isGeneric || type == null ? this.typeSymbol.type /*generic*/ : type /*specific instance*/;
		this.resolver = resolver;
		this.types = Types.instance(this.resolver.context);
		this.names = Names.instance(this.resolver.context);
		// TODO: consider getting rid of typeSymbol in constructor and always derive it from type
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacTypeBinding other
				&& Objects.equals(this.resolver, other.resolver)
				&& Objects.equals(this.type, other.type)
				&& Objects.equals(this.typeSymbol, other.typeSymbol)
				&& Objects.equals(this.isGeneric, other.isGeneric);
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.resolver, this.type, this.typeSymbol, this.isGeneric);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		List<Attribute.Compound> annots = this.typeSymbol.getAnnotationMirrors();
		if( this.resolver.isRecoveringBindings()) {
			return annots.stream()
					.map(am -> this.resolver.bindings.getAnnotationBinding(am, this))
					.toArray(IAnnotationBinding[]::new);
		} else {
			return annots.stream().filter(x -> !(x.type instanceof ErrorType))
					.map(am -> this.resolver.bindings.getAnnotationBinding(am, this))
					.toArray(IAnnotationBinding[]::new);
		}
		
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
					&& ownerSymbol.type != null) {
				if(this.resolver.bindings.getTypeBinding(ownerSymbol.type).getJavaElement() instanceof IType ownerType
						&& ownerType.getTypeParameter(this.getName()) != null) {
					return ownerType.getTypeParameter(this.getName());
				}
			} else if (this.typeSymbol.owner instanceof MethodSymbol ownerSymbol
					&& ownerSymbol.type != null ) {
				JavacMethodBinding mb = this.resolver.bindings.getMethodBinding(ownerSymbol.type.asMethodType(), ownerSymbol, null, isGeneric); 
				if( mb.getJavaElement() instanceof IMethod ownerMethod
						&& ownerMethod.getTypeParameter(this.getName()) != null) {
					return ownerMethod.getTypeParameter(this.getName());
				}
			}	
		}
		if (this.resolver.javaProject == null) {
			return null;
		}
		if (this.isArray()) {
			return this.getElementType().getJavaElement();
		}
		if (this.typeSymbol instanceof final ClassSymbol classSymbol) {
			if (isAnonymous()) {
				if (getDeclaringMethod() != null && getDeclaringMethod().getJavaElement() instanceof IMethod method) {
					// TODO find proper occurenceCount (eg checking the source range)
					return resolved(method.getType("", 1));
				} else if( getDeclaringMember() instanceof IBinding gdm && gdm != null && gdm.getJavaElement() instanceof IField field) {
					return resolved(field.getType("", 1));
				} else if (getDeclaringClass() != null && getDeclaringClass().getJavaElement() instanceof IType type) {
					return resolved(type.getType("", 1));
				}
			}
			if( this.typeSymbol.owner instanceof MethodSymbol) {
				if (getDeclaringMethod() != null && getDeclaringMethod().getJavaElement() instanceof IMethod method) {
					// TODO find proper occurenceCount (eg checking the source range)
					return resolved(method.getType(this.typeSymbol.name.toString(), 1));
				}				
			}
			
			JavaFileObject jfo = classSymbol == null ? null : classSymbol.sourcefile;
			ITypeRoot typeRoot = null;
			if (jfo != null) {
				var jfoFile = new File(jfo.getName());
				var jfoPath = new Path(jfo.getName());
				Stream<IFile> fileStream = jfoFile.isFile()	?
						Arrays.stream(this.resolver.javaProject.getResource().getWorkspace().getRoot().findFilesForLocationURI(jfoFile.toURI())) :
						jfoPath.segmentCount() > 1 ?
							Stream.of(this.resolver.javaProject.getResource().getWorkspace().getRoot().getFile(jfoPath)) :
							Stream.of();
				typeRoot = fileStream
					.map(JavaCore::create)
					.filter(ITypeRoot.class::isInstance)
					.map(ITypeRoot.class::cast)
					.findAny()
					.orElse(null);
			}
			IType candidate = null;
			if(typeRoot instanceof ICompilationUnit tmp) {
				{
					ICompilationUnit wc = tmp.findWorkingCopy(this.resolver.getWorkingCopyOwner());
					if (wc != null) {
						tmp = wc;
					}
				}
				String[] cleaned = cleanedUpName(this.type).split("\\$");
				if( cleaned.length > 0 ) {
					cleaned[0] = cleaned[0].substring(cleaned[0].lastIndexOf('.') + 1);
				}
				boolean done = false;
				for( int i = 0; i < cleaned.length && !done; i++ ) {
					candidate = (candidate == null ? tmp.getType(cleaned[i]) : candidate.getType(cleaned[i]));
					done |= (candidate == null);
				}
				if(candidate != null && candidate.exists()) {
					return resolved(candidate);
				}
			}
			try {
				IType ret = this.resolver.javaProject.findType(cleanedUpName(this.type), this.resolver.getWorkingCopyOwner(), new NullProgressMonitor());
				if (ret != null) {
					return resolved(ret);
				}
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			return resolved(candidate);
		}
		return null;
	}

	private IType resolved(IType type) {
		if (type instanceof SourceType && !(type instanceof ResolvedSourceType)) {
			return new ResolvedSourceType((JavaElement)type.getParent(), type.getElementName(), getKey(), type.getOccurrenceCount());
		}
		if (type instanceof BinaryType && !(type instanceof ResolvedBinaryType)) {
			return new ResolvedBinaryType((JavaElement)type.getParent(), type.getElementName(), getKey(), type.getOccurrenceCount());
		}
		return type;
	}

	private static String cleanedUpName(Type type) {
		if (type instanceof ClassType classType && classType.getEnclosingType() instanceof ClassType enclosing) {
			return cleanedUpName(enclosing) + "$" + type.tsym.getSimpleName().toString();
		}
		// For static inner types, type.getEnclosingType() returns null, so let's also check owner
		if (type.tsym instanceof ClassSymbol classSymbol && type.tsym.owner instanceof ClassSymbol enclosingSymbol) {
			return enclosingSymbol.getQualifiedName().toString() + '$' + classSymbol.getSimpleName().toString();
		}
		return type.tsym.getQualifiedName().toString();
	}

	@Override
	public String getKey() {
		if (isGenericType()) {
			return removeTrailingSemicolon(getKey(false)) + '<'
				+ Arrays.stream(getTypeParameters())
					.map(ITypeBinding::getName)
					.map(name -> 'T' + name + ';')
					.collect(Collectors.joining())
				+ ">;";
		} else if (isParameterizedType()) {
			return removeTrailingSemicolon(getKey(false)) + '<'
				+ Arrays.stream(getTypeArguments()).map(ITypeBinding::getKey).collect(Collectors.joining())
				+ ">;";
		}
		return getKey(this.type, this.typeSymbol.flatName());
	}


	private static String removeTrailingSemicolon(String key) {
		return key.endsWith(";") ? key.substring(0, key.length() - 1) : key;
	}

	private String getKey(Type t) {
		return getKey(t, this.typeSymbol.flatName());
	}

	public String getKey(boolean includeTypeParameters) {
		return getKey(this.type, this.typeSymbol.flatName(), includeTypeParameters);
	}

	public String getKey(Type t, Name n) {
		return getKey(type, n, true);
	}
	public String getKey(Type t, Name n, boolean includeTypeParameters) {
		try {
			StringBuilder builder = new StringBuilder();
			getKey(builder, t, n, false, includeTypeParameters, this.resolver);
			return builder.toString();
		} catch(BindingKeyException bke) {
			return null;
		}
	}


	static void getKey(StringBuilder builder, Type typeToBuild, boolean isLeaf, JavacBindingResolver resolver) throws BindingKeyException {
		getKey(builder, typeToBuild, typeToBuild.asElement().flatName(), isLeaf, false, resolver);
	}

	static void getKey(StringBuilder builder, Type typeToBuild, boolean isLeaf, boolean includeParameters, JavacBindingResolver resolver) throws BindingKeyException {
		getKey(builder, typeToBuild, typeToBuild.asElement().flatName(), isLeaf, includeParameters, resolver);
	}

	static void getKey(StringBuilder builder, Type typeToBuild, Name n, boolean isLeaf, boolean includeParameters, JavacBindingResolver resolver) throws BindingKeyException {
		if (typeToBuild instanceof Type.JCNoType) {
			return;
		}
		if (typeToBuild instanceof Type.CapturedType capturedType) {
			builder.append('!');
			getKey(builder, capturedType.wildcard, false, includeParameters, resolver);
			// taken from Type.CapturedType.toString()
			builder.append((capturedType.hashCode() & 0xFFFFFFFFL) % 997);
			builder.append(';');
			return;
		}
		if (typeToBuild.hasTag(TypeTag.UNKNOWN)) {
			builder.append('*');
			return;
		}
		if (typeToBuild instanceof ArrayType arrayType) {
			builder.append('[');
			getKey(builder, arrayType.elemtype, isLeaf, includeParameters, resolver);
			return;
		}
		if (typeToBuild instanceof Type.WildcardType wildcardType) {
			if (wildcardType.isUnbound()) {
				builder.append("+Ljava/lang/Object;");
			} else if (wildcardType.isExtendsBound()) {
				builder.append('+');
				getKey(builder, wildcardType.getExtendsBound(), isLeaf, includeParameters, resolver);
			} else if (wildcardType.isSuperBound()) {
				builder.append('-');
				getKey(builder, wildcardType.getSuperBound(), isLeaf, includeParameters, resolver);
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

			/*
			 * TODO - this name 'n' might be something like  test0502.A$1
			 * but the test suite expects test0502.A$182,
			 * where 182 is the location in the source of the symbol.
			 */
			builder.append(n.toString().replace('.', '/'));
			// This is a hack and will likely need to be enhanced
			if (typeToBuild.tsym instanceof ClassSymbol classSymbol && !(classSymbol.type instanceof ErrorType) && classSymbol.owner instanceof PackageSymbol) {
				JavaFileObject sourcefile = classSymbol.sourcefile;
				if (sourcefile != null && sourcefile.getKind() == JavaFileObject.Kind.SOURCE) {
					URI uri = sourcefile.toUri();
					String fileName = null;
					try {
						fileName = Paths.get(uri).getFileName().toString();
					} catch (IllegalArgumentException e) {
						// probably: uri is not a valid path
					}
					if (fileName != null && !fileName.startsWith(classSymbol.getSimpleName().toString())) {
						// There are multiple top-level types in this file,
						// inject 'FileName~' before the type name to show that this type came from `FileName.java`
						// (eg. Lorg/eclipse/jdt/FileName~MyTopLevelType;)
						int simpleNameIndex  = builder.lastIndexOf(classSymbol.getSimpleName().toString());
						builder.insert(simpleNameIndex, fileName.substring(0, fileName.indexOf(".java")) + "~");
					}
				}
			}


			boolean b1 = typeToBuild.isParameterized();
			boolean b2 = false;
			try {
				b2 = typeToBuild.tsym != null && typeToBuild.tsym.type != null && typeToBuild.tsym.type.isParameterized();
			} catch( CompletionFailure cf1) {
				throw new BindingKeyException(cf1);
			}
			if ((b1 || b2) && includeParameters) {
				builder.append('<');
				for (var typeArgument : typeToBuild.getTypeArguments()) {
					getKey(builder, typeArgument, false, includeParameters, resolver);
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
		return binding instanceof final ITypeBinding other &&
			Objects.equals(this.getKey(), other.getKey());
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
		if (this.type.isPrimitive()) {
			// use Javac signature to get correct variable name
			StringBuilder res = new StringBuilder();
			var generator = new Types.SignatureGenerator(this.types) {
				@Override
				protected void append(char ch) {
					res.append(ch);
				}
	
				@Override
				protected void append(byte[] ba) {
					res.append(new String(ba));
				}
	
				@Override
				protected void append(Name name) {
					res.append(name.toString());
				}
			};
			generator.assembleSig(this.type);
			return res.toString();
		}
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
			.filter(sym -> sym.name != this.names.error)
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

		if( this.isRecord()) {
			IMethodBinding[] ret = getDeclaredMethodsForRecords(l);
			if( ret != null ) {
				return ret;
			}
		}
		return getDeclaredMethodsDefaultImpl(l);
	}

	private IMethodBinding[] getDeclaredMethodsDefaultImpl(ArrayList<Symbol> l) {
		return StreamSupport.stream(l.spliterator(), false)
				.filter(MethodSymbol.class::isInstance)
				.map(MethodSymbol.class::cast)
				.map(sym -> {
					Type.MethodType methodType = this.types.memberType(this.type, sym).asMethodType();
					return this.resolver.bindings.getMethodBinding(methodType, sym, this.type, isGeneric);
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(IMethodBinding::getName))
				.toArray(IMethodBinding[]::new);
	}

	private IMethodBinding[] getDeclaredMethodsForRecords(ArrayList<Symbol> l) {
		ASTNode node = this.resolver.symbolToDeclaration.get(this.typeSymbol);
		boolean isRecord = this.isRecord() && node instanceof RecordDeclaration;
		if( !isRecord )
			return null;
		RecordDeclaration rd = (RecordDeclaration)node;
		List<BodyDeclaration> bodies = rd.bodyDeclarations();
		List<String> explicitMethods = bodies.stream()
				.filter(MethodDeclaration.class::isInstance)
				.map(MethodDeclaration.class::cast)
				.filter(Objects::nonNull)
				.map(x -> x.getName().toString())
				.map(String.class::cast)
				.collect(Collectors.toList());
		explicitMethods.add("<init>");
		// TODO this list is very basic, only method names. Need more usecases to do it better

		//ArrayList<String> explicitRecordMethods = node.bodyDeclarations();
		return StreamSupport.stream(l.spliterator(), false)
			.filter(MethodSymbol.class::isInstance)
			.map(MethodSymbol.class::cast)
			.map(sym -> {
				String symName = sym.name.toString();
				boolean isSynthetic = !explicitMethods.contains(symName);
				Type.MethodType methodType = this.types.memberType(this.type, sym).asMethodType();
				return this.resolver.bindings.getMethodBinding(methodType, sym, this.type, isSynthetic);
			})
			.filter(Objects::nonNull)
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
				return this.resolver.bindings.getTypeBinding(clazz.type, true);
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
					return this.resolver.bindings.getMethodBinding(methodType, method, null, true);
				}
				if( method.type instanceof Type.ForAll faType && faType.qtype instanceof MethodType mtt) {
					IMethodBinding found = this.resolver.bindings.getMethodBinding(mtt, method, null, true);
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
		if (isParameterizedType()) {
			// generic binding
			return this.resolver.bindings.getTypeBinding(this.type, true);
		}
		if (isRawType() && this.typeSymbol.type.isParameterized()) {
			// generic binding
			return this.resolver.bindings.getTypeBinding(this.typeSymbol.type, true);
		}
		return this.resolver.bindings.getTypeBinding(this.types.erasureRecursive(this.type));
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		if (typeSymbol == null) {
			return null;
		}
		try {
			Symbol symbol = types.findDescriptorSymbol(this.typeSymbol);
			if (symbol instanceof MethodSymbol methodSymbol) {
				// is a functional interface
				var res = this.types.memberType(this.type, methodSymbol).asMethodType();
				if (res != null) {
					return this.resolver.bindings.getMethodBinding(res, methodSymbol, this.type, false);
				}
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
		if (this.resolver.findDeclaringNode(this) instanceof TypeDeclaration typeDecl) {
			modifiers |= typeDecl.getModifiers(); // some invalid modifiers from DOM are missing in binding
		}
		// JDT doesn't mark interfaces as abstract
		if (this.isInterface()) {
			modifiers &= ~Modifier.ABSTRACT;
		}
		return modifiers;
	}

	@Override
	public String getName() {
		return getName(true);
	}
	
	public String getName(boolean checkParameterized) {
		if (this.isArray()) {
			StringBuilder builder = new StringBuilder(this.getElementType().getName());
			for (int i = 0; i < this.getDimensions(); i++) {
				builder.append("[]");
			}
			return builder.toString();
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
			builder.append(this.resolver.bindings.getTypeBinding(wt.type).getName());
			return builder.toString();
		}
		StringBuilder builder = new StringBuilder(this.typeSymbol.getSimpleName().toString());
		if(checkParameterized && isParameterizedType()) {
			ITypeBinding[] types = this.getUncheckedTypeArguments(this.type, this.typeSymbol);
			if (types != null && types.length > 0) {
				builder.append("<");
				for (int z = 0; z < types.length; z++ ) {
					ITypeBinding zBinding = types[z];
					if (zBinding != null) {
						builder.append(zBinding.getName());
						if( z != types.length - 1) {
							builder.append(",");
						}
					}
				}
				builder.append(">");
			}
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
		return getQualifiedNameImpl(this.type, this.typeSymbol, this.typeSymbol.owner, !this.isGeneric);
	}
	protected String getQualifiedNameImpl(Type type, TypeSymbol typeSymbol, Symbol owner, boolean includeParameters) {
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
			String parentName = getQualifiedNameImpl(parentType, tss, tss.owner, includeParameters);
			res.append(parentName);
			if( !"".equals(parentName)) {
				res.append(".");
			}
			res.append(typeSymbol.name.toString());
		} else {
			res.append(typeSymbol.toString());
		}

		if (includeParameters) {
			ITypeBinding[] typeArguments = getUncheckedTypeArguments(type, typeSymbol);
			boolean isTypeDeclaration = typeSymbol != null && typeSymbol.type == type;
			if (!isTypeDeclaration && typeArguments.length > 0) {
				res.append("<");
				int i;
				for (i = 0; i < typeArguments.length - 1; i++) {
					res.append(typeArguments[i].getQualifiedName());
					res.append(",");
				}
				res.append(typeArguments[i].getQualifiedName());
				res.append(">");
			}
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
		if (!isParameterizedType() || isTargettingPreGenerics()) {
			return NO_TYPE_ARGUMENTS;
		}
		return getUncheckedTypeArguments(this.type, this.typeSymbol);
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
			if (wildcardType.bound instanceof Type.TypeVar typeVar) {
				return this.resolver.bindings.getTypeVariableBinding(typeVar).getTypeBounds();
			}
			return new ITypeBinding[] { wildcardType.isUnbound() || wildcardType.isSuperBound() ?
					this.resolver.resolveWellKnownType(Object.class.getName()) :
					this.resolver.bindings.getTypeBinding(wildcardType.bound) };
		}
		return new ITypeBinding[0];
	}

	@Override
	public ITypeBinding getTypeDeclaration() {
		if (this.isParameterizedType() || this.isRawType()) {
			return getErasure();
		}
		return this.typeSymbol.type == this.type
			? this
			: this.resolver.bindings.getTypeBinding(this.typeSymbol.type, true);
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		if(!isGenericType() || isTargettingPreGenerics()) {
			return new ITypeBinding[0];
		}
		return ((ClassType)this.type).getTypeArguments()
				.map(this.resolver.bindings::getTypeBinding)
				.toArray(ITypeBinding[]::new);
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
			return this.types.isCastable(other.type, this.type);
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
		return !isRawType() && this.type.isParameterized() && this.isGeneric;
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
		if (this.resolver.findDeclaringNode(this) instanceof AbstractTypeDeclaration node) {
			return !(node.getParent() instanceof CompilationUnit
					|| node.getParent() instanceof AbstractTypeDeclaration
					|| node.getParent() instanceof AnonymousClassDeclaration);
		}
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
		return this.type.isParameterized() && !this.isGeneric;
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
