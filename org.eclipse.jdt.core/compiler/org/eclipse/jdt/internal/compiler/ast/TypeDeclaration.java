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
import org.eclipse.jdt.internal.compiler.util.*;


public class TypeDeclaration extends Statement implements ProblemSeverities, ReferenceContext {
	public int modifiers;
	public int modifiersSourceStart;
	public char[] name;
	public TypeReference superclass;
	public TypeReference[] superInterfaces;
	public FieldDeclaration[] fields;
	public AbstractMethodDeclaration[] methods;
	public MemberTypeDeclaration[] memberTypes;
	public SourceTypeBinding binding;
	public ClassScope scope;
	public MethodScope initializerScope;
	public MethodScope staticInitializerScope;
	public boolean ignoreFurtherInvestigation = false;
	public int maxFieldCount;
	public int declarationSourceStart ;
	public int declarationSourceEnd ;
	public int bodyStart;
	
/*
 *	We cause the compilation task to abort to a given extent.
 */
public void abort(int abortLevel) {

	if (scope == null){
		throw new AbortCompilation(); // cannot do better
	}

	CompilationResult compilationResult = scope.referenceCompilationUnit().compilationResult;
	
	switch (abortLevel) {
		case AbortCompilation :
			throw new AbortCompilation(compilationResult);
		case AbortCompilationUnit :
			throw new AbortCompilationUnit(compilationResult);
		case AbortMethod :
			throw new AbortMethod(compilationResult);
		default :
			throw new AbortType(compilationResult);
	}
}
/**
 * This method is responsible for adding a <clinit> method declaration to the type method collections.
 * Note that this implementation is inserting it in first place (as VAJ or javac), and that this
 * impacts the behavior of the method ConstantPool.resetForClinit(int. int), in so far as 
 * the latter will have to reset the constant pool state accordingly (if it was added first, it does 
 * not need to preserve some of the method specific cached entries since this will be the first method).
 * inserts the clinit method declaration in the first position.
 * 
 * @see org.eclipse.jdt.internal.compiler.codegen.ConstantPool.resetForClinit(int, int)
 */
public final void addClinit() {
	//see comment on needClassInitMethod

	if (needClassInitMethod()) {
		int length;
		AbstractMethodDeclaration[] methods;
		if ((methods = this.methods) == null) {
			length = 0;
			methods = new AbstractMethodDeclaration[1];
		} else {
			length = methods.length;
			System.arraycopy(methods, 0, (methods = new AbstractMethodDeclaration[length + 1]), 1, length);
		}
		Clinit clinit = new Clinit();
		methods[0] =  clinit;// clinit is added in first location, so as to minimize the use of ldcw (big consumer of constant inits)
		clinit.declarationSourceStart = clinit.sourceStart = sourceStart;
		clinit.declarationSourceEnd = clinit.sourceEnd = sourceEnd;
		this.methods = methods;
	}
}
/**
 *	Flow analysis for a local innertype
 *
 */
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (ignoreFurtherInvestigation)
		return flowInfo;
	try {
		// remember local types binding for innerclass emulation propagation
		currentScope.referenceCompilationUnit().record((LocalTypeBinding)binding);
		
		
		InitializationFlowContext initializerContext = new InitializationFlowContext(null, this, initializerScope);
		// propagate down the max field count
		updateMaxFieldCount();
		FlowInfo fieldInfo = flowInfo.copy(); // so as not to propagate changes outside this type
		if (fields != null) {
			for (int i = 0, count = fields.length; i < count; i++) {
				fieldInfo = fields[i].analyseCode(initializerScope, initializerContext, fieldInfo);
					if (fieldInfo == FlowInfo.DeadEnd) {
						// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
						// branch, since the previous initializer already got the blame.
						initializerScope.problemReporter().initializerMustCompleteNormally(fields[i]);
						fieldInfo = FlowInfo.initial(maxFieldCount).markAsFakeReachable(true);
					}					
			}
		}
		if (memberTypes != null) {
			for (int i = 0, count = memberTypes.length; i < count; i++) {
				memberTypes[i].analyseCode(scope, flowContext, fieldInfo.copy());
			}
		}
		if (methods != null) {
			int recursionBalance = 0; // check constructor recursions			
			for (int i = 0, count = methods.length; i < count; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.ignoreFurtherInvestigation)
					continue;
				if (method.isConstructor()) { // constructor
						ConstructorDeclaration constructor = (ConstructorDeclaration) method;
					 	constructor.analyseCode(scope, initializerContext, fieldInfo.copy());						
						// compute the recursive invocation balance:
						//   how many thisReferences vs. superReferences to constructors
						int refCount; 
						if ((refCount = constructor.referenceCount) > 0){
							if ((constructor.constructorCall == null) 
								|| constructor.constructorCall.isSuperAccess()
								|| !constructor.constructorCall.binding.isValidBinding()){
								recursionBalance -= refCount;
								constructor.referenceCount = -1; // for error reporting propagation																
							} else {
								recursionBalance += refCount;
							}
						}						
				} else { // regular method
					method.analyseCode(scope, null, flowInfo.copy());
				}
			}
			if (recursionBalance > 0){// there is one or more cycle(s) amongst constructor invocations
				scope.problemReporter().recursiveConstructorInvocation(this);
			}
		}
	} catch (AbortType e) {
		this.ignoreFurtherInvestigation = true;		
	}
	return flowInfo;
}
/**
 *	Flow analysis for a member innertype
 *
 */
