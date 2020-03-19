/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.util;

import com.sun.mirror.util.SourcePosition;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationValueImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;

public class SourcePositionImpl implements SourcePosition
{
    private final int _startingOffset;
    private final int _length;
    private final int _line;
    private final int _column;
    /** the back pointer to the declaration that created this object */
    private final EclipseMirrorObject _decl;

    public SourcePositionImpl(final int startingOffset,
                              final int length,
                              final int line,
                              final int column,
                              final EclipseDeclarationImpl decl)
    {
        _startingOffset = startingOffset;
        _length = length;
        _line = line < 1 ? 1 : line;
        _column = column < 0 ? 0 : column;
        _decl = decl;
        assert decl != null : "missing declaration [decl] == null."; //$NON-NLS-1$
    }

	public SourcePositionImpl(final int startingOffset,
							  final int length,
							  final int line,
							  final int column,
							  final AnnotationValueImpl decl )
	{
		_startingOffset = startingOffset;
        _length = length;
        _line = line < 1 ? 1 : line;
        _column = column < 0 ? 0 : column;
        _decl = decl;
        assert decl != null : "missing declaration [decl] == null."; //$NON-NLS-1$
	}

	public SourcePositionImpl(final int startingOffset,
							  final int length,
							  final int line,
							  final int column,
							  final AnnotationMirrorImpl decl )
	{
		_startingOffset = startingOffset;
        _length = length;
        _line = line < 1 ? 1 : line;
        _column = column < 0 ? 0 : column;
        _decl = decl;
        assert decl != null : "missing declaration [decl] == null."; //$NON-NLS-1$
	}

    @Override
	public int line(){ return _line; }
    @Override
	public int column(){ return _column; }
    @Override
	public File file(){
        IResource resource = getResource();
        if( resource == null ) return null;
        final IPath absPath = resource.getRawLocation();
        if(absPath == null) return null;
        return new File( absPath.toOSString() );
    }

    // for use in IDE mode for squiggling.
    public int getStartingOffset(){ return _startingOffset; }
    public int getEndingOffset(){ return _startingOffset + _length; }
    public int getLength(){ return _length; }
    public IFile getResource(){
		if( _decl instanceof EclipseDeclarationImpl )
			return ((EclipseDeclarationImpl)_decl).getResource();
		else if( _decl instanceof AnnotationMirrorImpl )
			return ((AnnotationMirrorImpl)_decl).getResource();
		else if( _decl instanceof AnnotationValueImpl )
			return ((AnnotationValueImpl)_decl).getResource();

		throw new IllegalStateException();
    }

    @Override
	public String toString()
    {
    	StringBuilder buffer = new StringBuilder();
    	buffer.append("offset = "); //$NON-NLS-1$
    	buffer.append(_startingOffset);
    	buffer.append(" line = "); //$NON-NLS-1$
    	buffer.append( _line );
    	buffer.append(" column = "); //$NON-NLS-1$
    	buffer.append( _column );
    	buffer.append(" length = "); //$NON-NLS-1$
    	buffer.append( _length );

    	return buffer.toString();
    }
}
