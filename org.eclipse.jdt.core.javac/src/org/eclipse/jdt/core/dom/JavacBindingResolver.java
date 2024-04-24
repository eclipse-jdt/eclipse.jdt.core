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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.javac.dom.JavacAnnotationBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMemberValuePairBinding;
import org.eclipse.jdt.internal.javac.dom.JavacMethodBinding;
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
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
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
	public final IJavaProject javaProject;
	private JavacConverter converter;

	public JavacBindingResolver(IJavaProject javaProject, JavacTask javacTask, Context context, JavacConverter converter) {
		this.javac = javacTask;
		this.context = context;
		this.javaProject = javaProject;
		this.converter = converter;
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
		if (jcTree instanceof JCIdent ident && ident.sym instanceof TypeSymbol typeSymbol) {
			return new JavacTypeBinding(ident.type, this);
		}
		if (jcTree instanceof JCFieldAccess access && access.sym instanceof TypeSymbol typeSymbol) {
			return new JavacTypeBinding(access.type, this);
		}
		if (jcTree instanceof JCPrimitiveTypeTree primitive) {
			return new JavacTypeBinding(primitive.type, this);
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
			return new JavacTypeBinding(jcClassDecl.type, this);
		}
		return null;
	}

	@Override
	ITypeBinding resolveType(EnumDeclaration enumDecl) {
		resolve();
		JCTree javacNode = this.converter.domToJavac.get(enumDecl);
		if (javacNode instanceof JCClassDecl jcClassDecl) {
			return new JavacTypeBinding(jcClassDecl.type, this);
		}
		return null;
	}

	public IBinding getBinding(final Symbol owner, final com.sun.tools.javac.code.Type type) {
		if (owner instanceof final PackageSymbol other) {
			return new JavacPackageBinding(other, this);
		} else if (owner instanceof TypeSymbol) {
			return new JavacTypeBinding(type, this);
		} else if (owner instanceof final MethodSymbol other) {
			return new JavacMethodBinding(type.asMethodType(), other, this);
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
		if (javacElement instanceof JCMethodInvocation javacMethodInvocation) {
			javacElement = javacMethodInvocation.getMethodSelect();
		}
		if (javacElement instanceof JCIdent ident && ident.sym instanceof MethodSymbol methodSymbol) {
			return new JavacMethodBinding(ident.type.asMethodType(), methodSymbol, this);
		}
		if (javacElement instanceof JCFieldAccess fieldAccess && fieldAccess.sym instanceof MethodSymbol methodSymbol) {
			return new JavacMethodBinding(fieldAccess.type.asMethodType(), methodSymbol, this);
		}
		return null;
	}

	@Override
	IMethodBinding resolveMethod(MethodDeclaration method) {
		resolve();
		JCTree javacElement = this.converter.domToJavac.get(method);
		if (javacElement instanceof JCMethodDecl methodDecl) {
			return new JavacMethodBinding(methodDecl.type.asMethodType(), methodDecl.sym, this);
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
			return getBinding(ident.sym, ident.type);
		}
		if (tree instanceof JCFieldAccess fieldAccess && fieldAccess.sym != null) {
			return getBinding(fieldAccess.sym, fieldAccess.type);
		}
		if (tree instanceof JCClassDecl classDecl && classDecl.sym != null) {
			return getBinding(classDecl.sym, classDecl.type);
		}
		if (tree instanceof JCVariableDecl variableDecl && variableDecl.sym != null) {
			return getBinding(variableDecl.sym, variableDecl.type);
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
			new JavacTypeBinding(jcExpr.type, this) :
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
			return new JavacTypeBinding(clazz.classType, this);
		} else if (attribute instanceof Attribute.Enum enumm) {
			return new JavacVariableBinding(enumm.value, this);
		} else if (attribute instanceof Attribute.Array array) {
			return Stream.of(array.values) //
					.map(nestedAttr -> {
						if (attribute instanceof Attribute.Constant constant) {
							return constant.value;
						} else if (attribute instanceof Attribute.Class clazz) {
							return new JavacTypeBinding(clazz.classType, this);
						} else if (attribute instanceof Attribute.Enum enumerable) {
							return new JavacVariableBinding(enumerable.value, this);
						}
						throw new IllegalArgumentException("Unexpected attribute type: " + nestedAttr.getClass().getCanonicalName());
					}) //
					.toArray(Object[]::new);
		}
		throw new IllegalArgumentException("Unexpected attribute type: " + attribute.getClass().getCanonicalName());
	}

}
