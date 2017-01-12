/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.env.IModuleContext;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.ModuleRequirement;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.PackageExport;
import org.eclipse.jdt.internal.core.builder.NameEnvironment;
import org.eclipse.jdt.internal.core.builder.ProblemFactory;

public class ModuleUtil {

	private static String[] EMPTRY_STRING_ARRAY = new String[0];

	public static String createModuleFromPackageRoot(String moduleName, IPackageFragmentRoot root) throws CoreException {
		IJavaProject project = root.getJavaProject();
		String lineDelimiter = null;
		if (project != null) {
			IScopeContext[] scopeContext;
			// project preference
			scopeContext = new IScopeContext[] { new ProjectScope(project.getProject()) };
			lineDelimiter = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
		}
		if (lineDelimiter == null) {
			lineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		LocalModuleImpl module = (LocalModuleImpl) createModuleFromPackageFragmentRoot(moduleName, project);
		return module.toString(lineDelimiter);
	}

	public static IModuleDescription createModuleFromPackageRoot(String moduleName, IJavaProject root) throws CoreException {
		return createModuleFromPackageFragmentRoot(moduleName, root.getJavaProject());
	}

	static class ModuleAccumulatorEnvironment extends NameEnvironment {
		public ModuleAccumulatorEnvironment(IJavaProject javaProject) {
			super(javaProject);
		}

		Set<String> modules = new HashSet<>();
		public String[] getModules() {
			String[] mods = new String[this.modules.size()];
			return this.modules.toArray(mods);
		}
		@Override
		public org.eclipse.jdt.internal.compiler.env.IModule getModule(char[] name) {
			return null;
		}

		@Override
		public void cleanup() {
			this.modules.clear();
		}

		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName, IModuleContext context) {
			NameEnvironmentAnswer answer = super.findType(compoundTypeName, context);
			if (answer.moduleName() != null) {
				this.modules.add(new String(answer.moduleName()));
			}
			return answer;
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, IModuleContext context) {
			NameEnvironmentAnswer answer = super.findType(typeName, packageName, context);
			if (answer != null && answer.moduleName() != null) {
				this.modules.add(new String(answer.moduleName()));
			}
			return answer;
		}

		@Override
		public boolean isPackage(char[][] parentPackageName, char[] packageName, IModuleContext context) {
			return super.isPackage(parentPackageName, packageName, context);
		}
	}
	private static Compiler newCompiler(ModuleAccumulatorEnvironment environment, IJavaProject javaProject) {
		Map<String, String> projectOptions = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(projectOptions);
		compilerOptions.performMethodsFullRecovery = true;
		compilerOptions.performStatementsRecovery = true;
		ICompilerRequestor requestor = new ICompilerRequestor() {
			@Override
			public void acceptResult(CompilationResult result) {
				// Nothing to do here
			}
		};
		Compiler newCompiler = new Compiler(
			environment,
			DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			compilerOptions,
			requestor,
			ProblemFactory.getProblemFactory(Locale.getDefault()));

		return newCompiler;
	}
	private static IModuleDescription createModuleFromPackageFragmentRoot(String moduleName, IJavaProject project) throws CoreException {

		ModuleAccumulatorEnvironment environment = new ModuleAccumulatorEnvironment(project);
		Compiler compiler = newCompiler(environment, project);
		LocalModuleImpl module = new LocalModuleImpl(moduleName == null ? project.getElementName() : moduleName);
		List<IModuleDescription.IPackageExport> exports = new ArrayList<>();
		// First go over the binary roots and see if any of them are modules
		List<IModuleDescription.IModuleReference> required = new ArrayList<>();
		Set<org.eclipse.jdt.internal.compiler.env.ICompilationUnit> toCompile = new HashSet<>();
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		for (IPackageFragmentRoot root : roots) {
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IJavaElement[] children = root.getChildren();
				for (IJavaElement child : children) {
					if (child instanceof IPackageFragment) {
						IPackageFragment fragment = (IPackageFragment) child;
						if (fragment.isDefaultPackage()) continue;
						ICompilationUnit[] units = fragment.getCompilationUnits();
						if (units.length != 0) {
							String pack = fragment.getElementName();
							exports.add(new LocalPackageExportImpl(pack, EMPTRY_STRING_ARRAY));
							for (ICompilationUnit iUnit : units) {
								org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceFile = 
										new BasicCompilationUnit(iUnit.getSource().toCharArray(), CharOperation.splitOn('.', pack.toCharArray()), iUnit.getPath().toOSString());
								toCompile.add(sourceFile);
							}
						}
					}
				}
			}
		}

