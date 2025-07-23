/*******************************************************************************
 * Copyright (c) 2023,2025 Red Hat, Inc. and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.javac.dom.JavacAnnotationBinding;
import org.eclipse.jdt.internal.javac.dom.JavacErrorMethodBinding;
import org.eclipse.jdt.internal.javac.dom.JavacErrorTypeBinding;
import org.eclipse.jdt.internal.javac.dom.JavacLambdaBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMemberValuePairBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMethodBinding;
import org.eclipse.jdt.internal.javac.dom.JavacModuleBinding;
import org.eclipse.jdt.internal.javac.dom.JavacPackageBinding;
import org.eclipse.jdt.internal.javac.dom.JavacTypeBinding;
import org.eclipse.jdt.internal.javac.dom.JavacTypeVariableBinding;
import org.eclipse.jdt.internal.javac.dom.JavacVariableBinding;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.ClassFinder;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.RootPackageSymbol;
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
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
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
import com.sun.tools.javac.tree.JCTree.JCTypeIntersection;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCTypeUnion;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

/**
 * Deals with creation of binding model, using the <code>Symbol</code>s from Javac.
 * @implNote Cannot move to another package because parent class is package visible only
 */
public class JavacBindingResolver extends BindingResolver {

	private JavacTask javacTask; // TODO evaluate memory cost of storing the instance
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
		private Map<JavacAnnotationBinding, JavacAnnotationBinding> annotationBindings = new HashMap<>();
		public JavacAnnotationBinding getAnnotationBinding(Compound ann, IBinding recipient) {
			JavacAnnotationBinding newInstance = new JavacAnnotationBinding(ann, JavacBindingResolver.this, recipient) { };
			annotationBindings.putIfAbsent(newInstance, newInstance);
			return annotationBindings.get(newInstance);
		}
		//
		private Map<JavacMemberValuePairBinding, JavacMemberValuePairBinding> memberValuePairBindings = new HashMap<>();
		public JavacMemberValuePairBinding getMemberValuePairBinding(MethodSymbol key, Attribute value) {
			JavacMemberValuePairBinding newInstance = new JavacMemberValuePairBinding(key, value, JavacBindingResolver.this) { };
			memberValuePairBindings.putIfAbsent(newInstance, newInstance);
			return memberValuePairBindings.get(newInstance);
		}
		public JavacMemberValuePairBinding getDefaultMemberValuePairBinding(IMethodBinding defaultAnnotationMethod) {
			if (!defaultAnnotationMethod.isAnnotationMember()) {
				return null;
			}
			JavacMemberValuePairBinding newInstance = new JavacMemberValuePairBinding(defaultAnnotationMethod, JavacBindingResolver.this) { };
			memberValuePairBindings.putIfAbsent(newInstance, newInstance);
			return memberValuePairBindings.get(newInstance);
		}
		//
		private Map<JavacMethodBinding, JavacMethodBinding> methodBindings = new HashMap<>();
		public JavacMethodBinding getMethodBinding(MethodType methodType, MethodSymbol sym,
				com.sun.tools.javac.code.Type type,
				boolean isSynthetic, boolean isDeclaration,
				List<com.sun.tools.javac.code.Type> typeArgs) {
			if( isSynthetic ) {
				return getSyntheticMethodBinding(methodType, sym, type, typeArgs);
			} else {
				return getMethodBinding(methodType, sym, type, isDeclaration, typeArgs);
			}
		}

		public JavacMethodBinding getMethodBinding(MethodType methodType, MethodSymbol methodSymbol,
				com.sun.tools.javac.code.Type parentType, boolean isDeclaration,
				List<com.sun.tools.javac.code.Type> resolvedTypeArgs) {
			JavacMethodBinding newInstance = new JavacMethodBinding(methodType, methodSymbol, parentType, JavacBindingResolver.this, false, isDeclaration, resolvedTypeArgs) { };
			return insertAndReturn(newInstance);
		}
		public JavacMethodBinding getSyntheticMethodBinding(MethodType methodType, MethodSymbol methodSymbol,
				com.sun.tools.javac.code.Type parentType, List<com.sun.tools.javac.code.Type> resolvedTypeArgs) {
			JavacMethodBinding newInstance = new JavacMethodBinding(methodType, methodSymbol, parentType, JavacBindingResolver.this, true, false, resolvedTypeArgs) { };
			return insertAndReturn(newInstance);
		}
		public JavacMethodBinding getErrorMethodBinding(MethodType methodType, Symbol originatingSymbol, List<com.sun.tools.javac.code.Type> typeArgs) {
			JavacMethodBinding newInstance = new JavacErrorMethodBinding(originatingSymbol, methodType, JavacBindingResolver.this) { };
			return insertAndReturn(newInstance);
		}
		private JavacMethodBinding insertAndReturn(JavacMethodBinding newInstance) {
			methodBindings.putIfAbsent(newInstance, newInstance);
			return methodBindings.get(newInstance);
		}
		//
		private Map<JavacModuleBinding, JavacModuleBinding> moduleBindings = new HashMap<>();
		public JavacModuleBinding getModuleBinding(ModuleType moduleType) {
			JavacModuleBinding newInstance = new JavacModuleBinding(moduleType, JavacBindingResolver.this) { };
			moduleBindings.putIfAbsent(newInstance, newInstance);
			return moduleBindings.get(newInstance);
		}
		public JavacModuleBinding getModuleBinding(ModuleSymbol moduleSymbol) {
			JavacModuleBinding newInstance = new JavacModuleBinding(moduleSymbol, JavacBindingResolver.this) { };
			moduleBindings.putIfAbsent(newInstance, newInstance);
			return moduleBindings.get(newInstance);
		}
		public JavacModuleBinding getModuleBinding(JCModuleDecl moduleDecl) {
			JavacModuleBinding newInstance = new JavacModuleBinding(moduleDecl, JavacBindingResolver.this) { };
			// Overwrite existing
			moduleBindings.put(newInstance, newInstance);
			return moduleBindings.get(newInstance);
		}

