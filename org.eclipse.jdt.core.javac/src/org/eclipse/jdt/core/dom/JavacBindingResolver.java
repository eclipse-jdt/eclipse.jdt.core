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
package org.eclipse.jdt.core.dom;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.javac.dom.JavacAnnotationBinding;
import org.eclipse.jdt.internal.javac.dom.JavacErrorMethodBinding;
import org.eclipse.jdt.internal.javac.dom.JavacLambdaBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMemberValuePairBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMethodBinding;
import org.eclipse.jdt.internal.javac.dom.JavacModuleBinding;
import org.eclipse.jdt.internal.javac.dom.JavacPackageBinding;
import org.eclipse.jdt.internal.javac.dom.JavacTypeBinding;
import org.eclipse.jdt.internal.javac.dom.JavacTypeVariableBinding;
import org.eclipse.jdt.internal.javac.dom.JavacVariableBinding;

import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ErrorType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.JCPrimitiveType;
import com.sun.tools.javac.code.Type.JCVoidType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.ModuleType;
import com.sun.tools.javac.code.Type.PackageType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;

/**
 * Deals with creation of binding model, using the <code>Symbol</code>s from Javac.
 * @implNote Cannot move to another package because parent class is package visible only
 */
public class JavacBindingResolver extends BindingResolver {

	private final JavacTask javac; // TODO evaluate memory cost of storing the instance
	// it will probably be better to run the `Enter` and then only extract interesting
	// date from it.
	public final Context context;
	public Map<Symbol, ASTNode> symbolToDeclaration;
	public final IJavaProject javaProject;
	private JavacConverter converter;
	boolean isRecoveringBindings = false;

	public static class BindingKeyException extends Exception {
		private static final long serialVersionUID = -4468681148041117634L;
		public BindingKeyException(Throwable t) {
			super(t);
		}
	    public BindingKeyException(String message, Throwable cause) {
	    	super(message, cause);
	    }
	}

	public class Bindings {
		private Map<String, JavacAnnotationBinding> annotationBindings = new HashMap<>();
		public JavacAnnotationBinding getAnnotationBinding(Compound ann, IBinding recipient) {
			JavacAnnotationBinding newInstance = new JavacAnnotationBinding(ann, JavacBindingResolver.this, recipient) { };
			String k = newInstance.getKey();
			if( k != null ) {
				annotationBindings.putIfAbsent(k, newInstance);
				return annotationBindings.get(k);
			}
			return null;
		}
		//
		private Map<String, JavacMemberValuePairBinding> memberValuePairBindings = new HashMap<>();
		public JavacMemberValuePairBinding getMemberValuePairBinding(MethodSymbol key, Attribute value) {
			JavacMemberValuePairBinding newInstance = new JavacMemberValuePairBinding(key, value, JavacBindingResolver.this) { };
			String k = newInstance.getKey();
			if( k != null ) {
				memberValuePairBindings.putIfAbsent(k, newInstance);
				return memberValuePairBindings.get(k);
			}
			return null;
		}
		//
		private Map<JavacMethodBinding, JavacMethodBinding> methodBindings = new HashMap<>();
		public JavacMethodBinding getMethodBinding(MethodType methodType, MethodSymbol sym, com.sun.tools.javac.code.Type type,
				boolean isSynthetic, boolean isDeclaration) {
			if( isSynthetic ) {
				return getSyntheticMethodBinding(methodType, sym, type);
			} else {
				return getMethodBinding(methodType, sym, type, isDeclaration);
			}
		}

		public JavacMethodBinding getMethodBinding(MethodType methodType, MethodSymbol methodSymbol, com.sun.tools.javac.code.Type parentType, boolean isDeclaration) {
			JavacMethodBinding newInstance = new JavacMethodBinding(methodType, methodSymbol, parentType, JavacBindingResolver.this, false, isDeclaration) { };
			return insertAndReturn(newInstance);
		}
		public JavacMethodBinding getSyntheticMethodBinding(MethodType methodType, MethodSymbol methodSymbol, com.sun.tools.javac.code.Type parentType) {
			JavacMethodBinding newInstance = new JavacMethodBinding(methodType, methodSymbol, parentType, JavacBindingResolver.this, true, false) { };
			return insertAndReturn(newInstance);
		}
		public JavacMethodBinding getErrorMethodBinding(MethodType methodType, Symbol originatingSymbol) {
			JavacMethodBinding newInstance = new JavacErrorMethodBinding(originatingSymbol, methodType, JavacBindingResolver.this) { };
			return insertAndReturn(newInstance);
		}
		private JavacMethodBinding insertAndReturn(JavacMethodBinding newInstance) {
			methodBindings.putIfAbsent(newInstance, newInstance);
			return methodBindings.get(newInstance);
		}
		//
		private Map<String, JavacModuleBinding> moduleBindings = new HashMap<>();
		public JavacModuleBinding getModuleBinding(ModuleType moduleType) {
			JavacModuleBinding newInstance = new JavacModuleBinding(moduleType, JavacBindingResolver.this) { };
			String k = newInstance.getKey();
			if( k != null ) {
				moduleBindings.putIfAbsent(k, newInstance);
				return moduleBindings.get(k);
			}
			return null;
		}
		public JavacModuleBinding getModuleBinding(ModuleSymbol moduleSymbol) {
			JavacModuleBinding newInstance = new JavacModuleBinding(moduleSymbol, JavacBindingResolver.this) { };
			String k = newInstance.getKey();
			if( k != null ) {
				moduleBindings.putIfAbsent(k, newInstance);
				return moduleBindings.get(k);
			}
			return null;
		}
		public JavacModuleBinding getModuleBinding(JCModuleDecl moduleDecl) {
			JavacModuleBinding newInstance = new JavacModuleBinding(moduleDecl, JavacBindingResolver.this) { };
			// Overwrite existing
			String k = newInstance.getKey();
			if( k != null ) {
				moduleBindings.put(k, newInstance);
				return moduleBindings.get(k);
			}
			return null;
		}