public void analyseCode(ClassScope classScope1) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		// propagate down the max field count
		updateMaxFieldCount();
		FlowInfo flowInfo = FlowInfo.initial(maxFieldCount); // start fresh init info
		InitializationFlowContext initializerContext = new InitializationFlowContext(null, this, initializerScope);
		InitializationFlowContext staticInitializerContext = new InitializationFlowContext(null, this, staticInitializerScope);
		FlowInfo nonStaticFieldInfo = flowInfo.copy(), staticFieldInfo = flowInfo.copy();
		if (fields != null) {
			for (int i = 0, count = fields.length; i < count; i++) {
				if (fields[i].isStatic()) {
					staticFieldInfo = fields[i].analyseCode(staticInitializerScope, staticInitializerContext, staticFieldInfo);
					// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
					// branch, since the previous initializer already got the blame.
					if (staticFieldInfo == FlowInfo.DeadEnd) {
						staticInitializerScope.problemReporter().initializerMustCompleteNormally(fields[i]);
						staticFieldInfo = FlowInfo.initial(maxFieldCount).markAsFakeReachable(true);
					}
				} else {
					nonStaticFieldInfo = fields[i].analyseCode(initializerScope, initializerContext, nonStaticFieldInfo);
					// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
					// branch, since the previous initializer already got the blame.
					if (nonStaticFieldInfo == FlowInfo.DeadEnd) {
						initializerScope.problemReporter().initializerMustCompleteNormally(fields[i]);
						nonStaticFieldInfo = FlowInfo.initial(maxFieldCount).markAsFakeReachable(true);
					}					
				}
			} 
		}
		if (memberTypes != null) {
			for (int i = 0, count = memberTypes.length; i < count; i++) {
				memberTypes[i].analyseCode(scope);
			}
		}
		if (methods != null) {
			int recursionBalance = 0; // check constructor recursions
			for (int i = 0, count = methods.length; i < count; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.ignoreFurtherInvestigation)
					continue;
				if (method.isInitializationMethod()) {
					if (method.isStatic()) { // <clinit>
					 	((Clinit) method).analyseCode(scope, staticInitializerContext, staticFieldInfo);
					} else { // constructor
						ConstructorDeclaration constructor = (ConstructorDeclaration) method;
					 	constructor.analyseCode(scope, initializerContext, nonStaticFieldInfo.copy());						
						// compute the recursive invocation balance:
						//   how many thisReferences vs. superReferences to constructors
						int refCount; 
						if ((refCount = constructor.referenceCount) > 0){
							if ((constructor.constructorCall == null) 
								|| constructor.constructorCall.isSuperAccess()
								|| !constructor.constructorCall.binding.isValidBinding()){
								recursionBalance -= refCount;
								constructor.referenceCount = -1; // for error reporting propagation								
							} else {
								recursionBalance += refCount;
							}
						}						
					}
				} else { // regular method
					method.analyseCode(scope, null, FlowInfo.initial(maxFieldCount));
				}
			}
			if (recursionBalance > 0){// there is one or more cycle(s) amongst constructor invocations
				scope.problemReporter().recursiveConstructorInvocation(this);
			}
		}
	} catch (AbortType e) {
		this.ignoreFurtherInvestigation = true;		
	};
}
/**
 *	Flow analysis for a local member innertype
 *
 */
