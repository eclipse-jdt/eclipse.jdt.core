package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashSet;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class DeclarationOfReferencedTypesPattern extends TypeReferencePattern {
	HashSet knownTypes;
public DeclarationOfReferencedTypesPattern(
	char[] qualification,
	char[] simpleName,
	int matchMode, 
	boolean isCaseSensitive) {
		
	super(qualification, simpleName, matchMode, isCaseSensitive);
	this.needsResolve = true;
	this.knownTypes = new HashSet();
}

/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// need accurate match to be able to open on type ref
	if (accuracy == IJavaSearchResultCollector.POTENTIAL_MATCH) return;
	
	int maxType = -1;
	TypeBinding typeBinding = null;
	if (reference instanceof TypeReference) {
		typeBinding = ((TypeReference)reference).binding;
		maxType = Integer.MAX_VALUE;
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference)reference;
		Binding binding = qNameRef.binding;
		maxType = qNameRef.tokens.length-1;
		switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
			case BindingIds.FIELD : // reading a field
				typeBinding = ((FieldBinding)binding).declaringClass;
				int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
				maxType -= otherBindingsCount + 1;
				break;
			case BindingIds.TYPE : //=============only type ==============
				typeBinding = (TypeBinding)binding;
		}
	} else if (reference instanceof SingleNameReference) {
		typeBinding = (TypeBinding)((SingleNameReference)reference).binding;
		maxType = 1;
	}
	
	if (typeBinding == null || typeBinding instanceof BaseTypeBinding) return;
	if (typeBinding instanceof ArrayBinding) {
		typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
	}
	this.reportDeclaration(typeBinding, maxType, locator);
}
private void reportDeclaration(TypeBinding typeBinding, int maxType, MatchLocator locator) throws CoreException {
	IType type = locator.lookupType(typeBinding);
	if (type == null) return; // case of a secondary type
	IResource resource = type.getUnderlyingResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null) {
			resource = type.getJavaProject().getProject();
		}
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile)type.getClassFile(), resource);
	}
	while (maxType > 0 && type != null) {
		if (!this.knownTypes.contains(type)) {
			if (isBinary) {
				locator.reportBinaryMatch(resource, type, info, IJavaSearchResultCollector.EXACT_MATCH);
			} else {
				TypeDeclaration typeDecl = ((SourceTypeBinding)typeBinding).scope.referenceContext;
				locator.report(resource, typeDecl.sourceStart, typeDecl.sourceEnd, type, IJavaSearchResultCollector.EXACT_MATCH);
			}
			this.knownTypes.add(type);
		}
		if (isBinary) {
			typeBinding = ((BinaryTypeBinding)typeBinding).enclosingType();
		} else {
			typeBinding = ((SourceTypeBinding)typeBinding).enclosingType();
		}
		IJavaElement parent = type.getParent();
		if (parent instanceof IType) {
			type = (IType)parent;
		} else {
			type = null;
		}
		maxType--;
	}
}
}