		//
		private Map<JavacPackageBinding, JavacPackageBinding> packageBindings = new HashMap<>();
		public JavacPackageBinding getPackageBinding(PackageSymbol packageSymbol) {
			if( packageSymbol.owner instanceof PackageSymbol parentPack) {
				if( !(parentPack instanceof RootPackageSymbol) )
					getPackageBinding(parentPack);
			}
			JavacPackageBinding newInstance = new JavacPackageBinding(packageSymbol, JavacBindingResolver.this) { };
			return preferentiallyInsertPackageBinding(newInstance);
		}
		public JavacPackageBinding getPackageBinding(Name name) {
			String n = packageNameToString(name);
			if( n == null )
				return null;
			JavacPackageBinding newInstance = new JavacPackageBinding(n, JavacBindingResolver.this) {};
			return preferentiallyInsertPackageBinding(newInstance);
		}

		public JavacPackageBinding findExistingPackageBinding(Name name) {
			String n = name == null ? null : name.toString();
			if( n == null )
				return null;
			JavacPackageBinding newInstance = new JavacPackageBinding(n, JavacBindingResolver.this) {};
			String k = newInstance == null ? null : newInstance.getKey();
			if( k != null ) {
				JavacPackageBinding current = packageBindings.get(k);
				return current;
			}
			return null;
		}

		private String packageNameToString(Name name) {
			String n = null;
			if( name instanceof QualifiedName )
				n = name.toString();
			else if( name instanceof SimpleName) {
				if( name.getParent() instanceof QualifiedName qn) {
					if( qn.getName() == name ) {
						n = qn.toString();
					} else if( qn.getQualifier() == name) {
						n = name.toString();
					}
				}
			}
			return n;
		}

		private JavacPackageBinding preferentiallyInsertPackageBinding(JavacPackageBinding newest) {
			// A package binding may be created while traversing something as simple as a name.
			// The binding using name-only logic should be instantiated, but
			// when a proper symbol is found, it should be added to that object.
			if( newest != null ) {
				JavacPackageBinding current = packageBindings.get(newest);
				if( current == null ) {
					packageBindings.putIfAbsent(newest, newest);
				} else if( current.getPackageSymbol() == null && newest.getPackageSymbol() != null) {
					current.setPackageSymbol(newest.getPackageSymbol());
				}
				return packageBindings.get(newest);
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

			boolean likelyGeneric = false;
//			if( type instanceof ClassType ct && ct.isParameterized()) {
//				 List<com.sun.tools.javac.code.Type> typeArgs = ct.getTypeArguments();
//				 int size = typeArgs.size();
//				 for( int i = 0; i < size && !likelyGeneric; i++ ) {
//					 if( typeArgs.get(i) instanceof TypeVar) {
//						 likelyGeneric = true;
//					 }
//				 }
//			}
//
			return getTypeBinding(type.baseType() /* remove metadata for constant values */, likelyGeneric);
		}
		public JavacTypeBinding getTypeBinding(com.sun.tools.javac.code.Type type, boolean isGeneric) {
			return getTypeBinding(type, null, isGeneric);
		}
		public JavacTypeBinding getTypeBinding(com.sun.tools.javac.code.Type type, com.sun.tools.javac.code.Type[] alternatives, boolean isGeneric) {
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
					JavacTypeBinding newInstance = new JavacTypeBinding(originalType, type.tsym, alternatives, isGeneric, JavacBindingResolver.this) { };
					typeBinding.putIfAbsent(newInstance, newInstance);
					JavacTypeBinding jcb = typeBinding.get(newInstance);
					jcb.setRecovered(true);
					return jcb;
				} else if (errorType.tsym instanceof ClassSymbol classErrorSymbol &&
							Character.isJavaIdentifierStart(classErrorSymbol.getSimpleName().charAt(0))) {
					// non usable original type: try symbol
					JavacTypeBinding newInstance = new JavacTypeBinding(classErrorSymbol.type, classErrorSymbol, alternatives, isGeneric, JavacBindingResolver.this) { };
					typeBinding.putIfAbsent(newInstance, newInstance);
					JavacTypeBinding jcb = typeBinding.get(newInstance);
					jcb.setRecovered(true);
					return jcb;
				}
				// no type information  we could recover from
				return null;
			}
			if (!(type.tsym instanceof ClassSymbol sym && sym.classfile == null && sym.sourcefile == null)
					&& !type.isParameterized() && !type.isRaw() && type instanceof ClassType classType
					&& classType.interfaces_field == null) {
				// workaround faulty case of TypeMismatchQuickfixText.testMismatchingReturnTypeOnGenericMethod
				// interfaces/supertypes are not set which seem to imply that the compiler generated
				// a dummy type object that's not suitable for a binding.
				// Fail back to an hopefully better type
				type = type.tsym.type;
			}
			JavacTypeBinding newInstance = new JavacTypeBinding(type, type.tsym, alternatives, isGeneric, JavacBindingResolver.this) { };
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
		private Map<JavacVariableBinding, JavacVariableBinding> variableBindings = new HashMap<>();
		public JavacVariableBinding getVariableBinding(VarSymbol varSymbol) {
			if (varSymbol == null) {
				return null;
			}
			JavacVariableBinding newInstance = new JavacVariableBinding(varSymbol, JavacBindingResolver.this) { };
			variableBindings.putIfAbsent(newInstance, newInstance);
			return variableBindings.get(newInstance);
		}
		//
		private Map<JavacLambdaBinding, JavacLambdaBinding> lambdaBindings = new HashMap<>();
		public JavacLambdaBinding getLambdaBinding(JavacMethodBinding javacMethodBinding, LambdaExpression lambda) {
			JavacLambdaBinding newInstance = new JavacLambdaBinding(javacMethodBinding, lambda);
			lambdaBindings.putIfAbsent(newInstance, newInstance);
			return lambdaBindings.get(newInstance);
		}