public void analyseCode(ClassScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		// remember local types binding for innerclass emulation propagation
		currentScope.referenceCompilationUnit().record((LocalTypeBinding)binding);

		/* force to emulation of access to direct enclosing instance: only for local members.
		 * By using the initializer scope, we actually only request an argument emulation, the
		 * field is not added until actually used. However we will force allocations to be qualified
		 * with an enclosing instance.
		 */
		initializerScope.emulateOuterAccess((SourceTypeBinding) binding.enclosingType(), false);
		
			
		InitializationFlowContext initializerContext = new InitializationFlowContext(null, this, initializerScope);
		// propagate down the max field count
		updateMaxFieldCount();
		FlowInfo fieldInfo = flowInfo.copy();// so as not to propagate changes outside this type
		if (fields != null) {
			for (int i = 0, count = fields.length; i < count; i++) {
				if (!fields[i].isStatic()) {
					fieldInfo = fields[i].analyseCode(initializerScope, initializerContext, fieldInfo);
					if (fieldInfo == FlowInfo.DeadEnd) {
						// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
						// branch, since the previous initializer already got the blame.
						initializerScope.problemReporter().initializerMustCompleteNormally(fields[i]);
						fieldInfo = FlowInfo.initial(maxFieldCount).markAsFakeReachable(true);
					}					
				}
			}
		}
		if (memberTypes != null) {
			for (int i = 0, count = memberTypes.length; i < count; i++) {
				memberTypes[i].analyseCode(scope, flowContext, fieldInfo.copy());
			}
		}
		if (methods != null) {
			int recursionBalance = 0; // check constructor recursions			
			for (int i = 0, count = methods.length; i < count; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.ignoreFurtherInvestigation)
					continue;
				if (method.isConstructor()) { // constructor
						ConstructorDeclaration constructor = (ConstructorDeclaration) method;
					 	constructor.analyseCode(scope, initializerContext, fieldInfo.copy());						
						// compute the recursive invocation balance:
						//   how many thisReferences vs. superReferences to constructors
						int refCount; 
						if ((refCount = constructor.referenceCount) > 0){
							if ((constructor.constructorCall == null) 
								|| constructor.constructorCall.isSuperAccess()
								|| !constructor.constructorCall.binding.isValidBinding()){
								recursionBalance -= refCount;
								constructor.referenceCount = -1; // for error reporting propagation								
							} else {
								recursionBalance += refCount;
							}
						}						
				} else { // regular method
					method.analyseCode(scope, null, flowInfo.copy());
				}
			}
			if (recursionBalance > 0){// there is one or more cycle(s) amongst constructor invocations
				scope.problemReporter().recursiveConstructorInvocation(this);
			}
		}
	} catch (AbortType e) {
		this.ignoreFurtherInvestigation = true;		
	};
}
/**
 *	Flow analysis for a package member type
 *
 */
