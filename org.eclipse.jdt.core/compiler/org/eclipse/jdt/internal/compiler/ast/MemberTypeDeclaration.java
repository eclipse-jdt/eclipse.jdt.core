package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.problem.AbortType;

public class MemberTypeDeclaration extends InnerTypeDeclaration {
	public TypeDeclaration enclosingType;
	/**
	 *	Iteration for a member innertype
	 *
	 */
	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {
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

}
