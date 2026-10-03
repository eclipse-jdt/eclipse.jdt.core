/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ASTNode.IsReachable;
import static org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration.FIELD;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * An abstraction to track instance field reads in early construction contexts and reroute them via proxy locals.
 * JVM does not like getfield on an uninitialized this, even if the field has been putfield earlier.
 *
 * We can't do this in the normal course of resolution (without being ultra-conservative and assign a proxy for EVERY
 * instance field, irrespective of whether in the early construction context, it is ONLY written to (proxy not needed)
 * or is also read from (need proxy) or neither (proxy not needed)) since we may encounter
 * field writes ahead of reads.
 *
 * We are here at the start of the constructor AFTER the constructor's arguments are resolved and injected into the scope,
 * but BEFORE anything else is. As a result, we may conclude here that a certain reference binds to a field while in reality
 * it binds to a local. This just creates unnecessary proxies - a case of suboptimality, not one of correctness.
 *
 * For value classes we also get here for handling instance fields - these are positioned at the start of the prologue as opposed
 * to at the start of the epilogue as is the case for identity classes.
 */
public final class ConstructionContext {

	private final ConstructorDeclaration constructorDeclaration;
	private final TypeDeclaration typeDeclaration;
	private final MethodScope constructionScope;
	private final SourceTypeBinding sourceType;
	private final ExplicitConstructorCall constructorCall;

	public Map<FieldBinding, LarvalProxyBinding> proxies;

    ConstructionContext(ConstructorDeclaration constructorDeclaration) {
    	this.constructionScope = constructorDeclaration.scope;
        this.constructorDeclaration = constructorDeclaration;
        this.typeDeclaration = this.constructionScope.referenceType();
        this.sourceType = this.typeDeclaration.binding;
        this.constructorCall = constructorDeclaration.constructorCall;
    }

    ConstructionContext(TypeDeclaration typeDeclaration) {
    	this.constructionScope = typeDeclaration.initializerScope;
        this.constructorDeclaration = null;
        this.typeDeclaration = typeDeclaration;
        this.sourceType = typeDeclaration.binding;
        this.constructorCall = null;
    }

    private boolean isNop() { // Early construction context field reads are illegal in the scenarios below, so we don't have to bend over backwards.

    	/* We are ENTIRELY dealing with a code generation concern here, so don't sweat if this client will not proceed to code generation. */
    	CompilationUnitDeclaration unit = this.constructionScope.referenceCompilationUnit();
    	if ((unit.bits & ASTNode.SkipFieldProxification) != 0)
    		return true;

    	if (this.typeDeclaration != null && (this.typeDeclaration.bits & ASTNode.SkipFieldProxification) != 0) // not needed ?
    		return true;

    	if (this.constructorDeclaration != null) {
    		if ((this.constructorDeclaration.bits & ASTNode.SkipFieldProxification) != 0) // not needed ?
    			return true;
    		if (!this.constructorDeclaration.invokesSuper())
    			return true;
    		if (this.constructorDeclaration.isCompactConstructor()) // can't touch this for read/write
    			return true;
    		if (this.constructorCall.isImplicitSuper() && this.constructorCall == this.constructorDeclaration.statements[0])
    			return true;
    	}

    	return !JavaFeature.STRICTLY_INITIALIZED_FIELDS.isSupported(this.constructionScope.compilerOptions());
    }

    public void enterFieldsResolution() {
    	this.constructionScope.enterEarlyConstructionContext();
    	if (isNop())
    		return;
    	Set<FieldBinding> readFields = new PrologueFieldReadReferencesCollector().collect(this.typeDeclaration);
		if (readFields != null)
			readFields.forEach(this::synthesizeLarvalProxy);
		this.constructionScope.setProxies(this.proxies);
    }

    public void leaveFieldsResolution() {
    	this.constructionScope.leaveEarlyConstructionContext();
    }

    public void enterFieldAnalysis(FlowInfo flowInfo) {
    	this.constructionScope.enterEarlyConstructionContext();
    	if (this.proxies != null)
    		this.proxies.values().forEach(proxy -> proxy.id = this.constructionScope.outerMostMethodScope().analysisIndex++);
    }

    public void leaveFieldAnalysis(FlowInfo flowInfo, FlowContext flowContext) {
    	this.constructionScope.leaveEarlyConstructionContext();
    }

