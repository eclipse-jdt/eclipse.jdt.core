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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.javac.dom.JavacAnnotationBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMemberValuePairBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMethodBinding;
import org.eclipse.jdt.internal.javac.dom.JavacModuleBinding;
import org.eclipse.jdt.internal.javac.dom.JavacPackageBinding;
import org.eclipse.jdt.internal.javac.dom.JavacTypeBinding;
import org.eclipse.jdt.internal.javac.dom.JavacVariableBinding;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.ModuleType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
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
	private Map<Symbol, ASTNode> symbolToDom;
	private final Map<String, IBinding> bindingCache;
	public final IJavaProject javaProject;
	private JavacConverter converter;
	boolean isRecoveringBindings = false;

	public JavacBindingResolver(IJavaProject javaProject, JavacTask javacTask, Context context, JavacConverter converter) {
		this.javac = javacTask;
		this.context = context;
		this.javaProject = javaProject;
		this.converter = converter;
		this.bindingCache = new HashMap<>();
	}

	private void resolve() {
		if (this.symbolToDom == null) {
			try {
				this.javac.analyze();
			} catch (IOException e) {
				ILog.get().error(e.getMessage(), e);
			}
			this.symbolToDom = new HashMap<>();
			this.converter.domToJavac.entrySet().forEach(entry ->
				symbol(entry.getValue()).ifPresent(sym -> this.symbolToDom.put(sym, entry.getKey())));
		}
	}

	@Override
	public ASTNode findDeclaringNode(IBinding binding) {
		return findNode(getJavacSymbol(binding));
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
			return packageBinding.packageSymbol;
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
		if (this.symbolToDom != null) {
			return this.symbolToDom.get(symbol);
		}
		return null;
	}

	private Optional<Symbol> symbol(JCTree value) {
		if (value instanceof JCClassDecl jcClassDecl) {
			return Optional.ofNullable(jcClassDecl.sym);
		}
		if (value instanceof JCFieldAccess jcFieldAccess) {
			return Optional.ofNullable(jcFieldAccess.sym);
		}
		if (value instanceof JCTree.JCVariableDecl jcVariableDecl) {
			return Optional.ofNullable(jcVariableDecl.sym);
		}
		if (value instanceof JCTree.JCMethodDecl jcMethodDecl) {
			return Optional.ofNullable(jcMethodDecl.sym);
		}
		// TODO fields, methods, variables...
		return Optional.empty();
	}

	@Override
	ITypeBinding resolveType(Type type) {
		resolve();
		JCTree jcTree = this.converter.domToJavac.get(type);
		if (jcTree instanceof JCIdent ident && ident.type != null) {
			return canonicalize(new JavacTypeBinding(ident.type, this));
		}
		if (jcTree instanceof JCFieldAccess access && access.type != null) {
			return canonicalize(new JavacTypeBinding(access.type, this));
		}
		if (jcTree instanceof JCPrimitiveTypeTree primitive && primitive.type != null) {
			return canonicalize(new JavacTypeBinding(primitive.type, this));
		}
		if (jcTree instanceof JCArrayTypeTree arrayType && arrayType.type != null) {
			return canonicalize(new JavacTypeBinding(arrayType.type, this));
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
		return super.resolveType(type);
	}

	@Override
	ITypeBinding resolveType(TypeDeclaration type) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(type);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return canonicalize(new JavacTypeBinding(jcClassDecl.type, this));
		}
		return null;
	}

	@Override
	ITypeBinding resolveType(EnumDeclaration enumDecl) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(enumDecl);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return canonicalize(new JavacTypeBinding(jcClassDecl.type, this));
		}
		return null;
	}

	@Override
	ITypeBinding resolveType(AnonymousClassDeclaration anonymousClassDecl) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(anonymousClassDecl);
		if (javacNode instanceof JCClassDecl jcClassDecl && jcClassDecl.type != null) {
			return canonicalize(new JavacTypeBinding(jcClassDecl.type, this));
		}
		return null;
	}
	ITypeBinding resolveTypeParameter(TypeParameter typeParameter) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(typeParameter);
		if (javacNode instanceof JCTypeParameter jcClassDecl) {
			return new JavacTypeBinding(jcClassDecl.type, this);
		}
		return null;
	}

	public IBinding getBinding(final Symbol owner, final com.sun.tools.javac.code.Type type) {
		if (owner instanceof final PackageSymbol other) {
			return canonicalize(new JavacPackageBinding(other, this));
		} else if (owner instanceof TypeSymbol typeSymbol) {
			return canonicalize(new JavacTypeBinding(typeSymbol.type, this));
		} else if (owner instanceof final MethodSymbol other) {
			return canonicalize(new JavacMethodBinding(type instanceof com.sun.tools.javac.code.Type.MethodType methodType ? methodType : owner.type.asMethodType(), other, this));
		} else if (owner instanceof final VarSymbol other) {
			return canonicalize(new JavacVariableBinding(other, this));
		}
		return null;
	}

	@Override
	IVariableBinding resolveField(FieldAccess fieldAccess) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(fieldAccess);
		if (javacElement instanceof JCFieldAccess javacFieldAccess && javacFieldAccess.sym instanceof VarSymbol varSymbol) {
			return canonicalize(new JavacVariableBinding(varSymbol, this));
		}
		return null;
	}

	@Override
	IVariableBinding resolveField(SuperFieldAccess fieldAccess) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(fieldAccess);
		if (javacElement instanceof JCFieldAccess javacFieldAccess && javacFieldAccess.sym instanceof VarSymbol varSymbol) {
			return new JavacVariableBinding(varSymbol, this);
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(MethodInvocation method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return canonicalize(new JavacMethodBinding(ident.type.asMethodType(), methodSymbol, this));
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return canonicalize(new JavacMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, this));
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(MethodDeclaration method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		if (javacElement instanceof JCMethodDecl methodDecl) {
			return canonicalize(new JavacMethodBinding(methodDecl.type.asMethodType(), methodDecl.sym, this));
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
			return canonicalize(new JavacMethodBinding(ident.type.asMethodType(), methodSymbol, this));
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return canonicalize(new JavacMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, this));
		}
		return null;
	}

	@Override
	IBinding resolveName(Name name) {
		resolve();
		JCTree tree = this.converter.domToJavac.get(name);
		if (tree == null) {
			tree = this.converter.domToJavac.get(name.getParent());
		}
		if (tree instanceof JCIdent ident && ident.sym != null) {
			return getBinding(ident.sym, ident.type != null ? ident.type : ident.sym.type);
		}
		if (tree instanceof JCFieldAccess fieldAccess && fieldAccess.sym != null) {
			return getBinding(fieldAccess.sym, fieldAccess.type);
		}
		if (tree instanceof JCMethodInvocation methodInvocation && methodInvocation.meth.type != null) {
			return getBinding(((JCFieldAccess)methodInvocation.meth).sym, methodInvocation.meth.type);
		}
		if (tree instanceof JCClassDecl classDecl && classDecl.sym != null) {
			return getBinding(classDecl.sym, classDecl.type);
		}
		if (tree instanceof JCMethodDecl methodDecl && methodDecl.sym != null) {
			return getBinding(methodDecl.sym, methodDecl.type);
		}
		if (tree instanceof JCVariableDecl variableDecl && variableDecl.sym != null) {
			return getBinding(variableDecl.sym, variableDecl.type);
		}
		return null;
	}

	@Override
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		resolve();
		if (this.converter.domToJavac.get(variable) instanceof JCVariableDecl decl) {
			if (!decl.type.isErroneous() || this.isRecoveringBindings) {
				return canonicalize(new JavacVariableBinding(decl.sym, this));
			}
		}
		return null;
	}

	@Override
	public IPackageBinding resolvePackage(PackageDeclaration decl) {
		resolve();
		if (this.converter.domToJavac.get(decl) instanceof JCPackageDecl jcPackageDecl) {
			return canonicalize(new JavacPackageBinding(jcPackageDecl.packge, this));
		}
		return null;
	}

	@Override
	public ITypeBinding resolveExpressionType(Expression expr) {
		resolve();
		if (expr instanceof SimpleName name) {
			IBinding binding = resolveName(name);
			if (binding.isRecovered() && !this.isRecoveringBindings) {
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
		return this.converter.domToJavac.get(expr) instanceof JCExpression jcExpr ?
				canonicalize(new JavacTypeBinding(jcExpr.type, this)) :
			null;
	}

	@Override
	IMethodBinding resolveConstructor(ClassInstanceCreation expression) {
		resolve();
		return this.converter.domToJavac.get(expression) instanceof JCNewClass jcExpr
				&& !jcExpr.constructor.type.isErroneous()?
						canonicalize(new JavacMethodBinding(jcExpr.constructor.type.asMethodType(), (MethodSymbol)jcExpr.constructor, this)) :
				null;
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
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(module);
		if( javacElement instanceof JCModuleDecl jcmd) {
			Object o = jcmd.sym.type;
			if( o instanceof ModuleType mt ) {
				return new JavacModuleBinding(mt, this);
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
			return canonicalize(new JavacTypeBinding(clazz.classType, this));
		} else if (attribute instanceof Attribute.Enum enumm) {
			return canonicalize(new JavacVariableBinding(enumm.value, this));
		} else if (attribute instanceof Attribute.Array array) {
			return Stream.of(array.values) //
					.map(nestedAttr -> {
						if (attribute instanceof Attribute.Constant constant) {
							return constant.value;
						} else if (attribute instanceof Attribute.Class clazz) {
							return canonicalize(new JavacTypeBinding(clazz.classType, this));
						} else if (attribute instanceof Attribute.Enum enumerable) {
							return canonicalize(new JavacVariableBinding(enumerable.value, this));
						}
						throw new IllegalArgumentException("Unexpected attribute type: " + nestedAttr.getClass().getCanonicalName());
					}) //
					.toArray(Object[]::new);
		}
		throw new IllegalArgumentException("Unexpected attribute type: " + attribute.getClass().getCanonicalName());
	}

	@Override
	IBinding resolveImport(ImportDeclaration importDeclaration) {
		var javac = this.converter.domToJavac.get(importDeclaration.getName());
		if (javac instanceof JCFieldAccess fieldAccess) {
			if (fieldAccess.sym != null) {
				return getBinding(fieldAccess.sym, null);
			}
			if (importDeclaration.isStatic()) {
				com.sun.tools.javac.code.Type type = fieldAccess.getExpression().type;
				if (type != null) {
					return Arrays.stream(new JavacTypeBinding(type, this).getDeclaredMethods())
						.filter(method -> Objects.equals(fieldAccess.getIdentifier().toString(), method.getName()))
						.findAny()
						.orElse(null);
				}
			}
		}
		return null;
	}

	public <T extends IBinding> T canonicalize(T binding) {
		T cachedBinding = (T) this.bindingCache.get(binding.getKey());
		if (cachedBinding == null) {
			this.bindingCache.put(binding.getKey(), binding);
			return binding;
		}
		return cachedBinding;
	}

	@Override
	ITypeBinding resolveWellKnownType(String typeName) {
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
		return canonicalize(new JavacTypeBinding(type, this));
	}
}
