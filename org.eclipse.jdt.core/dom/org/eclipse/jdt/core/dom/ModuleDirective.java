/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

/**
 * Abstract base class of AST nodes that represent module directives (added in JLS9 API).
 *
 * <pre>
 * ModuleDirective:
 *    {@link RequiresDirective}
 *    {@link ExportsDirective}
 *    {@link OpensDirective}
 *    {@link UsesDirective}
 *    {@link ProvidesDirective}
 * </pre>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.13 BETA_JAVA9
 */
public abstract class ModuleDirective extends ASTNode {

	ModuleDirective(AST ast) {
		super(ast);
		unsupportedBelow9();
	}
}