		org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sources = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[toCompile.size()];
		toCompile.toArray(sources);
		compiler.compile(sources);
		Collections.sort(exports, new Comparator<IModuleDescription.IPackageExport>() {
			@Override
			public int compare(IModuleDescription.IPackageExport o1, IModuleDescription.IPackageExport o2) {
				return o1.getPackageName().compareTo(
						o2.getPackageName());
			}
		});
		IModuleDescription.IPackageExport[] packs = new IModuleDescription.IPackageExport[exports.size()];
		packs = exports.toArray(packs);
		module.setExports(packs);
		String[] mods = environment.getModules();
		for (String string : mods) {
			required.add(new LocalModuleReferenceImpl(string, false));
		}
		Collections.sort(required, new Comparator<IModuleDescription.IModuleReference>() {
			@Override
			public int compare(IModuleDescription.IModuleReference o1, IModuleDescription.IModuleReference o2) {
				return new String(o1.getModuleName()).compareTo(new String(o2.getModuleName()));
			}
		});
		IModuleDescription.IModuleReference[] refs = new IModuleDescription.IModuleReference[required.size()];
		refs = required.toArray(refs);
		module.setRequiredModules(refs);
		return module;
	}
}
class LocalModuleImpl extends NamedMember implements IModuleDescription {
	IModuleDescription.IPackageExport[] exports = null;
	IModuleDescription.IModuleReference[] requires = null;
	IModuleDescription.IProvidedService[] services = null;
	IModuleDescription.IOpenPackage[] opened = null;

	String[] used = null;
	LocalModuleImpl(String name) {
		super(null, name);
	}
	@Override
	public IModuleReference[] getRequiredModules() throws JavaModelException {
		return this.requires;
	}
	public void setRequiredModules(IModuleDescription.IModuleReference[] requires) {
		this.requires = requires;
	}
	@Override
	public IPackageExport[] getExportedPackages() {
		return this.exports;
	}
	public void setExports(IPackageExport[] exports) {
		this.exports = exports;
	}
	@Override
	public IProvidedService[] getProvidedServices() {
		return this.services;
	}
	@Override
	public String[] getUsedServices() {
		return this.used;
	}
	@Override
	public IOpenPackage[] getOpenedPackages() throws JavaModelException {
		return this.opened;
	}

	public String toString(String lineDelimiter) {
		StringBuffer buffer = new StringBuffer();
		toStringContent(buffer, lineDelimiter);
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer, String lineDelimiter) {
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append(lineDelimiter);
		if (this.exports != null) {
			for(int i = 0; i < this.exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(this.exports[i].toString());
				buffer.append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter);
		if (this.requires != null) {
			for(int i = 0; i < this.requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				try {
					if (this.requires[i].isPublic()) {
						buffer.append(" public "); //$NON-NLS-1$
					}
				} catch (JavaModelException e) {
					// Ignore as it is unlikely to get a JME
				}
				buffer.append(this.requires[i].getModuleName());
				buffer.append(';').append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter).append('}').toString();
	}
	@Override
	public int getElementType() {
		return JAVA_MODULE;
	}
}
class LocalModuleReferenceImpl extends ModuleRequirement {
	String name;
	boolean isPublic = false;
	LocalModuleReferenceImpl(String name, boolean isPublic) {
		super(null, name);
		this.name = name;
		this.isPublic = isPublic;
	}
	@Override
	public boolean isPublic() {
		return this.isPublic;
	}
	@Override
	public int getElementType() {
		return MODULE_REFERENCE;
	}
	@Override
	public String getModuleName() {
		return this.name;
	}
	@Override
	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}
	@Override
	protected char getHandleMementoDelimiter() {
		return 0;
	}
	public boolean equals(Object o) {
		if (!(o instanceof LocalModuleReferenceImpl)) {
			return false;
		}
		return this.name.equals(((LocalModuleReferenceImpl) o).name);
	}
	
}
class LocalPackageExportImpl extends PackageExport {
	private String pkgName;
	private String[] targets;
	LocalPackageExportImpl(String pkgName, String[] targets) {
		super(null, pkgName);
		this.pkgName = pkgName;
		this.targets = targets;
	}
	@Override
	public String getPackageName() {
		return this.pkgName;
	}
	@Override
	public String[] getTargetModules() {
		return this.targets;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.pkgName);
		buffer.append(';');
		return buffer.toString();
	}
	@Override
	public int getElementType() {
		return PACKAGE_EXPORT;
	}
	@Override
	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}
	@Override
	protected char getHandleMementoDelimiter() {
		return 0;
	}
	public boolean equals(Object o) {
		if (!(o instanceof LocalPackageExportImpl)) {
			return false;
		}
		return this.pkgName.equals(((LocalPackageExportImpl) o).pkgName);
	}
}
