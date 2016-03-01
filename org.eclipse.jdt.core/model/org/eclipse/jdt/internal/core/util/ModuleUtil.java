/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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
import org.eclipse.jdt.core.IModule;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.NameEnvironment;
import org.eclipse.jdt.internal.core.builder.ProblemFactory;

public class ModuleUtil {

	public static String createModuleFromPackageRoot(String moduleName, IPackageFragmentRoot root) throws CoreException {
		return createModuleFromPackageFragmentRoot(moduleName, root, root.getJavaProject());
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
		public NameEnvironmentAnswer findType(char[][] compoundTypeName, org.eclipse.jdt.internal.compiler.env.IModule[] mods) {
			NameEnvironmentAnswer answer = super.findType(compoundTypeName, mods);
			if (answer.moduleName() != null) {
				this.modules.add(new String(answer.moduleName()));
			}
			return answer;
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, org.eclipse.jdt.internal.compiler.env.IModule[] mods) {
			NameEnvironmentAnswer answer = super.findType(typeName, packageName, mods);
			if (answer != null && answer.moduleName() != null) {
				this.modules.add(new String(answer.moduleName()));
			}
			return answer;
		}

		@Override
		public boolean isPackage(char[][] parentPackageName, char[] packageName, org.eclipse.jdt.internal.compiler.env.IModule[] module) {
			return super.isPackage(parentPackageName, packageName, module);
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
	private static String createModuleFromPackageFragmentRoot(String moduleName, IPackageFragmentRoot root, IJavaProject project) throws CoreException {
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
		if (!root.isArchive()) {
			ModuleAccumulatorEnvironment environment = new ModuleAccumulatorEnvironment(project);
			Compiler compiler = newCompiler(environment, project);
			LocalModuleImpl module = new LocalModuleImpl(moduleName == null ? root.getElementName() : moduleName);
			List<IModule.IPackageExport> exports = new ArrayList<>();
			// First go over the binary roots and see if any of them are modules
			List<IModule.IModuleReference> required = new ArrayList<>();
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			for (IPackageFragmentRoot binRoot : roots) {
				if (binRoot.isArchive()) {
					PackageFragmentRoot lib = (PackageFragmentRoot) binRoot;
					org.eclipse.jdt.internal.compiler.env.IModule mod = ((OpenableElementInfo) lib.getElementInfo()).getModule();
					if (mod != null) {
						LocalModuleReferenceImpl ref = new LocalModuleReferenceImpl(mod.name(), false);
						required.add(ref);
					}
				}
			}
			Set<org.eclipse.jdt.internal.compiler.env.ICompilationUnit> toCompile = new HashSet<>();
			IJavaElement[] children = root.getChildren();
			for (IJavaElement child : children) {
				if (child instanceof IPackageFragment) {
					IPackageFragment fragment = (IPackageFragment) child;
					if (fragment.isDefaultPackage()) continue;
					ICompilationUnit[] units = fragment.getCompilationUnits();
					if (units.length != 0) {
						String pack = fragment.getElementName();
						exports.add(new LocalPackageExportImpl(fragment, null));
						for (ICompilationUnit iUnit : units) {
							org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceFile = 
									new BasicCompilationUnit(iUnit.getSource().toCharArray(), CharOperation.splitOn('.', pack.toCharArray()), iUnit.getPath().toOSString());
							toCompile.add(sourceFile);
						}
					}
				}
			}
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sources = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[toCompile.size()];
			toCompile.toArray(sources);
			compiler.compile(sources);
			Collections.sort(exports, new Comparator<IModule.IPackageExport>() {
				@Override
				public int compare(IModule.IPackageExport o1, IModule.IPackageExport o2) {
					return o1.getExportedPackage().getElementName().compareTo(
							o2.getExportedPackage().getElementName());
				}
			});
			IModule.IPackageExport[] packs = new IModule.IPackageExport[exports.size()];
			packs = exports.toArray(packs);
			module.setExports(packs);
			String[] mods = environment.getModules();
			for (String string : mods) {
				required.add(new LocalModuleReferenceImpl(string.toCharArray(), false));
			}
			Collections.sort(required, new Comparator<IModule.IModuleReference>() {
				@Override
				public int compare(IModule.IModuleReference o1, IModule.IModuleReference o2) {
					return new String(o1.module().name()).compareTo(new String(o2.module().name()));
				}
			});
			IModule.IModuleReference[] refs = new IModule.IModuleReference[required.size()];
			refs = required.toArray(refs);
			module.setRequiredModules(refs);
			return module.toString(lineDelimiter);
		}
		return null;
	}
}
class LocalModuleImpl implements IModule {
	IModule.IPackageExport[] exports = null;
	IModule.IModuleReference[] requires = null;
	char[] name = null;
	LocalModuleImpl(String name) {
		this.name = name.toCharArray();
	}
	@Override
	public char[] name() {
		return this.name;
	}
	@Override
	public IModuleReference requires() throws JavaModelException {
		return this.requires();
	}
	public void setRequiredModules(IModule.IModuleReference[] requires) {
		this.requires = requires;
	}
	@Override
	public IPackageExport[] exports() {
		return this.exports;
	}
	public void setExports(IPackageExport[] exports) {
		this.exports = exports;
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
				if (this.requires[i].isPublic()) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].module().name());
				buffer.append(';').append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter).append('}').toString();
	}
}
class LocalModuleReferenceImpl implements IModule.IModuleReference {
	IModule ref;
	boolean isPublic = false;
	LocalModuleReferenceImpl(final char[] name, boolean isPublic) {
		this.ref = new IModule(){
			@Override
			public char[] name() {
				return name;
			}
			@Override
			public IPackageExport[] exports() throws JavaModelException {
				return null;
			}
			@Override
			public IModuleReference requires() throws JavaModelException {
				return null;
			}};
			this.isPublic = isPublic;
	}
	@Override
	public boolean isPublic() {
		return this.isPublic;
	}
	@Override
	public IModule module() {
		return this.ref;
	}
}
class LocalPackageExportImpl implements IModule.IPackageExport {
	private IPackageFragment pack;
	private IModule target;
	LocalPackageExportImpl(IPackageFragment pack, IModule target) {
		this.pack = pack;
	}
	@Override
	public IPackageFragment getExportedPackage() {
		return this.pack;
	}
	@Override
	public IModule getTargetModule() {
		return this.target;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.pack.getElementName());
		buffer.append(';');
		return buffer.toString();
	}
}
