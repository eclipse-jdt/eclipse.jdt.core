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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class RecordDeclaration extends TypeDeclaration {

	private Argument[] args;
	public int nRecordComponents;

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
		ConstructorDeclaration cd = null;
		//if a constructor has not the name of the type,
		//convert it into a method with 'null' as its return type
		boolean hasConstructor = true;
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
						if (this.args == null) {
							if (am.arguments == null)
								return (ConstructorDeclaration) am;
							continue; // else check next one.
						} else {
							if (am.arguments == null || am.arguments.length != this.args.length)
								continue;
							for (int j = 0; j < this.args.length; j++) {
								if (!CharOperation.equals(this.args[j].type.getLastToken(),
										am.arguments[j].type.getLastToken())) {
									hasConstructor = false;
									break;
								}
								/*TODO: Strictly speaking, at this point we can only say
								 * that there is high possibility that there is a constructor
								 * If it is false, then definitely it is false; else we need
								 * to check the bindings to say that there is a canonical
								 * constructor. To take care at binding resolution time.
								 */
							}
							if (hasConstructor)
								return (ConstructorDeclaration) am;
						}
						
					}
				}
			}
		}
		return cd;
	}
	@Override
	public ConstructorDeclaration createDefaultConstructor(	boolean needExplicitConstructorCall, boolean needToInsert) {
		//Add to method'set, the default constuctor that just recall the
		//super constructor with no arguments
		//The arguments' type will be positionned by the TC so just use
		//the default int instead of just null (consistency purpose)

		CompactConstructorDeclaration ccd = new CompactConstructorDeclaration(this.compilationResult);
//		constructor.bits |= ASTNode.IsDefaultConstructor;
		ccd.selector = this.name;
		ccd.modifiers = this.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
		ccd.modifiers |= ClassFileConstants.AccPublic; // JLS 14 8.10.5
		ccd.isImplicit = true;
		ccd.recordDeclaration = this;
		ccd.arguments = this.args;

		//if you change this setting, please update the
		//SourceIndexer2.buildTypeDeclaration(TypeDeclaration,char[]) method
		ccd.declarationSourceStart = ccd.sourceStart = this.sourceStart;
		ccd.declarationSourceEnd =
			ccd.sourceEnd = ccd.bodyEnd = this.sourceEnd;

		//the super call inside the constructor
		if (needExplicitConstructorCall) {
			ccd.constructorCall = SuperReference.implicitSuperConstructorCall();
			ccd.constructorCall.sourceStart = this.sourceStart;
			ccd.constructorCall.sourceEnd = this.sourceEnd;
		}

		//adding the constructor in the methods list: rank is not critical since bindings will be sorted
		if (needToInsert) {
			if (this.methods == null) {
				this.methods = new AbstractMethodDeclaration[] { ccd };
			} else {
				AbstractMethodDeclaration[] newMethods;
				System.arraycopy(
					this.methods,
					0,
					newMethods = new AbstractMethodDeclaration[this.methods.length + 1],
					1,
					this.methods.length);
				newMethods[0] = ccd;
				this.methods = newMethods;
			}
		}
		return ccd;
	}

	public void createDefaultAccessors(ProblemReporter problemReporter) {
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
	private AbstractMethodDeclaration createNewMethod(FieldDeclaration f) {
		MethodDeclaration m = new MethodDeclaration(this.compilationResult);
		m.selector = f.name;
		m.bits |= ASTNode.IsSynthetic;
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
	public StringBuffer printHeader(int indent, StringBuffer output) {
		printModifiers(this.modifiers & ~ClassFileConstants.AccRecord, output); // mask record alias volatile
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
}
