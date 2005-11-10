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
package org.eclipse.jdt.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * @see IField
 */

/* package */ class BinaryField extends BinaryMember implements IField {

/*
 * Constructs a handle to the field with the given name in the specified type. 
 */
protected BinaryField(JavaElement parent, String name) {
	super(parent, name);
}
public boolean equals(Object o) {
	if (!(o instanceof BinaryField)) return false;
	return super.equals(o);
}
/*
 * @see IField
 */
public Object getConstant() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return convertConstant(info.getConstant());
}
/*
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return info.getModifiers();
}
/*
 * @see IJavaElement
 */
public int getElementType() {
	return FIELD;
}
/*
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_FIELD;
}
public String getKey(boolean forceOpen) throws JavaModelException {
	return getKey(this, forceOpen);
}
/*
 * @see IField
 */
public String getTypeSignature() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return new String(ClassFile.translatedName(info.getTypeName()));
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.IField#isEnumConstant()
 */public boolean isEnumConstant() throws JavaModelException {
	return Flags.isEnum(getFlags());
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.IField#isResolved()
 */
public boolean isResolved() {
	return false;
}
public JavaElement resolved(Binding binding) {
	SourceRefElement resolvedHandle = new ResolvedBinaryField(this.parent, this.name, new String(binding.computeUniqueKey()));
	resolvedHandle.occurrenceCount = this.occurrenceCount;
	return resolvedHandle;
}
/*
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		toStringName(buffer);
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		toStringName(buffer);
	} else {
		try {
			buffer.append(Signature.toString(this.getTypeSignature()));
			buffer.append(" "); //$NON-NLS-1$
			toStringName(buffer);
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
public String getAttachedJavadoc(IProgressMonitor monitor, String encoding) throws JavaModelException {
	URL baseLocation= getJavadocBaseLocation();
	if (baseLocation == null) {
		return null;
	}
	StringBuffer pathBuffer = new StringBuffer(baseLocation.toExternalForm());

	if (!(pathBuffer.charAt(pathBuffer.length() - 1) == '/')) {
		pathBuffer.append('/');
	}
	IType declaringType = this.getDeclaringType();
	IPackageFragment pack= declaringType.getPackageFragment();
	pathBuffer.append(pack.getElementName().replace('.', '/')).append('/').append(declaringType.getTypeQualifiedName('.')).append(JavadocConstants.HTML_EXTENSION);
	if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	String contents = getURLContents(String.valueOf(pathBuffer), encoding);
	if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	if (contents == null) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.CANNOT_RETRIEVE_ATTACHED_JAVADOC, this));
	int indexAnchor = contents.indexOf(
			JavadocConstants.ANCHOR_PREFIX_START + this.getElementName() + JavadocConstants.ANCHOR_PREFIX_END);
	if (indexAnchor == -1) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNRECOGNIZED_JAVADOC_FORMAT, this));
	int indexOfEndLink = contents.indexOf(JavadocConstants.ANCHOR_SUFFIX, indexAnchor);
	if (indexOfEndLink == -1) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNRECOGNIZED_JAVADOC_FORMAT, this));
	int indexOfNextField = contents.indexOf(JavadocConstants.ANCHOR_PREFIX_START, indexOfEndLink);
	int indexOfBottom = contents.indexOf(JavadocConstants.CONSTRUCTOR_DETAIL, indexOfEndLink);
	indexOfNextField= Math.min(indexOfNextField, indexOfBottom);
	if (indexOfNextField == -1) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNRECOGNIZED_JAVADOC_FORMAT, this));
	return contents.substring(indexOfEndLink + JavadocConstants.ANCHOR_SUFFIX_LENGTH, indexOfNextField);
}
}
