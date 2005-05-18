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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.NonEclipseImplementationException;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.dom.ASTNode;

public class MessagerImpl implements Messager, EclipseMessager
{
    private final ProcessorEnvImpl _env;

    MessagerImpl(ProcessorEnvImpl env){
        _env = env;
    }
    
    public void printError(SourcePosition pos, String msg)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, IMarker.SEVERITY_ERROR, msg);
        else if (pos == null )
			printError(msg);        
        else
            throw new NonEclipseImplementationException("only applicable to eclipse mirror objects." +
                                                         " Found " + pos.getClass().getName());
    }
	
	public void printError(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null");
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		addMarker(_env.getFile(), start, node.getLength() + start, IMarker.SEVERITY_ERROR, msg, line );
	}

    public void printError(String msg)
    {
        print(IMarker.SEVERITY_ERROR, msg);
    }

    public void printNotice(SourcePosition pos, String msg)
    {
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, IMarker.SEVERITY_INFO, msg);
		else if (pos == null )
			printNotice(msg); 
        else
            throw new NonEclipseImplementationException("only applicable to eclipse mirror objects." +
                                                         " Found " + pos.getClass().getName());
    }
	
	public void printNotice(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null");
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		addMarker(_env.getFile(), start, node.getLength() + start, IMarker.SEVERITY_INFO, msg, line );
	}

    public void printNotice(String msg)
    {
       print(IMarker.SEVERITY_INFO, msg);
    }

    public void printWarning(SourcePosition pos, String msg)
    {		
        if( pos instanceof SourcePositionImpl )
            print((SourcePositionImpl)pos, IMarker.SEVERITY_WARNING, msg);
		else if (pos == null )
			printWarning(msg); 
        else
            throw new NonEclipseImplementationException("only applicable to eclipse mirror objects." +
                                                         " Found " + pos.getClass().getName());
    }
	
	public void printWarning(ASTNode node, String msg)
	{
		if( node == null )
			throw new IllegalArgumentException("'node' cannot be null");
		final int start = node.getStartPosition();
		final int line = _env.getAstCompilationUnit().lineNumber(start);
		addMarker(_env.getFile(), start, node.getLength() + start, IMarker.SEVERITY_WARNING, msg, line );
	}

    public void printWarning(String msg)
    {
        print(IMarker.SEVERITY_WARNING, msg);
    }

    private void print(SourcePositionImpl pos,
                       int severity,
                       String msg)
    {

        final int start = pos.getStartingOffset();
        final int end   = pos.getEndingOffset();
        final IResource resource = pos.getResource();
        if( resource == null ){
			throw new IllegalStateException("missing resource");            
        }
        else{
           addMarker(resource, 
				   	 pos.getStartingOffset(), 
				   	 pos.getEndingOffset(), 
				     severity, 
				     msg, 
				     pos.line());
        }
    }

    private void print(int severity, String msg)
    {
		addMarker(null, 0, 1, severity, msg, 1);	
    }

    private void addMarker(final IResource resource, final int start, final int end,
                            final int severity, String msg, final int line)
    {         
		if( msg == null )
			msg = "<no message>";		
        Map<String, Object> map = new HashMap<String, Object>(8); // entries = 6, loadFactory = 0.75 thus, capacity = 8
        map.put( IMarker.CHAR_START, start );
        map.put( IMarker.CHAR_END, end );
        map.put( IMarker.SEVERITY, severity );
        map.put( IMarker.MESSAGE, msg );
		map.put( IMarker.LINE_NUMBER, line);
		map.put( IMarker.LOCATION, "line: " + line);
        map = Collections.unmodifiableMap(map);
        _env.addMarker(resource, map);
    }
}
