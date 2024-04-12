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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.javac.dom.JavacAnnotationBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMemberValuePairBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMethodBinding;
import org.eclipse.jdt.internal.javac.dom.JavacPackageBinding;
import org.eclipse.jdt.internal.javac.dom.JavacTypeBinding;
import org.eclipse.jdt.internal.javac.dom.JavacVariableBinding;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

/**
 * Deals with creation of binding model, using the <code>Symbol</code>s from Javac.
 * @implNote Cannot move to another package because parent class is package visible only
 */
public class JavacBindingResolver extends BindingResolver {

	private final JavaCompiler javac; // TODO evaluate memory cost of storing the instance
	// it will probably be better to run the `Enter` and then only extract interesting
	// date from it.
	public final Context context;
	private Map<Symbol, ASTNode> symbolToDom;
	public final IJavaProject javaProject;
	private JavacConverter converter;

	public JavacBindingResolver(JavaCompiler javac, IJavaProject javaProject, Context context, JavacConverter converter) {
		this.javac = javac;
		this.context = context;
		this.javaProject = javaProject;
		this.converter = converter;
	}

	private void resolve() {
		if (this.symbolToDom == null) {
			java.util.List<JCCompilationUnit> units = this.converter.domToJavac.values().stream()
				.filter(JCCompilationUnit.class::isInstance)
				.map(JCCompilationUnit.class::cast)
				.toList();
			Modules.instance(this.context).initModules(List.from(units));
			Todo todo = Todo.instance(this.context);
			this.javac.enterTrees(List.from(units));
			Queue<Env<AttrContext>> attribute = this.javac.attribute(todo);
			this.javac.flow(attribute);
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
			return Optional.of(jcClassDecl.sym);
		}
		if (value instanceof JCFieldAccess jcFieldAccess) {
			return Optional.of(jcFieldAccess.sym);
		}
		// TODO fields, methods, variables...
		return Optional.empty();
	}

	@Override
	ITypeBinding resolveType(Type type) {
		resolve();
		JCTree jcTree = this.converter.domToJavac.get(type);
		final java.util.List<TypeSymbol> typeArguments = getTypeArguments(type);
		if (jcTree instanceof JCIdent ident && ident.sym instanceof TypeSymbol typeSymbol) {
			return new JavacTypeBinding(typeSymbol, this, typeArguments);
		}
		if (jcTree instanceof JCFieldAccess access && access.sym instanceof TypeSymbol typeSymbol) {
			return new JavacTypeBinding(typeSymbol, this, typeArguments);
		}
		if (jcTree instanceof JCPrimitiveTypeTree primitive) {
			return new JavacTypeBinding(primitive.type, this, typeArguments);
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
		if (javacNode instanceof JCClassDecl jcClassDecl) {
			return new JavacTypeBinding(jcClassDecl.sym, this, null);
		}
		return null;
	}

	public IBinding getBinding(final Symbol owner, final java.util.List<TypeSymbol> typeArguments) {
		if (owner instanceof final PackageSymbol other) {
			return new JavacPackageBinding(other, this);
		} else if (owner instanceof final TypeSymbol other) {
			return new JavacTypeBinding(other, this, typeArguments);
		} else if (owner instanceof final MethodSymbol other) {
			return new JavacMethodBinding(other, this, typeArguments);
		} else if (owner instanceof final VarSymbol other) {
			return new JavacVariableBinding(other, this);
		}
		return null;
	}

	@Override
	IVariableBinding resolveField(FieldAccess fieldAccess) {
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
		final java.util.List<TypeSymbol> typeArguments = getTypeArguments(method);
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return new JavacMethodBinding(methodSymbol, this, typeArguments);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return new JavacMethodBinding(methodSymbol, this, typeArguments);
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(MethodDeclaration method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		if (javacElement instanceof JCMethodDecl methodDecl) {
			return new JavacMethodBinding(methodDecl.sym, this, null);
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
		final java.util.List<TypeSymbol> typeArguments = getTypeArguments(name);
		if (tree instanceof JCIdent ident && ident.sym != null) {
			return getBinding(ident.sym, typeArguments);
		}
		if (tree instanceof JCFieldAccess fieldAccess && fieldAccess.sym != null) {
			return getBinding(fieldAccess.sym, typeArguments);
		}
		if (tree instanceof JCClassDecl classDecl && classDecl.sym != null) {
			return getBinding(classDecl.sym, typeArguments);
		}
		if (tree instanceof JCVariableDecl variableDecl && variableDecl.sym != null) {
			return getBinding(variableDecl.sym, typeArguments);
		}
		return null;
	}

	@Override
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		resolve();
		return this.converter.domToJavac.get(variable) instanceof JCVariableDecl decl ?
			new JavacVariableBinding(decl.sym, this) : null;
	}

	@Override
	public IPackageBinding resolvePackage(PackageDeclaration decl) {
		resolve();
		return null;
	}

	@Override
	public ITypeBinding resolveExpressionType(Expression expr) {
		resolve();
		return this.converter.domToJavac.get(expr) instanceof JCExpression jcExpr ?
			new JavacTypeBinding(jcExpr.type, this, null) :
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

}
