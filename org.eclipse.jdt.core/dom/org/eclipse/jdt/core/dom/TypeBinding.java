/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.jdt.internal.core.ClassFile;

/**
 * Internal implementation of type bindings.
 */
class TypeBinding implements ITypeBinding {

	private static final int VALID_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
		Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.STRICTFP;

	private static final String NO_NAME = ""; //$NON-NLS-1$	
	private static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	private static final IVariableBinding[] NO_VARIABLE_BINDINGS = new IVariableBinding[0];
	private static final IMethodBinding[] NO_METHOD_BINDINGS = new IMethodBinding[0];
	
	private org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;
	private BindingResolver resolver;
	private String key;
	
	public TypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
		this.binding = binding;
		this.resolver = resolver;
	}
	
	/*
	 * @see ITypeBinding#isPrimitive()
	 */
	public boolean isPrimitive() {
		return !isNullType() && binding.isBaseType();
	}

	/*
	 * @see ITypeBinding#isArray()
	 */
	public boolean isArray() {
		return binding.isArrayType();
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
	 * @see ITypeBinding#isClass()
	 */
	public boolean isClass() {
		return this.binding.isClass();
	}

	/*
	 * @see ITypeBinding#isInterface()
	 */
	public boolean isInterface() {
		return this.binding.isInterface();
	}

	/*
	 * @see ITypeBinding#isTypeVariable()
	 */
	public boolean isTypeVariable() {
		return this.binding.isTypeVariable();
	}

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isAnonymousType()) {
				return NO_NAME;
			} else {
				char[] shortName = referenceBinding.shortReadableName();
				if (referenceBinding.isMemberType() || referenceBinding.isLocalType()) {
					return new String(CharOperation.subarray(shortName, CharOperation.lastIndexOf('.', shortName) + 1, shortName.length));
				} else {
					return new String(shortName);
				}
			}
		} else if (this.binding.isArrayType()) {
			ArrayBinding arrayBinding = (ArrayBinding) this.binding;
			int dimensions = arrayBinding.dimensions;
			char[] brackets = new char[dimensions * 2];
			for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
				brackets[i] = ']';
				brackets[i - 1] = '[';
			}
			StringBuffer buffer = new StringBuffer();
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding leafComponentTypeBinding = arrayBinding.leafComponentType;
			if (leafComponentTypeBinding.isClass() || leafComponentTypeBinding.isInterface()) {
				ReferenceBinding referenceBinding2 = (ReferenceBinding) leafComponentTypeBinding;
				char[] shortName = referenceBinding2.shortReadableName();
				if (referenceBinding2.isMemberType() || referenceBinding2.isLocalType()) {
					buffer.append(CharOperation.subarray(shortName, CharOperation.lastIndexOf('.', shortName) + 1, shortName.length));
				} else {
					buffer.append(shortName);
				}
			} else {
				buffer.append(leafComponentTypeBinding.readableName());
			}
			buffer.append(brackets);
			return buffer.toString();
		} else {
			return new String(this.binding.readableName());
		}
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
	 * @see ITypeBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (this.binding.isArrayType() || this.binding.isBaseType()) {
			return null;
		}
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
		return null;
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

	/*
	 * @see ITypeBinding#getInterfaces()
	 */
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
	
	/*
	 * @see IBinding#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		if (this.binding == null || this.binding.isArrayType() || this.binding.isBaseType()) return null;
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		if (referenceBinding.isBinaryBinding()) {
			ClassFile classFile = (ClassFile) getClassFile(referenceBinding.getFileName());
			if (classFile == null) return null;
			return classFile.getType();
		}
		if (referenceBinding.isLocalType() || referenceBinding.isAnonymousType()) {
			// local or anonymous type
			ICompilationUnit cu = getCompilationUnit(referenceBinding.getFileName());
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
		} else {
			// member or top level type
			ITypeBinding declaringTypeBinding = getDeclaringClass();
			if (declaringTypeBinding == null) {
				// top level type
				ICompilationUnit cu = getCompilationUnit(referenceBinding.getFileName());
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
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		if (this.binding.isClass()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			if (referenceBinding.isAnonymousType()) {
				return accessFlags & ~Modifier.FINAL;
			}
			return accessFlags;
		} else if (this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
			// clear the AccAbstract and the AccInterface bits
			return accessFlags & ~(Modifier.ABSTRACT | 0x200);
		} else {
			return 0;
		}
	}

	/*
	 * @see ITypeBinding#getDeclaredModifiers()
	 */
	public int getDeclaredModifiers() {
		return getModifiers();
	}

	/*
	 * @see ITypeBinding#isTopLevel()
	 */
	public boolean isTopLevel() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isNested()
	 */
	public boolean isNested() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isMember()
	 */
	public boolean isMember() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isLocal()
	 */
	public boolean isLocal() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isLocalType() && !referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isAnonymous()
	 */
	public boolean isAnonymous() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isAnonymousType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#getDeclaredTypes()
	 */
	public ITypeBinding[] getDeclaredTypes() {
		try {
			if (this.binding.isClass() || this.binding.isInterface()) {
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
	
	public void appendParameterKey(StringBuffer buffer) {
		if (isTypeVariable()) {
			appendTypeVariableKey(buffer, false/*don't include declaring element*/);
		} else if (isArray() && getElementType().isTypeVariable()) {
			int dimensions = getDimensions();
			TypeBinding typeBinding = (TypeBinding) getElementType();
			typeBinding.appendTypeVariableKey(buffer, false/*don't include declaring element*/);
			for (int j = 0; j < dimensions; j++) {
				buffer.append('[').append(']');
			}
		} else {
			appendKey(buffer, true/*raw type only*/);
		}
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
	
	/*
	 * Returns the class file for the given file name, or null if not found.
	 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
	 */
	private IClassFile getClassFile(char[] fileName) {
		char[] slashSeparatedFileName = CharOperation.replaceOnCopy(fileName, File.separatorChar, '/');
		int lastSlash = CharOperation.lastIndexOf('/', slashSeparatedFileName);
		if (lastSlash == -1) return null;
		IPackageFragment pkg = getPackageFragment(slashSeparatedFileName, lastSlash);
		if (pkg == null) return null;
		char[] simpleName = CharOperation.subarray(slashSeparatedFileName, lastSlash+1, slashSeparatedFileName.length);
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

	/*
	 * @see ITypeBinding#getDeclaredFields()
	 */
	public IVariableBinding[] getDeclaredFields() {
		try {
			if (this.binding.isClass() || this.binding.isInterface()) {
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
			if (this.binding.isClass() || this.binding.isInterface()) {
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

	private boolean shouldBeRemoved(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		return methodBinding.isDefaultAbstract() || methodBinding.isSynthetic() || (methodBinding.isConstructor() && isInterface());
	}
	
	/*
	 * @see ITypeBinding#isFromSource()
	 */
	public boolean isFromSource() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isBinaryBinding();
		}
		return false;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.TYPE;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isDeprecated();
		}
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			StringBuffer buffer = new StringBuffer();
			appendKey(buffer);
			this.key = buffer.toString();
		}
		return this.key;
	}
	public void appendKey(StringBuffer buffer) {
		appendKey(buffer, false/*not only raw type*/);
	}
	public void appendKey(StringBuffer buffer, boolean rawTypeOnly) {
		if (this.key != null && !rawTypeOnly) {
			buffer.append(this.key);
			return;
		}
		if (isLocal()) {
			// declaring method or type
			SourceTypeBinding sourceBinding = (SourceTypeBinding) this.binding; // per construction, a local type can only be defined in source
			ClassScope scope = sourceBinding.scope;
			ClassScope classScope = scope.enclosingClassScope();
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration referenceContext = classScope.referenceContext;
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding internalBinding = referenceContext.binding;
			ITypeBinding typeBinding = this.resolver.getTypeBinding(internalBinding);
			if (typeBinding != null) {
				((TypeBinding) typeBinding).appendKey(buffer);
			}
			buffer.append('$');
			CompilationUnitScope compilationUnitScope = scope.compilationUnitScope();
			CompilationUnitDeclaration compilationUnitDeclaration = compilationUnitScope.referenceContext;
			LocalTypeBinding[] localTypeBindings = compilationUnitDeclaration.localTypes;
			for (int i = 0, max = compilationUnitDeclaration.localTypeCount; i < max; i++) {
				if (localTypeBindings[i] == sourceBinding) {
					buffer.append(i+1);
					if (!isAnonymous()) {
						buffer.append('$');
						buffer.append(sourceBinding.sourceName);
					}
					break;
				}
			}
		} else {
			if (this.binding.isTypeVariable()) {
				appendTypeVariableKey(buffer, true/*include declaring element*/);
			} else if (this.binding.isWildcard()) {
				WildcardBinding wildcardBinding = (WildcardBinding) binding;
				org.eclipse.jdt.internal.compiler.lookup.TypeBinding bound = wildcardBinding.bound;
				if (bound != null)
					((TypeBinding) this.resolver.getTypeBinding(bound)).appendKey(buffer);
				else
					buffer.append(wildcardBinding.genericTypeSignature());
			} else if (this.isClass()
					|| this.isInterface()
					|| this.isEnum()
					|| this.isAnnotation()) {
				char[] qualifiedSourceName = this.binding.qualifiedSourceName();
				if (qualifiedSourceName != null) {
					CharOperation.replace(qualifiedSourceName, '.', '$');
					buffer
						.append(getPackage().getName())
						.append('/')
						.append(qualifiedSourceName);
				} else {
					buffer
						.append(getPackage().getName())
						.append('/')
						.append(getName());
				}
				if (!rawTypeOnly) {
					// only one of the type parameters or type arguments is non-empty at the same time
					appendTypeParameters(buffer, getTypeParameters());
					appendTypeArguments(buffer, getTypeArguments());
				}
			} else if (this.binding.isArrayType()) {
				if (getElementType() != null) {
					((TypeBinding) getElementType()).appendKey(buffer);
					int dimensions = getDimensions();
					for (int j = 0; j < dimensions; j++) {
						buffer.append('[').append(']');
					}
				} else {
					int dimensions = this.getDimensions();
					for (int j = 0; j < dimensions; j++) {
						buffer.append('[').append(']');
					}
				}
			} else {
				// this is a primitive type
				buffer.append(getName());
			}
		}
	}

	public void appendTypeArguments(StringBuffer buffer, ITypeBinding[] typeArgs) {
		int typeArgsLength = typeArgs.length;
		if (typeArgsLength != 0) {
			buffer.append('<');
			for (int i = 0; i < typeArgsLength; i++) {
				TypeBinding typeArg = (TypeBinding) typeArgs[i];
				typeArg.appendParameterKey(buffer);
				buffer.append(',');
			}
			buffer.append('>');
		}
	}

	public void appendTypeParameters(StringBuffer buffer, ITypeBinding[] typeParameters) {
		int typeParametersLength = typeParameters.length;
		if (typeParametersLength != 0) {
			buffer.append('<');
			for (int i = 0; i < typeParametersLength; i++) {
				TypeBinding typeParameter = (TypeBinding) typeParameters[i];
				typeParameter.appendParameterKey(buffer);
				ITypeBinding[] bounds = typeParameter.getTypeBounds();
				for (int j = 0, length = bounds.length; j < length; j++) {
					TypeBinding bound = (TypeBinding) bounds[j];
					buffer.append(':');
					bound.appendParameterKey(buffer);
				}
				buffer.append(',');
			}
			buffer.append('>');
		}
	}

	/*
	 * Appends the key for this type variable binding to the given buffer.
	 * Include the declaring element if specified
	 */
	public void appendTypeVariableKey(StringBuffer buffer, boolean includeDeclaringElement) {
		TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
		buffer.append(typeVariableBinding.sourceName);
		if (includeDeclaringElement) {
			Binding declaringElement = typeVariableBinding.declaringElement;
			buffer.append(':');
			if (declaringElement instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
				buffer.append(this.resolver.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaringElement).getKey());
			} else if (declaringElement instanceof org.eclipse.jdt.internal.compiler.lookup.MethodBinding) {
				buffer.append(getNonRecursiveKey(this.resolver.getMethodBinding((org.eclipse.jdt.internal.compiler.lookup.MethodBinding) declaringElement)));						
			}
		}
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
	
	/**
	 * @see ITypeBinding#isNullType()
	 */
	public boolean isNullType() {
		return this.binding == BaseTypes.NullBinding;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getQualifiedName()
	 */
	public String getQualifiedName() {
		if (isAnonymous() || isLocal()) {
			return NO_NAME;
		}
		
		if (isPrimitive() || isNullType()) {
			return getName();
		}
		
		if (isArray()) {
			ITypeBinding elementType = getElementType();
			String elementTypeQualifiedName = elementType.getQualifiedName();
			if (elementTypeQualifiedName.length() != 0) {
				int dimensions = this.getDimensions();
				char[] brackets = new char[dimensions * 2];
				for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
					brackets[i] = ']';
					brackets[i - 1] = '[';
				}
				StringBuffer stringBuffer = new StringBuffer(elementTypeQualifiedName);
				stringBuffer.append(brackets);
				return stringBuffer.toString();
			} else {
				return NO_NAME;
			}
		}
		
		if (this.isTypeVariable()) {
			return new String(this.binding.sourceName());			
		}
		
		if (isTopLevel() || isMember()) {
			PackageBinding packageBinding = this.binding.getPackage();
			
			if (packageBinding == null || packageBinding.compoundName == CharOperation.NO_CHAR_CHAR) {
				return new String(this.binding.qualifiedSourceName());
			} else {
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer
					.append(this.binding.qualifiedPackageName())
					.append('.')
					.append(this.binding.qualifiedSourceName());
				return stringBuffer.toString();
			}
		}
		return NO_NAME;
	}
	
	/* (non-Javadoc)
	 * @see ITypeBinding#isEnum()
	 */
	public boolean isEnum() {
		return this.binding.isEnum();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isAnnotation()
	 */
	public boolean isAnnotation() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return false;
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
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isParameterizedType()
	 */
	public boolean isParameterizedType() {
		return this.binding.isParameterizedType();
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
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getErasure()
	 */
	public ITypeBinding getErasure() {
		return this.resolver.getTypeBinding(this.binding.erasure());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isRawType()
	 */
	public boolean isRawType() {
		return this.binding.isRawType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isWildcardType()
	 */
	public boolean isWildcardType() {
		return this.binding.isWildcard();
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isUpperbound()
	 */
	public boolean isUpperbound() {
		return this.binding.isWildcard() && ((WildcardBinding) this.binding).kind == Wildcard.SUPER;
	}
	
	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
	
	private String getNonRecursiveKey(IMethodBinding methodBinding) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(methodBinding.getDeclaringClass().getQualifiedName().replace('.', '/'));
		buffer.append('/');
		ITypeBinding _returnType = methodBinding.getReturnType();
		if (_returnType != null) {
			buffer.append(_returnType.getQualifiedName().replace('.', '/'));
			if (_returnType.isArray()) {
				int dimensions = _returnType.getDimensions();
				for (int i = 0; i < dimensions; i++) {
					buffer.append('[').append(']');
				}
			}
		}
		if (!methodBinding.isConstructor()) {
			buffer.append(methodBinding.getName());
		}
		ITypeBinding[] parameters = methodBinding.getParameterTypes();
		buffer.append('(');
		for (int i = 0, max = parameters.length; i < max; i++) {
			final ITypeBinding parameter = parameters[i];
			if (parameter != null) {
				buffer.append(parameter.getQualifiedName().replace('.', '/'));
			 	if (parameter.isArray()) {
					int dimensions = parameter.getDimensions();
					for (int j = 0; j < dimensions; j++) {
						buffer.append('[').append(']');
					}
			 	}
			}
		}
		buffer.append(')');
		ITypeBinding[] thrownExceptions = methodBinding.getExceptionTypes();
		for (int i = 0, max = thrownExceptions.length; i < max; i++) {
			final ITypeBinding thrownException = thrownExceptions[i];
			if (thrownException != null) {
				buffer.append(thrownException.getQualifiedName().replace('.', '/'));					
				if (thrownException.isArray()) {
					int dimensions = thrownException.getDimensions();
					for (int j = 0; j < dimensions; j++) {
						buffer.append('[').append(']');
					}
				}
			}
		}
		return String.valueOf(buffer);
	}
}