		//
		private Map<String, JavacPackageBinding> packageBindings = new HashMap<>();
		public JavacPackageBinding getPackageBinding(PackageSymbol packageSymbol) {
			JavacPackageBinding newInstance = new JavacPackageBinding(packageSymbol, JavacBindingResolver.this) { };
			return preferentiallyInsertPackageBinding(newInstance);
		}
		public JavacPackageBinding getPackageBinding(Name name) {
			String n = null;
			if( name instanceof QualifiedName ) 
				n = name.toString();
			else if( name instanceof SimpleName snn) {
				if( name.getParent() instanceof QualifiedName qn) {
					if( qn.getName() == name ) {
						n = qn.toString();
					} else if( qn.getQualifier() == name) {
						n = name.toString();
					}
				}
			}
			if( n == null )
				return null;
			JavacPackageBinding newInstance = new JavacPackageBinding(n, JavacBindingResolver.this) {};
			return preferentiallyInsertPackageBinding(newInstance);
		}
		private JavacPackageBinding preferentiallyInsertPackageBinding(JavacPackageBinding newest) {
			// A package binding may be created while traversing something as simple as a name. 
			// The binding using name-only logic should be instantiated, but 
			// when a proper symbol is found, it should be added to that object. 
			String k = newest == null ? null : newest.getKey();
			if( k != null ) {
				JavacPackageBinding current = packageBindings.get(k);
				if( current == null ) {
					packageBindings.putIfAbsent(k, newest);
				} else if( current.getPackageSymbol() == null && newest.getPackageSymbol() != null) {
					current.setPackageSymbol(newest.getPackageSymbol());
				}
				return packageBindings.get(k);
			}
			return null;
		}
		//
		private Map<JavacTypeBinding, JavacTypeBinding> typeBinding = new HashMap<>();
		public JavacTypeBinding getTypeBinding(JCTree tree, com.sun.tools.javac.code.Type type) {
			return getTypeBinding(type, tree instanceof JCClassDecl);
		}
		public JavacTypeBinding getTypeBinding(com.sun.tools.javac.code.Type type) {
			if (type == null) {
				return null;
			}
			return getTypeBinding(type.baseType() /* remove metadata for constant values */, false);
		}
		public JavacTypeBinding getTypeBinding(com.sun.tools.javac.code.Type type, boolean isDeclaration) {
			if (type instanceof com.sun.tools.javac.code.Type.TypeVar typeVar) {
				return getTypeVariableBinding(typeVar);
			}
			if (type == null || type == com.sun.tools.javac.code.Type.noType) {
				return null;
			}
			if (type instanceof ErrorType errorType) {
				var originalType = errorType.getOriginalType();
				if (originalType != com.sun.tools.javac.code.Type.noType
						&& !(originalType instanceof com.sun.tools.javac.code.Type.MethodType)
						&& !(originalType instanceof com.sun.tools.javac.code.Type.ForAll)
						&& !(originalType instanceof com.sun.tools.javac.code.Type.ErrorType)) {
					JavacTypeBinding newInstance = new JavacTypeBinding(originalType, type.tsym, isDeclaration, JavacBindingResolver.this) { };
					typeBinding.putIfAbsent(newInstance, newInstance);
					JavacTypeBinding jcb = typeBinding.get(newInstance);
					jcb.setRecovered(true);
					return jcb;
				} else if (errorType.tsym instanceof ClassSymbol classErrorSymbol &&
							Character.isJavaIdentifierStart(classErrorSymbol.getSimpleName().charAt(0))) {
					// non usable original type: try symbol
					JavacTypeBinding newInstance = new JavacTypeBinding(classErrorSymbol.type, classErrorSymbol, isDeclaration, JavacBindingResolver.this) { };
					typeBinding.putIfAbsent(newInstance, newInstance);
					JavacTypeBinding jcb = typeBinding.get(newInstance);
					jcb.setRecovered(true);
					return jcb;
				}
				// no type information  we could recover from
				return null;
			}
			if (!type.isParameterized() && !type.isRaw() && type instanceof ClassType classType
					&& classType.interfaces_field == null) {
				// workaround faulty case of TypeMismatchQuickfixText.testMismatchingReturnTypeOnGenericMethod
				// interfaces/supertypes are not set which seem to imply that the compiler generated
				// a dummy type object that's not suitable for a binding.
				// Fail back to an hopefully better type
				type = type.tsym.type;
			}
			JavacTypeBinding newInstance = new JavacTypeBinding(type, type.tsym, isDeclaration, JavacBindingResolver.this) { };
			typeBinding.putIfAbsent(newInstance, newInstance);
			return typeBinding.get(newInstance);
		}
		//
		private Map<JavacTypeVariableBinding, JavacTypeVariableBinding> typeVariableBindings = new HashMap<>();
		public JavacTypeVariableBinding getTypeVariableBinding(TypeVar typeVar) {
			JavacTypeVariableBinding newInstance = new JavacTypeVariableBinding(typeVar, (TypeVariableSymbol)typeVar.tsym, JavacBindingResolver.this) { };
			typeVariableBindings.putIfAbsent(newInstance, newInstance);
			return typeVariableBindings.get(newInstance);
		}
		//
		private Map<String, JavacVariableBinding> variableBindings = new HashMap<>();
		public JavacVariableBinding getVariableBinding(VarSymbol varSymbol) {
			if (varSymbol == null) {
				return null;
			}
			JavacVariableBinding newInstance = new JavacVariableBinding(varSymbol, JavacBindingResolver.this) { };
			String k = newInstance.getKey();
			if( k != null ) {
				variableBindings.putIfAbsent(k, newInstance);
				return variableBindings.get(k);
			}
			return null;
		}
		//
		private Map<String, JavacLambdaBinding> lambdaBindings = new HashMap<>();
		public JavacLambdaBinding getLambdaBinding(JavacMethodBinding javacMethodBinding, LambdaExpression lambda) {
			JavacLambdaBinding newInstance = new JavacLambdaBinding(javacMethodBinding, lambda);
			String k = newInstance.getKey();
			if( k != null ) {
				lambdaBindings.putIfAbsent(k, newInstance);
				return lambdaBindings.get(k);
			}
			return null;
		}

		public IBinding getBinding(final Symbol owner, final com.sun.tools.javac.code.Type type) {
			Symbol recoveredSymbol = getRecoveredSymbol(type);
			if (recoveredSymbol != null) {
				return getBinding(recoveredSymbol, recoveredSymbol.type);
			}
			if (type instanceof ErrorType) {
				if (type.getOriginalType() instanceof MethodType missingMethodType) {
					return getErrorMethodBinding(missingMethodType, owner);
				}
			}
			if (owner instanceof final PackageSymbol other) {
				return getPackageBinding(other);
			} else if (owner instanceof ModuleSymbol typeSymbol) {
				return getModuleBinding(typeSymbol);
			} else if (owner instanceof Symbol.TypeVariableSymbol typeVariableSymbol) {
				if (type instanceof TypeVar typeVar) {
					return getTypeVariableBinding(typeVar);
				} else if (typeVariableSymbol.type instanceof TypeVar typeVar) {
					return getTypeVariableBinding(typeVar);
				}
				// without the type there is not much we can do; fallthrough to null
			} else if (owner instanceof TypeSymbol typeSymbol) {
				return getTypeBinding(isTypeOfType(type) ? type : typeSymbol.type);
			} else if (owner instanceof final MethodSymbol other) {
				return getMethodBinding(type instanceof com.sun.tools.javac.code.Type.MethodType methodType ? methodType : owner.type.asMethodType(), other, null, false);
			} else if (owner instanceof final VarSymbol other) {
				return getVariableBinding(other);
			}
			return null;
		}
		public IBinding getBinding(String key) {
			IBinding binding;
			binding = this.annotationBindings.get(key);
			if (binding != null) {
				return binding;
			}
			binding = this.memberValuePairBindings.get(key);
			if (binding != null) {
				return binding;
			}
			binding = this.methodBindings.values()
					.stream()
					.filter(methodBindings -> key.equals(methodBindings.getKey()))
					.findAny()
					.orElse(null);
			if (binding != null) {
				return binding;
			}
			binding = this.moduleBindings.get(key);
			if (binding != null) {
				return binding;
			}
			binding = this.packageBindings.get(key);
			if (binding != null) {
				return binding;
			}
			binding = this.typeBinding.values()
					.stream()
					.filter(typeBinding -> key.equals(typeBinding.getKey()))
					.findAny()
					.orElse(null);
			if (binding != null) {
				return binding;
			}
			return this.variableBindings.get(key);
		}
	}
	public final Bindings bindings = new Bindings();
	private WorkingCopyOwner owner;
	private HashMap<ASTNode, IBinding> resolvedBindingsCache = new HashMap<>();

	public JavacBindingResolver(IJavaProject javaProject, JavacTask javacTask, Context context, JavacConverter converter, WorkingCopyOwner owner) {
		this.javac = javacTask;
		this.context = context;
		this.javaProject = javaProject;
		this.converter = converter;
		this.owner = owner;
	}

