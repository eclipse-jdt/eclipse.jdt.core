/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.ClassFile;

/**
 * Internal implementation of type bindings.
 */
class TypeBinding implements ITypeBinding {
	private static final IMethodBinding[] NO_METHOD_BINDINGS = new IMethodBinding[0];

	private static final String NO_NAME = ""; //$NON-NLS-1$	
	private static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	private static final IVariableBinding[] NO_VARIABLE_BINDINGS = new IVariableBinding[0];

	private static final int VALID_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
		Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.STRICTFP;
	
	private org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;
	private String key;
	private BindingResolver resolver;
	
	public TypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
		this.binding = binding;
		this.resolver = resolver;
	}

	/*
	 * @see ITypeBinding#getBinaryName()
	 * @since 3.0
	 */
	public String getBinaryName() {
		char[] constantPoolName = this.binding.constantPoolName();
		if (constantPoolName == null) return null;
		char[] dotSeparated = CharOperation.replaceOnCopy(constantPoolName, '/', '.');
		return new String(dotSeparated);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getBound()
	 */
	public ITypeBinding getBound() {
		if (this.binding.isWildcard()) {
			WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
			if (wildcardBinding.bound != null) {
				return this.resolver.getTypeBinding(wildcardBinding.bound);
			}
		}
		return null;
	}
	
	/*
	 * Returns the class file for the given file name, or null if not found.
	 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
	 */
	private IClassFile getClassFile(char[] fileName) {
		int lastSlash = CharOperation.lastIndexOf('/', fileName);
		if (lastSlash == -1) 
			lastSlash = CharOperation.lastIndexOf(File.separatorChar, fileName);
		if (lastSlash == -1)
			return null;
		IPackageFragment pkg = getPackageFragment(fileName, lastSlash);
		if (pkg == null) return null;
		char[] simpleName = CharOperation.subarray(fileName, lastSlash+1, fileName.length);
		return pkg.getClassFile(new String(simpleName));
	}
	
	/*
	 * Returns the compilation unit for the given file name, or null if not found.
	 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
	 */
	private ICompilationUnit getCompilationUnit(char[] fileName) {
		char[] slashSeparatedFileName = CharOperation.replaceOnCopy(fileName, File.separatorChar, '/');
		int lastSlash = CharOperation.lastIndexOf('/', slashSeparatedFileName);
		if (lastSlash == -1) return null;
		IPackageFragment pkg = getPackageFragment(slashSeparatedFileName, lastSlash);
		if (pkg == null) return null;
		char[] simpleName = CharOperation.subarray(slashSeparatedFileName, lastSlash+1, slashSeparatedFileName.length);
		ICompilationUnit cu = pkg.getCompilationUnit(new String(simpleName));
		if (this.resolver instanceof DefaultBindingResolver) {
			ICompilationUnit workingCopy = cu.findWorkingCopy(((DefaultBindingResolver) this.resolver).workingCopyOwner);
			if (workingCopy != null) 
				return workingCopy;
		}
		return cu;
	}

	/*
	 * @see ITypeBinding#getDeclaredFields()
	 */
	public IVariableBinding[] getDeclaredFields() {
		try {
			if (isClass() || isInterface() || isEnum()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				FieldBinding[] fields = referenceBinding.fields();
				int length = fields.length;
				IVariableBinding[] newFields = new IVariableBinding[length];
				for (int i = 0; i < length; i++) {
					newFields[i] = this.resolver.getVariableBinding(fields[i]);
				}
				return newFields;
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		return NO_VARIABLE_BINDINGS;
	}

	/*
	 * @see ITypeBinding#getDeclaredMethods()
	 */
	public IMethodBinding[] getDeclaredMethods() {
		try {
			if (isClass() || isInterface() || isEnum()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] methods = referenceBinding.methods();
				int length = methods.length;
				int removeSyntheticsCounter = 0;
				IMethodBinding[] newMethods = new IMethodBinding[length];
				for (int i = 0; i < length; i++) {
					org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding = methods[i];
					if (!shouldBeRemoved(methodBinding)) { 
						newMethods[removeSyntheticsCounter++] = this.resolver.getMethodBinding(methodBinding);
					}
				}
				if (removeSyntheticsCounter != length) {
					System.arraycopy(newMethods, 0, (newMethods = new IMethodBinding[removeSyntheticsCounter]), 0, removeSyntheticsCounter);
				}
				return newMethods;
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		return NO_METHOD_BINDINGS;
	}

	/*
	 * @see ITypeBinding#getDeclaredModifiers()
	 */
	public int getDeclaredModifiers() {
		return getModifiers();
	}

	/*
	 * @see ITypeBinding#getDeclaredTypes()
	 */
	public ITypeBinding[] getDeclaredTypes() {
		try {
			if (isClass() || isInterface() || isEnum()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				ReferenceBinding[] members = referenceBinding.memberTypes();
				int length = members.length;
				ITypeBinding[] newMembers = new ITypeBinding[length];
				for (int i = 0; i < length; i++) {
					newMembers[i] = this.resolver.getTypeBinding(members[i]);
				}
				return newMembers;
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		return NO_TYPE_BINDINGS;
	}

	/*
	 * @see ITypeBinding#getDeclaringMethod()
	 */
	public IMethodBinding getDeclaringMethod() {
		if (this.binding instanceof LocalTypeBinding) {
			LocalTypeBinding localTypeBinding = (LocalTypeBinding) this.binding;
			MethodBinding methodBinding = localTypeBinding.enclosingMethod;
			if (methodBinding != null) {
				try {
					return this.resolver.getMethodBinding(localTypeBinding.enclosingMethod);
				} catch (RuntimeException e) {
					/* in case a method cannot be resolvable due to missing jars on the classpath
					 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
					 */
				}
			}
		} else if (this.binding.isTypeVariable()) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			Binding declaringElement = typeVariableBinding.declaringElement;
			if (declaringElement instanceof MethodBinding) {
				try {
					return this.resolver.getMethodBinding((MethodBinding)declaringElement);
				} catch (RuntimeException e) {
					/* in case a method cannot be resolvable due to missing jars on the classpath
					 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
					 */
				}				
			}
		}
		return null;
	}

	/*
	 * @see ITypeBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isNestedType()) {
				try {
					return this.resolver.getTypeBinding(referenceBinding.enclosingType());
				} catch (RuntimeException e) {
					/* in case a method cannot be resolvable due to missing jars on the classpath
					 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
					 */
				}
			}
		} else if (this.binding.isTypeVariable()) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			Binding declaringElement = typeVariableBinding.declaringElement;
			if (declaringElement instanceof ReferenceBinding) {
				try {
					return this.resolver.getTypeBinding((ReferenceBinding)declaringElement);
				} catch (RuntimeException e) {
					/* in case a method cannot be resolvable due to missing jars on the classpath
					 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
					 */
				}				
			}
		}
		return null;
	}

	/*
	 * @see ITypeBinding#getDimensions()
	 */
	public int getDimensions() {
		if (!this.isArray()) {
			return 0;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return arrayBinding.dimensions;
	}

	/*
	 * @see ITypeBinding#getElementType()
	 */
	public ITypeBinding getElementType() {
		if (!this.isArray()) {
			return null;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return resolver.getTypeBinding(arrayBinding.leafComponentType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeDeclaration()
	 */
	public ITypeBinding getTypeDeclaration() {
		if (this.binding instanceof ParameterizedTypeBinding)
			return this.resolver.getTypeBinding(((ParameterizedTypeBinding)this.binding).type);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getErasure()
	 */
	public ITypeBinding getErasure() {
		return this.resolver.getTypeBinding(this.binding.erasure());
	}

	public ITypeBinding[] getInterfaces() {
		if (this.binding == null || this.binding.isArrayType() || this.binding.isBaseType()) {
			return NO_TYPE_BINDINGS;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding[] interfaces = null;
		try {
			interfaces = referenceBinding.superInterfaces();
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		if (interfaces == null) {
			return NO_TYPE_BINDINGS;
		}
		int length = interfaces.length;
		if (length == 0) {
			return NO_TYPE_BINDINGS;
		} else {
			ITypeBinding[] newInterfaces = new ITypeBinding[length];
			for (int i = 0; i < length; i++) {
				newInterfaces[i] = this.resolver.getTypeBinding(interfaces[i]);
			}
			return newInterfaces;
		}
	}
	
	public IJavaElement getJavaElement() {
		if (this.binding == null || this.binding.isArrayType() || this.binding.isBaseType()) return null;
		ReferenceBinding referenceBinding;
		if (this.binding.isParameterizedType() || this.binding.isRawType())
			referenceBinding = (ReferenceBinding) this.binding.erasure();
		else
			referenceBinding = (ReferenceBinding) this.binding;
		char[] fileName = referenceBinding.getFileName();
		if (Util.isClassFileName(fileName)) {
			ClassFile classFile = (ClassFile) getClassFile(fileName);
			if (classFile == null) return null;
			return classFile.getType();
		}
		if (referenceBinding.isLocalType() || referenceBinding.isAnonymousType()) {
			// local or anonymous type
			ICompilationUnit cu = getCompilationUnit(fileName);
			if (cu == null) return null;
			if (!(this.resolver instanceof DefaultBindingResolver)) return null;
			DefaultBindingResolver bindingResolver = (DefaultBindingResolver) this.resolver;
			ASTNode node = (ASTNode) bindingResolver.bindingsToAstNodes.get(this);
			// must use getElementAt(...) as there is no back pointer to the defining method (scope is null after resolution has ended)
			try {
				return cu.getElementAt(node.getStartPosition());
			} catch (JavaModelException e) {
				// does not exist
				return null;
			}
		} else if (referenceBinding.isTypeVariable()) {
			// type parameter
			final String typeVariableName = new String(referenceBinding.sourceName());
			Binding declaringElement = ((TypeVariableBinding) referenceBinding).declaringElement;
			IBinding declaringTypeBinding = null;
			if (declaringElement instanceof MethodBinding) {
				declaringTypeBinding = this.resolver.getMethodBinding((MethodBinding) declaringElement);
				IMethod declaringMethod = (IMethod) declaringTypeBinding.getJavaElement();
				return declaringMethod.getTypeParameter(typeVariableName);
			} else {
				declaringTypeBinding = this.resolver.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaringElement);
				IType declaringType = (IType) declaringTypeBinding.getJavaElement();
				return declaringType.getTypeParameter(typeVariableName);
			}
		} else {
			if (fileName == null) return null; // case of a WilCardBinding that doesn't have a corresponding Java element
			// member or top level type
			ITypeBinding declaringTypeBinding = getDeclaringClass();
			if (declaringTypeBinding == null) {
				// top level type
				ICompilationUnit cu = getCompilationUnit(fileName);
				if (cu == null) return null;
				return cu.getType(new String(referenceBinding.sourceName()));
			} else {
				// member type
				IType declaringType = (IType) declaringTypeBinding.getJavaElement();
				if (declaringType == null) return null;
				return declaringType.getType(new String(referenceBinding.sourceName()));
			}
		}
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			this.key = new String(this.binding.computeUniqueKey());
		}
		return this.key;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.TYPE;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		if (isClass()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			if (referenceBinding.isAnonymousType()) {
				return accessFlags & ~Modifier.FINAL;
			}
			return accessFlags;
		} else if (isAnnotation()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			// clear the AccAbstract, AccAnnotation and the AccInterface bits
			return accessFlags & ~(IConstants.AccAbstract | IConstants.AccInterface | IConstants.AccAnnotation);			
		} else if (isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			// clear the AccAbstract and the AccInterface bits
			return accessFlags & ~(IConstants.AccAbstract | IConstants.AccInterface);
		} else if (isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			// clear the AccEnum bits
			return accessFlags & ~IConstants.AccEnum;
		} else {
			return 0;
		}
	}

	public String getName() {
		if (isWildcardType()) {
			WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
			StringBuffer buffer = new StringBuffer();
			buffer.append(TypeConstants.WILDCARD_NAME);
			if (wildcardBinding.bound != null) {
				switch(wildcardBinding.kind) {
			        case Wildcard.SUPER :
			        	buffer.append(TypeConstants.WILDCARD_SUPER);
			            break;
			        case Wildcard.EXTENDS :
			        	buffer.append(TypeConstants.WILDCARD_EXTENDS);
				}
				buffer.append(getBound().getName());
			}
			return String.valueOf(buffer);
		}
		if (isParameterizedType()) {
			ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
			StringBuffer buffer = new StringBuffer();
			buffer.append(parameterizedTypeBinding.sourceName());
			ITypeBinding[] typeArguments = getTypeArguments();
			final int typeArgumentsLength = typeArguments.length;
			if (typeArgumentsLength != 0) {
				buffer.append('<');
				for (int i = 0, max = typeArguments.length; i < max; i++) {
					if (i > 0) {
						buffer.append(',');
					}
					buffer.append(typeArguments[i].getName());
				}
				buffer.append('>');	
			}
			return String.valueOf(buffer);
		}
		if (isRawType()) {
			return getTypeDeclaration().getName();
		}
		if (isPrimitive() || isNullType()) {
			BaseTypeBinding baseTypeBinding = (BaseTypeBinding) this.binding;
			return new String(baseTypeBinding.simpleName);
		}
		if (isArray()) {
			int dimensions = getDimensions();
			char[] brackets = new char[dimensions * 2];
			for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
				brackets[i] = ']';
				brackets[i - 1] = '[';
			}
			StringBuffer buffer = new StringBuffer(getElementType().getName());
			buffer.append(brackets);
			return String.valueOf(buffer);
		}
		if (isAnonymous()) {
			return NO_NAME;
		}
		if (isTypeVariable()) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			return new String(typeVariableBinding.sourceName);
		}
		return new String(this.binding.sourceName());
	}
	
	/*
	 * @see ITypeBinding#getPackage()
	 */
	public IPackageBinding getPackage() {
		if (this.binding.isBaseType() || this.binding.isArrayType()) {
			return null;
		} else {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return this.resolver.getPackageBinding(referenceBinding.getPackage());
		}
	}
	
	/*
	 * Returns the package that includes the given file name, or null if not found.
	 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
	 */
	private IPackageFragment getPackageFragment(char[] fileName, int lastSlash) {
		int jarSeparator = CharOperation.indexOf(IDependent.JAR_FILE_ENTRY_SEPARATOR, fileName);
		if (jarSeparator != -1) {
			String jarMemento = new String(CharOperation.subarray(fileName, 0, jarSeparator));
			IPackageFragmentRoot root = (IPackageFragmentRoot) JavaCore.create(jarMemento);
			char[] pkgName = CharOperation.subarray(fileName, jarSeparator+1, lastSlash);
			CharOperation.replace(pkgName, '/', '.');
			return root.getPackageFragment(new String(pkgName));
		} else {
			Path path = new Path(new String(CharOperation.subarray(fileName, 0, lastSlash)));
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IContainer folder = path.segmentCount() == 1 ? workspaceRoot.getProject(path.lastSegment()) : (IContainer) workspaceRoot.getFolder(path);
			IJavaElement element = JavaCore.create(folder);
			if (element == null) return null;
			switch (element.getElementType()) {
				case IJavaElement.PACKAGE_FRAGMENT:
					return (IPackageFragment) element;
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
					return ((IPackageFragmentRoot) element).getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
				case IJavaElement.JAVA_PROJECT:
					IPackageFragmentRoot root = ((IJavaProject) element).getPackageFragmentRoot(folder);
					if (root == null) return null;
					return root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
			}
			return null;
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getQualifiedName()
	 */
	public String getQualifiedName() {
		if (isAnonymous() || isLocal()) {
			return NO_NAME;
		}
		if (isPrimitive() || isNullType()) {
			BaseTypeBinding baseTypeBinding = (BaseTypeBinding) this.binding;
			return new String(baseTypeBinding.simpleName);
		}
		if (isWildcardType()) {
			WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
			StringBuffer buffer = new StringBuffer();
			buffer.append(TypeConstants.WILDCARD_NAME);
			final ITypeBinding bound = getBound();
			if (bound != null) {
				switch(wildcardBinding.kind) {
			        case Wildcard.SUPER :
			        	buffer.append(TypeConstants.WILDCARD_SUPER);
			            break;
			        case Wildcard.EXTENDS :
			        	buffer.append(TypeConstants.WILDCARD_EXTENDS);
				}
				buffer.append(bound.getQualifiedName());
			}
			return String.valueOf(buffer);
		}
		if (isRawType()) {
			return getTypeDeclaration().getQualifiedName();
		}
		if (isArray()) {
			ITypeBinding elementType = getElementType();
			if (elementType.isLocal() || elementType.isAnonymous()) {
				return NO_NAME;
			}
			final int dimensions = getDimensions();
			char[] brackets = new char[dimensions * 2];
			for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
				brackets[i] = ']';
				brackets[i - 1] = '[';
			}
			StringBuffer buffer = new StringBuffer(elementType.getQualifiedName());
			buffer.append(brackets);
			return String.valueOf(buffer);
		}
		if (isTypeVariable()) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			return new String(typeVariableBinding.sourceName);
		}
		if (isMember()) {
			StringBuffer buffer = new StringBuffer();
			buffer
				.append(getDeclaringClass().getQualifiedName())
				.append('.');
			if (isParameterizedType()) {
				ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
				buffer.append(parameterizedTypeBinding.sourceName());
				ITypeBinding[] typeArguments = getTypeArguments();
				final int typeArgumentsLength = typeArguments.length;
				if (typeArgumentsLength != 0) {
					buffer.append('<');
					for (int i = 0, max = typeArguments.length; i < max; i++) {
						if (i > 0) {
							buffer.append(',');
						}
						buffer.append(typeArguments[i].getQualifiedName());
					}
					buffer.append('>');	
				}
			} else {
				buffer.append(getName());
			}
			return String.valueOf(buffer);
		}
		if (isParameterizedType()) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(getTypeDeclaration().getQualifiedName());
			ITypeBinding[] typeArguments = getTypeArguments();
			final int typeArgumentsLength = typeArguments.length;
			if (typeArgumentsLength != 0) {
				buffer.append('<');
				for (int i = 0, max = typeArguments.length; i < max; i++) {
					if (i > 0) {
						buffer.append(',');
					}
					buffer.append(typeArguments[i].getQualifiedName());
				}
				buffer.append('>');
			}
			return String.valueOf(buffer);
		}
		if (isRawType()) {
			return getTypeDeclaration().getQualifiedName();
		}
		PackageBinding packageBinding = this.binding.getPackage();
		
		StringBuffer buffer = new StringBuffer();
		if (packageBinding != null && packageBinding.compoundName != CharOperation.NO_CHAR_CHAR) {
			buffer.append(CharOperation.concatWith(packageBinding.compoundName, '.')).append('.');
		}
		buffer.append(getName());

		return String.valueOf(buffer);
	}

	/*
	 * @see ITypeBinding#getSuperclass()
	 */
	public ITypeBinding getSuperclass() {
		if (this.binding == null || this.binding.isArrayType() || this.binding.isBaseType() || this.binding.isInterface()) {
			return null;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding superclass = null;
		try {
			superclass = referenceBinding.superclass();
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			return this.resolver.resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$
		}
		if (superclass == null) {
			return null;
		}
		return this.resolver.getTypeBinding(superclass);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeArguments()
	 */
	public ITypeBinding[] getTypeArguments() {
		if (this.binding.isParameterizedType()) {
			ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
			final org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] arguments = parameterizedTypeBinding.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				ITypeBinding[] typeArguments = new ITypeBinding[argumentsLength];
				for (int i = 0; i < argumentsLength; i++) {
					typeArguments[i] = this.resolver.getTypeBinding(arguments[i]);
				}
				return typeArguments;
			}
		}
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeBounds()
	 */
	public ITypeBinding[] getTypeBounds() {
		if (this.binding instanceof TypeVariableBinding) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			int boundsNumber = 0;
			ReferenceBinding superclass = typeVariableBinding.superclass();
			if (superclass != null) {
				boundsNumber++;
			}
			ReferenceBinding[] superinterfaces = typeVariableBinding.superInterfaces();
			int superinterfacesLength = 0;
			if (superinterfaces != null) {
				superinterfacesLength = superinterfaces.length;
				boundsNumber += superinterfacesLength;
			}
			if (boundsNumber != 0) {
				ITypeBinding[] typeBounds = new ITypeBinding[boundsNumber];
				int boundsIndex = 0;
				if (superclass != null) {
					typeBounds[boundsIndex++] = this.resolver.getTypeBinding(superclass);
				}
				if (superinterfaces != null) {
					for (int i = 0; i < superinterfacesLength; i++, boundsIndex++) {
						typeBounds[boundsIndex] = this.resolver.getTypeBinding(superinterfaces[i]);
					}
				}
				return typeBounds;
			}
		}
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeParameters()
	 */
	public ITypeBinding[] getTypeParameters() {
		TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
		if (typeVariableBindings != null) {
			int typeVariableBindingsLength = typeVariableBindings.length;
			if (typeVariableBindingsLength != 0) {
				ITypeBinding[] typeParameters = new ITypeBinding[typeVariableBindingsLength];
				for (int i = 0; i < typeVariableBindingsLength; i++) {
					typeParameters[i] = this.resolver.getTypeBinding(typeVariableBindings[i]);
				}
				return typeParameters;
			}
		}
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isGenericType()
	 * @since 3.1
	 */
	public boolean isGenericType() {
		// equivalent to return getTypeParameters().length > 0;
		if (isRawType()) {
			return false;
		}
		TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
		return (typeVariableBindings != null && typeVariableBindings.length > 0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isAnnotation()
	 */
	public boolean isAnnotation() {
		return this.binding.isAnnotationType();
	}

	/*
	 * @see ITypeBinding#isAnonymous()
	 */
	public boolean isAnonymous() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isAnonymousType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isArray()
	 */
	public boolean isArray() {
		return binding.isArrayType();
	}
	
	/* (non-Javadoc)
	 * @see ITypeBinding#isAssignmentCompatible(ITypeBinding)
	 */
	public boolean isAssignmentCompatible(ITypeBinding type) {
		if (this == type) return true;
		TypeBinding other = (TypeBinding) type;
		Scope scope = this.resolver.scope();
		if (scope == null) return false;
		return this.binding.isCompatibleWith(other.binding) || scope.isBoxingCompatibleWith(this.binding, other.binding);
	}
	
	/* (non-Javadoc)
	 * @see ITypeBinding#isCastCompatible(ITypeBinding)
	 */
	public boolean isCastCompatible(ITypeBinding type) {
		Expression expression = new Expression() {
			public StringBuffer printExpression(int indent,StringBuffer output) {
				return null;
			}
		};
		Scope scope = this.resolver.scope();
		if (scope == null) return false;
		return expression.checkCastTypesCompatibility(scope, this.binding, ((TypeBinding) type).binding, null);
	}

	/*
	 * @see ITypeBinding#isClass()
	 */
	public boolean isClass() {
		return this.binding.isClass() && !this.binding.isTypeVariable();
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isDeprecated();
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ITypeBinding#isEnum()
	 */
	public boolean isEnum() {
		return this.binding.isEnum();
	}

	/*
	 * @see IBinding#isEqualTo(Binding)
	 * @since 3.1
	 */
	public boolean isEqualTo(IBinding other) {
		if (other == this) {
			// identical binding - equal (key or no key)
			return true;
		}
		if (other == null) {
			// other binding missing
			return false;
		}
		if (!(other instanceof TypeBinding)) {
			return false;
		}
		org.eclipse.jdt.internal.compiler.lookup.TypeBinding otherBinding = ((TypeBinding) other).binding;
		// check return type
		return BindingComparator.isEqual(this.binding, otherBinding);
	}
	
	/*
	 * @see ITypeBinding#isFromSource()
	 */
	public boolean isFromSource() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isRawType()) {
				return !((RawTypeBinding) referenceBinding).type.isBinaryBinding();
			} else if (referenceBinding.isParameterizedType()) {
				ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) referenceBinding;
				org.eclipse.jdt.internal.compiler.lookup.TypeBinding erasure = parameterizedTypeBinding.erasure();
				if (erasure instanceof ReferenceBinding) {
					return !((ReferenceBinding) erasure).isBinaryBinding();
				}
				return false;
			} else {
				return !referenceBinding.isBinaryBinding();
			}
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isInterface()
	 */
	public boolean isInterface() {
		return this.binding.isInterface();
	}

	/*
	 * @see ITypeBinding#isLocal()
	 */
	public boolean isLocal() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isLocalType() && !referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isMember()
	 */
	public boolean isMember() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isNested()
	 */
	public boolean isNested() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isNestedType();
		}
		return false;
	}
	
	/**
	 * @see ITypeBinding#isNullType()
	 */
	public boolean isNullType() {
		return this.binding == BaseTypes.NullBinding;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isParameterizedType()
	 */
	public boolean isParameterizedType() {
		return this.binding.isParameterizedType() && ((ParameterizedTypeBinding) this.binding).arguments != null;
	}
	
	/*
	 * @see ITypeBinding#isPrimitive()
	 */
	public boolean isPrimitive() {
		return !isNullType() && binding.isBaseType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isRawType()
	 */
	public boolean isRawType() {
		return this.binding.isRawType();
	}

	/* (non-Javadoc)
	 * @see ITypeBinding#isSubTypeCompatible(ITypeBinding)
	 */
	public boolean isSubTypeCompatible(ITypeBinding type) {
		if (this == type) return true;
		if (this.binding.isBaseType()) return false;
		TypeBinding other = (TypeBinding) type;
		if (other.binding.isBaseType()) return false;
		return this.binding.isCompatibleWith(other.binding);
	}
	
	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/*
	 * @see ITypeBinding#isTopLevel()
	 */
	public boolean isTopLevel() {
		if (isClass() || isInterface() || isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isTypeVariable()
	 */
	public boolean isTypeVariable() {
		return this.binding.isTypeVariable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isUpperbound()
	 */
	public boolean isUpperbound() {
		return this.binding.isWildcard() && ((WildcardBinding) this.binding).kind == Wildcard.EXTENDS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isWildcardType()
	 */
	public boolean isWildcardType() {
		return this.binding.isWildcard();
	}

	private boolean shouldBeRemoved(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		return methodBinding.isDefaultAbstract() || methodBinding.isSynthetic() || (methodBinding.isConstructor() && isInterface());
	}
	
	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}
