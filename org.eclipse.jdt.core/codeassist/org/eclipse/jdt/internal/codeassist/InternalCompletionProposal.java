/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.NameLookup;

/**
 * Internal completion proposal
 * @since 3.1
 */
public class InternalCompletionProposal {
	private static Object NO_ATTACHED_SOURCE = new Object();
	
	static final char[] ARG = "arg".toCharArray();  //$NON-NLS-1$
	static final char[] ARG0 = "arg0".toCharArray();  //$NON-NLS-1$
	static final char[] ARG1 = "arg1".toCharArray();  //$NON-NLS-1$
	static final char[] ARG2 = "arg2".toCharArray();  //$NON-NLS-1$
	static final char[] ARG3 = "arg3".toCharArray();  //$NON-NLS-1$
	static final char[][] ARGS1 = new char[][]{ARG0};
	static final char[][] ARGS2 = new char[][]{ARG0, ARG1};
	static final char[][] ARGS3 = new char[][]{ARG0, ARG1, ARG2};
	static final char[][] ARGS4 = new char[][]{ARG0, ARG1, ARG2, ARG3};
	
	protected CompletionEngine completionEngine;
	protected NameLookup nameLookup;
	
	protected char[] declarationPackageName;
	protected char[] declarationTypeName;
	protected char[] packageName;
	protected char[] typeName;
	protected char[][] parameterPackageNames;
	protected char[][] parameterTypeNames;
	
	protected int accessibility = IAccessRule.K_ACCESSIBLE;
	
	protected boolean isConstructor = false;
	
	protected char[][] findMethodParameterNames(char[] signatureType, char[] selector, char[][] paramTypeNames){
		if(signatureType == null) return null;
		
		char[] tName = Signature.toCharArray(signatureType);
		char[][] parameterNames = null;
		int length = paramTypeNames.length;
		
		Object cachedType = this.completionEngine.typeCache.get(tName);
		
		IType type = null;
		if(cachedType != null) {
			if(cachedType != NO_ATTACHED_SOURCE && cachedType instanceof BinaryType) {
				type = (BinaryType)cachedType;
			}
		} else { 
			// TODO (david) shouldn't it be NameLookup.ACCEPT_ALL ?
			type = this.nameLookup.findType(new String(tName), false, NameLookup.ACCEPT_CLASSES & NameLookup.ACCEPT_INTERFACES);
			if(type instanceof BinaryType){
				if(((BinaryType)type).getSourceMapper() != null) {
					this.completionEngine.typeCache.put(tName, type);
				} else {
					this.completionEngine.typeCache.put(tName, NO_ATTACHED_SOURCE);
					type = null;
				}
			} else {
				type = null;
			}
		}
		
		if(type != null) {
			String[] args = new String[length];
			for(int i = 0;	i< length ; i++){
				args[i] = new String(paramTypeNames[i]);
			}
			IMethod method = type.getMethod(new String(selector),args);
			try{
				parameterNames = new char[length][];
				String[] params = method.getParameterNames();
				for(int i = 0;	i< length ; i++){
					parameterNames[i] = params[i].toCharArray();
				}
			} catch(JavaModelException e){
				parameterNames = null;
			}
		}
		
//		 default parameters name
		if(parameterNames == null) {
			switch (length) {
				case 0 :
					parameterNames = new char[length][];
					break;
				case 1 :
					parameterNames = ARGS1;
					break;
				case 2 :
					parameterNames = ARGS2;
					break;
				case 3 :
					parameterNames = ARGS3;
					break;
				case 4 :
					parameterNames = ARGS4;
					break;
				default :
					parameterNames = new char[length][];
					for (int i = 0; i < length; i++) {
						parameterNames[i] = CharOperation.concat(ARG, String.valueOf(i).toCharArray());
					}
					break;
			}
			
		}
		
		return parameterNames;
	}
	
	protected char[] getDeclarationPackageName() {
		return this.declarationPackageName;
	}
	
	protected char[] getDeclarationTypeName() {
		return this.declarationTypeName;
	}
	
	protected char[] getPackageName() {
		return this.packageName;
	}
	
	protected char[] getTypeName() {
		return this.typeName;
	}
	
	protected char[][] getParameterPackageNames() {
		return this.parameterPackageNames;
	}
	
	
	protected char[][] getParameterTypeNames() {
		return this.parameterTypeNames;
	}
	
	protected char[][] findMethodParameterNames(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] paramPackageNames, char[][] paramTypeNames){
		if(paramTypeNames == null) return null;
		
		char[][] parameterNames = null;
		int length = paramTypeNames.length;
		
		char[] tName = CharOperation.concat(declaringTypePackageName,declaringTypeName,'.');
		Object cachedType = this.completionEngine.typeCache.get(tName);
		
		IType type = null;
		if(cachedType != null) {
			if(cachedType != NO_ATTACHED_SOURCE && cachedType instanceof BinaryType) {
				type = (BinaryType)cachedType;
			}
		} else { 
			// TODO (david) shouldn't it be NameLookup.ACCEPT_ALL ?
			type = this.nameLookup.findType(new String(tName), false, NameLookup.ACCEPT_CLASSES & NameLookup.ACCEPT_INTERFACES);
			if(type instanceof BinaryType){
				if(((BinaryType)type).getSourceMapper() != null) {
					this.completionEngine.typeCache.put(tName, type);
				} else {
					this.completionEngine.typeCache.put(tName, NO_ATTACHED_SOURCE);
					type = null;
				}
			} else {
				type = null;
			}
		}
		
		if(type != null) {
			String[] args = new String[length];
			for(int i = 0;	i< length ; i++){
				char[] parameterType = CharOperation.concat(paramPackageNames[i],paramTypeNames[i],'.');
				args[i] = Signature.createTypeSignature(parameterType,true);
			}
			IMethod method = type.getMethod(new String(selector),args);
			try{
				parameterNames = new char[length][];
				String[] params = method.getParameterNames();
				for(int i = 0;	i< length ; i++){
					parameterNames[i] = params[i].toCharArray();
				}
			} catch(JavaModelException e){
				parameterNames = null;
			}
		}
		// default parameters name
		if(parameterNames == null) {
			switch (length) {
				case 0 :
					parameterNames = new char[length][];
					break;
				case 1 :
					parameterNames = ARGS1;
					break;
				case 2 :
					parameterNames = ARGS2;
					break;
				case 3 :
					parameterNames = ARGS3;
					break;
				case 4 :
					parameterNames = ARGS4;
					break;
				default :
					parameterNames = new char[length][];
					for (int i = 0; i < length; i++) {
						parameterNames[i] = CharOperation.concat(ARG, String.valueOf(i).toCharArray());
					}
					break;
			}
			
		}
		return parameterNames;
	}
	
	protected void setDeclarationPackageName(char[] declarationPackageName) {
		this.declarationPackageName = declarationPackageName;
	}
	
	protected void setDeclarationTypeName(char[] declarationTypeName) {
		this.declarationTypeName = declarationTypeName;
	}
	
	protected void setPackageName(char[] packageName) {
		this.packageName = packageName;
	}
	
	protected void setTypeName(char[] typeName) {
		this.typeName = typeName;
	}
	
	protected void setParameterPackageNames(char[][] parameterPackageNames) {
		this.parameterPackageNames = parameterPackageNames;
	}
	
	protected void setParameterTypeNames(char[][] parameterTypeNames) {
		this.parameterTypeNames = parameterTypeNames;
	}
	
	protected void setAccessibility(int kind) {
		this.accessibility = kind;
	}
	
	protected void setIsContructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}
}
