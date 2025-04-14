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
import java.util.stream.IntStream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.core.BinaryMethod;

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
        		var res = List.of(m.getParameterNames());
	        	if (!res.isEmpty() && m instanceof BinaryMethod binary && binary.getElementInfo() instanceof MethodInfo info
	        		&& IntStream.range(0, res.size()).mapToObj(n -> "arg" + n).toList().equals(res)
	        		&& (info.getArgumentNames() == null || info.getArgumentNames().length == 0)) {
	        		return null;
	        	}
	        	return res;
            } catch (JavaModelException ex) {
                ILog.get().warn(ex.getMessage(), ex);
            }
        }
        if (binding.getDeclaringClass().isFromSource() || binding.getDeclaringClass().isArray()) {
        	return List.of(binding.getParameterNames());
        }
        return null;
    }
}
