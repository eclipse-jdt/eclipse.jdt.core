package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LarvalProxyBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public final class PrologueResolutionContext {
    private final ConstructorDeclaration constructorDeclaration;
    private final BlockScope scope;
    public Map<FieldBinding, LocalVariableBinding> proxies;

    PrologueResolutionContext(ConstructorDeclaration constructorDeclaration) {
        this.constructorDeclaration = constructorDeclaration;
        this.scope = constructorDeclaration.scope;
    }

    void enter() {
    	this.scope.enterEarlyConstructionContext();
        Set<FieldBinding> fieldReferences = new PrologueFieldReferencesCollector().collect(this.constructorDeclaration);
        if (fieldReferences != null) {
            fieldReferences.forEach(this::synthesizeLarvalProxy);
        }
        this.scope.enclosingSourceType().setProxies(this.proxies);
    }

    Optional<Map<FieldBinding, LocalVariableBinding>> larvalProxies() {
    	return Optional.ofNullable(this.proxies);
    }

    public void leave() {
    	this.scope.leaveEarlyConstructionContext();
        this.scope.enclosingSourceType().setProxies(null);
        if (this.proxies != null) {
            this.proxies.keySet().forEach(field -> field.tagBits &= ~TagBits.IsShadowedByProxy);
        }
    }

    LocalVariableBinding synthesizeLarvalProxy(FieldBinding field) {
        if (this.proxies == null) {
            this.proxies = new HashMap<>();
        }
        LocalVariableBinding localVariable = new LarvalProxyBinding(field);
        localVariable.setConstant(Constant.NotAConstant);
        localVariable.useFlag = LocalVariableBinding.USED;
        this.scope.addLocalVariable(localVariable);
        this.proxies.put(field, localVariable);
        field.tagBits |= TagBits.IsShadowedByProxy;
        return localVariable;
    }

    private final class PrologueFieldReferencesCollector extends ASTVisitor {
        private final Set<FieldBinding> fieldReferences = new HashSet<>();
        private SourceTypeBinding sourceType;

        Set<FieldBinding> collect(ConstructorDeclaration constructor) {
            this.sourceType = constructor.scope.enclosingSourceType();
            Statement[] statements = constructor.statements;
            if (statements != null) {
                for (Statement statement : statements) {
                    statement.traverse(this, constructor.scope);
                    if (statement instanceof ExplicitConstructorCall) {
                        break;
                    }
                }
            }
            return this.fieldReferences;
        }

        @Override
        public boolean visit(TypeDeclaration typeDeclaration, BlockScope ___) {
            return false;
        }

        @Override
        public boolean visit(LambdaExpression lambda, BlockScope ____) {
            return false;
        }

        @Override
        public boolean visit(Assignment assignment, BlockScope ____) {
            Expression lhs = assignment.lhs;
            if (!(lhs instanceof SingleNameReference)) {
                lhs.traverse(this, PrologueResolutionContext.this.scope);
            }
            if (assignment.expression != null) {
                assignment.expression.traverse(this, PrologueResolutionContext.this.scope);
            }
            return false;
        }

        @Override
        public boolean visit(SingleNameReference singleNameReference, BlockScope ____) {
            Binding b = PrologueResolutionContext.this.scope.getBinding(singleNameReference.token, Binding.VARIABLE | Binding.TYPE,
                    singleNameReference, false);
            if (b instanceof FieldBinding field
                    && !field.isStatic()
                    && TypeBinding.equalsEquals(field.declaringClass, this.sourceType)
                    && field.sourceField().initialization == null) {
                this.fieldReferences.add(field);
            }
            return false;
        }

        @Override
        public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope ____) {
            Binding b = PrologueResolutionContext.this.scope.getBinding(qualifiedNameReference.tokens[0], Binding.VARIABLE | Binding.TYPE,
                    qualifiedNameReference, false);
            if (b instanceof FieldBinding field
                    && !field.isStatic()
                    && TypeBinding.equalsEquals(field.declaringClass, this.sourceType)
                    && field.sourceField().initialization == null) {
                this.fieldReferences.add(field);
            }
            return false;
        }
    }
}