		public IBinding getBinding(final Symbol owner, final com.sun.tools.javac.code.Type type) {
			Symbol recoveredSymbol = getRecoveredSymbol(type);
			if (recoveredSymbol != null) {
				return getBinding(recoveredSymbol, recoveredSymbol.type);
			}
			if (type != null && (type instanceof ErrorType || owner == null || owner.owner == null || owner.owner.type == com.sun.tools.javac.code.Type.noType)) {
				if (type.getOriginalType() instanceof MethodType missingMethodType) {
					return getErrorMethodBinding(missingMethodType, owner, null);
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
				var methodType = type instanceof com.sun.tools.javac.code.Type.MethodType aMethodType ? aMethodType :
					owner.type != null ? owner.type.asMethodType() :
					null;
				if (methodType != null) {
					return getMethodBinding(methodType, other, null, false, null);
				}
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
			binding = new ArrayList<>(this.typeBinding.values())
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
	private List<JCCompilationUnit> javacCompilationUnits;

	public JavacBindingResolver(IJavaProject javaProject, JavacTask javacTask, Context context, JavacConverter converter, WorkingCopyOwner owner, List<JCCompilationUnit> javacCompilationUnits) {
		this.javacTask = javacTask;
		this.context = context;
		this.javaProject = javaProject;
		this.converter = converter;
		this.owner = owner;
		this.javacCompilationUnits = javacCompilationUnits;
	}

	private void resolve() {
		if (this.symbolToDeclaration != null) {
			// already done and ready
			return;
		}
		final JavacTask tmpTask = this.javacTask;
		if( tmpTask == null ) {
			return;
		}
		synchronized (tmpTask) { // prevents from multiple `analyze` for the same task
			if( this.javacTask == null ) {
				return;
			}
			boolean alreadyAnalyzed = this.converter.domToJavac.values().stream().map(TreeInfo::symbolFor).anyMatch(Objects::nonNull);
			if (!alreadyAnalyzed) {
				// symbols not already present: analyze
				try {
					Iterable<? extends Element> elements;
					// long start = System.currentTimeMillis();
					if (this.javacTask instanceof JavacTaskImpl javacTaskImpl) {
						if (javacCompilationUnits != null && !javacCompilationUnits.isEmpty()) {
							Iterable<? extends CompilationUnitTree> trees = javacCompilationUnits;
							elements = javacTaskImpl.enter(trees);
						} else {
							elements = javacTaskImpl.enter();
						}
						elements = javacTaskImpl.analyze(elements);
//						long count = StreamSupport.stream(elements.spliterator(), false).count();
//						String name = elements.iterator().hasNext()
//								? elements.iterator().next().getSimpleName().toString()
//								: "";
//						ILog.get().info("enter/analyze elements=" + count + ", took: "
//								+ (System.currentTimeMillis() - start) + ", first=" + name);
					} else {
						elements = this.javacTask.analyze();
//						long count = StreamSupport.stream(elements.spliterator(), false).count();
//						String name = elements.iterator().hasNext()
//								? elements.iterator().next().getSimpleName().toString()
//								: "";
//						ILog.get().info("analyze elements=" + count + ", took: " + (System.currentTimeMillis() - start)
//								+ ", first=" + name);
					}
				} catch (IOException | Error | RuntimeException e) {
					ILog.get().error(e.getMessage(), e);
				}
			}
			// some cleanups to encourage garbage collection
			JavacCompilationUnitResolver.cleanup(context);
		}
		this.javacTask = null;
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

	public IBinding findBinding(String bindingKey) {
		return this.bindings.getBinding(bindingKey);
	}

	private void compoundListWithAction(HashSet<String> list, Function<String, String> f) {
		Iterator<String> it = new ArrayList<String>(list).iterator();
		while(it.hasNext()) {
			String transformed = f.apply(it.next());
			if( transformed != null && !list.contains(transformed)) {
				list.add(transformed);
			}
		}
	}

	public IBinding findUnresolvedBinding(String bindingKey) {
		boolean isUnresolved = bindingKey.startsWith("Q") || bindingKey.startsWith("+Q") || bindingKey.startsWith("-Q");
		if( !isUnresolved) {
			return findBinding(bindingKey);
		}

		final boolean bkExtends = bindingKey.startsWith("+");
		final boolean bkSuper = bindingKey.startsWith("-");

		String withoutSuperExtends = bindingKey.startsWith("+") || bindingKey.startsWith("-") ? bindingKey.substring(1) : bindingKey;

		HashSet<String> validNames = new HashSet<String>();
		validNames.add(bindingKey);
		compoundListWithAction(validNames, x -> x.replaceAll("\\.", "/"));
		compoundListWithAction(validNames, x -> x.endsWith(";") ? x.substring(0, x.length() - 1) : null);
		compoundListWithAction(validNames, x -> x.startsWith("+") || x.startsWith("-") ? x.substring(1) : null);
		compoundListWithAction(validNames, x -> x.lastIndexOf(".", x.length() - 1) != -1 ? x.substring(x.lastIndexOf(".") + 1) : null);
		compoundListWithAction(validNames, x -> x.startsWith("Q") ? x.substring(1) : null);
		compoundListWithAction(validNames, x -> x.contains("<Q") ? x.replaceAll("<Q", "<") : null);
		compoundListWithAction(validNames, x -> x.startsWith("+Q") ? x.replaceAll("\\+Q", "? extends ") : null);
		compoundListWithAction(validNames, x -> x.startsWith("-Q") ? x.replaceAll("-Q", "? super ") : null);
		String bindingKeySimpleName = Signature.getSignatureSimpleName(withoutSuperExtends);
		validNames.add(bindingKeySimpleName);

		Collection<JavacTypeBinding> c = new ArrayList<>(this.bindings.typeBinding.values());
		int matchesKey = 0x80;
		int matchesSimpleName = 0x40;
		int matchesSuperExtends = 0x10;
		record Pair(JavacTypeBinding binding, int weight) {};
		List<Pair> collector = new ArrayList<Pair>();

		c.stream().forEach(x -> {
			int total = 0;
			String k = x.getKey();
			if( validNames.contains(k))
				total += matchesKey;
			String n = x.getName();
			if( validNames.contains(n)) {
				total += matchesSimpleName;
			}
			if( bkExtends || bkSuper ) {
				if( x.isWildcardType() && x.getBound() != null ) {
					if( bkExtends && x.isUpperbound()) {
						total += matchesSuperExtends;
					} else if( bkSuper && !x.isUpperbound() ) {
						total += matchesSuperExtends;
					}
				}
			}
			if( total > 0 )
				collector.add(new Pair(x, total));
		});

		Collections.sort(collector, (o1, o2) -> o2.weight - o1.weight);
		return collector.size() > 0 ? collector.get(0).binding : null;
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
			IBinding b = this.getFieldAccessBinding(access);
			return b instanceof ITypeBinding tb ? tb : null;
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
		if (jcTree instanceof JCTypeUnion unionType) {
			com.sun.tools.javac.code.Type[] alternativesArray = new com.sun.tools.javac.code.Type[unionType.alternatives.size()];
			for (int i = 0 ; i < alternativesArray.length; i++) {
				alternativesArray[i] = unionType.alternatives.get(i).type;
			}
			return this.bindings.getTypeBinding(unionType.type, alternativesArray, false);
		}
		if (jcTree instanceof JCTypeIntersection intersectionType) {
			com.sun.tools.javac.code.Type[] alternativesArray = new com.sun.tools.javac.code.Type[intersectionType.bounds.size()];
			for (int i = 0 ; i < alternativesArray.length; i++) {
				alternativesArray[i] = intersectionType.bounds.get(i).type;
			}
			return this.bindings.getTypeBinding(intersectionType.type, alternativesArray, false);
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
					Collection<JCTree> jctr = JavacBindingResolver.this.converter.domToJavac.values();
					Iterator<JCTree> jcit = jctr.iterator();
					while(jcit.hasNext()) {
						Object o = jcit.next();
						if( o instanceof CompilationUnit cuuu ) {
							// Should this even be here? Migrated from prior version
							PackageDeclaration pd = cuuu.getPackage();
							if( pd != null ) {
								IPackageBinding pack = pd.resolveBinding();
								if( pack != null )
									return pack;
							}
						}
						if( o instanceof JCCompilationUnit jcuu) {
							JCPackageDecl jcpd = jcuu.getPackage();
							if( jcpd != null ) {
								JavacPackageBinding pckbind = bindings.getPackageBinding(jcpd.packge);
								if( pckbind != null ) {
									return pckbind;
								}
							}
						}
					}
				}
				return bindings.getPackageBinding(Symtab.instance(context).rootPackage);
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
		if (javacNode instanceof JCClassDecl jcClassDecl && (javacNode.type != null && "<any?>".equals(javacNode.type.toString()))) {
			return new JavacErrorTypeBinding(javacNode.type, javacNode.type.tsym, null, true, JavacBindingResolver.this, jcClassDecl.sym);
		}
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.type, true);
		}
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.sym != null && jcClassDecl.sym.type != null) {
			return this.bindings.getTypeBinding(jcClassDecl.sym.type, true);
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
	@Override
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
		List<com.sun.tools.javac.code.Type> typeArgs = null;
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			typeArgs = List.of();
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
				var res = this.bindings.getMethodBinding(methodType, methodSymbol, null, false, typeArgs);
				if (res != null) {
					return res;
				}
			}
		}
		var sym = javacElement instanceof JCIdent ident ? ident.sym :
			javacElement instanceof JCFieldAccess fieldAccess ? fieldAccess.sym :
				null;

		// Let's handle error types first
		if (type instanceof ErrorType errorType ) {
			com.sun.tools.javac.code.Type original = errorType;
			while(original instanceof ErrorType et && original != et.getOriginalType()) {
				original = et.getOriginalType();
			}

			if( original instanceof ForAll fa) {
				original = fa.asMethodType();
			}
			if( original instanceof MethodType methodType) {
				if (sym.owner instanceof TypeSymbol typeSymbol) {
					Iterator<Symbol> methods = typeSymbol.members().getSymbolsByName(sym.getSimpleName(), m -> m instanceof MethodSymbol && methodType.equals(m.type)).iterator();
					if (methods.hasNext()) {
						return this.bindings.getMethodBinding(methodType, (MethodSymbol)methods.next(), null, false, typeArgs);
					}
				}
				return this.bindings.getErrorMethodBinding(methodType, sym, typeArgs);
			}
		}



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
			return this.bindings.getMethodBinding(methodType, methodSymbol, parentType, false, typeArgs);
		}
		if (type == null && sym instanceof MethodSymbol methodSym && methodSym.type instanceof ForAll methodTemplateType) {
			// build type from template
			Map<TypeVar, com.sun.tools.javac.code.Type> resolutionMapping = new HashMap<>();
			var templateParameters = methodTemplateType.getTypeVariables();
			if( typeArgs != null ) {
				for (int i = 0; i < typeArgs.size() && i < templateParameters.size(); i++) {
					resolutionMapping.put(templateParameters.get(i), typeArgs.get(i));
				}
			}
			MethodType methodType = new MethodType(
					methodTemplateType.asMethodType().getParameterTypes().map(t -> applyType(t, resolutionMapping)),
					applyType(methodTemplateType.asMethodType().getReturnType(), resolutionMapping),
					methodTemplateType.asMethodType().getThrownTypes().map(t -> applyType(t, resolutionMapping)),
					methodTemplateType.tsym);
			return this.bindings.getMethodBinding(methodType, methodSym, methodSym.owner.type, false, typeArgs);
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
		if (sym instanceof MethodSymbol && sym.type instanceof MethodType) {
			return (IMethodBinding)this.bindings.getBinding(sym, sym.type);
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
		if (javacElement instanceof JCMethodDecl methodDecl && !(methodDecl.type instanceof ErrorType)) {
			if (methodDecl.type != null ) {
				return this.bindings.getMethodBinding(methodDecl.type.asMethodType(), methodDecl.sym, null, true, null);
			}
			if (methodDecl.sym instanceof MethodSymbol methodSymbol && methodSymbol.type != null) {
				return this.bindings.getMethodBinding(methodSymbol.type.asMethodType(), methodSymbol, null, true, null);
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
			List<com.sun.tools.javac.code.Type> typeArgs = streamOfTreeType(memberRef.getTypeArguments());
			if (memberRef.referentType != null && memberRef.referentType instanceof MethodType) {
				return this.bindings.getMethodBinding(memberRef.referentType.asMethodType(), methodSymbol, null, false, typeArgs);
			}
			if (methodSymbol.type instanceof MethodType) {
				return this.bindings.getMethodBinding(methodSymbol.type.asMethodType(), methodSymbol, null, false, typeArgs);
			}
		}
		return null;
	}

	private List<com.sun.tools.javac.code.Type> streamOfTreeType(List<? extends JCTree> items ) {
		return items != null ? items.stream().map(x -> x.type).toList() : new ArrayList<com.sun.tools.javac.code.Type>();
	}

	@Override
	IMethodBinding resolveMember(AnnotationTypeMemberDeclaration member) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(member);
		if (javacElement instanceof JCMethodDecl methodDecl) {
			List<com.sun.tools.javac.code.Type> typeArgs = streamOfTreeType(methodDecl.getTypeParameters());
			return this.bindings.getMethodBinding(methodDecl.type.asMethodType(), methodDecl.sym, null, true, typeArgs);
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
		if(javacElement instanceof JCNewClass jcExpr && !jcExpr.constructor.type.isErroneous()) {
			List<com.sun.tools.javac.code.Type> typeArgs = streamOfTreeType(jcExpr.typeargs);
			return this.bindings.getMethodBinding(jcExpr.constructor.type.asMethodType(), (MethodSymbol)jcExpr.constructor, null, true, typeArgs);
		}
		return null;
	}

	@Override
	IMethodBinding resolveConstructor(SuperConstructorInvocation expression) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(expression);
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			if (ident.type != null && (ident.type instanceof MethodType || ident.type instanceof ForAll)) {
				return this.bindings.getMethodBinding(ident.type.asMethodType(), methodSymbol, null, false, null);
			} else if (methodSymbol.asType() instanceof MethodType || methodSymbol.asType() instanceof ForAll) {
				return this.bindings.getMethodBinding(methodSymbol.asType().asMethodType(), methodSymbol, null, false, null);
			}
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, null, false, null);
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
			return this.bindings.getMethodBinding(ident.type.asMethodType(), methodSymbol, null, false, null);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol
				&& fieldAccess.type != null /* when there are syntax errors */) {
			return this.bindings.getMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, null, false, null);
		}
		return null;
	}

	IBinding resolveCached(ASTNode node, Function<ASTNode, IBinding> l) {
		resolve();
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
		if (name.getParent() instanceof MemberRef memberRef) {
			resolveReference(memberRef); // initialize symbols on Javadoc
		}
		if (name.getParent() instanceof MethodRef methodRef) {
			resolveReference(methodRef);
		}

		// first, prefer parent if appropriate
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
		if (name.getLocationInParent() == MethodInvocation.NAME_PROPERTY && name.getParent() instanceof MethodInvocation method) {
			return resolveMethod(method);
		}

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
		if( ps != null && ps.exists()) {
			return this.bindings.getPackageBinding(ps);
		}
		if( isPackageName(name)) {
			return this.bindings.getPackageBinding(name);
		}
		if( tree instanceof JCIdent jcid && jcid.sym instanceof ClassSymbol && jcid.type instanceof ErrorType) {
			IBinding b = this.bindings.findExistingPackageBinding(name);
			if( b != null )
				return b;
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

//				IBinding ret = resolveNameToJavac(name, tree);
//				if (ret != null) {
//					return ret;
//				}
//
//				// That didn't work... checking field access
//				IBinding fieldAccessBinding = this.getFieldAccessBinding(jcfa);
//				if( fieldAccessBinding != null ) {
//					return fieldAccessBinding;
//				}
			}
		}
		if( tree != null ) {
			// Looks duplicate to top of method, but is not. Must remain.
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
				return jcfa.sym instanceof PackageSymbol psym && psym.exists();
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
				&& errorType.getOriginalType() instanceof ErrorType
				&& !isRecoveringBindings()) {
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
		if (tree instanceof JCFieldAccess fieldAccess) {
			return this.getFieldAccessBinding(fieldAccess);
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
		if (tree instanceof JCModuleDecl variableDecl && variableDecl.sym != null && variableDecl.sym.type instanceof ModuleType) {
			return this.bindings.getModuleBinding(variableDecl);
		}
		return null;
	}

	@Override
	IVariableBinding resolveVariable(EnumConstantDeclaration enumConstant) {
		resolve();
		if (this.converter.domToJavac.get(enumConstant) instanceof JCVariableDecl decl) {
			// the decl.type can be null when there are syntax errors
			if ((decl.type != null && !decl.type.isErroneous()) || this.isRecoveringBindings()) {
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
			if ((decl.type != null && !decl.type.isErroneous()) || this.isRecoveringBindings()) {
				if (decl.name != Names.instance(this.context).error) { // cannot recover if name is error
					return this.bindings.getVariableBinding(decl.sym);
				}
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
			if (binding == null || (binding.isRecovered() && !this.isRecoveringBindings())) {
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
			if (newClass.encl != null) {
				String className = newClass.clazz instanceof JCTypeApply typeApply ? typeApply.clazz.toString() : newClass.clazz.toString();
				ITypeBinding enclosingTypeBinding = resolveExpressionType(((ClassInstanceCreation)expr).getExpression());
				List<ITypeBinding> potentialTypes = Stream.of(enclosingTypeBinding.getDeclaredTypes()).filter(innerType -> {
					String cleanedName = innerType.getName();
					if (cleanedName.endsWith(">")) {
						cleanedName = cleanedName.substring(0, cleanedName.lastIndexOf("<"));
					}
					return className.equals(cleanedName);
				}).toList();
				if (!potentialTypes.isEmpty()) {
					return potentialTypes.get(0);
				}
			}
			jcTree = newClass.getIdentifier();
		}
		if (jcTree instanceof JCFieldAccess jcFieldAccess) {
			if (jcFieldAccess.type instanceof PackageType) {
				return null;
			}
			if (expr instanceof SuperFieldAccess) {
				return this.bindings.getTypeBinding(jcFieldAccess.selected.type);
			}
			if (jcFieldAccess.type != null && !jcFieldAccess.type.isErroneous()) {
				return this.bindings.getTypeBinding(jcFieldAccess.type);
			}
			if (jcFieldAccess.sym != null) {
				return this.bindings.getTypeBinding(jcFieldAccess.sym.type);
			}
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
		if (jcTree instanceof JCLiteral jcLiteral && jcLiteral.type != null && jcLiteral.type.isErroneous()) {
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
				return classInstanceCreation.getType().resolveBinding();
//				return createRecoveredTypeBinding(classInstanceCreation.getType());
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
			if (jcExpr.constructor != null && !jcExpr.constructorType.isErroneous()) {
				List<com.sun.tools.javac.code.Type> javacTypeArgs =
						jcExpr.getTypeArguments().stream().map(jc -> jc.type).toList();
				return this.bindings.getMethodBinding(jcExpr.constructorType.asMethodType(), (MethodSymbol)jcExpr.constructor, jcExpr.type, false, javacTypeArgs);
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
			MethodType mt = ident.type != null && ident.type.getKind() == TypeKind.EXECUTABLE ? ident.type.asMethodType() : methodSymbol.type.asMethodType();
			return this.bindings.getMethodBinding(mt, methodSymbol, null, false, null);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, null, false, null);
		}
		return null;
	}

	public Types getTypes() {
		return Types.instance(this.context);
	}

	@Override
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
					}).toArray(Object[]::new);
		} else if( attribute instanceof Attribute.Error) {
			return null;
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
					ITypeBinding typeBinding = this.bindings.getTypeBinding(type);
					if (typeBinding != null) {
						IBinding binding = Arrays.stream(typeBinding.getDeclaredMethods())
								.filter(method -> Objects.equals(fieldAccess.getIdentifier().toString(), method.getName()))
								.findAny()
								.orElse(null);
						if (binding == null) {
							binding = Arrays.stream(typeBinding.getDeclaredFields()).filter(
									field -> Objects.equals(fieldAccess.getIdentifier().toString(), field.getName()))
									.findAny().orElse(null);
						}
						return binding;
					}
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
			ClassFinder finder = ClassFinder.instance(context);
			Modules modules = Modules.instance(context);
			Names names = Names.instance(context);
			if (finder != null && modules != null && names != null) {
				try {
					ClassSymbol sym = finder.loadClass(modules.getDefaultModule(), names.fromString(typeName));
					if (sym != null) {
						type = sym.type;
					}
				} catch (CompletionFailure failure) {
					// do nothing, class not found
				}
			}
		}
		return type != null ? this.bindings.getTypeBinding(type, true) : null;
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
	IMemberValuePairBinding resolveMemberValuePair(MemberValuePair memberValuePair) {
		resolve();
		if (this.converter.domToJavac.get(memberValuePair) instanceof JCAssign assign &&
			assign.lhs instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return this.bindings.getMemberValuePairBinding(methodSymbol, null);
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

	private IBinding getFieldAccessBinding(JCFieldAccess fieldAccess) {
		JCFieldAccess jcfa2 = (fieldAccess.sym == null && fieldAccess.selected instanceof JCFieldAccess jcfa3) ? jcfa3 : fieldAccess;
		if( jcfa2.sym != null ) {
			com.sun.tools.javac.code.Type typeToUse = jcfa2.type;
			IBinding bRet = this.bindings.getBinding(jcfa2.sym, typeToUse);
			if(bRet == null && jcfa2.selected instanceof JCTypeApply) {
				// ??? no idea if this is a good answer
				typeToUse = jcfa2.sym.type;
			}

			if( jcfa2 != fieldAccess && bRet instanceof ITypeBinding itb ) {
				String fieldAccessIdentifier = fieldAccess.getIdentifier().toString();
				// If we changed the field access, we need to go one generation lower
				Function<IBinding[], IBinding> func = bindings -> {
					for( int i = 0; i < bindings.length; i++ ) {
						String childName = bindings[i].getName();
						if( childName.equals(fieldAccessIdentifier)) {
							return bindings[i];
						}
					}
					return null;
				};
				IBinding ret = func.apply(itb.getDeclaredTypes());
				if( ret != null )
					return ret;
				ret = func.apply(itb.getDeclaredFields());
				if( ret != null )
					return ret;
				ret = func.apply(itb.getDeclaredMethods());
				if( ret != null )
					return ret;
			}
			return bRet;
		}
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
		if (jcTree == null && expression.getParent() != null) {
			jcTree = this.converter.domToJavac.get(expression.getParent());
			if (jcTree == null) {
				return null;
			}
		}
		return TreeInfo.symbolFor(jcTree) instanceof VarSymbol varSymbol ? varSymbol.getConstantValue() : null;
	}
	@Override
	boolean resolveBoxing(Expression expression) {
		// TODO need to handle many different things here, very rudimentary
		if( expression.getParent() instanceof MethodInvocation mi) {
			IMethodBinding mb = resolveMethod(mi);
			int foundArg = -1;
			if( mb != null ) {
				for( int i = 0; i < mi.arguments().size() && foundArg == -1; i++ ) {
					if( mi.arguments().get(i) == expression) {
						foundArg = i;
					}
				}
				if( foundArg != -1 ) {
					ITypeBinding[] tbs = mb.getParameterTypes();
					if( tbs.length > foundArg) {
						ITypeBinding foundType = tbs[foundArg];
						if( expression instanceof NumberLiteral nl) {
							if( isBoxedVersion(nl, foundType)) {
								return true;
							}
						} else {
							if( expression instanceof MethodInvocation inner) {
								JavacMethodBinding mbInner = (JavacMethodBinding)resolveMethod(inner);
								ITypeBinding retTypeInner = mbInner == null ? null : mbInner.getReturnType();
								if( isBoxedVersion(retTypeInner, foundType)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isBoxedVersion(NumberLiteral unboxed, ITypeBinding boxed) {
		if( boxed instanceof JavacTypeBinding boxedBind ) {
			String boxedString = boxedBind.typeSymbol == null ? null : boxedBind.typeSymbol.toString();
			if("java.lang.Integer".equals(boxedString)
					|| "java.lang.Float".equals(boxedString)
					|| "java.lang.Double".equals(boxedString)) {
				return true;
			}
		}
		return false;
	}

	private Map<String, String> boxingMap = null;
	private boolean isBoxedVersion(ITypeBinding unboxed, ITypeBinding boxed) {
		if( boxingMap == null ) {
			Map<String, String> m = new HashMap<String,String>();
			m.put("java.lang.Boolean", "boolean");
			m.put("java.lang.Byte", "byte");
			m.put("java.lang.Character", "char");
			m.put("java.lang.Float", "float");
			m.put("java.lang.Integer", "int");
			m.put("java.lang.Long", "long");
			m.put("java.lang.Short", "short");
			m.put("java.lang.Double", "double");
			boxingMap = m;
		}
		if( boxed instanceof JavacTypeBinding boxedBind && unboxed instanceof JavacTypeBinding unboxedBind) {
			String boxedString = boxedBind.typeSymbol == null ? null : boxedBind.typeSymbol.toString();
			String unboxedString = unboxedBind.typeSymbol == null ? null : unboxedBind.typeSymbol.toString();
			// TODO very rudimentary, fix it, add more
			if( boxingMap.get(boxedString) != null ) {
				if( unboxedString.equals(boxingMap.get(boxedString))) {
					return true;
				}
			}
			// Alternate case, they might be converting some types
			if( boxingMap.keySet().contains(boxedString) && boxingMap.values().contains(unboxedString)) {
				return true;
			}
		}
		return false;
	}
	@Override
	boolean resolveUnboxing(Expression expression) {
		Type t = null;
		if( expression instanceof ClassInstanceCreation cic ) {
			t = cic.getType();
		}
		if( t != null && expression.getParent() instanceof MethodInvocation mi) {
			int foundArg = -1;
			if( mi != null ) {
				for( int i = 0; i < mi.arguments().size() && foundArg == -1; i++ ) {
					if( mi.arguments().get(i) == expression) {
						foundArg = i;
					}
				}
				if( foundArg != -1 ) {
					IMethodBinding mb = resolveMethod(mi);
					ITypeBinding[] tbs = mb.getParameterTypes();
					if( tbs.length > foundArg) {
						ITypeBinding unboxed = tbs[foundArg];
						ITypeBinding boxed = resolveType(t);
						if( isBoxedVersion(unboxed, boxed)) {
							return true;
						}
					} else if( tbs.length > 0 && mb.isVarargs()) {
						ITypeBinding lastArg = tbs[tbs.length - 1];
						if( lastArg.isArray()) {
							ITypeBinding el = lastArg.getElementType();
							if( el.isPrimitive()) {
								if( isBoxedVersion(el, resolveType(t))) {
									return true;
								}
							}
						}
					}
				}
			}

		}
		return false;
	}
	public boolean isRecoveringBindings() {
		return isRecoveringBindings;
	}
}