public void analyseCode(CompilationUnitScope unitScope) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		FlowInfo flowInfo = FlowInfo.initial(maxFieldCount); // start fresh init info
		InitializationFlowContext initializerContext = new InitializationFlowContext(null, this, initializerScope);
		InitializationFlowContext staticInitializerContext = new InitializationFlowContext(null, this, staticInitializerScope);
		FlowInfo nonStaticFieldInfo = flowInfo.copy(), staticFieldInfo = flowInfo.copy();
		if (fields != null) {
			for (int i = 0, count = fields.length; i < count; i++) {
				if (fields[i].isStatic()) {
					staticFieldInfo = fields[i].analyseCode(staticInitializerScope, staticInitializerContext, staticFieldInfo);
					// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
					// branch, since the previous initializer already got the blame.
					if (staticFieldInfo == FlowInfo.DeadEnd) {
						staticInitializerScope.problemReporter().initializerMustCompleteNormally(fields[i]);
						staticFieldInfo = FlowInfo.initial(maxFieldCount).markAsFakeReachable(true);
					}
				} else {
					nonStaticFieldInfo = fields[i].analyseCode(initializerScope, initializerContext, nonStaticFieldInfo);
					// in case the initializer is not reachable, use a reinitialized flowInfo and enter a fake reachable
					// branch, since the previous initializer already got the blame.
					if (nonStaticFieldInfo == FlowInfo.DeadEnd) {
						initializerScope.problemReporter().initializerMustCompleteNormally(fields[i]);
						nonStaticFieldInfo = FlowInfo.initial(maxFieldCount).markAsFakeReachable(true);
					}					
				}
			}
		}
		if (memberTypes != null) {
			for (int i = 0, count = memberTypes.length; i < count; i++) {
				memberTypes[i].analyseCode(scope);
			}
		}
		if (methods != null) {
			int recursionBalance = 0; // check constructor recursions			
			for (int i = 0, count = methods.length; i < count; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.ignoreFurtherInvestigation)
					continue;
				if (method.isInitializationMethod()) {
					if (method.isStatic()) { // <clinit>
					 	((Clinit) method).analyseCode(scope, staticInitializerContext, staticFieldInfo);
					} else {// constructor
						ConstructorDeclaration constructor = (ConstructorDeclaration) method;
					 	constructor.analyseCode(scope, initializerContext, nonStaticFieldInfo.copy());						
						// compute the recursive invocation balance:
						//   how many thisReferences vs. superReferences to constructors
						int refCount; 
						if ((refCount = constructor.referenceCount) > 0){
							if ((constructor.constructorCall == null) 
								|| constructor.constructorCall.isSuperAccess()
								|| !constructor.constructorCall.binding.isValidBinding()){
								recursionBalance -= refCount;
								constructor.referenceCount = -1; // for error reporting propagation
							} else {
								recursionBalance += refCount;
							}
						}						
					}
				} else { // regular method
					method.analyseCode(scope, null, FlowInfo.initial(maxFieldCount));
				}
			}
			if (recursionBalance > 0){// there is one or more cycle(s) amongst constructor invocations
				scope.problemReporter().recursiveConstructorInvocation(this);
			}			
		}
	} catch (AbortType e) {
		this.ignoreFurtherInvestigation = true;		
	};
}
/*
 * Check for constructor vs. method with no return type.
 * Answers true if at least one constructor is defined
 */
public boolean checkConstructors(Parser parser) {
	//if a constructor has not the name of the type,
	//convert it into a method with 'null' as its return type

	boolean hasConstructor = false;
	if (methods != null) {
		for (int i = methods.length; --i >= 0;) {
			AbstractMethodDeclaration am;
			if ((am = methods[i]).isConstructor()) {
				if (!CharOperation.equals(am.selector, name)) {
					// the constructor was in fact a method with no return type
					// unless an explicit constructor call was supplied
					ConstructorDeclaration c = (ConstructorDeclaration) am;
					if ((c.constructorCall == null) 
						|| (c.constructorCall.isImplicitSuper())) { //changed to a method
						MethodDeclaration m = new MethodDeclaration();
						m.sourceStart = c.sourceStart;
						m.sourceEnd = c.sourceEnd;
						m.bodyStart = c.bodyStart;
						m.bodyEnd = c.bodyEnd;
						m.declarationSourceEnd = c.declarationSourceEnd;
						m.declarationSourceStart = c.declarationSourceStart;
						m.selector = c.selector;
						m.statements = c.statements;
						m.modifiers = c.modifiers;
						m.arguments = c.arguments;
						m.thrownExceptions = c.thrownExceptions;
						m.explicitDeclarations = c.explicitDeclarations;
						m.returnType = null;
						methods[i] = m;
					}
				} else {
					if (this.isInterface()){
						// report the problem and continue the parsing
						parser.problemReporter().interfaceCannotHaveConstructors((ConstructorDeclaration) am, parser.compilationUnit.compilationResult);
					}
					hasConstructor = true;
				}
			}
		}
	}
	return hasConstructor;
}
public CompilationResult compilationResult(){
	return scope.referenceCompilationUnit().compilationResult;
}
public ConstructorDeclaration createsInternalConstructor(
	boolean needExplicitConstructorCall, 
	boolean needToInsert) {

	//Add to method'set, the default constuctor that just recall the
	//super constructor with no arguments
	//The arguments' type will be positionned by the TC so just use
	//the default int instead of just null (consistency purpose)

	//the constructor
	ConstructorDeclaration constructor = new ConstructorDeclaration();
	constructor.isDefaultConstructor = true;
	constructor.selector = name;
	if (modifiers != AccDefault) {
		constructor.modifiers = 
			((this instanceof MemberTypeDeclaration) && (modifiers & AccPrivate) != 0)
				? AccDefault
				: modifiers & AccVisibilityMASK; 
	}

	//if you change this setting, please update the 
	//SourceIndexer2.buildTypeDeclaration(TypeDeclaration,char[]) method
	constructor.declarationSourceStart = constructor.sourceStart = sourceStart;
	constructor.declarationSourceEnd = constructor.sourceEnd = constructor.bodyEnd = sourceEnd;

	//the super call inside the constructor
	if (needExplicitConstructorCall){
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		constructor.constructorCall.sourceStart = sourceStart; 
		constructor.constructorCall.sourceEnd = sourceEnd;
	}

	//adding the constructor in the methods list
	if (needToInsert){
		if (methods == null) {
			methods = new AbstractMethodDeclaration[] {constructor};
		} else {
			AbstractMethodDeclaration[] newMethods;
			System.arraycopy(
				methods, 
				0, 
				newMethods = new AbstractMethodDeclaration[methods.length + 1], 
				1, 
				methods.length); 
			newMethods[0] = constructor;
			methods = newMethods;
		}
	}
	return constructor;
}
/*
 * Find the matching parse node, answers null if nothing found
 */
