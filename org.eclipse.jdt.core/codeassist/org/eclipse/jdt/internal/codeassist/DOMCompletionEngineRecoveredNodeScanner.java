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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringTemplateExpression;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.codeassist.DOMCompletionEngine.Bindings;

/**
 * This class define methods which helps to find most suitable bindings.
 */
final class DOMCompletionEngineRecoveredNodeScanner {
    // this class might need to consider the offset when scanning for suitable nodes since some times we get the full
    // statement where we might find multiple suitable node, so to narrow down the perfect we must check the offset.

    private ICompilationUnit cu;
    private int offset;

    public DOMCompletionEngineRecoveredNodeScanner(ICompilationUnit cu, int offset) {
        this.cu = cu;
        this.offset = offset;
    }

    // todo: we might need to improve not to traverse already traversed node paths.
    private class SuitableNodeVisitor extends ASTVisitor {
        private ITypeBinding foundBinding = null;
        private Bindings scope;
        private ICompilationUnit cu;
        private int offset;

        public SuitableNodeVisitor(Bindings scope, ICompilationUnit cu, int offset) {
            this.scope = scope;
            this.cu = cu;
            this.offset = offset;
        }

        public boolean foundNode() {
            return this.foundBinding != null;
        }

        @Override
        public boolean visit(MethodInvocation node) {
            this.foundBinding = node.resolveTypeBinding();
            if (this.foundBinding != null) {
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(FieldAccess node) {
            this.foundBinding = node.resolveTypeBinding();
            if (this.foundBinding != null) {
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(ExpressionStatement node) {
            this.foundBinding = node.getExpression().resolveTypeBinding();
            if (this.foundBinding != null) {
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(StringTemplateExpression node) {
            // statement such as 'System.out.println("hello" + Thread.currentThread().)' are identified as a
            // StringFragment part of StringTemplateExpression, the invocation which we are interested might be in the
            // the processor of the expression
            this.foundBinding = node.getProcessor().resolveTypeBinding();
            if (this.foundBinding != null) {
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleType node) {
            // this is part of a statement that is recovered due to syntax errors, so first check if the type is a
            // actual recoverable type, if not treat the type name as a variable name and search for such variable in
            // the context.
            var binding = node.resolveBinding();
            if (!binding.isRecovered()) {
                this.foundBinding = binding;
                return false;
            } else {
                var possibleVarName = binding.getName();
                var result = this.scope.stream().filter(IVariableBinding.class::isInstance)
                        .filter(b -> possibleVarName.equals(b.getName())).map(IVariableBinding.class::cast)
                        .map(v -> v.getType()).findFirst();
                if (result.isPresent()) {
                    this.foundBinding = result.get();
                    return false;
                }
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(QualifiedName node) {
            // this is part of a qualified expression such as "Thread.cu"
            this.foundBinding = node.getQualifier().resolveTypeBinding();
            if (this.foundBinding != null) {
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleName node) {
            // check if the node is just followed by a '.' before the offset.
            try {
                if (this.offset > 0) {
                    char charAt = this.cu.getSource().charAt(this.offset - 1);
                    if (charAt == '.' && (node.getStartPosition() + node.getLength()) == this.offset - 1) {
                        var name = node.getIdentifier();
                        // search for variables for bindings
                        var result = this.scope.stream().filter(IVariableBinding.class::isInstance)
                                .filter(b -> name.equals(b.getName())).map(IVariableBinding.class::cast)
                                .map(v -> v.getType()).findFirst();
                        if (result.isPresent()) {
                            this.foundBinding = result.get();
                            return false;
                        }
                    }
                }
            } catch (JavaModelException ex) {
                ILog.get().error(ex.getMessage(), ex);
            }
            this.foundBinding = null;
            return false;
        }

        public ITypeBinding foundTypeBinding() {
            return this.foundBinding;
        }
    }

    static Stream<IType> findTypes(String name, String qualifier, ICompilationUnit unit) {
        List<IType> types = new ArrayList<>();
        var searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[] { unit.getJavaProject() });
        TypeNameMatchRequestor typeRequestor = new TypeNameMatchRequestor() {
            @Override
            public void acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch match) {
                types.add(match.getType());
            }
        };
        try {
            new SearchEngine(unit.getOwner()).searchAllTypeNames(qualifier == null ? null : qualifier.toCharArray(),
                    SearchPattern.R_EXACT_MATCH, name.toCharArray(), SearchPattern.R_EXACT_MATCH,
                    IJavaSearchConstants.TYPE, searchScope, typeRequestor,
                    IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
        } catch (JavaModelException ex) {
            ILog.get().error(ex.getMessage(), ex);
        }
        return types.stream();
    }

    /**
     * Find the closest suitable node for completions from the recovered nodes at the given node.
     */
    public ITypeBinding findClosestSuitableBinding(ASTNode node, Bindings scope) {
        ASTNode parent = node;
        var visitor = new SuitableNodeVisitor(scope, this.cu, this.offset);
        while (parent != null && withInOffset(parent)) {
            parent.accept(visitor);
            if (visitor.foundNode()) {
                break;
            }
            parent = parent.getParent();
        }
        return visitor.foundTypeBinding();
    }

    private boolean withInOffset(ASTNode node) {
        return node.getStartPosition() <= this.offset;
    }
}
