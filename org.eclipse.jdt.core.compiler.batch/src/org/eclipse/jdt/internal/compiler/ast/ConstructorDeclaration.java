/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *     							bug 343713 - [compiler] bogus line number in constructor of inner class in 1.5 compliance
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
 *								bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
 *								bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								bug 400421 - [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 415399 - [1.8][compiler] Type annotations on constructor results dropped by the code generator
 *     Ulrich Grave <ulrich.grave@gmx.de> - Contributions for
 *                              bug 386692 - Missing "unused" warning on "autowired" fields
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.codegen.StackMapFrameCodeStream;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ConstructorDeclaration extends AbstractMethodDeclaration {

	public TypeParameter[] typeParameters;

	public AbstractVariableDeclaration [] protoArguments; // for compact constructors; we don't have a back pointer to declaring class.

public ConstructorDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}

private void complainOnUnusedPrivateConstructor() {
	MethodBinding constructorBinding = this.binding;
	if (constructorBinding == null || constructorBinding.isUsed())
		return;
	if ((this.bits & ASTNode.IsDefaultConstructor) != 0)
		return;
	if (constructorBinding.isPrivate()) {
		if ((this.binding.declaringClass.tagBits & TagBits.HasNonPrivateConstructor) == 0)
			return; // tolerate as known pattern to block instantiation
	} else if (!constructorBinding.isOrEnclosedByPrivateType()) {
		return;
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=270446, When the AST built is an abridged version
	// we don't have all tree nodes we would otherwise expect. (see ASTParser.setFocalPosition)
	ExplicitConstructorCall constructorCall = getConstructorCall();
	if (constructorCall == null)
		return;
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=264991, Don't complain about this
	// constructor being unused if the base class doesn't have a no-arg constructor.
	// See that a seemingly unused constructor that chains to another constructor with a
	// this(...) can be flagged as being unused without hesitation.
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142
	if (constructorCall.accessMode != ExplicitConstructorCall.This) {
		ReferenceBinding superClass = constructorBinding.declaringClass.superclass();
		if (superClass == null)
			return;
		// see if there is a no-arg super constructor
		MethodBinding methodBinding = superClass.getExactConstructor(Binding.NO_PARAMETERS);
		if (methodBinding == null)
			return;
		if (!methodBinding.canBeSeenBy(SuperReference.implicitSuperConstructorCall(), this.scope))
			return;
		ReferenceBinding declaringClass = constructorBinding.declaringClass;
		if (constructorBinding.isPublic() && constructorBinding.parameters.length == 0 && declaringClass.isStatic()
				&& declaringClass.findSuperTypeOriginatingFrom(TypeIds.T_JavaIoExternalizable, false) != null)
			return;
		// otherwise default super constructor exists, so go ahead and complain unused.
	}
	this.scope.problemReporter().unusedPrivateConstructor(this);
}
private void complainOnUnusedTypeVariables() {
	if (this.typeParameters != null  && !this.scope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
		for (TypeParameter typeParameter : this.typeParameters) {
			if ((typeParameter.binding.modifiers & ExtraCompilerModifiers.AccLocallyUsed) == 0) {
				this.scope.problemReporter().unusedTypeParameter(typeParameter);
			}
		}
	}
}

private static IErrorHandlingPolicy uniqueErrorHandlingPolicy = DefaultErrorHandlingPolicies.filterDuplicateProblems();

private FlowInfo analyzeFieldInitializations(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo prologueInfo) {

	TypeDeclaration typeDeclaration = classScope.referenceContext;
	ReferenceBinding declaringClass = typeDeclaration.binding;
	typeDeclaration.initializerScope.analysisPass++;

	boolean useOwningAnnotations = false, isCloseable = false;
	FieldDeclaration fieldNeedingClose = null;
	if (typeDeclaration.initializerScope.inPrimaryAnalysis()) {
		useOwningAnnotations = this.scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;
		isCloseable = declaringClass.hasTypeBit(TypeIds.BitAutoCloseable|TypeIds.BitCloseable);
	}

	FlowInfo flowInfo = prologueInfo.nullInfoLessUnconditionalCopy();

	// field initialization analysis should not see constructor locals.
	LocalVariableBinding[] locals = this.scope.locals;
	if (locals != null) {
		int numLocals = this.scope.localIndex;
		for (int i = 0; i < numLocals; i++)
			flowInfo.resetAssignmentInfo(locals[i]);
	}

	IErrorHandlingPolicy oldPolicy = typeDeclaration.initializerScope.problemReporter().switchErrorHandlingPolicy(uniqueErrorHandlingPolicy);
	try {
		FieldDeclaration [] fields = typeDeclaration.fields;
		if (fields != null) {
			for (FieldDeclaration field : fields) {
				if (field.isStatic())
					continue;

				if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0) {
					field.bits &= ~ASTNode.IsReachable; // unreachable in this universe.
				} else {
					field.bits &= ~ASTNode.IsUnreachableInAllUniverses; // not unreachable at least in this universe.
					field.bits |= ASTNode.IsReachable;
				}

				initializerFlowContext.handledExceptions = Binding.ANY_EXCEPTION; // tolerate them all, and record them
				flowInfo = field.analyseCode(typeDeclaration.initializerScope, initializerFlowContext, flowInfo);

				// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
				// branch, since the previous initializer already got the blame.
				if (flowInfo == FlowInfo.DEAD_END) {
					typeDeclaration.initializerScope.problemReporter().initializerMustCompleteNormally(field);
					flowInfo = FlowInfo.initial(typeDeclaration.maxFieldCount).setReachMode(FlowInfo.UNREACHABLE_OR_DEAD);
				}

				if (useOwningAnnotations && isCloseable && fieldNeedingClose == null) {
					if (!(field instanceof Initializer) && (field.binding.tagBits & TagBits.AnnotationOwning) != 0)
						fieldNeedingClose = field;
				}
			}

			if (fieldNeedingClose != null) {
				AbstractMethodDeclaration [] methods = typeDeclaration.methods;
				for (int i = 0, length = methods != null ? methods.length : 0; i < length; i++) {
					AbstractMethodDeclaration method = methods[i];
					if (method instanceof Clinit || (method instanceof ConstructorDeclaration))
						continue;
					if (CharOperation.equals(TypeConstants.CLOSE, method.selector) && method.arguments == null)
						fieldNeedingClose = null;
				}
				if (fieldNeedingClose != null)
					this.scope.problemReporter().missingImplementationOfClose(fieldNeedingClose);
			}
		}
	} finally {
		typeDeclaration.initializerScope.problemReporter().switchErrorHandlingPolicy(oldPolicy);
	}
	FlowInfo epilogueEntryInfo = prologueInfo.addInitializationsFrom(flowInfo);
	epilogueEntryInfo.setReachMode(flowInfo.reachMode());
	return epilogueEntryInfo;
}

public void analyseCode(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo flowInfo, int initialReachMode) {

	if (this.ignoreFurtherInvestigation)
		return;

	try {
		int nonStaticFieldInfoReachMode = flowInfo.reachMode();
		ExceptionHandlingFlowContext constructorContext;
		constructorContext =
			new ExceptionHandlingFlowContext(
				initializerFlowContext.parent,
				this,
				this.binding.thrownExceptions,
				initializerFlowContext,
				this.scope,
				FlowInfo.DEAD_END);

		// nullity, owning and mark as assigned
		analyseArguments(this.scope, flowInfo, initializerFlowContext, arguments(true), this.binding);
		ExplicitConstructorCall constructorCall = null;
		if (this.statements != null) {
			CompilerOptions compilerOptions = this.scope.compilerOptions();
			boolean enableSyntacticNullAnalysisForFields = compilerOptions.enableSyntacticNullAnalysisForFields;
			int complaintLevel = (nonStaticFieldInfoReachMode & FlowInfo.UNREACHABLE) == 0 ? Statement.NOT_COMPLAINED : Statement.COMPLAINED_FAKE_REACHABLE;
			this.scope.enterEarlyConstructionContext();
			flowInfo.setReachMode(initialReachMode);
			for (Statement stat : this.statements) {
				if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
					flowInfo = stat.analyseCode(this.scope, constructorContext, flowInfo);
				}
				if (enableSyntacticNullAnalysisForFields) {
					constructorContext.expireNullCheckedFieldInfo();
				}
				if (compilerOptions.analyseResourceLeaks) {
					FakedTrackingVariable.cleanUpUnassigned(this.scope, stat, flowInfo, false);
				}
				if (stat instanceof ExplicitConstructorCall) {
					constructorCall = (ExplicitConstructorCall) stat;
					if (constructorCall.accessMode == ExplicitConstructorCall.This)
						markFieldsAsInitializedAfterThisCall(constructorCall, flowInfo);
					else {
						flowInfo = analyzeFieldInitializations(classScope, initializerFlowContext, flowInfo);
						if ((flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0)
							complaintLevel = Statement.COMPLAINED_FAKE_REACHABLE;
					}
				}
			}
		}

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) { // don't fall through the constructor!
			this.bits |= ASTNode.NeedFreeReturn;
		}

		if (this.isCompactConstructor()) {
			for (FieldBinding field : this.binding.declaringClass.fields()) {
				if (!field.isStatic()) {
					flowInfo.markAsDefinitelyAssigned(field);
				}
			}
		}

		// check missing blank final field initializations (plus @NonNull)
		if (constructorCall != null && constructorCall.accessMode != ExplicitConstructorCall.This) {
			flowInfo = flowInfo.mergedWith(constructorContext.initsOnReturn);
			doFieldReachAnalysis(flowInfo, this.binding.declaringClass.fields());
		}

		initializerFlowContext.checkInitializerExceptions(
				this.scope,
				constructorContext,
				flowInfo);

		// anonymous constructor can gain extra thrown exceptions from unhandled ones
		if (this.binding.declaringClass.isAnonymousType()) {
			List computedExceptions = constructorContext.extendedExceptions;
			if (computedExceptions != null) {
				int size;
				if ((size = computedExceptions.size()) > 0) {
					ReferenceBinding[] actuallyThrownExceptions;
					computedExceptions.toArray(actuallyThrownExceptions = new ReferenceBinding[size]);
					this.binding.thrownExceptions = actuallyThrownExceptions;
				}
			}
		}

		// Complain about unused { constructors, type variables, parameters, catch blocks } etc
		complainOnUnusedPrivateConstructor();
		if (isRecursive(null /*lazy initialized visited list*/)) { // check constructor recursion, now that all constructors got resolved
			this.scope.problemReporter().recursiveConstructorInvocation(constructorCall);
		}
		complainOnUnusedTypeVariables();
		constructorContext.complainIfUnusedExceptionHandlers(this);
		this.scope.checkUnusedParameters(this.binding);
		this.scope.checkUnclosedCloseables(flowInfo, null, null/*don't report against a specific location*/, null);
	} catch (AbortMethod e) {
		this.ignoreFurtherInvestigation = true;
	}
}

