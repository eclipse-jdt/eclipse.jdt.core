/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Home for ast visitors of various flavors.
 */
@SuppressWarnings("unchecked") // JDT DOM AST API returns raw collections
public class Visitors {
	
	/**
     * Traverse the ast looking for annotations at the declaration level.
     * This visitor only operates at the declaration level. Method body
     * and field initializers and static block will be ignored.
     */
    public static final class AnnotationVisitor extends ASTVisitor
    {
        private final List<Annotation> _annotations;
        /**
         * @param annotations to be populated by this visitor
         */
        public AnnotationVisitor(final List<Annotation> annotations)
        { _annotations = annotations; }

        public boolean visit(MarkerAnnotation annotation)
        {
            _annotations.add(annotation);
            return false;
        }

        public boolean visit(SingleMemberAnnotation annotation)
        {
            _annotations.add(annotation);
            return false;
        }

        public boolean visit(NormalAnnotation annotation)
        {
            _annotations.add(annotation);
            return false;
        }

        // make sure we don't hit Arguments other than formal parameters.
        public boolean visit(Block blk){ return false; }
        public boolean visit(DoStatement doStatement){ return false; }
        public boolean visit(ForStatement forStatement){ return false; }
        public boolean visit(IfStatement ifStatement){ return false; }
        public boolean visit(TryStatement tryStatement){ return false; }
        
        public void reset(){ _annotations.clear(); }
    }
    
    /**
     * Locate all the annotations and the declaration that they annotate.
     * This visitor only operates at the declaration level. Method body
     * and field initializers and static block will be ignored.
     */
    public static final class AnnotatedNodeVisitor extends ASTVisitor
    {
        private final Map<ASTNode, List<Annotation>> _result;
        
        /**
         * @param map to be populated by this visitor. 
         *        Key is the declaration ast node and the value is the list 
         *        of annotation ast nodes that annotate the declaration.        
         */
        public AnnotatedNodeVisitor(Map<ASTNode, List<Annotation>> map)
        {
            _result = map;
        }

		/**
		 * visit package declaration
		 */
		public boolean visit(org.eclipse.jdt.core.dom.PackageDeclaration node)
        {
			final List<Annotation> annotations = node.annotations();
			if( !annotations.isEmpty() )
				_result.put(node, annotations);

            return false;
        }