    public void enterPrologueResolution() {
    	this.constructionScope.enterEarlyConstructionContext();
    	if (isNop())
    		return;

    	this.proxies = new LinkedHashMap<>();
    	if (this.typeDeclaration.initializerScope.proxies != null) {
    	    this.typeDeclaration.initializerScope.proxies.forEach((field, proxy) -> {
    	    	field.tagBits |= TagBits.NeedsProxyLocal;
    	    	this.proxies.put(field, proxy);
    	    });
    	}

		Set<FieldBinding> readFields = new PrologueFieldReadReferencesCollector().collect(this.constructorDeclaration);
		if (readFields != null) {
		    readFields.stream()
		            .filter(f -> !this.proxies.containsKey(f))
		            .forEach(this::synthesizeLarvalProxy);
		}

		this.constructionScope.setProxies(this.proxies);
    }

    public void leavePrologueResolution() {
    	this.constructionScope.leaveEarlyConstructionContext();
    	this.constructorDeclaration.computePrologueLocalsSize();
    }

    public void enterPrologueAnalysis(FlowInfo flowInfo) {
    	this.constructionScope.enterEarlyConstructionContext();
    	if (isNop())
    		return;
    	/* For identity classes, a proxy never represents a field with initialization. Consequently a blank final should not be marked DA
    	   till we see an assignment in byte code. For non-final fields we start off with the VM's default value of a DA 0.
    	   For value classes, a proxy will be created even for fields with initialization. The proxies should start out DA if the field is DA
    	*/
    	if (this.proxies != null) {
    	    this.proxies.forEach((field, proxy) -> {
    	    	proxy.id = this.constructionScope.outerMostMethodScope().analysisIndex++;
    	    	if (!field.isFinal() || this.sourceType.isValueClass() && flowInfo.isDefinitelyAssigned(field))
    	    		flowInfo.markAsDefinitelyAssigned(proxy);
    	    });
    	}
    }

    public FlowInfo leavePrologueAnalysis(FlowInfo flowInfo, FlowContext flowContext) {
    	this.constructionScope.leaveEarlyConstructionContext();
    	if (this.proxies != null) {
    		this.proxies.forEach((field, proxy) -> {
    			if (flowInfo.isDefinitelyAssigned(proxy)) // else premature proxy, VCAOT.testFalseProxy()
    				flowInfo.markAsDefinitelyAssigned(field);
    		});
    	}

    	if (!this.constructorDeclaration.invokesSuper())
    		return flowInfo;

        if ((this.constructorDeclaration.bits & ASTNode.ShouldInitializeStrictly) == 0)
        	return flowInfo;


        for (FieldBinding field : this.sourceType.fields()) {  // no selective strict initialization as of JDK28, just check all instance fields
            if (field.isStatic() || !field.isFinal() || flowInfo.isDefinitelyAssigned(field))
                continue;
            if (field.isRecordComponent() && this.constructorDeclaration.isCompactConstructor())
                continue;
            if (field.isBlankFinal())
            	this.constructionScope.problemReporter().uninitializedStrictInitField(field, this.constructorCall);
        }

        FlowInfo currentFlowInfo = flowInfo;
        if (this.typeDeclaration.isValueClass()) {
			FieldDeclaration [] fields = this.typeDeclaration.fields != null ? this.typeDeclaration.fields : ASTNode.NO_FIELD_DECLARATIONS;
			for (FieldDeclaration field : fields) {
				if (field instanceof Initializer initializer) {
					if ((initializer.bits & ASTNode.HasBeenAnalysed) != 0) {
						/* Value class initializer blocks feature in a well defined environment. All instance fields are already DA or
						   failing that already complained about. These blocks cannot assign to any of them. Their locals and constructor
						   locals are disjoint. So neither can influence the other. The only thing to pass on is the fall through status.
						   This allows us to skip repeated analysis in every constructor context.
						*/
						if (!initializer.fallsThrough)
							currentFlowInfo.setReachMode(FlowInfo.UNREACHABLE_OR_DEAD);
						continue;
					}
					initializer.bits |= ASTNode.HasBeenAnalysed;
					this.typeDeclaration.initializerContext.handledExceptions = Binding.ANY_EXCEPTION; // tolerate them all, and record them
					currentFlowInfo = initializer.analyseCode(this.typeDeclaration.initializerScope, this.typeDeclaration.initializerContext, currentFlowInfo);
					// In case the initializer throws, use a reinitialized flowInfo and enter a fake reachable
					// state, since the previous initializer already got the blame.
					if (currentFlowInfo == FlowInfo.DEAD_END) {
						this.typeDeclaration.initializerScope.problemReporter().initializerMustCompleteNormally(initializer);
						currentFlowInfo = FlowInfo.initial(this.typeDeclaration.maxFieldCount).setReachMode(FlowInfo.UNREACHABLE_OR_DEAD);
					}
					if (currentFlowInfo.reachMode() == FlowInfo.UNREACHABLE_OR_DEAD)
						initializer.fallsThrough = false;
				}
			}
    	}
        return currentFlowInfo;
    }

