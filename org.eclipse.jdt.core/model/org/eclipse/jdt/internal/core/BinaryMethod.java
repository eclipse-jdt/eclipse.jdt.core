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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IMethod
 */

/* package */ class BinaryMethod extends BinaryMember implements IMethod {
	
	class DecodeParametersNames extends SourceElementRequestorAdapter {
			String[] parametersNames;
		
			public void enterMethod(
				int declarationStart,
				int modifiers,
				char[] returnTypeName,
				char[] selector,
				int nameSourceStart,
				int nameSourceEnd,
				char[][] paramTypes,
				char[][] paramNames,
				char[][] exceptions) {
					if (paramNames != null) {
						int length = paramNames.length;
						this.parametersNames = new String[length];
						for (int i = 0; i < length; i++) {
							this.parametersNames[i] = new String(paramNames[i]);
						}
					}
				}
				
			public void enterConstructor(
				int declarationStart,
				int modifiers,
				char[] selector,
				int nameSourceStart,
				int nameSourceEnd,
				char[][] paramTypes,
				char[][] paramNames,
				char[][] exceptions) {
					if (paramNames != null) {
						int length = paramNames.length;
						this.parametersNames = new String[length];
						for (int i = 0; i < length; i++) {
							this.parametersNames[i] = new String(paramNames[i]);
						}
					}
				}
				
				public String[] getParametersNames() {
					return this.parametersNames;
				}
	}

	/**
	 * The parameter type signatures of the method - stored locally
	 * to perform equality test. <code>null</code> indicates no
	 * parameters.
	 */
	protected String[] parameterTypes;
	/**
	 * The parameter names for the method.
	 */
	protected String[] parameterNames;

	/**
	 * An empty list of Strings
	 */
	protected static final String[] NO_TYPES= new String[] {};
	protected String[] exceptionTypes;
	protected String returnType;
protected BinaryMethod(JavaElement parent, String name, String[] paramTypes) {
	super(parent, name);
	Assert.isTrue(name.indexOf('.') == -1);
	if (paramTypes == null) {
		this.parameterTypes= NO_TYPES;
	} else {
		this.parameterTypes= paramTypes;
	}
}
public boolean equals(Object o) {
	if (!(o instanceof BinaryMethod)) return false;
	return super.equals(o) && Util.equalArraysOrNull(this.parameterTypes, ((BinaryMethod)o).parameterTypes);
}
/*
 * @see IMethod
 */
public String[] getExceptionTypes() throws JavaModelException {
	if (this.exceptionTypes == null) {
		IBinaryMethod info = (IBinaryMethod) getElementInfo();
		char[][] eTypeNames = info.getExceptionTypeNames();
		if (eTypeNames == null || eTypeNames.length == 0) {
			this.exceptionTypes = NO_TYPES;
		} else {
			eTypeNames = ClassFile.translatedNames(eTypeNames);
			this.exceptionTypes = new String[eTypeNames.length];
			for (int j = 0, length = eTypeNames.length; j < length; j++) {
				// 1G01HRY: ITPJCORE:WINNT - method.getExceptionType not in correct format
				int nameLength = eTypeNames[j].length;
				char[] convertedName = new char[nameLength + 2];
				System.arraycopy(eTypeNames[j], 0, convertedName, 1, nameLength);
				convertedName[0] = 'L';
				convertedName[nameLength + 1] = ';';
				this.exceptionTypes[j] = new String(convertedName);
			}
		}
	}
	return this.exceptionTypes;
}
/*
 * @see IJavaElement
 */
public int getElementType() {
	return METHOD;
}
/*
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getElementInfo();
	return info.getModifiers();
}
/*
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento() {
	StringBuffer buff = new StringBuffer(((JavaElement) getParent()).getHandleMemento());
	char delimiter = getHandleMementoDelimiter();
	buff.append(delimiter);
	escapeMementoName(buff, getElementName());
	for (int i = 0; i < this.parameterTypes.length; i++) {
		buff.append(delimiter);
		buff.append(this.parameterTypes[i]);
	}
	if (this.occurrenceCount > 1) {
		buff.append(JEM_COUNT);
		buff.append(this.occurrenceCount);
	}
	return buff.toString();
}
/*
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_METHOD;
}
/*
 * @see IMethod
 */
public int getNumberOfParameters() {
	return this.parameterTypes == null ? 0 : this.parameterTypes.length;
}
/*
 * @see IMethod
 * Look for source attachment information to retrieve the actual parameter names as stated in source.
 */
public String[] getParameterNames() throws JavaModelException {
	if (this.parameterNames == null) {

		// force source mapping if not already done
		IType type = (IType) getParent();
		SourceMapper mapper = getSourceMapper();
		if (mapper != null) {
			char[][] paramNames = mapper.getMethodParameterNames(this);
			
			// map source and try to find parameter names
			if(paramNames == null) {
				char[] source = mapper.findSource(type);
				if (source != null){
					mapper.mapSource(type, source);
				}
				paramNames = mapper.getMethodParameterNames(this);
			}
			
			// if parameter names exist, convert parameter names to String array
			if(paramNames != null) {
				this.parameterNames = new String[paramNames.length];
				for (int i = 0; i < paramNames.length; i++) {
					this.parameterNames[i] = new String(paramNames[i]);
				}
			}
		}
		// if still no parameter names, produce fake ones
		if (this.parameterNames == null) {
			IBinaryMethod info = (IBinaryMethod) getElementInfo();
			int paramCount = Signature.getParameterCount(new String(info.getMethodDescriptor()));
			this.parameterNames = new String[paramCount];
			for (int i = 0; i < paramCount; i++) {
				this.parameterNames[i] = "arg" + i; //$NON-NLS-1$
			}
		}
	}
	return this.parameterNames;
}
/*
 * @see IMethod
 */
public String[] getParameterTypes() {
	return this.parameterTypes;
}

/**
 * @see IMethod#getTypeParameterSignatures()
 * @since 3.0
 */
public String[] getTypeParameterSignatures() throws JavaModelException {
	// TODO (jerome) - missing implementation
	return new String[0];
}

/*
 * @see IMethod
 */
public String getReturnType() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getElementInfo();
	if (this.returnType == null) {
		String returnTypeName= Signature.getReturnType(new String(info.getMethodDescriptor()));
		this.returnType= new String(ClassFile.translatedName(returnTypeName.toCharArray()));
	}
	return this.returnType;
}
/*
 * @see IMethod
 */
