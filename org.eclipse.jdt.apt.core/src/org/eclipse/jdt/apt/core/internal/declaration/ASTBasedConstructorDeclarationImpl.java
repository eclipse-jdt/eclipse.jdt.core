/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import com.sun.mirror.declaration.ConstructorDeclaration;

import com.sun.mirror.util.DeclarationVisitor;

public class ASTBasedConstructorDeclarationImpl
	extends ASTBasedExecutableDeclarationImpl
	implements ConstructorDeclaration{

	public ASTBasedConstructorDeclarationImpl(
			final org.eclipse.jdt.core.dom.BodyDeclaration astNode,
			final IFile file,
			final BaseProcessorEnv env)
	{
		super(astNode, file, env);
	}

	@Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitConstructorDeclaration(this);
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.CONSTRUCTOR; }
}
