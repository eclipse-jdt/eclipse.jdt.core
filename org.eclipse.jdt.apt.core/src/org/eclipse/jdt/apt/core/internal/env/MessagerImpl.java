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

package org.eclipse.jdt.apt.core.internal.env;

import com.sun.mirror.util.SourcePosition;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class MessagerImpl implements EclipseMessager
{
	public static enum Severity {
		ERROR,
		WARNING,
		INFO
	}
    private final AbstractCompilationEnv _env;

    MessagerImpl(AbstractCompilationEnv env){
        _env = env;
    }

    public void printError(SourcePosition pos, String msg, String[] arguments)
    {
    	if( pos == null )
    		printError(msg);
    	else if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, Severity.ERROR, msg, arguments);
    	else
    		print(pos, Severity.ERROR, msg, arguments);
    }

	@Override
	public void printError(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		// The only time you get a dom AST node is when you are processing in a per-file mode.
		// _env.getAstCompilationUnit() && _env.getFile() will return an non-null value.
		int line = _env.getAstCompilationUnit().getLineNumber(start);
		if( line < 1 )
			line = 1;
		_env.addMessage(_env.getFile(), start, node.getLength() + start, Severity.ERROR, msg, line, null );
	}

    @Override
	public void printError(String msg)
    {
        print(Severity.ERROR, msg, null);
    }

    public void printNotice(SourcePosition pos, String msg, String[] arguments)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, Severity.INFO, msg, arguments);
		else if (pos == null )
			printNotice(msg);
		else
    		print(pos, Severity.INFO, msg, arguments);
    }

	@Override
	public void printNotice(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		// The only time you get a dom AST node is when you are processing in a per-file mode.
		// _env.getAstCompilationUnit() && _env.getFile() will return an non-null value.
		int line = _env.getAstCompilationUnit().getLineNumber(start);
		if( line < 1 )
			line = 1;
		_env.addMessage(_env.getFile(), start, node.getLength() + start, Severity.INFO, msg, line, null );
	}

    @Override
	public void printNotice(String msg)
    {
       print(Severity.INFO, msg, null);
    }

    public void printWarning(SourcePosition pos, String msg, String[] arguments)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, Severity.WARNING, msg, arguments);
		else if (pos == null )
			printWarning(msg);
		else
    		print(pos, Severity.WARNING, msg, arguments);
    }

	@Override
	public void printWarning(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		// The only time you get a dom AST node is when you are processing in a per-file mode.
		// _env.getAstCompilationUnit() && _env.getFile() will return an non-null value.
		int line = _env.getAstCompilationUnit().getLineNumber(start);
		if( line < 1 )
			line = 1;
		_env.addMessage(_env.getFile(), start, node.getLength() + start, Severity.WARNING, msg, line, null);
	}

    @Override
	public void printWarning(String msg)
    {
        print(Severity.WARNING, msg, null);
    }

    @Override
	public void printError(SourcePosition pos, String msg) {
		printError(pos, msg, null);
	}

	@Override
	public void printWarning(SourcePosition pos, String msg) {
		printWarning(pos, msg, null);
	}

	@Override
	public void printNotice(SourcePosition pos, String msg) {
		printNotice(pos, msg, null);
	}

	@Override
	public void printFixableError(SourcePosition pos, String msg, String pluginId, String errorId) {
		if (pluginId == null) {
			throw new IllegalArgumentException("pluginId cannot be null"); //$NON-NLS-1$
		}
		if (errorId == null) {
			throw new IllegalArgumentException("errorId cannot be null"); //$NON-NLS-1$
		}
		printError(pos, msg, new String[] {pluginId, errorId});
	}

	@Override
	public void printFixableWarning(SourcePosition pos, String msg, String pluginId, String errorId) {
		if (pluginId == null) {
			throw new IllegalArgumentException("pluginId cannot be null"); //$NON-NLS-1$
		}
		if (errorId == null) {
			throw new IllegalArgumentException("errorId cannot be null"); //$NON-NLS-1$
		}
		printWarning(pos, msg, new String[] {pluginId, errorId});
	}

	@Override
	public void printFixableNotice(SourcePosition pos, String msg, String pluginId, String errorId) {
		if (pluginId == null) {
			throw new IllegalArgumentException("pluginId cannot be null"); //$NON-NLS-1$
		}
		if (errorId == null) {
			throw new IllegalArgumentException("errorId cannot be null"); //$NON-NLS-1$
		}
		printNotice(pos, msg, new String[] {pluginId, errorId});
	}

	@Override
	public void printFixableError(String msg, String pluginId, String errorId) {
		if (pluginId == null) {
			throw new IllegalArgumentException("pluginId cannot be null"); //$NON-NLS-1$
		}
		if (errorId == null) {
			throw new IllegalArgumentException("errorId cannot be null"); //$NON-NLS-1$
		}
		print(Severity.ERROR, msg, new String[] {pluginId, errorId});
	}

	@Override
	public void printFixableWarning(String msg, String pluginId, String errorId) {
		if (pluginId == null) {
			throw new IllegalArgumentException("pluginId cannot be null"); //$NON-NLS-1$
		}
		if (errorId == null) {
			throw new IllegalArgumentException("errorId cannot be null"); //$NON-NLS-1$
		}
		print(Severity.WARNING, msg, new String[] {pluginId, errorId});
	}

	@Override
	public void printFixableNotice(String msg, String pluginId, String errorId) {
		if (pluginId == null) {
			throw new IllegalArgumentException("pluginId cannot be null"); //$NON-NLS-1$
		}
		if (errorId == null) {
			throw new IllegalArgumentException("errorId cannot be null"); //$NON-NLS-1$
		}
		print(Severity.INFO, msg, new String[] {pluginId, errorId});
	}

    private void print(SourcePositionImpl pos,
    				   Severity severity,
                       String msg,
                       String[] arguments)
    {
        final IFile resource = pos.getResource();
        if( resource == null ){
			throw new IllegalStateException("missing resource"); //$NON-NLS-1$
        }
        else{
          _env.addMessage(resource, pos.getStartingOffset(), pos.getEndingOffset(),
						  severity, msg, pos.line(), arguments);
        }
    }

    private void print(SourcePosition pos,
    				   Severity severity,
    				   String msg,
    				   String[] arguments)
    {
    	final java.io.File file = pos.file();
    	IFile resource = null;
    	if( file != null ){
    		final String projAbsPath = _env.getProject().getLocation().toOSString();
    		final String fileAbsPath = file.getAbsolutePath();
    		final String fileRelPath = fileAbsPath.substring(projAbsPath.length());
    		resource = _env.getProject().getFile(fileRelPath);
    		if( !resource.exists() )
    			resource = null;
    	}

    	int offset = -1;
    	if( resource != null ){
    		final CompilationUnit unit = _env.getASTFrom(resource);
    		if( unit != null )
    			offset = unit.getPosition( pos.line(), pos.column() );
    	}
    	_env.addMessage(resource, offset, -1, severity, msg, pos.line(), arguments );
    }

    private void print(Severity severity, String msg, String[] arguments)
    {
     	_env.addMessage(null, -1, -1, severity, msg, 1, arguments );

    }

}