    public void enterPrologueGeneration(CodeStream codeStream) {
    	this.constructionScope.enterEarlyConstructionContext();
    	if (isNop())
    		return;

		if (this.proxies != null)
			this.proxies.values().forEach(codeStream::addProxy);
		// For value classes, field initializations are generated *before* any prologue in the body of the constructor
		if (this.typeDeclaration.isValueClass()) {
			FieldDeclaration [] fields = this.typeDeclaration.fields != null ? this.typeDeclaration.fields : ASTNode.NO_FIELD_DECLARATIONS;
			for (FieldDeclaration field : fields) {
				if (field.isStatic() || field instanceof Initializer)
					continue;
				if (field.initialization == null || !field.binding.hasProxyLocal()) {
					field.generateCode(this.typeDeclaration.initializerScope, codeStream);
				} else {
					field.initialization.generateCode(this.typeDeclaration.initializerScope, codeStream, true);
					codeStream.store((LocalVariableBinding) this.constructionScope.getProxy(field.binding), false);
				}
			}
		}
    }

    public void leavePrologueGeneration(CodeStream codeStream, MethodBinding link, TypeReference [] typeArguments) {
    	this.constructionScope.leaveEarlyConstructionContext();
		if (this.constructorDeclaration.invokesSuper())
			flushProxies(codeStream);
		if (this.constructorDeclaration.isCompactConstructor() && this.typeDeclaration.isValueClass())
			flushRecordComponents(codeStream);
		codeStream.invoke(Opcodes.OPC_invokespecial, link, null /* default declaringClass */, typeArguments);
 		enterEpilogueGeneration(codeStream);
    }

    public void enterEpilogueGeneration(CodeStream codeStream) {
    	if ((this.constructorCall != null && (this.constructorCall.bits & IsReachable) == 0) || !this.constructorDeclaration.invokesSuper())
    		return;

		// For value classes, initializer blocks are generated right after super call. For identity classes, all instance field initializations.
		FieldDeclaration [] fields = this.typeDeclaration.fields != null ? this.typeDeclaration.fields : ASTNode.NO_FIELD_DECLARATIONS;
		for (FieldDeclaration field : fields) {
			if (field.isStatic())
				continue;
			if (field instanceof Initializer)
				field.generateCode(this.typeDeclaration.initializerScope, codeStream);
			else if (!this.typeDeclaration.isValueClass())
				field.generateCode(this.typeDeclaration.initializerScope, codeStream);
		}
    }

    public void leaveEpilogueGeneration(CodeStream codeStream, boolean needReturn) {

    	this.constructionScope.leaveEarlyConstructionContext();

		if (!needReturn)
			return;

		if (this.constructorCall != null && (this.constructorCall.bits & ASTNode.IsReachable) == 0)
			return;

		if (this.constructorDeclaration.isCompactConstructor() && !this.typeDeclaration.isValueClass())
			flushRecordComponents(codeStream);
		codeStream.return_();
    }

	private void flushRecordComponents(CodeStream codeStream) { // it would be neat to model these as proxies for record fields.
		for (RecordComponent rc : this.typeDeclaration.recordComponents) {
			LocalVariableBinding parameter = this.constructionScope.findVariable(rc.name);
			FieldBinding field = this.sourceType.getField(rc.name, true).original();
			codeStream.aload_0();
			codeStream.load(parameter);
			codeStream.fieldAccess(Opcodes.OPC_putfield, field, this.sourceType);
		}
	}