public FieldDeclaration declarationOf(FieldBinding fieldBinding) {
	if (fieldBinding != null) {
		for (int i = 0, max = this.fields.length; i < max; i++) {
			FieldDeclaration fieldDecl;
			if ((fieldDecl = this.fields[i]).binding == fieldBinding)
				return fieldDecl;
		}
	}
	return null;
}
/*
 * Find the matching parse node, answers null if nothing found
 */
public TypeDeclaration declarationOf(MemberTypeBinding memberTypeBinding) {
	if (memberTypeBinding != null) {
		for (int i = 0, max = this.memberTypes.length; i < max; i++) {
			TypeDeclaration memberTypeDecl;
			if ((memberTypeDecl = this.memberTypes[i]).binding == memberTypeBinding)
				return memberTypeDecl;
		}
	}
	return null;
}
/*
 * Find the matching parse node, answers null if nothing found
 */
public AbstractMethodDeclaration declarationOf(MethodBinding methodBinding) {
	if (methodBinding != null) {
		for (int i = 0, max = this.methods.length; i < max; i++) {
			AbstractMethodDeclaration methodDecl;
			
			if ((methodDecl = this.methods[i]).binding == methodBinding)
				return methodDecl;
		}
	}
	return null;
}
/*
 * Finds the matching type amoung this type's member types.
 * Returns null if no type with this name is found.
 * The type name is a compound name relative to this type
 * eg. if this type is X and we're looking for Y.X.A.B
 *     then a type name would be {X, A, B}
 */
public TypeDeclaration declarationOfType(char[][] typeName) {
	int typeNameLength = typeName.length;
	if (typeNameLength < 1 || !CharOperation.equals(typeName[0], this.name)) {
		return null;
	}
	if (typeNameLength == 1) {
		return this;
	}
	char[][] subTypeName = new char[typeNameLength - 1][];
	System.arraycopy(typeName, 1, subTypeName, 0, typeNameLength - 1);
	for (int i = 0; i < this.memberTypes.length; i++) {
		TypeDeclaration typeDecl = this.memberTypes[i].declarationOfType(subTypeName);
		if (typeDecl != null) {
			return typeDecl;
		}
	}
	return null;
}
/**
 * Generic bytecode generation for type
 */
