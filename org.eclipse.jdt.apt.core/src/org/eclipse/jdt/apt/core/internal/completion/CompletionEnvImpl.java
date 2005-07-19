/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.completion;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.completion.EclipseCodeAssistEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.env.ITypeConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.sun.mirror.type.TypeMirror;

public class CompletionEnvImpl extends BaseProcessorEnv implements EclipseCodeAssistEnvironment
{
	CompletionEnvImpl(CompilationUnit astCompilationUnit,
				     IFile file,
				     IJavaProject javaProj)
    {
		super(astCompilationUnit, file, javaProj, Phase.OTHER);
    }
	
	public TypeMirror getType(String typeQualifiedName) {
		final int length = typeQualifiedName == null ? 0 : typeQualifiedName.length();
		if( length == 0 ) return null;
		final int dotIndex = typeQualifiedName.indexOf('.');
		if(dotIndex == -1){
			switch(length)
			{
			case 3: 
				if(ITypeConstants.INT.equals(typeQualifiedName) )
					return getIntType();
				break;
			case 4:
				if(ITypeConstants.CHAR.equals(typeQualifiedName) )
					return getCharType();
				else if(ITypeConstants.BYTE.equals(typeQualifiedName) )
					return getByteType();
				else if(ITypeConstants.LONG.equals(typeQualifiedName) )
					return getLongType();
				else if(ITypeConstants.VOID.equals(typeQualifiedName) )
					return getVoidType();
				break;
			case 5:
				if(ITypeConstants.DOUBLE.equals(typeQualifiedName) )
					return getDoubleType();
				else if(ITypeConstants.FLOAT.equals(typeQualifiedName) )
					return getFloatType();
				break;
			case 7:
				if(ITypeConstants.BOOLEAN.equals(typeQualifiedName) )
					return getBooleanType();
				break;
			}			
		}
		return getTypeUtils().getDeclaredType( getTypeDeclaration(typeQualifiedName) );
	}	
}