	private void flushProxies(CodeStream codeStream) {
		if (this.proxies != null) {
			this.proxies.forEach((field, proxy) -> {
		        codeStream.aload_0();
		        codeStream.load(proxy);
		        codeStream.fieldAccess(Opcodes.OPC_putfield, field, this.sourceType);
		        codeStream.removeVariable(proxy);
			});
		}
	}

    private LocalVariableBinding synthesizeLarvalProxy(FieldBinding field) {
        if (this.proxies == null)
            this.proxies = new LinkedHashMap<>();
        LarvalProxyBinding proxy = new LarvalProxyBinding(field);
        field.tagBits |= TagBits.NeedsProxyLocal;
        this.proxies.put(field, proxy);
        return proxy;
    }

    private final class PrologueFieldReadReferencesCollector extends ASTVisitor {

    	private final Set<FieldBinding> readFields = new LinkedHashSet<>();
    	private final Set<FieldBinding> thusFarDeclaredFields = new LinkedHashSet<>();
        private ReferenceContext referenceContext;

        Set<FieldBinding> collect(TypeDeclaration type) {
            this.referenceContext = type;
        	if (type.fields != null) {
        		Arrays.stream(type.fields)
        				.filter(f -> !f.isStatic() && f.getKind() == FIELD)
        				.forEach(f -> f.traverse(this, type.initializerScope));
        	}
            return this.readFields;
        }

        Set<FieldBinding> collect(ConstructorDeclaration constructor) {
            this.referenceContext = constructor;
            Statement[] statements = constructor.statements;
            if (statements != null) {
                for (Statement statement : statements) {
                    statement.traverse(this, constructor.scope);
                    if (statement instanceof ExplicitConstructorCall) {
                        break;
                    }
                }
            }
            return this.readFields;
        }

        @Override
        public boolean visit(FieldDeclaration field, MethodScope scope) {
        	this.thusFarDeclaredFields.add(field.binding);
        	if (field.initialization != null)
        		field.initialization.traverse(this, scope);
            return false;
        }

        @Override
        public boolean visit(TypeDeclaration ____, BlockScope scope) { // do not descend into
            return false;
        }

        @Override
        public boolean visit(LambdaExpression ____, BlockScope scope) { // do not descend into
            return false;
        }

        private boolean trackInstanceFieldGets(Binding b) {
			if (b instanceof FieldBinding field
                    && !field.isStatic()
                    && TypeBinding.equalsEquals(field.declaringClass, ConstructionContext.this.sourceType)
                    && (field.declaringClass.isValueClass() || field.sourceField() == null /* records */ || field.sourceField().initialization == null)) {
				if (this.referenceContext instanceof ConstructorDeclaration || this.thusFarDeclaredFields.contains(field)) // don't put up a proxy for illegal forward reference.
					this.readFields.add(field);
            }
			return false;
		}

        @Override
        public boolean visit(Assignment assignment, BlockScope scope) {
            Expression lhs = assignment.lhs;
            if (!(lhs instanceof SingleNameReference)) {
                lhs.traverse(this, scope);
            }
            if (assignment.expression != null) {
                assignment.expression.traverse(this, scope);
            }
            return false;
        }

        public boolean visit(ReferenceOfFieldOfThis referenceOfFieldOfThis, BlockScope scope) {
            Binding b = ConstructionContext.this.sourceType.getField(referenceOfFieldOfThis.token, false); // getBinding ignores mask and may return local :-(
            return trackInstanceFieldGets(b);
        }

        @Override
        public boolean visit(SingleNameReference snr, BlockScope scope) {
        	if (scope == null)
        		scope = ConstructionContext.this.constructionScope;
        	if (snr instanceof ReferenceOfFieldOfThis referenceOfFieldOfThis)
        		return visit(referenceOfFieldOfThis, scope);
            Binding b = scope.getBinding(snr.token, Binding.VARIABLE | Binding.TYPE, snr, false);
            return trackInstanceFieldGets(b);
        }

        @Override
        public boolean visit(QualifiedNameReference qnr, BlockScope scope) {
        	if (scope == null)
        		scope = ConstructionContext.this.constructionScope;
            Binding b = scope.getBinding(qnr.tokens[0], Binding.VARIABLE | Binding.TYPE, qnr, false);
            return trackInstanceFieldGets(b);
        }
    }
}