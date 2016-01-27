package org.eclipse.jdt.internal.core.pdom.indexer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.java.JavaIndex;
import org.eclipse.jdt.internal.core.pdom.java.JavaNames;
import org.eclipse.jdt.internal.core.pdom.java.PDOMResourceFile;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTreeNode;
import org.eclipse.jdt.internal.core.pdom.java.PDOMType;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTypeId;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTypeInterface;

public class BindingToIndexConverter {
	private static final boolean ENABLE_LOGGING = false;
	private JavaIndex index;
	private PDOMResourceFile resource;

	public BindingToIndexConverter(PDOMResourceFile resource) {
		this.resource = resource;
		this.index = JavaIndex.getIndex(resource.getPDOM());
	}

	public void addBinding(PDOMTreeNode parent, IBinding binding, IProgressMonitor monitor) {
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

	public void addMemberValuePair(PDOMTreeNode parent, IMemberValuePairBinding binding, IProgressMonitor monitor) {
		logInfo("Adding member value pair: " + binding.getName());
	}

	public void addPackage(PDOMTreeNode parent, IPackageBinding binding, IProgressMonitor monitor) {
		logInfo("Adding package: " + binding.getName());
	}

	public void addVariable(PDOMTreeNode parent, IVariableBinding binding, IProgressMonitor monitor) {
		logInfo("Adding variable: " + binding.getName());
	}

	public void addMethod(PDOMTreeNode parent, IMethodBinding binding, IProgressMonitor monitor) {
		logInfo("Adding method: " + binding.getName());
	}

	public void addAnnotation(PDOMTreeNode parent, IAnnotationBinding binding, IProgressMonitor monitor) {
		logInfo("Adding annotation: " + binding.getName());
	}

	public PDOMType addType(ITypeBinding binding, IProgressMonitor monitor) {
		logInfo("Adding type: " + binding.getBinaryName()); //$NON-NLS-1$

		PDOMTypeId name = makeTypeId(binding);
		PDOMType type = name.findTypeByResourceAddress(this.resource.address);

		if (type == null) {
			type = new PDOMType(getPDOM(), this.resource);
		}

		type.setTypeId(name);

		ITypeBinding superclass = binding.getSuperclass();

		if (superclass != null) {
			type.setSuperclass(makeTypeId(superclass));
		}

		for (ITypeBinding next : binding.getInterfaces()) {
			new PDOMTypeInterface(getPDOM(), type, makeTypeId(next));
		}

		return type;
	}

	private void logInfo(String string) {
		if (ENABLE_LOGGING) {
			Package.logInfo(string);
		}
	}

	private PDOMTypeId makeTypeId(ITypeBinding forBinding) {
		return this.index.createTypeId(JavaNames.binaryNameToFieldDescriptor(forBinding.getBinaryName().toCharArray()));
	}

	private PDOM getPDOM() {
		return resource.getPDOM();
	}
}
