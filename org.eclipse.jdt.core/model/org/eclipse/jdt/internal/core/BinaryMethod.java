package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.core.*;

/**
 * @see IMethod
 */

/* package */ class BinaryMethod extends BinaryMember implements IMethod {

	/**
	 * The parameter type signatures of the method - stored locally
	 * to perform equality test. <code>null</code> indicates no
	 * parameters.
	 */
	protected String[] fParameterTypes;
	/**
	 * The parameter names for the method.
	 */
	protected String[] fParameterNames;

	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
	protected String[] fExceptionTypes;
	protected String fReturnType;
protected BinaryMethod(IType parent, String name, String[] parameterTypes) {
	super(METHOD, parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
	if (parameterTypes == null) {
		fParameterTypes= fgEmptyList;
	} else {
		fParameterTypes= parameterTypes;
	}
}
public boolean equals(Object o) {
	return super.equals(o) && Util.equalArraysOrNull(fParameterTypes, ((BinaryMethod)o).fParameterTypes);
}
/**
 * @see IMethod
 */
public String[] getExceptionTypes() throws JavaModelException {
	if (fExceptionTypes == null) {
		IBinaryMethod info = (IBinaryMethod) getRawInfo();
		char[][] eTypeNames = info.getExceptionTypeNames();
		if (eTypeNames == null || eTypeNames.length == 0) {
			fExceptionTypes = fgEmptyList;
		} else {
			eTypeNames = ClassFile.translatedNames(eTypeNames);
			fExceptionTypes = new String[eTypeNames.length];
			for (int j = 0, length = eTypeNames.length; j < length; j++) {
				// 1G01HRY: ITPJCORE:WINNT - method.getExceptionType not in correct format
				int nameLength = eTypeNames[j].length;
				char[] convertedName = new char[nameLength + 2];
				System.arraycopy(eTypeNames[j], 0, convertedName, 1, nameLength);
				convertedName[0] = 'L';
				convertedName[nameLength + 1] = ';';
				fExceptionTypes[j] = new String(convertedName);
			}
		}
	}
	return fExceptionTypes;
}
/**
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	return info.getModifiers();
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento() {
	StringBuffer buff = new StringBuffer(((JavaElement) getParent()).getHandleMemento());
	buff.append(getHandleMementoDelimiter());
	buff.append(getElementName());
	for (int i = 0; i < fParameterTypes.length; i++) {
		buff.append(getHandleMementoDelimiter());
		buff.append(fParameterTypes[i]);
	}
	return buff.toString();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_METHOD;
}
/**
 * @see IMethod
 */
public int getNumberOfParameters() {
	return fParameterTypes == null ? 0 : fParameterTypes.length;
}
/**
 * @see IMethod
 */
public String[] getParameterNames() throws JavaModelException {
	if (fParameterNames == null) {
		IBinaryMethod info = (IBinaryMethod) getRawInfo();
		int paramCount = Signature.getParameterCount(new String(info.getMethodDescriptor()));
		fParameterNames = new String[paramCount];
		for (int i = 0; i < paramCount; i++) {
			fParameterNames[i] = "arg" + i; //$NON-NLS-1$
		}
	}
	return fParameterNames;
}
/**
 * @see IMethod
 */
public String[] getParameterTypes() {
	return fParameterTypes;
}
/**
 * @see IMethod
 */
public String getReturnType() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	if (fReturnType == null) {
		String returnType= Signature.getReturnType(new String(info.getMethodDescriptor()));
		fReturnType= new String(ClassFile.translatedName(returnType.toCharArray()));
	}
	return fReturnType;
}
/**
 * @see IMethod
 */
public String getSignature() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	return new String(info.getMethodDescriptor());
}
/**
 * @see IMethod
 */
public boolean isConstructor() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getRawInfo();
	return info.isConstructor();
}
/**
 */
public String readableName() {

	StringBuffer buffer = new StringBuffer(super.readableName());
	buffer.append("("); //$NON-NLS-1$
	String[] parameterTypes = this.getParameterTypes();
	int length;
	if (parameterTypes != null && (length = parameterTypes.length) > 0) {
		for (int i = 0; i < length; i++) {
			buffer.append(Signature.toString(parameterTypes[i]));
			if (i < length - 1) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
	}
	buffer.append(")"); //$NON-NLS-1$
	return buffer.toString();
}
}
