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

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;

class DOMCompletionContext extends CompletionContext {
    private int offset;
    private char[] token;
    private IJavaElement enclosingElement;

    DOMCompletionContext(int offset) {
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public char[] getToken() {
        return this.token;
    }

    public void setToken(char[] t) {
        this.token = t;
    }

    @Override
    public IJavaElement getEnclosingElement() {
        return this.enclosingElement;
    }

    public void setEnclosingElement(IJavaElement enclosingElement) {
        this.enclosingElement = enclosingElement;
    }
}