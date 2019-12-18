/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class RecordDeclaration extends TypeDeclaration {

	private Argument[] args;
	public int nRecordComponents;
	private static long[] EMPTY_LONG_POS = {-1, -1, -1 };

	public static Set<String> disallowedComponentNames;
	static {
		disallowedComponentNames = new HashSet<>(6);
		disallowedComponentNames.add("finalize"); //$NON-NLS-1$
		disallowedComponentNames.add("getClass"); //$NON-NLS-1$
		disallowedComponentNames.add("hashCode"); //$NON-NLS-1$
		disallowedComponentNames.add("notify");   //$NON-NLS-1$
		disallowedComponentNames.add("notifyAll");//$NON-NLS-1$
		disallowedComponentNames.add("toString"); //$NON-NLS-1$
	}
	public RecordDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}
	public RecordDeclaration(TypeDeclaration t) {
		super(t.compilationResult);
		this.modifiers = t.modifiers;
		this.modifiersSourceStart = t.modifiersSourceStart;
		this.annotations = t.annotations;
		this.name = t.name;
		this.superInterfaces = t.superInterfaces;
		this.fields = t.fields;
		this.methods = t.methods;
		this.memberTypes = t.memberTypes;
		this.binding = t.binding;
		this.scope = t.scope;
		this.initializerScope = t.initializerScope;
		this.staticInitializerScope = t.staticInitializerScope;
		this.ignoreFurtherInvestigation = t.ignoreFurtherInvestigation;
		this.maxFieldCount = t.maxFieldCount;
		this.declarationSourceStart = t.declarationSourceStart;
		this.declarationSourceEnd = t.declarationSourceEnd;
		this.bodyStart = t.bodyStart;
		this.bodyEnd = t.bodyEnd;
		this.missingAbstractMethods = t.missingAbstractMethods; // TODO: Investigate whether this is relevant.
		this.javadoc = t.javadoc;
		this.allocation = t.allocation;
		this.enclosingType = t.enclosingType;
		this.typeParameters = t.typeParameters;
		this.sourceStart = t.sourceStart;
		this.sourceEnd = t.sourceEnd;
	}
	public ConstructorDeclaration getConstructor(Parser parser) {
		if (this.methods != null) {
			for (int i = this.methods.length; --i >= 0;) {
				AbstractMethodDeclaration am;
				if ((am = this.methods[i]).isConstructor()) {
					if (!CharOperation.equals(am.selector, this.name)) {
						// the constructor was in fact a method with no return type
						// unless an explicit constructor call was supplied
						ConstructorDeclaration c = (ConstructorDeclaration) am;
						if (c.constructorCall == null || c.constructorCall.isImplicitSuper()) { //changed to a method
							MethodDeclaration m = parser.convertToMethodDeclaration(c, this.compilationResult);
							this.methods[i] = m;
						}
					} else {
						if (am instanceof CompactConstructorDeclaration) {
							CompactConstructorDeclaration ccd = (CompactConstructorDeclaration) am;
							ccd.recordDeclaration = this;
							if (ccd.arguments == null)
								ccd.arguments = this.args;
							return ccd;
						}
						// now we are looking at a "normal" constructor
						if (this.args == null && am.arguments == null)
							return (ConstructorDeclaration) am;
					}
				}
			}
		}
		/* At this point we can only say that there is high possibility that there is a constructor
		 * If it is a CCD, then definitely it is there (except for empty one); else we need to check 
		 * the bindings to say that there is a canonical constructor. To take care at binding resolution time.
		 */
		return null;
	}

	/** Returns an implicit canonical constructor, if any.
	 */
	public static ConstructorDeclaration getImplicitCanonicalConstructor(AbstractMethodDeclaration[] methods) {
		if (methods == null)
			return null;
		for (AbstractMethodDeclaration am : methods) {
			if (am instanceof ConstructorDeclaration && (am.bits & (ASTNode.IsCanonicalConstructor | ASTNode.IsImplicit)) != 0)
				return (ConstructorDeclaration) am;
		}
		return null;
	}
	@Override
	public ConstructorDeclaration createDefaultConstructor(boolean needExplicitConstructorCall, boolean needToInsert) {
		//Add to method'set, the default constuctor that just recall the
		//super constructor with no arguments
		//The arguments' type will be positionned by the TC so just use
		//the default int instead of just null (consistency purpose)

		ConstructorDeclaration constructor = new ConstructorDeclaration(this.compilationResult);
		constructor.bits |= ASTNode.IsCanonicalConstructor | ASTNode.IsImplicit;
		constructor.selector = this.name;
//		constructor.modifiers = this.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
		constructor.modifiers = this.modifiers & ClassFileConstants.AccPublic;
		constructor.modifiers |= ClassFileConstants.AccPublic; // JLS 14 8.10.5
		constructor.arguments = this.args;

		constructor.declarationSourceStart = constructor.sourceStart =
				constructor.bodyStart = this.sourceStart;
		constructor.declarationSourceEnd =
			constructor.sourceEnd = constructor.bodyEnd =  this.sourceStart - 1;

		//the super call inside the constructor
		// needExplicitConstructorCall is ignored for RecordDeclaration.

		/* The body of the implicitly declared canonical constructor initializes each field corresponding
		 * to a record component with the corresponding formal parameter in the order that they appear
		 * in the record component list.*/
		int l = this.args != null ? this.args.length : 0;
		Statement[] statements = new Statement[l];
		for (int i = 0; i < l; ++i) {
			Argument arg = this.args[i];
			FieldReference lhs = new FieldReference(arg.name, 0);
			lhs.receiver = ThisReference.implicitThis();
			statements[i] =  new Assignment(lhs, new SingleNameReference(arg.name, 0), 0);
		}
		constructor.statements = statements;

		//adding the constructor in the methods list: rank is not critical since bindings will be sorted
		if (needToInsert) {
			if (this.methods == null) {
				this.methods = new AbstractMethodDeclaration[] { constructor };
			} else {
				AbstractMethodDeclaration[] newMethods;
				System.arraycopy(
					this.methods,
					0,
					newMethods = new AbstractMethodDeclaration[this.methods.length + 1],
					1,
					this.methods.length);
				newMethods[0] = constructor;
				this.methods = newMethods;
			}
		}
		return constructor;
	}

	public void createImplicitAccessors(ProblemReporter problemReporter) {
		// JLS 14 8.10.3 Item 2 create the accessors for the fields if required
		/* 
		 * An implicitly declared public accessor method with the same name as the record component,
		 * whose return type is the declared type of the record component,
		 * unless a public method with the same signature is explicitly declared in the body of the declaration of R. 
		 */

		if (this.fields == null)
			return;
		Map<String, Set<AbstractMethodDeclaration>> accessors = new HashMap<>();
		for (int i = 0; i < this.nRecordComponents; i++) {
			FieldDeclaration f = this.fields[i] ;
			if (f != null && f.name != null && f.name.length > 0) {
				accessors.put(new String(f.name), new HashSet<>());
			}
		}
		if (this.methods != null) {
			for (int i = 0; i < this.methods.length; i++) {
				AbstractMethodDeclaration m = this.methods[i]; 
				if (m != null && m.selector != null & m.selector.length > 0) {
					String name1 = new String(m.selector);
					Set<AbstractMethodDeclaration> acc = accessors.get(name1);
					if (acc != null)
						acc.add(m);
				}
			}
		}
		for (int i = this.nRecordComponents - 1; i >= 0; i--) {
			FieldDeclaration f = this.fields[i] ;
			if (f != null && f.name != null && f.name.length > 0) {
				String name1 = new String(f.name);
				Set<AbstractMethodDeclaration> acc = accessors.get(name1);
				MethodDeclaration m = null;
				if (acc.size() > 0) {
					for (AbstractMethodDeclaration amd : acc) {
						m = (MethodDeclaration) amd;
						/* JLS 14 Sec 8.10.3 Item 1, Subitem 2
						 * An implicitly declared public accessor method with the same name as the record component, whose return
						 * type is the declared type of the record component, unless a public method with the same signature is
						 * explicitly declared in the body of the declaration of R
						 */
						// Here the assumption is method signature implies the method signature in source ie the return type
						// is not being considered - Given this, type resolution is not required and hence its a simple name and
						// parameter number check.
						if (m.arguments == null || m.arguments.length == 0) {
							// found the explicitly declared accessor.
							/*
							 *  JLS 14 Sec 8.10.3 Item 1 Sub-item 2 Para 3
							 *  It is a compile-time error if an explicitly declared accessor method has a throws clause.
							 */
							if (m.thrownExceptions != null && m.thrownExceptions.length > 0)
								problemReporter.recordAccessorMethodHasThrowsClause(m);
							break; // found
						}
						m = null;
					}
				}
				if (m == null) // no explicit accessor method found - declare one.
					createNewMethod(f);
			}
		}
	}
	@Override
	public void generateCode(ClassFile enclosingClassFile) {
		super.generateCode(enclosingClassFile);
	}
	private AbstractMethodDeclaration createNewMethod(FieldDeclaration f) {
		MethodDeclaration m = new MethodDeclaration(this.compilationResult);
		m.selector = f.name;
		m.bits |= ASTNode.IsImplicit;
		m.modifiers = ClassFileConstants.AccPublic;

		m.returnType = f.type;
		FieldReference fr = new FieldReference(f.name, -1);
		fr.receiver = new ThisReference(-1, -1);
		ReturnStatement ret = new ReturnStatement(fr, -1, -1);
		m.statements = new Statement[] { ret };
		m.isImplicit = true;
		/*
		 * JLS 14 Sec 8.10.3 Item 2 states that:
		 * "The implicitly declared accessor method is annotated with the annotation
		 * that appears on the corresponding record component, if this annotation type
		 * is applicable to a method declaration or type context."
		 * 
		 * However, at this point in compilation, sufficient information to determine
		 * the ElementType targeted by the annotation doesn't exist and hence a blanket 
		 * copy of annotation is done for now, and later (binding stage) irrelevant ones
		 * are weeded out.
		 */
		m.annotations = f.annotations;

		if (this.methods == null) { // Where is the constructor?
			this.methods = new AbstractMethodDeclaration[] { m };
		} else {
			AbstractMethodDeclaration[] newMethods;
			System.arraycopy(
				this.methods,
				0,
				newMethods = new AbstractMethodDeclaration[this.methods.length + 1],
				1,
				this.methods.length);
			newMethods[0] = m;
			this.methods = newMethods;
		}
		return m;
	}
	@Override
	public boolean isRecord() {
		return true;
	}
	@Override
	public StringBuffer printHeader(int indent, StringBuffer output) {
		printModifiers(this.modifiers, output);
		if (this.annotations != null) {
			printAnnotations(this.annotations, output);
			output.append(' ');
		}

		output.append("record "); //$NON-NLS-1$
		output.append(this.name);
		output.append('(');
		if (this.nRecordComponents > 0 && this.fields != null) {
			for (int i = 0; i < this.nRecordComponents; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				output.append(this.fields[i].type.getTypeName()[0]);
				output.append(' ');
				output.append(this.fields[i].name);
			}
		}
		output.append(')');
		if (this.typeParameters != null) {
			output.append("<");//$NON-NLS-1$
			for (int i = 0; i < this.typeParameters.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				this.typeParameters[i].print(0, output);
			}
			output.append(">");//$NON-NLS-1$
		}
		if (this.superInterfaces != null && this.superInterfaces.length > 0) {
			output.append(" implements "); //$NON-NLS-1$
			for (int i = 0; i < this.superInterfaces.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				this.superInterfaces[i].print(0, output);
			}
		}
		return output;
	}
	@Override
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(" {"); //$NON-NLS-1$
		if (this.memberTypes != null) {
			for (int i = 0; i < this.memberTypes.length; i++) {
				if (this.memberTypes[i] != null) {
					output.append('\n');
					this.memberTypes[i].print(indent + 1, output);
				}
			}
		}
		if (this.fields != null) {
			for (int fieldI = 0; fieldI < this.fields.length; fieldI++) {
				if (this.fields[fieldI] != null) {
					output.append('\n');
					if (fieldI < this.nRecordComponents)
						output.append("/* Implicit */"); //$NON-NLS-1$ //TODO BETA_JAVA14: Move this to FD?
					this.fields[fieldI].print(indent + 1, output);
				}
			}
		}
		if (this.methods != null) {
			for (int i = 0; i < this.methods.length; i++) {
				if (this.methods[i] != null) {
					output.append('\n');
					AbstractMethodDeclaration amd = this.methods[i];
					if (amd instanceof MethodDeclaration && ((MethodDeclaration) amd).isImplicit)
						output.append("/* Implicit */\n"); //$NON-NLS-1$// TODO BETA_JAVA14: Move this to MD?
					amd.print(indent + 1, output);
				}
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
	public Argument[] getArgs() {
		return this.args;
	}
	public void setArgs(Argument[] args) {
		this.args = args;
	}
	public static void checkAndFlagRecordNameErrors(char[] typeName, ASTNode node, Scope skope) {
		if (CharOperation.equals(typeName, TypeConstants.RECORD)) {
			if (skope.compilerOptions().sourceLevel == ClassFileConstants.JDK14) {
					skope.problemReporter().recordIsAReservedTypeName(node);
			}
		}
	}
	public void createImplicitRecordOverrideMethods(ProblemReporter problemReporter) {
		TypeReference superClass = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] {0});
		superClass.bits |= ASTNode.IsSuperType;
		this.superclass = superClass;

		// TODO Auto-generated method stub
		checkAndCreateImplicitequals(problemReporter);
		checkAndCreateImplicitHashCode(problemReporter);
		checkAndCreateImplicitToString(problemReporter);
	}
	AbstractMethodDeclaration[] getMethod(char[] name1) {
		if (name1 == null || name1.length == 0 || this.methods == null)
			return null;
		List<AbstractMethodDeclaration> amList = new ArrayList<>(0);
		for (AbstractMethodDeclaration amd : this.methods) {
			if (CharOperation.equals(name1, amd.selector))
				amList.add(amd);
		}
		return amList.toArray(new AbstractMethodDeclaration[0]);
	}
	private void checkAndCreateImplicitToString(ProblemReporter problemReporter) {
		if (null != getMethodByName(TypeConstants.TOSTRING))
			return;
		QualifiedTypeReference returnType = new QualifiedTypeReference(JAVA_LANG_STRING, RecordDeclaration.EMPTY_LONG_POS);
		MethodDeclaration md = createMethodDeclaration(TypeConstants.TOSTRING, returnType);
		MarkerAnnotation overrideAnnotation = new MarkerAnnotation(new SingleTypeReference(TypeConstants.JAVA_LANG_OVERRIDE[2], 0), 0);
		md.annotations = new Annotation[] { overrideAnnotation };
		// getClass().getName() + "@" + Integer.toHexString(hashCode())
		Expression left = new StringLiteral(this.name, -1, -1, -1);
		Expression right = new StringLiteral(new char[] {'@'}, -1, -1, -1);
		left = new BinaryExpression(left, right, OperatorIds.PLUS);
		MessageSend m = new MessageSend();
		m.receiver = new QualifiedNameReference(
				TypeConstants.JAVA_LANG_INTEGER,
				RecordDeclaration.EMPTY_LONG_POS, -1, -1);
		m.selector = "toHexString".toCharArray(); //$NON-NLS-1$
		MessageSend hc = new MessageSend();
		hc.receiver = new ThisReference(-1, -1);
		hc.selector = TypeConstants.HASHCODE;
		m.arguments = new Expression[] {hc};
		right = m;
		CombinedBinaryExpression cbe = new CombinedBinaryExpression(left, right, OperatorIds.PLUS, 1);
		md.statements = new Statement[] { new ReturnStatement(cbe, -1, -1) };
	}
	private static char[][] getBoxedName(char[] token) {
		return
			token == BYTE    ? JAVA_LANG_BYTE :
			token == SHORT   ? JAVA_LANG_SHORT :
			token == CHAR    ? JAVA_LANG_CHARACTER :
			token == INT     ? JAVA_LANG_INTEGER :
			token == LONG    ? JAVA_LANG_LONG :
			token == FLOAT   ? JAVA_LANG_FLOAT :
			token == DOUBLE  ? JAVA_LANG_DOUBLE :
			token == BOOLEAN ? JAVA_LANG_BOOLEAN : null;
	}
	private void checkAndCreateImplicitHashCode(ProblemReporter problemReporter) {
		if (null != getMethodByName(TypeConstants.HASHCODE))
			return;
		MethodDeclaration md = createMethodDeclaration(TypeConstants.HASHCODE, TypeReference.baseTypeReference(TypeIds.T_int, 0));
		MarkerAnnotation overrideAnnotation = new MarkerAnnotation(new SingleTypeReference(TypeConstants.JAVA_LANG_OVERRIDE[2], 0), 0);
		md.annotations = new Annotation[] { overrideAnnotation };

		List<Expression> initVals = new ArrayList<>();
		if (this.args != null) {
			for (Argument arg : this.args) {
				if (RecordDeclaration.disallowedComponentNames.contains(new String(arg.name)))
					continue;
				FieldReference fr = new FieldReference(arg.name, -1);
				fr.receiver = new ThisReference(-1, -1);
				Expression receiver = null;
				if (arg.type instanceof ArrayTypeReference) {
					MessageSend arraysHashCode = new MessageSend();
					arraysHashCode.selector = HASHCODE;
					arraysHashCode.receiver =
							new QualifiedNameReference(JAVA_UTIL_ARRAYS,
							RecordDeclaration.EMPTY_LONG_POS, -1, -1);
					arraysHashCode.arguments = new Expression[] { fr};
					initVals.add(arraysHashCode);
					continue;
				} else if (arg.type.isBaseTypeReference()) {
					QualifiedNameReference boxReference = new QualifiedNameReference(
							RecordDeclaration.getBoxedName(arg.type.getLastToken()),
							RecordDeclaration.EMPTY_LONG_POS, -1, -1);
					MessageSend m = new MessageSend();
					m.receiver = boxReference;
					m.selector = TypeConstants.VALUEOF;  // Integer.valueOf(int)
					m.arguments = new Expression[] { fr };
					receiver = m;
				} else {
					receiver = fr; //this.field
				}
				MessageSend messageSend = new MessageSend();
				messageSend.receiver = receiver;
				messageSend.selector = HASHCODE;
				initVals.add(messageSend);
			}
		}
		ArrayInitializer ai = new ArrayInitializer();
		ai.expressions = initVals.toArray(new Expression[0]);
		ArrayAllocationExpression aae = new ArrayAllocationExpression();
		aae.dimensions = new Expression[1];
		aae.dimensions[0] = null;
		aae.initializer = ai;
		aae.type = new SingleTypeReference(INT, -1);
		MessageSend arraysHashCode = new MessageSend();
		arraysHashCode.selector = HASHCODE;
		arraysHashCode.receiver =
				new QualifiedNameReference(JAVA_UTIL_ARRAYS,
				RecordDeclaration.EMPTY_LONG_POS, -1, -1);
		arraysHashCode.arguments = new Expression[] { aae };
		md.statements = new Statement[] { new ReturnStatement(arraysHashCode, -1, -1) };
	}
	private AbstractMethodDeclaration getMethodByName(char[] name1) {
		AbstractMethodDeclaration[] ams = getMethod(name1);
		for (AbstractMethodDeclaration amd : ams) {
			Argument[] args1 = amd.arguments;
			if (args1 == null || args1.length == 0)
				return amd; // explicit method exists, no need to create an implicit one.
		}
		return null;
	}
	private void checkAndCreateImplicitequals(ProblemReporter problemReporter) {
		AbstractMethodDeclaration[] ams = getMethod(TypeConstants.EQUALS);
		for (AbstractMethodDeclaration amd : ams) {
			Argument[] args1 = amd.arguments;
			if (args1 == null || args1.length != 1)
				continue;
			char[][] typeRef = args1[0].type != null ? args1[0].type.getTypeName() : null;
			if (typeRef == null)
				continue;
			if (CharOperation.equals(typeRef, JAVA_LANG_OBJECT)) {
				return; // explicit method exists, no need to create an implicit one.
			}
		}
		// In case of just object, we can never be sure - so create one implicit method 
		// and at resolution time, remove the implicit one if there is a conflict.
		// so, at this point we create one anyway.
		MethodDeclaration md = createMethodDeclaration(TypeConstants.EQUALS, TypeReference.baseTypeReference(TypeIds.T_boolean, 0));
		MarkerAnnotation overrideAnnotation = new MarkerAnnotation(new SingleTypeReference(TypeConstants.JAVA_LANG_OVERRIDE[2], 0), 0);
		md.annotations = new Annotation[] { overrideAnnotation };
		TypeReference objectTypeReference = new QualifiedTypeReference(JAVA_LANG_OBJECT, new long[] {0L, 0L, 0L});
		char[] objName = new char[] {'o','b','j'};
		md.arguments = new Argument[] { new Argument(objName, 0, objectTypeReference, 0)};

		List<Statement> stmts = new ArrayList<>();
		// if (!(obj instanceof recordName))
		//    return false;
		InstanceOfExpression ioe = new InstanceOfExpression(new SingleNameReference(objName, 0), new SingleTypeReference(this.name, 0L));
		Expression condition = new UnaryExpression(ioe, OperatorIds.NOT);
		Statement thenStatement = new ReturnStatement(new FalseLiteral(-1, -1), -1, -1);
		IfStatement ifStatement = new IfStatement(condition, thenStatement, -1, -1);
		stmts.add(ifStatement);

		// recordType o = (recordType) obj;
		LocalDeclaration ld = new LocalDeclaration(new char[] {'o'}, -1, -1);
		ld.type = new SingleTypeReference(this.name, -1);
		ld.initialization = new CastExpression(new SingleNameReference(objName, -1), new SingleTypeReference(this.name, 0L));
		stmts.add(ld);

		// check each field - ref javadoc of java.lang.Record#equals
		/*
		 * Impl Spec: The implicitly provided implementation returns true if and only if the argument is an
		 * instance of the same record type as this object, and each component of this record is equal to the
		 * corresponding component of the argument, according to java.util.Objects.equals(Object, Object) for
		 * components whose types are reference types, and according to the semantics of the equals method on
		 * the corresponding primitive wrapper type.
		 */
		if (this.args != null) {
			for (Argument arg : this.args) {
				if (RecordDeclaration.disallowedComponentNames.contains(new String(arg.name)))
					continue;
				FieldReference fr = new FieldReference(arg.name, -1);
				fr.receiver = new ThisReference(-1, -1);
				char [][] qfrName = new char[][] { {'o'}, arg.name };
				long [] qfrPos = {-1, -1};
				QualifiedNameReference ofr = new QualifiedNameReference(qfrName, qfrPos, -1, -1);
				if (arg.type.isBaseTypeReference()) {
					condition = new EqualExpression(fr, ofr, OperatorIds.NOT_EQUAL);
				} else {
					MessageSend messageSend = new MessageSend();
					messageSend.selector = TypeConstants.EQUALS;
					messageSend.arguments = new Expression[] {fr, ofr};
					messageSend.receiver = new QualifiedNameReference(JAVA_UTIL_OBJECTS, new long[] {-1, -1, -1}, -1, -1);
					condition = new UnaryExpression(messageSend, OperatorIds.NOT);
				}
				thenStatement = new ReturnStatement(new FalseLiteral(-1, -1), -1, -1);
				ifStatement = new IfStatement(condition, thenStatement, -1, -1);
				stmts.add(ifStatement);
			}
		}
		stmts.add(new ReturnStatement(new TrueLiteral(-1, -1), -1, -1));
		md.statements = stmts.toArray(new Statement[0]);
	}
	private MethodDeclaration createMethodDeclaration(char[] name1, TypeReference returnType) {
		MethodDeclaration m = new MethodDeclaration(this.compilationResult);
		m.selector = name1;
		m.modifiers = ClassFileConstants.AccPublic;

		m.returnType = returnType;

		if (this.methods == null) { // Where is the constructor?
			this.methods = new AbstractMethodDeclaration[] { m };
		} else {
			AbstractMethodDeclaration[] newMethods;
			System.arraycopy(
				this.methods,
				0,
				newMethods = new AbstractMethodDeclaration[this.methods.length + 1],
				1,
				this.methods.length);
			newMethods[0] = m;
			this.methods = newMethods;
		}
		m.bits |= ASTNode.IsImplicit;
		return m;
	}
}