	private void resolve() {
		if (this.symbolToDeclaration != null) {
			// already done and ready
			return;
		}
		synchronized (this.javac) { // prevents from multiple `analyze` for the same task
			boolean alreadyAnalyzed = this.converter.domToJavac.values().stream().map(TreeInfo::symbolFor).anyMatch(Objects::nonNull);
			if (!alreadyAnalyzed) {
				// symbols not already present: analyze
				try {
					this.javac.analyze();
				} catch (IOException e) {
					ILog.get().error(e.getMessage(), e);
				}
			}
		}
		synchronized (this) {
			if (this.symbolToDeclaration == null) {
				Map<Symbol, ASTNode> wipSymbolToDeclaration = new HashMap<>();
				this.converter.domToJavac.forEach((jdt, javac) -> {
					// We don't want FieldDeclaration (ref ASTConverterTest2.test0433)
					if (jdt instanceof MethodDeclaration ||
						jdt instanceof VariableDeclaration ||
						jdt instanceof EnumConstantDeclaration ||
						jdt instanceof AnnotationTypeMemberDeclaration ||
						jdt instanceof AbstractTypeDeclaration ||
						jdt instanceof AnonymousClassDeclaration ||
						jdt instanceof TypeParameter) {
						var symbol = TreeInfo.symbolFor(javac);
						if (symbol != null) {
							wipSymbolToDeclaration.put(symbol, jdt);
						}
					}
				});
				// prefill the binding so that they're already searchable by key
				wipSymbolToDeclaration.keySet().forEach(sym -> this.bindings.getBinding(sym, null));
				this.symbolToDeclaration = wipSymbolToDeclaration;
			}
		}
	}

	@Override
	public ASTNode findDeclaringNode(IBinding binding) {
		return findNode(getJavacSymbol(binding));
	}

	@Override
	public ASTNode findDeclaringNode(String bindingKey) {
		resolve();
		IBinding binding = this.bindings.getBinding(bindingKey);
		if (binding == null) {
			return null;
		}
		return findDeclaringNode(binding);
	}

	private Symbol getJavacSymbol(IBinding binding) {
		if (binding instanceof JavacMemberValuePairBinding valuePair) {
			return getJavacSymbol(valuePair.method);
		}
		if (binding instanceof JavacAnnotationBinding annotation) {
			return getJavacSymbol(annotation.getAnnotationType());
		}
		if (binding instanceof JavacMethodBinding method) {
			return method.methodSymbol;
		}
		if (binding instanceof JavacPackageBinding packageBinding) {
			return packageBinding.getPackageSymbol();
		}
		if (binding instanceof JavacTypeBinding type) {
			return type.typeSymbol;
		}
		if (binding instanceof JavacVariableBinding variable) {
			return variable.variableSymbol;
		}
		return null;
	}

	public ASTNode findNode(Symbol symbol) {
		if (this.symbolToDeclaration != null) {
			return this.symbolToDeclaration.get(symbol);
		}
		return null;
	}

	@Override
	public ITypeBinding resolveType(Type type) {
		if (type.getParent() instanceof ParameterizedType parameterized
			&& type.getLocationInParent() == ParameterizedType.TYPE_PROPERTY) {
			// use parent type for this as it keeps generics info
			return resolveType(parameterized);
		}
		resolve();
		if (type.getParent() instanceof ArrayCreation arrayCreation) {
			JCTree jcArrayCreation = this.converter.domToJavac.get(arrayCreation);
			return this.bindings.getTypeBinding(((JCNewArray)jcArrayCreation).type);
		}
		JCTree jcTree = this.converter.domToJavac.get(type);
		if (jcTree instanceof JCIdent ident && ident.type != null) {
			if (ident.type instanceof PackageType) {
				return null;
			}
			return this.bindings.getTypeBinding(ident.type);
		}
		if (jcTree instanceof JCFieldAccess access) {
			return this.bindings.getTypeBinding(access.type);
		}
		if (jcTree instanceof JCPrimitiveTypeTree primitive && primitive.type != null) {
			return this.bindings.getTypeBinding(primitive.type);
		}
		if (jcTree instanceof JCArrayTypeTree arrayType && arrayType.type != null) {
			return this.bindings.getTypeBinding(arrayType.type);
		}
		if (jcTree instanceof JCWildcard wcType && wcType.type != null) {
			return this.bindings.getTypeBinding(wcType.type);
		}
		if (jcTree instanceof JCTypeApply jcta && jcta.type != null) {
			var res = this.bindings.getTypeBinding(jcta.type);
			if (res != null) {
				return res;
			}
			if (jcta.getType().type instanceof ErrorType errorType) {
				res = this.bindings.getTypeBinding(errorType.getOriginalType(), true);
				if (res != null) {
					return res;
				}
			}
			if (jcta.getType().type != null) {
				res = this.bindings.getTypeBinding(jcta.getType().type);
				if (res != null) {
					return res;
				}
			}
		}
		if (jcTree instanceof JCAnnotatedType annotated && annotated.type != null) {
			return this.bindings.getTypeBinding(annotated.type);
		}

//			return this.flowResult.stream().map(env -> env.enclClass)
//				.filter(Objects::nonNull)
//				.map(decl -> decl.type)
//				.map(javacType -> javacType.tsym)
//				.filter(sym -> Objects.equals(type.toString(), sym.name.toString()))
//				.findFirst()
//				.map(symbol -> new JavacTypeBinding(symbol, this))
//				.orElse(null);
//		}
//		if (type instanceof QualifiedType qualifiedType) {
//			JCTree jcTree = this.converter.domToJavac.get(qualifiedType);
//		}
		if (type instanceof PrimitiveType primitive) { // a type can be requested even if there is no token for it in JCTree
			return resolveWellKnownType(primitive.getPrimitiveTypeCode().toString());
		}
		if (type.getAST().apiLevel() >= AST.JLS10 && type.isVar()) {
			if (type.getParent() instanceof VariableDeclaration varDecl) {
				IVariableBinding varBinding = resolveVariable(varDecl);
				if (varBinding != null) {
					return varBinding.getType();
				}
			}
			if (type.getParent() instanceof VariableDeclarationStatement statement &&
				this.converter.domToJavac.get(statement) instanceof JCVariableDecl jcDecl &&
				jcDecl.type != null) {
				return this.bindings.getTypeBinding(jcDecl.type);
			}
		}
		// Recovery: sometime with Javac, there is no suitable type/symbol
		// Workaround: use a RecoveredTypeBinding
		// Caveats: cascade to other workarounds
		return createRecoveredTypeBinding(type);
	}

	private RecoveredTypeBinding createRecoveredTypeBinding(Type type) {
		return new RecoveredTypeBinding(this, type) {
			@Override
			public ITypeBinding getTypeDeclaration() {
				if (isParameterizedType()) {
					return new GenericRecoveredTypeBinding(JavacBindingResolver.this, type, this);
				}
				return super.getTypeDeclaration();
			}
			@Override
			public IPackageBinding getPackage() {
				if (type instanceof SimpleType simpleType && simpleType.getName() instanceof SimpleName) {
					return JavacBindingResolver.this.converter.domToJavac
						.values()
						.stream()
						.filter(CompilationUnit.class::isInstance)
						.map(CompilationUnit.class::cast)
						.map(CompilationUnit::getPackage)
						.map(PackageDeclaration::resolveBinding)
						.findAny()
						.orElse(super.getPackage());
				}
				return super.getPackage();
			}
		};
	}

