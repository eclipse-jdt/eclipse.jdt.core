package org.eclipse.jdt.internal.core.pdom.indexer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.java.JavaIndex;
import org.eclipse.jdt.internal.core.pdom.java.JavaNames;
import org.eclipse.jdt.internal.core.pdom.java.NdResourceFile;
import org.eclipse.jdt.internal.core.pdom.java.NdTreeNode;
import org.eclipse.jdt.internal.core.pdom.java.NdType;
import org.eclipse.jdt.internal.core.pdom.java.NdTypeId;
import org.eclipse.jdt.internal.core.pdom.java.NdTypeInterface;

public class BindingToIndexConverter {
	private static final boolean ENABLE_LOGGING = false;
	private JavaIndex index;
	private NdResourceFile resource;

	public BindingToIndexConverter(NdResourceFile resource) {
		this.resource = resource;
		this.index = JavaIndex.getIndex(resource.getPDOM());
	}

	public void addBinding(NdTreeNode parent, IBinding binding, IProgressMonitor monitor) {
		switch (binding.getKind()) {
			case IBinding.TYPE:
				addType((ITypeBinding) binding, monitor);
				break;
			case IBinding.ANNOTATION:
				addAnnotation(parent, (IAnnotationBinding) binding, monitor);
				break;
			case IBinding.METHOD:
				addMethod(parent, (IMethodBinding) binding, monitor);
				break;
			case IBinding.VARIABLE:
				addVariable(parent, (IVariableBinding) binding, monitor);
				break;
			case IBinding.PACKAGE:
				addPackage(parent, (IPackageBinding) binding, monitor);
				break;
			case IBinding.MEMBER_VALUE_PAIR:
				addMemberValuePair(parent, (IMemberValuePairBinding) binding, monitor);
				break;
			default:
				Package.log("Encountered unknown binding type: " + binding.getKind(), null);
		}
	}

	public void addMemberValuePair(NdTreeNode parent, IMemberValuePairBinding binding, IProgressMonitor monitor) {
		logInfo("Adding member value pair: " + binding.getName());
	}

	public void addPackage(NdTreeNode parent, IPackageBinding binding, IProgressMonitor monitor) {
		logInfo("Adding package: " + binding.getName());
	}

	public void addVariable(NdTreeNode parent, IVariableBinding binding, IProgressMonitor monitor) {
		logInfo("Adding variable: " + binding.getName());
	}

	public void addMethod(NdTreeNode parent, IMethodBinding binding, IProgressMonitor monitor) {
		logInfo("Adding method: " + binding.getName());
	}

	public void addAnnotation(NdTreeNode parent, IAnnotationBinding binding, IProgressMonitor monitor) {
		logInfo("Adding annotation: " + binding.getName());
	}

	public NdType addType(ITypeBinding binding, IProgressMonitor monitor) {
		logInfo("Adding type: " + binding.getBinaryName()); //$NON-NLS-1$

		NdTypeId name = makeTypeId(binding);
		NdType type = name.findTypeByResourceAddress(this.resource.address);

		if (type == null) {
			type = new NdType(getPDOM(), this.resource);
		}

		type.setTypeId(name);

		ITypeBinding superclass = binding.getSuperclass();

		if (superclass != null) {
			type.setSuperclass(makeTypeId(superclass));
		}

		for (ITypeBinding next : binding.getInterfaces()) {
			new NdTypeInterface(getPDOM(), type, makeTypeId(next));
		}

		return type;
	}

	private void logInfo(String string) {
		if (ENABLE_LOGGING) {
			Package.logInfo(string);
		}
	}

	private NdTypeId makeTypeId(ITypeBinding forBinding) {
		return this.index.createTypeId(JavaNames.binaryNameToFieldDescriptor(forBinding.getBinaryName().toCharArray()));
	}

	private Nd getPDOM() {
		return resource.getPDOM();
	}
}
