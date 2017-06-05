package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.NameLookup.Answer;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * @since 3.13 BETA_JAVA9
 */
public class ModuleBinding implements IModuleBinding {

	protected static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	private String name = null;
	private volatile String key;
	private boolean isOpen = false;

	private org.eclipse.jdt.internal.compiler.lookup.ModuleBinding binding;
	protected BindingResolver resolver;

	private IAnnotationBinding[] annotations;
	private IModuleBinding[] requiredModules;
	private IPackageBinding[] exportedPackages;
	private IModuleBinding[] exportTargets;
	private IPackageBinding[] openPackages;
	private IModuleBinding[] openTargets;

	ModuleBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.ModuleBinding binding) {
		this.resolver = resolver;
		this.binding = binding;
		this.isOpen = binding.isOpen; // TODO
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		if (this.annotations == null) {
			this.annotations = resolveAnnotationBindings(this.binding.getAnnotations());
		}
		return this.annotations;
	}

	private IAnnotationBinding[] resolveAnnotationBindings(org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding[] internalAnnotations) {
		int length = internalAnnotations == null ? 0 : internalAnnotations.length;
		if (length != 0) {
			IAnnotationBinding[] tempAnnotations = new IAnnotationBinding[length];
			int convertedAnnotationCount = 0;
			for (int i = 0; i < length; i++) {
				org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding internalAnnotation = internalAnnotations[i];
				if (internalAnnotation == null)
					break;
				IAnnotationBinding annotationInstance = this.resolver.getAnnotationInstance(internalAnnotation);
				if (annotationInstance == null)
					continue;
				tempAnnotations[convertedAnnotationCount++] = annotationInstance;
			}
			if (convertedAnnotationCount != length) {
				if (convertedAnnotationCount == 0) {
					return this.annotations = AnnotationBinding.NoAnnotations;
				}
				System.arraycopy(tempAnnotations, 0, (tempAnnotations = new IAnnotationBinding[convertedAnnotationCount]), 0, convertedAnnotationCount);
			}
			return tempAnnotations;
		}
		return AnnotationBinding.NoAnnotations;
	}

	@Override
	public String getName() {
		if (this.name == null) {
			char[] tmp = this.binding.moduleName;	
			return tmp != null && tmp.length != 0 ? new String(tmp) : Util.EMPTY_STRING;
		}
		return this.name;
	}

	@Override
	public int getModifiers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isRecovered() {
		return false;
	}

	@Override
	public boolean isSynthetic() {
		// TODO Auto-generated method stub
		// TODO BETA_JAVA9 no reference seen in jvms draft - only in sotm
		// check on version change and after compiler ast implements isSynthetic return this.binding.isSynthetic();
		
		return false;
	}

	@Override
	public IJavaElement getJavaElement() {
		INameEnvironment nameEnvironment = this.binding.environment.nameEnvironment;
		if (!(nameEnvironment instanceof SearchableEnvironment)) return null;
		NameLookup nameLookup = ((SearchableEnvironment) nameEnvironment).nameLookup;
		if (nameLookup == null) return null;
		Answer answer = nameLookup.findModule(this.getName());
		if (answer == null) return null;
		return answer.module;
	}

	@Override
	public String getKey() {
		if (this.key == null) {
			char[] k = this.binding.computeUniqueKey();
			this.key = k == null || k == CharOperation.NO_CHAR ? Util.EMPTY_STRING : new String(k);
		}
		return this.key;
	}

	@Override
	public boolean isEqualTo(IBinding other) {
		if (other == this) // identical binding - equal (key or no key)
			return true;
		if (other == null) // other binding missing
			return false;

		if (!(other instanceof ModuleBinding))
			return false;

		org.eclipse.jdt.internal.compiler.lookup.ModuleBinding otherBinding = ((ModuleBinding) other).binding;
		return BindingComparator.isEqual(this.binding, otherBinding);
	}

	@Override
	public boolean isOpen() {
		return this.isOpen; // TODO: info needs to be derived from compiler ast - bug 517269 awaited.
	}
	@Override
	public IModuleBinding[] getRequiredModules() {
		if (this.requiredModules != null)
			return this.requiredModules;

		org.eclipse.jdt.internal.compiler.lookup.ModuleBinding[] reqs = this.binding.getAllRequiredModules();	
		IModuleBinding[] result = new IModuleBinding[reqs != null ? reqs.length : 0];
		for (int i = 0, l = result.length; i < l; ++i) {
			org.eclipse.jdt.internal.compiler.lookup.ModuleBinding req = reqs[i];
			result[i] = req != null ? this.resolver.getModuleBinding(req) : null;
		}
		return this.requiredModules = result;
	}

	private void getPacks(IPackageExport[] packs, List<IPackageBinding> packBindings, List<IModuleBinding> targets) {
		for (IPackageExport pack : packs) {
			org.eclipse.jdt.internal.compiler.lookup.PackageBinding packB = this.binding.getExportedPackage(pack.name());
			if (packB == null) continue;
			IPackageBinding p = this.resolver.getPackageBinding(packB);	
			if (p != null)
				packBindings.add(p);
			//TODO: How do we resolve target modules? From the entire Java Model Scope? Wait for the new lookup environment.
		}
	}

	@Override
	public IPackageBinding[] getExportedPackages() {
		if (this.exportedPackages != null) 
			return this.exportedPackages;
	
		List<IPackageBinding> packs = new ArrayList<>();
		getPacks(this.binding.exports, packs, null);
		return this.exportedPackages = packs.toArray(new IPackageBinding[0]);
	}

	@Override
	public IModuleBinding[] getExportedTo(IPackageBinding packageBinding) {
		getExportedPackages();
		return this.exportTargets;// TODO Auto-generated method stub
	}

	@Override
	public IPackageBinding[] getOpenPackages() {
		if (this.openPackages != null) 
			return this.openPackages;
	
		List<IPackageBinding> packs = new ArrayList<>();
		getPacks(this.binding.exports, packs, null);
		return this.openPackages = packs.toArray(new IPackageBinding[0]);
	}

	@Override
	public IModuleBinding[] getOpenedTo(IPackageBinding packageBinding) {
		getOpenPackages();
		return this.openTargets;// TODO Auto-generated method stub
	}

	/*
	 * helper method
	 */
	private ITypeBinding[] getTypes(org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] types) {
		int length = types == null ? 0 : types.length;
		TypeBinding[] result = new TypeBinding[length];
		for (int i = 0; i < length; ++i) {
			result[i] = (TypeBinding) this.resolver.getTypeBinding(types[i]);
		}
		return result;
	}

	@Override
	public ITypeBinding[] getUses() {
		return getTypes(this.binding.uses);
	}

	@Override
	public ITypeBinding[] getServices() {
		return getTypes(this.binding.services);
	}

	@Override
	public ITypeBinding[] getImplementations() {
		return getTypes(this.binding.implementations);
	}
	/**
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}