		/**
		 * visit class and interface declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.TypeDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit annotation type declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.AnnotationTypeDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit enum type declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.EnumDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit field declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.FieldDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit enum constant declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.EnumConstantDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit method declaration
		 */
        public boolean visit(MethodDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit annotation type member
		 */
        public boolean visit(AnnotationTypeMemberDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

        private void visitBodyDeclaration(final BodyDeclaration node)
        {
            final List<IExtendedModifier> extMods = node.modifiers();
			List<Annotation> annos = null;
            for( IExtendedModifier extMod : extMods ){
                if( extMod.isAnnotation() ){
					if( annos == null ){
                        annos = new ArrayList<Annotation>(2);
                        _result.put(node, annos);
					}
                    annos.add((Annotation)extMod);
                }
            }
        }

		/**
		 * visiting formal parameter declaration.
		 */
		public boolean visit(SingleVariableDeclaration node)
		{
			final List<IExtendedModifier> extMods = node.modifiers();
			List<Annotation> annos = null;
            for( IExtendedModifier extMod : extMods ){
                if( extMod.isAnnotation() ){
					if( annos == null ){
                        annos = new ArrayList<Annotation>(2);
                        _result.put(node, annos);
					}
                    annos.add((Annotation)extMod);
                }
            }
			return false;
		}

		/**
		 * @return false so we skip everything beyond declaration level.
		 */
        public boolean visit(Block node)
        {   // so we don't look into anything beyond declaration level.
            return false;
        }
        public boolean visit(MarkerAnnotation node){ return false; }
        public boolean visit(NormalAnnotation node){ return false; }
        public boolean visit(SingleMemberAnnotation node){ return false; }
    }
	
    /**
     * Given an annotation locate the declaration that its annotates. 
     * This visitor only operates at the declaration level. Method body
     * and field initializers and static block will be ignored.   
     *
     */
    public static final class DeclarationFinder extends ASTVisitor
    {
    	private final Annotation _anno;
    	// The declaration, could be a body declaration or a parameter
    	// could also remain null if the annotation doesn't actually
    	// annotates anything.
    	private ASTNode _result = null;
    	public DeclarationFinder(final Annotation annotation)
    	{
    		_anno = annotation;
    	}
    	
    	/**
    	 *  @return back the result of the search.
    	 */
    	public ASTNode getAnnotatedNode(){return _result;}
    	
    	/**
    	 * We only visit nodes that can have annotations on them
    	 */
    	public boolean visit(AnnotationTypeDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(AnnotationTypeMemberDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(EnumDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(EnumConstantDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(FieldDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(MethodDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(TypeDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	public boolean visit(SingleVariableDeclaration node) {
    		return internalVisit(node);
    	}
    	
    	private boolean internalVisit(ASTNode node) {
    		// terminate the search.
    		if( _result != null ) return false;
    		int nodeStart = node.getStartPosition();
    		int nodeEnd = nodeStart + node.getLength();
    		int annoStart = _anno.getStartPosition();
    		int annoEnd = annoStart + _anno.getLength();
    		
    		if (nodeStart > annoEnd) {
    			// We've passed our position. No need to search any further
    			return false;
    		}
    		if (nodeEnd > annoStart) { // nodeStart <= annoEnd && nodeEnd > annoStart
    			// This annotation declaration surrounds the offset
    			List<IExtendedModifier> extendedModifiers;
    			if (node.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
    				SingleVariableDeclaration declaration = (SingleVariableDeclaration)node;
    				extendedModifiers = declaration.modifiers();
    			}
    			else {
    				BodyDeclaration declaration = (BodyDeclaration)node;
    				extendedModifiers = declaration.modifiers();
    			}    			
    			for (IExtendedModifier modifier : extendedModifiers) {
    				// found what we came to look for.
    				if( modifier == _anno ){
    					_result = node;
    					return false;
    				}
    			}   			
    		}
    		
    		// Keep searching
    		return true;
    	}
    	
    	/**
		 * @return false so we skip everything beyond declaration level.
		 */
        public boolean visit(Block node)
        {   // so we don't look into anything beyond declaration level.
            return false;
        }
        public boolean visit(MarkerAnnotation node){ return false; }
        public boolean visit(NormalAnnotation node){ return false; }
        public boolean visit(SingleMemberAnnotation node){ return false; }
    }
	/**
	 * Responsible for finding the ending offset of the tighest ast node match that starts 
	 * at a given offset. This ast visitor can operator on an array of offsets in one pass.   
     * @author tyeung     
     */
    public static class EndingOffsetFinder extends ASTVisitor 
    {
    	private final int[] _sortedStartingOffset;
    	/** 
    	 * parallel to <code>_sortedOffsets</code> and contains 
    	 * the ending offset of the ast node that has the tightest match for the 
    	 * corresponding starting offset.
    	 */
    	private final int[] _endingOffsets;
    	
    	/**
    	 * @param offsets the array of offsets which will be sorted.
    	 * @throws IllegalArgumentException if <code>offsets</code> is <code>null</code>.
    	 */
    	public EndingOffsetFinder(int[] offsets)
    	{
    		if(offsets == null)
    			throw new IllegalArgumentException("argument cannot be null."); //$NON-NLS-1$
    		// sort the array first
    		Arrays.sort(offsets);
    	
    		// look for duplicates.		
    		int count = 0;	
    		for( int i=1, len=offsets.length; i<len; i++){
    			if( offsets[i-1] == offsets[i] )
    				continue;			
    			count ++;
    		}	
    	
    		if( count != offsets.length ){
    			_sortedStartingOffset = new int[count];
    	
    			int index = 0;
    			for( int i=0, len=offsets.length; i<len; i++){
    				if( i != 0 && offsets[i-1] == offsets[i] )
    					continue;
    				_sortedStartingOffset[index++] = offsets[i];
    			}		
    		}
    		else{
    			_sortedStartingOffset = offsets;
    		}
    		
    		_endingOffsets = new int[count];
    		for( int i=0; i<count; i++ )
    			_endingOffsets[i] = 0;
    	}
    	
    	public void preVisit(ASTNode node) 
    	{
    		final int startingOffset = node.getStartPosition();
    		final int endingOffset = startingOffset + node.getLength();
    		// starting offset is inclusive
    		int startIndex = Arrays.binarySearch(_sortedStartingOffset, startingOffset);
    		// ending offset is exclusive
    		int endIndex = Arrays.binarySearch(_sortedStartingOffset, endingOffset);
    		if( startIndex < 0 )
    			startIndex = - startIndex - 1;		
    		if( endIndex < 0 )
    			endIndex = - endIndex - 1;
    		else 
    			// endIndex needs to be exclusive and we want to 
    			// include the 'endIndex'th entry in our computation.
    			endIndex ++; 
    		if( startIndex >= _sortedStartingOffset.length )
    			return;
    		
    		for( int i=startIndex; i<endIndex; i++ ){    			
    			if( _endingOffsets[i] == 0 )
    				_endingOffsets[i] = endingOffset;
    			else if( endingOffset < _endingOffsets[i] )
    				_endingOffsets[i] = endingOffset;
    		}
    	}
    	
    	
    	public int getEndingOffset(final int startingOffset)
    	{
    		int index = Arrays.binarySearch(_sortedStartingOffset, startingOffset);
    		if( index == -1 ) return 0;
    		return _endingOffsets[index];
    	}
    }
}