public String getSignature() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getElementInfo();
	return new String(info.getMethodDescriptor());
}
/*
 * @see IMethod
 */
public boolean isConstructor() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getElementInfo();
	return info.isConstructor();
}
/*
 * @see IMethod#isMainMethod()
 */
public boolean isMainMethod() throws JavaModelException {
	return this.isMainMethod(this);
}

/*
 * @see IMethod#isSimilar(IMethod)
 */
public boolean isSimilar(IMethod method) {
	return 
		this.areSimilarMethods(
			this.getElementName(), this.getParameterTypes(),
			method.getElementName(), method.getParameterTypes(),
			null);
}

public String readableName() {

	StringBuffer buffer = new StringBuffer(super.readableName());
	buffer.append("("); //$NON-NLS-1$
	String[] paramTypes = this.parameterTypes;
	int length;
	if (paramTypes != null && (length = paramTypes.length) > 0) {
		for (int i = 0; i < length; i++) {
			buffer.append(Signature.toString(paramTypes[i]));
			if (i < length - 1) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
	}
	buffer.append(")"); //$NON-NLS-1$
	return buffer.toString();
}
/*
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		toStringName(buffer);
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		toStringName(buffer);
	} else {
		try {
			if (Flags.isStatic(this.getFlags())) {
				buffer.append("static "); //$NON-NLS-1$
			}
			if (!this.isConstructor()) {
				buffer.append(Signature.toString(this.getReturnType()));
				buffer.append(' ');
			}
			toStringName(buffer);
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
protected void toStringName(StringBuffer buffer) {
	buffer.append(getElementName());
	buffer.append('(');
	String[] parameters = this.getParameterTypes();
	int length;
	if (parameters != null && (length = parameters.length) > 0) {
		for (int i = 0; i < length; i++) {
			buffer.append(Signature.toString(parameters[i]));
			if (i < length - 1) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
	}
	buffer.append(')');
	if (this.occurrenceCount > 1) {
		buffer.append("#"); //$NON-NLS-1$
		buffer.append(this.occurrenceCount);
	}
}
}
