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

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * This class define methods which are used for handling dom based completions for method declarations.
 */
final class DOMCompletionEngineMethodDeclHandler {
    private DOMCompletionEngineMethodDeclHandler() {
    }

    /**
     * Find parameter names for given method binding.
     */
    public static List<String> findVariableNames(IMethodBinding binding) {
        if (binding.getJavaElement() instanceof IMethod m) {
            try {
                List<String> res = List.of(m.getParameterNames());
//                if (m instanceof SourceMethod || binding.getDeclaringClass().isArray() || !IntStream.range(0, m.getParameterTypes().length).mapToObj(n -> "arg" + n).toList().equals(res)) {
                	// generated default names, ignore
                	return res;
//                }
            } catch (JavaModelException ex) {
                ILog.get().warn(ex.getMessage(), ex);
            }
        }
        if (binding.getDeclaringClass().isFromSource()) {
        	return List.of(binding.getParameterNames());
        }
        return List.of();
    }
}
