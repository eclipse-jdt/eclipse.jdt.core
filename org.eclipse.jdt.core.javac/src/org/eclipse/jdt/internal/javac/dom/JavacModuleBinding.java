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
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.ModuleElement.DirectiveKind;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Directive.ExportsDirective;
import com.sun.tools.javac.code.Directive.OpensDirective;
import com.sun.tools.javac.code.Directive.ProvidesDirective;
import com.sun.tools.javac.code.Directive.RequiresDirective;
import com.sun.tools.javac.code.Directive.UsesDirective;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Type.ModuleType;
import com.sun.tools.javac.tree.JCTree.JCDirective;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCRequires;
public abstract class JavacModuleBinding implements IModuleBinding {

	private static final ITypeBinding[] NO_TYPE_ARGUMENTS = new ITypeBinding[0];
	final JavacBindingResolver resolver;
	public final ModuleSymbol moduleSymbol;
	private final ModuleType moduleType;
	private JCModuleDecl moduleDecl;

	public JavacModuleBinding(final ModuleType moduleType, final JavacBindingResolver resolver) {
		this((ModuleSymbol) moduleType.tsym, moduleType, resolver);
	}

	public JavacModuleBinding(final ModuleSymbol moduleSymbol, final JavacBindingResolver resolver) {
		this(moduleSymbol, (ModuleType)moduleSymbol.type, resolver);
	}

	public JavacModuleBinding(final JCModuleDecl decl, final JavacBindingResolver resolver) {
		this(decl.sym, (ModuleType)decl.sym.type, resolver);
		this.moduleDecl = decl;
	}
	
	public JavacModuleBinding(final ModuleSymbol moduleSymbol, final ModuleType moduleType, JavacBindingResolver resolver) {
		this.moduleType = moduleType;
		this.moduleSymbol = moduleSymbol;
		this.resolver = resolver;
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		// TODO - don't see any way to get this?
		List<Attribute.Compound> list = moduleSymbol.getRawAttributes();
		return list.stream().map(x -> this.resolver.bindings.getAnnotationBinding(x, this)).toArray(JavacAnnotationBinding[]::new);
	}

	@Override
	public String getName() {
		return this.moduleSymbol.name.toString();
	}

	@Override
	public int getModifiers() {
		return JavacMethodBinding.toInt(this.moduleSymbol.getModifiers());
	}

	@Override
	public boolean isDeprecated() {
		return this.moduleSymbol.isDeprecated();
	}

	@Override
	public boolean isRecovered() {
		return this.moduleSymbol.kind == Kinds.Kind.ERR;
	}

	@Override
	public boolean isSynthetic() {
		return (this.moduleSymbol.flags() & Flags.SYNTHETIC) != 0;
	}

	@Override
	public IJavaElement getJavaElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKey() {
		return "\"" + this.getName();
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof JavacModuleBinding other && //
				Objects.equals(this.moduleSymbol, other.moduleSymbol) && //
				Objects.equals(this.resolver, other.resolver);
	}

	@Override
	public boolean isOpen() {
		return this.moduleSymbol.isOpen();
	}

	@Override
	public IModuleBinding[] getRequiredModules() {
		ArrayList<ModuleSymbol> mods = new ArrayList<>();
		this.moduleSymbol.getDirectives().stream() 
				.filter(x -> x.getKind() == DirectiveKind.REQUIRES) 
				.map(x -> ((RequiresDirective)x).module) 
				.forEachOrdered(mods::add);
		if( this.moduleDecl != null ) {
			List<JCDirective> directives = this.moduleDecl.getDirectives();
			for( JCDirective jcd : directives ) {
				if( jcd instanceof JCRequires jcr) {
					JCExpression jce = jcr.moduleName;
					if( jce instanceof JCIdent jcid && jcid.sym instanceof ModuleSymbol mss) {
						if( !mods.contains(mss)) {
							mods.add(mss);
						}
					}
				}
			}
		}
		IModuleBinding[] ret = new IModuleBinding[mods.size()];
		for( int i = 0; i < mods.size(); i++ ) {
			ret[i] = this.resolver.bindings.getModuleBinding(mods.get(i));
		}
		return ret;
	}

