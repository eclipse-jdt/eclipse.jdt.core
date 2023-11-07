/*******************************************************************************
 * Copyright (c) 2005, 2013 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

/*package*/ class EnvUtil {
	/**
     * Handling the following 2 cases
     * 1) For IProblems that does not have a starting and ending offset,
     * place the problem at the class name.
     *
     * 2) For IProblems that does not have an ending offset, place the ending
     * offset at the end of the tightest ast node.
     * We will only walk the ast once to determine the ending
     * offsets of all the problems that do not have the information set.
     */
    static void updateProblemLength(List<APTProblem> problems, CompilationUnit astUnit)
    {
    	// for those problems that doesn't have an ending offset, figure it out by
    	// traversing the ast.
    	// we do it once just before we post the marker so we only have to walk the ast
    	// once.
    	int count = 0;
    	int[] classNameRange = null;
    	for(IProblem problem : problems ){
			if( problem.getSourceStart() < 0 ){
				if( classNameRange == null )
					classNameRange = getClassNameRange(astUnit);
				problem.setSourceStart(classNameRange[0]);
				problem.setSourceEnd(classNameRange[1]);
				problem.setSourceLineNumber(classNameRange[2]);
			}
			if( problem.getSourceEnd() < 0 ){
				count ++;
			}
    	}

		if( count > 0 ){
			if( astUnit != null ){
				final int[] startingOffsets = new int[count];
		    	int index = 0;
    			for( IProblem problem : problems ){
    				if( problem.getSourceEnd() < 0 )
    					startingOffsets[index++] = problem.getSourceStart();
    			}

    			final EndingOffsetFinder lfinder = new EndingOffsetFinder(startingOffsets);

    			astUnit.accept( lfinder );

    	    	for(IProblem problem : problems ){
    				if( problem.getSourceEnd() < 0 ){
    					int startingOffset = problem.getSourceStart();
    					int endingOffset = lfinder.getEndingOffset(startingOffset);
    	    			if( endingOffset == 0 )
    	    				endingOffset = startingOffset;
    	    			problem.setSourceEnd(endingOffset-1);
    				}
    			}
			}
			else{
    			for(IProblem problem : problems){
    				// set the -1 source end to be the same as the source start.
    				if( problem.getSourceEnd() < problem.getSourceStart() )
    					problem.setSourceEnd(problem.getSourceStart());
    			}
    		}
		}
    }

    /**
     * @return length 3 int array with the following information.
     * at index 0: contains the starting offset, always >= 0
     * at index 1: contains the ending offset, may be a negative number.
     * at index 2: the line number
     */
    private static int[] getClassNameRange(final CompilationUnit astUnit){
    	int[] startAndEnd = null;
    	if( astUnit != null){
    		final List<AbstractTypeDeclaration> topTypes = astUnit.types();
    		if( topTypes != null && topTypes.size() > 0 ){
    			final AbstractTypeDeclaration topType = topTypes.get(0);
    			startAndEnd = new int[3];
    			final SimpleName typename = topType.getName();
    			if( typename != null ){
    				startAndEnd[0] = typename.getStartPosition();
    				// ending offsets need to be exclusive.
    				startAndEnd[1] = startAndEnd[0] + typename.getLength() - 1;
    				startAndEnd[2] = astUnit.getLineNumber(typename.getStartPosition());
    				if( startAndEnd[2] < 1 )
    					startAndEnd[2] = 1;
    			}
    			else{
    				startAndEnd[0] = topType.getStartPosition();
    				// let case 2 in updateProblemLength() kicks in.
    				startAndEnd[1] = -2;
    				startAndEnd[2] = astUnit.getLineNumber(topType.getStartPosition());
    				if( startAndEnd[2] < 1 )
    					startAndEnd[2] = 1;
    			}
    		}
    	}
    	if( startAndEnd == null )
    		// let case 2 in updateProblemLength() kicks in.
    		return new int[]{0, -2, 1};

    	return startAndEnd;
    }

    /**
     * Responsible for finding the ending offset of the ast node that has the tightest match
     * for a given offset. This ast visitor can operator on an array of offsets in one pass.
     * @author tyeung
     */
    private static class EndingOffsetFinder extends ASTVisitor
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
    	private EndingOffsetFinder(int[] offsets)
    	{
    		if(offsets == null)
    			throw new IllegalArgumentException("argument cannot be null."); //$NON-NLS-1$
    		// sort the array first
    		Arrays.sort(offsets);

    		// look for duplicates.
    		int count = 0;
    		for( int i=0, len=offsets.length; i<len; i++){
    			if( i > 0 && offsets[i-1] == offsets[i] )
    				continue;
    			count ++;
    		}

    		if( count != offsets.length ){
    			_sortedStartingOffset = new int[count];

    			int index = 0;
    			for( int i=0, len=offsets.length; i<len; i++){
    				if( i > 0 && offsets[i-1] == offsets[i] )
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

    	@Override
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
