/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.ide.filer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager.Location;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.compiler.apt.ide.dispatch.IdeAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.apt.ide.dispatch.IdeProcessingEnvImpl;

/**
 * Implementation of the Filer interface that is used in IDE mode.
 * @see org.eclipse.jdt.internal.compiler.apt.dispatch.BatchFilerImpl
 * @since 3.3
 */
public class IdeFilerImpl implements Filer {
	
	private final IdeAnnotationProcessorManager _dispatchManager;
	private final IdeProcessingEnvImpl _env;

	public IdeFilerImpl(IdeAnnotationProcessorManager dispatchManager,
			IdeProcessingEnvImpl env) {
		_dispatchManager = dispatchManager;
		_env = env;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createClassFile(java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createClassFile(CharSequence name, Element... originatingElements)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException("Creating class files is not yet implemented"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createResource(javax.tools.JavaFileManager.Location, java.lang.CharSequence, java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public FileObject createResource(Location location, CharSequence pkg,
			CharSequence relativeName, Element... originatingElements) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param originatingElements should all be source types; binary types (ie elements in jar files)
	 * will be ignored.
	 * @see javax.annotation.processing.Filer#createSourceFile(java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
			throws IOException 
	{
		//TODO: check whether file has already been generated in this run
		List<IFile> parentFiles = new ArrayList<IFile>(originatingElements.length);
		for (Element elem : originatingElements) {
			parentFiles.add(_env.getEnclosingIFile(elem));
		}
		// Convert originatingElements to List<IFile>.  The originatingElements should all 
		// be source types, else they would not be getting 
		return new IdeJavaFileObject(_env, name, parentFiles);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#getResource(javax.tools.JavaFileManager.Location, java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public FileObject getResource(Location location, CharSequence pkg, CharSequence relativeName)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