private void markFieldsAsInitializedAfterThisCall(ExplicitConstructorCall call, FlowInfo flowInfo) {

	/* We are chaining to `this(...)': Flag all non-static fields as definitely assigned
       since they are supposed to be set inside the alternate constructor. Which also means
       any final fields that are already assigned in the current prologue will result in
       duplicate initialization.
	*/
	FieldBinding[] fields = this.binding.declaringClass.fields();
	for (FieldBinding field : fields) {
		if (!field.isStatic()) {
			if (field.isBlankFinal() && flowInfo.isPotentiallyAssigned(field))
				this.scope.problemReporter().duplicateInitializationOfBlankFinalField(field, call);
			flowInfo.markAsDefinitelyAssigned(field);
		}
	}
}

@Override
public AbstractVariableDeclaration[] arguments(boolean includedElided) {
	return includedElided && this.isCompactConstructor() ? this.protoArguments : super.arguments(includedElided);
}

protected void doFieldReachAnalysis(FlowInfo flowInfo, FieldBinding[] fields) {
	for (FieldBinding field : fields) {
		if (!field.isStatic() && !flowInfo.isDefinitelyAssigned(field)) {
			if (field.isFinal()) {
				this.scope.problemReporter().uninitializedBlankFinalField(
						field,
						((this.bits & ASTNode.IsDefaultConstructor) != 0)
							? (ASTNode) this.scope.referenceType().declarationOf(field.original())
							: this);
			} else if (field.isNonNull() || field.type.isFreeTypeVariable()) {
				FieldDeclaration fieldDecl = this.scope.referenceType().declarationOf(field.original());
				if (!isValueProvidedUsingAnnotation(fieldDecl))
					this.scope.problemReporter().uninitializedNonNullField(
						field,
						((this.bits & ASTNode.IsDefaultConstructor) != 0)
							? (ASTNode) fieldDecl
							: this);
			}
		}
	}
}
boolean isValueProvidedUsingAnnotation(FieldDeclaration fieldDecl) {
	// a member field annotated with @Inject is considered to be initialized by the injector
	if (fieldDecl.annotations != null) {
		int length = fieldDecl.annotations.length;
		for (int i = 0; i < length; i++) {
			Annotation annotation = fieldDecl.annotations[i];
			int annotId = annotation.resolvedType.id;
			if (annotId == TypeIds.T_JavaxInjectInject || annotId == TypeIds.T_JakartaInjectInject) {
				return true; // no concept of "optional"
			} else if (annotId == TypeIds.T_ComGoogleInjectInject) {
				MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
				if (memberValuePairs == Annotation.NoValuePairs)
					return true;
				for (MemberValuePair memberValuePair : memberValuePairs) {
					// if "optional=false" is specified, don't rely on initialization by the injector:
					if (CharOperation.equals(memberValuePair.name, TypeConstants.OPTIONAL))
						return memberValuePair.value instanceof FalseLiteral;
				}
			} else if (annotId == TypeIds.T_OrgSpringframeworkBeansFactoryAnnotationAutowired) {
				MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
				if (memberValuePairs == Annotation.NoValuePairs)
					return true;
				for (MemberValuePair memberValuePair : memberValuePairs) {
					if (CharOperation.equals(memberValuePair.name, TypeConstants.REQUIRED))
						return memberValuePair.value instanceof TrueLiteral;
				}
			}
		}
	}
	return false;
}

