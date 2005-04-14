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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IMethod
 */

/* package */ class BinaryMethod extends BinaryMember implements IMethod {
	
	class DecodeParametersNames extends SourceElementRequestorAdapter {
			String[] parametersNames;
		
			public void enterMethod(MethodInfo methodInfo) {
					if (methodInfo.parameterNames != null) {
						int length = methodInfo.parameterNames.length;
						this.parametersNames = new String[length];
						for (int i = 0; i < length; i++) {
							this.parametersNames[i] = new String(methodInfo.parameterNames[i]);
						}
					}
				}
				
			public void enterConstructor(MethodInfo methodInfo) {
					if (methodInfo.parameterNames != null) {
						int length = methodInfo.parameterNames.length;
						this.parametersNames = new String[length];
						for (int i = 0; i < length; i++) {
							this.parametersNames[i] = new String(methodInfo.parameterNames[i]);
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
		char[] genericSignature = info.getGenericSignature();
		if (genericSignature != null) {
			char[] dotBasedSignature = CharOperation.replaceOnCopy(genericSignature, '/', '.');
			this.exceptionTypes = Signature.getThrownExceptionTypes(new String(dotBasedSignature));
		}
		if (this.exceptionTypes == null || this.exceptionTypes.length == 0) {
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
 * @see JavaElement#getHandleMemento(StringBuffer)
 */
protected void getHandleMemento(StringBuffer buff) {
	((JavaElement) getParent()).getHandleMemento(buff);
	char delimiter = getHandleMementoDelimiter();
	buff.append(delimiter);
	escapeMementoName(buff, getElementName());
	for (int i = 0; i < this.parameterTypes.length; i++) {
		buff.append(delimiter);
		escapeMementoName(buff, this.parameterTypes[i]);
	}
	if (this.occurrenceCount > 1) {
		buff.append(JEM_COUNT);
		buff.append(this.occurrenceCount);
	}
}
/*
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_METHOD;
}
public String getKey(boolean forceOpen) throws JavaModelException {
	return getKey(this, org.eclipse.jdt.internal.compiler.lookup.Binding.USE_ACCESS_FLAGS_IN_BINDING_KEY/*with access flags*/, forceOpen);
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

public ITypeParameter getTypeParameter(String typeParameterName) {
	return new TypeParameter(this, typeParameterName);
}

public ITypeParameter[] getTypeParameters() throws JavaModelException {
	String[] typeParameterSignatures = getTypeParameterSignatures();
	int length = typeParameterSignatures.length;
	if (length == 0) return TypeParameter.NO_TYPE_PARAMETERS;
	ITypeParameter[] typeParameters = new ITypeParameter[length];
	for (int i = 0; i < typeParameterSignatures.length; i++) {
		String typeParameterName = Signature.getTypeVariable(typeParameterSignatures[i]);
		typeParameters[i] = new TypeParameter(this, typeParameterName);
	}
	return typeParameters;
}

/**
 * @see IMethod#getTypeParameterSignatures()
 * @since 3.0
 * @deprecated
 */
public String[] getTypeParameterSignatures() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getElementInfo();
	char[] genericSignature = info.getGenericSignature();
	if (genericSignature == null) 
		return CharOperation.NO_STRINGS;
	char[] dotBasedSignature = CharOperation.replaceOnCopy(genericSignature, '/', '.');
	char[][] typeParams = Signature.getTypeParameters(dotBasedSignature);
	return CharOperation.toStrings(typeParams);
}

/*
 * @see IMethod
 */
public String getReturnType() throws JavaModelException {
	if (this.returnType == null) {
		IBinaryMethod info = (IBinaryMethod) getElementInfo();
		this.returnType = getReturnType(info);
	}
	return this.returnType;
}
private String getReturnType(IBinaryMethod info) {
	char[] genericSignature = info.getGenericSignature();
	char[] signature = genericSignature == null ? info.getMethodDescriptor() : genericSignature;
	char[] dotBasedSignature = CharOperation.replaceOnCopy(signature, '/', '.');
	String returnTypeName= Signature.getReturnType(new String(dotBasedSignature));
	return new String(ClassFile.translatedName(returnTypeName.toCharArray()));
}
/*
 * @see IMethod
 */
public String getSignature() throws JavaModelException {
	IBinaryMethod info = (IBinaryMethod) getElementInfo();
	return new String(info.getMethodDescriptor());
}
/**
 * @see org.eclipse.jdt.internal.core.JavaElement#hashCode()
 */
public int hashCode() {
   int hash = super.hashCode();
	for (int i = 0, length = parameterTypes.length; i < length; i++) {
	    hash = Util.combineHashCodes(hash, parameterTypes[i].hashCode());
	}
	return hash;
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
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.IMethod#isResolved()
 */
public boolean isResolved() {
	return false;
}
/*
 * @see IMethod#isSimilar(IMethod)
 */
public boolean isSimilar(IMethod method) {
	return 
		areSimilarMethods(
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
public JavaElement resolved(Binding binding) {
	SourceRefElement resolvedHandle = new ResolvedBinaryMethod(this.parent, this.name, this.parameterTypes, new String(binding.computeUniqueKey()));
	resolvedHandle.occurrenceCount = this.occurrenceCount;
	return resolvedHandle;
}/*
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(tabString(tab));
	if (info == null) {
		toStringName(buffer);
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		toStringName(buffer);
	} else {
		IBinaryMethod methodInfo = (IBinaryMethod) info;
		int flags = methodInfo.getModifiers();
		if (Flags.isStatic(flags)) {
			buffer.append("static "); //$NON-NLS-1$
		}
		if (!methodInfo.isConstructor()) {
			buffer.append(Signature.toString(getReturnType(methodInfo)));
			buffer.append(' ');
		}
		toStringName(buffer, flags);
	}
}
protected void toStringName(StringBuffer buffer) {
	toStringName(buffer, 0);
}
protected void toStringName(StringBuffer buffer, int flags) {
	buffer.append(getElementName());
	buffer.append('(');
	String[] parameters = getParameterTypes();
	int length;
	if (parameters != null && (length = parameters.length) > 0) {
		boolean isVarargs = Flags.isVarargs(flags);
		for (int i = 0; i < length; i++) {
			try {
				if (i < length - 1) {
					buffer.append(Signature.toString(parameters[i]));
					buffer.append(", "); //$NON-NLS-1$
				} else if (isVarargs) {
					// remove array from signature
					String parameter = parameters[i].substring(1);
					buffer.append(Signature.toString(parameter));
					buffer.append(" ..."); //$NON-NLS-1$
				} else {
					buffer.append(Signature.toString(parameters[i]));
				}
			} catch (IllegalArgumentException e) {
				// parameter signature is malformed
				buffer.append("*** invalid signature: "); //$NON-NLS-1$
				buffer.append(parameters[i]);
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