public void generateCode(ClassFile enclosingClassFile) {
	if (ignoreFurtherInvestigation) {
		if (binding == null)
			return;
		ClassFile.createProblemType(this, scope.referenceCompilationUnit().compilationResult);
		return;
	}
	try {
		// create the result for a compiled type
		ClassFile classFile = new ClassFile(binding, enclosingClassFile, false);
		// generate all fiels
		classFile.addFieldInfos();

		// record the inner type inside its own .class file to be able
		// to generate inner classes attributes
		if (binding.isMemberType())
			classFile.recordEnclosingTypeAttributes(binding);
		if (binding.isLocalType()) {
			enclosingClassFile.recordNestedLocalAttribute(binding);
			classFile.recordNestedLocalAttribute(binding);
		}
		if (memberTypes != null) {
			for (int i = 0, max = memberTypes.length; i < max; i++) {
				// record the inner type inside its own .class file to be able
				// to generate inner classes attributes
				classFile.recordNestedMemberAttribute(memberTypes[i].binding);
				memberTypes[i].generateCode(scope, classFile);
			}
		}
		// generate all methods
		classFile.setForMethodInfos();
		if (methods != null) {
			for (int i = 0, max = methods.length; i < max; i++) {
				methods[i].generateCode(scope, classFile);
			}
		}
		
		// generate all methods
		classFile.addSpecialMethods();

		if (ignoreFurtherInvestigation){ // trigger problem type generation for code gen errors
			throw new AbortType(scope.referenceCompilationUnit().compilationResult);
		}

		// finalize the compiled type result
		classFile.addAttributes();
		scope.referenceCompilationUnit().compilationResult.record(binding.constantPoolName(), classFile);
	} catch (AbortType e) {
		if (binding == null)
			return;
		ClassFile.createProblemType(this, scope.referenceCompilationUnit().compilationResult);
	}
}
/**
 * Bytecode generation for a local inner type (API as a normal statement code gen)
 */
public void generateCode(BlockScope blockScope, CodeStream codeStream) {
	int pc = codeStream.position;
	if (binding != null){ 
		 ((NestedTypeBinding) binding).computeSyntheticArgumentsOffset();
	}
	generateCode(codeStream.classFile);
	codeStream.recordPositionsFrom(pc, this);
}
/**
 * Bytecode generation for a member inner type
 */
public void generateCode(ClassScope classScope, ClassFile enclosingClassFile) {

	((NestedTypeBinding)binding).computeSyntheticArgumentsOffset();
	generateCode(enclosingClassFile);
}
/**
 * Bytecode generation for a package member
 */