@Override
public void generateCode(ClassScope classScope, ClassFile classFile) {
	int problemResetPC = 0;
	if (this.ignoreFurtherInvestigation) {
		if (this.binding == null)
			return; // Handle methods with invalid signature or duplicates
		int problemsLength;
		CategorizedProblem[] problems =
			this.scope.referenceCompilationUnit().compilationResult.getProblems();
		CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		classFile.addProblemConstructor(this, this.binding, problemsCopy);
		return;
	}
	boolean restart = false;
	boolean abort = false;
	CompilationResult unitResult = null;
	int problemCount = 0;
	if (classScope != null) {
		TypeDeclaration referenceContext = classScope.referenceContext;
		if (referenceContext != null) {
			unitResult = referenceContext.compilationResult();
			problemCount = unitResult.problemCount;
		}
	}
	do {
		try {
			problemResetPC = classFile.contentsOffset;
			internalGenerateCode(classScope, classFile);
			restart = false;
		} catch (AbortMethod e) {
			if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
				// a branch target required a goto_w, restart code gen in wide mode.
				classFile.contentsOffset = problemResetPC;
				classFile.methodCount--;
				classFile.codeStream.resetInWideMode(); // request wide mode
				// reset the problem count to prevent reporting the same warning twice
				if (unitResult != null) {
					unitResult.problemCount = problemCount;
				}
				restart = true;
			} else if (e.compilationResult == CodeStream.RESTART_CODE_GEN_FOR_UNUSED_LOCALS_MODE) {
				classFile.contentsOffset = problemResetPC;
				classFile.methodCount--;
				classFile.codeStream.resetForCodeGenUnusedLocals();
				// reset the problem count to prevent reporting the same warning twice
				if (unitResult != null) {
					unitResult.problemCount = problemCount;
				}
				restart = true;
			} else {
				restart = false;
				abort = true;
			}
		}
	} while (restart);
	if (abort) {
		int problemsLength;
		CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
		CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		classFile.addProblemConstructor(this, this.binding, problemsCopy, problemResetPC);
	}
}

