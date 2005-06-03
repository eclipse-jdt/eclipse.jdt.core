/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.util.CompilationUnitSorter;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;

/**
 * This operation is used to sort elements in a compilation unit according to
 * certain criteria.
 * 
 * @since 2.1
 */
public class SortElementsOperation extends JavaModelOperation {
	
	Comparator comparator;
	int[] positions;
	
	/**
	 * Constructor for SortElementsOperation.
	 * @param elements
	 * @param positions
	 * @param comparator
	 */
	public SortElementsOperation(IJavaElement[] elements, int[] positions, Comparator comparator) {
		super(elements);
		this.comparator = comparator;
		this.positions = positions;
	}

	/**
	 * Returns the amount of work for the main task of this operation for
	 * progress reporting.
	 */
	protected int getMainAmountOfWork(){
		return this.elementsToProcess.length;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.core.JavaModelOperation#executeOperation()
	 */
	protected void executeOperation() throws JavaModelException {
		try {
			beginTask(Messages.operation_sortelements, getMainAmountOfWork()); 
			CompilationUnit copy = (CompilationUnit) this.elementsToProcess[0];
			ICompilationUnit unit = copy.getPrimary();
			IBuffer buffer = copy.getBuffer();
			if (buffer  == null) { 
				return;
			}
			char[] bufferContents = buffer.getCharacters();
			String result = processElement(unit, bufferContents);
			if (!CharOperation.equals(result.toCharArray(), bufferContents)) {
				copy.getBuffer().setContents(result);
			}
			worked(1);
		} finally {
			done();
		}
	}

	/**
	 * Method processElement.
	 * @param unit
	 * @param source
	 */
	private String processElement(ICompilationUnit unit, char[] source) {
		CompilerOptions options = new CompilerOptions(unit.getJavaProject().getOptions(true));
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(options.getMap());
		parser.setSource(source);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(false);
		org.eclipse.jdt.core.dom.CompilationUnit domUnit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);
		domUnit.accept(new ASTVisitor() {
			public boolean visit(org.eclipse.jdt.core.dom.CompilationUnit compilationUnit) {
				List types = compilationUnit.types();
				for (Iterator iter = types.iterator(); iter.hasNext();) {
					AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) iter.next();
					typeDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER, new Integer(typeDeclaration.getStartPosition()));
				}
				return true;
			}
			public boolean visit(AnnotationTypeDeclaration annotationTypeDeclaration) {
				List bodyDeclarations = annotationTypeDeclaration.bodyDeclarations();
				for (Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
					BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
					bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER, new Integer(bodyDeclaration.getStartPosition()));
				}
				return true;
			}