public void generateCode(CompilationUnitScope unitScope) {
	generateCode((ClassFile)null);
}
public boolean isInterface() {
	
	return (modifiers & AccInterface) != 0;
}
public final boolean needClassInitMethod() {
	//the real test is next code but the test on NotAConstant is
	//not valid UNTIL the TC has runned :-( .....
	//More, the binding hasn't been done.....:-(....

	//Thus the idea is therefore to generate a <clint> when some static
	//fields exist. If it has no statement, it will not be generated

	/*--------------------------------------------------
	if ( fields == null ) return false;
	for( int i = fields.length ; --i>= 0;){
	FieldDeclaration field = fields[i];
	if (field.binding.isStatic())
	{if ( (field.binding.isFinal()) && (field.initialization == null) ) return true ;
	if ( !field.isField()) return true ; //initializer are not-isField()
	if ( (field.initialization != null) && (field.binding.constant == NotAConstant) ) return true ;}}
	
	return false ;}
	----------------------------------------------------*/
	if ((this.bits & AddAssertionMASK) != 0) {
		return true;
	}
	if (fields == null)
		return false;
	if (isInterface()) return true; // fields are implicitly statics
	for (int i = fields.length; --i >= 0;) {
		FieldDeclaration field = fields[i];
		//need to test the modifier directly while there is no binding yet
		if ((field.modifiers & AccStatic) != 0)
			return true;
	}

	return false;
}
public void parseMethod(Parser parser, CompilationUnitDeclaration unit) {
	//connect method bodies
	if (unit.ignoreMethodBodies) return;

	// no scope were created, so cannot report further errors
	if (binding == null) return;
	
	//members
	if (memberTypes != null) {
		for (int i = memberTypes.length; --i >= 0;)
			memberTypes[i].parseMethod(parser, unit);
	}

	//methods
	if (methods != null) {
		for (int i = methods.length; --i >= 0;)
			methods[i].parseStatements(parser, unit);
	}

	//initializers
	if (fields != null) {
		for (int i = fields.length; --i >= 0;) {
			if (fields[i] instanceof Initializer) {
				((Initializer) fields[i]).parseStatements(parser, this, unit);
			}
		}
	}
}
public void resolve() {
	if (binding == null) {
		ignoreFurtherInvestigation = true;
		return;
	}

	try {
		// check superclass & interfaces
		if (binding.superclass != null) // watch out for Object ! (and other roots)	
			if (isTypeUseDeprecated(binding.superclass, scope))
				scope.problemReporter().deprecatedType(binding.superclass, superclass);
		if (superInterfaces != null)
			for (int i = superInterfaces.length; --i >=0;)
				if (superInterfaces[i].binding != null)
					if (isTypeUseDeprecated(superInterfaces[i].binding, scope))
						scope.problemReporter().deprecatedType(superInterfaces[i].binding, superInterfaces[i]);

		maxFieldCount = 0;
		int lastFieldID = -1;
		if (fields != null) {
			for (int i = 0, count = fields.length; i < count; i++) {
				FieldDeclaration field = fields[i];
				if (field.isField()) {
					if (field.binding == null) {
						ignoreFurtherInvestigation = true;
						continue;
					}
					maxFieldCount++;
					lastFieldID = field.binding.id;
				} else { // initializer
				 	((Initializer) field).lastFieldID = lastFieldID + 1;
				}
				field.resolve(field.isStatic() ? staticInitializerScope : initializerScope);
			}
		}
		if (memberTypes != null)
			for (int i = 0, count = memberTypes.length; i < count; i++)
				memberTypes[i].resolve(scope);
		if (methods != null)
			for (int i = 0, count = methods.length; i < count; i++)
				methods[i].resolve(scope);
	} catch (AbortType e) {
		this.ignoreFurtherInvestigation = true;		
		return;
	};
}
public void resolve(BlockScope blockScope) {
	// local type declaration

	// need to build its scope first and proceed with binding's creation
	blockScope.addLocalType(this);

	// and TC....
	if (binding != null) { // binding is not set if the receiver could not be created
		resolve();
		updateMaxFieldCount();
	}
}
public void resolve(ClassScope upperScope) {
	// member scopes are already created
	// request the construction of a binding if local member type

	resolve();
	updateMaxFieldCount();
}
public void resolve(CompilationUnitScope upperScope) {
	// top level : scope are already created

	resolve();
	updateMaxFieldCount();
}
public void tagAsHavingErrors(){
	ignoreFurtherInvestigation = true;
}
public String toString(int tab ){
	/*slow code */

	return 	tabString(tab) + toStringHeader() + 
			toStringBody(tab);
	}
