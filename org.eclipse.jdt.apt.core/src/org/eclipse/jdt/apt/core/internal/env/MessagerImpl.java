/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.env; 

import com.sun.mirror.apt.Messager;
import com.sun.mirror.util.SourcePosition;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class MessagerImpl implements Messager, EclipseMessager
{
    private final ProcessorEnvImpl _env;

    MessagerImpl(ProcessorEnvImpl env){
        _env = env;
    }
    
    public void printError(SourcePosition pos, String msg)
    {
    	if( pos == null )
    		printError(msg);
    	else if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, APTProblem.Severity.Error, msg);
    	else
    		print(pos, APTProblem.Severity.Error, msg);
    }
	
	public void printError(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		_env.addProblem(_env.getFile(), start, node.getLength() + start, APTProblem.Severity.Error, msg, line );
	}

    public void printError(String msg)
    {
        print(APTProblem.Severity.Error, msg);
    }

    public void printNotice(SourcePosition pos, String msg)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, APTProblem.Severity.Info, msg);
		else if (pos == null )
			printNotice(msg);
		else
    		print(pos, APTProblem.Severity.Info, msg);
    }
	
	public void printNotice(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		_env.addProblem(_env.getFile(), start, node.getLength() + start, APTProblem.Severity.Info, msg, line );
	}

    public void printNotice(String msg)
    {
       print(APTProblem.Severity.Info, msg);
    }

    public void printWarning(SourcePosition pos, String msg)
    {		
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, APTProblem.Severity.Warning, msg);
		else if (pos == null )
			printWarning(msg); 
		else
    		print(pos, APTProblem.Severity.Warning, msg);
    }
	
	public void printWarning(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null"); //$NON-NLS-1$
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		_env.addProblem(_env.getFile(), start, node.getLength() + start, APTProblem.Severity.Warning, msg, line );
	}

    public void printWarning(String msg)
    {
        print(APTProblem.Severity.Warning, msg);
    }    
  
    private void print(SourcePositionImpl pos,
    				   APTProblem.Severity severity,
                       String msg)
    {

        final int start = pos.getStartingOffset();
        final int end   = pos.getEndingOffset();
        final IFile resource = pos.getResource();
        if( resource == null ){
			throw new IllegalStateException("missing resource"); //$NON-NLS-1$            
        }
        else{          
          _env.addProblem(resource, pos.getStartingOffset(), pos.getEndingOffset(), 
						  severity, msg, pos.line());
        }
    }
    
    private void print(SourcePosition pos,
    				   APTProblem.Severity severity,
    				   String msg)
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
    	else
    		resource = null;
    	 
    	final IFile currentResource = _env.getFile();
    	int offset = 0;    	
    	if( currentResource.equals(resource) ){
    		final CompilationUnit unit = _env.getAstCompilationUnit();    		
    		offset = unit.getPosition(pos.line(), pos.column() );
    	}    	
    	_env.addProblem(resource, offset, -1, severity, msg, pos.line() );   
    }

    private void print(APTProblem.Severity severity, String msg)
    {
    	_env.addProblem(null, 0, -1, severity, msg, 1 );  
		
    }
  
}