	@Override
	public IPackageBinding[] getExportedPackages() {
		ExportsDirective[] arr = this.moduleSymbol.getDirectives().stream() //
				.filter(x -> x.getKind() == DirectiveKind.EXPORTS) //
				.map(x -> (ExportsDirective)x) //
				.toArray(ExportsDirective[]::new);
		IPackageBinding[] arr2 = new IPackageBinding[arr.length];
		for( int i = 0; i < arr.length; i++ ) {
			arr2[i] = this.resolver.bindings.getPackageBinding(arr[i].packge);
		}
		return arr2;
	}

	@Override
	public String[] getExportedTo(IPackageBinding packageBinding) {
		ExportsDirective[] arr = this.moduleSymbol.getDirectives().stream().filter((x) -> x.getKind() == DirectiveKind.EXPORTS).map((x) -> (ExportsDirective)x).toArray(ExportsDirective[]::new);
		for( int i = 0; i < arr.length; i++ ) {
			JavacPackageBinding tmp = this.resolver.bindings.getPackageBinding(arr[i].packge);
			if( tmp.isUnnamed() == packageBinding.isUnnamed() && 
					tmp.getName().equals(packageBinding.getName())) {
				return arr[i].getTargetModules().stream().map(ModuleSymbol::toString).toArray(String[]::new);
			}
		}
		return new String[0];
	}

	@Override
	public IPackageBinding[] getOpenedPackages() {
		OpensDirective[] arr = this.moduleSymbol.getDirectives().stream().filter((x) -> x.getKind() == DirectiveKind.OPENS).map((x) -> (OpensDirective)x).toArray(OpensDirective[]::new);
		IPackageBinding[] arr2 = new IPackageBinding[arr.length];
		for( int i = 0; i < arr.length; i++ ) {
			arr2[i] = this.resolver.bindings.getPackageBinding(arr[i].packge);
		}
		return arr2;
	}

	@Override
	public String[] getOpenedTo(IPackageBinding packageBinding) {
		OpensDirective[] arr = this.moduleSymbol.getDirectives().stream().filter((x) -> x.getKind() == DirectiveKind.OPENS).map((x) -> (OpensDirective)x).toArray(OpensDirective[]::new);
		for( int i = 0; i < arr.length; i++ ) {
			JavacPackageBinding tmp = this.resolver.bindings.getPackageBinding(arr[i].packge);
			if( tmp.isUnnamed() == packageBinding.isUnnamed() && 
					tmp.getName().equals(packageBinding.getName())) {
				return arr[i].getTargetModules().stream().map((x) -> x.toString()).toArray(String[]::new);
			}
		}
		return new String[0];
	}

	@Override
	public ITypeBinding[] getUses() {
		UsesDirective[] arr = this.moduleSymbol.getDirectives().stream().filter((x) -> x.getKind() == DirectiveKind.USES).map((x) -> (UsesDirective)x).toArray(UsesDirective[]::new);
		ITypeBinding[] arr2 = new ITypeBinding[arr.length];
		for( int i = 0; i < arr.length; i++ ) {
			arr2[i] = this.resolver.bindings.getTypeBinding(arr[i].getService().type);
		}
		return arr2;
	}

	@Override
	public ITypeBinding[] getServices() {
		ProvidesDirective[] arr = this.moduleSymbol.getDirectives().stream().filter((x) -> x.getKind() == DirectiveKind.PROVIDES).map((x) -> (ProvidesDirective)x).toArray(ProvidesDirective[]::new);
		ITypeBinding[] arr2 = new ITypeBinding[arr.length];
		for( int i = 0; i < arr.length; i++ ) {
			arr2[i] = this.resolver.bindings.getTypeBinding(arr[i].getService().type);
		}
		return arr2;
	}

	@Override
	public ITypeBinding[] getImplementations(ITypeBinding service) {
		ProvidesDirective[] arr = this.moduleSymbol.getDirectives().stream().filter((x) -> x.getKind() == DirectiveKind.PROVIDES).map((x) -> (ProvidesDirective)x).toArray(ProvidesDirective[]::new);
		for( int i = 0; i < arr.length; i++ ) {
			JavacTypeBinding tmp = this.resolver.bindings.getTypeBinding(arr[i].getService().type);
			if(service.getKey().equals(tmp.getKey())) {
				// we have our match
				JavacTypeBinding[] ret = arr[i].getImplementations().stream().map(x -> this.resolver.bindings.getTypeBinding(x.type)).toArray(JavacTypeBinding[]::new);
				return ret;
			}
		}
		return null;
	}
}