public String toStringBody(int tab) {
	/*slow code */

	String s = " {"/*nonNLS*/;
	if (memberTypes != null) {
		for (int i = 0; i < memberTypes.length; i++) {
			if (memberTypes[i] != null) {
				s += "\n"/*nonNLS*/ + memberTypes[i].toString(tab + 1);
			}
		}
	}
	if (fields != null) {
		for (int fieldI = 0; fieldI < fields.length; fieldI++) {
			if (fields[fieldI] != null) {
				s += "\n"/*nonNLS*/ + fields[fieldI].toString(tab + 1);
				if (fields[fieldI].isField()) s += ";"/*nonNLS*/;
			}
		}
	}
	if (methods != null) {
		for (int i = 0; i < methods.length; i++) {
			if (methods[i] != null) {
				s += "\n"/*nonNLS*/ + methods[i].toString(tab + 1);
			}
		}
	}
	s += "\n"/*nonNLS*/+tabString(tab) + "}"/*nonNLS*/;	
	return s;
}
public String toStringHeader() {
	/*slow code */

	String s = ""/*nonNLS*/;
	if (modifiers != AccDefault) {
		s += modifiersString(modifiers);
	}
	s += (isInterface() ? "interface "/*nonNLS*/ : "class "/*nonNLS*/) + new String(name);
	if (superclass != null)
		s += " extends "/*nonNLS*/ + superclass.toString(0);
	if (superInterfaces != null && superInterfaces.length > 0) {
		s += (isInterface() ? " extends "/*nonNLS*/ : " implements "/*nonNLS*/);
		for (int i = 0; i < superInterfaces.length; i++) {
			s += superInterfaces[i].toString(0);
			if (i != superInterfaces.length-1)
				s += ", "/*nonNLS*/;
		};
	};

	return s;
}
/**
 *	Iteration for a local innertype
 *
 */
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, blockScope)) {
			if (superclass != null)
				superclass.traverse(visitor, scope);
			if (superInterfaces != null) {
				int superInterfaceLength = superInterfaces.length;
				for (int i = 0; i < superInterfaceLength; i++)
					superInterfaces[i].traverse(visitor, scope);
			}
			if (memberTypes != null) {
				int memberTypesLength = memberTypes.length;
				for (int i = 0; i < memberTypesLength; i++)
					memberTypes[i].traverse(visitor, scope);
			}
			if (fields != null) {
				int fieldsLength = fields.length;
				for (int i = 0; i < fieldsLength; i++) {
					FieldDeclaration field;
					if ((field = fields[i]).isStatic()) {
						// local type cannot have static fields
					} else {
						field.traverse(visitor, initializerScope);
					}
				}
			}
			if (methods != null) {
				int methodsLength = methods.length;
				for (int i = 0; i < methodsLength; i++)
					methods[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	} catch (AbortType e) {
	}
}
/**
 *	Iteration for a member innertype
 *
 */
public void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope classScope) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, classScope)) {
			if (superclass != null)
				superclass.traverse(visitor, scope);
			if (superInterfaces != null) {
				int superInterfaceLength = superInterfaces.length;
				for (int i = 0; i < superInterfaceLength; i++)
					superInterfaces[i].traverse(visitor, scope);
			}
			if (memberTypes != null) {
				int memberTypesLength = memberTypes.length;
				for (int i = 0; i < memberTypesLength; i++)
					memberTypes[i].traverse(visitor, scope);
			}
			if (fields != null) {
				int fieldsLength = fields.length;
				for (int i = 0; i < fieldsLength; i++) {
					FieldDeclaration field;
					if ((field = fields[i]).isStatic()) {
						field.traverse(visitor, staticInitializerScope);
					} else {
						field.traverse(visitor, initializerScope);
					}
				}
			}
			if (methods != null) {
				int methodsLength = methods.length;
				for (int i = 0; i < methodsLength; i++)
					methods[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	} catch (AbortType e) {
	}
}
/**
 *	Iteration for a package member type
 *
 */
public void traverse(IAbstractSyntaxTreeVisitor visitor, CompilationUnitScope unitScope) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, unitScope)) {
			if (superclass != null)
				superclass.traverse(visitor, scope);
			if (superInterfaces != null) {
				int superInterfaceLength = superInterfaces.length;
				for (int i = 0; i < superInterfaceLength; i++)
					superInterfaces[i].traverse(visitor, scope);
			}
			if (memberTypes != null) {
				int memberTypesLength = memberTypes.length;
				for (int i = 0; i < memberTypesLength; i++)
					memberTypes[i].traverse(visitor, scope);
			}
			if (fields != null) {
				int fieldsLength = fields.length;
				for (int i = 0; i < fieldsLength; i++) {
					FieldDeclaration field;
					if ((field = fields[i]).isStatic()) {
						field.traverse(visitor, staticInitializerScope);
					} else {
						field.traverse(visitor, initializerScope);
					}
				}
			}
			if (methods != null) {
				int methodsLength = methods.length;
				for (int i = 0; i < methodsLength; i++)
					methods[i].traverse(visitor, scope);
			}
		}
	} catch (AbortType e) {
	}
}
/**
 * MaxFieldCount's computation is necessary so as to reserve space for
 * the flow info field portions. It corresponds to the maximum amount of
 * fields this class or one of its innertypes have.
 *
 * During name resolution, types are traversed, and the max field count is recorded
 * on the outermost type. It is then propagated down during the flow analysis.
 *
 * This method is doing either up/down propagation.
 */
void updateMaxFieldCount() {
	if (binding == null) return; // error scenario
	TypeDeclaration outerMostType = scope.outerMostClassScope().referenceType();
	if (maxFieldCount > outerMostType.maxFieldCount) {
		outerMostType.maxFieldCount = maxFieldCount; // up
	} else {
		maxFieldCount = outerMostType.maxFieldCount; // down
	}
}
}