public void generateSyntheticFieldInitializationsIfNecessary(MethodScope methodScope, CodeStream codeStream, ReferenceBinding declaringClass) {
	if (declaringClass instanceof NestedTypeBinding nestedType) {
		SyntheticArgumentBinding[] syntheticArgs = nestedType.syntheticEnclosingInstances();
		if (syntheticArgs != null) {
			for (SyntheticArgumentBinding syntheticArg : syntheticArgs) {
				if (syntheticArg.matchingField != null) {
					codeStream.aload_0();
					codeStream.load(syntheticArg);
					codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, null /* default declaringClass */);
				}
			}
		}
		syntheticArgs = nestedType.syntheticOuterLocalVariables();
		if (syntheticArgs != null) {
			for (SyntheticArgumentBinding syntheticArg : syntheticArgs) {
				if (syntheticArg.matchingField != null) {
					codeStream.aload_0();
					codeStream.load(syntheticArg);
					codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, null /* default declaringClass */);
				}
			}
		}
	}
}

private void internalGenerateCode(ClassScope classScope, ClassFile classFile) {
	classFile.generateMethodInfoHeader(this.binding);
	int methodAttributeOffset = classFile.contentsOffset;
	int attributeNumber = classFile.generateMethodInfoAttributes(this.binding);
	if ((!this.binding.isNative()) && (!this.binding.isAbstract())) {

		TypeDeclaration declaringType = classScope.referenceContext;
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);

		// initialize local positions - including initializer scope.
		ReferenceBinding declaringClass = this.binding.declaringClass;

		int enumOffset = declaringClass.isEnum() ? 2 : 0; // String name, int ordinal
		int argSlotSize = 1 + enumOffset; // this==aload0

		if (declaringClass.isNestedType()){
			this.scope.extraSyntheticArguments = declaringClass.syntheticOuterLocalVariables();
			this.scope.computeLocalVariablePositions(// consider synthetic arguments if any
					declaringClass.getEnclosingInstancesSlotSize() + 1 + enumOffset,
				codeStream);
			argSlotSize += declaringClass.getEnclosingInstancesSlotSize();
			argSlotSize += declaringClass.getOuterLocalVariablesSlotSize();
		} else {
			this.scope.computeLocalVariablePositions(1 + enumOffset,  codeStream);
		}

		for (LocalVariableBinding local : this.scope.locals) {
			if (local != null && local.isParameter()) {
				codeStream.addVisibleLocalVariable(local);
				local.recordInitializationStartPC(0);
				switch(local.type.id) {
					case TypeIds.T_long :
					case TypeIds.T_double :
						argSlotSize += 2;
						break;
					default :
						argSlotSize++;
						break;
				}
			}
		}

		MethodScope initializerScope = declaringType.initializerScope;
		initializerScope.computeLocalVariablePositions(argSlotSize, codeStream); // offset by the argument size (since not linked to method scope)

		codeStream.pushPatternAccessTrapScope(this.scope);
		ExplicitConstructorCall constructorCall = getConstructorCall();
		boolean needFieldInitializations = constructorCall == null || constructorCall.accessMode != ExplicitConstructorCall.This;

		// Synthetic initializations occur prior to explicit constructor call
		if (needFieldInitializations){
			generateSyntheticFieldInitializationsIfNecessary(this.scope, codeStream, declaringClass);
			codeStream.recordPositionsFrom(0, this.bodyStart > 0 ? this.bodyStart : this.sourceStart);
		}

		this.scope.enterEarlyConstructionContext();

		// generate statements
		if (this.statements != null) {
			for (Statement statement : this.statements) {
				statement.generateCode(this.scope, codeStream);
				if (!this.compilationResult.hasErrors() && (codeStream.stackDepth != 0 || codeStream.operandStack.size() != 0)) {
					this.scope.problemReporter().operandStackSizeInappropriate(this);
				}
				if (constructorCall == statement && constructorCall.accessMode != ExplicitConstructorCall.This) {
					if ((constructorCall.bits & IsReachable) != 0)
						generateFieldInitializations(declaringType, codeStream, initializerScope); // The single bit in the field can only say it is reachable in *some* universe
				}
			}
		}
		// if a problem got reported during code gen, then trigger problem method creation
		if (this.ignoreFurtherInvestigation) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
		if ((this.bits & ASTNode.NeedFreeReturn) != 0) {
			if (this.isCompactConstructor()) {
				// Note: the body of a compact constructor may not contain a return statement and so will need an injected return
				for (RecordComponent rc : classScope.referenceContext.recordComponents) {
					LocalVariableBinding parameter = this.scope.findVariable(rc.name);
					FieldBinding field = classScope.referenceContext.binding.getField(rc.name, true).original();
					codeStream.aload_0();
					codeStream.load(parameter);
					codeStream.fieldAccess(Opcodes.OPC_putfield, field, classScope.referenceContext.binding);
				}
			}
			codeStream.return_();
		}
		// See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796#issuecomment-1933458054
		codeStream.exitUserScope(this.scope, lvb -> !lvb.isParameter());
		codeStream.handleRecordAccessorExceptions(this.scope);
		// local variable attributes
		codeStream.exitUserScope(this.scope);
		codeStream.recordPositionsFrom(0, this.bodyEnd > 0 ? this.bodyEnd : this.sourceStart);
		try {
			classFile.completeCodeAttribute(codeAttributeOffset, this.scope);
		} catch(NegativeArraySizeException e) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
		attributeNumber++;
		if ((codeStream instanceof StackMapFrameCodeStream)
				&& needFieldInitializations
				&& declaringType.fields != null) {
			((StackMapFrameCodeStream) codeStream).resetSecretLocals();
		}
	}
	classFile.completeMethodInfo(this.binding, methodAttributeOffset, attributeNumber);
}
private void generateFieldInitializations(TypeDeclaration declaringType, CodeStream codeStream, MethodScope initializerScope) {
	if (declaringType.fields != null) {
		for (FieldDeclaration field : declaringType.fields) {
			if (!field.isStatic())
				field.generateCode(initializerScope, codeStream);
		}
	}
}

