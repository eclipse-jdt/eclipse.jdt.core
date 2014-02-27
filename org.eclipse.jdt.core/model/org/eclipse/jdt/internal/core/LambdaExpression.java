/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class LambdaExpression extends SourceType {

	org.eclipse.jdt.internal.compiler.ast.LambdaExpression lambdaExpression;
	SourceMethod lambdaMethod;
	
	public LambdaExpression(JavaElement parent, org.eclipse.jdt.internal.compiler.ast.LambdaExpression lambdaExpression) {
		super(parent, new String("<lambda>")); //$NON-NLS-1$
		this.lambdaExpression = lambdaExpression;
		this.occurrenceCount = lambdaExpression.ordinal;
	}
	
	@Override
	public String[] getCategories() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this)); // TODO: what the heck is this supposed to be ? 
	}

	@Override
	public int getFlags() throws JavaModelException {
		return this.lambdaExpression.binding.modifiers; // TODO
	}

	@Override
	public ISourceRange getJavadocRange() throws JavaModelException {
		return null;
	}

	@Override
	public boolean isBinary() {
		return false;
	}

	@Override
	public Object getElementInfo() throws JavaModelException {
		return new LambdaTypeElementInfo(this);
	}
	
	@Override
	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return new LambdaTypeElementInfo(this);
	}
	
	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public int getElementType() {
		return TYPE;
	}

	@Override
	public String getHandleIdentifier() {
		return null; // TODO
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public boolean isStructureKnown() throws JavaModelException {
		return true;
	}

	@Override
	public String getSource() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public ISourceRange getSourceRange() throws JavaModelException {
		return new SourceRange(this.lambdaExpression.sourceStart, this.lambdaExpression.sourceEnd - this.lambdaExpression.sourceStart + 1);
	}

	@Override
	public ISourceRange getNameRange() throws JavaModelException {
		return new SourceRange(this.lambdaExpression.sourceStart, this.lambdaExpression.arrowPosition() - this.lambdaExpression.sourceStart + 1);
	}

	@Override
	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace,
			IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace,
			IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public void rename(String newName, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IJavaElement[] getChildren() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public boolean hasChildren() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IAnnotation getAnnotation(String annotationName) {
		return null;
	}

	@Override
	public IAnnotation[] getAnnotations() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
			throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor)
			throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
			throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
			throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IMethod[] findMethods(IMethod method) {
		return null;
	}

	@Override
	public IJavaElement[] getChildrenForCategory(String category) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public String getElementName() {
		return new String("<lambda>"); //$NON-NLS-1$
	}

	@Override
	public IField getField(String fieldName) {
		return null;
	}

	@Override
	public IField[] getFields() throws JavaModelException {
		return new IField[0];
	}

	@Override
	public IInitializer getInitializer(int okkurrenceCount) {
		return null;
	}

	@Override
	public IInitializer[] getInitializers() throws JavaModelException {
		return new IInitializer[0];
	}

	@Override
	public IMethod getMethod(String selector, String[] parameterTypeSignatures) {
		return new SourceMethod(this, selector, parameterTypeSignatures);
	}
	
	public SourceMethod getMethod() {
		if (this.lambdaMethod != null)
			return this.lambdaMethod;
		
		TypeBinding [] argv = this.lambdaExpression.argumentsTypeElided() ? this.lambdaExpression.descriptor.parameters : this.lambdaExpression.argumentTypes(); 
		int argc = argv.length;
		String[] parameterTypeSignatures = new String[argc];
		for (int i = 0; i < argc; i++) {
			parameterTypeSignatures[i] = new String(argv[i].signature());
		}
		return this.lambdaMethod = new SourceMethod(this, new String(this.lambdaExpression.binding.selector), parameterTypeSignatures); 
	}

	@Override
	public IMethod[] getMethods() throws JavaModelException {
		return new IMethod[] { getMethod() };
	}

	@Override
	public String getSuperclassName() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public String getSuperclassTypeSignature() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public String[] getSuperInterfaceTypeSignatures() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public String[] getSuperInterfaceNames() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public String[] getTypeParameterSignatures() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public IType getType(String typeName) {
		return null;
	}

	@Override
	public ITypeParameter getTypeParameter(String typeParameterName) {
		return null;
	}

	@Override
	public String getTypeQualifiedName() {
		return null;
	}

	@Override
	public IType[] getTypes() throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public boolean isAnonymous() {
		return true;
	}

	@Override
	public boolean isClass() throws JavaModelException {
		return true;
	}

	@Override
	public boolean isEnum() throws JavaModelException {
		return false;
	}

	@Override
	public boolean isInterface() throws JavaModelException {
		return false;
	}

	@Override
	public boolean isAnnotation() throws JavaModelException {
		return false;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public boolean isMember()  {
		return false;
	}

	@Override
	public JavaElement resolved(Binding binding) {
		return this;
	}
	
	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public String[][] resolveType(String typeName) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

	@Override
	public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
	}

}
