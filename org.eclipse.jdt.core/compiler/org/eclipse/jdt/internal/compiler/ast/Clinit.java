package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class Clinit extends AbstractMethodDeclaration {
	public final static char[] ConstantPoolName = "<clinit>"/*nonNLS*/.toCharArray();
	
	private FieldBinding assertionSyntheticFieldBinding = null;
	private FieldBinding classLiteralSyntheticField = null;
	
public Clinit() {
	modifiers = 0;
	selector = ConstantPoolName;
}
public void analyseCode(ClassScope classScope, InitializationFlowContext staticInitializerFlowContext, FlowInfo flowInfo){
	
	if (ignoreFurtherInvestigation)
		return;
	try {
		ExceptionHandlingFlowContext clinitContext = new ExceptionHandlingFlowContext(
			staticInitializerFlowContext.parent, 
			this, 
			NoExceptions, 
			scope,
			FlowInfo.DeadEnd);

		// check for missing returning path
		needFreeReturn = !((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable());

		// check missing blank final field initializations
		flowInfo = flowInfo.mergedWith(staticInitializerFlowContext.initsOnReturn);
		FieldBinding[] fields = scope.enclosingSourceType().fields();
		for (int i = 0, count = fields.length; i < count; i++) {
			FieldBinding field;
			if ((field = fields[i]).isStatic()
				&& field.isFinal()
				&& (!flowInfo.isDefinitelyAssigned(fields[i]))) {
				scope.problemReporter().uninitializedBlankFinalField(field, scope.referenceType().declarationOf(field)); // can complain against the field decl, since only one <clinit>
			}
		}
		// check static initializers thrown exceptions
		staticInitializerFlowContext.checkInitializerExceptions(scope, clinitContext, flowInfo);
	} catch (AbortMethod e) {
		this.ignoreFurtherInvestigation = true;		
	}
}
/**
 * Bytecode generation for a <clinit> method
 *
 * @param classScope org.eclipse.jdt.internal.compiler.lookup.ClassScope
 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void generateCode(ClassScope classScope, ClassFile classFile) {
	int clinitOffset = 0;
	if (ignoreFurtherInvestigation) {
		// should never have to add any <clinit> problem method
		return;
	}
	try {
		clinitOffset = classFile.contentsOffset;
		ConstantPool constantPool = classFile.constantPool;
		int constantPoolOffset = constantPool.currentOffset;
		int constantPoolIndex = constantPool.currentIndex;
		classFile.generateMethodInfoHeaderForClinit();
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		this.resolve(classScope);
		
		codeStream.reset(this, classFile);
		TypeDeclaration declaringType = classScope.referenceContext;

		// initialize local positions - including initializer scope.
		scope.computeLocalVariablePositions(0, codeStream); // should not be necessary
		MethodScope staticInitializerScope = declaringType.staticInitializerScope;
		staticInitializerScope.computeLocalVariablePositions(0, codeStream); // offset by the argument size

		// 1.4 feature
		// This has to be done before any other initialization
		if (this.assertionSyntheticFieldBinding != null) {
			// generate code related to the activation of assertion for this class
			codeStream.generateClassLiteralAccessForType(classScope.enclosingSourceType(), classLiteralSyntheticField);
			codeStream.invokeJavaLangClassDesiredAssertionStatus();
			Label falseLabel = new Label(codeStream);
			codeStream.ifne(falseLabel);
			codeStream.iconst_1();
			Label jumpLabel = new Label(codeStream);
			codeStream.goto_(jumpLabel);
			falseLabel.place();
			codeStream.iconst_0();
			jumpLabel.place();
			codeStream.putstatic(this.assertionSyntheticFieldBinding);
		}

		// generate initializers
		if (declaringType.fields != null) {
			for (int i = 0, max = declaringType.fields.length; i < max; i++) {
				FieldDeclaration fieldDecl;
				if ((fieldDecl = declaringType.fields[i]).isStatic()) {
					fieldDecl.generateCode(staticInitializerScope, codeStream);
				}
			}
		}
		if (codeStream.position == 0) {
			// do not need to output a Clinit if no bytecodes
			// so we reset the offset inside the byte array contents.
			classFile.contentsOffset = clinitOffset;
			// like we don't addd a method we need to undo the increment on the method count
			classFile.methodCount--;
			// reset the constant pool to its state before the clinit
			constantPool.resetForClinit(constantPoolIndex, constantPoolOffset);
		} else {
			if (needFreeReturn) {
				int oldPosition = codeStream.position;
				codeStream.return_();
				codeStream.updateLocalVariablesAttribute(oldPosition);
			}
			// Record the end of the clinit: point to the declaration of the class
			codeStream.recordPositionsFrom(0, declaringType);
			classFile.completeCodeAttributeForClinit(codeAttributeOffset);
		}
	} catch (AbortMethod e) {
		// should never occur
		// the clinit referenceContext is the type declaration
		// All clinit problems will be reported against the type: AbortType instead of AbortMethod
		// reset the contentsOffset to the value before generating the clinit code
		// decrement the number of method info as well.
		// This is done in the addProblemMethod and addProblemConstructor for other
		// cases.
		classFile.contentsOffset = clinitOffset;
		classFile.methodCount--;	
	}
}
public boolean isClinit() {
	return true;
}
public boolean isInitializationMethod(){
	return true;
}
public boolean isStatic() {
	return true;
}
public void parseStatements(Parser parser, CompilationUnitDeclaration unit){
	//the clinit is filled by hand .... 
}
public void resolve(ClassScope scope) {
	this.scope = new MethodScope(scope, scope.referenceContext, true);
}
public String toString(int tab){
	/* slow code */
	String s = ""/*nonNLS*/ ;
	s = s + tabString(tab);
	s = s + "<clinit>()"/*nonNLS*/ ;
	s = s + toStringStatements(tab + 1);
	return s ;
}
	
public void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope classScope) {
	visitor.visit(this, classScope);
	visitor.endVisit(this, classScope);
}

public void addSupportForAssertion(FieldBinding assertionSyntheticFieldBinding) {
	this.assertionSyntheticFieldBinding = assertionSyntheticFieldBinding;
	// 1.4 feature
	// we need to add the field right now, because the field infos are generated before the methods
	SourceTypeBinding sourceType = this.scope.outerMostMethodScope().enclosingSourceType();
	this.classLiteralSyntheticField = sourceType.addSyntheticField(sourceType, scope);
}

}