@Override
public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
	TypeReference fakeReturnType = new SingleTypeReference(this.selector, 0);
	fakeReturnType.resolvedType = this.binding.declaringClass;
	AnnotationCollector collector = new AnnotationCollector(fakeReturnType, targetType, allAnnotationContexts);
	for (Annotation annotation : this.annotations) {
		annotation.traverse(collector, (BlockScope) null);
	}
}

@Override
public boolean isConstructor() {
	return true;
}

public boolean invokesSuper() {
	ExplicitConstructorCall constructorCall = getConstructorCall();
	return constructorCall != null && constructorCall.accessMode != ExplicitConstructorCall.This;
}

@Override
public boolean isCanonicalConstructor() {
	return (this.bits & ASTNode.IsCanonicalConstructor) != 0;
}

@Override
public boolean isCompactConstructor() {
	return (this.modifiers & ExtraCompilerModifiers.AccCompactConstructor) != 0;
}

@Override
public boolean isDefaultConstructor() {
	return (this.bits & ASTNode.IsDefaultConstructor) != 0;
}

@Override
public boolean isInitializationMethod() {
	return true;
}

/*
 * Returns true if the constructor is directly involved in a cycle.
 * Given most constructors aren't, we only allocate the visited list
 * lazily.
 */
public boolean isRecursive(ArrayList visited) {
	ExplicitConstructorCall constructorCall = getConstructorCall();
	if (this.binding == null
			|| constructorCall == null
			|| constructorCall.binding == null
			|| constructorCall.isSuperAccess()
			|| !constructorCall.binding.isValidBinding()) {
		return false;
	}

	ConstructorDeclaration targetConstructor =
		((ConstructorDeclaration)this.scope.referenceType().declarationOf(constructorCall.binding.original()));
	if (targetConstructor == null) return false; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=358762
	if (this == targetConstructor) return true; // direct case

	if (visited == null) { // lazy allocation
		visited = new ArrayList(1);
	} else {
		int index = visited.indexOf(this);
		if (index >= 0) return index == 0; // only blame if directly part of the cycle
	}
	visited.add(this);

	return targetConstructor.isRecursive(visited);
}