	@Override
	ITypeBinding resolveType(AnnotationTypeDeclaration type) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(type);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.type);
		}
		return null;
	}

	@Override
	ITypeBinding resolveType(RecordDeclaration type) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(type);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.type);
		}
		return null;
	}


	@Override
	ITypeBinding resolveType(TypeDeclaration type) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(type);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.type, true);
		}
		return null;
	}

	@Override
	ITypeBinding resolveType(EnumDeclaration enumDecl) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(enumDecl);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.type, true);
		}
		return null;
	}

	@Override
	ITypeBinding resolveType(AnonymousClassDeclaration anonymousClassDecl) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(anonymousClassDecl);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.type, true);
		}
		return null;
	}
	ITypeBinding resolveTypeParameter(TypeParameter typeParameter) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(typeParameter);
		if (javacNode instanceof JCTypeParameter jcClassDecl) {
			return this.bindings.getTypeBinding(jcClassDecl.type);
		}
		return null;
	}

	@Override
	IVariableBinding resolveField(FieldAccess fieldAccess) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(fieldAccess);
		if (javacElement instanceof JCFieldAccess javacFieldAccess && javacFieldAccess.sym instanceof VarSymbol varSymbol) {
			return this.bindings.getVariableBinding(varSymbol);
		}
		return null;
	}

	@Override
	IVariableBinding resolveField(SuperFieldAccess fieldAccess) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(fieldAccess);
		if (javacElement instanceof JCFieldAccess javacFieldAccess && javacFieldAccess.sym instanceof VarSymbol varSymbol) {
			return this.bindings.getVariableBinding(varSymbol);
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(MethodInvocation method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		List<com.sun.tools.javac.code.Type> typeArgs = List.of();
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
			typeArgs = javacMethodInvocation.getTypeArguments().stream().map(jcExpr -> jcExpr.type).toList();
		}
		var type = javacElement.type;
		// next condition matches `localMethod(this::missingMethod)`
		if (javacElement instanceof JCIdent ident && type == null) {
			ASTNode node = method;
			while (node != null && !(node instanceof AbstractTypeDeclaration)) {
				node = node.getParent();
			}
			if (node instanceof AbstractTypeDeclaration decl &&
				this.converter.domToJavac.get(decl) instanceof JCClassDecl javacClassDecl &&
				javacClassDecl.type instanceof ClassType classType &&
				!classType.isErroneous()) {
				type = classType;
			}
			if (type != null &&
				type.tsym.members().findFirst(ident.getName(), MethodSymbol.class::isInstance) instanceof MethodSymbol methodSymbol &&
				methodSymbol.type instanceof MethodType methodType) {
				var res = this.bindings.getMethodBinding(methodType, methodSymbol, null, false);
				if (res != null) {
					return res;
				}
			}
		}
		var sym = javacElement instanceof JCIdent ident ? ident.sym :
			javacElement instanceof JCFieldAccess fieldAccess ? fieldAccess.sym :
				null;
		if (type instanceof MethodType methodType && sym instanceof MethodSymbol methodSymbol) {
			com.sun.tools.javac.code.Type parentType = null;
			if (methodSymbol.owner instanceof ClassSymbol ownerClass && isTypeOfType(ownerClass.type)) {
				if (ownerClass.type.isParameterized()
					&& method.getExpression() != null
					&& resolveExpressionType(method.getExpression()) instanceof JavacTypeBinding exprType) {
					parentType = exprType.type;
				} else {
					parentType = ownerClass.type;
				}
			}
			return this.bindings.getMethodBinding(methodType, methodSymbol, parentType, false);
		}
		if (type instanceof ErrorType errorType && errorType.getOriginalType() instanceof MethodType methodType) {
			if (sym.owner instanceof TypeSymbol typeSymbol) {
				Iterator<Symbol> methods = typeSymbol.members().getSymbolsByName(sym.getSimpleName(), m -> m instanceof MethodSymbol && methodType.equals(m.type)).iterator();
				if (methods.hasNext()) {
					return this.bindings.getMethodBinding(methodType, (MethodSymbol)methods.next(), null, false);
				}
			}
			return this.bindings.getErrorMethodBinding(methodType, sym);
		}
		if (type == null && sym instanceof MethodSymbol methodSym && methodSym.type instanceof ForAll methodTemplateType) {
			// build type from template
			Map<TypeVar, com.sun.tools.javac.code.Type> resolutionMapping = new HashMap<>();
			var templateParameters = methodTemplateType.getTypeVariables();
			for (int i = 0; i < typeArgs.size() && i < templateParameters.size(); i++) {
				resolutionMapping.put(templateParameters.get(i), typeArgs.get(i));
			}
			MethodType methodType = new MethodType(
					methodTemplateType.asMethodType().getParameterTypes().map(t -> applyType(t, resolutionMapping)),
					applyType(methodTemplateType.asMethodType().getReturnType(), resolutionMapping),
					methodTemplateType.asMethodType().getThrownTypes().map(t -> applyType(t, resolutionMapping)),
					methodTemplateType.tsym);
			return this.bindings.getMethodBinding(methodType, methodSym, methodSym.owner.type, false);
		}
		if (type == null && sym != null && sym.type.isErroneous()
			&& sym.owner.type instanceof ClassType classType) {
			var parentTypeBinding = this.bindings.getTypeBinding(classType);
			return Arrays.stream(parentTypeBinding.getDeclaredMethods())
				.filter(binding -> binding.getName().equals(sym.getSimpleName().toString()))
				.findAny()
				.orElse(null);
		}
		if (type == null && sym instanceof MethodSymbol methodSymbol && methodSymbol.type instanceof MethodType
			&& javacElement instanceof JCFieldAccess selectedMethod
			&& selectedMethod.getExpression() != null
			&& selectedMethod.getExpression().type instanceof ClassType classType) {
			// method is resolved, but type is not, probably because of invalid param
			// workaround: check compatible method in selector
			var parentTypeBinding = this.bindings.getTypeBinding(classType);
			var res = Arrays.stream(parentTypeBinding.getDeclaredMethods())
				.filter(binding -> binding instanceof JavacMethodBinding javacMethodBinding && javacMethodBinding.methodSymbol == methodSymbol)
				.findAny()
				.orElse(null);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	/**
	 * Derives an "applied" type replacing know TypeVar with their current value
	 * @param from The type to check for replacement of TypeVars
	 * @param resolutionMapping a dictionary defining which concrete type must replace TypeVar
	 * @return The derived "applied" type: recursively checks the type, replacing
	 * 	known {@link TypeVar} instances in those with their value defined in `resolutionMapping`
	 */
	private static com.sun.tools.javac.code.Type applyType(com.sun.tools.javac.code.Type from, Map<TypeVar, com.sun.tools.javac.code.Type> resolutionMapping) {
		if (from instanceof TypeVar typeVar) {
			var directMapping = resolutionMapping.get(from);
			if (directMapping != null) {
				return directMapping;
			}
			return typeVar;
		}
		if (from instanceof JCNoType || from instanceof JCVoidType ||
			from instanceof JCPrimitiveType) {
			return from;
		}
		if (from instanceof ClassType classType) {
			var args = classType.getTypeArguments().map(typeArg -> applyType(typeArg, resolutionMapping));
			if (Objects.equals(args, classType.getTypeArguments())) {
				return classType;
			}
			return new ClassType(classType.getEnclosingType(), args, classType.tsym);
		}
		if (from instanceof ArrayType arrayType) {
			var targetElemType = applyType(arrayType.elemtype, resolutionMapping);
			if (Objects.equals(targetElemType, arrayType.elemtype)) {
				return arrayType;
			}
			return new ArrayType(targetElemType, arrayType.tsym);
		}
		return from;
	}

	@Override
	IMethodBinding resolveMethod(MethodDeclaration method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		if (javacElement instanceof JCMethodDecl methodDecl) {
			if (methodDecl.type != null) {
				return this.bindings.getMethodBinding(methodDecl.type.asMethodType(), methodDecl.sym, null, true);
			}
			if (methodDecl.sym instanceof MethodSymbol methodSymbol && methodSymbol.type != null) {
				return this.bindings.getMethodBinding(methodSymbol.type.asMethodType(), methodSymbol, null, true);
			}
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(LambdaExpression lambda) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(lambda);
		if (javacElement instanceof JCLambda jcLambda) {
			JavacTypeBinding typeBinding = this.bindings.getTypeBinding(jcLambda.type);
			if (typeBinding != null && typeBinding.getFunctionalInterfaceMethod() instanceof JavacMethodBinding methodBinding) {
				return this.bindings.getLambdaBinding(methodBinding, lambda);
			}
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(MethodReference methodReference) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(methodReference);
		if (javacElement instanceof JCMemberReference memberRef && memberRef.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(memberRef.referentType.asMethodType(), methodSymbol, null, false);
		}
		return null;
	}

	@Override
	IMethodBinding resolveMember(AnnotationTypeMemberDeclaration member) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(member);
		if (javacElement instanceof JCMethodDecl methodDecl) {
			return this.bindings.getMethodBinding(methodDecl.type.asMethodType(), methodDecl.sym, null, true);
		}
		return null;
	}

	@Override
	IMethodBinding resolveConstructor(EnumConstantDeclaration enumConstantDeclaration) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(enumConstantDeclaration);
		if( javacElement instanceof JCVariableDecl jcvd ) {
			javacElement = jcvd.init;
		}
		return javacElement instanceof JCNewClass jcExpr
				&& !jcExpr.constructor.type.isErroneous()?
						this.bindings.getMethodBinding(jcExpr.constructor.type.asMethodType(), (MethodSymbol)jcExpr.constructor, null, true) :
				null;
	}

	@Override
	IMethodBinding resolveConstructor(SuperConstructorInvocation expression) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(expression);
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(ident.type != null ? ident.type.asMethodType() : methodSymbol.asType().asMethodType(), methodSymbol, null, false);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, null, false);
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(SuperMethodInvocation method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(ident.type.asMethodType(), methodSymbol, null, false);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol
				&& fieldAccess.type != null /* when there are syntax errors */) {
			return this.bindings.getMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, null, false);
		}
		return null;
	}

	IBinding resolveCached(ASTNode node, Function<ASTNode, IBinding> l) {
		// Avoid using `computeIfAbsent` because it throws
		// ConcurrentModificationException when nesting calls
		var res = resolvedBindingsCache.get(node);
		if (res == null) {
			res = l.apply(node);
			resolvedBindingsCache.put(node, res);
		}
		return res;
	}

	@Override
	IBinding resolveName(Name name) {
		return resolveCached(name, (n) -> resolveNameImpl((Name)n));
	}

	private IBinding resolveNameImpl(Name name) {
		resolve();
		JCTree tree = this.converter.domToJavac.get(name);
		if( tree != null ) {
			var res = resolveNameToJavac(name, tree);
			if (res != null) {
				return res;
			}
		}
		DocTreePath path = this.converter.findDocTreePath(name);
		if (path != null) {
			if (JavacTrees.instance(this.context).getElement(path) instanceof Symbol symbol) {
				return this.bindings.getBinding(symbol, null);
			}
		}
		
		PackageSymbol ps = findPackageSymbol(name);
		if( ps != null ) {
			return this.bindings.getPackageBinding(ps);
		}
		if( isPackageName(name)) {
			return this.bindings.getPackageBinding(name);
		}
		ASTNode parent = name.getParent();
		if (name.getLocationInParent() == QualifiedName.NAME_PROPERTY && parent instanceof QualifiedName qname &&
			qname.getParent() instanceof SimpleType simpleType && simpleType.getLocationInParent() == ParameterizedType.TYPE_PROPERTY) {
			var typeBinding = resolveType((ParameterizedType)simpleType.getParent());
			if (typeBinding != null) {
				return typeBinding;
			}
		}
		if (name.getLocationInParent() == QualifiedType.NAME_PROPERTY &&
			parent.getLocationInParent() == QualifiedType.QUALIFIER_PROPERTY) {
			var typeBinding = resolveType((QualifiedType)parent);
			return typeBinding.getTypeDeclaration(); // exclude params
		}
		if (name.getLocationInParent() == SimpleType.NAME_PROPERTY
				|| name.getLocationInParent() == QualifiedType.NAME_PROPERTY
				|| name.getLocationInParent() == NameQualifiedType.NAME_PROPERTY) { // case of "var"
			return resolveType((Type)parent);
		}
		if (tree == null && (name.getFlags() & ASTNode.ORIGINAL) != 0) {
			tree = this.converter.domToJavac.get(parent);
			if( tree instanceof JCFieldAccess jcfa) {
				if( jcfa.selected instanceof JCIdent jcid && jcid.toString().equals(name.toString())) {
					tree = jcfa.selected;
				}
				var grandParent = parent.getParent();
				if (grandParent instanceof ParameterizedType parameterized) {
					var parameterizedType = resolveType(parameterized);
					if (parameterizedType != null) {
						return parameterizedType;
					}
					
				}
			}
		}
		if( tree != null ) {
			IBinding ret = resolveNameToJavac(name, tree);
			if (ret != null) {
				return ret;
			}
		}
		if (parent instanceof ImportDeclaration importDecl && importDecl.getName() == name) {
			return resolveImport(importDecl);
		}
		if (parent instanceof QualifiedName parentName && parentName.getName() == name) {
			return resolveNameImpl(parentName);
		}
		if( parent instanceof MethodRef mref && mref.getName() == name) {
			return resolveReference(mref);
		}
		if( parent instanceof MemberRef mref && mref.getName() == name) {
			return resolveReference(mref);
		}
		if (parent instanceof MethodInvocation methodInvocation && methodInvocation.getName() == name) {
			return resolveMethod(methodInvocation);
		}
		if (parent instanceof MethodDeclaration methodDeclaration && methodDeclaration.getName() == name) {
			return resolveMethod(methodDeclaration);
		}
		if (parent instanceof ExpressionMethodReference methodRef && methodRef.getName() == name) {
			return resolveMethod(methodRef);
		}
		if (parent instanceof TypeMethodReference methodRef && methodRef.getName() == name) {
			return resolveMethod(methodRef);
		}
		if (parent instanceof SuperMethodReference methodRef && methodRef.getName() == name) {
			return resolveMethod(methodRef);
		}
		if (parent instanceof VariableDeclaration decl && decl.getName() == name) {
			return resolveVariable(decl);
		}
		return null;
	}

	private boolean isPackageName(Name name) {
		ASTNode working = name;
		boolean insideQualifier = false;
		while( working instanceof Name ) {
			JCTree tree = this.converter.domToJavac.get(working);
			if( tree instanceof JCFieldAccess jcfa) {
				return jcfa.sym instanceof PackageSymbol;
			}
			if( working instanceof QualifiedName qnn) {
				if( qnn.getQualifier() == working) {
					insideQualifier = true;
				}
			}
			working = working.getParent();
		}
		return insideQualifier;
	}
	
	private PackageSymbol findPackageSymbol(Name name) {
		if( name instanceof SimpleName sn) {
			ASTNode parent = sn.getParent();
			if( parent instanceof QualifiedName qn) {
				JCTree tree = this.converter.domToJavac.get(parent);
				if( tree instanceof JCFieldAccess jcfa) {
					if( qn.getQualifier().equals(name)) {
						if( jcfa.selected instanceof JCIdent jcid && jcid.sym instanceof PackageSymbol pss)
							return  pss;
					} else if( qn.getName().equals(name)) {
						return jcfa.sym instanceof PackageSymbol pss ? pss : null;
					}
				}
			}
		}
		if( name instanceof QualifiedName qn ) {
			JCTree tree = this.converter.domToJavac.get(qn);
			if( tree instanceof JCFieldAccess jcfa) {
				return jcfa.sym instanceof PackageSymbol pss ? pss : null;
			}
		}
		return null;
	}

	IBinding resolveNameToJavac(Name name, JCTree tree) {
		boolean isTypeDeclaration = (name.getParent() instanceof AbstractTypeDeclaration typeDeclaration && typeDeclaration.getName() == name)
				|| (name.getParent() instanceof SimpleType type && type.getName() == name);
		if( name.getParent() instanceof AnnotatableType st && st.getParent() instanceof ParameterizedType pt) {
			if( st == pt.getType()) {
				tree = this.converter.domToJavac.get(pt);
				if (tree.type != null && !tree.type.isErroneous()) {
					IBinding b = this.bindings.getTypeBinding(tree.type, isTypeDeclaration);
					if( b != null ) {
						return b;
					}
				}
			}
		}

		if (tree instanceof JCIdent ident && ident.sym != null) {
			if (ident.type instanceof ErrorType errorType
					&& errorType.getOriginalType() instanceof ErrorType) {
				return null;
			}
			if (isTypeDeclaration) {
				return this.bindings.getTypeBinding(ident.type != null ? ident.type : ident.sym.type, true);
			}
			return this.bindings.getBinding(ident.sym, ident.type != null ? ident.type : ident.sym.type);
		}
		if (tree instanceof JCTypeApply variableDecl && variableDecl.type != null) {
			return this.bindings.getTypeBinding(variableDecl.type);
		}
		if (tree instanceof JCFieldAccess fieldAccess && fieldAccess.sym != null) {
			com.sun.tools.javac.code.Type typeToUse = fieldAccess.type;
			if(fieldAccess.selected instanceof JCTypeApply) {
				typeToUse = fieldAccess.sym.type;
			}
			return this.bindings.getBinding(fieldAccess.sym, typeToUse);
		}
		if (tree instanceof JCMethodInvocation methodInvocation && methodInvocation.meth.type != null) {
			return this.bindings.getBinding(((JCFieldAccess)methodInvocation.meth).sym, methodInvocation.meth.type);
		}
		if (tree instanceof JCClassDecl classDecl && classDecl.sym != null) {
			return this.bindings.getBinding(classDecl.sym, classDecl.type);
		}
		if (tree instanceof JCMethodDecl methodDecl && methodDecl.sym != null) {
			return this.bindings.getBinding(methodDecl.sym, methodDecl.type);
		}
		if (tree instanceof JCVariableDecl variableDecl && variableDecl.sym != null) {
			return this.bindings.getBinding(variableDecl.sym, variableDecl.type);
		}
		if (tree instanceof JCTypeParameter variableDecl && variableDecl.type != null && variableDecl.type.tsym != null) {
			return this.bindings.getBinding(variableDecl.type.tsym, variableDecl.type);
		}
		if (tree instanceof JCModuleDecl variableDecl && variableDecl.sym != null && variableDecl.sym.type instanceof ModuleType mtt) {
			return this.bindings.getModuleBinding(variableDecl);
		}
		return null;
	}

	@Override
	IVariableBinding resolveVariable(EnumConstantDeclaration enumConstant) {
		resolve();
		if (this.converter.domToJavac.get(enumConstant) instanceof JCVariableDecl decl) {
			// the decl.type can be null when there are syntax errors
			if ((decl.type != null && !decl.type.isErroneous()) || this.isRecoveringBindings) {
				return this.bindings.getVariableBinding(decl.sym);
			}
		}
		return null;
	}

	@Override
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		resolve();
		if (this.converter.domToJavac.get(variable) instanceof JCVariableDecl decl) {
			// the decl.type can be null when there are syntax errors
			if ((decl.type != null && !decl.type.isErroneous()) || this.isRecoveringBindings) {
				return this.bindings.getVariableBinding(decl.sym);
			}
		}
		return null;
	}

	@Override
	public IPackageBinding resolvePackage(PackageDeclaration decl) {
		resolve();
		if (this.converter.domToJavac.get(decl) instanceof JCPackageDecl jcPackageDecl) {
			return this.bindings.getPackageBinding(jcPackageDecl.packge);
		}
		return null;
	}

	@Override
	public ITypeBinding resolveExpressionType(Expression expr) {
		resolve();
		if (expr instanceof SimpleName name) {
			IBinding binding = resolveName(name);
			// binding can be null when the code has syntax errors
			if (binding == null || (binding.isRecovered() && !this.isRecoveringBindings)) {
				return null;
			}
			switch (binding) {
			case IVariableBinding variableBinding: return variableBinding.getType();
			case ITypeBinding typeBinding: return typeBinding;
			case IMethodBinding methodBinding: return methodBinding.getReturnType();
			default:
				return null;
			}
		}
		var jcTree = this.converter.domToJavac.get(expr);
		if (jcTree instanceof JCExpression expression
			&& isTypeOfType(expression.type)
			&& !expression.type.isErroneous()) {
			var res = this.bindings.getTypeBinding(expression.type);
			if (res != null) {
				return res;
			}
		}
		if (jcTree instanceof JCMethodInvocation javacMethodInvocation) {
			if (javacMethodInvocation.meth.type instanceof MethodType methodType) {
				return this.bindings.getTypeBinding(methodType.getReturnType());
			} else if (javacMethodInvocation.meth.type instanceof ErrorType errorType)  {
				if (errorType.getOriginalType() instanceof MethodType methodType) {
					return this.bindings.getTypeBinding(methodType.getReturnType());
				}
			}
			return null;
		}
		if (jcTree instanceof JCNewClass newClass
			&& newClass.type != null
			&& Symtab.instance(this.context).errSymbol == newClass.type.tsym) {
			jcTree = newClass.getIdentifier();
		}
		if (jcTree instanceof JCFieldAccess jcFieldAccess) {
			if (jcFieldAccess.type instanceof PackageType) {
				return null;
			}
			return this.bindings.getTypeBinding(jcFieldAccess.type.isErroneous() ? jcFieldAccess.sym.type : jcFieldAccess.type);
		}
		if (jcTree instanceof JCVariableDecl jcVariableDecl) {
			if (jcVariableDecl.type != null) {
				return this.bindings.getTypeBinding(jcVariableDecl.type);
			} else {
				return null;
			}
		}
		if (jcTree instanceof JCTypeCast jcCast && jcCast.getType() != null) {
			return this.bindings.getTypeBinding(jcCast.getType().type);
		}
		if (jcTree instanceof JCLiteral jcLiteral && jcLiteral.type.isErroneous()) {
			if (jcLiteral.typetag == TypeTag.CLASS) {
				return resolveWellKnownType("java.lang.String");
			} else if (jcLiteral.typetag == TypeTag.BOT) {
				return this.bindings.getTypeBinding(com.sun.tools.javac.code.Symtab.instance(this.context).botType);
			}
			return resolveWellKnownType(jcLiteral.typetag.name().toLowerCase());
		}
		if (jcTree instanceof JCExpression jcExpr) {
			if (jcExpr.type instanceof PackageType) {
				return null;
			}
			Symbol recoveredSymbol = getRecoveredSymbol(jcExpr.type);
			if (recoveredSymbol != null) {
				IBinding recoveredBinding = this.bindings.getBinding(recoveredSymbol, recoveredSymbol.type);
				return switch (recoveredBinding) {
					case IVariableBinding variableBinding -> variableBinding.getType();
					case ITypeBinding typeBinding -> typeBinding;
					case IMethodBinding methodBinding -> methodBinding.getReturnType();
					default -> null;
				};
			}
			if (jcExpr.type != null) {
				var res = this.bindings.getTypeBinding(jcExpr.type);
				if (res != null) {
					return res;
				}
			}
			// workaround Javac missing bindings in some cases
			if (expr instanceof ClassInstanceCreation classInstanceCreation) {
				return createRecoveredTypeBinding(classInstanceCreation.getType());
			}
		}
		return null;
	}

	@Override
	IMethodBinding resolveConstructor(ClassInstanceCreation expression) {
		return (IMethodBinding)resolveCached(expression, (n) -> resolveConstructorImpl((ClassInstanceCreation)n));
	}

	/**
	 * 
	 * @param t the type to check
	 * @return whether this is actually a type (returns
	 * {@code false} for things like {@link PackageType},
	 * {@link MethodType}...
	 */
	public static boolean isTypeOfType(com.sun.tools.javac.code.Type t) {
		return t == null ? false :
			switch (t.getKind()) {
				case PACKAGE, MODULE, EXECUTABLE, OTHER -> false;
				default -> true;
			};
	}

	private IMethodBinding resolveConstructorImpl(ClassInstanceCreation expression) {
		resolve();
		if (this.converter.domToJavac.get(expression) instanceof JCNewClass jcExpr) {
			if (jcExpr.constructor != null && !jcExpr.constructor.type.isErroneous()) {
				return this.bindings.getMethodBinding(jcExpr.constructor.type.asMethodType(), (MethodSymbol)jcExpr.constructor, jcExpr.type, false);
			}
		}
		ITypeBinding type = resolveType(expression.getType());
		if (type != null) {
			List<ITypeBinding> givenTypes = ((List<Expression>)expression.arguments()).stream()
					.map(this::resolveExpressionType)
					.toList();
			boolean hasTrailingNull;
			boolean matchExactParamCount = false;
			do {
				hasTrailingNull = !givenTypes.isEmpty() && givenTypes.getLast() == null;
				// try just checking by known args
				// first filter by args count
				var matchExactParamCountFinal = matchExactParamCount;
				var finalGivenTypes = givenTypes;
				var candidates = Arrays.stream(type.getDeclaredMethods())
					.filter(IMethodBinding::isConstructor)
					.filter(other -> matchExactParamCountFinal ? other.getParameterTypes().length == finalGivenTypes.size() : other.getParameterTypes().length >= finalGivenTypes.size())
					.toList();
				if (candidates.size() == 1) {
					return candidates.get(0);
				}
				if (candidates.size() > 1 && expression.arguments().size() > 0) {
					// then try filtering by arg types
					var typeFilteredCandidates = candidates.stream()
						.filter(other -> matchTypes(finalGivenTypes, other.getParameterTypes()))
						.toList();
					if (typeFilteredCandidates.size() == 1) {
						return typeFilteredCandidates.get(0);
					}
				}
				if (hasTrailingNull) {
					givenTypes = givenTypes.subList(0, givenTypes.size() - 1);
					matchExactParamCount = true;
				}
			} while (hasTrailingNull);
		}
		return null;
	}

	private boolean matchTypes(List<ITypeBinding> givenTypes, ITypeBinding[] expectedTypes) {
		for (int i = 0; i < Math.min(givenTypes.size(), expectedTypes.length); i++) {
			ITypeBinding givenType = givenTypes.get(i);
			ITypeBinding expectedType = expectedTypes[i];
			if (givenType != null) {
				if (!givenType.isAssignmentCompatible(expectedType)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	IMethodBinding resolveConstructor(ConstructorInvocation invocation) {
		return (IMethodBinding)resolveCached(invocation, (n) -> resolveConstructorImpl((ConstructorInvocation)n));
	}

	private IMethodBinding resolveConstructorImpl(ConstructorInvocation invocation) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(invocation);
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(ident.type != null && ident.type.getKind() == TypeKind.EXECUTABLE ? ident.type.asMethodType() : methodSymbol.type.asMethodType(), methodSymbol, null, false);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, null, false);
		}
		return null;
	}

	public Types getTypes() {
		return Types.instance(this.context);
	}

	private java.util.List<TypeSymbol> getTypeArguments(final Name name) {
		if (name.getParent() instanceof SimpleType simpleType) {
			return getTypeArguments(simpleType);
		}
		if (name.getParent() instanceof MethodInvocation methodInvocation && name == methodInvocation.getName()) {
			return getTypeArguments(methodInvocation);
		}
		return null;
	}

	private java.util.List<TypeSymbol> getTypeArguments(final Type type) {
		if (type instanceof SimpleType simpleType
				&& simpleType.getParent() instanceof ParameterizedType paramType
				&& paramType.getType() == simpleType) {
			java.util.List<org.eclipse.jdt.core.dom.Type> typeArguments = paramType.typeArguments();

			if (typeArguments == null) {
				return null;
			}
			return typeArguments.stream() //
				.map(a -> {
					JCTree tree = this.converter.domToJavac.get(a);
					if (tree == null) {
						return null;
					}
					if (tree instanceof JCIdent ident && ident.sym instanceof TypeSymbol typeSymbol) {
						return typeSymbol;
					}
					if (tree instanceof JCFieldAccess access && access.sym instanceof TypeSymbol typeSymbol) {
						return typeSymbol;
					}
					return null;
				}) //
				.collect(Collectors.toList());

		}
		return null;
	}

	private java.util.List<TypeSymbol> getTypeArguments(final MethodInvocation methodInvocation) {
		java.util.List<org.eclipse.jdt.core.dom.Type> typeArguments = methodInvocation.typeArguments();
		if (typeArguments == null) {
			return null;
		}
		return typeArguments.stream() //
			.map(a -> {
				JCTree tree = this.converter.domToJavac.get(a);
				if (tree == null) {
					return null;
				}
				if (tree instanceof JCIdent ident && ident.sym instanceof TypeSymbol typeSymbol) {
					return typeSymbol;
				}
				if (tree instanceof JCFieldAccess access && access.sym instanceof TypeSymbol typeSymbol) {
					return typeSymbol;
				}
				return null;
			}) //
			.collect(Collectors.toList());
	}

	IModuleBinding resolveModule(ModuleDeclaration module) {
		return (IModuleBinding)resolveCached(module, (n) -> resolveModuleImpl((ModuleDeclaration)n));
	}

	private IBinding resolveModuleImpl(ModuleDeclaration module) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(module);
		if( javacElement instanceof JCModuleDecl jcmd) {
			Object o = jcmd.sym.type;
			if( o instanceof ModuleType mt ) {
				return this.bindings.getModuleBinding(mt);
			}
		}
		return null;
	}

	/**
	 * Returns the constant value or the binding that a Javac attribute represents.
	 *
	 * See a detailed explanation of the returned value: {@link org.eclipse.jdt.core.dom.IMethodBinding#getDefaultValue()}
	 *
	 * @param attribute the javac attribute
	 * @return the constant value or the binding that a Javac attribute represents
	 */
	public Object getValueFromAttribute(Attribute attribute) {
		if (attribute == null) {
			return null;
		}
		if (attribute instanceof Attribute.Constant constant) {
			return constant.value;
		} else if (attribute instanceof Attribute.Class clazz) {
			return this.bindings.getTypeBinding(clazz.classType);
		} else if (attribute instanceof Attribute.Enum enumm) {
			return this.bindings.getVariableBinding(enumm.value);
		} else if (attribute instanceof Attribute.Array array) {
			return Stream.of(array.values) //
					.map(nestedAttr -> {
						if (nestedAttr instanceof Attribute.Constant constant) {
							return constant.value;
						} else if (nestedAttr instanceof Attribute.Class clazz) {
							return this.bindings.getTypeBinding(clazz.classType);
						} else if (nestedAttr instanceof Attribute.Enum enumerable) {
							return this.bindings.getVariableBinding(enumerable.value);
						}
						throw new IllegalArgumentException("Unexpected attribute type: " + nestedAttr.getClass().getCanonicalName());
					}) //
					.toArray(Object[]::new);
		}
		throw new IllegalArgumentException("Unexpected attribute type: " + attribute.getClass().getCanonicalName());
	}

	@Override
	IBinding resolveImport(ImportDeclaration importDeclaration) {
		return resolveCached(importDeclaration, (n) -> resolveImportImpl((ImportDeclaration)n));
	}

	private IBinding resolveImportImpl(ImportDeclaration importDeclaration) {
		var javac = this.converter.domToJavac.get(importDeclaration.getName());
		if (javac instanceof JCFieldAccess fieldAccess) {
			if (fieldAccess.sym != null) {
				return this.bindings.getBinding(fieldAccess.sym, null);
			}
			if (importDeclaration.isStatic()) {
				com.sun.tools.javac.code.Type type = fieldAccess.getExpression().type;
				if (type != null) {
					IBinding binding = Arrays.stream(this.bindings.getTypeBinding(type).getDeclaredMethods())
						.filter(method -> Objects.equals(fieldAccess.getIdentifier().toString(), method.getName()))
						.findAny()
						.orElse(null);
					if (binding == null) {
						binding = Arrays.stream(this.bindings.getTypeBinding(type).getDeclaredFields()).filter(
								field -> Objects.equals(fieldAccess.getIdentifier().toString(), field.getName()))
								.findAny().orElse(null);
					}
					return binding;
				}
			}
		}
		return null;
	}

	@Override
	public ITypeBinding resolveWellKnownType(String typeName) {
		resolve(); // could be skipped, but this method is used by ReconcileWorkingCopyOperation to generate errors
		com.sun.tools.javac.code.Symtab symtab = com.sun.tools.javac.code.Symtab.instance(this.context);
		com.sun.tools.javac.code.Type type = switch (typeName) {
		case "byte", "java.lang.Byte" -> symtab.byteType;
		case "char", "java.lang.Character" -> symtab.charType;
		case "double", "java.lang.Double" -> symtab.doubleType;
		case "float", "java.lang.Float" -> symtab.floatType;
		case "int", "java.lang.Integer" -> symtab.intType;
		case "long", "java.lang.Long" -> symtab.longType;
		case "short", "java.lang.Short" -> symtab.shortType;
		case "boolean", "java.lang.Boolean" -> symtab.booleanType;
		case "void", "java.lang.Void" -> symtab.voidType;
		case "java.lang.Object" -> symtab.objectType;
		case "java.lang.String" -> symtab.stringType;
		case "java.lang.StringBuffer" -> symtab.stringBufferType;
		case "java.lang.Throwable" -> symtab.throwableType;
		case "java.lang.Exception" -> symtab.exceptionType;
		case "java.lang.RuntimeException" -> symtab.runtimeExceptionType;
		case "java.lang.Error" -> symtab.errorType;
		case "java.lang.Class" -> symtab.classType;
		case "java.lang.Cloneable" -> symtab.cloneableType;
		case "java.io.Serializable" -> symtab.serializableType;
		default -> null;
		};
		if (type == null) {
			return null;
		}
		return this.bindings.getTypeBinding(type, true);
	}

	@Override
	IAnnotationBinding resolveAnnotation(Annotation annotation) {
		return (IAnnotationBinding)resolveCached(annotation, (n) -> resolveAnnotationImpl((Annotation)n));
	}

	IAnnotationBinding resolveAnnotationImpl(Annotation annotation) {
		resolve();
		IBinding recipient = null;
		if (annotation.getParent() instanceof AnnotatableType annotatable) {
			recipient = annotatable.resolveBinding();
		} else if (annotation.getParent() instanceof FieldDeclaration fieldDeclaration) {
			recipient = ((VariableDeclarationFragment)fieldDeclaration.fragments().get(0)).resolveBinding();
		} else if (annotation.getParent() instanceof TypeDeclaration td) {
			recipient = td.resolveBinding();
		}
		var javac = this.converter.domToJavac.get(annotation);
		if (javac instanceof JCAnnotation jcAnnotation) {
			return this.bindings.getAnnotationBinding(jcAnnotation.attribute, recipient);
		}
		return null;
	}

	@Override
	IBinding resolveReference(MethodRef ref) {
		return resolveCached(ref, (n) -> resolveReferenceImpl((MethodRef)n));
	}

	private IBinding resolveReferenceImpl(MethodRef ref) {
		resolve();
		DocTreePath path = this.converter.findDocTreePath(ref);
		if (path != null ) {
			Element e = JavacTrees.instance(this.context).getElement(path);
			if(e instanceof Symbol symbol) {
				IBinding r1 = this.bindings.getBinding(symbol, null);
				return r1;
			}
			TreePath dt = path.getTreePath();
			if( dt != null) {
				Tree t = dt.getLeaf();
				if( t instanceof JCMethodDecl jcmd) {
					MethodSymbol ms = jcmd.sym;
					IBinding r1 = ms == null ? null : this.bindings.getBinding(ms, jcmd.type);
					return r1;
				}
			}
		}
		if( ref.parameters() != null && ref.parameters().size() == 0) {
			// exhaustively search for a similar method ref
			DocTreePath[] possible = this.converter.searchRelatedDocTreePath(ref);
			if( possible != null ) {
				for( int i = 0; i < possible.length; i++ ) {
					Element e = JavacTrees.instance(this.context).getElement(possible[i]);
					if(e instanceof Symbol symbol) {
						IBinding r1 = this.bindings.getBinding(symbol, null);
						if( r1 != null )
							return r1;
					}
				}
			}
		}
		// 
		return null;
	}

	@Override
	IBinding resolveReference(MemberRef ref) {
		return resolveCached(ref, (n) -> resolveReferenceImpl((MemberRef)n));
	}

	private IBinding resolveReferenceImpl(MemberRef ref) {
		resolve();
		DocTreePath path = this.converter.findDocTreePath(ref);
		if (path != null && JavacTrees.instance(this.context).getElement(path) instanceof Symbol symbol) {
			return this.bindings.getBinding(symbol, null);
		}
		return null;
	}
	private static Symbol getRecoveredSymbol(com.sun.tools.javac.code.Type type) {
		if (type instanceof ErrorType) {
			try {
				Field candidateSymbolField = type.getClass().getField("candidateSymbol");
				candidateSymbolField.setAccessible(true);

				Object symbolFieldValue = candidateSymbolField.get(type);
				if (symbolFieldValue instanceof Symbol symbol) {
					return symbol;
				}
			} catch (NoSuchFieldException | IllegalAccessException unused) {
				// fall through to null
			}
		}
		return null;
	}

	@Override
	public WorkingCopyOwner getWorkingCopyOwner() {
		return this.owner;
	}

	@Override
	Object resolveConstantExpressionValue(Expression expression) {
		JCTree jcTree = this.converter.domToJavac.get(expression);
		if (jcTree instanceof JCLiteral literal) {
			return literal.getValue();
		}
		return TreeInfo.symbolFor(jcTree) instanceof VarSymbol varSymbol ? varSymbol.getConstantValue() : null;
	}
}
