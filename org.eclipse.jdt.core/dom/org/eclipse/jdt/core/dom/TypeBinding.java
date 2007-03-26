/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
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
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * Internal implementation of type bindings.
 */
class TypeBinding implements ITypeBinding {
	protected static final IMethodBinding[] NO_METHOD_BINDINGS = new IMethodBinding[0];

	private static final String NO_NAME = ""; //$NON-NLS-1$
	protected static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	protected static final IVariableBinding[] NO_VARIABLE_BINDINGS = new IVariableBinding[0];

	private static final int VALID_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
		Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.STRICTFP;

	org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;
	private String key;
	private BindingResolver resolver;
	private IVariableBinding[] fields;
	private IAnnotationBinding[] annotations;
	private IMethodBinding[] methods;
	private ITypeBinding[] members;
	private ITypeBinding[] interfaces;
	private ITypeBinding[] typeArguments;
	private ITypeBinding[] bounds;
	private ITypeBinding[] typeParameters;

	public TypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
		this.binding = binding;
		this.resolver = resolver;
	}

	public ITypeBinding createArrayType(int dimension) {
		int realDimensions = dimension;
		realDimensions += this.getDimensions();
		if (realDimensions < 1 || realDimensions > 255) {
			throw new IllegalArgumentException();
		}
		return this.resolver.resolveArrayType(this, dimension);
	}

	public IAnnotationBinding[] getAnnotations() {
		if (this.annotations != null) {
			return this.annotations;
		}
		if (this.binding.isAnnotationType() || this.binding.isClass() || this.binding.isEnum() || this.binding.isInterface()) {
			org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding refType =
				(org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding) this.binding;
			org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding[] internalAnnotations = refType.getAnnotations();
			int length = internalAnnotations == null ? 0 : internalAnnotations.length;
			if (length != 0) {
				IAnnotationBinding[] domInstances = new IAnnotationBinding[length];
				for (int i = 0; i < length; i++) {
					final IAnnotationBinding annotationInstance = this.resolver.getAnnotationInstance(internalAnnotations[i]);
					if (annotationInstance == null) {
						return AnnotationBinding.NoAnnotations;
					}
					domInstances[i] = annotationInstance;
				}
				return this.annotations = domInstances;
			}
		}
		return this.annotations = AnnotationBinding.NoAnnotations;
	}

	/*
	 * @see ITypeBinding#getBinaryName()
	 * @since 3.0
	 */
	public String getBinaryName() {
		if (this.binding.isTypeVariable()) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			org.eclipse.jdt.internal.compiler.lookup.Binding declaring = typeVariableBinding.declaringElement;
			StringBuffer binaryName = new StringBuffer();
			switch(declaring.kind()) {
				case org.eclipse.jdt.internal.compiler.lookup.Binding.METHOD :
					MethodBinding methodBinding = (MethodBinding) declaring;
					char[] constantPoolName = methodBinding.declaringClass.constantPoolName();
					if (constantPoolName == null) return null;
					binaryName
						.append(CharOperation.replaceOnCopy(constantPoolName, '/', '.'))
						.append('$')
						.append(methodBinding.signature())
						.append('$')
						.append(typeVariableBinding.sourceName);
					break;
				default :
					org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding = (org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaring;
					constantPoolName = typeBinding.constantPoolName();
					if (constantPoolName == null) return null;
					binaryName
						.append(CharOperation.replaceOnCopy(constantPoolName, '/', '.'))
						.append('$')
						.append(typeVariableBinding.sourceName);
			}
			return String.valueOf(binaryName);
		}
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
		int jarSeparator = CharOperation.indexOf(IDependent.JAR_FILE_ENTRY_SEPARATOR, fileName);
		int pkgEnd = CharOperation.lastIndexOf('/', fileName); // pkgEnd is exclusive
		if (pkgEnd == -1)
			pkgEnd = CharOperation.lastIndexOf(File.separatorChar, fileName);
		if (jarSeparator != -1 && pkgEnd < jarSeparator) // if in a jar and no slash, it is a default package -> pkgEnd should be equal to jarSeparator
			pkgEnd = jarSeparator;
		if (pkgEnd == -1)
			return null;
		IPackageFragment pkg = getPackageFragment(fileName, pkgEnd, jarSeparator);
		if (pkg == null) return null;
		int start;
		return pkg.getClassFile(new String(fileName, start = pkgEnd + 1, fileName.length - start));
	}

	/*
	 * Returns the compilation unit for the given file name, or null if not found.
	 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
	 */
	private ICompilationUnit getCompilationUnit(char[] fileName) {
		char[] slashSeparatedFileName = CharOperation.replaceOnCopy(fileName, File.separatorChar, '/');
		int pkgEnd = CharOperation.lastIndexOf('/', slashSeparatedFileName); // pkgEnd is exclusive
		if (pkgEnd == -1)
			return null;
		IPackageFragment pkg = getPackageFragment(slashSeparatedFileName, pkgEnd, -1/*no jar separator for .java files*/);
		if (pkg == null) return null;
		int start;
		ICompilationUnit cu = pkg.getCompilationUnit(new String(slashSeparatedFileName, start =  pkgEnd+1, slashSeparatedFileName.length - start));
		if (this.resolver instanceof DefaultBindingResolver) {
			ICompilationUnit workingCopy = cu.findWorkingCopy(((DefaultBindingResolver) this.resolver).workingCopyOwner);
			if (workingCopy != null)
				return workingCopy;
		}
		return cu;
	}

	/*
	 * @see ITypeBinding#getComponentType()
	 */
	public ITypeBinding getComponentType() {
		if (!this.isArray()) {
			return null;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return resolver.getTypeBinding(arrayBinding.elementsType());
	}

	/*
	 * @see ITypeBinding#getDeclaredFields()
	 */
	public IVariableBinding[] getDeclaredFields() {
		if (this.fields != null) {
			return this.fields;
		}
		try {
			if (isClass() || isInterface() || isEnum()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				FieldBinding[] fieldBindings = referenceBinding.fields();
				int length = fieldBindings.length;
				if (length != 0) {
					IVariableBinding[] newFields = new IVariableBinding[length];
					for (int i = 0; i < length; i++) {
						IVariableBinding variableBinding = this.resolver.getVariableBinding(fieldBindings[i]);
						if (variableBinding == null) {
							return this.fields = NO_VARIABLE_BINDINGS;
						}
						newFields[i] = variableBinding;
					}
					return this.fields = newFields;
				}
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declared fields"); //$NON-NLS-1$
		}
		return this.fields = NO_VARIABLE_BINDINGS;
	}

	/*
	 * @see ITypeBinding#getDeclaredMethods()
	 */
	public IMethodBinding[] getDeclaredMethods() {
		if (this.methods != null) {
			return this.methods;
		}
		try {
			if (isClass() || isInterface() || isEnum()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] internalMethods = referenceBinding.methods();
				int length = internalMethods.length;
				if (length != 0) {
					int removeSyntheticsCounter = 0;
					IMethodBinding[] newMethods = new IMethodBinding[length];
					for (int i = 0; i < length; i++) {
						org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding = internalMethods[i];
						if (!shouldBeRemoved(methodBinding)) {
							IMethodBinding methodBinding2 = this.resolver.getMethodBinding(methodBinding);
							if (methodBinding2 != null) {
								newMethods[removeSyntheticsCounter++] = methodBinding2;
							}
						}
					}
					if (removeSyntheticsCounter != length) {
						System.arraycopy(newMethods, 0, (newMethods = new IMethodBinding[removeSyntheticsCounter]), 0, removeSyntheticsCounter);
					}
					return this.methods = newMethods;
				}
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declared methods"); //$NON-NLS-1$
		}
		return this.methods = NO_METHOD_BINDINGS;
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
		if (this.members != null) {
			return this.members;
		}
		try {
			if (isClass() || isInterface() || isEnum()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				ReferenceBinding[] internalMembers = referenceBinding.memberTypes();
				int length = internalMembers.length;
				if (length != 0) {
					ITypeBinding[] newMembers = new ITypeBinding[length];
					for (int i = 0; i < length; i++) {
						ITypeBinding typeBinding = this.resolver.getTypeBinding(internalMembers[i]);
						if (typeBinding == null) {
							return this.members = NO_TYPE_BINDINGS;
						}
						newMembers[i] = typeBinding;
					}
					return this.members = newMembers;
				}
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declared methods"); //$NON-NLS-1$
		}
		return this.members = NO_TYPE_BINDINGS;
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
					org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declaring method"); //$NON-NLS-1$
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
					org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declaring method"); //$NON-NLS-1$
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
					org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declaring class"); //$NON-NLS-1$
				}
			}
		} else if (this.binding.isTypeVariable()) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			Binding declaringElement = typeVariableBinding.isCapture() ? ((CaptureBinding) typeVariableBinding).sourceType : typeVariableBinding.declaringElement;
			if (declaringElement instanceof ReferenceBinding) {
				try {
					return this.resolver.getTypeBinding((ReferenceBinding)declaringElement);
				} catch (RuntimeException e) {
					/* in case a method cannot be resolvable due to missing jars on the classpath
					 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
					 */
					org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve declaring class"); //$NON-NLS-1$
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
		if (this.interfaces != null) {
			return this.interfaces;
		}
		if (this.binding == null)
			return this.interfaces = NO_TYPE_BINDINGS;
		switch (this.binding.kind()) {
			case Binding.ARRAY_TYPE :
			case Binding.BASE_TYPE :
				return this.interfaces = NO_TYPE_BINDINGS;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding[] internalInterfaces = null;
		try {
			internalInterfaces = referenceBinding.superInterfaces();
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve interfaces"); //$NON-NLS-1$
		}
		int length = internalInterfaces == null ? 0 : internalInterfaces.length;
		if (length != 0) {
			ITypeBinding[] newInterfaces = new ITypeBinding[length];
			int interfacesCounter = 0;
			for (int i = 0; i < length; i++) {
				ITypeBinding typeBinding = this.resolver.getTypeBinding(internalInterfaces[i]);
				if (typeBinding == null) {
					continue;
				}
				newInterfaces[interfacesCounter++] = typeBinding;
			}
			if (length != interfacesCounter) {
				System.arraycopy(newInterfaces, 0, (newInterfaces = new ITypeBinding[interfacesCounter]), 0, interfacesCounter);
			}
			return this.interfaces = newInterfaces;
		}
		return this.interfaces = NO_TYPE_BINDINGS;
	}

	public IJavaElement getJavaElement() {
		JavaElement element = getUnresolvedJavaElement();
		if (element == null)
			return null;
		return element.resolved(this.binding);
	}

	private JavaElement getUnresolvedJavaElement() {
		return getUnresolvedJavaElement(this.binding);
	}
	private JavaElement getUnresolvedJavaElement(org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding ) {
		if (typeBinding == null)
			return null;
		switch (typeBinding.kind()) {
			case Binding.ARRAY_TYPE :
				typeBinding = ((ArrayBinding) typeBinding).leafComponentType();
				return getUnresolvedJavaElement(typeBinding);
			case Binding.BASE_TYPE :
			case Binding.WILDCARD_TYPE :
				return null;
			default :
				if (typeBinding.isCapture())
					return null;
		}
		ReferenceBinding referenceBinding;
		if (typeBinding.isParameterizedType() || typeBinding.isRawType())
			referenceBinding = (ReferenceBinding) typeBinding.erasure();
		else
			referenceBinding = (ReferenceBinding) typeBinding;
		char[] fileName = referenceBinding.getFileName();
		if (referenceBinding.isLocalType() || referenceBinding.isAnonymousType()) {
			// local or anonymous type
			if (Util.isClassFileName(fileName)) {
				int jarSeparator = CharOperation.indexOf(IDependent.JAR_FILE_ENTRY_SEPARATOR, fileName);
				int pkgEnd = CharOperation.lastIndexOf('/', fileName); // pkgEnd is exclusive
				if (pkgEnd == -1)
					pkgEnd = CharOperation.lastIndexOf(File.separatorChar, fileName);
				if (jarSeparator != -1 && pkgEnd < jarSeparator) // if in a jar and no slash, it is a default package -> pkgEnd should be equal to jarSeparator
					pkgEnd = jarSeparator;
				if (pkgEnd == -1)
					return null;
				IPackageFragment pkg = getPackageFragment(fileName, pkgEnd, jarSeparator);
				char[] constantPoolName = referenceBinding.constantPoolName();
				if (constantPoolName == null) {
					ClassFile classFile = (ClassFile) getClassFile(fileName);
					return classFile == null ? null : (JavaElement) classFile.getType();
				}
				pkgEnd = CharOperation.lastIndexOf('/', constantPoolName);
				char[] classFileName = CharOperation.subarray(constantPoolName, pkgEnd+1, constantPoolName.length);
				ClassFile classFile = (ClassFile) pkg.getClassFile(new String(classFileName) + SuffixConstants.SUFFIX_STRING_class);
				return (JavaElement) classFile.getType();
			}
			ICompilationUnit cu = getCompilationUnit(fileName);
			if (cu == null) return null;
			// must use getElementAt(...) as there is no back pointer to the defining method (scope is null after resolution has ended)
			try {
				int sourceStart = ((LocalTypeBinding) referenceBinding).sourceStart;
				return (JavaElement) cu.getElementAt(sourceStart);
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
				return (JavaElement) declaringMethod.getTypeParameter(typeVariableName);
			} else {
				ITypeBinding typeBinding2 = this.resolver.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaringElement);
				if (typeBinding2 == null) return null;
				declaringTypeBinding = typeBinding2;
				IType declaringType = (IType) declaringTypeBinding.getJavaElement();
				return (JavaElement) declaringType.getTypeParameter(typeVariableName);
			}
		} else {
			if (fileName == null) return null; // case of a WilCardBinding that doesn't have a corresponding Java element
			// member or top level type
			ITypeBinding declaringTypeBinding = null;
			if (this.isArray()) {
				declaringTypeBinding = this.getElementType().getDeclaringClass();
			} else {
				declaringTypeBinding = this.getDeclaringClass();
			}
			if (declaringTypeBinding == null) {
				// top level type
				if (Util.isClassFileName(fileName)) {
					ClassFile classFile = (ClassFile) getClassFile(fileName);
					if (classFile == null) return null;
					return (JavaElement) classFile.getType();
				}
				ICompilationUnit cu = getCompilationUnit(fileName);
				if (cu == null) return null;
				return (JavaElement) cu.getType(new String(referenceBinding.sourceName()));
			} else {
				// member type
				IType declaringType = (IType) declaringTypeBinding.getJavaElement();
				if (declaringType == null) return null;
				return (JavaElement) declaringType.getType(new String(referenceBinding.sourceName()));
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
			return accessFlags & ~(ClassFileConstants.AccAbstract | ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation);
		} else if (isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			// clear the AccAbstract and the AccInterface bits
			return accessFlags & ~(ClassFileConstants.AccAbstract | ClassFileConstants.AccInterface);
		} else if (isEnum()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			// clear the AccEnum bits
			return accessFlags & ~ClassFileConstants.AccEnum;
		} else {
			return Modifier.NONE;
		}
	}

	public String getName() {
		StringBuffer buffer;
		switch (this.binding.kind()) {

			case Binding.WILDCARD_TYPE :
				WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
				buffer = new StringBuffer();
				buffer.append(TypeConstants.WILDCARD_NAME);
				if (wildcardBinding.bound != null) {
					switch(wildcardBinding.boundKind) {
				        case Wildcard.SUPER :
				        	buffer.append(TypeConstants.WILDCARD_SUPER);
				            break;
				        case Wildcard.EXTENDS :
				        	buffer.append(TypeConstants.WILDCARD_EXTENDS);
					}
					buffer.append(getBound().getName());
				}
				return String.valueOf(buffer);

			case Binding.TYPE_PARAMETER :
				if (isCapture()) {
					return NO_NAME;
				}
				TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
				return new String(typeVariableBinding.sourceName);

			case Binding.PARAMETERIZED_TYPE :
				ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
				buffer = new StringBuffer();
				buffer.append(parameterizedTypeBinding.sourceName());
				ITypeBinding[] tArguments = getTypeArguments();
				final int typeArgumentsLength = tArguments.length;
				if (typeArgumentsLength != 0) {
					buffer.append('<');
					for (int i = 0; i < typeArgumentsLength; i++) {
						if (i > 0) {
							buffer.append(',');
						}
						buffer.append(tArguments[i].getName());
					}
					buffer.append('>');
				}
				return String.valueOf(buffer);

			case Binding.RAW_TYPE :
				return getTypeDeclaration().getName();

			case Binding.ARRAY_TYPE :
				ITypeBinding elementType = getElementType();
				if (elementType.isLocal() || elementType.isAnonymous() || elementType.isCapture()) {
					return NO_NAME;
				}
				int dimensions = getDimensions();
				char[] brackets = new char[dimensions * 2];
				for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
					brackets[i] = ']';
					brackets[i - 1] = '[';
				}
				buffer = new StringBuffer(elementType.getName());
				buffer.append(brackets);
				return String.valueOf(buffer);

			default :
				if (isPrimitive() || isNullType()) {
					BaseTypeBinding baseTypeBinding = (BaseTypeBinding) this.binding;
					return new String(baseTypeBinding.simpleName);
				}
				if (isAnonymous()) {
					return NO_NAME;
				}
				return new String(this.binding.sourceName());
		}
	}

	/*
	 * @see ITypeBinding#getPackage()
	 */
	public IPackageBinding getPackage() {
		switch (this.binding.kind()) {
			case Binding.BASE_TYPE :
			case Binding.ARRAY_TYPE :
			case Binding.TYPE_PARAMETER : // includes capture scenario
			case Binding.WILDCARD_TYPE :
				return null;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		return this.resolver.getPackageBinding(referenceBinding.getPackage());
	}

	/*
	 * Returns the package that includes the given file name, or null if not found.
	 * pkgEnd == jarSeparator if default package in a jar
	 * pkgEnd > jarSeparator if non default package in a jar
	 * pkgEnd > 0 if package not in a jar
	 *
	 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
	 */
	private IPackageFragment getPackageFragment(char[] fileName, int pkgEnd, int jarSeparator) {
		if (jarSeparator != -1) {
			String jarMemento = new String(fileName, 0, jarSeparator);
			IPackageFragmentRoot root = (IPackageFragmentRoot) JavaCore.create(jarMemento);
			if (pkgEnd == jarSeparator)
				return root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
			char[] pkgName = CharOperation.subarray(fileName, jarSeparator+1, pkgEnd);
			CharOperation.replace(pkgName, '/', '.');
			return root.getPackageFragment(new String(pkgName));
		} else {
			Path path = new Path(new String(fileName, 0, pkgEnd));
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
		StringBuffer buffer;
		switch (this.binding.kind()) {

			case Binding.WILDCARD_TYPE :
				WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
				buffer = new StringBuffer();
				buffer.append(TypeConstants.WILDCARD_NAME);
				final ITypeBinding bound = getBound();
				if (bound != null) {
					switch(wildcardBinding.boundKind) {
				        case Wildcard.SUPER :
				        	buffer.append(TypeConstants.WILDCARD_SUPER);
				            break;
				        case Wildcard.EXTENDS :
				        	buffer.append(TypeConstants.WILDCARD_EXTENDS);
					}
					buffer.append(bound.getQualifiedName());
				}
				return String.valueOf(buffer);

			case Binding.RAW_TYPE :
				return getTypeDeclaration().getQualifiedName();

			case Binding.ARRAY_TYPE :
				ITypeBinding elementType = getElementType();
				if (elementType.isLocal() || elementType.isAnonymous() || elementType.isCapture()) {
					return NO_NAME;
				}
				final int dimensions = getDimensions();
				char[] brackets = new char[dimensions * 2];
				for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
					brackets[i] = ']';
					brackets[i - 1] = '[';
				}
				buffer = new StringBuffer(elementType.getQualifiedName());
				buffer.append(brackets);
				return String.valueOf(buffer);

			case Binding.TYPE_PARAMETER :
				if (isCapture()) {
					return NO_NAME;
				}
				TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
				return new String(typeVariableBinding.sourceName);

			case Binding.PARAMETERIZED_TYPE :
				buffer = new StringBuffer();
				if (isMember()) {
					buffer
						.append(getDeclaringClass().getQualifiedName())
						.append('.');
					ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
					buffer.append(parameterizedTypeBinding.sourceName());
					ITypeBinding[] tArguments = getTypeArguments();
					final int typeArgumentsLength = tArguments.length;
					if (typeArgumentsLength != 0) {
						buffer.append('<');
						for (int i = 0; i < typeArgumentsLength; i++) {
							if (i > 0) {
								buffer.append(',');
							}
							buffer.append(tArguments[i].getQualifiedName());
						}
						buffer.append('>');
					}
					return String.valueOf(buffer);
				}
				buffer.append(getTypeDeclaration().getQualifiedName());
				ITypeBinding[] tArguments = getTypeArguments();
				final int typeArgumentsLength = tArguments.length;
				if (typeArgumentsLength != 0) {
					buffer.append('<');
					for (int i = 0; i < typeArgumentsLength; i++) {
						if (i > 0) {
							buffer.append(',');
						}
						buffer.append(tArguments[i].getQualifiedName());
					}
					buffer.append('>');
				}
				return String.valueOf(buffer);

			default :
				if (isAnonymous() || isLocal()) {
					return NO_NAME;
				}
				if (isPrimitive() || isNullType()) {
					BaseTypeBinding baseTypeBinding = (BaseTypeBinding) this.binding;
					return new String(baseTypeBinding.simpleName);
				}
				if (isMember()) {
					buffer = new StringBuffer();
					buffer
						.append(getDeclaringClass().getQualifiedName())
						.append('.');
					buffer.append(getName());
					return String.valueOf(buffer);
				}
				PackageBinding packageBinding = this.binding.getPackage();
				buffer = new StringBuffer();
				if (packageBinding != null && packageBinding.compoundName != CharOperation.NO_CHAR_CHAR) {
					buffer.append(CharOperation.concatWith(packageBinding.compoundName, '.')).append('.');
				}
				buffer.append(getName());
				return String.valueOf(buffer);
		}
	}

	/*
	 * @see ITypeBinding#getSuperclass()
	 */
	public ITypeBinding getSuperclass() {
		if (this.binding == null)
			return null;
		switch (this.binding.kind()) {
			case Binding.ARRAY_TYPE :
			case Binding.BASE_TYPE :
				return null;
			default:
				// no superclass for interface types (interface | annotation type)
				if (this.binding.isInterface())
					return null;
		}
		ReferenceBinding superclass = null;
		try {
			superclass = ((ReferenceBinding)this.binding).superclass();
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			org.eclipse.jdt.internal.core.util.Util.log(e, "Could not retrieve superclass"); //$NON-NLS-1$
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
		if (this.typeArguments != null) {
			return this.typeArguments;
		}
		if (this.binding.isParameterizedType()) {
			ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
			final org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] arguments = parameterizedTypeBinding.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				ITypeBinding[] newTypeArguments = new ITypeBinding[argumentsLength];
				for (int i = 0; i < argumentsLength; i++) {
					ITypeBinding typeBinding = this.resolver.getTypeBinding(arguments[i]);
					if (typeBinding == null) {
						return this.typeArguments = NO_TYPE_BINDINGS;
					}
					newTypeArguments[i] = typeBinding;
				}
				return this.typeArguments = newTypeArguments;
			}
		}
		return this.typeArguments = NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeBounds()
	 */
	public ITypeBinding[] getTypeBounds() {
		if (this.bounds != null) {
			return this.bounds;
		}
		if (this.binding instanceof TypeVariableBinding) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			ReferenceBinding varSuperclass = typeVariableBinding.superclass();
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding firstClassOrArrayBound = typeVariableBinding.firstBound;
			int boundsLength = 0;
			if (firstClassOrArrayBound != null) {
				if (firstClassOrArrayBound == varSuperclass) {
					boundsLength++;
				} else if (firstClassOrArrayBound.isArrayType()) { // capture of ? extends/super arrayType
					boundsLength++;
				} else {
					firstClassOrArrayBound = null;
				}
			}
			ReferenceBinding[] superinterfaces = typeVariableBinding.superInterfaces();
			int superinterfacesLength = 0;
			if (superinterfaces != null) {
				superinterfacesLength = superinterfaces.length;
				boundsLength += superinterfacesLength;
			}
			if (boundsLength != 0) {
				ITypeBinding[] typeBounds = new ITypeBinding[boundsLength];
				int boundsIndex = 0;
				if (firstClassOrArrayBound != null) {
					ITypeBinding typeBinding = this.resolver.getTypeBinding(firstClassOrArrayBound);
					if (typeBinding == null) {
						return this.bounds = NO_TYPE_BINDINGS;
					}
					typeBounds[boundsIndex++] = typeBinding;
				}
				if (superinterfaces != null) {
					for (int i = 0; i < superinterfacesLength; i++, boundsIndex++) {
						ITypeBinding typeBinding = this.resolver.getTypeBinding(superinterfaces[i]);
						if (typeBinding == null) {
							return this.bounds = NO_TYPE_BINDINGS;
						}
						typeBounds[boundsIndex] = typeBinding;
					}
				}
				return this.bounds = typeBounds;
			}
		}
		return this.bounds = NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeParameters()
	 */
	public ITypeBinding[] getTypeParameters() {
		if (this.typeParameters != null) {
			return this.typeParameters;
		}
		switch(this.binding.kind()) {
			case Binding.RAW_TYPE :
			case Binding.PARAMETERIZED_TYPE :
				return this.typeParameters = NO_TYPE_BINDINGS;
		}
		TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
		int typeVariableBindingsLength = typeVariableBindings == null ? 0 : typeVariableBindings.length;
		if (typeVariableBindingsLength != 0) {
			ITypeBinding[] newTypeParameters = new ITypeBinding[typeVariableBindingsLength];
			for (int i = 0; i < typeVariableBindingsLength; i++) {
				ITypeBinding typeBinding = this.resolver.getTypeBinding(typeVariableBindings[i]);
				if (typeBinding == null) {
					return this.typeParameters = NO_TYPE_BINDINGS;
				}
				newTypeParameters[i] = typeBinding;
			}
			return this.typeParameters = newTypeParameters;
		}
		return this.typeParameters = NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getWildcard()
	 * @since 3.1
	 */
	public ITypeBinding getWildcard() {
		if (this.binding instanceof CaptureBinding) {
			CaptureBinding captureBinding = (CaptureBinding) this.binding;
			return this.resolver.getTypeBinding(captureBinding.wildcard);
		}
		return null;
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
		try {
			if (this == type) return true;
			if (!(type instanceof TypeBinding)) return false;
			TypeBinding other = (TypeBinding) type;
			Scope scope = this.resolver.scope();
			if (scope == null) return false;
			return this.binding.isCompatibleWith(other.binding) || scope.isBoxingCompatibleWith(this.binding, other.binding);
		} catch (AbortCompilation e) {
			// don't surface internal exception to clients
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=143013
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see ITypeBinding#isCapture()
	 */
	public boolean isCapture() {
		return this.binding.isCapture();
	}

	/* (non-Javadoc)
	 * @see ITypeBinding#isCastCompatible(ITypeBinding)
	 */
	public boolean isCastCompatible(ITypeBinding type) {
		try {
			Expression expression = new Expression() {
				public StringBuffer printExpression(int indent,StringBuffer output) {
					return null;
				}
			};
			Scope scope = this.resolver.scope();
			if (scope == null) return false;
			if (!(type instanceof TypeBinding)) return false;
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding expressionType = ((TypeBinding) type).binding;
			// simulate capture in case checked binding did not properly get extracted from a reference
			expressionType = expressionType.capture(scope, 0);
			return expression.checkCastTypesCompatibility(scope, this.binding, expressionType, null);
		} catch (AbortCompilation e) {
			// don't surface internal exception to clients
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=143013
			return false;
		}
	}

	/*
	 * @see ITypeBinding#isClass()
	 */
	public boolean isClass() {
		return this.binding.isClass() && !this.binding.isTypeVariable() && !this.binding.isWildcard();
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
		} else if (isTypeVariable()) {
			final TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			final Binding declaringElement = typeVariableBinding.declaringElement;
			if (declaringElement instanceof MethodBinding) {
				MethodBinding methodBinding = (MethodBinding) declaringElement;
				return !methodBinding.declaringClass.isBinaryBinding();
			} else {
				final org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding = (org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaringElement;
				if (typeBinding instanceof ReferenceBinding) {
					return !((ReferenceBinding) typeBinding).isBinaryBinding();
				} else if (typeBinding instanceof ArrayBinding) {
					final ArrayBinding arrayBinding = (ArrayBinding) typeBinding;
					final org.eclipse.jdt.internal.compiler.lookup.TypeBinding leafComponentType = arrayBinding.leafComponentType;
					if (leafComponentType instanceof ReferenceBinding) {
						return !((ReferenceBinding) leafComponentType).isBinaryBinding();
					}
				}
			}

		} else if (isCapture()) {
			CaptureBinding captureBinding = (CaptureBinding) this.binding;
			return !captureBinding.sourceType.isBinaryBinding();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isInterface()
	 */
	public boolean isInterface() {
		return this.binding.isInterface() && !this.binding.isTypeVariable() && !this.binding.isWildcard();
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
		return this.binding == org.eclipse.jdt.internal.compiler.lookup.TypeBinding.NULL;
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
	 * @see IBinding#isRecovered()
	 */
	public boolean isRecovered() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ITypeBinding#isSubTypeCompatible(ITypeBinding)
	 */
	public boolean isSubTypeCompatible(ITypeBinding type) {
		try {
			if (this == type) return true;
			if (this.binding.isBaseType()) return false;
			if (!(type instanceof TypeBinding)) return false;
			TypeBinding other = (TypeBinding) type;
			if (other.binding.isBaseType()) return false;
			return this.binding.isCompatibleWith(other.binding);
		} catch (AbortCompilation e) {
			// don't surface internal exception to clients
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=143013
			return false;
		}
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
		return this.binding.isTypeVariable() && !this.binding.isCapture();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isUpperbound()
	 */
	public boolean isUpperbound() {
		return this.binding.isWildcard() && ((WildcardBinding) this.binding).boundKind == Wildcard.EXTENDS;
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