@Override
public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
	// fill up the constructor body with its statements
    if (((this.bits & ASTNode.IsDefaultConstructor) != 0) && this.statements == null) {
    	chainUpwards();
        return;
    }
	parser.parse(this, unit, false);
}

@Override
public StringBuilder printBody(int indent, StringBuilder output) {
	output.append(" {"); //$NON-NLS-1$
	if (this.statements != null) {
		for (Statement statement : this.statements) {
			output.append('\n');
			statement.printStatement(indent, output);
		}
	}
	output.append('\n');
	printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
	return output;
}

@Override
public void resolveJavadoc() {
	if (this.binding == null || this.javadoc != null) {
		super.resolveJavadoc();
	} else if ((this.bits & ASTNode.IsDefaultConstructor) == 0 ) {
		if (this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
			// Set javadoc visibility
			int javadocVisibility = this.binding.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
			ClassScope classScope = this.scope.classScope();
			ProblemReporter reporter = this.scope.problemReporter();
			int severity = reporter.computeSeverity(IProblem.JavadocMissing);
			if (severity != ProblemSeverities.Ignore) {
				if (classScope != null) {
					javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
				}
				int javadocModifiers = (this.binding.modifiers & ~ExtraCompilerModifiers.AccVisibilityMASK) | javadocVisibility;
				reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
			}
		}
	}
}

