/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.List;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.codeassist.DOMCompletionEngine.Bindings;

/**
 * This class define methods which are used for handling dom based completions for variable declarations.
 */
public final class DOMCompletionEngineVariableDeclHandler {

    /**
     * Find variable names for given variable binding.
     */
    public List<String> findVariableNames(IVariableBinding binding, String token, Bindings scope) {
        // todo: add more variable names suggestions and also consider the visible variables to avoid conflicting names.
        var typeName = binding.getType().getName();
        if (token != null && !token.isEmpty() && !typeName.startsWith(token)) {
            typeName = token.concat(typeName);
        } else {
            typeName = typeName.length() > 1 ? typeName.substring(0, 1).toLowerCase().concat(typeName.substring(1))
                    : typeName;
        }
        return List.of(typeName);
    }
}