			public boolean visit(AnonymousClassDeclaration anonymousClassDeclaration) {
				List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
				for (Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
					BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
					bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER, new Integer(bodyDeclaration.getStartPosition()));
				}
				return true;
			}
			
			public boolean visit(TypeDeclaration typeDeclaration) {
				List bodyDeclarations = typeDeclaration.bodyDeclarations();
				for (Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
					BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
					bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER, new Integer(bodyDeclaration.getStartPosition()));
				}
				return true;
			}

			public boolean visit(EnumDeclaration enumDeclaration) {
				List bodyDeclarations = enumDeclaration.bodyDeclarations();
				for (Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
					BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
					bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER, new Integer(bodyDeclaration.getStartPosition()));
				}
				return true;
			}			
		});
		final AST localAst = domUnit.getAST();
		final ASTRewrite rewriter = ASTRewrite.create(localAst);
		RangeMarker[] markers = null;
		
		final boolean needPositionsMapping = this.positions != null;
		if (needPositionsMapping) {
			markers = new RangeMarker[this.positions.length];
			for (int i= 0; i < this.positions.length; i++) {
				markers[i]= new RangeMarker(this.positions[i], 0);
			}
		}
		String generatedSource = new String(source);
		Document document = new Document(generatedSource);
		domUnit.accept(new ASTVisitor() {
			public boolean visit(org.eclipse.jdt.core.dom.CompilationUnit compilationUnit) {
				ListRewrite listRewrite = rewriter.getListRewrite(compilationUnit, org.eclipse.jdt.core.dom.CompilationUnit.TYPES_PROPERTY);
				List types = compilationUnit.types();
				final int length = types.size();
				if (length > 1) {
					final List myCopy = new ArrayList();
					myCopy.addAll(types);
					Collections.sort(myCopy, SortElementsOperation.this.comparator);
					for (int i = 0; i < length; i++) {
						listRewrite.replace((ASTNode) types.get(i), rewriter.createMoveTarget((ASTNode) myCopy.get(i)), null);
					}
				}
				return true;
			}
			public boolean visit(AnnotationTypeDeclaration annotationTypeDeclaration) {
				ListRewrite listRewrite = rewriter.getListRewrite(annotationTypeDeclaration, AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				List bodyDeclarations = annotationTypeDeclaration.bodyDeclarations();
				final int length = bodyDeclarations.size();
				if (length > 1) {
					final List myCopy = new ArrayList();
					myCopy.addAll(bodyDeclarations);
					Collections.sort(myCopy, SortElementsOperation.this.comparator);
					for (int i = 0; i < length; i++) {
						listRewrite.replace((ASTNode) bodyDeclarations.get(i), rewriter.createMoveTarget((ASTNode) myCopy.get(i)), null);
					}
				}
				return true;
			}

			public boolean visit(AnonymousClassDeclaration anonymousClassDeclaration) {
				ListRewrite listRewrite = rewriter.getListRewrite(anonymousClassDeclaration, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
				List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
				final int length = bodyDeclarations.size();
				if (length > 1) {
					final List myCopy = new ArrayList();
					myCopy.addAll(bodyDeclarations);
					Collections.sort(myCopy, SortElementsOperation.this.comparator);
					for (int i = 0; i < length; i++) {
						listRewrite.replace((ASTNode) bodyDeclarations.get(i), rewriter.createMoveTarget((ASTNode) myCopy.get(i)), null);
					}
				}
				return true;
			}
			
			public boolean visit(TypeDeclaration typeDeclaration) {
				ListRewrite listRewrite = rewriter.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
				List bodyDeclarations = typeDeclaration.bodyDeclarations();
				final int length = bodyDeclarations.size();
				if (length > 1) {
					final List myCopy = new ArrayList();
					myCopy.addAll(bodyDeclarations);
					Collections.sort(myCopy, SortElementsOperation.this.comparator);
					for (int i = 0; i < length; i++) {
						listRewrite.replace((ASTNode) bodyDeclarations.get(i), rewriter.createMoveTarget((ASTNode) myCopy.get(i)), null);
					}
				}
				return true;
			}

			public boolean visit(EnumDeclaration enumDeclaration) {
				ListRewrite listRewrite = rewriter.getListRewrite(enumDeclaration, EnumDeclaration.BODY_DECLARATIONS_PROPERTY);
				List bodyDeclarations = enumDeclaration.bodyDeclarations();
				int length = bodyDeclarations.size();
				if (length > 1) {
					final List myCopy = new ArrayList();
					myCopy.addAll(bodyDeclarations);
					Collections.sort(myCopy, SortElementsOperation.this.comparator);
					for (int i = 0; i < length; i++) {
						listRewrite.replace((ASTNode) bodyDeclarations.get(i), rewriter.createMoveTarget((ASTNode) myCopy.get(i)), null);
					}
				}				
				listRewrite = rewriter.getListRewrite(enumDeclaration, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
				List enumConstants = enumDeclaration.enumConstants();
				length = enumConstants.size();
				if (length > 1) {
					final List myCopy = new ArrayList();
					myCopy.addAll(enumConstants);
					Collections.sort(myCopy, SortElementsOperation.this.comparator);
					for (int i = 0; i < length; i++) {
						listRewrite.replace((ASTNode) enumConstants.get(i), rewriter.createMoveTarget((ASTNode) myCopy.get(i)), null);
					}
				}
				return true;
			}
		});			
		TextEdit edits = rewriter.rewriteAST(document, null);
		if (needPositionsMapping) {
			for (int i = 0, max = markers.length; i < max; i++) {
				insert(edits, markers[i]);
			}
		}
		try {
			edits.apply(document, TextEdit.UPDATE_REGIONS);
			generatedSource = document.get();
			if (needPositionsMapping) {
				for (int i= 0, max = markers.length; i < max; i++) {
					this.positions[i]= markers[i].getOffset();
				}
			}
		} catch (BadLocationException e) {
			// ignore
		}
		return generatedSource;
	}

	/**
	 * Possible failures:
	 * <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is <code>null</code></li>.
	 *  <li>INVALID_ELEMENT_TYPES - the supplied elements are not an instance of IWorkingCopy</li>.
	 * </ul>
	 * @return IJavaModelStatus
	 */
	public IJavaModelStatus verify() {
		if (this.elementsToProcess.length != 1) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		if (this.elementsToProcess[0] == null) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		if (!(this.elementsToProcess[0] instanceof ICompilationUnit) || !((ICompilationUnit) this.elementsToProcess[0]).isWorkingCopy()) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this.elementsToProcess[0]);
		}
		return JavaModelStatus.VERIFIED_OK;
	}
	
	public static void insert(TextEdit parent, TextEdit edit) {
		if (!parent.hasChildren()) {
			parent.addChild(edit);
			return;
		}
		TextEdit[] children= parent.getChildren();
		// First dive down to find the right parent.
		for (int i= 0; i < children.length; i++) {
			TextEdit child= children[i];
			if (covers(child, edit)) {
				insert(child, edit);
				return;
			}
		}
		// We have the right parent. Now check if some of the children have to
		// be moved under the new edit since it is covering it.
		for (int i= children.length - 1; i >= 0; i--) {
			TextEdit child= children[i];
			if (covers(edit, child)) {
				parent.removeChild(i);
				edit.addChild(child);
			}
		}
		parent.addChild(edit);
	}
	
	private static boolean covers(TextEdit thisEdit, TextEdit otherEdit) {
		if (thisEdit.getLength() == 0) {
			return false;
		}
		
		int thisOffset= thisEdit.getOffset();
		int thisEnd= thisEdit.getExclusiveEnd();	
		if (otherEdit.getLength() == 0) {
			int otherOffset= otherEdit.getOffset();
			return thisOffset <= otherOffset && otherOffset < thisEnd;
		} else {
			int otherOffset= otherEdit.getOffset();
			int otherEnd= otherEdit.getExclusiveEnd();
			return thisOffset <= otherOffset && otherEnd <= thisEnd;
		}
	}
}