@Override
public void resolve(ClassScope upperScope) {

	if (this.binding != null && this.binding.isCanonicalConstructor()) {
		RecordComponentBinding[] rcbs = upperScope.referenceContext.binding.components();
		boolean lastComponentVarargs = rcbs.length > 0 && rcbs[rcbs.length - 1].sourceRecordComponent().isVarArgs();
		if (this.binding.isVarargs() != lastComponentVarargs)
			upperScope.problemReporter().erasureIncompatibilityInCanonicalConstructor(this.arguments[this.arguments.length - 1].type);
		for (int i = 0; i < rcbs.length; ++i) {
			TypeBinding mpt = this.binding.parameters[i];
			TypeBinding rct = rcbs[i].type;
			if (TypeBinding.notEquals(mpt, rct))
				upperScope.problemReporter().erasureIncompatibilityInCanonicalConstructor(this.arguments[i].type);
		}

		if (!this.binding.isAsVisible(this.binding.declaringClass))
			this.scope.problemReporter().canonicalConstructorVisibilityReduced(this);
		if (this.typeParameters != null && this.typeParameters.length > 0)
			this.scope.problemReporter().canonicalConstructorShouldNotBeGeneric(this);
		if (this.binding.thrownExceptions != null && this.binding.thrownExceptions.length > 0)
			this.scope.problemReporter().canonicalConstructorHasThrowsClause(this);
		if (!this.isCompactConstructor()) {
			for (int i = 0; i < rcbs.length; i++)
				if (!CharOperation.equals(this.arguments[i].name, rcbs[i].name))
					this.scope.problemReporter().mismatchedParameterNameInCanonicalConstructor(rcbs[i], this.arguments[i]);
		}
	}
	super.resolve(upperScope);
}
/*
 * Type checking for constructor, just another method, except for special check
 * for recursive constructor invocations.
 */
@Override
public void resolveStatements() {
	SourceTypeBinding sourceType = this.scope.enclosingSourceType();
	if (!CharOperation.equals(sourceType.sourceName, this.selector)){
		this.scope.problemReporter().missingReturnType(this);
	}
	// typeParameters are already resolved from Scope#connectTypeVariables()
	if (this.binding != null && !this.binding.isPrivate()) {
		sourceType.tagBits |= TagBits.HasNonPrivateConstructor;
	}
	if ((this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
		this.scope.problemReporter().methodNeedBody(this);
	}
	this.scope.enterEarlyConstructionContext();
	super.resolveStatements();
	this.scope.leaveEarlyConstructionContext(); // code completion may work with diet mode constructors! These don't have ecc to issue leave!
	if (sourceType.id == TypeIds.T_JavaLangObject) {
		ExplicitConstructorCall constructorCall = getConstructorCall();
		if (constructorCall != null && constructorCall.accessMode != ExplicitConstructorCall.This) {
			if (constructorCall.accessMode == ExplicitConstructorCall.Super)
				this.scope.problemReporter().cannotUseSuperInJavaLangObject(constructorCall);
			for (int i = 0, length = this.statements.length; i < length; i++) {
				if (this.statements[i] == constructorCall) {
					this.statements[i] = new EmptyStatement(constructorCall.sourceStart, constructorCall.sourceEnd);
					break;
				}
			}
		}
	}
}

// returns the first constructor chaining call, early or late, implicit or explicit.
public ExplicitConstructorCall getConstructorCall() {
	if (this.statements != null) {
		for (int i = 0, length = this.statements.length; i < length; i++) {
			if (this.statements[i] instanceof ExplicitConstructorCall ctorCall)
				return ctorCall;
		}
	}
	return null;
}

public final void chainUpwards() {
	buildBody(ASTNode.NO_STATEMENTS, 0, 0, null);
}

public final void buildBody(ASTNode [] astStack, int astPtr, int length, /* @Nullable */CompilerOptions options) {

	for (int i = 0; i < length; i++) {
		Statement statement = (Statement) astStack[astPtr + i];
	    if (statement instanceof ExplicitConstructorCall) {
	    	if (i == 0 || (options != null && JavaFeature.FLEXIBLE_CONSTRUCTOR_BODIES.isSupported(options))) {
	    		System.arraycopy(astStack, astPtr, this.statements = new Statement[length], 0, length);
	    		return;
	    	}
	    }
	}

	boolean superCallPrecedes = true; // for JEP 401, super call is added to the tail end of the constructor

	this.statements = new Statement[length + 1];

	Statement superCall = SuperReference.implicitSuperConstructorCall();
	superCall.sourceEnd = this.sourceEnd;
	superCall.sourceStart = this.sourceStart;

	if (superCallPrecedes) {
		this.statements[0] = superCall;
		if (length > 0)
			System.arraycopy(astStack, astPtr, this.statements, 1, length);
	} else {
		this.statements[length] = superCall;
		if (length > 0)
			System.arraycopy(astStack, astPtr, this.statements, 0, length);
	}
}

@Override
public void traverse(ASTVisitor visitor, ClassScope classScope) {
	if (visitor.visit(this, classScope)) {
		if (this.javadoc != null) {
			this.javadoc.traverse(visitor, this.scope);
		}
		if (this.annotations != null) {
			int annotationsLength = this.annotations.length;
			for (int i = 0; i < annotationsLength; i++)
				this.annotations[i].traverse(visitor, this.scope);
		}
		if (this.typeParameters != null) {
			int typeParametersLength = this.typeParameters.length;
			for (int i = 0; i < typeParametersLength; i++) {
				this.typeParameters[i].traverse(visitor, this.scope);
			}
		}
		if (this.arguments != null) {
			int argumentLength = this.arguments.length;
			for (int i = 0; i < argumentLength; i++)
				this.arguments[i].traverse(visitor, this.scope);
		}
		if (this.thrownExceptions != null) {
			int thrownExceptionsLength = this.thrownExceptions.length;
			for (int i = 0; i < thrownExceptionsLength; i++)
				this.thrownExceptions[i].traverse(visitor, this.scope);
		}
		if (this.statements != null) {
			int statementsLength = this.statements.length;
			for (int i = 0; i < statementsLength; i++)
				this.statements[i].traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, classScope);
}
@Override
public TypeParameter[] typeParameters() {
    return this.typeParameters;
}
}
